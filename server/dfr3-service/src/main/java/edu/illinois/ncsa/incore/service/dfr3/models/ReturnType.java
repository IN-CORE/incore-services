package edu.illinois.ncsa.incore.service.dfr3.models;

public class ReturnType {
    public String type;
    public String unit;
    public String description;

    public ReturnType() {

    }

    public ReturnType(String type, String unit, String description) {
        this.type = type;
        this.unit = unit;
        this.description = description;
    }
}
