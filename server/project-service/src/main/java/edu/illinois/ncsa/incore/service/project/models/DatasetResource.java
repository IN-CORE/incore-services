package edu.illinois.ncsa.incore.service.project.models;

public class DatasetResource extends ProjectResource{

    public String type;

    public DatasetResource() {
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
