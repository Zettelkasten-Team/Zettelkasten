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

import com.explodingpixels.macwidgets.BottomBar;
import com.explodingpixels.macwidgets.BottomBarSize;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.DateComparer;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import com.explodingpixels.widgets.TableUtils;
import com.explodingpixels.widgets.WindowUtils;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.mac.MacToolbarButton;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.settings.AcceleratorKeys;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
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
import org.jdesktop.application.Application;

/**
 *
 * @author danielludecke
 */
public class SearchResultsFrame extends javax.swing.JFrame {

	/**
	 * Returns the table component of the search results window.
	 * 
	 * @return the table component of the search results window.
	 */
	public JTable getSearchFrameTable() {
		return data.getjTableResults();
	}

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
	public SearchResultsFrame(ZettelkastenView zkn, Daten d, SearchRequests sr, DesktopData desk, Settings s,
			AcceleratorKeys ak, Synonyms syn, BibTeX bib) {
		data.setSearchframe(this);
		// init variables from parameters
		data.setDataObj(d);
		data.setDesktopObj(desk);
		data.setBibtexObj(bib);
		data.setSearchrequest(sr);
		data.setSynonymsObj(syn);
		data.setAccKeys(ak);
		data.setSettingsObj(s);
		data.setMainframe(zkn);
		// check whether memory usage is logged. if so, tell logger that new entry
		// windows was opened
		if (data.getSettingsObj().isMemoryUsageLogged) {
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logged. Search Results Window opened.");
		}
		// create brushed look for window, so toolbar and window-bar become a unit
		if (data.getSettingsObj().isMacStyle()) {
			MacUtils.makeWindowLeopardStyle(getRootPane());
			// WindowUtils.createAndInstallRepaintWindowFocusListener(this);
			WindowUtils.installJComponentRepainterOnWindowFocusChanged(this.getRootPane());
		}
		// init all components
		Tools.initLocaleForDefaultActions(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class)
						.getContext().getActionMap(SearchResultsFrame.class, this));
		initComponents();
		initListeners();
		// remove border, gui-builder doesn't do this
		initBorders(data.getSettingsObj());
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// if we have mac os x with aqua, make the window look like typical
		// cocoa-applications
		if (data.getSettingsObj().isMacStyle()) {
			setupMacOSXLeopardStyle();
		}
		if (data.getSettingsObj().isSeaGlass()) {
			setupSeaGlassStyle();
		}
		// init toggle-items
		data.getViewMenuHighlight().setSelected(data.getSettingsObj().getHighlightSearchResults());
		data.getTb_highlight().setSelected(data.getSettingsObj().getHighlightSearchResults());
		data.getViewMenuShowEntry().setSelected(data.getSettingsObj().getShowSearchEntry());
		data.getjButtonResetList().setEnabled(false);
		// init table
		initTable();
		// init combobox. The automatic display-update should be managed
		// through the combobox's action listener
		initComboBox();
		// init the menu-accelerator table
		initAcceleratorTable();
		initActionMaps();
		// This method initialises the toolbar buttons. depending on the user-setting,
		// we either
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
		// get filename and find out where extension begins, so we can just set the
		// filename as title
		File f = data.getSettingsObj().getMainDataFile();
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
				setTitle(currentTitle + "- [" + fname.substring(0, extpos) + "]");
			}
		}
	}

	private void initBorders(Settings settingsObj) {
		/*
		 * Constructor for Matte Border public MatteBorder(int top, int left, int
		 * bottom, int right, Color matteColor)
		 */
		data.getjScrollPane1().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
		data.getjScrollPane4().setBorder(null);
		if (settingsObj.getUseMacBackgroundColor() || settingsObj.isMacStyle()) {
			data.getjListKeywords().setBackground(
					(settingsObj.isMacStyle()) ? ColorUtil.colorJTreeBackground : ColorUtil.colorJTreeLighterBackground);
			data.getjListKeywords().setForeground(ColorUtil.colorJTreeDarkText);
		}
		if (settingsObj.isSeaGlass()) {
			data.getjPanel3().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
			data.getjPanel4().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			data.getjListKeywords().setBorder(ZknMacWidgetFactory
					.getTitledBorder(data.getResourceMap().getString("jListKeywords.border.title"), settingsObj));
			if (settingsObj.getSearchFrameSplitLayout() == JSplitPane.HORIZONTAL_SPLIT) {
				data.getjPanel1().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
				data.getjPanel2().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			} else {
				data.getjPanel1().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
				data.getjPanel2().setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			}
			// jPanel3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
			// ColorUtil.getBorderGray(settingsObj)));
		}
		if (settingsObj.isMacStyle()) {
			ZknMacWidgetFactory.updateSplitPane(data.getjSplitPaneSearch1());
			ZknMacWidgetFactory.updateSplitPane(data.getjSplitPaneSearch2());
			data.getjListKeywords().setBorder(ZknMacWidgetFactory.getTitledBorder(
					data.getResourceMap().getString("jListKeywords.border.title"), ColorUtil.colorJTreeText, settingsObj));
		}
	}

	/**
	 * This method initializes the toolbar buttons. depending on the user-setting,
	 * we either display small, medium or large icons as toolbar-icons.
	 */
	public final void initToolbarIcons() {
		// check whether the toolbar should be displayed at all...
		if (!data.getSettingsObj().getShowIcons() && !data.getSettingsObj().getShowIconText()) {
			// if not, hide it and leave.
			data.getSearchToolbar().setVisible(false);
			// and set a border to the main panel, because the toolbar's dark border is
			// hidden
			// and remove border from the main panel
			data.getSearchMainPanel().setBorder(new MatteBorder(1, 0, 0, 0, ColorUtil.colorDarkLineGray));
			return;
		}
		// set toolbar visible
		data.getSearchToolbar().setVisible(true);
		// and remove border from the main panel
		data.getSearchMainPanel().setBorder(null);
		// init toolbar button array
		javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[] { data.getTb_copy(), data.getTb_selectall(), data.getTb_editentry(),
				data.getTb_remove(), data.getTb_manlinks(), data.getTb_luhmann(), data.getTb_bookmark(), data.getTb_desktop(), data.getTb_highlight() };
		String[] buttonNames = new String[] { "tb_copyText", "tb_selectallText", "tb_editText", "tb_deleteText",
				"tb_addmanlinksText", "tb_addluhmannText", "tb_addbookmarkText", "tb_addtodesktopText",
				"tb_highlightText" };
		String[] iconNames = new String[] { "copyIcon", "selectAllIcon", "editEntryIcon", "deleteIcon",
				"addManLinksIcon", "addLuhmannIcon", "addBookmarksIcon", "addDesktopIcon", "highlightKeywordsIcon" };
		// set toolbar-icons' text
		if (data.getSettingsObj().getShowIconText()) {
			for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
				toolbarButtons[cnt].setText(data.getToolbarResourceMap().getString(buttonNames[cnt]));
			}
		} else {
			for (javax.swing.JButton tbb : toolbarButtons) {
				tbb.setText("");
			}
		}
		// show icons, if requested
		if (data.getSettingsObj().getShowIcons()) {
			// retrieve icon theme path
			String icontheme = data.getSettingsObj().getIconThemePath();
			for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
				toolbarButtons[cnt].setIcon(new ImageIcon(
						ZettelkastenView.class.getResource(icontheme + data.getToolbarResourceMap().getString(iconNames[cnt]))));
			}
		} else {
			for (javax.swing.JButton tbb : toolbarButtons) {
				tbb.setIcon(null);
			}
		}
		if (data.getSettingsObj().isMacStyle())
			makeMacToolBar();
		if (data.getSettingsObj().isSeaGlass())
			makeSeaGlassToolbar();
	}

	private void setupSeaGlassStyle() {
		getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
		data.getjTextFieldFilterList().putClientProperty("JTextField.variant", "search");
		data.getjEditorPaneSearchEntry().setBackground(Color.white);
		data.getjButtonDeleteSearch().setBorderPainted(true);
		data.getjButtonDeleteSearch().putClientProperty("JButton.buttonType", "textured");
	}

	/**
	 * This method applies some graphical stuff so the appearance of the program is
	 * even more mac-like...
	 */
	private void setupMacOSXLeopardStyle() {

		data.getjTextFieldFilterList().putClientProperty("JTextField.variant", "search");
		MacWidgetFactory.makeEmphasizedLabel(data.getjLabel1());
		MacWidgetFactory.makeEmphasizedLabel(data.getjLabelHits());
	}

	private void makeSeaGlassToolbar() {
		Tools.makeTexturedToolBarButton(data.getTb_copy(), Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(data.getTb_selectall(), Tools.SEGMENT_POSITION_LAST);
		Tools.makeTexturedToolBarButton(data.getTb_editentry(), Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(data.getTb_remove(), Tools.SEGMENT_POSITION_LAST);
		Tools.makeTexturedToolBarButton(data.getTb_manlinks(), Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(data.getTb_luhmann(), Tools.SEGMENT_POSITION_MIDDLE);
		if (data.getSettingsObj().getShowAllIcons()) {
			Tools.makeTexturedToolBarButton(data.getTb_bookmark(), Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(data.getTb_desktop(), Tools.SEGMENT_POSITION_LAST);
		} else {
			Tools.makeTexturedToolBarButton(data.getTb_bookmark(), Tools.SEGMENT_POSITION_LAST);
		}
		Tools.makeTexturedToolBarButton(data.getTb_highlight(), Tools.SEGMENT_POSITION_ONLY);
		data.getSearchToolbar().setPreferredSize(
				new java.awt.Dimension(data.getSearchToolbar().getSize().width, Constants.seaGlassToolbarHeight));
		data.getSearchToolbar().add(new javax.swing.JToolBar.Separator(), 0);
	}

	private void makeMacToolBar() {
		// hide default toolbr
		data.getSearchToolbar().setVisible(false);
		this.remove(data.getSearchToolbar());
		// and create mac toolbar
		if (data.getSettingsObj().getShowIcons() || data.getSettingsObj().getShowIconText()) {

			UnifiedToolBar mactoolbar = new UnifiedToolBar();

			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_copy(), MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_selectall(), MacToolbarButton.SEGMENT_POSITION_LAST));
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_editentry(), MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_remove(), MacToolbarButton.SEGMENT_POSITION_LAST));
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_manlinks(), MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_luhmann(), MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			if (data.getSettingsObj().getShowAllIcons()) {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(data.getTb_bookmark(),
						MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(data.getTb_desktop(), MacToolbarButton.SEGMENT_POSITION_LAST));
			} else {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(data.getTb_bookmark(),
						MacToolbarButton.SEGMENT_POSITION_LAST));
			}
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(data.getTb_highlight(), MacToolbarButton.SEGMENT_POSITION_ONLY));

			mactoolbar.installWindowDraggerOnWindow(this);
			data.getSearchMainPanel().add(mactoolbar.getComponent(), BorderLayout.PAGE_START);
		}
		makeMacBottomBar();
	}

	private void makeMacBottomBar() {
		data.getjPanel9().setVisible(false);

		BottomBar macbottombar = new BottomBar(BottomBarSize.LARGE);
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(data.getjLabelHits()), 20);
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(data.getjLabel1()), 4);
		macbottombar.addComponentToLeft(data.getjComboBoxSearches(), 4);
		macbottombar.addComponentToLeft(data.getjButtonDeleteSearch(), 4);

		data.getjButtonDeleteSearch().setBorderPainted(true);
		data.getjButtonDeleteSearch().putClientProperty("JButton.buttonType", "textured");

		data.getSearchStatusPanel().remove(data.getjPanel9());
		data.getSearchStatusPanel().setBorder(null);
		data.getSearchStatusPanel().setLayout(new BorderLayout());
		data.getSearchStatusPanel().add(macbottombar.getComponent(), BorderLayout.PAGE_START);
	}

	/**
	 * This method sets the default font-size for tables, lists and treeviews. If
	 * the user wants to have bigger font-sizes for better viewing, the new
	 * font-size will be applied to the components here.
	 */
	private void initDefaultFontSize() {
        Font settingsTableFont = data.getSettingsObj().getTableFont();
		data.getjTableResults().setFont(settingsTableFont);
		data.getjListKeywords().setFont(settingsTableFont);
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
		stroke = KeyStroke.getKeyStroke(data.getAccKeys().getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showDesktopWindow"));
		ActionListener showDesktopWindowAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainframe().showDesktopWindow();
			}
		};
		getRootPane().registerKeyboardAction(showDesktopWindowAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		ActionListener showMainFrameAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainframe().bringToFront();
			}
		};
		getRootPane().registerKeyboardAction(showMainFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		stroke = KeyStroke.getKeyStroke(data.getAccKeys().getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showNewEntryWindow"));
		ActionListener showNewEntryFrameAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainframe().showNewEntryWindow();
			}
		};
		getRootPane().registerKeyboardAction(showNewEntryFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		data.getSearchSearchMenu().addMenuListener(new javax.swing.event.MenuListener() {
			@Override
			public void menuSelected(javax.swing.event.MenuEvent evt) {
				setListSelected(data.getjListKeywords().getSelectedIndex() != -1);
				String t1 = data.getjEditorPaneSearchEntry().getSelectedText();
				setTextSelected(t1 != null && !t1.isEmpty());
			}

			@Override
			public void menuDeselected(javax.swing.event.MenuEvent evt) {
			}

			@Override
			public void menuCanceled(javax.swing.event.MenuEvent evt) {
			}
		});
		data.getjEditorPaneSearchEntry().addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
				// get input event with additional modifiers
				java.awt.event.InputEvent inev = evt.getInputEvent();
				// check whether shift key was pressed, and if so, remove manual link
				if (inev.isControlDown() || inev.isMetaDown()) {
					// get selected entry
					int row = data.getjTableResults().getSelectedRow();
					// when we have a valid selection, go on
					if (row != -1) {
						int displayedZettel = Integer.parseInt(data.getjTableResults().getValueAt(row, 0).toString());
						if (Tools.removeHyperlink(evt.getDescription(), data.getDataObj(), displayedZettel)) {
							data.getMainframe().updateDisplay();
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					javax.swing.text.Element elem = evt.getSourceElement();
					if (elem != null) {
						AttributeSet attr = elem.getAttributes();
						AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
						if (a != null) {
							data.getjEditorPaneSearchEntry().setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
					data.getjEditorPaneSearchEntry().setToolTipText(null);
				} else {
					openAttachment(evt);
				}
			}
		});
		data.getjTableResults().addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				// this listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1)
					return;
				// only show entry on double clicl
				if (2 == evt.getClickCount())
					displayEntryInMainframe();
			}
		});
		data.getjTextFieldFilterList().addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(data.getjTableResults(), evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(data.getjTableResults(), data.getjTextFieldFilterList(),
							1);
				}
			}
		});
		//
		// Now come the mouse-listeners
		//
		// here we set up a popup-trigger for the jListEntryKeywords and how this
		// component
		// should react on mouse-clicks. a single click filters the jTableLinks, a
		// double-click
		// starts a keyword-search
		data.getjListKeywords().addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				// this listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, leeave...
				if (evt.getButton() != MouseEvent.BUTTON1)
					return;
				// on double click
				if (2 == evt.getClickCount()) {
					if (data.getjListKeywords().getSelectedIndex() != -1)
						newSearchFromKeywordsLogOr();
				}
				// on single click...
				if (1 == evt.getClickCount()) {
					highlightSegs();
				}
			}
		});
		data.getjListKeywords().addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
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
	 * This method inits the action map for several components like the tables, the
	 * treeviews or the lists. here we can associate certain keystrokes with related
	 * methods. e.g. hitting the enter-key in a table shows (activates) the related
	 * entry. <br>
	 * <br>
	 * Setting up action maps gives a better overview and is shorter than adding
	 * key-release-events to all components, although key-events would fulfill the
	 * same purpose. <br>
	 * <br>
	 * The advantage of action maps is, that dependent from the operating system we
	 * need only to associte a single action. with key-events, for each component we
	 * have to check whether the operating system is mac os or windows, and then
	 * checking for different keys, thus doubling each command: checking for F2 to
	 * edit, or checking for command+enter and also call the edit-method. using
	 * action maps, we simply as for the os once, storing the related
	 * keystroke-value as string, and than assign this string-value to the
	 * components.
	 */
	private void initActionMaps() {
		// <editor-fold defaultstate="collapsed" desc="Init of action-maps so we have
		// shortcuts for the tables">
		// create action which should be executed when the user presses
		// the enter-key
		AbstractAction a_enter = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data.getjTextFieldFilterList() == e.getSource())
					filterResultList();
				if (data.getjTableResults() == e.getSource())
					displayEntryInMainframe();
			}
		};
		// put action to the tables' actionmaps
		data.getjTextFieldFilterList().getActionMap().put("EnterKeyPressed", a_enter);
		data.getjTableResults().getActionMap().put("EnterKeyPressed", a_enter);
		// associate enter-keystroke with that action
		KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
		data.getjTextFieldFilterList().getInputMap().put(ks, "EnterKeyPressed");
		data.getjTableResults().getInputMap().put(ks, "EnterKeyPressed");
		// </editor-fold>
	}

	private void highlightSegs() {
		// and highlight text segments
		if (data.getSettingsObj().getHighlightSegments()) {
			int[] selectedValues = getSelectedEntriesFromTable();
			if (selectedValues != null && selectedValues.length > 0) {
				displayZettelContent(selectedValues[0], null);
			}
		}
	}

	/**
	 * This method sets the accelerator table for all relevant actions which should
	 * have accelerator keys. We don't use the GUI designer to set the values,
	 * because the user should have the possibility to define own accelerator keys,
	 * which are managed within the CAcceleratorKeys-class and loaed/saved via the
	 * CSettings-class
	 */
	private void initAcceleratorTable() {
		// setting up the accelerator table. we have two possibilities: either assigning
		// accelerator keys directly with an action like this:
		//
		// javax.swing.ActionMap actionMap =
		// org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class,
		// this);
		// AbstractAction ac = (AbstractAction) actionMap.get("newEntry");
		// KeyStroke controlN = KeyStroke.getKeyStroke("control N");
		// ac.putValue(AbstractAction.ACCELERATOR_KEY, controlN);
		//
		// or setting the accelerator key directly to a menu-item like this:
		//
		// newEntryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		// InputEvent.META_MASK));
		//
		// we choose the first option, because so we can easily iterate through the xml
		// file
		// and retrieve action names as well as accelerator keys. this saves a lot of
		// typing work here
		//
		// get the action map
		javax.swing.ActionMap actionMap = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
				.getActionMap(SearchResultsFrame.class, this);
		// iterate the xml file with the accelerator keys for the main window
		for (int cnt = 1; cnt <= data.getAccKeys().getCount(AcceleratorKeys.SEARCHRESULTSKEYS); cnt++) {
			// get the action's name
			String actionname = data.getAccKeys().getAcceleratorAction(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
			// check whether we have found any valid action name
			if (actionname != null && !actionname.isEmpty()) {
				// retrieve action
				AbstractAction ac = (AbstractAction) actionMap.get(actionname);
				// get the action's accelerator key
				String actionkey = data.getAccKeys().getAcceleratorKey(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
				// check whether we have any valid actionkey
				if (actionkey != null && !actionkey.isEmpty()) {
					// retrieve keystroke setting
					KeyStroke ks = KeyStroke.getKeyStroke(actionkey);
					// and put them together :-)
					ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);
				}
			}
		}
		// now set the mnemonic keys of the menus (i.e. the accelerator keys, which give
		// access
		// to the menu via "alt"+key). since the menus might have different texts,
		// depending on
		// the programs language, we retrieve the menu text and simply set the first
		// char
		// as mnemonic key
		// ATTENTION! Mnemonic keys are NOT applied on Mac OS, see Apple guidelines for
		// further details:
		// http://developer.apple.com/DOCUMENTATION/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html#//apple_ref/doc/uid/TP40001909-211867-BCIBDHFJ
		if (!data.getSettingsObj().isMacStyle()) {
			// init the variables
			String menutext;
			char mkey;
			// the mnemonic key for the file menu
			menutext = data.getSearchFileMenu().getText();
			mkey = menutext.charAt(0);
			data.getSearchFileMenu().setMnemonic(mkey);
			// the mnemonic key for the edit menu
			menutext = data.getSearchEditMenu().getText();
			mkey = menutext.charAt(0);
			data.getSearchEditMenu().setMnemonic(mkey);
			// the mnemonic key for the filter menu
			menutext = data.getSearchFilterMenu().getText();
			mkey = menutext.charAt(0);
			data.getSearchFilterMenu().setMnemonic(mkey);
			// the mnemonic key for the search menu
			menutext = data.getSearchSearchMenu().getText();
			mkey = menutext.charAt(0);
			data.getSearchSearchMenu().setMnemonic(mkey);
			// the mnemonic key for the view menu
			menutext = data.getSearchViewMenu().getText();
			mkey = menutext.charAt(0);
			data.getSearchViewMenu().setMnemonic(mkey);
		}
		// on Mac OS, at least for the German locale, the File menu is called different
		// compared to windows or linux. Furthermore, we don't need the about and
		// preferences
		// menu items, since these are locates on the program's menu item in the
		// apple-menu-bar
		if (PlatformUtil.isMacOS())
			data.getSearchFileMenu().setText(data.getResourceMap().getString("macFileMenuText"));
		// en- or disable fullscreen icons
		setFullScreenSupp(data.getGraphicdevice().isFullScreenSupported());
		// if full screen is not supported, tell this in the tool tip
		if (!data.getGraphicdevice().isFullScreenSupported()) {
			AbstractAction ac = (AbstractAction) actionMap.get("viewFullScreen");
			ac.putValue(AbstractAction.SHORT_DESCRIPTION, data.getResourceMap().getString("fullScreenNotSupported"));
		}
	}

	/**
	 * This option toggles the setting, whether a selected entry from the search
	 * results should also immediately be displayed in the main frame or not.
	 */
	@Action
	public void showEntryImmediately() {
		data.getSettingsObj().setShowSearchEntry(!data.getSettingsObj().getShowSearchEntry());
	}

	@Action
	public void resetResultslist() {
		prepareResultList(data.getjComboBoxSearches().getSelectedIndex());
		// set input focus to the table, so key-navigation can start immediately
		data.getjTableResults().requestFocusInWindow();
		// finally, select first entry
		try {
			data.getjTableResults().setRowSelectionInterval(0, 0);
		} catch (IllegalArgumentException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// enable refresh button
		data.getjButtonResetList().setEnabled(false);
	}

	private void filterResultList() {
		// when we filter the table and want to restore it, we don't need to run the
		// time-consuming task that creates the author-list and related
		// author-frequencies.
		// instead, we simply copy the values from the linkedlist to the table-model,
		// which is
		// much faster. but therefore we have to apply all changes to the filtered-table
		// (like adding/changing values in a filtered list) to the linked list as well.

		// get text from the textfield containing the filter string
		// convert to lowercase, we don't want case-sensitive search
		String text = data.getjTextFieldFilterList().getText().toLowerCase();
		// tell selection listener to do nothing...
		data.setTableUpdateActive(true);
		// when we have no text, do nothing
		if (!text.isEmpty()) {
			// get table model
			DefaultTableModel dtm = (DefaultTableModel) data.getjTableResults().getModel();
			// go through table and delete all rows that don't contain the filter text
			for (int cnt = (data.getjTableResults().getRowCount() - 1); cnt >= 0; cnt--) {
				// retrieve row-index from the model
				int rowindex = data.getjTableResults().convertRowIndexToModel(cnt);
				// get the string (author) value from the table
				// convert to lowercase, we don't want case-sensitive search
				String value = dtm.getValueAt(rowindex, 1).toString().toLowerCase();
				// in case we have the jTableTitles, we also add the timestamps and
				// rating-values to the filter-value
				// so we can also filter entries according to their timestamp
				value = value + dtm.getValueAt(rowindex, 2).toString() + dtm.getValueAt(rowindex, 3).toString()
						+ dtm.getValueAt(rowindex, 4).toString();
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
					} catch (PatternSyntaxException ex) {
						// in case of invalid regex, simply try to find the usual pattern
						if (!value.contains(text))
							dtm.removeRow(rowindex);
					}
				}
				// if the text is *not* part of the column, delete that row
				else if (!value.contains(text))
					dtm.removeRow(rowindex);
			}
			// reset textfield
			data.getjTextFieldFilterList().setText("");
			data.getjTextFieldFilterList().requestFocusInWindow();
			// enable textfield only if we have more than 1 element in the jtable
			data.getjTextFieldFilterList().setEnabled(data.getjTableResults().getRowCount() > 0);
			// enable refresh button
			data.getjButtonResetList().setEnabled(true);
			// create a new stringbuilder to prepare the label
			// that shows the amount of found entries
			StringBuilder sb = new StringBuilder("");
			sb.append("(");
			sb.append(String.valueOf(dtm.getRowCount()));
			sb.append(" ");
			sb.append(data.getResourceMap().getString("hitsText"));
			sb.append(")");
			// set labeltext
			data.getjLabelHits().setText(sb.toString());
		}
		// tell selection listener action is possible again...
		data.setTableUpdateActive(false);
	}

	/**
	 * This option toggles the setting whether search terms should be highlighted or
	 * not.
	 */
	@Action
	public void toggleHighlightResults() {
		// check whether highlighting is activated
		if (!data.getSettingsObj().getHighlightSearchResults()) {
			// if not, activate it
			data.getSettingsObj().setHighlightSearchResults(true);
		} else {
			// nex, if highlighting is activated,
			// check whether whole word highlighting is activated
			if (!data.getSettingsObj().getHighlightWholeWordSearch()) {
				// if not, activate whole-word-highlighting and do not
				// deactivate general highlighting
				data.getSettingsObj().setHighlightWholeWordSearch(true);
			}
			// else if both were activated, deactivate all
			else {
				data.getSettingsObj().setHighlightSearchResults(false);
				data.getSettingsObj().setHighlightWholeWordSearch(false);
			}
		}
		updateDisplay();
	}

	@Action
	public void addKeywordsToEntries() {
		// create linked list as parameter for filter-dialog
		LinkedList<String> keywords = new LinkedList<>();
		// go through all keyword-entries
		for (int cnt = 1; cnt <= data.getDataObj().getCount(Daten.KWCOUNT); cnt++) {
			// get keyword
			String k = data.getDataObj().getKeyword(cnt);
			// add it to list
			if (!k.isEmpty())
				keywords.add(k);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettingsObj(), keywords,
					data.getResourceMap().getString("addKeywordsToEntriesTitle"), false));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// when we have any selected keywords, go on and add them all to all the
		// selected
		// entries in the search result
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			// get all selected entries
			int[] entries = getSelectedEntriesFromTable();
			// go through all selected entries
			// now iterate the chosen keywords
			// and add each keyword to all selected entries
			for (int e : entries)
				data.getDataObj().addKeywordsToEntry(data.getFilterSearchDlg().getFilterTerms(), e, 1);
			// keyword-list is not up-to-date
			data.getDataObj().setKeywordlistUpToDate(false);
			// update the display
			updateDisplay();
		}
		// dispose window...
		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	@Action
	public void switchLayout() {
		int currentlayout = data.getSettingsObj().getSearchFrameSplitLayout();
		if (JSplitPane.HORIZONTAL_SPLIT == currentlayout) {
			currentlayout = JSplitPane.VERTICAL_SPLIT;
			if (data.getSettingsObj().isSeaGlass()) {
				data.getjPanel1().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(data.getSettingsObj())));
				data.getjPanel2().setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(data.getSettingsObj())));
			}
		} else {
			currentlayout = JSplitPane.HORIZONTAL_SPLIT;
			if (data.getSettingsObj().isSeaGlass()) {
				data.getjPanel1().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(data.getSettingsObj())));
				data.getjPanel2().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(data.getSettingsObj())));
			}
		}
		data.getSettingsObj().setSearchFrameSplitLayout(currentlayout);
		data.getjSplitPaneSearch1().setOrientation(currentlayout);
		if (data.getSettingsObj().isMacStyle())
			ZknMacWidgetFactory.updateSplitPane(data.getjSplitPaneSearch1());
	}

	@Action
	public void showEntryInDesktop() {
		// get selected row
		int row = data.getjTableResults().getSelectedRow();
		// check for valid value
		if (row != -1) {
			try {
				int nr = Integer.parseInt(data.getjTableResults().getValueAt(row, 0).toString());
				if (data.getDesktopObj().isEntryInAnyDesktop(nr)) {
					data.getMainframe().showEntryInDesktopWindow(nr);
				}
			} catch (NumberFormatException ex) {
			}
		}
	}

	@Action
	public void addAuthorsToEntries() {
		// create linked list as parameter for filter-dialog
		LinkedList<String> suthors = new LinkedList<>();
		// go through all author-entries
		for (int cnt = 1; cnt <= data.getDataObj().getCount(Daten.AUCOUNT); cnt++) {
			// get authors
			String a = data.getDataObj().getAuthor(cnt);
			// add it to list
			if (!a.isEmpty())
				suthors.add(a);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettingsObj(), suthors,
					data.getResourceMap().getString("addAuthorsToEntriesTitle"), false));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// when we have any selected keywords, go on and add them all to all the
		// selected
		// entries in the search result
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			// get all selected entries
			int[] entries = getSelectedEntriesFromTable();
			// go through all selected entries
			for (int e : entries) {
				// now iterate the chosen authors
				// and add each author to all selected entries
				for (String a : data.getFilterSearchDlg().getFilterTerms())
					data.getDataObj().addAuthorToEntry(a, e, 1);
			}
			// author-list is not up-to-date
			data.getDataObj().setAuthorlistUpToDate(false);
			// update the display
			updateDisplay();
		}
		// dispose window...
		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	/**
	 * This method inits the combo-boxes, i.e. filling it with search-result-entries
	 * and setting up an action listener. The action-listener will update the
	 * jTableResults with the search-result-entrynumbers and update the display
	 * (filling the textfields).
	 */
	private void initComboBox() {
		// clear combobox
		data.getjComboBoxSearches().removeAllItems();

		for (int cnt = 0; cnt < data.getSearchrequest().getCount(); cnt++) {
			data.getjComboBoxSearches().addItem(data.getSearchrequest().getShortDescription(cnt));
		}
		// add action listener to combo box
		data.getjComboBoxSearches().addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// set all results, i.e. all entry-numbers and the entries' titles, into the
				// search result table
				prepareResultList(data.getjComboBoxSearches().getSelectedIndex());
				// and update the display, i.e. show the entry's content
				updateDisplay();
				// finally, select first entry
				try {
					data.getjTableResults().setRowSelectionInterval(0, 0);
				} catch (IllegalArgumentException e) {
					Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
				}
				// set inputfocus to the table, so key-navigation can start immediately
				data.getjTableResults().requestFocusInWindow();
			}
		});
		try {
			// select first item
			data.getjComboBoxSearches().setSelectedIndex(0);
		} catch (IllegalArgumentException ex) {
			// log error
			Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
		}
	}

	/**
	 * This method initializes the table.<br>
	 * <br>
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
		newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
		data.getjTableResults().setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				Collections.unmodifiableSet(newForwardKeys));
		// bind our new backward focus traversal keys
		Set<AWTKeyStroke> newBackwardKeys = new HashSet<>(1);
		newBackwardKeys
				.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK));
		data.getjTableResults().setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				Collections.unmodifiableSet(newBackwardKeys));
		// create new table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>();
		// tell tgis jtable that it has an own sorter
		data.getjTableResults().setRowSorter(sorter);
		// and tell the sorter, which table model to sort.
		sorter.setModel((DefaultTableModel) data.getjTableResults().getModel());
		// in this table, the first column needs a custom comparator.
		try {
			// sorter for titles
			sorter.setComparator(1, new Comparer());
			// sorter for desktop names
			sorter.setComparator(5, new Comparer());
			// this table has two more columns that should be sorted, the columns with
			// the entries timestamps.
			sorter.setComparator(2, new DateComparer());
			sorter.setComparator(3, new DateComparer());
		} catch (IndexOutOfBoundsException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// get last table sorting
		RowSorter.SortKey sk = data.getSettingsObj().getTableSorting(data.getjTableResults());
		// any sorting found?
		if (sk != null) {
			// create array with sort key
			ArrayList<SortKey> l = new ArrayList<SortKey>();
			l.add(sk);
			// set sort key to table
			sorter.setSortKeys(l);
			// sort table
			sorter.sort();
		}
		// make extra table-sorter for itunes-tables
		if (data.getSettingsObj().isMacStyle()) {
			TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
				@Override
				public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
				}
			};
			TableUtils.makeSortable(data.getjTableResults(), sortDelegate);
			// change back default column-resize-behaviour when we have itunes-tables,
			// since the default for those is "auto resize off"
			data.getjTableResults().setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
		data.getjTableResults().setShowHorizontalLines(data.getSettingsObj().getShowGridHorizontal());
		data.getjTableResults().setShowVerticalLines(data.getSettingsObj().getShowGridVertical());
		data.getjTableResults().setIntercellSpacing(data.getSettingsObj().getCellSpacing());
		data.getjTableResults().getTableHeader().setReorderingAllowed(false);
		// if the user wants to see grids, we need to change the gridcolor on mac-aqua
		data.getjTableResults().setGridColor(data.getSettingsObj().getTableGridColor());
		SelectionListener listener = new SelectionListener(data.getjTableResults());
		data.getjTableResults().getSelectionModel().addListSelectionListener(listener);
		data.getjTableResults().getColumnModel().getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * This method updates the combobox, when new search results are added or former
	 * search requests are deleted. therefor, we have to temporarily remove the
	 * action listener, because changing the combobox-content would fire several
	 * actions, which may interfer with our updating-process
	 * 
	 * @param selectedrow here we can pass a table row that should be selected after
	 *                    updating the combo-box. use "0" to select the first entry
	 *                    in the table, "-1" to select the last selection (if any)
	 *                    or any other value.
	 * @param searchnr    the number of the searchrequest that should be displayed.
	 *                    use "-1" to show the default search-request, which is
	 *                    either the currently used search-request, or - if it was
	 *                    deleted - the last search request. use any other number
	 *                    for a specific search request.
	 */
	public void updateComboBox(int selectedrow, int searchnr) {
		// init variable
		int selection;
		// check whether we have any parameter
		if (searchnr != -1)
			selection = searchnr;
		// remember current selection for later use, see below
		else
			selection = data.getjComboBoxSearches().getSelectedIndex();
		// used for tablerowselection
		int row;
		// if we have a parameter for row-selection, set it here
		if (selectedrow != -1)
			row = selectedrow;
		// remember selected row...
		else
			row = data.getjTableResults().getSelectedRow();
		// get all action listeners from the combo box
		ActionListener[] al = data.getjComboBoxSearches().getActionListeners();
		// remove all action listeners so we don't fire several action-events
		// when we update the combo box. we can set the action listener later again
		for (ActionListener listener : al)
			data.getjComboBoxSearches().removeActionListener(listener);
		// clear combobox
		data.getjComboBoxSearches().removeAllItems();
		// add search descriptions to combobox
		for (int cnt = 0; cnt < data.getSearchrequest().getCount(); cnt++)
			data.getjComboBoxSearches().addItem(data.getSearchrequest().getShortDescription(cnt));
		// add action listener to combo box
		data.getjComboBoxSearches().addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Set all results, i.e. all entry-numbers and the entries' titles, into the
				// search result table
				prepareResultList(data.getjComboBoxSearches().getSelectedIndex());
				// and update the display, i.e. show the entry's content
				updateDisplay();
				// Set inputfocus to the table, so key-navigation can start immediately
				data.getjTableResults().requestFocusInWindow();
				// finally, select first entry
				try {
					data.getjTableResults().setRowSelectionInterval(0, 0);
				} catch (IllegalArgumentException e) {
					Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
				}
			}
		});
		// if we have any searchrequests at all, go on here
		if (data.getSearchrequest().getCount() > 0) {
			// check whether the last selected searchrequest is still available
			// if not, choose the last search request in the combobox...
			if (selection != data.getSearchrequest().getCurrentSearch())
				selection = data.getjComboBoxSearches().getItemCount() - 1;
			// Select search request
			data.getjComboBoxSearches().setSelectedIndex(selection);
			// if we had no prevous selection, set row-selector to first item.
			if (-1 == row)
				row = 0;
			// if the selected row was the last value, set row-counter to last row
			else if (row >= data.getjTableResults().getRowCount())
				row = data.getjTableResults().getRowCount() - 1;
			// finally...
			try {
				// Select the appropriate table-entry
				data.getjTableResults().setRowSelectionInterval(row, row);
				// and make sure it is visible...
				data.getjTableResults().scrollRectToVisible(data.getjTableResults().getCellRect(row, 0, false));
			} catch (IllegalArgumentException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
		// make window invisible
		else {
			setVisible(false);
			// and disable hotkey
			data.getMainframe().setSearchResultsAvailable(false);
		}
	}

	/**
	 * This method retrieves the result-entry-numbers from a search request "nr" and
	 * fills the jTableResult with those entry-numbers and the entries' related
	 * titles.
	 * 
	 * @param searchrequestnr the search request of which we want to display the
	 *                        search results.
	 */
	private void prepareResultList(int searchrequestnr) {
		// get search results
		int[] result = data.getSearchrequest().getSearchResults(searchrequestnr);
		// Save current search request number
		data.getSearchrequest().setCurrentSearch(searchrequestnr);
		// check whether we have any results
		if (result != null) {
			// tell selection listener to do nothing...
			data.setTableUpdateActive(true);
			// Sort the array with the entry-numbers of the search result
			if (result.length > 0)
				Arrays.sort(result);
			// get the table model
			DefaultTableModel dtm = (DefaultTableModel) data.getjTableResults().getModel();
			// clear table
			dtm.setRowCount(0);
			// iterate the result-array
			for (int cnt = 0; cnt < result.length; cnt++) {
				// create a new object
				Object[] ob = new Object[6];
				// Store the information in that object
				// first the entry number
				ob[0] = result[cnt];
				// then the entry's title
				ob[1] = data.getDataObj().getZettelTitle(result[cnt]);
				// get timestamp
				String[] timestamp = data.getDataObj().getTimestamp(result[cnt]);
				// init timestamp variables.
				String created = "";
				String edited = "";
				// check whether we have any timestamp at all.
				if (timestamp != null && !timestamp[0].isEmpty() && timestamp[0].length() >= 6)
					created = timestamp[0].substring(4, 6) + "." + timestamp[0].substring(2, 4) + ".20"
							+ timestamp[0].substring(0, 2);
				// check whether we have any timestamp at all.
				if (timestamp != null && !timestamp[1].isEmpty() && timestamp[1].length() >= 6)
					edited = timestamp[1].substring(4, 6) + "." + timestamp[1].substring(2, 4) + ".20"
							+ timestamp[1].substring(0, 2);
				ob[2] = created;
				ob[3] = edited;
				// now, the entry's rating
				ob[4] = data.getDataObj().getZettelRating(result[cnt]);
				// finally, check whether entry is on any desktop, and if so,
				// use desktop name in that column
				ob[5] = data.getDesktopObj().getDesktopNameOfEntry(result[cnt]);
				// and add that content as a new row to the table
				dtm.addRow(ob);
			}
			// create a new stringbuilder to prepare the label
			// that shows the amount of found entries
			StringBuilder sb = new StringBuilder("");
			sb.append("(");
			sb.append(String.valueOf(dtm.getRowCount()));
			sb.append(" ");
			sb.append(data.getResourceMap().getString("hitsText"));
			sb.append(")");
			// Set labeltext
			data.getjLabelHits().setText(sb.toString());
			// work done
			data.setTableUpdateActive(false);
			// enable filter text field
			data.getjTextFieldFilterList().setEnabled(true);
		}
	}

	/**
	 * This method updates the display, i.e. it retrieves the selected entry from
	 * the jTableResults and fills the textfields with content (displaying the
	 * entry).
	 */
	private void updateDisplay() {
		// get selected row
		int row = data.getjTableResults().getSelectedRow();
		// if we have any selections, go on
		if (row != -1) {
			// retrieve the value...
			Object o = data.getjTableResults().getValueAt(row, 0);
			try {
				// ...and try to convert it to an integer value
				int selection = Integer.parseInt(o.toString());
				// prepare array for search terms which might be highlighted
				String[] sts = getHighlightSearchterms();
				displayZettelContent(selection, sts);
				//
				// Here we set up the keywordlist for the JList
				//
				// retrieve the keywords of the selected entry
				String[] kws = data.getDataObj().getKeywords(selection);
				// prepare the JList which will display the keywords
				data.getKeywordListModel().clear();
				// check whether any keywords have been found
				if (kws != null) {
					// Sort the array
					if (kws.length > 0)
						Arrays.sort(kws);
					// iterate the string array and add its content to the list model
					for (String kw : kws)
						data.getKeywordListModel().addElement(kw);
				}
				// if we have any search terms, we want to select the related keywords...
				if (sts != null) {
					// create an integer list
					LinkedList<Integer> l = new LinkedList<>();
					// iterate all search terms
					for (String s : sts) {
						// try to find the keyword in the jList
						for (int cnt = 0; cnt < data.getKeywordListModel().getSize(); cnt++)
							if (s.equalsIgnoreCase(data.getKeywordListModel().get(cnt).toString()))
								l.add(cnt);
					}
					// create int-array
					int[] selections = new int[l.size()];
					// copy all elements of the list to the array
					for (int cnt = 0; cnt < l.size(); cnt++)
						selections[cnt] = l.get(cnt);
					// Set selected indices for the jList
					data.getjListKeywords().setSelectedIndices(selections);
				}
				// if we don't have highlighting, clear selection
				else
					data.getjListKeywords().clearSelection();
				// if we want to update the entry immediately, show entry in mainframe as well
				if (data.getSettingsObj().getShowSearchEntry())
					data.getMainframe().setNewActivatedEntryAndUpdateDisplay(selection);
				// finally, set desktop selected
				// setDesktopEntrySelected(desktopObj.isEntryInAnyDesktop(selection));
			} catch (NumberFormatException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		} else {
			data.getjEditorPaneSearchEntry().setText("");
			data.getKeywordListModel().clear();
		}
	}

	public void updateDisplayAfterEditing() {
		// get selected row
		int row = data.getjTableResults().getSelectedRow();
		// if we have any selections, go on
		if (row != -1) {
			// retrieve the value...
			Object o = data.getjTableResults().getValueAt(row, 0);
			try {
				// ...and try to convert it to an integer value
				int selection = Integer.parseInt(o.toString());
				// prepare array for search terms which might be highlighted
				String[] sts = getHighlightSearchterms();
				displayZettelContent(selection, sts);
			} catch (NumberFormatException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
	}

	private String[] getHighlightSearchterms() {
		// prepare array for search terms which might be highlighted
		String[] sts = null;
		// get search terms, if highlighting is requested
		if (data.getSettingsObj().getHighlightSearchResults()) {
			// get the selected index, i.e. the searchrequest we want to retrieve
			int index = data.getjComboBoxSearches().getSelectedIndex();
			// get the related search terms
			sts = data.getSearchrequest().getSearchTerms(index);
			// check whether the search was a synonym-search. if yes, add synonyms to search
			// terms
			if (data.getSearchrequest().isSynonymSearch(index)) {
				// create new linked list that will contain all highlight-terms, including
				// the related synonyms of the highlight-terms
				LinkedList<String> highlight = new LinkedList<>();
				// go through all searchterms
				for (String s : sts) {
					// get the synonym-line for each search term
					String[] synline = data.getSynonymsObj().getSynonymLineFromAny(s, false);
					// if we have synonyms...
					if (synline != null) {
						// add them to the linked list, if they are new
						for (String sy : synline) {
							if (!highlight.contains(sy))
								highlight.add(sy);
						}
					}
					// else simply add the search term to the linked list
					else if (!highlight.contains(s)) {
						highlight.add(s);
					}
				}
				if (highlight.size() > 0)
					sts = highlight.toArray(new String[highlight.size()]);
			}
		}
		return sts;
	}

	void displayZettelContent(int nr, String[] highlightterms) {
		// Set highlight search terms
		HtmlUbbUtil.setHighlighTerms(highlightterms, HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS,
				data.getSettingsObj().getHighlightWholeWordSearch());
		// retrieve the string array of the first entry
		String disp = data.getDataObj().getEntryAsHtml(nr,
				(data.getSettingsObj().getHighlightSegments()) ? getSelectedKeywordsFromList() : null, Constants.FRAME_SEARCH);
		// in case parsing was ok, display the entry
		if (Tools.isValidHTML(disp, nr)) {
			// Set entry information in the main textfield
			data.getjEditorPaneSearchEntry().setText(disp);
		}
		// else show error message box to user and tell him what to do
		else {
			StringBuilder cleanedContent = new StringBuilder("");
			cleanedContent
					.append("<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">");
			URL imgURL = org.jdesktop.application.Application
					.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass()
					.getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
			cleanedContent.append("<img border=\"0\" src=\"").append(imgURL).append("\">&#8195;");
			cleanedContent.append(data.getResourceMap().getString("incorrectNestedTagsText"));
			cleanedContent.append("</div>").append(data.getDataObj().getCleanZettelContent(nr)).append("</body>");
			// and display clean content instead
			data.getjEditorPaneSearchEntry().setText(cleanedContent.toString());
		}
		// place caret, so content scrolls to top
		data.getjEditorPaneSearchEntry().setCaretPosition(0);
	}

	@Action
	public void exportEntries() {
		// retrieve the selected index from the combo box, so we know the search result.
		// then get the related search results (entries as integer array) from the
		// search-reuest
		// finally, call the mainframe's exportwindow-method and pass the int-array with
		// the entry-numbers
		data.getMainframe().exportEntries(data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex()));
	}

	@Action
	public void editEntry() {
		// get selected entry
		int row = data.getjTableResults().getSelectedRow();
		// when we have a valid selection, go on
		if (row != -1) {
			// remember that entry editing came from search window
			data.getMainframe().editEntryFromSearchWindow = true;
			// open edit window
			data.getMainframe().openEditWindow(true, Integer.parseInt(data.getjTableResults().getValueAt(row, 0).toString()), false, false,
					-1);
		}
	}

	@Action
	public void duplicateSearch() {
		data.getSearchrequest().duplicateSearchRequest();
		updateComboBox(0, -1);
	}

	@Action
	public void findAndReplace() {
		// find and replace within search-results-entries, and update display if we have
		// any replacements.
		if (data.getMainframe().replace(data.getSearchframe(), null, getSelectedEntriesFromTable()))
			updateDisplay();
	}

	/**
	 * This method gets all selected elements of the jListEntryKeywords and returns
	 * them in an array.
	 *
	 * @return a string-array containing all selected keywords, or null if no
	 *         selection made
	 */
	private String[] getSelectedKeywordsFromList() {
		// get selected values
		List<String> values = data.getjListKeywords().getSelectedValuesList();
		// if we have any selections, go on
		if (!values.isEmpty()) {
			// create string array for selected values
			// return complete array
			return values.toArray(new String[values.size()]);
		}
		// ...or null, if error occurred.
		return null;
	}

	@Action(enabledProperty = "textSelected")
	public void newSearchFromSelection() {
		// open the search dialog
		// the parameters are as following:
		data.getMainframe().startSearch(new String[] { data.getjEditorPaneSearchEntry().getSelectedText() }, // string-array with search
																							// terms
				Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
				Constants.LOG_OR, // the logical combination
				false, // whole-word-search
				false, // match-case-search
				data.getSettingsObj().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettingsObj().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogOr() {
		// open the search dialog
		// the parameters are as following:
		data.getMainframe().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_OR, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettingsObj().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettingsObj().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogAnd() {
		// open the search dialog
		// the parameters are as following:
		data.getMainframe().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_AND, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettingsObj().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettingsObj().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogNot() {
		// open the search dialog
		// the parameters are as following:
		data.getMainframe().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_NOT, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettingsObj().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettingsObj().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	/**
	 * This method opens the usual find-dialog and lets the user enter a "new"
	 * search request. the current search results are then filtered according to the
	 * search-parameters entered by the user. a new searchresult is being displayed
	 * after that. <br>
	 * <br>
	 * So the user can create a new search result with those previous entries
	 * removed that do not match the search criteria.
	 */
	@Action
	public void filterSearch() {
		// if dialog window isn't already created, do this now
		if (null == data.getSearchDlg()) {
			// create a new dialog window
			data.setSearchDlg(new CSearchDlg(this, data.getSearchrequest(), data.getSettingsObj(), null));
			// center window
			data.getSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (!data.getSearchDlg().isCancelled()) {
			startSearch(Constants.SEARCH_USUAL, data.getSearchDlg().getSearchTerms(),
					data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex()), data.getSearchDlg().getWhereToSearch(),
					data.getSearchDlg().getLogical(), data.getSearchDlg().isWholeWord(), data.getSearchDlg().isMatchCase(),
					data.getSearchDlg().isSynonymsIncluded(), data.getSearchDlg().isAccentInsensitive(), data.getSearchDlg().isRegExSearch(),
					data.getSearchDlg().isTimestampSearch(), data.getSearchDlg().getDateFromValue(), data.getSearchDlg().getDateToValue(),
					data.getSearchDlg().getTimestampIndex());
		}

		data.getSearchDlg().dispose();
		data.setSearchDlg(null);
	}

	/**
	 * This method opens a dialog with a list that contains all keywords of the
	 * current search result's entries. The user can than choose keywords from this
	 * list and filter the search results, i.e. creating a new search result with
	 * those previous entries removed that do not match the search criteria (i.e.:
	 * don't have the selected keywords).
	 */
	@Action
	public void filterKeywords() {
		// retrieve current entries from the list
		int[] entries = data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex());
		// create linked list as parameter for filter-dialog
		LinkedList<String> keywords = new LinkedList<>();
		// go through all entries
		for (int e : entries) {
			// get keywords of each entries
			String[] kws = data.getDataObj().getKeywords(e);
			// now go through all keywords of that entry
			// if keyword does not exist, add it to list
			if (kws != null)
				for (String k : kws)
					if (!keywords.contains(k))
						keywords.add(k);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettingsObj(), keywords, null, true));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			startSearch(Constants.SEARCH_USUAL, data.getFilterSearchDlg().getFilterTerms(),
					data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex()), Constants.SEARCH_KEYWORDS,
					data.getFilterSearchDlg().getLogical(), true, true, data.getSettingsObj().getSearchAlwaysSynonyms(), false,
					/* accentInsensitive= */false, false, "", "", 0);
		}

		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
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
		startSearch(Constants.SEARCH_TOP_LEVEL_LUHMANN, null,
				data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex()), -1, Constants.LOG_OR, false,
				false, false, false, /* accentInsensitive= */false, false, null, null, 0);
	}

	/**
	 * This method opens a dialog with a list that contains all authors of the
	 * current search result's entries. The user can than choose authors from this
	 * list and filter the search results, i.e. creating a new search result with
	 * those previous entries removed that do not match the search criteria (i.e.:
	 * don't have the selected authors).
	 */
	@Action
	public void filterAuthors() {
		// retrieve current entries from the list
		int[] entries = data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex());
		// create linked list as parameter for filter-dialog
		LinkedList<String> authors = new LinkedList<>();
		// go through all entries
		for (int e : entries) {
			// get authors of each entries
			String[] aus = data.getDataObj().getAuthors(e);
			// now go through all keywords of that entry
			// if keyword does not exist, add it to list
			if (aus != null)
				for (String a : aus)
					if (!authors.contains(a))
						authors.add(a);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettingsObj(), authors, null, true));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			startSearch(Constants.SEARCH_USUAL, data.getFilterSearchDlg().getFilterTerms(),
					data.getSearchrequest().getSearchResults(data.getjComboBoxSearches().getSelectedIndex()), Constants.SEARCH_AUTHOR,
					data.getFilterSearchDlg().getLogical(), true, true, data.getSettingsObj().getSearchAlwaysSynonyms(), false,
					/* accentInsensitive= */false, false, "", "", 0);
		}

		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	/**
	 * Opens the search dialog. <br>
	 * <br>
	 * In case of keyword- and author-search <i>from the table</i> (lists), we can
	 * neglect the last parameter, since keyword- and author-search simply functions
	 * by searching for the index-numbers, that are always - or never - case
	 * sensitive relevant. <br>
	 * <br>
	 * When we have searchterms from the search-dialog, the user also can search for
	 * <i>parts</i> inside a keyword-string, so here the whole-word-parameter is
	 * relevant, since we then don't compare by index- numbers, but by the
	 * string-value of the keywords/authors.
	 * 
	 * @param searchterms    string-array with search terms
	 * @param searchin       the entries where the search should be apllied to, i.e.
	 *                       when we want to filter a certain search result
	 * @param where          the type of search, i.e. where to look, e.g. searching
	 *                       for keywords, authors, text etc.
	 * @param logand         logical-and-combination
	 * @param wholeword      whether we look for whole words or also parts of a
	 *                       word/phrase
	 * @param matchcase      whether the search should be case sensitive or not
	 * @param synonyms       whether the search should include synonyms or not
	 * @param timesearch     whether the user requested a time-search, i.e. a search
	 *                       for entries that were created or changed within a
	 *                       certain period
	 * @param datefrom       the start of the period, when a timesearch is
	 *                       requested. format: "yymmdd".
	 * @param dateto         the end of the period, when a timesearch is requested.
	 *                       format: "yymmdd".
	 * @param timestampindex
	 */
	private void startSearch(int searchtype, String[] searchterms, int[] searchin, int where, int logical,
			boolean wholeword, boolean matchcase, boolean syno, boolean accentInsensitive, boolean regex,
			boolean timesearch, String datefrom, String dateto, int timestampindex) {
		// check whether we have valid searchterms or not...
		if ((null == searchterms || searchterms.length < 1) && searchtype != Constants.SEARCH_TOP_LEVEL_LUHMANN)
			return;
		// if dialog window isn't already created, do this now
		if (null == data.getTaskDlg()) {
			// get parent und init window
			data.setTaskDlg(new TaskProgressDialog(this, TaskProgressDialog.TASK_SEARCH, data.getDataObj(), data.getSearchrequest(), data.getSynonymsObj(),
					searchtype, searchterms, searchin, where, logical, wholeword, matchcase, syno, accentInsensitive,
					regex, timesearch, datefrom, dateto, timestampindex, false,
					data.getSettingsObj().getSearchRemovesFormatTags()));
			// center window
			data.getTaskDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getTaskDlg());
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		// dispose the window and clear the object
		data.getTaskDlg().dispose();
		data.setTaskDlg(null);
		// check whether we have any search results at all
		if (data.getSearchrequest().getCurrentSearchResults() != null) {
			showLatestSearchResult();
		} else {
			// display error message box that nothing was found
			JOptionPane.showMessageDialog(this, data.getResourceMap().getString("errNothingFoundMsg"),
					data.getResourceMap().getString("errNothingFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	@Action
	public void showLongDesc() {
		// display long description
		JOptionPane.showMessageDialog(null, data.getSearchrequest().getLongDescription(data.getjComboBoxSearches().getSelectedIndex()),
				data.getResourceMap().getString("longDescTitle"), JOptionPane.PLAIN_MESSAGE);
	}

	@Action
	public void showHighlightSettings() {
		if (null == data.getHighlightSettingsDlg()) {
			data.setHighlightSettingsDlg(new CHighlightSearchSettings(this, data.getSettingsObj(),
					HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS));
			data.getHighlightSettingsDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getHighlightSettingsDlg());
		data.getHighlightSettingsDlg().dispose();
		data.setHighlightSettingsDlg(null);

		updateDisplay();
	}

	/**
	 * This method retrieves the selected entries and adds them to the deskop, by
	 * calling the mainframe's method addToDesktop().
	 */
	@Action
	public void addToDesktop() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values, add them to desktop
		if ((entries != null) && (entries.length > 0))
			data.getMainframe().addToDesktop(entries);
	}

	/**
	 * This method retrieves the selected entries and adds them to the deskop, by
	 * calling the mainframe's method addToDesktop().
	 */
	@Action
	public void addToBookmarks() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as bookmarks
			data.getMainframe().addToBookmarks(entries, false);
			// and display related tab
			data.getMainframe().menuShowBookmarks();
		}
	}

	/**
	 * This method retrieves the selected entries and adds them as follower-numbers
	 * to that entry that is selected in the mainframe's luhmann-tab, in the
	 * jTreeLuhmann.
	 */
	@Action
	public void addToLuhmann() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as followers
			data.getMainframe().addToLuhmann(entries);
			// and display related tab
			data.getMainframe().menuShowLuhmann();
		}
	}

	/**
	 * This method retrieves the selected entries and adds them as manual link to
	 * the mainframe's current entry.
	 */
	@Action
	public void addToManLinks() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as followers
			data.getMainframe().addToManLinks(entries);
			// and display related tab
			data.getMainframe().menuShowLinks();
		}
	}

	/**
	 * Selects all entries in the table with the search results
	 */
	@Action
	public void selectAll() {
		data.getjTableResults().selectAll();
	}

	/**
	 * This method gets all selected elements of the jTableResults and returns them
	 * in an array.
	 * 
	 * @return a integer-array containing all selected entries, or null if no
	 *         selection made
	 */
	private int[] getSelectedEntriesFromTable() {
		// get selected rows
		int[] rows = data.getjTableResults().getSelectedRows();
		// if we have any selections, go on
		if (rows != null && rows.length > 0) {
			// create string array for selected values
			int[] entries = new int[rows.length];
			try {
				// iterate array
				for (int cnt = 0; cnt < rows.length; cnt++) {
					// copy value from table to array
					entries[cnt] = Integer.parseInt(data.getjTableResults().getValueAt(rows[cnt], 0).toString());
				}
				// return complete array
				return entries;
			} catch (NumberFormatException e) {
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
		int[] rows = data.getjTableResults().getSelectedRows();
		// if we have any selections, go on
		if ((rows != null) && (rows.length > 0)) {
			// get the selected searchrequest
			int i = data.getjComboBoxSearches().getSelectedIndex();
			for (int cnt = rows.length - 1; cnt >= 0; cnt--) {
				// retrieve the values...
				Object o = data.getjTableResults().getValueAt(rows[cnt], 0);
				// ...and try to convert it to an integer value
				int selection = Integer.parseInt(o.toString());
				// delete the entry from the search request
				data.getSearchrequest().deleteResultEntry(i, selection);
			}
			updateComboBox(rows[0], -1);
		}
	}

	/**
	 * This method deletes the selected entries completely from the dataset
	 */
	@Action
	public void deleteEntryComplete() {
		// first display the to be deleted entry in the main-frame, so the user is not
		// confused
		// about which entry to delete...
		displayEntryInMainframe();
		// try to delete the entry
		// and bring search results frame to front...
		if (data.getMainframe().deleteEntries(getSelectedEntriesFromTable()))
			this.toFront();
	}

	/**
	 * This method removes all(!) search requests, i.e. clears the
	 * search-request-xml-data.
	 */
	@Action
	public void removeAllSearchResults() {
		// and create a JOptionPane with yes/no/cancel options
		int msgOption = JOptionPane.showConfirmDialog(null, data.getResourceMap().getString("askForDeleteAllMsg"),
				data.getResourceMap().getString("askForDeleteAllTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		// if the user wants to proceed, copy the image now
		if (JOptionPane.YES_OPTION == msgOption) {
			// completeley remove all search requests
			data.getSearchrequest().deleteAllSearchRequests();
			// reset combobox
			updateComboBox(-1, -1);
		}
	}

	private void displayEntryInMainframe() {
		// get selected entry
		int row = data.getjTableResults().getSelectedRow();
		// when we have a valid selection, go on
		if (row != -1)
			data.getMainframe().setNewActivatedEntryAndUpdateDisplay(Integer.parseInt(data.getjTableResults().getValueAt(row, 0).toString()));
	}

	/**
	 * This method removes a complete search request from the search results.
	 */
	@Action
	public void removeSearchResult() {
		// and create a JOptionPane with yes/no/cancel options
		int msgOption = JOptionPane.showConfirmDialog(null, data.getResourceMap().getString("askForDeleteSearchMsg"),
				data.getResourceMap().getString("askForDeleteSearchTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		// if the user wants to proceed, copy the image now
		if (JOptionPane.YES_OPTION == msgOption) {
			// get the selected searchrequest
			int i = data.getjComboBoxSearches().getSelectedIndex();
			// delete complete search request
			data.getSearchrequest().deleteSearchRequest(i);
			// update combo box
			updateComboBox(0, -1);
		}
	}

	/**
	 * Closes the window.
	 */
	@Action
	public void closeWindow() {
		// check whether memory usage is logged. if so, tell logger that new entry
		// windows was opened
		if (data.getSettingsObj().isMemoryUsageLogged) {
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logged. Search Results Window closed.");
		}
		dispose();
		setVisible(false);
	}

	/**
	 * Activates or deactivates the fullscreen-mode, thus switching between
	 * fullscreen and normal view.
	 */
	@Action(enabledProperty = "fullScreenSupp")
	public void viewFullScreen() {
		// check whether fullscreen is possible or not...
		if (data.getGraphicdevice().isFullScreenSupported()) {
			// if we already have a fullscreen window, quit fullscreen
			if (data.getGraphicdevice().getFullScreenWindow() != null)
				quitFullScreen();
			// else show fullscreen window
			else
				showFullScreen();
		}
	}

	/**
	 * This method activates the fullscreen-mode, if it's not already activated yet.
	 * To have a fullscreen-window without decoration, the frame is disposed first,
	 * then the decoration will be removed and the window made visible again.
	 */
	private void showFullScreen() {
		// check whether fullscreen is supported, and if we currently have a
		// fullscreen-window
		if (data.getGraphicdevice().isFullScreenSupported() && null == data.getGraphicdevice().getFullScreenWindow()) {
			// dispose frame, so we can remove the decoration when setting full screen mode
			data.getSearchframe().dispose();
			// hide menubar
			data.getSearchMenuBar().setVisible(false);
			// set frame non-resizable
			data.getSearchframe().setResizable(false);
			try {
				// remove decoration
				data.getSearchframe().setUndecorated(true);
			} catch (IllegalComponentStateException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			// show frame again
			data.getSearchframe().setVisible(true);
			// set fullscreen mode to this window
			data.getGraphicdevice().setFullScreenWindow(this);
		}
	}

	/**
	 * This method <i>de</i>activates the fullscreen-mode, if it's not already
	 * deactivated yet.
	 */
	private void quitFullScreen() {
		// check whether full screen is supported, and if we currently have a
		// fullscreen-window
		if (data.getGraphicdevice().isFullScreenSupported() && data.getGraphicdevice().getFullScreenWindow() != null) {
			// disable fullscreen-mode
			data.getGraphicdevice().setFullScreenWindow(null);
			// hide menubar
			data.getSearchMenuBar().setVisible(true);
			// make frame resizable again
			data.getSearchframe().setResizable(true);
			// dispose frame, so we can restore the decoration
			data.getSearchframe().dispose();
			try {
				// set decoration
				data.getSearchframe().setUndecorated(false);
			} catch (IllegalComponentStateException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			// show frame again
			data.getSearchframe().setVisible(true);
		}
	}

	/**
	 * This method is used to pass paramaters to this dialog, so it can display
	 * results when it is made visible. Since we don't dispose and clear this
	 * dialog, we cannot call the constructor each time, so we need another method
	 * where we can pass parameters of new search results. <br>
	 * <br>
	 * This dialog is not disposed and cleared, because we want to keep former
	 * search results, even when the user "closes" (i.e.: hides) this dialog.
	 */
	public void showLatestSearchResult() {
		// here we update the combo box, not the display. since selecting
		// an item, which is done in this method, fires an action to the action
		// listener,
		// the display update should be achieved through the combobox's actionlistener.
		updateComboBox(-1, data.getSearchrequest().getCount() - 1);
		// and make dialog visible
		setVisible(true);
		// repaint the components (necessary, since the components are not properly
		// repainted else)
		repaint();
		// set input focus
		this.setAlwaysOnTop(true);
		this.toFront();
		this.requestFocusInWindow();
		this.setAlwaysOnTop(false);
		setAlwaysOnTop(true);
		setAlwaysOnTop(false);
		toFront();
	}

	private void openAttachment(javax.swing.event.HyperlinkEvent evt) {
		// retrieve the event type, e.g. if a link was clicked by the user
		HyperlinkEvent.EventType typ = evt.getEventType();
		// get the description, to check whether we have a file or a hyperlink to a
		// website
		String linktype = evt.getDescription();
		// if the link was clicked, proceed
		if (typ == HyperlinkEvent.EventType.ACTIVATED) {
			// call method that handles the hyperlink-click
			String returnValue = Tools.openHyperlink(linktype, this, Constants.FRAME_SEARCH, data.getDataObj(), data.getBibtexObj(),
					data.getSettingsObj(), data.getjEditorPaneSearchEntry(),
					Integer.parseInt(data.getjTableResults().getValueAt(data.getjTableResults().getSelectedRow(), 0).toString()));
			// check whether we have a return value. this might be the case either when the
			// user clicked on
			// a footnote, or on the rating-stars
			if (returnValue != null) {
				// here we have a reference to another entry
				if (returnValue.startsWith("#z_") || returnValue.startsWith("#cr_")) {
					// show entry
					data.getMainframe().setNewActivatedEntryAndUpdateDisplay(data.getDataObj().getActivatedEntryNumber());
				}
				// edit cross references
				else if (returnValue.equalsIgnoreCase("#crt")) {
					data.getMainframe().editManualLinks();
				}
				// check whether a rating was requested
				else if (returnValue.startsWith("#rateentry")) {
					try {
						// retrieve entry-number
						int entrynr = Integer.parseInt(linktype.substring(10));
						// open rating-dialog
						if (null == data.getRateEntryDlg()) {
							data.setRateEntryDlg(new CRateEntry(this, data.getDataObj(), entrynr));
							data.getRateEntryDlg().setLocationRelativeTo(this);
						}
						ZettelkastenApp.getApplication().show(data.getRateEntryDlg());
						// check whether dialog was cancelled or not
						if (!data.getRateEntryDlg().isCancelled()) {
							// update display
							displayZettelContent(entrynr, null);
						}
						data.getRateEntryDlg().dispose();
						data.setRateEntryDlg(null);
					} catch (NumberFormatException ex) {
						// log error
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						Constants.zknlogger.log(Level.WARNING, "Could not rate entry. Link-text was {0}", linktype);
					}

				}
			}
		}
	}

	/**
	 * This class sets up a selection listener for the tables. each table which
	 * shall react on selections, e.g. by showing an entry, gets this
	 * selectionlistener in the method {@link #initSelectionListeners()
	 * initSelectionListeners()}.
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
			if (data.isTableUpdateActive())
				return;
			// get list selection model
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			// set value-adjusting to true, so we don't fire multiple value-changed
			// events...
			lsm.setValueIsAdjusting(true);
			if (data.getjTableResults() == table)
				updateDisplay();
		}
	}

	public boolean isTextSelected() {
		return data.isTextSelected();
	}

	public void setTextSelected(boolean b) {
		boolean old = isTextSelected();
		this.data.setTextSelected(b);
		firePropertyChange("textSelected", old, isTextSelected());
	}

	public boolean isListSelected() {
		return data.isListSelected();
	}

	public void setListSelected(boolean b) {
		boolean old = isListSelected();
		this.data.setListSelected(b);
		firePropertyChange("listSelected", old, isListSelected());
	}

	public boolean isDesktopEntrySelected() {
		return data.isDesktopEntrySelected();
	}

	public void setDesktopEntrySelected(boolean b) {
		boolean old = isDesktopEntrySelected();
		this.data.setDesktopEntrySelected(b);
		firePropertyChange("desktopEntrySelected", old, isDesktopEntrySelected());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        data.setSearchToolbar(new javax.swing.JToolBar());
        data.setTb_copy(new javax.swing.JButton());
        data.setTb_selectall(new javax.swing.JButton());
        data.setjSeparator12(new javax.swing.JToolBar.Separator());
        data.setTb_editentry(new javax.swing.JButton());
        data.setTb_remove(new javax.swing.JButton());
        data.setjSeparator3(new javax.swing.JToolBar.Separator());
        data.setTb_manlinks(new javax.swing.JButton());
        data.setTb_luhmann(new javax.swing.JButton());
        data.setTb_bookmark(new javax.swing.JButton());
        data.setTb_desktop(new javax.swing.JButton());
        data.setjSeparator5(new javax.swing.JToolBar.Separator());
        data.setTb_highlight(new javax.swing.JButton());
        data.setSearchMainPanel(new javax.swing.JPanel());
        data.setjSplitPaneSearch1(new javax.swing.JSplitPane());
        data.setjPanel1(new javax.swing.JPanel());
        data.setjScrollPane1(new javax.swing.JScrollPane());
        data.setjTableResults((data.getSettingsObj().isMacStyle()) ? com.explodingpixels.macwidgets.MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable());
        data.setjTextFieldFilterList(new javax.swing.JTextField());
        data.setjButtonResetList(new javax.swing.JButton());
        data.setjPanel2(new javax.swing.JPanel());
        data.setjSplitPaneSearch2(new javax.swing.JSplitPane());
        data.setjPanel3(new javax.swing.JPanel());
        data.setjScrollPane2(new javax.swing.JScrollPane());
        data.setjEditorPaneSearchEntry(new javax.swing.JEditorPane());
        data.setjPanel4(new javax.swing.JPanel());
        data.setjScrollPane4(new javax.swing.JScrollPane());
        data.setjListKeywords(MacSourceList.createMacSourceList());
        data.setSearchStatusPanel(new javax.swing.JPanel());
        data.setjPanel9(new javax.swing.JPanel());
        data.setjLabel1(new javax.swing.JLabel());
        data.setjComboBoxSearches(new javax.swing.JComboBox());
        data.setjLabelHits(new javax.swing.JLabel());
        data.setjButtonDeleteSearch(new javax.swing.JButton());
        data.setSearchMenuBar(new javax.swing.JMenuBar());
        data.setSearchFileMenu(new javax.swing.JMenu());
        data.setFileMenuLongDesc(new javax.swing.JMenuItem());
        data.setjSeparator2(new javax.swing.JSeparator());
        data.setFileMenuDuplicateSearch(new javax.swing.JMenuItem());
        data.setjSeparator22(new javax.swing.JSeparator());
        data.setFileMenuDeleteSearch(new javax.swing.JMenuItem());
        data.setFileMenuDeleteAll(new javax.swing.JMenuItem());
        data.setjSeparator20(new javax.swing.JSeparator());
        data.setFileMenuExport(new javax.swing.JMenuItem());
        data.setjSeparator13(new javax.swing.JSeparator());
        data.setFileMenuClose(new javax.swing.JMenuItem());
        data.setSearchEditMenu(new javax.swing.JMenu());
        data.setEditMenuCopy(new javax.swing.JMenuItem());
        data.setEditMenuSelectAll(new javax.swing.JMenuItem());
        data.setjSeparator10(new javax.swing.JSeparator());
        data.setEditMenuDelete(new javax.swing.JMenuItem());
        data.setjSeparator16(new javax.swing.JSeparator());
        data.setEditMenuEditEntry(new javax.swing.JMenuItem());
        data.setEditMenuDuplicateEntry(new javax.swing.JMenuItem());
        data.setEditMenuFindReplace(new javax.swing.JMenuItem());
        data.setjSeparator4(new javax.swing.JSeparator());
        data.setEditMenuDeleteEntry(new javax.swing.JMenuItem());
        data.setjSeparator21(new javax.swing.JSeparator());
        data.setEditMenuAddKeywordsToSelection(new javax.swing.JMenuItem());
        data.setEditMenuAddAuthorsToSelection(new javax.swing.JMenuItem());
        data.setjSeparator1(new javax.swing.JSeparator());
        data.setEditMenuManLinks(new javax.swing.JMenuItem());
        data.setEditMenuLuhmann(new javax.swing.JMenuItem());
        data.setEditMenuBookmarks(new javax.swing.JMenuItem());
        data.setjSeparator6(new javax.swing.JSeparator());
        data.setEditMenuDesktop(new javax.swing.JMenuItem());
        data.setSearchFilterMenu(new javax.swing.JMenu());
        data.setFilterSearch(new javax.swing.JMenuItem());
        data.setjSeparator14(new javax.swing.JSeparator());
        data.setFilterKeywords(new javax.swing.JMenuItem());
        data.setjSeparator15(new javax.swing.JSeparator());
        data.setFilterAuthors(new javax.swing.JMenuItem());
        data.setjSeparator23(new javax.swing.JPopupMenu.Separator());
        data.setFilterTopLevelLuhmann(new javax.swing.JMenuItem());
        data.setSearchSearchMenu(new javax.swing.JMenu());
        data.setSearchMenuSelectionContent(new javax.swing.JMenuItem());
        data.setjSeparator19(new javax.swing.JSeparator());
        data.setSearchMenuKeywordLogOr(new javax.swing.JMenuItem());
        data.setSearchMenuKeywordLogAnd(new javax.swing.JMenuItem());
        data.setSearchMenuKeywordLogNot(new javax.swing.JMenuItem());
        data.setSearchViewMenu(new javax.swing.JMenu());
        data.setViewMenuShowOnDesktop(new javax.swing.JMenuItem());
        data.setjSeparator11(new javax.swing.JSeparator());
        data.setViewMenuHighlight(new javax.swing.JCheckBoxMenuItem());
        data.setViewMenuHighlightSettings(new javax.swing.JMenuItem());
        data.setjSeparator9(new javax.swing.JSeparator());
        data.setViewMenuShowEntry(new javax.swing.JCheckBoxMenuItem());
        data.setjSeparator7(new javax.swing.JPopupMenu.Separator());
        data.setjMenuItemSwitchLayout(new javax.swing.JMenuItem());
        data.setjSeparator8(new javax.swing.JPopupMenu.Separator());
        data.setViewMenuFullScreen(new javax.swing.JMenuItem());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(SearchResultsFrame.class);
        setTitle(resourceMap.getString("FormSearchResults.title")); // NOI18N
        setName("FormSearchResults"); // NOI18N

        data.getSearchToolbar().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("searchToolbar.border.matteColor"))); // NOI18N
        data.getSearchToolbar().setRollover(true);
        data.getSearchToolbar().setName("searchToolbar"); // NOI18N

        data.getTb_copy().setAction(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(SearchResultsFrame.class, this).get("copy"));
        data.getTb_copy().setText(resourceMap.getString("tb_copy.text")); // NOI18N
        data.getTb_copy().setBorderPainted(false);
        data.getTb_copy().setFocusPainted(false);
        data.getTb_copy().setFocusable(false);
        data.getTb_copy().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_copy().setName("tb_copy"); // NOI18N
        data.getTb_copy().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_copy());

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(SearchResultsFrame.class, this);
        data.getTb_selectall().setAction(actionMap.get("selectAll")); // NOI18N
        data.getTb_selectall().setText(resourceMap.getString("tb_selectall.text")); // NOI18N
        data.getTb_selectall().setBorderPainted(false);
        data.getTb_selectall().setFocusPainted(false);
        data.getTb_selectall().setFocusable(false);
        data.getTb_selectall().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_selectall().setName("tb_selectall"); // NOI18N
        data.getTb_selectall().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_selectall());

        data.getjSeparator12().setName("jSeparator12"); // NOI18N
        data.getSearchToolbar().add(data.getjSeparator12());

        data.getTb_editentry().setAction(actionMap.get("editEntry")); // NOI18N
        data.getTb_editentry().setText(resourceMap.getString("tb_editentry.text")); // NOI18N
        data.getTb_editentry().setBorderPainted(false);
        data.getTb_editentry().setFocusPainted(false);
        data.getTb_editentry().setFocusable(false);
        data.getTb_editentry().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_editentry().setName("tb_editentry"); // NOI18N
        data.getTb_editentry().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_editentry());

        data.getTb_remove().setAction(actionMap.get("removeEntry")); // NOI18N
        data.getTb_remove().setText(resourceMap.getString("tb_remove.text")); // NOI18N
        data.getTb_remove().setBorderPainted(false);
        data.getTb_remove().setFocusPainted(false);
        data.getTb_remove().setFocusable(false);
        data.getTb_remove().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_remove().setName("tb_remove"); // NOI18N
        data.getTb_remove().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_remove());

        data.getjSeparator3().setName("jSeparator3"); // NOI18N
        data.getSearchToolbar().add(data.getjSeparator3());

        data.getTb_manlinks().setAction(actionMap.get("addToManLinks")); // NOI18N
        data.getTb_manlinks().setText(resourceMap.getString("tb_manlinks.text")); // NOI18N
        data.getTb_manlinks().setBorderPainted(false);
        data.getTb_manlinks().setFocusPainted(false);
        data.getTb_manlinks().setFocusable(false);
        data.getTb_manlinks().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_manlinks().setName("tb_manlinks"); // NOI18N
        data.getTb_manlinks().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_manlinks());

        data.getTb_luhmann().setAction(actionMap.get("addToLuhmann")); // NOI18N
        data.getTb_luhmann().setText(resourceMap.getString("tb_luhmann.text")); // NOI18N
        data.getTb_luhmann().setBorderPainted(false);
        data.getTb_luhmann().setFocusPainted(false);
        data.getTb_luhmann().setFocusable(false);
        data.getTb_luhmann().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_luhmann().setName("tb_luhmann"); // NOI18N
        data.getTb_luhmann().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_luhmann());

        data.getTb_bookmark().setAction(actionMap.get("addToBookmarks")); // NOI18N
        data.getTb_bookmark().setText(resourceMap.getString("tb_bookmark.text")); // NOI18N
        data.getTb_bookmark().setBorderPainted(false);
        data.getTb_bookmark().setFocusPainted(false);
        data.getTb_bookmark().setFocusable(false);
        data.getTb_bookmark().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_bookmark().setName("tb_bookmark"); // NOI18N
        data.getTb_bookmark().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_bookmark());

        data.getTb_desktop().setAction(actionMap.get("addToDesktop")); // NOI18N
        data.getTb_desktop().setText(resourceMap.getString("tb_desktop.text")); // NOI18N
        data.getTb_desktop().setBorderPainted(false);
        data.getTb_desktop().setFocusPainted(false);
        data.getTb_desktop().setFocusable(false);
        data.getTb_desktop().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_desktop().setName("tb_desktop"); // NOI18N
        data.getTb_desktop().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_desktop());

        data.getjSeparator5().setName("jSeparator5"); // NOI18N
        data.getSearchToolbar().add(data.getjSeparator5());

        data.getTb_highlight().setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        data.getTb_highlight().setText(resourceMap.getString("tb_highlight.text")); // NOI18N
        data.getTb_highlight().setBorderPainted(false);
        data.getTb_highlight().setFocusPainted(false);
        data.getTb_highlight().setFocusable(false);
        data.getTb_highlight().setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        data.getTb_highlight().setName("tb_highlight"); // NOI18N
        data.getTb_highlight().setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        data.getSearchToolbar().add(data.getTb_highlight());

        data.getSearchMainPanel().setName("searchMainPanel"); // NOI18N
        data.getSearchMainPanel().setLayout(new java.awt.BorderLayout());

        data.getjSplitPaneSearch1().setDividerLocation(240);
        data.getjSplitPaneSearch1().setOrientation(data.getSettingsObj().getSearchFrameSplitLayout());
        data.getjSplitPaneSearch1().setName("jSplitPaneSearch1"); // NOI18N
        data.getjSplitPaneSearch1().setOneTouchExpandable(true);

        data.getjPanel1().setName("jPanel1"); // NOI18N

        data.getjScrollPane1().setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        data.getjScrollPane1().setName("jScrollPane1"); // NOI18N

        data.getjTableResults().setModel(new javax.swing.table.DefaultTableModel(
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
        data.getjTableResults().setDragEnabled(true);
        data.getjTableResults().setName("jTableResults"); // NOI18N
        data.getjTableResults().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        data.getjTableResults().getTableHeader().setReorderingAllowed(false);
        data.getjScrollPane1().setViewportView(data.getjTableResults());
        if (data.getjTableResults().getColumnModel().getColumnCount() > 0) {
            data.getjTableResults().getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title0")); // NOI18N
            data.getjTableResults().getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title1")); // NOI18N
            data.getjTableResults().getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title2")); // NOI18N
            data.getjTableResults().getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title3")); // NOI18N
            data.getjTableResults().getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title4")); // NOI18N
            data.getjTableResults().getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title5")); // NOI18N
        }

        data.getjTextFieldFilterList().setName("jTextFieldFilterList"); // NOI18N

        data.getjButtonResetList().setAction(actionMap.get("resetResultslist")); // NOI18N
        data.getjButtonResetList().setIcon(resourceMap.getIcon("jButtonResetList.icon")); // NOI18N
        data.getjButtonResetList().setBorderPainted(false);
        data.getjButtonResetList().setContentAreaFilled(false);
        data.getjButtonResetList().setFocusable(false);
        data.getjButtonResetList().setName("jButtonResetList"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(data.getjPanel1());
        data.getjPanel1().setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjScrollPane1(), javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(data.getjTextFieldFilterList())
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(data.getjButtonResetList(), javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(data.getjScrollPane1(), javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(data.getjTextFieldFilterList(), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(data.getjButtonResetList()))
                .addGap(3, 3, 3))
        );

        data.getjSplitPaneSearch1().setLeftComponent(data.getjPanel1());

        data.getjPanel2().setName("jPanel2"); // NOI18N

        data.getjSplitPaneSearch2().setDividerLocation(280);
        data.getjSplitPaneSearch2().setName("jSplitPaneSearch2"); // NOI18N
        data.getjSplitPaneSearch2().setOneTouchExpandable(true);

        data.getjPanel3().setName("jPanel3"); // NOI18N

        data.getjScrollPane2().setBorder(null);
        data.getjScrollPane2().setName("jScrollPane2"); // NOI18N

        data.getjEditorPaneSearchEntry().setEditable(false);
        data.getjEditorPaneSearchEntry().setBorder(null);
        data.getjEditorPaneSearchEntry().setContentType(resourceMap.getString("jEditorPaneSearchEntry.contentType")); // NOI18N
        data.getjEditorPaneSearchEntry().setName("jEditorPaneSearchEntry"); // NOI18N
        data.getjScrollPane2().setViewportView(data.getjEditorPaneSearchEntry());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(data.getjPanel3());
        data.getjPanel3().setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjScrollPane2())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjScrollPane2(), javax.swing.GroupLayout.Alignment.TRAILING)
        );

        data.getjSplitPaneSearch2().setLeftComponent(data.getjPanel3());

        data.getjPanel4().setName("jPanel4"); // NOI18N

        data.getjScrollPane4().setName("jScrollPane4"); // NOI18N

        data.getjListKeywords().setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListKeywords.border.title"))); // NOI18N
        data.getjListKeywords().setModel(data.getKeywordListModel());
        data.getjListKeywords().setName("jListKeywords"); // NOI18N
        data.getjScrollPane4().setViewportView(data.getjListKeywords());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(data.getjPanel4());
        data.getjPanel4().setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjScrollPane4(), javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjScrollPane4(), javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
        );

        data.getjSplitPaneSearch2().setRightComponent(data.getjPanel4());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(data.getjPanel2());
        data.getjPanel2().setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjSplitPaneSearch2())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjSplitPaneSearch2())
        );

        data.getjSplitPaneSearch1().setRightComponent(data.getjPanel2());

        data.getSearchMainPanel().add(data.getjSplitPaneSearch1(), java.awt.BorderLayout.CENTER);

        data.getSearchStatusPanel().setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("searchStatusPanel.border.matteColor"))); // NOI18N
        data.getSearchStatusPanel().setMinimumSize(new java.awt.Dimension(200, 16));
        data.getSearchStatusPanel().setName("searchStatusPanel"); // NOI18N

        data.getjPanel9().setName("jPanel9"); // NOI18N

        data.getjLabel1().setText(resourceMap.getString("jLabel1.text")); // NOI18N
        data.getjLabel1().setName("jLabel1"); // NOI18N

        data.getjComboBoxSearches().setName("jComboBoxSearches"); // NOI18N

        data.getjLabelHits().setText(resourceMap.getString("jLabelHits.text")); // NOI18N
        data.getjLabelHits().setName("jLabelHits"); // NOI18N

        data.getjButtonDeleteSearch().setAction(actionMap.get("removeSearchResult")); // NOI18N
        data.getjButtonDeleteSearch().setIcon(resourceMap.getIcon("jButtonDeleteSearch.icon")); // NOI18N
        data.getjButtonDeleteSearch().setText(resourceMap.getString("jButtonDeleteSearch.text")); // NOI18N
        data.getjButtonDeleteSearch().setBorderPainted(false);
        data.getjButtonDeleteSearch().setFocusPainted(false);
        data.getjButtonDeleteSearch().setFocusable(false);
        data.getjButtonDeleteSearch().setName("jButtonDeleteSearch"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(data.getjPanel9());
        data.getjPanel9().setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(data.getjLabelHits())
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(data.getjLabel1())
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(data.getjComboBoxSearches(), 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(data.getjButtonDeleteSearch(), javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(data.getjButtonDeleteSearch())
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(data.getjLabelHits())
                        .addComponent(data.getjLabel1())
                        .addComponent(data.getjComboBoxSearches(), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout searchStatusPanelLayout = new javax.swing.GroupLayout(data.getSearchStatusPanel());
        data.getSearchStatusPanel().setLayout(searchStatusPanelLayout);
        searchStatusPanelLayout.setHorizontalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjPanel9(), javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchStatusPanelLayout.setVerticalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getjPanel9(), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        data.getSearchMenuBar().setName("searchMenuBar"); // NOI18N

        data.getSearchFileMenu().setText(resourceMap.getString("searchFileMenu.text")); // NOI18N
        data.getSearchFileMenu().setName("searchFileMenu"); // NOI18N

        data.getFileMenuLongDesc().setAction(actionMap.get("showLongDesc")); // NOI18N
        data.getFileMenuLongDesc().setName("fileMenuLongDesc"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuLongDesc());

        data.getjSeparator2().setName("jSeparator2"); // NOI18N
        data.getSearchFileMenu().add(data.getjSeparator2());

        data.getFileMenuDuplicateSearch().setAction(actionMap.get("duplicateSearch")); // NOI18N
        data.getFileMenuDuplicateSearch().setName("fileMenuDuplicateSearch"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuDuplicateSearch());

        data.getjSeparator22().setName("jSeparator22"); // NOI18N
        data.getSearchFileMenu().add(data.getjSeparator22());

        data.getFileMenuDeleteSearch().setAction(actionMap.get("removeSearchResult")); // NOI18N
        data.getFileMenuDeleteSearch().setName("fileMenuDeleteSearch"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuDeleteSearch());

        data.getFileMenuDeleteAll().setAction(actionMap.get("removeAllSearchResults")); // NOI18N
        data.getFileMenuDeleteAll().setName("fileMenuDeleteAll"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuDeleteAll());

        data.getjSeparator20().setName("jSeparator20"); // NOI18N
        data.getSearchFileMenu().add(data.getjSeparator20());

        data.getFileMenuExport().setAction(actionMap.get("exportEntries")); // NOI18N
        data.getFileMenuExport().setName("fileMenuExport"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuExport());

        data.getjSeparator13().setName("jSeparator13"); // NOI18N
        data.getSearchFileMenu().add(data.getjSeparator13());

        data.getFileMenuClose().setAction(actionMap.get("closeWindow")); // NOI18N
        data.getFileMenuClose().setName("fileMenuClose"); // NOI18N
        data.getSearchFileMenu().add(data.getFileMenuClose());

        data.getSearchMenuBar().add(data.getSearchFileMenu());

        data.getSearchEditMenu().setText(resourceMap.getString("searchEditMenu.text")); // NOI18N
        data.getSearchEditMenu().setName("searchEditMenu"); // NOI18N

        data.getEditMenuCopy().setAction(actionMap.get("copy"));
        data.getEditMenuCopy().setName("editMenuCopy"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuCopy());

        data.getEditMenuSelectAll().setAction(actionMap.get("selectAll")); // NOI18N
        data.getEditMenuSelectAll().setName("editMenuSelectAll"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuSelectAll());

        data.getjSeparator10().setName("jSeparator10"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator10());

        data.getEditMenuDelete().setAction(actionMap.get("removeEntry")); // NOI18N
        data.getEditMenuDelete().setName("editMenuDelete"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuDelete());

        data.getjSeparator16().setName("jSeparator16"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator16());

        data.getEditMenuEditEntry().setAction(actionMap.get("editEntry")); // NOI18N
        data.getEditMenuEditEntry().setName("editMenuEditEntry"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuEditEntry());

        data.getEditMenuDuplicateEntry().setAction(actionMap.get("duplicateEntry")); // NOI18N
        data.getEditMenuDuplicateEntry().setName("editMenuDuplicateEntry"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuDuplicateEntry());

        data.getEditMenuFindReplace().setAction(actionMap.get("findAndReplace")); // NOI18N
        data.getEditMenuFindReplace().setName("editMenuFindReplace"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuFindReplace());

        data.getjSeparator4().setName("jSeparator4"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator4());

        data.getEditMenuDeleteEntry().setAction(actionMap.get("deleteEntryComplete")); // NOI18N
        data.getEditMenuDeleteEntry().setName("editMenuDeleteEntry"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuDeleteEntry());

        data.getjSeparator21().setName("jSeparator21"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator21());

        data.getEditMenuAddKeywordsToSelection().setAction(actionMap.get("addKeywordsToEntries")); // NOI18N
        data.getEditMenuAddKeywordsToSelection().setName("editMenuAddKeywordsToSelection"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuAddKeywordsToSelection());

        data.getEditMenuAddAuthorsToSelection().setAction(actionMap.get("addAuthorsToEntries")); // NOI18N
        data.getEditMenuAddAuthorsToSelection().setName("editMenuAddAuthorsToSelection"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuAddAuthorsToSelection());

        data.getjSeparator1().setName("jSeparator1"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator1());

        data.getEditMenuManLinks().setAction(actionMap.get("addToManLinks")); // NOI18N
        data.getEditMenuManLinks().setName("editMenuManLinks"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuManLinks());

        data.getEditMenuLuhmann().setAction(actionMap.get("addToLuhmann")); // NOI18N
        data.getEditMenuLuhmann().setName("editMenuLuhmann"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuLuhmann());

        data.getEditMenuBookmarks().setAction(actionMap.get("addToBookmarks")); // NOI18N
        data.getEditMenuBookmarks().setName("editMenuBookmarks"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuBookmarks());

        data.getjSeparator6().setName("jSeparator6"); // NOI18N
        data.getSearchEditMenu().add(data.getjSeparator6());

        data.getEditMenuDesktop().setAction(actionMap.get("addToDesktop")); // NOI18N
        data.getEditMenuDesktop().setName("editMenuDesktop"); // NOI18N
        data.getSearchEditMenu().add(data.getEditMenuDesktop());

        data.getSearchMenuBar().add(data.getSearchEditMenu());

        data.getSearchFilterMenu().setText(resourceMap.getString("searchFilterMenu.text")); // NOI18N
        data.getSearchFilterMenu().setName("searchFilterMenu"); // NOI18N

        data.getFilterSearch().setAction(actionMap.get("filterSearch")); // NOI18N
        data.getFilterSearch().setName("filterSearch"); // NOI18N
        data.getSearchFilterMenu().add(data.getFilterSearch());

        data.getjSeparator14().setName("jSeparator14"); // NOI18N
        data.getSearchFilterMenu().add(data.getjSeparator14());

        data.getFilterKeywords().setAction(actionMap.get("filterKeywords")); // NOI18N
        data.getFilterKeywords().setName("filterKeywords"); // NOI18N
        data.getSearchFilterMenu().add(data.getFilterKeywords());

        data.getjSeparator15().setName("jSeparator15"); // NOI18N
        data.getSearchFilterMenu().add(data.getjSeparator15());

        data.getFilterAuthors().setAction(actionMap.get("filterAuthors")); // NOI18N
        data.getFilterAuthors().setName("filterAuthors"); // NOI18N
        data.getSearchFilterMenu().add(data.getFilterAuthors());

        data.getjSeparator23().setName("jSeparator23"); // NOI18N
        data.getSearchFilterMenu().add(data.getjSeparator23());

        data.getFilterTopLevelLuhmann().setAction(actionMap.get("filterTopLevelLuhmann")); // NOI18N
        data.getFilterTopLevelLuhmann().setName("filterTopLevelLuhmann"); // NOI18N
        data.getSearchFilterMenu().add(data.getFilterTopLevelLuhmann());

        data.getSearchMenuBar().add(data.getSearchFilterMenu());

        data.getSearchSearchMenu().setText(resourceMap.getString("searchSearchMenu.text")); // NOI18N
        data.getSearchSearchMenu().setName("searchSearchMenu"); // NOI18N

        data.getSearchMenuSelectionContent().setAction(actionMap.get("newSearchFromSelection")); // NOI18N
        data.getSearchMenuSelectionContent().setName("searchMenuSelectionContent"); // NOI18N
        data.getSearchSearchMenu().add(data.getSearchMenuSelectionContent());

        data.getjSeparator19().setName("jSeparator19"); // NOI18N
        data.getSearchSearchMenu().add(data.getjSeparator19());

        data.getSearchMenuKeywordLogOr().setAction(actionMap.get("newSearchFromKeywordsLogOr")); // NOI18N
        data.getSearchMenuKeywordLogOr().setName("searchMenuKeywordLogOr"); // NOI18N
        data.getSearchSearchMenu().add(data.getSearchMenuKeywordLogOr());

        data.getSearchMenuKeywordLogAnd().setAction(actionMap.get("newSearchFromKeywordsLogAnd")); // NOI18N
        data.getSearchMenuKeywordLogAnd().setName("searchMenuKeywordLogAnd"); // NOI18N
        data.getSearchSearchMenu().add(data.getSearchMenuKeywordLogAnd());

        data.getSearchMenuKeywordLogNot().setAction(actionMap.get("newSearchFromKeywordsLogNot")); // NOI18N
        data.getSearchMenuKeywordLogNot().setName("searchMenuKeywordLogNot"); // NOI18N
        data.getSearchSearchMenu().add(data.getSearchMenuKeywordLogNot());

        data.getSearchMenuBar().add(data.getSearchSearchMenu());

        data.getSearchViewMenu().setText(resourceMap.getString("searchViewMenu.text")); // NOI18N
        data.getSearchViewMenu().setName("searchViewMenu"); // NOI18N

        data.getViewMenuShowOnDesktop().setAction(actionMap.get("showEntryInDesktop")); // NOI18N
        data.getViewMenuShowOnDesktop().setName("viewMenuShowOnDesktop"); // NOI18N
        data.getSearchViewMenu().add(data.getViewMenuShowOnDesktop());

        data.getjSeparator11().setName("jSeparator11"); // NOI18N
        data.getSearchViewMenu().add(data.getjSeparator11());

        data.getViewMenuHighlight().setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        data.getViewMenuHighlight().setSelected(true);
        data.getViewMenuHighlight().setName("viewMenuHighlight"); // NOI18N
        data.getSearchViewMenu().add(data.getViewMenuHighlight());

        data.getViewMenuHighlightSettings().setAction(actionMap.get("showHighlightSettings")); // NOI18N
        data.getViewMenuHighlightSettings().setName("viewMenuHighlightSettings"); // NOI18N
        data.getSearchViewMenu().add(data.getViewMenuHighlightSettings());

        data.getjSeparator9().setName("jSeparator9"); // NOI18N
        data.getSearchViewMenu().add(data.getjSeparator9());

        data.getViewMenuShowEntry().setAction(actionMap.get("showEntryImmediately")); // NOI18N
        data.getViewMenuShowEntry().setSelected(true);
        data.getViewMenuShowEntry().setName("viewMenuShowEntry"); // NOI18N
        data.getSearchViewMenu().add(data.getViewMenuShowEntry());

        data.getjSeparator7().setName("jSeparator7"); // NOI18N
        data.getSearchViewMenu().add(data.getjSeparator7());

        data.getjMenuItemSwitchLayout().setAction(actionMap.get("switchLayout")); // NOI18N
        data.getjMenuItemSwitchLayout().setName("jMenuItemSwitchLayout"); // NOI18N
        data.getSearchViewMenu().add(data.getjMenuItemSwitchLayout());

        data.getjSeparator8().setName("jSeparator8"); // NOI18N
        data.getSearchViewMenu().add(data.getjSeparator8());

        data.getViewMenuFullScreen().setAction(actionMap.get("viewFullScreen")); // NOI18N
        data.getViewMenuFullScreen().setName("viewMenuFullScreen"); // NOI18N
        data.getSearchViewMenu().add(data.getViewMenuFullScreen());

        data.getSearchMenuBar().add(data.getSearchViewMenu());

        setJMenuBar(data.getSearchMenuBar());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(data.getSearchToolbar(), javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(data.getSearchMainPanel(), javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(data.getSearchStatusPanel(), javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(data.getSearchToolbar(), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(data.getSearchMainPanel(), javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(data.getSearchStatusPanel(), javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public boolean isFullScreenSupp() {
		return data.isFullScreenSupp();
	}

	public void setFullScreenSupp(boolean b) {
		boolean old = isFullScreenSupp();
		this.data.setFullScreenSupp(b);
		firePropertyChange("fullScreenSupp", old, isFullScreenSupp());
	}

    private SearchResultsFrameData data = new SearchResultsFrameData(new DefaultListModel<String>(), false, java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice(), org.jdesktop.application.Application
			.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
			.getResourceMap(SearchResultsFrame.class),
			org.jdesktop.application.Application
					.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
					.getResourceMap(ToolbarIcons.class), false, false, false, false);
}
