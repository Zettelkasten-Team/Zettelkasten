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
        data = new Daten(zettelkastenView, settings, null, null);
        SingleFrameApplication app = new SingleFrameApplication() {
            @Override
            protected void startup() {
            }
        };
        zettelkastenView = new ZettelkastenView(app, settings, tasksData);
        zettelkastenView.setData(data);
    }
    
    @Test
    public void testUpdateEntryPaneAndKeywordsPaneInvalidEntry() {
        int invalidEntryNumber = 0;

        zettelkastenView.updateEntryPaneAndKeywordsPane(invalidEntryNumber);

        assertTrue(zettelkastenView.jEditorPaneEntry.getText().contains("<body>"));
        assertTrue(zettelkastenView.keywordListModel.isEmpty());
        assertEquals(zettelkastenView.jTextFieldEntryNumber.getText(), "");
        assertEquals(zettelkastenView.statusOfEntryLabel.getText(), 
                     zettelkastenView.getResourceMap().getString("entryOfText"));
    }
}