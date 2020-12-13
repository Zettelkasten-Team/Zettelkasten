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

import de.danielluedecke.zettelkasten.ZettelkastenView;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * This class contains generic constant values that are used all over the
 * application.
 *
 *
 * @author danielludecke
 */
public class Constants {

    /*
     * This variable stores the current programme and build version number
     */
    // public static final String BUILD_VERSION = "3.2.8 (Build 20180603)";
    /**
     * This constants stores the website-address where the Zettelkasten can be
     * downloaded:<br><br>
     * {@code http://zettelkasten.danielluedecke.de/download.php}
     */
    public static final String UPDATE_URI = "http://zettelkasten.danielluedecke.de/download.php";
    /**
     * This constants stores the address of the file that contains update
     * information for the current Zettelkasten:<br><br>
     * {@code http://zettelkasten.danielluedecke.de/update.txt}
     */
    public static final String UPDATE_INFO_URI = "http://zettelkasten.danielluedecke.de/update.txt";
    /**
     * This constants stores the website-address where the Zettelkasten nightly
     * builds can be downloaded:<br><br>
     * {@code http://zettelkasten.danielluedecke.de/nightly.php}
     */
    public static final String UPDATE_NIGHTLY_URI = "http://zettelkasten.danielluedecke.de/nightly.php";
    /**
     * This constants stores the address of the file that contains update
     * information for the nightly Zettelkasten builds:<br><br>
     * {@code http://zettelkasten.danielluedecke.de/updatenightly.txt}
     */
    public static final String UPDATE_NIGHTLY_INFO_URI = "http://zettelkasten.danielluedecke.de/updatenightly.txt";
    /**
     * A constant indicating the current file-extension of the data file
     */
    public static final String ZKN_FILEEXTENSION = ".zkn3";
    /**
     * A constant indicating the file-extension of exported data file
     */
    public static final String ZKX_FILEEXTENSION = ".zkx3";
    /**
     * A constant indicating the current file-extension of the backup file
     */
    public static final String ZKN_BACKUPFILEEXTENSION = ".zkb3";
    /**
     * Subdirectory of the data-file, where images are stored by default
     */
    public static final String IMAGEPATH_SUBDIR = "img";
    /**
     * Subdirectory of the data-file, where form-images are stored by default
     */
    public static final String FORMIMAGEPATH_SUBDIR = "forms";
    /**
     * the appendinx to form-image-filenames, when we have the large-scaled
     * form-image for export.
     */
    public static final String FORMIMAGE_LARGE_APPENDIX = "_large";
    /**
     * File type of created form images
     */
    public static final String FORMIMAGE_EXTENSION = ".png";
    public static boolean isJava7OnMac = System.getProperty("java.version").startsWith("1.7") && System.getProperty("os.name").startsWith("Mac OS");
    public static boolean isJava8OnMac = System.getProperty("java.version").startsWith("1.8") && System.getProperty("os.name").startsWith("Mac OS");
    /**
     * This constant determines the interval of the timer that is responsible
     * for the automatic backup of the data file.
     */
    public static final long autobackupUpdateInterval = 300000;
    /**
     * This constant determines when the timer, that is responsible for the
     * automatic backup of the data file, should start for the first time.
     */
    public static final long autobackupUpdateStart = 300000;
    /**
     * Constant for the filename of the main data-xml-file
     */
    public static final String zknFileName = "zknFile.xml";
    /**
     * Constant for the filename of the author-xml-file
     */
    public static final String authorFileName = "authorFile.xml";
    /**
     * Constant for the filename of the keyword-xml-file
     */
    public static final String keywordFileName = "keywordFile.xml";
    /**
     * Constant for the filename of the metainformation-xml-file
     */
    public static final String metainfFileName = "metaInformation.xml";
    /**
     * Constant for the filename of ^the bookmarks-xml-file
     */
    public static final String bookmarksFileName = "bookmarks.xml";
    /**
     * Constant for the filename of the searchrequest-xml-file
     */
    public static final String searchrequestsFileName = "searchrequests.xml";
    /**
     * Constant for the filename of the desktop-xml-file
     */
    public static final String desktopFileName = "desktop.xml";
    /**
     * Constant for the filename of the desktop-modified-entries-xml-file
     */
    public static final String desktopModifiedEntriesFileName = "desktopme.xml";
    /**
     * Constant for the filename of the desktop-notes-xml-file
     */
    public static final String desktopNotesFileName = "desktopnt.xml";
    /**
     * Constant for the filname of archived desktop-files
     */
    public static final String archivedDesktopFileName = "archivedDesktop.xml";

    public static final String seaGlassLookAndFeelClassName = "Sea Glass";
    public static final Dimension seaGlassButtonDimension = new Dimension(42, 29);
    public static final int seaGlassToolbarHeight = 42;

