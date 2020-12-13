/*
 * This source is an adaption from the MacWindgets
 * http://code.google.com/p/macwidgets/
 * which are licensed under the GNU Lesser GPL
 */
package de.danielluedecke.zettelkasten.mac;

import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.util.Constants;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.SourceListBadgeContentProvider;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListColorScheme;
import com.explodingpixels.macwidgets.SourceListCountBadgeRenderer;
import com.explodingpixels.macwidgets.SourceListStandardColorScheme;
import com.explodingpixels.painter.FocusStatePainter;
import com.explodingpixels.painter.RectanglePainter;
import com.explodingpixels.widgets.IconProvider;
import com.explodingpixels.widgets.TextProvider;
import com.explodingpixels.widgets.TreeUtils;
import com.explodingpixels.widgets.WindowUtils;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.util.TreeUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author danielludecke
 */
public class MacSourceDesktopTree extends BasicTreeUI {

    private static final Font CATEGORY_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 12.0f);
    private static final Font ITEM_FONT = UIManager.getFont("Label.font").deriveFont(12.0f);
    private static final Font ITEM_SELECTED_FONT = ITEM_FONT.deriveFont(Font.BOLD);

    private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    private SourceListColorScheme fColorScheme;
    private FocusStatePainter fBackgroundPainter;
    private FocusStatePainter fSelectionBackgroundPainter;

    private final String EXPAND_NODE = "expandNode";
    private final String COLLAPSE_NODE = "collapseNode";

    private final CustomTreeModelListener fTreeModelListener = new CustomTreeModelListener();

    private final DesktopData desktopObj;
    private final Daten dataObj;
    private final Settings settingsObj;

    public MacSourceDesktopTree(DesktopData desk, Daten dat, Settings set) {
        desktopObj = desk;
        dataObj = dat;
        settingsObj = set;
    }

    @Override
    protected void completeUIInstall() {
        super.completeUIInstall();

        tree.setSelectionModel(new SourceListTreeSelectionModel());

        tree.setOpaque(false);
        tree.setRootVisible(true);
        tree.setLargeModel(true);
        tree.setShowsRootHandles(true);
        // TODO key height off font size.
        tree.setRowHeight(20);

        // install the default color scheme.
        setColorScheme(new SourceListStandardColorScheme());
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        // install a property change listener that repaints the JTree when the parent window's
        // focus state changes.
        WindowUtils.installJComponentRepainterOnWindowFocusChanged(tree);
    }

    @Override
    protected void installKeyboardActions() {
        super.installKeyboardActions();
        tree.getInputMap().put(KeyStroke.getKeyStroke("pressed RIGHT"), EXPAND_NODE);
        tree.getInputMap().put(KeyStroke.getKeyStroke("pressed LEFT"), COLLAPSE_NODE);
        tree.getActionMap().put(EXPAND_NODE, expandNodeAction());
        tree.getActionMap().put(COLLAPSE_NODE, collapseNodeAction());
    }

    private Action expandNodeAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tree.getLeadSelectionRow();
                tree.expandRow(selectedRow);
            }
        };
    }

    private Action collapseNodeAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tree.getLeadSelectionRow();
                tree.collapseRow(selectedRow);
            }
        };
    }

    @Override
    protected void setModel(TreeModel model) {
        // if there was a previously installed TreeModel, uninstall our listener from it.
        if (treeModel != null) {
            treeModel.removeTreeModelListener(fTreeModelListener);
        }

        super.setModel(model);

        // install our listener on the new TreeModel if neccessary.
        if (model != null) {
            model.addTreeModelListener(new CustomTreeModelListener());
        }
    }

    /**
     * Gets the {@link SourceListColorScheme} that this {@code SourceListTreeUI}
     * uses to paint.
     *
     * @return the {@link SourceListColorScheme} that this {@code SourceList}
     * uses to paint.
     */
    public SourceListColorScheme getColorScheme() {
        return fColorScheme;
    }

    /**
     * Sets the {@link SourceListColorScheme} that this {@code SourceListTreeUI}
     * uses to paint.
     *
     * @param colorScheme the {@link SourceListColorScheme} that this
     * {@code SourceList} uses to paint.
     */
    public void setColorScheme(SourceListColorScheme colorScheme) {
        checkColorSchemeNotNull(colorScheme);
        fColorScheme = colorScheme;
        fBackgroundPainter = new FocusStatePainter(
                new RectanglePainter(fColorScheme.getActiveBackgroundColor()),
                new RectanglePainter(fColorScheme.getActiveBackgroundColor()),
                new RectanglePainter(fColorScheme.getInactiveBackgroundColor()));
//        fBackgroundPainter = new FocusStatePainter(
//                new RectanglePainter(Color.WHITE),
//                new RectanglePainter(Color.WHITE),
//                new RectanglePainter(Color.WHITE));
        fSelectionBackgroundPainter = new FocusStatePainter(
                fColorScheme.getActiveFocusedSelectedItemPainter(),
                fColorScheme.getActiveUnfocusedSelectedItemPainter(),
                fColorScheme.getInactiveSelectedItemPainter());

        // create a new tree cell renderer in order to pick up the new colors.
        tree.setCellRenderer(new SourceListTreeCellRenderer());
        installDisclosureIcons();
    }

    private void installDisclosureIcons() {
        // install the collapsed and expanded icons as well as the margins to indent nodes.
        setCollapsedIcon(fColorScheme.getUnselectedCollapsedIcon());
        setExpandedIcon(fColorScheme.getUnselectedExpandedIcon());
        int indent = fColorScheme.getUnselectedCollapsedIcon().getIconWidth() / 2 + 2;
        setLeftChildIndent(indent);
        setRightChildIndent(indent);
    }

    @Override
    protected void paintExpandControl(Graphics g, Rectangle clipBounds, Insets insets,
            Rectangle bounds, TreePath path, int row, boolean isExpanded,
            boolean hasBeenExpanded, boolean isLeaf) {
        // if the given path is selected, then
        boolean isPathSelected = tree.getSelectionModel().isPathSelected(path);

        Icon expandIcon = isPathSelected ? fColorScheme.getSelectedExpandedIcon()
                : fColorScheme.getUnselectedExpandedIcon();
        Icon collapseIcon = isPathSelected ? fColorScheme.getSelectedCollapsedIcon()
                : fColorScheme.getUnselectedCollapsedIcon();

        Object categoryOrItem
                = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        boolean setIcon = false;

        setExpandedIcon(setIcon ? expandIcon : null);
        setCollapsedIcon(setIcon ? collapseIcon : null);

        super.paintExpandControl(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
    }

    @Override
    protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
        return new NodeDimensionsHandler() {
            @Override
            public Rectangle getNodeDimensions(
                    Object value, int row, int depth, boolean expanded, Rectangle size) {

                Rectangle dimensions = super.getNodeDimensions(value, row, depth, expanded, size);
                int containerWidth = tree.getParent() instanceof JViewport
                        ? tree.getParent().getWidth() : tree.getWidth();

                dimensions.width = containerWidth - getRowX(row, depth);

                return dimensions;
            }
        };
    }

    @Override
    public Rectangle getPathBounds(JTree tree, TreePath path) {
        Rectangle bounds = super.getPathBounds(tree, path);
        // if there are valid bounds for the given path, then stretch them to fill the entire width
        // of the tree. this allows repaints on focus events to follow the standard code path, and
        // still repaint the entire selected area.
        if (bounds != null) {
            bounds.x = 0;
            bounds.width = tree.getWidth();
        }
        return bounds;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        // TODO use c.getVisibleRect to trim painting to minimum rectangle.
        // paint the background for the tree.
        Graphics2D backgroundGraphics = (Graphics2D) g.create();
        fBackgroundPainter.paint(backgroundGraphics, c, c.getWidth(), c.getHeight());
        backgroundGraphics.dispose();

        // TODO use c.getVisibleRect to trim painting to minimum rectangle.
        // paint the background for the selected entry, if there is one.
        int selectedRow = getSelectionModel().getLeadSelectionRow();
        if (selectedRow >= 0 && tree.isVisible(tree.getPathForRow(selectedRow))) {

            Rectangle bounds = tree.getRowBounds(selectedRow);

            Graphics2D selectionBackgroundGraphics = (Graphics2D) g.create();
            selectionBackgroundGraphics.translate(0, bounds.y);
            fSelectionBackgroundPainter.paint(
                    selectionBackgroundGraphics, c, c.getWidth(), bounds.height);
            selectionBackgroundGraphics.dispose();
        }

        super.paint(g, c);
    }

    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
        // do nothing - don't paint horizontal lines.
    }

    @Override
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets,
            TreePath path) {
        // do nothing - don't paint vertical lines.
    }

    @Override
    protected void selectPathForEvent(TreePath path, MouseEvent event) {
        // only forward on the selection event if an area other than the expand/collapse control
        // was clicked. this typically isn't an issue with regular Swing JTrees, however, SourceList
        // tree nodes fill the entire width of the tree. thus their bounds are underneath the
        // expand/collapse control.
        if (!isLocationInExpandControl(path, event.getX(), event.getY())) {
            super.selectPathForEvent(path, event);
        }
    }

    // Utility methods. ///////////////////////////////////////////////////////////////////////////
    private boolean isCategoryRow(int row) {
        return !isItemRow(row);
    }

    private boolean isItemRow(int row) {
        return isItemPath(tree.getPathForRow(row));
    }

    private boolean isItemPath(TreePath path) {
        return path != null && path.getPathCount() > 2;
    }

