package de.danielluedecke.zettelkasten;

import java.util.Objects;

/**
 * EntryNumber represents a entry number.
 */
public class EntryID {
	private int entryNumber; 
	
	public EntryID(int entryNumber) {
		this.entryNumber = entryNumber;
	}
	
	public EntryID(String entryNumber) {
		this.entryNumber = Integer.parseInt(entryNumber);
	}
	
	public int asInt() {
	  return entryNumber;
	}
	
	public String asString() {
		return Integer.toString(entryNumber);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(entryNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntryID other = (EntryID) obj;
		return entryNumber == other.entryNumber;
	}
}
