package Semantic.Units.Model;

import Semantic.Units.Common.IPrefixUnit;
import Semantic.Units.Dimension.Dimension;
import Semantic.Units.SIPrefix;
import Semantic.Units.UnitSystem;

import java.util.List;

public class PrefixableUnit extends NamedUnit implements IPrefixUnit {
    public List<SIPrefix> acceptedPrefixes;

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension) {
        super(name, plural, symbol, dimension);
        this.acceptedPrefixes = SIPrefix.allPrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension, List<SIPrefix> acceptedPrefixes) {
        super(name, plural, symbol, dimension);
        this.acceptedPrefixes = acceptedPrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, symbol, dimension, unitSystem);
        this.acceptedPrefixes = SIPrefix.allPrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        super(name, plural, symbol, unicodeSymbol, dimension);
        this.acceptedPrefixes = SIPrefix.allPrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                          UnitSystem unitSystem) {
        super(name, plural, symbol, unicodeSymbol, dimension, unitSystem);
        this.acceptedPrefixes = SIPrefix.allPrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                          List<SIPrefix> acceptedPrefixes) {
        super(name, plural, unicodeSymbol, dimension);
        this.acceptedPrefixes = acceptedPrefixes;
    }

    public PrefixableUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                          List<SIPrefix> acceptedPrefixes, Dimension dimension, UnitSystem unitSystem) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension, unitSystem);
        this.acceptedPrefixes = acceptedPrefixes;
    }

    @Override
    public int getPrefixScale() {
        return 0;
    }
}
