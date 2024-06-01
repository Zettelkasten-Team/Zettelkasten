package de.danielluedecke.zettelkasten.history;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.Constants;

/**
 * Manages the history of entries in the program.
 */
public class HistoryManager implements HistoryNavigationListener {
    private final Daten data;
    private static final int HISTORY_MAX = 100; // Adjust as needed
    private int[] history;
    private int historyPosition;
    private int historyCount;
    private int activatedEntryNumber;

    public HistoryManager(Daten data) {
        this.data = data;
        this.history = new int[HISTORY_MAX];
        this.historyPosition = -1; // Initialize to -1 to indicate no history yet
        this.historyCount = 0;
    }

    /**
     * Adds the given entry number to the history.
     * 
     * @param entryNr the number of the entry to be added to the history
     */
    public void addToHistory(int entryNr) {
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
        Constants.zknlogger.info("Added to history: " + entryNr);
        data.displayHistory(history, historyCount);
    }

    /**
     * Checks if history back navigation is possible.
     * 
     * @return {@code true} if history back navigation is enabled, {@code false} otherwise
     */
    public boolean canHistoryBack() {
        return (historyPosition > 0);
    }

    /**
     * Checks if history forward navigation is possible.
     * 
     * @return {@code true} if history forward navigation is enabled, {@code false} otherwise
     */
    public boolean canHistoryFore() {
        return (historyPosition >= 0 && historyPosition < (historyCount - 1));
    }

    /**
     * Moves back through the history and returns the activated entry number.
     * 
     * @return the activated entry number after navigating back in history
     */
    public int historyBack() {
        if (canHistoryBack()) {
            activatedEntryNumber = history[--historyPosition];
        }
        return activatedEntryNumber;
    }

    /**
     * Moves forward through the history and returns the activated entry number.
     * 
     * @return the activated entry number after navigating forward in history
     */
    public int historyFore() {
        if (canHistoryFore()) {
            activatedEntryNumber = history[++historyPosition];
        }
        return activatedEntryNumber;
    }

    @Override
    public void navigateForwardInHistory() {
        historyFore();
    }

	@Override
	public void navigateBackwardInHistory() {
		historyBack();		
	}
}
