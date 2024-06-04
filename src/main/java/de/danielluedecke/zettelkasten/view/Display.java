package de.danielluedecke.zettelkasten.view;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import ch.dreyeck.zettelkasten.xml.Zettel;
import de.danielluedecke.zettelkasten.history.HistoryManager;

public class Display {
    private ZettelkastenView zettelkastenView;
    private HistoryManager historyManager;

    public Display(ZettelkastenView zettelkastenView, HistoryManager historyManager) {
        this.zettelkastenView = zettelkastenView;
        this.historyManager = historyManager;
    }

    public void getDisplayedZettel() {
        Zettel zettel = zettelkastenView.getDisplayedZettel();
        if (zettel != null) {
            historyManager.addToHistory(123); // Assuming 123 is the entryNr
        }
    }
}
