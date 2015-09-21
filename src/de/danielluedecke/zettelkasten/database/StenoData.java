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

package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.util.Constants;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.IllegalDataException;

/**
 *
 * @author danielludecke
 */
public class StenoData {

    /**
     * XML document that holds the data for auto-correction. Each element "entry"
     * has an attribute named "id" that contains the wrong, mispelled word. the
     * entry's text is the correct writing of the word.
     */
    private Document steno;
    /**
     * XML document that is used as backup
     */
    private Document backupdoc = null;


    /**
     * This class stores the information about the automatic correction (spelling) when
     * the user makes input for new entries.
     */
    public StenoData() {
        clear();
    }


    /**
     * Clears the XML document and creates a dummy-backup of the document, in case the original
     * XML-document contains data.
     */
    public final void clear() {
        // check whether backup document exists, whether autokorrektur-document exists and whether
        // the autokorrektur-document has any data. only in this case we create a backup
        if (steno!=null && steno.getRootElement().getContentSize()>0) {
            // create new backup doc
            backupdoc = new Document(new Element("backup_steno"));
            // copy content
            backupdoc.getRootElement().addContent(steno.getRootElement().cloneContent());
        }
        steno = new Document(new Element("steno"));
    }
    /**
     * This method checks whether the XML document is ok or corrupted. in case there have been
     * jdom-errors when adding new elements, the XML document {@link #steno} might
     * be empty, while the backup-document {@link #backupdoc} has data. In this case,
     * {@code false} is returned. If the XML-document is ok, {@code true} is returned.
     * @return {@code true} if the main XML-document is ok, {@code false}if it might be corrupted.
     * <br><br>
     * You can use {@link #restoreDocument() restoreDocument()} to restore a corrupted document.
     */
    public boolean isDocumentOK() {
        // check whether we have any XML-document at all. proceed only, if we have no document
        // of if the XML-document does not contain data
        if ((null==steno) || (steno.getRootElement().getContentSize()<1)) {
            // now check whether we have a backup of the XML document, which has content
            if ((backupdoc!=null) && (backupdoc.getRootElement().getContentSize()>0)) {
                // if so, the backup contains data that the main document does not has
                // so, we assume the document is *not* ok
                return false;
            }
        }
        // else everything is fine
        return true;
    }
    /**
     * In case we have a corrupted XML document with a backup document that has data
     * (see {@link #isDocumentOK() isDocumentOK()}), we can restore the backupped data
     * with this method.<br><br>
     * So, this method copies back the content of the {@link #backupdoc} to the
     * original XML document {@link #steno}.
     */
    public void restoreDocument() {
        // check whether we have a backup document that also contains data
        if ((backupdoc!=null) && (backupdoc.getRootElement().getContentSize()>0)) {
            // if we have it, create new main XML document
            steno = new Document(new Element("steno"));
            // and copy the content of the backup document to it
            steno.getRootElement().addContent(backupdoc.getRootElement().cloneContent());
        }
    }


    /**
     * Sets the document, e.g. after loading the settings
     * @param d the document with the steno-data
     */
    public void setDocument(Document d) {
        steno = d;
    }
    /**
     * Gets the xml-document that contains the steno-data
     * @return the xml-document with the steno-data
     */
    public Document getDocument() {
        return steno;
    }


    /**
     * Returns the amount of elements
     * @return Returns the amount of elements as integer value
     */
    public int getCount() {
        return steno.getRootElement().getContentSize();
    }


    /**
     * Gets a String-pair of steno data, i.e. a string array with 2 fields. first
     * field contains the short steno word, the second field holds the complete
     * version of the word
     * @param nr the position of the element we want to retrieve
     * @return a string array containing the steno and the complete word
     */
    public String[] getElement(int nr) {
        // get all elements
        List<Element> all = steno.getRootElement().getChildren();
        // get the requested element
        Element el = all.get(nr);
        // new string array
        String[] retval = null;
        // if we found an element, return its content
        if (el!=null) {
            retval = new String[2];
            retval[0] = el.getAttributeValue("id");
            retval[1] = el.getText();
        }

        return retval;
    }


