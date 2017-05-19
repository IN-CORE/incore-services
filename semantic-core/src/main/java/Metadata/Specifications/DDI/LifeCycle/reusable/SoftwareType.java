//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.reusable;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Describes a specific software package, which may be commercially available or custom-made.
 * <p>
 * <p>Java class for SoftwareType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="SoftwareType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:reusable:3_2}SoftwareName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}SoftwarePackage" minOccurs="0"/&gt;
 *         &lt;element name="SoftwareVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Description" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Date" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Function" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SoftwareType", propOrder = {
        "softwareName",
        "softwarePackage",
        "softwareVersion",
        "description",
        "date",
        "function"
})
public class SoftwareType {

    @XmlElement(name = "SoftwareName")
    protected List<NameType> softwareName;
    @XmlElement(name = "SoftwarePackage")
    protected CodeValueType softwarePackage;
    @XmlElement(name = "SoftwareVersion")
    protected String softwareVersion;
    @XmlElement(name = "Description")
    protected StructuredStringType description;
    @XmlElement(name = "Date")
    protected DateType date;
    @XmlElement(name = "Function")
    protected List<CodeValueType> function;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected String lang;

    /**
     * The name of the software package, including its producer.Gets the value of the softwareName property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the softwareName property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoftwareName().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameType }
     */
    public List<NameType> getSoftwareName() {
        if (softwareName == null) {
            softwareName = new ArrayList<NameType>();
        }
        return this.softwareName;
    }

    /**
     * A coded value from a controlled vocabulary, describing the software package.
     *
     * @return possible object is
     * {@link CodeValueType }
     */
    public CodeValueType getSoftwarePackage() {
        return softwarePackage;
    }

    /**
     * Sets the value of the softwarePackage property.
     *
     * @param value allowed object is
     *              {@link CodeValueType }
     */
    public void setSoftwarePackage(CodeValueType value) {
        this.softwarePackage = value;
    }

    /**
     * Gets the value of the softwareVersion property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Sets the value of the softwareVersion property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSoftwareVersion(String value) {
        this.softwareVersion = value;
    }

    /**
     * A description of the content and purpose of the software. May be expressed in multiple languages and supports the use of structured content.
     *
     * @return possible object is
     * {@link StructuredStringType }
     */
    public StructuredStringType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link StructuredStringType }
     */
    public void setDescription(StructuredStringType value) {
        this.description = value;
    }

    /**
     * Supported date of the software package with, at minimum, a release date if known.
     *
     * @return possible object is
     * {@link DateType }
     */
    public DateType getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     *
     * @param value allowed object is
     *              {@link DateType }
     */
    public void setDate(DateType value) {
        this.date = value;
    }

    /**
     * Identifies the functions handled by this software. Repeat for multiple functions. It may be advisable to note only those functions used in the specific usage of the software.Gets the value of the function property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the function property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFunction().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeValueType }
     */
    public List<CodeValueType> getFunction() {
        if (function == null) {
            function = new ArrayList<CodeValueType>();
        }
        return this.function;
    }

    /**
     * Language (human language) of the software package.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLang(String value) {
        this.lang = value;
    }

}