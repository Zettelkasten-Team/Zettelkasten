package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.Constants;

public class HtmlUbbUtilMarkdownImagesTest {

    @Test
    public void markdownImagesDoNotEmitStrayAnchorQuotes() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);
        Daten data = new Daten(new Document(new Element("zettelkasten")));

        String content = "![One](https://example.com/one.png)\n"
                + "A link: [Example](https://example.com)\n";
        String html = HtmlUbbUtil.convertUbbToHtml(settings, data, null, content, Constants.FRAME_DESKTOP, false, false);
        assertFalse(html.contains("</a>\">"));
        assertTrue(HtmlValidator.isValidHTML(html, 1, content));
    }
}