    /**
     * Constant for the filename of the settings-xml-file
     */
    public static final String settingsFileName = "settings.xml";
    /**
     * Constant for the filename of the foreign-words-xml-file
     */
    public static final String foreignWordsName = "foreignwords.xml";
    /**
     * Constant for the filename of the synonyms-xml-file
     */
    public static final String synonymsFileName = "synonyms.xml";
    /**
     * Constant for the filename of the bibtex-bib-file
     */
    public static final String bibTexFileName = "references.bib";
    /**
     * Constant for the filename of the steno-xml-file
     */
    public static final String stenoFileName = "steno.xml";
    /**
     * Constant for the filename of the synonyms-xml-file
     */
    public static final String autoKorrekturFileName = "autokorrektur.xml";
    /**
     * Constant for the filename of the main acceleratorkeys-xml-file
     */
    public static final String acceleratorKeysMainName = "acceleratorKeysMain.xml";
    /**
     * Constant for the filename of the new entry acceleratorkeys-xml-file
     */
    public static final String acceleratorKeysNewEntryName = "acceleratorKeysNewEntry.xml";
    /**
     * Constant for the filename of the main acceleratorkeys-xml-file
     */
    public static final String acceleratorKeysDesktopName = "acceleratorKeysDesktop.xml";
    /**
     * Constant for the filename of the new entry acceleratorkeys-xml-file
     */
    public static final String acceleratorKeysSearchResultsName = "acceleratorKeysSearchResults.xml";

    /**
     * A constant identifying which type of data was dragged and dropped. In
     * this case, the component is about to receive entry-numbers as
     * comma-separated string.
     */
    public static final String DRAG_SOURCE_TYPE_ENTRIES = "entries";
    /**
     * A constant identifying which type of data was dragged and dropped. In
     * this case, the component is about to receive a node that was dragged
     * inside this jTree.
     */
    public static final String DRAG_SOURCE_TYPE_NODE = "node";
    /**
     * A constant identifying which type of data was dragged and dropped. In
     * this case, the component is about to receive a bullet-point with its
     * value stored as string.
     */
    public static final String DRAG_SOURCE_TYPE_BULLET = "bullet";
    /**
     * A constant identifying which type of data was dragged and dropped. In
     * this case, the component is about to receive keyword-index-numbers as
     * comma-separated string.
     */
    public static final String DRAG_SOURCE_TYPE_KEYWORDS = "keywords";
    /**
     * A constant identifying which type of data was dragged and dropped. In
     * this case, the component is about to receive author-index-numbers as
     * comma-separated string.
     */
    public static final String DRAG_SOURCE_TYPE_AUTHORS = "ZKN3_drag_authors";
    /**
     * A constant used for identifying the source of a drag&drop-operation. in
     * this case, the drag-source was the jTreeDesktop (mainwindows-frame)
     */
    public static final String DRAG_SOURCE_JTREEDESKTOP = "jTreeDesktop";
    /**
     * A constant used for identifying the source of a drag&drop-operation. in
     * this case, the drag-source was the jTreeLuhmann (mainwindows-frame)
     */
    public static final String DRAG_SOURCE_JTREELUHMANN = "jTreeLuhmann";
    public static final String ROOT_ID_NAME = "root";

