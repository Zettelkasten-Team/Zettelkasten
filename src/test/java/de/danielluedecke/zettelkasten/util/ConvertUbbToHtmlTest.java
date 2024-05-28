package de.danielluedecke.zettelkasten.util;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;

public class ConvertUbbToHtmlTest {

	private Settings settings;
    private Daten dataObj;
    private BibTeX bibtexObj;
    
    @BeforeMethod
    public void setUp() {
        settings = new Settings();
        dataObj = Mockito.mock(Daten.class); // Mocking Daten object
        bibtexObj = new BibTeX(null, settings);
        
        // Initialize settings and data objects with necessary data
        settings.setMarkdownActivated(true);
        
        // Mock behavior for dataObj.addEntry
        Mockito.when(dataObj.addEntry(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(String[].class),
            Mockito.any(String[].class),
            Mockito.anyString(),
            Mockito.any(String[].class),
            Mockito.anyString(),
            Mockito.anyInt()
        )).thenReturn(1); // Return 1 for successful addition, you may adjust this based on your needs

        // Mock behavior for dataObj.setZettelTitle
        Mockito.doNothing().when(dataObj).setZettelTitle(
            Mockito.anyInt(),
            Mockito.anyString()
        );

        // Assuming retrieveFirstEmptyEntry might be problematic, mock its behavior
        Mockito.when(dataObj.retrieveFirstEmptyEntry()).thenReturn(1);
    }

    @Test
    public void testBoldItalicConversion() {
        String rawContent = "This is [b]bold[/b] and [i]italic[/i].";
        String expectedHtml = "This is <strong>bold</strong> and <em>italic</em>.";
        String convertedContent = HtmlUbbUtil.convertUbbToHtml(settings, dataObj, bibtexObj, rawContent, Constants.FRAME_MAIN, false, false);
        Assert.assertEquals(convertedContent, expectedHtml, "Bold and Italic tags should be converted correctly.");
    }

    @Test
    public void testUrlConversion() {
        String rawContent = "Visit [url]http://example.com[/url] for more info.";
        String expectedHtml = "Visit <a href=\"http://example.com\">http://example.com</a> for more info.";
        String convertedContent = HtmlUbbUtil.convertUbbToHtml(settings, dataObj, bibtexObj, rawContent, Constants.FRAME_MAIN, false, false);
        Assert.assertEquals(convertedContent, expectedHtml, "URL tags should be converted correctly.");
    }

    @Test
    public void testImageConversion() {
        String rawContent = "Here is an image: [img]image.jpg[/img].";
        String expectedHtml = "Here is an image: <img src=\"image.jpg\" alt=\"\" />.";
        String convertedContent = HtmlUbbUtil.convertUbbToHtml(settings, dataObj, bibtexObj, rawContent, Constants.FRAME_MAIN, false, false);
        Assert.assertEquals(convertedContent, expectedHtml, "Image tags should be converted correctly.");
    }

    @Test
    public void testManualLinks() {
        String rawContent = "See [url=#z_1]this link[/url].";
        String expectedHtml = "See <a href=\"#z_1\" alt=\"Test Title\" title=\"Test Title\">this link</a>.";
        String convertedContent = HtmlUbbUtil.convertUbbToHtml(settings, dataObj, bibtexObj, rawContent, Constants.FRAME_MAIN, false, false);
        Assert.assertEquals(convertedContent, expectedHtml, "Manual links should be converted correctly.");
    }

    @Test
    public void testFootnotes() {
        String rawContent = "Here is a footnote.[fn_1]";
        // Assuming convertFootnotes replaces [fn_1] with <a href="#fn_1">1</a> and adds the title attribute
        String expectedHtml = "Here is a footnote.<a href=\"#fn_1\" title=\"Author Name\">1</a>";
        String convertedContent = HtmlUbbUtil.convertUbbToHtml(settings, dataObj, bibtexObj, rawContent, Constants.FRAME_MAIN, true, true);
        Assert.assertEquals(convertedContent, expectedHtml, "Footnotes should be converted correctly.");
    }
}
