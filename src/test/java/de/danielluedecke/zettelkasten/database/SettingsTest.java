package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.TestObjectFactory;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SettingsTest {

	/**
	 * Common test document. See setUp().
	 */
	public Settings settings;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeEach
	public void setUp() throws JDOMException, IOException {
		// Prepare temporary folder for test.
		tempFolder.create();
		System.setProperty("user.dir", tempFolder.getRoot().getPath());
		System.setProperty("user.home", tempFolder.getRoot().getPath());

		AcceleratorKeys accKeys = new AcceleratorKeys();
		AutoKorrektur autoKorrekt = new AutoKorrektur();
		Synonyms synonyms = new Synonyms();
		StenoData steno = new StenoData();
		settings = new Settings(accKeys, autoKorrekt, synonyms, steno);
	}

	@Test
	void saveSettings_Normal_Success() throws Exception {
		// Change settings to confirm it is writing something new.
		String testValue = "MY_UNIQUE_TEST_FILE_PATH_123456";
		settings.setFilePath(new File(testValue));

		// Run save.
		boolean ok = settings.saveSettings();
		assertTrue(ok);

		// Confirm settings file has testValue.
		Document settingsXml = FileOperationsUtil.readXMLFileFromZipFile(settings.getSettingsFilePath(),
				Constants.settingsFileName);
		XMLOutputter outputter = new XMLOutputter();
		String xmlString = outputter.outputString(settingsXml);
		MatcherAssert.assertThat(xmlString, CoreMatchers.containsString(testValue));
	}

	@Test
	void saveSettings_MissingDir_Fails() {
		tempFolder.delete();

		// Run save. Should fail due to missing directory.
		boolean ok = settings.saveSettings();
		assertTrue(!ok);
	}

	@Test
	void loadSettings_Filepath_Success() throws IOException {
		File settingsTestFile = new File(SettingsTest.class.getClassLoader()
				.getResource("zettelkasten-settings_custom-accelerator.zks3").getPath());
		// Settings file to be used for test must exist.
		assertTrue(settingsTestFile.exists());

		// settings doesn't have an existing file. We will copy the test file to where
		// settings will look at.
		assertTrue(!settings.getSettingsFilePath().exists());
		FileUtils.copyFile(settingsTestFile, settings.getSettingsFilePath());
		assertTrue(settings.getSettingsFilePath().exists());

		// Run load.
		settings.loadSettings();

		// testValue is the value manually added to the settingsTestFile. A successful
		// load make settings have that in the filepath.
		String testValue = "MY_TEST_RECENT_DOC1";
		assertEquals(testValue, settings.getFilePath().getPath());
	}

	@Test
	void loadSettings_acceleratorKeysNewEntryInsertSymbolWithControlAltA_Success() throws IOException {
		File settingsTestFile = new File(SettingsTest.class.getClassLoader()
				.getResource("zettelkasten-settings_custom-accelerator.zks3").getPath());
		// Settings file to be used for test must exist.
		assertTrue(settingsTestFile.exists());

		// settings doesn't have an existing file. We will copy the test file to where
		// settings will look at.
		assertTrue(!settings.getSettingsFilePath().exists());
		FileUtils.copyFile(settingsTestFile, settings.getSettingsFilePath());
		assertTrue(settings.getSettingsFilePath().exists());

		// Run load.
		settings.loadSettings();

		// testValue is the value that was manually added to the settingsTestFile. A
		// successful load make the New Entry Keys have insertSymbol paired with
		// testValue.
		String testValue = "control alt A";
		assertEquals(testValue,
				settings.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "insertSymbol"));
	}

}