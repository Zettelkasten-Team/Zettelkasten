# Windows image file URLs: unresolved

`HtmlUbbUtil.fixImagePaths()` currently generates Windows image sources such as
`file://\C:\...\11401-mermaid-diagram.png`, including an extra leading separator
and Windows backslashes. Whether this URL is correctly interpreted by Swing's
image loader remains unresolved. The Markdown link-parser correction in commit
`873c7d69` does not address file-URL construction.

The two local-image cases in `HtmlUbbUtilMarkdownImagesTest` verify the parser
regression independently of that platform-specific prefix: an image element with
the expected filename survives, the following source link remains separate, and
no damaged image markers remain. They do not establish URL validity or successful
image loading on Windows. Existing and missing local PNG fixtures are both kept.

A separate investigation should check Windows file-URL construction and actual
Swing image loading for both cases. `fixImagePaths()` is unchanged here; the
subsequent Windows CI run is the authoritative cross-platform check for these
parser regression tests.
