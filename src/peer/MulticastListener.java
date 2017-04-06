package peer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class MulticastListener implements Runnable {
	private MulticastChannel multicastChannel;
	private Queue<byte[]> receivedMsgs;

	/**
	 * Construtor do listener para o canal multicast.
	 * 
	 * @param multicastChannel
	 *            Multicast Channel ao qual se quer atribuir um Listener
	 */
	public MulticastListener(MulticastChannel multicastChannel) {
		Queue<byte[]> receivedMsgs = new LinkedList<byte[]>();

		this.multicastChannel = multicastChannel;
		this.receivedMsgs = receivedMsgs;
	}

	/**
	 * Inicia o thread para tratar o evento.
	 */
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				byte[] data = (multicastChannel.receive());

				if (data.length != 0)
					receivedMsgs.add(data);
			} catch (IOException e) {
				System.out.println("There was an error when tried to receive from the multicast channel.");
				break;
			}
		}
	}

	/**
	 * Devolve todas as mensagens recebidas pelo socket.
	 * 
	 * @return Fila com todas as mensagens recebidas pela socket
	 */
	public Queue<byte[]> getQueue() {
		return this.receivedMsgs;
	}
}
