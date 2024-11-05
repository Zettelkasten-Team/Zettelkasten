package ch.dreyeck.zettelkasten.service;

import ch.dreyeck.zettelkasten.xml.Zettel;

import java.util.List;

public class ZettelService {
    private Zettel zettel;

    public ZettelService(Zettel zettel) {
        this.zettel = zettel;
    }

    // Add a link to the Zettel if it's not empty or null
    public boolean addLink(String link) {
        if (link == null || link.isEmpty()) {
            return false;  // Failed to add the link
        }
        List<String> links = zettel.getLinks().getLink();
        if (!links.contains(link)) {
            links.add(link);
            return true;  // Successfully added the link
        }
        return false;  // Link was already present
    }

    // Remove a link from the Zettel if it exists
    public boolean removeLink(String link) {
        List<String> links = zettel.getLinks().getLink();
        if (links.contains(link)) {
            links.remove(link);
            return true;  // Successfully removed the link
        }
        return false;  // Link not found
    }

    // Retrieve links from the Zettel
    public List<String> getLinks() {
        return zettel.getLinks().getLink();
    }
}
