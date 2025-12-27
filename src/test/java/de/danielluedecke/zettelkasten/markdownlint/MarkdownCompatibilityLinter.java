package de.danielluedecke.zettelkasten.markdownlint;

import java.util.ArrayList;
import java.util.List;

/**
 * Spec B linter:
 * - Descriptive, based on observed/suspected incompatibilities.
 * - Not an editor test. Not a Markdown correctness checker.
 *
 * Output is intended for:
 * - warning users before they paste content into the editor
 * - tracking progress: when editor behavior changes, update rules + fixtures
 */
public final class MarkdownCompatibilityLinter {

    private final List<MarkdownCompatibilityRule> rules;

    public MarkdownCompatibilityLinter(List<MarkdownCompatibilityRule> rules) {
        this.rules = new ArrayList<>(rules);
    }

    public static MarkdownCompatibilityLinter createDefault() {
        return new MarkdownCompatibilityLinter(MarkdownCompatibilityRules.all());
    }

    public List<MarkdownCompatibilityIssue> lint(String text) {
        List<MarkdownCompatibilityIssue> issues = new ArrayList<>();
        String normalized = text == null ? "" : text.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n", -1);

        // 1) single-line rules
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            for (MarkdownCompatibilityRule rule : rules) {
                if (rule.getScope() != MarkdownCompatibilityRule.Scope.SINGLE_LINE) {
                    continue;
                }

                if (rule.getPattern().matcher(line).matches() || rule.getPattern().matcher(line).find()) {
                    issues.add(new MarkdownCompatibilityIssue(
                        lineNumber,
                        rule.getId(),
                        rule.getDescription(),
                        excerpt(line)
                    ));
                }
            }
        }

        // 2) two-line window rules
        for (int i = 0; i < lines.length - 1; i++) {
            String window = lines[i] + "\n" + lines[i + 1];
            int lineNumber = i + 1;

            for (MarkdownCompatibilityRule rule : rules) {
                if (rule.getScope() != MarkdownCompatibilityRule.Scope.TWO_LINE_WINDOW) {
                    continue;
                }

                if (rule.getPattern().matcher(window).matches() || rule.getPattern().matcher(window).find()) {
                    issues.add(new MarkdownCompatibilityIssue(
                        lineNumber,
                        rule.getId(),
                        rule.getDescription(),
                        excerpt(window)
                    ));
                }
            }
        }

        return issues;
    }

    private static String excerpt(String s) {
        String oneLine = s.replace("\n", "\\n");
        if (oneLine.length() <= 120) {
            return oneLine;
        }
        return oneLine.substring(0, 117) + "...";
    }
}
