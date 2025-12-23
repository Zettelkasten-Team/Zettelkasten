package de.danielluedecke.zettelkasten.data;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import static org.junit.Assert.*;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JExample.class)
public class HistoryTest {

    @Mock
    private ZettelkastenView mockView; // Mocked view for dependency

    public HistoryTest() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

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

    @Test
    public void emptyHistorySnapshot() {
        History history = new History(3);
        assertEquals(-1, history.getActivatedEntryNumber());
        assertEquals(-1, history.getHistoryPosition());
        assertEquals(0, history.getHistoryCount());
        assertArrayEquals(new int[0], history.snapshot());
    }

    @Test
    public void sequentialAddsTrackSnapshot() {
        History history = new History(5);
        history.addToHistory(10);
        history.addToHistory(20);
        history.addToHistory(30);

        assertEquals(3, history.getHistoryCount());
        assertEquals(2, history.getHistoryPosition());
        assertEquals(30, history.getActivatedEntryNumber());
        assertArrayEquals(new int[] { 10, 20, 30 }, history.snapshot());
    }

    @Test
    public void backThenAddTruncatesForwardBranch() {
        History history = new History(5);
        history.addToHistory(1);
        history.addToHistory(2);
        history.addToHistory(3);

        history.historyBack(); // active: 2
        history.addToHistory(4); // should drop 3

        assertArrayEquals(new int[] { 1, 2, 4 }, history.snapshot());
        assertEquals(3, history.getHistoryCount());
        assertEquals(2, history.getHistoryPosition());
        assertEquals(4, history.getActivatedEntryNumber());
        assertFalse("Forward history should be truncated", history.canHistoryForward());
    }

    @Test
    public void bufferOverflowShiftsOldestEntry() {
        History history = new History(3);
        history.addToHistory(1);
        history.addToHistory(2);
        history.addToHistory(3);

        history.addToHistory(4); // overflow, drop 1
        assertArrayEquals(new int[] { 2, 3, 4 }, history.snapshot());
        assertEquals(3, history.getHistoryCount());
        assertEquals(2, history.getHistoryPosition());
        assertEquals(4, history.getActivatedEntryNumber());

        history.addToHistory(5); // overflow again, drop 2
        assertArrayEquals(new int[] { 3, 4, 5 }, history.snapshot());
        assertEquals(3, history.getHistoryCount());
        assertEquals(2, history.getHistoryPosition());
        assertEquals(5, history.getActivatedEntryNumber());
    }
}
