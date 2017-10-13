package edu.illinois.ncsa.incore.services.fragility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;

public class MappingSubject {
    @JsonProperty("schema")
    public SchemaType schemaType;

    @JsonProperty("inventory")
    public GeoJsonObject inventory;

    public MappingSubject() {

    }

    public MappingSubject(SchemaType type, GeoJsonObject inventory) {
        this.schemaType = type;
        this.inventory = inventory;
    }
}
