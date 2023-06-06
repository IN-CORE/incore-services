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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ICommonRepository;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.*;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.*;
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.hurricaneComparator;
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.tornadoComparator;

@Api(value = "tornadoes", authorizations = {})

@Path("tornadoes")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error.")
})
public class TornadoController {
    private static final Logger logger = Logger.getLogger(TornadoController.class);
    private final String username;
    private final List<String> groups;

    @Inject
    private ITornadoRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    private ICommonRepository commonRepository;

    @Inject
    private IAuthorizer authorizer;

    private final GeometryFactory factory = new GeometryFactory();

    @Inject
    public TornadoController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @ApiParam(value = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all tornadoes.")
    public List<Tornado> getTornadoes(
        @ApiParam(value = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @ApiParam(value = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import tornado comparator
        Comparator<Tornado> comparator = tornadoComparator(sortBy, order);

        List<Tornado> tornadoes = repository.getTornadoes();
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the space " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges(), this.groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            tornadoes = tornadoes.stream()
                .filter(hurricane -> spaceMembers.contains(hurricane.getId()))
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());
            return tornadoes;
        }
        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);

        List<Tornado> accessibleTornadoes = tornadoes.stream()
            .filter(tornado -> membersSet.contains(tornado.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleTornadoes;
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new tornado, the newly created tornado is returned.",
        notes = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create both model tornadoes and dataset-based tornadoes with GeoTiff files uploaded.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tornado", value = "Tornado json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Tornado files.", required = true, dataType = "string", paramType = "form")
    })
    public Tornado createTornado(
        @ApiParam(hidden = true) @FormDataParam("tornado") String tornadoJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        // error messages for tornado creation
        String tornadoErrorMsg = "Could not create Tornado, check the format and files of your request. " +
            "For dataset based tornado, the shapefile needs to have a GUID";
        String tornadoJsonErrorMsg = "Could not create Tornado. Check your tornado json.";

        // check if the user's number of the hazard is within the allocation
        if (!AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, this.username, "hazards")) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_ALLOCATION_MESSAGE);
        }

        // check if the user's number of the hazard dataset is within the allocation
        if (!AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, this.username, "hazardDatasets")) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DATASET_ALLOCATION_MESSAGE);
        }

        // check if the user's hazard dataset file size is within the allocation
        if (!AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, this.username, "hazardDatasetSize")) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                AllocationConstants.HAZARD_DATASET_ALLOCATION_FILESIZE_MESSAGE);
        }

        ObjectMapper mapper = new ObjectMapper();
        Tornado tornado = null;
        String datasetId = null;
        try {
            tornado = mapper.readValue(tornadoJson, Tornado.class);
            UserInfoUtils.throwExceptionIfIdPresent(tornado.getId());

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
                    throw new IncoreHTTPException(Response.Status.NOT_IMPLEMENTED,
                        "Requested tornado model, " + tornadoModel.getTornadoModel() + " is not yet implemented.");
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
                JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", "EF Boxes representing tornado");

                // Store the dataset
                datasetId = ServiceUtil.createDataset(datasetObject, this.username, files);
                tornadoModel.setDatasetId(datasetId);

                tornado.setCreator(this.username);
                tornado = repository.addTornado(tornado);
                addTornadoToSpace(tornado, this.username);
            } else if (tornado != null && tornado instanceof TornadoDataset) {
                TornadoDataset tornadoDataset = (TornadoDataset) tornado;
                datasetId = null;
                if (fileParts != null && !fileParts.isEmpty() && TornadoUtils.validateDatasetTypes(fileParts)) {
                    // Create dataset object representation for storing shapefile
                    JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", "EF Boxes representing tornado");
                    // Store the dataset
                    datasetId = ServiceUtil.createDataset(datasetObject, this.username);
                    if (datasetId != null) {
                        // attach files to the dataset
                        int statusCode = ServiceUtil.attachFileToTornadoDataset(datasetId, this.username, fileParts);
                        if (statusCode != HttpStatus.SC_OK) {
                            ServiceUtil.deleteDataset(datasetId, this.username);
                            logger.error(tornadoErrorMsg);
                            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoErrorMsg);
                        }
                    } else {
                        logger.error(tornadoJsonErrorMsg);
                        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoJsonErrorMsg);
                    }
                    ((TornadoDataset) tornado).setDatasetId(datasetId);

                    tornado.setCreator(this.username);
                    tornado = repository.addTornado(tornado);
                    addTornadoToSpace(tornado, this.username);
                } else {
                    ServiceUtil.deleteDataset(datasetId, this.username);
                    logger.error(tornadoErrorMsg);
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoErrorMsg);
                }
            }

            // add one more dataset in the usage
            AllocationUtils.increaseUsage(allocationsRepository, this.username, "hazards");

            tornado.setSpaces(spaceRepository.getSpaceNamesOfMember(tornado.getId()));
            return tornado;
        } catch (IOException e) {
            logger.error("Error mapping the request to a supported Tornado type.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Illegal Argument has been passed in.", e);
        }
        ServiceUtil.deleteDataset(datasetId, this.username);
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoErrorMsg);

    }

    @GET
    @Path("{tornado-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the tornado with matching id.")
    public Tornado getTornado(
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        Tornado tornado = repository.getTornadoById(tornadoId);
        if (tornado == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a tornado with id " + tornadoId);
        }

        tornado.setSpaces(spaceRepository.getSpaceNamesOfMember(tornadoId));

        Space space = spaceRepository.getSpaceByName(this.username);
        if (space != null && space.hasMember(tornadoId)) {
            return tornado;
        }

        if (authorizer.canUserReadMember(this.username, tornadoId, spaceRepository.getAllSpaces(), this.groups)) {
            return tornado;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN,
            this.username + " does not have the privileges to access the tornado " + tornadoId);
    }

    @GET
    @Path("{tornado-id}/value")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the wind speed at given location using the specified tornado.")
    @Deprecated
    public WindHazardResult getTornadoHazard(
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Latitude of a site. Ex: 35.027.", required = true) @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude of a site. Ex: -90.131.", required = true) @QueryParam("siteLong") double siteLong,
        @ApiParam(value = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation,
        @ApiParam(value = "Seed value for random values. Ex: 1000") @QueryParam("seed") @DefaultValue("-1") int seed) throws Exception {

        Tornado tornado = getTornado(tornadoId);
        if (tornado != null) {
            Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));

            try {
                return TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation, seed, this.username);
            } catch (Exception e) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error computing hazard." + e.getMessage());
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Tornado with id " + tornadoId + " was not found.");
        }
    }

    @POST
    @Path("{tornado-id}/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns tornado values for a set of locations",
        notes = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postTornadoValues(
        @ApiParam(value = "Tornado Id", required = true)
        @PathParam("tornado-id") String tornadoId,
        @ApiParam(value = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr,
        @ApiParam(value = "Simulated wind hazard index. Ex: 0 for first, 1 for second and so on")
        @FormDataParam("simulation") @DefaultValue("0") int simulation,
        @ApiParam(value = "Seed value for random values. Ex: 1000")
        @FormDataParam("seed") @DefaultValue("-1") int seed) {

        Tornado tornado = getTornado(tornadoId);

        // check if demand type is correct according to the definition; for now get the first definition
        // Check units to verify requested units matches the demand type
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        JSONArray listOfDemands = demandDefinition.getJSONArray("tornado");

        ObjectMapper mapper = new ObjectMapper();
        List<ValuesResponse> valResponse = new ArrayList<>();
        try {
            List<ValuesRequest> valuesRequest = mapper.readValue(requestJsonStr, new TypeReference<List<ValuesRequest>>() {
            });
            for (ValuesRequest request : valuesRequest) {
                List<String> demands = request.getDemands();
                List<String> units = request.getUnits();
                List<Double> hazVals = new ArrayList<>();
                List<String> resDemands = new ArrayList<>();
                List<String> resUnits = new ArrayList<>();

                if (!CommonUtil.validateHazardValuesInputs(demands, units, request.getLoc())) {
                    for (int i = 0; i < demands.size(); i++) {
                        hazVals.add(MISSING_REQUIRED_INPUTS);
                    }
                    resUnits = units;
                    resDemands = demands;
                } else {
                    for (int i = 0; i < demands.size(); i++) {
                        try {
                            if (!HazardUtil.verifyHazardDemandType(demands.get(i), listOfDemands)) {
                                hazVals.add(INVALID_DEMAND);
                                resUnits.add(units.get(i));
                                resDemands.add(demands.get(i));
                            } else {
                                if (!HazardUtil.verifyHazardDemandUnit(demands.get(i), units.get(i), listOfDemands)) {
                                    hazVals.add(INVALID_UNIT);
                                    resUnits.add(units.get(i));
                                    resDemands.add(demands.get(i));
                                } else {
                                    WindHazardResult res = TornadoCalc.getWindHazardAtSite(tornado,
                                        request.getLoc().getLocation(), units.get(i), simulation, seed, this.username);
                                    resDemands.add(res.getDemand());
                                    resUnits.add(res.getUnits());
                                    hazVals.add(res.getHazardValue());
                                }
                            }
                        } catch (IOException ex) {
                            hazVals.add(IO_EXCEPTION);
                            resUnits.add(units.get(i));
                            resDemands.add(demands.get(i));
                        } catch (Exception ex) {
                            hazVals.add(UNHANDLED_EXCEPTION);
                            resUnits.add(units.get(i));
                            resDemands.add(demands.get(i));
                        }
                    }
                }
                ValuesResponse response = new ValuesResponse();
                response.setHazardValues(hazVals);
                response.setDemands(resDemands);
                response.setUnits(resUnits);
                response.setLoc(request.getLoc());

                valResponse.add(response);
            }
        } catch (JsonProcessingException ex) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "JsonProcessingException: Please check the format of the json " +
                "request and that demands are provided for each location. This can also happen if the format of the locations are " +
                "incorrect. The format of the location needs to be decimalLat,decimalLong");
        } catch (Exception ex) {
            // Return the stack trace along with the api response.
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "An unhandled error occurred when calculating" +
                " hazard value. Please report this to dev team. \n\n More details: \n" + sw);
        }
        return valResponse;
    }

    @GET
    @Path("{tornado-id}/dataset")
    @Produces({MediaType.TEXT_PLAIN})
    @ApiOperation(value = "Returns a zip shapefile representing tornado defined by given id.")
    public Response getFile(
        @ApiParam(value = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        // TODO implement this and change MediaType to Octet Stream
        return Response.ok("Shapefile representing tornado not yet implemented.").build();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all tornadoes", notes = "Gets all tornadoes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No tornadoes found with the searched text")
    })
    public List<Tornado> findTornadoes(
        @ApiParam(value = "Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @ApiParam(value = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import tornado comparator
        Comparator<Tornado> comparator = tornadoComparator(sortBy, order);

        List<Tornado> tornadoes;
        Tornado tornado = repository.getTornadoById(text);
        if (tornado != null) {
            tornadoes = new ArrayList<Tornado>() {{
                add(tornado);
            }};
        } else {
            tornadoes = this.repository.searchTornadoes(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);

        tornadoes = tornadoes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return tornadoes;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tornado-id}")
    @ApiOperation(value = "Deletes a tornado", notes = "Also deletes attached dataset and related files")
    public Tornado deleteTornado(@ApiParam(value = "Tornado Id", required = true) @PathParam("tornado-id") String tornadoId) {
        Tornado tornado = getTornado(tornadoId);

        if (authorizer.canUserDeleteMember(this.username, tornadoId, spaceRepository.getAllSpaces(), this.groups)) {
            // delete associated datasets
            if (tornado != null && tornado instanceof TornadoModel) {
                TornadoModel tModel = (TornadoModel) tornado;
                if (ServiceUtil.deleteDataset(tModel.getDatasetId(), this.username) == null) {
                    spaceRepository.addToOrphansSpace(tModel.getDatasetId());
                }
            } else if (tornado != null && tornado instanceof TornadoDataset) {
                TornadoDataset tDataset = (TornadoDataset) tornado;
                ServiceUtil.deleteDataset(tDataset.getDatasetId(), this.username);
                if (ServiceUtil.deleteDataset(tDataset.getDatasetId(), this.username) == null) {
                    spaceRepository.addToOrphansSpace(tDataset.getDatasetId());
                }
            }

            Tornado deletedTornado = repository.deleteTornadoById(tornadoId); // remove tornado json

            // remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(tornadoId)) {
                    space.removeMember(tornadoId);
                    spaceRepository.addSpace(space);
                }
            }

            // reduce the number of hazard from the space
            AllocationUtils.decreaseUsage(allocationsRepository, this.username, "hazards");

            return deletedTornado;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " tornado " + tornadoId);
        }
    }

    //For adding tornado id to user's space
    private void addTornadoToSpace(Tornado tornado, String username) {
        Space space = spaceRepository.getSpaceByName(username);
        if (space != null) {
            space.addMember(tornado.getId());
            spaceRepository.addSpace(space);
        } else {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
            space.addMember(tornado.getId());
            spaceRepository.addSpace(space);
        }
    }

}
