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

import dev.morphia.annotations.Embedded;
import java.util.List;

@Embedded
public class DFR3Curve {
    public String description;
    public List<Rule> rules;
    public ReturnType returnType;
    public List<CurveParameter> curveParameters;

    public DFR3Curve() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    public List<CurveParameter> getCurveParameters() {
        return curveParameters;
    }

    public void setCurveParameters(List<CurveParameter> curveParameters) {
        this.curveParameters = curveParameters;
    }

}
