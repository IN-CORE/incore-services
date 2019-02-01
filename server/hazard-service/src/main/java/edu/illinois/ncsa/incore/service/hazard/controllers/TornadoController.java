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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.MeanWidthTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.RandomAngleTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.MeanLengthWidthAngleTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.RandomLengthWidthAngleTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.TornadoRandomWidth;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
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
    @ApiOperation(value = "API call returns all tornadoes.")
    public List<ScenarioTornado> getScenarioTornadoes(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username) {
        return repository.getScenarioTornadoes().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "API call creates a new tornado, the newly created tornado is returned.")
    public ScenarioTornado createScenarioTornado(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username, ScenarioTornado scenarioTornado) throws Exception {
        if (scenarioTornado != null) {
            Tornado tornado = null;

            if (scenarioTornado.getTornadoModel().equals("MeanWidthTornado")) {
                 tornado = new MeanWidthTornado();
            }
            else if(scenarioTornado.getTornadoModel().equals("MeanLengthWidthAngleTornado")) {
                tornado = new MeanLengthWidthAngleTornado();
            }
            else if(scenarioTornado.getTornadoModel().equals("RandomLengthWidthAngleTornado")) {
                tornado = new RandomLengthWidthAngleTornado();
            }
            else if (scenarioTornado.getTornadoModel().equals("RandomWidthTornado")) {
                tornado = new TornadoRandomWidth();
            }
            else if(scenarioTornado.getTornadoModel().equals("RandomAngleTornado")) {
                tornado = new RandomAngleTornado();
            }
            else {
                logger.error("Requested tornado model, " + scenarioTornado.getTornadoModel() + " is not yet implemented.");
                throw new UnsupportedHazardException("Requested tornado model, " + scenarioTornado.getTornadoModel() + " is not yet implemented.");
            }

            // Run the model
            tornado.createTornado(scenarioTornado.getTornadoParameters());

            // Set the resulting tornado generated by the model
            scenarioTornado.setTornadoWidth(tornado.getTornadoWidths());
            scenarioTornado.setEfBoxes(tornado.getEFBoxes());

            SimpleFeatureCollection collection = TornadoUtils.createTornadoGeometry(scenarioTornado);
            // Create the files from feature collection
            File[] files = TornadoUtils.createTornadoShapefile((DefaultFeatureCollection) collection);
            // Create dataset object representation for storing shapefile
            JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", username, "EF Boxes representing tornado");
            // Store the dataset
            String datasetId = ServiceUtil.createDataset(datasetObject, username, files);
            scenarioTornado.setTornadoDatasetId(datasetId);

            scenarioTornado.setPrivileges(Privileges.newWithSingleOwner(username));

            return repository.addScenarioTornado(scenarioTornado);

        } else {
            logger.warn("scenario tornado is null");
        }
        logger.error("Scenario tornado was null.");
        throw new InternalServerErrorException("Tornado was null");
    }

    @GET
    @Path("{tornado-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "API call returns the scenario tornado with matching id.")
    public ScenarioTornado getScenarioTornado(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        ScenarioTornado tornado = repository.getScenarioTornadoById(tornadoId);
        if (!authorizer.canRead(username, tornado.getPrivileges())) {
            throw new ForbiddenException();
        }
        return tornado;
    }

    @GET
    @Path("{tornado-id}/value")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "API call returns the wind speed at given location using the specified scenario tornado.")
    public WindHazardResult getScenarioTornadoHazard(
        @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Latitude of a site. Ex: 35.027.", required = true) @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude of a site. Ex: -90.131.", required = true) @QueryParam("siteLong") double siteLong,
        @ApiParam(value = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation) throws Exception {

        ScenarioTornado tornado = getScenarioTornado(username, tornadoId);
        if (tornado != null) {
            Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));

            try {
                return TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation);
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
    @ApiOperation(value = "API call returns the wind speed at given location using the specified scenario tornado.")
    public List<WindHazardResult> getScenarioTornadoHazardValues(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '35.027,-90.131'.", required = true) @QueryParam("point") List<IncorePoint> points,
        @ApiParam(value = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation) throws Exception {

        ScenarioTornado tornado = getScenarioTornado(username, tornadoId);
        List<WindHazardResult> hazardResults = new ArrayList<WindHazardResult>();
        if (tornado != null) {
            for (IncorePoint point : points) {
                try {
                    hazardResults.add(TornadoCalc.getWindHazardAtSite(tornado, point.getLocation(), demandUnits, simulation));
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
    @ApiOperation(value = "API call returns a zip shapefile representing tornado defined by given id.")
    public Response getFile(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        // TODO implement this and change MediaType to Octet Stream
        return Response.ok("Shapefile representing scenario tornado not yet implemented.").build();
    }
}
