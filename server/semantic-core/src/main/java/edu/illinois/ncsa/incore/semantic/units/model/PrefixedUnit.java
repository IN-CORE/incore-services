/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.common.IPrefixComparable;

import java.util.Objects;

public class PrefixedUnit extends NamedUnit implements IPrefixComparable {
    public Prefix prefix;
    public PrefixableUnit baseUnit;

    public PrefixedUnit(Prefix prefix, PrefixableUnit unit) {
        super(prefix.getName() + unit.name,
              prefix.getName() + unit.plural,
              prefix.getSymbol() + unit.symbol,
              prefix.getUnicodeSymbol() + unit.unicodeSymbol,
              unit.dimension);

        this.prefix = prefix;
        this.baseUnit = unit;
        this.unitSystem = baseUnit.unitSystem;

        if (!unit.applicablePrefixes.contains(prefix)) {
            throw new IllegalArgumentException(
                    "edu.illinois.ncsa.incore.semantic.units.SIPrefix " + prefix.getName() + " is not an applicable prefix to the unit " + unit.name);
        }
    }

    public PrefixedUnit(Prefix prefix, PrefixableUnit unit, String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol, unit.dimension);

        this.prefix = prefix;
        this.baseUnit = unit;
        this.unitSystem = baseUnit.unitSystem;

        if (!unit.applicablePrefixes.contains(prefix)) {
            throw new IllegalArgumentException(
                    "edu.illinois.ncsa.incore.semantic.units.SIPrefix " + prefix.getName() + " is not an applicable prefix to the unit " + unit.name);
        }
    }

    public Prefix getPrefix() {
        return prefix;
    }

    @Override
    public int getPrefixScale() {
        return prefix.getScale();
    }

    @Override
    public int getPrefixBase() {
        return prefix.getBase();
    }

    @Override
    public PrefixableUnit getBaseUnit() {
        return baseUnit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PrefixedUnit)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        PrefixedUnit unit = (PrefixedUnit) obj;

        return Objects.equals(prefix, unit.prefix) &&
                Objects.equals(baseUnit, unit.baseUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), prefix, baseUnit);
    }
}
