package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertTrue;

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

public class HtmlUbbUtilMarkdownEmphasisTest {

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
