/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import dev.morphia.annotations.Entity;

@Entity("TornadoDataset")
public class TornadoDataset extends Tornado {
    // CMN: this could be moved to the parent if we determine there will be no difference between probabilistic and
    // deterministic tornadoes. If there would be multiple files with different probabilities, this should be
    // modified similar to the Earthquake HazardDataset and the Tsunami hazard dataset
    private String datasetId;

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
