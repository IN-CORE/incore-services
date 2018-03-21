
package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.conversion.engine.ConversionEngine;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.io.parser.NameParser;
import edu.illinois.ncsa.incore.semantic.units.io.parser.SymbolParser;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Units {

    // TODO PSI, KSI
    // TODO Aliases
    // TODO Cleanup
    // TODO Resolve Static Errors
    // TODO Unit System and Aliasing

    private Units() {
    }

    public static final List<Unit> All = new ArrayList<>();

    static {
        initializeUnits();

        All.addAll(SIUnits.All);
        All.addAll(SIDerivedUnits.All);
        All.addAll(NonSIUnits.All);
        All.addAll(ImperialUnits.All);
        All.addAll(USCustomaryUnits.All);
        All.addAll(ISOUnits.All);
        All.addAll(CGSUnits.All);
        All.addAll(AstronomyUnits.All);
        All.addAll(TemperatureUnits.All);
    }

    public static void initializeUnits() {
        Prefixes.initialize();
        Dimensions.initialize();
        SIUnits.initialize();
        SIDerivedUnits.initialize();
        NonSIUnits.initialize();
        ImperialUnits.initialize();
        USCustomaryUnits.initialize();
        ISOUnits.initialize();
        CGSUnits.initialize();
        AstronomyUnits.initialize();
        TemperatureUnits.initialize();
    }

    /**
     * Initializes existing unit and dimensions, calling this before
     * making calls to the units api will result in faster performance.
     * Calling this method is optional but is recommended. You should only
     * call this once.
     */
    public static void initialize() {
        initializeUnits();
        ConversionEngine.initialize();
    }

    /**
     * Returns a list of units from the list of existing units from a specified dimension.
     * Example: Get all units with the dimension of area, this would return square metres, square feet, acres, etc.
     */
    public static List<Unit> getUnitsByDimension(Dimension dimension) {
        List<Unit> matches = Units.All.stream()
                                      .filter(unit -> unit.getDimension().equals(dimension))
                                      .collect(Collectors.toList());

        return matches;
    }

    /**
     * Returns a Unit parsed from a symbol string.
     * Example: m/s^2 => metres per second squared
     * Will throw an exception if unable to parse the string.
     */
    public static Unit parseSymbol(String symbol) throws ParseException {
        return SymbolParser.parseSymbol(symbol);
    }

    /**
     * Returns a Unit parsed from a name string.
     * Example: metres per second squared.
     * Will throw an exception if unable to parse the string.
     */
    public static Unit parseName(String name) throws ParseException {
        return NameParser.parseName(name);
    }

    /**
     * Returns a Unit parsed from a symbol string.
     * Example: m/s^2 => metres per second squared
     */
    public static Optional<Unit> tryParseSymbol(String symbol) {
        return SymbolParser.tryParseSymbol(symbol);
    }

    /**
     * Returns a Unit parsed from a name string.
     * Example: metres per second squared.
     */
    public static Optional<Unit> tryParseName(String name) {
        return NameParser.tryParseName(name);
    }

    /**
     * Converts a value from one unit type to another.
     * Will throw an exception if unable to convert the value.
     *
     * @param value Value to convert
     * @param from  Unit to convert from
     * @param to    Unit to convert to
     * @return The converted value
     */
    public static double convert(Number value, Unit from, Unit to) {
        return ConversionEngine.convert(value, from, to);
    }

    /**
     * Converts a value from one unit type to another.
     *
     * @param value Value to convert
     * @param from  Unit to convert from
     * @param to    Unit to convert to
     * @return The converted value
     */
    public static Optional<Double> tryConvert(Number value, Unit from, Unit to) {
        // TODO
        return null;
    }
}
