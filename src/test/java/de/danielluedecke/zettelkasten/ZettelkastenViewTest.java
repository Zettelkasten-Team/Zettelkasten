package de.danielluedecke.zettelkasten;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.jdesktop.application.SingleFrameApplication;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.settings.Settings;

public class ZettelkastenViewTest {
	private ZettelkastenView instance;
	private Daten mockData;

	@BeforeMethod
	public void setUp() {
		// Create a mock instance of ZettelkastenView
		instance = Mockito.mock(ZettelkastenView.class);
	}

	@Test
	public void testHistoryBack_whenCanGoBackInHistory() {
		// Arrange
		when(mockData.canGoBackInHistory()).thenReturn(true);

		// Act
		instance.historyBack();

		// Assert
		verify(mockData).historyBack();
		assertEquals(instance.getDisplayedZettel(), -1);
		// Assuming updateDisplay is a method that we can't directly test, verify its
		// effect or that it was called
		verify(instance, times(1)).updateDisplay();
	}

	@Test
	public void testHistoryBack_whenCannotGoBackInHistory() {
		// Arrange
		when(mockData.canGoBackInHistory()).thenReturn(false);

		// Act
		instance.historyBack();

		// Assert
		verify(mockData, never()).historyBack();
		// No need to reset displayedZettel or call updateDisplay
		assertNotEquals(instance.getDisplayedZettel(), -1);
		verify(instance, never()).updateDisplay();
	}
}
