package de.danielluedecke.zettelkasten;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jdesktop.application.ResourceMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;

/**
 * Runs real navigation and display synchronization without constructing a frame.
 * Pane rendering and toolbar updates are mocked; the pane's requested entry is
 * verified. No tab is selected, so no background tab-loading task is started.
 */
public class ZettelkastenViewNavigationTest {
    private ZettelkastenView view;
    private Daten data;

    @Before
    public void setUp() throws Exception {
        Element root = new Element("zettelkasten");
        for (int n = 1; n <= 3; n++) {
            root.addContent(new Element("zettel")
                    .addContent(new Element("title").setText("Entry " + n))
                    .addContent(new Element("content").setText("Content " + n)));
        }
        data = new Daten(new Document(root));
        data.activateEntry(2);
        view = mock(ZettelkastenView.class);
        setField("data", data);
        setField("settings", mock(Settings.class));
        when(view.getFrame()).thenReturn(mock(JFrame.class));
        when(view.getResourceMap()).thenReturn(mock(ResourceMap.class));
        for (String name : new String[] {
                "jButtonRefreshKeywords", "jButtonRefreshTitles", "jButtonRefreshAuthors",
                "jButtonRefreshCluster", "jButtonRefreshAttachments", "jTextFieldFilterKeywords",
                "jTextFieldFilterAuthors", "jTextFieldFilterTitles", "jTextFieldFilterCluster",
                "jTextFieldFilterAttachments", "jTableKeywords", "jTableAuthors", "jTableTitles",
                "jTreeCluster", "jTableAttachments", "menuBar", "statusMsgLabel" }) {
            Field field = ZettelkastenView.class.getDeclaredField(name);
            setField(name, mock(field.getType()));
        }
        JTabbedPane tabs = mock(JTabbedPane.class);
        when(tabs.getSelectedIndex()).thenReturn(-1);
        setField("jTabbedPaneMain", tabs);
        doCallRealMethod().when(view).showFirstEntry();
        doCallRealMethod().when(view).showPrevEntry();
        doCallRealMethod().when(view).showNextEntry();
        doCallRealMethod().when(view).showLastEntry();
        // Final display/preview methods already execute normally with the subclass mock maker.
        view.displayedZettel = 2;
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ZettelkastenView.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(view, value);
    }

    private void assertDisplayedEntry(int expected) {
        assertEquals(expected, data.getActivatedEntryNumber());
        assertEquals(expected, view.displayedZettel);
        verify(view).updateEntryPaneAndKeywordsPane(expected);
    }

    @Test
    public void previousEntrySynchronizesDisplay() {
        view.showPrevEntry();
        assertDisplayedEntry(1);
    }

    @Test
    public void nextEntrySynchronizesDisplay() {
        view.showNextEntry();
        assertDisplayedEntry(3);
    }

    @Test
    public void firstEntrySynchronizesDisplay() {
        view.showFirstEntry();
        assertDisplayedEntry(1);
    }

    @Test
    public void lastEntrySynchronizesDisplay() {
        view.showLastEntry();
        assertDisplayedEntry(3);
    }

    @Test
    public void navigationUsesActivatedEntryAfterDifferentPreview() {
        data.activateEntry(3);
        view.displayedZettel = 1;
        view.showPrevEntry();
        assertDisplayedEntry(2);
    }

    @Test
    public void previousEntryWrapsFromFirstToLast() {
        data.activateEntry(1);
        view.displayedZettel = 1;
        view.showPrevEntry();
        assertDisplayedEntry(3);
    }

    @Test
    public void nextEntryWrapsFromLastToFirst() {
        data.activateEntry(3);
        view.displayedZettel = 3;
        view.showNextEntry();
        assertDisplayedEntry(1);
    }

    @Test
    public void tablePreviewAndRefreshKeepActivatedEntrySeparate() {
        view.setNewDisplayedEntryAndUpdateDisplay(1);
        assertEquals(2, data.getActivatedEntryNumber());
        assertEquals(1, view.displayedZettel);
        verify(view).updateEntryPaneAndKeywordsPane(1);

        view.updateDisplay();
        assertEquals(2, data.getActivatedEntryNumber());
        assertEquals(1, view.displayedZettel);
    }
}
