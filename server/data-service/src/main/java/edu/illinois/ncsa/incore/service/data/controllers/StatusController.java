package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.utils.DataJsonUtils;
import edu.illinois.ncsa.incore.service.data.utils.ServiceUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Api(value = "status", authorizations = {})

@Path("status")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class StatusController {
    @Inject
    private IRepository repository;

    private static final Logger logger = Logger.getLogger(StatusController.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gives the status of the service.",
        notes = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String statusJson = "{\"status\": \"responding\"}";
        return statusJson;
    }

    @GET
    @Path("usage/datasets")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the count and size of datasets.",
        notes = "This excludes datasets created by hazard service.")
    public String getUserStatusDataset(@HeaderParam("x-auth-userinfo") String userInfo) {
        JSONObject outJson = null;
        try {
            outJson = DataJsonUtils.createUserStatusJson(userInfo, repository, "dataset");
        } catch (ParseException e) {
            logger.error("Error extracting user status");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
        }

        return outJson.toString();
    }

    @GET
    @Path("usage/hazards")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the count and size of datasets created by the hazard service.", notes = "")
    public String getUserStatusHazard(@HeaderParam("x-auth-userinfo") String userInfo) {
        String hazardUsage = null;

        int totalHazardNum = 0;

        // check hazard usage
        try {
            hazardUsage = ServiceUtils.getHazardUsage(userInfo);
            totalHazardNum = DataJsonUtils.getNumHazardFromJson(hazardUsage);
        } catch (IOException e) {
            logger.error("Error extracting user status");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
        }

        JSONObject outJson = null;
        try {
            outJson = DataJsonUtils.createUserStatusJson(userInfo, repository, "hazard");
            outJson.put("total_number_of_hazards", totalHazardNum);

        } catch (ParseException e) {
            logger.error("Error extracting user status");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error extracting user status");
        }

        return outJson.toString();
    }
}
