package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handlers.MdbHandler;

public class Peer {
	private static MulticastChannel mc = null;
	private static MulticastChannel mdb = null;
	private static MulticastChannel mdr = null;
	private static MulticastListener mcListener = null;
	private static MulticastListener mdbListener = null;
	private static MulticastListener mdrListener = null;
	private static MdbHandler mdbHandler = null;

	/**
	 * Função principal do programa.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
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
		mc.join();
		mdb = new MulticastChannel(MDB_IP, MDB_PORT);
		mdb.join();
		mdr = new MulticastChannel(MDR_IP, MDR_PORT);
		mdr.join();

		mcListener = new MulticastListener(mc);
		mdbListener = new MulticastListener(mdb);
		mdrListener = new MulticastListener(mdr);

		mcListener.start();
		mdbListener.start();
		mdrListener.start();

		//mcHandler = new McHandler(mdrListener.getQueue());
		mdbHandler = new MdbHandler(mdbListener.getQueue());
		//mdrHandler = new MdrHandler(mdrListener.getQueue());

		mdbHandler.start();

		//executor for sending hello every 1sec
		InetAddress hostAddr = InetAddress.getByName(MDB_IP);
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
				// TODO Auto-generated catch block
				System.out.println("Failed to multicast");
				e.printStackTrace();
			}
		};

		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

	}
}
