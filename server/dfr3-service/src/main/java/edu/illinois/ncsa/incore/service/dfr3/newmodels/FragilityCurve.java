/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.dfr3.newmodels;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlTransient
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "className")
public class FragilityCurve {

    public String description;
    public List<Rule> rules;
    public ReturnType returnType;

    public FragilityCurve() {

    }

    public FragilityCurve(String label) {
        this.description = label;
    }

    public FragilityCurve(String description, List<Rule> rules, ReturnType returnType) {
        this.description = description;
        this.rules = rules;
        this.returnType = returnType;
    }
}
