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
package de.danielluedecke.zettelkasten.tasks.export;

import bibtex.dom.BibtexEntry;
import de.danielluedecke.zettelkasten.CTexExportSettings;
import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.misc.Comparer;
import de.danielluedecke.zettelkasten.util.Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author danielludecke
 */
public class ExportTools {

    /**
     * get the strings for file descriptions from the resource map
     */
    private static final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ExportTask.class);

    /**
     * Creates a list of certain entry-values (keywords, authors, attachments)
     * in plain text format
     *
     * @param values the list-values (keywords, authors, attachments...) as
     * string-array
     * @param notfound the title of that list in case no elements have been
     * found
     * @param header the header of the list (e.g. keywords, authors...)
     * @param textagopen
     * @param textagclose
     * @return a String containing the HTML-formatted list of entry-values
     */
    public static String createPlainList(String[] values, String notfound, String header, String textagopen, String textagclose) {
        StringBuilder sb = new StringBuilder("");
        // if there is no keyword information, tell this the user
        if ((null == values) || (values.length < 1)) {
            sb.append(notfound).append(System.lineSeparator());
        } else {
            // create headline indicating that keyword-part starts here
            sb.append(textagopen).append(header).append(textagclose).append(System.lineSeparator()).append(System.lineSeparator());
            // iterate the keyword array
            for (String val : values) {
                // and append each author
                sb.append(val).append(System.lineSeparator());
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * This method creates a list of entry-numbers, which are usually referring
     * from one entry to other entries (e.g. the follower-numbers or manual
     * links to other entries). The list is in plain-text, i.e. it is used for
     * exporting entries to the TXT or LaTex-format.
     *
     * @param values the refrerring entries of which a list should be created
     * (i.e. that should appear in this list)
     * @param notfound a String which is displayed if the supposed entry has no
     * referring entry-numbers (i.e. {@code values} is empty)
     * @param header a header which says what kind of list this is
     * @param textagopen an open-tag of a tag which surrounds this list.
     * typically only used when creating lists for the {@code LaTex} format,
     * then e.g. <b>\\subsection{</b> is used.
     * @param textagclose a closing-tag of a tag which surrounds this list.
     * typically only used when creating lists for the {@code LaTex} format,
     * then e.g. <b>}{</b> is used.
     * @return the complete list of entries which have been passed through the
     * parameter {@code values}, where this list is "formatted" as paragraph for
     * usage in plain TXT or LaTex-export.
     */
    public static String createPlainCommaList(String[] values, String notfound, String header, String textagopen, String textagclose) {
        StringBuilder sb = new StringBuilder("");
        // if there is no keyword information, tell this the user
        if ((null == values) || (values.length < 1)) {
            sb.append(notfound).append(System.lineSeparator());
        } else {
            // create headline indicating that keyword-part starts here
            sb.append(textagopen).append(header).append(textagclose).append(System.lineSeparator()).append(System.lineSeparator());
            // iterate the keyword array
            for (String val : values) {
                // and append each author
                sb.append(val).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * This method creates a reference list in the export-format. This method is
     * used when exporting entries into html-format. The reference-list is
     * created from the used footnotes, i.e. each author-footnote in an entry is
     * added to the final reference-list. When exporting to HTML, the
     * authors-footnotes are linked with the author-value in the reference-list.
     *
     * @param dataObj
     * @param settingsObj
     * @param contentpage the complete html-page that is going to be exported,
     * so the author-footnotes can be extracted from this content.
     * @param footnotetag the footnote-tag, to identify where a footnote starts
     * @param footnoteclose the closing-tag of footnotes, so the author-number
     * within the footnote can be extracted
     * @param headeropen the header-tag, in case the title "reference list" is
     * surrounded by a header-tag
     * @param headerclose the header-tag, in case the title "reference list" is
     * surrounded by a header-tag
     * @param listtype whether the list is formatted in html or plain text. use
     * following constants:<br>
     * - CConstants.REFERENCE_LIST_TXT<br>
     * - CConstants.REFERENCE_LIST_HTML
     * @return a converted String containing the reference list with all
     * references (authors) that appeared as author-footnote in the export-file
     */
    public static String createReferenceList(Daten dataObj, Settings settingsObj, String contentpage, String footnotetag, String footnoteclose, String headeropen, String headerclose, int listtype) {
        // now prepare a reference list from possible footnotes
        LinkedList<String> footnotes = new LinkedList<>();
        // position index for finding the footnotes
        int pos = 0;
        // get length of footnote-tag, so we know where to look for the author-number within the footnote
        int len = footnotetag.length();
        // do search as long as pos is not -1 (not-found)
        while (pos != -1) {
            // find the html-tag for the footnote
            pos = contentpage.indexOf(footnotetag, pos);
            // if we found something...
            if (pos != -1) {
                // find the closing quotes
                int end = contentpage.indexOf(footnoteclose, pos + len);
                // if we found that as well...
                if (end != -1) {
                    // extract footnote-number
                    String fn = contentpage.substring(pos + len, end);
                    // and add it to the linked list, if it doesn't already exist
                    if (-1 == footnotes.indexOf(fn)) {
                        footnotes.add(fn);
                    }
                    // set pos to new position
                    pos = end;
                } else {
                    pos = pos + len;
                }
            }
        }
        StringBuilder sb = new StringBuilder("");
        // now we have all footnotes, i.e. the author-index-numbers, in the linked
        // list. now we can create a reference list
        if (footnotes.size() > 0) {
            // first, init the list in html...
            sb.append(headeropen);
            // append a new headline with the bullet's name
            sb.append(resourceMap.getString("referenceListHeading"));
            sb.append(headerclose).append(System.lineSeparator());
            // iterator for the linked list
            Iterator<String> i = footnotes.iterator();
            // go through all footnotes
            while (i.hasNext()) {
                // get author-number-string
                String au = i.next();
                try {
                    // convert string to int
                    int aunr = Integer.parseInt(au);
                    switch (listtype) {
                        case Constants.REFERENCE_LIST_TXT:
                            // prepare html-stuff for authors
                            sb.append("[").append(au).append("] ").append(dataObj.getAuthor(aunr)).append(System.lineSeparator());
                            break;
                        case Constants.REFERENCE_LIST_HTML:
                            // prepare html-stuff for authors
                            sb.append("<p class=\"reflist\"><b>[<a name=\"fn_").append(au).append("\">").append(au).append("</a>]</b> ");
                            sb.append(dataObj.getAuthor(aunr));
                            sb.append("</p>").append(System.lineSeparator());
                            break;
                    }
                } catch (NumberFormatException e) {
                    Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                }
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param exportDataString
     * @param filepath
     * @return
     */
    public static boolean writeExportData(String exportDataString, File filepath) {
        // yet everything is ok...
        boolean exportOk = true;
        // create filewriter
        Writer exportfile = null;
        // check whether we have content at all
        if (exportDataString != null && !exportDataString.isEmpty()) {
            try {
                // create output-file in UTF8-encoding
                exportfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF8"));
                // and try save content in UTF8-encoding to disk...
                exportfile.write(exportDataString);
            } catch (UnsupportedEncodingException ex) {
                try {
                    // create output-file in default-system-encoding
                    exportfile = new FileWriter(filepath);
                    // and save content to disk...
                    exportfile.write(exportDataString);
                } catch (IOException exep) {
                    // and change indicator
                    exportOk = false;
                    Constants.zknlogger.log(Level.SEVERE, exep.getLocalizedMessage());
                } finally {
                    try {
                        // finally, close filewriter
                        if (exportfile != null) {
                            exportfile.close();
                        }
                    } catch (IOException ioex) {
                        // and change indicator
                        exportOk = false;
                        Constants.zknlogger.log(Level.SEVERE, ioex.getLocalizedMessage());
                    }
                }
            } catch (IOException ex) {
                // and change indicator
                exportOk = false;
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                Constants.zknlogger.log(Level.SEVERE, "Using following path: {0}", filepath.getAbsolutePath());
            } finally {
                try {
                    // finally, close filewriter
                    if (exportfile != null) {
                        exportfile.close();
                    }
                } catch (IOException ex) {
                    // and change indicator
                    exportOk = false;
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        } else {
            // and change indicator
            exportOk = false;
            Constants.zknlogger.log(Level.SEVERE, "Error when exporting entries: no content available!");
        }
        return exportOk;
    }

    /**
     *
     * @param dataObj
     * @param bibtexObj
     * @param exportentries
     * @param filepath
     * @param resmap
     */
    public static void writeBibTexFile(Daten dataObj, BibTeX bibtexObj, ArrayList<Object> exportentries, File filepath, org.jdesktop.application.ResourceMap resmap) {
        // check whether we have any bibtex-entries at all...
        if (bibtexObj.getCount() > 0) {
            // this list will contain all found bibkeys within the authors
            // of the to be exported entries
            ArrayList<String> foundbibkeys = new ArrayList<>();
            for (Object exportentrie : exportentries) {
                try {
                    // retrieve zettelnumber
                    int zettelnummer = Integer.parseInt(exportentrie.toString());
                    // retrieve the author-index-numbers of each export-entry
                    int[] entryauthors = dataObj.getAuthorIndexNumbers(zettelnummer);
                    // check whether entry has any authors...
                    if (entryauthors != null && entryauthors.length > 0) {
                        // iterate all author-index-numbers
                        for (int au : entryauthors) {
                            // try to retrieve the bibkey from each author
                            String bibkey = dataObj.getAuthorBibKey(au);
                            // if we have a valid bibkey that is not already in our list with the
                            // found bibkeys, add the bibkey to it...
                            if (bibkey != null && !bibkey.isEmpty() && !foundbibkeys.contains(bibkey)) {
                                foundbibkeys.add(bibkey);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // do nothing here...
                }
            }
            // check whether we have found any bibkeys at all...
            if (foundbibkeys.size() > 0) {
                // get export-filepath as string
                String orifile = filepath.toString();
                // and copy whole filepath except file-extension, to a new string,
                // setting the ".bib" extension instead
                String exportbibtexfile = orifile.substring(0, orifile.lastIndexOf(".")) + ".bib";
                // set export-file-path and check whether file already exists. if yes, ask for
                // overwriting
                File exportbibfilepath = new File(exportbibtexfile);
                // check whether exported bibtex-file aready exists
                if (exportbibfilepath.exists()) {
                    // file exists, ask user to overwrite it...
                    int optionDocExists = JOptionPane.showConfirmDialog(null, resmap.getString("askForOverwriteFileMsg", "BibTeX-", exportbibfilepath.getName()), resmap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    // if the user does *not* choose to overwrite, quit...
                    if (JOptionPane.NO_OPTION == optionDocExists) {
                        return;
                    }
                }
                // sort bibkeys
                Collections.sort(foundbibkeys, new Comparer());
                // clear all entries for the export
                bibtexObj.clearExportBibtexEntries();
                // create iterator for the found bíbkeys
                Iterator<String> bibkeys = foundbibkeys.iterator();
                while (bibkeys.hasNext()) {
                    // retrieve bibtex-entry (from the attached bibtex-file) that is associated
                    // with the bibkey from the exported author
                    BibtexEntry be = bibtexObj.getEntry(bibkeys.next());
                    // add entry to the bibtex-export-file
                    bibtexObj.addBibtexEntryForExport(be);
                }
                // export all bibtex-entries
                bibtexObj.exportBibtexEntries(exportbibfilepath);
            } else {
                // and log info-message
                Constants.zknlogger.log(Level.INFO, resmap.getString("noBibtexEntriesMsg"));
            }
        } else {
            // retrieve filepath of attached file for the message-logging
            File caf = bibtexObj.getCurrentlyAttachedFile();
            // prepare default log-message. in case we have no attached file, use this string
            String cafstring = "<no attached file found>";
            if (caf != null) {
                // else use the filepath to the bibtex-file that had no vali entries
                cafstring = caf.toString();
            }
            // and log info-message
            Constants.zknlogger.log(Level.INFO, resmap.getString("noBibtexEntriesFoundMsg", cafstring));
        }
    }

    /**
     * This method converts all footnote-tags ({@code [fn <number>]}) into the
     * appropriate LaTex-cite-command, using the bibkeys of the referenced
     * author-values (if they have any bibkeys).
     * <br><br>
     * Each footnote that refers to an author-value with bibkey is converted
     * like this:
     * <b>{@code \footcite{<bibkey>}}</b>
     *
     * @param dataObj
     * @param content the entry's content, so the footnote-tags can be extracted
     * and possible bibkey-values retrieved.
     * @param referenceAsFootnote
     * @return the content as string value, with all footnote-tags that
     * reference to author-values with bibkeys converted to the
     * LaTex-cite-command
     */
    public static String createLatexFootnotes(Daten dataObj, String content, boolean referenceAsFootnote) {
        // if the reference should be in a footenote, create this string now
        String footRefOpen = (referenceAsFootnote) ? "\\footcite" : "\\cite";
        String footRefClose = "}";
        // save find-position
        List<Integer> start = new ArrayList<>();
        List<Integer> end = new ArrayList<>();
        try {
            // create foot note patterm
            Pattern p = Pattern.compile("\\[fn ([^\\[]*)\\]");
            // create matcher
            Matcher m = p.matcher(content);
            // check for occurences
            while (m.find()) {
                // save grouping-positions
                start.add(m.start());
                end.add(m.end());
            }
            // iterate found positions
            for (int i = start.size() - 1; i >= 0; i--) {
                // get footnote
                String fn = content.substring(start.get(i) + Constants.FORMAT_FOOTNOTE_OPEN.length(), end.get(i) - 1);
                // do we have a colon? this indicates a page separator
                String[] fnpagenr = fn.split(Pattern.quote(":"));
                String pagenr = null;
                // more than 1 value means, we have a page numner after colon
                if (fnpagenr.length > 1) {
                    // we assume reference index number at first position
                    fn = fnpagenr[0];
                    pagenr = fnpagenr[1];
                }
                if (null == pagenr || pagenr.isEmpty()) {
                    pagenr = "{";
                } else {
                    pagenr = "[" + resourceMap.getString("footnotePage") + pagenr + "]{";
                }
                // check whether footnote is a bibkey, or reference number
                int fnnr;
                try {
                    // try to parse token inside footnote tage
                    // and check whether it is an integer number
                    // (i.e. a reference to an author)
                    fnnr = Integer.parseInt(fn);
                } catch (NumberFormatException ex) {
                    // if it is no integer value, check whether
                    // token is a bibkey
                    fnnr = dataObj.getAuthorBibKeyPosition(fn);
                }
                // retrieve author value's bibkey
                String bibkey = dataObj.getAuthorBibKey(fnnr);
                // check whether we have any bibkey-value
                if (bibkey != null && !bibkey.isEmpty()) {
                    // if we have footnote cite and braces around footnote,
                    // remove them
                    if (referenceAsFootnote) {
                        try {
                            // footnote starts with (, remove
                            if (content.charAt(start.get(i) - 1) == '(') {
                                start.set(i, start.get(i) - 1);
                            }
                            // footnote ends with (, remove
                            if (content.charAt(end.get(i)) == ')') {
                                end.set(i, end.get(i) + 1);
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                    }
                    // now that we have the bibkey, replace footnote with cite-tag
                    content = content.substring(0, start.get(i)) + footRefOpen
                            + pagenr + bibkey + footRefClose
                            + content.substring(end.get(i));
                }
            }
        } catch (PatternSyntaxException | IndexOutOfBoundsException ex) {
        } catch (NumberFormatException ex) {
            Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            Constants.zknlogger.log(Level.WARNING, "Could not convert author ID into author number!");
        }
        // return content. this string now has converted footnotes, where the referenced
        // author value contains a bibkey.
        return content;
    }

    public static boolean isExportSettingsOk(JFrame frame, Settings settings, int type) {
        // when the user wants to export into PDF, open a new dialog where the user
        // can make page settings and font-sizes.
        if (Constants.EXP_TYPE_TEX == type || Constants.EXP_TYPE_DESKTOP_TEX == type) {
            latexExportSettingsDlg = new CTexExportSettings(frame, settings);
            // center window
            latexExportSettingsDlg.setLocationRelativeTo(frame);
            // show window
            ZettelkastenApp.getApplication().show(latexExportSettingsDlg);
            // retrieve cancel-flag
            boolean expcancelled = latexExportSettingsDlg.isCancelled();
            // free memory
            latexExportSettingsDlg.dispose();
            latexExportSettingsDlg = null;
            // check whether user cancelled export or not
            return !expcancelled;
        }
        return true;
    }
    private static CTexExportSettings latexExportSettingsDlg;
}
