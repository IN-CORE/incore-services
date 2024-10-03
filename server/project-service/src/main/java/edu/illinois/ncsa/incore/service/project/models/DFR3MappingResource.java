package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DFR3MappingResource extends ProjectResource {

    // Enum for status
    public enum Type {
        fragility,
        restoration,
        repair,
        recovery
    }
    public Type type;

    public String name;
    public String hazardType;
    public String inventoryType;

    @JsonProperty("mappingType")
    public String mappingType;

    public final List<Mapping> mappings = new ArrayList<>();
    public String creator;
    public String owner;

    public MappingEntryKey[] mappingEntryKeys;

    public DFR3MappingResource() {
    }

    // Getter and Setter for hazardType
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
