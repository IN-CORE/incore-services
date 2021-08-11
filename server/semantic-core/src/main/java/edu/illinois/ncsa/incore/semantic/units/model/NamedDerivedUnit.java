/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.model;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.UnitSystem;
import edu.illinois.ncsa.incore.semantic.units.common.IDerivableUnit;
import edu.illinois.ncsa.incore.semantic.units.common.Normalization;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.derived.DerivedUnit;

import java.util.List;

/**
 * Pre-fixable Named Derived Unit.
 */
public class NamedDerivedUnit extends PrefixableUnit implements IDerivableUnit {
    private final DerivedUnit derivation;

    public NamedDerivedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                            List<Prefix> acceptedPrefixes, Dimension dimension, DerivedUnit derivation) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, acceptedPrefixes, dimension, derivation.unitSystem);
        this.derivation = derivation;
    }

    public NamedDerivedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                            List<Prefix> acceptedPrefixes, Dimension dimension, UnitSystem unitSystem, DerivedUnit derivation) {
        super(name, unicodeName, plural, unicodePlural, symbol, unicodeSymbol, acceptedPrefixes, dimension, unitSystem);
        this.derivation = derivation;
    }

    public NamedDerivedUnit(String name, String plural, String symbol, String unicodeSymbol,
                            Dimension dimension, UnitSystem centimetreGramSecond, List<Prefix> none, DerivedUnit derivation) {
        super(name, plural, symbol, unicodeSymbol, dimension, derivation.unitSystem);
        this.derivation = derivation;
    }

    public DerivedUnit getDerivation() {
        return derivation;
    }

    @Override
    public Normalization getBaseNormalForm() {
        return this.getDerivation().getBaseNormalForm();
    }
}
