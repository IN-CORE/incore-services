/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tsunami;

import dev.morphia.annotations.Entity;

import java.util.LinkedList;
import java.util.List;

@Entity("TsunamiDataset")
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
