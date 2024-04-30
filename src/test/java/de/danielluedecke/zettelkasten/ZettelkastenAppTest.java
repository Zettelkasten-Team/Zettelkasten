package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.database.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        zettelkastenApp.configureLogger(loggerMock);

        // Act
        zettelkastenApp.startup();

        // Assert
        verify(loggerMock, times(2)).addHandler(any());
    }

    @Test
    void testShowMainWindow_NormalExecution() {
        // Test normal execution without any exceptions
        // Arrange
        ZettelkastenView mockView = mock(ZettelkastenView.class);
        Settings mockSettings = mock(Settings.class);
        TasksData mockTaskData = mock(TasksData.class);

        // Act
        ZettelkastenApp zettelkastenApp = new ZettelkastenApp();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            zettelkastenApp.showMainWindow();
        });

        // Add assertions based on the expected behavior
    }

}
