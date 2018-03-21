
package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class AstronomyUnits {
    public static void initialize() {}

    public static final NamedUnit parsec = new PrefixableUnit("parsec", "parsecs", "pc", "pc", Dimensions.length,
                                                              Arrays.asList(Prefixes.giga, Prefixes.mega));

    public static final NamedUnit solarMass = new PrefixableUnit("solar mass", "solar masses", "Mo", "Mo", Dimensions.mass);

    public static final List<Unit> All = Arrays.asList(parsec, solarMass);

}
