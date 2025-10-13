package de.danielluedecke.zettelkasten.history;

/** Passive listener for history activation changes. */
public interface HistoryListener {
    void onActivated(HistoryEvent e);
}
