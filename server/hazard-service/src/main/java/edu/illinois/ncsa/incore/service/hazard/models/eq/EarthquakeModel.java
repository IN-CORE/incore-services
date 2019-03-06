/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
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
