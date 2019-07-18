/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models;

public class PeriodStandardFragilityCurve extends StandardFragilityCurve {
    public double periodParam2;
    public double periodParam1;
    public double periodParam0;
    public int periodEqnType;

    public PeriodStandardFragilityCurve() {
        super();
    }

    public PeriodStandardFragilityCurve(double median, double beta, CurveType curveType, String label,
                                        int periodEqnType, double periodParam0, double periodParam1, double periodParam2) {
        super(median, beta, curveType, label);

        this.periodEqnType = periodEqnType;
        this.periodParam0 = periodParam0;
        this.periodParam1 = periodParam1;
        this.periodParam2 = periodParam2;
    }
}
