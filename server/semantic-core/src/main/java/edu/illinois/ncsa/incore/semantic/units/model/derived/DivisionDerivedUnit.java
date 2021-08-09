/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model.derived;

import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public final class DivisionDerivedUnit extends OperatorDerivedUnit {
    public DivisionDerivedUnit(Unit numerator, Unit denominator, Dimension dimension) {
        super(numerator.getName() + " per " + denominator.getName(),
            numerator.getPlural() + " per " + denominator.getName(),
            getSymbol(numerator, denominator),
            getUnicodeSymbol(numerator, denominator),
            dimension, numerator, denominator);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public DivisionDerivedUnit(Unit numerator, Unit denominator) {
        this(numerator, denominator, Dimension.divide(numerator.getDimension(), denominator.getDimension()));
    }

    private static String getSymbol(Unit numerator, Unit denominator) {
        if (denominator instanceof OperatorDerivedUnit) {
            return numerator.getSymbol() + "/(" + denominator.getSymbol() + ")";
        } else {
            return numerator.getSymbol() + "/" + denominator.getSymbol();
        }
    }

    private static String getUnicodeSymbol(Unit leftOperand, Unit rightOperand) {
        if (rightOperand instanceof OperatorDerivedUnit) {
            return leftOperand.getUnicodeSymbol() + "/(" + rightOperand.getUnicodeSymbol() + ")";
        } else {
            return leftOperand.getUnicodeSymbol() + "/" + rightOperand.getUnicodeSymbol();
        }
    }

    @Override
    protected Normalization computeCoherentNormalForm() {
        Normalization leftNormalForm = this.leftOperand.getCoherentNormalForm();
        Normalization rightNormalForm = this.rightOperand.getCoherentNormalForm();

        Normalization raisedRight = Normalization.raiseAll(rightNormalForm, -1);

        Normalization normalization = new Normalization(this, leftNormalForm, raisedRight);
        return normalization;
    }

    @Override
    protected Normalization computeBaseNormalForm() {
        Normalization leftNormalForm = this.leftOperand.getBaseNormalForm();
        Normalization rightNormalForm = this.rightOperand.getBaseNormalForm();

        Normalization raisedRight = Normalization.raiseAll(rightNormalForm, -1);

        Normalization normalization = new Normalization(this, leftNormalForm, raisedRight);
        return normalization;
    }
}
