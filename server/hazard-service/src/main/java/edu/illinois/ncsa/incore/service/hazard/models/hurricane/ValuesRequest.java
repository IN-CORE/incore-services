package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;

import java.util.List;

public class ValuesRequest  {
    private List<String> demands;
    private List<String> units;
    private IncorePoint loc;

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

    public IncorePoint getLoc() {
        return loc;
    }

    public void setLoc(IncorePoint loc) {
        this.loc = loc;
    }

}