    // Here we have the formatting tags which are used in the NewEntryFrame
    public static final String FORMAT_BOLD_OPEN = "[f]";
    public static final String FORMAT_BOLD_CLOSE = "[/f]";
    public static final String FORMAT_MD_BOLD_OPEN = "**";
    public static final String FORMAT_MD_BOLD_CLOSE = "**";
    public static final String FORMAT_ITALIC_OPEN = "[k]";
    public static final String FORMAT_ITALIC_CLOSE = "[/k]";
    public static final String FORMAT_MD_ITALIC_OPEN = "_";
    public static final String FORMAT_MD_ITALIC_CLOSE = "_";
    public static final String FORMAT_UNDERLINE_OPEN = "[u]";
    public static final String FORMAT_UNDERLINE_CLOSE = "[/u]";
    public static final String FORMAT_STRIKE_OPEN = "[d]";
    public static final String FORMAT_STRIKE_CLOSE = "[/d]";
    public static final String FORMAT_MD_STRIKE_OPEN = "---";
    public static final String FORMAT_MD_STRIKE_CLOSE = "---";
    public static final String FORMAT_ALIGNCENTER_OPEN = "[c]";
    public static final String FORMAT_ALIGNCENTER_CLOSE = "[/c]";
    public static final String FORMAT_ALIGNLEFT_OPEN = "[al]";
    public static final String FORMAT_ALIGNLEFT_CLOSE = "[/al]";
    public static final String FORMAT_ALIGNRIGHT_OPEN = "[ar]";
    public static final String FORMAT_ALIGNRIGHT_CLOSE = "[/ar]";
    public static final String FORMAT_ALIGNJUSTIFY_OPEN = "[ab]";
    public static final String FORMAT_ALIGNJUSTIFY_CLOSE = "[/ab]";
    public static final String FORMAT_LIST_OPEN = "[l]";
    public static final String FORMAT_LIST_CLOSE = "[/l]";
    public static final String FORMAT_NUMBEREDLIST_OPEN = "[n]";
    public static final String FORMAT_NUMBEREDLIST_CLOSE = "[/n]";
    public static final String FORMAT_LISTITEM_OPEN = "[*]";
    public static final String FORMAT_LISTITEM_CLOSE = "[/*]";
    public static final String FORMAT_FORM_TAG = "[form";
    public static final String FORMAT_NEWLINE = "[br]";
    public static final String FORMAT_FOOTNOTE_OPEN = "[fn ";
    public static final String FORMAT_SUP_OPEN = "[sup]";
    public static final String FORMAT_SUP_CLOSE = "[/sup]";
    public static final String FORMAT_SUB_OPEN = "[sub]";
    public static final String FORMAT_SUB_CLOSE = "[/sub]";
    public static final String FORMAT_FONT_OPEN = "[font";
    public static final String FORMAT_FONT_CLOSE = "[/font]";
    public static final String FORMAT_QUOTE_OPEN = "[q]";
    public static final String FORMAT_QUOTE_CLOSE = "[/q]";
    public static final String FORMAT_QUOTEMARK_OPEN = "[qm]";
    public static final String FORMAT_QUOTEMARK_CLOSE = "[/qm]";
    public static final String FORMAT_MD_QUOTE_OPEN = "> ";
    public static final String FORMAT_MD_QUOTE_CLOSE = "";
    public static final String FORMAT_MD_CODE_OPEN = "`";
    public static final String FORMAT_MD_CODE_CLOSE = "`";
    public static final String FORMAT_CODE_OPEN = "[code]";
    public static final String FORMAT_CODE_CLOSE = "[/code]";
    public static final String FORMAT_H1_OPEN = "[h1]";
    public static final String FORMAT_H1_CLOSE = "[/h1]";
    public static final String FORMAT_H2_OPEN = "[h2]";
    public static final String FORMAT_H2_CLOSE = "[/h2]";
    public static final String FORMAT_MD_H1_OPEN = "# ";
    public static final String FORMAT_MD_H1_CLOSE = "";
    public static final String FORMAT_MD_H2_OPEN = "## ";
    public static final String FORMAT_MD_H2_CLOSE = "";
    public static final String FORMAT_TABLECAPTION_OPEN = "[tc]";
    public static final String FORMAT_TABLECAPTION_CLOSE = "[/tc]";
    public static final String FORMAT_MANLINK_OPEN = "[z";
    public static final String FORMAT_MANLINK_CLOSE = "[/z]";
    public static final String FORMAT_IMG_OPEN = "[img]";
    public static final String FORMAT_IMG_CLOSE = "[/img]";
    public static final String FORMAT_MD_IMG_OPEN = "![Bild](";
    public static final String FORMAT_MD_IMG_CLOSE = ")";

    public static final String footnoteHtmlTag = "<a class=\"fnlink\" href=\"#fn_";

    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_TITLE = 1 << 1;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_CONTENT = 1 << 2;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_AUTHOR = 1 << 3;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_KEYWORDS = 1 << 4;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_LINKS = 1 << 5;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_REMARKS = 1 << 6;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_LINKCONTENT = 1 << 7;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_TIMESTAMP_CREATED = 1 << 8;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_TIMESTAMP_EDITED = 1 << 9;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_REFERRERS = 1 << 10;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_CLUSTER = 1 << 11;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_BOOKMARKS = 1 << 12;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_LUHMANN = 1 << 13;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_DESKTOP = 1 << 14;
    /**
     * Constants that show which parts of an entry the user wants to apply a
     * search-request. we use binary shift options here, so we can OR the
     * values.
     */
    public static final int SEARCH_RATINGS = 1 << 15;

