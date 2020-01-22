/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.models;

public class PeriodBuildingFragilityCurve extends FragilityCurve {
    public int periodEqnType;
    public double periodParam1;
    public double periodParam2;
    public double periodParam0;
    public double fsParam0;
    public double fsParam1;
    public double fsParam2;
    public double fsParam3;
    public double fsParam4;
    public double fsParam5;

    public PeriodBuildingFragilityCurve() {
        super();
    }

    public PeriodBuildingFragilityCurve(String label, int periodEqnType, double periodParam0, double periodParam1, double periodParam2,
                                        double fsParam0, double fsParam1, double fsParam2, double fsParam3, double fsParam4, double fsParam5) {
        super(label);

        this.periodEqnType = periodEqnType;
        this.periodParam1 = periodParam1;
        this.periodParam2 = periodParam2;
        this.periodParam0 = periodParam0;

        this.fsParam0 = fsParam0;
        this.fsParam1 = fsParam1;
        this.fsParam2 = fsParam2;
        this.fsParam3 = fsParam3;
        this.fsParam4 = fsParam4;
        this.fsParam5 = fsParam5;
    }
}
