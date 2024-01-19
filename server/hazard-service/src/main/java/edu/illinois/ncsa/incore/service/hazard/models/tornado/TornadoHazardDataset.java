package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.morphia.annotations.Embedded;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "hazardType")
@Embedded
public class TornadoHazardDataset {
    private String datasetId;
    private String demandType;
    private String demandUnits;
    private Double threshold = null;

    public String getDatasetId() {
        return datasetId;
    }

    public String getDemandType() {
        return demandType;
    }

    public String getDemandUnits() {
        return demandUnits;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public void setDemandType(String demandType) {
        this.demandType = demandType;
    }

    public void setDemandUnits(String demandUnits) {
        this.demandUnits = demandUnits;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @JsonIgnore
    public String getThresholdJsonString(){
        return String.format("{'%s': {'value': %s, 'unit': '%s'}}",
            this.demandType, this.threshold, this.demandUnits);
    }
}
