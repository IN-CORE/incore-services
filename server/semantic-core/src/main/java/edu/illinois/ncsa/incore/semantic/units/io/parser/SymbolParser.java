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

/**
 * Utility class for parsing a unit from a name or symbol string
 */
public final class SymbolParser {
    private SymbolParser() {}

    public static Unit parseSymbol(String symbol) throws ParseException {
        Optional<Unit> result = tryParseSymbol(symbol);

        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ParseException("Could not parse unit from symbol: " + symbol, 0);
        }
    }

    // TODO Unit.parse (can be handled by each class?)
    public static Optional<Unit> tryParseSymbol(String symbol) {
        List<Unit> allUnits = Units.All;

        // check if the unit symbol already exists in the existing units
        List<Unit> initialMatches = allUnits.stream()
                                            .filter(unit -> unit.getSymbol().equals(symbol) || unit.getUnicodeSymbol().equals(symbol))
                                            .collect(Collectors.toList());

        if (initialMatches.size() > 0) {
            return Optional.of(initialMatches.get(0));
        }

        int divisionCount = StringUtils.countMatches(symbol, "/");
        if (divisionCount > 1) {
            // TODO don't really need to handle this case. If it exists there should be parentheses
            return Optional.empty();
        }

        // Parse Division Derived Product
        if (divisionCount == 1) {
            String[] splitStr = StringUtils.split(symbol, "/", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> result = parseDivisionUnitBySymbol(leftSide, rightSide);

            return result;
        }

        // Parse Product Derived Unit
        if (symbol.contains(" ") || symbol.contains("\u22C5")) {
            String[] splitStr = StringUtils.split(symbol, " ", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> result = parseProductUnitBySymbol(leftSide, rightSide);

            return result;
        }

        // Parse Power Derived Unit
        if (symbol.contains("^")) {
            String[] splitStr = StringUtils.split(symbol, "^", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> result = parsePowerUnitBySymbol(leftSide, rightSide);

            return result;
        }

        // Parse Power Derived Unit (Unicode)
        int powerIndex = StringRepresentationUtil.indexOfUnicodePower(symbol);
        if (powerIndex != -1) {
            String leftSide = symbol.substring(0, powerIndex);
            String rightSide = symbol.substring(powerIndex);

            String power = StringRepresentationUtil.convertUnicodePowerToPlain(rightSide);

            Optional<Unit> result = parsePowerUnitBySymbol(leftSide, power);

            return result;
        }

        // Check prefixes of length 1
        if (symbol.length() > 1) {
            String prefixSymbol = symbol.substring(0, 1);
            String unitSymbol = symbol.substring(1);

            Optional<Unit> result = parsePrefixUnitBySymbol(prefixSymbol, unitSymbol);

            if (result.isPresent()) {
                return result;
            }
        }

        // Check prefixes of length 2
        if (symbol.length() > 2) {
            String prefixSymbol = symbol.substring(0, 2);
            String unitSymbol = symbol.substring(2);

            Optional<Unit> result = parsePrefixUnitBySymbol(prefixSymbol, unitSymbol);

            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    private static Optional<Unit> parseDivisionUnitBySymbol(String leftSide, String rightSide) {
        Optional<Unit> leftUnit = tryParseSymbol(leftSide);
        Optional<Unit> rightUnit = tryParseSymbol(rightSide);

        if (leftUnit.isPresent() && rightUnit.isPresent()) {
            DivisionDerivedUnit unit = new DivisionDerivedUnit(leftUnit.get(), rightUnit.get());

            return Optional.of(unit);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Unit> parseProductUnitBySymbol(String leftSide, String rightSide) {
        Optional<Unit> leftUnit = tryParseSymbol(leftSide);
        Optional<Unit> rightUnit = tryParseSymbol(rightSide);

        if (leftUnit.isPresent() && rightUnit.isPresent()) {
            ProductDerivedUnit unit = new ProductDerivedUnit(leftUnit.get(), rightUnit.get());

            return Optional.of(unit);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Unit> parsePowerUnitBySymbol(String unitSymbol, String powerString) {
        Optional<Unit> leftUnit = tryParseSymbol(unitSymbol);
        int power = Integer.parseInt(powerString);

        if (leftUnit.isPresent() && leftUnit.get() instanceof NamedUnit) {
            PowerDerivedUnit unit = new PowerDerivedUnit((NamedUnit) leftUnit.get(), power);

            return Optional.of(unit);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Unit> parsePrefixUnitBySymbol(String prefixSymbol, String unitSymbol) {
        Optional<Prefix> prefixResult = Prefixes.tryGetBySymbol(prefixSymbol);

        if (prefixResult.isPresent()) {
            Optional<Unit> unitResult = tryParseSymbol(unitSymbol);
            if (unitResult.isPresent() && unitResult.get() instanceof PrefixableUnit) {
                PrefixedUnit unit = new PrefixedUnit(prefixResult.get(), (PrefixableUnit) unitResult.get());
                return Optional.of(unit);
            }
        }

        return Optional.empty();
    }
}
