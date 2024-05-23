package playground;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ClickLogger {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Click Logger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");

        button1.addActionListener(new ClickListener("Button 1"));
        button2.addActionListener(new ClickListener("Button 2"));

        panel.add(button1);
        panel.add(button2);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    static class ClickListener implements ActionListener {
        private String componentName;

        public ClickListener(String componentName) {
            this.componentName = componentName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Click event on " + componentName);
            // You can log more information here if needed
        }
    }
}

