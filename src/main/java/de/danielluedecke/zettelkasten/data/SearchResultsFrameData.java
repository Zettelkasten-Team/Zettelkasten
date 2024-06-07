package de.danielluedecke.zettelkasten.data;

import java.awt.GraphicsDevice;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;

import org.jdesktop.application.ResourceMap;

import de.danielluedecke.zettelkasten.CFilterSearch;
import de.danielluedecke.zettelkasten.CHighlightSearchSettings;
import de.danielluedecke.zettelkasten.CRateEntry;
import de.danielluedecke.zettelkasten.CSearchDlg;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.DesktopData;
import de.danielluedecke.zettelkasten.database.SearchRequests;
import de.danielluedecke.zettelkasten.database.Synonyms;
import de.danielluedecke.zettelkasten.settings.AcceleratorKeys;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;

public class SearchResultsFrameData {
	/**
	 * CDaten object, which contains the XML data of the Zettelkasten
	 */
	private Daten dataObj;
	private DesktopData desktopObj;
	/**
	 * A reference to the CSearchRequests-class which stores the searchterms and
	 * other search settings like case-sensitive search, where to search in and so
	 * on...
	 */
	private SearchRequests searchrequest;
	/**
	 * CAccelerator object, which contains the XML data of the accelerator table for
	 * the menus
	 */
	private AcceleratorKeys accKeys;
	/**
	 * Reference to the settings class.
	 */
	private Settings settingsObj;
	private BibTeX bibtexObj;
	/**
	 *
	 */
	private Synonyms synonymsObj;
	/**
	 * Reference to the main frame.
	 */
	private ZettelkastenView mainframe;
	/**
	 * create a variable for a list model. this list model is used for the
	 * JList-component which displays the keywords of the current entry.
	 */
	private DefaultListModel<String> keywordListModel;
	/**
	 * Indicated whether a table's content is changed, e.g. entries deleted. if so,
	 * we have to tell this the selection listener which - otherwise - would be
	 * called several times...
	 */
	private boolean tableUpdateActive;
	/**
	 * This variable gets the graphic device and is needed for
	 * full-screen-functionality. see {@link #viewFullScreen() viewFullScreen()} for
	 * more details.
	 */
	private GraphicsDevice graphicdevice;
	private JFrame searchframe;
	/**
	 * get the strings for file descriptions from the resource map
	 */
	private ResourceMap resourceMap;
	/**
	 * get the strings for file descriptions from the resource map
	 */
	private ResourceMap toolbarResourceMap;
	/**
	 * This variable indicates whether we have selected text or not - and
	 * en/disables the new-search-functions. see {@link #newSearchFromAuthor()
	 * newSearchFromAuthor()} and {@link #newSearchFromSelection()
	 * newSearchFromSelection()}.
	 */
	private boolean textSelected;
	/**
	 * This variable indicates whether we have selected an item in the jListKeywords
	 * or not - and en/disables the new-search-functions. see
	 * {@link #newSearchFromKeywordsLogOr() newSearchFromKeywordsLogOr()}
	 */
	private boolean listSelected;
	/**
	 * This variable indicates whether we have selected an entry from the search
	 * results list (jTableResults) that is also present on any desktop.
	 */
	private boolean desktopEntrySelected;
	/**
	 * This variable indicates whether or not the fullscreen mode is supportet on
	 * the current system. if not, disable related icons...
	 */
	private boolean fullScreenSupp;
	private JMenuItem editMenuAddAuthorsToSelection;
	private JMenuItem editMenuAddKeywordsToSelection;
	private JMenuItem editMenuBookmarks;
	private JMenuItem editMenuCopy;
	private JMenuItem editMenuDelete;
	private JMenuItem editMenuDeleteEntry;
	private JMenuItem editMenuDesktop;
	private JMenuItem editMenuDuplicateEntry;
	private JMenuItem editMenuEditEntry;
	private JMenuItem editMenuFindReplace;
	private JMenuItem editMenuLuhmann;
	private JMenuItem editMenuManLinks;
	private JMenuItem editMenuSelectAll;
	private JMenuItem fileMenuClose;
	private JMenuItem fileMenuDeleteAll;
	private JMenuItem fileMenuDeleteSearch;
	private JMenuItem fileMenuDuplicateSearch;
	private JMenuItem fileMenuExport;
	private JMenuItem fileMenuLongDesc;
	private JMenuItem filterAuthors;
	private JMenuItem filterKeywords;
	private JMenuItem filterSearch;
	private JMenuItem filterTopLevelLuhmann;
	private JButton jButtonDeleteSearch;
	private JButton jButtonResetList;
	private JComboBox jComboBoxSearches;
	private JEditorPane jEditorPaneSearchEntry;
	private JLabel jLabel1;
	private JLabel jLabelHits;
	private JList jListKeywords;
	private JMenuItem jMenuItemSwitchLayout;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JPanel jPanel4;
	private JPanel jPanel9;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane4;
	private JSeparator jSeparator1;
	private JSeparator jSeparator10;
	private JSeparator jSeparator11;
	private Separator jSeparator12;
	private JSeparator jSeparator13;
	private JSeparator jSeparator14;
	private JSeparator jSeparator15;
	private JSeparator jSeparator16;
	private JSeparator jSeparator19;
	private JSeparator jSeparator2;
	private JSeparator jSeparator20;
	private JSeparator jSeparator21;
	private JSeparator jSeparator22;
	private javax.swing.JPopupMenu.Separator jSeparator23;
	private Separator jSeparator3;
	private JSeparator jSeparator4;
	private Separator jSeparator5;
	private JSeparator jSeparator6;
	private javax.swing.JPopupMenu.Separator jSeparator7;
	private javax.swing.JPopupMenu.Separator jSeparator8;
	private JSeparator jSeparator9;
	private JSplitPane jSplitPaneSearch1;
	private JSplitPane jSplitPaneSearch2;
	private JTable jTableResults;
	private JTextField jTextFieldFilterList;
	private JMenu searchEditMenu;
	private JMenu searchFileMenu;
	private JMenu searchFilterMenu;
	private JPanel searchMainPanel;
	private JMenuBar searchMenuBar;
	private JMenuItem searchMenuKeywordLogAnd;
	private JMenuItem searchMenuKeywordLogNot;
	private JMenuItem searchMenuKeywordLogOr;
	private JMenuItem searchMenuSelectionContent;
	private JMenu searchSearchMenu;
	private JPanel searchStatusPanel;
	private JToolBar searchToolbar;
	private JMenu searchViewMenu;
	private JButton tb_bookmark;
	private JButton tb_copy;
	private JButton tb_desktop;
	private JButton tb_editentry;
	private JButton tb_highlight;
	private JButton tb_luhmann;
	private JButton tb_manlinks;
	private JButton tb_remove;
	private JButton tb_selectall;
	private JMenuItem viewMenuFullScreen;
	private JCheckBoxMenuItem viewMenuHighlight;
	private JMenuItem viewMenuHighlightSettings;
	private JCheckBoxMenuItem viewMenuShowEntry;
	private JMenuItem viewMenuShowOnDesktop;
	private CHighlightSearchSettings highlightSettingsDlg;
	private CSearchDlg searchDlg;
	private TaskProgressDialog taskDlg;
	private CFilterSearch filterSearchDlg;
	private CRateEntry rateEntryDlg;

