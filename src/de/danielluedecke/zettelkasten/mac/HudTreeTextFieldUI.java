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
package de.danielluedecke.zettelkasten.mac;

import com.explodingpixels.macwidgets.plaf.HudPaintingUtils;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Daniel Luedecke
 */
public class HudTreeTextFieldUI extends BasicTextFieldUI {

    private final Color fontColor = ColorUtil.colorJTreeDarkText;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JTextComponent textComponent = (JTextComponent) c;

        textComponent.setOpaque(false);
        textComponent.setBorder(null);
        textComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorUtil.colorJTreeBackground),
                BorderFactory.createEmptyBorder(1, 2, 1, 2)));
        // textComponent.setBackground(new Color(255,255,255,96));
        textComponent.setBackground(ColorUtil.colorJTreeTextFieldBackground);
        textComponent.setForeground(fontColor);
        textComponent.setFont(HudPaintingUtils.getHudFont());
        textComponent.setSelectedTextColor(Color.BLACK);
        textComponent.setSelectionColor(fontColor);
        textComponent.setCaretColor(fontColor);
    }

    @Override
    protected void paintSafely(Graphics graphics) {
        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintSafely(graphics);
    }
}
