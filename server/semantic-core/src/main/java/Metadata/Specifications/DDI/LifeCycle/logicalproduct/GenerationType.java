//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.logicalproduct;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import Metadata.Specifications.DDI.LifeCycle.reusable.CommandCodeType;
import Metadata.Specifications.DDI.LifeCycle.reusable.OtherMaterialType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ReferenceType;
import Metadata.Specifications.DDI.LifeCycle.reusable.StructuredStringType;


/**
 * Description of the process used to generate the category content. Includes a reference to component parts, a description of the generation process, a structured command, and other materials that are needed in the generation process. The item may be designated as a derivation process (default value) and be qualified in some way by a qualification attribute.
 * <p>
 * <p>Java class for GenerationType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="GenerationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:logicalproduct:3_2}ComponentReference" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Description" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}CommandCode" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}OtherMaterial" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="isDerived" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="qualification" type="{http://www.w3.org/2001/XMLSchema}string" default="optional" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenerationType", propOrder = {
        "componentReference",
        "description",
        "commandCode",
        "otherMaterial"
})
public class GenerationType {

    @XmlElement(name = "ComponentReference")
    protected List<ReferenceType> componentReference;
    @XmlElement(name = "Description", namespace = "ddi:reusable:3_2")
    protected StructuredStringType description;
    @XmlElement(name = "CommandCode", namespace = "ddi:reusable:3_2")
    protected List<CommandCodeType> commandCode;
    @XmlElement(name = "OtherMaterial", namespace = "ddi:reusable:3_2")
    protected List<OtherMaterialType> otherMaterial;
    @XmlAttribute(name = "isDerived")
    protected Boolean isDerived;
    @XmlAttribute(name = "qualification")
    protected String qualification;

    /**
     * Reference to a category used in the generation process.Gets the value of the componentReference property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the componentReference property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponentReference().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceType }
     */
    public List<ReferenceType> getComponentReference() {
        if (componentReference == null) {
            componentReference = new ArrayList<ReferenceType>();
        }
        return this.componentReference;
    }

    /**
     * A description of the generation process. May be expressed in multiple languages and supports the use of structured content.
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
     * Structured information used by a system to generate the category.Gets the value of the commandCode property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commandCode property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommandCode().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommandCodeType }
     */
    public List<CommandCodeType> getCommandCode() {
        if (commandCode == null) {
            commandCode = new ArrayList<CommandCodeType>();
        }
        return this.commandCode;
    }

    /**
     * External documentation required for creating the generation - for example, a chart or table for defining poverty.Gets the value of the otherMaterial property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherMaterial property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherMaterial().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OtherMaterialType }
     */
    public List<OtherMaterialType> getOtherMaterial() {
        if (otherMaterial == null) {
            otherMaterial = new ArrayList<OtherMaterialType>();
        }
        return this.otherMaterial;
    }

    /**
     * Gets the value of the isDerived property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public boolean isIsDerived() {
        if (isDerived == null) {
            return true;
        } else {
            return isDerived;
        }
    }

    /**
     * Sets the value of the isDerived property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIsDerived(Boolean value) {
        this.isDerived = value;
    }

    /**
     * Gets the value of the qualification property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQualification() {
        if (qualification == null) {
            return "optional";
        } else {
            return qualification;
        }
    }

    /**
     * Sets the value of the qualification property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQualification(String value) {
        this.qualification = value;
    }

}