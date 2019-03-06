/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class OffsetOperation extends ReversibleOperation {
    private double offset;

    public OffsetOperation(double offset) {
        this.offset = offset;
    }

    @Override
    public double invoke(Number value) {
        return value.doubleValue() + offset;
    }

    @Override
    public double invokeInverse(Number value) {
        return value.doubleValue() - offset;
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new OffsetOperation(-1.0 * offset);
    }
}
