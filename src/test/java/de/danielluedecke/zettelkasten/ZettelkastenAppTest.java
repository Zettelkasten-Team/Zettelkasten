package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ZettelkastenAppTest {

    private ZettelkastenApp zettelkastenApp;
    private Logger loggerMock;

    @BeforeEach
    void setUp() {
        zettelkastenApp = new ZettelkastenApp(); // Initialize the ZettelkastenApp object
        loggerMock = mock(Logger.class); // Initialize the logger mock
        Settings settingsMock = mock(Settings.class);
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
    void showMainWindow_ShouldCreateZettelkastenViewWithDependencies() throws Exception {


        zettelkastenApp.showMainWindow();

        // Assert
        verify(zettelkastenApp).show(any(ZettelkastenView.class));
    }
}

