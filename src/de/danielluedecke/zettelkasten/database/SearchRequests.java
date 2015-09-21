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

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import de.danielluedecke.zettelkasten.util.Constants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
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
public class SearchRequests {
    /**
     * <searches>
     *   <searchrequest>
     *      <searchterms>
     *        <term>luhmann</term>
     *        <term>computer</term>
     *      </searchterms>
     *      <where>16</where>
     *      <logical>1</logical>
     *      <wholeword>false</wholeword>
     *      <matchcase>false</matchcase>
     *      <synonyms>true</synonyms>
     *      <regex>true</regex>
     *      <description>
     *        "computer", "luhmann", und-verknüpft, Suche in Schlagwörtern, Literaturangaben,
     *        Zetteln (und weitere...) (221037)
     *      </description>
     *      <results>1106,1110,1380,1553,1556,1558,1560,1561,1566,1594,1595</results>
     *    </searchrequest>
     *  </searches>
     */
    private Document searches;

    /**
     *
     */
    private boolean modified;
    /**
     * 
     */
    private final LinkedList<String> searchTermsHistory = new LinkedList<>();
    /**
     *
     */
    private static final int searchTermsHistoryMax = 20;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;
    /**
     *
     */
    private int[] searchresults;
    
    
    public SearchRequests(ZettelkastenView zkn) {
        zknframe = zkn;
        clear();
    }


    /**
     * Clears all search data.
     */
    public final void clear() {
        // create new XML document
        searches = new Document(new Element("searches"));
        // reset last-used search result attribute
        searches.getRootElement().setAttribute("lastused","-1");
        // set modified state
        modified = false;
    }


    /**
     * Set search data, e.g. when loading data, set loaded XML document
     * with this method.
     *
     * @param d the XML document with the search results that should be set as new
     * search results data
     */
    public void setSearchData(Document d) {
        searches = d;
    }
    /**
     * Retrieve the search results data document (XML-data)
     *
     * @return the search results data document (XML-data)
     */
    public Document getSearchData() {
        return searches;
    }


