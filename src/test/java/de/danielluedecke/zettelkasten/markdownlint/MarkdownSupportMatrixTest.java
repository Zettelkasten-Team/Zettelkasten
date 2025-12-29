package de.danielluedecke.zettelkasten.markdownlint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Fails if docs/markdown-support.md is out of sync with generator output.
 * Regenerate by copying MarkdownSupportMatrix.generate() output into the doc.
 */
public class MarkdownSupportMatrixTest {

    @Test
    public void docsMatrix_isUpToDate() throws IOException {
        Path doc = Paths.get("docs", "markdown-support.md");
        String expected = MarkdownSupportMatrix.generate();

        if (!Files.exists(doc)) {
            fail("Missing docs/markdown-support.md; create it from MarkdownSupportMatrix.generate()");
        }

        String actual = new String(Files.readAllBytes(doc), StandardCharsets.UTF_8);
        assertEquals("docs/markdown-support.md is out of date. Regenerate it.",
                normalize(expected),
                normalize(actual));
    }

    private static String normalize(String value) {
        String normalized = value.replace("\r\n", "\n").replace("\r", "\n");
        normalized = normalized.replaceAll("\n+$", "");
        return normalized + "\n";
    }
}