	public SearchResultsFrameData(DefaultListModel<String> keywordListModel, boolean tableUpdateActive,
			GraphicsDevice graphicdevice, ResourceMap resourceMap, ResourceMap toolbarResourceMap, boolean textSelected,
			boolean listSelected, boolean desktopEntrySelected, boolean fullScreenSupp) {
		this.keywordListModel = keywordListModel;
		this.tableUpdateActive = tableUpdateActive;
		this.graphicdevice = graphicdevice;
		this.resourceMap = resourceMap;
		this.toolbarResourceMap = toolbarResourceMap;
		this.textSelected = textSelected;
		this.listSelected = listSelected;
		this.desktopEntrySelected = desktopEntrySelected;
		this.fullScreenSupp = fullScreenSupp;
	}

	public Daten getDataObj() {
		return dataObj;
	}

	public void setDataObj(Daten dataObj) {
		this.dataObj = dataObj;
	}

	public DesktopData getDesktopObj() {
		return desktopObj;
	}

	public void setDesktopObj(DesktopData desktopObj) {
		this.desktopObj = desktopObj;
	}

	public SearchRequests getSearchrequest() {
		return searchrequest;
	}

	public void setSearchrequest(SearchRequests searchrequest) {
		this.searchrequest = searchrequest;
	}

