package ch.dreyeck.zettelkasten.box;

import ch.dreyeck.zettelkasten.attachments.AttachmentsView;
import ch.dreyeck.zettelkasten.service.ZettelService;
import ch.dreyeck.zettelkasten.xml.Zettel;
import de.danielluedecke.zettelkasten.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ZettelController {
    private Zettel selectedZettel;
    private AttachmentsView view;  // Reference to the view that displays attachments
    private ZettelService zettelService;  // Service layer for managing Zettel data

    // Constructor to initialize with a Zettel and its view
    public ZettelController(Zettel selectedZettel, AttachmentsView view) {
        this.selectedZettel = selectedZettel;
        this.view = view;
        this.zettelService = new ZettelService(selectedZettel);  // Initialize with selected Zettel
    }

    // Method to delete an attachment
    public void deleteAttachment(String attachmentValue) {
        if (zettelService.removeLink(attachmentValue)) {
            Constants.zknlogger.info("Deleted attachment: " + attachmentValue);
            refreshView();
        } else {
            Constants.zknlogger.info("Attachment not found: " + attachmentValue);
        }
    }

    // Method to add an attachment
    public void addAttachment(String attachmentValue) {
        if (zettelService.addLink(attachmentValue)) {
            Constants.zknlogger.info("Added attachment: " + attachmentValue);
            refreshView();
        } else {
            Constants.zknlogger.warning("Cannot add an empty attachment");
        }
    }

    /**
     * Gets the list of attachments for the selected Zettel.
     *
     * @return A list of attachment contents.
     */
    public List<String> getAttachments(Zettel zettel) {
        List<String> attachments = new ArrayList<>();

        if (zettel.getLinks() != null && zettel.getLinks().getLink() != null) {
            for (String attachment : zettel.getLinks().getLink()) {
                if (!attachments.contains(attachment)) { // Ensure uniqueness
                    attachments.add(attachment);
                }
            }
        }

        return attachments;
    }

    /**
     * Gets the list of attachments for the selected Zettel.
     *
     * @return A list of attachment contents.
     */
    public List<String> getAttachments() {
        return getAttachments(selectedZettel); // Call the private method with the selectedZettel
    }

    // Refreshes the view to display updated attachment list
    private void refreshView() {
        if (view != null) {
            view.updateAttachments(getAttachments());
        }
    }

    // Setter to allow switching the selected Zettel, if needed
    public void setSelectedZettel(Zettel newZettel) {
        this.selectedZettel = newZettel;
        this.zettelService = new ZettelService(newZettel);  // Update the service with the new Zettel
        refreshView();  // Refresh view to show attachments for the new Zettel
    }
}
