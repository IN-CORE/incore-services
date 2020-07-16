/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import java.util.LinkedList;
import java.util.List;

public class HurricaneDataset extends Hurricane {
    private List<HurricaneHazardDataset> hazardDatasets = new LinkedList<HurricaneHazardDataset>();

    public HurricaneDataset() {
    }

    public void setHurricaneHazardDatasets(List<HurricaneHazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addHurricaneHazardDataset(HurricaneHazardDataset hazardDataset) {
        hazardDatasets.add(hazardDataset);
    }

    public List<HurricaneHazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }
}
