package de.danielluedecke.zettelkasten.data;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JExample.class)
public class HistoryTest {

    @Test
    public History newHistoryShouldHaveNoNavigation() {
        History history = new History();
        assertFalse("Fresh history should not allow back", history.canHistoryBack());
        assertFalse("Fresh history should not allow forward", history.canHistoryForward());
        assertEquals("Initial activated entry should be -1", -1, history.historyBack());
        return history;
    }

    @Given("newHistoryShouldHaveNoNavigation")
    public History shouldTrackFirstEntry(History history) {
        history.addToHistory(1);
        assertFalse("Still can't go back after first entry", history.canHistoryBack());
        assertFalse("Still can't go forward after first entry", history.canHistoryForward());
        assertEquals("Current entry should be 1", 1, history.historyBack());
        return history;
    }

    @Given("shouldTrackFirstEntry")
    public History shouldHandleBasicNavigation(History history) {
        history.addToHistory(2);
        assertTrue("Should allow back after second entry", history.canHistoryBack());
        assertFalse("Should not allow forward yet", history.canHistoryForward());

        int backEntry = history.historyBack();
        assertEquals("Back navigation should return previous entry", 1, backEntry);
        assertTrue("Should now allow forward", history.canHistoryForward());

        int forwardEntry = history.historyForward();
        assertEquals("Forward navigation should return next entry", 2, forwardEntry);
        return history;
    }

    @Given("shouldHandleBasicNavigation")
    public void shouldClearForwardOnNewEntry(History history) {
        history.addToHistory(3);
        assertTrue("Should allow back", history.canHistoryBack());
        assertFalse("New entry should clear forward history", history.canHistoryForward());
    }
}