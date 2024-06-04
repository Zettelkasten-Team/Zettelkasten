package de.danielluedecke.zettelkasten.view;

import javax.swing.*;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.history.HistoryManager;
import de.danielluedecke.zettelkasten.util.Constants;

import java.util.List;

public class Display {
    private JEditorPane editorPane;
    private JList<String> keywordList;
    private JTabbedPane tabbedPane;
    private HistoryManager historyManager;

    public Display(JEditorPane editorPane, JList<String> keywordList, JTabbedPane tabbedPane, HistoryManager historyManager) {
        this.editorPane = editorPane;
        this.keywordList = keywordList;
        this.tabbedPane = tabbedPane;
        this.historyManager = historyManager;
    }

    public void updateEntryPaneAndKeywordsPane(Entry entry) {
        editorPane.setText(entry.getContent());

        DefaultListModel<String> keywordListModel = new DefaultListModel<>();
        for (String keyword : entry.getKeywords()) {
            keywordListModel.addElement(keyword);
        }
        keywordList.setModel(keywordListModel);
    }

    public void updateTabbedPaneData(Entry entry) {
        updateFollowerNumbers(entry.getFollowerNumbers());
        updateLinks(entry.getLinks());
        updateManualLinks(entry.getManualLinks());
    }

    public void updateFollowerNumbers(List<String> followerNumbers) {
        JList<String> followerList = getFollowerListComponent();
        DefaultListModel<String> followerListModel = new DefaultListModel<>();
        for (String number : followerNumbers) {
            followerListModel.addElement(number);
        }
        followerList.setModel(followerListModel);
    }

    public void updateLinks(List<String> links) {
        JList<String> linkList = getLinkListComponent();
        DefaultListModel<String> linkListModel = new DefaultListModel<>();
        for (String link : links) {
            linkListModel.addElement(link);
        }
        linkList.setModel(linkListModel);
    }

    public void updateManualLinks(List<String> manualLinks) {
        JList<String> manualLinkList = getManualLinkListComponent();
        DefaultListModel<String> manualLinkListModel = new DefaultListModel<>();
        for (String link : manualLinks) {
            manualLinkListModel.addElement(link);
        }
        manualLinkList.setModel(manualLinkListModel);
    }

    public JList<String> getFollowerListComponent() {
        // Implementation to get the follower list component from tabbedPane
        return new JList<>();
    }

    public JList<String> getLinkListComponent() {
        // Implementation to get the link list component from tabbedPane
        return new JList<>();
    }

    public JList<String> getManualLinkListComponent() {
        // Implementation to get the manual link list component from tabbedPane
        return new JList<>();
    }

    public boolean canAddToHistory(int entryNr) {
        // Implement logic to determine if the entry can be added to history
        // For example, you might check if the entryNr is already the current entry
        // displayed
        return true;
    }

    public void showEntry(int entryNr) {
        Entry entry = daten.getEntryByNr(entryNr);
        if (entry != null) {
            updateEntryPaneAndKeywordsPane(entry);
            updateTabbedPaneData(entry);
            if (canAddToHistory(entryNr)) {
                historyManager.addToHistory(entryNr);
                Constants.zknlogger.info("Added to history: " + entryNr);
            }
        }
    }
}
