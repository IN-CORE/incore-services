package SemanticModels.Units;

import SemanticModels.Units.Instances.SIDerivedUnits;
import SemanticModels.Units.Instances.SIUnits;
import SemanticModels.Units.Model.Derived.DivisionDerivedUnit;
import SemanticModels.Units.Model.Derived.PowerDerivedUnit;
import SemanticModels.Units.Model.PrefixedUnit;
import SemanticModels.Units.Model.Unit;
import org.junit.jupiter.api.Test;

import static SemanticModels.Units.Instances.ImperialUnits.*;
import static SemanticModels.Units.Instances.SIDerivedUnits.*;
import static SemanticModels.Units.Instances.SIUnits.gram;
import static SemanticModels.Units.Instances.SIUnits.second;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversionTests {
    private Unit millimetre = new PrefixedUnit(SIPrefix.milli, SIUnits.metre);
    private Unit kilometre = new PrefixedUnit(SIPrefix.kilo, SIUnits.metre);
    private Unit millijoule = new PrefixedUnit(SIPrefix.milli, SIDerivedUnits.joule);
    private Unit kilojoule = new PrefixedUnit(SIPrefix.kilo, SIDerivedUnits.joule);
    private Unit square_millimetre = new PowerDerivedUnit(millimetre, 2);
    private Unit square_kilometre = new PowerDerivedUnit(kilometre, 2);

    @Test
    public void testPrefixPositiveHighToNegativeLow() {
        Quantity quantity = new Quantity(1000000, millimetre);
        Quantity converted = quantity.convertTo(kilometre);

        assertEquals(1.0, converted.value);
    }

    @Test
    public void testPrefixNamedDerivedPositiveHighToNegativeLow() {
        Quantity quantity = new Quantity(1000000, millijoule);
        Quantity converted = quantity.convertTo(kilojoule);

        assertEquals(1.0, converted.value);
    }

    @Test
    public void testPrefixZeroToPositiveHigh() {
        Quantity quantity = new Quantity(10000, SIUnits.gram);
        Quantity converted = quantity.convertTo(SIUnits.kilogram);

        assertEquals(10.0, converted.value);
    }

    @Test
    public void testPrefixSquareLowToSquareHigh() {
        Quantity quantity = new Quantity(1, squareMetre);
        Quantity converted = quantity.convertTo(square_millimetre);

        assertEquals(1000000.0, converted.value);
    }

    @Test
    public void testPrefixDivisionNumerator() {
        Unit kilometrePerSecond = new DivisionDerivedUnit(kilometre, second);
        Quantity quantity = new Quantity(1000, metrePerSecond);

        Quantity converted = quantity.convertTo(kilometrePerSecond);

        assertEquals(1.0, converted.value);
    }

    @Test
    public void testPrefixDivisionComplex() {
        Unit gramPerSquareKilometer = new DivisionDerivedUnit(gram, square_kilometre);
        Quantity quantity = new Quantity(0.0001, kilogramPerSquareMetre);

        Quantity converted = quantity.convertTo(gramPerSquareKilometer);

        assertEquals(100000.0, converted.value);
    }

    @Test
    public void testConversion() {
        Quantity quantity = new Quantity(1, foot);

        Quantity converted = quantity.convertTo(inch);

        assertEquals(12.0, converted.value);
    }

    @Test
    public void testConversionWithPower() {
        Quantity quantity = new Quantity(1, squareFoot);

        Quantity converted = quantity.convertTo(squareInch);

        assertEquals(144.0, converted.value);
    }
}
