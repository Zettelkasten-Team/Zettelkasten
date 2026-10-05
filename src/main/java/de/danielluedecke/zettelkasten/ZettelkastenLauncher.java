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
package de.danielluedecke.zettelkasten;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 * Entry point that checks the Java version before starting the application.
 * <p>
 * This class is compiled for Java 8 (see pom.xml), so it can still run on old
 * runtimes and tell the user to install a newer Java instead of failing with an
 * {@link UnsupportedClassVersionError}. The macOS app launcher, for example,
 * falls back to any installed Java when none matches the requested version.
 */
public final class ZettelkastenLauncher {

	/** Keep in sync with maven.compiler.release in pom.xml. */
	static final int REQUIRED_JAVA_VERSION = 25;

	private ZettelkastenLauncher() {
	}

	public static void main(String[] args) throws Throwable {
		String specVersion = System.getProperty("java.specification.version", "");
		if (parseFeatureVersion(specVersion) < REQUIRED_JAVA_VERSION) {
			reportUnsupportedJava(specVersion);
			System.exit(1);
		}
		// Load the application via reflection, so its classes are only loaded on a
		// supported Java version.
		try {
			Class.forName("de.danielluedecke.zettelkasten.ZettelkastenApp")
					.getMethod("main", String[].class)
					.invoke(null, (Object) args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	/**
	 * Returns the feature version of a {@code java.specification.version} value,
	 * e.g. 8 for "1.8" and 25 for "25", or 0 if it cannot be parsed.
	 */
	static int parseFeatureVersion(String specVersion) {
		String version = specVersion.startsWith("1.") ? specVersion.substring(2) : specVersion;
		int end = 0;
		while (end < version.length() && Character.isDigit(version.charAt(end))) {
			end++;
		}
		return end == 0 ? 0 : Integer.parseInt(version.substring(0, end));
	}

	private static void reportUnsupportedJava(String specVersion) {
		boolean german = "de".equals(Locale.getDefault().getLanguage());
		String message = german
				? "Zettelkasten benötigt Java " + REQUIRED_JAVA_VERSION + " oder neuer.\n"
						+ "Gefundene Java-Version: " + specVersion + " (" + System.getProperty("java.home") + ")"
				: "Zettelkasten requires Java " + REQUIRED_JAVA_VERSION + " or newer.\n"
						+ "Found Java version: " + specVersion + " (" + System.getProperty("java.home") + ")";
		System.err.println(message);
		if (!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(null, message, "Zettelkasten", JOptionPane.ERROR_MESSAGE);
		}
	}
}
