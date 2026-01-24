package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.EntryID;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DatenTest {
	/**
	 * Common test document. See setUp().
	 */
	public Document document;

	@BeforeEach
	public void setUp() throws JDOMException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ "<zettelkasten firstzettel=\"1\" lastzettel=\"2\">" + "<zettel>"
				+ "<title>Verschlagwortung und alphanumerische anstatt thematische Ordnung</title>" + "<content />"
				+ "<author>1</author>" + "<keywords>1,2,1216,1983</keywords>" + "<manlinks />" + "<links />"
				+ "<misc>feste Stellordnung</misc>" + "<luhmann>4,10,61,161,1771,3622</luhmann>" + "</zettel>"
				+ "<zettel>" + "<title>Zettel entry that is the first parent of the Zettel entry #1</title>"
				+ "<content />" + "<author />" + "<keywords />" + "<manlinks />" + "<links />" + "<misc />"
				+ "<luhmann>1</luhmann>" + "</zettel>" + "<zettel>"
				+ "<title>Zettel entry that is not connected to any of the others</title>" + "<content />"
				+ "<author />" + "<keywords />" + "<manlinks />" + "<links />" + "<misc />" + "<luhmann />"
				+ "</zettel>" + "</zettelkasten>";
		SAXBuilder builder = new SAXBuilder();
		document = builder.build(new ByteArrayInputStream(xmlString.getBytes()));
	}

	@Test
	void testGoToFirstParentEntry() {
		// data by default has current entry == 1.
		Daten data = new Daten(document);

		// The first (and only) parent of entry 1 is entry 2.
		data.goToFirstParentEntry();
		assertEquals(data.getActivatedEntryNumber(), 2);

		// Entry 2 doesn't have a parent. Keep at the same Zettel number.
		data.goToFirstParentEntry();
		assertEquals(data.getActivatedEntryNumber(), 2);
	}

	@Test
	void testDeleteLuhmannNumber() {
		Daten daten = new Daten(document);

		daten.deleteLuhmannNumber(new EntryID(1), new EntryID(10));
		assertEquals("4,61,161,1771,3622", daten.getSubEntriesCsv(1));

		daten.deleteLuhmannNumber(new EntryID(1), new EntryID(4));
		assertEquals("61,161,1771,3622", daten.getSubEntriesCsv(1));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_PositionZero() {
		Daten daten = new Daten(document);
		assertTrue(daten.addSubEntryToEntryAtPosition(new EntryID(2), new EntryID(3), 0));
		assertEquals("3,1", daten.getSubEntriesCsv(2));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_PositionMinusOne() {
		Daten daten = new Daten(document);
		assertTrue(daten.addSubEntryToEntryAtPosition(new EntryID(2), new EntryID(3), -1));
		assertEquals("1,3", daten.getSubEntriesCsv(2));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_PositionOne() {
		Daten daten = new Daten(document);
		assertTrue(daten.addSubEntryToEntryAtPosition(new EntryID(2), new EntryID(3), 1));
		assertEquals("1,3", daten.getSubEntriesCsv(2));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_InvalidParentEntry() {
		Daten daten = new Daten(document);
		assertFalse(daten.addSubEntryToEntryAtPosition(new EntryID(2), new EntryID(54321), 0));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_InvalidSubEntry() {
		Daten daten = new Daten(document);
		assertFalse(daten.addSubEntryToEntryAtPosition(new EntryID(54321), new EntryID(3), 0));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_ParentEntryEqualsSubEntry() {
		Daten daten = new Daten(document);
		assertFalse(daten.addSubEntryToEntryAtPosition(new EntryID(3), new EntryID(3), 0));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_SubEntryAlreadyExists() {
		Daten daten = new Daten(document);
		assertFalse(daten.addSubEntryToEntryAtPosition(new EntryID(2), new EntryID(1), 0));
	}

	@Test
	void testAddSubEntryToEntryAtPosition_ParentIsDescendantOfSubEntry() {
		Daten daten = new Daten(document);
		assertFalse(daten.addSubEntryToEntryAtPosition(new EntryID(1), new EntryID(2), 0));
	}

	@Test
	void testAddSubEntryToEntryAfterSibling() {
		Daten daten = new Daten(document);
		assertTrue(daten.addSubEntryToEntryAfterSibling(new EntryID(2), new EntryID(3), new EntryID(1)));
		assertEquals("1,3", daten.getSubEntriesCsv(2));
	}

	@Test
	void testSetKeywordUsesCallbacksForSynonyms() {
		Synonyms synonyms = new Synonyms();
		synonyms.addSynonym(new String[] { "old", "old", "alias" });

		final boolean[] confirmCalled = new boolean[1];
		DatenUiCallbacks callbacks = new DatenUiCallbacks() {
			@Override
			public void resetBackupNecessary() {
			}

			@Override
			public void setBackupNecessary() {
			}

			@Override
			public boolean confirmReplaceKeywordInSynonyms(String oldKeyword, String newKeyword) {
				confirmCalled[0] = true;
				return true;
			}

			@Override
			public void displayHistory(int[] history, int historyCount) {
			}

			@Override
			public boolean createFormImage(Daten dataObj, String formTag) {
				return true;
			}
		};

		Daten daten = new Daten(callbacks, null, synonyms, null);
		int keywordPos = daten.addKeyword("old", 1);
		daten.setKeyword(keywordPos, "new");

		assertTrue(confirmCalled[0]);
		assertNotEquals(-1, synonyms.findSynonym("new", true));
		assertEquals(-1, synonyms.findSynonym("old", true));
	}
}
