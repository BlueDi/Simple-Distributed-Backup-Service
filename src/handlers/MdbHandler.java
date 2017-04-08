package handlers;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;

import interfaces.Chunk;
import peer.Peer;

public class MdbHandler implements Runnable {
	private int PEER_ID;
	private Queue<byte[]> msgQueue;
	private Queue<Chunk> chunksReceived = new LinkedList<Chunk>();

	public MdbHandler(Queue<byte[]> msgQueue, int id) {
		this.msgQueue = msgQueue;
		PEER_ID = id;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			analyseMessages();
		}
	}

	/**
	 * @return the chunksReceived
	 */
	public Queue<Chunk> getChunksReceived() {
		return chunksReceived;
	}

	/**
	 * Analisa todas as mensagens armazenadas.
	 */
	private void analyseMessages() {
		if (!msgQueue.isEmpty()) {
			byte[] data = msgQueue.poll();
			String convert = new String(data, 0, data.length);
			String[] msg = convert.substring(0, convert.indexOf("\r\n")).split("\\s");

			if (checkValidMessageType(msg[0]) && analyseHeader(msg)) {
				print(msg);

				byte[] body = analyseBody(data, convert);
				Chunk chunk = new Chunk(msg[3], Integer.parseInt(msg[4]), Integer.parseInt(msg[5]), body);

				storeChunk(chunk);
				chunksReceived.add(chunk);

				Peer.sendStored();
			}
		}
	}

	/**
	 * Verifica se a mensagem recebida é válida. A primeira verificação é se o
	 * MessageType está correto.
	 * 
	 * @param messageType
	 *            MessageType recebido
	 * @return True se tem o MessageType correto
	 */
	private boolean checkValidMessageType(String messageType) {
		return "PUTCHUNK".equals(messageType);
	}

	/**
	 * Analisa o cabeçalho da mensagem. TODO: Encriptação do FileId
	 * 
	 * @param msg
	 *            Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg) {
		String version = msg[1];
		int senderId = Integer.parseInt(msg[2]);
		int replicationDeg = Integer.parseInt(msg[5]);

		return "1.0".equals(version) && PEER_ID != senderId && (replicationDeg <= 9 || replicationDeg >= 0);
	}

	/**
	 * Analisa o body da mensagem.
	 * 
	 * @param data
	 *            Informação original recebida
	 * @param msg
	 *            Mensagem recebida
	 * @return byte[] com o body da mensagem
	 */
	private byte[] analyseBody(byte[] data, String msg) {
		int bodyIndex = msg.indexOf("\r\n") + 4;
		byte[] destination = new byte[msg.length() - bodyIndex];

		if (bodyIndex != -1) {
			System.arraycopy(data, bodyIndex, destination, 0, data.length - bodyIndex);
			System.out.println("Received a body of size " + destination.length + " bytes. " + bodyIndex);
		}
		return destination;
	}

	/**
	 * Cria o ficheiro que corresponde a um chunk. O ficheiro é criado na pasta
	 * chunks que está no mesmo nível que o src.
	 * 
	 * @param chunk
	 *            Chunk a guardar
	 * @throws IOException
	 *             Já existe um chunk com este fileId
	 */
	private void storeChunk(Chunk chunk) {
		String chunkNo = String.format("%03d", chunk.getChunkNumber());
		Path path = Paths.get(("./chunks/" + chunk.getFileId() + "." + chunkNo));
		byte[] data = new byte[0];

		try {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
			data = chunk.getContent();
			Files.write(path, data, StandardOpenOption.CREATE);
		} catch (FileAlreadyExistsException e) {
			System.err.println("Chunk already exists: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("I/O error in mdbHandler storeChunk.");
		}

	}

	/**
	 * Imprime a mensagem no ecrã.
	 * 
	 * @param msg
	 *            Array de strings a ser imprimido
	 */
	protected void print(String[] msg) {
		System.out.println("\nReceived on MDB: ");
		for (int i = 0; i < msg.length - 3; i++)
			System.out.print(msg[i] + "; ");
		System.out.print(" <CRLF><CRLF><body>\n");
	}
}
