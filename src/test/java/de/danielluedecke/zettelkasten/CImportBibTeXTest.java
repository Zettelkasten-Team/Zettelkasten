package de.danielluedecke.zettelkasten;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CImportBibTeXTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addSelectedAuthors() {

    }

    @Test
    void shouldCreateZettelFromBibTeXAbstract() {
        //TODO filter for the non-standard BibTeX field "abstract"
        // - Stream: CreateZettelFromBibTeXAbstract sClicked
        // (an event is generated and fed into a stream called sClicked)
        // - This event propagates to a map operation that transforms the Unit value into …
        // (this map operation produces a new stream that we have called sClearIt.
        // - The event in sClearIt propagates to the text field and changes its text contents to the contained value
        // See https://livebook.manning.com/book/functional-reactive-programming/chapter-2/23
        // 2.1. The Stream type: a stream of events

    }

    @Test
    void shouldImportKeywordsOfBibTeXEntry() {
    }
}