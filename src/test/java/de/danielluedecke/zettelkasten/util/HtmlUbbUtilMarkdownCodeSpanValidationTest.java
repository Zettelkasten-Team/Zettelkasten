package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.BibTeXUiCallbacks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DatenUiCallbacks;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlUbbUtilMarkdownCodeSpanValidationTest {

	@Test
	void markdownInlineCodeWithUnderscoresProducesValidHtml() {
		Settings settings = new Settings();
		settings.setMarkdownActivated(true);
		Daten data = createData(settings);

		String content = "Check `EditorFrame.saveEntry` and `ZETTELKASTEN_WORKSPACE_DIR` in code spans.\n"
				+ "Also `~/workspace` and `<code>` should stay literal.\n\n"
				+ "* **Bold** list item with `ZETTELKASTEN_WORKSPACE_DIR`\n"
				+ "* Another item with `EditorFrame.saveEntry` and underscores.\n";
		data.addEntry("Title", content, new String[0], new String[0], "", new String[0], "2025-01-01", -1);
		int entryNumber = data.getActivatedEntryNumber();

		String html = data.getEntryAsHtml(entryNumber, null, Constants.FRAME_DESKTOP);

		assertFalse(html.matches("(?s)<code>.*?</?(i|b|em|strong)>.*?</code>"),
				"HTML must not contain emphasis tags inside <code> spans");
		assertTrue(html.contains("<code>EditorFrame.saveEntry</code>"));
		assertTrue(html.contains("<code>ZETTELKASTEN_WORKSPACE_DIR</code>"));
		assertTrue(html.contains("<code>&lt;code&gt;</code>"));
		assertTrue(HtmlValidator.isValidHTML(html, entryNumber, content));
	}

	private Daten createData(Settings settings) {
		Synonyms synonyms = new Synonyms();
		BibTeX bibTeX = new BibTeX(BibTeXUiCallbacks.NO_OP, settings);
		return new Daten(DatenUiCallbacks.NO_OP, settings, synonyms, bibTeX);
	}
}
