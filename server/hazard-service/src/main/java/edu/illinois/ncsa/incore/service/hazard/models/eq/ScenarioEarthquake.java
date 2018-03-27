/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class ScenarioEarthquake {
    @Id
    @Property("_id")
    private ObjectId id;

    // Map of attenuation models and weights
    private Map<String, Double> attenuations;
    // Earthquake parameters (e.g. epicenter location, depth, etc
    private EqParameters eqParameters;

    private EqVisualization visualizationParameters;

    // ID of the site class dataset
    private String siteClassDataset;

    // Default site class
    private String defaultSiteClass;

    private String siteAmplification;

    private String rasterDatasetId;

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
        return id.toString();
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

    public String getRasterDatasetId() {
        return rasterDatasetId;
    }

    public void setRasterDatasetId(String rasterDatasetId) {
        this.rasterDatasetId = rasterDatasetId;
    }

    public EqVisualization getVisualizationParameters() {
        return visualizationParameters;
    }

    public void setVisualizationParameters(EqVisualization visualizationParameters) {
        this.visualizationParameters = visualizationParameters;
    }


}
