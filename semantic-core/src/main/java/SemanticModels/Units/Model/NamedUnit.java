package SemanticModels.Units.Model;

import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.Model.Derived.PowerDerivedUnit;
import SemanticModels.Units.UnitSystem;

// TODO prefixableunit and namedunit can be merged, prefixable units would need to be defined and initialized to none
public class NamedUnit extends Unit {

    public NamedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                     Dimension dimension, UnitSystem system) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension, system);
    }

    public NamedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                     Dimension dimension) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension);
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        super(name, plural, symbol, unicodeSymbol, dimension);
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                     UnitSystem unitSystem) {
        super(name, plural, symbol, unicodeSymbol, dimension, unitSystem);
    }

    public NamedUnit(String name, String plural, String symbol, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, symbol, dimension, unitSystem);
    }

    public NamedUnit(String name, String plural, String symbol) {
        super(name, plural, symbol);
    }

    public NamedUnit(String name, String plural, String symbol, Dimension dimension) {
        super(name, plural, symbol, dimension);
    }

    public NamedUnit(String name, String plural, Dimension dimension) {
        super(name, plural, dimension);
    }

    public NamedUnit(String name, String plural, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, dimension, unitSystem);
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol);
    }

    public NamedUnit getBaseUnit() {
        return this;
    }

    public boolean baseEquals(Unit unit) {
        if (unit instanceof NamedUnit) {
            return this.getBaseUnit().equals(((NamedUnit) unit).getBaseUnit());
        } else {
            return this.getBaseUnit().equals(unit);
        }
    }

    @Override
    public Normalization getNormalForm() {
        PowerDerivedUnit normalForm = new PowerDerivedUnit(this, 1);
        Normalization normalization = new Normalization(normalForm);
        return normalization;
    }

    @Override
    public Normalization getBaseNormalForm() {
        return this.getNormalForm();
    }
}
