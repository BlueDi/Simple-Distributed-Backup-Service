package handlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import interfaces.Chunk;

public class MdrHandler extends Handler implements Runnable {
	private Stack<Chunk> chunksRequests = new Stack<Chunk>();

	public MdrHandler(Queue<byte[]> msgQueue, int id) {
		super(msgQueue, id);
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			analyseMessages();
		}
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

		if (bodyIndex != -1)
			System.arraycopy(data, bodyIndex, destination, 0, data.length - bodyIndex);

		return destination;
	}

	/**
	 * Analisa o cabeçalho da mensagem.
	 * 
	 * @param msg
	 *            Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg) {
		return "1.0".equals(msg[1]) && PEER_ID != Integer.parseInt(msg[2]);
	}

	/**
	 * Analisa todas as mensagens armazenadas e cria o ficheiro se recebeu o
	 * último chunk do ficheiro.
	 */
	private void analyseMessages() {
		if (!msgQueue.isEmpty()) {
			byte[] data = msgQueue.poll();
			String convert = new String(data, 0, data.length);
			String[] msg = convert.substring(0, convert.indexOf("\r\n")).split("\\s");

			if (checkValidMessageType(msg[0]) && analyseHeader(msg)) {
				print(msg);

				byte[] body = analyseBody(data, convert);
				Chunk chunk = new Chunk(msg[3], Integer.parseInt(msg[4]), body);

				chunksRequests.push(chunk);

				if (isEndOfFile())
					createFile(chunk.getFileId());
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
		return "CHUNK".equals(messageType);
	}

	/**
	 * Cria o ficheiro fileId na pasta files, com os chunks armazenados em
	 * chunks.
	 * 
	 * @param fileId
	 *            Caminho do ficheiro a criar
	 */
	private void createFile(String fileId) {
		String encrypted = encrypt(fileId);
		mergeFiles("chunks/" + encrypted, "files/" + fileId);
	}

	/**
	 * @return the chunksReceived
	 */
	public Stack<Chunk> getRequests() {
		return chunksRequests;
	}

	/**
	 * Verifica se o chunk recebido mais recente é o último do ficheiro.
	 * 
	 * @return true se o chunk é o último do seu ficheiro, false se não for
	 */
	public boolean isEndOfFile() {
		if (chunksRequests.isEmpty())
			return false;
		return chunksRequests.peek().getContent().length < 64000;
	}

	/**
	 * Procura todos os chunks do ficheiro fileName.
	 * 
	 * @param fileName
	 *            ficheiro a procurar
	 * @return Lista dos chunks de fileName
	 */
	private List<File> listOfFilesToMerge(String fileName) {
		File f = new File(fileName);
		Path dir = Paths.get(f.getParent());
		List<File> files = new LinkedList<File>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, f.getName() + ".*")) {
			for (Path p : stream)
				files.add(p.toFile());
		} catch (Exception e) {
			System.out.println("Failed to locate the directory chunks");
		}

		return files;
	}

	/**
	 * Junta todos os chunks da lista files num novo ficheiro into.
	 * 
	 * @param files
	 *            lista de ficheiros a juntar
	 * @param into
	 *            novo ficheiro
	 * @throws IOException
	 */
	private void mergeFiles(List<File> files, File into) throws IOException {
		Files.createDirectories(into.toPath().getParent());
		Files.createFile(into.toPath());
		try (BufferedOutputStream mergingStream = new BufferedOutputStream(new FileOutputStream(into))) {
			for (File f : files) {
				Files.copy(f.toPath(), mergingStream);
			}
		} catch (FileNotFoundException e) {
			System.err.println("File was not found when trying to merge.");
		}
	}

	/**
	 * Cria o ficheiro destFile com o conteúdo de sourceFile.
	 * 
	 * @param sourceFile
	 * @param destFile
	 */
	private void mergeFiles(String sourceFile, String destFile) {
		try {
			mergeFiles(listOfFilesToMerge(sourceFile), new File(destFile));
		} catch (IOException e) {
			System.err.println("I/O exception in MdrHandler.mergeFiles.");
		}
	}

	/**
	 * Imprime a mensagem no ecrã.
	 * 
	 * @param msg
	 *            Array de strings a ser imprimido
	 */
	protected void print(String[] msg) {
		System.out.println("\nReceived on MDR: ");
		for (int i = 0; i < msg.length; i++)
			System.out.print(msg[i] + "; ");
		System.out.print("<CRLF><CRLF><body>\n");
	}
}
