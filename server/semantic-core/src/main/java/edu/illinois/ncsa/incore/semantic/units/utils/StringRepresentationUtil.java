/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StringRepresentationUtil {
    public static final List<String> PowerNames = Arrays.asList("square", "cubic", "quartic", "quintic", "sextic", "heptic", "octic");
    public static final Map<Integer, String> PowerToNames = new HashMap<>();
    public static final Map<String, Integer> NamesToPower = new HashMap<>();

    private StringRepresentationUtil() {
    }

    static {
        PowerToNames.put(2, "square");
        PowerToNames.put(3, "cubic");
        PowerToNames.put(4, "quartic");
        PowerToNames.put(5, "quintic");
        PowerToNames.put(6, "sextic");
        PowerToNames.put(7, "heptic");
        PowerToNames.put(8, "octic");

        NamesToPower.put("square", 2);
        NamesToPower.put("cubic", 3);
        NamesToPower.put("quartic", 4);
        NamesToPower.put("quintic", 5);
        NamesToPower.put("sextic", 6);
        NamesToPower.put("heptic", 7);
        NamesToPower.put("octic", 8);
    }

    /**
     * Converts an integer power to the unicode power representations.
     */
    public static String toUnicodePowerString(int power) {
        String powerStr = Integer.toString(power);

        powerStr = powerStr.replaceAll("\\+", "\u207A");
        powerStr = powerStr.replaceAll("-", "\u207B");
        powerStr = powerStr.replaceAll("0", "\u2070"); // Can be simplified
        powerStr = powerStr.replaceAll("1", "\u00B9"); // Can be simplified
        powerStr = powerStr.replaceAll("2", "\u00B2");
        powerStr = powerStr.replaceAll("3", "\u00B3");
        powerStr = powerStr.replaceAll("4", "\u2074");
        powerStr = powerStr.replaceAll("5", "\u2075");
        powerStr = powerStr.replaceAll("6", "\u2076");
        powerStr = powerStr.replaceAll("7", "\u2077");
        powerStr = powerStr.replaceAll("8", "\u2078");
        powerStr = powerStr.replaceAll("9", "\u2079");

        return powerStr;
    }

    /**
     * Converts a unicode power string to plain number strings.
     * Example:  ³² -> 32
     */
    public static String convertUnicodePowerToPlain(String unicodeStr) {
        String powerStr = unicodeStr;

        powerStr = powerStr.replaceAll("\u207A", "\\+");
        powerStr = powerStr.replaceAll("\u207B", "-");
        powerStr = powerStr.replaceAll("\u2070", "0");
        powerStr = powerStr.replaceAll("\u00B9", "1");
        powerStr = powerStr.replaceAll("\u00B2", "2");
        powerStr = powerStr.replaceAll("\u00B3", "3");
        powerStr = powerStr.replaceAll("\u2074", "4");
        powerStr = powerStr.replaceAll("\u2075", "5");
        powerStr = powerStr.replaceAll("\u2076", "6");
        powerStr = powerStr.replaceAll("\u2077", "7");
        powerStr = powerStr.replaceAll("\u2078", "8");
        powerStr = powerStr.replaceAll("\u2079", "9");

        return powerStr;
    }

    /**
     * Returns true if a unicode power character is found in the provided string
     */
    public static boolean containsUnicodePower(String str) {
        int index = indexOfUnicodePower(str);

        return index != -1;
    }

    /**
     * Returns the index of the first unicode power character found in the provided string.
     * Returns -1 if no match is found.
     */
    public static int indexOfUnicodePower(String str) {
        String[] powers = new String[]{"\u207A", "\u207B", "\u2070", "\u00B9", "\u00B2", "\u00B3",
            "\u2074", "\u2075", "\u2076", "\u2077", "\u2078", "\u2079"};

        int index = StringUtils.indexOfAny(str, powers);

        return index;
    }

    public static String getRaisedPowerName(int power) {
        return PowerToNames.getOrDefault(power, "");
    }

    public static int getPowerNameValue(String name) {
        return NamesToPower.getOrDefault(name, -1);
    }

    /**
     * Returns the raised power name for a given unit name and power
     * e.g. metre, power 3 becomes cubic metre
     * See NIST Guide to the SI Chapter 9.6 for more information
     */
    public static String getRaisedPowerName(String unitName, int power) {

        if (power == 1) {
            return unitName;
        }

        if (power > 8 || power < -8) {
            return unitName + " to the power of " + power;
        }

        String name = "";

        if (Math.abs(power) > 1 && Math.abs(power) <= 8) {
            name = getRaisedPowerName(Math.abs(power)) + " ";
        }

        name = name + unitName;

        if (power < 0 && power >= -8) {
            name = "reciprocal " + name;
        }

        return name;
    }

    // NIST Guide to the SI Chapter 9.6
    // TODO revise
    public static String getRaisedPowerPlural(String pluralName, int power) {
        switch (power) {
            case 1:
                return pluralName;
            case 2:
                return pluralName + " squared";
            case 3:
                return pluralName + " cubed";
            default:
                return pluralName + " to the power " + power;
        }
    }

    public static String convertToResourceName(String name) {
        // replace any character not in a-z
        String resourceName = name.toLowerCase()
            .replace("'", "")
            .replaceAll("\\s+", "_")
            .replaceAll("[^\\w]", "");

        return resourceName;
    }
}
