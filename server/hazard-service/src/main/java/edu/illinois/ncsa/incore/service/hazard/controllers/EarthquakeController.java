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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.HazardConstants;
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
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
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
        description = "Incore Hazard Service For Earthquake, Tornado, Tsunami and Hurricane",
        version = "v0.2.0",
        title = "Incore v2 Hazard API",
        contact = @Contact(
            name = "Jong S. Lee",
            email = "jonglee@illinois.edu",
            url = "http://resilience.colostate.edu"
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

@Api(value="earthquakes", authorizations = {})

@Path("earthquakes")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class EarthquakeController {
    private static final Logger logger = Logger.getLogger(EarthquakeController.class);
    private GeometryFactory factory = new GeometryFactory();

    @Inject
    private IEarthquakeRepository repository;

    @Inject
    private AttenuationProvider attenuationProvider;

    @Inject
    private IAuthorizer authorizer;

    // TODO add endpoint to retrieve a list of models

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new scenario earthquake and returns the newly created scenario earthquake.",
        notes="Additionally, a geotiff (raster) is created by default and publish it to data repository. " +
            "User can create both scenario earthquake (with attenuation) and prob earthquake (with geotiff file upload).")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "earthquake", value = "Earthquake json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Earthquake files.", required = true, dataType = "string", paramType = "form")
    })
    public Earthquake createEarthquake(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(hidden = true) @FormDataParam("earthquake") String eqJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        // TODO finish adding log statements
        // First, get the Earthquake object from the form
        // TODO what should be done if a user sends multiple earthquake objects?
        ObjectMapper mapper = new ObjectMapper();
        Earthquake earthquake = null;
        try {
            earthquake = mapper.readValue(eqJson, Earthquake.class);
            earthquake.setPrivileges(Privileges.newWithSingleOwner(username));

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (earthquake != null && earthquake instanceof EarthquakeModel) {

                EarthquakeModel scenarioEarthquake = (EarthquakeModel) earthquake;

                Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(scenarioEarthquake);
                try {
                    File hazardFile = new File(incoreWorkDirectory, HazardConstants.HAZARD_TIF);

                    GridCoverage gc = HazardCalc.getEarthquakeHazardRaster(scenarioEarthquake, attenuations, username);
                    HazardCalc.getEarthquakeHazardAsGeoTiff(gc, hazardFile);

                    String demandType = scenarioEarthquake.getVisualizationParameters().getDemandType();
                    String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);
                    String description = "scenario earthquake visualization";
                    String datasetId = ServiceUtil.createRasterDataset(hazardFile, demandType + " hazard", username, description, HazardConstants.DETERMINISTIC_HAZARD_SCHEMA);

                    DeterministicHazardDataset rasterDataset = new DeterministicHazardDataset();
                    rasterDataset.setEqParameters(scenarioEarthquake.getEqParameters());
                    rasterDataset.setDatasetId(datasetId);
                    rasterDataset.setDemandType(demandComponents[1]);
                    rasterDataset.setDemandUnits(scenarioEarthquake.getVisualizationParameters().getDemandUnits());
                    rasterDataset.setPeriod(Double.parseDouble(demandComponents[0]));

                    scenarioEarthquake.setHazardDataset(rasterDataset);
                    earthquake = repository.addEarthquake(earthquake);
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
                        String datasetType = HazardConstants.DETERMINISTIC_HAZARD_SCHEMA;
                        if (hazardDataset instanceof ProbabilisticHazardDataset) {
                            datasetType = HazardConstants.PROBABILISTIC_HAZARD_SCHEMA;
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
                        BodyPartEntity bodyPartEntity = (BodyPartEntity)filePart.getEntity();
                        InputStream fis = bodyPartEntity.getInputStream();

                        String datasetId = ServiceUtil.createRasterDataset(filename, fis, eqDataset.getName() + " " + datasetName, username, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }
                    // Save changes to earthquake
                    earthquake = repository.addEarthquake(earthquake);
                    return earthquake;
                }
                else {
                    logger.error("Could not create Earthquake. Check your file extensions and the number of files in the request.");
                    throw new BadRequestException("Could not create Earthquake. Check your file extensions and the number of files in the request.");                }
            }

        } catch (IOException e) {
            logger.error("Error mapping the request to an Earthquake object.", e);
        }
        throw new BadRequestException("Could not create earthquake, check the format of your request.");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all scenario earthquakes.")
    public List<Earthquake> getEarthquakes(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username) {

        return repository.getEarthquakes().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }

    @GET
    @Path("{earthquake-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the scenario earthquake matching the id.")
    public Earthquake getEarthquake(
        @ApiParam(value = "Earthquake dataset guid from data service.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username) {

        Earthquake earthquake = repository.getEarthquakeById(earthquakeId);
        if (earthquake == null) {
            throw new NotFoundException();
        }
        if (!authorizer.canRead(username, earthquake.getPrivileges())) {
            throw new ForbiddenException();
        }
        return earthquake;
    }

    @GET
    @Path("{earthquake-id}/raster")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns SeismicHazardResults for the given earthquake id, demand type, demand unit, " +
        "min/max X/Y, and grid spacing. SeismicHazardResults contains the metadata about the raster data along " +
        "with a list of HazardResults. Each HazardResult is a lat, long and hazard value.")
    public SeismicHazardResults getScenarioEarthquakeHazardForBox(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Earthquake dataset guid from data service.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Liquefaction demand type. Ex: g.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Liquefaction demand unit. Ex: PGD.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Bounding box of a raster. Min X.", required = true) @QueryParam("minX") double minX,
        @ApiParam(value = "Bounding box of a raster. Min Y.", required = true) @QueryParam("minY") double minY,
        @ApiParam(value = "Bounding box of a raster. Max X.", required = true) @QueryParam("maxX") double maxX,
        @ApiParam(value = "Bounding box of a raster. max Y.", required = true) @QueryParam("maxY") double maxY,
        @ApiParam(value = "Grid spacing.", required = true) @QueryParam("gridSpacing") double gridSpacing,
        @ApiParam(value = "Amplify hazard.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard) {

        Earthquake eq = getEarthquake(earthquakeId, username);
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
                        hazardValue = HazardCalc.getGroundMotionAtSite(earthquake, attenuations, localSite, period, demand, demandUnits, 0, amplifyHazard, username);
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
    @ApiOperation(value = "Gets hazard value. Returns the requested ground shaking parameter (PGA, SA, etc) " +
        "for a lat/long location using the scenario earthquake specified.")
    public List<SeismicHazardResult> getScenarioEarthquakeHazardValues(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Earthquake dataset guid from data service.", required = true) @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Liquefaction demand type. Ex: g.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Liquefaction demand unit. Ex: PGD.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "Amplify hazard.", required = false) @QueryParam("amplifyHazard") @DefaultValue("true") boolean amplifyHazard,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Earthquake eq = getEarthquake(earthquakeId, username);

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
                    hazardResults.add(HazardCalc.getGroundMotionAtSite(eq, attenuations, localSite, demandComponents[0], demandComponents[1], demandUnits, 0, amplifyHazard, username));
                } catch (Exception e) {
                    throw new InternalServerErrorException("Error computing hazard.", e);
                }
            }

            return hazardResults;
        } else {
            logger.error("Could not find scenario earthquake with id " + earthquakeId);
            throw new NotFoundException("Could not find scenario earthquake with id " + earthquakeId);
        }
    }

    @GET
    @Path("{earthquake-id}/liquefaction/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets liquefaction (PGD) value.", notes="This needs a valid susceptibility dataset " +
        "as a shapefile for the earthquake location.")
    public List<LiquefactionHazardResult> getScenarioEarthquakeLiquefaction(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Earthquake dataset guid from data service.", required = true)  @PathParam("earthquake-id") String earthquakeId,
        @ApiParam(value = "Geology dataset from data service.", required = true) @QueryParam("geologyDataset") String geologyId,
        @ApiParam(value = "Ground Water Id that currently doesn't do anything.") @QueryParam("groundWaterId") @DefaultValue(("")) String groundWaterId,
        @ApiParam(value = "Liquefaction demand unit. Ex: PGD", required = true)@QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'", required = true) @QueryParam("point") List<IncorePoint> points) {
        Earthquake eq = getEarthquake(earthquakeId, username);
        // TODO add logging/error for earthquake dataset that it can't be used
        if (eq != null && eq instanceof EarthquakeModel) {
            EarthquakeModel earthquake = (EarthquakeModel) eq;
            Map<BaseAttenuation, Double> attenuations = attenuationProvider.getAttenuations(earthquake);

            List<LiquefactionHazardResult> hazardResults = new LinkedList<LiquefactionHazardResult>();
            SimpleFeatureCollection soilGeology = (SimpleFeatureCollection) GISUtil.getFeatureCollection(geologyId, username);

            for (IncorePoint point : points) {
                Site localSite = new Site(point.getLocation());
                // TODO find groundwater depth if shapefile is passed in
                hazardResults.add(HazardCalc.getLiquefactionAtSite(earthquake, attenuations, localSite, soilGeology, demandUnits, username));
            }

            return hazardResults;
        } else {
            logger.error("Could not find scenario earthquake with id " + earthquakeId);
            throw new NotFoundException("Could not find scenario earthquake with id " + earthquakeId);
        }
    }

    @GET
    @Path("/soil/amplification")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets earthquake site hazard amplification. Returns the amplified hazard given " +
        "a methodology (e.g. NEHRP), soil map dataset id (optional), latitude, longitude, ground shaking " +
        "parameter (PGA, Sa, etc), hazard value, and default site class to use.")
    public Response getEarthquakeSiteAmplification(
        @ApiParam(value = "Method to get hazard amplification.", required = true) @QueryParam("method") String method,
        @ApiParam(value = "Dataset from data service.", required = true) @QueryParam("datasetId") @DefaultValue("") String datasetId,
        @ApiParam(value = "Latitude coordinate of the site.", required = true) @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude coordinate of the site.", required = true) @QueryParam("siteLong") double siteLong,
        @ApiParam(value = "Liquefaction demand type. Ex: g.", required = true) @QueryParam("demandType") String demandType,
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
    @ApiOperation(value = "Gets earthquake slope amplification. Returns the amplified hazard given " +
        "latitude, longitude.")
    public Response getEarthquakeSlopeAmplification(
        @ApiParam(value = "Latitude coordinate of the site.") @QueryParam("siteLat") double siteLat,
        @ApiParam(value = "Longitude coordinate of the site.") @QueryParam("siteLong") double siteLong) {

        return Response.ok("Topographic amplification not yet implemented").build();
    }

    @GET
    @Path("models")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets attenuation models. Returns the requested ground shaking parameter " +
        "(PGA, SA, etc) for a lat/long location using the attenuation model specified.")
    public Set<String> getSupportedEarthquakeModels() {
        return attenuationProvider.getAttenuations().keySet();
    }

}
