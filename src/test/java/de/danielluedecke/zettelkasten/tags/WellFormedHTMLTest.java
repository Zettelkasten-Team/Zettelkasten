package de.danielluedecke.zettelkasten.tags;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WellFormedHTMLTest {

    @Test
    public void testWellFormedTags1() {
        String[] tags = {"html", "body", "/body", "/html"};
        Assert.assertTrue(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testWellFormedTags2() {
        String[] tags = {"html", "body", "div", "/div", "/body", "/html"};
        Assert.assertTrue(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testNotWellFormedTags1() {
        String[] tags = {"html", "body", "/html", "/body"};
        Assert.assertFalse(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testNotWellFormedTags2() {
        String[] tags = {"html", "body", "div", "/body", "/div", "/html"};
        Assert.assertFalse(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testNotWellFormedTags3() {
        String[] tags = {"/html", "html", "body", "/body"};
        Assert.assertFalse(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testEmptyTags() {
        String[] tags = {};
        Assert.assertTrue(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testSingleOpenTag() {
        String[] tags = {"html"};
        Assert.assertFalse(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testSingleCloseTag() {
        String[] tags = {"/html"};
        Assert.assertFalse(WellFormedHTML.isWellFormed(tags));
    }

    @Test
    public void testNestedTags() {
        String[] tags = {"html", "head", "/head", "body", "div", "/div", "/body", "/html"};
        Assert.assertTrue(WellFormedHTML.isWellFormed(tags));
    }
}
