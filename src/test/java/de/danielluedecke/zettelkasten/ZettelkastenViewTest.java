package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class ZettelkastenViewTest {

    private ZettelkastenView zettelkastenView;

    @BeforeEach
    public void setUp() throws UnsupportedLookAndFeelException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // Initialize mock objects manually
        SingleFrameApplication mockApp = mock(SingleFrameApplication.class);
        Settings mockSettings = mock(Settings.class);
        TasksData mockTasksData = mock(TasksData.class);
        EditorFrame mockEditorFrame = mock(EditorFrame.class);

        // Create ZettelkastenView instance with mock dependencies
        zettelkastenView = new ZettelkastenView(mockApp, mockSettings, mockTasksData);
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
}