    /**
     * This method saves the results (i.e. the entry-numbers as integer-array) of the latest search request.
     * Use this method to indicate whether a search request (see {@link de.danielluedecke.zettelkasten.tasks.StartSearchTask StartSearchTask}
     * had results or not. Use {@code null} if the search had no results.
     *
     * @param sr an integer array with entry-numbers of found entries that matched the search parameters,
     * or {@code null} if nothing was found.
     */
    public void setCurrentSearchResults(int[] sr) {
        searchresults = sr;
    }
    /**
     * This method returns the results (i.e. the entry-numbers as integer-array) of the latest search request.
     * Use this method to check whether a search request (see {@link de.danielluedecke.zettelkasten.tasks.StartSearchTask StartSearchTask}
     * had results or not. Returns {@code null} if the search had no results.
     *
     * @return the found entries, with their numbers ad integer array, or {@code null} if nothing was found.
     */
    public int[] getCurrentSearchResults() {
        return searchresults;
    }
    
    
    /**
     * This method adds a new search request to the search-object. The relevant parameters
     * are stored in an xml-file, so we can save/load the data easily.
     * 
     * @param s the search terms, in a string-array
     * @param w where we want to look (e.g. in keywords, content, authors...
     * @param l which kind of logical-combination we have
     * @param ww if true, find wholewords only
     * @param mc if true, search is case-sensitive
     * @param syn true, if the search included synonyms, false otherwise
     * @param rex true, if the search term is a regular expression, false otherwise
     * @param r the search results, i.e. the entry-numbers as integer-array
     * @param n a shorted description of the search request, so we know what the user was looking for
     * @param ld a long description of the search request, so we know what the user was looking for
     * @return {@code true} if search result was successfully added, {@code false} if an error occured
     */
    public boolean addSearch(String[] s, int w, int l, boolean ww, boolean mc, boolean syn, boolean rex, int[] r, String n, String ld) {
        // check for valid parameters. do we have search terms?
        if (null==s || s.length<1) {
            return false;
        }
        // do we have search results?
        if (null==r || r.length<1) {
            return false;
        }
        // create a new element for search requests
        Element sr = new Element("searchrequest");
        try {
            // and add it to the document
            searches.getRootElement().addContent(sr);
            // create the child-elements
            Element st = new Element("searchterms");
            // iterate search terms and add each term as sub-element
            for (String sterm : s) {
                Element term = new Element("term");
                term.setText(sterm);
                st.addContent(term);
            }
            // now add the search terms to the parent
            sr.addContent(st);
            // create element for where to look
            Element where = new Element("where");
            where.setText(String.valueOf(w));
            // now add the element to the parent
            sr.addContent(where);
            // create element for the logical combination
            Element log = new Element("logical");
            log.setText(String.valueOf(l));
            // now add the element to the parent
            sr.addContent(log);
            // create element for whole-word
            Element whole = new Element("wholeword");
            whole.setText((ww)?"true":"false");
            // now add the element to the parent
            sr.addContent(whole);
            // create element for match case
            Element match = new Element("matchcase");
            match.setText((mc)?"true":"false");
            // now add the element to the parent
            sr.addContent(match);
            // create element for synonymsearch
            Element synonyms = new Element("synonyms");
            synonyms.setText((syn)?"true":"false");
            // now add the element to the parent
            sr.addContent(synonyms);
            // create element for synonymsearch
            Element regex = new Element("regex");
            regex.setText((rex)?"true":"false");
            // now add the element to the parent
            sr.addContent(regex);
            // create element for the description
            Element label = new Element(Daten.ELEMEMT_DESCRIPTION);
            label.setText(n);
            // now add the element to the parent
            sr.addContent(label);
            // create element for the long description
            Element longlabel = new Element("longdesc");
            longlabel.setText(ld);
            // now add the element to the parent
            sr.addContent(longlabel);
            // create element for search results
            Element results = new Element("results");
            // create stringbuilder, since we want to copy the ineger array
            // to an stringarray
            StringBuilder sb = new StringBuilder("");
            // iterate array and convert all integer to string
            for (int nr : r) {
                sb.append(String.valueOf(nr));
                sb.append(",");
            }
            // delete last comma
            if (sb.length()>1) {
                sb.setLength(sb.length()-1);
            }
            // add string to element
            results.setText(sb.toString());
            // now add the element to the parent
            sr.addContent(results);
            // change modified state
            setModified(true);
            // return success
            return true;
        }
        catch (IllegalAddException | IllegalDataException ex) {
            // log error
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
        }
        return false;
    }
    
    
    /**
     * This method retrieves the searchterms for a given search-request.
     * 
     * @param nr The number of the search request
     * @return the searchterms as string array, or null if an error occured
     */
    public String[] getSearchTerms(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, go on...
        if (el!=null) {
            // retrieve a list with all search terms
            List<?> terms = el.getChild("searchterms").getChildren();
            // if we have no elements, return null
            if ((terms.isEmpty()) || (terms.size()<1)) {
                return null;
            }
            // create linked list for return results
            LinkedList<String> st = new LinkedList<>();
            // create iterator
            Iterator<?> i = terms.iterator();
            // go through all search terms
            while (i.hasNext()) {
                // get each search-term-element
                Element e = (Element) i.next();
                // if it has text, add it to linked list
                if (!e.getText().isEmpty()) {
                    st.add(e.getText());
                }
            }
            // when we searched for entries whithout authors or keywords e.g., the
            // searchterms are emptry. in this case, return null
            if (st.isEmpty()) {
                return null;
            }
            // copy all children to an array
            String[] retval = st.toArray(new String[st.size()]);
            // return results
            return retval;
        }
        return null;
    }
    

