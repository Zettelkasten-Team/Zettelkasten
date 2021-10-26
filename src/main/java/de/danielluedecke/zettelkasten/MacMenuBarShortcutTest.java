package de.danielluedecke.zettelkasten;

import com.apple.eawt.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MacMenuBarShortcutTest {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                showUI();
            }
        });
    }

    private static void showUI(){
        JFrame testFrame = new JFrame("TestFrame");

        JLabel content = new JLabel("Press cmd-t to test whether the action is triggered");
        testFrame.getContentPane().add(content);

        JMenuBar menuBar = new JMenuBar();

        testFrame.setJMenuBar(menuBar);

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "It works!");
            }
        };
        action.putValue(Action.NAME, "Test action");
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenu menu = new JMenu("Menu");
        menu.add(new JMenuItem(action));
        menuBar.add(menu);

        Application.getApplication().setDefaultMenuBar(menuBar);

        testFrame.setVisible(true);
        testFrame.pack();
        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}