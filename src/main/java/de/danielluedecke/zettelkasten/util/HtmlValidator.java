package de.danielluedecke.zettelkasten.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.jsoup.Jsoup;

public class HtmlValidator {

	/**
	 * This method checks the content of {@code content} for valid HTML and returns
	 * {@code true} if the content could be parsed to HTML. With this, we check
	 * whether an entry makes use of correct or irregular nested tags.
	 *
	 * @param content      the html-page which should be checked for correctly
	 *                     nested tags, usually an entry's content
	 * @param zettelNummer the number of the entry that is checked for valid
	 *                     html-tags
	 * @return {@code true} when the content could be successfully parsed to HTML,
	 *         false otherwise
	 */
	public static boolean isValidHTML(String content, final int zettelNummer) {
		return isValidHTML(content, zettelNummer, null);
	}

	/**
	 * Validates HTML and logs context for parser errors.
	 *
	 * @param content      the html-page which should be checked for correctly nested tags
	 * @param zettelNummer the number of the entry that is checked for valid html-tags
	 * @param rawContent   the raw entry content (UBB) for context logging
	 * @return {@code true} when the content could be successfully parsed to HTML,
	 *         false otherwise
	 */
	public static boolean isValidHTML(String content, final int zettelNummer, final String rawContent) {
		final AtomicBoolean validHtml = new AtomicBoolean(true);

		HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
			@Override
			public void handleError(String errorMsg, int pos) {
				if (!errorMsg.toLowerCase().contains("body") && (errorMsg.toLowerCase().contains("unmatched")
						|| errorMsg.toLowerCase().contains("missing"))) {
					validHtml.set(false);
					String errorMessage = buildErrorMessage(zettelNummer, errorMsg, pos, rawContent, content);
					Constants.zknlogger.log(Level.SEVERE, errorMessage);
				}
			}
		};

		try (Reader reader = new StringReader(content)) {
			new ParserDelegator().parse(reader, callback, false);
		} catch (IOException ex) {
			validHtml.set(false);
			Constants.zknlogger.log(Level.WARNING, "Error parsing HTML content", ex);
		} catch (Exception ex) {
			validHtml.set(false);
			Constants.zknlogger.log(Level.SEVERE, "Error parsing HTML content", ex);
		}

		return validHtml.get();
	}

	private static String buildErrorMessage(int zettelNummer, String errorMsg, int pos, String rawContent,
			String htmlContent) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTML parse error for entry ").append(zettelNummer).append(" at pos ").append(pos).append(". ")
				.append(errorMsg).append(System.lineSeparator());
		sb.append("Raw content excerpt: ").append(excerpt(rawContent, pos, 60)).append(System.lineSeparator());
		sb.append("HTML excerpt: ").append(excerpt(htmlContent, pos, 60));
		return sb.toString();
	}

	private static String excerpt(String content, int pos, int radius) {
		if (content == null) {
			return "<null>";
		}
		int length = content.length();
		if (length == 0) {
			return "<empty>";
		}
		int safePos = Math.max(0, Math.min(pos, length));
		int start = Math.max(0, safePos - radius);
		int end = Math.min(length, safePos + radius);
		String snippet = content.substring(start, end);
		return snippet.replace("\r", "\\r").replace("\n", "\\n");
	}

	/**
	 * Checks if the HTML content is well-formed.
	 *
	 * @param htmlContent the HTML content to be checked
	 * @return true if the HTML content is well-formed, false otherwise
	 */
	public static boolean isWellFormed(String htmlContent) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
            // If parsing is successful, the HTML is well-formed
            return true;
        } catch (Exception e) {
            // If parsing fails, the HTML is not well-formed
            return false;
        }
    }
}
