package de.danielluedecke.zettelkasten.history;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;

public class HistoryManagerTest {
    private HistoryManager historyManager;
    private Daten mockData;

    @BeforeMethod
    public void setUp() {
        mockData = mock(Daten.class);
        historyManager = new HistoryManager(mockData);
    }

    @Test
    public void testAddToHistory() {
        // Initially, there should be no history, so canHistoryBack should return false
        assertFalse(historyManager.canHistoryBack(), "Initially, canHistoryBack should be false");

        // Add an entry to the history
        historyManager.addToHistory(1);
        // After adding one entry, canHistoryBack should still be false because there's no previous entry to go back to
        assertFalse(historyManager.canHistoryBack(), "After adding the first entry, canHistoryBack should still be false");

        // Add another entry to the history
        historyManager.addToHistory(2);
        // After adding the second entry, canHistoryBack should return true because now we have a previous entry to go back to
        assertTrue(historyManager.canHistoryBack(), "After adding the second entry, canHistoryBack should be true");
    }

    @Test
    public void testHistoryBack() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        assertEquals(historyManager.historyBack(), 1, "historyBack should return the previous entry");
    }

    @Test
    public void testHistoryFore() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        historyManager.historyBack();
        assertEquals(historyManager.historyFore(), 2, "historyFore should return the next entry");
    }

    @Test
    public void testCanHistoryBack() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        assertTrue(historyManager.canHistoryBack(), "canHistoryBack should be true after adding two entries");
        historyManager.historyBack();
        assertFalse(historyManager.canHistoryBack(), "canHistoryBack should be false after going back to the first entry");
    }

    @Test
    public void testCanHistoryFore() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        historyManager.historyBack();
        assertTrue(historyManager.canHistoryFore(), "canHistoryFore should be true after going back to the first entry");
        historyManager.historyFore();
        assertFalse(historyManager.canHistoryFore(), "canHistoryFore should be false after going forward to the last entry");
    }
}
