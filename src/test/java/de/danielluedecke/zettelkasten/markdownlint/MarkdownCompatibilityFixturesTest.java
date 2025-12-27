package de.danielluedecke.zettelkasten.markdownlint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MarkdownCompatibilityFixturesTest {

    private final MarkdownCompatibilityLinter linter = MarkdownCompatibilityLinter.createDefault();

    private static Path fixturesDir(String subdir) {
        return Paths.get("src", "test", "resources", "markdown-fixtures", subdir);
    }

    private static Stream<Path> mdFiles(Path root) throws IOException {
        return Files.walk(root)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".md"));
    }

    @Test
    public void compatibilityFixtures_haveNoIssues() throws IOException {
        Path root = fixturesDir("compatibility");
        try (Stream<Path> files = mdFiles(root)) {
            files.forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                    List<MarkdownCompatibilityIssue> issues = linter.lint(content);
                    assertTrue("Compatibility fixture must be lint-clean: " + p + "\nIssues: " + issues,
                        issues.isEmpty());
                } catch (IOException e) {
                    fail("Failed reading fixture: " + p + " (" + e.getMessage() + ")");
                }
            });
        }
    }

    @Test
    public void conformanceFixtures_triggerIssues() throws IOException {
        Path root = fixturesDir("conformance");
        try (Stream<Path> files = mdFiles(root)) {
            files.forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                    List<MarkdownCompatibilityIssue> issues = linter.lint(content);
                    assertFalse("Conformance fixture should trigger at least one issue (Spec B): " + p,
                        issues.isEmpty());
                } catch (IOException e) {
                    fail("Failed reading fixture: " + p + " (" + e.getMessage() + ")");
                }
            });
        }
    }
}
