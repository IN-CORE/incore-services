//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.datacollection;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import Metadata.Specifications.DDI.LifeCycle.reusable.LabelType;
import Metadata.Specifications.DDI.LifeCycle.reusable.MaintainableType;
import Metadata.Specifications.DDI.LifeCycle.reusable.NameType;
import Metadata.Specifications.DDI.LifeCycle.reusable.ReferenceType;
import Metadata.Specifications.DDI.LifeCycle.reusable.SchemeReferenceType;
import Metadata.Specifications.DDI.LifeCycle.reusable.StructuredStringType;


/**
 * A set of interviewer instructions to be displayed within the instrument, such as definitions, and explanations of terminology and questions. Content may also be used to provide the contents of an instruction manual for questions or instruments. In addition to the standard name, label, and description, allows for the inclusion of another InterviewerInstructionScheme by reference an a set of in-line instructions.
 * <p>
 * <p>Java class for InterviewerInstructionSchemeType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="InterviewerInstructionSchemeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}MaintainableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:datacollection:3_2}InterviewerInstructionSchemeName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Label" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Description" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}InterviewerInstructionSchemeReference" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{ddi:datacollection:3_2}Instruction"/&gt;
 *           &lt;element ref="{ddi:datacollection:3_2}InstructionReference"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{ddi:datacollection:3_2}InstructionGroup"/&gt;
 *           &lt;element ref="{ddi:datacollection:3_2}InstructionGroupReference"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InterviewerInstructionSchemeType", propOrder = {
        "interviewerInstructionSchemeName",
        "label",
        "description",
        "interviewerInstructionSchemeReference",
        "instructionOrInstructionReference",
        "instructionGroupOrInstructionGroupReference"
})
public class InterviewerInstructionSchemeType
        extends MaintainableType {

    @XmlElement(name = "InterviewerInstructionSchemeName")
    protected List<NameType> interviewerInstructionSchemeName;
    @XmlElement(name = "Label", namespace = "ddi:reusable:3_2")
    protected List<LabelType> label;
    @XmlElement(name = "Description", namespace = "ddi:reusable:3_2")
    protected StructuredStringType description;
    @XmlElement(name = "InterviewerInstructionSchemeReference", namespace = "ddi:reusable:3_2")
    protected List<SchemeReferenceType> interviewerInstructionSchemeReference;
    @XmlElements({
            @XmlElement(name = "Instruction", type = InstructionType.class),
            @XmlElement(name = "InstructionReference", type = ReferenceType.class)
    })
    protected List<Object> instructionOrInstructionReference;
    @XmlElements({
            @XmlElement(name = "InstructionGroup", type = InstructionGroupType.class),
            @XmlElement(name = "InstructionGroupReference", type = ReferenceType.class)
    })
    protected List<Object> instructionGroupOrInstructionGroupReference;

    /**
     * A name for the InterviewerInstructionScheme. May be expressed in multiple languages. Repeat the element to express names with different content, for example different names for different systems.Gets the value of the interviewerInstructionSchemeName property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interviewerInstructionSchemeName property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterviewerInstructionSchemeName().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameType }
     */
    public List<NameType> getInterviewerInstructionSchemeName() {
        if (interviewerInstructionSchemeName == null) {
            interviewerInstructionSchemeName = new ArrayList<NameType>();
        }
        return this.interviewerInstructionSchemeName;
    }

    /**
     * A display label for the InterviewerInstructionScheme. May be expressed in multiple languages. Repeat for labels with different content, for example, labels with differing length limitations.Gets the value of the label property.
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
     * A description of the content and purpose of the InterviewerInstructionScheme. May be expressed in multiple languages and supports the use of structured content.
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
     * Allows for the inclusion of an existing InterviewerInstructionScheme by reference.Gets the value of the interviewerInstructionSchemeReference property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interviewerInstructionSchemeReference property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterviewerInstructionSchemeReference().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemeReferenceType }
     */
    public List<SchemeReferenceType> getInterviewerInstructionSchemeReference() {
        if (interviewerInstructionSchemeReference == null) {
            interviewerInstructionSchemeReference = new ArrayList<SchemeReferenceType>();
        }
        return this.interviewerInstructionSchemeReference;
    }

    /**
     * Gets the value of the instructionOrInstructionReference property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instructionOrInstructionReference property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstructionOrInstructionReference().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstructionType }
     * {@link ReferenceType }
     */
    public List<Object> getInstructionOrInstructionReference() {
        if (instructionOrInstructionReference == null) {
            instructionOrInstructionReference = new ArrayList<Object>();
        }
        return this.instructionOrInstructionReference;
    }

    /**
     * Gets the value of the instructionGroupOrInstructionGroupReference property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instructionGroupOrInstructionGroupReference property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstructionGroupOrInstructionGroupReference().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InstructionGroupType }
     * {@link ReferenceType }
     */
    public List<Object> getInstructionGroupOrInstructionGroupReference() {
        if (instructionGroupOrInstructionGroupReference == null) {
            instructionGroupOrInstructionGroupReference = new ArrayList<Object>();
        }
        return this.instructionGroupOrInstructionGroupReference;
    }

}