package ch.dreyeck.zettelkasten;

import ch.dreyeck.zettelkasten.ZettelController;
import ch.dreyeck.zettelkasten.xml.Zettel;
import ch.dreyeck.zettelkasten.xml.Links;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class ZettelControllerTest {
    private ZettelController zettelController;
    private Zettel zettel;

    @BeforeMethod
    public void setUp() {
        // Set up a Zettel with three links to the same attachment
        zettel = new Zettel();
        Links links = new Links();

        // Adding duplicate links
        links.getLink().add("attachment1.pdf");
        links.getLink().add("attachment1.pdf");
        links.getLink().add("attachment1.pdf");

        zettel.setLinks(links);

        // Initialize the controller
        zettelController = new ZettelController(zettel, null); // Pass null for the view as we're testing logic
    }

    @Test
    public void testUniqueAttachmentsAfterDuplicates() {
        // Ensure there is only one unique attachment in the list
        List<String> attachments = zettelController.getAttachments(zettel);

        Assert.assertEquals(attachments.size(), 1, "There should be only one unique attachment link.");
        Assert.assertEquals(attachments.get(0), "attachment1.pdf", "The attachment should be 'attachment1.pdf'.");
    }
}
