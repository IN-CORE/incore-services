/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class FragilityCurveParameter {
    public String name;
    public String unit;
    public String description;
    public String fullName;
    public String expression;

    public FragilityCurveParameter() {
    }

    public FragilityCurveParameter(String name, String unit, String description, String fullName, String expression) {
        this.name = name;
        this.unit = unit;
        this.description = description;
        this.fullName = fullName;
        this.expression = expression;
    }
}
