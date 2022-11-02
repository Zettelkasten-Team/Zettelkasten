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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.IllegalDataException;
import org.jdom2.IllegalNameException;

/**
 *
 * @author danielludecke
 */
public class Synonyms {

    /**
     * XML Document that Stores the main data
     */
    private Document synonymsFile;
    private boolean modified;
    /**
     * XML document that is used as backup
     */
    private Document backupdoc = null;

    public Synonyms() {
        clear();
    }

    /**
     * Clears the XML document and creates a dummy-backup of the document, in case the original
     * XML-document contains data.
     */
    public final void clear() {
        // check whether backup document exists, whether autokorrektur-document exists and whether
        // the autokorrektur-document has any data. only in this case we create a backup
        if (synonymsFile != null && synonymsFile.getRootElement().getContentSize() > 1) {
            // create new backup doc
            backupdoc = new Document(new Element("backup_synonyms"));
            // copy content
            backupdoc.getRootElement().addContent(synonymsFile.getRootElement().cloneContent());
        }
        synonymsFile = new Document(new Element("synonyms"));
        // reset modified state
        modified = false;
    }

    /**
     * This method checks whether the XML document is ok or corrupted. in case there have been
     * jdom-errors when adding new elements, the XML document {@link #synonymsFile} might be empty,
     * while the backup-document {@link #backupdoc} has data. In this case, {@code false} is
     * returned. If the XML-document is ok, {@code true} is returned.
     *
     * @return {@code true} if the main XML-document is ok, {@code false}if it might be corrupted.
     * <br><br>
     * You can use {@link #restoreDocument() restoreDocument()} to restore a corrupted document.
     */
    public boolean isDocumentOK() {
        // check whether we have any XML-document at all. proceed only, if we have no document
        // of if the XML-document does not contain data
        if ((null == synonymsFile) || (synonymsFile.getRootElement().getContentSize() < 1)) {
            // now check whether we have a backup of the XML document, which has content
            if ((backupdoc != null) && (backupdoc.getRootElement().getContentSize() > 0)) {
                // if so, the backup contains data that the main document does not has
                // so, we assume the document is *not* ok
                return false;
            }
        }
        // else everything is fine
        return true;
    }

    /**
     * In case we have a corrupted XML document with a backup document that has data (see
     * {@link #isDocumentOK() isDocumentOK()}), we can restore the backupped data with this
     * method.<br><br>
     * So, this method copies back the content of the {@link #backupdoc} to the original XML
     * document {@link #synonymsFile}.
     */
    public void restoreDocument() {
        // check whether we have a backup document that also contains data
        if ((backupdoc != null) && (backupdoc.getRootElement().getContentSize() > 1)) {
            // if we have it, create new main XML document
            synonymsFile = new Document(new Element("synonyms"));
            // and copy the content of the backup document to it
            synonymsFile.getRootElement().addContent(backupdoc.getRootElement().cloneContent());
        }
    }

    /**
     * Sets the document, e.g. after loading the settings
     *
     * @param d the document with the synonyms-data
     */
    public void setDocument(Document d) {
        synonymsFile = d;
        // check whether backup document exists, whether autokorrektur-document exists and whether
        // the autokorrektur-document has any data. only in this case we create a backup
        if (synonymsFile != null && synonymsFile.getRootElement().getContentSize() > 1) {
            // create new backup doc
            backupdoc = new Document(new Element("backup_synonyms"));
            // copy content
            backupdoc.getRootElement().addContent(synonymsFile.getRootElement().cloneContent());
        }
    }

    /**
     * Gets the xml-document that contains the synonyms-data
     *
     * @return the xml-document with the synonyms-data
     */
    public Document getDocument() {
        return synonymsFile;
    }

    /**
     * This method returns the size of the xml data files
     *
     * @return the size of the data file
     */
    public int getCount() {
        return synonymsFile.getRootElement().getContentSize();
    }

    /**
     * Retrieves all synonyms - both index words and related synonyms - that currently are in the
     * synonyms data base.
     *
     * @return all current synonyms as string array.
     */
    public String[] getAllSynonyms() {
        LinkedList<String> synlist = new LinkedList<>();
        // iterate all synonyms elements
        for (int i = 0; i < getCount(); i++) {
            // retrieve synonyms
            String[] syns = getSynonymLine(i, true);
            // add to list
            for (String sy : syns) {
                // check for doubles
                if (!synlist.contains(sy)) {
                    synlist.add(sy);
                }
            }
        }
        return synlist.toArray(new String[synlist.size()]);
    }

    public void appendSynonyms(Document syndoc) {
        int count = syndoc.getRootElement().getContentSize();
        for (int i = 0; i < count; i++) {
            String[] synline = getSynonymLine(syndoc, i, false);
            addSynonym(synline);
        }
    }