    /**
     * Constants that show which search options the user chose. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int SEARCH_OPTION_MATCHCASE = 1 << 1;
    /**
     * Constants that show which search options the user chose. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int SEARCH_OPTION_WHOLEWORD = 1 << 2;
    /**
     * Constants that show which search options the user chose. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int SEARCH_OPTION_SYNONYMS = 1 << 3;
    /**
     * Indicates a typical search request.
     */
    public static final int SEARCH_USUAL = 1;
    /**
     * indicates that the user is searching for entries without authors
     */
    public static final int SEARCH_NO_AUTHORS = 2;
    /**
     * indicates that the user is searching for entries without keywords
     */
    public static final int SEARCH_NO_KEYWORDS = 3;
    /**
     * indicates that the user is searching for entries without remarks
     */
    public static final int SEARCH_NO_REMARKS = 4;
    /**
     * indicates that the user is searching for entries with attachments
     */
    public static final int SEARCH_WITH_ATTACHMENTS = 5;
    /**
     * indicates that the user is searching for entries with remarks
     */
    public static final int SEARCH_WITH_REMARKS = 6;
    /**
     * indicates that the user is searching for entries that lie within a
     * certain created-period
     */
    public static final int SEARCH_WITH_CREATED_TIME = 7;
    /**
     * indicates that the user is searching for entries that lie within a
     * certain edit-period
     */
    public static final int SEARCH_WITH_EDITED_TIME = 8;
    /**
     * indicates that the user is searching for entries that have been rated
     */
    public static final int SEARCH_WITH_RATINGS = 9;
    /**
     * indicates that the user is searching for entries that have <b>not</b>
     * been rated
     */
    public static final int SEARCH_WITHOUT_RATINGS = 10;
    /**
     * indicates that user wants to search entries
     * that are top-level-entries in a note sequence
     * (trailing entries, Folgezettel)
     */
    public static final int SEARCH_TOP_LEVEL_LUHMANN = 11;
    /**
     * indicates that user wants to search entries
     * that are in a note sequence
     * (trailing entries, Folgezettel)
     */
    public static final int SEARCH_IS_LUHMANN_PARENT = 12;
    /**
     * indicates that user wants to search entries
     * that are in a note sequence
     * (trailing entries, Folgezettel)
     */
    public static final int SEARCH_IS_ANY_LUHMANN = 13;
    /**
     * indicates that user wants to search entries
     * without any manual links
     */
    public static final int SEARCH_WITHOUT_MANUAL_LINKS = 14;
    /**
     * Indicates a logical-and-combined search
     */
    public static final int LOG_AND = 1;
    /**
     * Indicates a logical-or-combined search
     */
    public static final int LOG_OR = 2;
    /**
     * Indicates a logical-not-combined search
     */
    public static final int LOG_NOT = 3;

    public static final int FRAME_MAIN = 1;
    public static final int FRAME_SEARCH = 2;
    public static final int FRAME_NEWENTRY = 3;
    public static final int FRAME_DESKTOP = 4;

    public static final int MIN_SIDEBAR_SIZE = 250;

    /**
     * this is a usual search, that means the search results are added to the
     * CSearchResults-dialog-frame and displayed.<br><br>
     * We use the startSearch method for other purposes as well, for instance to
     * retrieve entries that should be added to the desktop or as manual links.
     * in this case, we need another constant to indicate that the search for
     * entries is not meant that search results should be displayed in the
     * search results window.
     */
    public static final int STARTSEARCH_USUAL = 1;
    /**
     * this is a desktop-search, that means the search results are *not* shown
     * in the searchresults-window. instead, they are added to the
     * desktop.<br><br>
     * We use the startSearch method for different purposes, for instance to
     * retrieve entries that should be added to the desktop or as manual links.
     * depending on the purpose, we need the related constant to indicate that
     * the search for entries is not meant that search results should be
     * displayed in the search results window.
     */
    public static final int STARTSEARCH_DESKTOP = 2;
    /**
     * this is a luhmann-search, that means the search results are *not* shown
     * in the searchresults-window. instead, they are added as follower-numbers
     * to the current entry.<br><br>
     * We use the startSearch method for different purposes, for instance to
     * retrieve entries that should be added to the desktop or as manual links.
     * depending on the purpose, we need the related constant to indicate that
     * the search for entries is not meant that search results should be
     * displayed in the search results window.
     */
    public static final int STARTSEARCH_LUHMANN = 3;
    /**
     * this is a manlink-search, that means the search results are *not* shown
     * in the searchresults-window. instead, they are added as manual links to
     * the current entry.<br><br>
     * We use the startSearch method for different purposes, for instance to
     * retrieve entries that should be added to the desktop or as manual links.
     * depending on the purpose, we need the related constant to indicate that
     * the search for entries is not meant that search results should be
     * displayed in the search results window.
     */
    public static final int STARTSEARCH_MANLINK = 4;

    /**
     * Indicates that a search request focusses on certain entries that have
     * been created during a certain period.
     */
    public static final int TIMESTAMP_CREATED = 0;
    /**
     * Indicates that a search request focusses on certain entries that have
     * been edited during a certain period.
     */
    public static final int TIMESTAMP_EDITED = 1;
    /**
     * Indicates that a search request focusses on certain entries that have
     * either been created or edited during a certain period.
     */
    public static final int TIMESTAMP_BOTH = 2;

    /**
     * Used for the CBiggerEditField to indicate which kind of data is being
     * edited in the window
     */
    public static final int EDIT_OTHER = 0;
    /**
     * Used for the CBiggerEditField to indicate which kind of data is being
     * edited in the window
     */
    public static final int EDIT_AUTHOR = 1;
    /**
     * Used for the CBiggerEditField to indicate which kind of data is being
     * edited in the window
     */
    public static final int EDIT_KEYWORD = 2;
    /**
     * Used for the CBiggerEditField to indicate which kind of data is being
     * edited in the window
     */
    public static final int EDIT_DESKTOP_COMMENT = 3;
    /**
     * Used for the CBiggerEditField to indicate which kind of data is being
     * edited in the window
     */
    public static final int EDIT_STYLESHEET = 4;

