/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Christopher Navarro, Chen Wang
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class ParametricFragilityCurve extends FragilityCurve {
    public List<LogitCurveParameter> parameters;
    public CurveType curveType;


    public ParametricFragilityCurve() {
        super();
    }

    public ParametricFragilityCurve(List<LogitCurveParameter> parameters, CurveType curveType, String label) {
        super(label);

        this.parameters = parameters;
        this.curveType = curveType;
    }
}
