package de.danielluedecke.zettelkasten.database;

import ch.dreyeck.zettelkasten.xml.Zettel;
import ch.dreyeck.zettelkasten.xml.Zettelkasten;
import junit.framework.TestCase;
import org.jdom2.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

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


    @BeforeEach
    public void setUp() {
        String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<zettelkasten firstzettel=\"1\" lastzettel=\"1\">"
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
                + "</zettelkasten>";
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
        final String manlinks = new Zettel().getManlinks();

    }

    @Test
    void getManualLinksAsSingleString() {
        final String manlinks = new Zettel().getManlinks();

    }

    @Test
    void retrieveElement() {
        Document doc = new org.jdom2.Document();
        java.lang.System.out.println(doc);
        // [Document:  No DOCTYPE declaration,  No root element]

        Daten.retrieveElement(doc,0);
        Daten.retrieveElement(doc,1);
        //WARNING: Root element not set
    }

}