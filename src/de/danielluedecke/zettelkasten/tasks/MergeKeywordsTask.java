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
import de.danielluedecke.zettelkasten.database.TasksData;
import java.util.LinkedList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Luedeke
 */
public class MergeKeywordsTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the main data class
     */
    private final Daten dataObj;
    private final TasksData taskdata;
    private final String oldKw;
    private final String newKw;
    private final JTable kwtable;
    private final int selectedrow;
    private final LinkedList<Object[]> linkedkeywords;
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(MergeKeywordsTask.class);

    MergeKeywordsTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, TasksData td, String o, String n, JTable t, int sr, LinkedList<Object[]> ll) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        dataObj = d;
        taskdata = td;
        parentDialog = parent;
        msgLabel = label;
        oldKw=o;
        newKw=n;
        kwtable=t;
        selectedrow = sr;
        linkedkeywords = ll;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }
    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        
        // mergekeywords
        dataObj.mergeKeywords(oldKw, newKw);
        // get the amount of occurences of the new keyword
        int newcnt = dataObj.getKeywordFrequencies(newKw);
        // go through the whole table and get all values
        for (int cnt=0; cnt<kwtable.getRowCount(); cnt++) {
            // if we found the new keyword (that already existed), add the
            // old count-value
            if (kwtable.getValueAt(cnt, 0).equals(newKw)) {
                // replace old count with new count
                kwtable.setValueAt(String.valueOf(newcnt), cnt, 1);
            }
        }
        // if we have a linked list, search for the old value, delete it,
        // search for the new value, and add the newcount as new occurence-value
        if (linkedkeywords!=null) {
            // for later use, to remove the old value
            int removeindex = -1;
            // iterate complete linked list
            for (int cnt=0; cnt<linkedkeywords.size(); cnt++) {
                // get each element
                Object[] o = linkedkeywords.get(cnt);
                // if element equals old keyword, store the indexnumber for later removal
                if (o[0].toString().equals(oldKw)) removeindex = cnt;
                // if we found the new keyword, add occurence-counter
                if (o[0].toString().equals(newKw)) {
                    // update object array
                    o[1] = String.valueOf(newcnt);
                    // set back new value
                    linkedkeywords.set(cnt, o);
                }
            }
            // finally, remove the old keyword
            if (removeindex!=-1) linkedkeywords.remove(removeindex);
        }

        // get the position of the new keyword in the table
        // now we also have to remove the rows with the deleted data from the table
        // therefore, get the table model to access the table data
        DefaultTableModel dtm = (DefaultTableModel)kwtable.getModel();
        // remove the row
        dtm.removeRow(kwtable.convertRowIndexToModel(selectedrow));
        return null;  // return your result
    }
    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        taskdata.setLinkedValues(linkedkeywords);
    }
    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
