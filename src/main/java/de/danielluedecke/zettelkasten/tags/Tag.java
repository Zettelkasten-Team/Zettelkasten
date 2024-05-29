package de.danielluedecke.zettelkasten.tags;

public abstract class Tag {
    protected String tag;

    public Tag(String tag) {
        this.tag = tag;
    }

    public abstract boolean isOpenTag();
    public abstract String getCloseTag();
    public String getTag() {
        return tag;
    }
}
