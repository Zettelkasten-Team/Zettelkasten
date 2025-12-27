package de.danielluedecke.zettelkasten.markdownlint;

import java.util.Objects;

public final class MarkdownCompatibilityIssue {

    private final int line;
    private final String ruleId;
    private final String description;
    private final String excerpt;

    public MarkdownCompatibilityIssue(int line, String ruleId, String description, String excerpt) {
        this.line = line;
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.description = Objects.requireNonNull(description, "description");
        this.excerpt = Objects.requireNonNull(excerpt, "excerpt");
    }

    public int getLine() {
        return line;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getDescription() {
        return description;
    }

    public String getExcerpt() {
        return excerpt;
    }

    @Override
    public String toString() {
        return "Line " + line + " [" + ruleId + "]: " + description + " :: " + excerpt;
    }
}
