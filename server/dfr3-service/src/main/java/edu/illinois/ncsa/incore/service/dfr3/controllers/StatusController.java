package edu.illinois.ncsa.incore.service.dfr3.controllers;

import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api(value = "status", authorizations = {})

@Path("status")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class StatusController {
    private static final Logger logger = Logger.getLogger(StatusController.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gives the status of the service.",
        notes = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String statusJson = "{\"status\": \"responding\"}";
        return statusJson;
    }
}
