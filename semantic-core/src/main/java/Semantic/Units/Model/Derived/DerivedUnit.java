package Semantic.Units.Model.Derived;

import Semantic.Units.Common.IDerivableUnit;
import Semantic.Units.Dimension.Dimension;
import Semantic.Units.Model.Unit;

public abstract class DerivedUnit extends Unit implements IDerivableUnit {
    public DerivedUnit(String name, String plural, String symbol) {
        super(name, plural, symbol);
    }

    public DerivedUnit(String name, String plural, String symbol, Dimension dimension) {
        super(name, plural, symbol, dimension);
    }

    public DerivedUnit(String name, String plural, Dimension dimension) {
        super(name, plural, dimension);
    }

    public DerivedUnit(String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol);
    }

    public DerivedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        super(name, plural, symbol, unicodeSymbol, dimension);
    }

    public DerivedUnit getDerivation() {
        return this;
    }
}
