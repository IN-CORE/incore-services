package edu.illinois.ncsa.incore.common.models;

public class DatasetResource extends ProjectResource{

    private String type;

    public DatasetResource(String id, Status status, String type) {
        super(id, status);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setHazardType(String type) {
        this.type = type;
    }
}
