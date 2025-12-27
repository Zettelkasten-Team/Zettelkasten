# Zettelkasten Editor: Markdown Support Matrix

This document is generated from Spec B linter rules.

- Last Updated: (generated)
- Scope: Spec B (descriptive, based on observed incompatibilities)

## Rules

| Rule ID | Description | Scope |
|--------:|-------------|-------|
| E001 | Emphasis inside ATX heading (e.g., ## **Bold** heading) | SINGLE_LINE |
| E002 | Emphasis inside Setext heading (two-line form) | TWO_LINE_WINDOW |
| E003 | Blockquote line starting with '>' | SINGLE_LINE |
| E005 | Bold-only or italic-only paragraph (single emphasized span only) | SINGLE_LINE |
| E006 | Nested or mixed emphasis markers (heuristic) | SINGLE_LINE |
| E007 | Formatted list item (e.g., - *Italic* item) | SINGLE_LINE |
| E008 | Unicode arrow '→' present (prefer ASCII '->') | SINGLE_LINE |
| E009 | Smart quotes present (prefer straight quotes) | SINGLE_LINE |
| E011 | Inline Markdown link [text](url) present (prefer raw URL) | SINGLE_LINE |

## Fixtures

Fixtures live under `src/test/resources/markdown-fixtures/`:

- `conformance/` contains valid Markdown constructs (Spec A intent) that are expected to trigger Spec B lints today.
- `compatibility/` contains known-safe content that must stay lint-clean.

## Notes

This matrix does not claim design intent. It records current, observed incompatibilities.
