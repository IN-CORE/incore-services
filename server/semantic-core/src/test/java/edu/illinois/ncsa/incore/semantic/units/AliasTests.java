/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AliasTests {
    @Test
    public void testBaseDimensionAliases_RefEquals() {
        assertTrue(Dimensions.L == Dimensions.length);
        assertTrue(Dimensions.I == Dimensions.electricCurrent);
        assertTrue(Dimensions.T == Dimensions.time);
        assertTrue(Dimensions.O == Dimensions.temperature);
        assertTrue(Dimensions.N == Dimensions.amountOfSubstance);
        assertTrue(Dimensions.M == Dimensions.mass);
        assertTrue(Dimensions.J == Dimensions.luminousIntensity);
    }

    @Test
    public void testBaseDimensionAliases_Equals() {
        assertTrue(Dimensions.L.equals(Dimensions.length));
        assertTrue(Dimensions.I.equals(Dimensions.electricCurrent));
        assertTrue(Dimensions.T.equals(Dimensions.time));
        assertTrue(Dimensions.O.equals(Dimensions.temperature));
        assertTrue(Dimensions.N.equals(Dimensions.amountOfSubstance));
        assertTrue(Dimensions.M.equals(Dimensions.mass));
        assertTrue(Dimensions.J.equals(Dimensions.luminousIntensity));
    }
}
