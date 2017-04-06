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
			String[] msg = convert.split("\\s", 9);
			boolean isValid = checkValidMessageType(msg[0]);

			if (isValid) {
				print(msg);

				analyseHeader(msg);

				byte[] body = analyseBody(data, convert);
				Chunk chunk = new Chunk(msg[3], Integer.parseInt(msg[4]), Integer.parseInt(msg[5]), body);

				storeChunk(chunk);
				chunksReceived.add(chunk);

				// sendConfirmationOfStoredChunk();
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
		int replicationDeg = Integer.parseInt(msg[5]);

		return "1.0".equals(version) && (replicationDeg <= 9 || replicationDeg >= 0) && "0xD0xA".equals(msg[6])
				&& "0xD0xA".equals(msg[7]);
	}

	/**
	 * Analisa o body da mensagem. TODO: Não sei se é útil.
	 * 
	 * @param msg
	 * @return
	 */
	private byte[] analyseBody(byte[] data, String msg) {
		int bodyIndex = msg.indexOf("0xD0xA 0xD0xA") + 14;
		byte[] destination = new byte[msg.length() - bodyIndex];

		if (bodyIndex != -1) {
			System.arraycopy(data, bodyIndex, destination, 0, data.length - bodyIndex);

			byte[] destination2 = new byte[bodyIndex];
			System.arraycopy(data, 0, destination2, 0, bodyIndex);
			System.out.println(new String(destination2));

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
		for (int i = 0; i < msg.length - 1; i++)
			System.out.print(msg[i] + "; ");
		System.out.print("<body>\n");
	}
}
