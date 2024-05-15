package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import junit.extensions.RepeatedTest;
import junit.framework.TestSuite;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BibTeXTest {

    Settings settings;
    TasksData tasksData;
    SingleFrameApplication singleFrameApplication;
    ZettelkastenView zknFrame;
    BibTeX bibTeX;

    public BibTeXTest() throws UnsupportedLookAndFeelException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    }

    public static junit.framework.Test suite() {
        return new RepeatedTest(new TestSuite(BibTeXTest.class), 1);
    }

    @Before
    public void setUp() throws Exception {
        // TODO: use TestObjectFactory ?
        
        this.settings = new Settings();
        this.tasksData = new TasksData();
        this.singleFrameApplication = new SingleFrameApplication() {
            @Override
            protected void startup() {

            }
        };
        this.zknFrame = new ZettelkastenView(
                this.singleFrameApplication,
                this.settings,
                this.tasksData);
        this.bibTeX = new BibTeX(this.zknFrame, this.settings);
    }

    @Test
    public void testGetFormattedEntry() {
        bibTeX.getFormattedEntry(bibTeX.getEntry(1), true);
    }

    @Test
    public void testDocument() {
        Document d = new Document("a", "t", "y");
        assertEquals("a", d.getAuthor());
        assertEquals("t", d.getTitle());
        assertEquals("y", d.getYear());
    }

    @Test
    public void testEmptyResult() {
        Result r = new Result();
        Assert.assertEquals("count=0 for empty result", 0, r.getCount());
    }

    @Test
    public void testResultWithTwoDocuments() {
        Document d1 = new Document("a1", "t1", "y1");
        Document d2 = new Document("a2", "t2", "y2");
        Result r = new Result(new Document[]{d1, d2});
        assert (r.getCount() == 2);
        assert (r.getItem(0) == d1);
        assert (r.getItem(1) == d2);
    }

}