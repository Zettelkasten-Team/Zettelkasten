package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.util.Constants;

public class HtmlUbbUtilMarkdownImagesTest {

    /**
     * Used as user.dir and user.home, so new Settings() neither reads nor creates
     * real Zettelkasten settings. Created before {@link #isolateSettings()} and
     * deleted by JUnit after each test, also when the test fails.
     */
    @Rule
    public TemporaryFolder settingsFolder = new TemporaryFolder();

    private String originalUserDir;
    private String originalUserHome;

    @Before
    public void isolateSettings() {
        originalUserDir = System.getProperty("user.dir");
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.dir", settingsFolder.getRoot().getPath());
        System.setProperty("user.home", settingsFolder.getRoot().getPath());
    }

    @After
    public void restoreSettingsLocation() {
        // Runs also when a test fails.
        restoreProperty("user.dir", originalUserDir);
        restoreProperty("user.home", originalUserHome);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

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
    @Test
    public void localMarkdownImageBeforeSourceLinkRemainsAnImage() throws Exception {
        File image = settingsFolder.newFile("11401-mermaid-diagram.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image);
        assertLocalImageBeforeSourceLink(image.getName());
    }

    @Test
    public void missingMarkdownImageBeforeSourceLinkKeepsItsResolvedSource() {
        assertLocalImageBeforeSourceLink("missing.png");
    }

    private void assertLocalImageBeforeSourceLink(String filename) {
        Settings settings = new Settings();
        settings.setMarkdownActivated(true);
        settings.setImageResize(true);
        Daten data = new Daten(new Document(new Element("zettelkasten")));
        data.setUserImagePath(settingsFolder.getRoot().getPath());
        String imageMarkup = "![Bild](" + filename + ")";
        assertEquals("[img]" + filename + "[/img]", Tools.convertMarkDown2UBB(imageMarkup));
        String content = "## Die tatsächliche Abhängigkeitsstruktur[br][br]"
                + imageMarkup + "[br][br]**Erhalten**[br]* Liste[br]"
                + "Quellen: [ASDF-Systemdefinitionen](https://example.com/dreyeck.asd)";
        String html = HtmlUbbUtil.convertUbbToHtml(settings, data, null, content,
                Constants.FRAME_DESKTOP, false, false);
        // Parser regression only: file-URL prefixes differ between platforms.
        // See docs/windows-image-file-url.md for the unresolved Windows URL issue.
        Matcher imageTag = Pattern.compile("<img\\b[^>]*\\bsrc=\"([^\"]*)\"").matcher(html);
        assertTrue(html, imageTag.find());
        String imageSource = imageTag.group(1);
        assertTrue(html, imageSource.endsWith("/" + filename) || imageSource.endsWith("\\" + filename));
        assertFalse(html, html.contains("img]"));
        assertTrue(html, html.contains("Quellen: <a href=\"https://example.com/dreyeck.asd\""
                + " title=\"https://example.com/dreyeck.asd\">ASDF-Systemdefinitionen</a>"));
        assertTrue(html, html.contains("<b>Erhalten</b>"));
        assertTrue(html, html.contains("<li>Liste</li>"));
        assertTrue(HtmlValidator.isValidHTML(html, 11401, content));
    }

}
