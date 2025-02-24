/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.ArrayList;
import java.util.List;

public class MeasurementConcept extends ValueConcept<Number> {
    public Dimension dimension;
    public List<Unit> commonUnits = new ArrayList<>();

    public Dimension getDimension() {
        return dimension;
    }

    public List<Unit> commonUnits() {
        return commonUnits;
    }
}
