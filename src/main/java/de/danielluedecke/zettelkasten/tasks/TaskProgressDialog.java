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
package de.danielluedecke.zettelkasten.tasks;

import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.tasks.export.ExportToCsvTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToHtmlTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToMdTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToTexTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToTxtTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToXmlTask;
import de.danielluedecke.zettelkasten.tasks.export.ExportToZknTask;
import de.danielluedecke.zettelkasten.tasks.importtasks.ImportFromCSV;
import de.danielluedecke.zettelkasten.tasks.importtasks.ImportFromZkn;
import de.danielluedecke.zettelkasten.tasks.importtasks.ImportFromZkx;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.InitStatusbarForTasks;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;

/**
 *
 * @author danielludecke
 */
public class TaskProgressDialog extends javax.swing.JDialog {

	/**
	 * Daten object, which contains the XML data of the Zettelkasten
	 */
	private final Daten daten;
	/**
	 *
	 */
	private TasksData tasksData;
	/**
	 * CBookmark object, which contains the XML data of the entries' bookmarks
	 */
	private Bookmarks bookmarks;
	/**
	 * DesktopData object, which contains the XML data of the desktop
	 */
	private DesktopData desktopData;
	/**
	 * Settings object, which contains the setting, for instance the file paths
	 * etc...
	 */
	private Settings settings;
	/**
	 * Settings object, which contains the setting, for instance the file paths
	 * etc...
	 */
	private Synonyms synonyms;
	/**
	 * Searchequests object, which contains the XML data of the searchrequests and
	 * -result that are related with this data file
	 */
	private SearchRequests searchRequests;
	private BibTeX bibTeX;

	private TaskMonitor taskMonitor;
	private Task foregroundTask; // FIXME Provide the parametrized type for this generic.

