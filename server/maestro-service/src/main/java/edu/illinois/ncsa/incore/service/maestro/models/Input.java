package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class Input {
    public String datasetId;
    public String dataType;

    public String getId() {
        return datasetId;
    }

    public void setId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
