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
        frame.setSize(800, 600);

        // Log area to display click events
        logArea = new JTextArea(15, 70);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Example interactive components
        JButton button1 = new JButton("New Note");
        JButton button2 = new JButton("Save Note");
        JTextField textField = new JTextField("Enter text here", 20);
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuItem1 = new JMenuItem("Open");
        JMenuItem menuItem2 = new JMenuItem("Close");

        // Add components to the menu
        menu.add(menuItem1);
        menu.add(menuItem2);
        menuBar.add(menu);

        // Layout setup
        JPanel panel = new JPanel();
        panel.add(button1);
        panel.add(button2);
        panel.add(textField);

        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(logScrollPane, BorderLayout.CENTER);

        // Recursively add ClickListener to all interactive components
        ClickLogger.ClickListener clickListener = new ClickLogger.ClickListener(logArea);
        ComponentUtils.addClickListenerToAllComponents(frame, clickListener);
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ZettelkastenApp app = new ZettelkastenApp();
            app.show();
        });
    }
}