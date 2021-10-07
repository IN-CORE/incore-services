/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.maestro.daos.IPlaybookDAO;
import edu.illinois.ncsa.incore.service.maestro.models.Playbook;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
    info = @Info(
        version = "v0.6.3",
        description = "IN-CORE Service For playbook",

        title = "IN-CORE v2 Maestro Service API",
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

@Api(value = "playbooks", authorizations = {})
@Path("playbooks")
public class PlaybookController {

    private static final Logger logger = Logger.getLogger(PlaybookController.class);
    private final String username;

    @Inject
    private IPlaybookDAO playbookDAO;

    @Inject
    public PlaybookController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the full definition of playbooks")
    public List<Playbook> getPlaybooks() {
        return this.playbookDAO.getAllPlaybooks();
    }

    @GET
    @Path("{playbookId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets a playbook definition by Id", notes = "Get a particular playbook definition based on the id provided")
    public Playbook getPlaybookById(@ApiParam(value = "playbook id") @PathParam("playbookId") String id) {
        Playbook playbook = this.playbookDAO.getPlaybookById(id);
        if (playbook != null) {
            return playbook;
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a fragility set with id " + id);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Create a playbook definition", notes = "Post a playbook definition")
    public Playbook uploadPlaybook(@ApiParam(value = "json representing the playbook definition") Playbook playbook) {
        return this.playbookDAO.addPlaybook(playbook);
    }

}
