package de.danielluedecke.zettelkasten;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class EntryIDTest {

	@Test
	public void testEntryIDIntConstructor() {
		EntryID entryID = new EntryID(123);
		assertEquals(entryID.asInt(), 123, "The entry number should be 123");
	}

	@Test
	public void testEntryIDStringConstructor() {
		EntryID entryID = new EntryID("456");
		assertEquals(entryID.asInt(), 456, "The entry number should be 456");
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void testEntryIDStringConstructorWithInvalidNumber() {
		new EntryID("invalid");
	}

	@Test
	public void testAsInt() {
		EntryID entryID = new EntryID(789);
		assertEquals(entryID.asInt(), 789, "The entry number should be 789");
	}

	@Test
	public void testAsString() {
		EntryID entryID = new EntryID(1011);
		assertEquals(entryID.asString(), "1011", "The entry number string should be '1011'");
	}

	@Test
	public void testEqualsSameObject() {
		EntryID entryID = new EntryID(1213);
		assertTrue(entryID.equals(entryID), "An EntryID should be equal to itself");
	}

	@Test
	public void testEqualsNull() {
		EntryID entryID = new EntryID(1415);
		assertFalse(entryID.equals(null), "An EntryID should not be equal to null");
	}

	@Test
	public void testEqualsDifferentClass() {
		EntryID entryID = new EntryID(1617);
		String notAnEntryID = "notAnEntryID";
		assertFalse(entryID.equals(notAnEntryID), "An EntryID should not be equal to an object of a different class");
	}

	@Test
	public void testEqualsSameEntryNumber() {
		EntryID entryID1 = new EntryID(1819);
		EntryID entryID2 = new EntryID(1819);
		assertTrue(entryID1.equals(entryID2), "EntryIDs with the same entry number should be equal");
	}

	@Test
	public void testEqualsDifferentEntryNumber() {
		EntryID entryID1 = new EntryID(2021);
		EntryID entryID2 = new EntryID(2223);
		assertFalse(entryID1.equals(entryID2), "EntryIDs with different entry numbers should not be equal");
	}

	@Test
	public void testHashCode() {
		EntryID entryID1 = new EntryID(2425);
		EntryID entryID2 = new EntryID(2425);
		assertEquals(entryID1.hashCode(), entryID2.hashCode(), "Equal EntryIDs should have the same hash code");
	}

	@Test
	public void testHashCodeDifferent() {
		EntryID entryID1 = new EntryID(2627);
		EntryID entryID2 = new EntryID(2829);
		assertNotEquals(entryID1.hashCode(), entryID2.hashCode(),
				"Different EntryIDs should have different hash codes");
	}
}
