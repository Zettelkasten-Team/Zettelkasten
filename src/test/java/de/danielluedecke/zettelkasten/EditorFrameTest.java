package de.danielluedecke.zettelkasten;


import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class EditorFrameTest extends SwingTestCase {

    private EditorFrame emptyEditorFrame;

    public EditorFrameTest(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        this.emptyEditorFrame = new EditorFrame(null, null, null, null, null, null, null, null, "", false, 0, false, false);

        getTestFrame().getContentPane().add(this.emptyEditorFrame, BorderLayout.CENTER);
        getTestFrame().pack();
        getTestFrame().setVisible(true);
    }

    @Test
    public void testContentPaneShouldBeEnabled() {
        assertTrue("Content pane should be enabled",
                this.emptyEditorFrame.getContentPane().isEnabled());
    }

    @After
    public void tearDown() throws Exception {
    }
}