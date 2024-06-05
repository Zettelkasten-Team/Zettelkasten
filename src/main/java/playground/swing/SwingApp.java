package playground.swing;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SwingApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Application");
        JPanel panel = new JPanel();
        JButton button = new JButton("Click Me");

        button.addActionListener(e -> System.out.println("Button Clicked"));

        panel.add(button);
        frame.add(panel);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
