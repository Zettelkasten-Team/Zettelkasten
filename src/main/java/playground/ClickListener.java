package playground;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JTextArea;

public class ClickListener implements ActionListener {
    private JTextArea logArea;

    public ClickListener(JTextArea logArea) {
        this.logArea = logArea;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String logMessage;
        if (source instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) source;
            logMessage = "Click event on button: " + button.getText();
        } else if (source instanceof JTextField) {
            JTextField textField = (JTextField) source;
            logMessage = "Action event on text field: " + textField.getText();
        } else if (source instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) source;
            logMessage = "Click event on menu item: " + menuItem.getText();
        } else {
            logMessage = "Click event on unknown component";
        }
        logArea.append(logMessage + "\n");
    }
}
