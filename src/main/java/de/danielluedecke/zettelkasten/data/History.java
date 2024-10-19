package de.danielluedecke.zettelkasten.data;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.history.NavigationListener;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.view.Display;

import java.util.logging.Level;

/**
 * Manages the history of entries in the program.
 */
public class History implements NavigationListener {
	private static final int HISTORY_MAX = 100; // Adjust as needed
	private static int[] history;
	private static int historyPosition;
	private int historyCount;
	private int activatedEntryNumber;
	private int[] displayedEntries;
	private int displayedCount;

	// Constructor without parameters
	public History() {
		this.history = new int[HISTORY_MAX];
		this.displayedEntries = new int[HISTORY_MAX]; // Initialize displayedEntries array
		this.historyPosition = -1;
		this.historyCount = 0;
		this.displayedCount = 0; // Initialize displayedCount
	}

	public History(Display display) {
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
	    // Log the current history before adding the new entry
	    logCurrentHistory();

	    // Avoid duplicates in history
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

	    // Log the added entry to history
	    Constants.zknlogger.info("Added to history: " + entryNr);
	}

	/**
	 * Logs the current history.
	 */
	public static void logCurrentHistory() {
	    StringBuilder historyBuilder = new StringBuilder("Current history: [");
	    for (int i = 0; i <= historyPosition; i++) {
	        historyBuilder.append(history[i]);
	        if (i < historyPosition) {
	            historyBuilder.append(", ");
	        }
	    }
	    historyBuilder.append("]");
	    Constants.zknlogger.info(historyBuilder.toString());
	}


	/**
	 * Checks if history back navigation is possible.
	 * 
	 * @return {@code true} if history back navigation is enabled, {@code false}
	 *         otherwise
	 */
	public boolean canHistoryBack() {
		return (historyPosition > 0);
	}

	/**
	 * Checks if history forward navigation is possible.
	 * 
	 * @return {@code true} if history forward navigation is enabled, {@code false}
	 *         otherwise
	 */
	public boolean canHistoryForward() {
		return (historyPosition >= 0 && historyPosition < (historyCount - 1));
	}

	/**
	 * Moves back through the history and returns the activated entry number.
	 * 
	 * @return the activated entry number after navigating back in history
	 */
	public int historyBack() {
		logCurrentHistory();
		if (canHistoryBack()) {
			activatedEntryNumber = history[--historyPosition];
		}
		Constants.zknlogger.log(Level.INFO, "Activated entry number:", String.valueOf(activatedEntryNumber));
		return activatedEntryNumber;
	}

	/**
	 * Moves forward through the history and returns the activated entry number.
	 * 
	 * @return the activated entry number after navigating forward in history
	 */
	public int historyForward() {
		logCurrentHistory();
		if (canHistoryForward()) {
			activatedEntryNumber = history[++historyPosition];
		}
		Constants.zknlogger.log(Level.INFO, "Activated entry number:", String.valueOf(activatedEntryNumber));
		return activatedEntryNumber;
	}

	@Override
	public int navigateForwardInHistory() {
		return historyForward();
	}

	@Override
	public void navigateBackwardInHistory() {
		historyBack();
	}

	public void updateHistory(ZettelkastenView zettelkastenView, int inputDisplayedEntry) {
		zettelkastenView.data.addToHistory(inputDisplayedEntry);
		// Update buttons for navigating through history.
		zettelkastenView.buttonHistoryBack.setEnabled(zettelkastenView.data.canHistoryBack());
		zettelkastenView.buttonHistoryForward.setEnabled(zettelkastenView.data.canHistoryForward());
	}
}