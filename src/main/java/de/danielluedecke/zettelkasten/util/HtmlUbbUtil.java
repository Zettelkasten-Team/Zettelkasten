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
package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.tasks.export.ExportTools;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.jdom2.Element;

/**
 * This class is responsible for the creation of a HTML page of an Zettelkasten
 * entry which is then displayed in the main window's JEditorPane.
 *
 * @author danielludecke
 */
public class HtmlUbbUtil {

    /**
     * get the strings for file descriptions from the resource map
     */
    private static final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(HtmlUbbUtil.class);

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    private static String[] highlightTermsSearch = null;
    private static String[] highlightTermsKeywords = null;
    private static String[] highlightTermsLivesearch = null;

    public static final int HIGHLIGHT_STYLE_SEARCHRESULTS = 1;
    public static final int HIGHLIGHT_STYLE_KEYWORDS = 2;
    public static final int HIGHLIGHT_STYLE_LIVESEARCH = 3;

    private static boolean highlightWholeWord;

    private static final int RATING_VALUE_NONE = 1;
    private static final int RATING_VALUE_HALF = 2;
    private static final int RATING_VALUE_FULL = 3;

    /**
     * This method creates the image-tag for a rating-point
     *
     * @param ratingvalue the ratingvalue, i.e. whether the image-tag should
     * contain a full, half or no value-point-image. use following
     * constants:<br>
     * <ul>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_FULL}</li>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_HALF}</li>
     * <li>{@link #RATING_VALUE_FULL RATING_VALUE_NONE}</li>
     * </ul>
     * @return the HTML-formatted image-tag with the requested rating-symbol
     */
    private static String getRatingSymbol(int ratingvalue) {
        // create value for img-name
        URL imgURL = null;
        // init return value, i.e. the image-tag
        StringBuilder imgtag = new StringBuilder("<img border=\"0\" src=\"");
        // check which image to choose
        switch (ratingvalue) {
            // no rating point
            case RATING_VALUE_NONE:
                imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_black.png");
                break;
            // half rating point
            case RATING_VALUE_HALF:
                imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_yellow.png");
                break;
            // full rating point
            case RATING_VALUE_FULL:
                imgURL = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/bullet_green.png");
                break;
        }
        // append image-src
        imgtag.append(imgURL);
        // and close tag
        imgtag.append("\">");
        // return result
        return imgtag.toString();
    }

    /**
     * This method creates and returns a string which contains the
     * CSS-style-definition for highlighting text / words / keywords /
     * live-search results in a jEditorPane.
     *
     * @param settingsObj a reference to the CSettings-class
     * @return a string which contains the CSS-style-definition for highlighting
     * keywords
     */
    private static String getHighlightCSS(Settings settingsObj) {
        StringBuilder highlightCss = new StringBuilder("");
        highlightCss.append(getHighlightCSS(settingsObj, HIGHLIGHT_STYLE_KEYWORDS));
        highlightCss.append(getHighlightCSS(settingsObj, HIGHLIGHT_STYLE_SEARCHRESULTS));
        highlightCss.append(getHighlightCSS(settingsObj, HIGHLIGHT_STYLE_LIVESEARCH));
        return highlightCss.toString();
    }

    /**
     * This method creates and returns a string which contains the
     * CSS-style-definition for highlighting text / words / keywords /
     * live-search results in a jEditorPane.
     *
     * @param settingsObj a reference to the CSettings-class
     * @param style
     * @return a string which contains the CSS-style-definition for highlighting
     * keywords
     */
    private static String getHighlightCSS(Settings settingsObj, int style) {
        // prepare antoher string builder for this css-style, because we need
        // it twice... see author-css below
        StringBuilder highlightsb = new StringBuilder("");
        String css;
        // select css-style
        switch (style) {
            case HIGHLIGHT_STYLE_KEYWORDS:
                css = "kw";
                break;
            case HIGHLIGHT_STYLE_SEARCHRESULTS:
                css = "sr";
                break;
            case HIGHLIGHT_STYLE_LIVESEARCH:
                css = "ls";
                break;
            default:
                css = "kw";
                break;
        }
        // prepare the style for highlighting the search terms in
        // the search results window
        highlightsb.append(".hs_").append(css).append("{");
        // when the user wants to have a background-color, show this now...
        if (settingsObj.getShowHighlightBackground(style)) {
            highlightsb.append("background-color:#").append(settingsObj.getHighlightBackgroundColor(style)).append(";");
        }
        highlightsb.append("color:#");
        highlightsb.append(settingsObj.getHighlightSearchStyle(Settings.FONTCOLOR, style));
        highlightsb.append(";font-size:1.");
        highlightsb.append(settingsObj.getHighlightSearchStyle(Settings.FONTSIZE, style));
        highlightsb.append("em;");
        // get the fontweight-setting
        String fw = settingsObj.getHighlightSearchStyle(Settings.FONTWEIGHT, style);
        // apply font-weight-setting only if it's not the default-value, otherwise
        // bold text would be displayed in normal-fontweight instead.
        if (!fw.equalsIgnoreCase("normal")) {
            highlightsb.append("font-weight:");
            highlightsb.append(fw);
            highlightsb.append(";");
        }
        // get the fontstyle-setting
        String fs = settingsObj.getHighlightSearchStyle(Settings.FONTSTYLE, style);
        // apply font-style-setting only if it's not the default-value, otherwise
        // italic text would be displayed in non-italic instead.
        if (!fs.equalsIgnoreCase("normal")) {
            highlightsb.append("font-style:");
            highlightsb.append(fs);
        }
        highlightsb.append("}").append(System.lineSeparator());
        return highlightsb.toString();
    }

    /**
     * This method creates an HTML-layer ({@code div}-tag) which contains the
     * graphical elements for the rating of an entry. This method returns an
     * HTML-snippet as a string which can be inserted anywhere inside a
     * {@code body}-element.
     *
     * @param data a reference to the {@code CDaten}-class, needed for
     * accessing the methods that retrieve the rating-values from entries.
     * @param entryNr the entry-number of the requested entry which rating
     * should be created.
     * @param sourceFrame a reference to the frame from where this function call
     * came. needed for the html-formatting, since entries are differently
     * formatted in the search window.
     * @return a HTML-snippet as string which can be inserted anywhere inside a
     * {@code body}-element.
     */
    private static String getEntryHeadline(Daten data, int entryNr, int sourceFrame) {
        float ratingValue = data.getZettelRating(entryNr);
        StringBuilder htmlRating = new StringBuilder();
        int wordCount = calculateWordCount(data, entryNr);

        htmlRating.append(System.lineSeparator()).append("<div class=\"entryrating\">")
                  .append("<table ").append(getTableAttributes()).append("class=\"tabentryrating\"><tr>")
                  .append(System.lineSeparator())
                  .append("<td colspan=\"2\" class=\"leftcellentryrating\">")
                  .append(getEntryHeading(data, entryNr, sourceFrame, wordCount))
                  .append("</td><td class=\"midcellentryrating\">")
                  .append(getEntryTimestamp(data, entryNr))
                  .append("</td><td class=\"rightcellentryrating\">")
                  .append(getRatingHtml(entryNr, ratingValue))
                  .append("</td></tr>").append(System.lineSeparator())
                  .append(getManualLinksHtml(data, entryNr))
                  .append("</table></div>").append(System.lineSeparator());

        return htmlRating.toString();
    }

    private static String getTableAttributes() {
        if (PlatformUtil.isJava7OnMac() || PlatformUtil.isJava7OnWindows()) {
            return "cellspacing=\"0\" ";
        }
        return "";
    }

    private static String getEntryHeading(Daten data, int entryNr, int sourceFrame, int wordCount) {
        StringBuilder heading = new StringBuilder();
        heading.append(resourceMap.getString("zettelDesc")).append(" ");

        // Store the result of getActivatedEntryNumber to avoid multiple calls
        int activatedEntryNumber = data.getActivatedEntryNumber();
        Constants.zknlogger.info("Activated entry number: " + activatedEntryNumber);

        if (entryNr != activatedEntryNumber && sourceFrame != Constants.FRAME_SEARCH) {
            heading.append("<a class=\"elink\" href=\"#activatedEntry\">")
                    .append(" ").append(activatedEntryNumber).append("&nbsp;</a>&raquo;&nbsp;")
                    .append("<a class=\"elink\" href=\"#cr_").append(entryNr).append("\">")
                    .append(entryNr).append("&nbsp;</a>(").append(wordCount).append(" ")
                    .append(resourceMap.getString("activatedZettelWordCount")).append(")");
        } else {
            heading.append(entryNr).append(" (").append(wordCount).append(" ")
                    .append(resourceMap.getString("activatedZettelWordCount")).append(")");
        }

        return heading.toString();
    }

    private static int calculateWordCount(Daten dataObj, int entrynr) {
        String wordCountString = dataObj.getZettelTitle(entrynr) + " " + dataObj.getCleanZettelContent(entrynr);
        String[] words = wordCountString.toLowerCase()
                                       .replace("ä", "ae")
                                       .replace("ö", "oe")
                                       .replace("ü", "ue")
                                       .replace("ß", "ss")
                                       .split("\\W+");
        return (int) Arrays.stream(words).filter(word -> word.trim().length() > 1).count();
    }

    private static String getRatingHtml(int entrynr, float ratingValue) {
        StringBuilder ratingHtml = new StringBuilder();
        ratingHtml.append("<a class=\"rlink\" href=\"#rateentry").append(entrynr).append("\">")
                  .append(resourceMap.getString("ratingText")).append(": ");

        for (int cnt = 5; cnt > 0; cnt--) {
            if (ratingValue >= 1.0) {
                ratingHtml.append(getRatingSymbol(RATING_VALUE_FULL));
            } else if (ratingValue >= 0.5) {
                ratingHtml.append(getRatingSymbol(RATING_VALUE_HALF));
            } else {
                ratingHtml.append(getRatingSymbol(RATING_VALUE_NONE));
            }
            ratingValue--;
        }

        ratingHtml.append("</a>");
        return ratingHtml.toString();
    }

    private static String getManualLinksHtml(Daten dataObj, int entrynr) {
        String[] manualLinks = Daten.getManualLinksAsString(entrynr);
        if (manualLinks == null || manualLinks.length == 0) {
            return "";
        }

        StringBuilder linksHtml = new StringBuilder();
        linksHtml.append("<tr><td class=\"crtitle\" valign=\"top\"><a href=\"#crt\">")
                 .append(resourceMap.getString("crossRefText")).append(":</a>&nbsp;</td><td class=\"mlink\" colspan=\"3\">");

        for (String ml : manualLinks) {
            String title = "";
            try {
                title = dataObj.getZettelTitle(Integer.parseInt(ml));
                title = title.replace("\"", "").replace("'", "").trim();
            } catch (NumberFormatException e) {
                // handle exception
            }
            linksHtml.append("<a href=\"#cr_").append(ml).append("\" title=\"").append(title).append("\" alt=\"").append(title).append("\">")
                     .append(ml).append("</a> &middot; ");
        }

        linksHtml.setLength(linksHtml.length() - " &middot; ".length());
        linksHtml.append("</td></tr>").append(System.lineSeparator());

        return linksHtml.toString();
    }

