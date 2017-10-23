/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models;

import java.math.BigDecimal;

public class StandardFragilityCurve extends FragilityCurve {
    public BigDecimal median;
    public BigDecimal beta;
    public FragilityCurveType curveType;

    public StandardFragilityCurve() {
        super();
    }

    public StandardFragilityCurve(BigDecimal median, BigDecimal beta, FragilityCurveType curveType, String label) {
        super(label);

        this.median = median;
        this.beta = beta;
        this.curveType = curveType;
    }
}
