/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class OffsetMultiplyOperation extends ReversibleOperation {
    private double factor;
    private double inverseFactor;
    private double offset;

    public OffsetMultiplyOperation(double offset, double factor) {
        this.factor = factor;
        this.offset = offset;
        this.inverseFactor = 1.0 / factor;
    }

    @Override
    public double invoke(Number value) {
        return ((value.doubleValue()) + offset) * factor;

    }

    @Override
    public double invokeInverse(Number value) {
        return ((value.doubleValue() * inverseFactor) - offset);
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new MultiplyOffsetOperation(inverseFactor, -1.0 * offset);
    }
}
