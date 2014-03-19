/*
 * Zettelkasten - nach Luhmann
 ** Copyright (C) 2001-2013 by Daniel Lüdecke (http://www.danielluedecke.de)
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
package de.danielluedecke.zettelkasten.tasks.importtasks;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author Daniel Luedecke
 */
public class ImportFromWeb extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the Daten object, which contains the XML data of the Zettelkasten.
     * will be passed as parameter in the constructor, see below
     */
    private Daten dataObj;
    /**
     *
     */
    private TasksData taskinfo;
    private URL websiteurl;
    /**
     *
     */
    private StringBuilder importedTypesMessage = new StringBuilder("");
    private javax.swing.JDialog parentDialog;
    private javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ImportTask.class);

    public ImportFromWeb(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, URL url) {
        super(app);
        dataObj = d;
        taskinfo = td;
        websiteurl = url;
        parentDialog = parent;
        msgLabel = label;
        taskinfo.setImportOk(true);
        taskinfo.setWebContent("");
        // set default import message
        msgLabel.setText(resourceMap.getString("importDlgMsgImportWeb"));
    }
    
    @Override
    protected Object doInBackground() {
            int pagetype = Constants.PAGE_TYPE_UNKOWN;
            // stringbuilder that will contain the content of the website
            StringBuilder webpage = new StringBuilder("");
            try {
/*                
            try {
                is = new FileInputStream(websiteurl.openStream());
                String UTF8 = "utf8";
                int BUFFER_SIZE = 8192;
                BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8), BUFFER_SIZE);
                String str;
                while ((str=br.readLine())!=null) {
                    file += str;
                }
                // open URL
                is = websiteurl.openStream();
*/                
                // open URL
                BufferedReader in = new BufferedReader(new InputStreamReader(websiteurl.openStream(), "UTF-8"));
                // buffer for stream
                String buff = "";
                // read content of website and copy content to string builder
                while ((buff=in.readLine())!=null) {
                    webpage.append(buff);
                }
                String content = webpage.toString();
                // check whether we have wordpress page
                if (content.indexOf("<meta name=\"generator\" content=\"WordPress")!=-1) {
                    pagetype = Constants.PAGE_TYPE_WORDPRESS;
                }
                // do import
                switch (pagetype) {
                    case Constants.PAGE_TYPE_WORDPRESS:
                        // find start of entry
                        int start = content.indexOf("<div class=\"entry-content\">");
                        // check if found
                        if (start!=-1) {
                            // find end of entry
                            int end = content.indexOf("<!-- end entry-content -->", start);
                            // check if found, we may have alternativ tag
                            if (-1==end) end = content.indexOf("<!-- .entry-content -->", start);
                            // check if found
                            if (end!=-1) {
                                try {
                                    // fetch content
                                    String entry = content.substring(start+27, end-6);
                                    // convert html to ubb
                                    entry = HtmlUbbUtil.replaceHtmlToUbb(entry);
                                    // set content
                                    taskinfo.setWebContent(entry);
                                    // retrieve tags and set as keywords
                                    // rel="tag">
                                }
                                catch (IndexOutOfBoundsException ex) {
                                }
                            }
                        }
                        break;
                    case Constants.PAGE_TYPE_UNKOWN:
                        taskinfo.setWebContent(content);
                        break;
                }
            }
            catch (IOException e) {
                // tell about fail
                Constants.zknlogger.log(Level.INFO,"No access to to website {0}. Import failed!", websiteurl.toString());
                taskinfo.setImportOk(false);
                return null;
            }
        return null;  // return your result
    }

    @Override protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().

        // after importing, the data file is modified, so the user does not
        // forget to save the data in the new fileformat.
        if (taskinfo.isImportOk()) {
            // TODO noch setzen?
            // dataObj.setModified(true);
        }
    }

    @Override
    protected void finished() {
        super.finished();
        taskinfo.setImportMessage(importedTypesMessage.toString());
        // Close Window
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }

}
