/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import dev.morphia.annotations.Entity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedList;
import java.util.List;

@Schema(name = "Earthquake dataset", description = "Contains id, description, name, privileges and the hazard datasets")
@Entity("EarthquakeDataset")
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
