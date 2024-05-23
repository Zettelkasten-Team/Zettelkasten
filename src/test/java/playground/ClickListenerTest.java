package playground;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.testng.Assert.assertTrue;

public class ClickListenerTest {
    private JTextArea logArea;
    private ClickListener clickListener;

    @BeforeMethod
    public void setUp() {
        logArea = new JTextArea();
        clickListener = new ClickListener(logArea);
    }

    @Test
    public void testButtonClick() {
        JButton button = new JButton("Test Button");
        button.addActionListener(clickListener);

        // Simulate button click
        button.doClick();

        // Verify log message
        String logContent = logArea.getText();
        assertTrue(logContent.contains("Click event on button: Test Button"));
    }

    @Test
    public void testTextFieldAction() {
        JTextField textField = new JTextField("Test TextField");
        textField.addActionListener(clickListener);

        // Simulate text field action
        ActionEvent event = new ActionEvent(textField, ActionEvent.ACTION_PERFORMED, null);
        clickListener.actionPerformed(event);

        // Verify log message
        String logContent = logArea.getText();
        assertTrue(logContent.contains("Action event on text field: Test TextField")); //FIXME JMenuItem is a AbstractButton as well
    }

    @Test
    public void testMenuItemClick() {
        JMenuItem menuItem = new JMenuItem("Test MenuItem");
        menuItem.addActionListener(clickListener);

        // Simulate menu item click
        menuItem.doClick();

        // Verify log message
        String logContent = logArea.getText();
        assertTrue(logContent.contains("Click event on menu item: Test MenuItem"));
    }
}
