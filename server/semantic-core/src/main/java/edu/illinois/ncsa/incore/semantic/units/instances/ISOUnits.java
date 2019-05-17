/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class ISOUnits {
    private ISOUnits() {}

    public static void initialize() {}

    public static final PrefixableUnit bit = new PrefixableUnit("bit", "bits", "b", Dimensions.informationEntropy,
                                                                Prefixes.Binary);

    public static final PrefixableUnit bytes = new PrefixableUnit("byte", "bytes", "B", Dimensions.informationEntropy,
                                                                  Prefixes.Binary);

    // public static final DivisionDerivedUnit bitsPerSecond = new DivisionDerivedUnit(bit, second, Dimensions.informationTransfer);
    // public static final DivisionDerivedUnit bytesPerSecond = new DivisionDerivedUnit(bytes, second, Dimensions.informationTransfer);

    public static final List<Unit> All = Arrays.asList(bit, bytes);
}
