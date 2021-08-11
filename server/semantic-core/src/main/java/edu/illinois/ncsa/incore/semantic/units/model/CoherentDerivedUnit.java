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
 * This class represents Derived Units with Special Names and Symbols
 */
public class CoherentDerivedUnit extends PrefixableUnit implements IDerivableUnit {
    private final DerivedUnit derivation;

    public CoherentDerivedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                               List<Prefix> applicablePrefixes, Dimension dimension, DerivedUnit derivation) {
        super();

        super.name = name;
        super.unicodeName = unicodeName;
        super.plural = plural;
        super.unicodePlural = unicodePlural;
        super.symbol = symbol;
        super.unicodeSymbol = unicodeSymbol;
        super.applicablePrefixes = applicablePrefixes;
        super.dimension = dimension;
        super.unitSystem = derivation.unitSystem;

        this.derivation = derivation;

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public CoherentDerivedUnit(String name, String unicodeName, String plural, String unicodePlural, String symbol, String unicodeSymbol,
                               List<Prefix> applicablePrefixes, Dimension dimension, UnitSystem unitSystem, DerivedUnit derivation) {
        super();

        super.name = name;
        super.unicodeName = unicodeName;
        super.plural = plural;
        super.unicodePlural = unicodePlural;
        super.symbol = symbol;
        super.unicodeSymbol = unicodeSymbol;
        super.applicablePrefixes = applicablePrefixes;
        super.dimension = dimension;
        super.unitSystem = unitSystem;

        this.derivation = derivation;

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public CoherentDerivedUnit(String name, String plural, String symbol, String unicodeSymbol, Dimension dimension,
                               UnitSystem unitSystem, List<Prefix> applicablePrefixes, DerivedUnit derivation) {
        super();

        super.name = name;
        super.unicodeName = name;
        super.plural = plural;
        super.unicodePlural = plural;
        super.symbol = symbol;
        super.unicodeSymbol = unicodeSymbol;
        super.applicablePrefixes = applicablePrefixes;
        super.dimension = dimension;
        super.unitSystem = unitSystem;

        this.derivation = derivation;

        super.baseNormalForm = computeBaseNormalForm();
        super.coherentNormalForm = computeCoherentNormalForm();
    }

    public DerivedUnit getDerivation() {
        return derivation;
    }

    @Override
    protected Normalization computeBaseNormalForm() {
        return this.derivation.getBaseNormalForm();
    }
}
