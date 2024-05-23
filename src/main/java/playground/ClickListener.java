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
        ComponentType componentType = getComponentType(source);
        String logMessage;

        switch (componentType) {
            case BUTTON:
                AbstractButton button = (AbstractButton) source;
                logMessage = "Click event on button: " + button.getText();
                break;
            case TEXT_FIELD:
                JTextField textField = (JTextField) source;
                logMessage = "Action event on text field: " + textField.getText();
                break;
            case MENU_ITEM:
                JMenuItem menuItem = (JMenuItem) source;
                logMessage = "Click event on menu item: " + menuItem.getText();
                break;
            case UNKNOWN:
            default:
                logMessage = "Click event on unknown component";
                break;
        }

        logArea.append(logMessage + "\n");
    }

    private ComponentType getComponentType(Object component) {
        if (component instanceof AbstractButton) {
            return ComponentType.BUTTON;
        } else if (component instanceof JTextField) {
            return ComponentType.TEXT_FIELD;
        } else if (component instanceof JMenuItem) {
            return ComponentType.MENU_ITEM;
        } else {
            return ComponentType.UNKNOWN;
        }
    }
}
