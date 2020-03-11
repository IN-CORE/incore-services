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

public class CustomExpressionFragilityCurve extends FragilityCurve {
    public String expression;

    public CustomExpressionFragilityCurve() {
        super();
    }

    public CustomExpressionFragilityCurve(String expression, String label) {
        super(label);
        this.expression = expression;
    }
}