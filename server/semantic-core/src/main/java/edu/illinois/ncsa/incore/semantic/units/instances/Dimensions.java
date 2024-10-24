/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.dimension.BaseDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.DerivedDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimensionless;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class contains the list of physical quantities which are associated with units.
 */
public final class Dimensions {
    private Dimensions() {
    }

    public static void initialize() {
    }

    public static final Dimensionless unspecified = new Dimensionless("Unspecified");
    public static final Dimensionless Dimensionless = new Dimensionless("Dimensionless");

    //region Base Dimensions
    public static final BaseDimension length = BaseDimension.length;
    public static final BaseDimension L = length;

    public static final BaseDimension mass = BaseDimension.mass;
    public static final BaseDimension M = mass;

    public static final BaseDimension time = BaseDimension.time;
    public static final BaseDimension T = time;

    public static final BaseDimension electricCurrent = BaseDimension.electricCurrent;
    public static final BaseDimension I = electricCurrent;

    public static final BaseDimension temperature = BaseDimension.temperature;
    public static final BaseDimension O = temperature; // theta

    public static final BaseDimension amountOfSubstance = BaseDimension.amountOfSubstance;
    public static final BaseDimension N = amountOfSubstance;

    public static final BaseDimension luminousIntensity = BaseDimension.luminousIntensity;
    public static final BaseDimension J = luminousIntensity;
    //endregion

    //region Derived Dimensions
    public static final DerivedDimension acceleration = new DerivedDimension("Acceleration", L, T, -2);
    public static final DerivedDimension angularAcceleration = new DerivedDimension("Angular acceleration", L, T, -2);
    public static final DerivedDimension angularVelocity = new DerivedDimension("Angular velocity", T, -2);
    public static final DerivedDimension angularMomentum = new DerivedDimension("Angular momentum", T, -1);
    public static final DerivedDimension area = new DerivedDimension("Area", L, 2);
    public static final DerivedDimension areaDensity = new DerivedDimension("Area density", M, L, -2);
    public static final DerivedDimension electricalCapacitance = new DerivedDimension("Capacitance", I, 2, T, 4, M, -1, L, -2);
    public static final DerivedDimension catalyticActivity = new DerivedDimension("Catalytic activity", N, T, -1);
    public static final DerivedDimension catalyticActivityConcentration = new DerivedDimension("Catalytic activity concentration", N, L,
        -3, T, -1);
    public static final DerivedDimension catalyticEfficiency = new DerivedDimension("Catalytic efficiency", L, 3, T, -1, N, -1);

    public static final DerivedDimension chemicalPotential = new DerivedDimension("Chemical potential", M, L, 2, T, -2, N, -1);
    public static final DerivedDimension molarConcentration = new DerivedDimension("Molar Concentration", N, L, -3);
    public static final DerivedDimension crackle = new DerivedDimension("Crackle", L, T, -5);
    public static final DerivedDimension electricCurrentDensity = new DerivedDimension("Current density", I, L, -2);
    public static final DerivedDimension dynamicViscosity = new DerivedDimension("Dynamic Viscosity", M, L, -1, T, -1);
    public static final DerivedDimension kinematicViscosity = new DerivedDimension("Kinematic Viscosity", L, 2, T, -1);
    public static final DerivedDimension electricCharge = new DerivedDimension("Electric Charge", I, T);
    public static final DerivedDimension electricChargeDensity = new DerivedDimension("Electric charge density", I, T, L, -3);
    public static final DerivedDimension electricDisplacement = new DerivedDimension("Electric Displacement", I, T, L, -2);
    public static final DerivedDimension electricFieldStrength = new DerivedDimension("Electric field strength", M, L, T, -3, I, -1);
    public static final DerivedDimension electricalConductance = new DerivedDimension("Electrical conductance", L, -2, M, -1, T, 3, I, 2);
    public static final DerivedDimension electricPotential = new DerivedDimension("Electric potential", L, 2, M, T, -3, I, -1);
    public static final DerivedDimension electricalResistance = new DerivedDimension("Electrical resistance", L, 2, M, T, -3, I, -2);
    public static final DerivedDimension energy = new DerivedDimension("Energy", M, L, 2, T, -2);
    public static final DerivedDimension energyDensity = new DerivedDimension("Energy density", M, L, -1, T, -2);
    public static final DerivedDimension entropy = new DerivedDimension("Entropy", M, L, 2, T, -2, O, -1);
    public static final DerivedDimension force = new DerivedDimension("Force", M, L, T, -2);
    public static final DerivedDimension fuelEfficiency = new DerivedDimension("Fuel efficiency", L, -2);
    public static final DerivedDimension impulse = new DerivedDimension("Impulse", M, L, T, -1);
    public static final DerivedDimension frequency = new DerivedDimension("Frequency", T, -1);
    public static final DerivedDimension halflife = new DerivedDimension("Half-life", T);
    public static final DerivedDimension heat = new DerivedDimension("Heat", M, L, 2, T, -2);
    public static final DerivedDimension heatCapacity = new DerivedDimension("Heat capacity", M, L, 2, T, -2, O, -1);
    public static final DerivedDimension heatFluxDensity = new DerivedDimension("Heat flux density", M, T, -3);