	public AcceleratorKeys getAccKeys() {
		return accKeys;
	}

	public void setAccKeys(AcceleratorKeys accKeys) {
		this.accKeys = accKeys;
	}

	public Settings getSettingsObj() {
		return settingsObj;
	}

	public void setSettingsObj(Settings settingsObj) {
		this.settingsObj = settingsObj;
	}

	public BibTeX getBibtexObj() {
		return bibtexObj;
	}

	public void setBibtexObj(BibTeX bibtexObj) {
		this.bibtexObj = bibtexObj;
	}

	public Synonyms getSynonymsObj() {
		return synonymsObj;
	}

	public void setSynonymsObj(Synonyms synonymsObj) {
		this.synonymsObj = synonymsObj;
	}

	public ZettelkastenView getMainframe() {
		return mainframe;
	}

	public void setMainframe(ZettelkastenView mainframe) {
		this.mainframe = mainframe;
	}

	public DefaultListModel<String> getKeywordListModel() {
		return keywordListModel;
	}

	public void setKeywordListModel(DefaultListModel<String> keywordListModel) {
		this.keywordListModel = keywordListModel;
	}

	public boolean isTableUpdateActive() {
		return tableUpdateActive;
	}

	public void setTableUpdateActive(boolean tableUpdateActive) {
		this.tableUpdateActive = tableUpdateActive;
	}

	public GraphicsDevice getGraphicdevice() {
		return graphicdevice;
	}

	public void setGraphicdevice(GraphicsDevice graphicdevice) {
		this.graphicdevice = graphicdevice;
	}

	public JFrame getSearchframe() {
		return searchframe;
	}

	public void setSearchframe(JFrame searchframe) {
		this.searchframe = searchframe;
	}

	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	public ResourceMap getToolbarResourceMap() {
		return toolbarResourceMap;
	}

	public void setToolbarResourceMap(ResourceMap toolbarResourceMap) {
		this.toolbarResourceMap = toolbarResourceMap;
	}

	public boolean isTextSelected() {
		return textSelected;
	}

	public void setTextSelected(boolean textSelected) {
		this.textSelected = textSelected;
	}

	public boolean isListSelected() {
		return listSelected;
	}

	public void setListSelected(boolean listSelected) {
		this.listSelected = listSelected;
	}

	public boolean isDesktopEntrySelected() {
		return desktopEntrySelected;
	}

	public void setDesktopEntrySelected(boolean desktopEntrySelected) {
		this.desktopEntrySelected = desktopEntrySelected;
	}

	public boolean isFullScreenSupp() {
		return fullScreenSupp;
	}

	public void setFullScreenSupp(boolean fullScreenSupp) {
		this.fullScreenSupp = fullScreenSupp;
	}

	public JMenuItem getEditMenuAddAuthorsToSelection() {
		return editMenuAddAuthorsToSelection;
	}

	public void setEditMenuAddAuthorsToSelection(JMenuItem editMenuAddAuthorsToSelection) {
		this.editMenuAddAuthorsToSelection = editMenuAddAuthorsToSelection;
	}

	public JMenuItem getEditMenuAddKeywordsToSelection() {
		return editMenuAddKeywordsToSelection;
	}

	public void setEditMenuAddKeywordsToSelection(JMenuItem editMenuAddKeywordsToSelection) {
		this.editMenuAddKeywordsToSelection = editMenuAddKeywordsToSelection;
	}

	public JMenuItem getEditMenuBookmarks() {
		return editMenuBookmarks;
	}

	public void setEditMenuBookmarks(JMenuItem editMenuBookmarks) {
		this.editMenuBookmarks = editMenuBookmarks;
	}

	public JMenuItem getEditMenuCopy() {
		return editMenuCopy;
	}

