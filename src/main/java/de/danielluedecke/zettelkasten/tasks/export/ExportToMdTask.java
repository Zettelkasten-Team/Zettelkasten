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

import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.TreeUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Luedeke
 */
public class ExportToMdTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the CDaten object, which contains the XML data of the
     * Zettelkasten will be passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     *
     */
    private final BibTeX bibtexObj;
    /**
     *
     */
    private final DesktopData desktopObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     *
     */
    private final Settings settingsObj;
    /**
     * Indicates whether or not a bibtex-file from the exported entries should
     * be created or not
     */
    private final boolean exportbibtex;
    /**
     * Defines whether each headline of any entry has its entry-number as
     * prefix. If set to {@code true}, each entry's title will appear as
     * <i>Entry XY: entry title</i>, if set to {@code false}, a title will just
     * appear as <i>entry title</i>, or left out if title is empty.
     */
    private final boolean zettelNumberAsPrefix;
    /**
     * This variable stores the parts which should be exported. It's a mix of
     * ORed constants, see below
     */
    private final int exportparts;
    /**
     * indicates which type of data format should be exported to. refer to the
     * Zettelkasten.view properties file (resources) to see which number is
     * which file type.
     */
    private final int exporttype;
    private final boolean isHeadingVisible;
    /**
     * file path to export file
     */
    private final File filepath;
    /**
     *
     */
    private StringBuilder exportPage = new StringBuilder("");
    /**
     *
     */
    private DefaultMutableTreeNode treenode = null;
    /**
     *
     */
    private ArrayList<Object> exportentries;
    /**
     *
     */
    private boolean exportOk;
    /**
     *
     */
    private boolean showOkMessage = true;
    private final boolean separateFiles;
    /**
     *
     */
    private final javax.swing.JDialog parentDialog;
    private final javax.swing.JLabel msgLabel;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(ExportTask.class);

    /**
     *
     * @param app
     * @param parent the dialog's parent frame
     * @param label
     * @param td a reference to the TaskDatas-class
     * @param d a reference to the Daten-class
     * @param dt a reference to the DesktopData-class
     * @param s a reference to the Settings-class
     * @param bto a refrence to the BibTeX-class
     * @param fp the filepath and -name of the export-file
     * @param ee an integer-array of those entries that should be exported. use {
     * @null} to export all entries
     * @param type the exporttype, i.e. whether the entries should be exported
     * to XML, RTF, PDF, TXT etc.<br>
     * use following constants:<br>
     * - CConstants.EXP_TYPE_PDF<br>
     * - CConstants.EXP_TYPE_RTF<br>
     * - CConstants.EXP_TYPE_XML<br>
     * - CConstants.EXP_TYPE_CSV<br>
     * - CConstants.EXP_TYPE_HTML<br>
     * - CConstants.EXP_TYPE_TXT<br>
     * - CConstants.EXP_TYPE_TEX (for LaTex-files)
     * @param part which parts of an entry (content, authors, keywords,
     * attachments...) should be exported.
     * @param n the treenode of a selected node (entry) within the DesktopFrame.
     * This indicates, which part of the Desktop-Entries should be exportet,
     * i.e. at which node and related children the export of entries starts.
     * @param bibtex whether a separate Bibtex-file containing a bibtex-styles
     * reference list should be created {@code true} or not {@code false}. This
     * file will be created depending on available Bibkeys as attributes of the
     * author-values.
     * @param ihv indicates whether the headings (titles) of exported entries
     * should be visible (use {@code true}) or not ({@code false}). <b>Only
     * applies when entries are exported from the DesktopFrame</b>
     * @param numberprefix indicates whether entries' titles should have their
     * entry-number included or not.
     * @param sf whether each note should be saved as separate file
     */
    public ExportToMdTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
                          TasksData td, Daten d, DesktopData dt, Settings s, BibTeX bto, File fp, ArrayList<Object> ee, int type, int part,
                          DefaultMutableTreeNode n, boolean bibtex, boolean ihv, boolean numberprefix, boolean sf) {
        super(app);
        dataObj = d;
        settingsObj = s;
        bibtexObj = bto;
        desktopObj = dt;
        filepath = fp;
        isHeadingVisible = ihv;
        exporttype = type;
        exportparts = part;
        exportbibtex = bibtex;
        exportentries = ee;
        zettelNumberAsPrefix = numberprefix;
        exportOk = true;
        taskinfo = td;
        treenode = n;
        separateFiles = sf;
        parentDialog = parent;
        msgLabel = label;

        // the variable "exportentries" stores all entry-numbers of those entries that should be exported.
        // if this array is null, we assume that *all* entries have to be exported. thus, insert
        // all entry-numbers here
        if (null == exportentries) {
            exportentries = new ArrayList<>();
            // copy all entry-numbers to array. remember that the entrynumbers range from 1 to site of file.
            for (int cnt = 0; cnt < dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // only add entries that are not empty
                if (!dataObj.isEmpty(cnt + 1)) {
                    exportentries.add(cnt + 1);
                }
            }
        }
        // show status text
        msgLabel.setText(resourceMap.getString("msg1"));
    }

    @Override
    protected Object doInBackground() {
        // Your Task's code here.  This method runs
        // on a background thread, so don't reference
        // the Swing GUI from here.
        // prevent task from processing when the file path is incorrect
        // if no file exists, exit task
        if (null == filepath) {
            showOkMessage = false;
            return null;
        }
        // get destination directory for separate files
        String separateFileDir = "";
        // if separate file for each note, check whether we have a slash at the end
        if (separateFiles) {
            if (filepath.isDirectory() && !filepath.toString().endsWith(String.valueOf(java.io.File.separatorChar))) {
                // if not, add slash
                separateFileDir = filepath.toString() + String.valueOf(java.io.File.separatorChar);
            } else {
                // else get file path
                separateFileDir = FileOperationsUtil.getFilePath(filepath);
            }
        }
        // check whether file already exists
        if (!filepath.isDirectory() && filepath.exists()) {
            // file exists, ask user to overwrite it...
            int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg", "", filepath.getName()), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if the user does *not* choose to overwrite, quit...
            if (optionDocExists != JOptionPane.YES_OPTION) {
                // don't show "export was OK" message in main frame
                showOkMessage = false;
                return null;
            }
        }
        // create a new stringbuilder that will contain the final string, i.e.
        // the html-page which we set to the jeditorpane
        exportPage = new StringBuilder("");
        // get the size of the export data, used for progressbar
        int contentsize = exportentries.size();
        // and create a counter for the progressbar's position
        int counter;
        switch (exporttype) {
            case Constants.EXP_TYPE_MD:
                // go through all elements of the data file
                for (counter = 0; counter < exportentries.size(); counter++) {
                    try {
                        // retrieve zettelnumber
                        int zettelnummer = Integer.parseInt(exportentries.get(counter).toString());
                        String zetteltitle = "";
                        // check whether the user wants to export titles.
                        if ((exportparts & Constants.EXPORT_TITLE) != 0) {
                            // first check whether we have a title or not
                            zetteltitle = dataObj.getZettelTitle(zettelnummer);
                            // only prepare a title when we want to have the entry's number as title-prefix,
                            // or if we have a title.
                            if (!zetteltitle.isEmpty() || zettelNumberAsPrefix) {
                                // make headline 1
                                exportPage.append("# ");
                                // each entry has at least the title "entry" plus its number.
                                // we need this to search through the entries when the user clicks an entry on the
                                // jTreeDesktop in the desktop-window
                                if (zettelNumberAsPrefix) {
                                    exportPage.append(resourceMap.getString("entryText")).append(" ").append(String.valueOf(zettelnummer));
                                }
                                // if we have a "real" title, append it...
                                if (!zetteltitle.isEmpty()) {
                                    if (zettelNumberAsPrefix) {
                                        exportPage.append(": ");
                                    }
                                    exportPage.append(zetteltitle);
                                }
                                exportPage.append(System.lineSeparator()).append(System.lineSeparator());
                            }
                        }
                        // check whether the user wants to export content
                        if ((exportparts & Constants.EXPORT_CONTENT) != 0) {
                            // get the zettelcontent, UBB converted to Markdown
                            String zc = Tools.convertUBB2MarkDown(dataObj.getZettelContent(zettelnummer));
                            // convert footnotes
                            zc = HtmlUbbUtil.convertFootnotesToPlain(dataObj, bibtexObj, settingsObj, zc);
                            // remove any non-compatible UBB tags
                            String zettelcontent = Tools.removeUbbFromString(zc, false);
                            // if we have content, add it.
                            if (!zettelcontent.isEmpty()) {
                                exportPage.append(zettelcontent);
                            } else {
                                // else add remark that entry is deleted
                                exportPage.append(resourceMap.getString("deletedEntry"));
                            }
                            exportPage.append(System.lineSeparator()).append(System.lineSeparator());
                        }
                        // if the user wants to export remarks, do this here.
                        if ((exportparts & Constants.EXPORT_REMARKS) != 0) {
                            // get entry's remarks
                            String remarks = dataObj.getCleanRemarks(zettelnummer);
                            // check whether we have any
                            if (!remarks.isEmpty()) {
                                // set headline indicating that we have remarks here
                                exportPage.append(resourceMap.getString("remarksHeader")).append(System.lineSeparator()).append(System.lineSeparator());
                                // init paragraph with class-attribute, so the user may change style aftwerwards
                                exportPage.append(remarks).append(System.lineSeparator()).append(System.lineSeparator());
                            }
                        }
                        // check whether the user wants to export authors
                        if ((exportparts & Constants.EXPORT_AUTHOR) != 0 && dataObj.hasAuthors(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getAuthorsWithIDandBibKey(zettelnummer), resourceMap.getString("NoAuthor"), resourceMap.getString("authorHeader"), "## ", ""));
                        }
                        // check whether user wants to export keywords.
                        if ((exportparts & Constants.EXPORT_KEYWORDS) != 0 && dataObj.hasKeywords(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getKeywords(zettelnummer, true), resourceMap.getString("NoKeyword"), resourceMap.getString("keywordHeader"), "## ", ""));
                        }
                        if ((exportparts & Constants.EXPORT_LINKS) != 0 && dataObj.hasAttachments(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getAttachmentsAsString(zettelnummer, false), resourceMap.getString("NoAttachment"), resourceMap.getString("attachmentHeader"), "## ", ""));
                        }
                        if ((exportparts & Constants.EXPORT_MANLINKS) != 0 && dataObj.hasManLinks(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainCommaList(dataObj.getManualLinksAsString(zettelnummer), resourceMap.getString("NoManLinks"), resourceMap.getString("manlinksHeader"), "## ", ""));
                        }
                        if ((exportparts & Constants.EXPORT_LUHMANN) != 0 && dataObj.hasLuhmannNumbers(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainCommaList(dataObj.getLuhmannNumbersAsString(zettelnummer), resourceMap.getString("NoLuhmann"), resourceMap.getString("luhmannHeader"), "## ", ""));
                        }
                        if ((exportparts & Constants.EXPORT_TIMESTAMP) != 0) {
                            String[] timestamp = dataObj.getTimestamp(zettelnummer);
                            // check whether we have a timestamp at all
                            if (timestamp != null && !timestamp[0].isEmpty()) {
                                // and add the created-timestamp
                                exportPage.append(resourceMap.getString("timestampCreated")).append(" ").append(Tools.getProperDate(timestamp[0], false));
                                // check whether we have a modified-timestamp
                                // if we have a modified-stamp, add it...
                                if (timestamp.length > 1 && !timestamp[1].isEmpty()) {
                                    exportPage.append(System.lineSeparator()).append(resourceMap.getString("timestampEdited")).append(" ").append(Tools.getProperDate(timestamp[1], false));
                                }
                                // and close the tags of the html-part
                                exportPage.append(System.lineSeparator()).append(System.lineSeparator());
                            }
                        }
                        // separate files for each note?
                        if (separateFiles) {
                            // get note number and title for filepath
                            String fname = String.valueOf(zettelnummer) + " " + FileOperationsUtil.getCleanFilePath(zetteltitle);
                            // create file path
                            File fp = new File(separateFileDir + fname.trim() + ".md");
                            // write export file
                            ExportTools.writeExportData(exportPage.toString(), fp);
                            // reset string builder
                            exportPage = new StringBuilder("");
                        }
                    } catch (NumberFormatException e) {
                        // leave out first char, which is always a "H", set by the method
                        // "createExportEntries()".
                        exportPage.append(System.lineSeparator()).append(exportentries.get(counter).toString().substring(2)).append(System.lineSeparator()).append(System.lineSeparator());
                    }
                    // update progress bar
                    setProgress(counter, 0, contentsize);
                }
                break;
            case Constants.EXP_TYPE_DESKTOP_MD:
                exportPage = new StringBuilder("");
                // get comment-export-options
                int commentExport = settingsObj.getDesktopCommentExport();
                switch (commentExport) {
                    case Constants.EXP_COMMENTS_NO:
                        exportEntriesWithComments(treenode, false);
                        break;
                    case Constants.EXP_COMMENTS_YES:
                        exportEntriesWithComments(treenode, true);
                        break;
                    case Constants.EXP_COMMENTS_ONLY:
                        exportEntriesWithCommentsOnly(treenode);
                        break;
                }
                break;
        }
        if (!separateFiles) {
            // show status text
            msgLabel.setText(resourceMap.getString("msg4"));
            // create reference list
            exportPage.append(ExportTools.createReferenceList(dataObj, settingsObj, exportPage.toString(), "[FN ", "]", System.lineSeparator(), System.lineSeparator(), Constants.REFERENCE_LIST_TXT));
            // show status text
            msgLabel.setText(resourceMap.getString("msg2"));
            // write export file
            exportOk = ExportTools.writeExportData(exportPage.toString(), filepath);
            // if the user requested a bibtex-export, do this now
            if (exportbibtex) {
                // show status text
                msgLabel.setText(resourceMap.getString("msgBibtextExport"));
                // write bibtex file
                ExportTools.writeBibTexFile(dataObj, bibtexObj, exportentries, filepath, resourceMap);
            }
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
        taskinfo.setExportOk(exportOk);
        taskinfo.setShowExportOkMessage(showOkMessage);
        // Close Window
        parentDialog.setVisible(false);
        parentDialog.dispose();
    }

    /**
     * This method creates a HTML-page that contains all the desktop-data (i.e.
     * all entries or modified entries on the desktop), where the output of the
     * HTML-page is identical to the view of the desktop. I.e. if the user
     * turned off entry-titles, these are also not exportet to the output file.
     *
     * @param node the starting point for the jTree-enumeration, either the root
     * node or a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     * @param exportcomments {@code true} if comments should also be exported,
     * {@code false} if comments should not be exported.
     */
    private void exportEntriesWithComments(DefaultMutableTreeNode node, boolean exportcomments) {
        // get a list with all children of the node
        Enumeration en = node.children();
        // go through all children
        while (en.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) en.nextElement();
            // if the child is a bullet...
            if (node.getAllowsChildren()) {
                // get bullet-text
                exportPage.append(createExportBullet(node, exportcomments));
            } // now we know we have an entry. so get the entry number...
            else {
                // add entry
                exportPage.append(createExportEntry(node, exportcomments));
            }
            // when the new node also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                exportEntriesWithComments(node, exportcomments);
            }
        }
    }

    /**
     * This method creates a HTML-page that contains all the desktop-data (i.e.
     * all entries or modified entries on the desktop), where the output of the
     * HTML-page is identical to the view of the desktop. I.e. if the user
     * turned off entry-titles, these are also not exportet to the output file.
     *
     * @param node the starting point for the jTree-enumeration, either the root
     * node or a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     * @param exportcomments {@code true} if comments should also be exported,
     * {@code false} if comments should not be exported.
     */
    private void exportEntriesWithCommentsOnly(DefaultMutableTreeNode node) {
        // get a list with all children of the node
        Enumeration en = node.children();
        // go through all children
        while (en.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) en.nextElement();
            // retrieve comment
            String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), System.lineSeparator());
            // check for valid comment
            if (com != null && !com.isEmpty()) {
                // if the child is a bullet...
                if (node.getAllowsChildren()) {
                    // get bullet-text
                    exportPage.append(createExportBullet(node, true));
                } // now we know we have an entry. so get the entry number...
                else {
                    // add entry
                    exportPage.append(createExportEntry(node, true));
                }
            }
            // when the new node also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                exportEntriesWithCommentsOnly(node);
            }
        }
    }

    /**
     * This method prepares the html-content for an exported bullet-point. this
     * method is used by {@link #exportEntriesWithCommentsToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithCommentsToPDF()
     * }
     * and
     * {@link #exportEntriesWithCommentsOnlyToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnlyToPDF()}.
     *
     * @param node the bullet-node, needed for timestamp and title-text
     * @return a html-snippet with the bullet as headline
     */
    private String createExportBullet(DefaultMutableTreeNode node, boolean exportcomments) {
        StringBuilder sb = new StringBuilder("");
        // retrieve bullet-level, so we can use subsections according to the bullet-level
        int bulletlevel = node.getLevel();
        while(bulletlevel-- > 0) {
            sb.append("#");
        }
        // append text
        sb.append(" ").
                append(TreeUtil.getNodeText(node)).
                append(System.lineSeparator()).
                append(System.lineSeparator());
        // check whether comments should be exported as well
        if (exportcomments) {
            // retrieve comment
            String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), System.lineSeparator());
            // check for valid comment
            if (com != null && !com.isEmpty()) {
                // append comment-text
                sb.append(resourceMap.getString("comment")).append(System.lineSeparator());
                // append comment
                sb.append(com).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * This method prepares the plain-text-content for an exported entry. this
     * method is used by
     * {@link #exportEntriesWithCommentsToText(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithCommentsToText()}
     * and
     * {@link #exportEntriesWithCommentsOnlyToText(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnlyToText()}.
     *
     * @param node the entry-node, needed for timestamp and entry-text
     * @return a html-snippet with the entry as content
     */
    private String createExportEntry(DefaultMutableTreeNode node, boolean exportcomments) {
        StringBuilder sb = new StringBuilder("");
        // we now want to check whether the user has made modifications to the entry's
        // content, which are only made to the desktop (the content of the entry in the main database
        // is not changed, so you can edit the desktop-entry without changing the entry's original
        // content - this is useful when you want to add some words/phrases between entries etc., which
        // should be applied only to the final text on the desktop, but not to the original entries).
        //
        // in case we have modified an entry on the desktop, this entry has a "content" element. to
        // retrieve the correct entry, we need to look for the unique timestamp of that entry - since
        // an entry could appear multiple times on the desktop, thus the entry number itself is no
        // valid value for retrieving the right entry. Therefore, each treenode has a user-object
        // assigned, which holds the unique timestamp
        String text = desktopObj.retrieveModifiedEntryContentFromTimestamp(TreeUtil.getNodeTimestamp(node));
        // retrieve entry-number
        int nr = TreeUtil.extractEntryNumberFromNode(node);
        // if nothing found, retrieve regular entry
        // that means, if the entry with the unique-timestamp has no or an empty content-element, the
        // entry was not modified - thus we retrieve the "original" entry.
        if (null == text || text.isEmpty()) {
            // get cleanded content, for plain text without any ubb-tags
            text = Tools.removeUbbFromString(Tools.convertUBB2MarkDown(dataObj.getZettelContent(nr)), false);
        } else {
            // else clean text from ubb-tags
            text = Tools.removeUbbFromString(text, false);
        }
        // check whether the user wants to export titles.
        // check whether the user wants to export titles.
        if (isHeadingVisible) {
            // first check whether we have a title or not
            String zetteltitle = dataObj.getZettelTitle(nr);
            // only prepare a title when we want to have the entry's number as title-prefix,
            // or if we have a title.
            if (!zetteltitle.isEmpty() || zettelNumberAsPrefix) {
                // each entry has at least the title "entry" plus its number.
                // we need this to search through the entries when the user clicks an entry on the
                // jTreeDesktop in the desktop-window
                if (zettelNumberAsPrefix) {
                    sb.append(resourceMap.getString("entryText")).append(" ").append(String.valueOf(nr));
                }
                // if we have a "real" title, append it...
                if (!zetteltitle.isEmpty()) {
                    if (zettelNumberAsPrefix) {
                        sb.append(": ");
                    }
                    sb.append(zetteltitle);
                }
                sb.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        // if we have content, add it.
        if (!text.isEmpty()) {
            sb.append(text);
        } else {
            // else add remark that entry is deleted
            sb.append(resourceMap.getString("deletedEntry"));
        }
        sb.append(System.lineSeparator()).append(System.lineSeparator());
        // if the user wishes to remove multiple line-breaks, do this here
        if (settingsObj.getRemoveLinesForDesktopExport()) {
            // retrieve current content
            text = sb.toString();
            // remove double line separaters
            text = text.replace(System.lineSeparator() + System.lineSeparator(), System.lineSeparator());
            // set back content
            sb = new StringBuilder(text);
        }
        // if the user wants to export remarks, do this here.
        if ((settingsObj.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_REMARKS) != 0) {
            // get entry's remarks
            String remarks = dataObj.getCleanRemarks(nr);
            // check whether we have any
            if (!remarks.isEmpty()) {
                // set headline indicating that we have remarks here
                sb.append(resourceMap.getString("remarksHeader")).append(System.lineSeparator()).append(System.lineSeparator());
                // init paragraph with class-attribute, so the user may change style aftwerwards
                sb.append(remarks).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        // check whether comments should be exported as well
        if (exportcomments) {
            // retrieve comment
            String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), System.lineSeparator());
            // check for valid comment
            if (com != null && !com.isEmpty()) {
                // append comment-text
                sb.append(resourceMap.getString("comment")).append(System.lineSeparator());
                // append comment
                sb.append(com).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        // check whether the user wants to export authors
        if ((settingsObj.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_AUTHORS) != 0) {
            sb.append(ExportTools.createPlainList(dataObj.getAuthors(nr), resourceMap.getString("NoAuthor"), resourceMap.getString("authorHeader"), "* ", ""));
        }
        // check whether user wants to export keywords.
        if ((settingsObj.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_KEYWORDS) != 0) {
            sb.append(ExportTools.createPlainList(dataObj.getKeywords(nr, true), resourceMap.getString("NoKeyword"), resourceMap.getString("keywordHeader"), "* ", ""));
        }

        return sb.toString();
    }
}
