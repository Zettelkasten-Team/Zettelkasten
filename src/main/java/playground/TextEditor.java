package playground;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextEditor extends JPanel {
    private JTextArea textArea;

    public TextEditor() {
        // Initialize the text area
        textArea = new JTextArea(10, 40);

        // Add a document listener to track text changes
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Handle text insertions
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Handle text removals
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Handle attribute changes
                textChanged();
            }
        });

        // Add the text area to the panel
        add(new JScrollPane(textArea));
    }

    // Method to handle text changes
    private void textChanged() {
        // Implement your logic here, such as updating UI or saving text
        System.out.println("Text changed: " + getText());
    }

    // Method to set text in the editor
    public void setText(String text) {
        textArea.setText(text);
    }

    // Method to retrieve text from the editor
    public String getText() {
        return textArea.getText();
    }

    // Example of additional method for clearing text
    public void clearText() {
        textArea.setText("");
    }

    // Example of additional method for disabling/enabling editor
    public void setEditable(boolean editable) {
        textArea.setEditable(editable);
    }

    // Other methods for customizing editor functionality can be added as needed
}
    