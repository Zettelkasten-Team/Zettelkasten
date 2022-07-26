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

import de.danielluedecke.zettelkasten.EntryID;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.util.classes.TreeUserObject;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

	private static final Map<String, Boolean> collapsedNodes = new HashMap<String, Boolean>();

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
	 * @return the ID of the node's name (userobject) as string, or {@code null} if
	 *         an error occured or nothing was found.
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
	 * @return a "cleaned" node-text with the id truncated, or {@code null} if an
	 *         error occured.
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
	 * @return the ID of the node's name (userobject) as string, or {@code null} if
	 *         an error occured or nothing was found.
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
	 *         {@code null} if an error occured or nothing was found.
	 */
	public static String getNodeTimestamp(DefaultMutableTreeNode node) {
		if (node != null) {
			TreeUserObject userObject = (TreeUserObject) node.getUserObject();
			return userObject.getId();
		}
		return null;
	}

	/**
	 * This method extracts an entry's number from a node's text (userobject) and
	 * returns it as integer-value
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

	private static void expandAllTrees(TreePath root, JTree tree) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getLastPathComponent();
		// Handle the whole tree.
		if (node.getChildCount() >= 0) {
			for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
				TreePath path = root.pathByAddingChild(n);
				expandAllTrees(path, tree);
			}
		}

		// Handle root.

		// Respect collapsedNodes if it exists.
		if (!collapsedNodes.isEmpty()) {
			TreeUserObject userObject = (TreeUserObject) node.getUserObject();
			if (userObject != null && collapsedNodes.get(userObject.getId())) {
				tree.collapsePath(root);
			} else {
				tree.expandPath(root);
			}
		} else {
			// Respect expandLevel.
			if (expandLevel == -1 || node.getLevel() < expandLevel) {
				tree.expandPath(root);
			} else {
				tree.collapsePath(root);
			}
		}
	}

	/**
	 * If expand is true, expands all nodes in the tree. Otherwise, collapses all
	 * nodes in the tree.
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
			for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements();) {
				TreeNode n = e.nextElement();
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
	 * If expand is true, expands all nodes in the tree. Otherwise, collapses all
	 * nodes in the tree.
	 *
	 * @param expand
	 * @param tree
	 */
	public static void expandAllTrees(boolean expand, JTree tree) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		expandAllTrees(new TreePath(root), expand, tree);
	}

	private static void saveCollapsedNodes(TreePath nodePath, JTree tree) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodePath.getLastPathComponent();
		// Save the whole tree.
		if (node.getChildCount() >= 0) {
			for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
				TreePath path = nodePath.pathByAddingChild(n);
				saveCollapsedNodes(path, tree);
			}
		}

		TreeUserObject userObject1 = (TreeUserObject) node.getUserObject();
		if (userObject1 != null && userObject1.getId().equals("3")) {
			userObject1 = null;
		}
		if (userObject1 != null && userObject1.getId().equals("4")) {
			userObject1 = null;
		}

		// Save the current node.
		TreeUserObject userObject = (TreeUserObject) node.getUserObject();
		if (userObject != null) {
			collapsedNodes.put(userObject.getId(), userObject.isCollapsed());
		}
	}

	/**
	 * If expand is true, expands all nodes in the tree. Otherwise, collapses all
	 * nodes in the tree.
	 *
	 * @param tree
	 * @return
	 */
	public static void saveCollapsedNodes(JTree tree) {
		collapsedNodes.clear();
		if (tree == null) {
			return;
		}
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		if (root == null) {
			return;
		}

		saveCollapsedNodes(new TreePath(root), tree);
	}

	public static String getEntryDisplayText(Daten data, boolean showNumber, EntryID entry) {
		StringBuilder sb = new StringBuilder("");
		String entryTitle = data.getZettelTitle(entry.asInt());

		// If title is empty, we use its number.
		if (showNumber || entryTitle.isEmpty()) {
			sb.append(entry.asString());
		}
		if (!entryTitle.isEmpty()) {
			sb.append(entryTitle);
		}
		return sb.toString();
	}

	/**
	 * Clears the array that saves all currently collapsed nodes from the
	 * jLuhmannTree, so the tree can be fully expanded.
	 */
	public static void resetCollapsedNodes() {
		collapsedNodes.clear();
	}

	/**
	 * This method selects the first entry in a jTree that start with the text that
	 * is entered in the filter-textfield.
	 *
	 * @param tree      the jTree where the item should be selected
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
