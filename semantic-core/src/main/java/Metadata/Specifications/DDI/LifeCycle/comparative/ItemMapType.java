//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.comparative;

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

import Metadata.Specifications.DDI.LifeCycle.reusable.IDType;
import Metadata.Specifications.DDI.LifeCycle.reusable.IdentifiableType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ReferenceType;


/**
 * Maps two items of the same type within the Source and Target Schemes identified.
 * <p>
 * <p>Java class for ItemMapType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="ItemMapType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SourceItem" type="{ddi:reusable:3_2}IDType"/&gt;
 *         &lt;element name="TargetItem" type="{ddi:reusable:3_2}IDType"/&gt;
 *         &lt;element ref="{ddi:comparative:3_2}Correspondence" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:comparative:3_2}RelatedMapReference" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemMapType", propOrder = {
        "sourceItem",
        "targetItem",
        "correspondence",
        "relatedMapReference"
})
public class ItemMapType
        extends IdentifiableType {

    @XmlElement(name = "SourceItem", required = true)
    protected IDType sourceItem;
    @XmlElement(name = "TargetItem", required = true)
    protected IDType targetItem;
    @XmlElement(name = "Correspondence")
    protected CorrespondenceType correspondence;
    @XmlElement(name = "RelatedMapReference")
    protected List<ReferenceType> relatedMapReference;
    @XmlAttribute(name = "alias")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String alias;

    /**
     * Gets the value of the sourceItem property.
     *
     * @return possible object is
     * {@link IDType }
     */
    public IDType getSourceItem() {
        return sourceItem;
    }

    /**
     * Sets the value of the sourceItem property.
     *
     * @param value allowed object is
     *              {@link IDType }
     */
    public void setSourceItem(IDType value) {
        this.sourceItem = value;
    }

    /**
     * Gets the value of the targetItem property.
     *
     * @return possible object is
     * {@link IDType }
     */
    public IDType getTargetItem() {
        return targetItem;
    }

    /**
     * Sets the value of the targetItem property.
     *
     * @param value allowed object is
     *              {@link IDType }
     */
    public void setTargetItem(IDType value) {
        this.targetItem = value;
    }

    /**
     * Describe the level of similarity and difference between the Source and the Target objects.
     *
     * @return possible object is
     * {@link CorrespondenceType }
     */
    public CorrespondenceType getCorrespondence() {
        return correspondence;
    }

    /**
     * Sets the value of the correspondence property.
     *
     * @param value allowed object is
     *              {@link CorrespondenceType }
     */
    public void setCorrespondence(CorrespondenceType value) {
        this.correspondence = value;
    }

    /**
     * Identifies related maps for example an ItemMap of two questions may point to the CodeMap defining the comparison of the two response domains.Gets the value of the relatedMapReference property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedMapReference property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedMapReference().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceType }
     */
    public List<ReferenceType> getRelatedMapReference() {
        if (relatedMapReference == null) {
            relatedMapReference = new ArrayList<ReferenceType>();
        }
        return this.relatedMapReference;
    }

    /**
     * Gets the value of the alias property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}