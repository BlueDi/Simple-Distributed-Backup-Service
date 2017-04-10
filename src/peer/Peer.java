package peer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import handlers.McHandler;
import handlers.MdbHandler;
import handlers.MdrHandler;
import interfaces.Backup;
import interfaces.Chunk;

public class Peer extends UnicastRemoteObject implements PeerInterface {
	private static final long serialVersionUID = 1L;

	private static MulticastChannel mc;
	private static MulticastChannel mdb;
	private static MulticastChannel mdr;

	private static MulticastListener mcListener;
	private static MulticastListener mdbListener;
	private static MulticastListener mdrListener;

	private static McHandler mcHandler;
	private static MdbHandler mdbHandler;
	private static MdrHandler mdrHandler;

	private static Thread mcListener_Thread;
	private static Thread mdbListener_Thread;
	private static Thread mdrListener_Thread;
	private static Thread mcHandler_Thread;
	private static Thread mdbHandler_Thread;
	private static Thread mdrHandler_Thread;

	private static int PEER_ID;
	private static double VERSION;

	protected Peer() throws RemoteException {
		super();
	}

	/**
	 * Inicia os Threads dos Handlers.
	 */
	private static void initializeHandlers() {
		mcHandler = new McHandler(mcListener.getQueue(), PEER_ID);
		mdbHandler = new MdbHandler(mdbListener.getQueue(), PEER_ID);
		mdrHandler = new MdrHandler(mdrListener.getQueue(), PEER_ID);

		mcHandler_Thread = new Thread(mcHandler);
		mdbHandler_Thread = new Thread(mdbHandler);
		mdrHandler_Thread = new Thread(mdrHandler);

		mcHandler_Thread.start();
		mdbHandler_Thread.start();
		mdrHandler_Thread.start();
	}

	/**
	 * Inicia os Threads dos Listeners.
	 */
	private static void initializeListeners() {
		mcListener = new MulticastListener(mc);
		mdbListener = new MulticastListener(mdb);
		mdrListener = new MulticastListener(mdr);

		mcListener_Thread = new Thread(mcListener);
		mdbListener_Thread = new Thread(mdbListener);
		mdrListener_Thread = new Thread(mdrListener);

		mcListener_Thread.start();
		mdbListener_Thread.start();
		mdrListener_Thread.start();
	}

	/**
	 * Junta dois byte arrays.
	 * 
	 * @param first
	 *            array para colocar no inicio do novo array
	 * @param second
	 *            array para colocar no fim do novo array
	 * @return Array = first + second
	 */
	private static byte[] joinArrays(byte[] first, byte[] second) {
		return ByteBuffer.wrap(new byte[first.length + second.length]).put(first).put(second).array();
	}

	/**
	 * Liga o programa aos canais multicast.
	 * 
	 * @throws IOException
	 *             Falha na ligação aos canais multicast
	 */
	private static void joinChannels() throws IOException {
		mc.join();
		mdb.join();
		mdr.join();
	}

	/**
	 * O formato da mensagem para enviar um chunk é:: CHUNK Version SenderId
	 * FileId ChunkNo CRLF;CRLF;Body
	 */
	public static void sendChunks() {
		while (!mcHandler.getChunksToSend().isEmpty()) {
			Chunk c = mcHandler.getChunksToSend().poll();
			String cnfrmtn_msg = "CHUNK" + " " + VERSION + " " + PEER_ID + " " + c.getFileId() + " "
					+ c.getChunkNumber() + " " + "\r\n" + "\r\n";
			byte[] confirmation = new byte[0];
			confirmation = joinArrays(cnfrmtn_msg.getBytes(), c.getContent());

			mdr.send(confirmation);
		}
	}

	/**
	 * O formato da mensagem de Stored é: STORED Version SenderId FileId ChunkNo
	 * CRLF;CRLF
	 */
	public static void sendStored() {
		for (Chunk c : mdbHandler.getChunksReceived()) {
			if (!c.isChecked()) {
				String cnfrmtn_msg = "STORED" + " " + VERSION + " " + PEER_ID + " " + c.getFileId() + " "
						+ c.getChunkNumber() + " " + "\r\n" + "\r\n";
				byte[] confirmation = cnfrmtn_msg.getBytes();

				mc.send(confirmation);
				c.setChecked(true);
			}
		}
	}

