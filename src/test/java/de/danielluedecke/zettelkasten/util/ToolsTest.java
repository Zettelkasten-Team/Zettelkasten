package de.danielluedecke.zettelkasten.util;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ToolsTest {

	@Test
	void testEntryNumberListStringToIntArray() {
		Assert.assertEquals(Arrays.asList(1), Tools.entryNumberListStringToIntArray("1"));
		Assert.assertEquals(Arrays.asList(1,2), Tools.entryNumberListStringToIntArray("1,2"));
		Assert.assertEquals(Arrays.asList(1,2,3), Tools.entryNumberListStringToIntArray("1-3"));
		Assert.assertEquals(Arrays.asList(1, 3,4), Tools.entryNumberListStringToIntArray("1,3-4"));
		
		Assert.assertEquals(null, Tools.entryNumberListStringToIntArray(""));
		Assert.assertEquals(null, Tools.entryNumberListStringToIntArray("not,number"));
		Assert.assertEquals(null, Tools.entryNumberListStringToIntArray("not-number"));
	}
}
