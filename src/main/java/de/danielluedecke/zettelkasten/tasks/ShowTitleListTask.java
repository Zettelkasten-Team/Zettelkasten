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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Luedeke
 */
public class ShowTitleListTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the data class
     */
    private final Daten data;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final DefaultTableModel tableModel;
    private LinkedList<Object[]> list;
    
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ShowTitleListTask.class);

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    ShowTitleListTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, DefaultTableModel tm) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        data = d;
        tableModel = tm;
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

        // check whether we have any keywords at all
        if (data.getCount(Daten.ZKNCOUNT) < 1) {
            // reset list
            list = null;
            // leave thread
            return null;
        }
        // create new instance of that variable
        list = new LinkedList<>();
        // reset progress counter
        int total_count = data.getCount(Daten.ZKNCOUNT);
        // start loop
        for (int cnt = 1; cnt <= data.getCount(Daten.ZKNCOUNT); cnt++) {
            // is entry deleted?
            if (data.isDeleted(cnt)) {
                // skip entry
                continue;
            }
            // get zettel-title
            String title = data.getZettelTitle(cnt);
            // get timestamp
            String[] timestamp = data.getTimestamp(cnt);
            // init timestamp variables.
            String created = "";
            String edited = "";
            // check whether we have any timestamp at all.
            if (timestamp != null && !timestamp[0].isEmpty() && timestamp[0].length() >= 6) {
                created = timestamp[0].substring(4, 6) + "." + timestamp[0].substring(2, 4) + ".20" + timestamp[0].substring(0, 2);
            }
            // check whether we have any timestamp at all.
            if (timestamp != null && !timestamp[1].isEmpty() && timestamp[1].length() >= 6) {
                edited = timestamp[1].substring(4, 6) + "." + timestamp[1].substring(2, 4) + ".20" + timestamp[1].substring(0, 2);
            }
            
            // Make Note Sequences column sortable.
            String luhmannindex = "0";
            if (data.isTopLevelLuhmann(cnt)) {
                luhmannindex = "3";
            } else if (data.findParentLuhmann(cnt, true) != -1) {
                if (data.hasLuhmannNumbers(cnt)) {
                    luhmannindex = "2";
                } else {
                    luhmannindex = "1";
                }
            }
                
            // create a new object with these data
            Object[] ob = new Object[6];
            ob[0] = cnt; // ob[0] = String.valueOf(cnt);
            ob[1] = title;
            ob[2] = created;
            ob[3] = edited;
            ob[4] = data.getZettelRating(cnt);
            ob[5] = luhmannindex;
            // and add it to the table
            list.add(ob);
            // update progressbar
            setProgress(cnt, 0, total_count);
        }
        return null;
    }

    @Override
    protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        // reset the table
        tableModel.setRowCount(0);
        // check whether we have any entries at all...
        if (list != null) {
            // create iterator for linked list
            Iterator<Object[]> i = list.iterator();
            // go through linked list and add all objects to the table model
            try {
                while (i.hasNext()) {
                    tableModel.addRow(i.next());
                }
            } catch (ConcurrentModificationException e) {
                // reset the table when we have overlappings threads
                tableModel.setRowCount(0);
            }
        }
        data.setTitlelistUpToDate(true);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
