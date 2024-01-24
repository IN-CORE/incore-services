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
import edu.illinois.ncsa.incore.common.HazardConstants;
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
import edu.illinois.ncsa.incore.service.hazard.Engine;
import edu.illinois.ncsa.incore.service.hazard.Job;
import edu.illinois.ncsa.incore.service.hazard.dao.IEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesRequest;
import edu.illinois.ncsa.incore.service.hazard.models.ValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.service.hazard.models.eq.liquefaction.LiquefactionValuesResponse;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardCalc;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
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
import org.geotools.data.simple.SimpleFeatureCollection;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.coverage.grid.GridCoverage;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil.*;
import static edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil.eqComparator;


// @SwaggerDefinition is common for all the service's controllers and can be put in any one of them
@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Hazard Service For Earthquake, Tornado, Tsunami, Hurricane and Flood",
        version = "1.23.0",
        title = "IN-CORE v2 Hazard Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore.ncsa.illinois.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    )
)

@Tag(name = "earthquakes")

@Path("earthquakes")
@ApiResponses(value = {
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class EarthquakeController {
    private static final Logger logger = Logger.getLogger(EarthquakeController.class);
    private final GeometryFactory factory = new GeometryFactory();
    private final String username;
    private final List<String> groups;
    private final String userGroups;

    @Inject
    private IEarthquakeRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    private ICommonRepository commonRepository;

    @Inject
    private AttenuationProvider attenuationProvider;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    private Engine engine;

    @Inject
    public EarthquakeController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new earthquake, the newly created earthquake is returned.",
        description = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create both model earthquakes (with attenuation) and dataset-based earthquakes " +
            "with GeoTiff files uploaded.")

    @RequestBody(description = "Earthquake json and files.", required = true,
        content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
            schema = @Schema(type = "object",
                properties = {@StringToClassMapItem(key = "earthquake", value = String.class),
                              @StringToClassMapItem(key = "file", value = String.class)}
            )
    ))

    public Earthquake createEarthquake(
        @Parameter(hidden = true) @FormDataParam("earthquake") String eqJson,
        @Parameter(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts,
        @Parameter(name = "Use workflow service.", required = false) @QueryParam("useWorkflow") @DefaultValue("false") boolean useWorkflow) {
        // TODO finish adding log statements
        // First, get the Earthquake object from the form
        // TODO what should be done if a user sends multiple earthquake objects?

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
        Earthquake earthquake = null;
        try {
            earthquake = mapper.readValue(eqJson, Earthquake.class);
            UserInfoUtils.throwExceptionIfIdPresent(earthquake.getId());

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (earthquake != null && earthquake instanceof EarthquakeModel) {

                EarthquakeModel scenarioEarthquake = (EarthquakeModel) earthquake;
                try {
                    String demandType = scenarioEarthquake.getVisualizationParameters().getDemandType();
                    String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);

                    String datasetId = null;
                    if (!useWorkflow) {
                        logger.debug("don't use workflow to create earthquake");
                        File hazardFile = new File(incoreWorkDirectory, HazardConstants.HAZARD_TIF);
                        Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(scenarioEarthquake);
                        GridCoverage gc = HazardCalc.getEarthquakeHazardRaster(scenarioEarthquake, attenuations, this.username, this.userGroups);
                        HazardCalc.getEarthquakeHazardAsGeoTiff(gc, hazardFile);
                        String description = "Earthquake visualization";
                        datasetId = ServiceUtil.createRasterDataset(hazardFile, demandType + " hazard", this.username, this.userGroups,
                            description, HazardConstants.DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA);
                    }

                    DeterministicHazardDataset rasterDataset = new DeterministicHazardDataset();
                    rasterDataset.setEqParameters(scenarioEarthquake.getEqParameters());
                    rasterDataset.setDatasetId(datasetId);
                    rasterDataset.setDemandType(demandComponents[1]);
                    rasterDataset.setDemandUnits(scenarioEarthquake.getVisualizationParameters().getDemandUnits());
                    rasterDataset.setPeriod(Double.parseDouble(demandComponents[0]));

                    scenarioEarthquake.addEarthquakeHazardDataset(rasterDataset);
                    // add creator using username info
                    earthquake.setCreator(this.username);
                    earthquake.setOwner(this.username);
                    earthquake = repository.addEarthquake(earthquake);
                    addEarthquakeToSpace(earthquake, this.username);

                    if (useWorkflow) {
                        logger.debug("use workflow to create earthquake");
                        // Add job to create dataset to the queue
                        engine.addJob(new Job(this.username, "earthquake", earthquake.getId(), eqJson));
                    }
                } catch (IOException e) {
                    logger.error("Error creating raster dataset", e);
                } catch (Exception e) {
                    logger.error("Error creating grid coverage.", e);
                }
            } else if (earthquake != null && earthquake instanceof EarthquakeDataset) {
                EarthquakeDataset eqDataset = (EarthquakeDataset) earthquake;

                // We assume the input files in the request are in the same order listed in the earthquake dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty() && HazardUtil.validateEqDatasetTypes(fileParts) &&
                    (eqDataset.getHazardDatasets().size() == fileParts.size())) {
                    for (FormDataBodyPart filePart : fileParts) {
                        HazardDataset hazardDataset = eqDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String description = "Deterministic hazard raster";
                        String datasetType = HazardConstants.DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA;
                        if (hazardDataset instanceof ProbabilisticHazardDataset) {
                            datasetType = HazardConstants.PROBABILISTIC_EARTHQUAKE_HAZARD_SCHEMA;
                            description = "Probabilistic hazard raster";
                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        double period = hazardDataset.getPeriod();
                        String datasetName = demandType;

                        if (period > 0.0) {
                            datasetName = period + " " + demandType;
                        }

                        String filename = filePart.getContentDisposition().getFileName();
                        BodyPartEntity bodyPartEntity = (BodyPartEntity) filePart.getEntity();
                        InputStream fis = bodyPartEntity.getInputStream();
                        //TODO: we should check that we successfully created a raster dataset
                        String datasetId = ServiceUtil.createRasterDataset(filename, fis, eqDataset.getName() + " " + datasetName,
                            this.username, this.userGroups, description, datasetType);

                        hazardDataset.setDatasetId(datasetId);
                    }

                    // add creator using username info
                    earthquake.setCreator(this.username);
                    earthquake.setOwner(this.username);

                    // Save changes to earthquake
                    earthquake = repository.addEarthquake(earthquake);

                    addEarthquakeToSpace(earthquake, this.username);
                } else {
                    logger.error("Could not create Earthquake. Check your file extensions and the number of files in the request.");
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create Earthquake. Check your file extensions " +
                        "and the number of files in the request.");
                }
            }

            // add one more dataset in the usage
            AllocationUtils.increaseUsage(allocationsRepository, this.username, "hazards");

            earthquake.setSpaces(spaceRepository.getSpaceNamesOfMember(earthquake.getId()));
            return earthquake;

        } catch (IOException e) {
            logger.error("Error mapping the request to an Earthquake object.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Illegal Argument has been passed in.", e);
        }
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create earthquake, check the format of your request.");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all earthquakes.")
    public List<Earthquake> getEarthquakes(
        @Parameter(name = "Name of the space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results.") @QueryParam("skip") int offset,
        @Parameter(name = "Limit number of results to return.") @DefaultValue("100") @QueryParam("limit") int limit
    ) {
        // import eq comparator
        Comparator<Earthquake> comparator = eqComparator(sortBy, order);

        try {
            List<Earthquake> earthquakes = repository.getEarthquakes();

            if (!spaceName.equals("")) {
                Space space = spaceRepository.getSpaceByName(spaceName);
                if (space == null) {
                    throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find space " + spaceName);
                }
                if (!authorizer.canRead(this.username, space.getPrivileges(), this.groups)) {
                    throw new IncoreHTTPException(Response.Status.NOT_FOUND,
                        this.username + " is not authorized to read the space " + spaceName);
                }
                List<String> spaceMembers = space.getMembers();

                earthquakes = earthquakes.stream()
                    .filter(earthquake -> spaceMembers.contains(earthquake.getId()))
                    .sorted(comparator)
                    .skip(offset)
                    .limit(limit)
                    .map(d -> {
                        d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                        return d;
                    })
                    .collect(Collectors.toList());

                return earthquakes;
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);
            List<Earthquake> accessibleEarthquakes = earthquakes.stream()
                .filter(earthquake -> membersSet.contains(earthquake.getId()))
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return accessibleEarthquakes;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid User Info!");
        }
    }

    @GET
    @Path("/demands")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns all earthquake allowed demand types and units.")
    public  List<DemandDefinition> getEarthquakeDemands() {
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        return DemandUtils.getAllowedDemands(demandDefinition, "earthquake");
    }

    @GET
    @Path("{earthquake-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns the earthquake with matching id.")
    public Earthquake getEarthquake(
        @Parameter(name = "Id of the earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId) {

        Earthquake earthquake = repository.getEarthquakeById(earthquakeId);
        if (earthquake == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find an earthquake with id " + earthquakeId);
        }

        earthquake.setSpaces(spaceRepository.getSpaceNamesOfMember(earthquakeId));

        //feeling lucky
        Space space = spaceRepository.getSpaceByName(this.username);
        if (space != null && space.hasMember(earthquakeId)) {
            return earthquake;
        }

        if (authorizer.canUserReadMember(this.username, earthquakeId, spaceRepository.getAllSpaces(), this.groups)) {
            return earthquake;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN,
            "You don't have permission to access the earthquake with id" + earthquakeId);
    }

    @GET
    @Path("{earthquake-id}/raster")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns SeismicHazardResults for a given attenuation model-based earthquake id, demand type and unit, " +
        "coordinates and grid spacing.", description = " SeismicHazardResults contains the metadata about the raster " +
        "data along with a list of HazardResults. Each HazardResult is a lat, long and hazard value.")
    public SeismicHazardResults getEarthquakeHazardForBox(
        @Parameter(name = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc", required = true) @QueryParam("demandType") String demandType,
        @Parameter(name = "Ground motion demand unit. Ex: g, %g, cm/s, etc", required = true) @QueryParam("demandUnits") String demandUnits,
        @Parameter(name = "Bounding box of a raster. Min X.", required = true) @QueryParam("minX") double minX,
        @Parameter(name = "Bounding box of a raster. Min Y.", required = true) @QueryParam("minY") double minY,
        @Parameter(name = "Bounding box of a raster. Max X.", required = true) @QueryParam("maxX") double maxX,
        @Parameter(name = "Bounding box of a raster. max Y.", required = true) @QueryParam("maxY") double maxY,
        @Parameter(name = "Grid spacing.", required = true) @QueryParam("gridSpacing") double gridSpacing,
        @Parameter(name = "Amplify hazard.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard) {

        Earthquake eq = getEarthquake(earthquakeId);
        SeismicHazardResults results = null;
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            String period = demandType;
            String demand = demandType;

            if (Pattern.compile(Pattern.quote(HazardUtil.SA), Pattern.CASE_INSENSITIVE).matcher(demandType).find()) {
                String[] demandSplit = demandType.split(" ");
                period = demandSplit[0];
                demand = demandSplit[1];
            }

            Map<BaseAttenuation, Double> attenuations;
            try {
                attenuations = attenuationProvider.getAttenuations(earthquake);
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
            }


            int width = 0;
            int height = 0;

            double dx = (maxX - minX);
            double dy = (maxY - minY);

            dx = Math.ceil(dx / gridSpacing) * gridSpacing;
            dy = Math.ceil(dy / gridSpacing) * gridSpacing;

            maxX = minX + dx;
            maxY = minY + dy;

            if (gridSpacing != 0) {
                long widthLong = Math.round(Math.abs((maxX - minX) / gridSpacing)); // + 1;
                long heightLong = Math.round(Math.abs((maxY - minY) / gridSpacing)); // + 1;
                if ((widthLong > Integer.MAX_VALUE) || (heightLong > Integer.MAX_VALUE)) {
                    logger.warn("Overflow....too many points to fit in an int");
                }
                // adjustMaxMin();
                width = (int) widthLong;
                height = (int) heightLong;

            }

            float cellsize = (float) gridSpacing;
            float startX = (float) minX + ((float) gridSpacing / 2.0f);
            float startY = (float) maxY - ((float) gridSpacing / 2.0f);
            Site localSite = null;
            SeismicHazardResult hazardValue = null;
            List<HazardResult> hazardResults = new LinkedList<>();
            for (int y = 0; y < height; y++) {

                startX = (float) minX + (cellsize / 2.0f);
                for (int x = 0; x < width; x++) {
                    try {
                        localSite = new Site(factory.createPoint(new Coordinate(startX, startY)));
                        hazardValue = HazardCalc.getGroundMotionAtSite(earthquake, attenuations, localSite, period,
                            demand, demandUnits, 0, amplifyHazard, null, this.username, this.userGroups);
                        hazardResults.add(new HazardResult(startY, startX, hazardValue.getHazardValue()));
                    } catch (Exception e) {
                        logger.error("Error computing hazard value.", e);
                    }

                    startX += (float) gridSpacing;
                }
                startY -= gridSpacing;
            }

            results = new SeismicHazardResults(hazardValue.getPeriod(), hazardValue.getDemand(), hazardResults);
            return results;
        }
        return null;
    }

    @POST
    @Path("{earthquake-id}/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns earthquake values for a set of locations",
        description = "Outputs hazard values, demand types, unit and location.")
    public List<ValuesResponse> postEarthquakeValues(
        @Parameter(name = "Earthquake Id", required = true)
        @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Json of the points along with demand types and units",
            required = true) @FormDataParam("points") String requestJsonStr,
        @Parameter(name = "Site class dataset from data service.", required = false)
        @FormDataParam("siteClassId") @DefaultValue("") String siteClassId,
        @Parameter(name = "Amplify earthquake by soil type", required = false)
        @FormDataParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard) {

        Earthquake eq = getEarthquake(earthquakeId);

        // check if demand type is correct according to the definition; for now get the first definition
        // Check units to verify requested units matches the demand type
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        JSONArray listOfDemands = demandDefinition.getJSONArray("earthquake");

        Map<BaseAttenuation, Double> attenuations = null;
        if (eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            try {
                attenuations = attenuationProvider.getAttenuations(earthquake);
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "Attenuation model not found");
            }
        }

        SimpleFeatureCollection siteClassFC = null;
        if (!siteClassId.isEmpty()) {
            siteClassFC = (SimpleFeatureCollection) GISUtil.getFeatureCollection(siteClassId, this.username, this.userGroups);
        }

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

                if (!CommonUtil.validateHazardValuesInputs(demands, units, request.getLoc())) {
                    for (int i = 0; i < demands.size(); i++) {
                        hazVals.add(MISSING_REQUIRED_INPUTS);
                    }
                    resUnits = units;
                    resDemands = demands;
                } else {
                    for (int i = 0; i < demands.size(); i++) {
                        try {
                            String[] demandComponents = HazardUtil.getHazardDemandComponents(demands.get(i));

                            if (demandComponents == null || !HazardUtil.verifyHazardDemandType(demandComponents[1], listOfDemands)) {
                                hazVals.add(INVALID_DEMAND);
                                resUnits.add(units.get(i));
                                resDemands.add(demands.get(i));
                            } else {
                                if (!HazardUtil.verifyHazardDemandUnit(demandComponents[1], units.get(i), listOfDemands)) {
                                    hazVals.add(INVALID_UNIT);
                                    resUnits.add(units.get(i));
                                    resDemands.add(demands.get(i));
                                } else {
                                    SeismicHazardResult res;
                                    try {
                                        res = HazardCalc.getGroundMotionAtSite(eq, attenuations,
                                            new Site(request.getLoc().getLocation()), demandComponents[0], demandComponents[1],
                                            units.get(i), 0, amplifyHazard, siteClassFC, this.username, this.userGroups);
                                        //condition to only show PGA/PGV without period prepended
                                        String period = Float.parseFloat(res.getPeriod().trim()) == 0.0 ? "" : res.getPeriod().trim() + " ";
                                        resDemands.add(period + res.getDemand());
                                        resUnits.add(res.getUnits());
                                        hazVals.add(res.getHazardValue());
                                    } catch (UnsupportedHazardException ex) {
                                        hazVals.add(UNSUPPORTED_HAZARD_MODEL);
                                        resUnits.add(units.get(i));
                                        resDemands.add(demands.get(i));
                                    }
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
            return valResponse;
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
    }

    @GET
    @Path("{earthquake-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns hazard values for the given earthquake id.",
        description = "The results contain ground shaking parameter (PGA, SA, etc) for specific locations.")
    @Deprecated
    public List<SeismicHazardResult> getEarthquakeHazardValues(
        @Parameter(name = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc.", required = true) @QueryParam("demandType") String demandType,
        @Parameter(name = "Ground motion demand unit. Ex: g, %g, cm/s, etc.", required = true) @QueryParam("demandUnits") String demandUnits,
        @Parameter(name = "Amplify hazard by soil type.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard,
        @Parameter(name = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Earthquake eq = getEarthquake(earthquakeId);

        // check if demand type is correct according to the definition; for now get the first definition
        // Check units to verify requested units matches the demand type
        JSONObject demandDefinition = new JSONObject(commonRepository.getAllDemandDefinitions().get(0).toJson());
        JSONArray listOfDemands = demandDefinition.getJSONArray("earthquake");

        String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);
        if (demandComponents == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not parse demand type " + demandType + ", please check the " +
                "format. It should be in a form similar to 0.2 SA or 0.2 Sec SA.");
        }

        // Check units to verify requested units matches the demand type
        if (!HazardUtil.verifyHazardDemandUnit(demandComponents[1], demandUnits, listOfDemands)) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The requested demand units, " + demandUnits + " is not supported " +
                "for " + demandType + ", please check requested units.");
        }

        List<SeismicHazardResult> hazardResults = new LinkedList<SeismicHazardResult>();
        if (eq != null) {
            Map<BaseAttenuation, Double> attenuations = null;
            if (eq instanceof EarthquakeModel) {
                EarthquakeModel earthquake = (EarthquakeModel) eq;
                try {
                    attenuations = attenuationProvider.getAttenuations(earthquake);
                } catch (UnsupportedHazardException ex) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
                }
            }
            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());

                try {
                    hazardResults.add(HazardCalc.getGroundMotionAtSite(eq, attenuations, localSite, demandComponents[0],
                        demandComponents[1], demandUnits, 0, amplifyHazard, null, this.username, this.userGroups));
                } catch (Exception e) {
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error computing hazard.");
                }
            }

            return hazardResults;
        } else {
            logger.error("Could not find  earthquake with id " + earthquakeId);
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find earthquake with id " + earthquakeId);
        }

    }


    @GET
    @Path("{earthquake-id}/aleatoryuncertainty")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns aleatory uncertainties for a model based earthquake")
    public Map<String, Double> getEarthquakeAleatoricUncertainties(
        @Parameter(name = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Demand Type. Ex: PGA.", required = true) @QueryParam("demandType") String demandType) {
        Earthquake eq = getEarthquake(earthquakeId);
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            try {
                Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(earthquake);
                Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();
                Map<String, Double> cumulativeAleatoryUncertainties = new HashMap<>();

                while (iterator.hasNext()) {
                    BaseAttenuation model = iterator.next();
                    Double weight = attenuations.get(model);
                    Map<String, Double> aleatoryUncertainties = model.getAleatoricUncertainties();

                    if (aleatoryUncertainties != null) {
                        //This logic adopted from the paper, assumes all models will have
                        // the same set of demand types defined for their respective aleatory uncertainties
                        aleatoryUncertainties.forEach((key, value) -> {
                            if (cumulativeAleatoryUncertainties.containsKey(key)) {
                                Double curVal = cumulativeAleatoryUncertainties.get(key);
                                cumulativeAleatoryUncertainties.put(key, curVal + (weight * Math.pow(value, 2)));
                            } else {
                                cumulativeAleatoryUncertainties.put(key, (weight * Math.pow(value, 2)));
                            }
                        });
                    }
                }

                for (Map.Entry<String, Double> element : cumulativeAleatoryUncertainties.entrySet()) {
                    cumulativeAleatoryUncertainties.put(element.getKey(), Math.sqrt(element.getValue()));
                }

                if (cumulativeAleatoryUncertainties.size() > 0) {
                    if (demandType != null && demandType.trim() != "") {
                        if (cumulativeAleatoryUncertainties.containsKey(demandType.trim().toUpperCase())) {
                            return new HashMap<String, Double>() {
                                {
                                    put(demandType, cumulativeAleatoryUncertainties.get(demandType.trim().toUpperCase()));
                                }
                            };
                        }
                    }
                }
                return cumulativeAleatoryUncertainties;
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
            }

        } else {
            logger.error("Earthquake with id " + earthquakeId + " is not attenuation model based");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Earthquake with id " + earthquakeId + " is not attenuation model " +
                "based");
        }
    }

    @GET
    @Path("{earthquake-id}/variance/{variance-type}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns total and epistemic variance for a model based earthquake")
    public List<VarianceResult> getEarthquakeVariance(
        @Parameter(name = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Type of Variance. epistemic or total", required = true) @PathParam("variance-type") String varianceType,
        @Parameter(name = "Demand Type. Ex: PGA.", required = true) @QueryParam("demandType") String demandType,
        @Parameter(name = "Demand unit. Ex: g.", required = true) @QueryParam("demandUnits") String demandUnits,
        @Parameter(name = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Earthquake eq = getEarthquake(earthquakeId);
        List<VarianceResult> varianceResults = new ArrayList<>();
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations;
            try {
                attenuations = attenuationProvider.getAttenuations(earthquake);
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
            }
            List<SeismicHazardResult> seismicHazardResults = getEarthquakeHazardValues(earthquakeId, demandType, demandUnits,
                false, points);
            Map<String, Double> aleatoricUncertainties = getEarthquakeAleatoricUncertainties(earthquakeId, demandType);

            // single attenuation model with weight 1
            boolean hasSingleModel = (attenuations.size() == 1);
            BaseAttenuation singleModel = null;

            int i = 0;
            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());
                Double hazardVal = seismicHazardResults.get(i).getHazardValue();
                Double cumulativeEpistemicVariance = 0.0;
                Double cumulativeTotalVariance = 0.0;

                for (Map.Entry<BaseAttenuation, Double> element : attenuations.entrySet()) {
                    BaseAttenuation model = element.getKey();
                    Double weight = element.getValue();
                    if (i == 0) {
                        singleModel = model;
                    }
                    try {
                        Double epistemicVariance = model.getEpistemicVariance(hazardVal, demandType, localSite);
                        cumulativeEpistemicVariance += weight * Math.pow(epistemicVariance, 2);
                    } catch (Exception e) {
                        logger.error("Error fetching epistemic variance for earthquake id " + earthquakeId, e);
                    }
                }

                cumulativeEpistemicVariance = Math.sqrt(cumulativeEpistemicVariance);

                if (varianceType.equalsIgnoreCase("total")) {
                    if (hasSingleModel) {
                        // This is for ChiouYoungs2014 that does not have aleatory uncertainty set and uses a custom stddev method
                        // NOTE that ChiouYoungs2014 will not work together with other models to share weights.
                        try {
                            cumulativeTotalVariance = singleModel.getStandardDeviation(hazardVal, demandType, localSite);
                        } catch (Exception e) {
                            logger.error("Error fetching standard deviation for earthquake id " + earthquakeId, e);
                        }
                    } else {
                        if (aleatoricUncertainties != null && aleatoricUncertainties.containsKey(demandType.trim().toUpperCase())) {
                            Double aleatoricUncertainty = aleatoricUncertainties.get(demandType.trim().toUpperCase());
                            cumulativeTotalVariance = Math.sqrt(Math.pow(aleatoricUncertainty, 2) + Math.pow(cumulativeEpistemicVariance,
                                2));

                        } else {
                            logger.error("Earthquake with id " + earthquakeId + " does not have valid attenuation uncertainties set");
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Earthquake with id " + earthquakeId +
                                " does not have valid attenuation uncertainties set");
                        }
                    }
                    varianceResults.add(new VarianceResult(localSite.getLocation().getY(), localSite.getLocation().getX(),
                        demandType, demandUnits, cumulativeTotalVariance));
                } else if (varianceType.equalsIgnoreCase("epistemic")) {
                    varianceResults.add(new VarianceResult(localSite.getLocation().getY(), localSite.getLocation().getX(),
                        demandType, demandUnits, cumulativeEpistemicVariance));

                } else {
                    logger.error("Input variance type " + varianceType + " is not implemented");
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Input variance type " + varianceType + " is not" +
                        " implemented");
                }
            }

        } else {
            logger.error("Earthquake with id " + earthquakeId + " is not attenuation model based");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Earthquake with id " + earthquakeId + " is not " +
                "attenuation model based");
        }

        return varianceResults;
    }

    @POST
    @Path("{earthquake-id}/liquefaction/values")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns liquefaction (PGD) values, probability of liquefaction, and probability of ground failure",
        description = "This needs a valid susceptibility dataset as a shapefile for a set of earthquake locations.")
    public List<LiquefactionValuesResponse> postEarthquakeLiquefactionValues(
        @Parameter(name = "Earthquake Id", required = true)
        @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Json of the points along with demand types(pgd) and units",
            required = true) @FormDataParam("points") String requestJsonStr,
        @Parameter(name = "Geology dataset from data service.", required = true)
        @FormDataParam("geologyDataset") String geologyId, @Parameter(name = "Site class dataset from data service.", required = false)
        @FormDataParam("siteClassId") @DefaultValue("") String siteClassId) {
        Earthquake eq = getEarthquake(earthquakeId);

        if (!(eq instanceof EarthquakeModel)) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Liquefaction is only supported for model-based earthquakes. " +
                "Please verify if the earthquake is attenuation model based");
        }

        if (geologyId == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Missing geology dataset in the request");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations;
            try {
                attenuations = attenuationProvider.getAttenuations(earthquake);
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
            }

            SimpleFeatureCollection siteClassFC = null;
            if (!siteClassId.isEmpty()) {
                siteClassFC = (SimpleFeatureCollection) GISUtil.getFeatureCollection(siteClassId, this.username, this.userGroups);
            }

            SimpleFeatureCollection soilGeology = (SimpleFeatureCollection) GISUtil.getFeatureCollection(geologyId,
                this.username, this.userGroups);

            List<LiquefactionValuesResponse> valResponse = new ArrayList<>();
            List<ValuesRequest> valuesRequest = mapper.readValue(requestJsonStr, new TypeReference<List<ValuesRequest>>() {
            });
            for (ValuesRequest request : valuesRequest) {
                List<String> demands = request.getDemands();
                List<String> units = request.getUnits();
                List<Double> pgdVals = new ArrayList<>();
                Double liqProb = 0.0;
                double[] groundFailureProb = {};
                List<String> resDemands = new ArrayList<>();
                List<String> resUnits = new ArrayList<>();

                CommonUtil.validateHazardValuesInput(demands, units, request.getLoc());

                for (int i = 0; i < demands.size(); i++) {
                    Site localSite = new Site(request.getLoc().getLocation());
                    // TODO find groundwater depth if shapefile is passed in
                    LiquefactionHazardResult res = HazardCalc.getLiquefactionAtSite(earthquake, attenuations, localSite,
                        soilGeology, units.get(i), siteClassFC, this.username, this.userGroups);
                    resDemands.add(PGD);
                    resUnits.add(res.getPgdUnits());
                    pgdVals.add(res.getPgd());
                    liqProb = res.getLiqProbability();
                    groundFailureProb = res.getGroundFailureProb();
                }

                LiquefactionValuesResponse response = new LiquefactionValuesResponse();
                response.setPgdValues(pgdVals);
                response.setLiqProbability(liqProb);
                response.setGroundFailureProb(groundFailureProb);
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
    @Path("{earthquake-id}/liquefaction/values")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns liquefaction (PGD) values, probability of liquefaction, and probability of ground failure.",
        description = "This needs a valid susceptibility dataset as a shapefile for the earthquake location.")
    @Deprecated
    public List<LiquefactionHazardResult> getEarthquakeLiquefaction(
        @Parameter(name = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @Parameter(name = "Geology dataset from data service.", required = true) @QueryParam("geologyDataset") String geologyId,
        @Parameter(name = "Liquefaction demand unit. Ex: in, cm, etc", required = true) @QueryParam("demandUnits") String demandUnits,
        @Parameter(name = "List of points provided as lat,long. Ex: '28.01,-83.85'", required = true) @QueryParam("point") List<IncorePoint> points) {
        Earthquake eq = getEarthquake(earthquakeId);
        // TODO add logging/error for earthquake dataset that it can't be used
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations;
            try {
                attenuations = attenuationProvider.getAttenuations(earthquake);
            } catch (UnsupportedHazardException ex) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Attenuation model not found");
            }

            List<LiquefactionHazardResult> hazardResults = new LinkedList<LiquefactionHazardResult>();
            SimpleFeatureCollection soilGeology = (SimpleFeatureCollection) GISUtil.getFeatureCollection(geologyId,
                this.username, this.userGroups);

            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());
                // TODO find groundwater depth if shapefile is passed in
                hazardResults.add(HazardCalc.getLiquefactionAtSite(earthquake, attenuations, localSite, soilGeology,
                    demandUnits, null, this.username, this.userGroups));
            }

            return hazardResults;
        } else {
            logger.error("Could not find earthquake with id " + earthquakeId);
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find earthquake with id " + earthquakeId);
        }
    }

    // TODO this is incomplete API, we need to determine if it's needed as a separate endpoint
    @GET
    @Path("/soil/amplification")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Returns earthquake site hazard amplification.", description = " This returns the amplified " +
        "hazard given a methodology (e.g. NEHRP), soil map dataset id (optional), latitude, longitude, ground shaking " +
        "parameter (PGA, Sa, etc), hazard value, and default site class to use.")
    public Response getEarthquakeSiteAmplification(
        @Parameter(name = "Method to get hazard amplification.", required = true) @QueryParam("method") String method,
        @Parameter(name = "ID of site class dataset from data service.", required = true) @QueryParam("datasetId") @DefaultValue("") String datasetId,
        @Parameter(name = "Latitude coordinate of the site.", required = true) @QueryParam("siteLat") double siteLat,
        @Parameter(name = "Longitude coordinate of the site.", required = true) @QueryParam("siteLong") double siteLong,
        @Parameter(name = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc.", required = true) @QueryParam("demandType") String demandType,
        @Parameter(name = "Hazard value.", required = true) @QueryParam("hazard") double hazard,
        @Parameter(name = "Default site classification. Expected  A, B, C, D, E or F.") @QueryParam("defaultSiteClass") String defaultSiteClass) {

        int localSiteClass = HazardUtil.getSiteClassAsInt(defaultSiteClass);
        if (localSiteClass == -1) {
            return Response.status(500).entity("Unknown default site classification, expected A, B, C, D, E or F").build();
        }

        if (!datasetId.isEmpty()) {
            // TODO implement this
        }

        String period = demandType;

        if (demandType.contains(HazardUtil.SA)) {
            String[] demandSplit = demandType.split(" ");
            period = demandSplit[0];
        }

        SiteAmplification siteAmplification = null;
        // Local site to get hazard for
        Site localSite = new Site(factory.createPoint(new Coordinate(siteLong, siteLat)));

        if (method.equalsIgnoreCase("NEHRP")) {
            siteAmplification = new NEHRPSiteAmplification();
            // Note, hazard value input should be PGA if amplifying PGV hazard because NEHRP uses PGA coefficients for amplifying PGV
            // and the range for interpretation is in units of g
            double amplification = siteAmplification.getSiteAmplification(localSite, hazard, localSiteClass, period);

            return Response.ok(amplification).build();

        }
        return Response.ok("Site amplification requested is not yet implemented").build();
    }

    @GET
    @Path("/slope/amplification")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(hidden = true, summary = "Returns earthquake slope amplification.")
    public Response getEarthquakeSlopeAmplification(
        @Parameter(hidden = true, name = "Latitude coordinate of the site.") @QueryParam("siteLat") double siteLat,
        @Parameter(hidden = true, name = "Longitude coordinate of the site.") @QueryParam("siteLong") double siteLong) {

        return Response.ok("Topographic amplification not yet implemented").build();
    }

    @GET
    @Path("models")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(hidden = true, summary = "Returns available attenuation models.", description = "This returns the available " +
        "attenuation models.")
    public Set<String> getSupportedEarthquakeModels() {
        return attenuationProvider.getAttenuations().keySet();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Search for a text in all earthquakes", description = "Gets all earthquakes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "No earthquakes found with the searched text")
    })
    public List<Earthquake> findEarthquakes(
        @Parameter(name = "Text to search by", example = "building") @QueryParam("text") String text,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit number of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        // import eq comparator
        Comparator<Earthquake> comparator = eqComparator(sortBy, order);

        List<Earthquake> earthquakes;
        Earthquake earthquake = repository.getEarthquakeById(text);
        if (earthquake != null) {
            earthquakes = new ArrayList<Earthquake>() {{
                add(earthquake);
            }};
        } else {
            earthquakes = this.repository.searchEarthquakes(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);
        earthquakes = earthquakes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return earthquakes;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{earthquake-id}")
    @Operation(summary = "Deletes an earthquake", description = "Also deletes attached dataset and related files")
    public Earthquake deleteEarthquake(@Parameter(name = "Earthquake Id", required = true) @PathParam("earthquake-id") String earthquakeId) {
        Earthquake eq = getEarthquake(earthquakeId);

        Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (this.username.equals(eq.getOwner()) || isAdmin) {
            // delete associated datasets
            if (eq != null && eq instanceof EarthquakeModel) {
                EarthquakeModel scenarioEarthquake = (EarthquakeModel) eq;
                for (HazardDataset dataset : scenarioEarthquake.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            } else if (eq != null && eq instanceof EarthquakeDataset) {
                EarthquakeDataset eqDataset = (EarthquakeDataset) eq;
                for (HazardDataset dataset : eqDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username, this.userGroups) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }

            Earthquake deletedEq = repository.deleteEarthquakeById(earthquakeId); // remove earthquake json

            //remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(earthquakeId)) {
                    space.removeMember(earthquakeId);
                    spaceRepository.addSpace(space);
                }
            }

            // reduce the number of hazard from the space
            AllocationUtils.decreaseUsage(allocationsRepository, this.username, "hazards");

            return deletedEq;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " earthquake " + earthquakeId);
        }
    }

    // Helper functions

    //For adding earthquake id to user's space
    private void addEarthquakeToSpace(Earthquake earthquake, String username) {
        Space space = spaceRepository.getSpaceByName(username);
        if (space == null) {
            space = new Space(username);
            space.setPrivileges(Privileges.newWithSingleOwner(username));
        }
        space.addMember(earthquake.getId());
        spaceRepository.addSpace(space);
    }

}
