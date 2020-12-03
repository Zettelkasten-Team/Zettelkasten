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
package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.misc.Comparer;
import org.jdom2.*;

import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author danielludecke
 */
public class Bookmarks {

    /**
     * The xml file which stores all accelerator key information of the main
     * window. This data is loaded and saved within the CSettings class. The
     * data is get/set via getFile/setFile methods (see below)
     */
    private Document bookmarks;
    /**
     *
     */
    private final Settings settingsObj;
    private boolean modified;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;

    /**
     * &lt;bookmarks&gt;<br>
     * &nbsp;&nbsp;&lt;category&gt;&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;entry&gt;Name of Category 1&lt;/entry&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;entry&gt;Name of Category 2&lt;/entry&gt;<br>
     * &nbsp;&nbsp;&lt;category&gt;<br>
     * &nbsp;&nbsp;&lt;bookmark&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="5" cat="1"&gt;This is the comment
     * of the bookmark&lt;/entry&gt;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="9" cat="2"&gt;This is the comment
     * of the bookmark&lt;/entry&gt;<br>
     * &nbsp;&nbsp;&lt;/bookmark&gt;<br>
     * &lt;/bookmarks&gt;<br>
     * <br>
     * The category-part contains all strings/names of the categories.
     * <br><br>
     * The bookmarks have two attributes:<br>
     * - id, which indicates the entry-number<br>
     * - cat, which refers to the category-id<br>
     * <br>
     * The text-value of each bookmark-element represents the comment for that
     * bookmark.
     *
     * @param zkn
     * @param s
     */
    public Bookmarks(ZettelkastenView zkn, Settings s) {
        zknframe = zkn;
        settingsObj = s;
        // init everything
        clear();
    }

    /**
     * clears the bookmarks data and creates empty root elements
     */
    public final void clear() {
        // create empty bookmakrs element
        bookmarks = new Document(new Element("bookmarks"));
        // create the two sub-parts. we have on the one hand the string labels
        // for the categories of the bookmarks. This element contains child-elements
        // with the string-elements
        Element cat = new Element("category");
        // on the other hand we have several bookmarks containing the bookmark-number
        // which are child-elements of the "bookmark" element
        Element bm = new Element("bookmark");
        // add both to the root-element
        bookmarks.getRootElement().addContent(cat);
        bookmarks.getRootElement().addContent(bm);
        // reset modified state
        modified = false;
    }

    /**
     * sets the bookmark-data
     *
     * @param doc an xml-file with the new bookmark-data
     */
    public void setBookmarkData(Document doc) {
        bookmarks = doc;
    }

    /**
     * retrieves the bookmark-data as xml-file
     *
     * @return the bookmark-data as xml-file
     */
    public Document getBookmarkData() {
        return bookmarks;
    }

    /**
     * sets the modified state of the bookmark-data
     *
     * @param m true when the bookmark-data was modified, or false if
     * modifications were saved.
     */
    public void setModified(boolean m) {
        modified = m;
        // update indicator for autobackup
        zknframe.setBackupNecessary();
    }

    /**
     * returns the modified state of the bookmark-data
     *
     * @return {@code true} if the bookmark-data was modified, false if it's
     * unchanged
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * This method counts the amount of saved bookmarks, i.e. the number of
     * elements within the child-element "bookmark"
     *
     * @return the number of bookmarks in the xml-file
     */
    public int getCount() {
        return bookmarks.getRootElement().getChild("bookmark").getContentSize();
    }

    /**
     * This method counts the amount of bookmarks that are assigned to the
     * category {@code cat}.
     *
     * @param cat a bookmarks category
     * @return the number of bookmarks in the category {@code cat}
     */
    public int getCount(String cat) {
        // retrieve category id
        int catid = getCategoryPosition(cat);
        // init return value;
        int retval = 0;
        // check for valid id
        if (catid != -1) {
            // iterate all bookmarks
            for (int cnt = 0; cnt < getCount(); cnt++) {
                // check whether bookmark's cat-id equals the requested cat-id
                if (getBookmarkCategory(cnt) == catid) {
                    retval++;
                }
            }
        }
        return retval;
    }

