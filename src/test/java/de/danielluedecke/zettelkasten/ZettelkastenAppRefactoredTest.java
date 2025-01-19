package de.danielluedecke.zettelkasten;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ZettelkastenAppRefactoredTest {

    private ZettelkastenAppRefactored app;

    @BeforeEach
    void setUp() {
        app = new ZettelkastenAppRefactored();
        app.initialize(new String[]{});
    }

    @Test
    void testDetermineLocaleUsingPowerMock() throws Exception {
        // Use PowerMock's Whitebox to access the private method
        Locale locale;

        // Test for English
        locale = Whitebox.invokeMethod(app, "determineLocale", "en");
        assertEquals(new Locale("en", "GB"), locale);

        // Test for German
        locale = Whitebox.invokeMethod(app, "determineLocale", "de");
        assertEquals(new Locale("de", "DE"), locale);

        // Test for Spanish
        locale = Whitebox.invokeMethod(app, "determineLocale", "es");
        assertEquals(new Locale("es", "ES"), locale);

        // Test for Portuguese
        locale = Whitebox.invokeMethod(app, "determineLocale", "pt");
        assertEquals(new Locale("pt", "BR"), locale);

        // Test for unsupported language (fallback)
        locale = Whitebox.invokeMethod(app, "determineLocale", "fr");
        assertEquals(new Locale("en", "GB"), locale);
    }

    @Test
    void testProcessDataFileValidFileUsingPowerMock() throws Exception {
        File validFile = new File("validFile.zkn");
        validFile.createNewFile(); // Create a valid file for the test

        // Use PowerMock's Whitebox to invoke the private method
        Whitebox.invokeMethod(app, "processDataFile", validFile.getAbsolutePath());

        // Verify that the valid file is set in settings
        assertEquals(validFile, app.getSettings().getMainDataFile());

        // Clean up
        validFile.delete();
    }

    @Test
    void testProcessDataFileInvalidFileUsingPowerMock() throws Exception {
        String invalidFilePath = "invalidFile.zkn";

        // Use PowerMock's Whitebox to invoke the private method
        Whitebox.invokeMethod(app, "processDataFile", invalidFilePath);

        // Verify that no file is set in settings
        assertNull(app.getSettings().getMainDataFile());
    }

    @Test
    void testProcessInitialEntryNumberValidUsingPowerMock() throws Exception {
        // Use PowerMock's Whitebox to invoke the private method
        Whitebox.invokeMethod(app, "processInitialEntryNumber", "123");

        // Verify the initial entry number is set in settings
        assertEquals(123, app.getSettings().getInitialParamZettel());
    }

    @Test
    void testProcessInitialEntryNumberInvalidUsingPowerMock() throws Exception {
        // Use PowerMock's Whitebox to invoke the private method
        Whitebox.invokeMethod(app, "processInitialEntryNumber", "abc");

        // Verify no initial entry number is set in settings
        assertNull(app.getSettings().getInitialParamZettel());
    }

    @Test
    void testProcessInitialEntryNumberNegativeUsingPowerMock() throws Exception {
        // Use PowerMock's Whitebox to invoke the private method
        Whitebox.invokeMethod(app, "processInitialEntryNumber", "-1");

        // Verify no initial entry number is set in settings
        assertNull(app.getSettings().getInitialParamZettel());
    }
}
