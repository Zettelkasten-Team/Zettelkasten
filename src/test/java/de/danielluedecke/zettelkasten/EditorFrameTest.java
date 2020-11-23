package de.danielluedecke.zettelkasten;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

public class EditorFrameTest extends SwingTestCase{

    public EditorFrameTest(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        EditorFrame emptyEditorFrame = new EditorFrame();
    }

    @After
    public void tearDown() throws Exception {
    }
}