/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.equivalence;

import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.metre;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.second;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EquivalenceTests {
    @Test
    public void testEquivalence_SIVsCGS() {

    }

    @Test
    public void testEquivalence_CancelledUnits() {
        ProductDerivedUnit metreDerived = new ProductDerivedUnit(metrePerSecond, second);

        assertTrue(metre.equivalentTo(metreDerived));
    }

    @Test
    public void testEquivalence_DivisionVsProduct() {
        // metre per second squared
        DivisionDerivedUnit mpsDivisionDerived = new DivisionDerivedUnit(metre, squareSecond);
        ProductDerivedUnit mpsProductDerived = new ProductDerivedUnit(metrePerSecond, reciprocalSecond);

        assertTrue(mpsDivisionDerived.equivalentTo(mpsProductDerived));
    }

    @Test
    public void testEquivalence_ProductVsPower() {
        // Arrange
        ProductDerivedUnit cubicMetreSUT = new ProductDerivedUnit(squareMetre, metre);

        boolean isEquivalent = cubicMetre.equivalentTo(cubicMetreSUT);
        boolean isEquivalentReversed = cubicMetreSUT.equivalentTo(cubicMetre);

        assertTrue(isEquivalent);
        assertTrue(isEquivalentReversed);
    }

    @Test
    public void testEquivalence_RaisedToPowerOfOne() {
        PowerDerivedUnit metreSUT = new PowerDerivedUnit(metre, 1);

        boolean isEquivalent = metreSUT.equivalentTo(metre);
        boolean isEquivalentReversed = metre.equivalentTo(metreSUT);

        assertTrue(isEquivalent);
        assertTrue(isEquivalentReversed);
    }

    @Test
    public void testEquivalence_NamedVsDerivation() {
        // J = (kg m^2) / s^2
        assertTrue(joule.equivalentTo(joule.getDerivation()));
    }


    @Test
    public void testEquivalence_NamedVsProductDerived() {
        ProductDerivedUnit Ws = new ProductDerivedUnit(watt, second);

        assertTrue(Ws.equivalentTo(joule));
    }

    @Test
    public void testEquivalence_NamedDerivedVsRaised() {
        PowerDerivedUnit J2 = new PowerDerivedUnit(joule, 2);
        Normalization j2Norm = J2.getBaseNormalForm();
        Normalization j2BaseNorm = J2.getBaseNormalForm();

        // assert
        // TODO validate Normalized forms are equivalent
    }
}
