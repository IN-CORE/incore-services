package edu.illinois.ncsa.incore.service.project.models;

public class DFR3MappingResource extends ProjectResource {

    // Enum for status
    public enum Type {
        fragility,
        restoration,
        repair,
        recovery
    }
    public Type type;

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
