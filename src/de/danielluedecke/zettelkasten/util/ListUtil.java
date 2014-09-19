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

import java.awt.event.KeyEvent;
import javax.swing.ListModel;

/**
 *
 * @author Daniel Luedecke
 */
public class ListUtil {

    /**
     * This method retrieves the key-code {@code keyCode} of a released key in the
     * JList {@code list} and checks whether this key was a navigation key (i.e.
     * cursor up/down/left/right or home/end). If so, the method tries to select the next
     * related entry of that JList, according to the pressed key.<br><br>
     * Furthermore, the related content is made visible (scroll rect to visible or ensure
     * index is visible).
     *
     * @param list a reference to the JList where the related key was released
     * @param keyCode the keycode of the released key
     */
    public static void navigateThroughList(javax.swing.JList list, int keyCode) {
        if (KeyEvent.VK_LEFT == keyCode || KeyEvent.VK_RIGHT == keyCode) {
            return;
        }
        int selectedRow = list.getSelectedIndex();
        ListModel lm = list.getModel();
        if (-1 == selectedRow) {
            selectedRow = 0;
        }
        switch (keyCode) {
            case KeyEvent.VK_HOME:
                selectedRow = 0;
                break;
            case KeyEvent.VK_END:
                selectedRow = lm.getSize() - 1;
                break;
            case KeyEvent.VK_DOWN:
                if (lm.getSize() > (selectedRow + 1)) {
                    selectedRow++;
                }
                break;
            case KeyEvent.VK_UP:
                if (selectedRow > 0) {
                    selectedRow--;
                }
                break;
        }
        list.setSelectedIndex(selectedRow);
        list.ensureIndexIsVisible(selectedRow);
    }
    /**
     * This method selects the first entry in the JList {@code list} that start with the
     * text that is entered in the filter-textfield {@code textfield}.
     *
     * @param list the JList where the item should be selected
     * @param textfield the related filtertextfield that contains the user-input
     */
    public static void selectByTyping(javax.swing.JList list, javax.swing.JTextField textfield) {
        String text = textfield.getText().toLowerCase();
        ListModel lm = list.getModel();
        for (int cnt = 0; cnt < lm.getSize(); cnt++) {
            String val = lm.getElementAt(cnt).toString();
            if (val.toLowerCase().startsWith(text)) {
                list.setSelectedIndex(cnt);
                list.ensureIndexIsVisible(cnt);
                // and leave method
                return;
            }
        }
    }

}
