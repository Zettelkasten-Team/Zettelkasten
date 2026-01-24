package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.settings.Settings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BibTeXTest {

	@Test
	void openAttachedFileNotifiesImportSummary() throws IOException {
		Settings settings = new Settings();
		final int[] summary = new int[] { -1, -1 };
		BibTeXUiCallbacks callbacks = new BibTeXUiCallbacks() {
			@Override
			public void setBackupNecessary() {
			}

			@Override
			public void notifyImportSummary(int newEntries, int updatedEntries) {
				summary[0] = newEntries;
				summary[1] = updatedEntries;
			}
		};
		BibTeX bibTeX = new BibTeX(callbacks, settings);
		Path bibFile = Files.createTempFile("zettelkasten-bibtex", ".bib");
		Files.write(bibFile,
				"@article{key, author={Doe, John}, title={Title}, year={2020}}".getBytes(StandardCharsets.UTF_8));
		bibTeX.setFilePath(bibFile.toFile());

		assertTrue(bibTeX.openAttachedFile("UTF-8", false));
		assertEquals(1, summary[0]);
		assertEquals(0, summary[1]);
	}
}
