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
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.types.HurricaneHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneCalc;
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
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.hurricaneComparator;

@Tag(name = "hurricanes")

@Path("hurricanes")
@ApiResponses(value = {
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class HurricaneController {
    private static final Logger log = Logger.getLogger(HurricaneController.class);
    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    private IHurricaneRepository repository;

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
    public HurricaneController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all hurricanes.")
    public List<Hurricane> getHurricanes(
        @Parameter(name = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import hurricane comparator
        Comparator<Hurricane> comparator = hurricaneComparator(sortBy, order);

        List<Hurricane> hurricanes = repository.getHurricanes();
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
            hurricanes = hurricanes.stream()
                .filter(hurricane -> spaceMembers.contains(hurricane.getId()))
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return hurricanes;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);
        List<Hurricane> accessibleHurricanes = hurricanes.stream()
            .filter(hurricane -> membersSet.contains(hurricane.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleHurricanes;
    }

    @GET
    @Path("/demands")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all hurricane allowed demand types and units.")
    public List<DemandDefinition> getHurricaneDemands() {
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        return DemandUtils.getAllowedDemands(demandDefinition, "hurricane");
    }

    @GET
    @Path("{hurricane-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns the hurricane with matching id.")
    public Hurricane getHurricaneById(
        @Parameter(name = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricane-id") String hurricaneId) {

        Hurricane hurricane = repository.getHurricaneById(hurricaneId);
        if (hurricane == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a hurricane with id " + hurricaneId);
        }

        hurricane.setSpaces(spaceRepository.getSpaceNamesOfMember(hurricaneId));

        if (authorizer.canUserReadMember(this.username, hurricaneId, spaceRepository.getAllSpaces(), this.groups)) {
            return hurricane;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You are not authorized to access the hurricane " + hurricaneId);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new hurricane, the newly created hurricane is returned.",
        description = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create dataset-based hurricanes only.")

    @RequestBody(description = "Hurricane json and files.", required = true,
        content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
            schema = @Schema(type = "object",
                properties = {@StringToClassMapItem(key = "hurricane", value = String.class),
                    @StringToClassMapItem(key = "file", value = String.class)}
            )
        )
    )
    public Hurricane createHurricane(
        @Parameter(hidden = true) @FormDataParam("hurricane") String hurricaneJson,
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
        Hurricane hurricane = null;
        try {
            hurricane = mapper.readValue(hurricaneJson, Hurricane.class);
            UserInfoUtils.throwExceptionIfIdPresent(hurricane.getId());

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (hurricane != null && hurricane instanceof HurricaneDataset) {
                HurricaneDataset hurricaneDataset = (HurricaneDataset) hurricane;

                // We assume the input files in the request are in the same order listed in the hurricane dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty()) {
                    for (FormDataBodyPart filePart : fileParts) {
                        HurricaneHazardDataset hazardDataset = hurricaneDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String datasetType = HazardConstants.DETERMINISTIC_HURRICANE_HAZARD_SCHEMA;
                        String description = "Deterministic hazard raster";
//                        if (hazardDataset instanceof ProbabilisticHurricaneHazard) {
//                              //enable this when we get a probabilistic hurricane
//                            description = "Probabilistic hazard raster";
//                            datasetType = HazardConstants.PROBABILISTIC_HURRICANE_HAZARD_SCHEMA;
//                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        String datasetName = demandType;
                        BodyPartEntity bodyPartEntity = (BodyPartEntity) filePart.getEntity();
                        String filename = filePart.getContentDisposition().getFileName();

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(),
                            hurricaneDataset.getName() + " " + datasetName, this.username, this.userGroups, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }

                    hurricane.setCreator(this.username);
                    hurricane.setOwner(this.username);
                    hurricane = repository.addHurricane(hurricane);

                    Space space = spaceRepository.getSpaceByName(this.username);
                    if (space == null) {
                        space = new Space(this.username);
                        space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    }
                    space.addMember(hurricane.getId());
                    spaceRepository.addSpace(space);

                    hurricane.setSpaces(spaceRepository.getSpaceNamesOfMember(hurricane.getId()));

                    // add one more dataset in the usage
                    AllocationUtils.increaseUsage(allocationsRepository, this.username, "hazards");

                    return hurricane;
                } else {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create hurricane, no files were attached with " +
                        "your request.");
                }
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported hurricane type.", e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Could not map the request to a supported hurricane type" +
                ". " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument has been passed in.", e);
        }
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create hurricane, check the format of your request.");
    }

    @POST
    @Path("{hurricane-id}/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns hurricane values for a set of locations",
        description = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postHurricaneValues(
        @Parameter(name = "Hurricane Id", required = true)
        @PathParam("hurricane-id") String hurricaneId,
        @Parameter(name = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr) {

        Hurricane hurricane = getHurricaneById(hurricaneId);

        // check if demand type is correct according to the definition; for now get the first definition
        // Check units to verify requested units matches the demand type
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        JSONArray listOfDemands = demandDefinition.getJSONArray("hurricane");

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
                                    HurricaneHazardResult res = HurricaneCalc.getHurricaneHazardValue(hurricane, demands.get(i),
                                        units.get(i), request.getLoc(), this.username, this.userGroups);
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
    @Path("{hurricane-id}")
    @Operation(summary = "Deletes a Hurricane", description = "Also deletes attached datasets and related files")
    public Hurricane deleteHurricanes(@Parameter(name = "Hurricane Id", required = true) @PathParam("hurricane-id") String hurricaneId) {
        Hurricane hurricane = getHurricaneById(hurricaneId);

        Boolean isAdmin = UserGroupUtils.isAdmin(this.groups);
        if (this.username.equals(hurricane.getOwner()) || isAdmin) {
            // delete associated datasets
            if (hurricane != null && hurricane instanceof HurricaneDataset) {
                HurricaneDataset hurrDataset = (HurricaneDataset) hurricane;
                for (HurricaneHazardDataset dataset : hurrDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }
//            else if(hurricane != null && hurricane instanceof HurricaneModel){
//                // add this when ready to migrate HurricaneWindfields
//            }

            Hurricane deletedHurr = repository.deleteHurricaneById(hurricaneId); // remove hurricane json

            //remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(hurricaneId)) {
                    space.removeMember(hurricaneId);
                    spaceRepository.addSpace(space);
                }
            }

            // reduce the number of hazard from the space
            AllocationUtils.decreaseUsage(allocationsRepository, this.username, "hazards");

            return deletedHurr;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " hurricane " + hurricaneId);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Search for a text in all hurricanes", description = "Gets all hurricanes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No hurricanes found with the searched text")
    })
    public List<Hurricane> findHurricanes(
        @Parameter(name = "Text to search by", example = "building") @QueryParam("text") String text,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import hurricane comparator
        Comparator<Hurricane> comparator = hurricaneComparator(sortBy, order);

        List<Hurricane> hurricanes;
        Hurricane hurricane = repository.getHurricaneById(text);
        if (hurricane != null) {
            hurricanes = new ArrayList<Hurricane>() {{
                add(hurricane);
            }};
        } else {
            hurricanes = this.repository.searchHurricanes(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);

        hurricanes = hurricanes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .sorted(comparator)
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
