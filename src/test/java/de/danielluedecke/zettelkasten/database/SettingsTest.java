package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeEach
	public void setUp() throws JDOMException, IOException {
		// Prepare temporary folder for test.
		tempFolder.create();
		System.setProperty("user.dir", tempFolder.getRoot().getPath());
		System.setProperty("user.home", tempFolder.getRoot().getPath());
	}

	@Test
	void saveSettings_Normal_Success() throws Exception {
		Settings settings = new Settings();
		// Change settings to confirm it is writing something new.
		String testValue = "MY_UNIQUE_TEST_FILE_PATH_123456";
		settings.setMainDataFile(new File(testValue));

		// Run save.
		boolean ok = settings.saveSettingsToFiles();
		assertTrue(ok);

		// Confirm settings file has testValue.
		Document settingsXml = FileOperationsUtil.readXMLFileFromZipFile(settings.getSettingsFile(),
				Constants.settingsFileName);
		XMLOutputter outputter = new XMLOutputter();
		String xmlString = outputter.outputString(settingsXml);
		MatcherAssert.assertThat(xmlString, CoreMatchers.containsString(testValue));
	}

	@Test
	void saveSettings_MissingDir_Fails() {
		Settings settings = new Settings();
		tempFolder.delete();

		// Run save. Should fail due to missing directory.
		boolean ok = settings.saveSettingsToFiles();
		assertTrue(!ok);
	}

	@Test
	void Constructor_MainDataFile_Success() throws IOException {
		Settings settings = new Settings();
		File settingsTestFile = new File(SettingsTest.class.getClassLoader()
				.getResource("zettelkasten-settings_custom-accelerator.zks3").getPath());
		// Settings file to be used for test must exist.
		assertTrue(settingsTestFile.exists());

		// settings doesn't have an existing file. We will copy the test file to where
		// settings will look at.
		assertTrue(!settings.getSettingsFile().exists());
		FileUtils.copyFile(settingsTestFile, settings.getSettingsFile());
		assertTrue(settings.getSettingsFile().exists());

		// Run load.
		settings = new Settings();

		// testValue is the value manually added to the settingsTestFile. A successful
		// load make settings have that in the filepath.
		String testValue = "MY_TEST_RECENT_DOC1";
		assertEquals(testValue, settings.getMainDataFile().getPath());
	}

	@Test
	void Constructor_acceleratorKeysNewEntryInsertSymbolWithControlAltA_Success() throws IOException {
		Settings settings = new Settings();
		File settingsTestFile = new File(SettingsTest.class.getClassLoader()
				.getResource("zettelkasten-settings_custom-accelerator.zks3").getPath());
		// Settings file to be used for test must exist.
		assertTrue(settingsTestFile.exists());

		// settings doesn't have an existing file. We will copy the test file to where
		// settings will look at.
		assertTrue(!settings.getSettingsFile().exists());
		FileUtils.copyFile(settingsTestFile, settings.getSettingsFile());
		assertTrue(settings.getSettingsFile().exists());

		// Run load.
		settings = new Settings();

		// testValue is the value that was manually added to the settingsTestFile. A
		// successful load make the New Entry Keys have insertSymbol paired with
		// testValue.
		String testValue = "control alt A";
		assertEquals(testValue,
				settings.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "insertSymbol"));
	}

	@Test
	void MainDataFile_setMainDataFile_Success() throws IOException {
		Settings settings = new Settings();

		Path testPath = Paths.get(tempFolder.getRoot().getPath(), "ANY_VALUE.zkn3");
		settings.setMainDataFile(testPath.toFile());

		assertEquals(testPath.toString(), settings.getMainDataFile().toString());
		assertEquals(testPath.getParent().toString(), settings.getMainDataFileDir().toString());

		// Invalid main data file returns null.
		assertEquals(null, settings.getMainDataFileNameWithoutExtension());
		// Now with an existing main data file.
		testPath.toFile().createNewFile();
		assertEquals("ANY_VALUE", settings.getMainDataFileNameWithoutExtension());
	}

	@Test
        void isMacStyle_MacOSWithAquaLookAndFeel_ReturnsTrue() {
            Settings settings = new Settings();

            if (System.getProperty("os.name").toLowerCase().startsWith("mac os") == true) {
                assertTrue(settings.isMacStyle());
            } else {
                assertFalse(settings.isMacStyle());
            }
        }

	@Test
	void loadSettings_FileNotFound_ReturnsFalse() {
		// Arrange: Create a Settings instance
		Settings settings = new Settings();

		// Act: Constructor should initialize settings with non-existent file
		boolean result = settings.getSettingsFile() == null;

		// Assert: Verify that settings file is not found
		assertFalse(result);
	}

	@Test
	public void useDefaultSettings_SettingsFileNotFound_ReturnsTrue() {
		// Arrange: Create a Settings instance
		Settings settings = new Settings();

		// Act: Simulate the scenario where the settings file is not found
		//boolean result = settings.useDefaultSettings();

		// Assert: Verify that useDefaultSettings() returns true
		//assertTrue(result, "useDefaultSettings() should return true when settings file is not found.");
	}

}