
package edu.illinois.ncsa.incore.semantic.units.utils;

import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