	public void setEditMenuCopy(JMenuItem editMenuCopy) {
		this.editMenuCopy = editMenuCopy;
	}

	public JMenuItem getEditMenuDelete() {
		return editMenuDelete;
	}

	public void setEditMenuDelete(JMenuItem editMenuDelete) {
		this.editMenuDelete = editMenuDelete;
	}

	public JMenuItem getEditMenuDeleteEntry() {
		return editMenuDeleteEntry;
	}

	public void setEditMenuDeleteEntry(JMenuItem editMenuDeleteEntry) {
		this.editMenuDeleteEntry = editMenuDeleteEntry;
	}

	public JMenuItem getEditMenuDesktop() {
		return editMenuDesktop;
	}

	public void setEditMenuDesktop(JMenuItem editMenuDesktop) {
		this.editMenuDesktop = editMenuDesktop;
	}

	public JMenuItem getEditMenuDuplicateEntry() {
		return editMenuDuplicateEntry;
	}

	public void setEditMenuDuplicateEntry(JMenuItem editMenuDuplicateEntry) {
		this.editMenuDuplicateEntry = editMenuDuplicateEntry;
	}

	public JMenuItem getEditMenuEditEntry() {
		return editMenuEditEntry;
	}

	public void setEditMenuEditEntry(JMenuItem editMenuEditEntry) {
		this.editMenuEditEntry = editMenuEditEntry;
	}

	public JMenuItem getEditMenuFindReplace() {
		return editMenuFindReplace;
	}

	public void setEditMenuFindReplace(JMenuItem editMenuFindReplace) {
		this.editMenuFindReplace = editMenuFindReplace;
	}

	public JMenuItem getEditMenuLuhmann() {
		return editMenuLuhmann;
	}

	public void setEditMenuLuhmann(JMenuItem editMenuLuhmann) {
		this.editMenuLuhmann = editMenuLuhmann;
	}

	public JMenuItem getEditMenuManLinks() {
		return editMenuManLinks;
	}

	public void setEditMenuManLinks(JMenuItem editMenuManLinks) {
		this.editMenuManLinks = editMenuManLinks;
	}

	public JMenuItem getEditMenuSelectAll() {
		return editMenuSelectAll;
	}

	public void setEditMenuSelectAll(JMenuItem editMenuSelectAll) {
		this.editMenuSelectAll = editMenuSelectAll;
	}

	public JMenuItem getFileMenuClose() {
		return fileMenuClose;
	}

	public void setFileMenuClose(JMenuItem fileMenuClose) {
		this.fileMenuClose = fileMenuClose;
	}

	public JMenuItem getFileMenuDeleteAll() {
		return fileMenuDeleteAll;
	}

	public void setFileMenuDeleteAll(JMenuItem fileMenuDeleteAll) {
		this.fileMenuDeleteAll = fileMenuDeleteAll;
	}

	public JMenuItem getFileMenuDeleteSearch() {
		return fileMenuDeleteSearch;
	}

	public void setFileMenuDeleteSearch(JMenuItem fileMenuDeleteSearch) {
		this.fileMenuDeleteSearch = fileMenuDeleteSearch;
	}

	public JMenuItem getFileMenuDuplicateSearch() {
		return fileMenuDuplicateSearch;
	}

	public void setFileMenuDuplicateSearch(JMenuItem fileMenuDuplicateSearch) {
		this.fileMenuDuplicateSearch = fileMenuDuplicateSearch;
	}

	public JMenuItem getFileMenuExport() {
		return fileMenuExport;
	}

	public void setFileMenuExport(JMenuItem fileMenuExport) {
		this.fileMenuExport = fileMenuExport;
	}

	public JMenuItem getFileMenuLongDesc() {
		return fileMenuLongDesc;
	}

	public void setFileMenuLongDesc(JMenuItem fileMenuLongDesc) {
		this.fileMenuLongDesc = fileMenuLongDesc;
	}

	public JMenuItem getFilterAuthors() {
		return filterAuthors;
	}

	public void setFilterAuthors(JMenuItem filterAuthors) {
		this.filterAuthors = filterAuthors;
	}

