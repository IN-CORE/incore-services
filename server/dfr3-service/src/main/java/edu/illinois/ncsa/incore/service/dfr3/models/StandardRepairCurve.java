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

public class StandardRepairCurve extends RepairCurve {
    public double median;
    public double beta;
    public CurveType curveType;

    public StandardRepairCurve() {
        super();
    }

    public StandardRepairCurve(double median, double beta, CurveType curveType, String label) {
        super(label);

        this.median = median;
        this.beta = beta;
        this.curveType = curveType;
    }
}