	public static final int TASK_LOAD = 1;
	public static final int TASK_SAVE = 2;
	public static final int TASK_SEARCH = 3;
	public static final int TASK_SHOWKEYWORDS = 4;
	public static final int TASK_SHOWAUTHORS = 5;
	public static final int TASK_SHOWTITLES = 6;
	public static final int TASK_UPDATEFILE = 7;
	public static final int TASK_REPLACE = 8;
	public static final int TASK_DELETEAUTHORS = 9;
	public static final int TASK_KEYWORDSUGGESTIONS = 10;
	public static final int TASK_IMPORTDATA = 11;
	public static final int TASK_EXPORTDATA = 12;
	public static final int TASK_SETFIRSTLINEASTITLE = 13;
	public static final int TASK_DELETEENTRY = 14;
	public static final int TASK_SHOWATTACHMENTS = 15;
	public static final int TASK_DELETEKEYWORDS = 16;
	public static final int TASK_MERGEAUTHORS = 17;
	public static final int TASK_MERGEKEYWORDS = 18;
	public static final int TASK_ENTRIESTOLUHMANN = 19;
	public static final int TASK_CONVERTFORMATTAGS = 20;
	public static final int TASK_REFRESHBIBTEX = 21;

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>a data file is opened (use {@link #TASK_LOAD} as parameter)</li>
	 * <li>a data file is saved (use {@link #TASK_SAVE} as parameter)</li>
	 * </ul>
	 *
	 * @param parent         a reference to the parent frame that created this
	 *                       dialog
	 * @param task_id        the task-id, identifying which task should be started,
	 *                       in case this constructor is used for more than one
	 *                       task. See above which task-id to use.
	 * @param daten          a reference to the Daten-class.
	 * @param bookmarks
	 * @param searchRequests
	 * @param desktopData
	 * @param synonyms
	 * @param settings
	 * @param bibTeX
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten daten, Bookmarks bookmarks,
			SearchRequests searchRequests, DesktopData desktopData, Synonyms synonyms, Settings settings,
			BibTeX bibTeX) {
		super(parent);
		this.daten = daten;
		this.bookmarks = bookmarks;
		this.searchRequests = searchRequests;
		this.desktopData = desktopData;
		this.synonyms = synonyms;
		this.settings = settings;
		this.bibTeX = bibTeX;

		initComponents();

		// set application icon
		setIconImage(Constants.zknicon.getImage());

		// init the progress bar and status icon for
		// the swing worker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null); // FIXME Remove this unused
																						// "isb" local variable.
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_LOAD:
			foregroundTask = loadFile();
			break;
		case TASK_SAVE:
			foregroundTask = saveFile();
			break;
		}
		startTask();
	}

	public TaskProgressDialog(java.awt.Frame parent, int task_id, TasksData td, Daten d, BibTeX bib) {
		super(parent);
		daten = d;
		bibTeX = bib;
		tasksData = td;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_REFRESHBIBTEX:
			foregroundTask = refreshBibTex();
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>a data file being updated to a new version (use {@link #TASK_UPDATEFILE}
	 * as parameter)</li>
	 * </ul>
	 *
	 * @param parent  a reference to the parent frame that created this dialog
	 * @param task_id the task-id, identifying which task should be started, in case
	 *                this constructor is used for more than one task. See above
	 *                which task-id to use.
	 * @param s
	 * @param d       a reference to the Daten-class.
	 * @param dk
	 * @param bib
	 * @param rf
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, Settings s, Daten d, DesktopData dk, BibTeX bib,
			boolean rf) {
		super(parent);
		daten = d;
		settings = s;
		desktopData = dk;
		bibTeX = bib;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_UPDATEFILE:
			foregroundTask = updateFile(rf);
			break;
		}
		startTask();
	}

	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, int messageOptions) {
		super(parent);
		daten = d;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_SETFIRSTLINEASTITLE:
			foregroundTask = setFirstLineAsTitle(messageOptions);
			break;
		case TASK_CONVERTFORMATTAGS:
			foregroundTask = convertFormatTags(messageOptions);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>words or strings in the data abse are being replaced (use
	 * {@link #TASK_REPLACE} as parameter)</li>
	 * </ul>
	 *
	 * @param parent  a reference to the parent frame that created this dialog
	 * @param task_id the task-id, identifying which task should be started, in case
	 *                this constructor is used for more than one task. See above
	 *                which task-id to use.
	 * @param td
	 * @param d       a reference to the Daten-class.
	 * @param fs      a string containing the find-term that should be replaced
	 * @param rs      a string containing the replace-term that replaces the
	 *                find-term {@code fs}
	 * @param re      an integer array containing the entry-numbers of those entries
	 *                where the search should be applied to. use {@code null} to
	 *                search and replace in all entries.
	 * @param w       where the search should be applied to, i.e. search within
	 *                content, keywords, authors etc.
	 * @param ww      pass true, if the search should find whole words only
	 * @param mc      whether the search is case sensitive (true) or not (false)
	 * @param rex     whether the find-term {@code fs} is a regular expression
	 *                (true) or not...
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, TasksData td, Daten d, String fs, String rs, int[] re,
			int w, boolean ww, boolean mc, boolean rex) {
		super(parent);
		daten = d;
		tasksData = td;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_REPLACE:
			foregroundTask = replace(fs, rs, re, w, ww, mc, rex);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>The keyword-list in the related tabbed pane is being displayed (use
	 * {@link #TASK_SHOWKEYWORDS} as parameter)</li>
	 * <li>The author-list in the related tabbed pane is being displayed (use
	 * {@link #TASK_SHOWAUTHORS} as parameter)</li>
	 * <li>The title-list in the related tabbed pane is being displayed (use
	 * {@link #TASK_SHOWTITLES} as parameter)</li>
	 * <li>The attachment-list in the related tabbed pane is being displayed (use
	 * {@link #TASK_SHOWATTACHMENTS} as parameter)</li>
	 * </ul>
	 *
	 * @param parent  a reference to the parent frame that created this dialog
	 * @param task_id the task-id, identifying which task should be started, in case
	 *                this constructor is used for more than one task. See above
	 *                which task-id to use.
	 * @param d       a reference to the Daten-class.
	 * @param s       a reference to the Synonyms-class. Only needed when showing
	 *                the keywords, i.e. when the {@code task_id} is
	 *                {@code TASK_SHOWKEYWORDS}
	 * @param bt      a reference to the Bibtex-class. Only needed when showing the
	 *                authors, i.e. when the {@code task_id} is
	 *                {@code TASK_SHOWAUTHORS}
	 * @param set     a reference to the Settings-class. Only needed when showing
	 *                the attachments, i.e. when the {@code task_id} is
	 *                {@code TASK_SHOWATTACHMENTS}
	 * @param is      indicates whether synonyms should be included when displaying
	 *                the keywords. Only needed when showing the keywords, i.e. when
	 *                the {@code task_id} is {@code TASK_SHOWKEYWORDS}
	 * @param et      indicates which entry-type of authors should be displayed.
	 *                this is a filter variable which refers to certain types of
	 *                publications, indicates in the bibtex-data file. Only needed
	 *                when showing the authors, i.e. when the {@code task_id} is
	 *                {@code TASK_SHOWAUTHORS}
	 * @param dtm     a reference to the DefaultTableModel of the related tabbed
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, Synonyms s, BibTeX bt, Settings set,
			boolean is, int et, DefaultTableModel dtm) {
		super(parent);
		daten = d;
		bibTeX = bt;
		synonyms = s;
		settings = set;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_SHOWKEYWORDS:
			foregroundTask = showKeywords(is, dtm);
			break;
		case TASK_SHOWAUTHORS:
			foregroundTask = showAuthors(et, dtm);
			break;
		case TASK_SHOWTITLES:
			foregroundTask = showTitles(dtm);
			break;
		case TASK_SHOWATTACHMENTS:
			foregroundTask = showAttachments(dtm);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>One or more selected author values in the related tabbed pane are being
	 * removed from the data base (use {@link #TASK_DELETEAUTHORS} as
	 * parameter)</li>
	 * </ul>
	 *
	 * @param parent  a reference to the parent frame that created this dialog
	 * @param task_id the task-id, identifying which task should be started, in case
	 *                this constructor is used for more than one task. See above
	 *                which task-id to use.
	 * @param d       a reference to the Daten-class.
	 * @param values
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, String[] values) {
		super(parent);
		daten = d;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_DELETEAUTHORS:
			foregroundTask = deleteAuthors(values);
			break;
		case TASK_DELETEKEYWORDS:
			foregroundTask = deleteKeywords(values);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>An entry should be deleted (removed) from the data base (use
	 * {@link #TASK_DELETEENTRY} as parameter)</li>
	 * </ul>
	 *
	 * @param parent  a reference to the parent frame that created this dialog
	 * @param task_id the task-id, identifying which task should be started, in case
	 *                this constructor is used for more than one task. See above
	 *                which task-id to use.
	 * @param d       a reference to the Daten-class.
	 * @param s       a reference to the SearchRequests-class
	 * @param entries one or more entry-numbers of those entries that should be
	 *                deleted.
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, SearchRequests s, int[] entries) {
		super(parent);
		daten = d;
		searchRequests = s;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_DELETEENTRY:
			foregroundTask = deleteEntry(entries);
			break;
		}
		startTask();
	}

	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, int[] entries, int insertpos) {
		super(parent);
		daten = d;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_ENTRIESTOLUHMANN:
			foregroundTask = entriesToLuhmann(entries, insertpos);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>The the keyword suggestions during a new entry or editring entry is used
	 * (use {@link #TASK_KEYWORDSUGGESTIONS} as parameter)</li>
	 * </ul>
	 *
	 * @param parent             a reference to the parent frame that created this
	 *                           dialog
	 * @param task_id            the task-id, identifying which task should be
	 *                           started, in case this constructor is used for more
	 *                           than one task. See above which task-id to use.
	 * @param td
	 * @param d                  a reference to the Daten-class.
	 * @param syn
	 * @param set
	 * @param extendedQuickInput Indicates whether we have an extended quick input
	 *                           setting. when this setting is activated,
	 *                           keyword-values consisting of more than one word are
	 *                           splitted, and the occurence of each keyword-part is
	 *                           searched in the main entries content. if found, the
	 *                           related keyword also counts as match.
	 * @param quickstep          Indictaes which of the four steps for the quick
	 *                           input is currently processed.
	 * @param selectedKeywords   The keywords the user selected in the first step of
	 *                           the quick input. needed to retrieve the keywords
	 *                           for the seconde step, since in this 2. step we want
	 *                           to have all related keywords of those keywords that
	 *                           have been selected in the first step.
	 * @param remainingKeywords  A List containing the remaining keywords that
	 *                           haven't been retrieved during the past quick input
	 *                           steps. having this list, we don't need to look
	 *                           through the whole keyword list on the one hand, on
	 *                           the other hand we prevent finding double keywords.
	 * @param fromFirstStep      Similar to {@code selectedKeywords}. We need the
	 *                           keywords of the first step for the third step.
	 * @param entrytext          The entry-text that contains the content of the
	 *                           entry, where we want to find related keywords for.
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, TasksData td, Daten d, Synonyms syn, Settings set,
			int extendedQuickInput, int quickstep, LinkedList<String> selectedKeywords,
			LinkedList<String> remainingKeywords, LinkedList<String> fromFirstStep, String entrytext) {
		super(parent);
		daten = d;
		tasksData = td;
		synonyms = syn;
		settings = set;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_KEYWORDSUGGESTIONS:
			foregroundTask = keywordSuggestions(extendedQuickInput, quickstep, selectedKeywords, remainingKeywords,
					fromFirstStep, entrytext);
			break;
		}
		startTask();
	}

	/**
	 * This constructor is called when:
	 * <ul>
	 * <li>A search request is started (use {@link #TASK_SEARCH} as parameter)</li>
	 * </ul>
	 *
	 *
	 * @param frame       a reference to the parent frame that created this dialog
	 * @param taskID      the task-id, identifying which task should be started, in
	 *                    case this constructor is used for more than one task. See
	 *                    above which task-id to use.
	 * @param d           a reference to the Daten-class.
	 * @param sr          a reference to the SearchRequests class
	 * @param sy
	 * @param tos
	 * @param s           a string-array containing search terms
	 * @param se          an integer array containing the entry-numbers of those
	 *                    entries where the search should be applied to
	 * @param w           where the search should be applied to, i.e. search within
	 *                    content, keywords, authors etc.
	 * @param l           the logical-combination of the search
	 * @param ww          pass true, if the search should find whole words only
	 * @param mc          whether the search is case sensitive (true) or not (false)
	 * @param syn         whether the search should include synonyms or not
	 * @param regexSearch whether the search terms contain regular expressions or
	 *                    not
	 * @param ts          whether the user requested a time-search, i.e. a search
	 *                    for entries that were created or changed within a certain
	 *                    period
	 * @param fr          the start of the period, when a timesearch is requested.
	 *                    format: "yymmdd".
	 * @param to          the end of the period, when a timesearch is requested.
	 *                    format: "yymmdd".
	 * @param tsi         the timestampindex, which indicate whether the user wants
	 *                    to search only for entries within a period of
	 *                    <i>creation</i> date (0), of <i>edited</i> date (1) or
	 *                    both (2).
	 * @param donly       whether the search should open the CSearchResults-frame
	 *                    (false), or whether the search-results are used for other
	 *                    purposes, like e.g. putting the results to the desktop
	 *                    (true)
	 * @param rt          Whether tags should be removed from entry content before
	 *                    searching the entry. Increases speed, however, some words
	 *                    may not be found (which have tags inside a word to
	 *                    emphasize a word part, like <i>Zettel</i>kasten.
	 */
	public TaskProgressDialog(java.awt.Frame frame, int taskID, Daten d, SearchRequests sr, Synonyms sy, int tos,
			String[] s, int[] se, int w, int l, boolean ww, boolean mc, boolean syn, boolean accentInsensitive,
			boolean regexSearch, boolean ts, String fr, String to, int tsi, boolean donly, boolean rt) {
		super(frame);

		daten = d;
		synonyms = sy;
		searchRequests = sr;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (taskID) {
		case TASK_SEARCH:
			foregroundTask = startSearch(tos, s, se, w, l, ww, mc, syn, accentInsensitive, regexSearch, ts, fr, to, tsi,
					donly, rt);
			break;
		}
		startTask();
	}

	public TaskProgressDialog(java.awt.Frame parent, int task_id, Daten d, TasksData td, String oldvalue,
			String newvalue, String newbibkey, JTable table, int selectedrow, LinkedList<Object[]> linkedvalues) {
		super(parent);
		daten = d;
		tasksData = td;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_MERGEAUTHORS:
			foregroundTask = mergeAuthors(oldvalue, newvalue, newbibkey, table, selectedrow, linkedvalues);
			break;
		case TASK_MERGEKEYWORDS:
			foregroundTask = mergeKeywords(oldvalue, newvalue, table, selectedrow, linkedvalues);
			break;
		}
		startTask();
	}

	/**
	 *
	 * @param parent  the dialog's parent frame
	 * @param task_id
	 * @param td      a reference to the TaskDatas-class
	 * @param d       a reference to the Daten-class
	 * @param bm
	 * @param dt      a reference to the DesktopData-class
	 * @param s       a reference to the Settings-class
	 * @param bto     a refrence to the BibTeX-class
	 * @param syn
	 * @param fp      the filepath and -name of the export-file
	 * @param ee      an integer-array of those entries that should be exported. use
	 *                {
	 * @null} to export all entries
	 * @param type          the exporttype, i.e. whether the entries should be
	 *                      exported to XML, RTF, PDF, TXT etc.<br>
	 *                      use following constants:<br>
	 *                      - CConstants.EXP_TYPE_PDF<br>
	 *                      - CConstants.EXP_TYPE_RTF<br>
	 *                      - CConstants.EXP_TYPE_XML<br>
	 *                      - CConstants.EXP_TYPE_CSV<br>
	 *                      - CConstants.EXP_TYPE_HTML<br>
	 *                      - CConstants.EXP_TYPE_TXT<br>
	 *                      - CConstants.EXP_TYPE_TEX (for LaTex-files)
	 * @param part          which parts of an entry (content, authors, keywords,
	 *                      attachments...) should be exported.
	 * @param csep          the separator-char when exporting the entries to a
	 *                      comma-separated value-file (.csv)
	 * @param n             the treenode of a selected node (entry) within the
	 *                      DesktopFrame. This indicates, which part of the
	 *                      Desktop-Entries should be exportet, i.e. at which node
	 *                      and related children the export of entries starts.
	 * @param separateFiles whether a new, separate file should be created for each
	 *                      note
	 * @param notag         whether formatting-tags should be removed {@code true}
	 *                      or not {@code false}
	 * @param bibtex        whether a separate Bibtex-file containing a
	 *                      bibtex-styles reference list should be created
	 *                      {@code true} or not {@code false}. This file will be
	 *                      created depending on available Bibkeys as attributes of
	 *                      the author-values.
	 * @param highlightkws  whether an entry's keywords sould be highlighted within
	 *                      the entry-content {@code true} or not {@code false}
	 * @param ct            {@code true} if a table of contents should be created at
	 *                      the beginning of the exported file (so you can create a
	 *                      TOC for PDF or HTML-documents), {@code false} otherwise.
	 *                      <b>Only applies when entries are exported from the
	 *                      DesktopFrame</b>
	 * @param ihv           indicates whether the headings (titles) of exported
	 *                      entries should be visible (use {@code true}) or not
	 *                      ({@code false}). <b>Only applies when entries are
	 *                      exported from the DesktopFrame</b>
	 * @param numberprefix  indicates whether entries' titles should have their
	 *                      entry-number included or not.
	 */
	public TaskProgressDialog(java.awt.Frame parent, int task_id, TasksData td, Daten d, Bookmarks bm, DesktopData dt,
			Settings s, BibTeX bto, Synonyms syn, File fp, ArrayList<Object> ee, int type, int part, char csep,
			DefaultMutableTreeNode n, boolean separateFiles, boolean notag, boolean bibtex, boolean highlightkws,
			boolean ct, boolean ihv, boolean numberprefix) {
		super(parent);
		// store parameters
		daten = d;
		settings = s;
		bibTeX = bto;
		tasksData = td;
		desktopData = dt;
		bookmarks = bm;
		synonyms = syn;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_EXPORTDATA:
			switch (type) {
			case Constants.EXP_TYPE_TXT:
			case Constants.EXP_TYPE_DESKTOP_TXT:
				foregroundTask = exportDataToTxt(fp, ee, type, part, n, bibtex, ihv, numberprefix, separateFiles);
				break;
			case Constants.EXP_TYPE_MD:
			case Constants.EXP_TYPE_DESKTOP_MD:
				foregroundTask = exportDataToMd(fp, ee, type, part, n, bibtex, ihv, numberprefix, separateFiles);
				break;
			case Constants.EXP_TYPE_TEX:
			case Constants.EXP_TYPE_DESKTOP_TEX:
				foregroundTask = exportDataToTex(fp, ee, type, part, n, bibtex, ihv, numberprefix, ct, separateFiles);
				break;
			case Constants.EXP_TYPE_HTML:
			case Constants.EXP_TYPE_RTF:
			case Constants.EXP_TYPE_ODT:
			case Constants.EXP_TYPE_DOCX:
			case Constants.EXP_TYPE_EPUB:
			case Constants.EXP_TYPE_DESKTOP_HTML:
			case Constants.EXP_TYPE_DESKTOP_RTF:
			case Constants.EXP_TYPE_DESKTOP_ODT:
			case Constants.EXP_TYPE_DESKTOP_DOCX:
			case Constants.EXP_TYPE_DESKTOP_EPUB:
				foregroundTask = exportDataToHtml(fp, ee, type, part, n, bibtex, ihv, highlightkws, numberprefix, ct);
				break;
			case Constants.EXP_TYPE_XML:
				foregroundTask = exportDataToXml(fp, ee, part, bibtex, notag);
				break;
			case Constants.EXP_TYPE_CSV:
				foregroundTask = exportDataToCsv(fp, ee, part, csep, notag, bibtex);
				break;
			case Constants.EXP_TYPE_ZKN3:
				foregroundTask = exportDataToZkn(fp, ee, bibtex);
				break;
			default:
				break;
			}
			break;
		}
		startTask();
	}

	public TaskProgressDialog(java.awt.Frame parent, int task_id, TasksData td, Daten d, Bookmarks bm, DesktopData dt,
			SearchRequests sr, Settings s, int type, File fp, String sepchar, boolean atou, boolean appendit,
			String dts, URL url) {
		super(parent);
		// store parameters
		daten = d;
		settings = s;
		tasksData = td;
		desktopData = dt;
		bookmarks = bm;
		searchRequests = sr;

		initComponents();
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// init the progress bar and status icon for
		// the swingworker background thread
		// creates a new class object. This variable is not used, it just associates
		// task monitors to
		// the background tasks. furthermore, by doing this, this class object also
		// animates the
		// busy icon and the progress bar of this frame.
		InitStatusbarForTasks isb = new InitStatusbarForTasks(null, progressBar, null);
		// check which task was requested and start that task
		switch (task_id) {
		case TASK_IMPORTDATA:
			switch (type) {
			case Constants.TYPE_ZKN3:
				foregroundTask = importFromZkx(fp, dts);
				break;
			case Constants.TYPE_ZKN:
				foregroundTask = importFromZkn(fp, atou, appendit, dts);
				break;
			case Constants.TYPE_CSV:
				foregroundTask = importFromCSV(fp, sepchar, atou, appendit, dts);
				break;
			default:
				break;
			}
			break;
		}
		startTask();
	}

	private void startTask() {
		ApplicationContext appC = Application.getInstance().getContext();
		taskMonitor = appC.getTaskMonitor();
		TaskService taskService = appC.getTaskService();

		// Execute the task and bring it to the foreground to make the animated progress
		// bar and the busy icon visible.
		taskService.execute(foregroundTask);
		taskMonitor.setForegroundTask(foregroundTask);
	}

	private Task exportDataToTxt(File fp, ArrayList<Object> ee, int type, int part, DefaultMutableTreeNode n,
			boolean bibtex, boolean ihv, boolean numberprefix, boolean separateFile) {
		return new ExportToTxtTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, desktopData, settings, bibTeX, fp, ee, type, part, n, bibtex, ihv,
				numberprefix, separateFile);
	}

	private Task exportDataToMd(File fp, ArrayList<Object> ee, int type, int part, DefaultMutableTreeNode n,
			boolean bibtex, boolean ihv, boolean numberprefix, boolean separateFile) {
		return new ExportToMdTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, desktopData, settings, bibTeX, fp, ee, type, part, n, bibtex, ihv,
				numberprefix, separateFile);
	}

	private Task exportDataToTex(File fp, ArrayList<Object> ee, int type, int part, DefaultMutableTreeNode n,
			boolean bibtex, boolean ihv, boolean numberprefix, boolean contenttable, boolean separateFile) {
		return new ExportToTexTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, desktopData, settings, bibTeX, fp, ee, type, part, n, bibtex, ihv,
				numberprefix, contenttable, separateFile);
	}

	private Task exportDataToHtml(File fp, ArrayList<Object> ee, int type, int part, DefaultMutableTreeNode n,
			boolean bibtex, boolean ihv, boolean hkws, boolean numberprefix, boolean toc) {
		return new ExportToHtmlTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, desktopData, settings, bibTeX, fp, ee, type, part, n, bibtex, ihv,
				hkws, numberprefix, toc);
	}

	private Task exportDataToXml(File fp, ArrayList<Object> ee, int part, boolean bibtex, boolean removeformattags) {
		return new ExportToXmlTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bibTeX, fp, ee, part, bibtex, removeformattags);
	}

	private Task exportDataToCsv(File fp, ArrayList<Object> ee, int part, char sep, boolean removeformattags,
			boolean bibtex) {
		return new ExportToCsvTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bibTeX, fp, ee, part, sep, removeformattags, bibtex);
	}

	private Task exportDataToZkn(File fp, ArrayList<Object> ee, boolean bibtex) {
		return new ExportToZknTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bookmarks, bibTeX, bibtex, fp, ee);
	}

	private Task importFromZkx(File fp, String defaulttimestamp) {
		return new ImportFromZkx(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bookmarks, desktopData, searchRequests, fp, defaulttimestamp);
	}

	private Task importFromZkn(File fp, boolean asciiToUnicode, boolean appendit, String defaulttimestamp) {
		return new ImportFromZkn(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bookmarks, desktopData, searchRequests, settings, fp, asciiToUnicode,
				appendit, defaulttimestamp);
	}

	private Task importFromCSV(File fp, String sepchar, boolean asciiToUnicode, boolean appendit,
			String defaulttimestamp) {
		return new ImportFromCSV(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bookmarks, desktopData, searchRequests, settings, fp, sepchar,
				asciiToUnicode, appendit, defaulttimestamp);
	}

	/**
	 *
	 * @param oldauthors
	 * @param newauthors
	 * @param authortable
	 * @param selectedrow
	 * @param linkedauthors
	 * @return
	 */
	private Task mergeAuthors(String oldauthors, String newauthors, String newbibkey, JTable authortable,
			int selectedrow, LinkedList<Object[]> linkedauthors) {
		return new MergeAuthorsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, tasksData, oldauthors, newauthors, newbibkey, authortable, selectedrow,
				linkedauthors);
	}

	/**
	 *
	 * @param oldkeywords
	 * @param newkeywords
	 * @param kwtable
	 * @param selectedrow
	 * @param linkedkeywords
	 * @return
	 */
	private Task mergeKeywords(String oldkeywords, String newkeywords, JTable kwtable, int selectedrow,
			LinkedList<Object[]> linkedkeywords) {
		return new MergeKeywordsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, tasksData, oldkeywords, newkeywords, kwtable, selectedrow, linkedkeywords);
	}

	/**
	 *
	 * @param eqi
	 * @param step
	 * @param sel
	 * @param rest
	 * @param ffs
	 * @param t
	 * @return
	 */
	private Task keywordSuggestions(int eqi, int step, LinkedList<String> sel, LinkedList<String> rest,
			LinkedList<String> ffs, String t) {
		return new KeywordSuggestionsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, synonyms, settings, eqi, step, sel, rest, ffs, t);
	}

	/**
	 *
	 * @param mo
	 * @return
	 */
	private Task setFirstLineAsTitle(int mo) {
		return new SetFirstLineAsTitleTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, mo);
	}

