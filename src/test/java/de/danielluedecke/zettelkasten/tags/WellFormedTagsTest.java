package de.danielluedecke.zettelkasten.tags;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WellFormedTagsTest {

    @Test
    public void testWellFormedHTMLTags() {
        Tag[] tags = { new HTMLTag("<html>"), new HTMLTag("<body>"), new HTMLTag("</body>"), new HTMLTag("</html>") };
        Assert.assertTrue(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testNotWellFormedHTMLTags() {
        Tag[] tags = { new HTMLTag("<html>"), new HTMLTag("<body>"), new HTMLTag("</html>"), new HTMLTag("</body>") };
        Assert.assertFalse(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testWellFormedUBBTags() {
        Tag[] tags = { new UBBTag("[b]"), new UBBTag("[i]"), new UBBTag("[/i]"), new UBBTag("[/b]") };
        Assert.assertTrue(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testNotWellFormedUBBTags() {
        Tag[] tags = { new UBBTag("[b]"), new UBBTag("[i]"), new UBBTag("[/b]"), new UBBTag("[/i]") };
        Assert.assertFalse(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testMixedTagsWellFormed() {
        Tag[] tags = { new HTMLTag("<html>"), new UBBTag("[b]"), new UBBTag("[/b]"), new HTMLTag("</html>") };
        Assert.assertTrue(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testMixedTagsNotWellFormed() {
        Tag[] tags = { new HTMLTag("<html>"), new UBBTag("[b]"), new HTMLTag("</html>"), new UBBTag("[/b]") };
        Assert.assertFalse(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testEmptyTags() {
        Tag[] tags = {};
        Assert.assertTrue(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testSingleHTMLTag() {
        Tag[] tags = { new HTMLTag("<html>") };
        Assert.assertFalse(WellFormedTags.isWellFormed(tags));
    }

    @Test
    public void testSingleUBBTag() {
        Tag[] tags = { new UBBTag("[b]") };
        Assert.assertFalse(WellFormedTags.isWellFormed(tags));
    }
}
