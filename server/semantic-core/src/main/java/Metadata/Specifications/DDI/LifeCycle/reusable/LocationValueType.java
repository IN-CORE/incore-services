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
 * A location of the specified geographic level providing information on its name, identification codes, temporal and spatial coverage as expressed by bounding and excluding polygon descriptions or references.
 * <p>
 * <p>Java class for LocationValueType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="LocationValueType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:reusable:3_2}IdentifiableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:reusable:3_2}LocationValueName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}GeographicLocationIdentifier" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}DefiningCharacteristic" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}GeographicTime" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}GeographicBoundary" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}SupersedesLocationValue" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:reusable:3_2}PrecedesLocationValue" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationValueType", propOrder = {
        "locationValueName",
        "geographicLocationIdentifier",
        "definingCharacteristic",
        "geographicTime",
        "geographicBoundary",
        "supersedesLocationValue",
        "precedesLocationValue"
})
public class LocationValueType
        extends IdentifiableType {

    @XmlElement(name = "LocationValueName")
    protected List<NameType> locationValueName;
    @XmlElement(name = "GeographicLocationIdentifier")
    protected List<GeographicLocationIdentifierType> geographicLocationIdentifier;
    @XmlElement(name = "DefiningCharacteristic")
    protected List<DefiningCharacteristicType> definingCharacteristic;
    @XmlElement(name = "GeographicTime")
    protected DateType geographicTime;
    @XmlElement(name = "GeographicBoundary")
    protected List<GeographicBoundaryType> geographicBoundary;
    @XmlElement(name = "SupersedesLocationValue")
    protected List<RelatedLocationValueReferenceType> supersedesLocationValue;
    @XmlElement(name = "PrecedesLocationValue")
    protected List<RelatedLocationValueReferenceType> precedesLocationValue;

    /**
     * A name for the Location. May be expressed in multiple languages. Repeat the element to express names with different content, for example different names for different systems.Gets the value of the locationValueName property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationValueName property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationValueName().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NameType }
     */
    public List<NameType> getLocationValueName() {
        if (locationValueName == null) {
            locationValueName = new ArrayList<NameType>();
        }
        return this.locationValueName;
    }

    /**
     * A unique identifier for the geographic location. Repeat for multiple identifiers from other authorized sources.Gets the value of the geographicLocationIdentifier property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geographicLocationIdentifier property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeographicLocationIdentifier().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeographicLocationIdentifierType }
     */
    public List<GeographicLocationIdentifierType> getGeographicLocationIdentifier() {
        if (geographicLocationIdentifier == null) {
            geographicLocationIdentifier = new ArrayList<GeographicLocationIdentifierType>();
        }
        return this.geographicLocationIdentifier;
    }

    /**
     * Use to attach one or more characteristics which define the area. These are often useful in terms of selection. For example a U.S. Block may be classified as Urban, Rural, or Mixed. The defining characteristic supports the use of a controlled vocabulary and may provide a time period for which the characteristic is valid.Gets the value of the definingCharacteristic property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the definingCharacteristic property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefiningCharacteristic().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DefiningCharacteristicType }
     */
    public List<DefiningCharacteristicType> getDefiningCharacteristic() {
        if (definingCharacteristic == null) {
            definingCharacteristic = new ArrayList<DefiningCharacteristicType>();
        }
        return this.definingCharacteristic;
    }

    /**
     * The time period for which the description (names and codes) are valid. Use a date range when start and end periods are known (or the location description is still valid). If the range is unknown indicate a single date for which you know the description is valid. This will allow others to relate it to a fuller date range if external information become available.
     *
     * @return possible object is
     * {@link DateType }
     */
    public DateType getGeographicTime() {
        return geographicTime;
    }

    /**
     * Sets the value of the geographicTime property.
     *
     * @param value allowed object is
     *              {@link DateType }
     */
    public void setGeographicTime(DateType value) {
        this.geographicTime = value;
    }

    /**
     * A choice of a BoundingBox and/or a set of BoundingPolygons and ExcludingPolygons that describe an area for a specific time period. Repeat the GeographicBoundary for changes in the geographic footprint of the LocationValue or when providing references to BoundingPolygons from different sources.Gets the value of the geographicBoundary property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geographicBoundary property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeographicBoundary().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeographicBoundaryType }
     */
    public List<GeographicBoundaryType> getGeographicBoundary() {
        if (geographicBoundary == null) {
            geographicBoundary = new ArrayList<GeographicBoundaryType>();
        }
        return this.geographicBoundary;
    }

    /**
     * Provides a reference to the LocationValue or Values that the current LocationValue supersedes partially or fully. For example, if the LocationValue was the State of North Dakota as defined in 1890 it would supersede the LocationValue for Dakota Territory (1861-1889) as a description of "part" of that Territory.Gets the value of the supersedesLocationValue property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supersedesLocationValue property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupersedesLocationValue().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelatedLocationValueReferenceType }
     */
    public List<RelatedLocationValueReferenceType> getSupersedesLocationValue() {
        if (supersedesLocationValue == null) {
            supersedesLocationValue = new ArrayList<RelatedLocationValueReferenceType>();
        }
        return this.supersedesLocationValue;
    }

    /**
     * Provides a reference to the LocationValue or Values that the current LocationValue immediately precedes partially or fully. For example, if the LocationValue was the Dakota Territory (1861-1889) preceded the State of North Dakota and the State of South Dakota as defined in 1890. The Dakota Territory provided "part" of its area in the creation of each new LocationValue.Gets the value of the precedesLocationValue property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the precedesLocationValue property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrecedesLocationValue().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RelatedLocationValueReferenceType }
     */
    public List<RelatedLocationValueReferenceType> getPrecedesLocationValue() {
        if (precedesLocationValue == null) {
            precedesLocationValue = new ArrayList<RelatedLocationValueReferenceType>();
        }
        return this.precedesLocationValue;
    }

}