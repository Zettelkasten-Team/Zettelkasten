package de.danielluedecke.zettelkasten;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZettelkastenAppPowerMockTest {

    private ZettelkastenApp app;

    @BeforeEach
    void setUp() {
        app = new ZettelkastenApp();
        app.initialize(new String[]{});  // Initialize the app
        app.initializeSettings();       // Ensure settings is initialized
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
