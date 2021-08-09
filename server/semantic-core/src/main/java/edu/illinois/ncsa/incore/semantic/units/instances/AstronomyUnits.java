/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.instances;

import edu.illinois.ncsa.incore.semantic.units.model.NamedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.Arrays;
import java.util.List;

public final class AstronomyUnits {
    public static void initialize() {
    }

    public static final NamedUnit parsec = new PrefixableUnit("parsec", "parsecs", "pc", "pc", Dimensions.length,
        Arrays.asList(Prefixes.giga, Prefixes.mega));

    public static final NamedUnit solarMass = new PrefixableUnit("solar mass", "solar masses", "Mo", "Mo", Dimensions.mass);

    public static final List<Unit> All = Arrays.asList(parsec, solarMass);

}
