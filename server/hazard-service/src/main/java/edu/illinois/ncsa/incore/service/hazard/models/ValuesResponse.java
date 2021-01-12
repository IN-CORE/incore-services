package edu.illinois.ncsa.incore.service.hazard.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;

import java.util.List;

public class ValuesResponse {
    private List<Double> hazardValues;
    private List<String> demands;
    private List<String> units;
    private IncorePoint loc;

    public List<Double> getHazardValues() {
        return hazardValues;
    }

    public void setHazardValues(List<Double> hazardValues) {
        this.hazardValues = hazardValues;
    }

    public List<String> getDemands() {
        return demands;
    }

    public void setDemands(List<String> demands) {
        this.demands = demands;
    }

    public List<String> getUnits() {
        return units;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public String getLoc() {
        return loc.toString();
    }

    public void setLoc(IncorePoint loc) {
        this.loc = loc;
    }

}
