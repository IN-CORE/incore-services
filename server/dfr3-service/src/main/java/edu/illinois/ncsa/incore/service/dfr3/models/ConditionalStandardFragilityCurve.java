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

import java.util.Map;
import java.util.List;

public class ConditionalStandardFragilityCurve extends FragilityCurve {
    public double[] alpha;
    public double[] beta;
    public AlphaType alphaType;
    public CurveType curveType;
    public Map<Integer, List<String>> rules;


    public ConditionalStandardFragilityCurve() {
        super();
    }

    public ConditionalStandardFragilityCurve(double[] alpha, double[] beta, AlphaType alphaType, CurveType curveType, String label, Map<Integer, List<String>> rules){
        super(label);
        this.alpha = alpha;
        this.beta = beta;
        this.alphaType = alphaType;
        this.curveType = curveType;
        this.rules = rules;
    }

    public double[] getAlpha() {
        return alpha;
    }

    public double[] getBeta() {
        return beta;
    }
}
