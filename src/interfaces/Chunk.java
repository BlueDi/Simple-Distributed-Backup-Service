package interfaces;

public class Chunk implements Comparable<Chunk> {
	private String fileId;
	private int replicationDegree;
	private int chunkNumber;
	private byte[] content;
	private boolean checked;
	
	public Chunk(String fileId, int chunkNumber, int replicationDegree, byte[] content) {
		this.fileId = fileId;
		this.replicationDegree = replicationDegree;
		this.chunkNumber = chunkNumber;
		this.content = content;
		this.checked = false;
	}
	
	/**
	 * @return the check
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param check the check to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
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
