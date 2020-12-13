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

import de.danielluedecke.zettelkasten.database.Settings;
import java.awt.Color;

/**
 *
 * @author Daniel Luedecke
 */
public class ColorUtil {
    /**
     * Color value, needed for jtree text field background on mac os x
     * Color(239,243,247)
     */
    public static final Color colorJTreeTextFieldBackground = new Color(239, 243, 247);
    /**
     * Color value, needed for jtree text field background on mac os x
     * Color(240,244,248)
     */
    public static final Color colorJTreeLighterBackground = new Color(240, 244, 248);
    /**
     * Color value, needed for setting border-color of the matte-top and -bottom-border
     * Color(136,173,224)
     */
    public static final Color colorNormalSeaGlassBlue = new Color(136, 173, 224);
    /**
     * Color value, needed for setting label-shadow-color mac-os-x-jtrees
     * Color(238,238,238)
     */
    public static final Color colorDarkLabelShadowGray = new Color(238, 238, 238);
    /**
     * Color value, needed for setting border-color of the matte-top and -bottom-border
     * Color(89,135,192)
     */
    public static final Color colorDarkLineSeaGlassBlue = new Color(89, 135, 192);
    /**
     * Color value, needed for setting the snow-leopard-style on mac os x
     * new Color(218,218,218)
     */
    public static final Color colorNormalElCapitanGray = new Color(236, 236, 236); //MacColorUtils.EMPTY_COLOR
    /**
     * Color value, needed for setting border-color of the matte-top and -bottom-border
     * Color(64,64,64);
     */
    public static final Color colorDarkLineGray = new Color(64, 64, 64);
    /**
     * Color value, needed for jtree background on mac os x
     * Color(230,235,242)
     */
    public static final Color colorJTreeBackground = new Color(230, 235, 242);
    /**
     * Color value, needed for setting the leopard-style on mac os x
     * Color(188,188,188);
     */
    public static final Color colorDarkGray = new Color(188, 188, 188);
    /**
     * Color value, needed for jtree text color on mac os x
     * (113,126,140)
     */
    public static final Color colorJTreeText = new Color(113, 126, 140);
    /**
     * Color value, needed for setting the leopard-style on mac os x
     * Color(158,158,158);
     */
    public static final Color colorDarkBorderGray = new Color(158, 158, 158);
    /**
     * Color value, needed for setting label-color of mac-os-xjtrees
     * Color(94,94,94)
     */
    public static final Color colorDarkLabelGray = new Color(94, 94, 94);
    /**
     * Color value, needed for jtree text color on mac os x
     * Color(56,63,70)
     */
    public static final Color colorJTreeDarkText = new Color(56, 63, 70);
    /**
     * Color value, needed for hud background on mac os x
     * Color(94,94,94)
     */
    public static final Color colorHudGray = new Color(94, 94, 94);
    /**
     * Color value, background color for sea glass style
     * Color(240,240,240)
     */
    public static final Color colorSeaGlassGray = new Color(240, 240, 240);
    /**
     * Color value, needed for setting the sea-glass-matte-style
     * Color(206,206,206)
     */
    public static final Color colorSeaGlassLineGray = new Color(206, 206, 206);
    /**
     * Color value, needed for setting the leopard-style on mac os x
     */
    public static final Color colorNoFocusGray = new Color(229, 229, 221);
    /**
     * Color value, needed for setting border-color of the matte-top and -bottom-border
     * new Color(158,158,158);
     */
    public static final Color colorDarkLineNimbusGray = new Color(158, 158, 158);

    /**
     * Color value, needed for setting border-color of the matte-top and -bottom-border
     * @param settings
     * @return 
     */
    public static Color getBorderGray(Settings settings) {
        if (settings.isMacAqua()) {
            return colorDarkBorderGray;
        }
        if (settings.isSeaGlass()) {
            return colorSeaGlassLineGray;
        }
        if (settings.isNimbus()) {
            return colorDarkLineNimbusGray;
        }
        return colorDarkLineGray;
    }

}
