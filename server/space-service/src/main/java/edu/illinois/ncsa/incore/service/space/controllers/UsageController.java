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

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by ywkim on 3/14/2022.
 */

@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Usage Service for getting the user's usage information.",
        version = "v0.1",
        title = "IN-CORE v2 Usage Service API",
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

@Api(value = "usage", authorizations = {})

@Path("usage")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})

public class UsageController {
    @Inject
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    private static final Logger logger = Logger.getLogger(UsageController.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the usage status and can be used as status check as well.",
        notes = "This will provide the usage of the logged in user.")
    public String getUsage(@HeaderParam("x-auth-userinfo") String userInfo) {
        JSONObject outJson = null;

        String username = JsonUtils.parseUserName(userInfo);

        try {
            outJson = JsonUtils.createUserUsageJson(username, allocationsRepository);
        } catch (ParseException e) {
            logger.error("Error extracting usage");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting usage");
        }
        return outJson.toString();
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the usage status of the given username.",
        notes = "This will only work for admin user group.")
    public String getUsageByUsername(
        @HeaderParam("x-auth-userinfo") String userInfo,
        @HeaderParam("x-auth-usergroup") String userGroup,
        @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("username") String userId) {
        // check if the logged in user is in admin group
        Boolean isAdmin = JsonUtils.isLoggedInUserAdmin(userGroup);

        JSONObject outJson = new JSONObject();

        if (isAdmin) {
            try {
                outJson = JsonUtils.createUserUsageJson(userId, allocationsRepository);
            } catch (ParseException e) {
                logger.error("Error extracting user status");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
            }
        } else {
            outJson.put("query_user_id", userId);
            outJson.put("reason_of_error", "logged in user is not an incore admin");
        }

        return outJson.toString();
    }
}