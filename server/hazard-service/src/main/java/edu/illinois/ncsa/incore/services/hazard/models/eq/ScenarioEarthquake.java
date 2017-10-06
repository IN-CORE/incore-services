package edu.illinois.ncsa.incore.services.hazard.models.eq;

import edu.illinois.ncsa.incore.services.hazard.NEHRPSoilType;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class ScenarioEarthquake {
    @Id
    @Property("_id")
    private String id;

    // Map of attenuation models and weights
    private Map<String, Double> attenuations;
    // Earthquake parameters (e.g. epicenter location, depth, etc
    private EqParameters eqParameters;

    // ID of the site class dataset
    private String siteClassDataset;

    // Default site class
    private String defaultSiteClass;

    private String siteAmplification;

    public ScenarioEarthquake() {
        defaultSiteClass = NEHRPSoilType.D;
        siteAmplification = "NEHRP";
    }

    public Map<String, Double> getAttenuations() {
        return this.attenuations;
    }

    public EqParameters getEqParameters() {
        return eqParameters;
    }

    public String getId() {
        return id;
    }

    public String getSiteClassDataset() {
        return siteClassDataset;
    }

    public String getDefaultSiteClass() {
        return defaultSiteClass;
    }

    public String getSiteAmplification() {
        return siteAmplification;
    }
}
