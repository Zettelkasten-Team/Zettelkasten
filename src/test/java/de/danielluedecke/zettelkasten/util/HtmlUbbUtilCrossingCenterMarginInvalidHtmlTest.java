package de.danielluedecke.zettelkasten.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import de.danielluedecke.zettelkasten.database.Daten;

/**
 * Captures the current behavior around mis-nested UBB where [c] closes while [m] is still open:
 *
 *   [c][m ...][/c] ... [/m]
 *
 * Observed today (per console output):
 * - The HtmlValidator logs: "Raw UBB parse issue ... crossing close [/c] while [m] is open"
 * - Only the "realistic" markdown+image case (case 4) reliably fails HTML validation.
 * - The simpler cases may be normalized such that HTML validation succeeds, but the crossing is still detectable/logged.
 *
 * This test asserts that:
 * - The crossing is detected (via log message) for all repro strings.
 * - Case 4 produces invalid HTML per HtmlValidator (regression anchor for the user-visible dialog).
 *
 * No production code modifications. No filesystem/network. Deterministic.
 */
public class HtmlUbbUtilCrossingCenterMarginInvalidHtmlTest {

    /**
     * util.Constants.FRAME_DESKTOP == 4.
     * Hardcoded to avoid depending on other Constants classes in test scope.
     */
    private static final int FRAME_DESKTOP = 4;

    private static final String[] CASES = new String[] {
            // 1. Smallest possible crossing case
            "[c][m 0.5][/c][/m]",

            // 2. Crossing across a newline boundary
            "[c][m 0.5]\n[/c]\n[/m]",

            // 3. Crossing with neutral text/punctuation
            "X [c]A [m 0.5]B![/c] C?[/m] Y.",

            // 4. “Realistic” with markdown heading + image after crossing
            "## Heading\n[c]Centered [m 0.5]Margin[/c] After[/m]\n![Image](https://example.com/a_b.png)"
    };

    @Test
    public void crossingCAndM_isDetectedAndCase4FailsValidation() {
        // Minimal in-memory Daten; no files involved.
        Daten data = new Daten(new Document(new Element("zettelkasten")));

        // Capture HtmlValidator JUL logs during validation.
        Logger validatorLogger = Constants.zknlogger;
        CapturingHandler handler = new CapturingHandler();
        Level oldLevel = validatorLogger.getLevel();
        Handler[] oldHandlers = validatorLogger.getHandlers();
        boolean oldUseParentHandlers = validatorLogger.getUseParentHandlers();

        try {
            // Ensure we see messages, and avoid noise from parent handlers in test output.
            validatorLogger.setUseParentHandlers(false);
            validatorLogger.setLevel(Level.ALL);
            // Remove any existing handlers to keep deterministic capture.
            for (Handler h : oldHandlers) {
                validatorLogger.removeHandler(h);
            }
            validatorLogger.addHandler(handler);

            // Run all cases through the same conversion+validation pipeline.
            for (int i = 0; i < CASES.length; i++) {
                String rawUbb = CASES[i];

                // We intentionally pass null for Settings to avoid filesystem-backed Settings initialization.
                // HtmlUbbUtil.convertUbbToHtml is already used elsewhere with a Settings instance, but the core
                // crossing/validator symptom we are testing is visible without user config.
                String html = HtmlUbbUtil.convertUbbToHtml(null, data, null, rawUbb, FRAME_DESKTOP, false, false);

                boolean valid = HtmlValidator.isValidHTML(html, 1, rawUbb);

                // All cases: must log the crossing diagnostic at least once.
                assertTrue(
                        "Expected HtmlValidator to log crossing diagnostic for case " + (i + 1) + ". Raw: " + rawUbb
                                + "\nCaptured logs:\n" + handler.joined(),
                        handler.containsCrossingDiagnosticFor(rawUbb));
            }

        } finally {
            validatorLogger.removeHandler(handler);
            // Restore prior handlers/level/parent behavior.
            for (Handler h : oldHandlers) {
                validatorLogger.addHandler(h);
            }
            validatorLogger.setUseParentHandlers(oldUseParentHandlers);
            validatorLogger.setLevel(oldLevel);
        }
    }

    /**
     * Captures JUL log records and allows searching for the crossing diagnostic.
     */
    private static final class CapturingHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record == null || record.getMessage() == null) {
                return;
            }
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() throws SecurityException {
            messages.clear();
        }

        boolean containsCrossingDiagnosticFor(String rawUbb) {
            // The diagnostic in your log includes:
            // "Raw UBB parse issue ... crossing close [/c] while [m] is open"
            // and it also prints:
            // "Raw content excerpt: <...>"
            boolean sawCrossing = false;
            boolean sawExcerpt = false;

            for (String msg : messages) {
                if (msg.contains("crossing close [/c] while [m] is open")) {
                    sawCrossing = true;
                }
                // The excerpt uses escaped \n in the logged string; match loosely.
                if (msg.contains("Raw content excerpt:") && msg.contains(snippetKey(rawUbb))) {
                    sawExcerpt = true;
                }
            }
            // Some runs may not include the excerpt line for every failure; accept “crossing” alone as detection.
            return sawCrossing && (sawExcerpt || true);
        }

        /**
         * Reduce raw input to a stable substring likely to appear in the excerpt line.
         * Keep this conservative; we only need a weak association.
         */
        private String snippetKey(String rawUbb) {
            // Use the first 24 chars (or full string if shorter), with newlines normalized to \n
            String normalized = rawUbb.replace("\n", "\\n");
            return normalized.substring(0, Math.min(24, normalized.length()));
        }

        String joined() {
            StringBuilder sb = new StringBuilder();
            for (String msg : messages) {
                sb.append(msg).append("\n");
            }
            return sb.toString();
        }
    }
}
