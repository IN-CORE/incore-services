/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.flood;

import java.util.LinkedList;
import java.util.List;

public class FloodDataset extends Flood {
    private List<FloodHazardDataset> hazardDatasets = new LinkedList<FloodHazardDataset>();

    public FloodDataset() {
    }

    public void setFloodHazardDatasets(List<FloodHazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addFloodHazardDataset(FloodHazardDataset hazardDataset) {
        hazardDatasets.add(hazardDataset);
    }

    public List<FloodHazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }
}
