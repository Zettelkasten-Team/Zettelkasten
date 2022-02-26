package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.util.Constants;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
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
        settings = new Settings(accKeys,autoKorrekt,synonyms,steno);
    }

    @Test
    void saveSettings_Normal_Success() throws IOException {
    	// Change settings to confirm it is writing something new.
    	String testValue = "MY_UNIQUE_TEST_FILE_PATH_123456";
    	settings.setFilePath(new File(testValue));
    	
        // Run save.
        boolean ok = settings.saveSettings();
        assertTrue(ok);
        
        // Confirm settings file has testValue.
        String settingsXml = "";
        ZipInputStream zip = new ZipInputStream(new FileInputStream(settings.getSettingsFilePath()));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals(Constants.settingsFileName)) {
            	settingsXml = new String(zip.readAllBytes());
                break;
            }
        }
        MatcherAssert.assertThat(settingsXml, CoreMatchers.containsString(testValue));
    }
    
    @Test
    void saveSettings_MissingDir_Fails() {
    	tempFolder.delete();
    	
        // Run save. Should fail due to missing directory.
    	boolean ok = settings.saveSettings();
        assertTrue(!ok);
    }

}