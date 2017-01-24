package SemanticModels.Units.Conversion;

import SemanticModels.Units.Conversion.Operations.Operation;
import SemanticModels.Units.Conversion.Operations.MultiplyOperation;
import SemanticModels.Units.Model.Unit;

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
