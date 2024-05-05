package de.danielluedecke.zettelkasten;

import static org.junit.jupiter.api.Assertions.*;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class ZettelkastenViewTest {
    @Mock
    private SingleFrameApplication mockApp;

    @Mock
    private Settings mockSettings;

    @Mock
    private TasksData mockTaskData;

    private ZettelkastenView zettelkastenView;

    @BeforeEach
    public void setUp() throws ClassNotFoundException,
            UnsupportedLookAndFeelException, InstantiationException,
            IllegalAccessException, IOException {
        // Initialize ZettelkastenView with mocked dependencies
        zettelkastenView = new ZettelkastenView(mockApp, mockSettings, mockTaskData);
    }

    @Test
    public void testShowNewEntryWindow() {
        // Call the showNewEntryWindow method
        zettelkastenView.showNewEntryWindow();

        // Assert that the editEntryDlg is not null after calling the method
        assertNotNull(zettelkastenView.editEntryDlg, "Edit dialog should not be null after calling showNewEntryWindow");
    }
}

