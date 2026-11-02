package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.StringReader;
import java.lang.reflect.Field;
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

public class HtmlUbbUtilAutomaticLinksTest {
    private static final String CHATGPT = "https://chatgpt.com/g/g-p-6928007ad8dc8191ac5152dfb0ca199b-hyperdoc-8470/c/6ae63fd8-fbe8-83ed-928b-5f68ce8ead73";

    private String render(String raw) {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(true);
        return HtmlUbbUtil.convertUbbToHtml(settings, null, null, raw, Constants.FRAME_MAIN, false, false);
    }

    private List<String> targets(String html) throws Exception {
        List<String> targets = new ArrayList<>();
        new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
            @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrs, int pos) {
                if (tag == HTML.Tag.A) targets.add((String) attrs.getAttribute(HTML.Attribute.HREF));
            }
        }, false);
        assertTrue(html, HtmlValidator.isValidHTML(html, 11480));
        return targets;
    }

    @Test public void originalChatGptUrlSurvivesClickHandoff() throws Exception {
        String html = render("[Existing](https://example.com)\n2026-11-02 12:06:42 – " + CHATGPT);
        assertEquals(List.of("https://example.com", CHATGPT), targets(html));
        HyperlinkEvent click = new HyperlinkEvent(this, HyperlinkEvent.EventType.ACTIVATED, null, targets(html).get(1));
        assertEquals(CHATGPT, click.getDescription());
        assertEquals(CHATGPT, new URI(click.getDescription()).toString());
    }

    @Test public void existingAnchorIsPreservedWithoutNesting() throws Exception {
        String anchor = "<a class='existing' title='a > b' href='https://example.com/a_b'>https://example.com/a_b</a>";
        String html = HtmlUbbUtil.convertHyperlinks(anchor + " " + CHATGPT);
        assertTrue(html, html.startsWith(anchor));
        assertEquals(List.of("https://example.com/a_b", CHATGPT), targets(html));
        assertEquals(html, HtmlUbbUtil.convertHyperlinks(html));
    }

    @Test public void internalLinksAndFootnoteAnchorsRemainSeparate() throws Exception {
        String html = render("[z 42]Note[/z] [Source](https://example.com) " + CHATGPT);
        assertEquals(List.of("#z_42", "https://example.com", CHATGPT), targets(html));
        String footnote = "<a class=\"fnlink\" href=\"#fn_7\">7</a>";
        assertTrue(HtmlUbbUtil.convertHyperlinks(footnote + " http://example.org").startsWith(footnote));
        assertEquals(List.of("#fn_7", "http://example.org"), targets(HtmlUbbUtil.convertHyperlinks(footnote + " http://example.org")));
    }

    @Test public void keywordHighlightingKeepsCompleteTarget() throws Exception {
        Field terms = HtmlUbbUtil.class.getDeclaredField("highlightTermsKeywords");
        Field whole = HtmlUbbUtil.class.getDeclaredField("highlightWholeWord");
        terms.setAccessible(true); whole.setAccessible(true);
        Object originalTerms = terms.get(null), originalWhole = whole.get(null);
        try {
            HtmlUbbUtil.setHighlighTerms(new String[] { "HyperDoc" }, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS, false);
            String html = render("[Existing](https://example.com) " + CHATGPT);
            assertTrue(html, html.contains("<span class=\"hs_kw\">hyperdoc</span>"));
            assertEquals(List.of("https://example.com", CHATGPT), targets(html));
        } finally { terms.set(null, originalTerms); whole.set(null, originalWhole); }
    }

    @Test public void longTargetWithQueryAndFragmentRemainsWhole() throws Exception {
        String url = "https://example.com/long-path/" + "abc-def/".repeat(30) + "end?first=a%2Fb&second=x_y#section-2";
        String html = HtmlUbbUtil.convertHyperlinks(url);
        assertEquals(List.of(url), targets(html));
        assertFalse(html, html.contains("<br"));
    }

    @Test public void trailingPunctuationStaysOutsideAnchor() throws Exception {
        String html = HtmlUbbUtil.convertHyperlinks("See https://example.com/path. Also (http://example.org/Topic_(name)), next.");
        assertEquals(List.of("https://example.com/path", "http://example.org/Topic_(name)"), targets(html));
        assertTrue(html, html.contains("</a>."));
        assertTrue(html, html.contains("</a>),"));
    }

    @Test public void attributesCodeAndImageMarkupAreNotRelinked() throws Exception {
        String prefix = "<span title='https://attribute.example/a > b'>Text</span>"
                + "<code>https://code.example</code> [img]https://image.example/a.png[/img] ";
        String html = HtmlUbbUtil.convertHyperlinks(prefix + CHATGPT);
        assertTrue(html, html.startsWith(prefix));
        assertEquals(List.of(CHATGPT), targets(html));
    }
}