	public static void sendRetransmission(int senderId) {
		while (!mcHandler.getChunksToRetransmit().isEmpty()) {
			System.out.println("\nSending multicast REMOVED: ");
			Chunk c = mcHandler.getChunksToRetransmit().poll();
			String outMessage = "PUTCHUNK" + " " + VERSION + " " + senderId + " " + c.getFileId() + " "
					+ c.getChunkNumber() + " " + c.getReplicationDegree();
			System.out.println(outMessage + " <CRLF><CRLF><body>");
			outMessage += " " + "\r\n" + "\r\n";

			byte[] buffer = new byte[0];
			buffer = joinArrays(outMessage.getBytes(), c.getContent());

			pauseThread(400);

			if (!mdbHandler.checkIfReceivedRetransmission(buffer)) {
				mdb.send(buffer);
			}
		}

	}

	/**
	 * The client evokes this function through RMI. It then reads the operation
	 * argument and calls the apropriate method.
	 */
	public void handleOperation(String operation, String arg1, String arg2) throws RemoteException {
		switch (operation) {
		case "BACKUP":
			int replicationDegree = Integer.parseInt(arg2);
			operationBackup(arg1, replicationDegree);
			break;
		case "RESTORE":
			operationRestore(arg1);
			break;
		case "DELETE":
			operationDelete(arg1);
			break;
		case "REMOVED":
			int spaceToFree = Integer.parseInt(arg1);
			operationReclaim(spaceToFree);
			break;
		default:
			System.out.println("Invalid message type.");
			break;
		}
	}

	/**
	 * Operação Backup. Envia os pedidos de PUTCHUNK pelo MDB Channel. O formato
	 * da mensagem de Backup é:: GETCHUNK Version SenderId FileId ChunkNo
	 * CRLF;CRLF
	 * 
	 * @param filePath
	 *            Ficheiro a guardar
	 * @param rD
	 *            Nível de replicação
	 */
	private void operationBackup(String filePath, int rD) {
		Backup bckp = new Backup(filePath, rD);
		ArrayList<Chunk> chunkFiles = bckp.getChunkFiles();

		while (!chunkFiles.isEmpty()) {
			System.out.println("\nSending multicast BACKUP: ");
			Chunk c = chunkFiles.remove(0);
			String outMessage = "PUTCHUNK" + " " + VERSION + " " + PEER_ID + " " + c.getFileId() + " "
					+ c.getChunkNumber() + " " + c.getReplicationDegree();
			System.out.println(outMessage + " <CRLF><CRLF><body>");
			outMessage += " " + "\r\n" + "\r\n";

			byte[] autoBuffer = new byte[0];
			autoBuffer = joinArrays(outMessage.getBytes(), c.getContent());

			int i = 0;
			long sleepTime = 1000;
			do {
				i++;
				mdb.send(autoBuffer);

				pauseThread(sleepTime);
				sleepTime *= 2;
			} while (i <= 5 && !mcHandler.receivedAllStored(c));

			if (i > 5) {
				System.err.println("Did not receive enough STORED messages");
				break;
			}
		}
	}

	/**
	 * Operação Delete. Envia os pedidos de DELETE pelo MC Channel. O formato da
	 * mensagem de Delete é: DELETE Version SenderId FileId CRLF;CRLF
	 * 
	 * @param filePath
	 *            Ficheiro a apagar
	 */
	private void operationDelete(String filePath) {
		String deleteMsg = "DELETE" + " " + VERSION + " " + PEER_ID + " " + filePath + " " + "\r\n" + "\r\n";
		byte[] delete = deleteMsg.getBytes();

		mc.send(delete);
	}

