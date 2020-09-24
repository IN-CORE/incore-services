/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Entity;

import java.util.List;

@Entity("FragilitySet")
public class FragilitySetRe extends FragilitySet {
    public List<FragilityCurveParameter> fragilityCurveParameters;

    public FragilitySetRe() { super(); }

    public List<FragilityCurveParameter> fragilityCurveParameters() { return fragilityCurveParameters; }

}
