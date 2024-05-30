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
    public void testHistoryBack_whenCanGoBackInHistory() {
        // Arrange
        when(mockData.canGoBackInHistory()).thenReturn(true);

        // Act
        boolean result = historyManager.historyBack();

        // Assert
        verify(mockData).historyBack();
        assertTrue(result);
    }

    @Test
    public void testHistoryBack_whenCannotGoBackInHistory() {
        // Arrange
        when(mockData.canGoBackInHistory()).thenReturn(false);

        // Act
        boolean result = historyManager.historyBack();

        // Assert
        verify(mockData, never()).historyBack();
        assertFalse(result);
    }
    
    @Test
    public void testAddToHistory() {
        historyManager.addToHistory(1);
        assertTrue(historyManager.canHistoryBack());
    }

    @Test
    public void testHistoryBack() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        assertEquals(historyManager.historyBack(), 1);
    }

    @Test
    public void testHistoryFore() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        historyManager.historyBack();
        assertEquals(historyManager.historyFore(), 2);
    }

    @Test
    public void testCanHistoryBack() {
        historyManager.addToHistory(1);
        assertTrue(historyManager.canHistoryBack());
        historyManager.historyBack();
        assertFalse(historyManager.canHistoryBack());
    }

    @Test
    public void testCanHistoryFore() {
        historyManager.addToHistory(1);
        historyManager.addToHistory(2);
        historyManager.historyBack();
        assertTrue(historyManager.canHistoryFore());
        historyManager.historyFore();
        assertFalse(historyManager.canHistoryFore());
    }
    
}
