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
	private int NUMBER_OF_CONFIRMATIONS;
	private MulticastSocket socket = null;

	/**
	 * Cria o canal para multicast.
	 * @param ip
	 * @param port
	 * @throws UnknownHostException
	 */
	public MulticastChannel(String ip, int port) throws UnknownHostException {
		this.group = InetAddress.getByName(ip);
		this.PORT = port;
		this.NUMBER_OF_CONFIRMATIONS = 0;
	}

	/**
	 * Liga o socket ao grupo.
	 * @throws IOException
	 */
	public void join() throws IOException {
		socket = new MulticastSocket(PORT);
		//socket.setTimeToLive(1);
		socket.joinGroup(group);
		//socket.setLoopbackMode(true);
	}

	/**
	 * Envia um DatagramPacket com a informação de toSend para a rede multicast.
	 * @param toSend
	 * @throws IOException
	 */
	public void send(byte[] toSend) throws IOException {
		DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group, PORT);
		socket.send(packet);
	}

	/**
	 * Recebe da rede multicast.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public byte[] receive(byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
		socket.receive(packet);
		byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		return data;
	}

	/**
	 * Fecha o socket.
	 * @throws IOException
	 */
	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}
}
