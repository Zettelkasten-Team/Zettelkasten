package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.BibTeXUiCallbacks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DatenUiCallbacks;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlUbbUtilMarkdownCodeSpanValidationTest {

	/**
	 * Used as user.dir and user.home, so new Settings() neither reads nor creates
	 * real Zettelkasten settings. Deleted by JUnit Jupiter after each test.
	 */
	@TempDir
	Path settingsDir;

	private String originalUserDir;
	private String originalUserHome;

	@BeforeEach
	void isolateSettings() {
		originalUserDir = System.getProperty("user.dir");
		originalUserHome = System.getProperty("user.home");
		System.setProperty("user.dir", settingsDir.toString());
		System.setProperty("user.home", settingsDir.toString());
	}

	@AfterEach
	void restoreSettingsLocation() {
		// Runs also when a test fails.
		restoreProperty("user.dir", originalUserDir);
		restoreProperty("user.home", originalUserHome);
	}

	private static void restoreProperty(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}

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
