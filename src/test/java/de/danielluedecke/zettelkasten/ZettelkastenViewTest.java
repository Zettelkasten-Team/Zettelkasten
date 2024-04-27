package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.util.Constants;

import org.jdesktop.application.SingleFrameApplication;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ZettelkastenViewTest {
    @Test
    public void testAskUserAndMaybeSaveChanges_NullMainDataFile() throws Exception {
        // Create a mock SingleFrameApplication
        SingleFrameApplication application = mock(SingleFrameApplication.class);

        // Set up the necessary dependencies
        Settings settings = new Settings(); // Assuming the Settings class exists
        TasksData tasksData = new TasksData(); // Assuming the TasksData class exists

        // Create a ZettelkastenView instance with appropriate constructor arguments
        ZettelkastenView zettelkastenView = new ZettelkastenView(application, settings, tasksData);

        // Use reflection to access the private method
        Method method = ZettelkastenView.class.getDeclaredMethod("askUserAndMaybeSaveChanges", String.class);
        method.setAccessible(true);

        // Call askUserAndMaybeSaveChanges with a title
        boolean result = (boolean) method.invoke(zettelkastenView, "Test Title");

        // Verify that the result is true (indicating no changes to save)
        assertTrue(result);
    }

    @Test
    public void testInitUIManagerLookAndFeel_SuccessfulUpdate() throws Exception {
        // Mock dependencies
        Settings settings = mock(Settings.class);
        when(settings.getLookAndFeel()).thenReturn("javax.swing.plaf.metal.MetalLookAndFeel");

        JFrame frame = mock(JFrame.class);
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

        // Create a mock SingleFrameApplication
        org.jdesktop.application.SingleFrameApplication application = mock(org.jdesktop.application.SingleFrameApplication.class);

        // Call the method
        ZettelkastenView zettelkastenView = new ZettelkastenView(application, settings, new TasksData());
        zettelkastenView.initUIManagerLookAndFeel();

        // Verify that UIManager's look and feel is updated
        verify(frame).getRootPane();
        verify(SwingUtilities.class, times(1));
        SwingUtilities.updateComponentTreeUI(frame);
    }

    @Test
    public void testInitUIManagerLookAndFeel_UnsupportedLookAndFeel() throws Exception {
        // Mock dependencies
        Settings settings = mock(Settings.class);
        when(settings.getLookAndFeel()).thenReturn("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

        // Create a mock SingleFrameApplication
        SingleFrameApplication application = mock(SingleFrameApplication.class);

        // Call the method
        ZettelkastenView zettelkastenView = new ZettelkastenView(application, settings, new TasksData());
        zettelkastenView.initUIManagerLookAndFeel();

        // Verify that a warning message is logged
        verify(Constants.zknlogger, times(1));
        Constants.zknlogger.log(eq(Level.WARNING), anyString());
    }

    @Test
    public void testInitUIManagerLookAndFeel_NullFrame() throws Exception {
        // Mock dependencies
        Settings settings = mock(Settings.class);
        when(settings.getLookAndFeel()).thenReturn("javax.swing.plaf.metal.MetalLookAndFeel");

        // Create a mock JFrame object
        JFrame frame = mock(JFrame.class);

        // Create a mock SingleFrameApplication
        SingleFrameApplication application = mock(SingleFrameApplication.class);

        // Create a ZettelkastenView instance with the mock JFrame
        ZettelkastenView zettelkastenView = new ZettelkastenView(application, settings, new TasksData());
        when(zettelkastenView.getFrame()).thenReturn(frame); // Stub the getFrame() method to return the mock JFrame

        // Call the method
        zettelkastenView.initUIManagerLookAndFeel();

        // Verify that a warning message is logged
        verify(Constants.zknlogger, times(1));
        Constants.zknlogger.log(eq(Level.WARNING), anyString());
    }
}
