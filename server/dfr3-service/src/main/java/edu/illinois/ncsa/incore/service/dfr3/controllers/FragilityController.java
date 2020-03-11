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
import edu.illinois.ncsa.incore.service.dfr3.models.FragilitySet;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import edu.illinois.ncsa.incore.common.models.UserInfo;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;


@SwaggerDefinition(
    info = @Info(
        version = "v0.6.3",
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
    ),

    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
)


@Api(value = "fragilities", authorizations = {})
@Path("fragilities")
public class FragilityController {

    private static final Logger logger = Logger.getLogger(FragilityController.class);
    @Inject
    IAuthorizer authorizer;
    @Inject
    private IFragilityDAO fragilityDAO;
    @Inject
    private ISpaceRepository spaceRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets list of fragilities", notes = "Apply filters to get the desired set of fragilities")
    public List<FragilitySet> getFragilities(@HeaderParam("x-auth-userinfo") String userInfo,
                                             @ApiParam(value = "demand type filter", example = "PGA") @QueryParam("demand") String demandType,
                                             @ApiParam(value = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                             @ApiParam(value = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                             @ApiParam(value = "not implemented", hidden = true) @QueryParam("author") String author,
                                             @ApiParam(value = "Legacy fragility Id from v1") @QueryParam("legacy_id") String legacyId,
                                             @ApiParam(value = "Fragility creator's username") @QueryParam("creator") String creator,
                                             @ApiParam(value = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                             @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                             @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try{
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

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
                    throw new NotFoundException();
                }
                if (!authorizer.canRead(username, space.getPrivileges())) {
                    throw new NotAuthorizedException(username + " is not authorized to read the space " + spaceName);
                }
                List<String> spaceMembers = space.getMembers();

                fragilitySets = fragilitySets.stream()
                    .filter(fragility -> spaceMembers.contains(fragility.getId()))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
                if (fragilitySets.size() == 0) {
                    throw new NotFoundException("No fragilities were found in space " + spaceName);
                }
                return fragilitySets;
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

            List<FragilitySet> accessibleFragilities = fragilitySets.stream()
                .filter(b -> membersSet.contains(b.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

            return accessibleFragilities;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a fragility set", notes = "Post a fragility set to the dfr3 service")
    public FragilitySet uploadFragilitySet(@HeaderParam("x-auth-userinfo") String userInfo,
                                           @ApiParam(value = "json representing the fragility set") FragilitySet fragilitySet) {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();

            fragilitySet.setCreator(username);
            String fragilityId = this.fragilityDAO.saveFragility(fragilitySet);

            Space space = spaceRepository.getSpaceByName(username);
            if (space == null) {
                space = new Space(username);
                space.setPrivileges(Privileges.newWithSingleOwner(username));
            }
            space.addMember(fragilityId);
            spaceRepository.addSpace(space);

            return fragilitySet;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @GET
    @Path("{fragilityId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a fragility by Id", notes = "Get a particular fragility based on the id provided")
    public FragilitySet getFragilityById(@HeaderParam("x-auth-userinfo") String userInfo,
                                         @ApiParam(value = "fragility id", example = "5b47b2d8337d4a36187c6727") @PathParam("fragilityId") String id) {
        if (userInfo == null || !JsonUtils.isJSONValid(userInfo)) {
            throw new BadRequestException("Invalid User Info!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UserInfo user = objectMapper.readValue(userInfo, UserInfo.class);
            String username = user.getPreferredUsername();
            Optional<FragilitySet> fragilitySet = this.fragilityDAO.getFragilitySetById(id);

            if (fragilitySet.isPresent()) {
                if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces())) {
                    return fragilitySet.get();
                }
            }

            throw new NotFoundException();
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all fragilities", notes = "Gets all fragilities that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No fragilities found with the searched text")
    })
    public List<FragilitySet> findFragilities(@HeaderParam("x-auth-userinfo") String userInfo,
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

            List<FragilitySet> sets = new ArrayList<>();
            Optional<FragilitySet> fs = this.fragilityDAO.getFragilitySetById(text);
            if (fs.isPresent()) {
                sets.add(fs.get());
            } else {
                sets = this.fragilityDAO.searchFragilities(text);
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

            List<FragilitySet> accessibleFragilities = sets.stream()
                .filter(b -> membersSet.contains(b.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

            return accessibleFragilities;
        }
        catch (Exception e) {
            throw new BadRequestException("Invalid User Info!");
        }

    }

}