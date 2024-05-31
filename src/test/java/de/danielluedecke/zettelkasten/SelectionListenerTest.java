package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.view.EditorFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.view.EditorFrame.SelectionListener;

public class SelectionListenerTest {

	@Mock
	private EditorFrame editorFrame;
	
	@BeforeMethod
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

	@Test
	public void testValueChanged_AuthorSelected() {
		JList<String> authorList = new JList<>();
		SelectionListener selectionListener = editorFrame.new SelectionListener(authorList);
		ListSelectionEvent event = new ListSelectionEvent(authorList, 0, 0, false);

		// Simulate selection of an author
		authorList.setSelectedIndex(0);

		// Invoke valueChanged method
		selectionListener.valueChanged(event);

		// Assert that author is selected
		// Add your assertions here
	}

	@Test
	public void testValueChanged_KeywordSelected() {
		JList<String> keywordList = new JList<>();
		SelectionListener selectionListener = editorFrame.new SelectionListener(keywordList);
		ListSelectionEvent event = new ListSelectionEvent(keywordList, 0, 0, false);

		// Simulate selection of a keyword
		keywordList.setSelectedIndex(0);

		// Invoke valueChanged method
		selectionListener.valueChanged(event);

		// Assert that keyword is selected
		// Add your assertions here
	}

	@Test
	public void testValueChanged_AttachmentSelected() {
		JList<String> attachmentList = new JList<>();
		SelectionListener selectionListener = editorFrame.new SelectionListener(attachmentList);
		ListSelectionEvent event = new ListSelectionEvent(attachmentList, 0, 0, false);

		// Simulate selection of an attachment
		attachmentList.setSelectedIndex(0);

		// Invoke valueChanged method
		selectionListener.valueChanged(event);

		// Assert that attachment is selected
		// Add your assertions here
	}

	@Test
	public void testValueChanged_QuickKeywordSelected() {
		JList<String> quickKeywordList = new JList<>();
		JTextArea textArea = new JTextArea();
		SelectionListener selectionListener = editorFrame.new SelectionListener(quickKeywordList);
		ListSelectionEvent event = new ListSelectionEvent(quickKeywordList, 0, 0, false);

		// Simulate selection of a quick keyword
		quickKeywordList.setSelectedIndex(0);
		textArea.setText("Example text for highlighting");

		// Invoke valueChanged method
		selectionListener.valueChanged(event);

		// Assert that quick keyword is selected and highlighted in the text area
		// Add your assertions here
	}
}
