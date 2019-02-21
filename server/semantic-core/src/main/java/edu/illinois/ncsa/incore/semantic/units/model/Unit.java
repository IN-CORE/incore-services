/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.common.ISerializable;
import edu.illinois.ncsa.incore.semantic.units.common.ISymbol;
import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.RDFFormat;
import edu.illinois.ncsa.incore.semantic.units.io.parser.NameParser;
import edu.illinois.ncsa.incore.semantic.units.io.parser.SymbolParser;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.ISerializer;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.RdfSerializer;
import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;

import java.io.Writer;
import java.util.Objects;
import java.util.Optional;

// TODO implement builder pattern if constructors become unmanageable
public abstract class Unit implements ISymbol, ISerializable {
    protected Normalization baseNormalForm;
    protected Normalization coherentNormalForm;

    protected String name = "";
    protected String unicodeName = "";
    protected String plural = "";
    protected String unicodePlural = "";
    protected String symbol = "";
    protected String unicodeSymbol = "";

    protected Dimension dimension = Dimensions.unspecified;
    protected UnitSystem unitSystem = UnitSystem.Unspecified;

    //region Constructors
    public Unit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                Dimension dimension, UnitSystem system) {
        this.name = name;
        this.unicodeName = unicodeName;
        this.plural = plural;
        this.unicodePlural = unicodePlural;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
        this.dimension = dimension;
        this.unitSystem = system;
    }

    protected Unit() {}

    public Unit(String name, String unicodeName, String plural, String unicodePlural, String symbol,
                String unicodeSymbol, Dimension dimension) {
        this(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension, UnitSystem.Unspecified);
    }

    public Unit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        this(name, name, plural, plural, symbol, unicodeSymbol, dimension, UnitSystem.Unspecified);
    }

    public Unit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension, UnitSystem unitSystem) {
        this(name, name, plural, plural, symbol, unicodeSymbol, dimension, unitSystem);
    }

    public Unit(String name, String plural, String symbol, Dimension dimension, UnitSystem unitSystem) {
        this(name, name, plural, plural, symbol, symbol, dimension, unitSystem);
    }

    public Unit(String name, String plural, String symbol) {
        this(name, name, plural, plural, symbol, symbol, Dimensions.unspecified, UnitSystem.Unspecified);
    }

    public Unit(String name, String plural, String symbol, Dimension dimension) {
        this(name, name, plural, plural, symbol, symbol, dimension, UnitSystem.Unspecified);
    }

    // no symbols, use the name instead
    public Unit(String name, String plural, Dimension dimension) {
        this(name, name, plural, plural, name, name, dimension, UnitSystem.Unspecified);
    }

    // no symbols, use the name instead
    public Unit(String name, String plural, Dimension dimension, UnitSystem unitSystem) {
        this(name, name, plural, plural, name, name, dimension, unitSystem);
    }

    // no symbols, use the name instead
    public Unit(String name, String plural, String symbol, String unicodeSymbol) {
        this(name, name, plural, plural, symbol, unicodeSymbol, Dimensions.unspecified, UnitSystem.Unspecified);
    }
    //endregion

    //region Getters
    public String getSymbol() {
        return symbol;
    }

    public UnitSystem getUnitSystem() {
        return unitSystem;
    }

    public String getName() {
        return name;
    }

    public String getUnicodeName() {
        return unicodeName;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }

    public String getUnicodePlural() {
        return unicodePlural;
    }

    public String getPlural() {
        return plural;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Normalization getBaseNormalForm() {
        return baseNormalForm;
    }

    public Normalization getCoherentNormalForm() {
        return coherentNormalForm;
    }
    //endregion

    public static Optional<Unit> parseSymbolString(String unitStr) {
        return SymbolParser.tryParseSymbol(unitStr);
    }

    public static Optional<Unit> parseNameString(String unitStr) {
        return NameParser.tryParseName(unitStr);
    }

    /**
     * Returns true if the unit has the dimension as the comparison unit
     */
    public boolean dimensionEquals(Unit unit) {
        return this.dimension.equals(unit.dimension);
    }

    /**
     * Returns true if the unit has the same dimension as the comparison dimension.
     */
    public boolean dimensionEquals(Dimension dimension) {
        return this.dimension.equals(dimension);
    }

    /**
     * Returns true if the unit has the equivalent dimension as the comparison unit
     */
    public boolean dimensionEquivalent(Unit unit) {
        return this.dimension.equivalentTo(unit.dimension);
    }

    /**
     * Returns true if the units are equal or equivalent.
     * For example: m * m and m^2 are equivalent but not equal.
     */
    public boolean equivalentTo(Unit compare) {
        // if they are equal then they are also equivalent
        if (this.equals(compare)) {
            return true;
        }

        Normalization normalForm = this.getBaseNormalForm();
        Normalization compareNormalForm = compare.getBaseNormalForm();

        return normalForm.equals(compareNormalForm);
    }

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
        serializer.serialize(this);
    }

    @Override
    public String getResourceName() {
        String resourceName = StringRepresentationUtil.convertToResourceName(this.getName());
        return resourceName;
    }

    /**
     * Computes the normalized form of a derived unit. This does not include
     * breaking down coherent derived units into their constituent parts.
     * The normalized form is expressed as the product of power derived units.
     * <p>
     * e.g. J/m => J^1 * m^-1
     */
    protected abstract Normalization computeCoherentNormalForm();

    /**
     * Compute the normalized form of a derived unit. This includes breaking
     * down coherent derived units into their constituent parts.
     * The normalized form is expressed as the product of power derived units.
     * <p>
     * e.g. J/m => kg^1 * m^1 * s^-2
     * e.g. rad/s => s^-1
     */
    protected abstract Normalization computeBaseNormalForm();


    /**
     * TODO
     */
    public abstract Unit getSimplifiedForm();


    @Override
    public String toString() {
        return this.getName() + " (" + this.getSymbol() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Unit)) {
            return false;
        }

        Unit unit = (Unit) obj;
        return Objects.equals(name, unit.name) &&
                Objects.equals(unicodeName, unit.unicodeName) &&
                Objects.equals(plural, unit.plural) &&
                Objects.equals(unicodePlural, unit.unicodePlural) &&
                Objects.equals(symbol, unit.symbol) &&
                Objects.equals(unicodeSymbol, unit.unicodeSymbol);
                // && Objects.equals(dimension, unit.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension);
    }
}
