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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import com.explodingpixels.widgets.WindowUtils;
import de.danielluedecke.zettelkasten.mac.MacToolbarButton;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.NewEntryFrameUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

import org.jdesktop.application.Action;

/**
 *
 * @author danielludecke
 */
public class CModifyDesktopEntry extends javax.swing.JFrame implements WindowListener {

    private final Settings settingsObj;
    private final AutoKorrektur spellObj;
    private final StenoData stenoObj;
    private final AcceleratorKeys accKeys;
    private final DesktopFrame desktopframe;
    /**
     * An undo manager to undo/redo input from the main text field
     */
    private final UndoManager undomanager = new UndoManager();
    private boolean modified;
    public boolean isModified() {
        return modified;
    }
    private String modifiedEntry;
    public String getModifiedEntry() {
        return modifiedEntry;
    }
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(CModifyDesktopEntry.class);

    private final javax.swing.ActionMap actionMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getActionMap(CModifyDesktopEntry.class, this);
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap toolbarResourceMap = 
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(ToolbarIcons.class);

    
    /**
     * 
     * @param parent
     * @param s
     * @param ak
     * @param st
     * @param acc
     * @param content 
     */
    public CModifyDesktopEntry(DesktopFrame parent, Settings s, AutoKorrektur ak, StenoData st, AcceleratorKeys acc, String content) {

        settingsObj = s;
        spellObj = ak;
        stenoObj = st;
        accKeys = acc;
        desktopframe = parent;

        // create brushed look for window, so toolbar and window-bar become a unit
        if (settingsObj.isMacStyle()) {
            MacUtils.makeWindowLeopardStyle(getRootPane());
            // WindowUtils.createAndInstallRepaintWindowFocusListener(this);
            WindowUtils.installJComponentRepainterOnWindowFocusChanged(this.getRootPane());
        }
        // init locale for the default-actions cut/copy/paste
        Tools.initLocaleForDefaultActions(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(CModifyDesktopEntry.class, this));
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // init all our document, window, and component-listeners
        initBorders(settingsObj);
        initListeners();
        initActionMaps();
        // if we have mac os x with aqua/leopard-style make window look like native leopard
        if (settingsObj.isMacStyle()) {
            setupMacOSXLeopardStyle();
        }
        if (settingsObj.isSeaGlass()) {
            setupSeaGlassStyle();
        }
        // init default font-size for tables, lists and textfields...
        initDefaultFontSize();
        // init the accelerator table
        initActionMaps();
        // This method initialises the toolbar buttons. depending on the user-setting, we either
        // display small, medium or large icons as toolbar-icons.
        initToolbarIcons();
        // when we have an entry to edit, fill the textfields with content
        // else set probable selected text from entry as "pre-content"
        // the content of "content" is retrieved from text-selection from the main window.
        initFields(content);
        // scroll text to first line
        jTextArea1.setCaretPosition(0);
        // reset modified-state...
        setModified(false);
        // reset the undomanager, in case it stored any changes
        // from the text field initiation when editing new entries
        undomanager.discardAllEdits();
        modifiedEntry = "";
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
    }

    /**
     * This method is called when the user wants to edit an entry. Here we fill all the
     * components (textareas, litviews) with the related texts/list-items.
     */
    private void initFields(String text) {
        // add new lines after each bullet-point, so they are display in a better overview.
        // usually, to avoid <br>-tags within <ul> and <li>-tags when the entry is converted
        // to html, an entered list will be converted to a single line, removing all new lines.
        // but for editing and display, it is better to have them in single lines each.
        text = text.replace(Constants.FORMAT_LIST_OPEN, Constants.FORMAT_LIST_OPEN+System.lineSeparator());
        text = text.replace(Constants.FORMAT_LIST_CLOSE, Constants.FORMAT_LIST_CLOSE+System.lineSeparator());
        text = text.replace(Constants.FORMAT_NUMBEREDLIST_OPEN, Constants.FORMAT_NUMBEREDLIST_OPEN+System.lineSeparator());
        text = text.replace(Constants.FORMAT_NUMBEREDLIST_CLOSE, Constants.FORMAT_NUMBEREDLIST_CLOSE+System.lineSeparator());
        text = text.replace(Constants.FORMAT_LISTITEM_CLOSE, Constants.FORMAT_LISTITEM_CLOSE+System.lineSeparator());
        // and set the text to the textarea
        jTextArea1.setText(Tools.replaceUbbToUnicode(text));
    }


