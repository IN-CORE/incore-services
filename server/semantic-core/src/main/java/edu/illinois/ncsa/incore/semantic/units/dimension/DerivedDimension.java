/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.dimension;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a derived dimension, which is derived from the product of base dimensions raised to a power.
 */
public class DerivedDimension extends Dimension {
    private Map<BaseDimension, Integer> powerDimensions = new LinkedHashMap<>();

    //region Constructors
    public DerivedDimension(String name, Map<BaseDimension, Integer> entries) {
        super(name);
        this.powerDimensions = entries;

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension... dimensions) {
        super(name);

        for (BaseDimension dimension : dimensions) {
            this.addDimension(dimension, 1);
        }

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, List<PowerDimension> dimensions) {
        super(name);

        for (PowerDimension dimension : dimensions) {
            this.addDimension(dimension);
        }

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(List<PowerDimension> dimensions) {
        this("", dimensions);
    }

    public DerivedDimension(String name, BaseDimension dimension, int power) {
        super(name);

        this.addDimension(dimension, power);

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2) {
        super(name);

        this.addDimension(dimension1, power1);
        this.addDimension(dimension2, power2);

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2) {
        this(name, dimension1, 1, dimension2, power2);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3) {
        super(name);

        this.addDimension(dimension1, power1);
        this.addDimension(dimension2, power2);
        this.addDimension(dimension3, power3);

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        super(name);

        this.addDimension(dimension1, power1);
        this.addDimension(dimension2, power2);
        this.addDimension(dimension3, power3);
        this.addDimension(dimension4, power4);

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, power1, dimension2, 1, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3, dimension4, power4);
    }

    public DerivedDimension(String name, BaseDimension dimension1, int power1, BaseDimension dimension2, int power2,
                            BaseDimension dimension3, int power3, BaseDimension dimension4, int power4, BaseDimension dimension5,
                            int power5) {
        super(name);

        this.addDimension(dimension1, power1);
        this.addDimension(dimension2, power2);
        this.addDimension(dimension3, power3);
        this.addDimension(dimension4, power4);
        this.addDimension(dimension5, power5);

        super.symbol = initializeSymbol();
        super.unicodeSymbol = initializeUnicodeSymbol();
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, int power2, BaseDimension dimension3,
                            int power3, BaseDimension dimension4, int power4, BaseDimension dimension5, int power5) {
        this(name, dimension1, 1, dimension2, power2, dimension3, power3, dimension4, power4, dimension5, power5);
    }

    public DerivedDimension(String name, BaseDimension dimension1, BaseDimension dimension2, BaseDimension dimension3,
                            int power3, BaseDimension dimension4, int power4, BaseDimension dimension5, int power5) {
        this(name, dimension1, 1, dimension2, 1, dimension3, power3, dimension4, power4, dimension5, power5);
    }
    //endregion

    public boolean equals(DerivedDimension dimension) {
        return this.getPowerDimensions().containsAll(dimension.getPowerDimensions());
    }

    @Override
    protected List<PowerDimension> getPowerDimensions() {
        List<PowerDimension> baseDimensions = new ArrayList<>();

        for (Map.Entry<BaseDimension, Integer> powerDimension : powerDimensions.entrySet()) {
            baseDimensions.add(new PowerDimension(powerDimension.getKey(), powerDimension.getValue()));
        }

        return baseDimensions;
    }

    @Override
    protected DerivedDimension getNormalizedDimension() {
        return this;
    }

    /**
     * Adds a new power dimension to the list of dimensions
     * This will evaluate existing dimensions e.g. (L A L) => L^2 A
     */
    private void addDimension(PowerDimension dimension) {
        this.addDimension(dimension.getBaseDimension(), dimension.getPower());
    }

    /**
     * Adds a new power dimension to the current list of dimensions.
     * This will evaluate existing dimensions e.g. (L A L) => L^2 A
     */
    private void addDimension(BaseDimension dimension, int power) {
        if (this.powerDimensions.containsKey(dimension)) {
            int currentPower = this.powerDimensions.get(dimension);
            this.powerDimensions.put(dimension, currentPower + power);
        } else {
            this.powerDimensions.put(dimension, power);
        }
    }

    /**
     * Creates the symbol for the derived dimension
     */
    private String initializeSymbol() {
        List<String> symbols = new ArrayList<>();

        List<PowerDimension> powerDimensions = this.getPowerDimensions();

        List<PowerDimension> sortedDimensions = powerDimensions.stream()
            .sorted(Comparator.comparingInt(
                dim -> dim.getBaseDimension().getSortOrder()))
            .collect(Collectors.toList());

        for (PowerDimension sortedDimension : sortedDimensions) {
            symbols.add(sortedDimension.getSymbol());
        }

        return String.join(" ", symbols);
    }

    /**
     * Creates the unicode symbol for the derived dimension
     */
    private String initializeUnicodeSymbol() {
        List<String> symbols = new ArrayList<>();

        List<PowerDimension> powerDimensions = this.getPowerDimensions();

        List<PowerDimension> sortedDimensions = powerDimensions.stream()
            .sorted(Comparator.comparingInt(
                dim -> dim.getBaseDimension().getSortOrder()))
            .collect(Collectors.toList());

        for (PowerDimension sortedDimension : sortedDimensions) {
            symbols.add(sortedDimension.getUnicodeSymbol());
        }

        return String.join("\u22c5", symbols);
    }
}
