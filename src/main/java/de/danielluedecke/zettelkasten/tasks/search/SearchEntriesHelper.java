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
package de.danielluedecke.zettelkasten.tasks.search;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;

/**
 * SearchEntries is responsible for searching entries.
 * 
 * Main user is the StartSearchTask class, which handles the UI aspects of the
 * SearchTask as well.
 *
 */
public class SearchEntriesHelper {
	/**
	 * daten is responsible for the entries data.
	 */
	private final Daten daten;

	/**
	 * synonymsData is responsible for the synonyms data.
	 */
	private final Synonyms synonymsData;

	/**
	 * options is a simple structure with the options to use in the search.
	 */
	private final SearchTaskOptions options;

	public SearchEntriesHelper(Daten d, Synonyms sy, SearchTaskOptions inputOptions) {
		daten = d;
		synonymsData = sy;
		options = inputOptions;
	}

	/**
	 * This method is responsible for handling synonyms and logical operations (AND,
	 * OR and NOT) of the search task.
	 *
	 * @param entryNumber the entry number of the entry that should be searched
	 */
	public boolean entryMatchesSearchTerms(int entryNumber, String[] searchTerms) {
		for (String searchTerm : searchTerms) {
			if (searchTerm.isEmpty()) {
				continue;
			}

			String[] termAndSynonyms = null;
			if (options.synonyms) {
				termAndSynonyms = synonymsData.getSynonymLineFromAny(searchTerm, options.matchcase);
			}
			if (termAndSynonyms == null) {
				// If no synonyms, initialize it to searchTerm.
				termAndSynonyms = new String[] { searchTerm };
			}

			boolean anySynonymFound = false;
			for (String termToBeFound : termAndSynonyms) {
				String processedTermToBeFound = termToBeFound;
				if (!options.regex) {
					// Convert the termToBeFound here to be case insensitive and/or accent
					// insensitive if not regex.
					if (!options.matchcase) {
						processedTermToBeFound = processedTermToBeFound.toLowerCase();
					}
					if (options.accentInsensitive) {
						processedTermToBeFound = StringUtils.stripAccents(processedTermToBeFound);
					}
				}

				boolean found = entryMatchesSearchTerm(entryNumber, processedTermToBeFound);
				if (found) {
					// No need to look at the other synonyms when one is found.
					anySynonymFound = true;
					break;
				}
			}
			if (anySynonymFound && (options.logical == Constants.LOG_OR)) {
				// No need to look at other search terms. If we found one, we return true for an
				// OR.
				return true;
			}
			if (anySynonymFound && (options.logical == Constants.LOG_NOT)) {
				// No need to look at other search terms. If we found one, we return false for a
				// NOT.
				return false;
			}
			if (!anySynonymFound && (options.logical == Constants.LOG_AND)) {
				// We already know. If we didn't find, we return false for an AND.
				return false;
			}
		}
		if (options.logical == Constants.LOG_OR) {
			// It didn't find any.
			return false;
		}
		if (options.logical == Constants.LOG_NOT) {
			// It didn't find any.
			return true;
		}
		// For LOG_AND, if we got here, we found every term.
		return true;
	}

