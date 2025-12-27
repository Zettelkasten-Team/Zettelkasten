package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.Constants;

public class HtmlUbbUtilMarkdownEmphasisTest {

    @Test
    public void markdownEmphasisIsBalancedPerLine() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);
        Daten data = new Daten(new Document(new Element("zettelkasten")));

        String content = "> **Time is not one thing...**\n"
                + "must *not simulate* lived time\n"
                + "Krieg’s whole argument is that **thinking machines must not**.";

        String html = HtmlUbbUtil.convertUbbToHtml(settings, data, null, content, Constants.FRAME_DESKTOP, false, false);
        assertTrue("Expected valid HTML from markdown emphasis", HtmlValidator.isValidHTML(html, 1, content));
    }
}
