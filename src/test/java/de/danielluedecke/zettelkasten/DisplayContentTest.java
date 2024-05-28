package de.danielluedecke.zettelkasten;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import javax.swing.*;
import java.net.URL;

public class DisplayContentTest {

    private YourClassUnderTest classUnderTest;
    private JEditorPane jEditorPaneSearchEntry;
    private JEditorPane jEditorPaneEntry;
    private JTextField jTextFieldEntryNumber;
    private DataMock dataMock;
    private SettingsMock settingsMock;
    private ResourceMapMock resourceMapMock;

    @BeforeMethod
    public void setUp() {
        classUnderTest = new YourClassUnderTest();
        jEditorPaneSearchEntry = new JEditorPane();
        jEditorPaneEntry = new JEditorPane();
        jTextFieldEntryNumber = new JTextField();
        dataMock = new DataMock();
        settingsMock = new SettingsMock();
        resourceMapMock = new ResourceMapMock();
    }

    @Test
    public void testDisplayZettelContentWithValidHtml() {
        String validHtml = "<html>Valid Content</html>";
        when(dataMock.getEntryAsHtml(anyInt(), any(), any())).thenReturn(validHtml);
        when(Tools.isValidHTML(validHtml, anyInt())).thenReturn(true);

        classUnderTest.displayZettelContent(1, new String[]{"term1", "term2"});

        Assert.assertEquals(jEditorPaneSearchEntry.getText(), validHtml);
        Assert.assertEquals(jEditorPaneSearchEntry.getCaretPosition(), 0);
    }

    @Test
    public void testDisplayZettelContentWithInvalidHtml() {
        String invalidHtml = "<html>Invalid Content</html>";
        String cleanContent = "Clean Content";
        when(dataMock.getEntryAsHtml(anyInt(), any(), any())).thenReturn(invalidHtml);
        when(Tools.isValidHTML(invalidHtml, anyInt())).thenReturn(false);
        when(dataMock.getCleanZettelContent(anyInt())).thenReturn(cleanContent);
        URL imgURL = getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
        when(resourceMapMock.getString(anyString())).thenReturn("Error Message");

        classUnderTest.displayZettelContent(1, new String[]{"term1", "term2"});

        String expectedHtml = "<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">" +
                "<img border=\"0\" src=\"" + imgURL + "\">&#8195;Error Message</div>" + cleanContent + "</body>";

        Assert.assertEquals(jEditorPaneSearchEntry.getText(), expectedHtml);
        Assert.assertEquals(jEditorPaneSearchEntry.getCaretPosition(), 0);
    }

    @Test
    public void testUpdateSelectedEntryPaneWithValidHtml() {
        String validHtml = "<html>Valid Content</html>";
        when(dataMock.getEntryAsHtml(anyInt(), any(), any())).thenReturn(validHtml);
        when(Tools.isValidHTML(validHtml, anyInt())).thenReturn(true);

        classUnderTest.updateSelectedEntryPane(1);

        Assert.assertEquals(jEditorPaneEntry.getText(), validHtml);
        Assert.assertEquals(jEditorPaneEntry.getCaretPosition(), 0);
    }

    @Test
    public void testUpdateSelectedEntryPaneWithInvalidHtml() {
        String invalidHtml = "<html>Invalid Content</html>";
        String cleanContent = "Clean Content";
        when(dataMock.getEntryAsHtml(anyInt(), any(), any())).thenReturn(invalidHtml);
        when(Tools.isValidHTML(invalidHtml, anyInt())).thenReturn(false);
        when(dataMock.getCleanZettelContent(anyInt())).thenReturn(cleanContent);
        URL imgURL = getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
        when(resourceMapMock.getString(anyString())).thenReturn("Error Message");

        classUnderTest.updateSelectedEntryPane(1);

        String expectedHtml = "<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">" +
                "<img border=\"0\" src=\"" + imgURL + "\">&#8195;Error Message</div>" + cleanContent + "</body>";

        Assert.assertEquals(jEditorPaneEntry.getText(), expectedHtml);
        Assert.assertEquals(jEditorPaneEntry.getCaretPosition(), 0);
    }

    @Test
    public void testUpdateSelectedEntryPaneUpdatesEntryNumber() {
        when(dataMock.getActivatedEntryNumber()).thenReturn(1);

        classUnderTest.updateSelectedEntryPane(1);

        Assert.assertEquals(jTextFieldEntryNumber.getText(), "1");
    }
}
