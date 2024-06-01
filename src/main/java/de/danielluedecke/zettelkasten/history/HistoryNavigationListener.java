package de.danielluedecke.zettelkasten.history;

/**
 * This interface defines a single method, `navigateForwardInHistory()`, which
 * any class implementing this interface must provide.
 */
public interface HistoryNavigationListener {
	void navigateForwardInHistory();
}
