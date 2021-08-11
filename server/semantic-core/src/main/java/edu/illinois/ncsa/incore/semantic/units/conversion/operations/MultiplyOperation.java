/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class MultiplyOperation extends ReversibleOperation {
    private final double factor;
    private final double inverseFactor;

    public MultiplyOperation(double factor) {
        this.factor = factor;
        this.inverseFactor = 1.0 / factor;
    }

    @Override
    public double invoke(Number value) {
        return (value.doubleValue() * factor);
    }

    @Override
    public double invokeInverse(Number value) {
        return (value.doubleValue() * inverseFactor);
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new MultiplyOperation(inverseFactor);
    }

    public double getFactor() {
        return factor;
    }
}
