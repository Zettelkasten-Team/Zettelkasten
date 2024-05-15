package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.Daten;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SampleLoadTest {

	private Daten daten;

	@Before
	public void initialize() throws Exception {
		daten = TestObjectFactory
				.getDaten(TestObjectFactory.ZKN3Settings.ZKN3_SAMPLE);
	}

	@Test
	public void testContentRetrieval() {
		int zettelNumber = 42;		
		Object content = daten.getZettelContent(zettelNumber);
		assertFalse("Could not retrieve zettel content", content.toString()
				.equals(""));
		System.out.println("Content is " + content);
	}

}
