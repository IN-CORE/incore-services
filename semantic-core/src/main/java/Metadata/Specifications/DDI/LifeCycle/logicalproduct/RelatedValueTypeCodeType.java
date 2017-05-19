//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.logicalproduct;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RelatedValueTypeCodeType.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RelatedValueTypeCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="GreaterThan"/&gt;
 *     &lt;enumeration value="LessThan"/&gt;
 *     &lt;enumeration value="Equal"/&gt;
 *     &lt;enumeration value="GreaterThanOrEqual"/&gt;
 *     &lt;enumeration value="LessThanOrEqual"/&gt;
 *     &lt;enumeration value="NotEqual"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "RelatedValueTypeCodeType")
@XmlEnum
public enum RelatedValueTypeCodeType {


    /**
     * The value of the source object is greater than the value of the target object.
     */
    @XmlEnumValue("GreaterThan")
    GREATER_THAN("GreaterThan"),

    /**
     * The value of the source object is less than the value of the target object.
     */
    @XmlEnumValue("LessThan")
    LESS_THAN("LessThan"),

    /**
     * The value of the source object is equal to the value of the target object.
     */
    @XmlEnumValue("Equal")
    EQUAL("Equal"),

    /**
     * The value of the source object is greater than or equal to the value of the target object.
     */
    @XmlEnumValue("GreaterThanOrEqual")
    GREATER_THAN_OR_EQUAL("GreaterThanOrEqual"),

    /**
     * The value of the source object is less than or equal to the value of the target object.
     */
    @XmlEnumValue("LessThanOrEqual")
    LESS_THAN_OR_EQUAL("LessThanOrEqual"),

    /**
     * The value of the source object is not equal the value of the target object.
     */
    @XmlEnumValue("NotEqual")
    NOT_EQUAL("NotEqual");
    private final String value;

    RelatedValueTypeCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelatedValueTypeCodeType fromValue(String v) {
        for (RelatedValueTypeCodeType c : RelatedValueTypeCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}