    /**
     * This method retrieves the searchterms for a given search-request.
     *
     * @param nr The number of the search request
     * @return {@code true} if the search was a synonym-search, false otherweise
     */
    public boolean isSynonymSearch(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, return "true"
        if (el!=null) {
            // retrieve synonyms-child-element
            el = el.getChild("synonyms");
            // if we have any element, return value
            if (el!=null) {
                return (el.getText().equalsIgnoreCase("true"));
            }
        }
        return false;
    }


    /**
     * This method retrieves the reg-ex-state, i.e. whether a search contained regular expressions
     * as search terms or "normal" search terms
     *
     * @param nr The number of the search request
     * @return {@code true} if the search was a regular-expression-search, false otherweise
     */
    public boolean isRegExSearch(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, return "true"
        if (el!=null) {
            // retrieve synonyms-child-element
            el = el.getChild("regex");
            // if we have any element, return value
            if (el!=null) {
                return (el.getText().equalsIgnoreCase("true"));
            }
        }
        return false;
    }


    /**
     * Adds a new searchterm from a search-request to the history of search terms. This histroy,
     * a linked list of type String, stores the 20 last used search terms. If this list is already
     * "full", the first (oldest) element will be removed and the new search term will be added to
     * the end of this list.
     *
     * @param st the new search term that should be added to the history
     */
    public void addToHistory(String st) {
        // get current size of history
        int count = searchTermsHistory.size();
        // if it reached the limit, remove first element
        if (count>=searchTermsHistoryMax) {
            searchTermsHistory.pollFirst();
        }
        // add new element to the end of the list
        searchTermsHistory.add(st);
    }
    /**
     * Retrieves an array of all previously used search terms.
     *
     * @return a string-array with the last 20 used search terms, or null if no entries are
     * currently saved in the history-list
     */
    public String[] getHistory() {
        // if we have no elements in our history, return null
        if (null==searchTermsHistory || 0==searchTermsHistory.size()) {
            return null;
        }
        // else copy list to string arary
        String[] hist = searchTermsHistory.toArray(new String[searchTermsHistory.size()]);
        // and sort that string array
        if (hist!=null && hist.length>0) {
            Arrays.sort(hist, new Comparer());
        }
        // return array
        return hist;
    }


