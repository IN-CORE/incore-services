/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.model.CoherentDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ReciprocalDerivedUnit;

import java.util.Arrays;
import java.util.List;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.*;

// NOTE: UnitSystem will be inferred as SI
// NOTE: Order of declared units is important due to static initialization rules in java
public final class SIDerivedUnits {
    private SIDerivedUnits() {
    }

    public static void initialize() {
    }

    public static final CoherentDerivedUnit hertz = new CoherentDerivedUnit("hertz", "hertz", "Hz", "Hz",
        Dimensions.frequency, UnitSystem.SI, Prefixes.SI,
        new ReciprocalDerivedUnit(SIUnits.second));

    public static final CoherentDerivedUnit radian = new CoherentDerivedUnit("radian", "radians", "rad", "rad",
        Dimensions.angle, UnitSystem.SI, Prefixes.SINegative,
        new DivisionDerivedUnit(SIUnits.metre, SIUnits.metre));

    public static final ReciprocalDerivedUnit reciprocalSecond = new ReciprocalDerivedUnit(SIUnits.second);

    public static final PowerDerivedUnit squareSecond = new PowerDerivedUnit(SIUnits.second, 2);

    public static final PowerDerivedUnit cubicSecond = new PowerDerivedUnit(SIUnits.second, 3);

    public static final PowerDerivedUnit quarticSecond = new PowerDerivedUnit(SIUnits.second, 4);

    public static final PowerDerivedUnit squareMetre = new PowerDerivedUnit(SIUnits.metre, 2, Dimensions.area);

    public static final PowerDerivedUnit cubicMetre = new PowerDerivedUnit(SIUnits.metre, 3, Dimensions.volume);

    public static final DivisionDerivedUnit metrePerSecond = new DivisionDerivedUnit(SIUnits.metre, SIUnits.second, Dimensions.velocity);

    public static final DivisionDerivedUnit cubicMetrePerSecond = new DivisionDerivedUnit(cubicMetre, SIUnits.second,
        Dimensions.volumetricFlow);

    public static final DivisionDerivedUnit metrePerSecondSquared = new DivisionDerivedUnit(SIUnits.metre, squareSecond,
        Dimensions.acceleration);

    public static final DivisionDerivedUnit metrePerSecondCubed = new DivisionDerivedUnit(SIUnits.metre, cubicSecond, Dimensions.jerk);

    public static final DivisionDerivedUnit metrePerQuarticSecond = new DivisionDerivedUnit(SIUnits.metre, quarticSecond,
        Dimensions.jounce);

    public static final DivisionDerivedUnit radianPerSecond = new DivisionDerivedUnit(radian, SIUnits.second, Dimensions.angularVelocity);

    public static final DivisionDerivedUnit radianPerSecondSquared = new DivisionDerivedUnit(radian, squareSecond,
        Dimensions.angularAcceleration);

    //region SI Coherent Derived Units with Special Names and Symbols
    public static final CoherentDerivedUnit steradian = new CoherentDerivedUnit("steradian", "steradians", "sr", "sr",
        Dimensions.solidAngle, UnitSystem.SI, Prefixes.None,
        new DivisionDerivedUnit(SIDerivedUnits.squareMetre, SIDerivedUnits.squareMetre));

    public static final CoherentDerivedUnit newton = new CoherentDerivedUnit("newton", "newtons", "N", "N",
        Dimensions.force, UnitSystem.SI, Prefixes.SI,
        new ProductDerivedUnit(SIUnits.kilogram, SIDerivedUnits.metrePerSecondSquared));

    public static final CoherentDerivedUnit pascal = new CoherentDerivedUnit("pascal", "pascals", "Pa", "Pa",
        Dimensions.pressure, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(newton, SIDerivedUnits.squareMetre));

    public static final CoherentDerivedUnit joule = new CoherentDerivedUnit("joule", "joules", "J", "J",
        Dimensions.energy, UnitSystem.SI, Prefixes.SI,
        new ProductDerivedUnit(newton, SIUnits.metre));

    public static final CoherentDerivedUnit watt = new CoherentDerivedUnit("watt", "watts", "W", "W",
        Dimensions.power, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(joule, SIUnits.second));

    public static final CoherentDerivedUnit coulomb = new CoherentDerivedUnit("coulomb", "coulombs", "C", "C",
        Dimensions.electricCharge, UnitSystem.SI, Prefixes.SI,
        new ProductDerivedUnit(SIUnits.second, SIUnits.ampere));

    public static final CoherentDerivedUnit volt = new CoherentDerivedUnit("volt", "volts", "V", "V",
        Dimensions.electricPotential, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(joule, coulomb));

    public static final CoherentDerivedUnit farad = new CoherentDerivedUnit("farad", "fards", "F", "F",
        Dimensions.electricalCapacitance, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(coulomb, volt));

    public static final CoherentDerivedUnit ohm = new CoherentDerivedUnit("ohm", "ohms", "ohms", "\u03c9",
        Dimensions.electricalResistance, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(volt, SIUnits.ampere));

