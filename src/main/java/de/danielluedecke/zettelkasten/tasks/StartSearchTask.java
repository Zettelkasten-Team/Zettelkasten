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

package de.danielluedecke.zettelkasten.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.tasks.search.SearchEntriesHelper;
import de.danielluedecke.zettelkasten.tasks.search.SearchTaskOptions;
import de.danielluedecke.zettelkasten.util.Constants;

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
	 * A reference to the SearchRequests-class which stores the searchterms and
	 * other search settings like case-sensitive search, where to search in and so
	 * on...
	 */
	private final SearchRequests searchrequest;

	/**
	 * Options for a search task.
	 */
	private final SearchTaskOptions searchTaskOptions;

	/**
	 *
	 */
	private final Synonyms synonymsObj;
	/**
	 * get the strings for file descriptions from the resource map
	 */
	private final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
			.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
			.getResourceMap(StartSearchTask.class);
	/**
	 * The search terms, passed as an array.
	 */
	private String[] searchTerms = null;
	/**
	 * The entries which should be searched.
	 */
	private int[] searchEntries = null;
	/**
	 * the class CStartSearch is not only used for search requests, but also for
	 * finding entries that should be added to the desktop or as luhmann-numbers. If
	 * we don't need the searchresults to be added to the {@link SearchRequests}
	 * class, we have to set this parameter to "true", false if we want to have a
	 * "real" search.
	 */
	private final boolean desktoponly;
	/**
	 * Search for note created/edited within a specific time period.
	 */
	private boolean timesearch = false;
	/**
	 * Whether tags should be removed from entry content before searching the entry.
	 * Increases speed, however, some words may not be found (which have tags inside
	 * a word to emphasize a word part, like <i>Zettel</i>kasten.
	 */
	private String datefrom = "";
	private String dateto = "";
	private int timestampType = -1;
	/**
	 * The entry-numbers of the search-result, i.e. the found entries of a
	 * search-request as integer-array.
	 */
	private int[] results;
	/**
	 * A short description of the search, which will be set in the
	 * CSearchResult-frame's combobox, so the user can identify what kind of search
	 * is related to the search results
	 */
	private String searchLabel;
	/**
	 * A long description of the search, which will be set in the
	 * CSearchResult-frame's combobox, so the user can identify what kind of search
	 * is related to the search results
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
	 * @param parent              the parent-window
	 * @param d                   a reference to the Daten class
	 * @param sr                  a reference to the CSearchRequesrs class
	 * @param searchType          Type of Search (in entries, entries w/o keywords
	 *                            etc.)
	 * @param inputSearchTerms    a string-array containing search terms
	 * @param inputSearchEntries  an integer array containing the entry-numbers of
	 *                            those entries where the search should be applied
	 *                            to
	 * @param inputWhere          where the search should be applied to, i.e. search
	 *                            within content, keywords, authors etc.
	 * @param inputLogicalType    the logical-combination of the search
	 * @param inputWholeword      pass true, if the search should find whole words
	 *                            only
	 * @param inputMatchCase      whether the search is case sensitive (true) or not
	 *                            (false)
	 * @param syn                 whether the search should include synonyms or not
	 * @param accentInsensitive   whether the search should be accent insensitive
	 * @param inputRegex          whether the search terms contain regular
	 *                            expressions or not
	 * @param inputTimeSearch     whether the user requested a time-search, i.e. a
	 *                            search for entries that were created or changed
	 *                            within a certain period
	 * @param timesearchFrom      the start of the period, when a timesearch is
	 *                            requested. format: "yymmdd".
	 * @param timesearchTo        the end of the period, when a timesearch is
	 *                            requested. format: "yymmdd".
	 * @param inputTimesearchType the timestampindex, which indicate whether the
	 *                            user wants to search only for entries within a
	 *                            period of <i>creation</i> date (0), of
	 *                            <i>edited</i> date (1) or both (2).
	 * @param inputDesktopOnly    whether the search should open the
	 *                            CSearchResults-frame (false), or whether the
	 *                            search-results are used for other purposes, like
	 *                            e.g. putting the results to the desktop (true)
	 * @param shouldRemoveTags    Whether tags should be removed from entry content
	 *                            before searching the entry. Increases speed,
	 *                            however, some words may not be found (which have
	 *                            tags inside a word to emphasize a word part, like
	 *                            <i>Zettel</i>kasten.
	 */
	StartSearchTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
			Daten d, SearchRequests sr, Synonyms sy, int searchType, String[] inputSearchTerms,
			int[] inputSearchEntries, int inputWhere, int inputLogicalType, boolean inputWholeword,
			boolean inputMatchCase, boolean syn, boolean inputAccentInsensitive, boolean inputRegex,
			boolean inputTimeSearch, String timesearchFrom, String timesearchTo, int inputTimesearchType,
			boolean inputDesktopOnly, boolean shouldRemoveTags) {
		// Runs on the EDT. Copy GUI state that
		// doInBackground() depends on from parameters
		// to ImportFileTask fields, here.
		super(app);
		dataObj = d;
		searchrequest = sr;

		searchTaskOptions = new SearchTaskOptions(searchType, inputWhere, inputLogicalType, inputWholeword,
				inputMatchCase, syn, inputAccentInsensitive, inputRegex, shouldRemoveTags);

		searchTerms = inputSearchTerms;
		searchEntries = inputSearchEntries;
		desktoponly = inputDesktopOnly;
		timesearch = inputTimeSearch;
		timestampType = inputTimesearchType;
		datefrom = timesearchFrom;
		dateto = timesearchTo;
		synonymsObj = sy;

		typeOfSearch = searchType;

		parentDialog = parent;
		msgLabel = label;
		// show status text
		msgLabel.setText(resourceMap.getString("msg1"));
	}

	private void filterSearchEntriesByTime() {
		if (!timesearch) {
			return;
		}
		List<Integer> acceptedEntries = new ArrayList<>();
		for (int cnt = 0; cnt < searchEntries.length; cnt++) {
			int entryNumber = searchEntries[cnt];

			String[] entryTimestamp = dataObj.getTimestamp(entryNumber);
			if (entryTimestamp == null) {
				// Entry with missing timestamp is filtered.
				continue;
			}

			// Init created and edited date from entryTimestamp.
			String created = "";
			String edited = "";
			try {
				created = entryTimestamp[0].substring(0, 6);
				edited = entryTimestamp[1].substring(0, 6);
			} catch (IndexOutOfBoundsException e) {
				// Leave the strings empty.
			}

			// check which part of the timestamp is relevant, i.e. whether the entry's
			// period is only relevant for the creation date, the last changed date or both
			switch (timestampType) {
			case Constants.TIMESTAMP_CREATED:
				// if we have no creation timestamp or the period-start is bigger
				// than or the period-end is below our creaion date, then leave
				if (created.isEmpty() || datefrom.compareTo(created) > 0 || dateto.compareTo(created) < 0) {
					break;
				}
				// else add entry to searchlist
				acceptedEntries.add(entryNumber);
				break;
			case Constants.TIMESTAMP_EDITED:
				// if we have no edited timestamp or the period-start is bigger
				// than or the period-end is below our edited date, then leave
				if (edited.isEmpty() || datefrom.compareTo(edited) > 0 || dateto.compareTo(edited) < 0) {
					break;
				}
				// else add entry to searchlist
				acceptedEntries.add(entryNumber);
				break;
			case Constants.TIMESTAMP_BOTH:
				// if we have no edited or creation timestamp or the period-start is bigger
				// than or the period-end is below our edited or creation date, then leave
				if (created.isEmpty() && edited.isEmpty()) {
					break;
				}
				if ((datefrom.compareTo(created) > 0 || dateto.compareTo(created) < 0)
						&& (datefrom.compareTo(edited) > 0 || dateto.compareTo(edited) < 0)) {
					break;
				}
				// else add entry to searchlist
				acceptedEntries.add(entryNumber);
				break;
			}
		}

		// Overwrite searchEntries with acceptedEntries.
		searchEntries = new int[acceptedEntries.size()];
		for (int cnt = 0; cnt < acceptedEntries.size(); cnt++) {
			searchEntries[cnt] = acceptedEntries.get(cnt);
		}
	}

	private List<Integer> finalResultsBySearchTerms() {
		SearchEntriesHelper searchEntriesHelper = new SearchEntriesHelper(dataObj, synonymsObj, searchTaskOptions);
		List<Integer> finalresults = new ArrayList<>();
		for (int i = 0; i < searchEntries.length; i++) {
			int entryNumber = searchEntries[i];

			if (searchEntriesHelper.entryMatchesSearchTerms(entryNumber, searchTerms)) {
				finalresults.add(entryNumber);
			}

			// Update progress bar.
			setProgress(i, 0, searchEntries.length);
		}
		return finalresults;
	}

	private void createSearchRequest(boolean filteredSearch, double searchDurationInSeconds) {
		//
		// Here we start preparing the description of the search-request. it is a
		// combination of searchterms, where the user looked for and the logical
		// combination
		// of the search. finally, a datestamp is added so we always have a unique
		// searchescription
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
				desc.append(resourceMap.getString("filteredSearchText")).append(" ").append(d.substring(pos))
						.append(", ");
				ldesc.append(resourceMap.getString("filteredSearchText")).append(" ").append(d.substring(pos))
						.append(System.lineSeparator()).append(System.lineSeparator());
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
		switch (searchTaskOptions.logical) {
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
		if ((searchTaskOptions.where & Constants.SEARCH_KEYWORDS) != 0) {
			desc.append(resourceMap.getString("searchInKeywords")).append(", ");
			anonymsearchtext.append(resourceMap.getString("searchInKeywords")).append(", ");
			ldesc.append("- ").append(resourceMap.getString("searchInKeywords")).append(System.lineSeparator());
			wherecnt++;
		}
		// when we searched authors, add to description that authors where one of the
		// entries fields that have been searched for the find term...
		if ((searchTaskOptions.where & Constants.SEARCH_AUTHOR) != 0) {
			desc.append(resourceMap.getString("searchInAuthors")).append(", ");
			anonymsearchtext.append(resourceMap.getString("searchInAuthors")).append(", ");
			ldesc.append("- ").append(resourceMap.getString("searchInAuthors")).append(System.lineSeparator());
			wherecnt++;
		}
		// when we searched content, add to description that content where one of the
		// entries fields that have been searched for the find term...
		if ((searchTaskOptions.where & Constants.SEARCH_CONTENT) != 0) {
			desc.append(resourceMap.getString("searchInContent")).append(", ");
			anonymsearchtext.append(resourceMap.getString("searchInContent")).append(", ");
			ldesc.append("- ").append(resourceMap.getString("searchInContent")).append(System.lineSeparator());
			wherecnt++;
		}
		// when we searched titles, add to description that titles where one of the
		// entries fields that have been searched for the find term...
		if ((searchTaskOptions.where & Constants.SEARCH_TITLE) != 0) {
			// long description contains all search-areas
			ldesc.append("- ").append(resourceMap.getString("searchInTitle")).append(System.lineSeparator());
			// short description only 3 parts
			if (wherecnt < 3) {
				desc.append(resourceMap.getString("searchInTitle")).append(", ");
				anonymsearchtext.append(resourceMap.getString("searchInTitle")).append(", ");
			}
			wherecnt++;
		}
		// when we searched attachments, add to description that attachments where one
		// of the
		// entries fields that have been searched for the find term...
		if ((searchTaskOptions.where & Constants.SEARCH_LINKS) != 0) {
			// long description contains all search-areas
			ldesc.append("- ").append(resourceMap.getString("searchInLinks")).append(System.lineSeparator());
			// short description only 3 parts
			if (wherecnt < 3) {
				desc.append(resourceMap.getString("searchInLinks")).append(", ");
				anonymsearchtext.append(resourceMap.getString("searchInLinks")).append(", ");
			}
			wherecnt++;
		}
		// when we searched attachments' contents, add to description that attachments'
		// contents where one of the
		// entries fields that have been searched for the find term...
		if ((searchTaskOptions.where & Constants.SEARCH_LINKCONTENT) != 0) {
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
		if ((searchTaskOptions.where & Constants.SEARCH_REMARKS) != 0) {
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
		if (searchTaskOptions.wholeword || searchTaskOptions.matchcase || searchTaskOptions.synonyms
				|| searchTaskOptions.accentInsensitive) {
			ldesc.append(System.lineSeparator()).append(resourceMap.getString("longDescSearchOptions"));
			if (searchTaskOptions.wholeword) {
				ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescWholeWord"));
				anonymsearchtext.append("; whole word");
			}
			if (searchTaskOptions.matchcase) {
				ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescMatchCase"));
				anonymsearchtext.append("; match case");
			}
			if (searchTaskOptions.synonyms) {
				ldesc.append(System.lineSeparator()).append("- ").append(resourceMap.getString("longDescSynonyms"));
				anonymsearchtext.append("; synonym search");
			}
			if (searchTaskOptions.accentInsensitive) {
				ldesc.append(System.lineSeparator()).append("- ")
						.append(resourceMap.getString("longDescAccentInsensitive"));
				anonymsearchtext.append("; accent insensitive search");
			}
		}
		// copy description to string
		searchLabel = desc.toString();
		longdesc = ldesc.toString();
		// log search time
		Constants.zknlogger.log(Level.INFO, "Search Request: {0}; Duration: {1} seconds (avg. {2} seconds per note).",
				new Object[] { anonymsearchtext.toString(), String.format("%.03f", searchDurationInSeconds),
						String.format("%.03f", searchDurationInSeconds / searchEntries.length) });
		// add all search request data and search results to our search-request-class
		// but only, if we don't want to add the data to the desktop instead of having
		// a searchrequest
		if (!desktoponly) {
			searchrequest.addSearch(searchTerms, searchTaskOptions.where, searchTaskOptions.logical,
					searchTaskOptions.wholeword, searchTaskOptions.matchcase, searchTaskOptions.synonyms,
					searchTaskOptions.accentInsensitive, searchTaskOptions.regex, results, searchLabel, longdesc);
		}
	}

	/**
	 * Search entries by term or regex. Selected entries are put in results. If
	 * nothing found, results is set to null.
	 * 
	 * @param filteredSearch
	 */
	private void doUsualSearch(boolean filteredSearch) {
		long searchStartTimeInNanoSeconds = System.nanoTime();
		List<Integer> finalresults = finalResultsBySearchTerms();
		double searchDurationInSeconds = (double) (System.nanoTime() - searchStartTimeInNanoSeconds) / 1000000000.0;

		if (finalresults.size() == 0) {
			// Found nothing.
			results = null;
			return;
		}

		// Overwrite results with finalresults.
		results = new int[finalresults.size()];
		for (int cnt = 0; cnt < finalresults.size(); cnt++) {
			results[cnt] = finalresults.get(cnt);
		}

		// Update searchrequest.
		createSearchRequest(filteredSearch, searchDurationInSeconds);
	}

	private void doSearchForEntriesWithoutAuthors() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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
	}

	private void doSearchForEntriesWithoutKeywords() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchNoKeywords"),
					Constants.SEARCH_KEYWORDS);
		} else {
			results = null;
		}

	}

	/**
	 * First-level follower (luhmann) entries
	 */
	private void doSearchForTopLevelLuhman() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchInLuhmannParents"),
					Constants.SEARCH_LUHMANN);
		} else {
			results = null;
		}

	}

	private void doSearchForLuhmanParent() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a string buffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
		for (int counter = 0; counter < len; counter++) {
			// get the number of the entry which we want to search through...
			int searchnr = searchEntries[counter];
			// find first parent
			int lp = dataObj.findParentLuhmann(searchnr, true);
			// check whether entry is a follower and has a first-level parent
			if (lp != -1) {
				// check if entry is not already in search results
				// if not, add search result
				if (!finalresults.contains(searchnr)) {
					finalresults.add(searchnr);
				}
			}
			// update progress bar
			setProgress(counter, 0, len);
		}
		// finally, check whether we have any search results at all...
		if (finalresults.size() > 0) {
			// create search results array, create search description and
			// add search results to the search-data-class.
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchInLuhmann"), Constants.SEARCH_LUHMANN);
		} else {
			results = null;
		}
	}

	private void doSearchForAnyLuhman() {
		// retrieve all note sequence IDs
		List<Integer> finalresults = dataObj.getAllLuhmannNumbers();
		// finally, check whether we have any searchresults at all...
		if (finalresults != null && finalresults.size() > 0) {
			// create search results array, create search description and
			// add search results to the search-data-class.
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchInLuhmann"), Constants.SEARCH_LUHMANN);
		} else {
			results = null;
		}

	}

	private void doSearchForEntriesWithoutManualLinks() {
		// init result array
		List<Integer> finalresults = new ArrayList<>();
		// get all manual link IDs
		List<Integer> alllinksresult = dataObj.getAllManualLinks();
		// check if we have any values
		if (alllinksresult != null && alllinksresult.size() > 0) {
			// if yes, we need to remove these from all search entries
			for (int i : searchEntries) {
				// is current note ID a manual link?
				if (!alllinksresult.contains(i)) {
					// if not, add it
					finalresults.add(i);
				}
			}
		} else {
			// if we don't have any manual links, all notes are
			// part of the search result
			for (int i : searchEntries) {
				finalresults.add(i);
			}
		}

		// finally, check whether we have any searchresults at all...
		if (finalresults.size() > 0) {
			// create search results array, create search description and
			// add search results to the search-data-class.
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchNoManLinks"),
					Constants.SEARCH_LUHMANN);
		} else {
			results = null;
		}
	}

	private void doSearchForEntriesWithoutRemarks() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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

	}

	private void doSearchForEntriesWithRemarks() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();

		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchWithRemarks"),
					Constants.SEARCH_REMARKS);
		} else {
			results = null;
		}

	}

	private void doSearchForEntriesWithAttachments() {
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
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
			prepareExtraSearchResults(finalresults, resourceMap.getString("searchWithAttachments"),
					Constants.SEARCH_LINKS);
		} else {
			results = null;
		}

	}

	private void doSearchBasedOnRating(boolean shouldHaveRating) {
		// get description
		String descr = "";
		if (shouldHaveRating) {
			descr = resourceMap.getString("searchRatings");
		} else {
			descr = resourceMap.getString("searchNoRatings");
		}
		// get the amount of entries
		int len = searchEntries.length;
		// init a stringbuffer that temporarily stores the found entry-numbers
		List<Integer> finalresults = new ArrayList<>();
		// iterate all entries where the search should be applied to...
		// this may differ. a search request from the main-window usually searches
		// through
		// all entries, while a filter of search results only is applied to certain
		// entries.
		// therefor, we store all relevant entry-numbers for the search in an
		// integer-array
		for (int counter = 0; counter < len; counter++) {
			// get the number of the entry which we want to search through...
			int searchnr = searchEntries[counter];
			// check whether we want to look for entries that have been rated
			if (shouldHaveRating) {
				// if we have a rating-count higher than 0, entry has been rated, so found
				if (dataObj.getZettelRatingCount(searchnr) > 0) {
					finalresults.add(searchnr);
				}
			} else {
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
	}

	private void doSearchBasedOnTime(boolean byCreatedTime) {
		// since the filtering for timestamp was already made in the beginning of the
		// method,
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

			if (byCreatedTime) {
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
				searchrequest.addSearch(searchTerms, /* where= */ts, /* logical= */Constants.LOG_OR,
						/* wholeword= */false, /* matchcase= */false, /* synonyms= */false,
						/* accentInsensitive= */false, /* regex= */false, results, searchLabel, longdesc);
			}
		}
	}

	@Override
	protected Object doInBackground() {
		// Your Task's code here. This method runs
		// on a background thread, so don't reference
		// the Swing GUI from here.
		// prevent task from processing when the file path is incorrect

		final boolean filteredSearch = searchEntries != null;

		// If searchEntries is missing, initialize it with all entries available.
		if (searchEntries == null) {
			searchEntries = new int[dataObj.getCount(Daten.ZKNCOUNT)];
			for (int cnt = 1; cnt <= dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
				searchEntries[cnt - 1] = cnt;
			}
		}

		filterSearchEntriesByTime();
		if (searchEntries.length == 0) {
			// If filterSearchEntriesByTime filtered everything, return nothing.
			results = null;
			return null;
		}

		switch (typeOfSearch) {
		case Constants.SEARCH_USUAL:
			doUsualSearch(filteredSearch);
			break;
		case Constants.SEARCH_NO_AUTHORS:
			doSearchForEntriesWithoutAuthors();
			break;
		case Constants.SEARCH_NO_KEYWORDS:
			doSearchForEntriesWithoutKeywords();
			break;
		case Constants.SEARCH_TOP_LEVEL_LUHMANN:
			doSearchForTopLevelLuhman();
			break;
		case Constants.SEARCH_IS_LUHMANN_PARENT:
			doSearchForLuhmanParent();
			break;
		case Constants.SEARCH_IS_ANY_LUHMANN:
			doSearchForAnyLuhman();
			break;
		case Constants.SEARCH_WITHOUT_MANUAL_LINKS:
			doSearchForEntriesWithoutManualLinks();
			break;
		case Constants.SEARCH_NO_REMARKS:
			doSearchForEntriesWithoutRemarks();
			break;
		case Constants.SEARCH_WITH_REMARKS:
			doSearchForEntriesWithRemarks();
			break;
		case Constants.SEARCH_WITH_ATTACHMENTS:
			doSearchForEntriesWithAttachments();
			break;
		case Constants.SEARCH_WITH_RATINGS:
			doSearchBasedOnRating(true);
			break;
		case Constants.SEARCH_WITHOUT_RATINGS:
			doSearchBasedOnRating(false);
			break;
		case Constants.SEARCH_WITH_CREATED_TIME:
			doSearchBasedOnTime(true);
			break;
		case Constants.SEARCH_WITH_EDITED_TIME:
			doSearchBasedOnTime(false);
			break;
		}

		// results is populated in the switch block above.
		return null;
	}

	@Override
	protected void succeeded(Object result) {
		// Runs on the EDT. Update the GUI based on
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
			searchrequest.addSearch(/* searchTerms= */new String[] { "" }, /* where= */searchwhat,
					/* logical= */Constants.LOG_NOT, /* wholeword= */true, /* matchcase= */true, /* synonyms= */false,
					/* accentInsensitive= */false, /* regex= */false, results, searchLabel, longdesc);
		}
	}
}
