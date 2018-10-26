package edu.illinois.ncsa.incore.service.hazard.models.eq;

import io.swagger.annotations.ApiModel;

import java.util.LinkedList;
import java.util.List;

@ApiModel(value="Earthquake dataset", description="Contains id, description, name, privileges and the hazard datasets")
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
