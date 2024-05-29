package de.danielluedecke.zettelkasten.tags;

public class UBBTag extends Tag {

    public UBBTag(String tag) {
        super(tag);
    }

    @Override
    public boolean isOpenTag() {
        return tag.matches("^\\[[^/][^\\]]*\\]$");
    }

    @Override
    public String getCloseTag() {
        if (isOpenTag()) {
            return "[/" + tag.substring(1);
        }
        return tag;
    }
}

