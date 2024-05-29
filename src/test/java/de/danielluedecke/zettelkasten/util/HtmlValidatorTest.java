package de.danielluedecke.zettelkasten.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HtmlValidatorTest {

    @Test
    public void testValidHTML() {
        String validHtmlContent = "<html><body><p>This is valid HTML content.</p></body></html>";
        boolean isValid = HtmlValidator.isValidHTML(validHtmlContent, 1);
        Assert.assertTrue(isValid, "Valid HTML content should return true.");
    }

    @Test
    public void testWellFormedHTML() {
        String validHtml = "<html><body><div><p>This is valid HTML content.</p></div></body></html>";
        Assert.assertTrue(HtmlValidator.isWellFormed(validHtml));
    }

    @Test
    public void testInvalidHTML() {
        String invalidHtml = "<html><body><div><p>This is invalid HTML content.</div></p></body></html>";
        Assert.assertFalse(HtmlValidator.isWellFormed(invalidHtml));
    }

    @Test
    public void testNullHTML() {
        String nullHtml = null;
        Assert.assertFalse(HtmlValidator.isWellFormed(nullHtml));
    }

    @Test
    public void testEmptyHTML() {
        String emptyHtml = "";
        Assert.assertFalse(HtmlValidator.isWellFormed(emptyHtml));
    }

}
