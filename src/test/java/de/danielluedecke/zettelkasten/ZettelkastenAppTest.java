package de.danielluedecke.zettelkasten;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.logging.Logger;
import static org.mockito.Mockito.*;

class ZettelkastenAppTest {

    private ZettelkastenApp zettelkastenApp;
    private Logger loggerMock;

    @BeforeEach
    void setUp() {
        zettelkastenApp = new ZettelkastenApp(); // Initialize the ZettelkastenApp object
        loggerMock = mock(Logger.class); // Initialize the logger mock
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
}
