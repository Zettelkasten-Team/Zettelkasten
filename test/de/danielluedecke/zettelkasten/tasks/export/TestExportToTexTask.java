package de.danielluedecke.zettelkasten.tasks.export;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.application.Application;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.danielluedecke.zettelkasten.TestObjectFactory;
import de.danielluedecke.zettelkasten.TestObjectFactory.ZKN3Settings;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;

public class TestExportToTexTask {

	private ExportToTexTask exportToTexTask;
	private Daten daten;
	private ZKN3Settings settings;

	@Before
	public void initialize() throws Exception {
		settings = TestObjectFactory.ZKN3Settings.ZKN3_TRICKY_MARKDOWN;
		daten = TestObjectFactory.getDaten(settings);

		JDialog parent = null;
		Application app = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class);
		JLabel label = new JLabel();
		TasksData td = null;
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

		exportToTexTask = new ExportToTexTask(app, parent, label, td, daten,
				dt, settings.settings, bto, fp, ee, type, part, n, bibtex, ihv,
				numberprefix, contenttable);
	}

	@Test
	public void testBugMarkdownZitatWirdNichtKorrektNachLatexExportiert()
			throws Exception {
		String brokenExportString = daten.getZettelContent(1);

		// daten.get
		System.out.println("String before tex convertion: "
				+ brokenExportString);
		/*
		 * It seems that the current implementation of
		 * ExportToTexTask.convertedTex() does *not* convert any quotation tags
		 * at all, neither in Markdown nor in UBB syntax:
		 */
		String convertedTex = getConvertedTex(brokenExportString);
		System.out.println("String after getConvertedTex: " + convertedTex);

		int exporttype = 13;
		StringBuilder exportPage = new StringBuilder(
				HtmlUbbUtil.convertUbbToTex(settings.settings, daten, new BibTex(null,
						settings.settings), convertedTex,
						settings.settings.getLatexExportFootnoteRef(),
						settings.settings.getLatexExportCreateFormTags(),
						Constants.EXP_TYPE_DESKTOP_TEX == exporttype,
						settings.settings.getLatexExportRemoveNonStandardTags()));

		String latexPage = exportPage.toString();
		
		System.out.println("exportPage after entire LaTex-Convertion: \n\n" + latexPage);
		
		assertFalse("Converted string still contains quotation tags",
				convertedTex.contains("[q]"));
		assertFalse("Converted string still contains quotation tags",
				convertedTex.contains("[/q]"));
		assertFalse("Converted string still contains quotation tags",
				convertedTex.contains(">"));
	}

	@Test
	public void testMarkdownQuotationBecomesLaTeXRangle() throws Exception {
		String inputString = daten.getZettelContent(1);

		/*
		 * It seems that convertSpecialChars() does not respect Markdown
		 * quotations: ">" is escaped into "\rangle"
		 */

		System.out.println("String before tex convertion: " + inputString);
		String convertedTex = getConvertedTex(inputString);
		System.out.println("String after tex convertion:  " + convertedTex);

		/*
		 * After solving this issue, the following assertions should AFAIK not
		 * fail:
		 */
		assertFalse("Quoted string is falsely introduced by \\rangle",
				convertedTex.contains("\\rangle"));
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
