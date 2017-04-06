package interfaces;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Backup {
	private String filepath; // used to get the file
	private int replicationLevel;
	private ArrayList<Chunk> chunkFiles = new ArrayList<Chunk>();
	private int READ_LENGTH = 64000;

	/**
	 * Retorna um array com os chunks que são necessários enviar.
	 * 
	 * @return Array de chunks por enviar
	 */
	public ArrayList<Chunk> getChunkFiles() {
		return chunkFiles;
	}

	/**
	 * Construtor do Backup.
	 * 
	 * @param filepath
	 *            Path para o ficheiro que se quer enviar
	 * @param replicationLevel
	 *            Nível de replicação. Quantos peers devem armazenar chunks
	 *            deste ficheiro
	 * @throws FileNotFoundException
	 *             Quando não econtra o ficheiro pretendido
	 */
	public Backup(String filepath, int replicationLevel) throws FileNotFoundException {
		this.filepath = filepath;
		this.replicationLevel = replicationLevel;

		splitFile();

		sendingData();
	}

	/**
	 * Construtor alternativo do Backup.
	 * 
	 * @param filepath
	 *            Path para o ficheiro que se quer enviar
	 * @param replicationLevel
	 *            Nível de replicação. Quantos peers devem armazenar chunks
	 *            deste ficheiro. String que representa um int
	 * @throws FileNotFoundException
	 */
	public Backup(String filepath, String replicationLevel) throws FileNotFoundException {
		this(filepath, Integer.parseInt(replicationLevel));
	}

	/**
	 * Divide o ficheiro que se quer fazer backup em chunks e armazena-os em
	 * chunkFiles. Enquanto o tamanho do ficheiro for maior que 0 tenta ler e
	 * criar um chunk.
	 * 
	 * @throws IOException
	 */
	public void splitFile() {
		File bckFile = new File(this.filepath);
		int chunkNo = 0;
		byte[] buffer = new byte[READ_LENGTH];

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(bckFile))) {
			int chunkSize = 0;

			while ((chunkSize = bis.read(buffer)) > 0) {
				byte[] data = buffer.clone();
				chunkNo++;

				if (chunkSize < READ_LENGTH) {
					byte[] lastChunk = Arrays.copyOfRange(buffer, 0, chunkSize);
					this.chunkFiles.add(new Chunk(filepath, chunkNo, replicationLevel, lastChunk));
				} else
					this.chunkFiles.add(new Chunk(filepath, chunkNo, replicationLevel, data));
			}
		} catch (IOException e) {
			System.err.println("Error when tried to read from the file. Backup.splitFile()");
		}
	}

	private void sendingData() {
		System.out.println("Número de chunks: " + chunkFiles.size());
	}
}
