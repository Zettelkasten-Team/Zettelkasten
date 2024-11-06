package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdom2.Element;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DatenDeleteAttachmentTest {
    private Daten daten;
    private int entryNumber;

    @BeforeMethod
    public void setUp() {
        // Mock the required dependencies
        ZettelkastenView view = Mockito.mock(ZettelkastenView.class);
        Settings settings = Mockito.mock(Settings.class);
        Synonyms synonyms = Mockito.mock(Synonyms.class);
        BibTeX bibTeX = Mockito.mock(BibTeX.class);

        // Initialize Daten with mocks
        daten = new Daten(view, settings, synonyms, bibTeX);
        entryNumber = 1;

        // Set up multiple duplicate attachments using addAttachments
        String[] attachments = {"attachment1.pdf", "attachment1.pdf", "attachment1.pdf"};
        daten.addAttachments(entryNumber, attachments);

        // Confirm attachments are added
        List<Element> initialAttachments = daten.getAttachments(entryNumber);
        Assert.assertEquals(initialAttachments.size(), 3, "Three instances of the attachment should have been added.");
    }

    @Test
    public void testDeleteAttachmentListedMultipleTimes() {
        // Attempt to delete an attachment that is listed three times
        daten.deleteAttachment("attachment1.pdf", entryNumber);

        // Get remaining attachments as Elements
        List<Element> remainingElements = daten.getAttachments(entryNumber);

        // Extract text values from Element objects for assertion
        List<String> remainingAttachments = remainingElements == null
                ? Collections.emptyList()
                : remainingElements.stream()
                .map(Element::getText)
                .collect(Collectors.toList());

        // Assert that all instances are deleted
        Assert.assertTrue(remainingAttachments.isEmpty(),
                "All instances of the attachment should be deleted, but duplicates may remain.");
    }
}
