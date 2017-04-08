package handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import interfaces.Chunk;
import peer.Peer;

public class McHandler implements Runnable {
	private int PEER_ID;
	private Queue<byte[]> msgQueue = new LinkedList<byte[]>();
	private String messageType = "";
	private Queue<Chunk> chunksToSend = new LinkedList<Chunk>();
	private Map<ChunkInfo, ArrayList<Integer>> storedMap = new HashMap<ChunkInfo, ArrayList<Integer>>();

	public McHandler(Queue<byte[]> msgQueue, int id) {
		this.msgQueue = msgQueue;
		PEER_ID = id;
	}

	/**
	 * Adiciona a mensagem de STORED à tree de mensagens Stored já recebidas.
	 * TODO: Modificar isto
	 * 
	 * @param ci
	 *            Mensagem recebida
	 * @param senderId
	 *            Peer que enviou a mensagem Stored
	 */
	private void addStoredChunk(ChunkInfo ci, int senderId) {
		ArrayList<Integer> peersComChunk = new ArrayList<Integer>();

		if (storedMap.containsKey(ci)) {
			peersComChunk = storedMap.get(ci);
			if (!peersComChunk.contains(senderId)) {
				peersComChunk.add(senderId);
				storedMap.put(ci, peersComChunk);
			}
		} else {
			peersComChunk.add(senderId);
			storedMap.put(ci, peersComChunk);
		}
	}

	/**
	 * Analisa o cabeçalho da mensagem. TODO: Encriptação do FileId
	 * 
	 * @param msg
	 *            Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg) {
		return "1.0".equals(msg[1]) && PEER_ID != Integer.parseInt(msg[2]);
	}

	/**
	 * Analisa todas as mensagens armazenadas vindas do MC.
	 */
	private void analyseMessages() {
		if (!msgQueue.isEmpty()) {
			byte[] data = msgQueue.poll();
			String convert = new String(data, 0, data.length);
			String[] msg = convert.substring(0, convert.indexOf("\r\n")).split("\\s");

			if (checkMessageType(msg[0]) && analyseHeader(msg)) {
				print(msg);
				if ("STORED".equals(this.messageType)) {
					ChunkInfo chunkinfo = new ChunkInfo(msg[3], Integer.parseInt(msg[4]));
					addStoredChunk(chunkinfo, Integer.parseInt(msg[2]));
				} else if ("DELETE".equals(this.messageType)) {
					deleteFiles(msg[3]);
				} else if ("GETCHUNK".equals(this.messageType)) {
					searchChunk(msg[3], msg[4]);
				} else if ("REMOVED".equals(this.messageType)) {
					doIHave(msg[3], msg[4]);
				}
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
		if ("STORED".equals(messageType) || "DELETE".equals(messageType) || "GETCHUNK".equals(messageType)
				|| "REMOVED".equals(messageType))
			this.messageType = messageType;

		return !this.messageType.isEmpty();
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
			System.err.println("Failed to delete chunks on MCHandler.");
		}
	}

	private void doIHave(String fileId, String chunkNo) {
//		int i = Integer.parseInt(chunkNo);
//		String chunkNoStr = String.format("%03d", i);
//
//		if (fileExists("chunks/" + fileId + chunkNoStr)) {
//			Path path = Paths.get("chunks/" + fileId + chunkNoStr);
//
//			try {
//				Chunk c = new Chunk(fileId, Integer.parseInt(chunkNo), Files.readAllBytes(path));
//				chunksToSend.add(c);
//				Peer.sendChunks();
//			} catch (IOException e) {
//				System.err.println("Failed to read in mcHandler.doIHave().");
//			}
//		}
	}

	private boolean fileExists(String path) {
		File f = new File(path);

		return f.exists() && !f.isDirectory();
	}

	public Queue<Chunk> getChunksToSend() {
		return chunksToSend;
	}

	public boolean receivedAllStored(Chunk c) {
		ChunkInfo ci = new ChunkInfo(c.getFileId(), c.getChunkNumber());
		List<Integer> li = storedMap.get(ci);

		if (li == null)
			return false;
		return c.getReplicationDegree() <= li.size();
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			analyseMessages();
		}
	}

	/**
	 * Procura o chunk do ficheiro fileId com o número chunkNo na pasta dos
	 * chunks. Se encontrar adiciona ao chunksToSend para depois ser enviado.
	 * 
	 * @param fileId
	 *            Ficheiro procurado
	 * @param chunkNo
	 *            Número do chunk procurado
	 */
	private void searchChunk(String fileId, String chunkNo) {
		int i = Integer.parseInt(chunkNo);
		String chunkNoStr = String.format("%03d", i);

		if (fileExists("chunks/" + fileId + chunkNoStr)) {
			Path path = Paths.get("chunks/" + fileId + chunkNoStr);

			try {
				Chunk c = new Chunk(fileId, Integer.parseInt(chunkNo), Files.readAllBytes(path));
				chunksToSend.add(c);
				Peer.sendChunks();
			} catch (IOException e) {
				System.err.println("Failed to read in mcHandler.searchChunk().");
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
