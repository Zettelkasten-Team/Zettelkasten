package de.danielluedecke.zettelkasten.util;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import de.danielluedecke.zettelkasten.view.EntryID;

public class EntryIDUtilsTest {
	@Test
	void testCsvToEntryIDList() {
		// Expected normal cases.
		Assert.assertEquals(Arrays.asList(), EntryIDUtils.csvToEntryIDList(""));
		Assert.assertEquals(Arrays.asList(new EntryID(1), new EntryID(2)), EntryIDUtils.csvToEntryIDList("1,2"));
		
		// Treated cases.
		Assert.assertEquals(Arrays.asList(new EntryID(1)), EntryIDUtils.csvToEntryIDList(",1"));
		Assert.assertEquals(Arrays.asList(new EntryID(1), new EntryID(2)), EntryIDUtils.csvToEntryIDList("1,2,"));
		
		// Exception cases
		Assert.assertThrows(NumberFormatException.class, () -> {
			EntryIDUtils.csvToEntryIDList("noncsv,");
	    });
		Assert.assertThrows(NumberFormatException.class, () -> {
			EntryIDUtils.csvToEntryIDList("1,2,nonnumber");
	    });
	}
}
