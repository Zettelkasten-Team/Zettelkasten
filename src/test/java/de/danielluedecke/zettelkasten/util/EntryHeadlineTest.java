package de.danielluedecke.zettelkasten.util;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;

public class EntryHeadlineTest {

    private static final String RATING_VALUE_FULL = null;
	private Daten mockDataObj;

    @BeforeMethod
    public void setUp() {
        // Set up a mock data object with necessary methods stubbed
        mockDataObj = Mockito.mock(Daten.class);
        Mockito.when(mockDataObj.getZettelTitle(Mockito.anyInt())).thenReturn("Sample Title");
        Mockito.when(mockDataObj.getCleanZettelContent(Mockito.anyInt())).thenReturn("Sample content with several words.");
        Mockito.when(mockDataObj.getZettelRating(Mockito.anyInt())).thenReturn(3.5f);
        Mockito.when(mockDataObj.getActivatedEntryNumber()).thenReturn(1);
        Mockito.when(Daten.getManualLinksAsString(Mockito.anyInt())).thenReturn(new String[]{"2", "3"});
    }

    @Test
    public void testCalculateWordCount() {
        int wordCount = calculateWordCount(mockDataObj, 1);
        Assert.assertEquals(wordCount, 5, "Word count should be 5");
    }

    @Test
    public void testAppendInitialHtml() {
        StringBuilder htmlRating = new StringBuilder();
        appendInitialHtml(htmlRating);
        Assert.assertTrue(htmlRating.toString().contains("<div class=\"entryrating\">"), "Initial HTML should contain the div tag");
    }

    @Test
    public void testAppendEntryHeading() {
        StringBuilder htmlRating = new StringBuilder();
        appendEntryHeading(htmlRating, mockDataObj, 1, 0, 5);
        String result = htmlRating.toString();
        Assert.assertTrue(result.contains("<td colspan=\"2\" class=\"leftcellentryrating\">"), "Entry heading should contain the correct td tag");
        Assert.assertTrue(result.contains("Sample Title"), "Entry heading should contain the sample title");
        Assert.assertTrue(result.contains("5 words"), "Entry heading should contain the word count");
    }

    @Test
    public void testAppendEntryTimestamp() {
        StringBuilder htmlRating = new StringBuilder();
        appendEntryTimestamp(htmlRating, mockDataObj, 1);
        Assert.assertTrue(htmlRating.toString().contains("timestamp"), "Entry timestamp should be appended correctly");
    }

    @Test
    public void testAppendRatingHtml() {
        StringBuilder htmlRating = new StringBuilder();
        appendRatingHtml(htmlRating, 1, 3.5f);
        String result = htmlRating.toString();
        Assert.assertTrue(result.contains("<a class=\"rlink\" href=\"#rateentry1\">"), "Rating HTML should contain the correct link");
        Assert.assertTrue(result.contains(getRatingSymbol(RATING_VALUE_FULL)), "Rating HTML should contain the full star rating symbol");
        Assert.assertTrue(result.contains(getRatingSymbol(RATING_VALUE_HALF)), "Rating HTML should contain the half star rating symbol");
        Assert.assertTrue(result.contains(getRatingSymbol(RATING_VALUE_NONE)), "Rating HTML should contain the no star rating symbol");
    }

    @Test
    public void testAppendManualLinks() {
        StringBuilder htmlRating = new StringBuilder();
        appendManualLinks(htmlRating, mockDataObj, 1);
        String result = htmlRating.toString();
        Assert.assertTrue(result.contains("<tr><td class=\"crtitle\" valign=\"top\"><a href=\"#crt\">"), "Manual links should be appended correctly");
        Assert.assertTrue(result.contains("2"), "Manual link should contain entry number 2");
        Assert.assertTrue(result.contains("3"), "Manual link should contain entry number 3");
    }

    @Test
    public void testGetEntryHeadline() {
        String htmlSnippet = getEntryHeadline(mockDataObj, 1, 0);
        Assert.assertTrue(htmlSnippet.contains("<div class=\"entryrating\">"), "HTML snippet should contain the entry rating div");
        Assert.assertTrue(htmlSnippet.contains("Sample Title"), "HTML snippet should contain the sample title");
        Assert.assertTrue(htmlSnippet.contains("5 words"), "HTML snippet should contain the word count");
        Assert.assertTrue(htmlSnippet.contains("timestamp"), "HTML snippet should contain the timestamp");
        Assert.assertTrue(htmlSnippet.contains("<a class=\"rlink\" href=\"#rateentry1\">"), "HTML snippet should contain the rating link");
        Assert.assertTrue(htmlSnippet.contains("<tr><td class=\"crtitle\" valign=\"top\"><a href=\"#crt\">"), "HTML snippet should contain manual links");
    }

