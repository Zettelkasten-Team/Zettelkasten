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

import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.*;
import de.danielluedecke.zettelkasten.util.misc.DateComparer;
import de.danielluedecke.zettelkasten.util.misc.Comparer;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.BibTex;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import org.jdesktop.application.Action;


/**
 *
 * @author  danielludecke
 */
public class SearchResultsFrame extends javax.swing.JFrame {

    /**
     * CDaten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    private final DesktopData desktopObj;
    /**
     * A reference to the CSearchRequests-class which stores the searchterms and other
     * search settings like case-sensitive search, where to search in and so on...
     */
    private final SearchRequests searchrequest;
    /**
     * CAccelerator object, which contains the XML data of the accelerator table for the menus
     */
    private final AcceleratorKeys accKeys;
    /**
     * Reference to the settings class.
     */
    private final Settings settingsObj;
    private final BibTex bibtexObj;
    /**
     *
     */
    private final Synonyms synonymsObj;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView mainframe;
    /**
     * create a variable for a list model. this list model is used for
     * the JList-component which displays the keywords of the current
     * entry.
     */
    private final DefaultListModel keywordListModel = new DefaultListModel();
    /**
     * Indicated whether a table's content is changed, e.g. entries deleted. if so, we have to tell this
     * the selection listener which - otherwise - would be called several times...
     */
    private boolean tableUpdateActive = false;
    /**
     * This variable gets the graphic device and ist needed for full-screen-functionality. see
     * {@link #viewFullScreen() viewFullScreen()} for more details.
     */
    private final GraphicsDevice graphicdevice = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();    
    private final JFrame searchframe;
    /**
     * Returns the table component of the search results window.
     * @return the table component of the search results window.
     */
    public JTable getSearchFrameTable() {
        return jTableResults;
    }
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap = 
        org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
        getContext().getResourceMap(SearchResultsFrame.class);    
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap toolbarResourceMap = 
        org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
        getContext().getResourceMap(ToolbarIcons.class);
    /**
     * 
     * @param zkn
     * @param d
     * @param sr
     * @param desk
     * @param s
     * @param ak
     * @param syn
     * @param bib 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public SearchResultsFrame(ZettelkastenView zkn, Daten d, SearchRequests sr, DesktopData desk, Settings s, AcceleratorKeys ak, Synonyms syn, BibTex bib) {
        searchframe = this;
        // init variables from parameters
        dataObj=d;
        desktopObj=desk;
        bibtexObj = bib;
        searchrequest=sr;
        synonymsObj = syn;
        accKeys = ak;
        settingsObj=s;
        mainframe=zkn;
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO,"Memory usage logged. Search Results Window opened.");
        }
        // init all components
        Tools.initLocaleForDefaultActions(org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(SearchResultsFrame.class, this));
        initComponents();
        initListeners();
        // remove border, gui-builder doesn't do this
        initBorders(settingsObj);
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        if (settingsObj.isSeaGlass()) {
            setupSeaGlassStyle();
        }
        // init toggle-items
        viewMenuHighlight.setSelected(settingsObj.getHighlightSearchResults());
        tb_highlight.setSelected(settingsObj.getHighlightSearchResults());
        viewMenuShowEntry.setSelected(settingsObj.getShowSearchEntry());
        jButtonResetList.setEnabled(false);
        // init table
        initTable();
        // init combobox. The automatic display-update should be managed
        // through the combobox's action listener
        initComboBox();
        // init the menu-accelerator table
        initAcceleratorTable();
        initActionMaps();
        // This method initialises the toolbar buttons. depending on the user-setting, we either
        // display small, medium or large icons as toolbar-icons.
        initToolbarIcons();
        // init default sont-sizes
        initDefaultFontSize();
        // and update the title
        updateTitle();
    }


    /**
     *
     */
    public final void updateTitle() {
        String currentTitle = getTitle();
        // get filename and find out where extension begins, so we can just set the filename as title
        File f = settingsObj.getFilePath();
        // check whether we have any valid filepath at all
        if (f!=null && f.exists()) {
            String fname = f.getName();
            // find file-extension
            int extpos = fname.lastIndexOf(Constants.ZKN_FILEEXTENSION);
            // set the filename as title
            if (extpos!=-1) {
                // show proxy-icon, only applies to mac.
                getRootPane().putClientProperty("Window.documentFile", f);
                // set file-name and app-name in title-bar
                setTitle(currentTitle+"- ["+fname.substring(0,extpos)+"]");
            }
        }
    }


    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane4.setBorder(null);
        if (settingsObj.getUseMacBackgroundColor()) {
            jListKeywords.setBackground(ColorUtil.colorJTreeLighterBackground);
            jListKeywords.setForeground(ColorUtil.colorJTreeDarkText);
        }
        if (settingsObj.isSeaGlass()) {
            jPanel3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
            jPanel4.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            jListKeywords.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jListKeywords.border.title"), settingsObj));
            if (settingsObj.getSearchFrameSplitLayout()==JSplitPane.HORIZONTAL_SPLIT) {
                jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
                jPanel2.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            }
            else {
                jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
                jPanel2.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            }
            // jPanel3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
        }
    }

    /**
     * This method initialises the toolbar buttons. depending on the user-setting, we either
     * display small, medium or large icons as toolbar-icons.
     */
    public final void initToolbarIcons() {
        // check whether the toolbar should be displayed at all...
        if (!settingsObj.getShowIcons() && !settingsObj.getShowIconText()) {
            // if not, hide it and leave.
            searchToolbar.setVisible(false);
            // and set a border to the main panel, because the toolbar's dark border is hidden
            // and remove border from the main panel
            searchMainPanel.setBorder(new MatteBorder(1,0,0,0,ColorUtil.colorDarkLineGray));
            return;
        }
        // set toolbar visible
        searchToolbar.setVisible(true);
        // and remove border from the main panel
        searchMainPanel.setBorder(null);
        // init toolbar button array
        javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[] {
            tb_copy, tb_selectall, tb_editentry, tb_remove, tb_manlinks,
            tb_luhmann, tb_bookmark, tb_desktop, tb_highlight
        };
        String[] buttonNames = new String[] { "tb_copyText", "tb_selectallText", "tb_editText",
                                              "tb_deleteText", "tb_addmanlinksText", "tb_addluhmannText",
                                              "tb_addbookmarkText", "tb_addtodesktopText", "tb_highlightText"
        };
        String[] iconNames = new String[] { "copyIcon", "selectAllIcon", "editEntryIcon",
                                            "deleteIcon", "addManLinksIcon", "addLuhmannIcon",
                                            "addBookmarksIcon", "addDesktopIcon", "highlightKeywordsIcon"
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
        if (settingsObj.isSeaGlass()) makeSeaGlassToolbar();
    }


    private void setupSeaGlassStyle() {
        getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
        jTextFieldFilterList.putClientProperty("JTextField.variant", "search");        
        jEditorPaneSearchEntry.setBackground(Color.white);
        jButtonDeleteSearch.setBorderPainted(true);
        jButtonDeleteSearch.putClientProperty("JButton.buttonType","textured");
    }
    
    
    private void makeSeaGlassToolbar() {
        Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_selectall, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_editentry, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_remove, Tools.SEGMENT_POSITION_LAST);
        Tools.makeTexturedToolBarButton(tb_manlinks, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_luhmann, Tools.SEGMENT_POSITION_MIDDLE);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_bookmark, Tools.SEGMENT_POSITION_MIDDLE);
            Tools.makeTexturedToolBarButton(tb_desktop, Tools.SEGMENT_POSITION_LAST);
        }
        else {
            Tools.makeTexturedToolBarButton(tb_bookmark, Tools.SEGMENT_POSITION_LAST);
        }
        Tools.makeTexturedToolBarButton(tb_highlight, Tools.SEGMENT_POSITION_ONLY);
        searchToolbar.setPreferredSize(new java.awt.Dimension(searchToolbar.getSize().width,Constants.seaGlassToolbarHeight));
        searchToolbar.add(new javax.swing.JToolBar.Separator(), 0);
    }


    /**
     * This method sets the default font-size for tables, lists and treeviews. If the
     * user wants to have bigger font-sizes for better viewing, the new font-size will
     * be applied to the components here.
     */
    private void initDefaultFontSize() {
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // get current font
        int fsize = jTableResults.getFont().getSize();
        // retrieve default listvewfont
        Font defaultfont = settingsObj.getTableFont();
        // create new font, add fontsize-value
        Font f = new Font(defaultfont.getName(), defaultfont.getStyle(), fsize+defaultsize);
        // set new font
        jTableResults.setFont(f);
        // set new font
        jListKeywords.setFont(f);
    }

    private void initListeners() {
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
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showDesktopWindow"));
        ActionListener showDesktopWindowAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                mainframe.showDesktopWindow();
            }
        };
        getRootPane().registerKeyboardAction(showDesktopWindowAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        ActionListener showMainFrameAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                mainframe.bringToFront();
            }
        };
        getRootPane().registerKeyboardAction(showMainFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showNewEntryWindow"));
        ActionListener showNewEntryFrameAction = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                mainframe.showNewEntryWindow();
            }
        };
        getRootPane().registerKeyboardAction(showNewEntryFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        searchSearchMenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override public void menuSelected(javax.swing.event.MenuEvent evt) {
                setListSelected(jListKeywords.getSelectedIndex()!=-1);
                String t1 = jEditorPaneSearchEntry.getSelectedText();
                setTextSelected(t1!=null && !t1.isEmpty());
            }
            @Override public void menuDeselected(javax.swing.event.MenuEvent evt) {}
            @Override public void menuCanceled(javax.swing.event.MenuEvent evt) {}
        });
        jEditorPaneSearchEntry.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            @Override public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                // get input event with additional modifiers
                java.awt.event.InputEvent inev = evt.getInputEvent();
                // check whether shift key was pressed, and if so, remove manual link
                if (inev.isControlDown() || inev.isMetaDown()) {
                    // get selected entry
                    int row = jTableResults.getSelectedRow();
                    // when we have a valid selection, go on
                    if (row!=-1) {
                        int displayedZettel = Integer.parseInt(jTableResults.getValueAt(row, 0).toString());
                        if (Tools.removeHyperlink(evt.getDescription(), dataObj, displayedZettel)) {
                            mainframe.updateDisplay();
                        }
                    }
                } else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    javax.swing.text.Element elem = evt.getSourceElement();
                    if (elem != null) {
                        AttributeSet attr = elem.getAttributes();
                        AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
                        if (a != null) {
                            jEditorPaneSearchEntry.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
                        }
                    }
                } else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    jEditorPaneSearchEntry.setToolTipText(null);
                } else {
                    openAttachment(evt);
                }
            }
        });
        jTableResults.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                // this listener should only react on left-mouse-button-clicks...
                // if other button then left-button clicked, don't count it.
                if(evt.getButton()!=MouseEvent.BUTTON1) return;
                // only show entry on double clicl
                if (2==evt.getClickCount()) displayEntryInMainframe();
            }
        });
        jTextFieldFilterList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    TableUtils.navigateThroughList(jTableResults, evt.getKeyCode());
                }
                else {
                    // select table-entry live, while the user is typing...
                    TableUtils.selectByTyping(jTableResults,jTextFieldFilterList,1);
                }
            }
        });
        //
        // Now come the mouse-listeners
        //
        // here we set up a popup-trigger for the jListEntryKeywords and how this component
        // should react on mouse-clicks. a single click filters the jTableLinks, a double-click
        // starts a keyword-search
        jListKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                // this listener should only react on left-mouse-button-clicks...
                // if other button then left-button clicked, leeave...
                if(evt.getButton()!=MouseEvent.BUTTON1) return;
                // on double click
                if (2==evt.getClickCount()) {
                    if (jListKeywords.getSelectedIndex()!=-1) newSearchFromKeywordsLogOr();
                }
                // on single click...
                if (1==evt.getClickCount()) {
                    highlightSegs();
                }
            }
        });
        jListKeywords.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent evt) {
                // if a navigation-key (arrows, page-down/up, home etc.) is pressed,
                // we assume a new item-selection, so behave like on a mouse-click and
                // filter the links
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    highlightSegs();
                }
            }
        });
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
    // <editor-fold defaultstate="collapsed" desc="Init of action-maps so we have shortcuts for the tables">
        // create action which should be executed when the user presses
        // the enter-key
        AbstractAction a_enter = new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterList==e.getSource()) filterResultList();
                if (jTableResults==e.getSource()) displayEntryInMainframe();
            }
        };
        // put action to the tables' actionmaps
        jTextFieldFilterList.getActionMap().put("EnterKeyPressed",a_enter);
        jTableResults.getActionMap().put("EnterKeyPressed",a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jTextFieldFilterList.getInputMap().put(ks, "EnterKeyPressed");        
        jTableResults.getInputMap().put(ks, "EnterKeyPressed");
    // </editor-fold>
    }
    
    
    private void highlightSegs() {
        // and highlight text segments
        if (settingsObj.getHighlightSegments()) {
            int[] selectedValues = getSelectedEntriesFromTable();
            if (selectedValues!=null && selectedValues.length>0) {
                displayZettelContent(selectedValues[0],null);
            }
        }
    }
    
    /**
     * This method sets the accelerator table for all relevant actions which should have
     * accelerator keys. We don't use the GUI designer to set the values, because the user
     * should have the possibility to define own accelerator keys, which are managed
     * within the CAcceleratorKeys-class and loaed/saved via the CSettings-class
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
        javax.swing.ActionMap actionMap = 
            org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).
            getContext().getActionMap(SearchResultsFrame.class, this);
        // iterate the xml file with the accelerator keys for the main window
        for (int cnt=1; cnt<=accKeys.getCount(AcceleratorKeys.SEARCHRESULTSKEYS); cnt++) {
            // get the action's name
            String actionname = accKeys.getAcceleratorAction(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
            // check whether we have found any valid action name
            if (actionname!=null && !actionname.isEmpty()) {
                // retrieve action
                AbstractAction ac = (AbstractAction) actionMap.get(actionname);
                // get the action's accelerator key
                String actionkey = accKeys.getAcceleratorKey(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
                // check whether we have any valid actionkey
                if (actionkey!=null && !actionkey.isEmpty()) {
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
        menutext = searchFileMenu.getText();
        mkey = menutext.charAt(0);
        searchFileMenu.setMnemonic(mkey);
        // the mnemonic key for the edit menu
        menutext = searchEditMenu.getText();
        mkey = menutext.charAt(0);
        searchEditMenu.setMnemonic(mkey);
        // the mnemonic key for the filter menu
        menutext = searchFilterMenu.getText();
        mkey = menutext.charAt(0);
        searchFilterMenu.setMnemonic(mkey);
        // the mnemonic key for the search menu
        menutext = searchSearchMenu.getText();
        mkey = menutext.charAt(0);
        searchSearchMenu.setMnemonic(mkey);
        // the mnemonic key for the view menu
        menutext = searchViewMenu.getText();
        mkey = menutext.charAt(0);
        searchViewMenu.setMnemonic(mkey);
        // on Mac OS, at least for the German locale, the File menu is called different
        // compared to windows or linux. Furthermore, we don't need the about and preferences
        // menu items, since these are locates on the program's menu item in the apple-menu-bar
        if (PlatformUtil.isMacOS()) searchFileMenu.setText(resourceMap.getString("macFileMenuText"));
        // en- or disable fullscreen icons
        setFullScreenSupp(graphicdevice.isFullScreenSupported());
        // if fullscreen is not supportet, tell this in the tooltip
        if (!graphicdevice.isFullScreenSupported()) {
            AbstractAction ac = (AbstractAction) actionMap.get("viewFullScreen");
            ac.putValue(AbstractAction.SHORT_DESCRIPTION,resourceMap.getString("fullScreenNotSupported"));
        }
    }


    /**
     * This option toggles the setting, whether a selected entry from the search results should also
     * immediately be displayed in the main frame or not.
     */
    @Action
    public void showEntryImmediately() {
        settingsObj.setShowSearchEntry(!settingsObj.getShowSearchEntry());
    }


    @Action
    public void resetResultslist() {
        prepareResultList(jComboBoxSearches.getSelectedIndex());
        // set inputfocus to the table, so key-navigation can start immediately
        jTableResults.requestFocusInWindow();
        // finally, select first entry
        try {
            jTableResults.setRowSelectionInterval(0, 0);
        }
        catch (IllegalArgumentException e) {
            Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
        }
        // enable refresh button
        jButtonResetList.setEnabled(false);
    }
    private void filterResultList() {
        // when we filter the table and want to restore it, we don't need to run the
        // time-consuming task that creates the author-list and related author-frequencies.
        // instead, we simply copy the values from the linkedlist to the table-model, which is
        // much faster. but therefore we have to apply all changes to the filtered-table
        // (like adding/changing values in a filtered list) to the linked list as well.

        // get text from the textfield containing the filter string
        // convert to lowercase, we don't want case-sensitive search
        String text = jTextFieldFilterList.getText().toLowerCase();
        // tell selection listener to do nothing...
        tableUpdateActive = true;
        // when we have no text, do nothing
        if (!text.isEmpty()) {
            // get table model
            DefaultTableModel dtm = (DefaultTableModel)jTableResults.getModel();
            // go through table and delete all rows that don't contain the filter text
            for (int cnt=(jTableResults.getRowCount()-1); cnt>=0; cnt--) {
                // retrieve row-index from the model
                int rowindex = jTableResults.convertRowIndexToModel(cnt);
                // get the string (author) value from the table
                // convert to lowercase, we don't want case-sensitive search
                String value = dtm.getValueAt(rowindex, 1).toString().toLowerCase();
                // in case we have the jTableTitles, we also add the timestamps and rating-values to the filter-value
                // so we can also filter entries according to their timestamp
                value = value+dtm.getValueAt(rowindex, 2).toString()+dtm.getValueAt(rowindex, 3).toString()+dtm.getValueAt(rowindex, 4).toString();
                // check for regex pattern
                if (text.contains("?")) {
                    try {
                        // replace all "?" into .
                        String dummy = text.replace("?", ".");
                        // in case the user wanted to search for ?, replace \. into \?.
                        dummy = dummy.replace("\\.", "\\?").toLowerCase();
                        // create regex pattern
                        Pattern pattern = Pattern.compile(dummy);
                        // now check whether pattern exists in value
                        Matcher matcher = pattern.matcher(value);
                        // if the text is *not* part of the column, delete that row
                        if (!matcher.find()) {
                            dtm.removeRow(rowindex);
                        }
                    }
                    catch (PatternSyntaxException ex) {
                        // in case of invalid regex, simply try to find the usual pattern
                        if (!value.contains(text)) dtm.removeRow(rowindex);
                    }
                }
                // if the text is *not* part of the column, delete that row
                else if (!value.contains(text)) dtm.removeRow(rowindex);
            }
            // reset textfield
            jTextFieldFilterList.setText("");
            jTextFieldFilterList.requestFocusInWindow();
            // enable textfield only if we have more than 1 element in the jtable
            jTextFieldFilterList.setEnabled(jTableResults.getRowCount()>0);
            // enable refresh button
            jButtonResetList.setEnabled(true);
            // create a new stringbuilder to prepare the label 
            // that shows the amount of found entries
            StringBuilder sb = new StringBuilder("");
            sb.append("(");
            sb.append(String.valueOf(dtm.getRowCount()));
            sb.append(" ");
            sb.append(resourceMap.getString("hitsText"));
            sb.append(")");
            // set labeltext
            jLabelHits.setText(sb.toString());
        }
        // tell selection listener action is possible again...
        tableUpdateActive = false;
    }
    
    /**
     * This option toggles the setting whether search terms should be highlighted or not.
     */
    @Action
    public void toggleHighlightResults() {
        // check whether highlighting is activated
        if (!settingsObj.getHighlightSearchResults()) {
            // if not, activate it
            settingsObj.setHighlightSearchResults(true);
        }
        else {
            // nex, if highlighting is activated,
            // check whether whole word highlighting is activated
            if (!settingsObj.getHighlightWholeWordSearch()) {
                // if not, activate whole-word-highlighting and do not
                // deactivate general highlighting
                settingsObj.setHighlightWholeWordSearch(true);
            }
            // else if both were activated, deactivate all
            else {
                settingsObj.setHighlightSearchResults(false);
                settingsObj.setHighlightWholeWordSearch(false);
            }
        }
        updateDisplay();
    }

    
    @Action
    public void addKeywordsToEntries() {
        // create linked list as parameter for filter-dialog
        LinkedList<String> keywords = new LinkedList<>();
        // go through all keyword-entries
        for (int cnt=1; cnt<=dataObj.getCount(Daten.KWCOUNT); cnt++) {
            // get keyword
            String k = dataObj.getKeyword(cnt);
            // add it to list
            if (!k.isEmpty()) keywords.add(k);
        }
        // if dialog window isn't already created, do this now
        if (null == filterSearchDlg) {
            // create a new dialog window
            filterSearchDlg = new CFilterSearch(this,settingsObj,keywords,resourceMap.getString("addKeywordsToEntriesTitle"),false);
            // center window
            filterSearchDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(filterSearchDlg);
        // when we have any selected keywords, go on and add them all to all the selected
        // entries in the search result
        if (filterSearchDlg.getFilterTerms()!=null) {
            // get all selected entries
            int[] entries = getSelectedEntriesFromTable();
            // go through all selected entries
            // now iterate the chosen keywords
            // and add each keyword to all selected entries
            for (int e : entries) dataObj.addKeywordsToEntry(filterSearchDlg.getFilterTerms(), e, 1);
            // keyword-list is not up-to-date
            dataObj.setKeywordlistUpToDate(false);
            // update the display
            updateDisplay();
        }
        // dispose window...
        filterSearchDlg.dispose();
        filterSearchDlg = null;
    }

    
    @Action
    public void switchLayout() {
        int currentlayout = settingsObj.getSearchFrameSplitLayout();
        if (JSplitPane.HORIZONTAL_SPLIT == currentlayout) {
            currentlayout = JSplitPane.VERTICAL_SPLIT;
            if (settingsObj.isSeaGlass()) {
                jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
                jPanel2.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            }
        }
        else {
            currentlayout = JSplitPane.HORIZONTAL_SPLIT;
            if (settingsObj.isSeaGlass()) {
                jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
                jPanel2.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            }
        }
        settingsObj.setSearchFrameSplitLayout(currentlayout);
        jSplitPaneSearch1.setOrientation(currentlayout);
    }
    
    
    @Action
    public void showEntryInDesktop() {
        // get selected row
        int row = jTableResults.getSelectedRow();
        // check for valid value
        if (row!=-1) {
            try {
                int nr = Integer.parseInt(jTableResults.getValueAt(row, 0).toString());
                if (desktopObj.isEntryInAnyDesktop(nr)) {
                    mainframe.showEntryInDesktopWindow(nr);
                }
            }
            catch (NumberFormatException ex) {
            }
        }
    }


    @Action
    public void addAuthorsToEntries() {
        // create linked list as parameter for filter-dialog
        LinkedList<String> suthors = new LinkedList<>();
        // go through all author-entries
        for (int cnt=1; cnt<=dataObj.getCount(Daten.AUCOUNT); cnt++) {
            // get authors
            String a = dataObj.getAuthor(cnt);
            // add it to list
            if (!a.isEmpty()) suthors.add(a);
        }
        // if dialog window isn't already created, do this now
        if (null == filterSearchDlg) {
            // create a new dialog window
            filterSearchDlg = new CFilterSearch(this,settingsObj,suthors,resourceMap.getString("addAuthorsToEntriesTitle"),false);
            // center window
            filterSearchDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(filterSearchDlg);
        // when we have any selected keywords, go on and add them all to all the selected
        // entries in the search result
        if (filterSearchDlg.getFilterTerms()!=null) {
            // get all selected entries
            int[] entries = getSelectedEntriesFromTable();
            // go through all selected entries
            for (int e : entries) {
                // now iterate the chosen authors
                // and add each author to all selected entries
                for (String a : filterSearchDlg.getFilterTerms()) dataObj.addAuthorToEntry(a, e, 1);
            }
            // author-list is not up-to-date
            dataObj.setAuthorlistUpToDate(false);
            // update the display
            updateDisplay();
        }
        // dispose window...
        filterSearchDlg.dispose();
        filterSearchDlg = null;
    }


    /**
     * This method inits the combo-boxes, i.e. filling it with search-result-entries
     * and setting up an action listener. The action-listener will update the jTableResults
     * with the search-result-entrynumbers and update the display (filling the textfields).
     */
    private void initComboBox() {
        // clear combobox
        jComboBoxSearches.removeAllItems();

        for (int cnt=0; cnt<searchrequest.getCount(); cnt++) {
            jComboBoxSearches.addItem(searchrequest.getShortDescription(cnt));
        }
        // add action listener to combo box
        jComboBoxSearches.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                // set all results, i.e. all entry-numbers and the entries' titles, into the search result table
                prepareResultList(jComboBoxSearches.getSelectedIndex());
                // and update the display, i.e. show the entry's content
                updateDisplay();
                // finally, select first entry
                try {
                    jTableResults.setRowSelectionInterval(0, 0);
                }
                catch (IllegalArgumentException e) {
                    Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
                }
                // set inputfocus to the table, so key-navigation can start immediately
                jTableResults.requestFocusInWindow();
            }
        });
        try {
            // select first item
            jComboBoxSearches.setSelectedIndex(0);
        }
        catch (IllegalArgumentException ex) {
            // log error
            Constants.zknlogger.log(Level.SEVERE,ex.getLocalizedMessage());
        }
    }


    /**
     * This method initializes the table.<br><br>
     * - it puts the tab-key as new traversal-key<br>
     * - sets the autosorter<br>
     * - displayes the cellgrid<br>
     * - implements action- and selection-listeners
     */
    private void initTable() {
        // usually, the tab key selects the next cell in a jTable. here we override this
        // setting, changing the tab-key to change the focus.

        // bind our new forward focus traversal keys
        Set<AWTKeyStroke> newForwardKeys = new HashSet<>(1);
        newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,0));
        jTableResults.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,Collections.unmodifiableSet(newForwardKeys));
        // bind our new backward focus traversal keys
        Set<AWTKeyStroke> newBackwardKeys = new HashSet<>(1);
        newBackwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,KeyEvent.SHIFT_MASK+KeyEvent.SHIFT_DOWN_MASK));
        jTableResults.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,Collections.unmodifiableSet(newBackwardKeys));
        // create new table sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        // tell tgis jtable that it has an own sorter
        jTableResults.setRowSorter(sorter);
        // and tell the sorter, which table model to sort.
        sorter.setModel((DefaultTableModel)jTableResults.getModel());
        // in this table, the first column needs a custom comparator.
        try {
            // sorter for titles
            sorter.setComparator(1,new Comparer());
            // sorter for desktop names
            sorter.setComparator(5,new Comparer());
            // this table has two more columns that should be sorted, the columns with
            // the entries timestamps.
            sorter.setComparator(2,new DateComparer());
            sorter.setComparator(3,new DateComparer());
        }
        catch (IndexOutOfBoundsException e) {
            Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
        }
        // get last table sorting
        RowSorter.SortKey sk = settingsObj.getTableSorting(jTableResults);
        // any sorting found?
        if (sk != null) {
            // create array with sort key
            ArrayList l = new ArrayList();
            l.add(sk);
            // set sort key to table
            sorter.setSortKeys(l);
            // sort table
            sorter.sort();
        }
        jTableResults.setShowHorizontalLines(settingsObj.getShowGridHorizontal());
        jTableResults.setShowVerticalLines(settingsObj.getShowGridVertical());
        jTableResults.setIntercellSpacing(settingsObj.getCellSpacing());
        jTableResults.getTableHeader().setReorderingAllowed(false);
        // if the user wants to see grids, we need to change the gridcolor on mac-aqua
        jTableResults.setGridColor(settingsObj.getTableGridColor());
        SelectionListener listener = new SelectionListener(jTableResults);
        jTableResults.getSelectionModel().addListSelectionListener(listener);
        jTableResults.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    
    /**
     * This method updates the combobox, when new search results are added or former
     * search requests are deleted. therefor, we have to temporarily remove the action listener,
     * because changing the combobox-content would fire several actions, which may interfer with
     * our updating-process
     * @param selectedrow here we can pass a table row that should be selected after updating the combo-box.
     * use "0" to select the first entry in the table, "-1" to select the last selection (if any) or any
     * other value.
     * @param searchnr the number of the searchrequest that should be displayed. use "-1" to show the default
     * search-request, which is either the currently used search-request, or - if it was deleted - the last search
     * request. use any other number for a specific search request.
     */
    public void updateComboBox(int selectedrow, int searchnr) {
        // init variable
        int selection;
        // check whether we have any parameter
        if (searchnr!=-1) selection = searchnr;
        // remember current selection for later use, see below
        else selection = jComboBoxSearches.getSelectedIndex();
        // used for tablerowselection
        int row;
        // if we have a parameter for row-selection, set it here
        if (selectedrow!=-1) row = selectedrow;
        // remember selected row...
        else row = jTableResults.getSelectedRow();
        // get all action listeners from the combo box
        ActionListener[] al = jComboBoxSearches.getActionListeners();
        // remove all action listeners so we don't fire several action-events
        // when we update the combo box. we can set the action listener later again
        for (ActionListener listener : al) jComboBoxSearches.removeActionListener(listener);
        // clear combobox
        jComboBoxSearches.removeAllItems();
        // add search descriptions to combobox
        for (int cnt=0; cnt<searchrequest.getCount(); cnt++) jComboBoxSearches.addItem(searchrequest.getShortDescription(cnt));
        // add action listener to combo box
        jComboBoxSearches.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                // set all results, i.e. all entry-numbers and the entries' titles, into the search result table
                prepareResultList(jComboBoxSearches.getSelectedIndex());
                // and update the display, i.e. show the entry's content
                updateDisplay();
                // set inputfocus to the table, so key-navigation can start immediately
                jTableResults.requestFocusInWindow();
                // finally, select first entry
                try {
                    jTableResults.setRowSelectionInterval(0, 0);
                }
                catch (IllegalArgumentException e) {
                    Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
                }
            }
        });
        // if we have any searchrequests at all, go on here
        if (searchrequest.getCount()>0) {
            // check whether the last selected searchrequest is still available
            // if not, choose the last search request in the combobox...
            if (selection!=searchrequest.getCurrentSearch()) selection = jComboBoxSearches.getItemCount()-1;
            // select search request
            jComboBoxSearches.setSelectedIndex(selection);
            // if we had no prevous selection, set row-selector to first item.
            if (-1==row) row = 0;
            // if the selected row was the last value, set row-counter to last row
            else if (row>=jTableResults.getRowCount()) row = jTableResults.getRowCount()-1;
            // finally...
            try {
                // select the appropriate table-entry
                jTableResults.setRowSelectionInterval(row, row);
                // and make sure it is visible...
                jTableResults.scrollRectToVisible(jTableResults.getCellRect(row,0,false));
            }
            catch (IllegalArgumentException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        // make window invisible
        else {
            setVisible(false);
            // and disable hotkey
            mainframe.setSearchResultsAvailable(false);
        }
    }


    /**
     * This method retrieves the result-entry-numbers from a search request "nr" and fills
     * the jTableResult with those entry-numbers and the entries' related titles.
     * 
     * @param searchrequestnr the search request of which we want to display the search results.
     */
    private void prepareResultList(int searchrequestnr) {
        // get search results
        int[] result = searchrequest.getSearchResults(searchrequestnr);
        // save current search request number
        searchrequest.setCurrentSearch(searchrequestnr);
        // check whether we have any results
        if (result!=null) {
            // tell selection listener to do nothing...
            tableUpdateActive = true;
            // sort the array with the entry-numbers of the search result
            if (result.length>0) Arrays.sort(result);
            // get the table model
            DefaultTableModel dtm = (DefaultTableModel)jTableResults.getModel();
            // clear table
            dtm.setRowCount(0);
            // iterate the result-array
            for (int cnt=0; cnt<result.length; cnt++) {
                // create a new object
                Object[] ob = new Object[6];
                // store the information in that object
                // first the entry number
                ob[0] = result[cnt];
                // then the entry's title
                ob[1] = dataObj.getZettelTitle(result[cnt]);
                // get timestamp
                String[] timestamp = dataObj.getTimestamp(result[cnt]);
                // init timestamp variables.
                String created = "";
                String edited = "";
                // check whether we have any timestamp at all.
                if (timestamp!=null && !timestamp[0].isEmpty() && timestamp[0].length()>=6) created = timestamp[0].substring(4,6)+"."+timestamp[0].substring(2,4)+".20"+timestamp[0].substring(0,2);
                // check whether we have any timestamp at all.
                if (timestamp!=null && !timestamp[1].isEmpty() && timestamp[1].length()>=6) edited = timestamp[1].substring(4,6)+"."+timestamp[1].substring(2,4)+".20"+timestamp[1].substring(0,2);
                ob[2] = created;
                ob[3] = edited;
                // now, the entry's rating
                ob[4] = dataObj.getZettelRating(result[cnt]);
                // finally, check whether entry is on any desktop, and if so,
                // use desktop name in that column
                ob[5] = desktopObj.getDesktopNameOfEntry(result[cnt]);
                // and add that content as a new row to the table
                dtm.addRow(ob);
            }
            // create a new stringbuilder to prepare the label 
            // that shows the amount of found entries
            StringBuilder sb = new StringBuilder("");
            sb.append("(");
            sb.append(String.valueOf(dtm.getRowCount()));
            sb.append(" ");
            sb.append(resourceMap.getString("hitsText"));
            sb.append(")");
            // set labeltext
            jLabelHits.setText(sb.toString());
            // work done
            tableUpdateActive = false;
            // enable filter text field
            jTextFieldFilterList.setEnabled(true);
        }
    }
    
    
    /**
     * This method updates the display, i.e. it retrieves the selected entry from
     * the jTableResults and fills the textfields with content (displaying the entry).
     */
    private void updateDisplay() {
        // get selected row
        int row = jTableResults.getSelectedRow();
        // if we have any selections, go on
        if (row!=-1) {
            // retrieve the value...
            Object o = jTableResults.getValueAt(row, 0);
            try {
                // ...and try to convert it to an integer value
                int selection = Integer.parseInt(o.toString());
                // prepare array for search terms which might be highlighted
                String[] sts = getHighlightSearchterms();
                displayZettelContent(selection,sts);
                //
                // Here we set up the keywordlist for the JList
                //
                // retrieve the keywords of the selected entry
                String[] kws = dataObj.getKeywords(selection);
                // prepare the JList which will display the keywords
                keywordListModel.clear();
                // check whether any keywords have been found
                if (kws!=null) {
                    // sort the array
                    if (kws.length>0) Arrays.sort(kws);
                    // iterate the string array and add its content to the list model
                    for (String kw : kws ) keywordListModel.addElement(kw);
                }
                // if we have any search terms, we want to select the related keywords...
                if (sts!=null) {
                    // create an integer list
                    LinkedList<Integer> l = new LinkedList<>();
                    // iterate all search terms
                    for (String s : sts) {
                        // try to find the keyword in the jList
                        for (int cnt=0; cnt<keywordListModel.getSize(); cnt++) if (s.equalsIgnoreCase(keywordListModel.get(cnt).toString())) l.add(cnt);
                    }
                    // create int-array
                    int [] selections = new int[l.size()];
                    // copy all elements of the list to the array
                    for (int cnt=0; cnt<l.size(); cnt++) selections[cnt] = l.get(cnt);
                    // set selected indices for the jList
                    jListKeywords.setSelectedIndices(selections);
                }
                // if we don't have highlighting, clear selection
                else jListKeywords.clearSelection();
                // if we want to update the entry immediately, show entry in mainframe as well
                if (settingsObj.getShowSearchEntry()) mainframe.showEntry(selection);
                // finally, set desktop selected
                // setDesktopEntrySelected(desktopObj.isEntryInAnyDesktop(selection));
            }
            catch (NumberFormatException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
        else {
            jEditorPaneSearchEntry.setText("");
            keywordListModel.clear();
        }
    }

    
    public void updateDisplayAfterEditing() {
        // get selected row
        int row = jTableResults.getSelectedRow();
        // if we have any selections, go on
        if (row!=-1) {
            // retrieve the value...
            Object o = jTableResults.getValueAt(row, 0);
            try {
                // ...and try to convert it to an integer value
                int selection = Integer.parseInt(o.toString());
                // prepare array for search terms which might be highlighted
                String[] sts = getHighlightSearchterms();
                displayZettelContent(selection,sts);
            }
            catch (NumberFormatException e) {
                Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
            }
        }
    }
    
    
    private String[] getHighlightSearchterms() {
        // prepare array for search terms which might be highlighted
        String[] sts = null;
        // get search terms, if highlighting is requested
        if (settingsObj.getHighlightSearchResults()) {
            // get the selected index, i.e. the searchrequest we want to retrieve
            int index = jComboBoxSearches.getSelectedIndex();
            // get the related search terms
            sts = searchrequest.getSearchTerms(index);
            // check whether the search was a synonym-search. if yes, add synonyms to search terms
            if (searchrequest.isSynonymSearch(index)) {
                // create new linked list that will contain all highlight-terms, including
                // the related synonyms of the highlight-terms
                LinkedList<String> highlight = new LinkedList<>();
                // go through all searchterms
                for (String s : sts) {
                    // get the synonym-line for each search term
                    String[] synline = synonymsObj.getSynonymLineFromAny(s,false);
                    // if we have synonyms...
                    if (synline!=null) {
                        // add them to the linked list, if they are new
                        for (String sy : synline) {
                            if (!highlight.contains(sy)) highlight.add(sy);
                        }
                    }
                    // else simply add the search term to the linked list
                    else if (!highlight.contains(s)) {
                        highlight.add(s);
                    }
                }
                if (highlight.size()>0) sts = highlight.toArray(new String[highlight.size()]);
            }
        }
        return sts;
    }
    
    
    private void displayZettelContent(int nr, String[] highlightterms) {
        // set highlight search terms
        HtmlUbbUtil.setHighlighTerms(highlightterms, HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS, settingsObj.getHighlightWholeWordSearch());
        // retrieve the string array of the first entry
        String disp = dataObj.getEntryAsHtml(nr, (settingsObj.getHighlightSegments())?getSelectedKeywordsFromList():null, Constants.FRAME_SEARCH);
        // in case parsing was ok, display the entry
        if (Tools.isValidHTML(disp,nr)) {
            // set entry information in the main textfield
            jEditorPaneSearchEntry.setText(disp);
        }
        // else show error message box to user and tell him what to do
        else {
            StringBuilder cleanedContent = new StringBuilder("");
            cleanedContent.append("<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">");
            URL imgURL = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
            cleanedContent.append("<img border=\"0\" src=\"").append(imgURL).append("\">&#8195;");
            cleanedContent.append(resourceMap.getString("incorrectNestedTagsText"));
            cleanedContent.append("</div>").append(dataObj.getCleanZettelContent(nr)).append("</body>");
            // and display clean content instead
            jEditorPaneSearchEntry.setText(cleanedContent.toString());
        }
        // place caret, so content scrolls to top
        jEditorPaneSearchEntry.setCaretPosition(0);
    }


    @Action
    public void exportEntries() {
        // retrieve the selected index from the combobox, so we know the search result.
        // then get the related searchresults (entries as integer array) from the search-reuest
        // finally, call the mainframe's exportwindow-method and pass the int-array with the entry-numbers
        mainframe.exportEntries(searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex()));
    }


    @Action
    public void editEntry() {
        // get selected entry
        int row = jTableResults.getSelectedRow();
        // when we have a valid selection, go on
        if (row!=-1) {
            // remember that entry editing came from search window
            mainframe.editEntryFromSearchWindow = true;
            // open edit window
            mainframe.openEditor(true,Integer.parseInt(jTableResults.getValueAt(row, 0).toString()),false,false,-1);
        }
    }

    @Action
    public void duplicateEntry() {
        // get selected entry
        int row = jTableResults.getSelectedRow();
        // when we have a valid selection, go on
        if (row!=-1) mainframe.duplicateEntry(Integer.parseInt(jTableResults.getValueAt(row, 0).toString()));
    }

    @Action
    public void duplicateSearch() {
        searchrequest.duplicateSearchRequest();
        updateComboBox(0,-1);
    }

    @Action
    public void findAndReplace() {
        // find and replace within search-results-entries, and update display if we have any replacements.
        if (mainframe.replace(searchframe,null, getSelectedEntriesFromTable())) updateDisplay();
    }

    /**
     * This method gets all selected elements of the jListEntryKeywords
     * and returns them in an array.
     *
     * @return a string-array containing all selected keywords, or null if no selection made
     */
    private String[] getSelectedKeywordsFromList() {
        // get selected values
        List<String> values = jListKeywords.getSelectedValuesList();
        // if we have any selections, go on
        if (!values.isEmpty()) {
            // create string array for selected values
            // return complete array
            return values.toArray(new String[values.size()]);
        }
        // ...or null, if error occured.
        return null;
    }


    @Action(enabledProperty = "textSelected")
    public void newSearchFromSelection() {
        // open the search dialog
        // the parameters are as following:
        mainframe.startSearch(new String[] {jEditorPaneSearchEntry.getSelectedText()},        // string-array with search terms
                    Constants.SEARCH_AUTHOR,             // the type of search, i.e. where to look
                    Constants.LOG_OR,                   // the logical combination
                    false,                                 // whole-word-search
                    false,                                 // match-case-search
                    settingsObj.getSearchAlwaysSynonyms(),   // whether synonyms should be included or not
                    false,                                // time-period search
                    false,                                // whether the search terms contain regular expressions or not
                    "",                                   // timestamp, date from (period start)
                    "",                                   // timestamp, date to (period end)
                    0,                                    // timestampindex (whether the period should focus on creation or edited date, or both)
                    false,                                // no display - whether the results should only be used for adding entries to the desktop or so (true), or if a searchresults-window shoud be opened (false)
                    Constants.STARTSEARCH_USUAL,          // whether we have a usual search, or a search for entries without remarks or keywords and so on - see related method findEntryWithout
                    Constants.SEARCH_USUAL);
    }

    @Action(enabledProperty = "listSelected")
    public void newSearchFromKeywordsLogOr() {
        // open the search dialog
        // the parameters are as following:
        mainframe.startSearch(getSelectedKeywordsFromList(),        // string-array with search terms
                    Constants.SEARCH_KEYWORDS,             // the type of search, i.e. where to look
                    Constants.LOG_OR,                   // the logical combination
                    true,                                 // whole-word-search
                    true,                                 // match-case-search
                    settingsObj.getSearchAlwaysSynonyms(),   // whether synonyms should be included or not
                    false,                                // time-period search
                    false,                                // whether the search terms contain regular expressions or not
                    "",                                   // timestamp, date from (period start)
                    "",                                   // timestamp, date to (period end)
                    0,                                    // timestampindex (whether the period should focus on creation or edited date, or both)
                    false,                                // no display - whether the results should only be used for adding entries to the desktop or so (true), or if a searchresults-window shoud be opened (false)
                    Constants.STARTSEARCH_USUAL,          // whether we have a usual search, or a search for entries without remarks or keywords and so on - see related method findEntryWithout
                    Constants.SEARCH_USUAL);
    }

    @Action(enabledProperty = "listSelected")
    public void newSearchFromKeywordsLogAnd() {
        // open the search dialog
        // the parameters are as following:
        mainframe.startSearch(getSelectedKeywordsFromList(),        // string-array with search terms
                    Constants.SEARCH_KEYWORDS,             // the type of search, i.e. where to look
                    Constants.LOG_AND,                   // the logical combination
                    true,                                 // whole-word-search
                    true,                                 // match-case-search
                    settingsObj.getSearchAlwaysSynonyms(),   // whether synonyms should be included or not
                    false,                                // time-period search
                    false,                                // whether the search terms contain regular expressions or not
                    "",                                   // timestamp, date from (period start)
                    "",                                   // timestamp, date to (period end)
                    0,                                    // timestampindex (whether the period should focus on creation or edited date, or both)
                    false,                                // no display - whether the results should only be used for adding entries to the desktop or so (true), or if a searchresults-window shoud be opened (false)
                    Constants.STARTSEARCH_USUAL,          // whether we have a usual search, or a search for entries without remarks or keywords and so on - see related method findEntryWithout
                    Constants.SEARCH_USUAL);
    }

    @Action(enabledProperty = "listSelected")
    public void newSearchFromKeywordsLogNot() {
        // open the search dialog
        // the parameters are as following:
        mainframe.startSearch(getSelectedKeywordsFromList(),        // string-array with search terms
                    Constants.SEARCH_KEYWORDS,             // the type of search, i.e. where to look
                    Constants.LOG_NOT,                   // the logical combination
                    true,                                 // whole-word-search
                    true,                                 // match-case-search
                    settingsObj.getSearchAlwaysSynonyms(),   // whether synonyms should be included or not
                    false,                                // time-period search
                    false,                                // whether the search terms contain regular expressions or not
                    "",                                   // timestamp, date from (period start)
                    "",                                   // timestamp, date to (period end)
                    0,                                    // timestampindex (whether the period should focus on creation or edited date, or both)
                    false,                                // no display - whether the results should only be used for adding entries to the desktop or so (true), or if a searchresults-window shoud be opened (false)
                    Constants.STARTSEARCH_USUAL,          // whether we have a usual search, or a search for entries without remarks or keywords and so on - see related method findEntryWithout
                    Constants.SEARCH_USUAL);
    }


    /**
     * This method opens the usual find-dialog and lets the user enter a "new" search request. the current
     * search results are then filtered according to the search-parameters entered by the user. a new
     * searchresult is being displayed after that.
     * <br><br>
     * So the user can create a new search result with those previous entries removed that do not match
     * the search criteria.
     */
    @Action
    public void filterSearch() {
        // if dialog window isn't already created, do this now
        if (null == searchDlg) {
            // create a new dialog window
            searchDlg = new CSearchDlg(this,searchrequest,settingsObj,null);
            // center window
            searchDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(searchDlg);
        // open the search dialog
        // the parameters are as following:
        // - string-array with search results
        // - the type of search, i.e. where to look
        // - logical-and-combination
        // - whole words
        // - case-sensitive search
        if (!searchDlg.isCancelled()) {
            startSearch(Constants.SEARCH_USUAL,
                        searchDlg.getSearchTerms(),
                        searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex()),
                        searchDlg.getWhereToSearch(),
                        searchDlg.getLogical(),
                        searchDlg.isWholeWord(),
                        searchDlg.isMatchCase(),
                        searchDlg.isSynonymsIncluded(),
                        searchDlg.isRegExSearch(),
                        searchDlg.isTimestampSearch(),
                        searchDlg.getDateFromValue(),
                        searchDlg.getDateToValue(),
                        searchDlg.getTimestampIndex());
        }
        
        searchDlg.dispose();
        searchDlg = null;
    }
    

    /**
     * This method opens a dialog with a list that contains all keywords of the current search result's entries.
     * The user can than choose keywords from this list and filter the search results, i.e. creating a new
     * search result with those previous entries removed that do not match the search criteria (i.e.: don't
     * have the selected keywords).
     */
    @Action
    public void filterKeywords() {
        // retrieve current entries from the list
        int[] entries = searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex());
        // create linked list as parameter for filter-dialog
        LinkedList<String> keywords = new LinkedList<>();
        // go through all entries
        for (int e : entries) {
            // get keywords of each entries
            String[] kws = dataObj.getKeywords(e);
            // now go through all keywords of that entry
            // if keyword does not exist, add it to list
            if (kws!=null) for (String k : kws) if (!keywords.contains(k)) keywords.add(k);
        }
        // if dialog window isn't already created, do this now
        if (null == filterSearchDlg) {
            // create a new dialog window
            filterSearchDlg = new CFilterSearch(this,settingsObj,keywords,null,true);
            // center window
            filterSearchDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(filterSearchDlg);
        // open the search dialog
        // the parameters are as following:
        // - string-array with search results
        // - the type of search, i.e. where to look
        // - logical-and-combination
        // - whole words
        // - case-sensitive search
        if (filterSearchDlg.getFilterTerms()!=null) {
            startSearch(Constants.SEARCH_USUAL,
                        filterSearchDlg.getFilterTerms(),
                        searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex()),
                        Constants.SEARCH_KEYWORDS,
                        filterSearchDlg.getLogical(),
                        true, true,
                        settingsObj.getSearchAlwaysSynonyms(),
                        false,false,"","",0);
        }
        
        filterSearchDlg.dispose();
        filterSearchDlg = null;
    }
    

    @Action
    public void filterTopLevelLuhmann() {
        // open the search dialog
        // the parameters are as following:
        // - string-array with search results
        // - the type of search, i.e. where to look
        // - logical-and-combination
        // - whole words
        // - case-sensitive search
        startSearch(Constants.SEARCH_TOP_LEVEL_LUHMANN,
                    null,
                    searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex()),
                    -1, Constants.LOG_OR, false, false, false, false, false, null, null, 0);
    }
    

    /**
     * This method opens a dialog with a list that contains all authors of the current search result's entries.
     * The user can than choose authors from this list and filter the search results, i.e. creating a new
     * search result with those previous entries removed that do not match the search criteria (i.e.: don't
     * have the selected authors).
     */
    @Action
    public void filterAuthors() {
        // retrieve current entries from the list
        int[] entries = searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex());
        // create linked list as parameter for filter-dialog
        LinkedList<String> authors = new LinkedList<>();
        // go through all entries
        for (int e : entries) {
            // get authors of each entries
            String[] aus = dataObj.getAuthors(e);
            // now go through all keywords of that entry
            // if keyword does not exist, add it to list
            if (aus!=null) for (String a : aus) if (!authors.contains(a)) authors.add(a);
        }
        // if dialog window isn't already created, do this now
        if (null == filterSearchDlg) {
            // create a new dialog window
            filterSearchDlg = new CFilterSearch(this,settingsObj,authors,null,true);
            // center window
            filterSearchDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(filterSearchDlg);
        // open the search dialog
        // the parameters are as following:
        // - string-array with search results
        // - the type of search, i.e. where to look
        // - logical-and-combination
        // - whole words
        // - case-sensitive search
        if (filterSearchDlg.getFilterTerms()!=null) {
            startSearch(Constants.SEARCH_USUAL,
                        filterSearchDlg.getFilterTerms(),
                        searchrequest.getSearchResults(jComboBoxSearches.getSelectedIndex()),
                        Constants.SEARCH_AUTHOR,
                        filterSearchDlg.getLogical(),
                        true,true,
                        settingsObj.getSearchAlwaysSynonyms(),
                        false,false,"","",0);
        }
        
        filterSearchDlg.dispose();
        filterSearchDlg = null;
    }
    

    /**
     * Opens the search dialog.
     * <br><br>
     * In case of keyword- and author-search <i>from the table</i> (lists), we can neglect the last
     * parameter, since keyword- and author-search simply functions by searching
     * for the index-numbers, that are always - or never - case sensitive relevant.
     * <br><br>
     * When we have searchterms from the search-dialog, the user also can search for <i>parts</i> inside
     * a keyword-string, so here the whole-word-parameter is relevant, since we then don't compare by index-
     * numbers, but by the string-value of the keywords/authors.
     * 
     * @param searchterms string-array with search terms
     * @param searchin the entries where the search should be apllied to, i.e. when we want to filter a certain search result
     * @param where the type of search, i.e. where to look, e.g. searching for keywords, authors, text etc.
     * @param logand logical-and-combination
     * @param wholeword whether we look for whole words or also parts of a word/phrase
     * @param matchcase whether the search should be case sensitive or not
     * @param synonyms whether the search should include synonyms or not
     * @param timesearch whether the user requested a time-search, i.e. a search for entries that were created
     * or changed within a certain period
     * @param datefrom the start of the period, when a timesearch is requested. format: "yymmdd".
     * @param dateto the end of the period, when a timesearch is requested. format: "yymmdd".
     * @param timestampindex
     */
    private void startSearch(int searchtype, String[] searchterms, int[] searchin, int where, int logical, boolean wholeword, boolean matchcase, boolean syno, boolean regex, boolean timesearch, String datefrom, String dateto, int timestampindex) {
        // check whether we have valid searchterms or not...
        if ((null==searchterms || searchterms.length<1) && searchtype!=Constants.SEARCH_TOP_LEVEL_LUHMANN) return;
        // if dialog window isn't already created, do this now
        if (null == taskDlg) {
            // get parent und init window
            taskDlg = new TaskProgressDialog(this, TaskProgressDialog.TASK_SEARCH, dataObj, searchrequest, synonymsObj,
                                             searchtype, searchterms, searchin, where, logical, wholeword, matchcase, syno,
                                             regex, timesearch, datefrom, dateto, timestampindex, false,
                                             settingsObj.getSearchRemovesFormatTags());
            // center window
            taskDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(taskDlg);
        // we have to manually dispose the window and release the memory
        // because next time this method is called, the showKwlDlg is still not null,
        // i.e. the constructor is not called (because the if-statement above is not true)
        // dispose the window and clear the object
        taskDlg.dispose();
        taskDlg = null;
        // check whether we have any search results at all
        if (searchrequest.getCurrentSearchResults()!=null) {
            showLatestSearchResult();
        }
        else {
            // display error message box that nothing was found
            JOptionPane.showMessageDialog(this,resourceMap.getString("errNothingFoundMsg"),resourceMap.getString("errNothingFoundTitle"),JOptionPane.PLAIN_MESSAGE);
        }
    }
    
    
    @Action
    public void showLongDesc() {
        // display long description
        JOptionPane.showMessageDialog(null,searchrequest.getLongDescription(jComboBoxSearches.getSelectedIndex()),resourceMap.getString("longDescTitle"),JOptionPane.PLAIN_MESSAGE);
    }
    
    
    @Action
    public void showHighlightSettings() {
        if (null == highlightSettingsDlg) {
            highlightSettingsDlg = new CHighlightSearchSettings(this,settingsObj,HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS);
            highlightSettingsDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(highlightSettingsDlg);
        highlightSettingsDlg.dispose();
        highlightSettingsDlg=null;
        
        updateDisplay();
    }
    
    
    /**
     * This method retrieves the selected entries and adds them to the deskop, by calling
     * the mainframe's method addToDesktop().
     */
    @Action
    public void addToDesktop() {
        // get selected entries
        int[] entries=getSelectedEntriesFromTable();
        // if we have any valid values, add them to desktop
        if ((entries!=null)&&(entries.length>0)) mainframe.addToDesktop(entries);
    }
    
    
    /**
     * This method retrieves the selected entries and adds them to the deskop, by calling
     * the mainframe's method addToDesktop().
     */
    @Action
    public void addToBookmarks() {
        // get selected entries
        int[] entries=getSelectedEntriesFromTable();
        // if we have any valid values...
        if ((entries!=null)&&(entries.length>0)) {
            // add them as bookmarks
            mainframe.addToBookmarks(entries, false);
            // and display related tab
            mainframe.menuShowBookmarks();
        }
    }
    

    /**
     * This method retrieves the selected entries and adds them as follower-numbers to that
     * entry that is selected in the mainframe's luhmann-tab, in the jTreeLuhmann.
     */
    @Action
    public void addToLuhmann() {
        // get selected entries
        int[] entries=getSelectedEntriesFromTable();
        // if we have any valid values...
        if ((entries!=null)&&(entries.length>0)) {
            // add them as followers
            mainframe.addToLuhmann(entries);
            // and display related tab
            mainframe.menuShowLuhmann();
        }
    }
    
    
    /**
     * This method retrieves the selected entries and adds them as manual link to the
     * mainframe's current entry.
     */
    @Action
    public void addToManLinks() {
        // get selected entries
        int[] entries=getSelectedEntriesFromTable();
        // if we have any valid values...
        if ((entries!=null)&&(entries.length>0)) {
            // add them as followers
            mainframe.addToManLinks(entries);
            // and display related tab
            mainframe.menuShowLinks();
        }
    }

    
    /**
     * Selects all entries in the table with the search results
     */
    @Action
    public void selectAll() {
        jTableResults.selectAll();
    }
    
    
    /**
     * This method gets all selected elements of the jTableResults
     * and returns them in an array.
     * 
     * @return a integer-array containing all selected entries, or null if no selection made
     */
    private int[] getSelectedEntriesFromTable() {
        // get selected rows
        int[] rows = jTableResults.getSelectedRows();
        // if we have any selections, go on
        if (rows!=null && rows.length>0) {
            // create string array for selected values
            int[] entries = new int[rows.length];
            try {
                // iterate array
                for (int cnt=0; cnt<rows.length; cnt++) {
                    // copy value from table to array
                    entries[cnt] = Integer.parseInt(jTableResults.getValueAt(rows[cnt], 0).toString());
                }
                // return complete array
                return entries;
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        // ...or null, if error occured.
        return null;
    }
    
    
    /**
     * This method removes the selected result-entry-numbers from the results list.
     */
    @Action
    public void removeEntry() {
        // get selected rows
        int[] rows = jTableResults.getSelectedRows();
        // if we have any selections, go on
        if ((rows!=null)&&(rows.length>0)) {
            // get the selected searchrequest
            int i = jComboBoxSearches.getSelectedIndex();
            for (int cnt=rows.length-1; cnt>=0; cnt--) {
                // retrieve the values...
                Object o = jTableResults.getValueAt(rows[cnt], 0);
                // ...and try to convert it to an integer value
                int selection = Integer.parseInt(o.toString());
                // delete the entry from the search request
                searchrequest.deleteResultEntry(i, selection);
            }
            updateComboBox(rows[0],-1);
        }
    }


    /**
     * This method deletes the selected entries completely from the dataset
     */
    @Action
    public void deleteEntryComplete() {
        // first display the to be deleted entry in the main-frame, so the user is not confused
        // about which entry to delete...
        displayEntryInMainframe();
        // try to delete the entry
        // and bring search results frame to front...
        if (mainframe.deleteEntries(getSelectedEntriesFromTable())) this.toFront();
    }
    
    
    /**
     * This method removes all(!) search requests, i.e. clears the search-request-xml-data.
     */
    @Action
    public void removeAllSearchResults() {
        // and create a JOptionPane with yes/no/cancel options
        int msgOption = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForDeleteAllMsg"), resourceMap.getString("askForDeleteAllTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // if the user wants to proceed, copy the image now
        if (JOptionPane.YES_OPTION == msgOption) {
            // completeley remove all search requests
            searchrequest.deleteAllSearchRequests();
            // reset combobox
            updateComboBox(-1,-1);
        }
    }


    private void displayEntryInMainframe() {
        // get selected entry
        int row = jTableResults.getSelectedRow();
        // when we have a valid selection, go on
        if (row!=-1) mainframe.showEntry(Integer.parseInt(jTableResults.getValueAt(row, 0).toString()));
    }

    
    /**
     * This method removes a complete search request from the search results.
     */
    @Action
    public void removeSearchResult() {
        // and create a JOptionPane with yes/no/cancel options
        int msgOption = JOptionPane.showConfirmDialog(null, resourceMap.getString("askForDeleteSearchMsg"), resourceMap.getString("askForDeleteSearchTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        // if the user wants to proceed, copy the image now
        if (JOptionPane.YES_OPTION == msgOption) {
            // get the selected searchrequest
            int i = jComboBoxSearches.getSelectedIndex();
            // delete complete search request
            searchrequest.deleteSearchRequest(i);
            // update combo box
            updateComboBox(0,-1);
        }
    }
    
    
    /**
     * Closes the window.
     */
    @Action
    public void closeWindow() {
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO,"Memory usage logged. Search Results Window closed.");
        }
        dispose();
        setVisible(false);
        // try to motivate garbage collector
        System.gc();
    }
    
    
    /**
     * Activates or deactivates the fullscreen-mode, thus switching between fullscreen and normal view.
     */
    @Action(enabledProperty = "fullScreenSupp")
    public void viewFullScreen() {
        // check whether fullscreen is possible or not...
        if (graphicdevice.isFullScreenSupported()) {
            // if we already have a fullscreen window, quit fullscreen
            if (graphicdevice.getFullScreenWindow()!=null) quitFullScreen();
            // else show fullscreen window
            else showFullScreen();
        }
    }

    
    /**
     * This method activates the fullscreen-mode, if it's not already activated yet. To have a 
     * fullscreen-window without decoration, the frame is disposed first, then the decoration
     * will be removed and the window made visible again.
     */
    private void showFullScreen() {
        // check whether fullscreen is supported, and if we currently have a fullscreen-window
        if (graphicdevice.isFullScreenSupported() && null==graphicdevice.getFullScreenWindow()) {
            // dispose frame, so we can remove the decoration when setting full screen mode
            searchframe.dispose();
            // hide menubar
            searchMenuBar.setVisible(false);
            // set frame non-resizable
            searchframe.setResizable(false);
            try {
                // remove decoration
                searchframe.setUndecorated(true);
            }
            catch (IllegalComponentStateException e) {
                Constants.zknlogger.log(Level.SEVERE,e.getLocalizedMessage());
            }
            // show frame again
            searchframe.setVisible(true);
            // set fullscreen mode to this window
            graphicdevice.setFullScreenWindow(this);
        }
    }
    
    
    /**
     * This method <i>de</i>activates the fullscreen-mode, if it's not already deactivated yet.
     */
    private void quitFullScreen() {
        // check whether fullscreen is supported, and if we currently have a fullscreen-window
        if (graphicdevice.isFullScreenSupported() && graphicdevice.getFullScreenWindow()!=null) {
            // disable fullscreen-mode
            graphicdevice.setFullScreenWindow(null);
            // hide menubar
            searchMenuBar.setVisible(true);
            // make frame resizable again
            searchframe.setResizable(true);
            // dispose frame, so we can restore the decoration
            searchframe.dispose();
            try {
                // set decoration
                searchframe.setUndecorated(false);
            }
            catch (IllegalComponentStateException e) {
                Constants.zknlogger.log(Level.SEVERE,e.getLocalizedMessage());
            }
            // show frame again
            searchframe.setVisible(true);
        }
    }
    
        
    /**
     * This method is used to pass paramaters to this dialog, so it can display results
     * when it is made visible. Since we don't dispose and clear this dialog, we cannot
     * call the constructor each time, so we need another method where we can pass parameters
     * of new search results.
     * <br><br>
     * This dialog is not disposed and cleared, because we want to keep former search results, even
     * when the user "closes" (i.e.: hides) this dialog.
     */
    public void showLatestSearchResult() {
        // here we update the combo box, not the display. since selecting
        // an item, which is done in this method, fires an action to the action listener,
        // the display update should be achieved through the combobox's actionlistener.
        updateComboBox(-1,searchrequest.getCount()-1);
        // and make dialog visible
        setVisible(true);
        // repaint the components (necessary, since the components are not properly repainted else)
        repaint();
        // set input focus
        SwingUtilities.invokeLater(() -> {
            this.setAlwaysOnTop(true);
            this.requestFocusInWindow();
            toFront();
            this.setAlwaysOnTop(false);
        });
    }


    private void openAttachment(javax.swing.event.HyperlinkEvent evt) {
        // retrieve the event type, e.g. if a link was clicked by the user
        HyperlinkEvent.EventType typ = evt.getEventType();
        // get the description, to check whether we have a file or a hyperlink to a website
        String linktype = evt.getDescription();
        // if the link was clicked, proceed
        if (typ==HyperlinkEvent.EventType.ACTIVATED) {
            // call method that handles the hyperlink-click
            String returnValue = Tools.openHyperlink(linktype, this, Constants.FRAME_SEARCH, dataObj, bibtexObj, settingsObj, jEditorPaneSearchEntry, Integer.parseInt(jTableResults.getValueAt(jTableResults.getSelectedRow(), 0).toString()));
            // check whether we have a return value. this might be the case either when the user clicked on
            // a footenote, or on the rating-stars
            if (returnValue!=null) {
                // here we have a reference to another entry
                if (returnValue.startsWith("#z_") || returnValue.startsWith("#cr_")) {
                    // show entry
                    mainframe.showEntry(dataObj.getCurrentZettelPos());
                }
                // edit cross references
                else if (returnValue.equalsIgnoreCase("#crt")) {
                    mainframe.editManualLinks();
                }
                // check whether a rating was requested
                else if (returnValue.startsWith("#rateentry")) {
                    try {
                        // retrieve entry-number
                        int entrynr = Integer.parseInt(linktype.substring(10));
                        // open rating-dialog
                        if (null == rateEntryDlg) {
                            rateEntryDlg = new CRateEntry(this,dataObj,entrynr);
                            rateEntryDlg.setLocationRelativeTo(this);
                        }
                        ZettelkastenApp.getApplication().show(rateEntryDlg);
                        // check whether dialog was cancelled or not
                        if (!rateEntryDlg.isCancelled()) {
                            // update display
                            displayZettelContent(entrynr, null);
                        }
                        rateEntryDlg.dispose();
                        rateEntryDlg=null;
                        // try to motivate garbage collector
                        System.gc();
                    }
                    catch (NumberFormatException ex) {
                        // log error
                        Constants.zknlogger.log(Level.WARNING,ex.getLocalizedMessage());
                        Constants.zknlogger.log(Level.WARNING, "Could not rate entry. Link-text was {0}", linktype);
                    }

                }
            }
        }
    }

    /**
     * This class sets up a selection listener for the tables. each table which shall react
     * on selections, e.g. by showing an entry, gets this selectionlistener in the method
     * {@link #initSelectionListeners() initSelectionListeners()}.
     */
    public class SelectionListener implements ListSelectionListener {
        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }
        @Override
        public void valueChanged(ListSelectionEvent e) {
            // if we have an update, don't react on selection changes
            if (tableUpdateActive) return;
            // get list selection model
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            // set value-adjusting to true, so we don't fire multiple value-changed events...
            lsm.setValueIsAdjusting(true);
            if (jTableResults==table) updateDisplay();
        }
    }


    /**
     * This variable indicates whether we have selected text or not - and en/disables
     * the new-search-functions. see {@link #newSearchFromAuthor() newSearchFromAuthor()}
     * and {@link #newSearchFromSelection() newSearchFromSelection()}.
     */
    private boolean textSelected = false;
    public boolean isTextSelected() {
        return textSelected;
    }
    public void setTextSelected(boolean b) {
        boolean old = isTextSelected();
        this.textSelected = b;
        firePropertyChange("textSelected", old, isTextSelected());
    }
    /**
     * This variable indicates whether we have selected an item in the jListKeywords or not - and en/disables
     * the new-search-functions. see {@link #newSearchFromKeywordsLogOr() newSearchFromKeywordsLogOr()}
     */
    private boolean listSelected = false;
    public boolean isListSelected() {
        return listSelected;
    }
    public void setListSelected(boolean b) {
        boolean old = isListSelected();
        this.listSelected = b;
        firePropertyChange("listSelected", old, isListSelected());
    }
    /**
     * This variable indicates whether we have selected an entry from the
     * search results list (jTableResults) that is also present on any
     * desktop.
     */
    private boolean desktopEntrySelected = false;
    public boolean isDesktopEntrySelected() {
        return desktopEntrySelected;
    }
    public void setDesktopEntrySelected(boolean b) {
        boolean old = isDesktopEntrySelected();
        this.desktopEntrySelected = b;
        firePropertyChange("desktopEntrySelected", old, isDesktopEntrySelected());
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchToolbar = new javax.swing.JToolBar();
        tb_copy = new javax.swing.JButton();
        tb_selectall = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        tb_editentry = new javax.swing.JButton();
        tb_remove = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        tb_manlinks = new javax.swing.JButton();
        tb_luhmann = new javax.swing.JButton();
        tb_bookmark = new javax.swing.JButton();
        tb_desktop = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        tb_highlight = new javax.swing.JButton();
        searchMainPanel = new javax.swing.JPanel();
        jSplitPaneSearch1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableResults = new javax.swing.JTable();
        jTextFieldFilterList = new javax.swing.JTextField();
        jButtonResetList = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jSplitPaneSearch2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPaneSearchEntry = new javax.swing.JEditorPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListKeywords = MacSourceList.createMacSourceList();
        searchStatusPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxSearches = new javax.swing.JComboBox();
        jLabelHits = new javax.swing.JLabel();
        jButtonDeleteSearch = new javax.swing.JButton();
        searchMenuBar = new javax.swing.JMenuBar();
        searchFileMenu = new javax.swing.JMenu();
        fileMenuLongDesc = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        fileMenuDuplicateSearch = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JSeparator();
        fileMenuDeleteSearch = new javax.swing.JMenuItem();
        fileMenuDeleteAll = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        fileMenuExport = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        fileMenuClose = new javax.swing.JMenuItem();
        searchEditMenu = new javax.swing.JMenu();
        editMenuCopy = new javax.swing.JMenuItem();
        editMenuSelectAll = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        editMenuDelete = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        editMenuEditEntry = new javax.swing.JMenuItem();
        editMenuDuplicateEntry = new javax.swing.JMenuItem();
        editMenuFindReplace = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        editMenuDeleteEntry = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JSeparator();
        editMenuAddKeywordsToSelection = new javax.swing.JMenuItem();
        editMenuAddAuthorsToSelection = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        editMenuManLinks = new javax.swing.JMenuItem();
        editMenuLuhmann = new javax.swing.JMenuItem();
        editMenuBookmarks = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        editMenuDesktop = new javax.swing.JMenuItem();
        searchFilterMenu = new javax.swing.JMenu();
        filterSearch = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        filterKeywords = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        filterAuthors = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        filterTopLevelLuhmann = new javax.swing.JMenuItem();
        searchSearchMenu = new javax.swing.JMenu();
        searchMenuSelectionContent = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        searchMenuKeywordLogOr = new javax.swing.JMenuItem();
        searchMenuKeywordLogAnd = new javax.swing.JMenuItem();
        searchMenuKeywordLogNot = new javax.swing.JMenuItem();
        searchViewMenu = new javax.swing.JMenu();
        viewMenuShowOnDesktop = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        viewMenuHighlight = new javax.swing.JCheckBoxMenuItem();
        viewMenuHighlightSettings = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        viewMenuShowEntry = new javax.swing.JCheckBoxMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSwitchLayout = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        viewMenuFullScreen = new javax.swing.JMenuItem();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getResourceMap(SearchResultsFrame.class);
        setTitle(resourceMap.getString("FormSearchResults.title")); // NOI18N
        setName("FormSearchResults"); // NOI18N

        searchToolbar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("searchToolbar.border.matteColor"))); // NOI18N
        searchToolbar.setFloatable(false);
        searchToolbar.setRollover(true);
        searchToolbar.setName("searchToolbar"); // NOI18N

        tb_copy.setAction(org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(SearchResultsFrame.class, this).get("copy"));
        tb_copy.setText(resourceMap.getString("tb_copy.text")); // NOI18N
        tb_copy.setBorderPainted(false);
        tb_copy.setFocusPainted(false);
        tb_copy.setFocusable(false);
        tb_copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_copy.setName("tb_copy"); // NOI18N
        tb_copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_copy);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ZettelkastenApp.class).getContext().getActionMap(SearchResultsFrame.class, this);
        tb_selectall.setAction(actionMap.get("selectAll")); // NOI18N
        tb_selectall.setText(resourceMap.getString("tb_selectall.text")); // NOI18N
        tb_selectall.setBorderPainted(false);
        tb_selectall.setFocusPainted(false);
        tb_selectall.setFocusable(false);
        tb_selectall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_selectall.setName("tb_selectall"); // NOI18N
        tb_selectall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_selectall);

        jSeparator12.setName("jSeparator12"); // NOI18N
        searchToolbar.add(jSeparator12);

        tb_editentry.setAction(actionMap.get("editEntry")); // NOI18N
        tb_editentry.setText(resourceMap.getString("tb_editentry.text")); // NOI18N
        tb_editentry.setBorderPainted(false);
        tb_editentry.setFocusPainted(false);
        tb_editentry.setFocusable(false);
        tb_editentry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_editentry.setName("tb_editentry"); // NOI18N
        tb_editentry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_editentry);

        tb_remove.setAction(actionMap.get("removeEntry")); // NOI18N
        tb_remove.setText(resourceMap.getString("tb_remove.text")); // NOI18N
        tb_remove.setBorderPainted(false);
        tb_remove.setFocusPainted(false);
        tb_remove.setFocusable(false);
        tb_remove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_remove.setName("tb_remove"); // NOI18N
        tb_remove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_remove);

        jSeparator3.setName("jSeparator3"); // NOI18N
        searchToolbar.add(jSeparator3);

        tb_manlinks.setAction(actionMap.get("addToManLinks")); // NOI18N
        tb_manlinks.setText(resourceMap.getString("tb_manlinks.text")); // NOI18N
        tb_manlinks.setBorderPainted(false);
        tb_manlinks.setFocusPainted(false);
        tb_manlinks.setFocusable(false);
        tb_manlinks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_manlinks.setName("tb_manlinks"); // NOI18N
        tb_manlinks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_manlinks);

        tb_luhmann.setAction(actionMap.get("addToLuhmann")); // NOI18N
        tb_luhmann.setText(resourceMap.getString("tb_luhmann.text")); // NOI18N
        tb_luhmann.setBorderPainted(false);
        tb_luhmann.setFocusPainted(false);
        tb_luhmann.setFocusable(false);
        tb_luhmann.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_luhmann.setName("tb_luhmann"); // NOI18N
        tb_luhmann.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_luhmann);

        tb_bookmark.setAction(actionMap.get("addToBookmarks")); // NOI18N
        tb_bookmark.setText(resourceMap.getString("tb_bookmark.text")); // NOI18N
        tb_bookmark.setBorderPainted(false);
        tb_bookmark.setFocusPainted(false);
        tb_bookmark.setFocusable(false);
        tb_bookmark.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_bookmark.setName("tb_bookmark"); // NOI18N
        tb_bookmark.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_bookmark);

        tb_desktop.setAction(actionMap.get("addToDesktop")); // NOI18N
        tb_desktop.setText(resourceMap.getString("tb_desktop.text")); // NOI18N
        tb_desktop.setBorderPainted(false);
        tb_desktop.setFocusPainted(false);
        tb_desktop.setFocusable(false);
        tb_desktop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_desktop.setName("tb_desktop"); // NOI18N
        tb_desktop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_desktop);

        jSeparator5.setName("jSeparator5"); // NOI18N
        searchToolbar.add(jSeparator5);

        tb_highlight.setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        tb_highlight.setText(resourceMap.getString("tb_highlight.text")); // NOI18N
        tb_highlight.setBorderPainted(false);
        tb_highlight.setFocusPainted(false);
        tb_highlight.setFocusable(false);
        tb_highlight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_highlight.setName("tb_highlight"); // NOI18N
        tb_highlight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_highlight);

        searchMainPanel.setName("searchMainPanel"); // NOI18N
        searchMainPanel.setLayout(new java.awt.BorderLayout());

        jSplitPaneSearch1.setBorder(null);
        jSplitPaneSearch1.setDividerLocation(240);
        jSplitPaneSearch1.setOrientation(settingsObj.getSearchFrameSplitLayout());
        jSplitPaneSearch1.setName("jSplitPaneSearch1"); // NOI18N
        jSplitPaneSearch1.setOneTouchExpandable(true);

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Zettel", "Überschrift", "Erstellt", "Geändert", "Bewertung", "Schreibtisch"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableResults.setDragEnabled(true);
        jTableResults.setName("jTableResults"); // NOI18N
        jTableResults.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTableResults.setShowVerticalLines(false);
        jTableResults.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTableResults);
        if (jTableResults.getColumnModel().getColumnCount() > 0) {
            jTableResults.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title0")); // NOI18N
            jTableResults.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title1")); // NOI18N
            jTableResults.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title2")); // NOI18N
            jTableResults.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title3")); // NOI18N
            jTableResults.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title4")); // NOI18N
            jTableResults.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title5")); // NOI18N
        }

        jTextFieldFilterList.setName("jTextFieldFilterList"); // NOI18N

        jButtonResetList.setAction(actionMap.get("resetResultslist")); // NOI18N
        jButtonResetList.setIcon(resourceMap.getIcon("jButtonResetList.icon")); // NOI18N
        jButtonResetList.setBorderPainted(false);
        jButtonResetList.setContentAreaFilled(false);
        jButtonResetList.setFocusable(false);
        jButtonResetList.setName("jButtonResetList"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldFilterList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetList, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilterList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonResetList))
                .addGap(3, 3, 3))
        );

        jSplitPaneSearch1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jSplitPaneSearch2.setBorder(null);
        jSplitPaneSearch2.setDividerLocation(280);
        jSplitPaneSearch2.setName("jSplitPaneSearch2"); // NOI18N
        jSplitPaneSearch2.setOneTouchExpandable(true);

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jEditorPaneSearchEntry.setEditable(false);
        jEditorPaneSearchEntry.setBorder(null);
        jEditorPaneSearchEntry.setContentType(resourceMap.getString("jEditorPaneSearchEntry.contentType")); // NOI18N
        jEditorPaneSearchEntry.setName("jEditorPaneSearchEntry"); // NOI18N
        jScrollPane2.setViewportView(jEditorPaneSearchEntry);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
        );

        jSplitPaneSearch2.setLeftComponent(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jListKeywords.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListKeywords.border.title"))); // NOI18N
        jListKeywords.setModel(keywordListModel);
        jListKeywords.setName("jListKeywords"); // NOI18N
        jScrollPane4.setViewportView(jListKeywords);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
        );

        jSplitPaneSearch2.setRightComponent(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneSearch2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneSearch2)
        );

        jSplitPaneSearch1.setRightComponent(jPanel2);

        searchMainPanel.add(jSplitPaneSearch1, java.awt.BorderLayout.CENTER);

        searchStatusPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("searchStatusPanel.border.matteColor"))); // NOI18N
        searchStatusPanel.setMinimumSize(new java.awt.Dimension(200, 16));
        searchStatusPanel.setName("searchStatusPanel"); // NOI18N

        jPanel9.setName("jPanel9"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxSearches.setName("jComboBoxSearches"); // NOI18N

        jLabelHits.setText(resourceMap.getString("jLabelHits.text")); // NOI18N
        jLabelHits.setName("jLabelHits"); // NOI18N

        jButtonDeleteSearch.setAction(actionMap.get("removeSearchResult")); // NOI18N
        jButtonDeleteSearch.setIcon(resourceMap.getIcon("jButtonDeleteSearch.icon")); // NOI18N
        jButtonDeleteSearch.setText(resourceMap.getString("jButtonDeleteSearch.text")); // NOI18N
        jButtonDeleteSearch.setBorderPainted(false);
        jButtonDeleteSearch.setFocusPainted(false);
        jButtonDeleteSearch.setFocusable(false);
        jButtonDeleteSearch.setName("jButtonDeleteSearch"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelHits)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxSearches, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDeleteSearch)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelHits)
                        .addComponent(jLabel1)
                        .addComponent(jComboBoxSearches, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout searchStatusPanelLayout = new javax.swing.GroupLayout(searchStatusPanel);
        searchStatusPanel.setLayout(searchStatusPanelLayout);
        searchStatusPanelLayout.setHorizontalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchStatusPanelLayout.setVerticalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        searchMenuBar.setName("searchMenuBar"); // NOI18N

        searchFileMenu.setText(resourceMap.getString("searchFileMenu.text")); // NOI18N
        searchFileMenu.setName("searchFileMenu"); // NOI18N

        fileMenuLongDesc.setAction(actionMap.get("showLongDesc")); // NOI18N
        fileMenuLongDesc.setName("fileMenuLongDesc"); // NOI18N
        searchFileMenu.add(fileMenuLongDesc);

        jSeparator2.setName("jSeparator2"); // NOI18N
        searchFileMenu.add(jSeparator2);

        fileMenuDuplicateSearch.setAction(actionMap.get("duplicateSearch")); // NOI18N
        fileMenuDuplicateSearch.setName("fileMenuDuplicateSearch"); // NOI18N
        searchFileMenu.add(fileMenuDuplicateSearch);

        jSeparator22.setName("jSeparator22"); // NOI18N
        searchFileMenu.add(jSeparator22);

        fileMenuDeleteSearch.setAction(actionMap.get("removeSearchResult")); // NOI18N
        fileMenuDeleteSearch.setName("fileMenuDeleteSearch"); // NOI18N
        searchFileMenu.add(fileMenuDeleteSearch);

        fileMenuDeleteAll.setAction(actionMap.get("removeAllSearchResults")); // NOI18N
        fileMenuDeleteAll.setName("fileMenuDeleteAll"); // NOI18N
        searchFileMenu.add(fileMenuDeleteAll);

        jSeparator20.setName("jSeparator20"); // NOI18N
        searchFileMenu.add(jSeparator20);

        fileMenuExport.setAction(actionMap.get("exportEntries")); // NOI18N
        fileMenuExport.setName("fileMenuExport"); // NOI18N
        searchFileMenu.add(fileMenuExport);

        jSeparator13.setName("jSeparator13"); // NOI18N
        searchFileMenu.add(jSeparator13);

        fileMenuClose.setAction(actionMap.get("closeWindow")); // NOI18N
        fileMenuClose.setName("fileMenuClose"); // NOI18N
        searchFileMenu.add(fileMenuClose);

        searchMenuBar.add(searchFileMenu);

        searchEditMenu.setText(resourceMap.getString("searchEditMenu.text")); // NOI18N
        searchEditMenu.setName("searchEditMenu"); // NOI18N

        editMenuCopy.setAction(actionMap.get("copy"));
        editMenuCopy.setName("editMenuCopy"); // NOI18N
        searchEditMenu.add(editMenuCopy);

        editMenuSelectAll.setAction(actionMap.get("selectAll")); // NOI18N
        editMenuSelectAll.setName("editMenuSelectAll"); // NOI18N
        searchEditMenu.add(editMenuSelectAll);

        jSeparator10.setName("jSeparator10"); // NOI18N
        searchEditMenu.add(jSeparator10);

        editMenuDelete.setAction(actionMap.get("removeEntry")); // NOI18N
        editMenuDelete.setName("editMenuDelete"); // NOI18N
        searchEditMenu.add(editMenuDelete);

        jSeparator16.setName("jSeparator16"); // NOI18N
        searchEditMenu.add(jSeparator16);

        editMenuEditEntry.setAction(actionMap.get("editEntry")); // NOI18N
        editMenuEditEntry.setName("editMenuEditEntry"); // NOI18N
        searchEditMenu.add(editMenuEditEntry);

        editMenuDuplicateEntry.setAction(actionMap.get("duplicateEntry")); // NOI18N
        editMenuDuplicateEntry.setName("editMenuDuplicateEntry"); // NOI18N
        searchEditMenu.add(editMenuDuplicateEntry);

        editMenuFindReplace.setAction(actionMap.get("findAndReplace")); // NOI18N
        editMenuFindReplace.setName("editMenuFindReplace"); // NOI18N
        searchEditMenu.add(editMenuFindReplace);

        jSeparator4.setName("jSeparator4"); // NOI18N
        searchEditMenu.add(jSeparator4);

        editMenuDeleteEntry.setAction(actionMap.get("deleteEntryComplete")); // NOI18N
        editMenuDeleteEntry.setName("editMenuDeleteEntry"); // NOI18N
        searchEditMenu.add(editMenuDeleteEntry);

        jSeparator21.setName("jSeparator21"); // NOI18N
        searchEditMenu.add(jSeparator21);

        editMenuAddKeywordsToSelection.setAction(actionMap.get("addKeywordsToEntries")); // NOI18N
        editMenuAddKeywordsToSelection.setName("editMenuAddKeywordsToSelection"); // NOI18N
        searchEditMenu.add(editMenuAddKeywordsToSelection);

        editMenuAddAuthorsToSelection.setAction(actionMap.get("addAuthorsToEntries")); // NOI18N
        editMenuAddAuthorsToSelection.setName("editMenuAddAuthorsToSelection"); // NOI18N
        searchEditMenu.add(editMenuAddAuthorsToSelection);

        jSeparator1.setName("jSeparator1"); // NOI18N
        searchEditMenu.add(jSeparator1);

        editMenuManLinks.setAction(actionMap.get("addToManLinks")); // NOI18N
        editMenuManLinks.setName("editMenuManLinks"); // NOI18N
        searchEditMenu.add(editMenuManLinks);

        editMenuLuhmann.setAction(actionMap.get("addToLuhmann")); // NOI18N
        editMenuLuhmann.setName("editMenuLuhmann"); // NOI18N
        searchEditMenu.add(editMenuLuhmann);

        editMenuBookmarks.setAction(actionMap.get("addToBookmarks")); // NOI18N
        editMenuBookmarks.setName("editMenuBookmarks"); // NOI18N
        searchEditMenu.add(editMenuBookmarks);

        jSeparator6.setName("jSeparator6"); // NOI18N
        searchEditMenu.add(jSeparator6);

        editMenuDesktop.setAction(actionMap.get("addToDesktop")); // NOI18N
        editMenuDesktop.setName("editMenuDesktop"); // NOI18N
        searchEditMenu.add(editMenuDesktop);

        searchMenuBar.add(searchEditMenu);

        searchFilterMenu.setText(resourceMap.getString("searchFilterMenu.text")); // NOI18N
        searchFilterMenu.setName("searchFilterMenu"); // NOI18N

        filterSearch.setAction(actionMap.get("filterSearch")); // NOI18N
        filterSearch.setName("filterSearch"); // NOI18N
        searchFilterMenu.add(filterSearch);

        jSeparator14.setName("jSeparator14"); // NOI18N
        searchFilterMenu.add(jSeparator14);

        filterKeywords.setAction(actionMap.get("filterKeywords")); // NOI18N
        filterKeywords.setName("filterKeywords"); // NOI18N
        searchFilterMenu.add(filterKeywords);

        jSeparator15.setName("jSeparator15"); // NOI18N
        searchFilterMenu.add(jSeparator15);

        filterAuthors.setAction(actionMap.get("filterAuthors")); // NOI18N
        filterAuthors.setName("filterAuthors"); // NOI18N
        searchFilterMenu.add(filterAuthors);

        jSeparator23.setName("jSeparator23"); // NOI18N
        searchFilterMenu.add(jSeparator23);

        filterTopLevelLuhmann.setAction(actionMap.get("filterTopLevelLuhmann")); // NOI18N
        filterTopLevelLuhmann.setName("filterTopLevelLuhmann"); // NOI18N
        searchFilterMenu.add(filterTopLevelLuhmann);

        searchMenuBar.add(searchFilterMenu);

        searchSearchMenu.setText(resourceMap.getString("searchSearchMenu.text")); // NOI18N
        searchSearchMenu.setName("searchSearchMenu"); // NOI18N

        searchMenuSelectionContent.setAction(actionMap.get("newSearchFromSelection")); // NOI18N
        searchMenuSelectionContent.setName("searchMenuSelectionContent"); // NOI18N
        searchSearchMenu.add(searchMenuSelectionContent);

        jSeparator19.setName("jSeparator19"); // NOI18N
        searchSearchMenu.add(jSeparator19);

        searchMenuKeywordLogOr.setAction(actionMap.get("newSearchFromKeywordsLogOr")); // NOI18N
        searchMenuKeywordLogOr.setName("searchMenuKeywordLogOr"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogOr);

        searchMenuKeywordLogAnd.setAction(actionMap.get("newSearchFromKeywordsLogAnd")); // NOI18N
        searchMenuKeywordLogAnd.setName("searchMenuKeywordLogAnd"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogAnd);

        searchMenuKeywordLogNot.setAction(actionMap.get("newSearchFromKeywordsLogNot")); // NOI18N
        searchMenuKeywordLogNot.setName("searchMenuKeywordLogNot"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogNot);

        searchMenuBar.add(searchSearchMenu);

        searchViewMenu.setText(resourceMap.getString("searchViewMenu.text")); // NOI18N
        searchViewMenu.setName("searchViewMenu"); // NOI18N

        viewMenuShowOnDesktop.setAction(actionMap.get("showEntryInDesktop")); // NOI18N
        viewMenuShowOnDesktop.setName("viewMenuShowOnDesktop"); // NOI18N
        searchViewMenu.add(viewMenuShowOnDesktop);

        jSeparator11.setName("jSeparator11"); // NOI18N
        searchViewMenu.add(jSeparator11);

        viewMenuHighlight.setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        viewMenuHighlight.setSelected(true);
        viewMenuHighlight.setName("viewMenuHighlight"); // NOI18N
        searchViewMenu.add(viewMenuHighlight);

        viewMenuHighlightSettings.setAction(actionMap.get("showHighlightSettings")); // NOI18N
        viewMenuHighlightSettings.setName("viewMenuHighlightSettings"); // NOI18N
        searchViewMenu.add(viewMenuHighlightSettings);

        jSeparator9.setName("jSeparator9"); // NOI18N
        searchViewMenu.add(jSeparator9);

        viewMenuShowEntry.setAction(actionMap.get("showEntryImmediately")); // NOI18N
        viewMenuShowEntry.setSelected(true);
        viewMenuShowEntry.setName("viewMenuShowEntry"); // NOI18N
        searchViewMenu.add(viewMenuShowEntry);

        jSeparator7.setName("jSeparator7"); // NOI18N
        searchViewMenu.add(jSeparator7);

        jMenuItemSwitchLayout.setAction(actionMap.get("switchLayout")); // NOI18N
        jMenuItemSwitchLayout.setName("jMenuItemSwitchLayout"); // NOI18N
        searchViewMenu.add(jMenuItemSwitchLayout);

        jSeparator8.setName("jSeparator8"); // NOI18N
        searchViewMenu.add(jSeparator8);

        viewMenuFullScreen.setAction(actionMap.get("viewFullScreen")); // NOI18N
        viewMenuFullScreen.setName("viewMenuFullScreen"); // NOI18N
        searchViewMenu.add(viewMenuFullScreen);

        searchMenuBar.add(searchViewMenu);

        setJMenuBar(searchMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchMainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(searchStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(searchMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(searchStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    /**
     * This variable indicates whether or not the fullscreen mode is supportet
     * on the current system. if not, disable related icons...
     */
    private boolean fullScreenSupp = false;
    public boolean isFullScreenSupp() {
        return fullScreenSupp;
    }

    public void setFullScreenSupp(boolean b) {
        boolean old = isFullScreenSupp();
        this.fullScreenSupp = b;
        firePropertyChange("fullScreenSupp", old, isFullScreenSupp());
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem editMenuAddAuthorsToSelection;
    private javax.swing.JMenuItem editMenuAddKeywordsToSelection;
    private javax.swing.JMenuItem editMenuBookmarks;
    private javax.swing.JMenuItem editMenuCopy;
    private javax.swing.JMenuItem editMenuDelete;
    private javax.swing.JMenuItem editMenuDeleteEntry;
    private javax.swing.JMenuItem editMenuDesktop;
    private javax.swing.JMenuItem editMenuDuplicateEntry;
    private javax.swing.JMenuItem editMenuEditEntry;
    private javax.swing.JMenuItem editMenuFindReplace;
    private javax.swing.JMenuItem editMenuLuhmann;
    private javax.swing.JMenuItem editMenuManLinks;
    private javax.swing.JMenuItem editMenuSelectAll;
    private javax.swing.JMenuItem fileMenuClose;
    private javax.swing.JMenuItem fileMenuDeleteAll;
    private javax.swing.JMenuItem fileMenuDeleteSearch;
    private javax.swing.JMenuItem fileMenuDuplicateSearch;
    private javax.swing.JMenuItem fileMenuExport;
    private javax.swing.JMenuItem fileMenuLongDesc;
    private javax.swing.JMenuItem filterAuthors;
    private javax.swing.JMenuItem filterKeywords;
    private javax.swing.JMenuItem filterSearch;
    private javax.swing.JMenuItem filterTopLevelLuhmann;
    private javax.swing.JButton jButtonDeleteSearch;
    private javax.swing.JButton jButtonResetList;
    private javax.swing.JComboBox jComboBoxSearches;
    private javax.swing.JEditorPane jEditorPaneSearchEntry;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelHits;
    private javax.swing.JList jListKeywords;
    private javax.swing.JMenuItem jMenuItemSwitchLayout;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPaneSearch1;
    private javax.swing.JSplitPane jSplitPaneSearch2;
    private javax.swing.JTable jTableResults;
    private javax.swing.JTextField jTextFieldFilterList;
    private javax.swing.JMenu searchEditMenu;
    private javax.swing.JMenu searchFileMenu;
    private javax.swing.JMenu searchFilterMenu;
    private javax.swing.JPanel searchMainPanel;
    private javax.swing.JMenuBar searchMenuBar;
    private javax.swing.JMenuItem searchMenuKeywordLogAnd;
    private javax.swing.JMenuItem searchMenuKeywordLogNot;
    private javax.swing.JMenuItem searchMenuKeywordLogOr;
    private javax.swing.JMenuItem searchMenuSelectionContent;
    private javax.swing.JMenu searchSearchMenu;
    private javax.swing.JPanel searchStatusPanel;
    private javax.swing.JToolBar searchToolbar;
    private javax.swing.JMenu searchViewMenu;
    private javax.swing.JButton tb_bookmark;
    private javax.swing.JButton tb_copy;
    private javax.swing.JButton tb_desktop;
    private javax.swing.JButton tb_editentry;
    private javax.swing.JButton tb_highlight;
    private javax.swing.JButton tb_luhmann;
    private javax.swing.JButton tb_manlinks;
    private javax.swing.JButton tb_remove;
    private javax.swing.JButton tb_selectall;
    private javax.swing.JMenuItem viewMenuFullScreen;
    private javax.swing.JCheckBoxMenuItem viewMenuHighlight;
    private javax.swing.JMenuItem viewMenuHighlightSettings;
    private javax.swing.JCheckBoxMenuItem viewMenuShowEntry;
    private javax.swing.JMenuItem viewMenuShowOnDesktop;
    // End of variables declaration//GEN-END:variables
    
    private CHighlightSearchSettings highlightSettingsDlg;
    private CSearchDlg searchDlg;
    private TaskProgressDialog taskDlg;
    private CFilterSearch filterSearchDlg;
    private CRateEntry rateEntryDlg;
}
