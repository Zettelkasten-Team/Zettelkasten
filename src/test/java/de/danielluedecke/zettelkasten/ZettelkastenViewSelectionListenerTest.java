package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdesktop.application.SingleFrameApplication;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import static org.mockito.Mockito.*;

public class ZettelkastenViewSelectionListenerTest {

    @Mock
    private JTable authorsTable;

    @Mock
    private JTable titlesTable;

    @Mock
    private JTable bookmarksTable;

    @Mock
    private ListSelectionEvent selectionEvent;

    @Mock
    private Settings mockSettings;

    @Mock
    private TasksData mockTasksData;

    @Mock
    private SingleFrameApplication app;

    private ZettelkastenView outerInstance;
    private ZettelkastenView.SelectionListener listener;

    @BeforeMethod
    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Initialize the outer class instance with the mock application context and other mocked parameters
        outerInstance = new ZettelkastenView(app, mockSettings, mockTasksData);

        // Manually invoke the initComponents() method to initialize UI components
        outerInstance.initComponents();

        // Manually instantiate the inner class with the outer class instance
        listener = outerInstance.new SelectionListener(bookmarksTable);
    }

    @Test
    public void testValueChangedWithAuthorsTable() {
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);
        when(authorsTable.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getMinSelectionIndex()).thenReturn(1);

        when(selectionEvent.getSource()).thenReturn(authorsTable);
        listener.valueChanged(selectionEvent);

        verify(authorsTable, times(1)).getSelectionModel();
        verify(selectionModel, times(1)).getMinSelectionIndex();
    }

    @Test
    public void testValueChangedWithTitlesTable() {
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);
        when(titlesTable.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getMinSelectionIndex()).thenReturn(1);

        when(selectionEvent.getSource()).thenReturn(titlesTable);
        listener.valueChanged(selectionEvent);

        verify(titlesTable, times(1)).getSelectionModel();
        verify(selectionModel, times(1)).getMinSelectionIndex();
    }

    @Test
    public void testValueChangedWithBookmarksTable() {
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);
        when(bookmarksTable.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getMinSelectionIndex()).thenReturn(1);

        when(selectionEvent.getSource()).thenReturn(bookmarksTable);
        listener.valueChanged(selectionEvent);

        verify(bookmarksTable, times(1)).getSelectionModel();
        verify(selectionModel, times(1)).getMinSelectionIndex();
    }
}
