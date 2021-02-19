//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2019.10.06 um 10:17:37 AM CEST 
//


package ch.dreyeck.zettelkasten.xml;

import jakarta.xml.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{}zettel" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="firstzettel" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="lastzettel" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "zettel"
})
@XmlRootElement(name = "zettelkasten")
public class Zettelkasten {

    @XmlElement(required = true)
    protected List<Zettel> zettel;
    @XmlAttribute(name = "firstzettel", required = true)
    protected BigInteger firstzettel;
    @XmlAttribute(name = "lastzettel", required = true)
    protected BigInteger lastzettel;

    /**
     * Gets the value of the zettel property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the zettel property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZettel().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Zettel }
     */
    public List<Zettel> getZettel() {
        if (zettel == null) {
            zettel = new ArrayList<Zettel>();
        }
        return this.zettel;
    }

    /**
     * Ruft den Wert der firstzettel-Eigenschaft ab.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getFirstzettel() {
        return firstzettel;
    }

    /**
     * Legt den Wert der firstzettel-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setFirstzettel(BigInteger value) {
        this.firstzettel = value;
    }

    /**
     * Ruft den Wert der lastzettel-Eigenschaft ab.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getLastzettel() {
        return lastzettel;
    }

    /**
     * Legt den Wert der lastzettel-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setLastzettel(BigInteger value) {
        this.lastzettel = value;
    }

}
