package de.danielluedecke.zettelkasten;


import de.danielluedecke.zettelkasten.database.*;
import junit.extensions.RepeatedTest;
import junit.framework.TestSuite;
import org.jdesktop.application.SingleFrameApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

public class EditorFrameTest extends SwingTestCase {
    private EditorFrame emptyFrame;

    public EditorFrameTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return new RepeatedTest(new TestSuite(EditorFrameTest.class), 1);
    }

    @Before
    public void setUp() throws Exception {

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

        BibTeX bibTex = new BibTeX(zknFrame, s);

        this.emptyFrame = new EditorFrame(zknFrame,
                new Daten(zknFrame, s, synonyms, bibTex),
                tasksData,
                acceleratorKeys,
                s,
                autoKorrektur,
                synonyms,
                stenoData,
                "",
                false,
                0,
                false,
                false);

        getTestFrame().getContentPane().add(this.emptyFrame.getRootPane(), BorderLayout.CENTER);
        getTestFrame().pack();
        getTestFrame().setVisible(true);

    }

    @Test
    public void testContentPaneShouldBeEnabled() {
        assertTrue("Content pane should be enabled",
                this.emptyFrame.getContentPane().getComponent(0).isEnabled());
    }

    @After
    public void tearDown() throws Exception {
    }
}
