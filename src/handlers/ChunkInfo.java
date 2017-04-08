package handlers;

public class ChunkInfo {
	public String fileId;
	public int chunkNo;

	public ChunkInfo(String fileId, int chunkNo) {
		this.fileId = fileId;
		this.chunkNo = chunkNo;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return obj.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {
		return fileId.hashCode() + chunkNo;
	}
}