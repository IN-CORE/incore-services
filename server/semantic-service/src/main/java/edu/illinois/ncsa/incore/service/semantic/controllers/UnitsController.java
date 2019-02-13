/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.semantic.controllers;

import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.parser.NameParser;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/")
public class UnitsController {
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
                           .header("Access-Control-Allow-Origin", "*")
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
                           .header("Access-Control-Allow-Origin", "*")
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
