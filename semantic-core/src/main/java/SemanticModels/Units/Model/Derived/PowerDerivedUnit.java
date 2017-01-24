package SemanticModels.Units.Model.Derived;

import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.Model.Normalization;
import SemanticModels.Units.Model.Unit;
import SemanticModels.Units.Utils.StringRepresentationUtil;

public class PowerDerivedUnit extends DerivedUnit {
    public Unit operand;
    public int power;

    public PowerDerivedUnit(Unit operand, int power) {
        super(StringRepresentationUtil.getRaisedPowerName(operand.name, power),
              StringRepresentationUtil.getRaisedPowerPlural(operand.plural, power),
              getSymbol(operand, power),
              getUnicodeSymbol(operand, power));

        this.operand = operand;
        this.power = power;
        this.unitSystem = operand.unitSystem;
    }

    public PowerDerivedUnit(Unit operand, int power, Dimension dimension) {
        super(StringRepresentationUtil.getRaisedPowerName(operand.name, power),
              StringRepresentationUtil.getRaisedPowerPlural(operand.plural, power),
              getSymbol(operand, power),
              getUnicodeSymbol(operand, power),
              dimension);

        this.operand = operand;
        this.power = power;
        this.unitSystem = operand.unitSystem;
    }

    private static String getSymbol(Unit operand, int power) {
        // x^1 => x
        if (power == 1) {
            return operand.symbol;
        } else {
            if (operand instanceof DerivedUnit) {
                return "(" + operand.symbol + ")^" + Integer.toString(power);
            } else {
                return operand.symbol + "^" + Integer.toString(power);
            }
        }
    }

    private static String getUnicodeSymbol(Unit operand, int power) {
        // x^1 => x
        if (power == 1) {
            return operand.symbol;
        } else {
            if (operand instanceof DerivedUnit) {
                return "(" + operand.unicodeSymbol + ")" + StringRepresentationUtil.getPowerRepresentationUnicode(power);
            } else {
                return operand.unicodeSymbol + StringRepresentationUtil.getPowerRepresentationUnicode(power);
            }
        }
    }

    @Override
    public Normalization getNormalForm() {
        Normalization normalForm = this.operand.getNormalForm();
        normalForm.raiseAll(this.power);
        return normalForm;
    }

    @Override
    public Normalization getBaseNormalForm() {
        Normalization normalForm = this.operand.getBaseNormalForm();
        normalForm.raiseAll(this.power);
        return normalForm;
    }

    public boolean equals(PowerDerivedUnit compareTo) {
        return (this.power == compareTo.power &&
                this.operand == compareTo.operand);
    }

    @Override
    public boolean equals(Object compareTo) {
        if (compareTo instanceof PowerDerivedUnit) {
            return this.equals((PowerDerivedUnit) compareTo);
        } else {
            return false;
        }
    }
}