    /**
     * Adds a new pair of steno/long words to the document
     *
     * @param abbr the short, steno-abbreviation of the word
     * @param longword the long, original version of the word
     * @return {@code true} if element was successfully addes, false if {@code stenoword} already existed
     */
    public boolean addElement(String abbr, String longword) {
        // check for existence
        if (exists(abbr)) {
            return false;
        }
        // if it doesn't already exist, create new element
        Element e = new Element(Daten.ELEMENT_ENTRY);
        try {
            // set id-attribute
            e.setAttribute("id", abbr);
            // set content
            e.setText(longword);
            // and add it to the document
            steno.getRootElement().addContent(e);
        }
        catch (IllegalAddException | IllegalDataException ex) {
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
            return false;
        }
        // return success
        return true;
    }

    
    /**
     * This method checks whether text string {@code text} ends with one of the abbreviations
     * (steno words) in the steno data base. if any abbreviation was found at the end of
     * {@code text}, this abbreviation is returned, else {@code null} is returned.
     * 
     * @param text a text fragment, which end may contain an abbreviation. If {@code text} ends with
     * an abbreviation, this abbreviation is returned.
     * @return a string with an abbreviation, if {@code text} ends with this abbreviation. {@code null}
     * if {@code text} does not contain (end with) any abbreviation.
     */
    public String findAbbreviationFromText(String text) {
        // go through all existing steno abbreviations
        for (int cnt=0; cnt<getCount(); cnt++) {
            // retrieve abbreviation
            String abbr = getAbbreviation(cnt);
            // check whether the text segment ends with the
            // current abbreviation, if so, we found a valid steno
            if (text.endsWith(abbr)) {
                try {
                    // now check whether the case is correct (matchcase)
                    if (text.substring(text.length()-abbr.length()).equals(abbr)) {
                        return abbr;
                    }
                }
                catch (IndexOutOfBoundsException ex) {
                }
            }
        }
        return null;
    }
    
    
    /**
     * This method returns the length of the longest abbreviation on the steno list.
     * This is needed to check how many chars before the current caret position in the edit window
     * have to be checked for the steno abbreviation
     * 
     * @return the length of the longest abbreviation
     */
    public int retrieveLongestAbbrLength() {
        int longest = 0;
        for (int cnt=0; cnt<getCount(); cnt++) {
            String abbr = getAbbreviation(cnt);
            if (abbr.length()>longest) {
                longest = abbr.length();
            }
        }
        return longest;
    }
    
    /**
     * This method returns the abbreviation of the steno element with the index number {@code nr}.
     * 
     * @param nr the index-number of the abbreviation that should be returned.
     * @return the abbreviation in the XML document with the index {@code nr}
     */
    public String getAbbreviation(int nr) {
        String[] retval = getElement(nr);
        if (retval!=null) {
            return retval[0];
        }
        return null;
    }
    

    /**
     * 
     * @param abbr
     * @return 
     */
    public String getStenoWord(String abbr) {
        // get all elements
        List<Element> all = steno.getRootElement().getChildren();
        // create an iterator
        Iterator<Element> i = all.iterator();
        // go through list
        while (i.hasNext()) {
            // get element
            Element el = i.next();
            // get spell-check-word
            String correct = el.getAttributeValue("id");
            // check for existing value
            if (null==correct) {
                return null;
            }
            // if the element's id equals the requestes string "e", return true
            // i.e. the string e already exists as element
            if (correct.equals(abbr)) {
                return el.getText();
            }
        }
        return null;
    }


    /**
     * Checks whether the value passed by the parameter "e" already exists in the
     * data. Therefore, each element's id-attribute is compared to the parameter "e".
     * @param abbr the string we want to look for if it exists
     * @return {@code true} if we found it, false if it doesn't exist
     */
    public boolean exists(String abbr) {
        // get all elements
        List<Element> all = steno.getRootElement().getChildren();
        // create an iterator
        Iterator<Element> i = all.iterator();
        // go through list
        while (i.hasNext()) {
            // get element
            Element el = i.next();
            // get attribute
            String att = el.getAttributeValue("id");
            // if the element's id equals the requestes string "e", return true
            // i.e. the string e already exists as element
            if (att!=null && att.equals(abbr)) {
                return true;
            }
        }
        // nothing found...
        return false;

    }
}