    public void addSynonym(String[] synline) {
        // we need at least two elements in the array: the original word and at least one synonym
        if (null == synline || synline.length < 2) {
            return;
        }
        // if the synonyms-index-word already exists, don't add it...
        if (getSynonymPosition(synline[0]) != -1) {
            return;
        }
        // create new synonyms element
        Element synonym = new Element(Daten.ELEMENT_ENTRY);
        try {
            // trim spaces
            synline[0] = synline[0].trim();
            // set the original word as value-attribute to the "entry"-element
            synonym.setAttribute("indexword", synline[0]);
            // now go through the rest of the string-array
            for (int cnt = 1; cnt < synline.length; cnt++) {
                // create a sub-child "syn" for each further synonym
                Element syn = new Element("syn");
                // set text from string array
                syn.setText(synline[cnt].trim());
                // add child to synonym-element
                synonym.addContent(syn);
            }
            // finally, add new element to the document
            synonymsFile.getRootElement().addContent(synonym);
            setModified(true);
        } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
            Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }

    public String[] getSynonymLine(Document doc, int nr, boolean matchcase) {
        // get element
        Element syn = retrieveElement(doc, nr);
        // init return value
        String[] retval = null;
        // if we have a valid element, go on
        if (syn != null) {
            // get list of child-element with synonyms
            List<?> l = syn.getChildren();
            // create array
            retval = new String[l.size() + 1];
            // first element of the array is the index word
            // retrieve indexword-attribute
            String attr = syn.getAttributeValue("indexword");
            // check whether value ok
            if (attr != null) {
                // then use it for array
                if (matchcase) {
                    retval[0] = attr;
                } else {
                    retval[0] = attr.toLowerCase();
                }
            } else {
                // else log error message, telling the number of the corrupted synonym-data
                Constants.zknlogger.log(Level.WARNING, "No index word for synonym {0} found.", String.valueOf(nr));
                // and return null
                return null;
            }
            // following elements are the synonyms. therefore, copy the children's text to the array
            for (int cnt = 0; cnt < l.size(); cnt++) {
                // get the element
                Element e = (Element) l.get(cnt);
                // get the element's text
                if (matchcase) {
                    retval[cnt + 1] = e.getText();
                } else {
                    retval[cnt + 1] = e.getText().toLowerCase();
                }
            }
        }
        return retval;
    }

    /**
     * This method returns a synonym (as index-word) with its related synonyms. The return-value is
     * a string-array with the first element being the index-word, and the following elements being
     * the related synonyms.
     *
     * @param nr the number of the requested synonym, with a range from 0 to {@link #getCount()}-1
     * @param matchcase use {@code true} if the synonyms (strings in the return array) should be
     * returned in original case-letters. use {@code false} if they should be returned in
     * lower-case-letters
     * @return a string-array with the first element being the index-word, and the following
     * elements being the related synonyms; or {@code null}, if no synonym was found
     */
    public String[] getSynonymLine(int nr, boolean matchcase) {
        return getSynonymLine(synonymsFile, nr, matchcase);
    }

