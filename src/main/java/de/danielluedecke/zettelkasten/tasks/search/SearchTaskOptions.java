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

import de.danielluedecke.zettelkasten.util.Constants;

public class SearchTaskOptions {
	/**
	 * This indicates where we want to look or where the search should be apllied
	 * to. We can search for the searchterms within keywords, entry-conten or title,
	 * authors etc.
	 *
	 * See {@link Constants} for more details.
	 */
	public int where;
	/**
	 * Indicates the logical-combination of search-terms. See constants below for
	 * more details.
	 */
	public int logical;
	/**
	 * Indicates whether a search should look for whole words only, or if a match is
	 * also given when the search terms is only part of a found word.
	 */
	public boolean wholeword;
	/**
	 * Indicates whether the search is case sensitive (true) or not (false).
	 */
	public boolean matchcase;
	/**
	 * whether the search should include synonyms or not.
	 */
	public boolean synonyms = false;
	/**
	 * whether the search should be accent insensitive.
	 */
	public boolean accentInsensitive = false;
	/**
	 * Regex search or not.
	 */
	public boolean regex = false;
	/**
	 * Whether tags should be removed from entry content before searching the entry.
	 * Increases speed, however, some words may not be found (which have tags inside
	 * a word to emphasize a word part, like [k]Zettel[/k]kasten.
	 */
	public boolean removeTags = true;

	public SearchTaskOptions(int searchType, int inputWhere, int inputLogicalType, boolean inputWholeword,
			boolean inputMatchCase, boolean syn, boolean inputAccentInsensitive, boolean inputRegex,
			boolean shouldRemoveTags) {
		where = inputWhere;
		logical = inputLogicalType;
		wholeword = inputWholeword;
		matchcase = inputMatchCase;
		synonyms = syn;
		accentInsensitive = inputAccentInsensitive;
		regex = inputRegex;
		removeTags = shouldRemoveTags;
	}
}
