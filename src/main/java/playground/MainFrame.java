package playground;
import javax.swing.*;

public class MainFrame extends JFrame {
    private JButton button;

    public MainFrame() {
        button = new JButton("Click me");

        // Add the button to the frame
        add(button);

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateUI() {
        // Update the UI
        SwingUtilities.updateComponentTreeUI(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            // Simulate a look and feel change
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Update the UI after changing the look and feel
            frame.updateUI();
        });
    }
}
