/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.HazardConstants;
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
import edu.illinois.ncsa.incore.service.hazard.dao.IFloodRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;
import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodDataset;
import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.flood.types.FloodHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.flood.utils.FloodCalc;
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
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;

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
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.floodComparator;

@Tag(name = "floods")

@Path("floods")
@ApiResponses(value = {
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class FloodController {
    private static final Logger log = Logger.getLogger(FloodController.class);
    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    private IFloodRepository repository;

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

    @Inject
    public FloodController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all floods.")
    public List<Flood> getFloods(
        @Parameter(name = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import flood comparator
        Comparator<Flood> comparator = floodComparator(sortBy, order);

        List<Flood> floods = repository.getFloods();
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any space with name " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges(), this.groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            floods = floods.stream()
                .filter(flood -> spaceMembers.contains(flood.getId()))
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());
            return floods;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);
        List<Flood> accessibleFloods = floods.stream()
            .filter(flood -> membersSet.contains(flood.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());
        return accessibleFloods;
    }

    @GET
    @Path("/demands")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all flood allowed demand types and units.")
    public List<DemandDefinition> getFloodDemands() {
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        return DemandUtils.getAllowedDemands(demandDefinition, "flood");
    }

    @GET
    @Path("{flood-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns the flood with matching id.")
    public Flood getFloodById(
        @Parameter(name = "Flood dataset guid from data service.", required = true) @PathParam("flood-id") String floodId) {

        Flood flood = repository.getFloodById(floodId);
        if (flood == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a flood with id " + floodId);
        }

        flood.setSpaces(spaceRepository.getSpaceNamesOfMember(floodId));

        if (authorizer.canUserReadMember(this.username, floodId, spaceRepository.getAllSpaces(), this.groups)) {
            return flood;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You are not authorized to access the flood " + floodId);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new flood, the newly created flood is returned.",
        description = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create dataset-based floods only.")

    @RequestBody(description = "Flood json and files.", required = true,
        content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
            schema = @Schema(type = "object",
                properties = {@StringToClassMapItem(key = "flood", value = String.class),
                    @StringToClassMapItem(key = "file", value = String.class)}
            )
        )
    )
    public Flood createFlood(
        @Parameter(hidden = true) @FormDataParam("flood") String floodJson,
        @Parameter(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

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
        Flood flood = null;
        try {
            flood = mapper.readValue(floodJson, Flood.class);
            UserInfoUtils.throwExceptionIfIdPresent(flood.getId());

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (flood != null && flood instanceof FloodDataset) {
                FloodDataset floodDataset = (FloodDataset) flood;

                // We assume the input files in the request are in the same order listed in the flood dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty()) {
                    for (FormDataBodyPart filePart : fileParts) {
                        FloodHazardDataset hazardDataset = floodDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String datasetType = HazardConstants.DETERMINISTIC_FLOOD_HAZARD_SCHEMA;
                        String description = "Deterministic hazard raster";
//                        TODO if we have probabilistic flood in the future
//                        if (hazardDataset instanceof ProbabilisticFloodHazard) {
//                              //enable this when we get a probabilistic flood
//                            description = "Probabilistic hazard raster";
//                            datasetType = HazardConstants.PROBABILISTIC_FLOOD_HAZARD_SCHEMA;
//                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        String datasetName = demandType;
                        BodyPartEntity bodyPartEntity = (BodyPartEntity) filePart.getEntity();
                        String filename = filePart.getContentDisposition().getFileName();

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(),
                            floodDataset.getName() + " " + datasetName, this.username, this.userGroups, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }

                    flood.setCreator(this.username);
                    flood.setOwner(this.username);
                    flood = repository.addFlood(flood);

                    Space space = spaceRepository.getSpaceByName(this.username);
                    if (space == null) {
                        space = new Space(this.username);
                        space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    }
                    space.addMember(flood.getId());
                    spaceRepository.addSpace(space);
                } else {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create flood, no files were attached with your " +
                        "request.");
                }

                // add one more dataset in the usage
                AllocationUtils.increaseUsage(allocationsRepository, this.username, "hazards");

                flood.setSpaces(spaceRepository.getSpaceNamesOfMember(flood.getId()));
                return flood;
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported flood type.", e);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument has been passed in.", e);
        }

        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create flood, check the format of your request.");
    }

    @POST
    @Path("{flood-id}/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns flood values for a set of locations",
        description = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postFloodValues(
        @Parameter(name = "Glood Id", required = true)
        @PathParam("flood-id") String floodId,
        @Parameter(name = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr) {
        Flood flood = getFloodById(floodId);

        // check if demand type is correct according to the definition; for now get the first definition
        // Check units to verify requested units matches the demand type
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        JSONArray listOfDemands = demandDefinition.getJSONArray("flood");

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
                                    FloodHazardResult res = FloodCalc.getFloodHazardValue(flood, demands.get(i), units.get(i),
                                        request.getLoc(),
                                        this.username, this.userGroups);
                                    resDemands.add(res.getDemand());
                                    resUnits.add(res.getUnits());
                                    hazVals.add(res.getHazardValue());
                                }
                            }
                        } catch (UnsupportedHazardException e) {
                            log.error("Exception in calculating flood values", e);
                            hazVals.add(UNSUPPORTED_HAZARD_MODEL);
                            resUnits.add(units.get(i));
                            resDemands.add(demands.get(i));
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


    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{flood-id}")
    @Operation(summary = "Deletes a flood", description = "Also deletes attached datasets and related files")
    public Flood deleteFlood(@Parameter(name = "Flood Id", required = true) @PathParam("flood-id") String floodId) {
        Flood flood = getFloodById(floodId);

        if (this.username.equals(flood.getOwner())) {
            // delete associated datasets
            if (flood != null && flood instanceof FloodDataset) {
                FloodDataset hurrDataset = (FloodDataset) flood;
                for (FloodHazardDataset dataset : hurrDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }
//            else if(flood != null && flood instanceof FloodModel){
//                // add this when ready to migrate FloodWindfields
//            }

            Flood deletedFlood = repository.deleteFloodById(floodId); // remove flood json

            //remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(floodId)) {
                    space.removeMember(floodId);
                    spaceRepository.addSpace(space);
                }
            }

            // reduce the number of hazard from the space
            AllocationUtils.decreaseUsage(allocationsRepository, this.username, "hazards");

            return deletedFlood;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " flood " + floodId);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Search for a text in all floods", description = "Gets all floods that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No floods found with the searched text")
    })
    public List<Flood> findFloods(
        @Parameter(name = "Text to search by", example = "building") @QueryParam("text") String text,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import flood comparator
        Comparator<Flood> comparator = floodComparator(sortBy, order);

        List<Flood> floods;
        Flood flood = repository.getFloodById(text);
        if (flood != null) {
            floods = new ArrayList<Flood>() {{
                add(flood);
            }};
        } else {
            floods = this.repository.searchFloods(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);

        floods = floods.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return floods;
    }

}
