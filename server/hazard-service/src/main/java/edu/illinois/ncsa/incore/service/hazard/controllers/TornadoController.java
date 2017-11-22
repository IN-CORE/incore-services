/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tornadoes")
public class TornadoController {

    @GET
    @Path("/models")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTornadoModelHazard(@QueryParam("modelId") String modelId, @QueryParam("datasetId") String tornadoId, @QueryParam("demandUnits") String demandUnits, @QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong ) {

        // calculate wind speed
        return Response.ok("Return tornado models hazard for "+modelId).build();
    }

}
