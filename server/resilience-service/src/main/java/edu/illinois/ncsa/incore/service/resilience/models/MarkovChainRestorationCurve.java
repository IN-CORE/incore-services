/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.resilience.models;

public class MarkovChainRestorationCurve extends RestorationCurve {
    public double param1;
    public double param2;
    public double param3;
    public double param4;
    public double param5;
    public double param6;
    public double param7;
    public double param8;
    public CurveType curveType;

    public MarkovChainRestorationCurve() {
        super();
    }

    public MarkovChainRestorationCurve(double param1, double param2, double param3,
                                       double param4, double param5, double param6, double param7, double param8,
                                       CurveType curveType, String label) {
        super(label);

        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;

        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
        this.param8 = param8;
        this.curveType = curveType;
    }
}
