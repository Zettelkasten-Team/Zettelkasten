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
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.IllegalDataException;

// TODO beim Import alter Daten und bei default-timestamp auch sek. und millisek. setzen

/**
 *
 * @author Luedeke
 */
public class ImportFromZkn extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the Daten object, which contains the XML data of the Zettelkasten.
     * will be passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     * Reference to the Bookmarks object, which contains the XML data of the bookmarks.
     * will be passed as parameter in the constructor, see below
     */
    private final Bookmarks bookmarksObj;
    /**
     * Reference to the Settings object, which contains the settings like fike paths etc...
     */
    private final Settings settingsObj;
    /**
     * Reference to the DesktopData object, which contains the XML data of the desktop.
     * will be passed as parameter in the constructor, see below
     */
    private final DesktopData desktopObj;
    /**
     * SearchRequests object, which contains the XML data of the searchrequests and -result
     * that are related with this data file
     */
    private final SearchRequests searchrequestsObj;
    /**
     * dummy variable that stores the importet data in a XML file.
     * this data is after being importtet converted to the XML structure
     * of the Daten dataObj
     */
    private final Document dummyXML;
    /**
     * file path to import file
     */
    private File filepath;
    /**
     * indicates whether a conversion from ascii to unicode chars is necessary
     */
    private final boolean atou;
    /**
     * indicates which type of data format should be imported.
     * refer to the Zettelkasten.view properties file (resources) to see
     * which number is which file type.
     */
    private int importType;
    /**
     * indicates whether the data should be appended to an already opened zettelkasten
     * or whether the old zettelkasten-data-file should be closed (and saved) before and
     * a new data-file should be created from the imported data
     */
    private final boolean append;
    /**
     * A default timestamp for importing old datafiles. Sometimes entries of old data files may
     * not contain timestamps. so we can insert a default value here...
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
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
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
     * @param s
     * @param fp
     * @param a2u
     * @param appendit
     * @param dts 
     */
    public ImportFromZkn(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, Bookmarks bm, DesktopData dt, SearchRequests sr, Settings s,
            File fp, boolean a2u, boolean appendit, String dts) {
        super(app);
        // init of the variable either passed as parameters or initiated the first time
        dataObj = d;
        taskinfo = td;
        bookmarksObj = bm;
        desktopObj = dt;
        searchrequestsObj = sr;
        settingsObj = s;
        parentDialog = parent;
        msgLabel = label;

        filepath = fp;
        atou = a2u;
        append = appendit;
        defaulttimestamp = dts;

        if (null==defaulttimestamp) {
            defaulttimestamp = Tools.getTimeStamp();
        }
        taskinfo.setImportOk(true);
        // initiate the XML file
        dummyXML = new Document(new Element("zettelkasten"));
        // set default import message
        msgLabel.setText(resourceMap.getString("importDlgMsgImport"));
    }
    @Override protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.

        // init of the xml element variables here, since they are
        // being used more often
        Element zettelkasten;
        Element zettel;
        Element content;
        Element keywords;
        Element author;
        Element manlinks;
        Element remarks;
        Element timestamp;
        Element hyperlinks;
        Element title;
        Element luhmann;

        // get the filelength
        final long l = filepath.length();
        final long kbl = l / 1024;
        // this counter is used for indicating the position of the progressbar
        long counter = 0;
        // this variable stores the amount of entries before the import starts. this
        // is needed when appending data and correcting links like bookmarks etc.
        int oldcount = dataObj.getCount(Daten.ZKNCOUNT);
        // init the stringbuffer
        StringBuilder buffer = new StringBuilder("");
        // init the input stream.
        InputStream is;
        // and the input streamreader.
        InputStreamReader ips = null;
        // First of all read the file. The way how the file is
        // imported depends on the filetype. the switch command
        // is used to choose the right import routine
        //
        // here begins the import of old zettelkasten data (.zkn)
        //
        //
        // what we do here is importing an ascii file of old zettelkasten data
        // an adding the imported content to a dummy xml file/document. the old zettelkasten
        // data used simple ascii strings which were separated by zeros. each zero therefor
        // indicates the beginning of a new element (e.g. content, keywords, authors, title...)
        //
        // the header of the old datafile is skipped (see below), while the data is imported and
        // assigned to the right element via switch-command. we have an index counter which tells
        // the switch command which element of the datafile has been imported recently.
        //
        // init variables
        Document zkndoc;

        // TODO entfernen von Dubletten nur bei zkn3-Dateien, nicht bei alten ZKN-Dateien?

        // try to open the file
        try {
            is = new FileInputStream(filepath);
        } catch (FileNotFoundException fileNotFoundException) {
            // display error message box
            JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgFileNotFound", filepath), resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
            // leave thread
            return null;
        }
        try {
            // when the input file is in ascii format, tell the input
            // stream to convert it into unicode
            if (atou) {
                ips = new InputStreamReader(is, "cp1252");
            } // else use default encoding
            else {
                ips = new InputStreamReader(is);
            }
        } catch (UnsupportedEncodingException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
        }
        // used for converting the imported, old zettelkasten data into the
        // new xml structure
        int authorPos;
        int keywordPos;
        String dummyString;
        // if we don't want to append the data, reset the zettelkastem
        if (!append) {
            resetDataFiles();
        }
        if (ips != null) {
            try {
            // needed for skipping some information of the old file format which should
                // not be imported
                boolean skip = false;
                buffer.setLength(0);
                // the read bytes are stored in this variable
                int c;
            // every data part of the old zettelkasten data
                // is separated by a 0. the first part is the
                // file version. if this value is not "2.6", we have the data
                // in a too old fileformat, which cannot be importet
                while (!skip && (c = ips.read()) != -1) {
                // a zero indicates a new part/entry/string of the old
                    // zettelkasten datafile which is imported. so each time we
                    // find a zero, a new part of the data structure begins
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                        buffer.append(chr);
                    } // leave the loop
                    else {
                        skip = true;
                    }
                    counter++;
                // when we have a very small data-file, don't count kilobytes,
                    // but only bytes...
                    if (l > 2048) {
                        setProgress(counter / 1024, 0, kbl);
                    } else {
                        setProgress(counter, 0, l);
                    }
                }
            // now we have to check, whether the imported data has the
                // correct format, indicated by the version number "2.6"
                // if not, show error message and leave thread
                if (!buffer.toString().contains("2.6")) {
                    // log error-message
                    Constants.zknlogger.log(Level.WARNING, "Failed when importing older version of Zettelkasten-data. Data-format was older than Version 2.6!");
                    // display error message box
                    JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgErrOldZknData"), resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
                    // return value that indicates that an error occured
                    taskinfo.setImportOk(false);
                    // leave thread
                    return null;
                }
                // reset skip-value
                skip = false;
                buffer.setLength(0);
            // now comes a part with the filedescription of the old
                // zettelkasten file. this information is needed and should be
                // saved in the metainformation file.
                while (!skip && (c = ips.read()) != -1) {
                    // as long as the delimiter-zero is not reached, read the bytes
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                        if (Tools.isLegalJDOMChar(c)) {
                            buffer.append(chr);
                        }
                    } // otherweise, transfer the buffer to the metainformation-xml-file
                    // and leave the loop
                    else {
                        try {
                            if (!append) {
                                dataObj.setZknDescription(buffer.toString());
                            } else {
                                dataObj.addZknDescription(buffer.toString());
                            }
                            skip = true;
                        } // in case we have an illegal add exception...
                        catch (IllegalAddException | IllegalDataException e) {
                            // display errormessage
                            showErrorLogMsg(e.getLocalizedMessage());
                            // reset data files
                            resetDataFiles();
                            // leave task
                            return null;
                        }
                        // ...or an illegal data exception, show error log and leave thread here

                    }
                    counter++;
                // when we have a very small data-file, don't count kilobytes,
                    // but only bytes...
                    if (l > 2048) {
                        setProgress(counter / 1024, 0, kbl);
                    } else {
                        setProgress(counter, 0, l);
                    }
                }
                // reset skip-value
                skip = false;
            // and read the rest of the not needed information. we now
                // have the needed filedescription and saved it in the
                // metainformation file
                while (!skip && (c = ips.read()) != -1) {
                // a zero indicates a new part/entry/string of the old
                    // zettelkasten datafile which is imported. so each time we
                    // find a zero, a new part of the data structure begins
                    if (0 == c) {
                        skip = true;
                    }
                    counter++;
                // when we have a very small data-file, don't count kilobytes,
                    // but only bytes...
                    if (l > 2048) {
                        setProgress(counter / 1024, 0, kbl);
                    } else {
                        setProgress(counter, 0, l);
                    }
                }

            // reset the string buffer so it contains only
                // new read data
                buffer.setLength(0);
            // this is an indicator which "counts" the separater-zeros,
                // which means that this variable indicates which part of an
                // entry is currently read (i.e. maintext, author, keywords...)
                int dataIndicator = 0;
            // get the root element of the temporary XML structure
                // this is used below to create new child elements of each entry
                zettelkasten = dummyXML.getRootElement();
                zettel = null;
                // read the file byte per byte
                while ((c = ips.read()) != -1) {
                // as long as the read char is no "separater" (zero),
                    // it is appended to the string buffer.
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                    // if char is a return character (new line), add a html-br-tag to
                        // the string buffer
                        if (13 == c) {
                            buffer.append("[br]");
                        } // in windows-ascii, each new line command consists of two bytes:
                        // 13 and 10. If a 13 was found, the new line tag (<br>) is already
                        // set, so we skip the second bye here.
                        else if (10 == c) {
                        } // else append the char to the buffer
                        else if (Tools.isLegalJDOMChar(c)) {
                            buffer.append(chr);
                        }

                    } else {
                        try {
                        // every time when a new "zettel" begins, create the
                            // related child element in the XML structure
                            if (0 == dataIndicator) {
                                zettel = new Element("zettel");
                                zettelkasten.addContent(zettel);
                            }
                            // check for null reference
                            if (null == zettel) {
                                zettel = new Element("zettel");
                                zettelkasten.addContent(zettel);
                            }
                        // if the char is a zero, it marks the end of a part
                            // of an zettelkasten entry (e.g. maintext (conten), author, keyword, etc.
                            // which are all separated by zeros)
                            // now we have to create a new XML element to copy the content
                            // of the buffer to the temporary XML structure
                            switch (dataIndicator) {
                            // the content of the string buffer is the MAINTEXT (CONTENT)
                                // of an entry. copy string buffer to related XML child element
                                case 0:
                                    content = new Element("content");
                                    zettel.addContent(content);
                                     // we have to update the list-format-elements from the
                                    // old format. while in the old format a list was just surrounded
                                    // by [l]-tags and each line was a new bullet point, we
                                    // now surround <li>-elements arround each line. So from
                                    // now on, a bullet point may contain several lines.
                                    content.addContent(replaceListElements(buffer.toString()));
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. keyword infos
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer are the KEYWORDS
                                // of an entry. copy string buffer to related XML child element
                                case 1:
                                    keywords = new Element(Daten.ELEMENT_KEYWORD);
                                    zettel.addContent(keywords);
                                    keywords.addContent(buffer.toString().trim());
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. author infos
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer are the AUTHOR INFOS
                                // of an entry. copy string buffer to related XML child element
                                case 2:
                                    author = new Element(Daten.ELEMENT_AUTHOR);
                                    zettel.addContent(author);
                                    author.addContent(buffer.toString().trim());
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. RELATIONS/LINKS infos
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer are the RELATIONS/LINK INFOS
                                // of an entry. These are NOT NEEDED, so skip them
                                case 3:  // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. OTHER REMARKS infos
                                    dataIndicator++;
                                    // reset buffer
                                    buffer.setLength(0);
                                    break;
                            // the content of the string buffer are the OTHER REMARKS
                                // of an entry. copy string buffer to related XML child element
                                case 4:
                                    remarks = new Element("remarks");
                                    zettel.addContent(remarks);
                                    remarks.addContent(buffer.toString());
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. TIMESTAMP infos
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer is the TIME STAMP
                                // of an entry. copy string buffer to related XML child element
                                case 5:
                                    timestamp = new Element("timestamp");
                                    zettel.addContent(timestamp);
                                    timestamp.addContent(buffer.toString());
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. HYPERLINKS
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer is the entry's HYPERLINKS
                                // of an entry. copy string buffer to related XML child element
                                case 6:
                                    hyperlinks = new Element("hyperlinks");
                                    zettel.addContent(hyperlinks);
                                    hyperlinks.addContent(buffer.toString());
                                     // increase dataIndicator, so next time buffer content
                                    // is regarded as the next element, i.e. TITLE
                                    dataIndicator++;
                                    break;
                            // the content of the string buffer is the entry's TITLE
                                // of an entry. copy string buffer to related XML child element
                                case 7:
                                    title = new Element("title");
                                    zettel.addContent(title);
                                    title.addContent(buffer.toString().trim());
                                    // RESET the dataIndicator, because now starts the next entry
                                    dataIndicator = 0;
                                    break;
                            }
                            // reset buffer
                            buffer.setLength(0);
                        } // in case we have an illegal-add-exception...
                        catch (IllegalAddException | IllegalDataException e) {
                            // display errormessage
                            showErrorLogMsg(e.getLocalizedMessage());
                            // reset data files
                            resetDataFiles();
                            // leave task
                            return null;
                        }
                        // ...or an illegal-data-exception, show error-log and leave thread

                    }
                    // increase the counter for the progress bar
                    counter++;
                // when we have a very small data-file, don't count kilobytes,
                    // but only bytes...
                    if (l > 2048) {
                        setProgress(counter / 1024, 0, kbl);
                    } else {
                        setProgress(counter, 0, l);
                    }
                }
                /*
                 * Now that we have imported the data into a xml document, we have to
                 * transfer this document into the CData class, which stores the original
                 * zettelkasten data.
                 *
                 * We have to do some conversions here. First of all, the keywords have to be
                 * extracted and the keyword datafile (see "Document keywordFile" in "CData.java")
                 * has to be created with these extracted keywords. The keyword strings in this
                 * dummy xml structure are then replaced by the index numbers (i.e. element-position)
                 * of the related keywords in the keywordFile.
                 *
                 * After that, the same procedure has to be applied to the author elements.
                 *
                 * Finally, the timestamp has to be converted.
                 */
                // get a list with all entry-elements of the importet data
                List<?> importetList = dummyXML.getRootElement().getContent();
                // and an iterator for the loop below
                Iterator<?> iterator = importetList.iterator();
            // get the size of the xml structure which was created from the importet
                // file, i.e. get the amount of entries to be converted
                int ifl = importetList.size();
                // reset the progressbar
                setProgress(0, 0, ifl);
                counter = 0;
                // set new import message, telling that data conversion is proceeded
                msgLabel.setText(resourceMap.getString("importDlgMsgConvert"));
            // create a new dummy document, where we store the converted data
                // afterwards we simply copy this document into the dataObj (Daten)
                // via "setZknData()"
                zkndoc = new Document(new Element(Daten.DOCUMENT_ZETTELKASTEN));
                zettelkasten = zkndoc.getRootElement();
            // iteration of the dummyXML file, i.e. going through every importet
                // entry and transfer it to the "Daten" class object.
                while (iterator.hasNext()) {
                    // get each entry-element
                    Element entry = (Element) iterator.next();
                    // create a new element "zettel"
                    zettel = new Element(Daten.ELEMENT_ZETTEL);
                    // and add it to the document
                    zettelkasten.addContent(zettel);
                    // add unique ID
                    zettel.setAttribute(Daten.ATTRIBUTE_ZETTEL_ID, Tools.createZknID(settingsObj.getFileName()) + String.valueOf(counter));
                // now we have to add the contents (content, authors, title, remark etc.)
                    // of this entry (zettel) to each sub element of a zettel
                    //
                    // first of all, the title
                    //
                    title = new Element(Daten.ELEMENT_TITLE);
                    zettel.addContent(title);
                    title.setText(entry.getChild(Daten.ELEMENT_TITLE).getText());
                //
                    // now comes the content
                    //
                    content = new Element(Daten.ELEMENT_CONTENT);
                    zettel.addContent(content);
                    content.setText(entry.getChild(Daten.ELEMENT_CONTENT).getText());
                //
                    // now comes the author
                    //
                    // extract the author
                    // first, get the author string of the imported data
                    dummyString = entry.getChild(Daten.ELEMENT_AUTHOR).getText();
                // create empty string buffer which stores the index numbers
                    // of the converted authors
                    StringBuilder newau = new StringBuilder("");
                    // proceed only, if authors exist
                    if (!dummyString.isEmpty()) {
                    // split author-values at line separator, in case we have several authors
                        // in one entry...
                        String[] authorparts = dummyString.split("\\[br\\]");
                        // iterate array
                        for (String ap : authorparts) {
                            // trim leading and trailing spaces
                            ap = ap.trim();
                            // check whether we have any author at all...
                            if (!ap.isEmpty()) {
                            // add it to the data file
                                // and store the position of the new added author in the
                                // variable authorPos
                                authorPos = dataObj.addAuthor(ap, 1);
                                // append author index number
                                newau.append(String.valueOf(authorPos));
                            // separator for the the index numbers, since more authors
                                // and thus more index numbers might be stored in the author element
                                newau.append(",");
                            }
                        }
                    // shorten the stringbuffer by one char, since we have a
                        // superfluous comma char (see for-loop above)
                        if (newau.length() > 1) {
                            newau.setLength(newau.length() - 1);
                        }
                    }
                    // create author element
                    author = new Element(Daten.ELEMENT_AUTHOR);
                    zettel.addContent(author);
                    // store author position as string value
                    author.setText(newau.toString());
                //
                    // now come the keywords
                    //
                    dummyString = entry.getChild(Daten.ELEMENT_KEYWORD).getText();
                // create empty string buffer which stores the index numbers
                    // of the converted keywords
                    StringBuilder newkw = new StringBuilder("");
                    // proceed only, if keywords exist
                    if (!dummyString.isEmpty()) {
                    // create a regular expression, that separates the keyword string at each comma.
                        // furthermore, commas within double-quotes ("") are not treated as separator-char,
                        // so the user can search for sentences that include commas as well. and finally, the
                        // quotes are removed, since we don't need them...
                        Matcher mat = Pattern.compile("(\"(.*?)\"|([^,]+)),?").matcher(dummyString);
                        // create a new list that will contain each found pattern (i.e. searchterm)
                        List<String> result = new ArrayList<>();
                        while (mat.find()) {
                            result.add(mat.group(2) == null ? mat.group(3) : mat.group(2));
                        }
                        // and copy the list to our array...
                        String[] kws = result.toArray(new String[result.size()]);
                    // convert each keyword string
                        // therefor, iterate the array
                        for (String kw : kws) {
                            // trim leading and trailing spaces
                            kw = kw.trim();
                            // only copy keyword, if we have one...
                            if (!kw.isEmpty()) {
                            // add it to the data file
                                // and store the position of the new added keyword in the
                                // variable keywordPos
                                keywordPos = dataObj.addKeyword(kw, 1);
                                // append the index number in the string buffer
                                newkw.append(String.valueOf(keywordPos));
                            // separator for the the index numbers, since more keywords
                                // and thus more index numbers might be stored in the keyword element
                                newkw.append(",");
                            }
                        }
                    // shorten the stringbuffer by one char, since we have a
                        // superfluous comma char (see for-loop above)
                        if (newkw.length() > 1) {
                            newkw.setLength(newkw.length() - 1);
                        }
                    }
                    // create keyword element
                    keywords = new Element(Daten.ELEMENT_KEYWORD);
                    zettel.addContent(keywords);
                    // store keyword index numbers
                    keywords.setText(newkw.toString());
                //
                    // now comes the manual links to other entries
                    //
                    manlinks = new Element(Daten.ELEMENT_MANLINKS);
                    zettel.addContent(manlinks);
                    manlinks.setText("");
                //
                    // now comes the hyperlinks
                    //
                    hyperlinks = new Element(Daten.ELEMENT_ATTACHMENTS);
                    zettel.addContent(hyperlinks);
                // the hyperlinks in the old data format are separated
                    // by ";", so parse them into an array and add a new
                    // sub element for each hyperlink entry
                    dummyString = entry.getChild("hyperlinks").getText();
                    // only add children, if we have text...
                    if (!dummyString.isEmpty()) {
                    // when the operating-system is *not* windows, convert
                        // backslashes to slashes
                        if (!PlatformUtil.isWindows()) {
                            dummyString = dummyString.replace("\\", System.getProperty("file.separator"));
                        }
                        // parse the single hyperlinks into a string array
                        String[] hls = dummyString.split(";");
                    // add each hyperlink string
                        // therefor, iterate the array
                        for (String hl : hls) {
                            Element sublink = new Element(Daten.ELEMENT_ATTCHILD);
                            sublink.setText(hl);
                            hyperlinks.addContent(sublink);
                        }
                    }
                //
                    // now comes the remarks
                    //
                    remarks = new Element(Daten.ELEMENT_REMARKS);
                    zettel.addContent(remarks);
                    remarks.setText(entry.getChild("remarks").getText().trim());
                //
                    // now comes the timestamp
                    //
                    // init the dummy variables
                    String tsDay;
                    String tsMonth = "";
                    String tsYear;
                    String tsHour;
                    String tsMinute;
                    // first get the old timestamp-string
                    String ts = entry.getChild("timestamp").getText();
                    // if no value exists, clear timestamp
                    if (null == ts) {
                        ts = "";
                    }
                    // when we have an old value, convert it...
                    if (!ts.isEmpty()) {
                    // we might have two parts, the created and the edited value, divided by ";"
                        // the format is as following:
                        // "Erstellt am: Sonntag, den 03. November 2008, um 07:28 Uhr;"
                        String[] tsParts = ts.split(";");
                        // go through array
                        for (int tscount = 0; tscount < tsParts.length; tscount++) {
                            // now look for the occurence of the day
                            int start = tsParts[tscount].indexOf(", den ");
                            // if it was found, proceed
                            if (start != -1) {
                                try {
                                // and copy the two digits after that occurence to the day-string
                                    // 6 positions after the occurence of ", den " starts the day in the string
                                    // and it's 2 chars long, so we +6 and +8 here.
                                    tsDay = tsParts[tscount].substring(start + 6, start + 8);
                                // now look for the next space after the month-string
                                    // after +8 comes a space sign, and after that starts the month. so we look
                                    // for the first space after "+10"
                                    int end = tsParts[tscount].indexOf(" ", start + 10);
                                    // and compare the month and copy it
                                    if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("januar")) {
                                        tsMonth = "01";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("februar")) {
                                        tsMonth = "02";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("märz")) {
                                        tsMonth = "03";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("april")) {
                                        tsMonth = "04";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("mai")) {
                                        tsMonth = "05";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("juni")) {
                                        tsMonth = "06";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("juli")) {
                                        tsMonth = "07";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("august")) {
                                        tsMonth = "08";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("september")) {
                                        tsMonth = "09";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("oktober")) {
                                        tsMonth = "10";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("november")) {
                                        tsMonth = "11";
                                    } else if (tsParts[tscount].substring(start + 10, end).equalsIgnoreCase("dezember")) {
                                        tsMonth = "12";
                                    }
                                // now check out the year
                                    // exactly 3 chars after the end-value we shoukd have the lower two digits of
                                    // the year, i.e. "07" for "2007" and so on
                                    tsYear = tsParts[tscount].substring(end + 3, end + 5);
                                    // now look for the occurence of the time
                                    start = tsParts[tscount].indexOf(", um ");
                                    // 5 positions after that we have the hour, as two-digit-value
                                    tsHour = tsParts[tscount].substring(start + 5, start + 7);
                                    // 3 positions after the hour (2 digits hour, one ":") we find the minutes
                                    tsMinute = tsParts[tscount].substring(start + 8, start + 10);
                                // so now we should have the complete created- or editedt-timestamp
                                    // and set the string for the created-subchild
                                    if (0 == tscount) {
                                        dataObj.setTimestampCreated(zettel, tsYear + tsMonth + tsDay + tsHour + tsMinute);
                                    } else {
                                        dataObj.setTimestampEdited(zettel, tsYear + tsMonth + tsDay + tsHour + tsMinute);
                                    }
                                } catch (IndexOutOfBoundsException ex) {
                                // set creation and modification timestamp, where the
                                    // creation date is a default timestamp and no edit timestamp
                                    // is used
                                    dataObj.setTimestamp(zettel, defaulttimestamp, "");
                                }
                            } else {
                            // set creation and modification timestamp, where the
                                // creation date is a default timestamp and no edit timestamp
                                // is used
                                dataObj.setTimestamp(zettel, defaulttimestamp, "");
                            }
                        }
                    } else {
                    // set creation and modification timestamp, where the
                        // creation date is a default timestamp and no edit timestamp
                        // is used
                        dataObj.setTimestamp(zettel, defaulttimestamp, "");
                    }
                //
                    // now comes the luhmann number
                    //
                    luhmann = new Element(Daten.ELEMENT_TRAILS);
                    zettel.addContent(luhmann);
                    luhmann.setText("");
                    // update the progressbar
                    counter++;
                    setProgress(counter, 0, ifl);
                }
                // now that all data is converted into the variable zkndoc...
                if (!append) {
                    // set this document as main zkn data...
                    dataObj.setZknData(zkndoc);
                } // resp. append the zkndoc to the maindata
                else {
                    // append imported entries.
                    dataObj.appendZknData(zkndoc);
                }
                // we're done! :-)
            } catch (IOException ex) {
                // tell user that opening/importing the source file failed
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                // display error message box
                JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgErrCorruptedFile"), resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
                // return value that indicates that an error occured
                taskinfo.setImportOk(false);
                // if import of main data failed, leave
            } finally {
                try {
                    ips.close();
                } catch (IOException ex) {
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        }

        //
        //
        // here begins the import of old bookmark data (.zkl)
        //
        //
        /*
         *
         * !!! IMPORT OF BOOKMARKS STARTS HERE !!!
         *
         * now where we have finished importing the main data
         * we should go on with importing the bookmarks
         *
         */
        // TODO Fehler beim Import? Ein Lesezeichen hatte keine Kategorie,
        // hier Standardkategorie setzen...
        // change file extension
        filepath = new File(FileOperationsUtil.setFileExtension(filepath, ".zkl"));
        // init the stringbuffer
        buffer = new StringBuilder("");
        try {
            is = new FileInputStream(filepath);
            // when the input file is in ascii format, tell the input
            // stream to convert it into unicode
            try {
                if (atou) {
                    ips = new InputStreamReader(is, "cp1252");
                } else {
                    ips = new InputStreamReader(is);
                }
            } catch (UnsupportedEncodingException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                ips = new InputStreamReader(is);
            }

            try {
                // needed for skipping some information of the old file format which should
                // not be imported
                boolean skip = false;
                buffer.setLength(0);
                // the read bytes are stored in this variable
                int c;
                // every data part of the old zettelkasten data
                // is separated by a 0. the first part is the
                // file version. if this value is not "2.6", we have the data
                // in a too old fileformat, which cannot be importet
                while (!skip && (c = ips.read()) != -1) {
                    // a zero indicates a new part/entry/string of the old
                    // zettelkasten datafile which is imported. so each time we
                    // find a zero, a new part of the data structure begins
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                        buffer.append(chr);
                    } // leave the loop
                    else {
                        skip = true;
                    }
                }
                // now we have to check, whether the imported data has the
                // correct format, indicated by the version number "2.7"
                // if not, show error message and leave thread
                if (!buffer.toString().contains("2.7")) {
                    // display error message box
                    JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgErrOldBookmarkData"), resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
                    // leave thread
                    return null;
                }
                // reset skip-value
                skip = false;
                // reset buffer-value
                buffer.setLength(0);
                // now comes a part with the categories of the bookmarks. they
                // are saved as one single string, separated by commas. so we have
                // to split the string at each comma after reading
                while (!skip && (c = ips.read()) != -1) {
                    // as long as the delimiter-zero is not reached, read the bytes
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                        buffer.append(chr);
                    } // otherweise, transfer the buffer to the metainformation-xml-file
                    // and leave the loop
                    else {
                        skip = true;
                    }
                }
                // parse the categories to a string array
                // we will have to use this array later, when adding the bookmarks
                String[] catarray = buffer.toString().split(",");
                // reset skip-value
                skip = false;
                // reset buffer-value
                buffer.setLength(0);
                // now comes a part with the bookmarks. they
                // are saved as one single string, separated by commas. so we have
                // to split the string at each comma after reading. the first part
                // contains the entry-number, the second the reference to the category.
                // we retrieve the category-label from the above read array "catarray"
                while (!skip && (c = ips.read()) != -1) {
                    // as long as the delimiter-zero is not reached, read the bytes
                    if (c != 0) {
                        // convert the integer value into a char value
                        char chr = (char) c;
                        if (Tools.isLegalJDOMChar(c)) {
                            buffer.append(chr);
                        }
                    } // otherwise leave the loop
                    else {
                        skip = true;
                    }
                }
                // parse the buffer to an string-array, which then contains the bookmarks
                // and category-index-numbers
                String[] bmarray = buffer.toString().split(",");
                // first check, whether any categories are used. if not, add
                // a default-category
                if (catarray.length < 1) {
                    bookmarksObj.addCategory(resourceMap.getString("defaultBookmarkCategory"));
                }
                // init the catstring, used below
                String catstring;
                // now go through all importet bookmarks and add them to the
                // bookmark-class. increase loop-counter by 2, since we have to read
                // two following array-entries: X=number, X+1=category-index
                for (int cnt = 0; cnt < bmarray.length; cnt += 2) {
                    // get the entry-number which is bookmarked
                    int bmindex = Integer.parseInt(bmarray[cnt]);
                    // get the category's index-number
                    int catindex = Integer.parseInt(bmarray[cnt + 1]);
                    // if the category-index-number is out of bounds set default category
                    if (catindex < 0 || catindex >= catarray.length) {
                        catstring = resourceMap.getString("defaultBookmarkCategory");
                    } // else retrieve the category-name from the catarray
                    else {
                        catstring = catarray[catindex];
                    }
                    // if the data was appended, add amount of old entries to the
                    // bookmark-number, since the new entry-numbers are "shifted"...
                    if (append) {
                        bmindex = bmindex + oldcount;
                    }
                    // now add the data to the bookmark-class. we have no comments yet,
                    // so we will pass an empty string for that parameter
                    bookmarksObj.addBookmark(bmindex, catstring, "");
                }
                // actually, we should have done everything by now. :-)
            } catch (IOException ex) {
                // tell user that opening/importing the bookmak-file failed
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                // display error message box
                JOptionPane.showMessageDialog(null, resourceMap.getString("importDlgErrOldBookmarkData"), resourceMap.getString("importDglErrTitle"), JOptionPane.PLAIN_MESSAGE);
            } finally {
                try {
                    ips.close();
                } catch (IOException ex) {
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        } catch (FileNotFoundException fileNotFoundException) {
            // if no bookmark file was found, simply go on
        }
        return null;  // return your result
    }

    @Override protected void succeeded(Object result) {
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


    private void showErrorLogMsg(String err) {
        // log error-message
        Constants.zknlogger.log(Level.SEVERE,err);
        // get entry number, where the import possibly was at the moment
        String nr = String.valueOf(dummyXML.getRootElement().getContentSize());
        // display error message box, telling the user, whether there might have been
        // a problematic / defect entry
        JOptionPane.showMessageDialog(null,resourceMap.getString("importDlgIllegalChar",nr),resourceMap.getString("importDglErrTitle"),JOptionPane.PLAIN_MESSAGE);
        // return value that indicates that an error occured
        taskinfo.setImportOk(false);
    }


    private void resetDataFiles() {
        // reset the data-files
        if (!append) {
            settingsObj.setFilePath(new File(""));
            dataObj.initZettelkasten();
            desktopObj.clear();
            bookmarksObj.clear();
            searchrequestsObj.clear();
        }
    }

    /**
     * This method looks for list-format-elements and enhances them to fit to the
     * new data format. each single line is parsed into a string and surrounded by <li> elements.
     * while in the old format a list was just surrounded by [l]-tags and each line was a
     * new bullet point, we now surround <li>-elements arround each line. So from
     * now on, a bullet point may contain several lines.
     *
     * @param s (the content of an entry)
     * @return the fixed content
     */
    private String replaceListElements(String s) {
        // first, convert old tags to new tags
        s = s.replace("<k>", Constants.FORMAT_ITALIC_OPEN)
                .replace("</k>", Constants.FORMAT_ITALIC_CLOSE)
                .replace("<f>", Constants.FORMAT_BOLD_OPEN)
                .replace("</f>", Constants.FORMAT_BOLD_CLOSE)
                .replace("<u>", Constants.FORMAT_UNDERLINE_OPEN)
                .replace("</u>", Constants.FORMAT_UNDERLINE_CLOSE)
                .replace("<d>", Constants.FORMAT_STRIKE_OPEN)
                .replace("</d>", Constants.FORMAT_STRIKE_CLOSE)
                .replace("<c>", Constants.FORMAT_ALIGNCENTER_OPEN)
                .replace("</c>", Constants.FORMAT_ALIGNCENTER_CLOSE)
                .replace("<l>", Constants.FORMAT_LIST_OPEN)
                .replace("</l>", Constants.FORMAT_LIST_CLOSE);
        // color formatting: [color #rrggbb] becomes <span style="color:#rrggbb"> ([^\\[]*)
        s = s.replaceAll("\\<color ([^\\<]*)\\>", "\\[color $1\\]");
        s = s.replace("</color>", "[/color]");
        // margins formatting: [m 0.5] becomes <span style="margin-left:0.5cm;margin-right:0.5cm">
        s = s.replaceAll("\\<m ([^\\<]*)\\>", "\\[m $1\\]");
        s = s.replace("</m>", "[/m]");
        try {
            if (!s.contains(Constants.FORMAT_LIST_OPEN)) {
                return s;
            }
        } catch (NullPointerException e) {
            return "";
        }
        // init some variables here.
        // the pos-value indicates the position from where we should start to extract from our string
        int pos = 0;
        // these two variables store the position of the beginning and end of an [l] and [/l] elements
        int l_open = 0;
        int l_close;
        // some dummy string variables
        String dummy;
        String[] listparts;
        // some dummy string variables
        StringBuilder insert = new StringBuilder("");
        StringBuilder retval = new StringBuilder("");
        // as long as we have found
        while (l_open != -1) {
            // find the next occurence of an opening-l-tag
            l_open = s.indexOf(Constants.FORMAT_LIST_OPEN, pos);
            // check whether we have found a list-tag
            if (l_open != -1) {
                // find the next occurence of an closing-l-tag after the opened l-tag
                l_close = s.indexOf(Constants.FORMAT_LIST_CLOSE, l_open);
                // if closing-tag does not exist, find new line.
                if (-1 == l_close) {
                    l_close = s.indexOf("[br]", l_open);
                }
                // if we found something, go on...
                if (l_close != -1) {
                    // copy the string between the l-tags
                    dummy = s.substring(l_open + Constants.FORMAT_LIST_OPEN.length(), l_close);
                    // more than one bullet points were formerly separated by new lines
                    // so now we split the whole string at every new line
                    listparts = dummy.split("\\[br\\]");
                    // reset the dummy insert stringbuffer, otherwise we duplicate the content
                    // from the previous loop
                    insert.setLength(0);
                    // iterate the array of lines
                    for (String lines : listparts) {
                        // surround each line with the new bullet-tag
                        insert.append(Constants.FORMAT_LISTITEM_OPEN).append(lines).append(Constants.FORMAT_LISTITEM_CLOSE);
                    }
                    // now, copy a substring from the last position we were to the found l-open-ag
                    retval.append(s.substring(pos, l_open + Constants.FORMAT_LIST_OPEN.length()));
                    // then, insert the new lines surrounded by the bullet-tag "[lp]"
                    retval.append(insert.toString());
                    // and add the close tag
                    retval.append(Constants.FORMAT_LIST_CLOSE);
                    // tell our position indicator, that we are now behind the last closed l-tag
                    // next turn, we start looking for a l-sequence at the index "pos"
                    pos = l_close + 4;
                } // else remove tags, since user did not properly use
                // them and return the original content, with list-tags removed
                else {
                    s = s.replace(Constants.FORMAT_LIST_OPEN, "");
                    s = s.replace(Constants.FORMAT_LIST_CLOSE, "");
                    return s;
                }
            } else {
                // finally, append the rest of the content
                retval.append(s.substring(pos));
            }
        }

        return retval.toString();
    }
}
