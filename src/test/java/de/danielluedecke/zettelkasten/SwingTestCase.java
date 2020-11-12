package de.danielluedecke.zettelkasten;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

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
    
    @Before
    public JFrame getTestFrame() {
        if (this.testFrame == null) {
            this.testFrame = new JFrame("Test");
    }
        final JFrame testFrame = this.testFrame;
        return testFrame;
    }
}
