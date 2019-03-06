/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

public class DeterministicHazardDataset extends HazardDataset {
    // The parameters used to derive the dataset
    private EqParameters eqParameters;

    public EqParameters getEqParameters() {
        return eqParameters;
    }

    public void setEqParameters(EqParameters eqParameters) {
        this.eqParameters = eqParameters;
    }

}
