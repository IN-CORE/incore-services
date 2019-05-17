/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to represent dimensionless Physical Quantities, e.g. Radians
 */
public class Dimensionless extends Dimension {
    public Dimensionless(String name) {
        super(name, "1", "1");
    }

    @Override
    protected List<PowerDimension> getPowerDimensions() {
        return new ArrayList<>();
    }

    @Override
    public DerivedDimension getNormalizedDimension() {
        return new DerivedDimension(this.getPowerDimensions());
    }
}
