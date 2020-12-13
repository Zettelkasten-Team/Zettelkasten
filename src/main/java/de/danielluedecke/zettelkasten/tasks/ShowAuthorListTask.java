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

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author danielludecke
 */
public class ShowAuthorListTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the main data class
     */
    private final Daten dataObj;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final BibTeX bibtexObj;
    private int entrytype = -1;

    private final DefaultTableModel tableModel;

    private ArrayList<Object[]> list;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(ShowAuthorListTask.class);

    ShowAuthorListTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, BibTeX bt, int et, DefaultTableModel dtm) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        dataObj = d;
        bibtexObj = bt;
        tableModel = dtm;
        entrytype = et;
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

        // get the amount of authors
        int count = dataObj.getCount(Daten.AUCOUNT);
        // check whether we have any keywords at all
        if (count < 1) {
            // reset list
            list = null;
            // leave thread
            return null;
        }
        // additional counter, since some author-elements might be empty due to deletion
        // so we have to copy the initial array (aus_empty) into a "final" array, which does
        // not contain empty strings
        int cnt;

        LinkedList<String> authorlist = new LinkedList<>();
        // go through all author-elements of the author datafile
        for (cnt = 0; cnt < count; cnt++) {
            // get the authors as string
            String au = dataObj.getAuthor(cnt + 1);
            // if the author-string is not empty, check for further filtering (entrytype)
            if (!au.isEmpty()) {
                // if the entrytype is not -1 or not 0, we have to filter all authors accoding
                // to their type, i.e. article, book, incollection etc.
                if (entrytype > 0) {
                    // retrieve authors bibkey so we can retrieve the entrytype
                    String bibkey = dataObj.getAuthorBibKey(cnt + 1);
                    // if we have no bibkey and look for entries w/o bibkey...
                    if ((null == bibkey || bibkey.isEmpty()) && Constants.BIBTEX_ENTRYTYPE_NOBIBKEY == entrytype) {
                        // add author to list
                        authorlist.add(au);
                    } // if we have any bibkey, goon
                    else {
                        // check whether entrytype of author equals the requested entrytype
                        if (entrytype == bibtexObj.getEntryType(bibkey)) {
                            // add author to list
                            authorlist.add(au);
                        }
                    }
                } else {
                    authorlist.add(au);
                }
            }
            // update progressbar
            setProgress(cnt, 0, count);
        }
        // sort list
        Collections.sort(authorlist, new Comparer());
        // create new instance of that variable
        list = new ArrayList<>();
        // get list size
        int ausize = authorlist.size();
        // go through all authors of the author-array
        for (cnt = 0; cnt < ausize; cnt++) {
            // get author string
            String au = authorlist.get(cnt);
            // retrieve the frequency of that author, i.e. the amount of usage in
            // the main data file. therefore, we need the position of the author-string,
            // which we have in our linked list, and we can retrieve this position-index
            // via "getAuthorPosition". After that, we can pass this position index as
            // parameter to the method "getAuthorFrequency".
            int auc = dataObj.getAuthorFrequency(dataObj.getAuthorPosition(au));
            // create a new object with these data
            Object[] ob = new Object[2];
            ob[0] = au;
            ob[1] = auc;
            // add data to linked list
            list.add(ob);
            // update progressbar
            setProgress(cnt, 0, ausize);
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
        dataObj.setAuthorlistUpToDate(true);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
