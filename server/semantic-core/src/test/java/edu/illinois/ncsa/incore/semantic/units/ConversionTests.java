/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits;
import edu.illinois.ncsa.incore.semantic.units.instances.SIUnits;
import edu.illinois.ncsa.incore.semantic.units.instances.TemperatureUnits;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.CGSUnits.erg;
import static edu.illinois.ncsa.incore.semantic.units.instances.ISOUnits.bit;
import static edu.illinois.ncsa.incore.semantic.units.instances.ISOUnits.bytes;
import static edu.illinois.ncsa.incore.semantic.units.instances.ImperialUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.TemperatureUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.USCustomaryUnits.squareFoot;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversionTests {
    private final PrefixedUnit millimetre = new PrefixedUnit(milli, SIUnits.metre);
    private final PrefixedUnit kilometre = new PrefixedUnit(kilo, SIUnits.metre);
    private final PrefixedUnit millijoule = new PrefixedUnit(milli, SIDerivedUnits.joule);
    private final PrefixedUnit kilojoule = new PrefixedUnit(kilo, SIDerivedUnits.joule);
    private final PowerDerivedUnit square_millimetre = new PowerDerivedUnit(millimetre, 2);
    private final PowerDerivedUnit square_kilometre = new PowerDerivedUnit(kilometre, 2);

    @Test
    @DisplayName("1,000,000 mm = 1 km")
    public void testPrefixPositiveHighToNegativeLow() {
        Quantity quantity = new Quantity(1000000, millimetre);
        Quantity converted = quantity.convertTo(kilometre);

        assertEquals(1.0, converted.value);
    }

    @Test
    @DisplayName("1,000,000 mJ = 1 kJ")
    public void testPrefixNamedDerivedPositiveHighToNegativeLow() {
        Quantity quantity = new Quantity(1000000, millijoule);
        Quantity converted = quantity.convertTo(kilojoule);

        assertEquals(1.0, converted.value);
    }

    @Test
    @DisplayName("10,000 g = 10 kg")
    public void testPrefixZeroToPositiveHigh() {
        Quantity quantity = new Quantity(10000, gram);
        Quantity converted = quantity.convertTo(SIUnits.kilogram);

        assertEquals(10.0, converted.value);
    }

    @Test
    @DisplayName("1 m^2 = 1,000,000 mm^2")
    public void testPrefixSquareLowToSquareHigh() {
        Quantity quantity = new Quantity(1, squareMetre);
        Quantity converted = quantity.convertTo(square_millimetre);

        assertEquals(1000000.0, converted.value);
    }

    @Test
    @DisplayName("1,000 m/s = 1 km/s")
    public void testPrefixDivisionNumerator() {
        Unit kilometrePerSecond = new DivisionDerivedUnit(kilometre, second);
        Quantity quantity = new Quantity(1000, metrePerSecond);

        Quantity converted = quantity.convertTo(kilometrePerSecond);

        assertEquals(1.0, converted.value);
    }

    @Test
    @DisplayName("0.0001 kg/m^2 = 100,000 g/km^2")
    public void testPrefixDivisionComplex() {
        Unit gramPerSquareKilometer = new DivisionDerivedUnit(gram, square_kilometre);
        Quantity quantity = new Quantity(0.0001, kilogramPerSquareMetre);

        Quantity converted = quantity.convertTo(gramPerSquareKilometer);

        assertEquals(100000.0, converted.value);
    }

    @Test
    @DisplayName("1 ft = 12 in")
    public void testConversion() {
        Quantity quantity = new Quantity(1, foot);

        Quantity converted = quantity.convertTo(inch);

        assertEquals(12.0, converted.value);
    }

    @Test
    @DisplayName("1 ft^2 = 144 in^2")
    public void testConversionWithPower() {
        Quantity quantity = new Quantity(1, squareFoot);

        Quantity converted = quantity.convertTo(squareInch);

        assertEquals(144.0, converted.value);
    }

    @Test
    @DisplayName("100,000 erg = 0.01 J")
    public void testSIVsCGS() {
        Quantity quantity = new Quantity(100000, erg);

        Quantity converted = quantity.convertTo(joule);

        assertEquals(0.01, converted.value);
    }

    @Test
    @DisplayName("21.98 J = 21.98 J")
    public void testSameUnitConversion() {
        Quantity quantity = new Quantity(21.98, joule);
        Quantity converted = quantity.convertTo(joule);

        assertEquals(quantity.value, converted.value);
    }

    @Test
    @DisplayName("14.2 m/s^2 = 14.2 (m/s) * s^-1")
    public void testSameUnitDifferentDerivation() {
        DivisionDerivedUnit mpsDivisionDerived = new DivisionDerivedUnit(metre, squareSecond);
        ProductDerivedUnit mpsProductDerived = new ProductDerivedUnit(metrePerSecond, reciprocalSecond);

        Quantity quantity = new Quantity(14.2, mpsDivisionDerived);
        Quantity converted = quantity.convertTo(mpsProductDerived);

        assertEquals(quantity.value, converted.value);
    }

    @Test
    @DisplayName("14.2 m/s = 14.2 (m * s^-1)")
    public void testDerivedDimensionVsSpecifiedDimension() {
        ProductDerivedUnit mpsProductDerived = new ProductDerivedUnit(metre, reciprocalSecond);

        Quantity quantity = new Quantity(14.2, metrePerSecond);
        Quantity converted = quantity.convertTo(mpsProductDerived);

        assertEquals(quantity.value, converted.value);
    }

    @Test
    @DisplayName("200 K = -73.15 degC")
    public void testTemperature_KtoC() {
        Quantity quantity = new Quantity(200.0, kelvin);
        Quantity converted = quantity.convertTo(celsius);

        assertEquals(-73.15, converted.value.doubleValue(), 0.1);
    }

    @Test
    @DisplayName("-40 degF = -40 degC")
    public void testTemperature_CtoF_Intersection() {
        Quantity quantityC = new Quantity(-40.0, celsius);
        Quantity quantityF = new Quantity(-40.0, fahrenheit);

        assertEquals(quantityC.value, quantityC.convertTo(fahrenheit).value);
        assertEquals(quantityF.value, quantityF.convertTo(celsius).value);
    }

    @Test
    public void testTemperature_C() {
        Quantity quantity = new Quantity(500, celsius);

        assertEquals(773.15, quantity.convertTo(kelvin).getDoubleValue(), 0.1);
        assertEquals(932.00, quantity.convertTo(fahrenheit).getDoubleValue(), 0.1);
        assertEquals(1391.67, quantity.convertTo(rankine).getDoubleValue(), 0.1);
        // assertEquals(-600.00, quantity.convertTo(delisle).getDoubleValue(), 0.1);
        assertEquals(165.00, quantity.convertTo(TemperatureUnits.newton).getDoubleValue(), 0.1);
        assertEquals(400.00, quantity.convertTo(reaumur).getDoubleValue(), 0.1);
        assertEquals(270.00, quantity.convertTo(romer).getDoubleValue(), 0.1);
    }

    @Test
    public void testTemperature_Inference() {
        Quantity quantity = new Quantity(932.00, fahrenheit);

        assertEquals(773.15, quantity.convertTo(kelvin).getDoubleValue(), 0.1);
        assertEquals(1391.67, quantity.convertTo(rankine).getDoubleValue(), 0.1);
        // assertEquals(-600.00, quantity.convertTo(delisle).getDoubleValue(), 0.1);
        assertEquals(165.00, quantity.convertTo(TemperatureUnits.newton).getDoubleValue(), 0.1);
        assertEquals(400.00, quantity.convertTo(reaumur).getDoubleValue(), 0.1);
        assertEquals(270.00, quantity.convertTo(romer).getDoubleValue(), 0.1);
    }

    @Test
    @DisplayName("1 byte = 8 bits")
    public void testConvertBinary() {
        Quantity quantity = new Quantity(1, bytes);

        assertEquals(8, quantity.convertTo(bit).getIntegerValue());
    }

    @Test
    @DisplayName("1 kibibit = 128 bytes")
    public void testConvertBinaryPrefix() {
        Unit kibiBit = new PrefixedUnit(kibi, bit);
        Quantity quantity = new Quantity(1, kibiBit);

        assertEquals(128, quantity.convertTo(bytes).getIntegerValue());
    }
}
