package handlers;

class ChunkInfo implements Comparable<ChunkInfo>{
	public String fileId;
	public String senderId;
	public int chunkNo;
	
	ChunkInfo(String senderId, String fileId){
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = 0;
	}

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
