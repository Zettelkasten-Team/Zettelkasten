package de.danielluedecke.zettelkasten;

import junit.framework.TestCase;
import org.junit.After;

import javax.swing.JFrame;

public class SwingTestCase extends TestCase {
    private JFrame testFrame = null;

    @After
    public void tearDown() throws Exception {
        if (this.testFrame != null) {
            this.testFrame.dispose( );
            this.testFrame = null;
    }
    }
    public JFrame getTestFrame( ) {
        if (this.testFrame == null) {
            this.testFrame = new JFrame("Test");
    }
        return this.testFrame;
    }
}
