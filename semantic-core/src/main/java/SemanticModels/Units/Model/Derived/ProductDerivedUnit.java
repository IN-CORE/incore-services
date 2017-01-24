package SemanticModels.Units.Model.Derived;

import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.Model.Normalization;
import SemanticModels.Units.Model.Unit;

public class ProductDerivedUnit extends OperatorDerivedUnit {
    public ProductDerivedUnit(Unit leftOperand, Unit rightOperand) {
        super(leftOperand.name + " " + rightOperand.name,
              leftOperand.name + " " + rightOperand.plural,
              getSymbol(leftOperand, rightOperand),
              getUnicodeSymbol(leftOperand, rightOperand),
              leftOperand, rightOperand);
    }

    public ProductDerivedUnit(Unit leftOperand, Unit rightOperand, Dimension dimension) {
        super(leftOperand.name + " " + rightOperand.name,
              leftOperand.name + " " + rightOperand.plural,
              getSymbol(leftOperand, rightOperand),
              getUnicodeSymbol(leftOperand, rightOperand),
              dimension, leftOperand, rightOperand);
    }

    public ProductDerivedUnit(String name, String plural, String symbol, String unicodeSymbol,
                              Dimension dimension, Unit leftOperand, Unit rightOperand) {
        super(name, plural, symbol, unicodeSymbol, dimension, leftOperand, rightOperand);
    }

    // TODO kg * m/s^2
    private static String getSymbol(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof DerivedUnit && rightOperand instanceof DerivedUnit) {
            return "(" + leftOperand.symbol + ") (" + rightOperand.symbol + ")";
        } else if (leftOperand instanceof DerivedUnit) {
            return "(" + leftOperand.symbol + ") " + rightOperand.symbol;
        } else if (rightOperand instanceof DerivedUnit) {
            return leftOperand.symbol + " (" + rightOperand.symbol + ")";
        } else {
            return leftOperand.symbol + " " + rightOperand.symbol;
        }
    }

    private static String getUnicodeSymbol(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof DerivedUnit && rightOperand instanceof DerivedUnit) {
            return "(" + leftOperand.unicodeSymbol + ")\u22c5(" + rightOperand.unicodeSymbol + ")";
        } else if (leftOperand instanceof DerivedUnit) {
            return "(" + leftOperand.unicodeSymbol + ")\u22c5" + rightOperand.unicodeSymbol;
        } else if (rightOperand instanceof DerivedUnit) {
            return leftOperand.unicodeSymbol + "\u22c5(" + rightOperand.unicodeSymbol + ")";
        } else {
            return leftOperand.unicodeSymbol + "\u22c5" + rightOperand.unicodeSymbol;
        }
    }

    @Override
    public Normalization getNormalForm() {
        Normalization leftNormalizedForm = this.leftOperand.getNormalForm();
        Normalization rightNormalizedForm = this.rightOperand.getNormalForm();

        Normalization normalization = new Normalization(leftNormalizedForm, rightNormalizedForm);
        return normalization;
    }

    @Override
    public Normalization getBaseNormalForm() {
        Normalization leftNormalizedForm = this.leftOperand.getBaseNormalForm();
        Normalization rightNormalizedForm = this.rightOperand.getBaseNormalForm();

        Normalization normalization = new Normalization(leftNormalizedForm, rightNormalizedForm);
        return normalization;
    }
}
