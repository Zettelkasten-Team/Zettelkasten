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

import bibtex.dom.BibtexEntry;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Luedeke
 */
public class RefreshBibTexTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the main data class
     */
    private final Daten dataObj;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final BibTeX bibtexObj;
    private final TasksData taskinfo;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(RefreshBibTexTask.class);

    RefreshBibTexTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, 
            javax.swing.JLabel label, TasksData td, Daten d, BibTeX bt) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        dataObj = d;
        bibtexObj = bt;
        taskinfo = td;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msgBibTexRefresh"));
    }

    @Override protected Object doInBackground() {
        // get attached entries
        ArrayList<BibtexEntry> attachedbibtexentries = bibtexObj.getEntriesFromAttachedFile();
        // for progress bar
        int cnt = 0;
        int length = attachedbibtexentries.size();
        // amount of upated entries
        int updateCount = 0;
        StringBuilder updatedAuthors = new StringBuilder("");
        // iterate all new entries
        for (BibtexEntry attachedbibtexentry : attachedbibtexentries) {
            // do we have this entry?
            String bibkey = attachedbibtexentry.getEntryKey();
            if (bibtexObj.hasEntry(bibkey)) {
                // if yes, update it
                bibtexObj.setEntry(bibkey, attachedbibtexentry);
                // retrieve author position 
                int aupos = dataObj.getAuthorBibKeyPosition(bibkey);
                // check if we have author already
                if (aupos != -1) {
                    // get current author
                    String oldAuthor = dataObj.getAuthor(aupos);
                    // get formatted author
                    String updatedAuthor = bibtexObj.getFormattedEntry(attachedbibtexentry, true);
                    // update author data, if it differs
                    if (!oldAuthor.equals(updatedAuthor)) {
                        // update author in data base
                        dataObj.setAuthor(aupos, updatedAuthor);
                        // copy info to string
                        updatedAuthors.append(updatedAuthor)
                                .append(" (bibkey: ")
                                .append(bibkey)
                                .append(")")
                                .append(System.lineSeparator());
                        updateCount++;
                    }
                }
            }
            // update progressbar
            setProgress(cnt++, 0, length);
        }
        // add all new entries to data base
        int newentries = bibtexObj.addEntries(attachedbibtexentries);
        // tell user
        if (newentries > 0 || updateCount > 0) {
            JOptionPane.showMessageDialog(null, 
                    resourceMap.getString("importMissingBibtexEntriesText", 
                            String.valueOf(newentries),
                            String.valueOf(updateCount)), 
                    "BibTeX-Import",
                    JOptionPane.PLAIN_MESSAGE);
        }
        // log info about updates authors
        taskinfo.setUpdatedAuthors(updatedAuthors.toString());
        return null;
    }

    @Override protected void succeeded(Object result) {
        dataObj.setAuthorlistUpToDate(false);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
