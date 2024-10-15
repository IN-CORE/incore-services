package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DFR3MappingResource extends ProjectResource {

    public enum Type {
        fragility,
        restoration,
        repair,
        recovery,
        unknown;

        // Map a string to a Type, ignoring case
        public static Type fromString(String mappingType) {
            try {
                return Type.valueOf(mappingType.toLowerCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                return unknown;
            }
        }
    }

    private Type type;
    private String mappingType;

    public String name;
    public String hazardType;
    public String inventoryType;

    public final List<Mapping> mappings = new ArrayList<>();
    public String creator;
    public String owner;

    public MappingEntryKey[] mappingEntryKeys;

    public DFR3MappingResource() {
    }

    // Getter for type with fallback to dataType if type is not set
    public Type getType() {
        if (type != null) {
            return type;
        }
        return Type.fromString(mappingType);
    }

    // Setter for type
    public void setType(String type) {
        this.type = Type.fromString(type);
    }

    // Getter and setter for dataType
    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.name != null && this.name.toLowerCase().contains(lowerCaseText)) ||
            (this.mappingType != null && this.mappingType.toLowerCase().contains(lowerCaseText)) ||
            (this.hazardType != null && this.hazardType.toLowerCase().contains(lowerCaseText)) ||
            (this.inventoryType != null && this.inventoryType.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.toLowerCase().contains(lowerCaseText)) ||
            (this.owner != null && this.owner.toLowerCase().contains(lowerCaseText)) ||
            (this.type != null && this.getType().toString().toLowerCase().contains(lowerCaseText));
    }
}
