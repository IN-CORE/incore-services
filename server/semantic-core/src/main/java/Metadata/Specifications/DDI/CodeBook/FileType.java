//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.05 at 03:37:15 PM CST 
//


package Metadata.Specifications.DDI.CodeBook;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fileTypeType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="fileTypeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ddi:codebook:2_5}simpleTextType"&gt;
 *       &lt;attribute name="charset" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileTypeType")
@XmlRootElement(name = "fileType")
public class FileType
        extends SimpleTextType {

    @XmlAttribute(name = "charset")
    protected String charset;

    /**
     * Gets the value of the charset property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Sets the value of the charset property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCharset(String value) {
        this.charset = value;
    }

}