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
public class AutoKorrektur {

    /**
     * XML document that holds the data for auto-correction. Each element "entry"
     * has an attribute named "id" that contains the wrong, mispelled word. the
     * entry's text is the correct writing of the word.
     */
    private Document autokorrektur;
    /**
     * XML document that is used as backup
     */
    private Document backupdoc = null;
    
    
    /**
     * This class stores the information about the automatic correction (spelling) when
     * the user makes input for new entries.
     */
    public AutoKorrektur() {
        clear();
    }


    /**
     * Clears the XML document and creates a dummy-backup of the document, in case the original
     * XML-document contains data.
     */
    public final void clear() {
        // check whether backup document exists, whether autokorrektur-document exists and whether
        // the autokorrektur-document has any data. only in this case we create a backup
        if (autokorrektur!=null && autokorrektur.getRootElement().getContentSize()>0) {
            // create new backup doc
            backupdoc = new Document(new Element("backup_autokorrektur"));
            // copy content
            backupdoc.getRootElement().addContent(autokorrektur.getRootElement().cloneContent());
        }
        autokorrektur = new Document(new Element("autokorrektur"));
    }
    /**
     * This method checks whether the XML document is ok or corrupted. in case there have been
     * jdom-errors when adding new elements, the XML document {@link #autokorrektur} might
     * be empty, while the backup-document {@link #backupdoc} has data. In this case,
     * {@code false} is returned. If the XML-document is ok, {@code true} is returned.
     * @return {@code true} if the main XML-document is ok, {@code false}if it might be corrupted.
     * <br><br>
     * You can use {@link #restoreDocument() restoreDocument()} to restore a corrupted document.
     */
    public boolean isDocumentOK() {
        // check whether we have any XML-document at all. proceed only, if we have no document
        // of if the XML-document does not contain data
        if ((null==autokorrektur) || (autokorrektur.getRootElement().getContentSize()<1)) {
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
     * original XML document {@link #autokorrektur}.
     */
    public void restoreDocument() {
        // check whether we have a backup document that also contains data
        if ((backupdoc!=null) && (backupdoc.getRootElement().getContentSize()>0)) {
            // if we have it, create new main XML document
            autokorrektur = new Document(new Element("autokorrektur"));
            // and copy the content of the backup document to it
            autokorrektur.getRootElement().addContent(backupdoc.getRootElement().cloneContent());
        }
    }

    
    /**
     * Sets the document, e.g. after loading the settings
     * @param d (the document with the auto-correction-data)
     */
    public void setDocument(Document d) {
        autokorrektur = d;
    }
    /**
     * Gets the xml-document that contains the auto-correction-data
     * @return the xml-document with the auto-correction-data
     */
    public Document getDocument() {
        return autokorrektur;
    }
    
    
    /**
     * Returns the amount of elements
     * @return Returns the amount of elements as integer value
     */
    public int getCount() {
        return autokorrektur.getRootElement().getContentSize();
    }


    /**
     * Checks whether the value passed by the parameter "e" already exists in the
     * data. Therefore, each element's id-attribute is compared to the parameter "e".
     * @param e the string we want to look for if it exists
     * @return {@code true} if we found it, false if it doesn't exist
     */
    public boolean exists(String e) {
        // get all elements
        List<Element> all = autokorrektur.getRootElement().getChildren();
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
            if (att!=null && att.equalsIgnoreCase(e)) {
                return true;
            }
        }
        // nothing found...
        return false;
        
    }


    /**
     * Adds a new pair of false/correct words to the document
     * 
     * @param falsch the wrong or mispelled word
     * @param richtig the correct writing
     * @return {@code true} if element was successfully addes, {@code false} if "falsch" already existed or
     * one of the parameters were invalid
     */
    public boolean addElement(String falsch, String richtig) {
        // check for minimum length
        if (null==falsch || falsch.length()<2 || null==richtig || richtig.length()<2) {
            return false;
        }
        // check for existence
        if (exists(falsch)) {
            return false;
        }
        // if it doesn't already exist, create new element
        Element e = new Element(Daten.ELEMENT_ENTRY);
        try {
            // set id-attribute
            e.setAttribute("id", falsch);
            // set content
            e.setText(richtig);
            // and add it to the document
            autokorrektur.getRootElement().addContent(e);
        }
        catch (IllegalAddException ex) {
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
            return false;
        }
        catch (IllegalDataException ex) {
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
            return false;
        }
        // return success
        return true;
    }
    
    
    /**
     * Gets a String-pair of auto-correct data, i.e. a string array with 2 fields. first
     * field contains the wrong/mispelled writing, the second field holds the correct
     * version of the word
     * @param nr the position of the element we want to retrieve
     * @return a string array containing the wrong and right spelling
     */
    public String[] getElement(int nr) {
        // get all elements
        List<Element> all = autokorrektur.getRootElement().getChildren();
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
     *
     * @param wrong
     * @return the correct spelling of the word {@code wrong} or {@code null}, if no spellcorrection was found
     */
    public String getCorrectSpelling(String wrong) {
        // get all elements
        List<Element> all = autokorrektur.getRootElement().getChildren();
        // create an iterator
        Iterator<Element> i = all.iterator();
        // go through list
        while (i.hasNext()) {
            // get element
            Element el = i.next();
            // get attribute value
            String att = el.getAttributeValue("id");
            // check for existing attribute
            if (null==att) {
                return null;
            }
            // get spell-check-word
            String correct = att.toLowerCase();
            // get lower-case word of mistyped wrong word...
            String retval = wrong.toLowerCase();
            // if the element's id equals the requestes string "e", return true
            // i.e. the string e already exists as element
            if (correct.equalsIgnoreCase(wrong)) {
                // now that we found the correct word, we want to see whether
                // the word starts with an upper case letter - and if so, convert
                // the first letter of the return value to upper case as well
                String firstLetter = wrong.substring(0, 1);
                String firstBigLetter = wrong.substring(0, 1).toUpperCase();
                // get return value
                retval = el.getText();
                // if both matches, we have upper case initial letter
                // convert first letter to uppercase
                if (firstLetter.equals(firstBigLetter)) {
                    retval = retval.substring(0,1).toUpperCase()+retval.substring(1);
                }
                return retval;
            }
            // when the misspelled phrase starts with an asterisk, we know that we should check the
            // end of or in between the typed word "wrong".
            if (correct.startsWith("*")) {
                // first we remove the asterisk
                correct = correct.substring(1);
                // if the misspelled phrase also ends with an asterisk, we have to check
                // for the phrase in between - that means, "wrong" is not allowed to end or start
                // with "correct"
                if (correct.endsWith(("*"))) {
                    // remove trailing asterisk
                    correct = correct.substring(0, correct.length()-1);
                    // if the mistyped word "wrong" does not start and end with "correct", we know
                    // that we have a correction in between
                    if (retval.contains(correct)) {
                        // return correct word for wrong spelling
                        return correctWithCase(retval,correct,el.getText(),wrong);
                    }
                }
                // if the mistyped word "wrong" does not end with "correct", we know
                // that
                else if (retval.endsWith(correct) && retval.contains(correct)) {
                    // return correct word for wrong spelling
                    return correctWithCase(retval,correct,el.getText(),wrong);
                }
            }
            else if (correct.endsWith("*")) {
                // get lower-case word of mistyped wrong word...
                retval = wrong.toLowerCase();
                // if the mistyped word "wrong" does not end with "correct", we know
                // that we have the correction at thr word beginning
                if (retval.startsWith(correct) && retval.contains(correct)) {
                    // return correct word for wrong spelling
                    return correctWithCase(retval,correct,el.getText(),wrong);
                }
            }
        }
        // return null, no correct word found
        return null;
    }


    private String correctWithCase(String retval, String correct, String newtext, String wrong) {
        retval = retval.replace(correct, newtext);
        // now that we found the correct word, we want to see whether
        // the word starts with an upper case letter - and if so, convert
        // the first letter of the return value to upper case as well
        String firstLetter = wrong.substring(0, 1);
        String firstBigLetter = wrong.substring(0, 1).toUpperCase();
        // if both matches, we have upper case initial letter
        // convert first letter to uppercase
        if (firstLetter.equals(firstBigLetter)) {
            retval = retval.substring(0,1).toUpperCase()+retval.substring(1);
        }
        return retval;
    }
}
