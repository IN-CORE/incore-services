package Semantic.Units.Model.Derived;

import Semantic.Units.Dimension.Dimension;
import Semantic.Units.Model.Unit;
import Semantic.Units.UnitSystem;

public abstract class OperatorDerivedUnit extends DerivedUnit {
    public Unit leftOperand;
    public Unit rightOperand;

    public OperatorDerivedUnit(String name, String plural, String plaintextSymbol, String unicodeSymbol, Unit leftOperand,
                               Unit rightOperand) {
        super(name, plural, plaintextSymbol, unicodeSymbol);

        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

        if (leftOperand.unitSystem == rightOperand.unitSystem) {
            this.unitSystem = leftOperand.unitSystem;
        } else {
            this.unitSystem = UnitSystem.Unspecified;
        }
    }

    public OperatorDerivedUnit(String name, String plural, String plaintextSymbol, String unicodeSymbol, Dimension dimension,
                               Unit leftOperand, Unit rightOperand) {
        super(name, plural, plaintextSymbol, unicodeSymbol, dimension);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

        if (leftOperand.unitSystem == rightOperand.unitSystem) {
            this.unitSystem = leftOperand.unitSystem;
        } else {
            this.unitSystem = UnitSystem.Unspecified;
        }
    }
}
