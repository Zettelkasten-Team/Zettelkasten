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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.jdom2.output.XMLOutputter;
import de.danielluedecke.zettelkasten.util.Constants;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author danielludecke
 */
public class SaveFileTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     * CBookmark object, which contains the XML data of the entries' bookmarks
     */
    private final Bookmarks bookmarkObj;
    /**
     * DesktopData object, which contains the XML data of the desktop
     */
    private final DesktopData desktopObj;
    private final Synonyms synonymsObj;
    private final BibTeX bibtexObj;
    /**
     * Settings object, which contains the setting, for instance the file paths
     * etc...
     */
    private final Settings settingsObj;
    /**
     * SearchRequests object, which contains the XML data of the searchrequests
     * and -result that are related with this data file
     */
    private final SearchRequests searchrequestsObj;

    private boolean saveOk = true;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(SaveFileTask.class);

    SaveFileTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, Bookmarks bm, SearchRequests sr, DesktopData dk, Synonyms sy, Settings s, BibTeX bib) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        bibtexObj = bib;
        synonymsObj = sy;
        bookmarkObj = bm;
        searchrequestsObj = sr;
        desktopObj = dk;
        settingsObj = s;
        parentDialog = parent;
        msgLabel = label;
        // show status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect
        File fp = settingsObj.getFilePath();
        // if no file exists, exit task
        if (null == fp) {
            // log error
            Constants.zknlogger.log(Level.WARNING, "Filepath is null!");
            saveOk = false;
            return null;
        }
        // check whether file is write protected
        if (!fp.canWrite()) {
            // ask whether write protection should be removed
            int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("removeWriteProtectMsg"), resourceMap.getString("removeWriteProtectTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // user cancelled dialog
            if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION == option || JOptionPane.NO_OPTION == option) {
                saveOk = false;
                return null;
            }
            // check return result
            if (JOptionPane.YES_OPTION == option) {
                try {
                    // try to remove write protection
                    fp.setWritable(true);
                } catch (SecurityException ex) {
                    // log error-message
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                    Constants.zknlogger.log(Level.SEVERE, "File is write-protected. Write protection could not be removed!");
                    saveOk = false;
                    return null;
                }
            }
        }
        // log file path
        Constants.zknlogger.log(Level.INFO, "Saving file to {0}", fp.toString());
        ByteArrayOutputStream bout = null;
        ZipOutputStream zip = null;
        // open the outputstream
        try {
            zip = new ZipOutputStream(new FileOutputStream(fp));
            // I first wanted to use a pretty output format, so advanced users who
            // extract the data file can better watch the xml-files. but somehow, this
            // lead to an error within the method "retrieveElement" in the class "Daten.java",
            // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
            // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            XMLOutputter out = new XMLOutputter();
            // show status text
            msgLabel.setText(resourceMap.getString("msg1"));
            // save metainformation
            zip.putNextEntry(new ZipEntry(Constants.metainfFileName));
            out.output(dataObj.getMetaInformationData(), zip);
            // save main data.
            zip.putNextEntry(new ZipEntry(Constants.zknFileName));
            out.output(dataObj.getZknData(), zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // save authors
            zip.putNextEntry(new ZipEntry(Constants.authorFileName));
            out.output(dataObj.getAuthorData(), zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg3"));
            // save keywords
            zip.putNextEntry(new ZipEntry(Constants.keywordFileName));
            out.output(dataObj.getKeywordData(), zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg4"));
            // save keywords
            zip.putNextEntry(new ZipEntry(Constants.bookmarksFileName));
            out.output(bookmarkObj.getBookmarkData(), zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg5"));
            // save keywords
            zip.putNextEntry(new ZipEntry(Constants.searchrequestsFileName));
            out.output(searchrequestsObj.getSearchData(), zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg6"));
            // save synonyms
            zip.putNextEntry(new ZipEntry(Constants.synonymsFileName));
            out.output(synonymsObj.getDocument(), zip);
            // save bibtex file
            zip.putNextEntry(new ZipEntry(Constants.bibTexFileName));
            bout = bibtexObj.saveFile();
            bout.writeTo(zip);
            // show status text
            msgLabel.setText(resourceMap.getString("msg6"));
            // save desktops
            zip.putNextEntry(new ZipEntry(Constants.desktopFileName));
            out.output(desktopObj.getDesktopData(), zip);
            zip.putNextEntry(new ZipEntry(Constants.desktopModifiedEntriesFileName));
            out.output(desktopObj.getDesktopModifiedEntriesData(), zip);
            zip.putNextEntry(new ZipEntry(Constants.desktopNotesFileName));
            out.output(desktopObj.getDesktopNotesData(), zip);
        } catch (IOException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // check whether file is write protected
            if (!fp.canWrite()) {
                // log error-message
                Constants.zknlogger.log(Level.SEVERE, "Save failed. The file is write-protected.");
                // show error message
                JOptionPane.showMessageDialog(null, resourceMap.getString("errorSavingWriteProtectedMsg"), resourceMap.getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
            } else {
                // show error message
                JOptionPane.showMessageDialog(null, resourceMap.getString("errorSavingMsg"), resourceMap.getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
            }
            // change error-indicator
            saveOk = false;
        } catch (SecurityException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // show error message
            JOptionPane.showMessageDialog(null, resourceMap.getString("errorNoAccessMsg"), resourceMap.getString("errorNoAccessTitle"), JOptionPane.PLAIN_MESSAGE);
            // change error-indicator
            saveOk = false;
        } finally {
            try {
                if (bout != null) {
                    bout.close();
                }
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        // after saving, change modified state to false.
        dataObj.setModified(!saveOk);
        dataObj.setMetaModified(!saveOk);
        searchrequestsObj.setModified(!saveOk);
        desktopObj.setModified(!saveOk);
        bookmarkObj.setModified(!saveOk);
        synonymsObj.setModified(!saveOk);
        bibtexObj.setModified(!saveOk);
    }

    @Override
    protected void finished() {
        super.finished();
        // save error-flag
        dataObj.setSaveOk(saveOk);
        // and log info message
        if (saveOk) {
            Constants.zknlogger.log(Level.INFO, "The data file has been successfully saved to {0}", settingsObj.getFilePath().toString());
        }
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
