package peer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handlers.McHandler;
import handlers.MdbHandler;
import interfaces.Backup;
import interfaces.Chunk;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
public class Peer implements PeerInterface {
	private static MulticastChannel mc;
	private static MulticastChannel mdb;
	private static MulticastChannel mdr;

	private static MulticastListener mcListener;
	private static MulticastListener mdbListener;
	private static MulticastListener mdrListener;

	private static McHandler mcHandler;
	private static MdbHandler mdbHandler;
	//private static MdrHandler mdrHandler;

	private static Thread mcListener_Thread;
	private static Thread mdbListener_Thread;
	private static Thread mdrListener_Thread;
	private static Thread mcHandler_Thread;
	private static Thread mdbHandler_Thread;
	//private static Thread mdrHandler_Thread;

	private static int PEER_ID;

	/**
	 * Liga o programa aos canais multicast.
	 * @throws IOException Falha na ligação aos canais multicast
	 */
	private static void joinChannels() throws IOException{
		mc.join();
		mdb.join();
		mdr.join();
	}

	/**
	 * Inicia os Threads dos Listeners.
	 */
	private static void initializeListeners(){		
		mcListener = new MulticastListener(mc);
		mdbListener = new MulticastListener(mdb);
		mdrListener = new MulticastListener(mdr);

		(new Thread(mdbListener)).start();
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
	private static void initializeHandlers(){		
		mcHandler = new McHandler(mcListener.getQueue());
		mdbHandler = new MdbHandler(mdbListener.getQueue());
		//mdrHandler = new MdrHandler();

		mcHandler_Thread = new Thread(mcHandler);
		mdbHandler_Thread = new Thread(mdbHandler);
		//mdrHandler_Thread = new Thread(mdrHandler);

		mcHandler_Thread.start();
		mdbHandler_Thread.start();
		//mdrHandler_Thread.start();
	}

	private static void sendStored(){		
		for(Chunk c: mdbHandler.getChunksReceived()){
			if(!c.isChecked()){
				String cnfrmtn_msg = "STORED" + " " + "1.0" + " " + PEER_ID + " " + c.getFileId() + " " + c.getChunkNumber() + " " + "0xD0xA" + " " + "0xD0xA";
				byte[] confirmation = cnfrmtn_msg.getBytes();

//				try {
//					Thread.sleep(Double.valueOf(Math.random() * 400).longValue());
//				} catch (InterruptedException e) {
//					System.out.println("Error when tried to wait a random delay to send the confirmation message on the MC channel.");
//					e.printStackTrace();
//				}

				mc.send(confirmation);
				c.setChecked(true);
			}
		}
	}
	
	private static void sendDelete(){
		String delete_msg = "DELETE" + " " + "1.0" + " " + PEER_ID + " " + "lorem_ipsum.txt" + " " + "0xD0xA" + " " + "0xD0xA";
		byte[] confirmation = delete_msg.getBytes();
		
		mc.send(confirmation);
	}

	public static void execute(String peer_ap, String operation, String filePath, String replicationDegree){
		String[] test = new String[] {"1.0", peer_ap, "test"};
		try {
			main(test);
		} catch (IOException e) {
			System.out.println("Failed to call main by execute in peer.");
		}

		PEER_ID = Integer.parseInt(peer_ap);
		Backup bckp;
		try {
			bckp = new Backup(filePath, replicationDegree);
			ArrayList<Chunk> chunkFiles = bckp.getChunkFiles();

			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

			Runnable task = () -> {
				if(!chunkFiles.isEmpty()){
					System.out.println("\nSending multicast: ");
					byte[] autoBuffer = null;
					Chunk c = chunkFiles.remove(0);
					String outMessage = "PUTCHUNK" + " " + "1.0" + " " + PEER_ID + " " + c.getFileId() + " " + c.getChunkNumber() + " " + c.getReplicationDegree() + " " + "0xD0xA" + " " + "0xD0xA" + " " + c.getContent();
					System.out.println(outMessage);
					autoBuffer = outMessage.getBytes();

					mdb.send(autoBuffer);		

					sendStored();
					
					sendDelete();
				}
			};

			int initialDelay = 0;
			int period = 1;
			executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

		} catch (NumberFormatException e) {
			System.out.println("replicationDegree isnt a number, backup constructor in Peer.execute()");
		} catch (FileNotFoundException e) {
			System.out.println("filePath didnt match a valid file, backup constructor in Peer.execute()");
		}
	}
	 public String sayHello() {
	        return "Hello, world!";
	    }
	/**
	 * Função principal do programa.
	 * @param args Argumentos passados na chamada do programa
	 * @throws IOException Caso não consiga criar o canal multicast ou não se consiga ligar ao canal multicast.
	 */
	public static void main(String[] args) throws IOException{
		if (args.length < 3 || args.length > 9) {
			System.out.println("Usage: <protocol_version> <server_id> <service_access_point>");
			System.out.println("Or: <protocol_version> <server_id> <service_access_point> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
			return;
		}		
		double version = Double.parseDouble(args[0]);
		PEER_ID = (int) (Math.random() * 9999);//= Integer.parseInt(args[1]);
		String srvc_accss_pnt = args[2];
		System.out.println("Peer: "+srvc_accss_pnt + " started.");
		//Iniciar ip e portas default
		String MC_IP = "224.0.0.2";
		int MC_PORT = 4002;
		String MDB_IP = "224.0.0.3";
		int MDB_PORT = 4003;
		String MDR_IP = "224.0.0.4";
		int MDR_PORT = 4004;

		if(args.length > 3){
			//se receber mais que 3 tem que alterar ip e portas
			MC_IP = args[3];
			MC_PORT = Integer.parseInt(args[4]);
			MDB_IP = args[5];
			MDB_PORT = Integer.parseInt(args[6]);
			MDR_IP = args[7];
			MDR_PORT = Integer.parseInt(args[8]);
		}
		//Registring RMI
		 try {
	            Peer obj = new Peer();
	            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(obj, 0);

	            // Bind the remote object's stub in the registry
	            Registry registry = LocateRegistry.getRegistry();
	            registry.bind("PeerInterface", stub);

	            System.err.println("RMI Sucessfully Registred");
	        } catch (Exception e) {
	            System.err.println("Server exception: " + e.toString());
	            e.printStackTrace();
	        }
		
		
		
		
		
		
		
		mc = new MulticastChannel(MC_IP, MC_PORT);
		mdb = new MulticastChannel(MDB_IP, MDB_PORT);
		mdr = new MulticastChannel(MDR_IP, MDR_PORT);

		joinChannels();
		initializeListeners();
		initializeHandlers();
	}
}
