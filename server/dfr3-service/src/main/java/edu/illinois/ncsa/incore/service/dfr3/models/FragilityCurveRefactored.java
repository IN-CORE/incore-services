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

import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

//@XmlTransient
@Embedded
public class FragilityCurveRefactored extends FragilityCurve {
    public List<Rule> rules;
    public ReturnType returnType;

    // for building period fragility curve, it can over write the common fragility curve paramters outside
    public List<FragilityCurveParameter> fragilityCurveParameters;


    public FragilityCurveRefactored() {
        super();
    }

    public FragilityCurveRefactored(List<Rule> rules, ReturnType returnType,
                            List<FragilityCurveParameter> fragilityCurveParameters,
                            String label) {
        super(label);

        this.rules = rules;
        this.returnType = returnType;
        this.fragilityCurveParameters = fragilityCurveParameters;
    }
}
