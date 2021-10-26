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
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.jdom2.Element;

/**
 *
 * @author Luedeke
 */
public class ShowAttachmentListTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the main data class
     */
    private final Daten dataObj;
    /**
     * Reference to the data class
     */
    private final Settings settingsObj;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final DefaultTableModel tableModel;
    private LinkedList<Object[]> list;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ShowAttachmentListTask.class);

    ShowAttachmentListTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, Settings set, DefaultTableModel tm) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        dataObj = d;
        settingsObj = set;
        tableModel = tm;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }
    @Override protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.

        // check whether we have any keywords at all
        if (dataObj.getCount(Daten.ZKNCOUNT) <1) {
            // reset list
            list = null;
            // leave thread
            return null;
        }
        // create new instance of that variable
        list = new LinkedList<>();
        // go through all entries
        for (int cnt=1; cnt<=dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
            // get the attachments of each entry
            List<Element> links = dataObj.getAttachments(cnt);
            // check whether we have any attachments at all
            if (links!=null) {
                // create iterator
                Iterator<Element> i = links.iterator();
                // got through attachments of entry
                while (i.hasNext()) {
                    // and add them to the table model, with the according entry-number
                    Element e = i.next();
                    // create a new object with these data
                    Object[] ob = new Object[3];
                    ob[0] = e.getText();
                    ob[1] = FileOperationsUtil.getFileExtension(settingsObj,dataObj,e.getText());
                    ob[2] = cnt;
                    // and add it to the linked list
                    list.add(ob);
                }
            }

            // update progressbar
            setProgress(cnt,0,dataObj.getCount(Daten.ZKNCOUNT));
        }

        return null;
    }
    @Override protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        // reset the table
        tableModel.setRowCount(0);
        // check whether we have any entries at all...
        if (list!=null) {
            // create iterator for linked list
            Iterator<Object[]> i = list.iterator();
            // go through linked list and add all objects to the table model
            try {
                while (i.hasNext()) tableModel.addRow(i.next());
            }
            catch (ConcurrentModificationException e) {
                // reset the table when we have overlappings threads
                tableModel.setRowCount(0);
            }
        }
        dataObj.setAttachmentlistUpToDate(true);
    }
    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
