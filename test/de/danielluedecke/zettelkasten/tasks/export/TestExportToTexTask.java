package de.danielluedecke.zettelkasten.tasks.export;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.TestObjectFactory;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;

public class TestExportToTexTask {

	private ExportToTexTask exportToTexTask;
	private Daten daten;

	@Before
	public void initialize() throws Exception {
		JDialog parent = null;
		Application app = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class);
		JLabel label = new JLabel();
		TasksData td = null;
		AcceleratorKeys ak = new AcceleratorKeys();
		Settings st = TestObjectFactory.ZKN3Settings.ZKN3_TRICKY_MARKDOWN.settings;
		AutoKorrektur ac = null;
		Synonyms sy = new Synonyms();
		StenoData stn = null;
		Daten d = new Daten(new ZettelkastenView(new SingleFrameApplication() {
			@Override
			protected void startup() {
			}
		}, st, ak, ac, sy, stn, td), null, null, null);
		DesktopData dt = null;
		File fp = null;
		BibTex bto = null;
		ArrayList<Object> ee = null;
		int type = 0;
		int part = 0;
		DefaultMutableTreeNode n = null;
		boolean bibtex = false;
		boolean ihv = false;
		boolean numberprefix = false;
		boolean contenttable = false;
		exportToTexTask = new ExportToTexTask(app, parent, label, td, d, dt,
				st, bto, fp, ee, type, part, n, bibtex, ihv, numberprefix,
				contenttable);

		daten = TestObjectFactory
				.getDaten(TestObjectFactory.ZKN3Settings.ZKN3_TRICKY_MARKDOWN);
	}

	@Test
	public void testBugMarkdownZitatWirdNichtKorrektNachLatexExportiert()
			throws Exception {

		String brokenExportString = daten.getZettelContent(1);

		System.out.println(getConvertedTex(brokenExportString));

	}

	/**
	 * Helper method to invoke the private method "getConvertedTex"
	 */
	private String getConvertedTex(String input) throws Exception {
		Class c = exportToTexTask.getClass();

		Method method = c.getDeclaredMethod("getConvertedTex", String.class);
		method.setAccessible(true);
		return (String) method.invoke(exportToTexTask, input);
	}
}
