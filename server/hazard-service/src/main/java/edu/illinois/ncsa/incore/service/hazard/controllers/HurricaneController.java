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
import io.swagger.annotations.ApiOperation;
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


@Path("hurricaneWindfields")
public class HurricaneController {
    private static final Logger log = Logger.getLogger(HurricaneController.class);

    @Inject
    private IAuthorizer authorizer;

    @Inject
    private IHurricaneRepository repository;

    private GeometryFactory factory = new GeometryFactory();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<HurricaneWindfields> getHurricaneWindfields(@HeaderParam("X-Credential-Username") String username,
                                                            @QueryParam("coast") String coast, @QueryParam("category") int category) {

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

        return hurricaneWindfields;
    }

    @GET
    @Path("{hurricaneId}")
    @Produces({MediaType.APPLICATION_JSON})
    public HurricaneWindfields getHurricaneWindfieldsById(@HeaderParam("X-Credential-Username") String username, @PathParam("hurricaneId") String hurricaneId) {
        HurricaneWindfields hurricane = repository.getHurricaneById(hurricaneId);
        if (!authorizer.canRead(username, hurricane.getPrivileges())) {
            throw new ForbiddenException();
        }
        return hurricane;
    }


    @GET
    @Path("{hurricaneId}/values")
    @Produces({MediaType.APPLICATION_JSON})
    public List<HurricaneWindfieldResult> getHurricaneWindfieldValues(@HeaderParam("X-Credential-Username") String username,
                                                                      @PathParam("hurricaneId") String hurricaneId,
                                                                      @DefaultValue("velocity") @QueryParam("demandType") String demandType,
                                                                      @DefaultValue("kt") @QueryParam("demandUnits") WindfieldDemandUnits demandUnits,
                                                                      @QueryParam("point") List<IncorePoint> points) {
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
    public HurricaneWindfields createHurricaneWindfields(@HeaderParam("X-Credential-Username") String username, HurricaneWindfields inputHurricane) {
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
    @ApiOperation(value="Simulates a hurricane by returning the result as json", notes= "Implemented to match MATLAB model output " +
        "and need not be exposed to external users", hidden = true)
    public HurricaneSimulationEnsemble getHurricaneJsonByCategory(@HeaderParam("X-Credential-Username") String username,
                                                                  @PathParam("coast") String coast,
                                                                  @QueryParam("category") int category,
                                                                  @QueryParam("TransD") double transD, @QueryParam("LandfallLoc") IncorePoint landfallLoc,
                                                                  @DefaultValue("6") @QueryParam("resolution") int resolution,
                                                                  @DefaultValue("80") @QueryParam("gridPoints") int gridPoints,
                                                                  @DefaultValue("circular") @QueryParam("reductionType") String rfMethod) {
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