    /**
     * This method inits the action map for several components like the tables, the treeviews
     * or the lists. here we can associate certain keystrokes with related methods. e.g. hitting
     * the enter-key in a table shows (activates) the related entry.
     * <br><br>
     * Setting up action maps gives a better overview and is shorter than adding key-release-events
     * to all components, although key-events would fulfill the same purpose.
     * <br><br>
     * The advantage of action maps is, that dependent from the operating system we need only
     * to associte a single action. with key-events, for each component we have to check
     * whether the operating system is mac os or windows, and then checking for different keys,
     * thus doubling each command: checking for F2 to edit, or checking for command+enter and also
     * call the edit-method. using action maps, we simply as for the os once, storing the related
     * keystroke-value as string, and than assign this string-value to the components.
     */
    private void initActionMaps() {
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_tab = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                if (jTextArea1==e.getSource()) NewEntryFrameUtil.checkSteno(settingsObj,stenoObj,jTextArea1);
            }
        };
        jTextArea1.getActionMap().put("TabKeyPressed",a_tab);
        // check for os, and use appropriate controlKey
        KeyStroke ks = KeyStroke.getKeyStroke("TAB");
        jTextArea1.getInputMap().put(ks, "TabKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-space-key. this should insert a protected space sign
        AbstractAction a_space = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                if (jTextArea1==e.getSource()) jTextArea1.replaceSelection("&#160;");
            }
        };
        jTextArea1.getActionMap().put("SpaceKeyPressed",a_space);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("control SPACE");
        jTextArea1.getInputMap().put(ks, "SpaceKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-asterisk-key. this should insert a bullet sign
        AbstractAction a_bullet = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                if (jTextArea1==e.getSource()) jTextArea1.replaceSelection(String.valueOf((char)8226));
            }
        };
        jTextArea1.getActionMap().put("BulletKeyPressed",a_bullet);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS())?"control CLOSE_BRACKET":"control PLUS");
        jTextArea1.getInputMap().put(ks, "BulletKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-space-key. this should insert a protected large space sign
        AbstractAction a_largespace = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                if (jTextArea1==e.getSource()) jTextArea1.replaceSelection("&#8195;");
            }
        };
        jTextArea1.getActionMap().put("LargeSpaceKeyPressed",a_largespace);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("control shift SPACE");
        jTextArea1.getInputMap().put(ks, "LargeSpaceKeyPressed");
        //
        // here we init all the toolbar actions...
        //
        // undo
        AbstractAction a_undo = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { undoAction(); }
        };
        jTextArea1.getActionMap().put("UndoKeyPressed",a_undo);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "undoAction"));
        jTextArea1.getInputMap().put(ks, "UndoKeyPressed");
        // redo
        AbstractAction a_redo = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { redoAction(); }
        };
        jTextArea1.getActionMap().put("RedoKeyPressed",a_redo);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "redoAction"));
        jTextArea1.getInputMap().put(ks, "RedoKeyPressed");
        // select all
        AbstractAction a_selectall = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { selectAllText(); }
        };
        jTextArea1.getActionMap().put("SelectAllKeyPressed",a_selectall);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "selecteAllText"));
        jTextArea1.getInputMap().put(ks, "selectAllKeyPressed");
        // format bold
        AbstractAction a_formatbold = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatBold(); }
        };
        jTextArea1.getActionMap().put("formatBoldKeyPressed",a_formatbold);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatBold"));
        jTextArea1.getInputMap().put(ks, "formatBoldKeyPressed");
        // format italic
        AbstractAction a_formatitalic = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatItalic(); }
        };
        jTextArea1.getActionMap().put("formatItalicKeyPressed",a_formatitalic);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatItalic"));
        jTextArea1.getInputMap().put(ks, "formatItalicKeyPressed");
        // format underline
        AbstractAction a_formatunderline = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatItalic(); }
        };
        jTextArea1.getActionMap().put("formatUnderlineKeyPressed",a_formatunderline);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatUnderline"));
        jTextArea1.getInputMap().put(ks, "formatUnderlineKeyPressed");
        // format strike
        AbstractAction a_formatstrike = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatStrikeThrough(); }
        };
        jTextArea1.getActionMap().put("formatStrikeThroughKeyPressed",a_formatstrike);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatStrikeThrough"));
        jTextArea1.getInputMap().put(ks, "formatStrikeThroughKeyPressed");
        // format cite
        AbstractAction a_formatcite = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatCite(); }
        };
        jTextArea1.getActionMap().put("formatCiteKeyPressed",a_formatcite);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatCite"));
        jTextArea1.getInputMap().put(ks, "formatCiteKeyPressed");
        // format color
        AbstractAction a_formatcolor = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) { formatColor(); }
        };
        jTextArea1.getActionMap().put("formatColorKeyPressed",a_formatcolor);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, "formatColor"));
        jTextArea1.getInputMap().put(ks, "formatColorKeyPressed");
    }


    /**
     * This method sets the default font-size for tables, lists and treeviews. If the
     * user wants to have bigger font-sizes for better viewing, the new font-size will
     * be applied to the components here.
     */
    private void initDefaultFontSize() {
        // set default fonts
        Font f = settingsObj.getMainFont();
        f = new Font(f.getName(), f.getStyle(), f.getSize()+4);
        jTextArea1.setFont(f);
        // get the default fontsize for textfields
        int defaultsize = settingsObj.getTextfieldFontSize();
        // only set new fonts, when fontsize differs from the initial value
        if (defaultsize>0) {
            // get current font
            f = jTextArea1.getFont();
            // create new font, add fontsize-value
            f = new Font(f.getName(), f.getStyle(), f.getSize()+defaultsize);
            // set new font
            jTextArea1.setFont(f);
        }
    }


    private void initListeners() {
        // these code lines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(this);
        // add window listener, so we can open a confirm-exit-dialog if necessary
        jTextArea1.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { setModified(true); updateUndoRedoButtons(); }
            @Override public void insertUpdate(DocumentEvent e) { setModified(true); updateUndoRedoButtons(); }
            @Override public void removeUpdate(DocumentEvent e) { setModified(true); updateUndoRedoButtons(); }
        });
        // add undomanager to the main textfield
        jTextArea1.getDocument().addUndoableEditListener(undomanager);
        // keep the last 1000 actions for undoing
        undomanager.setLimit(1000);
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                NewEntryFrameUtil.checkSpelling(evt.getKeyCode(),jTextArea1,settingsObj,spellObj);
                NewEntryFrameUtil.autoCompleteTags(jTextArea1, evt.getKeyChar());
            }
        });
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    jPopupMenuMain.show(jTextArea1, evt.getPoint().x, evt.getPoint().y);
                }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    jPopupMenuMain.show(jTextArea1, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
    }


    /**
     * This method initialises the toolbar buttons. depending on the user-setting, we either
     * display small, medium or large icons as toolbar-icons.
     */
    public final void initToolbarIcons() {
        // check whether the toolbar should be displayed at all...
        if (!settingsObj.getShowIcons() && !settingsObj.getShowIconText()) {
            // if not, hide it and leave.
            jToolBar1.setVisible(false);
            // and set a border to the main panel, because the toolbar's dark border is hidden
            // and remove border from the main panel
            mainPanel.setBorder(new MatteBorder(1,0,0,0,ColorUtil.getBorderGray(settingsObj)));
            return;
        }
        // set toolbar visible
        jToolBar1.setVisible(true);
        // and remove border from the main panel
        mainPanel.setBorder(null);
        // init toolbar button array
        javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[] {
            tb_cut, tb_copy, tb_paste, tb_selectall, tb_undo, tb_redo,
            tb_bold, tb_italic, tb_underline, tb_strike
        };
        String[] buttonNames = new String[] { "tb_cutText", "tb_copyText", "tb_pasteText",
                                              "tb_selectallText", "tb_undoText", "tb_redoText",
                                              "tb_boldText", "tb_italicText", "tb_underlineText", "tb_strikeText"
        };
 
        String[] iconNames = new String[] { "cutIcon", "copyIcon", "pasteIcon",
                                            "selectAllIcon", "undoIcon", "redoIcon",
                                            "formatBoldIcon", "formatItalicIcon", "formatUnderlineIcon", "formatStrikeThroughIcon",
        };
        
        // set toolbar-icons' text
        if (settingsObj.getShowIconText()) {
            for (int cnt=0; cnt< toolbarButtons.length; cnt++) {
                toolbarButtons[cnt].setText(toolbarResourceMap.getString(buttonNames[cnt]));
            }
        }
        else {
            for (javax.swing.JButton tbb : toolbarButtons) {
                tbb.setText("");
            }
        }
        // show icons, if requested
        if (settingsObj.getShowIcons()) {
            // retrieve icon theme path
            String icontheme = settingsObj.getIconThemePath();
            for (int cnt=0; cnt< toolbarButtons.length; cnt++) {
                toolbarButtons[cnt].setIcon(new ImageIcon(ZettelkastenView.class.getResource(icontheme+toolbarResourceMap.getString(iconNames[cnt]))));
            }
        }
        else {
            for (javax.swing.JButton tbb : toolbarButtons) {
                tbb.setIcon(null);
            }
        }
        if (settingsObj.getShowIcons()) {
            tb_selectall.setVisible(settingsObj.getShowAllIcons());
            tb_strike.setVisible(settingsObj.getShowAllIcons());
        }
        if (settingsObj.isMacStyle()) makeMacToolbar();
        if (settingsObj.isSeaGlass()) makeSeaGlassToolbar();
    }


    private void setupSeaGlassStyle() {
        getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
        jButtonApply.putClientProperty("JComponent.sizeVariant", "small");
        jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
    }

    /**
     * This method applies some graphical stuff so the appearance of the program is even more
     * mac-like...
     */
    private void setupMacOSXLeopardStyle() {

    }

    private void makeSeaGlassToolbar() {
        Tools.makeTexturedToolBarButton(tb_cut, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_paste, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_selectall, Tools.SEGMENT_POSITION_ONLY);
        Tools.makeTexturedToolBarButton(tb_undo, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_redo, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_bold, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_italic, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_underline, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_strike, Tools.SEGMENT_POSITION_LAST);
        jToolBar1.setPreferredSize(new java.awt.Dimension(jToolBar1.getSize().width,Constants.seaGlassToolbarHeight));
        jToolBar1.add(new javax.swing.JToolBar.Separator(), 0);
    }

    
    private void makeMacToolbar() {
        // hide default toolbr
        jToolBar1.setVisible(false);
        // and create mac toolbar
        if (settingsObj.getShowIcons() || settingsObj.getShowIconText()) {

            UnifiedToolBar mactoolbar = new UnifiedToolBar();

            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_cut, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_copy, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_paste, MacToolbarButton.SEGMENT_POSITION_LAST));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 0));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_selectall, MacToolbarButton.SEGMENT_POSITION_ONLY));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 0));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_undo, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_redo, MacToolbarButton.SEGMENT_POSITION_LAST));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 0));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_bold, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_italic, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_underline, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_strike, MacToolbarButton.SEGMENT_POSITION_LAST));

            mactoolbar.installWindowDraggerOnWindow(this);
            mainPanel.add(mactoolbar.getComponent(),BorderLayout.PAGE_START);
        }
    }


    /**
     * This method retrieves the data from the textfields and adds a new entry respectively
     * changes an entry which was edited. This method is called when the user presses the ok-button
     * to finish the data-entry, closing the dialog and savign the entry to the data-file.
     */
    private void saveEntry() {
        // retrieve the content
        modifiedEntry = jTextArea1.getText();
        // and re-convert all new lines to br's. this is necessary for converting
        // them into <br>'s because the entry is displayed as html-content. simple
        // new lines without "<br>" command would not be shown as new lines
        //
        // but first, we habe to remove all carriage-returns (\r), which are part of the
        // line-seperator in windows. somehow, the replace-command does *not* work, when
        // we replace "System.lineSeparator()" with "[br]", but only when
        // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
        // "\r", it is replaced by nothing, to clean the content.
        modifiedEntry = Tools.replaceUnicodeToUbb(modifiedEntry);
        // now we have to "clean" the lists. since each bullet point starts for optical reasons
        // in a new line, but in HTML-conversion this would mean additional <br>-tags within and
        // unorder list (<ul> and <li>), we remove all [br]-tags between the list tags.
        modifiedEntry = modifiedEntry.replace("[l][br]","[l]");
        modifiedEntry = modifiedEntry.replace("[/l][br]","[/l]");
        modifiedEntry = modifiedEntry.replace("[/*][br]","[/*]");
    }


    /**
     * This method is called when the user presses the "Apply" button to save the changes.
     * We have an extra method for this to enable/disable the apply-button depending on
     * whether we have text inside the main textfield. An entry must always have content-text,
     * otherwise it is considered as "deleted".
     */
    @Action
    public void applyChanges() {
        // when the user wants to apply the changes, save the entry
        saveEntry();
        // tell mainframe that it has to update the content
        desktopframe.finishedEditing();
        // and close the window
        dispose();
        setVisible(false);
    }


    /**
     * This method checks which textfield has the focus and then selects
     * the whole text in that component.
     */
    @Action
    public void selectAllText() {
        jTextArea1.selectAll();
    }


    /**
     * This is a small method which surrounds the currently selected text
     * with tags which are supplied as parameters
     *
     * @param opentag (the tag which is placed before the selection)
     * @param closetag (the tag which is placed after the selection)
     */
    private void surroundSelection(String opentag, String closetag) {
        // check whether tag is selected or not
        if (null==jTextArea1.getSelectedText()) {
            // get caret position
            int caret = jTextArea1.getCaretPosition();
            // if we don't have any selection, just insert tags
            jTextArea1.replaceSelection(opentag+closetag);
            // set caret position in between the tags
            jTextArea1.setCaretPosition(caret+opentag.length());
        }
        else {
            // else surround selection with tags
            jTextArea1.replaceSelection(opentag+jTextArea1.getSelectedText()+closetag);
        }
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have bold-formatting.
     */
    @Action
    public void formatBold() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_BOLD_OPEN,Constants.FORMAT_BOLD_CLOSE);
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have italic-formatting.
     */
    @Action
    public void formatItalic() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_ITALIC_OPEN,Constants.FORMAT_ITALIC_CLOSE);
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have underline-formatting.
     */
    @Action
    public void formatUnderline() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[u]","[/u]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have strike-through-formatting.
     */
    @Action
    public void formatStrikeThrough() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[d]","[/d]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have a header 1st grade
     */
    @Action
    public void formatHeading1() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[h1]","[/h1]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we a header 2nd grade
     */
    @Action
    public void formatHeading2() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[h2]","[/h2]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case the text is aligned centered.
     */
    @Action
    public void alignCenter() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[c]","[/c]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have left and right border margins.
     */
    @Action
    public void alignMargin() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an input dialog and let the user input the margin
        String margin = JOptionPane.showInputDialog(resourceMap.getString("msgInputMargin"));
        // if the user cancelled the dialog, quit method
        if (null==margin) return;
        // replace commas with periods.
        margin = margin.replace(",",".");
        // else prepare tags
        surroundSelection("[m "+margin+"]","[/m]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have a quotation or citation.
     */
    @Action
    public void formatCite() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[q]","[/q]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have changed the text-color.
     */
    @Action
    public void formatColor() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("msgInputColor"), null);
        // if the user chose a color, proceed
        if (color!=null) {
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // and insert the tags
            surroundSelection("[color #"+output.toString()+"]","[/color]");
        }
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have another text-background-color
     */
    @Action
    public void formatHighlight() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("msgInputColor"), null);
        // if the user chose a color, proceed
        if (color!=null) {
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // and insert the tags
            surroundSelection("[h #"+output.toString()+"]","[/h]");
        }
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have a list with bullet points
     */
    @Action
    public void formatList() {
        // retrieve the selection
        String selection = jTextArea1.getSelectedText();
        // get system line separator
        String linesep = System.lineSeparator();
        // check whether tag is selected or not
        if (null==selection) {
            // if we don't have any selection, just insert tags
            jTextArea1.replaceSelection("[l]"+linesep+"[*][/*]"+linesep+"[/l]");
        }
        else {
            // first, we habe to remove all carriage-returns (\r), which are part of the
            // line-seperator in windows. somehow, the replace-command does *not* work, when
            // we replace "System.lineSeparator()" with "[br]", but only when
            // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
            // "\r", it is replaced by nothing, to clean the content.
            if (linesep.contains("\r")) selection = selection.replace("\r", "");
            // first of, split the selected text at each new line
            String[] lines = selection.split("\n");
            // create a new stringbuffer for the output-string
            StringBuilder output = new StringBuilder("");
            // append the "open"-tag
            output.append("[l]");
            output.append(System.lineSeparator());
            for (String line : lines) {
                // append the open/close-tags for the bullet points
                // and put the line between these tags
                output.append("[*]").append(line).append("[/*]");
                output.append(System.lineSeparator());
            }
            // finally, append the close-tag
            output.append("[/l]");
            output.append(System.lineSeparator());
            // and paste the text
            jTextArea1.replaceSelection(output.toString());
        }
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have text-superscript-formatting.
     */
    @Action
    public void formatSup() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[sup]","[/sup]");
    }


    /**
     * Retrieves the text selection from the maintextfield and sourrounds
     * it with the related format-tags. In this case we have text-subscript-formatting.
     */
    @Action
    public void formatSub() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection("[sub]","[/sub]");
    }
    /**
     * En-/disables the undo and redo buttons.<br>
     * Usually this method belongs to "updateToolbar()", but we've outsourced this
     * part so we can call this method in the documents change listener. The undo/redo
     * buttons are then also immediately being updated when the user types text.
     *
     * @param focus indicates whether the main textfield has the focus or nor. if this
     * parameter is false, the undo/redo buttons are always disabled. if the value is
     * true, the buttons are enabled when undo/redo is possible (canUndo() and canRedo()).
     */
    private void updateUndoRedoButtons() {
        // set undo/redo
        setUndoPossible(undomanager.canUndo());
        setRedoPossible(undomanager.canRedo());
    }


    /**
     * Undo function for the main text field
     */
    @Action(enabledProperty = "undoPossible")
    public void undoAction() {
        if (undomanager.canUndo()) undomanager.undo();
        jTextArea1.requestFocus();
    }


    /**
     * Redo function for the main text field
     */
    @Action(enabledProperty = "redoPossible")
    public void redoAction() {
        if (undomanager.canRedo()) undomanager.redo();
        jTextArea1.requestFocus();
    }


    /**
     * This method removes double line separators from the selection of the text field that currently has the
     * input focus, and replaces them by single line-separators
     */
    @Action
    public void removeDoubleLineSeparators() {
        // get separator char
        String sep = System.lineSeparator();
        // repalce double line separators
        removeReplacement(sep+sep," ");
    }


    private void removeReplacement(String old, String replacewith) {
        // get the text from that textfield that has the focus
        // check which textarea has the input focus, so we can copy that component
        // into our variable - so we avoid code-duplication, and not using for each textarea
        // its own spell-checking...
        // get selected text of component
        String text = jTextArea1.getSelectedText();
        // if we have no selection, retrieve whole text and
        // and select all text, so we can use "replaceSelection" to
        // insert the new text
        if (null==text) {
            // get content from textarea
            text = jTextArea1.getText();
            // select all content
            jTextArea1.selectAll();
        }
        // replace chars
        text = text.replace(old,replacewith);
        // set text back to field with focus
        jTextArea1.replaceSelection(text);
        // set the modified state
        setModified(true);
    }

    /**
     * This method removes single line separators from the selection of the text field that currently has the
     * input focus
     */
    @Action
    public void removeSingleLineSeparators() {
        // get separator char
        String sep = System.lineSeparator();
        // repalce double line separators
        removeReplacement(sep," ");
    }


    /**
     * This method removes double space chars from the selection of the text field that currently has the
     * input focus, and replaces them by single space chars
     */
    @Action
    public void removeDoubleSpaceChars() {
        // repalce double spaces
        removeReplacement("  "," ");
    }


    /**
     * This method removes tabulator chars from the selection of the text field that currently has the
     * input focus
     */
    @Action
    public void removeTabChars() {
        // replace tabs
        removeReplacement("\t","");
    }


    @Action
    public void cancel() {
        closeWindow();
    }


    /**
     * This method changed the modified state. We need to do this in a method, because
     * when the user edits an entry, the apply-button will not become enabled on changes that
     * are made outside the main jTextAreaEntry. The property "textfieldFilled", which enables
     * or disables the apply-button, is only changed when the document of the jTextAreaEntry is
     * changed. This should prevent applying empty content to a new entry.<br><br>
     * But: when editing an entry, the text-content in the jTextAreaEntry could be filled, while
     * the user changes the author-values - these changes are recognized in the modified-value,
     * but do usually not enable the apply-button. thus, when we have editmode (true), we also
     * enable the apply-button here...
     *
     * @param m whether the modified state is true or false
     */
    private void setModified(boolean m) {
        // change modified state
        modified = m;
        jButtonApply.setEnabled(m);
    }


    /**
     * Closes the dialog.
     */
    @Action
    public void closeWindow() {
        // check for modifications
        if (modified) {
            // if so, open a confirm dialog
            // first get the error message
            String confirmExitText = resourceMap.getString("msgSaveChangesOnExit");
            // and then the dialogs title
            String confirmExitTitle = resourceMap.getString("msgSaveChangesOnExitTitle");
            // and create a JOptionPane with yes/no/cancel options
            int option = JOptionPane.showConfirmDialog(this, confirmExitText, confirmExitTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // if action is cancelled, return to the program
            if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION==option /*User pressed cancel key*/) return;
            // if save is requested, save changes
            if (JOptionPane.YES_OPTION == option) {
                // check whether we hav text in the maintextfield
                String cont = jTextArea1.getText();
                // if no text/content entered, tell user that entry needs to have content
                // and return...
                if (cont.isEmpty()) {
                    JOptionPane.showMessageDialog(this,resourceMap.getString("errMsgNoContentMsg"),resourceMap.getString("errMsgNoContentTitle"),JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                // save the data
                saveEntry();
            }
            // reset modified value, so we can check in our mainframe
            // whether we have changes or not which should be updated to the display
            if (JOptionPane.NO_OPTION == option) modified=false;
        }
        // tell mainframe that it has to update the content
        desktopframe.finishedEditing();
        // and close the window
        dispose();
        setVisible(false);
    }


    /**
     * This variable indicates whether undo/redo is possible. This is the case when the main
     * text fiel (jTextAreaEntry) has the focus and changes have been made.
     */
    private boolean undoPossible = false;
    public boolean isUndoPossible() {
        return undoPossible;
    }
    public void setUndoPossible(boolean b) {
        boolean old = isUndoPossible();
        this.undoPossible = b;
        firePropertyChange("undoPossible", old, isUndoPossible());
    }
    /**
     * This variable indicates whether undo/redo is possible. This is the case when the main
     * text fiel (jTextAreaEntry) has the focus and changes have been made.
     */
    private boolean redoPossible = false;
    public boolean isRedoPossible() {
        return redoPossible;
    }
    public void setRedoPossible(boolean b) {
        boolean old = isRedoPossible();
        this.redoPossible = b;
        firePropertyChange("redoPossible", old, isRedoPossible());
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("de.danielluedecke.zettelkasten.resources.CModifyDesktopEntry");
        jToolBar1 = new JToolBar();
        tb_cut = new JButton();
        tb_copy = new JButton();
        tb_paste = new JButton();
        tb_selectall = new JButton();
        tb_undo = new JButton();
        tb_redo = new JButton();
        tb_bold = new JButton();
        tb_italic = new JButton();
        tb_underline = new JButton();
        tb_strike = new JButton();
        jButtonApply = new JButton();
        mainPanel = new JPanel();
        jScrollPane1 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jButtonCancel = new JButton();
        jPopupMenuMain = new JPopupMenu();
        popupMainCut = new JMenuItem();
        popupMainCopy = new JMenuItem();
        popupMainPaste = new JMenuItem();
        jSeparator26 = new JSeparator();
        popupMainSelectAll = new JMenuItem();
        jSeparator25 = new JSeparator();
        popupMainUndo = new JMenuItem();
        popupMainRedo = new JMenuItem();
        jSeparator20 = new JSeparator();
        formatSubmenu = new JMenu();
        popupMainBold = new JMenuItem();
        popupMainItalic = new JMenuItem();
        popupMainUnderline = new JMenuItem();
        popupMainStrikeThrough = new JMenuItem();
        jSeparator16 = new JSeparator();
        popupMainHeader1 = new JMenuItem();
        poupMainHeader2 = new JMenuItem();
        jSeparator35 = new JSeparator();
        popupMainCite = new JMenuItem();
        jSeparator17 = new JSeparator();
        popupMainTextcolor = new JMenuItem();
        popupMainHighlight = new JMenuItem();
        jSeparator18 = new JSeparator();
        popupMainCenter = new JMenuItem();
        popupMainMargin = new JMenuItem();
        popupMainList = new JMenuItem();
        jSeparator15 = new JSeparator();
        popupMainSup = new JMenuItem();
        popupMainSub = new JMenuItem();
        jSeparator19 = new JSeparator();
        removeSubMenu = new JMenu();
        popupRemoveDoubleLine = new JMenuItem();
        jSeparator31 = new JSeparator();
        popupRemoveDoubleSpace = new JMenuItem();
        jSeparator32 = new JSeparator();
        popupRemoveTab = new JMenuItem();
        jSeparator33 = new JSeparator();
        popupRemoveSingleLine = new JMenuItem();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(bundle.getString("FormCModifyDesktopEntry.title"));
        setMinimumSize(new Dimension(300, 200));
        setName("FormCModifyDesktopEntry");
        Container contentPane = getContentPane();

        //======== jToolBar1 ========
        {
            jToolBar1.setBorder(new MatteBorder(0, 0, 1, 0, Color.black));
            jToolBar1.setFloatable(false);
            jToolBar1.setRollover(true);
            jToolBar1.setMinimumSize(new Dimension(200, 10));
            jToolBar1.setName("jToolBar1");

            //---- tb_cut ----
            tb_cut.setAction(actionMap.get("cut"));
            tb_cut.setText(bundle.getString("tb_cut.text"));
            tb_cut.setBorderPainted(false);
            tb_cut.setFocusPainted(false);
            tb_cut.setFocusable(false);
            tb_cut.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_cut.setName("tb_cut");
            tb_cut.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_cut);

            //---- tb_copy ----
            tb_copy.setAction(actionMap.get("copy"));
            tb_copy.setText(bundle.getString("tb_copy.text"));
            tb_copy.setBorderPainted(false);
            tb_copy.setFocusPainted(false);
            tb_copy.setFocusable(false);
            tb_copy.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_copy.setName("tb_copy");
            tb_copy.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_copy);

            //---- tb_paste ----
            tb_paste.setAction(actionMap.get("paste"));
            tb_paste.setText(bundle.getString("tb_paste.text"));
            tb_paste.setBorderPainted(false);
            tb_paste.setFocusPainted(false);
            tb_paste.setFocusable(false);
            tb_paste.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_paste.setName("tb_paste");
            tb_paste.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_paste);

            //---- jSeparator1 ----
            JSeparator jSeparator1 = new JSeparator();
            jSeparator1.setName("jSeparator1");
            jToolBar1.addSeparator();

            //---- tb_selectall ----
            tb_selectall.setText(bundle.getString("tb_selectall.text"));
            tb_selectall.setBorderPainted(false);
            tb_selectall.setFocusPainted(false);
            tb_selectall.setFocusable(false);
            tb_selectall.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_selectall.setName("tb_selectall");
            tb_selectall.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_selectall);

            //---- jSeparator3 ----
            JSeparator jSeparator3 = new JSeparator();
            jSeparator3.setName("jSeparator3");
            jToolBar1.addSeparator();

            //---- tb_undo ----
            tb_undo.setBorderPainted(false);
            tb_undo.setFocusPainted(false);
            tb_undo.setFocusable(false);
            tb_undo.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_undo.setName("tb_undo");
            tb_undo.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_undo);

            //---- tb_redo ----
            tb_redo.setBorderPainted(false);
            tb_redo.setFocusPainted(false);
            tb_redo.setFocusable(false);
            tb_redo.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_redo.setName("tb_redo");
            tb_redo.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_redo);

            //---- jSeparator2 ----
            JSeparator jSeparator2 = new JSeparator();
            jSeparator2.setName("jSeparator2");
            jToolBar1.addSeparator();

            //---- tb_bold ----
            tb_bold.setBorderPainted(false);
            tb_bold.setFocusPainted(false);
            tb_bold.setFocusable(false);
            tb_bold.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_bold.setName("tb_bold");
            tb_bold.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_bold);

            //---- tb_italic ----
            tb_italic.setBorderPainted(false);
            tb_italic.setFocusPainted(false);
            tb_italic.setFocusable(false);
            tb_italic.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_italic.setName("tb_italic");
            tb_italic.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_italic);

            //---- tb_underline ----
            tb_underline.setBorderPainted(false);
            tb_underline.setFocusPainted(false);
            tb_underline.setFocusable(false);
            tb_underline.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_underline.setName("tb_underline");
            tb_underline.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_underline);

            //---- tb_strike ----
            tb_strike.setBorderPainted(false);
            tb_strike.setFocusPainted(false);
            tb_strike.setFocusable(false);
            tb_strike.setHorizontalTextPosition(SwingConstants.CENTER);
            tb_strike.setName("tb_strike");
            tb_strike.setVerticalTextPosition(SwingConstants.BOTTOM);
            jToolBar1.add(tb_strike);
        }

        //---- jButtonApply ----
        jButtonApply.setName("jButtonApply");

        //======== mainPanel ========
        {
            mainPanel.setMinimumSize(new Dimension(200, 100));
            mainPanel.setName("mainPanel");
            mainPanel.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing. border. EmptyBorder
            ( 0, 0, 0, 0) , "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e", javax. swing. border. TitledBorder. CENTER, javax. swing. border
            . TitledBorder. BOTTOM, new java .awt .Font ("Dialo\u0067" ,java .awt .Font .BOLD ,12 ), java. awt
            . Color. red) ,mainPanel. getBorder( )) ); mainPanel. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void
            propertyChange (java .beans .PropertyChangeEvent e) {if ("borde\u0072" .equals (e .getPropertyName () )) throw new RuntimeException( )
            ; }} );
            mainPanel.setLayout(new BorderLayout());

            //======== jScrollPane1 ========
            {
                jScrollPane1.setBorder(null);
                jScrollPane1.setMinimumSize(new Dimension(20, 20));
                jScrollPane1.setName("jScrollPane1");

                //---- jTextArea1 ----
                jTextArea1.setLineWrap(true);
                jTextArea1.setWrapStyleWord(true);
                jTextArea1.setName("jTextArea1");
                jScrollPane1.setViewportView(jTextArea1);
            }
            mainPanel.add(jScrollPane1, BorderLayout.CENTER);
        }

        //---- jButtonCancel ----
        jButtonCancel.setName("jButtonCancel");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(jToolBar1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCancel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButtonApply)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonApply)
                        .addComponent(jButtonCancel))
                    .addGap(3, 3, 3))
        );
        pack();
        setLocationRelativeTo(getOwner());

        //======== jPopupMenuMain ========
        {
            jPopupMenuMain.setName("jPopupMenuMain");

            //---- popupMainCut ----
            popupMainCut.setAction(actionMap.get("cut"));
            popupMainCut.setName("popupMainCut");
            jPopupMenuMain.add(popupMainCut);

            //---- popupMainCopy ----
            popupMainCopy.setAction(actionMap.get("copy"));
            popupMainCopy.setName("popupMainCopy");
            jPopupMenuMain.add(popupMainCopy);

            //---- popupMainPaste ----
            popupMainPaste.setAction(actionMap.get("paste"));
            popupMainPaste.setName("popupMainPaste");
            jPopupMenuMain.add(popupMainPaste);

            //---- jSeparator26 ----
            jSeparator26.setName("jSeparator26");
            jPopupMenuMain.add(jSeparator26);

            //---- popupMainSelectAll ----
            popupMainSelectAll.setName("popupMainSelectAll");
            jPopupMenuMain.add(popupMainSelectAll);

            //---- jSeparator25 ----
            jSeparator25.setName("jSeparator25");
            jPopupMenuMain.add(jSeparator25);

            //---- popupMainUndo ----
            popupMainUndo.setName("popupMainUndo");
            jPopupMenuMain.add(popupMainUndo);

            //---- popupMainRedo ----
            popupMainRedo.setName("popupMainRedo");
            jPopupMenuMain.add(popupMainRedo);

            //---- jSeparator20 ----
            jSeparator20.setName("jSeparator20");
            jPopupMenuMain.add(jSeparator20);

            //======== formatSubmenu ========
            {
                formatSubmenu.setText(bundle.getString("formatSubmenu.text"));
                formatSubmenu.setName("formatSubmenu");

                //---- popupMainBold ----
                popupMainBold.setName("popupMainBold");
                formatSubmenu.add(popupMainBold);

                //---- popupMainItalic ----
                popupMainItalic.setName("popupMainItalic");
                formatSubmenu.add(popupMainItalic);

                //---- popupMainUnderline ----
                popupMainUnderline.setName("popupMainUnderline");
                formatSubmenu.add(popupMainUnderline);

                //---- popupMainStrikeThrough ----
                popupMainStrikeThrough.setName("popupMainStrikeThrough");
                formatSubmenu.add(popupMainStrikeThrough);

                //---- jSeparator16 ----
                jSeparator16.setName("jSeparator16");
                formatSubmenu.add(jSeparator16);

                //---- popupMainHeader1 ----
                popupMainHeader1.setName("popupMainHeader1");
                formatSubmenu.add(popupMainHeader1);

                //---- poupMainHeader2 ----
                poupMainHeader2.setName("poupMainHeader2");
                formatSubmenu.add(poupMainHeader2);

                //---- jSeparator35 ----
                jSeparator35.setName("jSeparator35");
                formatSubmenu.add(jSeparator35);

                //---- popupMainCite ----
                popupMainCite.setName("popupMainCite");
                formatSubmenu.add(popupMainCite);

                //---- jSeparator17 ----
                jSeparator17.setName("jSeparator17");
                formatSubmenu.add(jSeparator17);

                //---- popupMainTextcolor ----
                popupMainTextcolor.setName("popupMainTextcolor");
                formatSubmenu.add(popupMainTextcolor);

                //---- popupMainHighlight ----
                popupMainHighlight.setName("popupMainHighlight");
                formatSubmenu.add(popupMainHighlight);

                //---- jSeparator18 ----
                jSeparator18.setName("jSeparator18");
                formatSubmenu.add(jSeparator18);

                //---- popupMainCenter ----
                popupMainCenter.setName("popupMainCenter");
                formatSubmenu.add(popupMainCenter);

                //---- popupMainMargin ----
                popupMainMargin.setName("popupMainMargin");
                formatSubmenu.add(popupMainMargin);

                //---- popupMainList ----
                popupMainList.setName("popupMainList");
                formatSubmenu.add(popupMainList);

                //---- jSeparator15 ----
                jSeparator15.setName("jSeparator15");
                formatSubmenu.add(jSeparator15);

                //---- popupMainSup ----
                popupMainSup.setName("popupMainSup");
                formatSubmenu.add(popupMainSup);

                //---- popupMainSub ----
                popupMainSub.setName("popupMainSub");
                formatSubmenu.add(popupMainSub);
            }
            jPopupMenuMain.add(formatSubmenu);

            //---- jSeparator19 ----
            jSeparator19.setName("jSeparator19");
            jPopupMenuMain.add(jSeparator19);

            //======== removeSubMenu ========
            {
                removeSubMenu.setText(bundle.getString("removeSubMenu.text"));
                removeSubMenu.setName("removeSubMenu");

                //---- popupRemoveDoubleLine ----
                popupRemoveDoubleLine.setName("popupRemoveDoubleLine");
                removeSubMenu.add(popupRemoveDoubleLine);

                //---- jSeparator31 ----
                jSeparator31.setName("jSeparator31");
                removeSubMenu.add(jSeparator31);

                //---- popupRemoveDoubleSpace ----
                popupRemoveDoubleSpace.setName("popupRemoveDoubleSpace");
                removeSubMenu.add(popupRemoveDoubleSpace);

                //---- jSeparator32 ----
                jSeparator32.setName("jSeparator32");
                removeSubMenu.add(jSeparator32);

                //---- popupRemoveTab ----
                popupRemoveTab.setName("popupRemoveTab");
                removeSubMenu.add(popupRemoveTab);

                //---- jSeparator33 ----
                jSeparator33.setName("jSeparator33");
                removeSubMenu.add(jSeparator33);

                //---- popupRemoveSingleLine ----
                popupRemoveSingleLine.setName("popupRemoveSingleLine");
                removeSubMenu.add(popupRemoveSingleLine);
            }
            jPopupMenuMain.add(removeSubMenu);
        }
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Ralf Barkow
    private JToolBar jToolBar1;
    private JButton tb_cut;
    private JButton tb_copy;
    private JButton tb_paste;
    private JButton tb_selectall;
    private JButton tb_undo;
    private JButton tb_redo;
    private JButton tb_bold;
    private JButton tb_italic;
    private JButton tb_underline;
    private JButton tb_strike;
    private JButton jButtonApply;
    private JPanel mainPanel;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;
    private JButton jButtonCancel;
    private JPopupMenu jPopupMenuMain;
    private JMenuItem popupMainCut;
    private JMenuItem popupMainCopy;
    private JMenuItem popupMainPaste;
    private JSeparator jSeparator26;
    private JMenuItem popupMainSelectAll;
    private JSeparator jSeparator25;
    private JMenuItem popupMainUndo;
    private JMenuItem popupMainRedo;
    private JSeparator jSeparator20;
    private JMenu formatSubmenu;
    private JMenuItem popupMainBold;
    private JMenuItem popupMainItalic;
    private JMenuItem popupMainUnderline;
    private JMenuItem popupMainStrikeThrough;
    private JSeparator jSeparator16;
    private JMenuItem popupMainHeader1;
    private JMenuItem poupMainHeader2;
    private JSeparator jSeparator35;
    private JMenuItem popupMainCite;
    private JSeparator jSeparator17;
    private JMenuItem popupMainTextcolor;
    private JMenuItem popupMainHighlight;
    private JSeparator jSeparator18;
    private JMenuItem popupMainCenter;
    private JMenuItem popupMainMargin;
    private JMenuItem popupMainList;
    private JSeparator jSeparator15;
    private JMenuItem popupMainSup;
    private JMenuItem popupMainSub;
    private JSeparator jSeparator19;
    private JMenu removeSubMenu;
    private JMenuItem popupRemoveDoubleLine;
    private JSeparator jSeparator31;
    private JMenuItem popupRemoveDoubleSpace;
    private JSeparator jSeparator32;
    private JMenuItem popupRemoveTab;
    private JSeparator jSeparator33;
    private JMenuItem popupRemoveSingleLine;
    // End of variables declaration//GEN-END:variables

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
}
