package de.danielluedecke.zettelkasten.history;

import de.danielluedecke.zettelkasten.database.Daten;

public class HistoryManager {
	private final Daten data;

	private static final int HISTORY_MAX = 100; // Adjust as needed
	private int[] history;
	private int historyPosition;

	public HistoryManager(Daten data) {
		this.data = data;
		this.history = new int[HISTORY_MAX];
	}

	/**
	 * This method adds the {@code entryNr} to the history, 
	 * so the user can go back and fore to previous selected entries.
	 *
	 * @param entryNr the number of the entry that should be added to the history
	 */
	public void addToHistory(int entryNr) {
		// when the last history-entry equals the current entry, don't add
		// that to the history, so we don't have the same entry several times
		if (history[historyPosition] == entryNr) {
			return;
		}
		// when we reached the end of the array, rotate it...
		if (historyPosition >= (HISTORY_MAX - 1)) {
			// go through history array...
			// copy the next element the previous position
			for (int cnt = 0; cnt < (HISTORY_MAX - 1); cnt++) {
				history[cnt] = history[cnt + 1];
			}
			// add new value to history
			history[HISTORY_MAX - 1] = entryNr;
			historyPosition = HISTORY_MAX - 1;
		} else {
			// in any other case, simply increase the history counter
			historyPosition++;
			// add the new value
			history[historyPosition] = entryNr;
		}
	}
	
	public boolean historyBack() {
		if (data.canGoBackInHistory()) {
			data.historyBack();
			return true;
		}
		return false;
	}


}
