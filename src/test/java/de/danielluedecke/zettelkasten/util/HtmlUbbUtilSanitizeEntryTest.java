package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;

public class HtmlUbbUtilSanitizeEntryTest {

    @Test
    public void sanitizerCleansKnownHazardsAndProducesValidHtml() {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);
        Daten data = new Daten(new Document(new Element("zettelkasten")));

        String raw = ""
                + "## Heading\n"
                + "[c]Centered [m 0.5]Margin[/c] After[/m]\n"
                + "Link [z 1622]1622[/z]\n"
                + "![Image](https://example.com/a_b.png)\n"
                + "Stray < angle\n";

        String sanitized = HtmlUbbUtil.sanitizeEntryContentForHtml(raw);

        // Deterministic assertions about sanitization (do not rely on validator behavior for the unsanitized case).
        assertNotEquals("Sanitizer should modify the raw content", raw, sanitized);
        assertTrue("Sanitizer should remove/unwrap [c] tag hazards", !sanitized.contains("[c]") && !sanitized.contains("[/c]"));
        assertTrue("Sanitizer should remove/unwrap [m] tag hazards", !sanitized.contains("[m") && !sanitized.contains("[/m]"));
        assertTrue("Sanitizer should neutralize markdown image syntax", !sanitized.contains("![") && !sanitized.contains("]("));
        assertTrue("Sanitizer should escape stray '<'", !sanitized.contains("Stray < angle"));

        // Then ensure rendering produces valid HTML under the app's validator.
        // Constants.FRAME_DESKTOP = 4 (see util.Constants); use literal to avoid missing class in test scope.
        String sanitizedHtml = HtmlUbbUtil.convertUbbToHtml(settings, data, null, sanitized, 4, false, false);
        assertTrue("Expected valid HTML after sanitization", HtmlValidator.isValidHTML(sanitizedHtml, 1, sanitized));
    }
}
