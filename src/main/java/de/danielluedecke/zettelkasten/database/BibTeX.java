/*
 * Zettelkasten - nach Luhmann
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://zettelkasten.danielluedecke.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.danielluedecke.zettelkasten.database;

import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;
import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * This class is responsible for managing bibtex files.<br>
 * <br>
 * Usually, first of all a file must be opened ("attached") using the
 * {@link #openAttachedFile(java.lang.String)
 * openAttachedFile(java.lang.String)} method. after that, all entries are
 * stored in the variable {@link #bibtexfile bibtexfile}, while the single
 * bibtex entries are stored in {@link #attachedbibtexentries}. <br>
 * <br>
 * With this class, you can then retrieve single entries, retrieve bibtex
 * entries (i.e. author values) in a certain citation style etc. <br>
 * <br>
 * This class is mainly used for importing literature values from a bibtex file
 * (see <b>CImportBibTex</b>) or changing bibkey values from entry's author
 * values (see <b>CSetBibKey</b>).
 *
 * @author danielludecke
 */
public class BibTeX {

	/**
	 * A reference to the settings-class (CSettings)
	 */
	private final Settings settingsObj;
	/**
	 * The main variable that stors the currently opened bibtex-file
	 */
	private BibtexFile bibtexfile = new BibtexFile();
	/**
	 * This array stores all single entries from the attached bibtex file
	 * {@code bibtexfile}.
	 */
	private final ArrayList<BibtexEntry> attachedbibtexentries = new ArrayList<>();
	/**
	 * This array stores all single entries from the attached bibtex file
	 * {@code bibtexfile}.
	 */
	private final ArrayList<BibtexEntry> bibtexentries = new ArrayList<>();
	/**
	 * This array stores bibtex entries that should be exported. Since bibtex
	 * entries that should be exported may contain only a selection of all bibtex
	 * entries of the currently opened bibtex file, we use an extra array to store
	 * export entries.
	 */
	private final ArrayList<BibtexEntry> outputbibtexentries = new ArrayList<>();
	/**
	 * Stores the file path to the currently opened bibtex file.
	 */
	private File currentlyattachedfile = null;
	/**
	 * Stores the <b>general</b> citation style which is used as Zettelkasten
	 * default.<br>
	 * <br>
	 * Used when the user requests a formatted bibtex entry via
	 * {@link #getFormattedEntryFromAttachedFile(int)
	 * getFormattedEntryFromAttachedFile()}. The bibtex entry (i.e. author value) is
	 * formatted according to the selected citation style.
	 */
	private final List<Map<String, String>> importtypes = new ArrayList<>();
	/**
	 * Stores the <b>CBE</b> citation style.<br>
	 * <br>
	 * Used when the user requests a formatted bibtex entry via
	 * {@link #getFormattedEntryFromAttachedFile(int)
	 * getFormattedEntryFromAttachedFile()}. The bibtex entry (i.e. author value) is
	 * formatted according to the selected citation style.
	 */
	private final List<Map<String, String>> importtypesCBE = new ArrayList<>();
	/**
	 * Stores the <b>APA</b> citation style.<br>
	 * <br>
	 * Used when the user requests a formatted bibtex entry via
	 * {@link #getFormattedEntryFromAttachedFile(int)
	 * getFormattedEntryFromAttachedFile()}. The bibtex entry (i.e. author value) is
	 * formatted according to the selected citation style.
	 */
	private final List<Map<String, String>> importtypesAPA = new ArrayList<>();
	/**
	 * A variable indicating which citation-style is used when requesting a
	 * formatted bibtex-entry (see {@link #getFormattedEntry(bibtex.dom.BibtexEntry)
	 * getFormattedEntryFromAttachedFile()}).
	 */
	private int citestyle = Constants.BIBTEX_CITE_STYLE_GENERAL;
	private boolean modified;
	private final String editorToken = "°###°";
	/**
	 * Reference to the main frame.
	 */
	private final ZettelkastenView zknframe;
	/**
	 * get the strings for file descriptions from the resource map
	 */
	private final static org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
			.getInstance(ZettelkastenApp.class).getContext().getResourceMap(ZettelkastenView.class);

	public BibTeX(ZettelkastenView zkn, Settings s) {
		zknframe = zkn;
		settingsObj = s;
		modified = false;
		initStyles();
	}

	/**
	 * Set the change status (modified state). Should be called whenever changes
	 * have been made to the desktop file (set to true) or when the data has been
	 * saved (set to false).
	 *
	 * @param m true when changes are unsaved, false otherwise
	 */
	public void setModified(boolean m) {
		modified = m;
		zknframe.setBackupNecessary();
	}

	/**
	 * Check if the data file has been changed (modified)
	 *
	 * @return {@code true} if it is modified, false otherwise
	 */
	public boolean isModified() {
		return modified;
	}

	private void initStyles() {
		initStyleGeneral();
		initStyleAPA();
		initStyleCBE();
	}

