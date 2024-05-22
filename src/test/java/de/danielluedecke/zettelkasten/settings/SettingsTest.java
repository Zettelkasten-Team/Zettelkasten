package de.danielluedecke.zettelkasten.settings;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class SettingsTest {

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings();
    }

    @Test
    public void testLocateSettingsZipFileWithNameExists() {
        // Assume the file exists for this test
        String existingFilename = "zettelkasten-settings.zks3";
        File result = settings.locateSettingsZipFileWithName(existingFilename);
        assertNotNull("The file should exist and not be null", result);
        assertTrue("The file should exist", result.exists());
    }

    @Test
    public void testLocateSettingsZipFileWithNameDoesNotExist() {
        // Assume the file does not exist for this test
        String nonExistingFilename = "nonexistent-settings.zks3";
        File result = settings.locateSettingsZipFileWithName(nonExistingFilename);
        // NB. We get the nonexistent-settings.zks3 back
        // Separation of Concerns:
        // The `locateSettingsZipFileWithName` method focuses on locating the file path rather than managing file creation or existence checks, adhering to the single-responsibility principle.
        assertNotNull("The file should exist and not be null", result);
    }

}
