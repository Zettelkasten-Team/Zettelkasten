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

		if (validHtml.get() && rawContent != null) {
			UbbNestingValidator.Result rawResult = UbbNestingValidator.validate(rawContent);
			if (!rawResult.valid) {
				String errorMessage = buildRawErrorMessage(zettelNummer, rawResult, rawContent, content);
				Constants.zknlogger.log(Level.SEVERE, errorMessage);
			}
		}

		return validHtml.get();
	}

	private static String buildErrorMessage(int zettelNummer, String errorMsg, int pos, String rawContent,
			String htmlContent) {
		UbbNestingValidator.Result rawResult = rawContent != null ? UbbNestingValidator.validate(rawContent) : null;
		int rawOffset = rawResult != null && !rawResult.valid ? rawResult.rawPos : pos;
		String rawExcerpt = excerpt(rawContent, rawOffset, 60);
		String htmlExcerpt = excerpt(htmlContent, pos, 60);
		StringBuilder sb = new StringBuilder();
		sb.append("HTML parse error for entry ").append(zettelNummer).append(" at pos ").append(pos).append(". ")
				.append(errorMsg).append(System.lineSeparator());
		if (rawResult != null && !rawResult.valid) {
			sb.append("Raw UBB parse issue at pos ").append(rawResult.rawPos).append(": ")
					.append(rawResult.message).append(System.lineSeparator());
		}
		sb.append("Raw content excerpt: ").append(rawExcerpt).append(System.lineSeparator());
		sb.append("Raw tag counts: ").append(rawTagCounts(rawExcerpt)).append(System.lineSeparator());
		sb.append("HTML excerpt: ").append(htmlExcerpt).append(System.lineSeparator());
		sb.append("HTML tag context: ").append(htmlTagContext(htmlContent, pos));
		String crossingHint = ubbCrossingHint(rawExcerpt);
		if (crossingHint != null) {
			sb.append(System.lineSeparator()).append(crossingHint);
		}
		return sb.toString();
	}

	private static String buildRawErrorMessage(int zettelNummer, UbbNestingValidator.Result rawResult,
			String rawContent, String htmlContent) {
		int rawOffset = rawResult.rawPos;
		String rawExcerpt = excerpt(rawContent, rawOffset, 60);
		String htmlExcerpt = excerpt(htmlContent, rawOffset, 60);
		StringBuilder sb = new StringBuilder();
		sb.append("Raw UBB parse issue for entry ").append(zettelNummer).append(" at pos ")
				.append(rawResult.rawPos).append(": ").append(rawResult.message).append(System.lineSeparator());
		sb.append("Raw content excerpt: ").append(rawExcerpt).append(System.lineSeparator());
		sb.append("Raw tag counts: ").append(rawTagCounts(rawExcerpt)).append(System.lineSeparator());
		sb.append("HTML excerpt: ").append(htmlExcerpt);
		String crossingHint = ubbCrossingHint(rawExcerpt);
		if (crossingHint != null) {
			sb.append(System.lineSeparator()).append(crossingHint);
		}
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

	private static String rawTagCounts(String rawExcerpt) {
		if (rawExcerpt == null || rawExcerpt.isEmpty()) {
			return "<empty>";
		}
		return " [c]=" + countWithSpan(rawExcerpt, "[c]")
				+ " [/c]=" + countWithSpan(rawExcerpt, "[/c]")
				+ " [m]= " + countWithSpan(rawExcerpt, "[m ")
				+ " [/m]=" + countWithSpan(rawExcerpt, "[/m]");
	}

	private static String countWithSpan(String text, String token) {
		int count = 0;
		int first = -1;
		int last = -1;
		int idx = text.indexOf(token);
		while (idx != -1) {
			if (first == -1) {
				first = idx;
			}
			last = idx;
			count++;
			idx = text.indexOf(token, idx + token.length());
		}
		if (count == 0) {
			return "0";
		}
		return count + " (first=" + first + ", last=" + last + ")";
	}

	private static String htmlTagContext(String html, int pos) {
		if (html == null || html.isEmpty()) {
			return "<empty>";
		}
		int safePos = Math.max(0, Math.min(pos, html.length()));
		int prevStart = html.lastIndexOf('<', safePos);
		int prevEnd = prevStart != -1 ? html.indexOf('>', prevStart + 1) : -1;
		String prevTag = (prevStart != -1 && prevEnd != -1) ? html.substring(prevStart, prevEnd + 1) : "<none>";

		int nextStart = html.indexOf('<', safePos);
		int nextEnd = nextStart != -1 ? html.indexOf('>', nextStart + 1) : -1;
		String nextTag = (nextStart != -1 && nextEnd != -1) ? html.substring(nextStart, nextEnd + 1) : "<none>";

		return "prev=" + prevTag + ", next=" + nextTag;
	}

	private static String ubbCrossingHint(String rawExcerpt) {
		if (rawExcerpt == null || rawExcerpt.isEmpty()) {
			return null;
		}
		int openC = rawExcerpt.indexOf("[c]");
		int openM = rawExcerpt.indexOf("[m ");
		int closeC = rawExcerpt.indexOf("[/c]");
		int closeM = rawExcerpt.indexOf("[/m]");
		if (openC != -1 && openM != -1 && closeC != -1 && closeM != -1 && closeC < closeM) {
			return "Likely UBB crossing: [c] closes before [/m] in excerpt.";
		}
		return null;
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
