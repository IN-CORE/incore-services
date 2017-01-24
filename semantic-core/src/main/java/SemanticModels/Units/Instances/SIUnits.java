package SemanticModels.Units.Instances;

import SemanticModels.Units.SIPrefix;
import SemanticModels.Units.UnitSystem;
import SemanticModels.Units.Model.PrefixableUnit;
import SemanticModels.Units.Model.PrefixedUnit;

public class SIUnits {
    public static final PrefixableUnit metre = new PrefixableUnit("metre", "metres", "m",
                                                                  Dimensions.length, UnitSystem.SI);

    public static final PrefixableUnit gram = new PrefixableUnit("gram", "grams", "g",
                                                                 Dimensions.mass, UnitSystem.SI);

    public static final PrefixedUnit kilogram = new PrefixedUnit(SIPrefix.kilo, gram);

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
}