	public JMenuItem getFilterKeywords() {
		return filterKeywords;
	}

	public void setFilterKeywords(JMenuItem filterKeywords) {
		this.filterKeywords = filterKeywords;
	}

	public JMenuItem getFilterSearch() {
		return filterSearch;
	}

	public void setFilterSearch(JMenuItem filterSearch) {
		this.filterSearch = filterSearch;
	}

	public JMenuItem getFilterTopLevelLuhmann() {
		return filterTopLevelLuhmann;
	}

	public void setFilterTopLevelLuhmann(JMenuItem filterTopLevelLuhmann) {
		this.filterTopLevelLuhmann = filterTopLevelLuhmann;
	}

	public JButton getjButtonDeleteSearch() {
		return jButtonDeleteSearch;
	}

	public void setjButtonDeleteSearch(JButton jButtonDeleteSearch) {
		this.jButtonDeleteSearch = jButtonDeleteSearch;
	}

	public JButton getjButtonResetList() {
		return jButtonResetList;
	}

	public void setjButtonResetList(JButton jButtonResetList) {
		this.jButtonResetList = jButtonResetList;
	}

	public JComboBox getjComboBoxSearches() {
		return jComboBoxSearches;
	}

	public void setjComboBoxSearches(JComboBox jComboBoxSearches) {
		this.jComboBoxSearches = jComboBoxSearches;
	}

	public JEditorPane getjEditorPaneSearchEntry() {
		return jEditorPaneSearchEntry;
	}

	public void setjEditorPaneSearchEntry(JEditorPane jEditorPaneSearchEntry) {
		this.jEditorPaneSearchEntry = jEditorPaneSearchEntry;
	}

	public JLabel getjLabel1() {
		return jLabel1;
	}

	public void setjLabel1(JLabel jLabel1) {
		this.jLabel1 = jLabel1;
	}

	public JLabel getjLabelHits() {
		return jLabelHits;
	}

	public void setjLabelHits(JLabel jLabelHits) {
		this.jLabelHits = jLabelHits;
	}

	public JList getjListKeywords() {
		return jListKeywords;
	}

	public void setjListKeywords(JList jListKeywords) {
		this.jListKeywords = jListKeywords;
	}

	public JMenuItem getjMenuItemSwitchLayout() {
		return jMenuItemSwitchLayout;
	}

	public void setjMenuItemSwitchLayout(JMenuItem jMenuItemSwitchLayout) {
		this.jMenuItemSwitchLayout = jMenuItemSwitchLayout;
	}

	public JPanel getjPanel1() {
		return jPanel1;
	}

	public void setjPanel1(JPanel jPanel1) {
		this.jPanel1 = jPanel1;
	}

	public JPanel getjPanel2() {
		return jPanel2;
	}

	public void setjPanel2(JPanel jPanel2) {
		this.jPanel2 = jPanel2;
	}

	public JPanel getjPanel3() {
		return jPanel3;
	}

	public void setjPanel3(JPanel jPanel3) {
		this.jPanel3 = jPanel3;
	}

	public JPanel getjPanel4() {
		return jPanel4;
	}

	public void setjPanel4(JPanel jPanel4) {
		this.jPanel4 = jPanel4;
	}

	public JPanel getjPanel9() {
		return jPanel9;
	}

	public void setjPanel9(JPanel jPanel9) {
		this.jPanel9 = jPanel9;
	}

	public JScrollPane getjScrollPane1() {
		return jScrollPane1;
	}

	public void setjScrollPane1(JScrollPane jScrollPane1) {
		this.jScrollPane1 = jScrollPane1;
	}

	public JScrollPane getjScrollPane2() {
		return jScrollPane2;
	}

	public void setjScrollPane2(JScrollPane jScrollPane2) {
		this.jScrollPane2 = jScrollPane2;
	}

	public JScrollPane getjScrollPane4() {
		return jScrollPane4;
	}

	public void setjScrollPane4(JScrollPane jScrollPane4) {
		this.jScrollPane4 = jScrollPane4;
	}

	public JSeparator getjSeparator1() {
		return jSeparator1;
	}