    /**
     * Constant that indicates that the user wants large icons for the toolbar.
     */
    public static final int TOOLBAR_SIZE_LARGE = 0;
    /**
     * Constant that indicates that the user wants medium sized icons for the
     * toolbar.
     */
    public static final int TOOLBAR_SIZE_MEDIUM = 1;
    /**
     * Constant that indicates that the user wants small sized icons for the
     * toolbar.
     */
    public static final int TOOLBAR_SIZE_SMALL = 2;
    /**
     * Constant that indicates that the user wants to hide the toolbar.
     */
    public static final int TOOLBAR_NONE = 3;

    /**
     * Constants that indicate that the remarks of an entry should also be
     * displayed in the desktop-view
     */
    public static final int DESKTOP_SHOW_REMARKS = 1 << 1;
    /**
     * Constants that indicate that the authors of an entry should also be
     * displayed in the desktop-view
     */
    public static final int DESKTOP_SHOW_AUTHORS = 1 << 2;
    /**
     * Constants that indicate that the attachments of an entry should also be
     * displayed in the desktop-view
     */
    public static final int DESKTOP_SHOW_ATTACHMENTS = 1 << 3;
    /**
     * Constants that indicate that the keywords of an entry should also be
     * displayed in the desktop-view
     */
    public static final int DESKTOP_SHOW_KEYWORDS = 1 << 4;
    /**
     * Indicates whether the comments should be displayed in the desktop-window,
     * if desktop-entries have comments.
     */
    public static final int DESKTOP_WITH_COMMENTS = 0;
    /**
     * Indicates whether no comments at all should be displayed in the
     * desktop-window, independent from whether desktop-entries have comments or
     * not
     */
    public static final int DESKTOP_WITHOUT_COMMENTS = 1;
    /**
     * Indicates whether only comments and no entries should be displayed in the
     * desktop-window.
     */
    public static final int DESKTOP_ONLY_COMMENTS = 2;

