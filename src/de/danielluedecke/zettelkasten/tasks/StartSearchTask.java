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

package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jdom2.Element;

/**
 *
 * @author danielludecke
 */
public class StartSearchTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     * A reference to the SearchRequests-class which stores the searchterms and other
     * search settings like case-sensitive search, where to search in and so on...
     */
    private final SearchRequests searchrequest;
    /**
     *
     */
    private final Synonyms synonymsObj;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(StartSearchTask.class);
    /**
     * The search terms, passed as an array
     */
    private String[] searchTerms = null;
    /**
     * The entries which shall be searched...
     */
    private int[] searchEntries = null;
    /**
     * This indicates where we want to look or where the search should be apllied to. We can search
     * for the searchterms within keywords, entry-conten or title, authors etc.
     *
     * See {@link Constants} for more details.
     */
    private final int where;
    /**
     * Indicates the logical-combination of search-terms. See constants below for more details.
     */
    private final int logical;
    /**
     * Indicates whether a search should look for whole words only, or if a match is also given
     * when the search terms is only part of a found word.
     */
    private final boolean wholeword;
    /**
     * Indicates whether the search is case sensitive (true) or not (false)
     */
    private final boolean matchcase;
    /**
     * the class CStartSearch is not only used for search requests, but also for finding
     * entries that should be added to the desktop or as luhmann-numbers. If we don't
     * need the searchresults to be added to the {@link SearchRequests} class, we have
     * to set this parameter to "true", false if we want to have a "real" search.
     */
    private final boolean desktoponly;
    /**
     * whether the search should include synonyms or not
     */
    private boolean synonyms = false;
    private boolean regex = false;
    private boolean timesearch = false;
    private String datefrom = "";
    private String dateto = "";
    private int timestampindex = -1;
    private int foundCounter = 0;
    private String[] dummysearchterms = null;
    /**
     * The entry-numbers of the search-result, i.e. the found entries of a search-request
     * as integer-array.
     */
    private int[] results;
    /**
     * A short description of the search, which will be set in the CSearchResult-frame's combobox, so the
     * user can identify what kind of search is related to the search results
     */
    private String searchLabel;
    /**
     * A long description of the search, which will be set in the CSearchResult-frame's combobox, so the
     * user can identify what kind of search is related to the search results
     */
    private String longdesc;
    /**
     * Initially, we assume a usual search
     */
    private int typeOfSearch = Constants.SEARCH_USUAL;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;

    /**
     *
     * @param parent the parent-window
     * @param d a reference to the Daten class
     * @param sr a reference to the CSearchRequesrs class
     * @param s a string-array containing search terms
     * @param se an integer array containing the entry-numbers of those entries where
     * the search should be applied to
     * @param w where the search should be applied to, i.e. search within content, keywords, authors etc.
     * @param l the logical-combination of the search
     * @param ww pass true, if the search should find whole words only
     * @param mc whether the search is case sensitive (true) or not (false)
     * @param syn whether the search should include synonyms or not
     * @param rex whether the search terms contain regular expressions or not
     * @param ts whether the user requested a time-search, i.e. a search for entries that were created
     * or changed within a certain period
     * @param fr the start of the period, when a timesearch is requested. format: "yymmdd".
     * @param to the end of the period, when a timesearch is requested. format: "yymmdd".
     * @param tsi the timestampindex, which indicate whether the user wants to search only for entries
     * within a period of <i>creation</i> date (0), of <i>edited</i> date (1) or both (2).
     * @param donly whether the search should open the CSearchResults-frame (false), or whether the search-results
     * are used for other purposes, like e.g. putting the results to the desktop (true)
     */
    StartSearchTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, Daten d, SearchRequests sr, Synonyms sy,
                    int tos, String[] s, int[] se, int w, int l, boolean ww, boolean mc, boolean syn, boolean rex, boolean ts,
                    String fr, String to, int tsi, boolean donly) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj = d;
        searchrequest = sr;

        searchTerms = s;
        searchEntries = se;
        where = w;
        logical = l;
        wholeword = ww;
        matchcase = mc;
        desktoponly = donly;
        synonyms = syn;
        timesearch = ts;
        timestampindex = tsi;
        datefrom = fr;
        dateto = to;
        synonymsObj = sy;
        regex = rex;

        typeOfSearch = tos;

        parentDialog = parent;
        msgLabel = label;
        // show status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }
    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect
        //
        // when the searchEntries-array is not null, we usually have a filtered search request
        boolean filteredSearch = (searchEntries != null);
        // when we pass "null" as parameter for the entry-numbers of those entries
        // where the search should be applied to, we assume that we want to search
        // through the whole data. thus, we fill the arrays with all entrynumbers now...
        if (null == searchEntries) {
            searchEntries = new int[dataObj.getCount(Daten.ZKNCOUNT)];
            // go through all entries
            for (int cnt = 1; cnt <= dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                searchEntries[cnt - 1] = cnt;
            }
        }
        // when we have a search for entries within a certain period,
        // filter these entries
        if (timesearch) {
            // create new list for integer values
            List<Integer> intlist = new ArrayList<>();
            // go through all entries
            for (int cnt = 0; cnt < searchEntries.length; cnt++) {
                // get entrynumber
                int entrynumber = searchEntries[cnt];
                // get entries timestamp
                String[] timestamp = dataObj.getTimestamp(entrynumber);
                // if we have a timestamp, go on.
                if (timestamp != null) {
                    // create string that hold the created and edited date
                    String created = "";
                    String edited = "";
                    // try to get the substrings. we use a try/catch block here, because
                    // if one of the dates does not exist, the programm won't throw an error
                    // but just leave the strings empty...
                    try {
                        created = timestamp[0].substring(0, 6);
                        edited = timestamp[1].substring(0, 6);
                    } catch (IndexOutOfBoundsException e) {
                        // nothing here
                    }
                    // check which part of the timestamp is relevant, i.e. whether the entry's
                    // period is onyl relevant for the creation date, the last changed date or both
                    switch (timestampindex) {
                        case Constants.TIMESTAMP_CREATED:
                            // if we have no creation timestamp or the period-start is bigger
                            // than or the period-end is below our creaion date, then leave
                            if (created.isEmpty() || datefrom.compareTo(created) > 0 || dateto.compareTo(created) < 0) {
                                break;
                            }
                            // else add entry to searchlist
                            intlist.add(entrynumber);
                            break;
                        case Constants.TIMESTAMP_EDITED:
                            // if we have no edited timestamp or the period-start is bigger
                            // than or the period-end is below our edited date, then leave
                            if (edited.isEmpty() || datefrom.compareTo(edited) > 0 || dateto.compareTo(edited) < 0) {
                                break;
                            }
                            // else add entry to searchlist
                            intlist.add(entrynumber);
                            break;
                        case Constants.TIMESTAMP_BOTH:
                            // if we have no edited or creation timestamp or the period-start is bigger
                            // than or the period-end is below our edited or creation date, then leave
                            if (created.isEmpty() && edited.isEmpty()) {
                                break;
                            }
                            if ((datefrom.compareTo(created) > 0 || dateto.compareTo(created) < 0) && (datefrom.compareTo(edited) > 0 || dateto.compareTo(edited) < 0)) {
                                break;
                            }
                            // else add entry to searchlist
                            intlist.add(entrynumber);
                            break;
                    }
                }
            }
            // if we have no entries within this time period,
            // clear result-array and return null (leave task)
            if (intlist.size() < 1) {
                results = null;
                return null;
            }
            // create new array with entries that should be searched
            searchEntries = new int[intlist.size()];
            // copy all elements of the list to our searchEntries-array
            for (int cnt = 0; cnt < intlist.size(); cnt++) {
                searchEntries[cnt] = intlist.get(cnt);
            }
        }
        // save search time
        long nt = System.nanoTime();
        switch (typeOfSearch) {
            //
            // here starts a usual search request
            //
            case Constants.SEARCH_USUAL:
                // get the amount of entries
                int len = searchEntries.length;
                // init a linked list that temporarily stores the found entry-numbers
                List<Integer> finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // this variable counts the amount of matching search-results
                    // if we have logical-or-search, foundCounter finally has to be
                    // euqal to or greater than 1. Having logical-and-serach means, that foundCounter
                    // has to be equal to the amount of search terms.
                    foundCounter = 0;
                    // here starts the typical search for search terms
                    // the search for regular expressions can be found below...
                    if (!regex) {
                        // copy the original search-terms-array to a dummy-array. we do this
                        // because we want to remove found items from the array by setting that value
                        // to an empty string, so we avoid multiple found-results. if a searchterm "hello"
                        // has already been found in the keywords, and we have another searchterm "friend" to find
                        // (both together, i.e. logical-and), the search should not count another occurence of "hello"
                        // as second match.
                        dummysearchterms = new String[searchTerms.length];
                        // copy the values to the array...
                        for (int z = 0; z < searchTerms.length; z++) {
                            // check for case-sensitive search
                            if (matchcase) {
                                dummysearchterms[z] = searchTerms[z];
                            } // if not case-sensitive, transform to lower case
                            else {
                                dummysearchterms[z] = searchTerms[z].toLowerCase();
                            }
                        }
                        //
                        // here we want to search in keywords...
                        //
                        if ((where & Constants.SEARCH_KEYWORDS) != 0) {
                            findTerm(searchnr, Constants.SEARCH_KEYWORDS);
                        }
                        //
                        // here we want to search in authors...
                        //
                        if ((where & Constants.SEARCH_AUTHOR) != 0) {
                            findTerm(searchnr, Constants.SEARCH_AUTHOR);
                        }
                        //
                        // here we want to search in the content...
                        //
                        if ((where & Constants.SEARCH_CONTENT) != 0) {
                            findTerm(searchnr, Constants.SEARCH_CONTENT);
                        }
                        //
                        // here we want to search in the remarks...
                        //
                        if ((where & Constants.SEARCH_REMARKS) != 0) {
                            findTerm(searchnr, Constants.SEARCH_REMARKS);
                        }
                        //
                        // here we want to search in the titles...
                        //
                        if ((where & Constants.SEARCH_TITLE) != 0) {
                            findTerm(searchnr, Constants.SEARCH_TITLE);
                        }
                        //
                        // here we want to search the attachments' names
                        //
                        if ((where & Constants.SEARCH_LINKS) != 0) {
                            findTerm(searchnr, Constants.SEARCH_LINKS);
                        }
                        //
                        // now we have searched through all parts and can see whether this entry
                        // is a search-result or not...
                        //
                        // if we have logica-and-search, go on here...
                        if (Constants.LOG_AND == logical) {
                            // we then need to have at least the same amount of matches as
                            // the length of the searchterm-array
                            // if this is true, append entry-number to stringbuffer
                            if (foundCounter >= searchTerms.length) {
                                finalresults.add(searchnr);
                            }
                        } // in case we have logical-or-search...
                        else if (Constants.LOG_OR == logical) {
                            // ...we just need at least one found search term
                            // append entry-number to stringbuffer
                            if (foundCounter > 0) {
                                finalresults.add(searchnr);
                            }
                        } // in case we have logical-not-search...
                        else {
                            // if the user wants to exclude the search terms, we have a valid search
                            // result when no search term was found in that entry...
                            // append entry-number to stringbuffer
                            if (0 == foundCounter) {
                                finalresults.add(searchnr);
                            }
                        }
                    } // here we have the procedure when we search for regular expressions...
                    else {
                        // if we have a regular expression, we do not split the search term after each comma,
                        // but keep the whole expression as one search term... Thus, we only need the
                        // first index of that array, because usually the array should only contain one field
                        dummysearchterms = new String[searchTerms.length];
                        dummysearchterms[0] = searchTerms[0];
                        //
                        // here we want to search in keywords...
                        //
                        if ((where & Constants.SEARCH_KEYWORDS) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_KEYWORDS);
                        }
                        //
                        // here we want to search in authors...
                        //
                        if ((where & Constants.SEARCH_AUTHOR) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_AUTHOR);
                        }
                        //
                        // here we want to search in the content...
                        //
                        if ((where & Constants.SEARCH_CONTENT) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_CONTENT);
                        }
                        //
                        // here we want to search in the remarks...
                        //
                        if ((where & Constants.SEARCH_REMARKS) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_REMARKS);
                        }
                        //
                        // here we want to search in the titles...
                        //
                        if ((where & Constants.SEARCH_TITLE) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_TITLE);
                        }
                        //
                        // here we want to search the attachments' names
                        //
                        if ((where & Constants.SEARCH_LINKS) != 0) {
                            findRegExTerm(searchnr, Constants.SEARCH_LINKS);
                        }
                        // ...we just need at least one found search term
                        // append entry-number to stringbuffer
                        if (foundCounter > 0) {
                            finalresults.add(searchnr);
                        }
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // stop time
                double searchseconds = (double)(System.nanoTime() - nt) / 1000000000.0;
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // init the final results-array with the index-numbers of the found entries
                    results = new int[finalresults.size()];
                    // copy all string values to the final array
                    for (int cnt = 0; cnt < results.length; cnt++) {
                        results[cnt] = finalresults.get(cnt);
                    }
                    //
                    // Here we start preparing the description of the search-request. it is a
                    // combination of searchterms, where the user looked for and the logical combination
                    // of the search. finally, a datestamp is added so we always have a unique searchescription
                    // even if the user starts twice the same searchrequest.
                    //
                    // prepare search description
                    StringBuilder desc = new StringBuilder("");
                    StringBuilder ldesc = new StringBuilder("");
                    // when we have a filtered search, add description for this here
                    // furthermore, add the previous used search terms (from the original search
                    // that has been filtered) to the filter-search terms, so we have also the
                    // possibilty to highlight the terms from the previous search - and the new
                    // filter-search-terms
                    if (filteredSearch) {
                        // get the short-description of the current search
                        String d = searchrequest.getShortDescription(searchrequest.getCurrentSearch());
                        // find timestamp in that search
                        int pos = d.lastIndexOf("(");
                        // if we found it, set this as first text to the description
                        if (pos != -1) {
                            desc.append(resourceMap.getString("filteredSearchText")).append(" ").append(d.substring(pos)).append(", ");
                            ldesc.append(resourceMap.getString("filteredSearchText")).append(" ").append(d.substring(pos)).append(System.lineSeparator()).append(System.lineSeparator());
                        }
                        // create array list. here we will store the original search terms from
                        // the search that was filtered, and the new search term from the filter-request
                        List<String> newsearchterms = new ArrayList<>();
                        // get original search terms
                        String[] orisearchterms = searchrequest.getSearchTerms(searchrequest.getCurrentSearch());
                        // for (String s : orisearchterms) newsearchterms.add(s);
                        newsearchterms.addAll(Arrays.asList(orisearchterms));
                        // now add the new filter-search terms to that list as well
                        // for (String s : searchTerms) newsearchterms.add(s);
                        newsearchterms.addAll(Arrays.asList(searchTerms));
                        // and copy the "final" list back to the searchTerms-array
                        searchTerms = newsearchterms.toArray(new String[newsearchterms.size()]);
                    }
                    // when we have more than 3 search terms, truncate to three...
                    if (searchTerms.length > 3) {
                        // only copy first three searchterms
                        for (int cnt = 0; cnt < 3; cnt++) {
                            // retrieve each searchterm, so we can check for the length if it
                            String shortendterm = searchTerms[cnt];
                            // if search term is longer than 50 chars, truncate it...
                            if (shortendterm.length() > 50) {
                                shortendterm = shortendterm.substring(0, 50) + "...";
                            }
                            // and append search term to description
                            desc.append("\"").append(shortendterm).append("\", ");
                        }
                        // cut off last space and comma
                        desc.setLength(desc.length() - 2);
                        // add text, that there are even more searchterms, that have been let out here
                        desc.append(" ").append(resourceMap.getString("andMoreSearchterms")).append(", ");
                    } // else append all searchterms to the description
                    else {
                        // iterate searchterm-array
                        for (String st : searchTerms) {
                            // if search term is longer than 50 chars, truncate it...
                            if (st.length() > 50) {
                                st = st.substring(0, 50) + "...";
                            }
                            // and append it to description
                            desc.append("\"").append(st).append("\", ");
                        }
                    }
                    // here we prepare the long description
                    ldesc.append(resourceMap.getString("longdescLabelSearchTerms"));
                    for (String st : searchTerms) {
                        // if search term is longer than 90 chars, truncate it...
                        if (st.length() > 90) {
                            st = st.substring(0, 90) + "...";
                        }
                        ldesc.append(System.lineSeparator()).append("- ").append(st);
                    }
                    // for search log
                    StringBuilder anonymsearchtext = new StringBuilder(String.valueOf(searchTerms.length) + " search terms; ");
                    // search-options will be displayed in new line
                    ldesc.append(System.lineSeparator()).append("(");
                    // add the logical combination to the search description
                    switch (logical) {
                        // add short and long desctiption for the logical-and-search
                        case Constants.LOG_AND:
                            desc.append(resourceMap.getString("logAndText"));
                            ldesc.append(resourceMap.getString("logAndText"));
                            anonymsearchtext.append(resourceMap.getString("logAndText"));
                            break;
                        // add short and long desctiption for the logical-or-search
                        case Constants.LOG_OR:
                            desc.append(resourceMap.getString("logOrText"));
                            ldesc.append(resourceMap.getString("logOrText"));
                            anonymsearchtext.append(resourceMap.getString("logOrText"));
                            break;
                        // add short and long desctiption for the logical-not-search
                        case Constants.LOG_NOT:
                            desc.append(resourceMap.getString("logNotText"));
                            ldesc.append(resourceMap.getString("logNotText"));
                            anonymsearchtext.append(resourceMap.getString("logNotText"));
                            break;
                    }
                    desc.append(", ");
                    anonymsearchtext.append("; ");
                    // add new line for further search options
                    ldesc.append(")").append(System.lineSeparator()).append(System.lineSeparator());
                    // append where the user searched for the searchterms
                    desc.append(resourceMap.getString("searchWhere")).append(" ");
                    anonymsearchtext.append("searched in ");
                    ldesc.append(resourceMap.getString("searchWhere")).append(":").append(System.lineSeparator());
                    int wherecnt = 0;
                    // when we searched keywords, add to description that keywords where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_KEYWORDS) != 0) {
                        desc.append(resourceMap.getString("searchInKeywords")).append(", ");
                        anonymsearchtext.append(resourceMap.getString("searchInKeywords")).append(", ");
                        ldesc.append("- ").append(resourceMap.getString("searchInKeywords")).append(System.lineSeparator());
                        wherecnt++;
                    }
                    // when we searched authors, add to description that authors where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_AUTHOR) != 0) {
                        desc.append(resourceMap.getString("searchInAuthors")).append(", ");
                        anonymsearchtext.append(resourceMap.getString("searchInAuthors")).append(", ");
                        ldesc.append("- ").append(resourceMap.getString("searchInAuthors")).append(System.lineSeparator());
                        wherecnt++;
                    }
                    // when we searched content, add to description that content where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_CONTENT) != 0) {
                        desc.append(resourceMap.getString("searchInContent")).append(", ");
                        anonymsearchtext.append(resourceMap.getString("searchInContent")).append(", ");
                        ldesc.append("- ").append(resourceMap.getString("searchInContent")).append(System.lineSeparator());
                        wherecnt++;
                    }
                    // when we searched titles, add to description that titles where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_TITLE) != 0) {
                        // long description contains all search-areas
                        ldesc.append("- ").append(resourceMap.getString("searchInTitle")).append(System.lineSeparator());
                        // short description only 3 parts
                        if (wherecnt < 3) {
                            desc.append(resourceMap.getString("searchInTitle")).append(", ");
                            anonymsearchtext.append(resourceMap.getString("searchInTitle")).append(", ");
                        }
                        wherecnt++;
                    }
                    // when we searched attachments, add to description that attachments where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_LINKS) != 0) {
                        // long description contains all search-areas
                        ldesc.append("- ").append(resourceMap.getString("searchInLinks")).append(System.lineSeparator());
                        // short description only 3 parts
                        if (wherecnt < 3) {
                            desc.append(resourceMap.getString("searchInLinks")).append(", ");
                            anonymsearchtext.append(resourceMap.getString("searchInLinks")).append(", ");
                        }
                        wherecnt++;
                    }
                    // when we searched attachments' contents, add to description that attachments' contents where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_LINKCONTENT) != 0) {
                        // long description contains all search-areas
                        ldesc.append("- ").append(resourceMap.getString("searchInLinksContent")).append(System.lineSeparator());
                        // short description only 3 parts
                        if (wherecnt < 3) {
                            desc.append(resourceMap.getString("searchInLinksContent")).append(", ");
                            anonymsearchtext.append(resourceMap.getString("searchInLinksContent")).append(", ");
                        }
                        wherecnt++;
                    }
                    // when we searched remarks, add to description that remarks where one of the
                    // entries fields that have been searched for the find term...
                    if ((where & Constants.SEARCH_REMARKS) != 0) {
                        // long description contains all search-areas
                        ldesc.append("- ").append(resourceMap.getString("searchInRemarks")).append(System.lineSeparator());
                        if (wherecnt < 3) {
                            desc.append(resourceMap.getString("searchInRemarks")).append(", ");
                            anonymsearchtext.append(resourceMap.getString("searchInRemarks")).append(", ");
                        }
                        wherecnt++;
                    }
                    // cut off last space and comma
                    if (desc.length() > 2) {
                        desc.setLength(desc.length() - 2);
                    }
                    if (anonymsearchtext.length() > 2) {
                        anonymsearchtext.setLength(anonymsearchtext.length() - 2);
                    }
                    // when we have more than 3 search areas, add this to description
                    if (wherecnt > 3) {
                        // add text, that there are even more search-areas, that have been let out here
                        desc.append(" ");
                        desc.append(resourceMap.getString("andMoreSearchwhere"));
                        anonymsearchtext.append(" ");
                        anonymsearchtext.append(resourceMap.getString("andMoreSearchwhere"));
                    }
                    // append a time-string, so we always have a unique search-description,
                    // even if the user searches twice for the same searchterms
                    DateFormat df = new SimpleDateFormat("kkmmss");
                    desc.append(" (").append(df.format(new Date())).append(")");
                    // append matchcase/wholeword
                    if (wholeword || matchcase || synonyms) {
                        ldesc.append(System.lineSeparator()).append(resourceMap.getString("longDescSearchOptions"));
                        if (wholeword) {
                            ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescWholeWord"));
                            anonymsearchtext.append("; whole word");
                        }
                        if (matchcase) {
                            ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescMatchCase"));
                            anonymsearchtext.append("; match case");
                        }
                        if (synonyms) {
                            ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescSynonyms"));
                            anonymsearchtext.append("; synonym search");
                        }
                    }
                    // copy description to string
                    searchLabel = desc.toString();
                    longdesc = ldesc.toString();
                    // log search time
                    Constants.zknlogger.log(Level.INFO, "Search Request: {0}; Duration: {1} seconds.", new Object[]{anonymsearchtext.toString(), String.format("%.03f", searchseconds)});
                    // add all search request data and search results to our search-request-class
                    // but only, if we don't want to add the data to the desktop instead of having
                    // a searchrequest
                    if (!desktoponly) {
                        searchrequest.addSearch(searchTerms, where, logical, wholeword, matchcase, synonyms, regex, results, searchLabel, longdesc);
                    }
                } else {
                    results = null;
                }
                break;

            //
            // here starts a search for entries without authors
            //
            case Constants.SEARCH_NO_AUTHORS:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // when no author found, we have a match
                    if (null == dataObj.getAuthors(searchnr)) {
                        finalresults.add(searchnr);
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchNoAuthors"), Constants.SEARCH_AUTHOR);
                } else {
                    results = null;
                }

                break;

        //
            // here starts a search for entries without keywords
            //
            case Constants.SEARCH_NO_KEYWORDS:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // when no author found, we have a match
                    if (null == dataObj.getKeywords(searchnr)) {
                        finalresults.add(searchnr);
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchNoKeywords"), Constants.SEARCH_KEYWORDS);
                } else {
                    results = null;
                }

                break;

            //
            // here starts a search for first-level follower (luhmann) entries
            //
            case Constants.SEARCH_TOP_LEVEL_LUHMANN:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // check whether entry is a follower and has a first-level parent
                    if (dataObj.isTopLevelLuhmann(searchnr)) {
                        // check if entry is not already in searchresults
                        // if not, add search result
                        if (!finalresults.contains(searchnr)) {
                            finalresults.add(searchnr);
                        }
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchInLuhmannParents"), Constants.SEARCH_LUHMANN);
                } else {
                    results = null;
                }

                break;

            //
            // here starts a search for follower (luhmann) entries, i.e.
            // entries that do have a parent-trail entry
            //
            case Constants.SEARCH_IS_LUHMANN_PARENT:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // find first parent
                    int lp = dataObj.findParentlLuhmann(searchnr, true);
                    // check whether entry is a follower and has a first-level parent
                    if (lp != -1) {
                        // check if entry is not already in searchresults
                        // if not, add search result
                        if (!finalresults.contains(searchnr)) {
                            finalresults.add(searchnr);
                        }
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchInLuhmann"), Constants.SEARCH_LUHMANN);
                } else {
                    results = null;
                }

                break;
                
            //
            // here starts a search for entries without remarks
            //
            case Constants.SEARCH_NO_REMARKS:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // when no author found, we have a match
                    if (dataObj.getRemarks(searchnr).isEmpty()) {
                        finalresults.add(searchnr);
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchNoRemarks"), Constants.SEARCH_REMARKS);
                } else {
                    results = null;
                }

                break;

        //
            // here starts a search for entries *with* remarks
            //
            case Constants.SEARCH_WITH_REMARKS:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();

                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // when no author found, we have a match
                    if (!dataObj.getRemarks(searchnr).isEmpty()) {
                        finalresults.add(searchnr);
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }

                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchWithRemarks"), Constants.SEARCH_REMARKS);
                } else {
                    results = null;
                }

                break;

        //
            // here starts a search for entries with attachments
            //
            case Constants.SEARCH_WITH_ATTACHMENTS:
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // when no author found, we have a match
                    if (!dataObj.getAttachments(searchnr).isEmpty()) {
                        finalresults.add(searchnr);
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, resourceMap.getString("searchWithAttachments"), Constants.SEARCH_LINKS);
                } else {
                    results = null;
                }

                break;

        //
            // here starts a search for entries with/out ratings
            //
            case Constants.SEARCH_WITH_RATINGS:
            case Constants.SEARCH_WITHOUT_RATINGS:
                // get description
                String descr = "";
                // set description for a search for entries that have been rated
                if (Constants.SEARCH_WITH_RATINGS == typeOfSearch) {
                    descr = resourceMap.getString("searchRatings");
                }
                // set description for a search for entries that have *not* been rated
                if (Constants.SEARCH_WITHOUT_RATINGS == typeOfSearch) {
                    descr = resourceMap.getString("searchNoRatings");
                }
                // get the amount of entries
                len = searchEntries.length;
                // init a stringbuffer that temporarily stores the found entry-numbers
                finalresults = new ArrayList<>();
                // iterate all entries where the search should be applied to...
                // this may differ. a search request from the main-window usually searches through
                // all entries, while a filter of search results only is applied to certain entries.
                // therefor, we store all relevant entry-numbers for the search in an integer-array
                for (int counter = 0; counter < len; counter++) {
                    // get the number of the entry which we want to search through...
                    int searchnr = searchEntries[counter];
                    // check whether we want to look for entries that have been rated
                    if (Constants.SEARCH_WITH_RATINGS == typeOfSearch) {
                        // if we have a rating-count higher than 0, entry has been rated, so found
                        if (dataObj.getZettelRatingCount(searchnr) > 0) {
                            finalresults.add(searchnr);
                        }
                    } // check whether we want to look for entries that have *not* been rated
                    else if (Constants.SEARCH_WITHOUT_RATINGS == typeOfSearch) {
                        // if we have a rating-count that equals 0, entry has been *not* rated, so found
                        if (dataObj.getZettelRatingCount(searchnr) == 0) {
                            finalresults.add(searchnr);
                        }
                    }
                    // update progressbar
                    setProgress(counter, 0, len);
                }
                // finally, check whether we have any searchresults at all...
                if (finalresults.size() > 0) {
                    // create search results array, create search description and
                    // add search results to the search-data-class.
                    prepareExtraSearchResults(finalresults, descr, Constants.SEARCH_RATINGS);
                } else {
                    results = null;
                }

                break;

        //
            // here starts a search for entries with attachments
            //
            case Constants.SEARCH_WITH_CREATED_TIME:
            case Constants.SEARCH_WITH_EDITED_TIME:
                // since the filtering for timestamp was already made in the beginning of the method,
                // we already have our final results in the search entries
                results = searchEntries;
                // finally, check whether we have any searchresults at all...
                if (results != null) {
                    // prepare search description
                    StringBuilder desc = new StringBuilder("");
                    int ts;
                    // create search description
                    desc.append(resourceMap.getString("searchWithTimeMsg"));
                    desc.append(" ").append(searchTerms[0]).append(" ");
                    desc.append(resourceMap.getString("searchWithTimeTo"));
                    desc.append(" ").append(searchTerms[1]).append(" ");

                    if (Constants.SEARCH_WITH_CREATED_TIME == typeOfSearch) {
                        // append text for no authors
                        desc.append(resourceMap.getString("searchWithTimeCreated"));
                        ts = Constants.SEARCH_TIMESTAMP_CREATED;
                    } else {
                        // append text for no authors
                        desc.append(resourceMap.getString("searchWithTimeEdited"));
                        ts = Constants.SEARCH_TIMESTAMP_EDITED;
                    }
                    // append a time-string, so we always have a unique search-description,
                    // even if the user searches twice for the same searchterms
                    desc.append(" (");
                    DateFormat df = new SimpleDateFormat("kkmmss");
                    desc.append(df.format(new Date()));
                    desc.append(")");
                    // copy description to string
                    longdesc = searchLabel = desc.toString();
                    // add all search request data and search results to our search-request-class
                    // but only, if we don't want to add the data to the desktop instead of having
                    // a searchrequest
                    if (!desktoponly) {
                        searchrequest.addSearch(searchTerms, ts, Constants.LOG_OR, false, false, false, false, results, searchLabel, longdesc);
                    }
                }
                break;
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
        // save latest search results.
        searchrequest.setCurrentSearchResults(results);
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }

    /**
     * This method searches through all entries and tries to find the regular
     * expression that is stored in the first field of the search term array
     * {@link #dummysearchterms dummysearchterms}.
     * <br><br>
     * If a regular expression (i.e. the find term) was found, the
     * {@link #foundCounter foundCounter} is increased, indicating that the
     * entry with the index-number {@code searchnr} belongs to the search
     * results. {@code searchnr} is added to the search results in the
     * {@link #startSearch() startSearch()} task.
     *
     * @param searchnr the entry-number of that entry that is currently searched
     * @param type the type in which fields of an entry the findterm is searched
     * for. An ORed value of following constants:<br>
     * - CConstant.SEARCH_KEYWORDS<br>
     * - CConstant.SEARCH_AUTHOR<br>
     * - CConstant.SEARCH_TITLE<br>
     * - CConstant.SEARCH_REMARKS<br>
     * - CConstant.SEARCH_CONTENT
     */
    private void findRegExTerm(int searchnr, int type) {
        // first check, whether the searchterm is not empty
        // if we have a regular expression, we do not split the search term after each comma,
        // but keep the whole expression as one search term... Thus, we only need the
        // first index of that array, because usually the array should only contain one field
        if (!dummysearchterms[0].isEmpty()) {
            // prepare searchterms.
            String[] sterms = null;
            // here we retrieve the searchterms when we look for authors
            if (Constants.SEARCH_AUTHOR == type) {
                sterms = dataObj.getAuthors(searchnr);
            }
            // here we retrieve the searchterms when we look for keywords
            if (Constants.SEARCH_KEYWORDS == type) {
                sterms = dataObj.getKeywords(searchnr);
            }
            if (Constants.SEARCH_TITLE == type) {
                sterms = new String[]{dataObj.getZettelTitle(searchnr)};
            }
            if (Constants.SEARCH_CONTENT == type) {
                sterms = new String[]{dataObj.getCleanZettelContent(searchnr)};
            }
            if (Constants.SEARCH_REMARKS == type) {
                sterms = new String[]{dataObj.getRemarks(searchnr)};
            }
            if (Constants.SEARCH_LINKS == type) {
                // get the content of an entry
                List<Element> attachments = dataObj.getAttachments(searchnr);
                List<String> content = new ArrayList<>();
                // check whether we have any attachments at all
                if (attachments != null) {
                    // create iterator
                    Iterator<Element> i = attachments.iterator();
                    // iterate all attachments
                    while (i.hasNext()) {
                        content.add(i.next().getText());
                    }
                }
                sterms = content.toArray(new String[content.size()]);
            }
            // check whether we have any search terms
            if (sterms != null) {
                try {
                    // create a pattern from the first search term. if it fails, go on
                    // to the catch-block, else contiue here.
                    Pattern p = Pattern.compile(dummysearchterms[0]);
                    // iterate the array of all found search terms...
                    for (String loop : sterms) {
                        // now we know we have a valid regular expression. we now want to
                        // retrieve all matching groups
                        Matcher m = p.matcher(loop);
                        // check whether we have found anything at all...
                        if (m.find()) {
                            // increase found counter
                            foundCounter++;
                            return;
                        }
                    }
                } catch (PatternSyntaxException e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        }
    }


    /**
     * This method searches for all searchterms in the entry {@code searchnr} in a given part {@code type}.
     * <br><br>
     * If a find term was found, the {@link #foundCounter foundCounter}
     * is increased, indicating that the entry with the index-number {@code searchnr} belongs to the
     * search results. {@code searchnr} is added to the search results in the {@link #startSearch() startSearch()}
     * task.
     *
     * @param searchnr the entrynumber of the entry that should be searched
     * @param type the type, i.e. which part of the entry is relevant for the search. An ORed value
     * of following constants:<br>
     * - CConstant.SEARCH_KEYWORDS<br>
     * - CConstant.SEARCH_AUTHOR<br>
     * - CConstant.SEARCH_TITLE<br>
     * - CConstant.SEARCH_REMARKS<br>
     * - CConstant.SEARCH_CONTENT
     */
    private void findTerm(int searchnr, int type) {
        // go through all search terms
        for (int count = 0; count < dummysearchterms.length; count++) {
            // first check, whether the searchterm is not empty
            if (!dummysearchterms[count].isEmpty()) {
                // init variable
                String[] synline = null;
                // when we have synonyms included in the search, prepare the searchterm
                // therefor, retrieve the synonyms for the current search term
                if (synonyms) {
                    // retrieve the synonymline, where the current searchterm might be an
                    // index-word or any related synonym
                    synline = synonymsObj.getSynonymLineFromAny(dummysearchterms[count], matchcase);
                }
                // if there are synonyms for the current searchterm...
                if (null == synline) {
                    synline = new String[]{dummysearchterms[count]};
                }
                // go through all searchterms, including synonyms for the search term
                for (String synline1 : synline) {
                    // if we have whole-word-search, identify search results by
                    // comparing the keyword-index-numbers
                    if (wholeword) {
                        // prepare found-indicator
                        boolean somethingfound = false;
                        // when we have a whole-word-search, we can use an already existing
                        // method to check whether this keyword exists in an entry or not
                        if (Constants.SEARCH_KEYWORDS == type) {
                            somethingfound = dataObj.existsInKeywords(synline1, searchnr, matchcase);
                        } else if (Constants.SEARCH_AUTHOR == type) {
                            somethingfound = dataObj.existsInAuthors(synline1, searchnr);
                        } else {
                            // create emptry string
                            String content = "";
                            // now, depending on the part of entry we want to look, retrieve the related content
                            if (Constants.SEARCH_TITLE == type) {
                                content = dataObj.getZettelTitle(searchnr);
                            }
                            if (Constants.SEARCH_CONTENT == type) {
                                content = dataObj.getCleanZettelContent(searchnr);
                            }
                            if (Constants.SEARCH_REMARKS == type) {
                                content = dataObj.getRemarks(searchnr);
                            }
                            if (Constants.SEARCH_LINKS == type) {
                                // get the content of an entry
                                List<Element> attachments = dataObj.getAttachments(searchnr);
                                // check whether we have any attachments at all
                                if (attachments != null) {
                                    // create iterator
                                    Iterator<Element> i = attachments.iterator();
                                    // iterate all attachments
                                    while (i.hasNext()) {
                                        content = content + " " + i.next().getText();
                                    }
                                }
                            }

    //                        synline1 = "\\b"+synline1+"\\b";
    //                        // when the find & replace is *not* case-sensitive, set regular expression
    //                        // to ignore the case...
    //                        if (!matchcase) synline1 = "(?i)"+synline1;
    //                        // the final findterm now might look like this:
    //                        // "(?i)\\b<findterm>\\b", in case we ignore case and have whole word search
    //                        try {
    //                            // create a pattern from the first search term. if it fails, go on
    //                            // to the catch-block, else contiue here.
    //                            Pattern p = Pattern.compile(synline1);
    //                            // now we know we have a valid regular expression. we now want to
    //                            // retrieve all matching groups
    //                            Matcher m = p.matcher(content);
    //                            somethingfound = m.find();
    //                        }
    //                        catch (PatternSyntaxException ex) {
    //                            
    //                        }

                            // split the content at each new word-boundary - i.e. we have a whole-word-search here
                            String[] sterms = content.split("\\b");
                            // when we are looking for title, content or remarks, the variable "sterms" is not null
                            // if we were looking for keywords or authors, sterms is null, so this part
                            // is ignored...
                            if (sterms != null) {
                                // get search term
                                String stsearch = synline1;
                                // iterate the array of keyword- or author-strings of that entry
                                for (String loop : sterms) {
                                    // if the search is not case-sensitive, convert to lowercase
                                    if (!matchcase) {
                                        loop = loop.toLowerCase();
                                    }
                                    // when we found something, tell that our found indicator
                                    if (loop.equals(stsearch)) {
                                        // set found-indicator to true
                                        somethingfound = true;
                                        // and leave loop
                                        break;
                                    }
                                }
                            }
                        }
                        // when we found something, increase foundcounter, so we know how many matches we have
                        // furthermore, delete the searchterm, so we don't have multiple match-counts for just
                        // a single searchterm
                        if (somethingfound) {
                            // increase found counter
                            foundCounter++;
                            // clear found search term
                            dummysearchterms[count] = "";
                            // do we have logical OR or NOT? if yes, we
                            // can leave now
                            if (Constants.LOG_OR == logical || Constants.LOG_NOT == logical) {
                                return;
                            }
                            // leave synonym-loop, we don't need to look for further synomyns
                            break;
                        }
                    } else {
                        // prepare searchterms.
                        String content = null;
                        // here we retrieve the searchterms when we look for authors
                        if (Constants.SEARCH_AUTHOR == type) {
                            content = Arrays.toString(dataObj.getAuthors(searchnr));
                        }
                        // here we retrieve the searchterms when we look for keywords
                        if (Constants.SEARCH_KEYWORDS == type) {
                            content = Arrays.toString(dataObj.getKeywords(searchnr));
                        }
                        // now, depending on the part of entry we want to look, retrieve the related content
                        if (Constants.SEARCH_TITLE == type) {
                            content = dataObj.getZettelTitle(searchnr);
                        }
                        if (Constants.SEARCH_CONTENT == type) {
                            content = dataObj.getCleanZettelContent(searchnr);
                        }
                        if (Constants.SEARCH_REMARKS == type) {
                            content = dataObj.getRemarks(searchnr);
                        }
                        if (Constants.SEARCH_LINKS == type) {
                            // get the content of an entry
                            List<Element> attachments = dataObj.getAttachments(searchnr);
                            // check whether we have any attachments at all
                            if (attachments != null) {
                                // create iterator
                                Iterator<Element> i = attachments.iterator();
                                // iterate all attachments
                                while (i.hasNext()) {
                                    content = content + " " + i.next().getText();
                                }
                            }
                        }
                        // when we are looking for keywords or authors, the variable "sterms" is not null
                        // if we were looking for content, title or remarks, sterms is null, so this part
                        // is ignored...
                        if (content != null) {
                            // if the search is not case-sensitive, convert to lowercase
                            if (!matchcase) {
                                content = content.toLowerCase();
                            }
                            // if the content contains the searchterm, set foundindicator to true
                            if (content.contains(synline1)) {
                                // increase found counter
                                foundCounter++;
                                // clear found search term
                                dummysearchterms[count] = "";
                                // do we have logical OR or NOT? if yes, we
                                // can leave now
                                if (Constants.LOG_OR == logical || Constants.LOG_NOT == logical) {
                                    return;
                                }
                                // leave loop, we don't need to look for further synomyns
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param finalresults
     * @param descwhat
     * @param searchwhat
     */
    private void prepareExtraSearchResults(List<Integer> finalresults, String descwhat, int searchwhat) {
        // init the final results-array with the index-numbers of the found entries
        results = new int[finalresults.size()];
        // copy all string values to the final array
        for (int cnt = 0; cnt < results.length; cnt++) {
            results[cnt] = finalresults.get(cnt);
        }
        // prepare search description
        StringBuilder desc = new StringBuilder("");
        // append text for no authors
        desc.append(descwhat);
        // append a time-string, so we always have a unique search-description,
        // even if the user searches twice for the same searchterms
        desc.append(" (");
        DateFormat df = new SimpleDateFormat("kkmmss");
        desc.append(df.format(new Date()));
        desc.append(")");
        // copy description to string
        longdesc = searchLabel = desc.toString();
        // add all search request data and search results to our search-request-class
        // but only, if we don't want to add the data to the desktop instead of having
        // a searchrequest
        if (!desktoponly) {
            searchrequest.addSearch(new String[]{""}, searchwhat, Constants.LOG_NOT, true, true, false, false, results, searchLabel, longdesc);
        }
    }
}

