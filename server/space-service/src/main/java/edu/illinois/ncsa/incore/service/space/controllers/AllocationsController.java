/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.space.controllers;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.IGroupAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import javax.inject.Inject;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by ywkim on 3/14/2022.
 */

@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Allocations Service for getting the user's allocation information.",
        version = "v0.1",
        title = "IN-CORE v2 Allocation Service API",
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

@Api(value = "allocations", authorizations = {})

@Path("allocations")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})

public class AllocationsController {
    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    private IGroupAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository finalQuotaRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public AllocationsController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @ApiParam(value = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    private static final Logger logger = Logger.getLogger(AllocationsController.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the allocation and can be used as status check as well.",
        notes = "This will provide the allocation of the logged in user.")
    public String getUsage(@HeaderParam("x-auth-userinfo") String userInfo) {
        JSONObject outJson = null;

        String username = JsonUtils.parseUserName(userInfo);

        try {
            outJson = JsonUtils.createUserFinalQuotaJson(username, finalQuotaRepository);
        } catch (ParseException e) {
            logger.error("Error extracting allocation");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting allocation");
        }
        return outJson.toString();
    }

    @GET
    @Path("users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the allocation status of the given username.",
        notes = "This will only work for admin user group.")
    public String getAllocationsByUsername(
        @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("username") String userId) {
        JSONObject outJson = new JSONObject();

        if (this.authorizer.isUserAdmin(this.groups)) {
            try {
                outJson = JsonUtils.createUserFinalQuotaJson(userId, finalQuotaRepository);
            } catch (ParseException e) {
                logger.error("Error extracting user status");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
            }
        } else {
            logger.error("Error extracting user status");
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, "Logged in user is not incore admin");
        }

        return outJson.toString();
    }

    @GET
    @Path("groups/{groupname}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the allocation status of the given group name.",
        notes = "This will only work for admin user group.")
    public String getAllocationsByGroupname(
        @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("groupname") String groupId) {
        JSONObject outJson = new JSONObject();

        if (this.authorizer.isUserAdmin(this.groups)) {
            try {
                outJson = JsonUtils.createGroupAllocationJson(groupId, allocationsRepository);
            } catch (ParseException e) {
                logger.error("Error extracting user status");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
            }
        } else {
            outJson.put("query_user_id", groupId);
            outJson.put("reason_of_error", "logged in user is not an incore admin");
        }

        return outJson.toString();
    }
}
