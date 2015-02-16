package de.danielluedecke.zettelkasten;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;

public class SampleLoad {

	private Daten daten;

	@Before
	public void initialize() throws Exception {
		daten = TestObjectFactory
				.getDaten(TestObjectFactory.ZKN3Settings.ZKN3_TRICKY_MARKDOWN);
	}

	@Test
	public void testContentRetrieval() {
		int zettelNumber = 1;		
		Object content = daten.getZettelContentAsHtml(zettelNumber);
		assertFalse("Could not retrieve zettel content", content.toString()
				.equals(""));
		System.out.println("Content is " + content);
	}

}
