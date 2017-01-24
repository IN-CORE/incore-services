package SemanticModels.Units.Instances;

import SemanticModels.Units.Dimension.BaseDimension;
import SemanticModels.Units.Dimension.DerivedDimension;
import SemanticModels.Units.Dimension.Dimensionless;

public final class Dimensions {
    private Dimensions() {
    }

    public static final Dimensionless unspecified = new Dimensionless("Unspecified");

    // Base Dimensions
    public static final BaseDimension length = new BaseDimension("Length", "L");
    public static final BaseDimension L = length;

    public static final BaseDimension mass = new BaseDimension("Mass", "M");
    public static final BaseDimension M = mass;

    public static final BaseDimension time = new BaseDimension("Time", "T");
    public static final BaseDimension T = time;

    public static final BaseDimension electricCurrent = new BaseDimension("Electric current", "I");
    public static final BaseDimension I = electricCurrent;

    public static final BaseDimension temperature = new BaseDimension("Temperature", "\u0398");
    public static final BaseDimension O = temperature; // theta

    public static final BaseDimension amountOfSubstance = new BaseDimension("Amount of substance", "N");
    public static final BaseDimension N = amountOfSubstance;

    public static final BaseDimension luminousIntensity = new BaseDimension("Luminous intensity", "J");
    public static final BaseDimension J = luminousIntensity;

    // Derived Dimensions
    public static final DerivedDimension absorbedDoseRate = new DerivedDimension("Absorbed dose rate", L, 2, T, -3);
    public static final DerivedDimension acceleration = new DerivedDimension("Acceleration", L, 2, T, -2);
    public static final DerivedDimension angularAcceleration = new DerivedDimension("Angular acceleration", L, T, -2);
    public static final DerivedDimension angularVelocity = new DerivedDimension("Angular velocity", T, -2);
    public static final DerivedDimension angularMomentum = new DerivedDimension("Angular momentum", T, -1);
    public static final DerivedDimension area = new DerivedDimension("Area", L, 2);
    public static final DerivedDimension areaDensity = new DerivedDimension("Area density", M, L, -2);
    public static final DerivedDimension electricalCapacitance = new DerivedDimension("Capacitance", I, 2, T, 4, M, -1, L, -2);
    public static final DerivedDimension catalyticActivity = new DerivedDimension("Catalytic activity", N, T, -1);
    public static final DerivedDimension catalyticActivityConcentration = new DerivedDimension("Catalytic activity concentration", N, L, -3,
                                                                                               T, -1);
    public static final DerivedDimension chemicalPotential = new DerivedDimension("Chemical potential", M, L, 2, T, -2, N, -1);
    public static final DerivedDimension molarConcentration = new DerivedDimension("Molar Concentration", N, L, -3);
    public static final DerivedDimension crackle = new DerivedDimension("Crackle", L, T, -5);
    public static final DerivedDimension electricCurrentDensity = new DerivedDimension("Current density", I, L, -2);
    public static final DerivedDimension doseEquivalent = new DerivedDimension("Dose equivalent", L, 2, T, -2);
    public static final DerivedDimension dynamicViscosity = new DerivedDimension("Dynamic Viscosity", M, L, -1, T, -1);
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
    public static final DerivedDimension illuminance = new DerivedDimension("Illuminance", J, L, -2);
    public static final DerivedDimension impedance = new DerivedDimension("Impedance", M, L, 2, T, -3, I, -2);
    public static final DerivedDimension inductance = new DerivedDimension("Inductance", M, L, 2, T, -2, I, -2);
    public static final DerivedDimension irradiance = new DerivedDimension("Irradiance", M, T, -2);
    public static final DerivedDimension intensity = new DerivedDimension("Intensity", M, T, -3);
    public static final DerivedDimension jerk = new DerivedDimension("Jerk", L, T, -3);
    public static final DerivedDimension jounce = new DerivedDimension("Jounce", L, T, -4);
    public static final DerivedDimension linearDensity = new DerivedDimension("Linear density", M, L, -1);
    public static final DerivedDimension luminousFlux = new DerivedDimension("Luminous flux", J);
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
    public static final DerivedDimension radioActivity = new DerivedDimension("Radioactivity", T, -1);
    public static final DerivedDimension absorbedDose = new DerivedDimension("Absorbed dose", L, 2, T, -2);
    public static final DerivedDimension radiance = new DerivedDimension("Radiance", M, T, -3);
    public static final DerivedDimension radiantIntensity = new DerivedDimension("Radiant Intensity", M, L, 2, T, -3);
    public static final DerivedDimension reactionRate = new DerivedDimension("Reaction rate", N, L, -3, T, -1);
    public static final DerivedDimension velocity = new DerivedDimension("Velocity", L, 2, T, -2);
    public static final DerivedDimension specificEnergy = new DerivedDimension("Specific energy", L, 2, T, -2);
    public static final DerivedDimension specificHeatCapacity = new DerivedDimension("Specific heat capacity", L, 2, T, -2, O, -1);
    public static final DerivedDimension specificVolume = new DerivedDimension("Specific volume", L, 3, M, -1);
    public static final DerivedDimension spin = new DerivedDimension("Spin", M, L, 2, T, -1);
    public static final DerivedDimension stress = new DerivedDimension("Stress", M, L, -1, T, -2);
    public static final DerivedDimension stiffness = new DerivedDimension("Stiffness", M, T, -2);
    public static final DerivedDimension surfaceTension = new DerivedDimension("Surface tension", M, T, -2);
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
    public static final DerivedDimension radiantExposure = new DerivedDimension("Radiant exposure", M, T, -2);
    public static final DerivedDimension frequencyDrift = new DerivedDimension("Frequency drift", T, -2);
    public static final DerivedDimension specificAngularMomentum = new DerivedDimension("Specific angular momentum", L, 2, T, -1);
    public static final DerivedDimension luminousEfficacy = new DerivedDimension("Luminous efficacy", J, L, -2, M, -1, T, 3);
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
}
