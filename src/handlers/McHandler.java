package handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import interfaces.Chunk;

public class McHandler implements Runnable {
	private int PEER_ID;
	private Queue<byte[]> msgQueue = new LinkedList<byte[]>();
	private String messageType = "";
	private TreeSet<ChunkInfo> treeofchunks = new TreeSet<ChunkInfo>();
	private Queue<Chunk> chunksToSend = new LinkedList<Chunk>();

	public McHandler(Queue<byte[]> msgQueue, int id) {
		this.msgQueue = msgQueue;
		PEER_ID = id;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			analyseMessages();
		}
	}

	public Queue<Chunk> getChunksToSend() {
		return chunksToSend;
	}

	/**
	 * Analisa todas as mensagens armazenadas vindas do MC.
	 */
	private void analyseMessages() {
		if (!msgQueue.isEmpty()) {
			byte[] data = msgQueue.poll();
			String convert = new String(data, 0, data.length);
			String[] msg = convert.substring(0, convert.indexOf("\r\n")).split("\\s");

			print(msg);

			if (checkMessageType(msg[0]) && "STORED".equals(this.messageType)) {
				ChunkInfo chunkinfo = new ChunkInfo(msg[2], msg[3], Integer.parseInt(msg[4]));

				analyseHeader(msg);
				checkStoredChunk(chunkinfo);
			} else if (checkMessageType(msg[0]) && "DELETE".equals(this.messageType)) {
				deleteFiles(msg[3]);
			} else if (checkMessageType(msg[0]) && "GETCHUNK".equals(this.messageType)) {
				searchChunk(msg[3], msg[4]);
			}
		}
	}

	/**
	 * Verifica se o tipo de mensagem recebida é válida.
	 * 
	 * @param messageType
	 *            Tipo de mensagem
	 * @return true se é um tipo aceitavel, false caso contrário
	 */
	private boolean checkMessageType(String messageType) {
		if ("STORED".equals(messageType) || "DELETE".equals(messageType) || "GETCHUNK".equals(messageType))
			this.messageType = messageType;

		return !this.messageType.isEmpty();
	}

	/**
	 * Analisa o cabeçalho da mensagem. TODO: Encriptação do FileId
	 * 
	 * @param msg
	 *            Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg) {
		return "1.0".equals(msg[1]);
	}

	/**
	 * Verifica se já recebeu uma mensagem de STORED igual à recebida. TODO:
	 * Modificar isto
	 * 
	 * @param ci
	 *            Mensagem recebida
	 * @return true se já tinha recebido, false se é uma nova mensagem
	 */
	private boolean checkStoredChunk(ChunkInfo ci) {
		return !treeofchunks.add(ci);
	}

	/**
	 * Apaga todos os ficheiros começados por fileID
	 * 
	 * @param fileID
	 *            prefixo do nome do ficheiro a apagar
	 */
	private void deleteFiles(String fileId) {
		Path path = Paths.get("./chunks/");

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, fileId + "*")) {
			for (final Path file : directoryStream) {
				Files.delete(file);
			}
		} catch (IOException e) {
			System.out.println("Failed to delete chunks on MCHandler.");
		}
	}

	private boolean fileExists(String path) {
		File f = new File(path);

		return f.exists() && !f.isDirectory();
	}

	private void searchChunk(String fileId, String chunkNo) {
		int i = Integer.parseInt(chunkNo);
		String chunkNoStr = String.format("%03d", i);

		if (fileExists("chunks/" + fileId + chunkNoStr)) {
			Path path = Paths.get("chunks/" + fileId + chunkNoStr);

			try {
				Chunk c = new Chunk(fileId, Integer.parseInt(chunkNo), Files.readAllBytes(path));
				chunksToSend.add(c);
			} catch (IOException e) {
				System.out.println("Failed to read in mcHandler.searchChunk().");
			}
		}
	}

	/**
	 * Imprime a mensagem no ecrã.
	 * 
	 * @param msg
	 *            Array de strings a ser imprimido
	 */
	protected void print(String[] msg) {
		System.out.println("\nReceived on MC: ");
		for (int i = 0; i < msg.length; i++)
			System.out.print(msg[i] + "; ");
		System.out.println();
	}
}
