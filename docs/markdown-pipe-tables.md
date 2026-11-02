# Markdown table projection

When the existing Markdown setting is enabled, simple pipe tables render as HTML
tables. This is display-only; stored note content and settings are never rewritten.
UBB `[table]` blocks retain their existing behavior, including with Markdown off.

Supported: a header, a separator row with at least three hyphens per cell, and one
or more single-line data rows with the same number of cells. Rows require outer
pipes. Inline code, emphasis and links use the existing rendering pipeline. Colon
alignment markers are accepted but do not add alignment styling.

Multiline cells, escaped literal pipes, nested tables and optional outer pipes
are outside this limited implementation. A pipe inside inline code is protected.
A malformed or unsupported block stays text; a subsequent row of different width
ends an otherwise recognized table and remains outside it.

The comparison for entry 11483 found no heading/quote regression between the
parent of `da71ef9d` and that commit. Both rendered headings and quotes when
Markdown was enabled; neither supported Markdown pipe tables. A disabled or
missing Markdown setting still leaves Markdown syntax visible. This change does
not enable Markdown or repair user configuration automatically.

Fenced code blocks continue through the pre-existing backtick protection; this
change does not implement a full fenced-block parser or syntax highlighting.
