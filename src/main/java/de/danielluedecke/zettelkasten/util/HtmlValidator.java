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
		final AtomicBoolean validHtml = new AtomicBoolean(true);

		HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
			@Override
			public void handleError(String errorMsg, int pos) {
				if (!errorMsg.toLowerCase().contains("body") && (errorMsg.toLowerCase().contains("unmatched")
						|| errorMsg.toLowerCase().contains("missing"))) {
					validHtml.set(false);
					String errorMessage = String.format("Error when parsing the entry %d!%n%s%n", zettelNummer,
							errorMsg);
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
