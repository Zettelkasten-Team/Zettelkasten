package de.danielluedecke.zettelkasten.database;

import de.danielluedecke.zettelkasten.util.Constants;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;

public class DatenTest {
    /*
     startup:138, ZettelkastenApp
     <init>:690, ZettelkastenView
     loadDocument:8902, ZettelkastenView
     updateAfterOpen:10561, ZettelkastenView
     updateDisplay:2914, ZettelkastenView
     updateDisplayParts:3349, ZettelkastenView
     displayZettelContent:3392, ZettelkastenView
     getEntryAsHtml:43287, Daten
     getEntryAsHTML: 584, HtmlUbbUtil
     getEntryHeadline:317, HtmlUbbUtil
     getManualLinksAsString:3971, Daten
    */

	/**
	 * Common test document. See setUp().
	 */
	public Document document;

    @BeforeEach
    public void setUp() throws JDOMException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<zettelkasten firstzettel=\"1\" lastzettel=\"2\">"
                + "<zettel>"
                + "<title>Verschlagwortung und alphanumerische anstatt thematische Ordnung</title>"
                + "<content />"
                + "<author>1</author>"
                + "<keywords>1,2,1216,1983</keywords>"
                + "<manlinks />"
                + "<links />"
                + "<misc>feste Stellordnung</misc>"
                + "<luhmann>4,10,61,161,1771,3622</luhmann>"
                + "</zettel>"
                + "<zettel>"
                + "<title>Zettel entry that is the first parent of the Zettel entry #1</title>"
                + "<content />"
                + "<author />"
                + "<keywords />"
                + "<manlinks />"
                + "<links />"
                + "<misc />"
                + "<luhmann>1</luhmann>"
                + "</zettel>"
                + "</zettelkasten>";
  	  SAXBuilder builder = new SAXBuilder();
  	  document = builder.build(new ByteArrayInputStream(xmlString.getBytes()));
    }

    @Test
    void getEntryAsHtml() {
        // TEST retrieve the title, content and author of an entry and
        //  "convert" the data into a certain html layout then
        //  appear in the main window's textfield (jEditorPane).
    }

    @Test
    void deleteManualLinks() {
        // TEST remove one or more manual links from the current entry
        //FIXME There are two methods with the same name deleteManualLinks
        // The second method has a second parameter zettelPos, which stores
        // the index number of the currently displayed entry

        // There is one comment stating "if it exists, remove it."
        // Bug #330 showed up with a non-existing Zettel manlink.

        //TODO concept backlinks: getManualLinksAsString(mlparamentry)

    }

    @Test
    void getManualLinks() {
        // TEST return the manual links for an entry as integer-array.

    }

    @Test
    void getManualLinksAsString() {
    }

    @Test
    void getManualLinksAsSingleString() {
        Document doc = new org.jdom2.Document();
        int pos = 0;
        try {
            Daten.getManualLinksAsString(0);
        } catch (NullPointerException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }

    }

    @Test
    void retrieveElement() {
        final Element element;
        Document doc = new org.jdom2.Document();
        java.lang.System.out.println(doc);
        // [Document:  No DOCTYPE declaration,  No root element]

        Daten.retrieveElement(doc, 0);
        Daten.retrieveElement(doc, 1);
        Daten.retrieveElement(doc, -1);
        //WARNING: Root element not set
        //         Root should be [Element: <zettelkasten/>]

        // See getManualLinksAsString
    }

    @Test
    void testGoToFirstParentEntryParent() {
        // daten by default has current entry == 1. 
        Daten daten = new Daten(document);
        
		// the first (and only) parent of entry 1 is entry 2.
        daten.goToFirstParentEntry();
        assertEquals(daten.getCurrentZettelPos(), 2);
        
        // Entry 2 doesn't have a parent. Keep at the same Zettel number.
        daten.goToFirstParentEntry();
        assertEquals(daten.getCurrentZettelPos(), 2);
    }

}