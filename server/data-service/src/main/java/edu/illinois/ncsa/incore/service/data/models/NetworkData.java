package edu.illinois.ncsa.incore.service.data.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class NetworkData {
    private String dataType;

    private String fileName;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
