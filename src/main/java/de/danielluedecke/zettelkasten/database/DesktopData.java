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

import de.danielluedecke.zettelkasten.DesktopFrame;
import de.danielluedecke.zettelkasten.ZettelkastenView;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.Tools;
import org.jdom2.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * The root-element is names "desktops". For each desktop, a new child named
 * "desktop" is created. This child holds the content for one desktop.
 *
 * The first Element is always "bullet", a bullet-point. Bullet-points do always
 * have the attribute "name" which holds the name of the bullet-point that is
 * displayed in the jTree. Furthermore, each bullet-element automatically
 * creates a child-element "comment", even if a bullet-point has no comments.
 *
 * "comment" is therefor always the first child-element of "bullet".
 *
 * Futher child-elements are the entries, named "entry". Each entry-element has
 * an id with the related entry-number from the main-datafile (see CDaten-class)
 * and a timestamp which indicates when this entry was added to the desktop.
 *
 * After the entries, each bullet has possible new bullet-elements as
 * child-elements. These bullet-element indicate the next level of the outline,
 * repeating the structure described here and above.
 *
 * &lt;desktops&gt;<br>
 * &nbsp;&nbsp;&lt;desktop name="MyFirstDesktop"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;bullet name="Name of Bullet point"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;omment&gt;The comment of this
 * bullet&lt;/comment&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="4"
 * timestamp="0901155107"&gt;A possible comment to the entry&lt;/entry&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="11" timestamp="0901153112"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="17"
 * timestamp="0901155313"&gt;A possible comment to the entry&lt;/entry&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;bullet name="Name of Bullet
 * Point"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;comment /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="14"
 * timestamp="0902114301" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="1"
 * timestamp="0901121313"&gt;A possible comment to the entry&lt;/entry&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="23"
 * timestamp="0901453208" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="35"
 * timestamp="0902251714" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;bullet name="Name of
 * Bullet Point"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;comment
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="40"
 * timestamp="0902114301" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="44"
 * timestamp="0902114301" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/bullet&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;bullet name="Name of
 * Bullet Point"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;comment
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="50"
 * timestamp="0902114301" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entry id="54"
 * timestamp="0902114301" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/bullet&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/bullet&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/bullet&gt;<br>
 * &nbsp;&nbsp;&lt;/desktop&gt;<br>
 * &lt;/desktops&gt;
 *
 * @author danielludecke
 */
public class DesktopData {

    private Document desktop;
    private Document modifiedEntries;
    private Document desktopNotes;
    private int currentDesktop;
    private boolean modified;
    private int timestampid = 0;
    private Element foundDesktopElement = null;
    private static final String TREE_FOLDING_EXPANDED = "expand";
    private static final String TREE_FOLDING_COLLAPSED = "collaps";

    public static String ATTR_TIMESTAMP = "timestamp";
    public static String ATTR_COMMENT = "comment";
    public static String ELEMENT_ENTRY = "entry";
    public static String ELEMENT_BULLET = "bullet";

    /**
     * Constants for the method
     * {@link #importArchivedDesktop(org.jdom.Document) importArchivedDesktop()}.
     * Indicates that the import of an desktop-archive was successful.
     */
    public static final int IMPORT_ARCHIVE_OK = 1;
    /**
     * Constants for the method
     * {@link #importArchivedDesktop(org.jdom.Document) importArchivedDesktop()}.
     * Indicates that the import of an desktop-archive failed, because the
     * desktop-name of the imported archive already exists.
     */
    public static final int IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS = 2;
    /**
     * Constants for the method
     * {@link #importArchivedDesktop(org.jdom.Document) importArchivedDesktop()}.
     * Indicates that the import of an desktop-archive failed, because a
     * technical error occured.
     */
    public static final int IMPORT_ARCHIVE_ERR_OTHER = 3;
    /**
     * This element stores a bullet-element that is currently in the clipboard -
     * either due to a cut or copy operation from the CDesktop-Dialog
     */
    private Element clipbullet = null;
    /**
     * Reference to the main frame.
     */
    private final ZettelkastenView zknframe;
    /**
     *
     */
    private ArrayList<Integer> retrieveList;
    /**
     *
     */
    private ArrayList<String> timestampList;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap
            = org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
            getContext().getResourceMap(DesktopFrame.class);

    /**
     * @param zkn
     */
    public DesktopData(ZettelkastenView zkn) {
        zknframe = zkn;
        clear();
    }

    /**
     *
     */
    public final void clear() {
        // create empty documents
        desktop = new Document(new Element("desktops"));
        modifiedEntries = new Document(new Element("modifiedEntries"));
        desktopNotes = new Document(new Element("desktopNotes"));
        currentDesktop = -1;
        modified = false;
    }

    /**
     * Sets the complete desktop-data-file, usually needed when loading new
     * data.
     *
     * @param doc an xml-file containing the desktop-data
     */
    public void setDesktopData(Document doc) {
        desktop = doc;
    }

    /**
     * Gets the complete desktop-data-file, usually needed when savin the data.
     *
     * @return an xml-file containing the desktop-data
     */
    public Document getDesktopData() {
        return desktop;
    }

    /**
     * Sets the complete desktop-data-file, usually needed when loading new
     * data.
     *
     * @param doc an xml-file containing the desktop-data
     */
    public void setDesktopModifiedEntriesData(Document doc) {
        modifiedEntries = doc;
    }

    /**
     * Gets the complete desktop-data-file, usually needed when savin the data.
     *
     * @return an xml-file containing the desktop-data
     */
    public Document getDesktopModifiedEntriesData() {
        return modifiedEntries;
    }

    /**
     * Sets the complete desktop-data-file, usually needed when loading new
     * data.
     *
     * @param doc an xml-file containing the desktop-data
     */
    public void setDesktopNotesData(Document doc) {
        desktopNotes = doc;
    }

    /**
     * Gets the complete desktop-data-file, usually needed when savin the data.
     *
     * @return an xml-file containing the desktop-data
     */
    public Document getDesktopNotesData() {
        return desktopNotes;
    }

    /**
     * sets the modified state. should be called whenever changes have been made
     * to the desktopfile (set it to true) or when the data has been saved (set
     * it to false)
     *
     * @param m true when changes are unsaved, false otherwise
     */
    public void setModified(boolean m) {
        modified = m;
        // update indicator for autobackup
        zknframe.setBackupNecessary();
    }

    /**
     * Checks whether the datafile is modified
     *
     * @return {@code true} if it is modified, false otherwise
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * This method returns the index-number for the currently used desktop-data.
     * This is necessary since a desktop-document may contain several
     * desktop-data-units.
     *
     * @return the index-number of the currently used desktop, starting with
     * {@code 0} for the first desktop-data
     */
    public int getCurrentDesktopNr() {
        return currentDesktop;
    }

    /**
     * This method sets the index-number for the currently used desktop-data.
     * This is necessary since a desktop-document may contain several
     * desktop-data-units.
     *
     * @param nr the number of the current desktop, using {@code 0} as the first
     * index number.
     */
    public void setCurrentDesktopNr(int nr) {
        currentDesktop = nr;
    }

    /**
     * Returns the amount of saved desktops
     *
     * @return the amount of desktop-data-units. returns {@code 0} if no desktop
     * data exists.
     */
    public int getCount() {
        return desktop.getRootElement().getContentSize();
    }

    /**
     * This method retrieves the name of currently active desktop.
     *
     * @return a String containing the name of the currently active desktop or a
     * string {@code noDesktopNameFound} if the desktop was not found...
     */
    public String getCurrentDesktopName() {
        // return name of current desktop
        return getDesktopName(currentDesktop);
    }

    /**
     * This method retrieves the name of the desktop with the index {@code nr}.
     *
     * @param nr the desktop-index of which we want to have the name, ranged
     * from 0 to (count-1)
     * @return a String containing the name of the desktop, or a string
     * {@code noDesktopNameFound} if the desktop was not found...
     */
    public String getDesktopName(int nr) {
        // get the requested dektop-element
        Element d = getDesktopElement(nr);
        // if no dektop was found, return empty string
        if (null == d) {
            return resourceMap.getString("noDesktopNameFound");
        }
        // get the desktop's name-attribute
        String retval = d.getAttributeValue("name");
        // check for content
        if ((null == retval) || (retval.isEmpty())) {
            retval = resourceMap.getString("noDesktopNameFound");
        }
        // and return it
        return retval;
    }

