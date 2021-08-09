/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.parser;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.instances.Prefixes;
import edu.illinois.ncsa.incore.semantic.units.instances.Units;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO clean up
// TODO numerator and denominator strategy
@SuppressWarnings("Duplicates")
public class NameParser {

    public static Unit parseName(String name) throws ParseException {
        Optional<Unit> result = tryParseName(name);

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ParseException("Could not parse unit from name: " + name, 0);
        }
    }

    public static Unit parseResourceName(String name) throws ParseException {
        Optional<Unit> result = tryParseResourceName(name);

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ParseException("Could not parse unit from name: " + name, 0);
        }
    }

    public static Optional<Unit> tryParseResourceName(String resourceName) {
        String name = resourceName.replace("_", " ");
        return tryParseName(name);
    }

    public static Optional<Unit> tryParseName(String name) {
        List<Unit> allUnits = Units.All;

        // check if the unit symbol already exists in the existing units
        List<Unit> initialMatches = allUnits.stream()
            .filter(unit -> unit.getName().equals(name) || unit.getUnicodeName().equals(name))
            .collect(Collectors.toList());

        if (initialMatches.size() > 0) {
            return Optional.of(initialMatches.get(0));
        }

        String reciprocal = "reciprocal";
        if (name.startsWith(reciprocal)) {
            int length = reciprocal.length();

            String reciprocalName = name.substring(length + 1);

            Optional<Unit> result = parseReciprocalUnit(reciprocalName);
            return result;
        }

        int divisionCount = StringUtils.countMatches(name, " per ");
        if (divisionCount > 1) {
            // TODO can have multiple "per"
            return Optional.empty();
        }

        // Parse Division Derived Unit
        if (divisionCount == 1) {
            String[] splitStr = StringUtils.splitByWholeSeparator(name, " per ", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> result = parseDivisionUnit(leftSide, rightSide);

            return result;
        }

        if (name.contains(" ")) {
            int index = 0;
            int productIndex = name.indexOf(" ");
            String left = StringUtils.substring(name, 0, productIndex);
            if (StringRepresentationUtil.PowerNames.contains(left)) {
                index = name.indexOf(" ", productIndex + 1);
            } else {
                index = productIndex;
            }

            if (index == -1) {
                // Parse Power Derived Unit
                String[] split = StringUtils.split(name, " ", 2);
                String powerName = split[0];
                String unitName = split[1];

                Optional<Unit> result = parsePowerUnit(unitName, powerName);

                return result;
            } else {
                // Parse Product Derived Unit
                String leftSide = name.substring(0, index);
                String rightSide = name.substring(index + 1);

                Optional<Unit> result = parseProductUnit(leftSide, rightSide);

                return result;
            }
        }

        // Parse Prefixed Unit
        Optional<Prefix> prefixMatch = Prefixes.All.stream()
            .filter(p -> name.startsWith(p.getName()))
            .findFirst();
        if (prefixMatch.isPresent()) {
            Prefix prefix = prefixMatch.get();
            int index = name.lastIndexOf(prefix.getName()) + prefix.getName().length();
            String unitName = name.substring(index);
            Optional<Unit> unitResult = tryParseName(unitName);

            if (unitResult.isPresent() && unitResult.get() instanceof PrefixableUnit) {
                PrefixedUnit prefixedUnit = new PrefixedUnit(prefix, (PrefixableUnit) unitResult.get());

                return Optional.of(prefixedUnit);
            }
        }

        return Optional.empty();
    }

    private static Optional<Unit> parseReciprocalUnit(String name) {
        if (name.contains(" ")) {
            int index = 0;
            int productIndex = name.indexOf(" ");
            String left = StringUtils.substring(name, 0, productIndex);
            if (StringRepresentationUtil.PowerNames.contains(left)) {
                index = name.indexOf(" ", productIndex + 1);
            } else {
                index = productIndex;
            }

            if (index == -1) {
                // Parse Power Derived Unit
                String[] split = StringUtils.split(name, " ", 2);
                String powerName = split[0];
                String unitName = split[1];

                int power = StringRepresentationUtil.getPowerNameValue(powerName);

                if (power != -1) {
                    Optional<Unit> unitResult = tryParseName(unitName);
                    if (unitResult.isPresent() && unitResult.get() instanceof NamedUnit) {
                        Unit powerUnit = new PowerDerivedUnit((NamedUnit) unitResult.get(), -1 * power);
                        return Optional.of(powerUnit);
                    }
                }
            } else {
                // Parse Product Derived Unit
                String leftSide = name.substring(0, index);
                String rightSide = name.substring(index + 1);

                Optional<Unit> leftUnit = parseReciprocalUnit(leftSide);
                Optional<Unit> rightUnit = parseReciprocalUnit(rightSide);

                if (leftUnit.isPresent() && rightUnit.isPresent()) {
                    ProductDerivedUnit unit = new ProductDerivedUnit(leftUnit.get(), rightUnit.get());

                    return Optional.of(unit);
                } else {
                    return Optional.empty();
                }
            }
        } else {
            // Parse Named Unit (to the power -1)
            Optional<Unit> unitResult = tryParseName(name);

            if (unitResult.isPresent() && unitResult.get() instanceof NamedUnit) {
                Unit powerUnit = new PowerDerivedUnit((NamedUnit) unitResult.get(), -1);
                return Optional.of(powerUnit);
            }
        }

        // Parse Prefixed Unit
        Optional<Prefix> prefixMatch = Prefixes.All.stream()
            .filter(p -> name.startsWith(p.getName()))
            .findFirst();
        if (prefixMatch.isPresent()) {
            Prefix prefix = prefixMatch.get();
            int index = name.lastIndexOf(prefix.getName()) + prefix.getName().length();
            String unitName = name.substring(index);
            Optional<Unit> unitResult = tryParseName(unitName);

            if (unitResult.isPresent() && unitResult.get() instanceof PrefixableUnit) {
                PrefixedUnit prefixedUnit = new PrefixedUnit(prefix, (PrefixableUnit) unitResult.get());

                return Optional.of(prefixedUnit);
            }
        }

        return Optional.empty();
    }

    private static Optional<Unit> parseDivisionUnit(String leftSide, String rightSide) {
        Optional<Unit> leftUnit = tryParseName(leftSide);
        Optional<Unit> rightUnit = tryParseName(rightSide);

        if (leftUnit.isPresent() && rightUnit.isPresent()) {
            DivisionDerivedUnit unit = new DivisionDerivedUnit(leftUnit.get(), rightUnit.get());

            return Optional.of(unit);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Unit> parseProductUnit(String leftSide, String rightSide) {
        Optional<Unit> leftUnit = tryParseName(leftSide);
        Optional<Unit> rightUnit = tryParseName(rightSide);

        if (leftUnit.isPresent() && rightUnit.isPresent()) {
            ProductDerivedUnit unit = new ProductDerivedUnit(leftUnit.get(), rightUnit.get());

            return Optional.of(unit);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Unit> parsePowerUnit(String unitName, String powerName) {
        int power = StringRepresentationUtil.getPowerNameValue(powerName);
        if (power != -1) {
            Optional<Unit> unitResult = tryParseName(unitName);

            if (unitResult.isPresent() && unitResult.get() instanceof NamedUnit) {
                Unit powerUnit = new PowerDerivedUnit((NamedUnit) unitResult.get(), power);

                return Optional.of(powerUnit);
            }
        }

        return Optional.empty();
    }
}
