/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.dimension;

import edu.illinois.ncsa.incore.semantic.units.utils.StringRepresentationUtil;

import java.util.Collections;
import java.util.List;

/**
 * Used for normalization, should not be constructed by the API user
 */
class PowerDimension extends Dimension {
    private int power = 1;

    private final BaseDimension baseDimension;

    protected PowerDimension(BaseDimension dimension) {
        super.name = dimension.getName();
        super.symbol = dimension.getSymbol();
        super.unicodeSymbol = dimension.getUnicodeSymbol();
        this.baseDimension = dimension;
        this.power = 1;
    }

    protected PowerDimension(BaseDimension dimension, int power) {
        super.name = StringRepresentationUtil.getRaisedPowerName(dimension.getName(), power);

        if (power == 1) {
            super.unicodeSymbol = dimension.getUnicodeSymbol();
            super.symbol = dimension.getSymbol();
        } else {
            super.unicodeSymbol = dimension.getUnicodeSymbol() + StringRepresentationUtil.toUnicodePowerString(power);
            super.symbol = dimension.getSymbol() + "^" + power;
        }

        this.baseDimension = dimension;
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    public BaseDimension getBaseDimension() {
        return this.baseDimension;
    }

    @Override
    protected List<PowerDimension> getPowerDimensions() {
        return Collections.singletonList(this);
    }

    @Override
    public DerivedDimension getNormalizedDimension() {
        return new DerivedDimension("", this.getPowerDimensions());
    }

    // TODO should be equivalent?
    public boolean equals(PowerDimension dimension) {
        // true if they have the same power and base dimension
        return ((this.baseDimension.equals(dimension.baseDimension)) && (this.power == dimension.power));
    }
}
