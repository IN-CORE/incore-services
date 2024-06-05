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
import edu.illinois.ncsa.incore.service.dfr3.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.dfr3.utils.ServiceUtil;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@OpenAPIDefinition(
    info = @Info(
        version = "1.26.1",
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
    )
//    consumes = {"application/json"},
//    produces = {"application/json"},
//    schemes = {SwaggerDefinition.Scheme.HTTP}
)


@Tag(name = "Repair")
@Path("repairs")
public class RepairController {
    private static final Logger logger = Logger.getLogger(RepairController.class);

    private final String username;
    private final List<String> groups;
    private final String userGroups;

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
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name  = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.userGroups = userGroups;
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets list of repairs", summary = "Apply filters to get the desired set of repairs")
    public List<RepairSet> getRepairs(@Parameter(name = "hazard type  filter", example = "earthquake") @QueryParam("hazard") String hazardType,
                                      @Parameter(name = "Inventory type", example = "building") @QueryParam("inventory") String inventoryType,
                                      @Parameter(name = "Data type filter", example = "ergo:buildingInventoryVer7") @QueryParam("dataType") String dataType,
                                      @Parameter(name = "Repair creator's username") @QueryParam("creator") String creator,
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

        if (dataType != null){
            queryMap.put("dataType", dataType);
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
    @Operation(tags = "Create a repair set", summary = "Post a repair set to the repair service")
    public RepairSet uploadRepairSet(@Parameter(name = "json representing the repair set") RepairSet repairSet) {

        UserInfoUtils.throwExceptionIfIdPresent(repairSet.getId());

        // check if the user has the quota to put it in
        Boolean postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "dfr3");

        if (!postOk) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DFR3_ALLOCATION_MESSAGE);
        }

        // check if the parameters matches the defined data type in semantics
        String dataType = repairSet.getDataType();
        if (dataType == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "dataType is a required field.");
        }
        try {
            String semanticsDefinition = ServiceUtil.getJsonFromSemanticsEndpoint(dataType, username, userGroups);
            List<String> columns = CommonUtil.getColumnNames(semanticsDefinition);

            repairSet.getCurveParameters().forEach((params) -> {
               if(!columns.contains(params.name)) {
                   throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Curve parameter: " + params.name + " not found in the dataType: " + dataType);
               }
            });

        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not check the repair curve parameter matches the dataType columns.");
        }

        repairSet.setCreator(username);
        repairSet.setOwner(username);

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
    @Operation(tags = "Gets a repair by Id", description = "Get a particular repair based on the id provided")
    public RepairSet getRepairSetById(@Parameter(name = "hexadecimal repair id", example = "5b47b2d8337d4a36187c6727")
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
    @Operation(tags = "Deletes a repair by id")
    public RepairSet deleteRepairById(@Parameter(name = "repair id", example = "5b47b2d8337d4a36187c6727") @PathParam("repairId") String id) {
        Optional<RepairSet> repairSet = this.repairDAO.getRepairSetById(id);

        if (repairSet.isPresent()) {
            Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (this.username.equals(repairSet.get().getOwner()) || isAdmin) {
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
    @Operation(tags = "Search for a text in all repairs", summary = "Gets all repairs that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No repairs found with the searched text")
    })
    public List<RepairSet> findRepairs(@Parameter(name = "Text to search by", example = "steel") @QueryParam("text") String text,
                                       @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                       @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
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
