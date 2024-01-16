/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import dev.morphia.annotations.Entity;

import java.util.Map;

@Entity("EarthquakeModel")
public class EarthquakeModel extends Earthquake {
    // Map of attenuation models and weights
    private Map<String, Double> attenuations;
    // Earthquake parameters (e.g. epicenter location, depth, etc
    private EqParameters eqParameters;

    private EqVisualization visualizationParameters;
    // Default site class
    private final String defaultSiteClass;
    private final String siteAmplification;

    // Visualization raster
    private HazardDataset hazardDataset;

    public EarthquakeModel() {
        defaultSiteClass = NEHRPSoilType.D;
        siteAmplification = "NEHRP";
    }

    public void setHazardDataset(HazardDataset hazardDataset) {
        this.hazardDataset = hazardDataset;
    }

    public HazardDataset getHazardDataset() {
        return this.hazardDataset;
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
