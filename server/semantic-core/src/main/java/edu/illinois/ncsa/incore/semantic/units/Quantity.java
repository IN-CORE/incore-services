/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.conversion.engine.ConversionEngine;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public class Quantity implements Comparable<Quantity> {
    public Number value;
    public Unit unit;

    public Quantity(Number value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static Quantity of(Number value, Unit unit) {
        return new Quantity(value, unit);
    }

    public Number getValue() {
        return this.value;
    }

    public int getIntegerValue() {
        return this.value.intValue();
    }

    public double getDoubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public String toString() {
        return this.value.toString() + " " + this.unit.getPlural();
    }

    public String toUnicodeString() {
        return this.value.toString() + " " + this.unit.getUnicodePlural();
    }

    public Quantity convertTo(Unit convert) {
        return ConversionEngine.convert(this, convert);
    }

    //    public static edu.illinois.ncsa.incore.semantic.units.Quantity parse(String str) {
    //
    //    }

    // TODO
    @Override
    public int compareTo(Quantity compare) {
        /*if(this.unit.equals(compare.unit)) {
            return Number.compare(this.value, compare.value);
        } else if (this.unit.equivalentTo(compare.unit)) {

        } else if (this.unit.dimension.equivalentTo(compare.unit.dimension)) {

        } else {

        }

        // if not unknown
        // if the dimensions are equal
        // if the units are the same
        // if the units are equivalent
        // if the units are not equal/equivalent then convertTo one to the other then compare

        */
        return 0;
    }
}
