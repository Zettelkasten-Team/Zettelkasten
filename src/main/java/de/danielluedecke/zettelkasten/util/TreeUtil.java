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

import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.classes.TreeUserObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Luedeke
 */
public class TreeUtil {

    private static final List<String> collapsedNodes = new ArrayList<>();

    private static int expandLevel = -1;

    /**
     * Sets the level to which sub branches a tree will be expanded.
     *
     * @param level the "depth" of the tree expansion.
     */
    public static void setExpandLevel(int level) {
        expandLevel = level;
    }

    public static int getExpandLevel() {
        return expandLevel;
    }

    /**
     * This method extracts a node's ID.
     *
     * @param node the node where we want to extract the ID
     * @return the ID of the node's name (userobject) as string, or {@code null}
     * if an error occured or nothing was found.
     */
    public static boolean nodeIsRoot(DefaultMutableTreeNode node) {
        if (node != null) {
            TreeUserObject userObject = (TreeUserObject) node.getUserObject();
            return userObject.getId().equals(Constants.ROOT_ID_NAME);
        }
        return false;
    }

    /**
     * This method returns the text of a node which is used in the DesktopFrame.
     * This node usually has an id in its text. This method cuts off this id, so
     * only the "cleaned" node-text wil be returned.
     *
     * @param node the node which text should be retrieved
     * @return a "cleaned" node-text with the id truncated, or {@code null} if
     * an error occured.
     */
    public static String getNodeText(DefaultMutableTreeNode node) {
        String uebertext = null;
        if (node != null) {
            TreeUserObject userObject = (TreeUserObject) node.getUserObject();
            uebertext = userObject.toString();
        }
        return uebertext;
    }

    /**
     * This method extracts a node's ID.
     *
     * @param node the node where we want to extract the ID
     * @return the ID of the node's name (userobject) as string, or {@code null}
     * if an error occured or nothing was found.
     */
    public static String getNodeID(DefaultMutableTreeNode node) {
        if (node != null) {
            TreeUserObject userObject = (TreeUserObject) node.getUserObject();
            return userObject.getId();
        }
        return null;
    }

    /**
     * This method extracts the timestamp-id of a node's name.
     *
     * @param node the node where we want to extract the timestamp-id
     * @return the timestamp-id of the node's name (userobject) as string, or
     * {@code null} if an error occured or nothing was found.
     */
    public static String getNodeTimestamp(DefaultMutableTreeNode node) {
        if (node != null) {
            TreeUserObject userObject = (TreeUserObject) node.getUserObject();
            return userObject.getId();
        }
        return null;
    }

    /**
     * This method extracts an entry's number from a node's text (userobject)
     * and returns it as integer-value
     *
     * @param node the node from which the entry-number should be extracted
     * @return the entry-number, or -1 if an error occured.
     */
    public static int extractEntryNumberFromNode(DefaultMutableTreeNode node) {
        TreeUserObject userObject = (TreeUserObject) node.getUserObject();
        String entrynumber = userObject.getNr();
        if (entrynumber != null && !entrynumber.isEmpty()) {
            try {
                return Integer.parseInt(entrynumber);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private static void expandAllTrees(TreePath parent, JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAllTrees(path, tree);
            }
        }
        // retrieve treenode user object
        TreeUserObject userObject = (TreeUserObject) node.getUserObject();
        // check whether deepest level is reached.
        if ((expandLevel != -1 && node.getLevel() < expandLevel) || -1 == expandLevel) {
            // check whether treenode-id is in the list of collapsed items
            if (userObject != null && collapsedNodes.contains(userObject.getId())) {
                // if yes, collapse treenode
                tree.collapsePath(parent);
            } else {
                // else expand it
                tree.expandPath(parent);
            }
        } else {
            tree.collapsePath(parent);
        }
    }

    /**
     * If expand is true, expands all nodes in the tree. Otherwise, collapses
     * all nodes in the tree.
     *
     * @param tree
     */
    public static void expandAllTrees(JTree tree) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        expandAllTrees(new TreePath(root), tree);
    }

    private static void expandAllTrees(TreePath parent, boolean expand, JTree tree) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAllTrees(path, expand, tree);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    /**
     * If expand is true, expands all nodes in the tree. Otherwise, collapses
     * all nodes in the tree.
     *
     * @param expand
     * @param tree
     */
    public static void expandAllTrees(boolean expand, JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAllTrees(new TreePath(root), expand, tree);
    }

    private static void retrieveCollapsedNodes(TreePath parent, JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                retrieveCollapsedNodes(path, tree);
            }
        }
        // retrieve treenode user object
        TreeUserObject userObject = (TreeUserObject) node.getUserObject();
        // check for valid value
        if (userObject != null && userObject.isCollapsed()) {
            // if treenode is collapsed, remember state
            collapsedNodes.add(userObject.getId());
        }
    }

    /**
     * If expand is true, expands all nodes in the tree. Otherwise, collapses
     * all nodes in the tree.
     *
     * @param tree
     * @return
     */
    public static List<String> retrieveCollapsedNodes(JTree tree) {
        // clear collapsed nodes
        collapsedNodes.clear();
        // if we have no tree, do nothing
        if (null == tree) {
            return null;
        }
        // get root
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        // if we have no root, return
        if (null == root) {
            return null;
        }
        // else retrieve all collapsed nodes and save their ID's in a list
        retrieveCollapsedNodes(new TreePath(root), tree);
        return collapsedNodes;
    }

    public static String retrieveNodeTitle(Daten data, boolean showNumber, String nr) {
        StringBuilder sb = new StringBuilder("");
        String zettelTitle = data.getZettelTitle(Integer.parseInt(nr));
        if (showNumber || zettelTitle.isEmpty()) {
            sb.append(nr);
            if (!zettelTitle.isEmpty()) {
                sb.append(": ");
            }
        }
        if (!zettelTitle.isEmpty()) {
            sb.append(zettelTitle);
        }
        return sb.toString();
    }

    /**
     * Clears the array that saves all currently collapsed nodes from the
     * jLuhmannTree, so the tree cabn be fully expanded.
     */
    public static void resetCollapsedNodes() {
        // clear collapsed nodes
        collapsedNodes.clear();
    }

    /**
     * This method selects the first entry in a jTree that start with the text
     * that is entered in the filter-textfield.
     *
     * @param tree the jTree where the item should be selected
     * @param textfield the related filtertextfield that contains the user-input
     */
    public static void selectByTyping(javax.swing.JTree tree, javax.swing.JTextField textfield) {
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        MutableTreeNode root = (MutableTreeNode) dtm.getRoot();
        String text = textfield.getText().toLowerCase();
        if (root != null && !text.isEmpty()) {
            for (int cnt = 0; cnt < dtm.getChildCount(root); cnt++) {
                MutableTreeNode child = (MutableTreeNode) dtm.getChild(root, cnt);
                String childtext = child.toString().toLowerCase();
                if (childtext.startsWith(text)) {
                    TreePath tp = new TreePath(root);
                    tp = tp.pathByAddingChild(child);
                    tree.setSelectionPath(tp);
                    tree.scrollPathToVisible(tp);
                    return;
                }
            }
        }
    }
}