	private void initStyleGeneral() {
		/*
		 * Here we start with the styles for default-values (non-defined).
		 */
		Map<String, String> importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("series", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
		/*
		 * Here we start with the styles for books.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
		/*
		 * Here we start with the styles for articles.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("journal", " *");
		importstyles.put("journaltitle", " *");
		importstyles.put("volume", ", *");
		importstyles.put("number", "(*)");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
		/*
		 * Here we start with the styles for incollections.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("booktitle", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
		/*
		 * Here we start with the styles for inbooks.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("booktitle", ": *.");
		importstyles.put("chapter", " (Kapitel *)");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
		/*
		 * Here we start with the styles for misc-values.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("howpublished", " [*]");
		importstyles.put("url", " [*]");
		importtypes.add(importstyles);
	}

	private void initStyleCBE() {
		/*
		 * Here we start with the styles for default-values (non-defined).
		 */
		Map<String, String> importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("editor", " In: *, editors.");
		importstyles.put("series", " *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ". p. *.");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
		/*
		 * Here we start with the styles for books.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *.");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
		/*
		 * Here we start with the styles for articles.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("journal", " *");
		importstyles.put("journaltitle", " *");
		importstyles.put("volume", " *");
		importstyles.put("number", "(*)");
		importstyles.put("pages", ":*.");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
		/*
		 * Here we start with the styles for incollections.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("editor", " In: *, editors.");
		importstyles.put("booktitle", " *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ". p. *.");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
		/*
		 * Here we start with the styles for inbooks.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("editor", " In: *, editors.");
		importstyles.put("booktitle", " *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ". p. *.");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
		/*
		 * Here we start with the styles for misc.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*.");
		importstyles.put("year", " *.");
		importstyles.put("title", " *.");
		importstyles.put("howpublished", " [*]");
		importstyles.put("url", " [*]");
		importtypesCBE.add(importstyles);
	}

	private void initStyleAPA() {
		/*
		 * Here we start with the styles for default-values (non-defined).
		 */
		Map<String, String> importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("series", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
		/*
		 * Here we start with the styles for books.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
		/*
		 * Here we start with the styles for articles.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("journal", " *");
		importstyles.put("journaltitle", " *");
		importstyles.put("volume", ", *");
		importstyles.put("number", "(*)");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
		/*
		 * Here we start with the styles for incollections.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("booktitle", ": *.");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
		/*
		 * Here we start with the styles for inbooks.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("editor", " In * (Hrsg.)");
		importstyles.put("booktitle", ": *.");
		importstyles.put("chapter", " (Kapitel *)");
		importstyles.put("address", " *");
		importstyles.put("publisher", ": *");
		importstyles.put("pages", ", *");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
		/*
		 * Here we start with the styles for misc.
		 */
		importstyles = new LinkedHashMap<>();
		importstyles.put("author", "*");
		importstyles.put("year", " (*)");
		importstyles.put("title", ": *.");
		importstyles.put("howpublished", " [*]");
		importstyles.put("url", " [*]");
		importtypesAPA.add(importstyles);
	}

	/**
	 * Stores the style which should be used when e.g. importing authors into the
	 * database.
	 *
	 * @param style the style. use following constants:<br>
	 *              {@code BIBTEX_CITE_STYLE_GENERAL}<br>
	 *              {@code BIBTEX_CITE_STYLE_CBE}<br>
	 *              {@code BIBTEX_CITE_STYLE_APA}
	 */
	public void setCiteStyle(int style) {
		citestyle = style;
	}

	/**
	 * Retrieves the style which should be used when e.g. importing authors into the
	 * database.
	 *
	 * @return one of the following values:<br>
	 *         {@code BIBTEX_CITE_STYLE_GENERAL}<br>
	 *         {@code BIBTEX_CITE_STYLE_CBE}<br>
	 *         {@code BIBTEX_CITE_STYLE_APA}
	 */
	public int getCiteStyle() {
		return citestyle;
	}

	/**
	 * Sets the filepath to the latest used bibtex-file.
	 *
	 * @param fp filepath to the current bibtex-file
	 */
	public void setFilePath(File fp) {
		settingsObj.setLastUsedBibTexFile(fp.toString());
	}

	/**
	 *
	 * @param be
	 * @return
	 */
	public int setEntries(ArrayList<BibtexEntry> be) {
		int totalcount = 0;
		if (be != null) {
			bibtexentries.clear();
			totalcount = addEntries(be);
		}
		return totalcount;
	}

	/**
	 *
	 * @param be
	 * @return
	 */
	public int addEntries(ArrayList<BibtexEntry> be) {
		if (be != null) {
			int totalcount = 0;
			// iterate nodes
			for (BibtexEntry node : be) {
				totalcount += addEntry(node);
			}
			return totalcount;
		}
		return 0;
	}

	/**
	 *
	 * @param be
	 * @return
	 */
	public int addEntry(BibtexEntry be) {
		// check for valid vaöue
		if (be != null) {
			// check whether entry does not already exist and bibkey does not exist
			if (!bibtexentries.contains(be) && null == getEntry(be.getEntryKey())) {
				// get type
				String type = be.getEntryType();
				// check for valid value. don't import comments
				if (type != null && !type.equalsIgnoreCase("comment")) {
					// if yes, add that entry to the linked list
					bibtexentries.add(be);
					// change modified state
					setModified(true);
					return 1;
				}
			}
		}
		return 0;
	}

	/**
	 * Gets the path to the last used bibtex-file
	 *
	 * @return the path of the last used bibtex-file, or {@code null} if no such
	 *         path was specified nor does exist.
	 */
	public File getFilePath() {
		return settingsObj.getLastUsedBibTexFile();
	}

	/**
	 * Returns the currently attached bibtex-file. May be used to determine whether
	 * the user's chosen bibtex-file <i>is already</i> opened or <i>has to be</i>
	 * opened.
	 *
	 * @return the currently attached bibtex-file
	 */
	public File getCurrentlyAttachedFile() {
		return currentlyattachedfile;
	}

	/**
	 * "Detaches" the currently attached bibtex-file, which means that the filepath
	 * to the currently attached file is set to {@code null}.
	 */
	public void detachCurrentlyAttachedFile() {
		currentlyattachedfile = null;
	}

	public void setEncoding(int encoding) {
		settingsObj.setLastUsedBibtexFormat(encoding);
	}

	public int getEncoding() {
		return settingsObj.getLastUsedBibtexFormat();
	}

	public boolean refreshBibTexFile(Settings settingsObj) {
		// attach new file
		boolean success = openAttachedFile(Constants.BIBTEX_ENCODINGS[settingsObj.getLastUsedBibtexFormat()], false,
				true);
		return success;
	}

	/**
	 * This method opens (and "attaches") a bibtex-file which is specified via the
	 * {@link #setFilePath(java.io.File) setFilePath(File)} method. The file is
	 * parsed into the private variable {@code bibtexfile}, which can be accessed
	 * via {@link #getFile() getFile()}.
	 *
	 * @param encoding               the character encoding of the file. use values
	 *                               of the array
	 *                               {@code CConstants.BIBTEX_ENCODINGS} as
	 *                               parameter.
	 * @param suppressNewEntryImport {@code true} if missing entries should
	 *                               <b>not</b> be added to the
	 *                               {@link #bibtexentries}, i.e. the ZKN3-Database.
	 *                               Use {@code false} if missing entries should be
	 *                               added.
	 * @param updateExistingEntries  {@code true} whether entries with identical
	 *                               bibkey that have already been imported into the
	 *                               internal data base should be updated (replaced)
	 *                               with entries from the attached bibtex file.
	 *
	 * @return {@code true} if attachedfile was successfully opened, {@code false}
	 *         otherwise.
	 */
	public boolean openAttachedFile(String encoding, boolean suppressNewEntryImport, boolean updateExistingEntries) {
		// reset currently attached filepath
		currentlyattachedfile = null;
		// if we have no bibtex-filepath, return false
		if (getFilePath() == null || !getFilePath().exists()) {
			return false;
		}
		// create a new bibtex-parser for parsing the bibtex-file
		BibtexParser bp = new BibtexParser(false);
		// create stream-readers for reading the file
		InputStreamReader isr = null;
		InputStream is = null;
		try {
			// create fileinput-stream
			is = new FileInputStream(getFilePath());
			// read the stream, using the related character encoding
			isr = new InputStreamReader(is, encoding);
			// create new bibtex-file
			bibtexfile = new BibtexFile();
			// parse file into our bibtexfile-variable
			bp.parse(bibtexfile, isr);
			// get all nodes (entries) from the bibtex-file, so we can
			// prepare a linked list containing all entries of that bibtex-file
			List<?> bibNodes = bibtexfile.getEntries();
			// reset old linked list
			attachedbibtexentries.clear();
			// iterate nodes
			for (Object node : bibNodes) {
				// check whether the node is of type "bibtexentry"
				if (node instanceof BibtexEntry) {
					BibtexEntry be = (BibtexEntry) node;
					// if yes, add that entry to the linked list
					attachedbibtexentries.add(be);
					// now we have all entries from the specified bibtex-file
					// parsed into a linked list, so we have easy access to each
					// single bibtex-entry via the list "bibtexentries"
				}
			}
			// set new attached filepath, so we can figure out whether we have any
			// attached file or not...
			currentlyattachedfile = getFilePath();
		} catch (ParseException | IOException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return false;
			}
		}
		checkWhetherMissingEntriesShouldBeAdded(suppressNewEntryImport, updateExistingEntries);
		return true;
	}

	private void checkWhetherMissingEntriesShouldBeAdded(boolean suppressNewEntryImport,
			boolean updateExistingEntries) {
		// check whether missing entries should be added
		if (!updateExistingEntries && !suppressNewEntryImport) {
			// add all new entries to data base
			int newentries = addEntries(attachedbibtexentries);
			tellUser(newentries);
		}
	}

	private void tellUser(int newentries) {
		// tell user
		if (newentries > 0) {
			JOptionPane.showMessageDialog(null,
					resourceMap.getString("importMissingBibtexEntriesText", String.valueOf(newentries), 0 + ""),
					"BibTeX-Import", JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * This method opens (and "attaches") a bibtex-file which is specified via the
	 * {@link #setFilePath(java.io.File) setFilePath(File)} method. The file is
	 * parsed into the private variable {@code bibtexfile}, which can be accessed
	 * via {@link #getFile() getFile()}.
	 *
	 * @param encoding               the character encoding of the file. use values
	 *                               of the array
	 *                               {@code CConstants.BIBTEX_ENCODINGS} as
	 *                               parameter.
	 * @param suppressNewEntryImport {@code true} if missing entries should
	 *                               <b>not</b> be added to the
	 *                               {@link #bibtexentries}, i.e. the ZKN3-Database.
	 *                               Use {@code false} if missing entries should be
	 *                               added.
	 *
	 * @return {@code true} if attachedfile was successfully opened, {@code false}
	 *         otherwise.
	 */
	public boolean openAttachedFile(String encoding, boolean suppressNewEntryImport) {
		return openAttachedFile(encoding, suppressNewEntryImport, false);
	}

	/**
	 * This method opens (and "attaches") a bibtex-file which is specified via the
	 * {@link #setFilePath(java.io.File) setFilePath(File)} method. The file is
	 * parsed into the private variable {@code bibtexfile}, which can be accessed
	 * via {@link #getFile() getFile()}.
	 *
	 * @param is
	 * @param encoding the character encoding of the file. use values of the array
	 *                 {@code Constants.BIBTEX_ENCODINGS} as parameter.
	 * @return {@code true} if attachedfile was successfully opened, {@code false}
	 *         otherwise.
	 */
	public boolean openFile(InputStream is, String encoding) {
		// if we have no bibtex-filepath, return false
		if (null == is) {
			return false;
		}
		// create a new bibtex-parser for parsing the bibtex-file
		BibtexParser bp = new BibtexParser(false);
		// create stream-readers for reading the file
		InputStreamReader isr = null;
		try {
			// read the stream, using the related character encoding
			isr = new InputStreamReader(is, encoding);
			// create new bibtex-file
			bibtexfile = new BibtexFile();
			// parse file into our bibtexfile-variable
			bp.parse(bibtexfile, isr);
			// get all nodes (entries) from the bibtex-file, so we can
			// prepare a linked list containing all entries of that bibtex-file
			List<?> bibNodes = bibtexfile.getEntries();
			// reset old linked list
			bibtexentries.clear();
			// iterate nodes
			for (Object node : bibNodes) {
				// check whether the node is of type "bibtexentry"
				if (node instanceof BibtexEntry) {
					// if yes, add that entry to the linked list
					bibtexentries.add((BibtexEntry) node);
					// now we have all entries from the specified bibtex-file
					// parsed into a linked list, so we have easy access to each
					// single bibtex-entry via the list "bibtexentries"
				}
			}
		} catch (ParseException | IOException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				is.close();
			} catch (IOException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	public boolean appendFile(InputStream is, String encoding) {
		// if we have no bibtex-filepath, return false
		if (null == is) {
			return false;
		}
		// create a new bibtex-parser for parsing the bibtex-file
		BibtexParser bp = new BibtexParser(false);
		// create stream-readers for reading the file
		InputStreamReader isr = null;
		try {
			// read the stream, using the related character encoding
			isr = new InputStreamReader(is, encoding);
			// create new bibtex-file
			BibtexFile appfile = new BibtexFile();
			// parse file into our bibtexfile-variable
			bp.parse(appfile, isr);
			// get all nodes (entries) from the bibtex-file, so we can
			// prepare a linked list containing all entries of that bibtex-file
			List<?> bibNodes = appfile.getEntries();
			// iterate nodes
			for (Object node : bibNodes) {
				// check whether the node is of type "bibtexentry"
				if (node instanceof BibtexEntry) {
					// if yes, add that entry to the linked list
					addEntry((BibtexEntry) node);
					// now we have all entries from the specified bibtex-file
					// parsed into a linked list, so we have easy access to each
					// single bibtex-entry via the list "bibtexentries"
				}
			}
		} catch (ParseException | IOException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				is.close();
			} catch (IOException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * This method exports all BibtexEntries in the list {@link #outputbibtexentries
	 * outputbibtexentries} to the file given in the parameter {@code fp}.<br>
	 * <br>
	 * Use {@link #clearExportBibtexEntries() clearExportBibtexEntries()} to reset
	 * the list of export entries and
	 * {@link #addBibtexEntryForExport(bibtex.dom.BibtexEntry)
	 * addBibtexEntryForExport(bibtex.dom.BibtexEntry)} to add new entries to this
	 * list that should be exported.
	 *
	 * @return {@code true} if entries have been successfully exported,
	 *         {@code false} otherwise.
	 */
	public ByteArrayOutputStream saveFile() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bs, Charset.forName("UTF-8"));
		PrintWriter pf = new PrintWriter(osw);

		Iterator<BibtexEntry> i = bibtexentries.iterator();
		while (i.hasNext()) {
			i.next().printBibtex(pf);
		}
		try {
			osw.close();
			pf.close();
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
		}
		return bs;
	}

	/**
	 * This method returns the amount of entries in the currently attachedfile,
	 * which equals the size of the list {@code inputbibtexentries}.
	 *
	 * @return the amount of entries in the currently attachedfile
	 */
	public int getAttachedFileCount() {
		return attachedbibtexentries.size();
	}

	/**
	 * This method returns the amount of entries in the bibtex file, which equals
	 * the size of the list {@code inputbibtexentries}.
	 *
	 * @return the amount of entries in the currently bibtex file in the
	 *         ZKN3-Database
	 */
	public int getCount() {
		return bibtexentries.size();
	}

	/**
	 *
	 * @return
	 */
	public BibtexFile getFile() {
		return bibtexfile;
	}

	/**
	 * This method returns the file name of the last used bibtex-file.
	 *
	 * @return the name of the given file, excluding extension, or {@code null} if
	 *         an error occured.
	 */
	public String getFileName() {
		return FileOperationsUtil.getFileName(settingsObj.getLastUsedBibTexFile());
	}

	/**
	 * This method returns the imported entries of the original ("attached")
	 * bibtex-file. <br>
	 * <br>
	 * Bibtex-entries that should be exported to a new created bibtex-file always
	 * use functions with the suffix "ForExport" (e.g.
	 * {@link #addBibtexEntryForExport(bibtex.dom.BibtexEntry)
	 * addBibtexEntryForExport()}).
	 *
	 * @return
	 */
	public ArrayList<BibtexEntry> getEntriesFromAttachedFile() {
		return attachedbibtexentries;
	}

	/**
	 * This method returns the entries of the bibtex-file in the ZKN3-Database. <br>
	 * <br>
	 * Bibtex-entries that should be exported to a new created bibtex-file always
	 * use functions with the suffix "ForExport" (e.g.
	 * {@link #addBibtexEntryForExport(bibtex.dom.BibtexEntry)
	 * addBibtexEntryForExport()}).
	 *
	 * @return
	 */
	public ArrayList<BibtexEntry> getEntries() {
		return bibtexentries;
	}

	/**
	 * Gets the bibtex-entry indicated by {@code nr} from the original (attached)
	 * bibtex-file.
	 *
	 * @param nr the entry-number of the entry that should be returned
	 * @return the related entry, or {@code null} if an error occured.
	 */
	public BibtexEntry getEntryFromAttachedFile(int nr) {
		try {
			return attachedbibtexentries.get(nr);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Gets the bibtex-entry indicated by {@code nr} from the original bibtex-file
	 * in the ZKN3-Database.
	 *
	 * @param nr the entry-number of the entry that should be returned
	 * @return the related entry, or {@code null} if an error occured.
	 */
	public BibtexEntry getEntry(int nr) {
		try {
			return bibtexentries.get(nr);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Gets the bibtex-entry indicated by {@code bibkey} from the original
	 * (attached) bibtex-file.
	 *
	 * @param bibkey the bibkey of the entry that should be returned
	 * @return the related entry, or {@code null} if an error occured.
	 */
	public BibtexEntry getEntryFromAttachedFile(String bibkey) {
		// create an iterator
		Iterator<BibtexEntry> i = attachedbibtexentries.iterator();
		// and iterate all parsed entries from the attached file
		while (i.hasNext()) {
			// get each bibtex-entry
			BibtexEntry be = i.next();
			// if the entry-key (which is a bibkey) equals the parameter bibkey,
			// return that entry
			if (be.getEntryKey().equals(bibkey)) {
				return be;
			}
		}
		// else, if nothing found, return null...
		return null;
	}

	/**
	 * Gets the bibtex-entry indicated by {@code bibkey} from the original
	 * bibtex-file in the ZKN3-Database.
	 *
	 * @param bibkey the bibkey of the entry that should be returned
	 * @return the related entry, or {@code null} if an error occured or nothing
	 *         found.
	 */
	public BibtexEntry getEntry(String bibkey) {
		// create an iterator
		Iterator<BibtexEntry> i = bibtexentries.iterator();
		// and iterate all parsed entries from the attached file
		while (i.hasNext()) {
			// get each bibtex-entry
			BibtexEntry be = i.next();
			// if the entry-key (which is a bibkey) equals the parameter bibkey,
			// return that entry
			if (be.getEntryKey().equals(bibkey)) {
				return be;
			}
		}
		// else, if nothing found, return null...
		return null;
	}

	/**
	 * Checks whether an entry with the {@code bibkey} already exists in the data
	 * base.
	 *
	 * @param bibkey the bibkey, which should be checked for existence
	 * @return {@code true} if an entry with {@code bibkey} exists in the interal
	 *         data base, {@code false} otherwise.
	 */
	public boolean hasEntry(String bibkey) {
		return getEntry(bibkey) != null;
	}

	/**
	 * Sets (replaces) the bibtex-entry indicated by {@code bibkey} from the
	 * original bibtex-file in the ZKN3-Database with a new BibTexEntry
	 * {@code entry}.
	 *
	 * @param bibkey the bibkey of the entry that should be updated / set
	 * @param entry  the bibtexentry that should replace the old value
	 */
	public void setEntry(String bibkey, BibtexEntry entry) {
		// do we have any entries?
		if (null == bibtexentries || 0 == bibtexentries.size()) {
			return;
		}
		// create an iterator
		for (int i = 0; i < bibtexentries.size(); i++) {
			// get each bibtex-entry
			BibtexEntry be = bibtexentries.get(i);
			// if the entry-key (which is a bibkey) equals the parameter bibkey,
			// return that entry
			if (be.getEntryKey().equals(bibkey)) {
				bibtexentries.set(i, entry);
				return;
			}
		}
	}

	/**
	 * Removes the entry with the bibkey {@code bibkey} from the database.
	 *
	 * @param bibkey the bibkey value of the entry that should be removed from the
	 *               internal bibtex database.
	 * @return the removed bibtex-entry, or {@code null} of no entry was removed.
	 */
	public BibtexEntry removeEntry(String bibkey) {
		// retrieve entry index number
		int nr = getEntryIndexNumber(bibkey);
		// check for valid value
		if (nr != -1) {
			// remove entry
			BibtexEntry be = bibtexentries.remove(nr);
			// set modified state
			setModified(true);
			// return removed entry
			return be;
		}
		return null;
	}

	/**
	 * Gets an bibtex-entry index-number from the bibtex-entry that is associated
	 * with the bibkey-value {@code bibkey} from the original bibtex-file in the
	 * ZKN3-Database.
	 *
	 * @param bibkey the bibkey of the entry which number should be returned
	 * @return the related entry-number of the bibtex-entry that has the
	 *         bibkey-value {@code bibkey}, or {@code -1} if no such entry or bibkey
	 *         exists
	 */
	public int getEntryIndexNumber(String bibkey) {
		// create an iterator
		Iterator<BibtexEntry> i = bibtexentries.iterator();
		// init counter
		int counter = 0;
		// and iterate all parsed entries from the attached file
		while (i.hasNext()) {
			// get each bibtex-entry
			BibtexEntry be = i.next();
			// if the entry-key (which is a bibkey) equals the parameter bibkey,
			// return that entry-index-number
			if (be.getEntryKey().equals(bibkey)) {
				return counter;
			}
			// else increase counter
			counter++;
		}
		// else, if nothing found, return null...
		return -1;
	}

	/**
	 * This method returns the bibkey of the entry {@code entrynr} from the
	 * currently attached file.
	 *
	 * @param entrynr the entry-number of an entry within the currently attached
	 *                bibtex-file
	 * @return the related bibkey, or null if no key or no such entry exists.
	 */
	public String getBibkeyFromAttachedFile(int entrynr) {
		// retrieve entry
		BibtexEntry be = getEntryFromAttachedFile(entrynr);
		// if we have no valid entry, return null
		if (null == be) {
			return null;
		}
		// return entry-key
		return be.getEntryKey();
	}

	/**
	 * This method returns the bibkey of the entry {@code entrynr} from the
	 * currently attached file.
	 *
	 * @param entrynr the entry-number of an entry within the currently attached
	 *                bibtex-file
	 * @return the related bibkey, or null if no key or no such entry exists.
	 */
	public String getBibkey(int entrynr) {
		// retrieve entry
		BibtexEntry be = getEntry(entrynr);
		// if we have no valid entry, return null
		if (null == be) {
			return null;
		}
		// return entry-key
		return be.getEntryKey();
	}

	public String getAbstract(String bibkey) {
		// return abstract from bibtex entry
		return getAbstract(getEntry(bibkey));
	}

	/**
	 * This method returns the abstract or annotation of the entry {@code entrynr}
	 * from the currently attached file.
	 *
	 * @param bibkey
	 * @return the related abstract, or null if no key or no such entry exists.
	 */
	public String getAbstractFromAttachedFile(String bibkey) {
		// return abstract from bibtex entry
		return getAbstract(getEntryFromAttachedFile(bibkey));
	}

	/**
	 * This method retrieves the content of an bibtex-entry's abstract or annotation
	 * and returns it as string.
	 *
	 * @param be the bibtex-entry which abstract/annotation should be retrieved
	 * @return the abstract or annotation as string, or {@code null} if no such
	 *         entry or content exists.
	 */
	private String getAbstract(BibtexEntry be) {
		// if we have no valid entry, return null
		if (null == be) {
			return null;
		}
		// init variable
		String content = null;
		// retrieve abstract
		BibtexAbstractValue bav = be.getFieldValue("abstract");
		if (bav != null) {
			content = bav.toString();
		}
		// if the entry does not have a field named "abstract", try field-name "annote"
		// instead
		// (this name is used by Synapsen when entries are exported).
		if (null == content || content.isEmpty()) {
			bav = be.getFieldValue("annote");
			if (bav != null) {
				content = bav.toString();
			}
			// if the entry does not have a field named "abstract" nor "annote", try
			// field-name "note" instead
			// (this name is used by some apps when entries are exported).
			if (null == content || content.isEmpty()) {
				bav = be.getFieldValue("note");
				if (bav != null) {
					content = bav.toString();
				}
			}
		}
		// if we have any content, replace braces
		if (content != null) {
			content = content.replace("{", "").replace("}", "");
		}
		// return content
		return content;
	}

	public String[] getKeywords(String bibkey) {
		return getKewords(getEntry(bibkey));
	}

	public String[] getKeywords(int entrynr) {
		return getKewords(getEntry(entrynr));
	}

	/**
	 * This method returns the keywords or annotation of the entry {@code entrynr}
	 * from the currently attached file.
	 *
	 * @param entrynr the entry-number of an entry within the currently attached
	 *                bibtex-file
	 * @return the related keywords as string-array, or null if no keywords or no
	 *         such entry exists.
	 */
	public String[] getKeywordsFromAttachedFile(int entrynr) {
		return getKewords(getEntryFromAttachedFile(entrynr));
	}

	/**
	 * This method returns the keywords or annotation of the entry {@code entrynr}
	 * from the currently attached file.
	 *
	 * @param bibkey
	 * @return the related keywords as string-array, or null if no keywords or no
	 *         such entry exists.
	 */
	public String[] getKeywordsFromAttachedFile(String bibkey) {
		return getKewords(getEntryFromAttachedFile(bibkey));
	}

	/**
	 * This method retrieves the keywords of a bibtex-entry and returns them as
	 * string-array.
	 *
	 * @param be the bibtex-entry which keywords are requested
	 * @return the keywords of the bibtex-entry {@code be} as string-array, or
	 *         {@code null} if no such entry or keywords exist.
	 */
	private String[] getKewords(BibtexEntry be) {
		// if we have no valid entry, return null
		if (null == be) {
			return null;
		}
		// init variable
		String[] keywords = null;
		// retrieve keywods
		BibtexAbstractValue bav = be.getFieldValue("keywords");
		if (bav != null) {
			// remove braces
			String keywordline = bav.toString().replace("{", "").replace("}", "");
			// check whether keywords contain ; or , as separator-char
			String sep = (-1 == keywordline.indexOf(";")) ? "," : ";";
			// split keywords
			keywords = keywordline.split(sep);
		} // in some cases, the field "keywords" is named "tags" instead. look for this
			// field if "keywords" does not exist
		else {
			// retrieve keywods
			bav = be.getFieldValue("tags");
			if (bav != null) {
				// remove braces
				String keywordline = bav.toString().replace("{", "").replace("}", "");
				// check whether keywords contain ; or , as separator-char
				String sep = (-1 == keywordline.indexOf(";")) ? "," : ";";
				// split keywords
				keywords = keywordline.split(sep);
			}
		}
		// trim spaces
		if (keywords != null) {
			for (int i = 0; i < keywords.length; i++) {
				keywords[i] = keywords[i].trim();
			}
		}
		return keywords;
	}

	/**
	 *
	 * @param bibkey
	 * @return
	 */
	public int getEntryType(String bibkey) {
		// retrieve entry
		BibtexEntry be = getEntry(bibkey);
		// if we have no valid entry, return null
		if (null == be) {
			return -1;
		}
		// get entry's type
		String entrytype = be.getEntryType();
		// if no valid value was found, return null
		if (null == entrytype || entrytype.isEmpty()) {
			return -1;
		}
		if (entrytype.equalsIgnoreCase("book")) {
			return Constants.BIBTEX_ENTRYTYPE_BOOK;
		} else if (entrytype.equalsIgnoreCase("article")) {
			return Constants.BIBTEX_ENTRYTYPE_ARTICLE;
		} else if (entrytype.equalsIgnoreCase("incollection")) {
			return Constants.BIBTEX_ENTRYTYPE_BOOKARTICLE;
		} else if (entrytype.equalsIgnoreCase("inbook")) {
			return Constants.BIBTEX_ENTRYTYPE_CHAPTER;
		} else if (entrytype.equalsIgnoreCase("mastersthesis")) {
			return Constants.BIBTEX_ENTRYTYPE_THESIS;
		} else if (entrytype.equalsIgnoreCase("phdthesis")) {
			return Constants.BIBTEX_ENTRYTYPE_PHD;
		} else if (entrytype.equalsIgnoreCase("unpublished")) {
			return Constants.BIBTEX_ENTRYTYPE_UNPUBLISHED;
		} else if (entrytype.equalsIgnoreCase("conference")) {
			return Constants.BIBTEX_ENTRYTYPE_CONFERENCE;
		} else if (entrytype.equalsIgnoreCase("techreport") || entrytype.equalsIgnoreCase("report")) {
			return Constants.BIBTEX_ENTRYTYPE_REPORT;
		}
		return -1;
	}

	/**
	 *
	 * @param be
	 */
	public void addBibtexEntryForExport(BibtexEntry be) {
		outputbibtexentries.add(be);
	}

	/**
	 * Clears the list {@link #outputbibtexentries outputbibtexentries} which
	 * contains all entries that should be exported to a new Bibtex-file
	 */
	public void clearExportBibtexEntries() {
		outputbibtexentries.clear();
	}

	public void clearEntries() {
		bibtexentries.clear();
		setModified(true);
	}

	/**
	 * This method exports all BibtexEntries in the list {@link #outputbibtexentries
	 * outputbibtexentries} to the file given in the parameter {@code fp}.<br>
	 * <br>
	 * Use {@link #clearExportBibtexEntries() clearExportBibtexEntries()} to reset
	 * the list of export entries and
	 * {@link #addBibtexEntryForExport(bibtex.dom.BibtexEntry)
	 * addBibtexEntryForExport(bibtex.dom.BibtexEntry)} to add new entries to this
	 * list that should be exported.
	 *
	 * @param fp the filepath and filename of the new bibtex-file that should be
	 *           created, containing all entries which are currently saved to
	 *           {@link #outputbibtexentries outputbibtexentries}.
	 * @return {@code true} if entries have been successfully exported,
	 *         {@code false} otherwise.
	 */
	public boolean exportBibtexEntries(File fp) {
		if (outputbibtexentries.size() < 1) {
			return false;
		}
		OutputStream os = null;
		OutputStreamWriter osw = null;
		PrintWriter pf = null;
		try {
			os = new FileOutputStream(fp);
			osw = new OutputStreamWriter(os, Constants.BIBTEX_ENCODINGS[settingsObj.getLastUsedBibtexFormat()]);
			pf = new PrintWriter(osw);

			Iterator<BibtexEntry> i = outputbibtexentries.iterator();
			while (i.hasNext()) {
				i.next().printBibtex(pf);
			}
		} catch (IOException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		} finally {
			try {
				if (pf != null) {
					pf.close();
				}
				if (osw != null) {
					osw.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * This method returns a formatted string consisting of the author-, year- and
	 * title-value of an bibtex-entry of the currently attached bibtex-file. The
	 * requested entry is specified by its {@code entrynr}.
	 *
	 * @param entrynr the number of the entry that should be retrieved
	 * @return a string containing author, year and title-information of the entry
	 *         that matched the bibkey {@code bibkey}
	 */
	public String getFormattedEntryFromAttachedFile(int entrynr) {
		// retrieve entry that is associated with the "bibkey"-parameter
		BibtexEntry be = getEntryFromAttachedFile(entrynr);
		return getFormattedEntry(be, true);
	}

	/**
	 * This method returns a formatted string consisting of the author-, year- and
	 * title-value of an bibtex-entry of the currently attached bibtex-file. The
	 * requested entry is specified by its {@code entrynr}.
	 *
	 * @param entrynr the number of the entry that should be retrieved
	 * @return a string containing author, year and title-information of the entry
	 *         that matched the bibkey {@code bibkey}
	 */
	public String getFormattedEntry(int entrynr) {
		// retrieve entry that is associated with the "bibkey"-parameter
		BibtexEntry be = getEntry(entrynr);
		return getFormattedEntry(be, false);
	}

	/**
	 * This method returns a formatted string consisting of the author-, year- and
	 * title-value of an bibtex-entry of the currently attached bibtex-file. The
	 * requested entry is specified by its {@code bibkey}.
	 *
	 * @param bibkey the bibkey of the entry that should be retrieved
	 * @return a string containing author, year and title-information of the entry
	 *         that matched the bibkey {@code bibkey}
	 */
	public String getFormattedEntry(String bibkey) {
		// retrieve entry that is associated with the "bibkey"-parameter
		BibtexEntry be = getEntry(bibkey);
		return getFormattedEntry(be, false);
	}

	/**
	 * This method returns a formatted string consisting of the author-, year- and
	 * title-value of an bibtex-entry of the currently attached bibtex-file. The
	 * requested entry is specified by its {@code entrynr}.
	 *
	 * @param entrynr the number of the entry that should be retrieved
	 * @return a string containing author, year and title-information of the entry
	 *         that matched the bibkey {@code bibkey}
	 */
	public String getFormattedAuthor(int entrynr) {
		// retrieve entry that is associated with the "bibkey"-parameter
		BibtexEntry be = getEntry(entrynr);
		return getFormattedAuthor(be);
	}

	/**
	 * This method returns a formatted string consisting of the author-, year- and
	 * title-value of an bibtex-entry of the currently attached bibtex-file. The
	 * requested entry is specified by its {@code bibkey}.
	 *
	 * @param bibkey the bibkey of the entry that should be retrieved
	 * @return a string containing author, year and title-information of the entry
	 *         that matched the bibkey {@code bibkey}
	 */
	public String getFormattedAuthor(String bibkey) {
		// retrieve entry that is associated with the "bibkey"-parameter
		BibtexEntry be = getEntry(bibkey);
		return getFormattedAuthor(be);
	}

	/**
	 * This method returns a formatted string consisting of the author-, 
         * year- and title-value of an bibtex-entry of the currently attached 
         * bibtex-file. Thismethod does the work for both
	 * {@link #getFormattedEntryFromAttachedFile(java.lang.String)
	 * getFormattedEntryFromAttachedFile(String)} and
	 * {@link #getFormattedEntryFromAttachedFile(int)
	 * getFormattedEntryFromAttachedFile(int)}.<br>
	 * <br>
	 * The way an author-value from the imported bibtex-value is formatted 
         * and output as string is defined via the {@link #initStyles()
	 * initStyles()}-method. There we create linked hashmaps that contain 
         * the elements for different literatur-types (books, articles, 
         * abstracts...).<br>
	 * <br>
	 * In this method, we first check out the <i>type</i> of the bibtex-
         * entry (book, article...) and then get the related LinkedHashMap. 
         * In this HashMap, we have the single elements of the literatur 
         * (author, title, year, publisher...) in a certain order, with special 
         * values associated to each element. This is the formatting.<br>
	 * <br>
	 * E.g.: The key <b>"year"</b> has the value <b>" (*):"</b>, where the 
         * asterisk is replaced by the year-value, if the bibtex-entry contains 
         * a year-value. The formatted year thus would be for instance 
         * <b>" (2009):"</b>.
	 *
	 * @param be               the BibtexEntry that should be formatted
	 * @param fromAttachedFile
	 * @return the formatted output-string containing author, year and
	 *         title-information of the BibtexEntry {@code be}.
	 */
	public String getFormattedEntry(BibtexEntry be, boolean fromAttachedFile) {
		// if we found any entry, go on...
		if (be != null) {
			// get all entry fields
			Map<?, ?> m = be.getFields();
			// create a new map that will contain all fields that have to be replaced
			// for formatting the author-value. see below
			Map<String, String> fields;
			// copy import-type based on current citestyle into variable
			List<Map<String, String>> citetype = new ArrayList<>();
			switch (getCiteStyle()) {
			case Constants.BIBTEX_CITE_STYLE_GENERAL:
				citetype = importtypes;
				break;
			case Constants.BIBTEX_CITE_STYLE_CBE:
				citetype = importtypesCBE;
				break;
			case Constants.BIBTEX_CITE_STYLE_APA:
				citetype = importtypesAPA;
				break;
			}
			// check whether entry type is known. needed below
			boolean entryTypeKnown = true;
			// here we choose the import-style, depending on the type of author (book,
			// article...)
			if (be.getEntryType().equalsIgnoreCase("book")) {
				fields = citetype.get(1);
			} else if (be.getEntryType().equalsIgnoreCase("article")) {
				fields = citetype.get(2);
			} else if (be.getEntryType().equalsIgnoreCase("incollection")) {
				fields = citetype.get(3);
			} else if (be.getEntryType().equalsIgnoreCase("inbook")) {
				fields = citetype.get(4);
			} else if (be.getEntryType().equalsIgnoreCase("misc")) {
				fields = citetype.get(5);
			} else {
				fields = citetype.get(0);
				entryTypeKnown = false;
			}
			// retrieve all keys, i.e. author, title etc.
			Set<?> ks = m.keySet();
			StringBuilder sb = new StringBuilder("");
			// get the field from the import-type
			Set<String> fieldsets = fields.keySet();
			// create iterator
			Iterator<String> fi = fieldsets.iterator();
			// now we go through all field-elements that have been defined in the
			// import-style,
			// i.e. we look for authors, years, titles etc., in that order that was used
			// when
			// initiating the linked maps (see "initStyles()").
			while (fi.hasNext()) {
				// reset dummy-string
				String dummy = null;
				// retrieve each element of the author-type
				String f = fi.next();
				// check whether the field (author, year, title...) exists, and if yes, retrieve
				// value
				if (ks.contains(f)) {
					// get abstract value. we do not convert it directly to string. in case
					// "getFieldValue"
					// returns null, this would lead to a nullpointerexception.
					BibtexAbstractValue bav = be.getFieldValue(f);
					if (bav != null) {
						dummy = bav.toString();
					}
				} // in some cases, we have books, that do not have authors but editors only... in
					// this
					// case, check whether we have an editor-field instead of author-field.
				else if (f.equalsIgnoreCase("author") && (be.getEntryType().equalsIgnoreCase("misc")
						|| be.getEntryType().equalsIgnoreCase("book") || !entryTypeKnown)) {
					// check whether we find an editor-field instead of author-field
					if (ks.contains("editor")) {
						// get abstract value. we do not convert it directly to string. in case
						// "getFieldValue"
						// returns null, this would lead to a nullpointerexception.
						BibtexAbstractValue bav = be.getFieldValue("editor");
						if (bav != null) {
							dummy = bav.toString();
						}
					} // check whether we find an collaborator-field instead of author-field
					else if (ks.contains("collaborator")) {
						// get abstract value. we do not convert it directly to string. in case
						// "getFieldValue"
						// returns null, this would lead to a nullpointerexception.
						BibtexAbstractValue bav = be.getFieldValue("collaborator");
						if (bav != null) {
							dummy = bav.toString();
						}
					}
				} // in some cases, we have the field "date" in bibtex entries instead of "year".
					// in this case, we use the date-field as substitute for the year-field.
				else if (ks.contains("date") && !ks.contains("year") && f.equalsIgnoreCase("year")) {
					// get abstract value. we do not convert it directly to string. in case
					// "getFieldValue"
					// returns null, this would lead to a nullpointerexception.
					BibtexAbstractValue bav = be.getFieldValue("date");
					if (bav != null) {
						dummy = bav.toString();
					}
				}
				// if it's not empty...
				if (dummy != null && !dummy.isEmpty()) {
					// ...get the "format template"
					String app = fields.get(f);
					// and replace the place holder with the associated value from the bibtex-file
					if (app != null) {
						// first check, whether we have an author, because we need to split this
						if (f.equalsIgnoreCase("author") || f.equalsIgnoreCase("editor")) {
							// retrieve single authors
							String[] singleauthors = dummy.replace("{{", editorToken).replace("}}", "").replace("{", "")
									.replace("}", "").split(Pattern.quote(" and "));
							// prepare string builder
							StringBuilder finalauthors = new StringBuilder("");
							// iterate all found authors
							for (String aunames : singleauthors) {
								// add separator
								finalauthors.append(", ");
								// we have already removed one curly braces. if we have another one,
								// we have a comolete author phrase that should not be separated
								// we can completely add it as authors
								if (aunames.contains(editorToken)) {
									finalauthors.append(aunames.replace(editorToken, ""));
								} // if author sur- and given-names are comma-separated, we assume that the
									// sur-name comes first
								else if (aunames.contains(",")) {
									// retrieve sur and given name of author
									String[] names = aunames.trim().split(",");
									// check whether we have any author-field and value at all
									if (names != null && names.length > 0) {
										// add surname
										finalauthors.append(names[0].trim());
										// add name separator, depending on citatoin-style
										switch (getCiteStyle()) {
										case Constants.BIBTEX_CITE_STYLE_GENERAL:
											finalauthors.append(" ");
											break;
										case Constants.BIBTEX_CITE_STYLE_CBE:
											finalauthors.append(" ");
											break;
										case Constants.BIBTEX_CITE_STYLE_APA:
											finalauthors.append(", ");
											break;
										}
										// check whether we have any valid name-value at all, to avoid
										// null-pointer-exception
										if (names.length > 1 && names[1].length() > 0) {
											// in case we have several givennames, separate them
											// all at each space
											String[] givennames = names[1].trim().split(" ");
											// iterate all surnames
											for (String gname : givennames) {
												// check for at least one char length
												if (!gname.isEmpty() && gname.length() > 0) {
													switch (getCiteStyle()) {
													case Constants.BIBTEX_CITE_STYLE_GENERAL:
														finalauthors.append(gname.charAt(0));
														break;
													case Constants.BIBTEX_CITE_STYLE_CBE:
														finalauthors.append(gname.charAt(0));
														break;
													case Constants.BIBTEX_CITE_STYLE_APA:
														finalauthors.append(gname.charAt(0)).append(".");
														break;
													}
												}
											}
										}
									}
								} // else the given name comes first, so we
									// separate each full author-name at space-sign, so we can
									// retrieve sur- and given-name
								else {
									// retrieve sur and given name of author
									String[] names = aunames.trim().split(" ");
									// check whether we have any author-field and value at all
									if (names != null && names.length > 0) {
										// add sur-name
										finalauthors.append(names[names.length - 1].trim());
										// add sur and given-name separator
										switch (getCiteStyle()) {
										case Constants.BIBTEX_CITE_STYLE_GENERAL:
											finalauthors.append(" ");
											break;
										case Constants.BIBTEX_CITE_STYLE_CBE:
											finalauthors.append(" ");
											break;
										case Constants.BIBTEX_CITE_STYLE_APA:
											finalauthors.append(", ");
											break;
										}
										// iterate given names
										for (int cnt = 0; cnt < names.length - 1; cnt++) {
											// check whether we have any valid name-value at all, to avoid
											// null-pointer-exception
											if (names[cnt].length() > 0) {
												switch (getCiteStyle()) {
												case Constants.BIBTEX_CITE_STYLE_GENERAL:
													finalauthors.append(names[cnt].charAt(0));
													break;
												case Constants.BIBTEX_CITE_STYLE_CBE:
													finalauthors.append(names[cnt].charAt(0));
													break;
												case Constants.BIBTEX_CITE_STYLE_APA:
													finalauthors.append(names[cnt].charAt(0)).append(".");
													break;
												}
											}
										}
									}
								}
							}
							// copy all formatted authors to dummy-variable
							dummy = finalauthors.toString().substring(2).trim();
						}
						// here we replace the place holder with its content
						dummy = app.replace("*", dummy.replace("{", "").replace("}", ""));
						// check whether we have double periods at the end of the string...
						// this may happen, if the user entered a period at the end of a title,
						// while we automatically add a period after it.
						if ((dummy.endsWith("..") && !dummy.endsWith("..."))
								|| (dummy.endsWith("?.") || dummy.endsWith("!."))) {
							// cut the last period.
							dummy = dummy.substring(0, dummy.length() - 1);
						}
					}
					// convert escape chars
					dummy = convertEscapeChars(dummy, fromAttachedFile);
					// append it to final formatted author-value
					sb.append(dummy);
				}
			}
			// create a trimmed string, where tab and new-line-chars are replaced with
			// spaces
			String trimmedString = sb.toString().replace("\t", " ").replace("\n", " ").replace("\r", "")
					.replace("{ldots}", "...").trim();
			// to avoid double spaces, replace them with a single space
			return trimmedString.replace("  ", " ").trim();
		}
		return null;
	}

	/**
	 * This method returns a formatted string wth the author- and year-value of an
	 * bibtex-entry for in-text-citing. This method does the work for both
	 * {@link #getFormattedEntryFromAttachedFile(java.lang.String)
	 * getFormattedEntryFromAttachedFile(String)} and
	 * {@link #getFormattedEntryFromAttachedFile(int)
	 * getFormattedEntryFromAttachedFile(int)}.<br>
	 * <br>
	 * The way an author-value from the imported bibtex-value is formatted and
	 * output as string is defined via the {@link #initStyles()
	 * initStyles()}-method. There we create linked hashmaps that contain the
	 * elements for different literatur-types (books, articles, abstracts...).<br>
	 * <br>
	 * In this method, we first check out the <i>type</i> of the bibtex-entry (book,
	 * article...) and then get the related LinkedHashMap. In this HashMap, we have
	 * the single elements of the literatur (author, title, year, publisher...) in a
	 * certain order, with special values associated to each element. This is the
	 * formatting.<br>
	 * <br>
	 * E.g.: The key <b>"year"</b> has the value <b>" (*):"</b>, where the asterisk
	 * is replaced by the year-value, if the bibtex-entry contains a year-value. The
	 * formatted year thus would be for instance <b>" (2009):"</b>.
	 *
	 * @param be the BibtexEntry that should be formatted
	 * @return the formatted output-string containing author and year information of
	 *         the BibtexEntry {@code be}.
	 */
	private String getFormattedAuthor(BibtexEntry be) {
		// if we found any entry, go on...
		if (be != null) {
			// get all entry fields
			Map<?, ?> m = be.getFields();
			// retrieve all keys, i.e. author, title etc.
			Set<?> ks = m.keySet();
			String dummy = null;
			StringBuilder sb = new StringBuilder("");
			// check whether the field (author, year, title...) exists, and if yes, retrieve
			// value
			if (ks.contains("author")) {
				// get abstract value. we do not convert it directly to string. in case
				// "getFieldValue"
				// returns null, this would lead to a nullpointerexception.
				BibtexAbstractValue bav = be.getFieldValue("author");
				if (bav != null) {
					dummy = bav.toString();
				}
			} // in some cases, we have books, that do not have authors but editors only... in
				// this
				// case, check whether we have an editor-field instead of author-field.
				// check whether we find an editor-field instead of author-field
			else if (ks.contains("editor")) {
				// get abstract value. we do not convert it directly to string. in case
				// "getFieldValue"
				// returns null, this would lead to a nullpointerexception.
				BibtexAbstractValue bav = be.getFieldValue("editor");
				if (bav != null) {
					dummy = bav.toString();
				}
			} // check whether we find an collaborator-field instead of author-field
			else if (ks.contains("collaborator")) {
				// get abstract value. we do not convert it directly to string. in case
				// "getFieldValue"
				// returns null, this would lead to a nullpointerexception.
				BibtexAbstractValue bav = be.getFieldValue("collaborator");
				if (bav != null) {
					dummy = bav.toString();
				}
			}
			// check whether we have any authors at all. if not, quit
			if (null == dummy) {
				return null;
			}
			// retrieve single authors
			String[] singleauthors = dummy.replace("{{", editorToken).replace("}}", "").replace("{", "")
					.replace("}", "").split(Pattern.quote(" and "));
			// prepare string builder
			List<String> finalauthors = new ArrayList<>();
			// iterate all found authors
			for (String aunames : singleauthors) {
				// we have already removed one curly braces. if we have another one,
				// we have a comolete author phrase that should not be separated
				// we can completely add it as authors
				if (aunames.contains(editorToken)) {
					finalauthors.add(aunames.replace(editorToken, ""));
				} // check how many authors we have
					// if author sur- and given-names are comma-separated, we assume that the
					// sur-name comes first
				else if (aunames.contains(",")) {
					// retrieve sur and given name of author
					String[] names = aunames.trim().split(",");
					// check whether we have any author-field and value at all
					if (names != null && names.length > 0) {
						// add surname
						finalauthors.add(names[0].trim());
					}
				} // else the given name comes first, so we
					// separate each full author-name at space-sign, so we can
					// retieve sur- and given-name
				else {
					// retrieve sur and given name of author
					String[] names = aunames.trim().split(" ");
					// check whether we have any author-field and value at all
					if (names != null && names.length > 0) {
						// add sur-name
						finalauthors.add(names[names.length - 1].trim());
					}
				}
			}
			// check how many authors we have and add them to string builder
			if (1 == finalauthors.size()) {
				sb.append(finalauthors.get(0));
			} else if (2 == finalauthors.size()) {
				sb.append(finalauthors.get(0)).append(" & ").append(finalauthors.get(1));
			} else if (finalauthors.size() > 2) {
				sb.append(finalauthors.get(0)).append(" et al.");
			}
			// retrieve year
			if (ks.contains("year")) {
				// get abstract value. we do not convert it directly to string. in case
				// "getFieldValue"
				// returns null, this would lead to a nullpointerexception.
				BibtexAbstractValue bav = be.getFieldValue("year");
				if (bav != null) {
					dummy = bav.toString();
				}
			} // in some cases, we have the field "date" in bibtex entries instead of "year".
				// in this case, we use the date-field as substitute for the year-field.
			else if (ks.contains("date")) {
				// get abstract value. we do not convert it directly to string. in case
				// "getFieldValue"
				// returns null, this would lead to a nullpointerexception.
				BibtexAbstractValue bav = be.getFieldValue("date");
				if (bav != null) {
					dummy = bav.toString();
				}
			}
			sb.append(" ").append(dummy);
			// return result
			return convertEscapeChars(sb.toString(), false);
		}
		return null;
	}

	/**
	 *
	 * @param dummy
	 * @param fromAttachedFile
	 * @return
	 */
	private String convertEscapeChars(String dummy, boolean fromAttachedFile) {
		// check whether we have citavi- or mendeley-import. in such case, umlauts are
		// "encoded" with
		// backslah-quote (i.e. ä = \"a), so we have to re-cpnvert it.
		if ((fromAttachedFile && getEncoding() == Constants.BIBTEX_DESC_BIBDESK_INDEX) || !fromAttachedFile) {
			dummy = dummy.replace("{\\\"a}", "ä").replace("{\\\"A}", "Ä").replace("{\\\"o}", "ö")
					.replace("{\\\"O}", "Ö").replace("{\\\"u}", "ü").replace("{\\\"U}", "Ü").replace("{\\ss}", "ß");
		}
		if ((fromAttachedFile && getEncoding() == Constants.BIBTEX_DESC_CITAVI_INDEX
				|| getEncoding() == Constants.BIBTEX_DESC_MENDELEY_INDEX
				|| getEncoding() == Constants.BIBTEX_DESC_BIBDESK_INDEX) || !fromAttachedFile) {
			dummy = dummy.replace("\\\"a", "ä").replace("\\\"A", "Ä").replace("\\\"o", "ö").replace("\\\"O", "Ö")
					.replace("\\\"u", "ü").replace("\\\"U", "Ü").replace("\\ss", "ß");
		}
		return dummy;
	}
}