    private String getEntryHeadline(Daten mockDataObj2, int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	// The helper methods being tested (as in the refactored code)
    private static int calculateWordCount(Daten dataObj, int entrynr) {
        String content = dataObj.getZettelTitle(entrynr) + " " + dataObj.getCleanZettelContent(entrynr);
        String[] words = content.toLowerCase()
                                .replace("ä", "ae")
                                .replace("ö", "oe")
                                .replace("ü", "ue")
                                .replace("ß", "ss")
                                .split("\\W+");
        int wordCount = 0;
        for (String word : words) {
            word = word.replaceAll("[^A-Za-z0-9]+", "").trim();
            if (!word.isEmpty()) {
                wordCount++;
            }
        }
        return wordCount;
    }

    private static void appendInitialHtml(StringBuilder htmlRating) {
        htmlRating.append(System.lineSeparator()).append("<div class=\"entryrating\">")
                  .append("<table ")
                  .append(PlatformUtil.isJava7OnMac() || PlatformUtil.isJava7OnWindows() ? "cellspacing=\"0\" " : "")
                  .append("class=\"tabentryrating\"><tr>")
                  .append(System.lineSeparator());
    }

    private static void appendEntryHeading(StringBuilder htmlRating, Daten dataObj, int entrynr, int sourceframe, int wordCount) {
        htmlRating.append("<td colspan=\"2\" class=\"leftcellentryrating\">")
                  .append(resourceMap.getString("zettelDesc")).append(" ");
        if (entrynr != dataObj.getActivatedEntryNumber() && sourceframe != Constants.FRAME_SEARCH) {
            htmlRating.append("<a class=\"elink\" href=\"#activatedEntry\">")
                      .append(dataObj.getActivatedEntryNumber())
                      .append("&nbsp;</a>&raquo;&nbsp;<a class=\"elink\" href=\"#cr_")
                      .append(entrynr).append("\">")
                      .append(entrynr)
                      .append("&nbsp;</a>(")
                      .append(wordCount)
                      .append(" ")
                      .append(resourceMap.getString("activatedZettelWordCount"))
                      .append(")");
        } else {
            htmlRating.append(entrynr)
                      .append(" (")
                      .append(wordCount)
                      .append(" ")
                      .append(resourceMap.getString("activatedZettelWordCount"))
                      .append(")");
        }
        htmlRating.append("</td>");
    }

    private static void appendEntryTimestamp(StringBuilder htmlRating, Daten dataObj, int entrynr) {
        htmlRating.append("<td class=\"midcellentryrating\">")
                  .append(getEntryTimestamp(dataObj, entrynr))
                  .append("</td>");
    }

    private static void appendRatingHtml(StringBuilder htmlRating, int entrynr, float ratingValue) {
        htmlRating.append("<td class=\"rightcellentryrating\">")
                  .append("<a class=\"rlink\" href=\"#rateentry").append(entrynr).append("\">")
                  .append(resourceMap.getString("ratingText")).append(": ");
        for (int cnt = 5; cnt > 0; cnt--) {
            if (ratingValue >= 1.0) {
                htmlRating.append(getRatingSymbol(RATING_VALUE_FULL));
            } else if (ratingValue >= 0.5) {
                htmlRating.append(getRatingSymbol(RATING_VALUE_HALF));
            } else {
                htmlRating.append(getRatingSymbol(RATING_VALUE_NONE));
            }
            ratingValue--;
        }
        htmlRating.append("</a>")
                  .append("</td></tr>")
                  .append(System.lineSeparator());
    }

    private static void appendManualLinks(StringBuilder htmlRating, Daten dataObj, int entrynr) {
        String[] manualLinks = Daten.getManualLinksAsString(entrynr);
        if (manualLinks != null && manualLinks.length > 0) {
            htmlRating.append("<tr><td class=\"crtitle\" valign=\"top\"><a href=\"#crt\">")
                      .append(resourceMap.getString("crossRefText"))
                      .append(":</a>&nbsp;</td><td class=\"mlink\" colspan=\"3\">");

            StringBuilder crossRefs = new StringBuilder();
            for (String ml : manualLinks) {
                String title = "";
                try {
                    title = dataObj.getZettelTitle(Integer.parseInt(ml)).replace("\"", "").replace("'", "").trim();
                } catch (NumberFormatException e) {
                    // Log error or handle appropriately
                }
                crossRefs.append("<a href=\"#cr_")
                         .append(ml)
                         .append("\" title=\"")
                         .append(title)
                         .append("\" alt=\"")
                         .append(title)
                         .append("\">")
                         .append(ml)
                         .append("</a> &middot; ");
            }
            htmlRating.append(crossRefs.substring(0, crossRefs.length() - 10))
                      .append("</td></tr>")
                      .append(System.lineSeparator());
        }
    }
}
