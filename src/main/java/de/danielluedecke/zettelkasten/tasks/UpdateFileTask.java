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
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;

/**
 *
 * @author danielludecke
 */
public class UpdateFileTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     * DesktopData object, which contains the XML data of the desktop
     */
    private final DesktopData desktopObj;
    private final BibTeX bibtexObj;
    private final boolean resetFrequencies;
    private int updateType;
    
    private static final int UPDATE_TYPE_DATABASE = 1 << 1;
    private static final int UPDATE_TYPE_BIBTEX = 1 << 2;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
        getContext().getResourceMap(UpdateFileTask.class);

    UpdateFileTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, DesktopData dk, BibTeX bib, boolean rf) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        bibtexObj = bib;
        desktopObj = dk;
        resetFrequencies = rf;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg3"));
    }
    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect
        updateType = 0;
        // get version info
        String verinfo = dataObj.getVersionInfo();
        // update to V3.1
        // here we have to update from version 3.0 to 3.1
        if ((verinfo!=null && verinfo.equals("3.0")) || resetFrequencies) {
            // change status text
            msgLabel.setText(resourceMap.getString("msg1"));
            // get the end of the loop, we need this for the progress bar...
            int len = dataObj.getCount(Daten.AUCOUNT);
            // first, we have to update all authors. what we have to do is: updating
            // the author frequencies.
            for (int cnt=1; cnt<=len; cnt++) {
                // get author-string
                String au = dataObj.getAuthor(cnt);
                // retrieve author-frequency
                int freq = dataObj.getAuthorFrequencies(cnt);
                // update the new author
                dataObj.setAuthor(cnt, au, freq);
                // and the progressbar
                setProgress(cnt-1,0,len);
            }
            // change status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // get the end of the loop, we need this for the progress bar...
            len = dataObj.getCount(Daten.KWCOUNT);
            // first, we have to update all authors. what we have to do is: updating
            // the author frequencies.
            for (int cnt=1; cnt<=len; cnt++) {
                // get keyword-string
                String kw = dataObj.getKeyword(cnt);
                // retrieve keyword-frequency
                int freq = dataObj.getKeywordFrequencies(cnt);
                // update the new keyword
                dataObj.setKeyword(cnt, kw, freq);
                // and the progressbar
                setProgress(cnt-1,0,len);
            }
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        // update to V3.2
        // here we change the structure of the desktop-datafile
        // here we have to update from version 3.0 or 3.1 to 3.2
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg3"));
            // update timestamps from desktop-entries
            desktopObj.db_updateTimestamps();
            // initialise the desktop-notes-xml-file
            desktopObj.initDesktopNotesUpdate();
            // fix wrong edit-timestamp-tags in data file
            dataObj.fixWrongEditTags();
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        // Update to 3.3
        // here we have to update from version 3.0, 3.1 or 3.2 to 3.3
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1") || verinfo.equals("3.2"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg3"));
            // update zettel-ids
            dataObj.db_updateZettelIDs();
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        // Update to 3.4
        // here we have to update from version 3.0, 3.1, 3.2 or 3.3 to 3.4
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1") || verinfo.equals("3.2") || verinfo.equals("3.3"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg3"));
            // update unique author and keyword-id's
            dataObj.db_updateAuthorAndKeywordIDs();
            // update timestamp attributes
            dataObj.db_updateTimestampAttributes();
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        // Update to 3.6
        // here we have to update from version 3.0, 3.1, 3.2, 3.3, 3.4 or 3.5 to 3.6
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1") || verinfo.equals("3.2") || 
                              verinfo.equals("3.3") || verinfo.equals("3.4") || verinfo.equals("3.5"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg4"));
            // retrieve attached bibtex entries
            if (bibtexObj.getAttachedFileCount()>0) {
                // and store them in regular data base
                bibtexObj.setEntries(bibtexObj.getEntriesFromAttachedFile());
            }
            updateType = updateType | UPDATE_TYPE_BIBTEX;
        }
        // Update to 3.7
        // here we have to update from version 3.0 till 3.6 to 3.7
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1") || verinfo.equals("3.2") || 
                              verinfo.equals("3.3") || verinfo.equals("3.4") || verinfo.equals("3.5") || 
                              verinfo.equals("3.6"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg4"));
            // update inline-code format tags
            dataObj.db_updateInlineCodeFormatting();
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        
        // Update to 3.8
        // here we have to update from version 3.0 till 3.7 to 3.8
        if (verinfo!=null && (verinfo.equals("3.0") || verinfo.equals("3.1") || verinfo.equals("3.2") || 
                              verinfo.equals("3.3") || verinfo.equals("3.4") || verinfo.equals("3.5") || 
                              verinfo.equals("3.6") || verinfo.equals("3.7"))) {
            // change status message
            msgLabel.setText(resourceMap.getString("msg4"));
            // update inline-code format tags
            dataObj.db_updateRemoveZettelPosElements();
            updateType = updateType | UPDATE_TYPE_DATABASE;
        }
        
        return null;  // return your result
    }
    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        if ((updateType & UPDATE_TYPE_DATABASE)!=0) {
            dataObj.setModified(true);
            dataObj.setAuthorlistUpToDate(false);
            dataObj.setKeywordlistUpToDate(false);
        }
        if ((updateType & UPDATE_TYPE_BIBTEX)!=0) {
            bibtexObj.setModified(true);
        }
        dataObj.updateVersionInfo();
    }
    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
