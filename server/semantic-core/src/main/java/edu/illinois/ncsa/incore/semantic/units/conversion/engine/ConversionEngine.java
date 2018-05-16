
package edu.illinois.ncsa.incore.semantic.units.conversion.engine;

import edu.illinois.ncsa.incore.semantic.units.Quantity;
import edu.illinois.ncsa.incore.semantic.units.common.IPrefixComparable;
import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.conversion.UnitConversion;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.Operation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.ReversibleOperation;
import edu.illinois.ncsa.incore.semantic.units.instances.Conversions;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;

import java.util.Optional;

// TODO use multiple graphs implemented as a Map<edu.illinois.ncsa.incore.semantic.units.Dimension, Graph>
public final class ConversionEngine {
    private ConversionEngine() {
    }

    static {
        GraphSolver.initialize();
        Conversions.initialize();
    }

    public static void initialize() {
        // do nothing, java will invoke static constructor
    }

    public static void registerConversion(Unit fromUnit, Unit toUnit, double factor) {
        registerConversion(new UnitConversion(fromUnit, toUnit, factor));
    }

    public static void registerConversion(Unit fromUnit, Unit toUnit, Operation operation) {
        registerConversion(new UnitConversion(fromUnit, toUnit, operation));
    }

    public static void registerConversion(UnitConversion conversion) {
        Unit fromUnit = conversion.getConvertFromUnit();
        Unit toUnit = conversion.getConvertToUnit();

        Operation operation = conversion.getOperation();
        GraphSolver.addEdge(fromUnit, toUnit, operation);

        if (conversion.getOperation() instanceof ReversibleOperation) {
            // check if there is a reverse edge (do not overwrite)
            if (!GraphSolver.containsEdge(toUnit, fromUnit)) {
                ReversibleOperation inverseOperation = ((ReversibleOperation) operation).getInverseOperation();
                GraphSolver.addEdge(toUnit, fromUnit, inverseOperation);
            }
        }
    }

    public static Quantity convert(Quantity quantity, Unit convertTo) {
        double convertedValue = convert(quantity.value, quantity.unit, convertTo);
        Quantity converted = new Quantity(convertedValue, convertTo);
        return converted;
    }

    public static Optional<Double> tryConvert(Number value, Unit fromUnit, Unit toUnit) {
        // if they are the same unit, then there is no need to perform any conversion
        if (fromUnit.equals(toUnit)) {
            return Optional.of(value.doubleValue());
        }

        // if there is a conversion path between the two units
        if (GraphSolver.containsPath(fromUnit, toUnit)) {
            Optional<Double> result = GraphSolver.tryConvert(fromUnit, toUnit, value);
            return result;
        }

        // if they are a prefix or prefixable
        if (fromUnit instanceof IPrefixComparable && toUnit instanceof IPrefixComparable) {
            // they have the same base e.g. millimeter and kilometer
            if (((NamedUnit) fromUnit).baseEquals(toUnit)) {
                double convertedValue = prefixConvert((IPrefixComparable) fromUnit, (IPrefixComparable) toUnit, value);
                return Optional.of(convertedValue);
            }

            NamedUnit fromUnitBase = ((NamedUnit) fromUnit).getBaseUnit();
            NamedUnit toUnitBase = ((NamedUnit) toUnit).getBaseUnit();

            if (GraphSolver.containsPath(fromUnitBase, toUnitBase)) {
                Optional<Double> result = GraphSolver.tryConvert(fromUnitBase, toUnitBase, 1);
                if (result.isPresent()) {
                    double prefixConvertedValue = prefixConvert((IPrefixComparable) fromUnit, (IPrefixComparable) toUnit, value);
                    double convertedValue = prefixConvertedValue * result.get();
                    return Optional.of(convertedValue);
                } else {
                    return Optional.empty();
                }
            }
        }

        // get the normal forms of both units
        // TODO replace normalization call with equivalentToShallow or something else (this is just used for debugging)
        Normalization fromNormalForm = fromUnit.getCoherentNormalForm();
        Normalization toNormalForm = toUnit.getBaseNormalForm();

        if (fromNormalForm.equals(toNormalForm)) {
            double convertedValue = value.doubleValue() * getFactor(fromNormalForm, toNormalForm);
            return Optional.of(convertedValue);
        }

        Normalization baseFrom = fromUnit.getBaseNormalForm();
        Normalization baseTo = toUnit.getBaseNormalForm();

        if (fromUnit.equivalentTo(toUnit)) {
            double convertedValue = value.doubleValue() * getFactor(fromUnit.getBaseNormalForm(), toUnit.getBaseNormalForm());
            return Optional.of(convertedValue);
        }

        if (fromUnit.dimensionEquals(toUnit)) {
            double convertedValue = value.doubleValue() * getFactor(fromUnit.getBaseNormalForm(), toUnit.getBaseNormalForm());
            return Optional.of(convertedValue);
        }

        if(fromUnit.dimensionEquivalent(toUnit)) {
            double convertedValue = value.doubleValue() * getFactor(fromUnit.getBaseNormalForm(), toUnit.getBaseNormalForm());
            return Optional.of(convertedValue);
        }

        return Optional.empty();
    }

