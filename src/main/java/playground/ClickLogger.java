package playground;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class ClickLogger {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Click Logger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        
        JPanel panel = new JPanel();
        
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        
        JTextArea logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        
        button1.addActionListener(new ClickListener(logArea));
        button2.addActionListener(new ClickListener(logArea));
        
        panel.add(button1);
        panel.add(button2);
        
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        frame.setVisible(true);
    }
    
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
                logMessage = "Click event on " + button.getText();
            } else {
                logMessage = "Click event on unknown component";
            }
            logArea.append(logMessage + "\n");
        }
    }
}

