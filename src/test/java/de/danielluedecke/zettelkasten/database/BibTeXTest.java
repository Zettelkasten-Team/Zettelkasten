package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.settings.Settings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BibTeXTest {

	/**
	 * Managed by JUnit Jupiter and deleted after each test, also when the test
	 * fails.
	 */
	@TempDir
	Path tempDir;

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
		Path bibFile = Files.createFile(tempDir.resolve("zettelkasten-bibtex.bib"));
		Files.write(bibFile,
				"@article{key, author={Doe, John}, title={Title}, year={2020}}".getBytes(StandardCharsets.UTF_8));
		bibTeX.setFilePath(bibFile.toFile());

		assertTrue(bibTeX.openAttachedFile("UTF-8", false));
		assertEquals(1, summary[0]);
		assertEquals(0, summary[1]);
	}
}
