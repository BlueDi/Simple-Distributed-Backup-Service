package handlers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.TreeSet;

public class McHandler implements Runnable {
	private Queue<String> msgQueue;
	private TreeSet<ChunkInfo> treeofchunks = new TreeSet<ChunkInfo>();
	private String messageType = "";

	public McHandler(Queue<String> msgQueue) {
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()){
			analyseMessages();
		}
	}

	/**
	 * Analisa todas as mensagens armazenadas vindas do MC.
	 */
	private void analyseMessages(){
		if (!msgQueue.isEmpty()) {
			String[] msg = msgQueue.poll().split("\\s",7);
			print(msg);

			if(checkMessageType(msg[0]) && "STORED".equals(this.messageType)){
				ChunkInfo chunkinfo = new ChunkInfo(msg[2], msg[3], Integer.parseInt(msg[4]));

				analyseHeader(msg);
				checkStoredChunk(chunkinfo);
			}
			else if(checkMessageType(msg[0]) && "DELETE".equals(this.messageType)){
				ChunkInfo chunkinfo = new ChunkInfo(msg[2], msg[3]);
				
				deleteFiles(chunkinfo);
			}
		}
	}

	/**
	 * Apaga todos os ficheiros começados por fileID
	 * @param fileID prefixo do nome do ficheiro a apagar
	 */
	public static void deleteFiles(ChunkInfo chunkinfo) {
		Path path = Paths.get("./chunks/");

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, chunkinfo.fileId + "*")) {
			for (final Path file : directoryStream) {
				Files.delete(file);
			}
		} catch (IOException e) {
			System.out.println("Failed to delete chunks on MCHandler.");
		}
	}

	/**
	 * Verifica se o tipo de mensagem recebida é válida.
	 * @param messageType Tipo de mensagem
	 * @return true se é um tipo aceitavel, false caso contrário
	 */
	private boolean checkMessageType(String messageType) {
		if("STORED".equals(messageType) ||
				"DELETE".equals(messageType))
			this.messageType = messageType;

		return !this.messageType.isEmpty();
	}

	/**
	 * Analisa o cabeçalho da mensagem.
	 * TODO: Encriptação do FileId
	 * @param msg Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg) {
		String version = msg[1];

		return "1.0".equals(version) && 
				"0xD0xA".equals(msg[5]) &&
				"0xD0xA".equals(msg[6]);
	}

	/**
	 * Verifica se já recebeu uma mensagem de STORED igual à recebida.
	 * TODO: Modificar isto
	 * @param ci Mensagem recebida
	 * @return true se já tinha recebido, false se é uma nova mensagem
	 */
	private boolean checkStoredChunk(ChunkInfo ci){
		return !treeofchunks.add(ci);
	}

	/**
	 * Imprime a mensagem no ecrã.
	 * @param msg Array de strings a ser imprimido
	 */
	protected void print(String[] msg) {
		System.out.println("\nReceived on MC: ");
		for(int i = 0; i < msg.length; i++)
			System.out.print(msg[i] + "; ");
		System.out.println();
	}
}
