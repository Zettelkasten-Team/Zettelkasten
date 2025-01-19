package de.danielluedecke.zettelkasten.tasks;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.swing.*;
import java.io.File;

import static org.junit.Assert.*;

@RunWith(JExample.class)
public class SaveFileTaskTest {

    private SaveFileTask task;
    private Application mockApp;
    private Daten mockData;
    private Settings mockSettings;
    private JLabel mockLabel;
    private JDialog mockDialog;

    // Test 1: Initialize resources for further tests
    @Test
    public void initializeResources() {
        // Mock the Application and its ApplicationContext
        mockApp = Mockito.mock(Application.class);
        ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
        ResourceMap mockResourceMap = Mockito.mock(ResourceMap.class);

        // Configure the mock ResourceMap to return a mock string for any key
        Mockito.when(mockResourceMap.getString(Mockito.anyString())).thenReturn("mockMessage");

        // Configure the ApplicationContext to return the mock ResourceMap
        Mockito.when(mockContext.getResourceMap()).thenReturn(mockResourceMap);
        Mockito.when(mockApp.getContext()).thenReturn(mockContext);

        // Mock other dependencies
        mockData = Mockito.mock(Daten.class);
        mockSettings = Mockito.mock(Settings.class);
        mockLabel = Mockito.mock(JLabel.class);
        mockDialog = Mockito.mock(JDialog.class);

        // Initialize the task with mocked dependencies
        task = new SaveFileTask(mockApp, mockDialog, mockLabel, mockData,
                Mockito.mock(Bookmarks.class), Mockito.mock(SearchRequests.class),
                Mockito.mock(DesktopData.class), Mockito.mock(Synonyms.class),
                mockSettings, Mockito.mock(BibTeX.class));

        // Assertions and verifications
        assertNotNull(task);
    }

    // Test 3: Simulate error in saving process
    @Test
    @Given("initializeResources")
    public void shouldHandleSaveErrorGracefully() {
        // Setup: Simulate a file path error by returning null from settings
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(null);
        // Run
        task.doInBackground();
        // Verify error was handled and saveOk was set to false
        assertFalse("Save should fail due to null file path", task.saveOk);
    }

}
