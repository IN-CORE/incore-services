package edu.illinois.ncsa.incore.service.hazard.models.eq;

import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;

import java.util.List;

public class EqValuesRequest extends ValuesRequest {

    private List<Boolean> amplifyHazards;
    public List<Boolean> getAmplifyHazards() {
        return amplifyHazards;
    }

    public void setAmplifyHazards(List<Boolean> amplifyHazard) {
        this.amplifyHazards = amplifyHazard;
    }



}
