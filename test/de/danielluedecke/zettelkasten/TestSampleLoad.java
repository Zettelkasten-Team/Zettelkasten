package de.danielluedecke.zettelkasten;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;

public class TestSampleLoad {

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
				.equals("[h1]Headline[/h1]\n" +
						"\n" +
						"[q]Zitat, welches korrekt übernommen wird[/q]\n" +
						"\n" +
						"Das hier ist ein [qm]inline quote[/qm].\n" +
						"\n" +
						"# Headline\n" +
						"\n" +
						"> Zitat, welches nicht korrekt übernommen wird\n" +
						"\n" +
						"Das hier ist ein [qm]inline quote[/qm]."));
		System.out.println("Content is " + content);
	}

}
