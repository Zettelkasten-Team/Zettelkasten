package de.danielluedecke.zettelkasten.data;

import de.danielluedecke.zettelkasten.history.HistoryEvent;
import de.danielluedecke.zettelkasten.history.HistoryListener;
import de.danielluedecke.zettelkasten.util.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Pure model for managing entry-number history.
 * - No Swing/UI references
 * - Notifies listeners on activation changes
 * - Includes deprecated shims for legacy call sites (to be removed)
 */
public class History {

    private static final int DEFAULT_HISTORY_MAX = 100;

    private final int[] history;
    private int historyPosition = -1;   // index of active entry in history[], -1 = none
    private int historyCount = 0;       // number of valid items in history[]
    private int activatedEntryNumber = -1; // currently active entry number, -1 = none

    private final List<HistoryListener> listeners = new CopyOnWriteArrayList<>();

    public History() { this(DEFAULT_HISTORY_MAX); }

    public History(int capacity) {
        int cap = Math.max(1, capacity);
        this.history = new int[cap];
    }

    // ---------------- Listener API ----------------

    public void addListener(HistoryListener l) {
        if (l != null) listeners.add(l);
    }

    public void removeListener(HistoryListener l) {
        listeners.remove(l);
    }

    private void notifyActivated(int oldIdx, int newIdx, int oldEntry, int newEntry) {
        if (oldIdx == newIdx && oldEntry == newEntry) return; // no change
        HistoryEvent ev = new HistoryEvent(oldIdx, newIdx, oldEntry, newEntry);
        for (HistoryListener l : listeners) {
            l.onActivated(ev);
        }
    }

    // ---------------- Public API ----------------

    /** Adds the given entry number to history and activates it. */
    public void addToHistory(int entryNr) {
        logCurrentHistory("before addToHistory(" + entryNr + ")");

        // Avoid duplicate append if already the active one
        if (historyPosition >= 0 && historyPosition < historyCount && history[historyPosition] == entryNr) {
            Constants.zknlogger.info("Skipped addToHistory: same as current active " + entryNr);
            return;
        }

        int oldIdx   = historyPosition;
        int oldEntry = activatedEntryNumber;

        // If we are not at the end, truncate the "forward" branch
        if (historyPosition < historyCount - 1) {
            historyCount = historyPosition + 1;
        }

        if (historyCount < history.length) {
            // Append normally
            historyPosition = historyCount;
            history[historyPosition] = entryNr;
            historyCount++;
        } else {
            // Buffer full: shift left and put at the end
            System.arraycopy(history, 1, history, 0, history.length - 1);
            history[history.length - 1] = entryNr;
            historyPosition = history.length - 1;
            // historyCount stays at capacity
        }

        activatedEntryNumber = entryNr;

        Constants.zknlogger.info("Added to history: " + entryNr);
        logCurrentHistory("after addToHistory(" + entryNr + ")");

        notifyActivated(oldIdx, historyPosition, oldEntry, activatedEntryNumber);
    }

    /** @return true if a back navigation is possible. */
    public boolean canHistoryBack() {
        return historyPosition > 0;
    }

    /** @return true if a forward navigation is possible. */
    public boolean canHistoryForward() {
        return (historyPosition >= 0) && (historyPosition < (historyCount - 1));
    }

    /**
     * Move back in history; activates the previous entry if possible.
     * @return the activated entry number (or unchanged if no-op)
     */
    public int historyBack() {
        logCurrentHistory("before historyBack()");
        int oldIdx   = historyPosition;
        int oldEntry = activatedEntryNumber;

        if (canHistoryBack()) {
            historyPosition--;
            activatedEntryNumber = history[historyPosition];
            notifyActivated(oldIdx, historyPosition, oldEntry, activatedEntryNumber);
        }

        Constants.zknlogger.log(Level.INFO, "Activated entry number: {0}", activatedEntryNumber);
        logCurrentHistory("after historyBack()");
        return activatedEntryNumber;
    }

    /**
     * Move forward in history; activates the next entry if possible.
     * @return the activated entry number (or unchanged if no-op)
     */
    public int historyForward() {
        logCurrentHistory("before historyForward()");
        int oldIdx   = historyPosition;
        int oldEntry = activatedEntryNumber;

        if (canHistoryForward()) {
            historyPosition++;
            activatedEntryNumber = history[historyPosition];
            notifyActivated(oldIdx, historyPosition, oldEntry, activatedEntryNumber);
        }

        Constants.zknlogger.log(Level.INFO, "Activated entry number: {0}", activatedEntryNumber);
        logCurrentHistory("after historyForward()");
        return activatedEntryNumber;
    }

    /** Clears the entire history and deactivates any entry. */
    public void clear() {
        int oldIdx   = historyPosition;
        int oldEntry = activatedEntryNumber;

        historyPosition = -1;
        historyCount = 0;
        activatedEntryNumber = -1;

        Constants.zknlogger.info("History cleared.");
        notifyActivated(oldIdx, -1, oldEntry, -1);
    }

    // ---------------- Accessors ----------------

    public int getActivatedEntryNumber() { return activatedEntryNumber; }
    public int getActiveIndex()          { return historyPosition; }
    public int size()                    { return historyCount; }
    public int getHistoryPosition()      { return historyPosition; }
    public int getHistoryCount()         { return historyCount; }

    /** Returns a trimmed snapshot of the history entries (indices 0..historyCount-1). */
    public int[] snapshot() {
        return Arrays.copyOf(history, historyCount);
    }

    // ---------------- Logging ----------------

    private void logCurrentHistory(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("History ").append(context).append(" | size=").append(historyCount)
                .append(", pos=").append(historyPosition).append(", active=").append(activatedEntryNumber)
                .append(" | [");
        for (int i = 0; i < historyCount; i++) {
            if (i > 0) sb.append(", ");
            sb.append(history[i]);
            if (i == historyPosition) sb.append("*"); // mark active
        }
        sb.append("]");
        Constants.zknlogger.info(sb.toString());
    }

    // ---------------- Legacy shims (temporary) ----------------

    /**
     * TEMPORARY: accept any first arg to keep old calls like updateHistory(view, nr) compiling.
     * Does not depend on UI types and will be removed after call sites are migrated.
     */
    @Deprecated
    public void updateHistory(Object ignored, int inputDisplayedEntry) {
        addToHistory(inputDisplayedEntry);
    }

    /** TEMPORARY: bridge for old code calling navigateForwardInHistory(). */
    @Deprecated
    public int navigateForwardInHistory() {
        return historyForward();
    }

    /** TEMPORARY: bridge for old code calling navigateBackwardInHistory(). */
    @Deprecated
    public void navigateBackwardInHistory() {
        historyBack();
    }
}
