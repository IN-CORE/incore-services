/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.equivalence;

import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class InequivalenceTests {
    @Test
    public void testInequivalence_Power() {
        assertFalse(metre.equivalentTo(squareMetre));
    }

    @Test
    public void testInequivalence_NamedUnits() {
        assertFalse(joule.equivalentTo(watt.getDerivation()));
    }

    @Test
    public void testInequivalence_Prefix() {
        assertFalse(centimetre.equivalentTo(metre));
        assertFalse(kilogram.equivalentTo(gram));
    }

    @Test
    public void testInequivalence_DerivedPrefix() {
        // assertFalse();
    }

}
