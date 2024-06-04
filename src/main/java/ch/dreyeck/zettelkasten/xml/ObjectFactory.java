//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2024.03.18 um 02:14:17 AM CET 
//


package ch.dreyeck.zettelkasten.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.dreyeck.zettelkasten.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Keywords_QNAME = new QName("", "keywords");
    private final static QName _Author_QNAME = new QName("", "author");
    private final static QName _Luhmann_QNAME = new QName("", "luhmann");
    private final static QName _Link_QNAME = new QName("", "link");
    private final static QName _Manlinks_QNAME = new QName("", "manlinks");
    private final static QName _Title_QNAME = new QName("", "title");
    private final static QName _Content_QNAME = new QName("", "content");
    private final static QName _Misc_QNAME = new QName("", "misc");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.dreyeck.zettelkasten.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Zettel }
     * 
     */
    public Zettel createZettel() {
        return new Zettel();
    }

    /**
     * Create an instance of {@link Links }
     * 
     */
    public Links createLinks() {
        return new Links();
    }

    /**
     * Create an instance of {@link Zettelkasten }
     * 
     */
    public Zettelkasten createZettelkasten() {
        return new Zettelkasten();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "keywords")
    public JAXBElement<String> createKeywords(String value) {
        return new JAXBElement<String>(_Keywords_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "author")
    public JAXBElement<String> createAuthor(String value) {
        return new JAXBElement<String>(_Author_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "luhmann")
    public JAXBElement<String> createLuhmann(String value) {
        return new JAXBElement<String>(_Luhmann_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "link")
    public JAXBElement<String> createLink(String value) {
        return new JAXBElement<String>(_Link_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "manlinks")
    public JAXBElement<String> createManlinks(String value) {
        return new JAXBElement<String>(_Manlinks_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "content")
    public JAXBElement<String> createContent(String value) {
        return new JAXBElement<String>(_Content_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "misc")
    public JAXBElement<String> createMisc(String value) {
        return new JAXBElement<String>(_Misc_QNAME, String.class, null, value);
    }

}