    /**
     * This method sets the name of the desktop with the index {@code nr}.
     *
     * @param nr the desktop-index of which we want to have the name
     * @param name the new name of the desktop
     * @return {@code true} if name was successfully set, {@code false}
     */
    public boolean setDesktopName(int nr, String name) {
        // get the requested dektop-element
        Element d = getDesktopElement(nr);
        // if no dektop was found, return empty string
        if (null == d) {
            return false;
        }
        try {
            // change attribute
            d.setAttribute("name", name);
            // change modified state
            setModified(true);
        } catch (IllegalNameException | IllegalDataException ex) {
            Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * This method recursivly searches through all entris of the desktop
     * {@code desktopnr} and looks for occurences of the entry with the numer
     * {@code entrynr}. If an entry was found, true is returned, otherwise false
     * is returned.<br><br>
     * This method can be used to look for multiple entry occurences not only in
     * the current sub-tree or sub-bullet, but within all desktop-files - so the
     * user knows that he has already added the entry {@code entrynr} to this or
     * another existing desktop in the past.
     *
     * @param desktopnr the desktop where we should look for occurences of the
     * entry {@code entrynr}
     * @param entrynr the number of the entry that should be looked for.
     * usually, this number corresponds to entry-numbers of entries that are
     * currently being added
     * @return {@code true} if the entry with the number {@code entrynr} exists
     * in the desktop {@code desktopnr}, false otherwise
     */
    public boolean checkForDoubleEntry(int desktopnr, int entrynr) {
        // retrieve desktop-element
        Element desktopelement = getDesktopElement(desktopnr);
        // if desktop-element does not exist, return false
        if (null == desktopelement) {
            return false;
        }
        // find elements
        LinkedList<Element> retval = findElements(desktopelement, String.valueOf(entrynr), new LinkedList<Element>());
        // if we found something, return linked list, else return null
        return (retval.size() > 0);
    }

    /**
     * This method removes all entries which entry-number are passed in the
     * int-array {@code entries} from all available desktop-elements.
     *
     * @param entries an integer-array containing the entry-numbers of those
     * entries that should be removed from the desktop
     * @return
     */
    public boolean deleteEntries(int[] entries) {
        // indicator for deletes entries
        boolean haveDeletedEntries = false;
        // check for valid parameter
        if (entries != null && entries.length > 0 && getCount() > 0) {
            // create linked list which will contain all to be deleted entries...
            LinkedList<Element> finallist = new LinkedList<>();
            // go through all desktops (outer loop)...
            for (int cnt = 0; cnt < getCount(); cnt++) {
                // ...and search for all entries in each desktop (inner loop)
                for (int e : entries) {
                    // retrieve all found entries within the desktop
                    LinkedList<Element> lle = searchForEntry(cnt, e);
                    // check whether we have any returned entries...
                    if (lle != null && lle.size() > 0) {
                        // create a new iterator for the found results
                        Iterator<Element> prepare = lle.iterator();
                        // iterate them
                        while (prepare.hasNext()) {
                            // get each single entry as element
                            Element entry = prepare.next();
                            // and add it to the final list
                            if (entry != null) {
                                finallist.add(entry);
                            }
                        }
                    }
                }
            }
            // if we found any elements that should be deleted, do
            // this now...
            if (finallist.size() > 0) {
                // create iterator
                Iterator<Element> i = finallist.iterator();
                // go trhough linked list
                while (i.hasNext()) {
                    // get each element that should be deleted
                    Element entry = i.next();
                    // if we have a valid element, go on...
                    if (entry != null) {
                        // retrieve timestamp
                        String timestamp = entry.getAttributeValue(ATTR_TIMESTAMP);
                        // check whether we have any modified entry. if so, delete it, since
                        // we no longer need it...
                        deleteModifiedEntry(timestamp);
                        // get the entry's parent
                        Parent p = entry.getParent();
                        // remove entry from parent
                        p.removeContent(entry);
                        // set deleted-indicator to true
                        haveDeletedEntries = true;
                    }
                }
            }
        }
        // change modified state
        if (haveDeletedEntries) {
            setModified(true);
        }
        // return result
        return haveDeletedEntries;
    }

    /**
     * This method recursivles searches through all entris of the desktop
     * {@code desktopnr} and looks for occurences of the entry with the numer
     * {@code entrynr}. If an entry was found, this entry is returned as
     * {@code Element}, otherwise {@code null} is returned.<br><br>
     * This method can be used to look for entries that should be removed from
     * the desktop.
     *
     * @param desktopnr the desktop where we should look for occurences of the
     * entry {@code entrynr}
     * @param entrynr the number of the entry that should be looked for.
     * usually, this number corresponds to entry-numbers of entries that are
     * currently being added
     * @return a linked list of type {@code Element}, which contains the found
     * entry-Elements, in the desktop {@code desktopnr}. If no Elements
     * (entries) have been found, the method return {@code null}.
     */
    public LinkedList<Element> searchForEntry(int desktopnr, int entrynr) {
        // retrieve desktop-element
        Element desktopelement = getDesktopElement(desktopnr);
        // if desktop-element does not exist, return false
        if (null == desktopelement) {
            return null;
        }
        // find elements
        LinkedList<Element> retval = findElements(desktopelement, String.valueOf(entrynr), new LinkedList<Element>());
        // if we found something, return linked list, else return null
        return (retval.size() > 0) ? retval : null;
    }

    /**
     * This method is called from the
     * {@link #checkForDoubleEntry(int, int) checkForDoubleEntry(int, int)}
     * method and used to recursivly scan all elements of a desktop. If an
     * entry-element which id-attribute matches the parameter
     * {@code entrynumber} was found, this element is returned, else
     * {@code null} is returned.
     *
     * @param e the element where we start scanning. for the first call, use
     * {@link #getDesktopElement(int) getDesktopElement(int)} to retrieve the
     * root-element for starting the recursive scan.
     * @param entrynumber the number of the entry we are looking for. if any
     * element's id-attribut matches this parameter, the element is return, else
     * null
     * @return an element which id-attribute matches the parameter
     * {@code entrynumber}, or null if no element was found
     */
    private LinkedList<Element> findElements(Element e, String entrynumber, LinkedList<Element> foundelements) {
        // if we don't have any element, return null
        if (e == null) {
            return foundelements;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // else check whether we have child-elements - if so, re-call method
            if (hasChildren(e)) {
                foundelements = findElements(e, entrynumber, foundelements);
            }
            // check whether we have an entry-element that matched the requested id-number
            if (e != null && e.getName().equals(ELEMENT_ENTRY)) {
                // check whether attribute exists
                String att = e.getAttributeValue("id");
                // if so, and it machtes the requested id-number, add element to list
                if (att != null && att.equals(entrynumber)) {
                    foundelements.add(e);
                }
            }
        }
        return foundelements;
    }

    public boolean desktopHasComments(Element desktopelement) {
        return getCommentCount(desktopelement, 0) > 0;
    }

    public int getCommentCount(Element e, int commentcount) {
        // if we don't have any element, return null
        if (e == null) {
            return commentcount;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // else check whether we have child-elements - if so, re-call method
            if (hasChildren(e)) {
                commentcount = getCommentCount(e, commentcount);
            }
            // check whether we have an entry-element that matched the requested id-number
            if (e != null) {
                // check whether we have a bullet-point
                if (e.getName().equals(ELEMENT_BULLET)) {
                    // if we have a bullet, return the text of it's comment-child.
                    Element comel = e.getChild(ATTR_COMMENT);
                    if (comel != null && !comel.getText().isEmpty()) {
                        commentcount++;
                    }
                } else {
                    // else return the element's text
                    if (!e.getText().isEmpty()) {
                        commentcount++;
                    }
                }
            }
        }
        return commentcount;
    }

    /**
     * This method recursivly scans all elements of a desktop. If an
     * entry-element which id-attribute matches the parameter
     * {@code entrynumber} was found, this method returns {@code true}.
     *
     * @param e the element where we start scanning. for the first call, use
     * {@link #getDesktopElement(int) getDesktopElement(int)} to retrieve the
     * root-element for starting the recursive scan.
     * @param entrynumber the number of the entry we are looking for. if any
     * element's id-attribut matches this parameter, the element is return, else
     * null
     * @param found the initial value, should be {@code false} when initially
     * called
     * @return {@code true} when the entry with the number {@code entrynumber}
     * was found, {@code false} otherwise.
     */
    public boolean desktopHasElement(Element e, String entrynumber, boolean found) {
        // if we don't have any element, return null
        if (e == null) {
            return false;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // else check whether we have child-elements - if so, re-call method
            if (hasChildren(e)) {
                found = desktopHasElement(e, entrynumber, found);
            }
            // check whether an entry was found in children
            if (found) {
                return true;
            }
            // check whether we have an entry-element that matched the requested id-number
            if (e != null && e.getName().equals(ELEMENT_ENTRY)) {
                // check whether attribute exists
                String att = e.getAttributeValue("id");
                // if so, and it machtes the requested id-number, add element to list
                if (att != null && att.equals(entrynumber)) {
                    // save element
                    foundDesktopElement = e;
                    return true;
                }
            }
        }
        return found;
    }

    /**
     * This method checks whether an entry with the number {@code entrynumber}
     * is stored in any of the available desktops. If any of the desktops
     * contains an entry with the number {@code entrynumber}, {@code true} is
     * returned.
     *
     * @param entrynumber the number of that entry that should be found in the
     * desktops.
     * @return {@code true} if this entry was found in any desktop,
     * {@code false} otherwise or if no desktops exist.
     */
    public boolean isEntryInAnyDesktop(int entrynumber) {
        // init return value
        boolean entryfound = false;
        // reset found elements
        foundDesktopElement = null;
        // iterate all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // check whether the desktop has this entry as element
            entryfound = desktopHasElement(getDesktopElement(cnt), String.valueOf(entrynumber), entryfound);
            // if yes, break out of loop
            if (entryfound) {
                return true;
            }
        }
        // return result
        return entryfound;
    }

    /**
     * This method checks whether an entry with the number {@code entrynumber}
     * is stored in the current desktop. If this desktop has an entry with the
     * number {@code entrynumber}, {@code true} is returned.
     *
     * @param entrynumber the number of that entry that should be found in the
     * current desktop.
     * @return {@code true} if this entry was found in this desktop,
     * {@code false} otherwise or if no such desktop exist.
     */
    public boolean isEntryInCurrentDesktop(int entrynumber) {
        return isEntryInDesktop(entrynumber, getCurrentDesktopNr());
    }

    /**
     * This method checks whether an entry with the number {@code entrynumber}
     * is stored in the desktops with the desktop-nr {@code desktopnr}. If this
     * desktop has an entry with the number {@code entrynumber}, {@code true} is
     * returned.
     *
     * @param entrynumber the number of that entry that should be found in the
     * desktop with the index-number {@code desktopnr}.
     * @param desktopnr the number of the desktop where the entry should be
     * searched for.
     * @return {@code true} if this entry was found in this desktop,
     * {@code false} otherwise or if no such desktop exist.
     */
    public boolean isEntryInDesktop(int entrynumber, int desktopnr) {
        // init return value
        boolean entryfound = false;
        // reset found elements
        foundDesktopElement = null;
        // if parameter is out of range, return false
        if (desktopnr < 0 || desktopnr > getCount()) {
            return false;
        }
        // check whether the desktop has this entry as element
        return desktopHasElement(getDesktopElement(desktopnr), String.valueOf(entrynumber), entryfound);
    }

    /**
     * If we have an element found with
     * {@link #desktopHasElement(org.jdom2.Element, java.lang.String, boolean)}
     * we can get this {@code Element} with this function for further use.
     *
     * @return the Element of an entry that was found on a desktop (via
     * {@link #desktopHasElement()}, or {@code null} if no element / entry was
     * found.
     */
    public Element getFoundDesktopElement() {
        return foundDesktopElement;
    }

    /**
     * This methods searches all desktops for the entry {@code entrynr} and
     * returns the name of the desktop where the entry is located. If no desktop
     * with this entry was found, an empty string will be returned.
     *
     * @param entrynr the number of the entry that should be searched for in all
     * desktops
     * @return the name of the desktop containing the entry {@code entrynr} as
     * string or an empty string if now desktop with this entry was found.
     */
    public String getDesktopNameOfEntry(int entrynr) {
        // itereate all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // check whether entry is in this desktop
            if (isEntryInDesktop(entrynr, cnt)) {
                // quit method
                return getDesktopName(cnt);
            }
        }
        return "";
    }

