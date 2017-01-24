package SemanticModels.Units.Model;

import SemanticModels.Units.Common.IPrefixUnit;
import SemanticModels.Units.SIPrefix;

public class PrefixedUnit extends NamedUnit implements IPrefixUnit {
    public SIPrefix prefix;
    public PrefixableUnit baseUnit;

    public PrefixedUnit(SIPrefix prefix, PrefixableUnit unit) {
        super(prefix.getName() + unit.name,
              prefix.getName() + unit.plural,
              prefix.getAsciiSymbol() + unit.symbol,
              prefix.getUnicodeSymbol() + unit.unicodeSymbol,
              unit.dimension);

        this.prefix = prefix;
        this.baseUnit = unit;
        this.unitSystem = baseUnit.unitSystem;

        if (!unit.acceptedPrefixes.contains(prefix)) {
            throw new IllegalArgumentException("SIPrefix " + prefix.getName() + " is not an applicable prefix to the unit " + unit.name);
        }
    }

    public PrefixedUnit(SIPrefix prefix, PrefixableUnit unit, String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol, unit.dimension);

        this.prefix = prefix;
        this.baseUnit = unit;
        this.unitSystem = baseUnit.unitSystem;

        if (!unit.acceptedPrefixes.contains(prefix)) {
            throw new IllegalArgumentException("SIPrefix " + prefix.getName() + " is not an applicable prefix to the unit " + unit.name);
        }
    }

    @Override
    public int getPrefixScale() {
        return prefix.getScale();
    }

    @Override
    public PrefixableUnit getBaseUnit() {
        return baseUnit;
    }
}
