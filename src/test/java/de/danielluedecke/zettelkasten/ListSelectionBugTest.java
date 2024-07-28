package de.danielluedecke.zettelkasten;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListSelectionBugTest {

    private JList<String> jListQuickInputAuthor;
    private JList<String> jListKeywords;
    private JList<String> jListLinks;
    private JList<String> jListQuickInputKeywords;
    private boolean listUpdateActive;

    @BeforeMethod
    public void setUp() {
        jListQuickInputAuthor = new JList<>(new String[]{"Author1", "Author2"});
        jListKeywords = new JList<>(new String[]{"Keyword1", "Keyword2"});
        jListLinks = new JList<>(new String[]{"Link1", "Link2"});
        jListQuickInputKeywords = new JList<>(new String[]{"KeywordA", "KeywordB"});
        listUpdateActive = false;
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testValueChangedThrowsClassCastException() {
        ListSelectionListener listener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (listUpdateActive) {
                    return;
                }
                ListSelectionModel lsm = ((JList<?>) e.getSource()).getSelectionModel(); // This will throw ClassCastException
                lsm.setValueIsAdjusting(true);
            }
        };

        ListSelectionModel selectionModel = jListQuickInputAuthor.getSelectionModel();
        selectionModel.addListSelectionListener(listener);

        // Simulate selection change
        selectionModel.setSelectionInterval(0, 0);
    }
}

