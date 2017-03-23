package handlers;

import java.util.Queue;
import java.util.TreeSet;

public class McHandler implements Runnable {
	private Queue<String> msgQueue;
	private TreeSet<ChunkInfo> treeofchunks = new TreeSet<ChunkInfo>();

	public McHandler(Queue<String> msgQueue) {
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()){
			analyseMessages();
		}
	}

	class ChunkInfo implements Comparable<ChunkInfo>{
		public String fileId;
		public String senderId;
		public int chunkNo;

		ChunkInfo(String senderId, String fileId, int chunkNo){
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
		}

		@Override
		public int compareTo(ChunkInfo ci) {
			return fileId.equals(ci.fileId) || senderId.equals(ci.senderId) || (chunkNo == ci.chunkNo) ? 0 : -1;
		}
	}

	/**
	 * Analisa todas as mensagens armazenadas.
	 */
	private void analyseMessages(){
		if (!msgQueue.isEmpty()) {
			String[] msg = msgQueue.poll().split("\\s",7);
			print(msg);
			
			if(checkValidMessageType(msg[0])){
				ChunkInfo chunkinfo = new ChunkInfo(msg[2], msg[3], Integer.parseInt(msg[4]));
				
				analyseHeader(msg);
				checkStoredChunk(chunkinfo);
			}
		}
	}

	private boolean checkValidMessageType(String messageType) {
		return "STORED".equals(messageType);
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