    /**
     * This method returns all categories in sorted order, as string array, or
     * an empty array if no categories exist.
     *
     * @return all categories in sorted order, as string array, or an empty
     * array if no categories exist.
     */
    public String[] getCategoriesInSortedOrder() {
        // create linked array list
        List<String> retval = new ArrayList<>();
        // iterate all categories
        for (int cnt = 0; cnt < getCategoryCount(); cnt++) {
            // get category
            String cat = getCategory(cnt);
            // add bookmark categories descriptions to combobox
            if (cat != null && !cat.isEmpty()) {
                retval.add(cat);
            }
        }
        // sort list
        if (retval.size() > 0) {
            Collections.sort(retval);
        }
        // return result
        return retval.toArray(new String[retval.size()]);
    }

    /**
     * This method retrieves the bookmarked <b>entry-numbers</b> (<i>not</i> the
     * index-numbers of the bookmarks) that belong to the category {@code cat}
     * (i.e. all entries that have been bookmarked under the category
     * {@code cat}).
     *
     * @param cat the bookmark-category
     * @return the entry-index-numbers (<b>not</b> bookmark-indes-numbers) that
     * are bookmarked under the category {@code cat} as integer array, or
     * {@code null} if an error occured
     */
    public int[] getBookmarkedEntriesFromCat(String cat) {
        // retrieve category id
        int catid = getCategoryPosition(cat);
        // check for valid id
        if (catid != -1) {
            // create return value
            int[] entries = new int[getCount(cat)];
            // init array counter
            int counter = 0;
            // iterate all bookmarks
            for (int cnt = 0; cnt < getCount(); cnt++) {
                // check whether bookmark's cat-id equals the requested cat-id
                if (getBookmarkCategory(cnt) == catid) {
                    // retrieve entry-index-number
                    if (counter < entries.length) {
                        entries[counter] = getBookmarkEntry(cnt);
                    }
                    // increase counter
                    counter++;
                }
            }
            // return result
            return entries;
        }
        return null;
    }

    /**
     * This method returns all entry-index-numbers (<b>not</b>
     * bookmark-index-numbers) of all bookmarked entries.
     *
     * @return an integer-array containing all entry-index-numbers of all
     * bookmarked entries, or {@code null} if no bookmarks exists
     */
    public int[] getAllBookmarkedEntries() {
        // init return value
        int[] entries = null;
        // check whether we have any entries at all
        if (getCount() > 0) {
            // create array with export entries
            entries = new int[getCount()];
            // copy all bookmarked entry-numbers to that array
            for (int cnt = 0; cnt < entries.length; cnt++) {
                entries[cnt] = getBookmarkEntry(cnt);
            }
        }
        return entries;
    }

    /**
     * This method counts the amount of saved categories, i.e. the number of
     * elements within the child-element "category"
     *
     * @return the number of categories in the xml-file
     */
    public int getCategoryCount() {
        return bookmarks.getRootElement().getChild("category").getContentSize();
    }

    /**
     * This method retrieves all available bookmark-categories in alphabetically
     * sorted order.
     *
     * @return a string array with all bookmark-categories, sorted
     * alphabetically, or {@code null} if no categories exist.
     */
    public String[] getSortedCategories() {
        // if we have no categories, return null
        if (getCategoryCount() < 1) {
            return null;
        }
        // create return value
        String[] retval = new String[getCategoryCount()];
        // copy all categories to string array
        for (int cnt = 0; cnt < getCategoryCount(); cnt++) {
            retval[cnt] = getCategory(cnt);
        }
        // sort array
        if (retval != null && retval.length > 0) {
            Arrays.sort(retval, new Comparer());
        }
        return retval;
    }