    /**
     * This method returns the searcresults (i.e. the entry-numbers of the found entries)
     * of a certain search-request.
     * 
     * @param nr the number of the search request which results we want to have
     * @return the search-results (i.e. the entry-numbers of the found entries) as int-array, or {@code null}
     * if an error occured
     */
    public int[] getSearchResults(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, go on...
        if (el!=null) {
            // get all results and split them into an array
            String[] r = el.getChild("results").getText().split(",");
            // create integer array
            int[] retval = new int[r.length];
            try {
                // go through string-array and convert each string to integer
                for (int cnt=0; cnt<r.length; cnt++) {
                    retval[cnt] = Integer.parseInt(r[cnt]);
                }
                return retval;
            }
            catch (NumberFormatException ex) {
                // log error
                Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
                Constants.zknlogger.log(Level.WARNING,"Saved search result contained illegal entry numbers!");
                // return null
                return null;
            }
        }
        return null;
    }
    /**
     * This method returns the searcresults (i.e. the entry-numbers of the found entries)
     * of a certain search-request.
     * 
     * @param nr the number of the searchrequest where we want to set the new results
     * @param results the new results for the searchrequest {@code nr}
     * @return 
     */
    public boolean setSearchResults(int nr, int[] results) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, go on...
        if ((el!=null)&&(results!=null)) {
            // create string builder
            StringBuilder sb = new StringBuilder("");
            // go through all results
            for (int r : results) {
                // append them to stringbuilder
                sb.append(String.valueOf(r));
                sb.append(",");
            }
            // delete last comma
            if (sb.length()>1) {
                sb.setLength(sb.length()-1);
            }
            // get all results and split them into an array
            el.getChild("results").setText(sb.toString());
            // change modified state
            setModified(true);
            // everything is ok
            return true;
        }
        // error occurred
        return false;
    }
    
    
    /**
     * Sets the currently selected search results/request. Used in the CStartSearch-dialog
     * when filtering search results.
     * 
     * @return the number of the currently activated/selected search request, or -1 if an error occured.
     * Remember that this value has a range from 0 to ({@link #getCount() getCount()}-1)
     */
    public int getCurrentSearch() {
        try {
            // get the value for the last-used-searchrequest
            int ls = Integer.parseInt(searches.getRootElement().getAttributeValue("lastused"));
            // check whether it is out of bounds...
            if (ls>=getCount()) {
                ls = -1;
            }
            // return result
            return ls;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }
    /**
     * Sets the currently selected search results/request. Is used when a combobox-item in the
     * CSearchResults-frame is selected.
     * 
     * @param nr the number of the currently used search request. usually, this number corresponds
     * to the combobox-selection of the CSearchResults-frame.
     * Remember that this value has a range from 0 to ({@link #getCount() getCount()}-1)
     * @return {@code true} is current-searchindex could be successfully set, false if an error occured
     */
    public boolean setCurrentSearch(int nr) {
        // check whether "nr" is within valid bounds
        if (nr>=0 && nr<getCount()) {
            // if so, set last-used-attribute
            searches.getRootElement().setAttribute("lastused", String.valueOf(nr));
            // and return true
            return true;
        }
        // otherwise return false
        return false;
    }
    
    
    /**
     * This method deletes the entry {@code nr} from the search-results-list of the
     * searchrequest {@code searchrequest}.
     * 
     * @param searchrequest the number of the searchrequest
     * @param nr the entrynumber that should be removed from that searchrequest's rsultlist
     */
    public void deleteResultEntry(int searchrequest, int nr) {
        // get searchresults
        int[] results = getSearchResults(searchrequest);
        // check whether we have any
        if (results!=null) {
            // if we only have one result, and this one entry equals that entry that should be deleted,
            // we can completely delete the search-request
            if (1==results.length && nr==results[0]) {
                deleteSearchRequest(searchrequest);
            }
            else {
                // create new array which is one field shorter - because one entry
                // has to be deleted
                int[] newresults = new int[results.length-1];
                int newcnt = 0;
                // go through array of old results.
                // when result-value does not equal the entry that should be removed,
                // add that entry to the new results.
                for (int cnt=0; cnt<results.length;cnt++) {
                    if (results[cnt]!=nr) {
                        newresults[newcnt++] = results[cnt];
                    }
                }
                // set new search results
                setSearchResults(searchrequest,newresults);
            }
            // change modified state
            setModified(true);
        }
    }
    

    /**
     * This method retrieves the position of the entry {@code zettelnumber} within the searchrequest
     * {@code searchrequest}. if the requested entry was not found in that searchrequest, -1 is returned.
     *
     * @param searchrequest the searchrequest-index, where we want to look for an entry's position
     * @param zettelnumber the number of the entry we are looking for
     * @return the position within the search result, if the requestes entry {@code zettelnumber} was found,
     * or -1 if no entry was found
     */
    public int getZettelPositionInResult(int searchrequest, int zettelnumber) {
        // get searchresults from the related search request
        int[] results = getSearchResults(searchrequest);
        // check whether we have any search results
        if (results!=null) {
            // init counter
            int cnt=0;
            // go through all search result entries...
            for (int r : results) {
                // if search result entry equals zettelnumber, return position
                if (r==zettelnumber) {
                    return cnt;
                }
                // else increase position counter
                cnt++;
            }
        }
        // if no entry was found in the search result, return -1
        return -1;
    }


    /**
     * This method deleted a complete search request.
     * @param nr the number of the search request to be deleted
     */
    public void deleteSearchRequest(int nr) {
        // get amount of search requests.
        int count = getCount();
        // if we don't have any, return...
        if (0==count) {
            return;
        }
        // if we have exactly one search result, clear data
        if (1==count) {
            clear();
        }
        // remove the required search request
        else {
            searches.getRootElement().removeContent(nr);
            // decrease search-result-counter
            count--;
            // if the last current-search-index is now out of bounds,
            // set the current-search-index to the last search-request
            if (getCurrentSearch()>=count) {
                setCurrentSearch(count-1);
            }
        }
        // change modified state
        setModified(true);
    }


    /**
     * This method duplicated the current search result and appends it to the end of the
     * search results data. Furthermore, the duplicated search result is set as current search result
     * (i.e. which is in use).
     */
    public void duplicateSearchRequest() {
        // retrieve the current search XML element and add it to the root element, thus duplicating
        // the current search results data
        searches.getRootElement().addContent((Element)searches.getRootElement().getContent(getCurrentSearch()).clone());
        // set duplicated search results as currently used search results
        setCurrentSearch(searches.getRootElement().getContentSize());
        // append a time-string, so we always have a unique search-description,
        // even if the user searches twice for the same searchterms
        DateFormat df = new SimpleDateFormat("kkmmss");
        String desc = " ("+df.format(new Date())+")";
        // update search description
        setShortDescription(getCurrentSearch(),getShortDescription(getCurrentSearch())+desc);
        // and set modified state
        setModified(true);
    }


    /**
     * This method deletes all search requests and clears the search-request-xml-file.
     * The modified state is set to true, so the cleared xml-file will be saved, leading
     * to an empty or resettet search-request-file.
     */
    public void deleteAllSearchRequests() {
        clear();
        setModified(true);
    }
    
    
    /**
     * Gets the short description of a certain search-request.
     * The position is a value from 0 to (size of xml file - 1)
     * 
     * @param nr the number of the search-requests which label/description we want to retrieve.
     * @return the description of the search-request as string
     */
    public String getShortDescription(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, go on...
        if (el!=null) {
            String retval = el.getChild(Daten.ELEMEMT_DESCRIPTION).getText();
            return retval;
        }
        return "";
    }
    /**
     * Sets the short description of a certain search-request.
     * The position {@code nr} is a value from 0 to (size of xml file - 1)
     *
     * @param nr the number of the search-requests which label/description we want to change.
     * @param desc the description of the search-request as string
     */
    public void setShortDescription(int nr, String desc) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, change description
        if (el!=null) {
            el.getChild(Daten.ELEMEMT_DESCRIPTION).setText(desc);
        }
    }
    
    
    /**
     * Gets the short description of a certain search-request.
     * 
     * @param nr the number of the search-requests which label/description we want to retrieve.
     * @return the description of the search-request as string
     */
    public String getLongDescription(int nr) {
        // get the element
        Element el = retrieveElement(nr);
        // if we found an element, go on...
        if (el!=null) {
            String retval = el.getChild("longdesc").getText();
            return retval;
        }
        return "";
    }
    
    
    /**
     * This method returns the amount of saved search requests.
     * 
     * @return the amount of saved search requests
     */
    public int getCount() {
        return searches.getRootElement().getContentSize();
    }
    
    
    /**
     * Whenenver we have changes to current searches, added new or deleted existing
     * searches, we should change the modified state with this method.
     * 
     * @param m whether the modified-state is true or not
     */
    public void setModified(boolean m) {
        modified=m;
        // update indicator for autobackup
        zknframe.setBackupNecessary();
    }
    /**
     * Gets the modified state
     * @return {@code true} if we have any changes to the search requests, false otherwise
     */
    public boolean isModified() {
        return modified;
    }    
    
    
    /**
     * This function retrieves an element of a xml document at a given
     * position. The position is a value from 0 to (size of xml file - 1)
     * 
     * @param pos (the position of the element)
     * @return the element if a match was found, otherwise null)
     */
    private Element retrieveElement(int pos) {
        // create a list of all elements from the given xml file
        try { 
            List<?> elementList = searches.getRootElement().getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos);
            }
            catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        catch (IllegalStateException e) {
            return null;
        }
    }
    
}
