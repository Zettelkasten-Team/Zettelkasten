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
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author danielludecke
 */
public class SetFirstLineAsTitleTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    private final int messageOption;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(SetFirstLineAsTitleTask.class);

    SetFirstLineAsTitleTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, int mo) {
        super(app);
        dataObj = d;
        parentDialog = parent;
        msgLabel = label;
        messageOption = mo;
        // init status text
        msgLabel.setText(resourceMap.getString("msg3"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // go through all entries and search for entries without titles
        for (int cnt = 1; cnt <= dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
            // retrieve title
            String title = dataObj.getZettelTitle(cnt);
            // check whether title is empty and entry's content is NOT empty
            if (title.isEmpty() && !dataObj.isEmpty(cnt)) {
                // retrieve entry's content
                String content = dataObj.getZettelContent(cnt);
                // split content at each new line
                String[] lines = content.split(Pattern.quote("[br]"));
                // check for valid array
                if (lines.length > 0) {
                    // set new title
                    dataObj.setZettelTitle(cnt, lines[0].trim());
                    // change edited timestamp
                    dataObj.changeEditTimeStamp(cnt);
                }
                // check whether user wants to remove title-line
                if (JOptionPane.YES_OPTION == messageOption) {
                    // initiate line counter, but leave out first line (which was the title line)
                    int linecnt = 1;
                    // check whether next line is empty... might be the case, when the user
                    // used the first textline as title and separated the content with two
                    // new line separators...
                    if (lines.length > 1 && lines[1].trim().isEmpty()) {
                        linecnt = 2;
                    }
                    // now create s stringbuilder, iterate all lines and concatenate them
                    StringBuilder newContent = new StringBuilder("");
                    // iterate all lines
                    while (lines.length > linecnt) {
                        // append content line
                        newContent.append(lines[linecnt]);
                        // and append return-value, which got lost when splitting the string
                        newContent.append("[br]");
                        // increase line counter
                        linecnt++;
                    }
                    // delete last [br] if we have any content
                    if (newContent.length() > 1) {
                        newContent.setLength(newContent.length() - 4);
                    }
                    // set back new content without first title line
                    dataObj.setZettelContent(cnt, newContent.toString(), false);
                }
            }
            setProgress(cnt, 0, dataObj.getCount(Daten.ZKNCOUNT));
        }
        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
