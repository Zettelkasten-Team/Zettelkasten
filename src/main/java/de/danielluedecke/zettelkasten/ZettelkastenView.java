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

import com.explodingpixels.macwidgets.*;
import com.explodingpixels.widgets.TableUtils;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.mac.MacSourceTree;
import de.danielluedecke.zettelkasten.mac.MacToolbarButton;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.tasks.AutoBackupTask;
import de.danielluedecke.zettelkasten.tasks.FindDoubleEntriesTask;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import de.danielluedecke.zettelkasten.tasks.export.ExportTools;
import de.danielluedecke.zettelkasten.util.*;
import de.danielluedecke.zettelkasten.util.classes.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.MatteBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The application's main frame.
 */
public class ZettelkastenView extends FrameView implements WindowListener, DropTargetListener {

	// <editor-fold defaultstate="collapsed" desc="Variablendeklaration">
	/**
	 * searchRequests manages all searches and searchresults for the loaded
	 * datafile.
	 */
	public SearchRequests searchRequests;

	private final Daten data;
	private final TasksData taskinfo;
	public final Bookmarks bookmarks;
	private final BibTeX bibtex;
	public DesktopData desktop;
	private final Settings settings;

	/**
	 * This list model is used for the JList-component which displays the keywords
	 * of the current entry.
	 */
	private final DefaultListModel<String> keywordListModel = new DefaultListModel<String>();

	/**
	 * This list model is used for the JList-component which displays the found
	 * entries, which have relations between certain keywords. see "showCluster()"
	 * for more details.
	 */
	private final List<String> clusterList = new ArrayList<>();

	/**
	 * This list model is used for the JList-component which displays the
	 * is-follower-numbers.
	 */
	private final List<String> isFollowerList = new ArrayList<>();

	/**
	 * This variable stores the table data of the keyword-list when this list is
	 * filtered. All changes to a fitered table-list are also applied to this linked
	 * list. When the table-list is being refreshed, we don't need to run the
	 * time-consuming task; instead we simply iterate this list and set the values
	 * to the table
	 */
	private LinkedList<Object[]> linkedkeywordlist;

	/**
	 * This variable stores the table data of the author-list when this list is
	 * filtered. All changes to a fitered table-list are also applied to this linked
	 * list. When the table-list is being refreshed, we don't need to run the
	 * time-consuming task; instead we simply iterate this list and set the values
	 * to the table
	 */
	private LinkedList<Object[]> linkedauthorlist;

	/**
	 * This variable stores the table data of the title-list when this list is
	 * filtered. All changes to a fitered table-list are also applied to this linked
	 * list. When the table-list is being refreshed, we don't need to run the
	 * time-consuming task; instead we simply iterate this list and set the values
	 * to the table
	 */
	private LinkedList<Object[]> linkedtitlelist;

	/**
	 * This variable stores the table data of the attachment-list.
	 */
	private LinkedList<Object[]> linkedattachmentlist;

	/**
	 * This variable stores the state of the tree data of the cluster-list, whether
	 * it is filtered (true) or not (false). we don't need to store the initial
	 * elements, since we simply can iterate all keywords to restore that list
	 */
	private boolean linkedclusterlist = false;

	/**
	 * This string builder contains all follower-(trailing)-numbers of an entry,
	 * prepared for exporting these entries.
	 */
	private LinkedList<Integer> luhmannnumbersforexport;

	/**
	 * This variable indicates whether the tabbed pane with the jTableLinks needs
	 * updates or not. When selecting an entry, it is displayed, while the links in
	 * the table still belong/refer to the activated entry. when re-activating the
	 * entry, the jTableLinks usually would be updated (due to the
	 * {@link #updateDisplay() updateDisplay()} method). but this is not necessary,
	 * when the list is already uptodate. see
	 * {@link #updateDisplayedEntryAndKeywordsListWithSelectedEntryFromLinksInteraction()
	 * showRelatedKeywords()} for and {@link #updateLinksTab() showLinks()} for
	 * further details.
	 */
	private boolean needsLinkUpdate = true;

	/**
	 * This variable indicates whether the data file is currently being saved. This
	 * should prevent the automatic backup from starting while the data file is
	 * saved.
	 */
	private boolean isSaving = false;

	/**
	 * Indicates whether a system tray icon could be successfully installed or not.
	 */
	private boolean trayIconInstalled = false;

	boolean isLiveSearchActive = false;

	private boolean isbnc = false;
	
	private ApplicationContext context;

	public boolean isBackupNecessary() {
		return isbnc;
	}

	public void backupNecessary(boolean val) {
		isbnc = val;
	}

	private boolean errorIconIsVisible = false;
	boolean editEntryFromDesktop = false;
	boolean editEntryFromSearchWindow = false;

	private String updateURI = Constants.UPDATE_URI;

	public void setUpdateURI(String uri) {
		updateURI = uri;
	}

	/**
	 * This string contains an added keyword that was added to the jTableKeywords,
	 * so the new added value can be selected immediatley after adding in to the
	 * table.
	 */
	private String newAddedKeyword = null;

	/**
	 * This string contains an added author that was added to the jTableAuthors, so
	 * the new added value can be selected immediatley after adding in to the table.
	 */
	private String newAddedAuthor = null;

	private String lastClusterRelationKeywords = "";

	/**
	 * This variable stores the treepath of the node that should bes elected within
	 * the jtreeluhmann when all followers, including parents, should be displayed
	 */
	private DefaultMutableTreeNode selectedLuhmannNode = null;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_LINKS = 0;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_LUHMANN = 1;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_KEYWORDS = 2;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_AUTHORS = 3;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_TITLES = 4;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_CLUSTER = 5;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_BOOKMARKS = 6;

	/**
	 * Constant for the selected tab of the tab pane. Since the order of the tabs
	 * might change in the future, we declare constant here, so we just have to make
	 * changes here instead of searching through the source code
	 */
	private final int TAB_ATTACHMENTS = 7;

	/**
	 * indicates the currently selected tab, which will become the previously
	 * selected tab when the tabbedpane state changed.
	 */
	private int previousSelectedTab = -1;

	/**
	 * This variables stores the currently displayed zettel. The currently
	 * <i>displayed</i> Zettel may differ from the currently <i>active</i> Zettel,
	 * if we e.g. select an entry by single-clicking it from a jTable, but do not
	 * activate it by double-clicking it.
	 */
	public int displayedZettel = -1;

	/**
	 * Indicates whether the thread "createLinksTask" is running or not...
	 */
	private boolean createLinksIsRunning = false;

	/**
	 * Indicates whether the thread "createLinksFilterTask" is running or not...
	 */
	private boolean createFilterLinksIsRunning = false;

	/**
	 * Indicates whether the thread "createLuhmannTask" is running or not...
	 */
	private boolean createClusterIsRunning = false;

	/**
	 * Indicates whether the thread "createAutoBackupTask" is running or not...
	 */
	private boolean autoBackupRunning = false;

	public boolean isAutoBackupRunning() {
		return autoBackupRunning;
	}

	public void setAutoBackupRunning(boolean val) {
		autoBackupRunning = val;
	}

	/**
	 * Since the window for editing new entries is a modeless frame, we need to have
	 * an indicator which tells us whether the an entry is currently being edited or
	 * not. if yes, don't open another window.
	 */
	private boolean isEditModeActive = false;

	/**
	 * Indicated whether a table's content is changed, e.g. filtered. if so, we have
	 * to tell this the selection listener which - otherwise - would be called
	 * several times...
	 */
	private boolean tableUpdateActive = false;

	private Timer memoryDisplayTimer = null;
	private Timer flashErrorIconTimer = null;
	private int memoryLogCounter = 0;
	private Timer makeAutoBackupTimer;
	private TrayIcon trayIcon;
	private SystemTray tray = null;

	@SuppressWarnings("unused")
	private TasksStatusBar tasksStatusBar;

	private createLinksTask cLinksTask;

	/**
	 * String array that contain highlight terms. This is used when creating the
	 * html-entry in the update display method. The {@link #findLive()
	 * live-search}-feature uses this to highlight the terms, or e.g. highlighting
	 * the keywords in the text needs this array.
	 */
	static DataFlavor urlFlavor;
	static {
		try {
			urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
		} catch (ClassNotFoundException cnfe) {
			Constants.zknlogger.log(Level.WARNING, "Could not create URL Data Flavor!");
		}
	}

	/**
	 * get the strings for file descriptions from the resource map
	 */
	private final ResourceMap toolbarResourceMap = Application
			.getInstance(ZettelkastenApp.class).getContext()
			.getResourceMap(ToolbarIcons.class);
                        
        //Constructor
	public ZettelkastenView(SingleFrameApplication app, Settings st, TasksData td) throws ClassNotFoundException,
			UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException {
		super(app);
                this.searchRequests = new SearchRequests(this); // Initialize searchRequests
                this.desktop = new DesktopData(this);
                this.settings = st;
		this.taskinfo = td;

		if (settings != null) {
			bookmarks = new Bookmarks(this, settings);
			bibtex = new BibTeX(this, settings);
			data = new Daten(this, settings, settings.getSynonyms(), bibtex);
		} else {
			// Handle the case where settings is null
			bookmarks = null; // or initialize with default value
			bibtex = null;    // or initialize with default value
			// Handle any other initialization or error handling
			// For example, you could throw an exception or log an error message
			// depending on how you want to handle the null settings scenario.
			throw new IllegalArgumentException("Settings cannot be null.");
		}

		// Init Java look and feel.
		initUIManagerLookAndFeel();

		initBibtexFile();

		// Init all swing components. (initComponents is auto-generated.)
		initComponents();

		// initComponents is auto-generated, so further initialization is done in
		// postInitComponentsSetup.
		postInitComponentsSetup(getFrame());

		// Init exit-listener: what to do when exiting.
		app.addExitListener(new ConfirmExit());

		// If the file in settings exists, load it.
		if (!loadDataDocumentFromSettings()) {
			// Failed to load. Update display for an empty data document.
			initVariables();
			updateDisplay();
		}

		// Init AutoBackupTimer.
		makeAutoBackupTimer = new Timer();
		makeAutoBackupTimer.schedule(new AutoBackupTimer(), Constants.autobackupUpdateStart,
				Constants.autobackupUpdateInterval);
	}
        
        // Method to set the search requests
        public void setSearchRequests(SearchRequests searchRequests) {
            this.searchRequests = searchRequests;
        }
        
        // Method to get the search requests
        public SearchRequests getSearchRequests() {
            return searchRequests;
        }
        
        // Method to set the desktop
        public void setDesktopData(DesktopData desktop) {
            this.desktop = desktop;
        }
        
        // Method to get the desktop
        public DesktopData getDesktopData() {
            return desktop;
        }

	// Common code to prepare and start a foreground task in ZettelkastenView.
	private void prepareAndStartTask(Task<?, ?> task) {
		ApplicationContext appC = Application.getInstance().getContext();
		TaskMonitor tM = appC.getTaskMonitor();
		TaskService tS = appC.getTaskService();
		tS.execute(task);
		tM.setForegroundTask(task);
	}

	//
	private void postInitComponentsSetup(JFrame zettelkastenViewFrame) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		ToolTipManager.sharedInstance().registerComponent(jEditorPaneIsFollower);
		jEditorPaneEntry.setBackground(new Color(Integer.parseInt(settings.getMainBackgroundColor(), 16)));

		// Init elements that starts invisible.
		jScrollPane17.setVisible(false);
		jTreeKeywords.setVisible(false);
		jPanelLiveSearch.setVisible(false);

		// Init selected
		showHighlightKeywords.setSelected(settings.getHighlightKeywords());
		jCheckBoxShowSynonyms.setSelected(settings.getShowSynonymsInTable());
		jCheckBoxShowAllLuhmann.setSelected(settings.getShowAllLuhmann());

		// Init the searchbox for the toolbar.
		// TODO Add this toolbar searchbox to the .form file, so it can be
		// auto-generated with the rest of the UI.
		createToolbarSearchbox();

		// Some LookAndFeel needs to happen after initialization.
		if (settings.isSeaGlass()) {
			ZettelkastenView.super.getFrame().getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
		}
		if (settings.isMacStyle()) {
			setupMacOSXLeopardStyle();
		}
		if (settings.isSeaGlass()) {
			setupSeaGlassStyle();
		}

		initTrees();
		initTables();
		initDragDropTransferHandler();
		initDefaultFontSize();
		initActionMaps();
		initAcceleratorTable();
		initToolbarIcons(true);
		initRecentDocumentsMenuItems();

		// initListeners is called after the defaults are set to avoid triggering
		// unnecessary actions.
		initListeners();

		// Init application icon.
		zettelkastenViewFrame.setIconImage(Constants.zknicon.getImage());

		// Init tasks status bar.
		tasksStatusBar = new TasksStatusBar(statusAnimationLabel, null, null);

		// Init MacOS-specific application listener.
		if (PlatformUtil.isMacOS()) {
			setupMacOSXApplicationListener();
		}
	}

	private void initBibtexFile() {
		File bibtexFilepath = bibtex.getFilePath();
		if (bibtexFilepath == null) {
			Constants.zknlogger.log(Level.INFO,
					"No BibTeX-File specified in the settings. Skipping BibTeX initialization.");
			return;
		}
		if (!bibtexFilepath.exists()) {
			Constants.zknlogger.log(Level.INFO,
					"BibTeX-File specified in the settings [{0}] doesn't exist. Skipping BibTeX initialization.",
					bibtexFilepath.toString());
			return;
		}

		File currentlyattachedfile = bibtex.getCurrentlyAttachedFile();
		// If we have no currently attached bibtex-file, or the currently attached
		// bibtex-file
		// differs from the new selected file of the user, open the bibtex-file now.
		if ((currentlyattachedfile == null) || (!currentlyattachedfile.toString().equals(bibtexFilepath.toString()))) {
			// Open selected file, using the character encoding of the related
			// reference-manager (i.e.
			// the programme that has exported the bib-tex-file).
			if (bibtex.openAttachedFile(Constants.BIBTEX_ENCODINGS[settings.getLastUsedBibtexFormat()], true)) {
				Constants.zknlogger.log(Level.INFO, "BibTeX-File was successfully attached.");
			} else {
				Constants.zknlogger.log(Level.INFO, "BibTeX-File could not be found nor attached.");
			}
		}
	}

	/**
	 * This method inits several listeners for our components. we do this manually
	 * instead of letting the GUI-Builder create the event-methods, because this
	 * gives a better overview and avoids having too many (event-)methods.<br>
	 * <br>
	 * Furthermore, selection listeners for the tables, trees and lists are
	 * initiated. Whenever a user makes a selection in the components on the tabbed
	 * pane, we want to react to that, either by showing the related entry or
	 * displaying other stuff.
	 */
	private void initListeners() {
		// <editor-fold defaultstate="collapsed" desc="Here all relevant listeners are
		// initiated.">
		//
		// here we start with action listeners
		//
		// This action for the checkbox toggles the setting whether the synonyms
		// should be included in the keywordlist of the jtablekeywords or not
		jCheckBoxShowSynonyms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				settings.setShowSynonymsInTable(jCheckBoxShowSynonyms.isSelected());
				data.setKeywordlistUpToDate(false);
				// Refresh keyword list.
				showKeywords();
			}
		});
		jCheckBoxShowAllLuhmann.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Change setting and updateDisplay().
				settings.setShowAllLuhmann(jCheckBoxShowAllLuhmann.isSelected());
				updateDisplay();
			}
		});
		// This settings toggles the setting whether the cluster-list in the
		// jTreeCluster
		// should show *all* keywords or only those that are related to the current
		// entry.
		// "related" means, that we retrieve the current entry's keywords and search
		// through
		// all entries and retrieve those entries' keywords as well, if these entries'
		// keywords contain at least one keyword of the current entry's keywords.
		jCheckBoxCluster.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Tell that clusterlist is no longer up to date
				data.setClusterlistUpToDate(false);
				// refresh cluster list
				showCluster();
			}
		});
		// clear combobox
		jComboBoxAuthorType.removeAllItems();
		// add items to the combobox
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeAll"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeArticle"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeBook"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeBookArticle"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeChapter"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypePhD"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeThesis"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeUnpublished"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeConference"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeTechreport"));
		jComboBoxAuthorType.addItem(getResourceMap().getString("entryTypeNoBibKey"));
		// set rowcount
		jComboBoxAuthorType.setMaximumRowCount(jComboBoxAuthorType.getItemCount());
		// select the last active look and feel
		jComboBoxAuthorType.setSelectedIndex(0);
		// init actionlistener
		jComboBoxAuthorType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// authorlist needs update
				data.setAuthorlistUpToDate(false);
				// show authors
				showAuthors();
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
		jListEntryKeywords.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuKeywordList.isVisible()) {
					jPopupMenuKeywordList.show(jListEntryKeywords, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuKeywordList.isVisible()) {
					jPopupMenuKeywordList.show(jListEntryKeywords, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, leeave...
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on single click...
				if (1 == evt.getClickCount() && displayedZettel == data.getActivatedEntryNumber()) {
					// filter links
					filterLinks();
					highlightSegs();
				} // or search keyword on double click
				else if (evt.getClickCount() == 2) {
					searchKeywordsFromListLogAnd();
				}
			}
		});
		jEditorPaneEntry.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
					jPopupMenuMain.show(jEditorPaneEntry, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuMain.isVisible()) {
					jPopupMenuMain.show(jEditorPaneEntry, evt.getPoint().x, evt.getPoint().y);
				}
			}
		});
		jTableLinks.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed..
				if (evt.isPopupTrigger() && !jPopupMenuLinks.isVisible()) {
					jPopupMenuLinks.show(jTableLinks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed..
				if (evt.isPopupTrigger() && !jPopupMenuLinks.isVisible()) {
					jPopupMenuLinks.show(jTableLinks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// If a button other then left-mouse button, do nothing.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// On double-click, show entry.
				if (evt.getClickCount() == 2) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableLinks, 0));
				}
			}
		});
		jTableManLinks.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuLinks.isVisible()) {
					jPopupMenuLinks.show(jTableManLinks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuLinks.isVisible()) {
					jPopupMenuLinks.show(jTableManLinks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on double-click, show entry
				if (evt.getClickCount() == 2) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableManLinks, 0));
				}
			}
		});
		jTableKeywords.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuKeywords.isVisible()) {
					jPopupMenuKeywords.show(jTableKeywords, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuKeywords.isVisible()) {
					jPopupMenuKeywords.show(jTableKeywords, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on double-click, show entry
				if (evt.getClickCount() == 2) {
					searchLogOr();
				}
			}
		});
		jTableAuthors.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuAuthors.isVisible()) {
					jPopupMenuAuthors.show(jTableAuthors, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuAuthors.isVisible()) {
					jPopupMenuAuthors.show(jTableAuthors, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on double-click, show entry
				if (evt.getClickCount() == 2) {
					searchLogOr();
				}
			}
		});
		jTableTitles.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuTitles.isVisible()) {
					jPopupMenuTitles.show(jTableTitles, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuTitles.isVisible()) {
					jPopupMenuTitles.show(jTableTitles, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// If a button other then left-mouse button, do nothing.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// On double-click, show entry.
				if (evt.getClickCount() == 2) {
					UpdateDisplayOptions options = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder()
							.updateTitlesTab(false).build();
					int selectedEntry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableTitles, 0);
					setNewActivatedEntryAndUpdateDisplay(selectedEntry, options);
				}
			}
		});
		jTableBookmarks.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuBookmarks.isVisible()) {
					jPopupMenuBookmarks.show(jTableBookmarks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuBookmarks.isVisible()) {
					jPopupMenuBookmarks.show(jTableBookmarks, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on double-click, show entry
				if (evt.getClickCount() == 2) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableBookmarks, 0));
				}
			}
		});
		jTableAttachments.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuAttachments.isVisible()) {
					jPopupMenuAttachments.show(jTableAttachments, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuAttachments.isVisible()) {
					jPopupMenuAttachments.show(jTableAttachments, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on double-click, open attachment
				if (evt.getClickCount() == 2) {
					openAttachment();
				}
			}
		});
		jTreeLuhmann.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuLuhmann.isVisible()) {
					jPopupMenuLuhmann.show(jTreeLuhmann, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Check whether the popup-trigger-mouse-key was pressed.
				if (evt.isPopupTrigger() && !jPopupMenuLuhmann.isVisible()) {
					jPopupMenuLuhmann.show(jTreeLuhmann, evt.getPoint().x, evt.getPoint().y);
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on single-click, show entry
				if (evt.getClickCount() == 2) {
					setNewActivatedEntryAndUpdateDisplay(selectedEntryInJTreeLuhmann());
				}
			}
		});
		jTreeCluster.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				// on single-click, show cluster relations...
				if (1 == evt.getClickCount()) {
					showClusterRelations();
				}
			}
		});
		jLabelMemory.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				// This listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1) {
					return;
				}
			}
		});
		//
		// here we start with key-listeners
		//
		jListEntryKeywords.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				// if a navigation-key (arrows, page-down/up, home etc.) is pressed,
				// we assume a new item-selection, so behave like on a mouse-click and
				// filter the links
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// filter links
					filterLinks();
					highlightSegs();
				}
			}
		});
		jTextFieldLiveSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				// when the user presses the escape-key, hide panel
				if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
					findLiveCancel();
				} else if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
					// get the text from the live-search-textbox
					String livetext = jTextFieldLiveSearch.getText();
					// only highlight text, when we have more that two chars
					if (livetext.length() > 1) {
						// create array with search term
						// set highlightterms
						HtmlUbbUtil.setHighlighTerms(new String[] { livetext }, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH,
								false);
						updateDisplay();
					}
				}
			}
		});
		jTreeCluster.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				// if a navigation-key (arrows, page-down/up, home etc.) is pressed,
				// we assume a new item-selection, so behave like on a mouse-click and
				// show the cluster relations.
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					showClusterRelations();
				}
			}
		});
		jTextFieldFilterKeywords.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTableKeywords,
							evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTableKeywords,
							jTextFieldFilterKeywords, 0);
				}
			}
		});
		jTextFieldFilterAuthors.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTableAuthors, evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTableAuthors,
							jTextFieldFilterAuthors, 0);
				}
			}
		});
		jTextFieldFilterTitles.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTableTitles, evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTableTitles, jTextFieldFilterTitles,
							1);
				}
			}
		});
		jTextFieldFilterCluster.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				// select treenode live, while the user is typing...
				TreeUtil.selectByTyping(jTreeCluster, jTextFieldFilterCluster);
			}
		});
		jTextFieldFilterAttachments.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTableAttachments,
							evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTableAttachments,
							jTextFieldFilterAttachments, 0);
				}
			}
		});
		//
		// The hyperlink-listeners
		//
		jEditorPaneEntry.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// if the link was clicked, proceed
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					// get input event with additional modifiers
					InputEvent inev = evt.getInputEvent();
					// check whether shift key was pressed, and if so, remove manual link
					if (inev.isControlDown() || inev.isMetaDown()) {
						if (Tools.removeHyperlink(evt.getDescription(), data, displayedZettel)) {
							updateDisplay();
						}
					} else {
						openHyperlink(evt.getDescription());
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					javax.swing.text.Element elem = evt.getSourceElement();
					if (elem != null) {
						AttributeSet attr = elem.getAttributes();
						AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
						if (a != null) {
							jEditorPaneEntry.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
					jEditorPaneEntry.setToolTipText(null);
				}
			}
		});
		//
		// The hyperlink-listeners
		//
		jEditorPaneIsFollower.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// if the link was clicked, proceed
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					openHyperlink(evt.getDescription());
				}
			}
		});
		//
		// The hyperlink-listeners
		//
		jEditorPaneClusterEntries.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// if the link was clicked, proceed
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					openHyperlink(evt.getDescription());
				} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					javax.swing.text.Element elem = evt.getSourceElement();
					if (elem != null) {
						AttributeSet attr = elem.getAttributes();
						AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
						if (a != null) {
							jEditorPaneClusterEntries.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
					jEditorPaneClusterEntries.setToolTipText(null);
				}
			}
		});

		// Init the JTable selection listeners.
		JTable[] tables = new JTable[] { jTableLinks, jTableManLinks, jTableAuthors,
				jTableTitles, jTableBookmarks, jTableAttachments };
		for (JTable t : tables) {
			SelectionListener listener = new SelectionListener(t);
			t.getSelectionModel().addListSelectionListener(listener);
			t.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		}

		// Selection in a JTree is what is highlighted as selected in the UI. If
		// selection changes, this listener is called.
		jTreeLuhmann.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				handleNoteSequenceValueChange();
			}
		});
		jTreeLuhmann.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent evt) {
				// retrieve path of value that was expanded
				TreePath tp = evt.getPath();
				// check whether root was expanded or not. Therefore, retrieve
				// last node of the treepath, i.e. the node which was expanded
				DefaultMutableTreeNode expandednode = (DefaultMutableTreeNode) tp.getLastPathComponent();
				// if they equal, do nothing
				TreeUserObject userObject = (TreeUserObject) expandednode.getUserObject();
				userObject.setCollapsed(false);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent evt) {
				// retrieve path of value that was expanded
				TreePath tp = evt.getPath();
				// check whether root was expanded or not. Therefore, retrieve
				// last node of the treepath, i.e. the node which was expanded
				DefaultMutableTreeNode expandednode = (DefaultMutableTreeNode) tp.getLastPathComponent();
				// if they equal, do nothing
				TreeUserObject userObject = (TreeUserObject) expandednode.getUserObject();
				userObject.setCollapsed(true);
			}
		});
		//
		// init the menu-listeners...
		//
		recentDocsSubMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				initRecentDocumentsMenuItems();
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		recentDoc1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(1);
				if (fp != null) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(2);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(3);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(4);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(5);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(6);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(7);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		recentDoc8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String fp = settings.getRecentDoc(8);
				if (fp != null && !fp.isEmpty()) {
					openRecentDocument(fp);
				}
			}
		});
		fileMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				menuFileInformation
						.setEnabled(settings.getMainDataFile() != null && settings.getMainDataFile().exists());
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				highlightSegmentsMenuItem.setSelected(settings.getHighlightSegments());
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		editMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// set indicator which show whether we have selections or not
				setListFilledWithEntry(!jListEntryKeywords.getSelectedValuesList().isEmpty());
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		windowsMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				showSearchResultsMenuItem.setEnabled(searchRequests.getCount() > 0);
				showDesktopMenuItem.setEnabled(desktop.getCount() > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		findEntryKeywordsMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// set indicator which show whether we have selections or not
				setListFilledWithEntry(!jListEntryKeywords.getSelectedValuesList().isEmpty());
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuLinks.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				initViewMenuLinks();
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuAuthors.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// new keyword is always possible
				viewAuthorsNew.setEnabled(true);
				// at least one selection needed
				setTableEntriesSelected(jTableAuthors.getSelectedRowCount() > 0);
				setExportPossible(data.getCount(Daten.AUCOUNT) > 0);
				setBibtexFileLoaded(bibtex.getCurrentlyAttachedFile() != null);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuBookmarks.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// at least one selection needed
				setTableEntriesSelected(jTableBookmarks.getSelectedRowCount() > 0);
				setExportPossible(jTableBookmarks.getRowCount() > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuKeywords.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// new keyword is always possible
				viewKeywordsNew.setEnabled(true);
				// at least one selection needed
				setTableEntriesSelected(jTableKeywords.getSelectedRowCount() > 0);
				setExportPossible(data.getCount(Daten.KWCOUNT) > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuAttachments.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// get the amount of selected rows
				// at least one selection needed
				setTableEntriesSelected(jTableAttachments.getSelectedRowCount() > 0);
				setExportPossible(jTableAttachments.getRowCount() > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuTitles.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// get the amount of selected rows
				// at least one selection needed
				setTableEntriesSelected(jTableTitles.getSelectedRowCount() > 0);
				setExportPossible(data.getCount(Daten.ZKNCOUNT) > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuLuhmann.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				// set selected state
				viewMenuLuhmannShowNumbers.setSelected(settings.getShowLuhmannEntryNumber());
				// retrieve selected node
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeLuhmann.getLastSelectedPathComponent();
				// check whether any selection made, that is not the root
				setLuhmannSelected((node != null) && (!node.isRoot()));
				setTableEntriesSelected((node != null) && (!node.isRoot()));
				setExportPossible(!data.getSubEntriesCsv(displayedZettel).isEmpty());
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		viewMenuCluster.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent evt) {
				setExportPossible(clusterList.size() > 0);
			}

			@Override
			public void menuDeselected(MenuEvent evt) {
			}

			@Override
			public void menuCanceled(MenuEvent evt) {
			}
		});
		//
		// init the menu-listeners...
		//
		jPopupMenuKeywords.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// new keyword is always possible
				popupKeywordsNew.setEnabled(true);
				// at least one selection needed
				setTableEntriesSelected(jTableKeywords.getSelectedRowCount() > 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuKeywordList.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				initViewMenuLinks();
				popupKwListHighlightSegments.setSelected(settings.getHighlightSegments());
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuAuthors.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// new keyword is always possible
				popupAuthorsNew.setEnabled(true);
				// at least one selection needed
				setTableEntriesSelected(jTableAuthors.getSelectedRowCount() > 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuLuhmann.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// retrieve selected node
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeLuhmann.getLastSelectedPathComponent();
				// check whether any selection made, that is not the root
				setLuhmannSelected((node != null) && (!node.isRoot()));
				setTableEntriesSelected((node != null) && (!node.isRoot()));
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuTitles.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// at least one selection needed
				setTableEntriesSelected(jTableTitles.getSelectedRowCount() > 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuBookmarks.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// at least one selection needed
				setTableEntriesSelected(jTableBookmarks.getSelectedRowCount() > 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuLinks.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				initViewMenuLinks();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuAttachments.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				// at least one selection needed
				setTableEntriesSelected(jTableAttachments.getSelectedRowCount() > 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});
		jPopupMenuMain.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
				try {
					// set copy/cut actions en- or disabled, depending on whether we have
					// selected text or not
					String selection = jEditorPaneEntry.getSelectedText();
					// if we have selected text
					if (selection != null) {
						// enabled property
						setTextSelected(!selection.isEmpty());
					} else {
						// else disable it
						setTextSelected(false);
					}
				} catch (IllegalArgumentException e) {
					// when the selection is deleted, this exception
					// occurs. so disable copy/cut
					setTextSelected(false);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent evt) {
			}
		});

		jTabbedPaneMain.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				int sel = jTabbedPaneMain.getSelectedIndex();
				if (sel != previousSelectedTab) {
					// Update displayed entry to activated entry if tab changed.
					setNewDisplayedEntryAndUpdateDisplay(data.getActivatedEntryNumber());
				} else {
					updateDisplay();
				}
			}
		});
		// </editor-fold>
	}

	private void highlightSegs() {
		// and highlight text segments
		if (settings.getHighlightSegments()) {
			// jEditorPaneEntry.setDocument(new HTMLDocument());
			updateDisplay();
		}
	}

	private void createToolbarSearchbox() {
		// init a search textfield that is added to the toolbar
		tb_searchTextfield = new JTextField(15);
		// on mac, make textfield look like a search box
		if (settings.isMacStyle() || settings.isSeaGlass()) {
			tb_searchTextfield.putClientProperty("JTextField.variant", "search");
		} else {
			tb_searchTextfield.setPreferredSize(new Dimension(150, 26));
			tb_searchTextfield.setMaximumSize(new Dimension(200, 26));
			tb_searchTextfield.setAlignmentY(Component.CENTER_ALIGNMENT);
		}
		tb_searchTextfield.setToolTipText(getResourceMap().getString("searchfieldTooltip"));
		// put action to the tables' actionmaps
		tb_searchTextfield.getActionMap().put("EnterKeyPressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField tf = (JTextField) e.getSource();
				// create a regular expression, that separates the input at each comma.
				// furthermore, commas within double-quotes ("") are not treated as
				// separator-char,
				// so the user can search for sentences that include commas as well. and
				// finally, the
				// quotes are removed, since we don't need them...
				Matcher mat = Pattern.compile("(\"(.*?)\"|([^,]+)),?").matcher(tf.getText());
				// create a new list that will contain each found pattern (i.e. searchterm)
				List<String> result = new ArrayList<>();
				while (mat.find()) {
					result.add(mat.group(2) == null ? mat.group(3) : mat.group(2));
				}
				// and copy the list to our array...
				String[] searchterms = result.toArray(new String[result.size()]);
				startSearch(searchterms,
						Constants.SEARCH_AUTHOR | Constants.SEARCH_CONTENT | Constants.SEARCH_TITLE
								| Constants.SEARCH_KEYWORDS | Constants.SEARCH_REMARKS,
						Constants.LOG_OR, false, false, true, /* accentInsensitive= */ true, false, false, "", "", 0,
						false, Constants.STARTSEARCH_USUAL, Constants.SEARCH_USUAL);
			}
		});
		// associate enter-keystroke with that action
		KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
		tb_searchTextfield.getInputMap().put(ks, "EnterKeyPressed");
		// add search box to toolbar
		jPanelSearchBox = new JPanel();
		jLabelLupe = new JLabel();
		jPanelSearchBox.setName("jPanelSearchBox");
		jLabelLupe.setName("jLabelLupe");
		jLabelLupe.setIcon(Constants.lupeIcon);
		GroupLayout jPanelSearchBoxLayout = new GroupLayout(jPanelSearchBox);
		jPanelSearchBox.setLayout(jPanelSearchBoxLayout);
		jPanelSearchBoxLayout.setHorizontalGroup(jPanelSearchBoxLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanelSearchBoxLayout.createSequentialGroup().addContainerGap().addComponent(jLabelLupe)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(tb_searchTextfield, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanelSearchBoxLayout.setVerticalGroup(jPanelSearchBoxLayout
				.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(jPanelSearchBoxLayout.createSequentialGroup().addContainerGap()
						.addGroup(jPanelSearchBoxLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelLupe).addComponent(tb_searchTextfield,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap(20, Short.MAX_VALUE)));
		toolBar.add(settings.isSeaGlass() ? tb_searchTextfield : jPanelSearchBox);
		// hide label on mac
		jLabelLupe.setVisible(!settings.isMacStyle() && !settings.isSeaGlass());
	}

	/**
	 * This method initialises the toolbar buttons. depending on the user-setting,
	 * we either display small, medium or large icons as toolbar-icons.
	 *
	 * @param bottomBarNeedsUdpate if {@code true}, the bottom bar on mac aqua style
	 *                             will also be re-initialized. Use {@code true}
	 *                             only the first time the bottom bar is
	 *                             initialized. For further GUI-updates, e.g. from
	 *                             settings window, use {@code false} as parameter.
	 */
	private void initToolbarIcons(boolean bottomBarNeedsUdpate) {
		statusErrorButton.setVisible(false);
		statusDesktopEntryButton.setVisible(false);

		// check whether the toolbar should be displayed at all...
		if (settings.getShowIcons() || settings.getShowIconText()) {// set toolbar visible
			toolBar.setVisible(true);
			// and remove border from the main panel
			mainPanel.setBorder(null);

			// init toolbar button array
			JButton toolbarButtons[] = new JButton[] { tb_open, tb_save, tb_first, tb_next, tb_prev, tb_copy, tb_paste,
					tb_newEntry, tb_edit, tb_find, tb_addtodesktop, tb_addbookmark, tb_delete, tb_addluhmann,
					tb_addmanlinks, tb_selectall, tb_last };
			String[] buttonNames = new String[] { "tb_openText", "tb_saveText", "tb_firstText", "tb_nextText",
					"tb_prevText", "tb_copyText", "tb_pasteText", "tb_newEntryText", "tb_editText", "tb_findText",
					"tb_addtodesktopText", "tb_addbookmarkText", "tb_deleteText", "tb_addluhmannText",
					"tb_addmanlinksText", "tb_selectallText", "tb_lastText" };
			String[] iconNames = new String[] { "openIcon", "saveIcon", "showFirstEntryIcon", "showNextEntryIcon",
					"showPrevEntryIcon", "copyIcon", "pasteIcon", "newEntryIcon", "editEntryIcon", "findIcon",
					"addDesktopIcon", "addBookmarksIcon", "deleteIcon", "addLuhmannIcon", "addManLinksIcon",
					"selectAllIcon", "showLastEntryIcon" };

			// Show icon names ?
			if (settings.getShowIconText()) {
				for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
					toolbarButtons[cnt].setText(toolbarResourceMap.getString(buttonNames[cnt]));
				}
			} else {
				for (JButton tbb : toolbarButtons) {
					tbb.setText("");
				}
			}
			// Show icons ?
			if (settings.getShowIcons()) {
				// retrieve icon theme path
				String icontheme = settings.getIconThemePath();
				for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
					toolbarButtons[cnt].setIcon(new ImageIcon(ZettelkastenView.class
							.getResource(icontheme + toolbarResourceMap.getString(iconNames[cnt]))));
				}
			} else {
				for (JButton tbb : toolbarButtons) {
					tbb.setIcon(null);
				}
			}
			// check if all toolbar icons should be displayed or not
			if (settings.getShowIcons()) {
				tb_edit.setVisible(settings.getShowAllIcons());
				tb_delete.setVisible(settings.getShowAllIcons());
				tb_selectall.setVisible(settings.getShowAllIcons());
				tb_addtodesktop.setVisible(settings.getShowAllIcons());
				tb_find.setVisible(settings.getShowAllIcons());
			}
			if (settings.isMacStyle() && bottomBarNeedsUdpate) {
				makeMacToolbar();
			}
			if (settings.isSeaGlass()) {
				makeSeaGlassToolbar();
			}
		} else {
			// if not, hide it and leave.
			toolBar.setVisible(false);
			// and set a border to the main panel, because the toolbar's dark border is
			// hidden
			// and remove border from the main panel
			mainPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorUtil.colorDarkLineGray));
			return;
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
		// and retrieve action names as well as accelerator keys. This saves a lot of
		// typing work here
		//
		// get the action map
		ActionMap actionMap = Application
				.getInstance(ZettelkastenApp.class).getContext()
				.getActionMap(ZettelkastenView.class, this);
		// iterate the xml file with the accelerator keys for the main window
		for (int cnt = 1; cnt <= settings.getAcceleratorKeys().getCount(AcceleratorKeys.MAINKEYS); cnt++) {
			// get the action's name
			String actionname = settings.getAcceleratorKeys().getAcceleratorAction(AcceleratorKeys.MAINKEYS, cnt);
			// check whether we have found any valid action name
			if (actionname != null && !actionname.isEmpty()) {
				// retrieve action
				AbstractAction ac = (AbstractAction) actionMap.get(actionname);
				// get the action's accelerator key
				String actionkey = settings.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.MAINKEYS, cnt);
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
		//
		// ATTENTION! Mnemonic keys are NOT applied on Mac OS, see Apple guidelines for
		// further details:
		// http://developer.apple.com/DOCUMENTATION/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html#//apple_ref/doc/uid/TP40001909-211867-BCIBDHFJ
		//
		// when we have aqua look&feel, make some of the menu items invisivle, which
		// already
		// appear in the Apple-Menu
		if (settings.isMacStyle()) {
			aboutMenu.setVisible(false);
			exitMenuItem.setVisible(false);
			jSeparatorExit.setVisible(false);
		} else {
			// init the variables
			String menutext;
			char mkey;
			// The mnemonic key for the file menu
			menutext = fileMenu.getText();
			mkey = menutext.charAt(0);
			fileMenu.setMnemonic(mkey);
			// The mnemonic key for the edit menu
			menutext = editMenu.getText();
			mkey = menutext.charAt(0);
			editMenu.setMnemonic(mkey);
			// The mnemonic key for the search menu
			menutext = findMenu.getText();
			mkey = menutext.charAt(0);
			findMenu.setMnemonic(mkey);
			// The mnemonic key for the view menu
			menutext = viewMenu.getText();
			mkey = menutext.charAt(0);
			viewMenu.setMnemonic(mkey);
			// The mnemonic key for the windows menu
			menutext = windowsMenu.getText();
			mkey = menutext.charAt(0);
			windowsMenu.setMnemonic(mkey);
		}
		// on Mac OS, at least for the German locale, the File menu is called different
		// compared to windows or linux. Furthermore, we don't need the about and
		// preferences
		// menu items, since these are locates on the program's menu item in the
		// apple-menu-bar
		if (PlatformUtil.isMacOS()) {
			fileMenu.setText(getResourceMap().getString("macFileMenuText"));
		}
	}

	/**
	 * This methods initiates table sorter for the jTables in the main window we can
	 * now order the content of the keyword-lists, author-lists etc. in the
	 * jTabbedPane by clicking on the table header. <br>
	 * <br>
	 * Furthermore, visual settings like gridlines are set here. <br>
	 * <br>
	 * Finally, we setup actionsmaps and associate them with the enter-key. by doing
	 * so, we prevent the enter-key from selecting the next line. instead, a search
	 * request is startetd.
	 */
	private void initTables() {
		// Create custom tablerow-sorter for sorting certain table rows that
		// might contain german umlauts
		setCustomTableRowSorter(jTableAuthors, 0);
		setCustomTableRowSorter(jTableKeywords, 0);
		setCustomTableRowSorter(jTableTitles, 1);
		setCustomTableRowSorter(jTableLinks, 1);
		setCustomTableRowSorter(jTableManLinks, 1);
		setCustomTableRowSorter(jTableBookmarks, 1);
		setCustomTableRowSorter(jTableAttachments, 0);
		// 6th column in title table has icons
		jTableTitles.getColumnModel().getColumn(5).setCellRenderer(new TitleTableCellRenderer(data));
		JTable[] tables = new JTable[] { jTableLinks, jTableManLinks, jTableKeywords,
				jTableAuthors, jTableTitles, jTableBookmarks, jTableAttachments };
		for (JTable t : tables) {
			t.getTableHeader().setReorderingAllowed(false);
			t.setGridColor(settings.getTableGridColor());
			t.setShowHorizontalLines(settings.getShowGridHorizontal());
			t.setShowVerticalLines(settings.getShowGridVertical());
			t.setIntercellSpacing(settings.getCellSpacing());
			// make extra table-sorter for itunes-tables
			if (settings.isMacStyle()) {
				TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
					@Override
					public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
					}
				};
				TableUtils.makeSortable(t, sortDelegate);
				// change back default column-resize-behaviour when we have itunes-tables,
				// since the default for those is "auto resize off"
				t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			}
//            if (settings.isMacStyle()) {
//                t.setDefaultRenderer(String.class, new MacSourceList.CustomTableCellRenderer());
//            }
		}
	}

	/**
	 *
	 */
	private void initDragDropTransferHandler() {
		jTableLinks.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				return de.danielluedecke.zettelkasten.util.TableUtils.prepareStringForTransferHandler(jTableLinks);
			}

			@Override
			protected boolean importString(JComponent c, String str) {
				int[] entries = Tools.retrieveEntryNumbersFromTransferHandler(str, data.getCount(Daten.ZKNCOUNT));
				if (entries != null) {
					addToManLinks(entries);
					return true;
				}
				return false;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});
		jTableManLinks.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				return de.danielluedecke.zettelkasten.util.TableUtils.prepareStringForTransferHandler(jTableManLinks);
			}

			@Override
			protected boolean importString(JComponent c, String str) {
				int[] entries = Tools.retrieveEntryNumbersFromTransferHandler(str, data.getCount(Daten.ZKNCOUNT));
				if (entries != null) {
					addToManLinks(entries);
					return true;
				}
				return false;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});
		jTableBookmarks.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				return de.danielluedecke.zettelkasten.util.TableUtils.prepareStringForTransferHandler(jTableBookmarks);
			}

			@Override
			protected boolean importString(JComponent c, String str) {
				int[] entries = Tools.retrieveEntryNumbersFromTransferHandler(str, data.getCount(Daten.ZKNCOUNT));
				if (entries != null) {
					addToBookmarks(entries, false);
					return true;
				}
				return false;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});

		// Enable drag&drop in jTreeLuhmann.
		jTreeLuhmann.setDragEnabled(true);
		jTreeLuhmann.setTransferHandler(new EntryStringTransferHandler() {
			private EntryID draggedEntryToRemove = null;
			private EntryID draggedEntryParent = null;

			@Override
			protected String exportString(JComponent c) {
				JTree jTree = (JTree) c;
				DefaultMutableTreeNode draggedNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

				// Remember the dragged node, which must be removed when dropping the node.
				draggedEntryToRemove = TreeUtil.getEntryID(draggedNode);
				draggedEntryParent = TreeUtil.getEntryID(draggedNode.getParent());

				return Constants.DRAG_SOURCE_JTREELUHMANN;
			}

			@Override
			protected boolean importString(JComponent c, String dropInformationString) {
				// dropInformationString comes from the exportString() above.
				if (dropInformationString == null
						|| !dropInformationString.equals(Constants.DRAG_SOURCE_JTREELUHMANN)) {
					return false;
				}

				JTree jTree = (JTree) c;
				DefaultMutableTreeNode droppedNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
				if (droppedNode == null) {
					return false;
				}
				EntryID droppedEntry = TreeUtil.getEntryID(droppedNode);
				EntryID droppedEntryParent = TreeUtil.getEntryID(droppedNode.getParent());

				boolean droppedIntoItself = draggedEntryToRemove.equals(droppedEntry);
				if (droppedIntoItself) {
					return false;
				}

				// Drag&drop of jTreeLuhmann only works for reordering the sub-entries of a
				// specific entry. It doesn't currently support moving across entries.
				boolean droppedIntoParent = draggedEntryParent.equals(droppedEntry);
				boolean droppedIntoSibling = draggedEntryParent.equals(droppedEntryParent);
				boolean droppedIntoParentOrSibling = droppedIntoParent || droppedIntoSibling;
				if (!droppedIntoParentOrSibling) {
					return false;
				}

				// Delete the dragged entry before reinserting it.
				data.deleteLuhmannNumber(draggedEntryParent, draggedEntryToRemove);

				if (droppedIntoParent) {
					// Reinsert dragged entry at first position.
					data.addSubEntryToEntryAtPosition(draggedEntryParent, draggedEntryToRemove, 0);
				} else if (droppedIntoSibling) {
					// Reinsert dragged entry after dropped entry.
					data.addSubEntryToEntryAfterSibling(draggedEntryParent, draggedEntryToRemove, droppedEntry);
				} else {
					// This should never happen.
					Constants.zknlogger.log(Level.SEVERE, "BUG: jTreeLuhmann importString");
				}

				updateDisplay();
				return true;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});

		jListEntryKeywords.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				// retrieve selections
				List<String> kws = jListEntryKeywords.getSelectedValuesList();
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
					// remove carriage returns
					str = str.replace("\r", "");
					// split at each new line
					addKeywords(str.split("\n"), true);
					return true;
				}
				return false;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});
		jTableKeywords.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				// create string builder
				StringBuilder keywords = new StringBuilder("");
				// retrieve selected valued that are being dragged
				String[] kws = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0);
				// create a comma-separated string from string array
				for (String k : kws) {
					keywords.append(k).append("\n");
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
		jTableAttachments.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				// create string builder
				StringBuilder attachments = new StringBuilder("");
				// retrieve selected valued that are being dragged
				String[] atts = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAttachments, 0);
				// create a comma-separated string from string array
				for (String a : atts) {
					attachments.append(a).append("\n");
				}
				// return results
				return attachments.toString();
			}

			@Override
			protected boolean importString(JComponent c, String str) {
				return false;
			}

			@Override
			protected void cleanup(JComponent c, boolean remove) {
			}
		});
		jTableAuthors.setTransferHandler(new EntryStringTransferHandler() {
			@Override
			protected String exportString(JComponent c) {
				// create string builder
				StringBuilder authors = new StringBuilder("");
				authors.append(Constants.DRAG_SOURCE_TYPE_AUTHORS).append("\n");
				// retrieve selected values that are being dragged
				String[] aus = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0);
				// create a comma-separated string from string array
				for (String a : aus) {
					authors.append(a).append("\n");
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
		jEditorPaneEntry.setDragEnabled(true);
		new DropTarget(jEditorPaneEntry, this);
	}

	/**
	 * This method creates an own tablerow-sorter. This is necessary since we can
	 * use own comparators here, so we can insert string with German umlauts at the
	 * correct position - i.e. "ä" is inserted in "a", and not after "z".
	 *
	 * @param table  the table that should get the custom tablerow-sorter
	 * @param column the column where the sorter should be apllied to
	 */
	private void setCustomTableRowSorter(JTable table, int column) {
		// create new table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>();
		// Tell tgis jtable that it has an own sorter
		table.setRowSorter(sorter);
		// and tell the sorter, which table model to sort.
		sorter.setModel((DefaultTableModel) table.getModel());
		// in this table, the first column needs a custom comparator.
		try {
			sorter.setComparator(column, new Comparer());
			// in case we have the table with titles, we make an exception, because
			// this table has two more columns that should be sorted, the columns with
			// the entries timestamps.
			if (table == jTableTitles) {
				sorter.setComparator(2, new DateComparer());
				sorter.setComparator(3, new DateComparer());
			}
		} catch (IndexOutOfBoundsException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// get last table sorting
		SortKey sk = settings.getTableSorting(table);
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
	}

	/**
	 * This method sets the default font-size for tables, lists and treeviews. If
	 * the user wants to have bigger font-sizes for better viewing, the new
	 * font-size will be applied to the components here.
	 */
	private void initDefaultFontSize() {
		Font settingsTableFont = settings.getTableFont();
		jTableLinks.setFont(settingsTableFont);
		jTableManLinks.setFont(settingsTableFont);
		jTableAuthors.setFont(settingsTableFont);
		jTableKeywords.setFont(settingsTableFont);
		jTableTitles.setFont(settingsTableFont);
		jTableBookmarks.setFont(settingsTableFont);
		jTableAttachments.setFont(settingsTableFont);
		jListEntryKeywords.setFont(settingsTableFont);
		jTreeLuhmann.setFont(settingsTableFont);
		jTreeCluster.setFont(settingsTableFont);
		jTreeKeywords.setFont(settingsTableFont);
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
		// Init the locale for the default actions cut/copy/paste.
		Tools.initLocaleForDefaultActions(
				Application.getInstance(ZettelkastenApp.class)
						.getContext().getActionMap(ZettelkastenView.class, this));

		// <editor-fold defaultstate="collapsed" desc="Init of action-maps so we have
		// shortcuts for the tables">
		// create action which should be executed when the user presses
		// the enter-key
		AbstractAction a_enter = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTableAuthors == e.getSource()) {
					searchLogOr();
				} else if (jTableKeywords == e.getSource()) {
					searchLogOr();
				} else if (jTableLinks == e.getSource()) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableLinks, 0));
				} else if (jTableManLinks == e.getSource()) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableManLinks, 0));
				} else if (jTableTitles == e.getSource()) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableTitles, 0));
				} else if (jTextFieldFilterKeywords == e.getSource()) {
					filterKeywordList(false);
				} else if (jTextFieldFilterAttachments == e.getSource()) {
					filterAttachmentList(false);
				} else if (jTextFieldFilterAuthors == e.getSource()) {
					filterAuthorList(false);
				} else if (jTextFieldFilterTitles == e.getSource()) {
					filterTitleList(false);
				} else if (jTextFieldFilterCluster == e.getSource()) {
					filterClusterList();
				} else if (jTreeLuhmann == e.getSource()) {
					setNewActivatedEntryAndUpdateDisplay(selectedEntryInJTreeLuhmann());
				} else if (jListEntryKeywords == e.getSource()) {
					searchKeywordsFromListLogOr();
				} else if (jTableBookmarks == e.getSource()) {
					setNewActivatedEntryAndUpdateDisplay(
							ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableBookmarks, 0));
				} else if (jTableAttachments == e.getSource()) {
					openAttachment();
				} else if (jTextFieldEntryNumber == e.getSource()) {
					ZettelkastenViewUtil.hiddenFeatures(ZettelkastenView.this, jTextFieldEntryNumber, data,
							searchRequests, desktop, settings, settings.getAcceleratorKeys(), bibtex, displayedZettel);
				}
			}
		};
		// put action to the tables' actionmaps
		jTableAuthors.getActionMap().put("EnterKeyPressed", a_enter);
		jTableKeywords.getActionMap().put("EnterKeyPressed", a_enter);
		jTableLinks.getActionMap().put("EnterKeyPressed", a_enter);
		jTableManLinks.getActionMap().put("EnterKeyPressed", a_enter);
		jTableTitles.getActionMap().put("EnterKeyPressed", a_enter);
		jTableBookmarks.getActionMap().put("EnterKeyPressed", a_enter);
		jTableAttachments.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldFilterKeywords.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldFilterAttachments.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldFilterAuthors.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldFilterTitles.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldFilterCluster.getActionMap().put("EnterKeyPressed", a_enter);
		jTextFieldEntryNumber.getActionMap().put("EnterKeyPressed", a_enter);
		jTreeLuhmann.getActionMap().put("EnterKeyPressed", a_enter);
		jListEntryKeywords.getActionMap().put("EnterKeyPressed", a_enter);
		// associate enter-keystroke with that action
		KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
		jTableAuthors.getInputMap().put(ks, "EnterKeyPressed");
		jTableKeywords.getInputMap().put(ks, "EnterKeyPressed");
		jTableLinks.getInputMap().put(ks, "EnterKeyPressed");
		jTableManLinks.getInputMap().put(ks, "EnterKeyPressed");
		jTableTitles.getInputMap().put(ks, "EnterKeyPressed");
		jTableBookmarks.getInputMap().put(ks, "EnterKeyPressed");
		jTableAttachments.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldFilterKeywords.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldFilterAttachments.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldFilterAuthors.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldFilterTitles.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldFilterCluster.getInputMap().put(ks, "EnterKeyPressed");
		jTextFieldEntryNumber.getInputMap().put(ks, "EnterKeyPressed");
		jTreeLuhmann.getInputMap().put(ks, "EnterKeyPressed");
		jListEntryKeywords.getInputMap().put(ks, "EnterKeyPressed");

		// Delete and backspace action.
		AbstractAction a_delete = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == jTreeLuhmann) {
					deleteLuhmannFromEntry();
				} else if (e.getSource() == jListEntryKeywords) {
					deleteKeywordFromEntry();
				} else if (e.getSource() == jTableAuthors) {
					deleteAuthor();
				} else if (e.getSource() == jTableKeywords) {
					deleteKeyword();
				} else if (e.getSource() == jTableTitles) {
					deleteEntry();
				} else if (e.getSource() == jTableBookmarks) {
					deleteBookmark();
				} else if (e.getSource() == jTableManLinks) {
					deleteManualLink();
				} else if (e.getSource() == jTableAttachments) {
					deleteAttachment();
				}
			}
		};
		jTreeLuhmann.getActionMap().put("DeleteKeyPressed", a_delete);
		jListEntryKeywords.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableAuthors.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableManLinks.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableKeywords.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableTitles.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableBookmarks.getActionMap().put("DeleteKeyPressed", a_delete);
		jTableAttachments.getActionMap().put("DeleteKeyPressed", a_delete);

		ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "BACK_SPACE" : "DELETE");
		jTreeLuhmann.getInputMap().put(ks, "DeleteKeyPressed");
		jListEntryKeywords.getInputMap().put(ks, "DeleteKeyPressed");
		jTableAuthors.getInputMap().put(ks, "DeleteKeyPressed");
		jTableManLinks.getInputMap().put(ks, "DeleteKeyPressed");
		jTableKeywords.getInputMap().put(ks, "DeleteKeyPressed");
		jTableTitles.getInputMap().put(ks, "DeleteKeyPressed");
		jTableBookmarks.getInputMap().put(ks, "DeleteKeyPressed");
		jTableAttachments.getInputMap().put(ks, "DeleteKeyPressed");

		// create action which should be executed when the user presses
		// the ctrl-F10/meta-F10-key
		AbstractAction a_add = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTableAuthors == e.getSource() || jTextFieldFilterAuthors == e.getSource()) {
					addAuthorToList();
				} else if (jTableKeywords == e.getSource() || jTextFieldFilterKeywords == e.getSource()) {
					addKeywordToList();
				} else if (jTableManLinks == e.getSource()) {
					manualInsertLinks();
				}
			}
		};
		// put action to the tables' actionmaps
		jTableAuthors.getActionMap().put("AddKeyPressed", a_add);
		jTableManLinks.getActionMap().put("AddKeyPressed", a_add);
		jTableKeywords.getActionMap().put("AddKeyPressed", a_add);
		jTextFieldFilterAuthors.getActionMap().put("AddKeyPressed", a_add);
		jTextFieldFilterKeywords.getActionMap().put("AddKeyPressed", a_add);
		// check for os, and use appropriate controlKey
		ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "meta F10" : "ctrl F10");
		jTableAuthors.getInputMap().put(ks, "AddKeyPressed");
		jTableManLinks.getInputMap().put(ks, "AddKeyPressed");
		jTableKeywords.getInputMap().put(ks, "AddKeyPressed");
		jTextFieldFilterAuthors.getInputMap().put(ks, "AddKeyPressed");
		jTextFieldFilterKeywords.getInputMap().put(ks, "AddKeyPressed");
		// create action which should be executed when the user presses
		// the F2/meta-enter-key
		AbstractAction a_edit = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTableAuthors == e.getSource() || jTextFieldFilterAuthors == e.getSource()) {
					editAuthor();
				} else if (jTableKeywords == e.getSource() || jTextFieldFilterKeywords == e.getSource()) {
					editKeyword();
				} else if (jTableTitles == e.getSource() || jTextFieldFilterTitles == e.getSource()) {
					editTitle();
				} else if (jTableBookmarks == e.getSource()) {
					editBookmark();
				} else if (jTableAttachments == e.getSource() || jTextFieldFilterAttachments == e.getSource()) {
					editAttachment();
				}
			}
		};
		// put action to the tables' actionmaps
		jTableAuthors.getActionMap().put("EditKeyPressed", a_edit);
		jTableKeywords.getActionMap().put("EditKeyPressed", a_edit);
		jTableTitles.getActionMap().put("EditKeyPressed", a_edit);
		jTableBookmarks.getActionMap().put("EditKeyPressed", a_edit);
		jTableAttachments.getActionMap().put("EditKeyPressed", a_edit);
		jTextFieldFilterAuthors.getActionMap().put("EditKeyPressed", a_edit);
		jTextFieldFilterKeywords.getActionMap().put("EditKeyPressed", a_edit);
		jTextFieldFilterTitles.getActionMap().put("EditKeyPressed", a_edit);
		jTextFieldFilterAttachments.getActionMap().put("EditKeyPressed", a_edit);
		// check for os, and use appropriate controlKey
		ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "meta ENTER" : "F2");
		jTableAuthors.getInputMap().put(ks, "EditKeyPressed");
		jTableKeywords.getInputMap().put(ks, "EditKeyPressed");
		jTableTitles.getInputMap().put(ks, "EditKeyPressed");
		jTableBookmarks.getInputMap().put(ks, "EditKeyPressed");
		jTableAttachments.getInputMap().put(ks, "EditKeyPressed");
		jTextFieldFilterAuthors.getInputMap().put(ks, "EditKeyPressed");
		jTextFieldFilterKeywords.getInputMap().put(ks, "EditKeyPressed");
		jTextFieldFilterTitles.getInputMap().put(ks, "EditKeyPressed");
		jTextFieldFilterAttachments.getInputMap().put(ks, "EditKeyPressed");
		// create action which should be executed when the user presses
		// the insert/Meta-Backspace-key
		AbstractAction a_new = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTableAuthors == e.getSource()) {
					newAuthor();
				} else if (jTableKeywords == e.getSource()) {
					newKeyword();
				}
			}
		};
		// put action to the tables' actionmaps
		jTableAuthors.getActionMap().put("NewKeyPressed", a_new);
		jTableKeywords.getActionMap().put("NewKeyPressed", a_new);
		// check for os, and use appropriate controlKey
		ks = KeyStroke.getKeyStroke((PlatformUtil.isMacOS()) ? "meta BACK_SPACE" : "INSERT");
		jTableAuthors.getInputMap().put(ks, "NewKeyPressed");
		jTableKeywords.getInputMap().put(ks, "NewKeyPressed");
		// create action which should be executed when the user presses
		// the insert/Meta-Backspace-key
		AbstractAction a_find = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchLogOr();
			}
		};
		// put action to the tables' actionmaps
		jTextFieldFilterKeywords.getActionMap().put("FindKeyPressed", a_find);
		jTextFieldFilterAuthors.getActionMap().put("FindKeyPressed", a_find);
		// check for os, and use appropriate controlKey
		ks = KeyStroke.getKeyStroke("shift ENTER");
		jTextFieldFilterKeywords.getInputMap().put(ks, "FindKeyPressed");
		jTextFieldFilterAuthors.getInputMap().put(ks, "FindKeyPressed");
		// create action which should be executed when the user presses
		// the insert/Meta-Backspace-key
		AbstractAction a_findregex = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTextFieldFilterKeywords == e.getSource()) {
					filterKeywordList(true);
				} else if (jTextFieldFilterAttachments == e.getSource()) {
					filterAttachmentList(true);
				} else if (jTextFieldFilterAuthors == e.getSource()) {
					filterAuthorList(true);
				} else if (jTextFieldFilterTitles == e.getSource()) {
					filterTitleList(true);
				}
			}
		};
		// put action to the tables' actionmaps
		jTextFieldFilterKeywords.getActionMap().put("FindRegExKeyPressed", a_findregex);
		jTextFieldFilterAuthors.getActionMap().put("FindRegExKeyPressed", a_findregex);
		jTextFieldFilterTitles.getActionMap().put("FindRegExKeyPressed", a_findregex);
		jTextFieldFilterAttachments.getActionMap().put("FindRegExKeyPressed", a_findregex);
		// check for os, and use appropriate controlKey
		ks = KeyStroke.getKeyStroke("alt ENTER");
		jTextFieldFilterKeywords.getInputMap().put(ks, "FindRegExKeyPressed");
		jTextFieldFilterAuthors.getInputMap().put(ks, "FindRegExKeyPressed");
		jTextFieldFilterTitles.getInputMap().put(ks, "FindRegExKeyPressed");
		jTextFieldFilterAttachments.getInputMap().put(ks, "FindRegExKeyPressed");
		// </editor-fold>
	}

	/**
	 * Method to init the jTrees components. Removes all elements, sets the root and
	 * the selection-mode
	 */
	private void initTrees() {
		// in case we have mac os x with aqua look&feel, make JTrees look
		// mac-like
		if (settings.isMacStyle()) {
			// This tree has a root, so use "true" as parameter
			jTreeLuhmann.setUI(new MacSourceTree(true));
			// This tree has no root, so use "false" as parameter
			jTreeCluster.setUI(new MacSourceTree(false));
			// This tree has no root, so use "false" as parameter
			jTreeKeywords.setUI(new MacSourceTree(false));
		} // on all other os / look&feels JTrees remain normal.
		else {
			// create array with all jTrees of mainframe
			JTree[] trees = new JTree[] { jTreeLuhmann, jTreeCluster, jTreeKeywords };
			// and iterate that arrea
			for (JTree tree : trees) {
				// remove icons from jTree
				DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
				// Remove the icons
				renderer.setLeafIcon(null);
				renderer.setClosedIcon(null);
				renderer.setOpenIcon(null);
				// set tree to single-selection-mode
				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			}
		}
	}

	/**
	 * Clears the trees and the related jList on the tab with the tree.
	 */
	private void clearTabbedPaneModels() {
		DefaultTreeModel dtrm = (DefaultTreeModel) jTreeLuhmann.getModel();
		dtrm.setRoot(null);

		dtrm = (DefaultTreeModel) jTreeCluster.getModel();
		dtrm.setRoot(null);

		dtrm = (DefaultTreeModel) jTreeKeywords.getModel();
		dtrm.setRoot(null);

		clusterList.clear();
		isFollowerList.clear();

		DefaultTableModel dtm = (DefaultTableModel) jTableLinks.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableManLinks.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableKeywords.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableAuthors.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableTitles.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableBookmarks.getModel();
		dtm.setRowCount(0);

		dtm = (DefaultTableModel) jTableAttachments.getModel();
		dtm.setRowCount(0);
	}

	/**
	 * This method is called at startup, or when a new data file is loaded or
	 * created. used to reset all variable states that need to be resettet at the
	 * beginning.
	 */
	private void initVariables() {
		linkedkeywordlist = null;
		linkedauthorlist = null;
		linkedtitlelist = null;
		linkedattachmentlist = null;
		linkedclusterlist = false;

		clearTabbedPaneModels();

		displayedZettel = -1;
		// hide panels for live-search and is-follower-numbers
		jPanelLiveSearch.setVisible(false);
		/* jPanelManLinks.setVisible(false); */
	}

	/**
	 * Set the default look and feel before drawing the components.
	 * If the user has changed the default look and feel
	 * and we need to set something other than the default.
	 */
	public void initUIManagerLookAndFeel() {
		SwingUtilities.invokeLater(() -> {
			String laf = settings.getLookAndFeel();
			if (laf == null) {
				// Set a default LookAndFeel if laf is null
				laf = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
				// Handle the exception gracefully, e.g., log the error
				System.err.println("Failed to set LookAndFeel: " + e.getMessage());
				return;
			}

			// Update the UI after changing the look and feel (not necessary)
			// This line can be removed as UIManager.setLookAndFeel(laf) already updates the UI
			// JFrame frame = getFrame();
			// SwingUtilities.updateComponentTreeUI(frame);
		});
	}

	public final void setNewDisplayedEntryAndUpdateDisplay(int inputDisplayedEntry) {
		setNewDisplayedEntryAndUpdateDisplay(inputDisplayedEntry, UpdateDisplayOptions.defaultOptions());
	}

	public final void setNewDisplayedEntryAndUpdateDisplay(int inputDisplayedEntry, UpdateDisplayOptions options) {
		displayedZettel = inputDisplayedEntry;

		// We never update the links tab as it is based on the activated entry, which is
		// not changing.
		UpdateDisplayOptions overwrittenOptions = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder(options)
				.updateLinksTab(false).build();
		updateDisplay(overwrittenOptions);
	}

	/**
	 * This methods updates all the display.
	 */
	@Action
	public final void updateDisplay() {
		updateDisplay(UpdateDisplayOptions.defaultOptions());
	}

	public final void updateDisplay(UpdateDisplayOptions options) {
		if (!data.hasZettelID(displayedZettel)) {
			displayedZettel = data.getActivatedEntryNumber();
		}

		updateFrameTitle();
		updateEntryPaneAndKeywordsPane(displayedZettel);
		updateToolbarAndMenu();
		updateTabbedPane(options);
	}

	/**
	 * Switches the logical filtering of the entry's keyword-list
	 * (jListEntryKeywords). If the user selected keywords from the
	 * jListEntryKeywords, the link-list (jTableLinks) is filtered. Here we switch
	 * whether the link-list is filtered in logical-or or logical-and behaviour.
	 * after that, the link-list is being "re-filtered".
	 */
	@Action
	public void keywordListLogOr() {
		// switch checked items
		popupKwListLogOr.setSelected(true);
		popupKwListLogAnd.setSelected(false);
		viewMenuLinksKwListLogOr.setSelected(true);
		viewMenuLinksKwListLogAnd.setSelected(false);
		// save setting
		settings.setLogKeywordlist(Settings.SETTING_LOGKEYWORDLIST_OR);
		// and filter the list again
		filterLinks();
	}

	/**
	 * Switches the logical filtering of the entry's keyword-list
	 * (jListEntryKeywords). If the user selected keywords from the
	 * jListEntryKeywords, the link-list (jTableLinks) is filtered. Here we switch
	 * whether the link-list is filtered in logical-or or logical-and behaviour.
	 * after that, the link-list is being "re-filtered".
	 */
	@Action
	public void keywordListLogAnd() {
		// switch checked items
		popupKwListLogOr.setSelected(false);
		popupKwListLogAnd.setSelected(true);
		viewMenuLinksKwListLogOr.setSelected(false);
		viewMenuLinksKwListLogAnd.setSelected(true);
		// save setting
		settings.setLogKeywordlist(Settings.SETTING_LOGKEYWORDLIST_AND);
		// and filter the list again
		filterLinks();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to first level.
	 */
	@Action
	public void setLuhmannLevel1() {
		settings.setLuhmannExpandLevel(1);
		updateDisplay();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to second level.
	 */
	@Action
	public void setLuhmannLevel2() {
		settings.setLuhmannExpandLevel(2);
		updateDisplay();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to third level.
	 */
	@Action
	public void setLuhmannLevel3() {
		settings.setLuhmannExpandLevel(3);
		updateDisplay();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to fourth level.
	 */
	@Action
	public void setLuhmannLevel4() {
		settings.setLuhmannExpandLevel(4);
		updateDisplay();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to fifth level.
	 */
	@Action
	public void setLuhmannLevel5() {
		settings.setLuhmannExpandLevel(5);
		updateDisplay();
	}

	/**
	 * Sets the default expand level of jLuhmannTree to all level.
	 */
	@Action
	public void setLuhmannLevelAll() {
		settings.setLuhmannExpandLevel(-1);
		updateDisplay();
	}

	/**
	 * Check if a given menu has already been added to the menu bar.
	 * Avoid multiple occurrences of the same menus
	 * that refer to the JTabbedPane.
	 *
	 * @param menu the menu that should be checked for existence
	 * @return {@code true} if menu is already visible in the menu bar,
	 *         {@code false} otherwise.
	 */
	private boolean menuBarHasMenu(JMenu menu) {
		boolean result = false;
		// iterate all menu items
		for (int cnt = 0; cnt < menuBar.getMenuCount(); cnt++) {
			// check whether requested menu is already added
			// if yes, return true
			if (menuBar.getMenu(cnt) == menu) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * This method displays the menu that is related to the currently displayed tab
	 * of the JTabbedPane.<br>
	 * <br>
	 * To avoid UI-bugs (which may occur in Snow Leopard 10.6), but also to avoid
	 * multiple menu-handling for different operating systems, we now <b>remove</b>
	 * <i>all</i> menus that are related to tabs of the JTabbedPane (formerly, they
	 * were only set visible(true/false)). Thus, we have to check whether the to be
	 * displayed menu is already "visible" (i.e. it already has been added to the
	 * menu bar), so we don't have the same menu multiple times displayed in the
	 * menu bar. This check is achived with the method
	 * {@link #menuBarHasMenu(JMenu) menuBarHasMenu(javax.swing.JMenu)}.
	 * <br>
	 * <br>
	 * If the menu has not been added yet, it will be added then. And the menu bar
	 * is being validated and repainted.
	 *
	 * @param menu the menu that is related to the currently displayed tab in the
	 *             JTabbedPane.
	 */
	private void showTabMenu(JMenu menu) {
		// check whether the menu already is visible (added)
		if (!menuBarHasMenu(menu)) {
			// if not, add it now
			menuBar.add(menu);
			// and validate and repaint menu bar
			// (we have to do this to avoid graphical bugs, for instance the menu text
			// will not be properly repainted when we do not call the repaint-method)
			menuBar.validate();
			menuBar.repaint();
		}
	}

	private void initRecentDocumentsMenuItems() {
		setRecentDocumentMenuItem(recentDoc1, 1);
		setRecentDocumentMenuItem(recentDoc2, 2);
		setRecentDocumentMenuItem(recentDoc3, 3);
		setRecentDocumentMenuItem(recentDoc4, 4);
		setRecentDocumentMenuItem(recentDoc5, 5);
		setRecentDocumentMenuItem(recentDoc6, 6);
		setRecentDocumentMenuItem(recentDoc7, 7);
		setRecentDocumentMenuItem(recentDoc8, 8);
	}

	private void setRecentDocumentMenuItem(JMenuItem menuItem, int recentDocNr) {
		menuItem.setVisible(false);
		String recDoc = settings.getRecentDoc(recentDocNr);
		if (recDoc != null && !recDoc.isEmpty()) {
			menuItem.setVisible(true);
			menuItem.setText(FileOperationsUtil.getFileName(recDoc));
			updateRecentDocumentMenuIcon(menuItem, recDoc);
		}
	}

	private void updateRecentDocumentMenuIcon(JMenuItem menuItem, String recDoc) {
		File dummyfile = new File(recDoc);
		if (dummyfile.exists()) {
			ImageIcon zkn3Icon = Constants.zknicon;
			menuItem.setToolTipText(recDoc);
			menuItem.setIcon(zkn3Icon);
		} else {
			ImageIcon fileNotFoundIcon = Constants.errorIcon;
			String fileNotFountText = getResourceMap().getString("recDocfileNotFoundTxt");
			menuItem.setToolTipText(fileNotFountText);
			menuItem.setIcon(fileNotFoundIcon);
		}
	}

	/**
	 * This method removes all menus related to a specific tab. Usually called to
	 * reset before updating the display.
	 */
	private void removeTabMenus() {
		menuBar.remove(viewMenuLinks);
		menuBar.remove(viewMenuLuhmann);
		menuBar.remove(viewMenuKeywords);
		menuBar.remove(viewMenuAuthors);
		menuBar.remove(viewMenuTitles);
		menuBar.remove(viewMenuCluster);
		menuBar.remove(viewMenuBookmarks);
		menuBar.remove(viewMenuAttachments);
		menuBar.validate();
	}

	/**
	 * Here we set up the jTabbedPane according to the page to be displayed the
	 * keyword list e.g. is only to be displayed when selected - this is done within
	 * the changelistener of the jTabbedPane. The connections of each entry to other
	 * entries e.g. has to be updated each time
	 */
	private void updateTabbedPane(UpdateDisplayOptions options) {
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			clearTabbedPaneModels();
		}

		// Enable refresh-button if we have a linked list.
		jButtonRefreshKeywords.setEnabled(linkedkeywordlist != null);
		jButtonRefreshTitles.setEnabled(linkedtitlelist != null);
		jButtonRefreshAuthors.setEnabled(linkedauthorlist != null);
		jButtonRefreshCluster.setEnabled(linkedclusterlist);
		jButtonRefreshAttachments.setEnabled(linkedattachmentlist != null);

		// Enable filter text-field if we have more than 1 element in the associated
		// jTable.
		jTextFieldFilterKeywords.setEnabled(jTableKeywords.getRowCount() > 0);
		jTextFieldFilterAuthors.setEnabled(jTableAuthors.getRowCount() > 0);
		jTextFieldFilterTitles.setEnabled(jTableTitles.getRowCount() > 0);
		jTextFieldFilterCluster.setEnabled(jTreeCluster.getRowCount() > 0);
		jTextFieldFilterAttachments.setEnabled(jTableAttachments.getRowCount() > 0);

		// Remove all tab menus. Code below will show the correct tab menus.
		removeTabMenus();

		// Reset status text.
		statusMsgLabel.setText("");

		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			// Do nothing when we have zero notes.
			return;
		}

		int selectedTab = jTabbedPaneMain.getSelectedIndex();

		// We always need an update of the links.
		needsLinkUpdate = true;
		// When the previous tab was the links-tab, stop the background-task.
		if ((TAB_LINKS == previousSelectedTab || TAB_LINKS != selectedTab) && (cLinksTask != null)
				&& !cLinksTask.isDone()) {
			cLinksTask.cancel(true);
		}

		previousSelectedTab = selectedTab;

		switch (selectedTab) {
		case TAB_LINKS:
			if (options.shouldUpdateLinksTable()) {
				updateLinksTab();
			}
			break;
		case TAB_LUHMANN:
			updateNoteSequencesTab(options);
			break;
		case TAB_KEYWORDS:
			showKeywords();
			break;
		case TAB_AUTHORS:
			showAuthors();
			break;
		case TAB_TITLES:
			if (options.shouldUpdateTitlesTab()) {
				updateTitlesTab();
			}
			break;
		case TAB_CLUSTER:
			showCluster();
			break;
		case TAB_BOOKMARKS:
			showBookmarks();
			break;
		case TAB_ATTACHMENTS:
			showAttachments();
			break;
		default:
			if (options.shouldUpdateLinksTable()) {
				updateLinksTab();
			}
			break;
		}
	}

	/**
	 * This method is called from within the "updateDisplay" method. This method
	 * enables or disables the toolbar icons and menubars, dependent on whether
	 * their function is available or not. This depends on the amount of entries in
	 * the program.
	 */
	public void updateToolbarAndMenu() {
		// store the amount of entries
		int count = data.getCount(Daten.ZKNCOUNT);
		// at least one entry necessary to enable following functions
		setEntriesAvailable(count > 0);
		// more than one entry necessary to enable following functions
		setMoreEntriesAvailable(count > 1);
		// check each selected entries for followers
		setMoreLuhmann(data.hasLuhmannNumbers(data.getActivatedEntryNumber()));
		// check whether the current entry is bookmarked or not...
		setEntryBookmarked((-1 == bookmarks.getBookmarkPosition(displayedZettel)) && (count > 0));
		// check whether current entry is on any desktop or not
		statusDesktopEntryButton.setVisible(desktop.isEntryInAnyDesktop(displayedZettel) && (count > 0));
		statusDesktopEntryButton.setEnabled(desktop.isEntryInAnyDesktop(displayedZettel) && (count > 0));
		// retrieve modified data-files
		setSaveEnabled(settings.getSynonyms().isModified() | data.isMetaModified() | bibtex.isModified()
				| data.isModified() | bookmarks.isModified() | searchRequests.isModified() | desktop.isModified());
		buttonHistoryBack.setEnabled(data.canHistoryBack());
		buttonHistoryFore.setEnabled(data.canHistoryFore());
		setHistoryBackAvailable(data.canHistoryBack());
		setHistoryForAvailable(data.canHistoryFore());
		// desktop and search results avaiable
		setDesktopAvailable(desktop.getCount() > 0);
		setSearchResultsAvailable(searchRequests.getCount() > 0);
		showSearchResultsMenuItem.setEnabled(searchRequests.getCount() > 0);
		showDesktopMenuItem.setEnabled(desktop.getCount() > 0);
	}

	/**
	 * This method displays the information of an entry that is selected from the
	 * tree jTreeLuhmann, i.e. displaying a follower- or sub-entry of the current
	 * entry. we use an extra display-method here because we don't want to "lose"
	 * the current entry and we don't want updating the tabbed pane with the jTree
	 * when the user selects an entry from that tree.
	 */
	private void handleNoteSequenceValueChange() {
		// If no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}

		int selectedEntry = selectedEntryInJTreeLuhmann();
		// If we don't have a valid selection, do nothing.
		if (selectedEntry == -1) {
			return;
		}
		if (selectedEntry != displayedZettel) {
			// JTree already handles changing the selected tree node, so no need to rebuild
			// it.
			UpdateDisplayOptions options = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder()
					.updateNoteSequencesTab(false).build();
			setNewDisplayedEntryAndUpdateDisplay(selectedEntry, options);
		}
	}

	private void updateHighlightingTerms(int inputDisplayedEntry) {
		if (settings.getHighlightKeywords()) {
			String[] highlightterms = data.getSeparatedKeywords(inputDisplayedEntry);
			// create new linked list that will contain all highlight-terms, including
			// the related synonyms of the highlight-terms
			LinkedList<String> highlight = new LinkedList<>();
			// check whether we have any keywords to highlight
			if (highlightterms != null && highlightterms.length > 0) {
				highlight.addAll(Arrays.asList(highlightterms));
				// check whether synonyms should be included as well
				if (settings.getSearchAlwaysSynonyms()) {
					// iterate all current highlight keywords
					// and add synonyms
					for (String kw : highlightterms) {
						// get the synonym-line for each search term
						String[] synline = settings.getSynonyms().getSynonymLineFromAny(kw, false);
						// if we have synonyms...
						if (synline != null) {
							// iterate synonyms
							for (String sy : synline) {
								// add them to the linked list, if they are new
								if (!highlight.contains(sy)) {
									highlight.add(sy);
								}
							}
						}
					}
				}
				HtmlUbbUtil.setHighlighTerms(highlight.toArray(new String[highlight.size()]),
						HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS, settings.getHighlightWholeWord());
			} else {
				HtmlUbbUtil.setHighlighTerms(null, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS,
						settings.getHighlightWholeWord());
			}
		} else {
			HtmlUbbUtil.setHighlighTerms(null, HtmlUbbUtil.HIGHLIGHT_STYLE_KEYWORDS, settings.getHighlightWholeWord());
		}
	}

	private void updateKeywordsPane(int inputDisplayedEntry) {
		// Here we set up the keyword list for the JList
		// retrieve the current keywords
		String[] dataKeywords = data.getKeywords(inputDisplayedEntry);

		// prepare the JList which will display the keywords
		keywordListModel.clear();

		// check whether any keywords have been found
		if (dataKeywords != null) {
			// sort the array
			if (dataKeywords.length > 0) {
				Arrays.sort(dataKeywords, new Comparer());
			}
			// iterate the dataKeywords string array and add its content to the list model
			for (String kw : dataKeywords) {
				keywordListModel.addElement(kw);
			}
		}
		// create new string builder for border-text. we set the amount of keywords
		// as new border-title
		StringBuilder bordertext = new StringBuilder("");
		// get localised description
		bordertext.append(getResourceMap().getString("jListEntryKeywords.border.title"));
		// if we have any keywords...
		// copy amount of keywords behind description
		if (!keywordListModel.isEmpty()) {
			bordertext.append(" (").append(String.valueOf(keywordListModel.size())).append(")");
		}
		// set new border text
		Color bcol = (settings.isMacStyle()) ? ColorUtil.colorJTreeText : null;
		jListEntryKeywords.setBorder(ZknMacWidgetFactory.getTitledBorder(bordertext.toString(), bcol, settings));
	}

	/**
	 * This method is used to update the content of the textfields/lists, but not
	 * the whole display like tabbed pane as well. Usually this method is called
	 * when a link to another entry or a follower entry or any entry in one of the
	 * tabbed pane's tables is selected. This selection does just show the content
	 * of the related entry, which means the jEditorPanes and the jListKeywords are
	 * filled with the entry's data. but: the data in the tabbed pane, like the
	 * entry's follower-numbers, links and manual links etc. are <i>not</i> updated.
	 * this only occurs, when an entry is <i>activated</i>. e.g. by double-clicking
	 * on an entry in on of the tabbed pane's tables.
	 *
	 * @param inputDisplayedEntry the number of the entry that should be displayed
	 */
	private void updateEntryPaneAndKeywordsPane(int inputDisplayedEntry) {
		// If we have an invalid entry, reset panes.
		if (data.getCount(Daten.ZKNCOUNT) == 0 || inputDisplayedEntry == 0) {
			jEditorPaneEntry.setText("");

			Color bcol = (settings.isMacStyle()) ? ColorUtil.colorJTreeText : null;
			jListEntryKeywords.setBorder(ZknMacWidgetFactory
					.getTitledBorder(getResourceMap().getString("jListEntryKeywords.border.title"), bcol, settings));
			keywordListModel.clear();

			jTextFieldEntryNumber.setText("");
			statusOfEntryLabel.setText(getResourceMap().getString("entryOfText"));
			return;
		}

		// If the user wants to add all displayed entries to the history, include new
		// displayed entry to the history.
		if (settings.getAddAllToHistory()) {
			data.addToHistory(inputDisplayedEntry);
			// Update buttons for navigating through history.
			buttonHistoryBack.setEnabled(data.canHistoryBack());
			buttonHistoryFore.setEnabled(data.canHistoryFore());
		}

		updateHighlightingTerms(inputDisplayedEntry);
		updateSelectedEntryPane(inputDisplayedEntry);
		updateKeywordsPane(inputDisplayedEntry);

		statusOfEntryLabel.setText(
				getResourceMap().getString("entryOfText") + " " + String.valueOf(data.getCount(Daten.ZKNCOUNT)));
	}

	public void updateSelectedEntryPane(int inputDisplayedEntry) {
		String dataEntryAsHtml = data.getEntryAsHtml(inputDisplayedEntry,
				(settings.getHighlightSegments()) ? retrieveSelectedKeywordsFromList() : null, Constants.FRAME_MAIN);

		if (Tools.isValidHTML(dataEntryAsHtml, inputDisplayedEntry)) {
			jEditorPaneEntry.setText(dataEntryAsHtml);
		} else {
			StringBuilder cleanedContent = new StringBuilder("");
			cleanedContent
					.append("<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">");
			URL imgURL = Application
					.getInstance(ZettelkastenApp.class).getClass()
					.getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
			cleanedContent.append("<img border=\"0\" src=\"").append(imgURL).append("\">&#8195;");
			cleanedContent.append(getResourceMap().getString("incorrectNestedTagsText"));
			cleanedContent.append("</div>").append(data.getCleanZettelContent(inputDisplayedEntry)).append("</body>");
			// display clean content instead
			jEditorPaneEntry.setText(cleanedContent.toString());
		}

		// place caret, so content scrolls to top
		jEditorPaneEntry.setCaretPosition(0);

		// Update entryNumberJTextField with activated entry.
		jTextFieldEntryNumber.setText(String.valueOf(data.getActivatedEntryNumber()));
	}

	/**
	 * Action that deletes a selected subentry in the jTreeLuhmann from its parent.
	 */
	@Action(enabledProperty = "luhmannSelected")
	public void deleteLuhmannFromEntry() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeLuhmann.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
		if (parent == null) {
			return;
		}

		// Ask confirmation whether the link to the sub-entry should be deleted.
		int option = JOptionPane.showConfirmDialog(getFrame(), getResourceMap().getString("askForDeleteLuhmannMsg"),
				getResourceMap().getString("askForDeleteLuhmannTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		// Delete if Yes.
		if (option == JOptionPane.YES_OPTION) {
			EntryID selectedEntry = TreeUtil.getEntryID(selectedNode);
			EntryID parentEntry = TreeUtil.getEntryID(parent);
			data.deleteLuhmannNumber(parentEntry, selectedEntry);

			// Reset displayedZettel and updateDisplay.
			displayedZettel = -1;
			updateDisplay();
		}
	}

	/**
	 * This methods retrieves the number of a selected entry from the jTreeLuhmann
	 *
	 * @return The number of the selected entry, or -1 if an error occured
	 */
	private int selectedEntryInJTreeLuhmann() {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeLuhmann.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return -1;
		}
		return TreeUtil.getEntryID(selectedNode).asInt();
	}

	@Action
	public void showLuhmannEntryNumber() {
		boolean val = settings.getShowLuhmannEntryNumber();
		settings.setShowLuhmannEntryNumber(!val);
		updateDisplay();
	}

	/**
	 * This action shows the tab with the links to other entries, based on matching
	 * keywords. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowLinks() {
		jTabbedPaneMain.setSelectedIndex(TAB_LINKS);
	}

	/**
	 * This action shows the tab with the attachment of all entries. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowAttachments() {
		jTabbedPaneMain.setSelectedIndex(TAB_ATTACHMENTS);
	}

	/**
	 * This method updates the Note Sequences tab. Does nothing if data is
	 * unavailable.
	 * 
	 * The Note Sequences tab is divided into the following: a JTree pane, a Parents
	 * notes pane, and a Note Sequences menu. Each are updated here if appropriate.
	 */
	private synchronized void updateNoteSequencesTab(UpdateDisplayOptions options) {
		// If no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}

		if (options.shouldUpdateNoteSequencesTree()) {
			prepareNoteSequencesJTreePane();
		}
		prepareNoteSequencesParentsPane();

		showTabMenu(viewMenuLuhmann);
	}

	/**
	 * This method restores a filtered link-list. The list jTableLinks is filtered,
	 * when a user selects an item from the jListEntryKeywords. To restore the list,
	 * we use this method.<br>
	 * <br>
	 * When a filtered link-list (jTableLinks), which is filtered by selected
	 * keywords from the keyword-list (jListEntryKeywords), we can reset the
	 * link-list, showing not just the related keywords from the selected values,
	 * but all keywords again.
	 */
	@Action
	public void refreshFilteredLinks() {
		needsLinkUpdate = true;
		updateLinksTab();
	}

	/**
	 * This method updates the Links tab.
	 */
	private synchronized void updateLinksTab() {
		// If the link-table is not shown, nothing to do.
		if (jTabbedPaneMain.getSelectedIndex() != TAB_LINKS) {
			return;
		}

		// If no data available, nothing to show.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}

		// When no update needed, show menu.
		if (!needsLinkUpdate) {
			// Update might be needed next time.
			needsLinkUpdate = true;
			// Show view menu if we have at least one entry.
			if (jTableLinks.getRowCount() > 0) {
				showTabMenu(viewMenuLinks);
			}

			updateManualLinksTable();

			return;
		}

		if (createLinksIsRunning) {
			// Do nothing if task is already running.
			return;
		}

		jListEntryKeywords.clearSelection();

		// Clear table with links.
		DefaultTableModel tm = (DefaultTableModel) jTableLinks.getModel();
		tm.setRowCount(0);

		// Clear table with manual links.
		tm = (DefaultTableModel) jTableManLinks.getModel();
		tm.setRowCount(0);

		// Update status message.
		statusMsgLabel.setText(getResourceMap().getString("createLinksMsg"));
		prepareAndStartTask(createLinks());
	}

	private void updateManualLinksTable() {
		// get table model for manual links
		DefaultTableModel tm = (DefaultTableModel) jTableManLinks.getModel();
		// reset the table
		tm.setRowCount(0);
		// get the current manual links
		int[] manlinks;
		manlinks = data.getManualLinksOfActivatedEntry();
		// if we have any manual links, fille the table and display the panel
		if ((manlinks != null) && (manlinks.length > 0)) {
			for (int cnt = 0; cnt < manlinks.length; cnt++) {
				// create a new object
				Object[] ob = new Object[3];
				// store the information in that object
				ob[0] = manlinks[cnt];
				ob[1] = data.getZettelTitle(manlinks[cnt]);
				ob[2] = data.getZettelRating(manlinks[cnt]);
				// add new table row
				tm.addRow(ob);
			}
		}
		// display panel
		/* jPanelManLinks.setVisible(jTableManLinks.getRowCount()>0); */
	}

	/**
	 * This action shows the tab with the keywords of the current data file. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowKeywords() {
		jTabbedPaneMain.setSelectedIndex(TAB_KEYWORDS);
	}

	/**
	 * This method displays the all keywords in the keyword data file using a
	 * background task. after the task finishes, all keywords and their useage
	 * frequency in the main data file (zknfile) are displayed in the JTable of the
	 * JTabbedPane
	 */
	private void showKeywords() {
		// if no data available, do nothing
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableKeywords.getRowCount()) + " "
				+ getResourceMap().getString("statusTextKeywords") + ")");
		// show/enabke related menu
		showTabMenu(viewMenuKeywords);
		// if keywordlist is up to date, do nothing
		if (data.isKeywordlistUpToDate()) {
			return;
		}
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SHOWKEYWORDS, data,
					settings.getSynonyms(), null, /* only needed for authors */
					null, /* only needed for attachments */
					settings.getShowSynonymsInTable(), 0, /* only need for authors */
					(DefaultTableModel) jTableKeywords.getModel());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// check whether we have a new added keyword, and if so, select it
		if (newAddedKeyword != null) {
			// select recently added value
			de.danielluedecke.zettelkasten.util.TableUtils.selectValueInTable(jTableKeywords, newAddedKeyword, 0);
			// and clear strimg
			newAddedKeyword = null;
		}
		// enable textfield only if we have more than 1 element in the jtable
		jTextFieldFilterKeywords.setEnabled(jTableKeywords.getRowCount() > 0);
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableKeywords.getRowCount()) + " "
				+ getResourceMap().getString("statusTextKeywords") + ")");
	}

	public void updateZettelkasten(String updateBuildNr) {
		// If dialog window isn't already created, do this now.
		if (null == updateInfoDlg) {
			// get parent und init window
			updateInfoDlg = new CUpdateInfoBox(getFrame());
			// Center new dialog window.
			updateInfoDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(updateInfoDlg);
		// check whether user wants to hide this msg box
		if (updateInfoDlg.getHideUpdateMsg()) {
			// set update build number to settings, so this update msg will not be displayed
			// for this update number again
			settings.setShowUpdateHintVersion(updateBuildNr);
		}
		// check whether user wants to update
		if (updateInfoDlg.isShowHomepage()) {
			// open homepage
			try {
				Desktop.getDesktop().browse(new URI(updateURI));
			} catch (IOException | URISyntaxException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		updateInfoDlg.dispose();
		updateInfoDlg = null;
	}

	/**
	 * This methoid toggles the highlight-setting for the keywords. When activated,
	 * the keywords of the current displayed entry are highlighted in the entry's
	 * content-text.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void highlightKeywords() {
		// check whether highlighting is activated
		if (!settings.getHighlightKeywords()) {
			// if not, activate it
			settings.setHighlightKeyword(true);
		} else {
			// nex, if highlighting is activated,
			// check whether whole word highlighting is activated
			if (!settings.getHighlightWholeWord()) {
				// if not, activate whole-word-highlighting and do not
				// deactivate general highlighting
				settings.setHighlightWholeWord(true);
			} // else if both were activated, deactivate all
			else {
				settings.setHighlightKeyword(false);
				settings.setHighlightWholeWord(false);
			}
		}
		// no linkupdate needed
		needsLinkUpdate = false;
		// update the display
		updateDisplay();
	}

	@Action(enabledProperty = "entriesAvailable")
	public void highlightSegments() {
		// Toggle highlight setting for keywords
		settings.setHighlightSegments(!settings.getHighlightSegments());
		updateDisplay();
	}

	/**
	 * This method filters the keyword list from a given input of the user. the text
	 * entered into the jTextFieldFilterKeywords is retrieved, and all keywords in
	 * the jTableKeywords, that do <b>not</b> <i>contain</i> the entered text (case
	 * insensitive), are removed from the table.<br>
	 * <br>
	 * Thus, all remaining keywords in the table do start or at least contain the
	 * textinput from the textfield.
	 */
	private void filterKeywordList(boolean forceRegEx) {
		LinkedList<Object[]> l = filterList(jTextFieldFilterKeywords, jTableKeywords, jButtonRefreshKeywords,
				linkedkeywordlist, "statusTextKeywords", 0, forceRegEx);
		if (l != null) {
			linkedkeywordlist = l;
		}
	}

	/**
	 * When a keywordlist is filtered, the original table data is temporarily stored
	 * in a linked list. when the user perfoms the refresh-method, the original
	 * table data is restored by clearing the table's content and setting back all
	 * data from the linked list to the table.
	 */
	@Action
	public void refreshKeywordList() {
		refreshList(jTableKeywords, jButtonRefreshKeywords, jTextFieldFilterKeywords, linkedkeywordlist,
				"statusTextKeywords");
		linkedkeywordlist = null;
	}

	@Action(enabledProperty = "exportPossible")
	public void exportTitles() {
		// retrieve amount of table rows
		int rowcount = jTableTitles.getRowCount();
		// create array for entry-numbers
		int[] entries = new int[rowcount];
		// iterate all table values and copy each enty-number to the array
		for (int cnt = 0; cnt < rowcount; cnt++) {
			entries[cnt] = Integer.parseInt(jTableTitles.getValueAt(cnt, 0).toString());
		}
		// export entries from the jTableTitles in the according order...
		exportEntries(entries);
	}

	@Action(enabledProperty = "exportPossible")
	public void exportLuhmann() {
		// retrieve luhmann numbers
		int[] ent = prepareLuhmannNumbersForExport();
		// check for valid values
		if (ent != null) {
			exportEntries(ent);
		}
	}

	@Action(enabledProperty = "exportPossible")
	public void exportLuhmannToSearch() {
		// retrieve luhmann numbers
		int[] ent = prepareLuhmannNumbersForExport();
		// check for valid values
		if (ent != null) {
			// append a time-string to description, so we always have a unique
			// search-description,
			// even if the user searches twice for the same searchterms
			DateFormat df = new SimpleDateFormat("kkmmss");
			// add search
			searchRequests.addSearch(
					new String[] { getResourceMap().getString("exportLuhmannSearch", String.valueOf(displayedZettel)) },
					Constants.SEARCH_LUHMANN, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false,
					false, ent,
					getResourceMap().getString("exportLuhmannSearchDesc", String.valueOf(displayedZettel)) + " ("
							+ df.format(new Date()) + ")",
					getResourceMap().getString("exportLuhmannSearchDesc", String.valueOf(displayedZettel)));
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg) {
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			}
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		}
	}

	private int[] prepareLuhmannNumbersForExport() {
		// init linked list
		luhmannnumbersforexport = new LinkedList<>();
		// get recursive Luhmann-Numbers of current entry
		fillLuhmannNumbersForExport(displayedZettel);
		// add them...
		if (luhmannnumbersforexport.size() > 0) {
			// add initial entry
			luhmannnumbersforexport.add(0, displayedZettel);
			// convert to int array
			int[] ent = new int[luhmannnumbersforexport.size()];
			try {
				// Than the followers
				for (int cnt = 0; cnt < ent.length; cnt++) {
					ent[cnt] = luhmannnumbersforexport.get(cnt);
				}
			} catch (NumberFormatException e) {
			}
			return ent;
		}
		return null;
	}

	/**
	 * This method recursively retrieves all follower- and sub-follower-numbers
	 * (Luhmann-Numbers) of an entry and adds them to a stringbuilder. This method
	 * is needed for the {@link #addLuhmann addLuhmann}-Action that adds these
	 * follower-numbers to the treeview, directly behind the selected entry.
	 *
	 * @param zettelpos the number of the selected entry
	 */
	private void fillLuhmannNumbersForExport(int zettelpos) {
		// get the text from the luhmann-numbers
		String lnr = data.getSubEntriesCsv(zettelpos);
		// if we have any luhmann-numbers, go on...
		if (!lnr.isEmpty()) {
			String[] lnrs = lnr.split(",");
			// go throughh array of current luhmann-numbers
			for (String exist : lnrs) {
				// convert to int
				int e = Integer.parseInt(exist);
				// copy all values to an array
				if (!luhmannnumbersforexport.contains(e)) {
					luhmannnumbersforexport.add(e);
				}
				// check whether luhmann-value exists, by re-calling this method
				// again and go through a recusrive loop
				fillLuhmannNumbersForExport(e);
			}
		}
	}

	/**
	 * This method exports the automatic links (referrers) of an entry.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportLinks() {
		// create linked list that will contain all attachments
		ArrayList<Object> explinks;
		explinks = new ArrayList<>();
		// first, add original entry number
		explinks.add(String.valueOf(displayedZettel));
		// get length of links-table
		int len = jTableLinks.getRowCount();
		// copy all attachments to a linked list
		if (len > 0) {
			for (int cnt = 0; cnt < len; cnt++) {
				explinks.add(jTableLinks.getValueAt(cnt, 0));
			}
		}
		// get length of manual-links-table
		len = jTableManLinks.getRowCount();
		// if we also have manual links, add them
		if (len > 0) {
			// copy all attachments to a linked list
			for (int cnt = 0; cnt < len; cnt++) {
				explinks.add(jTableManLinks.getValueAt(cnt, 0));
			}
		}
		// call export-method
		exportEntries(explinks);
	}

	/**
	 * This method exports the automatic links (referrers) of an entry, but not to a
	 * file but to the search results window instead.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportLinksToSearch() {
		// create linked list that will contain all attachments
		ArrayList<Object> explinks = new ArrayList<>();
		// retrieve entry-numbers
		// first, add original entry number
		explinks.add(String.valueOf(displayedZettel));
		// get length of links-table
		int len = jTableLinks.getRowCount();
		// copy all attachments to a linked list
		if (len > 0) {
			for (int cnt = 0; cnt < len; cnt++) {
				explinks.add(jTableLinks.getValueAt(cnt, 0));
			}
		}
		// get length of manual-links-table
		len = jTableManLinks.getRowCount();
		// if we also have manual links, add them
		if (len > 0) {
			// copy all attachments to a linked list
			for (int cnt = 0; cnt < len; cnt++) {
				explinks.add(jTableManLinks.getValueAt(cnt, 0));
			}
		}
		// create int-array with all entry-numbers
		int[] expvalues = new int[explinks.size()];
		try {
			// copy all numbers from arraylist to int-array
			for (int cnt = 0; cnt < expvalues.length; cnt++) {
				expvalues[cnt] = Integer.parseInt(explinks.get(cnt).toString());
			}
			// append a time-string to description, so we always have a unique
			// search-description,
			// even if the user searches twice for the same searchterms
			DateFormat df = new SimpleDateFormat("kkmmss");
			// add search
			searchRequests.addSearch(
					new String[] { getResourceMap().getString("exportLinksSearch", String.valueOf(displayedZettel)) },
					Constants.SEARCH_REFERRERS, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false,
					false, expvalues,
					getResourceMap().getString("exportLinksSearchDesc", String.valueOf(displayedZettel)) + " ("
							+ df.format(new Date()) + ")",
					getResourceMap().getString("exportLinksSearchDesc", String.valueOf(displayedZettel)));
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg) {
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			}
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		} catch (NumberFormatException e) {
		}
	}

	public void exportDesktopToSearch(int[] entries, String desktopname) {
		// check for valid search results
		if (entries != null && entries.length > 0) {
			// append a time-string to description, so we always have a unique
			// search-description,
			// even if the user searches twice for the same searchterms
			DateFormat df = new SimpleDateFormat("kkmmss");
			// add search
			searchRequests.addSearch(new String[] { getResourceMap().getString("exportDesktopSearch") },
					Constants.SEARCH_DESKTOP, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false,
					false, entries,
					getResourceMap().getString("exportDesktopSearchDesc",
							"\"" + desktopname + "\"" + " (" + df.format(new Date()) + ")"),
					getResourceMap().getString("exportDesktopSearchDesc", "\"" + desktopname + "\""));
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg) {
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			}
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		}
	}

	public void exportDesktopMissingToSearch(int[] entries, String desktopname) {
		// check for valid search results
		if (entries != null && entries.length > 0) {
			// append a time-string to description, so we always have a unique
			// search-description,
			// even if the user searches twice for the same searchterms
			DateFormat df = new SimpleDateFormat("kkmmss");
			// add search
			searchRequests.addSearch(new String[] { getResourceMap().getString("exportMissingDesktopSearch") },
					Constants.SEARCH_DESKTOP, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false,
					false, entries,
					getResourceMap().getString("exportMissingDesktopSearchDesc",
							"\"" + desktopname + "\"" + " (" + df.format(new Date()) + ")"),
					getResourceMap().getString("exportMissingDesktopSearchDesc", "\"" + desktopname + "\""));
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg) {
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			}
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		}
	}

	/**
	 * This method exports the automatic links (referrers) of an entry.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportCluster() {
		// create linked list that will contain all attachments
		ArrayList<Object> expcluster = new ArrayList<>();
		// get length of cluster-entry-list
		// call export-method
		exportEntries(expcluster);
	}

	/**
	 * This method exports the automatic links (referrers) of an entry, but not to a
	 * file but to the search results window instead.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportClusterToSearch() {
		// create int-array with all entry-numbers
		int[] expvalues = new int[clusterList.size()];
		try {
			// copy all numbers from arraylist to int-array
			for (int cnt = 0; cnt < expvalues.length; cnt++) {
				expvalues[cnt] = Integer.parseInt(clusterList.get(cnt));
			}
			// append a time-string to description, so we always have a unique
			// search-description,
			// even if the user searches twice for the same searchterms
			DateFormat df = new SimpleDateFormat("kkmmss");
			// add search
			searchRequests.addSearch(
					new String[] { getResourceMap().getString("exportClusterSearch", lastClusterRelationKeywords) },
					Constants.SEARCH_CLUSTER, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false,
					false, expvalues,
					getResourceMap().getString("exportClusterSearchDesc", lastClusterRelationKeywords) + " ("
							+ df.format(new Date()) + ")",
					getResourceMap().getString("exportClusterSearchDesc", lastClusterRelationKeywords));
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg) {
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			}
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		} catch (NumberFormatException e) {
		}
	}

	/**
	 * This action opens an input dialog and lets the user input a new
	 * keyword-value. The currently selected keyword is then being changed and the
	 * modified state set. In case the user chose a new keyword which already
	 * exists, the user is being offered to "merge" the old keyword with the other
	 * existing one.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void editKeyword() {
		// get selected keywords
		String[] selectedkeywords = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0);
		// if now row is selected, leave...
		if (null == selectedkeywords) {
			return;
		}
		// get selected rows. we need this numbers for setting back the new values, see
		// below
		int[] selectedrows = jTableKeywords.getSelectedRows();
		// go through all selected keywords
		for (int cnt = selectedkeywords.length - 1; cnt >= 0; cnt--) {
			// save the old value
			String oldKw = selectedkeywords[cnt];
			// now check whether the selected keyword is a keyword, or only a synonym
			// this may happen, when the user includes the synonyms in the keyword-list,
			// so in fact we have no "real" keyword.
			if (data.findKeywordInDatabase(oldKw) != -1) {
				// open an input-dialog, setting the selected value as default-value
				String newKw = (String) JOptionPane.showInputDialog(getFrame(),
						getResourceMap().getString("editKeywordMsg"), getResourceMap().getString("editKeywordTitle"),
						JOptionPane.PLAIN_MESSAGE, null, null, oldKw);
				// ask the user if he wants to replace the new name of keywords, which appear as
				// synonyms, but *not*
				// as index-word, with the related index-words...
				newKw = Tools.replaceSynonymsWithKeywords(settings.getSynonyms(), new String[] { newKw })[0];
				// if we have a valid return-value that differs from the old value...
				if ((newKw != null) && (newKw.length() > 0) && (!oldKw.equalsIgnoreCase(newKw))) {
					// check whether the value already exists
					if (-1 == data.getKeywordPosition(newKw, false)) {
						// change the existing value in the table
						jTableKeywords.setValueAt(newKw, selectedrows[cnt], 0);
						// get the index-number of the old keyword-string
						int nr = data.getKeywordPosition(oldKw, false);
						// and change the entry to the new value
						data.setKeyword(nr, newKw);
						// now we want either to rename synonyms-index-words of the keyword
						// "oldKw", since this keyword has been renamed. or, if the new keyword
						// name "newKw" also has associated synonyms, we want to merge them.
						Tools.mergeSynonyms(settings.getSynonyms(), oldKw, newKw);
						// if we have a filtered list, remove the element also from
						// our refresh-list, so we don't show this item again when the list
						// is being refreshed
						if (linkedkeywordlist != null) {
							linkedkeywordlist = updateLinkedList(linkedkeywordlist, oldKw, newKw, 0);
						}
						updateDisplay();
					} else {
						// The new name for keyword already exists, so we can offer to merge
						// the keywords here. in fact, this is an easy find/replace-routine, since the
						// old keyword is replaced by the existing one, when we merge them.
						//
						// create a JOptionPane with yes/no/cancel options
						int option = JOptionPane.showConfirmDialog(getFrame(),
								getResourceMap().getString("mergeKeywordMsg"),
								getResourceMap().getString("mergeKeywordTitle"), JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE);
						// if no merge is requested, do nothing
						if (JOptionPane.NO_OPTION == option) {
							return;
						}
						// merge the keywords by opening a dialog with a background task
						// If dialog window isn't already created, do this now.
						if (taskDlg == null) {
							// get parent und init window
							taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_MERGEKEYWORDS, data,
									taskinfo, oldKw, newKw, null, jTableKeywords, selectedrows[cnt], linkedkeywordlist);
							// Center new dialog window.
							taskDlg.setLocationRelativeTo(getFrame());
						}
						waitForTaskDialog();

						// update the merged linked list
						linkedkeywordlist = taskinfo.getLinkedValues();
						// now we want either to rename synonyms-index-words of the keyword
						// "oldKw", since this keyword has been renamed. or, if the new keyword
						// name "newKw" also has associated synonyms, we want to merge them.
						Tools.mergeSynonyms(settings.getSynonyms(), oldKw, newKw);
						// show amount of entries
						statusMsgLabel.setText("(" + String.valueOf(jTableKeywords.getRowCount()) + " "
								+ getResourceMap().getString("statusTextKeywords") + ")");
						// finally, update display
						updateDisplay();
					}
				}
			} else {
				// display error message box
				JOptionPane.showMessageDialog(getFrame(),
						getResourceMap().getString("noKeywordSelectedMsg", oldKw,
								settings.getSynonyms().getIndexWord(oldKw, true)),
						getResourceMap().getString("noKeywordSelectedTitle"), JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	/**
	 * This action opens an input dialog and lets the user input a new title-value.
	 * The currently selected title is then being changed and the modified state
	 * set.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void editTitle() {
		// get the selected row
		int row = jTableTitles.getSelectedRow();
		// get entry number
		int entry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableTitles, 0);
		// if now row is selected, leave...
		if ((-1 == row) || (-1 == entry)) {
			return;
		}
		// open an input-dialog, setting the selected value as default-value
		String newt = (String) JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("editTitleMsg"),
				getResourceMap().getString("editTitleTitle"), JOptionPane.PLAIN_MESSAGE, null, null,
				jTableTitles.getValueAt(row, 1));
		// if we have a valid return-value...
		if ((newt != null) && (newt.length() > 0)) {
			// change the existing value in the table
			jTableTitles.setValueAt(newt, row, 1);
			// and change the title to the new value
			data.setZettelTitle(entry, newt);
			// change edited timestamp
			data.changeEditTimeStamp(entry);

			setNewDisplayedEntryAndUpdateDisplay(entry);
		}
	}

	/**
	 * This method deletes selected keywords in the jListEntryKeywords from an
	 * entry. Therefore, the selected values are passed as parameter to a method
	 * that identifies the index-numbers and removes them from the entry's
	 * keyword-index-numbers. <br>
	 * <br>
	 * This method is called when the user presses the delete-key in the keywordlist
	 * or activated the related popup-menu-item.
	 */
	@Action(enabledProperty = "listFilledWithEntry")
	public void deleteKeywordFromEntry() {
		// get the selected values of the keyword-list
		String[] kws = retrieveSelectedKeywordsFromList();
		// if we have any selections, go on...
		if ((kws != null) && (kws.length > 0)) {
			// prepare the msg-string
			String msg;
			// if we have just a single selection, use phrasing for that message
			msg = (1 == kws.length) ? getResourceMap().getString("askForDeleteKeywordMsgSingle")
					// else if we have multiple selections, use phrasing with appropriate wording
					: getResourceMap().getString("askForDeleteKeywordMsgMultiple", String.valueOf(kws.length));
			// ask whether keyword really should be deleted
			int option = JOptionPane.showConfirmDialog(getFrame(), msg,
					getResourceMap().getString("askForDeleteKeywordTitle"), JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			// if yes, go on
			if (JOptionPane.YES_OPTION == option) {
				// delete keywords from current entry
				data.deleteKeywordsFromEntry(kws, displayedZettel);
				// update the data (frequency of occurences of keywords) from the jTableKeywords
				// and linked filter-list
				for (String k : kws) {
					linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableKeywords,
							linkedkeywordlist, k, -1);
				}
				updateDisplay();
			}
		}
	}

	@Action
	public void showInformationBox() {
		if (null == informationDlg) {
			informationDlg = new CInformation(getFrame(), data, settings);
			informationDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(informationDlg);
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		informationDlg.dispose();
		informationDlg = null;
	}

	/**
	 * This method deletes a keyword which is currently selected in the
	 * JTableKeywords.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteKeyword() {
		// get the amount of selected keywords.
		int rowcount = jTableKeywords.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// prepare the msg-string
		String msg;
		// if we have just a single selection, use phrasing for that message
		msg = (1 == rowcount) ? getResourceMap().getString("askForDeleteKeywordMsgSingle")
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteKeywordMsgMultiple", String.valueOf(rowcount));
		// ask whether keyword really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteKeywordTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, go on
		if (JOptionPane.YES_OPTION == option) {
			// and delete the keywords by opening a dialog with a background task
			// If dialog window isn't already created, do this now.
			if (taskDlg == null) {
				// get parent und init window
				taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_DELETEKEYWORDS, data,
						ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0));
				// Center new dialog window.
				taskDlg.setLocationRelativeTo(getFrame());
			}
			waitForTaskDialog();

			// remove entries also from table and linked list
			linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyRemove(jTableKeywords, linkedkeywordlist,
					this);
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTableKeywords.getRowCount()) + " "
					+ getResourceMap().getString("statusTextKeywords") + ")");
			// finally, update display
			updateDisplay();
		}
	}

	/**
	 * This action deletes one or more selected bookmarks from the jTabelBookmarks.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteBookmark() {
		// get the amount of selected bookmarks.
		int rowcount = jTableBookmarks.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// if we have just a single selection, use phrasing for that message
		String msg = (1 == rowcount) ? getResourceMap().getString("askForDeleteBookmarkMsgSingle")
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteBookmarkMsgMultiple", String.valueOf(rowcount));
		// ask whether author really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteBookmarkTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, do so
		if (JOptionPane.YES_OPTION == option) {
			// delete bookmarks
			bookmarks.deleteBookmarks(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableBookmarks, 0));
			// update display
			updateDisplay();
		}
	}

	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteAttachment() {
		// get the amount of selected keywords.
		int rowcount = jTableAttachments.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// prepare the msg-string
		String msg;
		// if we have just a single selection, use phrasing for that message
		msg = (1 == rowcount) ? getResourceMap().getString("askForDeleteAttachmentMsgSingle")
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteAttachmentMsgMultiple", String.valueOf(rowcount));
		// ask whether keyword really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteAttachmentTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, go on
		if (JOptionPane.YES_OPTION == option) {
			// get selected keywords
			String[] selectedattachments = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAttachments, 0);
			// if now row is selected, leave...
			if (null == selectedattachments) {
				return;
			}
			// retrieve the selected enty-numbers
			int[] entrynumbers = ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableAttachments, 2);
			// check for valid values
			if (entrynumbers != null && entrynumbers.length > 0) {
				// go through all selected keywords
				for (int cnt = 0; cnt < selectedattachments.length; cnt++) {
					data.deleteAttachment(selectedattachments[cnt], entrynumbers[cnt]);
				}
			}
			// update display
			updateDisplay();
		}
	}

	/**
	 * This action opens an attachment which was double-clicked within the
	 * jTableAttachments from the tabbed pane.
	 */
	@Action
	public void openAttachment() {
		// get selected row
		int selectedrow = jTableAttachments.getSelectedRow();
		// if we have no selection, leave...
		if (-1 == selectedrow) {
			return;
		}
		// get selected value
		String linktype = jTableAttachments.getValueAt(selectedrow, 0).toString();
		// and open hyperlink
		openHyperlink(linktype);
	}

	/**
	 * This method opens a file or URL either from within a clicked link inside the
	 * jEditorPane (see
	 * {@link #eventHyperlinkActivated(HyperlinkEvent)
	 * eventHyperlinkActivated(javax.swing.event.HyperlinkEvent)} or from the
	 * attachment-list (see {@link #openAttachment() openAttachment()}.
	 *
	 * @param linktype the clicked link as string
	 */
	private void openHyperlink(String linktype) {
		// call method that handles the hyperlink-click
		String returnValue = Tools.openHyperlink(linktype, getFrame(), Constants.FRAME_MAIN, data, bibtex, settings,
				jEditorPaneEntry, displayedZettel);
		// check whether we have a return value. this might be the case either when the
		// user clicked on
		// a footenote, or on the rating-stars
		if (returnValue != null) {
			// here we have a reference to another entry
			if (returnValue.startsWith("#z_") || returnValue.equals("#activatedEntry")
					|| returnValue.startsWith("#cr_")) {
				// show entry
				setNewActivatedEntryAndUpdateDisplay(data.getActivatedEntryNumber());
			} // edit cross references
			else if (returnValue.equalsIgnoreCase("#crt")) {
				editManualLinks();
			} // check whether a rating was requested
			else if (returnValue.startsWith("#rateentry")) {
				try {
					// retrieve entry-number
					int entrynr = Integer.parseInt(linktype.substring(10));
					// open rating-dialog
					if (null == rateEntryDlg) {
						rateEntryDlg = new CRateEntry(getFrame(), data, entrynr);
						rateEntryDlg.setLocationRelativeTo(getFrame());
					}
					ZettelkastenApp.getApplication().show(rateEntryDlg);
					// check whether dialog was cancelled or not
					if (!rateEntryDlg.isCancelled()) {
						setNewDisplayedEntryAndUpdateDisplay(entrynr);
					}
					rateEntryDlg.dispose();
					rateEntryDlg = null;
				} catch (NumberFormatException ex) {
					// log error
					Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
					Constants.zknlogger.log(Level.WARNING, "Could not rate entry. Link-text was {0}", linktype);
				}
			} // The user clicked on the created or edited timestamp and wants to edit this
				// timestamp
			else if (returnValue.startsWith("#tstampc") || returnValue.startsWith("#tstampe")) {
				// init value
				String defaulttimestamp = "";
				// wait for valid input
				while (defaulttimestamp != null && defaulttimestamp.isEmpty()) {
					// get timestamp, either created or edited, depending on which timestamp was
					// clicked
					String ts = Tools.getProperShortDate(
							(returnValue.startsWith("#tstampc")) ? data.getTimestampCreated(displayedZettel)
									: data.getTimestampEdited(displayedZettel));
					// show input-dialog
					defaulttimestamp = (String) JOptionPane.showInputDialog(getFrame(), // parent window
							getResourceMap().getString("editTimeStampMsg"), // message text
							getResourceMap().getString("editTimeStampTitle"), // messagebox title
							JOptionPane.PLAIN_MESSAGE, // Type of dialog
							null, // icon
							null, // array of selection values.
							// must be null to get an input-field.
							// providing an array here would create a dropdown-combobox
							ts); // initial value, date of importfile
					// now convert the user input into a timestamp
					// therefore, check whether we have any valid input at all, if we have the
					// correct length (16 chars)
					// and if we have to "." at the right position. A valid input would be e.g.
					// "31.12.08 05:46" (dd.mm.yy hh:mm)
					if (defaulttimestamp != null && 16 == defaulttimestamp.length() && defaulttimestamp.charAt(2) == '.'
							&& defaulttimestamp.charAt(5) == '.' && defaulttimestamp.charAt(10) == ' '
							&& defaulttimestamp.charAt(13) == ':') {
						// check for number values
						try {
							defaulttimestamp = defaulttimestamp.substring(8, 10) + defaulttimestamp.substring(3, 5)
									+ defaulttimestamp.substring(0, 2) + defaulttimestamp.substring(11, 13)
									+ defaulttimestamp.substring(14);
							// is input valid? (i.e. only numbers)
							Integer.parseInt(defaulttimestamp);
							// set new timestamp
							if (returnValue.startsWith("#tstampc")) {
								data.setTimestampCreated(displayedZettel, defaulttimestamp);
							} else {
								data.setTimestampEdited(displayedZettel, defaulttimestamp);
							}
							updateDisplay();
						} // The user did not edit a valid date, and probably used chars instead of
							// numbers
						catch (NumberFormatException | IndexOutOfBoundsException ex) {
							// reset value, so the input can be done again
							defaulttimestamp = "";
						}
					} else {
						if (defaulttimestamp != null) {
							defaulttimestamp = "";
						}
					}
				}
			} // in case a footnote was clicked and the user wishes to display the author in
				// the related
				// author table, do this now...
			else {
				// display tabbed pane with authors
				jTabbedPaneMain.setSelectedIndex(TAB_AUTHORS);
				// we now want to display the author in the jTable. Therefore,
				// we go through all entries of the table and search for the related
				// string-vaule
				for (int cnt = 0; cnt < jTableAuthors.getRowCount(); cnt++) {
					// get each table-value
					String row = jTableAuthors.getValueAt(cnt, 0).toString();
					// compare to requested author-string
					if (row.equals(returnValue)) {
						// if we found a match, select table row
						jTableAuthors.setRowSelectionInterval(cnt, cnt);
						// and make sure the selected row is visible...
						jTableAuthors.scrollRectToVisible(jTableAuthors.getCellRect(cnt, 0, false));
						break;
					}
				}
			}
		}
	}

	@Action(enabledProperty = "tableEntriesSelected")
	public void openAttachmentDirectory() {
		// get selected row
		int selectedrow = jTableAttachments.getSelectedRow();
		// if we have no selection, leave...
		if (-1 == selectedrow) {
			return;
		}
		// get selected value
		File filepath = FileOperationsUtil.getLinkFile(settings, data,
				jTableAttachments.getValueAt(selectedrow, 0).toString());
		// check whether selected file exists
		if (filepath.exists()) {
			// if yes retrieve directory-path and open it.
			String path = filepath.toString().substring(0, filepath.toString().lastIndexOf(File.separatorChar));
			// open path
			ZettelkastenViewUtil.openFilePath(path, settings);
		}
	}

	/**
	 * This method deletes one or more manual links from the jTableManLinks
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteManualLink() {
		// get the amount of selected bookmarks.
		int rowcount = jTableManLinks.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// if we have just a single selection, use phrasing for that message
		String msg = (1 == rowcount) ? getResourceMap().getString("askForDeleteManLinksMsgSingle")
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteManLinksMsgMultiple", String.valueOf(rowcount));
		// ask whether author really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteManLinksTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, do so
		if (JOptionPane.YES_OPTION == option) {
			// delete bookmarks
			data.deleteManualLinks(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableManLinks, 0));
			// update display
			updateDisplay();
		}
	}

	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteBookmarkCategory() {
		// get the amount of selected bookmarks.
		int rowcount = jTableBookmarks.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// retrieve selected category
		Object selcat = jTableBookmarks.getValueAt(jTableBookmarks.getSelectedRow(), 1);
		// if we have just a single selection, use phrasing for that message
		String msg = (1 == rowcount)
				? getResourceMap().getString("askForDeleteBookmarkCategoryMsgSingle",
						(selcat != null) ? selcat.toString() : "")
				// else if we have multiple selections, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteBookmarkCategoryMsgMultiple", String.valueOf(rowcount));
		// ask whether author really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteBookmarkCategoryTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, do so
		if (JOptionPane.YES_OPTION == option) {
			// retrieve selected values
			String[] bmcats = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableBookmarks, 1);
			// if we have any selection, go on
			if (bmcats != null && bmcats.length > 0) {
				// iterate all selected values
				for (String bc : bmcats) {
					// get the table data at the selected row
					bookmarks.deleteCategory(bc);
				}
				// update display
				updateDisplay();
			}
		}
	}

	/**
	 * This method deletes the selected author(s), which is/are currently selected
	 * in the JTableAuthors.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteAuthor() {
		// get the amount of selected keywords.
		int rowcount = jTableAuthors.getSelectedRowCount();
		// if nothing is selected, leave
		if (rowcount < 1) {
			return;
		}
		// prepare the msg-string
		String msg;
		// if we have just a single selection, use phrasing for that message
		msg = (1 == rowcount) ? getResourceMap().getString("askForDeleteAuthorMsgSingle")
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteAuthorMsgMultiple", String.valueOf(rowcount));
		// ask whether author really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteAuthorTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, do so
		if (JOptionPane.YES_OPTION == option) {
			// and delete the authors by opening a dialog with a background task
			// If dialog window isn't already created, do this now.
			if (taskDlg == null) {
				// get parent und init window
				taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_DELETEAUTHORS, data,
						ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0));
				// Center new dialog window.
				taskDlg.setLocationRelativeTo(getFrame());
			}
			waitForTaskDialog();

			// update the tables and the possible linked lists
			linkedauthorlist = ZettelkastenViewUtil.updateTableFrequencyRemove(jTableAuthors, linkedauthorlist, this);
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTableAuthors.getRowCount()) + " "
					+ getResourceMap().getString("statusTextAuthors") + ")");
			// finally, update display
			updateDisplay();
		}
	}

	@Action
	public void newKeyword() {
		// open an input-dialog
		String newKw = (String) JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("newKeywordMsg"),
				getResourceMap().getString("newKeywordTitle"), JOptionPane.PLAIN_MESSAGE);
		// if we have a valid return-value...
		if ((newKw != null) && (newKw.length() > 0)) {
			// ask the user if he wants to replace possible keywords, which appear as
			// synonyms, but *not*
			// as index-word, with the related index-words...
			newKw = Tools.replaceSynonymsWithKeywords(settings.getSynonyms(), new String[] { newKw })[0];
			// check whether action was cancelled. if so, null is returned.
			if (newKw != null) {
				// check whether the value already exists
				if (-1 == data.getKeywordPosition(newKw, false)) {
					// add keyword to data file
					data.addKeyword(newKw, 0);
					// save new keyword value, so it can be selected in the table
					newAddedKeyword = newKw;
					// add keyword to table and linked filter-list
					linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyNew(jTableKeywords, linkedkeywordlist,
							newKw, 0);
					// update display
					updateDisplay();
				} else {
					// display error message box
					JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errValueExistsMsg", newKw),
							getResourceMap().getString("errValueExistsTitle"), JOptionPane.PLAIN_MESSAGE);
				}
			}
		}
	}

	@Action
	public void newAuthor() {
		// open an input-dialog, setting the selected value as default-value
		if (null == biggerEditDlg) {
			// create a new dialog with the bigger edit-field, passing some initial values
			biggerEditDlg = new CBiggerEditField(getFrame(), settings, getResourceMap().getString("newAuthorTitle"), "",
					"", Constants.EDIT_AUTHOR);
			// Center new dialog window.
			biggerEditDlg.setLocationRelativeTo(getFrame());
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
			// a "\n" is replaced by [br]. So, in case the system's line-separator also
			// contains a
			// "\r", it is replaced by nothing, to clean the content.
			if (linesep.contains("\r")) {
				newAu = newAu.replace("\r", "");
			}
			// ...parse them to an array
			String[] authors = newAu.split("\n");
			for (String a : authors) {
				// if we have an empty string, do nothing...
				if (!a.isEmpty()) {
					// check whether the value already exists
					if (-1 == data.getAuthorPosition(a)) {
						// add author to data file
						data.addAuthor(a, 0);
						// change bibkey
						if (newBibKey != null) {
							data.setAuthorBibKey(a, newBibKey);
							// reset bibkey, so we only use it once in case we have multiple authors
							newBibKey = null;
						}
						// save author-value so it can be selected in the table
						newAddedAuthor = a;
						// add author to jTableAuthors and to the linked filtered list
						linkedauthorlist = ZettelkastenViewUtil.updateTableFrequencyNew(jTableAuthors, linkedauthorlist,
								a, 0);
					} else {
						// when we have a too long author-string, we truncate it so we can
						// display the complete error message.
						if (a.length() > 40) {
							a = a.substring(0, 39) + "...";
						}
						// display error message box
						JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errValueExistsMsg", a),
								getResourceMap().getString("errValueExistsTitle"), JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
			// update display
			updateDisplay();
		}
	}

	/**
	 * This method deletes entries which are selected in the titles-table
	 * (jTableTitles).
	 * 
	 * This method shows an option pane where the user can confirm the
	 * delete-progress or cancel it.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void deleteEntry() {
		// If no selected rows, nothing to delete.
		if (jTableTitles.getSelectedRowCount() == 0) {
			return;
		}

		int[] selectedEntries = ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableTitles, 0);
		if (deleteEntries(selectedEntries)) {
			data.setTitlelistUpToDate(false);
			// deleteEntries calls updateDisplay() when successful.
		}
	}

	public void bringToFront() {
		super.getFrame().setAlwaysOnTop(true);
		super.getFrame().toFront();
		super.getFrame().requestFocus();
		super.getFrame().setAlwaysOnTop(false);
	}

	/**
	 * Deletes one or more entries which entry-numbers are passed as int-array
	 * 
	 * This method shows an option pane where the user can confirm the
	 * delete-progress or cancel it. If cancelled, the method returns false.
	 *
	 * @param entriesToDelete the index-numbers of the entries that should be
	 *                        deleted =======
	 * @param entriesToDelete the index-numbers of the entries that should be
	 *                        deleted >>>>>>> 385e70a Fix update Titles tab Dialog
	 *                        from attaching to edit window >>>>>>> refs/heads/main
	 * @return {@code true} if entries were deleted, {@code false} is deletion was
	 *         cancelled
	 */
	public boolean deleteEntries(int[] entriesToDelete) {
		if ((entriesToDelete == null) || (entriesToDelete.length == 0)) {
			return false;
		}
		// when we are editing an entry, check whether the to be deleted entry is
		// currently edited.
		// if yes, cancel deletion
		if (isEditModeActive && editEntryDlg != null) {
			// go through all entries that should be deleted
			for (int n : entriesToDelete) {
				// if one of those to be deleted entries is currently being edited, cancel
				// deletion
				if (n == editEntryDlg.entryNumber) {
					// show error message
					JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("deleteNotPossibleMsg"),
							getResourceMap().getString("deleteNotPossibleTitle"), JOptionPane.PLAIN_MESSAGE);
					// display edit-dialog
					editEntryDlg.toFront();
					// do nothing.
					return false;
				}
			}
		}
		// create a linked list that contains all entries, so we check whether one or
		// more of
		// the to be deleted entries are also appearing in one or more desktop-files.
		// but first, check, whether we have any desktops at all...
		if (desktop.getCount() > 0) {
			// create linked list
			LinkedList<Integer> checkEntries = new LinkedList<>();
			// copy all to be deleted entries to the linked list
			for (int n : entriesToDelete) {
				checkEntries.add(n);
			}
			// now check whether one or more entries of the to be deleted entries appear in
			// one or
			// more desktop-files and ask the user, whether the entries really should be
			// deleted
			String multipleOccurencesMessage = Tools
					.prepareDoubleEntriesMessage(Tools.retrieveDoubleEntries(desktop, checkEntries));
			// if we have any return-value, go on...
			if (multipleOccurencesMessage != null) {
				// get system line separator
				String linesep = System.lineSeparator();
				multipleOccurencesMessage = getResourceMap().getString("askForDeleteEntriesOnDesktop") + linesep
						+ linesep + multipleOccurencesMessage;
				// create output window
				if (null == multipleOccurencesDlg) {
					// create a new dialog with the desktop-dialog, passing some initial values
					multipleOccurencesDlg = new CShowMultipleDesktopOccurences(getFrame(), settings, true,
							multipleOccurencesMessage);
					// Center new dialog window.
					multipleOccurencesDlg.setLocationRelativeTo(null);
				} else {
					multipleOccurencesDlg.setInfoMsg(multipleOccurencesMessage);
				}
				// show window
				ZettelkastenApp.getApplication().show(multipleOccurencesDlg);
			}
		}
		// if we have just a single selection, use phrasing for that message
		String msg = (1 == entriesToDelete.length)
				? getResourceMap().getString("askForDeleteEntryMsgSingle", String.valueOf(entriesToDelete[0]))
				// else if we have multiple selectios, use phrasing with appropriate wording
				: getResourceMap().getString("askForDeleteEntryMsgMultiple", String.valueOf(entriesToDelete.length));
		// ask whether entry really should be deleted
		int option = JOptionPane.showConfirmDialog(getFrame(), msg,
				getResourceMap().getString("askForDeleteEntryTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if yes, go on
		if (JOptionPane.YES_OPTION == option) {
			// delete entries
			for (int cnt = 0; cnt < entriesToDelete.length; cnt++) {
				// first, retrieve the entry's authors, so we can update the table
				// jTableAuthors,
				// by decreasing the frequencies...
				String[] aus = data.getAuthors(entriesToDelete[cnt]);
				if (aus != null) {
					for (String a : aus) {
						linkedauthorlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableAuthors,
								linkedauthorlist, a, -1);
					}
				}
				// Then, retrieve the entry's keywords, so we can update the table
				// jTableKeywords,
				// by decreasing the frequencies...
				String[] kws = data.getKeywords(entriesToDelete[cnt]);
				if (kws != null) {
					for (String k : kws) {
						linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableKeywords,
								linkedkeywordlist, k, -1);
					}
				}
				// finally, we can remove that entry
				data.deleteZettel(entriesToDelete[cnt]);
			}
			// remove entries from desktop...
			if (desktop.deleteEntries(entriesToDelete) && desktopDlg != null) {
				desktopDlg.updateEntriesAfterEditing();
			}
			// remove entries from bookmarks...
			bookmarks.deleteBookmarks(entriesToDelete);
			// manual links and the entry's content are deleted via the CDaten-class.
			// but it might be that deleting the entry from luhmann-numbers, desktop-data
			// and search results is quite time consuming. thus, we delete those parts
			// in an exra background-task
			// If dialog window isn't already created, do this now.
			if (taskDlg == null) {
				// get parent und init window
				taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_DELETEENTRY, data, searchRequests,
						entriesToDelete);
				// Center new dialog window.
				taskDlg.setLocationRelativeTo(getFrame());
			}
			waitForTaskDialog();

			// when the search dialog is opened, remove entry from list...
			if (searchResultsDlg != null) {
				searchResultsDlg.updateComboBox(-1, -1);
			}
			// finally, update display
			updateDisplay();
			// entries deleted, so return true
			return true;
		}
		return false;
	}

	private void filterTitleList(boolean forceRegEx) {
		LinkedList<Object[]> l = filterList(jTextFieldFilterTitles, jTableTitles, jButtonRefreshTitles, linkedtitlelist,
				"statusTextTitles", 1, forceRegEx);
		if (l != null) {
			linkedtitlelist = l;
		}
	}

	private LinkedList<Object[]> filterList(JTextField filterfield, JTable table, JButton refreshbutton,
			LinkedList<Object[]> list, String resourcestring, int column, boolean forceRegEx) {
		// when we filter the table and want to restore it, we don't need to run the
		// time-consuming task that creates the author-list and related
		// author-frequencies.
		// instead, we simply copy the values from the linkedlist to the table-model,
		// which is
		// much faster. but therefore we have to apply all changes to the filtered-table
		// (like adding/changing values in a filtered list) to the linked list as well.

		// get text from the textfield containing the filter string
		// convert to lowercase, we don't want case-sensitive search
		String text = filterfield.getText();
		// when we have no text, do nothing
		if (text.isEmpty()) {
			return null;
		}
		// Tell table selection listener we are doing something, so don't react on
		// value-changes
		tableUpdateActive = true;
		// get table model
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		// if we haven't already stored the current complete table data, do this now
		if (null == list) {
			// create new instance of list
			list = new LinkedList<>();
			// go through all table-data
			for (int cnt = 0; cnt < dtm.getRowCount(); cnt++) {
				// init the object-variable
				Object[] o;
				// in case we have the table with titles, we make an exception, because
				// this table has two more columns that should be filtered, the columns with
				// the entries timestamps.
				if (table == jTableTitles) {
					o = new Object[5];
				} // in case we have the table with attachments, we make an exception, because
					// this table has one more column that should be filtered
				else if (table == jTableAttachments) {
					o = new Object[3];
				} // for each "typical" table, we have two columns
				else {
					o = new Object[2];
				}
				// fill object with values
				for (int len = 0; len < o.length; len++) {
					o[len] = dtm.getValueAt(table.convertRowIndexToModel(cnt), len);
				}
				// add object to linked list
				list.add(o);
			}
		}
		int[] columns;
		if (table == jTableTitles) {
			columns = new int[] { column, 2, 3, 4 };
		} else {
			columns = new int[] { column };
		}
		de.danielluedecke.zettelkasten.util.TableUtils.filterTable(table, dtm, text, columns, forceRegEx);
		// reset textfield
		if (!forceRegEx) {
			filterfield.setText("");
		}
		filterfield.requestFocusInWindow();
		// enable textfield only if we have more than 1 element in the jtable
		filterfield.setEnabled(table.getRowCount() > 0);
		// enable refresh button
		refreshbutton.setEnabled(true);
		// show amount of entries
		statusMsgLabel.setText(
				"(" + String.valueOf(table.getRowCount()) + " " + getResourceMap().getString(resourcestring) + ")");
		// all changes have been done...
		tableUpdateActive = false;
		// return list with new entries.
		return list;
	}

	@Action
	public void refreshTitleList() {
		refreshList(jTableTitles, jButtonRefreshTitles, jTextFieldFilterTitles, linkedtitlelist, "statusTextTitles");
		linkedtitlelist = null;
	}

	@Action
	public void filterClusterList() {
		// get the treemodel
		DefaultTreeModel dtm = (DefaultTreeModel) jTreeCluster.getModel();
		// and receive the root
		MutableTreeNode root = (MutableTreeNode) dtm.getRoot();
		// get the filter text
		String text = jTextFieldFilterCluster.getText().toLowerCase();
		// if we have any root and filter-text, go on
		if (root != null && !text.isEmpty()) {
			// go through all root's children
			for (int cnt = dtm.getChildCount(root) - 1; cnt >= 0; cnt--) {
				// get the child-node
				MutableTreeNode child = (MutableTreeNode) dtm.getChild(root, cnt);
				// get the node's text
				String childtext = child.toString().toLowerCase();
				// if the child does *not* contains the filtertext, remove it
				if (!childtext.contains(text)) {
					dtm.removeNodeFromParent(child);
				}
			}

			// if the filtering removed a selected node, clear the jListCluster
			if (null == jTreeCluster.getSelectionPath()) {
				clusterList.clear();
			}

			// indicate that we have filtered the list
			linkedclusterlist = true;
			// reset textfield
			jTextFieldFilterCluster.setText("");
			jTextFieldFilterCluster.requestFocusInWindow();
			// enable textfield only if we have more than 1 element in the jtable
			jTextFieldFilterCluster.setEnabled(jTreeCluster.getRowCount() > 0);
			// enable refresh button
			jButtonRefreshCluster.setEnabled(true);
		}
	}

	@Action
	public void refreshClusterList() {
		// first check whether we have filtered the list
		if (linkedclusterlist) {
			// if yes, init the list
			initClusterList();
			// enable filter field
			jTextFieldFilterCluster.setEnabled(true);
			// disable refresh button
			jButtonRefreshCluster.setEnabled(false);
		}
	}

	private void filterAuthorList(boolean forceRegEx) {
		LinkedList<Object[]> l = filterList(jTextFieldFilterAuthors, jTableAuthors, jButtonRefreshAuthors,
				linkedauthorlist, "statusTextAuthors", 0, forceRegEx);
		if (l != null) {
			linkedauthorlist = l;
		}
	}

	private void filterAttachmentList(boolean forceRegEx) {
		LinkedList<Object[]> l = filterList(jTextFieldFilterAttachments, jTableAttachments, jButtonRefreshAttachments,
				linkedattachmentlist, "statusTextAttachments", 0, forceRegEx);
		if (l != null) {
			linkedattachmentlist = l;
		}
	}

	@Action
	public void refreshAuthorList() {
		refreshList(jTableAuthors, jButtonRefreshAuthors, jTextFieldFilterAuthors, linkedauthorlist,
				"statusTextAuthors");
		linkedauthorlist = null;
	}

	private void refreshList(JTable table, JButton refreshbutton, JTextField filterfield, LinkedList<Object[]> list,
			String resourcestring) {
		// first check whether we have any saved values at all
		if (list != null) {
			// Table-values might be changed, so selection listener should not react
			tableUpdateActive = true;
			// get table model
			DefaultTableModel dtm = (DefaultTableModel) table.getModel();
			// delete all data from the author-table
			dtm.setRowCount(0);
			// create an iterator for the linked list
			ListIterator<Object[]> iterator = list.listIterator();
			// go through complete linked list and add each element to the table(model)
			while (iterator.hasNext()) {
				dtm.addRow(iterator.next());
			}
			// enable filter field
			filterfield.setEnabled(true);
			// disable refresh button
			refreshbutton.setEnabled(false);
			// show amount of entries
			statusMsgLabel.setText(
					"(" + String.valueOf(table.getRowCount()) + " " + getResourceMap().getString(resourcestring) + ")");
			// all changes have been made...
			tableUpdateActive = false;
		}
	}

	@Action
	public void refreshAttachmentList() {
		refreshList(jTableAttachments, jButtonRefreshAttachments, jTextFieldFilterAttachments, linkedattachmentlist,
				"statusTextAttachments");
		linkedattachmentlist = null;
	}

	/**
	 * This method opens a dialog with the data from the currently selected
	 * bookmarks. The user can then make changes like changing the category, editing
	 * comments etc.<br>
	 * <br>
	 * Changes are immediatly applied to the jTableBookmarks.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void editBookmark() {
		// get the selected row
		int row = jTableBookmarks.getSelectedRow();
		// if now row is selected, leave...
		if (-1 == row) {
			return;
		}
		// else change bookmark entry
		addToBookmarks(new int[] { ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableBookmarks, 0) },
				true);
	}

	/**
	 * This method opens an input-dialog where the user can edit a bookmark's
	 * category-name. After that, all bookmarks with the old category-name get this
	 * new entered category-name. <br>
	 * <br>
	 * If the new name already exists, the categories are merged.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void editBookmarkCategory() {
		// get the selected row
		int row = jTableBookmarks.getSelectedRow();
		// if now row is selected, leave...
		if (-1 == row) {
			return;
		}
		// save the old value
		String oldbm = jTableBookmarks.getValueAt(row, 1).toString();
		// open an input-dialog, setting the selected value as default-value
		String newbm = (String) JOptionPane.showInputDialog(getFrame(),
				getResourceMap().getString("editBookmarkCategoryMsg"),
				getResourceMap().getString("editBookmarkCategoryTitle"), JOptionPane.PLAIN_MESSAGE, null, null,
				jTableBookmarks.getValueAt(row, 1));
		// if we have a valid return-value that does not equal the old value...
		if ((newbm != null) && (newbm.length() > 0) && (!oldbm.equalsIgnoreCase(newbm))) {
			// check whether new category name already exists
			if (-1 == bookmarks.getCategoryPosition(newbm)) {
				// get index-number of old category
				int pos = bookmarks.getCategoryPosition(oldbm);
				// change category-name
				bookmarks.setCategory(pos, newbm);
			} // we know that the category already exists. here we cann offer merging...
			else {
				// get the position of old, previous category
				int oldpos = bookmarks.getCategoryPosition(oldbm);
				// get the position of the existing, new category
				int newpos = bookmarks.getCategoryPosition(newbm);
				// change category-index-numbers
				bookmarks.changeCategoryIndexOfBookmarks(oldpos, newpos);
			}
			updateDisplay();
		}
	}

	@Action(enabledProperty = "tableEntriesSelected")
	public void editAttachment() {
		// get selected keywords
		String[] selectedattachments = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAttachments, 0);
		// if now row is selected, leave...
		if (null == selectedattachments) {
			return;
		}
		// get selected rows. we need this numbers for setting back the new values, see
		// below
		int[] selectedrows = jTableAttachments.getSelectedRows();
		// retrieve the selected enty-numbers
		int[] entrynumbers = ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableAttachments, 2);
		// check for valid values
		if (entrynumbers != null && entrynumbers.length > 0) {
			// go through all selected keywords
			for (int cnt = selectedattachments.length - 1; cnt >= 0; cnt--) {
				// save the old value
				String oldAt = selectedattachments[cnt];
				// open an input-dialog, setting the selected value as default-value
				String newAt = (String) JOptionPane.showInputDialog(getFrame(),
						getResourceMap().getString("editAttachmentMsg"),
						getResourceMap().getString("editAttachmentTitle"), JOptionPane.PLAIN_MESSAGE, null, null,
						oldAt);
				// if we have a valid return-value that differs from the old value...
				if ((newAt != null) && (newAt.length() > 0) && (!oldAt.equalsIgnoreCase(newAt))) {
					// change the existing value in the table
					jTableAttachments.setValueAt(newAt, selectedrows[cnt], 0);
					// update the possible file-extension
					jTableAttachments.setValueAt(FileOperationsUtil.getFileExtension(settings, data, newAt),
							selectedrows[cnt], 1);
					// also change the attachment in the data-file
					data.changeAttachment(oldAt, newAt, entrynumbers[cnt]);
					// if we have a filtered list, remove the element also from
					// our refresh-list, so we don't show this item again when the list
					// is being refreshed
					if (linkedattachmentlist != null) {
						linkedattachmentlist = updateLinkedList(linkedattachmentlist, oldAt, newAt, 0);
					}
				}
				setNewDisplayedEntryAndUpdateDisplay(entrynumbers[0]);
			}
		}
	}

	private LinkedList<Object[]> updateLinkedList(LinkedList<Object[]> list, String oldvalue, String newvalue,
			int arrayindex) {
		// iterate list
		for (int pos = 0; pos < list.size(); pos++) {
			// get each element
			Object[] o = list.get(pos);
			// if element equals requested value, change frequency
			if (oldvalue.equals(o[arrayindex].toString())) {
				// change frequency valie
				o[arrayindex] = newvalue;
				// and set the element back to the list
				list.set(pos, o);
				break;
			}
		}
		return list;
	}

	@Action
	public void copyPlain() {
		Tools.copyPlain(data, displayedZettel, jEditorPaneEntry);
	}

	/**
	 * This method opens a dialog to edit an author. When the edited author already
	 * exists, the programm offers to "merge" the authors, and also all entries will
	 * be updated.<br>
	 * <br>
	 * Furthermore, when an edit-request was made while the author-list was
	 * filtered, the filtered linkedlist is also being updated.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void editAuthor() {
		// get selected authors
		String[] selectedauthors = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0);
		// if now row is selected, leave...
		if (null == selectedauthors) {
			return;
		}
		// get selected rows. we need this numbers for setting back the new values, see
		// below
		int[] selectedrows = jTableAuthors.getSelectedRows();
		// go through all selected keywords
		for (int cnt = selectedauthors.length - 1; cnt >= 0; cnt--) {
			// save the old value
			String oldAu = selectedauthors[cnt];
			// save old bibkey value
			String oldbibkey = data.getAuthorBibKey(oldAu);
			// open an input-dialog, setting the selected value as default-value
			if (null == biggerEditDlg) {
				// create a new dialog with the bigger edit-field, passing some initial values
				biggerEditDlg = new CBiggerEditField(getFrame(), settings,
						getResourceMap().getString("editAuthorTitle"), oldAu, oldbibkey, Constants.EDIT_AUTHOR);
				// Center new dialog window.
				biggerEditDlg.setLocationRelativeTo(getFrame());
			}
			// show window
			ZettelkastenApp.getApplication().show(biggerEditDlg);
			// after closing the window, get the new value
			String newAu = biggerEditDlg.getNewValue();
			String newBibKey = biggerEditDlg.getNewBibKey();
			// delete the input-dialog
			biggerEditDlg.dispose();
			biggerEditDlg = null;
			// if we have a valid return-value that does not equal the old value... (so
			// changes were made)
			if (newAu != null && newAu.length() > 0) {
				// check whether the value already exists, or if we have a case-change
				if (-1 == data.getAuthorPosition(newAu) || oldAu.equalsIgnoreCase(newAu)) {
					// change the existing value in the table
					jTableAuthors.setValueAt(newAu, selectedrows[cnt], 0);
					// get the index-number of the old author-string
					int nr = data.getAuthorPosition(oldAu);
					// and change the entry to the new value
					data.setAuthor(nr, newAu, newBibKey);
					// if we have a filtered list, remove the element also from
					// our refresh-list, so we don't show this item again when the list
					// is being refreshed
					if (linkedauthorlist != null) {
						linkedauthorlist = updateLinkedList(linkedauthorlist, oldAu, newAu, 0);
					}
				} else {
					// The new name for author already exists, so we can offer to merge
					// the authors here. in fact, this is an easy find/replace-routine, since the
					// old author is replaced by the existing one, when we merge them.

					// create a JOptionPane with yes/no/cancel options
					int option = JOptionPane.showConfirmDialog(getFrame(), getResourceMap().getString("mergeAuthorMsg"),
							getResourceMap().getString("mergeAuthorTitle"), JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE);
					// if no merge is requested, do nothing.
					if (JOptionPane.NO_OPTION == option) {
						return;
					}
					// merge the authors by opening a dialog with a background task
					// If dialog window isn't already created, do this now.
					if (taskDlg == null) {
						// get parent und init window
						taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_MERGEAUTHORS, data,
								taskinfo, oldAu, newAu, newBibKey, jTableAuthors, selectedrows[cnt], linkedauthorlist);
						// Center new dialog window.
						taskDlg.setLocationRelativeTo(getFrame());
					}
					waitForTaskDialog();

					// update the merged linked list
					linkedauthorlist = taskinfo.getLinkedValues();
					// show amount of entries
					statusMsgLabel.setText("(" + String.valueOf(jTableAuthors.getRowCount()) + " "
							+ getResourceMap().getString("statusTextAuthors") + ")");
				}
				// finally, update display
				updateDisplay();
				// update authortext in textbox
				showAuthorText();
			}
		}
	}

	@Action(enabledProperty = "tableEntriesSelected")
	public void changeBibkey() {
		if (setBibKeyDlg != null) {
			setBibKeyDlg.dispose();
			setBibKeyDlg = null;
		}
		// open an input-dialog, setting the selected value as default-value
		if (null == setBibKeyDlg) {
			// create a new dialog with the bigger edit-field, passing some initial values
			setBibKeyDlg = new CSetBibKey(getFrame(), this, data, bibtex, settings);
			// Center new dialog window.
			setBibKeyDlg.setLocationRelativeTo(getFrame());
		}
		// show window
		ZettelkastenApp.getApplication().show(setBibKeyDlg);
	}

	/**
	 * @return The selected values from the jTableAuthors as String-ArrayList, or
	 *         {@code null} if no values have been selected
	 */
	public LinkedList<String> getSelectedAuthors() {
		String[] arr = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0);
		if (arr != null && arr.length > 0) {
			return new LinkedList<String>(Arrays.asList(arr));
		}
		return null;
	}

	/**
	 * This method retrieves the selected keyword(s) from the jTableKeywords and
	 * adds them to the keyword-list (jListKeywords).
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addKeywordToList() {
		// check for any selections. if nothing selected, leave
		if (jTableKeywords.getRowCount() < 1) {
			return;
		}
		// get selections
		if (!addKeywords(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0), false)) {
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("noNewKeywordsFoundMsg"),
					getResourceMap().getString("noNewKeywordsFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	private boolean addKeywords(String[] kws, boolean comesFromTextSelection) {
		// check for valid array
		if ((kws != null) && (kws.length > 0)) {
			// add keywords to entry... all relevant stuff like checking for multiple
			// keywords,
			// synonyms or their related index-words etc. is done in this mehtod. an array
			// of those
			// keywords that have been added is returned.
			kws = data.addKeywordsToEntry(kws, displayedZettel, 1);
			// iterate array and update table-frequencies.
			if (kws != null && kws.length > 0) {
				// update table with linked list. when "comesFromTextSelection" is true, we have
				// to check whether we have a completely new keyword, so we have to use the
				// updateTableFrequencyNew-method, or an existing one...
				if (comesFromTextSelection) {
					// we may have several keywords, since "comesFromTextSelection" is also true
					// when
					// we have drag&drop from the jTableKeywords. In this case, we may have multiple
					// keywords
					// dropped here
					for (String k : kws) {
						// when the added keyword was a textselection (see addToKeywordList()), the
						// parameter
						// "comesFromTextSelection" is true. in this case, we check whether the keyword
						// already
						// exists or not. if *not*, selectedKeywordExists is set to true - so we can use
						// another
						// updateTable-Method below. in case we find the keyword, so it already exists,
						// we leave
						// "selectedKeywordExists" to false, so below is a simple change-frequency
						// update
						boolean selectedKeywordExists = (data.getKeywordPosition(k, false) != -1);
						// in this case we have an existing keyword from the text selection, so we need
						// to update the keyword-list
						if (selectedKeywordExists) {
							// to update the list, we have to select the added keyword in the
							// jTable. we do this here, and then we call the update method which
							// adds selected keywords as new keywords
							for (int cnt = 0; cnt < jTableKeywords.getRowCount(); cnt++) {
								// get each value
								String val = jTableKeywords.getValueAt(cnt, 0).toString();
								// if table-value starts with the entered text in the textfield...
								if (val.equals(k)) {
									// ...select that value
									jTableKeywords.getSelectionModel().setSelectionInterval(cnt, cnt);
									// and make it visible
									jTableKeywords.scrollRectToVisible(jTableKeywords.getCellRect(cnt, 0, false));
								}
							}
							linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableKeywords,
									linkedkeywordlist, k, 1);
						} // in this case we have a new keyword from the text selection, which is added
							// to the keyword-list
						else {
							linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyNew(jTableKeywords,
									linkedkeywordlist, k, 1);
						}
					}
				} // else when we have already existing keywords, update table with linked list
					// using
					// the updateTableFrequencyChange-method.
				else {
					for (String k : kws) {
						if (!k.isEmpty()) {
							linkedkeywordlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableKeywords,
									linkedkeywordlist, k, 1);
						}
					}
				}
				// update the display
				updateDisplay();
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the displayed entry to the bookmark list.
	 */
	@Action(enabledProperty = "entryBookmarked")
	public void addToBookmark() {
		addToBookmarks(new int[] { displayedZettel }, false);
	}

	/**
	 * Adds one or more bookmarks to the bookmark-datafile.
	 *
	 * @param bms  one or more bookmarks (entry-numbers) stored in an integer-array
	 * @param edit true when existing bookmarks should be edited, false if new
	 *             bookmarks should be added.
	 * @return {@code true} if bookmarks have been successfully added, false if an
	 *         error occured
	 */
	public boolean addToBookmarks(int[] bms, boolean edit) {
		// return false on null or empty array
		if (null == bms || bms.length < 1) {
			return false;
		}
		// open the bookmark-dialog
		if (null == newBookmarkDlg) {
			// create a new dialog for editing new bookmarks
			newBookmarkDlg = new CNewBookmark(getFrame(), bookmarks, bms, edit, settings);
			// Center new dialog window.
			newBookmarkDlg.setLocationRelativeTo(getFrame());
		}
		// show window
		ZettelkastenApp.getApplication().show(newBookmarkDlg);
		if (!newBookmarkDlg.isCancelled()) {
			updateDisplay();
		}
		// delete the input-dialog
		newBookmarkDlg.dispose();
		newBookmarkDlg = null;
		// everything ok
		return true;
	}

	private void addAttachments(String[] att) {
		// add attachments
		data.addAttachments(displayedZettel, att);
		updateDisplay();
	}

	/**
	 * This method retrieves the selected authors(s) from the jTableAuthors and adds
	 * them to the author-textfield of the entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addAuthorToList() {
		// check for any selections. if nothing selected, leave
		if (jTableAuthors.getRowCount() < 1) {
			return;
		}
		// get selections
		String[] aus = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0);
		addAuthorToList(aus);
	}

	private void addAuthorToList(String[] aus) {
		// check for valid array
		if ((aus != null) && (aus.length > 0)) {
			// iterate array
			for (String a : aus) {
				// if the author does not already exist in the current entry...
				if (!a.isEmpty() && !data.existsInAuthors(a, displayedZettel)) {
					// ...add it to that entry
					data.addAuthorToEntry(a, displayedZettel, 1);
					// and update table frequencies...
					linkedauthorlist = ZettelkastenViewUtil.updateTableFrequencyChange(jTableAuthors, linkedauthorlist,
							a, 1);
				}
			}
			// update the display
			updateDisplay();
		}
	}

	/**
	 * This action shows the tab with the sub-entries of an entry. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowLuhmann() {
		jTabbedPaneMain.setSelectedIndex(TAB_LUHMANN);
	}

	/**
	 * This action shows the tab with the authors of the main data. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowAuthors() {
		jTabbedPaneMain.setSelectedIndex(TAB_AUTHORS);
	}

	/**
	 * This action shows the tab with the bookmarks. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowBookmarks() {
		jTabbedPaneMain.setSelectedIndex(TAB_BOOKMARKS);
	}

	private void showAttachments() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// reset status text, since the amount of titles is euqal to the amount of
		// entries
		statusMsgLabel.setText("");
		// show/enabke related menu
		showTabMenu(viewMenuAttachments);
		// if list is up to date, do nothing.
		if (data.isAttachmentlistUpToDate()) {
			return;
		}
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SHOWATTACHMENTS, data, null, /*
																												 * only
																												 * needed
																												 * for
																												 * authors
																												 */
					null, /* only needed for authors */
					settings, false, /* only need for keywords */
					0, /* only need for authors */
					(DefaultTableModel) jTableAttachments.getModel());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// enable textfield only if we have more than 1 element in the jtable
		jTextFieldFilterAttachments.setEnabled(jTableAttachments.getRowCount() > 0);
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableAttachments.getRowCount()) + " "
				+ getResourceMap().getString("statusTextAttachments") + ")");
	}

	/**
	 * This method displays the all bookmarks in the jtable
	 */
	private void showBookmarks() {
		// fill bookmarked entries into table
		fillBookmarksToTables(bookmarks.getAllBookmarkedEntries());
		// get all action listeners from the combo box
		ActionListener[] al = jComboBoxBookmarkCategory.getActionListeners();
		// remove all action listeners so we don't fire several action-events
		// when we update the combo box. we can set the action listener later again
		for (ActionListener listener : al) {
			jComboBoxBookmarkCategory.removeActionListener(listener);
		}
		// clear combobox
		jComboBoxBookmarkCategory.removeAllItems();
		// add first cat-description
		jComboBoxBookmarkCategory.addItem(getResourceMap().getString("bookmarkAllCategoriesText"));
		// retrieve bookmark-categories
		String[] bmcats = bookmarks.getCategoriesInSortedOrder();
		// check if we have any categories
		if (bmcats != null && bmcats.length > 0) {
			// iterate array and add categories to combobox
			for (String bmc : bmcats) {
				jComboBoxBookmarkCategory.addItem(bmc);
			}
		}
		// add action listener to combo box
		jComboBoxBookmarkCategory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// get selection
				int selected = jComboBoxBookmarkCategory.getSelectedIndex();
				// check for valid selection
				if (selected != -1) {
					// check whether first value was selected. if so, show all bookmarks
					if (0 == selected) {
						fillBookmarksToTables(bookmarks.getAllBookmarkedEntries());
					} // else retrieve category and show only bookmarks of the selected category
					else {
						// retrieve selected category
						String cat = jComboBoxBookmarkCategory.getSelectedItem().toString();
						// retrieve all bookmarks from that category
						fillBookmarksToTables(bookmarks.getBookmarkedEntriesFromCat(cat));
					}
					// show amount of entries
					statusMsgLabel.setText("(" + String.valueOf(jTableBookmarks.getRowCount()) + " "
							+ getResourceMap().getString("statusTextBookmarks") + ")");
				}
			}
		});
		// show/enabke related menu
		showTabMenu(viewMenuBookmarks);
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableBookmarks.getRowCount()) + " "
				+ getResourceMap().getString("statusTextBookmarks") + ")");
	}

	/**
	 * This method fills all bookmarked entries, which are passed as parameter
	 * {@code bms}, into the jTableBookmarks.
	 *
	 * @param bms an integer-array with all bookmarked entries.
	 */
	private void fillBookmarksToTables(int[] bms) {
		// get the table model
		DefaultTableModel tm = (DefaultTableModel) jTableBookmarks.getModel();
		// reset the table
		tm.setRowCount(0);
		// check if we have any bookmarks at all
		if (bms != null && bms.length > 0) {
			// sort array
			Arrays.sort(bms);
			// go through all bookmark-entries
			for (int cnt = 0; cnt < bms.length; cnt++) {
				// get bookmark
				String[] bm = bookmarks.getCompleteBookmark(bookmarks.getBookmarkPosition(bms[cnt]));
				// check if bookmark exists
				if (bm != null) {
					// create a new object
					Object[] ob = new Object[2];
					// store the information in that object
					ob[0] = Integer.parseInt(bm[0]);
					ob[1] = bm[1];
					// and add that content as a new row to the table
					tm.addRow(ob);
				}
			}
		}
	}

	/**
	 * This method displays the all authors in the author data file using a
	 * background task. after the task finishes, all authors and their useage
	 * frequency in the main data file (zknfile) are displayed in the JTable of the
	 * JTabbedPane
	 */
	private void showAuthors() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableAuthors.getRowCount()) + " "
				+ getResourceMap().getString("statusTextAuthors") + ")");
		// show/enable related menu
		showTabMenu(viewMenuAuthors);
		// if authorlist is up to date, do nothing.
		if (data.isAuthorlistUpToDate()) {
			return;
		}
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SHOWAUTHORS, data, null, /*
																											 * only
																											 * needed
																											 * for
																											 * keywords
																											 */
					bibtex, null, /* only needed for attachments */
					false, /* only needed for keywords */
					jComboBoxAuthorType.getSelectedIndex(), (DefaultTableModel) jTableAuthors.getModel());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// check whether we have a new added author, and if so, select it
		if (newAddedAuthor != null) {
			// select recently added value
			de.danielluedecke.zettelkasten.util.TableUtils.selectValueInTable(jTableAuthors, newAddedAuthor, 0);
			// and clear strimg
			newAddedAuthor = null;
		}
		// enable textfield only if we have more than 1 element in the jtable
		jTextFieldFilterAuthors.setEnabled(jTableAuthors.getRowCount() > 0);
		// show amount of entries
		statusMsgLabel.setText("(" + String.valueOf(jTableAuthors.getRowCount()) + " "
				+ getResourceMap().getString("statusTextAuthors") + ")");
	}

	/**
	 * This action shows the tab with the titles of the current data file. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowTitles() {
		jTabbedPaneMain.setSelectedIndex(TAB_TITLES);
	}

	/**
	 * This action shows the tab with the titles of the current data file. <br>
	 * <br>
	 * We need this action just for the menu command, so we can set keybindings and
	 * resources to that menu item. it's better doing it like this rather than
	 * having an actionPerformed-event.
	 */
	@Action
	public void menuShowCluster() {
		jTabbedPaneMain.setSelectedIndex(TAB_CLUSTER);
	}

	/**
	 * This methods displays the entry which is selected in the titles-table
	 * (jTableTitles). This method is especially called from the mouse-clicked and
	 * key-released events from the jTableTitle.
	 */
	private void showEntryFromTitles() {
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			// If no data available, do nothing.
			return;
		}

		// Get the entry from the selected row.
		int selectedEntry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableTitles, 0);
		if (selectedEntry == -1) {
			// If invalid selection, do nothing.
			return;
		}

		// Show selected entry.
		UpdateDisplayOptions options = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().updateTitlesTab(false)
				.build();
		setNewDisplayedEntryAndUpdateDisplay(selectedEntry, options);
	}

	/**
	 * This methods displays the entry which is selected in the attachment-table
	 * (jTableAttachment). This method is especially called from the mouse-clicked
	 * and key-released events from the jTableTitle.
	 */
	private void showEntryFromAttachments() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// get the selected row
		int entry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableAttachments, 2);
		// if we don't have a valid selection, use current entry as reference
		if (-1 == entry) {
			setNewDisplayedEntryAndUpdateDisplay(data.getActivatedEntryNumber());
		} // and if it was a avalid value, show entry
		else {
			setNewDisplayedEntryAndUpdateDisplay(entry);
		}
	}

	/**
	 * This method retrieves the selected authors from the jTableAuthors and
	 * displays in the text area on the author-tab, so the user can see the complete
	 * author-value. the table-row usually is not wide enough to display the whole
	 * author-text
	 */
	public void showAuthorText() {
		// if no data available, do nothing.
		if (data.getCount(Daten.AUCOUNT) < 1) {
			return;
		}
		// get selected authors from table
		String[] aus = ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0);
		// if we have any selection, go on
		if (aus != null && aus.length > 0) {
			// string builder for output
			StringBuilder sb = new StringBuilder("");
			// append style-definition
			String finalcontent = HtmlUbbUtil.getAuthorStyleDefinition(settings);
			// go through selected authors and add them to stringbuilder,
			// including possible bibkey-values.
			for (String a : aus) {
				// append author-value
				sb.append(a);
				// get author-bibkey
				String bibkey = data.getAuthorBibKey(a);
				// if bibkey available, add it as well
				if (bibkey != null && !bibkey.isEmpty()) {
					sb.append(" <i>(Bibkey: ").append(bibkey).append(")</i>");
				}
				// add line-separators
				sb.append("<br><br>");
			}
			// cut of last two line separators
			if (sb.length() > 0) {
				sb.setLength(sb.length() - 8);
			}
			finalcontent = finalcontent + "<body>" + sb.toString() + "</body>";
			// show full author-text in text area
			jEditorPaneDispAuthor.setText(finalcontent);
		} else {
			jEditorPaneDispAuthor.setText("");
		}
	}

	/**
	 * This methods displays the entry which is selected in the bookmarks-table
	 * (jTableBookmarks). This method is especially called from the mouse-clicked
	 * and key-released events from the jTableBookmarks.
	 */
	private void showEntryFromBookmarks() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// get the entry number from selected bookmark
		int nr = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableBookmarks, 0);
		// if no entry is selected, show current entry
		if (-1 == nr) {
			// clear textfield content
			jEditorPaneBookmarkComment.setText("");
			setNewDisplayedEntryAndUpdateDisplay(data.getActivatedEntryNumber());
		} else {
			// get the selected row
			int row = jTableBookmarks.getSelectedRow();
			// show comment of bookmark
			String comment = (row != -1) ? bookmarks.getCommentAsHtml(row) : "";
			// show comment
			jEditorPaneBookmarkComment.setText(comment);
			setNewDisplayedEntryAndUpdateDisplay(nr);
		}
	}

	/*
	 * displaySelectedEntryFromManualLinks is called whenever a selected entry is
	 * changed in the Manual Links table. Sometimes multiple times with the same
	 * value.
	 */
	private void updateDisplayedEntryWithSelectedEntryFromManualLinks() {
		// If no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}

		// Get the selected entry.
		int entry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableManLinks, /* column= */ 0);
		// If no entry is selected, do nothing.
		if (entry == -1) {
			return;
		}
		// If selected entry is already the displayed entry, do nothing.
		if (displayedZettel == entry) {
			return;
		}

		// No need to update the Links tab when selection changes.
		UpdateDisplayOptions options = new UpdateDisplayOptions.UpdateDisplayOptionsBuilder().updateLinksTab(false)
				.build();
		setNewDisplayedEntryAndUpdateDisplay(entry, options);
	}

	/**
	 * This method displays the the keyword-clusters, i.e. the relation between
	 * keywords and those entries that contain all these related keywords. <br>
	 * <br>
	 * Therefore, this method first displays all keywords as first-level-elements of
	 * the jTreeCluster. Then, when a keyword is selected, this method searches for
	 * all entries that contain this keyword and sets the related entry-numbers into
	 * the jListCluster. <br>
	 * <br>
	 * After that, all keywords of those found entries are collected and displayed
	 * as children of the selected keyword. This repeats for each selected keyword,
	 * i.e. if a children is selected, the method searches for the selected keyword
	 * and all parent-keywords in the entries of the jList. All entries, that
	 * contain each(!) keyword from the first-level-keyword to the selected child
	 * will be displayed in the jList.
	 */
	private void showCluster() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// when a cluster-taks is already running, return
		if (createClusterIsRunning) {
			return;
		}
		// show/enabke related menu
		showTabMenu(viewMenuCluster);
		// if clusterlist is up to date, do nothing.
		if (data.isClusterlistUpToDate()) {
			return;
		}
		// reset status text, since the amount of titles is euqal to the amount of
		// entries
		statusMsgLabel.setText("");
		// init the cluster list. this method is separate, because we can
		// also use it for the "refreshClusterList()" method.
		initClusterList();

		if (!jCheckBoxCluster.isSelected()) {
			// enable textfield only if we have more than 1 element in the jTree
			jTextFieldFilterCluster.setEnabled(jTreeCluster.getRowCount() > 1);
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTreeCluster.getRowCount()) + " "
					+ getResourceMap().getString("statusTextKeywords") + ")");
		}
	}

	private synchronized void initClusterList() {
		// get the treemodel
		DefaultTreeModel dtm = (DefaultTreeModel) jTreeCluster.getModel();
		// and first of all, clear the jTree
		dtm.setRoot(null);
		// get the amount of keywords
		int kwcount = data.getCount(Daten.KWCOUNT);
		// if we have no keywords, quit
		if (kwcount < 1) {
			return;
		}
		// if this checkbox is selected, we don't show the relations of *all*
		// keywords, but only of those keywords that appear in the current entry
		// and all related keywords of the current entry's keywords.
		if (jCheckBoxCluster.isSelected()) {
			// when a cluster-taks is already running, return
			if (createClusterIsRunning) {
				return;
			}
			// disable checkbox during task operation
			jCheckBoxCluster.setEnabled(false);
			// Tell user that we are doing something...
			statusMsgLabel.setText(getResourceMap().getString("createLuhmannMsg"));

			prepareAndStartTask(clusterTask());
		} else {
			// else create a string array (for sorting the keywords)
			String[] kws = new String[kwcount];
			// copy all keywords to that array
			for (int cnt = 1; cnt <= kwcount; cnt++) {
				kws[cnt - 1] = data.getKeyword(cnt);
			}
			// sort the array
			if (kws != null && kws.length > 0) {
				Arrays.sort(kws, new Comparer());
			}
			// set this as root node. we don't need to care about this, since the
			// root is not visible.
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("ZKN3-Cluster");
			dtm.setRoot(root);
			// if we have any keywords, set them to the list
			if (kws != null) {
				// for each array in the keyword-array...
				for (String kw : kws) {
					// create a new node and add the keyword to the tree
					// remember that we might have empty keyword-entries in the array, which
					// have to be "removed" here
					if (!kw.isEmpty()) {
						root.add(new DefaultMutableTreeNode(kw));
					}
				}
				// completely expand the jTree
				TreeUtil.expandAllTrees(true, jTreeCluster);
			}
			// we have no filtered list...
			linkedclusterlist = false;
			// indicate that the cluster list is up to date...
			data.setClusterlistUpToDate(true);
		}
	}

	/**
	 * This method builds the cluster or keyword-relations. That means, all entries
	 * that contain <b>all</b> keywords from the tree root through the selected path
	 * are extracted and displayed in the related list. <br>
	 * <br>
	 * After all Keywords from the selected path and their related entries are set
	 * to the list, we get <b>all</b> keywords of the entries in that list. all new
	 * keywords, that do not already appear in the tree-path, are added as new
	 * children to the last selected keyword in the tree. Thus, by selecting a
	 * keyword, the related entries are shown and then the remaining related
	 * keywords are addes as new children.
	 */
	private void showClusterRelations() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// if the link-table is not shown, leave
		if (jTabbedPaneMain.getSelectedIndex() != TAB_CLUSTER) {
			return;
		}
		// retrieve selected node
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeCluster.getLastSelectedPathComponent();
		// if we have don't a valid selection, quit
		if (null == node) {
			return;
		}
		// get the treepath to the selection. this path contains all nodes from root
		// until to the selected child. we will set these nodes to the keywordlist
		TreePath tp = jTreeCluster.getLeadSelectionPath();
		// disable tree, showing that the program works...
		jTreeCluster.setEnabled(false);
		// create a linked list. here we will add all keywords from the selection to the
		// first level paremt
		LinkedList<String> kws = new LinkedList<>();
		// start from the second element, since we don't want the root-element
		// and copy all nodes from this path to the linked list
		for (int cnt = 1; cnt < tp.getPathCount(); cnt++) {
			kws.add(tp.getPathComponent(cnt).toString());
		}
		// now copy all related keywords into an array
		String[] keywords = kws.toArray(new String[kws.size()]);
		// copy all keywords to the string builder as well, so we can set the relations
		// as status msg label text
		StringBuilder sb = new StringBuilder("");
		// and convert them to strings
		for (String kwsingle : keywords) {
			sb.append(kwsingle).append(" + ");
		}
		// Trim string
		if (sb.length() > 3) {
			sb.setLength(sb.length() - 3);
		}
		// and set the relations as status message label
		if (!settings.isMacStyle()) {
			statusMsgLabel.setText(sb.toString());
		}
		jTreeCluster.setToolTipText(sb.toString());
		// remember this relation as "global variable"
		lastClusterRelationKeywords = sb.toString();
		// clear list
		clusterList.clear();
		// get count of entries
		int count = data.getCount(Daten.ZKNCOUNT);
		// go through all entries and get those entries
		// that contain *all* of the keywords in the array (true as parameter)
		for (int cnt = 1; cnt <= count; cnt++) {
			if (data.existsInKeywords(keywords, cnt, true, false)) {
				clusterList.add(String.valueOf(cnt));
			}
		}
		// get selected node again
		node = (DefaultMutableTreeNode) jTreeCluster.getLastSelectedPathComponent();
		// remove all children
		node.removeAllChildren();
		// save the current size of the linked list. we will add all new keywords to
		// this
		// list, but we may not set *all* keywords as children of "node", but only the
		// new ones - which start in this list at the index "previousSize".
		int previousSize = kws.size();
		for (String clusterList1 : clusterList) {
			// get each entry-number of the list
			int nr = Integer.parseInt(clusterList1);
			// get the entry's keywords
			String[] entrykws = data.getKeywords(nr);
			// go through all keywords of this array
			// if the keyword does not already exist in the list, add it
			if (entrykws != null) {
				for (String ek : entrykws) {
					if (-1 == kws.indexOf(ek)) {
						kws.add(ek);
					}
				}
			}
		}
		// if we have any new keywords, i.e. children, go on
		if (kws.size() > previousSize) {
			// create string array with the size of the new children
			String[] children = new String[kws.size() - previousSize];
			// receive all new element of that list
			// and copy them to the string array
			for (int cnt = 0; cnt < (kws.size() - previousSize); cnt++) {
				children[cnt] = kws.get(cnt + previousSize);
			}
			// sort the array
			if (children != null && children.length > 0) {
				Arrays.sort(children, new Comparer());
				// add each item as child of note
				// create and add a new child
				for (String c : children) {
					node.add(new DefaultMutableTreeNode(c));
				}
			}
		}
		// set cluster links
		jEditorPaneClusterEntries
				.setText(HtmlUbbUtil.getLinkedEntriesAsHtml(data, settings, clusterList, "clusterListText"));
		// enable tree, showing that the the method has finihsed
		jTreeCluster.setEnabled(true);
		jTreeCluster.requestFocusInWindow();
	}

	/**
	 * This method displays the all entries' titles using a background task. After
	 * the task finishes, all titles and their entry numbers in the main data file
	 * (zknfile) are displayed in the JTable of the JTabbedPane.
	 */
	private void updateTitlesTab() {
		// If no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}

		// Reset status text, since the amount of titles is equal to the amount of
		// entries.
		statusMsgLabel.setText("");

		showTabMenu(viewMenuTitles);

		// If Daten says it is up to date, do nothing.
		if (data.isTitlelistUpToDate()) {
			return;
		}

		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			JFrame parentFrame = getFrame();
			taskDlg = new TaskProgressDialog(parentFrame, TaskProgressDialog.TASK_SHOWTITLES, data, null, /*
																											 * only
																											 * needed
																											 * for
																											 * keywords
																											 */
					null, /* only needed for authors */
					null, /* only needed for attachments */
					false, /* only needed for keywords */
					0, /* only needed for authors */
					(DefaultTableModel) jTableTitles.getModel());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(parentFrame);
		}
		waitForTaskDialog();

		// Reset filtered title list.
		linkedtitlelist = null;
		// Refresh jButton starts disabled.
		jButtonRefreshTitles.setEnabled(false);
		// Enable filter jTextField only if we have more than 1 element in the jtable.
		jTextFieldFilterTitles.setEnabled(jTableTitles.getRowCount() > 0);
	}

	/**
	 * This Action creates the links between of the currently displayed entry with
	 * all other enries, based on matching keywords. These hyperlinks are stored in
	 * the JTable of the JTabbedPane
	 *
	 * @return the background task
	 */
	@Action
	public Task<?, ?> createLinks() {
		return new createLinksTask(
				Application.getInstance(ZettelkastenApp.class));
	}

	/**
	 * This method adds the content of the clipboard as new entry. The new entry is
	 * created automatically, where the clipboard content is used as entry content.
	 * No edit-window will be opened.
	 */
	@Action
	public void quickNewEntry() {
		// get the clipbaord
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			// retrieve clipboard content
			Transferable content = clipboard.getContents(null);
			// if we have any content, create new entry...
			if (content != null) {
				// first, copy clipboard to string
				String text = content.getTransferData(DataFlavor.stringFlavor).toString().trim();
				// identify new-line/line-separator-char
				String sepval = (text.contains("\r\n")) ? "\r\n" : "\n";
				// if we have any leading new lines, remove these
//                while (text.startsWith(System.lineSeparator())) {
//                    text = text.substring(System.lineSeparator().length());
//                }
				while (text.startsWith(sepval)) {
					text = text.substring(sepval.length());
				}
				// add text as new entry
				data.addEntry("", text, null, null, "", null, Tools.getTimeStamp(), -1);
				// and titles might be out of date now as well...
				data.setTitlelistUpToDate(false);
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Entry save finished.");
				// update the dislay...
				updateDisplay();
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Display updated.");
				// and create a backup...
				makeAutoBackup();
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Autobackup finished (if necessary).");
			}
		} catch (IllegalStateException | IOException | UnsupportedFlavorException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	/**
	 * This method adds the content of the clipboard as new entry. The first line of
	 * the clipboard content will be automatically set as entry's title. The new
	 * entry is created automatically, where the clipboard content is used as entry
	 * content. No edit-window will be opened.
	 */
	@Action
	public void quickNewEntryWithTitle() {
		// get the clipbaord
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			// retrieve clipboard content
			Transferable content = clipboard.getContents(null);
			// if we have any content, create new entry...
			if (content != null) {
				// first, copy clipboard to string
				String text = content.getTransferData(DataFlavor.stringFlavor).toString().trim();
				// identify new-line/line-separator-char
				String sepval = (text.contains("\r\n")) ? "\r\n" : "\n";
				String title = "";
				// if we have any leading new lines, remove these
				while (text.startsWith(sepval)) {
					text = text.substring(sepval.length());
				}
				// retrieve first line
				int pos = text.indexOf(sepval);
				// if we have more than one line, a first line is found
				if (pos != -1) {
					// cut first line and set it as title
					title = text.substring(0, pos).trim();
					// remove first line from text
					text = text.substring(pos);
					// if we have any leading new lines, remove these
					while (text.startsWith(sepval)) {
						text = text.substring(sepval.length());
					}
				}
//                // if we have any leading new lines, remove these
//                while (text.startsWith(System.lineSeparator())) {
//                    text = text.substring(System.lineSeparator().length());
//                }
//                // retrieve first line
//                int pos = text.indexOf(System.lineSeparator());
//                // if we have more than one line, a first line is found
//                if (pos!=-1) {
//                    // cut first line and set it as title
//                    title = text.substring(0, pos).trim();
//                    // remove first line from text
//                    text = text.substring(pos);
//                    // if we have any leading new lines, remove these
//                    while (text.startsWith(System.lineSeparator())) {
//                        text = text.substring(System.lineSeparator().length());
//                    }
//                }
				// add text as new entry
				data.addEntry(title, text, null, null, "", null, Tools.getTimeStamp(), -1);
				// and titles might be out of date now as well...
				data.setTitlelistUpToDate(false);
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Entry save finished.");
				// update the dislay...
				updateDisplay();
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Display updated.");
				// and create a backup...
				makeAutoBackup();
				// Tell about success
				Constants.zknlogger.log(Level.INFO, "Autobackup finished (if necessary).");
			}
		} catch (IllegalStateException | IOException | UnsupportedFlavorException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	/**
	 * This method inits the system tray. if supported, the program's window does
	 * not deiconfy/minimize to the taskbar, but hides and displays an icon in the
	 * system tray instead.
	 */
	private void initSystemTray() {
		// if systemtray is not supported, do nothing.
		if (!SystemTray.isSupported()) {
			return;
		}
		// create tray-icon with tooltip
		trayIcon = new TrayIcon((new ImageIcon(
				Application.getInstance(ZettelkastenApp.class)
						.getClass().getResource("/de/danielluedecke/zettelkasten/resources/icons/zkn3_16x16.png"),
				"Zettelkasten")).getImage());
		// retrieve system tray
		tray = SystemTray.getSystemTray();
		// Try to add the tray icon to the systray
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			Constants.zknlogger.log(Level.WARNING, "Tray Icon could not be added.");
			return;
		}
		// if tray icon was successfully added, add tooltip
		trayIcon.setToolTip("Zettelkasten");
		// and mouse listener, so the window will be restored when the user clicks on
		// the tray icon
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				// set main frame visible
				getFrame().setVisible(true);
				// restore frame state to normal state
				getFrame().setExtendedState(Frame.NORMAL);
				// if we have a tray icon, remove it
				if (tray != null) {
					// clear popup menu
					trayIcon.setPopupMenu(null);
					// remove tray icon
					tray.remove(trayIcon);
				}
				// and say that tray icon is currently not installed
				trayIconInstalled = false;
			}
		});
		trayIconInstalled = true;
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// call the general exit-handler from the desktop-application-api
		// here we do all the stuff we need when exiting the application
		ZettelkastenApp.getApplication().exit();
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// if minimizing to tray is activated, show tray icon
		if (settings.getMinimizeToTray()) {
			// when window is minimized to tray, init the system tray icon
			initSystemTray();
			// and hide the window, when try icon was successfully installed
			if (trayIconInstalled) {
				getFrame().setVisible(false);
			}
		}
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
		// get transferable
		Transferable tr = dtde.getTransferable();
		try {
			// check whether we have files dropped into textarea
			if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)
					|| tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || tr.isDataFlavorSupported(urlFlavor)) {
				// drag&drop was link action
				dtde.acceptDrop(DnDConstants.ACTION_LINK);
				// continue here if we have dropped an URLs from the webbrowser
				// in some cases, files are considered as URL. make sure that no
				// transfer data starting with "file" is accepted as URL
				if (tr.isDataFlavorSupported(urlFlavor)
						&& !tr.getTransferData(urlFlavor).toString().startsWith("file:")) {
					// retrieve url
					URL url = (URL) tr.getTransferData(urlFlavor);
					// TODO abfrage, was kommen soll, ob import oder anhang
					// importWebPage(url);
					// else add the text to the keyword-list (JList)
					addAttachments(new String[] { url.toString() });
				} else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					// retrieve list of dropped files
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
					// check for valid values
					if (files != null && files.size() > 0) {
						// create array
						File[] atts = new File[files.size()];
						// iterate drop files and convert to array
						for (int i = 0; i < files.size(); i++) {
							atts[i] = new File(files.get(i).toString());
						}
						// insert attachments
						boolean added = FileOperationsUtil.insertAttachments(data, settings, getFrame(), atts, null);
						// add attachments
						if (added) {
							// retrieve final added attachments
							String[] finalatts = FileOperationsUtil.getAddedAttachments();
							// checkfor valid values
							if (finalatts != null && finalatts.length > 0) {
								addAttachments(finalatts);
							}
						}
					}
				} else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					// get drop data
					String tfd = (String) tr.getTransferData(DataFlavor.stringFlavor);
					// check for valid values
					if (tfd != null && !tfd.isEmpty()) {
						// retrieve drag source info
						String[] sourceinformation = tfd.split("\\n");
						// check if authors have been dropped
						if (sourceinformation[0].equals(Constants.DRAG_SOURCE_TYPE_AUTHORS)) {
							// new array
							String aus[] = new String[sourceinformation.length - 1];
							// prepare new array without drag info
							for (int i = 1; i < sourceinformation.length; i++) {
								aus[i - 1] = sourceinformation[i];
							}
							// add authors
							addAuthorToList(aus);
						}
					}
				}
				dtde.getDropTargetContext().dropComplete(true);
			} else {
				Constants.zknlogger.log(Level.WARNING, "DataFlavor is not supported, drop rejected!");
				dtde.rejectDrop();
			}
		} catch (IOException | UnsupportedFlavorException ex) {
			Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
			dtde.rejectDrop();
		}
	}

    // Setter method for EditorFrame
    public void setEditorFrame(EditorFrame editorFrame) {
        this.editEntryDlg = editorFrame;
    }

    // Getter method for EditorFrame
    public EditorFrame getEditorFrame() {
        return editEntryDlg;
    }

	public void setContext(ApplicationContext applicationContext) {
		this.context = applicationContext;
	}


	/**
	 * This Action creates the links between of the currently displayed entry with
	 * all other entries, based on matching keywords. These hyperlinks are stored in
	 * the JTable of the JTabbedPane
	 *
	 * @return the background task
	 */
	private class createLinksTask extends Task<Object, Void> {

		/**
		 * This variable stores the table data of the links-list. We use this variable
		 * in the "createLinksTask", because when we add the values to the tables
		 * directly (via tablemodel) and the user skips through the entries before the
		 * task has finished, the table contains wrong values. so, within the task this
		 * list is filled, and only when the task has finished, we copy this list to the
		 * table.
		 */
		private ArrayList<Object[]> linkedlinkslist;

		createLinksTask(Application app) {
			// Runs on the EDT. Copy GUI state that
			// doInBackground() depends on from parameters
			// to createLinksTask fields, here.
			super(app);
			cLinksTask = this;
		}

		@Override
		protected Object doInBackground() {
			// Your Task's code here. This method runs
			// on a background thread, so don't reference
			// the Swing GUI from here.

			// tell program that this thread is running...
			createLinksIsRunning = true;
			// variable that indicates whether a match of keywords was found
			boolean found;
			int cnt;
			// get the length of the data file, i.e. the amount of entrys
			final int len = data.getCount(Daten.ZKNCOUNT);
			// get the keyword index numbers of the current entry
			String[] kws = data.getKeywordsOfActivatedEntry();
			// if we have any keywords, go on
			if (kws != null) {
				// create new instance of that variable
				linkedlinkslist = new ArrayList<>();
				// iterate all entrys of the zettelkasten
				for (cnt = 1; cnt <= len; cnt++) {
					// leave out the comparison of the current entry with itself
					if (cnt == data.getActivatedEntryNumber()) {
						continue;
					}
					// init the found indicator
					found = false;
					// iterate all keywords of current entry
					for (String k : kws) {
						// look for occurences of any of the current keywords
						if (data.existsInKeywords(k, cnt, false)) {
							// set found-indicator
							found = true;
							break;
						}
					}
					// if we have a match, connect entries, i.e. display the number and title of
					// the linked entries in the table of the tabbed pane
					if (found) {
						// create a new object
						Object[] ob = new Object[4];
						// store the information in that object
						ob[0] = cnt;
						ob[1] = data.getZettelTitle(cnt);
						ob[2] = data.getLinkStrength(data.getActivatedEntryNumber(), cnt);
						ob[3] = data.getZettelRating(cnt);
						// and add that content as a new row to the table
						linkedlinkslist.add(ob);
					}
				}
			}

			return null;
		}

		@Override
		protected void succeeded(Object result) {
			// Runs on the EDT. Update the GUI based on
			// the result computed by doInBackground().
			DefaultTableModel tm = (DefaultTableModel) jTableLinks.getModel();
			// reset the table
			tm.setRowCount(0);
			// check whether we have any entries at all...
			if (linkedlinkslist != null) {
				// create iterator for linked list
				Iterator<Object[]> i = linkedlinkslist.iterator();
				// go through linked list and add all objects to the table model
				try {
					while (i.hasNext()) {
						tm.addRow(i.next());
					}
				} catch (ConcurrentModificationException e) {
					// reset the table when we have overlappings threads
					tm.setRowCount(0);
				}
			}
			// display manual links now...
			updateManualLinksTable();
		}

		@Override
		protected void finished() {
			super.finished();
			cLinksTask = null;
			createLinksIsRunning = false;
			// show/enable viewmenu, if we have at least one entry...
			if ((jTableLinks.getRowCount() > 0) && (TAB_LINKS == jTabbedPaneMain.getSelectedIndex())) {
				showTabMenu(viewMenuLinks);
			}
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTableLinks.getRowCount()) + " "
					+ Application
							.getInstance(ZettelkastenApp.class).getContext()
							.getResourceMap(ZettelkastenView.class).getString("statusTextLinks")
					+ ")");
		}
	}

	private void fillLuhmannNumbers(DefaultMutableTreeNode node, EntryID nodeEntry, EntryID selectedEntry) {
		// Update selectedLuhmannNode if we have found it.
		if (nodeEntry.equals(selectedEntry)) {
			selectedLuhmannNode = (DefaultMutableTreeNode) node;
		}

		String subEntriesCsv = data.getSubEntriesCsv(nodeEntry.asInt());
		if (subEntriesCsv.isEmpty()) {
			// Nothing to add.
			return;
		}
		List<EntryID> subEntries = EntryIDUtils.csvToEntryIDList(subEntriesCsv);
		for (EntryID subEntry : subEntries) {
			// Create new subEntry node.
			String title = TreeUtil.getEntryDisplayText(data, settings.getShowLuhmannEntryNumber(), subEntry);
			DefaultMutableTreeNode loopNode = new DefaultMutableTreeNode(
					new TreeUserObject(title, subEntry.asString(), /* collapsed= */true));

			// Add to last position.
			node.insert(loopNode, node.getChildCount());

			// Recursively: create sub-entries of this new node.
			fillLuhmannNumbers(loopNode, subEntry, selectedEntry);
		}
	}

	/**
	 * This method, called when the user enters "m" as input in the entry-textfield
	 * in the lower statusbar, toggles a timer that display the current memory usage
	 * of the application. this timer is executes every 5 seconds, so the
	 * memory-usage is updated each 5 seconds. <br>
	 * <br>
	 * If the user types "m" for the second time, the timer is stopped.
	 */
	public void toggleMemoryTimer() {
		// check whether we have a already running timer...
		if (null == memoryDisplayTimer) {
			// if not, create new one
			memoryDisplayTimer = new Timer();
			// this timer should start immediately and update every 5 seconds
			memoryDisplayTimer.schedule(new MemoryTimer(), 0, 5000);
			// show memory-usage-label
			jLabelMemory.setVisible(true);
			// display memory usage
			calculateMemoryUsage();
			// switch on toggle
			settings.isMemoryUsageLogged = true;
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logging swicthed on.");
		} else {
			// if timer was running, cancel it
			memoryDisplayTimer.cancel();
			// purge it from the task-list
			memoryDisplayTimer.purge();
			// hide label
			jLabelMemory.setVisible(false);
			// free timer-object
			memoryDisplayTimer = null;
			// switch off toggle
			settings.isMemoryUsageLogged = false;
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logging swicthed off.");
		}
		// show current entry number again
		jTextFieldEntryNumber.setText(String.valueOf(data.getActivatedEntryNumber()));
	}

	private void terminateTimers() {
		if (memoryDisplayTimer != null) {
			// if timer was running, cancel it
			memoryDisplayTimer.cancel();
			// purge it from the task-list
			memoryDisplayTimer.purge();
			// hide label
			jLabelMemory.setVisible(false);
			// free timer-object
			memoryDisplayTimer = null;
		}
		if (makeAutoBackupTimer != null) {
			// if timer was running, cancel it
			makeAutoBackupTimer.cancel();
			// purge it from the task-list
			makeAutoBackupTimer.purge();
			// free timer-object
			makeAutoBackupTimer = null;
			// reset counter
			memoryLogCounter = 0;
		}
		if (flashErrorIconTimer != null) {
			// if timer was running, cancel it
			flashErrorIconTimer.cancel();
			// purge it from the task-list
			flashErrorIconTimer.purge();
			// free timer-object
			flashErrorIconTimer = null;
		}
	}

	private void calculateMemoryUsage() {
		// calculate memory usage from the application
		long totalMem = Runtime.getRuntime().totalMemory();
		// calculate the allocated memory from the jvm
		long freeMem = Runtime.getRuntime().freeMemory();
		// calculate the maximum system memory
		long maxMem = Runtime.getRuntime().maxMemory();
		// convert values to string
		String freeMemory = String.valueOf((totalMem - freeMem) / 1048576);
		String totalMemory = String.valueOf(totalMem / 1048576);
		String maximalMemory = String.valueOf(maxMem / 1048576);
		// display memory-usage
		jLabelMemory.setText(freeMemory + "MB / " + totalMemory + "MB (max. " + maximalMemory + "MB)");
		// increase log-counter. we want to update the *display* of the memory-usage
		// every
		// 5 seconds, but logging it to a file only each minute
		memoryLogCounter++;
		// when 12 ticks are over, we have one minute passed, so log info now...
		if (memoryLogCounter >= 12) {
			// log memory usage
			Constants.zknlogger.log(Level.INFO, "Memory-Usage: {0}MB / {1}MB (max. {2}MB)",
					new Object[] { freeMemory, totalMemory, maximalMemory });
			// reset counter
			memoryLogCounter = 0;
		}
	}

	private void flashErrorIcon() {
		errorIconIsVisible = !errorIconIsVisible;
		if (errorIconIsVisible) {
			statusErrorButton.setIcon(Constants.errorIcon);
		} else {
			statusErrorButton.setIcon(Constants.errorIconFaded);
		}

	}

	/**
	 * This class starts a timer that displays the memory-usage of the zettelkasten
	 */
	class MemoryTimer extends TimerTask {

		@Override
		public void run() {
			// display memory usage
			calculateMemoryUsage();
		}
	}

	/**
	 * This class starts a timer that displays the memory-usage of the zettelkasten
	 */
	class ErrorIconTimer extends TimerTask {

		@Override
		public void run() {
			// make update-icon flash
			flashErrorIcon();
		}
	}

	/**
	 * This class starts a timer that displays the memory-usage of the zettelkasten
	 */
	class AutoBackupTimer extends TimerTask {

		@Override
		public void run() {
			// create autobackup
			makeAutoBackup();
		}
	}

	private EntryID getRootEntryForLuhmannTree() {
		int rootEntry = -1;
		if (settings.getShowAllLuhmann()) {
			rootEntry = data.findParentlLuhmann(data.getActivatedEntryNumber(), /* firstParent= */false);

		}
		if (rootEntry == -1) {
			rootEntry = data.getActivatedEntryNumber();
		}
		return new EntryID(rootEntry);
	}

	private void prepareNoteSequencesJTreePane() {
		DefaultTreeModel dtm = (DefaultTreeModel) jTreeLuhmann.getModel();

		// Save collapsed state if same root as the previous tree.
		DefaultMutableTreeNode previousRoot = (DefaultMutableTreeNode) dtm.getRoot();
		EntryID newRootEntry = getRootEntryForLuhmannTree();
		if (previousRoot != null && TreeUtil.getEntryID(previousRoot).equals(newRootEntry)) {
			TreeUtil.saveCollapsedNodes(jTreeLuhmann);
		} else {
			TreeUtil.resetCollapsedNodes();
		}

		// We always reset the tree.
		dtm.setRoot(null);

		// Prepare new root.
		String title = TreeUtil.getEntryDisplayText(data, settings.getShowLuhmannEntryNumber(), newRootEntry);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				new TreeUserObject(title, newRootEntry.asString(), /* collapsed= */false));
		dtm.setRoot(root);

		// Init selectedLuhmannNode.
		selectedLuhmannNode = null;
		// Populate the whole tree rooted at root.
		fillLuhmannNumbers(root, newRootEntry, new EntryID(displayedZettel));

		TreeUtil.setExpandLevel(settings.getLuhmannExpandLevel());
		TreeUtil.expandAllTrees(jTreeLuhmann);

		// If Note Sequences are shown, put focus on them.
		jTreeLuhmann.requestFocusInWindow();

		// Select and scroll to visible current entry.
		if (selectedLuhmannNode != null) {
			TreePath tp = new TreePath(selectedLuhmannNode.getPath());
			jTreeLuhmann.setSelectionPath(tp);
			jTreeLuhmann.scrollPathToVisible(tp);
		}
	}

	/*
	 * Prepares the "Parents pane", which has links to the parents of the current
	 * activated entry.
	 */
	private void prepareNoteSequencesParentsPane() {
		isFollowerList.clear();

		EntryID shownEntry = displayedZettel != -1 ? new EntryID(displayedZettel)
				: new EntryID(data.getActivatedEntryNumber());

		// Go through the complete data set to get the list of parents of the
		// activatedEntry.
		for (int loopEntryId = 1; loopEntryId <= data.getCount(Daten.ZKNCOUNT); loopEntryId++) {
			List<EntryID> subEntries = EntryIDUtils.csvToEntryIDList(data.getSubEntriesCsv(loopEntryId));
			for (EntryID subEntry : subEntries) {
				if (subEntry.equals(shownEntry)) {
					try {
						isFollowerList.add(String.valueOf(loopEntryId));
						// No need to look further at its siblings.
						break;
					} catch (ConcurrentModificationException ex) {
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						System.out.println("Fehler Folgezettel UI!");
					}
				}
			}
		}

		// Update Pane to show parents.
		jEditorPaneIsFollower
				.setText(HtmlUbbUtil.getLinkedEntriesAsHtml(data, settings, isFollowerList, "isFollowerText"));
	}

	/**
	 * Action with background task, which imorts the file
	 *
	 * @return
	 */
	@Action
	public Task<?, ?> autoBackupTask() {
		return new AutoBackupTask(
				Application.getInstance(ZettelkastenApp.class),
				this, statusMsgLabel, data, desktop, settings, searchRequests, settings.getSynonyms(), bookmarks,
				bibtex);
	}

	/**
	 * This task creates the related (clustered) keywords from the current entry.
	 * Therefore, the current entry's keywords are retrieved. Then, in each entry of
	 * the data-file we look for occurences of the current entry's keywords. If we
	 * found any matches, the related entry's other keywords are added to the final
	 * keyword-list.
	 *
	 * @return the background task
	 */
	@Action
	public Task<?, ?> clusterTask() {
		return new createClusterTask(
				Application.getInstance(ZettelkastenApp.class));
	}

	/**
	 * This task creates the related (clustered) keywords from the current entry.
	 * Therefore, the current entry's keywords are retrieved. Then, in each entry of
	 * the data-file we look for occurences of the current entry's keywords. If we
	 * found any matches, the related entry's other keywords are added to the final
	 * keyword-list.
	 *
	 * @return the background task
	 */
	private class createClusterTask extends Task<Object, Void> {

		// create link list for the keywords and related keywords

		LinkedList<String> lwsClusterTask = new LinkedList<>();

		createClusterTask(Application app) {
			// Runs on the EDT. Copy GUI state that
			// doInBackground() depends on from parameters
			// to createLinksTask fields, here.
			super(app);
		}

		@Override
		protected Object doInBackground() {
			// Your Task's code here. This method runs
			// on a background thread, so don't reference
			// the Swing GUI from here.
			//
			// tell programm that the thread is running
			createClusterIsRunning = true;
			// get current entries keywords
			String[] cws = data.getKeywordsOfActivatedEntry();
			// if we have any current keywords, go on
			if (cws != null) {
				// get amount of entries
				int count = data.getCount(Daten.ZKNCOUNT);
				// add all current keywords and their related keywords to
				// the linked list
				for (String c : cws) {
					// add each curent keyword to cluster list
					lwsClusterTask.add(c);
					// now go through all entries
					for (int cnt = 1; cnt <= count; cnt++) {
						// check whether current keywords exits in entry
						if (data.existsInKeywords(c, cnt, false)) {
							// if yes, retrieve entry's keywords
							String[] newkws = data.getKeywords(cnt);
							// check whether we have any keywords at all
							if (newkws != null) {
								// if so, iterate keywords
								for (String n : newkws) {
									// and add each keyword to the link list, if it's not
									// already in that list...
									if (!lwsClusterTask.contains(n)) {
										lwsClusterTask.add(n);
									}
								}
							}
						}
					}
				}
				// sort the array
				Collections.sort(lwsClusterTask, new Comparer());
			}
			// we have no filtered list...
			linkedclusterlist = false;
			// indicate that the cluster list is up to date...
			data.setClusterlistUpToDate(true);

			return null;
		}

		@Override
		protected void succeeded(Object result) {
			// Runs on the EDT. Update the GUI based on
			// the result computed by doInBackground().
			//
			// get the treemodel
			DefaultTreeModel dtm = (DefaultTreeModel) jTreeCluster.getModel();
			// set this as root node. we don't need to care about this, since the
			// root is not visible.
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("ZKN3-Cluster");
			dtm.setRoot(root);
			// if we have any keywords, set them to the list
			if (lwsClusterTask.size() > 0) {
				// create iterator
				Iterator<String> i = lwsClusterTask.iterator();
				// and add all items to the list
				while (i.hasNext()) {
					root.add(new DefaultMutableTreeNode(i.next()));
				}
				// completely expand the jTree
				TreeUtil.expandAllTrees(true, jTreeCluster);
			}
		}

		@Override
		protected void finished() {
			super.finished();
			createClusterIsRunning = false;
			jCheckBoxCluster.setEnabled(true);
			// enable textfield only if we have more than 1 element in the jTree
			jTextFieldFilterCluster.setEnabled(jTreeCluster.getRowCount() > 1);
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTreeCluster.getRowCount()) + " "
					+ Application
							.getInstance(ZettelkastenApp.class).getContext()
							.getResourceMap(ZettelkastenView.class).getString("statusTextKeywords")
					+ ")");
			jTreeCluster.setToolTipText(null);
		}
	}

	/**
	 * This Action creates the links between of the currently displayed entry with
	 * all other enries, based on matching keywords. These hyperlinks are stored in
	 * the JTable of the JTabbedPane.<br>
	 * <br>
	 * Unlike the createLinks-task, this task does not look for any single
	 * occurences of keywords, but of logical-combination of the selected keywords.
	 * I.e., whether <i>all</i> or <i>at least one</i> of the selected keywords
	 * is/are part of another entry's keywords-list.
	 *
	 * @return the background task
	 */
	@Action
	public Task<?, ?> createFilterLinks() {
		return new createFilterLinksTask(
				Application.getInstance(ZettelkastenApp.class));
	}

	/**
	 * This Action creates the links between of the currently displayed entry with
	 * all other enries, based on matching keywords. These hyperlinks are stored in
	 * the JTable of the JTabbedPane.<br>
	 * <br>
	 * Unlike the createLinks-task, this task does not look for any single
	 * occurences of keywords, but of logical-combination of the selected keywords.
	 * I.e., whether <i>all</i> or <i>at least one</i> of the selected keywords
	 * is/are part of another entry's keywords-list.
	 *
	 * @return the background task
	 */
	private class createFilterLinksTask extends Task<Object, Void> {

		/**
		 * This variable stores the table data of the filtered links-list. We use this
		 * variable in the "createLinksTask", because when we add the values to the
		 * tables directly (via tablemodel) and the user skips through the entries
		 * before the task has finished, the table contains wrong values. so, within the
		 * task this list is filled, and only when the task has finished, we copy this
		 * list to the table.
		 */
		private ArrayList<Object[]> linkedfilteredlinkslist;

		createFilterLinksTask(Application app) {
			// Runs on the EDT. Copy GUI state that
			// doInBackground() depends on from parameters
			// to createLinksTask fields, here.
			super(app);
		}

		@Override
		protected Object doInBackground() {
			// Your Task's code here. This method runs
			// on a background thread, so don't reference
			// the Swing GUI from here.

			// tell program that this thread is running...
			createFilterLinksIsRunning = true;
			// variable that indicates whether a match of keywords was found
			boolean found;
			int cnt;
			// create string array for selected keyword-values
			String[] kws = retrieveSelectedKeywordsFromList();
			// if we have no selection, return null. this happens, when the view is
			// refreshed and a value
			// in the jListEntryKeywords is selected - the jList then loses somehow the
			// selectiob, so this
			// task is startet, although no keyword is selected...
			if (null == kws) {
				return null;
			}
			// get the length of the data file, i.e. the amount of entrys
			final int len = data.getCount(Daten.ZKNCOUNT);
			// get setting, whether we have logical-and or logical-or-search
			boolean log_and = settings.getLogKeywordlist().equalsIgnoreCase(Settings.SETTING_LOGKEYWORDLIST_AND);
			// create new instance of that variable
			linkedfilteredlinkslist = new ArrayList<>();
			// iterate all entrys of the zettelkasten
			for (cnt = 1; cnt <= len; cnt++) {
				// leave out the comparison of the current entry with itself
				if (cnt == data.getActivatedEntryNumber()) {
					continue;
				}
				// init the found indicator
				found = false;
				// if we have logical-or, at least one of the keywords must exist.
				// so go through all selected keywords and look for occurences
				if (data.existsInKeywords(kws, cnt, log_and, false)) {
					found = true;
				}
				// if we have a match, connect entries, i.e. display the number and title of
				// the linked entries in the table of the tabbed pane
				if (found) {
					// create a new object
					Object[] ob = new Object[3];
					// store the information in that object
					ob[0] = cnt;
					ob[1] = data.getZettelTitle(cnt);
					ob[2] = data.getLinkStrength(data.getActivatedEntryNumber(), cnt);
					// and add that content to the linked list
					linkedfilteredlinkslist.add(ob);
				}
			}

			return null;
		}

		@Override
		protected void succeeded(Object result) {
			// Runs on the EDT. Update the GUI based on
			// the result computed by doInBackground().
			DefaultTableModel tm = (DefaultTableModel) jTableLinks.getModel();
			// reset the table
			tm.setRowCount(0);
			// check whether we have any entries at all...
			if (linkedfilteredlinkslist != null) {
				// create iterator for linked list
				Iterator<Object[]> i = linkedfilteredlinkslist.iterator();
				// go through linked list and add all objects to the table model
				try {
					while (i.hasNext()) {
						tm.addRow(i.next());
					}
				} catch (ConcurrentModificationException e) {
					// reset the table when we have overlappings threads
					tm.setRowCount(0);
				}
			}
			// show amount of entries
			statusMsgLabel.setText("(" + String.valueOf(jTableLinks.getRowCount()) + " "
					+ Application
							.getInstance(ZettelkastenApp.class).getContext()
							.getResourceMap(ZettelkastenView.class).getString("statusTextLinks")
					+ ")");
		}

		@Override
		protected void finished() {
			super.finished();
			createFilterLinksIsRunning = false;
		}
	}

	/**
	 * This method opens the preferences-window (settings-window).
	 */
	@Action
	public void settingsWindow() {
		if (settingsDlg == null) {
			settingsDlg = new CSettingsDlg(getFrame(), settings, data, settings.getAutoKorrektur(),
					settings.getSynonyms(), settings.getStenoData());
			settingsDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(settingsDlg);
		// check for changes to synonyms
		if (settingsDlg.isSynModified()) {
			// update indicator for autobackup
			backupNecessary(true);
			setSaveEnabled(true);
			// check whether we have to update tabbed pane
			if (!data.isKeywordlistUpToDate()) {
				updateDisplay();
			}
		}
		// check whether only entry display should be updated
		if (settingsDlg.getDisplayUpdate()) {
			updateDisplay();
			if (desktopDlg != null) {
				desktopDlg.updateEntriesAfterEditing();
			}
			if (searchResultsDlg != null) {
				searchResultsDlg.updateDisplayAfterEditing();
			}
			// set background color
			jEditorPaneEntry.setBackground(new Color(Integer.parseInt(settings.getMainBackgroundColor(), 16)));
		}
		// when we have any changes in visual settings, update display
		if (settingsDlg.getNeedsUpdate()) {
			// update tables, e.g. show new cellspacing or grids
			initTables();
			// update toolbar, to show new icons if necessary
			initToolbarIcons(false);
			if (searchResultsDlg != null) {
				searchResultsDlg.initToolbarIcons();
			}
			if (desktopDlg != null) {
				desktopDlg.initToolbarIcons();
			}
			if (editEntryDlg != null) {
				editEntryDlg.initToolbarIcons();
			}
			// set background color
			jEditorPaneEntry.setBackground(new Color(Integer.parseInt(settings.getMainBackgroundColor(), 16)));
			updateDisplay();
		}
		// Show 'needs restart' message if necessary.
		if (settingsDlg.getNeedsLafUpdate()) {
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("needsRestartMsg"),
					getResourceMap().getString("needsRestartTitle"), JOptionPane.PLAIN_MESSAGE);
		}
		// when we have any changes in visual settings, tell desktop to update display
		if (desktopDlg != null) {
			desktopDlg.setNeedsUpdate(true);
		}
		// check for correct saving of settings
		if (!settingsDlg.isSaveSettingsOk()) {
			// show error log
			showErrorIcon();
		}
		settingsDlg.dispose();
		settingsDlg = null;
	}

	/**
	 * This method searches for entries that contain at least on of the selected
	 * entries (log-or) from the jTableAuthors or jTableKewords, and adds those
	 * entries as manual links to the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addManLinksLogOr() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addManLinksFromAuthors(Constants.LOG_OR);
			break;
		case TAB_KEYWORDS:
			addManLinksFromKeywords(Constants.LOG_OR);
			break;
		}
	}

	/**
	 * This method searches for entries that contain <i>all</i> selected entries
	 * (log-and) from the jTableAuthors or jTableKeywords, and adds those entries as
	 * manual links to the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addManLinksLogAnd() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addManLinksFromAuthors(Constants.LOG_AND);
			break;
		case TAB_KEYWORDS:
			addManLinksFromKeywords(Constants.LOG_AND);
			break;
		}
	}

	/**
	 * This method searches for entries that contain the selected keywords from the
	 * jTableKeywords, and adds those entries as luhmann-numbers (follower) to the
	 * current entry. <br>
	 * <br>
	 * See {@link #addLuhmannLogOr() addLuhmannLogOr()} and
	 * {@link #addLuhmannLogAnd() addLuhmannLogAnd()} for more details.
	 *
	 * @param log the logical combination of the search, whether at least one
	 *            keywords should exist (log-or) or if only entries are added that
	 *            contain all keywords (log-and)
	 */
	private void addLuhmannFromKeywords(int log) {
		// search for all entries that contain the selected keywords
		// and add them as luhmann-numbers
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0), // string-array with search
																								// terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_LUHMANN, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
		// update display
		updateDisplay();
	}

	/**
	 * This method searches for entries that contain the selected keywords from the
	 * jTableKeywords, and adds those entries as manual links (follower) to the
	 * current entry. <br>
	 * <br>
	 * See {@link #addManLinksLogOr() addManLinksLogOr()} and
	 * {@link #addManLinksLogAnd() addManLinksLogAnd()} for more details.
	 *
	 * @param log the logical combination of the search, whether at least one
	 *            keywords should exist (log-or) or if only entries are added that
	 *            contain all keywords (log-and)
	 */
	private void addManLinksFromKeywords(int log) {
		// search for all entries that contain the selected keywords
		// and add them as manual links
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0), // string-array with search
																								// terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_MANLINK, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
		// update display
		updateDisplay();
	}

	/**
	 * This method adds the selected entries from the the current activated tab in
	 * the tabbed pane, and adds those entries to the desktop.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addDesktop() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_TITLES:
			addToDesktop(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableTitles, 0));
			break;
		case TAB_LINKS:
			addToDesktop(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableLinks, 0));
			addToDesktop(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableManLinks, 0));
			break;
		case TAB_LUHMANN:
			addToDesktop(new int[] { selectedEntryInJTreeLuhmann() });
			break;
		case TAB_BOOKMARKS:
			addToDesktop(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableBookmarks, 0));
			break;
		}
	}

	/**
	 * This method searches for entries that contain the selected keywords from the
	 * jTableKeywords, and adds those entries to the desktop. <br>
	 * <br>
	 * See {@link #addDesktopLogOr() addDesktopLogOr()} and
	 * {@link #addDesktopLogAnd() addDesktopLogAnd()} for more details.
	 *
	 * @param log the logical combination of the search, whether at least one
	 *            keywords should exist (log-or) or if only entries are added that
	 *            contain all keywords (log-and)
	 */
	private void addDesktopFromKeywords(int log) {
		// search for all entries that contain the selected keywords and add them to the
		// desktop
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0), // string-array with search
																								// terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_DESKTOP, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	/**
	 * This method searches for entries that contain at least on of the selected
	 * entries (log-or) from the jTableAuthors or jTableKeywords, and adds those
	 * entries to the desktop.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addDesktopLogOr() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addDesktopFromAuthors(Constants.LOG_OR);
			break;
		case TAB_KEYWORDS:
			addDesktopFromKeywords(Constants.LOG_OR);
			break;
		}
	}

	/**
	 * This method searches for entries that contain <i>all</i> selected entries
	 * (log-and) from the jTableAuthors or jTableKeywords, and adds those entries to
	 * the desktop.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addDesktopLogAnd() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addDesktopFromAuthors(Constants.LOG_AND);
			break;
		case TAB_KEYWORDS:
			addDesktopFromKeywords(Constants.LOG_AND);
			break;
		}
	}

	/**
	 * This method searches for entries that contain the selected authors from the
	 * jTableAuthors, and adds those entries to the desktop. <br>
	 * <br>
	 * See {@link #addDesktopLogOr() addDesktopLogOr()} and
	 * {@link #addDesktopLogAnd() addDesktopLogAnd()} for more details.
	 *
	 * @param log the logical combination of the search, whether at least one author
	 *            should exist (log-or) or if only entries are added that contain
	 *            all authors (log-and)
	 */
	private void addDesktopFromAuthors(int log) {
		// search for all entries that contain the selected authors and add them to the
		// destzop
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with search
																							// terms
				Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_DESKTOP, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	/**
	 * This method rerieves the selected entries from the current activated tab in
	 * the tabbedpane as manual links to the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addManLinks() {
		// retrieve activated entry. In case we have selected an entry from any table in
		// the tabbed pane,
		// this entry is automatically displayed. Thus, this entry cannot be added as
		// manual link to the
		// displayed entry (=itself), but instead it should be added to the activated
		// entry.
		int activatedEntry = data.getActivatedEntryNumber();
		// retrieve selected tab
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_TITLES:
			addToManLinks(activatedEntry, ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableTitles, 0));
			break;
		// in this single case, we have to update the tab with the links-table...
		case TAB_LINKS:
			needsLinkUpdate = true;
			addToManLinks(activatedEntry, ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableLinks, 0));
			break;
		case TAB_BOOKMARKS:
			addToManLinks(activatedEntry,
					ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableBookmarks, 0));
			break;
		case TAB_LUHMANN:
			addToManLinks(activatedEntry, new int[] { selectedEntryInJTreeLuhmann() });
			break;
		}
	}

	/**
	 * This method adds one or more entries as follower-numbers to the entry that is
	 * selected in the jTreeLuhmann. <br>
	 * This method needs to be public, since we want to access it from other frames,
	 * like for instance {@link CSearchResults}.
	 *
	 * @param entries an int-array containing the entry-numbers of those entries
	 *                that should be added as follower-entries
	 * @return {@code true} if everything went OK, false if an error occurred
	 */
	public boolean addToLuhmann(int[] entries) {
		if ((entries == null) || (entries.length == 0) || (entries[0] == -1)) {
			return false;
		}

		int targetEntry = selectedEntryInJTreeLuhmann();
		if (targetEntry == -1) {
			// If no valid selection made, get current entry number
			targetEntry = data.getActivatedEntryNumber();
		}

		boolean error = false;
		for (int newSubEntry : entries) {
			if (!data.appendSubEntryToEntry(new EntryID(targetEntry), new EntryID(newSubEntry))) {
				error = true;
			}
		}
		if (error) {
			// If fails, display error message box.
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errLuhmannExistsMsg"),
					getResourceMap().getString("errLuhmannExistsTitle"), JOptionPane.PLAIN_MESSAGE);
		}

		updateDisplay();
		return true;
	}

	/**
	 * This method rerieves the selected entries from the current activated tab in
	 * the tabbedpane and adds them to the bookmarks of the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addBookmarks() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_TITLES:
			addToBookmarks(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableTitles, 0), false);
			break;
		case TAB_LUHMANN:
			addToBookmarks(new int[] { selectedEntryInJTreeLuhmann() }, false);
			break;
		}
	}

	/**
	 * This method retrieves the selected entries from the current activated tab in
	 * the tabbedpane and adds them as luhmann-numbers (follower) of the current
	 * entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addLuhmann() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_BOOKMARKS:
			addToLuhmann(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableBookmarks, 0));
			break;
		case TAB_LINKS:
			addToLuhmann(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableLinks, 0));
			addToLuhmann(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableManLinks, 0));
			break;
		case TAB_TITLES:
			addToLuhmann(ZettelkastenViewUtil.retrieveSelectedEntriesFromTable(data, jTableTitles, 0));
			break;
		}
	}

	/**
	 * This method searches for entries that contain at least on of the selected
	 * entries (log-or) from the jTableAuthors or jTableKeywords, and adds those
	 * entries as luhmann-numbers (follower) to the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addLuhmannLogOr() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addLuhmannFromAuthors(Constants.LOG_OR);
			break;
		case TAB_KEYWORDS:
			addLuhmannFromKeywords(Constants.LOG_OR);
			break;
		}

	}

	/**
	 * This method searches for entries that contain <i>all</i>of the selected
	 * entries (log-and) from the jTableAuthors or jTableKeywords, and adds those
	 * entries as luhmann-numbers (follower) to the current entry.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void addLuhmannLogAnd() {
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			addLuhmannFromAuthors(Constants.LOG_AND);
			break;
		case TAB_KEYWORDS:
			addLuhmannFromKeywords(Constants.LOG_AND);
			break;
		}

	}

	/**
	 * This method searches for entries that contain the selected authors from the
	 * jTableAuthors, and adds those entries as luhmann-numbers (follower) to the
	 * current entry. <br>
	 * <br>
	 * See {@link #addLuhmannLogOr() addLuhmannLogOr()} and
	 * {@link #addLuhmannLogAnd() addLuhmannLogAnd()} for more details.
	 *
	 * @param log the logical combination of the search, whether at least one author
	 *            should exist (log-or) or if only entries are added that contain
	 *            all authors (log-and)
	 */
	private void addLuhmannFromAuthors(int log) {
		// search for all entries that contain at least on of the selected keywords
		// and add them as luhmann-numbers
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with search
																							// terms
				Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_LUHMANN, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
		// update display
		updateDisplay();
	}

	/**
	 * This method searches for entries that contain the selected authors from the
	 * jTableAuthors, and adds those entries as manual links to the current entry.
	 * <br>
	 * <br>
	 * See {@link #addManLinksFromAuthors() addManLinksFromAuthors()} and
	 * {@link #addManLinksFromAuthorsLogAnd() addManLinksFromAuthorsLogAnd()} for
	 * more details.
	 *
	 * @param log the logical combination of the search, whether at least one author
	 *            should exist (log-or) or if only entries are added that contain
	 *            all authors (log-and)
	 */
	private void addManLinksFromAuthors(int log) {
		// search for all entries that contain at least on of the selected authors
		// and add them as manual links
		startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with search
																							// terms
				Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
				log, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				true, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_MANLINK, // whether we have a usual search, or a search for entries without
												// remarks or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
		// update display
		updateDisplay();
	}

	// TODO wenn import abbricht, werden nicht alle listen resettet, bspw. table
	// enthalten noch alte daten
	/**
	 * This method opens two dialogs: 1) the import dialog where the user can choose
	 * which type of data to import and where the file is locates. and 2) the status
	 * message window which does the import action in a background task
	 */
	@Action
	public void importWindow() throws IOException {
		// opens the Import Dialog. This Class is responsible
		// for getting the relevant import data. the import task
		// itself (background task) will be started as another dialog,
		// when this one is closed
		// If dialog window isn't already created, do this now.
		if (null == importWindow) {
			// when we have no data, there is no need for appending the data
			boolean isAppendPossible = data.getCount(Daten.ZKNCOUNT) > 0;
			// get parent und init window
			importWindow = new CImport(getFrame(), settings, bibtex, isAppendPossible);
			// Center new dialog window.
			importWindow.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(importWindow);
		// Here the data (filepath, filetype) from the import window
		// will be passed to another dialog window, which starts the
		// background task. these dialogs are separated because it looks
		// better to have a background task with progress bar in an own,
		// smaller dialog
		//
		// first check for valid return value. import is only started,
		// when the previous dialog wasn't cancelled or simply closed
		if (Constants.RETURN_VALUE_CONFIRM == importWindow.getReturnValue()) {
			// check whether we want to import bibtex data
			if (Constants.TYPE_BIB == importWindow.getImportType()) {
				importAuthors();
			} else {

				// TODO wieder entfernen, wenn CSV implementiert
				if (Constants.TYPE_CSV == importWindow.getImportType()) {
					JOptionPane.showMessageDialog(getFrame(), "CSV-import not implemented yet!", "Import",
							JOptionPane.PLAIN_MESSAGE);
					return;
				}

				// first check whether we have unsaved changes, when the user wants
				// to create a new data-file - but only the import-type is a data-file!
				if (((Constants.TYPE_ZKN3 == importWindow.getImportType())
						|| (Constants.TYPE_ZKN == importWindow.getImportType())
						|| (Constants.TYPE_CSV == importWindow.getImportType()))
						&& (!importWindow.getAppend()
								&& !askUserAndMaybeSaveChanges(getResourceMap().getString("msgSaveChangesTitle")))) {
					return;
				}
				// create default timestamp. this is only relevant for importing old data-files
				// (.zkn), because
				// entries of old data-files may not always have timestamps. to ensure each
				// entry has a time stamp
				// we offer the user to input a default date that is set for all entries that do
				// not have any timestamp
				String defaulttimestamp = null;
				// if old data should be imported, ask for default timestamp
				if (Constants.TYPE_ZKN == importWindow.getImportType()) {
					// get current date as default or initial value
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
					// get mainfile that should be importet
					File f = importWindow.getFilePath();
					// get last modification date from file
					long l = f.lastModified();
					// wait for valid input
					while (null == defaulttimestamp) {
						// show input-dialog
						defaulttimestamp = (String) JOptionPane.showInputDialog(getFrame(), // parent window
								getResourceMap().getString("defaultTimeStampMsg"), // message text
								getResourceMap().getString("defaultTimeStampTitle"), // messagebox title
								JOptionPane.PLAIN_MESSAGE, // type of dialog
								null, // icon
								null, // array of selection values.
								// must be null to get an input-field.
								// providing an array here would create a dropdown-combobox
								sdf.format(l)); // initial value, date of importfile
						// now convert the timestamp into something
						// therefore, check whether we have any valid input at all, if we have the
						// correct length (8 chars)
						// and if we have to "." at the right position. A valid input would be e.g.
						// "31.12.08" (dd.mm.yy)
						if (defaulttimestamp != null && 8 == defaulttimestamp.length()
								&& defaulttimestamp.charAt(2) == '.' && defaulttimestamp.charAt(5) == '.') {
							defaulttimestamp = defaulttimestamp.substring(6) + defaulttimestamp.substring(3, 5)
									+ defaulttimestamp.substring(0, 2) + "0001";
						} else {
							defaulttimestamp = null;
						}
					}
				}
				// If dialog window isn't already created, do this now.
				if (taskDlg == null) {
					// get parent und init window
					taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_IMPORTDATA, taskinfo, data,
							bookmarks, desktop, searchRequests, settings, importWindow.getImportType(),
							importWindow.getFilePath(), importWindow.getSeparatorChar(),
							importWindow.getAsciiToUnicode(), importWindow.getAppend(), defaulttimestamp, null);
					// Center new dialog window.
					taskDlg.setLocationRelativeTo(getFrame());
				}
				waitForTaskDialog();

				// when an error occured, show errorlog
				if (!taskinfo.isImportOk()) {
					showErrorLog();
				}
				// this is the typical stuff we need to do when a file is opened
				// or imported, but only when we imported "real" data
				switch (importWindow.getImportType()) {
				case Constants.TYPE_ZKN3:
				case Constants.TYPE_ZKN:
				case Constants.TYPE_CSV:
				case Constants.TYPE_XML:
					updateDisplayAfterOpen();
					break;
				}
			}
		}
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		importWindow.dispose();
		importWindow = null;
	}

	/**
	 * This method displays the log-file in a new window.
	 */
	@Action
	public void showErrorLog() {
		// terminate timer
		if (flashErrorIconTimer != null) {
			// if timer was running, cancel it
			flashErrorIconTimer.cancel();
			// purge it from the task-list
			flashErrorIconTimer.purge();
			// free timer-object
			flashErrorIconTimer = null;
		}
		// hide button
		statusErrorButton.setVisible(false);
		if (null == errorDlg) {
			errorDlg = new CErrorLog(getFrame(), this, settings);
			errorDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(errorDlg);
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		errorDlg.dispose();
		errorDlg = null;
	}

	public void showErrorIcon() {
		statusErrorButton.setVisible(true);
		if (flashErrorIconTimer != null) {
			// Error icon already exists.
			return;
		}
		flashErrorIconTimer = new Timer();
		// Timer starts immediately and update each second.
		flashErrorIconTimer.schedule(new ErrorIconTimer(), 0, 1000);
	}

	/**
	 * This method opens three dialogs: <br>
	 * <br>
	 * 1) a dialog where the user can choose, which entries he wants to export. <br>
	 * <br>
	 * 2) the export dialog where the user can choose which type of format the data
	 * to be exported and where the file should be saved. <br>
	 * <br>
	 * and 3) the status message window which does the export action in a background
	 * task
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void exportWindow() {
		// first, let the user choose, which entries to export - whether all entries
		// or just a selection...
		if (null == exportEntriesDlg) {
			// get parent und init window
			exportEntriesDlg = new CExportEntries(getFrame(), data.getCount(Daten.ZKNCOUNT), settings);
			// Center new dialog window.
			exportEntriesDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(exportEntriesDlg);
		// check for valid return-value
		if (!exportEntriesDlg.isCancelled()) {
			// now call the export-dialog with more options, so the chosen entries
			// will be exported
			exportEntries(exportEntriesDlg.getEntries());
			// we have to manually dispose the window and release the memory
			// because next time this method is called, the showKwlDlg is still not null,
			// i.e. the constructor is not called (because the if-statement above is not
			// true)
		}
		exportEntriesDlg.dispose();
		exportEntriesDlg = null;
	}

	/**
	 * This method opens the export-dialog where the user can choose which format to
	 * use when exporting entries. This method is public, because it is also called
	 * from the CSearchResults-window to export search results and from the
	 * Desktop/Outliner to export data.
	 *
	 * @param entries an array of entry-numbers that should be exported. use
	 *                {@code null} to export all entries.
	 */
	public void exportEntries(int[] entries) {
		// here we copy the integer-array to an object-array-list,
		// since our export-entries need to be in array-object-format
		ArrayList<Object> liste = new ArrayList<>();
		if (null == entries || entries.length < 1) {
			liste = null;
		} else {
			for (int cnt = 0; cnt < entries.length; cnt++) {
				liste.add(entries[cnt]);
			}
		}
		exportEntries(liste);
	}

	/**
	 * This method opens the export-dialog where the user can choose which format to
	 * use when exporting entries. This method is public, because it is also called
	 * from the CSearchResults-window to export search results and from the
	 * Desktop/Outliner to export data.
	 *
	 * @param entries an array of entry-numbers that should be exported. use
	 *                {@code null} to export all entries.
	 */
	public void exportEntries(ArrayList<Object> entries) {
		// opens the Export Dialog. This Class is responsible
		// for getting the relevant export data. the export task
		// itself (background task) will be started as another dialog,
		// when this one is closed
		// now open the export-dialog
		if (null == exportWindow) {
			// get parent und init window
			exportWindow = new CExport(getFrame(), settings, bibtex);
			// Center new dialog window.
			exportWindow.setLocationRelativeTo(getFrame());
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
			// when the user wants to export into PDF or LaTeX, open a new dialog where the
			// user can make some extra settings like page settings and font-sizes.
			if (ExportTools.isExportSettingsOk(getFrame(), settings, exportWindow.getExportType())) {
				// If dialog window isn't already created, do this now.
				if (taskDlg == null) {
					// open export dialog
					// get parent und init window
					taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_EXPORTDATA, taskinfo, data,
							bookmarks, desktop, settings, bibtex, settings.getSynonyms(), exportWindow.getFilePath(),
							entries, exportWindow.getExportType(), exportWindow.getExportParts(),
							exportWindow.getCSVSeparator(), null, exportWindow.isSeparateFileForNotes(),
							exportWindow.getFormatTagsRemoved(), exportWindow.getExportBibTex(),
							exportWindow.getKeywordsHighlighted(), false, false, exportWindow.hasTitlePrefix());
					// Center new dialog window.
					taskDlg.setLocationRelativeTo(getFrame());
				}
				waitForTaskDialog();

				// if an error occured, show error-log
				if (!taskinfo.isExportOk()) {
					showErrorIcon();
				} // else tell user that everything went fine
				else if (taskinfo.showExportOkMessage()) {
					JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("exportOkMsg"),
							getResourceMap().getString("exportOkTitle"), JOptionPane.PLAIN_MESSAGE);
				}
			}
		}
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		exportWindow.dispose();
		exportWindow = null;
	}

	/**
	 * Shows the about box and gives information about the programm and version
	 */
	@Action
	public void showAboutBox() {
		if (null == zknAboutBox) {
			zknAboutBox = new AboutBox(getFrame(), settings.isMacStyle() | settings.isMacStyle());
			zknAboutBox.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(zknAboutBox);
		// clear memory allocation
		zknAboutBox.dispose();
		zknAboutBox = null;
	}

	/**
	 * This method opens the view for editing new entries. 
	 * "Neuer Zettel" (German for new note)
	 */
	@Action
	public void newEntry() {
		openEditWindow(false, -1, false, false, -1);
	}
        
	@Action
    public void showNewEntryWindow() {
        if (editEntryDlg != null) {
        	 // Debugging: Log the current value of editEntryDlg
        	System.out.println("editEntryDlg value: " + editEntryDlg);
            editEntryDlg.setAlwaysOnTop(true);
            editEntryDlg.toFront();
            editEntryDlg.requestFocus();
            editEntryDlg.setAlwaysOnTop(false);
        } else {
            newEntry(); // Create a new input window
        }
    }

	/**
	 * This method opens the window for editing existing entries. All the stuff like
	 * saving the data to the main-data-object is done within the class
	 * de.danielluedecke.zettelkasten.EditorFrame
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void editEntry() {
		if (data.isDeleted(displayedZettel)) {
			openEditWindow(false, displayedZettel, false, true, -1);
		} else {
			openEditWindow(/* isEditing */true, displayedZettel, /* isLuhmann */false, /* isDeleted */false,
					/* insertAfterEntry */-1);
		}
	}

	/**
	 * This method opens the new-entry-window for editing new or existing entries.
	 * if an entry is currently being edited, the {@code isEditModeActive} flag is
	 * set. In this case, the edit-window is only brought to the front. Else, a new
	 * window is created.
	 *
	 * @param isEditing        true if we want to edit an existing entry, false if a
	 *                         new entry is to be created
	 * @param entrynumber      the entrynumber. relevant for editing existing
	 *                         entries.
	 * @param isLuhmann        true if the new entry should be inserted as follower
	 *                         of the current entry.
	 * @param isDeleted        true if the user wants to edit a deleted entry, thus
	 *                         inserting a new entry at an deleted entry's position
	 * @param insertAfterEntry This variable stores the number of that entry after
	 *                         which the new entry should be inserted. does only
	 *                         affect the prev/next attributes of an entry. Use
	 *                         {@code -1} to add entry to the end of entry order.
	 */
	public void openEditWindow(boolean isEditing, int entrynumber, boolean isLuhmann, boolean isDeleted,
			int insertAfterEntry) {
		openEditWindow(isEditing, entrynumber, isLuhmann, isDeleted, insertAfterEntry,
				jEditorPaneEntry.getSelectedText());
	}

	/**
	 * This method opens an edit window (EditorFrame) for editing new or existing
	 * entries. If an entry is currently being edited, the existing window is
	 * brought to the front. Else, a new window is created.
	 *
	 * @param isEditing        true if we want to edit an existing entry, false if a
	 *                         new entry is to be created
	 * @param entrynumber      the entrynumber. relevant for editing existing
	 *                         entries.
	 * @param isLuhmann        true if the new entry should be inserted as follower
	 *                         of the current entry.
	 * @param isDeleted        true if the user wants to edit a deleted entry, thus
	 *                         inserting a new entry at an deleted entry's position
	 * @param insertAfterEntry This variable stores the number of that entry after
	 *                         which the new entry should be inserted. does only
	 *                         affect the prev/next attributes of an entry. Use
	 *                         {@code -1} to add entry to the end of entry order.
	 * @param String           content
	 */
	private void openEditWindow(boolean isEditing, int entrynumber, boolean isLuhmann, boolean isDeleted,
			int insertAfterEntry, String content) {
		if (isEditModeActive) {
			// If an entry is already being edited, bring the existing EditorFrame window to
			// the front.
			editEntryDlg.toFront();
			return;
		}

		// Start edit mode.
		isEditModeActive = true;

		// Create new EditorFrame window and show it.
		editEntryDlg = new EditorFrame(this, data, taskinfo, settings.getAcceleratorKeys(), settings,
				settings.getAutoKorrektur(), settings.getSynonyms(), settings.getStenoData(), content, isEditing,
				entrynumber, isLuhmann, isDeleted);
		editEntryDlg.setLocationRelativeTo(getFrame());
		ZettelkastenApp.getApplication().show(editEntryDlg);
		editEntryDlg.toFront();
	}

	/**
	 * This method is called from the EditorFrame to indicate when an edit has been
	 * finished and the window has been closed.
	 * 
	 * @param changed whether any change happened.
	 */
	public void editFinishedEvent(boolean changed, boolean editMode, int entryNumber) {
		if (changed) {
			// Maybe update desktop window.
			if (editMode && desktopDlg != null
					&& desktop.desktopContainsEntry(desktop.getCurrentDesktopNr(), entryNumber)) {
				desktopDlg.updateEntriesAfterEditing();
			}

			// Maybe update search results window.
			if (editMode && searchResultsDlg != null) {
				searchResultsDlg.updateDisplayAfterEditing();
			}

			// Reset displayedZettel and updateDisplay.
			displayedZettel = -1;
			updateDisplay();

			// Create a backup.
			makeAutoBackup();

			// If entry was edited from search results window, bring window to front.
			if (editEntryFromSearchWindow && searchResultsDlg != null) {
				searchResultsDlg.toFront();
			}

			// If entry was edited from desktop window, bring window to front.
			if (editEntryFromDesktop && desktopDlg != null) {
				// Add to desktop if valid ZettelID.
				if (entryNumber != -1) {
					String id = data.getZettelID(entryNumber);
					if (id != null) {
						desktopDlg.addEntries(new int[] { entryNumber });
					}
				}
				desktopDlg.toFront();
			}
		}

		// Reset edit entry variables.
		editEntryFromDesktop = false;
		editEntryFromSearchWindow = false;
		isEditModeActive = false;
	}

	/**
	 * This method starts a background thread that creates an automatic backup of
	 * the current main data file. the file is saved to the same directory as the
	 * main data file, just changing the extenstion to ".zkb3". <br>
	 * <br>
	 * This method is called when we have changes that are not save, e.g. after the
	 * methods {@link #newEntry() newEntry()} or {@link #editEntry() editEntry()}.
	 */
	private void makeAutoBackup() {
		// if
		// - task is already running, or
		// - no backup necessary
		// - or an save-operation is in progress...
		// ...then do nothing.
		if (isAutoBackupRunning() || !isBackupNecessary() || isSaving) {
			return;
		}
		// check for autobackup
		if (settings.getAutoBackup() && (settings.getMainDataFile() != null)) {
			prepareAndStartTask(autoBackupTask());

			Constants.zknlogger.log(Level.INFO, "Autobackup finished (if necessary).");
		} else {
			setAutoBackupRunning(false);
		}
	}

	/**
	 * This mehtod creates an additional backup of<br>
	 * - the data-file - the meta-data ({@code zettelkasten-data.zkd3}) when the
	 * user quits the application. These files are saved to a certain directory that
	 * is specified by the user.
	 */
	private void makeExtraBackup() {
		// when no extrabackup is requested, do nothing.
		if (!settings.getExtraBackup()) {
			return;
		}
		// retrieve backup-directory
		File backuppath = settings.getExtraBackupDir();
		// when the path does not exist, leave...
		if (null == backuppath) {
			// log error
			Constants.zknlogger.log(Level.WARNING,
					"The file path to the extra backup (which is created when closing the application) is null! Extra backup could not be created!");
			return;
		}
		if (!backuppath.exists()) {
			// log error
			Constants.zknlogger.log(Level.WARNING,
					"Could not save extra backup to {0}. The file path to the extra backup (which is created when closing the application) does not exist! Extra backup could not be created!",
					backuppath.toString());
			return;
		}
		// get filename and find out where extension begins, so we can retrieve the
		// filename
		File datafile = settings.getMainDataFile();
		// if we have a valid file-path, go on...
		if (datafile != null) {
			// create a backup-filename, that consists of the data-file's filename
			String backupfilename = datafile.getName();
			// retrieve os-separator-char
			String sepchar = String.valueOf(File.separatorChar);
			// add additional separator-char if the file-path does not contain a trailing
			// separator char
			if (!backuppath.toString().endsWith(sepchar)) {
				backupfilename = sepchar + backupfilename;
			}
			// create final backup-file-path
			File backupfile = new File(backuppath.toString() + backupfilename);
			// if backup-filepath is identical with the data-filepath, we don't create an
			// extra backup
			// to prevent overwriting the file...
			if (backupfile.toString().equalsIgnoreCase(datafile.toString())) {
				// log error
				Constants.zknlogger.log(Level.WARNING,
						"The file path of the extra backup (which is created when closing the application) equals the main data file name! To prevent overwriting the data file, the extra backup was not created!");
				return;
			}
			try {
				// copy data file
				FileOperationsUtil.copyFile(datafile, backupfile, 4096);
				// log file path and success
				Constants.zknlogger.log(Level.INFO, "Extra-backup was copied to {0}", backupfile.toString());
				// retrieve filepath of meta-file, i.e. the file that stores spellchecking,
				// synonyms etc.
				File metafile = settings.getMetadataFile();
				// check whether file exists
				if (metafile != null && metafile.exists()) {
					// get filename
					String metafilename = "zettelkasten-data.zkd3";
					if (!backuppath.toString().endsWith(sepchar)) {
						metafilename = sepchar + metafilename;
					}
					// create backupfilepath
					File backupmetafile = new File(backuppath.toString() + metafilename);
					// copy meta-file
					FileOperationsUtil.copyFile(metafile, backupmetafile, 4096);
					// log file path and success
					Constants.zknlogger.log(Level.INFO, "Extra-backup meta-data was copied to {0}",
							backupmetafile.toString());
				} else {
					// log error
					Constants.zknlogger.log(Level.WARNING,
							"Extra-backup meta-data does not exists and thus could not be created!");
				}

			} catch (IOException ex) {
				Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
			}
		}
	}

	/**
	 * This method deletes the currently displayed note. <br>
	 * The entry is not being deleted completely. To keep the ordering and
	 * index-numbers of existing entries, a deleted entry will just be cleared (all
	 * content set to empty string values), and if a deleted entry is displayed,
	 * just a hint "deleted" is displayed in the main editor pane.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void deleteCurrentEntry() {
		if (deleteEntries(new int[] { displayedZettel })) {
			data.setTitlelistUpToDate(false);
		}
	}

	/**
	 * This method adds the currently displayed entry to the desktop-window.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void addToDesktop() {
		addToDesktop(new int[] { displayedZettel });
	}

	/**
	 * This method adds one or more entries to the desktop-window. The entry-numbers
	 * of the to be added entries have to be passed as integer-array.<br>
	 * <br>
	 * This method needs to be public, since we want to access it from other frames,
	 * like for instance {@link CSearchResults}.
	 *
	 * @param entries an int-array conatining the entry-numbers of those entries
	 *                that should be added to the desktop.
	 */
	public void addToDesktop(int[] entries) {
		// check for valid values
		if ((null == entries) || (entries.length < 1) || (-1 == entries[0])) {
			return;
		}
		// If dialog window isn't already created, do this now.
		if (null == desktopDlg) {
			desktopDlg = new DesktopFrame(this, taskinfo, data, bookmarks, desktop, settings,
					settings.getAcceleratorKeys(), bibtex, settings.getAutoKorrektur(), settings.getStenoData());
		}
		// show desktop
		ZettelkastenApp.getApplication().show(desktopDlg);
		// add entyry to desktop
		desktopDlg.addEntries(entries);
		// enable window-menu-item, if we have loaded desktop data
		setDesktopAvailable(desktop.getCount() > 0);
	}

	@Action(enabledProperty = "entriesAvailable")
	public void newDesktop() {
		// If dialog window isn't already created, do this now.
		if (null == desktopDlg) {
			desktopDlg = new DesktopFrame(this, taskinfo, data, bookmarks, desktop, settings,
					settings.getAcceleratorKeys(), bibtex, settings.getAutoKorrektur(), settings.getStenoData());
		}
		// show desktop
		ZettelkastenApp.getApplication().show(desktopDlg);
		// let user create new desktop
		desktopDlg.newDesktop();
		// enable window-menu-item, if we have loaded desktop data
		setDesktopAvailable(desktop.getCount() > 0);
	}

	/**
	 * This method opens the window for inserting new entries as "followers" or
	 * "sub-entries". These entries are inserted at the end of the data set, but are
	 * indicated as "followers" (i.e.: sub-entries) of the current visible entry.
	 * All the stuff like saving the data to the main-data-object is done within the
	 * class "CNewEntry.java". We than additionally set the "luhmann"-tag here (see
	 * CDaten.java for more detaiks), which is used in the "showLuhmann" method
	 * here.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void insertEntry() {
		createEntryAndAddAsSubEntry();
	}

	void createEntryAndAddAsSubEntry() {
		openEditWindow(/* isEditing */false, displayedZettel, /* isLuhmann */true, /* isDeleted */false,
				displayedZettel);
	}

	/**
	 * This method opens an window asking for the number of the entries to be
	 * inserted as sub-entries (Luhmann). Entries may be separated with commas, or
	 * also contain a "from-to" range. example: "4,6,11-15,19"
	 * 
	 * The received entries are inserted as sub-entries of the selected entry of the
	 * jTreeLuhmann (if any) or of the current visible entry.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void manualInsertEntry() {
		// Open an input-dialog.
		String newLuhmann = (String) JOptionPane.showInputDialog(getFrame(),
				getResourceMap().getString("newLuhmannMsg"), getResourceMap().getString("newLuhmannTitle"),
				JOptionPane.PLAIN_MESSAGE);
		if ((newLuhmann == null) || (newLuhmann.length() == 0)) {
			// Do nothing if input dialog is empty.
			return;
		}
		// Convert the string-input into an int-array of entry numbers.
		int[] selectedEntryNumbers = Tools.retrieveEntryNumbersFromInput(newLuhmann, data.getCount(Daten.ZKNCOUNT));
		if (selectedEntryNumbers == null) {
			// Do nothing if input was not parsed correctly. retrieveEntryNumbersFromInput
			// might shows errors to the user.
		}
		// Add selectedValues as follower-entries to the appropriate entry.
		addToLuhmann(selectedEntryNumbers);
	}

	/**
	 * This method opens the window for inserting new entries as manual links.
	 * Entries may be separated with commas, or also contain a "from-to" option.
	 * example: "4,6,11-15,19"
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void manualInsertLinks() {
		// open an input-dialog
		String manLinks = (String) JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("newManLinksMsg"),
				getResourceMap().getString("newManLinksTitle"), JOptionPane.PLAIN_MESSAGE);
		// if we have a valid return-value...
		if ((manLinks != null) && (manLinks.length() > 0)) {
			// convert the string-input into an int-array
			int[] selectedValues = Tools.retrieveEntryNumbersFromInput(manLinks, data.getCount(Daten.ZKNCOUNT));
			// and add them as follower-entries
			if (selectedValues != null) {
				addToManLinks(selectedValues);
			}
		}
	}

	public void editManualLinks() {
		// open an input-dialog
		String manLinks = (String) JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("newManLinksMsg"),
				data.getManualLinksAsSingleString(displayedZettel));
		// if we have a valid return-value...
		if (manLinks != null) {
			if (!manLinks.isEmpty()) {
				// convert the string-input into an int-array
				int[] selectedValues = Tools.retrieveEntryNumbersFromInput(manLinks, data.getCount(Daten.ZKNCOUNT));
				// and add them as crossreference-entries
				if (selectedValues != null) {
					addToManLinks(selectedValues);
				}
			} // if we have a valid empty return-value...
			else {
				// delete manual links
				data.setManualLinks(displayedZettel, "");
			}
			updateDisplay();
		}
	}

	/**
	 * This method adds one or more entries as manual links to the current entry.
	 * The entry-numbers of the to be added entries have to be passed as
	 * integer-array.<br>
	 * <br>
	 * This method needs to be public, since we want to access it from other frames,
	 * like for instance {@link CSearchResults}.
	 *
	 * @param entries an int-array conatining the entry-numbers of those entries
	 *                that should be added as manual links
	 * @return {@code true} if entries have been successfully added, false if an
	 *         error occured
	 */
	public boolean addToManLinks(int[] entries) {
		return addToManLinks(displayedZettel, entries);
	}

	/**
	 * This method adds one or more entries as manual links to the current entry.
	 * The entry-numbers of the to be added entries have to be passed as
	 * integer-array.<br>
	 * <br>
	 * We have this method with additional parameter (in addition to
	 * {@link #addToManLinks(int[]) addToManLinks(int[])}, in case we want to add
	 * manual links from the jTableLinks to the current entry. Usually, manual links
	 * are added to the displayed entry. Since the displayed entry is the entry that
	 * is selected in the jTableLinks, we would add the selected entry as manual
	 * link to the selected entry - which doesn't work. So, whenever an entry in the
	 * jTableLinks is selected, it will be added as manual link to the
	 * <b>activated</b> entry.<br>
	 * <br>
	 * In all other cases, manual links are added to the displayed entry, thus
	 * calling the {@link #addToManLinks(int[]) addToManLinks(int[])} method.
	 *
	 * @param activatedEntry the entry-number where the manual links should be added
	 *                       to...
	 * @param entries        an int-array conatining the entry-numbers of those
	 *                       entries that should be added as manual links
	 * @return {@code true} if entries have been successfully added, false if an
	 *         error occured
	 */
	private boolean addToManLinks(int activatedEntry, int[] entries) {
		if ((null == entries) || (entries.length < 1) || (-1 == entries[0])) {
			return false;
		}
		// init message-box indicator
		boolean error = false;
		// iterate array
		// and add it to the current entry
		for (int nr : entries) {
			if (!data.addManualLink(activatedEntry, nr)) {
				error = true;
			}
		}
		// display error message box, when any problems occured
		if (error) {
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errManLinksExistsMsg"),
					getResourceMap().getString("errManLinksExistsTitle"), JOptionPane.PLAIN_MESSAGE);
		}
		// update the display
		updateDisplay();
		// everything went ok
		return true;
	}

	/**
	 * This method creates a new, empty zettelkasten. If there are unsaved changes,
	 * the user can save them before. else, the currently opened datafile is
	 * immediately closed
	 */
	@Action
	public void newZettelkasten() {
		// first check whether we have unsaved changes, when the user wants
		// to open a new data-file
		if (askUserAndMaybeSaveChanges(getResourceMap().getString("msgSaveChangesTitle"))) {
			// reset the data-file
			Constants.zknlogger.log(Level.INFO, "Setting data file to null for a new Zettelkasten.");
			settings.setMainDataFile(null);
			data.reset();
			desktop.clear();
			bookmarks.clear();
			searchRequests.clear();
			bibtex.clearEntries();
			// set modified state to false
			data.setModified(false);
			searchRequests.setModified(false);
			desktop.setModified(false);
			bookmarks.setModified(false);
			bibtex.setModified(false);
			// init some variables
			initVariables();

			updateDisplay();
		}
	}

	/**
	 * Displays the first entry in the zettelkasten.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void showFirstEntry() {
		data.firstEntry();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * Displays the last entry in the zettelkasten.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void showLastEntry() {
		data.lastEntry();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * Displays the next entry in the zettelkasten.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void showNextEntry() {
		data.nextEntry();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * Sets the input focus to the textfield where the user can input an
	 * entry-number, so the requested entry is displayed.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void gotoEntry() {
		jTextFieldEntryNumber.requestFocusInWindow();
	}

	/**
	 * Make entryNumber the new activated entry and update display. Do nothing if
	 * entryNumber is invalid.
	 *
	 * @param entryNumber (the entry number to display)
	 */
	public void setNewActivatedEntryAndUpdateDisplay(int entryNumber) {
		setNewActivatedEntryAndUpdateDisplay(entryNumber, UpdateDisplayOptions.defaultOptions());
	}

	/**
	 * Make entryNumber the new activated entry and update display. Do nothing if
	 * entryNumber is invalid.
	 *
	 * @param entryNumber (the entry number to display)
	 * @param options     (update display options)
	 */
	public void setNewActivatedEntryAndUpdateDisplay(int entryNumber, UpdateDisplayOptions options) {
		if (data.activateEntry(entryNumber)) {
			// Reset displayedZettel.
			displayedZettel = -1;
			updateDisplay(options);
		} else {
			// This should never happen.
			Constants.zknlogger.log(Level.WARNING,
					"setNewActivatedEntryAndUpdateDisplay was called with invalid entry number: {0}", entryNumber);
		}
	}

	/**
	 * This method displays a random entry, where deleted entries will not be shown.
	 */
	@Action
	public void showRandomEntry() {
		// check for available entries
		if (!data.hasEntriesExcludingDeleted()) {
			return;
		}
		// init variable
		int randomnumber = -1;
		// create a random number and check, whether the entry with the created random
		// number
		// is deleted or not
		while (-1 == randomnumber || data.isDeleted(randomnumber)) {
			// create new random number until we have found a valid, non-deleted entry
			randomnumber = (int) (Math.random() * data.getCount(Daten.ZKNCOUNT)) + 1;
		}
		// show that entry
		setNewActivatedEntryAndUpdateDisplay(randomnumber);
	}

	/**
	 * displays the first entry in the zettelkasten
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void showPrevEntry() {
		data.prevEntry();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * Searches the database for multiple entries and displays them in a table.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findDoubleEntries() {
		// check whether dialog is already visible or was created
		if (null == doubleEntriesDlg || !doubleEntriesDlg.isVisible()) {
			// if not, create new dialog
			doubleEntriesDlg = new FindDoubleEntriesTask(getFrame(), this, data, settings);
			// Center new dialog window.
			doubleEntriesDlg.setLocationRelativeTo(getFrame());
		}
		// display window
		ZettelkastenApp.getApplication().show(doubleEntriesDlg);
		// and start background task to search for double entries.
		doubleEntriesDlg.startTask();
	}

	/**
	 * Opens a file dialog and lets the user choose a zkn3-file. Then a method in
	 * the CLoadSave-class is called to open the file and store the data in the
	 * CData-class.
	 */
	@Action
	public void openDocument() {
		// first check whether we have unsaved changes, when the user wants
		// to open a new data-file
		if (askUserAndMaybeSaveChanges(getResourceMap().getString("msgSaveChangesTitle"))) {
			// retrieve current filepath
			File loadfile = settings.getMainDataFile();
			// retrieve filename and extension
			String filedir = null;
			String filename = null;
			// check whether we have any valid filepath at all
			if (loadfile != null && loadfile.exists()) {
				filedir = loadfile.toString();
				filename = loadfile.getName();
			}
			// create a swing filechooser when we have no mac
			File filepath = FileOperationsUtil.chooseFile(getFrame(),
					(settings.isMacStyle()) ? FileDialog.LOAD : JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY,
					filedir, filename, getResourceMap().getString("fileDialogTitleOpen"),
					new String[] { Constants.ZKN_FILEEXTENSION, Constants.ZKN_BACKUPFILEEXTENSION },
					getResourceMap().getString("fileDescription1"), settings);
			if ((filepath != null) && filepath.exists()) {
				// check whether opened file is a backup-file
				if (filepath.toString().toLowerCase().endsWith(Constants.ZKN_BACKUPFILEEXTENSION.toLowerCase())) {
					try {
						// create a dummy-file with the original file-extension, so we can backup that
						// original file
						File fp = new File(filepath.toString().replace(Constants.ZKN_BACKUPFILEEXTENSION,
								Constants.ZKN_FILEEXTENSION));
						// in case the user already created a backup, we concatenate a trainling
						// backup-counter-number to avoid overwriting existing backup-files
						File checkbackup = FileOperationsUtil.getBackupFilePath(fp);
						// rename original file and append ".backup" as extension
						fp.renameTo(checkbackup);
						// rename backup-file to original file-name...
						filepath.renameTo(fp);
						// and update variable
						filepath = fp;
						// tell user that the backup-file has been loaded and the old original file
						// backuped
						JOptionPane.showMessageDialog(getFrame(),
								getResourceMap().getString("backupLoadedMsg", "\"" + checkbackup.getName() + "\"",
										System.lineSeparator() + System.lineSeparator() + "\""
												+ fp.toString().substring(0,
														fp.toString().lastIndexOf(File.separatorChar))
												+ "\"" + System.lineSeparator() + System.lineSeparator(),
										"\"" + fp.getName() + "\""),
								getResourceMap().getString("backupLoadedTitle"), JOptionPane.PLAIN_MESSAGE);
					} catch (SecurityException | NullPointerException e) {
						Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
					}
				}
				// save new filepath
				Constants.zknlogger.log(Level.INFO, "Setting data file to new Open Document file: {0}.",
						filepath.toString());
				settings.setMainDataFile(filepath);
				// when all information are ready, load the document by opening
				// a modal dialog, which opens the data via a background task. the
				// dialog only displays a progressbar and an animated busyicon while
				// opening the file, no user-interaction possible....
				loadDataDocumentFromSettings();
			}
		}
	}

	/**
	 * Opens the file {@code fp} and asks to save changes before opening the file
	 *
	 * @param fp the data file to be opened
	 */
	public void openRecentDocument(String fp) {
		// first check whether we have unsaved changes, when the user wants
		// to open a new data-file
		if (askUserAndMaybeSaveChanges(getResourceMap().getString("msgSaveChangesTitle"))) {
			// create filepath from paramter
			File filepath = new File(fp);
			// if file exists, open it
			if (filepath.exists()) {
				// save new filepath
				Constants.zknlogger.log(Level.INFO, "Setting data file to open a selected recent document file: {0}",
						filepath.toString());
				settings.setMainDataFile(filepath);
				// when all information are ready, load the document by opening
				// a modal dialog, which opens the data via a background task. the
				// dialog only displays a progressbar and an animated busyicon while
				// opening the file, no user-interaction possible....
				loadDataDocumentFromSettings();
			}
		}
	}

	// Caller is responsible for closing the returned InputStream.
	private ZipInputStream fileInputStreamInsideZip(File filepath, String entryName) {
		ZipInputStream result = null;
		ZipInputStream zip = null;
		try {
			zip = new ZipInputStream(new FileInputStream(filepath));
			ZipEntry entry;
			// Iterate over the files inside the zip-file.
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().equals(entryName)) {
					// Return result, make zip null, so it won't be closed by the finally block.
					result = zip;
					break;
				}
			}
		} catch (IOException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			return null;
		} finally {
			// Close zip only if result is invalid.
			try {
				if (result == null) {
					zip.close();
				}
			} catch (IOException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
				// We are only reading it here. We can ignore a failure to close.
			}
		}
		return result;
	}

	// This reads one Document at a time since SAXBuilder closes the InputStream.
	private Document loadXMLDocInsideZip(File filepath, String entryName) {
		ZipInputStream zip = fileInputStreamInsideZip(filepath, entryName);
		if (zip == null) {
			// Didn't find entryName in the zip file.
			return null;
		}

		Document doc;
		try {
			// SAXBuilder will close the zip if successful.
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(zip);
			return doc;
		} catch (JDOMException | IOException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());

			try {
				zip.close();
			} catch (IOException e2) {
				Constants.zknlogger.log(Level.WARNING, e2.getLocalizedMessage());
				// We are only reading it here. We can ignore a failure to close.
			}

			return null;
		}
	}

	private boolean loadMainDataZipDocument(File filepath, Daten daten, Bookmarks bookmarks,
			SearchRequests searchRequests, DesktopData desktopData, Synonyms synonyms, BibTeX bibTeX) {
		// Reset the objects associated with the main data zip document.
		daten.reset();
		bookmarks.clear();
		searchRequests.clear();
		desktopData.clear();
		synonyms.clear();

		Constants.zknlogger.log(Level.INFO, "Opening main data file {0}", filepath);

		bibTeX.openFile(fileInputStreamInsideZip(filepath, Constants.bibTexFileName), "UTF-8");

		// Failures sets the doc to null.
		daten.setMetaInformationData(loadXMLDocInsideZip(filepath, Constants.metainfFileName));
		daten.setZknData(loadXMLDocInsideZip(filepath, Constants.zknFileName));
		daten.setAuthorData(loadXMLDocInsideZip(filepath, Constants.authorFileName));
		daten.setKeywordData(loadXMLDocInsideZip(filepath, Constants.keywordFileName));

		bookmarks.setBookmarkData(loadXMLDocInsideZip(filepath, Constants.bookmarksFileName));
		searchRequests.setSearchData(loadXMLDocInsideZip(filepath, Constants.searchrequestsFileName));
		synonyms.setDocument(loadXMLDocInsideZip(filepath, Constants.synonymsFileName));

		desktopData.setDesktopData(loadXMLDocInsideZip(filepath, Constants.desktopFileName));
		desktopData
				.setDesktopModifiedEntriesData(loadXMLDocInsideZip(filepath, Constants.desktopModifiedEntriesFileName));
		desktopData.setDesktopNotesData(loadXMLDocInsideZip(filepath, Constants.desktopNotesFileName));

		Constants.zknlogger.log(Level.INFO, "Main data file load finished.");

		// after opening a new file, set modified state to false
		daten.setModified(false);
		daten.setMetaModified(false);
		searchRequests.setModified(false);
		desktopData.setModified(false);
		bookmarks.setModified(false);
		synonyms.setModified(false);
		bibTeX.setModified(false);
		return true;
	}

	/**
	 * load the document by opening a modal dialog, which opens the data via a
	 * background task. the dialog only displays a progressbar and an animated
	 * busyicon while opening the file, no user-interaction possible.... <br>
	 * <br>
	 * We have this part of code "outsourced" into an own method because we need
	 * this more often, e.g. when automatically loading the data at program
	 * start-up. <br>
	 * <br>
	 * Furthermore, we have the method "updateAfterOpen". We need this additional
	 * method because in that method we do all the stuff which e.g. has also to be
	 * made after importing files...
	 */
	private boolean loadDataDocumentFromSettings() {
		File documentFile = settings.getMainDataFile();
		if (documentFile == null) {
			Constants.zknlogger.log(Level.WARNING, "Could not open file! Filepath is null!");
			return false;
		}
		if (!documentFile.exists()) {
			Constants.zknlogger.log(Level.WARNING, "Could not open file {0}: it doesn't exist!",
					documentFile.toString());
			return false;
		}

		// If the backup is earlier than the document file, the user might want to load
		// the backup instead.
		if (isBackupMoreRecentThanDocument(documentFile)) {
			if (!askUserWhetherToLoadTheNewerBackup(documentFile)) {
				// User aborted the loading.
				return false;
			}
		}

		// Continue loading the file in documentFile.
		if (!loadMainDataZipDocument(documentFile, data, bookmarks, searchRequests, desktop, settings.getSynonyms(),
				bibtex)) {
			// Load failed.
			return false;
		}

		// Update recent-docs history.
		settings.addToRecentDocs(documentFile.toString());
		initRecentDocumentsMenuItems();

		// Check whether we have a file-version which is newer than the current
		// program-version. For example, the user is opening a newer data-file with an
		// older program-version.
		if (data.isIncompatibleFile()) {
			// Show error to the user and cancel the loading.
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("incompatibleDataFileMsg"),
					getResourceMap().getString("incompatibleDataFileTitle"), JOptionPane.PLAIN_MESSAGE);
			return false;
		}

		// Check whether we have a newer file-version available for the data file. If
		// yes, update the data file.
		if (data.isNewVersion()) {
			// Tell user that the data file is being updated.
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("updateDataMsg"),
					getResourceMap().getString("updateDataTitle"), JOptionPane.PLAIN_MESSAGE);
			if (taskDlg == null) {
				// Start the update and show the task dialog.
				taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_UPDATEFILE, settings, data,
						desktop, bibtex, false);
				// Center new dialog window.
				taskDlg.setLocationRelativeTo(getFrame());
			}
			waitForTaskDialog();
		}

		int entryNumber = getStartupEntryNumber();
		data.activateEntry(entryNumber);

		// Update display with the new data.
		updateDisplayAfterOpen();

		return true;
	}

	// The entry number to be shown at startup.
	private int getStartupEntryNumber() {
		int startupEntryFromCommandLine = settings.getStartupEntryFromCommandLine();
		if (startupEntryFromCommandLine != -1 && startupEntryFromCommandLine <= data.getCount(Daten.ZKNCOUNT)) {
			return startupEntryFromCommandLine;
		}

		int mode = settings.getStartupEntryNumberMode();
		if (mode == Settings.SHOWATSTARTUP_FIRST) {
			return 1;
		} else if (mode == Settings.SHOWATSTARTUP_LAST) {
			return settings.getStartupEntry();
		} else if (mode == Settings.SHOWATSTARTUP_RANDOM) {
			return (int) (Math.random() * data.getCount(Daten.ZKNCOUNT)) + 1;
		}
		return 1;
	}

	/**
	 * 
	 * Ask the user whether he wants to load the original file, the newer
	 * backup-file or cancel the complete load-operation. The user is given three
	 * options:<br>
	 * <br>
	 * 1. Cancel the load altogether. We clear the current data file in the
	 * settings.<br>
	 * 2. Load the newer backup. We create a copy of the original file with a
	 * .backup extension and set the newer backup as the current data file in the
	 * settings.<br>
	 * 3. Load the original, older file. We don't do anything, so the curent data
	 * file in the settings is kept to the original file.<br>
	 * 
	 * @param documentFile original file
	 */
	private boolean askUserWhetherToLoadTheNewerBackup(File documentFile) {
		Object[] options = { getResourceMap().getString("newerBackupOptionsBackup"), // YES_OPTION
				getResourceMap().getString("newerBackupOptionsDatafile"), // NO_OPTION
				getResourceMap().getString("newerBackupOptionsCancel") }; // CANCEL_OPTION
		// Show options and wait for user input.
		int option = JOptionPane.showOptionDialog(getFrame(), getResourceMap().getString("newerBackupMsg"),
				getResourceMap().getString("newerBackupTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
			// Clear current data file in settings as the user cancelled the loading of this
			// file.
			Constants.zknlogger.log(Level.INFO,
					"Setting data file to none after user canceled loading both the original file and its newer backup.");
			settings.setMainDataFile(null);
			return false;
		} else if (JOptionPane.YES_OPTION == option) {
			// The user wants to open the backup-file instead of the older, original file.
			File newerBackupfile = getBackupFileForDocument(documentFile);
			try {
				// In case the user already created a backup, we concatenate a trailing
				// backup-counter-number to avoid overwriting existing backup-files
				// we start with a "1".
				int backupcounter = 1;
				String backupext = ".backup";
				// First, create basic backup-file.
				File checkbackup = new File(documentFile.toString() + backupext);
				while (checkbackup.exists()) {
					// Keep increasing the extension-counter until a filename is available.
					backupcounter++;
					backupext = ".backup-" + String.valueOf(backupcounter);
					checkbackup = new File(documentFile.toString() + backupext);
				}
				// Rename the original file to a backup with a ".backup" extension.
				documentFile.renameTo(checkbackup);

				// Rename the newer backup-file to the original filename.
				newerBackupfile.renameTo(settings.getMainDataFile());

				// Tell user that the backup-file has been loaded and the old original file
				// was backup-ed.
				JOptionPane.showMessageDialog(getFrame(),
						getResourceMap().getString("backupLoadedMsg", "\"" + checkbackup.getName() + "\"",
								System.lineSeparator() + System.lineSeparator() + "\""
										+ documentFile.toString().substring(0,
												documentFile.toString().lastIndexOf(File.separatorChar))
										+ "\"" + System.lineSeparator() + System.lineSeparator(),
								"\"" + documentFile.getName() + "\""),
						getResourceMap().getString("backupLoadedTitle"), JOptionPane.PLAIN_MESSAGE);
			} catch (SecurityException | NullPointerException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	private File getBackupFileForDocument(File documentFile) {
		return new File(
				documentFile.toString().replace(Constants.ZKN_FILEEXTENSION, Constants.ZKN_BACKUPFILEEXTENSION));
	}

	private boolean isBackupMoreRecentThanDocument(File documentFile) {
		File backupfile = getBackupFileForDocument(documentFile);
		if (backupfile.exists()) {
			long modifiedOriginal = documentFile.lastModified();
			long modifiedBackup = backupfile.lastModified();
			return modifiedOriginal < modifiedBackup;
		}
		return false;
	}

	private boolean saveMainDataFileInSettings() {
		isSaving = true;
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SAVE, data, bookmarks, searchRequests,
					desktop, settings.getSynonyms(), settings, bibtex);
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		isSaving = false;

		updateDisplay();

		// Show error icon if failed.
		if (!data.isSaveOk()) {
			showErrorIcon();
			return false;
		}

		return true;
	}

	/**
	 * Saves the document. Opens a file dialog and then calls a method the the
	 * CLoadSave-class to save the data in the default file format (zkn3)
	 *
	 * @return {@code true} if save was successful, {@code false} otherwise
	 */
	@Action(enabledProperty = "saveEnabled")
	public boolean saveDocument() {
		// When autobackup is running, prevent saving and tell user to wait a moment.
		// TODO Instead of asking the user to try later, wait for autobackup to finish
		// and continue.
		if (isAutoBackupRunning()) {
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("waitForAutobackupMsg"),
					getResourceMap().getString("waitForAutobackupTitle"), JOptionPane.PLAIN_MESSAGE);
			return false;
		}

		File fp = settings.getMainDataFile();
		if (fp == null || !fp.exists()) {
			// If no valid filepath exists, perform saveDocumentAs to create new file.
			return saveDocumentAs();
		}

		return saveMainDataFileInSettings();
	}

	/**
	 * This method saves the data under a new filename. This method will always be
	 * called when no filepath is set in the dataobject.
	 *
	 * @return {@code true}, when a valid filename was given. false otherwise, or
	 *         when cancelled
	 */
	@Action(enabledProperty = "entriesAvailable")
	public boolean saveDocumentAs() {
		// When autobackup is running, prevent saving and tell user to wait a moment.
		// TODO Instead of asking the user to try later, wait for autobackup to finish
		// and continue.
		if (isAutoBackupRunning()) {
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("waitForAutobackupMsg"),
					getResourceMap().getString("waitForAutobackupTitle"), JOptionPane.PLAIN_MESSAGE);
			return false;
		}

		// Ask to the user for the new file filepath.
		File filepath = FileOperationsUtil.chooseFile(getFrame(),
				(settings.isMacStyle()) ? FileDialog.SAVE : JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, null,
				null, getResourceMap().getString("fileDialogTitleSave"), new String[] { Constants.ZKN_FILEEXTENSION },
				getResourceMap().getString("fileDescription1"), settings);
		if (filepath == null) {
			return false;
		}

		// Add the zkn file extension if necessary.
		if (!filepath.getName().toLowerCase().endsWith(Constants.ZKN_FILEEXTENSION)) {
			filepath = new File(filepath.getPath() + Constants.ZKN_FILEEXTENSION);
		}

		// Make sure the file exists.
		if (!filepath.exists()) {
			try {
				filepath.createNewFile();
			} catch (IOException ex) {
				Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
			}
		} else {
			// User selected a file that already exists, for safety, ask user to confirm the
			// overwrite.
			int optionDocExists = JOptionPane.showConfirmDialog(getFrame(),
					getResourceMap().getString("askForOverwriteFileMsg"),
					getResourceMap().getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			// If the user does *not* choose to overwrite, cancel the save.
			if (optionDocExists != JOptionPane.YES_OPTION) {
				return false;
			}
		}

		Constants.zknlogger.log(Level.INFO, "Setting data file to new Save As file: {0}.", filepath.toString());
		settings.setMainDataFile(filepath);
		return saveMainDataFileInSettings();
	}

	/**
	 * This method exports all bookmarks of the data-file to a file.<br>
	 * <br>
	 * This method fills a LinkedList with the export-data (i.e. the bookmarks).<br>
	 * <br>
	 * Then the method {@link #exportList(LinkedList, String)
	 * exportList(LinkedList,String)} is called, which is responsible for saving the
	 * data of this linked list to a file.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportBookmarks() {
		exportEntries(createExportBookmarks());
	}

	/**
	 * This method opens the bookmarks as new "search-request" in the
	 * search-results-window.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportBookmarksToSearch() {
		// create array with export entries
		int[] entries = createExportBookmarks();
		// copy all bookmarked entry-numbers to that array
		for (int cnt = 0; cnt < entries.length; cnt++) {
			entries[cnt] = bookmarks.getBookmarkEntry(cnt);
		}
		// append a time-string to description, so we always have a unique
		// search-description,
		// even if the user searches twice for the same searchterms
		DateFormat df = new SimpleDateFormat("kkmmss");
		// add search
		searchRequests.addSearch(new String[] { getResourceMap().getString("exportBookmarksSearch") },
				Constants.SEARCH_BOOKMARKS, Constants.LOG_OR, false, false, false, /* accentInsensitive= */false, false,
				entries, getResourceMap().getString("exportBookmarksSearchDesc") + " (" + df.format(new Date()) + ")",
				getResourceMap().getString("exportBookmarksSearchDesc"));
		// If dialog window isn't already created, do this now.
		if (null == searchResultsDlg) {
			searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
					settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
		}
		// show search results window
		ZettelkastenApp.getApplication().show(searchResultsDlg);
		// show latest search results by auto-selecting the last item in the combo-box
		searchResultsDlg.showLatestSearchResult();
		// enable window-menu-item, if we have loaded search results
		setSearchResultsAvailable(searchRequests.getCount() > 0);
	}

	private int[] createExportBookmarks() {
		// create array with all bookmark-categories
		String[] bookmarkCategories = new String[bookmarks.getCategoryCount() + 1];
		// first field is always "all"
		bookmarkCategories[0] = getResourceMap().getString("exportBookmarkCatAll");
		// retrieve bookmark-categories
		for (int cnt = 0; cnt < bookmarks.getCategoryCount(); cnt++) {
			bookmarkCategories[cnt + 1] = bookmarks.getCategory(cnt);
		}
		Object expo = JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("exportBookmarkCatMsg"),
				getResourceMap().getString("exportBookmarkCatTitle"), JOptionPane.PLAIN_MESSAGE, null,
				bookmarkCategories, null);
		// init variable
		int[] entries = null;
		// check for valid return value
		if (expo != null) {
			// selection was: all categories should be exported
			if (expo.toString().equals(getResourceMap().getString("exportBookmarkCatAll"))) {
				// copy all bookmarked entry-numbers to that array
				entries = bookmarks.getAllBookmarkedEntries();
			} // selection was a certain category
			else {
				entries = bookmarks.getBookmarkedEntriesFromCat(expo.toString());
			}
		}
		return entries;
	}

	/**
	 * This method exports all keywords of the data-file to a file.<br>
	 * <br>
	 * This method fills a LinkedList with the export-data (i.e. the keywords).<br>
	 * <br>
	 * Then the method {@link #exportList(LinkedList, String)
	 * exportList(LinkedList,String)} is called, which is responsible for saving the
	 * data of this linked list to a file.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportKeywords() {
		// get length of keyword file
		int len = data.getCount(Daten.KWCOUNT);
		// create linked list that will contain all keywords
		LinkedList<String> keywords = new LinkedList<>();
		// copy all keywords to a linked list
		for (int cnt = 0; cnt < len; cnt++) {
			keywords.add(data.getKeyword(cnt + 1));
		}
		// call export-method
		exportList(keywords, Daten.ELEMENT_KEYWORD);
	}

	/**
	 * This method imports literature entries/references from a given BibTeX file
	 * and adds the references as author entries to the authorFile.xml file of the
	 * data file. <br>
	 * <br A new dialog is opened (see {@link CImportBibTex}) where the user can
	 * select a BibTeX file to open, and a file format of that BibTeX file (which
	 * corresponds to the literature program that is used, e.g. Citavi, JabRef,
	 * Zotero, etc.). <br>
	 * <br>
	 * All BibTeX entries of this file are displayed in a table where the user can
	 * select the entries to be imported. Entries that have already been imported
	 * before are <i>not</i> listed in this table (these entries are identified by
	 * their BibTeX keys, i.e. if an existing author value has the same key as an
	 * entry in this BibTeX file). <br>
	 * <br>
	 * Beside importing the author-values, the user can optionally choose to create
	 * an entry for each imported BibTeX entry, in case the BibTeX entry has an
	 * abstract.
	 */
	@Action
	public void importAuthors() throws IOException {
		importBibTexDlg = new CImportBibTex(getFrame(), this, data, bibtex, settings);
		importBibTexDlg.setLocationRelativeTo(getFrame());
		ZettelkastenApp.getApplication().show(importBibTexDlg);
	}

	/**
	 * This method exports all authors of the data-file to a file.<br>
	 * <br>
	 * This method fills a LinkedList with the export-data (i.e. the
	 * author-value).<br>
	 * <br>
	 * Then the method {@link #exportList(LinkedList, String)
	 * exportList(LinkedList,String)} is called, which is responsible for saving the
	 * data of this linked list to a file.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportAuthors() {
		// get length of author file
		int len = data.getCount(Daten.AUCOUNT);
		// create linked list that will contain all authors
		LinkedList<String> authors = new LinkedList<>();
		// copy all authors to a linked list
		for (int cnt = 0; cnt < len; cnt++) {
			authors.add(data.getAuthor(cnt + 1));
		}
		// call export-method
		exportList(authors, "authors");
	}

	@Action
	public void attachBibtexFile() throws IOException {
		// retrieve attached bibtex-file
		File selectedfile = bibtex.getCurrentlyAttachedFile();
		// if we have no attached file, set last used file as filepath
		if (selectedfile == null || !selectedfile.exists()) {
			selectedfile = bibtex.getFilePath();
		}
		selectedfile = FileOperationsUtil.chooseFile(getFrame(),
				(settings.isMacStyle()) ? FileDialog.LOAD : JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY,
				(selectedfile == null) ? null : selectedfile.toString(),
				(selectedfile == null) ? null : selectedfile.getName(),
				getResourceMap().getString("bibTextFileChooserTitle"), new String[] { ".bib", ".txt" },
				getResourceMap().getString("bibTexDesc"), settings);
		if (selectedfile != null) {
			// set new bibtex-filepath
			bibtex.setFilePath(selectedfile);
			// detach current bibtex file
			bibtex.detachCurrentlyAttachedFile();
			// show input-dialog offering the choice of bibtex-encoding
			Object encodingchoice = JOptionPane.showInputDialog(getFrame(),
					getResourceMap().getString("bibtexEncodingsMsg"),
					getResourceMap().getString("bibtexEncodingsTitle"), JOptionPane.PLAIN_MESSAGE, null,
					Constants.BIBTEX_DESCRIPTIONS, Constants.BIBTEX_DESCRIPTIONS[settings.getLastUsedBibtexFormat()]);
			// if user did not cancel the operation, go on and open the bibtex-file
			if (encodingchoice != null) {
				// iterate all availabe bibtex-encodings.
				// if the appropriate encoding that matched the user's choice was found,
				// use that index-number to open the bibtex-file
				for (int enc = 0; enc < Constants.BIBTEX_DESCRIPTIONS.length; enc++) {
					if (encodingchoice.toString().equals(Constants.BIBTEX_DESCRIPTIONS[enc])) {
						settings.setLastUsedBibtexFormat(enc);
						break;
					}
				}
				// open selected file, using the character encoding of the related
				// reference-manager (i.e.
				// the programme that has exported the bib-tex-file).
				if (bibtex.openAttachedFile(Constants.BIBTEX_ENCODINGS[settings.getLastUsedBibtexFormat()], false)) {
					// tell about success
					Constants.zknlogger.log(Level.INFO, "BibTeX-File was successfully attached.");
					// tell user about success
					JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("bibtexAttachOkMsg"),
							getResourceMap().getString("bibtexAttachOkTitle"), JOptionPane.PLAIN_MESSAGE);
				} else {
					// tell about fail
					Constants.zknlogger.log(Level.INFO, "BibTeX-File could not be found nor attached.");
				}
			}
		}
	}

	/**
	 *
	 */
	@Action(enabledProperty = "bibtexFileLoaded")
	public void refreshBibTexFile() throws IOException {
		// retrieve current filepath of bibtex file
		File bibfile = bibtex.getFilePath();
		// check whether file already exists
		if (bibfile != null && bibfile.exists()) {
			// detach current bibtex file
			bibtex.detachCurrentlyAttachedFile();
			// open selected file, using the character encoding of the related
			// reference-manager (i.e.
			// the programme that has exported the bib-tex-file).
			if (bibtex.refreshBibTexFile(settings)) {
				// If dialog window isn't already created, do this now.
				if (taskDlg == null) {
					// get parent und init window
					taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_REFRESHBIBTEX, taskinfo, data,
							bibtex);
					// Center new dialog window.
					taskDlg.setLocationRelativeTo(getFrame());
				}
				waitForTaskDialog();

				// update author list
				showAuthors();
				// tell about success
				Constants.zknlogger.log(Level.INFO, "BibTeX-File was successfully refreshed.");
				// Constants.zknlogger.log(Level.INFO, "{0}{1}", new
				// Object[]{System.lineSeparator(), taskinfo.getUpdatedAuthors()});
			} else {
				// tell about fail
				Constants.zknlogger.log(Level.INFO, "BibTeX-File could not be found nor refreshed.");
			}
		}
	}

	/**
	 * This method exports all attachments of the data-file to a file.<br>
	 * <br>
	 * This method fills a LinkedList with the export-data (i.e. the
	 * attachment-values).<br>
	 * <br>
	 * Then the method {@link #exportList(LinkedList, String)
	 * exportList(LinkedList,String)} is called, which is responsible for saving the
	 * data of this linked list to a file.
	 */
	@Action(enabledProperty = "exportPossible")
	public void exportAttachments() {
		refreshAttachmentList();
		// get length of attachment-table
		int len = jTableAttachments.getRowCount();
		// create linked list that will contain all attachments
		LinkedList<String> attachments = new LinkedList<>();
		// copy all attachments to a linked list
		for (int cnt = 0; cnt < len; cnt++) {
			attachments.add(jTableAttachments.getValueAt(cnt, 0).toString());
		}
		// call export-method
		exportList(attachments, "attachments");
	}

	/**
	 *
	 * @param exportlist
	 * @param type
	 */
	private void exportList(LinkedList<String> exportlist, String type) {
		String formats = getResourceMap().getString("exportListFormat1") + ","
				+ getResourceMap().getString("exportListFormat2") + ","
				+ getResourceMap().getString("exportListFormat3");
		if (type.equalsIgnoreCase("authors")) {
			formats = formats + "," + getResourceMap().getString("exportListFormat4");
		}
		Object[] choice = formats.split(",");
		Object expo = JOptionPane.showInputDialog(getFrame(), getResourceMap().getString("exportListFormatMsg"),
				getResourceMap().getString("exportListFormatTitle"), JOptionPane.PLAIN_MESSAGE, null, choice, null);
		// check for valid return value or cancel-operation of user
		if (expo != null) {
			// convert object to string
			String exportformat = expo.toString();
			// check for valid file extenstion
			if (exportformat.equalsIgnoreCase(getResourceMap().getString("exportListFormat4"))) {
				exportformat = "bib";
			}
			// here we open a swing filechooser, in case the os ist no mac aqua
			File filepath = FileOperationsUtil.chooseFile(getFrame(),
					(settings.isMacStyle()) ? FileDialog.SAVE : JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, null,
					null, getResourceMap().getString("exportListFormatTitle"),
					new String[] { "." + exportformat.toLowerCase() }, expo.toString(), settings);
			// if we have any valid
			if (filepath != null) {
				// init extension-string
				String ext = null;
				// retrieve fileextension, in case the user does not enter a fileextension
				// later...
				if (exportformat.equals(getResourceMap().getString("exportListFormat1"))) {
					ext = "." + getResourceMap().getString("exportListFormat1").toLowerCase();
				}
				if (exportformat.equals(getResourceMap().getString("exportListFormat2"))) {
					ext = "." + getResourceMap().getString("exportListFormat2").toLowerCase();
				}
				if (exportformat.equals(getResourceMap().getString("exportListFormat3"))) {
					ext = "." + getResourceMap().getString("exportListFormat3").toLowerCase();
				}
				if (exportformat.equals("bib")) {
					ext = ".bib";
				}
				// check whether the user entered a file extension. if not, add "ext" as
				// extension
				if (!filepath.getName().toLowerCase().endsWith(ext)) {
					filepath = new File(filepath.getPath() + ext);
				}
				// if file does not exist, create it - otherwise the getFilePath-method of
				// the settings-class would return "null" as filepath, if file doesn't exist
				if (!filepath.exists()) {
					try {
						filepath.createNewFile();
					} catch (IOException ex) {
						Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
					}
				} else {
					// file exists, ask user to overwrite it...
					int optionDocExists = JOptionPane.showConfirmDialog(getFrame(),
							getResourceMap().getString("askForOverwriteFileMsg"),
							getResourceMap().getString("askForOverwriteFileTitle"), JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE);
					// if the user does *not* choose to overwrite, quit...
					if (optionDocExists != JOptionPane.YES_OPTION) {
						return;
					}
				}

				// create variable that indicates errors...
				boolean errorOccured = false;
				// sort list aphabetically before exporting it
				Collections.sort(exportlist, new Comparer());
				// here startes the export of xml-data
				if (exportformat.equals(getResourceMap().getString("exportListFormat1"))) {
					// create new document
					Document exportfile = new Document(new Element(type));
					// create list-iterator
					Iterator<String> i = exportlist.iterator();
					// iterate exportlist
					while (i.hasNext()) {
						// create new element
						Element e = new Element(Daten.ELEMENT_ENTRY);
						// at element from the list
						e.setText(i.next());
						// add element to the document
						exportfile.getRootElement().addContent(e);
					}
					// save the xml-file
					try {
						// open the outputstream
						FileOutputStream fos = new FileOutputStream(filepath);
						// create a new XML-outputter with the pretty output format,
						// so the xml-file looks nicer
						XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
						try {
							// save the main-export-file
							out.output(exportfile, fos);
						} catch (IOException e) {
							// log error-message
							Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
							errorOccured = true;
							// show error message
							JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errorSavingMsg"),
									getResourceMap().getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
						} finally {
							try {
								// close the output stream
								fos.close();
							} catch (IOException e) {
								// log error-message
								Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
								errorOccured = true;
							}
						}
					} catch (FileNotFoundException ex) {
						// log error-message
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						errorOccured = true;
						// show error message
						JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errorSavingMsg"),
								getResourceMap().getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
					}
				} else if (exportformat.equals("bib")) {
					ByteArrayOutputStream bout = null;
					OutputStream exportfile = null;
					try {
						bout = bibtex.saveFile();
						// create filewriter
						exportfile = new FileOutputStream(filepath);
						// retrieve string
						String bibdata = bout.toString("UTF-8");
						// write content
						exportfile.write(bibdata.getBytes(Charset.forName("UTF-8")));
					} catch (FileNotFoundException ex) {
						// log error-message
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						errorOccured = true;
					} catch (IOException ex) {
						// log error-message
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						errorOccured = true;
					} finally {
						try {
							if (bout != null) {
								bout.close();
							}
							if (exportfile != null) {
								exportfile.close();
							}
						} catch (IOException ex) {
							// log error-message
							Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
							errorOccured = true;
						}
					}
				} else {
					// create stringbuilder for the final content
					StringBuilder finalcontent = new StringBuilder("");
					// create list-iterator
					Iterator<String> i = exportlist.iterator();
					// here startes the export of txt-data
					if (exportformat.equals(getResourceMap().getString("exportListFormat2"))) {
						// iterate exportlist and copy each list-element to the string, separated by new
						// lines
						while (i.hasNext()) {
							finalcontent.append(i.next()).append(System.lineSeparator());
						}
					} // here startes the export of html-data
					else if (exportformat.equals(getResourceMap().getString("exportListFormat3"))) {
						// init html-page
						finalcontent.append("<html><head></head><body><ol>").append(System.lineSeparator());
						// iterate exportlist and copy each list-element to the string, separated by new
						// lines
						while (i.hasNext()) {
							// create dummy string to convert German umlauts
							String dummy = i.next();
							// convert special chars to html
							dummy = dummy.replace("&", "&amp;");
							dummy = dummy.replace("\"", "&quot;");
							dummy = dummy.replace("ä", "&auml;");
							dummy = dummy.replace("Ä", "&Auml;");
							dummy = dummy.replace("ö", "&ouml;");
							dummy = dummy.replace("Ö", "&Ouml;");
							dummy = dummy.replace("ü", "&uuml;");
							dummy = dummy.replace("Ü", "&Uuml;");
							dummy = dummy.replace("ß", "&szlig;");
							// append converted author to stringbuilder
							finalcontent.append("<li>").append(dummy).append("</li>").append(System.lineSeparator());
						}
						// close html-page
						finalcontent.append(System.lineSeparator()).append("</ol></body></html>");
					}
					// init output-filewriter
					Writer exportfile = null;
					try {
						// create filewriter
						exportfile = new FileWriter(filepath);
						// and save file to disk
						exportfile.write(finalcontent.toString());
					} catch (IOException ex) {
						// log error-message
						Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
						errorOccured = true;
						// show error message
						JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errorSavingMsg"),
								getResourceMap().getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
					} finally {
						try {
							// finally, close filewrite
							if (exportfile != null) {
								exportfile.close();
							}
						} catch (IOException ex) {
							// log error-message
							Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
							errorOccured = true;
							// show error message
							JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errorSavingMsg"),
									getResourceMap().getString("errorSavingTitle"), JOptionPane.PLAIN_MESSAGE);
						}
					}
				}
				// if an errors occured, show error-log
				if (errorOccured) {
					showErrorIcon();
				} else {
					JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("exportOkMsg"),
							getResourceMap().getString("exportOkTitle"), JOptionPane.PLAIN_MESSAGE);
				}
			}
		}
	}

	/**
	 * This methods goes back through the history and sets the current entry to the
	 * related entry in the history...
	 */
	@Action(enabledProperty = "historyBackAvailable")
	public void historyBack() {
		data.historyBack();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * goToFirstParentEntry goes to the first parent entry of the current entry. If
	 * the current entry is invalid or does not have a parent entry, it does
	 * nothing.
	 */
	@Action
	public void goToFirstParentEntry() {
		data.goToFirstParentEntry();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * This methods goes fore through the history and sets the current entry to the
	 * related entry in the history...
	 */
	@Action(enabledProperty = "historyForAvailable")
	public void historyFor() {
		data.historyFore();

		// Reset displayedZettel.
		displayedZettel = -1;

		updateDisplay();
	}

	/**
	 * Copies the current selection, or the whole text if no selection is made, of
	 * the textfield which currently has the focus to the clipboard. If no textfield
	 * is the focus owner, the selected values of the list or table which has the
	 * focus is copied to the clipboard.
	 */
	@Action
	public void selectAllText() {
		// look for the component which has the focus and copy the (selected) text
		if (jEditorPaneEntry.isFocusOwner()) {
			jEditorPaneEntry.selectAll();
		}
		if (jListEntryKeywords.isFocusOwner()) {
			jListEntryKeywords.setSelectionInterval(0, keywordListModel.size() - 1);
		}
		if (jTableKeywords.isFocusOwner()) {
			jTableKeywords.selectAll();
		}
		if (jTableAuthors.isFocusOwner()) {
			jTableAuthors.selectAll();
		}
		if (jTableTitles.isFocusOwner()) {
			jTableTitles.selectAll();
		}
		if (jTableAttachments.isFocusOwner()) {
			jTableAttachments.selectAll();
		}
	}

	/**
	 * Shows the search results window. If it hasn't been created yet, a new
	 * instance will be created. <br>
	 * <br>
	 * The window is modal, thus we don't wait for reactions here.
	 */
	@Action(enabledProperty = "searchResultsAvailable")
	public void showSearchResultWindow() {
		if (null == searchResultsDlg) {
			searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
					settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
		}
		ZettelkastenApp.getApplication().show(searchResultsDlg);
	}

	/**
	 * Shows the desktop/outliner window. If it hasn't been created yet, a new
	 * instance will be created. <br>
	 * <br>
	 * The window is modal, thus we don't wait for reactions here.
	 */
	@Action(enabledProperty = "desktopAvailable")
	public void showDesktopWindow() {
		if (null == desktopDlg)
			desktopDlg = new DesktopFrame(this, taskinfo, data, bookmarks, desktop, settings,
					settings.getAcceleratorKeys(), bibtex, settings.getAutoKorrektur(), settings.getStenoData());
		ZettelkastenApp.getApplication().show(desktopDlg);
	}

	/**
	 * Shows the desktop/outliner window. If it hasn't been created yet, a new
	 * instance will be created. <br>
	 * <br>
	 * The window is modal, thus we don't wait for reactions here.
	 */
	@Action
	public void showEntryInDesktopWindow() {
		showEntryInDesktopWindow(displayedZettel);
	}

	public void showEntryInDesktopWindow(int nr) {
		// check for valid value
		if (-1 == nr)
			return;
		// check whether desktop frame is already open. if not, create one
		if (null == desktopDlg)
			desktopDlg = new DesktopFrame(this, taskinfo, data, bookmarks, desktop, settings,
					settings.getAcceleratorKeys(), bibtex, settings.getAutoKorrektur(), settings.getStenoData());
		// show frame
		ZettelkastenApp.getApplication().show(desktopDlg);
		// show entry on desktop
		desktopDlg.showEntryInDesktop(nr);
	}

	/**
	 * Retrieves the selected text and adds it as new keyword to the entry.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void addToKeywordList() {
		// retrieve selected text
		String selection = jEditorPaneEntry.getSelectedText();
		// check whether we have any selection at all
		if (selection != null && !selection.isEmpty()) {
			// TODO zeilenumbrüche auslesen
			// since new-lines are not recognized when selecting text (instead, new lines
			// are
			// simple space-chars, thus multiple lines appear as one line with several
			// space-separated word)
			// we need to check whether the selected text appears as whole phrase in the
			// entry
			// ...
			// add it to keywords
			if (!addKeywords(new String[] { selection.trim() }, true))
				JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("noNewKeywordsFoundMsg"),
						getResourceMap().getString("noNewKeywordsFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * Retrieves the selected text and sets it as entry's title.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void setSelectionAsTitle() {
		// retrieve selected text
		String selection = jEditorPaneEntry.getSelectedText();
		// check whether we have any selection at all
		if (selection != null && !selection.isEmpty()) {
			// set new title
			data.setZettelTitle(displayedZettel, selection.trim());
			// change edited timestamp
			data.changeEditTimeStamp(displayedZettel);
			updateDisplay();
		}
	}

	/**
	 * Retrieves the first text line and sets it as entry's title.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void setFirstLineAsTitle() {
		// retrieve selected text
		String title = data.getCleanZettelContent(displayedZettel);
		// check whether we have any selection at all
		if (title != null && !title.isEmpty()) {
			// remove carriage returns
			title = title.replace("\r", "");
			// set new title
			data.setZettelTitle(displayedZettel, title.split("\n")[0].trim());
			// change edited timestamp
			data.changeEditTimeStamp(displayedZettel);
			updateDisplay();
		}
	}

	/**
	 * Retrieves the first text line and sets it as entry's title. This method is
	 * called from the titles-tab and used for automatically setting titles to all
	 * entries that have no titles yet.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void automaticFirstLineAsTitle() {
		// ask user if he wants to remove the lines that have been set as title
		int option = JOptionPane.showConfirmDialog(getFrame(), getResourceMap().getString("removeTitleLineMsg"),
				getResourceMap().getString("removeTitleLineTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		// if user cancelled or closed the dialog, do nothing.
		if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
			return;
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SETFIRSTLINEASTITLE, data, option);
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// reset title-list
		data.setTitlelistUpToDate(false);
		updateDisplay();
	}

	/**
	 * Opens the find dialog and sets the text selection as default search term.
	 */
	@Action(enabledProperty = "textSelected")
	public void findFromSelection() {
		// retrieve selected text
		String selection = jEditorPaneEntry.getSelectedText();
		// check whether we have any selection at all
		if (selection != null && !selection.isEmpty()) {
			find(selection.trim());
		}
	}

	/**
	 * Opens the find dialog.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void find() {
		find(null);
	}

	/**
	 * Opens the replace dialog.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void replace() {
		replace(getFrame(), null, null);
	}

	/**
	 * Opens the find dialog. An optional initial value {@code initSearchTerm} can
	 * be supplied as parameter.
	 * 
	 * @param initSearchTerm (optional) - can be used to set an initial search term.
	 *                       can be {@code null} if not used.
	 */
	private void find(String initSearchTerm) {
		if (searchDlg == null) {
			searchDlg = new CSearchDlg(getFrame(), searchRequests, settings, initSearchTerm);
			// Center new dialog window.
			searchDlg.setLocationRelativeTo(getFrame());
		}
		ZettelkastenApp.getApplication().show(searchDlg);
		// open the search dialog
		// the parameters are as following:
		if (!searchDlg.isCancelled())
			startSearch(searchDlg.getSearchTerms(), // - string-array with search results
					searchDlg.getWhereToSearch(), // - the type of search, i.e. where to look
					searchDlg.getLogical(), // - logical-and-combination
					searchDlg.isWholeWord(), // - whole words
					searchDlg.isMatchCase(), // - case-sensitive search
					searchDlg.isSynonymsIncluded(), // - whether synonyms are included in the search or not
					searchDlg.isAccentInsensitive(), // accent-insensitive search
					searchDlg.isRegExSearch(), // - whether the search term is a regular expression
					searchDlg.isTimestampSearch(), // - whether the search is limited to a certain period of time
													// (creation or change date of entry)
					searchDlg.getDateFromValue(), // - the start of the time period
					searchDlg.getDateToValue(), // - the end of the time period
					searchDlg.getTimestampIndex(), // - whether the user wants to look for a time period of the
													// *creation* or *edited* time stamp
					false, // - whether the search results are only needed for internal use (e.g. manual
							// links, desktop etc.) or used as "real" search results
					Constants.STARTSEARCH_USUAL, // - the type of search (usual, within authors etc.)
					Constants.SEARCH_USUAL);
		// dispose window after closing
		searchDlg.dispose();
		searchDlg = null;
	}

	/**
	 * Opens the replace-dialog and optionally sets the initial values
	 * {@code initSearchTerm} and {@code replaceentries}. If not cancelled, the
	 * requests search term will be replaced by its entered find term. applies to
	 * those domains (content, titles, keywords) which have been checked in the
	 * replace dialog.
	 *
	 * @param frame
	 * @param initSearchTerm optional. an initial search term that will be
	 *                       automatically set when the dialog is opened. use
	 *                       {@code null} if not used.
	 * @param replaceentries an integer-array that contains the entry-numbers where
	 *                       the replacements should be applied to. use {@code null}
	 *                       to find and replace in <i>all</i> entries. else, if
	 *                       replacements should only be done within certain entries
	 *                       (e.g. search results), simply pass the entries' numbers
	 *                       as integer-array.
	 * @return {@code true} if replacement was done, false if it was cancelled.
	 */
	public boolean replace(JFrame frame, String initSearchTerm, int[] replaceentries) {
		// If dialog window isn't already created, do this now.
		if (null == replaceDlg) {
			// create a new dialog window
			replaceDlg = new CReplaceDialog(frame, settings, initSearchTerm, (replaceentries != null));
			// Center new dialog window.
			replaceDlg.setLocationRelativeTo(frame);
		}
		ZettelkastenApp.getApplication().show(replaceDlg);
//      commented, since we want to allow empty find or replace terms
//        if (!replaceDlg.cancelled && (!replaceDlg.findTerm.isEmpty()&&!replaceDlg.replaceTerm.isEmpty()) ) {
		// when the user did not cancel, start replacement now.
		if (!replaceDlg.isCancelled()) {
			// If dialog window isn't already created, do this now.
			if (taskDlg == null) {
				// get parent und init window
				taskDlg = new TaskProgressDialog(frame, TaskProgressDialog.TASK_REPLACE, taskinfo, data,
						replaceDlg.getFindTerm(), // the find term
						replaceDlg.getReplaceTerm(), // the replace term
						replaceentries, // an array or entry numbers where the replacement should be applied to. Use
										// null to find and replace in all entries
						replaceDlg.getWhereToSearch(), // where to replace, i.e. authors, keywords, content...
						replaceDlg.isWholeWord(), // whether only whole words should be found
						replaceDlg.isMatchCase(), // whether search is case sensitive
						replaceDlg.isRegEx()); // whether find/replace-terms are regular expressions
				// Center new dialog window.
				taskDlg.setLocationRelativeTo(frame);
			}
			waitForTaskDialog();

			// show replace-results
			JOptionPane.showMessageDialog(frame, taskinfo.getReplaceMessage(),
					getResourceMap().getString("replace.Action.text"), JOptionPane.PLAIN_MESSAGE);
			updateDisplay();
		}
		// if search was cancelled, return false
		else {
			// dispose dialogs
			replaceDlg.dispose();
			replaceDlg = null;
			return false;
		}
		// dispose dialogs
		replaceDlg.dispose();
		replaceDlg = null;
		// return true...
		return true;
	}

	private void waitForTaskDialog() {
		// show() will block, wait on completion.
		ZettelkastenApp.getApplication().show(taskDlg);

		// Task dialog cleanup.
		taskDlg.dispose();
		taskDlg = null;
	}

	/**
	 * Starts a search request and finds entries that don't contain any keywords.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithoutKeywords() {
		findEntryWithout(Constants.SEARCH_NO_KEYWORDS);
	}

	/**
	 * Starts a search request and finds entries that don't have any remarks.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithoutRemarks() {
		findEntryWithout(Constants.SEARCH_NO_REMARKS);
	}

	/**
	 * Starts a search request and finds entries that have remarks.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithRemarks() {
		findEntryWithout(Constants.SEARCH_WITH_REMARKS);
	}

	/**
	 * Starts a search request and finds entries that have attachments.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithAttachments() {
		findEntryWithout(Constants.SEARCH_WITH_ATTACHMENTS);
	}

	/**
	 * Starts a search request and finds entries that don't contain any authors.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithoutAuthors() {
		findEntryWithout(Constants.SEARCH_NO_AUTHORS);
	}

	/**
	 * Starts a search request and finds entries that have been rated.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithRating() {
		findEntryWithout(Constants.SEARCH_WITH_RATINGS);
	}

	/**
	 * Starts a search request and finds entries that have <i>not</i> been rated.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithoutRating() {
		findEntryWithout(Constants.SEARCH_WITHOUT_RATINGS);
	}

	/**
	 * Starts a search request and finds entries that have <i>no</i> manual links.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findWithoutManualLinks() {
		findEntryWithout(Constants.SEARCH_WITHOUT_MANUAL_LINKS);
	}

	/**
	 * Starts a search request and finds entries that are part of any note sequence.
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findLuhmannAny() {
		findEntryWithout(Constants.SEARCH_IS_ANY_LUHMANN);
	}

	/**
	 * Starts a search request and finds entries that are top-level trailing entries
	 * (follower).
	 */
	@Action(enabledProperty = "moreEntriesAvailable")
	public void findLuhmannParent() {
		findEntryWithout(Constants.SEARCH_TOP_LEVEL_LUHMANN);
	}

	/**
	 * Starts a search request and finds entries <i>not</i> according to a certain
	 * find term, but according to missing or non-missing fields. With this function
	 * you can for instance search for entries that don't have keyword-values yet,
	 * or that don't have any remarks yet.
	 *
	 * @param what a value that indicates in which domains the user wants to search.
	 *             You can e.g. find entries without keywords, without authors or
	 *             with remarks etc.<br>
	 *             <br>
	 *             Use following constants:<br>
	 *             <ul>
	 *             <li>{@code CConstants.SEARCH_NO_AUTHORS} - finds entries
	 *             with<i>out</i> author values</li>
	 *             <li>{@code CConstants.SEARCH_NO_KEYWORDS} - finds entries
	 *             with<i>out</i> keyword values</li>
	 *             <li>{@code CConstants.SEARCH_NO_REMARKS} - finds entries
	 *             with<i>out</i> remarks</li>
	 *             <li>{@code CConstants.SEARCH_WITH_REMARKS} - finds entries with
	 *             remarks</li>
	 *             <li>{@code CConstants.SEARCH_WITH_ATTACHMENTS} - finds entries
	 *             with attachments</li>
	 *             <li>{@code CConstants.SEARCH_WITH_RATINGS} - finds entries that
	 *             have been rated</li>
	 *             </ul>
	 */
	private void findEntryWithout(int what) {
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SEARCH, data, searchRequests,
					settings.getSynonyms(), what, null, null, -1, Constants.LOG_OR, true, true, true,
					/* accentInsensitive= */false, false, false, null, null, 0, false,
					settings.getSearchRemovesFormatTags());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// check whether we have any search results at all
		if (searchRequests.getCurrentSearchResults() != null) {
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg)
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			// show search results window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		} else {
			// display error message box that nothing was found
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errNothingFoundMsg"),
					getResourceMap().getString("errNothingFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	@Action(enabledProperty = "moreEntriesAvailable")
	public void findEntriesWithTimeStampCreated() {
		findEntriesWithTimeStamp(Constants.SEARCH_WITH_CREATED_TIME, Constants.TIMESTAMP_CREATED,
				Constants.SEARCH_WITH_CREATED_TIME);
	}

	@Action(enabledProperty = "moreEntriesAvailable")
	public void findEntriesWithTimeStampEdited() {
		findEntriesWithTimeStamp(Constants.SEARCH_WITH_EDITED_TIME, Constants.TIMESTAMP_EDITED,
				Constants.SEARCH_WITH_EDITED_TIME);
	}

	/**
	 * This method searches for entries that have been either created or edited
	 * during a certain time-period. it is not necessary to provide a search term
	 * here.<br>
	 * <br>
	 * The user is being prompted to enter a startdate (start of search period) and
	 * end date. Depending on which menu-items calls this method, either all entries
	 * that have been created or edited during that period are shown in the search
	 * results window.
	 *
	 * @param wherestamp  in which part of the entry should be searched, necessary
	 *                    for search description. use
	 *                    {@code CConstants.SEARCH_WITH_CREATED_TIME} or
	 *                    {@code CConstants.SEARCH_WITH_EDITED_TIME}
	 * @param whichstamp  which timestamp is relevant use
	 *                    {@code CConstants.TIMESTAMP_CREATED} or
	 *                    {@code CConstants.TIMESTAMP_EDITED}
	 * @param whichsearch the type of search, relevant for the CStartSearch-class.
	 *                    use {@code CConstants.SEARCH_WITH_CREATED_TIME} or
	 *                    {@code CConstants.SEARCH_WITH_EDITED_TIME}
	 */
	private void findEntriesWithTimeStamp(int wherestamp, int whichstamp, int whichsearch) {
		// create default timestamp. this is only relevant for importing old data-files
		// (.zkn), because
		// entries of old data-files may not always have timestamps. to ensure each
		// entry has a time stamp
		// we offer the user to input a default date that is set for all entries that do
		// not have any timestamp
		String starttime = null;
		String stime = "";
		boolean cancelled = false;
		boolean inputIsOk = false;
		// wait for valid input
		while (!cancelled && !inputIsOk) {
			// show input-dialog
			starttime = (String) JOptionPane.showInputDialog(getFrame(), // parent window
					getResourceMap().getString("startTimeStampMsg"), // message text
					getResourceMap().getString("startTimeStampTitle"), // messagebox title
					JOptionPane.PLAIN_MESSAGE); // type of dialog
			// now convert the timestamp into something
			// therefore, check whether we have any valid input at all, if we have the
			// correct length (8 chars)
			// and if we have to "." at the right position. A valid input would be e.g.
			// "31.12.08" (dd.mm.yy)
			if (starttime != null && 8 == starttime.length() && starttime.charAt(2) == '.'
					&& starttime.charAt(5) == '.') {
				stime = starttime;
				starttime = starttime.substring(6) + starttime.substring(3, 5) + starttime.substring(0, 2);
				cancelled = false;
				inputIsOk = true;
			} else if (null == starttime || starttime.isEmpty()) {
				cancelled = true;
				inputIsOk = false;
			}
		}
		// check whether dialog was cancelled or not
		if (!inputIsOk && cancelled)
			return;
		// create default timestamp. this is only relevant for importing old data-files
		// (.zkn), because
		// entries of old data-files may not always have timestamps. to ensure each
		// entry has a time stamp
		// we offer the user to input a default date that is set for all entries that do
		// not have any timestamp
		String endtime = null;
		String etime = "";
		cancelled = false;
		inputIsOk = false;
		// wait for valid input
		while (!cancelled && !inputIsOk) {
			// show input-dialog
			endtime = (String) JOptionPane.showInputDialog(getFrame(), // parent window
					getResourceMap().getString("endTimeStampMsg"), // message text
					getResourceMap().getString("endTimeStampTitle"), // messagebox title
					JOptionPane.PLAIN_MESSAGE); // type of dialog
			// now convert the timestamp into something
			// therefore, check whether we have any valid input at all, if we have the
			// correct length (8 chars)
			// and if we have to "." at the right position. A valid input would be e.g.
			// "31.12.08" (dd.mm.yy)
			if (endtime != null && 8 == endtime.length() && endtime.charAt(2) == '.' && endtime.charAt(5) == '.') {
				etime = endtime;
				endtime = endtime.substring(6) + endtime.substring(3, 5) + endtime.substring(0, 2);
				cancelled = false;
				inputIsOk = true;
			} else if (null == endtime || endtime.isEmpty()) {
				cancelled = true;
				inputIsOk = false;
			}
		}
		// check whether dialog was cancelled or not
		if (!inputIsOk && cancelled)
			return;
		// If dialog window isn't already created, do this now.
		if (taskDlg == null) {
			// get parent und init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SEARCH, data, searchRequests,
					settings.getSynonyms(), whichsearch, new String[] { stime, etime }, null, -1, Constants.LOG_OR,
					true, true, true, /* accentInsensitive= */false, false, true, starttime, endtime, whichstamp, false,
					settings.getSearchRemovesFormatTags());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// check whether we have any search results at all
		if (searchRequests.getCurrentSearchResults() != null) {
			// If dialog window isn't already created, do this now.
			if (null == searchResultsDlg)
				searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
						settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
			// show search window
			ZettelkastenApp.getApplication().show(searchResultsDlg);
			// show latest search results by auto-selecting the last item in the combo-box
			searchResultsDlg.showLatestSearchResult();
			// enable window-menu-item, if we have loaded search results
			setSearchResultsAvailable(searchRequests.getCount() > 0);
		}
	}

	/**
	 * This method show the panel with the live-search text box, where the user can
	 * find text in the current entry live by typing.
	 */
	@Action(enabledProperty = "entriesAvailable")
	public void findLive() {
		// show panel with live-search-textbox
		jPanelLiveSearch.setVisible(true);
		// set focus to textbox
		jTextFieldLiveSearch.requestFocusInWindow();
		// indicate that live search is active
		isLiveSearchActive = true;
	}

	/**
	 * Cancels the live-search by simply hiding the panel
	 */
	@Action
	public void findLiveCancel() {
		// hide live-search-bar
		jPanelLiveSearch.setVisible(false);
		// reset highlight terms
		HtmlUbbUtil.setHighlighTerms(null, HtmlUbbUtil.HIGHLIGHT_STYLE_LIVESEARCH, false);
		// indicate that live search is over
		isLiveSearchActive = false;
		updateDisplay();
	}

	/**
	 * Opens the search dialog. This method can deal several search requests.<br>
	 * <br>
	 * For instance, we can have a usual search where the user wants to have search
	 * results displayed in a new frame (SearchResultsFrame). This action is
	 * typically performed, when the user starts a search-request from the
	 * find-dialog (CSearchDlg) (see {@link #find() find()}) or when the user
	 * double-clicks on selected entries in the jTableAuthors or jTableKeywords for
	 * example. <br>
	 * <br>
	 * On the other hand, the search-algorithm is also used to retrieve entries for
	 * adding them to the desktop, as manual links or as follower-numbers. In this
	 * case, the user chose to add - for instance - all entries that match a certain
	 * keyword as manual links to the current entry (see
	 * {@link #addManLinksFromKeywords(int) addManlinks}) or if the user wants to
	 * add all entries from a certain author to the desktop (see
	 * {@link #addDesktopFromAuthors(int) addDesktopFromAuthors}). In this case, no
	 * searchresults-dialog (SearchResultsFrame) is shown - instead, the
	 * "searchresults" are those entries that have to be added as manual links or to
	 * the desktop. <br>
	 * <br>
	 * The relevant parameters for indicating this are {@code displayonly} and
	 * {@code searchtype}, see below. <br>
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
	 * @param searchterms     string-array with search terms
	 * @param where           the type of search, i.e. where to look, e.g. searching
	 *                        for keywords, authors, text etc.
	 * @param logical
	 * @param wholeword       whether we look for whole words or also parts of a
	 *                        word/phrase
	 * @param matchcase       whether the search should be case sensitive or not
	 * @param syno
	 * @param regex
	 * @param timesearch      whether the user requested a time-search, i.e. a
	 *                        search for entries that were created or changed within
	 *                        a certain period
	 * @param datefrom        the start of the period, when a timesearch is
	 *                        requested. format: "yymmdd".
	 * @param dateto          the end of the period, when a timesearch is requested.
	 *                        format: "yymmdd".
	 * @param timestampindex
	 * @param displayonly     true, if the search results are only needed for adding
	 *                        them to the entry as manual links, luhmann-numbers or
	 *                        to the desktop. use false, if the search results
	 *                        should be displayed in the CSearchResults-dialog
	 * @param startsearchtype if we just use the search to retrieve entries that
	 *                        should be added as luhmann-numbers, as manual links or
	 *                        to the desktop, we can indicate this with this
	 *                        parameter. Also a usual search request is passed with
	 *                        this variable. use <i>CConstants.STARTSEARCH_xxx</i>
	 *                        for the appropriate type.
	 * @param searchtype
	 */
	public void startSearch(String[] searchterms, int where, int logical, boolean wholeword, boolean matchcase,
			boolean syno, boolean accentInsensitive, boolean regex, boolean timesearch, String datefrom, String dateto,
			int timestampindex, boolean displayonly, int startsearchtype, int searchtype) {
		if ((searchterms == null) || (searchterms.length == 0)) {
			// Invalid search terms. Nothing to search for.
			return;
		}
		// in case we search for adding desktop-entries, we have to modify the search
		// type here.
		if (taskDlg == null) {
			// get parent and init window
			taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_SEARCH, data, searchRequests,
					settings.getSynonyms(), searchtype, searchterms, null, where, logical, wholeword, matchcase, syno,
					accentInsensitive, regex, timesearch, datefrom, dateto, timestampindex, displayonly,
					settings.getSearchRemovesFormatTags());
			// Center new dialog window.
			taskDlg.setLocationRelativeTo(getFrame());
		}
		waitForTaskDialog();

		// check whether we have any search results at all
		if (searchRequests.getCurrentSearchResults() != null) {
			// check the search type, whether a usual search is requested or if we want to
			// retrieve entries from
			// keywords or author-table.
			switch (startsearchtype) {
			// this is a usual search, that means the search results are added
			// to the CSearchResults-dialog-frame and displayed
			case Constants.STARTSEARCH_USUAL:
				// If dialog window isn't already created, do this now.
				if (null == searchResultsDlg)
					searchResultsDlg = new SearchResultsFrame(this, data, searchRequests, desktop, settings,
							settings.getAcceleratorKeys(), settings.getSynonyms(), bibtex);
				// show search window
				ZettelkastenApp.getApplication().show(searchResultsDlg);
				// show latest search results by auto-selecting the last item in the combo-box
				searchResultsDlg.showLatestSearchResult();
				// enable window-menu-item, if we have loaded search results
				setSearchResultsAvailable(searchRequests.getCount() > 0);
				break;
			// this is a luhmann-search, that means the search results are *not* shown in
			// the searchresults-window. instead, they are added as follower-numbers to
			// the current entry.
			case Constants.STARTSEARCH_LUHMANN:
				// If dialog window isn't already created, do this now.
				if (taskDlg == null) {
					// get parent und init window
					taskDlg = new TaskProgressDialog(getFrame(), TaskProgressDialog.TASK_ENTRIESTOLUHMANN, data,
							searchRequests.getCurrentSearchResults(), data.getActivatedEntryNumber());
					// Center new dialog window.
					taskDlg.setLocationRelativeTo(getFrame());
				}
				waitForTaskDialog();

				break;
			// this is a desktop-search, that means the search results are *not* shown in
			// the searchresults-window. instead, they are added to the desktop
			case Constants.STARTSEARCH_DESKTOP:
				// if desktop window isn't already created, do this now
				if (null == desktopDlg)
					desktopDlg = new DesktopFrame(this, taskinfo, data, bookmarks, desktop, settings,
							settings.getAcceleratorKeys(), bibtex, settings.getAutoKorrektur(),
							settings.getStenoData());
				// show desktop window
				ZettelkastenApp.getApplication().show(desktopDlg);
				// add found entries to desktop
				desktopDlg.addEntries(searchRequests.getCurrentSearchResults());
				// enable window-menu-item, if we have loaded desktop data
				setDesktopAvailable(desktop.getCount() > 0);
				break;
			// this is a manlink-search, that means the search results are *not* shown in
			// the searchresults-window. instead, they are added as manual links to
			// the current entry.
			case Constants.STARTSEARCH_MANLINK:
				addToManLinks(searchRequests.getCurrentSearchResults());
				break;
			}
		} else {
			// display error message box that nothing was found
			JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("errNothingFoundMsg"),
					getResourceMap().getString("errNothingFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * This method gets all selected elements of the jListEntryKeywords and returns
	 * them in an array.
	 *
	 * @return a string-array containing all selected entries, or null if no
	 *         selection made
	 */
	private String[] retrieveSelectedKeywordsFromList() {
		// get selected values
		List<String> values = jListEntryKeywords.getSelectedValuesList();
		// if we have any selections, go on
		if (!values.isEmpty()) {
			// create string array for selected values
			String[] keywords = values.toArray(new String[values.size()]);
			// return complete array
			return keywords;
		}
		// ...or null, if error occured.
		return null;
	}

	/**
	 * Depending on the selected tabbed pane, this method retrieves the wither
	 * selected authors from the jTableAuthors or the selected keywords from the
	 * jTableKeywords and starts a search for those entries, that contain <i>all</i>
	 * selected entries.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void searchLogAnd() {
		// open the search dialog
		// the parameters are as following (see comments below):
		// however, in case of keyword- and author-search, we can neglect some
		// parameters, since keyword- and author-search simply functions by searching
		// for the index-numbers, that are e.g. always - or never - case sensitive
		// relevant
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with
																								// search terms
					Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
					Constants.LOG_AND, // the logical combination
					true, // whole-word-search
					true, // match-case-search
					settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
					settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																		// expressions or not
					false, // time-period search
					"", // timestamp, date from (period start)
					"", // timestamp, date to (period end)
					0, // timestampindex (whether the period should focus on creation or edited date,
						// or both)
					false, // no display - whether the results should only be used for adding entries to
							// the desktop or so (true), or if a searchresults-window shoud be opened
							// (false)
					Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without
													// remarks or keywords and so on - see related method
													// findEntryWithout
					Constants.SEARCH_USUAL);
			break;
		case TAB_KEYWORDS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0),
					Constants.SEARCH_KEYWORDS, Constants.LOG_AND, true, true, settings.getSearchAlwaysSynonyms(),
					settings.getSearchAlwaysAccentInsensitive(), false, false, "", "", 0, false,
					Constants.STARTSEARCH_USUAL, Constants.SEARCH_USUAL);
			break;
		}
	}

	/**
	 * Depending on the selected tabbed pane, this method retrieves the wither
	 * selected authors from the jTableAuthors or the selected keywords from the
	 * jTableKeywords and starts a search for those entries, that contain <i>all</i>
	 * selected entries.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void searchLogOr() {
		// open the search dialog
		// the parameters are as following:
		// however, in case of keyword- and author-search, we can neglect the last
		// parameter, since keyword- and author-search simply functions by searching
		// for the index-numbers, that are always - or never - case sensitive relevant
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with
																								// search terms
					Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
					Constants.LOG_OR, // the logical combination
					true, // whole-word-search
					true, // match-case-search
					settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
					settings.getSearchAlwaysAccentInsensitive(), false, // time-period search
					false, // whether the search terms contain regular expressions or not
					"", // timestamp, date from (period start)
					"", // timestamp, date to (period end)
					0, // timestampindex (whether the period should focus on creation or edited date,
						// or both)
					false, // no display - whether the results should only be used for adding entries to
							// the desktop or so (true), or if a searchresults-window shoud be opened
							// (false)
					Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without
													// remarks or keywords and so on - see related method
													// findEntryWithout
					Constants.SEARCH_USUAL);
			break;
		case TAB_KEYWORDS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0),
					Constants.SEARCH_KEYWORDS, Constants.LOG_OR, true, true, settings.getSearchAlwaysSynonyms(),
					settings.getSearchAlwaysAccentInsensitive(), false, false, "", "", 0, false,
					Constants.STARTSEARCH_USUAL, Constants.SEARCH_USUAL);
			break;
		}
	}

	/**
	 * Depending on the selected tabbed pane, this method retrieves the wither
	 * selected authors from the jTableAuthors or the selected keywords from the
	 * jTableKeywords and starts a search for those entries, that contain <i>all</i>
	 * selected entries.
	 */
	@Action(enabledProperty = "tableEntriesSelected")
	public void searchLogNot() {
		// open the search dialog
		// the parameters are as following:
		// however, in case of keyword- and author-search, we can neglect the last
		// parameter, since keyword- and author-search simply functions by searching
		// for the index-numbers, that are always - or never - case sensitive relevant
		switch (jTabbedPaneMain.getSelectedIndex()) {
		case TAB_AUTHORS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableAuthors, 0), // string-array with
																								// search terms
					Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
					Constants.LOG_NOT, // the logical combination
					true, // whole-word-search
					true, // match-case-search
					settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
					settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																		// expressions or not
					false, // time-period search
					"", // timestamp, date from (period start)
					"", // timestamp, date to (period end)
					0, // timestampindex (whether the period should focus on creation or edited date,
						// or both)
					false, // no display - whether the results should only be used for adding entries to
							// the desktop or so (true), or if a searchresults-window shoud be opened
							// (false)
					Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without
													// remarks or keywords and so on - see related method
													// findEntryWithout
					Constants.SEARCH_USUAL);
			break;
		case TAB_KEYWORDS:
			startSearch(ZettelkastenViewUtil.retrieveSelectedValuesFromTable(jTableKeywords, 0),
					Constants.SEARCH_KEYWORDS, Constants.LOG_NOT, true, true, settings.getSearchAlwaysSynonyms(),
					settings.getSearchAlwaysAccentInsensitive(), false, false, "", "", 0, false,
					Constants.STARTSEARCH_USUAL, Constants.SEARCH_USUAL);
			break;
		}
	}

	/**
	 * Retrieves the selected keywords from the jListEntryKeywords and starts a
	 * search for those entries, that contain <i>all</i> selected entries.
	 */
	@Action(enabledProperty = "listFilledWithEntry")
	public void searchKeywordsFromListLogAnd() {
		// open the search dialog
		// the parameters are as following:
		startSearch(retrieveSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_AND, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
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
	 * Retrieves the selected keywords from the jListEntryKeywords and starts a
	 * search for those entries, that contain <i>none</i> of the selected entries.
	 */
	@Action(enabledProperty = "listFilledWithEntry")
	public void searchKeywordsFromListLogNot() {
		// open the search dialog
		// the parameters are as following:
		startSearch(retrieveSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_NOT, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
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
	 * Retrieves the selected keywords from the jTableKeywords and starts a search
	 * for those entries, that contain <i>at least one</i> of the selected entries.
	 */
	@Action(enabledProperty = "listFilledWithEntry")
	public void searchKeywordsFromListLogOr() {
		// open the search dialog
		// the parameters are as following:
		startSearch(retrieveSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_OR, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				settings.getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				settings.getSearchAlwaysAccentInsensitive(), false, // whether the search terms contain regular
																	// expressions or not
				false, // time-period search
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
	 * Here we place the typical steps we have to do after a file was opened or
	 * imported. typical tasks are for instance setting certain state-variables to
	 * false or true, showing the first entry etc...
	 */
	private void updateDisplayAfterOpen() {
		// this is the typical stuff we need to do when a file is opened
		// or imported. first of all, all the views of the tabbed pane are not
		// uptodate, because we have new data. thus, we set all values to false,
		// indicating that all lists ar <b>not</b> up to date and all tables need
		// to be re-filled.
		data.setKeywordlistUpToDate(false);
		data.setAuthorlistUpToDate(false);
		data.setTitlelistUpToDate(false);
		data.setClusterlistUpToDate(false);
		data.setAttachmentlistUpToDate(false);
		// dispose and clear the window, if we have a created instance of it
		// (we need to do this e.g. if we have opened data, and load a new data-file)
		if (searchResultsDlg != null) {
			searchResultsDlg.dispose();
			searchResultsDlg = null;
		}
		// dispose and clear the window, if we have a created instance of it
		// (we need to do this e.g. if we have opened data, and load a new data-file)
		if (desktopDlg != null) {
			desktopDlg.dispose();
			desktopDlg = null;
		}
		// enable window-menu-item, if we have loaded search results
		setSearchResultsAvailable(searchRequests.getCount() > 0);
		// enable window-menu-item, if we have loaded desktop data
		setDesktopAvailable(desktop.getCount() > 0);
		// reset all necessary variables and clear all tables
		initVariables();
		updateDisplay();
	}

	/**
	 * Changes the text in the application's titlebar, by adding the filename of the
	 * currently opened file to it.
	 */
	private void updateFrameTitle() {
		String filename = settings.getMainDataFileNameWithoutExtension();
		if (filename == null) {
			// Defaults to the application's title.
			getFrame().setTitle(getResourceMap().getString("Application.title"));
			return;
		}
		// Set file-name and application's title to the title-bar.
		getFrame().setTitle("[" + filename + "] - " + getResourceMap().getString("Application.title"));
	}

	/**
	 * This method filters the entries in the jTableLinks depending on the selected
	 * keyword(s) of the jListKeywords.<br>
	 * <br>
	 * This method is called when the user selects an entry from the list
	 * jListKeywords. We then want to filter the list with links/references to other
	 * entries (jTableLinks) to show only those links (entries) that are related to
	 * the selected keywords.<br>
	 * <br>
	 * Futhermore, we want to offer logical-and and logical-or combination of the
	 * keywords, i.e. showing either entries that contain ALL selected keywords or
	 * AT LEAST ONE of the selected keywords.
	 */
	private synchronized void filterLinks() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// if the link-table is not shown, leave
		if (jTabbedPaneMain.getSelectedIndex() != TAB_LINKS) {
			return;
		}
		// if no selections made, or all values de-selected, do nothing.
		// and show all links instead
		if (jListEntryKeywords.getSelectedIndices().length < 1) {
			updateLinksTab();
			return;
		}
		// when thread is already running, do nothing...
		if (createFilterLinksIsRunning) {
			return;
		}
		// clear table
		DefaultTableModel tm = (DefaultTableModel) jTableLinks.getModel();
		// reset the table
		tm.setRowCount(0);
		// tell user that we are doing something...
		statusMsgLabel.setText(getResourceMap().getString("createLinksMsg"));
		prepareAndStartTask(createFilterLinks());
	}

	/**
	 * saveSettings saves the user and program settings. On error, it show and
	 * flashes error icon.
	 */
	private boolean saveSettingsOnExit() {
		// Save the activated entry number as the startup entry.
		settings.setStartupEntry(data.getActivatedEntryNumber());

		// Save table sorting.
		// Get table from search results window.
		JTable sft = null;
		if (searchResultsDlg != null) {
			sft = searchResultsDlg.getSearchFrameTable();
		}
		settings.setTableSorting(new JTable[] { jTableLinks, jTableManLinks, jTableKeywords, jTableAuthors,
				jTableTitles, jTableBookmarks, jTableAttachments, sft });

		if (!settings.saveSettingsToFiles()) {
			showErrorIcon();
			return false;
		}
		return true;
	}

	/**
	 * This method is called when a user selects an entry from the jTableLinks.
	 * Whenever a "linked" entry is selected in this table, we want to<br>
	 * 1) display that entry and<br>
	 * 2) highlight the keywords in common between the activated entry and the
	 * displayed entry.<br>
	 * <br>
	 * The highlighted keywords are shown in the jListEntryKeywords.
	 */
	private void updateDisplayedEntryAndKeywordsListWithSelectedEntryFromLinksInteraction() {
		// if no data available, do nothing.
		if (data.getCount(Daten.ZKNCOUNT) == 0) {
			return;
		}
		// No update to linked list needed.
		needsLinkUpdate = false;

		int selectedEntry = ZettelkastenViewUtil.retrieveSelectedEntryFromTable(data, jTableLinks, 0);
		if (selectedEntry == -1) {
			// If we don't have a valid selection, do nothing.
			return;
		}

		// Update displayed entry. This updates the keywordListModel before we highlight
		// the common keywords below.
		setNewDisplayedEntryAndUpdateDisplay(selectedEntry);

		// Select intersection between selected entry's keyword and activated entry's
		// keywords.
		// Get keywords of the selected entry.
		String[] selectedEntryKeywords = data.getKeywords(selectedEntry);
		// Get keywords of activated entry.
		String[] activatedEntryKeywords = data.getKeywordsOfActivatedEntry();

		Set<String> activatedEntryKeywordsSet = new HashSet<String>(Arrays.asList(activatedEntryKeywords));
		Set<String> selectedEntryKeywordsSet = new HashSet<String>(Arrays.asList(selectedEntryKeywords));
		selectedEntryKeywordsSet.retainAll(activatedEntryKeywordsSet);
		Set<String> intersection = selectedEntryKeywordsSet;

		List<Integer> indexesToSelect = new ArrayList<Integer>();
		for (int keywordIndex = 0; keywordIndex < keywordListModel.getSize(); ++keywordIndex) {
			if (intersection.contains(keywordListModel.elementAt(keywordIndex))) {
				indexesToSelect.add(keywordIndex);
			}
		}
		int[] indexesToSelectArray = indexesToSelect.stream().mapToInt(i -> i).toArray();
		jListEntryKeywords.setSelectedIndices(indexesToSelectArray);
	}

	public void setBackupNecessary() {
		backupNecessary(bibtex.isModified() | settings.getSynonyms().isModified() | data.isMetaModified()
				| data.isModified() | searchRequests.isModified() | bookmarks.isModified() | desktop.isModified());
		// update mainframe's toolbar and enable save-function
		if (isBackupNecessary()) {
			setSaveEnabled(true);
		}
	}

	public void resetBackupNecessary() {
		backupNecessary(false);
	}

	/**
	 * This method checks whether there are unsaved changes in the data-files
	 * (maindata, bookmarks, searchrequests, desktop-data...) and prepares a msg to
	 * save these changes. Usually, this method is called when there are
	 * modifications in one of the above mentioned datafiles, and a new data-file is
	 * to be imported or opened, or when the application is about to quit.
	 *
	 * @param title the title of the message box, e.g. if the changes should be
	 *              saved because the user wants to quit the application of to open
	 *              another data file
	 * @return <i>true</i> if the changes have been successfully saved or if the
	 *         user did not want to save anything, and the program can go on.
	 *         <i>false</i> if the user cancelled the dialog and the program should
	 *         <i>not</i> go on or not quit.
	 */
	private boolean askUserAndMaybeSaveChanges(String title) {
		boolean anyChange = bibtex.isModified() | settings.getSynonyms().isModified() | data.isMetaModified()
				| data.isModified() | searchRequests.isModified() | bookmarks.isModified() | desktop.isModified();
		if (!anyChange) {
			// Nothing to save, return true.
			Constants.zknlogger.log(Level.INFO, "Nothing to be saved in the main data file [{0}].",
					settings.getMainDataFile() != null ? settings.getMainDataFile().toString() : "null");
			return true;
		}
		// We have changes. We need to ask the user if they want to save the latest
		// changes.

		// Prepare message about what needs to be saved to show to the user.
		StringBuilder confirmText = new StringBuilder("");
		if (data.isMetaModified()) {
			confirmText.append(getResourceMap().getString("msgSaveMeta"));
		}
		if (data.isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveData"));
		}
		if (searchRequests.isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveSearches"));
		}
		if (bookmarks.isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveBookmarks"));
		}
		if (desktop.isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveDesktop"));
		}
		if (settings.getSynonyms().isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveSynonyms"));
		}
		if (bibtex.isModified()) {
			confirmText.append(getResourceMap().getString("msgSaveBibTex"));
		}

		// Ask user if changes should be saved.
		int option = JOptionPane.showConfirmDialog(getFrame(),
				getResourceMap().getString("msgSaveChanges", confirmText.toString()), title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		// If no save is requested, return true.
		if (option == JOptionPane.NO_OPTION) {
			return true;
		}
		// If dialog was cancelled, return false.
		if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
			return false;
		}

		// If save is requested, perform save.
		return saveDocument();
	}

	/**
	 * Exit-Listener is responsible for what have to be done closing the window and
	 * exiting the program.
	 */
	private class ConfirmExit implements Application.ExitListener {
		@Override
		public boolean canExit(EventObject e) {
			Constants.zknlogger.log(Level.INFO, "Program exit started.");

			// If we still have an active edit window, tell the user to finish editing
			// first.
			if (isEditModeActive) {
				JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("cannotExitActiveEditMsg"),
						getResourceMap().getString("cannotExitActiveEditTitle"), JOptionPane.PLAIN_MESSAGE);
				// Bring edit window to front.
				if (editEntryDlg != null) {
					editEntryDlg.toFront();
				}
				// Cancel the exit.
				return false;
			}

			// If we have autobackup running, tell the user to wait for it to finish.
			// TODO Instead of asking the user to try later, wait for autobackup to finish
			// and continue.
			if (isAutoBackupRunning()) {
				JOptionPane.showMessageDialog(getFrame(), getResourceMap().getString("cannotExitAutobackMsg"),
						getResourceMap().getString("cannotExitAutobackTitle"), JOptionPane.PLAIN_MESSAGE);
				// Cancel the exit.
				return false;
			}

			if (!askUserAndMaybeSaveChanges(getResourceMap().getString("msgSaveChangesOnExitTitle"))) {
				// Cancel the exit if the save failed.
				return false;
			}

			if (!saveSettingsOnExit()) {
				// Cancel the exit if the save failed.
				return false;
			}
			return true;
		}

		@Override
		public void willExit(EventObject e) {
			// Kill all timers (auto-save, memory-logging...).
			terminateTimers();

			makeExtraBackup();
		}
	}

	/**
	 * This is an application listener that is initialised when running the program
	 * on mac os x. by using this appListener, we can use the typical apple-menu bar
	 * which provides own about, preferences and quit-menu-items.
	 */
	private void setupMacOSXApplicationListener() {
		// <editor-fold defaultstate="collapsed" desc="Application-listener initiating
		// the stuff for the Apple-menu.">
		try {
			// get mac os-x application class
			Class<?> appc = Class.forName("com.apple.eawt.Application");
			// create a new instance for it.
			Object app = appc.newInstance();
			// get the application-listener class. here we can set our action to the apple
			// menu
			Class<?> lc = Class.forName("com.apple.eawt.ApplicationListener");
			Object listener = Proxy.newProxyInstance(lc.getClassLoader(), new Class<?>[] { lc },
					new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) {
							if (method.getName().equals("handleQuit")) {
								// call the general exit-handler from the desktop-application-api
								// here we do all the stuff we need when exiting the application
								ZettelkastenApp.getApplication().exit();
							}
							if (method.getName().equals("handlePreferences")) {
								// show settings window
								settingsWindow();
							}
							if (method.getName().equals("handleAbout")) {
								// show own aboutbox
								showAboutBox();
								try {
									// set handled to true, so other actions won't take place any more.
									// if we leave this out, a second, system-own aboutbox would be displayed
									setHandled(args[0], Boolean.TRUE);
								} catch (NoSuchMethodException | IllegalAccessException
										| InvocationTargetException ex) {
									Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
								}
							}
							return null;
						}

						private void setHandled(Object event, Boolean val)
								throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
							Method handleMethod = event.getClass().getMethod("setHandled",
									new Class<?>[] { boolean.class });
							handleMethod.invoke(event, new Object[] { val });
						}
					});
			// tell about success
			Constants.zknlogger.log(Level.INFO, "Apple Class Loader successfully initiated.");
			try {
				// add application listener that listens to actions on the apple menu items
				Method m = appc.getMethod("addApplicationListener", lc);
				m.invoke(app, listener);
				// register that we want that Preferences menu. by default, only the about box
				// is shown
				// but no pref-menu-item
				Method enablePreferenceMethod = appc.getMethod("setEnabledPreferencesMenu",
						new Class<?>[] { boolean.class });
				enablePreferenceMethod.invoke(app, new Object[] { Boolean.TRUE });
				// tell about success
				Constants.zknlogger.log(Level.INFO, "Apple Preference Menu successfully initiated.");
			} catch (NoSuchMethodException | SecurityException | InvocationTargetException ex) {
				Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
			}
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
		}
		// </editor-fold>

		// </editor-fold>
	}

	/**
	 * This method applies some graphical stuff so the appearance of the program is
	 * even more mac-like...
	 */
	private void setupMacOSXLeopardStyle() {
		// now we have to change back the background-color of all components in the
		// mainpart of the
		// frame, since the brush-metal-look applies to all components
		//
		// other components become normal gray - which is, however, a little bit
		// darker than the default gray
		//
		// snow-leopard and java 6/7 have different color-rendering, we need a different
		// background-color for OS X 10.5 and above as well as java 6 and 7

		// make searchfields look like mac
		setSearchTextFieldMacStyle();
		// remove custim borders
		jScrollPane2.setBorder(null);
		jScrollPane5.setBorder(null);
		jScrollPane16.setBorder(null);
		// make refresh buttons look like mac
		// get the action map
		ActionMap actionMap = Application
				.getInstance(ZettelkastenApp.class).getContext()
				.getActionMap(ZettelkastenView.class, this);
		// init variables
		AbstractAction ac;
		// get the toolbar-action
		ac = (AbstractAction) actionMap.get("findLiveCancel");
		// and change the large-icon-property, which is applied to the toolbar-icons,
		// to the new icon
		ac.putValue(AbstractAction.LARGE_ICON_KEY,
				new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSStopProgressFreestandingTemplate")
						.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		ac.putValue(AbstractAction.SMALL_ICON,
				new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSStopProgressFreestandingTemplate")
						.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
	}

	private void setupSeaGlassStyle() {
		setSearchTextFieldMacStyle();
		jEditorPaneClusterEntries.setBackground(Color.white);
		jEditorPaneIsFollower.setBackground(Color.white);
	}

	private void setSearchTextFieldMacStyle() {
		jTextFieldLiveSearch.putClientProperty("JTextField.variant", "search");
		jTextFieldFilterKeywords.putClientProperty("JTextField.variant", "search");
		jTextFieldFilterAuthors.putClientProperty("JTextField.variant", "search");
		jTextFieldFilterTitles.putClientProperty("JTextField.variant", "search");
		jTextFieldFilterCluster.putClientProperty("JTextField.variant", "search");
		jTextFieldFilterAttachments.putClientProperty("JTextField.variant", "search");
	}

	private void makeSeaGlassToolbar() {
		Tools.makeTexturedToolBarButton(tb_newEntry, Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(tb_open, Tools.SEGMENT_POSITION_MIDDLE);
		Tools.makeTexturedToolBarButton(tb_save, Tools.SEGMENT_POSITION_LAST);

		if (settings.getShowAllIcons()) {
			Tools.makeTexturedToolBarButton(tb_edit, Tools.SEGMENT_POSITION_FIRST);
			Tools.makeTexturedToolBarButton(tb_delete, Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(tb_paste, Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(tb_selectall, Tools.SEGMENT_POSITION_LAST);
		} else {
			Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_FIRST);
			Tools.makeTexturedToolBarButton(tb_paste, Tools.SEGMENT_POSITION_LAST);
		}

		Tools.makeTexturedToolBarButton(tb_addmanlinks, Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(tb_addluhmann, Tools.SEGMENT_POSITION_MIDDLE);

		if (settings.getShowAllIcons()) {
			Tools.makeTexturedToolBarButton(tb_addbookmark, Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(tb_addtodesktop, Tools.SEGMENT_POSITION_LAST);
		} else {
			Tools.makeTexturedToolBarButton(tb_addbookmark, Tools.SEGMENT_POSITION_LAST);
		}

		if (settings.getShowAllIcons()) {
			Tools.makeTexturedToolBarButton(tb_find, Tools.SEGMENT_POSITION_FIRST);
			Tools.makeTexturedToolBarButton(tb_first, Tools.SEGMENT_POSITION_MIDDLE);
		} else {
			Tools.makeTexturedToolBarButton(tb_first, Tools.SEGMENT_POSITION_FIRST);
		}

		Tools.makeTexturedToolBarButton(tb_prev, Tools.SEGMENT_POSITION_MIDDLE);
		Tools.makeTexturedToolBarButton(tb_next, Tools.SEGMENT_POSITION_MIDDLE);
		Tools.makeTexturedToolBarButton(tb_last, Tools.SEGMENT_POSITION_LAST);

		toolBar.setPreferredSize(new Dimension(toolBar.getSize().width, Constants.seaGlassToolbarHeight));
		toolBar.add(new JToolBar.Separator(), 0);
	}

	/**
	 *
	 * @param bottomBarNeedsUpdate if {@code true}, the bottom bar on mac aqua style
	 *                             will also be re-initialized. Use {@code true}
	 *                             only the first time the bottom bar is
	 *                             initialized. For further GUI-updates, e.g. from
	 *                             settings window, use {@code false} as parameter.
	 */
	private void makeMacToolbar() {
		// hide default toolbar
		toolBar.setVisible(false);
		getFrame().remove(toolBar);
		// and create mac toolbar
		if (settings.getShowIcons() || settings.getShowIconText()) {

			UnifiedToolBar mactoolbar = new UnifiedToolBar();

			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_newEntry, MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_open, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_save, MacToolbarButton.SEGMENT_POSITION_LAST));
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			if (settings.getShowAllIcons()) {
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_edit, MacToolbarButton.SEGMENT_POSITION_FIRST));
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_delete,
						MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_copy, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_paste, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_selectall,
						MacToolbarButton.SEGMENT_POSITION_LAST));
			} else {
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_copy, MacToolbarButton.SEGMENT_POSITION_FIRST));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_paste, MacToolbarButton.SEGMENT_POSITION_LAST));
			}
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_addmanlinks,
					MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_addluhmann,
					MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			if (settings.getShowAllIcons()) {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_addbookmark,
						MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_addtodesktop,
						MacToolbarButton.SEGMENT_POSITION_LAST));
			} else {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_addbookmark,
						MacToolbarButton.SEGMENT_POSITION_LAST));
			}
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			if (settings.getShowAllIcons()) {
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_find, MacToolbarButton.SEGMENT_POSITION_FIRST));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_first, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			} else {
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_first, MacToolbarButton.SEGMENT_POSITION_FIRST));
			}
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_prev, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_next, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_last, MacToolbarButton.SEGMENT_POSITION_LAST));

			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(32, 1));
			mactoolbar.addComponentToLeft(tb_searchTextfield);

			mactoolbar.installWindowDraggerOnWindow(ZettelkastenView.super.getFrame());
			mainPanel.add(mactoolbar.getComponent(), BorderLayout.PAGE_START);
		}
		makeMacBottomBar();
	}

	/**
	 */
	private void makeMacBottomBar() {
		jPanel12.setVisible(false);
		BottomBar macbottombar = new BottomBar(BottomBarSize.LARGE);
		// history buttons
		buttonHistoryBack.setBorderPainted(true);
		buttonHistoryFore.setBorderPainted(true);
		buttonHistoryBack.putClientProperty("JButton.buttonType", "segmentedTextured");
		buttonHistoryFore.putClientProperty("JButton.buttonType", "segmentedTextured");
		buttonHistoryBack.putClientProperty("JButton.segmentPosition", "only");
		buttonHistoryFore.putClientProperty("JButton.segmentPosition", "only");
		buttonHistoryBack.putClientProperty("JComponent.sizeVariant", "small");
		buttonHistoryFore.putClientProperty("JComponent.sizeVariant", "small");
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(statusEntryLabel), 5);
		macbottombar.addComponentToLeft(jTextFieldEntryNumber, 5);
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(statusOfEntryLabel), 10);
		macbottombar.addComponentToLeft(buttonHistoryBack);
		macbottombar.addComponentToLeft(buttonHistoryFore, 10);
		macbottombar.addComponentToLeft(MacButtonFactory.makeUnifiedToolBarButton(statusErrorButton), 5);
		macbottombar.addComponentToLeft(MacButtonFactory.makeUnifiedToolBarButton(statusDesktopEntryButton));
		macbottombar.addComponentToCenter(MacWidgetFactory.makeEmphasizedLabel(jLabelMemory));
		macbottombar.addComponentToRight(MacWidgetFactory.makeEmphasizedLabel(statusMsgLabel));
		macbottombar.addComponentToRight(statusAnimationLabel, 4);
		statusPanel.remove(jPanel12);
		statusPanel.setBorder(null);
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(macbottombar.getComponent(), BorderLayout.PAGE_START);
	}

	/**
	 * Selection listener for the tables. Each table that reacts to selections gets
	 * this SelectionListener in the {@link #initSelectionListeners()
	 * initSelectionListeners()} method.
	 */
	private class SelectionListener implements ListSelectionListener {
		JTable table;

		// It is necessary to keep the table since it is not possible
		// to determine the table from the event's source
		SelectionListener(JTable table) {
			this.table = table;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (tableUpdateActive) {
				// Do nothing if there is an ongoing table update.
				return;
			}

			// Get list selection model.
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			// Set value-adjusting to true, so we don't fire multiple value-changed
			// events.
			lsm.setValueIsAdjusting(true);
			if (table == jTableAuthors) {
				showAuthorText();
				if (setBibKeyDlg != null) {
					setBibKeyDlg.initTitleAndBibkey();
				}
			} else if (table == jTableTitles) {
				showEntryFromTitles();
			} else if (table == jTableAttachments) {
				showEntryFromAttachments();
			} else if (table == jTableLinks) {
				// If the user selects an entry from the links table, highlight the
				// keyword in jListEntryKeywords that is responsible for the link.
				updateDisplayedEntryAndKeywordsListWithSelectedEntryFromLinksInteraction();
			} else if (table == jTableManLinks) {
				updateDisplayedEntryWithSelectedEntryFromManualLinks();
			} else if (table == jTableBookmarks) {
				showEntryFromBookmarks();
			}
		}
	}

	/**
	 * This variable indicates whether we have any entries, so we can en- or disable
	 * the all relevant actions that need at least one entry to be enabled.
	 */
	private boolean entriesAvailable = false;

	public boolean isEntriesAvailable() {
		return entriesAvailable;
	}

	public void setEntriesAvailable(boolean b) {
		boolean old = isEntriesAvailable();
		this.entriesAvailable = b;
		firePropertyChange("entriesAvailable", old, isEntriesAvailable());
	}

	/**
	 * This variable indicates whether we have more than one entry, so we can en- or
	 * disable the all relevant actions that need at least two entries to be
	 * enabled.
	 */
	private boolean moreEntriesAvailable = false;

	public boolean isMoreEntriesAvailable() {
		return moreEntriesAvailable;
	}

	public void setMoreEntriesAvailable(boolean b) {
		boolean old = isMoreEntriesAvailable();
		this.moreEntriesAvailable = b;
		firePropertyChange("moreEntriesAvailable", old, isMoreEntriesAvailable());
	}

	/**
	 * This variable indicates whether we have entries in the tables, so we can en-
	 * or disable the cut and copy actions and other actions that need at least one
	 * selection.
	 */
	private boolean tableEntriesSelected = false;

	public boolean isTableEntriesSelected() {
		return tableEntriesSelected;
	}

	public void setTableEntriesSelected(boolean b) {
		boolean old = isTableEntriesSelected();
		this.tableEntriesSelected = b;
		firePropertyChange("tableEntriesSelected", old, isTableEntriesSelected());
	}

	/**
	 * This variable indicates whether we have entries in the tables, so we can en-
	 * or disable the cut and copy actions and other actions that need at least one
	 * selection.
	 */
	private boolean exportPossible = false;

	public boolean isExportPossible() {
		return exportPossible;
	}

	public void setExportPossible(boolean b) {
		boolean old = isExportPossible();
		this.exportPossible = b;
		firePropertyChange("exportPossible", old, isExportPossible());
	}

	/**
	 * This variable indicates whether we have search results or not. dependent on
	 * this setting, the related menu-item in the windows-menu is en/disbaled
	 */
	private boolean searchResultsAvailable = false;

	public boolean isSearchResultsAvailable() {
		return searchResultsAvailable;
	}

	public void setSearchResultsAvailable(boolean b) {
		boolean old = isSearchResultsAvailable();
		this.searchResultsAvailable = b;
		firePropertyChange("searchResultsAvailable", old, isSearchResultsAvailable());
	}

	/**
	 * This variable indicates whether we have desktop data or not. dependent on
	 * this setting, the related menu-item in the windows-menu is en/disbaled
	 */
	private boolean desktopAvailable = false;

	public boolean isDesktopAvailable() {
		return desktopAvailable;
	}

	public void setDesktopAvailable(boolean b) {
		boolean old = isDesktopAvailable();
		this.desktopAvailable = b;
		firePropertyChange("DesktopAvailable", old, isDesktopAvailable());
	}

	/**
	 * This variable indicates whether we have entries in the lists, so we can en-
	 * or disable the cut and copy actions and other actions that need at least one
	 * selection.
	 */
	private boolean listFilledWithEntry = false;

	public boolean isListFilledWithEntry() {
		return listFilledWithEntry;
	}

	public void setListFilledWithEntry(boolean b) {
		boolean old = isListFilledWithEntry();
		this.listFilledWithEntry = b;
		firePropertyChange("listFilledWithEntry", old, isListFilledWithEntry());
	}

	/**
	 * This variable indicates whether we have seleced text, so we can en- or
	 * disable the related actions.
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
	 * disable the related actions.
	 */
	private boolean bibtexFileLoaded = false;

	public boolean isBibtexFileLoaded() {
		return bibtexFileLoaded;
	}

	public void setBibtexFileLoaded(boolean b) {
		boolean old = isBibtexFileLoaded();
		this.bibtexFileLoaded = b;
		firePropertyChange("bibtexFileLoaded", old, isBibtexFileLoaded());
	}

	/**
	 * This variable indicates whether the displayed entry is already bookmarked, so
	 * we can en- or disable the bookmark-action.
	 */
	private boolean entryBookmarked = false;

	public boolean isEntryBookmarked() {
		return entryBookmarked;
	}

	public void setEntryBookmarked(boolean b) {
		boolean old = isEntryBookmarked();
		this.entryBookmarked = b;
		firePropertyChange("entryBookmarked", old, isEntryBookmarked());
	}

	/**
	 * This variable indicates whether the a luhmann-number (i.e. entry in the
	 * jTreeLuhmann) is selected or not. so we can en- or disable the
	 * bookmark-action.
	 */
	private boolean luhmannSelected = false;

	public boolean isLuhmannSelected() {
		return luhmannSelected;
	}

	public void setLuhmannSelected(boolean b) {
		boolean old = isLuhmannSelected();
		this.luhmannSelected = b;
		firePropertyChange("luhmannSelected", old, isLuhmannSelected());
	}

	/**
	 * This variable indicates whether an entry has follower or not (i.e. elements
	 * in the jTreeLuhmann)
	 */
	private boolean moreLuhmann = false;

	public boolean isMoreLuhmann() {
		return moreLuhmann;
	}

	public void setMoreLuhmann(boolean b) {
		boolean old = isMoreLuhmann();
		this.moreLuhmann = b;
		firePropertyChange("moreLuhmann", old, isMoreLuhmann());
	}

	/**
	 * This variable indicates whether the a luhmann-number (i.e. entry in the
	 * jTreeLuhmann) is selected or not. so we can en- or disable the
	 * bookmark-action.
	 */
	private boolean saveEnabled = false;

	public boolean isSaveEnabled() {
		return saveEnabled;
	}

	public void setSaveEnabled(boolean b) {
		boolean old = isSaveEnabled();
		this.saveEnabled = b;
		firePropertyChange("saveEnabled", old, isSaveEnabled());
	}

	/**
	 * This variable indicates whether the history-function is available or not.
	 */
	private boolean historyForAvailable = false;

	public boolean isHistoryForAvailable() {
		return historyForAvailable;
	}

	public void setHistoryForAvailable(boolean b) {
		boolean old = isHistoryForAvailable();
		this.historyForAvailable = b;
		firePropertyChange("historyForAvailable", old, isHistoryForAvailable());
	}

	/**
	 * This variable indicates whether the history-function is available or not.
	 */
	private boolean historyBackAvailable = false;

	public boolean isHistoryBackAvailable() {
		return historyBackAvailable;
	}

	public void setHistoryBackAvailable(boolean b) {
		boolean old = isHistoryBackAvailable();
		this.historyBackAvailable = b;
		firePropertyChange("historyBackAvailable", old, isHistoryBackAvailable());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPaneMain1 = new javax.swing.JSplitPane();
        jSplitPaneMain2 = new javax.swing.JSplitPane();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPaneEntry = new javax.swing.JEditorPane();
        jPanelLiveSearch = new javax.swing.JPanel();
        jTextFieldLiveSearch = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListEntryKeywords = MacSourceList.createMacSourceList();
        jPanelMainRight = new javax.swing.JPanel();
        jTabbedPaneMain = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPaneLinks = new javax.swing.JSplitPane();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableLinks = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jPanelManLinks = new javax.swing.JPanel();
        jScrollPane15 = new javax.swing.JScrollPane();
        jTableManLinks = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTreeLuhmann = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPaneIsFollower = new javax.swing.JEditorPane() {
            @Override
            public String getToolTipText(MouseEvent evt) {
                String text = null;
                int pos = viewToModel(evt.getPoint());
                if (pos >= 0) {
                    HTMLDocument hdoc = (HTMLDocument) getDocument();
                    javax.swing.text.Element e = hdoc.getCharacterElement(pos);
                    AttributeSet a = e.getAttributes();

                    SimpleAttributeSet value = (SimpleAttributeSet) a.getAttribute(HTML.Tag.A);
                    if (value != null) {
                        String alt = (String) value.getAttribute(HTML.Attribute.TITLE);
                        if (alt != null) {
                            text = alt;
                        }
                    }
                }
                return text;
            }};
            jCheckBoxShowAllLuhmann = new javax.swing.JCheckBox();
            jPanel2 = new javax.swing.JPanel();
            jTextFieldFilterKeywords = new javax.swing.JTextField();
            jButtonRefreshKeywords = new javax.swing.JButton();
            jCheckBoxShowSynonyms = new javax.swing.JCheckBox();
            jPanel16 = new javax.swing.JPanel();
            jScrollPane17 = new javax.swing.JScrollPane();
            jTreeKeywords = new javax.swing.JTree();
            jScrollPane6 = new javax.swing.JScrollPane();
            jTableKeywords = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
            jPanel7 = new javax.swing.JPanel();
            jTextFieldFilterAuthors = new javax.swing.JTextField();
            jSplitPaneAuthors = new javax.swing.JSplitPane();
            jPanel15 = new javax.swing.JPanel();
            jScrollPane7 = new javax.swing.JScrollPane();
            jTableAuthors = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
            jComboBoxAuthorType = new javax.swing.JComboBox();
            jPanelDispAuthor = new javax.swing.JPanel();
            jScrollPane16 = new javax.swing.JScrollPane();
            jEditorPaneDispAuthor = new javax.swing.JEditorPane();
            jButtonRefreshAuthors = new javax.swing.JButton();
            jPanel8 = new javax.swing.JPanel();
            jScrollPane8 = new javax.swing.JScrollPane();
            jTableTitles = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
            jTextFieldFilterTitles = new javax.swing.JTextField();
            jButtonRefreshTitles = new javax.swing.JButton();
            jPanel11 = new javax.swing.JPanel();
            jTextFieldFilterCluster = new javax.swing.JTextField();
            jButtonRefreshCluster = new javax.swing.JButton();
            jCheckBoxCluster = new javax.swing.JCheckBox();
            jPanel3 = new javax.swing.JPanel();
            jSplitPane1 = new javax.swing.JSplitPane();
            jScrollPane5 = new javax.swing.JScrollPane();
            jEditorPaneClusterEntries = new javax.swing.JEditorPane();
            jScrollPane11 = new javax.swing.JScrollPane();
            jTreeCluster = new javax.swing.JTree();
            jPanel9 = new javax.swing.JPanel();
            jComboBoxBookmarkCategory = new javax.swing.JComboBox();
            jSplitPane3 = new javax.swing.JSplitPane();
            jScrollPane9 = new javax.swing.JScrollPane();
            jTableBookmarks = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
            jScrollPane14 = new javax.swing.JScrollPane();
            jEditorPaneBookmarkComment = new javax.swing.JEditorPane();
            jPanel13 = new javax.swing.JPanel();
            jScrollPane13 = new javax.swing.JScrollPane();
            jTableAttachments = (settings.isMacStyle()) ? MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
            jTextFieldFilterAttachments = new javax.swing.JTextField();
            jButtonRefreshAttachments = new javax.swing.JButton();
            menuBar = new javax.swing.JMenuBar();
            fileMenu = new javax.swing.JMenu();
            newEntryMenuItem = new javax.swing.JMenuItem();
            insertEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator104 = new javax.swing.JSeparator();
            quickNewEntryMenuItem = new javax.swing.JMenuItem();
            quickNewTitleEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator1 = new javax.swing.JSeparator();
            openMenuItem = new javax.swing.JMenuItem();
            recentDocsSubMenu = new javax.swing.JMenu();
            recentDoc1 = new javax.swing.JMenuItem();
            recentDoc2 = new javax.swing.JMenuItem();
            recentDoc3 = new javax.swing.JMenuItem();
            recentDoc4 = new javax.swing.JMenuItem();
            recentDoc5 = new javax.swing.JMenuItem();
            recentDoc6 = new javax.swing.JMenuItem();
            recentDoc7 = new javax.swing.JMenuItem();
            recentDoc8 = new javax.swing.JMenuItem();
            jSeparator107 = new javax.swing.JSeparator();
            saveMenuItem = new javax.swing.JMenuItem();
            saveAsMenuItem = new javax.swing.JMenuItem();
            jSeparator2 = new javax.swing.JSeparator();
            newDesktopMenuItem = new javax.swing.JMenuItem();
            newZettelkastenMenuItem = new javax.swing.JMenuItem();
            jSeparator78 = new javax.swing.JSeparator();
            importMenuItem = new javax.swing.JMenuItem();
            exportMenuItem = new javax.swing.JMenuItem();
            jSeparator77 = new javax.swing.JSeparator();
            menuFileInformation = new javax.swing.JMenuItem();
            jSeparatorExit = new javax.swing.JSeparator();
            exitMenuItem = new javax.swing.JMenuItem();
            editMenu = new javax.swing.JMenu();
            editMenuItem = new javax.swing.JMenuItem();
            jSeparator33 = new javax.swing.JSeparator();
            deleteZettelMenuItem = new javax.swing.JMenuItem();
            jSeparator6 = new javax.swing.JSeparator();
            deleteKwFromListMenuItem = new javax.swing.JMenuItem();
            jSeparator40 = new javax.swing.JSeparator();
            copyMenuItem = new javax.swing.JMenuItem();
            copyPlainMenuItem = new javax.swing.JMenuItem();
            pasteMenuItem = new javax.swing.JMenuItem();
            selectAllMenuItem = new javax.swing.JMenuItem();
            jSeparator99 = new javax.swing.JSeparator();
            addSelectionToKeywordMenuItem = new javax.swing.JMenuItem();
            addFirstLineToTitleMenuItem = new javax.swing.JMenuItem();
            addSelectionToTitleMenuItem = new javax.swing.JMenuItem();
            jSeparator24 = new javax.swing.JSeparator();
            manualInsertLinksMenuItem = new javax.swing.JMenuItem();
            manualInsertMenuItem = new javax.swing.JMenuItem();
            jSeparator41 = new javax.swing.JSeparator();
            setBookmarkMenuItem = new javax.swing.JMenuItem();
            addToDesktopMenuItem = new javax.swing.JMenuItem();
            findMenu = new javax.swing.JMenu();
            findMenuItem = new javax.swing.JMenuItem();
            findReplaceMenuItem = new javax.swing.JMenuItem();
            jSeparator31 = new javax.swing.JSeparator();
            findEntryWithout = new javax.swing.JMenu();
            findEntriesWithoutKeywords = new javax.swing.JMenuItem();
            jSeparator69 = new javax.swing.JSeparator();
            findEntriesWithoutAuthors = new javax.swing.JMenuItem();
            jSeparator75 = new javax.swing.JSeparator();
            findEntriesWithoutRemarks = new javax.swing.JMenuItem();
            findEntriesWithRemarks = new javax.swing.JMenuItem();
            jSeparator106 = new javax.swing.JPopupMenu.Separator();
            findEntriesWithoutManualLinks = new javax.swing.JMenuItem();
            jSeparator65 = new javax.swing.JPopupMenu.Separator();
            findEntriesAnyLuhmann = new javax.swing.JMenuItem();
            findEntriesTopLevelLuhmann = new javax.swing.JMenuItem();
            jSeparator110 = new javax.swing.JSeparator();
            findEntriesWithRatings = new javax.swing.JMenuItem();
            findEntriesWithoutRatings = new javax.swing.JMenuItem();
            jSeparator76 = new javax.swing.JSeparator();
            findEntriesWithAttachments = new javax.swing.JMenuItem();
            jSeparator83 = new javax.swing.JSeparator();
            findEntriesFromCreatedTimestamp = new javax.swing.JMenuItem();
            findEntriesFromEditedTimestamp = new javax.swing.JMenuItem();
            jSeparator95 = new javax.swing.JSeparator();
            findDoubleEntriesItem = new javax.swing.JMenuItem();
            jSeparator68 = new javax.swing.JSeparator();
            findEntryKeywordsMenu = new javax.swing.JMenu();
            menuKwListSearchOr = new javax.swing.JMenuItem();
            jSeparator19 = new javax.swing.JSeparator();
            menuKwListSearchAnd = new javax.swing.JMenuItem();
            jSeparator39 = new javax.swing.JSeparator();
            menuKwListSearchNot = new javax.swing.JMenuItem();
            jSeparator18 = new javax.swing.JSeparator();
            liveSearchMenuItem = new javax.swing.JMenuItem();
            jSeparator22 = new javax.swing.JSeparator();
            homeMenuItem = new javax.swing.JMenuItem();
            prevEntryMenuItem = new javax.swing.JMenuItem();
            nextEntryMenuItem = new javax.swing.JMenuItem();
            lastEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator72 = new javax.swing.JPopupMenu.Separator();
            randomEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator111 = new javax.swing.JPopupMenu.Separator();
            historyForMenuItem = new javax.swing.JMenuItem();
            histroyBackMenuItem = new javax.swing.JMenuItem();
            goToFirstParentEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator112 = new javax.swing.JPopupMenu.Separator();
            gotoEntryMenuItem = new javax.swing.JMenuItem();
            viewMenu = new javax.swing.JMenu();
            showLinksMenuItem = new javax.swing.JMenuItem();
            showLuhmannMenuItem = new javax.swing.JMenuItem();
            showKeywordsMenuItem = new javax.swing.JMenuItem();
            showAuthorsMenuItem = new javax.swing.JMenuItem();
            showTitlesMenuItem = new javax.swing.JMenuItem();
            showClusterMenuItem = new javax.swing.JMenuItem();
            showBookmarksMenuItem = new javax.swing.JMenuItem();
            showAttachmentsMenuItem = new javax.swing.JMenuItem();
            jSeparator23 = new javax.swing.JSeparator();
            showCurrentEntryAgain = new javax.swing.JMenuItem();
            jSeparator55 = new javax.swing.JSeparator();
            showHighlightKeywords = new javax.swing.JCheckBoxMenuItem();
            highlightSegmentsMenuItem = new javax.swing.JCheckBoxMenuItem();
            viewMenuLinks = new javax.swing.JMenu();
            viewMenuLinksKwListRefresh = new javax.swing.JMenuItem();
            jSeparator116 = new javax.swing.JPopupMenu.Separator();
            viewMenuLinksRemoveManLink = new javax.swing.JMenuItem();
            jSeparator3 = new javax.swing.JSeparator();
            viewMenuLinksKwListLogOr = new javax.swing.JCheckBoxMenuItem();
            viewMenuLinksKwListLogAnd = new javax.swing.JCheckBoxMenuItem();
            jSeparator53 = new javax.swing.JSeparator();
            viewMenuLinksManLink = new javax.swing.JMenuItem();
            viewMenuLinksLuhmann = new javax.swing.JMenuItem();
            jSeparator58 = new javax.swing.JSeparator();
            viewMenuLinksDesktop = new javax.swing.JMenuItem();
            jSeparator100 = new javax.swing.JSeparator();
            viewMenuLinksExport = new javax.swing.JMenuItem();
            viewMenuExportToSearch = new javax.swing.JMenuItem();
            viewMenuLuhmann = new javax.swing.JMenu();
            viewMenuLuhmannDelete = new javax.swing.JMenuItem();
            jSeparator61 = new javax.swing.JSeparator();
            viewMenuLuhmannManLinks = new javax.swing.JMenuItem();
            viewMenuLuhmannBookmarks = new javax.swing.JMenuItem();
            jSeparator62 = new javax.swing.JSeparator();
            viewMenuLuhmannDesktop = new javax.swing.JMenuItem();
            jSeparator73 = new javax.swing.JPopupMenu.Separator();
            viewMenuLuhmannShowTopLevel = new javax.swing.JMenuItem();
            jSeparator102 = new javax.swing.JSeparator();
            viewMenuLuhmannExport = new javax.swing.JMenuItem();
            viewMenuLuhmannExportSearch = new javax.swing.JMenuItem();
            jSeparator118 = new javax.swing.JPopupMenu.Separator();
            viewMenuLuhmannShowNumbers = new javax.swing.JCheckBoxMenuItem();
            jSeparator101 = new javax.swing.JPopupMenu.Separator();
            viewMenuLuhmannShowLevel = new javax.swing.JMenu();
            viewMenuLuhmannDepthAll = new javax.swing.JMenuItem();
            jSeparator119 = new javax.swing.JPopupMenu.Separator();
            viewMenuLuhmannDepth1 = new javax.swing.JMenuItem();
            viewMenuLuhmannDepth2 = new javax.swing.JMenuItem();
            viewMenuLuhmannDepth3 = new javax.swing.JMenuItem();
            viewMenuLuhmannDepth4 = new javax.swing.JMenuItem();
            viewMenuLuhmannDepth5 = new javax.swing.JMenuItem();
            viewMenuKeywords = new javax.swing.JMenu();
            viewKeywordsCopy = new javax.swing.JMenuItem();
            jSeparator25 = new javax.swing.JSeparator();
            viewKeywordsSearchOr = new javax.swing.JMenuItem();
            viewKeywordsSearchAnd = new javax.swing.JMenuItem();
            viewKeywordsSearchNot = new javax.swing.JMenuItem();
            jSeparator26 = new javax.swing.JSeparator();
            viewKeywordsNew = new javax.swing.JMenuItem();
            viewKeywordsEdit = new javax.swing.JMenuItem();
            viewKeywordsDelete = new javax.swing.JMenuItem();
            jSeparator27 = new javax.swing.JSeparator();
            viewKeywordsAddToList = new javax.swing.JMenuItem();
            jSeparator47 = new javax.swing.JSeparator();
            viewKeywordsLuhmann = new javax.swing.JMenuItem();
            viewKeywordsLuhmannAnd = new javax.swing.JMenuItem();
            jSeparator67 = new javax.swing.JSeparator();
            viewKeywordsManLinks = new javax.swing.JMenuItem();
            viewKeywordsManLinksAnd = new javax.swing.JMenuItem();
            jSeparator48 = new javax.swing.JSeparator();
            viewKeywordsDesktop = new javax.swing.JMenuItem();
            viewKeywordsDesktopAnd = new javax.swing.JMenuItem();
            jSeparator80 = new javax.swing.JSeparator();
            viewKeywordsExport = new javax.swing.JMenuItem();
            viewMenuAuthors = new javax.swing.JMenu();
            viewAuthorsCopy = new javax.swing.JMenuItem();
            jSeparator28 = new javax.swing.JSeparator();
            viewAuthorsSubFind = new javax.swing.JMenu();
            viewAuthorsSearchOr = new javax.swing.JMenuItem();
            viewAuthorsSearchAnd = new javax.swing.JMenuItem();
            viewAuthorsSearchNot = new javax.swing.JMenuItem();
            jSeparator29 = new javax.swing.JSeparator();
            viewAuthorsSubEdit = new javax.swing.JMenu();
            viewAuthorsNew = new javax.swing.JMenuItem();
            viewAuthorsEdit = new javax.swing.JMenuItem();
            viewAuthorsDelete = new javax.swing.JMenuItem();
            jSeparator90 = new javax.swing.JSeparator();
            viewAuthorsBibkey = new javax.swing.JMenuItem();
            jSeparator30 = new javax.swing.JSeparator();
            viewAuthorsSubAdd = new javax.swing.JMenu();
            viewAuthorsAddToEntry = new javax.swing.JMenuItem();
            jSeparator51 = new javax.swing.JSeparator();
            viewAuthorsManLinks = new javax.swing.JMenuItem();
            viewAuthorsManLinksAnd = new javax.swing.JMenuItem();
            jSeparator71 = new javax.swing.JSeparator();
            viewAuthorsAddLuhmann = new javax.swing.JMenuItem();
            viewAuthorsAddLuhmannAnd = new javax.swing.JMenuItem();
            jSeparator52 = new javax.swing.JSeparator();
            viewAuthorsDesktop = new javax.swing.JMenuItem();
            viewAuthorsDesktopAnd = new javax.swing.JMenuItem();
            jSeparator81 = new javax.swing.JSeparator();
            viewAuthorsImport = new javax.swing.JMenuItem();
            viewAuthorsExport = new javax.swing.JMenuItem();
            jSeparator92 = new javax.swing.JSeparator();
            viewAuthorsAttachBibtexFile = new javax.swing.JMenuItem();
            viewAuthorsRefreshBibtexFile = new javax.swing.JMenuItem();
            viewMenuTitles = new javax.swing.JMenu();
            viewTitlesCopy = new javax.swing.JMenuItem();
            jSeparator43 = new javax.swing.JSeparator();
            viewTitlesEdit = new javax.swing.JMenuItem();
            viewTitlesDelete = new javax.swing.JMenuItem();
            jSeparator105 = new javax.swing.JSeparator();
            viewTitlesAutomaticFirstLine = new javax.swing.JMenuItem();
            jSeparator42 = new javax.swing.JSeparator();
            viewTitlesManLinks = new javax.swing.JMenuItem();
            viewTitlesLuhmann = new javax.swing.JMenuItem();
            viewTitlesBookmarks = new javax.swing.JMenuItem();
            jSeparator113 = new javax.swing.JPopupMenu.Separator();
            viewTitlesDesktop = new javax.swing.JMenuItem();
            jSeparator108 = new javax.swing.JPopupMenu.Separator();
            viewTitlesExport = new javax.swing.JMenuItem();
            viewMenuCluster = new javax.swing.JMenu();
            viewClusterExport = new javax.swing.JMenuItem();
            viewClusterExportToSearch = new javax.swing.JMenuItem();
            viewMenuBookmarks = new javax.swing.JMenu();
            viewBookmarksEdit = new javax.swing.JMenuItem();
            viewBookmarksDelete = new javax.swing.JMenuItem();
            jSeparator35 = new javax.swing.JSeparator();
            viewBookmarksEditCat = new javax.swing.JMenuItem();
            viewBookmarksDeleteCat = new javax.swing.JMenuItem();
            jSeparator37 = new javax.swing.JSeparator();
            viewBookmarksManLink = new javax.swing.JMenuItem();
            viewBookmarksAddLuhmann = new javax.swing.JMenuItem();
            jSeparator59 = new javax.swing.JSeparator();
            viewBookmarkDesktop = new javax.swing.JMenuItem();
            jSeparator82 = new javax.swing.JSeparator();
            viewBookmarksExport = new javax.swing.JMenuItem();
            viewBookmarksExportSearch = new javax.swing.JMenuItem();
            viewMenuAttachments = new javax.swing.JMenu();
            viewAttachmentsCopy = new javax.swing.JMenuItem();
            jSeparator84 = new javax.swing.JSeparator();
            viewAttachmentEdit = new javax.swing.JMenuItem();
            viewAttachmentsDelete = new javax.swing.JMenuItem();
            jSeparator85 = new javax.swing.JSeparator();
            viewMenuAttachmentGoto = new javax.swing.JMenuItem();
            jSeparator93 = new javax.swing.JSeparator();
            viewAttachmentsExport = new javax.swing.JMenuItem();
            windowsMenu = new javax.swing.JMenu();
            showSearchResultsMenuItem = new javax.swing.JMenuItem();
            jSeparator44 = new javax.swing.JSeparator();
            showDesktopMenuItem = new javax.swing.JMenuItem();
            jSeparator109 = new javax.swing.JSeparator();
            showNewEntryMenuItem = new javax.swing.JMenuItem();
            jSeparator34 = new javax.swing.JPopupMenu.Separator();
            showErrorLogMenuItem = new javax.swing.JMenuItem();
            aboutMenu = new javax.swing.JMenu();
            aboutMenuItem = new javax.swing.JMenuItem();
            jSeparatorAbout1 = new javax.swing.JSeparator();
            preferencesMenuItem = new javax.swing.JMenuItem();
            statusPanel = new javax.swing.JPanel();
            jPanel12 = new javax.swing.JPanel();
            statusEntryLabel = new javax.swing.JLabel();
            statusAnimationLabel = new javax.swing.JLabel();
            jTextFieldEntryNumber = new javax.swing.JTextField();
            statusOfEntryLabel = new javax.swing.JLabel();
            buttonHistoryBack = new javax.swing.JButton();
            buttonHistoryFore = new javax.swing.JButton();
            statusMsgLabel = new javax.swing.JLabel();
            statusErrorButton = new javax.swing.JButton();
            statusDesktopEntryButton = new javax.swing.JButton();
            toolBar = new javax.swing.JToolBar();
            tb_newEntry = new javax.swing.JButton();
            tb_open = new javax.swing.JButton();
            tb_save = new javax.swing.JButton();
            jSeparator4 = new javax.swing.JToolBar.Separator();
            tb_edit = new javax.swing.JButton();
            tb_delete = new javax.swing.JButton();
            tb_copy = new javax.swing.JButton();
            tb_paste = new javax.swing.JButton();
            tb_selectall = new javax.swing.JButton();
            jSeparator5 = new javax.swing.JToolBar.Separator();
            tb_addmanlinks = new javax.swing.JButton();
            tb_addluhmann = new javax.swing.JButton();
            tb_addbookmark = new javax.swing.JButton();
            tb_addtodesktop = new javax.swing.JButton();
            jSeparator10 = new javax.swing.JToolBar.Separator();
            tb_find = new javax.swing.JButton();
            tb_first = new javax.swing.JButton();
            tb_prev = new javax.swing.JButton();
            tb_next = new javax.swing.JButton();
            tb_last = new javax.swing.JButton();
            jSeparator32 = new javax.swing.JToolBar.Separator();
            jLabelMemory = new javax.swing.JLabel();
            jPopupMenuKeywords = new javax.swing.JPopupMenu();
            popupKeywordsCopy = new javax.swing.JMenuItem();
            jSeparator8 = new javax.swing.JSeparator();
            popupKeywordsSearchOr = new javax.swing.JMenuItem();
            popupKeywordsSearchAnd = new javax.swing.JMenuItem();
            popupKeywordsSearchNot = new javax.swing.JMenuItem();
            jSeparator9 = new javax.swing.JSeparator();
            popupKeywordsNew = new javax.swing.JMenuItem();
            popupKeywordsEdit = new javax.swing.JMenuItem();
            popupKeywordsDelete = new javax.swing.JMenuItem();
            jSeparator7 = new javax.swing.JSeparator();
            popupKeywordsAddToList = new javax.swing.JMenuItem();
            jSeparator45 = new javax.swing.JSeparator();
            popupKeywordsManLinks = new javax.swing.JMenuItem();
            popupKeywordsManLinksAnd = new javax.swing.JMenuItem();
            jSeparator66 = new javax.swing.JSeparator();
            popupKeywordsLuhmann = new javax.swing.JMenuItem();
            popupKeywordsLuhmannAnd = new javax.swing.JMenuItem();
            jSeparator46 = new javax.swing.JSeparator();
            popupKeywordsDesktop = new javax.swing.JMenuItem();
            popupKeywordsDesktopAnd = new javax.swing.JMenuItem();
            jPopupMenuKeywordList = new javax.swing.JPopupMenu();
            popupKwListCopy = new javax.swing.JMenuItem();
            jSeparator89 = new javax.swing.JSeparator();
            popupKwListSearchOr = new javax.swing.JMenuItem();
            popupKwListSearchAnd = new javax.swing.JMenuItem();
            popupKwListSearchNot = new javax.swing.JMenuItem();
            jSeparator13 = new javax.swing.JSeparator();
            popupKwListHighlight = new javax.swing.JMenuItem();
            popupKwListHighlightSegments = new javax.swing.JCheckBoxMenuItem();
            popupKwListRefresh = new javax.swing.JMenuItem();
            jSeparator11 = new javax.swing.JSeparator();
            popupKwListLogOr = new javax.swing.JCheckBoxMenuItem();
            popupKwListLogAnd = new javax.swing.JCheckBoxMenuItem();
            jSeparator12 = new javax.swing.JSeparator();
            popupKwListDelete = new javax.swing.JMenuItem();
            jPopupMenuAuthors = new javax.swing.JPopupMenu();
            popupAuthorsCopy = new javax.swing.JMenuItem();
            jSeparator14 = new javax.swing.JSeparator();
            popupAuthorsSearchLogOr = new javax.swing.JMenuItem();
            popupAuthorsSearchLogAnd = new javax.swing.JMenuItem();
            popupAuthorsSearchLogNot = new javax.swing.JMenuItem();
            jSeparator15 = new javax.swing.JSeparator();
            popupAuthorsNew = new javax.swing.JMenuItem();
            popupAuthorsEdit = new javax.swing.JMenuItem();
            popupAuthorsDelete = new javax.swing.JMenuItem();
            jSeparator91 = new javax.swing.JSeparator();
            popupAuthorsBibkey = new javax.swing.JMenuItem();
            jSeparator16 = new javax.swing.JSeparator();
            popupAuthorsAddToEntry = new javax.swing.JMenuItem();
            jSeparator49 = new javax.swing.JSeparator();
            popupAuthorsSubAdd = new javax.swing.JMenu();
            popupAuthorsManLinks = new javax.swing.JMenuItem();
            popupAuthorsManLinksAnd = new javax.swing.JMenuItem();
            jSeparator70 = new javax.swing.JSeparator();
            popupAuthorsLuhmann = new javax.swing.JMenuItem();
            popupAuthorsLuhmannAnd = new javax.swing.JMenuItem();
            jSeparator50 = new javax.swing.JSeparator();
            popupAuthorsDesktop = new javax.swing.JMenuItem();
            popupAuthorsDesktopAnd = new javax.swing.JMenuItem();
            jSeparator96 = new javax.swing.JSeparator();
            popupAuthorsImport = new javax.swing.JMenuItem();
            jPopupMenuLuhmann = new javax.swing.JPopupMenu();
            popupLuhmannAdd = new javax.swing.JMenuItem();
            jSeparator17 = new javax.swing.JSeparator();
            popupLuhmannDelete = new javax.swing.JMenuItem();
            jSeparator60 = new javax.swing.JSeparator();
            popupLuhmannManLinks = new javax.swing.JMenuItem();
            popupLuhmannBookmarks = new javax.swing.JMenuItem();
            jSeparator63 = new javax.swing.JSeparator();
            popupLuhmannDesktop = new javax.swing.JMenuItem();
            jSeparator117 = new javax.swing.JPopupMenu.Separator();
            popupLuhmannSetLevel = new javax.swing.JMenu();
            popupLuhmannLevelAll = new javax.swing.JMenuItem();
            jSeparator74 = new javax.swing.JPopupMenu.Separator();
            popupLuhmannLevel1 = new javax.swing.JMenuItem();
            popupLuhmannLevel2 = new javax.swing.JMenuItem();
            popupLuhmannLevel3 = new javax.swing.JMenuItem();
            popupLuhmannLevel4 = new javax.swing.JMenuItem();
            popupLuhmannLevel5 = new javax.swing.JMenuItem();
            jPopupMenuTitles = new javax.swing.JPopupMenu();
            popupTitlesCopy = new javax.swing.JMenuItem();
            jSeparator20 = new javax.swing.JSeparator();
            popupTitlesEdit = new javax.swing.JMenuItem();
            popupTitlesEditEntry = new javax.swing.JMenuItem();
            jSeparator103 = new javax.swing.JSeparator();
            popupTitlesDelete = new javax.swing.JMenuItem();
            jSeparator114 = new javax.swing.JPopupMenu.Separator();
            popupTitlesAutomaticTitle = new javax.swing.JMenuItem();
            jSeparator21 = new javax.swing.JSeparator();
            popupTitlesManLinks = new javax.swing.JMenuItem();
            popupTitlesLuhmann = new javax.swing.JMenuItem();
            popupTitlesBookmarks = new javax.swing.JMenuItem();
            jSeparator64 = new javax.swing.JSeparator();
            popupTitlesDesktop = new javax.swing.JMenuItem();
            jPopupMenuBookmarks = new javax.swing.JPopupMenu();
            popupBookmarksEdit = new javax.swing.JMenuItem();
            popupBookmarksDelete = new javax.swing.JMenuItem();
            jSeparator36 = new javax.swing.JSeparator();
            popupBookmarksEditCat = new javax.swing.JMenuItem();
            popupBookmarksDeleteCat = new javax.swing.JMenuItem();
            jSeparator38 = new javax.swing.JSeparator();
            popupBookmarksAddManLinks = new javax.swing.JMenuItem();
            popupBookmarksAddLuhmann = new javax.swing.JMenuItem();
            jSeparator56 = new javax.swing.JSeparator();
            popupBookmarkAddDesktop = new javax.swing.JMenuItem();
            jPopupMenuLinks = new javax.swing.JPopupMenu();
            popupLinksRefresh = new javax.swing.JMenuItem();
            jSeparator115 = new javax.swing.JPopupMenu.Separator();
            popupLinkRemoveManLink = new javax.swing.JMenuItem();
            jSeparator54 = new javax.swing.JSeparator();
            popupLinksManLinks = new javax.swing.JMenuItem();
            popupLinksLuhmann = new javax.swing.JMenuItem();
            jSeparator57 = new javax.swing.JSeparator();
            popupLinksDesktop = new javax.swing.JMenuItem();
            jPopupMenuAttachments = new javax.swing.JPopupMenu();
            popupAttachmentsCopy = new javax.swing.JMenuItem();
            jSeparator87 = new javax.swing.JSeparator();
            popupAttachmentsEdit = new javax.swing.JMenuItem();
            popupAttachmentsDelete = new javax.swing.JMenuItem();
            jSeparator94 = new javax.swing.JSeparator();
            popupAttachmentsGoto = new javax.swing.JMenuItem();
            jSeparator86 = new javax.swing.JSeparator();
            popupAttachmentsExport = new javax.swing.JMenuItem();
            jPopupMenuMain = new javax.swing.JPopupMenu();
            popupMainCopy = new javax.swing.JMenuItem();
            popupMainCopyPlain = new javax.swing.JMenuItem();
            jSeparator88 = new javax.swing.JSeparator();
            popupMainFind = new javax.swing.JMenuItem();
            jSeparator97 = new javax.swing.JSeparator();
            popupMainAddToKeyword = new javax.swing.JMenuItem();
            jSeparator98 = new javax.swing.JSeparator();
            popupMainSetFirstLineAsTitle = new javax.swing.JMenuItem();
            popupMainSetSelectionAsTitle = new javax.swing.JMenuItem();

            mainPanel.setName("mainPanel"); // NOI18N
            mainPanel.setLayout(new java.awt.BorderLayout());

            jSplitPaneMain1.setDividerLocation(650);
            jSplitPaneMain1.setName("jSplitPaneMain1"); // NOI18N
            jSplitPaneMain1.setOneTouchExpandable(true);

            jSplitPaneMain2.setDividerLocation(440);
            jSplitPaneMain2.setName("jSplitPaneMain2"); // NOI18N
            jSplitPaneMain2.setOneTouchExpandable(true);

            jPanel17.setName("jPanel17"); // NOI18N

            jScrollPane1.setBorder(null);
            jScrollPane1.setName("jScrollPane1"); // NOI18N

            jEditorPaneEntry.setEditable(false);
            jEditorPaneEntry.setBorder(null);
            org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ZettelkastenView.class);
            jEditorPaneEntry.setContentType(resourceMap.getString("jEditorPaneEntry.contentType")); // NOI18N
            jEditorPaneEntry.setName("jEditorPaneEntry"); // NOI18N
            jScrollPane1.setViewportView(jEditorPaneEntry);

            jPanelLiveSearch.setName("jPanelLiveSearch"); // NOI18N

            jTextFieldLiveSearch.setText(resourceMap.getString("jTextFieldLiveSearch.text")); // NOI18N
            jTextFieldLiveSearch.setToolTipText(resourceMap.getString("jTextFieldLiveSearch.toolTipText")); // NOI18N
            jTextFieldLiveSearch.setName("jTextFieldLiveSearch"); // NOI18N

            javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(ZettelkastenView.class, this);
            jButton1.setAction(actionMap.get("findLiveCancel")); // NOI18N
            jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
            jButton1.setBorderPainted(false);
            jButton1.setContentAreaFilled(false);
            jButton1.setFocusPainted(false);
            jButton1.setName("jButton1"); // NOI18N

            javax.swing.GroupLayout jPanelLiveSearchLayout = new javax.swing.GroupLayout(jPanelLiveSearch);
            jPanelLiveSearch.setLayout(jPanelLiveSearchLayout);
            jPanelLiveSearchLayout.setHorizontalGroup(
                jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLiveSearchLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jTextFieldLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );
            jPanelLiveSearchLayout.setVerticalGroup(
                jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelLiveSearchLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addGroup(jPanelLiveSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(3, 3, 3))
            );

            javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
            jPanel17.setLayout(jPanel17Layout);
            jPanel17Layout.setHorizontalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addComponent(jPanelLiveSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
            jPanel17Layout.setVerticalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel17Layout.createSequentialGroup()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                    .addGap(0, 0, 0)
                    .addComponent(jPanelLiveSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            );

            jSplitPaneMain2.setLeftComponent(jPanel17);

            jScrollPane3.setBorder(null);
            jScrollPane3.setName("jScrollPane3"); // NOI18N

            jListEntryKeywords.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListEntryKeywords.border.title"))); // NOI18N
            jListEntryKeywords.setModel(keywordListModel);
            jListEntryKeywords.setName("jListEntryKeywords"); // NOI18N
            jListEntryKeywords.setVisibleRowCount(-1);
            jScrollPane3.setViewportView(jListEntryKeywords);

            jSplitPaneMain2.setRightComponent(jScrollPane3);

            jSplitPaneMain1.setLeftComponent(jSplitPaneMain2);

            jPanelMainRight.setName("jPanelMainRight"); // NOI18N

            jTabbedPaneMain.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
            jTabbedPaneMain.setName("jTabbedPaneMain"); // NOI18N

            jPanel1.setName("jPanel1"); // NOI18N

            jSplitPaneLinks.setDividerLocation(250);
            jSplitPaneLinks.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPaneLinks.setName("jSplitPaneLinks"); // NOI18N
            jSplitPaneLinks.setOneTouchExpandable(true);

            jPanel14.setName("jPanel14"); // NOI18N

            jScrollPane4.setName("jScrollPane4"); // NOI18N

            jTableLinks.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Zettel", "Überschrift", "Relevanz", "Bewertung"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableLinks.setDragEnabled(true);
            jTableLinks.setName("jTableLinks"); // NOI18N
            jTableLinks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            jTableLinks.getTableHeader().setReorderingAllowed(false);
            jScrollPane4.setViewportView(jTableLinks);
            if (jTableLinks.getColumnModel().getColumnCount() > 0) {
                jTableLinks.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableLinks.columnModel.title0")); // NOI18N
                jTableLinks.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableLinks.columnModel.title1")); // NOI18N
                jTableLinks.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableLinks.columnModel.title2")); // NOI18N
                jTableLinks.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableLinks.columnModel.title3")); // NOI18N
            }

            javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
            jPanel14.setLayout(jPanel14Layout);
            jPanel14Layout.setHorizontalGroup(
                jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanel14Layout.setVerticalGroup(
                jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            );

            jSplitPaneLinks.setTopComponent(jPanel14);

            jPanelManLinks.setName("jPanelManLinks"); // NOI18N

            jScrollPane15.setBorder(null);
            jScrollPane15.setName("jScrollPane15"); // NOI18N

            jTableManLinks.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null},
                    {null, null, null},
                    {null, null, null},
                    {null, null, null}
                },
                new String [] {
                    "Eintrag", "Überschrift", "Bewertung"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.Integer.class, java.lang.String.class, java.lang.Float.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableManLinks.setToolTipText(resourceMap.getString("jTableManLinks.toolTipText")); // NOI18N
            jTableManLinks.setDragEnabled(true);
            jTableManLinks.setName("jTableManLinks"); // NOI18N
            jTableManLinks.getTableHeader().setReorderingAllowed(false);
            jScrollPane15.setViewportView(jTableManLinks);
            if (jTableManLinks.getColumnModel().getColumnCount() > 0) {
                jTableManLinks.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableManLinks.columnModel.title0")); // NOI18N
                jTableManLinks.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableManLinks.columnModel.title1")); // NOI18N
                jTableManLinks.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableManLinks.columnModel.title2")); // NOI18N
            }

            javax.swing.GroupLayout jPanelManLinksLayout = new javax.swing.GroupLayout(jPanelManLinks);
            jPanelManLinks.setLayout(jPanelManLinksLayout);
            jPanelManLinksLayout.setHorizontalGroup(
                jPanelManLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanelManLinksLayout.setVerticalGroup(
                jPanelManLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
            );

            jSplitPaneLinks.setRightComponent(jPanelManLinks);

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPaneLinks)
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPaneLinks)
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

            jPanel10.setName("jPanel10"); // NOI18N

            jSplitPane2.setDividerLocation(380);
            jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane2.setName("jSplitPane2"); // NOI18N
            jSplitPane2.setOneTouchExpandable(true);

            jScrollPane10.setBorder(null);
            jScrollPane10.setName("jScrollPane10"); // NOI18N

            jTreeLuhmann.setDragEnabled(true);
            jTreeLuhmann.setName("jTreeLuhmann"); // NOI18N
            jScrollPane10.setViewportView(jTreeLuhmann);

            jSplitPane2.setTopComponent(jScrollPane10);

            jScrollPane2.setBorder(null);
            jScrollPane2.setName("jScrollPane2"); // NOI18N

            jEditorPaneIsFollower.setEditable(false);
            jEditorPaneIsFollower.setContentType("text/html"); // NOI18N
            jEditorPaneIsFollower.setName("jEditorPaneIsFollower"); // NOI18N
            jScrollPane2.setViewportView(jEditorPaneIsFollower);

            jSplitPane2.setRightComponent(jScrollPane2);

            jCheckBoxShowAllLuhmann.setText(resourceMap.getString("jCheckBoxShowAllLuhmann.text")); // NOI18N
            jCheckBoxShowAllLuhmann.setToolTipText(resourceMap.getString("jCheckBoxShowAllLuhmann.toolTipText")); // NOI18N
            jCheckBoxShowAllLuhmann.setName("jCheckBoxShowAllLuhmann"); // NOI18N

            javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
            jPanel10.setLayout(jPanel10Layout);
            jPanel10Layout.setHorizontalGroup(
                jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane2)
                .addGroup(jPanel10Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jCheckBoxShowAllLuhmann)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel10Layout.setVerticalGroup(
                jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel10Layout.createSequentialGroup()
                    .addComponent(jSplitPane2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBoxShowAllLuhmann)
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel10.TabConstraints.tabTitle"), jPanel10); // NOI18N

            jPanel2.setName("jPanel2"); // NOI18N

            jTextFieldFilterKeywords.setText(resourceMap.getString("jTextFieldFilterKeywords.text")); // NOI18N
            jTextFieldFilterKeywords.setToolTipText(resourceMap.getString("jTextFieldFilterKeywords.toolTipText")); // NOI18N
            jTextFieldFilterKeywords.setEnabled(false);
            jTextFieldFilterKeywords.setName("jTextFieldFilterKeywords"); // NOI18N

            jButtonRefreshKeywords.setAction(actionMap.get("refreshKeywordList")); // NOI18N
            jButtonRefreshKeywords.setIcon(resourceMap.getIcon("jButtonRefreshKeywords.icon")); // NOI18N
            jButtonRefreshKeywords.setBorderPainted(false);
            jButtonRefreshKeywords.setContentAreaFilled(false);
            jButtonRefreshKeywords.setFocusPainted(false);
            jButtonRefreshKeywords.setName("jButtonRefreshKeywords"); // NOI18N

            jCheckBoxShowSynonyms.setText(resourceMap.getString("jCheckBoxShowSynonyms.text")); // NOI18N
            jCheckBoxShowSynonyms.setToolTipText(resourceMap.getString("jCheckBoxShowSynonyms.toolTipText")); // NOI18N
            jCheckBoxShowSynonyms.setName("jCheckBoxShowSynonyms"); // NOI18N

            jPanel16.setName("jPanel16"); // NOI18N

            jScrollPane17.setBorder(null);
            jScrollPane17.setName("jScrollPane17"); // NOI18N

            jTreeKeywords.setName("jTreeKeywords"); // NOI18N
            jScrollPane17.setViewportView(jTreeKeywords);

            jScrollPane6.setBorder(null);
            jScrollPane6.setName("jScrollPane6"); // NOI18N

            jTableKeywords.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Schlagwörter", "Häufigkeit"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.String.class, java.lang.Integer.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableKeywords.setDragEnabled(true);
            jTableKeywords.setName("jTableKeywords"); // NOI18N
            jTableKeywords.getTableHeader().setReorderingAllowed(false);
            jScrollPane6.setViewportView(jTableKeywords);
            if (jTableKeywords.getColumnModel().getColumnCount() > 0) {
                jTableKeywords.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableKeywords.columnModel.title0")); // NOI18N
                jTableKeywords.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableKeywords.columnModel.title1")); // NOI18N
            }

            javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
            jPanel16.setLayout(jPanel16Layout);
            jPanel16Layout.setHorizontalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanel16Layout.setVerticalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
            );

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jTextFieldFilterKeywords, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonRefreshKeywords, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jCheckBoxShowSynonyms)
                            .addGap(0, 102, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jCheckBoxShowSynonyms)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldFilterKeywords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButtonRefreshKeywords))
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

            jPanel7.setName("jPanel7"); // NOI18N

            jTextFieldFilterAuthors.setToolTipText(resourceMap.getString("jTextFieldFilterAuthors.toolTipText")); // NOI18N
            jTextFieldFilterAuthors.setEnabled(false);
            jTextFieldFilterAuthors.setName("jTextFieldFilterAuthors"); // NOI18N

            jSplitPaneAuthors.setDividerLocation(270);
            jSplitPaneAuthors.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPaneAuthors.setName("jSplitPaneAuthors"); // NOI18N
            jSplitPaneAuthors.setOneTouchExpandable(true);

            jPanel15.setName("jPanel15"); // NOI18N

            jScrollPane7.setBorder(null);
            jScrollPane7.setName("jScrollPane7"); // NOI18N

            jTableAuthors.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Autoren", "Häufigkeit"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.String.class, java.lang.Integer.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableAuthors.setDragEnabled(true);
            jTableAuthors.setName("jTableAuthors"); // NOI18N
            jTableAuthors.getTableHeader().setReorderingAllowed(false);
            jScrollPane7.setViewportView(jTableAuthors);
            if (jTableAuthors.getColumnModel().getColumnCount() > 0) {
                jTableAuthors.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableAuthors.columnModel.title0")); // NOI18N
                jTableAuthors.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableAuthors.columnModel.title1")); // NOI18N
            }

            jComboBoxAuthorType.setName("jComboBoxAuthorType"); // NOI18N

            javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
            jPanel15.setLayout(jPanel15Layout);
            jPanel15Layout.setHorizontalGroup(
                jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jComboBoxAuthorType, 0, 270, Short.MAX_VALUE)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanel15Layout.setVerticalGroup(
                jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBoxAuthorType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(3, 3, 3))
            );

            jSplitPaneAuthors.setTopComponent(jPanel15);

            jPanelDispAuthor.setName("jPanelDispAuthor"); // NOI18N

            jScrollPane16.setBorder(null);
            jScrollPane16.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane16.setName("jScrollPane16"); // NOI18N

            jEditorPaneDispAuthor.setEditable(false);
            jEditorPaneDispAuthor.setContentType(resourceMap.getString("jEditorPaneDispAuthor.contentType")); // NOI18N
            jEditorPaneDispAuthor.setName("jEditorPaneDispAuthor"); // NOI18N
            jScrollPane16.setViewportView(jEditorPaneDispAuthor);

            javax.swing.GroupLayout jPanelDispAuthorLayout = new javax.swing.GroupLayout(jPanelDispAuthor);
            jPanelDispAuthor.setLayout(jPanelDispAuthorLayout);
            jPanelDispAuthorLayout.setHorizontalGroup(
                jPanelDispAuthorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanelDispAuthorLayout.setVerticalGroup(
                jPanelDispAuthorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
            );

            jSplitPaneAuthors.setRightComponent(jPanelDispAuthor);

            jButtonRefreshAuthors.setAction(actionMap.get("refreshAuthorList")); // NOI18N
            jButtonRefreshAuthors.setIcon(resourceMap.getIcon("jButtonRefreshAuthors.icon")); // NOI18N
            jButtonRefreshAuthors.setBorderPainted(false);
            jButtonRefreshAuthors.setContentAreaFilled(false);
            jButtonRefreshAuthors.setFocusPainted(false);
            jButtonRefreshAuthors.setName("jButtonRefreshAuthors"); // NOI18N

            javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
            jPanel7.setLayout(jPanel7Layout);
            jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPaneAuthors)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(jTextFieldFilterAuthors)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButtonRefreshAuthors, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );
            jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                    .addComponent(jSplitPaneAuthors)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jTextFieldFilterAuthors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonRefreshAuthors))
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

            jPanel8.setName("jPanel8"); // NOI18N

            jScrollPane8.setBorder(null);
            jScrollPane8.setName("jScrollPane8"); // NOI18N

            jTableTitles.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Zettel", "Überschrift", "Erstellt", "Geändert", "Bewertung", "Folgezettel"
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
            jTableTitles.setDragEnabled(true);
            jTableTitles.setName("jTableTitles"); // NOI18N
            jTableTitles.getTableHeader().setReorderingAllowed(false);
            jScrollPane8.setViewportView(jTableTitles);
            if (jTableTitles.getColumnModel().getColumnCount() > 0) {
                jTableTitles.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title0")); // NOI18N
                jTableTitles.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title1")); // NOI18N
                jTableTitles.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title2")); // NOI18N
                jTableTitles.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title3")); // NOI18N
                jTableTitles.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title4")); // NOI18N
                jTableTitles.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("jTableTitles.columnModel.title5")); // NOI18N
            }

            jTextFieldFilterTitles.setToolTipText(resourceMap.getString("jTextFieldFilterTitles.toolTipText")); // NOI18N
            jTextFieldFilterTitles.setEnabled(false);
            jTextFieldFilterTitles.setName("jTextFieldFilterTitles"); // NOI18N

            jButtonRefreshTitles.setAction(actionMap.get("refreshTitleList")); // NOI18N
            jButtonRefreshTitles.setIcon(resourceMap.getIcon("jButtonRefreshTitles.icon")); // NOI18N
            jButtonRefreshTitles.setBorderPainted(false);
            jButtonRefreshTitles.setContentAreaFilled(false);
            jButtonRefreshTitles.setFocusPainted(false);
            jButtonRefreshTitles.setName("jButtonRefreshTitles"); // NOI18N

            javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
            jPanel8.setLayout(jPanel8Layout);
            jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jTextFieldFilterTitles, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButtonRefreshTitles, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonRefreshTitles)
                        .addComponent(jTextFieldFilterTitles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

            jPanel11.setName("jPanel11"); // NOI18N

            jTextFieldFilterCluster.setToolTipText(resourceMap.getString("jTextFieldFilterCluster.toolTipText")); // NOI18N
            jTextFieldFilterCluster.setEnabled(false);
            jTextFieldFilterCluster.setName("jTextFieldFilterCluster"); // NOI18N

            jButtonRefreshCluster.setAction(actionMap.get("refreshClusterList")); // NOI18N
            jButtonRefreshCluster.setIcon(resourceMap.getIcon("jButtonRefreshCluster.icon")); // NOI18N
            jButtonRefreshCluster.setBorderPainted(false);
            jButtonRefreshCluster.setContentAreaFilled(false);
            jButtonRefreshCluster.setFocusPainted(false);
            jButtonRefreshCluster.setName("jButtonRefreshCluster"); // NOI18N

            jCheckBoxCluster.setText(resourceMap.getString("jCheckBoxCluster.text")); // NOI18N
            jCheckBoxCluster.setToolTipText(resourceMap.getString("jCheckBoxCluster.toolTipText")); // NOI18N
            jCheckBoxCluster.setName("jCheckBoxCluster"); // NOI18N

            jPanel3.setName("jPanel3"); // NOI18N

            jSplitPane1.setDividerLocation(310);
            jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane1.setName("jSplitPane1"); // NOI18N

            jScrollPane5.setBorder(null);
            jScrollPane5.setName("jScrollPane5"); // NOI18N

            jEditorPaneClusterEntries.setEditable(false);
            jEditorPaneClusterEntries.setContentType("text/html"); // NOI18N
            jEditorPaneClusterEntries.setName("jEditorPaneClusterEntries"); // NOI18N
            jScrollPane5.setViewportView(jEditorPaneClusterEntries);

            jSplitPane1.setBottomComponent(jScrollPane5);

            jScrollPane11.setBorder(null);
            jScrollPane11.setName("jScrollPane11"); // NOI18N

            jTreeCluster.setName("jTreeCluster"); // NOI18N
            jTreeCluster.setRootVisible(false);
            jScrollPane11.setViewportView(jTreeCluster);

            jSplitPane1.setLeftComponent(jScrollPane11);

            javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
            jPanel3.setLayout(jPanel3Layout);
            jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1)
            );
            jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
            );

            javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
            jPanel11.setLayout(jPanel11Layout);
            jPanel11Layout.setHorizontalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jTextFieldFilterCluster)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonRefreshCluster, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel11Layout.createSequentialGroup()
                            .addComponent(jCheckBoxCluster)
                            .addGap(0, 32, Short.MAX_VALUE)))
                    .addContainerGap())
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
            jPanel11Layout.setVerticalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBoxCluster)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jTextFieldFilterCluster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonRefreshCluster))
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N

            jPanel9.setName("jPanel9"); // NOI18N

            jComboBoxBookmarkCategory.setToolTipText(resourceMap.getString("jComboBoxBookmarkCategory.toolTipText")); // NOI18N
            jComboBoxBookmarkCategory.setName("jComboBoxBookmarkCategory"); // NOI18N

            jSplitPane3.setDividerLocation(380);
            jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane3.setName("jSplitPane3"); // NOI18N
            jSplitPane3.setOneTouchExpandable(true);

            jScrollPane9.setBorder(null);
            jScrollPane9.setName("jScrollPane9"); // NOI18N

            jTableBookmarks.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null},
                    {null, null},
                    {null, null},
                    {null, null}
                },
                new String [] {
                    "Eintrag", "Kategorie"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.Integer.class, java.lang.String.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableBookmarks.setDragEnabled(true);
            jTableBookmarks.setName("jTableBookmarks"); // NOI18N
            jTableBookmarks.getTableHeader().setReorderingAllowed(false);
            jScrollPane9.setViewportView(jTableBookmarks);
            if (jTableBookmarks.getColumnModel().getColumnCount() > 0) {
                jTableBookmarks.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableBookmarks.columnModel.title0")); // NOI18N
                jTableBookmarks.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableBookmarks.columnModel.title1")); // NOI18N
            }

            jSplitPane3.setLeftComponent(jScrollPane9);

            jScrollPane14.setBorder(null);
            jScrollPane14.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane14.setName("jScrollPane14"); // NOI18N

            jEditorPaneBookmarkComment.setEditable(false);
            jEditorPaneBookmarkComment.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jEditorPaneBookmarkComment.border.title"))); // NOI18N
            jEditorPaneBookmarkComment.setContentType(resourceMap.getString("jEditorPaneBookmarkComment.contentType")); // NOI18N
            jEditorPaneBookmarkComment.setName("jEditorPaneBookmarkComment"); // NOI18N
            jScrollPane14.setViewportView(jEditorPaneBookmarkComment);

            jSplitPane3.setRightComponent(jScrollPane14);

            javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
            jPanel9.setLayout(jPanel9Layout);
            jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jComboBoxBookmarkCategory, 0, 258, Short.MAX_VALUE)
                    .addContainerGap())
                .addComponent(jSplitPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            );
            jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                    .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBoxBookmarkCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N

            jPanel13.setName("jPanel13"); // NOI18N

            jScrollPane13.setBorder(null);
            jScrollPane13.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane13.setName("jScrollPane13"); // NOI18N

            jTableAttachments.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null},
                    {null, null, null},
                    {null, null, null},
                    {null, null, null}
                },
                new String [] {
                    "Anhang", "Typ", "Zettel"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.String.class, java.lang.String.class, java.lang.Integer.class
                };
                boolean[] canEdit = new boolean [] {
                    false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jTableAttachments.setDragEnabled(true);
            jTableAttachments.setName("jTableAttachments"); // NOI18N
            jScrollPane13.setViewportView(jTableAttachments);
            if (jTableAttachments.getColumnModel().getColumnCount() > 0) {
                jTableAttachments.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableAttachments.columnModel.title0")); // NOI18N
                jTableAttachments.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableAttachments.columnModel.title1")); // NOI18N
                jTableAttachments.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableAttachments.columnModel.title2")); // NOI18N
            }

            jTextFieldFilterAttachments.setToolTipText(resourceMap.getString("jTextFieldFilterAttachments.toolTipText")); // NOI18N
            jTextFieldFilterAttachments.setEnabled(false);
            jTextFieldFilterAttachments.setName("jTextFieldFilterAttachments"); // NOI18N

            jButtonRefreshAttachments.setAction(actionMap.get("refreshAttachmentList")); // NOI18N
            jButtonRefreshAttachments.setIcon(resourceMap.getIcon("jButtonRefreshAttachments.icon")); // NOI18N
            jButtonRefreshAttachments.setBorderPainted(false);
            jButtonRefreshAttachments.setContentAreaFilled(false);
            jButtonRefreshAttachments.setFocusPainted(false);
            jButtonRefreshAttachments.setName("jButtonRefreshAttachments"); // NOI18N

            javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
            jPanel13.setLayout(jPanel13Layout);
            jPanel13Layout.setHorizontalGroup(
                jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jTextFieldFilterAttachments, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButtonRefreshAttachments, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            );
            jPanel13Layout.setVerticalGroup(
                jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jTextFieldFilterAttachments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonRefreshAttachments))
                    .addContainerGap())
            );

            jTabbedPaneMain.addTab(resourceMap.getString("jPanel13.TabConstraints.tabTitle"), jPanel13); // NOI18N

            javax.swing.GroupLayout jPanelMainRightLayout = new javax.swing.GroupLayout(jPanelMainRight);
            jPanelMainRight.setLayout(jPanelMainRightLayout);
            jPanelMainRightLayout.setHorizontalGroup(
                jPanelMainRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 291, Short.MAX_VALUE)
                .addGroup(jPanelMainRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPaneMain))
            );
            jPanelMainRightLayout.setVerticalGroup(
                jPanelMainRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 529, Short.MAX_VALUE)
                .addGroup(jPanelMainRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE))
            );

            jSplitPaneMain1.setRightComponent(jPanelMainRight);

            mainPanel.add(jSplitPaneMain1, java.awt.BorderLayout.CENTER);

            menuBar.setName("menuBar"); // NOI18N

            fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
            fileMenu.setName("fileMenu"); // NOI18N

            newEntryMenuItem.setAction(actionMap.get("newEntry")); // NOI18N
            newEntryMenuItem.setName("newEntryMenuItem"); // NOI18N
            fileMenu.add(newEntryMenuItem);

            insertEntryMenuItem.setAction(actionMap.get("insertEntry")); // NOI18N
            insertEntryMenuItem.setName("insertEntryMenuItem"); // NOI18N
            fileMenu.add(insertEntryMenuItem);

            jSeparator104.setName("jSeparator104"); // NOI18N
            fileMenu.add(jSeparator104);

            quickNewEntryMenuItem.setAction(actionMap.get("quickNewEntry")); // NOI18N
            quickNewEntryMenuItem.setName("quickNewEntryMenuItem"); // NOI18N
            fileMenu.add(quickNewEntryMenuItem);

            quickNewTitleEntryMenuItem.setAction(actionMap.get("quickNewEntryWithTitle")); // NOI18N
            quickNewTitleEntryMenuItem.setName("quickNewTitleEntryMenuItem"); // NOI18N
            fileMenu.add(quickNewTitleEntryMenuItem);

            jSeparator1.setName("jSeparator1"); // NOI18N
            fileMenu.add(jSeparator1);

            openMenuItem.setAction(actionMap.get("openDocument")); // NOI18N
            openMenuItem.setName("openMenuItem"); // NOI18N
            fileMenu.add(openMenuItem);

            recentDocsSubMenu.setText(resourceMap.getString("recentDocsSubMenu.text")); // NOI18N
            recentDocsSubMenu.setName("recentDocsSubMenu"); // NOI18N

            recentDoc1.setName("recentDoc1"); // NOI18N
            recentDocsSubMenu.add(recentDoc1);

            recentDoc2.setName("recentDoc2"); // NOI18N
            recentDocsSubMenu.add(recentDoc2);

            recentDoc3.setName("recentDoc3"); // NOI18N
            recentDocsSubMenu.add(recentDoc3);

            recentDoc4.setName("recentDoc4"); // NOI18N
            recentDocsSubMenu.add(recentDoc4);

            recentDoc5.setName("recentDoc5"); // NOI18N
            recentDocsSubMenu.add(recentDoc5);

            recentDoc6.setName("recentDoc6"); // NOI18N
            recentDocsSubMenu.add(recentDoc6);

            recentDoc7.setName("recentDoc7"); // NOI18N
            recentDocsSubMenu.add(recentDoc7);

            recentDoc8.setName("recentDoc8"); // NOI18N
            recentDocsSubMenu.add(recentDoc8);

            fileMenu.add(recentDocsSubMenu);

            jSeparator107.setName("jSeparator107"); // NOI18N
            fileMenu.add(jSeparator107);

            saveMenuItem.setAction(actionMap.get("saveDocument")); // NOI18N
            saveMenuItem.setName("saveMenuItem"); // NOI18N
            fileMenu.add(saveMenuItem);

            saveAsMenuItem.setAction(actionMap.get("saveDocumentAs")); // NOI18N
            saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
            fileMenu.add(saveAsMenuItem);

            jSeparator2.setName("jSeparator2"); // NOI18N
            fileMenu.add(jSeparator2);

            newDesktopMenuItem.setAction(actionMap.get("newDesktop")); // NOI18N
            newDesktopMenuItem.setName("newDesktopMenuItem"); // NOI18N
            fileMenu.add(newDesktopMenuItem);

            newZettelkastenMenuItem.setAction(actionMap.get("newZettelkasten")); // NOI18N
            newZettelkastenMenuItem.setName("newZettelkastenMenuItem"); // NOI18N
            fileMenu.add(newZettelkastenMenuItem);

            jSeparator78.setName("jSeparator78"); // NOI18N
            fileMenu.add(jSeparator78);

            importMenuItem.setAction(actionMap.get("importWindow")); // NOI18N
            importMenuItem.setName("importMenuItem"); // NOI18N
            fileMenu.add(importMenuItem);

            exportMenuItem.setAction(actionMap.get("exportWindow")); // NOI18N
            exportMenuItem.setName("exportMenuItem"); // NOI18N
            fileMenu.add(exportMenuItem);

            jSeparator77.setName("jSeparator77"); // NOI18N
            fileMenu.add(jSeparator77);

            menuFileInformation.setAction(actionMap.get("showInformationBox")); // NOI18N
            menuFileInformation.setName("menuFileInformation"); // NOI18N
            fileMenu.add(menuFileInformation);

            jSeparatorExit.setName("jSeparatorExit"); // NOI18N
            fileMenu.add(jSeparatorExit);

            exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
            exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
            exitMenuItem.setName("exitMenuItem"); // NOI18N
            fileMenu.add(exitMenuItem);

            menuBar.add(fileMenu);

            editMenu.setText(resourceMap.getString("editMenu.text")); // NOI18N
            editMenu.setName("editMenu"); // NOI18N

            editMenuItem.setAction(actionMap.get("editEntry")); // NOI18N
            editMenuItem.setName("editMenuItem"); // NOI18N
            editMenu.add(editMenuItem);

            jSeparator33.setName("jSeparator33"); // NOI18N
            editMenu.add(jSeparator33);

            deleteZettelMenuItem.setAction(actionMap.get("deleteCurrentEntry")); // NOI18N
            deleteZettelMenuItem.setName("deleteZettelMenuItem"); // NOI18N
            editMenu.add(deleteZettelMenuItem);

            jSeparator6.setName("jSeparator6"); // NOI18N
            editMenu.add(jSeparator6);

            deleteKwFromListMenuItem.setAction(actionMap.get("deleteKeywordFromEntry")); // NOI18N
            deleteKwFromListMenuItem.setName("deleteKwFromListMenuItem"); // NOI18N
            editMenu.add(deleteKwFromListMenuItem);

            jSeparator40.setName("jSeparator40"); // NOI18N
            editMenu.add(jSeparator40);

            copyMenuItem.setAction(actionMap.get("copy"));
            copyMenuItem.setName("copyMenuItem"); // NOI18N
            editMenu.add(copyMenuItem);

            copyPlainMenuItem.setAction(actionMap.get("copyPlain")); // NOI18N
            copyPlainMenuItem.setName("copyPlainMenuItem"); // NOI18N
            editMenu.add(copyPlainMenuItem);

            pasteMenuItem.setAction(actionMap.get("paste"));
            pasteMenuItem.setName("pasteMenuItem"); // NOI18N
            editMenu.add(pasteMenuItem);

            selectAllMenuItem.setAction(actionMap.get("selectAllText")); // NOI18N
            selectAllMenuItem.setName("selectAllMenuItem"); // NOI18N
            editMenu.add(selectAllMenuItem);

            jSeparator99.setName("jSeparator99"); // NOI18N
            editMenu.add(jSeparator99);

            addSelectionToKeywordMenuItem.setAction(actionMap.get("addToKeywordList")); // NOI18N
            addSelectionToKeywordMenuItem.setName("addSelectionToKeywordMenuItem"); // NOI18N
            editMenu.add(addSelectionToKeywordMenuItem);

            addFirstLineToTitleMenuItem.setAction(actionMap.get("setFirstLineAsTitle")); // NOI18N
            addFirstLineToTitleMenuItem.setName("addFirstLineToTitleMenuItem"); // NOI18N
            editMenu.add(addFirstLineToTitleMenuItem);

            addSelectionToTitleMenuItem.setAction(actionMap.get("setSelectionAsTitle")); // NOI18N
            addSelectionToTitleMenuItem.setName("addSelectionToTitleMenuItem"); // NOI18N
            editMenu.add(addSelectionToTitleMenuItem);

            jSeparator24.setName("jSeparator24"); // NOI18N
            editMenu.add(jSeparator24);

            manualInsertLinksMenuItem.setAction(actionMap.get("manualInsertLinks")); // NOI18N
            manualInsertLinksMenuItem.setName("manualInsertLinksMenuItem"); // NOI18N
            editMenu.add(manualInsertLinksMenuItem);

            manualInsertMenuItem.setAction(actionMap.get("manualInsertEntry")); // NOI18N
            manualInsertMenuItem.setName("manualInsertMenuItem"); // NOI18N
            editMenu.add(manualInsertMenuItem);

            jSeparator41.setName("jSeparator41"); // NOI18N
            editMenu.add(jSeparator41);

            setBookmarkMenuItem.setAction(actionMap.get("addToBookmark")); // NOI18N
            setBookmarkMenuItem.setName("setBookmarkMenuItem"); // NOI18N
            editMenu.add(setBookmarkMenuItem);

            addToDesktopMenuItem.setAction(actionMap.get("addToDesktop")); // NOI18N
            addToDesktopMenuItem.setName("addToDesktopMenuItem"); // NOI18N
            editMenu.add(addToDesktopMenuItem);

            menuBar.add(editMenu);

            findMenu.setText(resourceMap.getString("findMenu.text")); // NOI18N
            findMenu.setName("findMenu"); // NOI18N

            findMenuItem.setAction(actionMap.get("find")); // NOI18N
            findMenuItem.setName("findMenuItem"); // NOI18N
            findMenu.add(findMenuItem);

            findReplaceMenuItem.setAction(actionMap.get("replace")); // NOI18N
            findReplaceMenuItem.setName("findReplaceMenuItem"); // NOI18N
            findMenu.add(findReplaceMenuItem);

            jSeparator31.setName("jSeparator31"); // NOI18N
            findMenu.add(jSeparator31);

            findEntryWithout.setText(resourceMap.getString("findEntryWithout.text")); // NOI18N
            findEntryWithout.setName("findEntryWithout"); // NOI18N

            findEntriesWithoutKeywords.setAction(actionMap.get("findWithoutKeywords")); // NOI18N
            findEntriesWithoutKeywords.setName("findEntriesWithoutKeywords"); // NOI18N
            findEntryWithout.add(findEntriesWithoutKeywords);

            jSeparator69.setName("jSeparator69"); // NOI18N
            findEntryWithout.add(jSeparator69);

            findEntriesWithoutAuthors.setAction(actionMap.get("findWithoutAuthors")); // NOI18N
            findEntriesWithoutAuthors.setName("findEntriesWithoutAuthors"); // NOI18N
            findEntryWithout.add(findEntriesWithoutAuthors);

            jSeparator75.setName("jSeparator75"); // NOI18N
            findEntryWithout.add(jSeparator75);

            findEntriesWithoutRemarks.setAction(actionMap.get("findWithoutRemarks")); // NOI18N
            findEntriesWithoutRemarks.setName("findEntriesWithoutRemarks"); // NOI18N
            findEntryWithout.add(findEntriesWithoutRemarks);

            findEntriesWithRemarks.setAction(actionMap.get("findWithRemarks")); // NOI18N
            findEntriesWithRemarks.setName("findEntriesWithRemarks"); // NOI18N
            findEntryWithout.add(findEntriesWithRemarks);

            jSeparator106.setName("jSeparator106"); // NOI18N
            findEntryWithout.add(jSeparator106);

            findEntriesWithoutManualLinks.setAction(actionMap.get("findWithoutManualLinks")); // NOI18N
            findEntriesWithoutManualLinks.setName("findEntriesWithoutManualLinks"); // NOI18N
            findEntryWithout.add(findEntriesWithoutManualLinks);

            jSeparator65.setName("jSeparator65"); // NOI18N
            findEntryWithout.add(jSeparator65);

            findEntriesAnyLuhmann.setAction(actionMap.get("findLuhmannAny")); // NOI18N
            findEntriesAnyLuhmann.setName("findEntriesAnyLuhmann"); // NOI18N
            findEntryWithout.add(findEntriesAnyLuhmann);

            findEntriesTopLevelLuhmann.setAction(actionMap.get("findLuhmannParent")); // NOI18N
            findEntriesTopLevelLuhmann.setName("findEntriesTopLevelLuhmann"); // NOI18N
            findEntryWithout.add(findEntriesTopLevelLuhmann);

            jSeparator110.setName("jSeparator110"); // NOI18N
            findEntryWithout.add(jSeparator110);

            findEntriesWithRatings.setAction(actionMap.get("findWithRating")); // NOI18N
            findEntriesWithRatings.setName("findEntriesWithRatings"); // NOI18N
            findEntryWithout.add(findEntriesWithRatings);

            findEntriesWithoutRatings.setAction(actionMap.get("findWithoutRating")); // NOI18N
            findEntriesWithoutRatings.setName("findEntriesWithoutRatings"); // NOI18N
            findEntryWithout.add(findEntriesWithoutRatings);

            jSeparator76.setName("jSeparator76"); // NOI18N
            findEntryWithout.add(jSeparator76);

            findEntriesWithAttachments.setAction(actionMap.get("findWithAttachments")); // NOI18N
            findEntriesWithAttachments.setName("findEntriesWithAttachments"); // NOI18N
            findEntryWithout.add(findEntriesWithAttachments);

            jSeparator83.setName("jSeparator83"); // NOI18N
            findEntryWithout.add(jSeparator83);

            findEntriesFromCreatedTimestamp.setAction(actionMap.get("findEntriesWithTimeStampCreated")); // NOI18N
            findEntriesFromCreatedTimestamp.setName("findEntriesFromCreatedTimestamp"); // NOI18N
            findEntryWithout.add(findEntriesFromCreatedTimestamp);

            findEntriesFromEditedTimestamp.setAction(actionMap.get("findEntriesWithTimeStampEdited")); // NOI18N
            findEntriesFromEditedTimestamp.setName("findEntriesFromEditedTimestamp"); // NOI18N
            findEntryWithout.add(findEntriesFromEditedTimestamp);

            jSeparator95.setName("jSeparator95"); // NOI18N
            findEntryWithout.add(jSeparator95);

            findDoubleEntriesItem.setAction(actionMap.get("findDoubleEntries")); // NOI18N
            findDoubleEntriesItem.setName("findDoubleEntriesItem"); // NOI18N
            findEntryWithout.add(findDoubleEntriesItem);

            findMenu.add(findEntryWithout);

            jSeparator68.setName("jSeparator68"); // NOI18N
            findMenu.add(jSeparator68);

            findEntryKeywordsMenu.setText(resourceMap.getString("findEntryKeywordsMenu.text")); // NOI18N
            findEntryKeywordsMenu.setName("findEntryKeywordsMenu"); // NOI18N

            menuKwListSearchOr.setAction(actionMap.get("searchKeywordsFromListLogOr")); // NOI18N
            menuKwListSearchOr.setName("menuKwListSearchOr"); // NOI18N
            findEntryKeywordsMenu.add(menuKwListSearchOr);

            jSeparator19.setName("jSeparator19"); // NOI18N
            findEntryKeywordsMenu.add(jSeparator19);

            menuKwListSearchAnd.setAction(actionMap.get("searchKeywordsFromListLogAnd")); // NOI18N
            menuKwListSearchAnd.setName("menuKwListSearchAnd"); // NOI18N
            findEntryKeywordsMenu.add(menuKwListSearchAnd);

            jSeparator39.setName("jSeparator39"); // NOI18N
            findEntryKeywordsMenu.add(jSeparator39);

            menuKwListSearchNot.setAction(actionMap.get("searchKeywordsFromListLogNot")); // NOI18N
            menuKwListSearchNot.setName("menuKwListSearchNot"); // NOI18N
            findEntryKeywordsMenu.add(menuKwListSearchNot);

            findMenu.add(findEntryKeywordsMenu);

            jSeparator18.setName("jSeparator18"); // NOI18N
            findMenu.add(jSeparator18);

            liveSearchMenuItem.setAction(actionMap.get("findLive")); // NOI18N
            liveSearchMenuItem.setName("liveSearchMenuItem"); // NOI18N
            findMenu.add(liveSearchMenuItem);

            jSeparator22.setName("jSeparator22"); // NOI18N
            findMenu.add(jSeparator22);

            homeMenuItem.setAction(actionMap.get("showFirstEntry")); // NOI18N
            homeMenuItem.setName("homeMenuItem"); // NOI18N
            findMenu.add(homeMenuItem);

            prevEntryMenuItem.setAction(actionMap.get("showPrevEntry")); // NOI18N
            prevEntryMenuItem.setName("prevEntryMenuItem"); // NOI18N
            findMenu.add(prevEntryMenuItem);

            nextEntryMenuItem.setAction(actionMap.get("showNextEntry")); // NOI18N
            nextEntryMenuItem.setName("nextEntryMenuItem"); // NOI18N
            findMenu.add(nextEntryMenuItem);

            lastEntryMenuItem.setAction(actionMap.get("showLastEntry")); // NOI18N
            lastEntryMenuItem.setName("lastEntryMenuItem"); // NOI18N
            findMenu.add(lastEntryMenuItem);

            jSeparator72.setName("jSeparator72"); // NOI18N
            findMenu.add(jSeparator72);

            randomEntryMenuItem.setAction(actionMap.get("showRandomEntry")); // NOI18N
            randomEntryMenuItem.setName("randomEntryMenuItem"); // NOI18N
            findMenu.add(randomEntryMenuItem);

            jSeparator111.setName("jSeparator111"); // NOI18N
            findMenu.add(jSeparator111);

            historyForMenuItem.setAction(actionMap.get("historyBack")); // NOI18N
            historyForMenuItem.setText(resourceMap.getString("historyForMenuItem.text")); // NOI18N
            historyForMenuItem.setName("historyForMenuItem"); // NOI18N
            findMenu.add(historyForMenuItem);

            histroyBackMenuItem.setAction(actionMap.get("historyFor")); // NOI18N
            histroyBackMenuItem.setText(resourceMap.getString("histroyBackMenuItem.text")); // NOI18N
            histroyBackMenuItem.setName("histroyBackMenuItem"); // NOI18N
            findMenu.add(histroyBackMenuItem);

            goToFirstParentEntryMenuItem.setAction(actionMap.get("goToFirstParentEntry")); // NOI18N
            goToFirstParentEntryMenuItem.setText(resourceMap.getString("goToFirstParentEntryMenuItem.text")); // NOI18N
            goToFirstParentEntryMenuItem.setName("goToFirstParentEntryMenuItem"); // NOI18N
            findMenu.add(goToFirstParentEntryMenuItem);

            jSeparator112.setName("jSeparator112"); // NOI18N
            findMenu.add(jSeparator112);

            gotoEntryMenuItem.setAction(actionMap.get("gotoEntry")); // NOI18N
            gotoEntryMenuItem.setName("gotoEntryMenuItem"); // NOI18N
            findMenu.add(gotoEntryMenuItem);

            menuBar.add(findMenu);

            viewMenu.setText(resourceMap.getString("viewMenu.text")); // NOI18N
            viewMenu.setName("viewMenu"); // NOI18N

            showLinksMenuItem.setAction(actionMap.get("menuShowLinks")); // NOI18N
            showLinksMenuItem.setName("showLinksMenuItem"); // NOI18N
            viewMenu.add(showLinksMenuItem);

            showLuhmannMenuItem.setAction(actionMap.get("menuShowLuhmann")); // NOI18N
            showLuhmannMenuItem.setName("showLuhmannMenuItem"); // NOI18N
            viewMenu.add(showLuhmannMenuItem);

            showKeywordsMenuItem.setAction(actionMap.get("menuShowKeywords")); // NOI18N
            showKeywordsMenuItem.setName("showKeywordsMenuItem"); // NOI18N
            viewMenu.add(showKeywordsMenuItem);

            showAuthorsMenuItem.setAction(actionMap.get("menuShowAuthors")); // NOI18N
            showAuthorsMenuItem.setName("showAuthorsMenuItem"); // NOI18N
            viewMenu.add(showAuthorsMenuItem);

            showTitlesMenuItem.setAction(actionMap.get("menuShowTitles")); // NOI18N
            showTitlesMenuItem.setName("showTitlesMenuItem"); // NOI18N
            viewMenu.add(showTitlesMenuItem);

            showClusterMenuItem.setAction(actionMap.get("menuShowCluster")); // NOI18N
            showClusterMenuItem.setName("showClusterMenuItem"); // NOI18N
            viewMenu.add(showClusterMenuItem);

            showBookmarksMenuItem.setAction(actionMap.get("menuShowBookmarks")); // NOI18N
            showBookmarksMenuItem.setName("showBookmarksMenuItem"); // NOI18N
            viewMenu.add(showBookmarksMenuItem);

            showAttachmentsMenuItem.setAction(actionMap.get("menuShowAttachments")); // NOI18N
            showAttachmentsMenuItem.setName("showAttachmentsMenuItem"); // NOI18N
            viewMenu.add(showAttachmentsMenuItem);

            jSeparator23.setName("jSeparator23"); // NOI18N
            viewMenu.add(jSeparator23);

            showCurrentEntryAgain.setAction(actionMap.get("updateDisplay")); // NOI18N
            showCurrentEntryAgain.setName("showCurrentEntryAgain"); // NOI18N
            viewMenu.add(showCurrentEntryAgain);

            jSeparator55.setName("jSeparator55"); // NOI18N
            viewMenu.add(jSeparator55);

            showHighlightKeywords.setAction(actionMap.get("highlightKeywords")); // NOI18N
            showHighlightKeywords.setSelected(true);
            showHighlightKeywords.setName("showHighlightKeywords"); // NOI18N
            viewMenu.add(showHighlightKeywords);

            highlightSegmentsMenuItem.setAction(actionMap.get("highlightSegments")); // NOI18N
            highlightSegmentsMenuItem.setName("highlightSegmentsMenuItem"); // NOI18N
            viewMenu.add(highlightSegmentsMenuItem);

            menuBar.add(viewMenu);

            viewMenuLinks.setText(resourceMap.getString("viewMenuLinks.text")); // NOI18N
            viewMenuLinks.setName("viewMenuLinks"); // NOI18N

            viewMenuLinksKwListRefresh.setAction(actionMap.get("refreshFilteredLinks")); // NOI18N
            viewMenuLinksKwListRefresh.setName("viewMenuLinksKwListRefresh"); // NOI18N
            viewMenuLinks.add(viewMenuLinksKwListRefresh);

            jSeparator116.setName("jSeparator116"); // NOI18N
            viewMenuLinks.add(jSeparator116);

            viewMenuLinksRemoveManLink.setAction(actionMap.get("deleteManualLink")); // NOI18N
            viewMenuLinksRemoveManLink.setName("viewMenuLinksRemoveManLink"); // NOI18N
            viewMenuLinks.add(viewMenuLinksRemoveManLink);

            jSeparator3.setName("jSeparator3"); // NOI18N
            viewMenuLinks.add(jSeparator3);

            viewMenuLinksKwListLogOr.setAction(actionMap.get("keywordListLogOr")); // NOI18N
            viewMenuLinksKwListLogOr.setSelected(true);
            viewMenuLinksKwListLogOr.setName("viewMenuLinksKwListLogOr"); // NOI18N
            viewMenuLinks.add(viewMenuLinksKwListLogOr);

            viewMenuLinksKwListLogAnd.setAction(actionMap.get("keywordListLogAnd")); // NOI18N
            viewMenuLinksKwListLogAnd.setSelected(true);
            viewMenuLinksKwListLogAnd.setName("viewMenuLinksKwListLogAnd"); // NOI18N
            viewMenuLinks.add(viewMenuLinksKwListLogAnd);

            jSeparator53.setName("jSeparator53"); // NOI18N
            viewMenuLinks.add(jSeparator53);

            viewMenuLinksManLink.setAction(actionMap.get("addManLinks")); // NOI18N
            viewMenuLinksManLink.setName("viewMenuLinksManLink"); // NOI18N
            viewMenuLinks.add(viewMenuLinksManLink);

            viewMenuLinksLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            viewMenuLinksLuhmann.setName("viewMenuLinksLuhmann"); // NOI18N
            viewMenuLinks.add(viewMenuLinksLuhmann);

            jSeparator58.setName("jSeparator58"); // NOI18N
            viewMenuLinks.add(jSeparator58);

            viewMenuLinksDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            viewMenuLinksDesktop.setName("viewMenuLinksDesktop"); // NOI18N
            viewMenuLinks.add(viewMenuLinksDesktop);

            jSeparator100.setName("jSeparator100"); // NOI18N
            viewMenuLinks.add(jSeparator100);

            viewMenuLinksExport.setAction(actionMap.get("exportLinks")); // NOI18N
            viewMenuLinksExport.setName("viewMenuLinksExport"); // NOI18N
            viewMenuLinks.add(viewMenuLinksExport);

            viewMenuExportToSearch.setAction(actionMap.get("exportLinksToSearch")); // NOI18N
            viewMenuExportToSearch.setName("viewMenuExportToSearch"); // NOI18N
            viewMenuLinks.add(viewMenuExportToSearch);

            menuBar.add(viewMenuLinks);

            viewMenuLuhmann.setText(resourceMap.getString("viewMenuLuhmann.text")); // NOI18N
            viewMenuLuhmann.setName("viewMenuLuhmann"); // NOI18N

            viewMenuLuhmannDelete.setAction(actionMap.get("deleteLuhmannFromEntry")); // NOI18N
            viewMenuLuhmannDelete.setName("viewMenuLuhmannDelete"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannDelete);

            jSeparator61.setName("jSeparator61"); // NOI18N
            viewMenuLuhmann.add(jSeparator61);

            viewMenuLuhmannManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            viewMenuLuhmannManLinks.setName("viewMenuLuhmannManLinks"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannManLinks);

            viewMenuLuhmannBookmarks.setAction(actionMap.get("addBookmarks")); // NOI18N
            viewMenuLuhmannBookmarks.setName("viewMenuLuhmannBookmarks"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannBookmarks);

            jSeparator62.setName("jSeparator62"); // NOI18N
            viewMenuLuhmann.add(jSeparator62);

            viewMenuLuhmannDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            viewMenuLuhmannDesktop.setName("viewMenuLuhmannDesktop"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannDesktop);

            jSeparator73.setName("jSeparator73"); // NOI18N
            viewMenuLuhmann.add(jSeparator73);

            viewMenuLuhmannShowTopLevel.setAction(actionMap.get("findLuhmannParent")); // NOI18N
            viewMenuLuhmannShowTopLevel.setText(resourceMap.getString("viewMenuLuhmannShowTopLevel.text")); // NOI18N
            viewMenuLuhmannShowTopLevel.setName("viewMenuLuhmannShowTopLevel"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannShowTopLevel);

            jSeparator102.setName("jSeparator102"); // NOI18N
            viewMenuLuhmann.add(jSeparator102);

            viewMenuLuhmannExport.setAction(actionMap.get("exportLuhmann")); // NOI18N
            viewMenuLuhmannExport.setName("viewMenuLuhmannExport"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannExport);

            viewMenuLuhmannExportSearch.setAction(actionMap.get("exportLuhmannToSearch")); // NOI18N
            viewMenuLuhmannExportSearch.setName("viewMenuLuhmannExportSearch"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannExportSearch);

            jSeparator118.setName("jSeparator118"); // NOI18N
            viewMenuLuhmann.add(jSeparator118);

            viewMenuLuhmannShowNumbers.setAction(actionMap.get("showLuhmannEntryNumber")); // NOI18N
            viewMenuLuhmannShowNumbers.setSelected(true);
            viewMenuLuhmannShowNumbers.setName("viewMenuLuhmannShowNumbers"); // NOI18N
            viewMenuLuhmann.add(viewMenuLuhmannShowNumbers);

            jSeparator101.setName("jSeparator101"); // NOI18N
            viewMenuLuhmann.add(jSeparator101);

            viewMenuLuhmannShowLevel.setText(resourceMap.getString("viewMenuLuhmannShowLevel.text")); // NOI18N
            viewMenuLuhmannShowLevel.setName("viewMenuLuhmannShowLevel"); // NOI18N

            viewMenuLuhmannDepthAll.setAction(actionMap.get("setLuhmannLevelAll")); // NOI18N
            viewMenuLuhmannDepthAll.setName("viewMenuLuhmannDepthAll"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepthAll);

            jSeparator119.setName("jSeparator119"); // NOI18N
            viewMenuLuhmannShowLevel.add(jSeparator119);

            viewMenuLuhmannDepth1.setAction(actionMap.get("setLuhmannLevel1")); // NOI18N
            viewMenuLuhmannDepth1.setName("viewMenuLuhmannDepth1"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepth1);

            viewMenuLuhmannDepth2.setAction(actionMap.get("setLuhmannLevel2")); // NOI18N
            viewMenuLuhmannDepth2.setName("viewMenuLuhmannDepth2"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepth2);

            viewMenuLuhmannDepth3.setAction(actionMap.get("setLuhmannLevel3")); // NOI18N
            viewMenuLuhmannDepth3.setName("viewMenuLuhmannDepth3"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepth3);

            viewMenuLuhmannDepth4.setAction(actionMap.get("setLuhmannLevel4")); // NOI18N
            viewMenuLuhmannDepth4.setName("viewMenuLuhmannDepth4"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepth4);

            viewMenuLuhmannDepth5.setAction(actionMap.get("setLuhmannLevel5")); // NOI18N
            viewMenuLuhmannDepth5.setName("viewMenuLuhmannDepth5"); // NOI18N
            viewMenuLuhmannShowLevel.add(viewMenuLuhmannDepth5);

            viewMenuLuhmann.add(viewMenuLuhmannShowLevel);

            menuBar.add(viewMenuLuhmann);

            viewMenuKeywords.setText(resourceMap.getString("viewMenuKeywords.text")); // NOI18N
            viewMenuKeywords.setName("viewMenuKeywords"); // NOI18N

            viewKeywordsCopy.setAction(actionMap.get("copy"));
            viewKeywordsCopy.setText(resourceMap.getString("viewKeywordsCopy.text")); // NOI18N
            viewKeywordsCopy.setName("viewKeywordsCopy"); // NOI18N
            viewMenuKeywords.add(viewKeywordsCopy);

            jSeparator25.setName("jSeparator25"); // NOI18N
            viewMenuKeywords.add(jSeparator25);

            viewKeywordsSearchOr.setAction(actionMap.get("searchLogOr")); // NOI18N
            viewKeywordsSearchOr.setName("viewKeywordsSearchOr"); // NOI18N
            viewMenuKeywords.add(viewKeywordsSearchOr);

            viewKeywordsSearchAnd.setAction(actionMap.get("searchLogAnd")); // NOI18N
            viewKeywordsSearchAnd.setName("viewKeywordsSearchAnd"); // NOI18N
            viewMenuKeywords.add(viewKeywordsSearchAnd);

            viewKeywordsSearchNot.setAction(actionMap.get("searchLogNot")); // NOI18N
            viewKeywordsSearchNot.setName("viewKeywordsSearchNot"); // NOI18N
            viewMenuKeywords.add(viewKeywordsSearchNot);

            jSeparator26.setName("jSeparator26"); // NOI18N
            viewMenuKeywords.add(jSeparator26);

            viewKeywordsNew.setAction(actionMap.get("newKeyword")); // NOI18N
            viewKeywordsNew.setName("viewKeywordsNew"); // NOI18N
            viewMenuKeywords.add(viewKeywordsNew);

            viewKeywordsEdit.setAction(actionMap.get("editKeyword")); // NOI18N
            viewKeywordsEdit.setName("viewKeywordsEdit"); // NOI18N
            viewMenuKeywords.add(viewKeywordsEdit);

            viewKeywordsDelete.setAction(actionMap.get("deleteKeyword")); // NOI18N
            viewKeywordsDelete.setName("viewKeywordsDelete"); // NOI18N
            viewMenuKeywords.add(viewKeywordsDelete);

            jSeparator27.setName("jSeparator27"); // NOI18N
            viewMenuKeywords.add(jSeparator27);

            viewKeywordsAddToList.setAction(actionMap.get("addKeywordToList")); // NOI18N
            viewKeywordsAddToList.setName("viewKeywordsAddToList"); // NOI18N
            viewMenuKeywords.add(viewKeywordsAddToList);

            jSeparator47.setName("jSeparator47"); // NOI18N
            viewMenuKeywords.add(jSeparator47);

            viewKeywordsLuhmann.setAction(actionMap.get("addLuhmannLogOr")); // NOI18N
            viewKeywordsLuhmann.setName("viewKeywordsLuhmann"); // NOI18N
            viewMenuKeywords.add(viewKeywordsLuhmann);

            viewKeywordsLuhmannAnd.setAction(actionMap.get("addLuhmannLogAnd")); // NOI18N
            viewKeywordsLuhmannAnd.setName("viewKeywordsLuhmannAnd"); // NOI18N
            viewMenuKeywords.add(viewKeywordsLuhmannAnd);

            jSeparator67.setName("jSeparator67"); // NOI18N
            viewMenuKeywords.add(jSeparator67);

            viewKeywordsManLinks.setAction(actionMap.get("addManLinksLogOr")); // NOI18N
            viewKeywordsManLinks.setName("viewKeywordsManLinks"); // NOI18N
            viewMenuKeywords.add(viewKeywordsManLinks);

            viewKeywordsManLinksAnd.setAction(actionMap.get("addManLinksLogAnd")); // NOI18N
            viewKeywordsManLinksAnd.setName("viewKeywordsManLinksAnd"); // NOI18N
            viewMenuKeywords.add(viewKeywordsManLinksAnd);

            jSeparator48.setName("jSeparator48"); // NOI18N
            viewMenuKeywords.add(jSeparator48);

            viewKeywordsDesktop.setAction(actionMap.get("addDesktopLogOr")); // NOI18N
            viewKeywordsDesktop.setName("viewKeywordsDesktop"); // NOI18N
            viewMenuKeywords.add(viewKeywordsDesktop);

            viewKeywordsDesktopAnd.setAction(actionMap.get("addDesktopLogAnd")); // NOI18N
            viewKeywordsDesktopAnd.setName("viewKeywordsDesktopAnd"); // NOI18N
            viewMenuKeywords.add(viewKeywordsDesktopAnd);

            jSeparator80.setName("jSeparator80"); // NOI18N
            viewMenuKeywords.add(jSeparator80);

            viewKeywordsExport.setAction(actionMap.get("exportKeywords")); // NOI18N
            viewKeywordsExport.setName("viewKeywordsExport"); // NOI18N
            viewMenuKeywords.add(viewKeywordsExport);

            menuBar.add(viewMenuKeywords);

            viewMenuAuthors.setText(resourceMap.getString("viewMenuAuthors.text")); // NOI18N
            viewMenuAuthors.setName("viewMenuAuthors"); // NOI18N

            viewAuthorsCopy.setAction(actionMap.get("copy"));
            viewAuthorsCopy.setText(resourceMap.getString("viewAuthorsCopy.text")); // NOI18N
            viewAuthorsCopy.setName("viewAuthorsCopy"); // NOI18N
            viewAuthorsCopy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    viewAuthorsCopyActionPerformed(evt);
                }
            });
            viewMenuAuthors.add(viewAuthorsCopy);

            jSeparator28.setName("jSeparator28"); // NOI18N
            viewMenuAuthors.add(jSeparator28);

            viewAuthorsSubFind.setText(resourceMap.getString("viewAuthorsSubFind.text")); // NOI18N
            viewAuthorsSubFind.setName("viewAuthorsSubFind"); // NOI18N

            viewAuthorsSearchOr.setAction(actionMap.get("searchLogOr")); // NOI18N
            viewAuthorsSearchOr.setName("viewAuthorsSearchOr"); // NOI18N
            viewAuthorsSubFind.add(viewAuthorsSearchOr);

            viewAuthorsSearchAnd.setAction(actionMap.get("searchLogAnd")); // NOI18N
            viewAuthorsSearchAnd.setName("viewAuthorsSearchAnd"); // NOI18N
            viewAuthorsSubFind.add(viewAuthorsSearchAnd);

            viewAuthorsSearchNot.setAction(actionMap.get("searchLogNot")); // NOI18N
            viewAuthorsSearchNot.setName("viewAuthorsSearchNot"); // NOI18N
            viewAuthorsSubFind.add(viewAuthorsSearchNot);

            viewMenuAuthors.add(viewAuthorsSubFind);

            jSeparator29.setName("jSeparator29"); // NOI18N
            viewMenuAuthors.add(jSeparator29);

            viewAuthorsSubEdit.setText(resourceMap.getString("viewAuthorsSubEdit.text")); // NOI18N
            viewAuthorsSubEdit.setName("viewAuthorsSubEdit"); // NOI18N

            viewAuthorsNew.setAction(actionMap.get("newAuthor")); // NOI18N
            viewAuthorsNew.setName("viewAuthorsNew"); // NOI18N
            viewAuthorsSubEdit.add(viewAuthorsNew);

            viewAuthorsEdit.setAction(actionMap.get("editAuthor")); // NOI18N
            viewAuthorsEdit.setName("viewAuthorsEdit"); // NOI18N
            viewAuthorsSubEdit.add(viewAuthorsEdit);

            viewAuthorsDelete.setAction(actionMap.get("deleteAuthor")); // NOI18N
            viewAuthorsDelete.setName("viewAuthorsDelete"); // NOI18N
            viewAuthorsSubEdit.add(viewAuthorsDelete);

            jSeparator90.setName("jSeparator90"); // NOI18N
            viewAuthorsSubEdit.add(jSeparator90);

            viewAuthorsBibkey.setAction(actionMap.get("changeBibkey")); // NOI18N
            viewAuthorsBibkey.setName("viewAuthorsBibkey"); // NOI18N
            viewAuthorsSubEdit.add(viewAuthorsBibkey);

            viewMenuAuthors.add(viewAuthorsSubEdit);

            jSeparator30.setName("jSeparator30"); // NOI18N
            viewMenuAuthors.add(jSeparator30);

            viewAuthorsSubAdd.setText(resourceMap.getString("viewAuthorsSubAdd.text")); // NOI18N
            viewAuthorsSubAdd.setName("viewAuthorsSubAdd"); // NOI18N

            viewAuthorsAddToEntry.setAction(actionMap.get("addAuthorToList")); // NOI18N
            viewAuthorsAddToEntry.setName("viewAuthorsAddToEntry"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsAddToEntry);

            jSeparator51.setName("jSeparator51"); // NOI18N
            viewAuthorsSubAdd.add(jSeparator51);

            viewAuthorsManLinks.setAction(actionMap.get("addManLinksLogOr")); // NOI18N
            viewAuthorsManLinks.setName("viewAuthorsManLinks"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsManLinks);

            viewAuthorsManLinksAnd.setAction(actionMap.get("addManLinksLogAnd")); // NOI18N
            viewAuthorsManLinksAnd.setName("viewAuthorsManLinksAnd"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsManLinksAnd);

            jSeparator71.setName("jSeparator71"); // NOI18N
            viewAuthorsSubAdd.add(jSeparator71);

            viewAuthorsAddLuhmann.setAction(actionMap.get("addLuhmannLogOr")); // NOI18N
            viewAuthorsAddLuhmann.setName("viewAuthorsAddLuhmann"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsAddLuhmann);

            viewAuthorsAddLuhmannAnd.setAction(actionMap.get("addLuhmannLogAnd")); // NOI18N
            viewAuthorsAddLuhmannAnd.setName("viewAuthorsAddLuhmannAnd"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsAddLuhmannAnd);

            jSeparator52.setName("jSeparator52"); // NOI18N
            viewAuthorsSubAdd.add(jSeparator52);

            viewAuthorsDesktop.setAction(actionMap.get("addDesktopLogOr")); // NOI18N
            viewAuthorsDesktop.setName("viewAuthorsDesktop"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsDesktop);

            viewAuthorsDesktopAnd.setAction(actionMap.get("addDesktopLogAnd")); // NOI18N
            viewAuthorsDesktopAnd.setName("viewAuthorsDesktopAnd"); // NOI18N
            viewAuthorsSubAdd.add(viewAuthorsDesktopAnd);

            viewMenuAuthors.add(viewAuthorsSubAdd);

            jSeparator81.setName("jSeparator81"); // NOI18N
            viewMenuAuthors.add(jSeparator81);

            viewAuthorsImport.setAction(actionMap.get("importAuthors")); // NOI18N
            viewAuthorsImport.setName("viewAuthorsImport"); // NOI18N
            viewMenuAuthors.add(viewAuthorsImport);

            viewAuthorsExport.setAction(actionMap.get("exportAuthors")); // NOI18N
            viewAuthorsExport.setName("viewAuthorsExport"); // NOI18N
            viewMenuAuthors.add(viewAuthorsExport);

            jSeparator92.setName("jSeparator92"); // NOI18N
            viewMenuAuthors.add(jSeparator92);

            viewAuthorsAttachBibtexFile.setAction(actionMap.get("attachBibtexFile")); // NOI18N
            viewAuthorsAttachBibtexFile.setName("viewAuthorsAttachBibtexFile"); // NOI18N
            viewMenuAuthors.add(viewAuthorsAttachBibtexFile);

            viewAuthorsRefreshBibtexFile.setAction(actionMap.get("refreshBibTexFile")); // NOI18N
            viewAuthorsRefreshBibtexFile.setName("viewAuthorsRefreshBibtexFile"); // NOI18N
            viewMenuAuthors.add(viewAuthorsRefreshBibtexFile);

            menuBar.add(viewMenuAuthors);

            viewMenuTitles.setText(resourceMap.getString("viewMenuTitles.text")); // NOI18N
            viewMenuTitles.setName("viewMenuTitles"); // NOI18N

            viewTitlesCopy.setAction(actionMap.get("copy"));
            viewTitlesCopy.setText(resourceMap.getString("viewTitlesCopy.text")); // NOI18N
            viewTitlesCopy.setName("viewTitlesCopy"); // NOI18N
            viewMenuTitles.add(viewTitlesCopy);

            jSeparator43.setName("jSeparator43"); // NOI18N
            viewMenuTitles.add(jSeparator43);

            viewTitlesEdit.setAction(actionMap.get("editTitle")); // NOI18N
            viewTitlesEdit.setName("viewTitlesEdit"); // NOI18N
            viewMenuTitles.add(viewTitlesEdit);

            viewTitlesDelete.setAction(actionMap.get("deleteEntry")); // NOI18N
            viewTitlesDelete.setName("viewTitlesDelete"); // NOI18N
            viewMenuTitles.add(viewTitlesDelete);

            jSeparator105.setName("jSeparator105"); // NOI18N
            viewMenuTitles.add(jSeparator105);

            viewTitlesAutomaticFirstLine.setAction(actionMap.get("automaticFirstLineAsTitle")); // NOI18N
            viewTitlesAutomaticFirstLine.setName("viewTitlesAutomaticFirstLine"); // NOI18N
            viewMenuTitles.add(viewTitlesAutomaticFirstLine);

            jSeparator42.setName("jSeparator42"); // NOI18N
            viewMenuTitles.add(jSeparator42);

            viewTitlesManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            viewTitlesManLinks.setName("viewTitlesManLinks"); // NOI18N
            viewMenuTitles.add(viewTitlesManLinks);

            viewTitlesLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            viewTitlesLuhmann.setName("viewTitlesLuhmann"); // NOI18N
            viewMenuTitles.add(viewTitlesLuhmann);

            viewTitlesBookmarks.setAction(actionMap.get("addBookmarks")); // NOI18N
            viewTitlesBookmarks.setName("viewTitlesBookmarks"); // NOI18N
            viewMenuTitles.add(viewTitlesBookmarks);

            jSeparator113.setName("jSeparator113"); // NOI18N
            viewMenuTitles.add(jSeparator113);

            viewTitlesDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            viewTitlesDesktop.setName("viewTitlesDesktop"); // NOI18N
            viewMenuTitles.add(viewTitlesDesktop);

            jSeparator108.setName("jSeparator108"); // NOI18N
            viewMenuTitles.add(jSeparator108);

            viewTitlesExport.setAction(actionMap.get("exportTitles")); // NOI18N
            viewTitlesExport.setName("viewTitlesExport"); // NOI18N
            viewMenuTitles.add(viewTitlesExport);

            menuBar.add(viewMenuTitles);

            viewMenuCluster.setText(resourceMap.getString("viewMenuCluster.text")); // NOI18N
            viewMenuCluster.setName("viewMenuCluster"); // NOI18N

            viewClusterExport.setAction(actionMap.get("exportCluster")); // NOI18N
            viewClusterExport.setName("viewClusterExport"); // NOI18N
            viewMenuCluster.add(viewClusterExport);

            viewClusterExportToSearch.setAction(actionMap.get("exportClusterToSearch")); // NOI18N
            viewClusterExportToSearch.setName("viewClusterExportToSearch"); // NOI18N
            viewMenuCluster.add(viewClusterExportToSearch);

            menuBar.add(viewMenuCluster);

            viewMenuBookmarks.setText(resourceMap.getString("viewMenuBookmarks.text")); // NOI18N
            viewMenuBookmarks.setName("viewMenuBookmarks"); // NOI18N

            viewBookmarksEdit.setAction(actionMap.get("editBookmark")); // NOI18N
            viewBookmarksEdit.setName("viewBookmarksEdit"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksEdit);

            viewBookmarksDelete.setAction(actionMap.get("deleteBookmark")); // NOI18N
            viewBookmarksDelete.setName("viewBookmarksDelete"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksDelete);

            jSeparator35.setName("jSeparator35"); // NOI18N
            viewMenuBookmarks.add(jSeparator35);

            viewBookmarksEditCat.setAction(actionMap.get("editBookmarkCategory")); // NOI18N
            viewBookmarksEditCat.setName("viewBookmarksEditCat"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksEditCat);

            viewBookmarksDeleteCat.setAction(actionMap.get("deleteBookmarkCategory")); // NOI18N
            viewBookmarksDeleteCat.setName("viewBookmarksDeleteCat"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksDeleteCat);

            jSeparator37.setName("jSeparator37"); // NOI18N
            viewMenuBookmarks.add(jSeparator37);

            viewBookmarksManLink.setAction(actionMap.get("addManLinks")); // NOI18N
            viewBookmarksManLink.setName("viewBookmarksManLink"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksManLink);

            viewBookmarksAddLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            viewBookmarksAddLuhmann.setName("viewBookmarksAddLuhmann"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksAddLuhmann);

            jSeparator59.setName("jSeparator59"); // NOI18N
            viewMenuBookmarks.add(jSeparator59);

            viewBookmarkDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            viewBookmarkDesktop.setName("viewBookmarkDesktop"); // NOI18N
            viewMenuBookmarks.add(viewBookmarkDesktop);

            jSeparator82.setName("jSeparator82"); // NOI18N
            viewMenuBookmarks.add(jSeparator82);

            viewBookmarksExport.setAction(actionMap.get("exportBookmarks")); // NOI18N
            viewBookmarksExport.setName("viewBookmarksExport"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksExport);

            viewBookmarksExportSearch.setAction(actionMap.get("exportBookmarksToSearch")); // NOI18N
            viewBookmarksExportSearch.setName("viewBookmarksExportSearch"); // NOI18N
            viewMenuBookmarks.add(viewBookmarksExportSearch);

            menuBar.add(viewMenuBookmarks);

            viewMenuAttachments.setText(resourceMap.getString("viewMenuAttachments.text")); // NOI18N
            viewMenuAttachments.setName("viewMenuAttachments"); // NOI18N

            viewAttachmentsCopy.setAction(actionMap.get("copy"));
            viewAttachmentsCopy.setText(resourceMap.getString("viewAttachmentsCopy.text")); // NOI18N
            viewAttachmentsCopy.setName("viewAttachmentsCopy"); // NOI18N
            viewMenuAttachments.add(viewAttachmentsCopy);

            jSeparator84.setName("jSeparator84"); // NOI18N
            viewMenuAttachments.add(jSeparator84);

            viewAttachmentEdit.setAction(actionMap.get("editAttachment")); // NOI18N
            viewAttachmentEdit.setName("viewAttachmentEdit"); // NOI18N
            viewMenuAttachments.add(viewAttachmentEdit);

            viewAttachmentsDelete.setAction(actionMap.get("deleteAttachment")); // NOI18N
            viewAttachmentsDelete.setName("viewAttachmentsDelete"); // NOI18N
            viewMenuAttachments.add(viewAttachmentsDelete);

            jSeparator85.setName("jSeparator85"); // NOI18N
            viewMenuAttachments.add(jSeparator85);

            viewMenuAttachmentGoto.setAction(actionMap.get("openAttachmentDirectory")); // NOI18N
            viewMenuAttachmentGoto.setName("viewMenuAttachmentGoto"); // NOI18N
            viewMenuAttachments.add(viewMenuAttachmentGoto);

            jSeparator93.setName("jSeparator93"); // NOI18N
            viewMenuAttachments.add(jSeparator93);

            viewAttachmentsExport.setAction(actionMap.get("exportAttachments")); // NOI18N
            viewAttachmentsExport.setName("viewAttachmentsExport"); // NOI18N
            viewMenuAttachments.add(viewAttachmentsExport);

            menuBar.add(viewMenuAttachments);

            windowsMenu.setText(resourceMap.getString("windowsMenu.text")); // NOI18N
            windowsMenu.setName("windowsMenu"); // NOI18N

            showSearchResultsMenuItem.setAction(actionMap.get("showSearchResultWindow")); // NOI18N
            showSearchResultsMenuItem.setName("showSearchResultsMenuItem"); // NOI18N
            windowsMenu.add(showSearchResultsMenuItem);

            jSeparator44.setName("jSeparator44"); // NOI18N
            windowsMenu.add(jSeparator44);

            showDesktopMenuItem.setAction(actionMap.get("showDesktopWindow")); // NOI18N
            showDesktopMenuItem.setName("showDesktopMenuItem"); // NOI18N
            windowsMenu.add(showDesktopMenuItem);

            jSeparator109.setName("jSeparator109"); // NOI18N
            windowsMenu.add(jSeparator109);

            showNewEntryMenuItem.setAction(actionMap.get("showNewEntryWindow")); // NOI18N
            showNewEntryMenuItem.setName("showNewEntryMenuItem"); // NOI18N
            windowsMenu.add(showNewEntryMenuItem);

            jSeparator34.setName("jSeparator34"); // NOI18N
            windowsMenu.add(jSeparator34);

            showErrorLogMenuItem.setAction(actionMap.get("showErrorLog")); // NOI18N
            showErrorLogMenuItem.setName("showErrorLogMenuItem"); // NOI18N
            windowsMenu.add(showErrorLogMenuItem);

            menuBar.add(windowsMenu);

            aboutMenu.setText(resourceMap.getString("aboutMenu.text")); // NOI18N
            aboutMenu.setName("aboutMenu"); // NOI18N

            aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
            aboutMenuItem.setName("aboutMenuItem"); // NOI18N
            aboutMenu.add(aboutMenuItem);

            jSeparatorAbout1.setName("jSeparatorAbout1"); // NOI18N
            aboutMenu.add(jSeparatorAbout1);

            preferencesMenuItem.setAction(actionMap.get("settingsWindow")); // NOI18N
            preferencesMenuItem.setName("preferencesMenuItem"); // NOI18N
            aboutMenu.add(preferencesMenuItem);

            menuBar.add(aboutMenu);

            statusPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("statusPanel.border.matteColor"))); // NOI18N
            statusPanel.setMinimumSize(new java.awt.Dimension(200, 16));
            statusPanel.setName("statusPanel"); // NOI18N

            jPanel12.setName("jPanel12"); // NOI18N

            statusEntryLabel.setText(resourceMap.getString("statusEntryLabel.text")); // NOI18N
            statusEntryLabel.setName("statusEntryLabel"); // NOI18N

            statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

            jTextFieldEntryNumber.setColumns(4);
            jTextFieldEntryNumber.setText(resourceMap.getString("jTextFieldEntryNumber.text")); // NOI18N
            jTextFieldEntryNumber.setToolTipText(resourceMap.getString("jTextFieldEntryNumber.toolTipText")); // NOI18N
            jTextFieldEntryNumber.setName("jTextFieldEntryNumber"); // NOI18N

            statusOfEntryLabel.setText(resourceMap.getString("statusOfEntryLabel.text")); // NOI18N
            statusOfEntryLabel.setName("statusOfEntryLabel"); // NOI18N

            buttonHistoryBack.setAction(actionMap.get("historyBack")); // NOI18N
            buttonHistoryBack.setIcon(resourceMap.getIcon("buttonHistoryBack.icon")); // NOI18N
            buttonHistoryBack.setBorderPainted(false);
            buttonHistoryBack.setContentAreaFilled(false);
            buttonHistoryBack.setFocusPainted(false);
            buttonHistoryBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
            buttonHistoryBack.setName("buttonHistoryBack"); // NOI18N

            buttonHistoryFore.setAction(actionMap.get("historyFor")); // NOI18N
            buttonHistoryFore.setIcon(resourceMap.getIcon("buttonHistoryFore.icon")); // NOI18N
            buttonHistoryFore.setBorderPainted(false);
            buttonHistoryFore.setContentAreaFilled(false);
            buttonHistoryFore.setFocusPainted(false);
            buttonHistoryFore.setMargin(new java.awt.Insets(0, 0, 0, 0));
            buttonHistoryFore.setName("buttonHistoryFore"); // NOI18N

            statusMsgLabel.setText(resourceMap.getString("statusMsgLabel.text")); // NOI18N
            statusMsgLabel.setName("statusMsgLabel"); // NOI18N

            statusErrorButton.setAction(actionMap.get("showErrorLog")); // NOI18N
            statusErrorButton.setIcon(resourceMap.getIcon("statusErrorButton.icon")); // NOI18N
            statusErrorButton.setText(resourceMap.getString("statusErrorButton.text")); // NOI18N
            statusErrorButton.setToolTipText(resourceMap.getString("statusErrorButton.toolTipText")); // NOI18N
            statusErrorButton.setBorderPainted(false);
            statusErrorButton.setContentAreaFilled(false);
            statusErrorButton.setFocusPainted(false);
            statusErrorButton.setName("statusErrorButton"); // NOI18N

            statusDesktopEntryButton.setAction(actionMap.get("showEntryInDesktopWindow")); // NOI18N
            statusDesktopEntryButton.setIcon(resourceMap.getIcon("statusDesktopEntryButton.icon")); // NOI18N
            statusDesktopEntryButton.setText(resourceMap.getString("statusDesktopEntryButton.text")); // NOI18N
            statusDesktopEntryButton.setToolTipText(resourceMap.getString("statusDesktopEntryButton.toolTipText")); // NOI18N
            statusDesktopEntryButton.setBorderPainted(false);
            statusDesktopEntryButton.setContentAreaFilled(false);
            statusDesktopEntryButton.setFocusPainted(false);
            statusDesktopEntryButton.setName("statusDesktopEntryButton"); // NOI18N

            javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
            jPanel12.setLayout(jPanel12Layout);
            jPanel12Layout.setHorizontalGroup(
                jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                    .addComponent(statusEntryLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextFieldEntryNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(statusOfEntryLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(buttonHistoryBack, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(buttonHistoryFore, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(6, 6, 6)
                    .addComponent(statusErrorButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(statusDesktopEntryButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 667, Short.MAX_VALUE)
                    .addComponent(statusMsgLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(statusAnimationLabel))
            );
            jPanel12Layout.setVerticalGroup(
                jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel12Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusEntryLabel)
                            .addComponent(jTextFieldEntryNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusOfEntryLabel))
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(buttonHistoryFore, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonHistoryBack, javax.swing.GroupLayout.Alignment.LEADING))
                        .addComponent(statusAnimationLabel)
                        .addComponent(statusMsgLabel)
                        .addComponent(statusErrorButton)
                        .addComponent(statusDesktopEntryButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
            statusPanel.setLayout(statusPanelLayout);
            statusPanelLayout.setHorizontalGroup(
                statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(statusPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );
            statusPanelLayout.setVerticalGroup(
                statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            );

            toolBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("toolBar.border.matteColor"))); // NOI18N
            toolBar.setMinimumSize(new java.awt.Dimension(300, 20));
            toolBar.setName("toolBar"); // NOI18N

            tb_newEntry.setAction(actionMap.get("newEntry")); // NOI18N
            tb_newEntry.setText(resourceMap.getString("tb_newEntry.text")); // NOI18N
            tb_newEntry.setBorderPainted(false);
            tb_newEntry.setFocusPainted(false);
            tb_newEntry.setFocusable(false);
            tb_newEntry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_newEntry.setName("tb_newEntry"); // NOI18N
            tb_newEntry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_newEntry);

            tb_open.setAction(actionMap.get("openDocument")); // NOI18N
            tb_open.setText(resourceMap.getString("tb_open.text")); // NOI18N
            tb_open.setBorderPainted(false);
            tb_open.setFocusPainted(false);
            tb_open.setFocusable(false);
            tb_open.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_open.setName("tb_open"); // NOI18N
            tb_open.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_open);

            tb_save.setAction(actionMap.get("saveDocument")); // NOI18N
            tb_save.setText(resourceMap.getString("tb_save.text")); // NOI18N
            tb_save.setBorderPainted(false);
            tb_save.setFocusPainted(false);
            tb_save.setFocusable(false);
            tb_save.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_save.setName("tb_save"); // NOI18N
            tb_save.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_save);

            jSeparator4.setName("jSeparator4"); // NOI18N
            toolBar.add(jSeparator4);

            tb_edit.setAction(actionMap.get("editEntry")); // NOI18N
            tb_edit.setText(resourceMap.getString("tb_edit.text")); // NOI18N
            tb_edit.setBorderPainted(false);
            tb_edit.setFocusPainted(false);
            tb_edit.setFocusable(false);
            tb_edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_edit.setName("tb_edit"); // NOI18N
            tb_edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_edit);

            tb_delete.setAction(actionMap.get("deleteCurrentEntry")); // NOI18N
            tb_delete.setText(resourceMap.getString("tb_delete.text")); // NOI18N
            tb_delete.setBorderPainted(false);
            tb_delete.setFocusPainted(false);
            tb_delete.setFocusable(false);
            tb_delete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_delete.setName("tb_delete"); // NOI18N
            tb_delete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_delete);

            tb_copy.setAction(actionMap.get("copy"));
            tb_copy.setBorderPainted(false);
            tb_copy.setFocusPainted(false);
            tb_copy.setFocusable(false);
            tb_copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_copy.setName("tb_copy"); // NOI18N
            tb_copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_copy);

            tb_paste.setAction(actionMap.get("paste"));
            tb_paste.setBorderPainted(false);
            tb_paste.setFocusPainted(false);
            tb_paste.setFocusable(false);
            tb_paste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_paste.setName("tb_paste"); // NOI18N
            tb_paste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_paste);

            tb_selectall.setAction(actionMap.get("selectAllText")); // NOI18N
            tb_selectall.setText(resourceMap.getString("tb_selectall.text")); // NOI18N
            tb_selectall.setBorderPainted(false);
            tb_selectall.setFocusPainted(false);
            tb_selectall.setFocusable(false);
            tb_selectall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_selectall.setName("tb_selectall"); // NOI18N
            tb_selectall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_selectall);

            jSeparator5.setName("jSeparator5"); // NOI18N
            toolBar.add(jSeparator5);

            tb_addmanlinks.setAction(actionMap.get("manualInsertLinks")); // NOI18N
            tb_addmanlinks.setText(resourceMap.getString("tb_addmanlinks.text")); // NOI18N
            tb_addmanlinks.setBorderPainted(false);
            tb_addmanlinks.setFocusPainted(false);
            tb_addmanlinks.setFocusable(false);
            tb_addmanlinks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_addmanlinks.setName("tb_addmanlinks"); // NOI18N
            tb_addmanlinks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_addmanlinks);

            tb_addluhmann.setAction(actionMap.get("manualInsertEntry")); // NOI18N
            tb_addluhmann.setText(resourceMap.getString("tb_addluhmann.text")); // NOI18N
            tb_addluhmann.setBorderPainted(false);
            tb_addluhmann.setFocusPainted(false);
            tb_addluhmann.setFocusable(false);
            tb_addluhmann.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_addluhmann.setName("tb_addluhmann"); // NOI18N
            tb_addluhmann.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_addluhmann);

            tb_addbookmark.setAction(actionMap.get("addToBookmark")); // NOI18N
            tb_addbookmark.setText(resourceMap.getString("tb_addbookmark.text")); // NOI18N
            tb_addbookmark.setBorderPainted(false);
            tb_addbookmark.setFocusPainted(false);
            tb_addbookmark.setFocusable(false);
            tb_addbookmark.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_addbookmark.setName("tb_addbookmark"); // NOI18N
            tb_addbookmark.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_addbookmark);

            tb_addtodesktop.setAction(actionMap.get("addToDesktop")); // NOI18N
            tb_addtodesktop.setText(resourceMap.getString("tb_addtodesktop.text")); // NOI18N
            tb_addtodesktop.setBorderPainted(false);
            tb_addtodesktop.setFocusPainted(false);
            tb_addtodesktop.setFocusable(false);
            tb_addtodesktop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_addtodesktop.setName("tb_addtodesktop"); // NOI18N
            tb_addtodesktop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_addtodesktop);

            jSeparator10.setName("jSeparator10"); // NOI18N
            toolBar.add(jSeparator10);

            tb_find.setAction(actionMap.get("find")); // NOI18N
            tb_find.setText(resourceMap.getString("tb_find.text")); // NOI18N
            tb_find.setBorderPainted(false);
            tb_find.setFocusPainted(false);
            tb_find.setFocusable(false);
            tb_find.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_find.setName("tb_find"); // NOI18N
            tb_find.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_find);

            tb_first.setAction(actionMap.get("showFirstEntry")); // NOI18N
            tb_first.setText(resourceMap.getString("tb_first.text")); // NOI18N
            tb_first.setBorderPainted(false);
            tb_first.setFocusPainted(false);
            tb_first.setFocusable(false);
            tb_first.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_first.setName("tb_first"); // NOI18N
            tb_first.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_first);

            tb_prev.setAction(actionMap.get("showPrevEntry")); // NOI18N
            tb_prev.setText(resourceMap.getString("tb_prev.text")); // NOI18N
            tb_prev.setBorderPainted(false);
            tb_prev.setFocusPainted(false);
            tb_prev.setFocusable(false);
            tb_prev.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_prev.setName("tb_prev"); // NOI18N
            tb_prev.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_prev);

            tb_next.setAction(actionMap.get("showNextEntry")); // NOI18N
            tb_next.setText(resourceMap.getString("tb_next.text")); // NOI18N
            tb_next.setBorderPainted(false);
            tb_next.setFocusPainted(false);
            tb_next.setFocusable(false);
            tb_next.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_next.setName("tb_next"); // NOI18N
            tb_next.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_next);

            tb_last.setAction(actionMap.get("showLastEntry")); // NOI18N
            tb_last.setText(resourceMap.getString("tb_last.text")); // NOI18N
            tb_last.setBorderPainted(false);
            tb_last.setFocusPainted(false);
            tb_last.setFocusable(false);
            tb_last.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            tb_last.setName("tb_last"); // NOI18N
            tb_last.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            toolBar.add(tb_last);

            jSeparator32.setName("jSeparator32"); // NOI18N
            toolBar.add(jSeparator32);

            jLabelMemory.setName("jLabelMemory"); // NOI18N
            toolBar.add(jLabelMemory);

            jPopupMenuKeywords.setName("jPopupMenuKeywords"); // NOI18N

            popupKeywordsCopy.setAction(actionMap.get("copy"));
            popupKeywordsCopy.setName("popupKeywordsCopy"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsCopy);

            jSeparator8.setName("jSeparator8"); // NOI18N
            jPopupMenuKeywords.add(jSeparator8);

            popupKeywordsSearchOr.setAction(actionMap.get("searchLogOr")); // NOI18N
            popupKeywordsSearchOr.setName("popupKeywordsSearchOr"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsSearchOr);

            popupKeywordsSearchAnd.setAction(actionMap.get("searchLogAnd")); // NOI18N
            popupKeywordsSearchAnd.setName("popupKeywordsSearchAnd"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsSearchAnd);

            popupKeywordsSearchNot.setAction(actionMap.get("searchLogNot")); // NOI18N
            popupKeywordsSearchNot.setName("popupKeywordsSearchNot"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsSearchNot);

            jSeparator9.setName("jSeparator9"); // NOI18N
            jPopupMenuKeywords.add(jSeparator9);

            popupKeywordsNew.setAction(actionMap.get("newKeyword")); // NOI18N
            popupKeywordsNew.setName("popupKeywordsNew"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsNew);

            popupKeywordsEdit.setAction(actionMap.get("editKeyword")); // NOI18N
            popupKeywordsEdit.setName("popupKeywordsEdit"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsEdit);

            popupKeywordsDelete.setAction(actionMap.get("deleteKeyword")); // NOI18N
            popupKeywordsDelete.setName("popupKeywordsDelete"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsDelete);

            jSeparator7.setName("jSeparator7"); // NOI18N
            jPopupMenuKeywords.add(jSeparator7);

            popupKeywordsAddToList.setAction(actionMap.get("addKeywordToList")); // NOI18N
            popupKeywordsAddToList.setName("popupKeywordsAddToList"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsAddToList);

            jSeparator45.setName("jSeparator45"); // NOI18N
            jPopupMenuKeywords.add(jSeparator45);

            popupKeywordsManLinks.setAction(actionMap.get("addManLinksLogOr")); // NOI18N
            popupKeywordsManLinks.setName("popupKeywordsManLinks"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsManLinks);

            popupKeywordsManLinksAnd.setAction(actionMap.get("addManLinksLogAnd")); // NOI18N
            popupKeywordsManLinksAnd.setName("popupKeywordsManLinksAnd"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsManLinksAnd);

            jSeparator66.setName("jSeparator66"); // NOI18N
            jPopupMenuKeywords.add(jSeparator66);

            popupKeywordsLuhmann.setAction(actionMap.get("addLuhmannLogOr")); // NOI18N
            popupKeywordsLuhmann.setName("popupKeywordsLuhmann"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsLuhmann);

            popupKeywordsLuhmannAnd.setAction(actionMap.get("addLuhmannLogAnd")); // NOI18N
            popupKeywordsLuhmannAnd.setName("popupKeywordsLuhmannAnd"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsLuhmannAnd);

            jSeparator46.setName("jSeparator46"); // NOI18N
            jPopupMenuKeywords.add(jSeparator46);

            popupKeywordsDesktop.setAction(actionMap.get("addDesktopLogOr")); // NOI18N
            popupKeywordsDesktop.setName("popupKeywordsDesktop"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsDesktop);

            popupKeywordsDesktopAnd.setAction(actionMap.get("addDesktopLogAnd")); // NOI18N
            popupKeywordsDesktopAnd.setName("popupKeywordsDesktopAnd"); // NOI18N
            jPopupMenuKeywords.add(popupKeywordsDesktopAnd);

            jPopupMenuKeywordList.setName("jPopupMenuKeywordList"); // NOI18N

            popupKwListCopy.setAction(actionMap.get("copy"));
            popupKwListCopy.setName("popupKwListCopy"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListCopy);

            jSeparator89.setName("jSeparator89"); // NOI18N
            jPopupMenuKeywordList.add(jSeparator89);

            popupKwListSearchOr.setAction(actionMap.get("searchKeywordsFromListLogOr")); // NOI18N
            popupKwListSearchOr.setName("popupKwListSearchOr"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListSearchOr);

            popupKwListSearchAnd.setAction(actionMap.get("searchKeywordsFromListLogAnd")); // NOI18N
            popupKwListSearchAnd.setName("popupKwListSearchAnd"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListSearchAnd);

            popupKwListSearchNot.setAction(actionMap.get("searchKeywordsFromListLogNot")); // NOI18N
            popupKwListSearchNot.setName("popupKwListSearchNot"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListSearchNot);

            jSeparator13.setName("jSeparator13"); // NOI18N
            jPopupMenuKeywordList.add(jSeparator13);

            popupKwListHighlight.setAction(actionMap.get("highlightKeywords")); // NOI18N
            popupKwListHighlight.setName("popupKwListHighlight"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListHighlight);

            popupKwListHighlightSegments.setAction(actionMap.get("highlightSegments")); // NOI18N
            popupKwListHighlightSegments.setName("popupKwListHighlightSegments"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListHighlightSegments);

            popupKwListRefresh.setAction(actionMap.get("refreshFilteredLinks")); // NOI18N
            popupKwListRefresh.setName("popupKwListRefresh"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListRefresh);

            jSeparator11.setName("jSeparator11"); // NOI18N
            jPopupMenuKeywordList.add(jSeparator11);

            popupKwListLogOr.setAction(actionMap.get("keywordListLogOr")); // NOI18N
            popupKwListLogOr.setSelected(true);
            popupKwListLogOr.setName("popupKwListLogOr"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListLogOr);

            popupKwListLogAnd.setAction(actionMap.get("keywordListLogAnd")); // NOI18N
            popupKwListLogAnd.setSelected(true);
            popupKwListLogAnd.setName("popupKwListLogAnd"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListLogAnd);

            jSeparator12.setName("jSeparator12"); // NOI18N
            jPopupMenuKeywordList.add(jSeparator12);

            popupKwListDelete.setAction(actionMap.get("deleteKeywordFromEntry")); // NOI18N
            popupKwListDelete.setName("popupKwListDelete"); // NOI18N
            jPopupMenuKeywordList.add(popupKwListDelete);

            jPopupMenuAuthors.setName("jPopupMenuAuthors"); // NOI18N

            popupAuthorsCopy.setAction(actionMap.get("copy"));
            popupAuthorsCopy.setName("popupAuthorsCopy"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsCopy);

            jSeparator14.setName("jSeparator14"); // NOI18N
            jPopupMenuAuthors.add(jSeparator14);

            popupAuthorsSearchLogOr.setAction(actionMap.get("searchLogOr")); // NOI18N
            popupAuthorsSearchLogOr.setName("popupAuthorsSearchLogOr"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsSearchLogOr);

            popupAuthorsSearchLogAnd.setAction(actionMap.get("searchLogAnd")); // NOI18N
            popupAuthorsSearchLogAnd.setName("popupAuthorsSearchLogAnd"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsSearchLogAnd);

            popupAuthorsSearchLogNot.setAction(actionMap.get("searchLogNot")); // NOI18N
            popupAuthorsSearchLogNot.setName("popupAuthorsSearchLogNot"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsSearchLogNot);

            jSeparator15.setName("jSeparator15"); // NOI18N
            jPopupMenuAuthors.add(jSeparator15);

            popupAuthorsNew.setAction(actionMap.get("newAuthor")); // NOI18N
            popupAuthorsNew.setName("popupAuthorsNew"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsNew);

            popupAuthorsEdit.setAction(actionMap.get("editAuthor")); // NOI18N
            popupAuthorsEdit.setName("popupAuthorsEdit"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsEdit);

            popupAuthorsDelete.setAction(actionMap.get("deleteAuthor")); // NOI18N
            popupAuthorsDelete.setName("popupAuthorsDelete"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsDelete);

            jSeparator91.setName("jSeparator91"); // NOI18N
            jPopupMenuAuthors.add(jSeparator91);

            popupAuthorsBibkey.setAction(actionMap.get("changeBibkey")); // NOI18N
            popupAuthorsBibkey.setName("popupAuthorsBibkey"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsBibkey);

            jSeparator16.setName("jSeparator16"); // NOI18N
            jPopupMenuAuthors.add(jSeparator16);

            popupAuthorsAddToEntry.setAction(actionMap.get("addAuthorToList")); // NOI18N
            popupAuthorsAddToEntry.setName("popupAuthorsAddToEntry"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsAddToEntry);

            jSeparator49.setName("jSeparator49"); // NOI18N
            jPopupMenuAuthors.add(jSeparator49);

            popupAuthorsSubAdd.setText(resourceMap.getString("popupAuthorsSubAdd.text")); // NOI18N
            popupAuthorsSubAdd.setName("popupAuthorsSubAdd"); // NOI18N

            popupAuthorsManLinks.setAction(actionMap.get("addManLinksLogOr")); // NOI18N
            popupAuthorsManLinks.setName("popupAuthorsManLinks"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsManLinks);

            popupAuthorsManLinksAnd.setAction(actionMap.get("addManLinksLogAnd")); // NOI18N
            popupAuthorsManLinksAnd.setName("popupAuthorsManLinksAnd"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsManLinksAnd);

            jSeparator70.setName("jSeparator70"); // NOI18N
            popupAuthorsSubAdd.add(jSeparator70);

            popupAuthorsLuhmann.setAction(actionMap.get("addLuhmannLogOr")); // NOI18N
            popupAuthorsLuhmann.setName("popupAuthorsLuhmann"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsLuhmann);

            popupAuthorsLuhmannAnd.setAction(actionMap.get("addLuhmannLogAnd")); // NOI18N
            popupAuthorsLuhmannAnd.setName("popupAuthorsLuhmannAnd"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsLuhmannAnd);

            jSeparator50.setName("jSeparator50"); // NOI18N
            popupAuthorsSubAdd.add(jSeparator50);

            popupAuthorsDesktop.setAction(actionMap.get("addDesktopLogOr")); // NOI18N
            popupAuthorsDesktop.setName("popupAuthorsDesktop"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsDesktop);

            popupAuthorsDesktopAnd.setAction(actionMap.get("addDesktopLogAnd")); // NOI18N
            popupAuthorsDesktopAnd.setName("popupAuthorsDesktopAnd"); // NOI18N
            popupAuthorsSubAdd.add(popupAuthorsDesktopAnd);

            jPopupMenuAuthors.add(popupAuthorsSubAdd);

            jSeparator96.setName("jSeparator96"); // NOI18N
            jPopupMenuAuthors.add(jSeparator96);

            popupAuthorsImport.setAction(actionMap.get("importAuthors")); // NOI18N
            popupAuthorsImport.setName("popupAuthorsImport"); // NOI18N
            jPopupMenuAuthors.add(popupAuthorsImport);

            jPopupMenuLuhmann.setName("jPopupMenuLuhmann"); // NOI18N

            popupLuhmannAdd.setAction(actionMap.get("manualInsertEntry")); // NOI18N
            popupLuhmannAdd.setName("popupLuhmannAdd"); // NOI18N
            jPopupMenuLuhmann.add(popupLuhmannAdd);

            jSeparator17.setName("jSeparator17"); // NOI18N
            jPopupMenuLuhmann.add(jSeparator17);

            popupLuhmannDelete.setAction(actionMap.get("deleteLuhmannFromEntry")); // NOI18N
            popupLuhmannDelete.setName("popupLuhmannDelete"); // NOI18N
            jPopupMenuLuhmann.add(popupLuhmannDelete);

            jSeparator60.setName("jSeparator60"); // NOI18N
            jPopupMenuLuhmann.add(jSeparator60);

            popupLuhmannManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            popupLuhmannManLinks.setName("popupLuhmannManLinks"); // NOI18N
            jPopupMenuLuhmann.add(popupLuhmannManLinks);

            popupLuhmannBookmarks.setAction(actionMap.get("addBookmarks")); // NOI18N
            popupLuhmannBookmarks.setName("popupLuhmannBookmarks"); // NOI18N
            jPopupMenuLuhmann.add(popupLuhmannBookmarks);

            jSeparator63.setName("jSeparator63"); // NOI18N
            jPopupMenuLuhmann.add(jSeparator63);

            popupLuhmannDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            popupLuhmannDesktop.setName("popupLuhmannDesktop"); // NOI18N
            jPopupMenuLuhmann.add(popupLuhmannDesktop);

            jSeparator117.setName("jSeparator117"); // NOI18N
            jPopupMenuLuhmann.add(jSeparator117);

            popupLuhmannSetLevel.setText(resourceMap.getString("popupLuhmannSetLevel.text")); // NOI18N
            popupLuhmannSetLevel.setName("popupLuhmannSetLevel"); // NOI18N

            popupLuhmannLevelAll.setAction(actionMap.get("setLuhmannLevelAll")); // NOI18N
            popupLuhmannLevelAll.setName("popupLuhmannLevelAll"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevelAll);

            jSeparator74.setName("jSeparator74"); // NOI18N
            popupLuhmannSetLevel.add(jSeparator74);

            popupLuhmannLevel1.setAction(actionMap.get("setLuhmannLevel1")); // NOI18N
            popupLuhmannLevel1.setName("popupLuhmannLevel1"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevel1);

            popupLuhmannLevel2.setAction(actionMap.get("setLuhmannLevel2")); // NOI18N
            popupLuhmannLevel2.setName("popupLuhmannLevel2"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevel2);

            popupLuhmannLevel3.setAction(actionMap.get("setLuhmannLevel3")); // NOI18N
            popupLuhmannLevel3.setName("popupLuhmannLevel3"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevel3);

            popupLuhmannLevel4.setAction(actionMap.get("setLuhmannLevel4")); // NOI18N
            popupLuhmannLevel4.setName("popupLuhmannLevel4"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevel4);

            popupLuhmannLevel5.setAction(actionMap.get("setLuhmannLevel5")); // NOI18N
            popupLuhmannLevel5.setName("popupLuhmannLevel5"); // NOI18N
            popupLuhmannSetLevel.add(popupLuhmannLevel5);

            jPopupMenuLuhmann.add(popupLuhmannSetLevel);

            jPopupMenuTitles.setName("jPopupMenuTitles"); // NOI18N

            popupTitlesCopy.setAction(actionMap.get("copy"));
            popupTitlesCopy.setName("popupTitlesCopy"); // NOI18N
            jPopupMenuTitles.add(popupTitlesCopy);

            jSeparator20.setName("jSeparator20"); // NOI18N
            jPopupMenuTitles.add(jSeparator20);

            popupTitlesEdit.setAction(actionMap.get("editTitle")); // NOI18N
            popupTitlesEdit.setName("popupTitlesEdit"); // NOI18N
            jPopupMenuTitles.add(popupTitlesEdit);

            popupTitlesEditEntry.setAction(actionMap.get("editEntry")); // NOI18N
            popupTitlesEditEntry.setName("popupTitlesEditEntry"); // NOI18N
            jPopupMenuTitles.add(popupTitlesEditEntry);

            jSeparator103.setName("jSeparator103"); // NOI18N
            jPopupMenuTitles.add(jSeparator103);

            popupTitlesDelete.setAction(actionMap.get("deleteEntry")); // NOI18N
            popupTitlesDelete.setName("popupTitlesDelete"); // NOI18N
            jPopupMenuTitles.add(popupTitlesDelete);

            jSeparator114.setName("jSeparator114"); // NOI18N
            jPopupMenuTitles.add(jSeparator114);

            popupTitlesAutomaticTitle.setAction(actionMap.get("automaticFirstLineAsTitle")); // NOI18N
            popupTitlesAutomaticTitle.setName("popupTitlesAutomaticTitle"); // NOI18N
            jPopupMenuTitles.add(popupTitlesAutomaticTitle);

            jSeparator21.setName("jSeparator21"); // NOI18N
            jPopupMenuTitles.add(jSeparator21);

            popupTitlesManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            popupTitlesManLinks.setName("popupTitlesManLinks"); // NOI18N
            jPopupMenuTitles.add(popupTitlesManLinks);

            popupTitlesLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            popupTitlesLuhmann.setName("popupTitlesLuhmann"); // NOI18N
            jPopupMenuTitles.add(popupTitlesLuhmann);

            popupTitlesBookmarks.setAction(actionMap.get("addBookmarks")); // NOI18N
            popupTitlesBookmarks.setName("popupTitlesBookmarks"); // NOI18N
            jPopupMenuTitles.add(popupTitlesBookmarks);

            jSeparator64.setName("jSeparator64"); // NOI18N
            jPopupMenuTitles.add(jSeparator64);

            popupTitlesDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            popupTitlesDesktop.setName("popupTitlesDesktop"); // NOI18N
            jPopupMenuTitles.add(popupTitlesDesktop);

            jPopupMenuBookmarks.setName("jPopupMenuBookmarks"); // NOI18N

            popupBookmarksEdit.setAction(actionMap.get("editBookmark")); // NOI18N
            popupBookmarksEdit.setName("popupBookmarksEdit"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksEdit);

            popupBookmarksDelete.setAction(actionMap.get("deleteBookmark")); // NOI18N
            popupBookmarksDelete.setName("popupBookmarksDelete"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksDelete);

            jSeparator36.setName("jSeparator36"); // NOI18N
            jPopupMenuBookmarks.add(jSeparator36);

            popupBookmarksEditCat.setAction(actionMap.get("editBookmarkCategory")); // NOI18N
            popupBookmarksEditCat.setName("popupBookmarksEditCat"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksEditCat);

            popupBookmarksDeleteCat.setAction(actionMap.get("deleteBookmarkCategory")); // NOI18N
            popupBookmarksDeleteCat.setName("popupBookmarksDeleteCat"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksDeleteCat);

            jSeparator38.setName("jSeparator38"); // NOI18N
            jPopupMenuBookmarks.add(jSeparator38);

            popupBookmarksAddManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            popupBookmarksAddManLinks.setName("popupBookmarksAddManLinks"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksAddManLinks);

            popupBookmarksAddLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            popupBookmarksAddLuhmann.setName("popupBookmarksAddLuhmann"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarksAddLuhmann);

            jSeparator56.setName("jSeparator56"); // NOI18N
            jPopupMenuBookmarks.add(jSeparator56);

            popupBookmarkAddDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            popupBookmarkAddDesktop.setName("popupBookmarkAddDesktop"); // NOI18N
            jPopupMenuBookmarks.add(popupBookmarkAddDesktop);

            jPopupMenuLinks.setName("jPopupMenuLinks"); // NOI18N

            popupLinksRefresh.setAction(actionMap.get("refreshFilteredLinks")); // NOI18N
            popupLinksRefresh.setName("popupLinksRefresh"); // NOI18N
            jPopupMenuLinks.add(popupLinksRefresh);

            jSeparator115.setName("jSeparator115"); // NOI18N
            jPopupMenuLinks.add(jSeparator115);

            popupLinkRemoveManLink.setAction(actionMap.get("deleteManualLink")); // NOI18N
            popupLinkRemoveManLink.setName("popupLinkRemoveManLink"); // NOI18N
            jPopupMenuLinks.add(popupLinkRemoveManLink);

            jSeparator54.setName("jSeparator54"); // NOI18N
            jPopupMenuLinks.add(jSeparator54);

            popupLinksManLinks.setAction(actionMap.get("addManLinks")); // NOI18N
            popupLinksManLinks.setName("popupLinksManLinks"); // NOI18N
            jPopupMenuLinks.add(popupLinksManLinks);

            popupLinksLuhmann.setAction(actionMap.get("addLuhmann")); // NOI18N
            popupLinksLuhmann.setName("popupLinksLuhmann"); // NOI18N
            jPopupMenuLinks.add(popupLinksLuhmann);

            jSeparator57.setName("jSeparator57"); // NOI18N
            jPopupMenuLinks.add(jSeparator57);

            popupLinksDesktop.setAction(actionMap.get("addDesktop")); // NOI18N
            popupLinksDesktop.setName("popupLinksDesktop"); // NOI18N
            jPopupMenuLinks.add(popupLinksDesktop);

            jPopupMenuAttachments.setName("jPopupMenuAttachments"); // NOI18N

            popupAttachmentsCopy.setAction(actionMap.get("copy"));
            popupAttachmentsCopy.setName("popupAttachmentsCopy"); // NOI18N
            jPopupMenuAttachments.add(popupAttachmentsCopy);

            jSeparator87.setName("jSeparator87"); // NOI18N
            jPopupMenuAttachments.add(jSeparator87);

            popupAttachmentsEdit.setAction(actionMap.get("editAttachment")); // NOI18N
            popupAttachmentsEdit.setName("popupAttachmentsEdit"); // NOI18N
            jPopupMenuAttachments.add(popupAttachmentsEdit);

            popupAttachmentsDelete.setAction(actionMap.get("deleteAttachment")); // NOI18N
            popupAttachmentsDelete.setName("popupAttachmentsDelete"); // NOI18N
            jPopupMenuAttachments.add(popupAttachmentsDelete);

            jSeparator94.setName("jSeparator94"); // NOI18N
            jPopupMenuAttachments.add(jSeparator94);

            popupAttachmentsGoto.setAction(actionMap.get("openAttachmentDirectory")); // NOI18N
            popupAttachmentsGoto.setName("popupAttachmentsGoto"); // NOI18N
            jPopupMenuAttachments.add(popupAttachmentsGoto);

            jSeparator86.setName("jSeparator86"); // NOI18N
            jPopupMenuAttachments.add(jSeparator86);

            popupAttachmentsExport.setAction(actionMap.get("exportAttachments")); // NOI18N
            popupAttachmentsExport.setName("popupAttachmentsExport"); // NOI18N
            jPopupMenuAttachments.add(popupAttachmentsExport);

            jPopupMenuMain.setName("jPopupMenuMain"); // NOI18N

            popupMainCopy.setAction(actionMap.get("copy"));
            popupMainCopy.setName("popupMainCopy"); // NOI18N
            jPopupMenuMain.add(popupMainCopy);

            popupMainCopyPlain.setAction(actionMap.get("copyPlain")); // NOI18N
            popupMainCopyPlain.setName("popupMainCopyPlain"); // NOI18N
            jPopupMenuMain.add(popupMainCopyPlain);

            jSeparator88.setName("jSeparator88"); // NOI18N
            jPopupMenuMain.add(jSeparator88);

            popupMainFind.setAction(actionMap.get("findFromSelection")); // NOI18N
            popupMainFind.setName("popupMainFind"); // NOI18N
            jPopupMenuMain.add(popupMainFind);

            jSeparator97.setName("jSeparator97"); // NOI18N
            jPopupMenuMain.add(jSeparator97);

            popupMainAddToKeyword.setAction(actionMap.get("addToKeywordList")); // NOI18N
            popupMainAddToKeyword.setName("popupMainAddToKeyword"); // NOI18N
            jPopupMenuMain.add(popupMainAddToKeyword);

            jSeparator98.setName("jSeparator98"); // NOI18N
            jPopupMenuMain.add(jSeparator98);

            popupMainSetFirstLineAsTitle.setAction(actionMap.get("setFirstLineAsTitle")); // NOI18N
            popupMainSetFirstLineAsTitle.setName("popupMainSetFirstLineAsTitle"); // NOI18N
            jPopupMenuMain.add(popupMainSetFirstLineAsTitle);

            popupMainSetSelectionAsTitle.setAction(actionMap.get("setSelectionAsTitle")); // NOI18N
            popupMainSetSelectionAsTitle.setName("popupMainSetSelectionAsTitle"); // NOI18N
            jPopupMenuMain.add(popupMainSetSelectionAsTitle);

            setComponent(mainPanel);
            setMenuBar(menuBar);
            setStatusBar(statusPanel);
            setToolBar(toolBar);
        }// </editor-fold>//GEN-END:initComponents

	private void viewAuthorsCopyActionPerformed(ActionEvent evt) {// GEN-FIRST:event_viewAuthorsCopyActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_viewAuthorsCopyActionPerformed

	/**
	 * This event catches mouse-clicks which occur when the user clicks a hyperlink
	 * in the main editor-pane. First has to be checked, whether the clicked
	 * hyperlink was an web-url or links to a local file. Then the url or file will
	 * be opened
	 *
	 * @param evt
	 */

	/**
	 * Enables and disables the menu items for the popupMenuKeywordList and
	 * viewMenuLinks
	 */
	private void initViewMenuLinks() {
		// check the user settings. when the filtering of links (jTableLinks) is set to
		// logical-or,
		// select the appropriate item, else check the log-and-item
		if (settings.getLogKeywordlist().equalsIgnoreCase(Settings.SETTING_LOGKEYWORDLIST_OR)) {
			popupKwListLogOr.setSelected(true);
			popupKwListLogAnd.setSelected(false);
			viewMenuLinksKwListLogOr.setSelected(true);
			viewMenuLinksKwListLogAnd.setSelected(false);
		} else {
			popupKwListLogOr.setSelected(false);
			popupKwListLogAnd.setSelected(true);
			viewMenuLinksKwListLogOr.setSelected(false);
			viewMenuLinksKwListLogAnd.setSelected(true);
		}
		// set indicator which show whether we have selections or not
		setListFilledWithEntry(!jListEntryKeywords.getSelectedValuesList().isEmpty());
		setExportPossible(jTableLinks.getRowCount() > 0 || jTableManLinks.getRowCount() > 0);
		setTableEntriesSelected((jTableLinks.getSelectedRowCount() > 0) || (jTableManLinks.getSelectedRowCount() > 0));
		// show refresh links only when link-list (jTableLinks) are visible
		popupKwListRefresh.setEnabled(TAB_LINKS == jTabbedPaneMain.getSelectedIndex());
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem addFirstLineToTitleMenuItem;
    private javax.swing.JMenuItem addSelectionToKeywordMenuItem;
    private javax.swing.JMenuItem addSelectionToTitleMenuItem;
    private javax.swing.JMenuItem addToDesktopMenuItem;
    private javax.swing.JButton buttonHistoryBack;
    private javax.swing.JButton buttonHistoryFore;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem copyPlainMenuItem;
    private javax.swing.JMenuItem deleteKwFromListMenuItem;
    private javax.swing.JMenuItem deleteZettelMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findDoubleEntriesItem;
    private javax.swing.JMenuItem findEntriesAnyLuhmann;
    private javax.swing.JMenuItem findEntriesFromCreatedTimestamp;
    private javax.swing.JMenuItem findEntriesFromEditedTimestamp;
    private javax.swing.JMenuItem findEntriesTopLevelLuhmann;
    private javax.swing.JMenuItem findEntriesWithAttachments;
    private javax.swing.JMenuItem findEntriesWithRatings;
    private javax.swing.JMenuItem findEntriesWithRemarks;
    private javax.swing.JMenuItem findEntriesWithoutAuthors;
    private javax.swing.JMenuItem findEntriesWithoutKeywords;
    private javax.swing.JMenuItem findEntriesWithoutManualLinks;
    private javax.swing.JMenuItem findEntriesWithoutRatings;
    private javax.swing.JMenuItem findEntriesWithoutRemarks;
    private javax.swing.JMenu findEntryKeywordsMenu;
    private javax.swing.JMenu findEntryWithout;
    private javax.swing.JMenu findMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenuItem findReplaceMenuItem;
    private javax.swing.JMenuItem goToFirstParentEntryMenuItem;
    private javax.swing.JMenuItem gotoEntryMenuItem;
    private javax.swing.JCheckBoxMenuItem highlightSegmentsMenuItem;
    private javax.swing.JMenuItem historyForMenuItem;
    private javax.swing.JMenuItem histroyBackMenuItem;
    private javax.swing.JMenuItem homeMenuItem;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JMenuItem insertEntryMenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonRefreshAttachments;
    private javax.swing.JButton jButtonRefreshAuthors;
    private javax.swing.JButton jButtonRefreshCluster;
    private javax.swing.JButton jButtonRefreshKeywords;
    private javax.swing.JButton jButtonRefreshTitles;
    private javax.swing.JCheckBox jCheckBoxCluster;
    private javax.swing.JCheckBox jCheckBoxShowAllLuhmann;
    private javax.swing.JCheckBox jCheckBoxShowSynonyms;
    private javax.swing.JComboBox jComboBoxAuthorType;
    private javax.swing.JComboBox jComboBoxBookmarkCategory;
    private javax.swing.JEditorPane jEditorPaneBookmarkComment;
    private javax.swing.JEditorPane jEditorPaneClusterEntries;
    private javax.swing.JEditorPane jEditorPaneDispAuthor;
    private javax.swing.JEditorPane jEditorPaneEntry;
    private javax.swing.JEditorPane jEditorPaneIsFollower;
    private javax.swing.JLabel jLabelMemory;
    private javax.swing.JList jListEntryKeywords;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelDispAuthor;
    private javax.swing.JPanel jPanelLiveSearch;
    private javax.swing.JPanel jPanelMainRight;
    private javax.swing.JPanel jPanelManLinks;
    private javax.swing.JPopupMenu jPopupMenuAttachments;
    private javax.swing.JPopupMenu jPopupMenuAuthors;
    private javax.swing.JPopupMenu jPopupMenuBookmarks;
    private javax.swing.JPopupMenu jPopupMenuKeywordList;
    private javax.swing.JPopupMenu jPopupMenuKeywords;
    private javax.swing.JPopupMenu jPopupMenuLinks;
    private javax.swing.JPopupMenu jPopupMenuLuhmann;
    private javax.swing.JPopupMenu jPopupMenuMain;
    private javax.swing.JPopupMenu jPopupMenuTitles;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JSeparator jSeparator100;
    private javax.swing.JPopupMenu.Separator jSeparator101;
    private javax.swing.JSeparator jSeparator102;
    private javax.swing.JSeparator jSeparator103;
    private javax.swing.JSeparator jSeparator104;
    private javax.swing.JSeparator jSeparator105;
    private javax.swing.JPopupMenu.Separator jSeparator106;
    private javax.swing.JSeparator jSeparator107;
    private javax.swing.JPopupMenu.Separator jSeparator108;
    private javax.swing.JSeparator jSeparator109;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator110;
    private javax.swing.JPopupMenu.Separator jSeparator111;
    private javax.swing.JPopupMenu.Separator jSeparator112;
    private javax.swing.JPopupMenu.Separator jSeparator113;
    private javax.swing.JPopupMenu.Separator jSeparator114;
    private javax.swing.JPopupMenu.Separator jSeparator115;
    private javax.swing.JPopupMenu.Separator jSeparator116;
    private javax.swing.JPopupMenu.Separator jSeparator117;
    private javax.swing.JPopupMenu.Separator jSeparator118;
    private javax.swing.JPopupMenu.Separator jSeparator119;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
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
    private javax.swing.JToolBar.Separator jSeparator32;
    private javax.swing.JSeparator jSeparator33;
    private javax.swing.JPopupMenu.Separator jSeparator34;
    private javax.swing.JSeparator jSeparator35;
    private javax.swing.JSeparator jSeparator36;
    private javax.swing.JSeparator jSeparator37;
    private javax.swing.JSeparator jSeparator38;
    private javax.swing.JSeparator jSeparator39;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator40;
    private javax.swing.JSeparator jSeparator41;
    private javax.swing.JSeparator jSeparator42;
    private javax.swing.JSeparator jSeparator43;
    private javax.swing.JSeparator jSeparator44;
    private javax.swing.JSeparator jSeparator45;
    private javax.swing.JSeparator jSeparator46;
    private javax.swing.JSeparator jSeparator47;
    private javax.swing.JSeparator jSeparator48;
    private javax.swing.JSeparator jSeparator49;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator50;
    private javax.swing.JSeparator jSeparator51;
    private javax.swing.JSeparator jSeparator52;
    private javax.swing.JSeparator jSeparator53;
    private javax.swing.JSeparator jSeparator54;
    private javax.swing.JSeparator jSeparator55;
    private javax.swing.JSeparator jSeparator56;
    private javax.swing.JSeparator jSeparator57;
    private javax.swing.JSeparator jSeparator58;
    private javax.swing.JSeparator jSeparator59;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator60;
    private javax.swing.JSeparator jSeparator61;
    private javax.swing.JSeparator jSeparator62;
    private javax.swing.JSeparator jSeparator63;
    private javax.swing.JSeparator jSeparator64;
    private javax.swing.JPopupMenu.Separator jSeparator65;
    private javax.swing.JSeparator jSeparator66;
    private javax.swing.JSeparator jSeparator67;
    private javax.swing.JSeparator jSeparator68;
    private javax.swing.JSeparator jSeparator69;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator70;
    private javax.swing.JSeparator jSeparator71;
    private javax.swing.JPopupMenu.Separator jSeparator72;
    private javax.swing.JPopupMenu.Separator jSeparator73;
    private javax.swing.JPopupMenu.Separator jSeparator74;
    private javax.swing.JSeparator jSeparator75;
    private javax.swing.JSeparator jSeparator76;
    private javax.swing.JSeparator jSeparator77;
    private javax.swing.JSeparator jSeparator78;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator80;
    private javax.swing.JSeparator jSeparator81;
    private javax.swing.JSeparator jSeparator82;
    private javax.swing.JSeparator jSeparator83;
    private javax.swing.JSeparator jSeparator84;
    private javax.swing.JSeparator jSeparator85;
    private javax.swing.JSeparator jSeparator86;
    private javax.swing.JSeparator jSeparator87;
    private javax.swing.JSeparator jSeparator88;
    private javax.swing.JSeparator jSeparator89;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSeparator jSeparator90;
    private javax.swing.JSeparator jSeparator91;
    private javax.swing.JSeparator jSeparator92;
    private javax.swing.JSeparator jSeparator93;
    private javax.swing.JSeparator jSeparator94;
    private javax.swing.JSeparator jSeparator95;
    private javax.swing.JSeparator jSeparator96;
    private javax.swing.JSeparator jSeparator97;
    private javax.swing.JSeparator jSeparator98;
    private javax.swing.JSeparator jSeparator99;
    private javax.swing.JSeparator jSeparatorAbout1;
    private javax.swing.JSeparator jSeparatorExit;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPaneAuthors;
    private javax.swing.JSplitPane jSplitPaneLinks;
    private javax.swing.JSplitPane jSplitPaneMain1;
    private javax.swing.JSplitPane jSplitPaneMain2;
    private javax.swing.JTabbedPane jTabbedPaneMain;
    private javax.swing.JTable jTableAttachments;
    private javax.swing.JTable jTableAuthors;
    private javax.swing.JTable jTableBookmarks;
    private javax.swing.JTable jTableKeywords;
    private javax.swing.JTable jTableLinks;
    private javax.swing.JTable jTableManLinks;
    private javax.swing.JTable jTableTitles;
    private javax.swing.JTextField jTextFieldEntryNumber;
    private javax.swing.JTextField jTextFieldFilterAttachments;
    private javax.swing.JTextField jTextFieldFilterAuthors;
    private javax.swing.JTextField jTextFieldFilterCluster;
    private javax.swing.JTextField jTextFieldFilterKeywords;
    private javax.swing.JTextField jTextFieldFilterTitles;
    private javax.swing.JTextField jTextFieldLiveSearch;
    private javax.swing.JTree jTreeCluster;
    private javax.swing.JTree jTreeKeywords;
    private javax.swing.JTree jTreeLuhmann;
    private javax.swing.JMenuItem lastEntryMenuItem;
    private javax.swing.JMenuItem liveSearchMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem manualInsertLinksMenuItem;
    private javax.swing.JMenuItem manualInsertMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuFileInformation;
    private javax.swing.JMenuItem menuKwListSearchAnd;
    private javax.swing.JMenuItem menuKwListSearchNot;
    private javax.swing.JMenuItem menuKwListSearchOr;
    private javax.swing.JMenuItem newDesktopMenuItem;
    private javax.swing.JMenuItem newEntryMenuItem;
    private javax.swing.JMenuItem newZettelkastenMenuItem;
    private javax.swing.JMenuItem nextEntryMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem popupAttachmentsCopy;
    private javax.swing.JMenuItem popupAttachmentsDelete;
    private javax.swing.JMenuItem popupAttachmentsEdit;
    private javax.swing.JMenuItem popupAttachmentsExport;
    private javax.swing.JMenuItem popupAttachmentsGoto;
    private javax.swing.JMenuItem popupAuthorsAddToEntry;
    private javax.swing.JMenuItem popupAuthorsBibkey;
    private javax.swing.JMenuItem popupAuthorsCopy;
    private javax.swing.JMenuItem popupAuthorsDelete;
    private javax.swing.JMenuItem popupAuthorsDesktop;
    private javax.swing.JMenuItem popupAuthorsDesktopAnd;
    private javax.swing.JMenuItem popupAuthorsEdit;
    private javax.swing.JMenuItem popupAuthorsImport;
    private javax.swing.JMenuItem popupAuthorsLuhmann;
    private javax.swing.JMenuItem popupAuthorsLuhmannAnd;
    private javax.swing.JMenuItem popupAuthorsManLinks;
    private javax.swing.JMenuItem popupAuthorsManLinksAnd;
    private javax.swing.JMenuItem popupAuthorsNew;
    private javax.swing.JMenuItem popupAuthorsSearchLogAnd;
    private javax.swing.JMenuItem popupAuthorsSearchLogNot;
    private javax.swing.JMenuItem popupAuthorsSearchLogOr;
    private javax.swing.JMenu popupAuthorsSubAdd;
    private javax.swing.JMenuItem popupBookmarkAddDesktop;
    private javax.swing.JMenuItem popupBookmarksAddLuhmann;
    private javax.swing.JMenuItem popupBookmarksAddManLinks;
    private javax.swing.JMenuItem popupBookmarksDelete;
    private javax.swing.JMenuItem popupBookmarksDeleteCat;
    private javax.swing.JMenuItem popupBookmarksEdit;
    private javax.swing.JMenuItem popupBookmarksEditCat;
    private javax.swing.JMenuItem popupKeywordsAddToList;
    private javax.swing.JMenuItem popupKeywordsCopy;
    private javax.swing.JMenuItem popupKeywordsDelete;
    private javax.swing.JMenuItem popupKeywordsDesktop;
    private javax.swing.JMenuItem popupKeywordsDesktopAnd;
    private javax.swing.JMenuItem popupKeywordsEdit;
    private javax.swing.JMenuItem popupKeywordsLuhmann;
    private javax.swing.JMenuItem popupKeywordsLuhmannAnd;
    private javax.swing.JMenuItem popupKeywordsManLinks;
    private javax.swing.JMenuItem popupKeywordsManLinksAnd;
    private javax.swing.JMenuItem popupKeywordsNew;
    private javax.swing.JMenuItem popupKeywordsSearchAnd;
    private javax.swing.JMenuItem popupKeywordsSearchNot;
    private javax.swing.JMenuItem popupKeywordsSearchOr;
    private javax.swing.JMenuItem popupKwListCopy;
    private javax.swing.JMenuItem popupKwListDelete;
    private javax.swing.JMenuItem popupKwListHighlight;
    private javax.swing.JCheckBoxMenuItem popupKwListHighlightSegments;
    private javax.swing.JCheckBoxMenuItem popupKwListLogAnd;
    private javax.swing.JCheckBoxMenuItem popupKwListLogOr;
    private javax.swing.JMenuItem popupKwListRefresh;
    private javax.swing.JMenuItem popupKwListSearchAnd;
    private javax.swing.JMenuItem popupKwListSearchNot;
    private javax.swing.JMenuItem popupKwListSearchOr;
    private javax.swing.JMenuItem popupLinkRemoveManLink;
    private javax.swing.JMenuItem popupLinksDesktop;
    private javax.swing.JMenuItem popupLinksLuhmann;
    private javax.swing.JMenuItem popupLinksManLinks;
    private javax.swing.JMenuItem popupLinksRefresh;
    private javax.swing.JMenuItem popupLuhmannAdd;
    private javax.swing.JMenuItem popupLuhmannBookmarks;
    private javax.swing.JMenuItem popupLuhmannDelete;
    private javax.swing.JMenuItem popupLuhmannDesktop;
    private javax.swing.JMenuItem popupLuhmannLevel1;
    private javax.swing.JMenuItem popupLuhmannLevel2;
    private javax.swing.JMenuItem popupLuhmannLevel3;
    private javax.swing.JMenuItem popupLuhmannLevel4;
    private javax.swing.JMenuItem popupLuhmannLevel5;
    private javax.swing.JMenuItem popupLuhmannLevelAll;
    private javax.swing.JMenuItem popupLuhmannManLinks;
    private javax.swing.JMenu popupLuhmannSetLevel;
    private javax.swing.JMenuItem popupMainAddToKeyword;
    private javax.swing.JMenuItem popupMainCopy;
    private javax.swing.JMenuItem popupMainCopyPlain;
    private javax.swing.JMenuItem popupMainFind;
    private javax.swing.JMenuItem popupMainSetFirstLineAsTitle;
    private javax.swing.JMenuItem popupMainSetSelectionAsTitle;
    private javax.swing.JMenuItem popupTitlesAutomaticTitle;
    private javax.swing.JMenuItem popupTitlesBookmarks;
    private javax.swing.JMenuItem popupTitlesCopy;
    private javax.swing.JMenuItem popupTitlesDelete;
    private javax.swing.JMenuItem popupTitlesDesktop;
    private javax.swing.JMenuItem popupTitlesEdit;
    private javax.swing.JMenuItem popupTitlesEditEntry;
    private javax.swing.JMenuItem popupTitlesLuhmann;
    private javax.swing.JMenuItem popupTitlesManLinks;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JMenuItem prevEntryMenuItem;
    private javax.swing.JMenuItem quickNewEntryMenuItem;
    private javax.swing.JMenuItem quickNewTitleEntryMenuItem;
    private javax.swing.JMenuItem randomEntryMenuItem;
    private javax.swing.JMenuItem recentDoc1;
    private javax.swing.JMenuItem recentDoc2;
    private javax.swing.JMenuItem recentDoc3;
    private javax.swing.JMenuItem recentDoc4;
    private javax.swing.JMenuItem recentDoc5;
    private javax.swing.JMenuItem recentDoc6;
    private javax.swing.JMenuItem recentDoc7;
    private javax.swing.JMenuItem recentDoc8;
    private javax.swing.JMenu recentDocsSubMenu;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem setBookmarkMenuItem;
    private javax.swing.JMenuItem showAttachmentsMenuItem;
    private javax.swing.JMenuItem showAuthorsMenuItem;
    private javax.swing.JMenuItem showBookmarksMenuItem;
    private javax.swing.JMenuItem showClusterMenuItem;
    private javax.swing.JMenuItem showCurrentEntryAgain;
    private javax.swing.JMenuItem showDesktopMenuItem;
    private javax.swing.JMenuItem showErrorLogMenuItem;
    private javax.swing.JCheckBoxMenuItem showHighlightKeywords;
    private javax.swing.JMenuItem showKeywordsMenuItem;
    private javax.swing.JMenuItem showLinksMenuItem;
    private javax.swing.JMenuItem showLuhmannMenuItem;
    private javax.swing.JMenuItem showNewEntryMenuItem;
    private javax.swing.JMenuItem showSearchResultsMenuItem;
    private javax.swing.JMenuItem showTitlesMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JButton statusDesktopEntryButton;
    private javax.swing.JLabel statusEntryLabel;
    private javax.swing.JButton statusErrorButton;
    private javax.swing.JLabel statusMsgLabel;
    private javax.swing.JLabel statusOfEntryLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton tb_addbookmark;
    private javax.swing.JButton tb_addluhmann;
    private javax.swing.JButton tb_addmanlinks;
    private javax.swing.JButton tb_addtodesktop;
    private javax.swing.JButton tb_copy;
    private javax.swing.JButton tb_delete;
    private javax.swing.JButton tb_edit;
    private javax.swing.JButton tb_find;
    private javax.swing.JButton tb_first;
    private javax.swing.JButton tb_last;
    private javax.swing.JButton tb_newEntry;
    private javax.swing.JButton tb_next;
    private javax.swing.JButton tb_open;
    private javax.swing.JButton tb_paste;
    private javax.swing.JButton tb_prev;
    private javax.swing.JButton tb_save;
    private javax.swing.JButton tb_selectall;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem viewAttachmentEdit;
    private javax.swing.JMenuItem viewAttachmentsCopy;
    private javax.swing.JMenuItem viewAttachmentsDelete;
    private javax.swing.JMenuItem viewAttachmentsExport;
    private javax.swing.JMenuItem viewAuthorsAddLuhmann;
    private javax.swing.JMenuItem viewAuthorsAddLuhmannAnd;
    private javax.swing.JMenuItem viewAuthorsAddToEntry;
    private javax.swing.JMenuItem viewAuthorsAttachBibtexFile;
    private javax.swing.JMenuItem viewAuthorsBibkey;
    private javax.swing.JMenuItem viewAuthorsCopy;
    private javax.swing.JMenuItem viewAuthorsDelete;
    private javax.swing.JMenuItem viewAuthorsDesktop;
    private javax.swing.JMenuItem viewAuthorsDesktopAnd;
    private javax.swing.JMenuItem viewAuthorsEdit;
    private javax.swing.JMenuItem viewAuthorsExport;
    private javax.swing.JMenuItem viewAuthorsImport;
    private javax.swing.JMenuItem viewAuthorsManLinks;
    private javax.swing.JMenuItem viewAuthorsManLinksAnd;
    private javax.swing.JMenuItem viewAuthorsNew;
    private javax.swing.JMenuItem viewAuthorsRefreshBibtexFile;
    private javax.swing.JMenuItem viewAuthorsSearchAnd;
    private javax.swing.JMenuItem viewAuthorsSearchNot;
    private javax.swing.JMenuItem viewAuthorsSearchOr;
    private javax.swing.JMenu viewAuthorsSubAdd;
    private javax.swing.JMenu viewAuthorsSubEdit;
    private javax.swing.JMenu viewAuthorsSubFind;
    private javax.swing.JMenuItem viewBookmarkDesktop;
    private javax.swing.JMenuItem viewBookmarksAddLuhmann;
    private javax.swing.JMenuItem viewBookmarksDelete;
    private javax.swing.JMenuItem viewBookmarksDeleteCat;
    private javax.swing.JMenuItem viewBookmarksEdit;
    private javax.swing.JMenuItem viewBookmarksEditCat;
    private javax.swing.JMenuItem viewBookmarksExport;
    private javax.swing.JMenuItem viewBookmarksExportSearch;
    private javax.swing.JMenuItem viewBookmarksManLink;
    private javax.swing.JMenuItem viewClusterExport;
    private javax.swing.JMenuItem viewClusterExportToSearch;
    private javax.swing.JMenuItem viewKeywordsAddToList;
    private javax.swing.JMenuItem viewKeywordsCopy;
    private javax.swing.JMenuItem viewKeywordsDelete;
    private javax.swing.JMenuItem viewKeywordsDesktop;
    private javax.swing.JMenuItem viewKeywordsDesktopAnd;
    private javax.swing.JMenuItem viewKeywordsEdit;
    private javax.swing.JMenuItem viewKeywordsExport;
    private javax.swing.JMenuItem viewKeywordsLuhmann;
    private javax.swing.JMenuItem viewKeywordsLuhmannAnd;
    private javax.swing.JMenuItem viewKeywordsManLinks;
    private javax.swing.JMenuItem viewKeywordsManLinksAnd;
    private javax.swing.JMenuItem viewKeywordsNew;
    private javax.swing.JMenuItem viewKeywordsSearchAnd;
    private javax.swing.JMenuItem viewKeywordsSearchNot;
    private javax.swing.JMenuItem viewKeywordsSearchOr;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewMenuAttachmentGoto;
    private javax.swing.JMenu viewMenuAttachments;
    private javax.swing.JMenu viewMenuAuthors;
    private javax.swing.JMenu viewMenuBookmarks;
    private javax.swing.JMenu viewMenuCluster;
    private javax.swing.JMenuItem viewMenuExportToSearch;
    private javax.swing.JMenu viewMenuKeywords;
    private javax.swing.JMenu viewMenuLinks;
    private javax.swing.JMenuItem viewMenuLinksDesktop;
    private javax.swing.JMenuItem viewMenuLinksExport;
    private javax.swing.JCheckBoxMenuItem viewMenuLinksKwListLogAnd;
    private javax.swing.JCheckBoxMenuItem viewMenuLinksKwListLogOr;
    private javax.swing.JMenuItem viewMenuLinksKwListRefresh;
    private javax.swing.JMenuItem viewMenuLinksLuhmann;
    private javax.swing.JMenuItem viewMenuLinksManLink;
    private javax.swing.JMenuItem viewMenuLinksRemoveManLink;
    private javax.swing.JMenu viewMenuLuhmann;
    private javax.swing.JMenuItem viewMenuLuhmannBookmarks;
    private javax.swing.JMenuItem viewMenuLuhmannDelete;
    private javax.swing.JMenuItem viewMenuLuhmannDepth1;
    private javax.swing.JMenuItem viewMenuLuhmannDepth2;
    private javax.swing.JMenuItem viewMenuLuhmannDepth3;
    private javax.swing.JMenuItem viewMenuLuhmannDepth4;
    private javax.swing.JMenuItem viewMenuLuhmannDepth5;
    private javax.swing.JMenuItem viewMenuLuhmannDepthAll;
    private javax.swing.JMenuItem viewMenuLuhmannDesktop;
    private javax.swing.JMenuItem viewMenuLuhmannExport;
    private javax.swing.JMenuItem viewMenuLuhmannExportSearch;
    private javax.swing.JMenuItem viewMenuLuhmannManLinks;
    private javax.swing.JMenu viewMenuLuhmannShowLevel;
    private javax.swing.JCheckBoxMenuItem viewMenuLuhmannShowNumbers;
    private javax.swing.JMenuItem viewMenuLuhmannShowTopLevel;
    private javax.swing.JMenu viewMenuTitles;
    private javax.swing.JMenuItem viewTitlesAutomaticFirstLine;
    private javax.swing.JMenuItem viewTitlesBookmarks;
    private javax.swing.JMenuItem viewTitlesCopy;
    private javax.swing.JMenuItem viewTitlesDelete;
    private javax.swing.JMenuItem viewTitlesDesktop;
    private javax.swing.JMenuItem viewTitlesEdit;
    private javax.swing.JMenuItem viewTitlesExport;
    private javax.swing.JMenuItem viewTitlesLuhmann;
    private javax.swing.JMenuItem viewTitlesManLinks;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables

	private JTextField tb_searchTextfield;
	private JPanel jPanelSearchBox;
	private JLabel jLabelLupe;
	private TaskProgressDialog taskDlg;
	private EditorFrame editEntryDlg;
	private CImport importWindow;
	private CUpdateInfoBox updateInfoDlg;
	private CExport exportWindow;
	private CBiggerEditField biggerEditDlg;
	private SearchResultsFrame searchResultsDlg;
	private CSearchDlg searchDlg;
	private CReplaceDialog replaceDlg;
	private DesktopFrame desktopDlg;
	private CSettingsDlg settingsDlg;
	private CNewBookmark newBookmarkDlg;
	private CErrorLog errorDlg;
	private CInformation informationDlg;
	private CExportEntries exportEntriesDlg;
	private CImportBibTex importBibTexDlg;
	private CShowMultipleDesktopOccurences multipleOccurencesDlg;
	private CSetBibKey setBibKeyDlg;
	private AboutBox zknAboutBox;
	private FindDoubleEntriesTask doubleEntriesDlg;
	private CRateEntry rateEntryDlg;
}
