package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import org.junit.Test;
import de.danielluedecke.zettelkasten.settings.Settings;

public class HtmlUbbUtilMarkdownProjectionTest {
    private static final String EXCERPT = "## Federated Wiki ohne DNS\n\n> Können bekannte Sites erreichbar bleiben?\n\n"
            + "### Drei Arten von Ausfall\n\n* **Bekannte** Site\n* `DNS` bleibt literal\n\n"
            + "[Quelle](https://example.com/a_b_c) und https://example.org/path\n";
    private static final String TABLE = "| Ausfall | Antwort |\n| --- | --- |\n| DNS | Andere Auflösung |";

    private String render(String raw, boolean enabled) {
        Settings settings = mock(Settings.class);
        when(settings.getMarkdownActivated()).thenReturn(enabled);
        return HtmlUbbUtil.convertUbbToHtml(settings, null, null, raw, Constants.FRAME_MAIN, false, false);
    }

    @Test public void enabledMarkdownRendersReduced11483Content() {
        String html = render(EXCERPT.replace("\n", "[br]"), true);
        assertTrue(html, html.contains("<h3>Federated Wiki ohne DNS</h3>"));
        assertTrue(html, html.contains("<h4>Drei Arten von Ausfall</h4>"));
        assertTrue(html, html.contains("class=\"zitat\""));
        assertTrue(html, html.contains("<li><b>Bekannte</b> Site</li>"));
        assertTrue(html, html.contains("<code>DNS</code>"));
        assertTrue(html, html.contains("href=\"https://example.com/a_b_c\""));
        assertTrue(html, html.contains("href=\"https://example.org/path\""));
        assertTrue(HtmlValidator.isValidHTML(html, 11483, EXCERPT));
    }

    @Test public void disabledMarkdownKeepsSyntaxAndAutolinks() {
        String html = render(EXCERPT + TABLE, false);
        assertTrue(html, html.contains("## Federated Wiki"));
        assertTrue(html, html.contains("&gt; Können"));
        assertFalse(html, html.contains("<h3>"));
        assertFalse(html, html.contains("<table>"));
        assertTrue(html, html.contains("| --- | --- |"));
        assertTrue(html, html.contains("href=\"https://example.org/path\""));
    }

    @Test public void markdownBeforeAndAfterAutolinkIsRendered() {
        String html = render("## Before\nhttps://example.com\n\n### After\n> Quote", true);
        assertTrue(html, html.contains("<h3>Before</h3>"));
        assertTrue(html, html.contains("<h4>After</h4>"));
        assertTrue(html, html.contains("href=\"https://example.com\""));
        assertTrue(html, html.contains("class=\"zitat\""));
    }

    @Test public void existingAnchorAndBareUrlStaySeparate() {
        String anchor = "<a href=\"https://example.com\">Source</a>";
        String html = HtmlUbbUtil.convertHyperlinks(anchor + " https://example.org");
        assertTrue(html, html.startsWith(anchor));
        assertTrue(html, html.contains("href=\"https://example.org\""));
        assertTrue(HtmlValidator.isValidHTML(html, 1));
    }

    @Test public void highlightingPreservesHeadingAndTarget() throws Exception {
        Field terms = HtmlUbbUtil.class.getDeclaredField("highlightTermsKeywords");
        Field whole = HtmlUbbUtil.class.getDeclaredField("highlightWholeWord");
        terms.setAccessible(true); whole.setAccessible(true);
        Object oldTerms = terms.get(null), oldWhole = whole.get(null);
        try {
            HtmlUbbUtil.setHighlighTerms(new String[] {"HyperDoc"}, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS, false);
            String html = render("## HyperDoc\nhttps://example.com/HyperDoc", true);
            assertTrue(html, html.contains("<h3><span class=\"hs_kw\">HyperDoc</span></h3>"));
            assertTrue(html, html.contains("href=\"https://example.com/HyperDoc\""));
        } finally { terms.set(null, oldTerms); whole.set(null, oldWhole); }
    }

    @Test public void codeContentIsNotParsedAsMarkdownOrTable() {
        String raw = "`## literal https://example.com`\n```\n" + TABLE + "\n```";
        String html = render(raw, true);
        assertFalse(html, html.contains("<h3>"));
        assertFalse(html, html.contains("<table>"));
        assertFalse(html, html.contains("<a "));
        assertTrue(html, html.contains("## literal https://example.com"));
        assertTrue(html, html.contains("| --- | --- |"));
    }

    @Test public void simplePipeTablePreservesInlineCodeAndLinks() {
        String raw = "| **Ausfall** | Antwort |\n| :--- | ---: |\n"
                + "| `a|b` | [Quelle](https://example.com/a_b_c) |\n\nAfter";
        String html = render(raw, true);
        assertTrue(html, html.contains("<table><tr><th><b>Ausfall</b></th><th>Antwort</th></tr>"));
        assertTrue(html, html.contains("<td><code>a|b</code></td>"));
        assertTrue(html, html.contains("href=\"https://example.com/a_b_c\""));
        assertTrue(html, html.contains("</table><br><br>After"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
    }

    @Test public void existingUbbTablesStillRenderWithMarkdownDisabled() {
        String raw = "[table]A^B[br]One|Two[/table]";
        String html = render(raw, false);
        assertTrue(html, html.contains("<table>"));
        assertTrue(html, html.contains("<th class=\"tfirstcol\">A</th>"));
        assertTrue(html, html.contains(">One</td>"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, raw));
    }

    @Test public void inconsistentPipeRowsAreNotSilentlyReformatted() {
        String raw = "| A | B |\n| --- | --- |\n| Only one |";
        assertFalse(render(raw, true).contains("<table>"));
    }
}
