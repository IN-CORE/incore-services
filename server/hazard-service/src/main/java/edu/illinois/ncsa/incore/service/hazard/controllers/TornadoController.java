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
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ICommonRepository;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.DemandDefinition;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import edu.illinois.ncsa.incore.common.utils.DemandUtils;
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
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.tornadoComparator;

@Tag(name = "tornadoes")

@Path("tornadoes")
@ApiResponses(value = {
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class TornadoController {
    private static final Logger logger = Logger.getLogger(TornadoController.class);
    private final String username;
    private final List<String> groups;
    private final String userGroups;

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
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all tornadoes.")
    public List<Tornado> getTornadoes(
        @Parameter(name = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

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

    @GET
    @Path("/demands")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all tornado allowed demand types and units.")
    public List<DemandDefinition> getTornadoDemands() {
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        return DemandUtils.getAllowedDemands(demandDefinition, "tornado");
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new tornado, the newly created tornado is returned.",
        description = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create both model tornadoes and dataset-based tornadoes with GeoTiff files uploaded.")

    @RequestBody(description = "Tornado json and files.", required = true,
        content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
            schema = @Schema(type = "object",
                properties = {@StringToClassMapItem(key = "tornado", value = String.class),
                    @StringToClassMapItem(key = "file", value = String.class)}
            )
        )
    )
    public Tornado createTornado(
        @Parameter(hidden = true) @FormDataParam("tornado") String tornadoJson,
        @Parameter(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

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
                datasetId = ServiceUtil.createDataset(datasetObject, this.username, this.userGroups, files);
                // Assuming only one hazardDataset will be created here.
                TornadoHazardDataset hazardDataset = tornadoModel.getHazardDatasets().get(0);
                hazardDataset.setDatasetId(datasetId);

                tornado.setCreator(this.username);
                tornado.setOwner(this.username);
                tornado = repository.addTornado(tornado);
                addTornadoToSpace(tornado, this.username);
            } else if (tornado != null && tornado instanceof TornadoDataset) {
                TornadoDataset tornadoDataset = (TornadoDataset) tornado;

                // We assume the input files in the request are in the same order listed in the tornado dataset object and zipped
                int hazardDatasetIdx = 0;
                if (fileParts != null && !fileParts.isEmpty() && TornadoUtils.validateDatasetTypes(fileParts) &&
                    (tornadoDataset.getHazardDatasets().size() == fileParts.size())) {
                    for (FormDataBodyPart filePart: fileParts) {
                        datasetId = null;
                        // not sure if this is the right way to handle this
                        List<FormDataBodyPart> filePartList = new ArrayList<>();
                        filePartList.add((filePart));

                        TornadoHazardDataset hazardDataset = tornadoDataset.getHazardDatasets().get(hazardDatasetIdx);
                        hazardDatasetIdx++;

                        // Create dataset object representation for storing shapefile
                        JSONObject datasetObject = TornadoUtils.getTornadoDatasetObject("Tornado Hazard", "EF Boxes representing tornado");
                        // Store the dataset
                        datasetId = ServiceUtil.createDataset(datasetObject, this.username, this.userGroups);

                        if (datasetId != null) {
                            // attach files to the dataset
                            int statusCode = ServiceUtil.attachFileToTornadoDataset(datasetId, this.username, this.userGroups, filePartList);
                            if (statusCode != HttpStatus.SC_OK) {
                                ServiceUtil.deleteDataset(datasetId, this.username, this.userGroups);
                                logger.error(tornadoErrorMsg);
                                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoErrorMsg);
                            }
                        } else {
                            logger.error(tornadoJsonErrorMsg);
                            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoJsonErrorMsg);
                        }
                        hazardDataset.setDatasetId(datasetId);
                    }


                    tornado.setCreator(this.username);
                    tornado.setOwner(this.username);
                    tornado = repository.addTornado(tornado);
                    addTornadoToSpace(tornado, this.username);
                } else {
                    ServiceUtil.deleteDataset(datasetId, this.username, this.userGroups);
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
        ServiceUtil.deleteDataset(datasetId, this.username, this.userGroups);
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, tornadoErrorMsg);

    }

    @GET
    @Path("{tornado-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns the tornado with matching id.")
    public Tornado getTornado(
        @Parameter(name = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

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
    @Operation(summary = "Returns the wind speed at given location using the specified tornado.")
    @Deprecated
    public WindHazardResult getTornadoHazard(
        @Parameter(name = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId,
        @Parameter(name = "Tornado demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @Parameter(name = "Latitude of a site. Ex: 35.027.", required = true) @QueryParam("siteLat") double siteLat,
        @Parameter(name = "Longitude of a site. Ex: -90.131.", required = true) @QueryParam("siteLong") double siteLong,
        @Parameter(name = "Simulated wind hazard. Ex: 0.") @QueryParam("simulation") @DefaultValue("0") int simulation,
        @Parameter(name = "Seed value for random values. Ex: 1000") @QueryParam("seed") @DefaultValue("-1") int seed) throws Exception {

        Tornado tornado = getTornado(tornadoId);
        if (tornado != null) {
            Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));

            try {
                return TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation, seed, this.username, this.userGroups);
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
    @Operation(summary = "Returns tornado values for a set of locations",
        description = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postTornadoValues(
        @Parameter(name = "Tornado Id", required = true)
        @PathParam("tornado-id") String tornadoId,
        @Parameter(name = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr,
        @Parameter(name = "Simulated wind hazard index. Ex: 0 for first, 1 for second and so on")
        @FormDataParam("simulation") @DefaultValue("0") int simulation,
        @Parameter(name = "Seed value for random values. Ex: 1000")
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
                                        request.getLoc().getLocation(), units.get(i), simulation, seed, this.username, this.userGroups);
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
    @Operation(summary = "Returns a zip shapefile representing tornado defined by given id.")
    public Response getFile(
        @Parameter(name = "Tornado dataset guid from data service.", required = true) @PathParam("tornado-id") String tornadoId) {

        // TODO implement this and change MediaType to Octet Stream
        return Response.ok("Shapefile representing tornado not yet implemented.").build();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Search for a text in all tornadoes", description = "Gets all tornadoes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No tornadoes found with the searched text")
    })
    public List<Tornado> findTornadoes(
        @Parameter(name = "Text to search by", example = "building") @QueryParam("text") String text,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

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
    @Operation(summary = "Deletes a tornado", description = "Also deletes attached dataset and related files")
    public Tornado deleteTornado(@Parameter(name = "Tornado Id", required = true) @PathParam("tornado-id") String tornadoId) {
        Tornado tornado = getTornado(tornadoId);

        Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (this.username.equals(tornado.getOwner()) || isAdmin) {
            // delete associated datasets
            if (tornado != null && tornado instanceof TornadoModel) {
                TornadoModel tModel = (TornadoModel) tornado;
                for (TornadoHazardDataset dataset: tModel.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            } else if (tornado != null && tornado instanceof TornadoDataset) {
                TornadoDataset tDataset = (TornadoDataset) tornado;
                for (TornadoHazardDataset dataset: tDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
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