    public static final CoherentDerivedUnit siemens = new CoherentDerivedUnit("siemens", "siemens", "S", "S",
        Dimensions.electricalConductance,
        UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(SIUnits.ampere, volt));

    public static final CoherentDerivedUnit weber = new CoherentDerivedUnit("weber", "webers", "Wb", "Wb",
        Dimensions.magneticFlux, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(joule, SIUnits.ampere));

    public static final CoherentDerivedUnit tesla = new CoherentDerivedUnit("tesla", "teslas", "T", "T",
        Dimensions.magneticFluxDensity, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(weber, SIDerivedUnits.squareMetre));

    public static final CoherentDerivedUnit henry = new CoherentDerivedUnit("henry", "henries", "H", "H",
        Dimensions.inductance, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(weber, SIUnits.ampere));

    public static final CoherentDerivedUnit lumen = new CoherentDerivedUnit("lumen", "lumens", "lm", "lm",
        Dimensions.luminousFlux, UnitSystem.SI, Prefixes.SI,
        new ProductDerivedUnit(SIUnits.candela, steradian));

    public static final CoherentDerivedUnit lux = new CoherentDerivedUnit("lux", "lux", "lx", "lx",
        Dimensions.illuminance, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(lumen, SIDerivedUnits.squareMetre));

    public static final CoherentDerivedUnit becquerel = new CoherentDerivedUnit("becquerel", "becquerel", "Bq", "Bq",
        Dimensions.radioActivity, UnitSystem.SI, Prefixes.SI,
        new ReciprocalDerivedUnit(SIUnits.second));

    public static final CoherentDerivedUnit gray = new CoherentDerivedUnit("gray", "grays", "Gy", "Gy",
        Dimensions.absorbedDose, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(joule, SIUnits.kilogram));

    public static final CoherentDerivedUnit sievert = new CoherentDerivedUnit("sievert", "sieverts", "Sv", "Sv",
        Dimensions.doseEquivalent, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(joule, SIUnits.kilogram));

    public static final CoherentDerivedUnit katal = new CoherentDerivedUnit("katal", "katals", "kat", "kat",
        Dimensions.catalyticActivity, UnitSystem.SI, Prefixes.SI,
        new DivisionDerivedUnit(mole, SIUnits.second));
    //endregion

    // SI Derived
    public static final ProductDerivedUnit newtonSecond = new ProductDerivedUnit(newton, SIUnits.second, Dimensions.momentum);

    public static final ProductDerivedUnit newtonMetre = new ProductDerivedUnit(newton, SIUnits.metre, Dimensions.torque);

    public static final ProductDerivedUnit newtonMetreSecond = new ProductDerivedUnit(newtonMetre, SIUnits.second,
        Dimensions.angularMomentum);

    public static final DivisionDerivedUnit newtonPerSecond = new DivisionDerivedUnit(newton, SIUnits.second, Dimensions.yank);

    public static final ReciprocalDerivedUnit reciprocalMetre = new ReciprocalDerivedUnit(SIUnits.metre);

    public static final DivisionDerivedUnit kilogramPerSquareMetre = new DivisionDerivedUnit(SIUnits.kilogram, squareMetre,
        Dimensions.areaDensity);

    public static final DivisionDerivedUnit kilogramPerCubicMetre = new DivisionDerivedUnit(SIUnits.kilogram, cubicMetre,
        Dimensions.density);

    public static final DivisionDerivedUnit cubicMetrePerKilogram = new DivisionDerivedUnit(cubicMetre, SIUnits.kilogram,
        Dimensions.specificVolume);

    public static final DivisionDerivedUnit molePerCubicMetre = new DivisionDerivedUnit(mole, cubicMetre, Dimensions.molarity);

    public static final DivisionDerivedUnit cubicMetrePerMole = new DivisionDerivedUnit(cubicMetre, mole, Dimensions.molarVolume);

    public static final ProductDerivedUnit jouleSecond = new ProductDerivedUnit(joule, SIUnits.second, Dimensions.action);

    public static final DivisionDerivedUnit joulePerKelvin = new DivisionDerivedUnit(joule, SIUnits.kelvin, Dimensions.entropy);

    public static final DivisionDerivedUnit joulePerKelvinMole = new DivisionDerivedUnit(joulePerKelvin, mole, Dimensions.molarEntropy);

    public static final DivisionDerivedUnit joulePerKelvinKilogram = new DivisionDerivedUnit(joulePerKelvin, kilogram,
        Dimensions.heatCapacity);

    public static final DivisionDerivedUnit joulePerMole = new DivisionDerivedUnit(joule, mole, Dimensions.molarEnergy);

    public static final DivisionDerivedUnit joulePerKilogram = new DivisionDerivedUnit(joule, SIUnits.kilogram, Dimensions.specificEnergy);

    public static final DivisionDerivedUnit joulePerCubicMetre = new DivisionDerivedUnit(joule, cubicMetre, Dimensions.energyDensity);

    public static final DivisionDerivedUnit newtonPerMetre = new DivisionDerivedUnit(newton, SIUnits.metre, Dimensions.stiffness);

