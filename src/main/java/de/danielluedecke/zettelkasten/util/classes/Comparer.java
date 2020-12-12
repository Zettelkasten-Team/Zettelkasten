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
package de.danielluedecke.zettelkasten.util.classes;

import java.util.Comparator;

/**
 * This is a simple comparer for the utils-sort-methods. we need this if we want to sort
 * strings/arrays/lists with ignoring the case of the to be compared objects...
 */
public class Comparer implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        // check for valid parameters
        if (null == o1 || null == o2) {
            return 0;
        }

        String s1 = o1.toString().toLowerCase();
        String s2 = o2.toString().toLowerCase();

        int i = prepairForCompare(s1).compareTo(prepairForCompare(s2));
        return (0 != i) ? i : s1.compareTo(s2);
    }

    private String prepairForCompare(String s) {
        return s.toLowerCase().replace('ä', 'a')
                .replace('ö', 'o')
                .replace('ü', 'u')
                .replace('ß', 's');
    }
}
