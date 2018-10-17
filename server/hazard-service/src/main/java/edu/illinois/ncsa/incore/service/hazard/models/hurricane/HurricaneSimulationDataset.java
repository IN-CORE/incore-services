package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

public class HurricaneSimulationDataset {

    private String absTime;
    private String hazardDatasetId;
    //Include demandType and units?


    public String getAbsTime() {
        return absTime;
    }

    public void setAbsTime(String absTime) {
        this.absTime = absTime;
    }

    public String getHazardDatasetId() {
        return hazardDatasetId;
    }

    public void setHazardDatasetId(String hazardDatasetId) {
        this.hazardDatasetId = hazardDatasetId;
    }
}