    /**
     * This method returns a desktop-element-unit at the given position "nr".
     * Remind to use a range from 0 to (amount of desktops-1)!
     *
     * @param nr the desktop element we want to retrieve, ranged from 0 to
     * (count-1)
     * @return the element of the requested desktop-data (dektop including
     * child-elements like bullets and nodes), or null if an error occured
     */
    public Element getDesktopElement(int nr) {
        // create a list of all elements from the given xml file
        try {
            List<?> elementList = desktop.getRootElement().getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(nr);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
            return null;
        }
    }

    public boolean desktopNameExists(String desktopname) {
        // iterate all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // check whether desktopname equals the requested parameter
            if (getDesktopName(cnt).equalsIgnoreCase(desktopname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns a desktop-element-unit of the desktop with the name
     * {@code desktopname}. If no such desktop exists that matches the parameter
     * {@code desktopname}, {@code null} is returned.
     *
     * @param desktopname the name of the desktop of which element we want to
     * retrieve
     * @return the element of the requested desktop-data (dektop including
     * child-elements like bullets and nodes), or |@code null} if an error
     * occured
     */
    public Element getDesktopElement(String desktopname) {
        // desktop-number of element that is relevant for us
        int desktopnr = -1;
        // iterate all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // check whether desktopname equals the requested parameter
            if (getDesktopName(cnt).equals(desktopname)) {
                // if so, set desktopnumber
                desktopnr = cnt;
                // and leave loop
                break;
            }
        }
        // if we have found a desktop, get its element
        if (desktopnr != -1) {
            // create a list of all elements from the given xml file
            try {
                List<?> elementList = desktop.getRootElement().getContent();
                // and return the requestet Element
                try {
                    return (Element) elementList.get(desktopnr);
                } catch (IndexOutOfBoundsException e) {
                    return null;
                }
            } catch (IllegalStateException e) {
                Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * This method returns the currently used desktop as jdom-element.
     *
     * @return the currently used desktop as jdom-element, or null if an error
     * occured
     */
    public Element getCurrentDesktopElement() {
        return getDesktopElement(currentDesktop);
    }

    /**
     * This method adds a new desktop-element to the document. Furthermore,
     * currentDesktop-number is set to the latest added desktop-index-number.
     *
     * @param name (the name of the desktop, which appears in the
     * desktopDialog's combobox)
     * @return {@code true} if the desktop was successfully added, false
     * otherwise (e.g. because the desktop-name already existed)
     */
    public boolean addNewDesktop(String name) {
        return addNewDesktop(name, null);
    }

    /**
     * This method adds a new desktop-element to the document. Furthermore,
     * currentDesktop-number is set to the latest added desktop-index-number.
     *
     * @param name the name of the desktop, which appears in the desktopDialog's
     * combobox
     * @param notes the initial desktop-notes that should be associated with
     * this desktop. use this e.g. when importing an archived desktop (see
     * {@link #importArchivedDesktop(org.jdom.Document) importArchivedDesktop()}.
     * use {@code null} when no notes-content should be added (i.e. the elements
     * are being created, but they have no text).
     * @return {@code true} if the desktop was successfully added, {@code false}
     * otherwise (e.g. because the desktop-name already existed)
     */
    public boolean addNewDesktop(String name, String[] notes) {
        // first of all, go through all desktops and check whether the name
        // already exist, to avoid double naming...
        // when such a desktopname as "name" already exists, return false
        for (int cnt = 0; cnt < getCount(); cnt++) {
            if (name.equalsIgnoreCase(getDesktopName(cnt))) {
                return false;
            }
        }
        // create new element
        Element d = new Element("desktop");
        try {
            // set the desktop's name as attribute
            d.setAttribute("name", name);
            // add the element to the desktop
            desktop.getRootElement().addContent(d);
            // set currentDesktop index to the new desktop-element
            currentDesktop = desktop.getRootElement().getContentSize() - 1;
            // also add new desktop-notes-element
            Element desk = new Element("desktop");
            // set name attribute
            desk.setAttribute("name", name);
            // create notes elements
            Element n1 = new Element("notes1");
            Element n2 = new Element("notes2");
            Element n3 = new Element("notes3");
            // set initial notes text, if we have any...
            if (notes != null && notes.length > 0) {
                // check for array-length before setting text
                if (notes.length > 0) {
                    n1.setText(notes[0]);
                }
                // check for array-length before setting text
                if (notes.length > 1) {
                    n2.setText(notes[1]);
                }
                // check for array-length before setting text
                if (notes.length > 2) {
                    n3.setText(notes[2]);
                }
            }
            // add notes-sub-elements
            desk.addContent(n1);
            desk.addContent(n2);
            desk.addContent(n3);
            // add element to desktop-notes
            desktopNotes.getRootElement().addContent(desk);
            // change modified state
            setModified(true);
        } catch (IllegalAddException | IllegalNameException | IllegalDataException ex) {
            Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Adds a new bullet-point to the xml-document.
     *
     * @param timestamp the timestamp of the currently selected bullet-point
     * after which the new bullet should be inserted.
     * @param name the name of the bullet-point
     * @return the timestamp of the added bullet-element as {@code String} or
     * {@code null} if an error occured.
     */
    public String addBullet(String timestamp, String name) {
        // find the bullet group that is described in the tree path
        Element parentb = (timestamp != null) ? findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp) : getCurrentDesktopElement();
        String newts;
        // if we have a valid bullet-group, add the new bullet to the xml-file
        if (parentb != null) {
            // create a new element from the given name
            Element b = new Element(ELEMENT_BULLET);
            try {
                b.setAttribute("name", name);
                // set attribute for collapsed/expanded state of bullet
                b.setAttribute("treefold", TREE_FOLDING_EXPANDED);
                // create timestamp
                newts = Tools.getTimeStampWithMilliseconds();
                // check whether timestamp already exists. this is particularly the
                // case when a user adds several entries at once.
                while (timeStampExists(newts)) {
                    newts = Tools.getTimeStampWithMilliseconds();
                }
                // set timestamp as attribute
                b.setAttribute(ATTR_TIMESTAMP, newts);
                // add a "comment" to that bullet. remember, that each bullet
                // automatically gets a child-element called "comment"
                b.addContent(new Element(ATTR_COMMENT));
                // add new bullet to the bullet group
                parentb.addContent(b);
                // change modified state
                setModified(true);
                // return timestamp of added bullet
                return newts;
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                return null;
            }
        }
        return null;
    }

    private boolean timeStampExists(String timestamp) {
        // init return value
        boolean retval = false;
        // go through all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // try to find any element in each desktop that matches the timestamp
            Element found = findEntryElementFromTimestamp(getDesktopElement(cnt), timestamp);
            // if we found anything, modify return-value
            if (found != null) {
                retval = true;
            }
        }
        // return result
        return retval;
    }

    /**
     * This method renames the bullet which is given through the path
     * {@code tp}.
     *
     * @param timestamp the timestamp of the bullet that should be renamed
     * @param newName the new name of the bullet
     * @return {@code true} is renaming was successful, {@code false} if an
     * error occured.
     */
    public boolean renameBullet(String timestamp, String newName) {
        // retrieve selected bullet element
        Element b = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        if (b != null) {
            try {
                // change name
                b.setAttribute("name", newName);
                // change modified state
                setModified(true);
            } catch (IllegalNameException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * This method retrieves a bullet element from the given path {@code tp} and
     * copies it into a global Element-variable, so this element is stored for
     * later use.
     *
     * @param timestamp the timestamp of the bullet that should be copied to the
     * "clipboard"
     */
    public void copyBulletToClip(String timestamp) {
        // retrieve bullet for copy-operation
        clipbullet = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // but use new timestamp!
        if (clipbullet != null) {
            // create timestamp
            String newts = Tools.getTimeStampWithMilliseconds();
            // check whether timestamp already exists. this is particulary the
            // case when a user adds several entries at once.
            while (timeStampExists(newts)) {
                newts = Tools.getTimeStampWithMilliseconds();
            }
            // set timestamp as attribute
            clipbullet.setAttribute(ATTR_TIMESTAMP, newts);
        }
    }

    /**
     * This method retrieves a bullet element from the given path {@code tp} and
     * copies it into a global Element-variable, so this element is stored for
     * later use. Furthermore, the element retrieve from the treepath {@code tp}
     * is removed from the XML-document
     *
     * @param timestamp the timestamp of the bullet that should be copied to the
     * "clipboard" and deleted afterwards (cut-operation)
     */
    public void cutBulletToClip(String timestamp) {
        // retrieve the entry that should be deleted
        Element bullet = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        if (bullet != null) {
            // get the entry's parent
            Parent p = bullet.getParent();
            // get the index from "bullet"
            int index = p.indexOf(bullet);
            // if we have a valid index, go on
            if (index != -1) {
                // remove the content and save it to the clipboard-element
                clipbullet = (Element) p.removeContent(index);
                // change modified state
                setModified(true);
            }
        }
    }

    /**
     * Pastes the bullet-point {@code clipbullet} into the xml-document. The
     * Element {@code clipbullet} must be retrieved by
     * {@link #copyBulletToClip(java.util.LinkedList) copyBulletToClip(java.util.LinkedList)}.
     *
     * @param timestamp the timestamp of the bullet that should be inserted
     * @param isRoot {@code true} when the drop-source is the root-element of
     * the jTree, {@code false} when the drop-source is any other parent-element
     */
    public void pasteBulletFromClip(String timestamp, boolean isRoot) {
        // find the bulletgroup that is described in the treepath
        Element parentb = (isRoot) ? getCurrentDesktopElement() : findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // if we have a valid bullet-group, add the new bullet to the xml-file
        if (parentb != null && clipbullet != null) {
            // create a new element
            Element b = new Element(ELEMENT_BULLET);
            try {
                // set the name of the copied bullet-value
                b.setAttribute("name", clipbullet.getAttributeValue("name"));
                // and use copied timestamp
                b.setAttribute(ATTR_TIMESTAMP, clipbullet.getAttributeValue(ATTR_TIMESTAMP));
                // clone the content from the clipboard-bullet to the new element. we
                // have to clone the content, since the element "clipbullet" still might
                // be attached to the document, when the bullet is just copied, not cut.
                b.addContent(clipbullet.cloneContent());
                // add new bullet to the bulletgroup
                parentb.addContent(b);
                // change modified state
                setModified(true);
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * This method moves an element (entry-node or bullet-point) one position
     * up- or downwards, depending on the parameter {@code movement}.
     *
     * @param movement indicates whether the element should be moved up or down.
     * use following constants:<br> - {@code CConstants.MOVE_UP}<br> -
     * {@code CConstants.MOVE_DOWN}<br>
     * @param timestamp
     */
    public void moveElement(int movement, String timestamp) {
        // get the selected element, independent from whether it's a node or a bullet
        Element e = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        if (e != null) {
            // get the element's parent
            Element p = e.getParentElement();
            // get the index of the element that should be moved
            int index = p.indexOf(e);
            // remove the element that should be moved
            Element dummy = (Element) p.removeContent(index);
            try {
                // and insert element one index-position below the previous index-position
                p.addContent((Constants.MOVE_UP == movement) ? index - 1 : index + 1, dummy);
                // change modifed state
                setModified(true);
            } catch (IllegalAddException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * This method adds a new entry (child-node) to the xml-document.
     *
     * @param timestamp the timestamp of the element, where the entry "nr"
     * should be inserted as new child
     * @param nr the entry-number of the entry that should be added
     * @param insertpos the position where the new entry should be inserted.
     * necessary when we have already more children and the entry should be
     * inserted in between, at the beginning or end of the children-list.
     * @return the timestamp of the added entry-element as {@code String} or
     * {@code null} if an error occured.
     */
    public String addEntry(String timestamp, String nr, int insertpos) {
        // find the bullet that is described in the treepath
        Element b = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // if we have a valid bullet, add the new enry to the xml-file
        if (b != null) {
            // check whether element is a bullet, if not, retrieve its parent element
            if (!b.getName().equals(ELEMENT_BULLET)) {
                b = b.getParentElement();
            }
            // create a new element
            Element e = new Element(ELEMENT_ENTRY);
            try {
                e.setAttribute("id", nr);
                // create timestamp
                String ts = Tools.getTimeStampWithMilliseconds();
                // check whether timestamp already exists. this is particulary the
                // case when a user adds several entries at once.
                while (timeStampExists(ts)) {
                    ts = Tools.getTimeStampWithMilliseconds();
                }
                // add timestamp to entry element
                e.setAttribute(ATTR_TIMESTAMP, ts);
                // add new enry to the bullet at insert-position+1 (because at first
                // position in the bullet is always the comment)
                b.addContent(insertpos, e);
                // change modified state
                setModified(true);
                // return timestamp
                return ts;
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
        return null;
    }

    /**
     * This method changes the treefold-state of a bullet-point. The
     * <i>treefold</i>-attribute of the bullet-point is either set to
     * {@link #TREE_FOLDING_EXPANDED TREE_FOLDING_EXPANDED} or
     * {@link #TREE_FOLDING_COLLAPSED TREE_FOLDING_COLLAPSED}.<br><br>
     * With this attribute, we can check whether a bullet-point should be
     * expanded or not, when creating the TreeView in the CDesktop-Window.
     *
     * @param timestamp the timestamp of the bullet point which treefold-state
     * was changed
     * @param expanded {@code true} if the bullet point was expanded,
     * {@code false} if it was collapsed.
     */
    public void setBulletTreefold(String timestamp, boolean expanded) {
        // retrieve bullet of which fold-state should be switched
        Element b = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // check for valid value
        if (b != null) {
            try {
                // set new treefold-attribute
                b.setAttribute("treefold", (expanded) ? TREE_FOLDING_EXPANDED : TREE_FOLDING_COLLAPSED);
            } catch (IllegalNameException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Checks whether a bullet-point's treefold-state is expanded or not.
     *
     * @param timestamp the timestamp of the bzllet point, which treefold-state
     * we want to check
     * @return {@code true} if bullet is expanded, {@code false} if it is
     * collapsed
     */
    public boolean isBulletTreefoldExpanded(String timestamp) {
        // retrieve bullet of which fold-state should be switched
        Element b = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // check for valid value
        if (b != null) {
            // check whether attribute exists
            String tf = b.getAttributeValue("treefold");
            // set new treefold-attribute
            return (tf != null && !tf.isEmpty()) ? tf.equals(TREE_FOLDING_EXPANDED) : true;
        }
        return true;
    }

    /**
     * This method deletes the selected entry from the desktop.
     *
     * @param timestamp the timestamp of the to be deleted entry
     */
    public void deleteEntry(String timestamp) {
        // retrieve the entry that should be deleted
        Element entry = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        if (entry != null) {
            // check whether we have any modified entry. if so, delete it, since
            // we no longer need it...
            deleteModifiedEntry(timestamp);
            // get the entry's parent
            Parent p = entry.getParent();
            // remove entry from parent
            p.removeContent(entry);
            // change modified state
            setModified(true);
        }
    }

    /**
     * This method removes the element with the timestamp-attribute
     * {@code timestamp} from the list of modified entries.
     *
     * @param timestamp the timestamp of that entry that should be removed from
     * the modification-list
     */
    public void deleteModifiedEntry(String timestamp) {
        Element delentry = retrieveModifiedEntryElementFromTimestamp(timestamp);
        if (delentry != null) {
            modifiedEntries.getRootElement().removeContent(delentry);
            setModified(true);
        }
    }

    /**
     * This method deletes the selected entry from the desktop.
     *
     * @param timestamp the timestamp of the to the selected entry.
     */
    public void deleteBullet(String timestamp) {
        // retrieve the entry that should be deleted
        Element bullet = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        if (bullet != null) {
            // retrieve all timestamps of those entries which are children of the to be
            // deleted bullet
            String[] timestamps = retrieveBulletTimestamps(bullet);
            // get the entry's parent
            Parent p = bullet.getParent();
            // remove entry from parent
            p.removeContent(bullet);
            // now remove all possible modified entries of that list
            if (timestamps != null && timestamps.length > 0) {
                // iterate all timestamps
                for (String singlets : timestamps) {
                    // delete modified entries that already have been deleted due
                    // to the removal of the bullet-point
                    deleteModifiedEntry(singlets);
                }
            }
            // change modified state
            setModified(true);
        }
    }

    /**
     * This method deletes the current activated desktop.
     */
    public void deleteDesktop() {
        // check whether we have any desktops at all
        if (getCount() > 0) {
            // get current desktop
            Element d = getCurrentDesktopElement();
            // retrieve all timestamps of those entries which are children of the to be
            // deleted desktop
            String[] timestamps = retrieveBulletTimestamps(d);
            // now remove all possible modified entries of that list
            if (timestamps != null && timestamps.length > 0) {
                // iterate all timestamps
                for (String singlets : timestamps) {
                    // delete modified entries that already have been deleted due
                    // to the removal of the bullet-point
                    deleteModifiedEntry(singlets);
                }
            }
            // delete notes that are associated to this desktop
            deleteDesktopNotes(d.getAttributeValue("name"));
            // and remove it from the root
            desktop.getRootElement().removeContent(d);
            // set currentDesktop index to the new desktop-element
            currentDesktop = desktop.getRootElement().getContentSize() - 1;
            // change modified state
            setModified(true);
        }
    }

    private void deleteDesktopNotes(String desktopname) {
        // get all children from deskopNotes, since we need to find the right
        // desktop-element first...
        List<Element> elementList = desktopNotes.getRootElement().getChildren();
        // create an iterartor
        Iterator<Element> it = elementList.iterator();
        // go through all desktop-elements of the desktopNores-file
        while (it.hasNext()) {
            // retrieve element
            Element desk = it.next();
            // get desktop-name-attribute
            String att = desk.getAttributeValue("name");
            // check for desktop-name
            if (att != null && att.equals(desktopname)) {
                // if we found it, remove desktop-notes
                desktopNotes.getRootElement().removeContent(desk);
                // change modified flag
                setModified(true);
                // and leave methode
                return;
            }
        }
    }

    /**
     * This methods checks whether a given element {@code e} has child-elements
     * or not.
     *
     * @param e the element which should be checked for children
     * @return {@code true} if {@code e} has children, {@code false} otherwise
     */
    public boolean hasChildren(Element e) {
        if (null == e) {
            return false;
        }
        return (!e.getChildren().isEmpty());
    }

    /**
     * This methods checks whether a given desktop "nr" has any child-elements
     * (i.e. bullets) or not.
     *
     * @param nr the desktop which should be checked for children/bullets
     * @return {@code true} if the desktop has any bullets (children, elements),
     * false otherwise
     */
    public boolean desktopHasBullets(int nr) {
        // get the requested desktop
        Element e = getDesktopElement(nr);
        if (e != null) {
            // retrieve the list of all bullet-children
            List<Element> l = e.getChildren(ELEMENT_BULLET);
            // return true if we have any bullets...
            return (!l.isEmpty());
        }
        return false;
    }

    /**
     * Gets the comment of the selected entry in the jTree in the
     * CDesktop-frame. This method traverses the xml-datafile, looking for each
     * element and checks whether the elements
     * <i>timestamp</i>-attribute matches the parameter {@code timestamp}.
     * <br><br>
     * If it was found, the comment as string will be returned.
     *
     * @param timestamp
     * @param linesep
     * @return a string containing the comment, or an empty string if no comment
     * was found
     */
    public String getComment(String timestamp, String linesep) {
        // check for valid value
        if (null == timestamp || timestamp.isEmpty()) {
            return "";
        }
        // retrieve comment
        String comment = findEntryComment(getCurrentDesktopElement(), timestamp, "");
        // check for valid value
        if (comment != null && !comment.isEmpty()) {
            // now replace the br-tags with the requested line-sepatator
            comment = comment.replace("[br]", linesep);
        }
        return comment;
    }

    /**
     * Sets the comment of the selected entry in the jTree in the
     * CDesktop-frame. This method traverses the xml-datafile, looking in each
     * "depth-level" for the element that is given in the linked list parameter
     * <i>tp</i>. This parameter contains the treepath to the selected element.
     *
     * @param timestamp
     * @param comment a string containing the comment
     * @return {@code true} if comment was successfully set, {@code false} if an
     * error occured.
     */
    public boolean setComment(String timestamp, String comment) {
        // retrieve element that matches the timestamp
        Element e = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // check whether an entry was found or not
        if (e != null) {
            try {
                // check whether we have a bullet-point
                if (e.getName().equals(ELEMENT_BULLET)) {
                    // if we have a bullet, return the text of it's comment-child.
                    e.getChild(ATTR_COMMENT).setText(comment);
                } else {
                    // set comment for entry
                    e.setText(comment);
                }
                // change modified state
                setModified(true);
            } catch (IllegalDataException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * This method retrieves all entry-numbers of the current acticated desktop
     * in ascending sorted order and returns them as an integer-array. multiple
     * entry-numbers are removed, so this array contains each entry-number only
     * once.
     *
     * @return an integer-array with all entry-numbers of the current activated
     * desktop in ascending sorted order. multiple entry-numbers are removed, so
     * this array contains each entry-number only once.
     */
    public int[] retrieveDesktopEntries() {
        return retrieveDesktopEntries(getCurrentDesktopNr());
    }

    /**
     * This method retrieves all entry-numbers of the desktop {@code deskopnr}
     * in ascending sorted order and returns them as an integer-array. multiple
     * entry-numbers are removed, so this array contains each entry-number only
     * once.
     *
     * @param desktopnr the desktop-number of the desktop which entries should
     * be retrieved.
     * @return an integer-array with all entry-numbers of the desktop
     * {@code deskopnr} in ascending sorted order. multiple entry-numbers are
     * removed, so this array contains each entry-number only once.
     */
    public int[] retrieveDesktopEntries(int desktopnr) {
        // retrieve desktop xml-element
        Element desktopelement = getDesktopElement(desktopnr);
        // if we have such element, go on...
        if (desktopelement != null) {
            // create new list that will contain the return values
            retrieveList = new ArrayList<>();
            // fill list with all entry-numbers. since we have sub-bullets/levels, we
            // recursevly go through the desktop-data
            retrieveDesktopEntries(desktopelement);
            // if we have any results, go on...
            if (retrieveList.size() > 0) {
                // create return value
                int[] retval = new int[retrieveList.size()];
                // and copy all integer-values from the list to that array
                for (int cnt = 0; cnt < retval.length; cnt++) {
                    retval[cnt] = retrieveList.get(cnt);
                }
                return retval;
            }
        }
        return null;
    }

    /**
     * This method retrieves all entry-numbers of element {@code bullet} in
     * ascending sorted order and returns them as an integer-array. multiple
     * entry-numbers are removed, so this array contains each entry-number only
     * once.
     *
     * @param bullet the (bullet-)element of the desktop which entries should be
     * retrieved.
     * @return an integer-array with all entry-numbers of {@code bullet} in
     * ascending sorted order. multiple entry-numbers are removed, so this array
     * contains each entry-number only once.
     */
    public int[] retrieveBulletEntries(Element bullet) {
        // if we have such element, go on...
        if (bullet != null) {
            // create new list that will contain the return values
            retrieveList = new ArrayList<>();
            // fill list with all entry-numbers. since we have sub-bullets/levels, we
            // recursevly go through the desktop-data
            retrieveDesktopEntries(bullet);
            // if we have any results, go on...
            if (retrieveList.size() > 0) {
                // create return value
                int[] retval = new int[retrieveList.size()];
                // and copy all integer-values from the list to that array
                for (int cnt = 0; cnt < retval.length; cnt++) {
                    retval[cnt] = retrieveList.get(cnt);
                }
                return retval;
            }
        }
        return null;
    }

    /**
     * This method retrieves all entry-timestamps of the desktop
     * {@code desktopnr} and returns them as an string-array.
     *
     * @param desktopnr the number of of the desktop which entry-timestamps
     * should be retrieved.
     * @return a string-array with all entry-timestamps of those entry with the
     * parent {@code bullet}.
     */
    public String[] retrieveEntryTimestampsFromDesktop(int desktopnr) {
        return retrieveBulletTimestamps(getDesktopElement(desktopnr));
    }

    /**
     * This method retrieves all entry-timestamps of the currently activated
     * desktop and returns them as an string-array.
     *
     * @return a string-array with all entry-timestamps of those entry with the
     * parent {@code bullet}.
     */
    public String[] retrieveEntryTimestampsFromDesktop() {
        return retrieveBulletTimestamps(getCurrentDesktopElement());
    }

    /**
     * This method retrieves all entry-timestamps of those entry which are
     * children of the element {@code bullet} and returns them as an
     * string-array.
     *
     * @param bullet the (bullet-)element of the desktop which entries should be
     * retrieved.
     * @return a string-array with all entry-timestamps of those entry with the
     * parent {@code bullet}.
     */
    public String[] retrieveBulletTimestamps(Element bullet) {
        // if we have such element, go on...
        if (bullet != null) {
            // create new list that will contain the return values
            timestampList = new ArrayList<>();
            // fill list with all entry-numbers. since we have sub-bullets/levels, we
            // recursevly go through the desktop-data
            retrieveDesktopEntriesTimestamps(bullet);
            // if we have any results, go on...
            if (timestampList.size() > 0) {
                // return list
                return timestampList.toArray(new String[timestampList.size()]);
            }
        }
        return null;
    }

    /**
     *
     * @param e
     */
    private void retrieveDesktopEntries(Element e) {
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // we have to ignore the comment-tags here. comments are no tags that will
            // be displayed in the jtree, but that store comments which will be displayed
            // in the jeditorpane (see "updateDisplay" method for further details)
            if (!e.getName().equals(ATTR_COMMENT)) {
                // check whether we have no bullet, just an entry
                if (!e.getName().equals(ELEMENT_BULLET)) {
                    // retrieve id-attribute
                    String att = e.getAttributeValue("id");
                    // check for valid value
                    if (att != null) {
                        // get entry-number
                        int enr = Integer.parseInt(att);
                        // sort list so we can search whether entry-number already exists
                        Collections.sort(retrieveList);
                        // search for double entries
                        if (Collections.binarySearch(retrieveList, enr) < 0) {
                            // now we know we have an entry. so get the entry number...
                            retrieveList.add(enr);
                        }
                    }
                }
                // when the new element also has children, call this method again,
                // so we go through the strucuture recursively...
                if (hasChildren(e)) {
                    retrieveDesktopEntries(e);
                }
            }
        }
    }

    /**
     * This method retrieves all entries' timestamps of the element {@code e}
     * and all its child-element. Thus, {@code e} could either be a
     * root-(desktop-)element or a bullet-element.
     *
     * @param e the starting-element from where we want to have all entries'
     * timestamps, including all children of {@code e}.
     */
    private void retrieveDesktopEntriesTimestamps(Element e) {
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // we have to ignore the comment-tags here. comments are no tags that will
            // be displayed in the jtree, but that store comments which will be displayed
            // in the jeditorpane (see "updateDisplay" method for further details)
            if (!e.getName().equals(ATTR_COMMENT)) {
                // check whether we have no bullet, just an entry
                if (!e.getName().equals(ELEMENT_BULLET)) {
                    // get timestamp-attribute
                    String att = e.getAttributeValue(ATTR_TIMESTAMP);
                    // and add its timestamp to the list
                    if (att != null) {
                        timestampList.add(att);
                    }
                }
                // when the new element also has children, call this method again,
                // so we go through the strucuture recursively...
                if (hasChildren(e)) {
                    retrieveDesktopEntriesTimestamps(e);
                }
            }
        }
    }

    /**
     * This method retrieves the content of a modified entry. the modified
     * entries' content is stored in a separated XML-Document (see
     * {@link #modifiedEntries modifiedEntries}. each element of this document
     * has a timestamp-attribute that equals the timestamp-attribute of an entry
     * in the {@link #desktop desktop}-Document.
     * <br><br>
     * So, by passing a {@code timestamp} value, this method searches whether we
     * have any modified entry that has the same timestamp-attribut, and if so,
     * it returns the content of that element, which was modified (and thus
     * differs from an entry's content as it is stored in the original
     * database).
     *
     * @param timestamp the timestamp which should match the requested entry's
     * timestamp-attribute
     * @return the content of the modified entry, or {@code null} if no entry
     * was found.
     */
    public String retrieveModifiedEntryContentFromTimestamp(String timestamp) {
        // retrieve element
        Element retval = retrieveModifiedEntryElementFromTimestamp(timestamp);
        // check for content
        if (retval != null) {
            return retval.getText();
        }
        // else return null
        return null;
    }

    /**
     * This method retrieves the content of an entry (original content, not the
     * modifications!).
     * <br><br>
     * By passing a {@code timestamp} value, this method searches whether we
     * have any entry that has the same timestamp-attribut, and if so, it
     * returns the original content of that element.
     *
     * @param timestamp the timestamp which should match the requested entry's
     * timestamp-attribute
     * @return the content of the entry, or {@code null} if no entry was found.
     */
    public String retrieveOriginalEntryContentFromTimestamp(String timestamp) {
        // retrieve element
        Element retval = findEntryElementFromTimestamp(getCurrentDesktopElement(), timestamp);
        // check for content
        if (retval != null) {
            return retval.getText();
        }
        // else return null
        return null;
    }

    /**
     * This method retrieves the element of a modified entry. the modified
     * entries' content is stored in a separated XML-Document (see
     * {@link #modifiedEntries modifiedEntries}. each element of this document
     * has a timestamp-attribute that equals the timestamp-attribute of an entry
     * in the {@link #desktop desktop}-Document.
     * <br><br>
     * So, by passing a {@code timestamp} value, this method searches whether we
     * have any modified entry that has the same timestamp-attribut, and if so,
     * it returns that element which was modified (and thus differs from an
     * entry's content as it is stored in the original database).
     *
     * @param timestamp the timestamp which should match the requested entry's
     * timestamp-attribute
     * @return the modified entry as element, or {@code null} if no entry was
     * found.
     */
    private Element retrieveModifiedEntryElementFromTimestamp(String timestamp) {
        // retrieve all elements
        List<Content> elementList = modifiedEntries.getRootElement().getContent();
        // when we have any content, go on...
        if (elementList.size() > 0) {
            for (Content elementList1 : elementList) {
                // retrieve each single element
                Element e = (Element) elementList1;
                // retrieve timestamp-attribute
                String att = e.getAttributeValue(ATTR_TIMESTAMP);
                // compare timestamp-attribute-value to timestamp-parameter
                if (att != null && att.equals(timestamp)) {
                    // if they match, return that element
                    return e;
                }
            }
        }
        // else return null
        return null;
    }

    /**
     * Returns one of the three notes that can be saved with the desktop.
     *
     * @param nr the number of the notes, either 1, 2 or 3
     * @return the content of the notes-textfield, or an empty if an error
     * occured
     */
    public String getDesktopNotes(int nr) {
        // check for valid parameter
        if (nr >= 1 && nr <= 3) {
            // get all children from deskopNotes, since we need to find the right
            // desktop-element first...
            List<Element> elementList = desktopNotes.getRootElement().getChildren();
            // create an iterartor
            Iterator<Element> it = elementList.iterator();
            // go through all desktop-elements of the desktopNores-file
            while (it.hasNext()) {
                // retrieve element
                Element desk = it.next();
                // get name sttribute
                String att = desk.getAttributeValue("name");
                // check for desktop-name
                if (att != null && att.equals(getCurrentDesktopName())) {
                    // retrieve notes-element
                    Element note = desk.getChild("notes" + String.valueOf(nr));
                    // return note
                    return (note != null) ? note.getText() : "";
                }
            }
        }
        return "";
    }

    /**
     * Returns one of the three notes that can be saved with the desktop.
     *
     * @param nr the number of the notes, either 1, 2 or 3
     * @param note the content of the notes-textfield
     */
    public void setDesktopNotes(int nr, String note) {
        // check for valid parameter
        if (nr >= 1 && nr <= 3 && note != null) {
            // first check, whether the note has been modified or not. therefor, retrieve
            // current note-text and compare it to the parameter
            String currentnote = getDesktopNotes(nr);
            // if notes don't equal, go on...
            if (!currentnote.equals(note)) {
                // get all children from deskopNotes, since we need to find the right
                // desktop-element first...
                List<Element> elementList = desktopNotes.getRootElement().getChildren();
                // create an iterartor
                Iterator<Element> it = elementList.iterator();
                // go through all desktop-elements of the desktopNores-file
                while (it.hasNext()) {
                    // retrieve element
                    Element desk = it.next();
                    // get name sttribute
                    String att = desk.getAttributeValue("name");
                    // check for desktop-name
                    if (att != null && att.equals(getCurrentDesktopName())) {
                        // retrieve notes-element
                        Element el = desk.getChild("notes" + String.valueOf(nr));
                        // set note text
                        el.setText(note);
                        // change modify-tag
                        setModified(true);
                    }
                }
            }
        }
    }

    /**
     * This method checks whether an entry with the timestamp {@code timestamp}
     * has been modified or not. that means, the methods looks for any elements
     * in the {@link #modifiedEntries modifiedEntries} document that match the
     * timestamp-attribute.
     *
     * @param timestamp the timestamp of the possible modified entry.
     * @return {@code true} if the entry with the timestamp-attribute
     * {@code timestamp} was modified before, {@code false} otherwise.
     */
    public boolean isModifiedEntry(String timestamp) {
        return (retrieveModifiedEntryElementFromTimestamp(timestamp) != null);
    }

    /**
     * This method adds a modified entry to the
     * {@link #modifiedEntries modifiedEntries}-Document, in case the user
     * modifies an entry on the desktop, while the original content of that
     * entry in the main database should be left unchanged.
     *
     * @param timestamp a unique timestamp which is associated to the entry that
     * is modified, so we can later easily find the related modified content.
     * @param content the modified content itself.
     * @return {@code true} if entry was successfully saved, {@code false} if an
     * error occured.
     */
    public boolean addModifiedEntry(String timestamp, String content) {
        // check for valid parameters
        if (null == timestamp || timestamp.isEmpty()) {
            return false;
        }
        if (null == content) {
            content = "";
        }
        // first check, whether modified entry already exists. this may
        // occur, when importing archived desktop-files
        if (null == retrieveModifiedEntryElementFromTimestamp(timestamp)) {
            // create a new element
            Element entry = new Element(ELEMENT_ENTRY);
            try {
                // set timestamp-attribute
                entry.setAttribute(ATTR_TIMESTAMP, timestamp);
                // add content-string
                entry.setText(content);
                // add element to XML-document
                modifiedEntries.getRootElement().addContent(entry);
                // change modified-flag
                setModified(true);
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * This method changes the content of a modified entry in the
     * {@link #modifiedEntries modifiedEntries}-Document. Modified entries are
     * entries that the user modifies on the desktop, while the original content
     * of that entry in the main database should be left unchanged.
     *
     * @param timestamp a unique timestamp which is associated to the entry that
     * is modified, so we can later easily find the related modified content.
     * @param content the modified content itself.
     * @return {@code true} if the entry was successfully changed, {@code false}
     * if an error occured.
     */
    public boolean changeEntry(String timestamp, String content) {
        // retrieve entry-element that should be changed
        Element entry = retrieveModifiedEntryElementFromTimestamp(timestamp);
        // check for valid element
        if (null == entry) {
            // if we have no such element, add the modified entry
            return addModifiedEntry(timestamp, content);
        } else {
            try {
                // else change the content of that element
                entry.setText(content);
                // and change modified flag
                setModified(true);
            } catch (IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * This method updates the timestamp-attributes when the datafile is being
     * updated due to a newer file-version.<br><br>
     * This method is called when updating a file with version number 3.0 or 3.1
     * to 3.2 and higher.
     */
    public void db_updateTimestamps() {
        // use increasing id, for a unique timestamp-value
        timestampid = 1;
        // go through all desktops and update all timestamp-attributes
        for (int cnt = 0; cnt < getCount(); cnt++) {
            updateTimestamps(getDesktopElement(cnt));
        }
    }

    /**
     * This method initialises the desktopNotes-xml-file during an
     * update-process.<br><br>
     * This method is called when updating a file with version number 3.0 or 3.1
     * to 3.2 and higher.
     */
    public void initDesktopNotesUpdate() {
        // iterate all desktops
        for (int cnt = 0; cnt < getCount(); cnt++) {
            // retrieve desktopname
            String dname = getDesktopName(cnt);
            // create new desktop-element
            Element desk = new Element("desktop");
            try {
                // set name attribute
                desk.setAttribute("name", dname);
                // add notes-sub-elements
                desk.addContent(new Element("notes1"));
                desk.addContent(new Element("notes2"));
                desk.addContent(new Element("notes3"));
                // add element to desktop-notes
                desktopNotes.getRootElement().addContent(desk);
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    /**
     * This method updates the timestamp-attributes when the datafile is being
     * updated due to a newer file-version.<br><br>
     * This method is called when updating a file with version number 3.0 or 3.1
     * to 3.2 and higher.
     *
     * @param e the initial element, where the updating should start. usually,
     * use something like
     * {@link #getDesktopElement(int) getDesktopElement(int)}.
     */
    private void updateTimestamps(Element e) {
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        DecimalFormat df = new DecimalFormat("00000");
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            try {
                if (e.getName().equals(ELEMENT_ENTRY)) {
                    String timestamp = e.getAttributeValue(ATTR_TIMESTAMP);
                    if (timestamp != null) {
                        e.setAttribute(ATTR_TIMESTAMP, timestamp.concat(df.format(timestampid++)));
                    }
                }
                if (e.getName().equals(ELEMENT_BULLET)) {
                    e.setAttribute(ATTR_TIMESTAMP, Tools.getTimeStamp() + df.format(timestampid++));
                    e.setAttribute("treefold", TREE_FOLDING_EXPANDED);
                }
            } catch (IllegalNameException | IllegalDataException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
            // when the new element also has children, call this method again,
            // so we go through the strucuture recursively...
            if (hasChildren(e)) {
                updateTimestamps(e);
            }
        }
    }

    /**
     * This method retrieves the comment of an entry-element which
     * <i>timestamp</i>-attribute matches the parameter {@code t}.
     *
     * @param e the initial element where the search starts. usually, use
     * {@link #getCurrentDesktopElement() getCurrentDesktopElement()} fo this.
     * @param t the timestamp which should match the timestamp-attribute of the
     * entry-element
     * @param c the comment as string. when initially calling this method, pass
     * an empty string as parameter
     * @return the comment as string, or an emtpy string if no comment was found
     */
    private String findEntryComment(Element e, String t, String c) {
        // check for valid string
        if (null == c) {
            return "";
        }
        // check for valid comment. if we already found a comment,
        // return it
        if (!c.isEmpty()) {
            return c;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // check whether element has a timestamp value at all, and if it matches the parameter "t".
            if (e.getAttributeValue(ATTR_TIMESTAMP) != null && e.getAttributeValue(ATTR_TIMESTAMP).equals(t)) {
                // check whether we have a bullet-point
                if (e.getName().equals(ELEMENT_BULLET)) {
                    // if we have a bullet, return the text of it's comment-child.
                    Element comel = e.getChild(ATTR_COMMENT);
                    return (comel != null) ? comel.getText() : "";
                } else {
                    // else return the element's text
                    return e.getText();
                }
            }
            // when the new element also has children, call this method again,
            // so we go through the strucuture recursively...
            if (hasChildren(e)) {
                c = findEntryComment(e, t, c);
            }
        }
        return c;
    }

    public int findEntryNrFromTimestamp(String t) {
        return findEntryNrFromTimestamp(getCurrentDesktopNr(), t);
    }

    public int findEntryNrFromTimestamp(int desktopnr, String t) {
        Element e = findEntryElementFromTimestamp(getDesktopElement(desktopnr), t);
        // check for valid entry
        if (null == e) {
            return -1;
        }
        String att = e.getAttributeValue("id");
        if (att != null) {
            try {
                // get entry-number
                return Integer.parseInt(att);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * This method retrieves the entry-element which <i>timestamp</i>-attribute
     * matches the parameter {@code t}.
     *
     * @param e the initial element where the search starts. usually, use
     * {@link #getCurrentDesktopElement() getCurrentDesktopElement()} fo this.
     * @param t the timestamp which should match the timestamp-attribute of the
     * entry-element
     * @return the element which timestamp-attribute matches the parameter
     * {@code t}, or {@code null} if no element was found
     */
    public Element findEntryElementFromTimestamp(Element e, String t) {
        // check for valid entry
        if (null == e) {
            return null;
        }
        // check whether the element "e" passed as parameter already has a timestamp-attribute that
        // matches the parameter "t". if so, return that element.
        if (e.getAttributeValue(ATTR_TIMESTAMP) != null && e.getAttributeValue(ATTR_TIMESTAMP).equals(t)) {
            return e;
        }
        // get a list with all children of the element
        List<Element> children = e.getChildren();
        // create an iterator
        Iterator<Element> it = children.iterator();
        // go through all children
        while (it.hasNext()) {
            // get the child
            e = it.next();
            // check whether element has a timestamp value at all, and if it matches the parameter "t".
            if (e.getAttributeValue(ATTR_TIMESTAMP) != null && e.getAttributeValue(ATTR_TIMESTAMP).equals(t)) {
                return e;
            }
            // when the new element also has children, call this method again,
            // so we go through the strucuture recursively...
            if (hasChildren(e)) {
                // go into method again to traverse child-elements
                e = findEntryElementFromTimestamp(e, t);
                // when one of the child matches the requested entry, leave function without any further iteration
                if (e != null && e.getAttributeValue(ATTR_TIMESTAMP) != null && e.getAttributeValue(ATTR_TIMESTAMP).equals(t)) {
                    return e;
                }
            }
        }
        if (e != null) {
            return (e.getAttributeValue(ATTR_TIMESTAMP) != null && e.getAttributeValue(ATTR_TIMESTAMP).equals(t)) ? e : null;
        } else {
            return null;
        }
    }

    /**
     * This method retrieves the level (depth) of an bullet-element within the
     * tree-hierarchy. This method can be used for example to determine the
     * level of a heading, when exporting desktop-data.
     *
     * @param t the timestamp which should match the timestamp-attribute of the
     * entry-element
     * @return the level (depth) of the bullet within the tree-hierarchy as
     * integer-value, or -1 if an error occured;
     */
    public int getBulletLevel(String t) {
        // retrieve requestes bullet-element
        Element el = findEntryElementFromTimestamp(getCurrentDesktopElement(), t);
        // init bullet-level
        int bulletlevel = -1;
        // check for valid returnvalue
        if (el != null) {
            // iterate parents
            while (el.getParentElement() != null) {
                // increase bullet-level
                bulletlevel++;
                // get parent-element
                el = el.getParentElement();
            }
        }
        return bulletlevel;
    }

    /**
     * This method archives the desktop-data of the desktop with the name
     * {@code name} to a zipped xml-file. The file contains the desktop-data,
     * the modifed-entries-data for those entries that appear on the desktop and
     * the saved desktop-notes.
     *
     * @param name the name of the desktop that should be archived.
     * @return the archived document as XML-focument, or {@code null} if an
     * error occured.
     */
    public Document archiveDesktop(String name) {
        // create new document
        Document archive = new Document(new Element("archivedDesktop"));
        // add desktop-element of desktop that should be archived
        Element deskel = getDesktopElement(name);
        // if we found a valid value, go on
        if (deskel != null) {
            try {
                // set name attribute
                archive.getRootElement().setAttribute("name", name);
                // create desktop-element
                Element content_desktop = new Element("desktop");
                // clone content from current desktop
                content_desktop.addContent(deskel.cloneContent());
                // add element to archive-file
                archive.getRootElement().addContent(content_desktop);
                // now retrieve desktop-notes
                Element noteel = getDesktopNotes(name);
                // if we found a valid value, go on
                if (noteel != null) {
                    // create notes-element
                    Element content_notes = new Element("desktopNotes");
                    // clone content from current desktop-notes
                    content_notes.addContent(noteel.cloneContent());
                    // add content
                    archive.getRootElement().addContent(content_notes);
                }
                // now retrieve all timestamps from the archived desktop
                // and look for modified entries...
                // create new list that will contain the timestamps
                timestampList = new ArrayList<>();
                // fill list with all entry-numbers. since we have sub-bullets/levels, we
                // recursevly go through the desktop-data
                retrieveDesktopEntriesTimestamps(deskel);
                // if we have any results, go on...
                if (timestampList.size() > 0) {
                    // create base element
                    Element modifiedel = new Element("modifiedEntries");
                    // add all modified entries that appear on the archived desktop
                    String[] timestamps = timestampList.toArray(new String[timestampList.size()]);
                    for (String ts : timestamps) {
                        // retrieve modifed entry
                        Element me_dummy = retrieveModifiedEntryElementFromTimestamp(ts);
                        // check for valid value
                        if (me_dummy != null) {
                            // crate new modified-entry-element
                            Element me = new Element(ELEMENT_ENTRY);
                            // set timestamp-attribute
                            me.setAttribute(ATTR_TIMESTAMP, ts);
                            // and add modified text
                            me.setText(me_dummy.getText());
                            // and add content
                            modifiedel.addContent(me);
                        }
                    }
                    archive.getRootElement().addContent(modifiedel);
                }
            } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
                Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
                return null;
            }
        }
        return archive;
    }

    /**
     * This method archives the desktop-data of the desktop with the name
     * {@code name} to a zipped xml-file. The file contains the desktop-data,
     * the modifed-entries-data for those entries that appear on the desktop and
     * the saved desktop-notes.
     *
     * @param desktopnr the number of the desktop that should be archived.
     * @return the archived document as XML-document, or {@code null} if an
     * error occured.
     */
    public Document archiveDesktop(int desktopnr) {
        return archiveDesktop(getDesktopName(desktopnr));
    }

    /**
     * This method imports an archived desktop-file and appends it to the
     * current desktop-data. desktop-content, desktop-notes and modifed entries
     * are being added.
     *
     * @param archive the archive-file as xml-Document
     * @return one of the following return values:<br>
     * <ul>
     * <li>{@link #IMPORT_ARCHIVE_OK IMPORT_ARCHIVE_OK} in case the import was
     * successful</li>
     * <li>{@link #IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS}
     * in case the desktop-name already exists, so the user is asked to enter
     * another name</li>>
     * <li>{@link #IMPORT_ARCHIVE_ERR_OTHER IMPORT_ARCHIVE_ERR_OTHER} in case a
     * general error occured</li>
     * </ul>
     */
    public int importArchivedDesktop(Document archive) {
        // get imported desktopname
        String name = archive.getRootElement().getAttributeValue("name");
        // check whether we have any name at all. if not, return false
        if (null == name || name.isEmpty()) {
            return IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS;
        }
        // first of all, go through all desktops and check whether the name
        // already exist, to avoid double naming...
        // when such a desktopname as "name" already exists, return false
        for (int cnt = 0; cnt < getCount(); cnt++) {
            if (name.equalsIgnoreCase(getDesktopName(cnt))) {
                return IMPORT_ARCHIVE_ERR_DESKTOPNAME_EXISTS;
            }
        }
        // create new element
        Element d = new Element("desktop");
        try {
            // set the desktop's name as attribute
            d.setAttribute("name", name);
            // get desktop-content from archive
            d.addContent(archive.getRootElement().getChild("desktop").cloneContent());
            // add the element to the desktop
            desktop.getRootElement().addContent(d);
            // set currentDesktop index to the new desktop-element
            currentDesktop = desktop.getRootElement().getContentSize() - 1;
            // also add new desktop-notes-element
            Element desk = new Element("desktop");
            // set name attribute
            desk.setAttribute("name", name);
            // create notes elements
            Element n1 = new Element("notes1");
            Element n2 = new Element("notes2");
            Element n3 = new Element("notes3");
            // get notes-child
            Element noteschild = archive.getRootElement().getChild("desktopNotes");
            // check whether we have any content
            if (noteschild != null) {
                // get and add notes...
                Element nc1 = noteschild.getChild("notes1");
                if (nc1 != null) {
                    n1.setText(nc1.getText());
                }
                // get and add notes...
                Element nc2 = noteschild.getChild("notes2");
                if (nc2 != null) {
                    n2.setText(nc2.getText());
                }
                // get and add notes...
                Element nc3 = noteschild.getChild("notes3");
                if (nc3 != null) {
                    n3.setText(nc3.getText());
                }
            }
            // add notes-sub-elements
            desk.addContent(n1);
            desk.addContent(n2);
            desk.addContent(n3);
            // add element to desktop-notes
            desktopNotes.getRootElement().addContent(desk);
            // finally, add modified entries...
            List<Element> modent = archive.getRootElement().getChild("modifiedEntries").getChildren();
            // create iterator
            Iterator<Element> modit = modent.iterator();
            // and add all mofied entries
            while (modit.hasNext()) {
                // get element
                Element mod = modit.next();
                // and add modified entry
                addModifiedEntry(mod.getAttributeValue(ATTR_TIMESTAMP), mod.getText());
            }
            setModified(true);
        } catch (IllegalNameException | IllegalDataException | IllegalAddException ex) {
            Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
            return IMPORT_ARCHIVE_ERR_OTHER;
        }
        return IMPORT_ARCHIVE_OK;
    }

    /**
     * This method returns all desktop-notes of the desktop with the name
     * {@code desktopname} as {@code Element} with the notes as children. If no
     * such desktop exists that matches the parameter {@code desktopname},
     * {@code null} is returned.
     *
     * @param desktopname the name of the desktop of which we want to retrieve
     * the notes-elements
     * @return the note-elements of the requested desktop-data, or {@code null}
     * if an error occured
     */
    public Element getDesktopNotes(String desktopname) {
        // get all children from deskopNotes, since we need to find the right
        // desktop-element first...
        List<Element> elementList = desktopNotes.getRootElement().getChildren();
        // create an iterartor
        Iterator<Element> it = elementList.iterator();
        // go through all desktop-elements of the desktopNores-file
        while (it.hasNext()) {
            // retrieve element
            Element desk = it.next();
            // check for desktop-name
            if (desk.getAttributeValue("name").equals(desktopname)) {
                // return note
                return desk;
            }
        }
        return null;
    }
}
