package Semantic.Units.Conversion.Engine;

import Semantic.Units.Common.IPrefixUnit;
import Semantic.Units.Conversion.Operations.Operation;
import Semantic.Units.Conversion.Operations.ReversibleOperation;
import Semantic.Units.Conversion.UnitConversion;
import Semantic.Units.Instances.Conversions;
import Semantic.Units.Model.Derived.PowerDerivedUnit;
import Semantic.Units.Model.NamedUnit;
import Semantic.Units.Model.Normalization;
import Semantic.Units.Model.Unit;

import Semantic.Units.Quantity;

// TODO use multiple graphs implemented as a Map<Dimension, Graph>
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
            if (GraphSolver.containsEdge(toUnit, fromUnit)) {
                ReversibleOperation inverseOperation = ((ReversibleOperation) operation).getInverseOperation();
                GraphSolver.addEdge(toUnit, fromUnit, inverseOperation);
            }
        }
    }

    public static Quantity convert(Quantity quantity, Unit convertTo) {
        double convertedValue = convert(quantity.unit, convertTo, quantity.value);
        Quantity converted = new Quantity(convertedValue, convertTo);
        return converted;
    }

    public static double convert(Unit fromUnit, Unit toUnit, Number value) {
        // TODO check that they are equivalent
        // TODO check that their dimensions are equivalent

        // if they are the same unit, return the original quantity value
        if (fromUnit.equals(toUnit)) {
            return value.doubleValue();
        }

        // if they are a prefix
        if (fromUnit instanceof IPrefixUnit && toUnit instanceof IPrefixUnit) {
            return prefixConvert((IPrefixUnit) fromUnit, (IPrefixUnit) toUnit, value);
        }

        // if there is a direct conversion registered between the two units
        if (GraphSolver.containsEdge(fromUnit, toUnit)) {
            return GraphSolver.getEdge(fromUnit, toUnit).invoke(value.doubleValue());
        }

        // if there is an indirect conversion between the two units
        if (GraphSolver.containsPath(fromUnit, toUnit)) {
            return GraphSolver.convert(fromUnit, toUnit, value);
        }

        // get the normal forms of both units
        Normalization fromNormalForm = fromUnit.getNormalForm();
        Normalization toNormalForm = toUnit.getNormalForm();

        return value.doubleValue() * getFactor(fromNormalForm, toNormalForm);
    }

    private static double getFactor(Normalization fromNorm, Normalization toNorm) {
        int scale = 0;
        double factor = 1;

        for (PowerDerivedUnit fromOperand : fromNorm.getProductOperands()) {
            NamedUnit baseForm = (NamedUnit) fromOperand.operand;
            PowerDerivedUnit toOperand = toNorm.getUnitWithSameDimension(baseForm.getBaseUnit());

            int localScale = 0;
            double localFactor = 1;
            if (fromOperand.operand instanceof IPrefixUnit && toOperand.operand instanceof IPrefixUnit) {
                localScale = getPrefixScale((IPrefixUnit) fromOperand.operand, (IPrefixUnit) toOperand.operand);
            } else {
                localFactor = GraphSolver.getConversionFactor(fromOperand.operand, toOperand.operand);
            }

            localScale = localScale * fromOperand.power;
            localFactor = Math.pow(localFactor, fromOperand.power);
            scale = scale + localScale;
            factor = factor * localFactor;
        }

        return factor * Math.pow(10, scale);
    }

    private static double prefixConvert(IPrefixUnit fromUnit, IPrefixUnit toUnit, Number value) {
        int scale = getPrefixScale(fromUnit, toUnit);

        return (value.doubleValue() * Math.pow(10, scale));
    }

    private static int getPrefixScale(IPrefixUnit fromUnit, IPrefixUnit toUnit) {
        return (fromUnit.getPrefixScale() - toUnit.getPrefixScale());
    }
}
