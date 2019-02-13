/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units;

public enum UnitSystem {
    Imperial("Imperial"),
    USCustomary("US Customary"),
    CentimetreGramSecond("Centimetre-Gram-Second"),
    SI("SI"),
    Unspecified("Unspecified");

    private String name;

    private UnitSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