	public void setjSeparator1(JSeparator jSeparator1) {
		this.jSeparator1 = jSeparator1;
	}

	public JSeparator getjSeparator10() {
		return jSeparator10;
	}

	public void setjSeparator10(JSeparator jSeparator10) {
		this.jSeparator10 = jSeparator10;
	}

	public JSeparator getjSeparator11() {
		return jSeparator11;
	}

	public void setjSeparator11(JSeparator jSeparator11) {
		this.jSeparator11 = jSeparator11;
	}

	public Separator getjSeparator12() {
		return jSeparator12;
	}

	public void setjSeparator12(Separator jSeparator12) {
		this.jSeparator12 = jSeparator12;
	}

	public JSeparator getjSeparator13() {
		return jSeparator13;
	}

	public void setjSeparator13(JSeparator jSeparator13) {
		this.jSeparator13 = jSeparator13;
	}

	public JSeparator getjSeparator14() {
		return jSeparator14;
	}

	public void setjSeparator14(JSeparator jSeparator14) {
		this.jSeparator14 = jSeparator14;
	}

	public JSeparator getjSeparator15() {
		return jSeparator15;
	}

	public void setjSeparator15(JSeparator jSeparator15) {
		this.jSeparator15 = jSeparator15;
	}

	public JSeparator getjSeparator16() {
		return jSeparator16;
	}

	public void setjSeparator16(JSeparator jSeparator16) {
		this.jSeparator16 = jSeparator16;
	}

	public JSeparator getjSeparator19() {
		return jSeparator19;
	}

	public void setjSeparator19(JSeparator jSeparator19) {
		this.jSeparator19 = jSeparator19;
	}

	public JSeparator getjSeparator2() {
		return jSeparator2;
	}

	public void setjSeparator2(JSeparator jSeparator2) {
		this.jSeparator2 = jSeparator2;
	}

	public JSeparator getjSeparator20() {
		return jSeparator20;
	}

	public void setjSeparator20(JSeparator jSeparator20) {
		this.jSeparator20 = jSeparator20;
	}

	public JSeparator getjSeparator21() {
		return jSeparator21;
	}

	public void setjSeparator21(JSeparator jSeparator21) {
		this.jSeparator21 = jSeparator21;
	}

	public JSeparator getjSeparator22() {
		return jSeparator22;
	}

	public void setjSeparator22(JSeparator jSeparator22) {
		this.jSeparator22 = jSeparator22;
	}

	public javax.swing.JPopupMenu.Separator getjSeparator23() {
		return jSeparator23;
	}

	public void setjSeparator23(javax.swing.JPopupMenu.Separator jSeparator23) {
		this.jSeparator23 = jSeparator23;
	}

	public Separator getjSeparator3() {
		return jSeparator3;
	}

	public void setjSeparator3(Separator jSeparator3) {
		this.jSeparator3 = jSeparator3;
	}

	public JSeparator getjSeparator4() {
		return jSeparator4;
	}

	public void setjSeparator4(JSeparator jSeparator4) {
		this.jSeparator4 = jSeparator4;
	}

	public Separator getjSeparator5() {
		return jSeparator5;
	}

	public void setjSeparator5(Separator jSeparator5) {
		this.jSeparator5 = jSeparator5;
	}

	public JSeparator getjSeparator6() {
		return jSeparator6;
	}

	public void setjSeparator6(JSeparator jSeparator6) {
		this.jSeparator6 = jSeparator6;
	}

	public javax.swing.JPopupMenu.Separator getjSeparator7() {
		return jSeparator7;
	}

	public void setjSeparator7(javax.swing.JPopupMenu.Separator jSeparator7) {
		this.jSeparator7 = jSeparator7;
	}

	public javax.swing.JPopupMenu.Separator getjSeparator8() {
		return jSeparator8;
	}

	public void setjSeparator8(javax.swing.JPopupMenu.Separator jSeparator8) {
		this.jSeparator8 = jSeparator8;
	}

	public JSeparator getjSeparator9() {
		return jSeparator9;
	}

	public void setjSeparator9(JSeparator jSeparator9) {
		this.jSeparator9 = jSeparator9;
	}

