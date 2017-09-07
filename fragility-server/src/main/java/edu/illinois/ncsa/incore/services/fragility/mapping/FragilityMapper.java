package edu.illinois.ncsa.incore.services.fragility.mapping;

import ncsa.tools.common.exceptions.FailedComparisonException;

import java.util.*;

public class FragilityMapper {

    private List<MatchFilterMap> mappingSets = new ArrayList<MatchFilterMap>();

    public void addMappingSet(MatchFilterMap mappingSet) {
        mappingSets.add(mappingSet);
    }

    public String getFragilityFor(String schema, Map<String, Object> row, Map<String, Object> params) {
        //get all possible mapping sets that we know about for this schema
        //for each one, look at each fragility mapping set, see which is "best"

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
                // return the first fragility key
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
