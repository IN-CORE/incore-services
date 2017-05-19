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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A reference to a General or Generation Instruction that was used by the parent object, e.g. an instruction used to derive a Variable or used by a ProcessingEvent. The basic Reference structure is extended to allow for the use of Binding to link specific source parameters to the InParameter of the instruction at the point of use. If there is a conflict between a Binding in the iinstruction of a specific source to an InParameter and the Binding information provided in the ProcessingInstructionReference, the Binding information in the reference overrides that found in the instruction. TypeOfObject should be set to ProcessingInstruction.
 * <p>
 * <p>Java class for ProcessingInstructionReferenceType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="ProcessingInstructionReferenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}ReferenceType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Binding" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessingInstructionReferenceType", propOrder = {
        "binding"
})
public class ProcessingInstructionReferenceType
        extends ReferenceType {

    @XmlElement(name = "Binding")
    protected List<BindingType> binding;

    /**
     * A structure used to link the content of a parameter declared as the source to a parameter declared as the target. For example, linking the output of a question to the input of a generation instruction. Question A has an OutParameter X. Generation Instruction has an InParameter Y used in the recode instruction. Binding defines the content of InParameter Y to be whatever is provided by OutParameter X for use in the calculation of the recode.Gets the value of the binding property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the binding property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBinding().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BindingType }
     */
    public List<BindingType> getBinding() {
        if (binding == null) {
            binding = new ArrayList<BindingType>();
        }
        return this.binding;
    }

}