package de.danielluedecke.zettelkasten.database;

import java.util.*;

public class Entry {
    private int number;
    private List<Integer> subEntries;

    public Entry(int number) {
        this.number = number;
        this.subEntries = new ArrayList<>();
    }

    public void addSubEntry(int subEntryNumber) {
        this.subEntries.add(subEntryNumber);
    }

    public int getNumber() {
        return number;
    }

    public List<Integer> getSubEntries() {
        return subEntries;
    }
}

