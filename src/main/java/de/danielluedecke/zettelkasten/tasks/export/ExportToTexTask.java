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

import de.danielluedecke.zettelkasten.ZettelkastenApp;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.TreeUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author danielludecke
 */
public class ExportToTexTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the CDaten object, which contains the XML data of the Zettelkasten will be
     * passed as parameter in the constructor, see below
     */
    private final Daten dataObj;
    /**
     *
     */
    private final DesktopData desktopObj;
    /**
     *
     */
    private final BibTeX bibtexObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     *
     */
    private final Settings settingsObj;
    /**
     * Indicates whether or not a bibtex-file from the exported entries should be created or not
     */
    private final boolean exportbibtex;
    /**
     *
     */
    private final boolean createTOC;
    /**
     *
     */
    private boolean zettelHasForms = false;
    /**
     * Defines whether each headline of any entry has its entry-number as prefix. If set to
     * {@code true}, each entry's title will appear as <i>Entry XY: entry title</i>, if set to
     * {@code false}, a title will just appear as <i>entry title</i>, or left out if title is empty.
     */
    private final boolean zettelNumberAsPrefix;
    /**
     *
     */
    private final boolean isHeadingVisible;
    /**
     * This variable stores the parts which should be exported. It's a mix of ORed constants, see
     * below
     */
    private final int exportparts;
    /**
     * indicates which type of data format should be exported to. refer to the Zettelkasten.view
     * properties file (resources) to see which number is which file type.
     */
    private final int exporttype;
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
    private StringBuilder exportPageHeader = new StringBuilder("");
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
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ExportTask.class);

    /**
     *
     * @param app
     * @param parent
     * @param label
     * @param td
     * @param d
     * @param dt
     * @param s
     * @param bto
     * @param fp
     * @param ee
     * @param type
     * @param part
     * @param n
     * @param bibtex
     * @param ihv
     * @param numberprefix
     * @param contenttable
     * @param sf whether each note should be saved as separate file
     */
    public ExportToTexTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
                           TasksData td, Daten d, DesktopData dt, Settings s, BibTeX bto, File fp, ArrayList<Object> ee, int type, int part,
                           DefaultMutableTreeNode n, boolean bibtex, boolean ihv, boolean numberprefix, boolean contenttable, boolean sf) {
        super(app);
        dataObj = d;
        settingsObj = s;
        desktopObj = dt;
        bibtexObj = bto;
        filepath = fp;
        exporttype = type;
        exportparts = part;
        exportbibtex = bibtex;
        exportentries = ee;
        zettelNumberAsPrefix = numberprefix;
        isHeadingVisible = ihv;
        exportOk = true;
        createTOC = contenttable;
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
        exportPageHeader = new StringBuilder("");
        // and create a counter for the progressbar's position
        int counter;
        switch (exporttype) {
            case Constants.EXP_TYPE_TEX:
                // get the size of the export data, used for progressbar
                int contentsize = exportentries.size();
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
                                // start section, i.e. LaTex heading 1. level
                                exportPage.append("\\section{");
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
                                exportPage.append("}");
                                exportPage.append(System.lineSeparator()).append(System.lineSeparator());
                            }
                        }
                        // check whether the user wants to export content
                        if ((exportparts & Constants.EXPORT_CONTENT) != 0) {
                            // get the zettelcontent
                            String zettelcontent = getConvertedTex(dataObj.getZettelContent(zettelnummer));
                            // if we have content, add it.
                            if (!zettelcontent.isEmpty()) {
                                // check whether we have form-tags
                                if (zettelcontent.contains(Constants.FORMAT_FORM_TAG)) {
                                    zettelHasForms = true;
                                }
                                // first, replace all footnotes with bibkeys
                                // to appropriate cite-tag in latex
                                zettelcontent = ExportTools.createLatexFootnotes(dataObj, zettelcontent, settingsObj.getLatexExportFootnoteRef());
                                // then add content
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
                                exportPage.append("\\subsection{").append(resourceMap.getString("remarksHeader")).append("}").append(System.lineSeparator()).append(System.lineSeparator());
                                // init paragraph with class-attribute, so the user may change style aftwerwards
                                exportPage.append(remarks).append(System.lineSeparator()).append(System.lineSeparator());
                            }
                        }
                        // check whether the user wants to export authors
                        if ((exportparts & Constants.EXPORT_AUTHOR) != 0 && dataObj.hasAuthors(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getAuthors(zettelnummer), resourceMap.getString("NoAuthor"), resourceMap.getString("authorHeader"), "\\subsection{", "}"));
                        }
                        // check whether user wants to export keywords.
                        if ((exportparts & Constants.EXPORT_KEYWORDS) != 0 && dataObj.hasKeywords(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getKeywords(zettelnummer, true), resourceMap.getString("NoKeyword"), resourceMap.getString("keywordHeader"), "\\subsection{", "}"));
                        }
                        if ((exportparts & Constants.EXPORT_LINKS) != 0 && dataObj.hasAttachments(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainList(dataObj.getAttachmentsAsString(zettelnummer, false), resourceMap.getString("NoAttachment"), resourceMap.getString("attachmentHeader"), "\\subsection{", "}"));
                        }
                        if ((exportparts & Constants.EXPORT_MANLINKS) != 0 && dataObj.hasManLinks(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainCommaList(dataObj.getManualLinksAsString(zettelnummer), resourceMap.getString("NoManLinks"), resourceMap.getString("manlinksHeader"), "\\subsection{", "}"));
                        }
                        if ((exportparts & Constants.EXPORT_LUHMANN) != 0 && dataObj.hasLuhmannNumbers(zettelnummer)) {
                            exportPage.append(ExportTools.createPlainCommaList(dataObj.getLuhmannNumbersAsString(zettelnummer), resourceMap.getString("NoLuhmann"), resourceMap.getString("luhmannHeader"), "\\subsection{", "}"));
                        }
                        if ((exportparts & Constants.EXPORT_TIMESTAMP) != 0) {
                            String timestamp = dataObj.getTimestampCreated(zettelnummer);
                            // check whether we have a timestamp at all
                            if (timestamp != null && !timestamp.isEmpty()) {
                                // and add the created-timestamp
                                exportPage.append(resourceMap.getString("timestampCreated")).append(" ").append(Tools.getProperDate(timestamp, false));
                            }
                            // check whether we have a modified-timestamp
                            timestamp = dataObj.getTimestampEdited(zettelnummer);
                            // check whether we have a timestamp at all
                            if (timestamp != null && !timestamp.isEmpty()) {
                                exportPage.append(System.lineSeparator()).append(resourceMap.getString("timestampEdited")).append(" ").append(Tools.getProperDate(timestamp, false));
                            }
                            // and close the tags of the html-part
                            exportPage.append(System.lineSeparator()).append(System.lineSeparator());
                        }
                        // separate files for each note?
                        if (separateFiles) {
                            // conver tags to tex
                            exportPage = new StringBuilder(HtmlUbbUtil.convertUbbToTex(settingsObj, dataObj, bibtexObj, exportPage.toString(), settingsObj.getLatexExportFootnoteRef(), settingsObj.getLatexExportCreateFormTags(), Constants.EXP_TYPE_DESKTOP_TEX == exporttype, settingsObj.getLatexExportRemoveNonStandardTags()));
                            // get note number and title for filepath
                            String fname = String.valueOf(zettelnummer) + " " + FileOperationsUtil.getCleanFilePath(zetteltitle);
                            // create file path
                            File fp = new File(separateFileDir + fname.trim() + ".tex");
                            // write export file
                            ExportTools.writeExportData(exportPage.toString(), fp);
                            // reset string builder
                            exportPage = new StringBuilder("");
                        } else {
                            // uncomment this, if the "end{zettel}" command should be enabled. As this leads to errors when comiling the latex file,
                            // the begin{zettel} and end{zettel} tags are not used.
                            // exportPage.append(System.lineSeparator()).append("\\end{zettel}").append(System.lineSeparator()).append(System.lineSeparator());
                            // close zettel-tags
                            exportPage.append(System.lineSeparator()).append(System.lineSeparator());
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
            case Constants.EXP_TYPE_DESKTOP_TEX:
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
            // create document-header
            createLatexHeader();
            // create footer with final information, including enddocument tag
            createLatexFooter();
            // show status text that string will be converted from UBB to LaTex
            msgLabel.setText(resourceMap.getString("msg4"));
            // convert all ubb into html...
            // save content that should be exported and written to a file
            // into a string. the write-procedure is done later, see below
            exportPage = new StringBuilder(HtmlUbbUtil.convertUbbToTex(settingsObj, dataObj, bibtexObj, exportPage.toString(), settingsObj.getLatexExportFootnoteRef(), settingsObj.getLatexExportCreateFormTags(), Constants.EXP_TYPE_DESKTOP_TEX == exporttype, settingsObj.getLatexExportRemoveNonStandardTags()));
            // insert document-header
            exportPage.insert(0, exportPageHeader.toString());
            // show status text that file will be written
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
     *
     */
    private void createLatexHeader() {
        // check if preamble is requested
        if (settingsObj.getLatexExportNoPreamble()) {
            return;
        }
        // init default document class
        String defaultdocclass = "[12pt,oneside,a4paper]{scrartcl}";
        // check for user defined document class
        int docclass = settingsObj.getLastUsedLatexDocClass();
        // check if it differs from default value
        if (docclass != 0 && docclass < Constants.LATEX_DOCUMENT_CLASS.length) {
            // set user defined document class
            defaultdocclass = "{" + Constants.LATEX_DOCUMENT_CLASS[docclass] + "}";
        }
        // first, we need the document class
        exportPageHeader.append("% Dokumentenklasse: 1-seitiger DIN-A4-Artikel (wissenschaftlich)").append(System.lineSeparator());
        exportPageHeader.append("\\documentclass").append(defaultdocclass).append(System.lineSeparator()).append(System.lineSeparator());
        // include package for new German orthography
        // exportPageHeader.append("% Neue deutsche Rechtschreibung").append(System.lineSeparator());
        // exportPageHeader.append("\\usepackage{ngerman}").append(System.lineSeparator()).append(System.lineSeparator());
        // include math package for spencer brown forms
        // exportPageHeader.append("% Neue deutsche Rechtschreibung").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage{amsmath,xcolor}").append(System.lineSeparator()).append(System.lineSeparator());
        // include package for UTF8-umlaut-endocing
        exportPageHeader.append("% Eingabe von Umlauten").append(System.lineSeparator());
        exportPageHeader.append("% Siehe http://www.tug.org/texlive/devsrc/Master/texmf-dist/doc/latex/base/inputenc.pdf").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage[utf8]{inputenc}").append(System.lineSeparator()).append(System.lineSeparator());
        // include package for quotes;
        exportPageHeader.append("% Anführungszeichen einfach setzen").append(System.lineSeparator());
        exportPageHeader.append("% Siehe http://mirrors.ctan.org/macros/latex/contrib/csquotes/csquotes.pdf").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage[german=quotes]{csquotes}").append(System.lineSeparator()).append(System.lineSeparator());
        // include package for footer / page count
        // but only, when non-standard is chosen
        if (docclass != 0) {
            exportPageHeader.append("% Seitenzahlen in Fußzeile rechts").append(System.lineSeparator());
            exportPageHeader.append("\\usepackage{scrpage2}").append(System.lineSeparator());
            exportPageHeader.append("\\pagestyle{scrheadings}").append(System.lineSeparator());
            exportPageHeader.append("\\rofoot{\\pagemark}").append(System.lineSeparator()).append(System.lineSeparator());
        }
        // include package for graphics
        exportPageHeader.append("% Paket für Grafiken einbinden").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage{graphicx}").append(System.lineSeparator()).append(System.lineSeparator());
        // include package for colors
        exportPageHeader.append("% Paket für farbige und flexible Tabellenzellen").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage{colortbl}").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage{tabularx}").append(System.lineSeparator());
        exportPageHeader.append("\\definecolor{DarkGray}{gray}{0.7}").append(System.lineSeparator());
        exportPageHeader.append("\\definecolor{LightGray}{gray}{0.9}").append(System.lineSeparator());
        exportPageHeader.append("\\renewcommand{\\arraystretch}{1.2}").append(System.lineSeparator()).append(System.lineSeparator());
        // include package for references
        exportPageHeader.append("% Paket für Literaturverweise einbinden").append(System.lineSeparator());
        exportPageHeader.append("\\usepackage{multibib} % Hier ändern für eigene Literatur-Stil-Pakete").append(System.lineSeparator());
        // no paragraph indent
        exportPageHeader.append("% Standardmäßig kein Einrücken von Absätzen").append(System.lineSeparator());
        exportPageHeader.append("\\parindent 0pt").append(System.lineSeparator()).append(System.lineSeparator());
        // check whether document contains form-tags
        if (zettelHasForms && settingsObj.getLatexExportCreateFormTags()) {
            // if so, append makro to document-header
            doTheSpencerBrown();
        }
        // begin document
        exportPageHeader.append("\\begin{document}").append(System.lineSeparator());
        // create title
        exportPageHeader.append("\\title{");
        // in case we export a desktop, use that name as title
        if (Constants.EXP_TYPE_DESKTOP_TEX == exporttype) {
            exportPageHeader.append(desktopObj.getCurrentDesktopName());
        } // else, use data file's name as title
        else {
            settingsObj.getFileName();
        }
        exportPageHeader.append("}").append(System.lineSeparator());
        // check whether author-name or email should be set
        if (settingsObj.getLatexExportShowMail() || settingsObj.getLatexExportShowAuthor()) {
            // retrieve author and mail values
            String doc_author = settingsObj.getLatexExportAuthorValue();
            String doc_mail = settingsObj.getLatexExportMailValue();
            // check whether we have values here
            exportPageHeader.append("\\author{");
            if (doc_author != null && !doc_author.isEmpty()) {
                exportPageHeader.append(doc_author).append("\\\\").append(System.lineSeparator());
            }
            if (doc_mail != null && !doc_mail.isEmpty()) {
                exportPageHeader.append("\\texttt{").append(doc_mail).append("}");
            }
            exportPageHeader.append("}").append(System.lineSeparator());
        }
        exportPageHeader.append("\\maketitle").append(System.lineSeparator());
        // create table of contents, if requestet
        if (createTOC) {
            exportPageHeader.append("\\tableofcontents").append(System.lineSeparator());
        }
        exportPageHeader.append(System.lineSeparator());
    }

    /**
     *
     */
    private void createLatexFooter() {
        // check if preamble is requested
        if (settingsObj.getLatexExportNoPreamble()) {
            return;
        }
        // retrieve filename of data file and bibtex file
        String filename = FileOperationsUtil.getFileName(filepath);
        String bibname = bibtexObj.getFileName();
        // check whether one is empty or null
        if (null == filename) {
            filename = "";
        }
        if (null == bibname) {
            bibname = "";
        }
        // new line
        exportPage.append(System.lineSeparator());
        // include bibtex-file. in case the user created a separate bibtex-file, this filename is used. else,
        // the filename of the default-attached bibtex-file is used.
        exportPage.append("\\bibliography{").append((exportbibtex) ? filename : bibname).append("}").append(System.lineSeparator());
        // create default bibliography style
        exportPage.append("\\bibliographystyle{").append(Constants.LATEX_BIB_STYLECODE[settingsObj.getLastUsedLatexBibStyle()]).append("} % Auskommentieren, wenn eigener Literatur-Stil eingebunden wird.").append(System.lineSeparator()).append(System.lineSeparator());
        // print references
        exportPage.append("% kann anstelle der beiden vorigen Zeilen verwendet werden,").append(System.lineSeparator());
        exportPage.append("% um eigene Literatur-Stile zu nutzen.").append(System.lineSeparator());
        exportPage.append("% \\printbibliography").append(System.lineSeparator());
        // close document-tag
        exportPage.append(System.lineSeparator()).append("\\end{document}");
    }

    /**
     *
     */
    private void doTheSpencerBrown() {
        exportPageHeader.append("% Dieses Makro erstellt Befehle, um die Form-Notation von George Spencer Brown").append(System.lineSeparator());
        exportPageHeader.append("% zu imitieren, die oft von Autoren im Bereich Systemtheorie verwendet wird.").append(System.lineSeparator());
        exportPageHeader.append("% Vielen Dank an die Benutzer des Mr. Unix Forums für die hilfreichen Tipps zur Umsetzung!").append(System.lineSeparator());
        exportPageHeader.append("% http://www.mrunix.de/forums/showthread.php?t=66803&page=2").append(System.lineSeparator());
        exportPageHeader.append("%").append(System.lineSeparator());
        exportPageHeader.append("% Beispiele: Form =\\cross{Innenseite}%").append(System.lineSeparator());
        exportPageHeader.append("% Beispiele: Form =\\cross{Innenseite}Außenseite%").append(System.lineSeparator());
        exportPageHeader.append("% Beispiele: Form =\\ReEntry{\\cross{\\cross{\\cross{Innenseite}Kontext 1}Kontext 2}Kontex 3}Außenseite").append(System.lineSeparator());
        exportPageHeader.append("%").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crossleftdist  %Abstand links vom\\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crossrightdist %Abstand rechts vom\\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosscontleftdist  %Abstand Inhalt links zum \\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosscontrightdist %Abstand Inhalt rechts zum \\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosscontabovedist %Freiraum Inhalt oben zum \\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crossdepth %Tiefe fürs \\cross (Freiraum unterm Inhalt)").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosslinethickness %Linienstärke fürs \\cross").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosshooklength  %die Länge des Hakens").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosscontdepth").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crosscontbelowdist").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crossAbove").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\crossBelow").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\formabovedist").append(System.lineSeparator());
        exportPageHeader.append("\\newlength\\formbelowdist").append(System.lineSeparator());
        exportPageHeader.append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\formabovedist{4ex} %Ändern für Abstand über Form").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\formbelowdist{2ex} %Ändern für Abstand unter Form").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crossleftdist{0.5em}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crossrightdist{0.5em}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crosscontleftdist{1pt}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crosscontrightdist{4pt}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crosscontabovedist{3.5pt}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crossdepth{5pt}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crosslinethickness{0.4pt}").append(System.lineSeparator());
        exportPageHeader.append("\\setlength\\crosshooklength{\\dimexpr\\crossdepth-3pt\\relax}").append(System.lineSeparator()).append(System.lineSeparator());
        exportPageHeader.append("\\newcommand*\\cross[1]{%").append(System.lineSeparator());
        exportPageHeader.append("  \\settodepth\\crosscontdepth{#1}%").append(System.lineSeparator());
        exportPageHeader.append("  \\setlength\\crosscontbelowdist{0pt}%").append(System.lineSeparator());
        exportPageHeader.append("  \\ifdim\\crosscontdepth=0pt").append(System.lineSeparator());
        exportPageHeader.append("    \\settodepth\\crosscontdepth{g}%").append(System.lineSeparator());
        exportPageHeader.append("    \\addtolength\\crosscontbelowdist{\\crosscontdepth}%").append(System.lineSeparator());
        exportPageHeader.append("  \\fi").append(System.lineSeparator());
        exportPageHeader.append("  \\raisebox{-\\crosscontdepth}{%").append(System.lineSeparator());
        exportPageHeader.append("  \\mbox{%").append(System.lineSeparator());
        exportPageHeader.append("    \\hbox{%").append(System.lineSeparator());
        exportPageHeader.append("      \\kern \\crossleftdist").append(System.lineSeparator());
        exportPageHeader.append("      \\vbox{%").append(System.lineSeparator());
        exportPageHeader.append("       \\hrule height \\crosslinethickness").append(System.lineSeparator());
        exportPageHeader.append("          \\kern\\crosscontabovedist").append(System.lineSeparator());
        exportPageHeader.append("          \\hbox{%").append(System.lineSeparator());
        exportPageHeader.append("            \\kern \\crosscontleftdist").append(System.lineSeparator());
        exportPageHeader.append("            \\raisebox{-\\crosscontdepth}{%").append(System.lineSeparator());
        exportPageHeader.append("              \\begingroup").append(System.lineSeparator());
        exportPageHeader.append("                \\setlength\\crossleftdist{-\\crosscontleftdist}%").append(System.lineSeparator());
        exportPageHeader.append("                  \\vphantom{b}#1%").append(System.lineSeparator());
        exportPageHeader.append("              \\endgroup").append(System.lineSeparator());
        exportPageHeader.append("            }%").append(System.lineSeparator());
        exportPageHeader.append("            \\kern \\crosscontrightdist").append(System.lineSeparator());
        exportPageHeader.append("          }%").append(System.lineSeparator());
        exportPageHeader.append("          \\kern \\crosscontbelowdist").append(System.lineSeparator());
        exportPageHeader.append("        }%").append(System.lineSeparator());
        exportPageHeader.append("        \\vrule width \\crosslinethickness %depth \\crosscontdepth").append(System.lineSeparator());
        exportPageHeader.append("        \\kern \\crossrightdist").append(System.lineSeparator());
        exportPageHeader.append("      }%").append(System.lineSeparator());
        exportPageHeader.append("    }%").append(System.lineSeparator());
        exportPageHeader.append("  }%").append(System.lineSeparator());
        exportPageHeader.append("}").append(System.lineSeparator());
        exportPageHeader.append("").append(System.lineSeparator());
        exportPageHeader.append("\\newcommand*\\ReEntry[1]{%").append(System.lineSeparator());
        exportPageHeader.append("  \\settodepth\\crosscontdepth{#1}%").append(System.lineSeparator());
        exportPageHeader.append("  \\settoheight\\crossAbove{#1}%").append(System.lineSeparator());
        exportPageHeader.append("  \\addtolength\\crossAbove{\\crosslinethickness}%").append(System.lineSeparator());
        exportPageHeader.append("  \\setlength\\crossBelow{\\dimexpr\\crosscontdepth+\\crossdepth\\relax}%").append(System.lineSeparator());
        exportPageHeader.append("  \\raisebox{-\\crossBelow}[\\crossAbove][\\dimexpr\\crossBelow+\\crosslinethickness\\relax]{%").append(System.lineSeparator());
        exportPageHeader.append("    \\mbox{%").append(System.lineSeparator());
        exportPageHeader.append("      \\hbox{%").append(System.lineSeparator());
        exportPageHeader.append("        \\kern \\crossleftdist").append(System.lineSeparator());
        exportPageHeader.append("        \\vrule width \\crosslinethickness height \\dimexpr\\crosshooklength+\\crosscontdepth\\relax").append(System.lineSeparator());
        exportPageHeader.append("        \\kern-\\crosslinethickness").append(System.lineSeparator());
        exportPageHeader.append("        \\vbox{%").append(System.lineSeparator());
        exportPageHeader.append("          \\hrule height \\crosslinethickness").append(System.lineSeparator());
        exportPageHeader.append("          \\kern \\crosscontabovedist").append(System.lineSeparator());
        exportPageHeader.append("          \\hbox{%").append(System.lineSeparator());
        exportPageHeader.append("            \\kern \\crosscontleftdist").append(System.lineSeparator());
        exportPageHeader.append("            \\begingroup").append(System.lineSeparator());
        exportPageHeader.append("              \\setlength\\crossleftdist{-\\crosscontleftdist}%").append(System.lineSeparator());
        exportPageHeader.append("                #1%").append(System.lineSeparator());
        exportPageHeader.append("            \\endgroup").append(System.lineSeparator());
        exportPageHeader.append("            \\kern \\crosscontrightdist").append(System.lineSeparator());
        exportPageHeader.append("          }%").append(System.lineSeparator());
        exportPageHeader.append("          \\kern \\crossdepth").append(System.lineSeparator());
        exportPageHeader.append("          \\hrule height 0pt depth \\crosslinethickness").append(System.lineSeparator());
        exportPageHeader.append("        }%").append(System.lineSeparator());
        exportPageHeader.append("        \\vrule width\\crosslinethickness").append(System.lineSeparator());
        exportPageHeader.append("        \\kern \\crossrightdist").append(System.lineSeparator());
        exportPageHeader.append("      }%").append(System.lineSeparator());
        exportPageHeader.append("    }%").append(System.lineSeparator());
        exportPageHeader.append("  }%").append(System.lineSeparator());
        exportPageHeader.append("}").append(System.lineSeparator()).append(System.lineSeparator());
        exportPageHeader.append("\\newcommand*\\FormAbstand[1]{%").append(System.lineSeparator());
        // prepare form-paragraph
        String formtag = "\\raisebox{0pt}[\\formabovedist][\\formbelowdist]#1";
        // check whether formtag should be centred
        if (settingsObj.getLatexExportCenterForm()) {
            formtag = "\\begin{center}" + formtag + "\\end{center}";
        }
        // enter % for significant empty spaces
        formtag = formtag + "%";
        // insert string
        exportPageHeader.append("  ").append(formtag).append(System.lineSeparator());
        exportPageHeader.append("}").append(System.lineSeparator()).append(System.lineSeparator());
    }

    /**
     *
     * @param zettelnummer
     * @param comment
     * @return
     */
    private String latexBeginTag(String comment) {
        StringBuilder sb = new StringBuilder("");
        // check whether a comment should be added
        if (comment != null && !comment.isEmpty()) {
            // insert line-wrapped comment first...
            sb.append(Tools.lineWrapText(comment, 40, "%"));
        }
        // return result
        return sb.toString();
    }

    /**
     * This method creates a HTML-page that contains all the desktop-data (i.e. all entries or
     * modified entries on the desktop), where the output of the HTML-page is identical to the view
     * of the desktop. I.e. if the user turned off entry-titles, these are also not exportet to the
     * output file.
     *
     * @param node the starting point for the jTree-enumeration, either the root node or a bullet
     * (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param sb the stringbuilder which finally will contain all content in html-format
     * @param exportcomments {@code true} if comments should also be exported, {@code false} if
     * comments should not be exported.
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
                // append bullet point
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
     * This method creates a HTML-page that contains all the desktop-data (i.e. all entries or
     * modified entries on the desktop), where the output of the HTML-page is identical to the view
     * of the desktop. I.e. if the user turned off entry-titles, these are also not exportet to the
     * output file.
     *
     * @param node the starting point for the jTree-enumeration, either the root node or a bullet
     * (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param sb the stringbuilder which finally will contain all content in html-format
     * @param exportcomments {@code true} if comments should also be exported, {@code false} if
     * comments should not be exported.
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
                    // append bullet point
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
     * This method prepares the html-content for an exported entry. this method is used by {@link #exportEntriesWithCommentsToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithCommentsToPDF()
     * }
     * and
     * {@link #exportEntriesWithCommentsOnlyToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnlyToPDF()}.
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
            // retrieve regular content
            text = dataObj.getZettelContent(nr);
        }
        // convert special chars and enquote quotes
        text = getConvertedTex(text);
        // retrieve comment
        String com = (exportcomments) ? desktopObj.getComment(TreeUtil.getNodeTimestamp(node), System.lineSeparator()) : "";
        // add each beginning entry-tag
        sb.append(latexBeginTag(com));
        // check whether the user wants to export titles.
        // check whether the user wants to export titles.
        if (isHeadingVisible) {
            // first check whether we have a title or not
            String zetteltitle = dataObj.getZettelTitle(nr);
            // only prepare a title when we want to have the entry's number as title-prefix,
            // or if we have a title.
            if (!zetteltitle.isEmpty() || zettelNumberAsPrefix) {
                // don't start section here, because the bullets indicate a new section.
                // instead, use bold formatting
                sb.append("\\textbf{");
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
                sb.append("}");
                sb.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        // if we have content, add it.
        if (!text.isEmpty()) {
            // check whether we have form-tags
            if (text.contains(Constants.FORMAT_FORM_TAG)) {
                zettelHasForms = true;
            }
            // first, replace all footnotes with bibkeys
            // to appropriate cite-tag in latex
            text = ExportTools.createLatexFootnotes(dataObj, text, settingsObj.getLatexExportFootnoteRef());
            // then append the content
            sb.append(text);
        } else {
            // else add remark that entry is deleted
            sb.append(resourceMap.getString("deletedEntry"));
        }
        // if the user wants to export remarks, do this here.
        if ((settingsObj.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_REMARKS) != 0) {
            // get entry's remarks
            String remarks = dataObj.getCleanRemarks(nr);
            // check whether we have any
            if (!remarks.isEmpty()) {
                // set headline indicating that we have remarks here
                sb.append(System.lineSeparator()).append("\\textit{").append(resourceMap.getString("remarksHeader")).append("}");
                // init paragraph with class-attribute, so the user may change style aftwerwards
                sb.append(remarks);
            }
        }
        // close zettel-tags
        // uncomment this, if the "end{zettel}" command should be enabled. As this leads to errors when comiling the latex file,
        // the begin{zettel} and end{zettel} tags are not used, see method "latexBeginTag()"
        // sb.append(System.lineSeparator()).append("\\end{zettel}").append(System.lineSeparator());
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
        return sb.toString();
    }

    /**
     * This method prepares the html-content for an exported bullet-point. this method is used by {@link #exportEntriesWithCommentsToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithCommentsToPDF()
     * }
     * and
     * {@link #exportEntriesWithCommentsOnlyToPDF(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnlyToPDF()}.
     *
     * @param node the bullet-node, needed for timestamp and title-text
     * @return a html-snippet with the bullet as headline
     */
    private String createExportBullet(DefaultMutableTreeNode node, boolean exportcomments) {
        StringBuilder sb = new StringBuilder("");
        // check whether comments should be exported as well
        if (exportcomments) {
            // retrieve comment
            String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), System.lineSeparator());
            // check for valid comment
            if (com != null && !com.isEmpty()) {
                // append comment
                sb.append(Tools.lineWrapText(com, 40, "%"));
            }
        }
        // retrieve bullet-level, so we can use subsections according to the bullet-level
        int bulletlevel = node.getLevel();
        // check which level the bullet is, so we can either indicate this heading as section
        // or sub(sub)section...
        switch (bulletlevel) {
            case 1:
                sb.append("\\section{");
                break;
            case 2:
                sb.append("\\subsection{");
                break;
            case 3:
                sb.append("\\subsubsection{");
                break;
            case 4:
                sb.append("\\paragraph{");
                break;
            case 5:
                sb.append("\\subparagraph{");
                break;
        }
        // get bullet-text
        String text = TreeUtil.getNodeText(node);
        // get converted special chars and enquotes quotes
        text = getConvertedTex(text);
        // append text
        sb.append(text).append("}").append(System.lineSeparator()).append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * This method enquotes all quotes in a string, so " will be converted to {@code \enquote}.
     *
     * @param content
     * @return
     */
    private String enquoteQuotes(String content) {
        // check for valid value
        if (null == content) {
            return null;
        }
        if (content.isEmpty()) {
            return "";
        }
        // check whether french quotes should be converted
        if (settingsObj.getLatexExportConvertQuotes()) {
            // convert french quotes into normal quotes
            content = content.replaceAll(Pattern.quote("»"), Matcher.quoteReplacement("\""));
            content = content.replaceAll(Pattern.quote("«"), Matcher.quoteReplacement("\""));
            content = content.replaceAll(Pattern.quote("›"), Matcher.quoteReplacement("\""));
            content = content.replaceAll(Pattern.quote("‹"), Matcher.quoteReplacement("\""));
        }
        // init pos-variables
        int pos = 0;
        int endpos;
        // search for quotes until we don't find anymore.
        while (pos != -1) {
            // find start of quotes
            pos = content.indexOf("\"", pos);
            // check whether it was found or not
            if (pos != -1) {
                // find end of quotes
                endpos = content.indexOf("\"", pos + 1);
                // if we also found an endquote, replace quotes with latex-command
                if (endpos != -1) {
                    // replace quotes with enquote-tag
                    content = content.substring(0, pos) + "\\enquote{" + content.substring(pos + 1, endpos) + "}" + content.substring(endpos + 1);
                } // if no endquote was found, quit loop
                else {
                    pos = -1;
                }
            }
        }
        return content;
    }

    private String convertSpecialChars(String dummy) {
        if (null == dummy) {
            return null;
        }
        if (dummy.isEmpty()) {
            return "";
        }
        // convert signs and special chars
//        dummy = dummy.replaceAll(Pattern.quote("#"), Matcher.quoteReplacement("\\#"));
        dummy = dummy.replaceAll(Pattern.quote("{"), Matcher.quoteReplacement("\\{"));
        dummy = dummy.replaceAll(Pattern.quote("}"), Matcher.quoteReplacement("\\}"));
        dummy = dummy.replaceAll(Pattern.quote("$"), Matcher.quoteReplacement("\\$"));
        dummy = dummy.replaceAll(Pattern.quote("%"), Matcher.quoteReplacement("\\%"));
        dummy = dummy.replaceAll(Pattern.quote("&"), Matcher.quoteReplacement("\\&"));
        dummy = dummy.replaceAll(Pattern.quote("_"), Matcher.quoteReplacement("\\_"));
        dummy = dummy.replaceAll(Pattern.quote("→"), Matcher.quoteReplacement("\\textrightarrow"));
        dummy = dummy.replaceAll(Pattern.quote("←"), Matcher.quoteReplacement("\\textleftarrow"));
        dummy = dummy.replaceAll(Pattern.quote("↑"), Matcher.quoteReplacement("\\textuparrow"));
        dummy = dummy.replaceAll(Pattern.quote("↓"), Matcher.quoteReplacement("\\textdownarrow"));
        dummy = dummy.replaceAll(Pattern.quote("<"), Matcher.quoteReplacement("\\langle"));

        // TODO XXX Hack: ">" should only be convered if Markdown is not enabled, otherwise, it will be
        // taken as a quotation by subsequent components. (fixes bug reproduced by
        // "testBugMarkdownZitatWirdNichtKorrektNachLatexExportiert")  
        if (!settingsObj.getMarkdownActivated()) {
            dummy = dummy.replaceAll(Pattern.quote(">"), Matcher.quoteReplacement("\\rangle"));
        }

        dummy = dummy.replaceAll(Pattern.quote("§"), Matcher.quoteReplacement("\\textsection"));
        dummy = dummy.replaceAll(Pattern.quote("$"), Matcher.quoteReplacement("\\$"));
        dummy = dummy.replaceAll(Pattern.quote("€"), Matcher.quoteReplacement("\\texteuro"));
        dummy = dummy.replaceAll(Pattern.quote("£"), Matcher.quoteReplacement("\\textsterling"));
        dummy = dummy.replaceAll(Pattern.quote("¼"), Matcher.quoteReplacement("\\textonequarter"));
        dummy = dummy.replaceAll(Pattern.quote("½"), Matcher.quoteReplacement("\\textonehalf"));
        dummy = dummy.replaceAll(Pattern.quote("¾"), Matcher.quoteReplacement("\\textthreequarters"));
        // check whether french quotes should be converted
        if (!settingsObj.getLatexExportConvertQuotes()) {
            dummy = dummy.replaceAll(Pattern.quote("»"), Matcher.quoteReplacement("\\guillemotright"));
            dummy = dummy.replaceAll(Pattern.quote("«"), Matcher.quoteReplacement("\\guillemotleft"));
        }
        return dummy;
    }

    private String convertSpecialChars2(String dummy) {
        // need to convert umlauts?
        if (null == dummy || dummy.isEmpty() || !settingsObj.getLatexExportConvertUmlaut()) {
            return dummy;
        }
        // convert signs and special chars
        dummy = dummy.replaceAll(Pattern.quote("ä"), Matcher.quoteReplacement("{\\\"a}"));
        dummy = dummy.replaceAll(Pattern.quote("ö"), Matcher.quoteReplacement("{\\\"o}"));
        dummy = dummy.replaceAll(Pattern.quote("ü"), Matcher.quoteReplacement("{\\\"u}"));
        dummy = dummy.replaceAll(Pattern.quote("Ä"), Matcher.quoteReplacement("{\\\"A}"));
        dummy = dummy.replaceAll(Pattern.quote("Ö"), Matcher.quoteReplacement("{\\\"O}"));
        dummy = dummy.replaceAll(Pattern.quote("Ü"), Matcher.quoteReplacement("{\\\"U}"));
        dummy = dummy.replaceAll(Pattern.quote("ß"), Matcher.quoteReplacement("{\\ss}"));
        return dummy;
    }

    private String getConvertedTex(String text) {
        return convertSpecialChars2(enquoteQuotes(convertSpecialChars(text)));
    }
}
