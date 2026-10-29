package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;

public class HtmlUbbUtilBacktickEscapeTest {
    @Test
    public void escapedBackticksInLinkRemainLiteral() {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(true);
        Daten data = new Daten(new Document(new Element("zettelkasten")));
        // Synthetic reduction of entry 11392; not its stored text.
        String raw = "[Source \\`preview_emit\\`](https://example.com/code?utm_source=test)";
        String html = HtmlUbbUtil.convertUbbToHtml(settings, data, null, raw,
                Constants.FRAME_MAIN, false, false);
        assertTrue(html, html.contains("Source \\`preview_emit\\`</a>"));
        assertFalse(html, html.contains("<i>"));
        assertTrue(html, html.contains("href=\"https://example.com/code?utm_source=test\""));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
    }

    @Test
    public void regularCodeRemainsProtected() {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(true);
        String html = HtmlUbbUtil.convertUbbToHtml(settings, null, null,
                "`a_b_c` and **bold**", Constants.FRAME_MAIN, false, false);
        assertTrue(html, html.contains("<code>a_b_c</code>"));
        assertTrue(html, html.contains("<b>bold</b>"));
        assertTrue(HtmlValidator.isValidHTML(html, 1));
    }

    @Test
    public void invalidNestingStillFailsValidation() {
        assertFalse(HtmlValidator.isValidHTML("<b><i>broken</b></i>", 1));
        assertFalse(UbbNestingValidator.validate("[c][m 1]broken[/c][/m]").valid);
    }
}
