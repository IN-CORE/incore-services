
package edu.illinois.ncsa.incore.semantic.units.model;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.common.IPrefixComparable;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Prefixes;

import java.util.List;

public class PrefixableUnit extends NamedUnit implements IPrefixComparable {
    protected List<Prefix> applicablePrefixes;

    protected PrefixableUnit() {
        super();
    }

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension) {
        super(name, plural, symbol, dimension);
        this.applicablePrefixes = Prefixes.SI;
    }

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension, List<Prefix> applicablePrefixes) {
        super(name, plural, symbol, dimension);
        this.applicablePrefixes = applicablePrefixes;
    }

    public PrefixableUnit(String name, String plural, String symbol, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, symbol, dimension, unitSystem);
        this.applicablePrefixes = Prefixes.SI;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        super(name, plural, symbol, unicodeSymbol, dimension);
        this.applicablePrefixes = Prefixes.SI;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                          UnitSystem unitSystem) {
        super(name, plural, symbol, unicodeSymbol, dimension, unitSystem);
        this.applicablePrefixes = Prefixes.SI;
    }

    public PrefixableUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                          List<Prefix> applicablePrefixes) {
        super(name, plural, symbol, unicodeSymbol, dimension);
        this.applicablePrefixes = applicablePrefixes;
    }

    public PrefixableUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                          List<Prefix> applicablePrefixes, Dimension dimension, UnitSystem unitSystem) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension, unitSystem);
        this.applicablePrefixes = applicablePrefixes;
    }

    public List<Prefix> getApplicablePrefixes() {
        return applicablePrefixes;
    }

    @Override
    public int getPrefixScale() {
        return 0;
    }

    @Override
    public int getPrefixBase() {
        return 1;
    }
}
