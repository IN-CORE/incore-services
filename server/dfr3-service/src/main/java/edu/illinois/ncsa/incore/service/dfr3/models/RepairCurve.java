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
import dev.morphia.annotations.Embedded;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "className")
@XmlSeeAlso({StandardRepairCurve.class})
@Embedded
public abstract class RepairCurve {
    public String description;

    public RepairCurve() {

    }

    public RepairCurve(String label) {
        this.description = label;
    }
}

//TODO StepwiseRestorationCurve.class
