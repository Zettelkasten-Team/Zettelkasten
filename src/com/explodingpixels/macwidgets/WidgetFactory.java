package com.explodingpixels.macwidgets;

import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class WidgetFactory {

	   public static JSplitPane createHorizontalSplitPane(JComponent componentLeft, JComponent componentRight) {
	        JSplitPane splitPane = new JSplitPane(
	                JSplitPane.HORIZONTAL_SPLIT, componentLeft, componentRight);
	        splitPane.setContinuousLayout(true);
	        splitPane.setDividerSize(1);
	        ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(
	                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xa5a5a5)));
	        splitPane.setBorder(BorderFactory.createEmptyBorder());
	        return splitPane;
	    }
	
	   public static JSplitPane createVerticalSplitPane(JComponent componentTop, JComponent componentBottom) {
	        JSplitPane splitPane = new JSplitPane(
	                JSplitPane.VERTICAL_SPLIT, componentTop, componentBottom);
	        splitPane.setContinuousLayout(true);
	        splitPane.setDividerSize(1);
	        ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(
	                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xa5a5a5)));
	        splitPane.setBorder(BorderFactory.createEmptyBorder());
	        return splitPane;
	    }

	   public static void updateSplitPane(JSplitPane splitPane, Color dividerColor) {
	        splitPane.setContinuousLayout(true);
	        splitPane.setDividerSize(1);
                switch (splitPane.getOrientation()) {
                    case JSplitPane.HORIZONTAL_SPLIT:
        	        ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, dividerColor));
                        break;
                    case JSplitPane.VERTICAL_SPLIT:
        	        ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, dividerColor));
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
                    if (PlatformUtil.isJava7()) {
                        b = new TitledBorder(BorderFactory.createEmptyBorder(3,3,3,3), title, TitledBorder.LEFT, TitledBorder.BELOW_TOP);
                    }
                    else {
                        b = new TitledBorder(BorderFactory.createEmptyBorder(), title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
                    }
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
}
