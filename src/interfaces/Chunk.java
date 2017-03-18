package interfaces;

public class Chunk implements Comparable<Chunk> {
	private String fileId;
	private int replicationDegree;
	private int chunkNumber;
	private byte[] content;
	
	public Chunk(String fileId, int chunkNumber, int replicationDegree, byte[] content) {
		this.fileId = fileId;
		this.replicationDegree = replicationDegree;
		this.chunkNumber = chunkNumber;
		this.content = content;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public int getReplicationDegree() {
		return replicationDegree;
	}
	
	public int getChunkNumber() {
		return chunkNumber;
	}
	
	public byte[] getContent() {
		return content;
	}

	@Override
	public int compareTo(Chunk chunk2) {
		return fileId.equals(chunk2.fileId) && chunkNumber == chunk2.chunkNumber ? 0 : -1;
	}
}
