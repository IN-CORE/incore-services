
package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class TemperatureUnits {
    private TemperatureUnits() {}

    public static void initialize() {

    }

    public static final NamedUnit celsius = new NamedUnit("Degree Celsius", "Degrees Celsius", "degC", "°C", Dimensions.temperature);
    public static final NamedUnit fahrenheit = new NamedUnit("Degree Fahrenheit", "Degrees Fahrenheit", "degF", "°F", Dimensions.temperature);
    public static final NamedUnit rankine = new NamedUnit("Degree Rankine", "Degrees Rankine", "degRa", "°Ra", Dimensions.temperature);
    public static final NamedUnit romer = new NamedUnit("Degree Romer", "Degrees Romer", "degRo", "°Rø", Dimensions.temperature);
    public static final NamedUnit newton = new NamedUnit("Degree Newton", "Degrees Newton", "degN", "°N", Dimensions.temperature);
    public static final NamedUnit delisle = new NamedUnit("Degree Delisle", "Degrees Delisle", "degD", "°D", Dimensions.temperature);
    public static final NamedUnit reaumur = new NamedUnit("Degree Reaumur", "Degrees Reaumur", "degRe", "°Ré", Dimensions.temperature);

    public static final NamedUnit centigrade = celsius;

    public static final List<Unit> All = Arrays.asList(celsius, fahrenheit);
}
