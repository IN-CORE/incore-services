//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.reusable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Allows for string content which may be taken from an externally maintained controlled vocabulary (code value). If the content is from a controlled vocabulary provide the code value, as well as a reference to the code list from which the value is taken. Provide as many of the identifying attributes as needed to adequately identify the controlled vocabulary. Note that DDI has published a number of controlled vocabularies applicable to several locations using the CodeValue structure. Use of shared controlled vocabularies helps support interoperability and machine actionability.
 * <p>
 * <p>Java class for Country_3Type complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="Country_3Type"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;ddi:reusable:3_2&gt;CountryCodeType"&gt;
 *       &lt;attribute name="codeListID" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" fixed="ISO3166_3ch" /&gt;
 *       &lt;attribute name="codeListName" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" fixed="ISO3166 3-letter" /&gt;
 *       &lt;attribute name="codeListAgencyName" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" fixed="DDI Alliance" /&gt;
 *       &lt;attribute name="otherValue" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="codeListURN" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" fixed="urn:ddi-cv:ISO3166_3ch" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Country_3Type")
public class Country3Type
        extends CountryCodeType {

    @XmlAttribute(name = "codeListID")
    @XmlSchemaType(name = "anySimpleType")
    protected String codeListID;
    @XmlAttribute(name = "codeListName")
    @XmlSchemaType(name = "anySimpleType")
    protected String codeListName;
    @XmlAttribute(name = "codeListAgencyName")
    @XmlSchemaType(name = "anySimpleType")
    protected String codeListAgencyName;
    @XmlAttribute(name = "otherValue")
    protected String otherValue;
    @XmlAttribute(name = "codeListURN")
    @XmlSchemaType(name = "anySimpleType")
    protected String codeListURN;

    /**
     * Gets the value of the codeListID property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeListID() {
        if (codeListID == null) {
            return "ISO3166_3ch";
        } else {
            return codeListID;
        }
    }

    /**
     * Sets the value of the codeListID property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeListID(String value) {
        this.codeListID = value;
    }

    /**
     * Gets the value of the codeListName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeListName() {
        if (codeListName == null) {
            return "ISO3166 3-letter";
        } else {
            return codeListName;
        }
    }

    /**
     * Sets the value of the codeListName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeListName(String value) {
        this.codeListName = value;
    }

    /**
     * Gets the value of the codeListAgencyName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeListAgencyName() {
        if (codeListAgencyName == null) {
            return "DDI Alliance";
        } else {
            return codeListAgencyName;
        }
    }

    /**
     * Sets the value of the codeListAgencyName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeListAgencyName(String value) {
        this.codeListAgencyName = value;
    }

    /**
     * Gets the value of the otherValue property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOtherValue() {
        return otherValue;
    }

    /**
     * Sets the value of the otherValue property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOtherValue(String value) {
        this.otherValue = value;
    }

    /**
     * Gets the value of the codeListURN property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeListURN() {
        if (codeListURN == null) {
            return "urn:ddi-cv:ISO3166_3ch";
        } else {
            return codeListURN;
        }
    }

    /**
     * Sets the value of the codeListURN property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeListURN(String value) {
        this.codeListURN = value;
    }

}