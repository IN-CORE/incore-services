/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.common;

import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DivisionDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.ProductDerivedUnit;

import java.util.*;
import java.util.stream.Collectors;

public class Normalization {
    private Unit unitToNormalize;
    private Dimension dimension = Dimensions.unspecified;
    private List<PowerDerivedUnit> productOperands = new ArrayList<>();

    private Normalization(List<PowerDerivedUnit> operands) {
        this.productOperands = operands;
        this.mergeOperands();
    }

    public Normalization(PowerDerivedUnit operand) {
        this.unitToNormalize = operand;
        this.dimension = operand.getDimension();
        this.productOperands = Collections.singletonList(operand);
        this.mergeOperands();
    }

    public Normalization(DerivedUnit unitToNormalize, List<PowerDerivedUnit> operands) {
        this.unitToNormalize = unitToNormalize;
        this.dimension = unitToNormalize.getDimension();
        this.productOperands = operands;
        this.mergeOperands();
    }

    public Normalization(DerivedUnit unitToNormalize, Normalization... normalizations) {
        this.unitToNormalize = unitToNormalize;
        this.dimension = unitToNormalize.getDimension();

        for (Normalization normalization : normalizations) {
            productOperands.addAll(normalization.productOperands);
        }

        this.mergeOperands();
    }

    public static Normalization raiseAll(Normalization normalization, int power) {
        List<PowerDerivedUnit> newOperands = new ArrayList<>();

        for (PowerDerivedUnit productOperand : normalization.getProductOperands()) {
            PowerDerivedUnit newOperand = new PowerDerivedUnit(productOperand.getOperand(), productOperand.getPower() * power);
            newOperands.add(newOperand);
        }

        return new Normalization(newOperands);
    }

    public Unit toSimpleForm() {
        List<PowerDerivedUnit> positiveOperands = this.productOperands.stream()
            .filter(op -> op.getPower() > 0)
            .collect(Collectors.toList());

        List<PowerDerivedUnit> negativeOperands = this.productOperands.stream()
            .filter(op -> op.getPower() < 0)
            .collect(Collectors.toList());

        Unit result = null;

        if (productOperands.size() == 0) {
            // in the case of units cancelling (e.g. s * s^-1 => 1)
            result = new NamedUnit("", "", "1", this.dimension);
        }

        if (positiveOperands.size() > 0) {
            Unit numerator;

            if (positiveOperands.get(0).getPower() == 1) {
                numerator = positiveOperands.get(0).getOperand();
            } else {
                numerator = positiveOperands.get(0);
            }

            for (int i = 0; i < positiveOperands.size() - 1; i++) {
                PowerDerivedUnit current = positiveOperands.get(i + 1);

                Unit temp;
                if (current.getPower() == 1) {
                    // m^1 => m
                    temp = new ProductDerivedUnit(numerator, current.getOperand());
                } else {
                    temp = new ProductDerivedUnit(numerator, current);
                }

                numerator = temp;
            }

            // Numerator and Denominator Both Present
            if (negativeOperands.size() > 0) {
                Unit denominator;

                PowerDerivedUnit firstNegative = negativeOperands.get(0);
                if (negativeOperands.get(0).getPower() == -1) {
                    denominator = firstNegative.getOperand();
                } else {
                    denominator = new PowerDerivedUnit(firstNegative.getOperand(), Math.abs(firstNegative.getPower()));
                }

                for (int i = 0; i < negativeOperands.size() - 1; i++) {
                    PowerDerivedUnit current = negativeOperands.get(i + 1);

                    Unit temp;
                    if (current.getPower() == -1) {
                        temp = new ProductDerivedUnit(denominator, current);
                    } else {
                        // negative sign becomes positive
                        temp = new ProductDerivedUnit(denominator, new PowerDerivedUnit(current.getOperand(),
                            Math.abs(current.getPower())));
                    }

                    denominator = temp;
                }

                result = new DivisionDerivedUnit(numerator, denominator, this.dimension);
            } else {
                result = numerator;
            }
        }

        // No Positive Units only Negative
        if (negativeOperands.size() > 0 && positiveOperands.size() == 0) {
            Unit denominator = negativeOperands.get(0);

            for (int i = 0; i < negativeOperands.size() - 1; i++) {
                PowerDerivedUnit current = negativeOperands.get(i + 1);

                Unit temp;
                temp = new ProductDerivedUnit(denominator, current, this.dimension);

                denominator = temp;
            }

            result = denominator;
        }

        if (result != null && result.equals(this.unitToNormalize)) {
            return unitToNormalize;
        } else {
            return result;
        }
    }

    // TODO should return a cloned list so the internal structure isn't modified
    public List<PowerDerivedUnit> getProductOperands() {
        return productOperands;
    }

    /**
     *
     */
    public PowerDerivedUnit getUnitWithBase(NamedUnit baseUnit) {
        for (PowerDerivedUnit productOperand : productOperands) {
            NamedUnit operand = productOperand.getOperand();
            if (operand.getBaseUnit().equals(baseUnit.getBaseUnit())) {
                return productOperand;
            }
        }

        throw new IllegalArgumentException("Could not find unit matching " + baseUnit.getBaseUnit().getName() + " in the normalization");
    }

    public PowerDerivedUnit getUnitWithSameDimension(NamedUnit baseUnit) {
        for (PowerDerivedUnit productOperand : productOperands) {
            NamedUnit operand = productOperand.getOperand();

            if (operand.getDimension().equals(baseUnit.getDimension())) {
                return productOperand;
            }
        }

        throw new IllegalArgumentException("Could not find unit with matching dimension of " + baseUnit.getDimension().getUnicodeSymbol() + " in the normalization");
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
        Map<NamedUnit, List<PowerDerivedUnit>> collectedOperands = new HashMap<>();

        List<PowerDerivedUnit> newOperands = new ArrayList<>();

        // add all to map collection such that the same units are grouped together
        // e.g. (m^2,m^5),(s,s),(w)
        for (PowerDerivedUnit productOperand : productOperands) {
            if (!collectedOperands.containsKey(productOperand.getOperand())) {
                collectedOperands.put(productOperand.getOperand(), new ArrayList<>());
            }

            collectedOperands.get(productOperand.getOperand()).add(productOperand);
        }

        // add powers together
        // e.g. (m^2,m^5),(s,s),(w) => m^7 s^2 w
        for (Map.Entry<NamedUnit, List<PowerDerivedUnit>> entry : collectedOperands.entrySet()) {
            if (entry.getValue().size() > 1) {
                int power = 0;

                for (PowerDerivedUnit operand : entry.getValue()) {
                    power += operand.getPower();
                }

                PowerDerivedUnit powerMergedOperands = new PowerDerivedUnit(entry.getKey(), power);
                newOperands.add(powerMergedOperands);
            } else {
                newOperands.add(entry.getValue().get(0));
            }
        }

        // drop operands with power of 0
        // e.g. (m^7, s^0, w^1) => (m^7, w^1)
        List<PowerDerivedUnit> mergedOperands = newOperands.stream()
            .filter(x -> x.getPower() != 0)
            .collect(Collectors.toList());

        this.productOperands = mergedOperands;
    }
}