    public static final DerivedDimension impedance = new DerivedDimension("Impedance", M, L, 2, T, -3, I, -2);
    public static final DerivedDimension inductance = new DerivedDimension("Inductance", M, L, 2, T, -2, I, -2);
    public static final DerivedDimension irradiance = new DerivedDimension("Irradiance", M, T, -2);
    public static final DerivedDimension intensity = new DerivedDimension("Intensity", M, T, -3);
    public static final DerivedDimension jerk = new DerivedDimension("Jerk", L, T, -3);
    public static final DerivedDimension jounce = new DerivedDimension("Jounce", L, T, -4);
    public static final DerivedDimension linearDensity = new DerivedDimension("Linear density", M, L, -1);
    public static final DerivedDimension magneticFieldStrength = new DerivedDimension("Magnetic field strength", I, L, -1);
    public static final DerivedDimension magneticFlux = new DerivedDimension("Magnetic flux", M, L, 2, T, -2, I, -1);
    public static final DerivedDimension magneticFluxDensity = new DerivedDimension("Magnetic flux density", M, T, -2, I, -1);
    public static final DerivedDimension magnetization = new DerivedDimension("Magnetization", I, L, -1);
    public static final DerivedDimension density = new DerivedDimension("Density", M, L, -3);
    public static final DerivedDimension meanLifetime = new DerivedDimension("Mean lifetime", T);
    public static final DerivedDimension molarity = new DerivedDimension("Molarity", N, L, -3);
    public static final DerivedDimension molarVolume = new DerivedDimension("Molar volume", L, 3, N, -1);
    public static final DerivedDimension molarEnergy = new DerivedDimension("Molar energy", M, L, 2, T, -2, N, -1);
    public static final DerivedDimension molarEntropy = new DerivedDimension("Molar entropy", M, L, 2, T, -2, O, -1, N, -1);
    public static final DerivedDimension molarHeatCapacity = new DerivedDimension("Molar heat capacity", M, L, 2, T, -2, N, -1);
    public static final DerivedDimension momentOfInertia = new DerivedDimension("Moment of inertia", M, L, 2);
    public static final DerivedDimension momentum = new DerivedDimension("Momentum", M, L, T, -1);
    public static final DerivedDimension permeability = new DerivedDimension("Permeability", M, L, T, -2, I, -2);
    public static final DerivedDimension permittivity = new DerivedDimension("Permittivity", I, 2, M, -1, L, -3, T, 4);
    public static final DerivedDimension power = new DerivedDimension("Power", M, L, 2, T, -3);
    public static final DerivedDimension pressure = new DerivedDimension("Pressure", M, L, -1, T, -2);
    public static final DerivedDimension pop = new DerivedDimension("Pop", L, T, -6);
    public static final DerivedDimension reactionRate = new DerivedDimension("Reaction rate", N, L, -3, T, -1);
    public static final DerivedDimension velocity = new DerivedDimension("Velocity", L, T, -1);
    public static final DerivedDimension specificEnergy = new DerivedDimension("Specific energy", L, 2, T, -2);
    public static final DerivedDimension specificHeatCapacity = new DerivedDimension("Specific heat capacity", L, 2, T, -2, O, -1);
    public static final DerivedDimension specificVolume = new DerivedDimension("Specific volume", L, 3, M, -1);
    public static final DerivedDimension spin = new DerivedDimension("Spin", M, L, 2, T, -1);
    public static final DerivedDimension stress = new DerivedDimension("Stress", M, L, -1, T, -2);
    public static final DerivedDimension stiffness = new DerivedDimension("Stiffness", M, T, -2);
    public static final DerivedDimension surfaceTension = new DerivedDimension("Surface tension", M, T, -2);
    // public static final DerivedDimension thermalResistivity = new DerivedDimension("Thermal resistivity", );
    public static final DerivedDimension thermalConductivity = new DerivedDimension("Thermal conductivity", M, L, T, -3, O, -1);
    public static final DerivedDimension torque = new DerivedDimension("Torque", M, L, 2, T, -2);
    public static final DerivedDimension volume = new DerivedDimension("Volume", L, 3);
    public static final DerivedDimension volumetricFlow = new DerivedDimension("Volumetric Flow", L, 3, T, -1);
    public static final DerivedDimension waveLength = new DerivedDimension("Wavelength", L);
    public static final DerivedDimension waveNumber = new DerivedDimension("Wavenumber", L, -1);
    public static final DerivedDimension weight = new DerivedDimension("Weight", M, L, T, -2);
    public static final DerivedDimension work = new DerivedDimension("Work", M, L, 2, T, -2);
    public static final DerivedDimension yank = new DerivedDimension("Yank", M, L, T, -3);
    public static final DerivedDimension youngsModulus = new DerivedDimension("Young's Modulus", M, L, -1, T, -2);
    public static final DerivedDimension action = new DerivedDimension("Action", M, L, 2, T, -1);
    public static final DerivedDimension compressibility = new DerivedDimension("Compressibility", L, M, -1, T, 2);
    public static final DerivedDimension magneticReluctance = new DerivedDimension("Magnetic reluctance", L, -2, M, -1, T, -2, I, 2);
    public static final DerivedDimension magneticMoment = new DerivedDimension("Magnetic moment", L, 3, M, T, -2, I, -1);
    public static final DerivedDimension magneticVectorPotential = new DerivedDimension("Magnetic vector potential", L, M, T, -2, I, -1);
    public static final DerivedDimension magneticRigidity = new DerivedDimension("Magnetic rigidity", L, M, T, -2, I, -1);
    public static final DerivedDimension magneticSusceptibility = new DerivedDimension("Magnetic susceptibility", L, -1, M, -1, T, 2, I, 2);
    public static final DerivedDimension frequencyDrift = new DerivedDimension("Frequency drift", T, -2);
    public static final DerivedDimension specificAngularMomentum = new DerivedDimension("Specific angular momentum", L, 2, T, -1);

