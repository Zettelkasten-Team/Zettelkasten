package de.danielluedecke.zettelkasten.tasks.search;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Constants;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SearchEntriesHelperTest {
	/**
	 * Common/shared daten for test. See setUp().
	 */
	private Daten daten;
	/**
	 * Common/shared synonyms data for test. See setUp().
	 */
	private Synonyms synonymsData;

	/**
	 * Common/shared options for test. See setUp().
	 */
	SearchTaskOptions options;

	@BeforeEach
	public void setUp() throws JDOMException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ "<zettelkasten firstzettel=\"1\" lastzettel=\"2\">" + "<zettel>"
				+ "<title>Title of first entry</title>"
				+ "<content>Content of first entry [k]Zettel[/k]kasten</content>" + "<author />" + "<keywords />"
				+ "<manlinks />" + "<links />" + "<misc />" + "<luhmann>1</luhmann>" + "</zettel>" + "</zettelkasten>";
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new ByteArrayInputStream(xmlString.getBytes()));
		daten = new Daten(document);

		int where = Constants.SEARCH_CONTENT;
		int logical = Constants.LOG_AND;
		boolean wholeword = false;
		boolean matchcase = false;
		boolean synonyms = false;
		boolean accentInsensitive = false;
		boolean regex = false;
		boolean removeTags = false;
		options = new SearchTaskOptions(Constants.SEARCH_USUAL, where, logical, wholeword, matchcase, synonyms,
				accentInsensitive, regex, removeTags);

		synonymsData = new Synonyms();
	}

	@Test
	void testEntryMatchesSearchTerms_shouldRemoveTagsFalse_DoesntFindTerm() {
		String[] searchTerms = new String[] { "Zettelkasten" };
		options.removeTags = false;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_shouldRemoveTagsTrue_FindTerm() {
		String[] searchTerms = new String[] { "Zettelkaste" };
		options.removeTags = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_regexTrue_FindsTerm() {
		String[] searchTerms = new String[] { "Con.*" };
		options.regex = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_accentInsensitiveTrue_FindsDifferentAccentTerm() {
		String[] searchTerms = new String[] { "Cóntent" };
		options.accentInsensitive = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_accentInsensitiveFalse_DoesFindDifferentAccentTerm() {
		String[] searchTerms = new String[] { "Cóntent" };
		options.accentInsensitive = false;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_matchCaseFalse_DoesntFindDifferentCaseTerm() {
		String[] searchTerms = new String[] { "content" };
		options.matchcase = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_matchCaseFalse_FindsDifferentCaseTerm() {
		String[] searchTerms = new String[] { "content" };
		options.matchcase = false;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_wholewordFalse_FindsPartialTerm() {
		String[] searchTerms = new String[] { "Conte" };
		options.wholeword = false;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_wholewordTrue_DoesntFindPartialTerm() {
		String[] searchTerms = new String[] { "Conten" };
		options.wholeword = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_wholewordTrue_FindFullTerm() {
		String[] searchTerms = new String[] { "Content" };
		options.wholeword = true;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_whereIsRespected_DoesntFindTermInOtherSection() {
		String[] searchTerms = new String[] { "Content" };
		options.where = Constants.SEARCH_TITLE;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_whereIsRespected_FindsTermInCorrectSection() {
		String[] searchTerms = new String[] { "Content" };
		options.where = Constants.SEARCH_CONTENT;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalAndIsRespected_MissesOneTermReturnsFalse() {
		String[] searchTerms = new String[] { "Content", "MISSING" };
		options.logical = Constants.LOG_AND;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalAndIsRespected_FoundAllTermsReturnsTrue() {
		String[] searchTerms = new String[] { "Content", "first", "entry" };
		options.logical = Constants.LOG_AND;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalOrIsRespected_FoundOneTermReturnsTrue() {
		String[] searchTerms = new String[] { "Content", "MISSING" };
		options.logical = Constants.LOG_OR;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalOrIsRespected_FoundNoneReturnsFalse() {
		String[] searchTerms = new String[] { "MISSING" };
		options.logical = Constants.LOG_OR;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalNotRespected_FoundNoneReturnsTrue() {
		String[] searchTerms = new String[] { "MISSING" };
		options.logical = Constants.LOG_NOT;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertTrue(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

	@Test
	void testEntryMatchesSearchTerms_logicalNotRespected_FoundOneReturnsFalse() {
		String[] searchTerms = new String[] { "Content", "MISSING" };
		options.logical = Constants.LOG_NOT;

		SearchEntriesHelper helper = new SearchEntriesHelper(daten, synonymsData, options);

		assertFalse(helper.entryMatchesSearchTerms(/* entryNumber= */1, searchTerms));
	}

}