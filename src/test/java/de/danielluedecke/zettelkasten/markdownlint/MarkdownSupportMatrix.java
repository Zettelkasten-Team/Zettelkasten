package de.danielluedecke.zettelkasten.markdownlint;

import java.util.List;

public final class MarkdownSupportMatrix {

    private MarkdownSupportMatrix() {}

    public static String generate() {
        List<MarkdownCompatibilityRule> rules = MarkdownCompatibilityRules.all();

        StringBuilder sb = new StringBuilder();
        sb.append("# Zettelkasten Editor: Markdown Support Matrix\n\n");
        sb.append("This document is generated from Spec B linter rules.\n\n");
        sb.append("- Last Updated: (generated)\n");
        sb.append("- Scope: Spec B (descriptive, based on observed incompatibilities)\n\n");

        sb.append("## Rules\n\n");
        sb.append("| Rule ID | Description | Scope |\n");
        sb.append("|--------:|-------------|-------|\n");
        for (MarkdownCompatibilityRule r : rules) {
            sb.append("| ")
              .append(r.getId()).append(" | ")
              .append(escapePipes(r.getDescription())).append(" | ")
              .append(r.getScope()).append(" |\n");
        }

        sb.append("\n## Fixtures\n\n");
        sb.append("Fixtures live under `src/test/resources/markdown-fixtures/`:\n\n");
        sb.append("- `conformance/` contains valid Markdown constructs (Spec A intent) that are expected to trigger Spec B lints today.\n");
        sb.append("- `compatibility/` contains known-safe content that must stay lint-clean.\n");

        sb.append("\n");
        sb.append("## Notes\n\n");
        sb.append("This matrix does not claim design intent. It records current, observed incompatibilities.\n");
        return sb.toString();
    }

    private static String escapePipes(String s) {
        return s.replace("|", "\\|");
    }
}
