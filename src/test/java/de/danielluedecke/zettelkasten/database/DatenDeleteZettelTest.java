package de.danielluedecke.zettelkasten.database;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * AC-06 characterization of the existing {@code Daten.deleteZettel} behavior,
 * before deletion is extracted into a Swing-independent operation.
 *
 * Scope: {@code Daten.deleteZettel} only. The complete deletion workflow is
 * coordinated by {@code ZettelkastenView.deleteEntries} and
 * {@code DeleteEntryTask}, which additionally clean up desktops, bookmarks,
 * incoming Folgezettel references and search results; that workflow is not
 * covered here.
 *
 * Headless scope: these tests instantiate no Swing UI objects, but they still
 * need the current Maven test classpath. Logging through
 * {@code Constants.zknlogger} initializes the logger with
 * {@code ZettelkastenView.class.getName()}, which loads the Swing Application
 * Framework class {@code org.jdesktop.application.FrameView}. These tests are
 * therefore not evidence of complete Swing independence.
 */
public class DatenDeleteZettelTest {
    private Document document;
    private Daten daten;

    /**
     * Entries 1 and 2 are linked by manual links in both directions (as
     * {@code addManualLink} stores them); entry 3 is the Folgezettel parent of
     * entry 2. All entries have content, because {@code isDeleted} treats an entry
     * with empty content as deleted. There is no whitespace between elements,
     * because entry positions are counted over the root element's content.
     */
    @Before
    public void setUp() throws JDOMException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<zettelkasten>"
                + "<zettel zknid=\"id-one\">" + "<title>one</title>" + "<content>c1</content>" + "<author />"
                + "<keywords />" + "<manlinks>2</manlinks>" + "<links />" + "<misc />" + "<luhmann />" + "</zettel>"
                + "<zettel zknid=\"id-two\">" + "<title>two</title>" + "<content>c2</content>" + "<author />"
                + "<keywords />" + "<manlinks>1</manlinks>" + "<links />" + "<misc />" + "<luhmann />" + "</zettel>"
                + "<zettel zknid=\"id-three\">" + "<title>three</title>" + "<content>c3</content>" + "<author />"
                + "<keywords />" + "<manlinks />" + "<links />" + "<misc />" + "<luhmann>2</luhmann>" + "</zettel>"
                + "</zettelkasten>";
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        daten = new Daten(document);
    }

    /**
     * Category A: properties to preserve when deletion is extracted.
     */
    @Test
    public void testDeleteZettelPreservedProperties() {
        assertEquals("No reusable position before deletion", -1, daten.retrieveFirstEmptyEntry());

        assertTrue("Deleting an existing entry should succeed", daten.deleteZettel(2));

        // The entry is cleared in place; no entry is renumbered.
        assertEquals("Entry count must not change", 3, daten.getCount(Daten.ZKNCOUNT));
        assertTrue("Deleted entry should be recognized as deleted", daten.isDeleted(2));
        assertEquals("Deleted entry's title should be cleared", "", daten.getZettelTitle(2));
        assertEquals("Entry 1 keeps its position", "one", daten.getZettelTitle(1));
        assertEquals("Entry 3 keeps its position", "three", daten.getZettelTitle(3));

        // The manual backlink from entry 1 to the deleted entry is removed.
        assertNull("Backlink from entry 1 should be removed", daten.getManualLinks(1));
        assertNull("Deleted entry's manual links should be cleared", daten.getManualLinks(2));

        // The deleted entry's zknid is cleared; other identities are unchanged.
        assertEquals("Deleted entry's zknid should be cleared", "",
                Daten.retrieveElement(document, 2).getAttributeValue(Daten.ATTRIBUTE_ZETTEL_ID));
        assertEquals("Entry 1 keeps its zknid", "id-one",
                Daten.retrieveElement(document, 1).getAttributeValue(Daten.ATTRIBUTE_ZETTEL_ID));
        assertEquals("Entry 3 keeps its zknid", "id-three",
                Daten.retrieveElement(document, 3).getAttributeValue(Daten.ATTRIBUTE_ZETTEL_ID));

        // The emptied position is recognized as reusable.
        assertEquals("Deleted position should be the first reusable one", 2, daten.retrieveFirstEmptyEntry());

        // Deleting the same entry again is refused.
        assertFalse("Repeated deletion should be refused", daten.deleteZettel(2));
    }

    /**
     * Category B: current implementation artifacts of {@code deleteZettel}.
     *
     * These are characterization observations, not desirable domain invariants. A
     * later refactoring may deliberately eliminate them; this test is then
     * expected to change.
     */
    @Test
    public void testDeleteZettelObservedImplementationArtifacts() {
        assertTrue("Entry 1 should be activatable", daten.activateEntry(1));
        assertFalse("No back navigation before deletion", daten.canHistoryBack());

        assertTrue("Deleting an existing entry should succeed", daten.deleteZettel(2));

        // Observation: incoming Folgezettel references are not removed by
        // deleteZettel; in the application, DeleteEntryTask removes them afterwards.
        assertEquals("Observed: parent still references the deleted entry", "2", daten.getSubEntriesCsv(3));

        // Observation: deleteZettel calls nextEntry(), which advances the activated
        // entry (here onto the deleted entry 2) and adds it to the navigation history.
        assertEquals("Observed: activated entry advanced onto the deleted entry", 2,
                daten.getActivatedEntryNumber());
        assertTrue("Observed: deletion added a navigation history step", daten.canHistoryBack());
    }
}
