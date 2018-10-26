package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

public class HurricaneSimulationDataset {

    private String absTime;
    private String datasetId;
    //Include demandType and units?


    public String getAbsTime() {
        return absTime;
    }

    public void setAbsTime(String absTime) {
        this.absTime = absTime;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
