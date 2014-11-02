/*
 * Zettelkasten - nach Luhmann
 ** Copyright (C) 2001-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
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

package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.ToolbarIcons;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.Synonyms;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author danielludecke
 */
public class Tools {
    public static final int UBB2MARKDOWN = 1;
    public static final int MARKDOWN2UBB = 2;

    public static final String SEGMENT_POSITION_FIRST = "first";
    public static final String SEGMENT_POSITION_MIDDLE = "middle";
    public static final String SEGMENT_POSITION_LAST = "last";
    public static final String SEGMENT_POSITION_ONLY = "only";
    
    /**
     * get the strings for file descriptions from the resource map
     */
    private final static org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ZettelkastenView.class);
    /**
     * get the strings for file descriptions from the resource map
     */
    private final static org.jdesktop.application.ResourceMap toolbarResourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ToolbarIcons.class);
    /**
     *
     */
    private static boolean validhtml;


    /**
     * This method retrieves system information like the operating system and version, the architecture,
     * the used java runtime environment and the official vendor of the jre, and the java home directory.
     * @return a string with the above described system information
     */
    public static String getSystemInformation() {
        StringBuilder sysinfo = new StringBuilder("");
        sysinfo.append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" (").append(System.getProperty("os.arch")).append(")").append(System.lineSeparator());
        sysinfo.append("Java-Version ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")").append(System.lineSeparator());
        sysinfo.append(System.getProperty("java.home"));
        return sysinfo.toString();
    }


    /**
     * This method converts Zettelkasten ubb-format-chars like {@code [br]} or {@code &#9;} into
     * UTF-chars for a regular input in a text field (like \n or \t).
     *
     * @param text the text that contains UBB-tags like {@code [br]} or {@code &#9;}
     * @return a string with converted UTF-chars (line separators, tabs) used in a regular text field
     */
    public static String replaceUbbToUnicode(String text) {
        if (text!=null && !text.isEmpty()) {
            // replace all "br" with "real" new lines
            text = text.replace("[br]", System.lineSeparator());
            // replace all "tabs" with "real" tabs
            text = text.replace("&#9;", "\t");
            // replace all bullet-codes with "real" bullets
            text = text.replace("&#8226;", String.valueOf((char)8226));
        }
        return text;
    }


    /**
     * This method converts UTF-chars from a regular input or text field (like \n or \t)
     * into its Zettelkasten ubb-format.
     * <br><br>
     * Use this method if you want to make the content of {@code text} compatible with
     * the Zettelkasten's UBB-tags, i.e. if you need the content of {@code text} for
     * creating a new entry.
     *
     * @param text the text that contains UTF chars like line separators or tabs
     * @return a string with converted ubb-chars (line separators become {@code [br]}, tabs
     * {@code &#9;} etc.
     */
    public static String replaceUnicodeToUbb(String text) {
        if (text!=null && !text.isEmpty()) {
            // check whether the line-seperator has 2 chars. if so, remove first char...
            if (System.lineSeparator().contains("\r")) {
                text = text.replace("\r", "");
            }
            // ...only then this replacement will work
            text = text.replace("\n","[br]");
            // replace all "tabs" with "real" tabs
            text = text.replace("\t", "&#9;");
            // replace all bullet-codes with "real" bullets
            text = text.replace(String.valueOf((char)8226),"&#8226;");
        }
        return text;
    }

    
    /**
     * 
     * @param linktype
     * @param data
     * @param displayedZettel
     * @return 
     */
    public static boolean removeHyperlink(String linktype, Daten data, int displayedZettel) {
        // here we have a cross reference to another entry
        if (linktype.startsWith("#cr_")) {
            // only remove manual links from activated entry!
            if (displayedZettel!=data.getCurrentZettelPos()) return false;
            try {
                // if we have just a single selection, use phrasing for that message
                String msg = resourceMap.getString("askForDeleteManLinksMsgSingle");
                // ask whether author really should be deleted
                int option = JOptionPane.showConfirmDialog(null, msg, resourceMap.getString("askForDeleteManLinksTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                // if yes, do so
                if (JOptionPane.YES_OPTION == option ) {
                    // remove manual link
                    data.deleteManualLinks(new String[]{linktype.substring(4)});
                    return true;
                }                
            }
            catch (IndexOutOfBoundsException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        return false;
    }
    

    /**
     * This method opens a file or URL either from within a clicked link inside the jEditorPane (see
     * {@link #eventHyperlinkActivated(javax.swing.event.HyperlinkEvent) eventHyperlinkActivated(javax.swing.event.HyperlinkEvent)}
     * or from the attachment-list (see {@link #openAttachment() openAttachment()}.
     *
     * This method is called from the ZettelkastenView.class, the CDesktop.class and the CSearchResults.class.
     *
     * @param linktype a String value containing the URL of the clicked hyperlink
     * @param frame the frame which was the source from the editor pane that contained the hyperlink
     * @param sourceframe a reference to the frame from where this function call came. needed for
     * the html-formatting, since entries are differently formatted in the search window.
     * @param data a reference to the CDaten class
     * @param bibtexObj
     * @param settings a reference to the CSettings class
     * @param mainpane a reference to the JEditorPane that was the source of the hyperlink-event
     * @param displayedZettel the currently displayed entry. does not apply to the CDesktop.class
     * @return <ul>
     * <li>in case a literatur footnote was clicked, the related author-value is returned as string value.</li>>
     * <li>in case a rating-star was clicked, the value {@code #rateentry} with appended entry-number is returned.</li>
     * <li>in all other cases, {@code null} is returned.</li>
     * </ul>
     */
    public static String openHyperlink(String linktype, Frame frame, int sourceframe, Daten data, BibTex bibtexObj, Settings settings, JEditorPane mainpane, int displayedZettel) {
        // here comes the part that is not depending on the desktop-api
        //
        // here we have a reference (ankh) to the attachments, which are located at the
        // end of an entry
        if (linktype.equals("#hyperjump")) {
            mainpane.scrollToReference("hyperjump");
        }
        else if (linktype.equals("#activatedEntry")) {
            return linktype;
        }
        // here we have a literature footnote. if this link is activated, we want
        // to display the related author in the tabbed pane
        // otherwise try to open the file with the associated application
        else if (linktype.startsWith("#fn_")) {
            try {
                String au = "";
                // check sourceframe
                if (sourceframe!=Constants.FRAME_DESKTOP) {
                    // get and convert number
                    int aunr = Integer.parseInt(linktype.substring(4));
                    // get author from author-xml-file
                    au = data.getAuthor(aunr);
                    // create a new instance of the CHtml-class for highlighting
                    HtmlUbbUtil.setHighlighTerms(new String[] {Pattern.quote(au)}, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH,false);
                    // get the authors with new highlighted author that was selected from the footnote
                    String text = HtmlUbbUtil.getEntryAsHTML(settings, data, bibtexObj, displayedZettel, null, sourceframe);
                    // set new text
                    mainpane.setText(text);
                }
                // scroll to author in authortextfield
                mainpane.scrollToReference(linktype.substring(1));
                // when the user also wants to have the related author selected in the
                // jTableAuthors (see settings), do this now
                if (settings.getJumpFootnote()) {
                    return au;
                }
            }
            catch (NumberFormatException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        // here we have a reference to another entry
        else if (linktype.startsWith("#z_")) {
            try {
                // get and convert number
                int znr = Integer.parseInt(linktype.substring(3));
                // show entry
                if (data.zettelExists(znr) && !data.isDeleted(znr)) {
                    data.setCurrentZettelPos(znr);
                }
                // return String
                return linktype;
            }
            catch (NumberFormatException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        // here we have a cross reference to another entry
        else if (linktype.startsWith("#cr_")) {
            try {
                // get and convert number
                int znr = Integer.parseInt(linktype.substring(4));
                // show entry
                if (data.zettelExists(znr) && !data.isDeleted(znr)) {
                    data.setCurrentZettelPos(znr);
                }
                // return String
                return linktype;
            }
            catch (NumberFormatException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        // here we have a rating-request, i.e. the user clicked on the rating stars
        // and wants to rate the entry
        else if (linktype.startsWith("#rateentry")) {
            return linktype;
        }
        // here we have a edit cross reference
        else if (linktype.equalsIgnoreCase("#crt")) {
            return linktype;
        }
        // here we have a request for editing the timestamp
        else if (linktype.startsWith("#tstamp")) {
            return linktype;
        }
// TODO enable if movie-player is installed/possible.
// here we have a movie-url. if this link is activated, we want
// to play a moview in the hud-control-window (see CMoviePlayer).
// else if (linktype.startsWith("#mov")) {
// CMoviePlayer movplay = new CMoviePlayer(linktype.substring(4));
// movplay.showPlayer();
// }
        // here comes the part which depends on the desktop-api
        else {
            launchFile(linktype, frame, data, settings);
        }
        return null;
    }


    /**
     * 
     * @param linktype
     * @param frame
     * @param data
     * @param settings 
     */
    public static void launchFile(String linktype, Frame frame, Daten data, Settings settings) {
        // check whether linktype is a hyperlink
        if (FileOperationsUtil.isHyperlink(linktype)) {
            try {
                if (settings.getUseXDGOpen() && PlatformUtil.isLinux()) {
                    Runtime.getRuntime().exec("xdg-open "+linktype);
                    Constants.zknlogger.log(Level.INFO, "Using xdg-open with URL {0}", linktype);
                }
                // check whether desktop is supported
                else if (Desktop.isDesktopSupported()) {
                    // get the desktop
                    Desktop desk = Desktop.getDesktop();
                    // if yes, proceed...
                    // check whether opening a browser is supported or not...
                    if (!desk.isSupported(Desktop.Action.BROWSE)) {
                        // display error message box
                        JOptionPane.showMessageDialog(frame, resourceMap.getString("errLinkUnsopportedMsg"),resourceMap.getString("errLinkUnsopportedTitle"),JOptionPane.PLAIN_MESSAGE);
                        Constants.zknlogger.log(Level.WARNING, "Desktop.Action.BROWSE not supported!");
                    }
                    desk.browse(new URI(linktype));
                    Constants.zknlogger.log(Level.INFO, "Using desktop api with URL {0}", linktype);
                }
                // check whether we have windows os. if yes, use runtime exec instead of desktop
                else {
                    Constants.zknlogger.log(Level.WARNING, "Desktop-API not supported!");
                    if (PlatformUtil.isWindows()) {
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \""+linktype+"\"");
                        Constants.zknlogger.log(Level.INFO, "Using rundll32 with URL {0}", linktype);
                        // Runtime.getRuntime().exec("cmd /c start \""+linktype+"\"");
                    }
                    else if(PlatformUtil.isLinux()) {
                        Runtime.getRuntime().exec("xdg-open "+linktype);
                        Constants.zknlogger.log(Level.INFO, "Using xdg-open with URL {0}", linktype);
                    }
                }
            }
            catch (IOException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkNotFoundMsg",linktype),resourceMap.getString("errLinkNotFoundTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            catch (URISyntaxException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkSyntaxMsg"),resourceMap.getString("errLinkSyntaxTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            catch (SecurityException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkNoAccessMsg"),resourceMap.getString("errLinkNoAccessTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        // linktype seems to be a file
        else {
            File linkfile;
            File linuxpath;
            // create file from the link
            linkfile = FileOperationsUtil.getLinkFile(settings, data, linktype);
            // create path for linux with "file://" at beginning of string
            linuxpath = linkfile;
            // check whether path contains space chars
            if (linuxpath.toString().contains(" ")) {
                // if it fails, try to convert spaces with "%20"
                linuxpath = new File(linuxpath.toString().replaceAll(Pattern.quote(" "), Matcher.quoteReplacement("%20")));
            }
            try {
                // check whether desktop-api is supported
                if (Desktop.isDesktopSupported()) {
                    // get the desktop
                    Desktop desk = Desktop.getDesktop();
                    // if yes, proceed...
                    // if the string starts with "mailto:", we assume we have an email-link
                    if (linktype.startsWith("mailto:")) {
                        // check whether opening a browser is supported or not...
                        if (!desk.isSupported(Desktop.Action.MAIL)) {
                            // display error message box
                            JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkUnsopportedMsg"),resourceMap.getString("errLinkUnsopportedTitle"),JOptionPane.PLAIN_MESSAGE);
                        }
                        desk.mail(new URI(linktype));
                    }
                    else {
                        // check whether opening a file is supported or not
                        if (!desk.isSupported(Desktop.Action.OPEN) || PlatformUtil.isWindows()) {
                            // check whether we have windows os. if yes, use runtime exec instead of desktop
                            if (PlatformUtil.isWindows()) {
                                Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \""+linkfile.toString()+"\"");
                                Constants.zknlogger.log(Level.INFO, "Using rundll32 with filepath {0}", linkfile);
                            }
                            else if(PlatformUtil.isLinux()) {
                                Runtime.getRuntime().exec("xdg-open file://"+linuxpath.getPath());
                                Constants.zknlogger.log(Level.INFO, "Using xdg-open with filepath {0}", linuxpath.getPath());
                            }
                         }
                        else {
                            if(PlatformUtil.isLinux()) {
                                if (settings.getUseXDGOpen()) {
                                    Runtime.getRuntime().exec("xdg-open file://"+linuxpath.getPath());
                                    Constants.zknlogger.log(Level.INFO, "Using xdg-open with filepath {0}", linuxpath.getPath());
                                }
                                else {
                                    desk.open(linkfile);
                                    Constants.zknlogger.log(Level.INFO, "Using dekstop api with filepath {0}", linkfile.getPath());
                                }
                            }
                            else {
                                desk.open(linkfile);
                                Constants.zknlogger.log(Level.INFO, "Using desktop api with filepath {0}", linkfile.getPath());
                            }
                        }
                    }
                }
                // check whether we have windows os. if yes, use runtime exec instead of desktop
                else {
                    Constants.zknlogger.log(Level.WARNING, "Desktop-API not supported!");
                    if (PlatformUtil.isWindows()) {
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \""+linktype+"\"");
                        Constants.zknlogger.log(Level.INFO, "Using rundll32 with filepath {0}", linktype);
                        // Runtime.getRuntime().exec("cmd /c start \""+linktype+"\"");
                    }
                    else if(PlatformUtil.isLinux()) {
                        Runtime.getRuntime().exec("xdg-open file://"+linuxpath.getPath());
                        Constants.zknlogger.log(Level.INFO, "Using xdg-open with filepath {0}", linuxpath.getPath());
                    }
                }
            }
            catch (IOException | IllegalArgumentException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkNotFoundMsg",(linkfile!=null)?linkfile.toString():resourceMap.getString("linkFileUnknown")),resourceMap.getString("errLinkNotFoundTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            catch (SecurityException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkNoAccessMsg"),resourceMap.getString("errLinkNoAccessTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            catch (HeadlessException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkUnsopportedMsg"),resourceMap.getString("errLinkUnsopportedTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            catch (URISyntaxException e) {
                // display error message box
                JOptionPane.showMessageDialog(frame,resourceMap.getString("errLinkSyntaxMsg"),resourceMap.getString("errLinkSyntaxTitle"),JOptionPane.PLAIN_MESSAGE);
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
    }
    
    
    /**
     * This method checks the content of {@code content} for valid HTML and returns {@code true}
     * if the content could be parsed t HTML. With this, we check whether an entry makes use of correct
     * or irregular nested tags.
     *
     * @param content the html-page which should be checked for correctly nested tags, usually an entry's content
     * @param zettelnummer the number of the entry that is checked for valid html-tags
     * @return {@code true} when the content could be successfully parsed to HTML, false otherwise
     */
    public static boolean isValidHTML(String content, final int zettelnummer) {
        // check for valid html
        validhtml = true;
        // first, we parse the created web-page to catch errors that might occure when parsing
        // the entry-content. this might happen when tags are not properly used.
        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback () {
            // in case the parsing was not succssful, log that error message.
            @Override public void handleError(String errorMsg, int pos) {
                if (errorMsg.toLowerCase().contains("unmatched") || errorMsg.toLowerCase().contains("missing")) {
                    // if body tag is missing (which is true for all entries), don't log that message
                    if (!errorMsg.toLowerCase().contains("body")) {
                        // tell function that HTML is invalid.
                        validhtml = false;
                        errorMsg = System.lineSeparator()+"Error when parsing the entry "+String.valueOf(zettelnummer)+"!"+System.lineSeparator()+errorMsg+System.lineSeparator();
                        Constants.zknlogger.log(Level.SEVERE,errorMsg);
                    }
                }
            }
        };
        // create a string-reader that reads the entry's html-content
        Reader reader = new StringReader(content);
        // try to parse the html-page
        try {
            new ParserDelegator().parse(reader, callback, false);
        }
        catch (IOException ex) {
            Constants.zknlogger.log(Level.WARNING,ex.getLocalizedMessage());
        }
        return validhtml;
    }


    /**
     * This method cleans an HTML-entry from irregular nested tags by removing all [c] and [m] formatting tags.
     * @param dummy the entry's content that should be cleaned
     * @return the cleaned HTML-content for that entry.
     */
    public static String cleanHTML(String dummy) {
        // center alignment: [c] becomes <center>
        dummy = dummy.replaceAll("\\[c\\](.*?)\\[/c\\]", "$1");
        dummy = dummy.replaceAll("\\[m ([^\\[]*)\\](.*?)\\[/m\\]", "$2");
        return dummy;
    }



    /**
     * This method converts the timestamp-date into a readable format. The timestamp
     * of entries is provided as "yymmddhhmm", e.g. "0811051810" stands for "5th November 2008, 10:18 am"
     *
     * @param d the timestamp in original format
     * @param shortdate
     * @return the timestamp in a "readable" format
     */
    public static String getProperDate(String d, boolean shortdate) {
        // create an array with month names. we do this, because now we can
        // just edit the property-file for other languages/locales of the month-names
        String[] months = {
            resourceMap.getString("monthJan"),
            resourceMap.getString("monthFeb"),
            resourceMap.getString("monthMar"),
            resourceMap.getString("monthApr"),
            resourceMap.getString("monthMay"),
            resourceMap.getString("monthJun"),
            resourceMap.getString("monthJul"),
            resourceMap.getString("monthAug"),
            resourceMap.getString("monthSep"),
            resourceMap.getString("monthOct"),
            resourceMap.getString("monthNov"),
            resourceMap.getString("monthDec"),
        };
        // prepare the return-value
        StringBuilder retval = new StringBuilder("");
        // first, set the day. the day starts at position 4 within the string, always two digits
        // add a period and space behind it
        // here we first convert the substring into an int-value and than re-convert it to a string,
        // because by doing so we can have for instance "5" instead of "05", i.e. getting rid of
        // a leading zero.
        //
        // to check whether we are inside the boundaries when looking for the substring
        // we check for the length of the string
        if (d.length()>=6) {
            try {
                // get the day from the timestamp
                int day = Integer.parseInt(d.substring(4, 6));
                retval.append(String.valueOf(day)).append(". ");
                // now convert the month
                // therefore, retrieve the substring, convert it to an int-value and use this value
                // as index for the month array
                String mon = months[Integer.parseInt(d.substring(2,4))-1];
                if (shortdate) {
                    mon = mon.substring(0,3);
                }
                retval.append(mon).append(" ");
                // now the year, assuming that we will have the 21st century
                // fix this, when the new century 2100 begins :-)
                if (!shortdate) retval.append("20");
                retval.append(d.substring(0,2));
                if (!shortdate) retval.append(", ");
            }
            catch (NumberFormatException | IndexOutOfBoundsException ex) {
                Constants.zknlogger.log(Level.WARNING,ex.getLocalizedMessage());
            }
        }
        // to check whether we are inside the boundaries when looking for the substring
        // we check for the length of the string
        // and finally the time
        if (d.length()>=10 && !shortdate) {
            retval.append(d.substring(6,8)).append(":").append(d.substring(8,10));
        }
        // return the final string
        return retval.toString();
    }


    /**
     * This method converts the timestamp-date into a readable short format. The timestamp
     * of entries is provided as "yymmddhhmm", e.g. "0811051810" stands for "05.11.2008" 18:10
     *
     * @param d the timestamp in original format
     * @return the timestamp in a "readable" format
     */
    public static String getProperShortDate(String d) {
        // prepare the return-value
        StringBuilder retval = new StringBuilder("");
        // first, set the day. the day starts at position 4 within the string, always two digits
        // add a period and space behind it
        // here we first convert the substring into an int-value and than re-convert it to a string,
        // because by doing so we can have for instance "5" instead of "05", i.e. getting rid of
        // a leading zero.
        //
        // to check whether we are inside the boundaries when looking for the substring
        // we check for the length of the string
        if (d.length()>=6) {
            try {
                // get the day from the timestamp
                int day = Integer.parseInt(d.substring(4,6));
                retval.append(String.format("%02d", day)).append(".");
                // now convert the month
                // therefore, retrieve the substring, convert it to an int-value and use this value
                // as index for the month array
                int mon = Integer.parseInt(d.substring(2,4));
                retval.append(String.format("%02d", mon)).append(".");
                // now the year, assuming that we will have the 21st century
                // fix this, when the new century 2100 begins :-)
                retval.append("20").append(d.substring(0,2)).append(" ");
                // check for valid length
                if (d.length()>=10) {
                    // get the hour from the timestamp
                    int hour = Integer.parseInt(d.substring(6,8));
                    retval.append(String.format("%02d", hour)).append(":");
                    // get the minutes from the timestamp
                    int min = Integer.parseInt(d.substring(8,10));
                    retval.append(String.format("%02d", min));
                }
            }
            catch (NumberFormatException | IndexOutOfBoundsException ex) {
                Constants.zknlogger.log(Level.WARNING,ex.getLocalizedMessage());
            }
        }
        // return the final string
        return retval.toString();
    }


    /**
     * This methods retrieves entry-numbers from an input-field, where the user can enter certain entry-numbers,
     * comma-separated, including ranges.<br><br>
     * E.g. if the user enters following: "3,5,7-9", then this method would return an integer-array containing
     * the values 3,5,7,8,9.
     *
     * @param input the user-input from a textfield, given as string
     * @param len the length of the dataset, so we don't have any entry-numbers out of bounds
     * @return an integer-array with the converted entry-numbers from the user input, or null if an error occured or
     * the input contained invalid numbers.
     */
    public static int[] retrieveEntryNumbersFromInput(String input, int len) {
        // parse input at each comma
        String[] entries = input.split(",");
        // create linked list that will contain all entries...
        List<Integer> finalentries = new ArrayList<>();
        // go through all parts of the input
        for (String e : entries) {
            // remove leading/trailing space chars
            e = e.trim();
            // if we find a "-", add all entries from lower to upper range
            if (e.contains("-")) {
                // find the position of the divider
                int pos = e.indexOf("-");
                if (pos!=-1) {
                    try {
                        // get the value befor that divider
                        String lowerS = e.substring(0, pos).trim();
                        // and get the value behind that divider
                        String upperS = e.substring(pos+1).trim();
                        // convert strings to integer
                        int from = Integer.parseInt(lowerS);
                        int to = Integer.parseInt(upperS);
                        // only proceed, when lower range is smaller than upper range
                        if ((from<to) && (from>0) && (to<=len)) {
                            // now add all entries from lower to higher to the bullet
                            for (int cnt=from; cnt<=to; cnt++) {
                                finalentries.add(cnt);
                            }
                        }
                        else {
                            // tell user about invalid value
                            JOptionPane.showMessageDialog(null,resourceMap.getString("errInvalidValueMsg"),resourceMap.getString("errInvalidValueTitle"),JOptionPane.PLAIN_MESSAGE);
                            return null;
                        }
                    }
                    catch (NumberFormatException | IndexOutOfBoundsException ex) {
                        // tell user about invalid value
                        JOptionPane.showMessageDialog(null,resourceMap.getString("errInvalidValueMsg"),resourceMap.getString("errInvalidValueTitle"),JOptionPane.PLAIN_MESSAGE);
                        return null;
                    }
                }
            }
            else {
                try {
                    // convert string to integer
                    int nr = Integer.parseInt(e);
                    // check for valid entry...
                    if (nr<0 || nr>len) {
                        // tell user about invalid value
                        JOptionPane.showMessageDialog(null,resourceMap.getString("errInvalidValueMsg"),resourceMap.getString("errInvalidValueTitle"),JOptionPane.PLAIN_MESSAGE);
                        return null;
                    }
                    // else add nr
                    finalentries.add(nr);
                }
                catch (NumberFormatException ex) {
                    // tell user about invalid value
                    JOptionPane.showMessageDialog(null,resourceMap.getString("errInvalidValueMsg"),resourceMap.getString("errInvalidValueTitle"),JOptionPane.PLAIN_MESSAGE);
                    return null;
                }
            }
        }
        // create return value
        int[] retval = null;
        // check whether we have any entries
        if (finalentries.size()>0) {
            // init return value
            retval = new int[finalentries.size()];
            // copy entries from list to array
            for (int cnt=0; cnt<retval.length;cnt++) {
                retval[cnt] = finalentries.get(cnt);
            }
        }
        // return result
        return retval;
    }


    /**
     * This methods checks whether a string contains transfer-data from a table, and if so, extracts
     * the entry-numbers from this string.<br><br>
     * A string copied or dragged from a jTable may consist of several lines, where each line has
     * an entrynumber (first cell), a tab as separator char and a title (as 2nd cell).<br><br>
     * Thus, a dropped string might look like this:<br>
     * {@code 3   This is the third entry}<br>
     * {@code 6   This is number six}<br>
     * {@code 9   My last entry}
     *
     * @param str the string that was dropped or pasted, i.e. received by the transfer handler
     * @param maxcount
     * @return an integer-array with all entry-numbers that could be extracted, or {@code null}
     * if the string did not contain any entrynumbers.
     */
    public static int[] retrieveEntryNumbersFromTransferHandler(String str, int maxcount) {
        // check whether we have any valid string
        if (str!=null) {
            // create integer list for entry numbers
            ArrayList<Integer> entries = new ArrayList<>();
            // split drop-string at each new line
            String[] lines = str.split("\n");
            // iterate all line
            for (String line : lines) {
                // if line is not empty, split it at each cell (tab-separated)
                if (!line.isEmpty()) {
                    // retrieve cell-data
                    String[] cells = line.split("\t");
                    // if we have any cells, go on
                    if (cells.length>0) {
                        try {
                            // try to convert data of each first cell into an integer
                            int e = Integer.parseInt(cells[0]);
                            // if succeeded, and entry is within valid range,
                            // add it to list
                            if (e>0 && e<=maxcount) {
                                entries.add(e);
                            }
                        }
                        catch (NumberFormatException ex) {

                        }
                    }
                }
            }
            // if there were any entries in the drop-data, copy them to an int-array
            if (entries.size()>0) {
                int[] retval = new int[entries.size()];
                for (int cnt=0; cnt<entries.size(); cnt++) {
                    retval[cnt] = entries.get(cnt);
                }
                return retval;
            }
        }
        return null;
    }


    /**
     * This method retrieves multiple occurences of the entries that are passed in the linked list
     * {@code addedEntries} and puts them together to a linked list of type {@code Object []}.
     *
     * @param desktopObj the reference to the CDesktopData-class
     * @param addedEntries a linked list of integer values, containing all entry-numbers that should be
     * looked after for multiple occurences
     * @return a linked list of {@code Object []}, where each object-array has the 2 fields: {@code Object[0]}
     * containing the desktop-name as string, and {@code Object[1]} a linked list of type {@code Element} that
     * contains all found entries as Element-values.
     */
    public static List<Object[]> retrieveDoubleEntries(DesktopData desktopObj, List<Integer> addedEntries) {
        // create a linked list that will scan for multiple occurences of the added entries
        List<Object[]> multipleentries = new ArrayList<>();
        // first we go through all saved desktops we have and look
        // for occurences of the to be added entry. if the entry exists
        // in one or more desktops, the element is stored in our linked list.
        // if the entry does not exist, "null" is added to our linked list.
        // below we can than inform the user about multiple occurences of entries,
        // their location in the desktops and the timestamp when that entry was
        // added in the past
        for (int me=0; me<desktopObj.getCount(); me++) {
            // create a new object array for the relevant data
            Object[] o = new Object[2];
            // store the desktopname
            o[0] = desktopObj.getDesktopName(me);
            // now search for double entries (of those added entries) within this desktop
            // and store them in the array
            Iterator<Integer> it = addedEntries.iterator();
            // since we may find multiple entries, we create a new linked
            // list here, that will store *all* found entries of the current desktop
            List<Element> finallist = new ArrayList<>();
            // go through all found entries...
            // what we do here is following: we may have several different entries that already
            // have been added to this desktop before. these entries, if we have any, are
            // stored in the linked list "addedEntries". Now we have to iterate this list for
            // each existing desktop. each entry of the list "addedEntries" may appear one or more(!)
            // times in a desktop (e.g., the entry "16" may occure in several sub-bullets).
            // so calling the function "desktopObj.searchForEntry(me, it.next())" may return
            // several(!) Elements in a linked list for each(!) entry. thus, we may have to
            // concatenate several linked lists together. this is achieved by iterating the
            // linked list "lle" and adding each element of it to the resulting linked list "finallist"
            // for each entry within the found entries list "addedEntries".
            // finally, if we have any elements in our "finallist", it is added to the object-array "o".
            while (it.hasNext()) {
                // retrieve all found entries within the desktop
                List<Element> lle = desktopObj.searchForEntry(me, it.next());
                // check whether we have any returned entries...
                if (lle!=null && lle.size()>0) {
                    // create a new iterator for the found results
                    Iterator<Element> prepare = lle.iterator();
                    // iterate them
                    while (prepare.hasNext()) {
                        // get each single entry as element
                        Element e = prepare.next();
                        // and add it to the final list
                        if (e!=null) {
                            finallist.add(e);
                        }
                    }
                }
            }
            o[1] = (finallist.size()>0) ? finallist : null;
            // add the object to our linked list, so this list will contain the desktop-name
            // and the possible double entries als elements.
            multipleentries.add(o);
        }
        return multipleentries;
    }
    /**
     * This method prepares a message that tells the user which entries already appear in the desktop, and
     * at which position. the complete message is returned as string.
     *
     * @param list a linked list which contains the multiple-entry-data. see
     * {@link #retrieveDoubleEntries(zettelkasten.CDesktopData, java.util.LinkedList) retrieveDoubleEntries(zettelkasten.CDesktopData, java.util.LinkedList)}
     * for more details on how this parameter is created. use the return result of this method as this parameter
     * @return a string with the message which entries are at which position in the desktop-data, or {@code null}
     * if no occurences appear.
     */
    public static String prepareDoubleEntriesMessage(List<Object[]> list) {
        // retrieve system's line-separator
        String lineseparator = System.lineSeparator();
        // get an iterator for the multiple entries and check
        // whether we have any multiple occurences at all. if yes,
        // tell the user about that
        Iterator<Object[]> i = list.iterator();
        // prepare a string builder that will contain the information-message in case
        // we have any multiple occurences of entries...
        StringBuilder multipleOccurencesMessage = new StringBuilder("");
        // go through all entries of the linked list and check
        // whether we have found anything
        while (i.hasNext()) {
            // get element
            Object[] desktopdata = i.next();
            // if second element in array is not null, we have a match. now retrieve
            // the entry's data, so we can inform the user about the
            // entry's details...
            if (desktopdata[1]!=null) {
                // retrieve desktop name
                String dn = resourceMap.getString("multipleOccurencesDesktop")+" "+(String)desktopdata[0];
                StringBuilder dnsl = new StringBuilder("");
                // now we add a separator line, so check length of string
                for (int dnl=0; dnl<dn.length(); dnl++) dnsl.append("-");
                // first, append desktop-name
                multipleOccurencesMessage.append(dn).append(lineseparator);
                multipleOccurencesMessage.append(dnsl.toString()).append(lineseparator);
                // now retrieve the elements...
                List<Element> elements = (ArrayList<Element>)desktopdata[1];
                // create iterator for each found element
                Iterator<Element> entryIterator = elements.iterator();
                // go through the found entries in that desktop
                while (entryIterator.hasNext()) {
                    // get each found entry as element
                    Element entry = entryIterator.next();
                    // get the timestamp of the found entry
                    String timestamp = entry.getAttributeValue("timestamp");
                    // get the entrynumber of the found entry
                    String id = entry.getAttributeValue("id");
                    // create a linked list that will hold the path to the desktop
                    List<String> path = new ArrayList<>();
                    // as long as the found element has parents, we have path-elements/information
                    // to add...
                    while(entry.getParentElement()!=null) {
                        // retrieve parent-element
                        entry = entry.getParentElement();
                        // if it's a bullet, add the path-name to our path-list
                        if (entry.getName().equals("bullet")) {
                            path.add(0, entry.getAttributeValue("name"));
                        }
                    }
                    // now we can prepare the output string...
                    multipleOccurencesMessage.append(resourceMap.getString("multipleOccurencesMsg", id, getProperDate(timestamp,false)));
                    multipleOccurencesMessage.append(lineseparator).append(resourceMap.getString("multipleOccurencesLevel")).append(" ");
                    // go through the path-list and append all path-elements, so the user
                    // knows where to find the entry
                    for (int cnt=0; cnt<path.size(); cnt++) {
                        // add path
                        multipleOccurencesMessage.append(path.get(cnt));
                        // as long as we have a path-element left, append a separating comma
                        if (cnt<path.size()-1) {
                            multipleOccurencesMessage.append(" >>> ");
                        }
                    }
                    // append two line-separators for the next element...
                    multipleOccurencesMessage.append(lineseparator).append(lineseparator);
                }
            }
        }
        // delete the last two trailing lineseparators
        if (multipleOccurencesMessage.length()>0) {
            multipleOccurencesMessage.setLength(multipleOccurencesMessage.length()-2*lineseparator.length());
        }
        // if we have any content, return string. else return null
        return (multipleOccurencesMessage.length()>0) ? multipleOccurencesMessage.toString() : null;
    }


    /**
     * This method checks whether a list of {@code keywords} contains values that matches a synonym in
     * the synonyms-datafile, where the keyword-value is a synonym, but <i>no</i> index-word. If synonyms in the
     * {@code keywords}-array have been found, the user is asked whether he wants to replace these synonyms
     * by their index-words (which is recommended to do so, because keywords in general should be the
     * index-word of a synonym-line).
     *
     * @param synonymsObj a reference to the synonyms-data-class {@code CSynonyms}
     * @param keywords an array of keywords that should be checked, whether one or more elements of this
     * array are synonyms, but no index-word
     * @return if the user agrees, a "cleaned" array containing only keywords which appear as index-words of
     * synonyms only, or the original array which was passed as parameter if the user does not agree to
     * do the replacement. In case the user <i>cancelled</i> the action, {@code null} is returned.
     */
    public static String[] replaceSynonymsWithKeywords(Synonyms synonymsObj, String[] keywords) {
        // create linked list that will hold the keywords which have been recognized as synonyms, but not
        // as index-words
        List<String> synkeywords = new ArrayList<>();
        // check keywords whether they appear as synonyms, but not index-word
        for (String skw : keywords) {
            if ((synonymsObj.findSynonym(skw, true)!=-1) && !synonymsObj.isIndexWord(skw, true)) {
                synkeywords.add(skw);
            }
        }
        // if we have any matches, go on here
        if (synkeywords.size()>0) {
            // prepare option-message
            StringBuilder msg = new StringBuilder("");
            for (String synkeyword : synkeywords) {
                msg.append("<br>" + "- <b>").append(synkeyword).append("</b> <i>(Index-Synonym: ").append(synonymsObj.getIndexWord(synkeyword, true)).append(")</i>");
            }
            // show option pane
            int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("replaceSynonymsWithKeywordsMsg",msg.toString()), resourceMap.getString("replaceSynonymsWithKeywordsTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if the user wants to replace the keyword-synonyms by their index-word, do this now
            if (JOptionPane.YES_OPTION==option) {
                // create new list for return value
                ArrayList<String> newkeywords = new ArrayList<>();
                for (String keyword : keywords) {
                    // and replace all found synonyms-values by their related index-words
                    if ((synonymsObj.findSynonym(keyword, true) != -1) && !synonymsObj.isIndexWord(keyword, true)) {
                        // retrieve index-word
                        String iword = synonymsObj.getIndexWord(keyword, true);
                        // check whether replaced index-word already exists in keywords. by replacing a synonym with its
                        // index-word, we might produce duplicate keywords, in case the index-word already existed before.
                        // keywords[cnt] = iword;
                        if (!newkeywords.contains(iword)) {
                            newkeywords.add(iword);
                        }
                    } else {
                        if (!newkeywords.contains(keyword)) {
                            newkeywords.add(keyword);
                        }
                    }
                }
                // copy list to array
                keywords = newkeywords.toArray(new String[newkeywords.size()]);
            }
            else if (JOptionPane.CANCEL_OPTION==option || JOptionPane.CLOSED_OPTION==option /*User pressed cancel key*/) {
                return null;
            }
        }
        return keywords;
    }

    
    /**
     * This method sets the locale descriptions for the standard-actions cut, copy and paste -
     * which are in English by default.
     * @param actionMap the class's actionmap
     */
    public static void initLocaleForDefaultActions(javax.swing.ActionMap actionMap) {
        String[] actions = new String[] {"cut", "copy", "paste"};
        for (String ac : actions) {
            // get the action's name
            AbstractAction aac = (AbstractAction) actionMap.get(ac);
            // and put them together :-)
            aac.putValue(AbstractAction.NAME, toolbarResourceMap.getString(ac+".Action.text"));
            aac.putValue(AbstractAction.SHORT_DESCRIPTION, toolbarResourceMap.getString(ac+".Action.shortDescription"));
//            // get the new icon-path from the resource-map
//            URL imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource(resourceMap.getString(ac+".Action.largeIcon"));
//            // create icon
//            ImageIcon img = new ImageIcon(imgURL);
//            aac.putValue(AbstractAction.LARGE_ICON_KEY, img);
            aac.putValue(AbstractAction.SMALL_ICON, null);
        }
    }


    /**
     * This method retrieves a string-array of keywords and separates them at certain delimiter-chars
     * like comma, slash, minus, plus and space. Then, from all keyword-parts particular words are
     * removed, like "and", "the", "one" etc. The remaining parts of the keyword(s) are copied to a
     * string array and returned.
     *
     * @param keywords a string-array with keywords that should be separated
     * @param matchcase {@code true} if the case should not be changed, {@code false} if case should
     * be ignored and keyword-parts should be transformed to lower case.
     * @return a string array that contains the separated parts of the keywords, or null if no
     * keyword-parts have been found...
     */
    public static String[] retrieveSeparatedKeywords(String[] keywords, boolean matchcase) {
        // when we receive an empty array, return null
        if (null==keywords || 0==keywords.length) {
            return null;
        }
        // create new arraylist for retirn values
        List<String> separatedkws = new ArrayList<>();
        // first, add original keywords
        separatedkws.addAll(Arrays.asList(keywords));        
        // go through all found keywords
        for (String k : keywords) {
            // check, whether a keyword consists of more than one word, separated by , / - or +.
            // if so, split keyword at that char
            String[] sepkw = k.split("(,|/|-| |\\+)");
            // and add eacxh single part to the linked list
            for (String sp : sepkw) {
                // trim leading and trailing spaces
                sp = sp.trim();
                // if the user wants to ignore the case, transform string to lowercase
                if (!matchcase) {
                    sp = sp.toLowerCase();
                }
                // check whether the keyword-part has at least three chars and does
                // not equal typical words like "and", "or" etc...
                if (sp.length()>2 && (!sp.equalsIgnoreCase("und")
                                  && !sp.equalsIgnoreCase("der")
                                  && !sp.equalsIgnoreCase("die")
                                  && !sp.equalsIgnoreCase("das")
                                  && !sp.equalsIgnoreCase("des")
                                  && !sp.equalsIgnoreCase("den")
                                  && !sp.equalsIgnoreCase("dem")
                                  && !sp.equalsIgnoreCase("von")
                                  && !sp.equalsIgnoreCase("als")
                                  && !sp.equalsIgnoreCase("aus")
                                  && !sp.equalsIgnoreCase("auf")
                                  && !sp.equalsIgnoreCase("ein")
                                  && !sp.equalsIgnoreCase("eine")
                                  && !sp.equalsIgnoreCase("einer")
                                  && !sp.equalsIgnoreCase("eines")
                                  && !sp.equalsIgnoreCase("oder")
                                  && !sp.equalsIgnoreCase("the")
                                  && !sp.equalsIgnoreCase("why")
                                  && !sp.equalsIgnoreCase("one")
                                  && !sp.equalsIgnoreCase("and")
                    )) {
                    separatedkws.add(sp);
                }
            }
        }
        // return linked list as array...
        return (separatedkws.size()>0) ? separatedkws.toArray(new String[separatedkws.size()]) : null;
    }


    /**
     * This method "splits" a single <i>term</i> into its single word-parts. For example, a keyword
     * <i>Niklas, Luhmann - Zettelkasten</i> which is stored as a single keyword-entry, consists
     * of three parts (words). For creating the quick input keyword list (see CNewEntry), in some cases
     * we need the single parts of a keyword.
     *
     * @param settingsObj A reference to the CSettings-class
     * @param synonymsObj A reference to the CSynonyms-class
     * @param keyword the keyword, which should be split into its parts
     * @param matchcase {@code true} if the case should not be changed, {@code false} if case should
     * be ignored and keyword-parts should be transformed to lower case.
     * @return a string-array containing all splitted parts of the keywords, and spit-parts of the keyword-
     * related synonyms, if the keyword has any related synonyms.
     */
    public static String[] getKeywordsAndSynonymsParts(Settings settingsObj, Synonyms synonymsObj, String keyword, boolean matchcase) {
        // first, get the separated parts from the selected value
        String[] kwparts = retrieveSeparatedKeywords(new String[] {keyword}, matchcase);
        // create array with keywords, and - if necessary - related synonyms
        String[] synline = null;
        // retrieve synonyms if option is set
        if (settingsObj.getSearchAlwaysSynonyms()) {
            synline = retrieveSeparatedKeywords(synonymsObj.getSynonymLine(keyword, matchcase), matchcase);
        }
        // if we don't have any synonyms, put only keywords in the array
        if (null==synline) {
            // we here copy our separated keywords to our find-array
            synline = kwparts;
        }
        else {
            // here we now have findterms in our array "textparts" and "findterms".
            // we need to put these arrays together. We do this by creating an array list
            // and adding all elements of both array to that list. Then we copy the final
            // array-list back to our findterms-array
            List<String> separatedkws = new ArrayList<>();
            // copy all elements of textparts-array to the array-list
            for (String part1 : kwparts) {
                if (!separatedkws.contains(part1)) {
                    separatedkws.add(part1);
                }
            }
            // copy all elements of findterms-array to the array-list
            for (String part2 : synline) {
                if (!separatedkws.contains(part2)) {
                    separatedkws.add(part2);
                }
            }
            // copy array-list to string-array
            synline = separatedkws.toArray(new String[separatedkws.size()]);
        }
        return synline;
    }


    /**
     * This method checks the string {@code expression} for occurences of regular-expression
     * meta-characters, that usually have to be escaped.
     * @param expression the string that should be checked for occurences of reg-ex-meta-characters
     * @return {@code true} if {@code expression} contains reg-ex-meta-chars, {@code false} otherwise
     */
    public static boolean hasRegExChars(String expression) {
        // we assume having a regular expression only when we find certain meta-characters
        // to check for meta characters, we create an array with those chars and check whether
        // our searchterm "highlighterms" contains at least one of these chars...
        String[] allowedsigns = new String[] {"\\","+","{","}","(",")","[","]","$","*","?",".","|","<",">","^"};
        // init found-indicator
        boolean signfound = false;
        // iterate array
        for (String stdummy : allowedsigns) {
            if (expression.contains(stdummy)) {
                signfound = true;
            }
        }
        return signfound;
    }


    /**
     * This method converts all known markdown syntax into the application's "UBB"-format tags.
     *
     * @param dummy the entry content as string where Markdown should be converted to the common format tags
     * @return a string with "UBB"-format tags and no more Markdown syntax. Markdown has been replaced with
     * the common format tags of the Zettelkasten.
     */
    public static String convertMarkDown2UBB(String dummy) {
        dummy = dummy.replace("[br]", "\n");
        // quotes
        dummy = dummy.replaceAll("(^|\\n)(\\> )(.*)", "[q]$3[/q]");
        // bullets
        dummy = dummy.replaceAll("(^|\\n)(\\d\\. )(.*)", "[n][*]$3[/*][/n]");
        dummy = dummy.replace("[/n][n]", "");
        dummy = dummy.replaceAll("(^|\\n)(\\* )(.*)", "[l][*]$3[/*][/l]");
        dummy = dummy.replace("[/l][l]", "");
        // bold and italic formatting in markdown
        dummy = dummy.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "[f][k]$1[/k][/f]");
        dummy = dummy.replaceAll("___(.*?)___", "[f][k]$1[/k][/f]");
        // bold formatting
        dummy = dummy.replaceAll("__(.*?)__", "[f]$1[/f]");
        dummy = dummy.replaceAll("\\*\\*(.*?)\\*\\*", "[f]$1[/f]");
        // italic formatting
        dummy = dummy.replaceAll("_(.*?)_", "[k]$1[/k]");

        dummy = dummy.replaceAll("\\*(.*?)\\*", "[k]$1[/k]");
        // code blocks formatting
        dummy = dummy.replaceAll("\\`(.*?)\\`", "[code]$1[/code]");
        // headlines
        dummy = dummy.replaceAll("(^|\\n)#{4} (.*)", "[h4]$2[/h4]");
        dummy = dummy.replaceAll("(^|\\n)#{3} (.*)", "[h3]$2[/h3]");
        dummy = dummy.replaceAll("(^|\\n)#{2} (.*)", "[h2]$2[/h2]");
        dummy = dummy.replaceAll("(^|\\n)#{1} (.*)", "[h1]$2[/h1]");
        // strike
        dummy = dummy.replaceAll("---(.*?)---", "[d]$1[/d]");
        // images
        dummy = dummy.replaceAll("[!]{1}\\[([^\\[]+)\\]\\(([^\\)]+)\\)", "[img]$2[/img]");
        dummy = dummy.replace("\n","[br]");
        return dummy;
    }


    public static String convertUBB2MarkDown(String dummy) {
        // bold formatting: [f] becomes <b>
        dummy = dummy.replaceAll("\\[f\\](.*?)\\[/f\\]", "**$1**");
        // italic formatting: [k] becomes <i>
        dummy = dummy.replaceAll("\\[k\\](.*?)\\[/k\\]", "*$1*");
        // headline: [h4] becomes <h5>
        dummy = dummy.replaceAll("\\[h4\\](.*?)\\[/h4\\]", "#### $1");
        // headline: [h3] becomes <h4>
        dummy = dummy.replaceAll("\\[h3\\](.*?)\\[/h3\\]", "### $1");
        // headline: [h2] becomes <h3>
        dummy = dummy.replaceAll("\\[h2\\](.*?)\\[/h2\\]", "## $1");
        // headline: [h1] becomes <h2>
        dummy = dummy.replaceAll("\\[h1\\](.*?)\\[/h1\\]", "# $1");
        // cite formatting: [q] becomes <q>
        dummy = dummy.replaceAll("\\[q\\](.*?)\\[/q\\]", "> $1");
        // code formatting: [code] becomes `
        dummy = dummy.replaceAll("\\[code\\](.*?)\\[/code\\]", "`$1`");
        // strike-through formatting: [d] becomes <strike>
        dummy = dummy.replaceAll("\\[d\\](.*?)\\[/d\\]", "---$1---");
        /*
        try {
            // unordered list: [l] becomes <ul>
            Pattern p = Pattern.compile("\\[l\\](.*?)\\[/l\\]");
            // find all list-bullets inside
            Matcher m = p.matcher(dummy);
            while (m.find()) {
                String tmp = "[br]"+dummy.substring(m.start(), m.end());
                tmp = tmp.replace("[*]", "* [br]");
                dummy = dummy.substring(0, m.start())+tmp+dummy.substring(m.end());
            }
        }
        catch (IndexOutOfBoundsException ex) {

        }
        */
        dummy = dummy.replaceAll("\\[l\\](.*?)\\[/l\\]", "$1");
        // ordered list: [n] becomes <ol>
        dummy = dummy.replaceAll("\\[n\\](.*?)\\[/n\\]", "$1");
        // bullet points: [*] becomes <li>
        dummy = dummy.replaceAll("\\[\\*\\](.*?)\\[/\\*\\]", "* $1[br]");
        // image
        dummy = dummy.replaceAll("\\[img\\](.*?)\\[/img\\]", "![Bild]($1)");
        return dummy;
    }


    public static List<Integer> getImageResizeValues(String content) {
        // create image pattern
        Pattern p = Pattern.compile("\\[img\\]([^|]*)(.*?)\\[/img\\]");
        // and matacher
        Matcher m = p.matcher(content);
        // init return value. This array contains all resize values of imagaes, if we have any
        List<Integer> resizevalues = new ArrayList<>();
        // find image tags
        while (m.find()) {
            // get group counts. usually, we only have two groups:
            // 1. the file path to the image file
            // 2. the resize value
            int maxcount = m.groupCount();
            // get last group containing resize value
            // should start with "|", so remove it
            int reval;
            try {
                String resizeval = m.group(maxcount).substring(1);
                reval = Integer.parseInt(resizeval);
                resizevalues.add(reval);
            }
            catch (NumberFormatException | IndexOutOfBoundsException ex) {
            }
        }
        return resizevalues;
    }


    /**
     * This method removes all UBB-format-tag from an entry and returns a "cleaned" string
     * of that entry's content that does no longer contain any UBB-Format-tags.
     *
     * @param content the entry's content that should be cleaned from UBB-format-tags
     * @param includeMarkdown if {@code true}, Markdown syntax will also be removed. If {@code false},
     * Markdown syntax will remain in the string. This is needed when exporting content into Markdown
     * format and only not supported UBB tags should be removed, but not Markdown.
     * @return a cleaned string of that entry's content that does no longer contain any UBB-Format-tags
     */
    public static String removeUbbFromString(String content, boolean includeMarkdown) {
        String dummy = "";
        if (content!=null && !content.isEmpty()) {
            dummy = content.replaceAll("\\[k\\]", "")
                           .replaceAll("\\[f\\]", "")
                           .replaceAll("\\[u\\]", "")
                           .replaceAll("\\[h1\\]", "")
                           .replaceAll("\\[h2\\]", "")
                           .replaceAll("\\[h3\\]", "")
                           .replaceAll("\\[h4\\]", "")
                           .replaceAll("\\[q\\]", "")
                           .replaceAll("\\[d\\]", "")
                           .replaceAll("\\[c\\]", "")
                           .replaceAll("\\[code\\]", "")
                           .replaceAll("\\[sup\\]", "")
                           .replaceAll("\\[sub\\]", "")
                           .replaceAll("\\[/k\\]", "")
                           .replaceAll("\\[/f\\]", "")
                           .replaceAll("\\[/u\\]", "")
                           .replaceAll("\\[/h1\\]", "")
                           .replaceAll("\\[/h2\\]", "")
                           .replaceAll("\\[/h3\\]", "")
                           .replaceAll("\\[/h4\\]", "")
                           .replaceAll("\\[/q\\]", "")
                           .replaceAll("\\[/d\\]", "")
                           .replaceAll("\\[/c\\]", "")
                           .replaceAll("\\[/code\\]", "")
                           .replaceAll("\\[/sup\\]", "")
                           .replaceAll("\\[/sub\\]", "");
            // check whether only ubb or also markdown tags should be removed
            if (includeMarkdown) {
                // quotes
                dummy = dummy.replaceAll("(?<=\\[br\\])(\\> )(.*?)\\[br\\]", "$2[br]");
                // bold and italic formatting in markdown
                dummy = dummy.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "$1");
                dummy = dummy.replaceAll("___(.*?)___", "$1");
                // bold formatting
                dummy = dummy.replaceAll("__(.*?)__", "$1");
                dummy = dummy.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
                // italic formatting
                dummy = dummy.replaceAll("_(.*?)_", "$1");
                dummy = dummy.replaceAll("\\*(.*?)\\*", "$1");
                // headlines
                dummy = dummy.replaceAll("#{4} (.*?)\\[br\\]", "$1");
                dummy = dummy.replaceAll("#{3} (.*?)\\[br\\]", "$1");
                dummy = dummy.replaceAll("#{2} (.*?)\\[br\\]", "$1");
                dummy = dummy.replaceAll("#{1} (.*?)\\[br\\]", "$1");
                // strike
                dummy = dummy.replaceAll("---(.*?)---", "$1");
                // code
                dummy = dummy.replaceAll("\\`(.*?)\\`", "$1");
                // images
                dummy = dummy.replaceAll("[!]{1}\\[([^\\[]+)\\]\\(([^\\)]+)\\)", "");
                // hyperlinks
                dummy = dummy.replaceAll("\\[([^\\[]+)\\]\\(([^\\)]+)\\)","$1 ($2)");
            }
            dummy = dummy.replaceAll("\\[img\\](.*?)\\[/img\\]", "");
            dummy = dummy.replaceAll("\\[fn ([^\\[]*)\\]", "[FN $1]");
            dummy = dummy.replaceAll("\\[form ([^\\[]*)\\]", "$1");
            dummy = dummy.replaceAll("\\[color ([^\\[]*)\\](.*?)\\[/color\\]", "$2");
            dummy = dummy.replaceAll("\\[font ([^\\[]*)\\](.*?)\\[/font\\]", "$2");
            dummy = dummy.replaceAll("\\[h ([^\\[]*)\\](.*?)\\[/h\\]", "$2");
            dummy = dummy.replaceAll("\\[m ([^\\[]*)\\](.*?)\\[/m\\]", "$2");
            dummy = dummy.replaceAll("\\[l\\](.*?)\\[/l\\]", "$1");
            dummy = dummy.replaceAll("\\[\\*\\](.*?)\\[/\\*\\]", "- $1\n");
            dummy = dummy.replace("[br]", System.lineSeparator());
            // convert tables. we don't do this with regular expressions
            // first, init the index-variable
            int pos = 0;
            int end;
            // go and find all table-tages
            while (pos!=-1) {
                // find occurence of opening-tag
                pos = dummy.indexOf("[table]", pos);
                // when open-tag was found, go on and find end of table-tag
                if (pos!=-1) {
                    // find closing-tag
                    end = dummy.indexOf("[/table]", pos);
                    // if closing-tag also found, convert content to table
                    if (end!=-1) {
                        try {
                            // get table-content
                            String tablecontent = dummy.substring(pos+7, end).replaceAll("\\|", "\t").replaceAll("\\^", "\t");
                            dummy = dummy.substring(0, pos)+tablecontent+dummy.substring(end+8);
                            pos = 0;
                        }
                        catch (IndexOutOfBoundsException ex) {
                        }
                    }
                    // if no valid end-tag was found, try to find possible
                    // next table tage
                    else {
                        pos = pos+7;
                    }
                }
            }
        }

        return dummy;
    }


    /**
     * Checks a given keyCode-value, usually passed from the {@code KeyEvent-getKeyCode()}-method,
     * and checks whether it is a "navigation" key like arrows, page up/down, home etc.
     *
     * @param keyCode the keycode of the pressed or releases key
     * @return {@code true} if it is a navigation key, false otherwise.
     */
    public static boolean isNavigationKey(int keyCode) {
        return (KeyEvent.VK_HOME==keyCode ||
                KeyEvent.VK_END==keyCode ||
                KeyEvent.VK_UP==keyCode ||
                KeyEvent.VK_DOWN==keyCode ||
                KeyEvent.VK_LEFT==keyCode ||
                KeyEvent.VK_RIGHT==keyCode);
    }


    /**
     * This method copies selected text in plain format into the clipboard.
     *
     * @param dataObj a reference to the CDaten class. Needed to retrieve the enty's content
     * @param displayedZettel the currently displayed entry
     * @param editorPane the editor pane which is the copy source
     */
    public static void copyPlain(Daten dataObj, int displayedZettel, javax.swing.JEditorPane editorPane) {
        // create string builder that will contain complete plain entry
        StringBuilder plainEntry = new StringBuilder("");
        // retrieve entry's title
        String title = dataObj.getZettelTitle(displayedZettel);
        // check whether entry has any title
        if (title!=null && !title.isEmpty()) {
            // if yes, add title and line separator to string builder
            plainEntry.append(title);
            plainEntry.append(System.lineSeparator());
        }
        // retrieve plain entry that contains no ubb-tags and add it
        // to our string builder
        plainEntry.append(dataObj.getCleanZettelContent(displayedZettel));
        // get start and end of selection
        int selstart = editorPane.getSelectionStart()-1;
        int selend = editorPane.getSelectionEnd()-1;
        // fix value if necessary
        if (selstart<0) {
            selstart = 0;
        }
        // check whether end exceeds the string builders length - this is the case
        // e.g. if the user also selectes the time stamp...
        if (selend>=plainEntry.length()) {
            selend = plainEntry.length()-1;
        }
        // copy selection to string
        String selectedText = plainEntry.toString().substring(selstart, selend).trim();
        // if no text selected, quit
        if (selectedText.isEmpty()) {
            return;
        }
        // create new string-selection
        StringSelection stringSelection = new StringSelection(selectedText);
        // and copy string to clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection,null);
    }


    /**
     * This method returns the current date as string in the following format: yymmddhhmm
     * (e.g. <i>0811271548</i> for the 27th Nov. 2008, 15:48 (3:48pm). Used to create
     * an entry's timestamp.
     *
     * @return the current date as string, for use as timestamp.
     */
    public static String getTimeStamp() {
        // create new dateformat and format it in a simple way
        // so we just have the year (2 digits), the month and the day and the time.
        // e.g.: 0811271548 for the 27th Nov. 2008, 15:48 (3:48pm)
        DateFormat df = new SimpleDateFormat("yyMMddHHmm");
        return df.format(new Date());
    }


    /**
     * This method returns the current date as string in the following format: yymmddhhmmssms
     * (e.g. <i>081127154853157</i> for the 27th Nov. 2008, 15:48 (3:48pm) and 53 seconds and
     * 157 milliseconds. Used to create an entry's timestamp for the desktop-database.
     *
     * @return the current date as string, for use as timestamp.
     */
    public static String getTimeStampWithMilliseconds() {
        // create new dateformat and format it in a simple way
        // so we just have the year (2 digits), the month and the day and the time.
        // e.g.: 0811271548 for the 27th Nov. 2008, 15:48 (3:48pm)
        DateFormat df = new SimpleDateFormat("yyMMddHHmmssSSS");
        return df.format(new Date());
    }


    /**
     * This method creates a unique ID for each entry. This ID is returned as string
     * and stored in the XML-structure.
     *
     * @param filename
     * @return a unique ID for an entry as String value
     */
    public static String createZknID(String filename) {
        StringBuilder ts = new StringBuilder(getTimeStampWithMilliseconds());
        ts.append(filename);
        int randomnumber = (int)(Math.random()*99999);
        ts.append(String.valueOf(randomnumber));
        return ts.toString();
    }


    /**
     * This method accepts a single-line string {@code s} and returns it as line-separated
     * text, with each line having a maximum length of {@code len} chars.<br><br>
     * If the line wrapped text should have a prefix at the beginning of each line,
     * this can be passed as parameter {@code prefix}.
     *
     * @param s the single-lined string
     * @param len the length of each line from the returned string
     * @param prefix an optional string that is inserted infront of each line.
     * @return the string {@code s} with separated lines, with a line wrap after max. {@code len} chars.
     */
    public static String lineWrapText(String s, int len, String prefix) {
        // create tokenizer
        StringTokenizer st = new StringTokenizer(s, " ", true);
        // dummy string
        String word;
        // string builder for return value
        StringBuilder sb = new StringBuilder((prefix!=null&&!prefix.isEmpty())?prefix+" ":"");
        // line length counter
        int currentLineLen = 0;
        // iterate string
        while (st.hasMoreTokens()) {
            // check word-length
            int wordLen = (word = st.nextToken()).length();
            // if we haven't reached the line end...
            if (currentLineLen + wordLen <= len) {
                // ...append word
                sb.append(word);
                // and increase line length counter
                currentLineLen += wordLen;
            } else {
                // else check whether first char in new line is a space char
                boolean firstIsSpace = word.charAt(0) == ' ';
                // append new line
                sb.append(System.lineSeparator());
                // append prefix, if we have any
                sb.append((prefix!=null&&!prefix.isEmpty())?prefix+" ":"");
                // append word resp. nothing, if first char is space
                sb.append((firstIsSpace ? "" : word));
                // reset line length counter
                currentLineLen = firstIsSpace ? 0 : wordLen;
            }
        }
        // return result
        return sb.toString();
    }


    /**
     * This method converts separator chars of attachment- or image-paths into
     * the correct os-separator-char. This is needed, when the user uses the
     * program and data files both on windows or linux, for instance.
     *
     * @param csc the path to the attachment or image, as string.
     * @param settings a reference to the {@code CSettings} class.
     * @return the string {@code csc} with converted seperator chars, so the string
     * is usable for the current OS.
     */
    public static String convertSeparatorChars(String csc, Settings settings) {
        // check for valid parameter
        if (null==csc || csc.isEmpty()) {
            return "";
        }
        String retval = csc;
        // check whether attachment is a hyperlink or not. if it is *no* hyperlink,
        // we can convert the separator-chars - else we don't change them.
        if (!FileOperationsUtil.isHyperlink(csc)) {
            // check for os, and replace / with \ or \ with / (file separator-chars).
            // we do this since users can use their data on e.g. windows and linux, and therefor,
            // the separator-chars of images have to be switched.
            retval = (PlatformUtil.isWindows()) ? csc.replace("/","\\") : csc.replace("\\","/");
        }
        return retval;
    }


    /**
     * This method either merges two synonym-lines from those synonyms that are associated with
     * the keywords {@code oldKw} and {@code newKw}. Or, if no synonyms are associated with the
     * new keyword {@code newKw}, the index-word from the old synonyms-line, which should equal the
     * value {@code oldKw}, is renamed to the new index-word {@code newKw}.
     *
     * @param synonymsObj
     * @param oldKw
     * @param newKw
     */
    public static void mergeSynonyms(Synonyms synonymsObj, String oldKw, String newKw) {
        // check out, whether old and new keywords already exist as synonyms (index-words)
        int oldsynpos = synonymsObj.getSynonymPosition(oldKw);
        int newsynpos = synonymsObj.getSynonymPosition(newKw);
        // only proceed, if old keyword was an index-synonym...
        if (oldsynpos!=-1) {
            // either the new keyword does not exist as synonym, than simply
            // ask to rename the old index-word into the new keyword
            if (-1==newsynpos) {
                // create a JOptionPane with yes/no/cancel options
                int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("renameIndexSynonymMsg"), resourceMap.getString("renameIndexSynonymTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                // proceed, if user wants to rename
                if (JOptionPane.YES_OPTION == option) {
                    // rename index-word
                    synonymsObj.setIndexWord(oldsynpos, newKw);
                    // now ask the user whether he wants to keep the old keyword-name as additional synonym
                    // create a JOptionPane with yes/no options
                    option = JOptionPane.showConfirmDialog(null, resourceMap.getString("appendOldIndexSynonymMsg",newKw,oldKw,newKw), resourceMap.getString("appendOldIndexSynonymTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    // proceed, if user wants to append
                    if (JOptionPane.YES_OPTION == option) {
                        // append the former synonym, which was renamed, as additional synonym
                        // to the new synonym-line with the new index-word
                        synonymsObj.appendSingleSynonym(oldsynpos, oldKw);
                    }
                }
            }
            // the new keyword is also a synonym... now offer to merge both the old
            // and the new synonyms-line
            else if (oldsynpos!=newsynpos) {
                // create a JOptionPane with yes/no/cancel options
                int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("mergeSynonymsMsg"), resourceMap.getString("mergeSynonymsTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                // proceed, if user wants to rename
                if (JOptionPane.YES_OPTION == option) {
                    // rename index-word
                    if (!synonymsObj.mergeSynonymLines(newsynpos, oldsynpos)) {
                        // display error message box
                        JOptionPane.showMessageDialog(null,resourceMap.getString("errorMergeSynonymsMsg"),resourceMap.getString("errorMergeSynonymsTitle"),JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        }
    }


    /**
     *
     */
    public static void flushSessionLog() {
        Handler[] handlers = Constants.zknlogger.getHandlers();
        for (Handler h : handlers) {
            if (h.toString().contains("StreamHandler")) {
                h.flush();
            }
        }
    }


    /**
     * This method checks asciil-chars which were tread by an inputstream whether they are valid xml-chars
     * or not.
     *
     * @param zeichen the ascii-char we want to check
     * @return {@code true} if it is a valid char, {@code false} otherwise
     */
    public static boolean isLegalJDOMChar(int zeichen) {
        // every character beginning from 0x20 is a legal xml-char
        // below 0x20 (space-char), the tab-char (0x09), carriage return (0x0A) and new line (0x0D) are valid onky
        return zeichen>=32 || 9==zeichen || 10==zeichen || 13==zeichen;
    }


    /**
     * This method checks whether a string contains legal JDOM chars, so the string can be added
     * as entry-content to the XML-data file. Every non-legal-JDOM-char is replaced by a space-char.
     * @param content the content-string that should be checked for valid JDOM-chars. typically use the input
     * from a user made in the CNewEntry-dialog.
     * @return a "cleaned" string without any illegal JDOM-chars. Illegal chars are replaced by space-chars
     */
    public static String isValidJDOMChars(String content) {
        // copy content to char-array
        char[] contentchars = content.toCharArray();
        // create return value
        StringBuilder retval = new StringBuilder("");
        // iterate char-array and check for valid JDOM chars
        for (char c : contentchars) {
            // is char legal JDOM-char? then append it to string builder else append space char
            retval.append((isLegalJDOMChar(c))?c:" ");
        }
        // return cleaned string
        return retval.toString();
    }
    /**
     * This method extracts all occurences of possible form-tags (Laws of Form)
     * from an entry-string, which content is stored in {@code content}. The found
     * form-tags will be returned as an array list of strings.
     *
     * @param content a string with zettel content, which may contain form-tags
     * @return an array list with all form-tags of that entry as strings, or {@code null}
     * if no form tag was found.
     */
    public static ArrayList<String> getFormsFromString(String content) {
        // check for valid param
        if (null==content || content.isEmpty()) {
            return null;
        }
        // create new array list
        ArrayList<String> forms = new ArrayList<>();
        // find forms. we don't do this with regular expressions
        // first, init the index-variable
        int pos = 0;
        int end;
        // go and find all form-tages
        while (pos!=-1) {
            // find occurence of opening-tag
            pos = content.indexOf(Constants.FORMAT_FORM_TAG, pos);
            // when open-tag was found, go on and find end of table-tag
            if (pos!=-1) {
                // find closing-tag
                end = content.indexOf("]", pos);
                // if closing-tag also found, add form to arraylist
                if (end!=-1) {
                    try {
                        forms.add(content.substring(pos, end+1));
                    }
                    catch (IndexOutOfBoundsException ex) {
                    }
                }
                pos = end;
            }
        }
        return forms;
    }


    /**
     * This method returns the XML database as string. just for testing purposes.
     * @param dataObj
     * @return
     */
    public static String retrieveXMLFileAsString(Daten dataObj) {
        // create a new XML-outputter with the pretty output format,
        // so the xml-file looks nicer
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        // return XML data base as string
        return out.outputString(dataObj.getZknData());
    }

    
    public static boolean isPandocExportType(int exportType) {
        // check if export to pandoc format is requested
        return (Constants.EXP_TYPE_DESKTOP_DOCX==exportType ||
                Constants.EXP_TYPE_DESKTOP_ODT==exportType ||
                Constants.EXP_TYPE_DESKTOP_EPUB==exportType ||
                Constants.EXP_TYPE_DESKTOP_RTF==exportType ||
                Constants.EXP_TYPE_DOCX==exportType ||
                Constants.EXP_TYPE_ODT==exportType ||
                Constants.EXP_TYPE_EPUB==exportType ||
                Constants.EXP_TYPE_RTF==exportType);
    }
    

    public static boolean isPandocMissing(Settings settings, int exportType) {
        // start pandoc for conversion
        Runtime rt = Runtime.getRuntime();
        Process pr = null;
        boolean pandocmissing = false;
        // check if export to pandoc format is requested
        if (isPandocExportType(exportType)) {
            try {
                pr = rt.exec(settings.getPandocPath()+" --version");
                pr.waitFor();
            } catch (IOException | InterruptedException ex) {
                Constants.zknlogger.log(Level.WARNING,"Could not find Pandoc under specified path {0}.", settings.getPandocPath());
                pandocmissing = true;
            }
            // destroy process
            if (pr!=null) {
                Constants.zknlogger.log(Level.INFO,"Process exit code: {0}", String.valueOf(pr.exitValue()));
                pr.destroy();
            }
            // check whether pandoc is available
            if (pandocmissing) {
                // if not, show error message and leave
                JOptionPane.showMessageDialog(null,resourceMap.getString("noPandocInstalledMsg"),resourceMap.getString("noPandocInstalledTitle"),JOptionPane.PLAIN_MESSAGE);
            }
        }
        return pandocmissing;
    }


    public static LinkedList extractFootnotesFromContent(String content) {
        // now prepare a reference list from possible footnotes
        LinkedList<String> footnotes = new LinkedList<>();
        // position index for finding the footnotes
        int pos = 0;
        // do search as long as pos is not -1 (not-found)
        while (pos!=-1) {
            // find the html-tag for the footnote
            pos = content.indexOf(Constants.footnoteHtmlTag, pos);
            // if we found something...
            if (pos!=-1) {
                // find the closing quotes
                int end = content.indexOf("\"", pos+Constants.footnoteHtmlTag.length());
                // if we found that as well...
                if (end!=-1) {
                    // extract footnote-number
                    String fn = content.substring(pos+Constants.footnoteHtmlTag.length(), end);
                    // and add it to the linked list, if it doesn't already exist
                    if (-1==footnotes.indexOf(fn)) footnotes.add(fn);
                    // set pos to new position
                    pos = end;
                }
                else pos = pos+Constants.footnoteHtmlTag.length();
            }
        }
        return footnotes;
    }

    public static AbstractButton makeTexturedToolBarButton(AbstractButton button, String segmentPosition) {
        if (null==segmentPosition || segmentPosition.isEmpty() || segmentPosition.equals(SEGMENT_POSITION_ONLY)) {
            button.putClientProperty("JButton.buttonType","textured");
        }
        else {
            button.putClientProperty("JButton.buttonType","segmentedTextured");
            button.putClientProperty("JButton.segmentPosition",segmentPosition);
        }
        button.setText(null);
        button.setBorderPainted(true);
        button.setPreferredSize(Constants.seaGlassButtonDimension);
        return button;
    }
}
