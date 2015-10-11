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

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import de.danielluedecke.zettelkasten.util.Tools;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Luedeke
 */
public class KeywordSuggestionsTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Daten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     *
     */
    private final Synonyms synonymsObj;
    /**
     *
     */
    private final Settings settingsObj;
    /**
     * Indicates whether we have an extended quick input setting. when this setting is activated,
     * keyword-values consisting of more than one word are splitted, and the occurence of each
     * keyword-part is searched in the main entries content. if found, the related keyword also
     * counts as match.
     */
    private final int extendedQuickInput;
    /**
     * Indictaes which of the four steps for the quick input is currently processed.
     */
    private final int quickstep;
    /**
     * The entry-text that contains the content of the entry, where we want to find related keywords
     * for.
     */
    private final String entrytext;
    /**
     * The keywords the user selected in the first step of the quick input. needed to retrieve the
     * keywords for the seconde step, since in this 2. step we want to have all related keywords of
     * those keywords that have been selected in the first step.
     */
    private final LinkedList<String> selectedKeywords;
    /**
     * A List containing the remaining keywords that haven't been retrieved during the past quick
     * input steps. having this list, we don't need to look through the whole keyword list on the
     * one hand, on the other hand we prevent finding double keywords.
     */
    private LinkedList<String> remainingKeywords;
    /**
     * The final results of the quick input. Contains the keywords that have to be set to the
     * keyword list in the new entry-frame, when this task is finished.
     */
    private LinkedList<String> newKeywords;
    /**
     * Similar to {@link #selectedKeywords selectedKeywords}. We need the keywords of the first step
     * for the third step. See task-comments below.
     */
    private final LinkedList<String> fromFirstStep;

    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    
    private long nt;

    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(KeywordSuggestionsTask.class);

    /**
     *
     * @param app
     * @param parent
     * @param label
     * @param td
     * @param d
     * @param syn
     * @param st
     * @param eqi
     * @param step
     * @param sel
     * @param rest
     * @param ffs
     * @param t
     */
    KeywordSuggestionsTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
            TasksData td, Daten d, Synonyms syn, Settings st,
            int eqi, int step, LinkedList<String> sel, LinkedList<String> rest, LinkedList<String> ffs, String t) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        extendedQuickInput = eqi;
        dataObj = d;
        taskinfo = td;
        synonymsObj = syn;
        settingsObj = st;
        quickstep = step;
        selectedKeywords = sel;
        remainingKeywords = rest;
        fromFirstStep = ffs;
        entrytext = t.toLowerCase();
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        
        // save search time
        nt = System.nanoTime();

        // definition of variables
        Iterator<String> i;
        // create list for the new keywords
        newKeywords = new LinkedList<>();

        switch (quickstep) {
            /*
             * Here starts the first step of the quickinput.
             *
             * This step takes all keywords of the data-file and looks for their occurence
             * in the text and title that have been edited/entered by the user. if the
             * text contains a keyword, it is added to the list (newKeywords) that will be
             * displayed in the jList-component.
             *
             * Furthermore, the entered text will be split at each new word, and then this
             * step checks whether any word longer than 3 chars is part of a keyword. if we
             * found such a keyword, it is also added to the list.
             *
             * Finally, when we have the extended search activated, all keywords are also split
             * (some "keyword-values" may consist of several words, thus looking for the keyword
             * as a whole string differs from looking for single keyword-parts). If any keyword-part
             * occurs in the text, the keyword will be added to the final list.
             */
            case 1:
                // create the list with remaining keywords
                remainingKeywords = new LinkedList<>();
                // copy all keywords to the remaining list
                for (int cnt = 1; cnt <= dataObj.getCount(Daten.KWCOUNT); cnt++) {
                    String kw = dataObj.getKeyword(cnt);
                    // leave out empty keyword-elements
                    if (!kw.isEmpty()) {
                        remainingKeywords.add(kw);
                    }
                }
                // first check whether we have any keyword-occurences in the entry text
                // therefore, we need an iterator for the list
                i = remainingKeywords.iterator();
                // go through list
                while (i.hasNext()) {
                    // get keyword
                    String kw = i.next();
                    // create array with keywords, and - if necessary - related synonyms
                    String[] synline = null;
                    // retrieve synonyms if option is set
                    if (settingsObj.getSearchAlwaysSynonyms()) {
                        synline = synonymsObj.getSynonymLine(kw, false);
                    }
                    // if we don't have any synonyms, put only keyword in the array
                    if (null == synline) {
                        synline = new String[]{kw};
                    }
                    // iterate all keywords and related synonyms
                    for (String s : synline) {
                        // if we find this keyword in the text, add it to the
                        // list of new keywords
                        if (StringUtils.indexOf(entrytext, s.toLowerCase()) != -1) {
                            newKeywords.add(kw);
                            // and remove the item from the remaining keywords
                            i.remove();
                            // leave loop
                            break;
                        }
                    }
                }

                // when we have set the extended quickinput-option, we have to split
                // the keywords into its single words and check whether we have more keyword
                // occurences, i.e. even parts of a complete keyword-value are found in the entry text
                if (Settings.QUICK_INPUT_MORE == extendedQuickInput) {
                    // create new iterator again
                    i = remainingKeywords.iterator();
                    // go through list with remaining keywords
                    while (i.hasNext()) {
                        // get keyword
                        String kw = i.next();
                        // init found indicator
                        boolean found = false;
                        // get the parts of the keyword and its associated synonyms
                        String[] synline = Tools.getKeywordsAndSynonymsParts(settingsObj, synonymsObj, kw, false);
                        // iterate all keywords and related synonyms
                        for (String s : synline) {
                            // only keyword-parts of more than 3 chars are recognized
                            if ((s.length() > 3) && (StringUtils.indexOf(entrytext, s.toLowerCase()) != -1)) {
                                found = true;
                                // leave loop
                                break;
                            }
                        }
                        // if we find this keyword-part in the text, add it to the
                        // list of new keywords
                        if (found) {
                            newKeywords.add(kw);
                            // and remove the item from the remaining keywords
                            i.remove();
                        }
                    }
                }
                // check whether the extended-quickinput should deliver less results
                if (extendedQuickInput != Settings.QUICK_INPUT_LESS) {
                    // now split entry text at end of each word and look
                    // whether any word of the entry text is part of any of the remaining keywords
                    String[] words = entrytext.split("\\b");
                    int wlength = words.length;
                    // go through all words
                    for (int cnt = 0; cnt < wlength; cnt++) {
                        // get word of the entry text
                        String e = words[cnt].toLowerCase();
                        // only check for occurences when the word of the entry's text is longer than 3 chars
                        if (e.length() > 3) {
                            // create iterator again
                            i = remainingKeywords.iterator();
                            // go through list of remaining keywords
                            while (i.hasNext()) {
                                // get keyword
                                String kw = i.next();
                                // create array with keywords, and - if necessary - related synonyms
                                String[] synline = null;
                                // retrieve synonyms if option is set
                                if (settingsObj.getSearchAlwaysSynonyms()) {
                                    synline = synonymsObj.getSynonymLine(kw, false);
                                }
                                // if we don't have any synonyms, put only keyword in the array
                                if (null == synline) {
                                    synline = new String[]{kw};
                                }
                                // iterate all keywords and related synonyms
                                for (String s : synline) {
                                    // when the word is inside the keyword, add keyword to the list
                                    if (s.contains(e)) {
                                        newKeywords.add(kw);
                                        // and remove the item from the remaining keywords
                                        i.remove();
                                        // leave loop
                                        break;
                                    }
                                }
                            }

                        }
                        // update progressbar
                        setProgress(cnt, 0, wlength);
                    }
                }
                // sort the keywordlist
                if (newKeywords != null && !newKeywords.isEmpty()) {
                    Collections.sort(newKeywords, new Comparer());
                }
                // we're done with the first step
                break;

            /*
             * Here starts the second step of the quickinput.
             *
             * In this step, we get the selected keywords from the first step, i.e. the user
             * selections that have been chosen for the keywordlist. now we search in each entry
             * whether one of the selected keywords exists in that entry. if yes, we retrieve all
             * other keywords of that entry and add them to the final list (newKeywords).
             *
             * Thus, we now have all "related" keywords of the previous selected keywords.
             */
            case 2:
                // only proceed if we have any selected keywords at all
                if ((selectedKeywords != null) && (selectedKeywords.size() > 0)) {
                    // get count of entries
                    int slength = dataObj.getCount(Daten.ZKNCOUNT);
                    // go through all entries and check for the existence of the selected keyword
                    for (int cnt = 1; cnt <= slength; cnt++) {
                        // create iterator
                        i = selectedKeywords.iterator();
                        // go through all selected keywords
                        while (i.hasNext()) {
                            // get each selected keyword
                            String sel = i.next();
                            // if the selected keyword exists in that entry, copy related
                            // keywords to the new list
                            if (dataObj.existsInKeywords(sel, cnt, false)) {
                                // get the entries keywords
                                String[] kws = dataObj.getKeywords(cnt);
                                // when we have any keywords, go on
                                if (kws != null) {
                                    // go through all related keywords
                                    for (String kw : kws) {
                                        // if the requested keyword exists in the list of
                                        // remaining keywords...
                                        if (remainingKeywords.contains(kw)) {
                                            // add it to the new keywords list
                                            newKeywords.add(kw);
                                            // and remove it from the remaining keywords
                                            remainingKeywords.remove(kw);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        // update progressbar
                        setProgress(cnt, 0, slength);
                    }
                    // sort the keywordlist
                    if (newKeywords != null && !newKeywords.isEmpty()) {
                        Collections.sort(newKeywords, new Comparer());
                    }
                }
                // we're done with the first step
                break;

            /*
             * Here starts the third step of the quickinput.
             *
             * This step combines step one and two: first, all keywords that appear in the text
             * and all words from the content that appear in any keyword, those keywords are added
             * to a list. we have done this in step one, so we simply pass the list from step one
             * as parameter and use it here (fromFirstStep).
             *
             * Now, from these keywords, all related keywords are added to the final list. In step
             * two, we retrieved the related keywords of the users selection, now we retrieve the
             * related keywords of all found keywords of step 1.
             */
            case 3:
                // only proceed if we have any keywords from the first at all
                if ((fromFirstStep != null) && (fromFirstStep.size() > 0)) {
                    // get count of entries
                    int slength = dataObj.getCount(Daten.ZKNCOUNT);
                    // go through all entries and check for the existence of the keywords
                    // that we found in the first step
                    for (int cnt = 1; cnt <= slength; cnt++) {
                        // create iterator
                        i = fromFirstStep.iterator();
                        // go through all selected keywords
                        while (i.hasNext()) {
                            // get each keyword
                            String ffs = i.next();
                            // if the keyword from the first step exists in that entry, copy related
                            // keywords to the new list
                            if (dataObj.existsInKeywords(ffs, cnt, false)) {
                                // get the entries keywords
                                String[] kws = dataObj.getKeywords(cnt);
                                // when we have any keywords, go on
                                if (kws != null) {
                                    // go through all related keywords
                                    for (String kw : kws) {
                                        // if the requested keyword exists in the list of
                                        // remaining keywords...
                                        if (remainingKeywords.contains(kw)) {
                                            // add it to the new keywords list
                                            newKeywords.add(kw);
                                            // and remove it from the remaining keywords
                                            remainingKeywords.remove(kw);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        // update progressbar
                        setProgress(cnt, 0, slength);
                    }
                    // sort the keywordlists
                    if (newKeywords != null && !newKeywords.isEmpty()) {
                        Collections.sort(newKeywords, new Comparer());
                    }
                    if (remainingKeywords != null && !remainingKeywords.isEmpty()) {
                        Collections.sort(remainingKeywords, new Comparer());
                    }
                }
                // we're done with the first step
                break;
        }

        return null;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
        // Runs on the EDT.  Update the GUI based on
        // the result computed by doInBackground().
        // store results in data-class, so we have access to them even after this class is disposed
        taskinfo.setKeywordSuggestionList(remainingKeywords, TasksData.REMAINING_KW);
        taskinfo.setKeywordSuggestionList(newKeywords, TasksData.NEW_KW);
    }

    @Override
    protected void finished() {
        
        System.out.println((double)(System.nanoTime() - nt) / 1000000000.0);
        
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}
