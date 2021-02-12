package de.danielluedecke.zettelkasten.database;

import org.jdom2.Document;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.parser.Element;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatenTest {

    @Test
    void getEntryAsHtml() {
        // TEST retrieve the title, content and author of an entry and
        //  "convert" the data into a certain html layout then
        //  appear in the main window's textfield (jEditorPane).
    }

    @Test
    Element retrieveElement() {
        //TEST retrieve an element of
        // a xml document

        //Document org.jdom2 Java JDOM2
        //TODO https://www.tutorials.de/threads/xml-treeviewer-jaxb-oder-jdom.299788/
        // XML -> Treeviewer: JAXB oder JDOM?

        //TODO https://www.quora.com/How-do-you-use-TDD-Test-driven-Development-to-read-and-write-from-XML-files
        // How do you use TDD (Test driven Development) to read and write from XML files?

        return null;
    }
    @Test
    void getManualLinksAsString() {
        // TEST retrieveElement first
        Element zettel = retrieveElement();

    }
}