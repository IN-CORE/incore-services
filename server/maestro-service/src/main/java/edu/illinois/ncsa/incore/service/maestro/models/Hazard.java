package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class Hazard {
    public String id;
    public String hazardType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }
}
