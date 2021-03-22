package ch.dreyeck.zettelkasten.xml;

import org.junit.jupiter.api.Test;
import org.xmlunit.matchers.CompareMatcher;

import static org.hamcrest.MatcherAssert.assertThat;


class ZettelTest {

    @Test
    public void given2XMLS_whenIdentical_thenCorrect() {
        String controlXml = "<struct><int>3</int><boolean>false</boolean></struct>";
        String testXml = "<struct><int>3</int><boolean>false</boolean></struct>";
        assertThat(testXml, CompareMatcher.isIdenticalTo(controlXml));
    }

}