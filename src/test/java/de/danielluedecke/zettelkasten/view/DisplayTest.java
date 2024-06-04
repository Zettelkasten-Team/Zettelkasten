package de.danielluedecke.zettelkasten.view;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.history.HistoryManager;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;

public class DisplayTest {
    private Display display;
    private JEditorPane editorPaneMock;
    private JList<String> keywordListMock;
    private JTabbedPane tabbedPaneMock;
    private HistoryManager historyManagerMock;
    private Daten datenMock;

    @BeforeMethod
    public void setUp() {
        // Creating mock objects for dependencies
        editorPaneMock = mock(JEditorPane.class);
        keywordListMock = mock(JList.class);
        tabbedPaneMock = mock(JTabbedPane.class);
        historyManagerMock = mock(HistoryManager.class);
        datenMock = mock(Daten.class);

        // Creating Display instance with mock dependencies
        display = new Display(editorPaneMock, keywordListMock, tabbedPaneMock, historyManagerMock);
    }

    @Test
    public void testUpdateEntryPaneAndKeywordsPane() {
        // Test data
        Entry entry = new Entry();
        entry.setContent("Test content");
        List<String> keywords = Arrays.asList("keyword1", "keyword2", "keyword3");
        entry.setKeywords(keywords);

        // Calling method under test
        display.updateEntryPaneAndKeywordsPane(entry);

        // Verifying behavior
        verify(editorPaneMock).setText("Test content");
        verify(keywordListMock).setModel(any(DefaultListModel.class));
    }

    @Test
    public void testUpdateFollowerNumbers() {
        // Test data
        List<String> followerNumbers = Arrays.asList("follower1", "follower2", "follower3");

        // Calling method under test
        display.updateFollowerNumbers(followerNumbers);

        // Verifying behavior
        // Add relevant verification/assertion here
    }

    @Test
    public void testUpdateLinks() {
        // Test data
        List<String> links = Arrays.asList("link1", "link2", "link3");

        // Calling method under test
        display.updateLinks(links);

        // Verifying behavior
        // Add relevant verification/assertion here
    }

    @Test
    public void testUpdateManualLinks() {
        // Test data
        List<String> manualLinks = Arrays.asList("manualLink1", "manualLink2", "manualLink3");

        // Calling method under test
        display.updateManualLinks(manualLinks);

        // Verifying behavior
        // Add relevant verification/assertion here
    }

    @Test
    public void testShowEntryAddsToHistory() {
        // Test data
        int entryNr = 123;
        Entry entry = new Entry();
        entry.setContent("Test content");
        entry.setKeywords(Arrays.asList("keyword1", "keyword2"));
        when(datenMock.getEntryByNr(entryNr)).thenReturn(entry);
        when(display.canAddToHistory(entryNr)).thenReturn(true);

        // Calling method under test
        display.showEntry(entryNr);

        // Verifying behavior
        verify(editorPaneMock).setText("Test content");
        verify(keywordListMock).setModel(any(DefaultListModel.class));
        verify(historyManagerMock).addToHistory(entryNr);
    }

    @Test
    public void testShowEntryDoesNotAddToHistory() {
        // Test data
        int entryNr = 123;
        Entry entry = new Entry();
        entry.setContent("Test content");
        entry.setKeywords(Arrays.asList("keyword1", "keyword2"));
        when(datenMock.getEntryByNr(entryNr)).thenReturn(entry);
        when(display.canAddToHistory(entryNr)).thenReturn(false);

        // Calling method under test
        display.showEntry(entryNr);

        // Verifying behavior
        verify(editorPaneMock).setText("Test content");
        verify(keywordListMock).setModel(any(DefaultListModel.class));
        verify(historyManagerMock, never()).addToHistory(entryNr);
    }

    // Add more test methods as needed
}
