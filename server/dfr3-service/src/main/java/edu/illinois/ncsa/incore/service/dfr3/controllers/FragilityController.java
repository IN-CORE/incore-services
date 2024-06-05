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
import edu.illinois.ncsa.incore.common.SemanticsConstants;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ICommonRepository;
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
import edu.illinois.ncsa.incore.service.dfr3.models.FragilitySet;

import edu.illinois.ncsa.incore.service.dfr3.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.dfr3.utils.ServiceUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;


import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.ncsa.incore.service.dfr3.utils.ValidationUtils.isDemandValid;


@OpenAPIDefinition(
    info = @Info(
        version = "1.26.1",
        description = "IN-CORE Service For Fragilities and Fragility mappings",

        title = "IN-CORE v2 Fragility Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore.ncsa.illinois.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    )

//    consumes = {"application/json"},
//    produces = {"application/json"},
//    schemes = {SwaggerDefinition.Scheme.HTTP}
)


@Tag(name = "fragilities")
@Path("fragilities")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    IAuthorizer authorizer;
    @Inject
    private IFragilityDAO fragilityDAO;
    @Inject
    private IMappingDAO mappingDAO;
    @Inject
    private ISpaceRepository spaceRepository;
    @Inject
    private ICommonRepository commonRepository;
    @Inject
    private IUserAllocationsRepository allocationsRepository;
    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    public FragilityController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
        ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.userGroups = userGroups;
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets list of fragilities", summary = "Apply filters to get the desired set of fragilities")
    public List<FragilitySet> getFragilities(@Parameter(name = "demand type filter", example = "PGA") @QueryParam("demand") String demandType,
                                             @Parameter(name = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                             @Parameter(name = "Inventory type filter", example = "building") @QueryParam("inventory") String inventoryType,
                                             @Parameter(name = "Data type filter", example = "ergo:buildingInventoryVer7") @QueryParam("dataType") String dataType,
                                             @Parameter(name = "not implemented", hidden = true) @QueryParam("author") String author,
                                             @Parameter(name = "Legacy fragility Id from v1") @QueryParam("legacy_id") String legacyId,
                                             @Parameter(name = "Fragility creator's username") @QueryParam("creator") String creator,
                                             @Parameter(name = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                             @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                             @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        Map<String, String> queryMap = new HashMap<>();

        if (legacyId != null) {
            queryMap.put("legacyId", legacyId);
        }

        if (demandType != null) {
            queryMap.put("demandTypes", demandType);
        }

        if (hazardType != null) {
            queryMap.put("hazardType", hazardType);
        }

        if (inventoryType != null) {
            queryMap.put("inventoryType", inventoryType);
        }

        if (dataType != null){
            queryMap.put("dataType", dataType);
        }

        if (creator != null) {
            queryMap.put("creator", creator);
        }

        List<FragilitySet> fragilitySets;

        if (queryMap.isEmpty()) {
            fragilitySets = this.fragilityDAO.getFragilities();
        } else {
            fragilitySets = this.fragilityDAO.queryFragilities(queryMap);
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

            fragilitySets = fragilitySets.stream()
                .filter(fragility -> spaceMembers.contains(fragility.getId()))
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return fragilitySets;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<FragilitySet> accessibleFragilities = fragilitySets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleFragilities;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Create a fragility set", summary = "Post a fragility set to the dfr3 service")
    public FragilitySet uploadFragilitySet(@Parameter(name = "json representing the fragility set") FragilitySet fragilitySet) {

        UserInfoUtils.throwExceptionIfIdPresent(fragilitySet.getId());

        // check if the user has the quota to put it in
        Boolean postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "dfr3");

        if (!postOk) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DFR3_ALLOCATION_MESSAGE);
        }

        // check if demand type is correct according to the definition; for now get the first definition
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        String hazardType = fragilitySet.getHazardType();
        List<String> demandTypes = fragilitySet.getDemandTypes();
        List<String> demandUnits = fragilitySet.getDemandUnits();

        // check if size of demand type matches the unit
        if (demandTypes.size() != demandUnits.size()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Demand Types should match the shape of Demand Units!");
        } else {
            Iterator<String> dt = demandTypes.iterator();
            Iterator<String> du = demandUnits.iterator();
            while (dt.hasNext() && du.hasNext()) {
                String demandType = dt.next().toLowerCase();
                String demandUnit = du.next().toLowerCase();
                JSONArray listOfDemands = demandDefinition.getJSONArray(hazardType);

                HashMap<String, Boolean> matched = isDemandValid(demandType, demandUnit, listOfDemands);
                if (!matched.get("demandTypeExisted")) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Demand type: " + demandType + " not allowed.\n Allowed " +
                        "demand types and units are: " + listOfDemands);
                } else if (!matched.get("demandUnitAllowed")) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                        "Demand unit: " + demandUnit + " does not match the definition.\n " +
                            "Allowed demand types and units are: " + listOfDemands);
                }
            }
        }
        String dataType = fragilitySet.getDataType();
        String inventoryType = fragilitySet.getInventoryType();
        if (dataType == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "dataType is a required field.");
        }
        try {
            String semanticsDefinition = ServiceUtil.getJsonFromSemanticsEndpoint(dataType, username, userGroups);
            List<String> columns = CommonUtil.getColumnNames(semanticsDefinition);

            fragilitySet.getCurveParameters().forEach((params) -> {
                // Only check curve parameter if it does not belong to a part of the demand type
                if (!demandTypes.contains(params.fullName) && !demandUnits.contains(params.name)) {
                    // Check if inventoryType is "building" and the column is not reserved
                    boolean isBuildingAndNotReserved = "building".equals(inventoryType) && SemanticsConstants.RESERVED_COLUMNS.contains(params.name);

                    // If it's not a building parameter that is reserved, check if it's in the columns
                    if (!isBuildingAndNotReserved && !columns.contains(params.name)) {
                        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Curve parameter: " + params.name + " not found in the dataType: " + dataType);
                    }
                }
            });

        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not check the fragility curve parameter matches the dataType columns.");
        }


        fragilitySet.setCreator(username);
        fragilitySet.setOwner(username);

        if (fragilitySet.getFragilityCurves().size() == 0){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No fragility curves are included in the json. " +
                "Please provide at least one.");
        }
        String fragilityId = this.fragilityDAO.saveFragility(fragilitySet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(fragilityId);
        spaceRepository.addSpace(space);
        fragilitySet.setSpaces(spaceRepository.getSpaceNamesOfMember(fragilitySet.getId()));

        // add dfr3 in the usage
        AllocationUtils.increaseUsage(allocationsRepository, username, "dfr3");

        return fragilitySet;
    }

    @GET
    @Path("{fragilityId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets a fragility by Id", summary = "Get a particular fragility based on the id provided")
    public FragilitySet getFragilityById
        (@Parameter(name = "fragility id", example = "5b47b2d8337d4a36187c6727") @PathParam("fragilityId") String id) {
        Optional<FragilitySet> fragilitySet = this.fragilityDAO.getFragilitySetById(id);
        if (fragilitySet.isPresent()) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                FragilitySet fs = fragilitySet.get();
                fs.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return fs;
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "fragility with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a fragility set with id " + id);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{fragilityId}")
    @Operation(summary = "Deletes a fragility by id")
    public FragilitySet deleteFragilityById(@Parameter(name = "fragility id", example = "5b47b2d8337d4a36187c6727") @PathParam(
        "fragilityId") String id) {
        Optional<FragilitySet> fragilitySet = this.fragilityDAO.getFragilitySetById(id);

        if (fragilitySet.isPresent()) {
            Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (this.username.equals(fragilitySet.get().getOwner()) || isAdmin) {
//                Check for references in mappings, if found give 409
                if (this.mappingDAO.isCurvePresentInMappings(id)) {
                    throw new IncoreHTTPException(Response.Status.CONFLICT, "The fragility is referenced in at least one DFR3 mapping" +
                        ". It" +
                        " can not be deleted until" +
                        " all its references are removed from mappings");
                } else {
//                    remove id from spaces
                    List<Space> spaces = spaceRepository.getAllSpaces();
                    for (Space space : spaces) {
                        if (space.hasMember(id)) {
                            space.removeMember(id);
                            spaceRepository.addSpace(space);
                        }
                    }

                    // remove dfr3 in the usage
                    AllocationUtils.decreaseUsage(allocationsRepository, username,  "dfr3");

                    return this.fragilityDAO.deleteFragilitySetById(id);
                }
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to delete the " +
                    "fragility with id " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a fragility set with id " + id);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Search for a text in all fragilities", summary = "Gets all fragilities that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No fragilities found with the searched text")
    })
    public List<FragilitySet> findFragilities(@Parameter(name = "Text to search by", example = "steel") @QueryParam("text") String text,
                                              @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                              @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        List<FragilitySet> sets = new ArrayList<>();
        Optional<FragilitySet> fs = this.fragilityDAO.getFragilitySetById(text);
        if (fs.isPresent()) {
            sets.add(fs.get());
        } else {
            sets = this.fragilityDAO.searchFragilities(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<FragilitySet> accessibleFragilities = sets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleFragilities;

    }

}
