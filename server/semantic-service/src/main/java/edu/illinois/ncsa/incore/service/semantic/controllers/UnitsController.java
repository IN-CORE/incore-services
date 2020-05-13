/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.semantic.controllers;

import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.parser.NameParser;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("")
public class UnitsController {
    private String username;

    @Inject
    public UnitsController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
        // we want to limit the semantic service to admins for now
        Authorizer authorizer = new Authorizer();
        if (!authorizer.isUserAdmin(this.username)) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not an admin.");
        }
    }
    @GET
    @Path("/units/{uri}")
    public Response getUnit(@PathParam("uri") String uri,
                            @QueryParam("format") String format) {
        if (format == null) {
            format = "RDF/XML";
        } else {
            format = format.toUpperCase();
        }

        Optional<Unit> match = NameParser.tryParseResourceName(uri);

        if (match.isPresent()) {
            String rdf = match.get().serialize();
            return Response.ok(rdf)
                           .status(200)
                           .type("text/turtle;charset=utf-8")
                           .header("Access-Control-Allow-Origin", "http://localhost:3000")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }

    // Get Dimensions
    @GET
    @Path("/dimensions/{uri}")
    public Response getDimension(@PathParam("uri") String uri) {
        Optional<Dimension> match = Dimensions.getByResourceName(uri);

        if (match.isPresent()) {
            String rdf = match.get().serialize();
            return Response.ok(rdf)
                           .status(200)
                           .type("text/turtle;charset=utf-8")
                           .header("Access-Control-Allow-Origin", "http://localhost:3000")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }

    // Unit Systems

    // Get Units

    // Get Conversion

    // Query - Get Unit by Dimension, Symbol, Name, Unit System

}
