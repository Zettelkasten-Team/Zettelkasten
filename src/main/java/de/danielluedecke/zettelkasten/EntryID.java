package de.danielluedecke.zettelkasten;

import java.util.Objects;

/**
 * Represents an entry number with two constructors for initialization, and
 * provides methods to retrieve the entry number as an int or a String.
 */
public class EntryID {
	private int entryNumber;

	public EntryID(int entryNumber) {
		this.entryNumber = entryNumber;
	}

	/**
	 * Constructs an EntryID with the specified integer entry number.
	 *
	 * @param entryNumber the entry number
	 */
	public EntryID(String entryNumber) {
		this.entryNumber = Integer.parseInt(entryNumber);
	}

	/**
	 * Returns the entry number as an integer.
	 *
	 * @return the entry number
	 */
	public int asInt() {
		return entryNumber;
	}

	/**
	 * Returns the entry number as a string.
	 *
	 * @return the entry number as a string
	 */
	public String asString() {
		return Integer.toString(entryNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entryNumber);
	}

	@Override
	public boolean equals(Object obj) {
		assert (getClass() == obj.getClass());
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		EntryID other = (EntryID) obj;
		return entryNumber == other.entryNumber;
	}
}
