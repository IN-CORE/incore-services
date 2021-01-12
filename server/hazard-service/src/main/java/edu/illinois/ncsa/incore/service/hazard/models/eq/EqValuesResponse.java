package edu.illinois.ncsa.incore.service.hazard.models.eq;

import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;

import java.util.List;

public class EqValuesResponse  extends ValuesResponse {
//    TODO: enable periods?
//    private List<String> periods;

    private List<Boolean> amplifyHazards;

//    public List<String> getPeriods() {
//        return periods;
//    }
//
//    public void setPeriods(List<String> periods) {
//        this.periods = periods;
//    }

    public List<Boolean> getAmplifyHazards() {
        return amplifyHazards;
    }

    public void setAmplifyHazards(List<Boolean> amplifyHazards) {
        this.amplifyHazards = amplifyHazards;
    }

}
