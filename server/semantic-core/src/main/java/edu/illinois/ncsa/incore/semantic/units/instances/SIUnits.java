/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class SIUnits {
    private SIUnits() {
    }

    public static void initialize() {
    }

    public static final PrefixableUnit metre = new PrefixableUnit("metre", "metres", "m",
        Dimensions.length, UnitSystem.SI);

    public static final PrefixedUnit centimetre = new PrefixedUnit(Prefixes.centi, metre);

    public static final PrefixableUnit gram = new PrefixableUnit("gram", "grams", "g",
        Dimensions.mass, UnitSystem.SI);

    public static final PrefixedUnit kilogram = new PrefixedUnit(Prefixes.kilo, gram);

    public static final PrefixableUnit second = new PrefixableUnit("second", "seconds", "s",
        Dimensions.time, UnitSystem.SI);

    public static final PrefixableUnit ampere = new PrefixableUnit("ampere", "amperes", "A",
        Dimensions.electricCurrent, UnitSystem.SI);

    public static final PrefixableUnit kelvin = new PrefixableUnit("kelvin", "kelvin", "K",
        Dimensions.temperature, UnitSystem.SI);

    public static final PrefixableUnit mole = new PrefixableUnit("mole", "moles", "mol",
        Dimensions.amountOfSubstance, UnitSystem.SI);

    public static final PrefixableUnit candela = new PrefixableUnit("candela", "candela", "cd",
        Dimensions.luminousIntensity, UnitSystem.SI);

    // Aliases
    public static final PrefixableUnit meter = metre;

    public static final List<Unit> All = Arrays.asList(metre, kilogram, second, ampere, kelvin, mole, candela);
}
