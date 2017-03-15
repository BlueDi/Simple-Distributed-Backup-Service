package peer;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class MulticastListener extends Thread {
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
	}

	/**
	 * Inicia o thread para tratar o evento.
	 */
	public void run(){
		while (!isInterrupted()) {
			try {
				byte[] buf = new byte[65536];
				String data = new String(multicastChannel.receive(buf));

				if (data != null)
					receivedMsgs.add(data);
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
