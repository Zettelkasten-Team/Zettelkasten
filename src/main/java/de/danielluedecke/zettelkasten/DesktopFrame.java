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
package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.misc.EntryStringTransferHandler;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.util.misc.InitStatusbarForTasks;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.Bookmarks;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import de.danielluedecke.zettelkasten.tasks.export.ExportTools;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import de.danielluedecke.zettelkasten.util.TreeUtil;
import de.danielluedecke.zettelkasten.util.misc.TreeUserObject;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author danielludecke
 */
public class DesktopFrame extends javax.swing.JFrame implements WindowListener {

    /**
     * Reference to the main data class.
     */
    private final Daten dataObj;
    /**
     *
     */
    private final TasksData taskdata;
    /**
     * Reference to the desktop data class.
     */
    private final DesktopData desktopObj;
    /**
     * Reference to the bookmarks data class.
     */
    private final Bookmarks bookmarksObj;
    /**
     * Reference to the settings class.
     */
    private final Settings settingsObj;
    /**
     *
     */
    private final BibTex bibtexObj;
    /**
     *
     */
    private final AutoKorrektur spellObj;
    /**
     *
     */
    private final StenoData stenoObj;
    /**
     * CAccelerator object, which contains the XML data of the accelerator table
     * for the menus
     */
    private final AcceleratorKeys accKeys;
    /**
     *
     */
    private createDisplayTask cDisplayTask = null;
    /**
     * This variable indicates whether the task that updates the display is
     * already running or not...
     */
    private boolean cDisplayTaskIsRunning = false;
    /**
     * Indicates whether the main entries' headings are visible or not.
     */
    private boolean isHeadingVisible = true;
    private boolean isLuhmannIconVisible;
    /**
     *
     */
    private boolean isEntryNumberVisible = true;
    /**
     *
     */
    private boolean isEditModeActive = false;
    private boolean isSidebarHidden = true;
    /**
     * Indicates the number of that entry that should be immediately selected
     * after the treeview is updated
     */
    private int displayentrynr = -1;
    /**
     * Flag that indicates whether the message box warning when adding multiple
     * entries should only pop up once and not for each multiple entry
     */
    private boolean showMultipleEntryMsg;
    /**
     *
     */
    private String editModeTimeStamp = "";
    /**
     * Used for the {@link #addLuhmann addLuhmann} Action.
     */
    private StringBuilder luhmannnumbers;
    /**
     * This value stores entries that can be inserted using the
     * {@link #pasteNode() pasteNode()}.
     */
    public int[] clipEntries;
    /**
     * This variable indicates whether the entry in the clipboard is a bullet
     * (true) or a child-entry (false)
     */
    private boolean clipBullet = false;
    /**
     * This variable stores the name of the bullet that was copied or cut to the
     * clipboard. we need this information when inserting the bullet, so we can
     * avoid double bullet-entries (double names).
     */
    private String clipBulletName;
    /**
     * This variable stores modifications from entries that are cut or copied
     * within the jTree. Since cutting out an entry will delete it, the
     * timestamp reference to possible modifications is lost. To avoid this, we
     * store the modifications in this variable.
     */
    private String clipModifiedEntryContent = null;
    /**
     * This variable stores the treepath when a node was dragged&dropped within
     * the jtreedesktop
     */
    private DefaultMutableTreeNode movedNodeToRemove = null;
    /**
     *
     */
    private final String lineseparator;
    /**
     *
     */
    private String multipleOccurencesMessage;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;
    /**
     *
     */
    private int findlivepos;
    /**
     *
     */
    private int findlivemax = 1;
    /**
     * create a new stringbuilder that will contain the plain text of entries in
     * the editorpane, so we can count the words
     */
    private final StringBuilder sbWordCountDisplayTask = new StringBuilder("");
    /**
     *
     */
    private String completePage = "";
    /**
     * This variable gets the graphic device and ist needed for
     * full-screen-functionality. see {@link #viewFullScreen() viewFullScreen()}
     * for more details.
     */
    private final GraphicsDevice graphicdevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    /**
     *
     */
    private final JFrame mainframe;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(DesktopFrame.class);
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap toolbarResourceMap
            = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getResourceMap(ToolbarIcons.class);

    /**
     *
     * @param zkn
     * @param td
     * @param d
     * @param bm
     * @param dk
     * @param s
     * @param ak
     * @param bt
     * @param st
     * @param auk
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public DesktopFrame(ZettelkastenView zkn, TasksData td, Daten d, Bookmarks bm, DesktopData dk, Settings s, AcceleratorKeys ak, BibTex bt, AutoKorrektur auk, StenoData st) {
        // reference needed for fullscreen
        mainframe = this;

        dataObj = d;
        taskdata = td;
        desktopObj = dk;
        settingsObj = s;
        accKeys = ak;
        bibtexObj = bt;
        zknframe = zkn;
        spellObj = auk;
        stenoObj = st;
        bookmarksObj = bm;
        isLuhmannIconVisible = settingsObj.getShowLuhmannIconInDesktop();
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. Desktop Window opened.");
        }
        // init all components
        initComponents();
        initListeners();
        initBorders(settingsObj);
        headingsVisibleMenuItem.setSelected(isHeadingVisible);
        entryNumberVisibleMenuItem.setSelected(isEntryNumberVisible);
        luhmannIconVisible.setSelected(isLuhmannIconVisible);
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        if (settingsObj.isSeaGlass()) {
            setupSeaGlassStyle();
        }
        // hide live-searchpanel on init.
        jPanelLiveSearch.setVisible(false);
        // retrieve system's line-separator
        lineseparator = System.lineSeparator();
        // init the progressbar and animated icon for background tasks
        InitStatusbarForTasks isb = new InitStatusbarForTasks(statusAnimationLabel, null, null);
        // set click to update-text
        jEditorPaneMain.setText(resourceMap.getString("clickToUpdateText"));
        jButtonShowMultipleOccurencesDlg.setVisible(false);
        // init the accelerator keys
        initAcceleratorTable();
        // This method initialises the toolbar buttons. depending on the user-setting, we either
        // display small, medium or large icons as toolbar-icons.
        initToolbarIcons();
        // init jTree. we also initialise a DropTargetEvent here, so drag&drop-handling is
        // completeley initiated in this method
        initTree();
        // init drag&drop-operations for editor-panes, so the user can drag&drop entries
        // from the jTree to the editorpanes...
        initDropPanes(jTextArea1);
        initDropPanes(jTextArea2);
        initDropPanes(jTextArea3);
        initComboBox();
        initDefaultFontSize();
        updateTitle();
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jPanelLiveSearch.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorUtil.getBorderGray(settingsObj)));
        if (settingsObj.isSeaGlass()) {
            jSplitPaneDesktop2.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
            jTextArea1.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextArea1.border.title"), settingsObj));
            jTextArea2.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextArea2.border.title"), settingsObj));
            jTextArea3.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextArea3.border.title"), settingsObj));
        }
    }

    /**
     *
     */
    public final void updateTitle() {
        String currentTitle = getTitle();
        // get filename and find out where extension begins, so we can just set the filename as title
        File f = settingsObj.getFilePath();
        // check whether we have any valid filepath at all
        if (f != null && f.exists()) {
            String fname = f.getName();
            // find file-extension
            int extpos = fname.lastIndexOf(Constants.ZKN_FILEEXTENSION);
            // set the filename as title
            if (extpos != -1) {
                // show proxy-icon, only applies to mac.
                getRootPane().putClientProperty("Window.documentFile", f);
                // set file-name and app-name in title-bar
                setTitle(currentTitle + " - [" + fname.substring(0, extpos) + "]");
            }
        }
    }

