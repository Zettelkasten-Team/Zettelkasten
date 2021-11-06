package de.danielluedecke.zettelkasten.database;

import bibtex.dom.BibtexEntry;
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

    AcceleratorKeys acceleratorKeys;
    AutoKorrektur autoKorrektur;
    Synonyms synonyms;
    StenoData stenoData;
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
        this.acceleratorKeys = new AcceleratorKeys();
        this.autoKorrektur = new AutoKorrektur();
        this.synonyms = new Synonyms();
        this.stenoData = new StenoData();
        this.settings = new Settings(this.acceleratorKeys, this.autoKorrektur, this.synonyms, this.stenoData);
        this.tasksData = new TasksData();
        this.singleFrameApplication = new SingleFrameApplication() {
            @Override
            protected void startup() {

            }
        };
        this.zknFrame = new ZettelkastenView(
                this.singleFrameApplication,
                this.settings,
                this.acceleratorKeys,
                this.autoKorrektur,
                this.synonyms,
                this.stenoData,
                this.tasksData);
        this.bibTeX = new BibTeX(this.zknFrame, this.settings);
    }

    @Test
    public void testGetEntry() {
        BibtexEntry entry = bibTeX.getEntry(1);
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

}