    // TODO Fix
    public static double convert(Number value, Unit fromUnit, Unit toUnit) {
        // TODO check that they are equivalent
        // TODO check that their dimensions are equivalent


        // throw new IllegalArgumentException("Could not convert from " + fromUnit.getName() + " to " + toUnit.getName());

        return tryConvert(value, fromUnit, toUnit).get();
    }

    private static double getFactor(Normalization fromNorm, Normalization toNorm) {
        int scale = 0;
        double factor = 1;

        for (PowerDerivedUnit fromOperand : fromNorm.getProductOperands()) {
            NamedUnit baseForm = fromOperand.getOperand();
            PowerDerivedUnit toOperand = toNorm.getUnitWithSameDimension(baseForm.getBaseUnit());

            int localScale = 0;
            double localFactor = 1;
            if (fromOperand.getOperand() instanceof IPrefixComparable && toOperand.getOperand() instanceof IPrefixComparable) {
                int fromScale = ((IPrefixComparable) fromOperand.getOperand()).getPrefixScale();
                int toScale = ((IPrefixComparable) toOperand.getOperand()).getPrefixScale();

                localScale = fromScale - toScale;
            } else {
                localFactor = GraphSolver.getConversionFactor(fromOperand.getOperand(), toOperand.getOperand());
            }

            localScale = localScale * fromOperand.getPower();
            localFactor = Math.pow(localFactor, fromOperand.getPower());
            scale = scale + localScale;
            factor = factor * localFactor;
        }

        return factor * Math.pow(10, scale);
    }

    private static double prefixConvert(IPrefixComparable fromUnit, IPrefixComparable toUnit, Number value) {
        int fromPrefixBase = fromUnit.getPrefixBase();
        int fromPrefixScale = fromUnit.getPrefixScale();
        int toPrefixBase = toUnit.getPrefixBase();
        int toPrefixScale = toUnit.getPrefixScale();

        if (fromPrefixScale == 0 || toPrefixScale == 0) {
            // since at least one unit will have a prefix scale of 0, this should cancel
            int scale = fromPrefixScale - toPrefixScale;
            // since at least one unit will have a prefix base of 1, this should cancel
            int base = fromPrefixBase * toPrefixBase;

            return value.doubleValue() * Math.pow(base, scale);
        } else if (fromPrefixBase == toPrefixBase) {
            int scale = fromPrefixScale - toPrefixScale;

            return value.doubleValue() * Math.pow(fromPrefixBase, scale);
        } else {
            return value.doubleValue() * (Math.pow(fromPrefixBase, fromPrefixScale) / Math.pow(toPrefixBase, toPrefixScale));
        }
    }
}
