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

import edu.illinois.ncsa.incore.service.dfr3.models.mapping.PropertyMatch;
import ncsa.tools.common.exceptions.ParseException;
import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.types.filters.MatchFilter;
import ncsa.tools.common.util.FilterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping {
    private Map<String, String> legacyEntry = new HashMap<>();
    private Map<String, String> entry = new HashMap<>();
    private List<List<String>> rules = new ArrayList<>();

    public Map<String, String> getLegacyEntry() {
        return legacyEntry;
    }

    public Map<String, String> getEntry() {
        return entry;
    }

    public List<List<String>> getRules() {
        return rules;
    }

    public PropertyMatch asPropertyMatch() throws ParseException {
        // Construct MatchFilter from string
        List<String> andClauses = new ArrayList<>();

        for (List<String> subRules : this.rules) {
            andClauses.add(String.join(" && ", subRules));
        }

        String rulesString = String.join(" || ", andClauses);

        try {
            MatchFilter matchFilter = FilterUtils.buildFilter(rulesString);
            // Construct PropertyMatch
            PropertyMatch propertyMatch = new PropertyMatch(this.entry, matchFilter);

            return propertyMatch;
        } catch (ReflectionException | ClassNotFoundException ex) {
            throw new ParseException("Could not parse rules string", ex);
        }
    }
}
