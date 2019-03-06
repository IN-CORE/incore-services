/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.dimension;

import edu.illinois.ncsa.incore.semantic.units.common.ISerializable;
import edu.illinois.ncsa.incore.semantic.units.common.ISymbol;
import edu.illinois.ncsa.incore.semantic.units.io.RDFFormat;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.ISerializer;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.RdfSerializer;
import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Dimension implements ISymbol, ISerializable {
    protected String name;
    protected String symbol;
    protected String unicodeSymbol;

    protected Dimension() {}

    protected Dimension(String name) {
        this.name = name;
    }

    protected Dimension(String name, String symbol, String unicodeSymbol) {
        this.name = name;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
    }

    protected abstract List<PowerDimension> getPowerDimensions();

    protected abstract DerivedDimension getNormalizedDimension();

    //region Operations
    public static DerivedDimension reciprocal(Dimension dimension) {
        List<PowerDimension> operands = new ArrayList<>();

        for (PowerDimension powerDimension : dimension.getPowerDimensions()) {
            operands.add(new PowerDimension(powerDimension.getBaseDimension(), powerDimension.getPower() * -1));
        }

        DerivedDimension reciprocalDimension = new DerivedDimension("Unspecified", operands);

        return reciprocalDimension;
    }

    public static Dimension power(Dimension dimension, int power) {
        if (power == 1) {
            return dimension;
        }

        List<PowerDimension> operands = new ArrayList<>();

        for (PowerDimension powerDimension : dimension.getPowerDimensions()) {
            operands.add(new PowerDimension(powerDimension.getBaseDimension(), powerDimension.getPower() * power));
        }

        DerivedDimension powerDimension = new DerivedDimension("Unspecified", operands);

        return powerDimension;
    }

    public static DerivedDimension multiply(Dimension leftOperand, Dimension rightOperand) {
        List<PowerDimension> operands = new ArrayList<>();
        operands.addAll(leftOperand.getPowerDimensions());
        operands.addAll(rightOperand.getPowerDimensions());

        DerivedDimension dimension = new DerivedDimension("Unspecified", operands);

        return dimension;
    }

    public static DerivedDimension divide(Dimension leftOperand, Dimension rightOperand) {
        List<PowerDimension> operands = new ArrayList<>();
        List<PowerDimension> reciprocalRightOperands = new ArrayList<>();

        for (PowerDimension powerDimension : rightOperand.getPowerDimensions()) {
            reciprocalRightOperands.add(new PowerDimension(powerDimension.getBaseDimension(), powerDimension.getPower() * -1));
        }

        operands.addAll(leftOperand.getPowerDimensions());
        operands.addAll(reciprocalRightOperands);

        DerivedDimension dimension = new DerivedDimension("Unspecified", operands);

        return dimension;
    }
    //endregion

    //region Serialization Methods
    @Override
    public String serialize() {
        ISerializer rdfSerializer = new RdfSerializer(RDFFormat.TURTLE);
        String output = rdfSerializer.serialize(this);
        return output;
    }

    @Override
    public String serialize(ISerializer serializer) {
        return serializer.serialize(this);
    }

    @Override
    public void serialize(ISerializer serializer, Writer writer) {
        serializer.serialize(this, writer);
    }
    //endregion

    //region Getters
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }
    //endregion

    @Override
    public String getResourceName() {
        String resourceName = StringRepresentationUtil.convertToResourceName(this.getName());

        return resourceName;
    }

    /**
     * Returns true if two dimensions are equivalent (i.e. their normalizations are identical).
     * For example: The dimensions Angle and Refractive index are not equal but they are equivalent.
     */
    public boolean equivalentTo(Dimension dimension) {
        if (this.equals(dimension)) {
            return true;
        }

        // Since the dimension symbols are always in simplified form (and sorted in the same order)
        // this is equivalent to checking if two dimensions are equivalent
        return this.getSymbol().equals(dimension.getSymbol());
    }

    @Override
    public String toString() {
        return this.getName() + " (" + this.getSymbol() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Dimension)) {
            return false;
        }

        Dimension dimension = (Dimension) obj;

        return Objects.equals(name, dimension.name) &&
                Objects.equals(symbol, dimension.symbol) &&
                Objects.equals(unicodeSymbol, dimension.unicodeSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symbol, unicodeSymbol);
    }
}
