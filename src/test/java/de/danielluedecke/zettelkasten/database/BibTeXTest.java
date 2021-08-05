package de.danielluedecke.zettelkasten.database;

import bibtex.dom.BibtexEntry;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import junit.extensions.RepeatedTest;
import junit.framework.TestSuite;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;

public class BibTeXTest {

    public BibTeXTest() throws UnsupportedLookAndFeelException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    }

    public static junit.framework.Test suite() {
        return new RepeatedTest(new TestSuite(BibTeXTest.class), 1);
    }

    // TODO: maybe we should use the TestObjectFactory here.
    AcceleratorKeys acceleratorKeys = new AcceleratorKeys();
    AutoKorrektur autoKorrektur = new AutoKorrektur();
    Synonyms synonyms = new Synonyms();
    StenoData stenoData = new StenoData();
    Settings s = new Settings(acceleratorKeys, autoKorrektur, synonyms, stenoData);
    TasksData tasksData = new TasksData();

    SingleFrameApplication singleFrameApplication = new SingleFrameApplication() {
        @Override
        protected void startup() {

        }
    };

    ZettelkastenView zknFrame = new ZettelkastenView(
            singleFrameApplication,
            s,
            acceleratorKeys,
            autoKorrektur,
            synonyms,
            stenoData,
            tasksData);

    BibTeX bibTeX = new BibTeX(zknFrame, s);

    @Before
    public void setUp() throws Exception {
        AcceleratorKeys acceleratorKeys = new AcceleratorKeys();
        AutoKorrektur autoKorrektur = new AutoKorrektur();
        Synonyms synonyms = new Synonyms();
        StenoData stenoData = new StenoData();
        Settings s = new Settings(acceleratorKeys, autoKorrektur, synonyms, stenoData);
        TasksData tasksData = new TasksData();
    }

    @Test
    public void testGetEntry() {
        BibtexEntry entry = bibTeX.getEntry(1);
    }


}