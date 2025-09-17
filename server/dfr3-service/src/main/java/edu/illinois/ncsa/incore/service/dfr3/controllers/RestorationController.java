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
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRestorationDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;


@OpenAPIDefinition(
    info = @Info(
        version = "1.30.0",
        description = "IN-CORE Service For Restoration and Restoration mappings",

        title = "IN-CORE v2 DFR3 Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://tools.in-core.org"
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


@Tag(name = "restorations")
@Path("restorations")
public class RestorationController {
    private static final Logger logger = Logger.getLogger(RestorationController.class);

    private final String username;
    private final List<String> groups;

    @Inject
    IAuthorizer authorizer;
    @Inject
    private IRestorationDAO restorationDAO;
    @Inject
    IMappingDAO mappingDAO;
    @Inject
    private ISpaceRepository spaceRepository;
    @Inject
    private IUserAllocationsRepository allocationsRepository;
    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    public RestorationController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets list of restorations", description = "Apply filters to get the desired set of restorations")
    public List<RestorationSet> getRestorations(@Parameter(name = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                                @Parameter(name = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                                @Parameter(name = "Restoration creator's username") @QueryParam("creator") String creator,
                                                @Parameter(name = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                                @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                                @Parameter(name= "Limit no of results to return") @DefaultValue("100") @QueryParam(
                                                    "limit") int limit) {
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

        List<RestorationSet> restorationSets;

        if (queryMap.isEmpty()) {
            restorationSets = this.restorationDAO.getRestorations();
        } else {
            restorationSets = this.restorationDAO.queryRestorations(queryMap);
        }

        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the space " + spaceName);
            }
            if (!authorizer.canRead(username, space.getPrivileges(), groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            restorationSets = restorationSets.stream()
                .filter(restoration -> spaceMembers.contains(restoration.getId()))
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return restorationSets;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<RestorationSet> accessibleRestorations = restorationSets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleRestorations;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(description = "Create a restoration set", tags = "Post a restoration set to the restoration service")
    public RestorationSet uploadRestorationSet(@Parameter(name = "json representing the restoration set") RestorationSet restorationSet) {

        UserInfoUtils.throwExceptionIfIdPresent(restorationSet.getId());

        // check if the user has the quota to put it in
        Boolean postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "dfr3");

        if (!postOk) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DFR3_ALLOCATION_MESSAGE);
        }

        restorationSet.setCreator(username);
        restorationSet.setOwner(username);

        if (restorationSet.getRestorationCurves().size() == 0){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No restoration curves are included in the json. " +
                "Please provide at least one.");
        }
        String restorationId = this.restorationDAO.saveRestoration(restorationSet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(restorationId);
        spaceRepository.addSpace(space);
        restorationSet.setSpaces(spaceRepository.getSpaceNamesOfMember(restorationId));

        // add dfr3 in the usage
        AllocationUtils.increaseUsage(allocationsRepository, username, "dfr3");

        return restorationSet;
    }

    @GET
    @Path("{restorationId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets a restoration by Id", description = "Get a particular restoration based on the id provided")
    public RestorationSet getRestorationSetById(@Parameter(name = "hexadecimal restoration id", example = "5b47b2d8337d4a36187c6727")
                                                @PathParam("restorationId") String id) {
        Optional<RestorationSet> restorationSet = this.restorationDAO.getRestorationSetById(id);

        if (restorationSet.isPresent()) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                RestorationSet rs = restorationSet.get();
                rs.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return rs;
            }
        }

        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a restoration with id " + id);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{restorationId}")
    @Operation(tags = "Deletes a restoration by id")
    public RestorationSet deleteRestorationById(@Parameter(name = "restoration id", example = "5b47b2d8337d4a36187c6727") @PathParam(
        "restorationId") String id) {
        Optional<RestorationSet> restorationSet = this.restorationDAO.getRestorationSetById(id);

        if (restorationSet.isPresent()) {
            Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (this.username.equals(restorationSet.get().getOwner()) || isAdmin) {
//                Check for references in mappings, if found give 409
                if (this.mappingDAO.isCurvePresentInMappings(id)) {
                    throw new IncoreHTTPException(Response.Status.CONFLICT, "The restoration is referenced in at least one DFR3 mapping. " +
                        "It can not be deleted until" +
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
                    UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
                    AllocationUtils.decreaseUsage(allocationsRepository, username, "dfr3");

                    return this.restorationDAO.deleteRestorationSetById(id);
                }
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to delete the " +
                    "restoration with id " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a restoration set with id " + id);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Search for a text in all restorations", description = "Gets all restorations that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No restorations found with the searched text")
    })
    public List<RestorationSet> findRestorations(@Parameter(name = "Text to search by", example = "steel") @QueryParam("text") String text,
                                                 @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                                 @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam(
                                                     "limit") int limit) {
        List<RestorationSet> sets = new ArrayList<>();
        Optional<RestorationSet> fs = this.restorationDAO.getRestorationSetById(text);
        if (fs.isPresent()) {
            sets.add(fs.get());
        } else {
            sets = this.restorationDAO.searchRestorations(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<RestorationSet> accessibleRestorations = sets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleRestorations;
    }

}
