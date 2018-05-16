
package edu.illinois.ncsa.incore.semantic.units.parser.units;

import edu.illinois.ncsa.incore.semantic.units.instances.SIUnits;
import edu.illinois.ncsa.incore.semantic.units.instances.Units;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ReciprocalDerivedUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.kilogram;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.metre;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.second;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameParserTests {
    @Test
    @DisplayName("metre")
    public void testBaseUnitName() throws ParseException {
        String str = "metre";

        Unit expected = SIUnits.meter;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @Disabled
    @DisplayName("meter")
    public void testBaseUnitName_Alternate() throws ParseException {
        String str = "meter";

        Unit expected = SIUnits.meter;
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("kilometre (existing)")
    public void testPrefixUnit_Existing() throws ParseException {
        String str = "kilometre";

        Unit expected = new PrefixedUnit(kilo, metre);
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("yottametre (non-existing)")
    public void testPrefixUnit_NonExisting() throws ParseException {
        String str = "yottametre";

        Unit expected = new PrefixedUnit(yotta, metre);
        Unit actual = Units.parseName(str);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("square millimetre")
    public void testPrefixedPowerUnit() throws ParseException {
        // arrange
        String str = "square millimetre";
        PrefixedUnit millimetre = new PrefixedUnit(milli, metre);
        Unit expected = new PowerDerivedUnit(millimetre, 2);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("joule per metre")
    public void testDivisionDerived() throws ParseException {
        // arrange
        String str = "joule per metre";
        Unit expected = new DivisionDerivedUnit(joule, metre);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("joule per millimetre")
    public void testDivisionDerivedPrefix() throws ParseException {
        // arrange
        String str = "joule per millimetre";
        PrefixedUnit millimetre = new PrefixedUnit(milli, metre);
        Unit expected = new DivisionDerivedUnit(joule, millimetre);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("joule per square millimetre")
    public void testDivisionDerivedPrefixPower() throws ParseException {
        // arrange
        String str = "joule per square millimetre";
        PrefixedUnit millimetre = new PrefixedUnit(milli, metre);
        PowerDerivedUnit squareMillimetre = new PowerDerivedUnit(millimetre, 2);
        Unit expected = new DivisionDerivedUnit(joule, squareMillimetre);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("cubic joule per millimetre")
    public void testDivisionDerivedPowerPrefix() throws ParseException {
        // arrange
        String str = "cubic joule per millimetre";
        PrefixedUnit millimetre = new PrefixedUnit(milli, metre);
        PowerDerivedUnit cubicJoule = new PowerDerivedUnit(joule, 3);
        Unit expected = new DivisionDerivedUnit(cubicJoule, millimetre);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("reciprocal watt")
    public void testReciprocal() throws ParseException {
        // arrange
        String str = "reciprocal watt";
        Unit expected = new ReciprocalDerivedUnit(watt);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("reciprocal square watt")
    public void testReciprocalPower() throws ParseException {
        // arrange
        String str = "reciprocal square watt";
        Unit expected = new PowerDerivedUnit(watt, -2);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("reciprocal square megawatt")
    public void testReciprocalPowerPrefix() throws ParseException {
        // arrange
        String str = "reciprocal square megawatt";
        PrefixedUnit megawatt = new PrefixedUnit(mega, watt);
        Unit expected = new PowerDerivedUnit(megawatt, -2);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("reciprocal square megawatt second")
    public void testReciprocalComplex() throws ParseException {
        // arrange
        String str = "reciprocal square megawatt second";
        PrefixedUnit megawatt = new PrefixedUnit(mega, watt);
        PowerDerivedUnit reciprocalSquareMegawatt = new PowerDerivedUnit(megawatt, -2);
        Unit expected = new ProductDerivedUnit(reciprocalSquareMegawatt, reciprocalSecond);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("joule second (existing)")
    public void testProduct_Existing() throws ParseException {
        // arrange
        String str = "joule second";
        Unit expected = new ProductDerivedUnit(joule, second);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("joule metre (non-existing)")
    public void testProduct_NonExisting() throws ParseException {
        // arrange
        String str = "joule metre";
        Unit expected = new ProductDerivedUnit(joule, metre);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("square joule second")
    public void testProductPower() throws ParseException {
        // arrange
        String str = "square joule second";
        PowerDerivedUnit squareJoule = new PowerDerivedUnit(joule, 2);
        Unit expected = new ProductDerivedUnit(squareJoule, second);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("square joule second per henry")
    public void testProductPowerDivision() throws ParseException {
        // arrange
        String str = "square joule second per henry";
        PowerDerivedUnit squareJoule = new PowerDerivedUnit(joule, 2);
        ProductDerivedUnit squareJouleSecond = new ProductDerivedUnit(squareJoule, second);
        Unit expected = new DivisionDerivedUnit(squareJouleSecond, henry);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @Disabled
    @DisplayName("metre to the power of 9")
    public void testPowerPositiveHigh() throws ParseException {
        // arrange
        String str = "metre to the power of 9";
        PowerDerivedUnit expected = new PowerDerivedUnit(metre, 9);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @Disabled
    @DisplayName("metre to the power of negative 9")
    public void testPowerNegativeHigh() throws ParseException {
        // arrange
        String str = "metre to the power of negative 9";
        PowerDerivedUnit expected = new PowerDerivedUnit(metre, -9);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("kilogram per kilometre second")
    public void testDivisionProductDenominator() throws ParseException {
        // arrange
        String str = "kilogram per kilometre second";
        PrefixedUnit kilometre = new PrefixedUnit(kilo, metre);
        ProductDerivedUnit kilometreSecond = new ProductDerivedUnit(kilometre, second);
        DivisionDerivedUnit expected = new DivisionDerivedUnit(kilogram, kilometreSecond);

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("kilogram per square kilometre second")
    public void testDivisionPowerProductDenominator() throws ParseException {
        // arrange
        String str = "kilogram per square kilometre second";
        PrefixedUnit kilometre = new PrefixedUnit(kilo, metre);
        PowerDerivedUnit squareKilometre = new PowerDerivedUnit(kilometre, 2);
        ProductDerivedUnit squareKilometreSecond = new ProductDerivedUnit(squareKilometre, second);
        DivisionDerivedUnit expected = new DivisionDerivedUnit(kilogram, squareKilometreSecond);

        Units.initialize();

        // act
        Unit actual = Units.parseName(str);

        // assert
        assertEquals(expected, actual);
    }
}
