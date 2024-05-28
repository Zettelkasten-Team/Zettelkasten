package de.danielluedecke.zettelkasten.ui;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import bsh.org.objectweb.asm.Constants;
import de.danielluedecke.zettelkasten.database.Daten;

public class GetEntryHeadlineTest {

    private Daten dataObj;
    private static final int ENTRY_NUMBER = 1;
    private static final int SOURCE_FRAME = Constants.FRAME_MAIN; // Ensure this is correctly defined
    // FRAME_MAIN = 0;    // Define the actual value for FRAME_MAIN
    // FRAME_SEARCH = 1;  // Define the actual value for FRAME_SEARCH

    @BeforeClass
    public void setUp() {
        // Initialize the dataObj with mock data
        dataObj = new Daten(null);
        dataObj.addEntry(ENTRY_NUMBER, "Test Title", "This is the test content.", 4.5f, new String[]{"2", "3"});
        dataObj.setActivatedEntryNumber(1);
    }

    @Test
    public void testGetEntryHeadline_NotActivatedEntry() {
        String htmlSnippet = getEntryHeadline(dataObj, ENTRY_NUMBER + 1, SOURCE_FRAME);
        Assert.assertNotNull(htmlSnippet);
        Assert.assertTrue(htmlSnippet.contains("Test Title"));
        Assert.assertTrue(htmlSnippet.contains("This is the test content."));
        Assert.assertTrue(htmlSnippet.contains("4.5"));
    }

    @Test
    public void testGetEntryHeadline_ActivatedEntry() {
        String htmlSnippet = getEntryHeadline(dataObj, ENTRY_NUMBER, SOURCE_FRAME);
        Assert.assertNotNull(htmlSnippet);
        Assert.assertTrue(htmlSnippet.contains("Test Title"));
        Assert.assertTrue(htmlSnippet.contains("This is the test content."));
        Assert.assertTrue(htmlSnippet.contains("4.5"));
    }

    @Test
    public void testGetEntryHeadline_WordCount() {
        String htmlSnippet = getEntryHeadline(dataObj, ENTRY_NUMBER, SOURCE_FRAME);
        int expectedWordCount = "Test Title This is the test content.".split("\\W+").length;
        Assert.assertTrue(htmlSnippet.contains("(" + expectedWordCount + " "));
    }

    @Test
    public void testGetEntryHeadline_ManualLinks() {
        String htmlSnippet = getEntryHeadline(dataObj, ENTRY_NUMBER, SOURCE_FRAME);
        Assert.assertTrue(htmlSnippet.contains("<a href=\"#cr_2\""));
        Assert.assertTrue(htmlSnippet.contains("<a href=\"#cr_3\""));
    }

    @Test
    public void testGetEntryHeadline_RatingImages() {
        String htmlSnippet = getEntryHeadline(dataObj, ENTRY_NUMBER, SOURCE_FRAME);
        int fullStarCount = 4;
        int halfStarCount = 1;
        Assert.assertEquals(countOccurrences(htmlSnippet, "full_star_image"), fullStarCount);
        Assert.assertEquals(countOccurrences(htmlSnippet, "half_star_image"), halfStarCount);
    }

    private int countOccurrences(String str, String subStr) {
        return str.split(subStr, -1).length - 1;
    }
}
