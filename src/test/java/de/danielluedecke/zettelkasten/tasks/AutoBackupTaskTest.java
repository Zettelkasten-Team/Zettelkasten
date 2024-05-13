package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.TestObjectFactory;
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
import org.jdesktop.application.SingleFrameApplication;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class AutoBackupTaskTest {

    private AutoBackupTask autoBackupTask;

    @Mock
    private SingleFrameApplication app;

    public ZettelkastenView zknframe;

    @Mock
    private JLabel statusMsgLabelMock;

    private Daten dataObj;

    public DesktopData desktop;

    public Settings settingsObj;

    public SearchRequests searchRequests;

    public Synonyms synonymsObj;

    public Bookmarks bookmarks;

    public BibTeX bibtexObj;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);

        // Use TestObjectFactory to get Daten object
        dataObj = TestObjectFactory.getDaten(TestObjectFactory.ZKN3Settings.ZKN3_SAMPLE);

        // Retrieve settings from Daten object
        settingsObj = dataObj.settings;

        zknframe = dataObj.zknframe;
        desktop = dataObj.zknframe.desktop;

        searchRequests = dataObj.zknframe.searchRequests;
        synonymsObj = dataObj.synonymsObj;
        bookmarks = dataObj.zknframe.bookmarks;
        bibtexObj = dataObj.bibtexObj;

        // Mock the behavior of SingleFrameApplication and ApplicationContext
        Application applicationMock = mock(Application.class);
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        when(applicationMock.getContext()).thenReturn(applicationContextMock);

        // Mock the behavior of getResourceMap() to return a valid ResourceMap
        ResourceMap resourceMapMock = mock(ResourceMap.class);
        when(applicationContextMock.getResourceMap(any(Class.class), any(Class.class))).thenReturn(resourceMapMock);

        // Initialize the autoBackupTask with the mocked application
        autoBackupTask = createAutoBackupTask();

    }

    private AutoBackupTask createAutoBackupTask() {
        return new AutoBackupTask(app, zknframe, statusMsgLabelMock, dataObj,
                desktop, settingsObj, searchRequests, synonymsObj, bookmarks, bibtexObj);
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
