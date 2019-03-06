/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.types;

public enum WindfieldDemandUnits {
    kt,
    mps,
    kmph;

    public static WindfieldDemandUnits fromString(String unit) {
        String lowerUnit = unit.toLowerCase();
        return valueOf(lowerUnit);
    }

}
