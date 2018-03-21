
package edu.illinois.ncsa.incore.semantic.units.io.parser;

import edu.illinois.ncsa.incore.semantic.units.instances.Units;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for parsing a unit from a name or symbol string
 */
public final class UnitParser {
    private UnitParser() {}

    public static Optional<Unit> parseName(String unitStr) {
        List<Unit> allUnits = Units.All;

        List<Unit> initialMatches = allUnits.stream()
                                            .filter(unit -> unit.getSymbol().equals(unitStr))
                                            .collect(Collectors.toList());

        if (initialMatches.size() > 0) {
            return Optional.of(initialMatches.get(0));
        } else {

            return Optional.empty();
        }
    }

    // TODO Unit.parse (can be handled by each class?)
    public static Optional<Unit> parseSymbol(String unitStr) {
        List<Unit> allUnits = Units.All;

        List<Unit> initialMatches = allUnits.stream()
                                            .filter(unit -> unit.getSymbol().equals(unitStr))
                                            .collect(Collectors.toList());

        if (initialMatches.size() > 0) {
            return Optional.of(initialMatches.get(0));
        }

        int divisionCount = StringUtils.countMatches(unitStr, "/");
        if (divisionCount > 1) {
            // TODO don't really need to handle this case. If it exists there should be parentheses
            return Optional.empty();
        }

        // Parse Division Derived Product
        if (divisionCount == 1) {
            String[] splitStr = StringUtils.split(unitStr, "/", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> leftUnit = parseSymbol(leftSide);
            Optional<Unit> rightUnit = parseSymbol(rightSide);

            if (leftUnit.isPresent() && rightUnit.isPresent()) {
                DivisionDerivedUnit unit = new DivisionDerivedUnit(leftUnit.get(), rightUnit.get());

                return Optional.of(unit);
            } else {
                return Optional.empty();
            }
        }

        // Parse Product Derived Unit
        if (unitStr.contains(" ")) {
            String[] splitStr = StringUtils.split(unitStr, " ", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> leftUnit = parseSymbol(leftSide);
            Optional<Unit> rightUnit = parseSymbol(rightSide);

            if (leftUnit.isPresent() && rightUnit.isPresent()) {
                ProductDerivedUnit unit = new ProductDerivedUnit(leftUnit.get(), rightUnit.get());

                return Optional.of(unit);
            } else {
                return Optional.empty();
            }
        }

        // Parse Power Derived Unit
        if (unitStr.contains("^")) {
            // TODO should only be one instance (exception ms^2)
            String[] splitStr = StringUtils.split(unitStr, "^", 2);
            String leftSide = splitStr[0];
            String rightSide = splitStr[1];

            Optional<Unit> leftUnit = parseSymbol(leftSide);
            int power = Integer.parseInt(rightSide);

            if (leftUnit.isPresent()) {
                PowerDerivedUnit unit = new PowerDerivedUnit((NamedUnit) leftUnit.get(), power);

                return Optional.of(unit);
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static Unit parseUnicodeSymbol(String unitStr) {
        return null;
    }
}
