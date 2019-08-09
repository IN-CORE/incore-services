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
import edu.illinois.ncsa.incore.service.dfr3.daos.IRepairDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;
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
        description = "IN-CORE Service For Repair and Repair mappings",

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


@Api(value = "repairs", authorizations = {})
@Path("repairs")
public class RepairController {

    private static final Logger logger = Logger.getLogger(RepairController.class);
    @Inject
    IAuthorizer authorizer;
    @Inject
    private IRepairDAO repairDAO;
    @Inject
    private ISpaceRepository spaceRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets list of repairs", notes = "Apply filters to get the desired set of repairs")
    public List<RepairSet> getRepairs(@HeaderParam("X-Credential-Username") String username,
                                      @ApiParam(value = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                      @ApiParam(value = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                      @ApiParam(value = "Legacy repair Id from v1") @QueryParam("legacy_id") String legacyId,
                                      @ApiParam(value = "Repair creator's username") @QueryParam("creator") String creator,
                                      @ApiParam(value = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                      @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                      @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        if (legacyId != null) {
            queryMap.put("legacyId", legacyId);
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

        List<RepairSet> repairSets;

        if (queryMap.isEmpty()) {
            repairSets = this.repairDAO.getRepairs();
        } else {
            repairSets = this.repairDAO.queryRepairs(queryMap);
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

            repairSets = repairSets.stream()
                .filter(repair -> spaceMembers.contains(repair.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            if (repairSets.size() == 0) {
                throw new NotFoundException("No repairs were found in space " + spaceName);
            }
            return repairSets;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

        List<RepairSet> accessibleRepairs = repairSets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleRepairs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a repair set", notes = "Post a repair set to the repair service")
    public RepairSet uploadRepairSet(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "json representing the repair set") RepairSet repairSet) {
        repairSet.setPrivileges(Privileges.newWithSingleOwner(username));
        repairSet.setCreator(username);
        String repairId = this.repairDAO.saveRepair(repairSet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(repairId);
        spaceRepository.addSpace(space);

        return repairSet;
    }

    @GET
    @Path("{repairId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a repair by Id", notes = "Get a particular repair based on the id provided")
    public RepairSet getRepairSetById(@HeaderParam("X-Credential-Username") String username,
                                      @ApiParam(value = "hexadecimal repair id", example = "5b47b2d8337d4a36187c6727")
                                      @PathParam("repairId") String id) {
        Optional<RepairSet> repairSet = this.repairDAO.getRepairSetById(id);

        if (repairSet.isPresent()) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces())) {
                return repairSet.get();
            }
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all repairs", notes = "Gets all repairs that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No repairs found with the searched text")
    })
    public List<RepairSet> findRepairs(@HeaderParam("X-Credential-Username") String username,
                                       @ApiParam(value = "Text to search by", example = "steel") @QueryParam("text") String text,
                                       @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                       @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        List<RepairSet> sets = new ArrayList<>();
        Optional<RepairSet> fs = this.repairDAO.getRepairSetById(text);
        if (fs.isPresent()) {
            sets.add(fs.get());
        } else {
            sets = this.repairDAO.searchRepairs(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

        List<RepairSet> accessibleRepairs = sets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleRepairs;
    }

}
