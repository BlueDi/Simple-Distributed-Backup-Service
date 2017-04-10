package handlers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;

public class Handler {
	protected int PEER_ID;
	protected Queue<byte[]> msgQueue = new LinkedList<byte[]>();

	public Handler(Queue<byte[]> msgQueue, int id) {
		this.msgQueue = msgQueue;
		PEER_ID = id;
	}

	/**
	 * @return the pEER_ID
	 */
	public int getPEER_ID() {
		return PEER_ID;
	}

	/**
	 * @param pEER_ID
	 *            the pEER_ID to set
	 */
	public void setPEER_ID(int pEER_ID) {
		PEER_ID = pEER_ID;
	}

	/**
	 * @return the msgQueue
	 */
	public Queue<byte[]> getMsgQueue() {
		return msgQueue;
	}

	/**
	 * @param msgQueue
	 *            the msgQueue to set
	 */
	public void setMsgQueue(Queue<byte[]> msgQueue) {
		this.msgQueue = msgQueue;
	}

	protected String encrypt(String msg) {
		String encrypt = msg;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
			encrypt = Base64.getEncoder().encodeToString(hash);
			encrypt = encrypt.replace("/", "blue");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encrypt;
	}
}
