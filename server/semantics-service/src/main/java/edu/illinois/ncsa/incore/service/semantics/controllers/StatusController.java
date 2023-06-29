package edu.illinois.ncsa.incore.service.semantics.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
