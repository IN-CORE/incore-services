package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.core.Base64Variants;

public class HazardResource extends ProjectResource {

    // Enum for status
    public enum Type {
        earthquake,
        tornado,
        tsunami,
        flood,
        hurricane,
    }
    private Type type;

    public HazardResource() {
    }

    public HazardResource(String id, Status status, Type type) {
        super(id, status);
        this.type = type;
    }

    // Getter and Setter for hazardType
    public Type getType() {
        return type;
    }

    public void setHazardType(Type type) {
        this.type = type;
    }
}