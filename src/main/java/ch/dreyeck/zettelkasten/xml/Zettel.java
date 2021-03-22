//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.10.06 um 10:17:37 AM CEST 
//


package ch.dreyeck.zettelkasten.xml;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.math.BigInteger;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}title"/>
 *         &lt;element ref="{}content"/>
 *         &lt;element ref="{}author"/>
 *         &lt;element ref="{}keywords"/>
 *         &lt;element ref="{}manlinks"/>
 *         &lt;element ref="{}links"/>
 *         &lt;element ref="{}misc"/>
 *         &lt;element ref="{}luhmann"/>
 *       &lt;/sequence>
 *       &lt;attribute name="fromBibTex" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="rating" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ratingcount" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ts_created" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="ts_edited" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="zknid" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "title",
        "content",
        "author",
        "keywords",
        "manlinks",
        "links",
        "misc",
        "luhmann"
})
@XmlRootElement(name = "zettel")
public class Zettel {

    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String content;
    @XmlElement(required = true)
    protected String author;
    @XmlElement(required = true)
    protected String keywords;
    @XmlElement(required = true)
    protected String manlinks;
    @XmlElement(required = true)
    protected Links links;
    @XmlElement(required = true)
    protected String misc;
    @XmlElement(required = true)
    protected String luhmann;
    @XmlAttribute(name = "fromBibTex")
    protected BigInteger fromBibTex;
    @XmlAttribute(name = "rating")
    @XmlSchemaType(name = "anySimpleType")
    protected String rating;
    @XmlAttribute(name = "ratingcount")
    @XmlSchemaType(name = "anySimpleType")
    protected String ratingcount;
    @XmlAttribute(name = "ts_created", required = true)
    protected BigInteger tsCreated;
    @XmlAttribute(name = "ts_edited", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String tsEdited;
    @XmlAttribute(name = "zknid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String zknid;

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der content-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getContent() {
        return content;
    }

    /**
     * Legt den Wert der content-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Ruft den Wert der author-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Legt den Wert der author-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Ruft den Wert der keywords-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Legt den Wert der keywords-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeywords(String value) {
        this.keywords = value;
    }

    /**
     * Ruft den Wert der manlinks-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManlinks() {
        return manlinks;
    }

    /**
     * Legt den Wert der manlinks-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManlinks(String value) {
        this.manlinks = value;
    }

    /**
     * Ruft den Wert der links-Eigenschaft ab.
     *
     * @return possible object is
     * {@link Links }
     */
    public Links getLinks() {
        return links;
    }

    /**
     * Legt den Wert der links-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link Links }
     */
    public void setLinks(Links value) {
        this.links = value;
    }

    /**
     * Ruft den Wert der misc-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMisc() {
        return misc;
    }

    /**
     * Legt den Wert der misc-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMisc(String value) {
        this.misc = value;
    }

    /**
     * Ruft den Wert der luhmann-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLuhmann() {
        return luhmann;
    }

    /**
     * Legt den Wert der luhmann-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLuhmann(String value) {
        this.luhmann = value;
    }

    /**
     * Ruft den Wert der fromBibTex-Eigenschaft ab.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getFromBibTex() {
        return fromBibTex;
    }

    /**
     * Legt den Wert der fromBibTex-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setFromBibTex(BigInteger value) {
        this.fromBibTex = value;
    }

    /**
     * Ruft den Wert der rating-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRating() {
        return rating;
    }

    /**
     * Legt den Wert der rating-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRating(String value) {
        this.rating = value;
    }

    /**
     * Ruft den Wert der ratingcount-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRatingcount() {
        return ratingcount;
    }

    /**
     * Legt den Wert der ratingcount-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRatingcount(String value) {
        this.ratingcount = value;
    }

    /**
     * Ruft den Wert der tsCreated-Eigenschaft ab.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getTsCreated() {
        return tsCreated;
    }

    /**
     * Legt den Wert der tsCreated-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setTsCreated(BigInteger value) {
        this.tsCreated = value;
    }

    /**
     * Ruft den Wert der tsEdited-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTsEdited() {
        return tsEdited;
    }

    /**
     * Legt den Wert der tsEdited-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTsEdited(String value) {
        this.tsEdited = value;
    }

    /**
     * Ruft den Wert der zknid-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getZknid() {
        return zknid;
    }

    /**
     * Legt den Wert der zknid-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setZknid(String value) {
        this.zknid = value;
    }

}
