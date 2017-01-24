package SemanticModels.Units.Model;

import SemanticModels.Units.Model.Derived.PowerDerivedUnit;

import java.util.*;

public class Normalization {
    private List<PowerDerivedUnit> productOperands = new ArrayList<>();

    public Normalization(PowerDerivedUnit... operands) {
        this.productOperands = Arrays.asList(operands);

        this.mergeOperands();
    }

    public Normalization(Normalization... normalizations) {
        for (Normalization normalization : normalizations) {
            productOperands.addAll(normalization.productOperands);
        }

        this.mergeOperands();
    }

    // TODO should return a cloned list so the internal structure isn't modified
    public List<PowerDerivedUnit> getProductOperands() {
        return productOperands;
    }

    public void raiseAll(int power) {
        List<PowerDerivedUnit> newOperands = new ArrayList<>();

        for (PowerDerivedUnit productOperand : productOperands) {
            PowerDerivedUnit newOperand = new PowerDerivedUnit(productOperand.operand, productOperand.power * power);
            newOperands.add(newOperand);
        }

        this.productOperands = newOperands;
    }

    /**
     *
     */
    public PowerDerivedUnit getUnitWithBase(NamedUnit baseUnit) {
        for (PowerDerivedUnit productOperand : productOperands) {
            NamedUnit operand = (NamedUnit) productOperand.operand;
            if (operand.getBaseUnit().equals(baseUnit.getBaseUnit())) {
                return productOperand;
            }
        }

        throw new IllegalArgumentException("Could not find unit matching " + baseUnit.getBaseUnit().getName() + " in the normalization");
    }

    public PowerDerivedUnit getUnitWithSameDimension(NamedUnit baseUnit) {
        for (PowerDerivedUnit productOperand : productOperands) {
            NamedUnit operand = (NamedUnit) productOperand.operand;

            if (operand.getDimension().equals(baseUnit.getDimension())) {
                return productOperand;
            }
        }

        throw new IllegalArgumentException(
                "Could not find unit with matching dimension of " + baseUnit.getDimension().getUnicodeSymbol() + " in the normalization");

    }

    @Override
    public boolean equals(Object compare) {
        if (compare instanceof Normalization) {
            Normalization normCompare = (Normalization) compare;

            if (normCompare.productOperands.size() == this.productOperands.size()) {
                return this.productOperands.containsAll(normCompare.productOperands);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Will merge product operands into a single unit
     * e.g. x^1 * x^2 = x^3
     */
    private void mergeOperands() {
        Map<Unit, List<PowerDerivedUnit>> collectedOperands = new HashMap<>();

        List<PowerDerivedUnit> newOperands = new ArrayList<>();

        // add all to map collection such that the same units are grouped together
        // e.g. (m^2,m^5),(s,s),(w)
        for (PowerDerivedUnit productOperand : productOperands) {
            if (!collectedOperands.containsKey(productOperand.operand)) {
                collectedOperands.put(productOperand.operand, new ArrayList<PowerDerivedUnit>());
            }

            collectedOperands.get(productOperand.operand).add(productOperand);
        }

        for (Map.Entry<Unit, List<PowerDerivedUnit>> entry : collectedOperands.entrySet()) {
            if (entry.getValue().size() > 1) {
                int power = 0;

                for (PowerDerivedUnit newOperand : entry.getValue()) {
                    power += newOperand.power;
                }

                PowerDerivedUnit newOperand = new PowerDerivedUnit(entry.getKey(), power);
                newOperands.add(newOperand);
            } else {
                newOperands.add(entry.getValue().get(0));
            }
        }

        this.productOperands = newOperands;
    }
}
