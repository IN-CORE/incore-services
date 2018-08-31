package edu.illinois.ncsa.incore.service.hazard.models.eq;

import java.util.LinkedList;
import java.util.List;

public class EarthquakeDataset extends Earthquake {

    private List<HazardDataset> hazardDatasets = new LinkedList<HazardDataset>();

    public EarthquakeDataset() {
    }

    public void setHazardDatasets(List<HazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addHazardDataset(HazardDataset hazardDataset) {
        hazardDatasets.add(hazardDataset);
    }

    public List<HazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }
}
