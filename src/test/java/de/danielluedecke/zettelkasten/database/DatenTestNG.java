package de.danielluedecke.zettelkasten.database;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class DatenTestNG {

    public Document document;

    @BeforeMethod
    public void setUp() throws JDOMException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<zettelkasten firstzettel=\"1\" lastzettel=\"2\">" + "<zettel>"
                + "<title>Verschlagwortung und alphanumerische anstatt thematische Ordnung</title>" + "<content />"
                + "<author>1</author>" + "<keywords>1,2,1216,1983</keywords>" + "<manlinks />" + "<links />"
                + "<misc>feste Stellordnung</misc>" + "<luhmann>4,10,61,161,1771,3622</luhmann>" + "</zettel>"
                + "<zettel>" + "<title>Zettel entry that is the first parent of the Zettel entry #1</title>"
                + "<content />" + "<author />" + "<keywords />" + "<manlinks />" + "<links />" + "<misc />"
                + "<luhmann>1</luhmann>" + "</zettel>" + "<zettel>"
                + "<title>Zettel entry that is not connected to any of the others</title>" + "<content />"
                + "<author />" + "<keywords />" + "<manlinks />" + "<links />" + "<misc />" + "<luhmann />"
                + "</zettel>" + "</zettelkasten>";
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(new ByteArrayInputStream(xmlString.getBytes()));
    }

    @Test
    public void testFindParentLuhmannFirstParentTrue() {
        Daten daten = new Daten(document);

        // Test for first parent
        int result = daten.findParentLuhmann(1, true);
        assertEquals(result, 2, "First parent of entry 1 should be entry 2");
    }
}
