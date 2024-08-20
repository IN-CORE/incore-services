package edu.illinois.ncsa.incore.service.project.models;

public class DFR3MappingResource extends ProjectResource {

    // Enum for status
    public enum Type {
        fragility,
        restoration,
        repair,
        recovery
    }
    private DFR3MappingResource.Type type;

    public DFR3MappingResource() {
    }

    public DFR3MappingResource(String id, Status status, DFR3MappingResource.Type type) {
        super(id, status);
        this.type = type;
    }

    // Getter and Setter for hazardType
    public DFR3MappingResource.Type getType() {
        return type;
    }

    public void setHazardType(DFR3MappingResource.Type type) {
        this.type = type;
    }
}
