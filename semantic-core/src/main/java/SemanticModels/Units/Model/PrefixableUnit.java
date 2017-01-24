package SemanticModels.Units.Model;

import SemanticModels.Units.Common.IPrefixUnit;
import SemanticModels.Units.Dimension.Dimension;
import SemanticModels.Units.SIPrefix;
import SemanticModels.Units.UnitSystem;

import java.util.List;

// TODO remove IPrefixUnit here?
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
