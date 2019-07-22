/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.resilience.models.mapping;

import ncsa.tools.common.exceptions.FailedComparisonException;

import java.util.*;

public class ResilienceMapper {

    private List<MatchFilterMap> mappingSets = new ArrayList<MatchFilterMap>();

    public void addMappingSet(MatchFilterMap mappingSet) {
        mappingSets.add(mappingSet);
    }

    public String getResilienceCurveFor(String schema, Map<String, Object> row, Map<String, Object> params) {
        //get all possible mapping sets that we know about for this schema
        //for each one, look at each resilience mapping set, see which is "best"

        //not sure this is best, but combine row and params to text
        Map<String, Object> combinedParams = new HashMap<>();
        combinedParams.putAll(row);
        combinedParams.putAll(params);

        Optional<PropertyMatch> matched = mappingSets.stream()
                                                     .flatMap(mappingSet -> mappingSet.getPropertyMatches().stream())
                                                     .filter(propMatch -> {
                                                         try {
                                                             return propMatch.getMatchFilter().matches(combinedParams, true);
                                                         } catch (FailedComparisonException e) {
                                                             return false;
                                                         }
                                                     })
                                                     .findFirst();

        if (matched.isPresent()) {
            if (params.containsKey("key")) {
                String mapKey = params.get("key").toString();
                return matched.get().getMap().get(mapKey);
            } else {
                // return the first resilience key
                return matched.get().getMap().values().iterator().next();
            }
        } else {
            return "";
        }
    }

    public void addMappingSet(String mappingsetId) {
        //translate mappingsetId to an actual mappingSet
        //MatchFilterMap mappingSet = loadMappingSet(mappingsetId);
        //addMappingSet(mappingSet);
    }
}
