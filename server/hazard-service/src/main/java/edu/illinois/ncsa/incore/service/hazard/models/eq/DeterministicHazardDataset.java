package edu.illinois.ncsa.incore.service.hazard.models.eq;

public class DeterministicHazardDataset extends HazardDataset {
    // The parameters used to derive the dataset
    private EqParameters eqParameters;

    public EqParameters getEqParameters() {
        return eqParameters;
    }

    public void setEqParameters(EqParameters eqParameters) {
        this.eqParameters = eqParameters;
    }

}
