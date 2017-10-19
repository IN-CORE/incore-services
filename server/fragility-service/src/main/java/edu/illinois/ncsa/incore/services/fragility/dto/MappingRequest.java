package edu.illinois.ncsa.incore.services.fragility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.FeatureCollection;

import java.util.HashMap;

public class MappingRequest {
    @JsonProperty("params")
    public HashMap<String, Object> parameters = new HashMap<>();

    @JsonProperty("subject")
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
