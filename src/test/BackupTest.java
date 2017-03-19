package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import interfaces.Backup;

public class BackupTest {
	@Test
	public void testSplitFile() throws Exception {
		int n = 14;
		Backup backup = new Backup();
		
		backup.splitFile();
		
		assertTrue(backup.getChunkFiles().size()>0);
		
	}
}
