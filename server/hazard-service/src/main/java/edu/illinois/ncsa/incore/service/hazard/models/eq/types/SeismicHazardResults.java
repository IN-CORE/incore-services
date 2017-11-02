package main.java.edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;

@XmlRootElement
public class SeismicHazardResults {
    private String period;
    private String demandType;
    private String units;
    private List<HazardResult> hazardResults;

    public SeismicHazardResults(String hazardType, String demandType, List<HazardResult> hazardResults) {
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(hazardType);
        this.demandType = demandType;
        this.hazardResults = hazardResults;
    }

    public List<HazardResult> getHazardResults() {
        return hazardResults;
    }

    public String getPeriod() {
        return period;
    }

    public String getUnits() {
        return units;
    }

    public String getDemandType() {
        return demandType;
    }

}
