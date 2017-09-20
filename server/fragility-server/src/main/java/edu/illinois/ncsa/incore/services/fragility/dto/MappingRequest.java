package edu.illinois.ncsa.incore.services.fragility.dto;

import org.geojson.FeatureCollection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

@XmlRootElement
public class MappingRequest {
    @XmlElement(name = "params")
    public HashMap<String, Object> parameters = new HashMap<>();

    @XmlElement(name = "subject")
    public MappingSubject mappingSubject;

    public MappingRequest() {

    }

    public MappingRequest(MappingSubject subject) {
        this.mappingSubject = subject;
    }

    public MappingRequest(MappingSubject subject, HashMap<String, Object> parameters) {
        this.mappingSubject = subject;
        this.parameters = parameters;
    }

    public MappingRequest(SchemaType schema, FeatureCollection collection) {
        this.mappingSubject = new MappingSubject(schema, collection);
    }

    public MappingRequest(SchemaType schema, FeatureCollection collection, HashMap<String, Object> parameters) {
        this.mappingSubject = new MappingSubject(schema, collection);
        this.parameters = parameters;
    }
}
