//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.reusable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * The date or date range of the access restriction for all or portions of the data. Includes a reason for the access restriction as well as the user group to which the restriction applies.
 * <p>
 * <p>Java class for AccessRestrictionDateType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="AccessRestrictionDateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}DateType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:reusable:3_2}Reason" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}User" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessRestrictionDateType")
public class AccessRestrictionDateType
        extends DateType {


}