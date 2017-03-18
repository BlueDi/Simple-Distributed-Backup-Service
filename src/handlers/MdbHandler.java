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

public class MdbHandler extends Thread {
	private Queue<String> msgQueue;
	private Queue<Chunk> chunksReceived = new LinkedList<Chunk>();

	public MdbHandler(Queue<String> msgQueue) {
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		while (!isInterrupted())
			analyseMessages();
	}

	/**
	 * Analisa todas as mensagens armazenadas.
	 */
	private void analyseMessages(){
		if (!msgQueue.isEmpty()) {
			String[] msg = msgQueue.poll().split("\\s",9);
			boolean isValid = checkValidMessageType(msg[0]);

			if(isValid){
				print(msg);

				analyseHeader(msg);
				byte[] body = analyseBody(msg[8]);

				Chunk chunk = new Chunk(msg[3], Integer.parseInt(msg[4]), Integer.parseInt(msg[5]), body);

				try {
					storeChunk(chunk);
					chunksReceived.add(chunk);
				} catch (IOException e) {
					System.out.println("Failed to store chunk.");
					e.printStackTrace();
				}

				//sendConfirmationOfStoredChunk();
			}
		}
	}

	/**
	 * Verifica se a mensagem recebida é válida.
	 * A primeira verificação é se o MessageType está correto.
	 * @param messageType MessageType recebido
	 * @return True se tem o MessageType correto
	 */
	private boolean checkValidMessageType(String messageType){
		//check valid backup message
		if (!messageType.equals("PUTCHUNK")) 
			return false;
		return true;
	}

	/**
	 * Analisa o cabeçalho da mensagem.
	 * TODO: Encriptação do FileId
	 * @param msg Mensagem recebida
	 * @return true se o cabeçalho é válido
	 */
	private boolean analyseHeader(String[] msg){	
		String version = msg[1];
		int replicationDeg = Integer.parseInt(msg[5]);

		if(version != "1.0" && 
				(replicationDeg > 8 || replicationDeg < 0) &&
				msg[6].equals("0xD0xA") &&
				msg[7].equals("0xD0xA")
				)
			return false;

		return true;
	}

	/**
	 * Analisa o body da mensagem.
	 * TODO: Não sei se é útil.
	 * @param msg
	 * @return
	 */
	private byte[] analyseBody(String msg){
		byte[] destination = null;
		destination = msg.getBytes();
		return destination;
	}

	/**
	 * Cria o ficheiro que corresponde a um chunk.
	 * O ficheiro é criado na pasta chunks que está no mesmo nível que o src.
	 * @param chunk Chunk a guardar
	 * @throws IOException Já existe um chunk com este fileId
	 */
	private void storeChunk(Chunk chunk) throws IOException{
		byte data[] = chunk.getContent();
		Path path = Paths.get(("../chunks/" + chunk.getFileId()));
		Files.createDirectories(path.getParent());        
		try {
			Files.createFile(path);
			Files.write(path, data, StandardOpenOption.APPEND);
		} catch (FileAlreadyExistsException e) {
			System.err.println("Chunk already exists: " + e.getMessage());
		}
	}

	/**
	 * Imprime a mensagem no ecrã.
	 * @param msg Array de strings a ser imprimido
	 */
	private void print(String[] msg){
		System.out.println("Received: ");
		for(int i = 0; i < msg.length; i++)
			System.out.print(msg[i] + "; ");
		System.out.println();
	}
}
