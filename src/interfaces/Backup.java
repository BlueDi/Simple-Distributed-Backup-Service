package interfaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Backup {
	private String filename; // used for hashing only
	private String filepath; // used to get the file
	private int replicationLevel; 
	private String owner; // used for hashing only
	private ArrayList<Chunk> chunkFiles = new ArrayList<Chunk>();
	private int READ_LENGTH = 6400;

	public ArrayList<Chunk> getChunkFiles() {
		return chunkFiles;
	}

	public Backup() throws NoSuchAlgorithmException, IOException{
		this.filepath = "lorem_ipsum.txt";
		this.replicationLevel = 2;
		this.owner = "me";

		splitFile();
		
		sendingData();
	}
	
	public void splitFile(){
		File bckFile = new File(this.filepath);
		System.out.println(bckFile.getAbsolutePath());
		FileInputStream readStream;
		int fileS = (int) bckFile.length();
		int chunkNo = 1;
		byte[] byteChunkPart;
		
		try {
			readStream = new FileInputStream(bckFile);
			while(fileS > 0){
				if(fileS < READ_LENGTH)
					READ_LENGTH = fileS;
				byteChunkPart = new byte[READ_LENGTH];
				int read = readStream.read(byteChunkPart, 0, READ_LENGTH);
				fileS -= read;
				chunkNo += 1;

				Chunk chunk = new Chunk(filename, replicationLevel, chunkNo, byteChunkPart);
				this.chunkFiles.add(chunk);
			}
			readStream.close();

		}catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private void sendingData() throws IOException{		
		String msgStream = "uau";

		byte[] messageCompleted = msgStream.getBytes();
		System.out.println("numero de chunks: " + chunkFiles.size());
		//Peer.mdb.send(messageCompleted);
	}
}
