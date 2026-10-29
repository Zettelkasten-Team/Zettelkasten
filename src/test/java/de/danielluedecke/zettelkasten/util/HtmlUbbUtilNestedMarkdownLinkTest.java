package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;

public class HtmlUbbUtilNestedMarkdownLinkTest {
    private String render(String content) {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(true);
        when(settings.getImageResize()).thenReturn(true);
        when(settings.getImagePath(null, true)).thenReturn("unused/");
        Daten data = new Daten(new Document(new Element("zettelkasten")));
        return HtmlUbbUtil.convertUbbToHtml(settings, data, null, content, Constants.FRAME_MAIN, false, false);
    }

    @Test
    public void nestedLabelAndParenthesizedTargetRemainWhole() {
        String target = "https://example.com/Topic_(example)#some_note-1";
        String raw = "[[1]](" + target + ") and _emphasis_";
        String html = render(raw);
        assertTrue(html, html.contains("href=\"" + target + "\""));
        assertTrue(html, html.contains(">[1]</a>"));
        assertTrue(html, html.contains("<i>emphasis</i>"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
    }

    @Test
    public void linkedRemoteImageKeepsImageAndBothDestinations() {
        String image = "https://images.example.com/a_b_c.png?purpose=inline&size=2";
        String target = "https://example.com/source_page";
        String raw = "[![Image](" + image + ")](" + target + ")";
        String html = render(raw);
        assertTrue(html, html.contains("<a href=\"" + target + "\" title=\"" + target + "\">"
                + "<img src=\"" + image.replace("&", "&amp;") + "\"></a>"));
        assertFalse(html, html.contains("file://"));
        assertFalse(html, html.contains("[img]"));
        assertFalse(html, html.contains("<i>"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
    }

    @Test
    public void precedingImageIsNotSwallowedByLaterLink() {
        String html = render("![Local](image.png) and [Source](https://example.com/source)");
        assertTrue(html, html.contains("<img src="));
        assertTrue(html, html.contains("image.png\""));
        assertTrue(html, html.contains(">Source</a>"));
        assertFalse(html, html.contains("img]"));
    }
}
