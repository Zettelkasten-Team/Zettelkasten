package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.junit.Test;
import de.danielluedecke.zettelkasten.settings.Settings;

public class HtmlUbbUtilMarkdownLinkTargetTest {
    private String render(String raw) {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(true);
        return HtmlUbbUtil.convertUbbToHtml(settings, null, null, raw, Constants.FRAME_MAIN, false, false);
    }

    @Test
    public void destinationSurvivesFormattingAndClickHandoff() throws Exception {
        String url = "https://example.com/a_b_c__d?purpose=inline&encoded=%2F%5f&query=x_y_z";
        String raw = "_Before_ [**Label** | Medium](" + url + ") *after*";
        String html = render(raw);
        assertTrue(html, html.contains("<i>Before</i>"));
        assertTrue(html, html.contains("<i>after</i>"));
        assertTrue(html, html.contains("<b>Label</b> | Medium</a>"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
        assertClickTarget(html, url);
    }

    @Test
    public void longOpaqueDestinationRemainsUnchanged() throws Exception {
        String url = "https://images.example.com/static/" + "a_b_C-d".repeat(40) + "?purpose=inline";
        assertClickTarget(render("[Building Notes | by Author | Medium](" + url + ")"), url);
    }

    private void assertClickTarget(String html, String expected) throws Exception {
        List<String> targets = new ArrayList<>();
        new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
            @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrs, int pos) {
                if (tag == HTML.Tag.A) targets.add((String) attrs.getAttribute(HTML.Attribute.HREF));
            }
        }, false);
        assertEquals(1, targets.size());
        assertEquals(expected, targets.get(0));
        HyperlinkEvent click = new HyperlinkEvent(this, HyperlinkEvent.EventType.ACTIVATED, null, targets.get(0));
        // The listener hands getDescription() unchanged to Tools.openHyperlink;
        // its external-link branch forwards it to launchFile and new URI.
        assertEquals(expected, click.getDescription());
        assertEquals(expected, new URI(click.getDescription()).toString());
    }
}
