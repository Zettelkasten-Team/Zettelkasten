package de.danielluedecke.zettelkasten;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;

import static org.testng.Assert.*;

import org.jdesktop.application.SingleFrameApplication;

public class ZettelkastenViewUpdateDisplayTest {

    private ZettelkastenView zettelkastenView;
    private Settings settings;
    private TasksData tasksData;
    private Daten data;

    @BeforeMethod
    public void setUp() throws Exception {
        settings = new Settings();
        tasksData = new TasksData();
        data = new Daten(null);
        SingleFrameApplication app = new SingleFrameApplication() {
            @Override
            protected void startup() {
            }
        };
        zettelkastenView = new ZettelkastenView(app, settings, tasksData);
        zettelkastenView.setData(data);
    }

    @Test
    public void testUpdateEntryPaneAndKeywordsPaneValidEntry() {
        int validEntryNumber = 1;
        data.addEntry(validEntryNumber, "Sample Entry");

        zettelkastenView.updateEntryPaneAndKeywordsPane(validEntryNumber);

        assertEquals(zettelkastenView.jEditorPaneEntry.getText(), "Sample Entry");
        assertFalse(zettelkastenView.keywordListModel.isEmpty());
        assertEquals(zettelkastenView.jTextFieldEntryNumber.getText(), String.valueOf(validEntryNumber));
    }

    @Test
    public void testUpdateEntryPaneAndKeywordsPaneInvalidEntry() {
        int invalidEntryNumber = 0;

        zettelkastenView.updateEntryPaneAndKeywordsPane(invalidEntryNumber);

        assertEquals(zettelkastenView.jEditorPaneEntry.getText(), "");
        assertTrue(zettelkastenView.keywordListModel.isEmpty());
        assertEquals(zettelkastenView.jTextFieldEntryNumber.getText(), "");
        assertEquals(zettelkastenView.statusOfEntryLabel.getText(), 
                     zettelkastenView.getResourceMap().getString("entryOfText"));
    }

    @Test
    public void testUpdateEntryPaneAndKeywordsPaneAddToHistory() {
        settings.setAddAllToHistory(true);
        int entryNumber = 1;
        data.addEntry(entryNumber, "Sample Entry");

        zettelkastenView.updateEntryPaneAndKeywordsPane(entryNumber);

        assertTrue(data.isInHistory(entryNumber));
        assertTrue(zettelkastenView.buttonHistoryBack.isEnabled());
        assertFalse(zettelkastenView.buttonHistoryForward.isEnabled());
    }

    @Test
    public void testHistoryNavigationButtonsState() {
        settings.setAddAllToHistory(true);
        int firstEntry = 1;
        int secondEntry = 2;
        data.addEntry(firstEntry, "First Entry");
        data.addEntry(secondEntry, "Second Entry");

        zettelkastenView.updateEntryPaneAndKeywordsPane(firstEntry);
        zettelkastenView.updateEntryPaneAndKeywordsPane(secondEntry);

        assertTrue(data.canHistoryBack());
        assertFalse(data.canHistoryForward());

        zettelkastenView.buttonHistoryBack.doClick();

        assertTrue(data.canHistoryForward());
        assertEquals(zettelkastenView.jEditorPaneEntry.getText(), "First Entry");
    }
}
