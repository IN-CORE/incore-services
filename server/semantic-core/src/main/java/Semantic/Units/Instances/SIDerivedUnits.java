package Semantic.Units.Instances;

import Semantic.Units.Model.Derived.DivisionDerivedUnit;
import Semantic.Units.Model.Derived.PowerDerivedUnit;
import Semantic.Units.Model.Derived.ProductDerivedUnit;
import Semantic.Units.Model.Derived.ReciprocalDerivedUnit;
import Semantic.Units.Model.NamedDerivedUnit;

// NOTE: UnitSystem will be inferred as SI
// NOTE: Order of declared units is important due to static initialization rules in java
public class SIDerivedUnits {
    public static final NamedDerivedUnit hertz = new NamedDerivedUnit("hertz", "hertz", "Hz", "Hz",
                                                                      Dimensions.frequency,
                                                                      new ReciprocalDerivedUnit(SIUnits.second));

    public static final NamedDerivedUnit radian = new NamedDerivedUnit("radian", "radians", "rad", "rad",
                                                                       Dimensions.frequency,
                                                                       new DivisionDerivedUnit(SIUnits.metre, SIUnits.metre));

    public static final PowerDerivedUnit squareSecond = new PowerDerivedUnit(SIUnits.second, 2);

    public static final PowerDerivedUnit cubicSecond = new PowerDerivedUnit(SIUnits.second, 3);

    public static final PowerDerivedUnit quarticSecond = new PowerDerivedUnit(SIUnits.second, 4);

    public static final PowerDerivedUnit squareMetre = new PowerDerivedUnit(SIUnits.metre, 2, Dimensions.area);

    public static final PowerDerivedUnit cubicMetre = new PowerDerivedUnit(SIUnits.metre, 3, Dimensions.volume);

    public static final DivisionDerivedUnit metrePerSecond = new DivisionDerivedUnit(SIUnits.metre, SIUnits.second,
                                                                                     Dimensions.velocity);

    public static final DivisionDerivedUnit cubicMetrePerSecond = new DivisionDerivedUnit(cubicMetre, SIUnits.second,
                                                                                          Dimensions.volumetricFlow);

    public static final DivisionDerivedUnit metrePerSecondSquared = new DivisionDerivedUnit(SIUnits.metre, squareSecond,
                                                                                            Dimensions.acceleration);

    public static final DivisionDerivedUnit metrePerSecondCubed = new DivisionDerivedUnit(SIUnits.metre, cubicSecond, Dimensions.jerk);

    public static final DivisionDerivedUnit metrePerQuarticSecond = new DivisionDerivedUnit(SIUnits.metre, quarticSecond,
                                                                                            Dimensions.jounce);

    public static final DivisionDerivedUnit radianPerSecond = new DivisionDerivedUnit(radian, SIUnits.second,
                                                                                      Dimensions.angularVelocity);

    public static final DivisionDerivedUnit radianPerSecondSquared = new DivisionDerivedUnit(radian, squareSecond,
                                                                                             Dimensions.angularAcceleration);

    public static final NamedDerivedUnit steradian = new NamedDerivedUnit("steradian", "steradians", "sr", "sr",
                                                                          Dimensions.solidAngle,
                                                                          new DivisionDerivedUnit(SIDerivedUnits.squareMetre,
                                                                                                  SIDerivedUnits.squareMetre));

    public static final NamedDerivedUnit newton = new NamedDerivedUnit("newton", "newtons", "N", "N",
                                                                       Dimensions.force,
                                                                       new ProductDerivedUnit(SIUnits.kilogram,
                                                                                              SIDerivedUnits.metrePerSecondSquared));

    public static final NamedDerivedUnit pascal = new NamedDerivedUnit("pascal", "pascals", "Pa", "Pa",
                                                                       Dimensions.pressure,
                                                                       new DivisionDerivedUnit(newton, SIDerivedUnits.squareMetre));

