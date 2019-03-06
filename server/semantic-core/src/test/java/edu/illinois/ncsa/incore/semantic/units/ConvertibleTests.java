/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.second;
import static edu.illinois.ncsa.incore.semantic.units.instances.TemperatureUnits.celsius;
import static edu.illinois.ncsa.incore.semantic.units.instances.TemperatureUnits.fahrenheit;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvertibleTests {

    @Test
    public void testIncompatibleConversion() {
        Unit degCPerSecond = new DivisionDerivedUnit(celsius, second);
        Unit degFPerSecond = new DivisionDerivedUnit(fahrenheit, second);

        Quantity quantity = new Quantity(12.0, degCPerSecond);

        assertThrows(IllegalArgumentException.class, () -> {
            quantity.convertTo(degFPerSecond);
        });
    }
}
