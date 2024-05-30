package de.danielluedecke.zettelkasten.config;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdesktop.application.SingleFrameApplication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;
import java.io.IOException;

public class ZettelkastenViewTest {
    private ZettelkastenView zettelkastenView;
    private Daten mockData;
    private Settings mockSettings;
    private TasksData mockTasksData;
    private SingleFrameApplication mockApp;

    @BeforeMethod
    public void setUp() {
        mockData = mock(Daten.class);
        mockSettings = mock(Settings.class);
        mockTasksData = mock(TasksData.class);
        mockApp = mock(SingleFrameApplication.class);

        zettelkastenView = new ZettelkastenView(mockApp, mockSettings, mockTasksData);
        // Assuming there's a setter or a way to inject mockData into ZettelkastenView
        zettelkastenView.setData(mockData);
    }

    @Test
    public void testHistoryBack_whenCanGoBackInHistory() {
        // Arrange
        when(mockData.canGoBackInHistory()).thenReturn(true);

        // Act
        zettelkastenView.historyBack();

        // Assert
        verify(mockData).historyBack();
        assertEquals(zettelkastenView.getDisplayedZettel(), -1);
        // Assuming updateDisplay is a method that we can't directly test, verify its effect or that it was called
        verify(zettelkastenView, times(1)).updateDisplay();
    }

    @Test
    public void testHistoryBack_whenCannotGoBackInHistory() {
        // Arrange
        when(mockData.canGoBackInHistory()).thenReturn(false);

        // Act
        zettelkastenView.historyBack();

        // Assert
        verify(mockData, never()).historyBack();
        // No need to reset displayedZettel or call updateDisplay
        assertNotEquals(zettelkastenView.getDisplayedZettel(), -1);
        verify(zettelkastenView, never()).updateDisplay();
    }
}