    public static final NamedDerivedUnit joule = new NamedDerivedUnit("joule", "joules", "J", "J",
                                                                      Dimensions.energy,
                                                                      new ProductDerivedUnit(newton, SIUnits.metre));

    public static final NamedDerivedUnit watt = new NamedDerivedUnit("watt", "watts", "W", "W",
                                                                     Dimensions.power,
                                                                     new DivisionDerivedUnit(joule, SIUnits.second));

    public static final NamedDerivedUnit coulomb = new NamedDerivedUnit("coulomb", "coulombs", "C", "C",
                                                                        Dimensions.electricCharge,
                                                                        new ProductDerivedUnit(SIUnits.second, SIUnits.ampere));

    public static final NamedDerivedUnit volt = new NamedDerivedUnit("volt", "volts", "V", "V",
                                                                     Dimensions.electricPotential, new DivisionDerivedUnit(joule, coulomb));

    public static final NamedDerivedUnit farad = new NamedDerivedUnit("farad", "fards", "F", "F",
                                                                      Dimensions.electricalCapacitance,
                                                                      new DivisionDerivedUnit(coulomb, volt));

    public static final NamedDerivedUnit ohm = new NamedDerivedUnit("ohm", "ohms", "ohms", "\u03c9",
                                                                    Dimensions.electricalResistance,
                                                                    new DivisionDerivedUnit(volt, SIUnits.ampere));

    public static final NamedDerivedUnit siemens = new NamedDerivedUnit("siemens", "siemens", "S", "S",
                                                                        Dimensions.electricalConductance,
                                                                        new DivisionDerivedUnit(SIUnits.ampere, volt));

    public static final NamedDerivedUnit weber = new NamedDerivedUnit("weber", "webers", "Wb", "Wb",
                                                                      Dimensions.magneticFlux,
                                                                      new DivisionDerivedUnit(joule, SIUnits.ampere));

    public static final NamedDerivedUnit tesla = new NamedDerivedUnit("tesla", "teslas", "T", "T",
                                                                      Dimensions.magneticFluxDensity,
                                                                      new DivisionDerivedUnit(weber, SIDerivedUnits.squareMetre));

    public static final NamedDerivedUnit henry = new NamedDerivedUnit("henry", "henries", "H", "H",
                                                                      Dimensions.inductance,
                                                                      new DivisionDerivedUnit(weber, SIUnits.ampere));

    public static final NamedDerivedUnit lumen = new NamedDerivedUnit("lumen", "lumens", "lm", "lm",
                                                                      Dimensions.luminousFlux,
                                                                      new ProductDerivedUnit(SIUnits.candela, steradian));

    public static final NamedDerivedUnit lux = new NamedDerivedUnit("lux", "lux", "lx", "lx",
                                                                    Dimensions.illuminance,
                                                                    new DivisionDerivedUnit(lumen, SIDerivedUnits.squareMetre));

    public static final NamedDerivedUnit becquerel = new NamedDerivedUnit("becquerel", "becquerel", "Bq", "Bq",
                                                                          Dimensions.radioActivity,
                                                                          new ReciprocalDerivedUnit(SIUnits.second));

    public static final NamedDerivedUnit gray = new NamedDerivedUnit("gray", "grays", "Gy", "Gy",
                                                                     Dimensions.absorbedDose,
                                                                     new DivisionDerivedUnit(joule, SIUnits.kilogram));

    public static final NamedDerivedUnit sievert = new NamedDerivedUnit("sievert", "sieverts", "Sv", "Sv",
                                                                        Dimensions.doseEquivalent,
                                                                        new DivisionDerivedUnit(joule, SIUnits.kilogram));

    public static final NamedDerivedUnit katal = new NamedDerivedUnit("katal", "katals", "kat", "kat",
                                                                      Dimensions.catalyticActivity,
                                                                      new DivisionDerivedUnit(SIUnits.mole, SIUnits.second));

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

    public static final DivisionDerivedUnit molePerCubicMetre = new DivisionDerivedUnit(SIUnits.mole, cubicMetre, Dimensions.molarity);

