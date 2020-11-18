package de.danielluedecke.zettelkasten;

import junit.framework.TestCase;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SwingTestCase extends TestCase {
    private JFrame testFrame;

    @Override
    protected void tearDown() {
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
