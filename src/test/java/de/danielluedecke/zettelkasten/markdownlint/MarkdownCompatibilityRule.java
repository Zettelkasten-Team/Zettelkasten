package de.danielluedecke.zettelkasten.markdownlint;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Spec B: Descriptive rule based on observed editor incompatibilities.
 * This is not a normative Markdown specification.
 */
public final class MarkdownCompatibilityRule {

    public enum Scope {
        SINGLE_LINE,
        TWO_LINE_WINDOW
    }

    private final String id;
    private final String description;
    private final Scope scope;
    private final Pattern pattern;

    public MarkdownCompatibilityRule(String id, String description, Scope scope, Pattern pattern) {
        this.id = Objects.requireNonNull(id, "id");
        this.description = Objects.requireNonNull(description, "description");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Scope getScope() {
        return scope;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