    public static final DivisionDerivedUnit wattPerSquareMetre = new DivisionDerivedUnit(watt, squareMetre, Dimensions.irradiance);

    public static final ProductDerivedUnit pascalSecond = new ProductDerivedUnit(pascal, SIUnits.second, Dimensions.dynamicViscosity);

    public static final ReciprocalDerivedUnit reciprocalKelvin = new ReciprocalDerivedUnit(SIUnits.kelvin);

    public static final ReciprocalDerivedUnit reciprocalPascal = new ReciprocalDerivedUnit(pascal, Dimensions.compressibility);

    public static final ReciprocalDerivedUnit reciprocalHenry = new ReciprocalDerivedUnit(henry, Dimensions.magneticReluctance);

    public static final DivisionDerivedUnit weberPerMetre = new DivisionDerivedUnit(weber, SIUnits.metre,
        Dimensions.magneticVectorPotential);

    public static final ProductDerivedUnit weberMetre = new ProductDerivedUnit(weber, SIUnits.metre, Dimensions.magneticMoment);

    public static final ProductDerivedUnit teslaMetre = new ProductDerivedUnit(tesla, SIUnits.metre, Dimensions.magneticRigidity);

    public static final DivisionDerivedUnit joulePerSquareMetre = new DivisionDerivedUnit(joule, squareMetre, Dimensions.radiantExposure);

    public static final ProductDerivedUnit moleSecond = new ProductDerivedUnit(mole, second);

    public static final DivisionDerivedUnit cubicMetrePerMoleSecond = new DivisionDerivedUnit(cubicMetre, moleSecond,
        Dimensions.catalyticEfficiency);

    public static final ProductDerivedUnit kilogramSquareMetre = new ProductDerivedUnit(SIUnits.kilogram, squareMetre,
        Dimensions.momentOfInertia);

    public static final DivisionDerivedUnit newtonMetreSecondPerKilogram = new DivisionDerivedUnit(newtonMetreSecond, SIUnits.kilogram,
        Dimensions.specificAngularMomentum);

    public static final DivisionDerivedUnit hertzPerSecond = new DivisionDerivedUnit(hertz, SIUnits.second, Dimensions.frequencyDrift);

    public static final DivisionDerivedUnit lumenPerWatt = new DivisionDerivedUnit(lumen, watt, Dimensions.luminousEfficacy);

    public static final ProductDerivedUnit ampereRadian = new ProductDerivedUnit(SIUnits.ampere, radian, Dimensions.magnetomotiveForce);

    public static final DivisionDerivedUnit metrePerHenry = new DivisionDerivedUnit(SIUnits.metre, henry,
        Dimensions.magneticSusceptibility);

    public static final DivisionDerivedUnit wattPerSteradian = new DivisionDerivedUnit(watt, steradian, Dimensions.radiantIntensity);

    public static final DivisionDerivedUnit wattPerSteradianMetre = new DivisionDerivedUnit(wattPerSteradian, metre,
        Dimensions.spectralIntensity);

    public static final DivisionDerivedUnit wattPerSteradianSquareMetre = new DivisionDerivedUnit(wattPerSteradian, squareMetre,
        Dimensions.radiance);

    public static final DivisionDerivedUnit wattPerSteradianCubicMetre = new DivisionDerivedUnit(wattPerSteradian, cubicMetre,
        Dimensions.spectralRadiance);

    public static final DivisionDerivedUnit wattPerMetre = new DivisionDerivedUnit(watt, SIUnits.metre, Dimensions.spectralPower);

    public static final List<Unit> All = Arrays.asList(hertz, radian, reciprocalSecond, squareSecond, cubicSecond, quarticSecond,
        squareMetre, cubicMetre, metrePerSecond, cubicMetrePerSecond, metrePerSecondSquared,
        metrePerSecondCubed, metrePerQuarticSecond, radianPerSecond, radianPerSecondSquared,
        steradian, newton, pascal, joule, watt, coulomb, volt,
        farad, ohm, siemens, weber, tesla, henry, lumen, lux, becquerel, gray, sievert,
        katal, newtonSecond, newtonMetre, newtonMetreSecond, newtonPerSecond,
        reciprocalMetre, kilogramPerSquareMetre, kilogramPerCubicMetre,
        cubicMetrePerKilogram, molePerCubicMetre, cubicMetrePerMole, jouleSecond,
        joulePerKelvin, joulePerKelvinMole, joulePerKelvinKilogram, joulePerMole,
        joulePerKilogram, joulePerCubicMetre, newtonPerMetre, wattPerSquareMetre,
        pascalSecond, reciprocalKelvin, reciprocalPascal, reciprocalHenry, weberPerMetre,
        weberMetre, teslaMetre, joulePerSquareMetre, moleSecond, cubicMetrePerMoleSecond,
        kilogramSquareMetre, newtonMetreSecondPerKilogram, hertzPerSecond, lumenPerWatt,
        ampereRadian, metrePerHenry, wattPerSteradian, wattPerSteradianMetre,
        wattPerSteradianSquareMetre, wattPerSteradianCubicMetre, wattPerMetre);
}
