package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ReciprocalDerivedUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.metre;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.second;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluralTests {
    @Test
    @Disabled
    @DisplayName("cubic metres")
    public void testPower() {
        assertEquals("cubic metres", cubicMetre.getPlural());
    }

    @Test
    @Disabled
    @DisplayName("reciprocal seconds")
    public void testReciprocalSingle() {
        assertEquals("reciprocal seconds", reciprocalSecond.getPlural());
    }

    @Test
    @DisplayName("reciprocal pascal seconds")
    public void testReciprocalMultiple() {
        ProductDerivedUnit unit = new ProductDerivedUnit(new ReciprocalDerivedUnit(pascal), reciprocalSecond);
        assertEquals("reciprocal pascal seconds", unit.getPlural());
    }

    @Test
    @Disabled
    @DisplayName("reciprocal square metres")
    public void testReciprocalPowerSingle() {
        PowerDerivedUnit unit = new PowerDerivedUnit(metre, -2);
        assertEquals("reciprocal square metres", unit.getPlural());
    }

    @Test
    @DisplayName("reciprocal pascal square seconds")
    public void testReciprocalPowerMultiple() {
        ReciprocalDerivedUnit reciprocalPascal = new ReciprocalDerivedUnit(pascal);
        PowerDerivedUnit reciprocalSquareSecond = new PowerDerivedUnit(second, -2);
        ProductDerivedUnit unit = new ProductDerivedUnit(reciprocalPascal, reciprocalSquareSecond);

        assertEquals("reciprocal pascal square seconds", unit.getPlural());
    }
}
