package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;

import org.jdesktop.application.SingleFrameApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.*;

public class ZettelkastenViewTest {

    private ZettelkastenView zettelkastenView;

    @BeforeEach
    public void setUp() throws UnsupportedLookAndFeelException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // Initialize mock objects manually
        SingleFrameApplication mockApp = mock(SingleFrameApplication.class);
        Settings mockSettings = mock(Settings.class);
        TasksData mockTasksData = mock(TasksData.class);
        SearchRequests mockSearchRequests = mock(SearchRequests.class);
        DesktopData mockDesktopData = mock(DesktopData.class);

        // Create ZettelkastenView instance with mock dependencies
        zettelkastenView = new ZettelkastenView(mockApp, mockSettings, mockTasksData);
        
        // Set the mock SearchRequests object using the setter method
        zettelkastenView.setSearchRequests(mockSearchRequests);
        
        // Set the mock DesktopData object using the setter method
        zettelkastenView.setDesktopData(mockDesktopData);
    }

    @Test
    public void testZettelkastenViewConstructor() {
        SearchRequests mockSearchRequests = null;

        // Initialize SearchRequests separately using setter or initialization method
        zettelkastenView.setSearchRequests(mockSearchRequests);

        // Perform your assertions or verifications
        // For example:
        assertNotNull(zettelkastenView);
        Object mockApp = null;
        assertEquals(mockApp, zettelkastenView.getApplication());
        assertEquals(mockSearchRequests, zettelkastenView.getSearchRequests());
    }

    @Test
    public void testShowNewEntryWindowWithEditEntryDlgNotNull() {
        // Mock the EditorFrame instance
        EditorFrame mockEditorFrame = mock(EditorFrame.class);
        // Set the mock EditorFrame instance to the ZettelkastenView
        zettelkastenView.setEditorFrame(mockEditorFrame);

        // Call the method under test
        zettelkastenView.showNewEntryWindow();

        // Verify that editEntryDlg methods are called
        verify(mockEditorFrame).setAlwaysOnTop(true);
        verify(mockEditorFrame).toFront();
        verify(mockEditorFrame).requestFocus();
        verify(mockEditorFrame).setAlwaysOnTop(false);
        // Ensure that newEntry is not called
        verify(zettelkastenView, never()).newEntry();
    }

    @Test
    public void testShowNewEntryWindowWithEditEntryDlgNull() {
        // Call the method under test when editEntryDlg is null
        zettelkastenView.showNewEntryWindow();

        // Ensure that newEntry is called when editEntryDlg is null
        verify(zettelkastenView).newEntry();
    }

    @Test
    public void testOpenSettingsWindow() {
        // Mocking dependencies
        CSettingsDlg settingsDlg = mock(CSettingsDlg.class);
        ZettelkastenApp application = mock(ZettelkastenApp.class);
        JFrame frame = new JFrame(); // Create a JFrame for the settings window
        when(zettelkastenView.getFrame()).thenReturn(frame);
        when(ZettelkastenApp.getApplication()).thenReturn(application);

        // Use doAnswer for void method
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments(); // Get the arguments passed to the method
            JFrame frameArg = (JFrame) args[0]; // Cast the argument to JFrame
            assertEquals(frame, frameArg); // Verify that the correct JFrame is passed
            return settingsDlg; // Return the mocked settings dialog
        }).when(application).show(any(JFrame.class)); // Mock the show method with any JFrame argument

        // Triggering the method
        zettelkastenView.settingsWindow();

        // Verifying if the settings window is opened
        verify(application, times(1)).show(any(JFrame.class)); // Verify that the show method is called once with any JFrame argument
    }

    @Test
    public void testSynonymModification() {
        // Mocking dependencies
        Daten daten = mock(Daten.class);

        // Mock the behavior of Daten
        when(daten.isKeywordlistUpToDate()).thenReturn(true); // Assuming keywordlist is up-to-date

        // Triggering the method
        zettelkastenView.settingsWindow();

        // Verifying if backup indicator is updated and save is enabled
        verify(zettelkastenView, times(1)).backupNecessary(true);
        verify(zettelkastenView, times(1)).setSaveEnabled(true);

        // Verifying if tabbed pane is updated if keyword list is not up to date
        verify(daten, times(1)).isKeywordlistUpToDate();
        verify(zettelkastenView, times(0)).updateDisplay(); // No need to update display if keyword list is up-to-date
    }

}
