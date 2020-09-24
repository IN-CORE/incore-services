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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@Entity("FragilitySet")
//@XmlTransient
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "className")
@XmlSeeAlso({FragilitySetRe.class, FragilitySetLegacy.class})
public abstract class FragilitySet extends DFR3Set {
    protected String demandType;
    protected String demandUnits;
    protected String description;
    protected List<FragilityCurve> fragilityCurves;

    public FragilitySet() {

    }

    public FragilitySet(String demandType, String demandUnits, String description, List<FragilityCurve> fragilityCurves){
        this.demandType = demandType;
        this.demandUnits = demandUnits;
        this.description = description;
        this.fragilityCurves = fragilityCurves;
    }

    public String getDemandType() {
        return demandType;
    }

    public String getDemandUnits() {
        return demandUnits;
    }

    public String getDescription() { return description; }

    public List<FragilityCurve> getFragilityCurves() { return fragilityCurves; }

}
