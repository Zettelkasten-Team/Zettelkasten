package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.DesktopData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jdesktop.application.SingleFrameApplication;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;

import javax.swing.*;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.OngoingStubbing;

public class ZettelkastenViewTest {

    private SingleFrameApplication mockApp;
    private Settings mockSettings;
    private TasksData mockTasksData;

     ZettelkastenView zettelkastenView;
    private EditorFrame mockEditorFrame;

    @BeforeEach
    public void setUp() {
        try {
            // Initialize mock objects manually
            mockApp = mock(SingleFrameApplication.class);
            mockSettings = mock(Settings.class);
            mockTasksData = mock(TasksData.class);
            mockEditorFrame = mock(EditorFrame.class);

            // Create ZettelkastenView instance with mock dependencies
            zettelkastenView = new ZettelkastenView(mockApp, mockSettings, mockTasksData);

            // Mock the ApplicationContext and ResourceMap
            ApplicationContext applicationContext = mock(ApplicationContext.class);
            ResourceMap resourceMap = mock(ResourceMap.class);

            // Set up the mock behavior to return the expected title
            when(applicationContext.getResourceMap()).thenReturn(resourceMap);
            when(resourceMap.getString("Application.title")).thenReturn("Zettelkasten");

            // Set the mocked ApplicationContext for the ZettelkastenView instance
            zettelkastenView.setContext(applicationContext);

        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException | java.io.IOException | NullPointerException e) {
            // Handle the exception
            e.printStackTrace(); // Or any other error handling mechanism
        }
    }

    @Test
    public void testShowNewEntryWindow() {
        Assertions.assertNotNull(zettelkastenView);
        zettelkastenView.showNewEntryWindow();

        // Verify that the SingleFrameApplication mock is used properly
        verify(mockApp).show(any(JFrame.class));
    }
    
    @Test
    public void testDesktopData() {
        // Create a mock ZettelkastenView instance
        ZettelkastenView mockZkn = mock(ZettelkastenView.class);
        // Stub the getResourceMap method to return a mock resource map
        // Stub the getResourceMap method to return a mock resource map


        // Create a DesktopData object with the mock ZettelkastenView
        DesktopData desktopData = new DesktopData(mockZkn);
        // Test the behavior of the DesktopData object
        // For example, you can verify that the clear method is called

    }
    
}
