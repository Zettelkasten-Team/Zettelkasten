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

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import de.danielluedecke.zettelkasten.CImportBibTex;
import de.danielluedecke.zettelkasten.CSetBibKey;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author danielludecke
 */
public class Settings {

	/**
	 * filepath to the currently in use settings file
	 * ("zettelkasten-settings.zks3"). This file is a zip-container with the
	 * file-extension ".zks3" and contains several XML-Files.
	 */
	private final File zipSettingsFile;
	/**
	 * filepath to the currently in use metadata file ("zettelkasten-data.zkd3").
	 * This file is a zip-container with the file-extension ".zkd3" and contains
	 * several XML-Files.
	 */
	private final File zipMetadataFile;

	private AcceleratorKeys acceleratorKeys = new AcceleratorKeys();
	private AutoKorrektur autoKorrekt = new AutoKorrektur();
	private Synonyms synonyms = new Synonyms();
	private StenoData steno = new StenoData();

	/**
	 * XML-Document that stores the settings-information
	 */
	private Document settingsFile;
	/**
	 * XML-Document that stores the foreign-words
	 */
	private Document foreignWordsFile;

	/**
	 * Indicates whether the programm is running on a mac with aqua-look and feel or
	 * not...
	 * 
	 * @return {@code true}, if the programm is running on a mac with aqua-look and
	 *         feel
	 */
	public boolean isMacStyle() {
		return getLookAndFeel().contains("Aqua") && PlatformUtil.isMacOS();
	}

	public boolean isSeaGlass() {
		return getLookAndFeel().equals(Constants.seaGlassLookAndFeelClassName);
	}

	public boolean isNimbus() {
		return getLookAndFeel().contains("nimbus");
	}

	/**
	 * Indicates whether the memory-logging in the main window is activated. See
	 * method {@code toggleMemoryTimer()} in {@code ZettelkastenView.class}. We
	 * store this toggle as a "global variable", so we can check whether
	 * memory-logging is enbaled or not from different jFrames...
	 */
	public boolean isMemoryUsageLogged = false;

	private boolean highlightSegments = false;

	/**
	 * This variable stores an entry-number that was passed as command line
	 * argument.
	 */
	private int commandLineInitialEntryNumber = -1;

	public static final int FONTNAME = 1;
	public static final int FONTSIZE = 2;
	public static final int FONTCOLOR = 3;
	public static final int FONTSTYLE = 4;
	public static final int FONTWEIGHT = 5;

	public static final int SHOWATSTARTUP_FIRST = 0;
	public static final int SHOWATSTARTUP_LAST = 1;
	public static final int SHOWATSTARTUP_RANDOM = 2;

	public static final int QUICK_INPUT_NORMAL = 0;
	public static final int QUICK_INPUT_MORE = 1;
	public static final int QUICK_INPUT_LESS = 2;

	public static final int CUSTOM_CSS_ENTRY = 1;
	public static final int CUSTOM_CSS_DESKTOP = 2;

	public static final String SETTING_LOGKEYWORDLIST_OR = "OR";
	public static final String SETTING_LOGKEYWORDLIST_AND = "AND";

	public static final String FONT_ARIAL = "Arial";
	public static final String FONT_TIMES = "Times";
	public static final String FONT_HELVETICA = "Helvetica";
	public static final String FONT_COURIER = "Courier";

	public static final String ZETTELKASTEN_DEFAULT_DIR_NAME = ".Zettelkasten";
	/**
	 * Amount of stored recent documents
	 */
	private static final int RECENT_DOC_COUNT = 8;
	/**
	 * Here we have constants that refer to the elements that store each setting
	 */
	private static final String SETTING_FILEPATH = "filepath";
	private static final String SETTING_STARTUPENTRY = "startupEntry";
	private static final String SETTING_SHOWATSTARTUP = "showatstartup";
	private static final String SETTING_LAF = "lookandfeel";
	private static final String SETTING_TABLEFONT = "listviewfont";
	private static final String SETTING_DESKTOPOUTLINEFONT = "desktopoutlinefont";
	private static final String SETTING_MAINFONT = "mainfont";
	private static final String SETTING_CODEFONT = "codefont";
	private static final String SETTING_AUTHORFONT = "authorfont";
	private static final String SETTING_REMARKSFONT = "remarksfont";
	private static final String SETTING_TITLEFONT = "titlefont";
	private static final String SETTING_APPENDIXHEADERFONT = "appendixheaderfont";
	private static final String SETTING_HEADERFONT1 = "headerfont1";
	private static final String SETTING_HEADERFONT2 = "headerfont2";
	private static final String SETTING_QUOTEFONT = "quotefont";
	private static final String SETTING_ENTRYHEADERFONT = "entryheaderfont";
	private static final String SETTING_DESKTOPHEADERFONT = "desktopheaderfont";
	private static final String SETTING_DESKTOPITEMHEADERFONT = "desktopitemheaderfont";
	private static final String SETTING_DESKTOPITEMFONT = "desktopitemfont";
	private static final String SETTING_DESKTOPCOMMENTFONT = "desktopcommentfont";
	private static final String SETTING_LOGKEYWORDLIST = "logKeywordList";
	private static final String SETTING_SHOWGRID_HORIZONTAL = "showgrid";
	private static final String SETTING_SHOWGRID_VERTICAL = "showverticalgrid";
	private static final String SETTING_CELLSPACING = "cellspacing";
	private static final String SETTING_TABLEFONTSIZE = "tablefontsize";
	private static final String SETTING_DESKTOPOUTLINEFONTSIZE = "desktopoutlinefontsize";
	private static final String SETTING_TEXTFIELDFONTSIZE = "textfieldfontsize";
	private static final String SETTING_SPELLCORRECT = "spellcorrect";
	private static final String SETTING_STENOACTIVATED = "stenoactivated";
	private static final String SETTING_HIGHLIGHTWHOLEWORD = "highlightwholeword";
	private static final String SETTING_HIGHLIGHTWHOLEWORDSEARCH = "highlightwholewordsearch";
	private static final String SETTING_QUICKINPUT = "quckinput";
	private static final String SETTING_QUICKINPUTEXTENDED = "quickinputextended";
	private static final String SETTING_IMGRESIZE = "imgresize";
	private static final String SETTING_IMGRESIZEWIDTH = "imgresizewidth";
	private static final String SETTING_IMGRESIZEHEIGHT = "imgresizeheight";
	private static final String SETTING_SEARCHWHERE = "searchwhere";
	private static final String SETTING_SEARCHWHAT = "searchwhat";
	private static final String SETTING_SEARCHOPTION = "searchoption";
	private static final String SETTING_REPLACEWHERE = "replacewhere";
	private static final String SETTING_REPLACEWHAT = "replacewhat";
	private static final String SETTING_REPLACEOPTION = "replaceoption";
	private static final String SETTING_DESKTOPDISPLAYITEMS = "desktopdisplayitems";
	private static final String SETTING_AUTOBACKUP = "autobackup";
	private static final String SETTING_EXTRABACKUP = "extrabackup";
	private static final String SETTING_EXTRABACKUPPATH = "extrabackuppath";
	private static final String SETTING_PANDOCPATH = "pandocpath";
	private static final String SETTING_HIGHLIGHTSEARCHRESULTS = "highlightsearchresults";
	private static final String SETTING_HIGHLIGHTSEARCHSTYLE = "highlightsearchstyle";
	private static final String SETTING_HIGHLIGHTKEYWORDSTYLE = "highlightkeywordstyle";
	private static final String SETTING_HIGHLIGHTLIVESEARCHSTYLE = "highlightlivesearchstyle";
	private static final String SETTING_HIGHLIGHTKEYWORDS = "highlightkeywords";
	private static final String SETTING_SHOWHIGHLIGHTBACKGROUND = "showhighlightbackground";
	private static final String SETTING_SHOWHIGHLIGHTKEYWORDBACKGROUND = "showhighlightkeywordbackground";
	private static final String SETTING_SHOWHIGHLIGHTLIVESEARCHBACKGROUND = "showhighlightlivesearchbackground";
	private static final String SETTING_HIGHLIGHTBACKGROUNDCOLOR = "highlightbackgroundcolor";
	private static final String SETTING_HIGHLIGHTKEYWORDBACKGROUNDCOLOR = "highlightkeywordbackgroundcolor";
	private static final String SETTING_HIGHLIGHTLIVESEARCHBACKGROUNDCOLOR = "highlightlivesearchbackgroundcolor";
	private static final String SETTING_SHOWSEARCHENTRY = "showsearchentry";
	private static final String SETTING_SEARCHTIME = "searchtime";
	private static final String SETTING_SEARCHLOG = "searchlog";
	private static final String SETTING_SEARCHCOMBOTIME = "searchcombotime";
	private static final String SETTING_SEARCHDATETIME = "searchdatetime";
	private static final String SETTING_SUPFOOTNOTE = "supfootnote";
	private static final String SETTING_FOOTNOTEBRACES = "footnotebraces";
	private static final String SETTING_JUMPFOOTNOTE = "jumpfootnote";
	private static final String SETTING_AUTOUPDATE = "autoupdate";
	private static final String SETTING_AUTONIGHTLYUPDATE = "autonightlyupdate";
	private static final String SETTING_SEARCHALWAYSSYNONYMS = "searchalwayssynonyms";
	private static final String SETTING_SEARCHALWAYSACCENTINSENSITIVE = "searchalwaysaccentinsensitive";
	private static final String SETTING_SHOWICONS = "showtoolbar";
	private static final String SETTING_SHOWALLICONS = "showallicons";
	private static final String SETTING_SHOWENTRYHEADLINE = "showentryheadline";
	private static final String SETTING_FILLEMPTYPLACES = "fillemptyplaces";
	private static final String SETTING_MANUALTIMESTAMP = "manualtimestamp";
	private static final String SETTING_SHOWSYNONYMSINTABLE = "showsynintable";
	private static final String SETTING_ADDALLTOHISTORY = "addalltohistory";
	private static final String SETTING_LASTUSEDBIBTEXFILE = "lastusedbibtexfile";
	private static final String SETTING_LASTUSEDBIBTEXFORMAT = "lastusedbibtexformat";
	private static final String SETTING_LASTOPENEDIMPORTDIR = "lastopenedimportdir";
	private static final String SETTING_LASTOPENEDEXPORTDIR = "lastopenedexportdir";
	private static final String SETTING_LASTOPENEDIMAGEDIR = "lastopenedimagedir";
	private static final String SETTING_LASTOPENEDATTACHMENTDIR = "lastopenedattachmentdir";
	private static final String SETTING_ALWAYSMACSTYLE = "alwaysmacstyle";
	private static final String SETTING_SHOWICONTEXT = "showicontext";
	private static final String SETTING_USEMACBACKGROUNDCOLOR = "usemacbackgroundcolor";
	private static final String SETTING_EXPORTPARTS = "exportparts";
	private static final String SETTING_EXPORTFORMAT = "exportformat";
	private static final String SETTING_DESKTOPEXPORTFORMAT = "desktopexportformat";
	private static final String SETTING_DESKTOPCOMMENTEXPORT = "desktopcommentexport";
	private static final String SETTING_DISPLAYEDTOOLBARICONS = "displayedtoolbaricons";
	private static final String SETTING_MINIMIZETOTRAY = "minimizetotray";
	private static final String SETTING_COPYPLAIN = "copyplain";
	private static final String SETTING_RECENT_DOC = "recentDoc";
	private static final String SETTING_LOCALE = "locale";
	private static final String SETTING_REMOVELINESFORDESKTOPEXPORT = "removelinesfordeskopexport";
	private static final String SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG = "showmultipledesktopoccurencesdlg";
	private static final String SETTING_TOCFORDESKTOPEXPORT = "tocfordesktopexport";
	private static final String SETTING_GETLASTUSEDDESKTOPNUMBER = "lastuseddesktopnumber";
	private static final String SETTING_LATEXEXPORTFOOTNOTE = "latexexportfootnote";
	private static final String SETTING_LATEXEXPORTFORMTAG = "latexexportformtag";
	private static final String SETTING_LATEXEXPORTSHOWAUTHOR = "latexexportshowauthor";
	private static final String SETTING_LATEXEXPORTSHOWMAIL = "latexexportshowmail";
	private static final String SETTING_LATEXEXPORTCONVERTQUOTES = "latexexportconvertquotes";
	private static final String SETTING_LATEXEXPORTCENTERFORM = "latexexportcenterform";
	private static final String SETTING_LATEXEXPORTLASTUSEDBIBSTYLE = "latexexportlastusedbibstyle";
	private static final String SETTING_LATEXEXPORTDOCUMENTCLASS = "latexexportdocumentclass";
	private static final String SETTING_LATEXEXPORTAUTHORVALUE = "latexexportauthorvalue";
	private static final String SETTING_LATEXEXPORTMAILVALUE = "latexexportmailvalue";
	private static final String SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS = "latexexportremovenonstandardtags";
	private static final String SETTING_LATEXEXPORTTABLESTATSTYLE = "latexexporttablestatstyle";
	private static final String SETTING_LATEXEXPORTNOPREAMBLE = "latexexportnopreamble";
	private static final String SETTING_LATEXEXPORTCONVERTUMLAUT = "latexexportconvertumlaut";
	private static final String SETTING_ICONTHEME = "icontheme";
	private static final String SETTING_SHOWUPDATEHINTVERSION = "showUpdateHintVersion";
	private static final String SETTING_USEXDGOPEN = "usexdgopen";
	private static final String SETTING_MANLINKCOLOR = "manlinkcolor";
	private static final String SETTING_FNLINKCOLOR = "footnotelinkcolor";
	private static final String SETTING_LINKCOLOR = "linkcolor";
	private static final String SETTING_DESKTOPSHOWCOMMENTS = "desktopshowcomments";
	private static final String SETTING_AUTOCOMPLETETAGS = "autocompletetags";
	private static final String SETTING_MARKDOWNACTIVATED = "markdownactivated";
	private static final String SETTING_TABLEHEADERCOLOR = "tableheadercolor";
	private static final String SETTING_APPENDIXBACKGROUNDCOLOR = "appendixlistbackgroundcolor";
	private static final String SETTING_TABLEEVENROWCOLOR = "tableevenrowcolor";
	private static final String SETTING_TABLEODDROWCOLOR = "tableoddrowcolor";
	private static final String SETTING_SHOWTABLEBORDER = "showtableborder";
	private static final String SETTING_ENTRYHEADERBACKGROUNDCOLOR = "entryheaderbackgroundcolor";
	private static final String SETTING_QUOTEBACKGROUNDCOLOR = "quotebackgroundcolor";
	private static final String SETTING_MAINBACKGROUNDCOLOR = "mainbackgroundcolor";
	private static final String SETTING_CONTENTBACKGROUNDCOLOR = "contenbackgroundcolor";
	private static final String SETTING_SEARCHFRAMESPLITLAYOUT = "searchframesplitlayout";
	private static final String SETTING_CUSTOMCSSENTRY = "customcssentry";
	private static final String SETTING_CUSTOMCSSDESKTOP = "customcssdesktop";
	private static final String SETTING_USECUSTOMCSSENTRY = "usecustomcssentry";
	private static final String SETTING_USECUSTOMCSSDESKTOP = "usecustomcssdesktop";
	private static final String SETTING_SHOWLUHMANNENTRYNUMBER = "showluhmannentrynumber";
	private static final String SETTING_SHOWDESKTOPENTRYNUMBER = "showdesktopentrynumber";
	private static final String SETTING_LASTUSEDSETBIBKEYCHOICE = "lastusedbibkeychoice";
	private static final String SETTING_LASTUSEDSETBIBKEYTYPE = "lastusedbibkeytype";
	private static final String SETTING_LASTUSEDSETBIBIMPORTSOURCE = "lastusedbibimportsource";
	private static final String SETTING_SHOWALLLUHMANN = "showallluhmann";
	private static final String SETTING_SHOWLUHMANNICONINDESK = "showluhmanniconindesk";
	private static final String SETTING_LUHMANNTREEEXPANDLEVEL = "luhmanntreeexpandlevel";
	private static final String SETTING_SEARCHWITHOUTFORMATTAGS = "searchwithoutformattags";
	private static final String SETTING_MAKELUHMANNCOLUMNSORTABLE = "makeluhmanncolumnsortable";
	private static final String SETTING_TABLEROWSORTING = "tablerowsorting";

	/**
	 * get the strings for file descriptions from the resource map
	 */
	private org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
			.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
			.getResourceMap(ZettelkastenView.class);

	/**
	 * This class stores all relevant settings for the Zettelkasten.<br>
	 * <br>
	 * We combine several xml-files and compress them into a single zip-container,
	 * named "zettelkasten-settings.zks3" and "zettelkasten-data.zkd3".
	 */
	public Settings() {
		// Init settings file and acceleratorKeys.
		zipSettingsFile = locateSettingsZipFileWithName("zettelkasten-settings.zks3");
		if (zipSettingsFile == null || !loadZettelkastenSettingsFile(zipSettingsFile)) {
			resetSettingsSettingsDocuments();
		}

		// Init foreignWordsFile, synonyms, autoKorrekt, steno objects.
		zipMetadataFile = locateSettingsZipFileWithName("zettelkasten-data.zkd3");
		if (zipMetadataFile == null || !loadZettelkastenMetadataFile(zipMetadataFile)) {
			resetMetadataSettingsDocuments();
		}

		// Always init default settings of missing fields.
		initDefaultSettingsIfMissing();
	}

