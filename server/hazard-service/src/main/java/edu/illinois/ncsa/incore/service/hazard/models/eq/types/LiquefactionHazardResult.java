package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LiquefactionHazardResult {
    private String pgdUnits;
    private double pgd;
    private double liqProbability;
    private double[] groundFailureProb;

    public LiquefactionHazardResult(double pgd, String pgdUnits, double liqProbability, double[] groundFailureProb) {
        this.pgd = pgd;
        this.pgdUnits = pgdUnits;
        this.liqProbability = liqProbability;
        this.groundFailureProb = groundFailureProb;
    }

    public String getPgdUnits() {
        return pgdUnits;
    }

    public void setPgdUnits(String pgdUnits) {
        this.pgdUnits = pgdUnits;
    }

    public double getPgd() {
        return pgd;
    }

    public void setPgd(double pgd) {
        this.pgd = pgd;
    }

    public double getLiqProbability() {
        return liqProbability;
    }

    public void setLiqProbability(double liqProbability) {
        this.liqProbability = liqProbability;
    }

    public double[] getGroundFailureProb() {
        return groundFailureProb;
    }

    public void setGroundFailureProb(double[] groundFailureProb) {
        this.groundFailureProb = groundFailureProb;
    }
}