    public static final DivisionDerivedUnit cubicMetrePerMole = new DivisionDerivedUnit(cubicMetre, SIUnits.mole, Dimensions.molarVolume);

    public static final ProductDerivedUnit jouleSecond = new ProductDerivedUnit(joule, SIUnits.second, Dimensions.action);

    public static final DivisionDerivedUnit joulePerKelvin = new DivisionDerivedUnit(joule, SIUnits.kelvin, Dimensions.entropy);

    //public static final DivisionDerivedUnit joulePerKelvinMole = new DivisionDerivedUnit(SINamedDerived.joule, kelvinMole, Dimensions.molarEntropy);

    //public static final DivisionDerivedUnit joulePerKelvinKilogram = new DivisionDerivedUnit(SINamedDerived.joule, kelvinKilogram, Dimensions.specificEntropy);

    public static final DivisionDerivedUnit joulePerMole = new DivisionDerivedUnit(joule, SIUnits.mole, Dimensions.molarEnergy);

    public static final DivisionDerivedUnit joulePerKilogram = new DivisionDerivedUnit(joule, SIUnits.kilogram, Dimensions.specificEnergy);

    public static final DivisionDerivedUnit joulePerCubicMetre = new DivisionDerivedUnit(joule, cubicMetre, Dimensions.energyDensity);

    public static final DivisionDerivedUnit newtonPerMetre = new DivisionDerivedUnit(newton, SIUnits.metre, Dimensions.stiffness);

    public static final DivisionDerivedUnit wattPerSquareMetre = new DivisionDerivedUnit(watt, squareMetre, Dimensions.irradiance);

    //public static final DivisionDerivedUnit wattPerMetreKelvin = new DivisionDerivedUnit();

    public static final ProductDerivedUnit pascalSecond = new ProductDerivedUnit(pascal, SIUnits.second, Dimensions.dynamicViscosity);

    public static final ReciprocalDerivedUnit reciprocalKelvin = new ReciprocalDerivedUnit(SIUnits.kelvin);

    public static final ReciprocalDerivedUnit reciprocalPascal = new ReciprocalDerivedUnit(pascal, Dimensions.compressibility);

    public static final ReciprocalDerivedUnit reciprocalHenry = new ReciprocalDerivedUnit(henry, Dimensions.magneticReluctance);

    public static final DivisionDerivedUnit weberPerMetre = new DivisionDerivedUnit(weber, SIUnits.metre,
                                                                                    Dimensions.magneticVectorPotential);

    public static final ProductDerivedUnit weberMetre = new ProductDerivedUnit(weber, SIUnits.metre, Dimensions.magneticMoment);

    public static final ProductDerivedUnit teslaMetre = new ProductDerivedUnit(tesla, SIUnits.metre, Dimensions.magneticRigidity);

    public static final DivisionDerivedUnit joulePerSquareMetre = new DivisionDerivedUnit(joule, squareMetre, Dimensions.radiantExposure);

    //        public static final DivisionDerivedUnit cubicMetrePerMoleSecond = new DivisionDerivedUnit(cubicMetre, moleSecond,
    //                                                                                            Dimensions.CatalyticEfficiency);

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

    //        public static final DivisionDerivedUnit wattPerSteradianMetre = new DivisionDerivedUnit(SINamedDerived.watt, steradianMetre,
    //                                                                                          Dimensions.spectralIntensity);
    //
    //        public static final DivisionDerivedUnit wattPerSteradianSquareMetre = new DivisionDerivedUnit(SINamedDerived.watt, steradianMetre,
    //                                                                                                Dimensions.radiance);
    //
    //        public static final DivisionDerivedUnit wattPerSteradianCubicMetre = new DivisionDerivedUnit(SINamedDerived.watt, steradianMetre,
    //                                                                                               Dimensions.spectralRadiance);

    public static final DivisionDerivedUnit wattPerMetre = new DivisionDerivedUnit(watt, SIUnits.metre, Dimensions.spectralPower);
}
