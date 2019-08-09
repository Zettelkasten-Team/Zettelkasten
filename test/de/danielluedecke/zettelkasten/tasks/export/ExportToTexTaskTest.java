/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.danielluedecke.zettelkasten.tasks.export;

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
public class ExportToTexTaskTest {

    public ExportToTexTaskTest() {
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
     * Test of doInBackground method, of class ExportToTexTask.
     */
    @Test
    public void testDoInBackground() {
        System.out.println("doInBackground");
        ExportToTexTask instance = null;
        Object expResult = null;
        Object result = instance.doInBackground();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testBugMarkdownZitatWirdNichtKorrektNachLatexExportiert()
            throws Exception {
        /*
		 * It seems that the current implementation of
		 * ExportToTexTask.convertedTex() does *not* convert any quotation tags
		 * at all, neither in Markdown nor in UBB syntax:
         */
    }

    @Test
    public void testMarkdownQuotationBecomesLaTeXRangle() throws Exception {
        /*
		 * It seems that convertSpecialChars() does not respect Markdown
		 * quotations: ">" is escaped into "\rangle"
         */
    }

    /**
     * Test of succeeded method, of class ExportToTexTask.
     */
    @Test
    public void testSucceeded() {
        System.out.println("succeeded");
        Object result_2 = null;
        ExportToTexTask instance = null;
        instance.succeeded(result_2);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of finished method, of class ExportToTexTask.
     */
    @Test
    public void testFinished() {
        System.out.println("finished");
        ExportToTexTask instance = null;
        instance.finished();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
