package SemanticModels.Units.Model.Derived;

import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.Instances.Dimensions;
import SemanticModels.Units.Model.Normalization;
import SemanticModels.Units.Model.Unit;

public class DivisionDerivedUnit extends OperatorDerivedUnit {
    public DivisionDerivedUnit(String name, String plural, String symbol, String unicodeSymbol,
                               Dimension dimension, Unit leftOperand, Unit rightOperand) {
        super(name, plural, symbol, unicodeSymbol, dimension, leftOperand, rightOperand);
    }

    public DivisionDerivedUnit(Unit numerator, Unit denominator, Dimension dimension) {
        super(numerator.name + " per " + denominator.name,
              numerator.plural + " per " + denominator.name,
              getSymbol(numerator, denominator),
              getUnicodeSymbol(numerator, denominator),
              dimension, numerator, denominator);
    }

    public DivisionDerivedUnit(Unit numerator, Unit denominator) {
        this(numerator, denominator, Dimensions.unspecified);
    }

    private static String getSymbol(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof DerivedUnit && rightOperand instanceof DerivedUnit) {
            return "(" + leftOperand.symbol + ")/(" + rightOperand.symbol + ")";
        } else if (leftOperand instanceof DerivedUnit) {
            return "(" + leftOperand.symbol + ")/" + rightOperand.symbol;
        } else if (rightOperand instanceof DerivedUnit) {
            return leftOperand.symbol + "/(" + rightOperand.symbol + ")";
        } else {
            return leftOperand.symbol + "/" + rightOperand.symbol;
        }
    }

    private static String getUnicodeSymbol(Unit leftOperand, Unit rightOperand) {
        if (leftOperand instanceof DerivedUnit && rightOperand instanceof DerivedUnit) {
            return "(" + leftOperand.unicodeSymbol + ")/(" + rightOperand.unicodeSymbol + ")";
        } else if (leftOperand instanceof DerivedUnit) {
            return "(" + leftOperand.unicodeSymbol + ")/" + rightOperand.unicodeSymbol;
        } else if (rightOperand instanceof DerivedUnit) {
            return leftOperand.unicodeSymbol + "/(" + rightOperand.unicodeSymbol + ")";
        } else {
            return leftOperand.unicodeSymbol + "/" + rightOperand.unicodeSymbol;
        }
    }

    @Override
    public Normalization getNormalForm() {
        Normalization leftNormalForm = this.leftOperand.getNormalForm();
        Normalization rightNormalForm = this.rightOperand.getNormalForm();

        rightNormalForm.raiseAll(-1);

        Normalization normalization = new Normalization(leftNormalForm, rightNormalForm);
        return normalization;
    }

    @Override
    public Normalization getBaseNormalForm() {
        Normalization leftNormalForm = this.leftOperand.getBaseNormalForm();
        Normalization rightNormalForm = this.rightOperand.getBaseNormalForm();

        rightNormalForm.raiseAll(-1);

        Normalization normalization = new Normalization(leftNormalForm, rightNormalForm);
        return normalization;
    }
}
