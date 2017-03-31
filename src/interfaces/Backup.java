package interfaces;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Backup {
	private String filepath; // used to get the file
	private int replicationLevel;
	private ArrayList<Chunk> chunkFiles = new ArrayList<Chunk>();
	private int READ_LENGTH = 64000;

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
//		//FileInputStream readStream = new FileInputStream(bckFile);
//
//		System.out.println("Backing up file: " + bckFile.getAbsolutePath());
//		int fileSize = (int) bckFile.length();
//		System.out.println("File size is: " + fileSize + " bytes.");
//		int chunkNo = 0;
//		byte[] byteChunkPart;
//
//
//		while(fileSize > 0){
//			int toRead = READ_LENGTH;
//
//			if(fileSize < READ_LENGTH)
//				toRead = fileSize;
//
//			byteChunkPart = new byte[toRead];
//
//			try  (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(bckFile))) {
//				fileSize -= bis.read(byteChunkPart, 0, toRead);
//				chunkNo++;
//				System.out.println("Leu: " + byteChunkPart.length);
//				Chunk chunk = new Chunk(filepath, chunkNo, replicationLevel, byteChunkPart);
//				this.chunkFiles.add(chunk);
//			} catch (IOException e) {
//				System.out.println("Failed to read from file in Backup.splitFile(). " + e.getMessage());
//			}
//		}


		//        try (BufferedInputStream bis = new BufferedInputStream(readStream)) {//try-with-resources to ensure closing stream
		//            String name = bckFile.getName();
		//
		//            int tmp = 0;
		//            while ((tmp = bis.read(byteChunkPart)) > 0) {
		//                //write each chunk of data into separate file with different number in name
		//                File newFile = new File(bckFile.getParent(), name + "." + String.format("%03d", chunkNo++));
		//                try (FileOutputStream out = new FileOutputStream(newFile)) {
		//                    out.write(byteChunkPart, 0, tmp);
		//                }
		//            }
		//        } catch (IOException e1) {
		//			e1.printStackTrace();
		//		}


		int partCounter = 1;//I like to name parts from 001, 002, 003, ...
		//you can change it to 0 if you want 000, 001, ...

		int sizeOfFiles = 64000;
		byte[] buffer = new byte[sizeOfFiles];

		try (BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(bckFile))) {//try-with-resources to ensure closing stream
			String name = bckFile.getName();

			int tmp = 0;
			while ((tmp = bis.read(buffer)) > 0) {
				//write each chunk of data into separate file with different number in name
				File newFile = new File(bckFile.getParent(), "chunks/" + name + "."
						+ String.format("%03d", partCounter++));
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, tmp);//tmp is chunk size
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendingData(){
		System.out.println("Número de chunks: " + chunkFiles.size());
	}
}