	/**
	 * Operação Reclaim. Envia os pedidos de REMOVED pelo MC Channel. O formato
	 * da mensagem de Reclaim é: REMOVED Version SenderId FileId ChunkNo
	 * CRLF;CRLF
	 * 
	 * @param spaceToFree
	 *            Espaço a libertar
	 */
	private void operationReclaim(int spaceToFree) {
		Queue<Path> pathsToRemove = new LinkedList<Path>();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("chunks/"))) {
			for (Path path : directoryStream) {
				System.out.println("  " + path.toString() + " ---- " + spaceToFree);
				pathsToRemove.add(path);
				spaceToFree -= Files.size(path);

				if (spaceToFree <= 0)
					break;
			}
		} catch (IOException ex) {
			System.err.println("Error while trying to open chunks directory.");
		}

		while (!pathsToRemove.isEmpty()) {
			Path p = pathsToRemove.poll();
			String completeFilePath = p.getFileName().toString();
			String filePath = completeFilePath.substring(0, completeFilePath.lastIndexOf('.'));
			String chunkNo = completeFilePath.substring(completeFilePath.lastIndexOf('.') + 1,
					completeFilePath.length());

			chunkNo.replaceFirst("^0+(?!$)", "");

			String removedMsg = "REMOVED" + " " + VERSION + " " + PEER_ID + " " + filePath + " " + chunkNo;
			System.out.println("RECLAIM: " + removedMsg + " <CRLF><CRLF>");
			removedMsg += " " + "\r\n" + "\r\n";
			byte[] remove = removedMsg.getBytes();

			mc.send(remove);

			try {
				Files.delete(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Operação Restore. Envia os pedidos de GETCHUNK pelo MC Channel. O formato
	 * da mensagem para pedir um chunk é: GETCHUNK Version SenderId FileId
	 * ChunkNo CRLF;CRLF
	 * 
	 * @param filePath
	 *            Ficheiro a recriar
	 */
	private void operationRestore(String filePath) {
		int chunkNo = 0;
		do {
			chunkNo++;
			String outMessage = "GETCHUNK" + " " + VERSION + " " + PEER_ID + " " + filePath + " " + chunkNo;
			System.out.println("RESTORE: " + outMessage);
			outMessage += " " + "\r\n" + "\r\n";
			byte[] buffer = outMessage.getBytes();

			mc.send(buffer);

			pauseThread(500);
		} while (!mdrHandler.isEndOfFile());
	}

	/**
	 * Operação State. Cria uma mensagem sobre o estado atual do Peer.
	 */
	public String operationState() {
		String stateMsg = "STATE:\n  Peer " + PEER_ID + ":\n";

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("chunks/"))) {
			for (Path path : directoryStream)
				stateMsg += "    " + path.toString() + "\n";
		} catch (IOException ex) {
			System.err.println("Error when tried to open chunks directory for State message.");
		}

		return stateMsg;
	}

	/**
	 * Waits from 0 to time in ms.
	 * 
	 * @param time
	 *            is the max time the Thread can sleep
	 */
	private static void pauseThread(long time) {
		try {
			new Thread(() -> {
				try {
					Thread.sleep(ThreadLocalRandom.current().nextLong(time));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}) {
				{
					start();
				}
			}.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Função principal do programa.
	 * 
	 * @param args
	 *            Argumentos passados na chamada do programa
	 * @throws IOException
	 *             Caso não consiga criar o canal multicast ou não se consiga
	 *             ligar ao canal multicast.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 3 || args.length > 9) {
			System.err.println("Usage: <protocol_version> <server_id> <service_access_point>");
			System.err.println(
					"Or: <protocol_version> <server_id> <service_access_point> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
		} else {
			VERSION = Double.parseDouble(args[0]);
			PEER_ID = Integer.parseInt(args[1]);
			String srvc_accss_pnt = args[2];
			System.out.println("Peer: " + srvc_accss_pnt + " started.");

			String MC_IP = "224.0.0.2";
			int MC_PORT = 4002;
			String MDB_IP = "224.0.0.3";
			int MDB_PORT = 4003;
			String MDR_IP = "224.0.0.4";
			int MDR_PORT = 4004;

			if (args.length > 3) {
				MC_IP = args[3];
				MC_PORT = Integer.parseInt(args[4]);
				MDB_IP = args[5];
				MDB_PORT = Integer.parseInt(args[6]);
				MDR_IP = args[7];
				MDR_PORT = Integer.parseInt(args[8]);
			}

			Peer obj = new Peer();

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(srvc_accss_pnt, obj);

			System.out.println("RMI Sucessfully Registred");

			mc = new MulticastChannel(MC_IP, MC_PORT);
			mdb = new MulticastChannel(MDB_IP, MDB_PORT);
			mdr = new MulticastChannel(MDR_IP, MDR_PORT);

			joinChannels();
			initializeListeners();
			initializeHandlers();
		}
	}

}
