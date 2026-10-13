package de.danielluedecke.zettelkasten;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.Settings;

/**
 * AC-06: a {@link SearchRequests} modification reaches the real
 * {@code ZettelkastenView.setBackupNecessary()}, sets the view's backup-required
 * state, and thereby passes the gate of the real {@code makeAutoBackup()}.
 * <p>
 * The view is a Mockito mock (no Swing frame), with the real methods of the
 * backup-state path enabled and its collaborators replaced by mocks. The
 * autobackup task itself is not started: {@code Settings.getAutoBackup()} is
 * false, so {@code makeAutoBackup()} stops right after its gate.
 * <p>
 * Limitation: this uses reflection and a Mockito view, not a fully initialized
 * Swing application; it does not cover the constructor wiring, the timer or
 * {@code AutoBackupTask}.
 */
public class ZettelkastenViewSearchRequestsBackupStateTest {

    private ZettelkastenView view;
    private Settings settings;
    private SearchRequests searchRequests;

    @Before
    public void setUp() throws Exception {
        view = mock(ZettelkastenView.class);
        settings = mock(Settings.class);
        when(settings.getSynonyms()).thenReturn(mock(Synonyms.class));
        when(settings.getAutoBackup()).thenReturn(false);

        when(view.createSearchRequestsUiCallbacks()).thenCallRealMethod();
        org.mockito.Mockito.doCallRealMethod().when(view).setBackupNecessary();
        org.mockito.Mockito.doCallRealMethod().when(view).backupNecessary(anyBoolean());
        when(view.isBackupNecessary()).thenCallRealMethod();
        doNothing().when(view).setSaveEnabled(anyBoolean());

        searchRequests = new SearchRequests(view.createSearchRequestsUiCallbacks());

        setField("settings", settings);
        setField("bibtex", mock(BibTeX.class));
        setField("data", mock(Daten.class));
        setField("bookmarks", mock(Bookmarks.class));
        setField("desktop", mock(DesktopData.class));
        setField("searchRequests", searchRequests);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = ZettelkastenView.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(view, value);
    }

    private void runMakeAutoBackup() throws Exception {
        Method m = ZettelkastenView.class.getDeclaredMethod("makeAutoBackup");
        m.setAccessible(true);
        m.invoke(view);
    }

    @Test
    public void testAddSearchSetsBackupStateAndOpensAutobackupGate() throws Exception {
        // Without any modification the gate of makeAutoBackup() stays closed.
        runMakeAutoBackup();
        assertFalse(view.isBackupNecessary());
        verify(settings, never()).getAutoBackup();
        verify(view, never()).setSaveEnabled(true);

        boolean added = searchRequests.addSearch(new String[] { "term" }, 0, 0, false, false, false, false, false,
                new int[] { 1, 2 }, "short", "long");

        assertTrue(added);
        assertTrue(searchRequests.isModified());
        assertTrue(view.isBackupNecessary());
        verify(view, times(1)).setSaveEnabled(true);

        // The same method the autobackup timer runs now passes its gate.
        runMakeAutoBackup();
        verify(settings, times(1)).getAutoBackup();
    }
}
