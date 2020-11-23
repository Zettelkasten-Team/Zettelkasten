package de.danielluedecke.zettelkasten;


import junit.extensions.RepeatedTest;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

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
        this.emptyFrame = new EditorFrame(null, null, null, null, null, null, null, null, null, false, 0, false, false);

        getTestFrame().getContentPane().add(this.emptyFrame, BorderLayout.CENTER);
        getTestFrame().pack();
        getTestFrame().setVisible(true);
    }

    @Test
    public void testContentPaneShouldBeEnabled() {
        assertTrue("Content pane should be enabled",
                this.emptyFrame.getContentPane().isEnabled());
    }

    @After
    public void tearDown() throws Exception {
    }
}