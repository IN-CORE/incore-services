
package edu.illinois.ncsa.incore.semantic.units.model;

import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;

// NOTE non-prefixable
public class NamedUnit extends Unit {
    protected NamedUnit() {
        super();
    }

    public NamedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                     Dimension dimension, UnitSystem system) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension, system);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                     Dimension dimension) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, dimension);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension) {
        super(name, plural, symbol, unicodeSymbol, dimension);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                     UnitSystem unitSystem) {
        super(name, plural, symbol, unicodeSymbol, dimension, unitSystem);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, symbol, dimension, unitSystem);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol) {
        super(name, plural, symbol);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol, Dimension dimension) {
        super(name, plural, symbol, dimension);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, Dimension dimension) {
        super(name, plural, dimension);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, Dimension dimension, UnitSystem unitSystem) {
        super(name, plural, dimension, unitSystem);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public NamedUnit(String name, String plural, String symbol, String unicodeSymbol) {
        super(name, plural, symbol, unicodeSymbol);

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
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
    protected Normalization computeCoherentNormalForm() {
        PowerDerivedUnit normalForm = new PowerDerivedUnit(this, 1);
        Normalization normalization = new Normalization(normalForm);
        return normalization;
    }

    @Override
    protected Normalization computeBaseNormalForm() {
        PowerDerivedUnit normalForm = new PowerDerivedUnit(this, 1);
        Normalization normalization = new Normalization(normalForm);
        return normalization;
    }

    @Override
    public Unit getSimplifiedForm() {
        return this;
    }
}
