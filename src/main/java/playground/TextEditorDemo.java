package playground;

import javax.swing.*;

public class TextEditorDemo {
    public static void main(String[] args) {
        // Create a JFrame to host the TextEditor
        JFrame frame = new JFrame("Text Editor Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create an instance of the TextEditor
        TextEditor textEditor = new TextEditor();
        
        // Add the TextEditor to the JFrame
        frame.getContentPane().add(textEditor);
        
        // Set JFrame size and make it visible
        frame.setSize(400, 300);
        frame.setVisible(true);
    }
}
