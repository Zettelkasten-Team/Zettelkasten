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

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.SearchRequests;

/**
 * This Task actually does <b>not</b> delete the entry, but removes the number of the deleted
 * entry from any related other entries, i.e. the deleted entry's number will be removed from
 * scents and search requests.
 * 
 * @author danielludecke
 */
public class DeleteEntryTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * CDaten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     * 
     */
    private final SearchRequests searchrequestsObj;
    /**
     * Array of entry-index-numbers. These entries 
     * will be deleted.
     */
    private final int[] entries;
    
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(DeleteEntryTask.class);
    
    
    /**
     * This progress dialog does the final step when deleting entries. While removal of
     * entry in the data base is quite fast, the removal of links like trailing
     * entries or search results may be time consuming. hence, this task is performed
     * in this separate backgroud task.
     * 
     * @param app reference to Zettelkasten.app
     * @param parent reference to parent dialog, usually "this"
     * @param label message label for progress dialog
     * @param d reference to Daten class
     * @param sr reference to SearchRequests class
     * @param nrs Array of entry-index-numbers. These entries will be deleted.
     */
    DeleteEntryTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, SearchRequests sr, int[] nrs) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        searchrequestsObj=sr;
        entries = nrs;
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

        int len = dataObj.getCount(Daten.ZKNCOUNT);
        // remove deleted entry-numbers from all luhmann-numbers of other entries...
        for (int cnt = 0; cnt < len; cnt++) {
            for (int e : entries) {
                dataObj.deleteLuhmannNumber(cnt + 1, e);
            }
            setProgress(cnt, 0, len);
        }
        // remove deleted entries from search requests...
        for (int e : entries) {
            for (int cnt = 0; cnt < searchrequestsObj.getCount(); cnt++) {
                if (searchrequestsObj.getZettelPositionInResult(cnt, e) != -1) {
                    searchrequestsObj.deleteResultEntry(cnt, e);
                }
            }
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
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
