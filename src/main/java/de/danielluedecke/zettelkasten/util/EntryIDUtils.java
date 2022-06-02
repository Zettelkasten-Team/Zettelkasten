package de.danielluedecke.zettelkasten.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.danielluedecke.zettelkasten.EntryID;

public class EntryIDUtils {
	static public List<EntryID> csvToEntryIDList(String csv) {
		if (csv.isEmpty()) {
			return Collections.<EntryID>emptyList();
		}
		String[] entriesAsString = csv.split(",");
		List<EntryID> retval = new ArrayList<EntryID>(entriesAsString.length);
		for (String entryAsString : entriesAsString) {
			if (entryAsString.isEmpty()) {
				continue;
			}
			retval.add(new EntryID(entryAsString));
		}
		return retval;
	}
}
