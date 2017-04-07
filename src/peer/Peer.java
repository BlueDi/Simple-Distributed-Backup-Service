package peer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import handlers.McHandler;
import handlers.MdbHandler;
import handlers.MdrHandler;
import interfaces.Backup;
import interfaces.Chunk;

import java.io.*;

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

	private static ArrayList<FileInformation> fileList;

	protected Peer() throws RemoteException {
		super();
	}

	/**
	 * Liga o programa aos canais multicast.
	 * 
	 * @throws IOException
	 *             Falha na liga��o aos canais multicast
	 */
	private static void joinChannels() throws IOException {
		mc.join();
		mdb.join();
		mdr.join();
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
	 * The client evokes this function through RMI. It then reads the operation
	 * argument and calls the apropriate method.
	 */
	public void handleOperation(String operation, String filePath, String replicationDegree) throws RemoteException {
		switch (operation) {
		case "BACKUP":
			operationBackup(filePath, replicationDegree);
			break;
		case "RESTORE":
			operationRestore(filePath + ".");
			break;
		case "DELETE":
			operationDelete(filePath);
			break;
		case "RECLAIM":
			operationReclaim(filePath);
			break;
		case "STATE":
			operationState();
			break;
		default:
			System.out.println("Invalid message type.");
			break;
		}
	}

	private static void sendStored() {
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

	/**
	 * Junta dois byte arrays.
	 * 
	 * @param first
	 *            array para colocar no inicio do novo array
	 * @param second
	 *            array para colocar no fim do novo array
	 * @return Array = first + second
	 */
	private byte[] joinArrays(byte[] first, byte[] second) {
		return ByteBuffer.wrap(new byte[first.length + second.length]).put(first).put(second).array();
	}

	private void operationBackup(String filePath, String replicationDegree) {
		try {
			Backup bckp = new Backup(filePath, replicationDegree);
			ArrayList<Chunk> chunkFiles = bckp.getChunkFiles();

			while (!chunkFiles.isEmpty()) {
				System.out.println("\nSending multicast BACKUP: ");
				Chunk c = chunkFiles.remove(0);
				String outMessage = "PUTCHUNK" + " " + VERSION + " " + PEER_ID + " " + c.getFileId() + " "
						+ c.getChunkNumber() + " " + c.getReplicationDegree();
				System.out.println(outMessage + " <crlf><crlf><body>");
				outMessage += " " + "\r\n" + "\r\n";

				byte[] autoBuffer = new byte[0];
				autoBuffer = joinArrays(outMessage.getBytes(), c.getContent());

				mdb.send(autoBuffer);

				sendStored();
			}
		} catch (NumberFormatException e) {
			System.out.println("Error in Peer.operationBackup(). replicationDegree isnt a number.");
		} catch (FileNotFoundException e) {
			System.out.println("Error in Peer.operationBackup(). filePath didnt match a valid file.");
		}
	}

	/**
	 * The format of the message is: CHUNK <Version> <SenderId> <FileId>
	 * <ChunkNo> <CRLF><CRLF><Body>
	 */
	private void sendChunks() {
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
	 * The format of the message is: GETCHUNK <Version> <SenderId> <FileId>
	 * <ChunkNo> <CRLF><CRLF>
	 * 
	 * @param filePath
	 */
	private void operationRestore(String filePath) {
		int i = 0;
		do {
			i++;
			String outMessage = "GETCHUNK" + " " + VERSION + " " + PEER_ID + " " + filePath + " " + i;
			System.out.println(outMessage);
			outMessage += " " + "\r\n" + "\r\n";
			byte[] buffer = outMessage.getBytes();

			mc.send(buffer);

			sendChunks();

			// TODO: isto nao funciona mas � preciso fazer uma espera antes de
			// receber as mensagens de rece��o
			try {
				Thread.sleep(Double.valueOf(Math.random() * 400).longValue());
			} catch (InterruptedException e) {
				System.out.println(
						"Error when tried to wait a random delay to send the confirmation message on the MC channel.");
			}
		} while (!mdrHandler.isEndOfFile());
	}

	private void operationDelete(String filePath) {
		String deleteMsg = "DELETE" + " " + VERSION + " " + PEER_ID + " " + filePath + " " + "\r\n" + "\r\n";
		byte[] delete = deleteMsg.getBytes();

		mc.send(delete);
	}

	private void operationReclaim(String filePath) {
		String removedMsg = "REMOVED" + " " + VERSION + " " + PEER_ID + " " + filePath
				+ " " /* + chunkNo + " " */ + "\r\n" + "\r\n";
		byte[] remove = removedMsg.getBytes();

		mc.send(remove);
	}

	public void operationState() {
		System.out.println("State.");
		File[] files = new File("./chunks").listFiles();
		for (File file : files) {
			if (file.isFile()) {
				System.out.println(file.getName());
			}
		}

	}

	/**
	 * Fun��o principal do programa.
	 * 
	 * @param args
	 *            Argumentos passados na chamada do programa
	 * @throws IOException
	 *             Caso n�o consiga criar o canal multicast ou n�o se consiga
	 *             ligar ao canal multicast.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 3 || args.length > 9) {
			System.out.println("Usage: <protocol_version> <server_id> <service_access_point>");
			System.out.println(
					"Or: <protocol_version> <server_id> <service_access_point> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
			return;
		}
		VERSION = Double.parseDouble(args[0]);
		PEER_ID = Integer.parseInt(args[1]);
		String srvc_accss_pnt = args[2];
		System.out.println("Peer: " + srvc_accss_pnt + " started.");

		// Iniciar ip e portas default
		String MC_IP = "224.0.0.2";
		int MC_PORT = 4002;
		String MDB_IP = "224.0.0.3";
		int MDB_PORT = 4003;
		String MDR_IP = "224.0.0.4";
		int MDR_PORT = 4004;

		if (args.length > 3) {
			// se receber mais que 3 tem que alterar ip e portas
			MC_IP = args[3];
			MC_PORT = Integer.parseInt(args[4]);
			MDB_IP = args[5];
			MDB_PORT = Integer.parseInt(args[6]);
			MDR_IP = args[7];
			MDR_PORT = Integer.parseInt(args[8]);
		}

		// Registring RMI
		Peer obj = new Peer();

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind(srvc_accss_pnt, obj);

		System.err.println("RMI Sucessfully Registred");

		fileList = new ArrayList<FileInformation>();
		mc = new MulticastChannel(MC_IP, MC_PORT);
		mdb = new MulticastChannel(MDB_IP, MDB_PORT);
		mdr = new MulticastChannel(MDR_IP, MDR_PORT);

		joinChannels();
		initializeListeners();
		initializeHandlers();
	}

}
