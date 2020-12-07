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

import de.danielluedecke.zettelkasten.CImportBibTex;
import de.danielluedecke.zettelkasten.CSetBibKey;
import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author danielludecke
 */
public class Settings {
    
    /**
     * A reference to the accelerator keys class
     */
    private final AcceleratorKeys acceleratorKeys;
    /**
     * A reference to the auto-correction class
     */
    private final AutoKorrektur autoKorrekt;
    /**
     * A reference to the synonyms-class
     */
    private final Synonyms synonyms;
    /**
     * A reference to the steno class
     */
    private final StenoData steno;
    /**
     * Stores the filepath of the currently in use settings file
     */
    private final File filepath;
    /**
     * Stores the filepath of the currently in use metadatafile
     */
    private final File datafilepath;
    /**
     * XML-Document that stores the settings-information
     */
    private Document settingsFile;    
    /**
     * XML-Document that stores the foreign-words
     */
    private Document foreignWordsFile;
    /**
     * Stores the files which we want to retrieve from the settings-file (zettelkasten-settings.zks3).
     * This file is a zip-container with the file-extension ".zks3" and contains several XML-Files.
     * We cannot retrieve those file simply with the method "zip.getNextEntry()", since the SAXBuilder
     * closes the zip-inputstream. To retrieve all XML-files from within the zip-file, without saving
     * them temporarily to harddisk(!), we need to re-open the zip-container again for each file.
     * 
     * See method "loadSettings" below for more details.
     */
    private final List<String> filesToLoad = new ArrayList<>();
    /**
     * Stores the files which we want to retrieve from the meta-data-file (zettelkasten-data.zkd3).
     * This file is a zip-container with the file-extension ".zkd3" and contains several XML-Files.
     * We cannot retrieve those file simply with the method "zip.getNextEntry()", since the SAXBuilder
     * closes the zip-inputstream. To retrieve all XML-files from within the zip-file, without saving
     * them temporarily to harddisk(!), we need to re-open the zip-container again for each file.
     * 
     * See method "loadSettings" below for more details.
     */
    private final List<String> dataFilesToLoad = new ArrayList<>();
    
    public boolean isSeaGlass() {
        return getLookAndFeel().equals(Constants.seaGlassLookAndFeelClassName);
    }
    public boolean isNimbus() {
        return getLookAndFeel().contains("nimbus");
    }
    /**
     * Indicates whether the memory-logging in the main window is activated.
     * See method {@code toggleMemoryTimer()} in {@code ZettelkastenView.class}.
     * We store this toggle as a "global variable", so we can check whether memory-logging
     * is enbaled or not from different jFrames...
     */
    public boolean isMemoryUsageLogged = false;
    /**
     * 
     */
    private boolean highlightSegments = false;
    /**
     * This variable stores an entry-number that was passed as parameter.
     */
    private int initialParamEntry = -1;


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
    /**
     * Amount of stored recent documents
     */
    private static final int recentDocCount = 8;
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

    public static final String SETTING_LOGKEYWORDLIST_OR = "OR";
    public static final String SETTING_LOGKEYWORDLIST_AND = "AND";

