package edu.illinois.ncsa.incore.service.data.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class NetworkData {
    private String networkType;

    private String fileName;

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