    /**
     *
     */
    private void initListeners() {
        // <editor-fold defaultstate="collapsed" desc="Here all relevant listeners are initiated.">
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                quitFullScreen();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(this);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showSearchResultWindow"));
        ActionListener showSearchResultsAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                zknframe.showSearchResultWindow();
            }
        };
        getRootPane().registerKeyboardAction(showSearchResultsAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showNewEntryWindow"));
        ActionListener showNewEntryFrameAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                zknframe.showNewEntryWindow();
            }
        };
        getRootPane().registerKeyboardAction(showNewEntryFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        ActionListener showMainFrameAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                zknframe.bringToFront();
            }
        };
        getRootPane().registerKeyboardAction(showMainFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea1, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea1, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextArea2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea2, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea2, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextArea3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea3, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCutCopyPaste.isVisible()) {
                    jPopupMenuCutCopyPaste.show(jTextArea3, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTreeDesktop.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuDesktop.isVisible()) {
                    jPopupMenuDesktop.show(jTreeDesktop, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuDesktop.isVisible()) {
                    jPopupMenuDesktop.show(jTreeDesktop, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // this listener should only react on left-mouse-button-clicks...
                // if other button then left-button clicked, leeave...
                if (evt.getButton() == MouseEvent.BUTTON1 && 2 == evt.getClickCount()) {
                    modifiyEntry();
                }
            }
        });
        jTreeDesktop.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                updateToolbarAndMenu();
                scrollToEntry();
            }
        });
        jTreeDesktop.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            @Override
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                changeBulletTreefold(evt, true);
            }

            @Override
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
                changeBulletTreefold(evt, false);
            }
        });
        jEditorPaneMain.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateView();
            }
        });
        jEditorPaneMain.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                // retrieve the event type, e.g. if a link was clicked by the user
                HyperlinkEvent.EventType typ = evt.getEventType();
                // get the description, to check whether we have a file or a hyperlink to a website
                String linktype = evt.getDescription();
                // if the link was clicked, proceed
                if (typ == HyperlinkEvent.EventType.ACTIVATED) {
                    // call method that handles the hyperlink-click
                    String returnValue = Tools.openHyperlink(linktype, mainframe, Constants.FRAME_DESKTOP, dataObj, bibtexObj, settingsObj, jEditorPaneMain, -1);
                    // check whether we have a return value. this might be the case either when the user clicked on
                    // a footenote, or on the rating-stars
                    if (returnValue != null && returnValue.startsWith("#z_")) {
                        // show entry
                        zknframe.showEntry(dataObj.getCurrentZettelPos());
                    }
                } else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    javax.swing.text.Element elem = evt.getSourceElement();
                    if (elem != null) {
                        AttributeSet attr = elem.getAttributes();
                        AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
                        if (a != null) {
                            jEditorPaneMain.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
                        }
                    }
                } else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    jEditorPaneMain.setToolTipText(null);
                }
            }
        });

        jTextFieldLiveSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // when the user presses the escape-key, hide panel
                if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
                    findCancel();
                } // when up/down arrows are pressed, find next/previous occurence of search term
                else if (Tools.isNavigationKey(evt.getKeyCode())) {
                    switch (evt.getKeyCode()) {
                        // if user pressed arrow down key, find next occurence of live search
                        case KeyEvent.VK_DOWN:
                            findLiveNext();
                            break;
                        // if user pressed arrow down key, find previous occurence of live search
                        case KeyEvent.VK_UP:
                            findLivePrev();
                            break;
                    }
                } // when user presses enter key, retrieve new search term and highlight term
                else if (KeyEvent.VK_ENTER == evt.getKeyCode() && jTextFieldLiveSearch.getText().length() > 1) {
                    updateLiveSearchDisplay();
                }
            }
        });
        //
        // init the menu-listeners...
        //
        desktopMenuFile.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                exportMultipleMenuItem.setEnabled(desktopObj.getCount() > 1);
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        desktopMenuView.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                // set selected state
                showDesktopTreeEntryNumberMenuItem.setSelected(settingsObj.getShowDesktopEntryNumber());
                // check whether we have any comments at all
                if (!desktopObj.desktopHasComments(desktopObj.getCurrentDesktopElement())) {
                    // if not, disable menu item
                    jRadioButtonDesktopOnlyComments.setEnabled(false);
                    // if view option was comments only, change this option
                    if (Constants.DESKTOP_ONLY_COMMENTS == settingsObj.getDesktopCommentDisplayOptions()) {
                        settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_WITH_COMMENTS);
                    }
                } else {
                    jRadioButtonDesktopOnlyComments.setEnabled(true);
                }
                switch (settingsObj.getDesktopCommentDisplayOptions()) {
                    case Constants.DESKTOP_WITH_COMMENTS:
                        jRadioButtonDesktopWithComment.setSelected(true);
                        break;
                    case Constants.DESKTOP_WITHOUT_COMMENTS:
                        jRadioButtonDesktopWithoutComments.setSelected(true);
                        break;
                    case Constants.DESKTOP_ONLY_COMMENTS:
                        jRadioButtonDesktopOnlyComments.setSelected(true);
                        break;
                    default:
                        jRadioButtonDesktopWithComment.setSelected(true);
                        break;
                }
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        // </editor-fold>
    }

    /**
     * This method changes a bullet-point's treefold-state, so it's remembered
     * when re-building the treeview.
     *
     * @param evt the {@code TreeExpansionEvent} which holds information about
     * the node that was expanded or collapsed
     * @param isExpanded {@code true} if the node was expandend, {@code false}
     * if it was collapsed. This information is retrieved from the
     * {@code addTreeExpansionListener}, see
     * {@link #initListeners() initListeners()}.
     */
    private void changeBulletTreefold(javax.swing.event.TreeExpansionEvent evt, boolean isExpanded) {
        // retrieve path of value that was expanded
        TreePath tp = evt.getPath();
        // check whether root was expanded or not. therefore, retrieve root-node
        // and the last node of the treepath, i.e. the node which was expanded
        TreeNode root = (TreeNode) jTreeDesktop.getModel().getRoot();
        TreeNode expandednode = (TreeNode) tp.getLastPathComponent();
        // if they equal, do nothing
        if (expandednode.equals(root) || TreeUtil.nodeIsRoot((DefaultMutableTreeNode) expandednode)) {
            return;
        }
        // else, retrieve node's timestamp
        String timestamp = TreeUtil.getNodeTimestamp((DefaultMutableTreeNode) tp.getLastPathComponent());
        // and set new treefold-state
        desktopObj.setBulletTreefold(timestamp, isExpanded);
    }

    /**
     * This method initiates the drag&drop-hanlding for the jEditorPanes. By
     * doing this, the user can drag entries from the jTree to the
     * editorpane(s), and the entry's content will be displayed in that
     * editorpane. it's just a more comfortable way to handle the notes a user
     * can make when working with the outliner-feature - in case the user wants
     * to keep entries permanently visible, this feature can be used.
     *
     * @param editorpane an existing jEditorPane, in this case we use the
     * components {@code jEditorPaneS1}, {@code jEditorPaneS2} and
     * {@code jEditorPaneS3}
     */
    private void initDropPanes(final javax.swing.JTextArea editorpane) {
        editorpane.setTransferHandler(new EntryStringTransferHandler() {
            @Override
            protected String exportString(JComponent c) {
                return editorpane.getSelectedText();
            }

            @Override
            protected boolean importString(JComponent c, String str) {
                // check for valid drop-string
                if (str != null) {
                    // each received string consists of two lines. the first one with information
                    // about the drag-source and the drag-operation, the second one with the data
                    // by this we can see whether we have received entries (i.e. a valid drop)
                    String[] dropinformation = str.split("\n");
                    // get source information
                    String[] sourceinfo = dropinformation[0].split(",");
                    // check out the source of the drag-operation. if we have a valid source,
                    // retrieve entries than.
                    if (sourceinfo.length > 1 && sourceinfo[1].equals(Constants.DRAG_SOURCE_TYPE_ENTRIES)) {
                        // we only accept drop-data from tables, so we might have several
                        // lines, separates by a new-line. thus, split all new lines into
                        // a string array.
                        String[] entries = dropinformation[1].split(",");
                        // if we have any dropped data, go on...
                        if (entries != null && entries.length > 0) {
                            // get converted entry
                            StringBuilder text = new StringBuilder("");
                            for (String entrie : entries) {
                                try {
                                    text.append(dataObj.getZettelContentAsHtml(Integer.parseInt(entrie)));
                                    text.append(lineseparator);
                                } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                                }
                            }
                            // add content to editor pane
                            editorpane.setText(text.toString());
                        }
                        return true;
                    } else {
                        editorpane.append(str);
                    }
                }
                return false;
            }

            @Override
            protected void cleanup(JComponent c, boolean remove) {
            }
        });
    }

    /**
     * This method initialises the toolbar buttons. depending on the
     * user-setting, we either display small, medium or large icons as
     * toolbar-icons.
     */
    public final void initToolbarIcons() {
        // check whether the toolbar should be displayed at all...
        if (!settingsObj.getShowIcons() && !settingsObj.getShowIconText()) {
            // if not, hide it and leave.
            jToolBarDesktop.setVisible(false);
            // and set a border to the main panel, because the toolbar's dark border is hidden
            // and remove border from the main panel
            jPanel1.setBorder(new MatteBorder(1, 0, 0, 0, ColorUtil.colorDarkLineGray));
            return;
        }
        // set toolbar visible
        jToolBarDesktop.setVisible(true);
        // and remove border from the main panel
        jPanel1.setBorder(null);
        // init toolbar button array
        javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[]{
            tb_newbullet, tb_newentry, tb_modifyentry, tb_cut, tb_copy, tb_paste,
            tb_rename, tb_comment, tb_delete, tb_refresh, tb_addluhmann,
            tb_moveup, tb_movedown
        };
        String[] buttonNames = new String[]{"tb_newbulletText", "tb_newdesktopentryText", "tb_editText",
            "tb_cutText", "tb_copyText", "tb_pasteText",
            "tb_renameText", "tb_commentText", "tb_deleteText",
            "tb_refreshText", "tb_addluhmannText",
            "tb_moveupText", "tb_movedownText"
        };
        String[] iconNames = new String[]{"newBulletIcon", "newDesktopEntryIcon", "editEntryIcon",
            "cutIcon", "copyIcon", "pasteIcon",
            "renameBulletIcon", "commentNodeIcon", "deleteIcon",
            "updateViewIcon", "addLuhmannIcon",
            "moveNodeUpIcon", "moveNodeDownIcon"
        };
        // set toolbar-icons' text
        if (settingsObj.getShowIconText()) {
            for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
                toolbarButtons[cnt].setText(toolbarResourceMap.getString(buttonNames[cnt]));
            }
        } else {
            for (javax.swing.JButton tbb : toolbarButtons) {
                tbb.setText("");
            }
        }
        // show icons, if requested
        if (settingsObj.getShowIcons()) {
            // retrieve icon theme path
            String icontheme = settingsObj.getIconThemePath();
            for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
                toolbarButtons[cnt].setIcon(new ImageIcon(ZettelkastenView.class.getResource(icontheme + toolbarResourceMap.getString(iconNames[cnt]))));
            }
        } else {
            for (javax.swing.JButton tbb : toolbarButtons) {
                tbb.setIcon(null);
            }
        }
        if (!settingsObj.getShowAllIcons()) {
            tb_addluhmann.setVisible(false);
            tb_rename.setVisible(false);
        }
        if (settingsObj.isSeaGlass()) {
            makeSeaGlassToolbar();
        }
    }

    private void setupSeaGlassStyle() {
        getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
        jTextFieldLiveSearch.putClientProperty("JTextField.variant", "search");
        jEditorPaneMain.setBackground(Color.white);
    }

    /**
     * This method applies some graphical stuff so the appearance of the program
     * is even more mac-like...
     */
    private void setupMacOSXLeopardStyle() {
        // <editor-fold defaultstate="collapsed" desc="This method applies some UI-stuff particular for Mac OS X">
        // now we have to change back the background-color of all components in the mainpart of the
        // frame, since the brush-metal-look applies to all components
        //
        // make searchfields look like mac
        jTextFieldLiveSearch.putClientProperty("JTextField.variant", "search");
        // other components become normal gray - which is, however, a little bit
        // darker than the default gray
        Color backcol = ColorUtil.getMacBackgroundColor();
        // on Leopard (OS X 10.5), we have different rendering, thus we need these lines
        if (PlatformUtil.isLeopard()) {
            mainframe.getContentPane().setBackground(backcol);
            jPanel1.setBackground(backcol);
        }
        jPanel3.setBackground(backcol);
        jPanel4.setBackground(backcol);
        jPanel5.setBackground(backcol);
        jPanel6.setBackground(backcol);
        jPanelLiveSearch.setBackground(backcol);
        jSplitPaneDesktop1.setBackground(backcol);
        jSplitPaneDesktop2.setBackground(backcol);
        // get the toolbar-action
        AbstractAction ac = (AbstractAction) org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(DesktopFrame.class, this).get("findCancel");
        // and change the large-icon-property, which is applied to the toolbar-icons,
        // to the new icon
        ac.putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSStopProgressFreestandingTemplate").getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        ac.putValue(AbstractAction.SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSStopProgressFreestandingTemplate").getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        // </editor-fold>
    }

    private void makeSeaGlassToolbar() {
        Tools.makeTexturedToolBarButton(tb_newbullet, Tools.SEGMENT_POSITION_FIRST);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_newentry, Tools.SEGMENT_POSITION_MIDDLE);
            Tools.makeTexturedToolBarButton(tb_addluhmann, Tools.SEGMENT_POSITION_LAST);
        } else {
            Tools.makeTexturedToolBarButton(tb_newentry, Tools.SEGMENT_POSITION_LAST);
        }
        Tools.makeTexturedToolBarButton(tb_modifyentry, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_cut, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_paste, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_moveup, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_movedown, Tools.SEGMENT_POSITION_LAST);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_rename, Tools.SEGMENT_POSITION_FIRST);
            Tools.makeTexturedToolBarButton(tb_comment, Tools.SEGMENT_POSITION_MIDDLE);
        } else {
            Tools.makeTexturedToolBarButton(tb_comment, Tools.SEGMENT_POSITION_FIRST);
        }
        Tools.makeTexturedToolBarButton(tb_delete, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_refresh, Tools.SEGMENT_POSITION_ONLY);
        jToolBarDesktop.setPreferredSize(new java.awt.Dimension(jToolBarDesktop.getSize().width, Constants.seaGlassToolbarHeight));
        jToolBarDesktop.add(new javax.swing.JToolBar.Separator(), 0);
    }

    /**
     * This method sets the default font-size for tables, lists and treeviews.
     * If the user wants to have bigger font-sizes for better viewing, the new
     * font-size will be applied to the components here.
     */
    private void initDefaultFontSize() {
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getDesktopOutlineFontSize();
        // get current font
        int fsize = jTreeDesktop.getFont().getSize();
        // retrieve default listvewfont
        Font defaultfont = settingsObj.getDesktopOutlineFont();
        // create new font, add fontsize-value
        Font f = new Font(defaultfont.getName(), defaultfont.getStyle(), fsize + defaultsize);
        // set new font
        jTreeDesktop.setFont(f);
    }

    /**
     * This method sets the accelerator table for all relevant actions which
     * should have accelerator keys. We don't use the GUI designer to set the
     * values, because the user should have the possibility to define own
     * accelerator keys, which are managed within the CAcceleratorKeys-class and
     * loaed/saved via the CSettings-class
     */
    private void initAcceleratorTable() {
        // setting up the accelerator table. we have two possibilities: either assigning
        // accelerator keys directly with an action like this:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get("newEntry");
        // KeyStroke controlN = KeyStroke.getKeyStroke("control N");
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, controlN);
        //
        // or setting the accelerator key directly to a menu-item like this:
        //
        // newEntryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK));
        //
        // we choose the first option, because so we can easily iterate through the xml file
        // and retrieve action names as well as accelerator keys. this saves a lot of typing work here
        //
        // get the action map
        javax.swing.ActionMap actionMap
                = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
                getContext().getActionMap(DesktopFrame.class, this);
        // iterate the xml file with the accelerator keys for the main window
        for (int cnt = 1; cnt <= accKeys.getCount(AcceleratorKeys.DESKTOPKEYS); cnt++) {
            // get the action's name
            String actionname = accKeys.getAcceleratorAction(AcceleratorKeys.DESKTOPKEYS, cnt);
            // check whether we have found any valid action name
            if (actionname != null && !actionname.isEmpty()) {
                // retrieve action
                AbstractAction ac = (AbstractAction) actionMap.get(actionname);
                // get the action's accelerator key
                String actionkey = accKeys.getAcceleratorKey(AcceleratorKeys.DESKTOPKEYS, cnt);
                // check whether we have any valid actionkey
                if (actionkey != null && !actionkey.isEmpty()) {
                    // retrieve keystroke setting
                    KeyStroke ks = KeyStroke.getKeyStroke(actionkey);
                    // and put them together :-)
                    ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);
                }
            }
        }
        // now set the mnemonic keys of the menus (i.e. the accelerator keys, which give access
        // to the menu via "alt"+key). since the menus might have different texts, depending on
        // the programs language, we retrieve the menu text and simply set the first char
        // as mnemonic key
        // ATTENTION! Mnemonic keys are NOT applied on Mac OS, see Apple guidelines for
        // further details:
        // http://developer.apple.com/DOCUMENTATION/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html#//apple_ref/doc/uid/TP40001909-211867-BCIBDHFJ
        // init the variables
        String menutext;
        char mkey;
        // the mnemonic key for the file menu
        menutext = desktopMenuFile.getText();
        mkey = menutext.charAt(0);
        desktopMenuFile.setMnemonic(mkey);
        // the mnemonic key for the edit menu
        menutext = desktopMenuEdit.getText();
        mkey = menutext.charAt(0);
        desktopMenuEdit.setMnemonic(mkey);
        // the mnemonic key for the view menu
        menutext = desktopMenuView.getText();
        mkey = menutext.charAt(0);
        desktopMenuView.setMnemonic(mkey);
        // on Mac OS, at least for the German locale, the File menu is called different
        // compared to windows or linux. Furthermore, we don't need the about and preferences
        // menu items, since these are locates on the program's menu item in the apple-menu-bar
        if (PlatformUtil.isMacOS()) {
            desktopMenuFile.setText(resourceMap.getString("macFileMenuText"));
        }
        // en- or disable fullscreen icons
        setFullScreenSupp(graphicdevice.isFullScreenSupported());
        // if fullscreen is not supportet, tell this in the tooltip
        if (!graphicdevice.isFullScreenSupported()) {
            AbstractAction ac = (AbstractAction) actionMap.get("viewFullScreen");
            ac.putValue(AbstractAction.SHORT_DESCRIPTION, resourceMap.getString("fullScreenNotSupported"));
        }
        // jTrees have their own input-map, so we have to re-assign the standard-shortcuts for
        // the jTree here... otherwise, copying nodes would lead to copy just the node's text,
        // and not - as we are intending - the node itself...
        String[] treeactions = new String[]{"cutNode", "copyNode", "pasteNode", "renameBullet", "moveNodeUp", "moveNodeDown"};
        // iterate the actions-array and change input-map for all relevant actions in the jTree
        for (String tac : treeactions) {
            // change tree's action-map for cut-action
            jTreeDesktop.getActionMap().put(tac + "KeyPressed", actionMap.get(tac));
            // associate new keystroke with that action
            KeyStroke ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.DESKTOPKEYS, tac));
            // and put the new keystroke-value to the tree's input-map
            jTreeDesktop.getInputMap().put(ks, tac + "KeyPressed");
        }
    }

    /**
     * This method inits the combobox. First it clears all existing items, then
     * it adds all available desktop-data and finally an actionlistener is added
     * to the jcombobox, that updates the display each time another desktop is
     * chosen.
     */
    private void initComboBox() {
        // clear combobox
        jComboBoxDesktop.removeAllItems();
        // fill combo box, if we have any items at all
        if (desktopObj.getCount() > 0) {
            // go through all desktops and add their names to the combobox
            for (int cnt = 0; cnt < desktopObj.getCount(); cnt++) {
                jComboBoxDesktop.addItem(desktopObj.getDesktopName(cnt));
            }
            // add action listener
            addCbActionListener();
            // select first item
            jComboBoxDesktop.setSelectedIndex(settingsObj.getLastUsedDesktop(desktopObj.getCount()));
        }
    }

    /**
     * This method inits the action listener for the combo box.
     */
    private void addCbActionListener() {
        // add action listener to combo box
        jComboBoxDesktop.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // save current notes
                desktopObj.setDesktopNotes(1, jTextArea1.getText());
                desktopObj.setDesktopNotes(2, jTextArea2.getText());
                desktopObj.setDesktopNotes(3, jTextArea3.getText());
                // after that, set the new selected index as current desktop index
                desktopObj.setCurrentDesktopNr(jComboBoxDesktop.getSelectedIndex());
                // update the treeview, i.e. fill it with outline/structure from
                // the xml-document
                updateTreeView();
                // if view option was comments only, change this option if we don't have any comments at all
                if (Constants.DESKTOP_ONLY_COMMENTS == settingsObj.getDesktopCommentDisplayOptions() && !desktopObj.desktopHasComments(desktopObj.getCurrentDesktopElement())) {
                    settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_WITH_COMMENTS);
                }
                // tell the user that he should click to update...
                jEditorPaneMain.setText(resourceMap.getString("clickToUpdateText"));
                // init notes, if any
                jTextArea1.setText(desktopObj.getDesktopNotes(1));
                jTextArea2.setText(desktopObj.getDesktopNotes(2));
                jTextArea3.setText(desktopObj.getDesktopNotes(3));
                setNeedsUpdate(true);
            }
        });
    }

    /**
     * Toggles the visibility of the notes-sidebar, i.e. either collapses or
     * expands the splitpane on demand.
     */
    @Action
    public void toggleNotesVisibility() {
        isSidebarHidden = !isSidebarHidden;
        if (isSidebarHidden) {
            setSplitLocation(jSplitPaneDesktop2.getDividerLocation());
            jSplitPaneDesktop2.setDividerLocation(1.0d);
        } else {
            int pos = getSplitLocation();
            if (-1 == pos) {
                pos = mainframe.getWidth() - Constants.MIN_SIDEBAR_SIZE;
            }
            jSplitPaneDesktop2.setDividerLocation(pos);
        }
    }
    private int savedDividerLocation = 0;

    private void setSplitLocation(int pos) {
        savedDividerLocation = pos;
    }

    private int getSplitLocation() {
        return savedDividerLocation;
    }

    @Action
    public void printContent() {
        javax.swing.JEditorPane tmpPane = new javax.swing.JEditorPane();
        tmpPane.setEditorKit(jEditorPaneMain.getEditorKit());
        tmpPane.setContentType(jEditorPaneMain.getContentType());
        StringBuilder sb = new StringBuilder("");
        sb.append(completePage);
        // if we have any content, insert html-header at the beginning...
        if (sb.length() > 0) {
            sb.insert(0, HtmlUbbUtil.getHtmlHeaderForDesktop(settingsObj, true));
        }
        tmpPane.setText(sb.toString());
        try {
            tmpPane.print();
        } catch (PrinterException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /**
     * Displays the selected entry in the main frame.
     */
    @Action(enabledProperty = "entryNodeSelected")
    public void displayEntryInMainframe() {
        // retrieve selection from jtree
        int nr = getSelectedEntryNumber();
        // when we have a valid selection, go on
        if (nr != -1) {
            zknframe.showEntry(nr);
        }
    }

    public void showEntryInDesktop(int entrynr) {
        // check for valid value
        if (-1 == entrynr) {
            return;
        }
        // itereate all desktops
        for (int cnt = 0; cnt < desktopObj.getCount(); cnt++) {
            // check whether entry is in this desktop
            if (desktopObj.isEntryInDesktop(entrynr, cnt)) {
                // set entry number
                displayentrynr = entrynr;
                // selecte desktop
                jComboBoxDesktop.setSelectedIndex(cnt);
                // quit method
                return;
            }
        }
    }

    @Action
    public void showDesktopEntryNumber() {
        boolean val = settingsObj.getShowDesktopEntryNumber();
        settingsObj.setShowDesktopEntryNumber(!val);
        val = isNeedsUpdate();
        updateTreeView();
        if (!val) {
            setNeedsUpdate(false);
        }
    }

    /**
     * Each time we add/delete a desktop, the combo box is being updated. First,
     * this method checks whether the current desktop was modified or not, and
     * saves the current desktop-data to xml-file, if necessary.
     * <br><br>
     * Then all action listeners are removed, so no actionevent is fired when we
     * add new items to the combobox. Then all available desktop-data is added.
     * Finally, an actionlistener is added to the jcombobox, that updates the
     * display each time another desktop is chosen.
     *
     * @param closing true when the window will be closed, false if the combobox
     * should be updated and the desktop-window will not be closed.
     */
    private void updateComboBox(boolean closing) {
        // get all action listeners from the combo box
        ActionListener[] al = jComboBoxDesktop.getActionListeners();
        // remove all action listeners so we don't fire several action-events
        // when we update the combo box. we can set the action listener later again
        for (ActionListener listener : al) {
            jComboBoxDesktop.removeActionListener(listener);
        }
        // clear combobox
        jComboBoxDesktop.removeAllItems();
        // add all desktop-items to combo-box
        for (int cnt = 0; cnt < desktopObj.getCount(); cnt++) {
            jComboBoxDesktop.addItem(desktopObj.getDesktopName(cnt));
        }
        // add action listener to combo box
        addCbActionListener();
        // enable multiple-export-menu-item
        exportMultipleMenuItem.setEnabled(desktopObj.getCount() > 1);
        // select last item
        if (!closing) {
            try {
                jComboBoxDesktop.setSelectedIndex(jComboBoxDesktop.getItemCount() - 1);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * A simple initialisation of the jTreeDesktop, i.e. the tree is cleared and
     * single-selection-mode is set.
     */
    private void initTree() {
        // get the treemodel
        DefaultTreeModel dtm = (DefaultTreeModel) jTreeDesktop.getModel();
        // and first of all, clear the jTree
        dtm.setRoot(null);
        // set cell renderer, for desktop icons
        jTreeDesktop.setCellRenderer(new MyCommentRenderer(Constants.iconDesktopComment,
                Constants.iconDesktopLuhmann,
                settingsObj.getUseMacBackgroundColor()));
        if (settingsObj.getUseMacBackgroundColor()) {
            jTreeDesktop.setBackground(ColorUtil.colorJTreeLighterBackground);
            jTreeDesktop.setForeground(ColorUtil.colorJTreeDarkText);
        }
        // set tree-selection-mode
        jTreeDesktop.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // enable drag&drop
        jTreeDesktop.setDragEnabled(true);
        // init transfer handler for tree
        jTreeDesktop.setTransferHandler(new EntryStringTransferHandler() {
            @Override
            protected String exportString(JComponent c) {
                // retrieve tree-component
                javax.swing.JTree t = (javax.swing.JTree) c;
                // retrieve selected node that was dragged
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) t.getSelectionPath().getLastPathComponent();
                // prepare export-string, telling that the drag-source is the jTreeDesktop
                StringBuilder retval = new StringBuilder(Constants.DRAG_SOURCE_JTREEDESKTOP + ",");
                // and give information whether the dragged entry is a bullet or an entry
                retval.append((selectedNode.getAllowsChildren()) ? Constants.DRAG_SOURCE_TYPE_BULLET + "\n" : Constants.DRAG_SOURCE_TYPE_ENTRIES + "\n");
                // next line contains the entry-number, or -1 if a bullet was selected
                retval.append(String.valueOf(getSelectedEntryNumber())).append("\n");
                // retrieve treepath of dragged entry/bullet
                TreePath tp = t.getSelectionPath();
                // add each single path component to return string, new-line-separated
                for (int cnt = 1; cnt < tp.getPathCount(); cnt++) {
                    retval.append(tp.getPathComponent(cnt).toString()).append("\n");
                }
                // finally, add node-ID to string-information
                retval.append(TreeUtil.getNodeTimestamp(selectedNode));
                // remember selected node, which should be removed when dropping the node.
                movedNodeToRemove = selectedNode;
                // return information
                return retval.toString();
            }

            @Override
            protected boolean importString(JComponent c, String str) {
                // get drop-component, i.e. the jTreeDesktop
                javax.swing.JTree t = (javax.swing.JTree) c;
                // retrieve selected node
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) t.getSelectionPath().getLastPathComponent();
                // check for valid drop-string
                if (str != null && selectedNode != null) {
                    // retrieve nodetimestamp
                    String nodets = TreeUtil.getNodeTimestamp(selectedNode);
                    // each received string consists of two lines. the first one with information
                    // about the drag-source and the drag-operation, the second one with the data
                    // by this we can see whether we have received entries (i.e. a valid drop)
                    String[] dropinformation = str.split("\n");
                    // get source information
                    String[] sourceinfo = dropinformation[0].split(",");
                    // in case an edited entry has been dragged, remember 
                    // modifications and comment
                    String modifications = null;
                    String comment = null;
                    // check out the source of the drag-operation. if we have a valid source,
                    // retrieve entries.
                    // here we have
                    // - entries as drop-elements
                    // - and no root selected, i.e. a valid drop-location
                    if (sourceinfo.length > 1 && sourceinfo[1].equals(Constants.DRAG_SOURCE_TYPE_ENTRIES) && !selectedNode.isRoot()) {
                        // check for self-drop
                        if (TreeUtil.getNodeTimestamp(movedNodeToRemove).equals(nodets)) {
                            return false;
                        }
                        // here we have the jTreeDesktop as drag-source, i.e. a drag&drop from within
                        // the jDesktopComponent. that means, we have to delete the drag-source, i.e.
                        // the dragged node that was moved to the new location
                        if (sourceinfo[0].equals(Constants.DRAG_SOURCE_JTREEDESKTOP)) {
                            // retrieve node timestamp
                            String ts = dropinformation[dropinformation.length - 1];
                            // save modifications and comment
                            modifications = desktopObj.retrieveModifiedEntryContentFromTimestamp(ts);
                            comment = desktopObj.getComment(ts, "[br]");
                            // delete entry
                            desktopObj.deleteEntry(ts);
                            // and remove node from original position
                            movedNodeToRemove.removeFromParent();
                        }
                        // retrieve dropped entries. double entries are removed. if an entry comes from within
                        // the desktop, we have to check whether the dragged entry was just moved within a
                        // bullet. if so, we don't have to consider it as "double" entry.
                        String entries = retrieveDropEntries(dropinformation[1], selectedNode);
                        // if we have any entries left, i.e. not only double entries, add them now
                        if (entries != null && entries.length() > 0) {
                            // add new entry
                            addEntries(entries, modifications, comment);
                            // update toolbars
                            // updateTreeView();
                            updateToolbarAndMenu();
                            return true;
                        }
                    } // here we have
                    // - a bullet as drag-source
                    // - and the root or a bullet (or: no entry) as drop-location
                    else if (sourceinfo.length > 1 && sourceinfo[1].equals(Constants.DRAG_SOURCE_TYPE_BULLET) && selectedNode.getAllowsChildren()) {
                        // check for self-drop
                        if (TreeUtil.getNodeTimestamp(movedNodeToRemove).equals(nodets)) {
                            return false;
                        }
                        // cut bullet from XML-data file
                        desktopObj.cutBulletToClip(dropinformation[dropinformation.length - 1]);
                        // and paste the bullet at the target-destination
                        desktopObj.pasteBulletFromClip(TreeUtil.getNodeTimestamp(selectedNode), selectedNode.isRoot());
                        // update treeview and toolbars
                        updateTreeView();
                        updateToolbarAndMenu();
                        return true;
                    } // if we have no drag-information specified, check whether we have a table-drag
                    // and can retrieve any entry-information
                    else if (!selectedNode.isRoot()) {
                        // try to extract entry-numbers from dropped string
                        int[] entrynrs = Tools.retrieveEntryNumbersFromTransferHandler(str, dataObj.getCount(Daten.ZKNCOUNT));
                        // if we have any entries left, i.e. not only double entries, add them now
                        if (entrynrs != null && entrynrs.length > 0) {
                            // create new string builder
                            StringBuilder sb = new StringBuilder("");
                            // iterate all moved/copied/pasted entrynumbers
                            for (int cnt = 0; cnt < entrynrs.length; cnt++) {
                                // check whether entrynumber already exists
                                if (!entryExists(entrynrs[cnt], selectedNode)) {
                                    // if not, add it.
                                    sb.append(String.valueOf(entrynrs[cnt])).append(",");
                                }
                            }
                            // delete last comma
                            if (sb.length() > 1) {
                                sb.setLength((sb.length() - 1));
                                // add new entry
                                addEntries(sb.toString());
                                // update toolbars
                                // updateTreeView();
                                updateToolbarAndMenu();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            protected void cleanup(JComponent c, boolean remove) {
            }
        });
    }

    /**
     * This method retrieves all entry-numbers of entries that have been dropped
     * (or dragged and dropped) on the jTree. The method checks whether any of
     * the dropped entries already exist as entry of the parent-bullet, and if
     * so, the related entry-number will be removed.
     *
     * @param entries the dropped entry-numbers as single string, comma
     * separated.
     * @param targetNode the node where the entries have been dropped
     * @return a string with entry-numbers, comma separated, with double or
     * multiple entries of the {@code targetNode} (or its parent, if targetNode
     * is no bullet) removed.
     */
    private String retrieveDropEntries(String entries, DefaultMutableTreeNode targetNode) {
        // split entries into array
        String[] singleentry = entries.split(",");
        // now we have to check whether the dragged entry already exists as sub-entry of
        // the target's bullet-point. therefore, we have to retrieve the closest parent-bullet
        // to the drop-location
        DefaultMutableTreeNode parent = targetNode;
        // if the targetPath itself is not already a bullet, retrieve its parent.
        if (!targetNode.getAllowsChildren()) {
            parent = (DefaultMutableTreeNode) parent.getParent();
        }
        //check for null
        if (null == parent) {
            return null;
        }
        // go through all entries and check, whether they exists. if so, remove them from the list,
        // so only valid entries are dropped...
        StringBuilder sb = new StringBuilder("");
        for (String singleentry1 : singleentry) {
            if (!entryExists(Integer.parseInt(singleentry1), parent)) {
                sb.append(singleentry1).append(",");
            }
        }
        // delete last comma
        if (sb.length() > 1) {
            sb.setLength((sb.length() - 1));
        }
        // if all elements have been removed, return null
        if (sb.length() < 1) {
            return null;
        }
        // return result
        return sb.toString();
    }

    /**
     * This method checks whether an entry with the number {@code dropnr}
     * already exists as entry of the parent-bullet {@code parent}.
     *
     * @param dropnr the number of the dropped entry
     * @param parent the parent-bullet of the dropped entry
     * @return {@code true} if an entry with the same number as {@code dropnr}
     * already exists as entry of the parent-bullet {@code parent},
     * {@code false} otherwise.
     */
    private boolean entryExists(int dropnr, DefaultMutableTreeNode parent) {
        // check for null
        if (null == parent) {
            return false;
        }
        // now go through all bullet's children
        for (int counter = 0; counter < parent.getChildCount(); counter++) {
            // get child
            DefaultMutableTreeNode doublechild = (DefaultMutableTreeNode) parent.getChildAt(counter);
            // retrieve the node's text and extract the entry-number
            int childnr = TreeUtil.extractEntryNumberFromNode(doublechild);
            // compare child's text with newBullet-text
            if (childnr == dropnr) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeWindow();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     *
     */
    private class MyCommentRenderer extends DefaultTreeCellRenderer {

        Icon commentIcon, luhmannIcon;
        boolean useMacBackgound;

        public MyCommentRenderer(Icon icon, Icon iconLuhmann, boolean bg) {
            commentIcon = icon;
            luhmannIcon = iconLuhmann;
            useMacBackgound = bg;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            setBackgroundNonSelectionColor(tree.getBackground());
            if (!leaf && !sel && useMacBackgound) {
                setForeground(ColorUtil.colorJTreeDarkText);
            }
            setFont(getFont().deriveFont((leaf) ? Font.PLAIN : Font.BOLD));
            if (isCommentNode(value)) {
                setIcon(commentIcon);
            } else if (isLuhmannNode(value)) {
                setIcon(luhmannIcon);
            }
            return this;
        }

        /**
         * This method checks whether a selected node has a comment or not.
         *
         * @param value the selected node
         * @return {@code true} if the selected node has a comment,
         * {@code false} otherwise.
         */
        protected boolean isCommentNode(Object value) {
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
    }

    /**
     * Updates/enables/disables the toolbar- and menu-buttons, depending on
     * whether the functions are available and executable or not.
     */
    private void updateToolbarAndMenu() {
        // get selection
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // enable actions that require a selected bullet, but no root
        setBulletSelected((node != null) && (node.getAllowsChildren()));
        // enable actions that either require a selcted bullet or child-node, but no root
        setNodeSelected((node != null) && (!node.isRoot()));
        // enable actions that either require a selcted bullet or child-node, but no root
        setEntryNodeSelected((node != null) && (!node.isRoot()) && (!node.getAllowsChildren()));
        // enable actions that require any selection
        setAnyNodeSelected(node != null);
        setModifiedEntryNode(desktopObj.isModifiedEntry(getSelectedNodeTimestamp()));
        // enable paste-option
        setClipFilled(((clipBullet && isBulletSelected()) || (!clipBullet && clipEntries != null && clipEntries.length > 0)));
        // enable the move-actions, which let the user move an entry/bullet up and down
        setMoveUpEnabled(!isFirstNode() && node != null && !node.isRoot());
        setMoveDownEnabled(!isLastNode() && node != null && !node.isRoot());
        // create string that will hold the luhmann-numbers
        String luhmann = "";
        // get selected entry-number
        int nr = getSelectedEntryNumber();
        // if we have a valid entry, get luhmann-numbers
        if (nr != -1) {
            luhmann = dataObj.getLuhmannNumbers(nr);
        }
        // enable actions that require a selected child-node (entty), that has luhmann-numbers
        setLuhmannNodeSelected(isNodeSelected() && !luhmann.isEmpty());
    }

    /**
     * This method retrieves the entry of the selected node and extracts its
     * entry-number.
     *
     * @return the number of the selected entry, or -1 if nothing was selected.
     */
    private int getSelectedEntryNumber() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // if we have a valid node, go on...
        if (node != null) {
            // return TreeUtil.extractEntryNumberFromNodeText(node);
            return TreeUtil.extractEntryNumberFromNode(node);
        }
        return -1;
    }

    /**
     * This method updates the display, i.e. receiving the entry-structure from
     * the jTree and displays all those entries in html-formatting in the
     * jEditorPane.
     */
    private synchronized void updateDisplay() {
        // when the display is up to date, do nothing here...
        if (!isNeedsUpdate()) {
            return;
        }
        // cancel already running task if necessary, so we don't have more parallel tasks running
        if (cDisplayTask != null) {
            cDisplayTask.cancel(true);
        }
        // if task is already running, leave...
        if (cDisplayTaskIsRunning) {
            return;
        }
        Task cdT = displayTask();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(cdT);
        tM.setForegroundTask(cdT);
    }

    /**
     * This method fills recursively all entries' contents to the jEditorPane.
     * Therefore, the jTreeDesktop is enumerated. Each element is then converted
     * to a string, html-formatted, and appended to a StringBuilder. At the end,
     * the StringBuilder's content is set to the jEditorPane.
     *
     * @param node the root node, as starting point for the jTree-enumeration
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     */
    private void exportEntriesToEditorPane(DefaultMutableTreeNode node, StringBuilder sb, boolean showComments) {
        // get a list with all children of the node
        Enumeration en = node.children();
        // go through all children
        while (en.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) en.nextElement();
            // if the child is a bullet...
            if (node.getAllowsChildren()) {
                // retrieve bullet-level
                int lvl = node.getLevel();
                String level = String.valueOf(lvl);
                // new table row
                sb.append("<tr>").append(lineseparator);
                // check whether comments should be displayed
                if (showComments) {
                    // retieve and create comment
                    String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                    // insert comment as table-data
                    sb.append("<td valign=\"top\" class=\"comment\">")
                            .append(com)
                            .append("</td>")
                            .append(lineseparator);
                }
                // insert entry content
                sb.append("<td>").append("<h").append(level).append(">");
                // now create the reference-ankh, so we can use the "scrollToReference" method
                // of the jEditorPane easily each time the user clicks on an entry in the jTree
                // to scroll to that entry in the text field.
                sb.append("<a name=\"")
                        .append("entry")
                        .append(TreeUtil.getNodeTimestamp(node))
                        .append("\">&nbsp;</a>");
                // append a new headline with the bullet's name
                sb.append(TreeUtil.getNodeText(node));
                // also add headline to wordcounter
                sbWordCountDisplayTask.append(TreeUtil.getNodeText(node)).append(" ");
                sb.append("</h")
                        .append(level)
                        .append(">")
                        .append("</td>")
                        .append("</tr>")
                        .append(lineseparator);
            } // now we know we have an entry. so get the entry number...
            else {
                // we now want to check whether the user has made modifications to the entry's
                // content, which are only made to the desktop (the content of the entry in the main database
                // is not changed, so you can edit the desktop-entry without changing the entry's original
                // content - this is useful when you want to add some words/phrases between entries etc., which
                // should be applied only to the final text on the desktop, but not to the original entries).
                //
                // in case we have modified an entry on the desktop, this entry has a "content" element. to
                // retrieve the correct entry, we need to look for the unique timestamp of that entry - since
                // an entry could appear multiple times on the desktop, thus the entry number itself is no
                // valid value for retrieving the right entry. Therefore, each treenode has a user-object
                // assigned, which holds the unique timestamp
                String text = desktopObj.retrieveModifiedEntryContentFromTimestamp(TreeUtil.getNodeTimestamp(node));
                // retrieve entry-number
                int nr = TreeUtil.extractEntryNumberFromNode(node);
                // if nothing found, retrieve regular entry
                // that means, if the entry with the unique-timestamp has no or an empty content-element, the
                // entry was not modified - thus we retrieve the "original" entry.
                if (null == text || text.isEmpty()) {
                    // also cleaned text for wordcounter
                    sbWordCountDisplayTask.append(dataObj.getCleanZettelContent(nr)).append(" ");
                    // get zettel-text
                    text = HtmlUbbUtil.getHtmlContentForDesktop(dataObj, bibtexObj, settingsObj, nr, isHeadingVisible, isEntryNumberVisible, false, false);
                } // else if we have a modified entry-content, we still need to convert its
                // ubb-tags to HTML. this is done here...
                else {
                    // also cleaned text for wordcounter
                    sbWordCountDisplayTask.append(Tools.removeUbbFromString(text, true)).append(" ");
                    // get the html-text for an entry which content is passed as parameter...
                    text = HtmlUbbUtil.getHtmlContentForDesktop(dataObj,
                            bibtexObj, settingsObj, text, nr, isHeadingVisible,
                            isEntryNumberVisible, false, false);
                }
                // new table row
                sb.append("<tr>").append(lineseparator);
                // check whether comments should be displayed
                if (showComments) {
                    // retieve and create comment
                    String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                    // insert comment as table-data
                    sb.append("<td valign=\"top\" class=\"comment\">")
                            .append(com)
                            .append("</td>");
                }
                sb.append("<td class=\"content\">").append(lineseparator);
                // now create the reference-ankh, so we can use the "scrollToReference" method
                // of the jEditorPane easily each time the user clicks on an entry in the jTree
                // to scroll to that entry in the text field.
                sb.append("<a name=\"")
                        .append("entry")
                        .append(TreeUtil.getNodeTimestamp(node))
                        .append("\">&nbsp;</a>")
                        .append(lineseparator);
                // and append the html-text of the entry...
                sb.append(text)
                        .append("</td>")
                        .append("</tr>")
                        .append(lineseparator);
            }
            // when the new node also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                exportEntriesToEditorPane(node, sb, showComments);
            }
        }
    }

    /**
     * This method fills recursively all entries' contents to the jEditorPane.
     * Therefore, the jTreeDesktop is enumerated. Each element is then converted
     * to a string, html-formatted, and appended to a StringBuilder. At the end,
     * the StringBuilder's content is set to the jEditorPane.
     *
     * @param node the root node, as starting point for the jTree-enumeration
     * @param sb the stringbuilder which finally will contain all content in
     * html-format
     */
    private void exportCommentsToEditorPane(DefaultMutableTreeNode node, StringBuilder sb) {
        // get a list with all children of the node
        Enumeration en = node.children();
        // go through all children
        while (en.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) en.nextElement();
            // if the child is a bullet...
            if (node.getAllowsChildren()) {
                // retrieve bullet-level
                int lvl = node.getLevel();
                String level = String.valueOf(lvl);
                // retieve and create comment
                String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                // check whether comment is empty or not
                if (com != null && !com.isEmpty()) {
                    // new table row
                    sb.append("<tr>");
                    // insert paragraph heading
                    sb.append("<td><h").append(level).append(">");
                    // now create the reference-ankh, so we can use the "scrollToReference" method
                    // of the jEditorPane easily each time the user clicks on an entry in the jTree
                    // to scroll to that entry in the text field.
                    sb.append("<a name=\"");
                    sb.append("entry").append(TreeUtil.getNodeTimestamp(node));
                    sb.append("\">&nbsp;</a>");
                    // append a new headline with the bullet's name
                    sb.append(TreeUtil.getNodeText(node));
                    sb.append("</h").append(level).append(">");
                    // insert comment as table-data
                    sb.append("<p>").append(com).append("</p>");
                    sb.append("</td></tr>").append(lineseparator);
                    // also add headline and comment to wordcounter
                    sbWordCountDisplayTask.append(TreeUtil.getNodeText(node)).append(" ").append(com).append(" ");
                }
            } // now we know we have an entry. so get the entry number...
            else {
                // retieve and create comment
                String com = desktopObj.getComment(TreeUtil.getNodeTimestamp(node), "<br>");
                // check whether comment is empty or not
                if (com != null && !com.isEmpty()) {
                    // new table row
                    sb.append("<tr>");
                    sb.append("<td class=\"content\">");
                    // now create the reference-ankh, so we can use the "scrollToReference" method
                    // of the jEditorPane easily each time the user clicks on an entry in the jTree
                    // to scroll to that entry in the text field.
                    sb.append("<a name=\"");
                    sb.append("entry").append(TreeUtil.getNodeTimestamp(node));
                    sb.append("\">&nbsp;</a>");
                    // and append the html-text of the entry...
                    sb.append(HtmlUbbUtil.getZettelTitleForDesktop(dataObj, TreeUtil.extractEntryNumberFromNode(node), isEntryNumberVisible));
                    // insert comment as table-data
                    sb.append("<p>").append(com).append("</p>");
                    sb.append("</td></tr>").append(lineseparator);
                    // also cleaned text for wordcounter
                    sbWordCountDisplayTask.append(com).append(" ");
                }
            }
            // when the new node also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                exportCommentsToEditorPane(node, sb);
            }
        }
    }

    /**
     * This method scrolls to that entry in the jEditorPane, that is selected
     * from the jTree. This is achieved by simply activating a hyperlink (ankh),
     * thus we can scroll to the reference.<br><br>
     * A unique reference for each entry is set in the node's text in the jTree
     * via the method
     * {@link #fillChildren(org.jdom.Element, javax.swing.tree.DefaultMutableTreeNode, javax.swing.tree.DefaultTreeModel) fillChildren}
     * and
     * {@link #exportEntriesToEditorPane(javax.swing.tree.DefaultMutableTreeNode, java.lang.StringBuilder) exportEntriesToEditorPane}.
     */
    private void scrollToEntry() {
        // get selected node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        if (null == node || node.isRoot()) {
            return;
        }
        // get the node's text. this is what we want to search in the editor pane
        jEditorPaneMain.scrollToReference("entry" + getSelectedNodeTimestamp());
    }

    @Action
    public void findLive() {
        // first, update the display, so we search in the current desktop
        updateDisplay();
        // display the panel that contains the textfield for the search terms
        // and the find next/prev-buttons
        jPanelLiveSearch.setVisible(true);
        // set input-focus to textfield
        jTextFieldLiveSearch.requestFocusInWindow();

    }

    @Action
    public void findLiveNext() {
        // first, we have to create a string that equals the ankh (reference) where
        // the editorpane should scroll to. each found search term has a link-tag with
        // a consecutive numbering (<a name="hl1">, <a name="hl2"> etc...)
        // the variable "findlivepos" contains the number of the current references ankh
        String ankh = "hl" + String.valueOf(findlivepos);
        // scroll the the reference
        jEditorPaneMain.scrollToReference(ankh);
        // increase findlivepos to refer to the next reference
        if (findlivepos < findlivemax) {
            findlivepos++;
        } // if we reach the last reference, start at first reference again
        else {
            findlivepos = 1;
        }
    }

    @Action
    public void findLivePrev() {
        // decrease findlivepos.
        if (findlivepos > 1) {
            findlivepos--;
        } // when we reached the first reference, set to last occurencs of search terms
        else {
            findlivepos = findlivemax;
        }
        // create reference string
        String ankh = "hl" + String.valueOf(findlivepos);
        // and scroll to reference
        jEditorPaneMain.scrollToReference(ankh);
    }

    @Action
    public void findCancel() {
        // restore complete page without highlighting
        jEditorPaneMain.setText(HtmlUbbUtil.getHtmlHeaderForDesktop(settingsObj, false) + completePage);
        // scroll to top
        jEditorPaneMain.setCaretPosition(0);
        // hide live-search-bar
        jPanelLiveSearch.setVisible(false);
    }

    private void updateLiveSearchDisplay() {
        // retrieve search term from textfield and set it as highlight-term for
        // the html-conversion
        HtmlUbbUtil.setHighlighTerms(new String[]{jTextFieldLiveSearch.getText()}, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH, false);
        // init the counter for the references to the found search terms
        findlivepos = 1;
        // retrieve the desktop-content with highlighted search term
        StringBuilder newtext = new StringBuilder(HtmlUbbUtil.highlightSearchTerms(completePage, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH));
        // counter for bulding the HTML-link-tags, so we can scroll directly to each references
        // found search term, using the editor pane's scrollToReference()-method
        int counter = 1;
        int pos = 0;
        // while we find a highlighted search term, we want to insert an ankh (html-link-tag
        // including the reference-name) before that search term
        while (pos != -1) {
            // find occurence of highlighted text
            pos = newtext.toString().indexOf("<span class=\"hs_ls\"", pos);
            // if we found something...
            if (pos != -1) {
                // insert linktag before.
                newtext.insert(pos, "<a name=\"hl" + String.valueOf(counter++) + "\"></a>");
                // and increase position-index, so we don't have an infinite loop that always finds
                // the same <span>-tag
                pos = pos + 40;
            }
        }
        // store the maximum amount of found/highlighted search terms, so we know when to
        // restart with our reference counter. see findLiveNext() and findLivePrev() for
        // more details
        findlivemax = counter - 1;
        // set new highlighted text to editor pane
        jEditorPaneMain.setText(HtmlUbbUtil.getHtmlHeaderForDesktop(settingsObj, false) + newtext.toString());
        // set scroll-area to top
        jEditorPaneMain.setCaretPosition(0);
        // and scroll to first reference in the editor pane
        String ankh = "hl1";
        jEditorPaneMain.scrollToReference(ankh);
    }

    @Action
    public void switchHeadingsVisibility() {
        isHeadingVisible = !isHeadingVisible;
        headingsVisibleMenuItem.setSelected(isHeadingVisible);
        setNeedsUpdate(true);
    }

    @Action
    public void switchLuhmannVisibility() {
        isLuhmannIconVisible = !isLuhmannIconVisible;
        luhmannIconVisible.setSelected(isLuhmannIconVisible);
        settingsObj.setShowLuhmannIconInDesktop(isLuhmannIconVisible);
        updateTreeView();
    }

    @Action
    public void switchEntryNumberVisibility() {
        isEntryNumberVisible = !isEntryNumberVisible;
        entryNumberVisibleMenuItem.setSelected(isEntryNumberVisible);
        setNeedsUpdate(true);
    }

    @Action
    public void menuDisplayDesktopWithComments() {
        settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_WITH_COMMENTS);
        setNeedsUpdate(true);
    }

    @Action
    public void menuDisplayDesktopWithoutComments() {
        settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_WITHOUT_COMMENTS);
        setNeedsUpdate(true);
    }

    @Action
    public void menuDisplayDesktopOnlyComments() {
        settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_ONLY_COMMENTS);
        setNeedsUpdate(true);
    }

    /**
     * This method is called whenever an entry that is also on the desktop was
     * changed. In this case we assume that the entry's content was changed, so
     * we need an update to the treeview (in case the entry has a new title, we
     * need to display it in the treeview as well) and we need an update to the
     * html-content.
     */
    public void updateEntriesAfterEditing() {
        updateTreeView();
    }

    /**
     * This method updates the treeview after each change made to the desktop.
     * The changes are only applied to the desktop-data-file (the xml-file
     * managed in the {@link #desktopObj CDesktop-Class}). This method retrieves
     * the structure from this xml-document and copies it to the
     * treeview.<br><br>
     * Updating the treeview does <b>not</b> update the display of the entries
     * as well, since many display-updates take much time. Rather the action
     * {@link #updateView() updateView} is enabled, which updates the display.
     */
    private void updateTreeView() {
        // get the treemodel
        DefaultTreeModel dtm = (DefaultTreeModel) jTreeDesktop.getModel();
        // and first of all, clear the jTree
        dtm.setRoot(null);
        // create a new root element from the current desktop name and set it as root
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeUserObject(desktopObj.getDesktopName(jComboBoxDesktop.getSelectedIndex()), Constants.ROOT_ID_NAME, ""));
        dtm.setRoot(root);
        // get current desktop-element
        Element d = desktopObj.getCurrentDesktopElement();
        // check whether the current desktop has any children at all
        if (desktopObj.hasChildren(d)) {
            // create treeview-content
            fillChildren(d, root, dtm);
        }
        // finally, expand the whole tree
//        expandAllTrees(true, jTreeDesktop);
        expandAllTrees();
        // check whether a certain entry should be selected or not
        if (displayentrynr != -1) {
            // check whether entry exists in current desktop
            if (desktopObj.isEntryInCurrentDesktop(displayentrynr)) {
                // if yes, retrieve element
                Element en = desktopObj.getFoundDesktopElement();
                // check for valid value
                if (en != null) {
                    // get timestamp of entry
                    String timestamp = en.getAttributeValue("timestamp");
                    // select entry in treeview
                    selectTreePath(timestamp);
                }
            }
            // reset display entry nr
            displayentrynr = -1;
        }
        // display needs to be updated...
        setNeedsUpdate(true);
    }

    /**
     * This method updates the jTreeView. Each time an update for the treevuew
     * is needed, this method is called. It then recursevly traverses all
     * XML-Elements of the currently activated desktop-element, where the
     * starting desktop-element is passed in the parameter {@code e}.
     * <br><br>
     * The method retrieves each element, checks whether the element is an
     * entry- or a bullet-element, than either, in case of a bullet point, uses
     * the name-attribute as node-name and appends the timestamp-attribute as
     * ID; or it retrieves the entry's title from the entry-number that is
     * stored in each entry-element, and appends the entry's timestamp-attribute
     * as ID.
     * <br><br>
     * After that, the node is inserted in the jTree.
     *
     * @param e
     * @param n
     * @param dtm
     */
    private void fillChildren(Element e, DefaultMutableTreeNode n, DefaultTreeModel dtm) {
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // create a new node
            DefaultMutableTreeNode node;
            // we have to ignore the comment-tags here. comments are no tags that will
            // be displayed in the jtree, but that store comments which will be displayed
            // in the jeditorpane (see "updateDisplay" method for further details)
            if (!e.getName().equals("comment")) {
                // if the child is a bullet...
                if (e.getName().equals("bullet")) {
                    // create new stringbuilder
                    StringBuilder sb = new StringBuilder("");
                    // append name of bullet point
                    sb.append(e.getAttributeValue("name"));
//                    // and append unique id, which is the element's timestamp
//                    sb.append(" [id#").append(e.getAttributeValue("timestamp")).append("]");
//                    // create a node with the element's name-attribute
//                    node = new DefaultMutableTreeNode(sb.toString());
                    // create a node with the element's name-attribute
                    node = new DefaultMutableTreeNode(new TreeUserObject(sb.toString(), e.getAttributeValue("timestamp"), ""));
                    // and tell node to have children
                    node.setAllowsChildren(true);
                } else {
                    // now we know we have an entry. so get the entry number...
                    int nr = Integer.parseInt(e.getAttributeValue("id"));
                    // get the zettel title
                    String title = TreeUtil.retrieveNodeTitle(dataObj, settingsObj.getShowDesktopEntryNumber(), String.valueOf(nr));
                    // create a new node
                    node = new DefaultMutableTreeNode(new TreeUserObject(title, e.getAttributeValue("timestamp"), String.valueOf(nr)));
                    // and tell node not to have children
                    node.setAllowsChildren(false);
                }
                // add new node to treeview
                dtm.insertNodeInto(node, n, n.getChildCount());
                // when the new element also has children, call this method again,
                // so we go through the strucuture recursively...
                if (desktopObj.hasChildren(e)) {
                    fillChildren(e, node, dtm);
                }
            }
        }
    }

    /**
     * This method retrieves all entries on the desktop and adds their number to
     * the list {@code liste}. This array of entry-numbers is needed in the
     * export-dialog.
     *
     * @param e the starting point for the jTree-enumeration, either the root
     * elementor a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param liste an array-object that will hold the found entry-nubers
     */
    private void createExportEntries(Element e, ArrayList<Object> liste) {
        // if we have no element, return.
        if (null == e) {
            return;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // we have to ignore the comment-tags here. comments are no tags that will
            // be displayed in the jtree, but that store comments which will be displayed
            // in the jeditorpane (see "updateDisplay" method for further details)
            if (!e.getName().equals("comment")) {
                // if the child is a bullet...
                if (e.getName().equals("bullet")) {
                    // first, we want to retrieve the header-level
                    int headerlevel = 1;
                    // get bullet's parent
                    Element f = e.getParentElement();
                    // as long as we have not reached the root, get further parent-elements
                    // and increase counter for header-level
                    while (!f.isRootElement()) {
                        f = f.getParentElement();
                        headerlevel++;
                    }
                    // add the element's name-attribute. since headers might consist of only numbers,
                    // we add a char here. this is necessary, since the export-methods distinguish
                    // between headers and entry-numbers simply by parsing integer-values. if the parsing
                    // succeeds, we have an entry, if a NumberFormatException is thrown, we have a headline.
                    // to treat headline with numbers only as headlines, we add a char to be sure that every
                    // headline will throw an exception when parsing the array's elements to integer.
                    liste.add("h" + String.valueOf(headerlevel) + e.getAttributeValue("name"));
                } else {
                    // now we know we have an entry. so get the entry number...
                    int nr = Integer.parseInt(e.getAttributeValue("id"));
                    liste.add(nr);
                }
                // when the new element also has children, call this method again,
                // so we go through the strucuture recursively...
                if (desktopObj.hasChildren(e)) {
                    createExportEntries(e, liste);
                }
            }
        }
    }

