/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.controllers;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.service.dfr3.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRepairDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRestorationDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.*;
import edu.illinois.ncsa.incore.service.dfr3.models.dto.MappingRequest;
import edu.illinois.ncsa.incore.service.dfr3.models.dto.MappingResponse;
import edu.illinois.ncsa.incore.service.dfr3.models.mapping.Dfr3Mapper;
import edu.illinois.ncsa.incore.service.dfr3.models.mapping.MatchFilterMap;

import edu.illinois.ncsa.incore.common.models.UserInfo;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import ncsa.tools.common.exceptions.ParseException;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "mappings", authorizations = {})
@Path("mappings")
public class MappingController {
    private static final Logger logger = Logger.getLogger(MappingController.class);

    @Inject
    private IMappingDAO mappingDAO;
    @Inject
    private IFragilityDAO fragilityDAO;

    @Inject
    private IRepairDAO repairDAO;

    @Inject
    private IRestorationDAO restorationDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets list of all inventory mappings", notes = "Apply filters to get the desired set of mappings")
    public List<MappingSet> getMappings(@HeaderParam("x-auth-userinfo") String userInfo,
                                        @ApiParam(value = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                        @ApiParam(value = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                        @ApiParam(value = "DFR3 Mapping type", example = "fragility, restoration, repair") @QueryParam("mappingType") String mappingType,
                                        @ApiParam(value = "Creator's username") @QueryParam("creator") String creator,
                                        @ApiParam(value = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

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

            if (mappingType != null) {
                queryMap.put("mappingType", mappingType);
            }

            List<MappingSet> mappingSets;

            if (queryMap.isEmpty()) {
                mappingSets = this.mappingDAO.getMappingSets();
            } else {
                mappingSets = this.mappingDAO.queryMappingSets(queryMap);
            }
            if (!spaceName.equals("")) {
                Space space = spaceRepository.getSpaceByName(spaceName);
                if (space == null) {
                    throw new NotFoundException();
                }
                if (!authorizer.canRead(username, space.getPrivileges())) {
                    throw new NotAuthorizedException(username + " is not authorized to read the space " + spaceName);
                }
                List<String> spaceMembers = space.getMembers();

                mappingSets = mappingSets.stream()
                    .filter(mapping -> spaceMembers.contains(mapping.getId()))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
                if (mappingSets.size() == 0) {
                    throw new NotFoundException("No mappings were found in space " + spaceName);
                }
                return mappingSets;
            }
            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

            return mappingSets.stream()
                .filter(b -> membersSet.contains(b.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @GET
    @Path("{mappingSetId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a mapping set by Id", notes = "Get a particular mapping set based on the id provided")
    public MappingSet getMappingSetById(@HeaderParam("x-auth-userinfo") String userInfo,
                                        @ApiParam(value = "mapping id", example = "5b47b2d9337d4a36187c7563") @PathParam("mappingSetId") String id) {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

            ObjectMapper objectMapper = new ObjectMapper();
            try{
                UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
                String username = user.getPreferredUsername();


                Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(id);

            if (mappingSet.isPresent()) {
                MappingSet actual = mappingSet.get();
                if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces())) {
                    return actual;
                }
                throw new ForbiddenException();
            } else {
                throw new NotFoundException();
            }
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create an inventory mapping", notes = "Post a json that represents mapping between inventory's attributes and DFR3 object sets")
    public MappingSet uploadMapping(@HeaderParam("x-auth-userinfo") String userInfo,
                                    @ApiParam(value="json representing the fragility mapping") MappingSet mappingSet) {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try{
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

            mappingSet.setCreator(username);

            String id = this.mappingDAO.saveMappingSet(mappingSet);

            Space space = spaceRepository.getSpaceByName(username);
            if (space == null) {
                space = new Space(username);
                space.setPrivileges(Privileges.newWithSingleOwner(username));
            }
            space.addMember(id);
            spaceRepository.addSpace(space);

            return mappingSet;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @POST
    @Path("{mappingSetId}/matched")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Map each inventory to a DFR3 object Id based on the input mapping Id",
        notes = "Returns a json where key is the inventory id that is mapped to a DFR3 object id based on the input mapping id")
    public MappingResponse mapFragilities(@HeaderParam("x-auth-userinfo") String userInfo,
                                          @PathParam("mappingSetId") String mappingSetId,
                                          MappingRequest mappingRequest) throws ParseException {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try{
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

            Map<String, DFR3Set> setJsonMap = new HashMap<>();
            Map<String, String> setIdMap = new HashMap<>();

            List<Space> allSpaces = spaceRepository.getAllSpaces();

            boolean canReadMapping = authorizer.canUserReadMember(username, mappingSetId, allSpaces);
            if (!canReadMapping) {
                throw new ForbiddenException();
            }

            Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(mappingSetId);

            if (!mappingSet.isPresent()) {
                throw new BadRequestException();
            }

            String mappingType = mappingSet.get().getMappingType();

            MatchFilterMap matchFilterMap = mappingSet.get().asMatchFilterMap();

            Dfr3Mapper mapper = new Dfr3Mapper();

            mapper.addMappingSet(matchFilterMap);

            List<Feature> features = new ArrayList<>();

            if (mappingRequest.mappingSubject.inventory instanceof FeatureCollection) {
                features = ((FeatureCollection) mappingRequest.mappingSubject.inventory).getFeatures();
            }

            if (mappingRequest.mappingSubject.inventory instanceof Feature) {
                features = new ArrayList<>();
                features.add((Feature) mappingRequest.mappingSubject.inventory);
            }

            Map<String, DFR3Set> queriedDfr3Set = new HashMap<>();
            for (Feature feature : features) {
                String setKey = mapper.getDfr3CurveFor(mappingRequest.mappingSubject.schemaType.toString(),
                    feature.getProperties(), mappingRequest.parameters);

                if (ObjectId.isValid(setKey)) {
                    DFR3Set currSet = null;
                    if (queriedDfr3Set.containsKey(setKey)) {
                        currSet = queriedDfr3Set.get(setKey);
                    } else {
                        if (mappingType.equalsIgnoreCase("fragility")) {
                            Optional<FragilitySet> fragilitySet = this.fragilityDAO.getFragilitySetById(setKey);

                            if (fragilitySet.isPresent()) {
                                if (authorizer.canUserReadMember(username, setKey, allSpaces)) {
                                    currSet = fragilitySet.get();
                                }
                                queriedDfr3Set.put(setKey, currSet);
                            }
                        } else if (mappingType.equalsIgnoreCase("repair")) {
                            Optional<RepairSet> repairSet = this.repairDAO.getRepairSetById(setKey);

                            if (repairSet.isPresent()) {
                                if (authorizer.canUserReadMember(username, setKey, allSpaces)) {
                                    currSet = repairSet.get();
                                }
                                queriedDfr3Set.put(setKey, currSet);
                            }
                        } else if (mappingType.equalsIgnoreCase("restoration")) {
                            Optional<RestorationSet> restorationSet = this.restorationDAO.getRestorationSetById(setKey);

                            if (restorationSet.isPresent()) {
                                if (authorizer.canUserReadMember(username, setKey, allSpaces)) {
                                    currSet = restorationSet.get();
                                }
                                queriedDfr3Set.put(setKey, currSet);
                            }
                        }
                    }

                    // If we found a matching object and user has read access to it
                    if (currSet != null) {
                        setJsonMap.put(setKey, currSet);
                        setIdMap.put(feature.getId(), setKey);
                    }
                }
            }

            // Construct response
            MappingResponse mappingResponse = new MappingResponse(setJsonMap, setIdMap);

            return mappingResponse;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all mappings", notes = "Gets all mappings that contain a specific text")
    public List<MappingSet> findMappings(@HeaderParam("x-auth-userinfo") String userInfo,
                                              @ApiParam(value = "Text to search by", example = "steel") @QueryParam("text") String text,
                                              @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                              @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

            List<MappingSet> sets = new ArrayList<>();
            Optional<MappingSet> ms = this.mappingDAO.getMappingSetById(text);
            if (ms.isPresent()) {
                sets.add(ms.get());
            } else {
                sets = this.mappingDAO.searchMappings(text);
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

            List<MappingSet> accessibleMappings = sets.stream()
                .filter(b -> membersSet.contains(b.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

            return accessibleMappings;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

}