    // Radiometry
    public static final DerivedDimension radiantEnergy = new DerivedDimension("Radiant energy", M, L, 2, T, -2);
    public static final DerivedDimension radiantEnergyDensity = new DerivedDimension("Radiant energy density", M, L, -1, T, -2);
    public static final DerivedDimension radiantFlux = new DerivedDimension("Radiant flux", M, L, 2, T, -3);
    public static final DerivedDimension radiantExposure = new DerivedDimension("Radiant exposure", M, T, -2);
    public static final DerivedDimension doseEquivalent = new DerivedDimension("Dose equivalent", L, 2, T, -2);
    public static final DerivedDimension absorbedDoseRate = new DerivedDimension("Absorbed dose rate", L, 2, T, -3);
    public static final DerivedDimension radioActivity = new DerivedDimension("Radioactivity", T, -1);
    public static final DerivedDimension absorbedDose = new DerivedDimension("Absorbed dose", L, 2, T, -2);
    public static final DerivedDimension radiance = new DerivedDimension("Radiance", M, T, -3);
    public static final DerivedDimension radiantIntensity = new DerivedDimension("Radiant Intensity", M, L, 2, T, -3);

    // Photometry
    public static final DerivedDimension luminousEnergy = new DerivedDimension("Luminous energy", J, T);
    public static final DerivedDimension luminousFlux = new DerivedDimension("Luminous flux", J);
    public static final DerivedDimension luminousEfficacy = new DerivedDimension("Luminous efficacy", J, L, -2, M, -1, T, 3);
    public static final DerivedDimension illuminance = new DerivedDimension("Illuminance", J, L, -2);
    public static final DerivedDimension luminance = new DerivedDimension("Luminance", J, L, -2);
    public static final DerivedDimension luminousExitance = new DerivedDimension("Luminous exitance", J, L, -2);
    public static final DerivedDimension luminousExposure = new DerivedDimension("Luminous exposure", J, T, L, -2);
    public static final DerivedDimension luminousEnergyDensity = new DerivedDimension("Luminous energy density", J, T, L, -3);
    public static final Dimensionless luminousEfficiency = new Dimensionless("Luminous efficiency");

