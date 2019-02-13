/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.model.CoherentDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;

import java.util.Arrays;
import java.util.List;

public final class CGSUnits {
    private CGSUnits() {}

    public static void initialize() {}

    // Aliases
    public static final NamedUnit centimetre = SIUnits.centimetre;
    public static final NamedUnit gram = SIUnits.gram;
    public static final NamedUnit second = SIUnits.second;

    // Derived
    public static final DerivedUnit centimetrePerSecond = new DivisionDerivedUnit(centimetre, second);
    public static final DerivedUnit centimetrePerSecondSquared = new DivisionDerivedUnit(centimetrePerSecond, second);

    // Named Derived
    public static final CoherentDerivedUnit dyne = new CoherentDerivedUnit("dyne", "dynes", "dyn", "dyn",
                                                                           Dimensions.force, UnitSystem.CentimetreGramSecond, Prefixes.None,
                                                                           new ProductDerivedUnit(gram, centimetrePerSecondSquared));

    public static final CoherentDerivedUnit erg = new CoherentDerivedUnit("erg", "ergs", "erg", "erg",
                                                                          Dimensions.energy, UnitSystem.CentimetreGramSecond, Prefixes.None,
                                                                          new ProductDerivedUnit(dyne, centimetre));

    public static final NamedUnit poise = new NamedUnit("poise", "poises", "P", "P",
                                                        Dimensions.dynamicViscosity, UnitSystem.CentimetreGramSecond);

    public static final NamedUnit stokes = new NamedUnit("strokes", "strokes", "St", "St",
                                                         Dimensions.kinematicViscosity, UnitSystem.CentimetreGramSecond);

    public static final NamedUnit stilb = new NamedUnit("stilb", "stilbs", "sb", "sb", Dimensions.luminance,
                                                        UnitSystem.CentimetreGramSecond);

    public static final NamedUnit phot = new NamedUnit("phot", "phots", "ph", "ph", Dimensions.illuminance,
                                                       UnitSystem.CentimetreGramSecond);

    public static final NamedUnit gal = new NamedUnit("gal", "gals", "Gal", "Gal", Dimensions.acceleration,
                                                      UnitSystem.CentimetreGramSecond);

    public static final NamedUnit maxwell = new NamedUnit("maxwell", "maxwells", "Mx", "Mx",
                                                          Dimensions.magneticFlux, UnitSystem.CentimetreGramSecond);

    public static final NamedUnit gauss = new NamedUnit("gauss", "gauss", "G", "G",
                                                        Dimensions.magneticFluxDensity, UnitSystem.CentimetreGramSecond);

    public static final NamedUnit oersted = new NamedUnit("oersted", "\u0153rsted", "oersted", "\u0153rsted", "Oe", "Oe",
                                                          Dimensions.magneticFieldStrength, UnitSystem.CentimetreGramSecond);

    public static final NamedUnit kayser = new NamedUnit("kayser", "kayers", "K", "K", Dimensions.waveNumber,
                                                         UnitSystem.CentimetreGramSecond);

    // Electromagnetic
    // Abampere
    public static final NamedUnit abampere = new NamedUnit("abampere", "abamperes", "abA", Dimensions.electricCurrent,
                                                           UnitSystem.CentimetreGramSecond);

    //    public static final DivisionDerivedUnit abcoulomb = new DivisionDerivedUnit("abcoulomb", "abcoulombs", "abA", Dimensions.electricCurrent,
    //                                                                        UnitSystem.CentimetreGramSecond);


    public static final List<Unit> All = Arrays.asList(centimetre, gram, second, /*erg, dyne,*/ poise, stokes, stilb, phot, gal, maxwell,
                                                       gauss, oersted, kayser);
}
