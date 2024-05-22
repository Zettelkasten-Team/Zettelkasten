package de.danielluedecke.zettelkasten.tasks;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

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
		ZettelkastenView zknFrame = new ZettelkastenView(app, settingsObj, tasksData);

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
		autoBackupTask = new AutoBackupTask(app, zknFrame, statusMsgLabel, dataObj, desktopObj, settingsObj, searchObj,
				synonymsObj, bookmarksObj, bibtexObj);

	}
	
	@Test
	public void testPerformBackup() throws IOException {
		String pathname = "/tmp/backup.txt";
		File backupFile = new File(pathname);
		autoBackupTask.performBackup(backupFile);
	}
}
