package Semantic.Units.Instances;

import Semantic.Units.UnitSystem;
import Semantic.Units.Model.Derived.PowerDerivedUnit;
import Semantic.Units.Model.NamedUnit;

public class USCustomaryUnits {
    // Length - International
    public static final NamedUnit point = new NamedUnit("point", "points", "p", "p",
                                                        Dimensions.length, UnitSystem.USCustomary);

    public static final NamedUnit pica = new NamedUnit("pica", "picas", "P", "P",
                                                       Dimensions.length, UnitSystem.USCustomary);

    // Length - US Survey
    public static final NamedUnit link = new NamedUnit("link", "links", "li", "li",
                                                       Dimensions.length, UnitSystem.USCustomary);

    public static final NamedUnit surveyFoot = new NamedUnit("survey foot", "survey foot", "ft", "ft",
                                                             Dimensions.length, UnitSystem.USCustomary);

    public static final NamedUnit rod = new NamedUnit("rod", "rods", "rd", "rd",
                                                      Dimensions.length, UnitSystem.USCustomary);

    public static final NamedUnit surveyMile = new NamedUnit("survey mile", "survey miles", "mi", "mi",
                                                             Dimensions.length, UnitSystem.USCustomary);

    // Area
    public static final NamedUnit section = new NamedUnit("section", "section", Dimensions.area, UnitSystem.USCustomary);
    public static final NamedUnit surveyTownship = new NamedUnit("survey township", "survey townships", Dimensions.area,
                                                                 UnitSystem.USCustomary);

    public static final PowerDerivedUnit squareFoot = new PowerDerivedUnit(ImperialUnits.foot, 2, Dimensions.area);
    public static final PowerDerivedUnit squareChain = new PowerDerivedUnit(ImperialUnits.chain, 2, Dimensions.area);

    // Volume
    public static final PowerDerivedUnit cubicInch = new PowerDerivedUnit(ImperialUnits.inch, 3, Dimensions.volume);
    public static final PowerDerivedUnit cubicFoot = new PowerDerivedUnit(ImperialUnits.foot, 3, Dimensions.volume);
    public static final PowerDerivedUnit cubicYard = new PowerDerivedUnit(ImperialUnits.yard, 3, Dimensions.volume);

    // Fluid Volume
    public static final NamedUnit minim = new NamedUnit("minim", "minims", "min", "min", Dimensions.volume,
                                                        UnitSystem.USCustomary);

    public static final NamedUnit fluidDram = new NamedUnit("fluid dram", "fluid drams", "fl dr", "fl dr",
                                                            Dimensions.volume, UnitSystem.USCustomary);

    public static final NamedUnit teaspoon = new NamedUnit("teaspoon", "teaspoons", "tsp", "tsp",
                                                           Dimensions.volume, UnitSystem.USCustomary);

    public static final NamedUnit tablespoon = new NamedUnit("tablespoon", "tablespoons", "Tbsp", "Tbsp",
                                                             Dimensions.volume, UnitSystem.USCustomary);

    public static final NamedUnit fluidOunce = new NamedUnit("fluid ounce", "fluid ounces", "fl oz", "fl oz",
                                                             Dimensions.volume, UnitSystem.USCustomary);

    public static final NamedUnit shot = new NamedUnit("shot", "shots", "jig", "jig", Dimensions.volume, UnitSystem.USCustomary);
    public static final NamedUnit gill = new NamedUnit("gill", "gills", "gi", "gi", Dimensions.volume, UnitSystem.USCustomary);
    public static final NamedUnit cup = new NamedUnit("cup", "cups", "cp", "cp", Dimensions.volume, UnitSystem.USCustomary);
    public static final NamedUnit liquidPint = new NamedUnit("pint", "pints", "pt", "pt", Dimensions.volume,
                                                             UnitSystem.USCustomary);
    public static final NamedUnit liquidQuart = new NamedUnit("quart", "quarts", "qt", "qt", Dimensions.volume,
                                                              UnitSystem.USCustomary);
    public static final NamedUnit liquidGallon = new NamedUnit("gallon", "gallons", "gal", "gal", Dimensions.volume,
                                                               UnitSystem.USCustomary);
    public static final NamedUnit liquidBarrel = new NamedUnit("barrel", "barrels", "bbl", "bbl", Dimensions.volume,
                                                               UnitSystem.USCustomary);
    public static final NamedUnit oilBarrel = new NamedUnit("oil barrel", "oil barrels", "bbl", "bll", Dimensions.volume,
                                                            UnitSystem.USCustomary);
    public static final NamedUnit hogshead = new NamedUnit("hogshead", "hogshead", Dimensions.volume, UnitSystem.USCustomary);

    // Dry Volume
    public static final NamedUnit dryPint = new NamedUnit("pint", "pints", "pt", "pt", Dimensions.volume, UnitSystem.USCustomary);
    public static final NamedUnit dryQuart = new NamedUnit("quart", "quarts", "qt", "qt", Dimensions.volume,
                                                           UnitSystem.USCustomary);
    public static final NamedUnit dryGallon = new NamedUnit("gallon", "gallons", "gal", "gal", Dimensions.volume,
                                                            UnitSystem.USCustomary);
    public static final NamedUnit peck = new NamedUnit("peck", "pecks", "pk", "pk", Dimensions.volume, UnitSystem.USCustomary);
    public static final NamedUnit bushel = new NamedUnit("bushel", "bushels", "bu", "bu", Dimensions.volume,
                                                         UnitSystem.USCustomary);
    public static final NamedUnit dryBarrel = new NamedUnit("barrel", "barrels", "bbl", "bbl", Dimensions.volume,
                                                            UnitSystem.USCustomary);

    // Mass
    public static final NamedUnit pennyweight = new NamedUnit("pennyweight", "pennyweights", "dwt", "dwt",
                                                              Dimensions.mass, UnitSystem.USCustomary);

    public static final NamedUnit troyOunce = new NamedUnit("troy ounce", "troy ounces", "oz t", "oz t", Dimensions.mass,
                                                            UnitSystem.USCustomary);
    public static final NamedUnit troyPound = new NamedUnit("troy pound", "troy pounds", "lb t", "lb t", Dimensions.mass,
                                                            UnitSystem.USCustomary);
}
