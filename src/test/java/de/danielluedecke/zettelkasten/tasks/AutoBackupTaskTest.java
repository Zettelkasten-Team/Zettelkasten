package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.TestObjectFactory;
import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.swing.JLabel;
import java.io.File;
import java.io.IOException;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class AutoBackupTaskTest {

    private AutoBackupTask autoBackupTask;
    private ResourceMap rm;

    @Mock
    Application appSpy;

    //@Mock
    ZettelkastenView zkn;

    @Mock
    JLabel statusMsgLabel;

    Daten dataObj;

    @Mock
    DesktopData desktopObj;

    //@Mock
    Settings settingsObj;

    @Mock
    SearchRequests searchObj;

    //@Mock
    Synonyms synonymsObj;

    @Mock
    Bookmarks bookmarksObj;

    //@Mock
    BibTeX bibtexObj;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);

        // Mock the behavior of the ApplicationContext within the Application mock
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);

        // Create a spy instead of a mock for the Application instance
        appSpy = spy(ZettelkastenApp.class);

        // Stub the getContext() method on the spy
        doReturn(applicationContextMock).when(appSpy).getContext();

        rm = org.jdesktop.application.Application
                .getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
                .getResourceMap(ZettelkastenView.class);

        // Use TestObjectFactory to get Daten object
        dataObj = TestObjectFactory.getDaten(TestObjectFactory.ZKN3Settings.ZKN3_SAMPLE);

        zkn = dataObj.zknframe;

        settingsObj = dataObj.settings;
        bibtexObj = dataObj.bibtexObj;
        synonymsObj = dataObj.synonymsObj;

        // Initialize the autoBackupTask with the mocked application
        autoBackupTask = createAutoBackupTask();

        // Stubbing createBackupFile() to return a valid backup file
        File backupFileMock = mock(File.class);
        when(autoBackupTask.createBackupFile()).thenReturn(backupFileMock);

    }

    private AutoBackupTask createAutoBackupTask() {
        return new AutoBackupTask(appSpy, zkn, statusMsgLabel, dataObj, desktopObj, settingsObj, searchObj, synonymsObj,
                bookmarksObj, bibtexObj);
    }

    @Test
    public void testBackupErrorHandling() throws IOException {
        // Stubbing createBackupFile() to return a valid backup file
        File backupFileMock = mock(File.class);
        when(autoBackupTask.createBackupFile()).thenReturn(backupFileMock);

        // Stubbing performBackup() to throw an IOException
        IOException ioExceptionMock = mock(IOException.class);
        doThrow(ioExceptionMock).when(autoBackupTask).performBackup(backupFileMock);

        // Call the method being tested
        autoBackupTask.doInBackground();

        // Verify that handleBackupError() is called when IOException occurs during backup
        verify(autoBackupTask).handleBackupError(ioExceptionMock);
    }

    @Test
    public void testDoInBackground() throws Exception {
        // AutoBackupTask instance is already initialized in setUp method
        Object expResult = null;
        Object result = autoBackupTask.doInBackground();
        assert expResult == result;
    }

    @Test
    public void testSucceeded() {
        // AutoBackupTask instance is already initialized in setUp method
        Object result_2 = null;
        autoBackupTask.succeeded(result_2);
        // Add verification if necessary
    }

    @Test
    public void testFinished() {
        // AutoBackupTask instance is already initialized in setUp method
        autoBackupTask.finished();
        // Add verification if necessary
    }
}
