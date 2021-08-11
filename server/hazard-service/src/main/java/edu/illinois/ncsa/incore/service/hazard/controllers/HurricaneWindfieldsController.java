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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneWindfieldsRepository;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.GISHurricaneUtils;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneSimulationDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneSimulationEnsemble;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneWindfields;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.types.HurricaneWindfieldResult;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.types.WindfieldsDemandUnits;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.utils.HurricaneWindfieldsCalc;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.utils.HurricaneWindfieldsUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.opengis.geometry.MismatchedDimensionException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value = "hurricaneWindfields", authorizations = {})

@Path("hurricaneWindfields")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class HurricaneWindfieldsController {
    private static final Logger log = Logger.getLogger(HurricaneWindfieldsController.class);
    private final String username;

    @Inject
    private IHurricaneWindfieldsRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public HurricaneWindfieldsController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all hurricanes.")
    public List<HurricaneWindfields> getHurricaneWindfields(
        @ApiParam(value = "Hurricane coast. Ex: 'gulf, florida or east'.", required = true) @QueryParam("coast") String coast,
        @ApiParam(value = "Hurricane category. Ex: between 1 and 5.", required = true) @QueryParam("category") int category,
        @ApiParam(value = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<HurricaneWindfields> hurricaneWindfields = repository.getHurricaneWindfields();
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any space with name " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges())) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            hurricaneWindfields = hurricaneWindfields.stream()
                .filter(hurricane -> spaceMembers.contains(hurricane.getId()))
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());
            return hurricaneWindfields;
        }

        if (coast != null) {
            hurricaneWindfields = hurricaneWindfields.stream().filter(s -> s.getCoast().equals(coast)).collect(Collectors.toList());
        }

        if (category > 0) {
            hurricaneWindfields = hurricaneWindfields.stream().filter(s -> s.getCategory() == category).collect(Collectors.toList());
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        List<HurricaneWindfields> accessibleHurricaneWindfields = hurricaneWindfields.stream()
            .filter(hurricaneWindfield -> membersSet.contains(hurricaneWindfield.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleHurricaneWindfields;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new hurricane, simulation of hurricane windfields is returned.",
        notes = "One dataset for each time frame of the simulation is returned representing the hurricane " +
            "windfield's raster. Each cell represents the windspeed at 10m elevation and 3-sec wind gust by default")
    public HurricaneWindfields createHurricaneWindfields(
        HurricaneWindfields inputHurricane) {

        HurricaneWindfields hurricaneWindfields = new HurricaneWindfields();
        if (inputHurricane != null) {
            String demandType = inputHurricane.getDemandType().trim();

            if (!demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_3SECS) && !demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS)) {
                log.error("Unsupported hurricane demandType provided in POST JSON : " + demandType);
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unsupported hurricane demandType. Please use 3s or 60s");
            }
            HurricaneSimulationEnsemble hurricaneSimulationEnsemble = getHurricaneJsonByCategory(inputHurricane.getCoast(),
                inputHurricane.getCategory(), inputHurricane.getTransD(),
                new IncorePoint(inputHurricane.getLandfallLocation()), demandType, inputHurricane.getDemandUnits().toString().trim(),
                inputHurricane.getGridResolution(), inputHurricane.getGridPoints(), inputHurricane.getRfMethod());

            try {
                ObjectMapper mapper = new ObjectMapper();
                String ensemBleString = mapper.writeValueAsString(hurricaneSimulationEnsemble);

                hurricaneWindfields.setName(inputHurricane.getName());
                hurricaneWindfields.setDescription(inputHurricane.getDescription());
                hurricaneWindfields.setCreator(this.username);

                hurricaneWindfields.setCategory(inputHurricane.getCategory());
                hurricaneWindfields.setCoast(inputHurricane.getCoast());
                hurricaneWindfields.setGridResolution(inputHurricane.getGridResolution());
                hurricaneWindfields.setRasterResolution(inputHurricane.getRasterResolution());
                hurricaneWindfields.setTransD(inputHurricane.getTransD());
                hurricaneWindfields.setModelUsed(hurricaneSimulationEnsemble.getModelUsed());
                hurricaneWindfields.setLandfallLocation(inputHurricane.getLandfallLocation());
                hurricaneWindfields.setTimes(hurricaneSimulationEnsemble.getTimes());
                hurricaneWindfields.setGridPoints(inputHurricane.getGridPoints());

                hurricaneWindfields.setDemandType(inputHurricane.getDemandType());
                hurricaneWindfields.setDemandUnits(inputHurricane.getDemandUnits());

                hurricaneWindfields.setHazardDatasets(GISHurricaneUtils.processHurricaneFromJson(ensemBleString,
                    inputHurricane.getRasterResolution(), this.username));

                //save hurricane
                hurricaneWindfields = repository.addHurricaneWindfields(hurricaneWindfields);

                //add hurricane to the user's space
                Space space = spaceRepository.getSpaceByName(this.username);
                if (space != null) {
                    space.addMember(hurricaneWindfields.getId());
                    spaceRepository.addSpace(space);
                } else {
                    space = new Space(this.username);
                    space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    space.addMember(hurricaneWindfields.getId());
                    spaceRepository.addSpace(space);
                }

            } catch (JsonGenerationException e) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error finding a mapping for the coast and category");
            } catch (JsonProcessingException e) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Couldn't process json");
            } catch (MismatchedDimensionException e) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error in geometry dimensions");
            }
        }
        hurricaneWindfields.setSpaces(spaceRepository.getSpaceNamesOfMember(hurricaneWindfields.getId()));
        return hurricaneWindfields;
    }

    @GET
    @Path("{hurricaneId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the hurricane with matching id.")
    public HurricaneWindfields getHurricaneWindfieldsById(
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricaneId") String hurricaneId) {

        HurricaneWindfields hurricane = repository.getHurricaneWindfieldsById(hurricaneId);
        if (hurricane == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a hurricane with id " + hurricaneId);
        }

        hurricane.setSpaces(spaceRepository.getSpaceNamesOfMember(hurricaneId));

        if (authorizer.canUserReadMember(this.username, hurricaneId, spaceRepository.getAllSpaces())) {
            return hurricane;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You are not authorized to access the hurricane " + hurricaneId);
    }

    @POST
    @Path("{hurricaneId}/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns hurricane wind field values for a set of locations",
        notes = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postHurricaneWindFieldValues(
        @ApiParam(value = "hurricane wind field Id", required = true)
        @PathParam("hurricaneId") String hurricaneId,
        @ApiParam(value = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr,
        @ApiParam(value = "Elevation in meters at which wind speed has to be calculated.") @FormDataParam("elevation") @DefaultValue("10" +
            ".0") double elevation,
        @ApiParam(value = "Terrain exposure or roughness length. Acceptable range is 0.003 to 2.5 ") @FormDataParam("roughness") @DefaultValue("0.03") double roughness) {

        HurricaneWindfields hurricane = getHurricaneWindfieldsById(hurricaneId);
        //Get shapefile datasetid
        String datasetId = hurricane.findFullPathDatasetId();
        String hurrDemandType = hurricane.getDemandType();
        String hurrDemandUnits = hurricane.getDemandUnits().toString();

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ValuesResponse> valResponse = new ArrayList<>();
            List<ValuesRequest> valuesRequest = mapper.readValue(requestJsonStr, new TypeReference<List<ValuesRequest>>() {
            });
            for (ValuesRequest request : valuesRequest) {
                List<String> demands = request.getDemands();
                List<String> units = request.getUnits();
                List<Double> hazVals = new ArrayList<>();
                List<String> resDemands = new ArrayList<>();
                List<String> resUnits = new ArrayList<>();

                CommonUtil.validateHazardValuesInput(demands, units, request.getLoc());

                for (int i = 0; i < demands.size(); i++) {

                    if (!demands.get(i).equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_3SECS) && !demands.get(i).equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS)) {
                        log.error("Unsupported hurricane demandType provided to GET values : " + demands.get(i));
                        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unsupported hurricane demandType. Please use 3s or " +
                            "60s.");
                    }

                    double lat = request.getLoc().getLocation().getY();
                    double lon = request.getLoc().getLocation().getX();
                    double windValue = 0;
                    try {
                        windValue = GISHurricaneUtils.CalcVelocityFromPoint(datasetId, this.username, lat, lon); // 3s gust at 10m elevation

                        HashMap<String, Double> convertedWf = HurricaneWindfieldsUtil.convertWindfieldVelocity(hurrDemandType, windValue,
                            elevation, roughness);
                        windValue = convertedWf.get(demands.get(i));

                        if (!units.get(i).equals(hurrDemandUnits)) {
                            windValue = HurricaneWindfieldsUtil.getCorrectUnitsOfVelocity(windValue, hurrDemandUnits, units.get(i));
                        }
                    } catch (IOException e) {
                        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "IOException: Please check the json format for the " +
                            "points.");

                    }

                    HurricaneWindfieldResult res = new HurricaneWindfieldResult(lat, lon, windValue,
                        demands.get(i), units.get(i));

                    resDemands.add(res.getDemandType());
                    resUnits.add(res.getDemandUnits());
                    hazVals.add(res.getHazardValue());
                }

                ValuesResponse response = new ValuesResponse();
                response.setHazardValues(hazVals);
                response.setDemands(resDemands);
                response.setUnits(resUnits);
                response.setLoc(request.getLoc());
                valResponse.add(response);
            }
            return valResponse;
        } catch (IOException ex) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "IOException: Please check the json format for the points.");
        } catch (IllegalArgumentException e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid arguments provided to the api, check the format of your " +
                "request.");
        }
    }

    @GET
    @Path("{hurricaneId}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the hurricane wind field values.")
    @Deprecated
    public List<HurricaneWindfieldResult> getHurricaneWindfieldValues(
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricaneId") String hurricaneId,
        @ApiParam(value = "Hurricane demand type. Ex. '3s', '60s'.") @QueryParam("demandType") @DefaultValue(HurricaneWindfieldsUtil.WIND_VELOCITY_3SECS) String demandType,
        @ApiParam(value = "Hurricane demand unit.") @QueryParam("demandUnits") @DefaultValue(HurricaneWindfieldsUtil.UNITS_MPH) WindfieldsDemandUnits demandUnits,
        @ApiParam(value = "Elevation in meters at which wind speed has to be calculated.") @QueryParam("elevation") @DefaultValue("10.0") double elevation,
        @ApiParam(value = "Terrain exposure or roughness length. Acceptable range is 0.003 to 2.5 ") @QueryParam("roughness") @DefaultValue("0.03") double roughness,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.09,-80.62'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        HurricaneWindfields hurricane = getHurricaneWindfieldsById(hurricaneId);
        List<HurricaneWindfieldResult> hurrResults = new ArrayList<>();

        //Get shapefile datasetid
        String datasetId = hurricane.findFullPathDatasetId();
        String hurrDemandType = hurricane.getDemandType();
        String hurrDemandUnits = hurricane.getDemandUnits().toString();

        if (hurricane != null) {

            if (!demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_3SECS) && !demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS)) {
                log.error("Unsupported hurricane demandType provided to GET values : " + demandType);
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unsupported hurricane demandType. Please use 3s or 60s.");
            }

            for (IncorePoint point : points) {
                double windValue = 0;
                double lat = point.getLocation().getY();
                double lon = point.getLocation().getX();
                try {

                    windValue = GISHurricaneUtils.CalcVelocityFromPoint(datasetId, this.username, lat, lon); // 3s gust at 10m elevation

                    HashMap<String, Double> convertedWf = HurricaneWindfieldsUtil.convertWindfieldVelocity(hurrDemandType, windValue,
                        elevation, roughness);
                    windValue = convertedWf.get(demandType);

                    if (!demandUnits.toString().equals(hurrDemandUnits)) {
                        windValue = HurricaneWindfieldsUtil.getCorrectUnitsOfVelocity(windValue, hurrDemandUnits, demandUnits.toString());
                    }
                } catch (IOException e) {
                    log.error("Velocity calculation failed from the shapefile");
                }

                HurricaneWindfieldResult res = new HurricaneWindfieldResult(lat, lon, windValue, demandType, demandUnits.toString());
                hurrResults.add(res);
            }
        }

        return hurrResults;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hurricaneId}")
    @ApiOperation(value = "Deletes a Hurricane Windfield", notes = "Also deletes attached datasets and related files")
    public HurricaneWindfields deleteHurricaneWindfields(@ApiParam(value = "Hurricane Windfield Id", required = true) @PathParam(
        "hurricaneId") String hurricaneId) {
        HurricaneWindfields hurricane = getHurricaneWindfieldsById(hurricaneId);

        if (authorizer.canUserDeleteMember(this.username, hurricaneId, spaceRepository.getAllSpaces())) {
            //delete associated datasets
            for (HurricaneSimulationDataset dataset : hurricane.getHazardDatasets()) {
                if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username) == null) {
                    spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                }
            }

            HurricaneWindfields deletedHurricane = repository.deleteHurricaneWindfieldsById(hurricaneId); // remove hurricane document

            // remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(hurricaneId)) {
                    space.removeMember(hurricaneId);
                    spaceRepository.addSpace(space);
                }
            }

            return deletedHurricane;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " hurricane " + hurricaneId);
        }
    }

    @GET
    @Path("json/{coast}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(hidden = true, value = "Simulates a hurricane by returning the result as json.",
        notes = "It is implemented to match MATLAB output and need not be exposed to external users")
    public HurricaneSimulationEnsemble getHurricaneJsonByCategory(
        @ApiParam(value = "Hurricane coast. Ex: 'gulf, florida or east'.", required = true) @PathParam("coast") String coast,
        @ApiParam(value = "Hurricane category. Ex: between 1 and 5.", required = true) @QueryParam("category") int category,
        @ApiParam(value = "Huricane landfall direction angle. Ex: 30.5.", required = true) @QueryParam("TransD") double transD,
        @ApiParam(value = "Huricane landfall location. Ex: '28.09,-80.62'.", required = true) @QueryParam("LandfallLoc") IncorePoint landfallLoc,
        @ApiParam(value = "Hurricane demand type. Ex. '3s', '60s'.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Hurricane demand unit.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Resolution. Ex: 6.", required = true) @QueryParam("resolution") @DefaultValue("6") int resolution,
        @ApiParam(value = "Number of grid points. Ex: 80.", required = true) @QueryParam("gridPoints") @DefaultValue("80") int gridPoints,
        @ApiParam(value = "Reduction type. Ex: 'circular'.", required = true) @QueryParam("reductionType") @DefaultValue("circular") String rfMethod) {

        //TODO: Handle both cases Sandy/sandy. Standardize to lower case?
        if (coast == null || category <= 0 || category > 5) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Coast needs to be gulf, florida or east. Category should be " +
                "between 1 to 5");
        }

        if (HurricaneWindfieldsUtil.categoryMapping.get(coast) != null) {
            return HurricaneWindfieldsCalc.simulateHurricane(this.username, transD, landfallLoc,
                HurricaneWindfieldsUtil.categoryMapping.get(coast)[category - 1], demandType, demandUnits, resolution, gridPoints,
                rfMethod);
        } else {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Error finding a mapping for the coast and category");
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all hurricanes", notes = "Gets all hurricanes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No hurricanes found with the searched text")
    })
    public List<HurricaneWindfields> findHurricaneWindfields(
        @ApiParam(value = "Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<HurricaneWindfields> hurricanes;
        HurricaneWindfields hurricane = repository.getHurricaneWindfieldsById(text);
        if (hurricane != null) {
            hurricanes = new ArrayList<HurricaneWindfields>() {{
                add(hurricane);
            }};
        } else {
            hurricanes = this.repository.searchHurricaneWindfields(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        hurricanes = hurricanes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return hurricanes;
    }

}
