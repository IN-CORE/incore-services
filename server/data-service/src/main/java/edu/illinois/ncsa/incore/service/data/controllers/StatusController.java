package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.utils.DataJsonUtils;
import edu.illinois.ncsa.incore.service.data.utils.ServiceUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Tag(name = "status")

@Path("status")
public class StatusController {
    @Inject
    private IRepository repository;

    private static final Logger logger = Logger.getLogger(StatusController.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Gives the status of the service.",
        description = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String statusJson = "{\"status\": \"responding\"}";
        return statusJson;
    }

    @GET
    @Path("usage/datasets")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gives the count and size of datasets.",
        description = "This excludes datasets created by hazard service.")
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
    @Operation(summary = "Gives the count and size of datasets created by the hazard service.", description = "")
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
