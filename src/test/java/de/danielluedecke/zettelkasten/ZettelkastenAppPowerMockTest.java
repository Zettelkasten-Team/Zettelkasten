package de.danielluedecke.zettelkasten;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZettelkastenAppPowerMockTest {

    private ZettelkastenApp app;

    /**
     * Used as user.dir and user.home, so new Settings() neither reads nor creates
     * real Zettelkasten settings. Deleted by JUnit Jupiter after each test.
     */
    @TempDir
    Path settingsDir;

    private String originalUserDir;
    private String originalUserHome;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.dir", settingsDir.toString());
        System.setProperty("user.home", settingsDir.toString());
        app = new ZettelkastenApp();
        app.initialize(new String[]{});  // Initialize the app
        app.initializeSettings();       // Ensure settings is initialized
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
    void testCommandLineParamsValidFile() throws Exception {
        String validFilePath = "test.zkn";
        File validFile = new File(validFilePath);

        // Use PowerMock to invoke the private method
        Whitebox.invokeMethod(app, "updateSettingsWithCommandLineParams", (Object) new String[]{validFilePath});

        // Valid file should be set in settings
        assertEquals(validFile, app.getSettings().getMainDataFile(), "Valid file should be set in settings.");
    }

    @Test
    void testCommandLineParamsInvalidFile() throws Exception {
        String invalidFilePath = "invalid.zkn";

        // Use PowerMock to invoke the private method
        Whitebox.invokeMethod(app, "updateSettingsWithCommandLineParams", (Object) new String[]{invalidFilePath});

        // Since the file doesn't exist, it should not be set
        assertNull(app.getSettings().getMainDataFile(), "Invalid file should not be set.");
    }

    @Test
    void testCommandLineParamsInvalidParameter() throws Exception {
        String invalidParameter = "not-a-number";

        // Use PowerMock to invoke the private method
        Whitebox.invokeMethod(app, "updateSettingsWithCommandLineParams", (Object) new String[]{invalidParameter});

        // Ensure no data file or initial entry is set
        assertNull(app.getSettings().getMainDataFile(), "Invalid parameter should not set a data file.");
        assertNull(app.getSettings().getInitialParamZettel(), "Invalid parameter should not set an initial entry.");
    }

}
