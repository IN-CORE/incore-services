/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class ScaleOperation extends ReversibleOperation {
    private int scaleFactor;
    private int inverseScaleFactor;

    public ScaleOperation(int scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.inverseScaleFactor = -1 * scaleFactor;
    }

    @Override
    public double invoke(Number value) {
        return (value.doubleValue() * Math.pow(10, scaleFactor));
    }

    @Override
    public double invokeInverse(Number value) {
        return (value.doubleValue() * Math.pow(10, inverseScaleFactor));
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new ScaleOperation(inverseScaleFactor);
    }
}
