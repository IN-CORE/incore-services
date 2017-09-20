//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.05 at 03:37:15 PM CST 
//


package Metadata.Specifications.DDI.CodeBook;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for catgryGrpType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="catgryGrpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:codebook:2_5}baseElementType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{ddi:codebook:2_5}labl" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:codebook:2_5}catStat" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{ddi:codebook:2_5}txt" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="missing" default="N"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="Y"/&gt;
 *             &lt;enumeration value="N"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="missType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="catgry" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="catGrp" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="levelno" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="levelnm" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="compl" default="true"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="true"/&gt;
 *             &lt;enumeration value="false"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="excls" default="true"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="true"/&gt;
 *             &lt;enumeration value="false"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "catgryGrpType", propOrder = {
        "labls",
        "catStats",
        "txts"
})
@XmlRootElement(name = "catgryGrp")
public class CatgryGrp
        extends BaseElementType {

    @XmlElement(name = "labl")
    protected List<Labl> labls;
    @XmlElement(name = "catStat")
    protected List<CatStat> catStats;
    @XmlElement(name = "txt")
    protected List<Txt> txts;
    @XmlAttribute(name = "missing")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String missing;
    @XmlAttribute(name = "missType")
    protected String missType;
    @XmlAttribute(name = "catgry")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> catgries;
    @XmlAttribute(name = "catGrp")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> catGrps;
    @XmlAttribute(name = "levelno")
    protected String levelno;
    @XmlAttribute(name = "levelnm")
    protected String levelnm;
    @XmlAttribute(name = "compl")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String compl;
    @XmlAttribute(name = "excls")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String excls;

    /**
     * Gets the value of the labls property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the labls property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLabls().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Labl }
     */
    public List<Labl> getLabls() {
        if (labls == null) {
            labls = new ArrayList<Labl>();
        }
        return this.labls;
    }

    /**
     * Gets the value of the catStats property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catStats property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatStats().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CatStat }
     */
    public List<CatStat> getCatStats() {
        if (catStats == null) {
            catStats = new ArrayList<CatStat>();
        }
        return this.catStats;
    }

    /**
     * Gets the value of the txts property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the txts property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTxts().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Txt }
     */
    public List<Txt> getTxts() {
        if (txts == null) {
            txts = new ArrayList<Txt>();
        }
        return this.txts;
    }

    /**
     * Gets the value of the missing property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMissing() {
        if (missing == null) {
            return "N";
        } else {
            return missing;
        }
    }

    /**
     * Sets the value of the missing property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMissing(String value) {
        this.missing = value;
    }

    /**
     * Gets the value of the missType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMissType() {
        return missType;
    }

    /**
     * Sets the value of the missType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMissType(String value) {
        this.missType = value;
    }

    /**
     * Gets the value of the catgries property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catgries property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatgries().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getCatgries() {
        if (catgries == null) {
            catgries = new ArrayList<Object>();
        }
        return this.catgries;
    }

    /**
     * Gets the value of the catGrps property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catGrps property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatGrps().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     */
    public List<Object> getCatGrps() {
        if (catGrps == null) {
            catGrps = new ArrayList<Object>();
        }
        return this.catGrps;
    }

    /**
     * Gets the value of the levelno property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLevelno() {
        return levelno;
    }

    /**
     * Sets the value of the levelno property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLevelno(String value) {
        this.levelno = value;
    }

    /**
     * Gets the value of the levelnm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLevelnm() {
        return levelnm;
    }

    /**
     * Sets the value of the levelnm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLevelnm(String value) {
        this.levelnm = value;
    }

    /**
     * Gets the value of the compl property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCompl() {
        if (compl == null) {
            return "true";
        } else {
            return compl;
        }
    }

    /**
     * Sets the value of the compl property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCompl(String value) {
        this.compl = value;
    }

    /**
     * Gets the value of the excls property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExcls() {
        if (excls == null) {
            return "true";
        } else {
            return excls;
        }
    }

    /**
     * Sets the value of the excls property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExcls(String value) {
        this.excls = value;
    }

}