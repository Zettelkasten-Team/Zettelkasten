package de.danielluedecke.zettelkasten;

import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.danielluedecke.zettelkasten.data.History;

public class ZettelkastenViewTest {

	private ZettelkastenView instance;

	@Mock
	private History mockHistoryManager;

	@BeforeMethod
	public void setUp() {
		// Create a mock instance of ZettelkastenView
		instance = Mockito.mock(ZettelkastenView.class);
	}

	@Test
	public void testSetHistoryManager() {
		// Act
		instance.setHistoryManager(mockHistoryManager);

		// Assert
		assertEquals(instance.getHistoryManager(), mockHistoryManager);
	}
}
