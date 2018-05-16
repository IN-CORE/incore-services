
package edu.illinois.ncsa.incore.semantic.units.model.derived;

import edu.illinois.ncsa.incore.semantic.units.common.IDerivableUnit;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public abstract class DerivedUnit extends Unit implements IDerivableUnit {
    protected DerivedUnit() {
        super();
    }

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
