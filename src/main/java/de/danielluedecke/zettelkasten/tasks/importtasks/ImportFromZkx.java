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
package de.danielluedecke.zettelkasten.tasks.importtasks;

import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

// TODO beim Import alter Daten und bei default-timestamp auch sek. und millisek. setzen

/**
 *
 * @author Luedeke
 */
public class ImportFromZkx extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the Daten object, which contains the XML data of the Zettelkasten. will be
     * passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     * Reference to the Bookmarks object, which contains the XML data of the bookmarks. will be
     * passed as parameter in the constructor, see below
     */
    private final Bookmarks bookmarksObj;
    /**
     * file path to import file
     */
    private final File filepath;
    /**
     * A default timestamp for importing old datafiles. Sometimes entries of old data files may not
     * contain timestamps. so we can insert a default value here...
     */
    private String defaulttimestamp;
    /**
     *
     */
    private final StringBuilder importedTypesMessage = new StringBuilder("");
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(ImportTask.class);

    /**
     *
     * @param app
     * @param parent
     * @param label
     * @param td
     * @param d
     * @param bm
     * @param dt
     * @param sr
     * @param fp
     * @param dts
     */
    public ImportFromZkx(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, Bookmarks bm, DesktopData dt, SearchRequests sr,
            File fp, String dts) {
        super(app);
        // init of the variable either passed as parameters or initiated the first time
        dataObj = d;
        taskinfo = td;
        bookmarksObj = bm;
        parentDialog = parent;
        msgLabel = label;

        filepath = fp;
        defaulttimestamp = dts;

        if (null == defaulttimestamp) {
            defaulttimestamp = Tools.getTimeStamp();
        }
        taskinfo.setImportOk(true);
        // set default import message
        msgLabel.setText(resourceMap.getString("importDlgMsgImport"));
    }

    @Override
    protected Object doInBackground() {
        // what we do here is importing new zettelkasten-data (.zkn3).
        // in the beginning, we simply load the data-file. but when appending
        // it to the existing data, we need to convert the author- and keyword-
        // index-numbers. therefore, for each entry the author and keyword-strings
        // are retrieved, added to the existing author and keyword-file, and the
        // new index-numbers replace the old ones. finally, when the complete
        // data-file is converted, it is appended to the existing data-file.
        //

        // TODO unique-Zettel-IDs durch Verweise auf entsprechende Zettel (Zettelnummer) ersetzen.
        // dies gilt für:
        // - Schreibtischdaten
        // - Suchergebnisse

        // create dummy-documents, where the imported data is stored.
        Document zkn3Doc = new Document(new Element("zettelkasten"));
        Document author3Doc = new Document(new Element("authors"));
        Document keyword3Doc = new Document(new Element(Daten.ELEMENT_KEYWORD));
        Document bookmark3Doc = new Document(new Element("bookmarks"));
        Document search3Doc = new Document(new Element("searches"));
        Document desktop3Doc = new Document(new Element("desktops"));
        Document desktopModifiedEntries3Doc = new Document(new Element("modifiedEntries"));
        Document meta3Doc = new Document(new Element("metainformation"));
        // it looks like the SAXBuilder is closing an input stream. So we have to
        // re-open the ZIP-file each time we want to retrieve an XML-file from it
        // this is necessary, because we want tot retrieve the zipped xml-files
        // *without* temporarily saving them to harddisk
        for (int cnt = 0; cnt < dataObj.getFilesToLoadCount(); cnt++) {
            // open the zip-file
            ZipInputStream zip = null;
            try {
                zip = new ZipInputStream(new FileInputStream(filepath));                    
                ZipEntry zentry;
                // now iterate the zip-file, searching for the requested file in it
                while ((zentry = zip.getNextEntry()) != null) {
                    String entryname = zentry.getName();
                    // if the found file matches the requested one, start the SAXBuilder
                    if (entryname.equals(dataObj.getFileToLoad(cnt))) {
                        try {
                            SAXBuilder builder = new SAXBuilder();
                            Document doc = builder.build(zip);
                            // compare, which file we have retrieved, so we store the data
                            // correctly on our data-object
                            if (entryname.equals(Constants.metainfFileName)) {
                                meta3Doc = doc;
                            }
                            if (entryname.equals(Constants.zknFileName)) {
                                zkn3Doc = doc;
                            }
                            if (entryname.equals(Constants.authorFileName)) {
                                author3Doc = doc;
                            }
                            if (entryname.equals(Constants.keywordFileName)) {
                                keyword3Doc = doc;
                            }
                            if (entryname.equals(Constants.bookmarksFileName)) {
                                bookmark3Doc = doc;
                            }
                            if (entryname.equals(Constants.searchrequestsFileName)) {
                                search3Doc = doc;
                            }
                            if (entryname.equals(Constants.desktopFileName)) {
                                desktop3Doc = doc;
                            }
                            if (entryname.equals(Constants.desktopModifiedEntriesFileName)) {
                                desktopModifiedEntries3Doc = doc;
                            }
                            break;
                        } catch (JDOMException e) {
                            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                taskinfo.setImportOk(false);
                return null;
            } finally {
                try {
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    taskinfo.setImportOk(false);
                    return null;
                }
            }
        }
        // retrieve version-element
        Element ver_el = meta3Doc.getRootElement().getChild("version");
        // store fileformat-information
        String importedFileFormat = "";
        // check whether it's null or not
        if (null == ver_el) {
            taskinfo.setImportOk(false);
        } else {
            // get data-version of imported file
            importedFileFormat = ver_el.getAttributeValue("id");
            float lv = Float.parseFloat(importedFileFormat);
            // get fileversion of backward compatibility
            // float cv = Float.parseFloat(currentFileFormat);
            float cv = Float.parseFloat(Daten.backwardCompatibleVersion);
            // check whether the current data-version is newer than the loaded one
            taskinfo.setImportOk(lv >= cv);
        }
        // if we have not the right file-format, tell this the user...
        if (!taskinfo.isImportOk()) {
            // log error-message
            Constants.zknlogger.log(Level.WARNING,
                    "Failed when importing Zettelkasten-data. Import-Fileversion: {0} Requested Fileversion: {1}",
                    new Object[]{importedFileFormat, Daten.backwardCompatibleVersion});
            // display error message box
            JOptionPane.showMessageDialog(null,
                    resourceMap.getString("importDlgErrWrongFileFormat", Daten.backwardCompatibleVersion, importedFileFormat),
                    resourceMap.getString("importDglErrTitle"),
                    JOptionPane.PLAIN_MESSAGE);
            // leave thread
            return null;
        }
        // remove all entries with identical ID, because we can't have these entries twice
        // in the database. if the user wants to update entries (with same IDs), the synch feature
        // can be used.
        removeDoubleEntries(zkn3Doc);
        // get the length of the data
        final int len = zkn3Doc.getRootElement().getContentSize();
        // reset the progressbar
        setProgress(0, 0, len);
        msgLabel.setText(resourceMap.getString("importDlgMsgEdit"));
        //
        // at first, add the description of the new importet zettelkasten
        //
        // get the child element
        Element el = meta3Doc.getRootElement().getChild(Daten.ELEMEMT_DESCRIPTION);
        // if we have any element, add description
        if (el != null) {
            dataObj.addZknDescription(el.getText());
        }
        //
        // now, convert the old index-numbers of the authors and keywords
        // to the new numbers and add the entries to the existing data file
        //
        // go through all entries and prepare them and add them to the
        // main data file. especially the new author- and keyword-index-numbers
        // have to be prepared
        for (int cnt = 0; cnt < len; cnt++) {
            // get each child
            Element z = (Element) zkn3Doc.getRootElement().getContent(cnt);
            // we only need to convert the author- and keyword-index-numbers.
            // first we start with the author-index-numbers...
            // if the author-element is not empty...
            if (!z.getChild(Daten.ELEMENT_AUTHOR).getText().isEmpty()) {
                // ...get the autors indexnumbers
                String[] aun = z.getChild(Daten.ELEMENT_AUTHOR).getText().split(",");
                // create new stringbuilder that will contain the new index-numbers
                StringBuilder sb = new StringBuilder("");
                // iterate the array
                for (String aun1 : aun) {
                    // get the related author-element from the author-file.
                    // the needed author-index-number is stored as integer (string-value)
                    // in the author-indexnumbers-array "aun".
                    Element dummyauthor = (Element) author3Doc.getRootElement().getContent(Integer.parseInt(aun1) - 1);
                    // get the string value for that author
                    String authorstring = dummyauthor.getText();
                    // if we have any author, go on..
                    if (!authorstring.isEmpty()) {
                        // add author to the data file
                        // and store the position of the new added author in the
                        // variable authorPos
                        int authorPos = dataObj.addAuthor(authorstring, 1);
                        // store author position as string value
                        sb.append(String.valueOf(authorPos));
                        sb.append(",");
                    }
                }
                // truncate last comma
                if (sb.length() > 1) {
                    sb.setLength(sb.length() - 1);
                }
                // set new author-index-numbers
                z.getChild(Daten.ELEMENT_AUTHOR).setText(sb.toString());
            }
            // now that the authors are converted, we need to convert
            // the keyword-index-numbers
            // if the keyword-element is not empty...
            if (!z.getChild(Daten.ELEMENT_KEYWORD).getText().isEmpty()) {
                // ...get the keywords-index-numbers
                String[] kwn = z.getChild(Daten.ELEMENT_KEYWORD).getText().split(",");
                // create new stringbuilder that will contain the new index-numbers
                StringBuilder sb = new StringBuilder("");
                // iterate the array
                for (String kwn1 : kwn) {
                    // get the related keyword-element from the keyword-file.
                    // the needed keyword-index-number is stored as integer (string-value)
                    // in the keyword-indexnumbers-array "kwn".
                    Element dummykeyword = (Element) keyword3Doc.getRootElement().getContent(Integer.parseInt(kwn1) - 1);
                    // get the string value for that keyword
                    String keywordstring = dummykeyword.getText();
                    // if we have any keywords, go on..
                    if (!keywordstring.isEmpty()) {
                        // add it to the data file
                        // and store the position of the new added keyword in the
                        // variable keywordPos
                        int keywordPos = dataObj.addKeyword(keywordstring, 1);
                        // store author position as string value
                        sb.append(String.valueOf(keywordPos));
                        sb.append(",");
                    }
                }
                // truncate last comma
                if (sb.length() > 1) {
                    sb.setLength(sb.length() - 1);
                }
                // set new keyword-index-numbers
                z.getChild(Daten.ELEMENT_KEYWORD).setText(sb.toString());
            }
            // update progressbar
            setProgress(cnt, 0, len);
        }
        // now that all entries are converted, append the data to the existing file
        dataObj.appendZknData(zkn3Doc);
        // TODO append desktop-data
        // TODO append search-data                        
        // append bookmarks
        bookmarksObj.appendBookmarks(dataObj, bookmark3Doc);
        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().

        // after importing, the data file is modified, so the user does not
        // forget to save the data in the new fileformat.
        if (taskinfo.isImportOk()) {
            dataObj.setModified(true);
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

    /**
     *
     * @param zdoc
     */
    private void removeDoubleEntries(Document zdoc) {
        // set new import message, telling that data conversion is proceeded
        msgLabel.setText(resourceMap.getString("importDlgMsgRemoveDouble"));
        // create a list of all elements from the given xml file
        List<?> elementList = zdoc.getRootElement().getContent();
        // reset the progressbar
        setProgress(0, 0, elementList.size());
        // the outer loop for the imported data
        for (int cnt = 0; cnt < elementList.size(); cnt++) {
            // get element of imported data file
            Element importentry = (Element) elementList.get(cnt);
            // now add id to zettel-element
            String id = importentry.getAttributeValue(Daten.ATTRIBUTE_ZETTEL_ID);
            // check for valid value
            if (id != null && !id.isEmpty()) {
                // check whether Zettel with unique ID already exists
                // in the current database
                if (dataObj.findZettelFromID(id) != -1) {
                    // if yes, remove double entry from imported document
                    zdoc.getRootElement().getContent().remove(cnt);
                    // add number of removed entry to list. remember that
                    // the entry-number adds on to our counter, which starts
                    // at zero.
                }
            }
            setProgress(cnt, 0, elementList.size());
        }
    }
}
