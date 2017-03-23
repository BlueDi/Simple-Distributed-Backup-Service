package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;

import interfaces.Backup;

public class BackupTest {
	@Test
	public void testSplitFile() throws FileNotFoundException {
		String filetotest = "lorem_ipsum.txt";
		Backup backup = new Backup(filetotest, 2);
		
		assertTrue(backup.getChunkFiles().size() == 15);
	}
}
