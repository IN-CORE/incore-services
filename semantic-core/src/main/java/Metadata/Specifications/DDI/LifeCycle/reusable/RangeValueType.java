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
import javax.xml.bind.annotation.XmlType;


/**
 * Describes a bounding value of a string.
 * <p>
 * <p>Java class for RangeValueType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="RangeValueType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;ddi:reusable:3_2&gt;ValueType"&gt;
 *       &lt;attribute name="included" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangeValueType")
public class RangeValueType
        extends ValueType {

    @XmlAttribute(name = "included", required = true)
    protected boolean included;

    /**
     * Gets the value of the included property.
     */
    public boolean isIncluded() {
        return included;
    }

    /**
     * Sets the value of the included property.
     */
    public void setIncluded(boolean value) {
        this.included = value;
    }

}