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
    public void testInvalidHTML() {
        String invalidHtmlContent = "<html><body><p>This is invalid HTML content.</body></html>";
        boolean isValid = HtmlValidator.isValidHTML(invalidHtmlContent, 2);
        Assert.assertFalse(isValid, "Invalid HTML content should return false.");
    }

    // Add more test cases to cover edge cases and additional scenarios

}
