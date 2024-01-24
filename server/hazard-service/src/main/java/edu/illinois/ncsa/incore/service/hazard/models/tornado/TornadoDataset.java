/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import dev.morphia.annotations.Entity;
import java.util.LinkedList;
import java.util.List;

@Entity("TornadoDataset")
public class TornadoDataset extends Tornado {
    // CMN: this could be moved to the parent if we determine there will be no difference between probabilistic and
    // deterministic tornadoes. If there would be multiple files with different probabilities, this should be
    // modified similar to the Earthquake HazardDataset and the Tsunami hazard dataset

    private List<TornadoHazardDataset> hazardDatasets;

    public TornadoDataset(){
        this.hazardDatasets = new LinkedList<>();
    }

    public List<TornadoHazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }

    public void setHazardDatasets(List<TornadoHazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addTornadoHazardDataset(TornadoHazardDataset hazardDataset) { this.hazardDatasets.add(hazardDataset); }

}
