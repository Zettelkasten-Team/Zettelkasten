/*
 * Zettelkasten - nach Luhmann
 ** Copyright (C) 2001-2014 by Daniel Lüdecke (http://www.danielluedecke.de)
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
 *
 * @author Daniel Luedecke
 */
public class PlatformUtil {
    /**
     * Indicates whether the programm is running on a mac or not...
     * @return {@code true} if current OS is any mac os
     */
    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os");
    }
    /**
     * Indicates whether the programm is running on a linux or not...
     * @return {@code true} if current OS is any linux os
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
    /**
     * Retrieve current Java version.
     * @return The current Java version as string.
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
    /**
     * Indicates whether Mac OS X 10.5 (Leopard) is running...
     * @return {@code true} if current OS is mac os 10.5 (Leopard)
     */
    public static boolean isLeopard() {
        return isMacOS() & System.getProperty("os.version").startsWith("10.5");
    }
    /**
     * Indicates whether Mac OS X 10.6 (Snow Leopard) is running...
     * @return {@code true} if current OS is mac os 10.6 (snow leopard)
     */
    public static boolean isSnowLeopard() {
        return isMacOS() & System.getProperty("os.version").startsWith("10.6");
    }
    /**
     * Indicates whether Mac OS X 10.7 (Lion) is running...
     * @return {@code true} if current OS is mac os 10.7 (lion)
     */
    public static boolean isLion() {
        return isMacOS() & System.getProperty("os.version").startsWith("10.7");
    }
    /**
     * Indicates whether Mac OS X 10.8 (Mountain LION) is running...
     * @return {@code true} if current OS is mac os 10.8 (mountain lion)
     */
    public boolean isMountainLion() {
        return isMacOS() & System.getProperty("os.version").startsWith("10.8");
    }
    /**
     * Indicates whether the OS is a windows OS
     * @return {@code true} if current OS is a windows system
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
    /**
     * indicates whether java 7 is running on windows
     * @return {@code true} if current OS is a windows system with Java 1.7 installed
     */
    public static boolean isJava7OnWindows() {
        return isWindows() && getJavaVersion().startsWith("1.7");
    }
    /**
     * indicates whether java 8 is running on windows
     * @return {@code true} if current OS is a windows system with Java 1.8 installed
     */
    public static boolean isJava8OnWindows() {
        return isWindows() && getJavaVersion().startsWith("1.8");
    }
    /**
     * indicates whether java 7 is running on mac
     * @return {@code true} if current OS is any mac os with Java 1.7 installed
     */
    public static boolean isJava7OnMac() {
        return isMacOS() && getJavaVersion().startsWith("1.7");
    }
    /**
     * indicates whether java 8 is running on mac
     * @return {@code true} if current OS is any mac os with Java 1.8 installed
     */
    public static boolean isJava8OnMac() {
        return isMacOS() && getJavaVersion().startsWith("1.8");
    }
    /**
     * indicates whether java 7 is running on current system
     * @return {@code true} if Java 1.7 is installed on current system
     */
    public static boolean isJava7() {
        return getJavaVersion().startsWith("1.7");
    }
    /**
     * indicates whether java 8 is running on current system
     * @return {@code true} if Java 1.8 is installed on current system
     */
    public static boolean isJava8() {
        return getJavaVersion().startsWith("1.8");
    }
    /**
     * indicates whether java 6 is running on mac
     * @return {@code true} if current OS is any mac os with Java 1.6 installed
     */
    public static boolean isJava6OnMac() {
        return isMacOS() && getJavaVersion().startsWith("1.6");
    }
    /**
     * Indicates wether the current OS is Windows 7
     * @return {@code true} if current OS is windows 7
     */
    public static boolean isWindows7() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows 7");
    }
    /**
     * Indicates wether the current OS is Windows 8
     * @return {@code true} if current OS is windows 8
     */
    public static boolean isWindows8() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows 8");
    }
}
