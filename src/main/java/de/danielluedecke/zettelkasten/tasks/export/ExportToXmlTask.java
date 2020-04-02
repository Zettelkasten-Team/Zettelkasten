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
package de.danielluedecke.zettelkasten.tasks.export;

import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Luedeke
 */
public class ExportToXmlTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the CDaten object, which contains the XML data of the
     * Zettelkasten will be passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    private final BibTex bibtexObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     * This variable stores the parts which should be exported. It's a mix of
     * ORed constants, see below
     */
    private final int exportparts;
    /**
     * Indicates wheher the UBB-Fomattags should be removed and the entries
     * should be exported in plain text, without format-tags.
     */
    private final boolean removeformattags;
    /**
     * file path to export file
     */
    private final File filepath;
    /**
     *
     */
    private ArrayList<Object> exportentries;
    /**
     *
     */
    private boolean exportOk;
    /**
     *
     */
    private final boolean exportbibtex;
    /**
     *
     */
    private boolean showOkMessage = true;
    /**
     *
     */
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ExportTask.class);

    public ExportToXmlTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, BibTex bib, File fp, ArrayList<Object> ee, int part, boolean bibtex, boolean rft) {
        super(app);
        dataObj = d;
        bibtexObj = bib;
        exportbibtex = bibtex;
        filepath = fp;
        exportparts = part;
        exportentries = ee;
        removeformattags = rft;
        exportOk = true;
        taskinfo = td;
        parentDialog = parent;
        msgLabel = label;

        // the variable "exportentries" stores all entry-numbers of those entries that should be exported.
        // if this array is null, we assume that *all* entries have to be exported. thus, insert
        // all entry-numbers here
        if (null == exportentries) {
            exportentries = new ArrayList<>();
            // copy all entry-numbers to array. remember that the entrynumbers range from 1 to site of file.
            for (int cnt = 0; cnt < dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // only add entries that are not empty
                if (!dataObj.isEmpty(cnt + 1)) {
                    exportentries.add(cnt + 1);
                }
            }
        }
        // show status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect

        // if no file exists, exit task
        if (null == filepath) {
            showOkMessage = false;
            return null;
        }
        // check whether file already exists
        if (filepath.exists()) {
            // file exists, ask user to overwrite it...
            int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg", "", filepath.getName()), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if the user does *not* choose to overwrite, quit...
            if (optionDocExists != JOptionPane.YES_OPTION) {
                // don't show "export was OK" message in main frame
                showOkMessage = false;
                return null;
            }
        }
        int contentsize;
        int counter;
        // first of all, create a new, empty xml-document
        Document exportDoc = new Document(new Element("zettelkasten"));
        // yet everything is ok...
        exportOk = true;
        // create a list of all elements from the main xml file
        try {
            // get the size of the export data, used for progressbar
            contentsize = exportentries.size();
            // go through all elements of the data file
            for (counter = 0; counter < exportentries.size(); counter++) {
                // add the headline to our final export document
                exportDoc.getRootElement().addContent(exportEntries(counter));
                // update progress bar
                setProgress(counter, 0, contentsize);
            }
        } catch (IllegalStateException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // show warning message box
            JOptionPane.showMessageDialog(null, resourceMap.getString("errorExportMsg"), resourceMap.getString("errorExportTitle"), JOptionPane.PLAIN_MESSAGE);
            // and change indicator
            exportOk = false;
        }
        //
        // now that we've created our xml-document, we can
        // export it to a file
        //
        FileOutputStream fos = null;
        try {
            // show status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // open the outputstream
            fos = new FileOutputStream(filepath);
            // create a new XML-outputter with the pretty output format,
            // so the xml-file looks nicer
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            // save the main-export-file
            out.output(exportDoc, fos);
        } catch (IOException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            // and change indicator
            exportOk = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // log error-message
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                // and change indicator
                exportOk = false;
            }
        }
        // if the user requested a bibtex-export, do this now
        if (exportbibtex) {
            // show status text
            msgLabel.setText(resourceMap.getString("msgBibtextExport"));
            // write bibtex file
            ExportTools.writeBibTexFile(dataObj, bibtexObj, exportentries, filepath, resourceMap);
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
        taskinfo.setExportOk(exportOk);
        taskinfo.setShowExportOkMessage(showOkMessage);
        // Close Window
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }

    /**
     *
     * @param counter
     * @return
     */
    private Element exportEntries(int counter) {
        try {
            // retrieve zettelnumber
            int zettelnummer = Integer.parseInt(exportentries.get(counter).toString());
            // get the zettel-element
            Element zettel = dataObj.retrieveZettel(zettelnummer);
            // create new zettel-element for our final export file
            Element el_zettel = new Element(Daten.ELEMENT_ZETTEL);

            el_zettel.setAttribute(Daten.ATTRIBUTE_ZETTEL_ID, ""+zettelnummer);

            // see whether the bit "EXPORT_TITLE" is set
            // in the exportparts-variabe. if so, export title
            if ((exportparts & Constants.EXPORT_TITLE) != 0) {
                // create new title element
                Element el = new Element(Daten.ELEMENT_TITLE);
                // set the text from the data-file
                el.setText(zettel.getChild(Daten.ELEMENT_TITLE).getText());
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_CONTENT" is set
            // in the exportparts-variabe. if so, export content
            if ((exportparts & Constants.EXPORT_CONTENT) != 0) {
                // create new content element
                Element el = new Element(Daten.ELEMENT_CONTENT);
                // set the text from the data-file
                el.setText((removeformattags)
                        ? dataObj.getCleanZettelContent(zettelnummer)
                        : zettel.getChild(Daten.ELEMENT_CONTENT).getText());
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_AUTHOR" is set
            // in the exportparts-variabe. if so, export author
            if ((exportparts & Constants.EXPORT_AUTHOR) != 0) {
                // create new content element
                Element el = new Element(Daten.ELEMENT_AUTHORS);
                // first check, whether we have any keywords at all
                if (zettel.getChild(Daten.ELEMENT_AUTHOR).getText().isEmpty()) {
                    // if not, set empty string
                    el.setText("");
                } else {
                    // get the author string
                    String[] aus = zettel.getChild(Daten.ELEMENT_AUTHOR).getText().split(",");
                    // if we have any author, go on
                    if (aus != null && aus.length > 0) {
                        // iterate array
                        for (String a : aus) {
                            // create new child-element
                            Element au = new Element(Daten.ELEMENT_AUTHOR);
                            // set the text from the data-file
                            au.setText(dataObj.getAuthor(Integer.parseInt(a)));
                            // add child-element
                            el.addContent(au);
                        }
                    } else {
                        // else set empty string
                        el.setText("");
                    }
                }
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_KEYWORDS" is set
            // in the exportparts-variabe. if so, export keywords
            if ((exportparts & Constants.EXPORT_KEYWORDS) != 0) {
                // create new content element
                Element el = new Element(Daten.ELEMENT_KEYWORD);
                // first check, whether we have any keywords at all
                if (zettel.getChild(Daten.ELEMENT_KEYWORD).getText().isEmpty()) {
                    // if not, set empty string
                    el.setText("");
                } else {
                    // get the index numbers. we now have all keyword-index-numbers
                    // as a string array. these numbers reference to the keyword-string-values
                    // in the keyword-xml-file
                    String[] nrs = zettel.getChild(Daten.ELEMENT_KEYWORD).getText().split(",");
                    // if we have any author, go on
                    if (nrs != null && nrs.length > 0) {
                        // iterate the array
                        for (String n : nrs) {
                            // create new child element
                            Element kw = new Element("keyword");
                            // now get the keyword string from the keyword-xml-file
                            kw.setText(dataObj.getKeyword(Integer.parseInt(n)));
                            // and add this subchild
                            el.addContent(kw);
                        }
                    } else {
                        // else set empty string
                        el.setText("");
                    }
                }
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_MANLINKS" is set
            // in the exportparts-variabe. if so, export manual links
            if ((exportparts & Constants.EXPORT_MANLINKS) != 0) {
                // create new manlinks element
                Element el = new Element(Daten.ELEMENT_MANLINKS);
                // set the text from the data-file
                el.setText(zettel.getChild(Daten.ELEMENT_MANLINKS).getText());
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_MANLINKS" is set
            // in the exportparts-variabe. if so, export manual links
            if ((exportparts & Constants.EXPORT_LUHMANN) != 0) {
                // create new manlinks element
                Element el = new Element(Daten.ELEMENT_TRAILS);
                // set the text from the data-file
                el.setText(zettel.getChild(Daten.ELEMENT_TRAILS).getText());
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_LINKS" is set
            // in the exportparts-variabe. if so, export links
            if ((exportparts & Constants.EXPORT_LINKS) != 0) {
                // create new link element
                Element el = new Element(Daten.ELEMENT_ATTACHMENTS);
                // add the content from the data-file. we cannot use settext here,
                // because we might have several sub-children
                // get the list of all sub-children
                List<Element> l = zettel.getChild(Daten.ELEMENT_ATTACHMENTS).getChildren();
                // create an iterator
                Iterator<Element> i = l.iterator();
                // go through loop and add all children
                while (i.hasNext()) {
                    // create child-element for our parent-element
                    Element el_link = new Element(Daten.ELEMENT_ATTCHILD);
                    // get the child-element from the list
                    Element el_dummy = i.next();
                    // and set the text to our created child element
                    el_link.setText(el_dummy.getText());
                    // add the child-element to our parent
                    el.addContent(el_link);
                }
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_REMARKS" is set
            // in the exportparts-variabe. if so, export remarks
            if ((exportparts & Constants.EXPORT_REMARKS) != 0) {
                // create new remarks element
                Element el = new Element(Daten.ELEMENT_REMARKS);
                // set the text from the data-file
                el.setText(zettel.getChild(Daten.ELEMENT_REMARKS).getText());
                // and add it to our final document
                el_zettel.addContent(el);
            }
            // see whether the bit "EXPORT_TIMESTAMP" is set
            // in the exportparts-variabe. if so, export timestamp
            if ((exportparts & Constants.EXPORT_TIMESTAMP) != 0) {
                // set timestamp for export element
                dataObj.setTimestamp(el_zettel, dataObj.getTimestampCreated(zettel), dataObj.getTimestampEdited(zettel));
            }
            return el_zettel;
        } catch (NumberFormatException e) {
            // create new headline element
            Element headline = new Element("headline");
            // add headline-text to it.
            headline.setText(exportentries.get(counter).toString().substring(2));
            return headline;
        }
    }
}
