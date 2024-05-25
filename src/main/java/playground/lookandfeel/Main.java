package playground.lookandfeel;

import javax.swing.*;
import javax.swing.plaf.metal.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set the custom theme
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            MetalLookAndFeel.setCurrentTheme(new TestTheme());

            // Update the UI
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());

            // Create and display Swing application
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Custom Theme Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);

                JLabel label = new JLabel("This is a test label");
                frame.add(label);

                frame.setVisible(true);
            });
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
