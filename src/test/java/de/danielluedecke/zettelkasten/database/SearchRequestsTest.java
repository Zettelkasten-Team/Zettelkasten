package de.danielluedecke.zettelkasten.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * AC-06: {@link SearchRequests} reports changes of its modified state through
 * {@link SearchRequestsUiCallbacks}, without a {@code ZettelkastenView}.
 */
public class SearchRequestsTest {

    private SearchRequests searchRequests;

    /**
     * Value of {@link SearchRequests#isModified()} observed inside each
     * notification, in call order.
     */
    private final List<Boolean> notifications = new ArrayList<>();

    @Before
    public void setUp() {
        searchRequests = new SearchRequests(new SearchRequestsUiCallbacks() {
            @Override
            public void setBackupNecessary() {
                notifications.add(searchRequests.isModified());
            }
        });
    }

    private boolean addSearch(SearchRequests sr) {
        return sr.addSearch(new String[] { "term" }, 0, 0, false, false, false, false, false, new int[] { 1, 2 },
                "short", "long");
    }

    @Test
    public void testNewInstanceIsUnmodifiedAndDoesNotNotify() {
        assertFalse(searchRequests.isModified());
        assertEquals(0, notifications.size());
    }

    @Test
    public void testAddSearchNotifiesAfterSettingModified() {
        assertTrue(addSearch(searchRequests));

        assertTrue(searchRequests.isModified());
        assertEquals(1, searchRequests.getCount());
        // One notification, sent after the modified state was set.
        assertEquals(List.of(true), notifications);
    }

    @Test
    public void testSetModifiedFalseNotifiesAndClearsModifiedState() {
        assertTrue(addSearch(searchRequests));

        searchRequests.setModified(false);

        assertFalse(searchRequests.isModified());
        assertEquals(List.of(true, false), notifications);
    }

    @Test
    public void testClearResetsModifiedStateWithoutNotification() {
        assertTrue(addSearch(searchRequests));

        searchRequests.clear();

        assertFalse(searchRequests.isModified());
        assertEquals(0, searchRequests.getCount());
        assertEquals(List.of(true), notifications);
    }

    @Test
    public void testNullCallbacksAreIgnored() {
        SearchRequests withoutCallbacks = new SearchRequests(null);

        assertTrue(addSearch(withoutCallbacks));
        assertTrue(withoutCallbacks.isModified());
    }
}
