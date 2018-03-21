
package edu.illinois.ncsa.incore.semantic.units.conversion;

import edu.illinois.ncsa.incore.semantic.units.conversion.operations.MultiplyOperation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.Operation;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public class UnitConversion {
    private Unit convertFrom;
    private Unit convertTo;
    private Operation operation;

    public UnitConversion(Unit convertFrom, Unit convertTo, Operation operation) {
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
        this.operation = operation;
    }

    public UnitConversion(Unit convertFrom, Unit convertTo, Number factor) {
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
        this.operation = new MultiplyOperation(factor.doubleValue());
    }

    public Unit getConvertFromUnit() {
        return this.convertFrom;
    }

    public Unit getConvertToUnit() {
        return this.convertTo;
    }

    public Operation getOperation() {
        return this.operation;
    }
}
