/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models;

import java.util.List;

public class FragilitySetBuilder {
    private String legacyId;
    private String description;
    private List<String> authors;
    private PaperReference paperReference;
    private String resultUnit;
    private String resultType;
    private String demandType;
    private String demandUnits;
    private String hazardType;
    private String inventoryType;
    private List<FragilityCurve> fragilityCurves;

    public FragilitySetBuilder setLegacyId(String legacyId) {
        this.legacyId = legacyId;
        return this;
    }

    public FragilitySetBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public FragilitySetBuilder setAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    public FragilitySetBuilder setPaperReference(PaperReference paperReference) {
        this.paperReference = paperReference;
        return this;
    }

    public FragilitySetBuilder setResultUnit(String resultUnit) {
        this.resultUnit = resultUnit;
        return this;
    }

    public FragilitySetBuilder setResultType(String resultType) {
        this.resultType = resultType;
        return this;
    }

    public FragilitySetBuilder setDemandType(String demandType) {
        this.demandType = demandType;
        return this;
    }

    public FragilitySetBuilder setDemandUnits(String demandUnits) {
        this.demandUnits = demandUnits;
        return this;
    }

    public FragilitySetBuilder setHazardType(String hazardType) {
        this.hazardType = hazardType;
        return this;
    }

    public FragilitySetBuilder setInventoryType(String inventoryType) {
        this.inventoryType = inventoryType;
        return this;
    }

    public FragilitySetBuilder setFragilityCurves(List<FragilityCurve> fragilityCurves) {
        this.fragilityCurves = fragilityCurves;
        return this;
    }

    public FragilitySet build() {
        return new FragilitySet(legacyId, description, authors, paperReference, resultUnit, resultType, demandType, demandUnits,
                                hazardType, inventoryType, fragilityCurves);
    }
}
