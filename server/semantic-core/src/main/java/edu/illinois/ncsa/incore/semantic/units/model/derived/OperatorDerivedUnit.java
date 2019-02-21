/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model.derived;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Objects;

public abstract class OperatorDerivedUnit extends DerivedUnit {
    public Unit leftOperand;
    public Unit rightOperand;

    protected OperatorDerivedUnit(String name, String plural, String symbol, String unicodeSymbol,
                                  Unit leftOperand, Unit rightOperand) {
        super(name, plural, symbol, unicodeSymbol);

        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

        if (leftOperand.getUnitSystem().equals(rightOperand.getUnitSystem())) {
            this.unitSystem = leftOperand.getUnitSystem();
        } else {
            this.unitSystem = UnitSystem.Unspecified;
        }
    }

    protected OperatorDerivedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                                  Unit leftOperand, Unit rightOperand) {
        super(name, plural, symbol, unicodeSymbol, dimension);

        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

        if (leftOperand.getUnitSystem().equals(rightOperand.getUnitSystem())) {
            this.unitSystem = leftOperand.getUnitSystem();
        } else {
            this.unitSystem = UnitSystem.Unspecified;
        }
    }

    @Override
    public Unit getSimplifiedForm() {
        return this.getCoherentNormalForm().toSimpleForm();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperatorDerivedUnit)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        OperatorDerivedUnit unit = (OperatorDerivedUnit) obj;

        return Objects.equals(leftOperand, unit.leftOperand) &&
                Objects.equals(rightOperand, unit.rightOperand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), leftOperand, rightOperand);
    }
}
