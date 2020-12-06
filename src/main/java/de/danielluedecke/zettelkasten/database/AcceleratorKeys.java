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

import de.danielluedecke.zettelkasten.util.Constants;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author danielludecke
 */
public class AcceleratorKeys {

    /**
     * The xml file which stores all accelerator key information of the main window.
     * This data is loaded and saved within the CSettings class. The data is get/set 
     * via getFile/setFile methods (see below)
     */
    private final Document acceleratorKeysMain;
    /**
     * The xml file which stores all accelerator key information of the new entry window.
     * This data is loaded and saved within the CSettings class. The data is get/set 
     * via getFile/setFile methods (see below)
     */
    private final Document acceleratorKeysNewEntry;
    /**
     * The xml file which stores all accelerator key information of the desktop window.
     * This data is loaded and saved within the CSettings class. The data is get/set 
     * via getFile/setFile methods (see below)
     */
    private final Document acceleratorKeysDesktop;
    /**
     * The xml file which stores all accelerator key information of the search results window.
     * This data is loaded and saved within the CSettings class. The data is get/set 
     * via getFile/setFile methods (see below)
     */
    private final Document acceleratorKeysSearchResults;
    /**
     * this is the mask key. on mac os, we ususally have the "meta"-key as mask,
     * on windows or linux, however, ctrl is used
     */
    private String mask=null;
    private String delkey=null;
    private String pluskey=null;
    private String minuskey=null;
    private String renamekey=null;
    private String historykey=null;
    private String numbersign=null;
    private String ctrlkey=null;

