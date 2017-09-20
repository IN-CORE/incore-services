//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.physicaldataproduct.proprietary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import Metadata.Specifications.DDI.LifeCycle.reusable.CodeValueType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ProprietaryInfoType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ReferenceType;


/**
 * Describes a single data item within the record, linking it to its description in a variable and providing information on its data type and any item specific proprietary information.
 * <p>
 * <p>Java class for DataItemType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="DataItemType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:reusable:3_2}VariableReference" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:physicaldataproduct_proprietary:3_2}ProprietaryDataType" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:physicaldataproduct_proprietary:3_2}ProprietaryOutputFormat" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}ProprietaryInfo" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataItemType", propOrder = {
        "variableReference",
        "proprietaryDataType",
        "proprietaryOutputFormat",
        "proprietaryInfo"
})
public class DataItemType {

    @XmlElement(name = "VariableReference", namespace = "ddi:reusable:3_2")
    protected ReferenceType variableReference;
    @XmlElement(name = "ProprietaryDataType")
    protected CodeValueType proprietaryDataType;
    @XmlElement(name = "ProprietaryOutputFormat")
    protected CodeValueType proprietaryOutputFormat;
    @XmlElement(name = "ProprietaryInfo", namespace = "ddi:reusable:3_2")
    protected ProprietaryInfoType proprietaryInfo;

    /**
     * Reference to a Variable describing the content of the data item.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getVariableReference() {
        return variableReference;
    }

    /**
     * Sets the value of the variableReference property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setVariableReference(ReferenceType value) {
        this.variableReference = value;
    }

    /**
     * Allows an override of the default data type, using the language of the proprietary software. A controlled vocabulary is recommended.
     *
     * @return possible object is
     * {@link CodeValueType }
     */
    public CodeValueType getProprietaryDataType() {
        return proprietaryDataType;
    }

    /**
     * Sets the value of the proprietaryDataType property.
     *
     * @param value allowed object is
     *              {@link CodeValueType }
     */
    public void setProprietaryDataType(CodeValueType value) {
        this.proprietaryDataType = value;
    }

    /**
     * Indicates the proprietary output format.
     *
     * @return possible object is
     * {@link CodeValueType }
     */
    public CodeValueType getProprietaryOutputFormat() {
        return proprietaryOutputFormat;
    }

    /**
     * Sets the value of the proprietaryOutputFormat property.
     *
     * @param value allowed object is
     *              {@link CodeValueType }
     */
    public void setProprietaryOutputFormat(CodeValueType value) {
        this.proprietaryOutputFormat = value;
    }

    /**
     * Contains proprietary information specific to the data item. This is expressed as a set of key (name)-value pairs.
     *
     * @return possible object is
     * {@link ProprietaryInfoType }
     */
    public ProprietaryInfoType getProprietaryInfo() {
        return proprietaryInfo;
    }

    /**
     * Sets the value of the proprietaryInfo property.
     *
     * @param value allowed object is
     *              {@link ProprietaryInfoType }
     */
    public void setProprietaryInfo(ProprietaryInfoType value) {
        this.proprietaryInfo = value;
    }

}