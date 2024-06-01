package de.danielluedecke.zettelkasten.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryManagerEventSourceExample {
    private List<HistoryNavigationListener> listeners = new ArrayList<>();

    // Method to add a listener
    public void addHistoryNavigationListener(HistoryNavigationListener listener) {
        listeners.add(listener);
    }

    // Method to notify all listeners
    public void notifyNavigateForward() {
        for (HistoryNavigationListener listener : listeners) {
            listener.navigateForwardInHistory();
        }
    }

    // Example method that triggers the event
    public void historyFore() {
        // Logic to go forward in history
        // ...

        // Notify all listeners about the navigation event
        notifyNavigateForward();
    }
}
