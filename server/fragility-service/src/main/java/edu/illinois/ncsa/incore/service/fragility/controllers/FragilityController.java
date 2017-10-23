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
import edu.illinois.ncsa.incore.service.fragility.models.MappingRequest;
import edu.illinois.ncsa.incore.service.fragility.models.MappingResponse;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.FragilityMapper;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
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

    // TODO Should replace the controller action path params with query params
    @GET
    @Path("{fragilityId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilityById(@PathParam("fragilityId") String id) throws Exception {
        return this.getFragilityByAttributeType("_id", id);
    }

    @GET
    @Path("/demand/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilityByDemandType(@PathParam("type") String type) throws Exception {
        return this.getFragilityByAttributeType("demandType", type);
    }

    @GET
    @Path("/hazard/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilityByHazardType(@PathParam("type") String type) throws Exception {
        return this.getFragilityByAttributeType("hazardType", type);
    }

    @GET
    @Path("/inventory/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilityByInventoryType(@PathParam("type") String type) throws Exception {
        return this.getFragilityByAttributeType("inventoryType", type);
    }

    @GET
    @Path("/author/{author}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FragilitySet> getFragilityByAuthor(@PathParam("author") String author) throws Exception {
        List<FragilitySet> sets = this.dataAccess.queryFragilityAuthor(author);

        if(sets.size() > 0) {
            return sets;
        } else {
            throw new NotFoundException("Could not find fragilities with author of " + author);
        }
    }

    private List<FragilitySet> getFragilityByAttributeType(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.dataAccess.queryFragilities(attributeType, attributeValue);

        if (sets.size() > 0) {
            return sets;
        } else {
            throw new NotFoundException("Could not find fragilities with " + attributeType + " = " + attributeValue);
        }
    }
}
