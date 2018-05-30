package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LiquefactionHazardResult {

    private String pgdUnits;
    private double pgd;
    private double liqProbability;

    public LiquefactionHazardResult(double pgd, String pgdUnits, double liqProbability) {
        this.pgd = pgd;
        this.pgdUnits = pgdUnits;
        this.liqProbability = liqProbability;
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


}
