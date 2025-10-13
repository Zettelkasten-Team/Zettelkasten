package de.danielluedecke.zettelkasten.history;

/** Immutable payload describing a change of the active history entry. */
public final class HistoryEvent {
    private final int oldIndex;
    private final int newIndex;
    private final int oldEntryNumber; // -1 if none
    private final int newEntryNumber; // -1 if none

    public HistoryEvent(int oldIndex, int newIndex, int oldEntryNumber, int newEntryNumber) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.oldEntryNumber = oldEntryNumber;
        this.newEntryNumber = newEntryNumber;
    }

    public int oldIndex() { return oldIndex; }
    public int newIndex() { return newIndex; }
    public int oldEntryNumber() { return oldEntryNumber; }
    public int newEntryNumber() { return newEntryNumber; }
}
