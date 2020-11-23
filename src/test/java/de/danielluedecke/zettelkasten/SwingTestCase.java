package de.danielluedecke.zettelkasten;


import junit.framework.TestCase;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SwingTestCase extends TestCase {
    private JFrame testFrame;

    public SwingTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        if (this.testFrame != null) {
            this.testFrame.dispose();
            this.testFrame = null;
        }
    }

    public JFrame getTestFrame() {
        if (this.testFrame == null) {
            this.testFrame = new JFrame("Test");
        }
        return this.testFrame;
    }

    public void waitForSwing() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                });
            } catch (InterruptedException e) {
            } catch (InvocationTargetException e) {
            }
        }
    }
}
