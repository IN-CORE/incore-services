package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class Hazard {
    public String hazardId;
    public String hazardType;

    public String getId() {
        return hazardId;
    }

    public void setId(String hazardId) {
        this.hazardId = hazardId;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }
}