//    private boolean isItemRow(int row) {
//        TreePath path = tree.getPathForRow(row);
//        if (path!=null) {
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//            if (node!=null) {
//                return !node.getAllowsChildren();
//            }
//        }
//        return false;
//    }
    private String getTextForNode(TreeNode node, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        String retVal;

        if (node instanceof DefaultMutableTreeNode
                && ((DefaultMutableTreeNode) node).getUserObject() instanceof TextProvider) {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            retVal = ((TextProvider) userObject).getText();
        } else {
            retVal = tree.convertValueToText(node, selected, expanded, leaf, row, hasFocus);
        }

        return retVal;
    }

    private Icon getIconForNode(TreeNode node) {
        Icon retVal = null;
        if (node instanceof DefaultMutableTreeNode
                && ((DefaultMutableTreeNode) node).getUserObject() instanceof IconProvider) {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            retVal = ((IconProvider) userObject).getIcon();
        }
        return retVal;
    }

    private static void checkColorSchemeNotNull(SourceListColorScheme colorScheme) {
        if (colorScheme == null) {
            throw new IllegalArgumentException("The given SourceListColorScheme cannot be null.");
        }
    }

    private boolean isCommentNode(Object value) {
        // if no value return
        if (null == value) {
            return false;
        }
        // retrieve node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        // when root, return
        if (node.isRoot()) {
            return false;
        }
        return (!desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>").isEmpty());
    }

    /**
     * This method checks whether an entry of a selected node has follower
     * numbers or not.
     *
     * @param value the selected node
     * @return {@code true} if the entry of the selected node has followers,
     * {@code false} otherwise.
     */
    protected boolean isLuhmannNode(Object value) {
        // if no value return
        if (null == value || !settingsObj.getShowLuhmannIconInDesktop()) {
            return false;
        }
        // retrieve node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        // when root, return
        if (node.isRoot()) {
            return false;
        }
        // else, get entry number of selected node
        int entry = TreeUtil.extractEntryNumberFromNode(node);
        // no entry selected? then return false
        if (-1 == entry) {
            return false;
        }
        // return, whether entry has followers
        return (dataObj.hasLuhmannNumbers(entry));
    }

    // Custom TreeModelListener. //////////////////////////////////////////////////////////////////
    private class CustomTreeModelListener implements TreeModelListener {

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            // no implementation.
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            if (tree != null) {
                Object root = tree.getModel().getRoot();
                if (root != null) {
                    TreePath pathToRoot = new TreePath(root);
                    if (path != null && path.getParentPath() != null
                            && path.getParentPath().getLastPathComponent().equals(root)
                            && !tree.isExpanded(pathToRoot)) {
                        TreeUtils.expandPathOnEdt(tree, new TreePath(root));
                    }
                }
            }
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            // no implementation.
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            // no implementation.
        }
    }

    // Custom TreeCellRenderer. ///////////////////////////////////////////////////////////////////
    private class SourceListTreeCellRenderer implements TreeCellRenderer {

        private final CategoryTreeCellRenderer iCategoryRenderer = new CategoryTreeCellRenderer();

        private final ItemTreeCellRenderer iItemRenderer = new ItemTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            TreeCellRenderer render = isCategoryRow(row) ? iCategoryRenderer : iItemRenderer;
            return render.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
        }

    }

    private class CategoryTreeCellRenderer implements TreeCellRenderer {

//        private JLabel fLabel = MacWidgetFactory.makeEmphasizedLabel(new JLabel(),
//                CConstants.colorDarkLabelGray,
//                CConstants.colorDarkLabelGray,
//                CConstants.colorDarkLabelShadowGray);
        private final JLabel fLabel = MacWidgetFactory.makeEmphasizedLabel(new JLabel(),
                fColorScheme.getCategoryTextColor(),
                fColorScheme.getCategoryTextColor(),
                fColorScheme.getCategoryTextShadowColor());

        private final JLabel fSelectedCatLabel = MacWidgetFactory.makeEmphasizedLabel(new JLabel(),
                fColorScheme.getSelectedItemTextColor(),
                fColorScheme.getSelectedItemTextColor(),
                fColorScheme.getSelectedItemFontShadowColor());

        private CategoryTreeCellRenderer() {
            fLabel.setFont(CATEGORY_FONT);
            fSelectedCatLabel.setFont(CATEGORY_FONT);
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            TreeNode node = (TreeNode) value;
            JLabel label = selected ? fSelectedCatLabel : fLabel;
            label.setText(getTextForNode(node, selected, expanded, leaf, row, hasFocus));
            if (isCommentNode(value)) {
                label.setIcon(Constants.iconDesktopComment);
            } else if (isLuhmannNode(value)) {
                label.setIcon(Constants.iconDesktopLuhmann);
            } else {
                label.setIcon(null);
            }
            return label;
        }
    }

    private class ItemTreeCellRenderer implements TreeCellRenderer {

        private final PanelBuilder fBuilder;

        private final SourceListCountBadgeRenderer fCountRenderer = new SourceListCountBadgeRenderer(
                fColorScheme.getSelectedBadgeColor(), fColorScheme.getActiveUnselectedBadgeColor(),
                fColorScheme.getInativeUnselectedBadgeColor(), fColorScheme.getBadgeTextColor());

        private final JLabel fSelectedLabel = MacWidgetFactory.makeEmphasizedLabel(new JLabel(),
                fColorScheme.getSelectedItemTextColor(),
                fColorScheme.getSelectedItemTextColor(),
                fColorScheme.getSelectedItemFontShadowColor());

        private final JLabel fUnselectedLabel = MacWidgetFactory.makeEmphasizedLabel(new JLabel(),
                fColorScheme.getUnselectedItemTextColor(),
                fColorScheme.getUnselectedItemTextColor(),
                TRANSPARENT_COLOR);

        private ItemTreeCellRenderer() {
            fSelectedLabel.setFont(ITEM_SELECTED_FONT);
            fUnselectedLabel.setFont(ITEM_FONT);

            // definte the FormLayout columns and rows.
            FormLayout layout = new FormLayout("fill:0px:grow, 5px, p, 5px", "3px, fill:p:grow, 3px");
            // create the builders with our panels as the component to be filled.
            fBuilder = new PanelBuilder(layout);
            fBuilder.getPanel().setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            TreeNode node = (TreeNode) value;
            JLabel label = selected ? fSelectedLabel : fUnselectedLabel;
            label.setText(getTextForNode(node, selected, expanded, leaf, row, hasFocus));
            label.setIcon(getIconForNode(node));
            if (isCommentNode(value)) {
                label.setIcon(Constants.iconDesktopComment);
            } else if (isLuhmannNode(value)) {
                label.setIcon(Constants.iconDesktopLuhmann);
            }

            fBuilder.getPanel().removeAll();
            CellConstraints cc = new CellConstraints();
            fBuilder.add(label, cc.xywh(1, 1, 1, 3));

            if (value instanceof DefaultMutableTreeNode
                    && ((DefaultMutableTreeNode) value).getUserObject() instanceof SourceListBadgeContentProvider) {
                Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
                SourceListBadgeContentProvider badgeContentProvider
                        = (SourceListBadgeContentProvider) userObject;
                if (badgeContentProvider.getCounterValue() > 0) {
                    fBuilder.add(fCountRenderer.getComponent(), cc.xy(3, 2, "center, fill"));
                    fCountRenderer.setState(badgeContentProvider.getCounterValue(), selected);
                }
            }

            return fBuilder.getPanel();
        }
    }

    // SourceListTreeSelectionModel implementation. ///////////////////////////////////////////////
    private class SourceListTreeSelectionModel extends DefaultTreeSelectionModel {

        public SourceListTreeSelectionModel() {
            setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
//
//        private boolean canSelect(TreePath path) {
//            return true;
////            return isItemPath(path);
//        }
//
//        @Override
//        public void setSelectionPath(TreePath path) {
//            if (canSelect(path)) {
//                super.setSelectionPath(path);
//            }
//        }
//
//        @Override
//        public void setSelectionPaths(TreePath[] paths) {
//            if (canSelect(paths[0])) {
//                super.setSelectionPaths(paths);
//            }
//        }
    }
}
