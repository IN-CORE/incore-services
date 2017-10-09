package edu.illinois.ncsa.incore.services.hazard;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.services.hazard.eq.EqParameters;
import edu.illinois.ncsa.incore.services.hazard.eq.Site;
import edu.illinois.ncsa.incore.services.hazard.eq.models.AtkinsonBoore1995;
import edu.illinois.ncsa.incore.services.hazard.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.services.hazard.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.services.hazard.eq.util.HazardUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

@Path("earthquake")
public class EarthquakeResource {
    private static final Logger logger = Logger.getLogger(EarthquakeResource.class);
    private GeometryFactory factory = new GeometryFactory();

    @Context
    ServletContext context;

    @GET
    @Path("/model")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getEarthquakeModelHazard(@QueryParam("modelId") String modelId, @QueryParam("demandType") String demandType, @QueryParam("demandUnits") String demandUnits, @QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong, @QueryParam("eqJson") String eqJson) {
        EqParameters eqParameters = null;
        try {
            eqParameters = new ObjectMapper().readValue(eqJson, EqParameters.class);

            // TODO Need to handle conversions where attenuation cannot directly produce requested demand type (e.g. SA to PGV)
            String period = demandType;
            String demand = demandType;

            if(demandType.contains(HazardUtil.SA)) {
                String[] demandSplit = demandType.split(" ");
                period = demandSplit[0];
                demand = demandSplit[1];
            }

            // TODO How can we store and lookup these model by ID?
            // TODO handle the case of a defined earthquake using multiple attenuations for weighting
            if (modelId.equalsIgnoreCase("AtkinsonBoore1995")) {
                try {
                    String fileName = modelId + ".csv";
                    URL coefficientURL = context.getResource("/WEB-INF/hazard/earthquake/coefficients/" + fileName);
                    AtkinsonBoore1995 model = new AtkinsonBoore1995();
                    model.readCoeffients(coefficientURL);
                    model.setRuptureParameters(eqParameters);

                    // Local site to get hazard for
                    Site localSite = new Site(factory.createPoint(new Coordinate(siteLong, siteLat)));

                    double value = model.getValue(period, localSite);
                    return Response.ok(value).build();
                } catch (MalformedURLException e) {
                    logger.error("Error locating model coefficients.", e);
                } catch (Exception e) {
                    logger.error("Error getting model value for point.", e);
                }
            } else {
                return Response.status(404).entity("Unknown attenuation model").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500).entity("Error reading earthquake parameters").build();
        }

        return Response.status(500).build();
    }

    // CMN: If we assume the Web application has access to the soil class layer then we could eliminate datasetId and lookup site classification on the client side
    @GET
    @Path("/soil/amplification")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getEarthquakeSiteAmplification(@QueryParam("method") String method, @QueryParam("datasetId") @DefaultValue("") String datasetId, @QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong, @QueryParam("demandType") String demandType, @QueryParam("hazard") double hazard, @QueryParam("defaultSiteClass") String defaultSiteClass) {

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

    // Dataset API

    @GET
    @Path("/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getEarthquakeDatasetHazard(@QueryParam("datasetId") String datasetId, @QueryParam("demandType") String demandType, @QueryParam("demandUnits") String demandUnits, @QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong) {
        // TODO Take the dataset id, retrieve the dataset and return the value at the raster point
        // Local site to get hazard for
        Site localSite = new Site(factory.createPoint(new Coordinate(siteLong, siteLat)));

        //HazardUtil.findRasterPoint(localSite, hazardRaster);
        return Response.ok("Return earthquake hazard from a dataset not yet implemented").build();
    }

    @GET
    @Path("/slope/amplification")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getEarthquakeSlopeAmplification(@QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong) {
        return Response.ok("Topographic amplification not yet implemented").build();
    }


}