    public static final DerivedDimension magnetomotiveForce = new DerivedDimension("Magnetomotive force", I);
    public static final DerivedDimension spectralPower = new DerivedDimension("Spectral power", L, M, T, -3);
    public static final DerivedDimension spectralRadiance = new DerivedDimension("Spectral radiance", M, L, -1, T, -3);
    public static final DerivedDimension spectralIntensity = new DerivedDimension("Spectral intensity", L, M, T, -3);

    public static final Dimensionless planeAngle = new Dimensionless("Plane angle");
    public static final Dimensionless solidAngle = new Dimensionless("Solid angle");
    public static final Dimensionless indexOfRefraction = new Dimensionless("Index of refraction");
    public static final Dimensionless machNumber = new Dimensionless("Mach Number");
    public static final Dimensionless massFraction = new Dimensionless("Mass fraction");
    public static final Dimensionless refractiveIndex = new Dimensionless("Refractive Index");
    public static final Dimensionless angle = new Dimensionless("Angle");
    public static final Dimensionless strain = new Dimensionless("Strain");

    public static final Dimensionless fieldRatio = new Dimensionless("Field Ratio");

    public static final Dimensionless informationEntropy = new Dimensionless("Information Entropy");
    //endregion

    public static final List<BaseDimension> BaseDimensions = Arrays.asList(length, mass, time, electricCurrent, temperature,
        amountOfSubstance, luminousIntensity);

    public static final List<Dimension> All = Arrays.asList(length, mass, time, electricCurrent, temperature,
        amountOfSubstance, luminousIntensity,
        acceleration, angularAcceleration, angularVelocity, angularMomentum, area,
        areaDensity, electricalCapacitance, catalyticActivity,
        catalyticActivityConcentration, chemicalPotential, molarConcentration, crackle,
        electricCurrentDensity, dynamicViscosity, kinematicViscosity, electricCharge,
        electricChargeDensity, electricDisplacement, electricFieldStrength,
        electricalConductance, electricPotential, electricalResistance, energy,
        energyDensity, entropy, force, fuelEfficiency, impulse, frequency, halflife,
        heat, heatCapacity, heatFluxDensity, impedance, inductance, irradiance,
        intensity, jerk, jounce, linearDensity, magneticFieldStrength, magneticFlux,
        magneticFluxDensity, magnetization, density, meanLifetime, molarity,
        molarVolume, molarEnergy, molarEntropy, molarHeatCapacity, momentOfInertia,
        momentum, permeability, permittivity, power, pressure, pop, reactionRate,
        velocity, specificEnergy, specificHeatCapacity, specificVolume, spin, stress,
        stiffness, surfaceTension, thermalConductivity, torque, volume, volumetricFlow,
        waveLength, waveNumber, weight, work, yank, youngsModulus, action,
        compressibility, magneticReluctance, magneticMoment, magneticVectorPotential,
        magneticRigidity, magneticSusceptibility, frequencyDrift,
        specificAngularMomentum, radiantEnergy, radiantEnergyDensity, radiantFlux,
        radiantExposure, doseEquivalent, absorbedDoseRate, radioActivity, absorbedDose,
        radiance, radiantIntensity, luminousEnergy, luminousFlux, luminousEfficacy,
        illuminance, luminance, luminousExitance, luminousExposure,
        luminousEnergyDensity, luminousEfficiency, magnetomotiveForce, spectralPower,
        spectralRadiance, spectralIntensity, planeAngle, solidAngle, indexOfRefraction,
        machNumber, massFraction, refractiveIndex, angle, strain, fieldRatio,
        informationEntropy);

    /**
     * Returns a dimension from the current list of dimensions by resource name
     */
    public static Optional<Dimension> getByResourceName(String resourceName) {
        return All.stream()
            .filter(dimension -> dimension.getResourceName().equals(resourceName))
            .findFirst();
    }

    /**
     * Returns a dimension from the current list of dimensions by dimension name
     */
    public static Optional<Dimension> getByName(String name) {
        return All.stream()
            .filter(dimension -> dimension.getResourceName().equals(name))
            .findFirst();
    }

    //region Operators
    public static DerivedDimension multiply(Dimension leftOperand, Dimension rightOperand) {
        return Dimension.multiply(leftOperand, rightOperand);
    }

    public static DerivedDimension divide(Dimension leftOperand, Dimension rightOperand) {
        return Dimension.divide(leftOperand, rightOperand);
    }

    public static DerivedDimension reciprocal(Dimension operand) {
        return Dimension.reciprocal(operand);
    }

    public static Dimension power(Dimension operand, int power) {
        return Dimension.power(operand, power);
    }
    //endregion
}