	/**
	 * Returns the settings file to use based on the location priority. Returns null
	 * if failed to open.<br>
	 * <br>
	 * Zettelkasten settings location priority:<br>
	 * 1. Current directory from which Zettelkasten started.<br>
	 * 2. Zettelkasten directory in the user's home directory.<br>
	 * <br>
	 */
	private File locateSettingsZipFileWithName(String filename) {
		Path currentDir = Paths.get(System.getProperty("user.dir"));
		Path currentDirFilePath = Paths.get(currentDir.toString(), filename);
		File currentDirFile = currentDirFilePath.toFile();
		if (currentDirFile.exists()) {
			// If a settings file in the current dir file exists, use it.
			return currentDirFile;
		}

		// Fallback to Zettelkasten directory in the user's home directory.
		Path userDir = Paths.get(System.getProperty("user.home"));
		Path userDirZettelkastenDirPath = Paths.get(userDir.toString(), ZETTELKASTEN_DEFAULT_DIR_NAME);
		File userDirZettelkastenDirFile = userDirZettelkastenDirPath.toFile();

		// If that directory doesn't exist, try to create it.
		if (!userDirZettelkastenDirFile.exists()) {
			try {
				if (!userDirZettelkastenDirFile.mkdir()) {
					Constants.zknlogger.log(Level.SEVERE,
							"Failed to create Zettelkasten directory in the user directoy: {0}",
							userDirZettelkastenDirPath.toString());
					return null;
				}
			} catch (SecurityException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return null;
			}
		}

		// Return settings inside the Zettelkasten directory in the user's home
		// directory.
		Path settingsPath = Paths.get(userDir.toString(), ZETTELKASTEN_DEFAULT_DIR_NAME, filename);
		return settingsPath.toFile();
	}

	/**
	 * @return filepath to the zettelkasten-data.zkd3 file.
	 */
	public File getMetadataFile() {
		return zipMetadataFile;
	}

	/**
	 * @return filepath to the zettelkasten-settings.zks3 file.
	 */
	public File getSettingsFile() {
		return zipSettingsFile;
	}

	public AcceleratorKeys getAcceleratorKeys() {
		return acceleratorKeys;
	}

	public Synonyms getSynonyms() {
		return synonyms;
	}

	public AutoKorrektur getAutoKorrektur() {
		return autoKorrekt;
	}

	public StenoData getStenoData() {
		return steno;
	}

	// Named like this because we have a two settings file, and one is named
	// settings.
	private void resetSettingsSettingsDocuments() {
		// Create the empty documents.
		settingsFile = new Document(new Element("settings"));
		acceleratorKeys = new AcceleratorKeys();
	}

	// Named like this because we have a two settings file, and one is named
	// settings.
	private void resetMetadataSettingsDocuments() {
		// Create the empty documents.
		foreignWordsFile = new Document(new Element("foreignwords"));
		synonyms = new Synonyms();
		autoKorrekt = new AutoKorrektur();
		steno = new StenoData();
	}

	/**
	 * Inits all documents, i.e. creates new document elements
	 */
	public void resetSettings() {
		resetSettingsSettingsDocuments();
		resetMetadataSettingsDocuments();
		initDefaultSettingsIfMissing();
	}

	/**
	 * This method adds the file from the filepath {@code fp} to the list 
         * of recent documents and rotates that list, if necessary.
	 *
	 * @param fp the filepath to the document that should be added to the 
         *           list of recent documents
	 */
	public void addToRecentDocs(String fp) {
		// check for valid parameter
		if (fp == null || fp.isEmpty()) {
			return;
		}
		// check whether file exists
		File dummy = new File(fp);
		if (!dummy.exists()) {
			return;
		}
		// create linked list
		LinkedList<String> recdocs = new LinkedList<>();
		// add new filepath to linked list
		recdocs.add(fp);
		// iterate all current recent documents
		for (int cnt = 1; cnt <= RECENT_DOC_COUNT; cnt++) {
			// retrieve recent document
			String recentDoc = getRecentDoc(cnt);
			// check whether the linked list already contains such a document
			if (recentDoc != null && !recentDoc.isEmpty()) {
				// check for existing file
				dummy = new File(recentDoc);
				// if not, add it to the list
				if (dummy.exists() && !recdocs.contains(recentDoc)) {
					recdocs.add(recentDoc);
				}
			}
		}
		// iterate all current recent documents again
		for (int cnt = 1; cnt <= RECENT_DOC_COUNT; cnt++) {
			// check for valid bounds of linked list
			if (recdocs.size() >= cnt) {
				// and set recent document
				setRecentDoc(cnt, recdocs.get(cnt - 1));
			} // else fill remaining recent documents with empty strings
			else {
				setRecentDoc(cnt, "");
			}
		}
	}

	/**
	 * Retrieves the recent document at the position {@code nr}. Returns
	 * {@code null} if recent document does not exist or is empty
	 *
	 * @param nr the number of the requested recent document. use a value from 1 to
	 *           {@link #RECENT_DOC_COUNT recentDocCount}.
	 * @return the recent document (the file path) as string, or {@code null} if no
	 *         such element or path exists.
	 */
	public String getRecentDoc(int nr) {
		String value = genericStringGetter(SETTING_RECENT_DOC + String.valueOf(nr), "");
		if (value.isEmpty()) {
			return null;
		}
		return value;
	}

	/**
	 * Add a new recent document to the position {@code nr} in the list of recent
	 * documents.
	 *
	 * @param nr the number of the requested recent document. use a value from 1 to
	 *           {@link #RECENT_DOC_COUNT recentDocCount}.
	 * @param fp the filepath to the recently used document as string
	 */
	public void setRecentDoc(int nr, String fp) {
		genericStringSetter(SETTING_RECENT_DOC + String.valueOf(nr), fp);
	}

	public Color getTableGridColor() {
		if (isMacStyle()) {
			return Constants.gridcolortransparent;
		}
		return ((getShowGridHorizontal() || getShowGridVertical()) ? Constants.gridcolor
				: Constants.gridcolortransparent);
	}

	/**
	 * If an entry-number was passed as paramter, use this method to store the
	 * entry-number, so the entry can be displayed immediately after opening a data
	 * file. Use -1 to indicate that no parameter-entry-number should be set
	 *
	 * @return the entry-number which was passed as parameter, or -1 if no such
	 *         paramter was passed
	 */
	public int getStartupEntryFromCommandLine() {
		return commandLineInitialEntryNumber;
	}

	/**
	 * If an entry-number was passed as parameter, use this method to store the
	 * entry-number, so the entry can be displayed immediately after opening a data
	 * file. Use -1 to indicate that no parameter-entry-number should be set
	 *
	 * @param nr the entry-number which was passed as parameter, or -1 if no such
	 *           paramter was passed
	 */
	public void setInitialParamZettel(int nr) {
		commandLineInitialEntryNumber = nr;
	}

