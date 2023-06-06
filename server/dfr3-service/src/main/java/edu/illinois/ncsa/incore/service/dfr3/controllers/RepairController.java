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
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRepairDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;


@SwaggerDefinition(
    info = @Info(
        version = "v0.6.3",
        description = "IN-CORE Service For Repair and Repair mappings",

        title = "IN-CORE v2 DFR3 Service API",
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


@Api(value = "repairs", authorizations = {})
@Path("repairs")
public class RepairController {
    private static final Logger logger = Logger.getLogger(RepairController.class);

    private final String username;
    private final List<String> groups;

    @Inject
    IAuthorizer authorizer;
    @Inject
    private IRepairDAO repairDAO;
    @Inject
    private IMappingDAO mappingDAO;
    @Inject
    private ISpaceRepository spaceRepository;
    @Inject
    private IUserAllocationsRepository allocationsRepository;
    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    public RepairController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @ApiParam(value = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets list of repairs", notes = "Apply filters to get the desired set of repairs")
    public List<RepairSet> getRepairs(@ApiParam(value = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                      @ApiParam(value = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                      @ApiParam(value = "Repair creator's username") @QueryParam("creator") String creator,
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

        List<RepairSet> repairSets;

        if (queryMap.isEmpty()) {
            repairSets = this.repairDAO.getRepairs();
        } else {
            repairSets = this.repairDAO.queryRepairs(queryMap);
        }

        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the space " + spaceName);
            }
            if (!authorizer.canRead(username, space.getPrivileges(), groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    "You don't have the required permissions to access the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            repairSets = repairSets.stream()
                .filter(repair -> spaceMembers.contains(repair.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            return repairSets;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<RepairSet> accessibleRepairs = repairSets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleRepairs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a repair set", notes = "Post a repair set to the repair service")
    public RepairSet uploadRepairSet(@ApiParam(value = "json representing the repair set") RepairSet repairSet) {

        UserInfoUtils.throwExceptionIfIdPresent(repairSet.getId());

        // check if the user has the quota to put it in
        Boolean postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "dfr3");

        if (!postOk) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DFR3_ALLOCATION_MESSAGE);
        }

        repairSet.setCreator(username);
        if (repairSet.getRepairCurves().size() == 0){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No repair curves are included in the json. " +
                "Please provide at least one.");
        }
        String repairId = this.repairDAO.saveRepair(repairSet);

        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(repairId);
        spaceRepository.addSpace(space);
        repairSet.setSpaces(spaceRepository.getSpaceNamesOfMember(repairId));

        // add dfr3 in the usage
        AllocationUtils.increaseUsage(allocationsRepository, username, "dfr3");

        return repairSet;
    }

    @GET
    @Path("{repairId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a repair by Id", notes = "Get a particular repair based on the id provided")
    public RepairSet getRepairSetById(@ApiParam(value = "hexadecimal repair id", example = "5b47b2d8337d4a36187c6727")
                                      @PathParam("repairId") String id) {
        Optional<RepairSet> repairSet = this.repairDAO.getRepairSetById(id);

        if (repairSet.isPresent()) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                RepairSet rs = repairSet.get();
                rs.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return rs;
            }
        }

        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a repair with id " + id);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{repairId}")
    @ApiOperation(value = "Deletes a repair by id")
    public RepairSet deleteRepairById(@ApiParam(value = "repair id", example = "5b47b2d8337d4a36187c6727") @PathParam("repairId") String id) {
        Optional<RepairSet> repairSet = this.repairDAO.getRepairSetById(id);

        if (repairSet.isPresent()) {
            if (authorizer.canUserDeleteMember(username, id, spaceRepository.getAllSpaces(), groups)) {
//                Check for references in mappings, if found give 409
                if (this.mappingDAO.isCurvePresentInMappings(id)) {
                    throw new IncoreHTTPException(Response.Status.CONFLICT, "The repair is referenced in at least one DFR3 mapping. It " +
                        "can not be deleted until" +
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
                    AllocationUtils.decreaseUsage(allocationsRepository, username, "dfr3");

                    return this.repairDAO.deleteRepairSetById(id);
                }
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to delete the repair " +
                    "with id " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a repair set with id " + id);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all repairs", notes = "Gets all repairs that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No repairs found with the searched text")
    })
    public List<RepairSet> findRepairs(@ApiParam(value = "Text to search by", example = "steel") @QueryParam("text") String text,
                                       @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                       @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        List<RepairSet> sets = new ArrayList<>();
        Optional<RepairSet> fs = this.repairDAO.getRepairSetById(text);
        if (fs.isPresent()) {
            sets.add(fs.get());
        } else {
            sets = this.repairDAO.searchRepairs(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<RepairSet> accessibleRepairs = sets.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleRepairs;
    }
}
