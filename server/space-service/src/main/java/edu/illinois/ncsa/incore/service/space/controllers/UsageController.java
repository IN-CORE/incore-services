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
import edu.illinois.ncsa.incore.common.models.UserUsages;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import java.util.*;

/**
 * Created by ywkim on 3/14/2022.
 */

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Usage Service for getting the user's usage information.",
        version = "1.29.0",
        title = "IN-CORE v2 Usage Service API",
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

@Tag(name = "usage")
//@Api(value = "usage", authorizations = {})

@Path("usage")
//@ApiResponses(value = {
//    @ApiResponse(code = 500, message = "Internal Server Error")
//})

public class UsageController {
    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    private IAuthorizer authorizer;

    private static final Logger logger = Logger.getLogger(UsageController.class);

    @Inject
    public UsageController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gives the usage status and can be used as status check as well.",
        description = "This will provide the usage of the logged in user.")
    public UserUsages getUsage() {

        try {
            return AllocationUtils.createUserUsage(username, allocationsRepository);
        } catch (ParseException e) {
            logger.error("Error extracting usage");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting usage");
        }
    }

    @GET
    @Path("users/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gives the usage status of the given username.",
        description = "This will only work for admin user group.")
    public UserUsages getUsageByUsername(
        @Parameter(name = "Dataset Id from data service", required = true) @PathParam("username") String userId) {

        if (this.authorizer.isUserAdmin(this.groups)) {
            try {
                return AllocationUtils.createUserUsage(userId, allocationsRepository);
            } catch (ParseException e) {
                logger.error("Error extracting user status");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
            }
        } else {
            logger.error("Logged in user is not an incore admin");
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, "Logged in user is not an incore admin");
        }
    }
}