	/**
	 *
	 * @param entries
	 * @return
	 */
	private Task deleteEntry(int[] entries) {
		return new DeleteEntryTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, searchRequests, entries);
	}

	/**
	 *
	 * @param entries
	 * @param insertpos
	 * @return
	 */
	private Task entriesToLuhmann(int[] entries, int insertpos) {
		return new EntriesToLuhmannTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, entries, insertpos);
	}

	/**
	 *
	 * @param authors
	 * @return
	 */
	private Task deleteAuthors(String[] authors) {
		return new DeleteAuthorsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, authors);
	}

	/**
	 *
	 * @param keywords
	 * @return
	 */
	private Task deleteKeywords(String[] keywords) {
		return new DeleteKeywordsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, keywords);
	}

	/**
	 *
	 * @param et
	 * @param dtm
	 * @return
	 */
	private Task showAuthors(int et, DefaultTableModel dtm) {
		return new ShowAuthorListTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, bibTeX, et, dtm);
	}

	/**
	 *
	 * @param dtm
	 * @return
	 */
	private Task showAttachments(DefaultTableModel dtm) {
		return new ShowAttachmentListTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, settings, dtm);
	}

	/**
	 *
	 * @param fs
	 * @param rs
	 * @param re
	 * @param w
	 * @param ww
	 * @param mc
	 * @param rex
	 * @return
	 */
	private Task replace(String fs, String rs, int[] re, int w, boolean ww, boolean mc, boolean rex) {
		return new ReplaceTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, fs, rs, re, w, ww, mc, rex);
	}

	/**
	 *
	 * @param includeSynonyms
	 * @return
	 */
	private Task showKeywords(boolean includeSynonyms, DefaultTableModel dtm) {
		return new ShowKeywordListTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, synonyms, includeSynonyms, dtm);
	}

	/**
	 *
	 * @param conv
	 * @return
	 */
	private Task convertFormatTags(int conv) {
		return new ConvertFormatTagsTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, conv);
	}

	/**
	 *
	 * @param dtm
	 * @return
	 */
	private Task showTitles(DefaultTableModel dtm) {
		return new ShowTitleListTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, dtm);
	}

	/**
	 *
	 * @param resetFreq
	 * @return
	 */
	private Task updateFile(boolean resetFreq) {
		return new UpdateFileTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, desktopData, bibTeX, resetFreq);
	}

	/**
	 *
	 * @return
	 */
	private Task loadFile() {
		// initiate the "statusbar" (the loading splash screen), giving visual
		// feedback during open and save operations
		return new LoadFileTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, bookmarks, searchRequests, desktopData, synonyms, settings, bibTeX);
	}

	private Task refreshBibTex() {
		return new RefreshBibTexTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, tasksData, daten, bibTeX);
	}

	/**
	 *
	 * @return
	 */
	private Task saveFile() {
		return new SaveFileTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, bookmarks, searchRequests, desktopData, synonyms, settings, bibTeX);
	}

	/**
	 * @param tos               Type of Search (in entries, entries w/o keywords
	 *                          etc.)
	 * @param s                 a string-array containing search terms
	 * @param se                an integer array containing the entry-numbers of
	 *                          those entries where the search should be applied to
	 * @param w                 where the search should be applied to, i.e. search
	 *                          within content, keywords, authors etc.
	 * @param l                 the logical-combination of the search
	 * @param ww                pass true, if the search should find whole words
	 *                          only
	 * @param mc                whether the search is case sensitive (true) or not
	 *                          (false)
	 * @param syn               whether the search should include synonyms or not
	 * @param accentInsensitive whether the search should be accent insensitive
	 * @param rex               whether the search terms contain regular expressions
	 *                          or not
	 * @param ts                whether the user requested a time-search, i.e. a
	 *                          search for entries that were created or changed
	 *                          within a certain period
	 * @param fr                the start of the period, when a timesearch is
	 *                          requested. format: "yymmdd".
	 * @param to                the end of the period, when a timesearch is
	 *                          requested. format: "yymmdd".
	 * @param tsi               the timestampindex, which indicate whether the user
	 *                          wants to search only for entries within a period of
	 *                          <i>creation</i> date (0), of <i>edited</i> date (1)
	 *                          or both (2).
	 * @param donly             whether the search should open the
	 *                          CSearchResults-frame (false), or whether the
	 *                          search-results are used for other purposes, like
	 *                          e.g. putting the results to the desktop (true)
	 * @param rt                Whether tags should be removed from entry content
	 *                          before searching the entry. Increases speed,
	 *                          however, some words may not be found (which have
	 *                          tags inside a word to emphasize a word part, like
	 *                          <i>Zettel</i>kasten.
	 * @return
	 */
	private Task startSearch(int tos, String[] s, int[] se, int w, int l, boolean ww, boolean mc, boolean syn,
			boolean accentInsensitive, boolean rex, boolean ts, String fr, String to, int tsi, boolean donly,
			boolean rt) {
		// initiate the "statusbar" (the loading splash screen), giving visiual
		// feedback during open and save operations
		return new StartSearchTask(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class),
				this, msgLabel, daten, searchRequests, synonyms, tos, s, se, w, l, ww, mc, syn, accentInsensitive, rex,
				ts, fr, to, tsi, donly, rt);
	}

	/**
	 *
	 */
	@Action
	public void cancel() {
		if ((taskMonitor.getForegroundTask() != null) && !taskMonitor.getForegroundTask().isDone()) {
			taskMonitor.getForegroundTask().cancel(true);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		progressBar = new javax.swing.JProgressBar();
		cancelButton = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		msgLabel = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setName("FormTaskProgressDlg"); // NOI18N
		setResizable(false);

		jPanel1.setName("jPanel1"); // NOI18N

		jPanel2.setName("jPanel2"); // NOI18N

		progressBar.setName("progressBar"); // NOI18N

		javax.swing.ActionMap actionMap = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
				.getActionMap(TaskProgressDialog.class, this);
		cancelButton.setAction(actionMap.get("cancel")); // NOI18N
		cancelButton.setName("cancelButton"); // NOI18N

		org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
								.add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cancelButton)));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
						.add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(cancelButton)));

		jPanel3.setName("jPanel3"); // NOI18N

		msgLabel.setName("msgLabel"); // NOI18N
		msgLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

		org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
				.getResourceMap(TaskProgressDialog.class);
		jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
		jLabel1.setName("jLabel1"); // NOI18N

		org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(jPanel3Layout.createSequentialGroup().add(jLabel1)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(msgLabel,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.add(msgLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				jPanel1Layout.createSequentialGroup().addContainerGap()
						.add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
								.add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(jPanel1Layout.createSequentialGroup().addContainerGap()
						.add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JLabel msgLabel;
	private javax.swing.JProgressBar progressBar;
	// End of variables declaration//GEN-END:variables

}
