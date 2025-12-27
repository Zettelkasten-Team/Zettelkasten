package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.Constants;

public class HtmlUbbUtilListEmphasisNormalizationTest {

    private static Daten createData(Settings settings) {
        Synonyms syn = new Synonyms();
        BibTeX bib = new BibTeX(null, settings);
        return new Daten(null, settings, syn, bib);
    }

    @Test
    public void listEmphasisNormalizationPreventsMisnestedTags() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(false);
        Daten data = createData(settings);
        BibTeX bib = data.bibtexObj;

        String content = "[l][*]Discrete[k] https://example.com/one.png[/*]"
                + "[*]Relational[/k][/*][/l]"
                + "[br][l][*][f]time as ordering device[k][/*]"
                + "[*][/f]time as experienced flow[/k][/*][/l]";

        data.addEntry("Test", content, new String[0], new String[0], "", null, "2025-01-01", -1, false, -1);
        int entryNr = data.getActivatedEntryNumber();

        String html = HtmlUbbUtil.getEntryAsHTMLSanitized(settings, data, bib, entryNr, null, Constants.FRAME_DESKTOP);
        assertFalse(html.contains("</a>\">"));
        assertTrue(HtmlValidator.isValidHTML(html, entryNr, content));
    }

    @Test
    public void markdownListEmphasisAndImagesNormalizeToValidHtml() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);
        Daten data = createData(settings);
        BibTeX bib = data.bibtexObj;

        String content = "[l][*][f][f]time as ordering device[k][/*]"
                + "[*][/f]time as experienced flow[/k][/*][/l]\n"
                + "![Image](https://example.com/STM_file.png)";

        data.addEntry("Test", content, new String[0], new String[0], "", null, "2025-01-01", -1, false, -1);
        int entryNr = data.getActivatedEntryNumber();

        String html = HtmlUbbUtil.getEntryAsHTMLSanitized(settings, data, bib, entryNr, null, Constants.FRAME_DESKTOP);
        assertFalse(html.contains("</a>\">"));
        assertFalse(html.contains("</a>\""));
        assertTrue(HtmlValidator.isValidHTML(html, entryNr, content));
    }
}
