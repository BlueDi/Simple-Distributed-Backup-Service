package peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handlers.MdbHandler;
import interfaces.Backup;
import interfaces.Chunk;

public class Peer {
	private static MulticastChannel mc;
	private static MulticastChannel mdb;
	private static MulticastChannel mdr;

	private static MulticastListener mcListener;
	private static MulticastListener mdbListener;
	private static MulticastListener mdrListener;

	//private static McHandler mcHandler;
	private static MdbHandler mdbHandler;
	//private static MdrHandler mdrHandler;

	private static Thread mcListener_Thread;
	private static Thread mdbListener_Thread;
	private static Thread mdrListener_Thread;
	//private static Thread mcHandler_Thread;
	private static Thread mdbHandler_Thread;
	//private static Thread mdrHandler_Thread;

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
		//mcHandler = new McHandler(mcListener.getQueue());
		mdbHandler = new MdbHandler(mdbListener.getQueue());
		//mdrHandler = new MdrHandler(mdrListener.getQueue());

		//mcHandler_Thread = new Thread(mcHandler);
		mdbHandler_Thread = new Thread(mdbHandler);
		//mdrHandler_Thread = new Thread(mdrHandler);

		//mcHandler_Thread.start();
		mdbHandler_Thread.start();
		//mdrHandler_Thread.start();
	}

	/**
	 * Função principal do programa.
	 * @param args Argumentos passados na chamada do programa
	 * @throws IOException Caso não consiga criar o canal multicast ou não se consiga ligar ao canal multicast.
	 */
	public static void main(String[] args) throws IOException{
		if (args.length < 6) {
			System.out.println("Usage: java <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
			return;
		}

		String MC_IP = args[0];
		int MC_PORT = Integer.parseInt(args[1]);
		String MDB_IP = args[2];
		int MDB_PORT = Integer.parseInt(args[3]);
		String MDR_IP = args[4];
		int MDR_PORT = Integer.parseInt(args[5]);

		mc = new MulticastChannel(MC_IP, MC_PORT);
		mdb = new MulticastChannel(MDB_IP, MDB_PORT);
		mdr = new MulticastChannel(MDR_IP, MDR_PORT);

		joinChannels();
		initializeListeners();
		initializeHandlers();

		Backup bckp = new Backup("lorem_ipsum.txt", 2);
		ArrayList<Chunk> chunkFiles = bckp.getChunkFiles();

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				if(!chunkFiles.isEmpty()){
					System.out.println("\nSending multicast: ");
					byte[] autoBuffer = null;
					Chunk c = chunkFiles.remove(0);
					String outMessage = "PUTCHUNK" + " " + "1.0" + " " + "128.128.128.128" + " " + c.getFileId() + " " + c.getChunkNumber() + " " + c.getReplicationDegree() + " " + "0xD0xA" + " " + "0xD0xA" + " " + c.getContent();
					//estrutura da mensagem do backup: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
					System.out.println(outMessage);
					autoBuffer = outMessage.getBytes();
					mdb.send(autoBuffer);
				}
				Thread.sleep(200);
			} 
			catch (IOException e) {
				System.out.println("Failed to multicast");
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
	}
}
