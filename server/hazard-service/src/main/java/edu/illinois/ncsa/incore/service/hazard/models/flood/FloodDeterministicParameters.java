/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.flood;

import dev.morphia.annotations.Embedded;

@Embedded
public class FloodDeterministicParameters {
    private String model = "riverine flooding"; // remove default when more models are added

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}

