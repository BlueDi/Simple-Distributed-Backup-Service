package peer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handlers.MdbHandler;
import interfaces.Backup;

public class Peer {
	private static MulticastChannel mc = null;
	private static MulticastChannel mdb = null;
	private static MulticastChannel mdr = null;

	private static MulticastListener mcListener = null;
	private static MulticastListener mdbListener = null;
	private static MulticastListener mdrListener = null;

	//private static McHandler mcHandler = null;
	private static MdbHandler mdbHandler = null;
	//private static MdrHandler mdrHandler = null;

	private static Thread mcListener_Thread = null;
	private static Thread mdbListener_Thread = null;
	private static Thread mdrListener_Thread = null;
	//private static Thread mcHandler_Thread = null;
	private static Thread mdbHandler_Thread = null;
	//private static Thread mdrHandler_Thread = null;

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

		//executor for sending hello every 1sec
		String outMessage = "PUTCHUNK " + "1.0" + " " + "128.128.128.128" + " " + "chunkteste" + " " + "5" + " " + "3" + " " + "0xD0xA" + " " + "0xD0xA" + " " + "Dados que o chunk vai ter";
		//estrutura da mensagem do backup: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF> <Body>
		System.out.println(outMessage);
		byte[] autoBuffer = outMessage.getBytes();

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				mdb.send(autoBuffer);
				String message_sent = new String(autoBuffer);
				System.out.println("Sending multicast: " + message_sent);
			} catch (IOException e) {
				System.out.println("Failed to multicast");
				e.printStackTrace();
			}
		};

		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
		

	}
}
