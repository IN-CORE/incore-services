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

import Metadata.Specifications.DDI.LifeCycle.reusable.IdentifiableType;
import Metadata.Specifications.DDI.LifeCycle.reusable.LabelType;
import Metadata.Specifications.DDI.LifeCycle.reusable.NameType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ReferenceType;
import Metadata.Specifications.DDI.LifeCycle.reusable.StructuredStringType;


/**
 * Describes the relationship between records of different types or of the same type within a longitudinal study. Identifies the key and linking value relationships. All relationships are pairwise. Multiple pairwise relationships maybe needed to clarify all record relationships within a logical product or data set. In addition to the standard name, label, and description, the structure identifies the pair of logical records for which the relationship is defined as SourceLogicalRecord and TargetLogicalRecord, describes the link between these two records and indicates the relationship of the of the source record to the target record.
 * <p>
 * <p>Java class for RecordRelationshipType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="RecordRelationshipType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:logicalproduct:3_2}RecordRelationshipName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Label" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Description" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:logicalproduct:3_2}SourceLogicalRecordReference"/&gt;
 *         &lt;element ref="{ddi:logicalproduct:3_2}TargetLogicalRecordReference"/&gt;
 *         &lt;element ref="{ddi:logicalproduct:3_2}SourceTargetLink" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="relationToTarget" type="{ddi:logicalproduct:3_2}RelationCodeType" default="Unknown" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecordRelationshipType", propOrder = {
        "recordRelationshipName",
        "label",
        "description",
        "sourceLogicalRecordReference",
        "targetLogicalRecordReference",
        "sourceTargetLink"
})
public class RecordRelationshipType
        extends IdentifiableType {

    @XmlElement(name = "RecordRelationshipName")
    protected List<NameType> recordRelationshipName;
    @XmlElement(name = "Label", namespace = "ddi:reusable:3_2")
    protected List<LabelType> label;
    @XmlElement(name = "Description", namespace = "ddi:reusable:3_2")
    protected StructuredStringType description;
    @XmlElement(name = "SourceLogicalRecordReference", required = true)
    protected ReferenceType sourceLogicalRecordReference;
    @XmlElement(name = "TargetLogicalRecordReference", required = true)
    protected ReferenceType targetLogicalRecordReference;
    @XmlElement(name = "SourceTargetLink")
    protected List<SourceTargetLinkType> sourceTargetLink;
    @XmlAttribute(name = "relationToTarget")
    protected RelationCodeType relationToTarget;

    /**
     * A name for the RecordRelationship. May be expressed in multiple languages. Repeat the element to express names with different content, for example different names for different systems.Gets the value of the recordRelationshipName property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recordRelationshipName property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecordRelationshipName().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameType }
     */
    public List<NameType> getRecordRelationshipName() {
        if (recordRelationshipName == null) {
            recordRelationshipName = new ArrayList<NameType>();
        }
        return this.recordRelationshipName;
    }

    /**
     * A display label for the RecordRelationship. May be expressed in multiple languages. Repeat for labels with different content, for example, labels with differing length limitations.Gets the value of the label property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the label property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLabel().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LabelType }
     */
    public List<LabelType> getLabel() {
        if (label == null) {
            label = new ArrayList<LabelType>();
        }
        return this.label;
    }

    /**
     * A description of the content and purpose of the RecordRelationship. May be expressed in multiple languages and supports the use of structured content.
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
     * A reference to the Logical Record acting as the Source Record. All relationship information is from the source to the target. If the relationship is not unidirectional (i.e., sibling) simply assign one record as the source and the other as the target.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getSourceLogicalRecordReference() {
        return sourceLogicalRecordReference;
    }

    /**
     * Sets the value of the sourceLogicalRecordReference property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setSourceLogicalRecordReference(ReferenceType value) {
        this.sourceLogicalRecordReference = value;
    }

    /**
     * A reference to the Logical Record acting as the Target Record.
     *
     * @return possible object is
     * {@link ReferenceType }
     */
    public ReferenceType getTargetLogicalRecordReference() {
        return targetLogicalRecordReference;
    }

    /**
     * Sets the value of the targetLogicalRecordReference property.
     *
     * @param value allowed object is
     *              {@link ReferenceType }
     */
    public void setTargetLogicalRecordReference(ReferenceType value) {
        this.targetLogicalRecordReference = value;
    }

    /**
     * Each SourceTargetLink provides a pair of variables which act as all or part of a link between the source and the target records. Repeat if more than one set of variables is required to make the link.Gets the value of the sourceTargetLink property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceTargetLink property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceTargetLink().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SourceTargetLinkType }
     */
    public List<SourceTargetLinkType> getSourceTargetLink() {
        if (sourceTargetLink == null) {
            sourceTargetLink = new ArrayList<SourceTargetLinkType>();
        }
        return this.sourceTargetLink;
    }

    /**
     * Gets the value of the relationToTarget property.
     *
     * @return possible object is
     * {@link RelationCodeType }
     */
    public RelationCodeType getRelationToTarget() {
        if (relationToTarget == null) {
            return RelationCodeType.UNKNOWN;
        } else {
            return relationToTarget;
        }
    }

    /**
     * Sets the value of the relationToTarget property.
     *
     * @param value allowed object is
     *              {@link RelationCodeType }
     */
    public void setRelationToTarget(RelationCodeType value) {
        this.relationToTarget = value;
    }

}