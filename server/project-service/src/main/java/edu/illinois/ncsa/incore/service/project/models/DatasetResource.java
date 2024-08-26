package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetResource extends ProjectResource{

    // TODO Read from dataset object
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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
