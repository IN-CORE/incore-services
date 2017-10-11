package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SeismicHazardResult {
    private String period;
    private String demand;
    private String units;
    private double hazardValue;

    public SeismicHazardResult(double hazardValue, String hazardType, String demand) {
        this.hazardValue = hazardValue;
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(hazardType);
        this.demand = demand;
    }

    public SeismicHazardResult(double hazardValue, String hazardType) {
        this.hazardValue = hazardValue;
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(hazardType);
    }

    public double getHazardValue() {
        return hazardValue;
    }

    public String getPeriod() {
        return period;
    }

    public String getUnits() {
        return units;
    }

    public String getDemand() {
        return demand;
    }
}