    /**
     *
     * @param dataObj
     * @param entrynr
     * @return
     */
    private static String getEntryAuthors(Daten dataObj, int entrynr, String content, int sourceframe) {
        StringBuilder retval = new StringBuilder("");
        // ***********************************************
        // get authors
        // ***********************************************
        String[] authors = dataObj.getAuthors(entrynr);
        // separate authors from content
        retval.append("<h1>").append(resourceMap.getString("authorsText")).append("</h1>").append(System.lineSeparator());
        // check if we have any values
        if (authors != null && authors.length > 0) {
            // copy all authors to linked list, so we can remove those authors that have been added
            // as footnotes
            LinkedList<String> remainingAuthors = new LinkedList<>();
            remainingAuthors.addAll(Arrays.asList(authors));
            // extract footnotes.
            LinkedList<String> footnotes = Tools.extractFootnotesFromContent(content);
            // now we have all footnotes, i.e. the author-index-numbers, in the linked
            // list. now we can create a reference list
            if (footnotes.size() > 0) {
                // iterator for the linked list
                Iterator<String> i = footnotes.iterator();
                while (i.hasNext()) {
                    String au = i.next();
                    try {
                        int aunr = Integer.parseInt(au);
                        retval.append("<p class=\"reflist\"><b>[<a name=\"fn_").append(au).append("\">").append(au).append("</a>]</b> ");
                        String aus = dataObj.getAuthor(aunr);
                        // remove author from remaining list
                        remainingAuthors.remove(aus);
                        // if parameters in the string array highlight-terms have been passed, we assume that
                        // these terms should be highlighted...
                        if (Constants.FRAME_SEARCH == sourceframe) {
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_SEARCHRESULTS);
                        } else {
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_LIVESEARCH);
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_KEYWORDS);
                        }
                        // autoconvert url's to hyperlinks
                        // aus = convertHyperlinks(aus.replace("<", "&lt;").replace(">", "&gt;"));
                        aus = convertHyperlinks(aus);
                        retval.append(aus);
                        retval.append("</p>").append(System.lineSeparator());
                    } catch (NumberFormatException e) {
                        Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                    }
                }
                // check whether we have any remaining authors that did not appear in footnotes
                if (remainingAuthors.size() > 0) {
                    // iterator for the linked list
                    Iterator<String> ri = remainingAuthors.iterator();
                    while (ri.hasNext()) {
                        String aus = ri.next();
                        retval.append("<p class=\"reflist\">");
                        // if parameters in the string array highlight-terms have been passed, we assume that
                        // these terms should be highlighted...
                        if (Constants.FRAME_SEARCH == sourceframe) {
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_SEARCHRESULTS);
                        } else {
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_LIVESEARCH);
                            aus = highlightSearchTerms(aus, HIGHLIGHT_STYLE_KEYWORDS);
                        }
                        // autoconvert url's to hyperlinks
                        // aus = convertHyperlinks(aus.replace("<", "&lt;").replace(">", "&gt;"));
                        aus = convertHyperlinks(aus);
                        retval.append(aus);
                        retval.append("</p>").append(System.lineSeparator());
                    }
                }
            } else {
                for (String au : authors) {
                    // if parameters in the string array highlight-terms have been passed, we assume that
                    // these terms should be highlighted...
                    if (Constants.FRAME_SEARCH == sourceframe) {
                        au = highlightSearchTerms(au, HIGHLIGHT_STYLE_SEARCHRESULTS);
                    } else {
                        au = highlightSearchTerms(au, HIGHLIGHT_STYLE_LIVESEARCH);
                        au = highlightSearchTerms(au, HIGHLIGHT_STYLE_KEYWORDS);
                    }
                    // autoconvert url's to hyperlinks
                    au = convertHyperlinks(au);
                    retval.append("<p class=\"reflist\">").append(au).append("</p>").append(System.lineSeparator());
                }
            }
        } else {
            retval.append("<p class=\"reflist\"><i>").append(resourceMap.getString("NoAuthor")).append("</i></p>");
        }
        retval.append("<p></p>");
        return retval.toString();
    }

    /**
     *
     * @param dataObj
     * @param settings
     * @param entrynr
     * @return
     */
    private static String getEntryRemarks(Daten dataObj, Settings settings, int entrynr, int sourceframe) {
        StringBuilder retval = new StringBuilder("");
        String remarks = dataObj.getRemarks(entrynr);
        // if we have remarks, replace all return-ubb-tags into html-tags
        if (!remarks.isEmpty()) {
            // now copy the content of the entry to a dummy string. here we convert
            // the format codes into html-tags. the format codes are simplified tags
            // for the user to enable simple format editing
            boolean markdownActivated = settings != null && Boolean.TRUE.equals(settings.getMarkdownActivated());
            remarks = replaceUbbToHtml(remarks, markdownActivated, false, false, false);
            // autoconvert url's to hyperlinks
            remarks = convertHyperlinks(remarks);
            // if parameters in the string array highlight-terms have been passed, we assume that
            // these terms should be highlighted...
            if (Constants.FRAME_SEARCH == sourceframe) {
                remarks = highlightSearchTerms(remarks, HIGHLIGHT_STYLE_SEARCHRESULTS);
            } else {
                remarks = highlightSearchTerms(remarks, HIGHLIGHT_STYLE_LIVESEARCH);
                remarks = highlightSearchTerms(remarks, HIGHLIGHT_STYLE_KEYWORDS);
            }
            // after the conversion is done, append the content to the resulting return string
            retval.append("<h1>").append(resourceMap.getString("remarksText")).append("</h1>").append(System.lineSeparator());
            retval.append("<div class=\"remarks\">").append(remarks).append("</div><p></p>").append(System.lineSeparator());
        }
        return retval.toString();
    }

    /**
     *
     * @param dataObj
     * @param entrynr
     * @return
     */
    private static String getEntryAttachments(Daten dataObj, int entrynr, int sourceframe) {
        StringBuilder retval = new StringBuilder("");
        String[] attachments = dataObj.getAttachmentsAsString(entrynr, false);
        if ((attachments != null) && (attachments.length > 0)) {
            // we need a temporary buffer again
            StringBuilder dummylink = new StringBuilder("");
            // iterare all attachments
            for (String att : attachments) {
                // if it's not an empty element, surround it with html-list-tags
                if (!att.isEmpty()) {
                    dummylink.append("<li><a href=\"").append(att).append("\">");
                    // if parameters in the string array highlight-terms have been passed, we assume that
                    // these terms should be highlighted...
                    if (Constants.FRAME_SEARCH == sourceframe) {
                        att = highlightSearchTerms(att, HIGHLIGHT_STYLE_SEARCHRESULTS);
                    } else {
                        att = highlightSearchTerms(att, HIGHLIGHT_STYLE_LIVESEARCH);
                        att = highlightSearchTerms(att, HIGHLIGHT_STYLE_KEYWORDS);
                    }
                    dummylink.append(att);
                    dummylink.append("</a></li>").append(System.lineSeparator());
                }
            }
            // if there have been attachments, they must be stored in the stringbuffer now
            // so, if the string buffer has content, append it to the resulting return string
            if (dummylink.length() > 0) {
                // apply a class attribute to the attachments, so we can have certain styles
                // and formatting for the links as well
                retval.append("<div class=\"attachments\"><h1>").append(resourceMap.getString("HyperlinksHeader")).append("</h1>").append(System.lineSeparator()).append("<ul>");
                retval.append(dummylink.toString());
                retval.append("</ul></div><p></p>").append(System.lineSeparator());
            }
        }
        return retval.toString();
    }

    /**
     *
     * @param dataObj
     * @param entrynr
     * @return
     */
    private static String getEntryTimestamp(Daten dataObj, int entrynr) {
        StringBuilder retval = new StringBuilder("");
        String created = dataObj.getTimestampCreated(entrynr);
        String edited = dataObj.getTimestampEdited(entrynr);
        // check whether we have a timestamp at all
        if ((created != null) && (!created.isEmpty())) {
            // and add the created-timestamp
            retval.append("<a class=\"tslink\" href=\"#tstampc\">").append(resourceMap.getString("timestampCreated")).append(" ").append(Tools.getProperDate(created, true)).append("</a>");
            // check whether we have a modified-timestamp
            // if we have a modified-stamp, add it...
            if ((edited != null) && (!edited.isEmpty())) {
                retval.append("&nbsp;&middot;&nbsp").append("<a class=\"tslink\" href=\"#tstampe\">").append(resourceMap.getString("timestampEdited")).append(" ").append(Tools.getProperDate(edited, true)).append("</a>");
            }
            // and close the tags of the html-part
            retval.append(System.lineSeparator());
        }
        return retval.toString();
    }

    /**
     * This method creates a html page of the parameters passed to this class
     * constructor It is easier to keep the overview over the layout style when
     * the html page, which is responsible for the "look'n'feel" of an entry, is
     * being created in a separate class rather than in the Daten class
     *
     * @param settings a reference to the Settings-class
     * @param data a reference to the Daten-class
     * @param bibtexObj
     * @param entryNr the entry-number of the entry that should be converted
     * into HTML
     * @param segmentKeywords the keywords of the entry, separated at each word,
     * used for highlighting keywords in the content.
     * @param sourceFrame a reference to the frame from where this function call
     * came. needed for the html-formatting, since entries are differently
     * formatted in the search window.
     * @return a string with the html-page-content
     */
    public static String getEntryAsHTML(Settings settings,
            Daten data,
            BibTeX bibtexObj,
            int entryNr,
            String[] segmentKeywords,
            int sourceFrame) {
        return getEntryAsHTML(settings, data, bibtexObj, entryNr, segmentKeywords, sourceFrame, false, false);
    }

    public static String getEntryAsHTMLSanitized(Settings settings,
            Daten data,
            BibTeX bibtexObj,
            int entryNr,
            String[] segmentKeywords,
            int sourceFrame) {
        return getEntryAsHTML(settings, data, bibtexObj, entryNr, segmentKeywords, sourceFrame, true, true);
    }

    private static String getEntryAsHTML(Settings settings,
            Daten data,
            BibTeX bibtexObj,
            int entryNr,
            String[] segmentKeywords,
            int sourceFrame,
            boolean applyNormalization,
            boolean sanitizeRaw) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's "entry textfield"
        StringBuilder retval = new StringBuilder("");
        // ***********************************************
        // initiate html-page, header
        // ***********************************************
        retval.append("<html><head>").append(System.lineSeparator()); // NEW!
        // first of all, prepare the header and style information of the main content
        retval.append("<style>").append(System.lineSeparator());
        // get the common style definition for the basic-tags
        retval.append(getCommonStyleDefinition(settings, segmentKeywords, data.getKeywords(entryNr), false, false));
        // close style definition
        retval.append("</style>").append(System.lineSeparator());
        // close header and open body
        retval.append("</head><body>").append(System.lineSeparator()); // NEW!
        // ***********************************************
        // init headline
        // ***********************************************
        if (settings.getShowEntryHeadline()) {
            retval.append(getEntryHeadline(data, entryNr, sourceFrame));
        }
        // now start with the HTMLªª content itself
        retval.append("<div class=\"content\">");
        // ***********************************************
        // get entry's title
        // ***********************************************
        String title = data.getZettelTitle(entryNr).replace("<", "&lt;").replace(">", "&gt;");
        // if the entry has a title, add it surrounded by <h1>-tags
        if (!title.isEmpty()) {
            // if parameters in the string array highlight-terms have been passed, we assume that
            // these terms should be highlighted...
            if (Constants.FRAME_SEARCH == sourceFrame) {
                title = highlightSearchTerms(title, HIGHLIGHT_STYLE_SEARCHRESULTS);
            } else {
                title = highlightSearchTerms(title, HIGHLIGHT_STYLE_LIVESEARCH);
                title = highlightSearchTerms(title, HIGHLIGHT_STYLE_KEYWORDS);
            }
            retval.append("<h1>");
            retval.append(title);
            retval.append("</h1>").append(System.lineSeparator());
        }
        // ***********************************************
        // get entry's content
        // ***********************************************
        String content = data.getZettelContent(entryNr);
        // if we have content, show it.
        if (!content.isEmpty()) {
            // now copy the content of the entry to a dummy string. here we convert
            // the format codes into html-tags. the format codes are simplified tags
            // for the user to enable simple format editing
            String dummy = convertUbbToHtmlInternal(settings, data, bibtexObj, content, sourceFrame, false, false,
                    applyNormalization, sanitizeRaw);
            // after the conversion is done, append the content to the resulting return string
            retval.append(dummy);
        } else {
            retval.append("<i>").append(resourceMap.getString("deletedEntry")).append("</i>");
        }
        // close all tags properly
        retval.append("</div>").append(System.lineSeparator());
        // check whether we need an appendix at all...
        boolean appendixNeeded = (data.hasAttachments(entryNr) | data.hasAuthors(entryNr) | data.hasRemarks(entryNr));
        if (appendixNeeded) {
            // ***********************************************
            // here we start with additional infos that belong to an
            // entry's appendix. thus, we have different style options
            // ***********************************************
            retval.append("<div class=\"appendixcontent\">").append(System.lineSeparator());
            // ***********************************************
            // get remarks
            // ***********************************************
            retval.append(getEntryRemarks(data, settings, entryNr, sourceFrame));
            // ***********************************************
            // get authors
            // ***********************************************
            retval.append(getEntryAuthors(data, entryNr, retval.toString(), sourceFrame));
            // ***********************************************
            // now look for attachments
            // ***********************************************
            retval.append(getEntryAttachments(data, entryNr, sourceFrame));
            // close body and html-page
            retval.append("</div>").append(System.lineSeparator());
        }
        retval.append("</body><html>").append(System.lineSeparator()); // NEW!
        return retval.toString();
    }

    /**
     * This method creates the style-definition for author-values in CSS-format,
     * so it can be used in HTML-editorpanes for formatting the "page" (i.e.,
     * the html-content)
     *
     * @param settingsObj a reference to the CSettings-class.
     * @return a string containing the CSS-definition of the authors.
     */
    public static String getAuthorStyleDefinition(Settings settingsObj) {
        // reset the temporary string buffer
        StringBuilder retval = new StringBuilder("");
        // append new style information for the next "html-page"
        // these author information are set to the second textpanel in the main window
        retval.append("<style>").append(System.lineSeparator());
        // body-tag with main font settings
        retval.append("body{font-family:");
        retval.append(settingsObj.getAuthorFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settingsObj.getAuthorFont(Settings.FONTSIZE));
        retval.append("px;color:#");
        retval.append(settingsObj.getAuthorFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settingsObj.getAuthorFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settingsObj.getAuthorFont(Settings.FONTWEIGHT));
        retval.append(";margin:3px}").append(System.lineSeparator());
        retval.append("p{margin:0}").append(System.lineSeparator());
        // css for links, usually only footnotes.
        retval.append("a{color:#003399;text-decoration:none}").append(System.lineSeparator());
        // copy css-style to retval...
        retval.append(getHighlightCSS(settingsObj));
        retval.append("</style>");
        return retval.toString();
    }

    /**
     * This method converts url's to html-hyperlinks.
     *
     * @param dummy the string where url's should be converted to hyperlinks
     * @return the converted string with link-tags around the url's
     */
    public static String convertHyperlinks(String dummy) {
        if (dummy == null || dummy.isEmpty()) {
            return dummy;
        }
        // check whether we already found links
        if (-1 == dummy.indexOf("<a href=")) {
            if (dummy.indexOf('<') == -1) {
                dummy = convertHyperlinksPlain(dummy);
            } else {
                dummy = convertHyperlinksOutsideTags(dummy);
            }
        }
        return dummy;
    }

    private static String convertHyperlinksPlain(String dummy) {
        // this group also considered ( and ) as end of hyperlink, but
        // Wikipedia links e.g. would not be converted correctly.
        // String groupEndOfURL = "[^ \"\\]\\)\\(\\[\\|\\t\\n\\r<]";
        // if no hyperlinks have been found yet, do autoconvert
        String groupEndOfURL = "[^ \"\\[\\]\\|\\t\\n\\r<]";
        dummy = dummy.replaceAll("([\\w]+?://" + groupEndOfURL + "*)", "<a href=\"$1\">$1</a>");
        dummy = dummy.replaceAll("([^://])(www\\." + groupEndOfURL + "*)", "$1<a href=\"http://$2\">$2</a>");
        dummy = dummy.replaceAll("(mailto:)(" + groupEndOfURL + "*)", "$1<a href=\"mailto:$2\">$2</a>");
        return dummy;
    }

    private static String convertHyperlinksOutsideTags(String input) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            int lt = input.indexOf('<', i);
            if (lt == -1) {
                out.append(convertHyperlinksPlain(input.substring(i)));
                break;
            }
            if (lt > i) {
                out.append(convertHyperlinksPlain(input.substring(i, lt)));
            }
            int gt = input.indexOf('>', lt + 1);
            if (gt == -1) {
                out.append(input.substring(lt));
                break;
            }
            out.append(input.substring(lt, gt + 1));
            i = gt + 1;
        }
        return out.toString();
    }

    /**
     * This method highlights searchterms, i.e. it replaces (or: surrounds) all
     * occurences of each string-element of the global array
     * {@code highlighterms} inside the given string {@code dummy} with
     * HTML-Tags that assign a style to this HTML-Tags that it looks
     * highlighted.
     *
     * @param dummy the string where the searchterms should be highlighted
     * @param style
     * @return a "converted" string with highlight-HTML-tags surrounding the
     * searchterms
     */
    public static String highlightSearchTerms(String dummy, int style) {
        return highlightSearchTerms(dummy, style, false);
    }

    /**
     * This method highlights searchterms, i.e. it replaces (or: surrounds) all
     * occurences of each string-element of the global array
     * {@code highlighterms} inside the given string {@code dummy} with
     * HTML-Tags that assign a style to this HTML-Tags that it looks
     * highlighted.
     *
     * @param dummy the string where the searchterms should be highlighted
     * @param style
     * @return a "converted" string with highlight-HTML-tags surrounding the
     * searchterms
     */
    public static String highlightSearchTermsInUBB(String dummy, int style) {
        return highlightSearchTerms(dummy, style, true);
    }

    /**
     * This method highlights searchterms, i.e. it replaces (or: surrounds) all
     * occurences of each string-element of the global array
     * {@code highlighterms} inside the given string {@code dummy} with
     * HTML-Tags that assign a style to this HTML-Tags that it looks
     * highlighted.
     *
     * @param dummy the string where the searchterms should be highlighted
     * @param cssclass either {@code null} if this method is a usual call, or a
     * style-attribute for the span-tag. the latter is necessary when calling
     * this method when exporting entries to PDF, since the PDF-HTML-Parser
     * cannot handle class-attributes, so instead we directly put the
     * style-information into the HTML-tag.
     * @param isUBB {@code true} if an UBB-string should be converted,
     * {@code false} when an already converted HTML-Page should be converted.
     * @return a "converted" string with highlight-HTML-tags surrounding the
     * searchterms
     */
    private static String highlightSearchTerms(String dummy, int style, boolean isUBB) {
        String cssclass = "class=\"hs_kw\"";
        String[] highlightterms = null;
        switch (style) {
            case HIGHLIGHT_STYLE_KEYWORDS:
                cssclass = "class=\"hs_kw\"";
                highlightterms = highlightTermsKeywords;
                break;
            case HIGHLIGHT_STYLE_SEARCHRESULTS:
                cssclass = "class=\"hs_sr\"";
                highlightterms = highlightTermsSearch;
                break;
            case HIGHLIGHT_STYLE_LIVESEARCH:
                cssclass = "class=\"hs_ls\"";
                highlightterms = highlightTermsLivesearch;
                break;
        }
        // if parameters in the string array highlight-terms have been passed, we assume that
        // these terms should be highlighted...
        if ((highlightterms != null) && (highlightterms.length > 0)) {
            // create a new string-array containing the highlightterms.
            // the highlightterms might be regular expressions, so we check for this
            // here. if we find a valid pattern, we assume the searchterm is just a one-string-array,
            // containing the regular expression pattern. if the pattern-compiling fails, we assume we have
            // a normal array with search terms.
            // depending on this, the array "findterms" will either contain the results from the matching
            // regular expresions, or the usual searchterms
            String[] findterms;
            // we assume having a regular expression only when we find certain meta-characters
            // to check for meta characters, we create an array with those chars and check whether
            // our searchterm "highlighterms" contains at least one of these chars...
            // if we found one of the meta-chars that indicate that we might have a regular
            // expression, we try to compile the pattern given in highlighterms.
            if (Tools.hasRegExChars(highlightterms[0])) {
                try {
                    // create a pattern from the first search term. if it fails, go on
                    // to the catch-block, else contiue here.
                    Pattern p = Pattern.compile(highlightterms[0]);
                    // now we know we have a valid regular expression. we now want to
                    // retrieve all matching groups
                    Matcher m = p.matcher(dummy);
                    // add each found string to a linked array-list
                    List<String> founds = new ArrayList<>();
                    // iterate all matching groups and check whether these "search terms" have already
                    // been added to the linked list. if not, add it,
                    while (m.find()) {
                        if (!founds.contains(m.group())) {
                            founds.add(m.group());
                        }
                    }
                    // finally, copy contenr of the array list to the findterms.
                    findterms = founds.toArray(new String[founds.size()]);
                } catch (PatternSyntaxException e) {
                    // when the pattern could not be compiles, we have a "usual" expression
                    // as search term
                    findterms = highlightterms;
                }
            } // if we could not find any meta-character, we assume having a "normal" expression
            // as search term
            else {
                findterms = highlightterms;
            }
            // iterate array
            // and surround all search terms with a html-tag to highlight the search term
            for (String st : findterms) {
                // but only if the search term is longer than 1 char...
                if (st.length() > 1) {
                    // st = st.toLowerCase();
                    // escape all relevant regex-chars in
                    // the search term, that could appear
                    st = Pattern.quote(st);
                    // check for whole word
                    if (highlightWholeWord) {
                        st = "\\b" + st + "\\b";
                    }
                    // now replace the searchterm with itself and surrounding html-tags. use
                    // regex-lookahead to avoid a replacement within existing html-tags
                    if (!isUBB) {
                        dummy = dummy.replaceAll("(?i)" + st + "(?![^<>]*>)", "<span " + cssclass + ">$0</span>");
                    } else {
                        dummy = dummy.replaceAll("(?<![\\[img\\](.*?)\\[/img\\]])" + st, "<span " + cssclass + ">$0</span>");
                    }
                }
            }
        }
        return dummy;
    }

    /**
     * Sets the terms that will be highlighted when preparing the entry for
     * display. in case no highlighting is requested, use {@code null} as
     * parameter.
     *
     * @param ht the highligh terms as string-array. These terms will be
     * highlighted in the display. USe {@code null} when no highlight is
     * requested.
     * @param style
     * @param wholeword
     */
    public static void setHighlighTerms(String[] ht, int style, boolean wholeword) {
        // sort array descending, so we have also highlighted words which
        // are "inside" other words.
//        if (ht!=null && ht.length>0) {
//            Arrays.sort(ht, new BackComparer());
//        }
        highlightWholeWord = wholeword;
        switch (style) {
            case HIGHLIGHT_STYLE_KEYWORDS:
                highlightTermsKeywords = ht;
                break;
            case HIGHLIGHT_STYLE_SEARCHRESULTS:
                highlightTermsSearch = ht;
                break;
            case HIGHLIGHT_STYLE_LIVESEARCH:
                highlightTermsLivesearch = ht;
                highlightWholeWord = false;
                break;
        }
    }

    /**
     * This method converts all ubb-tags of an entry, that are used to indicate
     * formatting, into html-tags. We use this to set up an html-page with the
     * entries content that is displayed in a jEditorPane.
     *
     * @param settings a reference to the CSettings-class
     * @param dataObj a reference to the CDaten-class
     * @param bibtexObj
     * @param c the content of the entry in "raw" format (i.e. as it is stored
     * in the xml-file)
     * @param sourceframe
     * @param isExport {@code true} if this method is called from the
     * CExportDlg-dialog, where we can create tooltips for literatur-footnotes
     * in HTML-format. {@code false} otherwise.
     * @param createHTMLFootnotes
     * @return a converted string with html-tags instead of ubb-tags
     */
    public static String convertUbbToHtml(Settings settings, Daten dataObj, BibTeX bibtexObj, String c, int sourceframe,
            boolean isExport, boolean createHTMLFootnotes) {
        return convertUbbToHtmlInternal(settings, dataObj, bibtexObj, c, sourceframe, isExport, createHTMLFootnotes,
                false, false);
    }

    private static String convertUbbToHtmlInternal(Settings settings, Daten dataObj, BibTeX bibtexObj, String c,
            int sourceframe, boolean isExport, boolean createHTMLFootnotes, boolean applyNormalization,
            boolean sanitizeRaw) {
        String normalized = c;
        if (sanitizeRaw) {
            normalized = sanitizeEntryContentForHtml(normalized);
        }
        if (applyNormalization && !isExport) {
            if (Constants.zknlogger.isLoggable(Level.FINE)) {
                UbbNestingNormalizer.NormalizeResult result = UbbNestingNormalizer.normalizeWithStats(normalized);
                normalized = result.text;
                if (result.changed) {
                    Constants.zknlogger.log(Level.FINE,
                            "UBB normalize applied (frame={0}, dropped={1}, autoClosed={2})",
                            new Object[] { sourceframe, result.droppedStrayCloses, result.autoClosed });
                }
            } else {
                normalized = UbbNestingNormalizer.normalize(normalized);
            }
        }
        boolean markdownActivated = settings != null && Boolean.TRUE.equals(settings.getMarkdownActivated());
        String dummy = replaceUbbToHtml(normalized, markdownActivated,
                (Constants.FRAME_DESKTOP == sourceframe), isExport, applyNormalization);
        if (applyNormalization) {
            dummy = sanitizeBrokenAnchorQuotes(dummy);
            dummy = fixBrokenTags(dummy, "<img[^>]*>");
            dummy = fixBrokenTags(dummy, "<a href=[^>]*>");
            dummy = normalizeEmphasisNesting(dummy);
            dummy = closeEmphasisBeforeListEnd(dummy);
        }
        if (dataObj != null) {
            // add title attributes to manual links
            int pos = 0;
            while (pos != -1) {
                // find manual link tag
                pos = dummy.indexOf("href=\"#z_", pos);
                if (pos != -1) {
                    try {
                        // find close
                        int end = dummy.indexOf("\"", pos + 9);
                        // get and convert number
                        int znr = Integer.parseInt(dummy.substring(pos + 9, end));
                        // retrieve title
                        String title = dataObj.getZettelTitle(znr);
                        // if we have title, add it
                        if (!title.isEmpty()) {
                            // check if title has quotes, and if so, remove them
                            title = title.replace("\"", "").replace("'", "");
                            title = title.trim();
                            // insert alt text
                            dummy = dummy.substring(0, end + 1) + " alt=\"" + title + "\"" + " title=\"" + title + "\"" + dummy.substring(end + 1);
                        }
                        // increase pos counter
                        pos = end + 10;
                    } catch (NumberFormatException ex) {
                        pos = pos + 10;
                    }
                }
            }
        }
        // convert footnotes
        if (dataObj != null && settings != null) {
            dummy = convertFootnotes(dataObj, bibtexObj, settings, dummy, false, true);
        }
        // convert movie-tags
        // dummy = dummy.replaceAll("\\[mov ([^\\[]*)\\]", "<a href=\"#mov$1\">Film</a>");
        // highlight text segemtns
        dummy = convertUbbHighlightSegments(dummy);
        dummy = dummy.replaceAll("\\[s ([^\\[]*)\\](.*?)\\[/s\\]", "<span class=\"highlight_$1\">$2</span>");
        // autoconvert url's to hyperlinks
        // we have to do this before(!) converting image-tags, otherwise the source-reference (file://) will
        // be recognized as URL (that methods searches for xxxx://).
        dummy = convertHyperlinks(dummy);
        // convert images, including resizing images
        if (dataObj != null && settings != null) {
            dummy = convertImages(dataObj, settings, dummy, isExport);
        }
        // convert possible table tags to html
        dummy = convertTablesToHTML(dummy);
        // convert possible form tags to html
        if (dataObj != null && settings != null) {
            dummy = convertForms(settings, dataObj, dummy, Constants.EXP_TYPE_HTML, false, isExport);
        }
        // if parameters in the string array highlight-terms have been passed, we assume that
        // these terms should be highlighted... but don't highlight search terms on desktop!
        // desktop has its own live search function...
        if (Constants.FRAME_DESKTOP != sourceframe) {
            if (Constants.FRAME_SEARCH == sourceframe) {
                dummy = highlightSearchTerms(dummy, HIGHLIGHT_STYLE_SEARCHRESULTS);
            } else {
                dummy = highlightSearchTerms(dummy, HIGHLIGHT_STYLE_LIVESEARCH);
                dummy = highlightSearchTerms(dummy, HIGHLIGHT_STYLE_KEYWORDS);
            }
        }
        // when we export data to HTML-format, we can create tooltips for the footnotes...
        if (isExport && createHTMLFootnotes) {
            // create tooltips for the footnotes.
            int pos = 0;
            while (pos != -1) {
                // search for link-tag which has a footnote-ankh
                pos = dummy.indexOf(Constants.footnoteHtmlTag, pos);
                // if something was found, go on...
                if (pos != -1) {
                    // find end of href-attribute, so we can exract the author-number
                    int numpos = dummy.indexOf("\"", pos + Constants.footnoteHtmlTag.length());
                    if (numpos != -1) {
                        // now extract author-number
                        try {
                            // convert substring to integer
                            int authornr = Integer.parseInt(dummy.substring(pos + Constants.footnoteHtmlTag.length(), numpos));
                            // retrieve author-value and remove all quote-signs
                            String authorval = dataObj.getAuthor(authornr).replace("\"", "").replace("'", "");
                            // determine close-bracket of a-tag
                            int closebracket = dummy.indexOf(">", pos + Constants.footnoteHtmlTag.length());
                            if (closebracket != -1) {
                                // insert title-tag inside of footnote-link
                                dummy = dummy.substring(0, closebracket) + " title=\"" + authorval + "\"" + dummy.substring(closebracket);
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                    // increae pos-counter to avoid endless while-loops...
                    pos += Constants.footnoteHtmlTag.length();
                }
            }
        }
        return dummy;
    }

    /**
     * This method replaces footnote tags with the associated authors, thus returning well formatted
     * author values instead of cryptic footnote tags. The formatted author value is returned
     * in plain text w/o any Markdown, TeX or HTML-formatting.
     * 
     * @param data
     * @param bibtexObj
     * @param settings
     * @param content
     * @return 
     */
    public static String convertFootnotesToPlain(Daten data, BibTeX bibtexObj, Settings settings, String content) {
        return convertFootnotes(data, bibtexObj, settings, content, false, false);
    }
    
    private static String convertFootnotes(Daten data, BibTeX bibtexObj, Settings settings, String content, boolean isLatex, boolean asHtml) {
        if (isLatex) {
            content = content.replaceAll("\\[fn ([^\\[]*)\\]", "(FN $1)");
        } else {
            // save find-position
            List<Integer> start = new ArrayList<>();
            List<Integer> end = new ArrayList<>();
            try {
                // create foot note pattern
                Pattern p = Pattern.compile("\\[fn ([^\\[]*)\\]");
                // create matcher
                Matcher m = p.matcher(content);
                // check for occurrences
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
                    // final author string
                    String replaceValue;
                    // more than 1 value means, we have a page numner after colon
                    if (fnpagenr.length > 1) {
                        // we assume reference index number at first position
                        fn = fnpagenr[0];
                        pagenr = fnpagenr[1];
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
                        fnnr = data.getAuthorBibKeyPosition(fn);
                        fn = String.valueOf(fnnr);
                    }
                    // retrieve author value's bibkey
                    String bibkey = data.getAuthorBibKey(fnnr);
                    // check whether we have any bibkey-value
                    if (bibkey != null && !bibkey.isEmpty()) {
                        // get formatted author value
                        String formattedAuthor = bibtexObj.getFormattedAuthor(bibkey);
                        // check for valid value. if we have formatted author, use this
                        if (formattedAuthor != null && !formattedAuthor.isEmpty()) {
                            //
                            StringBuilder ref = new StringBuilder("");
                            // replace footnote as HTML?
                            if (asHtml) {
                                ref.append(Constants.footnoteHtmlTag)
                                        .append(fn)
                                        .append("\">")
                                        .append(formattedAuthor)
                                        .append("</a>");
                                // add page number, if we have any
                                if (pagenr != null && !pagenr.isEmpty()) {
                                    ref.append(", ").append(resourceMap.getString("footnotePage")).append(pagenr);
                                }
                            } else {
                                ref.append(formattedAuthor);
                                // add page number, if we have any
                                if (pagenr != null && !pagenr.isEmpty()) {
                                    ref.append(", ").append(resourceMap.getString("footnotePage")).append(pagenr);
                                }
                            }
                            // copy to string
                            replaceValue = ref.toString();
                        } else {
                            replaceValue = getNonFormattedFootnote(settings, fn, pagenr, asHtml);
                        }
                        // now that we have the bibkey, replace footnote with cite-tag
                        content = content.substring(0, start.get(i)) + replaceValue + content.substring(end.get(i));
                    } else {
                        replaceValue = getNonFormattedFootnote(settings, fn, pagenr, asHtml);
                        // now that we have the bibkey, replace footnote with cite-tag
                        content = content.substring(0, start.get(i)) + replaceValue + content.substring(end.get(i));
                    }
                }
            } catch (PatternSyntaxException | IndexOutOfBoundsException ex) {
            } catch (NumberFormatException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                Constants.zknlogger.log(Level.WARNING, "Could not convert author ID into author number!");
            }
            if (asHtml) {
                // footnote: [fn 102] becomes <sup>[102]</sup> (or just [102], if no superscription is set)
                if (settings.getSupFootnote()) {
                    content = content.replaceAll("\\[fn ([^\\[]*)\\]", "<sup>[" + Constants.footnoteHtmlTag + "$1\">$1</a>]</sup>");
                } else {
                    content = content.replaceAll("\\[fn ([^\\[]*)\\]", "[" + Constants.footnoteHtmlTag + "$1\">$1</a>]");
                }
            }
        }
        return content;
    }
    
    private static String getNonFormattedFootnote(Settings settings, String fn, String pagenr, boolean asHtml) {
        StringBuilder ref = new StringBuilder("");
        if (asHtml) {
            // else use footnote number
            if (settings.getSupFootnote()) {
                ref.append("<sup>");
            }
            ref.append("[")
                    .append(Constants.footnoteHtmlTag)
                    .append(fn)
                    .append("\">")
                    .append(fn)
                    .append("</a>");
            // add page number, if we have any
            if (pagenr != null && !pagenr.isEmpty()) {
                ref.append(", ").append(resourceMap.getString("footnotePage")).append(pagenr);
            }
            ref.append("]");
            if (settings.getSupFootnote()) {
                ref.append("</sup>");
            }
        } else {
            ref.append("[").append(fn);
            // add page number, if we have any
            if (pagenr != null && !pagenr.isEmpty()) {
                ref.append(", ").append(resourceMap.getString("footnotePage")).append(pagenr);
            }
            ref.append("]");
        }
        return ref.toString();
    }

    private static String fixImageTags(String dummy) {
        // init position counter
        int pos = 0;
        int pos2;
        // we now look for the resize-indicator "|" in each image tag. Even if images should
        // not be resized, we have to insert this char in order to let the regex-replace-all function
        // work correctly. Thus, each image-tag without "|" inside will get an additional "|"
        // just befor the closing tag
        while (pos != -1) {
            // check whether img-tag exists
            pos = dummy.indexOf("[img]", pos);
            // if yes, go on...
            if (pos != -1) {
                // ... and check for closing tag
                pos2 = dummy.indexOf("[/img]", pos);
                // if found, go on...
                if (pos2 != -1) {
                    // ...and check whether image tag has an "|"
                    if (-1 == dummy.substring(pos, pos2).indexOf("|")) {
                        // if not, insert such a resize-indicator char
                        dummy = dummy.substring(0, pos2) + "|" + dummy.substring(pos2);
                    }
                }
                // and change pos-counter
                pos = pos2;
            }
        }
        return dummy;
    }

    private static String fixImagePaths(String dummy, String imgpath) {
        // if we have a windows operating system, we have to add an additonal
        // separator char, so the link to the image starts with "file:///" instead of only "file://"
        String sep = "";
        if (IS_WINDOWS) {
            imgpath = File.separatorChar + imgpath;
            sep = String.valueOf(File.separatorChar);
        }
        // init position counter
        int pos = 0;
        int pos2;
        // we now look for the resize-indicator "|" in each image tag. Even if images should
        // not be resized, we have to insert this char in order to let the regex-replace-all function
        // work correctly. Thus, each image-tag without "|" inside will get an additional "|"
        // just befor the closing tag
        while (pos != -1) {
            // check whether img-tag exists
            pos = dummy.indexOf("[img]", pos);
            // if yes, go on...
            if (pos != -1) {
                // ... and check for closing tag
                pos2 = dummy.indexOf("[/img]", pos);
                // if found, go on...
                if (pos2 != -1) {
                    try {
                        String path = dummy.substring(pos + 5, pos2);
                        File imgfile = new File(path.substring(0, path.indexOf("|")));
                        if (!imgfile.exists()) {
                            // image tag: [img]img/gfx.png[/img] becomes <img src="/img/gfx.png">
                            // first insert the path to the image folder inside the img-src.
                            dummy = dummy.substring(0, pos) + "[img]file://" + imgpath + dummy.substring(pos + 5);
                        } else {
                            // image tag: [img]img/gfx.png[/img] becomes <img src="/img/gfx.png">
                            // first insert the path to the image folder inside the img-src.
                            dummy = dummy.substring(0, pos) + "[img]file://" + sep + dummy.substring(pos + 5);
                        }
                    } catch (IndexOutOfBoundsException ex) {
                    }
                }
                // and change pos-counter
                pos = pos2;
            }
        }
        return dummy;
    }

    private static String convertImages(Daten dataObj, Settings settings, String dummy, boolean isExport) {
        // fix image tags, needed due to manual resizing with "|".,
        // so the regex below works
        dummy = fixImageTags(dummy);
        // get individual resize values from image tags
        List<Integer> resizevalues = Tools.getImageResizeValues(dummy);
        // here we create a path to our image folder. this is needed for
        // converting image tags, since the image ae copied to an own local folder,
        // but the source-information only stores the file name, not the path information.
        // see CNewEntry.java, method "insertImage" for more details
        // image tag: [img]img/gfx.png[/img] becomes <img src="/img/gfx.png">
        // first insert the path to the image folder inside the img-src.
//        dummy = dummy.replace("[img]", "[img]file://"+imgpath);
        dummy = fixImagePaths(dummy, settings.getImagePath(dataObj.getUserImagePath(), true));
        // dummy = dummy.replaceAll("\\[img\\](.*?)\\[/img\\]", "<img src=\"$1\">");
        dummy = dummy.replaceAll("\\[img\\]([^|]*)(.*?)\\[/img\\]", "<img src=\"$1\">");
        // check whether the user likes to resize large images to smaller
        // thumbnails
        if (settings.getImageResize() || resizevalues.size() > 0) {
            // find index that searches for img-tags
            int pos = 0;
            int valcnt = 0;
            // addvalue, indicating where to extract the filename of the image. since the windows-os
            // has an additional "/", this value is different in windows and other systems...
            int addvalue = (IS_WINDOWS) ? 18 : 17;
            // find occurences of mage tags
            while (pos != -1) {
                pos = dummy.indexOf("<img src=", pos);
                // if we found an image, go on
                if (pos != -1) {
                    // find the end of the src-tag, so we know where to find the filename
                    int end = dummy.indexOf("\"", pos + 10);
                    // check for valid value
                    if (end != -1) {
                        try {
                            int width, height, rw, rh;
                            // create a new file from the imagepath
                            File imageFile = new File(dummy.substring(pos + addvalue, end));
                            try {
                                // try to read the image
                                Image image = new ImageIcon(ImageIO.read(imageFile)).getImage();
                                // get the image's size (width/height)
                                width = image.getWidth(null);
                                height = image.getHeight(null);
                                // check whether we have predifined resize values...
                                if (resizevalues.size() > 0) {
                                    rw = rh = resizevalues.get(valcnt);
                                    valcnt++;
                                } // ...or automatic recaling
                                else {
                                    // get the preferred maximum width and height
                                    rw = settings.getImageResizeWidth();
                                    rh = settings.getImageResizeHeight();
                                }
                                // if the image is larger than the preffered thumbnail-size, resize
                                // width and heigh (proportionally)
                                if (width > rw) {
                                    float faktor = (float) width / rw;
                                    width = (int) (width / faktor);
                                    height = (int) (height / faktor);
                                }
                                if (height > rh) {
                                    float faktor = (float) height / rh;
                                    width = (int) (width / faktor);
                                    height = (int) (height / faktor);
                                }
                                // create a new string builder
                                StringBuilder resize = new StringBuilder("");
                                // here we extract the original img-source and add a
                                // width and height attribute. furthermore, the image is
                                // surrounded by a hyperlink-tag that referrs to the original image
                                if (!isExport) {
                                    resize.append("<a href=\"");
                                    resize.append(dummy.substring(pos + addvalue, end));
                                    resize.append("\">");
                                }
                                resize.append(dummy.substring(pos, end + 1));
                                resize.append(" width=\"");
                                resize.append(String.valueOf(width));
                                resize.append("\" height=\"");
                                resize.append(String.valueOf(height));
                                resize.append("\" border=\"0\">");
                                if (!isExport) {
                                    resize.append("</a>");
                                }
                                // replace old img-tag with new one
                                // we have to do this between-step because we need to know where
                                // the search has to coninute (pos).
                                String newdummy = dummy.substring(0, pos) + resize.toString();
                                // set search-position indicator
                                pos = newdummy.length();
                                dummy = newdummy + dummy.substring(end + 2);
                            } catch (IOException | IndexOutOfBoundsException ex) {
                                pos = end + 1;
                            }
                        } catch (IndexOutOfBoundsException ex) {
                        }
                    }
                }
            }
        }
        return dummy;
    }

    private static String replaceUbbToHtml(String dummy, boolean isMarkdownActivated, boolean isDesktop, boolean isExport,
            boolean applyMarkdownNormalization) {
        // replace headlines
        String head1, head2, head3, head4;
        String head1md, head2md, head3md, head4md;
        if (isDesktop) {
            head1 = "<h3>$1</h3>";
            head2 = "<h4>$1</h4>";
            head3 = "<h5>$1</h5>";
            head4 = "<h6>$1</h6>";
            head1md = "$1<h3>$2</h3>";
            head2md = "$1<h4>$2</h4>";
            head3md = "$1<h5>$2</h5>";
            head4md = "$1<h6>$2</h6>";
        } else {
            head1 = "<h2>$1</h2>";
            head2 = "<h3>$1</h3>";
            head3 = "<h4>$1</h4>";
            head4 = "<h5>$1</h5>";
            head1md = "$1<h2>$2</h2>";
            head2md = "$1<h3>$2</h3>";
            head3md = "$1<h4>$2</h4>";
            head4md = "$1<h5>$2</h5>";
        }
        // check whether markdown is activated
        // if yes, replace markdown here
        if (isMarkdownActivated) {
            dummy = dummy.replace("[br]", "\n");
            if (applyMarkdownNormalization) {
                dummy = normalizeMarkdownEmphasis(dummy);
            }
            // quotes
            dummy = dummy.replaceAll("(^|\\n)(\\> )(.*)", "[q]$3[/q]");
            // after quotes have been replaced, replace < and > signs
            if (!isExport) {
                dummy = dummy.replace(">", "&gt;").replace("<", "&lt;");
            }
            // bullets
            dummy = dummy.replaceAll("(^|\\n)(\\d\\. )(.*)", "<ol><li>$3</li></ol>");
            dummy = dummy.replace("</ol><ol>", "");
            dummy = dummy.replaceAll("(^|\\n)(\\* )(.*)", "<ul><li>$3</li></ul>");
            dummy = dummy.replace("</ul><ul>", "");
            // bold and italic formatting in markdown
            dummy = dummy.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>");
            dummy = dummy.replaceAll("___(.*?)___", "<b><i>$1</i></b>");
            // bold formatting
            dummy = dummy.replaceAll("__(.*?)__", "<b>$1</b>");
            dummy = dummy.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
            // italic formatting
            dummy = dummy.replaceAll("_(.*?)_", "<i>$1</i>");
            dummy = dummy.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
            // headlines
            dummy = dummy.replaceAll("(^|\\n)#{4} (.*)", head4md);
            dummy = dummy.replaceAll("(^|\\n)#{3} (.*)", head3md);
            dummy = dummy.replaceAll("(^|\\n)#{2} (.*)", head2md);
            dummy = dummy.replaceAll("(^|\\n)#{1} (.*)", head1md);
            // strike
            dummy = dummy.replaceAll("---(.*?)---", "<strike>$1</strike>");
            // images
            // TODO: consider moving images above italics to avoid underscore interference.
            dummy = replaceMarkdownImages(dummy);
            dummy = replaceMarkdownLinks(dummy);
            // replace line breaks
            dummy = dummy.replace("\n", "[br]");
        } else {
            // if we don't have markdown, and thus no quotes-syntax with "> ...",
            // we need to replace non-tag-< and > here
            if (!isExport) {
                dummy = dummy.replace(">", "&gt;").replace("<", "&lt;");
            }
        }
        // inline-code blocks formatting
        dummy = dummy.replaceAll("\\`(.*?)\\`", "<code>$1</code>");
        // new line
        dummy = dummy.replace("[br]", "<br>");
        // hyperlinks
        dummy = dummy.replaceAll("\\[([^\\[]+)\\]\\(http([^\\)]+)\\)", "<a href=\"http$2\" title=\"http$2\">$1</a>");
        // bold formatting: [f] becomes <b>
        dummy = dummy.replaceAll("\\[f\\](.*?)\\[/f\\]", "<b>$1</b>");
        // italic formatting: [k] becomes <i>
        dummy = dummy.replaceAll("\\[k\\](.*?)\\[/k\\]", "<i>$1</i>");
        // underline formatting: [u] becomes <u>
        dummy = dummy.replaceAll("\\[u\\](.*?)\\[/u\\]", "<u>$1</u>");
        // headline: [h4] becomes <h5>
        dummy = dummy.replaceAll("\\[h4\\](.*?)\\[/h4\\]", head4);
        // headline: [h3] becomes <h4>
        dummy = dummy.replaceAll("\\[h3\\](.*?)\\[/h3\\]", head3);
        // headline: [h2] becomes <h3>
        dummy = dummy.replaceAll("\\[h2\\](.*?)\\[/h2\\]", head2);
        // headline: [h1] becomes <h2>
        dummy = dummy.replaceAll("\\[h1\\](.*?)\\[/h1\\]", head1);
        // cite formatting: [q] becomes <q>
        if (isExport) {
            dummy = dummy.replaceAll("\\[q\\](.*?)\\[/q\\]", "<blockquote>$1</blockquote>");
            // quotation marks
            dummy = dummy.replaceAll("\\[qm\\](.*?)\\[/qm\\]", "<q>$1</q>");
        } else {
            dummy = dummy.replaceAll("\\[q\\](.*?)\\[/q\\]", "<div class=\"zitat\">$1</div>");
            // quotation marks
            dummy = dummy.replace("[qm]", "&ldquo;");
            dummy = dummy.replace("[/qm]", "&rdquo;");
        }
        // code blocks formatting: [code] becomes <pre>
        dummy = dummy.replaceAll("\\[code\\](.*?)\\[/code\\]", "<pre>$1</pre>");
        // strike-through formatting: [d] becomes <strike>
        dummy = dummy.replaceAll("\\[d\\](.*?)\\[/d\\]", "<strike>$1</strike>");
        // superscript: [sup] becomes <sup>
        dummy = dummy.replaceAll("\\[sup\\](.*?)\\[/sup\\]", "<sup>$1</sup>");
        // subscript: [sub] becomes <sub>
        dummy = dummy.replaceAll("\\[sub\\](.*?)\\[/sub\\]", "<sub>$1</sub>");
        // font
        dummy = dummy.replaceAll("\\[font ([^\\[]*)\\](.*?)\\[/font\\]", "<span style=\"font-family:$1\">$2</span>");
        // center alignment: [c] becomes <center>
        dummy = dummy.replaceAll("\\[c\\](.*?)\\[/c\\]", "<div style=\"text-align:center\">$1</div>");
        // left alignment
        dummy = dummy.replaceAll("\\[al\\](.*?)\\[/al\\]", "<div style=\"text-align:left\">$1</div>");
        // right alignment
        dummy = dummy.replaceAll("\\[ar\\](.*?)\\[/ar\\]", "<div style=\"text-align:right\">$1</div>");
        // justify alignment
        dummy = dummy.replaceAll("\\[ab\\](.*?)\\[/ab\\]", "<div style=\"text-align:justify\">$1</div>");
        // color formatting: [color #rrggbb] becomes <span style="color:#rrggbb"> ([^\\[]*)
        dummy = dummy.replaceAll("\\[color ([^\\[]*)\\](.*?)\\[/color\\]", "<span style=\"color:$1\">$2</span>");
        // background-color formatting: [h #rrggbb] becomes <span style="background-color:#rrggbb">
        dummy = dummy.replaceAll("\\[h ([^\\[]*)\\](.*?)\\[/h\\]", "<span style=\"background-color:$1\">$2</span>");
        // margins formatting: [m 0.5] becomes <span style="margin-left:0.5cm;margin-right:0.5cm">
        // dummy = dummy.replaceAll("\\[m ([^\\[]*)\\]([^\\[]*)\\[/m\\]", "<div style=\"margin-left:$1cm;margin-right:$1cm\">$2</div>");
        dummy = dummy.replaceAll("\\[m ([^\\[]*)\\](.*?)\\[/m\\]", "<div style=\"margin-left:$1cm;margin-right:$1cm\">$2</div>");
        // unordered list: [l] becomes <ul>
        dummy = dummy.replaceAll("\\[l\\](.*?)\\[/l\\]", "<ul>$1</ul>");
        // ordered list: [n] becomes <ol>
        dummy = dummy.replaceAll("\\[n\\](.*?)\\[/n\\]", "<ol>$1</ol>");
        // bullet points: [*] becomes <li>
        dummy = dummy.replaceAll("\\[\\*\\](.*?)\\[/\\*\\]", "<li>$1</li>");
        // manual links
        dummy = dummy.replaceAll("\\[z ([^\\[]*)\\](.*?)\\[/z\\]", "<a class=\"manlink\" href=\"#z_$1\">$2</a>");
        // remove all new lines after headlines
        dummy = dummy.replaceAll("\\</h([^\\<]*)\\>\\<br\\>", "</h$1>");
        return dummy;
    }

    private static String replaceMarkdownImages(String input) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("![", i)) {
                int altEnd = input.indexOf("](", i + 2);
                if (altEnd != -1) {
                    int urlStart = altEnd + 2;
                    int urlEnd = findClosingParen(input, urlStart);
                    if (urlEnd != -1) {
                        String url = input.substring(urlStart, urlEnd);
                        out.append("[img]").append(url).append("[/img]");
                        i = urlEnd + 1;
                        continue;
                    }
                }
            }
            out.append(input.charAt(i));
            i++;
        }
        return out.toString();
    }

    private static String replaceMarkdownLinks(String input) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '[' && (i == 0 || input.charAt(i - 1) != '!')) {
                int textEnd = input.indexOf("](", i + 1);
                if (textEnd != -1) {
                    int urlStart = textEnd + 2;
                    if (input.startsWith("http", urlStart)) {
                        int urlEnd = findClosingParen(input, urlStart);
                        if (urlEnd != -1) {
                            String label = input.substring(i + 1, textEnd);
                            String url = input.substring(urlStart, urlEnd);
                            out.append("<a href=\"")
                                    .append(escapeHtmlAttribute(url))
                                    .append("\" title=\"")
                                    .append(escapeHtmlAttribute(url))
                                    .append("\">")
                                    .append(label)
                                    .append("</a>");
                            i = urlEnd + 1;
                            continue;
                        }
                    }
                }
            }
            out.append(input.charAt(i));
            i++;
        }
        return out.toString();
    }

    private static String normalizeMarkdownEmphasis(String input) {
        StringBuilder out = new StringBuilder(input.length());
        String[] lines = input.split("\n", -1);
        for (int idx = 0; idx < lines.length; idx++) {
            String line = lines[idx];
            String normalizedLine = normalizeEmphasisInLine(line);
            out.append(normalizedLine);
            if (idx < lines.length - 1) {
                out.append('\n');
            }
        }
        return out.toString();
    }

    private static String normalizeEmphasisInLine(String line) {
        if (line.isEmpty()) {
            return line;
        }
        int doubleCount = countNonOverlapping(line, "**");
        if (doubleCount % 2 != 0) {
            line = line + "**";
        }
        int singleCount = countSingleAsterisks(line);
        if (singleCount % 2 != 0) {
            line = line + "*";
        }
        return line;
    }

    private static int countNonOverlapping(String line, String token) {
        int count = 0;
        int idx = 0;
        while (idx != -1) {
            idx = line.indexOf(token, idx);
            if (idx != -1) {
                count++;
                idx += token.length();
            }
        }
        return count;
    }

    private static int countSingleAsterisks(String line) {
        int count = 0;
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (ch == '*') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '*') {
                    i += 2;
                    continue;
                }
                count++;
            }
            i++;
        }
        return count;
    }

    private static int findClosingParen(String input, int start) {
        int depth = 0;
        int i = start;
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (ch == '\\' && i + 1 < input.length() && input.charAt(i + 1) == ')') {
                i += 2;
                continue;
            }
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
            i++;
        }
        return -1;
    }

    private static String escapeHtmlAttribute(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * Sanitizes entry content before HTML rendering. This removes or neutralizes
     * known sources of invalid nesting, especially [c] and [m ...] formatting.
     */
    public static String sanitizeEntryContentForHtml(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String sanitized = Tools.cleanHTML(raw);
        sanitized = neutralizeMarkdownImages(sanitized);
        sanitized = escapeStrayLt(sanitized);
        return sanitized;
    }

    private static String neutralizeMarkdownImages(String input) {
        StringBuilder out = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("![", i)) {
                int altEnd = input.indexOf("](", i + 2);
                if (altEnd != -1) {
                    int urlStart = altEnd + 2;
                    int urlEnd = findClosingParen(input, urlStart);
                    if (urlEnd != -1) {
                        String alt = input.substring(i + 2, altEnd);
                        String url = input.substring(urlStart, urlEnd);
                        out.append("[Image: ").append(alt).append("] ")
                                .append(neutralizeUrlUnderscores(url));
                        i = urlEnd + 1;
                        continue;
                    }
                }
            }
            out.append(input.charAt(i));
            i++;
        }
        return out.toString();
    }

    private static String neutralizeUrlUnderscores(String url) {
        return url.replace("_", "&#95;");
    }

    public static String sanitizeMarkdownHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        String sanitized = html;
        // Normalize <img> to self-closing form.
        sanitized = sanitized.replaceAll("<img([^>]*?)>", "<img$1 />");
        sanitized = sanitized.replaceAll("<img([^>]*?)\\s*/>", "<img$1 />");
        // Close open <p> before block starts.
        sanitized = sanitized.replaceAll("<p>(\\s*)(<(ul|ol|blockquote)\\b)", "</p>$1$2");
        // Escape stray '<' that does not begin a known tag.
        sanitized = escapeStrayLt(sanitized);
        return sanitized;
    }

    private static String escapeStrayLt(String html) {
        StringBuilder out = new StringBuilder(html.length());
        int i = 0;
        while (i < html.length()) {
            char ch = html.charAt(i);
            if (ch == '<') {
                int end = html.indexOf('>', i + 1);
                if (end == -1) {
                    out.append("&lt;");
                    i++;
                    continue;
                }
                String tag = html.substring(i + 1, end).trim();
                if (isKnownHtmlTag(tag)) {
                    out.append(html, i, end + 1);
                } else {
                    out.append("&lt;");
                }
                i = end + 1;
                continue;
            }
            out.append(ch);
            i++;
        }
        return out.toString();
    }

    private static boolean isKnownHtmlTag(String tag) {
        if (tag.isEmpty()) {
            return false;
        }
        String lower = tag.toLowerCase();
        if (lower.startsWith("/")) {
            lower = lower.substring(1).trim();
        }
        if (lower.startsWith("img") || lower.startsWith("br")) {
            return true;
        }
        int space = lower.indexOf(' ');
        if (space != -1) {
            lower = lower.substring(0, space);
        }
        return "a".equals(lower) || "b".equals(lower) || "i".equals(lower) || "u".equals(lower)
                || "p".equals(lower) || "ul".equals(lower) || "ol".equals(lower) || "li".equals(lower)
                || "blockquote".equals(lower) || "h1".equals(lower) || "h2".equals(lower)
                || "h3".equals(lower) || "h4".equals(lower) || "h5".equals(lower) || "h6".equals(lower)
                || "span".equals(lower) || "div".equals(lower) || "code".equals(lower)
                || "strike".equals(lower);
    }

    private static String fixBrokenTags(String content, String regexpattern) {
        try {
            // we need to fix emphasing in image tags. if image file path has
            // underscores, these have been replaced to italic / bold etc.
            Pattern p = Pattern.compile(regexpattern);
            // create matcher
            Matcher m = p.matcher(content);
            // save find-position
            List<Integer> start = new ArrayList<>();
            List<Integer> end = new ArrayList<>();
            List<String> itag = new ArrayList<>();
            // check for occurences
            while (m.find()) {
                // save grouping-positions
                start.add(m.start());
                end.add(m.end());
                itag.add(m.group());
            }
            // have any image tags?
            if (!start.isEmpty()) {
                for (int i = start.size() - 1; i >= 0; i--) {
                    // get image tag
                    String imtag = itag.get(i).
                            replaceAll(Pattern.quote("<b>"), "__").
                            replaceAll(Pattern.quote("</b>"), "__").
                            replaceAll(Pattern.quote("<i>"), "_").
                            replaceAll(Pattern.quote("</i>"), "_");
                    content = content.substring(0, start.get(i)) + 
                            imtag +
                            content.substring(end.get(i), content.length());
                }
            }
        } catch (PatternSyntaxException e) {
        }
        return content;
    }

    private static String closeEmphasisBeforeListEnd(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        StringBuilder out = new StringBuilder(html.length());
        int openB = 0;
        int openI = 0;
        int i = 0;
        while (i < html.length()) {
            int lt = html.indexOf('<', i);
            if (lt == -1) {
                out.append(html.substring(i));
                break;
            }
            if (lt > i) {
                out.append(html.substring(i, lt));
            }
            int gt = html.indexOf('>', lt + 1);
            if (gt == -1) {
                out.append(html.substring(lt));
                break;
            }
            String tag = html.substring(lt, gt + 1);
            String name = extractTagName(tag);
            boolean closing = tag.startsWith("</");

            if ("b".equals(name)) {
                if (closing) {
                    if (openB > 0) {
                        openB--;
                        out.append(tag);
                    }
                } else {
                    openB++;
                    out.append(tag);
                }
            } else if ("i".equals(name)) {
                if (closing) {
                    if (openI > 0) {
                        openI--;
                        out.append(tag);
                    }
                } else {
                    openI++;
                    out.append(tag);
                }
            } else if ("li".equals(name) && closing) {
                while (openI > 0) {
                    out.append("</i>");
                    openI--;
                }
                while (openB > 0) {
                    out.append("</b>");
                    openB--;
                }
                out.append(tag);
            } else {
                out.append(tag);
            }
            i = gt + 1;
        }
        return out.toString();
    }

    private static String normalizeEmphasisNesting(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        StringBuilder out = new StringBuilder(html.length());
        Deque<String> stack = new ArrayDeque<>();
        int i = 0;
        while (i < html.length()) {
            int lt = html.indexOf('<', i);
            if (lt == -1) {
                out.append(html.substring(i));
                break;
            }
            if (lt > i) {
                out.append(html.substring(i, lt));
            }
            int gt = html.indexOf('>', lt + 1);
            if (gt == -1) {
                out.append(html.substring(lt));
                break;
            }
            String tag = html.substring(lt, gt + 1);
            String name = extractTagName(tag);
            boolean closing = tag.startsWith("</");

            if ("b".equals(name) || "i".equals(name)) {
                if (!closing) {
                    if (!stack.isEmpty() && stack.peek().equals(name)) {
                        // collapse duplicate open
                    } else {
                        stack.push(name);
                        out.append(tag);
                    }
                } else {
                    if (stack.isEmpty()) {
                        // drop stray close
                    } else if (stack.peek().equals(name)) {
                        stack.pop();
                        out.append(tag);
                    } else if (stack.contains(name)) {
                        // close intervening tags first to restore nesting
                        while (!stack.isEmpty() && !stack.peek().equals(name)) {
                            out.append("</").append(stack.pop()).append(">");
                        }
                        if (!stack.isEmpty() && stack.peek().equals(name)) {
                            stack.pop();
                            out.append(tag);
                        }
                    } else {
                        // drop stray close
                    }
                }
            } else {
                out.append(tag);
            }
            i = gt + 1;
        }
        return out.toString();
    }

    private static String sanitizeBrokenAnchorQuotes(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return html.replace("</a>\">", "\">").replace("</a>\"", "\"");
    }

    private static String extractTagName(String tag) {
        if (tag == null || tag.length() < 3 || tag.charAt(0) != '<') {
            return null;
        }
        int start = tag.startsWith("</") ? 2 : 1;
        int end = start;
        while (end < tag.length()) {
            char ch = tag.charAt(end);
            if (ch == '>' || Character.isWhitespace(ch) || ch == '/') {
                break;
            }
            end++;
        }
        if (end <= start) {
            return null;
        }
        return tag.substring(start, end).toLowerCase();
    }
    
    
    public static String replaceHtmlToUbb(String dummy) {
        // new line
        dummy = dummy.replace("<br>", "[br]");
        dummy = dummy.replace("<br/>", "[br]");
        dummy = dummy.replace("<br />", "[br]");
        // bold formatting: [f] becomes <b>
        dummy = dummy.replaceAll("\\<b\\>(.*?)\\</b\\>", "[f]$1[/f]");
        dummy = dummy.replaceAll("\\<strong\\>(.*?)\\</strong\\>", "[f]$1[/f]");
        // italic formatting: [k] becomes <i>
        dummy = dummy.replaceAll("\\<i\\>(.*?)\\</i\\>", "[k]$1[/k]");
        dummy = dummy.replaceAll("\\<em\\>(.*?)\\</em\\>", "[k]$1[/k]");
        // underline formatting: [u] becomes <u>
        dummy = dummy.replaceAll("\\<u\\>(.*?)\\</u\\>", "[u]$1[/u]");
        // headlines
        dummy = dummy.replaceAll("\\<h1\\>(.*?)\\</h1\\>", "[h1]$1[/h1]");
        dummy = dummy.replaceAll("\\<h2\\>(.*?)\\</h2\\>", "[h2]$1[/h2]");
        dummy = dummy.replaceAll("\\<h3\\>(.*?)\\</h3\\>", "[h3]$1[/h3]");
        dummy = dummy.replaceAll("\\<h4\\>(.*?)\\</h4\\>", "[h4]$1[/h4]");
        dummy = dummy.replaceAll("\\<h5\\>(.*?)\\</h5\\>", "[h5]$1[/h5]");
        // lists
        dummy = dummy.replaceAll("\\<ul\\>(.*?)\\</ul\\>", "[l]$1[/l]");
        dummy = dummy.replaceAll("\\<ol\\>(.*?)\\</ol\\>", "[n]$1[/n]");
        dummy = dummy.replaceAll("\\<li\\>(.*?)\\</li\\>", "[*]$1[/*]");
        return dummy;
    }

    /**
     * This method converts special chars of highlighted segments keywords into
     * chars which can be read by a CSS-style-sheet. If, for instance, the user
     * has assigned a text segement with a keyword that contains umlauts, spaces
     * or other special chars, these chars cannot be used to declare a
     * CSS-class. In this case, all these chars are replaced by a "_".
     *
     * @param content the entry's content
     * @return the entry's content with all appearing highlight-segment-keywords
     * converted
     */
    private static String convertUbbHighlightSegments(String content) {
        try {
            // create a pattern from the first search term. if it fails, go on
            // to the catch-block, else contiue here.
            Matcher m = Pattern.compile("\\[s ([^\\[]*)\\]").matcher(content);
            // find all matches and copy the start/end-positions to our arraylist
            // we now can easily retrieve the found terms and their positions via this
            // array, thus navigation with find-next and find-prev-buttons is simple
            while (m.find()) {
                for (int cnt = 0; cnt < m.groupCount(); cnt++) {
                    String segword = m.group(cnt);
                    String segpart = segword.substring(3);
                    segpart = segpart.replace(" ", "_")
                            .replace(":", "_")
                            .replace("/", "_")
                            .replace("ß", "ss")
                            .replace("ä", "ae")
                            .replace("ö", "oe")
                            .replace("ü", "ue")
                            .replace("Ä", "Ae")
                            .replace("Ö", "Oe")
                            .replace("Ü", "Ue")
                            .replace("\\", "_");
                    content = content.replace(segword, "[s " + segpart);
                }
            }
        } catch (PatternSyntaxException | IndexOutOfBoundsException e) {
            // and leave method
            return content;
        }
        return content;
    }

    /**
     * Since tables cannot be converted using regular expressions (in the
     * {@link #convertUbbToHtml(zettelkasten.CSettings, java.lang.String) convertUbbToHtml()}-method),
     * we do this in an extra method. This, this method converts table-tags into
     * HTML-tables.
     *
     * @param dummy the entry's content
     * @return the entry's content, with table-tags converted to HTML
     */
    private static String convertTablesToHTML(String dummy) {
        // convert tables. we don't do this with regular expressions
        // first, init the index-variable
        int pos = 0;
        int end;
        // go and find all table-tages
        while (pos != -1) {
            // find occurence of opening-tag
            pos = dummy.indexOf("[table]", pos);
            // when open-tag was found, go on and find end of table-tag
            if (pos != -1) {
                // find closing-tag
                end = dummy.indexOf("[/table]", pos);
                // if closing-tag also found, convert content to table
                if (end != -1) {
                    StringBuilder tabelle = new StringBuilder();
                    // get table-content
                    String tablecontent = dummy.substring(pos + 7, end);
                    // get table rows
                    String[] tablerows = tablecontent.split(Pattern.quote("<br>"));
                    // init rowcounter
                    int rowcnt = 0;
                    // iterate all table rows
                    for (String row : tablerows) {
                        // remove leading and trailing spaces and tabs
                        // row = row.trim();
                        // if the row is not empty, go on
                        if (!row.isEmpty()) {
                            // check whether we have a caption
                            if (row.startsWith(Constants.FORMAT_TABLECAPTION_OPEN)) {
                                // replace tags with caption
                                row = row.replace(Constants.FORMAT_TABLECAPTION_OPEN, "<caption>");
                                row = row.replace(Constants.FORMAT_TABLECAPTION_CLOSE, "</caption>");
                                // close row--tags
                                tabelle.append(row).append(System.lineSeparator());
                            } else {
                                // check whether row is table header or a simple data-row. therefore,
                                // look for occurences of "|", which is a cell-separator, or for "^",
                                // whih is the separator for the tableheader
                                boolean isheader = row.contains("^");
                                // use approprate split-char: | for cells, ^ for header-rows
                                String[] tablecells = row.split((isheader) ? "\\^" : "\\|");
                                // check whether we have header line
                                if (isheader) {
                                    // append opening tag for tablerow
                                    tabelle.append("<tr class=\"rowhead\">");
                                } else {
                                    String rowclass = ((rowcnt % 2 == 0) ? "rowodd" : "roweven");
                                    rowcnt++;
                                    tabelle.append("<tr class=\"").append(rowclass).append("\">");
                                }
                                // init columncounter
                                boolean firstcol = true;
                                // iterate each table-data-cell
                                for (String cell : tablecells) {
                                    // check whether we have first column or other column
                                    // so we can style the appearance of first column separately
                                    String colclass = (firstcol) ? "class=\"tfirstcol\"" : "class=\"tothercol\"";
                                    firstcol = false;
                                    // append td/th-tags with table-data
                                    tabelle.append((isheader) ? ("<th " + colclass + ">") : ("<td " + colclass + "valign=\"top\">"));
                                    tabelle.append(cell.trim());
                                    tabelle.append((isheader) ? "</th>" : "</td>");
                                }
                                // close row--tags
                                tabelle.append("</tr>").append(System.lineSeparator());
                            }
                        }
                    }

                    String tableString = (PlatformUtil.isJava7OnMac() || PlatformUtil.isJava7OnWindows()) ? "<table cellspacing=\"0\">" : "<table>";
                    dummy = dummy.substring(0, pos) + tableString + tabelle.toString().replace("\\\\", "<br>") + "</table>" + dummy.substring(end + 8);
                    pos = pos + tabelle.toString().length();
                } // if no valid end-tag was found, try to find possible
                // next table tage
                else {
                    pos += 7;
                }
            }
        }
        return dummy;
    }

    /**
     *
     * @param settings
     * @param dataObj
     * @param dummy
     * @param format
     * @param createFormTag
     * @param isExport
     * @return
     */
    private static String convertForms(Settings settings, Daten dataObj, String dummy, int format, boolean createFormTag, boolean isExport) {
        // TODO weitere formate einfügen, bspw. text?
        // convert forms. we don't do this with regular expressions
        // first, init the index-variable
        int pos = 0;
        int end;
        // go and find all form-tages
        while (pos != -1) {
            // find occurence of opening-tag
            pos = dummy.indexOf(Constants.FORMAT_FORM_TAG, pos);
            // when open-tag was found, go on and find end of table-tag
            if (pos != -1) {
                // find closing-tag
                end = dummy.indexOf("]", pos);
                // if closing-tag also found, convert content to table
                if (end != -1) {
                    // check whether the user wants to create a form tag. if so, no graphic is inserted
                    // into the source-code, but for instance a LaTex-macro for creating forms, or
                    // plain text in text files.
                    if (createFormTag) {
                        // extract form information from tag
                        ExtractFormInformation extractedforms = new ExtractFormInformation(dummy.substring(pos, end));
                        // create replace string
                        String replace = "";
                        // now convert forms to the correct export-format
                        switch (format) {
                            case Constants.EXP_TYPE_TEX:
                                replace = convertFormsToTex(extractedforms.getDescription(), extractedforms.getDistinctions(), extractedforms.getUnmarkedSpace(), extractedforms.hasReentry());
                                break;
                            case Constants.EXP_TYPE_HTML:
                                replace = convertFormsToHtml(extractedforms.getDescription(), extractedforms.getDistinctions(), extractedforms.getUnmarkedSpace(), extractedforms.hasReentry());
                                break;
                        }
                        // and replace form-tag with converted string
                        dummy = dummy.substring(0, pos) + replace + dummy.substring(end + 1);
                    } // here we have no creation of form-tag, so we will insert an image instead. the 
                    // form-tags will be replaced by the related form-image.
                    else {
                        // here we create a path to our form-image folder. this is needed for
                        // converting form-image tags, since the image ae copied to an own local folder,
                        // but the source-information only stores the file name, not the path information.
                        String imgpath = settings.getFormImagePath(dataObj.getUserImagePath(), true);
                        // create replace-string
                        StringBuilder replace = new StringBuilder("");
                        // now convert forms to the correct export-format
                        switch (format) {
                            case Constants.EXP_TYPE_TEX:
                                replace.append("\\includegraphics[scale=0.35]{");
                                replace.append(imgpath);
                                replace.append(FileOperationsUtil.convertFormtagToImagepath(dummy.substring(pos, end + 1), true, true));
                                replace.append("}");
                                break;
                            case Constants.EXP_TYPE_HTML:
                            case Constants.EXP_TYPE_RTF:
                            case Constants.EXP_TYPE_DOCX:
                            case Constants.EXP_TYPE_ODT:
                                // if we have a windows operating system, we have to add an additonal
                                // separator char, so the link to the image starts with "file:///" instead of only "file://"
                                if (IS_WINDOWS) {
                                    imgpath = File.separatorChar + imgpath;
                                }
                                // now create html-snippet
                                replace.append("<img ");
                                // when we export the form-images, we use the large version of images,
                                // but we rescale them. the higher resolution means better printing results.
                                if (isExport) {
                                    // create a new file from the imagepath
                                    File imageFile = new File(imgpath + FileOperationsUtil.convertFormtagToImagepath(dummy.substring(pos, end + 1), true, isExport));
                                    try {
                                        // try to read the image
                                        Image image = new ImageIcon(ImageIO.read(imageFile)).getImage();
                                        // get the image's size (width/height)
                                        int width = image.getWidth(null);
                                        int height = image.getHeight(null);
                                        // and resize image by 50%
                                        replace.append("width=\"").append(String.valueOf(width / 2)).append("\" height=\"").append(String.valueOf(height / 2)).append("\" ");
                                    } catch (IOException ex) {
                                    }
                                }
                                replace.append("src=\"");
                                if (format == Constants.EXP_TYPE_HTML) {
                                    replace.append("file://");
                                }
                                replace.append(imgpath);
                                replace.append(FileOperationsUtil.convertFormtagToImagepath(dummy.substring(pos, end + 1), true, isExport));
                                replace.append("\">");
                                break;
                        }
                        // and replace form-tag with converted string
                        dummy = dummy.substring(0, pos) + replace.toString() + dummy.substring(end + 1);
                    }
                }
                pos += 3;
            }
        }
        return dummy;
    }

    /**
     *
     * @param desc
     * @param distinctions
     * @param reentry
     * @return
     */
    private static String convertFormsToTex(String desc, String[] distinctions, String unmarkedSpace, boolean reentry) {
        StringBuilder sb = new StringBuilder("");
        // check for valid parameter
        if (distinctions != null && distinctions.length > 0) {
            // create distance from form to text
            sb.append("\\FormAbstand{");
            // get description
            if (desc != null && !desc.isEmpty()) {
                sb.append(desc).append(" =");
            }
            // check whether we have a reentry
            if (reentry) {
                // open re entry tag
                sb.append("\\ReEntry{");
            }
            // count distinctions
            for (int cnt = 0; cnt < distinctions.length - 1; cnt++) {
                // and add crosses
                sb.append("\\cross{");
            }
            // iterate distinctions
            for (String d : distinctions) {
                // add each distinction
                sb.append(d).append("}");
            }
            // append unmarked space
            if (unmarkedSpace != null && !unmarkedSpace.isEmpty()) {
                sb.append(unmarkedSpace);
            }
            // close reentry, if necessary
            if (reentry) {
                sb.append("}");
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param desc
     * @param distinctions
     * @param reentry
     * @return
     */
    private static String convertFormsToHtml(String desc, String[] distinctions, String unmarkedSpace, boolean reentry) {
        // create string builder
        StringBuilder sb = new StringBuilder("");
        // check whether we have a description
        if (desc != null && !desc.isEmpty()) {
            // if yes, append it
            sb.append(desc).append(" = ");
        }
        // append first distincion
        sb.append(distinctions[0]).append(" | ");
        // append second distincion
        sb.append(distinctions[1]);
        // check whether we have two marks (=3 distinctions)
        if (3 == distinctions.length) {
            // and if so, append third dist.
            sb.append(" | ").append(distinctions[2]);
        }
        // check whether we have an unmarked space
        if (unmarkedSpace != null && !unmarkedSpace.isEmpty()) {
            sb.append(" || ").append(unmarkedSpace);
        }
        return sb.toString();
    }

    /**
     * Since tables cannot be converted using regular expressions (in the
     * {@link #convertUbbToHtml(zettelkasten.CSettings, java.lang.String) convertUbbToHtml()}-method),
     * we do this in an extra method. This, this method converts table-tags into
     * HTML-tables.
     *
     * @param dummy the entry's content
     * @return the entry's content, with table-tags converted to HTML
     */
    private static String convertTablesToTex(String dummy, Settings settingsObj) {
        // convert tables. we don't do this with regular expressions
        // first, init the index-variable
        int pos = 0;
        int end;
        // if we have statistical table style, don't use vertical column lines / grid
        String grid = (settingsObj.getLatexExportStatisticTableStyle()) ? "" : "|";
        // if we have statistical table style, center table
        String centertable = (settingsObj.getLatexExportStatisticTableStyle()) ? "\\centering" + System.lineSeparator() : "";
        // if we have statistical table style, don't use vertical column lines / grid
        String tablestart = (settingsObj.getLatexExportStatisticTableStyle()) ? "\\begin{tabular}" : "\\begin{tabularx}{\\textwidth}";
        // if we have statistical table style, don't use vertical column lines / grid
        String tableend = (settingsObj.getLatexExportStatisticTableStyle()) ? "\\end{tabular}" : "\\end{tabularx}";
        // caption string
        String caption = "";
        // go and find all table-tages
        while (pos != -1) {
            // find occurence of opening-tag
            pos = dummy.indexOf("[table]", pos);
            // when open-tag was found, go on and find end of table-tag
            if (pos != -1) {
                // find closing-tag
                end = dummy.indexOf("[/table]", pos);
                // if closing-tag also found, convert content to table
                if (end != -1) {
                    StringBuilder tabelle = new StringBuilder();
                    // init header-row-index
                    int headerrow = 0;
                    // get table-content
                    String tablecontent = dummy.substring(pos + 7, end);
                    // get table rows
                    String[] tablerows = tablecontent.split(System.lineSeparator());
                    // check for valid values
                    if (tablerows != null && tablerows.length > 0) {
                        // check whether we have a caption
                        if (tablerows[headerrow].startsWith(Constants.FORMAT_TABLECAPTION_OPEN)) {
                            // replace tags with caption
                            tablerows[headerrow] = tablerows[headerrow].replace(Constants.FORMAT_TABLECAPTION_OPEN, "\\caption{");
                            tablerows[headerrow] = tablerows[headerrow].replace(Constants.FORMAT_TABLECAPTION_CLOSE, "}");
                            // copy row data
                            caption = tablerows[headerrow];
                            caption = caption + System.lineSeparator();
                            // increase header row counter
                            if (tablerows.length > 1) {
                                headerrow = 1;
                            }
                        }
                        // check whether row is table header or a simple data-row. therefore,
                        // look for occurences of "|", which is a cell-separator, or for "^",
                        // whih is the separator for the tableheader
                        boolean isheader = tablerows[headerrow].contains("^");
                        // use approprate split-char: | for cells, ^ for header-rows
                        String[] tablecells = tablerows[headerrow].split((isheader) ? "\\^" : "\\|");
                        // if we have a table header, make each cell text bold, if we have
                        // not statistical table style
                        if (isheader && !settingsObj.getLatexExportStatisticTableStyle()) {
                            // create string builder
                            StringBuilder tablecell = new StringBuilder("");
                            for (String tablecell1 : tablecells) {
                                // enclose text in bold format
                                tablecell.append("\\textbf{").append(tablecell1).append("}^");
                            }
                            // delete last ^
                            if (tablecell.length() > 1) {
                                tablecell.setLength(tablecell.length() - 1);
                            }
                            // set back result to table rows array
                            tablerows[headerrow] = tablecell.toString();
                        }
                        // now check how many columns we have...
                        for (int col = 0; col < tablecells.length; col++) {
                            // append a latex-column
                            if (settingsObj.getLatexExportStatisticTableStyle()) {
                                // align cell content right, if we have
                                // a statistic like table style
                                tabelle.append("r");
                            } // else, if we have "usual" table style, go on here...
                            else {
                                // only for first columns (labels), choose left alignment
                                // tabelle.append((0==col)?"l":"c");
                                // new! X stands for variable column widht
                                tabelle.append((0 == col) ? "l" : "X");
                            }
                        }
                        // close latex-column-count
                        tabelle.append(grid).append("}").append(System.lineSeparator());
                        // add horozontal line
                        tabelle.append("\\hline").append(System.lineSeparator());
                        // create table row counter. this is needed for alternate coloring of rows
                        boolean colorrow = false;
                        // count rows...
                        int rowscounted = 0;
                        // iterate all table rows
                        for (String row : tablerows) {
                            // remove leading and trailing spaces and tabs
                            // row = row.trim();
                            // if the row is not empty, go on
                            if (!row.isEmpty() && !row.startsWith("\\caption{")) {
                                // replace new lines
                                row = row.replaceAll(Pattern.quote("\\\\"), System.lineSeparator());
                                // check whether row is table header or a simple data-row. therefore,
                                // look for occurences of "|", which is a cell-separator, or for "^",
                                // whih is the separator for the tableheader
                                isheader = row.contains("^");
                                // use approprate split-char: | for cells, ^ for header-rows
                                row = row.replaceAll((isheader) ? "\\^" : "\\|", " \\& ");
                                // only colorize table cells if we have non-statistical style
                                if (!settingsObj.getLatexExportStatisticTableStyle()) {
                                    // check whether we have a header-row
                                    if (isheader) {
                                        // apply dark gray cell color
                                        tabelle.append("\\rowcolor{DarkGray}").append(System.lineSeparator());
                                    } else {
                                        // check whether we have odd row
                                        if (colorrow) {
                                            // apply light gray cell color
                                            tabelle.append("\\rowcolor{LightGray}").append(System.lineSeparator());
                                            colorrow = false;
                                        } else {
                                            // apply white cell color
                                            tabelle.append("\\rowcolor{white}").append(System.lineSeparator());
                                            colorrow = true;
                                        }
                                    }
                                }
                                // close row--tags
                                tabelle.append(row).append(" \\\\ ").append(System.lineSeparator());
                                // check whether we have first row
                                if (0 == rowscounted) {
                                    // increase counter, only needed to find first row
                                    rowscounted++;
                                    // now check whether we have statistical table style
                                    // if so, add two lines
                                    if (settingsObj.getLatexExportStatisticTableStyle()) {
                                        tabelle.append("\\hline").append("\\hline").append(System.lineSeparator());
                                    }
                                }
                            }
                        }

                        // create final tex-table-tempplate
                        dummy = dummy.substring(0, pos) + "\\begin{table}[htp]" + System.lineSeparator()
                                + centertable + caption
                                + tablestart + "{" + grid
                                + tabelle.toString()
                                + "\\hline" + System.lineSeparator()
                                + tableend + System.lineSeparator()
                                + "\\end{table}" + dummy.substring(end + 8);
                        pos = pos + tabelle.toString().length();
                    }
                } // if no valid end-tag was found, try to find possible
                // next table tage
                else {
                    pos += 7;
                }
            }
        }
        return dummy;
    }

	/**
	 * This method converts all ubb tags of an item (or entry) that are used to specify
	 * formatting into html tags. This way an HTML page with the content of the item
	 * is created and displayed in a jEditorPane.
	 *
	 * @param settings
	 * @param data
	 * @param bibTeX
	 * @param c                     the content of the entry in "raw" format (i.e.
	 *                              as it is stored in the xml-file)
	 * @param useFootnoteRef
	 * @param createFormTag
	 * @param isDesktop
	 * @param removeNonStandardTags
	 * @return a converted string with html-tags instead of ubb-tags
	 */
    public static String convertUbbToTex(Settings settings, Daten data, BibTeX bibTeX, String c, boolean useFootnoteRef, boolean createFormTag, boolean isDesktop, boolean removeNonStandardTags) {
        // here we create a path to our image folder. this is needed for
        // converting image tags, since the image ae copied to an own local folder,
        // but the source-information only stores the file name, not the path information.
        // see CNewEntry.java, method "insertImage" for more details
        String imgpath = settings.getImagePath(data.getUserImagePath(), true);
        // for latex, we need / instead of \ as separator char
        imgpath = imgpath.replace("\\", "/");
        // if we have a windows operating system, we have to add an additonal
        // separator char, so the link to the image starts with "file:///" instead of only "file://"
        // if (IS_WINDOWS) imgpath = File.separatorChar+imgpath;
        boolean markdownActivated = settings != null && Boolean.TRUE.equals(settings.getMarkdownActivated());
        String dummy = (markdownActivated ? Tools.convertMarkDown2UBB(c) : c);
        // new line
        dummy = dummy.replace("[br]", System.lineSeparator());
        // italic formatting: [k] becomes <i>
        dummy = dummy.replaceAll("\\[k\\](.*?)\\[/k\\]", Matcher.quoteReplacement("\\emph{") + "$1" + "}");
        // bold formatting: [f] becomes <b>
        dummy = dummy.replaceAll("\\[f\\](.*?)\\[/f\\]", Matcher.quoteReplacement("\\textbf{") + "$1" + "}");
        // underline formatting: [u] becomes <u>
        dummy = dummy.replaceAll("\\[u\\](.*?)\\[/u\\]", Matcher.quoteReplacement("\\underline{") + "$1" + "}");
        if (isDesktop) {
            // headline: [h1] becomes <h2>
            dummy = dummy.replaceAll("\\[h1\\](.*?)\\[/h1\\]", Matcher.quoteReplacement("\\paragraph{") + "$1" + "}");
            // headline: [h2] becomes <h3>
            dummy = dummy.replaceAll("\\[h2\\](.*?)\\[/h2\\]", Matcher.quoteReplacement("\\subparagraph{") + "$1" + "}");
        } else {
            // headline: [h1] becomes <h2>
            dummy = dummy.replaceAll("\\[h1\\](.*?)\\[/h1\\]", Matcher.quoteReplacement("\\subsection{") + "$1" + "}");
            // headline: [h2] becomes <h3>
            dummy = dummy.replaceAll("\\[h2\\](.*?)\\[/h2\\]", Matcher.quoteReplacement("\\subsubsection{") + "$1" + "}");
        }
        // cite formatting: [q] becomes <q>
        dummy = dummy.replaceAll("\\[q\\](.*?)\\[/q\\]", Matcher.quoteReplacement("\\begin{quotation}") + "$1" + Matcher.quoteReplacement("\\end{quotation}"));
        // strike-through formatting: [d] becomes <strike>
        dummy = dummy.replaceAll("\\[d\\](.*?)\\[/d\\]", Matcher.quoteReplacement("\\sout{") + "$1" + "}");
        // superscript: [sup] becomes <sup>
        dummy = dummy.replaceAll("\\[sup\\](.*?)\\[/sup\\]", Matcher.quoteReplacement("\\textsuperscript{") + "$1" + "}");
        // subscript: [sub] becomes <sub>
        dummy = dummy.replaceAll("\\[sub\\](.*?)\\[/sub\\]", Matcher.quoteReplacement("\\textsubscript{") + "$1" + "}");
        // center alignment: [c] becomes <center>
        dummy = dummy.replaceAll("\\[c\\](.*?)\\[/c\\]", Matcher.quoteReplacement("\\begin{center}") + "$1" + Matcher.quoteReplacement("\\end{center}"));
        // left alignment
        dummy = dummy.replaceAll("\\[al\\](.*?)\\[/al\\]", Matcher.quoteReplacement("\\begin{flushleft}") + "$1" + Matcher.quoteReplacement("\\end{flushleft}"));
        // right alignment
        dummy = dummy.replaceAll("\\[ar\\](.*?)\\[/ar\\]", Matcher.quoteReplacement("\\begin{flushright}") + "$1" + Matcher.quoteReplacement("\\end{flushright}"));
        // justify alignment
        dummy = dummy.replaceAll("\\[ab\\](.*?)\\[/ab\\]", "$1");
        // color formatting: [color #rrggbb] becomes <span style="color:#rrggbb"> ([^\\[]*)
        if (!removeNonStandardTags) {
            dummy = dummy.replaceAll("\\[color ([^\\[]*)\\](.*?)\\[/color\\]", Matcher.quoteReplacement("\\textcolor{[rgb]{$1}") + "$2" + "}");
        } // in case the user does not want extra packages, use emph instead of color-tags
        else {
            dummy = dummy.replaceAll("\\[color ([^\\[]*)\\](.*?)\\[/color\\]", Matcher.quoteReplacement("\\emph{") + "$2" + "}");
        }
        dummy = dummy.replaceAll("\\[font ([^\\[]*)\\](.*?)\\[/font\\]", "{\\fontfamily{$1}\\selectfont\n$2}");
        // background-color formatting: [h #rrggbb] becomes <span style="background-color:#rrggbb">
        dummy = dummy.replaceAll("\\[h ([^\\[]*)\\](.*?)\\[/h\\]", "$2");
        // margins formatting: [m 0.5] becomes <span style="margin-left:0.5cm;margin-right:0.5cm">
        // dummy = dummy.replaceAll("\\[m ([^\\[]*)\\]([^\\[]*)\\[/m\\]", "<div style=\"margin-left:$1cm;margin-right:$1cm\">$2</div>");
        dummy = dummy.replaceAll("\\[m ([^\\[]*)\\](.*?)\\[/m\\]", Matcher.quoteReplacement("\\vspace{$1 cm}$2"));
        // fix image tags, needed due to manual resizing with "|".,
        // so the regex below works
        dummy = fixImageTags(dummy);
        // image tag: [img]img/gfx.png[/img] becomes <img src="/img/gfx.png">
        // first insert the path to the image folder inside the img-src.
        dummy = dummy.replace("[img]", "[img]" + imgpath);
        dummy = dummy.replaceAll("\\[img\\]([^|]*)(.*?)\\[/img\\]", Matcher.quoteReplacement("\\includegraphics{") + "$1" + "}");
        // unordered list: [l] becomes <ul>
        dummy = dummy.replaceAll("\\[l\\](.*?)\\[/l\\]", Matcher.quoteReplacement("\\begin{itemize}") + System.lineSeparator() + "$1" + Matcher.quoteReplacement("\\end{itemize}"));
        // ordered list: [ol] becomes <ol>
        dummy = dummy.replaceAll("\\[n\\](.*?)\\[/n\\]", Matcher.quoteReplacement("\\begin{enumerate}") + System.lineSeparator() + "$1" + Matcher.quoteReplacement("\\end{enumerate}"));
        // bullet points: [*] becomes <li>
        dummy = dummy.replaceAll("\\[\\*\\](.*?)\\[/\\*\\]", Matcher.quoteReplacement("\\item ") + "$1" + System.lineSeparator());
        // konvertierung von sonderzeichen
        dummy = dummy.replace("...", "\\dots");
        // here we convert all author-footnotes to latex-cite-tags
        dummy = ExportTools.createLatexFootnotes(data, dummy, useFootnoteRef);
        // replace all remaining footnotes without bibkey: [fn 102] becomes (FN xx)
        dummy = convertFootnotes(data, bibTeX, settings, dummy, true, false);
        // convert tables in tex-format
        dummy = convertTablesToTex(dummy, settings);
        // convert form-tags
        dummy = convertForms(settings, data, dummy, Constants.EXP_TYPE_TEX, createFormTag, true);

        // Convert [qm] ("inline quotes") to \enquote{} (fixes bug reproduced by "testBugMarkdownZitatWirdNichtKorrektNachLatexExportiert")
        dummy = dummy.replaceAll("\\[qm\\](.*?)\\[/qm\\]", Matcher.quoteReplacement("\\enquote{") + "$1" + "}");

        return dummy;
    }

    /**
     * This method returns the remarks of a current entry in HTML-format, so it
     * is ready to use for displaying in a jEditorPane.
     *
     * @param settings a reference to the CSettings-class
     * @param text the remarks-text, i.e. the entry's remark
     * @return the entry's remark-text converted into HTML, for displaying in a
     * jEditorPane
     */
    public static String getHtmlBookmarksComment(Settings settings, String text) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's entry remarks field
        StringBuilder retval = new StringBuilder("");
        // first of all, prepare the header and style information of the main content
        retval.append("<style>").append(System.lineSeparator());
        // body-tag with main font settings
        // body-tag with main font settings
        retval.append("body{font-family:");
        retval.append(settings.getRemarksFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getRemarksFont(Settings.FONTSIZE));
        retval.append("px;color:#");
        retval.append(settings.getRemarksFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getRemarksFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getRemarksFont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        // copy css-style to retval...
        retval.append(getHighlightCSS(settings));
        retval.append(".content{padding:3px}").append(System.lineSeparator());
        // css for links, usually only footnotes.
        retval.append("a{color:#003399;text-decoration:none}").append(System.lineSeparator());
        retval.append("</style>");
        // now start with the html content itself
        retval.append("<div class=\"content\">");
        // if we have remarks, replace all return-ubb-tags into html-tags
        if (!text.isEmpty()) {
            // now copy the content of the entry to a dummy string. here we convert
            // the format codes into html-tags. the format codes are simplified tags
            // for the user to enable simple format editing
            String dummy = text.replace("<", "&lt;").replace(">", "&gt;").replace("[br]", "<br>");
            // if parameters in the string array highlight-terms have been passed, we assume that
            // these terms should be highlighted...
            dummy = highlightSearchTerms(dummy, HIGHLIGHT_STYLE_LIVESEARCH);
            dummy = highlightSearchTerms(dummy, HIGHLIGHT_STYLE_LIVESEARCH);
            // autoconvert url's to hyperlinks
            dummy = convertHyperlinks(dummy);
            // after the conversion is done, append the content to the resulting return string
            retval.append(dummy);
        }
        // close all tags properly
        retval.append("</div>").append(System.lineSeparator());

        return retval.toString();
    }

    public static String getLinkedEntriesAsHtml(Daten dataObj, Settings settings, List<String> entries, String resmapstring) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's entry remarks field
        StringBuilder retval = new StringBuilder("");
        // first of all, prepare the header and style information of the main content
        retval.append("<style>").append(System.lineSeparator());
        // body-tag with main font settings
        // body-tag with main font settings
        retval.append("body{font-family:");
        retval.append(settings.getEntryHeaderFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getEntryHeaderFont(Settings.FONTSIZE));
        retval.append("px;color:black;font-style:");
        retval.append(settings.getEntryHeaderFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getEntryHeaderFont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        retval.append("table{border-collapse:collapse;border:none}").append(System.lineSeparator());
        retval.append("td{border:none}").append(System.lineSeparator());
        // css for links, usually only footnotes.
        retval.append("a{color:black;text-decoration:none}").append(System.lineSeparator());
        retval.append("</style>");
        // now start with the html content itself
        retval.append("<table><tr>").append(System.lineSeparator());
        retval.append("<td width=\"1%\" valign=\"top\">");
        retval.append(resourceMap.getString(resmapstring));
        retval.append("</td><td valign=\"top\">");
        if (entries != null && !entries.isEmpty()) {
            StringBuilder entrysb = new StringBuilder("");
            Iterator<String> i = entries.iterator();
            while (i.hasNext()) {
                String entry = i.next();
                try {
                    int entrynr = Integer.parseInt(entry);
                    String title = dataObj.getZettelTitle(entrynr).trim();
                    // check if title has quotes, and if so, escape them
                    title = title.replace("\"", "").replace("'", "");
                    entrysb.append("<a href=\"#cr_").append(entry).append("\" alt=\"").append(title).append("\" title=\"").append(title).append("\">").append(entry).append("</a>").append(" &middot; ");
                } catch (NumberFormatException ex) {
                }
            }
            // append string, but delete last 10 chars, which are " &middot; "
            retval.append(entrysb.toString().substring(0, entrysb.length() - 10));
        }
        // close all tags properly
        retval.append("</td></tr></table>").append(System.lineSeparator());

        return retval.toString();
    }

    /**
     * This method creates a common style-sheet for the basic tags, mainly the
     * different font-settings. since we use this style-definition for several
     * purposes (desktop, main window...), we have "outsourced" this part to
     * this method. it is called from "getEntryAsHTML" and
     * "getHtmlContentForDesktop".
     *
     * @param settings a reference to the CSettings-class
     * @param segmentKeywords a String-Array containing keywords that have been
     * assigned to text segements. These text segements or paragraphs will be
     * highlighted in case a keyword is selected.
     * @param entrykeywords
     * @param isDesktop true, when the style-definition is used for the
     * CDesktop-frame, false otherwise
     * @param isExport true, when the style-definition is used for exporting
     * data, false otherwise
     * @return the style-definitions for the basic-tags in HTML-CSS-Format
     */
    private static String getCommonStyleDefinition(Settings settings, String[] segmentKeywords, String[] entrykeywords, boolean isDesktop, boolean isExport) {
        StringBuilder retval = new StringBuilder(getCommonStyleDefinition(settings, isDesktop, isExport, false));
        if (segmentKeywords != null && segmentKeywords.length > 0) {
            for (String sk : segmentKeywords) {
                sk = sk.replace(" ", "_")
                        .replace(":", "_")
                        .replace("/", "_")
                        .replace("ß", "ss")
                        .replace("ä", "ae")
                        .replace("ö", "oe")
                        .replace("ü", "ue")
                        .replace("Ä", "Ae")
                        .replace("Ö", "Oe")
                        .replace("Ü", "Ue")
                        .replace("\\", "_");

                retval.append(".highlight_").append(sk).append("{");
                retval.append("background-color:#").append(settings.getHighlightBackgroundColor(HIGHLIGHT_STYLE_KEYWORDS)).append("}");
                retval.append(System.lineSeparator());
            }

            if (entrykeywords != null && entrykeywords.length > 0) {
                LinkedList<String> eks = new LinkedList<>();
                eks.addAll(Arrays.asList(entrykeywords));
                for (String sk : segmentKeywords) {
                    eks.remove(sk);
                }

                Iterator<String> eki = eks.iterator();
                while (eki.hasNext()) {
                    String ek = eki.next();
                    ek = ek.replace(" ", "_")
                            .replace(":", "_")
                            .replace("/", "_")
                            .replace("ß", "ss")
                            .replace("ä", "ae")
                            .replace("ö", "oe")
                            .replace("ü", "ue")
                            .replace("Ä", "Ae")
                            .replace("Ö", "Oe")
                            .replace("Ü", "Ue")
                            .replace("\\", "_");

                    retval.append(".highlight_").append(ek).append("{");
                    retval.append("background-color:#ffffff}");
                    retval.append(System.lineSeparator());
                }
            }
        }
        return retval.toString();
    }

    /**
     * This method creates a common style-sheet for the basic tags, mainly the
     * different font-settings. since we use this style-definition for several
     * purposes (desktop, main window...), we have "outsourced" this part to
     * this method. it is called from "getEntryAsHTML" and
     * "getHtmlContentForDesktop".
     *
     * @param settings a reference to the CSettings-class
     * @param isDesktop true, when the style-definition is used for the
     * CDesktop-frame, false otherwise
     * @param isExport true, when the style-definition is used for exporting
     * data, false otherwise
     * @param isPrint
     * @return the style-definitions for the basic-tags in HTML-CSS-Format
     */
    public static String getCommonStyleDefinition(Settings settings, boolean isDesktop, boolean isExport, boolean isPrint) {
        // check whether we have custom css settings
        if (settings != null && settings.getUseCustomCSS((isDesktop) ? Settings.CUSTOM_CSS_DESKTOP : Settings.CUSTOM_CSS_ENTRY)) {
            // retrieve custom style sheet
            String customCss = settings.getCustomCSS((isDesktop) ? Settings.CUSTOM_CSS_DESKTOP : Settings.CUSTOM_CSS_ENTRY);
            // check for valid value
            if (customCss != null && !customCss.isEmpty()) {
                return customCss;
            }
        }
        // constant defining the padding for all content
        final String contentpadding = (PlatformUtil.isMacOS()) ? "padding:8px" : "padding:5px";
        final String halfcontentpadding = (PlatformUtil.isMacOS()) ? "4" : "2";
        // init string builder
        StringBuilder retval = new StringBuilder("");
        // remember, since the desktop has bullets, that these bullets are now
        // the html-tag "h1", so each title/header of this entries moves one level deeper, i,e,
        // h1 becomes h2, h2 becomes h3 and so on...
        String h1 = (isDesktop) ? "h2" : "h1";
        String h2 = (isDesktop) ? "h3" : "h2";
        String h3 = (isDesktop) ? "h4" : "h3";
        String fontunit = (isExport || isPrint) ? "pt" : "px";
        /*
         * CSS for the body
         */
        retval.append("body{font-family:");
        // if we print content, use times / serif fonts
        // body-tag with main font settings
        if (isPrint) {
            retval.append("Times New Roman,serif");
        } else {
            retval.append(settings.getMainfont(Settings.FONTNAME));
        }
        retval.append(";font-size:");
        retval.append(settings.getMainfont(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getMainfont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getMainfont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getMainfont(Settings.FONTWEIGHT));
        // line-height currently not supported by Java
        // retval.append(";line-height:18px");
        retval.append("}").append(System.lineSeparator());
        /*
         * table formatting
         */
        if (settings.getShowTableBorder()) {
            retval.append("table{border-collapse:collapse;border-top:1px solid black;border-bottom:1px solid black;padding:0px}").append(System.lineSeparator());
            retval.append(".rowhead{border-top:1px solid black;border-bottom:1px solid black;background-color:#").append(settings.getTableHeaderColor()).append("}").append(System.lineSeparator());
        } else {
            retval.append("table{border-collapse:collapse;border:none}").append(System.lineSeparator());
            retval.append(".rowhead{background-color:#").append(settings.getTableHeaderColor()).append("}").append(System.lineSeparator());
        }
        retval.append("th{padding:4px;border:none;font-weight:bold;");
        retval.append("background-color:#").append(settings.getTableHeaderColor()).append("}").append(System.lineSeparator());
        retval.append("td{padding:4px;vertical-align:top}").append(System.lineSeparator());
        retval.append(".roweven{background-color:#").append(settings.getTableRowEvenColor()).append("}").append(System.lineSeparator());
        retval.append(".rowodd{background-color:#").append(settings.getTableRowOddColor()).append("}").append(System.lineSeparator());
        retval.append(".tfirstcol{text-align:left}").append(System.lineSeparator());
        retval.append(".tothercol{text-align:center}").append(System.lineSeparator());
        retval.append("caption{font-style:italic}").append(System.lineSeparator());
        /*
         * header tags formatting
         */
        retval.append(h1);
        retval.append("{font-family:");
        retval.append(settings.getTitleFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getTitleFont(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getTitleFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getTitleFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getTitleFont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        // h2-tag with header1 font settings
        retval.append(h2);
        retval.append("{font-family:");
        retval.append(settings.getHeaderfont1(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getHeaderfont1(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getHeaderfont1(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getHeaderfont1(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getHeaderfont1(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        // h3-tag with header2 font settings
        retval.append(h3);
        retval.append("{font-family:");
        retval.append(settings.getHeaderfont2(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getHeaderfont2(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getHeaderfont2(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getHeaderfont2(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getHeaderfont2(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        /*
         * append style-definition for lists
         */
        retval.append("ul{margin-left:10px}").append(System.lineSeparator());
        retval.append("ol{margin-left:10px}").append(System.lineSeparator());
        // append style-definition for lists
        retval.append("li{margin-bottom:");
        // now we have to calculated a prop ortional line-distance. we do this
        // by dividing the font-size by 1.5, which is the margin-distance from
        // each list-point
        int s = Integer.parseInt(settings.getMainfont(Settings.FONTSIZE));
        retval.append(String.valueOf((s / 2))).append(fontunit).append("}").append(System.lineSeparator());
        /*
         * css for links, usually only footnotes.
         */
        retval.append("a{color:#");
        retval.append(settings.getLinkColor());
        retval.append(";text-decoration:none}").append(System.lineSeparator());
        retval.append("a.manlink{color:#");
        retval.append(settings.getManlinkColor());
        retval.append(";text-decoration:none}").append(System.lineSeparator());
        retval.append("a.fnlink{color:#");
        retval.append(settings.getFootnoteLinkColor());
        retval.append(";text-decoration:none}").append(System.lineSeparator());
        /*
         * css for different character formattings
         */
        // make footnotes a bit smaller
        retval.append("sup{font-size:0.9em}").append(System.lineSeparator());
        retval.append("sub{font-size:0.9em}").append(System.lineSeparator());
        // create style for quotes
        if (isExport) {
            retval.append("blockquote");
        } else {
            retval.append(".zitat");
        }
        retval.append("{padding:0.2cm;margin-left:0.2cm;margin-right:0.2cm;background-color:#").append(settings.getQuoteBackgroundColor()).append(";");
        retval.append("font-family:");
        retval.append(settings.getQuoteFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getQuoteFont(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getQuoteFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
        // create style for code
        retval.append("code, pre");
        retval.append("{font-family:");
        retval.append(settings.getCodeFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getCodeFont(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getCodeFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
        /*
         * css style for reference list fonts
         */
        retval.append(".reflist{font-family:");
        retval.append(settings.getAuthorFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getAuthorFont(Settings.FONTSIZE));
        retval.append(fontunit).append(";color:#");
        retval.append(settings.getAuthorFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getAuthorFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getAuthorFont(Settings.FONTWEIGHT));
        retval.append(";margin-bottom:");
        // now we have to calculated a proportional line-distance. we do this
        // by dividing the font-size by 1.5, which is the margin-distance from
        // each list-point
        s = Integer.parseInt(settings.getAuthorFont(Settings.FONTSIZE));
        float f = (float) (s / 2);
        retval.append(String.valueOf((int) Math.ceil(f))).append("px}").append(System.lineSeparator());
        /*
         * css style for search term highlighting
         */
        retval.append(getHighlightCSS(settings));
        /**
         * *************************************************************
         * css style desktop display
         **************************************************************
         */
        if (isDesktop) {
            retval.append("table.maintable{border-collapse:collapse;border:none}").append(System.lineSeparator());
            retval.append(".content{").append(contentpadding).append("}").append(System.lineSeparator());
            // desktop bullet settings
            retval.append("h1{font-family:");
            retval.append(settings.getDesktopHeaderfont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getDesktopHeaderfont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getDesktopHeaderfont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getDesktopHeaderfont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getDesktopHeaderfont(Settings.FONTWEIGHT));
            retval.append("}").append(System.lineSeparator());
            // paragraph settings
            retval.append("p{margin-bottom:6px;margin-top:3px}").append(System.lineSeparator());
            // desktop-comment settings
            retval.append(".comment{padding:5px;background-color:#f0f0f0;");
            if (!isExport) {
                retval.append("padding-top:25px;");
            }
            retval.append("font-family:");
            retval.append(settings.getDesktopCommentfont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getDesktopCommentfont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getDesktopCommentfont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getDesktopCommentfont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getDesktopCommentfont(Settings.FONTWEIGHT));
            retval.append("}").append(System.lineSeparator());
            // desktop header font for the display items
            retval.append(".items{font-family:");
            retval.append(settings.getDesktopItemHeaderfont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getDesktopItemHeaderfont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getDesktopItemHeaderfont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getDesktopItemHeaderfont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getDesktopItemHeaderfont(Settings.FONTWEIGHT));
            retval.append("}").append(System.lineSeparator());
            // desktop font for the display items text
            retval.append(".itemfont{font-family:");
            retval.append(settings.getDesktopItemfont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getDesktopItemfont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getDesktopItemfont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getDesktopItemfont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getDesktopItemfont(Settings.FONTWEIGHT));
            retval.append(";margin-top:3px;margin-bottom:3px");
            retval.append("}").append(System.lineSeparator());
        } /**
         * *************************************************************
         * css style entry display
         **************************************************************
         */
        else {
            /*
             * css style for rating-area
             */
            retval.append(".entryrating{background-color:#").append(settings.getEntryHeadingBackgroundColor()).append(";");
            retval.append(contentpadding).append(";font-family:");
            retval.append(settings.getEntryHeaderFont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getEntryHeaderFont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getEntryHeaderFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
            retval.append(".elink, .tslink, .rlink {color:#");
            retval.append(settings.getEntryHeaderFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
            retval.append(".crtitle{margin:0px;padding:0px;width:1%;vertical-align:top;padding-top:").append(halfcontentpadding).append("px}").append(System.lineSeparator());
            retval.append(".crtitle a{color:#");
            retval.append(settings.getEntryHeaderFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
            retval.append(".mlink {margin:0px;padding:0px;padding-top:").append(halfcontentpadding).append("px}").append(System.lineSeparator());
            retval.append(".mlink a{color:#");
            retval.append(settings.getEntryHeaderFont(Settings.FONTCOLOR)).append("}").append(System.lineSeparator());
            retval.append(".tabentryrating{border-collapse:collapse;border:none;margin:0px;padding:0px;width:100%}").append(System.lineSeparator());
            retval.append(".leftcellentryrating{margin:0px;padding:0px;width:30%}").append(System.lineSeparator());
            retval.append(".midcellentryrating{margin:0px;padding:0px;width:45%}").append(System.lineSeparator());
            retval.append(".rightcellentryrating{margin:0px;padding:0px;width:25%}").append(System.lineSeparator());
            retval.append(".content{background-color:#");
            retval.append(settings.getContentBackgroundColor()).append(";");
            retval.append(contentpadding).append(";padding-bottom:20px}").append(System.lineSeparator());
            /*
             * css style for appendix
             */
            retval.append(".appendixcontent{background-color:#");
            retval.append(settings.getAppendixBackgroundColor()).append(";");
            retval.append(contentpadding).append("}").append(System.lineSeparator());
            // appendix header
            retval.append(".appendixcontent h1");
            retval.append("{font-family:");
            retval.append(settings.getAppendixHeaderFont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getAppendixHeaderFont(Settings.FONTSIZE));
            retval.append(fontunit).append(";color:#");
            retval.append(settings.getAppendixHeaderFont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getAppendixHeaderFont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getAppendixHeaderFont(Settings.FONTWEIGHT));
            retval.append(";margin:0;padding:0}").append(System.lineSeparator());
            // h1-tag with title font settings
            retval.append(".attachments ul{color:#");
            retval.append(settings.getLinkColor());
            retval.append(";font-size:9").append(fontunit).append(";margin-left:10px}").append(System.lineSeparator());
            retval.append(".attachments ul a{color:#");
            retval.append(settings.getLinkColor());
            retval.append(";text-decoration:none}").append(System.lineSeparator());
            /*
             * css style for remarks-area in appendix
             */
            retval.append(".remarks{font-family:");
            retval.append(settings.getRemarksFont(Settings.FONTNAME));
            retval.append(";font-size:");
            retval.append(settings.getRemarksFont(Settings.FONTSIZE));
            retval.append("px;color:#");
            retval.append(settings.getRemarksFont(Settings.FONTCOLOR));
            retval.append(";font-style:");
            retval.append(settings.getRemarksFont(Settings.FONTSTYLE));
            retval.append(";font-weight:");
            retval.append(settings.getRemarksFont(Settings.FONTWEIGHT));
            retval.append(";margin-top:");
            s = Integer.parseInt(settings.getRemarksFont(Settings.FONTSIZE));
            f = (float) (s / 2);
            retval.append(String.valueOf((int) Math.ceil(f))).append("px}").append(System.lineSeparator());
        }
        return retval.toString();
    }

    /**
     * This method return the html-header, i.e. the style-definitions, for the
     * desktop- window. in contrary to the main window, where each entry gets
     * its own header, because the html-page in the editorpane consists of just
     * one entry, we have many entries in one editorpane in the desktop-window -
     * but we need the style-definition only once. Thus, this method is
     * seperated from the other method, which creates the html-content of an
     * entry (see "getHtmlContentForDesktop" for further details).
     *
     * @param settings
     * @param print
     * @return The style definition for the html-page from the desktop window
     */
    public static String getHtmlHeaderForDesktop(Settings settings, boolean print) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's "entry textfield"
        StringBuilder retval = new StringBuilder("");
        // first of all, prepare the header and style information of the main content
        retval.append("<style>").append(System.lineSeparator());
        // get the common style definition for the basic-tags
        retval.append(getCommonStyleDefinition(settings, true, false, print));
        retval.append("</style>");

        return retval.toString();
    }

    /**
     * This method return the html-header, i.e. the style-definitions, for the
     * export- html-page. in contrary to the main window, where each entry gets
     * its own header, because the html-page in the editorpane consists of just
     * one entry, we have many entries in one editorpane in the desktop-window -
     * but we need the style-definition only once. Thus, this method is
     * seperated from the other method, which creates the html-content of an
     * entry.
     *
     * @param settings
     * @return The style definition for the html-page from the desktop window
     */
    public static String getHtmlHeaderForExport(Settings settings) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's "entry textfield"
        StringBuilder retval = new StringBuilder("");
        // first of all, prepare the header and style information of the main content
        retval.append("<style>").append(System.lineSeparator());
        // get the common style definition for the basic-tags
        retval.append(getCommonStyleDefinition(settings, false, true, false));
        // body-tag with main font settings
        retval.append(".deskhead{font-family:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTSIZE));
        retval.append("pt;color:#");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        retval.append(".author{font-family:");
        retval.append(settings.getAuthorFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getAuthorFont(Settings.FONTSIZE));
        retval.append("pt;color:#");
        retval.append(settings.getAuthorFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getAuthorFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getAuthorFont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        retval.append("</style><div class=\"content\">");

        return retval.toString();
    }

    /**
     * This method return the html-header, i.e. the style-definitions, for the
     * export- html-page. in contrary to the main window, where each entry gets
     * its own header, because the html-page in the editorpane consists of just
     * one entry, we have many entries in one editorpane in the desktop-window -
     * but we need the style-definition only once. Thus, this method is
     * seperated from the other method, which creates the html-content of an
     * entry.
     * <br><br>
     * Since we use a different export-approach when exporting the desktop-data
     * (we pass a complete html-page as parameter), we need to add html, head,
     * and body-tags within this method here!
     *
     * @param settings
     * @return The style definition for the html-page from the desktop window
     */
    public static String getHtmlHeaderForDesktopExport(Settings settings) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the main window's "entry textfield"
        StringBuilder retval = new StringBuilder("");
        // first of all, prepare the header and style information of the main content
        retval.append("<html><head><style>").append(System.lineSeparator());
        // get the common style definition for the basic-tags
        retval.append(getCommonStyleDefinition(settings, true, true, false));
        // body-tag with main font settings
        retval.append(".tocheader1 {margin-left:0.5em;}").append(System.lineSeparator());
        retval.append(".tocentry1 {margin-left:1em;}").append(System.lineSeparator());
        retval.append(".tocheader2 {margin-left:1.5em;}").append(System.lineSeparator());
        retval.append(".tocentry2 {margin-left:2em;}").append(System.lineSeparator());
        retval.append(".tocheader3 {margin-left:2.5em;}").append(System.lineSeparator());
        retval.append(".tocentry3 {margin-left:3em;}").append(System.lineSeparator());
        retval.append(".tocheader4 {margin-left:3.5em;}").append(System.lineSeparator());
        retval.append(".tocentry4 {margin-left:4em;}").append(System.lineSeparator());
        retval.append(".tocheader5 {margin-left:4.5em;}").append(System.lineSeparator());
        retval.append(".tocentry5 {margin-left:5em;}").append(System.lineSeparator());
        retval.append(".deskhead{font-family:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTSIZE));
        retval.append("pt;color:#");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getDesktopHeaderfont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        retval.append(".author{font-family:");
        retval.append(settings.getAuthorFont(Settings.FONTNAME));
        retval.append(";font-size:");
        retval.append(settings.getAuthorFont(Settings.FONTSIZE));
        retval.append("pt;color:#");
        retval.append(settings.getAuthorFont(Settings.FONTCOLOR));
        retval.append(";font-style:");
        retval.append(settings.getAuthorFont(Settings.FONTSTYLE));
        retval.append(";font-weight:");
        retval.append(settings.getAuthorFont(Settings.FONTWEIGHT));
        retval.append("}").append(System.lineSeparator());
        retval.append("</style></head><body><div class=\"content\">");

        return retval.toString();
    }

    /**
     * This method creates a html page of the parameters passed to this class
     * constructor It is easier to keep the overview over the layout style when
     * the html page, which is responsible for the "look'n'feel" of an entry, is
     * being created in a separate class rather than in the CDaten class.
     * <br><br>
     * This method creates the html-content of entries for the desktop window.
     *
     * @param dataObj
     * @param bibtexObj
     * @param settings
     * @param nr
     * @param isExport
     * @param isHeadingVisible
     * @param isEntryNumberVisible
     * @param createHtmlFootnotes
     * @return a string with the html-page-content
     */
    public static String getHtmlContentForDesktop(Daten dataObj, BibTeX bibtexObj, Settings settings, int nr, boolean isHeadingVisible, boolean isEntryNumberVisible, boolean isExport, boolean createHtmlFootnotes) {
        // get the zettelcontent
        return getHtmlContentForDesktop(dataObj,
                bibtexObj,
                settings,
                dataObj.getZettelContent(nr),
                nr,
                isHeadingVisible,
                isEntryNumberVisible,
                isExport,
                createHtmlFootnotes);
                                       // and replace header-tags, since they change
        // order when they are put on the desktop
        // .replace("h3","h4").replace("h2","h3");

    }

    /**
     *
     * @param dataObj
     * @param nr
     * @param isEntryNumberVisible
     * @return
     */
    public static String getZettelTitleForDesktop(Daten dataObj, int nr, boolean isEntryNumberVisible) {
        StringBuilder retval = new StringBuilder("");
        // first check whether we have a title or not
        String zetteltitle = dataObj.getZettelTitle(nr).replace("<", "&lt;").replace(">", "&gt;");
        // display title. remember, since the desktop has bullets, that these bullets are now
        // the html-tag "h1", so each title/header of this entries moves one level deeper, i,e,
        // h1 becomes h2, h2 becomes h3 and so on...
        //
        // each entry has at least the title "entry" plus its number.
        // we need this to search through the entries when the user clicks an entry on the
        // jTreeDesktop in the desktop-window
        // start open-tag for heading 2. level
        retval.append("<p><strong>");
        // if either entry number should be visible, or if no entry title available
        // the entry-number should be displayed
        if (isEntryNumberVisible || zetteltitle.isEmpty()) {
            // retrieve locale entry-text
            retval.append(resourceMap.getString("entryText"));
            retval.append(" ");
            // and add entry-number
            retval.append(String.valueOf(nr));
        }
        // if we have a "real" title, append it...
        if (!zetteltitle.isEmpty()) {
            // don't use a colon, if entry-number should not be visible
            if (isEntryNumberVisible) {
                retval.append(": ");
            }
            // append title
            retval.append(zetteltitle);
        }
        retval.append("</strong></p>").append(System.lineSeparator());
        return retval.toString();
    }

    /**
     * This method creates a html page of the parameters passed to this class
     * constructor It is easier to keep the overview over the layout style when
     * the html page, which is responsible for the "look'n'feel" of an entry, is
     * being created in a separate class rather than in the CDaten class.
     * <br><br>
     * This method creates the html-content of entries for the desktop window.
     *
     * @param dataObj
     * @param bibtexObj
     * @param settings
     * @param isHeadingVisible
     * @param nr
     * @param zettelcontent
     * @param isEntryNumberVisible
     * @param isExport
     * @param createHtmlFootnotes
     * @return a string with the html-page-content
     */
    public static String getHtmlContentForDesktop(Daten dataObj, BibTeX bibtexObj, Settings settings, String zettelcontent, int nr, boolean isHeadingVisible, boolean isEntryNumberVisible, boolean isExport, boolean createHtmlFootnotes) {
        // create an empty string buffer. this buffer contains the html-string
        // which is being display in the desktop window's main textfield
        StringBuilder retval = new StringBuilder("");
        // create entry title
        if (isHeadingVisible) {
            retval.append(getZettelTitleForDesktop(dataObj, nr, isEntryNumberVisible));
        }
        // if we have content, convert ubb-tags.
        if (!zettelcontent.isEmpty()) {
            // now copy the content of the entry to a dummy string. here we convert
            // the format codes into html-tags. the format codes are simplified tags
            // for the user to enable simple format editing
            String dummy = convertUbbToHtml(settings, dataObj, bibtexObj, zettelcontent, Constants.FRAME_DESKTOP, isExport, createHtmlFootnotes);
            // after the conversion is done, append the content to the resulting return string
            retval.append("<p>").append(dummy).append("</p>");
        } else {
            retval.append("<p><i>").append(resourceMap.getString("deletedEntry")).append("</i></p>");
        }
        //
        // here we setup the remarks
        //
        // if the user wants to display authors, display them now...
        if ((settings.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_REMARKS) != 0) {
            // get entries remarks
            String rem = dataObj.getRemarks(nr);
            // if the entry has remarks, add them to the content
            if (!rem.isEmpty()) {
                // set title
                retval.append(System.lineSeparator()).append("<p class=\"items\">");
                retval.append(resourceMap.getString("remarksText"));
                retval.append("</p>").append(System.lineSeparator()).append("<p class=\"itemfont\">");
                // now copy the content of the entry to a dummy string. here we convert
                // the format codes into html-tags. the format codes are simplified tags
                // for the user to enable simple format editing
                rem = rem.replace("<", "&lt;").replace(">", "&gt;").replace("[br]", "<br>");
                // after the conversion is done, append the content to the resulting return string
                retval.append(rem);
                retval.append("</p>").append(System.lineSeparator());
            }
        }
        //
        // here we setup the authors
        //
        // if the user wants to display authors, display them now...
        if ((settings.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_AUTHORS) != 0) {
            // get entry's authors
            String[] zettelauthors = dataObj.getAuthors(nr);
            // if there is no author information, tell this the user
            if ((zettelauthors != null) && (zettelauthors.length > 0)) {
                // set title
                retval.append(System.lineSeparator()).append("<p class=\"items\">");
                retval.append(resourceMap.getString("authorsText"));
                retval.append("</p>");
                // iterate the author array
                for (String aus : zettelauthors) {
                    // autoconvert url's to hyperlinks
                    aus = convertHyperlinks(aus.replace("<", "&lt;").replace(">", "&gt;"));
                    // and append each author
                    retval.append(System.lineSeparator()).append("<p class=\"itemfont\">").append(aus).append("</p>");
                }
                retval.append(System.lineSeparator());
            }
        }
        //
        // here we setup the attachments
        //
        // if the user wants to display authors, display them now...
        if ((settings.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_ATTACHMENTS) != 0) {
            // get entry's attachments
            List<Element> links = dataObj.getAttachments(nr);
            // if there is no author information, tell this the user
            if ((links != null) && (!links.isEmpty())) {
                // set title
                retval.append(System.lineSeparator()).append("<p class=\"items\">");
                retval.append(resourceMap.getString("attachmentsText"));
                retval.append("</p>").append(System.lineSeparator()).append("<p class=\"itemfont\">");
                // create iterator
                Iterator<Element> ie = links.iterator();
                // iterate the list
                while (ie.hasNext()) {
                    Element e = ie.next();
                    // and append each attachment
                    retval.append("<a href=\"").append(e.getText()).append("\">").append(e.getText()).append("</a><br>").append(System.lineSeparator());
                }
                retval.append("</p>").append(System.lineSeparator());
            }
        }
        //
        // here we setup the keywords
        //
        // if the user wants to display authors, display them now...
        if ((settings.getDesktopDisplayItems() & Constants.DESKTOP_SHOW_KEYWORDS) != 0) {
            // get entry's keywords
            String[] kws = dataObj.getKeywords(nr);
            // if there is no author information, tell this the user
            if ((kws != null) && (kws.length > 0)) {
                // set title
                retval.append(System.lineSeparator()).append("<p class=\"items\">");
                retval.append(resourceMap.getString("keywordsText"));
                retval.append("</p>").append(System.lineSeparator()).append("<p class=\"itemfont\">");
                // sort array
                Arrays.sort(kws, new Comparer());
                // iterate the string arryy
                // and append each keyword
                for (String k : kws) {
                    retval.append(k).append(", ");
                }
                // truncate last comma and space
                retval.setLength(retval.length() - 2);
                // close tag
                retval.append("</p>").append(System.lineSeparator());
            }
        }
        // return finished entry
        return retval.toString();
    }
}
