package edu.illinois.ncsa.incore.service.project.models;

public class DatasetResource extends ProjectResource{

    public String type;

    public DatasetResource() {
    }

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