    /**
     * Constant used as parameter for the getCount method
     */
    public static final int MAINKEYS = 1;
    /**
     * Constant used as parameter for the getCount method
     */
    public static final int NEWENTRYKEYS = 2;
    /**
     * Constant used as parameter for the getCount method
     */
    public static final int DESKTOPKEYS = 3;
    /**
     * Constant used as parameter for the getCount method
     */
    public static final int SEARCHRESULTSKEYS = 4;
    
    
    /**
     * The accelerator keys class. This class manages the accelerator keys. The user can define
     * own accelerator keys for each relevant action. Retrieving and setting this user defined
     * data is done by this class.
     * <br>
     * <br>
     * An XML-File could look like this:<br>
     * <br>
     * &lt;acceleratorkeys&gt;<br>
     * &nbsp;&nbsp;&lt;key action=&quot;newEntry&quot;&gt;control n&lt;/key&gt;<br>
     * &nbsp;&nbsp;&lt;key action=&quot;openDocument&quot;&gt;control o&lt;/key&gt;<br>
     * &lt;/acceleratorkeys&gt;<br>
     */
    public AcceleratorKeys() {
        // init the xml file which should store the accelerator keys
        acceleratorKeysMain = new Document(new Element("acceleratorkeys"));
        // init the xml file which should store the accelerator keys
        acceleratorKeysNewEntry = new Document(new Element("acceleratorkeys"));
        // init the xml file which should store the accelerator keys
        acceleratorKeysDesktop = new Document(new Element("acceleratorkeys"));
        // init the xml file which should store the accelerator keys
        acceleratorKeysSearchResults = new Document(new Element("acceleratorkeys"));
        // init a default acceleratotr table
        initAcceleratorKeys();
    }
    
    
    /**
     * This method inits a default accelerator table. Usually, the CSettings-class loads
     * information from an xml file and overwrites these default settings, by passing the loaded
     * xml file via "setAcceleratorFile" to this class (see below)
     */
    public final void initAcceleratorKeys() {
        // check out which os we have, and set the appropriate mask-key
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
            mask="meta";
            delkey="BACK_SPACE";
            pluskey="CLOSE_BRACKET";
            minuskey="SLASH";
            renamekey="meta ENTER";
            historykey="control shift";
            numbersign = "BACK_SLASH";
            ctrlkey="control";
        }
        else {
            mask="control";
            delkey="DELETE";
            pluskey="PLUS";
            minuskey="MINUS";
            renamekey="F2";
            historykey="alt";
            numbersign = "NUMBER_SIGN";
            ctrlkey="control";
        }
        // We separate the initialisation of the accelerator tables for each 
        // window to keep an better overiew.
        initMainKeys();
        initNewEntryKeys();
        initDesktopKeys();
        initSearchResultsKeys();
    }
    
    
    /**
     * This method inits the accelerator table of the main window's menus. We separate
     * the initialisation of the accelerator tables for each window to keep an better
     * overiew.
     * 
     * This method creates all the acceleratorkeys-child-elements, but only, if they don't
     * already exist. We do this because when loading older acceleratorkeys-xml-document-structures,
     * we might have new elements that would not be initialised. but now we can call this 
     * method after loading the xml-document, and create elements and default values for all
     * new elements. This ensures compatibility to older/news settings-file-versions.
     */
    private void initMainKeys() {
        // this is our element variable which will be used below to set all the child elements
        Element acckey;
        
        // now we have to go through an endless list of accelerator keys. it is important
        // that the attribute values have exactly the same spelling like the actions' names
        // which can be found in the properties-files (resources). This ensures we can easily
        // assign accelerator keys to actions:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get(CAcceleratorKey.getActionName());
        // KeyStroke ks = KeyStroke.getKeyStroke(CAcceleratorKey.getAccelerator());
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);        
        
        //
        // The actions of the main window's file menu
        //
        
        // the accelerator for the "newEntry" action
        if (!findElement(MAINKEYS,"newEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "newEntry");
            acckey.setText(mask+" N");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "insertEntry" action
        if (!findElement(MAINKEYS,"insertEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertEntry");
            acckey.setText(mask+" I");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "quickNewEntry" action
        if (!findElement(MAINKEYS,"quickNewEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "quickNewEntry");
            acckey.setText(mask+" alt N");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "quickNewEntryWithTitle" action
        if (!findElement(MAINKEYS,"quickNewEntryWithTitle")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "quickNewEntryWithTitle");
            acckey.setText(mask+" shift N");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "openDocument" action
        if (!findElement(MAINKEYS,"openDocument")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "openDocument");
            acckey.setText(mask+" O");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "saveDocument" action
        if (!findElement(MAINKEYS,"saveDocument")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "saveDocument");
            acckey.setText(mask+" S");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "saveDocumentAs" action
        if (!findElement(MAINKEYS,"saveDocumentAs")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "saveDocumentAs");
            acckey.setText(mask+" shift S");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "importWindow" action
        if (!findElement(MAINKEYS,"importWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "importWindow");
            acckey.setText(mask+" shift I");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "exportWindow" action
        if (!findElement(MAINKEYS,"exportWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "exportWindow");
            acckey.setText(mask+" shift E");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "quit" action
        if (!findElement(MAINKEYS,"quit")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "quit");
            acckey.setText(mask+" Q");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        
        //
        // The actions of the main window's edit menu
        //

        // the accelerator for the "copyPlain" action
        if (!findElement(MAINKEYS,"copyPlain")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "copyPlain");
            acckey.setText(mask+" shift C");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "editEntry" action
        if (!findElement(MAINKEYS,"editEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "editEntry");
            acckey.setText(mask+" E");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "deleteCurrentEntry" action
        if (!findElement(MAINKEYS,"deleteCurrentEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "deleteCurrentEntry");
            acckey.setText(mask+" shift "+delkey);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "manualInsertEntry" action
        if (!findElement(MAINKEYS,"manualInsertEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "manualInsertEntry");
            acckey.setText(mask+" alt I");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
            
        // the accelerator for the "manualInsertLinks" action
        if (!findElement(MAINKEYS,"manualInsertLinks")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "manualInsertLinks");
            acckey.setText(mask+" alt L");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
            
        // the accelerator for the "selectAllText" action
        if (!findElement(MAINKEYS,"selectAllText")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "selectAllText");
            acckey.setText(mask+" A");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "addToDesktop" action
        if (!findElement(MAINKEYS,"addToDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToDesktop");
            acckey.setText("F9");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "addToBookmark" action
        if (!findElement(MAINKEYS,"addToBookmark")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToBookmark");
            acckey.setText(mask+" B");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "updateDisplay" action
        if (!findElement(MAINKEYS,"updateDisplay")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "updateDisplay");
            acckey.setText("F5");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        
        
        //
        // The actions of the main window's find menu
        //

        // the accelerator for the "find" action
        if (!findElement(MAINKEYS,"find")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "find");
            acckey.setText(mask+" F");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "replace" action
        if (!findElement(MAINKEYS,"replace")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "replace");
            acckey.setText(mask+" R");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "findLive" action
        if (!findElement(MAINKEYS,"findLive")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findLive");
            acckey.setText(mask+" shift F");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showFirstEntry" action
        if (!findElement(MAINKEYS,"showFirstEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showFirstEntry");
            acckey.setText(mask+" shift "+minuskey);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "gotoEntry" action
        if (!findElement(MAINKEYS,"gotoEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "gotoEntry");
            acckey.setText(mask+" G");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "showRandomEntry" action
        if (!findElement(MAINKEYS,"showRandomEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showRandomEntry");
            acckey.setText(ctrlkey+" "+numbersign);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "historyFor" action
        if (!findElement(MAINKEYS,"historyFor")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "historyFor");
            acckey.setText(historykey+" RIGHT");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "historyBack" action
        if (!findElement(MAINKEYS,"historyBack")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "historyBack");
            acckey.setText(historykey+" LEFT");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        // the accelerator for the "showLastEntry" action
        if (!findElement(MAINKEYS,"showLastEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showLastEntry");
            acckey.setText(mask+" shift "+pluskey);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "showPrevEntry" action
        if (!findElement(MAINKEYS,"showPrevEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showPrevEntry");
            acckey.setText(mask+" "+minuskey);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showNextEntry" action
        if (!findElement(MAINKEYS,"showNextEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showNextEntry");
            acckey.setText(mask+" "+pluskey);
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        
        //
        // The actions of the main window's view menu
        //
        
        // the accelerator for the "menuShowLinks" action
        if (!findElement(MAINKEYS,"menuShowLinks")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowLinks");
            acckey.setText(mask+" F1");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowLuhmann" action
        if (!findElement(MAINKEYS,"menuShowLuhmann")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowLuhmann");
            acckey.setText(mask+" F2");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowKeywords" action
        if (!findElement(MAINKEYS,"menuShowKeywords")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowKeywords");
            acckey.setText(mask+" F3");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowAuthors" action
        if (!findElement(MAINKEYS,"menuShowAuthors")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowAuthors");
            acckey.setText(mask+" F4");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowTitles" action
        if (!findElement(MAINKEYS,"menuShowTitles")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowTitles");
            acckey.setText(mask+" F5");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowCluster" action
        if (!findElement(MAINKEYS,"menuShowCluster")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowCluster");
            acckey.setText(mask+" F6");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowBookmarks" action
        if (!findElement(MAINKEYS,"menuShowBookmarks")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowBookmarks");
            acckey.setText(mask+" F7");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "menuShowAttachments" action
        if (!findElement(MAINKEYS,"menuShowAttachments")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "menuShowAttachments");
            acckey.setText(mask+" F8");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showSearchResultWindow" action
        if (!findElement(MAINKEYS,"showSearchResultWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showSearchResultWindow");
            acckey.setText("F3");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showDesktopWindow" action
        if (!findElement(MAINKEYS,"showDesktopWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showDesktopWindow");
            acckey.setText("F4");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showNewEntryWindow" action
        if (!findElement(MAINKEYS,"showNewEntryWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showNewEntryWindow");
            acckey.setText("F11");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "highlightKeywords" action
        if (!findElement(MAINKEYS,"highlightKeywords")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "highlightKeywords");
            acckey.setText("F7");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        //
        // The actions of the main window's popup menu
        //

        // the accelerator for the "addToKeywordList" action
        if (!findElement(MAINKEYS,"addToKeywordList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToKeywordList");
            acckey.setText(mask+" shift K");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "setSelectionAsTitle" action
        if (!findElement(MAINKEYS,"setSelectionAsTitle")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "setSelectionAsTitle");
            acckey.setText(mask+" alt U");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }

        // the accelerator for the "setFirstLineAsTitle" action
        if (!findElement(MAINKEYS,"setFirstLineAsTitle")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "setFirstLineAsTitle");
            acckey.setText(mask+" shift U");
            acceleratorKeysMain.getRootElement().addContent(acckey);
        }
    }


    /**
     * This methos inits the accelerator table of the new entry window's menus. We separate
     * the initialisation of the accelerator tables for each window to keep an better
     * overiew.
     */
    private void initNewEntryKeys() {
        // this is our element variable which will be used below to set all the child elements
        Element acckey;
        
        // now we have to go through an endless list of accelerator keys. it is important
        // that the attribute values have exactly the same spelling like the actions' names
        // which can be found in the properties-files (resources). This ensures we can easily
        // assign accelerator keys to actions:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get(CAcceleratorKey.getActionName());
        // KeyStroke ks = KeyStroke.getKeyStroke(CAcceleratorKey.getAccelerator());
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);        
        
        //
        // The actions of the new entry window's file menu
        //
        
        // the accelerator for the "closeWindow" action
        if (!findElement(NEWENTRYKEYS,"closeWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "closeWindow");
            acckey.setText(mask+" W");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        // the accelerator for the "applyChanges" action
        if (!findElement(NEWENTRYKEYS,"applyChanges")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "applyChanges");
            acckey.setText(mask+" shift S");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        // the accelerator for the "retrieveKeywordsFromDisplayedEntry" action
        if (!findElement(NEWENTRYKEYS,"retrieveKeywordsFromDisplayedEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "retrieveKeywordsFromDisplayedEntry");
            acckey.setText(mask+" alt G");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        //
        // The actions of the new entry window's edit menu
        //
        
        // the accelerator for the "selecteAllText" action
        if (!findElement(NEWENTRYKEYS,"selecteAllText")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "selecteAllText");
            acckey.setText(mask+" A");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addSegmentFromQuickList" action
        if (!findElement(NEWENTRYKEYS,"addSegmentFromQuickList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addSegmentFromQuickList");
            acckey.setText(mask+" shift G");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "addKeywordFromSelection" action
        if (!findElement(NEWENTRYKEYS,"addKeywordFromSelection")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addKeywordFromSelection");
            acckey.setText(mask+" shift K");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "addTitleFromSelection" action
        if (!findElement(NEWENTRYKEYS,"addTitleFromSelection")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addTitleFromSelection");
            acckey.setText(mask+" alt U");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "addTitleFromFirstLine" action
        if (!findElement(NEWENTRYKEYS,"addTitleFromFirstLine")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addTitleFromFirstLine");
            acckey.setText(mask+" shift U");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "undoAction" action
        if (!findElement(NEWENTRYKEYS,"undoAction")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "undoAction");
            acckey.setText(mask+" Z");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "redoAction" action
        if (!findElement(NEWENTRYKEYS,"redoAction")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "redoAction");
            acckey.setText(mask+" shift Z");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "replace" action
        if (!findElement(NEWENTRYKEYS,"replace")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "replace");
            acckey.setText(mask+" H");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        
        //
        // The actions of the new entry window's insert menu
        //
        
        // the accelerator for the "addAuthorFromMenu" action
        if (!findElement(NEWENTRYKEYS,"addAuthorFromMenu")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addAuthorFromMenu");
            acckey.setText(mask+" shift L");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "insertFootnote" action
        if (!findElement(NEWENTRYKEYS,"insertFootnote")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertFootnote");
            acckey.setText(mask+" shift N");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "insertImage" action
        if (!findElement(NEWENTRYKEYS,"insertImage")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertImage");
            acckey.setText(mask+" shift I");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "insertTable" action
        if (!findElement(NEWENTRYKEYS,"insertTable")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertTable");
            acckey.setText(mask+" shift T");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "insertHyperlink" action
        if (!findElement(NEWENTRYKEYS,"insertHyperlink")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertHyperlink");
            acckey.setText(mask+" K");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "insertManualLink" action
        if (!findElement(NEWENTRYKEYS,"insertManualLink")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertManualLink");
            acckey.setText(mask+" shift M");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "insertAttachment" action
        if (!findElement(NEWENTRYKEYS,"insertAttachment")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertAttachment");
            acckey.setText(mask+" shift A");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "insertSymbol" action
        if (!findElement(NEWENTRYKEYS,"insertSymbol")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertSymbol");
            acckey.setText(mask+" alt S");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }


        // the accelerator for the "insertForm" action
        if (!findElement(NEWENTRYKEYS,"insertForm")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertForm");
            acckey.setText(mask+" shift F");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "insertManualTimestamp" action
        if (!findElement(NEWENTRYKEYS,"insertManualTimestamp")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertManualTimestamp");
            acckey.setText(mask+" F7");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

            
        //
        // The actions of the new entry window's format menu
        //
        
        // the accelerator for the "formatBold" action
        if (!findElement(NEWENTRYKEYS,"formatBold")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatBold");
            acckey.setText(mask+" B");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);        
        }
        
        // the accelerator for the "formatItalic" action
        if (!findElement(NEWENTRYKEYS,"formatItalic")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatItalic");
            acckey.setText(mask+" I");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatUnderline" action
        if (!findElement(NEWENTRYKEYS,"formatUnderline")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatUnderline");
            acckey.setText(mask+" U");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatStrikeThrough" action
        if (!findElement(NEWENTRYKEYS,"formatStrikeThrough")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatStrikeThrough");
            acckey.setText(mask+" D");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatSup" action
        if (!findElement(NEWENTRYKEYS,"formatSup")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatSup");
            acckey.setText(mask+" "+pluskey);
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatSub" action
        if (!findElement(NEWENTRYKEYS,"formatSub")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatSub");
            acckey.setText(ctrlkey+" "+numbersign);
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        
        // the accelerator for the "formatHeading1" action
        if (!findElement(NEWENTRYKEYS,"formatHeading1")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatHeading1");
            acckey.setText(mask+" shift H");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatHeading2" action
        if (!findElement(NEWENTRYKEYS,"formatHeading2")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatHeading2");
            acckey.setText(mask+" alt H");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "formatCite" action
        if (!findElement(NEWENTRYKEYS,"formatCite")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatCite");
            acckey.setText(mask+" shift C");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "formatCode" action
        if (!findElement(NEWENTRYKEYS,"formatCode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatCode");
            acckey.setText(mask+" alt C");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "formatFont" action
        if (!findElement(NEWENTRYKEYS,"formatFont")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatFont");
            acckey.setText(mask+" alt F");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "formatQuote" action
        if (!findElement(NEWENTRYKEYS,"formatQuote")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatQuote");
            acckey.setText(mask+" shift 2");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }

        // the accelerator for the "alignCenter" action
        if (!findElement(NEWENTRYKEYS,"alignCenter")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "alignCenter");
            acckey.setText(mask+" E");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "alignLeft" action
        if (!findElement(NEWENTRYKEYS,"alignLeft")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "alignLeft");
            acckey.setText(mask+" L");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "alignRight" action
        if (!findElement(NEWENTRYKEYS,"alignRight")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "alignRight");
            acckey.setText(mask+" R");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "alignMargin" action
        if (!findElement(NEWENTRYKEYS,"alignMargin")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "alignMargin");
            acckey.setText(mask+" shift R");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatHighlight" action
        if (!findElement(NEWENTRYKEYS,"formatHighlight")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatHighlight");
            acckey.setText(mask+" M");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatList" action
        if (!findElement(NEWENTRYKEYS,"formatList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatList");
            acckey.setText(mask+" alt L");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatList" action
        if (!findElement(NEWENTRYKEYS,"formatOrderedList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatOrderedList");
            acckey.setText(mask+" alt N");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "formatColor" action
        if (!findElement(NEWENTRYKEYS,"formatColor")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "formatColor");
            acckey.setText(mask+" T");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        
        //
        // The actions of the new entry window's window menu
        //
        
        // the accelerator for the "setFocusToEditPane" action
        if (!findElement(NEWENTRYKEYS,"setFocusToEditPane")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "setFocusToEditPane");
            acckey.setText(mask+" F2");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        // the accelerator for the "setFocusToKeywordList" action
        if (!findElement(NEWENTRYKEYS,"setFocusToKeywordList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "setFocusToKeywordList");
            acckey.setText(mask+" F3");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
        // the accelerator for the "setFocusToAuthorList" action
        if (!findElement(NEWENTRYKEYS,"setFocusToAuthorList")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "setFocusToAuthorList");
            acckey.setText(mask+" F4");
            acceleratorKeysNewEntry.getRootElement().addContent(acckey);
        }
    }

    
    /**
     * This methos inits the accelerator table of the desktop window's menus. We separate
     * the initialisation of the accelerator tables for each window to keep an better
     * overiew.
     */
    private void initDesktopKeys() {
        // this is our element variable which will be used below to set all the child elements
        Element acckey;
        
        // now we have to go through an endless list of accelerator keys. it is important
        // that the attribute values have exactly the same spelling like the actions' names
        // which can be found in the properties-files (resources). This ensures we can easily
        // assign accelerator keys to actions:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get(CAcceleratorKey.getActionName());
        // KeyStroke ks = KeyStroke.getKeyStroke(CAcceleratorKey.getAccelerator());
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);        
        
        //
        // The actions of the desktop window's file menu
        //
        
        // the accelerator for the "newDesktop" action
        if (!findElement(DESKTOPKEYS,"newDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "newDesktop");
            acckey.setText(mask+" shift N");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addBullet" action
        if (!findElement(DESKTOPKEYS,"addBullet")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addBullet");
            acckey.setText(mask+" B");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addEntry" action
        if (!findElement(DESKTOPKEYS,"addEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addEntry");
            acckey.setText(mask+" alt N");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addLuhmann" action
        if (!findElement(DESKTOPKEYS,"addLuhmann")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addLuhmann");
            acckey.setText(mask+" alt I");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "insertEntry" action
        if (!findElement(DESKTOPKEYS,"insertEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "insertEntry");
            acckey.setText(mask+" N");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "exportDesktop" action
        if (!findElement(DESKTOPKEYS,"exportDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "exportDesktop");
            acckey.setText(mask+" shift E");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "exportMultipleDesktop" action
        if (!findElement(DESKTOPKEYS,"exportMultipleDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "exportMultipleDesktop");
            acckey.setText(mask+" shift X");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "printContent" action
        if (!findElement(DESKTOPKEYS,"printContent")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "printContent");
            acckey.setText(mask+" P");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "closeWindow" action
        if (!findElement(DESKTOPKEYS,"closeWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "closeWindow");
            acckey.setText(mask+" W");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        //
        // The actions of the desktop window's edit menu
        //
        
        // the accelerator for the "cutNode" action
        if (!findElement(DESKTOPKEYS,"cutNode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "cutNode");
            acckey.setText(mask+" X");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "copyNode" action
        if (!findElement(DESKTOPKEYS,"copyNode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "copyNode");
            acckey.setText(mask+" C");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "pasteNode" action
        if (!findElement(DESKTOPKEYS,"pasteNode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "pasteNode");
            acckey.setText(mask+" V");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "editEntry" action
        if (!findElement(DESKTOPKEYS,"editEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "editEntry");
            acckey.setText(mask+" alt E");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "modifiyEntry" action
        if (!findElement(DESKTOPKEYS,"modifiyEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "modifiyEntry");
            acckey.setText(mask+" E");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "applyModificationsToOriginalEntry" action
        if (!findElement(DESKTOPKEYS,"applyModificationsToOriginalEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "applyModificationsToOriginalEntry");
            acckey.setText(mask+" shift P");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "applyAllModificationsToOriginalEntries" action
        if (!findElement(DESKTOPKEYS,"applyAllModificationsToOriginalEntries")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "applyAllModificationsToOriginalEntries");
            acckey.setText(mask+" alt P");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "moveNodeUp" action
        if (!findElement(DESKTOPKEYS,"moveNodeUp")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "moveNodeUp");
            acckey.setText(mask+" UP");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "moveNodeDown" action
        if (!findElement(DESKTOPKEYS,"moveNodeDown")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "moveNodeDown");
            acckey.setText(mask+" DOWN");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "renameBullet" action
        if (!findElement(DESKTOPKEYS,"renameBullet")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "renameBullet");
            acckey.setText(renamekey);
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "commentNode" action
        if (!findElement(DESKTOPKEYS,"commentNode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "commentNode");
            acckey.setText(mask+" K");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "deleteNode" action
        if (!findElement(DESKTOPKEYS,"deleteNode")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "deleteNode");
            acckey.setText(delkey);
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        //
        // The actions of the desktop window's find menu
        //
        
        // the accelerator for the "findLive" action
        if (!findElement(DESKTOPKEYS,"findLive")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findLive");
            acckey.setText(mask+" shift F");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "findLiveNext" action
        if (!findElement(DESKTOPKEYS,"findLiveNext")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findLiveNext");
            acckey.setText(mask+" G");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        // the accelerator for the "findLivePrev" action
        if (!findElement(DESKTOPKEYS,"findLivePrev")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findLivePrev");
            acckey.setText(mask+" shift G");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }

        //
        // The actions of the desktop window's view menu
        //

        // the accelerator for the "updateView" action
        if (!findElement(DESKTOPKEYS,"updateView")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "updateView");
            acckey.setText("F5");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "updateView" action
        if (!findElement(DESKTOPKEYS,"toggleNotesVisibility")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "toggleNotesVisibility");
            acckey.setText("shift F11");
            acceleratorKeysDesktop.getRootElement().addContent(acckey);
        }
    }

    
    /**
     * This methos inits the accelerator table of the desktop window's menus. We separate
     * the initialisation of the accelerator tables for each window to keep an better
     * overiew.
     */
    private void initSearchResultsKeys() {
        // this is our element variable which will be used below to set all the child elements
        Element acckey;
        
        // now we have to go through an endless list of accelerator keys. it is important
        // that the attribute values have exactly the same spelling like the actions' names
        // which can be found in the properties-files (resources). This ensures we can easily
        // assign accelerator keys to actions:
        //
        // javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class, this);
        // AbstractAction ac = (AbstractAction) actionMap.get(CAcceleratorKey.getActionName());
        // KeyStroke ks = KeyStroke.getKeyStroke(CAcceleratorKey.getAccelerator());
        // ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);        
        
        //
        // The actions of the search results window's edit menu
        //
        
        // the accelerator for the "removeSearchResult" action
        if (!findElement(SEARCHRESULTSKEYS,"removeSearchResult")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "removeSearchResult");
            acckey.setText(mask+" shift "+delkey);
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "removeAllSearchResults" action
        if (!findElement(SEARCHRESULTSKEYS,"removeAllSearchResults")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "removeAllSearchResults");
            acckey.setText(mask+" alt shift "+delkey);
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }

        // the accelerator for the "exportEntries" action
        if (!findElement(SEARCHRESULTSKEYS,"exportEntries")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "exportEntries");
            acckey.setText(mask+" shift E");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }

        // the accelerator for the "closeWindow" action
        if (!findElement(SEARCHRESULTSKEYS,"closeWindow")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "closeWindow");
            acckey.setText(mask+" W");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        
        
        //
        // The actions of the search results window's edit menu
        //
        
        // the accelerator for the "selectAll" action
        if (!findElement(SEARCHRESULTSKEYS,"selectAll")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "selectAll");
            acckey.setText(mask+" A");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "editEntry" action
        if (!findElement(SEARCHRESULTSKEYS,"editEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "editEntry");
            acckey.setText(mask+" E");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }

        // the accelerator for the "findAndReplace" action
        if (!findElement(SEARCHRESULTSKEYS,"findAndReplace")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "findAndReplace");
            acckey.setText(mask+" R");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }

        // the accelerator for the "removeEntry" action
        if (!findElement(SEARCHRESULTSKEYS,"removeEntry")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "removeEntry");
            acckey.setText(delkey);
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addToManLinks" action
        if (!findElement(SEARCHRESULTSKEYS,"addToManLinks")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToManLinks");
            acckey.setText(mask+" alt L");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addToLuhmann" action
        if (!findElement(SEARCHRESULTSKEYS,"addToLuhmann")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToLuhmann");
            acckey.setText(mask+" alt I");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addToBookmarks" action
        if (!findElement(SEARCHRESULTSKEYS,"addToBookmarks")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToBookmarks");
            acckey.setText(mask+" B");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "addToDesktop" action
        if (!findElement(SEARCHRESULTSKEYS,"addToDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "addToDesktop");
            acckey.setText("F9");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "showEntryInDesktop" action
        if (!findElement(SEARCHRESULTSKEYS,"showEntryInDesktop")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "showEntryInDesktop");
            acckey.setText(mask+" F9");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "toggleHighlightResults" action
        if (!findElement(SEARCHRESULTSKEYS,"toggleHighlightResults")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "toggleHighlightResults");
            acckey.setText("F7");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
        
        // the accelerator for the "switchLayout" action
        if (!findElement(SEARCHRESULTSKEYS,"switchLayout")) {
            acckey=new Element("key");
            acckey.setAttribute("action", "switchLayout");
            acckey.setText(mask+" F7");
            acceleratorKeysSearchResults.getRootElement().addContent(acckey);
        }
    }    
    
    
    /**
     * Retrieves an xml-file with the requested accelerator information. This method is called
     * from within the CSettings-class, which handles the loading and saving of these xml files.
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @return
     */
    public Document getDocument(int what) {
        // init variable
        Document doc;
        // select the right xml document, depending on which accelerator table is requested
        switch (what) {
            case MAINKEYS: doc = acceleratorKeysMain; break;
            case NEWENTRYKEYS: doc = acceleratorKeysNewEntry; break;
            case DESKTOPKEYS: doc = acceleratorKeysDesktop; break;
            case SEARCHRESULTSKEYS: doc = acceleratorKeysSearchResults; break;
            default: doc = acceleratorKeysMain; break;
        }
        return doc;
    }
    
    /**
     * This method sets an accelerator file. This method called from within the CSettings class
     * where the data is loaded and the file/information is passed to this method.
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param af
     */
    public void setDocument(int what, Document af) {
        // select the right xml document, depending on which accelerator table is requested
        // TODO wieder entfernen
//        switch (what) {
//            case MAINKEYS: acceleratorKeysMain=af; break;
//            case NEWENTRYKEYS: acceleratorKeysNewEntry=af; break;
//            case DESKTOPKEYS: acceleratorKeysDesktop=af; break;
//            case SEARCHRESULTSKEYS: acceleratorKeysSearchResults=af; break;
//            default: acceleratorKeysMain=af; break;
//        }
    }
    

    /**
     * This method returns the size of one of the xml data files. Following constants should
     * be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @return the size of the requested data file
     */
    public int getCount(int what) {
        return getDocument(what).getRootElement().getContentSize();
    }
    
    
    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param pos (a valid position of an element)
     * @return the string containing the accelerator key or an empty string if nothing was found
     */
    public String getAcceleratorKey(int what, int pos) {
        // retrieve the element
        Element acckey = retrieveElement(what, pos);
        // if the element was not found, return an empty string
        if (null==acckey) {
            return "";
        }
        // else the value (i.e. the accelerator key string)
        return acckey.getText();
    }
    
    
    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     *
     * @param what uses constants, see global field definition at top of source
     * @param actionname the attribute (i.e. the action's name) we want to find
     * @return the string containing the accelerator key or null if nothing was found
     */
    public String getAcceleratorKey(int what, String actionname) {
        // retrieve the element
        Element acckey = retrieveElement(what, actionname);
        // if the element was not found, return an empty string
        if (null==acckey) {
            return null;
        }
        // else the value (i.e. the accelerator key string)
        return acckey.getText();
    }


    /**
     * This methods returns the accelerator key of a given position in the xml-file
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what uses constants, see global field definition at top of source
     * @param pos a valid position of an element
     * @return the string containing the accelerator key or an empty string if nothing was found
     */
    public String getAcceleratorAction(int what, int pos) {
        // retrieve the element
        Element acckey = retrieveElement(what, pos);
        // return the matching string value of the element
        String retval;
        // if the element was not found, return an empty string
        if (null==acckey) {
            retval = "";
        }
        // else the value (i.e. the accelerator key string)
        else {
            retval = acckey.getAttributeValue("action");
        }
        
        return retval;
    }
    
    
    /**
     * This method sets an accelerator key of an related action. To change an accelerator key,
     * provide the action's name and the keystroke-value as string parameters. furthermore, we
     * have to tell the method, to which file the changes should be applied (param what).
     * 
     * Following constants should be used as parameters:<br>
     * MAINKEYS<br>
     * NEWENTRYKEYS<br>
     * DESKTOPKEYS<br>
     * SEARCHRESULTSKEYS<br>
     * 
     * @param what (uses constants, see global field definition at top of source)
     * @param action (the action's name, as string, e.g. "newEntry")
     * @param keystroke (the keystroke, e.g. "ctrl N" (win/linux) or "meta O" (mac)
     */
    public void setAccelerator(int what, String action, String keystroke) {
        // create a list of all elements from the xml file
        try {
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // and an iterator for the loop below
            Iterator<?> iterator = elementList.iterator();

            // counter for the return value if a found element attribute matches the parameter
            int cnt = 1;
            // iterate loop
            while (iterator.hasNext()) {
                // retrieve each single element
                Element acckey = (Element) iterator.next();
                // if action-attribute matches the parameter string...
                if (action.equals(acckey.getAttributeValue("action"))) {
                    // ...set the new keystroke
                    acckey.setText(keystroke);
                    // and leave method
                    return;
                }
                // else increase counter
                cnt++;
            }
        }
        catch (IllegalStateException e) {
            Constants.zknlogger.log(Level.WARNING,e.getLocalizedMessage());
        }
    }
    

    /**
     * This method looks for the occurence of the attribute "attr". All elements of
     * an xml-file are searched for the given attribute. If an element contains that
     * atrtribut, the method returns true, false otherwise.
     * 
     * @param doc (the xml-document where to look for the attribute)
     * @param attr (the attribute we want to find)
     * @return {@code true} if we have an element that contains that attribute, false otherwise
     */
    private boolean findElement(int what, String attr) {
        // create a list of all elements from the acceleratorkeys xml file
        try { 
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // if we have any elements at all, go on
            if (elementList.size()>0) {
                // and an iterator for the loop below
                Iterator<?> iterator = elementList.iterator();
                // iterate loop
                while (iterator.hasNext()) {
                    // retrieve each single element
                    Element entry = (Element) iterator.next();
                    // try to get the requested element
                    String sv = entry.getAttributeValue("action");
                    // if it exists, return true
                    if (sv!=null && sv.equals(attr)) {
                        return true;
                    }
                }
                // if no attribute was found, return false
                return false;
            }
            else {
                return false;
            }
        }
        catch (IllegalStateException e) {
            return false;
        }
    }
    
    
    /**
     * This method looks for the occurence of the attribute "attr". All elements of
     * an xml-file are searched for the given attribute. If an element contains that
     * atrtribut, it is returned.
     *
     * @param doc the xml-document where to look for the attribute
     * @param attr the attribute (i.e. the action's name) we want to find
     * @return the element which matches the given action-name {@code attr} inside the document {@code doc},
     * or null if no match was found
     */
    private Element retrieveElement(int what, String attr) {
        // create a list of all elements from the acceleratorkeys xml file
        try {
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // if we have any elements at all, go on
            if (elementList.size()>0) {
                // and an iterator for the loop below
                Iterator<?> iterator = elementList.iterator();
                // iterate loop
                while (iterator.hasNext()) {
                    // retrieve each single element
                    Element entry = (Element) iterator.next();
                    // try to get the requested element
                    String sv = entry.getAttributeValue("action");
                    // if it exists, return true
                    if (sv!=null && sv.equals(attr)) {
                        return entry;
                    }
                }
            }
            return null;
        }
        catch (IllegalStateException e) {
            return null;
        }
    }


    /**
     * This function retrieves an element of a xml document at a given
     * position. The position is a value from 1 to (size of xml file) - in contrary
     * to usual array handling where the range is from 0 to (size-1).
     * 
     * @param doc (the xml document where to look for elements)
     * @param pos (the position of the element)
     * @return the element if a match was found, otherwise null)
     */
    private Element retrieveElement(int what, int pos) {
        // create a list of all elements from the given xml file
        try { 
            List<?> elementList = getDocument(what).getRootElement().getContent();
            // and return the requestet Element
            try {
                return (Element) elementList.get(pos-1);
            }
            catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        catch (IllegalStateException e) {
            return null;
        }
    }
}
