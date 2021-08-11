/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion;

import edu.illinois.ncsa.incore.semantic.units.conversion.operations.MultiplyOperation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.Operation;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public class UnitConversion {
    private final Unit convertFrom;
    private final Unit convertTo;
    private final Operation operation;

    public UnitConversion(Unit convertFrom, Unit convertTo, Operation operation) {
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
        this.operation = operation;
    }

    public UnitConversion(Unit convertFrom, Unit convertTo, Number factor) {
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
        this.operation = new MultiplyOperation(factor.doubleValue());
    }

    public Unit getConvertFromUnit() {
        return this.convertFrom;
    }

    public Unit getConvertToUnit() {
        return this.convertTo;
    }

    public Operation getOperation() {
        return this.operation;
    }
}
