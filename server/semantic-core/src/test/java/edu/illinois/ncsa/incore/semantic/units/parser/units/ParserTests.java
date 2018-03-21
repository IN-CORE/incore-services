
package edu.illinois.ncsa.incore.semantic.units.parser.units;

import edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits;
import edu.illinois.ncsa.incore.semantic.units.instances.Units;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {


    @Test
    @DisplayName("centimetres per second squared")
    public void testParse_Prefix_Long_Plural() throws ParseException {
        String str = "centimetres per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("centi-metres per second squared")
    public void testParse_DashPrefix_Long_Plural() throws ParseException {
        String str = "centi-metres per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("metre per second squared")
    public void testParse_Long_Singular() throws ParseException {
        String str = "metre per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("centimetre per second squared")
    public void testParse_Prefix_Long_Singular() throws ParseException {
        String str = "centimetre per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }


    @Test
    @DisplayName("centi-metre per second squared")
    public void testParse_DashPrefix_Long_Singular() throws ParseException {
        String str = "centi-metre per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("metres per second squared")
    public void testParse_LongPlural() throws ParseException {
        String str = "metres per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("meters per second squared")
    public void testParse_LongSingularAlt() throws ParseException {
        String str = "meters per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("meters per second squared")
    public void testParse_Long_Plural_Alt() throws ParseException {
        String str = "meters per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("centimeters per second squared")
    public void testParse_Prefix_Long_Plural_Alt() throws ParseException {
        String str = "centimeters per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }


    @Test
    @DisplayName("centi-meters per second squared")
    public void testParse_DashPrefix_Long_Plural_Alt() throws ParseException {
        String str = "centi-meters per second squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("metre_per_second_squared")
    public void testParse_Url() throws ParseException {
        String str = "metre_per_second_squared";

        Unit expected = SIDerivedUnits.metrePerSecondSquared;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }
}
