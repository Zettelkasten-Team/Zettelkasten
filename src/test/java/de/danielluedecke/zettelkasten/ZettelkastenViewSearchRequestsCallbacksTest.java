package de.danielluedecke.zettelkasten;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.SearchRequestsUiCallbacks;

/**
 * AC-06: the callbacks {@link ZettelkastenView} gives to {@link SearchRequests}
 * still trigger the view's backup indicator. The view itself is a mock, so no
 * Swing frame is created.
 */
public class ZettelkastenViewSearchRequestsCallbacksTest {

    @Test
    public void testSearchRequestModificationCallsSetBackupNecessary() {
        ZettelkastenView view = mock(ZettelkastenView.class);
        when(view.createSearchRequestsUiCallbacks()).thenCallRealMethod();
        SearchRequestsUiCallbacks callbacks = view.createSearchRequestsUiCallbacks();
        SearchRequests searchRequests = new SearchRequests(callbacks);

        searchRequests.setModified(true);

        assertTrue(searchRequests.isModified());
        verify(view, times(1)).setBackupNecessary();
    }
}
