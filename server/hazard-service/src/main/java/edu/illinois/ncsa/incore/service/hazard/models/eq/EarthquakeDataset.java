/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import io.swagger.annotations.ApiModel;

import java.util.LinkedList;
import java.util.List;

@ApiModel(value="Earthquake dataset", description="Contains id, description, name, privileges and the hazard datasets")
public class EarthquakeDataset extends Earthquake {

    private List<HazardDataset> hazardDatasets = new LinkedList<HazardDataset>();

    public EarthquakeDataset() {
    }

    public void setHazardDatasets(List<HazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addHazardDataset(HazardDataset hazardDataset) {
        hazardDatasets.add(hazardDataset);
    }

    public List<HazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }
}
