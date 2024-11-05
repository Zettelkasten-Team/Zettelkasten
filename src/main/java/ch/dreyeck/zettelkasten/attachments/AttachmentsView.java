package ch.dreyeck.zettelkasten.attachments;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AttachmentsView extends JPanel {
    private JList<String> attachmentList;  // To display attachment values
    private DefaultListModel<String> listModel;

    public AttachmentsView() {
        setLayout(new BorderLayout());

        // Initialize the list model and the JList
        listModel = new DefaultListModel<>();
        attachmentList = new JList<>(listModel);
        attachmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add the JList to a scroll pane and add to the panel
        JScrollPane scrollPane = new JScrollPane(attachmentList);
        add(scrollPane, BorderLayout.CENTER);

        // Optional: Add any other UI elements like buttons here
    }

    // Update method to refresh the list of attachments
    public void updateAttachments(List<String> attachments) {
        listModel.clear();
        for (String attachment : attachments) {
            listModel.addElement(attachment);
        }
    }

    // Getter for selected attachment value (if needed)
    public String getSelectedAttachment() {
        return attachmentList.getSelectedValue();
    }
}

