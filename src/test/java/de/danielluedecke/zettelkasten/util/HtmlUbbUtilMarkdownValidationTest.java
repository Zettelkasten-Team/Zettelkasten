package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.Constants;

public class HtmlUbbUtilMarkdownValidationTest {

    @Test
    public void markdownActivatedHtmlValidation() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);

        Daten data = new Daten(new Document(new Element("zettelkasten")));

        String content = "[b]Bold [i]and italic[/b] mismatch[/i]";
        String html = HtmlUbbUtil.convertUbbToHtml(settings, data, null, content, Constants.FRAME_DESKTOP, false, false);

        boolean valid = HtmlValidator.isValidHTML(html, 1, content);
        if (!valid) {
            System.out.println("Invalid HTML output: " + html);
        }
        assertTrue("Expected valid HTML, got invalid for content: " + content, valid);
    }
}
