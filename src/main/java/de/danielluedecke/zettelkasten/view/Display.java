package de.danielluedecke.zettelkasten.view;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.data.History;
import ch.dreyeck.zettelkasten.xml.Zettel;

public class Display {
    private ZettelkastenView zettelkastenView;
    private History history;

    public Display(ZettelkastenView zettelkastenView, History history) {
        this.zettelkastenView = zettelkastenView;
        this.history = history;
    }

    public Zettel getDisplayedZettel() {
        Zettel zettel = zettelkastenView.getDisplayedZettel();
        if (zettel != null) {
            int entryNr = zettelkastenView.getDisplayedEntryNumber();
            history.addToHistory(entryNr);
        }
		return zettel;
    }
}
