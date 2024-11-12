package de.danielluedecke.zettelkasten.tasks;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.swing.*;
import java.io.File;

import static org.junit.Assert.*;

@RunWith(JExample.class)
public class SaveFileTaskTest {

    private SaveFileTask task;
    private Daten mockData;
    private Settings mockSettings;
    private JLabel mockLabel;
    private JDialog mockDialog;

    // Test 1: Initialize resources for further tests
    @Test
    public void initializeResources() {
        // Setup mock objects
        mockData = Mockito.mock(Daten.class);
        mockSettings = Mockito.mock(Settings.class);
        mockLabel = Mockito.mock(JLabel.class);
        mockDialog = Mockito.mock(JDialog.class);

        task = new SaveFileTask(null, mockDialog, mockLabel, mockData, Mockito.mock(Bookmarks.class), Mockito.mock(SearchRequests.class), Mockito.mock(DesktopData.class), Mockito.mock(Synonyms.class), mockSettings, Mockito.mock(BibTeX.class));

        assertNotNull(task);
    }

    // Test 2: Verify save process in doInBackground
    @Given("initializeResources")
    public void shouldSaveDataCorrectly() throws Exception {
        // Setup: configure mocks to simulate successful save path
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(new File("mockSave.zip"));
        // Run
        task.doInBackground();
        // Assert that the saveOk flag remains true after successful save
        assertTrue("Save should be successful", task.saveOk);
    }

    // Test 3: Simulate error in saving process
    @Given("initializeResources")
    public void shouldHandleSaveErrorGracefully() {
        // Setup: Simulate a file path error by returning null from settings
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(null);
        // Run
        task.doInBackground();
        // Verify error was handled and saveOk was set to false
        assertFalse("Save should fail due to null file path", task.saveOk);
    }

    // Test 4: Verify modified flags are set correctly after save
    @Given("shouldSaveDataCorrectly")
    public void shouldSetModifiedFlagsOnSuccess() {
        // Run
        task.succeeded(null);
        // Verify all flags were set based on saveOk
        Mockito.verify(mockData).setModified(false);
    }

    // Test 5: Verify dialog is disposed in the finished method
    @Given("shouldSaveDataCorrectly")
    public void shouldDisposeDialogOnFinished() {
        // Run
        task.finished();
        // Verify dialog is disposed
        Mockito.verify(mockDialog).dispose();
    }
}
