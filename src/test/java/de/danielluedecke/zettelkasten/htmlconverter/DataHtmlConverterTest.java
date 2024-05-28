package de.danielluedecke.zettelkasten.htmlconverter;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataHtmlConverterTest {

    private DataHtmlConverter dataHtmlConverter;

    @BeforeMethod
    public void setUp() {
        dataHtmlConverter = new DataHtmlConverter();
    }

    @Test
    public void testConvertDataToHtml_NullInput_ReturnsEmptyString() {
        // Arrange
        String input = null;

        // Act
        String result = dataHtmlConverter.convertDataToHtml(input);

        // Assert
        Assert.assertEquals(result, "");
    }

    @Test
    public void testConvertDataToHtml_EmptyInput_ReturnsEmptyString() {
        // Arrange
        String input = "";

        // Act
        String result = dataHtmlConverter.convertDataToHtml(input);

        // Assert
        Assert.assertEquals(result, "");
    }

    @Test
    public void testConvertDataToHtml_ValidInput_ReturnsHtmlFormattedString() {
        // Arrange
        String input = "Sample data";

        // Act
        String result = dataHtmlConverter.convertDataToHtml(input);

        // Assert
        Assert.assertTrue(result.contains("<html>"));
        Assert.assertTrue(result.contains("<body>"));
        Assert.assertTrue(result.contains("<div>Sample data</div>"));
        Assert.assertTrue(result.contains("</body>"));
        Assert.assertTrue(result.contains("</html>"));
    }

    // Add more test cases as needed to cover different scenarios and edge cases
}
