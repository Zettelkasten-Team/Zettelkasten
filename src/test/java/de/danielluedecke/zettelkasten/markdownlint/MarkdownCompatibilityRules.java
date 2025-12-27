package de.danielluedecke.zettelkasten.markdownlint;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static de.danielluedecke.zettelkasten.markdownlint.MarkdownCompatibilityRule.Scope;

/**
 * Spec B: Descriptive rules based on observed failures in the Zettelkasten editor.
 *
 * IMPORTANT:
 * - These rules do NOT define correct Markdown.
 * - They define patterns that are currently known/suspected to trigger editor rejection.
 * - When the editor improves, remove or relax rules only with supporting evidence/tests.
 */
public final class MarkdownCompatibilityRules {

    private MarkdownCompatibilityRules() {}

    private static MarkdownCompatibilityRule rule(String id, String description, Scope scope, String regex) {
        return new MarkdownCompatibilityRule(id, description, scope, Pattern.compile(regex));
    }

    /**
     * Rules are intentionally conservative: they warn early rather than trying to fully parse Markdown.
     */
    public static List<MarkdownCompatibilityRule> all() {
        return Arrays.asList(
            // E001: emphasis inside ATX heading
            rule("E001",
                 "Emphasis inside ATX heading (e.g., ## **Bold** heading)",
                 Scope.SINGLE_LINE,
                 "^#{1,6}\\s+.*([*_]{1,2}).*"),

            // E002: emphasis inside Setext heading (two-line window)
            // We detect emphasis in the first line, followed by === or --- underline.
            rule("E002",
                 "Emphasis inside Setext heading (two-line form)",
                 Scope.TWO_LINE_WINDOW,
                 "^(.*([*_]{1,2}).*)\\n([=]{2,}|[-]{2,})\\s*$"),

            // E003: blockquote
            rule("E003",
                 "Blockquote line starting with '>'",
                 Scope.SINGLE_LINE,
                 "^>\\s?.*"),

            // E005: bold-only paragraph (single line consisting only of emphasis)
            rule("E005",
                 "Bold-only or italic-only paragraph (single emphasized span only)",
                 Scope.SINGLE_LINE,
                 "^\\s*([*_]{1,2})[^\\n]+\\1\\s*$"),

            // E006: nested/mixed emphasis markers in one line (heuristic)
            rule("E006",
                 "Nested or mixed emphasis markers (heuristic)",
                 Scope.SINGLE_LINE,
                 ".*([*_]{2}.*[*_].*[*_]{2}|[*_].*[*_]{2}.*[*_]).*"),

            // E007: formatted list item (heuristic)
            rule("E007",
                 "Formatted list item (e.g., - *Italic* item)",
                 Scope.SINGLE_LINE,
                 "^\\s*[-*+]\\s+.*([*_]{1,2}).*"),

            // E008: unicode arrow present
            rule("E008",
                 "Unicode arrow '→' present (prefer ASCII '->')",
                 Scope.SINGLE_LINE,
                 ".*→.*"),

            // E009: smart quotes
            rule("E009",
                 "Smart quotes present (prefer straight quotes)",
                 Scope.SINGLE_LINE,
                 ".*[“”].*"),

            // E011: inline Markdown link [text](url) (heuristic)
            rule("E011",
                 "Inline Markdown link [text](url) present (prefer raw URL)",
                 Scope.SINGLE_LINE,
                 ".*\\[[^\\]]+\\]\\([^\\)]+\\).*")
        );
    }
}
