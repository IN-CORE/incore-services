/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Embedded;

import java.util.HashMap;
import java.util.Map;

@Embedded
public class Mapping {
    private Map<String, String> legacyEntry = new HashMap<>();
    private Map<String, String> entry = new HashMap<>();
    private Object rules = new Object();

    public Map<String, String> getLegacyEntry() {
        return legacyEntry;
    }

    public Map<String, String> getEntry() {
        return entry;
    }

    public Object getRules() {
        return rules;
    }

}
