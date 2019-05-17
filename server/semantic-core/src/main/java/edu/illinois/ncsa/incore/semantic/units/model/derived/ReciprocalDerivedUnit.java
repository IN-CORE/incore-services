/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model.derived;

import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public final class ReciprocalDerivedUnit extends PowerDerivedUnit {
    public ReciprocalDerivedUnit(NamedUnit operand) {
        super(operand, -1,
              "reciprocal " + operand.getName(),
              "reciprocal " + operand.getPlural(),
              computeSymbol(operand),
              computeUnicodeSymbol(operand));

        super.dimension = Dimension.reciprocal(operand.getDimension());
        super.unitSystem = operand.getUnitSystem();
    }

    public ReciprocalDerivedUnit(NamedUnit operand, Dimension dimension) {
        super(operand, -1,
              "reciprocal " + operand.getName(),
              "reciprocal " + operand.getPlural(),
              computeSymbol(operand),
              computeUnicodeSymbol(operand));

        super.dimension = dimension;
        super.unitSystem = operand.getUnitSystem();
    }

    private static String computeSymbol(Unit operand) {
        if (operand instanceof DerivedUnit) {
            return "(" + operand.getSymbol() + ")^-1";
        } else {
            return operand.getSymbol() + "^-1";
        }
    }

    private static String computeUnicodeSymbol(Unit operand) {
        if (operand instanceof DerivedUnit) {
            return "(" + operand.getUnicodeSymbol() + ")" + "\u207B\u00B9";
        } else {
            return operand.getUnicodeSymbol() + "\u207B\u00B9";
        }
    }
}
