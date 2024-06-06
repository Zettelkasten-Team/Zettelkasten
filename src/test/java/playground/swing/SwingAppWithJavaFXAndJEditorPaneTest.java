package playground.swing;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.swing.*;
import javafx.embed.swing.JFXPanel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SwingAppWithJavaFXAndJEditorPaneTest {

    private JFrame frame;
    private JPanel panel;
    private JButton swingButton;
    private JEditorPane jEditorPaneEntry;
    private JFXPanel jfxPanel;

    @BeforeClass
    public void setUp() {
    	
        if (System.getenv("DISPLAY") == null) {
            throw new SkipException("Skipping GUI tests as no DISPLAY is set");
        }
        
        // Initialize the application components
        frame = new JFrame("Swing and JavaFX Application");
        panel = new JPanel();
        swingButton = new JButton("Swing Button");
        jEditorPaneEntry = new JEditorPane();
        jEditorPaneEntry.setName("jEditorPaneEntry");
        jEditorPaneEntry.setText("This is a JEditorPane");
        jfxPanel = new JFXPanel();

        // Add components to the panel
        panel.add(swingButton);
        panel.add(jEditorPaneEntry);
        panel.add(jfxPanel);
        frame.add(panel);
    }

    @Test
    public void testSwingButtonCreation() {
        // Test if Swing button is created and has correct properties
        assertNotNull(swingButton, "Swing button should not be null");
        assertEquals(swingButton.getText(), "Swing Button", "Swing button text should be 'Swing Button'");
    }

    @Test
    public void testJEditorPaneCreation() {
        // Test if JEditorPane is created and has correct properties
        assertNotNull(jEditorPaneEntry, "JEditorPane should not be null");
        assertEquals(jEditorPaneEntry.getName(), "jEditorPaneEntry", "JEditorPane should have name 'jEditorPaneEntry'");
        assertEquals(jEditorPaneEntry.getText(), "This is a JEditorPane", "JEditorPane text should be 'This is a JEditorPane'");
    }

    @Test
    public void testJFXPanelCreation() {
        // Test if JFXPanel is created
        assertNotNull(jfxPanel, "JFXPanel should not be null");
    }

    @Test
    public void testJEditorPaneSearch() {
        // Test if JEditorPane can be found by name
        JEditorPane foundEditorPane = SwingAppWithJavaFX.findEditorPaneByName(panel, "jEditorPaneEntry");
        assertNotNull(foundEditorPane, "JEditorPane should be found");
        assertEquals(foundEditorPane, jEditorPaneEntry, "Found JEditorPane should be the same as the created one");
    }

}
