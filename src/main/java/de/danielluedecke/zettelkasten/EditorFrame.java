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

import de.danielluedecke.zettelkasten.util.classes.InitStatusbarForTasks;
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.database.Settings;
import de.danielluedecke.zettelkasten.database.AutoKorrektur;
import de.danielluedecke.zettelkasten.database.AcceleratorKeys;
import de.danielluedecke.zettelkasten.database.StenoData;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.ImagePreview;
import de.danielluedecke.zettelkasten.util.classes.EntryStringTransferHandler;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import de.danielluedecke.zettelkasten.database.Daten;
import com.explodingpixels.macwidgets.BottomBar;
import com.explodingpixels.macwidgets.BottomBarSize;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import com.explodingpixels.widgets.WindowUtils;
import de.danielluedecke.zettelkasten.database.TasksData;
import de.danielluedecke.zettelkasten.mac.MacToolbarButton;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.FileOperationsUtil;
import de.danielluedecke.zettelkasten.util.ListUtil;
import de.danielluedecke.zettelkasten.util.NewEntryFrameUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.undo.UndoManager;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;
import org.jdom2.Element;

/**
 *
 * @author danielludecke
 */
public class EditorFrame extends javax.swing.JFrame implements WindowListener, DropTargetListener {

    /**
     * create a variable for a list model. this list model is used for the
     * JList-component which displays the keywords of the current entry.
     */
    private final DefaultListModel keywordListModel = new DefaultListModel();
    /**
     * create a variable for a list model. this list model is used for the
     * JList-component which displays the links of the current entry.
     */
    private DefaultListModel linkListModel = new DefaultListModel();
    /**
     * create a variable for a list model. this list model is used for the
     * JList-component which displays the quickinput-elements.
     */
    private final DefaultListModel quickInputKeywordsListModel = new DefaultListModel();
    /**
     * create a variable for a list model. this list model is used for the
     * JList-component which displays the quickinput-elements.
     */
    private final DefaultListModel quickInputAuthorListModel = new DefaultListModel();
    /**
     * CDaten object, which contains the XML data of the Zettelkasten
     */
    private final Daten dataObj;
    /**
     *
     */
    private final TasksData taskinfo;
    /**
     *
     */
    private final Synonyms synonymsObj;
    /**
     * CAccelerator object, which contains the XML data of the accelerator table
     * for the menus
     */
    private final AcceleratorKeys accKeys;
    /**
     *
     */
    private final Settings settingsObj;
    /**
     *
     */
    private final AutoKorrektur spellObj;
    /**
     *
     */
    private final StenoData stenoObj;
    /**
     * We need this List-Array to iterate the hyperlinks of an entry. Is needed
     * when an entry should be edited, and the JList with all the links should
     * be filled
     */
    private List<Element> hyperlinks;
    /**
     * state variable that indicated whether we have a new entry (false), or an
     * edit-action (true)
     */
    private static boolean editmode;

    /**
     * Determines whether the EditorFrame is used for a new entry or for
     * editing an existing entry.
     *
     * @param val {@code true} if we want to edit an existing entry,
     * {@code false} if a new entry is to be created
     */
    public void setEditMode(boolean val) {
        editmode = val;
    }

    /**
     * Determines whether a new entry is created or an existing entry is edited.
     *
     * @return {@code true} if an existing entry is currently edited,
     * {@code false} if a new entry is currently edited
     */
    public boolean isEditMode() {
        return editmode;
    }
    /**
     * state variable that indicated whether we have an inserted entry or not
     */
    public boolean luhmann;
    /**
     *
     */
    public boolean isDeleted;
    private Font lastSelectefFont;
    /**
     * This variable stores the entry number, if we have an entry which should
     * be edited
     */
    public int entryNumber;
    /**
     * Indicates whether the user made changes, i.e. edited data
     */
    private boolean modified;
    /**
     * inidcates whether the quickinput list for authors is up to date or not
     */
    private boolean authorListUpToDate = false;
    /**
     * inidcates whether the quickinput list for keywords is up to date or not
     */
    private boolean keywordsListUpToDate = false;
    /**
     * This variable indicates whether the task that creates the keyword-list is
     * already running
     */
    private boolean qiKeywordTaskIsRunning = false;
    /**
     * This variable indicates whether the task that creates the author-list is
     * already running
     */
    private boolean qiAuthorTaskIsRunning = false;
    /**
     *
     */
    private String[] selectedAuthors = null;
    /**
     * Indicated whether a list's content is changed, e.g. filtered. if so, we
     * have to tell this the selection listener which - otherwise - would be
     * called several times...
     */
    private boolean listUpdateActive = false;
    /**
     * An undo manager to undo/redo input from the main text field
     */
    private final UndoManager undomanager = new UndoManager();
    /**
     * These lists hold the keywords from the quick-input-action. if the
     * quick-input-settings is activated (see CSettings), we do not show the
     * whole keyword-list at once, but filter the list according to the
     * relevance of the keywords, divided into four steps.
     */
    private LinkedList<String> keywordStep1, displayedKeywordList;
    /**
     * This linked list holds the current selected keywords. we need this
     * especially for the first step, where the keywords of the second step base
     * on the selected keywords of the first step.
     */
    private LinkedList<String> selectedKeywords, remainingKeywords;
    /**
     * This varibale indicates in which step of the quickinput we are currently.
     */
    private int stepcounter;
    private int focusowner = Constants.FOCUS_UNKNOWN;
    private final int DROP_LOCATION_TEXTAREAENTRY = 1;
    private final int DROP_LOCATION_LISTATTACHMENTS = 2;
    private final ZettelkastenView mainframe;
    static DataFlavor urlFlavor;

    static {
        try {
            urlFlavor
                    = new DataFlavor("application/x-java-url; class=java.net.URL");
        } catch (ClassNotFoundException cnfe) {
            Constants.zknlogger.log(Level.WARNING, "Could not create URL Data Flavor!");
        }
    }
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(EditorFrame.class);
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap toolbarResourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(ToolbarIcons.class);

