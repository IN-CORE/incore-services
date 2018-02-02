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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Path("fragilities")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    @Inject
    public MatchFilterMap matchFilterMap;

    @Inject
    private IFragilityDAO dataAccess;

    @POST
    @Path("/map")
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

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilities() {
        List<FragilitySet> fragilitySets = dataAccess.getFragilities();

        return fragilitySets.stream()
                            .limit(100)
                            .collect(Collectors.toList());
    }

    @GET
    @Path("{fragilityId}")
    @Produces({MediaType.APPLICATION_JSON})
    public FragilitySet getFragilityById(@PathParam("fragilityId") String id) throws Exception {
        FragilitySet fragilitySet = this.dataAccess.getById(id);

        if (fragilitySet == null) {
            throw new NotFoundException();
        } else {
            return fragilitySet;
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> findFragilities(@QueryParam("text") String text) {
        List<FragilitySet> sets = this.dataAccess.searchFragilities(text);

        if(sets == null || sets.size() == 0) {
            throw new NotFoundException();
        } else {
            return sets;
        }
    }

    @GET
    @Path("/query")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> queryFragilities(@QueryParam("demand") String demandType, @QueryParam("hazard") String hazardType,
                                               @QueryParam("inventory") String inventoryType, @QueryParam("author") String author,
                                               @QueryParam("legacy_id") String legacyId) {
        Map<String, String> queryMap = new HashMap<>();

        if (legacyId != null) {
            queryMap.put("legacyId", legacyId);
        }

        if (demandType != null) {
            queryMap.put("demandType", demandType);
        }

        if (hazardType != null) {
            queryMap.put("hazardType", hazardType);
        }

        if (inventoryType != null) {
            queryMap.put("inventoryType", inventoryType);
        }

        if (author != null) {

        }

        
        List<FragilitySet> filteredSets = this.dataAccess.queryFragilities(queryMap);

        return filteredSets;
    }
}
