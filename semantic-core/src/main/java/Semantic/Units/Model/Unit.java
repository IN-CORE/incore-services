package Semantic.Units.Model;

import Semantic.Units.Common.ISymbolizable;
import Semantic.Units.Dimension.Dimension;
import Semantic.Units.Instances.Dimensions;
import Semantic.Units.UnitSystem;

// TODO implement builder pattern if constructors become unmanageable
// TODO make immutable?
public abstract class Unit implements ISymbolizable {
    private Normalization normalization;

    public String name = "";
    public String unicodeName = "";
    public String plural = "";
    public String unicodePlural = "";
    public String symbol = "";
    public String unicodeSymbol = "";

    public Dimension dimension = Dimensions.unspecified;
    public UnitSystem unitSystem = UnitSystem.Unspecified;

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

    public Unit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                Dimension dimension) {
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

    public String getName() {
        return this.name;
    }

    public String getUnicodeSymbol() {
        return this.symbol;
    }

    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Gets the normalized form of a derived unit.
     * The normalized form is expressed in terms of power derived units
     * e.g. J/m => J^1 * m^-1
     */
    public abstract Normalization getNormalForm();

    /**
     * Gets the normalized form of a derived unit including
     * the derivation of a named derived unit
     * e.g. J/m => kg^1 * m^1 * s^-2
     */
    public abstract Normalization getBaseNormalForm();

    public boolean equivalentTo(Unit compare) {
        Normalization normalForm = this.getBaseNormalForm();
        Normalization compareNormalForm = compare.getBaseNormalForm();

        return normalForm.equals(compareNormalForm);
    }
}