    /**
     * This method returns a complete bookmark at the position {@code nr}, i.e.
     * the bookmarked entry-index-number, the category name and the bookmark's
     * comment are return as an string-array.
     * <br><br>
     * The counterpart of this method is "addBookmark", which adds a new
     * complete bookmark including comment and category.
     *
     * @param nr the bookmark which should be retrieved
     * @return an array with all bookmark-information:<br>
     * - String[0]: entry-number<br>
     * - String[1]: category-name and<br>
     * - String[2]: comment or {@code null} if no bookmark-element was found
     */
    public String[] getCompleteBookmark(int nr) {
        // retrieve the bookmark-element
        Element bm = retrieveBookmarkElement(nr);
        // if it does not exist, leave method
        if (null == bm) {
            return null;
        }
        // else create return value
        String[] retval = new String[3];
        // retrieve the entry-number, which is bookmarked
        String entry_id = bm.getAttributeValue("id");
        // check for valid attribute-value
        if (entry_id != null) {
            // valid attribute, so store entry-id
            retval[0] = entry_id;
        } else {
            // else return null
            return null;
        }
        // get the category-string
        String category = bm.getAttributeValue("cat");
        // check for valid value
        if (category != null) {
            // save category
            try {
                retval[1] = getCategory(Integer.parseInt(category));
            } catch (NumberFormatException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                return null;
            }
        } else {
            // else return null
            return null;
        }
        // get the bookmarks comment
        retval[2] = bm.getText();

        return retval;
    }

    /**
     * This method returns the comment of a given bookmark {@code nr}.
     *
     * @param nr the bookmark-number of which comment should be retrieved
     * @return a string with the comment, or an empty string if no entry or
     * comment existed.
     */
    public String getComment(int nr) {
        // retrieve the bookmark-element
        Element bm = retrieveBookmarkElement(nr);
        // if it does not exist, leave method
        if (null == bm) {
            return "";
        }
        // else return comment, if we have any...
        return bm.getText();
    }

    /**
     * This method sets the comment of a given bookmark "nr".
     *
     * @param nr the bookmark-number of which comment should be changes
     * @param c
     * @return false if element/bookmark does not exist, true if comment was
     * successfully changed
     */
    public boolean setComment(int nr, String c) {
        // retrieve the bookmark-element
        Element bm = retrieveBookmarkElement(nr);
        // if element does not exist, return false
        if (null == bm) {
            return false;
        }
        // else set comment
        bm.setText(c);
        // change modified-state
        setModified(true);
        // everythings OK
        return true;
    }

    /**
     * This method returns the comment of a given bookmark in HTML-Format. The
     * bookmark-number which comment should be retrieved, is passed via paramter
     * (pos).
     *
     * @param nr
     * @return a string containing the comment of a bookmark, formatted in HTML,
     * or an empty string if no comment existed
     */
    public String getCommentAsHtml(int nr) {
        // get comment
        String c = getComment(nr);
        // init return value
        String retval = "";
        // if we have any comment, go on...
        if ((c != null) && (!c.isEmpty())) {
            // convert comment to html
            retval = HtmlUbbUtil.getHtmlBookmarksComment(settingsObj, c);
        }
        return retval;
    }

    /**
     * This method returns the name of the category at position {@code pos}
     *
     * @param pos the position of the requested category
     * @return the name of the requested category, or an empty string if no
     * category was found
     */
    public String getCategory(int pos) {
        // retrieve category element
        Element cat = retrieveCategoryElement(pos);
        // if it does not exist, return empty string
        if (null == cat) {
            return "";
        }
        // else return category name
        return cat.getText();
    }

    /**
     * This method changes the name of the category at position "pos"
     *
     * @param pos the position of the requested category
     * @param name the new name of the category
     */
    public void setCategory(int pos, String name) {
        // retrieve category element
        Element cat = retrieveCategoryElement(pos);
        // set new category name
        if (cat != null) {
            cat.setText(name);
            setModified(true);
        }
    }

