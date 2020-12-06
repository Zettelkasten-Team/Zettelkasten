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
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import de.danielluedecke.zettelkasten.util.TreeUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Luedeke
 */
public class ExportToHtmlTask extends org.jdesktop.application.Task<Object, Void> {

    /**
     * Reference to the CDaten object, which contains the XML data of the
     * Zettelkasten will be passed as parameter in the constructor, see below
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
     *
     */
    private final boolean isHeadingVisible;
    /**
     *
     */
    private final boolean highlightKeywords;
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
    private StringBuilder exportTableOfContent = new StringBuilder("");
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
    private final boolean createTOC;
    /**
     *
     */
    private boolean showOkMessage = true;
    /**
     *
     */
    private DefaultMutableTreeNode treenode = null;
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

    public ExportToHtmlTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, javax.swing.JLabel label,
                            TasksData td, Daten d, DesktopData dt, Settings s, BibTeX bto, File fp, ArrayList<Object> ee, int type, int part,
                            DefaultMutableTreeNode n, boolean bibtex, boolean ihv, boolean hkws, boolean numberprefix, boolean toc) {
        super(app);
        dataObj = d;
        settingsObj = s;
        desktopObj = dt;
        bibtexObj = bto;
        filepath = fp;
        createTOC = toc;
        exporttype = type;
        exportparts = part;
        exportbibtex = bibtex;
        exportentries = ee;
        zettelNumberAsPrefix = numberprefix;
        isHeadingVisible = ihv;
        exportOk = true;
        highlightKeywords = hkws;
        treenode = n;
        taskinfo = td;
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
        // check whether file already exists
        if (filepath.exists()) {
            // file exists, ask user to overwrite it...
            int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg", "", filepath.getName()), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if the user does *not* choose to overwrite, quit...
            if (optionDocExists != JOptionPane.YES_OPTION) {
                // don't show "export was OK" message in main frame
                showOkMessage = false;
                return null;
            }
        }
        int contentsize;
        int counter;
        int commentExport;

        switch (exporttype) {
            //
            // here starts the export of the data in XML format
            //
            case Constants.EXP_TYPE_HTML:
            case Constants.EXP_TYPE_ODT:
            case Constants.EXP_TYPE_DOCX:
            case Constants.EXP_TYPE_RTF:
            case Constants.EXP_TYPE_EPUB:
                // create a new stringbuilder that will contain the final string, i.e.
                // the html-page which we set to the jeditorpane
                exportPage = new StringBuilder("<html><head>");
                // first of all, append the html-header with all style information
                exportPage.append(HtmlUbbUtil.getHtmlHeaderForExport(settingsObj)).append("</head><body>");
                // get the size of the export data, used for progressbar
                contentsize = exportentries.size();
                // go through all elements of the data file
                for (counter = 0; counter < exportentries.size(); counter++) {
                    // create export entries
                    exportPage.append(exportEntries(counter));
                    // update progress bar
                    setProgress(counter, 0, contentsize);
                }
                // show status text
                msgLabel.setText(resourceMap.getString("msg4"));
                // we don't want ho highlight any terms
                HtmlUbbUtil.setHighlighTerms(null, HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS, false);
                // convert all ubb into html...
                String htmlpage = HtmlUbbUtil.convertUbbToHtml(settingsObj, dataObj, bibtexObj, exportPage.toString(), Constants.FRAME_MAIN, true, true);
                // show status text
                msgLabel.setText(resourceMap.getString("msg3"));
                // create reference-list by extracting the authors from the author-footnotes.
                exportPage = new StringBuilder(htmlpage + ExportTools.createReferenceList(dataObj, settingsObj, htmlpage, Constants.footnoteHtmlTag, "\"", "<h1>", "</h1>", Constants.REFERENCE_LIST_HTML));
                // close html-page
                exportPage.append("</div></body></html>");
                // get the html-content from the stringbuilder that holds
                //the complete desktopdata in html
                String finalcontent = exportPage.toString();
                // convert special chars (German umlauts) to html-code
                finalcontent = finalcontent/*.replace("&", "&amp;")*/
                        .replace("ä", "&auml;")
                        .replace("Ä", "&Auml;")
                        .replace("ö", "&ouml;")
                        .replace("Ö", "&Ouml;")
                        .replace("ü", "&uuml;")
                        .replace("Ü", "&Uuml;")
                        .replace("ß", "&szlig;");
                // save content that should be exported and written to a file
                // into a string. the write-procedure is done later, see below
                exportPage = new StringBuilder(finalcontent);
                break;
            case Constants.EXP_TYPE_DESKTOP_HTML:
            case Constants.EXP_TYPE_DESKTOP_ODT:
            case Constants.EXP_TYPE_DESKTOP_DOCX:
            case Constants.EXP_TYPE_DESKTOP_RTF:
            case Constants.EXP_TYPE_DESKTOP_EPUB:
                // init variable
                exportPage = new StringBuilder("");
                exportTableOfContent = new StringBuilder("<h1>" + resourceMap.getString("TOC") + "</h1><br>");
                // get comment-export-options
                commentExport = settingsObj.getDesktopCommentExport();
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
                // check whether we have any content
                if (exportPage.length() > 0) {
                    // if yes, create footnotes.
                    exportPage = new StringBuilder(createFootnotes(createTOC));
                }
                // create dummy-string-builder
                StringBuilder buf = new StringBuilder("");
                // copy html-page to string
                String preparestring = exportPage.toString();
                // iterate each char of the string and encode UTF8-chars
                for (int i = 0; i < preparestring.length(); i++) {
                    // retrieve char
                    char c = preparestring.charAt(i);
                    // if it's a normal char, append it...
                    if ((int) c < 160) {
                        buf.append(c);
                    } else {
                        // else append entity of unicode-char
                        buf.append("&#").append((int) c).append(";");
                    }
                }
                // return results
                exportPage = new StringBuilder(buf.toString());
                // check for valid content
                if (exportPage.length() > 0) {
                    // convert special chars (German umlauts) to html-code
                    String desktopContentAsHTML = exportPage.toString();
                    desktopContentAsHTML = desktopContentAsHTML.replace("ä", "&auml;")
                            .replace("Ä", "&Auml;")
                            .replace("ö", "&ouml;")
                            .replace("Ö", "&Ouml;")
                            .replace("ü", "&uuml;")
                            .replace("Ü", "&Uuml;")
                            .replace("ß", "&szlig;");
                    // save content that should be exported and written to a file
                    // into a string. the write-procedure is done later, see below
                    exportPage = new StringBuilder(desktopContentAsHTML);
                } else {
                    // and change indicator
                    exportOk = false;
                    Constants.zknlogger.log(Level.SEVERE, "Error when exporting the desktop-content: no content available!");
                }
                break;
        }
        // show status text that file will be written
        msgLabel.setText(resourceMap.getString("msg2"));
        // do pandoc-conversion here
        if (exporttype != Constants.EXP_TYPE_HTML && exporttype != Constants.EXP_TYPE_DESKTOP_HTML) {
            // -------------------------------------------------------------------
            // Create string with output content
            // -------------------------------------------------------------------
            // remove "file://" from image tags, otherwise pandoc does not convert
            // images correctly
            String outputPage = exportPage.toString();
            String replaceStr = "file://";
            // on windows os, we have an additional separator char in the file://
            if (PlatformUtil.isWindows()) {
                replaceStr = replaceStr + File.separatorChar;
            }
            // replace all file:// orccurences within image tags
            outputPage = outputPage.replace("<img src=\"" + replaceStr, "<img src=\"");
            outputPage = outputPage.replaceAll("\\<img ([^\\[]*)\\>", "<p><img $1></p>");
            // retrieve output format
            String outformat = "docx";
            switch (exporttype) {
                case Constants.EXP_TYPE_DESKTOP_DOCX:
                case Constants.EXP_TYPE_DOCX:
                    outformat = "docx";
                    break;
                case Constants.EXP_TYPE_DESKTOP_ODT:
                case Constants.EXP_TYPE_ODT:
                    outformat = "odt";
                    break;
                case Constants.EXP_TYPE_DESKTOP_RTF:
                case Constants.EXP_TYPE_RTF:
                    outformat = "rtf";
                    break;
                case Constants.EXP_TYPE_DESKTOP_EPUB:
                case Constants.EXP_TYPE_EPUB:
                    outformat = "epub";
                    break;
            }
            // create several temporary pathes, in case the user dows not have
            // access to a specific tmp-dir
            String[] temppathes = new String[]{
                FileOperationsUtil.getZettelkastenHomeDir(),
                FileOperationsUtil.getZettelkastenDataDir(settingsObj),
                FileOperationsUtil.getWorkingDir()
            };
            // file name for tmp-file
            String temppath = "";
            File fn = null;
            // retrieve zettelkasten home dir as base path for 
            for (String tpath : temppathes) {
                // create temporary file
                fn = new File(tpath + Tools.getTimeStampWithMilliseconds() + FileOperationsUtil.getFileName(filepath) + ".html");
                // log filepath
                Constants.zknlogger.log(Level.INFO, "Verwendeter temporärer Pfad für Pandoc-Export: {0}", fn);
                // write export file
                exportOk = ExportTools.writeExportData(outputPage, fn);
                // wait a little bit
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ex) {
                    Constants.zknlogger.log(Level.SEVERE, "Waiting for export file interrupted...");
                }
                // check if we had success
                if (exportOk && fn.exists()) {
                    // set temp-path
                    temppath = tpath;
                    // break out of loop
                    break;
                }
            }
            // continue if export is ok
            if (exportOk && !temppath.isEmpty() && fn != null) {
                // create output filename
                File outfn = new File(temppath + FileOperationsUtil.getFileName(filepath) + "." + outformat);
                // create argument list
                List<String> args = Arrays.asList(settingsObj.getPandocPath(), "-f", "html", "-t", outformat, "-o", outfn.getAbsolutePath(), fn.getAbsolutePath());
                // start pandoc for conversion
                ProcessBuilder pb = new ProcessBuilder(args);
                // log parameter, for debugging
                Constants.zknlogger.log(Level.INFO, Arrays.toString(args.toArray(new String[args.size()])));
                // ste process working dir
                pb = pb.directory(fn.getParentFile());
                pb = pb.redirectInput(ProcessBuilder.Redirect.PIPE).redirectError(ProcessBuilder.Redirect.PIPE);
                // log filepath
                Constants.zknlogger.log(Level.INFO, "Verwendete temporäre Exportdatei für Pandoc-Export: {0}", outfn);
                try {
                    // start process
                    Process p = pb.start();
                    Scanner sc = null;
                    // catch error stream
                    StringBuilder errstr = new StringBuilder("");
                    // create scanner to receive compiler messages
                    try {
                        sc = new Scanner(p.getInputStream()).useDelimiter(System.lineSeparator());
                        // write output to text area
                        while (sc.hasNextLine()) {
                            errstr.append(System.lineSeparator()).append(sc.nextLine());
                        }
                    } catch (IllegalStateException ex) {
                        // log error stream
                        Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                    } finally {
                        if (sc != null) {
                            sc.close();
                        }
                    }
                    try {
                        // write output to text area
                        // create scanner to receive compiler messages
                        sc = new Scanner(p.getErrorStream()).useDelimiter(System.lineSeparator());
                        // write output to text area
                        while (sc.hasNextLine()) {
                            errstr.append(System.lineSeparator()).append(sc.nextLine());
                        }
                    } catch (IllegalStateException ex) {
                        // log error stream
                        Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                    } finally {
                        if (sc != null) {
                            sc.close();
                        }
                    }
                    // log error stream
                    Constants.zknlogger.log(Level.INFO, "Pandoc-Process-Log:" + System.lineSeparator() + "{0}", errstr.toString());
                    // wait for other process to be finished
                    p.waitFor();
                    p.destroy();
                } catch (IOException | InterruptedException ex) {
                    // and change indicator
                    exportOk = false;
                    Constants.zknlogger.log(Level.SEVERE, "Could not convert file! Either Pandoc is missing or export file not found.");
                } finally {
                    try {
                        // check whether file exists
                        exportOk = outfn.exists();
                        // log renaming result
                        if (!exportOk) {
                            Constants.zknlogger.log(Level.SEVERE, "Creating export file failed! Could not save tmp-file to {0}.", outfn.toString());
                        } else {
                            Constants.zknlogger.log(Level.INFO, "Successfully created tmp-file to {0}.", outfn.toString());
                        }
                        // check whether file could be moved
                        exportOk = outfn.renameTo(filepath);
                        fn.deleteOnExit();
                        // log renaming result
                        if (!exportOk) {
                            Constants.zknlogger.log(Level.SEVERE, "Creating export file failed! Could not rename to {0}.", filepath.toString());
                        } else {
                            Constants.zknlogger.log(Level.INFO, "Successfully renamed export file to {0}.", filepath.toString());
                        }
                    } catch (SecurityException ex) {
                        // and change indicator
                        exportOk = false;
                        Constants.zknlogger.log(Level.SEVERE, "Could not convert file! Access to destination file path denied!");
                    }
                }
            } else {
                Constants.zknlogger.log(Level.SEVERE, "Could not create export file!");
            }
        } else {
            // write export file
            exportOk = ExportTools.writeExportData(exportPage.toString(), filepath);
        }
        // if the user requested a bibtex-export, do this now
        if (exportbibtex) {
            // show status text
            msgLabel.setText(resourceMap.getString("msgBibtextExport"));
            // write bibtex file
            ExportTools.writeBibTexFile(dataObj, bibtexObj, exportentries, filepath, resourceMap);
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
     * @param counter
     * @return
     */
    private String exportEntries(int counter) {
        StringBuilder sb = new StringBuilder("");
        try {
            // retrieve zettelnumber
            int zettelnummer = Integer.parseInt(exportentries.get(counter).toString());
            // check whether the user wants to export titles.
            if ((exportparts & Constants.EXPORT_TITLE) != 0) {
                // first check whether we have a title or not
                String zetteltitle = dataObj.getZettelTitle(zettelnummer).replace("<", "&lt;").replace(">", "&gt;");
                // only prepare a title when we want to have the entry's number as title-prefix,
                // or if we have a title.
                if (!zetteltitle.isEmpty() || zettelNumberAsPrefix) {
                    // each entry has at least the title "entry" plus its number.
                    // we need this to search through the entries when the user clicks an entry on the
                    // jTreeDesktop in the desktop-window
                    sb.append("<h1>");
                    // surround entry header with ankh
                    sb.append("<a name=\"z_").append(String.valueOf(zettelnummer)).append("\">");
                    if (zettelNumberAsPrefix) {
                        sb.append(resourceMap.getString("entryText")).append(" ").append(String.valueOf(zettelnummer));
                    }
                    // if we have a "real" title, append it...
                    if (!zetteltitle.isEmpty()) {
                        if (zettelNumberAsPrefix) {
                            sb.append(": ");
                        }
                        sb.append(zetteltitle);
                    }
                    sb.append("</a>");
                    sb.append("</h1>").append(System.lineSeparator());
                }
            }
            // check whether the user wants to export content
            if ((exportparts & Constants.EXPORT_CONTENT) != 0) {
                // get the zettelcontent
                String zettelcontent = dataObj.getZettelContent(zettelnummer, true).replace("<", "&lt;").replace(">", "&gt;");
                // init paragraph with class-attribute, so the user may change style aftwerwards
                sb.append("<p class=\"zettelcontent\">");
                // if we have content, add it.
                if (!zettelcontent.isEmpty()) {
                    /*
                     // check whether keywords should be highlighted
                     if (highlightkeywords) {
                     // set current entry's keywords as highlight terms
                     CHtml.setHighlighTerms(dataObj.getSeparatedKeywords(zettelnummer));
                     // and highlight keywords within the entry's content
                     zettelcontent = CHtml.highlightSearchTerms(zettelcontent,null);
                     }
                     */
                    // check whether keywords should be highlighted
                    if (highlightKeywords) {
                        // set current entry's keywords as highlight terms
                        HtmlUbbUtil.setHighlighTerms(dataObj.getSeparatedKeywords(zettelnummer), HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS, settingsObj.getHighlightWholeWord());
                        // and highlight keywords within the entry's content
                        zettelcontent = HtmlUbbUtil.highlightSearchTermsInUBB(zettelcontent, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS);
                    }
                    sb.append(zettelcontent);
                } else {
                    // else add remark that entry is deleted
                    sb.append("<i>").append(resourceMap.getString("deletedEntry")).append("</i>").append(System.lineSeparator());
                }
                sb.append("</p>");
            }
            // if the user wants to export remarks, do this here.
            if ((exportparts & Constants.EXPORT_REMARKS) != 0) {
                // get entry's remarks
                String remarks = dataObj.getRemarks(zettelnummer);
                // check whether we have any
                if (!remarks.isEmpty()) {
                    // set headline indicating that we have remarks here
                    sb.append("<h4>").append(resourceMap.getString("remarksHeader")).append("</h4>").append(System.lineSeparator());
                    // init paragraph with class-attribute, so the user may change style aftwerwards
                    sb.append("<p class=\"para_remarks\">").append(remarks).append("</p>").append(System.lineSeparator());
                }
            }
            if ((exportparts & Constants.EXPORT_TIMESTAMP) != 0) {
                String[] timestamp = dataObj.getTimestamp(zettelnummer);
                // check whether we have a timestamp at all
                if (timestamp != null && !timestamp[0].isEmpty()) {
                    // prepare the html-part
                    sb.append("<p class=\"timestamp\">");
                    // and add the created-timestamp
                    sb.append(resourceMap.getString("timestampCreated")).append(" ").append(Tools.getProperDate(timestamp[0], false));
                    // check whether we have a modified-timestamp
                    // if we have a modified-stamp, add it...
                    if (timestamp.length > 1 && !timestamp[1].isEmpty()) {
                        sb.append("<br>").append(resourceMap.getString("timestampEdited")).append(" ").append(Tools.getProperDate(timestamp[1], false));
                    }
                    // and close the tags of the html-part
                    sb.append("</p>").append(System.lineSeparator());
                }
            }
            // check whether the user wants to export authors
            if ((exportparts & Constants.EXPORT_AUTHOR) != 0 && dataObj.hasAuthors(zettelnummer)) {
                sb.append(createHTMLList(dataObj.getAuthors(zettelnummer), resourceMap.getString("NoAuthor"), resourceMap.getString("authorHeader"), "author"));
            }
            // check whether user wants to export keywords.
            if ((exportparts & Constants.EXPORT_KEYWORDS) != 0 && dataObj.hasKeywords(zettelnummer)) {
                sb.append(createHTMLList(dataObj.getKeywords(zettelnummer, true), resourceMap.getString("NoKeyword"), resourceMap.getString("keywordHeader"), "list_keywords"));
            }
            if ((exportparts & Constants.EXPORT_LINKS) != 0 && dataObj.hasAttachments(zettelnummer)) {
                sb.append(createHTMLList(dataObj.getAttachmentsAsString(zettelnummer, true), resourceMap.getString("NoAttachment"), resourceMap.getString("attachmentHeader"), "list_attachments"));
            }
            if ((exportparts & Constants.EXPORT_MANLINKS) != 0 && dataObj.hasManLinks(zettelnummer)) {
                sb.append(createHTMLCommaList(dataObj.getManualLinksAsString(zettelnummer), resourceMap.getString("NoManLinks"), resourceMap.getString("manlinksHeader"), "list_manlinks"));
            }
            if ((exportparts & Constants.EXPORT_LUHMANN) != 0 && dataObj.hasLuhmannNumbers(zettelnummer)) {
                sb.append(createHTMLCommaList(dataObj.getLuhmannNumbersAsString(zettelnummer), resourceMap.getString("NoLuhmann"), resourceMap.getString("luhmannHeader"), "list_luhmann"));
            }
        } catch (NumberFormatException e) {
            // leave out first char, which is always a "H", set by the method
            // "createExportEntries()".
            sb.append("<h1 class=\"deskhead\">").append(exportentries.get(counter).toString().substring(2)).append("</h1>").append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * This method creates a list of entry-numbers, which are usually referring
     * from one entry to other entries (e.g. the follower-numbers or manual
     * links to other entries). The list is formatted in HTML, so it can be used
     * for HTML-export.
     *
     * @param values the refrerring entries of which a list should be created
     * (i.e. that should appear in this list)
     * @param notfound a String which is displayed if the supposed entry has no
     * referring entry-numbers (i.e. {@code values} is empty)
     * @param header a header which says what kind of list this is
     * @param cssclass a CSS-class-reference that is used as
     * {@code class}-attribute for the HTML-p element.
     * @return the complete list of entries which have been passed through the
     * parameter {@code values}, where this list is "formatted" as paragraph for
     * usage in HTML-export.
     */
    private String createHTMLCommaList(String[] values, String notfound, String header, String cssclass) {
        StringBuilder sb = new StringBuilder("");
        // if there is no keyword information, tell this the user
        if ((null == values) || (values.length < 1)) {
            sb.append("<p><i>").append(notfound).append("</i></p>").append(System.lineSeparator());
        } else {
            // create headline indicating that keyword-part starts here
            sb.append("<h4>").append(header).append("</h4>").append(System.lineSeparator());
            // init paragraph with class-attribute, so the user may change style aftwerwards
            sb.append("<p class=\"").append(cssclass).append("\">");
            // iterate the keyword array
            for (String val : values) {
                // and append each author
                sb.append(val).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("</p>").append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Creates a list of certain entry-values (keywords, authors, attachments)
     * in HTML-Format
     *
     * @param values the list-values (keywords, authors, attachments...) as
     * string-array
     * @param notfound the title of that list in case no elements have been
     * found
     * @param header the header of the list (e.g. keywords, authors...)
     * @param cssclass a class-attribute that should be added to the HTML-tags,
     * so the user can format the layout of the exported html-page afterwards.
     * @return a String containing the HTML-formatted list of entry-values
     */
    private String createHTMLList(String[] values, String notfound, String header, String cssclass) {
        StringBuilder sb = new StringBuilder("");
        // if there is no keyword information, tell this the user
        if ((null == values) || (values.length < 1)) {
            sb.append("<p><i>").append(notfound).append("</i></p>").append(System.lineSeparator());
        } else {
            // create headline indicating that keyword-part starts here
            sb.append("<h4>").append(header).append("</h4>").append(System.lineSeparator());
            // init paragraph with class-attribute, so the user may change style aftwerwards
            sb.append("<ul class=\"").append(cssclass).append("\">");
            // iterate the keyword array
            for (String val : values) {
                // autoconvert url's to hyperlinks
                // val = CHtml.convertHyperlinks(val.replace("<", "&lt;").replace(">", "&gt;"));
                // and append each author
                sb.append("<li>").append(val.replace("<", "&lt;").replace(">", "&gt;")).append("</li>").append(System.lineSeparator());
            }
            sb.append("</ul>").append(System.lineSeparator());
        }
        return sb.toString();
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
                // append bullet-point
                exportPage.append(createExportBullet(node));
                // check whether comments should be exported as well
                if (exportcomments) {
                    // retrieve comment
                    String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                    // check for valid comment
                    if (com != null && !com.isEmpty()) {
                        // append comment
                        exportPage.append("<p class=\"comment\">").append(com).append("</p>").append(System.lineSeparator());
                    }
                }
            } // now we know we have an entry. so get the entry number...
            else {
                // and append the html-text of the entry...
                exportPage.append(createExportEntry(node));
                // check whether comments should be exported as well
                if (exportcomments) {
                    // retrieve comment
                    String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                    // check for valid comment
                    if (com != null && !com.isEmpty()) {
                        // append comment and replace [br]-tags
                        exportPage.append("<p class=\"comment\">").append(com).append("</p>").append(System.lineSeparator());
                    }
                }
            }
            // when the new node also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                exportEntriesWithComments(node, exportcomments);
            }
        }
    }

    /**
     * This method prepares the html-content for an exported entry. this method
     * is used by {@link #exportEntriesWithComments(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithComments()
     * }
     * and
     * {@link #exportEntriesWithCommentsOnly(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnly()}.
     *
     * @param node the entry-node, needed for timestamp and entry-text
     * @return a html-snippet with the entry as content
     */
    private String createExportEntry(DefaultMutableTreeNode node) {
        // retrieve node's timestamp
        String timestamp = TreeUtil.getNodeTimestamp(node);
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
        String text = desktopObj.retrieveModifiedEntryContentFromTimestamp(timestamp);
        // retrieve entry-number
        int nr = TreeUtil.extractEntryNumberFromNode(node);
        // if nothing found, retrieve regular entry
        // that means, if the entry with the unique-timestamp has no or an empty content-element, the
        // entry was not modified - thus we retrieve the "original" entry.
        if (null == text || text.isEmpty()) {
            text = HtmlUbbUtil.getHtmlContentForDesktop(dataObj, bibtexObj, settingsObj, nr, isHeadingVisible, zettelNumberAsPrefix, true, true);
        } // else if we have a modified entry-content, we still need to convert its
        // ubb-tags to HTML. this is done here...
        else {
            // get the html-text for an entry which content is passed as parameter...
            text = HtmlUbbUtil.getHtmlContentForDesktop(dataObj, bibtexObj, settingsObj, text, nr, isHeadingVisible, zettelNumberAsPrefix, true, true);
        }
        // if the user wishes to remove multiple line-breaks, do this here
        if (settingsObj.getRemoveLinesForDesktopExport()) {
            text = text.replace("<br><br>", "<br>");
        }
        // now create the reference-ankh, so we can use the "scrollToReference" method
        // of the jEditorPane easily each time the user clicks on an entry in the jTree
        // to scroll to that entry in the text field.
        StringBuilder sb = new StringBuilder("");
        sb.append("<a name=\"");
        sb.append("entry").append(timestamp);
        sb.append("\">&nbsp;</a>").append(System.lineSeparator());
        sb.append(text);
        // retrieve entry's title
        String title = dataObj.getZettelTitle(nr);
        // if we have no title, use enty number instead
        if (title.isEmpty()) {
            title = resourceMap.getString("entryText") + " " + String.valueOf(nr);
        }
        // retrieve node-level, so we can use margins according the the depth of the node in the outline structure
        int lvl = node.getLevel();
        // set maximum level depth
        if (lvl > 5) {
            lvl = 5;
        }
        // convert to string for css-class
        String level = String.valueOf(lvl);
        // create toc-entry
        exportTableOfContent.append("<p class=\"tocentry").append(level).append("\"><a href=\"#entry").append(timestamp).append("\">").append(title).append("</a></p>").append(System.lineSeparator());
        // return result
        return sb.toString();
    }

    /**
     * This method prepares the html-content for an exported bullet-point. this
     * method is used by {@link #exportEntriesWithComments(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder, boolean) exportEntriesWithComments()
     * }
     * and
     * {@link #exportEntriesWithCommentsOnly(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesWithCommentsOnly()}.
     *
     * @param node the bullet-node, needed for timestamp and title-text
     * @return a html-snippet with the bullet as headline
     */
    private String createExportBullet(DefaultMutableTreeNode node) {
        StringBuilder sb = new StringBuilder("");
        // get node's timestamp
        String timestamp = TreeUtil.getNodeTimestamp(node);
        // retrieve bullet-level
        int bulletlevel = node.getLevel();
        // create html-tags for bullet
        sb.append("<h").append(String.valueOf(bulletlevel)).append(">");
        // now create the reference-ankh, so we can use the "scrollToReference" method
        // of the jEditorPane easily each time the user clicks on an entry in the jTree
        // to scroll to that entry in the text field.
        sb.append("<a name=\"");
        sb.append("entry").append(timestamp);
        sb.append("\">&nbsp;</a>");
        sb.append(TreeUtil.getNodeText(node));
        sb.append("</h").append(String.valueOf(bulletlevel)).append(">").append(System.lineSeparator());
        // retrieve node-level, so we can use margins according the the depth of the node in the outline structure
        int lvl = node.getLevel();
        int headerlvl = lvl + 1;
        // set maximum level depth
        if (lvl > 5) {
            lvl = 5;
        }
        if (headerlvl > 5) {
            headerlvl = 5;
        }
        // convert to string for css-class
        String level = String.valueOf(lvl);
        String headerlevel = String.valueOf(headerlvl);
        // add bullet-point to table of content
        exportTableOfContent.append("<h").append(headerlevel).append(" class=\"tocheader").append(level).append("\"><a href=\"#entry").append(timestamp).append("\">").append(TreeUtil.getNodeText(node)).append("</a></h").append(headerlevel).append(">").append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * This method creates a HTML-page that contains all the desktop-data (i.e.
     * all entries or modified entries on the desktop) which have comments
     * associated. Only bullet points or entries with comments are being
     * exported.<br><br>
     * The output of the HTML-page is identical to the view of the desktop. I.e.
     * if the user turned off entry-titles, these are also not exportet to the
     * output file.
     *
     * @param node the starting point for the jTree-enumeration, either the root
     * node or a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     */
    private void exportEntriesWithCommentsOnly(DefaultMutableTreeNode node) {
        // get a list with all children of the node
        Enumeration en = node.children();
        // go through all children
        while (en.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) en.nextElement();
            // retrieve comment
            String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
            // check for valid comment
            if (com != null && !com.isEmpty()) {
                // if the child is a bullet...
                if (node.getAllowsChildren()) {
                    // append bullet-point
                    exportPage.append(createExportBullet(node));
                    // append comment
                    exportPage.append("<p class=\"comment\">").append(com).append("</p>").append(System.lineSeparator());
                } // now we know we have an entry. so get the entry number...
                else {
                    // and append the html-text of the entry...
                    exportPage.append(createExportEntry(node));
                    // append comment
                    exportPage.append("<p class=\"comment\">").append(com).append("</p>").append(System.lineSeparator());
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
     *
     * @param createTOC
     * @return
     */
    private String createFootnotes(boolean createTOC) {
        // now prepare a reference list from possible footnotes
        LinkedList<String> footnotes = new LinkedList<>();
        // position index for finding the footnotes
        int pos = 0;
        // we need the content of the stringbuilder in a string that we can search through
        String dummysb = exportPage.toString();
        // do search as long as pos is not -1 (not-found)
        while (pos != -1) {
            // find the html-tag for the footnote
            pos = dummysb.indexOf(Constants.footnoteHtmlTag, pos);
            // if we found something...
            if (pos != -1) {
                // find the closing quotes
                int end = dummysb.indexOf("\"", pos + Constants.footnoteHtmlTag.length());
                // if we found that as well...
                if (end != -1) {
                    // extract footnote-number
                    String fn = dummysb.substring(pos + Constants.footnoteHtmlTag.length(), end);
                    // and add it to the linked list, if it doesn't already exist
                    if (-1 == footnotes.indexOf(fn)) {
                        footnotes.add(fn);
                    }
                    // set pos to new position
                    pos = end;
                } else {
                    pos = pos + Constants.footnoteHtmlTag.length();
                }
            }
        }
        // now we have all footnotes, i.e. the author-index-numbers, in the linked
        // list. now we can create a reference list
        if (footnotes.size() > 0) {
            // insert a paragraph for space
            exportPage.append("<p>&nbsp;</p>").append(System.lineSeparator());
            // first, init the list in html and add title "references"
            exportPage.append("<h1>").append(resourceMap.getString("referenceListHeading")).append("</h1>").append(System.lineSeparator());
            // open unordered list-tag
            exportPage.append("<ul>").append(System.lineSeparator());
            // iterator for the linked list
            Iterator<String> i = footnotes.iterator();
            while (i.hasNext()) {
                String au = i.next();
                try {
                    int aunr = Integer.parseInt(au);
                    exportPage.append("<li class=\"reflist\"><b>[<a name=\"fn_").append(au).append("\">").append(au).append("</a>]</b> ");
                    exportPage.append(dataObj.getAuthor(aunr));
                    exportPage.append("</li>").append(System.lineSeparator());
                } catch (NumberFormatException e) {
                    Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                }
            }
            // close unordered list-tag
            exportPage.append("</ul>").append(System.lineSeparator());
        }
        // add table of contents, if requested
        if (createTOC) {
            exportPage.insert(0, exportTableOfContent.toString() + "<br><p>&nbsp;</p><br>");
        }
        // and if so, insert style-definition
        exportPage.insert(0, HtmlUbbUtil.getHtmlHeaderForDesktopExport(settingsObj));
        // and close tags
        exportPage.append("</body></html>");
        // return result
        return exportPage.toString();
    }
}
