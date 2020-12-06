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
package de.danielluedecke.zettelkasten.tasks.export;

import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Luedeke
 */
public class ExportToZknTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the CDaten object, which contains the XML data of the
     * Zettelkasten will be passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     *
     */
    private final Bookmarks bookmarksObj;
    private final BibTeX bibtexObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     * file path to export file
     */
    private final File filepath;
    /**
     *
     */
    private ArrayList<Object> exportentries;
    /**
     *
     */
    private boolean exportOk;
    private final boolean exportbibtex;
    /**
     *
     */
    private boolean showOkMessage = true;
    /**
     *
     */
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ExportTask.class);

    public ExportToZknTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
                           TasksData td, Daten d, Bookmarks bm, BibTeX bib, boolean exportBib, File fp, ArrayList<Object> ee) {
        super(app);
        dataObj = d;
        bookmarksObj = bm;
        bibtexObj = bib;
        filepath = fp;
        exportentries = ee;
        exportOk = true;
        taskinfo = td;
        parentDialog = parent;
        msgLabel = label;
        exportbibtex = exportBib;

        // the variable "exportentries" stores all entry-numbers of those entries that should be exported.
        // if this array is null, we assume that *all* entries have to be exported. thus, insert
        // all entry-numbers here
        if (null == exportentries) {
            exportentries = new ArrayList<>();
            // copy all entry-numbers to array. remember that the entrynumbers range from 1 to site of file.
            for (int cnt = 0; cnt < dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // only add entries that are not empty
                if (!dataObj.isEmpty(cnt + 1)) {
                    exportentries.add(cnt + 1);
                }
            }
        }

        // show status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect

        // if no file exists, exit task
        if (null == filepath) {
            showOkMessage = false;
            return null;
        }
        // check whether file already exists
        if (filepath.exists()) {
            // file exists, ask user to overwrite it...
            int optionDocExists = JOptionPane.showConfirmDialog(null,
                    resourceMap.getString("askForOverwriteFileMsg", "", filepath.getName()),
                    resourceMap.getString("askForOverwriteFileTitle"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            // if the user does *not* choose to overwrite, quit...
            if (optionDocExists != JOptionPane.YES_OPTION) {
                // don't show "export was OK" message in main frame
                showOkMessage = false;
                return null;
            }
        }
        // yet everything is ok...
        exportOk = true;
        // create list with all export entries...
        ArrayList<Integer> entrylist = new ArrayList<>();
        // go through all elements of the data file
        for (Object exportentrie : exportentries) {
            try {
                // retrieve zettelnumber
                int zettelnummer = Integer.parseInt(exportentrie.toString());
                // and add it to list.
                entrylist.add(zettelnummer);
            } catch (NumberFormatException e) {
            }
        }
        // sort array
        Collections.sort(entrylist);
        // create document for exporting the entries
        dataObj.createExportEntries(entrylist);
        // create export-XML-file for bookmarks
        Document bookmarks = new Document(new Element("bookmarks"));
        // iterate all bookmarks
        for (int cnt = 0; cnt < bookmarksObj.getCount(); cnt++) {
            // retrieve each bookmarked entry number
            int bookmarkpos = bookmarksObj.getBookmarkEntry(cnt);
            // check whether bookmarked entry is in export list
            if (entrylist.contains(bookmarkpos)) {
                // create new bookmark element
                Element bookmark = new Element("bookmark");
                // add zettel-id as attribute
                bookmark.setAttribute("id", dataObj.getZettelID(bookmarkpos));
                // add bookmark-category as attribute
                bookmark.setAttribute("cat", bookmarksObj.getBookmarkCategoryAsString(cnt));
                // add comment as text
                bookmark.setText(bookmarksObj.getComment(cnt));
                // add element to XML file
                bookmarks.getRootElement().addContent(bookmark);
            }
        }

        // TODO suchergebnisse nummern in id's umwandeln und mitexportieren
        // TODO schreibtisch-Daten: zettel-nummern in id's umwandeln und mitexportieren
        // export data to zkn3-file
        ZipOutputStream zip = null;
        // open the outputstream
        try {
            zip = new ZipOutputStream(new FileOutputStream(filepath));
            // I first wanted to use a pretty output format, so advanced users who
            // extract the data file can better watch the xml-files. but somehow, this
            // lead to an error within the method "retrieveElement" in the class "CDaten.java",
            // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
            // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            XMLOutputter out = new XMLOutputter();
            // show status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // save metainformation
            zip.putNextEntry(new ZipEntry(Constants.metainfFileName));
            out.output(dataObj.getMetaInformationData(), zip);
            // save main data.
            zip.putNextEntry(new ZipEntry(Constants.zknFileName));
            out.output(dataObj.retrieveExportDocument(), zip);
            // save authors
            zip.putNextEntry(new ZipEntry(Constants.authorFileName));
            out.output(dataObj.getAuthorData(), zip);
            // save keywords
            zip.putNextEntry(new ZipEntry(Constants.keywordFileName));
            out.output(dataObj.getKeywordData(), zip);
            // save bookmarks
            zip.putNextEntry(new ZipEntry(Constants.bookmarksFileName));
            out.output(bookmarks, zip);
            // close zip-stream
        } catch (IOException | SecurityException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // change error-indicator
            exportOk = false;
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                // log error-message
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                // change error-indicator
                exportOk = false;
            }
        }
        // if the user requested a bibtex-export, do this now
        if (exportbibtex) {
            // show status text
            msgLabel.setText(resourceMap.getString("msgBibtextExport"));
            // write bibtex file
            ExportTools.writeBibTexFile(dataObj, bibtexObj, exportentries, filepath, resourceMap);
        }

        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
    }

    @Override
    protected void finished() {
        super.finished();
        taskinfo.setExportOk(exportOk);
        taskinfo.setShowExportOkMessage(showOkMessage);
        // Close Window
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }
}
