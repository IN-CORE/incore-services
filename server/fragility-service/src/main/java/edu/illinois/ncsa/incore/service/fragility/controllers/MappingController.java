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

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import edu.illinois.ncsa.incore.service.fragility.models.MappingSet;
import edu.illinois.ncsa.incore.service.fragility.models.dto.MappingRequest;
import edu.illinois.ncsa.incore.service.fragility.models.dto.MappingResponse;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.FragilityMapper;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import io.swagger.annotations.Api;
import ncsa.tools.common.exceptions.ParseException;
import org.apache.log4j.Logger;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Api(value="mappings", authorizations = {})
@Path("mappings")
public class MappingController {
    private static final Logger logger = Logger.getLogger(MappingController.class);

    @Inject
    private IMappingDAO mappingDAO;
    @Inject
    private IFragilityDAO fragilityDAO;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<MappingSet> getMappings(@HeaderParam("X-Credential-Username") String username,
                                        @QueryParam("hazard") String hazardType,
                                        @QueryParam("inventory") String inventoryType,
                                        @QueryParam("creator") String creator,
                                        @QueryParam("skip") int offset,
                                        @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        if (hazardType != null) {
            queryMap.put("hazardType", hazardType);
        }

        if (inventoryType != null) {
            queryMap.put("inventoryType", inventoryType);
        }

        if (creator != null) {
            queryMap.put("creator", creator);
        }

        List<MappingSet> mappingSets;

        if (queryMap.isEmpty()) {
            mappingSets = this.mappingDAO.getMappingSets()
                .stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        } else {
            mappingSets = this.mappingDAO.queryMappingSets(queryMap, offset, limit);
        }

        return mappingSets.stream()
            .filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());
    }

    @GET
    @Path("{mappingSetId}")
    @Produces({MediaType.APPLICATION_JSON})
    public MappingSet getMappingSetById(@HeaderParam("X-Credential-Username") String username, @PathParam("mappingSetId") String id) {
        Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(id);

        if (mappingSet.isPresent()) {
            MappingSet actual = mappingSet.get();
            if (authorizer.canRead(username, actual.getPrivileges())) {
                return actual;
            } else {
                throw new ForbiddenException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public MappingSet uploadMapping(@HeaderParam("X-Credential-Username") String username, MappingSet mappingSet) {
        mappingSet.setPrivileges(Privileges.newWithSingleOwner(username));
        mappingSet.setCreator(username);
        this.mappingDAO.saveMappingSet(mappingSet);
        return mappingSet;
    }

    @POST
    @Path("{mappingSetId}/matched")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public MappingResponse mapFragilities(@HeaderParam("X-Credential-Username") String username,
                                          @PathParam("mappingSetId") String mappingSetId,
                                          MappingRequest mappingRequest) throws ParseException {

        List<FragilitySet> fragilitySets = this.fragilityDAO.getCachedFragilities().stream()
            .filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());


        Map<String, FragilitySet> fragilitySetMap = new HashMap<>();
        Map<String, String> fragilityMap = new HashMap<>();

        Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(mappingSetId);

        if (!mappingSet.isPresent()) {
            throw new BadRequestException();
        }

        MatchFilterMap matchFilterMap = mappingSet.get().asMatchFilterMap();

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
            String fragilityKey = mapper.getFragilityFor(mappingRequest.mappingSubject.schemaType.toString(),
                feature.getProperties(), mappingRequest.parameters);

            Optional<FragilitySet> fragilityMatch = fragilitySets.stream()
                .filter(set -> set.getId().equals(fragilityKey))
                .findFirst();

            if (fragilityMatch.isPresent()) {
                FragilitySet fragilitySet = fragilityMatch.get();
                fragilitySetMap.put(fragilitySet.getId(), fragilitySet);
                fragilityMap.put(feature.getId(), fragilitySet.getId());
            }
        }

        // Construct response
        MappingResponse mappingResponse = new MappingResponse(fragilitySetMap, fragilityMap);

        return mappingResponse;
    }
}
