/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten.tasks.importtasks;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Tools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Luedeke
 */
public class ImportFromCSV extends org.jdesktop.application.Task<Object, Void> {
	/**
	 * Reference to the Daten object, which contains the XML data of the
	 * Zettelkasten. will be passed as parameter in the constructor, see below
	 */
	private final Daten dataObj;
	/**
	 *
	 */
	private final TasksData taskinfo;
	/**
	 * Reference to the Bookmarks object, which contains the XML data of the
	 * bookmarks. will be passed as parameter in the constructor, see below
	 */
	private final Bookmarks bookmarksObj;
	/**
	 * Reference to the Settings object, which contains the settings like fike paths
	 * etc...
	 */
	private final Settings settingsObj;
	/**
	 * Reference to the DesktopData object, which contains the XML data of the
	 * desktop. will be passed as parameter in the constructor, see below
	 */
	private final DesktopData desktopObj;
	/**
	 * SearchRequests object, which contains the XML data of the searchrequests and
	 * -result that are related with this data file
	 */
	private final SearchRequests searchrequestsObj;
	/**
	 * file path to import file
	 */
	private final File filepath;
	/**
	 * indicates whether the data should be appended to an already opened
	 * zettelkasten or whether the old zettelkasten-data-file should be closed (and
	 * saved) before and a new data-file should be created from the imported data
	 */
	private final boolean append;
	private final char separatorchar;
	/**
	 * A default timestamp for importing old datafiles. Sometimes entries of old
	 * data files may not contain timestamps. so we can insert a default value
	 * here...
	 */
	private String defaulttimestamp;
	/**
	 *
	 */
	private final StringBuilder importedTypesMessage = new StringBuilder("");
	private final javax.swing.JDialog parentDialog;
	private final javax.swing.JLabel msgLabel;

	/**
	 * get the strings for file descriptions from the resource map
	 */
	private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
			.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
			.getResourceMap(ImportTask.class);

	/**
	 * 
	 * @param app
	 * @param parent
	 * @param label
	 * @param td
	 * @param d
	 * @param bm
	 * @param dt
	 * @param sr
	 * @param s
	 * @param fp
	 * @param sepchar
	 * @param a2u
	 * @param appendit
	 * @param dts
	 */
	public ImportFromCSV(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
			TasksData td, Daten d, Bookmarks bm, DesktopData dt, SearchRequests sr, Settings s, File fp, String sepchar,
			boolean a2u, boolean appendit, String dts) {
		super(app);
		// init of the variable either passed as parameters or initiated the first time
		dataObj = d;
		taskinfo = td;
		bookmarksObj = bm;
		desktopObj = dt;
		searchrequestsObj = sr;
		settingsObj = s;
		parentDialog = parent;
		msgLabel = label;

		filepath = fp;
		append = appendit;
		defaulttimestamp = dts;
		separatorchar = sepchar.toCharArray()[1];

		if (null == defaulttimestamp) {
			defaulttimestamp = Tools.getTimeStamp();
		}
		taskinfo.setImportOk(true);
		new Document(new Element("zettelkasten"));
		// set default import message
		msgLabel.setText(resourceMap.getString("importDlgMsgImport"));
	}

	@Override
	protected Object doInBackground() throws CsvException {
		// Your Task's code here. This method runs
		// on a background thread, so don't reference
		// the Swing GUI from here.

		// return value that indicates that an error occurred
		taskinfo.setImportOk(false);
		try {
			final CSVParser parser = new CSVParserBuilder().withSeparator(separatorchar).withQuoteChar('\"').build();
			final CSVReader csvreader = new CSVReaderBuilder(new FileReader(filepath)).withCSVParser(parser).build();
			csvreader.readAll();
		} catch (FileNotFoundException ex) {
			// display error message box
			JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgFileNotFound", filepath),
					resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
			// leave thread
			return null;
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
			return null;
		}
		// init variables
		Document zkndoc = null;
		// if we don't want to append the data, reset the zettelkastem
		if (!append) {
			resetDataFiles();
		}
		// now that all data is converted into the variable zkndoc...
		if (!append) {
			// set this document as main zkn data...
			dataObj.setZknData(zkndoc);
		} // resp. append the zkndoc to the maindata
		else {
			// append imported entries.
			dataObj.appendZknData(zkndoc);
		}
		return null; // return your result
	}

	@Override
	protected void succeeded(Object result) {
		// Runs on the EDT. Update the GUI based on
		// the result computed by doInBackground().

		// after importing, the data file is modified, so the user does not
		// forget to save the data in the new fileformat.
		if (taskinfo.isImportOk()) {
			dataObj.setModified(true);
		}
	}

	@Override
	protected void finished() {
		super.finished();
		taskinfo.setImportMessage(importedTypesMessage.toString());
		// Close Window
		parentDialog.setVisible(false);
		parentDialog.dispose();
	}

	private void resetDataFiles() {
		// reset the data-files
		if (!append) {
			Constants.zknlogger.log(Level.INFO,
					"Setting data file to none during import from csv.");
			settingsObj.setMainDataFile(new File(""));
			dataObj.reset();
			desktopObj.clear();
			bookmarksObj.clear();
			searchrequestsObj.clear();
		}
	}
}
