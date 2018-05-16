
package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;

import java.util.Arrays;
import java.util.List;

public final class ImperialUnits {
    private ImperialUnits() {}

    public static void initialize() {}

    // Length
    public static final NamedUnit thou = new NamedUnit("thou", "thou", "th", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit inch = new NamedUnit("inch", "inches", "in", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit foot = new NamedUnit("foot", "foot", "ft", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit yard = new NamedUnit("yard", "yards", "yd", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit chain = new NamedUnit("chain", "chains", "ch", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit furlong = new NamedUnit("furlong", "furlongs", "fur", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit mile = new NamedUnit("mile", "miles", "ml", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit league = new NamedUnit("league", "leagues", "lea", Dimensions.length, UnitSystem.Imperial);

    // Length - Maritime
    public static final NamedUnit fathom = new NamedUnit("fathom", "fathoms", "ftm", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit cable = new NamedUnit("cable", "cables", Dimensions.length, UnitSystem.Imperial);
    public static final NamedUnit nauticalMile = new NamedUnit("nautical mile", "nautical miles", Dimensions.length,
                                                               UnitSystem.Imperial);

    // Area
    public static final PowerDerivedUnit squareFoot = new PowerDerivedUnit(foot, 2, Dimensions.area);
    public static final PowerDerivedUnit squareInch = new PowerDerivedUnit(inch, 2, Dimensions.area);
    public static final PowerDerivedUnit squareLeague = new PowerDerivedUnit(league, 2, Dimensions.area);
    public static final NamedUnit perch = new NamedUnit("perch", "perches", Dimensions.area, UnitSystem.Imperial);
    public static final NamedUnit rood = new NamedUnit("rood", "roods", Dimensions.area, UnitSystem.Imperial);
    public static final NamedUnit acre = new NamedUnit("acre", "acres", Dimensions.area, UnitSystem.Imperial);

    // Volume
    public static final NamedUnit fluidOunce = new NamedUnit("fluid ounce", "fluid ounces", "fl oz", Dimensions.volume,
                                                             UnitSystem.Imperial);
    public static final NamedUnit gill = new NamedUnit("gill", "gills", "gi", Dimensions.volume, UnitSystem.Imperial);
    public static final NamedUnit pint = new NamedUnit("pint", "pints", "pt", Dimensions.volume, UnitSystem.Imperial);
    public static final NamedUnit quart = new NamedUnit("quart", "quarts", "qt", Dimensions.volume, UnitSystem.Imperial);
    public static final NamedUnit gallon = new NamedUnit("gallon", "gallons", "gal", Dimensions.volume, UnitSystem.Imperial);

    // Mass
    public static final NamedUnit grain = new NamedUnit("grain", "grains", "gr", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit drachm = new NamedUnit("drachm", "drachms", "dr", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit ounce = new NamedUnit("ounce", "ounces", "oz", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit pound = new NamedUnit("pound", "pounds", "lb", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit stone = new NamedUnit("stone", "stone", "st", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit quarter = new NamedUnit("quarter", "quarters", "quarter", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit hundredweight = new NamedUnit("hundredweight", "hundredweights", "cwt", Dimensions.mass,
                                                                UnitSystem.Imperial);
    public static final NamedUnit ton = new NamedUnit("ton", "tons", "t", Dimensions.mass, UnitSystem.Imperial);
    public static final NamedUnit slug = new NamedUnit("slug", "slugs", "slug", Dimensions.mass, UnitSystem.Imperial);

    public static final List<Unit> All = Arrays.asList(foot);
}
