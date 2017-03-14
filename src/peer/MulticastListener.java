package peer;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class MulticastListener implements Runnable {
	Thread t;
	private MulticastChannel multicastChannel;
	private Queue<String> receivedMsgs;

	/**
	 * Construtor do listener para o canal multicast.
	 * @param multicastChannel
	 */
	public MulticastListener(MulticastChannel multicastChannel) {
		Queue<String> receivedMsgs = new LinkedList<String>();

		this.multicastChannel = multicastChannel;
		this.receivedMsgs = receivedMsgs;

		t = new Thread(this);
		t.start();
	}

	/**
	 * Inicia o thread para tratar o evento.
	 */
	public void run(){
		while (!t.isInterrupted()) {
			try {
				byte[] buf = new byte[65536];
				byte[] data = multicastChannel.receive(buf);

				if (data != null)
					receivedMsgs.add(new String(data));
			}
			catch (SocketException e) {
				break;
			}
			catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * Devolve todas as mensagens recebidas pelo socket.
	 * @return Fila com todas as mensagens recebidas pela socket
	 */
	public Queue<String> getQueue() {
		return this.receivedMsgs;
	}
}
