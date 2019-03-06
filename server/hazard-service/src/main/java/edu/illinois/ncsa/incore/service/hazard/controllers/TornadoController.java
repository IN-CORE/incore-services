/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.*;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value="tornadoes", authorizations = {})

@Path("tornadoes")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error.")
})
public class TornadoController {
    private static final Logger logger = Logger.getLogger(TornadoController.class);

    @Inject
    private ITornadoRepository repository;

    @Inject
    private IAuthorizer authorizer;

    private GeometryFactory factory = new GeometryFactory();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all tornadoes.")
    public List<Tornado> getTornadoes(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username) {
        return repository.getTornadoes().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }


    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new tornado, the newly created tornado is returned.",
        notes="Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create both model tornadoes and dataset-based tornadoes with GeoTiff files uploaded.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tornado", value = "Tornado json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Tornado files.", required = true, dataType = "string", paramType = "form")
    })
    public Tornado createTornado(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(hidden = true) @FormDataParam("tornado") String tornadoJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        ObjectMapper mapper = new ObjectMapper();
        Tornado tornado = null;
        try {
            tornado = mapper.readValue(tornadoJson, Tornado.class);
            tornado.setPrivileges(Privileges.newWithSingleOwner(username));

            // TODO verify that parameters like title are not null

            if (tornado != null && tornado instanceof TornadoModel) {
                TornadoModel tornadoModel = (TornadoModel) tornado;
                BaseTornado newTornado = null;

                if (tornadoModel.getTornadoModel().equals("MeanWidthTornado")) {
                    newTornado = new MeanWidthTornado();
                } else if (tornadoModel.getTornadoModel().equals("MeanLengthWidthAngleTornado")) {
                    newTornado = new MeanLengthWidthAngleTornado();
                } else if (tornadoModel.getTornadoModel().equals("RandomLengthWidthAngleTornado")) {
                    newTornado = new RandomLengthWidthAngleTornado();
                } else if (tornadoModel.getTornadoModel().equals("RandomWidthTornado")) {
                    newTornado = new TornadoRandomWidth();
                } else if (tornadoModel.getTornadoModel().equals("RandomAngleTornado")) {
                    newTornado = new RandomAngleTornado();
                } else {
                    logger.error("Requested tornado model, " + tornadoModel.getTornadoModel() + " is not yet implemented.");
                    throw new BadRequestException("Requested tornado model, " + tornadoModel.getTornadoModel() + " is not yet implemented.");
                }

                // Run the model
                newTornado.createTornado(tornadoModel.getTornadoParameters());

                // Set the resulting tornado generated by the model
                tornadoModel.setTornadoWidth(newTornado.getTornadoWidths());
                tornadoModel.setEfBoxes(newTornado.getEFBoxes());

                SimpleFeatureCollection collection = TornadoUtils.createTornadoGeometry(tornadoModel);
                // Create the files from feature collection
                File[] files = TornadoUtils.createTornadoShapefile((DefaultFeatureCollection) collection);

                // Create dataset object representation for storing shapefile
                JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", username, "EF Boxes representing tornado");

                // Store the dataset
                String datasetId = ServiceUtil.createDataset(datasetObject, username, files);
                tornadoModel.setDatasetId(datasetId);

                return repository.addTornado(tornado);
            } else if (tornado != null && tornado instanceof TornadoDataset) {
                TornadoDataset tornadoDataset = (TornadoDataset) tornado;
                if (fileParts != null && !fileParts.isEmpty() && TornadoUtils.validateDatasetTypes(fileParts)) {
                    // Create dataset object representation for storing shapefile
                    JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", username, "EF Boxes representing tornado");
                    // Store the dataset
                    String datasetId = ServiceUtil.createDataset(datasetObject, username, fileParts);
                    ((TornadoDataset) tornado).setDatasetId(datasetId);

                    return repository.addTornado(tornado);
                } else {
                    logger.error("Could not create Tornado. Check your file extensions and the number of files in the request.");
                    throw new BadRequestException("Could not create Tornado. Check your file extensions and the number of files in the request.");
                }
            }
        } catch (IOException e) {
            logger.error("Error mapping the request to a supported Tornado type.", e);
        }
        throw new BadRequestException("Could not create Tornado, check the format of your request.");
    }

    @GET
    @Path("{tornado-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the tornado with matching id.")
    public Tornado getTornado(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        Tornado tornado = repository.getTornadoById(tornadoId);

        if (!authorizer.canRead(username, tornado.getPrivileges())) {
            throw new ForbiddenException();
        }
        return tornado;
    }

    @GET
    @Path("{tornado-id}/value")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the wind speed at given location using the specified tornado.")
    public WindHazardResult getTornadoHazard(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Latitude of a site. Ex: 35.027.", required = true) @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude of a site. Ex: -90.131.", required = true) @QueryParam("siteLong") double siteLong,
        @ApiParam(value = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation) throws Exception {

        Tornado tornado = getTornado(username, tornadoId);
        if (tornado != null) {
            Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));

            try {
                return TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation, username);
            } catch (Exception e) {
                throw new InternalServerErrorException("Error computing hazard.", e);
            }
        } else {
            throw new NotFoundException("Tornado with id " + tornadoId + " was not found.");
        }
    }

    @GET
    @Path("{tornado-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the wind speed at given location using the specified tornado.")
    public List<WindHazardResult> getTornadoHazardValues(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '35.027,-90.131'.", required = true) @QueryParam("point") List<IncorePoint> points,
        @ApiParam(value = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation) throws Exception {

        Tornado tornado = getTornado(username, tornadoId);
        List<WindHazardResult> hazardResults = new ArrayList<WindHazardResult>();
        if (tornado != null) {
            for (IncorePoint point : points) {
                try {
                    hazardResults.add(TornadoCalc.getWindHazardAtSite(tornado, point.getLocation(), demandUnits, simulation, username));
                } catch (UnsupportedHazardException e) {
                    logger.error("Could not get the requested hazard type. Check that the hazard type and units " + demandUnits + " are supported", e);
                    // logger.error("Could not get the requested hazard type. Check that the hazard type " + demandType + " and units " + demandUnits + " are supported", e);
                }
            }

            return hazardResults;
        } else {
            throw new NotFoundException("Tornado with id " + tornadoId + " was not found.");
        }
    }

    @GET
    @Path("{tornado-id}/dataset")
    @Produces({MediaType.TEXT_PLAIN})
    @ApiOperation(value = "Returns a zip shapefile representing tornado defined by given id.")
    public Response getFile(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        // TODO implement this and change MediaType to Octet Stream
        return Response.ok("Shapefile representing tornado not yet implemented.").build();
    }

}
