package de.danielluedecke.zettelkasten;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ListSelectionFixTest {

    private JList<String> jListQuickInputAuthor;
    private JList<String> jListKeywords;
    private JList<String> jListLinks;
    private JList<String> jListQuickInputKeywords;
    private boolean listUpdateActive;
    private boolean authorSelected;
    private boolean keywordSelected;
    private boolean attachmentSelected;
    private boolean quickKeywordSelected;

    @BeforeMethod
    public void setUp() {
        jListQuickInputAuthor = new JList<>(new String[]{"Author1", "Author2"});
        jListKeywords = new JList<>(new String[]{"Keyword1", "Keyword2"});
        jListLinks = new JList<>(new String[]{"Link1", "Link2"});
        jListQuickInputKeywords = new JList<>(new String[]{"KeywordA", "KeywordB"});
        listUpdateActive = false;
        authorSelected = false;
        keywordSelected = false;
        attachmentSelected = false;
        quickKeywordSelected = false;
    }

    private void setAuthorSelected(boolean selected) {
        authorSelected = selected;
    }

    private void setKeywordSelected(boolean selected) {
        keywordSelected = selected;
    }

    private void setAttachmentSelected(boolean selected) {
        attachmentSelected = selected;
    }

    private void setQuickKeywordSelected(boolean selected) {
        quickKeywordSelected = selected;
    }

    @Test
    public void testValueChangedNoClassCastException() {
        ListSelectionListener listener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (listUpdateActive) {
                    return;
                }

                Object source = e.getSource();
                if (source instanceof ListSelectionModel) {
                    ListSelectionModel lsm = (ListSelectionModel) source;
                    lsm.setValueIsAdjusting(true);

                    if (jListQuickInputAuthor.getSelectionModel() == lsm) {
                        setAuthorSelected(jListQuickInputAuthor.getSelectedIndex() != -1);
                    } else if (jListKeywords.getSelectionModel() == lsm) {
                        setKeywordSelected(jListKeywords.getSelectedIndex() != -1);
                    } else if (jListLinks.getSelectionModel() == lsm) {
                        setAttachmentSelected(jListLinks.getSelectedIndex() != -1);
                    } else if (jListQuickInputKeywords.getSelectionModel() == lsm) {
                        setQuickKeywordSelected(jListQuickInputKeywords.getSelectedIndex() != -1);
                    }
                }
            }
        };

        ListSelectionModel selectionModel = jListQuickInputAuthor.getSelectionModel();
        selectionModel.addListSelectionListener(listener);

        // Simulate selection change
        selectionModel.setSelectionInterval(0, 0);
        assertTrue(authorSelected);
        assertFalse(keywordSelected);
        assertFalse(attachmentSelected);
        assertFalse(quickKeywordSelected);
    }
}

