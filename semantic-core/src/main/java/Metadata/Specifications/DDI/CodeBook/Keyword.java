//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.05 at 03:37:15 PM CST 
//


package Metadata.Specifications.DDI.CodeBook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for keywordType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="keywordType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:codebook:2_5}simpleTextType"&gt;
 *       &lt;attribute name="vocab" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="vocabURI" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "keywordType")
@XmlRootElement(name = "keyword")
public class Keyword
        extends SimpleTextType {

    @XmlAttribute(name = "vocab")
    protected String vocab;
    @XmlAttribute(name = "vocabURI")
    protected String vocabURI;

    /**
     * Gets the value of the vocab property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVocab() {
        return vocab;
    }

    /**
     * Sets the value of the vocab property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVocab(String value) {
        this.vocab = value;
    }

    /**
     * Gets the value of the vocabURI property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVocabURI() {
        return vocabURI;
    }

    /**
     * Sets the value of the vocabURI property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVocabURI(String value) {
        this.vocabURI = value;
    }

}