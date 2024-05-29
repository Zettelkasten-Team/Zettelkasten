package de.danielluedecke.zettelkasten;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;

import org.jdesktop.application.SingleFrameApplication;

public class ZettelkastenViewTest {

    @Mock
    private Settings settings;

    @Mock
    private TasksData tasksData;

    private JTable jTableBookmarks;
    private ZettelkastenView instance;

    @BeforeMethod
    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Mock SingleFrameApplication
        SingleFrameApplication app = mock(SingleFrameApplication.class);
        JFrame frame = mock(JFrame.class);
        when(app.getMainFrame()).thenReturn(frame);
        
        // Use real JRootPane and JPanel for frame
        when(frame.getRootPane()).thenReturn(new JRootPane());
        when(frame.getContentPane()).thenReturn(new JPanel());
        
        // Mock the components used in ZettelkastenView
        JPanel mainPanel = mock(JPanel.class);
        JMenuBar menuBar = mock(JMenuBar.class);
        JPanel statusPanel = mock(JPanel.class);
        JToolBar toolBar = mock(JToolBar.class);

        // Initialize the ZettelkastenView with mock settings and tasksData
        instance = new ZettelkastenView(app, settings, tasksData) {
            @Override
			public void initComponents() {
                setComponent(mainPanel);
                setMenuBar(menuBar);
                setStatusBar(statusPanel);
                setToolBar(toolBar);
            }
        };

        // Set up the table model and the table
        DefaultTableModel tableModel = new DefaultTableModel(new Object[][] {
            {"Bookmark 1"},
            {"Bookmark 2"},
            {"Bookmark 3"}
        }, new String[] {"Bookmarks"});
        
        jTableBookmarks = new JTable(tableModel);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);
        jTableBookmarks.setSelectionModel(selectionModel);
    }

    @Test
    public void testSelectionListener_BookmarkSelected() {
        // Set up the selection listener
        ZettelkastenView.SelectionListener selectionListener = instance.new SelectionListener(jTableBookmarks);

        // Create a mock ListSelectionEvent
        ListSelectionEvent event = mock(ListSelectionEvent.class);
        when(event.getSource()).thenReturn(jTableBookmarks.getSelectionModel());

        // Simulate selecting a row
        jTableBookmarks.getSelectionModel().setSelectionInterval(1, 1);

        // Invoke the valueChanged method
        selectionListener.valueChanged(event);

        // Verify that the selected row is processed correctly
        verify(jTableBookmarks, times(1)).getSelectedRow();
        verify(jTableBookmarks, times(1)).convertRowIndexToModel(1);
        
        // Add additional verifications based on your implementation
    }
}
