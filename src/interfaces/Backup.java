package interfaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Backup {
	private String filepath; // used to get the file
	private int replicationLevel;
	private ArrayList<Chunk> chunkFiles = new ArrayList<Chunk>();
	private int READ_LENGTH = 6400;

	/**
	 * Retorna um array com os chunks que são necessários enviar.
	 * @return Array de chunks por enviar
	 */
	public ArrayList<Chunk> getChunkFiles() {
		return chunkFiles;
	}

	/**
	 * Construtor do Backup.
	 * @param filepath Path para o ficheiro que se quer enviar
	 * @param replicationLevel Nível de replicação. Quantos peers devem armazenar chunks deste ficheiro
	 * @throws FileNotFoundException Quando não econtra o ficheiro pretendido
	 */
	public Backup(String filepath, int replicationLevel) throws FileNotFoundException{
		this.filepath = filepath;
		this.replicationLevel = replicationLevel;

		splitFile();

		sendingData();
	}

	/**
	 * Construtor alternativo do Backup.
	 * @param filepath Path para o ficheiro que se quer enviar
	 * @param replicationLevel Nível de replicação. Quantos peers devem armazenar chunks deste ficheiro. String que representa um int
	 * @throws FileNotFoundException
	 */
	public Backup(String filepath, String replicationLevel) throws FileNotFoundException {
		this(filepath, Integer.parseInt(replicationLevel));
	}

	/**
	 * Divide o ficheiro que se quer fazer backup em chunks e armazena-os em chunkFiles.
	 * Enquanto o tamanho do ficheiro for maior que 0 tenta ler e criar um chunk.
	 * @throws FileNotFoundException 
	 */
	public void splitFile() throws FileNotFoundException{
		File bckFile = new File(this.filepath);
		System.out.println(bckFile.getAbsolutePath());
		FileInputStream readStream;
		int fileS = (int) bckFile.length();
		int chunkNo = 0;
		byte[] byteChunkPart;

		readStream = new FileInputStream(bckFile);
		while(fileS > 0){
			int toRead = READ_LENGTH;
			if(fileS < READ_LENGTH)
				toRead = fileS;
			byteChunkPart = new byte[toRead];

			try {
				fileS -= readStream.read(byteChunkPart, 0, toRead);
				chunkNo++;
				Chunk chunk = new Chunk(filepath, chunkNo, replicationLevel, byteChunkPart);
				this.chunkFiles.add(chunk);
			} catch (IOException e) {
				System.out.println("Failed to read from file.");
				e.printStackTrace();
			}
		}

		try {
			readStream.close();
		} catch (IOException e) {
			System.out.println("Failed to close the file.");
			e.printStackTrace();
		}

	}

	private void sendingData(){
		System.out.println("numero de chunks: " + chunkFiles.size());
	}
}
