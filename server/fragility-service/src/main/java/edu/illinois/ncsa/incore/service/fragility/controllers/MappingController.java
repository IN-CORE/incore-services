/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.controllers;

import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import edu.illinois.ncsa.incore.service.fragility.models.MappingRequest;
import edu.illinois.ncsa.incore.service.fragility.models.MappingResponse;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.FragilityMapper;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import org.apache.log4j.Logger;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("mapping")
public class MappingController {
    private static final Logger logger = Logger.getLogger(MappingController.class);

    @Inject
    public MatchFilterMap matchFilterMap;

    @Inject
    private IFragilityDAO dataAccess;

    @POST
    @Path("/match")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public MappingResponse mapFragilities(MappingRequest mappingRequest) {
        // load all available fragilities
        // TODO should filter based on schema (e.g. building, bridge, etc.) and any additional criteria
        List<FragilitySet> fragilitySets = dataAccess.getFragilities();

        Map<String, FragilitySet> fragilitySetMap = new HashMap<>();
        Map<String, String> fragilityMap = new HashMap<>();

        FragilityMapper mapper = new FragilityMapper();

        mapper.addMappingSet(matchFilterMap);

        List<Feature> features = new ArrayList<>();

        if (mappingRequest.mappingSubject.inventory instanceof FeatureCollection) {
            features = ((FeatureCollection) mappingRequest.mappingSubject.inventory).getFeatures();
        }

        if (mappingRequest.mappingSubject.inventory instanceof Feature) {
            features = new ArrayList<>();
            features.add((Feature) mappingRequest.mappingSubject.inventory);
        }

        for (Feature feature : features) {
            String fragilityKey = mapper.getFragilityFor(mappingRequest.mappingSubject.schemaType.toString(), feature.getProperties(),
                                                         mappingRequest.parameters);

            Optional<FragilitySet> fragilityMatch = fragilitySets.stream()
                                                                 .filter(set -> set.getLegacyId().equals(fragilityKey))
                                                                 .findFirst();

            if (fragilityMatch.isPresent()) {
                FragilitySet fragilitySet = fragilityMatch.get();
                fragilitySetMap.put(fragilitySet.getLegacyId(), fragilitySet);
                fragilityMap.put(feature.getId(), fragilitySet.getLegacyId());
            }
        }

        // Construct response
        MappingResponse mappingResponse = new MappingResponse(fragilitySetMap, fragilityMap);

        return mappingResponse;
    }
}
