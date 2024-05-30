package de.danielluedecke.zettelkasten.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.*;

public class UI {

    /**
     * * Use the event thread to show a frame. When this method has * returned
     * the frame will be showing and to the front.
     */
    public static void showFrame(final JFrame frame) {
        runInEventThread(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
                frame.toFront();
            }
        });
    }

    public static void runInEventThread(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
                new Robot().waitForIdle();
            } catch (Exception e) {
                e.printStackTrace();
                assert false : e.getMessage();
            }
        }
    }

}
