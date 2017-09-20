//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.04 at 03:10:13 PM CST 
//


package Metadata.Specifications.DDI.LifeCycle.reusable;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Used to identify described identifiable objects for purposes of internal and/or external referencing. Elements of this type cannot be versioned or maintained except as part of a complex parent element. Provides containers for Uniform Resource Name (URN) as well as ID information. An entity can either be identified either by a URN and/or an identification sequence. At a minimum, one or the other is required. You must designate they type of URN supported by your agency, either "Canonical" or "Deprecated". To fully support interoperability both the DDI URN and identification sequence should be used. Note that to support interoperability of the canonical and deprecated URN. If both URN and the identification sequence is used, and there is any conflict, the URN takes precedence. The action attribute is used ONLY for inheritance is a Group structure. For the local use the maintainable you can "Add", "Delete", or "Replace" an identified object. These actions only effect the local usage of the content. These changes cannot be inherited.
 * <p>
 * <p>Java class for AbstractIdentifiableType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="AbstractIdentifiableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice maxOccurs="2"&gt;
 *           &lt;element ref="{ddi:reusable:3_2}URN"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element ref="{ddi:reusable:3_2}Agency"/&gt;
 *             &lt;element ref="{ddi:reusable:3_2}ID"/&gt;
 *             &lt;element ref="{ddi:reusable:3_2}Version"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{ddi:reusable:3_2}UserID" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="inheritanceAction" type="{ddi:reusable:3_2}ActionCodeType" /&gt;
 *       &lt;attribute name="objectSource" type="{ddi:reusable:3_2}DDIURNType" /&gt;
 *       &lt;attribute name="scopeOfUniqueness" type="{ddi:reusable:3_2}UniquenessScopeType" default="Agency" /&gt;
 *       &lt;attribute name="isUniversallyUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractIdentifiableType", propOrder = {
        "urnOrAgencyAndID",
        "userID"
})
@XmlSeeAlso({
        IdentifiableType.class,
        AbstractVersionableType.class
})
public abstract class AbstractIdentifiableType {

    @XmlElementRefs({
            @XmlElementRef(name = "URN", namespace = "ddi:reusable:3_2", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "ID", namespace = "ddi:reusable:3_2", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "Version", namespace = "ddi:reusable:3_2", type = JAXBElement.class, required = false),
            @XmlElementRef(name = "Agency", namespace = "ddi:reusable:3_2", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> urnOrAgencyAndID;
    @XmlElement(name = "UserID")
    protected List<UserIDType> userID;
    @XmlAttribute(name = "inheritanceAction")
    protected ActionCodeType inheritanceAction;
    @XmlAttribute(name = "objectSource")
    protected String objectSource;
    @XmlAttribute(name = "scopeOfUniqueness")
    protected UniquenessScopeType scopeOfUniqueness;
    @XmlAttribute(name = "isUniversallyUnique")
    protected Boolean isUniversallyUnique;

    /**
     * Gets the value of the urnOrAgencyAndID property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the urnOrAgencyAndID property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getURNOrAgencyAndID().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link URNType }{@code >}
     * {@link JAXBElement }{@code <}{@link IDType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public List<JAXBElement<?>> getURNOrAgencyAndID() {
        if (urnOrAgencyAndID == null) {
            urnOrAgencyAndID = new ArrayList<JAXBElement<?>>();
        }
        return this.urnOrAgencyAndID;
    }

    /**
     * Allows for the specification of identifiers other than the specified DDI identification of the object. This may be a legacy ID from DDI-C, a system specific ID such as for a database or registry, or a non-DDI unique identifier. As the identifier is specific to a system the system must be identified with the UserID structure.Gets the value of the userID property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userID property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserID().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserIDType }
     */
    public List<UserIDType> getUserID() {
        if (userID == null) {
            userID = new ArrayList<UserIDType>();
        }
        return this.userID;
    }

    /**
     * Gets the value of the inheritanceAction property.
     *
     * @return possible object is
     * {@link ActionCodeType }
     */
    public ActionCodeType getInheritanceAction() {
        return inheritanceAction;
    }

    /**
     * Sets the value of the inheritanceAction property.
     *
     * @param value allowed object is
     *              {@link ActionCodeType }
     */
    public void setInheritanceAction(ActionCodeType value) {
        this.inheritanceAction = value;
    }

    /**
     * Gets the value of the objectSource property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getObjectSource() {
        return objectSource;
    }

    /**
     * Sets the value of the objectSource property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setObjectSource(String value) {
        this.objectSource = value;
    }

    /**
     * Gets the value of the scopeOfUniqueness property.
     *
     * @return possible object is
     * {@link UniquenessScopeType }
     */
    public UniquenessScopeType getScopeOfUniqueness() {
        if (scopeOfUniqueness == null) {
            return UniquenessScopeType.AGENCY;
        } else {
            return scopeOfUniqueness;
        }
    }

    /**
     * Sets the value of the scopeOfUniqueness property.
     *
     * @param value allowed object is
     *              {@link UniquenessScopeType }
     */
    public void setScopeOfUniqueness(UniquenessScopeType value) {
        this.scopeOfUniqueness = value;
    }

    /**
     * Gets the value of the isUniversallyUnique property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isIsUniversallyUnique() {
        return isUniversallyUnique;
    }

    /**
     * Sets the value of the isUniversallyUnique property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIsUniversallyUnique(Boolean value) {
        this.isUniversallyUnique = value;
    }

}