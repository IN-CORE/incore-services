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

@Embedded
public class LogitCurveParameter {
    // parameter name / friendly name
    public String name;

    // parameter unit
    public String unit;

    // parameter value - coefficient (A)
    public double coefficient;

    // parameter intercept default input - intercept term (X)
    public double interceptTermDefault;

    public LogitCurveParameter() {
    }

    public LogitCurveParameter(String name, String unit, double coefficient, double interceptTermDefault) {
        this.name = name;
        this.unit = unit;
        this.coefficient = coefficient;
        this.interceptTermDefault = interceptTermDefault;
    }
}
