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

import java.util.Objects;

public class PowerDerivedUnit extends DerivedUnit {
    protected NamedUnit operand;
    protected int power;

    protected PowerDerivedUnit(NamedUnit operand, int power, String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol);
        this.operand = operand;
        this.power = power;

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public PowerDerivedUnit(NamedUnit operand, int power) {
        super(StringRepresentationUtil.getRaisedPowerName(operand.getName(), power),
            StringRepresentationUtil.getRaisedPowerPlural(operand.getPlural(), power),
            computeSymbol(operand, power),
            computeUnicodeSymbol(operand, power));

        this.operand = operand;
        this.power = power;
        this.unitSystem = operand.getUnitSystem();

        super.dimension = Dimension.power(operand.getDimension(), power);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public PowerDerivedUnit(NamedUnit operand, int power, Dimension dimension) {
        super(StringRepresentationUtil.getRaisedPowerName(operand.getName(), power),
            StringRepresentationUtil.getRaisedPowerPlural(operand.getPlural(), power),
            computeSymbol(operand, power),
            computeUnicodeSymbol(operand, power),
            dimension);

        this.operand = operand;
        this.power = power;
        this.unitSystem = operand.getUnitSystem();

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    private static String computeSymbol(NamedUnit operand, int power) {
        // x^1 => x
        if (power == 1) {
            return operand.getSymbol();
        } else {
            return operand.getSymbol() + "^" + power;
        }
    }

    private static String computeUnicodeSymbol(NamedUnit operand, int power) {
        // x^1 => x
        if (power == 1) {
            return operand.getSymbol();
        } else {
            return operand.getUnicodeSymbol() + StringRepresentationUtil.toUnicodePowerString(power);
        }
    }

    @Override
    public Normalization computeCoherentNormalForm() {
        Normalization normalForm = new Normalization(this);
        return normalForm;
    }

    @Override
    public Normalization computeBaseNormalForm() {
        Normalization normalForm = new Normalization(this);
        return normalForm;
    }

    public int getPower() {
        return power;
    }

    public NamedUnit getOperand() {
        return operand;
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

        if (!(obj instanceof PowerDerivedUnit)) {
            return false;
        }

        PowerDerivedUnit unit = (PowerDerivedUnit) obj;
        return power == unit.power &&
            Objects.equals(operand, unit.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operand, power);
    }
}
