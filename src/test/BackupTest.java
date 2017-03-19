package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import interfaces.Backup;

public class BackupTest {
	@Test
	public void testSplitFile() throws Exception {
		String filetotest = "lorem_ipsum.txt";
		int replicationLevel = 2;
		
		Backup backup = new Backup(filetotest, replicationLevel);
		
		backup.splitFile();
		
		assertTrue(backup.getChunkFiles().size()>0);
		
	}
}
