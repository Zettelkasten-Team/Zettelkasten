/*
 * Zettelkasten - nach Luhmann
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 *
 * Homepage: http://zettelkasten.danielluedecke.de
 *
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 *
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der
 * GNU General Public License.
 *
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.danielluedecke.zettelkasten.util;

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.tasks.export.ExportTools;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Exports a single Zettel to a Markdown file in the workspace directory using Pandoc.
 */
public final class MarkdownWorkspaceExporter {

	private static final String WORKSPACE_ENV = "ZETTELKASTEN_WORKSPACE_DIR";
	private static final String HOME_WORKSPACE_DIR = "workspace";
	private static final AtomicBoolean loggedMissingWorkspace = new AtomicBoolean(false);

	private MarkdownWorkspaceExporter() {
	}

	public static void exportOnSave(Settings settings, Daten data, int entryNumber) {
		if (settings == null || data == null || entryNumber < 1) {
			return;
		}
		Constants.zknlogger.log(Level.WARNING, "Markdown export attempt for entry {0} (markdown={1}).",
				new Object[] { entryNumber, Boolean.TRUE.equals(settings.getMarkdownActivated()) });
		String pandocPath = settings.getPandocPath();
		if (pandocPath == null || pandocPath.trim().isEmpty()) {
			Constants.zknlogger.log(Level.WARNING, "Pandoc path is not configured; skipping Markdown export.");
			return;
		}
		Path workspaceDir = resolveWorkspaceDir();
		if (workspaceDir == null) {
			if (!loggedMissingWorkspace.getAndSet(true)) {
				Constants.zknlogger.log(Level.WARNING,
						"No workspace directory found for Markdown export. Set {0} or create ~/workspace.",
						WORKSPACE_ENV);
			}
			return;
		}
		String html = data.getEntryAsHtml(entryNumber, null, Constants.FRAME_DESKTOP);
		if (html == null || html.isEmpty()) {
			Constants.zknlogger.log(Level.WARNING, "No HTML content available for Markdown export of entry {0}.",
					entryNumber);
			return;
		}
		html = sanitizeExportHtml(html, data, entryNumber);
		Path tempHtml = null;
		try {
			tempHtml = Files.createTempFile(workspaceDir, "zkn-", ".html");
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.WARNING, "Could not create temporary HTML file for Markdown export.", ex);
			return;
		}
		if (!ExportTools.writeExportData(html, tempHtml.toFile())) {
			Constants.zknlogger.log(Level.WARNING, "Failed to write temporary HTML file for Markdown export.");
			cleanupTempFile(tempHtml);
			return;
		}
		Path outFile = workspaceDir.resolve("z" + entryNumber + ".md");
		List<String> args = Arrays.asList(pandocPath, "-f", "html", "-t", "markdown", "-o",
				outFile.toAbsolutePath().toString(), tempHtml.toAbsolutePath().toString());
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(workspaceDir.toFile());
		pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
			consumeProcessOutput(process);
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				Constants.zknlogger.log(Level.WARNING, "Pandoc export failed for entry {0} (exit code {1}).",
						new Object[] { entryNumber, exitCode });
			} else {
				Constants.zknlogger.log(Level.INFO, "Markdown export created: {0}",
						outFile.toAbsolutePath().toString());
			}
		} catch (InterruptedException ex) {
			Constants.zknlogger.log(Level.WARNING, "Pandoc export interrupted for entry {0}.",
					new Object[] { entryNumber });
			Thread.currentThread().interrupt();
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.WARNING, "Pandoc export failed for entry {0}: {1}",
					new Object[] { entryNumber, ex.getLocalizedMessage() });
		} finally {
			cleanupTempFile(tempHtml);
		}
	}

	static Path resolveWorkspaceDir() {
		String envDir = System.getenv(WORKSPACE_ENV);
		if (envDir != null && !envDir.trim().isEmpty()) {
			Path envPath = Paths.get(envDir.trim());
			if (Files.isDirectory(envPath)) {
				return envPath;
			}
			Constants.zknlogger.log(Level.WARNING, "Workspace directory does not exist: {0}", envPath);
			return null;
		}
		String home = System.getProperty("user.home");
		if (home == null || home.isEmpty()) {
			return null;
		}
		Path fallback = Paths.get(home).resolve(HOME_WORKSPACE_DIR);
		return Files.isDirectory(fallback) ? fallback : null;
	}

	private static void consumeProcessOutput(Process process) {
		Thread reader = new Thread(() -> {
			try (java.io.InputStream stream = process.getInputStream()) {
				byte[] buffer = new byte[1024];
				while (stream.read(buffer) != -1) {
					// Discard output to avoid process blocking on full buffers.
				}
			} catch (IOException ex) {
				Constants.zknlogger.log(Level.FINE, "Failed to consume Pandoc output.", ex);
			}
		}, "pandoc-output-consumer");
		reader.setDaemon(true);
		reader.start();
	}

	private static void cleanupTempFile(Path tempHtml) {
		if (tempHtml == null) {
			return;
		}
		try {
			Files.deleteIfExists(tempHtml);
		} catch (IOException ex) {
			Constants.zknlogger.log(Level.FINE, "Could not delete temporary HTML export file.", ex);
		}
	}

	private static String sanitizeExportHtml(String html, Daten data, int entryNumber) {
		String title = data.getZettelTitle(entryNumber);
		String headingText = "Zettel " + entryNumber;
		if (title != null && !title.isEmpty()) {
			headingText += " \u2013 " + title;
		}
		String headingHtml = "<h1>" + escapeHtml(headingText) + "</h1>";
		String sanitized = html.replaceFirst("(?s)<div class=\"entryrating\">.*?</div>", "");
		sanitized = sanitized.replaceFirst("(?s)<h1>.*?</h1>",
				java.util.regex.Matcher.quoteReplacement(headingHtml));
		sanitized = sanitized.replaceAll("(?is)<img[^>]*src=['\"]jar:[^'\"]*['\"][^>]*>", "");
		sanitized = sanitized.replaceAll("(?is)<a[^>]*class=['\"](?:tslink|rlink)['\"][^>]*>(.*?)</a>", "$1");
		sanitized = sanitized.replaceAll(
				"(?is)<a[^>]*(?:name|id|href)=['\"][^'\"]*(?:tstamp|rateentry)[^'\"]*['\"][^>]*>(.*?)</a>",
				"$1");
		sanitized = sanitized.replaceAll(
				"(?is)<a[^>]*(?:name|id)=['\"][^'\"]*(?:tstamp|rateentry)[^'\"]*['\"][^>]*></a>",
				"");
		return sanitized;
	}

	private static String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}
}
