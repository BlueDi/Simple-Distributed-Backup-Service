package peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MulticastChannel {
	private InetAddress group;
	private int PORT;
	// private int NUMBER_OF_CONFIRMATIONS;
	private MulticastSocket socket = null;

	/**
	 * Cria o canal para multicast.
	 * 
	 * @param ip
	 * @param port
	 * @throws UnknownHostException
	 */
	public MulticastChannel(String ip, int port) throws UnknownHostException {
		this.group = InetAddress.getByName(ip);
		this.PORT = port;
		// this.NUMBER_OF_CONFIRMATIONS = 0;
	}

	/**
	 * Fecha o socket.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}

	/**
	 * Liga o socket ao grupo.
	 * 
	 * @throws IOException
	 */
	public void join() throws IOException {
		socket = new MulticastSocket(PORT);
		// socket.setTimeToLive(1);
		socket.joinGroup(group);
		// socket.setLoopbackMode(true);
	}

	/**
	 * Recebe da rede multicast.
	 * 
	 * @param buf
	 *            buffer a ser preenchido
	 * @return byte[] com data recebida
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {
		byte[] buf = new byte[65536];
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
		socket.receive(packet);
		return Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
	}

	/**
	 * Envia um DatagramPacket com a informação de toSend para a rede multicast.
	 * 
	 * @param toSend
	 *            Dados a ser enviados sob a forma de byte[]
	 * @throws IOException
	 */
	public void send(byte[] toSend) {
		DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Falhou no envio do packet.");
			e.printStackTrace();
		}
	}
}
