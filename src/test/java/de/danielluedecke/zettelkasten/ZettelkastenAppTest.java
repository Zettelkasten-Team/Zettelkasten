package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.database.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ZettelkastenAppTest {

    private ZettelkastenApp zettelkastenApp;
    private Logger loggerMock;

    @BeforeEach
    void setUp() {
        zettelkastenApp = new ZettelkastenApp();
        loggerMock = mock(Logger.class);
    }

    @Test
    void startup_ShouldInitializeLoggerAndSettings() {
        // Arrange
        TasksData tasksDataMock = mock(TasksData.class);
        Settings settingsMock = mock(Settings.class);
        doNothing().when(loggerMock).log(any(Level.class), anyString());
        when(settingsMock.getLanguage()).thenReturn("en");
        zettelkastenApp.initializeTaskData();
        zettelkastenApp.initializeSettings();

        // Act
        zettelkastenApp.startup();

        // Assert
        zettelkastenApp.initializeTaskData();
        assertNotNull(zettelkastenApp.initializeSettings());
        verify(loggerMock, atLeastOnce()).log(any(Level.class), anyString());
        verify(settingsMock).getLanguage();
    }
}
