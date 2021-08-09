/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;


import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class NonSIUnits {
    private NonSIUnits() {
    }

    public static void initialize() {
    }

    // Time
    public static final NamedUnit minute = new NamedUnit("minute", "minutes", "min", "min", Dimensions.time);
    public static final NamedUnit hour = new NamedUnit("hour", "hours", "h", "h", Dimensions.time);
    public static final NamedUnit day = new NamedUnit("day", "days", "d", "d", Dimensions.time);

    //
    public static final NamedUnit arcdegree = new NamedUnit("degree", "degrees", "deg", "\u02da", Dimensions.planeAngle);
    public static final NamedUnit arcminute = new NamedUnit("arc minute", "arc minutes", "'", "'", Dimensions.planeAngle);
    public static final NamedUnit arcsecond = new NamedUnit("arc second", "arc seconds", "\"", "\"", Dimensions.planeAngle);

    public static final PrefixableUnit litre = new PrefixableUnit("litre", "litres", "L", "L", Dimensions.volume);
    public static final NamedUnit tonne = new NamedUnit("tonne", "tonnes", "t", "t", Dimensions.mass);
    public static final NamedUnit astronomicalunit = new NamedUnit("astronomical unit", "astronomical units", "au", "au",
        Dimensions.length);

    public static final NamedUnit neper = new NamedUnit("neper", "nepers", "Np", Dimensions.fieldRatio);
    public static final PrefixableUnit bel = new PrefixableUnit("bel", "bels", "B", Dimensions.fieldRatio);
    public static final PrefixedUnit decibel = new PrefixedUnit(Prefixes.deci, bel, "decibel", "decibels", "dB", "dB");

    public static final PrefixableUnit electronvolt = new PrefixableUnit("electronvolt", "electronvolts", "eV", "eV",
        Dimensions.energy,
        Arrays.asList(Prefixes.milli, Prefixes.kilo, Prefixes.mega,
            Prefixes.giga, Prefixes.tera, Prefixes.peta,
            Prefixes.exa));

    // TODO unified atomic mass unit (Alias Unit and a more names)
    public static final NamedUnit dalton = new NamedUnit("dalton", "daltons", "Da", "Da", Dimensions.mass);

    //TODO Natural and Atomic Units
    public static final NamedUnit angstrom = new NamedUnit("angstrom", "\u00e5ngstr\u00f6m", "angstroms", "\u00e5ngstr\u00f6ms", "A",
        "\u212b", Dimensions.length);
    // TODO hectare, dectare, etc..
    public static final PrefixableUnit are = new PrefixableUnit("are", "are", "a", "a", Dimensions.area);
    public static final PrefixedUnit decare = new PrefixedUnit(Prefixes.deci, are, "decare", "decares", "daa", "daa");
    public static final PrefixedUnit hectare = new PrefixedUnit(Prefixes.hecto, are, "hectare", "hectares", "ha", "ha");

    public static final PrefixableUnit barn = new PrefixableUnit("barn", "barns", "b", "b", Dimensions.area,
        Arrays.asList(Prefixes.mega, Prefixes.kilo, Prefixes.milli, Prefixes.micro,
            Prefixes.micro, Prefixes.nano, Prefixes.pico, Prefixes.femto,
            Prefixes.atto, Prefixes.zepto, Prefixes.yocto));

    public static final PrefixableUnit bar = new PrefixableUnit("bar", "bars", "bar", "bar", Dimensions.pressure,
        Arrays.asList(Prefixes.mega, Prefixes.kilo, Prefixes.deci,
            Prefixes.centi, Prefixes.milli));

    public static final NamedUnit atmosphere = new NamedUnit("atmosphere", "atmospheres", "atm", "atm", Dimensions.pressure);

    // dyne per square centimeter
    public static final NamedUnit barye = new NamedUnit("barye", "baryes", "Ba", "Ba", Dimensions.pressure);

    public static final NamedUnit millimetreOfMercury = new NamedUnit("millimetre of mercury", "millimetres of mercury", "mmHg", "mmHg",
        Dimensions.pressure);

    public static final NamedUnit torr = new NamedUnit("torr", "torrs", "Torr", Dimensions.pressure);

    // TODO watt hour

    public static final NamedUnit curie = new NamedUnit("curie", "curies", "Ci", "Ci", Dimensions.radioActivity);

    public static final NamedUnit roentgen = new NamedUnit("roentgen", "r\u00F6entgen", "roetgens", "r\u00F6entgens", "R", "R",
        Dimensions.radiantExposure);

    public static final NamedUnit rad = new NamedUnit("rad", "rads", "rad", "rad", Dimensions.absorbedDose);

    public static final NamedUnit rontgenEquivalentMan = new NamedUnit("rontgen equivalent man", "r\u00F6ntgen equivalent mans",
        "rontgen equivalent mans", "r\u00F6ntgen equivalent mans", "rem",
        "rem", Dimensions.doseEquivalent);

    public static final NamedUnit gForce = new NamedUnit("g-force", "g-force", "g", Dimensions.acceleration);

    public static final List<Unit> All = Arrays.asList(minute, day, hour);
}