	public JSplitPane getjSplitPaneSearch1() {
		return jSplitPaneSearch1;
	}

	public void setjSplitPaneSearch1(JSplitPane jSplitPaneSearch1) {
		this.jSplitPaneSearch1 = jSplitPaneSearch1;
	}

	public JSplitPane getjSplitPaneSearch2() {
		return jSplitPaneSearch2;
	}

	public void setjSplitPaneSearch2(JSplitPane jSplitPaneSearch2) {
		this.jSplitPaneSearch2 = jSplitPaneSearch2;
	}

	public JTable getjTableResults() {
		return jTableResults;
	}

	public void setjTableResults(JTable jTableResults) {
		this.jTableResults = jTableResults;
	}

	public JTextField getjTextFieldFilterList() {
		return jTextFieldFilterList;
	}

	public void setjTextFieldFilterList(JTextField jTextFieldFilterList) {
		this.jTextFieldFilterList = jTextFieldFilterList;
	}

	public JMenu getSearchEditMenu() {
		return searchEditMenu;
	}

	public void setSearchEditMenu(JMenu searchEditMenu) {
		this.searchEditMenu = searchEditMenu;
	}

	public JMenu getSearchFileMenu() {
		return searchFileMenu;
	}

	public void setSearchFileMenu(JMenu searchFileMenu) {
		this.searchFileMenu = searchFileMenu;
	}

	public JMenu getSearchFilterMenu() {
		return searchFilterMenu;
	}

	public void setSearchFilterMenu(JMenu searchFilterMenu) {
		this.searchFilterMenu = searchFilterMenu;
	}

	public JPanel getSearchMainPanel() {
		return searchMainPanel;
	}

	public void setSearchMainPanel(JPanel searchMainPanel) {
		this.searchMainPanel = searchMainPanel;
	}

	public JMenuBar getSearchMenuBar() {
		return searchMenuBar;
	}

	public void setSearchMenuBar(JMenuBar searchMenuBar) {
		this.searchMenuBar = searchMenuBar;
	}

	public JMenuItem getSearchMenuKeywordLogAnd() {
		return searchMenuKeywordLogAnd;
	}

	public void setSearchMenuKeywordLogAnd(JMenuItem searchMenuKeywordLogAnd) {
		this.searchMenuKeywordLogAnd = searchMenuKeywordLogAnd;
	}

	public JMenuItem getSearchMenuKeywordLogNot() {
		return searchMenuKeywordLogNot;
	}

	public void setSearchMenuKeywordLogNot(JMenuItem searchMenuKeywordLogNot) {
		this.searchMenuKeywordLogNot = searchMenuKeywordLogNot;
	}

	public JMenuItem getSearchMenuKeywordLogOr() {
		return searchMenuKeywordLogOr;
	}

	public void setSearchMenuKeywordLogOr(JMenuItem searchMenuKeywordLogOr) {
		this.searchMenuKeywordLogOr = searchMenuKeywordLogOr;
	}

	public JMenuItem getSearchMenuSelectionContent() {
		return searchMenuSelectionContent;
	}

	public void setSearchMenuSelectionContent(JMenuItem searchMenuSelectionContent) {
		this.searchMenuSelectionContent = searchMenuSelectionContent;
	}

	public JMenu getSearchSearchMenu() {
		return searchSearchMenu;
	}

	public void setSearchSearchMenu(JMenu searchSearchMenu) {
		this.searchSearchMenu = searchSearchMenu;
	}

	public JPanel getSearchStatusPanel() {
		return searchStatusPanel;
	}

	public void setSearchStatusPanel(JPanel searchStatusPanel) {
		this.searchStatusPanel = searchStatusPanel;
	}

	public JToolBar getSearchToolbar() {
		return searchToolbar;
	}

	public void setSearchToolbar(JToolBar searchToolbar) {
		this.searchToolbar = searchToolbar;
	}

	public JMenu getSearchViewMenu() {
		return searchViewMenu;
	}

	public void setSearchViewMenu(JMenu searchViewMenu) {
		this.searchViewMenu = searchViewMenu;
	}

	public JButton getTb_bookmark() {
		return tb_bookmark;
	}

