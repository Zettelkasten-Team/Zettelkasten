package de.danielluedecke.zettelkasten.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HtmlValidator {

	/**
	 * This method checks the content of {@code content} for valid HTML and returns
	 * {@code true} if the content could be parsed t HTML. With this, we check
	 * whether an entry makes use of correct or irregular nested tags.
	 *
	 * @param content      the html-page which should be checked for correctly
	 *                     nested tags, usually an entry's content
	 * @param zettelnummer the number of the entry that is checked for valid
	 *                     html-tags
	 * @return {@code true} when the content could be successfully parsed to HTML,
	 *         false otherwise
	 */
	public static boolean isValidHTML(String content, final int zettelnummer) {		
		// AtomicBoolean to make the flag thread-safe
	    final AtomicBoolean validHtml = new AtomicBoolean(true);

		// first, we parse the created web-page to catch errors that might occure when
		// parsing
		// the entry-content. this might happen when tags are not properly used.
		HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
			// in case the parsing was not succssful, log that error message.
			@Override
			public void handleError(String errorMsg, int pos) {
				if (errorMsg.toLowerCase().contains("unmatched") || errorMsg.toLowerCase().contains("missing")) {
					// if body tag is missing (which is true for all entries), don't log that
					// message
					if (!errorMsg.toLowerCase().contains("body")) {
						// tell function that HTML is invalid.
						validHtml.set(false);
						errorMsg = System.lineSeparator() + "Error when parsing the entry "
								+ String.valueOf(zettelnummer) + "!" + System.lineSeparator() + errorMsg
								+ System.lineSeparator();
						Constants.zknlogger.log(Level.SEVERE, errorMsg);
					}
				}
			}
		};
		// create a string-reader that reads the entry's html-content
		Reader reader = new StringReader(content);
		// try to parse the html-page
		try {
			new ParserDelegator().parse(reader, callback, false);
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
		}
		return validHtml.get();
	}

}