    /**
     * This function retrieves a category-element of a xml document at a given
     * position. used for other methods like
     * {@link #getCategory(int) getCategory(int)}. The position is a value from
     * 0 to {@link #getCategoryCount() getCategoryCount()}-1.
     *
     * @param pos the position of the element
     * @return the element if a match was found, otherwise null)
     */
    private Element retrieveCategoryElement(int pos) {
        // create a list of all elements from the given xml file
        try {
            List<?> elementList = bookmarks.getRootElement().getChild("category").getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos);
            } catch (IndexOutOfBoundsException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                return null;
            }
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * This function retrieves a bookmark-element of a xml document at a given
     * position. used for other methods like getBookmark or getCompleteBookmark
     * The position is a value from 0 to {@link #getCount() getCount()}-1.
     *
     * @param pos the position of the element
     * @return the element if a match was found, otherwise null)
     */
    private Element retrieveBookmarkElement(int pos) {
        // checkl for valid value
        if (pos < 0 || pos >= getCount()) {
            return null;
        }
        // create a list of all elements from the given xml file
        try {
            List<?> elementList = bookmarks.getRootElement().getChild("bookmark").getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos);
            } catch (IndexOutOfBoundsException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                return null;
            }
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            return null;
        }
    }

    /**
     *
     * @param dataObj
     * @param newbookmarks
     */
    public void appendBookmarks(Daten dataObj, Document newbookmarks) {
        // create a list of all elements from the given xml file
        try {
            List<?> elementList = newbookmarks.getRootElement().getContent();
            try {
                // iterate all imported bookmarks
                for (int cnt = 0; cnt < newbookmarks.getContentSize(); cnt++) {
                    // retrieve each single bookmark-element
                    Element b = (Element) elementList.get(cnt);
                    // get bookmark-id (i.e. unique entry-ID)
                    String id = b.getAttributeValue("id");
                    // check for valid value
                    if (id != null && !id.isEmpty()) {
                        // find entry number from ID
                        int index = dataObj.findZettelFromID(id);
                        // check for valid return parameter
                        if (index != -1) {
                            // we now have the entry's number. now retrieve
                            // bookmark-category
                            String cat = b.getAttributeValue("cat");
                            // check for valid value
                            if (cat != null && !cat.isEmpty()) {
                                // retrieve possible comment
                                String comment = b.getText();
                                // and add new imported bookmark
                                addBookmark(index, cat, comment);
                            }
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
            }
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }

    /**
     * This method deletes a category from the data file. The category that
     * should be deleted is indicated by the category's index-number, passed as
     * parameter "nr". If the index-number is not known, use
     * {@link #deleteCategory(java.lang.String) deleteCategory(String cat)} to
     * delete that category or
     * {@link #getCategoryPosition getCategoryPosition(int nr)} to retrieve that
     * number.
     * <br><br>
     * <b>Attention!</b> All related bookmarks that are assigned to this
     * category are deleted as well!
     *
     * @param nr the index-number of the to be deleted category.
     */
    public void deleteCategory(int nr) {
        // get cat-element
        Element category = bookmarks.getRootElement().getChild("category");
        // delete category from the xml-file
        if (category != null && category.removeContent(nr) != null) {
            // if we have successfully deleted a category, delete all bookmarks from
            // that category as well
            for (int cnt = getCount() - 1; cnt >= 0; cnt--) {
                // get each bookmark
                Element bm = retrieveBookmarkElement(cnt);
                try {
                    // get category-atribute
                    String cat = bm.getAttributeValue("cat");
                    // check whether attribute exists
                    if (cat != null) {
                        // get cat id
                        int catid = Integer.parseInt(cat);
                        // if catid equals the deleted category, delete bookmark
                        if (catid == nr) {
                            // get bookmark-element
                            Element child = bookmarks.getRootElement().getChild("bookmark");
                            // if it exists, remove it
                            if (child != null) {
                                child.removeContent(cnt);
                            }
                        } // in case the category-id was greater than the deleted category index-number,
                        // we have to update the category-index-number of the non-deleted bookmark
                        else if (catid > nr) {
                            bm.setAttribute("cat", String.valueOf(catid - 1));
                        }
                    }
                } catch (NumberFormatException | IllegalNameException | IllegalDataException e) {
                    Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
                }
                // change modified state
                setModified(true);
            }
        }
    }

    /**
     * This method deletes a category from the data-file.
     *
     * @param cat the category-name as string.
     */
    public void deleteCategory(String cat) {
        // delete category from it's retrieved index number
        deleteCategory(getCategoryPosition(cat));
    }

    /**
     * This method deletes one or more bookmarks from the datafile. The numbers
     * of the bookmarked <i>entries</i> have to be passed as parameters.
     *
     * @param bms an integer array with the entry-numbers of those entries that
     * are bookmarked and should be deleted.
     */
    public void deleteBookmarks(int[] bms) {
        // if we have any bookmark-numbers to be deleted, go on...
        if (bms != null && bms.length > 0) {
            // modified-indicator
            boolean haveRemovedEntries = false;
            // go through array with all bookmarks that should be deleted
            // and remove their content from the xml-file
            for (int cnt = 0; cnt < bms.length; cnt++) {
                // get the bookmark-position of the bookmarked entry
                int pos = getBookmarkPosition(bms[cnt]);
                // check whether bookmark exists
                if (pos != -1) {
                    // if it exists, remove it
                    bookmarks.getRootElement().getChild("bookmark").removeContent(pos);
                    // and set removeindicator to true...
                    haveRemovedEntries = true;
                }
            }
            // change modified state
            if (haveRemovedEntries) {
                setModified(true);
            }
        }
    }

    /**
     * Adds a new bookmark to the bookmark-file. First, we have to check whether
     * the bookmark already exists. If not, add it. Then we have to check for
     * the category. If it already exists, retrieve the category's index-number.
     * Else add a new category.
     * <br><br>
     * Use "getCompleteBookmark" for retrieving a bookmark's entry-number,
     * category and comment.
     *
     * @param index the index-number of the bookmark, i.e. the entry's number
     * @param cat the category, under which the bookmark should appear.
     * @param comment an optional comment for the bookmark
     */
    public void addBookmark(int index, String cat, String comment) {
        // first check whether this index-number was already bookmarked...
        // if not, a -1 is return, else the index-number
        // if the bookmark already exists, do nothing
        if (-1 == getBookmarkPosition(index)) {
            try {
                // retrieve the position of the category
                int catpos = getCategoryPosition(cat);
                // check whether the category exists
                if (-1 == catpos) {
                    // if the category doesn't already exist, add it
                    catpos = addCategory(cat);
                }
                // create new bookmark-element
                Element bm = new Element(Daten.ELEMENT_ENTRY);
                // set the id, i.e. the number of the entry which is bookmarked
                bm.setAttribute("id", String.valueOf(index));
                // set the category-index for this bookmark
                bm.setAttribute("cat", String.valueOf(catpos));
                // and add the comment
                if (null == comment) {
                    comment = "";
                }
                bm.setText(comment);
                // retrieve the bookmark-"root"-element
                Element bookbase = bookmarks.getRootElement().getChild("bookmark");
                // and add the bookmark-element
                bookbase.addContent(bm);
                // change modified-state
                setModified(true);
            } catch (IllegalAddException | IllegalNameException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Changes an existing bookmark in the bookmark-file. First, we have to
     * check whether the bookmark already exists. If not, leave method. Then we
     * have to check for the category. If it already exists, retrieve the
     * category's index-number. Else add a new category.
     *
     * @param index the index-number of the bookmark, i.e. the entry's number
     * @param cat the category, under which the bookmark should appear.
     * @param comment an optional comment for the bookmark
     */
    public void changeBookmark(int index, String cat, String comment) {
        // get the bookmark position
        int pos = getBookmarkPosition(index);
        // check whether it exists and go on...
        if (pos != -1) {
            // retrieve the position of the category
            int catpos = getCategoryPosition(cat);
            // check whether the category exists
            if (-1 == catpos) {
                // if the category doesn't already exist, add it
                catpos = addCategory(cat);
            }
            try {
                // get bookmark-element
                Element bm = retrieveBookmarkElement(pos);
                // set the id, i.e. the number of the entry which is bookmarked
                bm.setAttribute("id", String.valueOf(index));
                // set the category-index for this bookmark
                bm.setAttribute("cat", String.valueOf(catpos));
                // and add the comment
                bm.setText(comment);
                // change modified-state
                setModified(true);
            } catch (IllegalNameException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * This method changes the category index of all bookmarks. All the old
     * category-index-numbers of bookmarks are changed to the new
     * category-index.
     *
     * @param oldindex the old category-index-number
     * @param newindex the new category-index-number
     */
    public void changeCategoryIndexOfBookmarks(int oldindex, int newindex) {
        // go through all bookmarks
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // if we found the old index, change the index to the new index.
            if (oldindex == getBookmarkCategory(cnt)) {
                setBookmarkCategory(cnt, newindex);
            }
        }
    }

    /**
     * This method returns the position of a bookmarked entry-number in the
     * bookmarks XML file. if the entry-number was not bookmarked (i.e. the
     * entry-number does not exist as bookmark-number), the return value is -1
     *
     * @param nr the number of the bookmarked entry
     * @return the position of the bookmark within the xml-file, or -1 if no
     * bookmark was found
     */
    public int getBookmarkPosition(int nr) {
        // create a list of all bookmarks elements from the bookmark xml file
        try {
            // get root element
            Element bme = bookmarks.getRootElement();
            // get child element
            Element bookchild = bme.getChild("bookmark");
            // check for valid value
            if (bookchild != null) {
                // we are interested in the children of the child-element "bookmark"
                List<?> bookmarkList = bookchild.getContent();
                // an iterator for the loop below
                Iterator<?> iterator = bookmarkList.iterator();
                // counter for the return value if a found bookmark-number matches the parameter
                int cnt = 0;
                // start loop
                while (iterator.hasNext()) {
                    // retrieve each element
                    Element bm = (Element) iterator.next();
                    // if bookmark-number matches the parameter integer value, return the position
                    if (Integer.parseInt(bm.getAttributeValue("id")) == nr) {
                        return cnt;
                    }
                    // else increase counter
                    cnt++;
                }
            }
            // if no bookmark was found, return -1
            return -1;
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            return -1;
        }
    }

    /**
     * This method returns the category-id (category-number) of a given bookmark
     * {@code nr}. Use {@link #getEntryCategory(int) getEntryCategory(int)} if
     * you want to get the category of an entry (with the number {@code nr})
     * that was bookmarked, and not the category of a bookmark-id itself.
     *
     * @param nr the bookmark-index-number as it appears in the database
     * (<b>not</b> the entry-number of the bookmark)
     * @return the number of the bookmark's category-id, or -1 of no category
     * was found
     */
    public int getBookmarkCategory(int nr) {
        // check for valid parameter
        if (nr < 0 || nr >= getCount()) {
            return -1;
        }
        // retrieve the bookmark-element
        Element bm = retrieveBookmarkElement(nr);
        // if it does not exist, leave method
        if (null == bm) {
            return -1;
        }
        // else return category-id
        return Integer.parseInt(bm.getAttributeValue("cat"));
    }

    /**
     * This method changes the category-id (category-number) of a given bookmark
     * {@code nr}.
     *
     * @param nr the bookmark-index-number as it appears in the database
     * (<b>not</b> the entry-number of the bookmark)
     * @param newindex the new index-number of the bookmark's category-id
     */
    public void setBookmarkCategory(int nr, int newindex) {
        // check for valid value
        if (nr >= 1 && nr < getCount()) {
            // retrieve the bookmark-element
            Element bm = retrieveBookmarkElement(nr);
            // if we have a valid element, change the category-attribute
            if (bm != null) {
                bm.setAttribute("cat", String.valueOf(newindex));
                setModified(true);
            }
        }
    }

    /**
     * This method returns the category-id (category-number) of a given
     * bookmarked entry {@code nr}. Use
     * {@link #getBookmarkCategory(int) getBookmarkCategory(int)} if you want to
     * get the category of a bookmark with the index-number {@code nr}.
     *
     * @param nr the entry-number of a bookmarked entry (<b>not</b> the
     * bookmark-id)
     * @return the number of the bookmark's category-id, or -1 of no category
     * was found
     */
    public int getEntryCategory(int nr) {
        // retrieve bookmark-id from entry-number and return category-id
        return getBookmarkCategory(getBookmarkPosition(nr));
    }

    /**
     * This method changes the category-id (category-number) of a given
     * bookmarked entry {@code nr}.
     *
     * @param nr the entry-number of a bookmarked entry (<b>not</b> the
     * bookmark-id)
     * @param newindex the new index-number of the entry's category-id
     */
    public void setEntryCategory(int nr, int newindex) {
        setBookmarkCategory(getBookmarkPosition(nr), newindex);
    }

    /**
     * This method returns the entry-number of a given bookmark {@code nr}. The
     * value {@code nr} has a range from 0 to {@link #getCount() getCount()}-1.
     *
     * @param nr the bookmark-index-number as it appears in the database
     * (<b>not</b> the entry-number of the bookmark)
     * @return the number of the entry that was bookmarked, or -1 of no bookmark
     * was found
     */
    public int getBookmarkEntry(int nr) {
        // retrieve the bookmark-element
        Element bm = retrieveBookmarkElement(nr);
        // if it does not exist, leave method
        if (null == bm) {
            return -1;
        }
        // else return category-id
        return Integer.parseInt(bm.getAttributeValue("id"));
    }

    /**
     * This method returns the category-name of a given bookmark {@code nr}.
     *
     * @param nr the bookmark-index-number as it appears in the database
     * (<b>not</b> the entry-number of the bookmark)
     * @return the name of the bookmark's category, or null of no category-name
     * was found
     */
    public String getBookmarkCategoryAsString(int nr) {
        // get the cat-id
        int catid = getBookmarkCategory(nr);
        // if no cat-id found, return null
        if (-1 == catid) {
            return null;
        }
        // get the category
        String catname = getCategory(catid);
        // if no category-name was found, return null
        if (catname.isEmpty()) {
            return null;
        }
        // else return that name...
        return catname;
    }

    /**
     * This method returns the position of a bookmark-category in the bookmarks
     * XML file. if the category doesn't exist, the return value is -1
     *
     * @param cat the string-value of the category
     * @return the position of the category within the xml-file, or -1 if no
     * category was found
     */
    public int getCategoryPosition(String cat) {
        // create a list of all category elements from the bookmark xml file
        try {
            // we are interested in the children of the child-element "category"
            List<?> catList = bookmarks.getRootElement().getChild("category").getContent();
            // an iterator for the loop below
            Iterator<?> iterator = catList.iterator();
            // counter for the return value if a found category string matches the parameter
            int cnt = 0;
            // start loop
            while (iterator.hasNext()) {
                // retrieve each element
                Element catname = (Element) iterator.next();
                // if category string matches the parameter integer value, return the position
                if (catname.getText().equals(cat)) {
                    return cnt;
                }
                // else increase counter
                cnt++;
            }
            // if no category was found, return -1
            return -1;
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    /**
     * Adds a new category string to the bookmarks category list.
     *
     * @param cat a string with the new category-name
     * @return the index of the currently added category (which equals the new
     * size of the amount of categories - 1)
     */
    public int addCategory(String cat) {
        // get the sub-element "category" of the bookmarks xml datafile
        Element category = bookmarks.getRootElement().getChild("category");
        // create a new category element
        Element newCat = new Element(Daten.ELEMENT_ENTRY);
        // add the new category element to the bookmarks datafile
        category.addContent(newCat);
        // and finally add the parameter (new category string) to the recently created
        // category element
        newCat.setText(cat);
        setModified(true);
        // return the new size of the categories, i.e. the category position of 
        // the recently added category entry
        return bookmarks.getRootElement().getChild("category").getContentSize() - 1;
    }
}
