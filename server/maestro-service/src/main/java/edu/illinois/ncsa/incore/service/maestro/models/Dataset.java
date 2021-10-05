package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class Dataset {
    public String id;
    public String dataType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
