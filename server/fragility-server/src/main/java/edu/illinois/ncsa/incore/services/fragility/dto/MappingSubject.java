package edu.illinois.ncsa.incore.services.fragility.dto;

import org.geojson.FeatureCollection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MappingSubject {
    @XmlElement(name = "schema")
    public SchemaType schemaType;

    @XmlElement(name = "inventory")
    public FeatureCollection inventory;

    public MappingSubject() {

    }

    public MappingSubject(SchemaType type, FeatureCollection inventory) {
        this.schemaType = type;
        this.inventory = inventory;
    }
}
