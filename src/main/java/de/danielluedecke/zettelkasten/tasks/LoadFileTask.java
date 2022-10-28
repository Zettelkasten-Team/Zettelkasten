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

import de.danielluedecke.zettelkasten.database.*;
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
import de.danielluedecke.zettelkasten.util.Constants;

/**
 *
 * @author danielludecke
 */
public class LoadFileTask extends org.jdesktop.application.Task<Object, Void> {

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    private final Daten daten;
    private final Bookmarks bookmarks;
    private final SearchRequests searchRequests;
    private final DesktopData desktopData;
    private final Synonyms synonyms;
    private final Settings settings;
    private final BibTeX bibTeX;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(LoadFileTask.class);

    LoadFileTask(org.jdesktop.application.Application app,
                 javax.swing.JDialog parent,
                 javax.swing.JLabel label,
                 Daten d,
                 Bookmarks bm,
                 SearchRequests sr,
                 DesktopData dk,
                 Synonyms sy,
                 Settings s,
                 BibTeX bib) {

        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);

        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1")); // FIXME msg1 ?

        daten = d;
        bookmarks = bm;
        searchRequests = sr;
        desktopData = dk;
        synonyms = sy;
        settings = s;
        bibTeX = bib;
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
        File fp = settings.getFilePath();
        if (fp == null || !fp.exists()) {
            Constants.zknlogger.log(Level.WARNING, "Filepath is null or does not exist!");
            return null;
        }
        ZipInputStream zip;
        try {
            // Reset the Zettelkasten data files
            daten.reset();
            desktopData.clear();
            bookmarks.clear();
            searchRequests.clear();

            // log file path
            Constants.zknlogger.log(Level.INFO, "Opening file {0}", fp);

            // It looks like the SAXBuilder is closing an input stream. So we have to
            // reopen the ZIP file every time we want to retrieve an XML file from it.
            // This is necessary, because we want to retrieve the zipped XML files
            // *without* temporarily saving them to disk.
            for (int cnt = 0; cnt < daten.getFilesToLoadCount(); cnt++) {
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
                        if (entryname.equals(daten.getFileToLoad(cnt))) {
                            if (entryname.equals(Constants.bibTexFileName)) {
                                bibTeX.openFile(zip, "UTF-8");
                                Constants.zknlogger.log(Level.INFO, "{0} data successfully opened.", entryname);
                                break;
                            } else {
                                try {
                                    SAXBuilder builder = new SAXBuilder(); // FIXME Disable access to external entities in XML parsing.

                                    Document doc = builder.build(zip);
                                    // compare, which file we have retrieved, so we store the data
                                    // correctly on our data-object
                                    if (entryname.equals(Constants.metainfFileName)) {
                                        daten.setMetaInformationData(doc);
                                    }
                                    if (entryname.equals(Constants.zknFileName)) {
                                        daten.setZknData(doc);
                                    }
                                    if (entryname.equals(Constants.authorFileName)) {
                                        daten.setAuthorData(doc);
                                    }
                                    if (entryname.equals(Constants.keywordFileName)) {
                                        daten.setKeywordData(doc);
                                    }
                                    if (entryname.equals(Constants.bookmarksFileName)) {
                                        bookmarks.setBookmarkData(doc);
                                    }
                                    if (entryname.equals(Constants.searchrequestsFileName)) {
                                        searchRequests.setSearchData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopFileName)) {
                                        desktopData.setDesktopData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopModifiedEntriesFileName)) {
                                        desktopData.setDesktopModifiedEntriesData(doc);
                                    }
                                    if (entryname.equals(Constants.desktopNotesFileName)) {
                                        desktopData.setDesktopNotesData(doc);
                                    }
                                    if (entryname.equals(Constants.synonymsFileName)) {
                                        synonyms.setDocument(doc);
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
            Constants.zknlogger.log(Level.INFO, "Complete data file successfully opened.");
        } catch (IOException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().

        // after opening a new file, set modified state to false
        daten.setModified(false);
        daten.setMetaModified(false);
        searchRequests.setModified(false);
        desktopData.setModified(false);
        bookmarks.setModified(false);
        synonyms.setModified(false);
        bibTeX.setModified(false);
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
