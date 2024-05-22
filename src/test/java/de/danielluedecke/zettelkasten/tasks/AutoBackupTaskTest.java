package de.danielluedecke.zettelkasten.tasks;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;

public class AutoBackupTaskTest {

	private AutoBackupTask autoBackupTask;

	@Before
	public void setUp() throws Exception {
		// Setup dependencies

		// Mock dependencies
        SingleFrameApplication app = mock(SingleFrameApplication.class);
        ApplicationContext context = mock(ApplicationContext.class);

		TasksData tasksData = new TasksData();

		// settingsObj
		Settings settingsObj = new Settings();

		// zknFrame
		ZettelkastenView zknFrame = mock(ZettelkastenView.class); // Mock ZettelkastenView

		// statusMsgLabel
		JLabel statusMsgLabel = mock(JLabel.class);

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
		
		// Mock behavior of defaultResourceMap
	    ResourceMap resourceMap = mock(ResourceMap.class);
	    when(context.getResourceMap()).thenReturn(resourceMap);
	    when(app.getContext()).thenReturn(context);

		// Instantiate AutoBackupTask
		autoBackupTask = new AutoBackupTask(app, zknFrame, statusMsgLabel, dataObj, desktopObj, settingsObj, searchObj,
				synonymsObj, bookmarksObj, bibtexObj);
		
		// Mock behavior of ZettelkastenView methods
	    doNothing().when(zknFrame).initComponents(); // Mock initComponents() to avoid NullPointerException

	}
	
	@Test
	public void testPerformBackup() throws IOException {
		String pathname = "/tmp/backup.txt";
		File backupFile = new File(pathname);
		autoBackupTask.performBackup(backupFile);
	}
}