	/**
	 * Create all the used XML elements in settingsFile,
	 * setting them to the default value if missing.
	 * 
	 * We do this because when loading old settings file, it might be missing new
	 * settings elements. This ensures some compatibility to older/news
	 * settings-file-versions.
	 */
	public void initDefaultSettingsIfMissing() {
		acceleratorKeys.initDefaultAcceleratorKeysIfMissing();

		for (int cnt = 0; cnt < RECENT_DOC_COUNT; cnt++) {
			genericElementInitIfMissing(SETTING_RECENT_DOC + String.valueOf(cnt + 1), "");
		}

		{
			// Install FlatLafs Light and Dark with Ubuntu accent colour (Todo: make colors / themeing flexible, not hardcoded)
			UIManager.installLookAndFeel("FlatLightLaf", "com.formdev.flatlaf.FlatLightLaf");
			FlatLightLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#ea5420"));
			UIManager.installLookAndFeel("FlatDarkLaf", "com.formdev.flatlaf.FlatDarkLaf");
			FlatDarkLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#ea5420"));
			// Retrieve all installed LookAndFeels
			UIManager.LookAndFeelInfo[] installed_laf = UIManager.getInstalledLookAndFeels();
			boolean lafAquaFound = false;
			String aquaclassname = "";
			String flatlightlafclassname = "";

			// Give Aqua and FlatLightLaf proper classnames
			for (UIManager.LookAndFeelInfo laf : installed_laf) {
				if (laf.getName().equalsIgnoreCase("mac os x") || laf.getClassName().contains("Aqua")) {
					lafAquaFound = true;
					aquaclassname = laf.getClassName();
				}
				if (laf.getName().equalsIgnoreCase("FlatLightLaf") || laf.getClassName().contains("com.formdev.flatlaf.FlatLightLaf")) {
					flatlightlafclassname = laf.getClassName();
				}
			}
			// Set Aqua as default on MacOS, FlatLightLaf on Linux and System Laf on Windows
			if (lafAquaFound) {
				genericElementInitIfMissing(SETTING_LAF, aquaclassname);
			} else if (PlatformUtil.isLinux()) {
				genericElementInitIfMissing(SETTING_LAF, flatlightlafclassname);
			}
			else {
				genericElementInitIfMissing(SETTING_LAF, UIManager.getSystemLookAndFeelClassName());
			}
		}

		// Default font is the "SansSerif" logical font name.
		String defaultFont = Font.SANS_SERIF;

		String pandoc = "pandoc";
		if (PlatformUtil.isMacOS()) {
			pandoc = "/usr/local/bin/pandoc";
		} else if (PlatformUtil.isLinux()) {
			pandoc = "/usr/bin/pandoc";
		}
		genericElementInitIfMissing(SETTING_PANDOCPATH, pandoc);
		genericElementInitIfMissing(SETTING_DISPLAYEDTOOLBARICONS,
				PlatformUtil.isMacOS() ? "1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,1,0,1,0,0,1,1,1,1,1"
						: "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1");
		genericElementInitIfMissing(SETTING_LOCALE, Locale.getDefault().getLanguage());
		genericElementInitIfMissing(SETTING_FILEPATH, "");
		genericElementInitIfMissing(SETTING_SEARCHFRAMESPLITLAYOUT, String.valueOf(JSplitPane.VERTICAL_SPLIT));
		genericElementInitIfMissing(SETTING_CUSTOMCSSENTRY, "");
		genericElementInitIfMissing(SETTING_CUSTOMCSSDESKTOP, "");
		genericElementInitIfMissing(SETTING_GETLASTUSEDDESKTOPNUMBER, "0");
		genericElementInitIfMissing(SETTING_AUTOCOMPLETETAGS, "1");
		genericElementInitIfMissing(SETTING_MARKDOWNACTIVATED, "0");
		genericElementInitIfMissing(SETTING_LASTOPENEDIMPORTDIR, "");
		genericElementInitIfMissing(SETTING_LASTOPENEDEXPORTDIR, "");
		genericElementInitIfMissing(SETTING_LASTOPENEDIMAGEDIR, "");
		genericElementInitIfMissing(SETTING_SHOWICONTEXT, "1");
		genericElementInitIfMissing(SETTING_USEMACBACKGROUNDCOLOR, "0");
		genericElementInitIfMissing(SETTING_LASTOPENEDATTACHMENTDIR, "");
		genericElementInitIfMissing(SETTING_AUTOBACKUP, "1");
		genericElementInitIfMissing(SETTING_SHOWALLLUHMANN, "0");
		genericElementInitIfMissing(SETTING_SHOWLUHMANNICONINDESK, "1");
		genericElementInitIfMissing(SETTING_EXTRABACKUP, "0");
		genericElementInitIfMissing(SETTING_ALWAYSMACSTYLE, "0");
		genericElementInitIfMissing(SETTING_MINIMIZETOTRAY, "0");
		genericElementInitIfMissing(SETTING_ADDALLTOHISTORY, "0");
		genericElementInitIfMissing(SETTING_COPYPLAIN, "0");
		genericElementInitIfMissing(SETTING_EXTRABACKUPPATH, "");
		genericElementInitIfMissing(SETTING_FILLEMPTYPLACES, "0");
		genericElementInitIfMissing(SETTING_MANUALTIMESTAMP, "0");
		genericElementInitIfMissing(SETTING_SEARCHTIME, "");
		genericElementInitIfMissing(SETTING_SEARCHALWAYSSYNONYMS, "1");
		genericElementInitIfMissing(SETTING_SEARCHALWAYSACCENTINSENSITIVE, "0");
		genericElementInitIfMissing(SETTING_SHOWSYNONYMSINTABLE, "0");
		genericElementInitIfMissing(SETTING_SHOWICONS, "1");
		genericElementInitIfMissing(SETTING_SHOWALLICONS, "1");
		genericElementInitIfMissing(SETTING_SHOWENTRYHEADLINE, "1");
		genericElementInitIfMissing(SETTING_ICONTHEME, "0");
		genericElementInitIfMissing(SETTING_FOOTNOTEBRACES, "1");
		genericElementInitIfMissing(SETTING_SHOWUPDATEHINTVERSION, "0");
		genericElementInitIfMissing(SETTING_USECUSTOMCSSENTRY, "0");
		genericElementInitIfMissing(SETTING_USECUSTOMCSSDESKTOP, "0");
		genericElementInitIfMissing(SETTING_USEXDGOPEN, "1");
		genericElementInitIfMissing(SETTING_MANLINKCOLOR, "0033cc");
		genericElementInitIfMissing(SETTING_FNLINKCOLOR, "0033cc");
		genericElementInitIfMissing(SETTING_LINKCOLOR, "003399");
		genericElementInitIfMissing(SETTING_APPENDIXBACKGROUNDCOLOR, "ffffff");
		genericElementInitIfMissing(SETTING_TABLEHEADERCOLOR, "e4e4e4");
		genericElementInitIfMissing(SETTING_TABLEEVENROWCOLOR, "eeeeee");
		genericElementInitIfMissing(SETTING_ENTRYHEADERBACKGROUNDCOLOR, "555555");
		genericElementInitIfMissing(SETTING_QUOTEBACKGROUNDCOLOR, "f2f2f2");
		genericElementInitIfMissing(SETTING_MAKELUHMANNCOLUMNSORTABLE, "0");
		genericElementInitIfMissing(SETTING_MAINBACKGROUNDCOLOR, "ffffff");
		genericElementInitIfMissing(SETTING_CONTENTBACKGROUNDCOLOR, "ffffff");
		genericElementInitIfMissing(SETTING_TABLEODDROWCOLOR, "f8f8f8");
		genericElementInitIfMissing(SETTING_SHOWTABLEBORDER, "1");
		genericElementInitIfMissing(SETTING_SEARCHWITHOUTFORMATTAGS, "1");
		genericElementInitIfMissing(SETTING_SHOWLUHMANNENTRYNUMBER, "0");
		genericElementInitIfMissing(SETTING_SHOWDESKTOPENTRYNUMBER, "0");
		genericElementInitIfMissing(SETTING_DESKTOPSHOWCOMMENTS, String.valueOf(Constants.DESKTOP_WITH_COMMENTS));
		genericElementInitIfMissing(SETTING_LASTUSEDBIBTEXFORMAT, "0");
		genericElementInitIfMissing(SETTING_SHOWHIGHLIGHTBACKGROUND, "1");
		genericElementInitIfMissing(SETTING_SHOWHIGHLIGHTKEYWORDBACKGROUND, "1");
		genericElementInitIfMissing(SETTING_SHOWHIGHLIGHTLIVESEARCHBACKGROUND, "1");
		genericElementInitIfMissing(SETTING_SEARCHCOMBOTIME, "0");
		genericElementInitIfMissing(SETTING_SEARCHDATETIME, "");
		genericElementInitIfMissing(SETTING_SEARCHLOG, "0");
		genericElementInitIfMissing(SETTING_HIGHLIGHTSEARCHRESULTS, "0");
		genericElementInitIfMissing(SETTING_HIGHLIGHTKEYWORDS, "0");
		genericElementInitIfMissing(SETTING_SHOWSEARCHENTRY, "0");
		genericElementInitIfMissing(SETTING_SUPFOOTNOTE, "1");
		genericElementInitIfMissing(SETTING_TABLEROWSORTING, "");
		genericElementInitIfMissing(SETTING_JUMPFOOTNOTE, "0");
		genericElementInitIfMissing(SETTING_STARTUPENTRY, "1");
		genericElementInitIfMissing(SETTING_SHOWATSTARTUP, "0");
		genericElementInitIfMissing(SETTING_LOGKEYWORDLIST, SETTING_LOGKEYWORDLIST_OR);
		genericElementInitIfMissing(SETTING_SHOWGRID_HORIZONTAL, "0");
		genericElementInitIfMissing(SETTING_SHOWGRID_VERTICAL, "0");
		genericElementInitIfMissing(SETTING_CELLSPACING, "1,1");
		genericElementInitIfMissing(SETTING_SPELLCORRECT, "0");
		genericElementInitIfMissing(SETTING_STENOACTIVATED, "0");
		genericElementInitIfMissing(SETTING_HIGHLIGHTWHOLEWORD, "0");
		genericElementInitIfMissing(SETTING_HIGHLIGHTWHOLEWORDSEARCH, "0");
		genericElementInitIfMissing(SETTING_QUICKINPUT, "0");
		genericElementInitIfMissing(SETTING_AUTOUPDATE, "1");
		genericElementInitIfMissing(SETTING_TOCFORDESKTOPEXPORT, "0");
		genericElementInitIfMissing(SETTING_REMOVELINESFORDESKTOPEXPORT, "1");
		genericElementInitIfMissing(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG, "0");
		genericElementInitIfMissing(SETTING_AUTONIGHTLYUPDATE, "0");
		genericElementInitIfMissing(SETTING_QUICKINPUTEXTENDED, "0");
		genericElementInitIfMissing(SETTING_IMGRESIZE, "1");
		genericElementInitIfMissing(SETTING_IMGRESIZEWIDTH, "400");
		genericElementInitIfMissing(SETTING_IMGRESIZEHEIGHT, "400");
		genericElementInitIfMissing(SETTING_TABLEFONTSIZE, "0");
		genericElementInitIfMissing(SETTING_DESKTOPOUTLINEFONTSIZE, "0");
		genericElementInitIfMissing(SETTING_TEXTFIELDFONTSIZE, "0");
		genericElementInitIfMissing(SETTING_LASTUSEDSETBIBKEYCHOICE, String.valueOf(CSetBibKey.CHOOSE_BIBKEY_MANUAL));
		genericElementInitIfMissing(SETTING_LASTUSEDSETBIBKEYTYPE, String.valueOf(CSetBibKey.CHOOSE_BIBKEY_MANUAL));
		genericElementInitIfMissing(SETTING_LASTUSEDSETBIBIMPORTSOURCE, String.valueOf(CImportBibTex.BIBTEX_SOURCE_DB));
		genericElementInitIfMissing(SETTING_LATEXEXPORTFOOTNOTE, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTFORMTAG, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTSHOWAUTHOR, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS, "1");
		genericElementInitIfMissing(SETTING_LATEXEXPORTNOPREAMBLE, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTCONVERTUMLAUT, "1");
		genericElementInitIfMissing(SETTING_LATEXEXPORTTABLESTATSTYLE, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTSHOWMAIL, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTCONVERTQUOTES, "1");
		genericElementInitIfMissing(SETTING_LATEXEXPORTCENTERFORM, "1");
		genericElementInitIfMissing(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTDOCUMENTCLASS, "0");
		genericElementInitIfMissing(SETTING_LATEXEXPORTAUTHORVALUE, "");
		genericElementInitIfMissing(SETTING_LATEXEXPORTMAILVALUE, "");
		genericElementInitIfMissing(SETTING_SEARCHWHERE, String.valueOf(Constants.SEARCH_CONTENT
				| Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS));
		genericElementInitIfMissing(SETTING_REPLACEWHERE, String.valueOf(Constants.SEARCH_CONTENT
				| Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS));
		genericElementInitIfMissing(SETTING_EXPORTPARTS, String.valueOf(Constants.EXPORT_TITLE
				| Constants.EXPORT_CONTENT | Constants.EXPORT_AUTHOR | Constants.EXPORT_REMARKS));
		genericElementInitIfMissing(SETTING_EXPORTFORMAT, String.valueOf(Constants.EXP_TYPE_DESKTOP_DOCX));
		genericElementInitIfMissing(SETTING_DESKTOPEXPORTFORMAT, String.valueOf(Constants.EXP_TYPE_DESKTOP_DOCX));
		genericElementInitIfMissing(SETTING_DESKTOPCOMMENTEXPORT, "0");
		genericElementInitIfMissing(SETTING_DESKTOPDISPLAYITEMS,
				String.valueOf(Constants.DESKTOP_SHOW_REMARKS | Constants.DESKTOP_SHOW_AUTHORS));
		genericElementInitIfMissing(SETTING_SEARCHWHAT, "");
		genericElementInitIfMissing(SETTING_REPLACEWHAT, "");
		genericElementInitIfMissing(SETTING_SEARCHOPTION, "0");
		genericElementInitIfMissing(SETTING_REPLACEOPTION, "0");
		genericElementInitIfMissing(SETTING_LASTUSEDBIBTEXFILE, "");
		genericElementInitIfMissing(SETTING_HIGHLIGHTBACKGROUNDCOLOR, "ffff66");
		genericElementInitIfMissing(SETTING_HIGHLIGHTKEYWORDBACKGROUNDCOLOR, "ffff66");
		genericElementInitIfMissing(SETTING_HIGHLIGHTLIVESEARCHBACKGROUNDCOLOR, "ffff66");
		genericElementInitIfMissing(SETTING_TABLEFONT, defaultFont);
		genericElementInitIfMissing(SETTING_DESKTOPOUTLINEFONT, defaultFont);
		genericElementInitIfMissing(SETTING_LUHMANNTREEEXPANDLEVEL, "-1");

		if (null == settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTSEARCHSTYLE)) {
			// create element for font
			Element el = new Element(SETTING_HIGHLIGHTSEARCHSTYLE);
			settingsFile.getRootElement().addContent(el);
			el.setAttribute("size", "0");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "0000ff");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTKEYWORDSTYLE)) {
			// create element for font
			Element el = new Element(SETTING_HIGHLIGHTKEYWORDSTYLE);
			settingsFile.getRootElement().addContent(el);
			el.setAttribute("size", "0");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "0000ff");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTLIVESEARCHSTYLE)) {
			// create element for font
			Element el = new Element(SETTING_HIGHLIGHTLIVESEARCHSTYLE);
			settingsFile.getRootElement().addContent(el);
			el.setAttribute("size", "0");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "0000ff");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_MAINFONT)) {
			// create element for font
			Element el = new Element(SETTING_MAINFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "11");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "000000");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_QUOTEFONT)) {
			// create element for font
			Element el = new Element(SETTING_QUOTEFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "11");
			el.setAttribute("color", "333333");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERFONT)) {
			// create element for font
			Element el = new Element(SETTING_ENTRYHEADERFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "10");
			el.setAttribute("color", "F4F4F4");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_AUTHORFONT)) {
			// create element for font
			Element el = new Element(SETTING_AUTHORFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "10");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "333333");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_CODEFONT)) {
			// create element for font
			Element el = new Element(SETTING_CODEFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "11");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "333333");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_REMARKSFONT)) {
			// create element for font
			Element el = new Element(SETTING_REMARKSFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "10");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "333333");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPHEADERFONT)) {
			// create element for font
			Element el = new Element(SETTING_DESKTOPHEADERFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "14");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "bold");
			el.setAttribute("color", "000000");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMHEADERFONT)) {
			// create element for font
			Element el = new Element(SETTING_DESKTOPITEMHEADERFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "12");
			el.setAttribute("style", "italic");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "555555");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMFONT)) {
			// create element for font
			Element el = new Element(SETTING_DESKTOPITEMFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "10");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "808080");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPCOMMENTFONT)) {
			// create element for font
			Element el = new Element(SETTING_DESKTOPCOMMENTFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "9");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "normal");
			el.setAttribute("color", "333333");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_TITLEFONT)) {
			// create element for font
			Element el = new Element(SETTING_TITLEFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "13");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "bold");
			el.setAttribute("color", "800000");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_APPENDIXHEADERFONT)) {
			// create element for font
			Element el = new Element(SETTING_APPENDIXHEADERFONT);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "13");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "bold");
			el.setAttribute("color", "555555");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_HEADERFONT1)) {
			// create element for font
			Element el = new Element(SETTING_HEADERFONT1);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "12");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "bold");
			el.setAttribute("color", "000040");
		}

		if (null == settingsFile.getRootElement().getChild(SETTING_HEADERFONT2)) {
			// create element for font
			Element el = new Element(SETTING_HEADERFONT2);
			settingsFile.getRootElement().addContent(el);
			el.setText(defaultFont);
			el.setAttribute("size", "11");
			el.setAttribute("style", "normal");
			el.setAttribute("weight", "bold");
			el.setAttribute("color", "000000");
		}
	}

	private boolean loadZettelkastenSettingsFile(File zipSettingsPath) {
		if (!zipSettingsPath.exists()) {
			Constants.zknlogger.log(Level.WARNING, String
					.format("Could not open settings file: filepath [%s] does not exist.", zipSettingsPath.getPath()));
			return false;
		}
		Constants.zknlogger.log(Level.INFO, String.format("Found settings file [%s]", zipSettingsPath.getPath()));

		try {
			settingsFile = FileOperationsUtil.readXMLFileFromZipFile(zipSettingsPath, Constants.settingsFileName);
			acceleratorKeys.setDocument(AcceleratorKeys.MAINKEYS,
					FileOperationsUtil.readXMLFileFromZipFile(zipSettingsPath, Constants.acceleratorKeysMainName));
			acceleratorKeys.setDocument(AcceleratorKeys.NEWENTRYKEYS,
					FileOperationsUtil.readXMLFileFromZipFile(zipSettingsPath, Constants.acceleratorKeysNewEntryName));
			acceleratorKeys.setDocument(AcceleratorKeys.DESKTOPKEYS,
					FileOperationsUtil.readXMLFileFromZipFile(zipSettingsPath, Constants.acceleratorKeysDesktopName));
			acceleratorKeys.setDocument(AcceleratorKeys.SEARCHRESULTSKEYS, FileOperationsUtil
					.readXMLFileFromZipFile(zipSettingsPath, Constants.acceleratorKeysSearchResultsName));
		} catch (Exception e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	private boolean loadZettelkastenMetadataFile(File zipDataFile) {
		if (!zipDataFile.exists()) {
			Constants.zknlogger.log(Level.WARNING, String
					.format("Could not open metadata file: filepath [%s] does not exist.", zipDataFile.getPath()));
			return false;
		}
		Constants.zknlogger.log(Level.INFO, String.format("Found metadata file [%s]", zipDataFile.getPath()));

		try {
			foreignWordsFile = FileOperationsUtil.readXMLFileFromZipFile(zipDataFile, Constants.foreignWordsName);
			synonyms.setDocument(FileOperationsUtil.readXMLFileFromZipFile(zipDataFile, Constants.synonymsFileName));
			autoKorrekt.setDocument(
					FileOperationsUtil.readXMLFileFromZipFile(zipDataFile, Constants.autoKorrekturFileName));
			steno.setDocument(FileOperationsUtil.readXMLFileFromZipFile(zipDataFile, Constants.stenoFileName));
		} catch (Exception e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	/*
	 * createZettelkastenSettingsZipFile is responsible for creating the
	 * Zettelkasten Settings zip file. Returns true iff successful.
	 * 
	 * The Zettelkasten Settings file is a zip file with the following files inside:
	 * Constants.settingsFileName, Constants.acceleratorKeysMainName,
	 * Constants.acceleratorKeysNewEntryName, Constants.acceleratorKeysDesktopName,
	 * and Constants.acceleratorKeysSearchResultsName.
	 */
	private boolean createZettelkastenSettingsZipFile() {
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipSettingsFile))) {
			XMLOutputter out = new XMLOutputter();

			// Add settings file.
			zip.putNextEntry(new ZipEntry(Constants.settingsFileName));
			out.output(settingsFile, zip);
			// Add main accelerator keys.
			zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysMainName));
			out.output(acceleratorKeys.getDocument(AcceleratorKeys.MAINKEYS), zip);
			// Add New Entry accelerator keys.
			zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysNewEntryName));
			out.output(acceleratorKeys.getDocument(AcceleratorKeys.NEWENTRYKEYS), zip);
			// Add Desktop accelerator keys.
			zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysDesktopName));
			out.output(acceleratorKeys.getDocument(AcceleratorKeys.DESKTOPKEYS), zip);
			// Add Search Results accelerator keys.
			zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysSearchResultsName));
			out.output(acceleratorKeys.getDocument(AcceleratorKeys.SEARCHRESULTSKEYS), zip);
		} catch (IOException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	/**
	 * saveZettelkastenSettingsFile saves the Zettelkasten Settings file to
	 * {@code zipSettingsFile} and returns true iff successful. On failure, if
	 * existing file might have been corrupted, a message dialog informs the user
	 * where to find the backup file of the original existing Zettelkasten Settings
	 * file.
	 * 
	 * Zip creation is handled by createZettelkastenSettingsZipFile.
	 */
	private boolean saveZettelkastenSettingsFile() {
		// Before creating the new zip file, create a temporary backup in case of
		// errors.
		File tmpDataFile = new File(zipSettingsFile.toString() + ".tmp");
		boolean ok = FileOperationsUtil.createFileCopyIfExists(zipSettingsFile, tmpDataFile);
		if (!ok) {
			// Avoid attempting to save file if we failed to create temporary backup. Return
			// error. Error is logged inside createFileCopyIfExists.
			return false;
		}

		// Create new zip file.
		ok = createZettelkastenSettingsZipFile();
		if (!ok) {
			// On failure, rename temporary file to a .backup file and tell user. The
			// temporary backup file might not exist due to a missing original file.
			try {
				if (tmpDataFile.exists()) {
					File checkbackup = FileOperationsUtil.getBackupFilePath(zipSettingsFile);
					tmpDataFile.renameTo(checkbackup);

					Constants.zknlogger.log(Level.INFO, "A backup of the settings was saved to {0}",
							checkbackup.toString());
					// Tell user that an error occurred.
					JOptionPane.showMessageDialog(null,
							resourceMap.getString("settingsSaveErrMsg", "\"" + checkbackup.getName() + "\""),
							resourceMap.getString("settingsSaveErrTitle"), JOptionPane.PLAIN_MESSAGE);
				}
			} catch (Exception e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			return false;
		}

		// In case of success, delete temporary backup file created in the beginning of
		// the function.
		try {
			if (tmpDataFile.exists()) {
				tmpDataFile.delete();
			}
		} catch (Exception e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			// Don't treat failure to delete temporary backup file as a failure to the
			// overall function.
		}
		return true;
	}

	/*
	 * createZettelkastenDataZipFile is responsible for creating the Zettelkasten
	 * Metadata zip file. Returns true iff successful.
	 * 
	 * The Zettelkasten Data file is a zip file with a separate file for the current
	 * state of each of the following class member variables: foreignWordsFile,
	 * synonyms, steno, and autoKorrekt.
	 */
	private boolean createZettelkastenMetadataZipFile() {
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipMetadataFile))) {
			XMLOutputter out = new XMLOutputter();

			// Add foreign words file.
			zip.putNextEntry(new ZipEntry(Constants.foreignWordsName));
			out.output(foreignWordsFile, zip);
			// Add synonyms file.
			zip.putNextEntry(new ZipEntry(Constants.synonymsFileName));
			out.output(synonyms.getDocument(), zip);
			// Add steno file.
			zip.putNextEntry(new ZipEntry(Constants.stenoFileName));
			out.output(steno.getDocument(), zip);
			// Add Korrektur file.
			zip.putNextEntry(new ZipEntry(Constants.autoKorrekturFileName));
			out.output(autoKorrekt.getDocument(), zip);
		} catch (Exception e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	/**
	 * saveZettelkastenDataFile saves the Zettelkasten Metadata file to
	 * zettelkastenDataFilepath and returns true iff successful. On failure, if
	 * existing file might have been corrupted, a message dialog informs the user
	 * where to find the backup file of the original existing Zettelkasten Metadata
	 * file.
	 * 
	 * Zip creation is handled by createZettelkastenDataZipFile.
	 * 
	 */
	private boolean saveZettelkastenMetadataFile() {
		// Before the main save action, create a temporary backup in case of any error
		// during save.
		File tmpDataFile = new File(zipMetadataFile.toString() + ".tmp");
		boolean ok = FileOperationsUtil.createFileCopyIfExists(zipMetadataFile, tmpDataFile);
		if (!ok) {
			// Avoid attempting to save file if we failed to create temporary backup. Return
			// error.
			return false;
		}

		// Create new zip file.
		ok = createZettelkastenMetadataZipFile();
		if (!ok) {
			// On failure, rename temporary file to a .backup file and tell user. The
			// temporary backup file might not exist due to a missing original file.
			try {
				if (tmpDataFile.exists()) {
					File checkbackup = FileOperationsUtil.getBackupFilePath(zipMetadataFile);
					tmpDataFile.renameTo(checkbackup);

					Constants.zknlogger.log(Level.INFO, "A backup of the meta-data was saved to {0}",
							checkbackup.toString());
					// Tell user that an error occurred.
					JOptionPane.showMessageDialog(null,
							resourceMap.getString("metadataSaveErrMsg", "\"" + checkbackup.getName() + "\""),
							resourceMap.getString("metadataSaveErrTitle"), JOptionPane.PLAIN_MESSAGE);
				}
			} catch (Exception e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			return false;
		}

		// In case of success, delete temporary backup file created in the beginning of
		// the function.
		try {
			if (tmpDataFile.exists()) {
				tmpDataFile.delete();
			}
		} catch (Exception e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			// Don't treat failure to delete temporary backup file as a failure to the
			// overall function.
		}
		return true;
	}

	/**
	 * Saves this class settings to the associated files and returns true iff
	 * successful. On failure, if existing files might have been corrupted, a
	 * message dialog informs the user where to find the backup file of the original
	 * existing files.
	 *
	 * @return success status
	 */
	public boolean saveSettingsToFiles() {
		boolean ok = true;
		// Keep track of whether any of the saves failed.
		ok = ok && saveZettelkastenSettingsFile();
		ok = ok && saveZettelkastenMetadataFile();

		Constants.zknlogger.log(Level.INFO, "Successfully saved the settings files [{0}] and [{1}].",
				new String[] { zipSettingsFile.toString(), zipMetadataFile.toString() });
		return ok;
	}

	/**
	 * Returns the main data file.
	 *
	 * @return the filepath of the main datafile, or null if none.
	 */
	public File getMainDataFile() {
		String value = genericStringGetter(SETTING_FILEPATH, "");
		if (value.isEmpty()) {
			return null;
		}
		return new File(value);
	}

	/**
	 * Sets the filepath of the main data file.
	 *
	 * @param file filepath of the currently used main datafile
	 */
	public void setMainDataFile(File file) {
		genericStringSetter(SETTING_FILEPATH, (file == null) ? "" : file.toString());
		// We always reset the startup entry when changing the main data file.
		setStartupEntry(-1);
	}

	/**
	 * Returns the name of the main data file without the extension. For example,
	 * a/b/test.zkn3 returns 'test'.
	 *
	 * @return name of the main data file without the extension
	 */
	public String getMainDataFileNameWithoutExtension() {
		File f = getMainDataFile();
		if (f != null && f.exists()) {
			String extension = FilenameUtils.getExtension(f.getName());
			if (("." + extension).equals(Constants.ZKN_FILEEXTENSION)) {
				return FilenameUtils.removeExtension(f.getName());
			}
		}
		return null;
	}

	public void setTableSorting(javax.swing.JTable[] tables) {
		Element el = settingsFile.getRootElement().getChild(SETTING_TABLEROWSORTING);
		if (el == null) {
			el = new Element(SETTING_TABLEROWSORTING);
			settingsFile.getRootElement().addContent(el);
		}
		// iterate all tables
		for (javax.swing.JTable t : tables) {
			// check if table is valid
			if (t != null) {
				// get sorter for each table
				javax.swing.RowSorter<? extends TableModel> sorter = t.getRowSorter();
				// get sort keys (column, sort order)
				List<? extends SortKey> sk = sorter.getSortKeys();
				if (sk != null && sk.size() > 0) {
					// get first element
					RowSorter.SortKey ssk = sk.get(0);
					// set sort column and sort order
					String value = String.valueOf(ssk.getColumn()) + "," + ssk.getSortOrder().toString();
					el.setAttribute(t.getName(), value);
				}
			}
		}
	}

	public RowSorter.SortKey getTableSorting(javax.swing.JTable table) {
		if (table != null) {
			Element el = settingsFile.getRootElement().getChild(SETTING_TABLEROWSORTING);
			if (el == null) {
				return null;
			}
			// get sorting attributes
			List<Attribute> attr = el.getAttributes();
			// check if we found any sorting attributes
			if (!attr.isEmpty()) {
				// find associated table attribute
				for (Attribute a : attr) {
					// found table attribute?
					if (a.getName().equals(table.getName())) {
						// get attributes
						String[] values = a.getValue().split(",");
						// sorted column
						int col = Integer.parseInt(values[0]);
						SortOrder so;
						// sort direction
						switch (values[1]) {
						case "DESCENDING":
							so = SortOrder.DESCENDING;
							break;
						case "ASCENDING":
							so = SortOrder.ASCENDING;
							break;
						default:
							so = SortOrder.UNSORTED;
							break;
						}
						// create and return sort key
						return new RowSorter.SortKey(col, so);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the filepath of the last used image path when inserting images to a
	 * new entry
	 *
	 * @return the filepath of the last opened image directory, or null if no
	 *         filepath was specified.
	 */
	public File getLastOpenedImageDir() {
		return genericDirGetter(SETTING_LASTOPENEDIMAGEDIR);
	}

	/**
	 * Sets the filepath of the last used image path when inserting images to a new
	 * entry
	 *
	 * @param fp the filepath of the last opened image directory
	 */
	public void setLastOpenedImageDir(File fp) {
		genericDirSetter(SETTING_LASTOPENEDIMAGEDIR, fp);
	}

	/**
	 * Retrieves the filepath of the last used import path when data was imported
	 *
	 * @return the filepath of the last opened import directory, or null if no
	 *         filepath was specified.
	 */
	public File getLastOpenedImportDir() {
		return genericDirGetter(SETTING_LASTOPENEDIMPORTDIR);
	}

	/**
	 * Sets the filepath of the last used import path when data was imported
	 *
	 * @param fp the filepath of the last opened import directory
	 */
	public void setLastOpenedImportDir(File fp) {
		genericDirSetter(SETTING_LASTOPENEDIMPORTDIR, fp);
	}

	/**
	 * Retrieves the filepath of the last used import path when data was imported
	 *
	 * @return the filepath of the last opened import directory, or null if no
	 *         filepath was specified.
	 */
	public File getLastOpenedExportDir() {
		return genericDirGetter(SETTING_LASTOPENEDEXPORTDIR);
	}

	/**
	 * Sets the filepath of the last used import path when data was imported
	 *
	 * @param fp the filepath of the last opened import directory
	 */
	public void setLastOpenedExportDir(File fp) {
		genericDirSetter(SETTING_LASTOPENEDEXPORTDIR, fp);
	}

	/**
	 * Retrieves the filepath of the last used image path when inserting images to a
	 * new entry
	 *
	 * @return the filepath of the last opened image directory, or null if no
	 *         filepath was specified.
	 */
	public File getLastOpenedAttachmentDir() {
		return genericDirGetter(SETTING_LASTOPENEDATTACHMENTDIR);
	}

	/**
	 * Sets the filepath of the last used image path when inserting images to a new
	 * entry
	 *
	 * @param fp the filepath of the last opened image directory
	 */
	public void setLastOpenedAttachmentDir(File fp) {
		genericDirSetter(SETTING_LASTOPENEDATTACHMENTDIR, fp);
	}

	/**
	 * Retrieves the filepath for the external backup when leaving the application
	 *
	 * @return filepath of the external backup, or null if no path was specified
	 */
	public File getExtraBackupDir() {
		return genericDirGetter(SETTING_EXTRABACKUPPATH);
	}

	/**
	 * Sets the filepath for the external backup when leaving the application
	 *
	 * @param value filepath of the external backup
	 */
	public void setExtraBackupPath(String value) {
		genericStringSetter(SETTING_EXTRABACKUPPATH, value);
	}

	/**
	 * Retrieves the filepath for the external converter tool "pandoc"
	 *
	 * @return the filepath for the external converter tool "pandoc"
	 */
	public String getPandocPath() {
		String value = genericStringGetter(SETTING_PANDOCPATH, "");
		if (value.isEmpty()) {
			return null;
		}
		return value;
	}

	/**
	 * Sets the filepath for the for the external converter tool "pandoc"
	 *
	 * @param fp the filepath to the external converter tool "pandoc"
	 */
	public void setPandocPath(String value) {
		genericStringSetter(SETTING_PANDOCPATH, value);
	}

	/**
	 * Retrieves the filepath of the last used BibTeX file. we need this path when
	 * exporting entries (from the desktop or the export-method from the main
	 * frame), and the user wants to create a separate BibTeX-File out of the
	 * authors that have been exported.
	 *
	 * @return the filepath of the last used bixb text file, or null if no path is
	 *         saved
	 */
	public File getLastUsedBibTexFile() {
		String value = genericStringGetter(SETTING_LASTUSEDBIBTEXFILE, "");
		if (value.isEmpty()) {
			return null;
		}
		return new File(value);
	}

	/**
	 * Sets the filepath of the last used BibTeX file. we need this path when
	 * exporting entries (from the desktop or the export-method from the main
	 * frame), and the user wants to create a separate BibTeX-File out of the
	 * authors that have been exported.
	 *
	 * @param fp the filepath of the last used BibTeX file
	 */
	public void setLastUsedBibTexFile(String fp) {
		genericStringSetter(SETTING_LASTUSEDBIBTEXFILE, fp);
	}

	/**
	 * Retrieves the image path, where images used in entries are stored. This is
	 * typically the directory "img", which is a sub directory of the filepath
	 * directory.
	 *
	 * @param userpath          a path to the user-defined directory for storing
	 *                          images. as default, use the
	 *                          {@code Daten.getUserImagePath()} method to retrieve
	 *                          this path.
	 * @param trailingSeparator if true, a file-separator-char will be appended, if
	 *                          false not
	 * @return the directory to the img-path, , or an empty string if no path was
	 *         found
	 */
	public String getImagePath(File userpath, boolean trailingSeparator) {
		return getImagePath(userpath, trailingSeparator, Constants.IMAGEPATH_SUBDIR);
	}

	/**
	 * Retrieves the image path, where images used in entries are stored. This is
	 * typically the directory "forms", which is a sub directory of the filepath
	 * directory.
	 *
	 * @param userpath          a path to the user-defined directory for storing
	 *                          images. as default, use the
	 *                          {@code Daten.getUserImagePath()} method to retrieve
	 *                          this path.
	 * @param trailingSeparator if true, a file-separator-char will be appended, if
	 *                          false not
	 * @return the directory to the img-path, , or an empty string if no path was
	 *         found
	 */
	public String getFormImagePath(File userpath, boolean trailingSeparator) {
		return getImagePath(userpath, trailingSeparator, Constants.FORMIMAGEPATH_SUBDIR);
	}

	/**
	 *
	 * @param userpath
	 * @param trailingSeparator
	 * @param subdir
	 * @return
	 */
	private String getImagePath(File userpath, boolean trailingSeparator, String subdir) {
		StringBuilder retval = new StringBuilder("");
		// check whether we have a user-defined attachment path. if yes,
		// use this as attachment-path, else get the base directory
		if (userpath != null && userpath.exists()) {
			// get userpath
			retval.append(userpath.toString());
		} else {
			// get base dir
			File f = getMainDataFileDir();
			// if we have no valid filepath, return empty string
			if (null == f) {
				return "";
			}
			// create a new image path from the base dir plus appending "/img/" directory
			retval.append(f.getPath()).append(File.separatorChar).append(subdir);
		}
		// check whether a trailing separator char should be added
		if (trailingSeparator) {
			// indicates whether we already have a trailing seperator char
			boolean sepcharalreadyexists = false;
			// if so, check whether we don't already have such a trailing separator char
			try {
				sepcharalreadyexists = (retval.charAt(retval.length() - 1) == File.separatorChar);
			} catch (IndexOutOfBoundsException ex) {
			}
			// if we don't already have a separator char, append it now...
			if (!sepcharalreadyexists) {
				retval.append(File.separatorChar);
			}
		} // if no trailing separator char requested, delete it, if any
		else {
			// indicates whether we already have a trailing separator char
			boolean sepcharalreadyexists = false;
			// if so, check whether we don't already have such a trailing separator char
			try {
				sepcharalreadyexists = (retval.charAt(retval.length() - 1) == File.separatorChar);
			} catch (IndexOutOfBoundsException ex) {
			}
			// if we already have a separator char, delete it now...
			if (sepcharalreadyexists) {
				try {
					retval.deleteCharAt(retval.length() - 1);
				} catch (StringIndexOutOfBoundsException ex) {
				}
			}
		}
		// return result.
		return retval.toString();

	}

	/**
	 * Retrieves the image path, where attachments used in entries are stored. This
	 * is typically the directory "attachments", which is a sub directory of the
	 * filepath directory.
	 *
	 * @param userpath
	 * @param trailingSeparator if true, a file-separator-char will be appended, if
	 *                          false not
	 * @return the directory to the attachment-path, or an empty string if no path
	 *         was found
	 */
	public String getAttachmentPath(File userpath, boolean trailingSeparator) {
		// init variables
		StringBuilder retval = new StringBuilder("");
		// check whether we have a user-defined attachment path. if yes,
		// use this as attachment-path, else get the base directory
		if (userpath != null && userpath.exists()) {
			// get user path
			retval.append(userpath.toString());
		} else {
			// get base dir
			File f = getMainDataFileDir();
			// if we have no valid filepath, return empty string
			if (null == f) {
				return "";
			}
			// create a new attachment path from the base dir plus appending "/attachment/"
			// directory
			retval.append(f.getPath()).append(File.separatorChar).append("attachments");
		}
		// check whether a trailing separator char should be added
		if (trailingSeparator) {
			// indicates whether we already have a trailing seperator char
			boolean sepcharalreadyexists = false;
			// if so, check whether we don't already have such a trailing separator char
			try {
				sepcharalreadyexists = (retval.charAt(retval.length() - 1) == File.separatorChar);
			} catch (IndexOutOfBoundsException ex) {
			}
			// if we don't already have a separator char, append it now...
			if (!sepcharalreadyexists) {
				retval.append(File.separatorChar);
			}
		} // if no trailing separator char requested, delete it, if any
		else {
			// indicates whether we already have a trailing separator char
			boolean sepcharalreadyexists = false;
			// if so, check whether we don't already have such a trailing separator char
			try {
				sepcharalreadyexists = (retval.charAt(retval.length() - 1) == File.separatorChar);
			} catch (IndexOutOfBoundsException ex) {
			}
			// if we already have a separator char, delete it now...
			if (sepcharalreadyexists) {
				try {
					retval.deleteCharAt(retval.length() - 1);
				} catch (StringIndexOutOfBoundsException ex) {
				}
			}
		}
		// retrieve attachment-path
		String convertSeparatorChars = Tools.convertSeparatorChars(retval.toString(), this);
		// return result.
		return convertSeparatorChars;
	}

	/**
	 * Gets the startup entry. This is the entry which is displayed immediately
	 * after opening a data file.
	 *
	 * @return the number of the startup entry
	 */
	public int getStartupEntry() {
		return genericIntGetter(SETTING_STARTUPENTRY, -1);
	}

	/**
	 * Gets the startup entry. This is the entry which is displayed immediately
	 * after opening a data file.
	 *
	 * @param nr (the number of the last viewed/activated entry)
	 */
	public void setStartupEntry(int nr) {
		genericIntSetter(SETTING_STARTUPENTRY, nr);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the find-dialog, which
	 * is opened from the main window. depending on this variable (and the set bits
	 * of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getSearchWhere() {
		return genericIntGetter(SETTING_SEARCHWHERE, Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE
				| Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the find-dialog, which
	 * is opened from the main window. depending on this variable (and the set bits
	 * of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @param where an integer value, where the single bits indicate whether a
	 *              checkbox should be selected or not.
	 */
	public void setSearchWhere(int where) {
		genericIntSetter(SETTING_SEARCHWHERE, where);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the replace-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getReplaceWhere() {
		return genericIntGetter(SETTING_REPLACEWHERE, Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE
				| Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the replace-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @param where an integer value, where the single bits indicate whether a
	 *              checkbox should be selected or not.
	 */
	public void setReplaceWhere(int where) {
		genericIntSetter(SETTING_REPLACEWHERE, where);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the
	 * desktop-display-dialog, which is opened from the desktop-window. depending on
	 * this variable (and the set bits of it) we can figure out which checkboxes
	 * should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getDesktopDisplayItems() {
		return genericIntGetter(SETTING_DESKTOPDISPLAYITEMS,
				Constants.DESKTOP_SHOW_REMARKS | Constants.DESKTOP_SHOW_AUTHORS);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the
	 * desktop-display-dialog, which is opened from the desktop-window. depending on
	 * this variable (and the set bits of it) we can figure out which checkboxes
	 * should be initially selected.
	 *
	 * @param items an integer value, where the single bits indicate whether a
	 *              checkbox should be selected or not.
	 */
	public void setDesktopDisplayItems(int items) {
		genericIntSetter(SETTING_DESKTOPDISPLAYITEMS, items);
	}

	/**
	 * Returns the depth of expanded levels from the follower tab.
	 * 
	 * @return the depth of expanded levels from the follower tab.
	 */
	public int getLuhmannExpandLevel() {
		return genericIntGetter(SETTING_LUHMANNTREEEXPANDLEVEL, -1);
	}

	/**
	 * Sets the depths of levels that should be expanded when the follower tab is
	 * viewed.
	 * 
	 * @param level the depths of levels that should be expanded when the follower
	 *              tab is shown.
	 */
	public void setLuhmannExpandLevel(int level) {
		genericIntSetter(SETTING_LUHMANNTREEEXPANDLEVEL, level);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the find-dialog, which
	 * is opened from the main window. depending on this variable (and the set bits
	 * of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getSearchOptions() {
		return genericIntGetter(SETTING_SEARCHOPTION, 0);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the find-dialog, which
	 * is opened from the main window. depending on this variable (and the set bits
	 * of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @param nr an integer value, where the single bits indicate whether a checkbox
	 *           should be selected or not.
	 */
	public void setSearchOptions(int nr) {
		genericIntSetter(SETTING_SEARCHOPTION, nr);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the export-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getExportParts() {
		return genericIntGetter(SETTING_EXPORTPARTS, 0);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the export-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @param val an integer value, where the single bits indicate whether a
	 *            checkbox should be selected or not.
	 */
	public void setExportParts(int val) {
		genericIntSetter(SETTING_EXPORTPARTS, val);
	}

	/**
	 * @return an integer value indicating which export-format (docx, rtf, txt...)
	 *         was lastly selected by the user.
	 */
	public int getExportFormat() {
		return genericIntGetter(SETTING_EXPORTFORMAT, 0);
	}

	/**
	 * @param val an integer value indicating which export-format (docx, rtf,
	 *            txt...) was lastly selected by the user.
	 */
	public void setExportFormat(int val) {
		genericIntSetter(SETTING_EXPORTFORMAT, val);
	}

	/**
	 * @return an integer value indicating which export-format (docx, rtf, txt...)
	 *         was lastly selected by the user.
	 */
	public int getDesktopExportFormat() {
		return genericIntGetter(SETTING_DESKTOPEXPORTFORMAT, 0);
	}

	/**
	 * @param val an integer value indicating which export-format (docx, rtf,
	 *            txt...) was lastly selected by the user.
	 */
	public void setDesktopExportFormat(int val) {
		genericIntSetter(SETTING_DESKTOPEXPORTFORMAT, val);
	}

	/**
	 * @return an integer value indicating whether the user wants to export the
	 *         desktop-data (entries) with their comments, without comments or if
	 *         only entries with comments at all should be exported. <br>
	 *         <br>
	 *         Returns on of the following constants:<br>
	 *         - {@code EXP_COMMENT_YES}<br>
	 *         - {@code EXP_COMMENT_NO}<br>
	 *         - {@code EXP_COMMENT_ONLY}<br>
	 */
	public int getDesktopCommentExport() {
		return genericIntGetter(SETTING_DESKTOPCOMMENTEXPORT, 0);
	}

	/**
	 * @param val an integer value indicating whether the user wants to export the
	 *            desktop-data (entries) with their comments, without comments or if
	 *            only entries with comments at all should be exported. <br>
	 *            <br>
	 *            Use following constants:<br>
	 *            - {@code EXP_COMMENT_YES}<br>
	 *            - {@code EXP_COMMENT_NO}<br>
	 *            - {@code EXP_COMMENT_ONLY}<br>
	 */
	public void setDesktopCommentExport(int val) {
		genericIntSetter(SETTING_DESKTOPCOMMENTEXPORT, val);
	}

	/**
	 * this method gets the initiated fields (checkboxes) for the replace-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @return an integer value, where the single bits indicate whether a checkbox
	 *         should be selected or not.
	 */
	public int getReplaceOptions() {
		return genericIntGetter(SETTING_REPLACEOPTION, 0);
	}

	/**
	 * this method sets the initiated fields (checkboxes) for the replace-dialog,
	 * which is opened from the main window. depending on this variable (and the set
	 * bits of it) we can figure out which checkboxes should be initially selected.
	 *
	 * @param nr an integer value, where the single bits indicate whether a checkbox
	 *           should be selected or not.
	 */
	public void setReplaceOptions(int nr) {
		genericIntSetter(SETTING_REPLACEOPTION, nr);
	}

	/**
	 * This method gets the last used search term which was entered in the main
	 * window's find dialog.
	 *
	 * @return the last used search term for the find dialog
	 */
	public String getSearchWhat() {
		return genericStringGetter(SETTING_SEARCHWHAT, "");
	}

	/**
	 * This method sets the last used search term which was entered in the main
	 * window's find dialog.
	 *
	 * @param searchterm the last used search term for the find dialog
	 */
	public void setSearchWhat(String searchterm) {
		genericStringSetter(SETTING_SEARCHWHAT, searchterm);
	}

	/**
	 * This method gets the last used replace term which was entered in the main
	 * window's replace dialog.
	 *
	 * @return the last used replaceterm for the replace dialog
	 */
	public String getReplaceWhat() {
		return genericStringGetter(SETTING_REPLACEWHAT, "");
	}

	/**
	 * This method sets the last used replace term which was entered in the main
	 * window's replace dialog.
	 *
	 * @param replaceterm the last used replace term for the replace dialog
	 */
	public void setReplaceWhat(String replaceterm) {
		genericStringSetter(SETTING_REPLACEWHAT, replaceterm);
	}

	/**
	 * Retrieves the base filepath, i.e. the usual directory where the data file is
	 * stored. Setting this base path enables relative path-settings for images,
	 * data files and e.g. linked files (like DOCXs etc.), so the user can easily
	 * move his "data directory" and then simply change the base path.
	 *
	 * @return {@code null}, if no value is set... else the directory of the
	 *         data-file, <i>without</i> trailing separator char
	 */
	public File getMainDataFileDir() {
		File mainDataFile = getMainDataFile();
		if (mainDataFile == null) {
			return null;
		}
		return mainDataFile.getParentFile();
	}

	/**
	 * Retrieves the logical combination for filtering the link-list when the user
	 * selectes a keyword in the jListKeywords. See method "filterLinks()" in
	 * "ZettelkastenView.java" for more details
	 *
	 * @return
	 */
	public String getLogKeywordlist() {
		return genericStringGetter(SETTING_LOGKEYWORDLIST, "");
	}

	/**
	 * Sets the logical combination for filtering the link-list when the user
	 * selectes a keyword in the jListKeywords. See method "filterLinks()" in
	 * "ZettelkastenView.java" for more details
	 *
	 * @param path
	 */
	public void setLogKeywordlist(String path) {
		genericStringSetter(SETTING_LOGKEYWORDLIST, path);
	}

	/**
	 * Get the setting for look and feel
	 *
	 * @return Look and feel class name as string
	 */
	public String getLookAndFeel() {
		return genericStringGetter(SETTING_LAF, "");
	}

	/**
	 * Set the default look and feel setting
	 *
	 * @param laf (Look and feel class name as string)
	 */
	public void setLookAndFeel(String laf) {
		genericStringSetter(SETTING_LAF, laf);
	}

	/**
	 * Gets the show-grid-variable. If true, the <i>horizontal</i> grids in lists
	 * and tables should be displayed.
	 *
	 * @return {@code true} if the <i>horizontal</i> grids in lists and tables
	 *         should be displayed, flase otherwise
	 */
	public boolean getShowGridHorizontal() {
		return genericBooleanGetter(SETTING_SHOWGRID_HORIZONTAL);
	}

	/**
	 * Sets the show-grid-variable. If true, the <i>horizontal</i> grids in lists
	 * and tables should be displayed.
	 *
	 * @param show true if the grids should be displayed, false otherweise
	 */
	public void setShowGridHorizontal(boolean show) {
		genericBooleanSetter(SETTING_SHOWGRID_HORIZONTAL, show);
	}

	/**
	 * Gets the show-grid-variable. If true, the <i>vertical</i> grids in lists and
	 * tables should be displayed.
	 *
	 * @return {@code true} if the <i>vertical</i> grids in lists and tables should
	 *         be displayed, flase otherwise
	 */
	public boolean getShowGridVertical() {
		return genericBooleanGetter(SETTING_SHOWGRID_VERTICAL);
	}

	/**
	 * Sets the show-grid-variable. If true, the <i>vertical</i> grids in lists and
	 * tables should be displayed.
	 *
	 * @param show true if the grids should be displayed, false otherweise
	 */
	public void setShowGridVertical(boolean show) {
		genericBooleanSetter(SETTING_SHOWGRID_VERTICAL, show);
	}

	/**
	 * Whether all follower entries, including top-level parent follower, should be
	 * shown in trailing entries or not.
	 *
	 * @return val {@code true} if all trailing entries, including top-level parent
	 *         follower, should be shown in trailing entries; {@code false}
	 *         otherwise.
	 */
	public boolean getShowAllLuhmann() {
		return genericBooleanGetter(SETTING_SHOWALLLUHMANN);
	}

	/**
	 * Whether all follower entries, including top-level parent follower, should be
	 * shown in trailing entries or not.
	 *
	 * @param val {@code true} if all trailing entries, including top-level parent
	 *            follower, should be shown in trailing entries; {@code false}
	 *            otherwise.
	 */
	public void setShowAllLuhmann(boolean val) {
		genericBooleanSetter(SETTING_SHOWALLLUHMANN, val);
	}

	/**
	 * Whether all follower entries, including top-level parent follower, should be
	 * shown in trailing entries or not.
	 *
	 * @return val {@code true} if all trailing entries, including top-level parent
	 *         follower, should be shown in trailing entries; {@code false}
	 *         otherwise.
	 */
	public boolean getShowLuhmannIconInDesktop() {
		return genericBooleanGetter(SETTING_SHOWLUHMANNICONINDESK);
	}

	/**
	 * Whether all follower entries, including top-level parent follower, should be
	 * shown in trailing entries or not.
	 *
	 * @param val {@code true} if all trailing entries, including top-level parent
	 *            follower, should be shown in trailing entries; {@code false}
	 *            otherwise.
	 */
	public void setShowLuhmannIconInDesktop(boolean val) {
		genericBooleanSetter(SETTING_SHOWLUHMANNICONINDESK, val);
	}

	/**
	 * Whether or not the searches from the tables, which are not started via the
	 * find-dialog, but via the (context-)menus, should include synonym-search or
	 * not.
	 *
	 * @return {@code true} if the search should include synonyms, false otherwise
	 */
	public boolean getSearchAlwaysSynonyms() {
		return genericBooleanGetter(SETTING_SEARCHALWAYSSYNONYMS);
	}

	/**
	 * Whether or not the searches from the tables, which are not started via the
	 * find-dialog, but via the (context-)menus, should include synonym-search or
	 * not.
	 *
	 * @param val true if the search should include synonyms, false otherwise
	 */
	public void setSearchAlwaysSynonyms(boolean val) {
		genericBooleanSetter(SETTING_SEARCHALWAYSSYNONYMS, val);
	}

	public boolean getSearchAlwaysAccentInsensitive() {
		return genericBooleanGetter(SETTING_SEARCHALWAYSACCENTINSENSITIVE);
	}

	public void setSearchAlwaysAccentInsensitive(boolean val) {
		genericBooleanSetter(SETTING_SEARCHALWAYSACCENTINSENSITIVE, val);
	}

	public boolean getMakeLuhmannColumnSortable() {
		return genericBooleanGetter(SETTING_MAKELUHMANNCOLUMNSORTABLE);
	}

	public void setMakeLuhmannColumnSortable(boolean val) {
		genericBooleanSetter(SETTING_MAKELUHMANNCOLUMNSORTABLE, val);
	}

	/**
	 * Whether or not keyword-synonyms should be displayed in the jTableKeywords
	 *
	 * @return {@code true} keyword-synonyms should be displayed in the
	 *         jTableKeywords, false otherwise
	 */
	public boolean getShowSynonymsInTable() {
		return genericBooleanGetter(SETTING_SHOWSYNONYMSINTABLE);
	}

	/**
	 * Whether or not keyword-synonyms should be displayed in the jTableKeywords
	 *
	 * @param val true keyword-synonyms should be displayed in the jTableKeywords,
	 *            false otherwise
	 */
	public void setShowSynonymsInTable(boolean val) {
		genericBooleanSetter(SETTING_SHOWSYNONYMSINTABLE, val);
	}

	/**
	 * This setting gets the spacing between table cells.
	 *
	 * @return a dimension value, holding the horizontal and vertical
	 *         cellspacing-values
	 */
	public Dimension getCellSpacing() {
		String value = genericStringGetter(SETTING_CELLSPACING, "");
		if (value.isEmpty()) {
			return new Dimension(1, 1);
		}
		String[] splitted = value.split(",");
		return new Dimension(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
	}

	/**
	 * This setting stores the spacing between table cells.
	 *
	 * @param hor the horizontal distance between the table cells
	 * @param ver the vertical distance between the table cells
	 */
	public void setCellSpacing(int hor, int ver) {
		StringBuilder builder = new StringBuilder("");
		builder.append(String.valueOf(hor));
		builder.append(",");
		builder.append(String.valueOf(ver));
		genericStringSetter(SETTING_CELLSPACING, builder.toString());
	}

	/**
	 * Gets the setting for the quick input of keywords.
	 *
	 * @return {@code true} if the keyword-quickinput should be activated
	 */
	public boolean getQuickInput() {
		return genericBooleanGetter(SETTING_QUICKINPUT);
	}

	/**
	 * Sets the setting for the keyword-quick-input when editing new entries..
	 *
	 * @param val true if the keyword-quickinput should be activated
	 */
	public void setQuickInput(boolean val) {
		genericBooleanSetter(SETTING_QUICKINPUT, val);
	}

	/**
	 * Gets the setting for the autobackup-option.
	 *
	 * @return {@code true} if autobackup should be activated
	 */
	public boolean getAutoBackup() {
		return genericBooleanGetter(SETTING_AUTOBACKUP);
	}

	/**
	 * Sets the setting for the autobackup-option
	 *
	 * @param val true if the autobackup should be activated
	 */
	public void setAutoBackup(boolean val) {
		genericBooleanSetter(SETTING_AUTOBACKUP, val);
	}

	/**
	 * Gets the setting for the minimize to tray-option.
	 *
	 * @return {@code true} if minimizing to tray should be activated
	 */
	public boolean getMinimizeToTray() {
		return genericBooleanGetter(SETTING_MINIMIZETOTRAY);
	}

	/**
	 * Sets the setting for the minimizing to tray-option
	 *
	 * @param val true if minimizing to tray should be activated
	 */
	public void setMinimizeToTray(boolean val) {
		genericBooleanSetter(SETTING_MINIMIZETOTRAY, val);
	}

	/**
	 * Gets the setting for the autoupdate-option.
	 *
	 * @return {@code true} if autoupdate should be activated
	 */
	public boolean getAutoUpdate() {
		return genericBooleanGetter(SETTING_AUTOUPDATE);
	}

	/**
	 * Sets the setting for the autobackup-option
	 *
	 * @param val true if the autobackup should be activated
	 */
	public void setAutoUpdate(boolean val) {
		genericBooleanSetter(SETTING_AUTOUPDATE, val);
	}

	/**
	 * Gets the setting whether the warning dialog in the desktop window, that tells
	 * the user if added entries already have been added before, should be shown or
	 * not.
	 *
	 * @return {@code true} if the warning dialog in the desktop window, that tells
	 *         the user if added entries already have been added before, should be
	 *         shown
	 */
	public boolean getHideMultipleDesktopOccurencesDlg() {
		return genericBooleanGetter(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG);
	}

	/**
	 * Sets the setting whether the warning dialog in the desktop window, that tells
	 * the user if added entries already have been added before, should be shown or
	 * not.
	 *
	 * @param val {@code true} if the warning dialog in the desktop window, that
	 *            tells the user if added entries already have been added before,
	 *            should be shown
	 */
	public void setHideMultipleDesktopOccurencesDlg(boolean val) {
		genericBooleanSetter(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG, val);
	}

	/**
	 * Gets the setting whether a table of contents should be created when exporting
	 * desktop data.
	 *
	 * @return {@code true} if a table of contents should be created when exporting
	 *         desktop data.
	 */
	public boolean getTOCForDesktopExport() {
		return genericBooleanGetter(SETTING_TOCFORDESKTOPEXPORT);
	}

	/**
	 * Sets the setting whether a table of contents should be created when exporting
	 * desktop data.
	 *
	 * @param val {@code true} if a table of contents should be created when
	 *            exporting desktop data.
	 */
	public void setTOCForDesktopExport(boolean val) {
		genericBooleanSetter(SETTING_TOCFORDESKTOPEXPORT, val);
	}

	/**
	 * Gets the setting whether multiple lines in the output file of the desktop
	 * data should be removed or not.
	 *
	 * @return {@code true} if multiple lines in the output file of the desktop data
	 *         should be removed
	 */
	public boolean getRemoveLinesForDesktopExport() {
		return genericBooleanGetter(SETTING_REMOVELINESFORDESKTOPEXPORT);
	}

	/**
	 * Sets the setting whether multiple lines in the output file of the desktop
	 * data should be removed or not.
	 *
	 * @param val {@code true} if multiple lines in the output file of the desktop
	 *            data should be removed
	 */
	public void setRemoveLinesForDesktopExport(boolean val) {
		genericBooleanSetter(SETTING_REMOVELINESFORDESKTOPEXPORT, val);
	}

	/**
	 * Gets the setting for the autobackup-option.
	 *
	 * @return {@code true} if autobackup should be activated
	 */
	public boolean getAutoNightlyUpdate() {
		return genericBooleanGetter(SETTING_AUTONIGHTLYUPDATE);
	}

	/**
	 * Sets the setting for the autobackup-option
	 *
	 * @param val true if the autobackup should be activated
	 */
	public void setAutoNightlyUpdate(boolean val) {
		genericBooleanSetter(SETTING_AUTONIGHTLYUPDATE, val);
	}

	/**
	 * Gets the setting for the show icon text option.
	 *
	 * @return {@code true} if show icon text should be activated
	 */
	public boolean getShowIconText() {
		return genericBooleanGetter(SETTING_SHOWICONTEXT);
	}

	/**
	 * Sets the setting for the show icon text option
	 *
	 * @param val true if the show icon text should be activated
	 */
	public void setShowIconText(boolean val) {
		genericBooleanSetter(SETTING_SHOWICONTEXT, val);
	}

	public boolean getAutoCompleteTags() {
		return genericBooleanGetter(SETTING_AUTOCOMPLETETAGS);
	}

	public void setAutoCompleteTags(boolean val) {
		genericBooleanSetter(SETTING_AUTOCOMPLETETAGS, val);
	}

	public boolean getUseMacBackgroundColor() {
		return genericBooleanGetter(SETTING_USEMACBACKGROUNDCOLOR);
	}

	public void setUseMacBackgroundColor(boolean val) {
		genericBooleanSetter(SETTING_USEMACBACKGROUNDCOLOR, val);
	}

	public boolean getMarkdownActivated() {
		return genericBooleanGetter(SETTING_MARKDOWNACTIVATED);
	}

	public void setMarkdownActivated(boolean val) {
		genericBooleanSetter(SETTING_MARKDOWNACTIVATED, val);
	}

	/**
	 * Gets the setting for the autobackup-option.
	 *
	 * @return {@code true} if autobackup should be activated
	 */
	public boolean getExtraBackup() {
		return genericBooleanGetter(SETTING_EXTRABACKUP);
	}

	/**
	 * Sets the setting for the autobackup-option
	 *
	 * @param val true if the autobackup should be activated
	 */
	public void setExtraBackup(boolean val) {
		genericBooleanSetter(SETTING_EXTRABACKUP, val);
	}

	public String getCustomCSS(int what) {
		String ch;
		switch (what) {
		case CUSTOM_CSS_ENTRY:
			ch = SETTING_CUSTOMCSSENTRY;
			break;
		case CUSTOM_CSS_DESKTOP:
			ch = SETTING_CUSTOMCSSDESKTOP;
			break;
		default:
			ch = SETTING_CUSTOMCSSENTRY;
			break;
		}
		return genericStringGetter(ch, null);
	}

	public void setCustomCSS(int what, String css) {
		String ch;
		switch (what) {
		case CUSTOM_CSS_ENTRY:
			ch = SETTING_CUSTOMCSSENTRY;
			break;
		case CUSTOM_CSS_DESKTOP:
			ch = SETTING_CUSTOMCSSDESKTOP;
			break;
		default:
			ch = SETTING_CUSTOMCSSENTRY;
			break;
		}
		genericStringSetter(ch, css);
	}

	/**
	 * Gets the setting for the default locale
	 *
	 * @return a string with a lowercase-2-letter-country-code for the default
	 *         languages
	 */
	public String getLanguage() {
		return genericStringGetter(SETTING_LOCALE, Locale.getDefault().getLanguage());
	}

	/**
	 * Sets the default language
	 *
	 * @param lang a string with a lowercase-2-letter-country-code for the default
	 *             languages
	 */
	public void setLanguage(String lang) {
		genericStringSetter(SETTING_LOCALE, lang.toLowerCase());
	}

	/**
	 * Gets the setting whether all displayed/watched entries should be added to the
	 * history of displayed entries, or whether only the activated entries should be
	 * added to the history list
	 *
	 * @return {@code true} if every displayed entry should be added to the history
	 *         list, {@code false} if only activated entries should be added to it.
	 */
	public boolean getAddAllToHistory() {
		return genericBooleanGetter(SETTING_ADDALLTOHISTORY);
	}

	/**
	 * Gets the setting whether all displayed/watched entries should be added to the
	 * history of displayed entries, or whether only the activated entries should be
	 * added to the history list
	 *
	 * @param val {@code true} if every displayed entry should be added to the
	 *            history list, {@code false} if only activated entries should be
	 *            added to it.
	 */
	public void setAddAllToHistory(boolean val) {
		genericBooleanSetter(SETTING_ADDALLTOHISTORY, val);
	}

	/**
	 * retrieves the desktop-number of the last used desktop.
	 *
	 * @param count the amount of desktops
	 * @return the number of the last used desktop, or {@code -1} if no desktop
	 *         exists. if a lastly used desktop was deleted, a {@code 0} is returned
	 *         instead. if no desktop exists at all, {@code -1} is returned.
	 */
	public int getLastUsedDesktop(int count) {
		if (count == 0) {
			return -1;
		}
		int value = genericIntGetter(SETTING_GETLASTUSEDDESKTOPNUMBER, 0);
		if (value >= count) {
			return 0;
		}
		return value;
	}

	/**
	 * Stores the currently used desktop, so this desktop can be shown on next
	 * program startup.
	 *
	 * @param val the index-number of the currently used desktop, starting with the
	 *            index-number {code 0} for the first desktop.
	 */
	public void setLastUsedDesktop(int val) {
		genericIntSetter(SETTING_GETLASTUSEDDESKTOPNUMBER, val);
	}

	public int getSearchFrameSplitLayout() {
		return genericIntGetter(SETTING_SEARCHFRAMESPLITLAYOUT, JSplitPane.HORIZONTAL_SPLIT);
	}

	public void setSearchFrameSplitLayout(int val) {
		genericIntSetter(SETTING_SEARCHFRAMESPLITLAYOUT, val);
	}

	/**
	 * Gets the setting whether new entries should be inserted at empty positions of
	 * previous deleted entries or not.
	 *
	 * @return {@code true} if new entries should be inserted at empty positions;
	 *         false if new entries should be inserted at the end of the data file
	 */
	public boolean getInsertNewEntryAtEmpty() {
		return genericBooleanGetter(SETTING_FILLEMPTYPLACES);
	}

	/**
	 * Sets the setting whether new entries should be inserted at empty positions of
	 * previous deleted entries or not.
	 *
	 * @param val true if new entries should be inserted at empty positions; false
	 *            if new entries should be inserted at the end of the data file
	 */
	public void setInsertNewEntryAtEmpty(boolean val) {
		genericBooleanSetter(SETTING_FILLEMPTYPLACES, val);
	}

	/**
	 * Gets the settings, whether highlighting searchresults and keywords should
	 * highlight the background, i.e. setting a background-color or not
	 *
	 * @param style
	 * @return {@code true} if a background-color for highlighting should be shown,
	 *         false otherwise
	 */
	public boolean getShowHighlightBackground(int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_SHOWHIGHLIGHTBACKGROUND;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_SHOWHIGHLIGHTKEYWORDBACKGROUND;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_SHOWHIGHLIGHTLIVESEARCHBACKGROUND;
			break;
		default:
			hs_style = SETTING_SHOWHIGHLIGHTBACKGROUND;
			break;
		}
		return genericBooleanGetter(hs_style);
	}

	/**
	 * Gets the settings, whether highlighting searchresults and keywords should
	 * highlight the background, i.e. setting a background-color or not
	 *
	 * @param val   true if a background-color for highlighting should be shown,
	 *              false otherwise
	 * @param style
	 */
	public void setShowHighlightBackground(boolean val, int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_SHOWHIGHLIGHTBACKGROUND;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_SHOWHIGHLIGHTKEYWORDBACKGROUND;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_SHOWHIGHLIGHTLIVESEARCHBACKGROUND;
			break;
		default:
			hs_style = SETTING_SHOWHIGHLIGHTBACKGROUND;
			break;
		}
		genericIntSetter(hs_style, style);
	}

	/**
	 * Gets the settings, whether highlighting searchresults and keywords should
	 * highlight the background, i.e. setting a background-color or not
	 *
	 * @param style
	 * @return {@code true} if a background-color for highlighting should be shown,
	 *         false otherwise
	 */
	public String getHighlightBackgroundColor(int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_HIGHLIGHTBACKGROUNDCOLOR;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_HIGHLIGHTKEYWORDBACKGROUNDCOLOR;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_HIGHLIGHTLIVESEARCHBACKGROUNDCOLOR;
			break;
		default:
			hs_style = SETTING_HIGHLIGHTBACKGROUNDCOLOR;
			break;
		}
		return genericStringGetter(hs_style, "ffff66");
	}

	/**
	 * Gets the settings, whether highlighting searchresults and keywords should
	 * highlight the background, i.e. setting a background-color or not
	 *
	 * @param col
	 * @param style
	 */
	public void setHighlightBackgroundColor(String col, int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_HIGHLIGHTBACKGROUNDCOLOR;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_HIGHLIGHTKEYWORDBACKGROUNDCOLOR;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_HIGHLIGHTLIVESEARCHBACKGROUNDCOLOR;
			break;
		default:
			hs_style = SETTING_HIGHLIGHTBACKGROUNDCOLOR;
			break;
		}
		genericIntSetter(hs_style, style);
	}

	/**
	 *
	 * @return
	 */
	public String getAppendixBackgroundColor() {
		return genericStringGetter(SETTING_APPENDIXBACKGROUNDCOLOR, "f2f2f2");
	}

	/**
	 *
	 * @param col
	 */
	public void setReflistBackgroundColor(String value) {
		genericStringSetter(SETTING_APPENDIXBACKGROUNDCOLOR, value);
	}

	/**
	 *
	 * @return
	 */
	public String getTableHeaderColor() {
		return genericStringGetter(SETTING_TABLEHEADERCOLOR, "e4e4e4");
	}

	public void setTableHeaderColor(String value) {
		genericStringSetter(SETTING_TABLEHEADERCOLOR, value);
	}

	public String getTableRowEvenColor() {
		return genericStringGetter(SETTING_TABLEEVENROWCOLOR, "eeeeee");
	}

	public void setTableRowEvenColor(String value) {
		genericStringSetter(SETTING_TABLEEVENROWCOLOR, value);
	}

	public String getTableRowOddColor() {
		return genericStringGetter(SETTING_TABLEODDROWCOLOR, "f8f8f8");
	}

	public void setTableRowOddColor(String value) {
		genericStringSetter(SETTING_TABLEODDROWCOLOR, value);
	}

	/**
	 * Gets the setting for the highlighting of search results. when activated, the
	 * search terms in the search results window (CSearchResults) are highlighted.
	 *
	 * @return {@code true} if search terms should be highlighted
	 */
	public boolean getHighlightSearchResults() {
		return genericBooleanGetter(SETTING_HIGHLIGHTSEARCHRESULTS);
	}

	/**
	 * Sets the setting for the highlighting of search results. when activated, the
	 * search terms in the search results window (CSearchResults) are highlighted.
	 *
	 * @param val {@code true} if search terms should be highlighted
	 */
	public void setHighlightSearchResults(boolean val) {
		genericBooleanSetter(SETTING_HIGHLIGHTSEARCHRESULTS, val);
	}

	/**
	 * Gets the setting for the highlighting of keywords in the main frame's
	 * entry-content. when activated, the keywords of an entry that appear in the
	 * entry-content are highlighted.
	 *
	 * @return {@code true} if keywords should be highlighted
	 */
	public boolean getHighlightKeywords() {
		return genericBooleanGetter(SETTING_HIGHLIGHTKEYWORDS);
	}

	/**
	 * Sets the setting for the highlighting of keywords in the main frame's
	 * entry-content. when activated, the keywords of an entry that appear in the
	 * entry-content are highlighted.
	 *
	 * @param val {@code true} if keywords should be highlighted
	 */
	public void setHighlightKeyword(boolean val) {
		genericBooleanSetter(SETTING_HIGHLIGHTKEYWORDS, val);
	}

	public boolean getHighlightSegments() {
		return highlightSegments;
	}

	public void setHighlightSegments(boolean val) {
		highlightSegments = val;
	}

	/**
	 * Gets the setting for showing an entry from the search results window
	 * immediatley. when activated, a selected entry in the search results window is
	 * immediately displayed in the main window.
	 *
	 * @return {@code true} if entry should be displayed at once
	 */
	public boolean getShowSearchEntry() {
		return genericBooleanGetter(SETTING_SHOWSEARCHENTRY);
	}

	/**
	 * Sets the setting for showing an entry from the search results window
	 * immediatley. when activated, a selected entry in the search results window is
	 * immediately displayed in the main window.
	 *
	 * @param val {@code true} if entry should be displayed at once
	 */
	public void setShowSearchEntry(boolean val) {
		genericBooleanSetter(SETTING_SHOWSEARCHENTRY, val);
	}

	/**
	 * Gets the setting whether the footnotes should be superscripted or not. A
	 * superscripted footnote is displayed smaller, but changes the line-height.
	 *
	 * @return {@code true} if footnote should be superscripted
	 */
	public boolean getSupFootnote() {
		return genericBooleanGetter(SETTING_SUPFOOTNOTE);
	}

	/**
	 * Sets the setting whether the footnotes should be superscripted or not. A
	 * superscripted footnote is displayed smaller, but changes the line-height.
	 *
	 * @param val use true, if footnote should be superscripted
	 */
	public void setSupFootnote(boolean val) {
		genericBooleanSetter(SETTING_SUPFOOTNOTE, val);
	}

	public boolean getFootnoteBraces() {
		return genericBooleanGetter(SETTING_FOOTNOTEBRACES);
	}

	public void setFootnoteBraces(boolean val) {
		genericBooleanSetter(SETTING_FOOTNOTEBRACES, val);
	}

	public boolean getSearchRemovesFormatTags() {
		return genericBooleanGetter(SETTING_SEARCHWITHOUTFORMATTAGS);
	}

	public void setSearchRemovesFormatTags(boolean val) {
		genericBooleanSetter(SETTING_SEARCHWITHOUTFORMATTAGS, val);
	}

	/**
	 * Gets the setting whether a click on the footnotes should open the tab with
	 * the authorlist and select the related author or not.
	 *
	 * @return {@code true} if footnote should show the related author in the tabbed
	 *         pane
	 */
	public boolean getJumpFootnote() {
		return genericBooleanGetter(SETTING_JUMPFOOTNOTE);
	}

	/**
	 * Sets the setting whether a click on the footnotes should open the tab with
	 * the authorlist and select the related author or not.
	 *
	 * @param val {@code true} if footnote should show the related author in the
	 *            tabbed pane
	 */
	public void setJumpFootnote(boolean val) {
		genericBooleanSetter(SETTING_JUMPFOOTNOTE, val);
	}

	/**
	 * Gets the setting whether a search request should search in entries within a
	 * certain date-range.
	 *
	 * @return {@code true} if search should look for entries with a certain date
	 *         (timestamp)
	 */
	public boolean getSearchTime() {
		return genericBooleanGetter(SETTING_SEARCHTIME);
	}

	/**
	 * Sets the setting whether a search request should search in entries within a
	 * certain date-range.
	 *
	 * @param val {@code true} if search should look for entries with a certain date
	 *            (timestamp)
	 */
	public void setSearchTime(boolean val) {
		genericBooleanSetter(SETTING_SEARCHTIME, val);
	}

	/**
	 * Gets the setting which logicalk-combination the user chose for the last
	 * search request.
	 *
	 * @return 0 if search was log-and; 1 for log-or and 2 for log-not.
	 */
	public int getSearchLog() {
		return genericIntGetter(SETTING_SEARCHLOG, 0);
	}

	/**
	 * Sets the setting which logicalk-combination the user chose for the last
	 * search request.
	 *
	 * @param val 0 if search was log-and; 1 for log-or and 2 for log-not.
	 */
	public void setSearchLog(int val) {
		genericIntSetter(SETTING_SEARCHLOG, val);
	}

	/**
	 * Gets the setting for the thumbnail width of images. This value indicates the
	 * maximum width of images which are displayed in the textfield. larger images
	 * are resized to fit the preferred maximum size and a link to the original
	 * image is inserted.
	 *
	 * @return the preferred maximum width of an image
	 */
	public int getImageResizeWidth() {
		return genericIntGetter(SETTING_IMGRESIZEWIDTH, 300);
	}

	/**
	 * Sets the setting for the thumbnail width of images. This value indicates the
	 * maximum width of images which are displayed in the textfield. larger images
	 * are resized to fit the preferred maximum size and a link to the original
	 * image is inserted.
	 *
	 * @param val the preferred maximum width of an image
	 */
	public void setImageResizeWidth(int val) {
		genericIntSetter(SETTING_IMGRESIZEWIDTH, val);
	}

	/**
	 * Gets the setting for the thumbnail width of images. This value indicates the
	 * maximum width of images which are displayed in the textfield. larger images
	 * are resized to fit the preferred maximum size and a link to the original
	 * image is inserted.
	 *
	 * @return the preferred maximum width of an image
	 */
	public int getImageResizeHeight() {
		return genericIntGetter(SETTING_IMGRESIZEHEIGHT, 300);
	}

	/**
	 * Sets the setting for the thumbnail width of images. This value indicates the
	 * maximum width of images which are displayed in the textfield. larger images
	 * are resized to fit the preferred maximum size and a link to the original
	 * image is inserted.
	 *
	 * @param val the preferred maximum width of an image
	 */
	public void setImageResizeHeight(int val) {
		genericIntSetter(SETTING_IMGRESIZEHEIGHT, val);
	}

	/**
	 * This method returns the default font-size for tables and lists. The user
	 * cannot choose the font or color, but at least a bigger font-size for better
	 * viewing is possible.
	 *
	 * @return the value for which the original font size should be increased.
	 */
	public int getTableFontSize() {
		Element el = settingsFile.getRootElement().getChild(SETTING_TABLEFONTSIZE);
		if (el != null) {
			int fontSize = Integer.parseInt(el.getText());
			// Before August 2022, the default was zero. This is will update the old default
			// to 12.
			if (fontSize == 0) {
				return 12;
			} else {
				return fontSize;
			}
		}
		return 12;
	}

	/**
	 * This method sets the default font-size for tables and lists. The user cannot
	 * choose the font or color, but at least a bigger font-size for better viewing
	 * is possible.
	 *
	 * @param size the value for which the original font-size should be increased
	 */
	public void setTableFontSize(int size) {
		genericIntSetter(SETTING_TABLEFONTSIZE, size);
	}

	public int getDesktopOutlineFontSize() {
		return genericIntGetter(SETTING_DESKTOPOUTLINEFONTSIZE, 0);
	}

	public void setDesktopOutlineFontSize(int size) {
		genericIntSetter(SETTING_DESKTOPOUTLINEFONTSIZE, size);
	}

	/**
	 * This method returns the index-value for the manual timestamp that can be
	 * inserted when editing a new entry (see CNewEntry-dlg). For the different
	 * String-values that are used to create the DateFormat, see
	 * {@code CConstants.manualTimestamp}.
	 *
	 * @return the index-value for the manual timestamp
	 */
	public int getManualTimestamp() {
		return genericIntGetter(SETTING_MANUALTIMESTAMP, 0);
	}

	/**
	 * This method sets the index-value for the manual timestamp that can be
	 * inserted when editing a new entry (see CNewEntry-dlg). For the different
	 * String-values that are used to create the DateFormat, see
	 * {@code CConstants.manualTimestamp}.
	 *
	 * @param val the index-value for the manual timestamp
	 */
	public void setManualTimestamp(int val) {
		genericIntSetter(SETTING_MANUALTIMESTAMP, val);
	}

	/**
	 * This method returns the default font-size for textfields in the
	 * CNewEntry-dialog. The user cannot choose the font or color, but at least a
	 * bigger font-size for better viewing is possible.
	 *
	 * @return the value for which the original font size should be increased.
	 */
	public int getTextfieldFontSize() {
		return genericIntGetter(SETTING_TEXTFIELDFONTSIZE, 0);
	}

	/**
	 * This method sets the default font-size for textfields in the
	 * CNewEntry-dialog. The user cannot choose the font or color, but at least a
	 * bigger font-size for better viewing is possible.
	 *
	 * @param size the value for which the original font-size should be increased
	 */
	public void setTextfieldFontSize(int value) {
		genericIntSetter(SETTING_TEXTFIELDFONTSIZE, value);
	}

	public int getLastUsedSetBibyKeyChoice() {
		return genericIntGetter(SETTING_LASTUSEDSETBIBKEYCHOICE, CSetBibKey.CHOOSE_BIBKEY_FROM_DB);
	}

	public void setLastUsedSetBibyKeyChoice(int value) {
		genericIntSetter(SETTING_LASTUSEDSETBIBKEYCHOICE, value);
	}

	public int getLastUsedSetBibyKeyType() {
		return genericIntGetter(SETTING_LASTUSEDSETBIBKEYTYPE, CSetBibKey.TYPE_BIBKEY_NEW);
	}

	public void setLastUsedSetBibyKeyType(int value) {
		genericIntSetter(SETTING_LASTUSEDSETBIBKEYTYPE, value);
	}

	public int getLastUsedBibtexImportSource() {
		return genericIntGetter(SETTING_LASTUSEDSETBIBIMPORTSOURCE, CImportBibTex.BIBTEX_SOURCE_DB);
	}

	public void setLastUsedBibtexImportSource(int value) {
		genericIntSetter(SETTING_LASTUSEDSETBIBIMPORTSOURCE, value);
	}

	/**
	 * Gets the setting for the thumbnail activation. This value indicates whether
	 * iamges should always be display in original size, or whether large images
	 * should be resized
	 *
	 * @return {@code true} if large images should be resized.
	 */
	public boolean getImageResize() {
		return genericBooleanGetter(SETTING_IMGRESIZE);
	}

	/**
	 * Sets the setting for the thumbnail activation. This value indicates whether
	 * iamges should always be display in original size, or whether large images
	 * should be resized
	 *
	 * @param val whether thumbnail-display is enabled or not
	 */
	public void setImageResize(boolean val) {
		genericBooleanSetter(SETTING_IMGRESIZE, val);
	}

	public boolean getShowTableBorder() {
		return genericBooleanGetter(SETTING_SHOWTABLEBORDER);
	}

	public void setShowTableBorder(boolean val) {
		genericBooleanSetter(SETTING_SHOWTABLEBORDER, val);
	}

	public boolean getShowLuhmannEntryNumber() {
		return genericBooleanGetter(SETTING_SHOWLUHMANNENTRYNUMBER);
	}

	public void setShowLuhmannEntryNumber(boolean val) {
		genericBooleanSetter(SETTING_SHOWLUHMANNENTRYNUMBER, val);
	}

	public boolean getShowDesktopEntryNumber() {
		return genericBooleanGetter(SETTING_SHOWDESKTOPENTRYNUMBER);
	}

	public void setShowDesktopEntryNumber(boolean val) {
		genericBooleanSetter(SETTING_SHOWDESKTOPENTRYNUMBER, val);
	}

	public boolean getShowEntryHeadline() {
		return genericBooleanGetter(SETTING_SHOWENTRYHEADLINE);
	}

	public void setShowEntryHeadline(boolean val) {
		genericBooleanSetter(SETTING_SHOWENTRYHEADLINE, val);
	}

	/**
	 * Gets the setting for the extended quick input of keywords.
	 *
	 * @return {@code true} if the extended keyword-quickinput should be activated
	 */
	public int getQuickInputExtended() {
		return genericIntGetter(SETTING_QUICKINPUTEXTENDED, 0);
	}

	/**
	 * Sets the setting for the extended keyword-quick-input when editing new
	 * entries..
	 *
	 * @param val true if the extended keyword-quickinput should be activated
	 */
	public void setQuickInputExtended(int val) {
		genericIntSetter(SETTING_QUICKINPUTEXTENDED, val);
	}

	/**
	 * Gets the spell-correction-variable. If true, the grids in lists and tables
	 * should be displayed.
	 *
	 * @return
	 */
	public boolean getSpellCorrect() {
		return genericBooleanGetter(SETTING_SPELLCORRECT);
	}

	/**
	 * Sets the spell-correction-variable. If true, the grids in lists and tables
	 * should be displayed.
	 *
	 * @param val (true if the spelling should be automatically corrected, false
	 *            otherwise)
	 */
	public void setSpellCorrect(boolean val) {
		genericBooleanSetter(SETTING_SPELLCORRECT, val);
	}

	/**
	 * Gets the steno-variable. If true, steno is activated, false otherwise
	 *
	 * @return {@code true} if steno is activated, false otherwise
	 */
	public boolean getStenoActivated() {
		return genericBooleanGetter(SETTING_STENOACTIVATED);
	}

	/**
	 * Sets the steno-variable. If true, steno is activated, false otherwise
	 *
	 * @param val {@code true} if steno is activated, false otherwise
	 */
	public void setStenoActivated(boolean val) {
		genericBooleanSetter(SETTING_STENOACTIVATED, val);
	}

	public boolean getHighlightWholeWord() {
		return genericBooleanGetter(SETTING_HIGHLIGHTWHOLEWORD);
	}

	public void setHighlightWholeWord(boolean val) {
		genericBooleanSetter(SETTING_HIGHLIGHTWHOLEWORD, val);
	}

	public boolean getHighlightWholeWordSearch() {
		return genericBooleanGetter(SETTING_HIGHLIGHTWHOLEWORDSEARCH);
	}

	public void setHighlightWholeWordSearch(boolean val) {
		genericBooleanSetter(SETTING_HIGHLIGHTWHOLEWORDSEARCH, val);
	}

	public Font getTableFont() {
		String value = genericStringGetter(SETTING_TABLEFONT, "");
		if (value.isEmpty()) {
			return null;
		}
		return new Font(value, Font.PLAIN, getTableFontSize());
	}

	public void setTableFont(Font f) {
		genericStringSetter(SETTING_TABLEFONT, f.getName());
		setTableFontSize(f.getSize());
	}

	public Font getDesktopOutlineFont() {
		String value = genericStringGetter(SETTING_DESKTOPOUTLINEFONT, "");
		if (value.isEmpty()) {
			return null;
		}
		return new Font(value, Font.PLAIN, 12);
	}

	public void setDesktopOutlineFont(String value) {
		genericStringSetter(SETTING_DESKTOPOUTLINEFONT, value);
	}

	/**
	 * Retrieves settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getMainfont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the main font as font-object.
	 *
	 * @return the main-font as {@code Font} variable.
	 */
	public Font getMainFont() {
		String style = getMainfont(FONTSTYLE);
		String weight = getMainfont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getMainfont(FONTSIZE));
		return new Font(getMainfont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setMainfont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
		if (el == null) {
			el = new Element(SETTING_MAINFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getAuthorFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_AUTHORFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the authir font as font-object.
	 *
	 * @return the author-font as {@code Font} variable.
	 */
	public Font getAuthorFont() {
		// get the font style.
		String style = getAuthorFont(FONTSTYLE);
		String weight = getAuthorFont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getAuthorFont(FONTSIZE));
		return new Font(getAuthorFont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setAuthorFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_AUTHORFONT);
		if (el == null) {
			el = new Element(SETTING_AUTHORFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	public String getCodeFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_CODEFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the authir font as font-object.
	 *
	 * @return the author-font as {@code Font} variable.
	 */
	public Font getCodeFont() {
		// get the font style.
		String style = getCodeFont(FONTSTYLE);
		String weight = getCodeFont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getCodeFont(FONTSIZE));
		return new Font(getCodeFont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setCodeFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_CODEFONT);
		if (el == null) {
			el = new Element(SETTING_CODEFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getRemarksFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_REMARKSFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the remarks font as font-object.
	 *
	 * @return the remarks-font as {@code Font} variable.
	 */
	public Font getRemarksFont() {
		String style = getRemarksFont(FONTSTYLE);
		String weight = getRemarksFont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getRemarksFont(FONTSIZE));
		return new Font(getRemarksFont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the mainfont (the font used for the
	 * main-entry-textfield).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setRemarksFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_REMARKSFONT);
		if (el == null) {
			el = new Element(SETTING_REMARKSFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the desktop-window's main headers.
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getDesktopHeaderfont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPHEADERFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getDesktopHeaderFont() {
		String style = getDesktopHeaderfont(FONTSTYLE);
		String weight = getDesktopHeaderfont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getDesktopHeaderfont(FONTSIZE));
		return new Font(getDesktopHeaderfont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the desktop-window's main header font.
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setDesktopHeaderfont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPHEADERFONT);
		if (el == null) {
			el = new Element(SETTING_DESKTOPHEADERFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the desktop-window's item headers (additional display
	 * items).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getDesktopItemHeaderfont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMHEADERFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getDesktopItemHeaderFont() {
		String style = getDesktopItemHeaderfont(FONTSTYLE);
		String weight = getDesktopItemHeaderfont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getDesktopItemHeaderfont(FONTSIZE));
		return new Font(getDesktopItemHeaderfont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the desktop-window's item header font (additional
	 * display items).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setDesktopItemHeaderfont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMHEADERFONT);
		if (el == null) {
			el = new Element(SETTING_DESKTOPITEMHEADERFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the desktop-window's items (additional display items).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getDesktopItemfont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getDesktopItemFont() {
		String style = getDesktopItemfont(FONTSTYLE);
		String weight = getDesktopItemfont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getDesktopItemfont(FONTSIZE));
		return new Font(getDesktopItemfont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the desktop-window's item font (additional display
	 * items).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setDesktopItemfont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMFONT);
		if (el == null) {
			el = new Element(SETTING_DESKTOPITEMFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the style of highlighting the search terms in the
	 * search result window.
	 *
	 * @param what  indicates, which style-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 * @param style
	 * @return the related style-information as string.
	 */
	public String getHighlightSearchStyle(int what, int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_HIGHLIGHTSEARCHSTYLE;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_HIGHLIGHTKEYWORDSTYLE;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_HIGHLIGHTLIVESEARCHSTYLE;
			break;
		default:
			hs_style = SETTING_HIGHLIGHTSEARCHSTYLE;
			break;
		}
		Element el = settingsFile.getRootElement().getChild(hs_style);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Changes settings for the style of highlighting the search terms in the search
	 * result window.
	 *
	 * @param value the new value for the style-characteristic
	 * @param what  indicates, which style-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 * @param style
	 */
	public void setHighlightSearchStyle(String value, int what, int style) {
		String hs_style;
		switch (style) {
		case HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS:
			hs_style = SETTING_HIGHLIGHTSEARCHSTYLE;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS:
			hs_style = SETTING_HIGHLIGHTKEYWORDSTYLE;
			break;
		case HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH:
			hs_style = SETTING_HIGHLIGHTLIVESEARCHSTYLE;
			break;
		default:
			hs_style = SETTING_HIGHLIGHTSEARCHSTYLE;
			break;
		}
		Element el = settingsFile.getRootElement().getChild(hs_style);
		if (el == null) {
			el = new Element(hs_style);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the desktop-window's comment font.
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getDesktopCommentfont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPCOMMENTFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getDesktopCommentFont() {
		String style = getDesktopCommentfont(FONTSTYLE);
		String weight = getDesktopCommentfont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getDesktopCommentfont(FONTSIZE));
		return new Font(getDesktopCommentfont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the desktop-window's commentfont.
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setDesktopCommentfont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPCOMMENTFONT);
		if (el == null) {
			el = new Element(SETTING_DESKTOPCOMMENTFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the titlefont (the font used for the main-entry's
	 * title).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getTitleFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_TITLEFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the title font as font-object.
	 *
	 * @return the title-font as {@code Font} variable.
	 */
	public Font getTitleFont() {
		// get the font style.
		String style = getTitleFont(FONTSTYLE);
		String weight = getTitleFont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getTitleFont(FONTSIZE));
		return new Font(getTitleFont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the titlefont (the font used for the main-entry's
	 * title).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setTitleFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_TITLEFONT);
		if (el == null) {
			el = new Element(SETTING_TITLEFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the titlefont (the font used for the main-entry's
	 * title).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getAppendixHeaderFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_APPENDIXHEADERFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the title font as font-object.
	 *
	 * @return the title-font as {@code Font} variable.
	 */
	public Font getAppendixHeaderFont() {
		// get the font style.
		String style = getAppendixHeaderFont(FONTSTYLE);
		String weight = getAppendixHeaderFont(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getAppendixHeaderFont(FONTSIZE));
		return new Font(getAppendixHeaderFont(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the titlefont (the font used for the main-entry's
	 * title).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setAppendixHeaderFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_APPENDIXHEADERFONT);
		if (el == null) {
			el = new Element(SETTING_APPENDIXHEADERFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the header-1-font (the font used for the main-entry's
	 * 1st heading-tags).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getHeaderfont1(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT1);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getHeaderFont1() {
		String style = getHeaderfont1(FONTSTYLE);
		String weight = getHeaderfont1(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getHeaderfont1(FONTSIZE));
		return new Font(getHeaderfont1(FONTNAME), fstyle, fsize);
	}

	/**
	 * Changes settings for the header-1-font (the font used for the main-entry's
	 * 1st heading-tags).
	 *
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 * @param value (the new value for the font-characteristic) - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setHeaderfont1(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT1);
		if (el == null) {
			el = new Element(SETTING_HEADERFONT1);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Retrieves settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getHeaderfont2(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT2);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			case FONTSTYLE:
				retval = el.getAttributeValue("style");
				break;
			case FONTWEIGHT:
				retval = el.getAttributeValue("weight");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getHeaderFont2() {
		String style = getHeaderfont2(FONTSTYLE);
		String weight = getHeaderfont2(FONTWEIGHT);
		// init default values
		int fstyle = Font.PLAIN;
		// convert the css-string-style into a font-integer-style
		switch (style) {
		case "normal":
			fstyle = Font.PLAIN;
			break;
		case "italic":
			fstyle = Font.ITALIC;
			break;
		}
		// in css, the bold-property is not a style-attribute, but a
		// font-weight-attribute
		// that's why we have separated this here
		if (weight.equals("bold")) {
			fstyle = fstyle + Font.BOLD;
		}
		// convert the size
		int fsize = Integer.parseInt(getHeaderfont2(FONTSIZE));
		return new Font(getHeaderfont2(FONTNAME), fstyle, fsize);
	}

	/**
	 * Retrieves settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getQuoteFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_QUOTEFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param what (indicates, which font-characteristic we want to have. use
	 *             following constants:<br>
	 *             - FONTNAME<br>
	 *             - FONTSIZE<br>
	 *             - FONTCOLOR<br>
	 *             - FONTSTYLE<br>
	 *             - FONTWEIGHT<br>
	 * @return the related font-information as string.
	 */
	public String getEntryHeaderFont(int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERFONT);
		String retval = "";
		if (el != null) {
			switch (what) {
			case FONTNAME:
				retval = el.getText();
				break;
			case FONTSIZE:
				retval = el.getAttributeValue("size");
				break;
			case FONTCOLOR:
				retval = el.getAttributeValue("color");
				break;
			}
		}
		return retval;
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getQuoteFont() {
		// convert the size
		int fsize = Integer.parseInt(getQuoteFont(FONTSIZE));
		return new Font(getQuoteFont(FONTNAME), Font.PLAIN, fsize);
	}

	/**
	 * Retrieves the header font as font-object.
	 *
	 * @return the header-font as {@code Font} variable.
	 */
	public Font getEntryHeaderFont() {
		// convert the size
		int fsize = Integer.parseInt(getEntryHeaderFont(FONTSIZE));
		return new Font(getEntryHeaderFont(FONTNAME), Font.PLAIN, fsize);
	}

	/**
	 * Changes settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setHeaderfont2(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT2);
		if (el == null) {
			el = new Element(SETTING_HEADERFONT2);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		case FONTSTYLE:
			el.setAttribute("style", value);
			break;
		case FONTWEIGHT:
			el.setAttribute("weight", value);
			break;
		}
	}

	/**
	 * Changes settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setQuoteFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_QUOTEFONT);
		if (el == null) {
			el = new Element(SETTING_QUOTEFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		}
	}

	/**
	 * Changes settings for the header-2-font (the font used for the main-entry's
	 * 2nd heading-tags).
	 *
	 * @param value (the new value for the font-characteristic)
	 * @param what  (indicates, which font-characteristic we want to have. use
	 *              following constants:<br>
	 *              - FONTNAME<br>
	 *              - FONTSIZE<br>
	 *              - FONTCOLOR<br>
	 *              - FONTSTYLE<br>
	 *              - FONTWEIGHT<br>
	 */
	public void setEntryHeadeFont(String value, int what) {
		Element el = settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERFONT);
		if (el == null) {
			el = new Element(SETTING_ENTRYHEADERFONT);
			settingsFile.getRootElement().addContent(el);
		}
		switch (what) {
		case FONTNAME:
			el.setText(value);
			break;
		case FONTSIZE:
			el.setAttribute("size", value);
			break;
		case FONTCOLOR:
			el.setAttribute("color", value);
			break;
		}
	}

	public String getManlinkColor() {
		return genericStringGetter(SETTING_MANLINKCOLOR, "0033cc");
	}

	public void setManlinkColor(String value) {
		genericStringSetter(SETTING_MANLINKCOLOR, value);
	}

	public String getFootnoteLinkColor() {
		return genericStringGetter(SETTING_FNLINKCOLOR, "0033cc");
	}

	public void setFootnoteLinkColor(String value) {
		genericStringSetter(SETTING_FNLINKCOLOR, value);
	}

	public String getLinkColor() {
		return genericStringGetter(SETTING_LINKCOLOR, "f2f2f2");
	}

	public void setLinkColor(String value) {
		genericStringSetter(SETTING_LINKCOLOR, value);
	}

	public String getEntryHeadingBackgroundColor() {
		return genericStringGetter(SETTING_ENTRYHEADERBACKGROUNDCOLOR, "f2f2f2");
	}

	public void setEntryHeadingBackgroundColor(String value) {
		genericStringSetter(SETTING_ENTRYHEADERBACKGROUNDCOLOR, value);
	}

	public String getQuoteBackgroundColor() {
		return genericStringGetter(SETTING_QUOTEBACKGROUNDCOLOR, "f2f2f2");
	}

	public void setQuoteBackgroundColor(String value) {
		genericStringSetter(SETTING_QUOTEBACKGROUNDCOLOR, value);
	}

	public String getMainBackgroundColor() {
		return genericStringGetter(SETTING_MAINBACKGROUNDCOLOR, "ffffff");
	}

	public void setMainBackgroundColor(String value) {
		genericStringSetter(SETTING_MAINBACKGROUNDCOLOR, value);
	}

	public String getContentBackgroundColor() {
		return genericStringGetter(SETTING_CONTENTBACKGROUNDCOLOR, "ffffff");
	}

	public void setContentBackgroundColor(String value) {
		genericStringSetter(SETTING_CONTENTBACKGROUNDCOLOR, value);
	}

	public int getStartupEntryNumberMode() {
		return genericIntGetter(SETTING_SHOWATSTARTUP, 1);
	}

	public void setShowAtStartup(int value) {
		genericStringSetter(SETTING_SHOWATSTARTUP, String.valueOf(value));
	}

	/**
	 * This method keeps the selection of the combobox in the search dialog
	 * (CSearchDlg), which stores the information whether the user wanted to search
	 * for entries with a certain create-date, changed-date or both.
	 *
	 * @return the index of the selected item.
	 */
	public int getSearchComboTime() {
		return genericIntGetter(SETTING_SEARCHCOMBOTIME, 0);
	}

	/**
	 * This method keeps the selection of the combobox in the search dialog
	 * (CSearchDlg), which stores the information whether the user wanted to search
	 * for entries with a certain create-date, changed-date or both.
	 *
	 * @param value the index of the selected item.
	 */
	public void setSearchComboTime(int value) {
		genericStringSetter(SETTING_SEARCHCOMBOTIME, String.valueOf(value));
	}

	/**
	 * When the user wants to search for entries with a certain creation or
	 * modified-date, this setting stores the values from the last entered
	 * date-input from the user
	 *
	 * @return a string value, comma separated, which holds to dates: the beginning
	 *         and the end-date of the period the user wanted to search for entries.
	 */
	public String getSearchDateTime() {
		return genericStringGetter(SETTING_SEARCHDATETIME, "");
	}

	/**
	 * When the user wants to search for entries with a certain creation or
	 * modified-date, this setting stores the values from the last entered
	 * date-input from the user
	 *
	 * @param value a string value, comma separated, which holds to dates: the
	 *              beginning and the end-date of the period the user wanted to
	 *              search for entries. these strings are taken from the formatted
	 *              textfields in the CSearchDlg.
	 */
	public void setSearchDateTime(String value) {
		genericStringSetter(SETTING_SEARCHDATETIME, value);
	}

	public boolean getShowIcons() {
		return genericBooleanGetter(SETTING_SHOWICONS);
	}

	public void setShowIcons(boolean value) {
		genericBooleanSetter(SETTING_SHOWICONS, value);
	}

	public boolean getShowAllIcons() {
		return genericBooleanGetter(SETTING_SHOWALLICONS);
	}

	public void setShowAllIcons(boolean value) {
		genericBooleanSetter(SETTING_SHOWALLICONS, value);
	}

	/**
	 * @return returns the last used bibtex-format, i.e. the format (encoding) of
	 *         the currently attached BibTeX file. following constants are used:<br>
	 *         0: UTF-8 (Bibliographix)<br>
	 *         1: UTF-8 (Citavi)<br>
	 *         2: ISO8859_1 (Emacs with AucTex/RefTex)<br>
	 *         3: UTF-8 (Endnote)<br>
	 *         4: ISO8859_1 (JabRef)<br>
	 *         5: UTF-8 (Refworks)
	 */
	public int getLastUsedBibtexFormat() {
		return genericIntGetter(SETTING_LASTUSEDBIBTEXFORMAT, 0);
	}

	/**
	 * Sets the character-encoding of the currently attached BibTeX file.
	 *
	 * @param value set the last used BibTeX format, i.e. the format (encoding) of
	 *              the currently attached BibTeX file. The following constants are
	 *              used:<br>
	 *              0: UTF-8 (Bibliographix)<br>
	 *              1: UTF-8 (Citavi)<br>
	 *              2: ISO8859_1 (Emacs with AucTex/RefTex)<br>
	 *              3: UTF-8 (Endnote)<br>
	 *              4: ISO8859_1 (JabRef)<br>
	 *              5: UTF-8 (Refworks)
	 */
	public void setLastUsedBibtexFormat(int value) {
		genericIntSetter(SETTING_LASTUSEDBIBTEXFORMAT, value);
	}

	/**
	 * @return returns the display-option of the desktop window, i.e. whether
	 *         comments should be displayed in the desktop window or not, or if only
	 *         comments should be displayed.
	 *
	 *         following constants are used:<br>
	 *         <ul>
	 *         <li>Constants.DESKTOP_WITH_COMMENTS</li>
	 *         <li>Constants.DESKTOP_WITHOUT_COMMENTS</li>
	 *         <li>Constants.DESKTOP_ONLY_COMMENTS</li>
	 *         </ul>
	 */
	public int getDesktopCommentDisplayOptions() {
		return genericIntGetter(SETTING_DESKTOPSHOWCOMMENTS, 0);
	}

	/**
	 * Sets the display-option of the desktop window, i.e. whether comments should
	 * be displayed in the desktop window or not, or if only comments should be
	 * displayed.
	 *
	 * @param value the display-option. following constants are used:<br>
	 *              <ul>
	 *              <li>Constants.DESKTOP_WITH_COMMENTS</li>
	 *              <li>Constants.DESKTOP_WITHOUT_COMMENTS</li>
	 *              <li>Constants.DESKTOP_ONLY_COMMENTS</li>
	 *              </ul>
	 */
	public void setDesktopCommentDisplayOptions(int value) {
		genericIntSetter(SETTING_DESKTOPSHOWCOMMENTS, value);
	}

	public String getShowUpdateHintVersion() {
		return genericStringGetter(SETTING_SHOWUPDATEHINTVERSION, "0");
	}

	public void setShowUpdateHintVersion(String currentBuildNr) {
		genericStringSetter(SETTING_SHOWUPDATEHINTVERSION, currentBuildNr);
	}

	public boolean getUseXDGOpen() {
		return genericBooleanGetter(SETTING_USEXDGOPEN);
	}

	/**
	 *
	 * @param value
	 */
	public void setUseXDGOpen(boolean value) {
		genericBooleanSetter(SETTING_USEXDGOPEN, value);
	}

	public boolean getUseCustomCSS(int what) {
		String ch;
		switch (what) {
		case CUSTOM_CSS_ENTRY:
			ch = SETTING_USECUSTOMCSSENTRY;
			break;
		case CUSTOM_CSS_DESKTOP:
			ch = SETTING_USECUSTOMCSSDESKTOP;
			break;
		default:
			ch = SETTING_USECUSTOMCSSENTRY;
			break;
		}
		return genericBooleanGetter(ch);
	}

	/**
	 *
	 * @param what
	 * @param value
	 */
	public void setUseCustomCSS(int what, boolean value) {
		String ch;
		switch (what) {
		case CUSTOM_CSS_ENTRY:
			ch = SETTING_USECUSTOMCSSENTRY;
			break;
		case CUSTOM_CSS_DESKTOP:
			ch = SETTING_USECUSTOMCSSDESKTOP;
			break;
		default:
			ch = SETTING_USECUSTOMCSSENTRY;
			break;
		}
		genericBooleanSetter(ch, value);
	}

	/**
	 * @return returns the currently used icon theme. following constants are
	 *         used:<br>
	 *         0: standard<br>
	 *         1: Tango<br>
	 */
	public int getIconTheme() {
		return genericIntGetter(SETTING_ICONTHEME, 0);
	}

	/**
	 *
	 * @param value sets the currently used icon theme. following constants are
	 *              used:<br>
	 *              0: standard<br>
	 *              1: Tango<br>
	 */
	public void setIconTheme(int value) {
		genericIntSetter(SETTING_ICONTHEME, value);
	}

	public String getIconThemePath() {
		// retrieve basic icon theme
		int theme = getIconTheme();
		// check whether value is out of bounds
		if (theme >= Constants.iconThemes.length) {
			theme = 0;
		}
		// get default path
		String defpath = Constants.standardIconThemePath;
		// check whether we have os x
		if (isMacStyle() || isSeaGlass()) {
			defpath = defpath + "osx/";
		} else {
			defpath = defpath + Constants.iconThemes[theme];
		}
		// return path
		return defpath;
	}

	public boolean getLatexExportCreateFormTags() {
		return genericBooleanGetter(SETTING_LATEXEXPORTFORMTAG);
	}

	public void setLatexExportCreateFormTags(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTFORMTAG, val);
	}

	/**
	 * {@code true} when author-references should be references in a footnote, when
	 * exporting to LaTex. If {@code false}, references are directly in the text.
	 *
	 * @return
	 */
	public boolean getLatexExportFootnoteRef() {
		return genericBooleanGetter(SETTING_LATEXEXPORTFOOTNOTE);
	}

	public void setLatexExportFootnoteRef(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTFOOTNOTE, val);
	}

	public int getLastUsedLatexBibStyle() {
		return genericIntGetter(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, 0);
	}

	public void setLastUsedLatexBibStyle(int value) {
		genericIntSetter(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, value);
	}

	public int getLastUsedLatexDocClass() {
		return genericIntGetter(SETTING_LATEXEXPORTDOCUMENTCLASS, 0);
	}

	public void setLastUsedLatexDocClass(int value) {
		genericIntSetter(SETTING_LATEXEXPORTDOCUMENTCLASS, value);
	}

	public boolean getLatexExportShowAuthor() {
		return genericBooleanGetter(SETTING_LATEXEXPORTSHOWAUTHOR);
	}

	public void setLatexExportShowAuthor(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTSHOWAUTHOR, val);
	}

	public boolean getLatexExportShowMail() {
		return genericBooleanGetter(SETTING_LATEXEXPORTSHOWMAIL);
	}

	public void setLatexExportShowMail(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTSHOWMAIL, val);
	}

	public boolean getLatexExportConvertQuotes() {
		return genericBooleanGetter(SETTING_LATEXEXPORTCONVERTQUOTES);
	}

	public void setLatexExportConvertQuotes(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTCONVERTQUOTES, val);
	}

	public boolean getLatexExportCenterForm() {
		return genericBooleanGetter(SETTING_LATEXEXPORTCENTERFORM);
	}

	public void setLatexExportCenterForm(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTCENTERFORM, val);
	}

	public boolean getLatexExportRemoveNonStandardTags() {
		return genericBooleanGetter(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS);
	}

	public void setLatexExportRemoveNonStandardTags(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS, val);
	}

	public boolean getLatexExportNoPreamble() {
		return genericBooleanGetter(SETTING_LATEXEXPORTNOPREAMBLE);
	}

	public void setLatexExportNoPreamble(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTNOPREAMBLE, val);
	}

	public boolean getLatexExportConvertUmlaut() {
		return genericBooleanGetter(SETTING_LATEXEXPORTCONVERTUMLAUT);
	}

	public void setLatexExportConvertUmlaut(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTCONVERTUMLAUT, val);
	}

	public boolean getLatexExportStatisticTableStyle() {
		return genericBooleanGetter(SETTING_LATEXEXPORTTABLESTATSTYLE);
	}

	public void setLatexExportStatisticTableStyle(boolean val) {
		genericBooleanSetter(SETTING_LATEXEXPORTTABLESTATSTYLE, val);
	}

	public String getLatexExportAuthorValue() {
		return genericStringGetter(SETTING_LATEXEXPORTAUTHORVALUE, "");
	}

	public void setLatexExportAuthorValue(String value) {
		genericStringSetter(SETTING_LATEXEXPORTAUTHORVALUE, value);
	}

	public String getLatexExportMailValue() {
		return genericStringGetter(SETTING_LATEXEXPORTMAILVALUE, "");
	}

	public void setLatexExportMailValue(String value) {
		genericStringSetter(SETTING_LATEXEXPORTMAILVALUE, value);
	}

	private void genericElementInitIfMissing(String attr, String value) {
		if (settingsFile.getRootElement().getChild(attr) == null) {
			Element el = new Element(attr);
			el.setText(value);
			settingsFile.getRootElement().addContent(el);
		}
	}

	private File genericDirGetter(String key) {
		// we do this step by step rather that appending a ".getText()" to the line
		// below, because
		// by doing so we can check whether the child element exists or not, and
		// avoiding null pointer
		// exceptions
		// first, get the filepath, which is in relation to the zkn-path
		Element el = settingsFile.getRootElement().getChild(key);
		// create an empty string as return value
		String value = "";
		// is the element exists, copy the text to the return value
		if (el != null) {
			value = el.getText();
		}
		// when we have no filename, return null
		if (value.isEmpty()) {
			return null;
		}
		// else return filepath
		return new File(value);
	}

	private void genericDirSetter(String key, File fp) {
		// try to find filepath-element
		Element el = settingsFile.getRootElement().getChild(key);
		if (el == null) {
			el = new Element(key);
			settingsFile.getRootElement().addContent(el);
		}
		// set new file path which should be now relative to the zkn-path
		el.setText((fp == null) ? "" : FileOperationsUtil.getFilePath(fp));
	}

	private boolean genericBooleanGetter(String key) {
		Element el = settingsFile.getRootElement().getChild(key);
		if (el != null) {
			return el.getText().equals("1");
		}
		return false;
	}

	private void genericBooleanSetter(String key, boolean val) {
		Element el = settingsFile.getRootElement().getChild(key);
		if (el == null) {
			el = new Element(key);
			settingsFile.getRootElement().addContent(el);
		}
		el.setText((val) ? "1" : "0");
	}

	/**
	 * Returns the setting (saved value) for integer values. Return the argument
	 * {@code defaultValue} if element does not exist in the settings file.
	 * 
	 * @param key          the key of the specific settings
	 * @param defaultValue a default value that will be returned in case the setting
	 *                     {@code key} does not exist.
	 * @return the saved setting for {@code key} as integer value.
	 */
	private int genericIntGetter(String key, int defaultValue) {
		Element el = settingsFile.getRootElement().getChild(key);
		if (el != null) {
			try {
				return Integer.parseInt(el.getText());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	private void genericIntSetter(String key, int val) {
		Element el = settingsFile.getRootElement().getChild(key);
		if (el == null) {
			el = new Element(key);
			settingsFile.getRootElement().addContent(el);
		}
		el.setText(String.valueOf(val));
	}

	private String genericStringGetter(String key, String defaultValue) {
		Element el = settingsFile.getRootElement().getChild(key);
		String retval = defaultValue;
		if (el != null) {
			retval = el.getText();
		}
		return retval;
	}

	private void genericStringSetter(String key, String val) {
		Element el = settingsFile.getRootElement().getChild(key);
		if (el == null) {
			el = new Element(key);
			settingsFile.getRootElement().addContent(el);
		}
		if (val != null) {
			el.setText(val);
		} else {
			el.setText("");
		}
	}
}
