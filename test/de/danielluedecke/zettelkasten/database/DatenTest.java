/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rgb
 */
public class DatenTest {
    
    public DatenTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initZettelkasten method, of class Daten.
     */
    @Test
    public void testInitZettelkasten() {
        System.out.println("initZettelkasten");
        Daten instance = null;
        instance.initZettelkasten();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVersionInfo method, of class Daten.
     */
    @Test
    public void testGetVersionInfo() {
        System.out.println("getVersionInfo");
        Daten instance = null;
        String expResult = "";
        String result = instance.getVersionInfo();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserAttachmentPath method, of class Daten.
     */
    @Test
    public void testGetUserAttachmentPath() {
        System.out.println("getUserAttachmentPath");
        Daten instance = null;
        File expResult = null;
        File result = instance.getUserAttachmentPath();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUserAttachmentPath method, of class Daten.
     */
    @Test
    public void testSetUserAttachmentPath() {
        System.out.println("setUserAttachmentPath");
        String path = "";
        Daten instance = null;
        instance.setUserAttachmentPath(path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserImagePath method, of class Daten.
     */
    @Test
    public void testGetUserImagePath() {
        System.out.println("getUserImagePath");
        Daten instance = null;
        File expResult = null;
        File result = instance.getUserImagePath();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUserImagePath method, of class Daten.
     */
    @Test
    public void testSetUserImagePath() {
        System.out.println("setUserImagePath");
        String path = "";
        Daten instance = null;
        instance.setUserImagePath(path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentVersionInfo method, of class Daten.
     */
    @Test
    public void testGetCurrentVersionInfo() {
        System.out.println("getCurrentVersionInfo");
        Daten instance = null;
        String expResult = "";
        String result = instance.getCurrentVersionInfo();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMetaModified method, of class Daten.
     */
    @Test
    public void testIsMetaModified() {
        System.out.println("isMetaModified");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isMetaModified();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMetaModified method, of class Daten.
     */
    @Test
    public void testSetMetaModified() {
        System.out.println("setMetaModified");
        boolean m = false;
        Daten instance = null;
        instance.setMetaModified(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isModified method, of class Daten.
     */
    @Test
    public void testIsModified() {
        System.out.println("isModified");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isModified();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModified method, of class Daten.
     */
    @Test
    public void testSetModified() {
        System.out.println("setModified");
        boolean m = false;
        Daten instance = null;
        instance.setModified(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilesToLoadCount method, of class Daten.
     */
    @Test
    public void testGetFilesToLoadCount() {
        System.out.println("getFilesToLoadCount");
        Daten instance = null;
        int expResult = 0;
        int result = instance.getFilesToLoadCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileToLoad method, of class Daten.
     */
    @Test
    public void testGetFileToLoad() {
        System.out.println("getFileToLoad");
        int index = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getFileToLoad(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZknData method, of class Daten.
     */
    @Test
    public void testSetZknData() {
        System.out.println("setZknData");
        Document zkd = null;
        Daten instance = null;
        instance.setZknData(zkd);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZknData method, of class Daten.
     */
    @Test
    public void testGetZknData() {
        System.out.println("getZknData");
        Daten instance = null;
        Document expResult = null;
        Document result = instance.getZknData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isNewVersion method, of class Daten.
     */
    @Test
    public void testIsNewVersion() {
        System.out.println("isNewVersion");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isNewVersion();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIncompatibleFile method, of class Daten.
     */
    @Test
    public void testIsIncompatibleFile() {
        System.out.println("isIncompatibleFile");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isIncompatibleFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of appendZknData method, of class Daten.
     */
    @Test
    public void testAppendZknData() {
        System.out.println("appendZknData");
        Document zkd = null;
        Daten instance = null;
        instance.appendZknData(zkd);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthorData method, of class Daten.
     */
    @Test
    public void testSetAuthorData() {
        System.out.println("setAuthorData");
        Document ald = null;
        Daten instance = null;
        instance.setAuthorData(ald);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorData method, of class Daten.
     */
    @Test
    public void testGetAuthorData() {
        System.out.println("getAuthorData");
        Daten instance = null;
        Document expResult = null;
        Document result = instance.getAuthorData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKeywordData method, of class Daten.
     */
    @Test
    public void testSetKeywordData() {
        System.out.println("setKeywordData");
        Document kld = null;
        Daten instance = null;
        instance.setKeywordData(kld);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordData method, of class Daten.
     */
    @Test
    public void testGetKeywordData() {
        System.out.println("getKeywordData");
        Daten instance = null;
        Document expResult = null;
        Document result = instance.getKeywordData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMetaInformationData method, of class Daten.
     */
    @Test
    public void testSetMetaInformationData() {
        System.out.println("setMetaInformationData");
        Document mid = null;
        Daten instance = null;
        instance.setMetaInformationData(mid);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMetaInformationData method, of class Daten.
     */
    @Test
    public void testGetMetaInformationData() {
        System.out.println("getMetaInformationData");
        Daten instance = null;
        Document expResult = null;
        Document result = instance.getMetaInformationData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCompleteZknData method, of class Daten.
     */
    @Test
    public void testSetCompleteZknData() {
        System.out.println("setCompleteZknData");
        Document zkd = null;
        Document ald = null;
        Document kld = null;
        Document mid = null;
        Daten instance = null;
        instance.setCompleteZknData(zkd, ald, kld, mid);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZknDescription method, of class Daten.
     */
    @Test
    public void testGetZknDescription() {
        System.out.println("getZknDescription");
        Daten instance = null;
        String expResult = "";
        String result = instance.getZknDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZknDescription method, of class Daten.
     */
    @Test
    public void testSetZknDescription() {
        System.out.println("setZknDescription");
        String desc = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.setZknDescription(desc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addZknDescription method, of class Daten.
     */
    @Test
    public void testAddZknDescription() {
        System.out.println("addZknDescription");
        String desc = "";
        Daten instance = null;
        instance.addZknDescription(desc);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of duplicateEntry method, of class Daten.
     */
    @Test
    public void testDuplicateEntry() {
        System.out.println("duplicateEntry");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.duplicateEntry(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateVersionInfo method, of class Daten.
     */
    @Test
    public void testUpdateVersionInfo() {
        System.out.println("updateVersionInfo");
        Daten instance = null;
        instance.updateVersionInfo();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveZettel method, of class Daten.
     */
    @Test
    public void testRetrieveZettel() {
        System.out.println("retrieveZettel");
        int pos = 0;
        Daten instance = null;
        Element expResult = null;
        Element result = instance.retrieveZettel(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findKeywordInDatabase method, of class Daten.
     */
    @Test
    public void testFindKeywordInDatabase() {
        System.out.println("findKeywordInDatabase");
        String kw = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.findKeywordInDatabase(kw);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordPosition method, of class Daten.
     */
    @Test
    public void testGetKeywordPosition() {
        System.out.println("getKeywordPosition");
        String kw = "";
        boolean matchcase = false;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getKeywordPosition(kw, matchcase);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBibkeyPosition method, of class Daten.
     */
    @Test
    public void testGetBibkeyPosition() {
        System.out.println("getBibkeyPosition");
        String bibkey = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getBibkeyPosition(bibkey);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addKeyword method, of class Daten.
     */
    @Test
    public void testAddKeyword() {
        System.out.println("addKeyword");
        String kw = "";
        int freq = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.addKeyword(kw, freq);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addKeywordsToDatabase method, of class Daten.
     */
    @Test
    public void testAddKeywordsToDatabase() {
        System.out.println("addKeywordsToDatabase");
        String[] kws = null;
        Daten instance = null;
        instance.addKeywordsToDatabase(kws);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeyword method, of class Daten.
     */
    @Test
    public void testGetKeyword() {
        System.out.println("getKeyword");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getKeyword(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKeyword method, of class Daten.
     */
    @Test
    public void testSetKeyword_int_String() {
        System.out.println("setKeyword");
        int pos = 0;
        String kw = "";
        Daten instance = null;
        instance.setKeyword(pos, kw);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKeyword method, of class Daten.
     */
    @Test
    public void testSetKeyword_3args() {
        System.out.println("setKeyword");
        int pos = 0;
        String kw = "";
        int freq = 0;
        Daten instance = null;
        instance.setKeyword(pos, kw, freq);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteKeyword method, of class Daten.
     */
    @Test
    public void testDeleteKeyword() {
        System.out.println("deleteKeyword");
        int pos = 0;
        Daten instance = null;
        instance.deleteKeyword(pos);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteZettel method, of class Daten.
     */
    @Test
    public void testDeleteZettel() {
        System.out.println("deleteZettel");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.deleteZettel(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAuthorsFromEntry method, of class Daten.
     */
    @Test
    public void testDeleteAuthorsFromEntry() {
        System.out.println("deleteAuthorsFromEntry");
        String[] aus = null;
        int nr = 0;
        Daten instance = null;
        instance.deleteAuthorsFromEntry(aus, nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteKeywordsFromEntry method, of class Daten.
     */
    @Test
    public void testDeleteKeywordsFromEntry() {
        System.out.println("deleteKeywordsFromEntry");
        String[] kws = null;
        int nr = 0;
        Daten instance = null;
        instance.deleteKeywordsFromEntry(kws, nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveNonexistingKeywords method, of class Daten.
     */
    @Test
    public void testRetrieveNonexistingKeywords() {
        System.out.println("retrieveNonexistingKeywords");
        String[] kws = null;
        int nr = 0;
        boolean matchcase = false;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.retrieveNonexistingKeywords(kws, nr, matchcase);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existsInKeywords method, of class Daten.
     */
    @Test
    public void testExistsInKeywords_4args() {
        System.out.println("existsInKeywords");
        String[] kws = null;
        int nr = 0;
        boolean log_and = false;
        boolean matchcase = false;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.existsInKeywords(kws, nr, log_and, matchcase);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existsInKeywords method, of class Daten.
     */
    @Test
    public void testExistsInKeywords_3args() {
        System.out.println("existsInKeywords");
        String kw = "";
        int nr = 0;
        boolean matchcase = false;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.existsInKeywords(kw, nr, matchcase);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existsInAuthors method, of class Daten.
     */
    @Test
    public void testExistsInAuthors_String_int() {
        System.out.println("existsInAuthors");
        String au = "";
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.existsInAuthors(au, nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of existsInAuthors method, of class Daten.
     */
    @Test
    public void testExistsInAuthors_int_int() {
        System.out.println("existsInAuthors");
        int authorindexnumber = 0;
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.existsInAuthors(authorindexnumber, nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mergeKeywords method, of class Daten.
     */
    @Test
    public void testMergeKeywords() {
        System.out.println("mergeKeywords");
        String oldkw = "";
        String newkw = "";
        Daten instance = null;
        instance.mergeKeywords(oldkw, newkw);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mergeAuthors method, of class Daten.
     */
    @Test
    public void testMergeAuthors() {
        System.out.println("mergeAuthors");
        String oldau = "";
        String newau = "";
        Daten instance = null;
        instance.mergeAuthors(oldau, newau);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addKeywordToEntry method, of class Daten.
     */
    @Test
    public void testAddKeywordToEntry() {
        System.out.println("addKeywordToEntry");
        String kw = "";
        int nr = 0;
        int freq = 0;
        Daten instance = null;
        instance.addKeywordToEntry(kw, nr, freq);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addKeywordsToEntry method, of class Daten.
     */
    @Test
    public void testAddKeywordsToEntry() {
        System.out.println("addKeywordsToEntry");
        String[] kws = null;
        int nr = 0;
        int freq = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.addKeywordsToEntry(kws, nr, freq);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAuthorToEntry method, of class Daten.
     */
    @Test
    public void testAddAuthorToEntry() {
        System.out.println("addAuthorToEntry");
        String au = "";
        int nr = 0;
        int freq = 0;
        Daten instance = null;
        instance.addAuthorToEntry(au, nr, freq);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findAuthorInDatabase method, of class Daten.
     */
    @Test
    public void testFindAuthorInDatabase() {
        System.out.println("findAuthorInDatabase");
        String auth = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.findAuthorInDatabase(auth);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorPosition method, of class Daten.
     */
    @Test
    public void testGetAuthorPosition() {
        System.out.println("getAuthorPosition");
        String auth = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorPosition(auth);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorBibKeyPosition method, of class Daten.
     */
    @Test
    public void testGetAuthorBibKeyPosition() {
        System.out.println("getAuthorBibKeyPosition");
        String bibkey = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorBibKeyPosition(bibkey);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAuthor method, of class Daten.
     */
    @Test
    public void testAddAuthor() {
        System.out.println("addAuthor");
        String auth = "";
        int freq = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.addAuthor(auth, freq);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addEntry method, of class Daten.
     */
    @Test
    public void testAddEntry_10args() {
        System.out.println("addEntry");
        String title = "";
        String content = "";
        String[] authors = null;
        String[] keywords = null;
        String remarks = "";
        String[] links = null;
        String timestamp = "";
        int luhmann = 0;
        boolean editDeletedEntry = false;
        int editDeletedEntryPosition = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.addEntry(title, content, authors, keywords, remarks, links, timestamp, luhmann, editDeletedEntry, editDeletedEntryPosition);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addEntry method, of class Daten.
     */
    @Test
    public void testAddEntry_8args() {
        System.out.println("addEntry");
        String title = "";
        String content = "";
        String[] authors = null;
        String[] keywords = null;
        String remarks = "";
        String[] links = null;
        String timestamp = "";
        int luhmann = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.addEntry(title, content, authors, keywords, remarks, links, timestamp, luhmann);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addEntryFromBibTex method, of class Daten.
     */
    @Test
    public void testAddEntryFromBibTex() {
        System.out.println("addEntryFromBibTex");
        String title = "";
        String content = "";
        String[] authors = null;
        String[] keywords = null;
        String timestamp = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.addEntryFromBibTex(title, content, authors, keywords, timestamp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContentFromBibTexRemark method, of class Daten.
     */
    @Test
    public void testSetContentFromBibTexRemark() {
        System.out.println("setContentFromBibTexRemark");
        int pos = 0;
        boolean val = false;
        Daten instance = null;
        instance.setContentFromBibTexRemark(pos, val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isContentFromBibTex method, of class Daten.
     */
    @Test
    public void testIsContentFromBibTex() {
        System.out.println("isContentFromBibTex");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isContentFromBibTex(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeEntry method, of class Daten.
     */
    @Test
    public void testChangeEntry() {
        System.out.println("changeEntry");
        String title = "";
        String content = "";
        String[] authors = null;
        String[] keywords = null;
        String remarks = "";
        String[] links = null;
        String timestamp = "";
        int entrynumber = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.changeEntry(title, content, authors, keywords, remarks, links, timestamp, entrynumber);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addLuhmannNumber method, of class Daten.
     */
    @Test
    public void testAddLuhmannNumber() {
        System.out.println("addLuhmannNumber");
        int entry = 0;
        int addvalue = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.addLuhmannNumber(entry, addvalue);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addManualLink method, of class Daten.
     */
    @Test
    public void testAddManualLink_int_int() {
        System.out.println("addManualLink");
        int entry = 0;
        int addvalue = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.addManualLink(entry, addvalue);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addManualLink method, of class Daten.
     */
    @Test
    public void testAddManualLink_List_int() {
        System.out.println("addManualLink");
        List<Integer> manlinks = null;
        int sourceEntry = 0;
        Daten instance = null;
        instance.addManualLink(manlinks, sourceEntry);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addManualLink method, of class Daten.
     */
    @Test
    public void testAddManualLink_int_List() {
        System.out.println("addManualLink");
        int sourceEntry = 0;
        List<Integer> manlinks = null;
        Daten instance = null;
        instance.addManualLink(sourceEntry, manlinks);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteLuhmannNumber method, of class Daten.
     */
    @Test
    public void testDeleteLuhmannNumber() {
        System.out.println("deleteLuhmannNumber");
        int entry = 0;
        int removevalue = 0;
        Daten instance = null;
        instance.deleteLuhmannNumber(entry, removevalue);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertLuhmannNumber method, of class Daten.
     */
    @Test
    public void testInsertLuhmannNumber() {
        System.out.println("insertLuhmannNumber");
        int entry = 0;
        int insertnr = 0;
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.insertLuhmannNumber(entry, insertnr, pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteManualLinks method, of class Daten.
     */
    @Test
    public void testDeleteManualLinks_StringArr_int() {
        System.out.println("deleteManualLinks");
        String[] manlinks = null;
        int zpos = 0;
        Daten instance = null;
        instance.deleteManualLinks(manlinks, zpos);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteManualLinks method, of class Daten.
     */
    @Test
    public void testDeleteManualLinks_StringArr() {
        System.out.println("deleteManualLinks");
        String[] manlinks = null;
        Daten instance = null;
        instance.deleteManualLinks(manlinks);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLuhmannNumbers method, of class Daten.
     */
    @Test
    public void testGetLuhmannNumbers() {
        System.out.println("getLuhmannNumbers");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getLuhmannNumbers(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLuhmannNumbersAsString method, of class Daten.
     */
    @Test
    public void testGetLuhmannNumbersAsString() {
        System.out.println("getLuhmannNumbersAsString");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getLuhmannNumbersAsString(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLuhmannNumbersAsInteger method, of class Daten.
     */
    @Test
    public void testGetLuhmannNumbersAsInteger() {
        System.out.println("getLuhmannNumbersAsInteger");
        int pos = 0;
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getLuhmannNumbersAsInteger(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getManualLinks method, of class Daten.
     */
    @Test
    public void testGetManualLinks() {
        System.out.println("getManualLinks");
        int pos = 0;
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getManualLinks(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setManualLinks method, of class Daten.
     */
    @Test
    public void testSetManualLinks_int_intArr() {
        System.out.println("setManualLinks");
        int pos = 0;
        int[] manlinks = null;
        Daten instance = null;
        instance.setManualLinks(pos, manlinks);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getManualLinksAsString method, of class Daten.
     */
    @Test
    public void testGetManualLinksAsString() {
        System.out.println("getManualLinksAsString");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getManualLinksAsString(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getManualLinksAsSingleString method, of class Daten.
     */
    @Test
    public void testGetManualLinksAsSingleString() {
        System.out.println("getManualLinksAsSingleString");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getManualLinksAsSingleString(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setManualLinks method, of class Daten.
     */
    @Test
    public void testSetManualLinks_int_String() {
        System.out.println("setManualLinks");
        int pos = 0;
        String manlinks = "";
        Daten instance = null;
        instance.setManualLinks(pos, manlinks);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentManualLinks method, of class Daten.
     */
    @Test
    public void testGetCurrentManualLinks() {
        System.out.println("getCurrentManualLinks");
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getCurrentManualLinks();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentManualLinksAsString method, of class Daten.
     */
    @Test
    public void testGetCurrentManualLinksAsString() {
        System.out.println("getCurrentManualLinksAsString");
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getCurrentManualLinksAsString();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthor method, of class Daten.
     */
    @Test
    public void testGetAuthor() {
        System.out.println("getAuthor");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getAuthor(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthor method, of class Daten.
     */
    @Test
    public void testSetAuthor_int_String() {
        System.out.println("setAuthor");
        int pos = 0;
        String auth = "";
        Daten instance = null;
        instance.setAuthor(pos, auth);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthor method, of class Daten.
     */
    @Test
    public void testSetAuthor_3args_1() {
        System.out.println("setAuthor");
        int pos = 0;
        String auth = "";
        String bibkey = "";
        Daten instance = null;
        instance.setAuthor(pos, auth, bibkey);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthor method, of class Daten.
     */
    @Test
    public void testSetAuthor_3args_2() {
        System.out.println("setAuthor");
        int pos = 0;
        String auth = "";
        int freq = 0;
        Daten instance = null;
        instance.setAuthor(pos, auth, freq);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthor method, of class Daten.
     */
    @Test
    public void testSetAuthor_4args() {
        System.out.println("setAuthor");
        int pos = 0;
        String auth = "";
        String bibkey = "";
        int freq = 0;
        Daten instance = null;
        instance.setAuthor(pos, auth, bibkey, freq);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAuthor method, of class Daten.
     */
    @Test
    public void testDeleteAuthor() {
        System.out.println("deleteAuthor");
        int pos = 0;
        Daten instance = null;
        instance.deleteAuthor(pos);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntryAsHtml method, of class Daten.
     */
    @Test
    public void testGetEntryAsHtml() {
        System.out.println("getEntryAsHtml");
        int pos = 0;
        String[] segmentKeywords = null;
        int sourceframe = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getEntryAsHtml(pos, segmentKeywords, sourceframe);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelRating method, of class Daten.
     */
    @Test
    public void testGetZettelRating() {
        System.out.println("getZettelRating");
        int nr = 0;
        Daten instance = null;
        float expResult = 0.0F;
        float result = instance.getZettelRating(nr);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelRatingCount method, of class Daten.
     */
    @Test
    public void testGetZettelRatingCount() {
        System.out.println("getZettelRatingCount");
        int nr = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getZettelRatingCount(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelRatingAsString method, of class Daten.
     */
    @Test
    public void testGetZettelRatingAsString() {
        System.out.println("getZettelRatingAsString");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelRatingAsString(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addZettelRating method, of class Daten.
     */
    @Test
    public void testAddZettelRating() {
        System.out.println("addZettelRating");
        int nr = 0;
        float rate = 0.0F;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.addZettelRating(nr, rate);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetZettelRating method, of class Daten.
     */
    @Test
    public void testResetZettelRating() {
        System.out.println("resetZettelRating");
        int nr = 0;
        Daten instance = null;
        instance.resetZettelRating(nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEmpty method, of class Daten.
     */
    @Test
    public void testIsEmpty_int() {
        System.out.println("isEmpty");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isEmpty(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEmpty method, of class Daten.
     */
    @Test
    public void testIsEmpty_Element() {
        System.out.println("isEmpty");
        Element entry = null;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isEmpty(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDeleted method, of class Daten.
     */
    @Test
    public void testIsDeleted_int() {
        System.out.println("isDeleted");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isDeleted(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDeleted method, of class Daten.
     */
    @Test
    public void testIsDeleted_Element() {
        System.out.println("isDeleted");
        Element entry = null;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isDeleted(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasEntriesExcludingDeleted method, of class Daten.
     */
    @Test
    public void testHasEntriesExcludingDeleted() {
        System.out.println("hasEntriesExcludingDeleted");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasEntriesExcludingDeleted();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestamp method, of class Daten.
     */
    @Test
    public void testGetTimestamp_int() {
        System.out.println("getTimestamp");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getTimestamp(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestamp method, of class Daten.
     */
    @Test
    public void testGetTimestamp_Element() {
        System.out.println("getTimestamp");
        Element entry = null;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getTimestamp(entry);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestampEdited method, of class Daten.
     */
    @Test
    public void testGetTimestampEdited_Element() {
        System.out.println("getTimestampEdited");
        Element entry = null;
        Daten instance = null;
        String expResult = "";
        String result = instance.getTimestampEdited(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestampEdited method, of class Daten.
     */
    @Test
    public void testGetTimestampEdited_int() {
        System.out.println("getTimestampEdited");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getTimestampEdited(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestampCreated method, of class Daten.
     */
    @Test
    public void testGetTimestampCreated_Element() {
        System.out.println("getTimestampCreated");
        Element entry = null;
        Daten instance = null;
        String expResult = "";
        String result = instance.getTimestampCreated(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestampCreated method, of class Daten.
     */
    @Test
    public void testGetTimestampCreated_int() {
        System.out.println("getTimestampCreated");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getTimestampCreated(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAttachments method, of class Daten.
     */
    @Test
    public void testGetAttachments() {
        System.out.println("getAttachments");
        int pos = 0;
        Daten instance = null;
        List<Element> expResult = null;
        List<Element> result = instance.getAttachments(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasAttachments method, of class Daten.
     */
    @Test
    public void testHasAttachments() {
        System.out.println("hasAttachments");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasAttachments(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasAuthors method, of class Daten.
     */
    @Test
    public void testHasAuthors() {
        System.out.println("hasAuthors");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasAuthors(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasKeywords method, of class Daten.
     */
    @Test
    public void testHasKeywords() {
        System.out.println("hasKeywords");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasKeywords(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasRemarks method, of class Daten.
     */
    @Test
    public void testHasRemarks() {
        System.out.println("hasRemarks");
        int pos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasRemarks(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAttachmentsAsString method, of class Daten.
     */
    @Test
    public void testGetAttachmentsAsString() {
        System.out.println("getAttachmentsAsString");
        int pos = 0;
        boolean makeLinkToAttachment = false;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getAttachmentsAsString(pos, makeLinkToAttachment);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAttachments method, of class Daten.
     */
    @Test
    public void testSetAttachments() {
        System.out.println("setAttachments");
        int pos = 0;
        String[] attachments = null;
        Daten instance = null;
        instance.setAttachments(pos, attachments);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAttachments method, of class Daten.
     */
    @Test
    public void testAddAttachments() {
        System.out.println("addAttachments");
        int pos = 0;
        String[] attachments = null;
        Daten instance = null;
        instance.addAttachments(pos, attachments);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeAttachment method, of class Daten.
     */
    @Test
    public void testChangeAttachment() {
        System.out.println("changeAttachment");
        String oldAttachment = "";
        String newAttachment = "";
        int entrynr = 0;
        Daten instance = null;
        instance.changeAttachment(oldAttachment, newAttachment, entrynr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAttachment method, of class Daten.
     */
    @Test
    public void testDeleteAttachment() {
        System.out.println("deleteAttachment");
        String value = "";
        int entrynr = 0;
        Daten instance = null;
        instance.deleteAttachment(value, entrynr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRemarks method, of class Daten.
     */
    @Test
    public void testGetRemarks() {
        System.out.println("getRemarks");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getRemarks(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRemarks method, of class Daten.
     */
    @Test
    public void testSetRemarks() {
        System.out.println("setRemarks");
        int pos = 0;
        String remarks = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.setRemarks(pos, remarks);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCleanRemarks method, of class Daten.
     */
    @Test
    public void testGetCleanRemarks() {
        System.out.println("getCleanRemarks");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getCleanRemarks(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentKeywords method, of class Daten.
     */
    @Test
    public void testGetCurrentKeywords() {
        System.out.println("getCurrentKeywords");
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getCurrentKeywords();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywords method, of class Daten.
     */
    @Test
    public void testGetKeywords_int() {
        System.out.println("getKeywords");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getKeywords(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywords method, of class Daten.
     */
    @Test
    public void testGetKeywords_int_boolean() {
        System.out.println("getKeywords");
        int pos = 0;
        boolean sort = false;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getKeywords(pos, sort);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSeparatedKeywords method, of class Daten.
     */
    @Test
    public void testGetSeparatedKeywords() {
        System.out.println("getSeparatedKeywords");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getSeparatedKeywords(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthors method, of class Daten.
     */
    @Test
    public void testGetAuthors() {
        System.out.println("getAuthors");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getAuthors(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorsWithIDandBibKey method, of class Daten.
     */
    @Test
    public void testGetAuthorsWithIDandBibKey() {
        System.out.println("getAuthorsWithIDandBibKey");
        int pos = 0;
        Daten instance = null;
        String[] expResult = null;
        String[] result = instance.getAuthorsWithIDandBibKey(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCount method, of class Daten.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        int what = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getCount(what);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addToHistory method, of class Daten.
     */
    @Test
    public void testAddToHistory() {
        System.out.println("addToHistory");
        int entrynr = 0;
        Daten instance = null;
        instance.addToHistory(entrynr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canHistoryBack method, of class Daten.
     */
    @Test
    public void testCanHistoryBack() {
        System.out.println("canHistoryBack");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.canHistoryBack();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canHistoryFore method, of class Daten.
     */
    @Test
    public void testCanHistoryFore() {
        System.out.println("canHistoryFore");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.canHistoryFore();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of historyBack method, of class Daten.
     */
    @Test
    public void testHistoryBack() {
        System.out.println("historyBack");
        Daten instance = null;
        instance.historyBack();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of historyFore method, of class Daten.
     */
    @Test
    public void testHistoryFore() {
        System.out.println("historyFore");
        Daten instance = null;
        instance.historyFore();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoEntry method, of class Daten.
     */
    @Test
    public void testGotoEntry() {
        System.out.println("gotoEntry");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.gotoEntry(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextEntry method, of class Daten.
     */
    @Test
    public void testNextEntry() {
        System.out.println("nextEntry");
        Daten instance = null;
        instance.nextEntry();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prevEntry method, of class Daten.
     */
    @Test
    public void testPrevEntry() {
        System.out.println("prevEntry");
        Daten instance = null;
        instance.prevEntry();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of firstEntry method, of class Daten.
     */
    @Test
    public void testFirstEntry() {
        System.out.println("firstEntry");
        Daten instance = null;
        instance.firstEntry();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of lastEntry method, of class Daten.
     */
    @Test
    public void testLastEntry() {
        System.out.println("lastEntry");
        Daten instance = null;
        instance.lastEntry();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordIndexNumbers method, of class Daten.
     */
    @Test
    public void testGetKeywordIndexNumbers() {
        System.out.println("getKeywordIndexNumbers");
        int pos = 0;
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getKeywordIndexNumbers(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKeywordIndexNumbers method, of class Daten.
     */
    @Test
    public void testSetKeywordIndexNumbers() {
        System.out.println("setKeywordIndexNumbers");
        int pos = 0;
        String kws = "";
        Daten instance = null;
        instance.setKeywordIndexNumbers(pos, kws);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthorIndexNumbers method, of class Daten.
     */
    @Test
    public void testSetAuthorIndexNumbers() {
        System.out.println("setAuthorIndexNumbers");
        int pos = 0;
        String aus = "";
        Daten instance = null;
        instance.setAuthorIndexNumbers(pos, aus);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorIndexNumbers method, of class Daten.
     */
    @Test
    public void testGetAuthorIndexNumbers() {
        System.out.println("getAuthorIndexNumbers");
        int pos = 0;
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getAuthorIndexNumbers(pos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordFrequencies method, of class Daten.
     */
    @Test
    public void testGetKeywordFrequencies_int() {
        System.out.println("getKeywordFrequencies");
        int pos = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getKeywordFrequencies(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordFrequencies method, of class Daten.
     */
    @Test
    public void testGetKeywordFrequencies_String() {
        System.out.println("getKeywordFrequencies");
        String kw = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getKeywordFrequencies(kw);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordFrequency method, of class Daten.
     */
    @Test
    public void testGetKeywordFrequency() {
        System.out.println("getKeywordFrequency");
        int pos = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getKeywordFrequency(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorFrequencies method, of class Daten.
     */
    @Test
    public void testGetAuthorFrequencies_int() {
        System.out.println("getAuthorFrequencies");
        int pos = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorFrequencies(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorFrequencies method, of class Daten.
     */
    @Test
    public void testGetAuthorFrequencies_String() {
        System.out.println("getAuthorFrequencies");
        String au = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorFrequencies(au);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorFrequency method, of class Daten.
     */
    @Test
    public void testGetAuthorFrequency() {
        System.out.println("getAuthorFrequency");
        int pos = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorFrequency(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorBibKey method, of class Daten.
     */
    @Test
    public void testGetAuthorBibKey_int() {
        System.out.println("getAuthorBibKey");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getAuthorBibKey(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorBibKey method, of class Daten.
     */
    @Test
    public void testGetAuthorBibKey_String() {
        System.out.println("getAuthorBibKey");
        String au = "";
        Daten instance = null;
        String expResult = "";
        String result = instance.getAuthorBibKey(au);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthorBibKey method, of class Daten.
     */
    @Test
    public void testSetAuthorBibKey_int_String() {
        System.out.println("setAuthorBibKey");
        int pos = 0;
        String key = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.setAuthorBibKey(pos, key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthorBibKey method, of class Daten.
     */
    @Test
    public void testSetAuthorBibKey_String_String() {
        System.out.println("setAuthorBibKey");
        String au = "";
        String key = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.setAuthorBibKey(au, key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCurrentZettelPos method, of class Daten.
     */
    @Test
    public void testSetCurrentZettelPos() {
        System.out.println("setCurrentZettelPos");
        int nr = 0;
        Daten instance = null;
        instance.setCurrentZettelPos(nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInitialHistoryPos method, of class Daten.
     */
    @Test
    public void testSetInitialHistoryPos() {
        System.out.println("setInitialHistoryPos");
        int nr = 0;
        Daten instance = null;
        instance.setInitialHistoryPos(nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentZettelPos method, of class Daten.
     */
    @Test
    public void testGetCurrentZettelPos() {
        System.out.println("getCurrentZettelPos");
        Daten instance = null;
        int expResult = 0;
        int result = instance.getCurrentZettelPos();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLinkStrength method, of class Daten.
     */
    @Test
    public void testGetLinkStrength() {
        System.out.println("getLinkStrength");
        int sourceentry = 0;
        int destentry = 0;
        Daten instance = null;
        int expResult = 0;
        int result = instance.getLinkStrength(sourceentry, destentry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelTitle method, of class Daten.
     */
    @Test
    public void testGetZettelTitle() {
        System.out.println("getZettelTitle");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelTitle(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZettelTitle method, of class Daten.
     */
    @Test
    public void testSetZettelTitle() {
        System.out.println("setZettelTitle");
        int pos = 0;
        String title = "";
        Daten instance = null;
        instance.setZettelTitle(pos, title);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeEditTimeStamp method, of class Daten.
     */
    @Test
    public void testChangeEditTimeStamp() {
        System.out.println("changeEditTimeStamp");
        int pos = 0;
        Daten instance = null;
        instance.changeEditTimeStamp(pos);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelContent method, of class Daten.
     */
    @Test
    public void testGetZettelContent_int() {
        System.out.println("getZettelContent");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelContent(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelContentAsHtml method, of class Daten.
     */
    @Test
    public void testGetZettelContentAsHtml() {
        System.out.println("getZettelContentAsHtml");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelContentAsHtml(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelContent method, of class Daten.
     */
    @Test
    public void testGetZettelContent_int_boolean() {
        System.out.println("getZettelContent");
        int pos = 0;
        boolean encodeUTF = false;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelContent(pos, encodeUTF);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZettelContent method, of class Daten.
     */
    @Test
    public void testSetZettelContent() {
        System.out.println("setZettelContent");
        int pos = 0;
        String content = "";
        boolean changetimestamp = false;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.setZettelContent(pos, content, changetimestamp);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCleanZettelContent method, of class Daten.
     */
    @Test
    public void testGetCleanZettelContent() {
        System.out.println("getCleanZettelContent");
        int pos = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getCleanZettelContent(pos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setKeywordlistUpToDate method, of class Daten.
     */
    @Test
    public void testSetKeywordlistUpToDate() {
        System.out.println("setKeywordlistUpToDate");
        boolean val = false;
        Daten instance = null;
        instance.setKeywordlistUpToDate(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isKeywordlistUpToDate method, of class Daten.
     */
    @Test
    public void testIsKeywordlistUpToDate() {
        System.out.println("isKeywordlistUpToDate");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isKeywordlistUpToDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setClusterlistUpToDate method, of class Daten.
     */
    @Test
    public void testSetClusterlistUpToDate() {
        System.out.println("setClusterlistUpToDate");
        boolean val = false;
        Daten instance = null;
        instance.setClusterlistUpToDate(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isClusterlistUpToDate method, of class Daten.
     */
    @Test
    public void testIsClusterlistUpToDate() {
        System.out.println("isClusterlistUpToDate");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isClusterlistUpToDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAuthorlistUpToDate method, of class Daten.
     */
    @Test
    public void testSetAuthorlistUpToDate() {
        System.out.println("setAuthorlistUpToDate");
        boolean val = false;
        Daten instance = null;
        instance.setAuthorlistUpToDate(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAuthorlistUpToDate method, of class Daten.
     */
    @Test
    public void testIsAuthorlistUpToDate() {
        System.out.println("isAuthorlistUpToDate");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isAuthorlistUpToDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTitlelistUpToDate method, of class Daten.
     */
    @Test
    public void testSetTitlelistUpToDate() {
        System.out.println("setTitlelistUpToDate");
        boolean val = false;
        Daten instance = null;
        instance.setTitlelistUpToDate(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isTitlelistUpToDate method, of class Daten.
     */
    @Test
    public void testIsTitlelistUpToDate() {
        System.out.println("isTitlelistUpToDate");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isTitlelistUpToDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAttachmentlistUpToDate method, of class Daten.
     */
    @Test
    public void testSetAttachmentlistUpToDate() {
        System.out.println("setAttachmentlistUpToDate");
        boolean val = false;
        Daten instance = null;
        instance.setAttachmentlistUpToDate(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAttachmentlistUpToDate method, of class Daten.
     */
    @Test
    public void testIsAttachmentlistUpToDate() {
        System.out.println("isAttachmentlistUpToDate");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isAttachmentlistUpToDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createExportEntries method, of class Daten.
     */
    @Test
    public void testCreateExportEntries() {
        System.out.println("createExportEntries");
        ArrayList<Integer> entrynumbers = null;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.createExportEntries(entrynumbers);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveExportDocument method, of class Daten.
     */
    @Test
    public void testRetrieveExportDocument() {
        System.out.println("retrieveExportDocument");
        Daten instance = null;
        Document expResult = null;
        Document result = instance.retrieveExportDocument();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fixWrongEditTags method, of class Daten.
     */
    @Test
    public void testFixWrongEditTags() {
        System.out.println("fixWrongEditTags");
        Daten instance = null;
        instance.fixWrongEditTags();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of db_updateZettelIDs method, of class Daten.
     */
    @Test
    public void testDb_updateZettelIDs() {
        System.out.println("db_updateZettelIDs");
        Daten instance = null;
        instance.db_updateZettelIDs();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of db_updateAuthorAndKeywordIDs method, of class Daten.
     */
    @Test
    public void testDb_updateAuthorAndKeywordIDs() {
        System.out.println("db_updateAuthorAndKeywordIDs");
        Daten instance = null;
        instance.db_updateAuthorAndKeywordIDs();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of db_updateTimestampAttributes method, of class Daten.
     */
    @Test
    public void testDb_updateTimestampAttributes() {
        System.out.println("db_updateTimestampAttributes");
        Daten instance = null;
        instance.db_updateTimestampAttributes();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of db_updateRemoveZettelPosElements method, of class Daten.
     */
    @Test
    public void testDb_updateRemoveZettelPosElements() {
        System.out.println("db_updateRemoveZettelPosElements");
        Daten instance = null;
        instance.db_updateRemoveZettelPosElements();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of db_updateInlineCodeFormatting method, of class Daten.
     */
    @Test
    public void testDb_updateInlineCodeFormatting() {
        System.out.println("db_updateInlineCodeFormatting");
        Daten instance = null;
        instance.db_updateInlineCodeFormatting();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestamp method, of class Daten.
     */
    @Test
    public void testSetTimestamp_3args_1() {
        System.out.println("setTimestamp");
        Element zettel = null;
        String created = "";
        String edited = "";
        Daten instance = null;
        instance.setTimestamp(zettel, created, edited);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestampEdited method, of class Daten.
     */
    @Test
    public void testSetTimestampEdited_Element_String() {
        System.out.println("setTimestampEdited");
        Element zettel = null;
        String edited = "";
        Daten instance = null;
        instance.setTimestampEdited(zettel, edited);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestampCreated method, of class Daten.
     */
    @Test
    public void testSetTimestampCreated_Element_String() {
        System.out.println("setTimestampCreated");
        Element zettel = null;
        String created = "";
        Daten instance = null;
        instance.setTimestampCreated(zettel, created);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestampEdited method, of class Daten.
     */
    @Test
    public void testSetTimestampEdited_int_String() {
        System.out.println("setTimestampEdited");
        int nr = 0;
        String edited = "";
        Daten instance = null;
        instance.setTimestampEdited(nr, edited);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestampCreated method, of class Daten.
     */
    @Test
    public void testSetTimestampCreated_int_String() {
        System.out.println("setTimestampCreated");
        int nr = 0;
        String created = "";
        Daten instance = null;
        instance.setTimestampCreated(nr, created);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestamp method, of class Daten.
     */
    @Test
    public void testSetTimestamp_3args_2() {
        System.out.println("setTimestamp");
        int nr = 0;
        String created = "";
        String edited = "";
        Daten instance = null;
        instance.setTimestamp(nr, created, edited);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZettelID method, of class Daten.
     */
    @Test
    public void testSetZettelID_int() {
        System.out.println("setZettelID");
        int nr = 0;
        Daten instance = null;
        instance.setZettelID(nr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setZettelID method, of class Daten.
     */
    @Test
    public void testSetZettelID_Element() {
        System.out.println("setZettelID");
        Element zettel = null;
        Daten instance = null;
        instance.setZettelID(zettel);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelID method, of class Daten.
     */
    @Test
    public void testGetZettelID_int() {
        System.out.println("getZettelID");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelID(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelID method, of class Daten.
     */
    @Test
    public void testGetZettelID_Element() {
        System.out.println("getZettelID");
        Element zettel = null;
        Daten instance = null;
        String expResult = "";
        String result = instance.getZettelID(zettel);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLastAddedZettelID method, of class Daten.
     */
    @Test
    public void testSetLastAddedZettelID() {
        System.out.println("setLastAddedZettelID");
        Element zettel = null;
        Daten instance = null;
        instance.setLastAddedZettelID(zettel);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLastAddedZettelID method, of class Daten.
     */
    @Test
    public void testGetLastAddedZettelID() {
        System.out.println("getLastAddedZettelID");
        Daten instance = null;
        String expResult = "";
        String result = instance.getLastAddedZettelID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorID method, of class Daten.
     */
    @Test
    public void testGetAuthorID_int() {
        System.out.println("getAuthorID");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getAuthorID(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorID method, of class Daten.
     */
    @Test
    public void testGetAuthorID_String() {
        System.out.println("getAuthorID");
        String au = "";
        Daten instance = null;
        String expResult = "";
        String result = instance.getAuthorID(au);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordID method, of class Daten.
     */
    @Test
    public void testGetKeywordID_int() {
        System.out.println("getKeywordID");
        int nr = 0;
        Daten instance = null;
        String expResult = "";
        String result = instance.getKeywordID(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordID method, of class Daten.
     */
    @Test
    public void testGetKeywordID_String() {
        System.out.println("getKeywordID");
        String kw = "";
        Daten instance = null;
        String expResult = "";
        String result = instance.getKeywordID(kw);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findZettelFromID method, of class Daten.
     */
    @Test
    public void testFindZettelFromID() {
        System.out.println("findZettelFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.findZettelFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findAuthorFromID method, of class Daten.
     */
    @Test
    public void testFindAuthorFromID() {
        System.out.println("findAuthorFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.findAuthorFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findKeywordFromID method, of class Daten.
     */
    @Test
    public void testFindKeywordFromID() {
        System.out.println("findKeywordFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.findKeywordFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelNumberFromID method, of class Daten.
     */
    @Test
    public void testGetZettelNumberFromID() {
        System.out.println("getZettelNumberFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getZettelNumberFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAuthorNumberFromID method, of class Daten.
     */
    @Test
    public void testGetAuthorNumberFromID() {
        System.out.println("getAuthorNumberFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getAuthorNumberFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeywordNumberFromID method, of class Daten.
     */
    @Test
    public void testGetKeywordNumberFromID() {
        System.out.println("getKeywordNumberFromID");
        String id = "";
        Daten instance = null;
        int expResult = 0;
        int result = instance.getKeywordNumberFromID(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zettelExists method, of class Daten.
     */
    @Test
    public void testZettelExists_String() {
        System.out.println("zettelExists");
        String id = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.zettelExists(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of zettelExists method, of class Daten.
     */
    @Test
    public void testZettelExists_int() {
        System.out.println("zettelExists");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.zettelExists(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of keywordExists method, of class Daten.
     */
    @Test
    public void testKeywordExists() {
        System.out.println("keywordExists");
        String id = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.keywordExists(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of authorExists method, of class Daten.
     */
    @Test
    public void testAuthorExists() {
        System.out.println("authorExists");
        String id = "";
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.authorExists(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasAuthorID method, of class Daten.
     */
    @Test
    public void testHasAuthorID() {
        System.out.println("hasAuthorID");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasAuthorID(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasKeywordID method, of class Daten.
     */
    @Test
    public void testHasKeywordID() {
        System.out.println("hasKeywordID");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasKeywordID(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSaveOk method, of class Daten.
     */
    @Test
    public void testSetSaveOk() {
        System.out.println("setSaveOk");
        boolean val = false;
        Daten instance = null;
        instance.setSaveOk(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSaveOk method, of class Daten.
     */
    @Test
    public void testIsSaveOk() {
        System.out.println("isSaveOk");
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isSaveOk();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZettelForms method, of class Daten.
     */
    @Test
    public void testGetZettelForms() {
        System.out.println("getZettelForms");
        int nr = 0;
        Daten instance = null;
        ArrayList<String> expResult = null;
        ArrayList<String> result = instance.getZettelForms(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isTopLevelLuhmann method, of class Daten.
     */
    @Test
    public void testIsTopLevelLuhmann() {
        System.out.println("isTopLevelLuhmann");
        int nr = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.isTopLevelLuhmann(nr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findParentlLuhmann method, of class Daten.
     */
    @Test
    public void testFindParentlLuhmann() {
        System.out.println("findParentlLuhmann");
        int nr = 0;
        boolean firstParent = false;
        Daten instance = null;
        int expResult = 0;
        int result = instance.findParentlLuhmann(nr, firstParent);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllLuhmannNumbers method, of class Daten.
     */
    @Test
    public void testGetAllLuhmannNumbers_int() {
        System.out.println("getAllLuhmannNumbers");
        int zettelpos = 0;
        Daten instance = null;
        int[] expResult = null;
        int[] result = instance.getAllLuhmannNumbers(zettelpos);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllLuhmannNumbers method, of class Daten.
     */
    @Test
    public void testGetAllLuhmannNumbers_0args() {
        System.out.println("getAllLuhmannNumbers");
        Daten instance = null;
        List<Integer> expResult = null;
        List<Integer> result = instance.getAllLuhmannNumbers();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllManualLinks method, of class Daten.
     */
    @Test
    public void testGetAllManualLinks() {
        System.out.println("getAllManualLinks");
        Daten instance = null;
        List<Integer> expResult = null;
        List<Integer> result = instance.getAllManualLinks();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasLuhmannNumbers method, of class Daten.
     */
    @Test
    public void testHasLuhmannNumbers() {
        System.out.println("hasLuhmannNumbers");
        int zettelpos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasLuhmannNumbers(zettelpos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasManLinks method, of class Daten.
     */
    @Test
    public void testHasManLinks() {
        System.out.println("hasManLinks");
        int zettelpos = 0;
        Daten instance = null;
        boolean expResult = false;
        boolean result = instance.hasManLinks(zettelpos);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
