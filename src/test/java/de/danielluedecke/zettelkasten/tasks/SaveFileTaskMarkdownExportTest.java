package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.BibTeXUiCallbacks;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DatenUiCallbacks;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveFileTaskMarkdownExportTest {

	private String originalHome;

	@AfterEach
	void restoreHome() {
		if (originalHome != null) {
			System.setProperty("user.home", originalHome);
		}
	}

	@Test
	void saveFileTaskTriggersMarkdownExport(@TempDir Path tempDir) throws Exception {
		Assumptions.assumeTrue(!isWindows(), "Mock pandoc script uses /bin/sh.");
		setHome(tempDir);
		Path workspace = tempDir.resolve("workspace");
		Files.createDirectories(workspace);

		Settings settings = new Settings();
		settings.setMarkdownActivated(true);
		settings.setMainDataFile(new File(tempDir.resolve("test.zkn3").toString()));
		Path pandocScript = createPandocScript(tempDir);
		settings.setPandocPath(pandocScript.toString());

		Synonyms synonyms = new Synonyms();
		BibTeX bibTeX = new BibTeX(BibTeXUiCallbacks.NO_OP, settings);
		Daten data = new Daten(DatenUiCallbacks.NO_OP, settings, synonyms, bibTeX);
		data.addEntry("Title", "Content", new String[0], new String[0], "", new String[0], "2025-01-01", -1);
		int entryNumber = data.getActivatedEntryNumber();

		Application app = new TestApplication();
		initializeResourceManager(app);

		Bookmarks bookmarks = new Bookmarks(null, settings);
		SearchRequests searchRequests = new SearchRequests(null);
		de.danielluedecke.zettelkasten.database.DesktopData desktopData = createDesktopDataStub();
		JLabel label = new JLabel();
		JDialog dialog = Mockito.mock(JDialog.class);

		SaveFileTask task = new SaveFileTask(app, dialog, label, data, bookmarks, searchRequests,
				desktopData, synonyms, settings, bibTeX);

		task.doInBackground();
		task.succeeded(null);

		Path outFile = workspace.resolve("z" + entryNumber + ".md");
		assertTrue(waitForFile(outFile, Duration.ofSeconds(5)));
	}

	private void setHome(Path tempDir) {
		originalHome = System.getProperty("user.home");
		System.setProperty("user.home", tempDir.toString());
	}

	private de.danielluedecke.zettelkasten.database.DesktopData createDesktopDataStub() {
		de.danielluedecke.zettelkasten.database.DesktopData desktop = Mockito.mock(
				de.danielluedecke.zettelkasten.database.DesktopData.class);
		Document doc = new Document(new Element("desktop"));
		Mockito.when(desktop.getDesktopData()).thenReturn(doc);
		Mockito.when(desktop.getDesktopModifiedEntriesData()).thenReturn(new Document(new Element("modified")));
		Mockito.when(desktop.getDesktopNotesData()).thenReturn(new Document(new Element("notes")));
		return desktop;
	}

	private static void initializeResourceManager(Application app) {
		try {
			java.lang.reflect.Field contextField = Application.class.getDeclaredField("context");
			contextField.setAccessible(true);
			ApplicationContext context = (ApplicationContext) contextField.get(app);
			ResourceManager manager = new TestResourceManager(context);
			java.lang.reflect.Method setResourceManager = ApplicationContext.class
					.getDeclaredMethod("setResourceManager", ResourceManager.class);
			setResourceManager.setAccessible(true);
			setResourceManager.invoke(context, manager);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to initialize ApplicationContext ResourceManager", e);
		}
	}

	private static class TestApplication extends Application {
		@Override
		protected void startup() {
			// no-op for tests
		}
	}

	private static class TestResourceManager extends ResourceManager {
		TestResourceManager(ApplicationContext context) {
			super(context);
		}
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

	private boolean waitForFile(Path file, Duration timeout) throws InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			if (Files.exists(file)) {
				return true;
			}
			Thread.sleep(50);
		}
		return Files.exists(file);
	}
}
