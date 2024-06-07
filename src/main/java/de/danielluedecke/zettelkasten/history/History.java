package de.danielluedecke.zettelkasten.history;

import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.view.Display;
import ch.dreyeck.zettelkasten.xml.Zettel;

/**
 * Manages the history of entries in the program.
 */
public class History implements HistoryNavigationListener {
    private static final int HISTORY_MAX = 100; // Adjust as needed
    private int[] history;
    private int historyPosition;
    private int historyCount;
    private int activatedEntryNumber;
    private Display display;

    public History(Display display) {
        this.history = new int[HISTORY_MAX];
        this.historyPosition = -1; // Initialize to -1 to indicate no history yet
        this.historyCount = 0;
        this.display = display;
    }

    /**
     * Adds the given entry number to the history if a Zettel is displayed.
     * 
     * @param entryNr the number of the entry to be added to the history
     */
    public void addToHistory(int entryNr) {
        Zettel currentZettel = display.getDisplayedZettel();
        if (currentZettel == null) {
            Constants.zknlogger.info("No Zettel displayed. Entry not added to history: " + entryNr);
            return;
        }

        // Avoid duplicates
        if (historyPosition >= 0 && history[historyPosition] == entryNr) {
            return;
        }

        if (historyPosition < HISTORY_MAX - 1) {
            history[++historyPosition] = entryNr;
        } else {
            System.arraycopy(history, 1, history, 0, HISTORY_MAX - 1);
            history[HISTORY_MAX - 1] = entryNr;
            historyPosition = HISTORY_MAX - 1;
        }
        historyCount = Math.min(historyCount + 1, HISTORY_MAX);
        activatedEntryNumber = entryNr; // Update activated entry number
        Constants.zknlogger.info("Added to history: " + entryNr);
    }

    public boolean canHistoryBack() {
        return (historyPosition > 0);
    }

    public boolean canHistoryFore() {
        return (historyPosition >= 0 && historyPosition < (historyCount - 1));
    }

    public int historyBack() {
        if (canHistoryBack()) {
            activatedEntryNumber = history[--historyPosition];
        }
        return activatedEntryNumber;
    }

    public int historyFore() {
        if (canHistoryFore()) {
            activatedEntryNumber = history[++historyPosition];
        }
        return activatedEntryNumber;
    }

    @Override
    public int navigateForwardInHistory() {
        return historyFore();
    }

    @Override
    public void navigateBackwardInHistory() {
        historyBack();        
    }

    // Getters for testing purposes
    public int getHistoryCount() {
        return historyCount;
    }

    public int[] getHistory() {
        return history;
    }
}
