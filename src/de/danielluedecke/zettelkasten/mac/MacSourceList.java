/*
 * This source is an adaption from the MacWindgets
 * http://code.google.com/p/macwidgets/
 * which are licensed under the GNU Lesser GPL
 */

package de.danielluedecke.zettelkasten.mac;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 *
 * @author danielludecke
 */
public class MacSourceList {
    /**
     * Create a SourceList style JList.
     */
    public static JList createMacSourceList() {
        // currently this new list-property is disabled, because the rendering is too
        // slow, when jlists are being filtered etc. thus, we simply create a JList
        // without the new mac-styled cell-renderer
        return new JList();
/*
        // init variable
        JList list = null;
        // check whether os is mac os x and whether aqua is the currently used look and feel
        LookAndFeel laf = UIManager.getLookAndFeel();
        boolean ismacaqua = System.getProperty("os.name").toLowerCase().startsWith("mac os") & laf.getDescription().toLowerCase().contains("aqua");
        // only install the new renderer, when we have mac os x with aqua look&feel
        if (ismacaqua) {
                // use this line to create a list with a mac-like grey background
                // JList list = new SourceList();
            // for usual white background-color, use this line instead
            list = new JList();
            // install a custom renderer that wraps the already installed renderer.
            list.setCellRenderer(new CustomListCellRenderer(list.getCellRenderer()));
        }
        // in case we have no mac nor aqua look&feel, create just a normal JList...
        else {
            list = new JList();
        }
        return list;
*/
    }

    /**
     * A custom JList that renders like a Mac SourceList.
     */
    public static class SourceList extends JList {

        public SourceList() {
            // make the component non-opaque so that we can paint the background in
            // paintComponent.
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // paint the background of the component using the special Mac border
            // painter.
            Border backgroundPainter =
                UIManager.getBorder("List.sourceListBackgroundPainter");
            backgroundPainter.paintBorder(this, g, 0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }

    /**
     * A custom ListCellRenderrer that wraps a delegate renderer.
     */
    public static class CustomListCellRenderer extends JPanel
        implements ListCellRenderer {

        private ListCellRenderer fDelegate;
        private boolean fIsSelected;
        private boolean fIsFocused;

        public CustomListCellRenderer(ListCellRenderer delegate) {
            this.setOpaque(false);
            this.setLayout(new BorderLayout());
            this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            fDelegate = delegate;
        }

        @Override
        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {

            this.removeAll();
            // remember the isSelected and cellHasFocus state so that we can use those
            // values in the paintComponent method.
            fIsSelected = isSelected;
            fIsFocused = cellHasFocus;
            // call the delegate renderer
            JComponent component = (JComponent) fDelegate.getListCellRendererComponent(
                list, value, index, isSelected, false);
            // make the delegate rendere non-opqaue so that the background shows through.
            component.setOpaque(false);
            component.setFont(component.getFont().deriveFont((fIsSelected)?Font.BOLD:Font.PLAIN));
            this.add(component, BorderLayout.CENTER);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // if the item was selected, then paint the custom Mac selection background.
            if (fIsSelected) {
                Border backgroundPainter = fIsFocused
                    ? UIManager.getBorder("List.sourceListFocusedSelectionBackgroundPainter")
                    : UIManager.getBorder("List.sourceListSelectionBackgroundPainter");
                backgroundPainter.paintBorder(this, g, 0, 0, getWidth(), getHeight());
            }
        }
    }
}
