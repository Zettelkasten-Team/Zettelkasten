/*
 * Relaunch64 - A Java Crossassembler for C64 machine language coding.
 * Copyright (C) 2001-2013 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://www.popelganda.de
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
package de.danielluedecke.zettelkasten.mac;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 *
 * @author Daniel Luedecke
 */
public class ZknMacWidgetFactory {
    public static void updateSplitPane(JSplitPane splitPane, Color dividerColor) {
         splitPane.setContinuousLayout(true);
         switch (splitPane.getOrientation()) {
             case JSplitPane.HORIZONTAL_SPLIT:
                 splitPane.setDividerSize(1);
                 ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, dividerColor));
                 break;
             case JSplitPane.VERTICAL_SPLIT:
                 splitPane.setDividerSize(6);
                 ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, dividerColor));
                 break;
         }
         splitPane.setBorder(BorderFactory.createEmptyBorder());
     }

    public static void updateSplitPane(JSplitPane splitPane) {
        updateSplitPane(splitPane, new Color(0xa5a5a5));
     }

    public static TitledBorder getTitledBorder(String title, Color titlecolor, Settings settings) {
         TitledBorder b;
         if (settings.isMacAqua() || settings.isSeaGlass()) {
             b = new TitledBorder(BorderFactory.createEmptyBorder(3,3,3,3), title, TitledBorder.LEFT, TitledBorder.BELOW_TOP);
             Font f = b.getTitleFont();
             // for sea glass, this will return null, so get different font setting
             if (null==f) {
                 f = settings.getTableFont();
             }
             if (f!=null) {
                 b.setTitleFont(f.deriveFont(Font.BOLD));
             }
             // check whether title color is used
             if (titlecolor!=null) {
                 b.setTitleColor(titlecolor);
             }
         }
         else {
             b = javax.swing.BorderFactory.createTitledBorder(null, title);
         }
         return b;
    }

    public static TitledBorder getTitledBorder(String title, Settings settings) {
        return getTitledBorder(title, null, settings);
    }
    
    public static void setTextFieldBorder(JTextField textComponent) {
        if (textComponent.isFocusOwner()) {
            textComponent.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ColorUtil.colorJTreeText),
                    BorderFactory.createEmptyBorder(1, 2, 1, 2)));
        }
        else {
            textComponent.setBorder(null);
        }
    }
    public static JTextField createHudTreeTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setUI(new HudTreeTextFieldUI());
        return textField;
    }
}
