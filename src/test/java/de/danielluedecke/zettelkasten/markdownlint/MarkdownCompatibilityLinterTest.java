package de.danielluedecke.zettelkasten.markdownlint;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MarkdownCompatibilityLinterTest {

    private final MarkdownCompatibilityLinter linter = MarkdownCompatibilityLinter.createDefault();

    @Test
    public void e001_atxHeadingWithEmphasis_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("## **Bold** heading\n");
        assertTrue("Expected E001 for emphasis inside ATX heading",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E001")));
    }

    @Test
    public void e002_setextHeadingWithEmphasis_isFlagged() {
        String text = "*Italic* heading\n---\n";
        List<MarkdownCompatibilityIssue> issues = linter.lint(text);
        assertTrue("Expected E002 for emphasis inside Setext heading",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E002")));
    }

    @Test
    public void e003_blockquote_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("> quote\n");
        assertTrue("Expected E003 for blockquote",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E003")));
    }

    @Test
    public void e005_boldOnlyParagraph_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("**bold only**\n");
        assertTrue("Expected E005 for bold-only paragraph",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E005")));
    }

    @Test
    public void e006_nestedEmphasisHeuristic_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("*italic **bold***\n");
        assertTrue("Expected E006 for nested emphasis heuristic",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E006")));
    }

    @Test
    public void e007_formattedListItem_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("- *Italic* item\n");
        assertTrue("Expected E007 for formatted list item",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E007")));
    }

    @Test
    public void e008_unicodeArrow_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("text → text\n");
        assertTrue("Expected E008 for unicode arrow",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E008")));
    }

    @Test
    public void e009_smartQuotes_areFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("“smart” quotes\n");
        assertTrue("Expected E009 for smart quotes",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E009")));
    }

    @Test
    public void e011_inlineLink_isFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("[text](https://example.com)\n");
        assertTrue("Expected E011 for inline Markdown link",
            issues.stream().anyMatch(i -> i.getRuleId().equals("E011")));
    }

    @Test
    public void plainText_isNotFlagged() {
        List<MarkdownCompatibilityIssue> issues = linter.lint("Plain text only.\nSecond line.\n");
        assertTrue("Plain text should not be flagged", issues.isEmpty());
    }
}
