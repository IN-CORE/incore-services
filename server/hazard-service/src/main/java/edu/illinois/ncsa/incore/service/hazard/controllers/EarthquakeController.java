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
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.hazard.Engine;
import edu.illinois.ncsa.incore.service.hazard.HazardConstants;
import edu.illinois.ncsa.incore.service.hazard.Job;
import edu.illinois.ncsa.incore.service.hazard.dao.IEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardCalc;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.opengis.coverage.grid.GridCoverage;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


// @SwaggerDefinition is common for all the service's controllers and can be put in any one of them
@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Hazard Service For Earthquake, Tornado, Tsunami and Hurricane",
        version = "v0.6.3",
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
    ),
    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
//    ,tags = {
//        @Tag(name = "Private", description = "Tag used to denote operations as private")
//    },
    //externalDocs = @ExternalDocs(value = "FEMA  Hazard Manual", url = "https://www.fema.gov/earthquake")
)

@Api(value = "earthquakes", authorizations = {})

@Path("earthquakes")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class EarthquakeController {
    private static final Logger logger = Logger.getLogger(EarthquakeController.class);
    private GeometryFactory factory = new GeometryFactory();
    private String username;

    @Inject
    private IEarthquakeRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private AttenuationProvider attenuationProvider;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    private Engine engine;

    @Inject
    public EarthquakeController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new earthquake, the newly created earthquake is returned.",
        notes = "Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create both model earthquakes (with attenuation) and dataset-based earthquakes " +
            "with GeoTiff files uploaded.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "earthquake", value = "Earthquake json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Earthquake files.", required = true, dataType = "string", paramType = "form")
    })
    public Earthquake createEarthquake(
        @ApiParam(hidden = true) @FormDataParam("earthquake") String eqJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts,
        @ApiParam(value = "Use workflow service.", required = false) @QueryParam("useWorkflow") @DefaultValue("false") boolean useWorkflow)
    {
        // TODO finish adding log statements
        // First, get the Earthquake object from the form
        // TODO what should be done if a user sends multiple earthquake objects?
        ObjectMapper mapper = new ObjectMapper();
        Earthquake earthquake = null;
        try {
            earthquake = mapper.readValue(eqJson, Earthquake.class);

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (earthquake != null && earthquake instanceof EarthquakeModel) {

                EarthquakeModel scenarioEarthquake = (EarthquakeModel) earthquake;

                Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(scenarioEarthquake);
                try {
                    String demandType = scenarioEarthquake.getVisualizationParameters().getDemandType();
                    String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);

                    String datasetId = null;
                    if(!useWorkflow) {
                        System.out.println("don't use workflow");
                        File hazardFile = new File(incoreWorkDirectory, HazardConstants.HAZARD_TIF);
                        GridCoverage gc = HazardCalc.getEarthquakeHazardRaster(scenarioEarthquake, attenuations, this.username);
                        HazardCalc.getEarthquakeHazardAsGeoTiff(gc, hazardFile);
                        String description = "Earthquake visualization";
                        datasetId = ServiceUtil.createRasterDataset(hazardFile, demandType + " hazard", this.username,
                            description, HazardConstants.DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA);
                    }


                    DeterministicHazardDataset rasterDataset = new DeterministicHazardDataset();
                    rasterDataset.setEqParameters(scenarioEarthquake.getEqParameters());
                    rasterDataset.setDatasetId(datasetId);
                    rasterDataset.setDemandType(demandComponents[1]);
                    rasterDataset.setDemandUnits(scenarioEarthquake.getVisualizationParameters().getDemandUnits());
                    rasterDataset.setPeriod(Double.parseDouble(demandComponents[0]));

                    scenarioEarthquake.setHazardDataset(rasterDataset);
                    // add creator using username info
                    earthquake.setCreator(this.username);
                    earthquake = repository.addEarthquake(earthquake);
                    addEarthquakeToSpace(earthquake, this.username);

                    if(useWorkflow) {
                        System.out.println("use workflow");
                        // Add job to create dataset to the queue
                        engine.addJob(new Job(this.username, "earthquake", earthquake.getId(), eqJson));
                    }
                    return earthquake;
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
                            this.username, description, datasetType);

                        hazardDataset.setDatasetId(datasetId);
                    }

                    // add creator using username info
                    earthquake.setCreator(this.username);

                    // Save changes to earthquake
                    earthquake = repository.addEarthquake(earthquake);

                    addEarthquakeToSpace(earthquake, this.username);

                    return earthquake;
                } else {
                    logger.error("Could not create Earthquake. Check your file extensions and the number of files in the request.");
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create Earthquake. Check your file extensions and the number of files in the request.");
                }
            }

        } catch (IOException e) {
            logger.error("Error mapping the request to an Earthquake object.", e);
        }
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create earthquake, check the format of your request.");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all earthquakes.")
    public List<Earthquake> getEarthquakes(
        @ApiParam(value = "Name of the space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Skip the first n results.") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit number of results to return.") @DefaultValue("100") @QueryParam("limit") int limit) {

        try {
            List<Earthquake> earthquakes = repository.getEarthquakes();

            if (!spaceName.equals("")) {
                Space space = spaceRepository.getSpaceByName(spaceName);
                if (space == null) {
                    throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find space " + spaceName);
                }
                if (!authorizer.canRead(this.username, space.getPrivileges())) {
                    throw new IncoreHTTPException(Response.Status.NOT_FOUND, this.username + " is not authorized to read the space " + spaceName);
                }
                List<String> spaceMembers = space.getMembers();

                earthquakes = earthquakes.stream()
                    .filter(earthquake -> spaceMembers.contains(earthquake.getId()))
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
                return earthquakes;
            }

            Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());
            List<Earthquake> accessibleEarthquakes = earthquakes.stream()
                .filter(earthquake -> membersSet.contains(earthquake.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

            return accessibleEarthquakes;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid User Info!");
        }
    }

    @GET
    @Path("{earthquake-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the earthquake with matching id.")
    public Earthquake getEarthquake(
        @ApiParam(value = "Id of the earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId) {

        Earthquake earthquake = repository.getEarthquakeById(earthquakeId);
        if (earthquake == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find an earthquake with id " + earthquakeId);
        }
        //feeling lucky
        Space space = spaceRepository.getSpaceByName(this.username);
        if (space != null && space.hasMember(earthquakeId)) {
            return earthquake;
        }

        if (authorizer.canUserReadMember(this.username, earthquakeId, spaceRepository.getAllSpaces())) {
            return earthquake;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You don't have permission to access the earthquake with id" + earthquakeId);
    }

    @GET
    @Path("{earthquake-id}/raster")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns SeismicHazardResults for a given attenuation model-based earthquake id, demand type and unit, " +
        "coordinates and grid spacing.", notes = " SeismicHazardResults contains the metadata about the raster " +
        "data along with a list of HazardResults. Each HazardResult is a lat, long and hazard value.")
    public SeismicHazardResults getEarthquakeHazardForBox(
        @ApiParam(value = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Ground motion demand unit. Ex: g, %g, cm/s, etc", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Bounding box of a raster. Min X.", required = true) @QueryParam("minX") double minX,
        @ApiParam(value = "Bounding box of a raster. Min Y.", required = true) @QueryParam("minY") double minY,
        @ApiParam(value = "Bounding box of a raster. Max X.", required = true) @QueryParam("maxX") double maxX,
        @ApiParam(value = "Bounding box of a raster. max Y.", required = true) @QueryParam("maxY") double maxY,
        @ApiParam(value = "Grid spacing.", required = true) @QueryParam("gridSpacing") double gridSpacing,
        @ApiParam(value = "Amplify hazard.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard) {

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

            Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(earthquake);

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
                            demand, demandUnits, 0, amplifyHazard, this.username);
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

    @GET
    @Path("{earthquake-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns hazard values for the given earthquake id.",
        notes = "The results contain ground shaking parameter (PGA, SA, etc) for specific locations.")
    public List<SeismicHazardResult> getEarthquakeHazardValues(
        @ApiParam(value = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Ground motion demand unit. Ex: g, %g, cm/s, etc.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Amplify hazard by soil type.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Earthquake eq = getEarthquake(earthquakeId);

        String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);

        List<SeismicHazardResult> hazardResults = new LinkedList<SeismicHazardResult>();
        if (eq != null) {
            Map<BaseAttenuation, Double> attenuations = null;
            if (eq instanceof EarthquakeModel) {
                EarthquakeModel earthquake = (EarthquakeModel) eq;
                attenuations = attenuationProvider.getAttenuations(earthquake);
            }
            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());

                try {
                    hazardResults.add(HazardCalc.getGroundMotionAtSite(eq, attenuations, localSite, demandComponents[0],
                        demandComponents[1], demandUnits, 0, amplifyHazard, this.username));
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
    @ApiOperation(value = "Returns aleatory uncertainties for a model based earthquake")
    public Map<String, Double> getEarthquakeAleatoricUncertainties(
        @ApiParam(value = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Demand Type. Ex: PGA.", required = true) @QueryParam("demandType") String demandType) {
         Earthquake eq = getEarthquake(earthquakeId);
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
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
        } else {
            logger.error("Earthquake with id " + earthquakeId + " is not attenuation model based");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Earthquake with id " + earthquakeId + " is not attenuation model based");
        }
    }

    @GET
    @Path("{earthquake-id}/variance/{variance-type}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns total and epistemic variance for a model based earthquake")
    public List<VarianceResult> getEarthquakeVariance(
        @ApiParam(value = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Type of Variance. epistemic or total", required = true) @PathParam("variance-type") String varianceType,
        @ApiParam(value = "Demand Type. Ex: PGA.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Demand unit. Ex: g.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Earthquake eq = getEarthquake(earthquakeId);
        List<VarianceResult> varianceResults = new ArrayList<>();
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(earthquake);
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
                            cumulativeTotalVariance = Math.sqrt(Math.pow(aleatoricUncertainty, 2) + Math.pow(cumulativeEpistemicVariance, 2));

                        } else {
                            logger.error("Earthquake with id " + earthquakeId + " does not have valid attenuation uncertainties set");
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Earthquake with id " + earthquakeId + " does not have valid attenuation uncertainties set");
                        }
                    }
                    varianceResults.add(new VarianceResult(localSite.getLocation().getY(), localSite.getLocation().getX(),
                        demandType, demandUnits, cumulativeTotalVariance));
                } else if (varianceType.equalsIgnoreCase("epistemic")) {
                    varianceResults.add(new VarianceResult(localSite.getLocation().getY(), localSite.getLocation().getX(),
                        demandType, demandUnits, cumulativeEpistemicVariance));

                } else {
                    logger.error("Input variance type " + varianceType + " is not implemented");
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Input variance type " + varianceType + " is not implemented");
                }
            }

        } else {
            logger.error("Earthquake with id " + earthquakeId + " is not attenuation model based");
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Earthquake with id " + earthquakeId + " is not attenuation model based");
        }

        return varianceResults;

    }


    @GET
    @Path("{earthquake-id}/liquefaction/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns liquefaction (PGD) values, probability of liquefaction, and probability of ground failure.",
        notes = "This needs a valid susceptibility dataset as a shapefile for the earthquake location.")
    public List<LiquefactionHazardResult> getEarthquakeLiquefaction(
        @ApiParam(value = "ID of the Earthquake.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Geology dataset from data service.", required = true) @QueryParam("geologyDataset") String geologyId,
        @ApiParam(value = "Liquefaction demand unit. Ex: in, cm, etc", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'", required = true) @QueryParam("point") List<IncorePoint> points) {
        Earthquake eq = getEarthquake(earthquakeId);
        // TODO add logging/error for earthquake dataset that it can't be used
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(earthquake);

            List<LiquefactionHazardResult> hazardResults = new LinkedList<LiquefactionHazardResult>();
            SimpleFeatureCollection soilGeology = (SimpleFeatureCollection) GISUtil.getFeatureCollection(geologyId,
                this.username);

            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());
                // TODO find groundwater depth if shapefile is passed in
                hazardResults.add(HazardCalc.getLiquefactionAtSite(earthquake, attenuations, localSite, soilGeology,
                    demandUnits, this.username));
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
    @ApiOperation(value = "Returns earthquake site hazard amplification.", notes = " This returns the amplified " +
        "hazard given a methodology (e.g. NEHRP), soil map dataset id (optional), latitude, longitude, ground shaking " +
        "parameter (PGA, Sa, etc), hazard value, and default site class to use.")
    public Response getEarthquakeSiteAmplification(
        @ApiParam(value = "Method to get hazard amplification.", required = true) @QueryParam("method") String method,
        @ApiParam(value = "ID of site class dataset from data service.", required = true) @QueryParam("datasetId") @DefaultValue("") String datasetId,
        @ApiParam(value = "Latitude coordinate of the site.", required = true) @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude coordinate of the site.", required = true) @QueryParam("siteLong") double siteLong,
        @ApiParam(value = "Ground motion demand type. Ex: PGA, PGV, 0.2 SA, etc.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Hazard value.", required = true) @QueryParam("hazard") double hazard,
        @ApiParam(value = "Default site classification. Expected  A, B, C, D, E or F.") @QueryParam("defaultSiteClass") String defaultSiteClass) {

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
    @ApiOperation(hidden = true, value = "Returns earthquake slope amplification.")
    public Response getEarthquakeSlopeAmplification(
        @ApiParam(hidden = true, value = "Latitude coordinate of the site.") @QueryParam("siteLat") double siteLat,
        @ApiParam(hidden = true, value = "Longitude coordinate of the site.") @QueryParam("siteLong") double siteLong) {

        return Response.ok("Topographic amplification not yet implemented").build();
    }

    @GET
    @Path("models")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(hidden = true, value = "Returns available attenuation models.", notes = "This returns the available " +
        "attenuation models.")
    public Set<String> getSupportedEarthquakeModels() {
        return attenuationProvider.getAttenuations().keySet();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all earthquakes", notes = "Gets all earthquakes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No earthquakes found with the searched text")
    })
    public List<Earthquake> findEarthquakes(
        @ApiParam(value = "Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit number of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Earthquake> earthquakes;
        Earthquake earthquake = repository.getEarthquakeById(text);
        if (earthquake != null) {
            earthquakes = new ArrayList<Earthquake>() {{
                add(earthquake);
            }};
        } else {
            earthquakes = this.repository.searchEarthquakes(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());
        earthquakes = earthquakes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return earthquakes;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{earthquake-id}")
    @ApiOperation(value = "Deletes an earthquake", notes = "Also deletes attached dataset and related files")
    public Earthquake deleteEarthquake(@ApiParam(value = "Earthquake Id", required = true) @PathParam("earthquake-id") String earthquakeId) {
        Earthquake eq = getEarthquake(earthquakeId);

        if (authorizer.canUserDeleteMember(this.username, earthquakeId, spaceRepository.getAllSpaces())) {
            // delete associated datasets
            if (eq != null && eq instanceof EarthquakeModel) {
                EarthquakeModel scenarioEarthquake = (EarthquakeModel) eq;
                if (ServiceUtil.deleteDataset(scenarioEarthquake.getRasterDataset().getDatasetId(), this.username) == null) {
                    spaceRepository.addToOrphansSpace(scenarioEarthquake.getRasterDataset().getDatasetId());
                }
            } else if (eq != null && eq instanceof EarthquakeDataset) {
                EarthquakeDataset eqDataset = (EarthquakeDataset) eq;
                for (HazardDataset dataset : eqDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username) == null) {
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
