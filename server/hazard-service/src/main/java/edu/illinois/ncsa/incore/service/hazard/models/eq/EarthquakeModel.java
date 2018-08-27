package edu.illinois.ncsa.incore.service.hazard.models.eq;

import java.util.Map;

public class EarthquakeModel extends Earthquake {
    // Map of attenuation models and weights
    private Map<String, Double> attenuations;
    // Earthquake parameters (e.g. epicenter location, depth, etc
    private EqParameters eqParameters;

    private EqVisualization visualizationParameters;
    // Default site class
    private String defaultSiteClass;
    private String siteAmplification;

    // Visualization raster
    private HazardDataset rasterDataset;

    public EarthquakeModel() {
        defaultSiteClass = NEHRPSoilType.D;
        siteAmplification = "NEHRP";
    }

    public void setHazardDataset(HazardDataset rasterDataset) {
        this.rasterDataset = rasterDataset;
    }

    public HazardDataset getRasterDataset() {
        return this.rasterDataset;
    }

    public Map<String, Double> getAttenuations() {
        return this.attenuations;
    }

    public EqParameters getEqParameters() {
        return eqParameters;
    }

    public EqVisualization getVisualizationParameters() {
        return visualizationParameters;
    }

    public void setVisualizationParameters(EqVisualization visualizationParameters) {
        this.visualizationParameters = visualizationParameters;
    }

    public String getDefaultSiteClass() {
        return defaultSiteClass;
    }

    public String getSiteAmplification() {
        return siteAmplification;
    }

}
