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
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author danielludecke
 */
public class ReplaceTask extends org.jdesktop.application.Task<Object, Void> {
    private final Daten dataObj;
    private final TasksData taskinfo;
    private String findTerm;
    private String replaceTerm;
    private int[] replaceEntries;
    private final int replaceWhere;
    private final boolean wholeword;
    private final boolean matchcase;
    private final boolean regex;
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ReplaceTask.class);

    /**
     *
     * @param app
     * @param parent
     * @param label
     * @param d
     * @param fs a string containing the find-term that should be replaced
     * @param rs a string containing the replace-term that replaces the find-term {@code fs}
     * @param re an integer array containing the entry-numbers of those entries where
     * the search should be applied to. use {@code null} to search and replace in all entries.
     * @param w where the search should be applied to, i.e. search within content, keywords, authors etc.
     * @param ww pass true, if the search should find whole words only
     * @param mc whether the search is case sensitive (true) or not (false)
     * @param rex whether the find-term {@code fs} is a regular expression (true) or not...
     */
    ReplaceTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label, TasksData td,
                Daten d, String fs, String rs, int[] re, int w, boolean ww, boolean mc, boolean rex) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to ImportFileTask fields, here.
        super(app);
        dataObj=d;
        taskinfo = td;
        findTerm=fs;
        replaceTerm=rs;
        replaceEntries=re;
        replaceWhere=w;
        wholeword=ww;
        matchcase=mc;
        regex=rex;
        parentDialog = parent;
        msgLabel = label;
        // init status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }
    @Override
    protected Object doInBackground() {
        // here we initiate variables that count the amount of changes in each
        // element, so after the replacement is done we can tell the user where
        // and how many replacements have been made
        int changeCounterTitles = 0;
        int changeCounterContent = 0;
        int changeCounterRemarks = 0;
        int changeCounterLinks = 0;
        int changeCounterAuthors = 0;
        int changeCounterKeywords = 0;
        int totalreplacements;
        // when we pass "null" as parameter for the entry-numbers of those entries
        // where the search should be applied to, we assume that we want to search
        // through the whole data. thus, we fill the arrays with all entrynumbers now...
        if (null==replaceEntries) {
            replaceEntries = new int[dataObj.getCount(Daten.ZKNCOUNT)];
            // go through all entries
            for (int cnt=1; cnt<=dataObj.getCount(Daten.ZKNCOUNT);cnt++) replaceEntries[cnt-1] = cnt;
        }
        // when we have *no* regular expression for the replace-request,
        // quote/escape reg-ex-chars in replacement term
        if (!regex && !replaceTerm.isEmpty()) replaceTerm = Matcher.quoteReplacement(replaceTerm);
        // check whether the findterm is a regular expression or not.
        // if not, prepare findterm with wholeword and matchcase-regex
        if (!regex && !findTerm.isEmpty()) {
            // escape chars, so they are not recognized as regex.
            findTerm = Pattern.quote(findTerm);
            // when we have a whole-word-find&replace, surround findTerm with
            // the regular expression that indicates word beginning and ending (i.e. whole word)
            if (wholeword) findTerm = "\\b"+findTerm+"\\b";
            // when the find & replace is *not* case-sensitive, set regular expression
            // to ignore the case...
            if (!matchcase) findTerm = "(?i)"+findTerm;
            // the final findterm now might look like this:
            // "(?i)\\b<findterm>\\b", in case we ignore case and have whole word search
        }
        // here we check whether we should find and replace in entries
        // later, below, we check whether we have to replace in lists, like
        // the keyword- or author-list
        if ((replaceWhere&Constants.SEARCH_CONTENT)!=0 ||
            (replaceWhere&Constants.SEARCH_TITLE)!=0 ||
            (replaceWhere&Constants.SEARCH_LINKS)!=0 ||
            (replaceWhere&Constants.SEARCH_REMARKS)!=0) {
            // go through all entries...
            for (int cnt=0; cnt<replaceEntries.length; cnt++) {
                //
                // check whether we have to replace titles
                //
                if ((replaceWhere&Constants.SEARCH_TITLE)!=0) {
                    // get the entry's title...
                    String oldtitle = dataObj.getZettelTitle(replaceEntries[cnt]);
                    // check whether the findterm is empty. if so, we want to
                    // replace an empty title-element with the replaceterm
                    if (findTerm.isEmpty()) {
                        // when both findterm and title are empty, set replace-term
                        // as new title and increase our counter...
                        if (oldtitle.isEmpty()) {
                            dataObj.setZettelTitle(replaceEntries[cnt], replaceTerm);
                            changeCounterTitles++;
                        }
                    }
                    // else replace replace the findterm within the current title (stored in "dummy")
                    // with the replace-term
                    else if (!oldtitle.isEmpty()) {
                        // replace findterm in old title
                        String newtitle = oldtitle.replaceAll(findTerm, replaceTerm);
                        // check whether we have any changes at all...
                        if (!oldtitle.equals(newtitle)) {
                            // if yes, set new title
                            dataObj.setZettelTitle(replaceEntries[cnt], newtitle);
                            // and increase our counter
                            changeCounterTitles++;
                        }
                    }
                }
                //
                // check whether we have to replace content
                //
                // check whether the findterm is empty. if so, do nothing, because we cannot
                // have an empty entry-content
                if ((replaceWhere&Constants.SEARCH_CONTENT)!=0 && !findTerm.isEmpty()) {
                    // get the entry's content...
                    String oldcontent = dataObj.getZettelContent(replaceEntries[cnt]);
                    // and replace the findterms.
                    String newcontent = oldcontent.replaceAll(findTerm, replaceTerm);
                    // check whether we have any changes at all
                    // and check that the new content is not empty!
                    if (!oldcontent.equals(newcontent) && !newcontent.isEmpty()) {
                        // if yes, set new content
                        dataObj.setZettelContent(replaceEntries[cnt], newcontent, false);
                        // and increase our counter
                        changeCounterContent++;
                    }
                }
                //
                // check whether we have to replace remarks
                //
                if ((replaceWhere&Constants.SEARCH_REMARKS)!=0) {
                    // get the entry's remarks...
                    String oldremarks = dataObj.getRemarks(replaceEntries[cnt]);
                    // check whether the findterm is empty. if so, we want to
                    // replace an empty remarks-element with the replaceterm
                    if (findTerm.isEmpty()) {
                        // when both findterm and remarks are empty, set replace-term
                        // as new remarks and increase our counter...
                        if (oldremarks.isEmpty()) {
                            if (dataObj.setRemarks(replaceEntries[cnt], replaceTerm)) changeCounterRemarks++;
                        }
                    }
                    // else replace replace the findterm within the current remarks (stored in "oldremarks")
                    // with the replace-term
                    else if (!oldremarks.isEmpty()) {
                        // replace findterm in old remarks
                        String newremarks = oldremarks.replaceAll(findTerm, replaceTerm);
                        // check whether we have any changes at all...
                        if (!oldremarks.equals(newremarks)) {
                            // if yes, set new remarks
                            if (dataObj.setRemarks(replaceEntries[cnt], newremarks)) {
                                // and increase our counter
                                changeCounterRemarks++;
                            }
                        }
                    }
                }
                //
                // check whether we have to replace attachments
                //
                // we cannot have empty attachment-elements, so we
                // only proceed here when we have any findterms
                if ((replaceWhere&Constants.SEARCH_LINKS)!=0 && !findTerm.isEmpty()) {
                    // get a string array with all entry's attachments
                    String[] att = dataObj.getAttachmentsAsString(replaceEntries[cnt], false);
                    // here we check whether we have made any changes at all...
                    boolean changesmade = false;
                    // this list stores all new attachment-values
                    ArrayList<String> newarray = new ArrayList<>();
                    // go through all possible attachments
                    for (String oldatt : att) {
                        // replace findterm in old attachments
                        String newatt = oldatt.replaceAll(findTerm, replaceTerm);
                        // check whether we have any changes at all.
                        // if yes, change indicator-variable...
                        if (!oldatt.equals(newatt)) changesmade = true;
                        // add newattachment to the linked list, but only
                        // if it's not empty...
                        if (!newatt.isEmpty()) newarray.add(newatt);
                    }
                    // now check whether we had any changes to the attachments
                    // at all...
                    if (changesmade) {
                        // set new attachments
                        dataObj.setAttachments(replaceEntries[cnt], newarray.toArray(new String[newarray.size()]));
                        // and increase our counter
                        changeCounterLinks++;
                    }
                }
                // update progress bar
                setProgress(cnt,0,replaceEntries.length);
            }
        }
        // here we check whether we should find and replace in the keyword-list.
        // be careful when replacing empty replaceterms: check
        // whether keyword is not empty - else don't change keyword!
        // since we don't have empty keywords, we only need to proceed
        // when we have a find term.
        if ((replaceWhere&Constants.SEARCH_KEYWORDS)!=0 && !findTerm.isEmpty()) {
            // go through all keywords
            for (int cnt=1; cnt<=dataObj.getCount(Daten.KWCOUNT); cnt++) {
                // retrieve old keyword
                String oldkw = dataObj.getKeyword(cnt);
                // replace findterm in old keyword
                String newkw = oldkw.replaceAll(findTerm, replaceTerm);
                // if we have any changes, and new keyword is not empty...
                if (!oldkw.equals(newkw) && !newkw.isEmpty()) {
                    // check whether new keyword (replace term) already exists
                    if (dataObj.getKeywordPosition(newkw, false)!=-1) {
                        // the new name for keyword already exists, so we can offer to merge
                        // the keywords here. in fact, this is an easy find/replace-routine, since the
                        // old keyword is replaced by the existing one, when we merge them.
                        // create a JOptionPane with yes/no/cancel options
                        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("mergeKeywordMsg", newkw), resourceMap.getString("mergeKeywordTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                        // if no merge is requested, leave method
                        if (JOptionPane.NO_OPTION == option) continue;
                        // merge the keywords by opening a dialog with a background task
                        dataObj.mergeKeywords(oldkw, newkw);
                        // and increase our counter
                        changeCounterKeywords++;
                    }
                    else {
                        // ...change keyword
                        dataObj.setKeyword(cnt, newkw);
                        // and increase our counter
                        changeCounterKeywords++;
                    }
                }
            }
        }
        // here we check whether we should find and replace in the author-list.
        // be careful when replacing empty replaceterms: check
        // whether author is not empty - else don't change author!
        // since we don't have empty authors, we only need to proceed
        // when we have a find term.
        if ((replaceWhere&Constants.SEARCH_AUTHOR)!=0 && !findTerm.isEmpty()) {
            // go through all authors
            for (int cnt=1; cnt<=dataObj.getCount(Daten.AUCOUNT); cnt++) {
                // retrieve old author
                String oldau = dataObj.getAuthor(cnt);
                // replace findterm in old author
                String newau = oldau.replaceAll(findTerm, replaceTerm);
                // if we have any changes, and new author is not empty...
                if (!oldau.equals(newau) && !newau.isEmpty()) {
                    // check whether new author (replace term) already exists
                    if (dataObj.getAuthorPosition(newau)!=-1) {
                        // the new name for author already exists, so we can offer to merge
                        // the authors here. in fact, this is an easy find/replace-routine, since the
                        // old author is replaced by the existing one, when we merge them.
                        // create a JOptionPane with yes/no/cancel options
                        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("mergeAuthorMsg"), resourceMap.getString("mergeAuthorTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                        // if no merge is requested, leave method
                        if (JOptionPane.NO_OPTION == option ) continue;
                        // merge authors
                        dataObj.mergeAuthors(oldau, newau);
                        // and increase our counter
                        changeCounterAuthors++;
                    }
                    else {
                        // ...change author
                        dataObj.setAuthor(cnt, newau);
                        // and increase our counter
                        changeCounterAuthors++;
                    }
                }
            }
        }
        // calculate total replacements made
        totalreplacements = changeCounterTitles+
                            changeCounterContent+
                            changeCounterRemarks+
                            changeCounterLinks+
                            changeCounterAuthors+
                            changeCounterKeywords;
        // prepare string builder for result message
        StringBuilder replacemsg = new StringBuilder("");
        // check whether we have any replacements at all...
        // if yes, totalreplacements would be greater than zero
        if (totalreplacements>0) {
            // now check which replace-domain had any changes - this is indicated by the related counter-variable, which
            // must have a value greater than zero. if we have changes, retrieve the message-string from the
            // resourcemap and create the "replaced message"
            // in some casess, we also set the up-to-date-indicator for particular tables to false,
            // so a table update is made.
            if (changeCounterTitles>0) {
                replacemsg.append(resourceMap.getString("replacedInTitles",String.valueOf(changeCounterTitles)));
                dataObj.setTitlelistUpToDate(false);
            }
            if (changeCounterContent>0) replacemsg.append(resourceMap.getString("replacedInContents",String.valueOf(changeCounterContent)));
            if (changeCounterRemarks>0) replacemsg.append(resourceMap.getString("replacedInRemarks",String.valueOf(changeCounterRemarks)));
            if (changeCounterLinks>0) {
                replacemsg.append(resourceMap.getString("replacedInAttachments",String.valueOf(changeCounterLinks)));
                dataObj.setAttachmentlistUpToDate(false);
            }
            if (changeCounterKeywords>0) {
                replacemsg.append(resourceMap.getString("replacedInKeywords",String.valueOf(changeCounterKeywords)));
                dataObj.setKeywordlistUpToDate(false);
            }
            if (changeCounterAuthors>0) {
                replacemsg.append(resourceMap.getString("replacedInAuthors",String.valueOf(changeCounterAuthors)));
                dataObj.setAuthorlistUpToDate(false);
            }
        }
        // now create the "final" message
        taskinfo.setReplaceMessage((totalreplacements>0) ? resourceMap.getString("replacedMsg", replacemsg.toString()) : resourceMap.getString("noReplacementsMsg"));
        // and store replacement-count
        taskinfo.setReplaceCount(totalreplacements);
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
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}

