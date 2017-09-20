package edu.illinois.ncsa.incore.services.fragility.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum SchemaType {
    @XmlEnumValue("building")
    Building,
    @XmlEnumValue("bridge")
    Bridge,
    @XmlEnumValue("roadway")
    Roadway,
    @XmlEnumValue("railway")
    Railway
}
