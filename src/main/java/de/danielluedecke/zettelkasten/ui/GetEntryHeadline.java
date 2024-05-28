package de.danielluedecke.zettelkasten.ui;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.PlatformUtil;

/**
 * This method creates an HTML-layer ({@code div}-tag) which contains the
 * graphical elements for the rating of an entry. This method returns an
 * HTML-snippet as a string which can be inserted anywhere inside a
 * {@code body}-element.
 *
 * @param dataObj a reference to the {@code CDaten}-class, needed for
 * accessing the methods that retrieve the rating-values from entries.
 * @param entrynr the entry-number of the requested entry which rating
 * should be created.
 * @param sourceframe a reference to the frame from where this function call
 * came. needed for the html-formatting, since entries are differently
 * formatted in the search window.
 * @return a HTML-snippet as string which can be inserted anywhere inside a
 * {@code body}-element.
 */
public class GetEntryHeadline {

    public static String getEntryHeadline(Daten dataObj, int entrynr, int sourceframe) {
        float ratingValue = dataObj.getZettelRating(entrynr);
        StringBuilder htmlRating = new StringBuilder();
        int wordCount = calculateWordCount(dataObj, entrynr);

        htmlRating.append(System.lineSeparator()).append("<div class=\"entryrating\">")
                  .append("<table ").append(getTableAttributes()).append("class=\"tabentryrating\"><tr>")
                  .append(System.lineSeparator())
                  .append("<td colspan=\"2\" class=\"leftcellentryrating\">")
                  .append(getEntryHeading(dataObj, entrynr, sourceframe, wordCount))
                  .append("</td><td class=\"midcellentryrating\">")
                  .append(getEntryTimestamp(dataObj, entrynr))
                  .append("</td><td class=\"rightcellentryrating\">")
                  .append(getRatingHtml(entrynr, ratingValue))
                  .append("</td></tr>").append(System.lineSeparator())
                  .append(getManualLinksHtml(dataObj, entrynr))
                  .append("</table></div>").append(System.lineSeparator());

        return htmlRating.toString();
    }

    private static String getTableAttributes() {
        if (PlatformUtil.isJava7OnMac() || PlatformUtil.isJava7OnWindows()) {
            return "cellspacing=\"0\" ";
        }
        return "";
    }

    private static String getEntryHeading(Daten dataObj, int entrynr, int sourceframe, int wordCount) {
        StringBuilder heading = new StringBuilder();
        heading.append(resourceMap.getString("zettelDesc")).append(" ");

        if (entrynr != dataObj.getActivatedEntryNumber() && sourceframe != Constants.FRAME_SEARCH) {
            heading.append("<a class=\"elink\" href=\"#activatedEntry\">")
                   .append(" ").append(dataObj.getActivatedEntryNumber()).append("&nbsp;</a>&raquo;&nbsp;")
                   .append("<a class=\"elink\" href=\"#cr_").append(entrynr).append("\">")
                   .append(entrynr).append("&nbsp;</a>(").append(wordCount).append(" ")
                   .append(resourceMap.getString("activatedZettelWordCount")).append(")");
        } else {
            heading.append(entrynr).append(" (").append(wordCount).append(" ")
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
                title = dataObj.getZettelTitle(Integer.parseInt(ml)).replace("\"", "").replace("'", "").trim();
            } catch (NumberFormatException e) {
                // Handle error if necessary
            }
            linksHtml.append("<a href=\"#cr_").append(ml).append("\" title=\"").append(title).append("\" alt=\"")
                     .append(title).append("\">").append(ml).append("</a> &middot; ");
        }

        return linksHtml.substring(0, linksHtml.length() - 10) + "</td></tr>" + System.lineSeparator();
    }

    // Mock implementations for demonstration. Replace with actual implementations.
    private static String getRatingSymbol(int ratingValue) {
        switch (ratingValue) {
            case RATING_VALUE_FULL:
                return "★";  // Full star symbol
            case RATING_VALUE_HALF:
                return "☆";  // Half star symbol
            case RATING_VALUE_NONE:
            default:
                return "✩";  // Empty star symbol
        }
    }

    private static String getEntryTimestamp(Daten dataObj, int entrynr) {
        // Mock implementation for demonstration. Replace with actual implementation.
        return "2024-05-28 12:34:56";
    }
}
