package edu.illinois.ncsa.incore.service.hazard.models.tornado;

public class TornadoDataset extends Tornado {
    // CMN: this could be moved to the parent if we determine there will be no difference between probabilistic and
    // deterministic tornadoes. If there would be multiple files with different probabilities, this should be
    // modified similar to the Earthquake HazardDataset and the Tsunami hazard dataset
    private String datasetId;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