//    /**
//     * If expand is true, expands all nodes in the tree.
//     * Otherwise, collapses all nodes in the tree.
//     */
//    public void expandAllTrees(boolean expand, JTree tree) {
//        // get tree-root
//        TreeNode root = (TreeNode)tree.getModel().getRoot();
//        // Traverse tree from root
//        expandAllTrees(new TreePath(root), expand, tree);
//    }
//    private void expandAllTrees(TreePath parent, boolean expand, JTree tree) {
//        // Traverse children
//        TreeNode node = (TreeNode)parent.getLastPathComponent();
//        // if we have children, go on
//        if (node.getChildCount() >= 0) {
//            // itereate all children
//            for (Enumeration e=node.children(); e.hasMoreElements();) {
//                TreeNode n = (TreeNode)e.nextElement();
//                TreePath path = parent.pathByAddingChild(n);
//                expandAllTrees(path, expand, tree);
//            }
//        }
//        // Expansion or collapse must be done bottom-up
//        if (expand) {
//            tree.expandPath(parent);
//        } else {
//            tree.collapsePath(parent);
//        }
//    }
    /**
     * If expand is true, expands all nodes in the tree. Otherwise, collapses
     * all nodes in the tree.
     */
    public void expandAllTrees() {
        // get tree-root
        TreeNode root = (TreeNode) jTreeDesktop.getModel().getRoot();
        // Traverse tree from root
        expandAllTrees(new TreePath(root));
    }

    private void expandAllTrees(TreePath parent) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        // if we have children, go on
        if (node.getChildCount() >= 0) {
            // itereate all children
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                if (n.getAllowsChildren()) {
                    // Expansion or collapse must be done bottom-up
                    if (desktopObj.isBulletTreefoldExpanded(TreeUtil.getNodeTimestamp(n))) {
                        jTreeDesktop.expandPath(path);
                        expandAllTrees(path);
                    } else {
                        jTreeDesktop.collapsePath(path);
                    }
                }
            }
        }
