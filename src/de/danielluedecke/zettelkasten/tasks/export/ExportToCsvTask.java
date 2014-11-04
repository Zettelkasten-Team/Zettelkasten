/*
 * Zettelkasten - nach Luhmann
 ** Copyright (C) 2001-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
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

import au.com.bytecode.opencsv.CSVWriter;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jdom2.Element;

/**
 *
 * @author Luedeke
 */
public class ExportToCsvTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the CDaten object, which contains the XML data of the Zettelkasten
     * will be passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     * 
     */
    private final BibTex bibtexObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     * Indicates wheher the UBB-Fomattags should be removed and the entries should be exported
     * in plain text, without format-tags.
     */
    private final boolean removeformattags;
    /**
     * Indicates whether or not a bibtex-file from the exported entries should be created or not
     */
    private final boolean exportbibtex;
    /**
     *
     */
    private final char csvseparator;
    /**
     * This variable stores the parts which should be exported. It's a mix of
     * ORed constants, see below
     */
    private final int exportparts;
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
    private boolean showOkMessage = true;
    /**
     *
     */
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ExportTask.class);
    
    public ExportToCsvTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, BibTex bto, File fp, ArrayList<Object> ee, int part, char csep,
            boolean notag, boolean bibtex) {
        super(app);
        dataObj = d;
        bibtexObj = bto;
        filepath = fp;
        exportparts=part;
        removeformattags = notag;
        csvseparator = csep;
        exportbibtex = bibtex;
        exportentries = ee;
        exportOk = true;
        taskinfo = td;
        parentDialog = parent;
        msgLabel = label;
        
        // the variable "exportentries" stores all entry-numbers of those entries that should be exported.
        // if this array is null, we assume that *all* entries have to be exported. thus, insert
        // all entry-numbers here
        if (null==exportentries) {
            exportentries = new ArrayList<>();
            // copy all entry-numbers to array. remember that the entrynumbers range from 1 to site of file.
            for (int cnt=0; cnt<dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // only add entries that are not empty
                if (!dataObj.isEmpty(cnt+1)) {
                    exportentries.add(cnt+1);
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
            int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg","",filepath.getName()), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if the user does *not* choose to overwrite, quit...
            if (optionDocExists!=JOptionPane.YES_OPTION) {
                // don't show "export was OK" message in main frame
                showOkMessage = false;
                return null;
            }
        }
        int contentsize;
        int counter;
        // yet everything is ok...
        exportOk = true;
        // create csv-writer and export the data
        // get the size of the export data, used for progressbar
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"), csvseparator)) {
            // get the size of the export data, used for progressbar
            contentsize = exportentries.size();
            // create linked list which will hold all values of one comma-separated line
            LinkedList<String> csvline = new LinkedList<>();
            // first of all, create a "header"-line that contains the headers/description for the parts of an entry
            // that should be exported
            if ((exportparts & Constants.EXPORT_TITLE)!=0) {
                csvline.add(resourceMap.getString("csvHeaderTitle"));
            }
            if ((exportparts & Constants.EXPORT_CONTENT)!=0) {
                csvline.add(resourceMap.getString("csvHeaderContent"));
            }
            if ((exportparts & Constants.EXPORT_AUTHOR)!=0) {
                csvline.add(resourceMap.getString("csvHeaderAuthor"));
            }
            if ((exportparts & Constants.EXPORT_KEYWORDS)!=0) {
                csvline.add(resourceMap.getString("csvHeaderKeywords"));
            }
            if ((exportparts & Constants.EXPORT_MANLINKS)!=0) {
                csvline.add(resourceMap.getString("csvHeaderManLinks"));
            }
            if ((exportparts & Constants.EXPORT_LUHMANN)!=0) {
                csvline.add(resourceMap.getString("csvHeaderLuhmann"));
            }
            if ((exportparts & Constants.EXPORT_LINKS)!=0) {
                csvline.add(resourceMap.getString("csvHeaderLinks"));
            }
            if ((exportparts & Constants.EXPORT_REMARKS)!=0) {
                csvline.add(resourceMap.getString("csvHeaderRemarks"));
            }
            if ((exportparts & Constants.EXPORT_TIMESTAMP)!=0) {
                csvline.add(resourceMap.getString("csvHeaderTimestamp"));
            }
            // copy linked list to string array
            String[] finalline = csvline.toArray(new String[csvline.size()]);
            // write array to csv-file
            writer.writeNext(finalline);
            // go through all elements of the data file
            for (counter=0; counter<exportentries.size(); counter++) {
                try {
                    // retrieve zettelnumber
                    int zettelnummer = Integer.parseInt(exportentries.get(counter).toString());
                    // get the zettel-element
                    Element zettel = dataObj.retrieveZettel(zettelnummer);
                    // clear data-line
                    csvline.clear();
                    // see whether the bit "EXPORT_TITLE" is set
                    // in the exportparts-variabe. if so, export title
                    if ((exportparts & Constants.EXPORT_TITLE)!=0) {
                        csvline.add(zettel.getChild("title").getText());
                    }
                    // see whether the bit "EXPORT_CONTENT" is set
                    // in the exportparts-variabe. if so, export content
                    if ((exportparts & Constants.EXPORT_CONTENT)!=0) {
                        csvline.add((removeformattags)
                                ? dataObj.getCleanZettelContent(zettelnummer)
                                : dataObj.getZettelContent(zettelnummer));
                    }
                    // see whether the bit "EXPORT_AUTHOR" is set
                    // in the exportparts-variabe. if so, export author
                    if ((exportparts & Constants.EXPORT_AUTHOR)!=0) {
                        // get author strings
                        String[] aus = dataObj.getAuthors(zettelnummer);
                        // if we have any author, go on
                        if (aus!=null && aus.length>0) {
                            // create string builder for author values
                            StringBuilder sbauthor = new StringBuilder("");
                            // iterate array of authors
                            for (String a : aus) {
                                // append author to stringbuilder
                                sbauthor.append(a);
                                // and add a new line
                                sbauthor.append(System.lineSeparator());
                            }
                            // if we have any values in the stringbuilder, truncate last line separator
                            if (sbauthor.length()>1) {
                                sbauthor.setLength((sbauthor.length()-System.lineSeparator().length()));
                            }
                            // finally, add author values to the csv-line
                            csvline.add(sbauthor.toString());
                        }
                        else {
                            // else set empty string
                            csvline.add("");
                        }
                    }
                    // see whether the bit "EXPORT_KEYWORDS" is set
                    // in the exportparts-variabe. if so, export keywords
                    if ((exportparts & Constants.EXPORT_KEYWORDS)!=0) {
                        // get keywords-trings
                        String[] kws = dataObj.getKeywords(zettelnummer,true);
                        // if we have any author, go on
                        if (kws!=null && kws.length>0) {
                            // create string builder for author values
                            StringBuilder sbkeywords = new StringBuilder("");
                            // iterate array of authors
                            for (String k : kws) {
                                // append author to stringbuilder
                                sbkeywords.append(k);
                                // and add a new line
                                sbkeywords.append(System.lineSeparator());
                            }
                            // if we have any values in the stringbuilder, truncate last line separator
                            if (sbkeywords.length()>1) {
                                sbkeywords.setLength((sbkeywords.length()-System.lineSeparator().length()));
                            }
                            // finally, add author values to the csv-line
                            csvline.add(sbkeywords.toString());
                        }
                        else {
                            // else set empty string
                            csvline.add("");
                        }
                    }
                    // see whether the bit "EXPORT_MANLINKS" is set
                    // in the exportparts-variabe. if so, export manual links
                    if ((exportparts & Constants.EXPORT_MANLINKS)!=0) {
                        csvline.add(zettel.getChild(Daten.ELEMENT_MANLINKS).getText());
                    }
                    // see whether the bit "EXPORT_MANLINKS" is set
                    // in the exportparts-variabe. if so, export manual links
                    if ((exportparts & Constants.EXPORT_LUHMANN)!=0) {
                        csvline.add(zettel.getChild("luhmann").getText());
                    }
                    // see whether the bit "EXPORT_LINKS" is set
                    // in the exportparts-variabe. if so, export links
                    if ((exportparts & Constants.EXPORT_LINKS)!=0) {
                        // add the content from the data-file. we cannot use settext here,
                        // because we might have several sub-children
                        // get the list of all sub-children
                        List<Element> l = dataObj.getAttachments(zettelnummer);
                        // create an iterator
                        Iterator<Element> i = l.iterator();
                        // create string builder for csv-value
                        StringBuilder links = new StringBuilder("");
                        // go through loop and add all children
                        while (i.hasNext()) {
                            // get the child-element from the list
                            Element el_dummy = i.next();
                            // and set the text to our created child element
                            links.append(el_dummy.getText());
                            links.append(System.lineSeparator());
                        }
                        // if we have any values in the stringbuilder, truncate last line separator
                        if (links.length()>1) {
                            links.setLength((links.length()-System.lineSeparator().length()));
                        }
                        // finally, add author values to the csv-line
                        csvline.add(links.toString());
                    }
                    // see whether the bit "EXPORT_REMARKS" is set
                    // in the exportparts-variabe. if so, export remarks
                    if ((exportparts & Constants.EXPORT_REMARKS)!=0) {
                        csvline.add(zettel.getChild(Daten.ELEMENT_REMARKS).getText());
                    }
                    // see whether the bit "EXPORT_TIMESTAMP" is set
                    // in the exportparts-variabe. if so, export timestamp
                    if ((exportparts & Constants.EXPORT_TIMESTAMP)!=0) {
                        // add timestamp to csv
                        csvline.add(dataObj.getTimestampCreated(zettel) +";"+dataObj.getTimestampEdited(zettel));
                    }
                    // copy linked list to string array
                    finalline = csvline.toArray(new String[csvline.size()]);
                    // write array to csv-file
                    writer.writeNext(finalline);
                    // update progress bar
                    setProgress(counter,0,contentsize);
                }
                catch (NumberFormatException e) {
                    // write headline to csv-file
                    writer.writeNext(new String[] {exportentries.get(counter).toString().substring(2)});
                    // update progress bar
                    setProgress(counter,0,contentsize);
                }
            }
            // close outputstream
        }
        catch (IOException e) {
            // log error-message
            Constants.zknlogger.log(Level.SEVERE,e.getLocalizedMessage());
            // and change indicator
            exportOk = false;
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

    
}
