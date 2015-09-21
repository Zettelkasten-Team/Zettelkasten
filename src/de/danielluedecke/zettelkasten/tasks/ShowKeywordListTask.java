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
import de.danielluedecke.zettelkasten.database.Synonyms;
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
public class ShowKeywordListTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the main data class
     */
    private final Daten dataObj;
    /**
     * Reference to the synonyms class
     */
    private final Synonyms synonymsObj;
    /**
     *
     */
    private final boolean includeSynonyms;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final DefaultTableModel tableModel;

    private ArrayList<Object[]> list;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ShowKeywordListTask.class);

    ShowKeywordListTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, Synonyms s, boolean is, DefaultTableModel dtm) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        dataObj = d;
        synonymsObj = s;
        includeSynonyms = is;
        tableModel = dtm;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.

        // get the amount of keywords
        int count = dataObj.getCount(Daten.KWCOUNT);
        // check whether we have any keywords at all
        if (count<1) {
            // reset list
            list = null;
            // leave thread
            return null;
        }
        // additional counter, since some keyword-elements might be empty due to deletion
        // so we have to copy the initial array (kws_empty) into a "final" array, which does
        // not contain empty strings
        int cnt;

        LinkedList<String> keywordlist = new LinkedList<>();

        // go through all keywords of the keyword datafile
        for (cnt=0; cnt<count; cnt++) {
            // get the keyeords as string
            String kw = dataObj.getKeyword(cnt+1);
            // if the keyword-string is not empty, add it to our linked list
            if (!kw.isEmpty()) keywordlist.add(kw);
            // update progressbar
            setProgress(cnt,0,count);
        }
        // check whether the user wants to include keyword-synonyms into
        // the jTableKeywords. If yes, include them here...
        if (includeSynonyms) {
            // change status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // first, get the synonyms-line for the whole keywords-expression,
            // i.e. even if a single keyword-expression consists of several words,
            // we use this as a single "whole word" unit.
            // add all synonyms to keyword list, independent from whether they
            // are already in use or not
            String[] syns = synonymsObj.getAllSynonyms();
            // check for valid values
            if (syns!=null && syns.length>0) {
                // iterate synonyms-array and add each synonym that is not already
                // in the keywordlist
                for (String s : syns) {
                    if (!keywordlist.contains(s)) {
                        keywordlist.add(s);
                    }
                }
            }
        }
        // sort list
        Collections.sort(keywordlist, new Comparer());
        // create new instance of that variable
        list = new ArrayList<>();
        // get list size
        int kwsize = keywordlist.size();

        // go through all keywords of the keyword array
        for (cnt=0; cnt<kwsize; cnt++) {
            // retrieve keyword
            String kw = keywordlist.get(cnt);
            // retrieve the frequency of that keyword, i.e. the amount of usage in
            // the main data file. therefor, we need the position of the keyword-string,
            // which we have in our linked list, and we can retrieve this position-index
            // via "getKeywordPosition". After that, we can pass this position index as
            // parameter to the method "getKeywordFrequency".
            int pos = dataObj.getKeywordPosition(kw,false);
            int kwc = (pos!=-1) ? dataObj.getKeywordFrequency(pos) : 0;
            // create a new object with these data
            Object[] ob = new Object[2];
            ob[0] = kw;
            ob[1] = kwc;
            // and add it to the table
            list.add(ob);
            // update progressbar
            setProgress(cnt,0,kwsize);
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
        dataObj.setKeywordlistUpToDate(true);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