	public void setTb_bookmark(JButton tb_bookmark) {
		this.tb_bookmark = tb_bookmark;
	}

	public JButton getTb_copy() {
		return tb_copy;
	}

	public void setTb_copy(JButton tb_copy) {
		this.tb_copy = tb_copy;
	}

	public JButton getTb_desktop() {
		return tb_desktop;
	}

	public void setTb_desktop(JButton tb_desktop) {
		this.tb_desktop = tb_desktop;
	}

	public JButton getTb_editentry() {
		return tb_editentry;
	}

	public void setTb_editentry(JButton tb_editentry) {
		this.tb_editentry = tb_editentry;
	}

	public JButton getTb_highlight() {
		return tb_highlight;
	}

	public void setTb_highlight(JButton tb_highlight) {
		this.tb_highlight = tb_highlight;
	}

	public JButton getTb_luhmann() {
		return tb_luhmann;
	}

	public void setTb_luhmann(JButton tb_luhmann) {
		this.tb_luhmann = tb_luhmann;
	}

	public JButton getTb_manlinks() {
		return tb_manlinks;
	}

	public void setTb_manlinks(JButton tb_manlinks) {
		this.tb_manlinks = tb_manlinks;
	}

	public JButton getTb_remove() {
		return tb_remove;
	}

	public void setTb_remove(JButton tb_remove) {
		this.tb_remove = tb_remove;
	}

	public JButton getTb_selectall() {
		return tb_selectall;
	}

	public void setTb_selectall(JButton tb_selectall) {
		this.tb_selectall = tb_selectall;
	}

	public JMenuItem getViewMenuFullScreen() {
		return viewMenuFullScreen;
	}

	public void setViewMenuFullScreen(JMenuItem viewMenuFullScreen) {
		this.viewMenuFullScreen = viewMenuFullScreen;
	}

	public JCheckBoxMenuItem getViewMenuHighlight() {
		return viewMenuHighlight;
	}

	public void setViewMenuHighlight(JCheckBoxMenuItem viewMenuHighlight) {
		this.viewMenuHighlight = viewMenuHighlight;
	}

	public JMenuItem getViewMenuHighlightSettings() {
		return viewMenuHighlightSettings;
	}

	public void setViewMenuHighlightSettings(JMenuItem viewMenuHighlightSettings) {
		this.viewMenuHighlightSettings = viewMenuHighlightSettings;
	}

	public JCheckBoxMenuItem getViewMenuShowEntry() {
		return viewMenuShowEntry;
	}

	public void setViewMenuShowEntry(JCheckBoxMenuItem viewMenuShowEntry) {
		this.viewMenuShowEntry = viewMenuShowEntry;
	}

	public JMenuItem getViewMenuShowOnDesktop() {
		return viewMenuShowOnDesktop;
	}

	public void setViewMenuShowOnDesktop(JMenuItem viewMenuShowOnDesktop) {
		this.viewMenuShowOnDesktop = viewMenuShowOnDesktop;
	}

	public CHighlightSearchSettings getHighlightSettingsDlg() {
		return highlightSettingsDlg;
	}

	public void setHighlightSettingsDlg(CHighlightSearchSettings highlightSettingsDlg) {
		this.highlightSettingsDlg = highlightSettingsDlg;
	}

	public CSearchDlg getSearchDlg() {
		return searchDlg;
	}

	public void setSearchDlg(CSearchDlg searchDlg) {
		this.searchDlg = searchDlg;
	}

	public TaskProgressDialog getTaskDlg() {
		return taskDlg;
	}

	public void setTaskDlg(TaskProgressDialog taskDlg) {
		this.taskDlg = taskDlg;
	}

	public CFilterSearch getFilterSearchDlg() {
		return filterSearchDlg;
	}

	public void setFilterSearchDlg(CFilterSearch filterSearchDlg) {
		this.filterSearchDlg = filterSearchDlg;
	}

	public CRateEntry getRateEntryDlg() {
		return rateEntryDlg;
	}

	public void setRateEntryDlg(CRateEntry rateEntryDlg) {
		this.rateEntryDlg = rateEntryDlg;
	}
}