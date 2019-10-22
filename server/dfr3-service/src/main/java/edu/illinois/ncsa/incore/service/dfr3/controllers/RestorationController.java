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
import edu.illinois.ncsa.incore.service.dfr3.daos.IRestorationDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;


@SwaggerDefinition(
    info = @Info(
        version = "v0.4.0",
        description = "IN-CORE Service For Restoration and Restoration mappings",

        title = "IN-CORE v2 DFR3 Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore2.ncsa.illinois.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    ),

    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
)


@Api(value = "restorations", authorizations = {})
@Path("restorations")
public class RestorationController {

    private static final Logger logger = Logger.getLogger(RestorationController.class);
    @Inject
    IAuthorizer authorizer;
    @Inject
    private IRestorationDAO restorationDAO;
    @Inject
    private ISpaceRepository spaceRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets list of restorations", notes = "Apply filters to get the desired set of restorations")
    public List<RestorationSet> getRestorations(@HeaderParam("X-Credential-Username") String username,
                                                @ApiParam(value = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                                @ApiParam(value = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                                @ApiParam(value = "Restoration creator's username") @QueryParam("creator") String creator,
                                                @ApiParam(value = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                                @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                                @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

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
                throw new NotFoundException();
            }
            if (!authorizer.canRead(username, space.getPrivileges())) {
                throw new NotAuthorizedException(username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            restorationSets = restorationSets.stream()
                .filter(restoration -> spaceMembers.contains(restoration.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            if (restorationSets.size() == 0) {
                throw new NotFoundException("No restorations were found in space " + spaceName);
            }
            return restorationSets;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

        List<RestorationSet> accessibleRestorations = restorationSets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleRestorations;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a restoration set", notes = "Post a restoration set to the restoration service")
    public RestorationSet uploadRestorationSet(@HeaderParam("X-Credential-Username") String username,
                                               @ApiParam(value = "json representing the restoration set") RestorationSet restorationSet) {
        restorationSet.setCreator(username);
        String restorationId = this.restorationDAO.saveRestoration(restorationSet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(restorationId);
        spaceRepository.addSpace(space);

        return restorationSet;
    }

    @GET
    @Path("{restorationId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a restoration by Id", notes = "Get a particular restoration based on the id provided")
    public RestorationSet getRestorationSetById(@HeaderParam("X-Credential-Username") String username,
                                                @ApiParam(value = "hexadecimal restoration id", example = "5b47b2d8337d4a36187c6727")
                                                @PathParam("restorationId") String id) {
        Optional<RestorationSet> restorationSet = this.restorationDAO.getRestorationSetById(id);

        if (restorationSet.isPresent()) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces())) {
                return restorationSet.get();
            }
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all restorations", notes = "Gets all restorations that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No restorations found with the searched text")
    })
    public List<RestorationSet> findRestorations(@HeaderParam("X-Credential-Username") String username,
                                                 @ApiParam(value = "Text to search by", example = "steel") @QueryParam("text") String text,
                                                 @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                                 @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        List<RestorationSet> sets = new ArrayList<>();
        Optional<RestorationSet> fs = this.restorationDAO.getRestorationSetById(text);
        if (fs.isPresent()) {
            sets.add(fs.get());
        } else {
            sets = this.restorationDAO.searchRestorations(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

        List<RestorationSet> accessibleRestorations = sets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleRestorations;
    }

}