    /**
     * Constants that indicate which type of data format the user wants to
     * import/export.<br>
     * TYPE_ZKN3 are new Zettelkasten-data-files
     */
    public static final int TYPE_ZKN3 = 1; // new zettelkasten-files
    /**
     * Constants that indicate which type of data format the user wants to
     * import/export.<br>
     * TYPE_ZKN are <i>old</i> Zettelkasten-data-files
     */
    public static final int TYPE_ZKN = 2;  // older zettelkasten-files
    /**
     * Constants that indicate which type of data format the user wants to
     * import/export.<br>
     * TYPE_CSV are are comma separated values, CSV-files, as they can be used
     * in office applications like Excel
     */
    public static final int TYPE_CSV = 3;  // csv-files
    /**
     * Constants that indicate which type of data format the user wants to
     * import/export.<br>
     * TYPE_BIB are are bibtex-files
     */
    public static final int TYPE_BIB = 4;  // bibtex-file
    /**
     * Constants that indicate which type of data format the user wants to
     * import/export.<br>
     * TYPE_XML are xml files
     */
    public static final int TYPE_XML = 5;  // xml-files

    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_TITLE = 1 << 1;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_CONTENT = 1 << 2;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_AUTHOR = 1 << 3;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_KEYWORDS = 1 << 4;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_LINKS = 1 << 5;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_REMARKS = 1 << 6;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_TIMESTAMP = 1 << 7;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_MANLINKS = 1 << 8;
    /**
     * Constants that show which parts the user wants to export. we use binary
     * shift options here, so we can OR the values.
     */
    public static final int EXPORT_LUHMANN = 1 << 9;

    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code RTF} stands for the RTF file format,
     * i.e. RichTextFormat
     */
    public static final int EXP_TYPE_RTF = 2;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code XML} stands for the XML file format
     */
    public static final int EXP_TYPE_XML = 3;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code CSV} stands for the CSV file format,
     * i.e. comma separated values
     */
    public static final int EXP_TYPE_CSV = 4;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code HTML} stands for the HTML file format
     */
    public static final int EXP_TYPE_HTML = 5;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code TXT} stands for the plain text file
     * format
     */
    public static final int EXP_TYPE_TXT = 6;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code TEX} stands for the LaTex file format
     */
    public static final int EXP_TYPE_TEX = 7;
    /**
     * Constants that indicate which type of data format the user wants to
     * export his data to.<br><br> {@code ZKN3} is the current file format, so
     * data exported to this format can be easily re-importet.
     */
    public static final int EXP_TYPE_ZKN3 = 8;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code RTF}
     * stands for the RTF file format, i.e. RichTextFormat
     */
    public static final int EXP_TYPE_DESKTOP_RTF = 10;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code HTML}
     * stands for the HTML file format
     */
    public static final int EXP_TYPE_DESKTOP_HTML = 11;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code TXT}
     * stands for the plain text file format
     */
    public static final int EXP_TYPE_DESKTOP_TXT = 12;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code TEX}
     * stands for the LaTex file format
     */
    public static final int EXP_TYPE_DESKTOP_TEX = 13;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code MD}
     * stands for the Markdown format
     */
    public static final int EXP_TYPE_DESKTOP_MD = 14;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code ODT}
     * stands for the OpenDocument Text file format
     */
    public static final int EXP_TYPE_DESKTOP_ODT = 15;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code DOCX}
     * stands for the Office Open XML file format
     */
    public static final int EXP_TYPE_DESKTOP_DOCX = 16;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code EPUB}
     * stands for the ePub file format
     */
    public static final int EXP_TYPE_DESKTOP_EPUB = 17;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code MD}
     * stands for the Markdown format
     */
    public static final int EXP_TYPE_MD = 18;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code ODT}
     * stands for the OpenDocument Text file format
     */
    public static final int EXP_TYPE_ODT = 19;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code DOCX}
     * stands for the Office Open XML file format
     */
    public static final int EXP_TYPE_DOCX = 20;
    /**
     * Constants that indicate which type of data format the user wants to
     * export <b>the desktop-content</b> (outliner) to.<br><br> {@code EPUB}
     * stands for the ePub file format
     */
    public static final int EXP_TYPE_EPUB = 21;

    /**
     * Indicates whether the user wants to export the desktop-data
     * <i>without</i> any comments.
     */
    public static final int EXP_COMMENTS_NO = 0;
    /**
     * Indicates whether the user wants to export the desktop-data <i>with</i>
     * any comments.
     */
    public static final int EXP_COMMENTS_YES = 1;
    /**
     * Indicates whether the user wants to export only those desktop-entries
     * that have any comments at all.
     */
    public static final int EXP_COMMENTS_ONLY = 2;

    /**
     * Constant to define tha the main textfield "entry" in the new entry and
     * main dialog has the focus.
     */
    public static final int FOCUS_FIELD_TEXT = 1;
    /**
     * Constant to define that the textfield "title" in the new entry dialog has
     * the focus.
     */
    public static final int FOCUS_FIELD_TITLE = 2;
    /**
     * Constant to define that the textfield "authors" in the new entry and main
     * dialog has the focus.
     */
    public static final int FOCUS_FIELD_AUTHOR = 3;
    /**
     * Constant to define that textfield "addkeywords" in the new entry dialog
     * has the focus.
     */
    public static final int FOCUS_FIELD_ADDKEYWORDS = 4;
    /**
     * Constant to define that textfield "remarks" in the new entry and main
     * dialog has the focus.
     */
    public static final int FOCUS_FIELD_REMARKS = 5;
    /**
     * Constant to define which textfield in the new entry dialog has the focus.
     */
    public static final int FOCUS_FIELD_ADDLINK = 6;
    /**
     * Constant to define that the entry's keywords-list has the focus.
     */
    public static final int FOCUS_FIELD_ENTRYKEYWORDS = 7;
    /**
     * Constant to define that the jTableAuthor in the mainframe's tabbed pane
     * has the focus
     */
    public static final int FOCUS_TABLE_AUTHORS = 8;
    /**
     * Constant to define that the jTableKeywords in the mainframe's tabbed pane
     * has the focus
     */
    public static final int FOCUS_TABLE_KEYWORDS = 9;
    /**
     * Constant to define that the jTableTitles in the mainframe's tabbed pane
     * has the focus
     */
    public static final int FOCUS_TABLE_TITLES = 10;
    /**
     * Constant to define that the jTableAttachments in the mainframe's tabbed
     * pane has the focus
     */
    public static final int FOCUS_TABLE_ATTACHMENTS = 11;
    /**
     * Constant to define which textfield in the new entry dialog has the focus.
     */
    public static final int FOCUS_UNKNOWN = 99;

    /**
     * A string array that contains the different encodings that are used by the
     * different reference- managers when exporting their data to a bibtext
     * file. use also {@link #BIBTEX_DESCRIPTIONS} to retrieve the programmes'
     * names associated with these encoding.<br><br>
     * 0: UTF-8 (Bibliographix)<br>
     * 1: UTF-8 (BibDesk)<br>
     * 2: UTF-8 (Citavi)<br>
     * 3: ISO8859_1 (Emacs with AucTex/RefTex)<br>
     * 4: UTF-8 (Endnote)<br>
     * 5: ISO8859_1 (JabRef)<br>
     * 6: UTF-8 (JabRef)<br>
     * 7: UTF-8 (Mendeley)<br>
     * 8: UTF-8 (Refworks)<br>
     * 9: UTF-8 (Zotero)<br>
     */
    public static final String[] BIBTEX_ENCODINGS = {"UTF-8",
        "UTF-8",
        "MacRoman",
        "UTF-8",
        "UTF-8",
        "ISO8859_1",
        "UTF-8",
        "ISO8859_1",
        "UTF-8",
        "UTF-8",
        "UTF-8",
        "UTF-8"};
    /**
     * A string array that contains the different names of the reference
     * managers. use also {@link #BIBTEX_ENCODINGS} to retrieve the associated
     * encoding for these programmes.<br><br>
     * 0: Bibliographix<br>
     * 1: BibDesk<br>
     * 2: Citavi<br>
     * 3: Emacs with AucTex/RefTex<br>
     * 4: Endnote<br>
     * 5: JabRef (ISO8859_1-Encoding)<br>
     * 6: JabRef (UTF-8-Encoding)<br>
     * 7: Mendeley (UTF-8-Encoding)<br>
     * 8: Refworks<br>
     * 9: Zotero<br>
     */
    public static final String[] BIBTEX_DESCRIPTIONS = {"Bibliographix",
        "BibDesk",
        "Bookends (MacRoman)",
        "Bookends (UTF-8)",
        "Citavi",
        "Emacs/AucTex/RefTex",
        "Endnote",
        "JabRef (ISO8859_1)",
        "JabRef (UTF8)",
        "Mendeley",
        "RefWorks",
        "Zotero"};

    private static final String BIBTEX_STRING_BIBDESK = "BibDesk";
    private static final String BIBTEX_STRING_CITAVI = "Citavi";
    private static final String BIBTEX_STRING_MENDELEY = "Mendeley";

    private static int getBibtexDescriptionIndex(String desc) {
        for (int i = 0; i < BIBTEX_DESCRIPTIONS.length; i++) {
            if (BIBTEX_DESCRIPTIONS[i].equals(desc)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * A reference to the BibDesk-value in the
     * {@link #BIBTEX_DESCRIPTIONS BIBTEX_DESCRIPTIONS} string-array.
     */
    public static final int BIBTEX_DESC_BIBDESK_INDEX = getBibtexDescriptionIndex(BIBTEX_STRING_BIBDESK);
    /**
     * A reference to the Citavi-value in the
     * {@link #BIBTEX_DESCRIPTIONS BIBTEX_DESCRIPTIONS} string-array.
     */
    public static final int BIBTEX_DESC_CITAVI_INDEX = getBibtexDescriptionIndex(BIBTEX_STRING_CITAVI);
    /**
     * A reference to the Mendeley-value in the
     * {@link #BIBTEX_DESCRIPTIONS BIBTEX_DESCRIPTIONS} string-array.
     */
    public static final int BIBTEX_DESC_MENDELEY_INDEX = getBibtexDescriptionIndex(BIBTEX_STRING_MENDELEY);

    public static final String[] LATEX_BIB_STYLES = {"First appearance (unsrt)",
        "Alphabetical (plain)",
        "Computer Journal (cj)",
        "Council of Biology Editors (cbe)",
        "Institute of Electrical and Electronics Engineers (IEEE)",
        "Institute of Electrical and Electronics Engineers (ieeetr)",
        "Nature (nature)",
        "Nucleic Acid Research (nar)"};

    public static final String[] LATEX_BIB_STYLECODE = {"unsrt",
        "plain",
        "cj",
        "cbe",
        "IEEE",
        "ieeetr",
        "nature",
        "nar"};

    public static final String[] LATEX_DOCUMENT_CLASS = {"Standard",
        "article",
        "report",
        "letter",
        "book",
        "slides"};

    /**
     * A constant indicating that a bibtex-entry is of type article
     */
    public static final int BIBTEX_ENTRYTYPE_ARTICLE = 1;
    /**
     * A constant indicating that a bibtex-entry is of type book
     */
    public static final int BIBTEX_ENTRYTYPE_BOOK = 2;
    /**
     * A constant indicating that a bibtex-entry is of type article within a
     * book
     */
    public static final int BIBTEX_ENTRYTYPE_BOOKARTICLE = 3;
    /**
     * A constant indicating that a bibtex-entry is of type incollection
     */
    public static final int BIBTEX_ENTRYTYPE_CHAPTER = 4;
    /**
     * A constant indicating that a bibtex-entry is of type phd-thesis
     */
    public static final int BIBTEX_ENTRYTYPE_PHD = 5;
    /**
     * A constant indicating that a bibtex-entry is of type master-thesis
     */
    public static final int BIBTEX_ENTRYTYPE_THESIS = 6;
    /**
     * A constant indicating that a bibtex-entry is of type unpublished
     */
    public static final int BIBTEX_ENTRYTYPE_UNPUBLISHED = 7;
    /**
     * A constant indicating that a bibtex-entry is of type conference
     */
    public static final int BIBTEX_ENTRYTYPE_CONFERENCE = 8;
    /**
     * A constant indicating that a bibtex-entry is of type techreport
     */
    public static final int BIBTEX_ENTRYTYPE_REPORT = 9;
    /**
     * A constant indicating that an author has no bibkey
     */
    public static final int BIBTEX_ENTRYTYPE_NOBIBKEY = 10;
    /**
     * Indicates the style of how a bibtex-entry should be returned when
     * requested for display. These constants are also used when importing
     * authors
     */
    public static final int BIBTEX_CITE_STYLE_GENERAL = 0;
    /**
     * Indicates the style of how a bibtex-entry should be returned when
     * requested for display. These constants are also used when importing
     * authors
     */
    public static final int BIBTEX_CITE_STYLE_CBE = 1;
    /**
     * Indicates the style of how a bibtex-entry should be returned when
     * requested for display. These constants are also used when importing
     * authors
     */
    public static final int BIBTEX_CITE_STYLE_APA = 2;
    /**
     * Color-constant for the grid-color of tables
     */
    public static final Color gridcolor = Color.LIGHT_GRAY;
    public static final Color gridcolortransparent = new Color(0, 0, 0, 0);

    /**
     * COnstant indicating that we want to create a reference list for a
     * html-page when exporting data. See CExportDlg fore more details.
     */
    public static final int REFERENCE_LIST_HTML = 1;
    /**
     * COnstant indicating that we want to create a reference list for a
     * txt-page when exporting data. See CExportDlg fore more details.
     */
    public static final int REFERENCE_LIST_TXT = 2;

    /**
     * Used in the CDesktop-class to indicate whether the deleted item from the
     * jTree was a desktop-item, i.e. the user wants to delete a complete
     * desktop.
     */
    public static final int DEL_DESKTOP = 1;
    /**
     * Used in the CDesktop-class to indicate whether the deleted item from the
     * jTree was a bullet-item, i.e. the user wants to delete a bullet with its
     * sub-entries
     */
    public static final int DEL_BULLET = 2;
    /**
     * Used in the CDesktop-class to indicate whether the deleted item from the
     * jTree was an entry-item, i.e. the user wants to delete a single entry
     * (node).
     */
    public static final int DEL_ENTRY = 3;

    public static final int MOVE_UP = 1;
    public static final int MOVE_DOWN = 2;

    public static final int RETURN_VALUE_CANCEL = -1;
    public static final int RETURN_VALUE_CLOSE = 0;
    public static final int RETURN_VALUE_CONFIRM = 1;

    /**
     * This is a constant for the image icon for comment nodes in the desktop
     * window
     */
    public static final ImageIcon iconDesktopComment = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/desktop_comment.png"));
    /**
     * This is a constant for the image icon for follower number nodes in the
     * desktop window
     */
    public static final ImageIcon iconDesktopLuhmann = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/luhmann-number_desk.png"));
    public static final ImageIcon iconTopLuhmann = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/top_luhmann.png"));
    public static final ImageIcon iconNoTopLuhmann = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/no_top_luhmann.png"));
    public static final ImageIcon iconMiddleLuhmann = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/middle_luhmann.png"));

    /**
     * This is the constant for the application's icon
     */
    public static final ImageIcon zknicon = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/zkn3_16x16.png"));
    /**
     * This is the constant for the application's icon
     */
    public static final ImageIcon errorIcon = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png"));
    /**
     * This is the constant for the faded application's icon
     */
    public static final ImageIcon errorIconFaded = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/error-grey.png"));
    /**
     * This is the constant for the magnifier icon
     */
    public static final ImageIcon lupeIcon = new ImageIcon(Constants.class.getResource("/de/danielluedecke/zettelkasten/resources/icons/magnifier.png"));
    /**
     * Resource-Path to the standard icon theme
     */
    public static final String standardIconThemePath = "/de/danielluedecke/zettelkasten/resources/toolbaricons/";
    /**
     *
     */
    public static final String[] iconThemes = {"", "crystal/", "tango/", "osx/", "crystal/small/", "tango/small/"};
    /**
     *
     */
    public static final String[] iconThemesNames = {"Standard", "Crystal", "Tango", "Standard (small)", "Crystal (small)", "Tango (small)"};

    /**
     *
     */
    public final static Logger zknlogger = Logger.getLogger(ZettelkastenView.class.getName());

    /**
     * These are the possible dateformats for the manual timestamp.<br><br>
     * usage:<br>
     * {@code DateFormat df = new SimpleDateFormat(CConstants.manualTimestamp[4]);}<br>
     * {@code String timestamp = df.format(new Date());}
     */
    public static final String[] manualTimestamp = {"d.M.yyyy (HH:mm)",
        "d. MMM yyyy (HH:mm)",
        "d. MMMM yyyy (HH:mm)",
        "E, d.M.yyyy (HH:mm)",
        "E, d. MMM yyyy (HH:mm)",
        "E, d. MMMM yyyy (HH:mm)",
        "EEEE, d.M.yyyy (HH:mm)",
        "EEEE, d. MMM yyyy (HH:mm)",
        "EEEE, d. MMMM yyyy (HH:mm)",
        "yyyy-MM-dd HH:mm:ss"};


}
