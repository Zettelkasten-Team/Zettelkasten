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

import java.util.LinkedList;

/**
 *
 * @author Luedeke
 */
public class TasksData {
    /**
     * Needed to store information when data was replaced, see ReplaceTask for more details
     */
    private int totalreplacements = -1;
    /**
     * Needed to store information when data was replaced, see ReplaceTask for more details
     */
    private String replacemessage = "";
    /**
     *
     */
    private String importmessage = "";
    /**
     *
     */
    private boolean isImportOk = false;
    /**
     *
     */
    private boolean isExportOk = false;
    /**
     * 
     */
    private boolean showExportOkMessage = true;
    /**
     * 
     */
    private LinkedList<Object[]> linkedvalues = null;
    private String webcontent = "";
    /**
     * Need to store global information, given from the {@code KeywordSuggestionsTask}. See {@code NewEntry} frame for
     * further information.
     */
    private LinkedList<String> remainingKeywords = null;
    public static final int REMAINING_KW = 1;
    /**
     * Need to store global information, given from the {@code KeywordSuggestionsTask}. See {@code NewEntry} frame for
     * further information.
     */
    private LinkedList<String> newKeywords = null;
    public static final int NEW_KW = 2;
    /**
     * Stores global information from the {@code ReplaceTask}, the number of replaced
     * occurences.
     *
     * @param msg the final message with the results from the {@code ReplaceTask}.
     */
    public void setReplaceMessage(String msg) {
        replacemessage = msg;
    }
    /**
     * Stores global information from the {@code ReplaceTask}, the number of replaced
     * occurences.
     *
     * @return the final message with the results from the {@code ReplaceTask}.
     */
    public String getReplaceMessage() {
        return replacemessage;
    }
    /**
     * 
     * @param cnt 
     */
    public void setReplaceCount(int cnt) {
        totalreplacements = cnt;
    }
    /**
     * 
     * @return 
     */
    public int getReplaceCount() {
        return totalreplacements;
    }
    /**
     * 
     * @return 
     */
    public LinkedList<Object[]> getLinkedValues() {
        return linkedvalues;
    }
    /**
     * 
     * @param val 
     */
    public void setLinkedValues(LinkedList<Object[]> val) {
        linkedvalues = val;
    }
    /**
     * Stores global information from the {@code ImportTask}, the message of
     * possible errors while importing data.
     *
     * @param msg the final message with the results from the {@code ImportTask}.
     */
    public void setImportMessage(String msg) {
        importmessage = msg;
    }
    /**
     * Stores global information from the {@code ImportTask}, the message of
     * possible errors while importing data.
     *
     * @return the final message with the results from the {@code ImportTask}.
     */
    public String getImportMessage() {
        return importmessage;
    }
    /**
     * 
     * @param val 
     */
    public void setImportOk(boolean val) {
        isImportOk = val;
    }
    /**
     * 
     * @return 
     */
    public boolean isImportOk() {
        return isImportOk;
    }
    /**
     * 
     * @param val 
     */
    public void setExportOk(boolean val) {
        isExportOk = val;
    }
    /**
     * 
     * @return 
     */
    public boolean isExportOk() {
        return isExportOk;
    }
    /**
     * 
     * @param val 
     */
    public void setShowExportOkMessage(boolean val) {
        showExportOkMessage = val;
    }
    /**
     * 
     * @return 
     */
    public boolean showExportOkMessage() {
        return showExportOkMessage;
    }
    /**
     * 
     * @param list
     * @param whichlist 
     */
    public void setKeywordSuggestionList(LinkedList<String> list, int whichlist) {
        switch (whichlist) {
            case REMAINING_KW: remainingKeywords = list; break;
            case NEW_KW: newKeywords = list; break;
        }
    }
    /**
     * 
     * @param whichlist
     * @return 
     */
    public LinkedList<String> getKeywordSuggesionList(int whichlist) {
        LinkedList<String> retval = null;
        switch (whichlist) {
            case REMAINING_KW: retval = remainingKeywords; break;
            case NEW_KW: retval = newKeywords; break;
        }
        return retval;
    }
}
