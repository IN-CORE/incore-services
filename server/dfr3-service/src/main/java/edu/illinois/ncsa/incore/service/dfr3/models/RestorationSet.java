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

import dev.morphia.annotations.Entity;

import java.util.List;

@Entity("RestorationSet")
public class RestorationSet extends DFR3Set {

    private String timeUnits;
    private List<RestorationCurve> restorationCurves;

    public String getTimeUnits() {
        return timeUnits;
    }

    public List<RestorationCurve> getRestorationCurves() {
        return restorationCurves;
    }
}
