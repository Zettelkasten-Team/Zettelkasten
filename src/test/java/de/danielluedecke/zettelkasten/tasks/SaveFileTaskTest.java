package de.danielluedecke.zettelkasten.tasks;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.swing.*;
import java.io.File;

import static org.junit.Assert.*;

@RunWith(JExample.class)
@Ignore("Disabled due to AppFramework initialization instability in test harness")
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
        // Initialize a minimal Application context so ResourceMap is available
        mockApp = new TestApplication();
        initializeResourceManager(mockApp);

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
}
