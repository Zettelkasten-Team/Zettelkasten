package de.danielluedecke.zettelkasten;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.Tools;

import javax.swing.JEditorPane;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

public class SearchResultsFrameTest {

    private SearchResultsFrame searchResultsFrame;
    private Daten dataObjMock;
    private Settings settingsObjMock;
    private HtmlUbbUtil htmlUbbUtilMock;
    private JEditorPane jEditorPaneSearchEntryMock;

    @BeforeMethod
    public void setUp() {
        dataObjMock = mock(Daten.class);
        settingsObjMock = mock(Settings.class);
        htmlUbbUtilMock = mock(HtmlUbbUtil.class);
        jEditorPaneSearchEntryMock = mock(JEditorPane.class);

        searchResultsFrame = new SearchResultsFrame(
            null, dataObjMock, null, null, settingsObjMock, null, null, null);
        searchResultsFrame.jEditorPaneSearchEntry = jEditorPaneSearchEntryMock;
    }

    @Test
    public void testDisplayZettelContent_ValidHTML() {
        // Mock data
        String[] highlightTerms = {"term1", "term2"};
        String validHTML = "<html><body>Valid HTML</body></html>";
        when(dataObjMock.getEntryAsHtml(anyInt(), any(), anyString())).thenReturn(validHTML);
        when(Tools.isValidHTML(validHTML, 1)).thenReturn(true);
        when(settingsObjMock.getHighlightSegments()).thenReturn(true);

        // Call method
        searchResultsFrame.displayZettelContent(1, highlightTerms);

        // Verify interactions
        verify(htmlUbbUtilMock).setHighlighTerms(eq(highlightTerms), eq(HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS), anyBoolean());
        verify(jEditorPaneSearchEntryMock).setText(validHTML);
        verify(jEditorPaneSearchEntryMock).setCaretPosition(0);
    }

    @Test
    public void testDisplayZettelContent_InvalidHTML() {
        // Mock data
        String[] highlightTerms = {"term1", "term2"};
        String invalidHTML = "<html><body>Invalid HTML</body></html>";
        String errorContent = "<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\"><img border=\"0\" src=\"null\">errorText</div>Clean content</body>";
        when(dataObjMock.getEntryAsHtml(anyInt(), any(), anyString())).thenReturn(invalidHTML);
        when(Tools.isValidHTML(invalidHTML, 1)).thenReturn(false);
        when(dataObjMock.getCleanZettelContent(1)).thenReturn("Clean content");
        when(htmlUbbUtilMock.HIGHLIGHT_STYLE_SEARCHRESULTS).thenReturn("HIGHLIGHT_STYLE_SEARCHRESULTS");
        when(htmlUbbUtilMock.getHighlightWholeWordSearch()).thenReturn(false);
        when(htmlUbbUtilMock.setHighlighTerms(any(), any(), anyBoolean())).thenReturn(null);
        when(jEditorPaneSearchEntryMock.getCaretPosition()).thenReturn(0);
        when(jEditorPaneSearchEntryMock.getText()).thenReturn(errorContent);

        // Call method
        searchResultsFrame.displayZettelContent(1, highlightTerms);

        // Verify interactions
        verify(htmlUbbUtilMock).setHighlighTerms(eq(highlightTerms), eq(HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS), anyBoolean());
        verify(jEditorPaneSearchEntryMock).setText(errorContent);
        verify(jEditorPaneSearchEntryMock).setCaretPosition(0);
    }
}
