package de.danielluedecke.zettelkasten.history;

/**
 * a listener interface for handling history navigation events
 */
public interface NavigationListener {

	/**
	 * This interface defines 2 methods, `navigateForwardInHistory()` and
	 * `navigateBackwardInHistory()`, which any class implementing this interface
	 * must provide.
	 */
	int navigateForwardInHistory();
	void navigateBackwardInHistory();
}
