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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
@XmlSeeAlso({CustomExpressionFragilityCurve.class, PeriodBuildingFragilityCurve.class,
             StandardFragilityCurve.class, PeriodStandardFragilityCurve.class})
public abstract class FragilityCurve {
    public String description;

    public FragilityCurve() {

    }

    public FragilityCurve(String label) {
        this.description = label;
    }
}