    /**
     * This method sets a new synonm-line, i.e. a synonym (as index-word) with its related synonyms.
     * The new synonyms have to passed as string-parameter {@code synline}.
     *
     * @param nr the number of the requested synonym, with a range from 0 to (getCount()-1)
     * @param synline a string-array with the first element being the index-word, and the following
     * elements being the related synonyms
     */
    public void setSynonymLine(int nr, String[] synline) {
        // get element
        Element synonym = retrieveElement(nr);
        // remove all child-content (i.e. all synonyms)
        synonym.removeContent();
        try {
            // set the original word as value-attribute to the "entry"-element
            synonym.setAttribute("indexword", synline[0]);
            // now go through the rest of the string-array
            for (int cnt = 1; cnt < synline.length; cnt++) {
                // create a sub-child "syn" for each further synonym
                Element syn = new Element("syn");
                // set text from string array
                syn.setText(synline[cnt]);
                // add child to synonym-element
                synonym.addContent(syn);
                setModified(true);
            }
        } catch (IllegalDataException | IllegalNameException ex) {
            Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }

    public boolean mergeSynonymLines(String originalLineIndexWord, String mergedLineIndexWord) {
        return mergeSynonymLines(getSynonymPosition(originalLineIndexWord), getSynonymPosition(mergedLineIndexWord));
    }

    /**
     * This method merges two synonym-lines, where the line given by the index-number
     * {@code originalLine} remains, while the line with the index-number {@code mergedLine} will be
     * appended to the synonyms of the {@code originalLine}.
     *
     * @param originalLine the index-number of the original line that should remain in the database
     * @param mergedLine the index-number of the synonym-line that should be appended to the
     * original line.
     * @return
     */
    public boolean mergeSynonymLines(int originalLine, int mergedLine) {
        // check for valid parameters
        if (originalLine < 0 || originalLine >= getCount() || mergedLine < 0 || mergedLine >= getCount()) {
            return false;
        }
        // retrieve original synonyms line
        String[] oriline = getSynonymLine(originalLine, true);
        // retrieve apped-synonyms line
        String[] appendline = getSynonymLine(mergedLine, true);
        // check for valid values
        if (null == oriline || null == appendline) {
            return false;
        }
        // create array-list that will contain new synonym line
        List<String> newline = new ArrayList<>();
        // add all "old", original synonyms
        newline.addAll(Arrays.asList(oriline));
        // now append all "merged" synonyms
        newline.addAll(Arrays.asList(appendline));
        // set new line
        setSynonymLine(originalLine, newline.toArray(new String[newline.size()]));
        // remove old synonym-line
        synonymsFile.getRootElement().removeContent(mergedLine);
        setModified(true);
        return true;
    }

    /**
     * This method sets a new index-word to the synonyms-line with the number {@code nr}.
     *
     * @param nr the number of the synonyms-line, where the index-word should be changed
     * @param indexword the new index-word, as string
     */
    public void setIndexWord(int nr, String indexword) {
        // get element
        Element synonym = retrieveElement(nr);
        // set the original word as value-attribute to the "entry"-element
        synonym.setAttribute("indexword", indexword);
        setModified(true);
    }

    /**
     * This method adds a new synonym {@code appendsyn} to an existing synonym-line {@code nr}.
     *
     * @param nr the number of the existing synonym-line
     * @param appendsyn the new synonym that should be appended to that line.
     */
    public void appendSingleSynonym(int nr, String appendsyn) {
        // get element
        Element synonym = retrieveElement(nr);
        // chekc for valid value
        if (synonym != null) {
            try {
                // create a sub-child "syn" for each further synonym
                Element syn = new Element("syn");
                // set text from string array
                syn.setText(appendsyn);
                // add child to synonym-element
                synonym.addContent(syn);
                setModified(true);
            } catch (IllegalAddException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * This method returns a synonym (as index-word) with its related synonyms. The return-value is
     * a string-array with the first element being the index-word, and the following elements being
     * the related synonyms.
     * <br><br>
     * If a complete synonyme-line from <b>any word</b> is requested, use
     * {@link #getSynonymLineFromAny(java.lang.String) #getSynonymLineFromAny()}
     *
     * @param indexword string-value of the synonym (original- or index-word) which is searched for
     * in the list
     * @param matchcase
     * @return a string-array with the first element being the index-word, and the following
     * elements being the related synonyms; or {@code null}, if no synonym was found
     */
    public String[] getSynonymLine(String indexword, boolean matchcase) {
        // retrieve position of the index-word
        int pos = getSynonymPosition(indexword);
        // if we found an index-word, return the synonyms, else return null
        if (pos != -1) {
            return getSynonymLine(pos, matchcase);
        } else {
            return null;
        }
    }

    private Element retrieveElement(Document doc, int pos) {
        // create a list of all elements from the given xml file
        try {
            List<?> elementList = doc.getRootElement().getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * This function retrieves an element of a xml document at a given position. used for other
     * methods like getAuthor or getKeyword. The position is a value from 0 to (size-1).
     *
     * @param pos the position of the element, ranged from 0 to (document-size - 1)
     * @return the element if a match was found, otherwise null
     */
    private Element retrieveElement(int pos) {
        return retrieveElement(synonymsFile, pos);
    }

    /**
     * This method returns the position of a synonym (original or index word) in the XML file if the
     * synonym doesn't exist, the return value is -1.
     * <br><br>
     *
     * @param indexword string-value of the synonym (original- or index-word) which is searched for
     * in the list
     * @return the position of the synonym or -1 if no match was found
     */
    public int getSynonymPosition(String indexword) {
        // trim spaces
        indexword = indexword.trim();
        // create a list of all author elements from the author xml file
        try {
            List<?> synList = synonymsFile.getRootElement().getContent();
            // and an iterator for the loop below
            Iterator<?> iterator = synList.iterator();
            // counter for the return value if a found synonym matches the parameter
            int cnt = 0;

            while (iterator.hasNext()) {
                Element syn = (Element) iterator.next();
                // if synonym-index-word matches the parameter string, return the position
                if (indexword.equalsIgnoreCase(syn.getAttributeValue("indexword"))) {
                    return cnt;
                }
                // else increase counter
                cnt++;
            }
            // if no author was found, return -1
            return -1;
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            return -1;
        }
    }

    /**
     * This method returns the position of a synonym in the XML file, independent from wether the
     * requested word is an index-word or a related synonym. if the synonym doesn't exist, the
     * return value is -1.
     * <br><br>
     * If the position of a certain index-word only is requestes, use
     * {@link #getSynonymPosition(java.lang.String) getSynonymPosition(java.lang.String)} instead.
     * <br><br>
     * If a complete synonyme-line is requested, use
     * {@link #getSynonymLine(java.lang.String) #getSynonymLine(java.lang.String)} or
     * {@link #getSynonymLineFromAny(java.lang.String) getSynonymLineFromAny(java.lang.String)}.
     *
     * @param synonym string-value of the synonym (wither original- or index-word, or any related
     * synonym) which is searched for in the list.
     * @param matchcase
     * @return the position of the synonym or -1 if no match was found
     */
    public int findSynonym(String synonym, boolean matchcase) {
        // go through all entries
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // get each synonym-line
            String[] synline = getSynonymLine(cnt, matchcase);
            // check for valid value
            if (synline != null) {
                // if the array contains the requested synonym, return line-position
                for (String s : synline) {
                    if (s.equalsIgnoreCase(synonym)) {
                        return cnt;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * This method searches all index-words of the synonyms-file and compares them to the parameter
     * {@code synonym}. If a match was found, i.e. if the parameter {@code synonym} is an
     * index-word, {@code true} is returned, {@code false} otherwise.
     *
     * @param synonym string-value which should be checked whether it is an index-word or not
     * @param matchcase
     * @return {@code true} if the parameter {@code synonym} is an index-word, {@code false}
     * otherwise
     */
    public boolean isIndexWord(String synonym, boolean matchcase) {
        // go through all entries
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // get each synonym-line
            String[] synline = getSynonymLine(cnt, matchcase);
            // if the index-word of the array contains the requested synonym, return true
            if (synline != null && synline[0].equalsIgnoreCase(synonym)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method searches through all synonyms in the data-file and returns the first index-word
     * when a related synonym of that index-word matches {@code synvalue}.<br><br><b>Note:</b> This
     * method excludes index-words from the search! Only non-index-words of each synonym-line are
     * checked for matching the parameter {@code synvalue}.
     *
     * @param synvalue the synonym where we want to retrieve the related index-word
     * @param matchcase whether the check for the synonyms should be case sensitive {@code true} or
     * not.
     * @return the index-word of the related synonym {@code synvalue}, or {@code null} if no
     * index-word was found.
     */
    public String getIndexWord(String synvalue, boolean matchcase) {
        // go through all entries
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // get each synonym-line
            String[] synline = getSynonymLine(cnt, matchcase);
            // go through array to find the value "synvalue"...
            if (synline != null && synline.length > 1) {
                for (int c = 1; c < synline.length; c++) {
                    // if we found the synvalue, return the related indexword
                    if (synline[c].equalsIgnoreCase(synvalue)) {
                        return synline[0];
                    }
                }
            }
        }
        // else return null...
        return null;
    }

    /**
     * This method returns the related synonyms of a given synonym in the XML file, independent from
     * wether the requested word is an index-word or a related synonym. if the synonym doesn't
     * exist, the return value is null.
     * <br><br>
     * If a complete synonyme-line from the <b>index-word</b> only is requested, use
     * {@link #getSynonymLine(java.lang.String) #getSynonymLine(java.lang.String)}
     *
     * @param synonym string-value of the synonym (wither original- or index-word, or any related
     * synonym) which is searched for in the list.
     * @param matchcase
     * @return the other related synonyms as string array, with the parameter {@code synonym}
     * <b>included</b>, or null if {@code synonym} wasn't found.
     */
    public String[] getSynonymLineFromAny(String synonym, boolean matchcase) {
        // go through all entries
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // get each synonym-line
            String[] synline = getSynonymLine(cnt, matchcase);
            // check for valid value
            if (synline != null) {
                // if the array contains the requested synonym, return the synonym-line (string-array)
                for (String s : synline) {
                    if (s.equalsIgnoreCase(synonym)) {
                        return synline;
                    }
                }
            }
        }
        return null;
    }

    /**
     * sets the modified state of the bookmark-data
     *
     * @param m true when the bookmark-data was modified, or false if modifications were saved.
     */
    public void setModified(boolean m) {
        modified = m;
    }

    /**
     * returns the modified state of the bookmark-data
     *
     * @return {@code true} if the bookmark-data was modified, false if it's unchanged
     */
    public boolean isModified() {
        return modified;
    }
}
