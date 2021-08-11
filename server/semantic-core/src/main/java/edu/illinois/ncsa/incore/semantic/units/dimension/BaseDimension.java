/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.dimension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class used to represents the seven base physical quantities of which all other physical quantities
 * are derived.
 * <p>
 * Note: This Class is sealed with a closed constructor, this can be modified in the future if
 * new BaseDimensions need to be created (I can't imagine a scenario that would involve constructing new BaseDimensions).
 */
public final class BaseDimension extends Dimension {
    private final int sortOrder;

    public static final BaseDimension length = new BaseDimension("Length", "L", "L", 1);
    public static final BaseDimension mass = new BaseDimension("Mass", "M", "M", 2);
    public static final BaseDimension time = new BaseDimension("Time", "T", "T", 3);
    public static final BaseDimension electricCurrent = new BaseDimension("Electric current", "I", "I", 4);
    public static final BaseDimension temperature = new BaseDimension("Temperature", "O", "\u0398", 5);
    public static final BaseDimension amountOfSubstance = new BaseDimension("Amount of substance", "N", "N", 6);
    public static final BaseDimension luminousIntensity = new BaseDimension("Luminous intensity", "J", "J", 7);

    public static final List<BaseDimension> All = Arrays.asList(length, mass, time, electricCurrent,
        temperature, amountOfSubstance, luminousIntensity);

    private BaseDimension(String name, String symbol, String unicodeSymbol, int sortOrder) {
        super(name, symbol, unicodeSymbol);
        this.sortOrder = sortOrder;
    }

    //region Query Operators
    public static BaseDimension getBySymbol(String symbol) {
        return tryGetBySymbol(symbol).get();
    }

    public static Optional<BaseDimension> tryGetBySymbol(String symbol) {
        return All.stream().filter(dimension -> dimension.getSymbol().equals(symbol) || dimension.getUnicodeSymbol().equals(symbol))
            .findFirst();
    }
    //endregion

    @Override
    protected List<PowerDimension> getPowerDimensions() {
        return Collections.singletonList(new PowerDimension(this));
    }

    @Override
    protected DerivedDimension getNormalizedDimension() {
        return new DerivedDimension(this.getPowerDimensions());
    }

    // TODO
    @Override
    public boolean equivalentTo(Dimension dimension) {
        return false;
    }

    protected int getSortOrder() {
        return sortOrder;
    }
}
