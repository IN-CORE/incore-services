/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tsunami;

import java.util.LinkedList;
import java.util.List;

public class TsunamiDataset extends Tsunami {
    private List<TsunamiHazardDataset> hazardDatasets = new LinkedList<TsunamiHazardDataset>();

    public TsunamiDataset() {
    }

    public void setTsunamiHazardDatasets(List<TsunamiHazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addTsunamiHazardDataset(TsunamiHazardDataset hazardDataset) {
        hazardDatasets.add(hazardDataset);
    }

    public List<TsunamiHazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }
}
