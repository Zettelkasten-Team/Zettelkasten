package de.danielluedecke.zettelkasten.history;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.view.Display;

public class HistoryManagerTest {
    private HistoryManager historyManager;
    private Display mockDisplay;

    @BeforeMethod
    public void setUp() {
        mockDisplay = mock(Display.class);
        historyManager = new HistoryManager(mockDisplay);
    }

    @Test
    public void testAddToHistory() {
        assertFalse(historyManager.canHistoryBack(), "Initially, canHistoryBack should be false");

        historyManager.addToHistory(1);
        assertFalse(historyManager.canHistoryBack(), "After adding the first entry, canHistoryBack should still be false");

        historyManager.addToHistory(2);
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

    @Test
    public void testNavigateForwardInHistory() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        historyManager.historyBack();
        historyManager.navigateForwardInHistory();
        assertEquals(historyManager.historyFore(), 2, "navigateForwardInHistory should call historyFore and return the next entry");
    }
}
