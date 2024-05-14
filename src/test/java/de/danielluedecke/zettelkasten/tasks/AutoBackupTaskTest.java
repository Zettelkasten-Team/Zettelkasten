package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;
import javax.swing.JLabel;
import org.jdesktop.application.SingleFrameApplication;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class AutoBackupTaskTest {

    private AutoBackupTask autoBackupTask;

    @Before
    public void setUp() throws Exception {
        // Setup dependencies        

        // app
        SingleFrameApplication app = new SingleFrameApplication() {
            @Override
            protected void startup() {

            }
        };

        TasksData tasksData = new TasksData();

        // settingsObj
        Settings settingsObj = new Settings();

        // zknFrame
        ZettelkastenView zknFrame = new ZettelkastenView(
                app,
                settingsObj,
                tasksData);

        // statusMsgLabel
        JLabel statusMsgLabel = new JLabel();

        // bibtexObj
        BibTeX bibtexObj = new BibTeX(zknFrame, settingsObj);

        // synonymsObj
        Synonyms synonymsObj = new Synonyms();

        // dataObj
        Daten dataObj = new Daten(zknFrame, settingsObj, synonymsObj, bibtexObj);

        // desktopObj
        DesktopData desktopObj = new DesktopData(zknFrame);

        // searchObj
        SearchRequests searchObj = new SearchRequests(zknFrame);

        // bookmarksObj
        Bookmarks bookmarksObj = new Bookmarks(zknFrame, settingsObj);

        // Instantiate AutoBackupTask 
        autoBackupTask = new AutoBackupTask(app, zknFrame, statusMsgLabel,
                dataObj, desktopObj,
                settingsObj, searchObj,
                synonymsObj, bookmarksObj,
                bibtexObj);

    }

}
