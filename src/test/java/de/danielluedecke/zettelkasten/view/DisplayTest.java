package de.danielluedecke.zettelkasten.view;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import ch.dreyeck.zettelkasten.xml.Zettel;
import de.danielluedecke.zettelkasten.history.History;

public class DisplayTest {
    private Display display;
    private ZettelkastenView zettelkastenViewMock;

    // Annotate historyManagerMock with @Mock
    @Mock
    private History historyManagerMock;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        zettelkastenViewMock = mock(ZettelkastenView.class);
        
        // Use the mocked historyManager
        display = new Display(zettelkastenViewMock, historyManagerMock); // Pass historyManagerMock
    }

    @Test
    public void testDisplayedZettel() {
        int entryNr = 123;
        Zettel zettel = new Zettel();
        zettel.setContent("Test content");
        zettel.setKeywords(Arrays.asList("keyword1", "keyword2").toString());
        when(zettelkastenViewMock.getDisplayedZettel()).thenReturn(new Zettel());

        display.getDisplayedZettel();

        // Verify that addToHistory is invoked with the correct entry number
        verify(historyManagerMock).addToHistory(entryNr);
    }

}
