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

/**
 * @author Daniel Luedecke
 */
public class PlatformUtil {
    /**
     * Indicates whether the program is running on a mac or not...
     *
     * @return {@code true} if current OS is any mac os
     */
    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os");
    }

    /**
     * Indicates whether the program is running on a linux or not...
     *
     * @return {@code true} if current OS is any linux os
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * Retrieve current Java version.
     *
     * @return The current Java version as string.
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Indicates whether the OS is a windows OS
     *
     * @return {@code true} if current OS is a windows system
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * indicates whether java 7 is running on windows
     *
     * @return {@code true} if current OS is a windows system with Java 1.7 installed
     */
    public static boolean isJava7OnWindows() {
        return isWindows() && getJavaVersion().startsWith("1.7");
    }

    /**
     * indicates whether java 7 is running on mac
     *
     * @return {@code true} if current OS is any mac os with Java 1.7 installed
     */
    public static boolean isJava7OnMac() {
        return isMacOS() && getJavaVersion().startsWith("1.7");
    }

}
