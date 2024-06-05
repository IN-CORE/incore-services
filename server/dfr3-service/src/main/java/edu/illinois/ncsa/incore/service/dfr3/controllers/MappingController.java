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

import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.dfr3.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRepairDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRestorationDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.Mapping;
import edu.illinois.ncsa.incore.service.dfr3.models.MappingSet;
import edu.illinois.ncsa.incore.service.dfr3.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.dfr3.utils.ServiceUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.ncsa.incore.service.dfr3.utils.CommonUtil.extractColumnsFromMapping;


@Tag(name = "Mapping")
@Path("mappings")
public class MappingController {
    private static final Logger logger = Logger.getLogger(MappingController.class);

    private final String username;
    private final List<String> groups;
    private final String userGroups;

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
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public MappingController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.userGroups = userGroups;
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets list of all inventory mappings", summary = "Apply filters to get the desired set of mappings")
    public List<MappingSet> getMappings(@Parameter(name= "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                        @Parameter(name = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                        @Parameter(name = "Data type", example = "ergo:buildingInventoryVer7") @QueryParam("dataType") String dataType,
                                        @Parameter(name = "DFR3 Mapping type", example = "fragility, restoration, repair") @QueryParam(
                                            "mappingType") String mappingType,
                                        @Parameter(name = "Creator's username") @QueryParam("creator") String creator,
                                        @Parameter(name = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        Map<String, String> queryMap = new HashMap<>();

        if (hazardType != null) {
            queryMap.put("hazardType", hazardType);
        }

        if (inventoryType != null) {
            queryMap.put("inventoryType", inventoryType);
        }

        if (dataType != null) {
            queryMap.put("dataType", dataType);
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
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a space with name " + spaceName);
            }
            if (!authorizer.canRead(username, space.getPrivileges(), groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            mappingSets = mappingSets.stream()
                .filter(mapping -> spaceMembers.contains(mapping.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            return mappingSets;
        }
        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<MappingSet> accessibleMappingSets = mappingSets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleMappingSets;
    }

    @GET
    @Path("{mappingSetId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets a mapping set by Id", summary = "Get a particular mapping set based on the id provided")
    public MappingSet getMappingSetById(@Parameter(name = "mapping id", example = "5b47b2d9337d4a36187c7563") @PathParam("mappingSetId") String id) {
        Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(id);

        if (mappingSet.isPresent()) {
            MappingSet actual = mappingSet.get();
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                actual.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return actual;
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You don't have authorization to access the mapping " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a mapping with id " + id);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Create an inventory mapping", summary = "Post a json that represents mapping between inventory's attributes and " +
        "DFR3 object sets")
    public MappingSet uploadMapping(@Parameter(name = "json representing the fragility mapping") MappingSet mappingSet) {

        UserInfoUtils.throwExceptionIfIdPresent(mappingSet.getId());

        // check if the user has the quota to put it in
        Boolean postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "dfr3");

        if (!postOk) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DFR3_ALLOCATION_MESSAGE);
        }

        List<Mapping> mappings = mappingSet.getMappings();
        Set<String> columnSet = new HashSet<>();

        int idx = 0;
        String prevRuleClassName = "";
        // This validates if the format of the "rules" being submitted is an Array or Hash. It is needed because we made "rules" attribute
        // accept any Object in INCORE1-1153. This can be removed when the existing mappings in DFR3 database are updated to new format
        // and we need not support backward compatibility. Also, validates if all rules are of the same format (either Array or HashMap)
        for (Mapping mapping : mappings) {
            if (!(mapping.getRules() instanceof java.util.ArrayList) && !(mapping.getRules() instanceof java.util.HashMap)) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "At least one of the provided mapping rules' format is " +
                    "incorrect");
            }

            if (idx == 0) {
                prevRuleClassName = mapping.getRules().getClass().getName();
            } else {
                if (!prevRuleClassName.equals(mapping.getRules().getClass().getName())) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "All rules in the mapping should be in the same format");
                }
                prevRuleClassName = mapping.getRules().getClass().getName();
            }
            idx++;

            // get unique column names
            if (mapping.getRules() instanceof ArrayList) {
                extractColumnsFromMapping((ArrayList<?>) mapping.getRules(), columnSet);
            }
            else if(mapping.getRules() instanceof HashMap) {
                extractColumnsFromMapping((HashMap<?, ?>) mapping.getRules(), columnSet);
            }
        }

        List<String> uniqueColumns = new ArrayList<>(columnSet);

        // check if the parameters matches the defined data type in semantics
        String dataType = mappingSet.getDataType();
        if (dataType == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "dataType is a required field.");
        }
        try {
            String semanticsDefinition = ServiceUtil.getJsonFromSemanticsEndpoint(dataType, username, userGroups);
            List<String> columns = CommonUtil.getColumnNames(semanticsDefinition);

            // parse mapping rules to find column names
            uniqueColumns.forEach((uniqueColumn) -> {
                if(!columns.contains(uniqueColumn)) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Column: " + uniqueColumn + " in the Mapping Rules not found in the dataType: " + dataType);
                }
            });

        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not check if the column in the mapping rules matches the dataType columns.");
        }


        mappingSet.setCreator(username);
        mappingSet.setOwner(username);

        String id = this.mappingDAO.saveMappingSet(mappingSet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(id);
        spaceRepository.addSpace(space);
        mappingSet.setSpaces(spaceRepository.getSpaceNamesOfMember(id));

        // add dfr3 in the usage
        AllocationUtils.increaseUsage(allocationsRepository, username, "dfr3");

        return mappingSet;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{mappingId}")
    @Operation(summary = "Deletes a mapping by id")
    public MappingSet deleteMappingById(@Parameter(name = "mapping id", example = "5b47b2d8337d4a36187c6727") @PathParam("mappingId") String id) {
        Optional<MappingSet> mappingSet = this.mappingDAO.getMappingSetById(id);

        if (mappingSet.isPresent()) {
            Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (this.username.equals(mappingSet.get().getOwner()) || isAdmin) {
//              remove id from spaces
                List<Space> spaces = spaceRepository.getAllSpaces();
                for (Space space : spaces) {
                    if (space.hasMember(id)) {
                        space.removeMember(id);
                        spaceRepository.addSpace(space);
                    }
                }

                // remove dfr3 in the usage
                AllocationUtils.decreaseUsage(allocationsRepository, username, "dfr3");

                return this.mappingDAO.deleteMappingSetById(id);
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to delete the mapping" +
                    " with id " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a mapping set with id " + id);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Search for a text in all mappings", summary = "Gets all mappings that contain a specific text")
    public List<MappingSet> findMappings(@Parameter(name = "Text to search by", example = "steel") @QueryParam("text") String text,
                                         @Parameter(name = "DFR3 Mapping type", example = "fragility, restoration, repair") @QueryParam(
                                             "mappingType") String mappingType,
                                         @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                         @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        try {
            List<MappingSet> sets = new ArrayList<>();
            Optional<MappingSet> ms = this.mappingDAO.getMappingSetById(text);
            if (ms.isPresent()) {
                sets.add(ms.get());
            } else {
                sets = this.mappingDAO.searchMappings(text, mappingType);
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

            List<MappingSet> accessibleMappings = sets.stream()
                .filter(b -> membersSet.contains(b.getId()))
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return accessibleMappings;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Unknown Server error occured");
        }
    }

}
