package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.BibTeXUiCallbacks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DatenUiCallbacks;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorFrameMarkdownExportTest {

	private String originalHome;

	@AfterEach
	void restoreHome() {
		if (originalHome != null) {
			System.setProperty("user.home", originalHome);
		}
	}

	@Test
	void exportOnSaveWritesMarkdown(@TempDir Path tempDir) throws IOException {
		Assumptions.assumeTrue(!isWindows(), "Mock pandoc script uses /bin/sh.");
		setHome(tempDir);
		Path workspace = tempDir.resolve("workspace");
		Files.createDirectories(workspace);

		Settings settings = new Settings();
		Path pandocScript = createPandocScript(tempDir);
		settings.setPandocPath(pandocScript.toString());

		Daten data = createData(settings);
		data.addEntry("Title", "Content", new String[0], new String[0], "", new String[0], "2025-01-01", -1);
		int entryNumber = data.getActivatedEntryNumber();

		EditorFrame.exportMarkdownOnSave(settings, data, entryNumber, false);

		Path outFile = workspace.resolve("z" + entryNumber + ".md");
		assertTrue(Files.exists(outFile));
		assertTrue(new String(Files.readAllBytes(outFile), StandardCharsets.UTF_8).contains("# exported"));
	}

	private void setHome(Path tempDir) {
		originalHome = System.getProperty("user.home");
		System.setProperty("user.home", tempDir.toString());
	}

	private Daten createData(Settings settings) {
		Synonyms synonyms = new Synonyms();
		BibTeX bibTeX = new BibTeX(BibTeXUiCallbacks.NO_OP, settings);
		return new Daten(DatenUiCallbacks.NO_OP, settings, synonyms, bibTeX);
	}

	private Path createPandocScript(Path tempDir) throws IOException {
		Path script = tempDir.resolve("pandoc-mock.sh");
		String content = "#!/bin/sh\n"
				+ "out=\"\"\n"
				+ "prev=\"\"\n"
				+ "for arg in \"$@\"; do\n"
				+ "  if [ \"$prev\" = \"-o\" ]; then\n"
				+ "    out=\"$arg\"\n"
				+ "    break\n"
				+ "  fi\n"
				+ "  prev=\"$arg\"\n"
				+ "done\n"
				+ "if [ -n \"$out\" ]; then\n"
				+ "  echo \"# exported\" > \"$out\"\n"
				+ "fi\n"
				+ "exit 0\n";
		Files.write(script, content.getBytes(StandardCharsets.UTF_8));
		script.toFile().setExecutable(true);
		return script;
	}

	private boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName != null && osName.toLowerCase().contains("win");
	}
}
