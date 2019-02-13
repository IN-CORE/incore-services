/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model.derived;

import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;

public final class ProductDerivedUnit extends OperatorDerivedUnit {
    public ProductDerivedUnit(Unit leftOperand, Unit rightOperand) {
        super(computeName(leftOperand, rightOperand),
              computePlural(leftOperand, rightOperand),
              computeSymbol(leftOperand, rightOperand),
              computeUnicodeSymbol(leftOperand, rightOperand),
              leftOperand, rightOperand);

        super.dimension = Dimension.multiply(leftOperand.getDimension(), rightOperand.getDimension());

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public ProductDerivedUnit(Unit leftOperand, Unit rightOperand, Dimension dimension) {
        super(computeName(leftOperand, rightOperand),
              computePlural(leftOperand, rightOperand),
              computeSymbol(leftOperand, rightOperand),
              computeUnicodeSymbol(leftOperand, rightOperand),
              dimension, leftOperand, rightOperand);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    private static String computeName(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof PowerDerivedUnit && rightOperand instanceof PowerDerivedUnit) {
            NamedUnit leftBase = ((PowerDerivedUnit) leftOperand).getOperand();
            NamedUnit rightBase = ((PowerDerivedUnit) rightOperand).getOperand();
            int leftPower = ((PowerDerivedUnit) leftOperand).getPower();
            int rightPower = ((PowerDerivedUnit) rightOperand).getPower();

            if (leftPower < 0 && rightPower < 0) {
                String leftName = leftBase.getName();
                String rightName = rightBase.getName();

                if (leftPower != -1) {
                    leftName = StringRepresentationUtil.getRaisedPowerName(Math.abs(leftPower)) + " " + leftBase.getName();
                }

                if (rightPower != -1) {
                    rightName = StringRepresentationUtil.getRaisedPowerName(Math.abs(rightPower)) + " " + rightBase.getName();
                }

                return "reciprocal " + leftName + " " + rightName;
            }
        }

        return leftOperand.getName() + " " + rightOperand.getName();
    }

    private static String computePlural(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof PowerDerivedUnit && rightOperand instanceof PowerDerivedUnit) {
            NamedUnit leftBase = ((PowerDerivedUnit) leftOperand).getOperand();
            NamedUnit rightBase = ((PowerDerivedUnit) rightOperand).getOperand();
            int leftPower = ((PowerDerivedUnit) leftOperand).getPower();
            int rightPower = ((PowerDerivedUnit) rightOperand).getPower();

            if (leftPower < 0 && rightPower < 0) {
                String leftName = leftBase.getName();
                String rightName = rightBase.getPlural();

                if (leftPower != -1) {
                    leftName = StringRepresentationUtil.getRaisedPowerName(Math.abs(leftPower)) + " " + leftBase.getName();
                }

                if (rightPower != -1) {
                    rightName = StringRepresentationUtil.getRaisedPowerName(Math.abs(rightPower)) + " " + rightBase.getPlural();
                }

                return "reciprocal " + leftName + " " + rightName;
            }
        }

        return leftOperand.getName() + " " + rightOperand.getPlural();
    }

    private static String computeSymbol(Unit leftOperand, Unit rightOperand) {
        return leftOperand.getSymbol() + " " + rightOperand.getSymbol();
    }

    private static String computeUnicodeSymbol(Unit leftOperand, Unit rightOperand) {
        return leftOperand.getUnicodeSymbol() + "\u22C5" + rightOperand.getUnicodeSymbol();
    }

    @Override
    protected Normalization computeCoherentNormalForm() {
        Normalization leftNormalizedForm = this.leftOperand.getCoherentNormalForm();
        Normalization rightNormalizedForm = this.rightOperand.getCoherentNormalForm();

        Normalization normalization = new Normalization(this, leftNormalizedForm, rightNormalizedForm);
        return normalization;
    }

    @Override
    protected Normalization computeBaseNormalForm() {
        Normalization leftNormalizedForm = this.leftOperand.getBaseNormalForm();
        Normalization rightNormalizedForm = this.rightOperand.getBaseNormalForm();

        Normalization normalization = new Normalization(this, leftNormalizedForm, rightNormalizedForm);
        return normalization;
    }
}
