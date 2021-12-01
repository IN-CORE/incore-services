/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Entity;

import java.util.List;

@Entity("FragilitySet")
public class FragilitySet extends DFR3Set {
    protected List<String> demandTypes;
    protected List<String> demandUnits;
    protected List<DFR3Curve> fragilityCurves;

    public List<String> getDemandTypes() {
        return demandTypes;
    }

    public void setDemandTypes(List<String> demandTypes) {
        this.demandTypes = demandTypes;
    }

    public List<String> getDemandUnits() {
        return demandUnits;
    }

    public void setDemandUnits(List<String> demandUnits) {
        this.demandUnits = demandUnits;
    }

    public List<DFR3Curve> getFragilityCurves() {
        return fragilityCurves;
    }

}
