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

import de.danielluedecke.zettelkasten.util.Constants;
import javax.swing.AbstractButton;

/**
 *
 * @author Daniel Luedecke
 */
public class MacToolbarButton {

    public static final String SEGMENT_POSITION_FIRST = "first";
    public static final String SEGMENT_POSITION_MIDDLE = "middle";
    public static final String SEGMENT_POSITION_LAST = "last";
    public static final String SEGMENT_POSITION_ONLY = "only";

    public static AbstractButton makeTexturedToolBarButton(AbstractButton button, String segmentPosition) {

        if (Constants.isJava7OnMac || Constants.isJava8OnMac || null == segmentPosition || segmentPosition.isEmpty() || segmentPosition.equals(SEGMENT_POSITION_ONLY)) {
            button.putClientProperty("JButton.buttonType", "textured");
        } else {
            button.putClientProperty("JButton.buttonType", "segmentedTextured");
            button.putClientProperty("JButton.segmentPosition", segmentPosition);
        }
        button.setText(null);
        button.setBorderPainted(true);
        button.setPreferredSize(Constants.seaGlassButtonDimension);

        return button;
    }
}
