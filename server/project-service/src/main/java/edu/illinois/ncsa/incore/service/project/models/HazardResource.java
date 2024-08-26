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
    public Type type;

    public HazardResource() {
    }

    // Getter and Setter for hazardType
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
