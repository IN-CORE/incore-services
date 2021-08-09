/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringRepresentationUtilsTest {
    @Test
    public void testPowerToUnicode_SingleDigit() {
        //act
        String actual = StringRepresentationUtil.toUnicodePowerString(4);

        //assert
        assertEquals("\u2074", actual);
    }

    @Test
    public void testPowerToUnicode_MultipleDigits() {
        //act
        String actual = StringRepresentationUtil.toUnicodePowerString(249);

        //assert
        assertEquals("\u00B2\u2074\u2079", actual);
    }

    @Test
    public void testIndexUnicodePower_Positive() {
        assertEquals(3, StringRepresentationUtil.indexOfUnicodePower("m/s⁷"));
    }

    @Test
    public void testIndexUnicodePower_Negative() {
        assertEquals(-1, StringRepresentationUtil.indexOfUnicodePower("m/s^7"));
    }

    @Test
    public void testContainsUnicodePower_Positive() {
        assertTrue(StringRepresentationUtil.containsUnicodePower("m/s⁷"));
    }

    @Test
    public void testContainsUnicodePower_Negative() {
        assertFalse(StringRepresentationUtil.containsUnicodePower("m/s^7"));
    }

    @Test
    public void testConvertUnicodePowerToPlain_Single() {
        assertEquals(StringRepresentationUtil.convertUnicodePowerToPlain("³"), "3");
    }

    @Test
    public void testConvertUnicodePowerToPlain_Multiple() {
        assertEquals(StringRepresentationUtil.convertUnicodePowerToPlain("³²"), "32");
    }
}
