package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ZettelkastenAppTest {

    private ZettelkastenApp zettelkastenApp;
    private Logger loggerMock;
    private Settings settingsMock;
    private TasksData tasksDataMock;

    @BeforeEach
    void setUp() {
        zettelkastenApp = new ZettelkastenApp();
        loggerMock = mock(Logger.class);
        settingsMock = mock(Settings.class); // Assign to class-level variable
        tasksDataMock = mock(TasksData.class);
        zettelkastenApp.configureLogger(loggerMock);
    }

    @Test
    void startup_ShouldInitializeLoggerAndSettings() {
        
        // Act
        zettelkastenApp.startup();

        // Assert
        verify(loggerMock, times(2)).addHandler(any());
    }

    @Test
    void showMainWindow_ShouldCreateZettelkastenViewWithDependencies() throws Exception {
        // Arrange
        Logger loggerMock = mock(Logger.class);
        zettelkastenApp = spy(new ZettelkastenApp());
        doNothing().when(zettelkastenApp).logStartingMainWindow(); // Mocking logStartingMainWindow method
        doNothing().when(zettelkastenApp).validateSettings(); // Mocking validateSettings method
        doNothing().when(zettelkastenApp).createMainWindow(); // Mocking createMainWindow method
        zettelkastenApp.configureLogger(loggerMock); // Injecting the mock logger

        // Act
        zettelkastenApp.showMainWindow();

        // Assert
        //verify(loggerMock).log(eq(Level.INFO), eq("Starting Main Window.")); // Verifying log message
        verify(zettelkastenApp).validateSettings(); // Verifying settings validation
        verify(zettelkastenApp).createMainWindow(); // Verifying main window creation
    }
    
}