    /**
     * Creates new form CNewEntry. This Dialog is an edit-mask for creating new
     * entries or editing existing entries. Therefor, we pass the data-class as
     * parameter, so we can either retrieve information to fill automatically
     * the textfields (when editing an existing entry) or save the information
     * and add a new entry to the data-class.
     *
     * @param zkn
     * @param d
     * @param td
     * @param ak
     * @param s
     * @param ac
     * @param syn
     * @param stn
     * @param content
     * @param em
     * @param en
     * @param l
     * @param isdel
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public EditorFrame(ZettelkastenView zkn, Daten d, TasksData td, AcceleratorKeys ak, Settings s, AutoKorrektur ac, Synonyms syn, StenoData stn, String content, boolean em, int en, boolean l, boolean isdel) {
        mainframe = zkn;

        // init the variables from the parameters
        dataObj = d;
        taskinfo = td;
        stenoObj = stn;
        accKeys = ak;
        settingsObj = s;
        synonymsObj = syn;
        spellObj = ac;
        isDeleted = isdel;
        lastSelectefFont = new Font("Courier", Font.PLAIN, 12);
        editmode = em;
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. New Entry Window opened.");
        }
        // create brushed look for window, so toolbar and window-bar become a unit
        if (settingsObj.isMacAqua()) {
            MacUtils.makeWindowLeopardStyle(getRootPane());
            // WindowUtils.createAndInstallRepaintWindowFocusListener(this);
            WindowUtils.installJComponentRepainterOnWindowFocusChanged(this.getRootPane());
        }
        entryNumber = en;
        luhmann = l;
        keywordStep1 = selectedKeywords = displayedKeywordList = remainingKeywords = null;
        stepcounter = 1;
        // init locale for the default-actions cut/copy/paste
        Tools.initLocaleForDefaultActions(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(EditorFrame.class, this));
        initComponents();
        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // init all our document, window, and component-listeners
        initListeners();
        // set borders manually
        initBorders(settingsObj);
        // clear the listviews
        keywordListModel.clear();
        linkListModel.clear();
        quickInputKeywordsListModel.clear();
        quickInputAuthorListModel.clear();
        // show/hide quickkeyword-goon-button and quickinput-menu-items
        jButtonQuickKeyword.setVisible(settingsObj.getQuickInput());
        jCheckBoxQuickInput.setSelected(settingsObj.getQuickInput());
        initComboBox();
        // if we have mac os x with aqua/leopard-style make window look like native leopard
        if (settingsObj.isMacAqua()) {
            setupMacOSXLeopardStyle();
        }
        if (settingsObj.isSeaGlass()) {
            setupSeaGlassStyle();
        }
        // init default font-size for tables, lists and textfields...
        initDefaultFontSize();
        // disable add- and remove-buttons
        setKeywordSelected(false);
        setAttachmentSelected(false);
        // init the progress bar and status icon for
        // the swingworker background thread
        // creates a new class object. This variable is not used, it just associates task monitors to
        // the background tasks. furthermore, by doing this, this class object also animates the 
        // busy icon and the progress bar of this frame.
        InitStatusbarForTasks isb = new InitStatusbarForTasks(statusAnimationLabel, null, null);
        // init the accelerator table
        initAcceleratorTable();
        initActionMaps();
        initDragDropTransferHandler();
        // This method initialises the toolbar buttons. depending on the user-setting, we either
        // display small, medium or large icons as toolbar-icons.
        initToolbarIcons();
        // when we have an entry to edit, fill the textfields with content
        // else set probable selected text from entry as "pre-content"
        // the content of "content" is retrieved from text-selection from the main window.
        if (!editmode) {
            if (content != null) {
                jTextAreaEntry.setText(content);
                // if we have editmode, enable apply-button
                setTextfieldFilled(!content.isEmpty());
            }
        } else {
            initFields();
        }
        // scroll text to first line
        jTextAreaEntry.setCaretPosition(0);
        // set the input focus to the first textfield
        jTextFieldTitle.setCaretPosition(0);
        // reset modified-state...
        setModified(false);
        // reset the undomanager, in case it stored any changes
        // from the text field initiation when editing new entries
        undomanager.discardAllEdits();
        // update the toolbar and menu items
        updateToolbar(false);
        // and select the tab with the author list
        jTabbedPaneNewEntry1.setSelectedIndex(0);
        // and display authors.
        showAuthors();
    }

    private void initBorders(Settings settingsObj) {
        /*
         * Constructor for Matte Border
         * public MatteBorder(int top, int left, int bottom, int right, Color matteColor)
         */
        jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane3.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane4.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane5.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane6.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        jScrollPane7.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
        if (settingsObj.isSeaGlass()) {
            jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane4.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane5.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane6.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane7.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jSplitPaneNewEntry2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
            jSplitPaneNewEntry3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
            jSplitPaneNewEntry4.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            jTabbedPaneNewEntry1.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
            jPanel4.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jTextAreaEntry.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaEntry.border.title"), settingsObj));
            jTextAreaAuthor.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaAuthor.border.title"), settingsObj));
            jListKeywords.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jListKeywords.border.title"), settingsObj));
            jTextAreaRemarks.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaRemarks.border.title"), settingsObj));
            jListLinks.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jListLinks.border.title"), settingsObj));
        }
        if (settingsObj.isMacAqua()) {
            ZknMacWidgetFactory.updateSplitPane(jSplitPaneNewEntry1);
            ZknMacWidgetFactory.updateSplitPane(jSplitPaneNewEntry2);
            ZknMacWidgetFactory.updateSplitPane(jSplitPaneNewEntry3);
            ZknMacWidgetFactory.updateSplitPane(jSplitPaneNewEntry4);
            jScrollPane3.setBorder(null);
            jScrollPane4.setBorder(null);
            jScrollPane5.setBorder(null);
            jScrollPane6.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jScrollPane7.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
            jTextAreaEntry.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaEntry.border.title"), settingsObj));
            jTextAreaAuthor.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaAuthor.border.title"), settingsObj));
            jListKeywords.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jListKeywords.border.title"), ColorUtil.colorJTreeText, settingsObj));
            jTextAreaRemarks.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jTextAreaRemarks.border.title"), ColorUtil.colorJTreeText, settingsObj));
            jListLinks.setBorder(ZknMacWidgetFactory.getTitledBorder(resourceMap.getString("jListLinks.border.title"), ColorUtil.colorJTreeText, settingsObj));
            jTextFieldAddKeyword.setText(resourceMap.getString("textFieldDefaultText"));
            jTextFieldAddLink.setText(resourceMap.getString("textFieldDefaultText"));
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
        jComboBoxQuickInput.removeAllItems();
        // add items
        jComboBoxQuickInput.addItem(resourceMap.getString("quickInputNormal"));
        jComboBoxQuickInput.addItem(resourceMap.getString("quickInputMore"));
        jComboBoxQuickInput.addItem(resourceMap.getString("quickInputLess"));
        // select requested item
        jComboBoxQuickInput.setEnabled(settingsObj.getQuickInput());
        jComboBoxQuickInput.setSelectedIndex(settingsObj.getQuickInputExtended());
        // add action listener to combo box
        jComboBoxQuickInput.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // update selected quickinput-type
                settingsObj.setQuickInputExtended(jComboBoxQuickInput.getSelectedIndex());
                // indicate that keywordlist needs update
                keywordsListUpToDate = false;
                // if the keyword-tab is visible, refresh the view
                showKeywords();
            }
        });
    }

    private void initDragDropTransferHandler() {
        jListKeywords.setTransferHandler(new EntryStringTransferHandler() {
            @Override
            protected String exportString(JComponent c) {
                // retrieve selections
                List<String> kws = jListKeywords.getSelectedValuesList();
                // when we have no selection, return null
                if (kws.isEmpty()) {
                    return null;
                }
                StringBuilder keywords = new StringBuilder("");
                // iterate array and copy all selected keywords to clipboard
                for (String o : kws) {
                    keywords.append(o);
                    keywords.append(System.lineSeparator());
                }
                return keywords.toString();
            }

            @Override
            protected boolean importString(JComponent c, String str) {
                // check for valid drop-string
                if (str != null) {
                    // retrieve keywords
                    String[] kws = str.split("\n");
                    // if no keywords were dropped, leave
                    if (kws.length < 1) {
                        return false;
                    }
                    // iterate all dropped keywords
                    for (String kw : kws) {
                        // now check for double entries. returns false if keyword does not exist.
                        if (!kw.isEmpty() && !isDoubleKeywords(kw)) {
                            // add the text to the keyword-list (JList)
                            keywordListModel.addElement((String) kw);
                            // scroll jList down, so the new added keywords become visible
                            jListKeywords.scrollRectToVisible(jListKeywords.getCellBounds(keywordListModel.size() - 1, keywordListModel.size()));
                            // set the modified state
                            setModified(true);
                            // and clear the input-fields for further entries
                            jTextFieldAddKeyword.setText("");
                            jTextFieldAddKeyword.requestFocusInWindow();
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            protected void cleanup(JComponent c, boolean remove) {
            }
        });
        jListQuickInputKeywords.setTransferHandler(new EntryStringTransferHandler() {
            @Override
            protected String exportString(JComponent c) {
                // retrieve selected keyword-values
                List<String> kws = jListQuickInputKeywords.getSelectedValuesList();
                // if no selection made, return null
                if (kws.isEmpty()) {
                    return null;
                }
                // create string builder
                StringBuilder keywords = new StringBuilder("");
                // create a comma-separated string from string array
                for (String k : kws) {
                    keywords.append(k).append("\n");
                }
                // delete last newline-char
                if (keywords.length() > 1) {
                    keywords.setLength(keywords.length() - 1);
                }
                // return results
                return keywords.toString();
            }

            @Override
            protected boolean importString(JComponent c, String str) {
                return false;
            }

            @Override
            protected void cleanup(JComponent c, boolean remove) {
            }
        });
        jListQuickInputAuthor.setTransferHandler(new EntryStringTransferHandler() {
            @Override
            protected String exportString(JComponent c) {
                // retrieve selected keyword-values
                List<String> aus = jListQuickInputAuthor.getSelectedValuesList();
                // if no selection made, return null
                if (aus.isEmpty()) {
                    return null;
                }
                // create string builder
                StringBuilder authors = new StringBuilder("");
                // create a comma-separated string from string array
                for (String a : aus) {
                    authors.append(a).append("\n");
                }
                // delete last newline-char
                if (authors.length() > 1) {
                    authors.setLength(authors.length() - 1);
                }
                // return results
                return authors.toString();
            }

            @Override
            protected boolean importString(JComponent c, String str) {
                return false;
            }

            @Override
            protected void cleanup(JComponent c, boolean remove) {
            }
        });
        jTextAreaEntry.setDragEnabled(true);
        jListLinks.setDragEnabled(true);
        DropTarget dropTarget = new DropTarget(jTextAreaEntry, this);
        DropTarget listDropTarget = new DropTarget(jListLinks, this);
        /*
         jTextAreaAuthor.setTransferHandler(new MoveStringTransferHandler() {
         @Override protected String exportString(JComponent c) {
         String selectedText = jTextAreaAuthor.getSelectedText();
         // if no text selected, quit
         if (null==selectedText) return null;
         // return results
         return selectedText.toString().trim();
         }
         @Override protected boolean importString(JComponent c, String str) {
         // check for valid drop-string
         if (str!=null) {
         // each received string consists of two lines. the first one with information
         // about the drag-source and the drag-operation, the second one with the data
         // by this we can see whether we have received entries (i.e. a valid drop)
         String[] aus = str.split("\n");
         // iterate all dropped authors
         for (String au : aus) {
         // check whether author already exisrs in textfield
         if (!au.isEmpty() && !checkForDoubleAuthors(au)) {
         // if not, append author string
         // therefore, add a new line, but only if the textfield is not empty
         // (i.e. we already have an author)
         if (!jTextAreaAuthor.getText().isEmpty()) jTextAreaAuthor.append(System.lineSeparator());
         jTextAreaAuthor.append(au);
         // jTextAreaAuthor.insert(au, jTextAreaAuthor.getCaretPosition());
         // set the modified state
         setModified(true);
         }
         }
         return true;
         }
         return false;
         }
         @Override protected void cleanup(JComponent c, boolean remove) {
         if (remove) {
         jTextAreaAuthor.replaceSelection("");
         }
         }
         });
         */
    }

    private void initListeners() {
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        };
        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        ActionListener showMainFrameAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mainframe.bringToFront();
            }
        };
        getRootPane().registerKeyboardAction(showMainFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showSearchResultWindow"));
        ActionListener showSearchResultsAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mainframe.showSearchResultWindow();
            }
        };
        getRootPane().registerKeyboardAction(showSearchResultsAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        stroke = KeyStroke.getKeyStroke(accKeys.getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showDesktopWindow"));
        ActionListener showDesktopAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mainframe.showDesktopWindow();
            }
        };
        getRootPane().registerKeyboardAction(showDesktopAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        // init selection listeners for our lists...
        SelectionListener listlistener = new SelectionListener(jListQuickInputAuthor);
        jListQuickInputAuthor.getSelectionModel().addListSelectionListener(listlistener);
        // init selection listeners for our lists...
        listlistener = new SelectionListener(jListQuickInputKeywords);
        jListQuickInputKeywords.getSelectionModel().addListSelectionListener(listlistener);
        // init selection listeners for our lists...
        listlistener = new SelectionListener(jListKeywords);
        jListKeywords.getSelectionModel().addListSelectionListener(listlistener);
        // init selection listeners for our lists...
        listlistener = new SelectionListener(jListLinks);
        jListLinks.getSelectionModel().addListSelectionListener(listlistener);
        // if the document is ever edited, assume that it needs to be saved
        // so we add some document listeners here
        jTextFieldTitle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
                setRefreshKeywords();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
                setRefreshKeywords();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
                setRefreshKeywords();
            }
        });
        jTextAreaEntry.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
                updateUndoRedoButtons(true);
                setRefreshKeywords();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
                updateUndoRedoButtons(true);
                setRefreshKeywords();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
                updateUndoRedoButtons(true);
                setRefreshKeywords();
            }
        });
        jTextAreaAuthor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }
        });
        jTextAreaRemarks.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }
        });
        // add window listener, so we can open a confirm-exit-dialog if necessary
        addWindowListener(this);
        // add undomanager to the main textfield
        jTextAreaEntry.getDocument().addUndoableEditListener(undomanager);
        // keep the last 1000 actions for undoing
        undomanager.setLimit(1000);
        //
        // here we start with our component-listeners...
        //
        jTabbedPaneNewEntry1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                switch (jTabbedPaneNewEntry1.getSelectedIndex()) {
                    case 0:
                        showAuthors();
                        break;
                    case 1:
                        showKeywords();
                        break;
                    default:
                        showAuthors();
                        break;
                }
            }
        });
        //
        // here start the focus listener
        //
        jTextAreaEntry.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                focusowner = Constants.FOCUS_FIELD_TEXT;
                updateToolbar(true);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (!evt.isTemporary()) {
                    updateToolbar(false);
                }
            }
        });
        if (settingsObj.isMacAqua()) {
            jTextFieldAddKeyword.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    ZknMacWidgetFactory.setTextFieldBorder(jTextFieldAddKeyword);
                    updateTextFieldText(jTextFieldAddKeyword);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    ZknMacWidgetFactory.setTextFieldBorder(jTextFieldAddKeyword);
                    updateTextFieldText(jTextFieldAddKeyword);
                }
            });
            jTextFieldAddLink.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    ZknMacWidgetFactory.setTextFieldBorder(jTextFieldAddLink);
                    updateTextFieldText(jTextFieldAddLink);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    ZknMacWidgetFactory.setTextFieldBorder(jTextFieldAddLink);
                    updateTextFieldText(jTextFieldAddLink);
                }
            });
        }
        /*        
         jListQuickInputKeywords.addFocusListener(new java.awt.event.FocusAdapter() {
         @Override public void focusLost(java.awt.event.FocusEvent evt) {
         // retrieve opposite component
         Component oppComp = evt.getOppositeComponent();
         // check for valid valie
         if (oppComp!=null) {
         // retrieve component's name. in case we have the root (when popup is triggered)
         // the name is null
         String oppName = oppComp.getName();
         // check whether the focus went to the keyword-textfield
         if (oppName!=null && !oppName.equals("jTextFieldFilterKeywordlist")) {
         removeHighlights();
         }
         }
         }
         });
         jTextFieldFilterKeywordlist.addFocusListener(new java.awt.event.FocusAdapter() {
         @Override public void focusLost(java.awt.event.FocusEvent evt) {
         // retrieve opposite component
         Component oppComp = evt.getOppositeComponent();
         // check for valid valie
         if (oppComp!=null) {
         // retrieve component's name. in case we have the root (when popup is triggered)
         // the name is null
         String oppName = oppComp.getName();
         // check whether the focus went to the keyword-textfield
         if (oppName!=null && !oppName.equals("jListQuickInputKeywords")) {
         removeHighlights();
         }
         }
         }
         });
         */
        jTextAreaAuthor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                focusowner = Constants.FOCUS_FIELD_AUTHOR;
            }
        });
        jTextAreaRemarks.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                focusowner = Constants.FOCUS_FIELD_REMARKS;
            }
        });
        jTextFieldTitle.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                focusowner = Constants.FOCUS_FIELD_TITLE;
            }
        });
        jListQuickInputAuthor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                setAuthorSelected(jListQuickInputAuthor.getSelectedIndex() != -1);
            }
        });
        jListQuickInputAuthor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // this listener should only react on left-mouse-button-clicks...
                // if other button then left-button clicked, don't count it.
                if (evt.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (2 == evt.getClickCount()) {
                    addQuickAuthorToList();
                }
            }
        });
        jTextFieldTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_TITLE;
                    jPopupMenuCCP.show(jTextFieldTitle, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_TITLE;
                    jPopupMenuCCP.show(jTextFieldTitle, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextFieldAddKeyword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_ADDKEYWORDS;
                    jPopupMenuCCP.show(jTextFieldAddKeyword, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_ADDKEYWORDS;
                    jPopupMenuCCP.show(jTextFieldAddKeyword, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextFieldAddLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_ADDLINK;
                    jPopupMenuCCP.show(jTextFieldAddLink, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuCCP.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_ADDLINK;
                    jPopupMenuCCP.show(jTextFieldAddLink, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jListQuickInputKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuQuickKeywords.isVisible()) {
                    setSegmentPossible((jTextAreaEntry.getSelectedText() != null) && (jListQuickInputKeywords.getSelectedValue() != null));
                    focusowner = Constants.FOCUS_UNKNOWN;
                    jPopupMenuQuickKeywords.show(jListQuickInputKeywords, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuKeywords.isVisible()) {
                    setSegmentPossible((jTextAreaEntry.getSelectedText() != null) && (jListQuickInputKeywords.getSelectedValue() != null));
                    focusowner = Constants.FOCUS_FIELD_ADDKEYWORDS;
                    jPopupMenuKeywords.show(jListKeywords, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // this listener should only react on left-mouse-button-clicks...
                // if other button then left-button clicked, don't count it.
                if (evt.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                if (2 == evt.getClickCount()) {
                    addQuickKeywordToList();
                }
            }
        });
        jTextAreaEntry.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_TEXT;
                    jPopupMenuMain.show(jTextAreaEntry, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_TEXT;
                    jPopupMenuMain.show(jTextAreaEntry, evt.getPoint().x, evt.getPoint().y);
                }
                enableBySelection();
            }
        });
        jTextAreaEntry.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                NewEntryFrameUtil.checkSpelling(evt.getKeyCode(), jTextAreaEntry, settingsObj, spellObj);
                if (settingsObj.getAutoCompleteTags()) {
                    NewEntryFrameUtil.autoCompleteTags(jTextAreaEntry, evt.getKeyChar());
                }
                enableBySelection();
            }
        });
        jTextAreaAuthor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_AUTHOR;
                    jPopupMenuMain.show(jTextAreaAuthor, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_AUTHOR;
                    jPopupMenuMain.show(jTextAreaAuthor, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextAreaAuthor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                NewEntryFrameUtil.checkSpelling(evt.getKeyCode(), jTextAreaAuthor, settingsObj, spellObj);
            }
        });
        jTextAreaRemarks.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_REMARKS;
                    jPopupMenuMain.show(jTextAreaRemarks, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
                    focusowner = Constants.FOCUS_FIELD_REMARKS;
                    jPopupMenuMain.show(jTextAreaRemarks, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        jTextAreaRemarks.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                NewEntryFrameUtil.checkSpelling(evt.getKeyCode(), jTextAreaRemarks, settingsObj, spellObj);
            }
        });
        jTextFieldFilterAuthorlist.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    ListUtil.navigateThroughList(jListQuickInputAuthor, evt.getKeyCode());
                } else {
                    // select table-entry live, while the user is typing...
                    ListUtil.selectByTyping(jListQuickInputAuthor, jTextFieldFilterAuthorlist);
                }
            }
        });
        jTextFieldFilterKeywordlist.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (Tools.isNavigationKey(evt.getKeyCode())) {
                    // if user pressed navigation key, select next table entry
                    ListUtil.navigateThroughList(jListQuickInputKeywords, evt.getKeyCode());
                } else {
                    // select table-entry live, while the user is typing...
                    ListUtil.selectByTyping(jListQuickInputKeywords, jTextFieldFilterKeywordlist);
                }
            }
        });
        jListKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuKeywords.isVisible()) {
                    setSegmentPossible((jTextAreaEntry.getSelectedText() != null) && (jListKeywords.getSelectedValue() != null));
                    focusowner = Constants.FOCUS_FIELD_ADDKEYWORDS;
                    jPopupMenuKeywords.show(jListKeywords, evt.getPoint().x, evt.getPoint().y);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger() && !jPopupMenuKeywords.isVisible()) {
                    setSegmentPossible((jTextAreaEntry.getSelectedText() != null) && (jListKeywords.getSelectedValue() != null));
                    focusowner = Constants.FOCUS_FIELD_ADDKEYWORDS;
                    jPopupMenuKeywords.show(jListKeywords, evt.getPoint().x, evt.getPoint().y);
                }
            }
        });
        newEntryEditMenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                enableBySelection();
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        jPopupMenuMain.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                enableBySelection();
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
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
            jToolBarNewEntry.setVisible(false);
            // and set a border to the main panel, because the toolbar's dark border is hidden
            // and remove border from the main panel
            newEntryMainPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorUtil.colorDarkLineGray));
            return;
        }
        // set toolbar visible
        jToolBarNewEntry.setVisible(true);
        // and remove border from the main panel
        newEntryMainPanel.setBorder(null);
        // init toolbar button array
        javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[]{
            tb_cut, tb_copy, tb_paste, tb_selectall, tb_undo, tb_redo, tb_newauthor,
            tb_manlink, tb_footnote, tb_insertimage, tb_inserttable, tb_insertattachment,
            tb_bold, tb_italic, tb_underline, tb_strike, tb_textcolor, tb_highlight
        };
        String[] buttonNames = new String[]{"tb_cutText", "tb_copyText", "tb_pasteText",
            "tb_selectallText", "tb_undoText", "tb_redoText",
            "tb_newAuthorText", "tb_manlinkText", "tb_footnoteText", "tb_insertimageText",
            "tb_inserttableText", "tb_insertattachmentText", "tb_boldText",
            "tb_italicText", "tb_underlineText", "tb_strikeText",
            "tb_textcolorText", "tb_highlightText"
        };

        String[] iconNames = new String[]{"cutIcon", "copyIcon", "pasteIcon",
            "selectAllIcon", "undoIcon", "redoIcon",
            "newAuthorIcon", "insertManlinkIcon", "insertFootnoteIcon", "insertImageIcon",
            "insertTableIcon", "insertAttachmentIcon", "formatBoldIcon",
            "formatItalicIcon", "formatUnderlineIcon", "formatStrikeThroughIcon",
            "formatColorIcon", "highlightKeywordsIcon"
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
        // get the action map
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.
                getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().
                getActionMap(EditorFrame.class, this);
        // finally, we have to manuall init the actions for the popup-menu, since the gui-builder always
        // puts the menu-items before the line where the action-map is initialised. we cannot change
        // this because it is in the protected area, and when changing it from outside, it will
        // always be re-arranged by the gui-designer
        popupMainCut.setAction(actionMap.get("cut"));
        popupMainCopy.setAction(actionMap.get("copy"));
        popupMainPaste.setAction(actionMap.get("paste"));
        if (settingsObj.getShowIcons()) {
            tb_selectall.setVisible(settingsObj.getShowAllIcons());
            tb_newauthor.setVisible(settingsObj.getShowAllIcons());
            tb_highlight.setVisible(settingsObj.getShowAllIcons());
            tb_strike.setVisible(settingsObj.getShowAllIcons());
        }
        if (settingsObj.isMacAqua()) {
            makeMacToolbar();
        }
        if (settingsObj.isSeaGlass()) {
            makeSeaGlassToolbar();
        }
    }

    private void setupSeaGlassStyle() {
        getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
        componentVariants();
    }

    /**
     * This method applies some graphical stuff so the appearance of the program
     * is even more mac-like...
     */
    private void setupMacOSXLeopardStyle() {

        Color darkbackcol = ColorUtil.colorJTreeBackground;
        jPanel4.setBackground(darkbackcol);
        jPanel6.setBackground(darkbackcol);
        jListKeywords.setBackground(darkbackcol);
        jListLinks.setBackground(darkbackcol);
        jTextAreaRemarks.setBackground(darkbackcol);
        componentVariants();
    }

    private void componentVariants() {
        // some of the buttons will be designed specifically here...
        jButtonAddKeywords.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        jButtonQuickKeyword.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        jButtonAddKeywords.putClientProperty("JButton.segmentPosition", (settingsObj.getQuickInput()) ? "first" : "only");
        jButtonQuickKeyword.putClientProperty("JButton.segmentPosition", "last");
        // also change design of author-button
        jButtonAddAuthors.putClientProperty("JButton.buttonType", "roundRect");
        jTextFieldFilterKeywordlist.putClientProperty("JTextField.variant", "search");
        jTextFieldFilterAuthorlist.putClientProperty("JTextField.variant", "search");
        // change button size
        if (settingsObj.isSeaGlass()) {
            jButtonAddKeywords.putClientProperty("JComponent.sizeVariant", "small");
            jButtonQuickKeyword.putClientProperty("JComponent.sizeVariant", "small");
            jButtonAddAuthors.putClientProperty("JComponent.sizeVariant", "small");
            jButtonOK.putClientProperty("JComponent.sizeVariant", "small");
            jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        }
    }

    private void makeSeaGlassToolbar() {
        Tools.makeTexturedToolBarButton(tb_cut, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_paste, Tools.SEGMENT_POSITION_LAST);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_selectall, Tools.SEGMENT_POSITION_ONLY);
        }
        Tools.makeTexturedToolBarButton(tb_undo, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_redo, Tools.SEGMENT_POSITION_LAST);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_newauthor, Tools.SEGMENT_POSITION_ONLY);
        }
        Tools.makeTexturedToolBarButton(tb_footnote, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_manlink, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_insertimage, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_inserttable, Tools.SEGMENT_POSITION_MIDDLE);
        Tools.makeTexturedToolBarButton(tb_insertattachment, Tools.SEGMENT_POSITION_LAST);

        Tools.makeTexturedToolBarButton(tb_bold, Tools.SEGMENT_POSITION_FIRST);
        Tools.makeTexturedToolBarButton(tb_italic, Tools.SEGMENT_POSITION_MIDDLE);
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_underline, Tools.SEGMENT_POSITION_MIDDLE);
            Tools.makeTexturedToolBarButton(tb_strike, Tools.SEGMENT_POSITION_LAST);
        } else {
            Tools.makeTexturedToolBarButton(tb_underline, Tools.SEGMENT_POSITION_LAST);
        }
        if (settingsObj.getShowAllIcons()) {
            Tools.makeTexturedToolBarButton(tb_textcolor, Tools.SEGMENT_POSITION_FIRST);
            Tools.makeTexturedToolBarButton(tb_highlight, Tools.SEGMENT_POSITION_LAST);
        } else {
            Tools.makeTexturedToolBarButton(tb_textcolor, Tools.SEGMENT_POSITION_ONLY);
        }
        jToolBarNewEntry.setPreferredSize(new java.awt.Dimension(jToolBarNewEntry.getSize().width, Constants.seaGlassToolbarHeight));
        jToolBarNewEntry.add(new javax.swing.JToolBar.Separator(), 0);
    }

    private void makeMacToolbar() {
        // hide default toolbr
        jToolBarNewEntry.setVisible(false);
        this.remove(jToolBarNewEntry);
        // and create mac toolbar
        if (settingsObj.getShowIcons() || settingsObj.getShowIconText()) {

            UnifiedToolBar mactoolbar = new UnifiedToolBar();

            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_cut, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_copy, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_paste, MacToolbarButton.SEGMENT_POSITION_LAST));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            if (settingsObj.getShowAllIcons()) {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_selectall, MacToolbarButton.SEGMENT_POSITION_ONLY));
                mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            }
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_undo, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_redo, MacToolbarButton.SEGMENT_POSITION_LAST));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            if (settingsObj.getShowAllIcons()) {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_newauthor, MacToolbarButton.SEGMENT_POSITION_ONLY));
                mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            }
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_manlink, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_footnote, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_insertimage, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_inserttable, MacToolbarButton.SEGMENT_POSITION_LAST));
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_bold, MacToolbarButton.SEGMENT_POSITION_FIRST));
            mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_italic, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
            if (settingsObj.getShowAllIcons()) {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_underline, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_strike, MacToolbarButton.SEGMENT_POSITION_LAST));
            } else {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_underline, MacToolbarButton.SEGMENT_POSITION_LAST));
            }
            mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
            if (settingsObj.getShowAllIcons()) {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_textcolor, MacToolbarButton.SEGMENT_POSITION_FIRST));
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_highlight, MacToolbarButton.SEGMENT_POSITION_LAST));
            } else {
                mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_textcolor, MacToolbarButton.SEGMENT_POSITION_ONLY));
            }

            mactoolbar.installWindowDraggerOnWindow(this);
            newEntryMainPanel.add(mactoolbar.getComponent(), BorderLayout.PAGE_START);
        }
        makeMacBottomBar();
    }

    private void makeMacBottomBar() {
        jPanel1.setVisible(false);
        BottomBar macbottombar = new BottomBar(BottomBarSize.LARGE);
        macbottombar.addComponentToRight(jButtonCancel);
        macbottombar.addComponentToRight(jButtonOK, 4);
        macbottombar.addComponentToRight(statusAnimationLabel, 4);

        jButtonCancel.putClientProperty("JButton.buttonType", "textured");
        jButtonOK.putClientProperty("JButton.buttonType", "textured");
        jButtonCancel.putClientProperty("JComponent.sizeVariant", "small");
        jButtonOK.putClientProperty("JComponent.sizeVariant", "small");

        statusPanel.remove(jPanel1);
        statusPanel.setBorder(null);
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(macbottombar.getComponent(), BorderLayout.PAGE_START);
    }

    /**
     * This method sets the default font-size for tables, lists and treeviews.
     * If the user wants to have bigger font-sizes for better viewing, the new
     * font-size will be applied to the components here.
     */
    private void initDefaultFontSize() {
        // set default fonts
        Font f = settingsObj.getMainFont();
        f = new Font(f.getName(), f.getStyle(), f.getSize() + 4);
        jTextAreaEntry.setFont(f);
        f = settingsObj.getAuthorFont();
        f = new Font(f.getName(), f.getStyle(), f.getSize() + 4);
        jTextAreaAuthor.setFont(f);
        f = settingsObj.getRemarksFont();
        f = new Font(f.getName(), f.getStyle(), f.getSize() + 4);
        jTextAreaRemarks.setFont(f);
        // get the default fontsize for tables and lists
        int defaultsize = settingsObj.getTableFontSize();
        // get current font
        int fsize = jListQuickInputAuthor.getFont().getSize();
        // retrieve default listvewfont
        Font defaultfont = settingsObj.getTableFont();
        // create new font, add fontsize-value
        f = new Font(defaultfont.getName(), defaultfont.getStyle(), fsize + defaultsize);
        // set new font
        jListQuickInputAuthor.setFont(f);
        jListQuickInputKeywords.setFont(f);
        jListKeywords.setFont(f);
        jListLinks.setFont(f);
        jTextFieldTitle.setFont(f);
        // get the default fontsize for textfields
        defaultsize = settingsObj.getTextfieldFontSize();
        // only set new fonts, when fontsize differs from the initial value
        if (defaultsize > 0) {
            // get current font
            f = jTextAreaEntry.getFont();
            // create new font, add fontsize-value
            f = new Font(f.getName(), f.getStyle(), f.getSize() + defaultsize);
            // set new font
            jTextAreaEntry.setFont(f);
            jTextAreaRemarks.setFont(f);
            jTextAreaAuthor.setFont(f);
        }
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
                = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
                getContext().getActionMap(EditorFrame.class, this);
        // iterate the xml file with the accelerator keys for the main window
        for (int cnt = 1; cnt <= accKeys.getCount(AcceleratorKeys.NEWENTRYKEYS); cnt++) {
            // get the action's name
            String actionname = accKeys.getAcceleratorAction(AcceleratorKeys.NEWENTRYKEYS, cnt);
            // check whether we have found any valid action name
            if (actionname != null && !actionname.isEmpty()) {
                // retrieve action
                AbstractAction ac = (AbstractAction) actionMap.get(actionname);
                // get the action's accelerator key
                String actionkey = accKeys.getAcceleratorKey(AcceleratorKeys.NEWENTRYKEYS, cnt);
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
        if (!settingsObj.isMacAqua()) {
            // init the variables
            String menutext;
            char mkey;
            // the mnemonic key for the file menu
            menutext = newEntryFileMenu.getText();
            mkey = menutext.charAt(0);
            newEntryFileMenu.setMnemonic(mkey);
            // the mnemonic key for the edit menu
            menutext = newEntryEditMenu.getText();
            mkey = menutext.charAt(0);
            newEntryEditMenu.setMnemonic(mkey);
            // the mnemonic key for the insert menu
            menutext = newEntryInsertMenu.getText();
            mkey = menutext.charAt(0);
            newEntryInsertMenu.setMnemonic(mkey);
            // the mnemonic key for the format menu
            menutext = newEntryFormatMenu.getText();
            mkey = menutext.charAt(0);
            newEntryFormatMenu.setMnemonic(mkey);
            // the mnemonic key for the file menu
            menutext = newEntryWindowMenu.getText();
            mkey = menutext.charAt(1);
            newEntryWindowMenu.setMnemonic(mkey);
        }
        // on Mac OS, at least for the German locale, the File menu is called different
        // compared to windows or linux. Furthermore, we don't need the about and preferences
        // menu items, since these are locates on the program's menu item in the apple-menu-bar
        if (PlatformUtil.isMacOS()) {
            newEntryFileMenu.setText(resourceMap.getString("macFileMenuText"));
        }
    }

    /**
     * This method inits the action map for several components like the tables,
     * the treeviews or the lists. here we can associate certain keystrokes with
     * related methods. e.g. hitting the enter-key in a table shows (activates)
     * the related entry.
     * <br><br>
     * Setting up action maps gives a better overview and is shorter than adding
     * key-release-events to all components, although key-events would fulfill
     * the same purpose.
     * <br><br>
     * The advantage of action maps is, that dependent from the operating system
     * we need only to associte a single action. with key-events, for each
     * component we have to check whether the operating system is mac os or
     * windows, and then checking for different keys, thus doubling each
     * command: checking for F2 to edit, or checking for command+enter and also
     * call the edit-method. using action maps, we simply as for the os once,
     * storing the related keystroke-value as string, and than assign this
     * string-value to the components.
     */
    private void initActionMaps() {
        // create action which should be executed when the user presses
        // the enter-key
        AbstractAction a_enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jListQuickInputAuthor == e.getSource()) {
                    addQuickAuthorToList();
                } else if (jListQuickInputKeywords == e.getSource()) {
                    addQuickKeywordToList();
                } else if (jTextFieldFilterAuthorlist == e.getSource()) {
                    filterAuthors();
                } else if (jTextFieldFilterKeywordlist == e.getSource()) {
                    filterKeywords();
                } else if (jTextFieldAddKeyword == e.getSource()) {
                    addKeyword();
                } else if (jTextFieldAddLink == e.getSource()) {
                    addLink();
                }
            }
        };
        // put action to the tables' actionmaps
        jListQuickInputAuthor.getActionMap().put("EnterKeyPressed", a_enter);
        jListQuickInputKeywords.getActionMap().put("EnterKeyPressed", a_enter);
        jTextFieldFilterAuthorlist.getActionMap().put("EnterKeyPressed", a_enter);
        jTextFieldFilterKeywordlist.getActionMap().put("EnterKeyPressed", a_enter);
        jTextFieldAddKeyword.getActionMap().put("EnterKeyPressed", a_enter);
        jTextFieldAddLink.getActionMap().put("EnterKeyPressed", a_enter);
        // associate enter-keystroke with that action
        KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
        jListQuickInputAuthor.getInputMap().put(ks, "EnterKeyPressed");
        jListQuickInputKeywords.getInputMap().put(ks, "EnterKeyPressed");
        jTextFieldFilterAuthorlist.getInputMap().put(ks, "EnterKeyPressed");
        jTextFieldFilterKeywordlist.getInputMap().put(ks, "EnterKeyPressed");
        jTextFieldAddKeyword.getInputMap().put(ks, "EnterKeyPressed");
        jTextFieldAddLink.getInputMap().put(ks, "EnterKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_delete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jListKeywords == e.getSource()) {
                    removeKeywordFromList();
                } else if (jListLinks == e.getSource()) {
                    removeLinkFromList();
                }
            }
        };
        jListKeywords.getActionMap().put("DeleteKeyPressed", a_delete);
        jListLinks.getActionMap().put("DeleteKeyPressed", a_delete);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "BACK_SPACE" : "DELETE");
        jListKeywords.getInputMap().put(ks, "DeleteKeyPressed");
        jListLinks.getInputMap().put(ks, "DeleteKeyPressed");
        // create action which should be executed when the user presses
        // the delete/backspace-key
        AbstractAction a_tab = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextAreaEntry == e.getSource()) {
                    NewEntryFrameUtil.checkSteno(settingsObj, stenoObj, jTextAreaEntry);
                }
                if (jTextAreaAuthor == e.getSource()) {
                    NewEntryFrameUtil.checkSteno(settingsObj, stenoObj, jTextAreaAuthor);
                }
                if (jTextAreaRemarks == e.getSource()) {
                    NewEntryFrameUtil.checkSteno(settingsObj, stenoObj, jTextAreaRemarks);
                }
            }
        };
        jTextAreaEntry.getActionMap().put("TabKeyPressed", a_tab);
        jTextAreaAuthor.getActionMap().put("TabKeyPressed", a_tab);
        jTextAreaRemarks.getActionMap().put("TabKeyPressed", a_tab);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("TAB");
        jTextAreaEntry.getInputMap().put(ks, "TabKeyPressed");
        jTextAreaAuthor.getInputMap().put(ks, "TabKeyPressed");
        jTextAreaRemarks.getInputMap().put(ks, "TabKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-space-key. this should insert a protected space sign
        AbstractAction a_space = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextAreaEntry == e.getSource()) {
                    jTextAreaEntry.replaceSelection("&#160;");
                }
                if (jTextAreaRemarks == e.getSource()) {
                    jTextAreaRemarks.replaceSelection("&#160;");
                }
            }
        };
        jTextAreaEntry.getActionMap().put("SpaceKeyPressed", a_space);
        jTextAreaRemarks.getActionMap().put("SpaceKeyPressed", a_space);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("control SPACE");
        jTextAreaEntry.getInputMap().put(ks, "SpaceKeyPressed");
        jTextAreaRemarks.getInputMap().put(ks, "SpaceKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-asterisk-key. this should insert a bullet sign
        AbstractAction a_bullet = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextAreaEntry == e.getSource()) {
                    jTextAreaEntry.replaceSelection(String.valueOf((char) 8226));
                }
                if (jTextAreaRemarks == e.getSource()) {
                    jTextAreaRemarks.replaceSelection(String.valueOf((char) 8226));
                }
            }
        };
        jTextAreaEntry.getActionMap().put("BulletKeyPressed", a_bullet);
        jTextAreaRemarks.getActionMap().put("BulletKeyPressed", a_bullet);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "meta shift CLOSE_BRACKET" : "control shift PLUS");
        jTextAreaEntry.getInputMap().put(ks, "BulletKeyPressed");
        jTextAreaRemarks.getInputMap().put(ks, "BulletKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-space-key. this should insert a protected large space sign
        AbstractAction a_largespace = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextAreaEntry == e.getSource()) {
                    jTextAreaEntry.replaceSelection("&#8195;");
                }
                if (jTextAreaRemarks == e.getSource()) {
                    jTextAreaRemarks.replaceSelection("&#8195;");
                }
            }
        };
        jTextAreaEntry.getActionMap().put("LargeSpaceKeyPressed", a_largespace);
        jTextAreaRemarks.getActionMap().put("LargeSpaceKeyPressed", a_largespace);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke("control shift SPACE");
        jTextAreaEntry.getInputMap().put(ks, "LargeSpaceKeyPressed");
        jTextAreaRemarks.getInputMap().put(ks, "LargeSpaceKeyPressed");
        // create action which should be executed when the user presses
        // the ctrl-F10/meta-F10-key
        AbstractAction a_add = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextFieldFilterKeywordlist == e.getSource()) {
                    addQuickKeywordToList();
                } else if (jTextFieldFilterAuthorlist == e.getSource()) {
                    addQuickAuthorToList();
                }
            }
        };
        // put action to the tables' actionmaps
        jTextFieldFilterKeywordlist.getActionMap().put("AddKeyPressed", a_add);
        jTextFieldFilterAuthorlist.getActionMap().put("AddKeyPressed", a_add);
        // check for os, and use appropriate controlKey
        ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "meta F10" : "ctrl F10");
        jTextFieldFilterKeywordlist.getInputMap().put(ks, "AddKeyPressed");
        jTextFieldFilterAuthorlist.getInputMap().put(ks, "AddKeyPressed");
    }

    /**
     * This method is called when the user wants to edit an entry. Here we fill
     * all the components (textareas, litviews) with the related
     * texts/list-items.
     */
    private void initFields() {
        // get the title and fill textfield
        jTextFieldTitle.setText(dataObj.getZettelTitle(entryNumber));
        // get the content of that entry
        String text = dataObj.getZettelContent(entryNumber);
        // add new lines after each bullet-point, so they are display in a better overview.
        // usually, to avoid <br>-tags within <ul> and <li>-tags when the entry is converted
        // to html, an entered list will be converted to a single line, removing all new lines.
        // but for editing and display, it is better to have them in single lines each.
        text = text.replace(Constants.FORMAT_LIST_OPEN, Constants.FORMAT_LIST_OPEN + System.lineSeparator());
        text = text.replace(Constants.FORMAT_LIST_CLOSE, Constants.FORMAT_LIST_CLOSE + System.lineSeparator());
        text = text.replace(Constants.FORMAT_NUMBEREDLIST_OPEN, Constants.FORMAT_NUMBEREDLIST_OPEN + System.lineSeparator());
        text = text.replace(Constants.FORMAT_NUMBEREDLIST_CLOSE, Constants.FORMAT_NUMBEREDLIST_CLOSE + System.lineSeparator());
        text = text.replace(Constants.FORMAT_LISTITEM_CLOSE, Constants.FORMAT_LISTITEM_CLOSE + System.lineSeparator());
        // and set the text to the textarea
        jTextAreaEntry.setText(Tools.replaceUbbToUnicode(text));
        // get the authors and set them to the textarea
        String[] authors = dataObj.getAuthors(entryNumber);
        // stringbuffer for temporatily saving the authors
        StringBuilder sb = new StringBuilder("");
        // if we have any authors, go on and build author list
        if (authors != null) {
            // iterate array and add each author to the buffer, and append a new line
            // after each author. we use a temporary stringbuffer here, so we can 
            // delete the last newline ("\n") which is not needed...
            for (String au : authors) {
                sb.append(au);
                sb.append(System.lineSeparator());
            }
        }
        // delete last newline-symbol
        if (sb.length() > 0) {
            sb.setLength(sb.length() - (System.lineSeparator().length()));
        }
        // and set the string to the textarea
        jTextAreaAuthor.setText(sb.toString());
        // retrieve the current keywords
        String[] kws = dataObj.getKeywords(entryNumber);
        // prepare the JList which will display the keywords
        keywordListModel.clear();
        // check whether any keywords have been found
        if (kws != null) {
            // sort keywords
            if (kws.length > 0) {
                Arrays.sort(kws);
            }
            // iterate the string array and add its content to the list model
            for (String kw : kws) {
                keywordListModel.addElement(kw);
            }
        }
        // get the remarks of that entry
        text = dataObj.getRemarks(entryNumber);
        // and set the text to the textarea
        jTextAreaRemarks.setText(Tools.replaceUbbToUnicode(text));
        // empty the JList with hyperlinks
        linkListModel.clear();
        // now retrieve the (hyper-)links of an entry
        hyperlinks = dataObj.getAttachments(entryNumber);
        // if we have (hyper-)links, continue
        if (hyperlinks != null && hyperlinks.size() > 0) {
            // iterare the child elements of the hyperlinks. these were
            // passed as parameter as a List-type
            Iterator<?> iterator = hyperlinks.iterator();

            while (iterator.hasNext()) {
                // first, copy the element of the list to an own variable
                Element link = (Element) iterator.next();
                // if it's not an empty element, add it to the JListView
                if (!link.getText().isEmpty()) {
                    linkListModel.addElement(link.getText());
                }
            }
        }
        // finally, change title
        setTitle(resourceMap.getString("frametitleEdit") + " (" + String.valueOf(entryNumber) + ")");
        // if we have editmode, enable apply-button
        setTextfieldFilled(!jTextAreaEntry.getText().isEmpty());
    }

    /**
     * This method changed the modified state. We need to do this in a method,
     * because when the user edits an entry, the apply-button will not become
     * enabled on changes that are made outside the main jTextAreaEntry. The
     * property "textfieldFilled", which enables or disables the apply-button,
     * is only changed when the document of the jTextAreaEntry is changed. This
     * should prevent applying empty content to a new entry.<br><br>
     * But: when editing an entry, the text-content in the jTextAreaEntry could
     * be filled, while the user changes the author-values - these changes are
     * recognized in the modified-value, but do usually not enable the
     * apply-button. thus, when we have editmode (true), we also enable the
     * apply-button here...
     *
     * @param m whether the modified state is true or false
     */
    private void setModified(boolean m) {
        // change modified state
        modified = m;
        // if we have editmode, enable apply-button
        setTextfieldFilled(!jTextAreaEntry.getText().isEmpty());
    }

    public boolean isModified() {
        return modified;
    }

    private void updateTextFieldText(JTextField tf) {
        if (tf.isFocusOwner()) {
            String text = tf.getText();
            if (text.equals(resourceMap.getString("textFieldDefaultText"))) {
                tf.setText("");
            }
        } else {
            String text = tf.getText();
            if (text.isEmpty()) {
                tf.setText(resourceMap.getString("textFieldDefaultText"));
            }
        }
    }

    /**
     * This method updates the toolbar and en-/disables the menu- and
     * toolbar-items depending on whether the main textfield (jTextAreaEntry)
     * has the focus or not. Formatting action only apply to the maintextfield,
     * so disable then when other fields have the input focus.
     *
     * @param focus indicates whether the <b>main textfield</b> has the focus or
     * nor. if this parameter is false, the undo/redo buttons are always
     * disabled. if the value is true, the buttons are enabled when undo/redo is
     * possible (canUndo() and canRedo()).
     */
    private void updateToolbar(boolean foc) {
        // we've outsourced the update of the undo/redo buttons to an
        // own method so we can call this method also from within the
        // documents change listener. undo/redo buttons will immediately
        // being updated as the users is typing text.
        updateUndoRedoButtons(foc);
        setFocus(foc);
    }

    /**
     * En-/disables the undo and redo buttons.<br>
     * Usually this method belongs to "updateToolbar()", but we've outsourced
     * this part so we can call this method in the documents change listener.
     * The undo/redo buttons are then also immediately being updated when the
     * user types text.
     *
     * @param focus indicates whether the main textfield has the focus or nor.
     * if this parameter is false, the undo/redo buttons are always disabled. if
     * the value is true, the buttons are enabled when undo/redo is possible
     * (canUndo() and canRedo()).
     */
    private void updateUndoRedoButtons(boolean foc) {
        // set undo/redo
        setUndoPossible(foc && undomanager.canUndo());
        setRedoPossible(foc && undomanager.canRedo());
    }

    private void setRefreshKeywords() {
        if (settingsObj.getQuickInput() && (dataObj.getCount(Daten.KWCOUNT) > 0)) {
            // only the first and the third step are using the text-content to retrieve
            // the keywords, so we only need to enable the refresh-button for the keyword-quickinput
            // when text is changed during the 1st or 3rd step.
            if ((1 == stepcounter) || (3 == stepcounter)) {
                // disable the refresh and filter buttons
                jButtonRefreshKeywordlist.setEnabled(true);
                // set upto-date state to false
                keywordsListUpToDate = false;
            }
        }
    }

    @Action
    public void editSynonyms() {
        if (null == synonymsDlg) {
            synonymsDlg = new CSynonymsEdit(null, synonymsObj, settingsObj, dataObj);
            synonymsDlg.setLocationRelativeTo(null);
        }
        ZettelkastenApp.getApplication().show(synonymsDlg);
        // change modified state and enable apply-button
        if (synonymsDlg.isModified()) {
            mainframe.setBackupNecessary();
        }
        synonymsDlg.dispose();
        synonymsDlg = null;
    }

    @Action
    public void refreshAuthorList() {
        authorListUpToDate = false;
        showAuthors();
    }

    @Action
    public void setFocusToEditPane() {
        jTextAreaEntry.requestFocusInWindow();
    }

    @Action
    public void setFocusToKeywordList() {
        jTabbedPaneNewEntry1.setSelectedIndex(1);
        jTextFieldFilterKeywordlist.requestFocusInWindow();
    }

    @Action
    public void setFocusToAuthorList() {
        jTabbedPaneNewEntry1.setSelectedIndex(0);
        jTextFieldFilterAuthorlist.requestFocusInWindow();
    }

    /**
     * This method creates the quickinput-list for authors by calling a
     * background task which does this work...
     */
    @Action
    public final synchronized void showAuthors() {
        // when authorlist is uptodate, leave...
        if (authorListUpToDate) {
            // select added authors if we have some
            selectNewAddedAuthors();
            return;
        }
        // en/disable keyword quick-input function depending on the amount of keywords
        int aucount = dataObj.getCount(Daten.AUCOUNT);
        jTextFieldFilterAuthorlist.setEnabled(aucount > 0);
        // disbale button, will only be enabled on selection
        // jButtonAddAuthors.setEnabled(false);
        // when we have no authors at all, quit
        if (aucount < 1) {
            return;
        }
        // leave method when task is already running...
        if (qiAuthorTaskIsRunning) {
            return;
        }
        // set upto-date-indicator to false, otherwise the thread will not be executed
        authorListUpToDate = false;
        // when opening this dialog, automatically create the author list
        Task qiauT = quickInputAuthor();
        // get the application's context...
        ApplicationContext appC = Application.getInstance().getContext();
        // ...to get the TaskMonitor and TaskService
        TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        // with these we can execute the task and bring it to the foreground
        // i.e. making the animated progressbar and busy icon visible
        tS.execute(qiauT);
        tM.setForegroundTask(qiauT);
    }

    /**
     * This method creates the quickinput-list for keywords by calling a
     * background task which does this work...
     */
    @Action
    public synchronized void showKeywords() {
        // en/disable keyword quick-input function depending on the amount of keywords
        int kwcount = dataObj.getCount(Daten.KWCOUNT);
        // no author selected
        setAuthorSelected(false);
        // enable textfield for filtering
        jTextFieldFilterKeywordlist.setEnabled(kwcount > 0);
        // disable buttons. will only be enabled on table selection
        // jButtonAddKeywords.setEnabled(false);
        // show special button if we have quickinput
        jButtonQuickKeyword.setVisible((stepcounter < 4) && settingsObj.getQuickInput());
        // when we have no keywords, quit
        if (kwcount < 1) {
            return;
        }
        // if we don't have quick-input settings, show whole keywords.
        if (!settingsObj.getQuickInput()) {
            // enable combobox
            jComboBoxQuickInput.setEnabled(false);
            // reset linked lists...
            keywordStep1 = selectedKeywords = remainingKeywords = null;
            stepcounter = 1;
            // when the task is already running, leave...
            if (qiKeywordTaskIsRunning) {
                return;
            }
            // when opening this dialog, automatically create the author list
            Task qikwT = quickInputKeywords();
            // get the application's context...
            ApplicationContext appC = Application.getInstance().getContext();
            // ...to get the TaskMonitor and TaskService
            TaskMonitor tM = appC.getTaskMonitor();
            TaskService tS = appC.getTaskService();
            // with these we can execute the task and bring it to the foreground
            // i.e. making the animated progressbar and busy icon visible
            tS.execute(qikwT);
            tM.setForegroundTask(qikwT);
        } else {
            // enable combobox
            jComboBoxQuickInput.setEnabled(stepcounter < 2);
            // only process the task if keywordlist is not uptodate. else we can
            // reset the list by setting the linkedlists
            if (!keywordsListUpToDate) {
                // only create list when we have a text length of more that 3 chars
                if (jTextAreaEntry.getText().length() > 3 || jTextFieldTitle.getText().length() > 2) {
                    // the background task is only needed for the first three steps. within the
                    // third step, the remaining keywords are equal to the keywordStep4-list, since
                    // the fourth step only contains the remaining keywords...
                    if (stepcounter < 4) {
                        createQuickKeywordList();
                    }
                    // here we start with step one
                    if (1 == stepcounter) {
                        // when the task is over, receive the remaining keywords...
                        remainingKeywords = taskinfo.getKeywordSuggesionList(TasksData.REMAINING_KW);
                        // and the current keywordlist
                        displayedKeywordList = keywordStep1 = taskinfo.getKeywordSuggesionList(TasksData.NEW_KW);
                    } else if (2 == stepcounter || 3 == stepcounter) {
                        // when the task is over, receive the remaining keywords...
                        remainingKeywords = taskinfo.getKeywordSuggesionList(TasksData.REMAINING_KW);
                        // and the current keywordlist
                        displayedKeywordList = taskinfo.getKeywordSuggesionList(TasksData.NEW_KW);
                    } else if (4 == stepcounter) {
                        // when the task is over, receive the remaining keywords...
                        displayedKeywordList = remainingKeywords;
                        // update button-appearance
                        jButtonAddKeywords.putClientProperty("JButton.segmentPosition", "only");
                    }
                    // and set the new keywords to the list
                    jListQuickInputKeywords.setListData(displayedKeywordList.toArray());
                    // scroll to first entry in the list
                    jListQuickInputKeywords.ensureIndexIsVisible(0);
                    // dispose the window and clear the object
                    if (taskDlg != null) {
                        taskDlg.dispose();
                        taskDlg = null;
                    }
                    // clear the filter-textfield
                    jTextFieldFilterKeywordlist.setText("");
                    // disable the refresh and filter buttons
                    jButtonRefreshKeywordlist.setEnabled(false);
                    // set upto-date state to true, so we don't have to start the task more often
                    // than needed
                    keywordsListUpToDate = true;
                } else {
                    jListQuickInputKeywords.setListData(new String[]{});
                }
                // enable textfield, if necessary
                int listsize = jListQuickInputKeywords.getModel().getSize();
                jTextFieldFilterKeywordlist.setEnabled(listsize > 0);
            } else {
                jListQuickInputKeywords.setListData(displayedKeywordList.toArray());
                // scroll to first entry in the list
                jListQuickInputKeywords.ensureIndexIsVisible(0);
                // disable the refresh and filter buttons
                jButtonRefreshKeywordlist.setEnabled(false);
            }
        }
    }

    private void createQuickKeywordList() {
        // if dialog window isn't already created, do this now
        if (null == taskDlg) {
            // retrieve selection
            String text = jTextAreaEntry.getSelectedText();
            // check whether text-selection is at least 3 chars long. in this case,
            // the keywords are selected according to the text-selection. in all other
            // cases, we use the complete entry-text as base for the keyword-selection
            if (null == text || text.length() < 3) {
                // get the entry-text, consisting of title and text content
                text = jTextFieldTitle.getText() + " " + jTextAreaEntry.getText();
            }
            // get parent und init window
            taskDlg = new TaskProgressDialog(this, TaskProgressDialog.TASK_KEYWORDSUGGESTIONS, taskinfo, dataObj, synonymsObj, settingsObj,
                    settingsObj.getQuickInputExtended(), stepcounter, selectedKeywords, remainingKeywords, keywordStep1, text);
            // center window
            taskDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(taskDlg);
        // dispose the window and clear the object
        taskDlg.dispose();
        taskDlg = null;
    }

    private void enableBySelection() {
        String selection = null;
        switch (focusowner) {
            case Constants.FOCUS_FIELD_TEXT:
                selection = jTextAreaEntry.getSelectedText();
                break;
            case Constants.FOCUS_FIELD_AUTHOR:
                selection = jTextAreaAuthor.getSelectedText();
                break;
            case Constants.FOCUS_FIELD_REMARKS:
                selection = jTextAreaRemarks.getSelectedText();
                break;
        }
        setTextSelected(selection != null);
        setSegmentPossible((selection != null) && (jListQuickInputKeywords.getSelectedValue() != null));
    }

    /**
     * This method checks which textfield has the focus and then selects the
     * whole text in that component.
     */
    @Action
    public void selecteAllText() {
        if (jTextFieldTitle.isFocusOwner()) {
            jTextFieldTitle.selectAll();
        }
        if (jTextAreaEntry.isFocusOwner()) {
            jTextAreaEntry.selectAll();
        }
        if (jTextAreaAuthor.isFocusOwner()) {
            jTextAreaAuthor.selectAll();
        }
        if (jTextAreaRemarks.isFocusOwner()) {
            jTextAreaRemarks.selectAll();
        }
        if (jTextFieldAddKeyword.isFocusOwner()) {
            jTextFieldAddKeyword.selectAll();
        }
        if (jTextFieldAddLink.isFocusOwner()) {
            jTextFieldAddLink.selectAll();
        }
        setTextSelected(true);
    }

    /**
     * Undo function for the main text field
     */
    @Action(enabledProperty = "undoPossible")
    public void undoAction() {
        if (undomanager.canUndo()) {
            undomanager.undo();
        }
        jTextAreaEntry.requestFocus();
    }

    /**
     * Redo function for the main text field
     */
    @Action(enabledProperty = "redoPossible")
    public void redoAction() {
        if (undomanager.canRedo()) {
            undomanager.redo();
        }
        jTextAreaEntry.requestFocus();
    }

    @Action
    public void addAuthorFromMenu() {
        // open an input-dialog, setting the selected value as default-value
        if (null == biggerEditDlg) {
            // create a new dialog with the bigger edit-field, passing some initial values
            biggerEditDlg = new CBiggerEditField(this, settingsObj, resourceMap.getString("newAuthorTitle"), "", "", Constants.EDIT_AUTHOR);
            // center window
            biggerEditDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(biggerEditDlg);
        // after closing the window, get the new value
        String newAu = biggerEditDlg.getNewValue();
        String newBibKey = biggerEditDlg.getNewBibKey();
        // delete the input-dialog
        biggerEditDlg.dispose();
        biggerEditDlg = null;
        // if we have a valid return-value...
        if ((newAu != null) && (newAu.length() > 0)) {
            // get system line separator
            String linesep = System.lineSeparator();
            // but first, we habe to remove all carriage-returns (\r), which are part of the
            // line-seperator in windows. somehow, the replace-command does *not* work, when
            // we replace "System.lineSeparator()" with "[br]", but only when
            // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
            // "\r", it is replaced by nothing, to clean the content.
            if (linesep.contains("\r")) {
                newAu = newAu.replace("\r", "");
            }
            // ...parse them to an array
            String[] authors = newAu.split("\n");
            // list for new authors. needed to avoid empty entries
            LinkedList<String> newaus = new LinkedList<>();
            // go through array of all authors
            for (String a : authors) {
                // check whether string is empty or not (may occur when the user uses two new lines
                // for separating several entries...
                if (!a.isEmpty()) {
                    // add author. used below for the array "selectedAuthors"
                    newaus.add(a);
                    // check whether the value already exists
                    if (-1 == dataObj.getAuthorPosition(a)) {
                        // check whether author already exisrs in textfield
                        if (!checkForDoubleAuthors(a)) {
                            // if not, append author string
                            // therefore, add a new line, but only if the textfield is not empty
                            // (i.e. we already have an author)
                            // add author to data file
                            dataObj.addAuthor(a, 0);
                            // change bibkey
                            if (newBibKey != null) {
                                dataObj.setAuthorBibKey(a, newBibKey);
                                // reset bibkey, so we only use it once in case we have multiple authors
                                newBibKey = null;
                            }
                            // if not, append author string
                            // therefore, add a new line, but only if the textfield is not empty
                            // (i.e. we already have an author)
                            if (!jTextAreaAuthor.getText().isEmpty()) {
                                jTextAreaAuthor.append(System.lineSeparator());
                            }
                            jTextAreaAuthor.append(a);
                            // set the modified state
                            setModified(true);
                            // authorlist is no longer uptodate
                            authorListUpToDate = false;
                            // we need to do this so we know in our mainframe that we
                            // have new authors and the authorlist is out of date.
                            dataObj.setAuthorlistUpToDate(false);
                        }
                    } else {
                        // display error message box
                        JOptionPane.showMessageDialog(this, resourceMap.getString("errValueExistsMsg"), resourceMap.getString("errValueExistsTitle"), JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
            // copy new added authors into a global array, so we can select the new addes authors
            // after they have been added to the list via the background-task
            if (newaus.size() > 0) {
                selectedAuthors = newaus.toArray(new String[newaus.size()]);
            }
            // refresh authorlist
            showAuthors();
        }
    }

    private void selectNewAddedAuthors() {
        if (selectedAuthors != null) {
            // create an integer array for the index-numbers of the new author values
            int[] selectedIndices = new int[selectedAuthors.length];
            // go through all new author values and retrieve their index-numbers.
            // save these numbers to the array
            for (int cnt = 0; cnt < selectedAuthors.length; cnt++) {
                selectedIndices[cnt] = quickInputAuthorListModel.indexOf(selectedAuthors[cnt]);
            }
            // sort the array, so when making the first entry in the *array* visible, this should also be the
            // first visible entry in the jList
            if (selectedIndices != null && selectedIndices.length > 0) {
                Arrays.sort(selectedIndices);
            }
            // select new values
            jListQuickInputAuthor.setSelectedIndices(selectedIndices);
            // make first value visible
            jListQuickInputAuthor.ensureIndexIsVisible(selectedIndices[0]);
            // reset author-array
            selectedAuthors = null;
            // switch select propertry
            setAuthorSelected(true);
        }
    }

    /**
     * This method removes the currently selected item(s) from the keywordlist
     */
    @Action(enabledProperty = "keywordSelected")
    public void removeKeyword() {
        removeKeywordFromList();
    }

    /**
     * This method removes the currently selected item(s) from the linklist
     */
    @Action(enabledProperty = "attachmentSelected")
    public void removeLink() {
        removeLinkFromList();
    }

    @Action(enabledProperty = "textSelected")
    public void addSelectionToSteno() {
        // check which textarea has the input focus, so we can copy that component
        // into our variable - so we avoid code-duplication, and not using for each textarea
        // its own spell-checking...
        JTextArea ta = null;
        switch (focusowner) {
            case Constants.FOCUS_FIELD_TEXT:
                ta = jTextAreaEntry;
                break;
            case Constants.FOCUS_FIELD_AUTHOR:
                ta = jTextAreaAuthor;
                break;
            case Constants.FOCUS_FIELD_REMARKS:
                ta = jTextAreaRemarks;
                break;
        }
        // if any textarea has the focus (and no text*field*) go on...
        if (ta != null) {
            // the button for editing the spellchecking-words was pressed,
            // so open the window for edting them...
            if (null == stenoEdit) {
                // get parent und init window
                stenoEdit = new CStenoEdit(this, stenoObj, settingsObj, ta.getSelectedText());
                // center window
                stenoEdit.setLocationRelativeTo(this);
            }
            ZettelkastenApp.getApplication().show(stenoEdit);
            // dispose window
            stenoEdit.dispose();
            stenoEdit = null;
        }
    }

    @Action(enabledProperty = "textSelected")
    public void addSelectionToSpellCorrection() {
        // check which textarea has the input focus, so we can copy that component
        // into our variable - so we avoid code-duplication, and not using for each textarea
        // its own spell-checking...
        JTextArea ta = null;
        switch (focusowner) {
            case Constants.FOCUS_FIELD_TEXT:
                ta = jTextAreaEntry;
                break;
            case Constants.FOCUS_FIELD_AUTHOR:
                ta = jTextAreaAuthor;
                break;
            case Constants.FOCUS_FIELD_REMARKS:
                ta = jTextAreaRemarks;
                break;
        }
        // if any textarea has the focus (and no text*field*) go on...
        if (ta != null) {
            // the button for editing the spellchecking-words was pressed,
            // so open the window for edting them...
            if (null == autoKorrektEdit) {
                // get parent und init window
                autoKorrektEdit = new CAutoKorrekturEdit(this, spellObj, settingsObj, ta.getSelectedText().toLowerCase());
                // center window
                autoKorrektEdit.setLocationRelativeTo(this);
            }
            ZettelkastenApp.getApplication().show(autoKorrektEdit);
            // dispose window
            autoKorrektEdit.dispose();
            autoKorrektEdit = null;
            // get the correct spelling, if word was mispelled
            String correct = spellObj.getCorrectSpelling(ta.getSelectedText());
            // if we found a correct spellig, i.e. the typed word was false, go on
            if (correct != null) {
                ta.replaceSelection(correct);
            }
        }
    }

    /**
     * This is a small method which surrounds the currently selected text with
     * tags which are supplied as parameters
     *
     * @param opentag (the tag which is placed before the selection)
     * @param closetag (the tag which is placed after the selection)
     */
    private void surroundSelection(String opentag, String closetag) {
        // check whether tag is selected or not
        if (null == jTextAreaEntry.getSelectedText()) {
            // get caret position
            int caret = jTextAreaEntry.getCaretPosition();
            // if we don't have any selection, just insert tags
            jTextAreaEntry.replaceSelection(opentag + closetag);
            // set caret position in between the tags
            jTextAreaEntry.setCaretPosition(caret + opentag.length());
        } else {
            // get selection offset
            int sel_start = jTextAreaEntry.getSelectionStart();
            int sel_end = jTextAreaEntry.getSelectionEnd();
            // surround selection with tags
            jTextAreaEntry.replaceSelection(opentag + jTextAreaEntry.getSelectedText() + closetag);
            // set back selection
            jTextAreaEntry.setSelectionStart(sel_start + opentag.length());
            jTextAreaEntry.setSelectionEnd(sel_end + opentag.length());
        }
    }

    /**
     *
     */
    private void removeAlignment() {
        // retrieve text selection
        String selection = jTextAreaEntry.getSelectedText();
        // check whether tag is selected or not
        if (null == selection) {
            return;
        }
        // remember caret start
        int caret = jTextAreaEntry.getSelectionStart();
        // replace alignments
        selection = selection.replace(Constants.FORMAT_ALIGNCENTER_OPEN, "").replace(Constants.FORMAT_ALIGNCENTER_CLOSE, "");
        selection = selection.replace(Constants.FORMAT_ALIGNRIGHT_OPEN, "").replace(Constants.FORMAT_ALIGNRIGHT_CLOSE, "");
        selection = selection.replace(Constants.FORMAT_ALIGNLEFT_OPEN, "").replace(Constants.FORMAT_ALIGNLEFT_CLOSE, "");
        selection = selection.replace(Constants.FORMAT_ALIGNJUSTIFY_OPEN, "").replace(Constants.FORMAT_ALIGNJUSTIFY_CLOSE, "");
        // replace selection with removed alignment
        jTextAreaEntry.replaceSelection(selection);
        // set new selection
        jTextAreaEntry.setSelectionStart(caret);
        jTextAreaEntry.setSelectionEnd(caret + selection.length());
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have bold-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatBold() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_BOLD_OPEN, Constants.FORMAT_MD_BOLD_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_BOLD_OPEN, Constants.FORMAT_BOLD_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have italic-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatItalic() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_ITALIC_OPEN, Constants.FORMAT_MD_ITALIC_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_ITALIC_OPEN, Constants.FORMAT_ITALIC_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have underline-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatUnderline() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_UNDERLINE_OPEN, Constants.FORMAT_UNDERLINE_CLOSE);
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have
     * strike-through-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatStrikeThrough() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_STRIKE_OPEN, Constants.FORMAT_MD_STRIKE_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_STRIKE_OPEN, Constants.FORMAT_STRIKE_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a header 1st grade
     */
    @Action(enabledProperty = "focus")
    public void formatHeading1() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_H1_OPEN, Constants.FORMAT_MD_H1_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_H1_OPEN, Constants.FORMAT_H1_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we a header 2nd grade
     */
    @Action(enabledProperty = "focus")
    public void formatHeading2() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_H2_OPEN, Constants.FORMAT_MD_H2_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_H2_OPEN, Constants.FORMAT_H2_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case the text is aligned centered.
     */
    @Action(enabledProperty = "focus")
    public void alignCenter() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        removeAlignment();
        surroundSelection(Constants.FORMAT_ALIGNCENTER_OPEN, Constants.FORMAT_ALIGNCENTER_CLOSE);
    }

    /**
     */
    @Action(enabledProperty = "focus")
    public void alignRight() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        removeAlignment();
        surroundSelection(Constants.FORMAT_ALIGNRIGHT_OPEN, Constants.FORMAT_ALIGNRIGHT_CLOSE);
    }

    /**
     */
    @Action(enabledProperty = "focus")
    public void alignLeft() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        removeAlignment();
        surroundSelection(Constants.FORMAT_ALIGNLEFT_OPEN, Constants.FORMAT_ALIGNLEFT_CLOSE);
    }

    /**
     */
    @Action(enabledProperty = "focus")
    public void alignJustify() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        removeAlignment();
        surroundSelection(Constants.FORMAT_ALIGNJUSTIFY_OPEN, Constants.FORMAT_ALIGNJUSTIFY_CLOSE);
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have left and right border
     * margins.
     */
    @Action(enabledProperty = "focus")
    public void alignMargin() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an input dialog and let the user input the margin
        String margin = JOptionPane.showInputDialog(resourceMap.getString("msgInputMargin"));
        // if the user cancelled the dialog, quit method
        if (null == margin) {
            return;
        }
        // replace commas with periods.
        margin = margin.replace(",", ".");
        // else prepare tags
        surroundSelection("[m " + margin + "]", "[/m]");
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a quotation or
     * citation.
     */
    @Action(enabledProperty = "focus")
    public void formatCite() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_QUOTE_OPEN, Constants.FORMAT_MD_QUOTE_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_QUOTE_OPEN, Constants.FORMAT_QUOTE_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a inline quotation
     * marks.
     */
    @Action(enabledProperty = "focus")
    public void formatQuote() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_QUOTEMARK_OPEN, Constants.FORMAT_QUOTEMARK_CLOSE);
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a code blocks
     */
    @Action(enabledProperty = "focus")
    public void formatCode() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        if (settingsObj.getMarkdownActivated()) {
            surroundSelection(Constants.FORMAT_MD_CODE_OPEN, Constants.FORMAT_MD_CODE_CLOSE);
        } else {
            surroundSelection(Constants.FORMAT_CODE_OPEN, Constants.FORMAT_CODE_CLOSE);
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a inline-code blocks
     */
    @Action(enabledProperty = "focus")
    public void formatInlineCode() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_MD_CODE_OPEN, Constants.FORMAT_MD_CODE_CLOSE);
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a quotation or
     * citation.
     */
    @Action(enabledProperty = "segmentPossible")
    public void addSegment() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        // get index of first selected item
        Object o = jListKeywords.getSelectedValue();
        if (o != null) {
            surroundSelection("[s " + o.toString() + "]", "[/s]");
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a quotation or
     * citation.
     */
    @Action(enabledProperty = "segmentPossible")
    public void addSegmentFromQuickList() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        // get index of first selected item
        Object o = jListQuickInputKeywords.getSelectedValue();
        if (o != null) {
            surroundSelection("[s " + o.toString() + "]", "[/s]");
            addQuickKeywordToList();
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have changed the
     * text-color.
     */
    @Action(enabledProperty = "focus")
    public void formatColor() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("msgInputColor"), null);
        // if the user chose a color, proceed
        if (color != null) {
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // and insert the tags
            surroundSelection("[color #" + output.toString() + "]", "[/color]");
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have another
     * text-background-color
     */
    @Action(enabledProperty = "focus")
    public void formatHighlight() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method

        // first, show an color-chooser-dialog and let the user choose the color
        Color color = JColorChooser.showDialog(this, resourceMap.getString("msgInputColor"), null);
        // if the user chose a color, proceed
        if (color != null) {
            // convert the color-rgb-values into a hexa-decimal-string
            StringBuilder output = new StringBuilder("");
            // we need the format option to keep the leeding zero of hex-values
            // from 00 to 0F.
            output.append(String.format("%02x", color.getRed()));
            output.append(String.format("%02x", color.getGreen()));
            output.append(String.format("%02x", color.getBlue()));
            // and insert the tags
            surroundSelection("[h #" + output.toString() + "]", "[/h]");
        }
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a list with bullet
     * points
     */
    @Action(enabledProperty = "focus")
    public void formatList() {
        insertList(Constants.FORMAT_LIST_OPEN, Constants.FORMAT_LIST_CLOSE);
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have a list with bullet
     * points
     */
    @Action(enabledProperty = "focus")
    public void formatOrderedList() {
        insertList(Constants.FORMAT_NUMBEREDLIST_OPEN, Constants.FORMAT_NUMBEREDLIST_CLOSE);
    }

    /**
     *
     * @param listTypeOpenTag
     * @param listTypeCloseTag
     */
    private void insertList(String listTypeOpenTag, String listTypeCloseTag) {
        // retrieve the selection
        String selection = jTextAreaEntry.getSelectedText();
        // get system line separator
        String linesep = System.lineSeparator();
        // check whether tag is selected or not
        if (null == selection) {
            // if we don't have any selection, just insert tags
            jTextAreaEntry.replaceSelection(listTypeOpenTag + linesep + Constants.FORMAT_LISTITEM_OPEN + Constants.FORMAT_LISTITEM_CLOSE + linesep + listTypeCloseTag);
        } else {
            // first, we habe to remove all carriage-returns (\r), which are part of the
            // line-seperator in windows. somehow, the replace-command does *not* work, when
            // we replace "System.lineSeparator()" with "[br]", but only when
            // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
            // "\r", it is replaced by nothing, to clean the content.
            if (linesep.contains("\r")) {
                selection = selection.replace("\r", "");
            }
            // first of, split the selected text at each new line
            String[] lines = selection.split("\n");
            // create a new stringbuffer for the output-string
            StringBuilder output = new StringBuilder("");
            // append the "open"-tag
            output.append(listTypeOpenTag);
            output.append(System.lineSeparator());
            for (String line : lines) {
                // append the open/close-tags for the bullet points
                // and put the line between these tags
                output.append(Constants.FORMAT_LISTITEM_OPEN).append(line).append(Constants.FORMAT_LISTITEM_CLOSE);
                output.append(System.lineSeparator());
            }
            // finally, append the close-tag
            output.append(listTypeCloseTag);
            output.append(System.lineSeparator());
            // and paste the text
            jTextAreaEntry.replaceSelection(output.toString());
        }

    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have
     * text-superscript-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatSup() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_SUP_OPEN, Constants.FORMAT_SUP_CLOSE);
    }

    @Action(enabledProperty = "focus")
    public void formatFont() {
        // create font-chooser dialog
        if (null == fontDlg) {
            fontDlg = new CFontChooser(null, lastSelectefFont);
            fontDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(fontDlg);

        // if the user has chosen a font, set it
        if (fontDlg.selectedFont != null) {
            lastSelectefFont = fontDlg.selectedFont;
            surroundSelection(Constants.FORMAT_FONT_OPEN + " " + lastSelectefFont.getFontName() + "]", Constants.FORMAT_FONT_CLOSE);
        }
        // close and dispose the font-dialog
        fontDlg.dispose();
        fontDlg = null;
    }

    /**
     * Retrieves the text selection from the maintextfield and sourrounds it
     * with the related format-tags. In this case we have
     * text-subscript-formatting.
     */
    @Action(enabledProperty = "focus")
    public void formatSub() {
        // since we have multiple usage of the folliwing code, we simply
        // put it in an own method
        surroundSelection(Constants.FORMAT_SUB_OPEN, Constants.FORMAT_SUB_CLOSE);
    }

    @Action(enabledProperty = "focus")
    public void insertTable() {
        // in case the caret is inside an existing table, we assume the user wants to edit
        // that table. therefore, copy the table-content and pass it as parameter to the
        // insertTableDialog
        String edittable = null;
        // retrieve caret position
        int caret = jTextAreaEntry.getCaretPosition();
        // retrieve start of table-tag
        int tablestartpos = jTextAreaEntry.getText().lastIndexOf("[table]");
        // now we check, whether the caret is inside a table. if yes, we copy the
        // table content and fill the insertTableDialog with the content of the available
        // table. Therefore, the caret has to be inside the table
        if (tablestartpos != -1 && tablestartpos < caret) {
            // retrieve end of table-tag
            int tableendpos = jTextAreaEntry.getText().indexOf("[/table]", tablestartpos);
            // check whether caret is still inside the table
            if (tableendpos != -1 && tableendpos > caret) {
                // copy table content
                edittable = jTextAreaEntry.getText().substring(tablestartpos + 7, tableendpos);
                // select current table, so it will be replaced
                try {
                    jTextAreaEntry.setCaretPosition(tablestartpos);
                    jTextAreaEntry.moveCaretPosition(tableendpos + 8);
                } catch (IllegalArgumentException ex) {
                }
            }
        }
        // the button for editing the spellchecking-words was pressed,
        // so open the window for edting them...
        if (null == insertTableDlg) {
            // get parent und init window
            insertTableDlg = new CInsertTable(this, settingsObj, edittable);
            // center window
            insertTableDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(insertTableDlg);
        // if we have any changes, insert table
        if (insertTableDlg.isModified()) {
            jTextAreaEntry.replaceSelection(insertTableDlg.getTableTag());
        } // else set caret to old position and "de-select" the text
        else {
            jTextAreaEntry.setCaretPosition(caret);
        }
        // dispose window
        insertTableDlg.dispose();
        insertTableDlg = null;
    }

    @Action(enabledProperty = "focus")
    public void insertManualLink() {
        // retrieve caret position
        int caret = jTextAreaEntry.getCaretPosition();
        // the button for editing the spellchecking-words was pressed,
        // so open the window for edting them...
        if (null == manualLinkEditDlg) {
            // get parent und init window
            manualLinkEditDlg = new CInsertManualLink(this, jTextAreaEntry.getSelectedText(), settingsObj);
            // center window
            manualLinkEditDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(manualLinkEditDlg);
        // retrieve return value
        String manlink = manualLinkEditDlg.getManlink();
        // if we have any changes, insert table
        if (manlink != null && !manlink.isEmpty()) {
            jTextAreaEntry.replaceSelection(manlink);
        } // else set caret to old position and "de-select" the text
        else {
            jTextAreaEntry.setCaretPosition(caret);
        }
        // dispose window
        manualLinkEditDlg.dispose();
        manualLinkEditDlg = null;
    }

    /**
     * This method inserts a footnote-tag, which inserts references to authors.
     * the footnote-tags can only be inserted if at least one author is
     * selected.
     */
    @Action(enabledProperty = "authorSelected")
    public void insertFootnote() {
        // retrieve all selected authors
        List<String> o = jListQuickInputAuthor.getSelectedValuesList();
        // if we have selections, go on
        if (!o.isEmpty()) {
            // check whether to insert braces around footnotes
            String fnbraceopen = settingsObj.getFootnoteBraces() ? "(" : "";
            String fnbraceclose = settingsObj.getFootnoteBraces() ? ")" : "";
            // create stringbuilder for footnote-tags
            StringBuilder fn = new StringBuilder("");
            // create list iterator
            Iterator<String> oink = o.iterator();
            while (oink.hasNext()) {
                // get each author string
                String au = oink.next();
                // check whether author already exisrs in textfield
                if (!checkForDoubleAuthors(au)) {
                    // if not, append author string
                    // therefore, add a new line, but only if the textfield is not empty
                    // (i.e. we already have an author)
                    if (!jTextAreaAuthor.getText().isEmpty()) {
                        jTextAreaAuthor.append(System.lineSeparator());
                    }
                    jTextAreaAuthor.append(au);
                    // set the modified state
                    setModified(true);
                }
                // find the authorposition for the footnote
                int aunr = dataObj.getAuthorPosition(au);
                // when we have a valid author, go on...
                if (aunr != -1) {
                    // if the stringbuilder already contains values, we have more than one footnote.
                    // thus, add a space between the footnotes...
                    if (fn.length() > 0) {
                        fn.append("; ");
                    }
                    // add the ubb-tag for the footnote
                    fn.append(Constants.FORMAT_FOOTNOTE_OPEN).append(String.valueOf(aunr)).append("]");
                }
            }
            // insert the string into the textfield
            if (fn.length() > 0) {
                jTextAreaEntry.replaceSelection(fnbraceopen + fn.toString() + fnbraceclose);
            }
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        // remember drop location
        int droplocation = DROP_LOCATION_TEXTAREAENTRY;
        // get transferable
        Transferable tr = dtde.getTransferable();
        try {
            // check whether we have files dropped into textarea
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || tr.isDataFlavorSupported(urlFlavor)) {
                // drag&drop was link action
                dtde.acceptDrop(DnDConstants.ACTION_LINK);
                // retrieve drop component
                Component c = dtde.getDropTargetContext().getDropTarget().getComponent();
                // check for valid value
                if (c != null) {
                    // retrieve component's name
                    String name = c.getName();
                    // check for valid value
                    if (name != null && !name.isEmpty()) {
                        // check if files were dropped in entry field
                        // in this case, image files will we inserted into
                        // the entry, not attached as attachments
                        if (name.equalsIgnoreCase("jTextAreaEntry")) {
                            droplocation = DROP_LOCATION_TEXTAREAENTRY;
                        } // check whether files have been dropped into attachment list
                        // in this case, we add the images as attachments, not inside
                        // the entry
                        else if (name.equalsIgnoreCase("jListLinks")) {
                            droplocation = DROP_LOCATION_LISTATTACHMENTS;
                        }
                    }
                }
                boolean skip = false;
                // continue here if we have dropped an URLs from the webbrowser
                if (tr.isDataFlavorSupported(urlFlavor)) {
                    // retrieve url
                    URL url = (URL) tr.getTransferData(urlFlavor);
                    // do we have a hyperlink?
                    if (FileOperationsUtil.isHyperlink(url.toString())) {
                        // else add the text to the keyword-list (JList)
                        linkListModel.addElement(url.toString());
                        // scroll jList down, so the new added links become visible
                        jListLinks.scrollRectToVisible(jListLinks.getCellBounds(linkListModel.size() - 1, linkListModel.size()));
                        // set the modified state
                        setModified(true);
                        skip = true;
                    }
                } 
                if (!skip) {
                    // retrieve list of dropped files
                    java.util.List files = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                    // check for valid values
                    if (files != null && files.size() > 0) {
                        // create list with final image files
                        List<File> imgfiles = new ArrayList<>();
                        // create list with final image files
                        List<File> anyfiles = new ArrayList<>();
                        // dummy
                        File file;
                        for (Object file1 : files) {
                            // get each single object from droplist
                            file = (File) file1;
                            // check whether it is a file
                            if (file.isFile()) {
                                // if it's an image, add it to image file list
                                if (FileOperationsUtil.isSupportedImageFile(file) && DROP_LOCATION_TEXTAREAENTRY == droplocation) {
                                    imgfiles.add(file);
                                } else {
                                    // if so, add it to list
                                    anyfiles.add(file);
                                }
                            }
                        }
                        // check if we have any valid values,
                        // i.e. image files have been dragged and dropped
                        // if so, insert img-tags
                        if (imgfiles.size() > 0) {
                            insertImages(imgfiles.toArray(new File[imgfiles.size()]));
                        }
                        // check if we have any valid values,
                        // i.e. any files have been dragged and dropped
                        // if so, insert attachments
                        if (anyfiles.size() > 0) {
                            insertAttachments(anyfiles.toArray(new File[anyfiles.size()]));
                        }
                    }
                }
                dtde.getDropTargetContext().dropComplete(true);
            } else {
                Constants.zknlogger.log(Level.WARNING, "DataFlavor.javaFileListFlavor or Data.Flavor.URL is not supported, rejected");
                dtde.rejectDrop();
            }
        } catch (IOException | UnsupportedFlavorException ex) {
            Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            dtde.rejectDrop();
        }
    }

    public class TableContentPane extends JPanel {

        public TableContentPane() {
            super(new GridLayout(1, 0));
            // create empty table header
            String[] headers = new String[16];
            for (int cnt = 0; cnt < 16; cnt++) {
                headers[cnt] = "";
            }
            // create tablemodel for the table data, which is not editable
            final DefaultTableModel model = new DefaultTableModel(headers, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            // prepare int-array that holds the utf-8-values for the signs and symbols
            int[] values = new int[736];
            // create advancing counter
            int counter = 0;
            // now fill the int-array with certain selected unicode-chars
            for (int cnt = 161; cnt <= 255; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 913; cnt <= 969; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 8592; cnt <= 8601; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }

            values[counter++] = 8704;
            values[counter++] = 8706;
            values[counter++] = 8707;
            values[counter++] = 8709;
            values[counter++] = 8711;
            values[counter++] = 8712;
            values[counter++] = 8713;
            values[counter++] = 8715;
            values[counter++] = 8719;
            values[counter++] = 8721;
            values[counter++] = 8722;
            values[counter++] = 8727;
            values[counter++] = 8730;
            values[counter++] = 8733;
            values[counter++] = 8734;
            values[counter++] = 8736;
            values[counter++] = 8743;
            values[counter++] = 8744;
            values[counter++] = 8745;
            values[counter++] = 8746;
            values[counter++] = 8747;
            values[counter++] = 8756;
            values[counter++] = 8764;
            values[counter++] = 8773;
            values[counter++] = 8776;
            values[counter++] = 8800;
            values[counter++] = 8804;
            values[counter++] = 8805;
            values[counter++] = 8834;
            values[counter++] = 8835;
            values[counter++] = 8836;
            values[counter++] = 8838;
            values[counter++] = 8839;
            values[counter++] = 8984;
            values[counter++] = 8718;
            values[counter++] = 8721;
            values[counter++] = 8730;
            values[counter++] = 8734;
            values[counter++] = 8617;
            values[counter++] = 8618;
            values[counter++] = 5167;
            values[counter++] = 5169;
            values[counter++] = 5171;
            values[counter++] = 5176;
            values[counter++] = 5196;
            values[counter++] = 5198;
            values[counter++] = 5200;
            values[counter++] = 5205;
            values[counter++] = 8266;
            values[counter++] = 8267;
            values[counter++] = 8481;
            values[counter++] = 8507;
            values[counter++] = 8212;
            values[counter++] = 8224;
            values[counter++] = 8226;
            values[counter++] = 8227;
            values[counter++] = 8230;
            values[counter++] = 8240;
            values[counter++] = 8241;
            values[counter++] = 8258;
            values[counter++] = 8264;
            values[counter++] = 8265;
            values[counter++] = 8968;
            values[counter++] = 8969;
            values[counter++] = 8970;
            values[counter++] = 8971;
            values[counter++] = 8997;
            values[counter++] = 8998;
            values[counter++] = 9001;
            values[counter++] = 9002;
            values[counter++] = 9003;
            values[counter++] = 9099;

            for (int cnt = 8624; cnt <= 8629; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 8644; cnt <= 8667; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 9312; cnt <= 9351; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 9728; cnt <= 9900; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 10136; cnt <= 10174; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 8531; cnt <= 8542; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 8853; cnt <= 8865; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }
            for (int cnt = 256; cnt <= 448; cnt++) {
                if (counter < values.length) {
                    values[counter++] = cnt;
                }
            }

            // now add all the values row by row to the table model
            counter = 0;
            for (int row = 0; row < 46; row++) {
                Object[] tablerow = new Object[16];
                for (int col = 0; col < 16; col++) {
                    tablerow[col] = String.valueOf((char) values[counter++]);
                }
                model.addRow(tablerow);
            }
            // create a table out of that model
            final JTable table = new JTable(model);
            // add mouse-listener, so a doubleclick inserts the sign
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (2 == evt.getClickCount()) {
                        String sign = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString();
                        // look for the component which has the focus and paste clipboard into it
                        replaceSelectionInFocus(sign);

                    }
                }
            });
            // create action for enter-key. by doing this instead of using a key-event, hitting
            // the enter-key does not select a new cell, so another symbol than the intended one
            // is inserted
            AbstractAction a_enter = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String sign = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString();
                    // look for the component which has the focus and paste clipboard into it
                    replaceSelectionInFocus(sign);
                }
            };
            table.getActionMap().put("EnterKeyPressed", a_enter);
            // associate enter-keystroke with that action
            KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
            table.getInputMap().put(ks, "EnterKeyPressed");
            // no table header
            table.setTableHeader(null);
            // grid-color
            table.setGridColor(settingsObj.getTableGridColor());
            // single cells can be selected, of course
            table.setCellSelectionEnabled(true);
            table.setFillsViewportHeight(true);
            // increase row height
            table.setRowHeight(table.getRowHeight() + 15);
            // increase font
            Font f = table.getFont();
            f = new Font(settingsObj.getMainfont(Settings.FONTNAME), f.getStyle(), f.getSize() + 10);
            table.setFont(f);
            // add a scrollpane
            JScrollPane scrollPane = new JScrollPane(table);
            // and add scrollpane to contentpanel
            add(scrollPane);
        }
    }

    private void replaceSelectionInFocus(String replacestring) {
        switch (focusowner) {
            case Constants.FOCUS_FIELD_TITLE:
                jTextFieldTitle.replaceSelection(replacestring);
                break;
            case Constants.FOCUS_FIELD_TEXT:
                jTextAreaEntry.replaceSelection(replacestring);
                break;
            case Constants.FOCUS_FIELD_AUTHOR:
                jTextAreaAuthor.replaceSelection(replacestring);
                break;
            case Constants.FOCUS_FIELD_REMARKS:
                jTextAreaRemarks.replaceSelection(replacestring);
                break;
        }
    }

    @Action
    public void insertManualTimestamp() {
        // create dateformat from user-settings...
        DateFormat df = new SimpleDateFormat(Constants.manualTimestamp[settingsObj.getManualTimestamp()]);
        // create string-timestamp
        String timestamp = df.format(new Date());
        // the timestamp can be inserted anywhere...
        replaceSelectionInFocus(timestamp);
    }

    @Action
    public void insertSymbol() {
        // check whether dialog is still open
        if (symbolsDlg != null) {
            // check whether dialog is visible
            if (symbolsDlg.isVisible()) {
                // if yes, bring it to front
                symbolsDlg.toFront();
            } else {
                // else dispose it.
                symbolsDlg.dispose();
                symbolsDlg = null;
            }
        }
        // if necesarry, create new diaolog
        if (null == symbolsDlg) {
            // create new dialog
            symbolsDlg = new JDialog(this);
            // dipose it on close action
            symbolsDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            // give it a title
            symbolsDlg.setTitle(resourceMap.getString("symbolsDialogTitle"));
            //Create and set up the content pane.
            TableContentPane newContentPane = new TableContentPane();
            //content panes must be opaque
            newContentPane.setOpaque(true);
            symbolsDlg.setContentPane(newContentPane);
            // give dialog a unique name
            symbolsDlg.setName("FormSymbols");
            //Display the window.
            symbolsDlg.pack();
            symbolsDlg.setLocationRelativeTo(this);
            symbolsDlg.setVisible(true);
        }
    }

    /**
     * This method removes double line separators from the selection of the text
     * field that currently has the input focus, and replaces them by single
     * line-separators
     */
    @Action(enabledProperty = "textSelected")
    public void removeDoubleLineSeparators() {
        // get separator char
        String sep = System.lineSeparator();
        // replace double line separators
        removeReplacement(sep + sep, " ");
        // in case someone copied a text with only a newline-char,
        // also try to remove these chars
        removeReplacement("\n\n", " ");
    }

    private void removeReplacement(String old, String replacewith) {
        // get the text from that textfield that has the focus
        // check which textarea has the input focus, so we can copy that component
        // into our variable - so we avoid code-duplication, and not using for each textarea
        // its own spell-checking...
        JTextArea ta = null;
        switch (focusowner) {
            case Constants.FOCUS_FIELD_TEXT:
                ta = jTextAreaEntry;
                break;
            case Constants.FOCUS_FIELD_AUTHOR:
                ta = jTextAreaAuthor;
                break;
            case Constants.FOCUS_FIELD_REMARKS:
                ta = jTextAreaRemarks;
                break;
        }
        // if one of the supposed textareas had the input-focus (that means "ta" is not
        // null), replace text now
        if (ta != null) {
            // get selected text of component
            String text = ta.getSelectedText();
            // if we have no selection, retrieve whole text and 
            // and select all text, so we can use "replaceSelection" to
            // insert the new text
            if (null == text) {
                // get content from textarea
                text = ta.getText();
                // select all content
                ta.selectAll();
            }
            // get selection range
            int selstart = ta.getSelectionStart();
            int selend = ta.getSelectionEnd();
            // get textlength befor replacement
            int length_before = text.length();
            // replace chars
            text = text.replace(old, replacewith);
            // get textlength afterreplacement
            int length_after = text.length();
            // set text back to field with focus
            ta.replaceSelection(text);
            // set the modified state
            setModified(true);
            // set back selection
            ta.setSelectionStart(selstart);
            ta.setSelectionEnd(selend - (length_before - length_after));
        }
    }

    /**
     * This method removes single line separators from the selection of the text
     * field that currently has the input focus
     */
    @Action(enabledProperty = "textSelected")
    public void removeSingleLineSeparators() {
        // get separator char
        String sep = System.lineSeparator();
        // repalce double line separators
        removeReplacement(sep, " ");
        // in case someone copied a text with only a newline-char,
        // also try to remove these chars
        removeReplacement("\n", " ");
    }

    /**
     * This method removes double space chars from the selection of the text
     * field that currently has the input focus, and replaces them by single
     * space chars
     */
    @Action(enabledProperty = "textSelected")
    public void removeDoubleSpaceChars() {
        // repalce double spaces
        removeReplacement("  ", " ");
    }

    /**
     * This method removes tabulator chars from the selection of the text field
     * that currently has the input focus
     */
    @Action(enabledProperty = "textSelected")
    public void removeTabChars() {
        // repalce tabs
        removeReplacement("\t", "");
    }

    @Action(enabledProperty = "textfieldFilled")
    public void replace() {
        if (null == findReplaceDlg || !findReplaceDlg.isVisible()) {
            // set input focus to main textfield
            jTextAreaEntry.requestFocusInWindow();
            // create a new dialog with the bigger edit-field, passing some initial values
            findReplaceDlg = new CFindReplaceDialog(this, jTextAreaEntry, settingsObj);
            // center window
            findReplaceDlg.setLocationRelativeTo(this);
        }
        // show window
        ZettelkastenApp.getApplication().show(findReplaceDlg);
    }

    /**
     * This method open a file-chooser dialog and lets the user choose
     * image-files, which will be inserted in the textfield. the
     * image-file-chooser gives the user a preview of the image. when an
     * image-file is chosen, the image-file is copied to an own image-directory
     * ("/img/") which is a subdirectory of the main-data-file's directory. if
     * the directory doesn't already exist, it will be created.
     */
    @Action(enabledProperty = "focus")
    public void insertImage() {
        // get the title for the file dialog and use it as function parameter
        JFileChooser fc = FileOperationsUtil.createFileChooser(resourceMap.getString("insertImageDialogTitle"),
                JFileChooser.FILES_ONLY,
                settingsObj.getLastOpenedImageDir(),
                // TODO andere Grafikformate später, wenn unterstützt
                // new String[] {".jpg",".jpeg",".bmp",".png",".gif",".tif",".tiff"},
                new String[]{".jpg", ".jpeg", ".png", ".gif"},
                resourceMap.getString("imageFileText"));
        //Add the preview pane.
        fc.setAccessory(new ImagePreview(fc));
        // enable multiple selection
        fc.setMultiSelectionEnabled(true);
        // show the dialog
        int option = fc.showOpenDialog(null);
        // if a file was chosen, set the filepath
        if (JFileChooser.APPROVE_OPTION == option) {
            insertImages(fc.getSelectedFiles());
        }
    }

    private void insertImages(File[] sources) {
        // store open and close tags for images
        String imgopen, imgclose;
        // check whether markdown is used
        if (settingsObj.getMarkdownActivated()) {
            imgopen = Constants.FORMAT_MD_IMG_OPEN;
            imgclose = Constants.FORMAT_MD_IMG_CLOSE;
        } else {
            imgopen = Constants.FORMAT_IMG_OPEN;
            imgclose = Constants.FORMAT_IMG_CLOSE;
        }
        // declare constants for moving/copying files
        final int ATT_COPY = 0;
        final int ATT_MOVE = 1;
        // files should remain in their original folder
        final int ATT_REMAIN = 2;
        // check whether we already have a saved data file or not. if not, we have no related
        // path for the subdirectory "img", thus we cannot copy the images
        if (null == settingsObj.getFilePath() || !settingsObj.getFilePath().exists()) {
            // display error message box
            JOptionPane.showMessageDialog(this, resourceMap.getString("noDataFileSavedMsg"), resourceMap.getString("noDataFileSavedTitle"), JOptionPane.PLAIN_MESSAGE);
            return;
        }
        // store the imagepath, needed now and for later use, see below
        // the image path always equals the filepath of the 
        String simagedir = settingsObj.getImagePath(dataObj.getUserImagePath(), false);
        // create new linked list that will contain a "cleaned" list of files, i.e. only contains
        // those selected files that haven't been copied to the attachment directory yet.
        LinkedList<File> newfiles = new LinkedList<>();
        // iterate array
        for (File cf : sources) {
            // first off all, let's check whether the user chose an already existing image
            // which already has been copied to the application's image directory. if so, no
            // new copy operation is needed thus we *exclude* that file from the copy-list,
            // but already *include* it to the jList with attach,ents...
            // if the source file does not start with the same string part as the application's
            // applications directory, we assume that the attachment has not been copied to that directory yet.
            if (!cf.toString().startsWith(simagedir)) {
                newfiles.add(cf);
            } else {
                // create a string buffer, which can be manipulated
                StringBuilder sb = new StringBuilder(cf.toString());
                // now delete everything of that filepath from the beginning to the image directory.
                // we add +1 one here, because the image directory string does not have the trailing
                // separator char, so this must also be deleted from the source-path
                sb.delete(0, simagedir.length() + 1);
                // now insert the open tag at the beginning of the file name
                sb.insert(0, imgopen);
                // and append the close tag at the end
                sb.append(imgclose);
                // and insert the string into the textfield
                jTextAreaEntry.replaceSelection(sb.toString());
            }
        }
        // if we don't have any new files that haven't been copied to the attachment-directory before,
        // we can leave the method now...
        if (newfiles.size() < 1) {
            return;
        }
        // else create new array with files to be copied.
        sources = newfiles.toArray(new File[newfiles.size()]);
        // now the program copies the image to an own sub-directory of the
        // application's directory. ask the user to conform this action
        // create a JOptionPane with yes/no/cancel options
        // create a JOptionPane with moce/copy/cancel options
        int msgOption = JOptionPane.showOptionDialog(this,
                resourceMap.getString("msgConfirmImageCopyMsg", simagedir),
                resourceMap.getString("msgConfirmImageCopyTitle"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{
                    resourceMap.getString("optionFileCopy"),
                    resourceMap.getString("optionFileMove"),
                    resourceMap.getString("optionFileRemain"),
                    resourceMap.getString("optionFileCancel"),},
                resourceMap.getString("optioneFileCopy"));
        // if the user wants to proceed, copy the image now
        if (ATT_COPY == msgOption || ATT_MOVE == msgOption) {
            // first, check whether we already have an image directory
            // create the file-object with the necessary directory path
            File imagedir = new File(simagedir);
            // if the directory does not exist, create it
            if (!imagedir.exists()) {
                // create directory
                try {
                    if (!imagedir.mkdir()) {
                        // if it fails, show warning message and leave method
                        // create a message string including the filepath of the directory
                        // which could not be created
                        JOptionPane.showMessageDialog(this, resourceMap.getString("errMsgCreateImgDirMsg", imagedir), resourceMap.getString("errMsgCreateDirTitle"), JOptionPane.PLAIN_MESSAGE);
                        return;
                    }
                } catch (SecurityException e) {
                    Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                    // if it fails, show warning message and leave method
                    // create a message string including the filepath of the directory
                    // which could not be created
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errMsgCreateImgDirMsg", imagedir), resourceMap.getString("errMsgCreateDirTitle"), JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }
            // go through all selected files
            for (File f : sources) {
                // store the fileextension for later use, see below
                String fileextension = FileOperationsUtil.getFileExtension(f);
                // create a new file variable from the destination string
                // retrieve the application's directory and add an "/img/" as subfolder for images
                // and append the file name of the image file. we need this string already now for the
                // message box to tell the user where the file will be copied to.
                File dest = new File(settingsObj.getImagePath(dataObj.getUserImagePath(), true) + f.getName());
                // check whether the file exists. if yes, the user should enter another name
                while (dest.exists()) {
                    // open an option dialog and let the user prompt a new filename
                    Object fnobject = JOptionPane.showInputDialog(this, resourceMap.getString("msgFileExists"), resourceMap.getString("msgFileExistsTitle"), JOptionPane.PLAIN_MESSAGE, null, null, dest.getName());
                    // if the user cancelled the dialog, quit method
                    if (null == fnobject) {
                        return;
                    }
                    // convert object to string
                    String newfilename = fnobject.toString();
                    // check whether the user just typed in a name without extension
                    // if so, add extension here
                    if (!newfilename.endsWith("." + fileextension)) {
                        newfilename = newfilename.concat("." + fileextension);
                    }
                    // and create a new file
                    dest = new File(settingsObj.getImagePath(dataObj.getUserImagePath(), true) + newfilename);
                }

                try {
                    // here we go when the user wants to *copy* the files
                    if (ATT_COPY == msgOption) {
                        // create and copy file...
                        dest.createNewFile();
                        // if we have a file which does not already exist, copy the source to the dest
                        FileOperationsUtil.copyFile(f, dest, 1024);
                    } // here we go when the user wants to *move* the files
                    else if (ATT_MOVE == msgOption) {
                        // if moving the file failed...
                        if (!f.renameTo(dest)) {
                            // ... show error msg
                            JOptionPane.showMessageDialog(this, resourceMap.getString("errMsgFileMove"), resourceMap.getString("errMsgFileMoveTitle"), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    // create a new string buffer
                    StringBuilder imagetag = new StringBuilder("");
                    // open the tag
                    imagetag.append(imgopen);
                    // add the filename to the copied imagefile
                    imagetag.append(dest.getName());
                    // close tag
                    imagetag.append(imgclose).append(System.lineSeparator());
                    // and insert the string into the textfield
                    jTextAreaEntry.replaceSelection(imagetag.toString());
                    // set the modified state
                    setModified(true);
                    // set new default directory
                    settingsObj.setLastOpenedImageDir(f);
                } catch (IOException ex) {
                    Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errMsgFileCopy"), resourceMap.getString("errMsgFileCopyTitle"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        } else if (ATT_REMAIN == msgOption) {
            // go through all selected files
            for (File f : sources) {
                // create a new string buffer
                StringBuilder imagetag = new StringBuilder("");
                // open the tag
                imagetag.append(imgopen);
                // add the filename to the copied imagefile
                imagetag.append(f.getPath());
                // close tag
                imagetag.append(imgclose).append(System.lineSeparator());
                // and insert the string into the textfield
                jTextAreaEntry.replaceSelection(imagetag.toString());
                // set the modified state
                setModified(true);
                // set new default directory
                settingsObj.setLastOpenedImageDir(f);
            }
        }
    }

    @Action(enabledProperty = "focus")
    public void insertForm() {
        // retrieve caret position
        int caret = jTextAreaEntry.getCaretPosition();
        // the button for editing the spellchecking-words was pressed,
        // so open the window for edting them...
        if (null == formEditDlg) {
            // get parent und init window
            formEditDlg = new CFormEditor(this, settingsObj);
            // center window
            formEditDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(formEditDlg);
        // if we have any changes, insert table
        if (formEditDlg.isModified()) {
            jTextAreaEntry.replaceSelection(formEditDlg.getFormTag());
        } // else set caret to old position and "de-select" the text
        else {
            jTextAreaEntry.setCaretPosition(caret);
        }
        // dispose window
        formEditDlg.dispose();
        formEditDlg = null;
    }

    @Action(enabledProperty = "focus")
    public void insertHyperlink() {
        // retrieve caret position
        int caret = jTextAreaEntry.getCaretPosition();
        // the button for editing the spellchecking-words was pressed,
        // so open the window for edting them...
        if (null == hyperlinkEditDlg) {
            // get parent und init window
            hyperlinkEditDlg = new CInsertHyperlink(this, jTextAreaEntry.getSelectedText(), settingsObj);
            // center window
            hyperlinkEditDlg.setLocationRelativeTo(this);
        }
        ZettelkastenApp.getApplication().show(hyperlinkEditDlg);
        // retrieve return value
        String hyperlink = hyperlinkEditDlg.getHyperlink();
        // if we have any changes, insert table
        if (hyperlink != null && !hyperlink.isEmpty()) {
            jTextAreaEntry.replaceSelection(hyperlink);
        } // else set caret to old position and "de-select" the text
        else {
            jTextAreaEntry.setCaretPosition(caret);
        }
        // dispose window
        hyperlinkEditDlg.dispose();
        hyperlinkEditDlg = null;
    }

    /**
     * Opens a file chooser to insert attachments from harddisk to the currently
     * edited entry. Attachments' filepaths will be used and relative path will
     * be calculated if necessary, and the filepaths will be added to the
     * attachment list.
     */
    @Action
    public void insertAttachment() {
        // get the title for the file dialog and use it as function parameter
        JFileChooser fc = FileOperationsUtil.createFileChooser(resourceMap.getString("insertAttachmentDialogTitle"),
                JFileChooser.FILES_ONLY,
                settingsObj.getLastOpenedAttachmentDir(),
                null,
                null);
        // enable multiple selection
        fc.setMultiSelectionEnabled(true);
        // open dialog
        int option = fc.showOpenDialog(null);
        // if a file was chosen, set the filepath
        if (JFileChooser.APPROVE_OPTION == option) {
            insertAttachments(fc.getSelectedFiles());

        }
    }

    /**
     * Inserts all attachments that have been chosen by the
     * {@link #insertAttachment()} method to the {@link #linkListModel}.
     *
     * @param sources A list of files chosen via the {@link #insertAttachment()}
     * method.
     */
    private void insertAttachments(File[] sources) {
        // insert attachments
        boolean mod = FileOperationsUtil.insertAttachments(dataObj, settingsObj, this, sources, linkListModel);
        // check for modifications
        if (mod) {
            // switch mod flag
            setModified(true);
            // get updated model
            linkListModel = FileOperationsUtil.getListModel();
            // check whether we have any entries in the list
            if (linkListModel.size() > 0) {
                // scroll jList down, so the new added links become visible
                jListLinks.scrollRectToVisible(jListLinks.getCellBounds(linkListModel.size() - 1, linkListModel.size()));
            }
        }
    }

    /**
     * filters the keywords list. retrieves the textinpur from the textfield and
     * removes all entries that don't have the entered text as part of their
     * string.
     */
    @Action
    public void filterKeywords() {
        // retrieve the filtertext
        String filter = jTextFieldFilterKeywordlist.getText();
        // when empty or null, leave
        if (null == filter || filter.isEmpty()) {
            return;
        }
        // tell selection listener we are working and selection listener should not react on changes now
        listUpdateActive = true;
        // we don't want case sensitive search here
        filter = filter.toLowerCase();
        // retrieve the list model
        ListModel lm = jListQuickInputKeywords.getModel();
        // create new linked list that will contain the filtered elements
        LinkedList<String> list = new LinkedList<>();
        // iterate the listmodel
        for (int cnt = 0; cnt < lm.getSize(); cnt++) {
            // retrieve the listitem and make it lowercase
            String item = lm.getElementAt(cnt).toString().toLowerCase();
            // if it matches the filter term, keep it
            // put item to the array
            if (item.contains(filter)) {
                list.add(lm.getElementAt(cnt).toString());
            }
        }
        // set new listdata
        jListQuickInputKeywords.setListData(list.toArray());
        jListQuickInputKeywords.revalidate();
        // and enable the refresh button
        jTextFieldFilterKeywordlist.setText("");
        jButtonRefreshKeywordlist.setEnabled(true);
        // when we don't have quickinput, we need to set this value to false
        // otherwise the task to create/refresh the list won't start
        if (!settingsObj.getQuickInput()) {
            keywordsListUpToDate = false;
        }
        // work done...
        listUpdateActive = false;
    }

    /**
     * filters the author list. retrieves the textinpur from the textfield and
     * removes all entries that don't have the entered text as part of their
     * string.
     */
    @Action
    public void filterAuthors() {
        // retrieve the filtertext
        String filter = jTextFieldFilterAuthorlist.getText();
        // when empty or null, leave
        if (null == filter || filter.isEmpty()) {
            return;
        }
        // tell selection listener we are working and selection listener should not react on changes now
        listUpdateActive = true;
        // we don't want case sensitive search here
        filter = filter.toLowerCase();
        // iterate list model from last to first entry
        // we do this because removing the later entries first does not change
        // the index number from "lower" entries, so we avoid counting up to a
        // size which elements in fact no longer exist, because some elements have already
        // been removed (which decreases the model's size).
        for (int cnt = quickInputAuthorListModel.getSize() - 1; cnt >= 0; cnt--) {
            // retrieve the listitem and make it lowercase
            String item = quickInputAuthorListModel.getElementAt(cnt).toString().toLowerCase();
            // if it matches the filter term, keep it
            // put item to the array
            if (!item.contains(filter)) {
                quickInputAuthorListModel.remove(cnt);
            }
        }
        // and enable the refresh button
        jTextFieldFilterAuthorlist.setText("");
        jButtonRefreshAuthorlist.setEnabled(true);
        // work done
        listUpdateActive = false;
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
            if (JOptionPane.CANCEL_OPTION == option || JOptionPane.CLOSED_OPTION == option /*User pressed cancel key*/) {
                return;
            }
            // if save is requested, save changes
            if (JOptionPane.YES_OPTION == option) {
                // check whether we hav text in the maintextfield
                String cont = jTextAreaEntry.getText();
                // if no text/content entered, tell user that entry needs to have content
                // and return...
                if (cont.isEmpty()) {
                    JOptionPane.showMessageDialog(this, resourceMap.getString("errMsgNoContentMsg"), resourceMap.getString("errMsgNoContentTitle"), JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                // save the data
                if (!saveEntry()) {
                    return;
                }
            }
            // reset modified value, so we can check in our mainframe
            // whether we have changes or not which should be updated to the display
            if (JOptionPane.NO_OPTION == option) {
                modified = false;
            }
        }
        // tell mainframe that it has to update the content
        mainframe.editFinishedEvent();
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. New Entry Window closed.");
        }
        // and close the window
        dispose();
        setVisible(false);
    }

    @Action
    public void cancel() {
        closeWindow();
    }

    /**
     * This method is called when the user presses the "Apply" button to save
     * the changes. We have an extra method for this to enable/disable the
     * apply-button depending on whether we have text inside the main textfield.
     * An entry must always have content-text, otherwise it is considered as
     * "deleted".
     */
    @Action(enabledProperty = "textfieldFilled")
    public void applyChanges() {
        // when the user wants to apply the changes, save the entry
        if (!saveEntry()) {
            return;
        }
        // tell mainframe that it has to update the content
        mainframe.editFinishedEvent();
        // check whether memory usage is logged. if so, tell logger that new entry windows was opened
        if (settingsObj.isMemoryUsageLogged) {
            // log info
            Constants.zknlogger.log(Level.INFO, "Memory usage logged. New Entry Window closed.");
        }
        // and close the window    
        dispose();
        setVisible(false);
    }

    /**
     * This method retrieves the data from the textfields and adds a new entry
     * respectively changes an entry which was edited. This method is called
     * when the user presses the ok-button to finish the data-entry, closing the
     * dialog and savign the entry to the data-file.
     */
    private boolean saveEntry() {
        // counter for loops
        int cnt;
        // retrieve keywords
        // init keyword-stringarray
        String[] keywords = new String[keywordListModel.getSize()];
        // iterate keywordarray
        for (cnt = 0; cnt < keywords.length; cnt++) {
            keywords[cnt] = keywordListModel.get(cnt).toString();
        }
        // ask the user if he wants to replace possible keywords, which appear as synonyms, but *not*
        // as index-word, with the related index-words...
        keywords = Tools.replaceSynonymsWithKeywords(synonymsObj, keywords);
        // check for cancel-action
        if (null == keywords) {
            return false;
        }
        // get system line separator
        String linesep = System.lineSeparator();
        // retrieve the title
        String title = jTextFieldTitle.getText();
        // retrieve the content
        String content = jTextAreaEntry.getText();
        // and re-convert all new lines to br's. this is necessary for converting
        // them into <br>'s because the entry is displayed as html-content. simple
        // new lines without "<br>" command would not be shown as new lines
        //
        // but first, we habe to remove all carriage-returns (\r), which are part of the
        // line-seperator in windows. somehow, the replace-command does *not* work, when
        // we replace "System.lineSeparator()" with "[br]", but only when
        // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
        // "\r", it is replaced by nothing, to clean the content.
        content = Tools.replaceUnicodeToUbb(content);
        // now we have to "clean" the lists. since each bullet point starts for optical reasons
        // in a new line, but in HTML-conversion this would mean additional <br>-tags within and
        // unorder list (<ul> and <li>), we remove all [br]-tags between the list tags.
        content = content.replace(Constants.FORMAT_NUMBEREDLIST_OPEN + Constants.FORMAT_NEWLINE, Constants.FORMAT_NUMBEREDLIST_OPEN);
        content = content.replace(Constants.FORMAT_NUMBEREDLIST_CLOSE + Constants.FORMAT_NEWLINE, Constants.FORMAT_NUMBEREDLIST_CLOSE);
        content = content.replace(Constants.FORMAT_LIST_OPEN + Constants.FORMAT_NEWLINE, Constants.FORMAT_LIST_OPEN);
        content = content.replace(Constants.FORMAT_LIST_CLOSE + Constants.FORMAT_NEWLINE, Constants.FORMAT_LIST_CLOSE);
        content = content.replace(Constants.FORMAT_LISTITEM_CLOSE + Constants.FORMAT_NEWLINE, Constants.FORMAT_LISTITEM_CLOSE);
        // check whether all chars are legal JDOM-chars. when copied text from clipboard,
        // it might be that the text contains illegal JDOM chars...
        content = Tools.isValidJDOMChars(content);
        // retrieve the authors
        String authordummy = jTextAreaAuthor.getText();
        // create string array for authors
        ArrayList<String> authors = new ArrayList<>(); 
        // if we have any authors...
        if (!authordummy.isEmpty()) {
            // see comment above for this line...
            if (linesep.contains("\r")) {
                authordummy = authordummy.replace("\r", "");
            }
            // ...parse them to an string array
            authors.addAll(Arrays.asList(authordummy.split("\n")));
        }
        // get all know author index numbers
        LinkedList<Integer> knownauthors = new LinkedList<>();
        for (String a : authors) {
            // find author position
            int ap = dataObj.getAuthorPosition(a);
            if (ap != -1) {
                knownauthors.add(ap);
            }
        }
        // find all authos that have been referenced by footnotes
        LinkedList<Integer> referredauthors = Tools.getFootnoteIDs(dataObj, content);
        // now match given authors with referred authors. if user has referenced authors
        // with footnotes, but did not add these authors to the reference list,
        // we now add them automatically
        for (int rai : referredauthors) {
            // no match?
            if (-1 == knownauthors.indexOf(rai)) {
                // get author
                String au = dataObj.getAuthor(rai);
                // add to authors
                if (au != null && !au.isEmpty()) {
                    authors.add(au);
                }
            }
        }
        // retrieve the remarks
        String remarks = jTextAreaRemarks.getText();
        // and re-convert all new lines to br's. this is necessary for converting
        // them into <br>'s because the entry is displayed as html-content. simple
        // new lines without "<br>" command would not be shown as new lines
        //
        // but first, we habe to remove all carriage-returns (\r), which are part of the
        // line-seperator in windows. somehow, the replace-command does *not* work, when
        // we replace "System.lineSeparator()" with "[br]", but only when
        // a "\n" is replaced by [br]. So, in case the system's line-separator also contains a
        // "\r", it is replaced by nothing, to clean the content.
        remarks = Tools.replaceUnicodeToUbb(remarks);
        // retrieve hyperlinks
        // init links-stringarray
        String[] links = new String[linkListModel.getSize()];
        // iterate linkarray
        for (cnt = 0; cnt < links.length; cnt++) {
            links[cnt] = linkListModel.get(cnt).toString();
        }

        if (isEditMode()) {
            // change entry and fetch result
            if (!dataObj.changeEntry(title, content, authors.toArray(new String[authors.size()]), keywords, remarks, links, Tools.getTimeStamp(), entryNumber)) {
                JOptionPane.showMessageDialog(this,
                        resourceMap.getString("errMsgChangeEntry"),
                        resourceMap.getString("errMsgChangeEntryTitle"),
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                // tell about success
                Constants.zknlogger.log(Level.INFO, "Entry's changes applied.");
            }
        } else {
            // here we have the user editing an "deleted" entry (empty place in the XML-document),
            // so the variable "entryNumber" indicates where in the *database* the entry should be stored.
            if (isDeleted) {
                // add entry, and fetch result
                int result = dataObj.addEntry(title,
                        content,
                        authors.toArray(new String[authors.size()]),
                        keywords,
                        remarks,
                        links,
                        Tools.getTimeStamp(),
                        -1,
                        true,
                        entryNumber);
                // check whether result was an error when adding a follower-entry (trailing entry(
                if (result == Daten.ADD_LUHMANNENTRY_ERR) {
                    JOptionPane.showMessageDialog(this,
                            resourceMap.getString("errMsgInsertEntry"),
                            resourceMap.getString("errMsgInsertEntryTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } // else check whether an error occured when adding a new entry
                else if (result == Daten.ADD_ENTRY_ERR) {
                    JOptionPane.showMessageDialog(this,
                            resourceMap.getString("errMsgAddEntry"),
                            resourceMap.getString("errMsgAddEntryTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } else {
                    // tell about success
                    Constants.zknlogger.log(Level.INFO, "New entry saved.");
                }
            } // here we have the user adding a new entry at the end of the XML-document.
            // In this case, the variable "entryNumber" indicates whether we have a trailing entry or not.
            else {
                // if we don't have to insert an entry here, indicate that by setting the
                // entryNumber of the current entry to -1;
                if (!luhmann) {
                    entryNumber = -1;
                }
                // else, we pass the number of the current entry just before the user clicked "insertEntry"
                // to the addEntry-method, indicating that the index-number of the added entry should be
                // included in the entry "entryNumber" luhmann-tag (that indicates follower- and sub-entrties).
                int result = dataObj.addEntry(title,
                        content,
                        authors.toArray(new String[authors.size()]),
                        keywords,
                        remarks,
                        links,
                        Tools.getTimeStamp(),
                        entryNumber);
                // check whether result was an error when adding a follower-entry (trailing entry(
                if (result == Daten.ADD_LUHMANNENTRY_ERR) {
                    JOptionPane.showMessageDialog(this,
                            resourceMap.getString("errMsgInsertEntry"),
                            resourceMap.getString("errMsgInsertEntryTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } // else check whether an error occured when adding a new entry
                else if (result == Daten.ADD_ENTRY_ERR) {
                    JOptionPane.showMessageDialog(this,
                            resourceMap.getString("errMsgAddEntry"),
                            resourceMap.getString("errMsgAddEntryTitle"),
                            JOptionPane.PLAIN_MESSAGE);
                } else {
                    // tell about success
                    Constants.zknlogger.log(Level.INFO, "New entry saved.");
                }
            }
        }
        return true;
    }

    @Action(enabledProperty = "focus")
    public void addKeywordFromSelection() {
        // retrieve selected text
        String selection = jTextAreaEntry.getSelectedText();
        // check whether we have any selection at all
        if (selection != null && !selection.isEmpty()) {
            addKeywords(selection.trim().split("\n"), true);
        }
    }

    @Action(enabledProperty = "focus")
    public void addTitleFromSelection() {
        // retrieve selected text
        String selection = jTextAreaEntry.getSelectedText();
        // check whether we have any selection at all
        if (selection != null && !selection.isEmpty()) {
            jTextFieldTitle.setText(selection.trim());
        }
    }

    @Action(enabledProperty = "textfieldFilled")
    public void addTitleFromFirstLine() {
        // retrieve selected text
        String text = jTextAreaEntry.getText();
        // check whether we have any selection at all
        if (text != null && text.length() > 0) {
            jTextFieldTitle.setText(text.split("\n")[0].trim());
        }
    }

    /**
     * This method retrieves the text from the keyword input field
     * ({@link #jTextFieldAddKeyword jTextFieldAddKeyword}) and adds it to the
     * keyword-list {@link #jListKeywords jListKeywords}.
     */
    @Action
    public void addKeyword() {
        // get the text from the input field
        String kw = jTextFieldAddKeyword.getText();
        // if no text entered, leave method and do nothing
        if (kw.isEmpty()) {
            return;
        }
        // and add keywords
        addKeywords(new String[]{kw}, false);
    }

    /**
     * This action retrieves the keywords of the currently displayed entry from
     * the main frame and adds them as keywords to the currently edited entry,
     * i.e. these values are added to the {@link #jListKeywords jListKeywords}.
     */
    @Action
    public void retrieveKeywordsFromDisplayedEntry() {
        addKeywords(dataObj.getKeywords(mainframe.displayedZettel, true), false);
    }

    /**
     * This method adds keywords that are passed as string-array to the
     * keyword-list of the edited entry, i.e. these values are addes to the
     * {@link #jListKeywords jListKeywords}.
     *
     * @param kws the keyword-values that should be added to the
     * {@link #jListKeywords jListKeywords}.
     * @param focusToMainfield {@code true} if the
     * {@link #jTextAreaEntry jTextAreaEntry} should retrieve the input focus
     * after adding the keywords, {@code false} if the
     * {@link #jTextFieldAddKeyword jTextFieldAddKeyword} should become the
     * focus-owner after adding the keywords.
     */
    private void addKeywords(String[] kws, boolean focusToMainfield) {
        // check for valid values
        if (null == kws || kws.length < 1) {
            return;
        }
        // track changes
        boolean kwadded = false;
        // iterare all keywords
        for (String kw : kws) {
            // trim keyword
            kw = kw.trim();
            // now check for double entries. returns false if keyword does not exist.
            if (!kw.isEmpty() && !isDoubleKeywords(kw)) {
                // add the text to the keyword-list (JList)
                keywordListModel.addElement((String) kw);
                kwadded = true;
            }
        }
        // only make changes when something was added
        if (kwadded) {
            // scroll jList down, so the new added keywords become visible
            jListKeywords.scrollRectToVisible(jListKeywords.getCellBounds(keywordListModel.size() - 1, keywordListModel.size()));
            // set the modified state
            setModified(true);
            // and clear the input-fields for further entries
            jTextFieldAddKeyword.setText("");
            // set input-focus to requested textfield
            if (focusToMainfield) {
                jTextAreaEntry.requestFocusInWindow();
            } else {
                jTextFieldAddKeyword.requestFocusInWindow();
            }
        }
    }

    /**
     * This method checks a given string if it is already in the author
     * textfield, i.e. it checks whether an author was already assigned to that
     * entry. If the given string is found within the author-textfield, this
     * method returns true, false otherwise.
     *
     * @param kw (the author which should be looked for)
     * @return {@code true} when the author already exists, false otherwise
     */
    private boolean checkForDoubleAuthors(String au) {
        // retrieve the text from the author textfield
        String text = jTextAreaAuthor.getText();
        // remove all carriage-returns
        if (System.lineSeparator().contains("\r")) {
            text = text.replace("\r", "");
        }
        // split into single authors
        String[] aus = text.split("\n");
        // check whether we have any authors at all
        if ((aus != null) && (aus.length > 0)) {
            // iterate the author array
            for (String eachauthor : aus) {
                // if we found the author-parameter, return true
                if (eachauthor.equalsIgnoreCase(au)) {
                    return true;
                }
            }
        }
        // no matching author found
        return false;
    }

    /**
     * This method checks a given string if it is already in the keywordlist,
     * i.e. it checks whether a keyword was already assigned to that entry. If
     * the given string is found within the keywordlist, this method returns
     * true, false otherwise.
     *
     * @param kw (the keyword which should be looked for)
     * @return {@code true} when the keyword already exists, false otherwise
     */
    private boolean isDoubleKeywords(String kw) {
        // when the list is empty, no double keyword can be found
        if (keywordListModel.size() < 1) {
            return false;
        }
        // iterate the array
        for (int cnt = 0; cnt < keywordListModel.size(); cnt++) {
            // if keyword already exists, return true
            if (kw.equalsIgnoreCase(keywordListModel.get(cnt).toString())) {
                return true;
            }
        }
        // if keyword doesn't exist, return false
        return false;
    }

    /**
     * This methods looks for selected entries in the keywordlist and removes
     * them from it
     */
    private void removeKeywordFromList() {
        // get index of first selected item
        int[] indices = jListKeywords.getSelectedIndices();
        // if no item is selected, do nothing
        if (indices.length < 1) {
            return;
        }
        /**
         * Neu ab hier ab Version 3.1.2
         */
        boolean displayUpdated = false;
        // iterate selected values and add them back to the quick-keyword-list
        for (int in : indices) {
            // get selected value
            String kw = keywordListModel.get(in).toString();
            // if selected keyword is in selected list, remove it from there
            if (settingsObj.getQuickInput() && selectedKeywords != null && selectedKeywords.size() > 0 && selectedKeywords.contains(kw)) {
                selectedKeywords.remove(kw);
            }
            // if applied keyword from keyword-display-list
            if (displayedKeywordList != null && !displayedKeywordList.contains(kw)) {
                // add keyword back to display list
                displayedKeywordList.add(kw);
                // indicate changes
                displayUpdated = true;
            }
        }
        // check whether changes to the list have been made, that should be updated
        if (displayUpdated) {
            // sort list
            Collections.sort(displayedKeywordList, new Comparer());
            // set new listdata without applied keywords
            jListQuickInputKeywords.setListData(displayedKeywordList.toArray());
            // disable the refresh and filter buttons
            jButtonRefreshKeywordlist.setEnabled(false);
        }
        /**
         * Neu bis hier ab Version 3.1.2
         */
        // than remove all selected items. since the index numbers of higher
        // items change, when a lower item in the listorder is removed, we have
        // to iterate the array backwards.
        for (int cnt = indices.length - 1; cnt >= 0; cnt--) {
            keywordListModel.remove(indices[cnt]);
        }
        // set the modified state
        setModified(true);
    }

    /**
     * This method retrieves the text from the input field and adds it to the
     * JList
     */
    @Action
    public void addLink() {
        // get the text from the input field
        String kw = jTextFieldAddLink.getText();
        // if no text entered, leave method and do nothing
        if (kw.isEmpty()) {
            return;
        }
        // else add the text to the keyword-list (JList)
        linkListModel.addElement((String) kw);
        // scroll jList down, so the new added links become visible
        jListLinks.scrollRectToVisible(jListLinks.getCellBounds(linkListModel.size() - 1, linkListModel.size()));
        // set the modified state
        setModified(true);
        // and clear the input-fields for further entries
        jTextFieldAddLink.setText("");
        jTextFieldAddLink.requestFocusInWindow();
    }

    /**
     * This methods looks for selected entries in the linklist and removes them
     * from it
     */
    private void removeLinkFromList() {
        // get index of first selected item
        int[] indices = jListLinks.getSelectedIndices();
        // if no item is selected, do nothing
        if (indices.length < 1) {
            return;
        }
        // than remove all selected items. since the index numbers of higher
        // items change, when a lower item in the listorder is removed, we have
        // to iterate the array backwards.
        for (int cnt = indices.length - 1; cnt >= 0; cnt--) {
            linkListModel.remove(indices[cnt]);
        }
        // set the modified state
        setModified(true);
    }

    /**
     * This method retrieves the selected item from the quickinput-author list
     * and sets it to the textarea with the author information. This method is
     * e.g. called from the mouseclick- or keypressed-event from the
     * quickinput-jlist-component
     */
    @Action(enabledProperty = "quickKeywordSelected")
    public void addQuickKeywordToList() {
        // when we have no selected keywords yet, create list now...
        if (settingsObj.getQuickInput() && (null == selectedKeywords)) {
            selectedKeywords = new LinkedList<>();
        }
        // retrieve all selected keywords
        List<String> o = jListQuickInputKeywords.getSelectedValuesList();
        // if we have selections, go on
        if (!o.isEmpty()) {
            // go through all selected values
            for (int cnt = o.size() - 1; cnt >= 0; cnt--) {
                // get each keyword string
                String kw = o.get(cnt);
                // check whether keyword already exisrs in the jlist
                if (!isDoubleKeywords(kw)) {
                    // if not, add keyword to listmodel
                    keywordListModel.addElement((String) kw);
                    // add selections to our linked list
                    if (settingsObj.getQuickInput()) {
                        selectedKeywords.add(kw);
                    }
                    // remove applied keyword from keyword-display-list
                    if (displayedKeywordList.contains(kw)) {
                        displayedKeywordList.remove(kw);
                    }
                    // set the modified state
                    setModified(true);
                }
                // scroll jList down, so the new added keywords become visible
                jListKeywords.scrollRectToVisible(jListKeywords.getCellBounds(keywordListModel.size() - 1, keywordListModel.size()));
                // clear selection
                jListQuickInputKeywords.clearSelection();
                // set new listdata without applied keywords
                jListQuickInputKeywords.setListData(displayedKeywordList.toArray());
                // disable the refresh and filter buttons
                jButtonRefreshKeywordlist.setEnabled(false);
            }
        }
    }

    @Action
    public void showNextQuickInputStep() {
        stepcounter++;
        keywordsListUpToDate = false;
        showKeywords();
    }

    @Action
    public void toggleQuickInput() {
        settingsObj.setQuickInput(!settingsObj.getQuickInput());
        jButtonQuickKeyword.setVisible(settingsObj.getQuickInput());
        jCheckBoxQuickInput.setSelected(settingsObj.getQuickInput());
        // update the segment-button-appearance
        jButtonAddKeywords.putClientProperty("JButton.segmentPosition", (settingsObj.getQuickInput()) ? "first" : "only");
        stepcounter = 1;
        keywordsListUpToDate = false;
        // if the keyword-tab is visible, refresh the view
        showKeywords();
    }

    /**
     * This method retrieves the selected item from the quickinput-author list
     * and sets it to the textarea with the author information. This method is
     * e.g. called from the mouseclick- or keypressed-event from the
     * quickinput-jlist-component
     */
    @Action(enabledProperty = "authorSelected")
    public void addQuickAuthorToList() {
        // retrieve all selected authors
        List<String> o = jListQuickInputAuthor.getSelectedValuesList();
        // if we have selections, go on
        if (!o.isEmpty()) {
            for (String au : o) {
                // check whether author already exisrs in textfield
                if (!checkForDoubleAuthors(au)) {
                    // if not, append author string
                    // therefore, add a new line, but only if the textfield is not empty
                    // (i.e. we already have an author)
                    if (!jTextAreaAuthor.getText().isEmpty()) {
                        jTextAreaAuthor.append(System.lineSeparator());
                    }
                    jTextAreaAuthor.append(au);
                    // set the modified state
                    setModified(true);
                }
            }
        }
    }

    /**
     * A background task which creates the author list
     *
     * @return
     */
    @Action
    public Task quickInputAuthor() {
        // disable tabpane during background task operations
        jTabbedPaneNewEntry1.setEnabled(false);

        return new QuickInputAuthorTask(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class));
    }

    private class QuickInputAuthorTask extends org.jdesktop.application.Task<Object, Void> {

        LinkedList<String> taskauthorlist = null;

        QuickInputAuthorTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            // tell programm that task is running
            qiAuthorTaskIsRunning = true;
            // tell selection listener we are working and selection listener should not react on changes now
            listUpdateActive = true;
            // get the amount of keywords
            int count = dataObj.getCount(Daten.AUCOUNT);
            int cnt;
            // create new list that will contain the array, but without
            // possible empty elements. when an author or keyword is deleted,
            // its content is just cleared, the element itself is not removed. we do
            // this to have always the same indexnumber for a keyword or an author.
            // now we copy the array to a linked list, leaving out empty elements
            taskauthorlist = new LinkedList<>();
            // go through all keywords of the keyword datafile
            for (cnt = 0; cnt < count; cnt++) {
                // get the author as string and add it to list
                String au = dataObj.getAuthor(cnt + 1);
                // if author is not empty, add it.
                if (!au.isEmpty()) {
                    taskauthorlist.add(au);
                }
            }
            // sort array
            Collections.sort(taskauthorlist, new Comparer());
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            //
            // check whether we have any entries at all...
            if (taskauthorlist != null) {
                // clear listmodel
                quickInputAuthorListModel.clear();
                // create iterator for the found authors
                Iterator<String> i = taskauthorlist.iterator();
                // go through linked list and add all authors to the listmodel
                try {
                    while (i.hasNext()) {
                        quickInputAuthorListModel.addElement(i.next());
                    }
                } catch (ConcurrentModificationException e) {
                    // reset the table when we have overlappings threads
                    quickInputAuthorListModel.clear();
                }
            }
            // enable tabpane when task has succeeded
            jTabbedPaneNewEntry1.setEnabled(true);
            // clear the filter-textfield
            jTextFieldFilterAuthorlist.setText("");
            // disable the refresh and filter buttons
            jButtonRefreshAuthorlist.setEnabled(false);
            // set upto-date state to true, so we don't have to start the task more often
            // than needed
            authorListUpToDate = true;
        }

        @Override
        protected void finished() {
            super.finished();
            // enabled input fields if necessary...
            jTextFieldFilterAuthorlist.setEnabled(quickInputAuthorListModel.getSize() > 0);
            // in case we have added new authors via the menu to the author list
            // the new values should be automatically selected.
            selectNewAddedAuthors();
            // tell program that task is over
            qiAuthorTaskIsRunning = false;
            // work done
            listUpdateActive = false;
        }
    }

    /**
     * Starts the background task which creates the keywordlist
     *
     * @return
     */
    @Action
    public Task quickInputKeywords() {
        // disable tabpane during background task operations
        jTabbedPaneNewEntry1.setEnabled(false);
        return new QuickInputKeywordsTask(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class));
    }

    private class QuickInputKeywordsTask extends org.jdesktop.application.Task<Object, Void> {

        QuickInputKeywordsTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to createLinksTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            // when the list is already up to date, do nothing
            if (keywordsListUpToDate) {
                // enable tabpane during background task operations
                jTabbedPaneNewEntry1.setEnabled(true);
                return null;
            }
            // tell programm the task is running
            qiKeywordTaskIsRunning = true;
            // tell selection listener we are working and selection listener should not react on changes now
            listUpdateActive = true;
            // get the amount of keywords
            int count = dataObj.getCount(Daten.KWCOUNT);
            int cnt;
            // create new list that will contain the array, but without
            // possible empty elements. when an author or keyword is deleted,
            // its content is just cleared, the element itself is not removed. we do
            // this to have always the same indexnumber for a keyword or an author.
            // now we copy the array to a linked list, leaving out empty elements
            displayedKeywordList = new LinkedList<>();
            // go through all keywords of the keyword datafile
            for (cnt = 0; cnt < count; cnt++) {
                // get the keyword as string and add them to the array
                String kw = dataObj.getKeyword(cnt + 1);
                // if keyword is not empry, add it to liszt
                if (!kw.isEmpty()) {
                    displayedKeywordList.add(kw);
                }
            }
            // sort list
            Collections.sort(displayedKeywordList, new Comparer());

            return null;
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            //
            // and apply the data to that list
            jListQuickInputKeywords.setListData(displayedKeywordList.toArray());
            // update list
            jListQuickInputKeywords.revalidate();
            // clear the filter-textfield
            jTextFieldFilterKeywordlist.setText("");
            // disable the refresh and filter buttons
            jButtonRefreshKeywordlist.setEnabled(false);
            // enable tabpane when task has succeeded
            jTabbedPaneNewEntry1.setEnabled(true);
            // set upto-date state to true, so we don't have to start the task more often
            // than needed
            keywordsListUpToDate = true;
        }

        @Override
        protected void finished() {
            super.finished();
            // enable textfield
            int listsize = jListQuickInputKeywords.getModel().getSize();
            jTextFieldFilterKeywordlist.setEnabled(listsize > 0);
            // disable "add"-button
            setQuickKeywordSelected(false);
            // tell programm that task is over
            qiKeywordTaskIsRunning = false;
            // work done
            listUpdateActive = false;
        }
    }

    /**
     * This variable indicates whether the main textfield has the focus or not.
     * dependent on that, most toolbar/menu-actions are being enabled or
     * disabled.
     */
    private boolean focus = false;

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean b) {
        boolean old = isFocus();
        this.focus = b;
        firePropertyChange("focus", old, isFocus());
    }

    /**
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
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
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
     */
    private boolean segmentPossible = false;

    public boolean isSegmentPossible() {
        return segmentPossible;
    }

    public void setSegmentPossible(boolean b) {
        boolean old = isSegmentPossible();
        this.segmentPossible = b;
        firePropertyChange("segmentPossible", old, isSegmentPossible());
    }

    /**
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
     */
    private boolean authorSelected = false;

    public boolean isAuthorSelected() {
        return authorSelected;
    }

    public void setAuthorSelected(boolean b) {
        boolean old = isAuthorSelected();
        this.authorSelected = b;
        firePropertyChange("authorSelected", old, isAuthorSelected());
    }

    /**
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
     */
    private boolean keywordSelected = false;

    public boolean isKeywordSelected() {
        return keywordSelected;
    }

    public final void setKeywordSelected(boolean b) {
        boolean old = isKeywordSelected();
        this.keywordSelected = b;
        firePropertyChange("keywordSelected", old, isKeywordSelected());
    }

    /**
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
     */
    private boolean quickKeywordSelected = false;

    public boolean isQuickKeywordSelected() {
        return quickKeywordSelected;
    }

    public void setQuickKeywordSelected(boolean b) {
        boolean old = isQuickKeywordSelected();
        this.quickKeywordSelected = b;
        firePropertyChange("quickKeywordSelected", old, isQuickKeywordSelected());
    }

    /**
     * This variable indicates whether we have seleced text, so we can en- or
     * disable the cut and copy actions.
     */
    private boolean attachmentSelected = false;

    public boolean isAttachmentSelected() {
        return attachmentSelected;
    }

    public final void setAttachmentSelected(boolean b) {
        boolean old = isAttachmentSelected();
        this.attachmentSelected = b;
        firePropertyChange("attachmentSelected", old, isAttachmentSelected());
    }

    /**
     * This variable indicates whether the we have text in the main textfield.
     * This is necessary to enable/disable the apply-button, which should only
     * be enabled when we have text in the main textfield. an entry must have
     * content, otherwise it will be considered as deleted.
     */
    private boolean textfieldFilled = false;

    public boolean isTextfieldFilled() {
        return textfieldFilled;
    }

    private void setTextfieldFilled(boolean b) {
        boolean old = isTextfieldFilled();
        this.textfieldFilled = b;
        firePropertyChange("textfieldFilled", old, isTextfieldFilled());
    }

    /**
     * This variable indicates whether undo/redo is possible. This is the case
     * when the main text fiel (jTextAreaEntry) has the focus and changes have
     * been made.
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
     * This variable indicates whether undo/redo is possible. This is the case
     * when the main text fiel (jTextAreaEntry) has the focus and changes have
     * been made.
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

    /**
     * This class sets up a selection listener for the tables. each table which
     * shall react on selections, e.g. by showing an entry, gets this
     * selectionlistener in the method
     * {@link #initSelectionListeners() initSelectionListeners()}.
     */
    public class SelectionListener implements ListSelectionListener {

        JList list;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JList list) {
            this.list = list;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // when we filter or refresh JLists, don't call value-changed event
            if (listUpdateActive) {
                return;
            }
            // get list selection model
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            // set value-adjusting to true, so we don't fire multiple value-changed events...
            lsm.setValueIsAdjusting(true);
            if (jListQuickInputAuthor == list) {
                setAuthorSelected(jListQuickInputAuthor.getSelectedIndex() != -1);
            } else if (jListKeywords == list) {
                setKeywordSelected(jListKeywords.getSelectedIndex() != -1);
            } else if (jListLinks == list) {
                setAttachmentSelected(jListLinks.getSelectedIndex() != -1);
            } // check whether we have changes to the values...
            else if (jListQuickInputKeywords == list) {
                // first of all, remove all old highlights
                removeHighlights();
                // if yes, get selected index to check whether we have a valid selection
                int index = jListQuickInputKeywords.getSelectedIndex();
                setQuickKeywordSelected(index != -1);
                // en-/disable button dependent from a valid selection
                // jButtonAddKeywords.setEnabled(index!=-1);
                // if we have any valid selection, go on here...
                if (index != -1) {
                    // retrieve selected value
                    String text = jListQuickInputKeywords.getSelectedValue().toString().toLowerCase();
                    // first, get the separated parts from the selected value
                    String[] findterms = Tools.getKeywordsAndSynonymsParts(settingsObj, synonymsObj, text, false);
                    // create a new highlighter
                    Highlighter hilite = jTextAreaEntry.getHighlighter();
                    // get entry-text
                    String doctext = jTextAreaEntry.getText().toLowerCase();
                    // iterate array and highlight first match
                    for (String ft : findterms) {
                        int pos = 0;
                        // search for keyword or synonym
                        while ((pos = doctext.indexOf(ft.toLowerCase(), pos)) >= 0) {
                            try {
                                // Create highlighter using private painter and apply around pattern
                                hilite.addHighlight(pos, pos + ft.length(), new MyHighlightPainter(new Color(255, 255, 102)));
                            } catch (BadLocationException ex) {
                            }
                            // set pos to new index
                            pos += ft.length();
                        }
                    }
                }
            }
        }
    }

    /**
     * A private subclass of the default highlight painter
     */
    class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * Removes only our private highlights
     */
    @Action
    public void removeHighlights() {
        Highlighter hilite = jTextAreaEntry.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
        for (Highlighter.Highlight hilite1 : hilites) {
            if (hilite1.getPainter() instanceof MyHighlightPainter) {
                hilite.removeHighlight(hilite1);
            }
        }
    }

    /**
     * This method refreshes the keywordlist for the quick-input, and apllies
     * the keyword-search on the text-selection only. thus, this method is only
     * enabled when text is selected
     */
    @Action(enabledProperty = "textSelected")
    public void createQuickInputFromSelection() {
        if (jCheckBoxQuickInput.isSelected()) {
            stepcounter = 1;
            keywordsListUpToDate = false;
            // if the keyword-tab is visible, refresh the view
            showKeywords();
        } else {
            toggleQuickInput();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuMain = new javax.swing.JPopupMenu();
        popupMainCut = new javax.swing.JMenuItem();
        popupMainCopy = new javax.swing.JMenuItem();
        popupMainPaste = new javax.swing.JMenuItem();
        jSeparator26 = new javax.swing.JSeparator();
        popupMainSelectAll = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JSeparator();
        popupMainUndo = new javax.swing.JMenuItem();
        popupMainRedo = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        popupMainRemoveHighlights = new javax.swing.JMenuItem();
        jSeparator43 = new javax.swing.JSeparator();
        popupMainAddSegment = new javax.swing.JMenuItem();
        popupMainAddKeywordFromSelection = new javax.swing.JMenuItem();
        popupMainAddTitleFromSelection = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JSeparator();
        popupMainReplace = new javax.swing.JMenuItem();
        jSeparator36 = new javax.swing.JSeparator();
        popupMainQuickInputSelection = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        formatSubmenu = new javax.swing.JMenu();
        popupMainBold = new javax.swing.JMenuItem();
        popupMainItalic = new javax.swing.JMenuItem();
        popupMainUnderline = new javax.swing.JMenuItem();
        popupMainStrikeThrough = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        popupMainHeader1 = new javax.swing.JMenuItem();
        poupMainHeader2 = new javax.swing.JMenuItem();
        jSeparator35 = new javax.swing.JSeparator();
        popupMainCite = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        popupMainTextcolor = new javax.swing.JMenuItem();
        popupMainHighlight = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JSeparator();
        popupMainCenter = new javax.swing.JMenuItem();
        popupMainMargin = new javax.swing.JMenuItem();
        popupMainList = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        popupMainSup = new javax.swing.JMenuItem();
        popupMainSub = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        removeSubMenu = new javax.swing.JMenu();
        popupRemoveDoubleLine = new javax.swing.JMenuItem();
        jSeparator31 = new javax.swing.JSeparator();
        popupRemoveDoubleSpace = new javax.swing.JMenuItem();
        jSeparator32 = new javax.swing.JSeparator();
        popupRemoveTab = new javax.swing.JMenuItem();
        jSeparator33 = new javax.swing.JSeparator();
        popupRemoveSingleLine = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        popupMainAddSpellCorrect = new javax.swing.JMenuItem();
        popupMainAddSteno = new javax.swing.JMenuItem();
        jPopupMenuKeywords = new javax.swing.JPopupMenu();
        popupKeywordsAddSegment = new javax.swing.JMenuItem();
        jSeparator41 = new javax.swing.JSeparator();
        popupKeywordsGetCurrentKeywords = new javax.swing.JMenuItem();
        jSeparator37 = new javax.swing.JSeparator();
        popupKeywordsRemove = new javax.swing.JMenuItem();
        jPopupMenuQuickKeywords = new javax.swing.JPopupMenu();
        popupQuickKeywordsAddSegment = new javax.swing.JMenuItem();
        jPopupMenuCCP = new javax.swing.JPopupMenu();
        popupCCPcut = new javax.swing.JMenuItem();
        popupCCPcopy = new javax.swing.JMenuItem();
        popupCCPpaste = new javax.swing.JMenuItem();
        jToolBarNewEntry = new javax.swing.JToolBar();
        tb_cut = new javax.swing.JButton();
        tb_copy = new javax.swing.JButton();
        tb_paste = new javax.swing.JButton();
        jSeparator49 = new javax.swing.JToolBar.Separator();
        tb_selectall = new javax.swing.JButton();
        jSeparator50 = new javax.swing.JToolBar.Separator();
        tb_undo = new javax.swing.JButton();
        tb_redo = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        tb_newauthor = new javax.swing.JButton();
        jSeparator48 = new javax.swing.JToolBar.Separator();
        tb_footnote = new javax.swing.JButton();
        tb_manlink = new javax.swing.JButton();
        tb_insertimage = new javax.swing.JButton();
        tb_inserttable = new javax.swing.JButton();
        tb_insertattachment = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        tb_bold = new javax.swing.JButton();
        tb_italic = new javax.swing.JButton();
        tb_underline = new javax.swing.JButton();
        tb_strike = new javax.swing.JButton();
        jSeparator51 = new javax.swing.JToolBar.Separator();
        tb_textcolor = new javax.swing.JButton();
        tb_highlight = new javax.swing.JButton();
        newEntryMainPanel = new javax.swing.JPanel();
        jSplitPaneNewEntry1 = new javax.swing.JSplitPane();
        jSplitPaneNewEntry2 = new javax.swing.JSplitPane();
        jSplitPaneNewEntry3 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaEntry = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldTitle = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaAuthor = new javax.swing.JTextArea();
        jSplitPaneNewEntry4 = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListKeywords = MacSourceList.createMacSourceList();
        jTextFieldAddKeyword = (settingsObj.isMacAqua()) ? ZknMacWidgetFactory.createHudTreeTextField(resourceMap.getString("textFieldDefaultText")) : new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaRemarks = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        jListLinks = MacSourceList.createMacSourceList();
        jTextFieldAddLink = (settingsObj.isMacAqua()) ? ZknMacWidgetFactory.createHudTreeTextField(resourceMap.getString("textFieldDefaultText")) : new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPaneNewEntry1 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jListQuickInputAuthor = MacSourceList.createMacSourceList();
        jTextFieldFilterAuthorlist = new javax.swing.JTextField();
        jButtonRefreshAuthorlist = new javax.swing.JButton();
        jButtonAddAuthors = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jListQuickInputKeywords = MacSourceList.createMacSourceList();
        jTextFieldFilterKeywordlist = new javax.swing.JTextField();
        jButtonRefreshKeywordlist = new javax.swing.JButton();
        jButtonAddKeywords = new javax.swing.JButton();
        jButtonQuickKeyword = new javax.swing.JButton();
        jPanelQuickInput = new javax.swing.JPanel();
        jCheckBoxQuickInput = new javax.swing.JCheckBox();
        jComboBoxQuickInput = new javax.swing.JComboBox();
        statusPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        statusAnimationLabel = new javax.swing.JLabel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jMenuBarNewEntry = new javax.swing.JMenuBar();
        newEntryFileMenu = new javax.swing.JMenu();
        jMenuItemApply = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        jMenuItemEditSynonyms = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItemClose = new javax.swing.JMenuItem();
        newEntryEditMenu = new javax.swing.JMenu();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemSelectAll = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        jMenuItemAddSegment = new javax.swing.JMenuItem();
        jSeparator42 = new javax.swing.JSeparator();
        jMenuItemAddKeywordFromSelection = new javax.swing.JMenuItem();
        jSeparator39 = new javax.swing.JSeparator();
        jMenuItemAddFirstLineToTitle = new javax.swing.JMenuItem();
        jMenuItemAddTitleFromSelection = new javax.swing.JMenuItem();
        jSeparator40 = new javax.swing.JSeparator();
        jMenuItemUndo = new javax.swing.JMenuItem();
        jMenuItemRedo = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JSeparator();
        jMenuItemReplace = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        jMenuItemRetrieveDisplayedKeywords = new javax.swing.JMenuItem();
        jSeparator38 = new javax.swing.JSeparator();
        editRemoveMenu = new javax.swing.JMenu();
        menuRemoveDoubleLineSeps = new javax.swing.JMenuItem();
        jSeparator27 = new javax.swing.JSeparator();
        menuRemoveDoubleSpaces = new javax.swing.JMenuItem();
        jSeparator28 = new javax.swing.JSeparator();
        menuRemoveTabs = new javax.swing.JMenuItem();
        jSeparator29 = new javax.swing.JSeparator();
        menuRemoveLineSeps = new javax.swing.JMenuItem();
        newEntryInsertMenu = new javax.swing.JMenu();
        jMenuItemNewAuthor = new javax.swing.JMenuItem();
        jSeparator24 = new javax.swing.JSeparator();
        jMenuItemInsertURL = new javax.swing.JMenuItem();
        jMenuItemInsertManlink = new javax.swing.JMenuItem();
        jMenuItemInsertFootnote = new javax.swing.JMenuItem();
        jMenuItemInsertImage = new javax.swing.JMenuItem();
        jMenuItemInsertTable = new javax.swing.JMenuItem();
        jMenuItemInsertForm = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        jMenuItemInsertAttachment = new javax.swing.JMenuItem();
        jSeparator34 = new javax.swing.JSeparator();
        jMenuItemSymbols = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JSeparator();
        jMenuItemManualTimestamp = new javax.swing.JMenuItem();
        newEntryFormatMenu = new javax.swing.JMenu();
        jMenuItemFBold = new javax.swing.JMenuItem();
        jMenuItemFItalic = new javax.swing.JMenuItem();
        jMenuItemFUnderline = new javax.swing.JMenuItem();
        jMenuItemFStrike = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemHeading1 = new javax.swing.JMenuItem();
        jMenuItemHeading2 = new javax.swing.JMenuItem();
        jSeparator30 = new javax.swing.JSeparator();
        jMenuItemCite = new javax.swing.JMenuItem();
        jMenuItemQuotemark = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItemFont = new javax.swing.JMenuItem();
        jMenuItemColor = new javax.swing.JMenuItem();
        jMenuItemHighlight = new javax.swing.JMenuItem();
        jMenuItemCode = new javax.swing.JMenuItem();
        jMenuItemInlineCode = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItemAlignLeft = new javax.swing.JMenuItem();
        jMenuItemCenter = new javax.swing.JMenuItem();
        jMenuItemAlignRight = new javax.swing.JMenuItem();
        jMenuItemAlignJustify = new javax.swing.JMenuItem();
        jSeparator45 = new javax.swing.JPopupMenu.Separator();
        jMenuItemMargin = new javax.swing.JMenuItem();
        jSeparator44 = new javax.swing.JPopupMenu.Separator();
        jMenuItemList = new javax.swing.JMenuItem();
        jMenuItemOrderedList = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMenuItemTextSup = new javax.swing.JMenuItem();
        jMenuItemTextSub = new javax.swing.JMenuItem();
        newEntryWindowMenu = new javax.swing.JMenu();
        jMenuItemFocusMain = new javax.swing.JMenuItem();
        jSeparator47 = new javax.swing.JPopupMenu.Separator();
        jMenuItemFocusKeywords = new javax.swing.JMenuItem();
        jSeparator46 = new javax.swing.JPopupMenu.Separator();
        jMenuItemFocusAuthors = new javax.swing.JMenuItem();

        jPopupMenuMain.setName("jPopupMenuMain"); // NOI18N

        popupMainCut.setName("popupMainCut"); // NOI18N
        jPopupMenuMain.add(popupMainCut);

        popupMainCopy.setName("popupMainCopy"); // NOI18N
        jPopupMenuMain.add(popupMainCopy);

        popupMainPaste.setName("popupMainPaste"); // NOI18N
        jPopupMenuMain.add(popupMainPaste);

        jSeparator26.setName("jSeparator26"); // NOI18N
        jPopupMenuMain.add(jSeparator26);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(EditorFrame.class, this);
        popupMainSelectAll.setAction(actionMap.get("selecteAllText")); // NOI18N
        popupMainSelectAll.setName("popupMainSelectAll"); // NOI18N
        jPopupMenuMain.add(popupMainSelectAll);

        jSeparator25.setName("jSeparator25"); // NOI18N
        jPopupMenuMain.add(jSeparator25);

        popupMainUndo.setAction(actionMap.get("undoAction")); // NOI18N
        popupMainUndo.setName("popupMainUndo"); // NOI18N
        jPopupMenuMain.add(popupMainUndo);

        popupMainRedo.setAction(actionMap.get("redoAction")); // NOI18N
        popupMainRedo.setName("popupMainRedo"); // NOI18N
        jPopupMenuMain.add(popupMainRedo);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jPopupMenuMain.add(jSeparator8);

        popupMainRemoveHighlights.setAction(actionMap.get("removeHighlights")); // NOI18N
        popupMainRemoveHighlights.setName("popupMainRemoveHighlights"); // NOI18N
        jPopupMenuMain.add(popupMainRemoveHighlights);

        jSeparator43.setName("jSeparator43"); // NOI18N
        jPopupMenuMain.add(jSeparator43);

        popupMainAddSegment.setAction(actionMap.get("addSegmentFromQuickList")); // NOI18N
        popupMainAddSegment.setName("popupMainAddSegment"); // NOI18N
        jPopupMenuMain.add(popupMainAddSegment);

        popupMainAddKeywordFromSelection.setAction(actionMap.get("addKeywordFromSelection")); // NOI18N
        popupMainAddKeywordFromSelection.setName("popupMainAddKeywordFromSelection"); // NOI18N
        jPopupMenuMain.add(popupMainAddKeywordFromSelection);

        popupMainAddTitleFromSelection.setAction(actionMap.get("addTitleFromSelection")); // NOI18N
        popupMainAddTitleFromSelection.setName("popupMainAddTitleFromSelection"); // NOI18N
        jPopupMenuMain.add(popupMainAddTitleFromSelection);

        jSeparator22.setName("jSeparator22"); // NOI18N
        jPopupMenuMain.add(jSeparator22);

        popupMainReplace.setAction(actionMap.get("replace")); // NOI18N
        popupMainReplace.setName("popupMainReplace"); // NOI18N
        jPopupMenuMain.add(popupMainReplace);

        jSeparator36.setName("jSeparator36"); // NOI18N
        jPopupMenuMain.add(jSeparator36);

        popupMainQuickInputSelection.setAction(actionMap.get("createQuickInputFromSelection")); // NOI18N
        popupMainQuickInputSelection.setName("popupMainQuickInputSelection"); // NOI18N
        jPopupMenuMain.add(popupMainQuickInputSelection);

        jSeparator20.setName("jSeparator20"); // NOI18N
        jPopupMenuMain.add(jSeparator20);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getResourceMap(EditorFrame.class);
        formatSubmenu.setText(resourceMap.getString("formatSubmenu.text")); // NOI18N
        formatSubmenu.setName("formatSubmenu"); // NOI18N

        popupMainBold.setAction(actionMap.get("formatBold")); // NOI18N
        popupMainBold.setName("popupMainBold"); // NOI18N
        formatSubmenu.add(popupMainBold);

        popupMainItalic.setAction(actionMap.get("formatItalic")); // NOI18N
        popupMainItalic.setName("popupMainItalic"); // NOI18N
        formatSubmenu.add(popupMainItalic);

        popupMainUnderline.setAction(actionMap.get("formatUnderline")); // NOI18N
        popupMainUnderline.setName("popupMainUnderline"); // NOI18N
        formatSubmenu.add(popupMainUnderline);

        popupMainStrikeThrough.setAction(actionMap.get("formatStrikeThrough")); // NOI18N
        popupMainStrikeThrough.setName("popupMainStrikeThrough"); // NOI18N
        formatSubmenu.add(popupMainStrikeThrough);

        jSeparator16.setName("jSeparator16"); // NOI18N
        formatSubmenu.add(jSeparator16);

        popupMainHeader1.setAction(actionMap.get("formatHeading1")); // NOI18N
        popupMainHeader1.setName("popupMainHeader1"); // NOI18N
        formatSubmenu.add(popupMainHeader1);

        poupMainHeader2.setAction(actionMap.get("formatHeading2")); // NOI18N
        poupMainHeader2.setName("poupMainHeader2"); // NOI18N
        formatSubmenu.add(poupMainHeader2);

        jSeparator35.setName("jSeparator35"); // NOI18N
        formatSubmenu.add(jSeparator35);

        popupMainCite.setAction(actionMap.get("formatCite")); // NOI18N
        popupMainCite.setName("popupMainCite"); // NOI18N
        formatSubmenu.add(popupMainCite);

        jSeparator17.setName("jSeparator17"); // NOI18N
        formatSubmenu.add(jSeparator17);

        popupMainTextcolor.setAction(actionMap.get("formatColor")); // NOI18N
        popupMainTextcolor.setName("popupMainTextcolor"); // NOI18N
        formatSubmenu.add(popupMainTextcolor);

        popupMainHighlight.setAction(actionMap.get("formatHighlight")); // NOI18N
        popupMainHighlight.setName("popupMainHighlight"); // NOI18N
        formatSubmenu.add(popupMainHighlight);

        jSeparator18.setName("jSeparator18"); // NOI18N
        formatSubmenu.add(jSeparator18);

        popupMainCenter.setAction(actionMap.get("alignCenter")); // NOI18N
        popupMainCenter.setName("popupMainCenter"); // NOI18N
        formatSubmenu.add(popupMainCenter);

        popupMainMargin.setAction(actionMap.get("alignMargin")); // NOI18N
        popupMainMargin.setName("popupMainMargin"); // NOI18N
        formatSubmenu.add(popupMainMargin);

        popupMainList.setAction(actionMap.get("formatList")); // NOI18N
        popupMainList.setName("popupMainList"); // NOI18N
        formatSubmenu.add(popupMainList);

        jSeparator15.setName("jSeparator15"); // NOI18N
        formatSubmenu.add(jSeparator15);

        popupMainSup.setAction(actionMap.get("formatSup")); // NOI18N
        popupMainSup.setName("popupMainSup"); // NOI18N
        formatSubmenu.add(popupMainSup);

        popupMainSub.setAction(actionMap.get("formatSub")); // NOI18N
        popupMainSub.setName("popupMainSub"); // NOI18N
        formatSubmenu.add(popupMainSub);

        jPopupMenuMain.add(formatSubmenu);

        jSeparator19.setName("jSeparator19"); // NOI18N
        jPopupMenuMain.add(jSeparator19);

        removeSubMenu.setText(resourceMap.getString("removeSubMenu.text")); // NOI18N
        removeSubMenu.setName("removeSubMenu"); // NOI18N

        popupRemoveDoubleLine.setAction(actionMap.get("removeDoubleLineSeparators")); // NOI18N
        popupRemoveDoubleLine.setName("popupRemoveDoubleLine"); // NOI18N
        removeSubMenu.add(popupRemoveDoubleLine);

        jSeparator31.setName("jSeparator31"); // NOI18N
        removeSubMenu.add(jSeparator31);

        popupRemoveDoubleSpace.setAction(actionMap.get("removeDoubleSpaceChars")); // NOI18N
        popupRemoveDoubleSpace.setName("popupRemoveDoubleSpace"); // NOI18N
        removeSubMenu.add(popupRemoveDoubleSpace);

        jSeparator32.setName("jSeparator32"); // NOI18N
        removeSubMenu.add(jSeparator32);

        popupRemoveTab.setAction(actionMap.get("removeTabChars")); // NOI18N
        popupRemoveTab.setName("popupRemoveTab"); // NOI18N
        removeSubMenu.add(popupRemoveTab);

        jSeparator33.setName("jSeparator33"); // NOI18N
        removeSubMenu.add(jSeparator33);

        popupRemoveSingleLine.setAction(actionMap.get("removeSingleLineSeparators")); // NOI18N
        popupRemoveSingleLine.setName("popupRemoveSingleLine"); // NOI18N
        removeSubMenu.add(popupRemoveSingleLine);

        jPopupMenuMain.add(removeSubMenu);

        jSeparator9.setName("jSeparator9"); // NOI18N
        jPopupMenuMain.add(jSeparator9);

        popupMainAddSpellCorrect.setAction(actionMap.get("addSelectionToSpellCorrection")); // NOI18N
        popupMainAddSpellCorrect.setName("popupMainAddSpellCorrect"); // NOI18N
        jPopupMenuMain.add(popupMainAddSpellCorrect);

        popupMainAddSteno.setAction(actionMap.get("addSelectionToSteno")); // NOI18N
        popupMainAddSteno.setName("popupMainAddSteno"); // NOI18N
        jPopupMenuMain.add(popupMainAddSteno);

        jPopupMenuKeywords.setName("jPopupMenuKeywords"); // NOI18N

        popupKeywordsAddSegment.setAction(actionMap.get("addSegment")); // NOI18N
        popupKeywordsAddSegment.setName("popupKeywordsAddSegment"); // NOI18N
        jPopupMenuKeywords.add(popupKeywordsAddSegment);

        jSeparator41.setName("jSeparator41"); // NOI18N
        jPopupMenuKeywords.add(jSeparator41);

        popupKeywordsGetCurrentKeywords.setAction(actionMap.get("retrieveKeywordsFromDisplayedEntry")); // NOI18N
        popupKeywordsGetCurrentKeywords.setName("popupKeywordsGetCurrentKeywords"); // NOI18N
        jPopupMenuKeywords.add(popupKeywordsGetCurrentKeywords);

        jSeparator37.setName("jSeparator37"); // NOI18N
        jPopupMenuKeywords.add(jSeparator37);

        popupKeywordsRemove.setAction(actionMap.get("removeKeyword")); // NOI18N
        popupKeywordsRemove.setName("popupKeywordsRemove"); // NOI18N
        jPopupMenuKeywords.add(popupKeywordsRemove);

        jPopupMenuQuickKeywords.setName("jPopupMenuQuickKeywords"); // NOI18N

        popupQuickKeywordsAddSegment.setAction(actionMap.get("addSegmentFromQuickList")); // NOI18N
        popupQuickKeywordsAddSegment.setName("popupQuickKeywordsAddSegment"); // NOI18N
        jPopupMenuQuickKeywords.add(popupQuickKeywordsAddSegment);

        jPopupMenuCCP.setName("jPopupMenuCCP"); // NOI18N

        popupCCPcut.setAction(actionMap.get("cut"));
        popupCCPcut.setName("popupCCPcut"); // NOI18N
        jPopupMenuCCP.add(popupCCPcut);

        popupCCPcopy.setAction(actionMap.get("copy"));
        popupCCPcopy.setName("popupCCPcopy"); // NOI18N
        jPopupMenuCCP.add(popupCCPcopy);

        popupCCPpaste.setAction(actionMap.get("paste"));
        popupCCPpaste.setName("popupCCPpaste"); // NOI18N
        jPopupMenuCCP.add(popupCCPpaste);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(resourceMap.getString("FormNewEntry.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(300, 200));
        setName("FormNewEntry"); // NOI18N

        jToolBarNewEntry.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("jToolBarNewEntry.border.matteColor"))); // NOI18N
        jToolBarNewEntry.setFloatable(false);
        jToolBarNewEntry.setRollover(true);
        jToolBarNewEntry.setMinimumSize(new java.awt.Dimension(200, 10));
        jToolBarNewEntry.setName("jToolBarNewEntry"); // NOI18N

        tb_cut.setAction(actionMap.get("cut"));
        tb_cut.setText(resourceMap.getString("tb_cut.text")); // NOI18N
        tb_cut.setBorderPainted(false);
        tb_cut.setFocusPainted(false);
        tb_cut.setFocusable(false);
        tb_cut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_cut.setName("tb_cut"); // NOI18N
        tb_cut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_cut);

        tb_copy.setAction(actionMap.get("copy"));
        tb_copy.setText(resourceMap.getString("tb_copy.text")); // NOI18N
        tb_copy.setBorderPainted(false);
        tb_copy.setFocusPainted(false);
        tb_copy.setFocusable(false);
        tb_copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_copy.setName("tb_copy"); // NOI18N
        tb_copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_copy);

        tb_paste.setAction(actionMap.get("paste"));
        tb_paste.setText(resourceMap.getString("tb_paste.text")); // NOI18N
        tb_paste.setBorderPainted(false);
        tb_paste.setFocusPainted(false);
        tb_paste.setFocusable(false);
        tb_paste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_paste.setName("tb_paste"); // NOI18N
        tb_paste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_paste);

        jSeparator49.setName("jSeparator49"); // NOI18N
        jToolBarNewEntry.add(jSeparator49);

        tb_selectall.setAction(actionMap.get("selecteAllText")); // NOI18N
        tb_selectall.setText(resourceMap.getString("tb_selectall.text")); // NOI18N
        tb_selectall.setBorderPainted(false);
        tb_selectall.setFocusPainted(false);
        tb_selectall.setFocusable(false);
        tb_selectall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_selectall.setName("tb_selectall"); // NOI18N
        tb_selectall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_selectall);

        jSeparator50.setName("jSeparator50"); // NOI18N
        jToolBarNewEntry.add(jSeparator50);

        tb_undo.setAction(actionMap.get("undoAction")); // NOI18N
        tb_undo.setText(resourceMap.getString("tb_undo.text")); // NOI18N
        tb_undo.setBorderPainted(false);
        tb_undo.setFocusPainted(false);
        tb_undo.setFocusable(false);
        tb_undo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_undo.setName("tb_undo"); // NOI18N
        tb_undo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_undo);

        tb_redo.setAction(actionMap.get("redoAction")); // NOI18N
        tb_redo.setText(resourceMap.getString("tb_redo.text")); // NOI18N
        tb_redo.setBorderPainted(false);
        tb_redo.setFocusPainted(false);
        tb_redo.setFocusable(false);
        tb_redo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_redo.setName("tb_redo"); // NOI18N
        tb_redo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_redo);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBarNewEntry.add(jSeparator1);

        tb_newauthor.setAction(actionMap.get("addAuthorFromMenu")); // NOI18N
        tb_newauthor.setText(resourceMap.getString("tb_newauthor.text")); // NOI18N
        tb_newauthor.setBorderPainted(false);
        tb_newauthor.setFocusPainted(false);
        tb_newauthor.setFocusable(false);
        tb_newauthor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_newauthor.setName("tb_newauthor"); // NOI18N
        tb_newauthor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_newauthor);

        jSeparator48.setName("jSeparator48"); // NOI18N
        jToolBarNewEntry.add(jSeparator48);

        tb_footnote.setAction(actionMap.get("insertFootnote")); // NOI18N
        tb_footnote.setText(resourceMap.getString("tb_footnote.text")); // NOI18N
        tb_footnote.setBorderPainted(false);
        tb_footnote.setFocusPainted(false);
        tb_footnote.setFocusable(false);
        tb_footnote.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_footnote.setName("tb_footnote"); // NOI18N
        tb_footnote.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_footnote);

        tb_manlink.setAction(actionMap.get("insertManualLink")); // NOI18N
        tb_manlink.setText(resourceMap.getString("tb_manlink.text")); // NOI18N
        tb_manlink.setBorderPainted(false);
        tb_manlink.setFocusPainted(false);
        tb_manlink.setFocusable(false);
        tb_manlink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_manlink.setName("tb_manlink"); // NOI18N
        tb_manlink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_manlink);

        tb_insertimage.setAction(actionMap.get("insertImage")); // NOI18N
        tb_insertimage.setText(resourceMap.getString("tb_insertimage.text")); // NOI18N
        tb_insertimage.setBorderPainted(false);
        tb_insertimage.setFocusPainted(false);
        tb_insertimage.setFocusable(false);
        tb_insertimage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_insertimage.setName("tb_insertimage"); // NOI18N
        tb_insertimage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_insertimage);

        tb_inserttable.setAction(actionMap.get("insertTable")); // NOI18N
        tb_inserttable.setText(resourceMap.getString("tb_inserttable.text")); // NOI18N
        tb_inserttable.setBorderPainted(false);
        tb_inserttable.setFocusPainted(false);
        tb_inserttable.setFocusable(false);
        tb_inserttable.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_inserttable.setName("tb_inserttable"); // NOI18N
        tb_inserttable.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_inserttable);

        tb_insertattachment.setAction(actionMap.get("insertAttachment")); // NOI18N
        tb_insertattachment.setText(resourceMap.getString("tb_insertattachment.text")); // NOI18N
        tb_insertattachment.setBorderPainted(false);
        tb_insertattachment.setFocusPainted(false);
        tb_insertattachment.setFocusable(false);
        tb_insertattachment.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_insertattachment.setName("tb_insertattachment"); // NOI18N
        tb_insertattachment.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_insertattachment);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jToolBarNewEntry.add(jSeparator6);

        tb_bold.setAction(actionMap.get("formatBold")); // NOI18N
        tb_bold.setText(resourceMap.getString("tb_bold.text")); // NOI18N
        tb_bold.setBorderPainted(false);
        tb_bold.setFocusPainted(false);
        tb_bold.setFocusable(false);
        tb_bold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_bold.setName("tb_bold"); // NOI18N
        tb_bold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_bold);

        tb_italic.setAction(actionMap.get("formatItalic")); // NOI18N
        tb_italic.setText(resourceMap.getString("tb_italic.text")); // NOI18N
        tb_italic.setBorderPainted(false);
        tb_italic.setFocusPainted(false);
        tb_italic.setFocusable(false);
        tb_italic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_italic.setName("tb_italic"); // NOI18N
        tb_italic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_italic);

        tb_underline.setAction(actionMap.get("formatUnderline")); // NOI18N
        tb_underline.setText(resourceMap.getString("tb_underline.text")); // NOI18N
        tb_underline.setBorderPainted(false);
        tb_underline.setFocusPainted(false);
        tb_underline.setFocusable(false);
        tb_underline.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_underline.setName("tb_underline"); // NOI18N
        tb_underline.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_underline);

        tb_strike.setAction(actionMap.get("formatStrikeThrough")); // NOI18N
        tb_strike.setText(resourceMap.getString("tb_strike.text")); // NOI18N
        tb_strike.setBorderPainted(false);
        tb_strike.setFocusPainted(false);
        tb_strike.setFocusable(false);
        tb_strike.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_strike.setName("tb_strike"); // NOI18N
        tb_strike.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_strike);

        jSeparator51.setName("jSeparator51"); // NOI18N
        jToolBarNewEntry.add(jSeparator51);

        tb_textcolor.setAction(actionMap.get("formatColor")); // NOI18N
        tb_textcolor.setText(resourceMap.getString("tb_textcolor.text")); // NOI18N
        tb_textcolor.setBorderPainted(false);
        tb_textcolor.setFocusPainted(false);
        tb_textcolor.setFocusable(false);
        tb_textcolor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_textcolor.setName("tb_textcolor"); // NOI18N
        tb_textcolor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_textcolor);

        tb_highlight.setAction(actionMap.get("formatHighlight")); // NOI18N
        tb_highlight.setText(resourceMap.getString("tb_highlight.text")); // NOI18N
        tb_highlight.setBorderPainted(false);
        tb_highlight.setFocusPainted(false);
        tb_highlight.setFocusable(false);
        tb_highlight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_highlight.setName("tb_highlight"); // NOI18N
        tb_highlight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBarNewEntry.add(tb_highlight);

        newEntryMainPanel.setMinimumSize(new java.awt.Dimension(280, 150));
        newEntryMainPanel.setName("newEntryMainPanel"); // NOI18N
        newEntryMainPanel.setLayout(new java.awt.BorderLayout());

        jSplitPaneNewEntry1.setBorder(null);
        jSplitPaneNewEntry1.setDividerLocation(550);
        jSplitPaneNewEntry1.setMinimumSize(new java.awt.Dimension(280, 180));
        jSplitPaneNewEntry1.setName("jSplitPaneNewEntry1"); // NOI18N
        jSplitPaneNewEntry1.setOneTouchExpandable(true);

        jSplitPaneNewEntry2.setBorder(null);
        jSplitPaneNewEntry2.setDividerLocation(350);
        jSplitPaneNewEntry2.setMinimumSize(new java.awt.Dimension(150, 100));
        jSplitPaneNewEntry2.setName("jSplitPaneNewEntry2"); // NOI18N
        jSplitPaneNewEntry2.setOneTouchExpandable(true);

        jSplitPaneNewEntry3.setBorder(null);
        jSplitPaneNewEntry3.setDividerLocation(350);
        jSplitPaneNewEntry3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneNewEntry3.setMinimumSize(new java.awt.Dimension(90, 90));
        jSplitPaneNewEntry3.setName("jSplitPaneNewEntry3"); // NOI18N
        jSplitPaneNewEntry3.setOneTouchExpandable(true);

        jPanel3.setMinimumSize(new java.awt.Dimension(90, 40));
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(20, 5));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaEntry.setLineWrap(true);
        jTextAreaEntry.setWrapStyleWord(true);
        jTextAreaEntry.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextAreaEntry.border.title"))); // NOI18N
        jTextAreaEntry.setName("jTextAreaEntry"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaEntry);

        jPanel9.setMinimumSize(new java.awt.Dimension(30, 20));
        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setPreferredSize(new java.awt.Dimension(100, 36));

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldTitle.setText(resourceMap.getString("jTextFieldTitle.text")); // NOI18N
        jTextFieldTitle.setName("jTextFieldTitle"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldTitle)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
        );

        jSplitPaneNewEntry3.setTopComponent(jPanel3);

        jPanel5.setMinimumSize(new java.awt.Dimension(90, 40));
        jPanel5.setName("jPanel5"); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaAuthor.setLineWrap(true);
        jTextAreaAuthor.setWrapStyleWord(true);
        jTextAreaAuthor.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextAreaAuthor.border.title"))); // NOI18N
        jTextAreaAuthor.setName("jTextAreaAuthor"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaAuthor);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
        );

        jSplitPaneNewEntry3.setRightComponent(jPanel5);

        jSplitPaneNewEntry2.setLeftComponent(jSplitPaneNewEntry3);

        jSplitPaneNewEntry4.setBorder(null);
        jSplitPaneNewEntry4.setDividerLocation(220);
        jSplitPaneNewEntry4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneNewEntry4.setMinimumSize(new java.awt.Dimension(40, 90));
        jSplitPaneNewEntry4.setName("jSplitPaneNewEntry4"); // NOI18N
        jSplitPaneNewEntry4.setOneTouchExpandable(true);

        jPanel4.setMinimumSize(new java.awt.Dimension(30, 40));
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jListKeywords.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListKeywords.border.title"))); // NOI18N
        jListKeywords.setModel(keywordListModel);
        jListKeywords.setName("jListKeywords"); // NOI18N
        jScrollPane3.setViewportView(jListKeywords);

        jTextFieldAddKeyword.setName("jTextFieldAddKeyword"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldAddKeyword, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldAddKeyword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPaneNewEntry4.setTopComponent(jPanel4);

        jPanel6.setMinimumSize(new java.awt.Dimension(30, 40));
        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane4.setBorder(null);
        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane4.setMinimumSize(new java.awt.Dimension(20, 5));
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTextAreaRemarks.setLineWrap(true);
        jTextAreaRemarks.setWrapStyleWord(true);
        jTextAreaRemarks.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jTextAreaRemarks.border.title"))); // NOI18N
        jTextAreaRemarks.setName("jTextAreaRemarks"); // NOI18N
        jScrollPane4.setViewportView(jTextAreaRemarks);

        jScrollPane5.setMinimumSize(new java.awt.Dimension(20, 15));
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jListLinks.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListLinks.border.title"))); // NOI18N
        jListLinks.setModel(linkListModel);
        jListLinks.setName("jListLinks"); // NOI18N
        jScrollPane5.setViewportView(jListLinks);

        jTextFieldAddLink.setText(resourceMap.getString("jTextFieldAddLink.text")); // NOI18N
        jTextFieldAddLink.setName("jTextFieldAddLink"); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldAddLink)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldAddLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPaneNewEntry4.setRightComponent(jPanel6);

        jSplitPaneNewEntry2.setRightComponent(jSplitPaneNewEntry4);

        jSplitPaneNewEntry1.setLeftComponent(jSplitPaneNewEntry2);

        jPanel2.setMinimumSize(new java.awt.Dimension(90, 90));
        jPanel2.setName("jPanel2"); // NOI18N

        jTabbedPaneNewEntry1.setToolTipText(resourceMap.getString("jTabbedPaneNewEntry1.toolTipText")); // NOI18N
        jTabbedPaneNewEntry1.setMinimumSize(new java.awt.Dimension(80, 70));
        jTabbedPaneNewEntry1.setName("jTabbedPaneNewEntry1"); // NOI18N

        jPanel7.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel7.setName("jPanel7"); // NOI18N

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        jListQuickInputAuthor.setModel(quickInputAuthorListModel);
        jListQuickInputAuthor.setDragEnabled(true);
        jListQuickInputAuthor.setName("jListQuickInputAuthor"); // NOI18N
        jScrollPane6.setViewportView(jListQuickInputAuthor);

        jTextFieldFilterAuthorlist.setText(resourceMap.getString("jTextFieldFilterAuthorlist.text")); // NOI18N
        jTextFieldFilterAuthorlist.setToolTipText(resourceMap.getString("jTextFieldFilterAuthorlist.toolTipText")); // NOI18N
        jTextFieldFilterAuthorlist.setName("jTextFieldFilterAuthorlist"); // NOI18N

        jButtonRefreshAuthorlist.setAction(actionMap.get("refreshAuthorList")); // NOI18N
        jButtonRefreshAuthorlist.setIcon(resourceMap.getIcon("jButtonRefreshAuthorlist.icon")); // NOI18N
        jButtonRefreshAuthorlist.setText(resourceMap.getString("jButtonRefreshAuthorlist.text")); // NOI18N
        jButtonRefreshAuthorlist.setBorderPainted(false);
        jButtonRefreshAuthorlist.setContentAreaFilled(false);
        jButtonRefreshAuthorlist.setFocusPainted(false);
        jButtonRefreshAuthorlist.setIconTextGap(0);
        jButtonRefreshAuthorlist.setName("jButtonRefreshAuthorlist"); // NOI18N
        jButtonRefreshAuthorlist.setPreferredSize(new java.awt.Dimension(16, 16));

        jButtonAddAuthors.setAction(actionMap.get("addQuickAuthorToList")); // NOI18N
        jButtonAddAuthors.setName("jButtonAddAuthors"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonAddAuthors)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jTextFieldFilterAuthorlist, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRefreshAuthorlist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButtonRefreshAuthorlist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldFilterAuthorlist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAddAuthors)
                .addContainerGap())
        );

        jTabbedPaneNewEntry1.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), null, jPanel7, resourceMap.getString("jPanel7.TabConstraints.tabToolTip")); // NOI18N

        jPanel8.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel8.setName("jPanel8"); // NOI18N

        jScrollPane7.setMinimumSize(new java.awt.Dimension(30, 30));
        jScrollPane7.setName("jScrollPane7"); // NOI18N

        jListQuickInputKeywords.setModel(quickInputKeywordsListModel);
        jListQuickInputKeywords.setDragEnabled(true);
        jListQuickInputKeywords.setName("jListQuickInputKeywords"); // NOI18N
        jScrollPane7.setViewportView(jListQuickInputKeywords);

        jTextFieldFilterKeywordlist.setToolTipText(resourceMap.getString("jTextFieldFilterKeywordlist.toolTipText")); // NOI18N
        jTextFieldFilterKeywordlist.setName("jTextFieldFilterKeywordlist"); // NOI18N

        jButtonRefreshKeywordlist.setAction(actionMap.get("showKeywords")); // NOI18N
        jButtonRefreshKeywordlist.setIcon(resourceMap.getIcon("jButtonRefreshKeywordlist.icon")); // NOI18N
        jButtonRefreshKeywordlist.setText(resourceMap.getString("jButtonRefreshKeywordlist.text")); // NOI18N
        jButtonRefreshKeywordlist.setBorderPainted(false);
        jButtonRefreshKeywordlist.setContentAreaFilled(false);
        jButtonRefreshKeywordlist.setFocusPainted(false);
        jButtonRefreshKeywordlist.setIconTextGap(0);
        jButtonRefreshKeywordlist.setName("jButtonRefreshKeywordlist"); // NOI18N
        jButtonRefreshKeywordlist.setPreferredSize(new java.awt.Dimension(16, 16));

        jButtonAddKeywords.setAction(actionMap.get("addQuickKeywordToList")); // NOI18N
        jButtonAddKeywords.setName("jButtonAddKeywords"); // NOI18N

        jButtonQuickKeyword.setAction(actionMap.get("showNextQuickInputStep")); // NOI18N
        jButtonQuickKeyword.setName("jButtonQuickKeyword"); // NOI18N

        jPanelQuickInput.setMinimumSize(new java.awt.Dimension(30, 20));
        jPanelQuickInput.setName("jPanelQuickInput"); // NOI18N

        jCheckBoxQuickInput.setAction(actionMap.get("toggleQuickInput")); // NOI18N
        jCheckBoxQuickInput.setName("jCheckBoxQuickInput"); // NOI18N

        jComboBoxQuickInput.setToolTipText(resourceMap.getString("jComboBoxQuickInput.toolTipText")); // NOI18N
        jComboBoxQuickInput.setName("jComboBoxQuickInput"); // NOI18N

        javax.swing.GroupLayout jPanelQuickInputLayout = new javax.swing.GroupLayout(jPanelQuickInput);
        jPanelQuickInput.setLayout(jPanelQuickInputLayout);
        jPanelQuickInputLayout.setHorizontalGroup(
            jPanelQuickInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelQuickInputLayout.createSequentialGroup()
                .addGroup(jPanelQuickInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxQuickInput)
                    .addComponent(jComboBoxQuickInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanelQuickInputLayout.setVerticalGroup(
            jPanelQuickInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelQuickInputLayout.createSequentialGroup()
                .addComponent(jCheckBoxQuickInput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBoxQuickInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButtonAddKeywords)
                        .addGap(0, 0, 0)
                        .addComponent(jButtonQuickKeyword))
                    .addComponent(jPanelQuickInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jTextFieldFilterKeywordlist)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRefreshKeywordlist, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButtonRefreshKeywordlist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldFilterKeywordlist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelQuickInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddKeywords)
                    .addComponent(jButtonQuickKeyword))
                .addContainerGap())
        );

        jTabbedPaneNewEntry1.addTab(resourceMap.getString("jPanel8.TabConstraints.tabTitle"), null, jPanel8, resourceMap.getString("jPanel8.TabConstraints.tabToolTip")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneNewEntry1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneNewEntry1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPaneNewEntry1.setRightComponent(jPanel2);

        newEntryMainPanel.add(jSplitPaneNewEntry1, java.awt.BorderLayout.CENTER);

        statusPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("statusPanel.border.matteColor"))); // NOI18N
        statusPanel.setMinimumSize(new java.awt.Dimension(200, 16));
        statusPanel.setName("statusPanel"); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(260, 10));
        jPanel1.setName("jPanel1"); // NOI18N

        statusAnimationLabel.setIcon(resourceMap.getIcon("statusAnimationLabel.icon")); // NOI18N
        statusAnimationLabel.setText(resourceMap.getString("statusAnimationLabel.text")); // NOI18N
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        jButtonOK.setAction(actionMap.get("applyChanges")); // NOI18N
        jButtonOK.setText(resourceMap.getString("jButtonOK.text")); // NOI18N
        jButtonOK.setName("jButtonOK"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonCancel)
                        .addComponent(jButtonOK))
                    .addComponent(statusAnimationLabel))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jMenuBarNewEntry.setMinimumSize(new java.awt.Dimension(10, 1));
        jMenuBarNewEntry.setName("jMenuBarNewEntry"); // NOI18N

        newEntryFileMenu.setText(resourceMap.getString("jMenuNewEntryFile.text")); // NOI18N
        newEntryFileMenu.setName("jMenuNewEntryFile"); // NOI18N

        jMenuItemApply.setAction(actionMap.get("applyChanges")); // NOI18N
        jMenuItemApply.setText(resourceMap.getString("jMenuItemApply.text")); // NOI18N
        jMenuItemApply.setName("jMenuItemApply"); // NOI18N
        newEntryFileMenu.add(jMenuItemApply);

        jSeparator13.setName("jSeparator13"); // NOI18N
        newEntryFileMenu.add(jSeparator13);

        jMenuItemEditSynonyms.setAction(actionMap.get("editSynonyms")); // NOI18N
        jMenuItemEditSynonyms.setName("jMenuItemEditSynonyms"); // NOI18N
        newEntryFileMenu.add(jMenuItemEditSynonyms);

        jSeparator7.setName("jSeparator7"); // NOI18N
        newEntryFileMenu.add(jSeparator7);

        jMenuItemClose.setAction(actionMap.get("closeWindow")); // NOI18N
        jMenuItemClose.setName("jMenuItemClose"); // NOI18N
        newEntryFileMenu.add(jMenuItemClose);

        jMenuBarNewEntry.add(newEntryFileMenu);

        newEntryEditMenu.setText(resourceMap.getString("jMenuNewEntryEdit.text")); // NOI18N
        newEntryEditMenu.setName("jMenuNewEntryEdit"); // NOI18N

        jMenuItemCut.setAction(actionMap.get("cut"));
        jMenuItemCut.setName("jMenuItemCut"); // NOI18N
        newEntryEditMenu.add(jMenuItemCut);

        jMenuItemCopy.setAction(actionMap.get("copy"));
        jMenuItemCopy.setName("jMenuItemCopy"); // NOI18N
        newEntryEditMenu.add(jMenuItemCopy);

        jMenuItemPaste.setAction(actionMap.get("paste"));
        jMenuItemPaste.setName("jMenuItemPaste"); // NOI18N
        newEntryEditMenu.add(jMenuItemPaste);

        jSeparator2.setName("jSeparator2"); // NOI18N
        newEntryEditMenu.add(jSeparator2);

        jMenuItemSelectAll.setAction(actionMap.get("selecteAllText")); // NOI18N
        jMenuItemSelectAll.setName("jMenuItemSelectAll"); // NOI18N
        newEntryEditMenu.add(jMenuItemSelectAll);

        jSeparator12.setName("jSeparator12"); // NOI18N
        newEntryEditMenu.add(jSeparator12);

        jMenuItemAddSegment.setAction(actionMap.get("addSegmentFromQuickList")); // NOI18N
        jMenuItemAddSegment.setName("jMenuItemAddSegment"); // NOI18N
        newEntryEditMenu.add(jMenuItemAddSegment);

        jSeparator42.setName("jSeparator42"); // NOI18N
        newEntryEditMenu.add(jSeparator42);

        jMenuItemAddKeywordFromSelection.setAction(actionMap.get("addKeywordFromSelection")); // NOI18N
        jMenuItemAddKeywordFromSelection.setName("jMenuItemAddKeywordFromSelection"); // NOI18N
        newEntryEditMenu.add(jMenuItemAddKeywordFromSelection);

        jSeparator39.setName("jSeparator39"); // NOI18N
        newEntryEditMenu.add(jSeparator39);

        jMenuItemAddFirstLineToTitle.setAction(actionMap.get("addTitleFromFirstLine")); // NOI18N
        jMenuItemAddFirstLineToTitle.setName("jMenuItemAddFirstLineToTitle"); // NOI18N
        newEntryEditMenu.add(jMenuItemAddFirstLineToTitle);

        jMenuItemAddTitleFromSelection.setAction(actionMap.get("addTitleFromSelection")); // NOI18N
        jMenuItemAddTitleFromSelection.setName("jMenuItemAddTitleFromSelection"); // NOI18N
        newEntryEditMenu.add(jMenuItemAddTitleFromSelection);

        jSeparator40.setName("jSeparator40"); // NOI18N
        newEntryEditMenu.add(jSeparator40);

        jMenuItemUndo.setAction(actionMap.get("undoAction")); // NOI18N
        jMenuItemUndo.setName("jMenuItemUndo"); // NOI18N
        newEntryEditMenu.add(jMenuItemUndo);

        jMenuItemRedo.setAction(actionMap.get("redoAction")); // NOI18N
        jMenuItemRedo.setName("jMenuItemRedo"); // NOI18N
        newEntryEditMenu.add(jMenuItemRedo);

        jSeparator21.setName("jSeparator21"); // NOI18N
        newEntryEditMenu.add(jSeparator21);

        jMenuItemReplace.setAction(actionMap.get("replace")); // NOI18N
        jMenuItemReplace.setName("jMenuItemReplace"); // NOI18N
        newEntryEditMenu.add(jMenuItemReplace);

        jSeparator14.setName("jSeparator14"); // NOI18N
        newEntryEditMenu.add(jSeparator14);

        jMenuItemRetrieveDisplayedKeywords.setAction(actionMap.get("retrieveKeywordsFromDisplayedEntry")); // NOI18N
        jMenuItemRetrieveDisplayedKeywords.setName("jMenuItemRetrieveDisplayedKeywords"); // NOI18N
        newEntryEditMenu.add(jMenuItemRetrieveDisplayedKeywords);

        jSeparator38.setName("jSeparator38"); // NOI18N
        newEntryEditMenu.add(jSeparator38);

        editRemoveMenu.setText(resourceMap.getString("editRemoveMenu.text")); // NOI18N
        editRemoveMenu.setName("editRemoveMenu"); // NOI18N

        menuRemoveDoubleLineSeps.setAction(actionMap.get("removeDoubleLineSeparators")); // NOI18N
        menuRemoveDoubleLineSeps.setName("menuRemoveDoubleLineSeps"); // NOI18N
        editRemoveMenu.add(menuRemoveDoubleLineSeps);

        jSeparator27.setName("jSeparator27"); // NOI18N
        editRemoveMenu.add(jSeparator27);

        menuRemoveDoubleSpaces.setAction(actionMap.get("removeDoubleSpaceChars")); // NOI18N
        menuRemoveDoubleSpaces.setName("menuRemoveDoubleSpaces"); // NOI18N
        editRemoveMenu.add(menuRemoveDoubleSpaces);

        jSeparator28.setName("jSeparator28"); // NOI18N
        editRemoveMenu.add(jSeparator28);

        menuRemoveTabs.setAction(actionMap.get("removeTabChars")); // NOI18N
        menuRemoveTabs.setName("menuRemoveTabs"); // NOI18N
        editRemoveMenu.add(menuRemoveTabs);

        jSeparator29.setName("jSeparator29"); // NOI18N
        editRemoveMenu.add(jSeparator29);

        menuRemoveLineSeps.setAction(actionMap.get("removeSingleLineSeparators")); // NOI18N
        menuRemoveLineSeps.setName("menuRemoveLineSeps"); // NOI18N
        editRemoveMenu.add(menuRemoveLineSeps);

        newEntryEditMenu.add(editRemoveMenu);

        jMenuBarNewEntry.add(newEntryEditMenu);

        newEntryInsertMenu.setText(resourceMap.getString("jMenuNewEntryInsert.text")); // NOI18N
        newEntryInsertMenu.setName("jMenuNewEntryInsert"); // NOI18N

        jMenuItemNewAuthor.setAction(actionMap.get("addAuthorFromMenu")); // NOI18N
        jMenuItemNewAuthor.setName("jMenuItemNewAuthor"); // NOI18N
        newEntryInsertMenu.add(jMenuItemNewAuthor);

        jSeparator24.setName("jSeparator24"); // NOI18N
        newEntryInsertMenu.add(jSeparator24);

        jMenuItemInsertURL.setAction(actionMap.get("insertHyperlink")); // NOI18N
        jMenuItemInsertURL.setName("jMenuItemInsertURL"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertURL);

        jMenuItemInsertManlink.setAction(actionMap.get("insertManualLink")); // NOI18N
        jMenuItemInsertManlink.setText(resourceMap.getString("jMenuItemInsertManlink.text")); // NOI18N
        jMenuItemInsertManlink.setName("jMenuItemInsertManlink"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertManlink);

        jMenuItemInsertFootnote.setAction(actionMap.get("insertFootnote")); // NOI18N
        jMenuItemInsertFootnote.setName("jMenuItemInsertFootnote"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertFootnote);

        jMenuItemInsertImage.setAction(actionMap.get("insertImage")); // NOI18N
        jMenuItemInsertImage.setName("jMenuItemInsertImage"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertImage);

        jMenuItemInsertTable.setAction(actionMap.get("insertTable")); // NOI18N
        jMenuItemInsertTable.setName("jMenuItemInsertTable"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertTable);

        jMenuItemInsertForm.setAction(actionMap.get("insertForm")); // NOI18N
        jMenuItemInsertForm.setName("jMenuItemInsertForm"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertForm);

        jSeparator11.setName("jSeparator11"); // NOI18N
        newEntryInsertMenu.add(jSeparator11);

        jMenuItemInsertAttachment.setAction(actionMap.get("insertAttachment")); // NOI18N
        jMenuItemInsertAttachment.setName("jMenuItemInsertAttachment"); // NOI18N
        newEntryInsertMenu.add(jMenuItemInsertAttachment);

        jSeparator34.setName("jSeparator34"); // NOI18N
        newEntryInsertMenu.add(jSeparator34);

        jMenuItemSymbols.setAction(actionMap.get("insertSymbol")); // NOI18N
        jMenuItemSymbols.setName("jMenuItemSymbols"); // NOI18N
        newEntryInsertMenu.add(jMenuItemSymbols);

        jSeparator23.setName("jSeparator23"); // NOI18N
        newEntryInsertMenu.add(jSeparator23);

        jMenuItemManualTimestamp.setAction(actionMap.get("insertManualTimestamp")); // NOI18N
        jMenuItemManualTimestamp.setName("jMenuItemManualTimestamp"); // NOI18N
        newEntryInsertMenu.add(jMenuItemManualTimestamp);

        jMenuBarNewEntry.add(newEntryInsertMenu);

        newEntryFormatMenu.setText(resourceMap.getString("jMenuNewEntryFormat.text")); // NOI18N
        newEntryFormatMenu.setName("jMenuNewEntryFormat"); // NOI18N

        jMenuItemFBold.setAction(actionMap.get("formatBold")); // NOI18N
        jMenuItemFBold.setName("jMenuItemFBold"); // NOI18N
        newEntryFormatMenu.add(jMenuItemFBold);

        jMenuItemFItalic.setAction(actionMap.get("formatItalic")); // NOI18N
        jMenuItemFItalic.setName("jMenuItemFItalic"); // NOI18N
        newEntryFormatMenu.add(jMenuItemFItalic);

        jMenuItemFUnderline.setAction(actionMap.get("formatUnderline")); // NOI18N
        jMenuItemFUnderline.setName("jMenuItemFUnderline"); // NOI18N
        newEntryFormatMenu.add(jMenuItemFUnderline);

        jMenuItemFStrike.setAction(actionMap.get("formatStrikeThrough")); // NOI18N
        jMenuItemFStrike.setName("jMenuItemFStrike"); // NOI18N
        newEntryFormatMenu.add(jMenuItemFStrike);

        jSeparator3.setName("jSeparator3"); // NOI18N
        newEntryFormatMenu.add(jSeparator3);

        jMenuItemHeading1.setAction(actionMap.get("formatHeading1")); // NOI18N
        jMenuItemHeading1.setName("jMenuItemHeading1"); // NOI18N
        newEntryFormatMenu.add(jMenuItemHeading1);

        jMenuItemHeading2.setAction(actionMap.get("formatHeading2")); // NOI18N
        jMenuItemHeading2.setName("jMenuItemHeading2"); // NOI18N
        newEntryFormatMenu.add(jMenuItemHeading2);

        jSeparator30.setName("jSeparator30"); // NOI18N
        newEntryFormatMenu.add(jSeparator30);

        jMenuItemCite.setAction(actionMap.get("formatCite")); // NOI18N
        jMenuItemCite.setName("jMenuItemCite"); // NOI18N
        newEntryFormatMenu.add(jMenuItemCite);

        jMenuItemQuotemark.setAction(actionMap.get("formatQuote")); // NOI18N
        jMenuItemQuotemark.setName("jMenuItemQuotemark"); // NOI18N
        newEntryFormatMenu.add(jMenuItemQuotemark);

        jSeparator5.setName("jSeparator5"); // NOI18N
        newEntryFormatMenu.add(jSeparator5);

        jMenuItemFont.setAction(actionMap.get("formatFont")); // NOI18N
        jMenuItemFont.setName("jMenuItemFont"); // NOI18N
        newEntryFormatMenu.add(jMenuItemFont);

        jMenuItemColor.setAction(actionMap.get("formatColor")); // NOI18N
        jMenuItemColor.setName("jMenuItemColor"); // NOI18N
        newEntryFormatMenu.add(jMenuItemColor);

        jMenuItemHighlight.setAction(actionMap.get("formatHighlight")); // NOI18N
        jMenuItemHighlight.setName("jMenuItemHighlight"); // NOI18N
        newEntryFormatMenu.add(jMenuItemHighlight);

        jMenuItemCode.setAction(actionMap.get("formatCode")); // NOI18N
        jMenuItemCode.setToolTipText(resourceMap.getString("jMenuItemCode.toolTipText")); // NOI18N
        jMenuItemCode.setName("jMenuItemCode"); // NOI18N
        newEntryFormatMenu.add(jMenuItemCode);

        jMenuItemInlineCode.setAction(actionMap.get("formatInlineCode")); // NOI18N
        jMenuItemInlineCode.setName("jMenuItemInlineCode"); // NOI18N
        newEntryFormatMenu.add(jMenuItemInlineCode);

        jSeparator4.setName("jSeparator4"); // NOI18N
        newEntryFormatMenu.add(jSeparator4);

        jMenuItemAlignLeft.setAction(actionMap.get("alignLeft")); // NOI18N
        jMenuItemAlignLeft.setName("jMenuItemAlignLeft"); // NOI18N
        newEntryFormatMenu.add(jMenuItemAlignLeft);

        jMenuItemCenter.setAction(actionMap.get("alignCenter")); // NOI18N
        jMenuItemCenter.setName("jMenuItemCenter"); // NOI18N
        newEntryFormatMenu.add(jMenuItemCenter);

        jMenuItemAlignRight.setAction(actionMap.get("alignRight")); // NOI18N
        jMenuItemAlignRight.setName("jMenuItemAlignRight"); // NOI18N
        newEntryFormatMenu.add(jMenuItemAlignRight);

        jMenuItemAlignJustify.setAction(actionMap.get("alignJustify")); // NOI18N
        jMenuItemAlignJustify.setName("jMenuItemAlignJustify"); // NOI18N
        newEntryFormatMenu.add(jMenuItemAlignJustify);

        jSeparator45.setName("jSeparator45"); // NOI18N
        newEntryFormatMenu.add(jSeparator45);

        jMenuItemMargin.setAction(actionMap.get("alignMargin")); // NOI18N
        jMenuItemMargin.setName("jMenuItemMargin"); // NOI18N
        newEntryFormatMenu.add(jMenuItemMargin);

        jSeparator44.setName("jSeparator44"); // NOI18N
        newEntryFormatMenu.add(jSeparator44);

        jMenuItemList.setAction(actionMap.get("formatList")); // NOI18N
        jMenuItemList.setName("jMenuItemList"); // NOI18N
        newEntryFormatMenu.add(jMenuItemList);

        jMenuItemOrderedList.setAction(actionMap.get("formatOrderedList")); // NOI18N
        jMenuItemOrderedList.setName("jMenuItemOrderedList"); // NOI18N
        newEntryFormatMenu.add(jMenuItemOrderedList);

        jSeparator10.setName("jSeparator10"); // NOI18N
        newEntryFormatMenu.add(jSeparator10);

        jMenuItemTextSup.setAction(actionMap.get("formatSup")); // NOI18N
        jMenuItemTextSup.setName("jMenuItemTextSup"); // NOI18N
        newEntryFormatMenu.add(jMenuItemTextSup);

        jMenuItemTextSub.setAction(actionMap.get("formatSub")); // NOI18N
        jMenuItemTextSub.setName("jMenuItemTextSub"); // NOI18N
        newEntryFormatMenu.add(jMenuItemTextSub);

        jMenuBarNewEntry.add(newEntryFormatMenu);

        newEntryWindowMenu.setText(resourceMap.getString("newEntryWindowMenu.text")); // NOI18N
        newEntryWindowMenu.setName("newEntryWindowMenu"); // NOI18N

        jMenuItemFocusMain.setAction(actionMap.get("setFocusToEditPane")); // NOI18N
        jMenuItemFocusMain.setName("jMenuItemFocusMain"); // NOI18N
        newEntryWindowMenu.add(jMenuItemFocusMain);

        jSeparator47.setName("jSeparator47"); // NOI18N
        newEntryWindowMenu.add(jSeparator47);

        jMenuItemFocusKeywords.setAction(actionMap.get("setFocusToKeywordList")); // NOI18N
        jMenuItemFocusKeywords.setName("jMenuItemFocusKeywords"); // NOI18N
        newEntryWindowMenu.add(jMenuItemFocusKeywords);

        jSeparator46.setName("jSeparator46"); // NOI18N
        newEntryWindowMenu.add(jSeparator46);

        jMenuItemFocusAuthors.setAction(actionMap.get("setFocusToAuthorList")); // NOI18N
        jMenuItemFocusAuthors.setName("jMenuItemFocusAuthors"); // NOI18N
        newEntryWindowMenu.add(jMenuItemFocusAuthors);

        jMenuBarNewEntry.add(newEntryWindowMenu);

        setJMenuBar(jMenuBarNewEntry);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarNewEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(newEntryMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBarNewEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(newEntryMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editRemoveMenu;
    private javax.swing.JMenu formatSubmenu;
    private javax.swing.JButton jButtonAddAuthors;
    private javax.swing.JButton jButtonAddKeywords;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonQuickKeyword;
    private javax.swing.JButton jButtonRefreshAuthorlist;
    private javax.swing.JButton jButtonRefreshKeywordlist;
    private javax.swing.JCheckBox jCheckBoxQuickInput;
    private javax.swing.JComboBox jComboBoxQuickInput;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jListKeywords;
    private javax.swing.JList jListLinks;
    private javax.swing.JList jListQuickInputAuthor;
    private javax.swing.JList jListQuickInputKeywords;
    private javax.swing.JMenuBar jMenuBarNewEntry;
    private javax.swing.JMenuItem jMenuItemAddFirstLineToTitle;
    private javax.swing.JMenuItem jMenuItemAddKeywordFromSelection;
    private javax.swing.JMenuItem jMenuItemAddSegment;
    private javax.swing.JMenuItem jMenuItemAddTitleFromSelection;
    private javax.swing.JMenuItem jMenuItemAlignJustify;
    private javax.swing.JMenuItem jMenuItemAlignLeft;
    private javax.swing.JMenuItem jMenuItemAlignRight;
    private javax.swing.JMenuItem jMenuItemApply;
    private javax.swing.JMenuItem jMenuItemCenter;
    private javax.swing.JMenuItem jMenuItemCite;
    private javax.swing.JMenuItem jMenuItemClose;
    private javax.swing.JMenuItem jMenuItemCode;
    private javax.swing.JMenuItem jMenuItemColor;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemEditSynonyms;
    private javax.swing.JMenuItem jMenuItemFBold;
    private javax.swing.JMenuItem jMenuItemFItalic;
    private javax.swing.JMenuItem jMenuItemFStrike;
    private javax.swing.JMenuItem jMenuItemFUnderline;
    private javax.swing.JMenuItem jMenuItemFocusAuthors;
    private javax.swing.JMenuItem jMenuItemFocusKeywords;
    private javax.swing.JMenuItem jMenuItemFocusMain;
    private javax.swing.JMenuItem jMenuItemFont;
    private javax.swing.JMenuItem jMenuItemHeading1;
    private javax.swing.JMenuItem jMenuItemHeading2;
    private javax.swing.JMenuItem jMenuItemHighlight;
    private javax.swing.JMenuItem jMenuItemInlineCode;
    private javax.swing.JMenuItem jMenuItemInsertAttachment;
    private javax.swing.JMenuItem jMenuItemInsertFootnote;
    private javax.swing.JMenuItem jMenuItemInsertForm;
    private javax.swing.JMenuItem jMenuItemInsertImage;
    private javax.swing.JMenuItem jMenuItemInsertManlink;
    private javax.swing.JMenuItem jMenuItemInsertTable;
    private javax.swing.JMenuItem jMenuItemInsertURL;
    private javax.swing.JMenuItem jMenuItemList;
    private javax.swing.JMenuItem jMenuItemManualTimestamp;
    private javax.swing.JMenuItem jMenuItemMargin;
    private javax.swing.JMenuItem jMenuItemNewAuthor;
    private javax.swing.JMenuItem jMenuItemOrderedList;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemQuotemark;
    private javax.swing.JMenuItem jMenuItemRedo;
    private javax.swing.JMenuItem jMenuItemReplace;
    private javax.swing.JMenuItem jMenuItemRetrieveDisplayedKeywords;
    private javax.swing.JMenuItem jMenuItemSelectAll;
    private javax.swing.JMenuItem jMenuItemSymbols;
    private javax.swing.JMenuItem jMenuItemTextSub;
    private javax.swing.JMenuItem jMenuItemTextSup;
    private javax.swing.JMenuItem jMenuItemUndo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelQuickInput;
    private javax.swing.JPopupMenu jPopupMenuCCP;
    private javax.swing.JPopupMenu jPopupMenuKeywords;
    private javax.swing.JPopupMenu jPopupMenuMain;
    private javax.swing.JPopupMenu jPopupMenuQuickKeywords;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JSeparator jSeparator23;
    private javax.swing.JSeparator jSeparator24;
    private javax.swing.JSeparator jSeparator25;
    private javax.swing.JSeparator jSeparator26;
    private javax.swing.JSeparator jSeparator27;
    private javax.swing.JSeparator jSeparator28;
    private javax.swing.JSeparator jSeparator29;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator30;
    private javax.swing.JSeparator jSeparator31;
    private javax.swing.JSeparator jSeparator32;
    private javax.swing.JSeparator jSeparator33;
    private javax.swing.JSeparator jSeparator34;
    private javax.swing.JSeparator jSeparator35;
    private javax.swing.JSeparator jSeparator36;
    private javax.swing.JSeparator jSeparator37;
    private javax.swing.JSeparator jSeparator38;
    private javax.swing.JSeparator jSeparator39;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator40;
    private javax.swing.JSeparator jSeparator41;
    private javax.swing.JSeparator jSeparator42;
    private javax.swing.JSeparator jSeparator43;
    private javax.swing.JPopupMenu.Separator jSeparator44;
    private javax.swing.JPopupMenu.Separator jSeparator45;
    private javax.swing.JPopupMenu.Separator jSeparator46;
    private javax.swing.JPopupMenu.Separator jSeparator47;
    private javax.swing.JToolBar.Separator jSeparator48;
    private javax.swing.JToolBar.Separator jSeparator49;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator50;
    private javax.swing.JToolBar.Separator jSeparator51;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPaneNewEntry1;
    private javax.swing.JSplitPane jSplitPaneNewEntry2;
    private javax.swing.JSplitPane jSplitPaneNewEntry3;
    private javax.swing.JSplitPane jSplitPaneNewEntry4;
    private javax.swing.JTabbedPane jTabbedPaneNewEntry1;
    private javax.swing.JTextArea jTextAreaAuthor;
    private javax.swing.JTextArea jTextAreaEntry;
    private javax.swing.JTextArea jTextAreaRemarks;
    private javax.swing.JTextField jTextFieldAddKeyword;
    private javax.swing.JTextField jTextFieldAddLink;
    private javax.swing.JTextField jTextFieldFilterAuthorlist;
    private javax.swing.JTextField jTextFieldFilterKeywordlist;
    private javax.swing.JTextField jTextFieldTitle;
    private javax.swing.JToolBar jToolBarNewEntry;
    private javax.swing.JMenuItem menuRemoveDoubleLineSeps;
    private javax.swing.JMenuItem menuRemoveDoubleSpaces;
    private javax.swing.JMenuItem menuRemoveLineSeps;
    private javax.swing.JMenuItem menuRemoveTabs;
    private javax.swing.JMenu newEntryEditMenu;
    private javax.swing.JMenu newEntryFileMenu;
    private javax.swing.JMenu newEntryFormatMenu;
    private javax.swing.JMenu newEntryInsertMenu;
    private javax.swing.JPanel newEntryMainPanel;
    private javax.swing.JMenu newEntryWindowMenu;
    private javax.swing.JMenuItem popupCCPcopy;
    private javax.swing.JMenuItem popupCCPcut;
    private javax.swing.JMenuItem popupCCPpaste;
    private javax.swing.JMenuItem popupKeywordsAddSegment;
    private javax.swing.JMenuItem popupKeywordsGetCurrentKeywords;
    private javax.swing.JMenuItem popupKeywordsRemove;
    private javax.swing.JMenuItem popupMainAddKeywordFromSelection;
    private javax.swing.JMenuItem popupMainAddSegment;
    private javax.swing.JMenuItem popupMainAddSpellCorrect;
    private javax.swing.JMenuItem popupMainAddSteno;
    private javax.swing.JMenuItem popupMainAddTitleFromSelection;
    private javax.swing.JMenuItem popupMainBold;
    private javax.swing.JMenuItem popupMainCenter;
    private javax.swing.JMenuItem popupMainCite;
    private javax.swing.JMenuItem popupMainCopy;
    private javax.swing.JMenuItem popupMainCut;
    private javax.swing.JMenuItem popupMainHeader1;
    private javax.swing.JMenuItem popupMainHighlight;
    private javax.swing.JMenuItem popupMainItalic;
    private javax.swing.JMenuItem popupMainList;
    private javax.swing.JMenuItem popupMainMargin;
    private javax.swing.JMenuItem popupMainPaste;
    private javax.swing.JMenuItem popupMainQuickInputSelection;
    private javax.swing.JMenuItem popupMainRedo;
    private javax.swing.JMenuItem popupMainRemoveHighlights;
    private javax.swing.JMenuItem popupMainReplace;
    private javax.swing.JMenuItem popupMainSelectAll;
    private javax.swing.JMenuItem popupMainStrikeThrough;
    private javax.swing.JMenuItem popupMainSub;
    private javax.swing.JMenuItem popupMainSup;
    private javax.swing.JMenuItem popupMainTextcolor;
    private javax.swing.JMenuItem popupMainUnderline;
    private javax.swing.JMenuItem popupMainUndo;
    private javax.swing.JMenuItem popupQuickKeywordsAddSegment;
    private javax.swing.JMenuItem popupRemoveDoubleLine;
    private javax.swing.JMenuItem popupRemoveDoubleSpace;
    private javax.swing.JMenuItem popupRemoveSingleLine;
    private javax.swing.JMenuItem popupRemoveTab;
    private javax.swing.JMenuItem poupMainHeader2;
    private javax.swing.JMenu removeSubMenu;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton tb_bold;
    private javax.swing.JButton tb_copy;
    private javax.swing.JButton tb_cut;
    private javax.swing.JButton tb_footnote;
    private javax.swing.JButton tb_highlight;
    private javax.swing.JButton tb_insertattachment;
    private javax.swing.JButton tb_insertimage;
    private javax.swing.JButton tb_inserttable;
    private javax.swing.JButton tb_italic;
    private javax.swing.JButton tb_manlink;
    private javax.swing.JButton tb_newauthor;
    private javax.swing.JButton tb_paste;
    private javax.swing.JButton tb_redo;
    private javax.swing.JButton tb_selectall;
    private javax.swing.JButton tb_strike;
    private javax.swing.JButton tb_textcolor;
    private javax.swing.JButton tb_underline;
    private javax.swing.JButton tb_undo;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void windowOpened(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        closeWindow();
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }
    
    private TaskProgressDialog taskDlg;
    private JDialog symbolsDlg;
    private CSynonymsEdit synonymsDlg;
    private CBiggerEditField biggerEditDlg;
    private CFormEditor formEditDlg;
    private CInsertHyperlink hyperlinkEditDlg;
    private CAutoKorrekturEdit autoKorrektEdit;
    private CStenoEdit stenoEdit;
    private CFindReplaceDialog findReplaceDlg;
    private CInsertTable insertTableDlg;
    private CInsertManualLink manualLinkEditDlg;
    private CFontChooser fontDlg;
}
