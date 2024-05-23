package playground;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ZettelkastenApp {
    private JFrame frame;
    private JTextArea logArea;

    public ZettelkastenApp() {
        frame = new JFrame("Zettelkasten");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Log area to display click events
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Example interactive components
        JButton button1 = new JButton("New Note");
        JButton button2 = new JButton("Save Note");
        JTextField textField = new JTextField("Enter text here", 20);

        // Add ClickListener to components
        button1.addActionListener(new ClickListener(logArea));
        button2.addActionListener(new ClickListener(logArea));
        textField.addActionListener(new ClickListener(logArea));

        // Layout setup
        JPanel panel = new JPanel();
        panel.add(button1);
        panel.add(button2);
        panel.add(textField);

        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(logScrollPane, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }

    // ClickListener class
    static class ClickListener implements ActionListener {
        private JTextArea logArea;

        public ClickListener(JTextArea logArea) {
            this.logArea = logArea;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String logMessage;
            if (source instanceof JButton) {
                JButton button = (JButton) source;
                logMessage = "Click event on button: " + button.getText();
            } else if (source instanceof JTextField) {
                JTextField textField = (JTextField) source;
                logMessage = "Action event on text field: " + textField.getText();
            } else {
                logMessage = "Click event on unknown component";
            }
            logArea.append(logMessage + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ZettelkastenApp app = new ZettelkastenApp();
            app.show();
        });
    }
}

