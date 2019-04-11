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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulationEnsemble;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneWindfields;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.types.HurricaneWindfieldResult;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.types.WindfieldDemandUnits;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.GISHurricaneUtils;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneCalc;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.opengis.geometry.MismatchedDimensionException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(value="hurricaneWindfields", authorizations = {})

@Path("hurricaneWindfields")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class HurricaneController {
    private static final Logger log = Logger.getLogger(HurricaneController.class);

    @Inject
    private IAuthorizer authorizer;

    @Inject
    private IHurricaneRepository repository;

    private GeometryFactory factory = new GeometryFactory();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all hurricanes.")
    public List<HurricaneWindfields> getHurricaneWindfields(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Hurricane coast. Ex: 'gulf, florida or east'.", required = true) @QueryParam("coast") String coast,
        @ApiParam(value = "Hurricane category. Ex: between 1 and 5.", required = true) @QueryParam("category") int category,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        List<HurricaneWindfields> hurricaneWindfields = repository.getHurricanes().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());

        if (coast != null) {
            hurricaneWindfields = hurricaneWindfields.stream().filter(s -> s.getCoast().equals(coast)).collect(Collectors.toList());
        }

        if (category > 0) {
            hurricaneWindfields = hurricaneWindfields.stream().filter(s -> s.getCategory() == category).collect(Collectors.toList());
        }

        return hurricaneWindfields.stream()
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());
    }

    @GET
    @Path("{hurricaneId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the hurricane with matching id.")
    public HurricaneWindfields getHurricaneWindfieldsById(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricaneId") String hurricaneId) {

        HurricaneWindfields hurricane = repository.getHurricaneById(hurricaneId);
        if (!authorizer.canRead(username, hurricane.getPrivileges())) {
            throw new ForbiddenException();
        }
        return hurricane;
    }

    @GET
    @Path("{hurricaneId}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the hurricane wind field values.")
    public List<HurricaneWindfieldResult> getHurricaneWindfieldValues(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricaneId") String hurricaneId,
        @ApiParam(value = "Hurricane demand type. Ex. 'velocity'.") @QueryParam("demandType") @DefaultValue("velocity") String demandType,
        @ApiParam(value = "Hurricane demand unit.") @QueryParam("demandUnits") @DefaultValue("kt") WindfieldDemandUnits demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.09,-80.62'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        HurricaneWindfields hurricane = getHurricaneWindfieldsById(username, hurricaneId);
        List<HurricaneWindfieldResult> hurrResults = new ArrayList<>();

        //Get shapefile datasetid
        String datasetId = hurricane.findFullPathDatasetId();

        //Unzip shapefiles locally
        File incoreWorkDir = ServiceUtil.getWorkDirectory();
        File zipFile = ServiceUtil.getFileFromDataService(datasetId, username, incoreWorkDir);
        URL shpFileUrl = GISUtil.unZipShapefiles(zipFile, incoreWorkDir);
        String hurricaneUnits = hurricane.getVelocityUnits();

        if (hurricane != null) {
            for (IncorePoint point : points) {
                double windValue = 0;
                double lat = point.getLocation().getY();
                double lon = point.getLocation().getX();
                try {
                    //TODO: Take features directly instead of loading from file each time - improves performance
                    windValue = GISHurricaneUtils.CalcVelocityFromPoint(shpFileUrl.getPath(), lat, lon);
                    if (!demandUnits.toString().equals(hurricaneUnits)) {
                        windValue = HurricaneUtil.getCorrectUnitsOfVelocity(windValue, hurricaneUnits, demandUnits.toString());
                    }
                } catch (IOException e) {
                    log.error("Velocity calculation failed from the shapefile");
                }

                HurricaneWindfieldResult res = new HurricaneWindfieldResult(lat, lon, windValue, demandType, demandUnits.toString());
                hurrResults.add(res);
            }
        }

        if (incoreWorkDir.exists()) {
            incoreWorkDir.delete();
        }

        return hurrResults;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new hurricane, simulation of hurricane windfields is returned.",
        notes = "One dataset for each time frame of the simulation is returned representing the hurricane " +
            "windfield's raster.")
    public HurricaneWindfields createHurricaneWindfields(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        HurricaneWindfields inputHurricane) {

        HurricaneWindfields hurricaneWindfields = new HurricaneWindfields();
        if (inputHurricane != null) {
            HurricaneSimulationEnsemble hurricaneSimulationEnsemble = getHurricaneJsonByCategory(username,
                inputHurricane.getCoast(), inputHurricane.getCategory(), inputHurricane.getTransD(),
                new IncorePoint(inputHurricane.getLandfallLocation()),
                inputHurricane.getGridResolution(), inputHurricane.getGridPoints(), inputHurricane.getRfMethod());

            try {
                ObjectMapper mapper = new ObjectMapper();
                String ensemBleString = mapper.writeValueAsString(hurricaneSimulationEnsemble);

                hurricaneWindfields.setName(inputHurricane.getName());
                hurricaneWindfields.setDescription(inputHurricane.getDescription());

                hurricaneWindfields.setCategory(inputHurricane.getCategory());
                hurricaneWindfields.setCoast(inputHurricane.getCoast());
                hurricaneWindfields.setGridResolution(inputHurricane.getGridResolution());
                hurricaneWindfields.setRasterResolution(inputHurricane.getRasterResolution());
                hurricaneWindfields.setTransD(inputHurricane.getTransD());
                hurricaneWindfields.setModelUsed(hurricaneSimulationEnsemble.getModelUsed());
                hurricaneWindfields.setLandfallLocation(inputHurricane.getLandfallLocation());
                hurricaneWindfields.setPrivileges(Privileges.newWithSingleOwner(username));
                hurricaneWindfields.setTimes(hurricaneSimulationEnsemble.getTimes());
                hurricaneWindfields.setGridPoints(inputHurricane.getGridPoints());
                hurricaneWindfields.setHazardDatasets(GISHurricaneUtils.processHurricaneFromJson(ensemBleString,
                    inputHurricane.getRasterResolution(), username));

                hurricaneWindfields.setPrivileges(Privileges.newWithSingleOwner(username));

                repository.addHurricane(hurricaneWindfields);
            } catch (JsonGenerationException e) {
                throw new NotFoundException("Error finding a mapping for the coast and category");
            } catch (JsonProcessingException e) {
                throw new NotFoundException("Couldn't process json");
            } catch (MismatchedDimensionException e) {
                throw new NotFoundException("Error in geometry dimensions");
            }
        }
        return hurricaneWindfields;
    }

    @GET
    @Path("json/{coast}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(hidden = true, value = "Simulates a hurricane by returning the result as json.",
        notes = "It is implemented to match MATLAB output and need not be exposed to external users")
    public HurricaneSimulationEnsemble getHurricaneJsonByCategory(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Hurricane coast. Ex: 'gulf, florida or east'.", required = true) @PathParam("coast") String coast,
        @ApiParam(value = "Hurricane category. Ex: between 1 and 5.", required = true) @QueryParam("category") int category,
        @ApiParam(value = "Huricane landfall direction angle. Ex: 30.5.", required = true) @QueryParam("TransD") double transD,
        @ApiParam(value = "Huricane landfall location. Ex: '28.09,-80.62'.", required = true) @QueryParam("LandfallLoc") IncorePoint landfallLoc,
        @ApiParam(value = "Resolution. Ex: 6.", required = true) @QueryParam("resolution") @DefaultValue("6") int resolution,
        @ApiParam(value = "Number of grid points. Ex: 80.", required = true) @QueryParam("gridPoints") @DefaultValue("80") int gridPoints,
        @ApiParam(value = "Reduction type. Ex: 'circular'.", required = true) @QueryParam("reductionType") @DefaultValue("circular") String rfMethod) {

        //TODO: Handle both cases Sandy/sandy. Standardize to lower case?
        if (coast == null || category <= 0 || category > 5) {
            throw new NotFoundException("Coast needs to be gulf, florida or east. Category should be between 1 to 5");
        }

        if (HurricaneUtil.categoryMapping.get(coast) != null) {
            return HurricaneCalc.simulateHurricane(username, transD, landfallLoc,
                HurricaneUtil.categoryMapping.get(coast)[category - 1], resolution, gridPoints, rfMethod);
        } else {
            throw new NotFoundException("Error finding a mapping for the coast and category");
        }
    }


}
