package SemanticModels.Units.Model.Derived;

import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.Model.NamedUnit;
import SemanticModels.Units.Model.Normalization;
import SemanticModels.Units.Model.Unit;

public class ReciprocalDerivedUnit extends DerivedUnit {
    public NamedUnit operand;

    public ReciprocalDerivedUnit(NamedUnit operand) {
        super("reciprocal " + operand.name,
              "reciprocal " + operand.plural,
              getSymbol(operand),
              getUnicodeSymbol(operand));

        this.operand = operand;
        this.unitSystem = operand.unitSystem;
    }

    public ReciprocalDerivedUnit(NamedUnit operand, Dimension dimension) {
        super("reciprocal " + operand.name,
              "reciprocal " + operand.plural,
              getSymbol(operand),
              getUnicodeSymbol(operand),
              dimension);

        this.operand = operand;
        this.unitSystem = operand.unitSystem;
    }

    public ReciprocalDerivedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                                 NamedUnit operand) {
        super(name, plural, symbol, unicodeSymbol, dimension);
        this.operand = operand;
        this.unitSystem = operand.unitSystem;
    }

    private static String getSymbol(Unit operand) {
        if (operand instanceof DerivedUnit) {
            return "(" + operand.symbol + ")^-1";
        } else {
            return operand.symbol + "^-1";
        }
    }

    private static String getUnicodeSymbol(Unit operand) {
        if (operand instanceof DerivedUnit) {
            return "(" + operand.unicodeSymbol + ")" + "\u207B\u00B9";
        } else {
            return operand.unicodeSymbol + "\u207B\u00B9";
        }
    }

    @Override
    public Normalization getNormalForm() {
        Normalization normalForm = this.operand.getNormalForm();
        normalForm.raiseAll(-1);
        return normalForm;
    }

    @Override
    public Normalization getBaseNormalForm() {
        Normalization normalForm = this.operand.getBaseNormalForm();
        normalForm.raiseAll(-1);
        return normalForm;
    }
}