//        if (node.getAllowsChildren()) {
//            // Expansion or collapse must be done bottom-up
//            if (desktopObj.isBulletTreefoldExpanded(CCommonMethods.getNodeTimestamp((DefaultMutableTreeNode)node))) {
//                jTreeDesktop.expandPath(parent);
//            } else {
//                jTreeDesktop.collapsePath(parent);
//            }
//        }
    }

    /**
     * This method adds a new desktop to the desktop-data-file. The user can
     * input a new description (string-value). After that, the string is passed
     * to the CDesktopData-class'es method "addNewDesktop", where a new
     * desktop-element is created
     */
    @Action
    public void newDesktop() {
        // user-input for new desktop-description
        String newDesk = (String) JOptionPane.showInputDialog(null, resourceMap.getString("newDesktopMsg"), resourceMap.getString("newDesktopTitle"), JOptionPane.PLAIN_MESSAGE);
        // if we have any valdi input, go on...
        if ((newDesk != null) && (newDesk.length() > 0)) {
            //  add the description as new element to the desktop-data-class
            if (desktopObj.addNewDesktop(newDesk)) {
                // save current notes
                desktopObj.setDesktopNotes(1, jTextArea1.getText());
                desktopObj.setDesktopNotes(2, jTextArea2.getText());
                desktopObj.setDesktopNotes(3, jTextArea3.getText());
                // get the treemodel
                DefaultTreeModel dtm = (DefaultTreeModel) jTreeDesktop.getModel();
                // set the desktop-description as root-element.
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(newDesk);
                root.setAllowsChildren(true);
                dtm.setRoot(root);
                // and update combo box
                updateComboBox(false);
            } else {
                // desktop-name already existed, so desktop was not added...
                JOptionPane.showMessageDialog(this, resourceMap.getString("errDesktopExistsMsg"), resourceMap.getString("errDesktopExistsTitle"), JOptionPane.PLAIN_MESSAGE);
            }
        } // if new desktop action was cancelled, check whether we have any desktops at all...
        else if (desktopObj.getCount() < 1) {
            // if not, close window
            resetAndClose();
        }
    }

    /**
     * This action renames a bullet point, including the root-bullet, i.e. the
     * name of the desktop. Entry-nodes cannot be renamed.
     */
    @Action(enabledProperty = "bulletSelected")
    public void renameBullet() {
        // copy bullet's name into variable
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // open an input-dialog, setting the selected value as default-value
        String newBulletName = (String) JOptionPane.showInputDialog(this, resourceMap.getString("editBulletMsg"), resourceMap.getString("editBulletTitle"), JOptionPane.PLAIN_MESSAGE, null, null, node.toString());
        // if we have a valid return-value, and this bulletname does not already exist...
        if ((newBulletName != null) && (newBulletName.length() > 0) && (!checkIfBulletExists(newBulletName))) {
            // ...either change desktop's name if the root is selected
            if (node.isRoot()) {
                // check whether renaming succeeded
                if (!desktopObj.setDesktopName(desktopObj.getCurrentDesktopNr(), newBulletName)) {
                    // if not, tell user...
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errRenameDesktopMsg"), resourceMap.getString("errRenameDesktopTitle"), JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                // update the combobox
                updateComboBox(false);
            } // ...or change the name of the selected bullet
            else {
                if (!desktopObj.renameBullet(TreeUtil.getNodeTimestamp(node), newBulletName)) {
                    // if not, tell user...
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errRenameBulletMsg"), resourceMap.getString("errRenameBulletTitle"), JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                // update treeview
                updateTreeView();
            }
        }
    }

    /**
     * This method deletes the selected entry, bullet with child-entries or even
     * the complete desktop if the root is selected.
     */
    @Action(enabledProperty = "anyNodeSelected")
    public void deleteNode() {
        // get selection
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // if we have a valid selection, go on
        if (node != null) {
            // variable that indicates what type of node we have to delete
            int delete_what;
            // check what we have to delete
            if (node.isRoot()) {
                delete_what = Constants.DEL_DESKTOP;
            } else if (node.getAllowsChildren()) {
                delete_what = Constants.DEL_BULLET;
            } else {
                delete_what = Constants.DEL_ENTRY;
            }
            // create empty string
            String msg = "";
            // create message for confirm dialog, based on the type what we want to delete
            switch (delete_what) {
                case Constants.DEL_DESKTOP:
                    msg = resourceMap.getString("askForDeleteDesktopMsg");
                    break;
                case Constants.DEL_BULLET:
                    msg = resourceMap.getString("askForDeleteBulletMsg");
                    break;
            }
            int option = -1;
            // ask for delete only when the root or a bullet has to be deleted
            if (delete_what != Constants.DEL_ENTRY) {
                // ask user whether the node should be deleted
                option = JOptionPane.showConfirmDialog(null, msg, resourceMap.getString("askForDeleteTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            }
            // if yes, go on
            if ((JOptionPane.YES_OPTION == option) || (Constants.DEL_ENTRY == delete_what)) {
                // check which type of node was selected. either root, bullet or entry; and
                // delete the related entry/bullet with children/complete desktop
                switch (delete_what) {
                    case Constants.DEL_DESKTOP:
                        // delete desktop
                        desktopObj.deleteDesktop();
                        // if we have any desktops left, update treeview
                        if (desktopObj.getCount() > 0) {
                            updateComboBox(false);
                        } // else reset all fields and close window
                        else {
                            resetAndClose();
                        }
                        break;

                    case Constants.DEL_BULLET:
                        // delete parent
                        desktopObj.deleteBullet(TreeUtil.getNodeTimestamp(node));
                        // update view
                        updateTreeView();
                        break;

                    case Constants.DEL_ENTRY:
                        // delete entry
                        desktopObj.deleteEntry(TreeUtil.getNodeTimestamp(node));
                        // update view
                        updateTreeView();
                        break;
                }
            }
        }
    }

    private void resetAndClose() {
        // set click to update-text
        jEditorPaneMain.setText(resourceMap.getString("clickToUpdateText"));
        // reset tree
        initTree();
        // reset combobox
        updateComboBox(true);
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. Desktop Window closed.");
        }
        // make window invisible
        setVisible(false);
    }

    /**
     * Opens a dialog where the user can choose which additional items for an
     * entry (like author, remarks etc.) should be displayed in the desktop.
     */
    @Action
    public void displayItems() {
        if (null == desktopDisplayItemsDlg) {
            // create a new dialog with the desktop-dialog, passing some initial values
            desktopDisplayItemsDlg = new CDesktopDisplayItems(this, settingsObj);
            // center window
            desktopDisplayItemsDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(desktopDisplayItemsDlg);
        // when the user made changes and applies them...
        if (desktopDisplayItemsDlg.isNeedsUpdate()) {
            // tell programm that we need an update to the display.
            setNeedsUpdate(true);
        }
        // delete the windows
        desktopDisplayItemsDlg.dispose();
        desktopDisplayItemsDlg = null;
    }

    /**
     * This method adds a new header/bulletpoint to the desktop (and to the
     * jtree). Bullets differ from "usual" note in such case, that they are
     * allowed to have children - in contrary to nodes that refer to entries,
     * which may not have children.
     * <br><br>
     * New bullet are always addes to the end of a selected node's
     * children-list, while a new "entry-node" should be added <b>before</b>
     * bullets. Thus, in a children list, first come all entry-nodes, than all
     * bullet-nodes (always in this order).
     */
    @Action(enabledProperty = "bulletSelected")
    public void addBullet() {
        // wait for user input
        String newBullet = (String) JOptionPane.showInputDialog(null, resourceMap.getString("newBulletMsg"), resourceMap.getString("newBulletTitle"), JOptionPane.PLAIN_MESSAGE);
        // if we have a valid input and no cancel-operation, go on
        if (newBullet != null && newBullet.length() > 0) {
            // now we have to check whether the name of "newBullet" already exists as a bullet on this
            // level of the outline/structure. Therefor, check whether "insert" is a selected entry or a node.
            // if it's an entry, thus the new bullet is located on the same level, retrieve the parent.
            // then we go through all parent's children and check for the occurence of a bullet named
            // "newBullet".
            if (checkIfBulletExists(newBullet)) {
                return;
            }
            // retrieve selected node
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
            // add bullet to xml-file. therefor, check whether selection is the root, then pass
            // "null" as parameter. in every other case, pass the bullet's timestamp as parameter
            String timestamp = desktopObj.addBullet((node.isRoot()) ? null : TreeUtil.getNodeTimestamp(node), newBullet);
            // update treeview
            updateTreeView();
            // add new bullet to path and re-select new path...
            selectTreePath(timestamp);
            // update the menus/toolbar
            updateToolbarAndMenu();
        }
    }

    /**
     * This method checks whether a new bullet {@code newBullet}, that should be
     * added/inserted as sub-bullet to an existing bullet {@code insert},
     * already exists as sub-element of the parent {@code insert} or not.
     *
     * @param newBullet the name of the new bullet that should be inserted as
     * child of {@code insert}
     * @param insert the parent-bullet, where the bullet {@code newBullet}
     * should be added to
     * @return {@code true} if a bullet which name equals {@code newBullet}
     * already exists, false if {@code newBullet} does not exist and can
     * successfully be added/inserted.
     */
    private boolean checkIfBulletExists(String newBullet) {
        // get the selected node, so we know where to insert the new bullet
        DefaultMutableTreeNode insert = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // now we have to check whether the name of "newBullet" already exists as a bullet on this
        // level of the outline/structure. Therefor, check whether "insert" is a selected entry or a node.
        // if it's an entry, thus the new bullet is located on the same level, retrieve the parent.
        // then we go through all parent's children and check for the occurence of a bullet named
        // "newBullet".
        DefaultMutableTreeNode parent = ((insert.getAllowsChildren()) ? insert : (DefaultMutableTreeNode) insert.getParent());
        // got through all parent's children
        for (int cnt = 0; cnt < parent.getChildCount(); cnt++) {
            // get child
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(cnt);
            // since each bullet-point has a timestamp-id, we need to extract the bullet's name
            // only for comparison
            String text = child.toString();
            int pos = text.indexOf("[");
            // only continue if we found something...
            if (pos != -1) {
                // compare child's text with newBullet-text
                if (newBullet.equalsIgnoreCase(text.substring(0, pos).trim())) {
                    // if we found a match, tell user about cancelling
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errBulletExistsMsg"), resourceMap.getString("errBulletExistsTitle"), JOptionPane.PLAIN_MESSAGE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This methods adds a new entry-node, i.e. a node that is not allowed to
     * have children, to the jTree.
     * <br><br>
     * First of all, an input-dialog is opened where the user can enter several
     * entry-numbers, separated by commas, including "from-to"-option (e.g.:
     * "4,7,13-16,22"). Then each entry is retrieved, setting entry-number and
     * entry-title as node-text.
     * <br><br>
     * The nodes are added to the end of the selected node's children-list, but
     * <b>before</b> the first heading/bullet. so, a child-list always lists
     * first the entry-nodes (that are not allowed to have children), followed
     * by header/bullet-nodes (that may have children).
     * <br><br>
     * An entry-node shall never be located <b>behind</b> a bullet/header node!
     * <br><br><i>This method is separated in two parts!</i> The main part can
     * be found in the {@link #addEntries addEntries} method.
     */
    @Action(enabledProperty = "nodeSelected")
    public void addEntry() {
        // wait for user input
        String newEntries = (String) JOptionPane.showInputDialog(null, resourceMap.getString("newEntryMsg"), resourceMap.getString("newEntryTitle"), JOptionPane.PLAIN_MESSAGE);
        // if we have a valid input and no cancel-operation, go on
        if (newEntries != null && newEntries.length() > 0) {
            addEntries(newEntries);
        }
    }

    @Action(enabledProperty = "entryNodeSelected")
    public void editEntry() {
        // get selected entry
        int nr = getSelectedEntryNumber();
        // when we have a valid selection, go on
        if (nr != -1) {
            zknframe.openEditor(true, nr, false, false, -1);
        }
    }

    @Action(enabledProperty = "nodeSelected")
    public void insertEntry() {
        zknframe.editEntryFromDesktop = true;
        zknframe.openEditor(false, -1, false, false, getSelectedEntryNumber());
    }

    /**
     * This methods adds a new entry-node, i.e. a node that is not allowed to
     * have children, to the jTree.
     * <br><br>
     * First of all, an input-dialog is opened where the user can enter several
     * entry-numbers, separated by commas, including "from-to"-option (e.g.:
     * "4,7,13-16,22"). Then each entry is retrieved, setting entry-number and
     * entry-title as node-text.
     * <br><br>
     * The nodes are added to the end of the selected node's children-list, but
     * <b>before</b> the first heading/bullet. so, a child-list always lists
     * first the entry-nodes (that are not allowed to have children), followed
     * by header/bullet-nodes (that may have children).
     * <br><br>
     * An entry-node shall never be located <b>behind</b> a bullet/header node!
     * <br><br><i>This method is separated in two parts!</i> The inital part can
     * be found in the {@link #addEntry addEntry} method.
     *
     * @param newEntries
     */
    private void addEntries(String newEntries) {
        addEntries(newEntries, null, null);
    }

    /**
     * This methods adds a new entry-node, i.e. a node that is not allowed to
     * have children, to the jTree.
     * <br><br>
     * First of all, an input-dialog is opened where the user can enter several
     * entry-numbers, separated by commas, including "from-to"-option (e.g.:
     * "4,7,13-16,22"). Then each entry is retrieved, setting entry-number and
     * entry-title as node-text.
     * <br><br>
     * The nodes are added to the end of the selected node's children-list, but
     * <b>before</b> the first heading/bullet. so, a child-list always lists
     * first the entry-nodes (that are not allowed to have children), followed
     * by header/bullet-nodes (that may have children).
     * <br><br>
     * An entry-node shall never be located <b>behind</b> a bullet/header node!
     * <br><br><i>This method is separated in two parts!</i> The inital part can
     * be found in the {@link #addEntry addEntry} method.
     *
     * @param newEntries
     * @param modifications
     */
    private void addEntries(String newEntries, String modifications, String comment) {
        // at the end we need this to check for changes
        boolean modified = false;
        // counter for inserting the new node, used below
        int childcount = 0;
        // remember the last added entry's timestamp
        String lastadded = null;
        // get the selected node, so we know where to insert the new entry
        DefaultMutableTreeNode insert = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // if we have selected an entry, make sure to get the parent as insert-point
        // else we have selected a parent (bullet/header). the entry should be inserted at
        // first posiion after the bullet.
        if (!insert.getAllowsChildren()) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) insert.getParent();
            // get the index of the selected child (i.e. the selected entry)
            // and add one, so the entry is inserted *after* the current selected entry
            childcount = parent.getIndex(insert) + 1;
            // and set the insert-value to the parent
            insert = parent;
        }
        // retrieve arrays from the text-input
        int[] entries = Tools.retrieveEntryNumbersFromInput(newEntries, dataObj.getCount(Daten.ZKNCOUNT));
        // here we store all found entries that have been added, so we can afterwards iterate
        // this array to find the multiple entries for the above linked-list
        // we do this in two steps, because we want to search all entries within one desktop,
        // that means: we want to go through all desktops (the outer loop), and inside this
        // loop we want to go through all entries (the inner loop). but below, we are iterating
        // all entries, so the desktop-iteration would be the inner-loop. this is not intended,
        // because we want to sort the multiple entries according to an ordered desktop-list,
        // which requires iterating the desktops as outer loop.
        LinkedList<Integer> multipleEntries = new LinkedList<>();
        // init multiple message flag
        showMultipleEntryMsg = true;
        // check for valid user-input
        if (entries != null) {
            for (int cnt : entries) {
                // check whether entry already exists
                if (noDoubleEntries(insert, cnt)) {
                    // go through all desktops and check whether the current entry already exists.
                    // if yes, add added entry to our linked list.
                    for (int me = 0; me < desktopObj.getCount(); me++) {
                        if (desktopObj.checkForDoubleEntry(me, cnt)) {
                            multipleEntries.add(cnt);
                        }
                    }
                    // add entry to desktop (xml-file)
                    lastadded = desktopObj.addEntry(TreeUtil.getNodeTimestamp(insert),
                            String.valueOf(cnt),
                            childcount);
                    // check whether we also have modifications to add
                    if (modifications != null && !modifications.isEmpty()) {
                        desktopObj.addModifiedEntry(lastadded, modifications);
                    }
                    if (comment != null && !comment.isEmpty()) {
                        desktopObj.setComment(lastadded, comment);
                    }
                    // remember that we have changes
                    modified = true;
                    // increase counter to indicate next entry
                    childcount++;
                }
            }
        }
        // update treeview if we have changes
        if (modified) {
            // update treeview
            updateTreeView();
            // and re-select initial entry/bullet
            selectTreePath(lastadded);
            // show message box in case we have multiple occurences of entries...
            showMultipleOccurencesMessage(Tools.retrieveDoubleEntries(desktopObj, multipleEntries));
        }
    }

    private void showMultipleOccurencesMessage(List<Object[]> list) {
        // if we have any multiple occurences, the stringbuilder must be longer than 0
        multipleOccurencesMessage = Tools.prepareDoubleEntriesMessage(list);
        // check whether we have any multiple occurences...
        if (multipleOccurencesMessage != null) {
            // check whether this dialog should be displayed at all...
            if (!settingsObj.getHideMultipleDesktopOccurencesDlg()) {
                multipleOccurencesMessage = resourceMap.getString("showMultipleEntriesOnDesktop")
                        + lineseparator + lineseparator
                        + multipleOccurencesMessage;
                if (null == multipleOccurencesDlg) {
                    // create a new dialog with the desktop-dialog, passing some initial values
                    multipleOccurencesDlg = new CShowMultipleDesktopOccurences(this,
                            settingsObj, false, multipleOccurencesMessage);
                    // center window
                    multipleOccurencesDlg.setLocationRelativeTo(null);
                } else {
                    multipleOccurencesDlg.setInfoMsg(multipleOccurencesMessage);
                }
                // show window
                ZettelkastenApp.getApplication().show(multipleOccurencesDlg);
            } // if not, just display icon
            else {
                jButtonShowMultipleOccurencesDlg.setVisible(true);
            }
        }
    }

    @Action
    public void openMultipleOccurencesMessageDlg() {
        if (multipleOccurencesMessage != null) {
            multipleOccurencesMessage = resourceMap.getString("showMultipleEntriesOnDesktop")
                    + lineseparator + lineseparator
                    + multipleOccurencesMessage;
            if (null == multipleOccurencesDlg) {
                // create a new dialog with the desktop-dialog, passing some initial values
                multipleOccurencesDlg = new CShowMultipleDesktopOccurences(this, settingsObj, false, multipleOccurencesMessage);
                // center window
                multipleOccurencesDlg.setLocationRelativeTo(null);
            } else {
                multipleOccurencesDlg.setInfoMsg(multipleOccurencesMessage);
            }
            // show window
            ZettelkastenApp.getApplication().show(multipleOccurencesDlg);
            // hide button
            jButtonShowMultipleOccurencesDlg.setVisible(false);
        }
    }

    /**
     * This method adds entries, indicated by their index-numbers, to the
     * desktop. this method is usually used to add entries to the desktop from
     * other windows, like main-window or searchresults-window etc.
     *
     * @param entries the entry-numbers in an integer-array
     */
    public void addEntries(int[] entries) {
        // when we have no entries, leave
        if ((null == entries) || (entries.length < 1)) {
            return;
        }
        // if we don't have any desktop created yet, tell the user to do so
        // before adding new entries...
        while (desktopObj.getCount() < 1) {
            // ask user whether a new desktop should be created...
            int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForNewDesktopMsg"), resourceMap.getString("askForNewDesktopTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if no, leave method...
            if (JOptionPane.NO_OPTION == option) {
                return;
            }
            // else create new desktop
            newDesktop();
            // when the user added a new desktop, select it
            if (desktopObj.getCount() >= 1) {
                TreePath tp = new TreePath((TreeNode) jTreeDesktop.getModel().getRoot());
                jTreeDesktop.setSelectionPath(tp);
            }
        }
        // if we don't have any bullets created yet, tell the user to do so
        // before adding new entries...
        while (!desktopObj.desktopHasBullets(desktopObj.getCurrentDesktopNr())) {
            // ask user whether a new bullet should be created...
            int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForNewBulletMsg"), resourceMap.getString("askForNewBulletTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            // if no, leave method
            if (JOptionPane.NO_OPTION == option) {
                return;
            }
            // else add bullet
            addBullet();
        }
        // if a valid node is selected, add entries
        if (isNodeSelected()) {
            // create a new stringbuilder
            StringBuilder sb = new StringBuilder("");
            // iterate the array with all entries
            for (int e : entries) {
                // append entry-number
                sb.append(String.valueOf(e));
                // and a comma
                sb.append(",");
            }
            // delete last comma
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
            }
            // add entries
            addEntries(sb.toString());
        } else {
            // copy entries to "clipboard", so the user can paste them after selecting a bullet or node.
            clipEntries = entries;
            // tell user that a selection has to be made...
            JOptionPane.showMessageDialog(this, resourceMap.getString("errSelectionNeededMsg"), resourceMap.getString("errSelectionNeededTitle"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * This action is enabled, when an entry has follower-entries
     * (Luhmann-Numbers). If a user performs this action, all entry's
     * follower-entries (Luhmann-Numbers) are added behind this entry.
     */
    @Action(enabledProperty = "luhmannNodeSelected")
    public void addLuhmann() {
        addEntries(dataObj.getLuhmannNumbers(getSelectedEntryNumber()));
    }

    /**
     * This action is enabled, when an entry has follower-entries
     * (Luhmann-Numbers). If a user performs this action, all entry's
     * follower-entries (Luhmann-Numbers) are added behind this entry.
     */
    @Action(enabledProperty = "luhmannNodeSelected")
    public void addLuhmannComplete() {
        // init string builder
        luhmannnumbers = new StringBuilder("");
        // get recursive Luhmann-Numbers
        fillLuhmannNumbers(getSelectedEntryNumber());
        // add them...
        if (luhmannnumbers.length() > 0) {
            // delete last comma
            luhmannnumbers.setLength(luhmannnumbers.length() - 1);
            // add entries to treeview
            addEntries(luhmannnumbers.toString());
        }
    }

    /**
     * This method recursively retrieves all follower- and sub-follower-numbers
     * (Luhmann-Numbers) of an entry and adds them to a stringbuilder. This
     * method is needed for the {@link #addLuhmann addLuhmann}-Action that adds
     * these follower-numbers to the treeview, directly behind the selected
     * entry.
     *
     * @param zettelpos the number of the selected entry
     */
    private void fillLuhmannNumbers(int zettelpos) {
        // get the text from the luhmann-numbers
        String lnr = dataObj.getLuhmannNumbers(zettelpos);
        // if we have any luhmann-numbers, go on...
        if (!lnr.isEmpty()) {
            String[] lnrs = lnr.split(",");
            // go throughh array of current luhmann-numbers
            for (String exist : lnrs) {
                // copy all values to an array
                luhmannnumbers.append(exist);
                luhmannnumbers.append(",");
                // check whether luhmann-value exists, by re-calling this method
                // again and go through a recusrive loop
                fillLuhmannNumbers(Integer.parseInt(exist));
            }
        }
    }

//    /**
//     * This method recursively retrieves all follower- and sub-follower-numbers
//     * (Luhmann-Numbers) of an entry and adds them to a stringbuilder. This method
//     * is needed for the {@link #addLuhmann addLuhmann}-Action that adds these
//     * follower-numbers to the treeview, directly behind the selected entry.
//     * 
//     * @param zettelpos the number of the selected entry
//     */
//    private String fillLuhmannNumbersWithBullets(int zettelpos, String timestamp) {
//        String lastadded = timestamp;
//        // get the text from the luhmann-numbers
//        String lnr = dataObj.getLuhmannNumbers(zettelpos);
//        // if we have any luhmann-numbers, go on...
//        if (!lnr.isEmpty()) {
//            String[] lnrs = lnr.split(",");
//            // go throughh array of current luhmann-numbers
//            for (String exist : lnrs) {
//                // add entry to desktop (xml-file)
//                lastadded = desktopObj.addEntry(timestamp, exist, 0);
//                
//                if (dataObj.hasLuhmannNumbers(Integer.parseInt(exist))) {
//                    lastadded = desktopObj.addBullet(lastadded, "Ebene " + exist);
//                    // check whether luhmann-value exists, by re-calling this method
//                    // again and go through a recusrive loop
//                    fillLuhmannNumbersWithBullets(Integer.parseInt(exist), lastadded);
//                }
//            }
//        }
//        return (lastadded);
//    }
    /**
     * This method is called from "addEntry" and checks whether an entry-number
     * already exists as a child of that parent.
     *
     * @param parent the insert-parent, where the entry should be inserted as
     * child
     * @param nr the number of the entry that should be inserted
     * @return {@code true} if the entry "nr" already exists as child of
     * "parent".
     */
    private boolean noDoubleEntries(DefaultMutableTreeNode parent, int nr) {
        // got through all parent's children
        for (int counter = 0; counter < parent.getChildCount(); counter++) {
            // get child
            DefaultMutableTreeNode doublechild = (DefaultMutableTreeNode) parent.getChildAt(counter);
            // retrieve the node's text and extract the entry-number
            int childnr = TreeUtil.extractEntryNumberFromNode(doublechild);
            // compare child's text with newBullet-text
            if (childnr == nr) {
                // if we found a match, tell user about cancelling
                if (showMultipleEntryMsg) {
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errEntryExistsMsg"), resourceMap.getString("errEntryExistsTitle"), JOptionPane.PLAIN_MESSAGE);
                    showMultipleEntryMsg = false;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Exports the desktop-data to a file, i.e. the content will be written to a
     * file "as it is" (wysiwyg). unlink the typical export method, this export
     * method also export bullet-point-headings, invisible entry-titles,
     * modified entries etc. some of these features are not supported by the
     * usual export-method.
     */
    @Action
    public void exportDesktop() {
        export(desktopObj.getCurrentDesktopElement(), (DefaultMutableTreeNode) jTreeDesktop.getModel().getRoot());
    }

    @Action
    public void importArchivedDesktop() {
        // create document
        Document archive = new Document();
        // retrieve last used importdirectory
        File importdir = settingsObj.getFilePath();
        // let user choose filepath
        File filepath = FileOperationsUtil.chooseFile(this,
                JFileChooser.OPEN_DIALOG,
                JFileChooser.FILES_ONLY,
                (null == importdir) ? null : importdir.getPath(),
                (null == importdir) ? null : importdir.getName(),
                resourceMap.getString("openArchiveTitle"),
                new String[]{".zip"},
                "Zip",
                settingsObj);
        // if we have a valid file, go on
        if (filepath != null && filepath.exists()) {
            ZipInputStream zip = null;
            // open the zip-file
            try {
                zip = new ZipInputStream(new FileInputStream(filepath));
                ZipEntry entry;
                // now iterate the zip-file, searching for the requested file in it
                while ((entry = zip.getNextEntry()) != null) {
                    // get filename of zip-entry
                    String entryname = entry.getName();
                    // if the found file matches the requested one, start the SAXBuilder
                    if (entryname.equals(Constants.archivedDesktopFileName)) {
                        try {
                            SAXBuilder builder = new SAXBuilder();
                            archive = builder.build(zip);
                            break;
                        } catch (JDOMException e) {
                            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                        }
                    }
                }
                // tell about success
                Constants.zknlogger.log(Level.INFO, "Desktop archive successfully opened.");
            } catch (IOException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                // show error message dialog
                JOptionPane.showMessageDialog(this, resourceMap.getString("openArchiveDlgErr"), resourceMap.getString("openArchiveDlgTitle"), JOptionPane.PLAIN_MESSAGE);
                // and show error log
                zknframe.showErrorIcon();
                return;
            } finally {
                try {
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                }
            }
            // init variables that indicate the success of the import-progress
            boolean finished = false;
            // we have a loop here, because the desktop-name of the imported archiv might already exist.
            // in this case, the user can retry with new names, until a new name was entered, or the
            // user cancels the action
            while (!finished) {
                // import archive
                int result = desktopObj.importArchivedDesktop(archive);
                // here we go on in case the desktop-name of the imported archive
                // already exists. in this case, the user shoould rename the archive
                switch (result) {
                    case DesktopData.IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS:
                        // desktop-name already existed, so desktop was not added...
                        JOptionPane.showMessageDialog(this, resourceMap.getString("errDesktopNameExistsMsg", archive.getRootElement().getAttributeValue("name")), resourceMap.getString("errDesktopNameExistsTitle"), JOptionPane.PLAIN_MESSAGE);
                        // user-input for new desktop-description
                        String newDeskName = (String) JOptionPane.showInputDialog(this, resourceMap.getString("newDesktopMsg"), resourceMap.getString("newDesktopTitle"), JOptionPane.PLAIN_MESSAGE);
                        // check for valid-return value, or if the user cancelled the action
                        if (newDeskName != null && !newDeskName.isEmpty()) {
                            // if everything was ok, set new name
                            archive.getRootElement().setAttribute("name", newDeskName);
                        } else {
                            // else user has cancelled process
                            JOptionPane.showMessageDialog(this, resourceMap.getString("openArchiveCancelled"), resourceMap.getString("openArchiveDlgTitle"), JOptionPane.PLAIN_MESSAGE);
                            return;
                        }   break;
                    case DesktopData.IMPORT_ARCHIVE_ERR_OTHER:
                        // tell user about problem
                        JOptionPane.showMessageDialog(this, resourceMap.getString("openArchiveError"), resourceMap.getString("openArchiveDlgTitle"), JOptionPane.PLAIN_MESSAGE);
                        // and show error log
                        zknframe.showErrorIcon();
                        return;
                    case DesktopData.IMPORT_ARCHIVE_OK:
                        // everything is ok, so quit while-loop
                        finished = true;
                        break;
                    default:
                        break;
                }
            }
            // show success
            JOptionPane.showMessageDialog(this, resourceMap.getString("openArchiveOK"), resourceMap.getString("openArchiveTitle"), JOptionPane.PLAIN_MESSAGE);
            // and update combo box
            updateComboBox(false);
        }
    }

    /**
     * This method archives the currently active desktop to a zipped xml-file.
     * This method saves the current desktop-data, the related notes for this
     * desktop and possible modified entries.<br><br>
     * Archive files can be imported as well (see
     * {@link #importArchivedDesktop() importArchivedDesktop()}).
     */
    @Action
    public void archiveDesktop() {
        // retrieve data-filepath
        File datafp = settingsObj.getFilePath();
        // convert to string, and cut off filename by creating a substring that cuts off
        // everything after the last separator char
        String datafilepath = (datafp != null) ? datafp.getPath().substring(0, datafp.toString().lastIndexOf(java.io.File.separator) + 1) : "";
        // ... to cut off extension
        if (!datafilepath.isEmpty()) {
            datafilepath = datafilepath + desktopObj.getCurrentDesktopName() + resourceMap.getString("archiveDesktopSuffix");
        }
        // create new path
        File exportdir = new File(datafilepath);
        // here we open a swing filechooser, in case the os ist no mac aqua
        File filepath = FileOperationsUtil.chooseFile(this,
                JFileChooser.SAVE_DIALOG,
                JFileChooser.FILES_ONLY,
                exportdir.getPath(),
                exportdir.getName(),
                resourceMap.getString("fileDialogTitleSave"),
                new String[]{".zip"},
                "Zip",
                settingsObj);
        if (filepath != null) {
            // add fileextenstion, if necessay
            if (!filepath.getName().endsWith(".zip")) {
                filepath = new File(filepath.toString() + ".zip");
            }
            // check whether file already exists
            if (filepath.exists()) {
                // file exists, ask user to overwrite it...
                int optionDocExists = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForOverwriteFileMsg", filepath.getName()), resourceMap.getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                // if the user does *not* choose to overwrite, quit...
                if (optionDocExists != JOptionPane.YES_OPTION) {
                    // don't show "export was OK" message in main frame
                    return;
                }
            }
            // create desktop-archive-XML-document
            Document archiveddesktop = desktopObj.archiveDesktop(desktopObj.getCurrentDesktopName());
            // check whether an error occured
            if (null == archiveddesktop) {
                // tell user about error
                JOptionPane.showMessageDialog(this, resourceMap.getString("archiveErrMsg"), resourceMap.getString("archiveErrTitle"), JOptionPane.PLAIN_MESSAGE);
                zknframe.showErrorIcon();
                return;
            }
            // export and zip file
            ZipOutputStream zip = null;
            // open the outputstream
            try {
                zip = new ZipOutputStream(new FileOutputStream(filepath));
                // I first wanted to use a pretty output format, so advanced users who
                // extract the data file can better watch the xml-files. but somehow, this
                // lead to an error within the method "retrieveElement" in the class "CDaten.java",
                // saying the a org.jdom.text cannot be converted to org.jdom.element?!?
                // XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                XMLOutputter out = new XMLOutputter();
                // save archived desktop
                zip.putNextEntry(new ZipEntry(Constants.archivedDesktopFileName));
                out.output(archiveddesktop, zip);
            } catch (IOException | SecurityException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                JOptionPane.showMessageDialog(this, resourceMap.getString("archiveErrMsg"), resourceMap.getString("archiveErrTitle"), JOptionPane.PLAIN_MESSAGE);
                zknframe.showErrorIcon();
                return;
            } finally {
                try {
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                }
            }
            JOptionPane.showMessageDialog(this, resourceMap.getString("archiveOkMsg"), resourceMap.getString("archiveOkTitle"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Exports the desktop-data to a file, i.e. the content will be written to a
     * file "as it is" (wysiwyg). unlink the typical export method, this export
     * method also export bullet-point-headings, invisible entry-titles,
     * modified entries etc. some of these features are not supported by the
     * usual export-method.
     *
     * @param startelement the starting point for the jTree-enumeration, either
     * the root element or a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     * @param startnode the starting point for the jTree-enumeration, either the
     * root node or a bullet (if only a bullet should be exported, see
     * {@link #exportDesktopBullet() exportDesktopBullet()}).
     */
    private void export(Element startelement, DefaultMutableTreeNode startnode) {
        // opens the Export Dialog. This Class is responsible
        // for getting the relevant export data. the export task
        // itself (background task) will be started as another dialog,
        // when this one is closed
        // now open the export-dialog
        if (null == exportWindow) {
            // get parent und init window
            exportWindow = new CDesktopExport(this, settingsObj, bibtexObj);
            // center window
            exportWindow.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(exportWindow);
        // Here the data (filepath, filetype) from the export window
        // will be passed to another dialog window, which starts the
        // background task. these dialogs are separated because it looks
        // better to have a background task with progress bar in an own,
        // smaller dialog
        //
        // first check for valid return value. export is only started,
        // when the previous dialog wasn't cancelled or simply closed
        if (Constants.RETURN_VALUE_CONFIRM == exportWindow.getReturnValue()) {
            // when the user wants to export into PDF or LaTex, open a new dialog where the user
            // can make some extra settings like page settings and font-sizes.
            if (ExportTools.isExportSettingsOk(this, settingsObj, exportWindow.getExportType())) {
                // if dialog window isn't already created, do this now
                if (null == taskDlg) {
                    // create list that will contain all entries which should be exported
                    ArrayList<Object> liste = new ArrayList<>();
                    // here we recursively create the list of headlines and entry-numbers
                    // from the current desktop
                    createExportEntries(startelement, liste);
                    // open export dialog
                    // get parent und init window
                    taskDlg = new TaskProgressDialog(this, TaskProgressDialog.TASK_EXPORTDATA, taskdata, dataObj, bookmarksObj, desktopObj, settingsObj, bibtexObj, null,
                            exportWindow.getFilePath(), liste, exportWindow.getExportType(), 0, ';', startnode, false, false,
                            exportWindow.getExportBibTex(), false, exportWindow.getExportToc(), isHeadingVisible, isEntryNumberVisible);
                    // center window
                    taskDlg.setLocationRelativeTo(this);
                }
                ZettelkastenApp.getApplication().show(taskDlg);
                // dispose the window and clear the object
                taskDlg.dispose();
                taskDlg = null;
                // else tell user that everything went fine
                if (taskdata.showExportOkMessage()) {
                    JOptionPane.showMessageDialog(this, resourceMap.getString("exportOkMsg"), resourceMap.getString("exportOkTitle"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
        // we have to manually dispose the window and release the memory
        // because next time this method is called, the showKwlDlg is still not null,
        // i.e. the constructor is not called (because the if-statement above is not true)
        exportWindow.dispose();
        exportWindow = null;
        // try to motivate garbage collector
        System.gc();
    }

    /**
     * This method exports not a complete desktop, but the selected bullets with
     * all its sub-entries (child-nodes) to a file.
     */
    @Action(enabledProperty = "bulletSelected")
    public void exportDesktopBullet() {
        // only export subentries, if a bullet-point is selected.
        if (!isBulletSelected()) {
            return;
        }
        // find starting-element (selected bullet) from timestamp
        String timestamp = getSelectedNodeTimestamp();
        // and export data
        export(desktopObj.findEntryElementFromTimestamp(desktopObj.getCurrentDesktopElement(), timestamp),
                (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent());
    }

    /**
     * This method exports all desktops to a file. Unlike the typical
     * export-function for desktop-data (see
     * {@link #exportDesktop() exportDesktop()}, this method does not export the
     * desktop-data "as it is" (including modified entries, see
     * {@link #modifiyEntry() modifiyEntry()}), but only the simple
     * entry-contents.<br><br>
     * While the typical desktop-export-functions call the
     * {@link #export(org.jdom.Element, javax.swing.tree.DefaultMutableTreeNode) export()}
     * method, this method calls the export-method from the mainframe.
     */
    @Action
    public void exportMultipleDesktop() {
        // check whether multiple desktops available
        if (desktopObj.getCount() < 2) {
            return;
        }

        if (null == desktopMultipleExportDlg) {
            // create a new dialog with the desktop-dialog, passing some initial values
            desktopMultipleExportDlg = new CDesktopMultipleExport(this, desktopObj, settingsObj);
            // center window
            desktopMultipleExportDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(desktopMultipleExportDlg);
        // check for return-value
        if (desktopMultipleExportDlg.getChosenDesktops() != null && desktopMultipleExportDlg.getChosenDesktops().length > 0) {
            // create list that will contain all entries which should be exported
            ArrayList<Object> liste = new ArrayList<>();
            // iterate all chosen desktop-names
            for (String desktopname : desktopMultipleExportDlg.getChosenDesktops()) {
                // here we recursively create the list of headlines and entry-numbers
                // from the current desktop
                createExportEntries(desktopObj.getDesktopElement(desktopname), liste);
            }
            // call export-method and export the entries.
            zknframe.exportEntries(liste);
        }
        // delete the windows
        desktopMultipleExportDlg.dispose();
        desktopMultipleExportDlg = null;
    }

    /**
     * This method "exports" all entries of the current desktop as search
     * results, i.e. all entry-numbers are shown in the search results
     * window.<br><br>
     * You can also export just the entries of a selected bullet and all its
     * sub-entries (child-nodes) as search result, see
     * {@link #exportBulletToSearch() exportBulletToSearch()}.
     */
    @Action
    public void exportToSearch() {
        zknframe.exportDesktopToSearch(desktopObj.retrieveDesktopEntries(desktopObj.getCurrentDesktopNr()), desktopObj.getCurrentDesktopName());
    }

    /**
     * This method "exports" all entries of the currently selected bullet and
     * all its sub-entries (child-nodes) as search results, i.e. all related
     * entry-numbers are shown in the search results window.<br><br>
     * You can also export the entries of the complete desktop as search result,
     * see {@link #exportToSearch() exportToSearch()}.
     */
    @Action(enabledProperty = "bulletSelected")
    public void exportBulletToSearch() {
        if (isBulletSelected()) {
            // retrieve selected node and its timestamp
            String timestamp = getSelectedNodeTimestamp();
            // here we recursively create the list of headlines and entry-numbers
            // from the current desktop
            zknframe.exportDesktopToSearch(desktopObj.retrieveBulletEntries(desktopObj.findEntryElementFromTimestamp(desktopObj.getCurrentDesktopElement(), timestamp)), desktopObj.getCurrentDesktopName());
        }
    }

    /**
     * This method shows all entries that are <b>not</b> used in the current
     * desktop as search results, i.e. all entry-numbers that do not appear in
     * the current desktop are shown in the search results window.
     */
    @Action
    public void exportMissingToSearch() {
        // retrieve all entry-numbers from current desktop
        int[] currentdesktopentries = desktopObj.retrieveDesktopEntries(desktopObj.getCurrentDesktopNr());
        // check for valid return value
        if (currentdesktopentries != null && currentdesktopentries.length > 0) {
            // create new integer-array
            ArrayList<Integer> expvalues = new ArrayList<>();
            // go through all entry-numbers
            for (int cnt = 1; cnt <= dataObj.getCount(Daten.ZKNCOUNT); cnt++) {
                // if entry-number is *not* on the desktop...
                if (!isInArray(cnt, currentdesktopentries)) {
                    // ... add it to our result-array
                    expvalues.add(cnt);
                }
            }
            // if we have any results, go on...
            if (expvalues.size() > 0) {
                // create return value
                int[] retval = new int[expvalues.size()];
                // and copy all integer-values from the list to that array
                for (int cnt = 0; cnt < retval.length; cnt++) {
                    retval[cnt] = expvalues.get(cnt);
                }
                zknframe.exportDesktopToSearch(retval, desktopObj.getCurrentDesktopName());
            }
        }
    }

    /**
     * This method checks for the occurence of the value {@code nr} inside the
     * array {@code arr}.
     *
     * @param nr the integer-value we are looking for
     * @param arr the integer-array, which mightt contain{@code nr}
     * @return {@code true} if {@code nr} was found inside {@code arr},
     * {@code false} otherwise.
     */
    private boolean isInArray(int nr, int[] arr) {
        // check for valid array
        if (arr != null && arr.length > 0) {
            // go through whole array
            for (int cnt = 0; cnt < arr.length; cnt++) {
                // if we found the value "nr" inside the array "arr", return true
                if (arr[cnt] == nr) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This methods checks whether a selected entry is the last entry in the
     * list of a bullet's child-nodes.
     *
     * @return {@code true} if the selected entry is the last entry in a list of
     * child-nodes, false otherwise
     */
    private boolean isLastNode() {
        // if no node selected, return...
        if (!isNodeSelected()) {
            return false;
        }
        // get the selected node, so we know where to insert the new bullet
        DefaultMutableTreeNode selection = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // enumerate all children of the node's parent's children...
        Enumeration<DefaultMutableTreeNode> children = (Enumeration<DefaultMutableTreeNode>) node.getParent().children();
        while (children.hasMoreElements()) {
            try {
                // get each child
                node = children.nextElement();
                // if the child of the enumeration equals the selection...
                if (node.equals(selection)) {
                    // get next node after selected entry
                    node = children.nextElement();
                    // if the selected entry has another entry behind, return false
                    // otherwise true
                    return (!isBulletSelected()) ? (node.getAllowsChildren()) : false;
                }
            } catch (NoSuchElementException e) {
                // if there is no entry after the selected one, return true
                return true;
            }
        }
        return false;
    }

    /**
     * This methods checks whether a selected entry is the first entry in the
     * list of a bullet's child-nodes.
     *
     * @return {@code true} if the selected entry is the first entry in a list
     * of child-nodes, false otherwise
     */
    private boolean isFirstNode() {
        // if no node selected, return...
        if (!isNodeSelected()) {
            return false;
        }
        // get the selected node, so we know where to insert the new bullet
        DefaultMutableTreeNode selection = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        // if the selection is a node-entry...
        // ...return true when selected entry equals the first child-node of the selected entry's parent
        if (!isBulletSelected()) {
            return (0 == selection.getParent().getIndex(selection));
            // return (selection.equals(selection.getParent().getChildAt(0)));
        } // else if the selection is a bullet-point...
        else {
            // get the selected node's index
            int index = selection.getParent().getIndex(selection);
            // if index is 0, it is the first node.
            if (0 == index) {
                return true;
            }
            // else check whether the node before the selection is a bullet and return false, if it is...
            return !selection.getParent().getChildAt(index - 1).getAllowsChildren();
        }
    }

    /**
     * Closes the window, i.e. makes it invisible
     */
    @Action
    public void closeWindow() {
        // save current notes
        desktopObj.setDesktopNotes(1, jTextArea1.getText());
        desktopObj.setDesktopNotes(2, jTextArea2.getText());
        desktopObj.setDesktopNotes(3, jTextArea3.getText());
        // save last used desktop nr
        settingsObj.setLastUsedDesktop(jComboBoxDesktop.getSelectedIndex());
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. Desktop Window closed.");
        }
        // dispose window
        dispose();
        setVisible(false);
        // try to motivate garbage collector
        System.gc();
    }

    /**
     * This method modifies an entry, but these changes are only applied to the
     * desktop-view. the original entry's content in the main-database is not
     * changed.
     * <br><br>
     * This is helpful when the user wants to "connect" entries by adding some
     * words or paragraphs, which is only necessary for a readable text on the
     * desktop, while the original entries should be left unmodified.
     */
    @Action(enabledProperty = "entryNodeSelected")
    public void modifiyEntry() {
        // check whether a correct entry node was selected or not...
        if (!isEntryNodeSelected()) {
            return;
        }
        // check whether an entry is already being edited, i.e. the edit-window is already created
        if (isEditModeActive) {
            // if so, bring that window to the front
            modifyEntryDlg.toFront();
        } // else create a new window and display it.
        else {
            // get the selected node, to tell the getComment method wether we have a bullet or an entry selected
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
            // retrieve and store edit-timestamp
            editModeTimeStamp = TreeUtil.getNodeTimestamp(node);
            // retrieve content from modified entry, if any...
            String oldcontent = desktopObj.retrieveModifiedEntryContentFromTimestamp(editModeTimeStamp);
            // check for any modified content. if we have no content, retrieve entry's original content instead
            if (null == oldcontent || oldcontent.isEmpty()) {
                // get original entry content
                oldcontent = dataObj.getZettelContent(TreeUtil.extractEntryNumberFromNode(node));
            }
            // create a new dialog with the bigger edit-field, passing some initial values
            modifyEntryDlg = new CModifyDesktopEntry(this, settingsObj, spellObj, stenoObj, accKeys, oldcontent);
            // center window
            modifyEntryDlg.setLocationRelativeTo(this);
            ZettelkastenApp.getApplication().show(modifyEntryDlg);
            // edit window was initialized
            isEditModeActive = true;
        }
    }

    /**
     * This method replaces an original's entry's content with the modifications
     * that have been made for this entry on the desktop.
     */
    @Action(enabledProperty = "modifiedEntryNode")
    public void applyModificationsToOriginalEntry() {
        // as whether original entry's content should really be replaced
        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForApplyModificationsMsg"), resourceMap.getString("askForApplyModificationsTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // if yes, go on
        if (JOptionPane.YES_OPTION == option) {
            // retrieve selected entry-number
            int nr = getSelectedEntryNumber();
            // check for valid value
            if (nr != -1) {
                // retrieve timestamp of selected entry
                String timestamp = getSelectedNodeTimestamp();
                // retrieve modified content
                String content = desktopObj.retrieveModifiedEntryContentFromTimestamp(timestamp);
                // and change entry
                dataObj.setZettelContent(nr, content, true);
                // remove modified entry from modification list
                desktopObj.deleteModifiedEntry(timestamp);
                // check whether modified entry is currently displayed. if so,
                // update view in mainframe...
                if (nr == zknframe.displayedZettel) {
                    zknframe.updateZettelContent(nr);
                }
            }
        }
    }

    /**
     * This method replaces an original's entry's content with the modifications
     * that have been made for this entry on the desktop.
     */
    @Action
    public void applyAllModificationsToOriginalEntries() {
        // as whether original entry's content should really be replaced
        int option = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForApplyModificationsMsg"), resourceMap.getString("askForApplyModificationsTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // if yes, go on
        if (JOptionPane.YES_OPTION == option) {
            // retrieve all entry timestamps from desktop
            String[] alltimestamps = desktopObj.retrieveEntryTimestampsFromDesktop();
            // check for valid value
            if (alltimestamps != null) {
                // iterate array
                for (String ats : alltimestamps) {
                    // get entry number
                    int enr = desktopObj.findEntryNrFromTimestamp(ats);
                    // retrieve modified content
                    String content = desktopObj.retrieveModifiedEntryContentFromTimestamp(ats);
                    // check for valid values
                    if (enr != -1 && content != null) {
                        // and change entry
                        dataObj.setZettelContent(enr, content, true);
                        // remove modified entry from modification list
                        desktopObj.deleteModifiedEntry(ats);
                        // check whether modified entry is currently displayed. if so,
                        // update view in mainframe...
                        if (enr == zknframe.displayedZettel) {
                            zknframe.updateZettelContent(enr);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is called after an entry was modified (see
     * {@link #modifiyEntry() modifiyEntry()} and
     * {@link #modifyEntryDlg modifyEntryDlg} for more details). Since the
     * edit-window is modal, we need a message from that window when the editing
     * is finished and when the changes can be applied to the desktop data. the
     * edit-window calls this method to indicate the finishing of an
     * edit-operation.
     */
    public void finishedEditing() {
        // after closing the window, get the new value
        String newcontent = modifyEntryDlg.getModifiedEntry();
        boolean newconmodified = modifyEntryDlg.isModified();
        // delete the input-dialog
        modifyEntryDlg.dispose();
        modifyEntryDlg = null;
        // reset flag
        isEditModeActive = false;
        // check for valid return value
        if (newconmodified && newcontent != null && !newcontent.isEmpty()) {
            // change modified entry
            if (desktopObj.changeEntry(editModeTimeStamp, newcontent)) {
                // update menu-items
                updateToolbarAndMenu();
                // change updateflag
                setNeedsUpdate(true);
            } else {
                // if an error occured, tell user about it...
                JOptionPane.showMessageDialog(this, resourceMap.getString("errModifyEntryMsg"), resourceMap.getString("errModifyEntryTitle"), JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    // TODO kommentare von einträgen mit kopieren/verschieben
    /**
     * Retrieves the entry-number of the selected entry and copies it to the
     * internal clipboard. after that, the selected node is deleted.
     */
    @Action(enabledProperty = "nodeSelected")
    public void cutNode() {
        if (isBulletSelected()) {
            // store value, so we know the clipboard does contain bullets
            clipBullet = true;
            // copy bullet's name into variable
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
            clipBulletName = node.toString();
            // tell the desktop-data-class that the selected bullet has to be stored
            // for later use
            desktopObj.cutBulletToClip(TreeUtil.getNodeTimestamp(node));
            // update view
            updateTreeView();
        } else {
            // retrieve selected entry.
            int nr = getSelectedEntryNumber();
            // if we have any entries, copy their numbers to the clipboard
            if (nr != -1) {
                // retrieve timestamp of selected node
                String ts = TreeUtil.getNodeTimestamp((DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent());
                // retrieve modifications of that entry, if any modifications were made
                clipModifiedEntryContent = desktopObj.retrieveModifiedEntryContentFromTimestamp(ts);
                // delete selected node
                deleteNode();
                // and copy the entrynumber to the clipboard
                clipEntries = new int[]{nr};
                // store value, so we know the clipboard does not contain bullets
                clipBullet = false;
            }
        }
        // update toolbars
        updateToolbarAndMenu();
    }

    /**
     * Retrieves the entry-number of the selected entry and copies it to the
     * internal clipboard.
     */
    @Action(enabledProperty = "nodeSelected")
    public void copyNode() {
        if (isBulletSelected()) {
            // store value, so we know the clipboard does contain bullets
            clipBullet = true;
            // copy bullet's name into variable
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
            clipBulletName = node.toString();
            // tell the desktop-data-class that the selected bullet has to be stored
            // for later use
            desktopObj.copyBulletToClip(TreeUtil.getNodeTimestamp(node));
        } else {
            // retrieve selected entry.
            int nr = getSelectedEntryNumber();
            // if we have any entries, copy their numbers to the clipboard
            if (nr != -1) {
                // retrieve timestamp of selected node
                String ts = TreeUtil.getNodeTimestamp((DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent());
                // retrieve modifications of that entry, if any modifications were made
                clipModifiedEntryContent = desktopObj.retrieveModifiedEntryContentFromTimestamp(ts);
                // and copy the entrynumber to the clipboard
                clipEntries = new int[]{nr};
                // store value, so we know the clipboard does not contain bullets
                clipBullet = false;
            }
        }
        // update toolbars
        updateToolbarAndMenu();
    }

    /**
     * This method pastes a node or bullet from the clipboard into the treeview.
     * Relevant variables for this operation are:<br><ul>
     * <li>{@code clipBullet}, a boolean variable that indicates whether the
     * internal clipboard is filled with entries (nodes) or a bullet</li>
     * <li>{@code clipBulletName}, a string variable containing the name of the
     * cut or copied bullet-point, in case we have a bullet-point in the
     * clkipboard. this is need for checking if the bullet exists at that point
     * where it should be inserted (pasted)</li>
     * <li> {@code clipEntries}, an integer-array containing the entry-numbers
     * of the entries that should be pasted in case we have entries (nodes) in
     * the clipboard.</li>
     * </ul>
     */
    @Action(enabledProperty = "clipFilled")
    public void pasteNode() {
        // when we have a bullet-point in the clipboard, go on here...
        if (clipBullet) {
            // cut off id from clipbullet-timestamp-id
            int pos = clipBulletName.indexOf("[");
            // only continue if we found something...
            if (pos != -1) {
                // compare child's text with newBullet-text
                clipBulletName = clipBulletName.substring(0, pos).trim();
            }
            // first check, whether the bullet-point already exists as child where the user
            // wants to pase the bullet
            if (!checkIfBulletExists(clipBulletName)) {
                // get selection
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
                // if not, insert it.
                desktopObj.pasteBulletFromClip(getSelectedNodeTimestamp(), node.isRoot());
                // update treeview
                updateTreeView();
            }
        } // ...else if we have entries in the clipboard, add them now...
        else if ((clipEntries != null) && (clipEntries.length > 0)) {
            // check whether we have only one entry and modifications
            // this indicates a cut/copy and paste, so we use the other add-method
            // which also add modifications.
            if (1 == clipEntries.length) {
                addEntries(String.valueOf(clipEntries[0]), clipModifiedEntryContent, null);
            } else {
                addEntries(clipEntries);
            }
        }
    }

    /**
     * This method moves the selected node in the jTree upwards, if possible.
     */
    @Action(enabledProperty = "moveUpEnabled")
    public void moveNodeUp() {
        moveNode(Constants.MOVE_UP);
    }

    /**
     * This method moves the selected node in the jTree downwards, if possible.
     */
    @Action(enabledProperty = "moveDownEnabled")
    public void moveNodeDown() {
        moveNode(Constants.MOVE_DOWN);
    }

    /**
     * This method moves the selected node up- or downwards, if possible,
     * depending on the parameter {@code movement}.
     *
     * @param movement indicates whether the selected node in the jTree should
     * be moved up- or downwards. use following constants:<br> -
     * {@code CConstants.MOVE_UP}<br> - {@code CConstants.MOVE_DOWN}
     */
    private void moveNode(int movement) {
        // retrieve timestamp from selected node
        String timestamp = getSelectedNodeTimestamp();
        // move element up
        desktopObj.moveElement(movement, timestamp);
        // update treeview
        updateTreeView();
        // re-select the previous path
        selectTreePath(timestamp);
        // update toolbars
        updateToolbarAndMenu();
    }

    /**
     * This method selects the node in the jTree that matches the timestamp-id
     * {@code timestamp}.
     *
     * @param timestamp the timestamp of the entry which node should be selected
     * in the jTree.
     */
    private void selectTreePath(String timestamp) {
        // get the root
        DefaultMutableTreeNode node = findNodeFromTimestamp((DefaultMutableTreeNode) jTreeDesktop.getModel().getRoot(), timestamp);
        // if we have a valid value...
        if (node != null) {
            // select node
            jTreeDesktop.setSelectionPath(new TreePath(node.getPath()));
        }
        // and bring focus back to window, if necessary
        this.toFront();
        jTreeDesktop.requestFocusInWindow();
    }

    /**
     * This method recursevily traverses the jTree, starting at the position
     * {@code node}, and tries to find an entry with the timestamp-id
     * {@code timestamp}.
     *
     * @param node the starting-point in the jTree, where this method searches
     * through all (child-)elements.
     * @param timestamp the timestamp-id of the requested entry
     * @return the found node that matches the entry with the timestamp-id
     * {@code timestamp}, or {@code null} if nothing was found.
     */
    private DefaultMutableTreeNode findNodeFromTimestamp(DefaultMutableTreeNode node, String timestamp) {
        // check whether the element "e" passed as parameter already has a timestamp-attribute that
        // matches the parameter "t". if so, return that element.
        String ts = TreeUtil.getNodeTimestamp(node);
        if (ts != null && ts.equals(timestamp)) {
            return node;
        }
        // now go through all parts of the previously selected path
        Enumeration children = node.children();
        // go through all children
        while (children.hasMoreElements()) {
            // get the child
            node = (DefaultMutableTreeNode) children.nextElement();
            // check whether element has a timestamp value at all, and if it matches the parameter "t".
            ts = TreeUtil.getNodeTimestamp(node);
            if (ts != null && ts.equals(timestamp)) {
                return node;
            }
            // when the new element also has children, call this method again,
            // so we go through the strucuture recursively...
            if (node.getChildCount() > 0) {
                // if we have any child-elements, go into method again
                // to traverse recursevely all elements
                node = findNodeFromTimestamp(node, timestamp);
                // retrieve timestamp from returned node.
                ts = TreeUtil.getNodeTimestamp(node);
                // if we found a match, leave function and don't iterate any further elements
                if (ts != null && ts.equals(timestamp)) {
                    return node; /* else return null; */

                }
            }
        }
        // retrieve timestamp from returned node.
        ts = TreeUtil.getNodeTimestamp(node);
        // and return either the node if timestamp matches or null if not.
        if (ts != null && ts.equals(timestamp)) {
            return node;
        } else {
            return null;
        }
    }

    /**
     * This method updates the display, i.e. the whole page (the entries in
     * html-formatting) is new created and then set to the jEditorPane. This
     * action is only enabled whenever changes to the desktopstructure have been
     * made, like inserting or renaming bullets, inserting or removing entries
     * and so on...
     */
    @Action(enabledProperty = "needsUpdate")
    public void updateView() {
        updateDisplay();
    }

    /**
     * Activates or deactivates the fullscreen-mode, thus switching between
     * fullscreen and normal view.
     */
    @Action(enabledProperty = "fullScreenSupp")
    public void viewFullScreen() {
        // check whether fullscreen is possible or not...
        if (graphicdevice.isFullScreenSupported()) {
            // if we already have a fullscreen window, quit fullscreen
            if (graphicdevice.getFullScreenWindow() != null) {
                quitFullScreen();
            } // else show fullscreen window
            else {
                showFullScreen();
            }
        }
    }

    /**
     * This method activates the fullscreen-mode, if it's not already activated
     * yet. To have a fullscreen-window without decoration, the frame is
     * disposed first, then the decoration will be removed and the window made
     * visible again.
     */
    private void showFullScreen() {
        // check whether fullscreen is supported, and if we currently have a fullscreen-window
        if (graphicdevice.isFullScreenSupported() && null == graphicdevice.getFullScreenWindow()) {
            // dispose frame, so we can remove the decoration when setting full screen mode
            mainframe.dispose();
            // hide menubar
            jMenuBarDesktop.setVisible(false);
            // set frame non-resizable
            mainframe.setResizable(false);
            try {
                // remove decoration
                mainframe.setUndecorated(true);
            } catch (IllegalComponentStateException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            // show frame again
            mainframe.setVisible(true);
            // set fullscreen mode to this window
            graphicdevice.setFullScreenWindow(this);
        }
    }

    /**
     * This method <i>de</i>activates the fullscreen-mode, if it's not already
     * deactivated yet.
     */
    private void quitFullScreen() {
        // check whether fullscreen is supported, and if we currently have a fullscreen-window
        if (graphicdevice.isFullScreenSupported() && graphicdevice.getFullScreenWindow() != null) {
            // disable fullscreen-mode
            graphicdevice.setFullScreenWindow(null);
            // hide menubar
            jMenuBarDesktop.setVisible(true);
            // make frame resizable again
            mainframe.setResizable(true);
            // dispose frame, so we can restore the decoration
            mainframe.dispose();
            try {
                // set decoration
                mainframe.setUndecorated(false);
            } catch (IllegalComponentStateException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            }
            // show frame again
            mainframe.setVisible(true);
        }
    }

    /**
     * This method extracts the timestamp-id of the selected node's name.
     *
     * @return the timestamp-id of the node's name (userobject) as string, or
     * {@code null} if an error occured or nothing was found.
     */
    private String getSelectedNodeTimestamp() {
        DefaultMutableTreeNode selectednode = (DefaultMutableTreeNode) jTreeDesktop.getLastSelectedPathComponent();
        return (TreeUtil.getNodeTimestamp(selectednode));
    }

    /**
     * This actions opens an input-dialog where the user cann add comments to
     * the currently selected node or bullet-point.<br><br>
     * These comments are displayed in a grey area, just like it appears in
     * typical office-word-programs (word, writer, pages...).<br><br>
     * The comments are added to the xml-file that stores the desktop-data as
     * well as to a linked list which manages the comments. See
     * {@link #exportEntriesToEditorPane exportEntriesToEditorPane} for more
     * details on how the display-page is created.
     */
    @Action(enabledProperty = "nodeSelected")
    public void commentNode() {
        // get the comment of the selected entry
        String oldcomment = desktopObj.getComment(getSelectedNodeTimestamp(), lineseparator);
        // open an input-dialog, setting the selected value as default-value
        if (null == biggerEditDlg) {
            // create a new dialog with the bigger edit-field, passing some initial values
            biggerEditDlg = new CBiggerEditField(this, settingsObj, resourceMap.getString("newCommentTitle"), oldcomment, "", Constants.EDIT_DESKTOP_COMMENT);
            // center window
            biggerEditDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(biggerEditDlg);
        // after closing the window, get the new value
        String newcomment = biggerEditDlg.getNewValue();
        // delete the input-dialog
        biggerEditDlg.dispose();
        biggerEditDlg = null;

        if (newcomment != null) {
            // and re-convert all new lines to br's. this is necessary for converting
            // them into <br>'s because the entry is displayed as html-content. simple
            // new lines without "<br>" command would not be shown as new lines
            //
            // but first, we habe to remove all carriage-returns (\r), which are part of the
            // line-seperator in windows. somehow, the replace-command does *not* work, when
            // we replace "System.lineSeparator()" with "[br]", but only when
            // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
            // "\r", it is replaced by nothing, to clean the content.
            if (lineseparator.contains("\r")) {
                newcomment = newcomment.replace("\r", "");
            }
            newcomment = newcomment.replace("\n", "[br]");
            // set the comment to the selected entry
            if (desktopObj.setComment(getSelectedNodeTimestamp(), newcomment)) {
                // update treeview, so the new comment is also associated with the
                // correct node. we have to do this, because the comments are managed
                // via a linked list that is created in this method. otherwise the
                // comment would not appear...
                updateTreeView();
                // display needs to be updated...
                setNeedsUpdate(true);
            } else {
                // in case the new comment could not be set, tell user about it...
                JOptionPane.showMessageDialog(this, resourceMap.getString("errSetCommentMsg"), resourceMap.getString("errSetCommentTitle"), JOptionPane.PLAIN_MESSAGE);
            }
        }
        // if view option was comments only, change this option if we don't have any comments at all
        if (Constants.DESKTOP_ONLY_COMMENTS == settingsObj.getDesktopCommentDisplayOptions() && !desktopObj.desktopHasComments(desktopObj.getCurrentDesktopElement())) {
            settingsObj.setDesktopCommentDisplayOptions(Constants.DESKTOP_WITH_COMMENTS);
        }
    }

    /**
     * @return the background task
     */
    @Action
    public Task displayTask() {
        return new createDisplayTask(org.jdesktop.application.Application.getInstance(ZettelkastenApp.class));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private class createDisplayTask extends org.jdesktop.application.Task<Object, Void> {

        // create a new stringbuilder that will contain the final string, i.e.
        // the html-page which we set to the jeditorpane
        StringBuilder sbDisplayTask = new StringBuilder("");

        createDisplayTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);

            cDisplayTask = this;
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            // tell programm that the task is running
            cDisplayTaskIsRunning = true;
            // clear content
            jEditorPaneMain.setText(resourceMap.getString("contentBeingUpdated"));
            // get the treemodel
            DefaultTreeModel dtm = (DefaultTreeModel) jTreeDesktop.getModel();
            // create a new root element from the current desktop name and set it as root
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
            // check whether the current desktop has any children at all
            if (root.getChildCount() > 0) {
                //
                boolean showComments;
                // clean stringbuilder for word-count
                sbWordCountDisplayTask.setLength(0);
                // create the content for the html-page
                sbDisplayTask.append("<table ");
                if (PlatformUtil.isJava7OnMac() || PlatformUtil.isJava7OnWindows()) {
                    sbDisplayTask.append("cellspacing=\"0\" ");
                }
                sbDisplayTask.append("class=\"maintable\">");
                // check which display option is chosen
                switch (settingsObj.getDesktopCommentDisplayOptions()) {
                    case Constants.DESKTOP_WITH_COMMENTS:
                        showComments = desktopObj.desktopHasComments(desktopObj.getCurrentDesktopElement());
                        exportEntriesToEditorPane(root, sbDisplayTask, showComments);
                        break;
                    case Constants.DESKTOP_WITHOUT_COMMENTS:
                        showComments = false;
                        exportEntriesToEditorPane(root, sbDisplayTask, showComments);
                        break;
                    case Constants.DESKTOP_ONLY_COMMENTS:
                        showComments = false;
                        exportCommentsToEditorPane(root, sbDisplayTask);
                        break;
                    default:
                        showComments = desktopObj.desktopHasComments(desktopObj.getCurrentDesktopElement());
                        exportEntriesToEditorPane(root, sbDisplayTask, showComments);
                        break;
                }
                // retrieve all authors from desktop
                // therefor, we first need all entries
                int[] entries = desktopObj.retrieveDesktopEntries();
                LinkedList<String> remainingAuthors = new LinkedList<>();
                // interate entries
                for (int e : entries) {
                    // get authors
                    String[] aus = dataObj.getAuthors(e);
                    // iterate all authors
                    if (aus != null && aus.length > 0) {
                        for (String au : aus) {
                            // check whether author exists in our array
                            if (!remainingAuthors.contains(au)) {
                                // if not, add it
                                remainingAuthors.add(au);
                            }
                        }
                    }
                }
                // extract footnotes.
                LinkedList<String> footnotes = Tools.extractFootnotesFromContent(sbDisplayTask.toString());
                boolean hasAuthors = (footnotes.size() > 0 || remainingAuthors.size() > 0);
                // check whether we have any footnotes or authors
                if (hasAuthors) {
                    // first, init the list in html...
                    // create a separator line
                    sbDisplayTask.append("<tr>");
                    if (showComments) {
                        sbDisplayTask.append(lineseparator).append("<td class=\"comment\"></td>");
                    }
                    sbDisplayTask.append(lineseparator).append("<td><hr></td></tr>").append(lineseparator);
                    // add title "references"
                    sbDisplayTask.append("<tr>");
                    if (showComments) {
                        sbDisplayTask.append(lineseparator).append("<td class=\"comment\"></td>");
                    }
                    sbDisplayTask.append(lineseparator).append("<td><h1>");
                    // append a new headline with the bullet's name
                    sbDisplayTask.append(resourceMap.getString("referenceListHeading"));
                    sbDisplayTask.append("</h1></td></tr>").append(lineseparator);
                    // and add another table row for the unordered list...
                    sbDisplayTask.append("<tr>");
                    if (showComments) {
                        sbDisplayTask.append(lineseparator).append("<td class=\"comment\"></td>");
                    }
                    sbDisplayTask.append(lineseparator).append("<td>").append(lineseparator);
                }
                // now we have all footnotes, i.e. the author-index-numbers, in the linked
                // list. now we can create a reference list
                if (footnotes.size() > 0) {
                    // iterator for the linked list
                    Iterator<String> i = footnotes.iterator();

                    while (i.hasNext()) {
                        String au = i.next();
                        try {
                            int aunr = Integer.parseInt(au);
                            String aus = dataObj.getAuthor(aunr);
                            sbDisplayTask.append("<p class=\"reflist\"><b>[<a name=\"fn_").append(au).append("\">").append(au).append("</a>]</b> ");
                            sbDisplayTask.append(aus);
                            sbDisplayTask.append("</p>").append(lineseparator);
                            // remove author from remaining list
                            remainingAuthors.remove(aus);
                        } catch (NumberFormatException e) {
                            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                        }
                    }
                }
                // check whether we have any remaining authors that did not appear in footnotes
                if (remainingAuthors.size() > 0) {
                    // sort array
                    java.util.Collections.sort(remainingAuthors);
                    // iterator for the linked list
                    Iterator<String> ri = remainingAuthors.iterator();
                    while (ri.hasNext()) {
                        String aus = ri.next();
                        sbDisplayTask.append("<p class=\"reflist\">");
                        sbDisplayTask.append(aus);
                        sbDisplayTask.append("</p>").append(lineseparator);
                    }
                }
                // close tags
                if (hasAuthors) {
                    sbDisplayTask.append("</td></tr>").append(lineseparator);
                }
                // and close table tags...
                sbDisplayTask.append("</table>");
                // get complete entry-content, i.e. title and content
                String wordcoutnstring = sbWordCountDisplayTask.toString();
                // split complete content at each word
                String[] words = wordcoutnstring.toLowerCase().
                        replace("ä", "ae").
                        replace("ö", "oe").
                        replace("ü", "ue").
                        replace("ß", "ss").
                        split("\\W");
                // init wordcounter
                int wordcount = 0;
                // iterate all words of the entry
                for (String word : words) {
                    // remove all non-letter-chars
                    word = word.replace("([^A-Za-z0-9]+)", "");
                    // trim spaces
                    word = word.trim();
                    // if we have a "word" with more than one char, count it as word...
                    if (!word.isEmpty() /* && word.length()>1 */) {
                        wordcount++;
                    }
                }
                // set count words to label
                jLabelWordCount.setText("(" + String.valueOf(wordcount) + " " + resourceMap.getString("WordCount") + ", " + String.valueOf(sbWordCountDisplayTask.length()) + " " + resourceMap.getString("CharCount") + ")");
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            //
            // tell everbody that the display is up to date
            setNeedsUpdate(false);
            // store complete page. we need this string for the live-search,
            // since e.g. German umlauts will be escaped in the jEditorPane, so
            // we can't retrieve the page's content via jEditorPane.getText().
            completePage = sbDisplayTask.toString();
            // if we have any content, insert html-header at the beginning...
            if (sbDisplayTask.length() > 0) {
                sbDisplayTask.insert(0, HtmlUbbUtil.getHtmlHeaderForDesktop(settingsObj, false));
            }
            // set the html-page to the editor pane
            jEditorPaneMain.setText(sbDisplayTask.toString());
            // scroll to first entry
            jEditorPaneMain.setCaretPosition(0);
        }

        @Override
        protected void finished() {
            super.finished();
            cDisplayTaskIsRunning = false;
            cDisplayTask = null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuDesktop = new javax.swing.JPopupMenu();
        popupNewBullet = new javax.swing.JMenuItem();
        popupNewEntry = new javax.swing.JMenuItem();
        popupNewLuhmann = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        popupExportBullet = new javax.swing.JMenuItem();
        popupExportBulletToSearch = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        popupModifyEntry = new javax.swing.JMenuItem();
        popupApplyModificationsEntry = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JSeparator();
        popupCut = new javax.swing.JMenuItem();
        popupCopy = new javax.swing.JMenuItem();
        popupPaste = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        popupRename = new javax.swing.JMenuItem();
        popupComment = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        popupDelete = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        popupShowEntryInMain = new javax.swing.JMenuItem();
        popupRefreshView = new javax.swing.JMenuItem();
        jPopupMenuCutCopyPaste = new javax.swing.JPopupMenu();
        jMenuItemNotesCut = new javax.swing.JMenuItem();
        jMenuItemNotesCopy = new javax.swing.JMenuItem();
        jMenuItemNotesPaste = new javax.swing.JMenuItem();
        buttonGroupDesktopView = new javax.swing.ButtonGroup();
        jToolBarDesktop = new javax.swing.JToolBar();
        tb_newbullet = new javax.swing.JButton();
        tb_newentry = new javax.swing.JButton();
        tb_addluhmann = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        tb_modifyentry = new javax.swing.JButton();
        tb_cut = new javax.swing.JButton();
        tb_copy = new javax.swing.JButton();
        tb_paste = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        tb_moveup = new javax.swing.JButton();
        tb_movedown = new javax.swing.JButton();
        jSeparator29 = new javax.swing.JToolBar.Separator();
        tb_rename = new javax.swing.JButton();
        tb_comment = new javax.swing.JButton();
        tb_delete = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        tb_refresh = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSplitPaneDesktop1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeDesktop = new javax.swing.JTree();
        jPanel4 = new javax.swing.JPanel();
        jSplitPaneDesktop2 = new javax.swing.JSplitPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPaneMain = new javax.swing.JEditorPane();
        jPanelLiveSearch = new javax.swing.JPanel();
        jTextFieldLiveSearch = new javax.swing.JTextField();
        jButtonLivePrev = new javax.swing.JButton();
        jButtonLiveNext = new javax.swing.JButton();
        jButtonLiveCancel = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxDesktop = new javax.swing.JComboBox();
        statusAnimationLabel = new javax.swing.JLabel();
        jButtonShowMultipleOccurencesDlg = new javax.swing.JButton();
        jLabelWordCount = new javax.swing.JLabel();
        jMenuBarDesktop = new javax.swing.JMenuBar();
        desktopMenuFile = new javax.swing.JMenu();
        newDesktopMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        addBulletMenuItem = new javax.swing.JMenuItem();
        insertNewEntryMenuItem = new javax.swing.JMenuItem();
        jSeparator31 = new javax.swing.JPopupMenu.Separator();
        addEntryMenuItem = new javax.swing.JMenuItem();
        addLuhmannMenuItem = new javax.swing.JMenuItem();
        addLuhmannCompleteMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        exportSubMenu = new javax.swing.JMenu();
        exportMenuItem = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        exportMultipleMenuItem = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JSeparator();
        exportDeskToSearch = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JSeparator();
        exportMissingToSearch = new javax.swing.JMenuItem();
        jSeparator28 = new javax.swing.JSeparator();
        archiveDesktopMenuItem = new javax.swing.JMenuItem();
        importArchivedDesktopMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        desktopMenuEdit = new javax.swing.JMenu();
        modifyEntryItem = new javax.swing.JMenuItem();
        applyModificationsToOriginalMenuItem = new javax.swing.JMenuItem();
        applyAllModificationsMenuItem = new javax.swing.JMenuItem();
        jSeparator24 = new javax.swing.JSeparator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        editEntryMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        moveUpMenuItem = new javax.swing.JMenuItem();
        moveDownMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        renameBulletMenuItem = new javax.swing.JMenuItem();
        commentMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        deleteMenuItem = new javax.swing.JMenuItem();
        desktopMenuFind = new javax.swing.JMenu();
        findMenuLiveSearch = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JSeparator();
        findMenuLiveNext = new javax.swing.JMenuItem();
        findMenuLivePrev = new javax.swing.JMenuItem();
        desktopMenuView = new javax.swing.JMenu();
        updateViewMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jRadioButtonDesktopWithComment = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonDesktopWithoutComments = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonDesktopOnlyComments = new javax.swing.JRadioButtonMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        headingsVisibleMenuItem = new javax.swing.JCheckBoxMenuItem();
        entryNumberVisibleMenuItem = new javax.swing.JCheckBoxMenuItem();
        luhmannIconVisible = new javax.swing.JCheckBoxMenuItem();
        jSeparator27 = new javax.swing.JSeparator();
        displayItemsMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        viewFullScreenMenuItem = new javax.swing.JMenuItem();
        jSeparator26 = new javax.swing.JPopupMenu.Separator();
        showDesktopTreeEntryNumberMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator30 = new javax.swing.JPopupMenu.Separator();
        toggleNotesVisibility = new javax.swing.JMenuItem();

        jPopupMenuDesktop.setName("jPopupMenuDesktop"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(DesktopFrame.class, this);
        popupNewBullet.setAction(actionMap.get("addBullet")); // NOI18N
        popupNewBullet.setName("popupNewBullet"); // NOI18N
        jPopupMenuDesktop.add(popupNewBullet);

        popupNewEntry.setAction(actionMap.get("addEntry")); // NOI18N
        popupNewEntry.setName("popupNewEntry"); // NOI18N
        jPopupMenuDesktop.add(popupNewEntry);

        popupNewLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
        popupNewLuhmann.setName("popupNewLuhmann"); // NOI18N
        jPopupMenuDesktop.add(popupNewLuhmann);

        jSeparator23.setName("jSeparator23"); // NOI18N
        jPopupMenuDesktop.add(jSeparator23);

        popupExportBullet.setAction(actionMap.get("exportDesktopBullet")); // NOI18N
        popupExportBullet.setName("popupExportBullet"); // NOI18N
        jPopupMenuDesktop.add(popupExportBullet);

        popupExportBulletToSearch.setAction(actionMap.get("exportBulletToSearch")); // NOI18N
        popupExportBulletToSearch.setName("popupExportBulletToSearch"); // NOI18N
        jPopupMenuDesktop.add(popupExportBulletToSearch);

        jSeparator11.setName("jSeparator11"); // NOI18N
        jPopupMenuDesktop.add(jSeparator11);

        popupModifyEntry.setAction(actionMap.get("modifiyEntry")); // NOI18N
        popupModifyEntry.setName("popupModifyEntry"); // NOI18N
        jPopupMenuDesktop.add(popupModifyEntry);

        popupApplyModificationsEntry.setAction(actionMap.get("applyModificationsToOriginalEntry")); // NOI18N
        popupApplyModificationsEntry.setName("popupApplyModificationsEntry"); // NOI18N
        jPopupMenuDesktop.add(popupApplyModificationsEntry);

        jSeparator25.setName("jSeparator25"); // NOI18N
        jPopupMenuDesktop.add(jSeparator25);

        popupCut.setAction(actionMap.get("cutNode")); // NOI18N
        popupCut.setName("popupCut"); // NOI18N
        jPopupMenuDesktop.add(popupCut);

        popupCopy.setAction(actionMap.get("copyNode")); // NOI18N
        popupCopy.setName("popupCopy"); // NOI18N
        jPopupMenuDesktop.add(popupCopy);

        popupPaste.setAction(actionMap.get("pasteNode")); // NOI18N
        popupPaste.setName("popupPaste"); // NOI18N
        jPopupMenuDesktop.add(popupPaste);

        jSeparator12.setName("jSeparator12"); // NOI18N
        jPopupMenuDesktop.add(jSeparator12);

        popupRename.setAction(actionMap.get("renameBullet")); // NOI18N
        popupRename.setName("popupRename"); // NOI18N
        jPopupMenuDesktop.add(popupRename);

        popupComment.setAction(actionMap.get("commentNode")); // NOI18N
        popupComment.setName("popupComment"); // NOI18N
        jPopupMenuDesktop.add(popupComment);

        jSeparator13.setName("jSeparator13"); // NOI18N
        jPopupMenuDesktop.add(jSeparator13);

        popupDelete.setAction(actionMap.get("deleteNode")); // NOI18N
        popupDelete.setName("popupDelete"); // NOI18N
        jPopupMenuDesktop.add(popupDelete);

        jSeparator14.setName("jSeparator14"); // NOI18N
        jPopupMenuDesktop.add(jSeparator14);

        popupShowEntryInMain.setAction(actionMap.get("displayEntryInMainframe")); // NOI18N
        popupShowEntryInMain.setName("popupShowEntryInMain"); // NOI18N
        jPopupMenuDesktop.add(popupShowEntryInMain);

        popupRefreshView.setAction(actionMap.get("updateView")); // NOI18N
        popupRefreshView.setName("popupRefreshView"); // NOI18N
        jPopupMenuDesktop.add(popupRefreshView);

        jPopupMenuCutCopyPaste.setName("jPopupMenuCutCopyPaste"); // NOI18N

        jMenuItemNotesCut.setAction(actionMap.get("cut"));
        jMenuItemNotesCut.setName("jMenuItemNotesCut"); // NOI18N
        jPopupMenuCutCopyPaste.add(jMenuItemNotesCut);

        jMenuItemNotesCopy.setAction(actionMap.get("copy"));
        jMenuItemNotesCopy.setName("jMenuItemNotesCopy"); // NOI18N
        jPopupMenuCutCopyPaste.add(jMenuItemNotesCopy);

        jMenuItemNotesPaste.setAction(actionMap.get("paste"));
        jMenuItemNotesPaste.setName("jMenuItemNotesPaste"); // NOI18N
        jPopupMenuCutCopyPaste.add(jMenuItemNotesPaste);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(DesktopFrame.class);
        setTitle(resourceMap.getString("FormDesktop.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(350, 250));
        setName("FormDesktop"); // NOI18N

        jToolBarDesktop.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("jToolBarDesktop.border.matteColor"))); // NOI18N
        jToolBarDesktop.setFloatable(false);
        jToolBarDesktop.setRollover(true);
        jToolBarDesktop.setMinimumSize(new java.awt.Dimension(200, 10));
        jToolBarDesktop.setName("jToolBarDesktop"); // NOI18N

        tb_newbullet.setAction(actionMap.get("addBullet")); // NOI18N
        tb_newbullet.setText(resourceMap.getString("tb_newbullet.text")); // NOI18N
        tb_newbullet.setBorderPainted(false);
        tb_newbullet.setFocusPainted(false);
        tb_newbullet.setFocusable(false);
        tb_newbullet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_newbullet.setName("tb_newbullet"); // NOI18N
        tb_newbullet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_newbullet);

        tb_newentry.setAction(actionMap.get("addEntry")); // NOI18N
        tb_newentry.setText(resourceMap.getString("tb_newentry.text")); // NOI18N
        tb_newentry.setBorderPainted(false);
        tb_newentry.setFocusPainted(false);
        tb_newentry.setFocusable(false);
        tb_newentry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_newentry.setName("tb_newentry"); // NOI18N
        tb_newentry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_newentry);

        tb_addluhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
        tb_addluhmann.setText(resourceMap.getString("tb_addluhmann.text")); // NOI18N
        tb_addluhmann.setBorderPainted(false);
        tb_addluhmann.setFocusPainted(false);
        tb_addluhmann.setFocusable(false);
        tb_addluhmann.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_addluhmann.setName("tb_addluhmann"); // NOI18N
        tb_addluhmann.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_addluhmann);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBarDesktop.add(jSeparator3);

        tb_modifyentry.setAction(actionMap.get("modifiyEntry")); // NOI18N
        tb_modifyentry.setText(resourceMap.getString("tb_modifyentry.text")); // NOI18N
        tb_modifyentry.setBorderPainted(false);
        tb_modifyentry.setFocusPainted(false);
        tb_modifyentry.setFocusable(false);
        tb_modifyentry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_modifyentry.setName("tb_modifyentry"); // NOI18N
        tb_modifyentry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_modifyentry);

        tb_cut.setAction(actionMap.get("cutNode")); // NOI18N
        tb_cut.setText(resourceMap.getString("tb_cut.text")); // NOI18N
        tb_cut.setBorderPainted(false);
        tb_cut.setFocusPainted(false);
        tb_cut.setFocusable(false);
        tb_cut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_cut.setName("tb_cut"); // NOI18N
        tb_cut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_cut);

        tb_copy.setAction(actionMap.get("copyNode")); // NOI18N
        tb_copy.setText(resourceMap.getString("tb_copy.text")); // NOI18N
        tb_copy.setBorderPainted(false);
        tb_copy.setFocusPainted(false);
        tb_copy.setFocusable(false);
        tb_copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_copy.setName("tb_copy"); // NOI18N
        tb_copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_copy);

        tb_paste.setAction(actionMap.get("pasteNode")); // NOI18N
        tb_paste.setText(resourceMap.getString("tb_paste.text")); // NOI18N
        tb_paste.setBorderPainted(false);
        tb_paste.setFocusPainted(false);
        tb_paste.setFocusable(false);
        tb_paste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_paste.setName("tb_paste"); // NOI18N
        tb_paste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_paste);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBarDesktop.add(jSeparator4);

        tb_moveup.setAction(actionMap.get("moveNodeUp")); // NOI18N
        tb_moveup.setText(resourceMap.getString("tb_moveup.text")); // NOI18N
        tb_moveup.setBorderPainted(false);
        tb_moveup.setFocusPainted(false);
        tb_moveup.setFocusable(false);
        tb_moveup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_moveup.setName("tb_moveup"); // NOI18N
        tb_moveup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_moveup);

        tb_movedown.setAction(actionMap.get("moveNodeDown")); // NOI18N
        tb_movedown.setText(resourceMap.getString("tb_movedown.text")); // NOI18N
        tb_movedown.setBorderPainted(false);
        tb_movedown.setFocusPainted(false);
        tb_movedown.setFocusable(false);
        tb_movedown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_movedown.setName("tb_movedown"); // NOI18N
        tb_movedown.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_movedown);

        jSeparator29.setName("jSeparator29"); // NOI18N
        jToolBarDesktop.add(jSeparator29);

        tb_rename.setAction(actionMap.get("renameBullet")); // NOI18N
        tb_rename.setText(resourceMap.getString("tb_rename.text")); // NOI18N
        tb_rename.setBorderPainted(false);
        tb_rename.setFocusPainted(false);
        tb_rename.setFocusable(false);
        tb_rename.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_rename.setName("tb_rename"); // NOI18N
        tb_rename.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_rename);

        tb_comment.setAction(actionMap.get("commentNode")); // NOI18N
        tb_comment.setText(resourceMap.getString("tb_comment.text")); // NOI18N
        tb_comment.setBorderPainted(false);
        tb_comment.setFocusPainted(false);
        tb_comment.setFocusable(false);
        tb_comment.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_comment.setName("tb_comment"); // NOI18N
        tb_comment.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_comment);

        tb_delete.setAction(actionMap.get("deleteNode")); // NOI18N
        tb_delete.setText(resourceMap.getString("tb_delete.text")); // NOI18N
        tb_delete.setBorderPainted(false);
        tb_delete.setFocusPainted(false);
        tb_delete.setFocusable(false);
        tb_delete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_delete.setName("tb_delete"); // NOI18N
        tb_delete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_delete);

        jSeparator7.setName("jSeparator7"); // NOI18N
        jToolBarDesktop.add(jSeparator7);

        tb_refresh.setAction(actionMap.get("updateView")); // NOI18N
        tb_refresh.setText(resourceMap.getString("tb_refresh.text")); // NOI18N
        tb_refresh.setBorderPainted(false);
        tb_refresh.setFocusPainted(false);
        tb_refresh.setFocusable(false);
        tb_refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_refresh.setName("tb_refresh"); // NOI18N
        tb_refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarDesktop.add(tb_refresh);

        jPanel1.setMinimumSize(new java.awt.Dimension(200, 150));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPaneDesktop1.setBorder(null);
        jSplitPaneDesktop1.setDividerLocation(200);
        jSplitPaneDesktop1.setMinimumSize(new java.awt.Dimension(200, 200));
        jSplitPaneDesktop1.setName("jSplitPaneDesktop1"); // NOI18N
        jSplitPaneDesktop1.setOneTouchExpandable(true);

        jPanel3.setMinimumSize(new java.awt.Dimension(50, 100));
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTreeDesktop.setName("jTreeDesktop"); // NOI18N
        jScrollPane1.setViewportView(jTreeDesktop);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
        );

        jSplitPaneDesktop1.setLeftComponent(jPanel3);

        jPanel4.setMinimumSize(new java.awt.Dimension(150, 100));
        jPanel4.setName("jPanel4"); // NOI18N

        jSplitPaneDesktop2.setBorder(null);
        jSplitPaneDesktop2.setDividerLocation(400);
        jSplitPaneDesktop2.setMinimumSize(new java.awt.Dimension(150, 150));
        jSplitPaneDesktop2.setName("jSplitPaneDesktop2"); // NOI18N
        jSplitPaneDesktop2.setOneTouchExpandable(true);

        jPanel5.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jEditorPaneMain.setEditable(false);
        jEditorPaneMain.setContentType(resourceMap.getString("jEditorPaneMain.contentType")); // NOI18N
        jEditorPaneMain.setText(resourceMap.getString("jEditorPaneMain.text")); // NOI18N
        jEditorPaneMain.setName("jEditorPaneMain"); // NOI18N
        jScrollPane2.setViewportView(jEditorPaneMain);

        jPanelLiveSearch.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanelLiveSearch.setName("jPanelLiveSearch"); // NOI18N

        jTextFieldLiveSearch.setToolTipText(resourceMap.getString("jTextFieldLiveSearch.toolTipText")); // NOI18N
        jTextFieldLiveSearch.setName("jTextFieldLiveSearch"); // NOI18N

        jButtonLivePrev.setAction(actionMap.get("findLivePrev")); // NOI18N
        jButtonLivePrev.setText(resourceMap.getString("jButtonLivePrev.text")); // NOI18N
        jButtonLivePrev.setBorderPainted(false);
        jButtonLivePrev.setContentAreaFilled(false);
        jButtonLivePrev.setFocusPainted(false);
        jButtonLivePrev.setName("jButtonLivePrev"); // NOI18N

        jButtonLiveNext.setAction(actionMap.get("findLiveNext")); // NOI18N
        jButtonLiveNext.setText(resourceMap.getString("jButtonLiveNext.text")); // NOI18N
        jButtonLiveNext.setBorderPainted(false);
        jButtonLiveNext.setContentAreaFilled(false);
        jButtonLiveNext.setFocusPainted(false);
        jButtonLiveNext.setName("jButtonLiveNext"); // NOI18N

        jButtonLiveCancel.setAction(actionMap.get("findCancel")); // NOI18N
        jButtonLiveCancel.setBorderPainted(false);
        jButtonLiveCancel.setContentAreaFilled(false);
        jButtonLiveCancel.setFocusPainted(false);
        jButtonLiveCancel.setName("jButtonLiveCancel"); // NOI18N

        javax.swing.GroupLayout jPanelLiveSearchLayout = new javax.swing.GroupLayout(jPanelLiveSearch);
        jPanelLiveSearch.setLayout(jPanelLiveSearchLayout);
        jPanelLiveSearchLayout.setHorizontalGroup(
            jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLiveSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLivePrev, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLiveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLiveCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelLiveSearchLayout.setVerticalGroup(
            jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLiveSearchLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonLiveCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonLiveNext, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonLivePrev, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanelLiveSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPaneDesktop2.setLeftComponent(jPanel5);

        jPanel6.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane3.setBorder(null);
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextArea1.border.title"))); // NOI18N
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane3.setViewportView(jTextArea1);

        jScrollPane4.setBorder(null);
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTextArea2.setLineWrap(true);
        jTextArea2.setToolTipText(resourceMap.getString("jTextArea2.toolTipText")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextArea2.border.title"))); // NOI18N
        jTextArea2.setName("jTextArea2"); // NOI18N
        jScrollPane4.setViewportView(jTextArea2);

        jScrollPane5.setBorder(null);
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTextArea3.setLineWrap(true);
        jTextArea3.setWrapStyleWord(true);
        jTextArea3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextArea3.border.title"))); // NOI18N
        jTextArea3.setName("jTextArea3"); // NOI18N
        jScrollPane5.setViewportView(jTextArea3);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane3)
            .addComponent(jScrollPane5)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
        );

        jSplitPaneDesktop2.setRightComponent(jPanel6);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneDesktop2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneDesktop2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPaneDesktop1.setRightComponent(jPanel4);

        jPanel1.add(jSplitPaneDesktop1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("jPanel2.border.matteColor"))); // NOI18N
        jPanel2.setMinimumSize(new java.awt.Dimension(200, 30));
        jPanel2.setName("jPanel2"); // NOI18N

        jPanel7.setMinimumSize(new java.awt.Dimension(200, 20));
        jPanel7.setName("jPanel7"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxDesktop.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxDesktop.setName("jComboBoxDesktop"); // NOI18N

        statusAnimationLabel.setIcon(resourceMap.getIcon("statusAnimationLabel.icon")); // NOI18N
        statusAnimationLabel.setText(resourceMap.getString("statusAnimationLabel.text")); // NOI18N
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        jButtonShowMultipleOccurencesDlg.setAction(actionMap.get("openMultipleOccurencesMessageDlg")); // NOI18N
        jButtonShowMultipleOccurencesDlg.setIcon(resourceMap.getIcon("jButtonShowMultipleOccurencesDlg.icon")); // NOI18N
        jButtonShowMultipleOccurencesDlg.setToolTipText(resourceMap.getString("jButtonShowMultipleOccurencesDlg.toolTipText")); // NOI18N
        jButtonShowMultipleOccurencesDlg.setBorderPainted(false);
        jButtonShowMultipleOccurencesDlg.setContentAreaFilled(false);
        jButtonShowMultipleOccurencesDlg.setFocusPainted(false);
        jButtonShowMultipleOccurencesDlg.setIconTextGap(0);
        jButtonShowMultipleOccurencesDlg.setName("jButtonShowMultipleOccurencesDlg"); // NOI18N

        jLabelWordCount.setToolTipText(resourceMap.getString("jLabelWordCount.toolTipText")); // NOI18N
        jLabelWordCount.setName("jLabelWordCount"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxDesktop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelWordCount)
                .addGap(18, 18, 18)
                .addComponent(jButtonShowMultipleOccurencesDlg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jComboBoxDesktop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelWordCount)))
                    .addComponent(jButtonShowMultipleOccurencesDlg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jMenuBarDesktop.setMinimumSize(new java.awt.Dimension(100, 1));
        jMenuBarDesktop.setName("jMenuBarDesktop"); // NOI18N

        desktopMenuFile.setText(resourceMap.getString("desktopMenuFile.text")); // NOI18N
        desktopMenuFile.setName("desktopMenuFile"); // NOI18N

        newDesktopMenuItem.setAction(actionMap.get("newDesktop")); // NOI18N
        newDesktopMenuItem.setName("newDesktopMenuItem"); // NOI18N
        desktopMenuFile.add(newDesktopMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        desktopMenuFile.add(jSeparator1);

        addBulletMenuItem.setAction(actionMap.get("addBullet")); // NOI18N
        addBulletMenuItem.setName("addBulletMenuItem"); // NOI18N
        desktopMenuFile.add(addBulletMenuItem);

        insertNewEntryMenuItem.setAction(actionMap.get("insertEntry")); // NOI18N
        insertNewEntryMenuItem.setName("insertNewEntryMenuItem"); // NOI18N
        desktopMenuFile.add(insertNewEntryMenuItem);

        jSeparator31.setName("jSeparator31"); // NOI18N
        desktopMenuFile.add(jSeparator31);

        addEntryMenuItem.setAction(actionMap.get("addEntry")); // NOI18N
        addEntryMenuItem.setName("addEntryMenuItem"); // NOI18N
        desktopMenuFile.add(addEntryMenuItem);

        addLuhmannMenuItem.setAction(actionMap.get("addLuhmann")); // NOI18N
        addLuhmannMenuItem.setName("addLuhmannMenuItem"); // NOI18N
        desktopMenuFile.add(addLuhmannMenuItem);

        addLuhmannCompleteMenuItem.setAction(actionMap.get("addLuhmannComplete")); // NOI18N
        addLuhmannCompleteMenuItem.setName("addLuhmannCompleteMenuItem"); // NOI18N
        desktopMenuFile.add(addLuhmannCompleteMenuItem);

        jSeparator5.setName("jSeparator5"); // NOI18N
        desktopMenuFile.add(jSeparator5);

        exportSubMenu.setText(resourceMap.getString("exportSubMenu.text")); // NOI18N
        exportSubMenu.setName("exportSubMenu"); // NOI18N

        exportMenuItem.setAction(actionMap.get("exportDesktop")); // NOI18N
        exportMenuItem.setName("exportMenuItem"); // NOI18N
        exportSubMenu.add(exportMenuItem);

        jSeparator20.setName("jSeparator20"); // NOI18N
        exportSubMenu.add(jSeparator20);

        exportMultipleMenuItem.setAction(actionMap.get("exportMultipleDesktop")); // NOI18N
        exportMultipleMenuItem.setName("exportMultipleMenuItem"); // NOI18N
        exportSubMenu.add(exportMultipleMenuItem);

        jSeparator21.setName("jSeparator21"); // NOI18N
        exportSubMenu.add(jSeparator21);

        exportDeskToSearch.setAction(actionMap.get("exportToSearch")); // NOI18N
        exportDeskToSearch.setName("exportDeskToSearch"); // NOI18N
        exportSubMenu.add(exportDeskToSearch);

        jSeparator22.setName("jSeparator22"); // NOI18N
        exportSubMenu.add(jSeparator22);

        exportMissingToSearch.setAction(actionMap.get("exportMissingToSearch")); // NOI18N
        exportMissingToSearch.setName("exportMissingToSearch"); // NOI18N
        exportSubMenu.add(exportMissingToSearch);

        desktopMenuFile.add(exportSubMenu);

        jSeparator28.setName("jSeparator28"); // NOI18N
        desktopMenuFile.add(jSeparator28);

        archiveDesktopMenuItem.setAction(actionMap.get("archiveDesktop")); // NOI18N
        archiveDesktopMenuItem.setName("archiveDesktopMenuItem"); // NOI18N
        desktopMenuFile.add(archiveDesktopMenuItem);

        importArchivedDesktopMenuItem.setAction(actionMap.get("importArchivedDesktop")); // NOI18N
        importArchivedDesktopMenuItem.setName("importArchivedDesktopMenuItem"); // NOI18N
        desktopMenuFile.add(importArchivedDesktopMenuItem);

        jSeparator15.setName("jSeparator15"); // NOI18N
        desktopMenuFile.add(jSeparator15);

        printMenuItem.setAction(actionMap.get("printContent")); // NOI18N
        printMenuItem.setName("printMenuItem"); // NOI18N
        desktopMenuFile.add(printMenuItem);

        jSeparator16.setName("jSeparator16"); // NOI18N
        desktopMenuFile.add(jSeparator16);

        closeMenuItem.setAction(actionMap.get("closeWindow")); // NOI18N
        closeMenuItem.setName("closeMenuItem"); // NOI18N
        desktopMenuFile.add(closeMenuItem);

        jMenuBarDesktop.add(desktopMenuFile);

        desktopMenuEdit.setText(resourceMap.getString("desktopMenuEdit.text")); // NOI18N
        desktopMenuEdit.setName("desktopMenuEdit"); // NOI18N

        modifyEntryItem.setAction(actionMap.get("modifiyEntry")); // NOI18N
        modifyEntryItem.setName("modifyEntryItem"); // NOI18N
        desktopMenuEdit.add(modifyEntryItem);

        applyModificationsToOriginalMenuItem.setAction(actionMap.get("applyModificationsToOriginalEntry")); // NOI18N
        applyModificationsToOriginalMenuItem.setName("applyModificationsToOriginalMenuItem"); // NOI18N
        desktopMenuEdit.add(applyModificationsToOriginalMenuItem);

        applyAllModificationsMenuItem.setAction(actionMap.get("applyAllModificationsToOriginalEntries")); // NOI18N
        applyAllModificationsMenuItem.setName("applyAllModificationsMenuItem"); // NOI18N
        desktopMenuEdit.add(applyAllModificationsMenuItem);

        jSeparator24.setName("jSeparator24"); // NOI18N
        desktopMenuEdit.add(jSeparator24);

        cutMenuItem.setAction(actionMap.get("cutNode")); // NOI18N
        cutMenuItem.setName("cutMenuItem"); // NOI18N
        desktopMenuEdit.add(cutMenuItem);

        copyMenuItem.setAction(actionMap.get("copyNode")); // NOI18N
        copyMenuItem.setName("copyMenuItem"); // NOI18N
        desktopMenuEdit.add(copyMenuItem);

        pasteMenuItem.setAction(actionMap.get("pasteNode")); // NOI18N
        pasteMenuItem.setName("pasteMenuItem"); // NOI18N
        desktopMenuEdit.add(pasteMenuItem);

        jSeparator19.setName("jSeparator19"); // NOI18N
        desktopMenuEdit.add(jSeparator19);

        editEntryMenuItem.setAction(actionMap.get("editEntry")); // NOI18N
        editEntryMenuItem.setName("editEntryMenuItem"); // NOI18N
        desktopMenuEdit.add(editEntryMenuItem);

        jSeparator17.setName("jSeparator17"); // NOI18N
        desktopMenuEdit.add(jSeparator17);

        moveUpMenuItem.setAction(actionMap.get("moveNodeUp")); // NOI18N
        moveUpMenuItem.setName("moveUpMenuItem"); // NOI18N
        desktopMenuEdit.add(moveUpMenuItem);

        moveDownMenuItem.setAction(actionMap.get("moveNodeDown")); // NOI18N
        moveDownMenuItem.setName("moveDownMenuItem"); // NOI18N
        desktopMenuEdit.add(moveDownMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        desktopMenuEdit.add(jSeparator2);

        renameBulletMenuItem.setAction(actionMap.get("renameBullet")); // NOI18N
        renameBulletMenuItem.setName("renameBulletMenuItem"); // NOI18N
        desktopMenuEdit.add(renameBulletMenuItem);

        commentMenuItem.setAction(actionMap.get("commentNode")); // NOI18N
        commentMenuItem.setName("commentMenuItem"); // NOI18N
        desktopMenuEdit.add(commentMenuItem);

        jSeparator6.setName("jSeparator6"); // NOI18N
        desktopMenuEdit.add(jSeparator6);

        deleteMenuItem.setAction(actionMap.get("deleteNode")); // NOI18N
        deleteMenuItem.setName("deleteMenuItem"); // NOI18N
        desktopMenuEdit.add(deleteMenuItem);

        jMenuBarDesktop.add(desktopMenuEdit);

        desktopMenuFind.setText(resourceMap.getString("desktopMenuFind.text")); // NOI18N
        desktopMenuFind.setName("desktopMenuFind"); // NOI18N

        findMenuLiveSearch.setAction(actionMap.get("findLive")); // NOI18N
        findMenuLiveSearch.setName("findMenuLiveSearch"); // NOI18N
        desktopMenuFind.add(findMenuLiveSearch);

        jSeparator18.setName("jSeparator18"); // NOI18N
        desktopMenuFind.add(jSeparator18);

        findMenuLiveNext.setAction(actionMap.get("findLiveNext")); // NOI18N
        findMenuLiveNext.setName("findMenuLiveNext"); // NOI18N
        desktopMenuFind.add(findMenuLiveNext);

        findMenuLivePrev.setAction(actionMap.get("findLivePrev")); // NOI18N
        findMenuLivePrev.setName("findMenuLivePrev"); // NOI18N
        desktopMenuFind.add(findMenuLivePrev);

        jMenuBarDesktop.add(desktopMenuFind);

        desktopMenuView.setText(resourceMap.getString("desktopMenuView.text")); // NOI18N
        desktopMenuView.setName("desktopMenuView"); // NOI18N

        updateViewMenuItem.setAction(actionMap.get("updateView")); // NOI18N
        updateViewMenuItem.setName("updateViewMenuItem"); // NOI18N
        desktopMenuView.add(updateViewMenuItem);

        jSeparator9.setName("jSeparator9"); // NOI18N
        desktopMenuView.add(jSeparator9);

        jRadioButtonDesktopWithComment.setAction(actionMap.get("menuDisplayDesktopWithComments")); // NOI18N
        buttonGroupDesktopView.add(jRadioButtonDesktopWithComment);
        jRadioButtonDesktopWithComment.setName("jRadioButtonDesktopWithComment"); // NOI18N
        desktopMenuView.add(jRadioButtonDesktopWithComment);

        jRadioButtonDesktopWithoutComments.setAction(actionMap.get("menuDisplayDesktopWithoutComments")); // NOI18N
        buttonGroupDesktopView.add(jRadioButtonDesktopWithoutComments);
        jRadioButtonDesktopWithoutComments.setName("jRadioButtonDesktopWithoutComments"); // NOI18N
        desktopMenuView.add(jRadioButtonDesktopWithoutComments);

        jRadioButtonDesktopOnlyComments.setAction(actionMap.get("menuDisplayDesktopOnlyComments")); // NOI18N
        buttonGroupDesktopView.add(jRadioButtonDesktopOnlyComments);
        jRadioButtonDesktopOnlyComments.setName("jRadioButtonDesktopOnlyComments"); // NOI18N
        desktopMenuView.add(jRadioButtonDesktopOnlyComments);

        jSeparator8.setName("jSeparator8"); // NOI18N
        desktopMenuView.add(jSeparator8);

        headingsVisibleMenuItem.setAction(actionMap.get("switchHeadingsVisibility")); // NOI18N
        headingsVisibleMenuItem.setSelected(true);
        headingsVisibleMenuItem.setName("headingsVisibleMenuItem"); // NOI18N
        desktopMenuView.add(headingsVisibleMenuItem);

        entryNumberVisibleMenuItem.setAction(actionMap.get("switchEntryNumberVisibility")); // NOI18N
        entryNumberVisibleMenuItem.setSelected(true);
        entryNumberVisibleMenuItem.setName("entryNumberVisibleMenuItem"); // NOI18N
        desktopMenuView.add(entryNumberVisibleMenuItem);

        luhmannIconVisible.setAction(actionMap.get("switchLuhmannVisibility")); // NOI18N
        luhmannIconVisible.setSelected(true);
        luhmannIconVisible.setName("luhmannIconVisible"); // NOI18N
        desktopMenuView.add(luhmannIconVisible);

        jSeparator27.setName("jSeparator27"); // NOI18N
        desktopMenuView.add(jSeparator27);

        displayItemsMenuItem.setAction(actionMap.get("displayItems")); // NOI18N
        displayItemsMenuItem.setName("displayItemsMenuItem"); // NOI18N
        desktopMenuView.add(displayItemsMenuItem);

        jSeparator10.setName("jSeparator10"); // NOI18N
        desktopMenuView.add(jSeparator10);

        viewFullScreenMenuItem.setAction(actionMap.get("viewFullScreen")); // NOI18N
        viewFullScreenMenuItem.setName("viewFullScreenMenuItem"); // NOI18N
        desktopMenuView.add(viewFullScreenMenuItem);

        jSeparator26.setName("jSeparator26"); // NOI18N
        desktopMenuView.add(jSeparator26);

        showDesktopTreeEntryNumberMenuItem.setAction(actionMap.get("showDesktopEntryNumber")); // NOI18N
        showDesktopTreeEntryNumberMenuItem.setSelected(true);
        showDesktopTreeEntryNumberMenuItem.setName("showDesktopTreeEntryNumberMenuItem"); // NOI18N
        desktopMenuView.add(showDesktopTreeEntryNumberMenuItem);

        jSeparator30.setName("jSeparator30"); // NOI18N
        desktopMenuView.add(jSeparator30);

        toggleNotesVisibility.setAction(actionMap.get("toggleNotesVisibility")); // NOI18N
        toggleNotesVisibility.setName("toggleNotesVisibility"); // NOI18N
        desktopMenuView.add(toggleNotesVisibility);

        jMenuBarDesktop.add(desktopMenuView);

        setJMenuBar(jMenuBarDesktop);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jToolBarDesktop, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jToolBarDesktop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This variable indicates whether the selection in the jtree is a
     * bullet-point or not, and no root-element
     */
    private boolean bulletSelected = false;

    /**
     * This variable indicates whether the selection in the jtree is a
     * bullet-point or not, and no root-element
     *
     * @return Returns true, if a bullet-point (except root element) is selected
     * in the tree view.
     */
    public boolean isBulletSelected() {
        return bulletSelected;
    }

    /**
     * This variable indicates whether the selection in the jtree is a
     * bullet-point or not, and no root-element. Triggers a property change to
     * (de-)activate menu items.
     *
     * @param b true, if a bullet point is selected.
     */
    public void setBulletSelected(boolean b) {
        boolean old = isBulletSelected();
        this.bulletSelected = b;
        firePropertyChange("bulletSelected", old, isBulletSelected());
    }
    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element
     */
    private boolean entryNodeSelected = false;

    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element
     *
     * @return
     */
    public boolean isEntryNodeSelected() {
        return entryNodeSelected;
    }

    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element. Triggers a
     * property change to (de-)activate menu items.
     *
     * @param b
     */
    public void setEntryNodeSelected(boolean b) {
        boolean old = isEntryNodeSelected();
        this.entryNodeSelected = b;
        firePropertyChange("entryNodeSelected", old, isEntryNodeSelected());
    }
    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element
     */
    private boolean nodeSelected = false;

    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element
     *
     * @return
     */
    public boolean isNodeSelected() {
        return nodeSelected;
    }

    /**
     * This variable indicates whether the selection in the jtree is a
     * child-note (entry), i.e. no bullet and no root-element
     *
     * @param b
     */
    public void setNodeSelected(boolean b) {
        boolean old = isNodeSelected();
        this.nodeSelected = b;
        firePropertyChange("nodeSelected", old, isNodeSelected());
    }
    /**
     * This variable indicates whether the we have any selection in the jtree,
     * i.e. a bullet, a child-node or a root-element
     */
    private boolean anyNodeSelected = false;

    /**
     * This variable indicates whether the we have any selection in the jtree,
     * i.e. a bullet, a child-node or a root-element
     *
     * @return
     */
    public boolean isAnyNodeSelected() {
        return anyNodeSelected;
    }

    /**
     * This variable indicates whether the we have any selection in the jtree,
     * i.e. a bullet, a child-node or a root-element
     *
     * @param b
     */
    public void setAnyNodeSelected(boolean b) {
        boolean old = isAnyNodeSelected();
        this.anyNodeSelected = b;
        firePropertyChange("anyNodeSelected", old, isAnyNodeSelected());
    }
    /**
     * This variable indicates whether the selected entry was modified or not.
     */
    private boolean modifiedEntryNode = false;

    /**
     * This variable indicates whether the selected entry was modified or not.
     *
     * @return
     */
    public boolean isModifiedEntryNode() {
        return modifiedEntryNode;
    }

    /**
     * This variable indicates whether the selected entry was modified or not.
     *
     * @param b
     */
    public void setModifiedEntryNode(boolean b) {
        boolean old = isModifiedEntryNode();
        this.modifiedEntryNode = b;
        firePropertyChange("modifiedEntryNode", old, isModifiedEntryNode());
    }
    /**
     * This variable indicates whether the selected node is a child-note (entry)
     * and also has luhmann-numbers (followers)
     */
    private boolean luhmannNodeSelected = false;

    /**
     * This variable indicates whether the selected node is a child-note (entry)
     * and also has luhmann-numbers (followers)
     *
     * @return
     */
    public boolean isLuhmannNodeSelected() {
        return luhmannNodeSelected;
    }

    /**
     * This variable indicates whether the selected node is a child-note (entry)
     * and also has luhmann-numbers (followers)
     *
     * @param b
     */
    public void setLuhmannNodeSelected(boolean b) {
        boolean old = isLuhmannNodeSelected();
        this.luhmannNodeSelected = b;
        firePropertyChange("luhmannNodeSelected", old, isLuhmannNodeSelected());
    }
    /**
     * This variable indicates whether we have any entries or bullets in the
     * clipboard
     */
    private boolean clipFilled = false;

    /**
     * This variable indicates whether we have any entries or bullets in the
     * clipboard
     *
     * @return
     */
    public boolean isClipFilled() {
        return clipFilled;
    }

    /**
     * This variable indicates whether we have any entries or bullets in the
     * clipboard
     *
     * @param b
     */
    public void setClipFilled(boolean b) {
        boolean old = isClipFilled();
        this.clipFilled = b;
        firePropertyChange("clipFilled", old, isClipFilled());
    }
    /**
     * This variable indicates whether we have a non-updated display of the
     * entries, where the changes were made when this window was invisible. this
     * for instance happen when initiating the desktop after program-startup:
     * the desktop is initiated, but the jEditorPane should not be filled with
     * entries at that moment, because it might take too long for the startup.
     * instead, we will do the update of the isplay later, when the window
     * becomes visible.
     */
    private boolean needsUpdate = false;

    /**
     * This variable indicates whether we have a non-updated display of the
     * entries, where the changes were made when this window was invisible. this
     * for instance happen when initiating the desktop after program-startup:
     * the desktop is initiated, but the jEditorPane should not be filled with
     * entries at that moment, because it might take too long for the startup.
     * instead, we will do the update of the isplay later, when the window
     * becomes visible.
     *
     * @return
     */
    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    /**
     * This variable indicates whether we have a non-updated display of the
     * entries, where the changes were made when this window was invisible. this
     * for instance happen when initiating the desktop after program-startup:
     * the desktop is initiated, but the jEditorPane should not be filled with
     * entries at that moment, because it might take too long for the startup.
     * instead, we will do the update of the isplay later, when the window
     * becomes visible.
     *
     * @param b
     */
    public void setNeedsUpdate(boolean b) {
        boolean old = isNeedsUpdate();
        this.needsUpdate = b;
        firePropertyChange("needsUpdate", old, isNeedsUpdate());
    }
    /**
     * This variable indicates whether or not the fullscreen mode is supportet
     * on the current system. if not, disable related icons...
     */
    private boolean fullScreenSupp = false;

    /**
     * This variable indicates whether or not the fullscreen mode is supportet
     * on the current system. if not, disable related icons...
     *
     * @return
     */
    public boolean isFullScreenSupp() {
        return fullScreenSupp;
    }

    /**
     * This variable indicates whether or not the fullscreen mode is supportet
     * on the current system. if not, disable related icons...
     *
     * @param b
     */
    public void setFullScreenSupp(boolean b) {
        boolean old = isFullScreenSupp();
        this.fullScreenSupp = b;
        firePropertyChange("fullScreenSupp", old, isFullScreenSupp());
    }
    /**
     * This variable indicates whether the user has selected a node at the very
     * first position. depending on this, the action to move an entry/bullet up
     * is en-/disabled.
     */
    private boolean moveUpEnabled = false;

    /**
     * This variable indicates whether the user has selected a node at the very
     * first position. depending on this, the action to move an entry/bullet up
     * is en-/disabled.
     *
     * @return
     */
    public boolean isMoveUpEnabled() {
        return moveUpEnabled;
    }

    /**
     * This variable indicates whether the user has selected a node at the very
     * first position. depending on this, the action to move an entry/bullet up
     * is en-/disabled.
     *
     * @param b
     */
    public void setMoveUpEnabled(boolean b) {
        boolean old = isMoveUpEnabled();
        this.moveUpEnabled = b;
        firePropertyChange("moveUpEnabled", old, isMoveUpEnabled());
    }
    /**
     * This variable indicates whether the user has selected a node at the very
     * last position. depending on this, the action to move an entry/bullet down
     * is en-/disabled.
     */
    private boolean moveDownEnabled = false;

    /**
     * This variable indicates whether the user has selected a node at the very
     * last position. depending on this, the action to move an entry/bullet down
     * is en-/disabled.
     *
     * @return
     */
    public boolean isMoveDownEnabled() {
        return moveDownEnabled;
    }

    /**
     * This variable indicates whether the user has selected a node at the very
     * last position. depending on this, the action to move an entry/bullet down
     * is en-/disabled.
     *
     * @param b
     */
    public void setMoveDownEnabled(boolean b) {
        boolean old = isMoveDownEnabled();
        this.moveDownEnabled = b;
        firePropertyChange("moveDownEnabled", old, isMoveDownEnabled());
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addBulletMenuItem;
    private javax.swing.JMenuItem addEntryMenuItem;
    private javax.swing.JMenuItem addLuhmannCompleteMenuItem;
    private javax.swing.JMenuItem addLuhmannMenuItem;
    private javax.swing.JMenuItem applyAllModificationsMenuItem;
    private javax.swing.JMenuItem applyModificationsToOriginalMenuItem;
    private javax.swing.JMenuItem archiveDesktopMenuItem;
    private javax.swing.ButtonGroup buttonGroupDesktopView;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem commentMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu desktopMenuEdit;
    private javax.swing.JMenu desktopMenuFile;
    private javax.swing.JMenu desktopMenuFind;
    private javax.swing.JMenu desktopMenuView;
    private javax.swing.JMenuItem displayItemsMenuItem;
    private javax.swing.JMenuItem editEntryMenuItem;
    private javax.swing.JCheckBoxMenuItem entryNumberVisibleMenuItem;
    private javax.swing.JMenuItem exportDeskToSearch;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenuItem exportMissingToSearch;
    private javax.swing.JMenuItem exportMultipleMenuItem;
    private javax.swing.JMenu exportSubMenu;
    private javax.swing.JMenuItem findMenuLiveNext;
    private javax.swing.JMenuItem findMenuLivePrev;
    private javax.swing.JMenuItem findMenuLiveSearch;
    private javax.swing.JCheckBoxMenuItem headingsVisibleMenuItem;
    private javax.swing.JMenuItem importArchivedDesktopMenuItem;
    private javax.swing.JMenuItem insertNewEntryMenuItem;
    private javax.swing.JButton jButtonLiveCancel;
    private javax.swing.JButton jButtonLiveNext;
    private javax.swing.JButton jButtonLivePrev;
    private javax.swing.JButton jButtonShowMultipleOccurencesDlg;
    private javax.swing.JComboBox jComboBoxDesktop;
    private javax.swing.JEditorPane jEditorPaneMain;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelWordCount;
    private javax.swing.JMenuBar jMenuBarDesktop;
    private javax.swing.JMenuItem jMenuItemNotesCopy;
    private javax.swing.JMenuItem jMenuItemNotesCut;
    private javax.swing.JMenuItem jMenuItemNotesPaste;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelLiveSearch;
    private javax.swing.JPopupMenu jPopupMenuCutCopyPaste;
    private javax.swing.JPopupMenu jPopupMenuDesktop;
    private javax.swing.JRadioButtonMenuItem jRadioButtonDesktopOnlyComments;
    private javax.swing.JRadioButtonMenuItem jRadioButtonDesktopWithComment;
    private javax.swing.JRadioButtonMenuItem jRadioButtonDesktopWithoutComments;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JSeparator jSeparator24;
    private javax.swing.JSeparator jSeparator25;
    private javax.swing.JPopupMenu.Separator jSeparator26;
    private javax.swing.JSeparator jSeparator27;
    private javax.swing.JSeparator jSeparator28;
    private javax.swing.JToolBar.Separator jSeparator29;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator30;
    private javax.swing.JPopupMenu.Separator jSeparator31;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPaneDesktop1;
    private javax.swing.JSplitPane jSplitPaneDesktop2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField jTextFieldLiveSearch;
    private javax.swing.JToolBar jToolBarDesktop;
    private javax.swing.JTree jTreeDesktop;
    private javax.swing.JCheckBoxMenuItem luhmannIconVisible;
    private javax.swing.JMenuItem modifyEntryItem;
    private javax.swing.JMenuItem moveDownMenuItem;
    private javax.swing.JMenuItem moveUpMenuItem;
    private javax.swing.JMenuItem newDesktopMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem popupApplyModificationsEntry;
    private javax.swing.JMenuItem popupComment;
    private javax.swing.JMenuItem popupCopy;
    private javax.swing.JMenuItem popupCut;
    private javax.swing.JMenuItem popupDelete;
    private javax.swing.JMenuItem popupExportBullet;
    private javax.swing.JMenuItem popupExportBulletToSearch;
    private javax.swing.JMenuItem popupModifyEntry;
    private javax.swing.JMenuItem popupNewBullet;
    private javax.swing.JMenuItem popupNewEntry;
    private javax.swing.JMenuItem popupNewLuhmann;
    private javax.swing.JMenuItem popupPaste;
    private javax.swing.JMenuItem popupRefreshView;
    private javax.swing.JMenuItem popupRename;
    private javax.swing.JMenuItem popupShowEntryInMain;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem renameBulletMenuItem;
    private javax.swing.JCheckBoxMenuItem showDesktopTreeEntryNumberMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JButton tb_addluhmann;
    private javax.swing.JButton tb_comment;
    private javax.swing.JButton tb_copy;
    private javax.swing.JButton tb_cut;
    private javax.swing.JButton tb_delete;
    private javax.swing.JButton tb_modifyentry;
    private javax.swing.JButton tb_movedown;
    private javax.swing.JButton tb_moveup;
    private javax.swing.JButton tb_newbullet;
    private javax.swing.JButton tb_newentry;
    private javax.swing.JButton tb_paste;
    private javax.swing.JButton tb_refresh;
    private javax.swing.JButton tb_rename;
    private javax.swing.JMenuItem toggleNotesVisibility;
    private javax.swing.JMenuItem updateViewMenuItem;
    private javax.swing.JMenuItem viewFullScreenMenuItem;
    // End of variables declaration//GEN-END:variables

    private TaskProgressDialog taskDlg;
    private CModifyDesktopEntry modifyEntryDlg;
    private CDesktopExport exportWindow;
    private CBiggerEditField biggerEditDlg;
    private CDesktopDisplayItems desktopDisplayItemsDlg;
    private CDesktopMultipleExport desktopMultipleExportDlg;
    private CShowMultipleDesktopOccurences multipleOccurencesDlg;
}