	/**
	 * stringHasWholeword returns true if searchTerm is a wholeword in content. Both
	 * input should be already converted according to matchcase and
	 * accentInsensitive.
	 */
	private boolean stringHasWholeword(String content, String searchTerm) {
		// Split the content at each new word-boundary.
		String[] existingWords = content.split("\\b");
		if (existingWords == null) {
			return false;
		}

		for (String existingWord : existingWords) {
			if (StringUtils.equals(existingWord, searchTerm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if any string in strings matches regexString.
	 */
	private boolean anyStringMatchesRegex(String[] strings, String regexString) {
		try {
			Pattern p = Pattern.compile(regexString);
			for (String loop : strings) {
				Matcher m = p.matcher(loop);
				if (m.find()) {
					return true;
				}
			}
		} catch (PatternSyntaxException e) {
			System.out.println(e.getLocalizedMessage());
		}
		return false;
	}

	private boolean stringMatchesSearchTerm(String content, String searchTerm) {
		if (options.regex) {
			// Modifiers like matchcase and accentInsensitive does not affect regex search.
			String[] sterms = new String[] { content };
			return anyStringMatchesRegex(sterms, searchTerm);
		}

		// Convert the content to be case insensitive and/or accent insensitive.
		// searchTerm is converted in entryMatchesSearchTerms().
		if (!options.matchcase) {
			content = content.toLowerCase();
		}
		if (options.accentInsensitive) {
			content = StringUtils.stripAccents(content);
		}

		if (options.wholeword) {
			// Keywords can be checked for word match directly.
			return stringHasWholeword(content, searchTerm);
		}

		return StringUtils.contains(content, searchTerm);
	}

	private boolean entryKeywordsMatchesSearchTerm(int entryNumber, String searchTerm) {
		if (options.wholeword) {
			// Keywords can be checked for word match directly.
			return daten.existsInKeywords(searchTerm, entryNumber, options.matchcase);
		}
		if (options.regex) {
			String[] sterms = daten.getKeywords(entryNumber);
			return anyStringMatchesRegex(sterms, searchTerm);
		}
		String content = Arrays.toString(daten.getKeywords(entryNumber));
		if (!options.matchcase) {
			return StringUtils.containsIgnoreCase(content, searchTerm);
		} else {
			return StringUtils.contains(content, searchTerm);
		}
	}

	private boolean entryAuthorMatchesSearchTerm(int entryNumber, String searchTerm) {

		if (options.wholeword) {
			// Keywords can be checked for word match directly.
			return daten.existsInAuthors(searchTerm, entryNumber);
		}
		if (options.regex) {
			String[] sterms = daten.getAuthors(entryNumber);
			return anyStringMatchesRegex(sterms, searchTerm);
		}
		String content = Arrays.toString(daten.getAuthors(entryNumber));
		if (!options.matchcase) {
			return StringUtils.containsIgnoreCase(content, searchTerm);
		} else {
			return StringUtils.contains(content, searchTerm);
		}
	}

	/**
	 * entryMatchesSearchTerm is responsible for looking at the different locations
	 * searchTerm can be.
	 * 
	 * @param entryNumber the entry number of the entry that should be searched
	 * @param searchTerm  searchTerm to find. It can be a regex.
	 * @return
	 */
	private boolean entryMatchesSearchTerm(int entryNumber, String searchTerm) {
		if ((options.where & Constants.SEARCH_KEYWORDS) != 0) {
			boolean match = entryKeywordsMatchesSearchTerm(entryNumber, searchTerm);
			if (match) {
				return true;
			}
		}
		if ((options.where & Constants.SEARCH_AUTHOR) != 0) {
			boolean match = entryAuthorMatchesSearchTerm(entryNumber, searchTerm);
			if (match) {
				return true;
			}
		}

		// Other types need to be broken down into words.
		if ((options.where & Constants.SEARCH_TITLE) != 0) {
			String content = daten.getZettelTitle(entryNumber);
			boolean match = stringMatchesSearchTerm(content, searchTerm);
			if (match) {
				return true;
			}
		}
		if ((options.where & Constants.SEARCH_CONTENT) != 0) {
			String content = cleanZettelContent(entryNumber);
			boolean match = stringMatchesSearchTerm(content, searchTerm);
			if (match) {
				return true;
			}
		}
		if ((options.where & Constants.SEARCH_REMARKS) != 0) {
			String content = daten.getRemarks(entryNumber);
			boolean match = stringMatchesSearchTerm(content, searchTerm);
			if (match) {
				return true;
			}
		}
		if ((options.where & Constants.SEARCH_LINKS) != 0) {
			String content = "";
			// get the content of an entry
			List<Element> attachments = daten.getAttachments(entryNumber);
			// check whether we have any attachments at all
			if (attachments != null) {
				// create iterator
				Iterator<Element> i = attachments.iterator();
				// iterate all attachments
				while (i.hasNext()) {
					content = content + " " + i.next().getText();
				}
			}

			boolean match = stringMatchesSearchTerm(content, searchTerm);
			if (match) {
				return true;
			}
		}
		return false;
	}

	private String cleanZettelContent(int nr) {
		String content = daten.getZettelContent(nr);
		if (options.removeTags) {
			String dummy = "";
			if (content != null && !content.isEmpty()) {
				dummy = content.replaceAll("\\[k\\]", "").replaceAll("\\[f\\]", "").replaceAll("\\[u\\]", "")
						.replaceAll("\\[q\\]", "").replaceAll("\\[d\\]", "").replaceAll("\\[c\\]", "")
						.replaceAll("\\[code\\]", "").replaceAll("\\[sup\\]", "").replaceAll("\\[sub\\]", "")
						.replaceAll("\\[/k\\]", "").replaceAll("\\[/f\\]", "").replaceAll("\\[/u\\]", "")
						.replaceAll("\\[/q\\]", "").replaceAll("\\[/d\\]", "").replaceAll("\\[/c\\]", "")
						.replaceAll("\\[/code\\]", "").replaceAll("\\[/sup\\]", "").replaceAll("\\[/sub\\]", "")
						.replaceAll("\\[color ([^\\[]*)\\](.*?)\\[/color\\]", "$2")
						.replaceAll("\\[font ([^\\[]*)\\](.*?)\\[/font\\]", "$2")
						.replaceAll("\\[h ([^\\[]*)\\](.*?)\\[/h\\]", "$2")
						.replaceAll("\\[m ([^\\[]*)\\](.*?)\\[/m\\]", "$2").replaceAll("\\[n\\](.*?)\\[/n\\]", "$1")
						.replaceAll("\\[l\\](.*?)\\[/l\\]", "$1").replaceAll("\\[\\*\\](.*?)\\[/\\*\\]", "- $1\n")
						.replaceAll("\\[tc\\](.*?)\\[/tc\\]", "$1").replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "$1")
						.replaceAll("___(.*?)___", "$1").replaceAll("__(.*?)__", "$1")
						.replaceAll("\\*\\*(.*?)\\*\\*", "$1").replaceAll("_(.*?)_", "$1")
						.replaceAll("\\*(.*?)\\*", "$1").replaceAll("---(.*?)---", "$1");
			}
			return dummy;
		}
		return content;
	}

}