    public static final String FONT_ARIAL = "Arial";
    public static final String FONT_TIMES = "Times";
    public static final String FONT_HELVETICA = "Helvetica";
    public static final String FONT_COURIER = "Courier";
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ZettelkastenView.class);

    /**
     * This class stores all relevant settings for the zettelkasten. we don't use the properties api
     * or something like that, since we store more data than just settings in our settings-file.
     * furthermore, saving settings like font-colors etc. in an xml-file allows easier moving of the
     * whole program including settings-data to other directories or platforms/coputers.
     * <br><br>
     * We combine several xml-files and compress them into a single zip-container, named
     * "zettelkasten-settings.zks3". here we store the settings as xml-file, the foreign-words-file,
     * the synonyms-file and several xml-files that store the accelerator-keys for the different
     * windows.
     *
     * @param ak
     * @param ac
     * @param syn
     * @param stn
     */
    public Settings(AcceleratorKeys ak, AutoKorrektur ac, Synonyms syn, StenoData stn) {
        // first of all, store the reference to the CAcceleratorKeys-class, because we
        // are loading information from this class and pass them to the accKeys class
        acceleratorKeys = ak;
        autoKorrekt = ac;
        synonyms = syn;
        steno = stn;
        // here we add all files which are stored in the zipped settings-file in a list-array
        filesToLoad.add(Constants.settingsFileName);
        filesToLoad.add(Constants.acceleratorKeysMainName);
        filesToLoad.add(Constants.acceleratorKeysNewEntryName);
        filesToLoad.add(Constants.acceleratorKeysDesktopName);
        filesToLoad.add(Constants.acceleratorKeysSearchResultsName);
        // here we add all files which are stored in the zipped meta-data-file in a list-array
        dataFilesToLoad.add(Constants.foreignWordsName);
        dataFilesToLoad.add(Constants.synonymsFileName);
        dataFilesToLoad.add(Constants.autoKorrekturFileName);
        dataFilesToLoad.add(Constants.stenoFileName);
        // create file path to settings file
        filepath = createFilePath("zettelkasten-settings.zks3");
        datafilepath = createFilePath("zettelkasten-data.zkd3");
        // now initiate some empty xml-documents, which store the information
        initDocuments();
    }

    private File createFilePath(String filename) {
        // these lines are needed for the portable use of the Zettelkasten
        // we check whether there's a settings-file in the application's directory.
        // if so, we use these settings-files, else we use and create in a directory
        // relative to the user's home-dir.
        File portableFile = new File(System.getProperty("user.dir") + File.separatorChar + filename);
        // check whether we found a settings-file or not...
        if (portableFile.exists()) {
            return portableFile;
        }
        // if we found no settings-file in the same directory as the application is stored,
        // we assume we have no portable version in use... In this case, store settings-file
        // in a sub-directory of the user's home-dir.
        String fp = System.getProperty("user.home") + File.separatorChar;
        // first of all, we want to check for a subdirectory ".Zettelkasten" in the user's home-directory
        File fpdir = new File(System.getProperty("user.home") + File.separatorChar + ".Zettelkasten");
        // if that directory doesn't exist, try to create it
        if (!fpdir.exists()) {
            try {
                if (fpdir.mkdir()) {
                    fp = fp + ".Zettelkasten" + File.separatorChar;
                }
            } catch (SecurityException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                return null;
            }
        } // else add ".Zettelkasten"-directory to our string
        else {
            fp = fp + ".Zettelkasten" + File.separatorChar;
        }
        // append filename
        fp = fp + filename;
        // return result
        return new File(fp);
    }

    /**
     * Returns the data-filepath, i.e. the path to the {@code zettelkasten-data.zkd3} file, where
     * the synonyms, foreign words, steno and spellchecking-data is saved. this method is used from
     * the main frames makeExtraBackup() method.
     *
     * @return the filepath to the zettelkasten-data.zkd3-file.
     */
    public File getMetaFilePath() {
        return datafilepath;
    }

    /**
     * Inits all documents, i.e. creates new document elements
     */
    private void initDocuments() {
        // first of all, create the empty documents
        settingsFile = new Document(new Element("settings"));
        foreignWordsFile = new Document(new Element("foreignwords"));
        // now fill the initoal elements
        fillElements();
    }

    /**
     * Inits all documents, i.e. creates new document elements
     */
    public void clear() {
        initDocuments();
    }

    /**
     * This method adds the file from the filepath {@code fp} to the list of recent documents and
     * rotates that list, if necessary.
     *
     * @param fp the filepath to the document that should be added to the list of recent documents
     */
    public void addToRecentDocs(String fp) {
        // check for valid parameter
        if (null == fp || fp.isEmpty()) {
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
        for (int cnt = 1; cnt <= recentDocCount; cnt++) {
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
        for (int cnt = 1; cnt <= recentDocCount; cnt++) {
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
     * Retrieves the recent document at the position {@code nr}. Returns {@code null} if recent
     * document does not exist or is empty
     *
     * @param nr the number of the requested recent document. use a value from 1 to
     * {@link #recentDocCount recentDocCount}.
     * @return the recent document (the file path) as string, or {@code null} if no such element or
     * path exists.
     */
    public String getRecentDoc(int nr) {
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if we have any valid document
        if (el != null) {
            // check whether its value is empty
            String retval = el.getText();
            // and if not, return in
            if (!retval.isEmpty()) {
                return retval;
            }
        }
        // else return null
        return null;
    }

    /**
     * Add a new recent document to the position {@code nr} in the list of recent documents.
     *
     * @param nr the number of the requested recent document. use a value from 1 to
     * {@link #recentDocCount recentDocCount}.
     * @param fp the filepath to the recently used document as string
     */
    public void setRecentDoc(int nr, String fp) {
        // check for valid parameter
        if (null == fp) {
            return;
        }
        // retrieve element
        Element el = settingsFile.getRootElement().getChild(SETTING_RECENT_DOC + String.valueOf(nr));
        // if element exists...
        if (el != null) {
            // add filepath
            el.setText(fp);
        } else {
            // create a filepath-element
            el = new Element(SETTING_RECENT_DOC + String.valueOf(nr));
            // add filepath
            el.setText(fp);
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
    }

    public Color getTableGridColor() {
        return ((getShowGridHorizontal() || getShowGridVertical()) ? Constants.gridcolor : Constants.gridcolortransparent);
    }

    /**
     * If an entry-number was passed as paramter, use this method to store the entry-number, so the
     * entry can be displayed immediately after opening a data file. Use -1 to indicate that no
     * parameter-entry-number should be set
     *
     * @return the entry-number which was passed as parameter, or -1 if no such paramter was passed
     */
    public int getInitialParamZettel() {
        return initialParamEntry;
    }

    /**
     * If an entry-number was passed as paramter, use this method to store the entry-number, so the
     * entry can be displayed immediately after opening a data file. Use -1 to indicate that no
     * parameter-entry-number should be set
     *
     * @param nr the entry-number which was passed as parameter, or -1 if no such paramter was
     * passed
     */
    public void setInitialParamZettel(int nr) {
        initialParamEntry = nr;
    }

    /**
     * This method creates all the settings-child-elements, but only, if they don't already exist.
     * We do this because when loading older settings-xml-document-structures, we might have new
     * elements that would not be initialised. but now we can call this method after loading the
     * xml-document, and create elements and default values for all new elements. This ensures
     * compatibility to older/news settings-file-versions.
     */
    public void fillElements() {
        for (int cnt = 0; cnt < recentDocCount; cnt++) {
            // create field-identifier
            String fi = SETTING_RECENT_DOC + String.valueOf(cnt + 1);
            // retrieve content
            if (null == settingsFile.getRootElement().getChild(fi)) {
                // create a filepath-element
                Element el = new Element(fi);
                el.setText("");
                // and add it to the document
                settingsFile.getRootElement().addContent(el);
            }
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_LAF)) {
            // create element for look and feel
            Element el = new Element(SETTING_LAF);
            settingsFile.getRootElement().addContent(el);
            // retrieve all installed Look and Feels
            UIManager.LookAndFeelInfo[] installed_laf = UIManager.getInstalledLookAndFeels();
            // init found-variables
            boolean laf_aqua_found = false;
            boolean laf_nimbus_found = false;
            String aquaclassname = "";
            String nimbusclassname = "";
            // in case we find "nimbus" LAF, set this as default on non-mac-os
            // because it simply looks the best.
            for (UIManager.LookAndFeelInfo laf : installed_laf) {
                // check whether laf is mac os x
                if (laf.getName().equalsIgnoreCase("mac os x") || laf.getClassName().contains("Aqua")) {
                    laf_aqua_found = true;
                    aquaclassname = laf.getClassName();
                }
                // check whether laf is nimbus
                if (laf.getName().equalsIgnoreCase("nimbus") || laf.getClassName().contains("Nimbus")) {
                    laf_nimbus_found = true;
                    nimbusclassname = laf.getClassName();
                }
            }
            // check which laf was found and set appropriate default value
            if (laf_aqua_found) {
                el.setText(aquaclassname);
            } else if (laf_nimbus_found) {
                el.setText(nimbusclassname);
            } else {
                el.setText(UIManager.getSystemLookAndFeelClassName());
            }
        }
        
        // init standard font. on mac, it's helvetica
        String font = "Helvetica";
        // on older windows arial
        if (System.getProperty("os.name").startsWith("Windows")) {
            font = "Arial";
            // on new windows Calibri
            if (System.getProperty("os.name").startsWith("Windows 7") || System.getProperty("os.name").startsWith("Windows 8")) {
                font = "Calibri";
            }
        } // and on linux we take Nimbus Sans L Regular
        else if (System.getProperty("os.name").startsWith("Linux")) {
            font = "Nimbus Sans L Regular";
        }
        
        String pandoc = "pandoc";
        if (PlatformUtil.isMacOS()) {
            pandoc = "/usr/local/bin/pandoc";
        } else if (PlatformUtil.isLinux()) {
            pandoc = "/usr/bin/pandoc";
        }
        genericElementInit(SETTING_PANDOCPATH, pandoc);
        genericElementInit(SETTING_DISPLAYEDTOOLBARICONS, 
                PlatformUtil.isMacOS() ? 
                        "1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,1,0,1,0,0,1,1,1,1,1" : 
                        "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1");
        genericElementInit(SETTING_LOCALE, Locale.getDefault().getLanguage());
        genericElementInit(SETTING_FILEPATH, "");
        genericElementInit(SETTING_SEARCHFRAMESPLITLAYOUT, String.valueOf(JSplitPane.VERTICAL_SPLIT));
        genericElementInit(SETTING_CUSTOMCSSENTRY, "");
        genericElementInit(SETTING_CUSTOMCSSDESKTOP, "");
        genericElementInit(SETTING_GETLASTUSEDDESKTOPNUMBER, "0");
        genericElementInit(SETTING_AUTOCOMPLETETAGS, "1");
        genericElementInit(SETTING_MARKDOWNACTIVATED, "0");
        genericElementInit(SETTING_LASTOPENEDIMPORTDIR, "");
        genericElementInit(SETTING_LASTOPENEDEXPORTDIR, "");
        genericElementInit(SETTING_LASTOPENEDIMAGEDIR, "");
        genericElementInit(SETTING_SHOWICONTEXT, "1");
        genericElementInit(SETTING_USEMACBACKGROUNDCOLOR, "0");
        genericElementInit(SETTING_LASTOPENEDATTACHMENTDIR, "");
        genericElementInit(SETTING_AUTOBACKUP, "1");
        genericElementInit(SETTING_SHOWALLLUHMANN, "0");
        genericElementInit(SETTING_SHOWLUHMANNICONINDESK, "1");
        genericElementInit(SETTING_EXTRABACKUP, "0");
        genericElementInit(SETTING_ALWAYSMACSTYLE, "0");
        genericElementInit(SETTING_MINIMIZETOTRAY, "0");
        genericElementInit(SETTING_ADDALLTOHISTORY, "0");
        genericElementInit(SETTING_COPYPLAIN, "0");
        genericElementInit(SETTING_EXTRABACKUPPATH, "");
        genericElementInit(SETTING_FILLEMPTYPLACES, "0");
        genericElementInit(SETTING_MANUALTIMESTAMP, "0");
        genericElementInit(SETTING_SEARCHTIME, "");
        genericElementInit(SETTING_SEARCHALWAYSSYNONYMS, "1");
        genericElementInit(SETTING_SHOWSYNONYMSINTABLE, "0");
        genericElementInit(SETTING_SHOWICONS, "1");
        genericElementInit(SETTING_SHOWALLICONS, "1");
        genericElementInit(SETTING_SHOWENTRYHEADLINE, "1");
        genericElementInit(SETTING_ICONTHEME, "0");
        genericElementInit(SETTING_FOOTNOTEBRACES, "1");
        genericElementInit(SETTING_SHOWUPDATEHINTVERSION, "0");
        genericElementInit(SETTING_USECUSTOMCSSENTRY, "0");
        genericElementInit(SETTING_USECUSTOMCSSDESKTOP, "0");
        genericElementInit(SETTING_USEXDGOPEN, "1");
        genericElementInit(SETTING_MANLINKCOLOR, "0033cc");
        genericElementInit(SETTING_FNLINKCOLOR, "0033cc");
        genericElementInit(SETTING_LINKCOLOR, "003399");
        genericElementInit(SETTING_APPENDIXBACKGROUNDCOLOR, "ffffff");
        genericElementInit(SETTING_TABLEHEADERCOLOR, "e4e4e4");
        genericElementInit(SETTING_TABLEEVENROWCOLOR, "eeeeee");
        genericElementInit(SETTING_ENTRYHEADERBACKGROUNDCOLOR, "555555");
        genericElementInit(SETTING_QUOTEBACKGROUNDCOLOR, "f2f2f2");
        genericElementInit(SETTING_MAKELUHMANNCOLUMNSORTABLE, "0");
        genericElementInit(SETTING_MAINBACKGROUNDCOLOR, "ffffff");
        genericElementInit(SETTING_CONTENTBACKGROUNDCOLOR, "ffffff");
        genericElementInit(SETTING_TABLEODDROWCOLOR, "f8f8f8");
        genericElementInit(SETTING_SHOWTABLEBORDER, "1");
        genericElementInit(SETTING_SEARCHWITHOUTFORMATTAGS, "1");
        genericElementInit(SETTING_SHOWLUHMANNENTRYNUMBER, "0");
        genericElementInit(SETTING_SHOWDESKTOPENTRYNUMBER, "0");
        genericElementInit(SETTING_DESKTOPSHOWCOMMENTS, String.valueOf(Constants.DESKTOP_WITH_COMMENTS));
        genericElementInit(SETTING_LASTUSEDBIBTEXFORMAT, "0");
        genericElementInit(SETTING_SHOWHIGHLIGHTBACKGROUND, "1");
        genericElementInit(SETTING_SHOWHIGHLIGHTKEYWORDBACKGROUND, "1");
        genericElementInit(SETTING_SHOWHIGHLIGHTLIVESEARCHBACKGROUND, "1");
        genericElementInit(SETTING_SEARCHCOMBOTIME, "0");
        genericElementInit(SETTING_SEARCHDATETIME, "");
        genericElementInit(SETTING_SEARCHLOG, "0");
        genericElementInit(SETTING_HIGHLIGHTSEARCHRESULTS, "0");
        genericElementInit(SETTING_HIGHLIGHTKEYWORDS, "0");
        genericElementInit(SETTING_SHOWSEARCHENTRY, "0");
        genericElementInit(SETTING_SUPFOOTNOTE, "1");
        genericElementInit(SETTING_TABLEROWSORTING, "");
        genericElementInit(SETTING_JUMPFOOTNOTE, "0");
        genericElementInit(SETTING_STARTUPENTRY, "1");
        genericElementInit(SETTING_SHOWATSTARTUP, "0");
        genericElementInit(SETTING_LOGKEYWORDLIST, SETTING_LOGKEYWORDLIST_OR);
        genericElementInit(SETTING_SHOWGRID_HORIZONTAL, "0");
        genericElementInit(SETTING_SHOWGRID_VERTICAL, "0");
        genericElementInit(SETTING_CELLSPACING, "1,1");
        genericElementInit(SETTING_SPELLCORRECT, "0");
        genericElementInit(SETTING_STENOACTIVATED, "0");
        genericElementInit(SETTING_HIGHLIGHTWHOLEWORD, "0");
        genericElementInit(SETTING_HIGHLIGHTWHOLEWORDSEARCH, "0");
        genericElementInit(SETTING_QUICKINPUT, "0");
        genericElementInit(SETTING_AUTOUPDATE, "1");
        genericElementInit(SETTING_TOCFORDESKTOPEXPORT, "0");
        genericElementInit(SETTING_REMOVELINESFORDESKTOPEXPORT, "1");
        genericElementInit(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG, "0");
        genericElementInit(SETTING_AUTONIGHTLYUPDATE, "0");
        genericElementInit(SETTING_QUICKINPUTEXTENDED, "0");
        genericElementInit(SETTING_IMGRESIZE, "1");
        genericElementInit(SETTING_IMGRESIZEWIDTH, "400");
        genericElementInit(SETTING_IMGRESIZEHEIGHT, "400");
        genericElementInit(SETTING_TABLEFONTSIZE, "0");
        genericElementInit(SETTING_DESKTOPOUTLINEFONTSIZE, "0");
        genericElementInit(SETTING_TEXTFIELDFONTSIZE, "0");
        genericElementInit(SETTING_LASTUSEDSETBIBKEYCHOICE, String.valueOf(CSetBibKey.CHOOSE_BIBKEY_MANUAL));
        genericElementInit(SETTING_LASTUSEDSETBIBKEYTYPE, String.valueOf(CSetBibKey.CHOOSE_BIBKEY_MANUAL));
        genericElementInit(SETTING_LASTUSEDSETBIBIMPORTSOURCE, String.valueOf(CImportBibTex.BIBTEX_SOURCE_DB));
        genericElementInit(SETTING_LATEXEXPORTFOOTNOTE, "0");
        genericElementInit(SETTING_LATEXEXPORTFORMTAG, "0");
        genericElementInit(SETTING_LATEXEXPORTSHOWAUTHOR, "0");
        genericElementInit(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS, "1");
        genericElementInit(SETTING_LATEXEXPORTNOPREAMBLE, "0");
        genericElementInit(SETTING_LATEXEXPORTCONVERTUMLAUT, "1");
        genericElementInit(SETTING_LATEXEXPORTTABLESTATSTYLE, "0");
        genericElementInit(SETTING_LATEXEXPORTSHOWMAIL, "0");
        genericElementInit(SETTING_LATEXEXPORTCONVERTQUOTES, "1");
        genericElementInit(SETTING_LATEXEXPORTCENTERFORM, "1");
        genericElementInit(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, "0");
        genericElementInit(SETTING_LATEXEXPORTDOCUMENTCLASS, "0");
        genericElementInit(SETTING_LATEXEXPORTAUTHORVALUE, "");
        genericElementInit(SETTING_LATEXEXPORTMAILVALUE, "");
        genericElementInit(SETTING_SEARCHWHERE, String.valueOf(Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS));
        genericElementInit(SETTING_REPLACEWHERE, String.valueOf(Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS));
        genericElementInit(SETTING_EXPORTPARTS, String.valueOf(Constants.EXPORT_TITLE | Constants.EXPORT_CONTENT | Constants.EXPORT_AUTHOR | Constants.EXPORT_REMARKS));
        genericElementInit(SETTING_EXPORTFORMAT, String.valueOf(Constants.EXP_TYPE_DESKTOP_DOCX));
        genericElementInit(SETTING_DESKTOPEXPORTFORMAT, String.valueOf(Constants.EXP_TYPE_DESKTOP_DOCX));
        genericElementInit(SETTING_DESKTOPCOMMENTEXPORT, "0");
        genericElementInit(SETTING_DESKTOPDISPLAYITEMS, String.valueOf(Constants.DESKTOP_SHOW_REMARKS | Constants.DESKTOP_SHOW_AUTHORS));
        genericElementInit(SETTING_SEARCHWHAT, "");
        genericElementInit(SETTING_REPLACEWHAT, "");
        genericElementInit(SETTING_SEARCHOPTION, "0");
        genericElementInit(SETTING_REPLACEOPTION, "0");
        genericElementInit(SETTING_LASTUSEDBIBTEXFILE, "");
        genericElementInit(SETTING_HIGHLIGHTBACKGROUNDCOLOR, "ffff66");
        genericElementInit(SETTING_HIGHLIGHTKEYWORDBACKGROUNDCOLOR, "ffff66");
        genericElementInit(SETTING_HIGHLIGHTLIVESEARCHBACKGROUNDCOLOR, "ffff66");
        genericElementInit(SETTING_TABLEFONT, font);
        genericElementInit(SETTING_DESKTOPOUTLINEFONT, font);
        genericElementInit(SETTING_LUHMANNTREEEXPANDLEVEL, "-1");
        
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
            el.setText(font);
            el.setAttribute("size", "11");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "000000");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_QUOTEFONT)) {
            // create element for font
            Element el = new Element(SETTING_QUOTEFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "11");
            el.setAttribute("color", "333333");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERFONT)) {
            // create element for font
            Element el = new Element(SETTING_ENTRYHEADERFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "10");
            el.setAttribute("color", "F4F4F4");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_AUTHORFONT)) {
            // create element for font
            Element el = new Element(SETTING_AUTHORFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "10");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "333333");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_CODEFONT)) {
            // create element for font
            Element el = new Element(SETTING_CODEFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "11");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "333333");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_REMARKSFONT)) {
            // create element for font
            Element el = new Element(SETTING_REMARKSFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "10");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "333333");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPHEADERFONT)) {
            // create element for font
            Element el = new Element(SETTING_DESKTOPHEADERFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "14");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "bold");
            el.setAttribute("color", "000000");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMHEADERFONT)) {
            // create element for font
            Element el = new Element(SETTING_DESKTOPITEMHEADERFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "12");
            el.setAttribute("style", "italic");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "555555");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMFONT)) {
            // create element for font
            Element el = new Element(SETTING_DESKTOPITEMFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "10");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "808080");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_DESKTOPCOMMENTFONT)) {
            // create element for font
            Element el = new Element(SETTING_DESKTOPCOMMENTFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "9");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "normal");
            el.setAttribute("color", "333333");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_TITLEFONT)) {
            // create element for font
            Element el = new Element(SETTING_TITLEFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "13");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "bold");
            el.setAttribute("color", "800000");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_APPENDIXHEADERFONT)) {
            // create element for font
            Element el = new Element(SETTING_APPENDIXHEADERFONT);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "13");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "bold");
            el.setAttribute("color", "555555");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_HEADERFONT1)) {
            // create element for font
            Element el = new Element(SETTING_HEADERFONT1);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "12");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "bold");
            el.setAttribute("color", "000040");
        }

        if (null == settingsFile.getRootElement().getChild(SETTING_HEADERFONT2)) {
            // create element for font
            Element el = new Element(SETTING_HEADERFONT2);
            settingsFile.getRootElement().addContent(el);
            el.setText(font);
            el.setAttribute("size", "11");
            el.setAttribute("style", "normal");
            el.setAttribute("weight", "bold");
            el.setAttribute("color", "000000");
        }
    }

    private void genericElementInit(String attr, String value) {
        if (null == settingsFile.getRootElement().getChild(attr)) {
            // create a filepath-element
            Element el = new Element(attr);
            el.setText(value);
            // and add it to the document
            settingsFile.getRootElement().addContent(el);
        }
    }
    
    
    /**
     * Loads the settings file
     */
    public void loadSettings() {
        // if file exists, go on...
        if (filepath != null && filepath.exists()) {
            // first of all, we load the basic-settings. when we have done this, we load
            // the meta-data, like spellchecking-data, synonyms and foreign words. these files
            // are not related to a certain Zettelkasten data file, but general. thus, they are
            // not stored in the .zkn3-files. however, these meta-data is not only pure settings.
            // it is better to have them separated, in the base-zkn-directory (zkn-path) if possible,
            // so whenever the user removes the program directory, the other data is still there.
            for (String filesToLoad1 : filesToLoad) {
                ZipInputStream zip = null;
                // open the zip file
                try {
                    zip = new ZipInputStream(new FileInputStream(filepath));
                    ZipEntry entry;
                    // now iterate the zip file, searching for the requested file in it
                    while ((entry = zip.getNextEntry()) != null) {
                        String entryname = entry.getName();
                        // if the found file matches the requested one, start the SAXBuilder
                        if (entryname.equals(filesToLoad1)) {
                            try {
                                SAXBuilder builder = new SAXBuilder();
                                Document doc = builder.build(zip);
                                // compare, which file we have retrieved, so we store the data
                                // correctly on our data object
                                if (entryname.equals(Constants.settingsFileName)) {
                                    settingsFile = doc;
                                }
                                if (entryname.equals(Constants.acceleratorKeysMainName)) {
                                    acceleratorKeys.setDocument(AcceleratorKeys.MAINKEYS, doc);
                                }
                                if (entryname.equals(Constants.acceleratorKeysNewEntryName)) {
                                    acceleratorKeys.setDocument(AcceleratorKeys.NEWENTRYKEYS, doc);
                                }
                                if (entryname.equals(Constants.acceleratorKeysDesktopName)) {
                                    acceleratorKeys.setDocument(AcceleratorKeys.DESKTOPKEYS, doc);
                                }
                                if (entryname.equals(Constants.acceleratorKeysSearchResultsName)) {
                                    acceleratorKeys.setDocument(AcceleratorKeys.SEARCHRESULTSKEYS, doc);
                                }
                                break;
                            } catch (JDOMException e) {
                                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                } finally {
                    try {
                        if (zip != null) {
                            zip.close();
                        }
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
                // now fill/create all child-elements that do not already exist
                fillElements();
                acceleratorKeys.initAcceleratorKeys();
            }
        }

        if (datafilepath != null && datafilepath.exists()) {
            // now we load the meta-data. see comment above for more information...
            for (String dataFilesToLoad1 : dataFilesToLoad) {
                ZipInputStream zip = null;
                // open the zip-file
                try {
                    zip = new ZipInputStream(new FileInputStream(datafilepath));
                    ZipEntry entry;
                    // now iterate the zip-file, searching for the requested file in it
                    while ((entry = zip.getNextEntry()) != null) {
                        String entryname = entry.getName();
                        // if the found file matches the requested one, start the SAXBuilder
                        if (entryname.equals(dataFilesToLoad1)) {
                            try {
                                SAXBuilder builder = new SAXBuilder();
                                Document doc = builder.build(zip);
                                // compare, which file we have retrieved, so we store the data
                                // correctly on our data-object
                                if (entryname.equals(Constants.foreignWordsName)) {
                                    foreignWordsFile = doc;
                                }
                                if (entryname.equals(Constants.synonymsFileName)) {
                                    synonyms.setDocument(doc);
                                }
                                if (entryname.equals(Constants.autoKorrekturFileName)) {
                                    autoKorrekt.setDocument(doc);
                                }
                                if (entryname.equals(Constants.stenoFileName)) {
                                    steno.setDocument(doc);
                                }
                                break;
                            } catch (JDOMException e) {
                                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                } finally {
                    try {
                        if (zip != null) {
                            zip.close();
                        }
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * Saves the settings file
     *
     * @return
     */
    public boolean saveSettings() {
        // initial value
        boolean saveok = true;
        ZipOutputStream zip = null;
        // open the output stream
        try {
            zip = new ZipOutputStream(new FileOutputStream(filepath));
            // I first wanted to use a pretty output format, so advanced users who
            // extract the data file can better watch the xml-files. but somehow, this
            // lead to an error within the method "retrieveElement" in the class "Daten.java",
            // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
            // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            XMLOutputter out = new XMLOutputter();
            // save settings
            zip.putNextEntry(new ZipEntry(Constants.settingsFileName));
            out.output(settingsFile, zip);
            // save settings
            // here we retrieve the accelerator key file for the main window
            zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysMainName));
            out.output(acceleratorKeys.getDocument(AcceleratorKeys.MAINKEYS), zip);
            // save settings
            // here we retrieve the acceleratorkey-file for the new-entry-window
            zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysNewEntryName));
            out.output(acceleratorKeys.getDocument(AcceleratorKeys.NEWENTRYKEYS), zip);
            // save settings
            // here we retrieve the acceleratorkey-file for the desktop-window
            zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysDesktopName));
            out.output(acceleratorKeys.getDocument(AcceleratorKeys.DESKTOPKEYS), zip);
            // save settings
            // here we retrieve the acceleratorkey-file for the search-results-window
            zip.putNextEntry(new ZipEntry(Constants.acceleratorKeysSearchResultsName));
            out.output(acceleratorKeys.getDocument(AcceleratorKeys.SEARCHRESULTSKEYS), zip);
            // close zipfile
        } catch (IOException e) {
            // log error
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // change return value
            saveok = false;
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        // first, create temporary backup of data file
        // therefor, create tmp file path
        File tmpdatafp = new File(datafilepath.toString() + ".tmp");
        // check whether we have any saved data at all
        if (datafilepath.exists()) {
            try {
                // check whether we already have a temporary file. if so, delete it
                if (tmpdatafp.exists()) {
                    try {
                        tmpdatafp.delete();
                    } catch (SecurityException e) {
                        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
                // and now copy the datafile
                FileOperationsUtil.copyFile(datafilepath, tmpdatafp, 1024);
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        // save original data-file. in case we get an error here, we can copy
        // back the temporary saved file...
        // open the output stream
        try {
            zip = new ZipOutputStream(new FileOutputStream(datafilepath));
            // I first wanted to use a pretty output format, so advanced users who
            // extract the data file can better watch the xml-files. but somehow, this
            // lead to an error within the method "retrieveElement" in the class "Daten.java",
            // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
            // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            XMLOutputter out = new XMLOutputter();
            // save foreign words
            zip.putNextEntry(new ZipEntry(Constants.foreignWordsName));
            out.output(foreignWordsFile, zip);
            // save settings
            zip.putNextEntry(new ZipEntry(Constants.synonymsFileName));
            out.output(synonyms.getDocument(), zip);
            // save settings
            zip.putNextEntry(new ZipEntry(Constants.stenoFileName));
            out.output(steno.getDocument(), zip);
            // save auto-correction
            zip.putNextEntry(new ZipEntry(Constants.autoKorrekturFileName));
            out.output(autoKorrekt.getDocument(), zip);
            // close zip-file
        } catch (IOException | SecurityException e) {
            // log error message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // change return value
            saveok = false;
            // first, create basic backup-file
            File checkbackup = FileOperationsUtil.getBackupFilePath(datafilepath);
            // rename temporary file as backup-file
            tmpdatafp.renameTo(checkbackup);
            // log path.
            Constants.zknlogger.log(Level.INFO, "A backup of the meta-data was saved to {0}", checkbackup.toString());
            // tell user that an error occurred
            JOptionPane.showMessageDialog(null, resourceMap.getString("metadataSaveErrMsg", "\"" + checkbackup.getName() + "\""),
                    resourceMap.getString("metadataSaveErrTitle"),
                    JOptionPane.PLAIN_MESSAGE);
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        // finally, delete temp-file
        tmpdatafp = new File(datafilepath.toString() + ".tmp");
        // check whether we already have a temporary file. if so, delete it
        if (tmpdatafp.exists()) {
            try {
                tmpdatafp.delete();
            } catch (SecurityException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }
        // return result
        return saveok;
    }

    /**
     * Retrieves the filepath of the last used main datafile
     *
     * @return the filepath of the last used main datafile, or null if no filepath was specified.
     */
    public File getFilePath() {
        /*
         We do this step by step rather that appending a ".getText()" to the line below, because
         by doing so we can check whether the child element exists or not, and avoiding null pointer
         exceptions
         first, get the filepath, which is in relation to the zkn-path
        */
        Element el = settingsFile.getRootElement().getChild(SETTING_FILEPATH);
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

    /**
     * Sets the filepath of the currently used main datafile
     *
     * @param fp (the filepath of the currently used main datafile)
     */
    public void setFilePath(File fp) {
        // try to find filepath-element
        Element el = settingsFile.getRootElement().getChild(SETTING_FILEPATH);
        if (null == el) {
            el = new Element(SETTING_FILEPATH);
            settingsFile.getRootElement().addContent(el);
        }
        // set new file path which should be now relative to the zkn-path
        el.setText((null == fp) ? "" : fp.toString());
    }

    /**
     *
     * @return
     */
    public String getFileName() {
        // get filename and find out where extension begins, so we can just set the filename as title
        File f = getFilePath();
        // check whether we have any valid filepath at all
        if (f != null && f.exists()) {
            String fname = f.getName();
            // find file-extension
            int extpos = fname.lastIndexOf(Constants.ZKN_FILEEXTENSION);
            // set the filename as title
            if (extpos != -1) {
                // return file-name
                return fname.substring(0, extpos);
            }
        }
        return null;
    }
    
    public void setTableSorting(javax.swing.JTable[] tables) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEROWSORTING);
        if (null == el) {
            el = new Element(SETTING_TABLEROWSORTING);
            settingsFile.getRootElement().addContent(el);
        }
        // iterate all tables
        for (javax.swing.JTable t : tables) {
            // check if table is valid
            if (t != null) {
                // get sorter for each table
                javax.swing.DefaultRowSorter sorter = (javax.swing.DefaultRowSorter) t.getRowSorter();
                // get sort keys (column, sort order)
                List<RowSorter.SortKey> sk = sorter.getSortKeys();
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
     * Retrieves the filepath of the last used image path when inserting images to a new entry
     *
     * @return the filepath of the last opened image directory, or null if no filepath was
     * specified.
     */
    public File getLastOpenedImageDir() {
        return genericDirGetter(SETTING_LASTOPENEDIMAGEDIR);
    }

    /**
     * Sets the filepath of the last used image path when inserting images to a new entry
     *
     * @param fp the filepath of the last opened image directory
     */
    public void setLastOpenedImageDir(File fp) {
        genericDirSetter(SETTING_LASTOPENEDIMAGEDIR, fp);
    }

    /**
     * Retrieves the filepath of the last used import path when data was imported
     *
     * @return the filepath of the last opened import directory, or null if no filepath was
     * specified.
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
     * @return the filepath of the last opened import directory, or null if no filepath was
     * specified.
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
     * Retrieves the filepath of the last used image path when inserting images to a new entry
     *
     * @return the filepath of the last opened image directory, or null if no filepath was
     * specified.
     */
    public File getLastOpenedAttachmentDir() {
        return genericDirGetter(SETTING_LASTOPENEDATTACHMENTDIR);
    }

    /**
     * Sets the filepath of the last used image path when inserting images to a new entry
     *
     * @param fp the filepath of the last opened image directory
     */
    public void setLastOpenedAttachmentDir(File fp) {
        genericDirSetter(SETTING_LASTOPENEDATTACHMENTDIR, fp);
    }

    /**
     * Retrieves the filepath for the external backup when leaving the application
     *
     * @return the filepath of the external backup, or null if no path was specified
     */
    public File getExtraBackupPath() {
        return genericDirGetter(SETTING_EXTRABACKUPPATH);
    }

    /**
     * Sets the filepath for the external backup when leaving the application
     *
     * @param fp the filepath of the external backup
     */
    public void setExtraBackupPath(String fp) {
        // try to find filepath-element
        Element el = settingsFile.getRootElement().getChild(SETTING_EXTRABACKUPPATH);
        if (null == el) {
            el = new Element(SETTING_EXTRABACKUPPATH);
            settingsFile.getRootElement().addContent(el);
        }
        // set new file path which should be now relative to the zkn-path
        el.setText(fp);
    }

    /**
     * Retrieves the filepath for the external converter tool "pandoc"
     *
     * @return the filepath for the external converter tool "pandoc"
     */
    public String getPandocPath() {
        // first, get the filepath, which is in relation to the zkn-path
        Element el = settingsFile.getRootElement().getChild(SETTING_PANDOCPATH);
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
        return value;
    }

    /**
     * Sets the filepath for the for the external converter tool "pandoc"
     *
     * @param fp the filepath to the external converter tool "pandoc"
     */
    public void setPandocPath(String fp) {
        // try to find filepath-element
        Element el = settingsFile.getRootElement().getChild(SETTING_PANDOCPATH);
        if (null == el) {
            el = new Element(SETTING_PANDOCPATH);
            settingsFile.getRootElement().addContent(el);
        }
        // set new file path which should be now relative to the zkn-path
        el.setText(fp);
    }

    /**
     * Retrieves the filepath of the last used BibTeX file. we need this path when exporting entries
     * (from the desktop or the export-method from the main frame), and the user wants to create a
     * separate BibTeX-File out of the authors that have been exported.
     *
     * @return the filepath of the last used bixb text file, or null if no path is saved
     */
    public File getLastUsedBibTexFile() {
        return genericDirGetter(SETTING_LASTUSEDBIBTEXFILE);
    }

    /**
     * Sets the filepath of the last used BibTeX file. we need this path when exporting entries
     * (from the desktop or the export-method from the main frame), and the user wants to create a
     * separate BibTeX-File out of the authors that have been exported.
     *
     * @param fp the filepath of the last used BibTeX file
     */
    public void setLastUsedBibTexFile(String fp) {
        // try to find filepath-element
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDBIBTEXFILE);
        if (null == el) {
            el = new Element(SETTING_LASTUSEDBIBTEXFILE);
            settingsFile.getRootElement().addContent(el);
        }
        // set new file path which should be now relative to the zkn-path
        el.setText(fp);
    }

    /**
     * Retrieves the image path, where images used in entries are stored. This is typically the
     * directory "img", which is a sub directory of the filepath directory.
     *
     * @param userpath a path to the user-defined directory for storing images. as default, use the
     * {@code Daten.getUserImagePath()} method to retrieve this path.
     * @param trailingSeparator if true, a file-separator-char will be appended, if false not
     * @return the directory to the img-path, , or an empty string if no path was found
     */
    public String getImagePath(File userpath, boolean trailingSeparator) {
        return getImagePath(userpath, trailingSeparator, Constants.IMAGEPATH_SUBDIR);
    }

    /**
     * Retrieves the image path, where images used in entries are stored. This is typically the
     * directory "forms", which is a sub directory of the filepath directory.
     *
     * @param userpath a path to the user-defined directory for storing images. as default, use the
     * {@code Daten.getUserImagePath()} method to retrieve this path.
     * @param trailingSeparator if true, a file-separator-char will be appended, if false not
     * @return the directory to the img-path, , or an empty string if no path was found
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
            File f = getBaseDir();
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
     * Retrieves the image path, where attachments used in entries are stored. This is typically the
     * directory "attachments", which is a sub directory of the filepath directory.
     *
     * @param userpath
     * @param trailingSeparator if true, a file-separator-char will be appended, if false not
     * @return the directory to the attachment-path, or an empty string if no path was found
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
            File f = getBaseDir();
            // if we have no valid filepath, return empty string
            if (null == f) {
                return "";
            }
            // create a new attachment path from the base dir plus appending "/attachment/" directory
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
     * Gets the startup entry. This is the entry which is displayed immediately after opening a data
     * file.
     *
     * @return the number of the startup entry
     */
    public int getStartupEntry() {
        return genericIntGetter(SETTING_STARTUPENTRY, -1);
    }

    /**
     * Gets the startup entry. This is the entry which is displayed immediately after opening a data
     * file.
     *
     * @param nr (the number of the last viewed/activated entry)
     */
    public void setStartupEntry(int nr) {
        genericIntSetter(SETTING_STARTUPENTRY, nr);
    }

    /**
     * this method gets the initiated fields (checkboxes) for the find-dialog, which is opened from
     * the main window. depending on this variable (and the set bits of it) we can figure out which
     * checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getSearchWhere() {
        return genericIntGetter(SETTING_SEARCHWHERE, Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the find-dialog, which is opened from
     * the main window. depending on this variable (and the set bits of it) we can figure out which
     * checkboxes should be initially selected.
     *
     * @param where an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setSearchWhere(int where) {
        genericIntSetter(SETTING_SEARCHWHERE, where);
    }

    /**
     * this method gets the initiated fields (checkboxes) for the replace-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getReplaceWhere() {
        return genericIntGetter(SETTING_REPLACEWHERE, Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE | Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the replace-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @param where an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setReplaceWhere(int where) {
        genericIntSetter(SETTING_REPLACEWHERE, where);
    }

    /**
     * this method gets the initiated fields (checkboxes) for the desktop-display-dialog, which is
     * opened from the desktop-window. depending on this variable (and the set bits of it) we can
     * figure out which checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getDesktopDisplayItems() {
        return genericIntGetter(SETTING_DESKTOPDISPLAYITEMS, Constants.DESKTOP_SHOW_REMARKS | Constants.DESKTOP_SHOW_AUTHORS);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the desktop-display-dialog, which is
     * opened from the desktop-window. depending on this variable (and the set bits of it) we can
     * figure out which checkboxes should be initially selected.
     *
     * @param items an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setDesktopDisplayItems(int items) {
        genericIntSetter(SETTING_DESKTOPDISPLAYITEMS, items);
    }

    /**
     * Returns the depth of expanded levels from the follower tab.
     * @return the depth of expanded levels from the follower tab.
     */
    public int getLuhmannExpandLevel() {
        return genericIntGetter(SETTING_LUHMANNTREEEXPANDLEVEL, -1);
    }
    
    /**
     * Sets the depths of levels that should be expanded when the follower
     * tab is viewed.
     * 
     * @param level the depths of levels that should be expanded when the 
     * follower tab is shown.
     */
    public void setLuhmannExpandLevel(int level) {
        genericIntSetter(SETTING_LUHMANNTREEEXPANDLEVEL, level);
    }
    
    /**
     * this method gets the initiated fields (checkboxes) for the find-dialog, which is opened from
     * the main window. depending on this variable (and the set bits of it) we can figure out which
     * checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getSearchOptions() {
        return genericIntGetter(SETTING_SEARCHOPTION, 0);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the find-dialog, which is opened from
     * the main window. depending on this variable (and the set bits of it) we can figure out which
     * checkboxes should be initially selected.
     *
     * @param nr an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setSearchOptions(int nr) {
        genericIntSetter(SETTING_SEARCHOPTION, nr);
    }

    /**
     * this method gets the initiated fields (checkboxes) for the export-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getExportParts() {
        return genericIntGetter(SETTING_EXPORTPARTS, 0);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the export-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @param val an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setExportParts(int val) {
        genericIntSetter(SETTING_EXPORTPARTS, val);
    }

    /**
     * @return an integer value indicating which export-format (docx, rtf, txt...) was lastly
     * selected by the user.
     */
    public int getExportFormat() {
        return genericIntGetter(SETTING_EXPORTFORMAT, 0);
    }

    /**
     * @param val an integer value indicating which export-format (docx, rtf, txt...) was lastly
     * selected by the user.
     */
    public void setExportFormat(int val) {
        genericIntSetter(SETTING_EXPORTFORMAT, val);
    }

    /**
     * @return an integer value indicating which export-format (docx, rtf, txt...) was lastly
     * selected by the user.
     */
    public int getDesktopExportFormat() {
        return genericIntGetter(SETTING_DESKTOPEXPORTFORMAT, 0);
    }

    /**
     * @param val an integer value indicating which export-format (docx, rtf, txt...) was lastly
     * selected by the user.
     */
    public void setDesktopExportFormat(int val) {
        genericIntSetter(SETTING_DESKTOPEXPORTFORMAT, val);
    }

    /**
     * @return an integer value indicating whether the user wants to export the desktop-data
     * (entries) with their comments, without comments or if only entries with comments at all
     * should be exported.
     * <br><br>
     * Returns on of the following constants:<br> - {@code EXP_COMMENT_YES}<br> -
     * {@code EXP_COMMENT_NO}<br> - {@code EXP_COMMENT_ONLY}<br>
     */
    public int getDesktopCommentExport() {
        return genericIntGetter(SETTING_DESKTOPCOMMENTEXPORT, 0);
    }

    /**
     * @param val an integer value indicating whether the user wants to export the desktop-data
     * (entries) with their comments, without comments or if only entries with comments at all
     * should be exported.
     * <br><br>
     * Use following constants:<br> - {@code EXP_COMMENT_YES}<br> - {@code EXP_COMMENT_NO}<br> -
     * {@code EXP_COMMENT_ONLY}<br>
     */
    public void setDesktopCommentExport(int val) {
        genericIntSetter(SETTING_DESKTOPCOMMENTEXPORT, val);
    }

    /**
     * this method gets the initiated fields (checkboxes) for the replace-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @return an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public int getReplaceOptions() {
        return genericIntGetter(SETTING_REPLACEOPTION, 0);
    }

    /**
     * this method sets the initiated fields (checkboxes) for the replace-dialog, which is opened
     * from the main window. depending on this variable (and the set bits of it) we can figure out
     * which checkboxes should be initially selected.
     *
     * @param nr an integer value, where the single bits indicate whether a checkbox should be
     * selected or not.
     */
    public void setReplaceOptions(int nr) {
        genericIntSetter(SETTING_REPLACEOPTION, nr);
    }

    /**
     * This method gets the last used search term which was entered in the main window's find
     * dialog.
     *
     * @return the last used search term for the find dialog
     */
    public String getSearchWhat() {
        return genericStringGetter(SETTING_SEARCHWHAT, "");
    }

    /**
     * This method sets the last used search term which was entered in the main window's find
     * dialog.
     *
     * @param searchterm the last used search term for the find dialog
     */
    public void setSearchWhat(String searchterm) {
        genericStringSetter(SETTING_SEARCHWHAT, searchterm);
    }

    /**
     * This method gets the last used replace term which was entered in the main window's replace
     * dialog.
     *
     * @return the last used replaceterm for the replace dialog
     */
    public String getReplaceWhat() {
        return genericStringGetter(SETTING_REPLACEWHAT, "");
    }

    /**
     * This method sets the last used replace term which was entered in the main window's replace
     * dialog.
     *
     * @param replaceterm the last used replace term for the replace dialog
     */
    public void setReplaceWhat(String replaceterm) {
        genericStringSetter(SETTING_REPLACEWHAT, replaceterm);
    }

    /**
     * Retrieves the base filepath, i.e. the usual directory where the data file is stored. Setting
     * this base path enables relative path-settings for images, data files and e.g. linked files
     * (like DOCXs etc.), so the user can easily move his "data directory" and then simply change
     * the base path.
     *
     * @return {@code null}, if no value is set... else the directory of the data-file,
     * <i>without</i> trailing separator char
     */
    public File getBaseDir() {
        // first, get the filepath, which is in relation to the zkn-path
        Element el = settingsFile.getRootElement().getChild(SETTING_FILEPATH);
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
        // find last separator char to get the base-directory of the data-file
        int index = value.lastIndexOf(String.valueOf(File.separatorChar));
        // if nothing found, return null
        if (-1 == index) {
            return null;
        }
        try {
            // else cut off the filename, so we only have the data-file's directory
            value = value.substring(0, index);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
        // else return filepath
        return new File(value);
    }

    /**
     * Retrieves the logical combination for filtering the link-list when the user selectes a
     * keyword in the jListKeywords. See method "filterLinks()" in "ZettelkastenView.java" for more
     * details
     *
     * @return
     */
    public String getLogKeywordlist() {
        return genericStringGetter(SETTING_LOGKEYWORDLIST, "");
    }

    /**
     * Sets the logical combination for filtering the link-list when the user selectes a keyword in
     * the jListKeywords. See method "filterLinks()" in "ZettelkastenView.java" for more details
     *
     * @param path
     */
    public void setLogKeywordlist(String path) {
        genericStringSetter(SETTING_LOGKEYWORDLIST, path);
    }

    /**
     * Retrieves the setting which java-look'n'feel the user wants to have set
     *
     * @return the string for the look'n'feel's class name
     */
    public String getLookAndFeel() {
        return genericStringGetter(SETTING_LAF, "");
    }

    /**
     * Saves the look'n'feel setting so we know which look'n'feel to be set when the program is
     * started.
     *
     * @param laf (the look'n'feel's classname)
     */
    public void setLookAndFeel(String laf) {
        genericStringSetter(SETTING_LAF, laf);
    }

    /**
     * Gets the show-grid-variable. If true, the <i>horizontal</i> grids in lists and tables should
     * be displayed.
     *
     * @return {@code true} if the <i>horizontal</i> grids in lists and tables should be displayed,
     * flase otherwise
     */
    public boolean getShowGridHorizontal() {
        return genericBooleanGetter(SETTING_SHOWGRID_HORIZONTAL);
    }

    /**
     * Sets the show-grid-variable. If true, the <i>horizontal</i> grids in lists and tables should
     * be displayed.
     *
     * @param show true if the grids should be displayed, false otherweise
     */
    public void setShowGridHorizontal(boolean show) {
        genericBooleanSetter(SETTING_SHOWGRID_HORIZONTAL, show);
    }

    /**
     * Gets the show-grid-variable. If true, the <i>vertical</i> grids in lists and tables should be
     * displayed.
     *
     * @return {@code true} if the <i>vertical</i> grids in lists and tables should be displayed,
     * flase otherwise
     */
    public boolean getShowGridVertical() {
        return genericBooleanGetter(SETTING_SHOWGRID_VERTICAL);
    }

    /**
     * Sets the show-grid-variable. If true, the <i>vertical</i> grids in lists and tables should be
     * displayed.
     *
     * @param show true if the grids should be displayed, false otherweise
     */
    public void setShowGridVertical(boolean show) {
        genericBooleanSetter(SETTING_SHOWGRID_VERTICAL, show);
    }

    /**
     * Whether all follower entries, including top-level parent follower, should be shown in
     * trailing entries or not.
     *
     * @return val {@code true} if all trailing entries, including top-level parent follower, should
     * be shown in trailing entries; {@code false} otherwise.
     */
    public boolean getShowAllLuhmann() {
        return genericBooleanGetter(SETTING_SHOWALLLUHMANN);
    }

    /**
     * Whether all follower entries, including top-level parent follower, should be shown in
     * trailing entries or not.
     *
     * @param val {@code true} if all trailing entries, including top-level parent follower, should
     * be shown in trailing entries; {@code false} otherwise.
     */
    public void setShowAllLuhmann(boolean val) {
        genericBooleanSetter(SETTING_SHOWALLLUHMANN, val);
    }

    /**
     * Whether all follower entries, including top-level parent follower, should be shown in
     * trailing entries or not.
     *
     * @return val {@code true} if all trailing entries, including top-level parent follower, should
     * be shown in trailing entries; {@code false} otherwise.
     */
    public boolean getShowLuhmannIconInDesktop() {
        return genericBooleanGetter(SETTING_SHOWLUHMANNICONINDESK);
    }

    /**
     * Whether all follower entries, including top-level parent follower, should be shown in
     * trailing entries or not.
     *
     * @param val {@code true} if all trailing entries, including top-level parent follower, should
     * be shown in trailing entries; {@code false} otherwise.
     */
    public void setShowLuhmannIconInDesktop(boolean val) {
        genericBooleanSetter(SETTING_SHOWLUHMANNICONINDESK, val);
    }

    /**
     * Whether or not the searches from the tables, which are not started via the find-dialog, but
     * via the (context-)menus, should include synonym-search or not.
     *
     * @return {@code true} if the search should include synonyms, false otherwise
     */
    public boolean getSearchAlwaysSynonyms() {
        return genericBooleanGetter(SETTING_SEARCHALWAYSSYNONYMS);
    }

    /**
     * Whether or not the searches from the tables, which are not started via the find-dialog, but
     * via the (context-)menus, should include synonym-search or not.
     *
     * @param val true if the search should include synonyms, false otherwise
     */
    public void setSearchAlwaysSynonyms(boolean val) {
        genericBooleanSetter(SETTING_SEARCHALWAYSSYNONYMS, val);
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
     * @return {@code true} keyword-synonyms should be displayed in the jTableKeywords, false
     * otherwise
     */
    public boolean getShowSynonymsInTable() {
        return genericBooleanGetter(SETTING_SHOWSYNONYMSINTABLE);
    }

    /**
     * Whether or not keyword-synonyms should be displayed in the jTableKeywords
     *
     * @param val true keyword-synonyms should be displayed in the jTableKeywords, false otherwise
     */
    public void setShowSynonymsInTable(boolean val) {
        genericBooleanSetter(SETTING_SHOWSYNONYMSINTABLE, val);
    }

    /**
     * This setting gets the spacing between table cells.
     *
     * @return a dimension value, holding the horizontal and vertical cellspacing-values
     */
    public Dimension getCellSpacing() {
        Element el = settingsFile.getRootElement().getChild(SETTING_CELLSPACING);
        if (el != null) {
            // parse both string-values to an array
            String[] dummy = el.getText().split(",");
            // first value indicates horizontal spacing
            int space_hor = Integer.parseInt(dummy[0]);
            // second value indicates vertical distance
            int space_ver = Integer.parseInt(dummy[1]);
            // return values as dimension
            return new Dimension(space_hor, space_ver);
        }
        return new Dimension(1, 1);
    }

    /**
     * This setting stores the spacing between table cells.
     *
     * @param hor the horizontal distance between the table cells
     * @param ver the vertical distance between the table cells
     */
    public void setCellSpacing(int hor, int ver) {
        Element el = settingsFile.getRootElement().getChild(SETTING_CELLSPACING);
        if (null == el) {
            el = new Element(SETTING_CELLSPACING);
            settingsFile.getRootElement().addContent(el);
        }
        // create string builder
        StringBuilder dummy = new StringBuilder("");
        // add horizontal value
        dummy.append(String.valueOf(hor));
        // add separating comma
        dummy.append(",");
        // add vertical value
        dummy.append(String.valueOf(ver));
        // store values
        el.setText(dummy.toString());
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
     * Gets the setting for the autobackup-option.
     *
     * @return {@code true} if autobackup should be activated
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
     * Gets the setting whether the warning dialog in the desktop window, that tells the user if
     * added entries already have been added before, should be shown or not.
     *
     * @return {@code true} if the warning dialog in the desktop window, that tells the user if
     * added entries already have been added before, should be shown
     */
    public boolean getHideMultipleDesktopOccurencesDlg() {
        return genericBooleanGetter(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG);
    }

    /**
     * Sets the setting whether the warning dialog in the desktop window, that tells the user if
     * added entries already have been added before, should be shown or not.
     *
     * @param val {@code true} if the warning dialog in the desktop window, that tells the user if
     * added entries already have been added before, should be shown
     */
    public void setHideMultipleDesktopOccurencesDlg(boolean val) {
        genericBooleanSetter(SETTING_HIDEMULTIPLEDESKTOPOCCURENCESDLG, val);
    }

    /**
     * Gets the setting whether a table of contents should be created when exporting desktop data.
     *
     * @return {@code true} if a table of contents should be created when exporting desktop data.
     */
    public boolean getTOCForDesktopExport() {
        return genericBooleanGetter(SETTING_TOCFORDESKTOPEXPORT);
    }

    /**
     * Sets the setting whether a table of contents should be created when exporting desktop data.
     *
     * @param val {@code true} if a table of contents should be created when exporting desktop data.
     */
    public void setTOCForDesktopExport(boolean val) {
        genericBooleanSetter(SETTING_TOCFORDESKTOPEXPORT, val);
    }

    /**
     * Gets the setting whether multiple lines in the output file of the desktop data should be
     * removed or not.
     *
     * @return {@code true} if multiple lines in the output file of the desktop data should be
     * removed
     */
    public boolean getRemoveLinesForDesktopExport() {
        return genericBooleanGetter(SETTING_REMOVELINESFORDESKTOPEXPORT);
    }

    /**
     * Sets the setting whether multiple lines in the output file of the desktop data should be
     * removed or not.
     *
     * @param val {@code true} if multiple lines in the output file of the desktop data should be
     * removed
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
        Element el = settingsFile.getRootElement().getChild(SETTING_AUTONIGHTLYUPDATE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the autobackup-option
     *
     * @param val true if the autobackup should be activated
     */
    public void setAutoNightlyUpdate(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_AUTONIGHTLYUPDATE);
        if (null == el) {
            el = new Element(SETTING_AUTONIGHTLYUPDATE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the setting for the autobackup-option.
     *
     * @return {@code true} if autobackup should be activated
     */
    public boolean getShowIconText() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWICONTEXT);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the autobackup-option
     *
     * @param val true if the autobackup should be activated
     */
    public void setShowIconText(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWICONTEXT);
        if (null == el) {
            el = new Element(SETTING_SHOWICONTEXT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getAutoCompleteTags() {
        Element el = settingsFile.getRootElement().getChild(SETTING_AUTOCOMPLETETAGS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setAutoCompleteTags(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_AUTOCOMPLETETAGS);
        if (null == el) {
            el = new Element(SETTING_AUTOCOMPLETETAGS);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getUseMacBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_USEMACBACKGROUNDCOLOR);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setUseMacBackgroundColor(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_USEMACBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_USEMACBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getMarkdownActivated() {
        Element el = settingsFile.getRootElement().getChild(SETTING_MARKDOWNACTIVATED);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setMarkdownActivated(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MARKDOWNACTIVATED);
        if (null == el) {
            el = new Element(SETTING_MARKDOWNACTIVATED);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the setting for the autobackup-option.
     *
     * @return {@code true} if autobackup should be activated
     */
    public boolean getExtraBackup() {
        Element el = settingsFile.getRootElement().getChild(SETTING_EXTRABACKUP);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the autobackup-option
     *
     * @param val true if the autobackup should be activated
     */
    public void setExtraBackup(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_EXTRABACKUP);
        if (null == el) {
            el = new Element(SETTING_EXTRABACKUP);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
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
        Element el = settingsFile.getRootElement().getChild(ch);
        if (el != null) {
            return el.getText();
        }
        return null;
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
        Element el = settingsFile.getRootElement().getChild(ch);
        if (null == el) {
            el = new Element(ch);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(css);
    }

    /**
     * Gets the setting for the default locale
     *
     * @return a string with a lowercase-2-letter-country-code for the default languags
     */
    public String getLanguage() {
        return genericStringGetter(SETTING_LOCALE, Locale.getDefault().getLanguage());
    }

    /**
     * Sets the default language
     *
     * @param lang a string with a lowercase-2-letter-country-code for the default languags
     */
    public void setLanguage(String lang) {
        genericStringSetter(SETTING_LOCALE, lang.toLowerCase());
    }

    /**
     * Gets the setting whether all displayed/watched entries should be added to the history of
     * displayed entries, or whether only the activated entries should be added to the history list
     *
     * @return {@code true} if every displayed entry should be added to the history list,
     * {@code false} if only activated entries should be added to it.
     */
    public boolean getAddAllToHistory() {
        return genericBooleanGetter(SETTING_ADDALLTOHISTORY);
    }

    /**
     * Gets the setting whether all displayed/watched entries should be added to the history of
     * displayed entries, or whether only the activated entries should be added to the history list
     *
     * @param val {@code true} if every displayed entry should be added to the history list,
     * {@code false} if only activated entries should be added to it.
     */
    public void setAddAllToHistory(boolean val) {
        genericBooleanSetter(SETTING_ADDALLTOHISTORY, val);
    }

    /**
     * retrieves the desktop-number of the last used desktop.
     *
     * @param count the amount of desktops
     * @return the number of the last used desktop, or {@code -1} if no desktop exists. if a lastly
     * used desktop was deleted, a {@code 0} is returned instead. if no desktop exists at all,
     * {@code -1} is returned.
     */
    public int getLastUsedDesktop(int count) {
        // check for any desktops at all
        if (count < 1) {
            return -1;
        }
        // get attribute which stores last used desktop number
        Element el = settingsFile.getRootElement().getChild(SETTING_GETLASTUSEDDESKTOPNUMBER);
        // check for valid value
        if (el != null) {
            try {
                // retrieve value
                int retval = Integer.parseInt(el.getText());
                // check for valid bounds
                if (retval >= count) {
                    retval = 0;
                }
                // return value
                return retval;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Stores the currently used desktop, so this desktop can be shown on next program startup.
     *
     * @param val the index-number of the currently used desktop, starting with the index-number
     * {code 0} for the first desktop.
     */
    public void setLastUsedDesktop(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_GETLASTUSEDDESKTOPNUMBER);
        if (null == el) {
            el = new Element(SETTING_GETLASTUSEDDESKTOPNUMBER);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    public int getSearchFrameSplitLayout() {
        // get attribute which stores last used desktop number
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHFRAMESPLITLAYOUT);
        // check for valid value
        if (el != null) {
            try {
                // retrieve value
                return Integer.parseInt(el.getText());
            } catch (NumberFormatException e) {
                return JSplitPane.HORIZONTAL_SPLIT;
            }
        }
        return JSplitPane.HORIZONTAL_SPLIT;
    }

    public void setSearchFrameSplitLayout(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHFRAMESPLITLAYOUT);
        if (null == el) {
            el = new Element(SETTING_SEARCHFRAMESPLITLAYOUT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * Gets the setting whether new entries should be inserted at empty positions of previous
     * deleted entries or not.
     *
     * @return {@code true} if new entries should be inserted at empty positions; false if new
     * entries should be inserted at the end of the data file
     */
    public boolean getInsertNewEntryAtEmpty() {
        Element el = settingsFile.getRootElement().getChild(SETTING_FILLEMPTYPLACES);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting whether new entries should be inserted at empty positions of previous
     * deleted entries or not.
     *
     * @param val true if new entries should be inserted at empty positions; false if new entries
     * should be inserted at the end of the data file
     */
    public void setInsertNewEntryAtEmpty(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_FILLEMPTYPLACES);
        if (null == el) {
            el = new Element(SETTING_FILLEMPTYPLACES);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the settings, whether highlighting searchresults and keywords should highlight the
     * background, i.e. setting a background-color or not
     *
     * @param style
     * @return {@code true} if a background-color for highlighting should be shown, false otherwise
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
        Element el = settingsFile.getRootElement().getChild(hs_style);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Gets the settings, whether highlighting searchresults and keywords should highlight the
     * background, i.e. setting a background-color or not
     *
     * @param val true if a background-color for highlighting should be shown, false otherwise
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
        Element el = settingsFile.getRootElement().getChild(hs_style);
        if (null == el) {
            el = new Element(hs_style);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the settings, whether highlighting searchresults and keywords should highlight the
     * background, i.e. setting a background-color or not
     *
     * @param style
     * @return {@code true} if a background-color for highlighting should be shown, false otherwise
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
        Element el = settingsFile.getRootElement().getChild(hs_style);
        if (el != null) {
            return el.getText();
        }
        return "ffff66";
    }

    /**
     * Gets the settings, whether highlighting searchresults and keywords should highlight the
     * background, i.e. setting a background-color or not
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
        Element el = settingsFile.getRootElement().getChild(hs_style);
        if (null == el) {
            el = new Element(hs_style);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    /**
     *
     * @return
     */
    public String getAppendixBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_APPENDIXBACKGROUNDCOLOR);
        if (el != null) {
            return el.getText();
        }
        return "f2f2f2";
    }

    /**
     *
     * @param col
     */
    public void setReflistBackgroundColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_APPENDIXBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_APPENDIXBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    /**
     *
     * @return
     */
    public String getTableHeaderColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEHEADERCOLOR);
        if (el != null) {
            return el.getText();
        }
        return "e4e4e4";
    }

    /**
     *
     * @param col
     */
    public void setTableHeaderColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEHEADERCOLOR);
        if (null == el) {
            el = new Element(SETTING_TABLEHEADERCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    /**
     *
     * @return
     */
    public String getTableRowEvenColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEEVENROWCOLOR);
        if (el != null) {
            return el.getText();
        }
        return "eeeeee";
    }

    /**
     *
     * @param col
     */
    public void setTableRowEvenColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEEVENROWCOLOR);
        if (null == el) {
            el = new Element(SETTING_TABLEEVENROWCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    /**
     *
     * @return
     */
    public String getTableRowOddColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEODDROWCOLOR);
        if (el != null) {
            return el.getText();
        }
        return "f8f8f8";
    }

    /**
     *
     * @param col
     */
    public void setTableRowOddColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEODDROWCOLOR);
        if (null == el) {
            el = new Element(SETTING_TABLEODDROWCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    /**
     * Gets the setting for the highlighting of search results. when activated, the search terms in
     * the search results window (CSearchResults) are highlighted.
     *
     * @return {@code true} if search terms should be highlighted
     */
    public boolean getHighlightSearchResults() {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTSEARCHRESULTS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the highlighting of search results. when activated, the search terms in
     * the search results window (CSearchResults) are highlighted.
     *
     * @param val {@code true} if search terms should be highlighted
     */
    public void setHighlightSearchResults(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTSEARCHRESULTS);
        if (null == el) {
            el = new Element(SETTING_HIGHLIGHTSEARCHRESULTS);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the setting for the highlighting of keywords in the main frame's entry-content. when
     * activated, the keywords of an entry that appear in the entry-content are highlighted.
     *
     * @return {@code true} if keywords should be highlighted
     */
    public boolean getHighlightKeywords() {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTKEYWORDS);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the highlighting of keywords in the main frame's entry-content. when
     * activated, the keywords of an entry that appear in the entry-content are highlighted.
     *
     * @param val {@code true} if keywords should be highlighted
     */
    public void setHighlightKeyword(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTKEYWORDS);
        if (null == el) {
            el = new Element(SETTING_HIGHLIGHTKEYWORDS);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getHighlightSegments() {
        return highlightSegments;
    }

    public void setHighlightSegments(boolean val) {
        highlightSegments = val;
    }

    /**
     * Gets the setting for showing an entry from the search results window immediatley. when
     * activated, a selected entry in the search results window is immediately displayed in the main
     * window.
     *
     * @return {@code true} if entry should be displayed at once
     */
    public boolean getShowSearchEntry() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWSEARCHENTRY);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for showing an entry from the search results window immediatley. when
     * activated, a selected entry in the search results window is immediately displayed in the main
     * window.
     *
     * @param val {@code true} if entry should be displayed at once
     */
    public void setShowSearchEntry(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWSEARCHENTRY);
        if (null == el) {
            el = new Element(SETTING_SHOWSEARCHENTRY);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the setting whether the footnotes should be superscripted or not. A superscripted
     * footnote is displayed smaller, but changes the line-height.
     *
     * @return {@code true} if footnote should be superscripted
     */
    public boolean getSupFootnote() {
        return genericBooleanGetter(SETTING_SUPFOOTNOTE);
    }

    /**
     * Sets the setting whether the footnotes should be superscripted or not. A superscripted
     * footnote is displayed smaller, but changes the line-height.
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
     * Gets the setting whether a click on the footnotes should open the tab with the authorlist and
     * select the related author or not.
     *
     * @return {@code true} if footnote should show the related author in the tabbed pane
     */
    public boolean getJumpFootnote() {
        return genericBooleanGetter(SETTING_JUMPFOOTNOTE);
    }

    /**
     * Sets the setting whether a click on the footnotes should open the tab with the authorlist and
     * select the related author or not.
     *
     * @param val {@code true} if footnote should show the related author in the tabbed pane
     */
    public void setJumpFootnote(boolean val) {
        genericBooleanSetter(SETTING_JUMPFOOTNOTE, val);
    }

    /**
     * Gets the setting whether a search request should search in entries within a certain
     * date-range.
     *
     * @return {@code true} if search should look for entries with a certain date (timestamp)
     */
    public boolean getSearchTime() {
        return genericBooleanGetter(SETTING_SEARCHTIME);
    }

    /**
     * Sets the setting whether a search request should search in entries within a certain
     * date-range.
     *
     * @param val {@code true} if search should look for entries with a certain date (timestamp)
     */
    public void setSearchTime(boolean val) {
        genericBooleanSetter(SETTING_SEARCHTIME, val);
    }

    /**
     * Gets the setting which logicalk-combination the user chose for the last search request.
     *
     * @return 0 if search was log-and; 1 for log-or and 2 for log-not.
     */
    public int getSearchLog() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHLOG);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 0;
    }

    /**
     * Sets the setting which logicalk-combination the user chose for the last search request.
     *
     * @param val 0 if search was log-and; 1 for log-or and 2 for log-not.
     */
    public void setSearchLog(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHLOG);
        if (null == el) {
            el = new Element(SETTING_SEARCHLOG);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * Gets the setting for the thumbnail width of images. This value indicates the maximum width of
     * images which are displayed in the textfield. larger images are resized to fit the preferred
     * maximum size and a link to the original image is inserted.
     *
     * @return the preferred maximum width of an image
     */
    public int getImageResizeWidth() {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZEWIDTH);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 300;
    }

    /**
     * Sets the setting for the thumbnail width of images. This value indicates the maximum width of
     * images which are displayed in the textfield. larger images are resized to fit the preferred
     * maximum size and a link to the original image is inserted.
     *
     * @param val the preferred maximum width of an image
     */
    public void setImageResizeWidth(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZEWIDTH);
        if (null == el) {
            el = new Element(SETTING_IMGRESIZEWIDTH);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * Gets the setting for the thumbnail width of images. This value indicates the maximum width of
     * images which are displayed in the textfield. larger images are resized to fit the preferred
     * maximum size and a link to the original image is inserted.
     *
     * @return the preferred maximum width of an image
     */
    public int getImageResizeHeight() {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZEHEIGHT);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 300;
    }

    /**
     * Sets the setting for the thumbnail width of images. This value indicates the maximum width of
     * images which are displayed in the textfield. larger images are resized to fit the preferred
     * maximum size and a link to the original image is inserted.
     *
     * @param val the preferred maximum width of an image
     */
    public void setImageResizeHeight(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZEHEIGHT);
        if (null == el) {
            el = new Element(SETTING_IMGRESIZEHEIGHT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * This method returns the default font-size for tables and lists. The user cannot choose the
     * font or color, but at least a bigger font-size for better viewing is possible.
     *
     * @return the value for which the original font size should be increased.
     */
    public int getTableFontSize() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEFONTSIZE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 0;
    }

    /**
     * This method sets the default font-size for tables and lists. The user cannot choose the font
     * or color, but at least a bigger font-size for better viewing is possible.
     *
     * @param size the value for which the original font-size should be increased
     */
    public void setTableFontSize(int size) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEFONTSIZE);
        if (null == el) {
            el = new Element(SETTING_TABLEFONTSIZE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(size));
    }

    public int getDesktopOutlineFontSize() {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPOUTLINEFONTSIZE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 0;
    }

    public void setDesktopOutlineFontSize(int size) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPOUTLINEFONTSIZE);
        if (null == el) {
            el = new Element(SETTING_DESKTOPOUTLINEFONTSIZE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(size));
    }

    /**
     * This method returns the index-value for the manual timestamp that can be inserted when
     * editing a new entry (see CNewEntry-dlg). For the different String-values that are used to
     * create the DateFormat, see {@code CConstants.manualTimestamp}.
     *
     * @return the index-value for the manual timestamp
     */
    public int getManualTimestamp() {
        Element el = settingsFile.getRootElement().getChild(SETTING_MANUALTIMESTAMP);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 0;
    }

    /**
     * This method sets the index-value for the manual timestamp that can be inserted when editing a
     * new entry (see CNewEntry-dlg). For the different String-values that are used to create the
     * DateFormat, see {@code CConstants.manualTimestamp}.
     *
     * @param val the index-value for the manual timestamp
     */
    public void setManualTimestamp(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MANUALTIMESTAMP);
        if (null == el) {
            el = new Element(SETTING_MANUALTIMESTAMP);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * This method returns the default font-size for textfields in the CNewEntry-dialog. The user
     * cannot choose the font or color, but at least a bigger font-size for better viewing is
     * possible.
     *
     * @return the value for which the original font size should be increased.
     */
    public int getTextfieldFontSize() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TEXTFIELDFONTSIZE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return 0;
    }

    /**
     * This method sets the default font-size for textfields in the CNewEntry-dialog. The user
     * cannot choose the font or color, but at least a bigger font-size for better viewing is
     * possible.
     *
     * @param size the value for which the original font-size should be increased
     */
    public void setTextfieldFontSize(int size) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TEXTFIELDFONTSIZE);
        if (null == el) {
            el = new Element(SETTING_TEXTFIELDFONTSIZE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(size));
    }

    public int getLastUsedSetBibyKeyChoice() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBKEYCHOICE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return CSetBibKey.CHOOSE_BIBKEY_FROM_DB;
    }

    public void setLastUsedSetBibyKeyChoice(int choice) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBKEYCHOICE);
        if (null == el) {
            el = new Element(SETTING_LASTUSEDSETBIBKEYCHOICE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(choice));
    }

    public int getLastUsedSetBibyKeyType() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBKEYTYPE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return CSetBibKey.TYPE_BIBKEY_NEW;
    }

    public void setLastUsedSetBibyKeyType(int type) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBKEYTYPE);
        if (null == el) {
            el = new Element(SETTING_LASTUSEDSETBIBKEYTYPE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(type));
    }

    public int getLastUsedBibtexImportSource() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBIMPORTSOURCE);
        if (el != null) {
            return Integer.parseInt(el.getText());
        }
        return CImportBibTex.BIBTEX_SOURCE_DB;
    }

    public void setLastUsedBibtexImportSource(int source) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LASTUSEDSETBIBIMPORTSOURCE);
        if (null == el) {
            el = new Element(SETTING_LASTUSEDSETBIBIMPORTSOURCE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(source));
    }

    /**
     * Gets the setting for the thumbnail activation. This value indicates whether iamges should
     * always be display in original size, or whether large images should be resized
     *
     * @return {@code true} if large images should be resized.
     */
    public boolean getImageResize() {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the setting for the thumbnail activation. This value indicates whether iamges should
     * always be display in original size, or whether large images should be resized
     *
     * @param val whether thumbnail-display is enabled or not
     */
    public void setImageResize(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_IMGRESIZE);
        if (null == el) {
            el = new Element(SETTING_IMGRESIZE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getShowTableBorder() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWTABLEBORDER);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowTableBorder(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWTABLEBORDER);
        if (null == el) {
            el = new Element(SETTING_SHOWTABLEBORDER);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getShowLuhmannEntryNumber() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWLUHMANNENTRYNUMBER);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowLuhmannEntryNumber(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWLUHMANNENTRYNUMBER);
        if (null == el) {
            el = new Element(SETTING_SHOWLUHMANNENTRYNUMBER);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getShowDesktopEntryNumber() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWDESKTOPENTRYNUMBER);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowDesktopEntryNumber(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWDESKTOPENTRYNUMBER);
        if (null == el) {
            el = new Element(SETTING_SHOWDESKTOPENTRYNUMBER);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getShowEntryHeadline() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWENTRYHEADLINE);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setShowEntryHeadline(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWENTRYHEADLINE);
        if (null == el) {
            el = new Element(SETTING_SHOWENTRYHEADLINE);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the setting for the extended quick input of keywords.
     *
     * @return {@code true} if the extended keyword-quickinput should be activated
     */
    public int getQuickInputExtended() {
        Element el = settingsFile.getRootElement().getChild(SETTING_QUICKINPUTEXTENDED);
        if (el != null) {
            try {
                return Integer.parseInt(el.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Sets the setting for the extended keyword-quick-input when editing new entries..
     *
     * @param val true if the extended keyword-quickinput should be activated
     */
    public void setQuickInputExtended(int val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_QUICKINPUTEXTENDED);
        if (null == el) {
            el = new Element(SETTING_QUICKINPUTEXTENDED);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(val));
    }

    /**
     * Gets the spell-correction-variable. If true, the grids in lists and tables should be
     * displayed.
     *
     * @return
     */
    public boolean getSpellCorrect() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SPELLCORRECT);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the spell-correction-variable. If true, the grids in lists and tables should be
     * displayed.
     *
     * @param val (true if the spelling should be automatically corrected, false otherwise)
     */
    public void setSpellCorrect(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SPELLCORRECT);
        if (null == el) {
            el = new Element(SETTING_SPELLCORRECT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    /**
     * Gets the steno-variable. If true, steno is activated, false otherwise
     *
     * @return {@code true} if steno is activated, false otherwise
     */
    public boolean getStenoActivated() {
        Element el = settingsFile.getRootElement().getChild(SETTING_STENOACTIVATED);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    /**
     * Sets the steno-variable. If true, steno is activated, false otherwise
     *
     * @param val {@code true} if steno is activated, false otherwise
     */
    public void setStenoActivated(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_STENOACTIVATED);
        if (null == el) {
            el = new Element(SETTING_STENOACTIVATED);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getHighlightWholeWord() {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTWHOLEWORD);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setHighlightWholeWord(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTWHOLEWORD);
        if (null == el) {
            el = new Element(SETTING_HIGHLIGHTWHOLEWORD);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public boolean getHighlightWholeWordSearch() {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTWHOLEWORDSEARCH);
        if (el != null) {
            return el.getText().equals("1");
        }
        return false;
    }

    public void setHighlightWholeWordSearch(boolean val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HIGHLIGHTWHOLEWORDSEARCH);
        if (null == el) {
            el = new Element(SETTING_HIGHLIGHTWHOLEWORDSEARCH);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }

    public Font getTableFont() {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEFONT);
        if (el != null) {
            return new Font(el.getText(), Font.PLAIN, 12);
        }
        return null;
    }

    public void setTableFont(String f) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TABLEFONT);
        if (null == el) {
            el = new Element(SETTING_TABLEFONT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(f);
    }

    public Font getDesktopOutlineFont() {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPOUTLINEFONT);
        if (el != null) {
            return new Font(el.getText(), Font.PLAIN, 12);
        }
        return null;
    }

    public void setDesktopOutlineFont(String f) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPOUTLINEFONT);
        if (null == el) {
            el = new Element(SETTING_DESKTOPOUTLINEFONT);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(f);
    }

    /**
     * Retrieves settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getMainfont(FONTSIZE));
        return new Font(getMainfont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setMainfont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINFONT);
        if (null == el) {
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
     * Retrieves settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getAuthorFont(FONTSIZE));
        return new Font(getAuthorFont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setAuthorFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_AUTHORFONT);
        if (null == el) {
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getCodeFont(FONTSIZE));
        return new Font(getCodeFont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setCodeFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_CODEFONT);
        if (null == el) {
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
     * Retrieves settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getRemarksFont(FONTSIZE));
        return new Font(getRemarksFont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the mainfont (the font used for the main-entry-textfield).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setRemarksFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_REMARKSFONT);
        if (null == el) {
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
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
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
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setDesktopHeaderfont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPHEADERFONT);
        if (null == el) {
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
     * Retrieves settings for the desktop-window's item headers (additional display items).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getDesktopItemHeaderfont(FONTSIZE));
        return new Font(getDesktopItemHeaderfont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the desktop-window's item header font (additional display items).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setDesktopItemHeaderfont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMHEADERFONT);
        if (null == el) {
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
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getDesktopItemfont(FONTSIZE));
        return new Font(getDesktopItemfont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the desktop-window's item font (additional display items).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setDesktopItemfont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPITEMFONT);
        if (null == el) {
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
     * Retrieves settings for the style of highlighting the search terms in the search result
     * window.
     *
     * @param what indicates, which style-characteristic we want to have. use following
     * constants:<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
     * Changes settings for the style of highlighting the search terms in the search result window.
     *
     * @param value the new value for the style-characteristic
     * @param what indicates, which style-characteristic we want to have. use following
     * constants:<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        if (null == el) {
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
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
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
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setDesktopCommentfont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_DESKTOPCOMMENTFONT);
        if (null == el) {
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
     * Retrieves settings for the titlefont (the font used for the main-entry's title).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getTitleFont(FONTSIZE));
        return new Font(getTitleFont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the titlefont (the font used for the main-entry's title).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setTitleFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_TITLEFONT);
        if (null == el) {
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
     * Retrieves settings for the titlefont (the font used for the main-entry's title).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getAppendixHeaderFont(FONTSIZE));
        return new Font(getAppendixHeaderFont(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the titlefont (the font used for the main-entry's title).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setAppendixHeaderFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_APPENDIXHEADERFONT);
        if (null == el) {
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
     * Retrieves settings for the header-1-font (the font used for the main-entry's 1st
     * heading-tags).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getHeaderfont1(FONTSIZE));
        return new Font(getHeaderfont1(FONTNAME), fstyle, fsize);
    }

    /**
     * Changes settings for the header-1-font (the font used for the main-entry's 1st heading-tags).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * @param value (the new value for the font-characteristic) - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setHeaderfont1(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT1);
        if (null == el) {
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
     * Retrieves settings for the header-2-font (the font used for the main-entry's 2nd
     * heading-tags).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
        // in css, the bold-property is not a style-attribute, but a font-weight-attribute
        // that's why we have separated this here
        if (weight.equals("bold")) {
            fstyle = fstyle + Font.BOLD;
        }
        // convert the size
        int fsize = Integer.parseInt(getHeaderfont2(FONTSIZE));
        return new Font(getHeaderfont2(FONTNAME), fstyle, fsize);
    }

    /**
     * Retrieves settings for the header-2-font (the font used for the main-entry's 2nd
     * heading-tags).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
     * Retrieves settings for the header-2-font (the font used for the main-entry's 2nd
     * heading-tags).
     *
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
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
     * Changes settings for the header-2-font (the font used for the main-entry's 2nd heading-tags).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setHeaderfont2(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_HEADERFONT2);
        if (null == el) {
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
     * Changes settings for the header-2-font (the font used for the main-entry's 2nd heading-tags).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setQuoteFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_QUOTEFONT);
        if (null == el) {
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
     * Changes settings for the header-2-font (the font used for the main-entry's 2nd heading-tags).
     *
     * @param value (the new value for the font-characteristic)
     * @param what (indicates, which font-characteristic we want to have. use following
     * constants:<br>
     * - FONTNAME<br>
     * - FONTSIZE<br>
     * - FONTCOLOR<br>
     * - FONTSTYLE<br>
     * - FONTWEIGHT<br>
     */
    public void setEntryHeadeFont(String value, int what) {
        Element el = settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERFONT);
        if (null == el) {
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
        Element el = settingsFile.getRootElement().getChild(SETTING_MANLINKCOLOR);
        String retval = "0033cc";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setManlinkColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MANLINKCOLOR);
        if (null == el) {
            el = new Element(SETTING_MANLINKCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getFootnoteLinkColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_FNLINKCOLOR);
        String retval = "0033cc";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setFootnoteLinkColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_FNLINKCOLOR);
        if (null == el) {
            el = new Element(SETTING_FNLINKCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getLinkColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LINKCOLOR);
        String retval = "003399";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setLinkColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LINKCOLOR);
        if (null == el) {
            el = new Element(SETTING_LINKCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getEntryHeadingBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERBACKGROUNDCOLOR);
        String retval = "f2f2f2";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setEntryHeadingBackgroundColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_ENTRYHEADERBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_ENTRYHEADERBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getQuoteBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_QUOTEBACKGROUNDCOLOR);
        String retval = "f2f2f2";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setQuoteBackgroundColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_QUOTEBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_QUOTEBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getMainBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINBACKGROUNDCOLOR);
        String retval = "ffffff";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setMainBackgroundColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_MAINBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_MAINBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public String getContentBackgroundColor() {
        Element el = settingsFile.getRootElement().getChild(SETTING_CONTENTBACKGROUNDCOLOR);
        String retval = "ffffff";
        if (el != null) {
            retval = el.getText();
        }
        return retval;
    }

    public void setContentBackgroundColor(String col) {
        Element el = settingsFile.getRootElement().getChild(SETTING_CONTENTBACKGROUNDCOLOR);
        if (null == el) {
            el = new Element(SETTING_CONTENTBACKGROUNDCOLOR);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(col);
    }

    public int getShowAtStartup() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWATSTARTUP);
        int retval = 0;
        if (el != null) {
            try {
                retval = Integer.parseInt(el.getText());
            } catch (NumberFormatException e) {
                retval = 0;
            }
        }
        return retval;
    }

    public void setShowAtStartup(int value) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SHOWATSTARTUP);
        if (null == el) {
            el = new Element(SETTING_SHOWATSTARTUP);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(value));
    }

    /**
     * This method keeps the selection of the combobox in the search dialog (CSearchDlg), which
     * stores the information whether the user wanted to search for entries with a certain
     * create-date, changed-date or both.
     *
     * @return the index of the selected item.
     */
    public int getSearchComboTime() {
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHCOMBOTIME);
        int retval = 0;
        if (el != null) {
            try {
                retval = Integer.parseInt(el.getText());
            } catch (NumberFormatException e) {
                retval = 0;
            }
        }
        return retval;
    }

    /**
     * This method keeps the selection of the combobox in the search dialog (CSearchDlg), which
     * stores the information whether the user wanted to search for entries with a certain
     * create-date, changed-date or both.
     *
     * @param value the index of the selected item.
     */
    public void setSearchComboTime(int value) {
        Element el = settingsFile.getRootElement().getChild(SETTING_SEARCHCOMBOTIME);
        if (null == el) {
            el = new Element(SETTING_SEARCHCOMBOTIME);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(String.valueOf(value));
    }

    /**
     * When the user wants to search for entries with a certain creation or modified-date, this
     * setting stores the values from the last entered date-input from the user
     *
     * @return a string value, comma separated, which holds to dates: the beginning and the end-date
     * of the period the user wanted to search for entries.
     */
    public String getSearchDateTime() {
        return genericStringGetter(SETTING_SEARCHDATETIME, "");
    }

    /**
     * When the user wants to search for entries with a certain creation or modified-date, this
     * setting stores the values from the last entered date-input from the user
     *
     * @param value a string value, comma separated, which holds to dates: the beginning and the
     * end-date of the period the user wanted to search for entries. these strings are taken from
     * the formatted textfields in the CSearchDlg.
     */
    public void setSearchDateTime(String value) {
        genericStringSetter(SETTING_SEARCHDATETIME, value);
    }

    /**
     *
     * @return
     */
    public boolean getShowIcons() {
        return genericBooleanGetter(SETTING_SHOWICONS);
    }

    /**
     *
     * @param value
     */
    public void setShowIcons(boolean value) {
        genericBooleanSetter(SETTING_SHOWICONS, value);
    }

    /**
     *
     * @return
     */
    public boolean getShowAllIcons() {
        return genericBooleanGetter(SETTING_SHOWALLICONS);
    }

    /**
     *
     * @param value
     */
    public void setShowAllIcons(boolean value) {
        genericBooleanSetter(SETTING_SHOWALLICONS, value);
    }

    /**
     * @return returns the last used bibtex-format, i.e. the format (encoding) of the currently
     * attached BibTeX file. following constants are used:<br>
     * 0: UTF-8 (Bibliographix)<br>
     * 1: UTF-8 (Citavi)<br>
     * 2: ISO8859_1 (Emacs with AucTex/RefTex)<br>
     * 3: UTF-8 (Endnote)<br>
     * 4: ISO8859_1 (JabRef)<br>
     * 5: UTF-8 (Refworks)
     */
    public int getLastUsedBibtexFormat() {
        return genericIntGetter(SETTING_LASTUSEDBIBTEXFORMAT, 0);
    }

    /**
     * Sets the character-encoding of the currently attached BibTeX file.
     *
     * @param value set the last used BibTeX format, i.e. the format (encoding) of the currently
     * attached BibTeX file. The following constants are used:<br>
     * 0: UTF-8 (Bibliographix)<br>
     * 1: UTF-8 (Citavi)<br>
     * 2: ISO8859_1 (Emacs with AucTex/RefTex)<br>
     * 3: UTF-8 (Endnote)<br>
     * 4: ISO8859_1 (JabRef)<br>
     * 5: UTF-8 (Refworks)
     */
    public void setLastUsedBibtexFormat(int value) {
        genericIntSetter(SETTING_LASTUSEDBIBTEXFORMAT, value);
    }

    /**
     * @return returns the display-option of the desktop window, i.e. whether comments should be
     * displayed in the desktop window or not, or if only comments should be displayed.
     *
     * following constants are used:<br>
     * <ul>
     * <li>Constants.DESKTOP_WITH_COMMENTS</li>
     * <li>Constants.DESKTOP_WITHOUT_COMMENTS</li>
     * <li>Constants.DESKTOP_ONLY_COMMENTS</li>
     * </ul>
     */
    public int getDesktopCommentDisplayOptions() {
        return genericIntGetter(SETTING_DESKTOPSHOWCOMMENTS, 0);
    }

    /**
     * Sets the display-option of the desktop window, i.e. whether comments should be displayed in
     * the desktop window or not, or if only comments should be displayed.
     *
     * @param value the display-option. following constants are used:<br>
     * <ul>
     * <li>Constants.DESKTOP_WITH_COMMENTS</li>
     * <li>Constants.DESKTOP_WITHOUT_COMMENTS</li>
     * <li>Constants.DESKTOP_ONLY_COMMENTS</li>
     * </ul>
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
        Element el = settingsFile.getRootElement().getChild(ch);
        boolean retval = false;
        if (el != null) {
            retval = el.getText().equals("1");
        }
        return retval;
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
        Element el = settingsFile.getRootElement().getChild(ch);
        if (null == el) {
            el = new Element(ch);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText(value ? "1" : "0");
    }

    /**
     * @return returns the currently used icon theme. following constants are used:<br>
     * 0: standard<br>
     * 1: Tango<br>
     */
    public int getIconTheme() {
        return genericIntGetter(SETTING_ICONTHEME, 0);
    }

    /**
     *
     * @param value sets the currently used icon theme. following constants are used:<br>
     * 0: standard<br>
     * 1: Tango<br>
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
        if (isSeaGlass()) {
            defpath = defpath + "osx/";
        } else {
            defpath = defpath + Constants.iconThemes[theme];
        }
        // return path
        return defpath;
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportCreateFormTags() {
        return genericBooleanGetter(SETTING_LATEXEXPORTFORMTAG);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportCreateFormTags(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTFORMTAG, val);
    }

    /**
     * {@code true} when author-references should be references in a footnote, when exporting to
     * LaTex. If {@code false}, references are directly in the text.
     *
     * @return
     */
    public boolean getLatexExportFootnoteRef() {
        return genericBooleanGetter(SETTING_LATEXEXPORTFOOTNOTE);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportFootnoteRef(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTFOOTNOTE, val);
    }

    /**
     *
     * @return
     */
    public int getLastUsedLatexBibStyle() {
        return genericIntGetter(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, 0);
    }

    /**
     *
     * @param value
     */
    public void setLastUsedLatexBibStyle(int value) {
        genericIntSetter(SETTING_LATEXEXPORTLASTUSEDBIBSTYLE, value);
    }

    /**
     *
     * @return
     */
    public int getLastUsedLatexDocClass() {
        return genericIntGetter(SETTING_LATEXEXPORTDOCUMENTCLASS, 0);
    }

    /**
     *
     * @param value
     */
    public void setLastUsedLatexDocClass(int value) {
        genericIntSetter(SETTING_LATEXEXPORTDOCUMENTCLASS, value);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportShowAuthor() {
        return genericBooleanGetter(SETTING_LATEXEXPORTSHOWAUTHOR);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportShowAuthor(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTSHOWAUTHOR, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportShowMail() {
        return genericBooleanGetter(SETTING_LATEXEXPORTSHOWMAIL);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportShowMail(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTSHOWMAIL, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportConvertQuotes() {
        return genericBooleanGetter(SETTING_LATEXEXPORTCONVERTQUOTES);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportConvertQuotes(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTCONVERTQUOTES, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportCenterForm() {
        return genericBooleanGetter(SETTING_LATEXEXPORTCENTERFORM);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportCenterForm(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTCENTERFORM, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportRemoveNonStandardTags() {
        return genericBooleanGetter(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportRemoveNonStandardTags(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTREMOVENONSTANDARDTAGS, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportNoPreamble() {
        return genericBooleanGetter(SETTING_LATEXEXPORTNOPREAMBLE);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportNoPreamble(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTNOPREAMBLE, val);
    }

    /**
     *
     * @return
     */
    public boolean getLatexExportConvertUmlaut() {
        return genericBooleanGetter(SETTING_LATEXEXPORTCONVERTUMLAUT);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportConvertUmlaut(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTCONVERTUMLAUT, val);
    }
    
    /**
     *
     * @return
     */
    public boolean getLatexExportStatisticTableStyle() {
        return genericBooleanGetter(SETTING_LATEXEXPORTTABLESTATSTYLE);
    }

    /**
     *
     * @param val
     */
    public void setLatexExportStatisticTableStyle(boolean val) {
        genericBooleanSetter(SETTING_LATEXEXPORTTABLESTATSTYLE, val);
    }

    /**
     *
     * @return
     */
    public String getLatexExportAuthorValue() {
        return genericStringGetter(SETTING_LATEXEXPORTAUTHORVALUE, "");
    }

    /**
     *
     * @param val
     */
    public void setLatexExportAuthorValue(String val) {
        genericStringSetter(SETTING_LATEXEXPORTAUTHORVALUE, val);
    }

    /**
     *
     * @return
     */
    public String getLatexExportMailValue() {
        Element el = settingsFile.getRootElement().getChild(SETTING_LATEXEXPORTMAILVALUE);
        if (el != null) {
            return el.getText();
        }
        return "";
    }

    /**
     *
     * @param val
     */
    public void setLatexExportMailValue(String val) {
        Element el = settingsFile.getRootElement().getChild(SETTING_LATEXEXPORTMAILVALUE);
        if (null == el) {
            el = new Element(SETTING_LATEXEXPORTMAILVALUE);
            settingsFile.getRootElement().addContent(el);
        }
        if (val != null && !val.isEmpty()) {
            el.setText(val);
        } else {
            el.setText("");
        }
    }
    
    private File genericDirGetter(String key) {
        // we do this step by step rather that appending a ".getText()" to the line below, because
        // by doing so we can check whether the child element exists or not, and avoiding null pointer
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
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        // set new file path which should be now relative to the zkn-path
        el.setText((null == fp) ? "" : FileOperationsUtil.getFilePath(fp));
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
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        el.setText((val) ? "1" : "0");
    }
    
    /**
     * Returns the setting (saved value) for integer values. Return
     * the argument {@code defaultValue} if element does not exist
     * in the settings file.
     * 
     * @param key the key of the specific settings
     * @param defaultValue a default value that will be returned in case the 
     * setting {@code key} does not exist.
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
        if (null == el) {
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
        if (null == el) {
            el = new Element(key);
            settingsFile.getRootElement().addContent(el);
        }
        if (val != null && !val.isEmpty()) {
            el.setText(val);
        } else {
            el.setText("");
        }
    }
}
