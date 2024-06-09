package de.danielluedecke.zettelkasten.data;

import de.danielluedecke.zettelkasten.CFilterSearch;
import de.danielluedecke.zettelkasten.CHighlightSearchSettings;
import de.danielluedecke.zettelkasten.CRateEntry;
import de.danielluedecke.zettelkasten.CSearchDlg;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import java.awt.GraphicsDevice;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.jdesktop.application.ResourceMap;

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
	 * Daten object, which contains the XML data of the Zettelkasten
	 */
	private Daten data;
	private DesktopData desktopData;
	/**
	 * A reference to the CSearchRequests-class which stores the search terms and
	 * other search settings like case-sensitive search, where to search in and so
	 * on...
	 */
	private SearchRequests searchRequests;
	/**
	 * Accelerator object, which contains the XML data of the accelerator table for
	 * the menus
	 */
	private AcceleratorKeys acceleratorKeys;
	/**
	 * Reference to the settings class.
	 */
	private Settings settings;
	private BibTeX bibTeX;
	/**
	 *
	 */
	private Synonyms synonyms;
	/**
	 * Reference to the main frame.
	 */
	private ZettelkastenView mainFrame;
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
	private GraphicsDevice graphicDevice;
	private JFrame searchFrame;
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
	 * This variable indicates whether or not the full screen mode is supported on
	 * the current system. if not, disable related icons...
	 */
	private boolean fullScreenSupport;
	private CHighlightSearchSettings highlightSettingsDlg;
	private CSearchDlg searchDlg;
	private TaskProgressDialog taskDlg;
	private CFilterSearch filterSearchDlg;
	private CRateEntry rateEntryDlg;

	public SearchResultsFrameData(DefaultListModel<String> keywordListModel, boolean tableUpdateActive,
			GraphicsDevice graphicdevice, ResourceMap resourceMap, ResourceMap toolbarResourceMap, boolean textSelected,
			boolean listSelected, boolean desktopEntrySelected, boolean fullScreenSupport) {
		this.keywordListModel = keywordListModel;
		this.tableUpdateActive = tableUpdateActive;
		this.graphicDevice = graphicdevice;
		this.resourceMap = resourceMap;
		this.toolbarResourceMap = toolbarResourceMap;
		this.textSelected = textSelected;
		this.listSelected = listSelected;
		this.desktopEntrySelected = desktopEntrySelected;
		this.fullScreenSupport = fullScreenSupport;
	}

	public Daten getData() {
		return data;
	}

	public void setData(Daten data) {
		this.data = data;
	}

	public DesktopData getDesktopData() {
		return desktopData;
	}

	public void setDesktopData(DesktopData desktopData) {
		this.desktopData = desktopData;
	}

	public SearchRequests getSearchRequests() {
		return searchRequests;
	}

	public void setSearchRequests(SearchRequests searchRequests) {
		this.searchRequests = searchRequests;
	}

	public AcceleratorKeys getAcceleratorKeys() {
		return acceleratorKeys;
	}

	public void setAcceleratorKeys(AcceleratorKeys acceleratorKeys) {
		this.acceleratorKeys = acceleratorKeys;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public BibTeX getBibTeX() {
		return bibTeX;
	}

	public void setBibTeX(BibTeX bibTeX) {
		this.bibTeX = bibTeX;
	}

	public Synonyms getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(Synonyms synonyms) {
		this.synonyms = synonyms;
	}

	public ZettelkastenView getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(ZettelkastenView mainFrame) {
		this.mainFrame = mainFrame;
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

	public GraphicsDevice getGraphicDevice() {
		return graphicDevice;
	}

	public void setGraphicDevice(GraphicsDevice graphicDevice) {
		this.graphicDevice = graphicDevice;
	}

	public JFrame getSearchFrame() {
		return searchFrame;
	}

	public void setSearchFrame(JFrame searchFrame) {
		this.searchFrame = searchFrame;
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

	public boolean isFullScreenSupported() {
		return fullScreenSupport;
	}

	public void setFullScreenSupport(boolean fullScreenSupport) {
		this.fullScreenSupport = fullScreenSupport;
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