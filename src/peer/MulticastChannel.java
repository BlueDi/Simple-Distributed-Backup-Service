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

	public MulticastChannel(String ip, int port) throws UnknownHostException {
		this.group = InetAddress.getLocalHost(); //InetAddress.getByName(ip);
		this.PORT = port;
		this.NUMBER_OF_CONFIRMATIONS = 0;
	}

	public void join() throws IOException {
		socket = new MulticastSocket(PORT);
		socket.setTimeToLive(1);
		socket.joinGroup(group);
		socket.setLoopbackMode(true);
	}

	public void send(byte[] toSend) throws IOException {
		DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group, PORT);
		socket.send(packet);
	}

	public byte[] receive(byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
		socket.receive(packet);
		byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		System.out.println(data);
		return data;
	}

	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}
}
