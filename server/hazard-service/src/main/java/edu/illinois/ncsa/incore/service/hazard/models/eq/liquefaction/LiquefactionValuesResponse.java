package edu.illinois.ncsa.incore.service.hazard.models.eq.liquefaction;

import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;

import java.util.List;

public class LiquefactionValuesResponse {
    private List<Double> pgdValues;
    private Double liqProbability;
    private double[] groundFailureProb;
    private List<String> demands;
    private List<String> units;
    private IncorePoint loc;

    public Double getLiqProbability() {
        return liqProbability;
    }

    public void setLiqProbability(Double liqProbability) {
        this.liqProbability = liqProbability;
    }

    public double[] getGroundFailureProb() {
        return groundFailureProb;
    }

    public void setGroundFailureProb(double[] groundFailureProb) {
        this.groundFailureProb = groundFailureProb;
    }

    public List<Double> getPgdValues() {
        return pgdValues;
    }

    public void setPgdValues(List<Double> pgd) {
        this.pgdValues = pgd;
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
