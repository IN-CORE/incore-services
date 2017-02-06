package Semantic.Units;

import Semantic.Units.Model.Derived.PowerDerivedUnit;
import Semantic.Units.Model.Derived.ProductDerivedUnit;
import Semantic.Units.Model.Normalization;
import org.junit.jupiter.api.Test;

import static Semantic.Units.Instances.SIUnits.metre;
import static Semantic.Units.Instances.SIUnits.second;
import static Semantic.Units.Instances.SIDerivedUnits.cubicMetre;
import static Semantic.Units.Instances.SIDerivedUnits.squareMetre;
import static Semantic.Units.Instances.SIDerivedUnits.joule;
import static Semantic.Units.Instances.SIDerivedUnits.watt;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EquivalenceTests {
    @Test
    public void testProductEquivalence() {
        // Arrange
        ProductDerivedUnit cubicMetreSUT = new ProductDerivedUnit(squareMetre, metre);

        boolean isEquivalent = cubicMetre.equivalentTo(cubicMetreSUT);
        boolean isEquivalentReversed = cubicMetreSUT.equivalentTo(cubicMetre);

        assertTrue(isEquivalent);
        assertTrue(isEquivalentReversed);
    }

    @Test
    public void testPowerOneEquivalence() {
        PowerDerivedUnit metreSUT = new PowerDerivedUnit(metre, 1);

        boolean isEquivalent = metreSUT.equivalentTo(metre);
        boolean isEquivalentReversed = metre.equivalentTo(metreSUT);

        assertTrue(isEquivalent);
        assertTrue(isEquivalentReversed);
    }

    @Test
    public void testInequivalence() {
        assertFalse(metre.equivalentTo(squareMetre));
    }

    @Test
    public void testNamedEquivalence() {
        // J = (kg m^2) / s^2
        assertTrue(joule.equivalentTo(joule.getDerivation()));
    }

    @Test
    public void testNamedInequivalence() {
        assertFalse(joule.equivalentTo(watt.getDerivation()));
    }

    @Test
    public void testNamedEquivalenceProduct() {
        ProductDerivedUnit Ws = new ProductDerivedUnit(watt, second);

        assertTrue(Ws.equivalentTo(joule));
    }

    @Test
    public void testNamedDerivedRaisedEquivalence() {
        PowerDerivedUnit J2 = new PowerDerivedUnit(joule, 2);
        Normalization j2Norm = J2.getNormalForm();
        Normalization j2BaseNorm = J2.getBaseNormalForm();

        int i = 0;
    }

    @Test
    public void testNamedDerivedRaisedTwiceEquivalence() {
        PowerDerivedUnit J2 = new PowerDerivedUnit(joule, 2);
        PowerDerivedUnit J4 = new PowerDerivedUnit(J2, 2);
        Normalization j4Norm = J4.getNormalForm();
        Normalization j4BaseNorm = J4.getBaseNormalForm();

        int i = 0;
    }

    @Test
    public void testDerivedRaisedEquivalence() {
        PowerDerivedUnit quarticMetreDerived = new PowerDerivedUnit(squareMetre, 2);
        PowerDerivedUnit quarticMetre = new PowerDerivedUnit(metre, 4);

        assertTrue(quarticMetreDerived.equivalentTo(quarticMetre));
    }
}
