package playground;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JMenuItem;

public class ComponentUtils {

    public static void addClickListenerToAllComponents(Container container, ActionListener listener) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).addActionListener(listener);
            } else if (component instanceof JTextField) {
                ((JTextField) component).addActionListener(listener);
            } else if (component instanceof JMenuItem) {
                ((JMenuItem) component).addActionListener(listener);
            }
            if (component instanceof Container) {
                addClickListenerToAllComponents((Container) component, listener);
            }
        }
    }
}
