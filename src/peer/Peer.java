package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {
	private static MulticastChannel mc = null;
	private static MulticastChannel mdb = null;
	private static MulticastChannel mdr = null;
	private static MulticastListener mcListener = null;
	private static MulticastListener mdbListener = null;
	private static MulticastListener mdrListener = null;

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

		//executor for sending hello every 1sec
		InetAddress hostAddr = InetAddress.getLocalHost();
		String outMessage = hostAddr.getHostAddress().toString() + " " + MC_PORT;
		System.out.println(outMessage);
		byte[] autoBuffer = outMessage.getBytes();
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				mc.send(autoBuffer);
				System.out.println("Sending multicast");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to multicast");
				e.printStackTrace();
			}
		};

		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

		//escutar se algum cliente está a mandar dados
		System.out.println("Server started");
		while (true) {
			System.out.println(mc.receive(autoBuffer));
		}
	}
}
