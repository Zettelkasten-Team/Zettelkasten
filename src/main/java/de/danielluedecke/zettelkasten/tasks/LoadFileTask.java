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
package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.database.BibTeX;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;

/**
 *
 * @author danielludecke
 */
public class LoadFileTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     * CBookmark object, which contains the XML data of the entries' bookmarks
     */
    private final Bookmarks bookmarkObj;
    /**
     * CBookmark object, which contains the XML data of the entries' bookmarks
     */
    private final Synonyms synonymsObj;
    /**
     * DesktopData object, which contains the XML data of the desktop
     */
    private final DesktopData desktopObj;
    /**
     * Settings object, which contains the setting, for instance the file paths
     * etc...
     */
    private final Settings settingsObj;
    private final BibTeX bibtexObj;
    /**
     * SearchRequests object, which contains the XML data of the searchrequests
     * and -result that are related with this data file
     */
    private final SearchRequests searchrequestsObj;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(LoadFileTask.class);

    /**
     *
     * @param app
     * @param parent
     * @param label
     * @param d
     * @param bm
     * @param sr
     * @param dk
     * @param s
     */
    LoadFileTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, Bookmarks bm, SearchRequests sr, DesktopData dk, Synonyms sy, Settings s, BibTeX bib) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        bibtexObj = bib;
        bookmarkObj = bm;
        synonymsObj = sy;
        searchrequestsObj = sr;
        desktopObj = dk;
        settingsObj = s;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect

        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
        // get the file path from the data file which has to be opened
        File fp = settingsObj.getFilePath();
        // if no file exists, exit task
        if (null == fp || !fp.exists()) {
            // log error
            Constants.zknlogger.log(Level.WARNING, "Filepath is null or does not exist!");
            return null;
        }
        ZipInputStream zip;
        try {
            // reset the zettelkasten-data-files
            dataObj.initZettelkasten();
            desktopObj.clear();
            bookmarkObj.clear();
            searchrequestsObj.clear();
            // log file path
            Constants.zknlogger.log(Level.INFO, "Opening file {0}", fp.toString());
            // it looks like the SAXBuilder is closing an input stream. So we have to
            // re-open the ZIP-file each time we want to retrieve an XML-file from it
            // this is necessary, because we want tot retrieve the zipped xml-files
            // *without* temporarily saving them to harddisk
            for (int cnt = 0; cnt < dataObj.getFilesToLoadCount(); cnt++) {
                // show status text
                switch (cnt) {
                    case 0:
                    case 1:
                        msgLabel.setText(resourceMap.getString("msg1"));
                        break;
                    case 2:
                        msgLabel.setText(resourceMap.getString("msg2"));
                        break;
                    case 3:
                        msgLabel.setText(resourceMap.getString("msg3"));
                        break;
                    case 4:
                        msgLabel.setText(resourceMap.getString("msg4"));
                        break;
                    case 5:
                        msgLabel.setText(resourceMap.getString("msg5"));
                        break;
                    case 6:
                    case 7:
                        msgLabel.setText(resourceMap.getString("msg6"));
                        break;
                    default:
                        msgLabel.setText(resourceMap.getString("msg1"));
                        break;
                }
                // open the zip-file
                zip = new ZipInputStream(new FileInputStream(fp));
                ZipEntry entry;
                try {
                    // now iterate the zip-file, searching for the requested file in it
                    while ((entry = zip.getNextEntry()) != null) {
                        // get filename of zip-entry
                        String entryname = entry.getName();
                        // if the found file matches the requested one, start the SAXBuilder
                        if (entryname.equals(dataObj.getFileToLoad(cnt))) {
                            if (entryname.equals(Constants.bibTexFileName)) {
                                bibtexObj.openFile(zip, "UTF-8");
                                Constants.zknlogger.log(Level.INFO, "{0} data successfully opened.", entryname);
                                break;
                            } else {
                                try {
                                    SAXBuilder builder = new SAXBuilder();
                                    // Document doc = new Document();
                                    Document doc = builder.build(zip);
                                    // compare, which file we have retrieved, so we store the data
                                    // correctly on our data-object
                                    if (entryname.equals(Constants.metainfFileName)) {
                                        dataObj.setMetaInformationData(doc);
                                    }
                                    if (entryname.equals(Constants.zknFileName)) {
                                        dataObj.setZknData(doc);
                                    }
                                    if (entryname.equals(Constants.authorFileName)) {
                                        dataObj.setAuthorData(doc);
                                    }
                                    if (entryname.equals(Constants.keywordFileName)) {
                                        dataObj.setKeywordData(doc);
                                    }
                                    if (entryname.equals(Constants.bookmarksFileName)) {
                                        bookmarkObj.setBookmarkData(doc);
                                    }
                                    if (entryname.equals(Constants.searchrequestsFileName)) {
                                        searchrequestsObj.setSearchData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopFileName)) {
                                        desktopObj.setDesktopData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopModifiedEntriesFileName)) {
                                        desktopObj.setDesktopModifiedEntriesData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopNotesFileName)) {
                                        desktopObj.setDesktopNotesData(doc);
                                    }
                                    if (entryname.equals(Constants.synonymsFileName)) {
                                        synonymsObj.setDocument(doc);
                                    }
                                    // tell about success
                                    Constants.zknlogger.log(Level.INFO, "{0} data successfully opened.", entryname);
                                    break;
                                } catch (JDOMException e) {
                                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                } finally {
                    try {
                        zip.close();
                    } catch (IOException e) {
                        Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
            }
            // tell about success
            Constants.zknlogger.log(Level.INFO, "Complete data file successfully opened.");
        } catch (IOException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        }

        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().

        // after opening a new file, set modified state to false
        dataObj.setModified(false);
        dataObj.setMetaModified(false);
        searchrequestsObj.setModified(false);
        desktopObj.setModified(false);
        bookmarkObj.setModified(false);
        synonymsObj.setModified(false);
        bibtexObj.setModified(false);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
        // tell about success
        Constants.zknlogger.log(Level.INFO, "Opening data file successfully finished.");
    }
}
