/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EqVisualization;
import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.service.hazard.models.eq.liquefaction.HazusLiquefaction;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.LiquefactionHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.SeismicHazardResult;
import org.apache.log4j.Logger;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.media.jai.RasterFactory;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class HazardCalc {
    private static final Logger logger = Logger.getLogger(HazardCalc.class);
    private static GeometryFactory factory = new GeometryFactory();

    public static LiquefactionHazardResult getLiquefactionAtSite(ScenarioEarthquake earthquake, Map<BaseAttenuation, Double> attenuations, Site site, SimpleFeatureCollection soilGeology, String demandUnits) {
        HazusLiquefaction liquefaction = new HazusLiquefaction();
        String susceptibilitity = null;
        double pgaValue = 0.0;
        double groundDeformation = 0.0;
        double[] groundFailureProb = new double[3];
        double magnitude = earthquake.getEqParameters().getMagnitude();
        // Assumption from Hazus
        // TODO this should be optionally provided by the user with a ground water depth map
        double groundWaterDepth = 5.0;
        try {
            SimpleFeature feature = HazardUtil.getPointInPolygon(site.getLocation(), soilGeology);
            if (feature != null) {
                susceptibilitity = feature.getAttribute("liq_suscep").toString();
                pgaValue = getGroundMotionAtSite(earthquake, attenuations, site, "PGA", "PGA", "g", 0, true).getHazardValue();
                groundDeformation = liquefaction.getPermanentGroundDeformation(susceptibilitity, pgaValue, magnitude);
                double liqProbability = liquefaction.getProbabilityOfLiquefaction(earthquake.getEqParameters().getMagnitude(), pgaValue, susceptibilitity, groundWaterDepth);
                groundFailureProb = liquefaction.getProbabilityOfGroundFailure(susceptibilitity, pgaValue, groundWaterDepth, magnitude);

                // Default units of permanent ground deformation
                String pgdUnits = HazardUtil.units_in;
                if (demandUnits.equalsIgnoreCase(HazardUtil.units_cm)) {
                    pgdUnits = HazardUtil.units_cm;
                    groundDeformation *= 2.54;
                }

                // TODO we could add other conversions or let the user convert from the default
                return new LiquefactionHazardResult(site.getLocation().getY(), site.getLocation().getX(), groundDeformation, pgdUnits, liqProbability, groundFailureProb);
            } else {
                return new LiquefactionHazardResult(site.getLocation().getY(), site.getLocation().getX(), 0.0, "in", 0.0, groundFailureProb);
            }
        } catch (Exception e) {
            logger.error("Could not compute PGA ground motion required to determine liquefaction.");
            return null;
        }

    }

    public static SeismicHazardResult getGroundMotionAtSite(ScenarioEarthquake earthquake, Map<BaseAttenuation, Double> attenuations, Site site, String hazardType, String demand, String demandUnits, int spectrumOverride, boolean amplifyHazard) throws Exception {
        Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();

        if (HazardUtil.SD.equalsIgnoreCase(hazardType)) {
            // TODO CMN - I don't see any example that uses this so deferring implementation
            throw new UnsupportedHazardException("Conversion to SD is not yet implemented");
        } else if (HazardUtil.PGV.equalsIgnoreCase(hazardType)) {
            // First, check if it supports the hazard directly from the attenuation models
            boolean supported = supportsHazard(attenuations, hazardType);
            // If not supported, check if it supports 1.0 Sec SA
            if (!supported) {
                supported = supportsHazard(attenuations, "1.0 Sa");

                if (!supported) {
                    throw new UnsupportedHazardException(hazardType + " is not supported and cannot be converted to given the scenario earthquake");
                }
                logger.debug(hazardType + " is not directly supported by the scenario earthquake, using 1.0 second SA to compute " + hazardType);

                SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, "1.0", "Sa", spectrumOverride, amplifyHazard);
                double updatedHazardVal = HazardUtil.convertHazard(result.getHazardValue(), "g", 1.0, HazardUtil.SA, demandUnits, HazardUtil.PGV);
                return new SeismicHazardResult(updatedHazardVal, HazardUtil.PGV, HazardUtil.PGV, demandUnits);
            }

        } else {
            boolean supported = supportsHazard(attenuations, hazardType);
            if (!supported) {
                // TODO add spectrum method support so we can infer values
                throw new UnsupportedHazardException(hazardType + " is not supported by the given scenario earthquake.");
            }
            return computeGroundMotionAtSite(earthquake, attenuations, site, hazardType, demand, spectrumOverride, amplifyHazard);
        }

        return null;

    }

    public static SeismicHazardResult computeGroundMotionAtSite(ScenarioEarthquake earthquake, Map<BaseAttenuation, Double> attenuations, Site site, String hazardType, String demand, int spectrumOverride, boolean amplifyHazard) throws Exception {
        double hazardValue = 0.0;
        String closestHazardPeriod = hazardType;
        Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();
        while (iterator.hasNext()) {
            BaseAttenuation model = iterator.next();
            double weight = attenuations.get(model);
            SeismicHazardResult matchedResult = model.getValueClosestMatch(hazardType, site);
            hazardValue += (Math.log(matchedResult.getHazardValue()) * weight);

            closestHazardPeriod = matchedResult.getPeriod();
        }

        hazardValue = Math.exp(hazardValue);

        // TODO check if site class dataset is defined for amplifying hazard
        int siteClass = HazardUtil.getSiteClassAsInt(earthquake.getDefaultSiteClass());

        SiteAmplification siteAmplification = null;
        if (amplifyHazard) {
            // TODO need to add check for if VS already accounted for soil type

            // TODO add check for Rix Fernandez, no need to amplify

            // TODO Add support for other amplification methods besides NEHRP

            if (earthquake.getSiteAmplification().equalsIgnoreCase("NEHRP")) {
                siteAmplification = new NEHRPSiteAmplification();

                if (closestHazardPeriod.equalsIgnoreCase(HazardUtil.PGV)) {
                    double pga = computeGroundMotionAtSite(earthquake, attenuations, site, "PGA", demand, spectrumOverride, false).getHazardValue();
                    hazardValue *= siteAmplification.getSiteAmplification(site, pga, siteClass, closestHazardPeriod);
                } else {
                    // Note, hazard value input should be PGA if amplifying PGV hazard because NEHRP uses PGA coefficients for amplifying PGV
                    // and the range for interpretation is in units of g
                    hazardValue *= siteAmplification.getSiteAmplification(site, hazardValue, siteClass, closestHazardPeriod);
                }
            }

        }

        return new SeismicHazardResult(hazardValue, closestHazardPeriod, demand);
    }

    public static boolean supportsHazard(Map<BaseAttenuation, Double> attenuations, String demandType) {
        Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();
        boolean canOutputHazard = true;
        while (iterator.hasNext()) {
            BaseAttenuation model = iterator.next();
            if (!model.canOutput(demandType)) {
                canOutputHazard = false;
            }
        }

        return canOutputHazard;
    }

    public static GridCoverage getEarthquakeHazardRaster(ScenarioEarthquake scenarioEarthquake, Map<BaseAttenuation, Double> attenuations) throws Exception {
        EqVisualization visualizationParameters = scenarioEarthquake.getVisualizationParameters();
        boolean amplifyHazard = visualizationParameters.isAmplifyHazard();
        double minX = visualizationParameters.getMinX();
        double maxX = visualizationParameters.getMaxX();
        double minY = visualizationParameters.getMinY();
        double maxY = visualizationParameters.getMaxY();
        int numPoints = visualizationParameters.getNumPoints();

        String demandType = visualizationParameters.getDemandType();
        String period = demandType;
        String demand = demandType;

        if (Pattern.compile(Pattern.quote(HazardUtil.SA), Pattern.CASE_INSENSITIVE).matcher(demandType).find()) {
            String[] demandSplit = demandType.split(" ");
            period = demandSplit[0];
            demand = demandSplit[1];
        }

        int width = 0;
        int height = 0;

        // Compute Grid spacing
        double gridSpacing = Math.sqrt((maxX - minX) * (maxY - minY) / numPoints);

        double dx = (maxX - minX);
        double dy = (maxY - minY);

        dx = Math.ceil(dx / gridSpacing) * gridSpacing;
        dy = Math.ceil(dy / gridSpacing) * gridSpacing;

        // Make sure we end up slightly past the end or we might end up with a grid smaller than intended
        maxX = minX + dx;
        maxY = minY + dy;

        // Recompute the grid spacing using the new min/max so number of points matches request
        gridSpacing = Math.sqrt((maxX - minX) * (maxY - minY) / numPoints);

        if (gridSpacing != 0) {
            long widthLong = Math.round(Math.abs((maxX - minX) / gridSpacing)); // + 1;
            long heightLong = Math.round(Math.abs((maxY - minY) / gridSpacing)); // + 1;
            if ((widthLong > Integer.MAX_VALUE) || (heightLong > Integer.MAX_VALUE)) {
                logger.error("Overflow....too many points to fit in an int");
            }
            // adjustMaxMin();
            width = (int) widthLong;
            height = (int) heightLong;

        }

        float cellsize = (float) gridSpacing;
        float startX = (float) minX + ((float) gridSpacing / 2.0f);
        float startY = (float) maxY - ((float) gridSpacing / 2.0f);

        Site localSite = null;

        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GridCoverageFactory gcFactory = CoverageFactoryFinder.getGridCoverageFactory(null);
        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width, height, 1, null);

        // Get default demand units for the hazard type
        String demandUnits = BaseAttenuation.getUnits(demand);
        Envelope envelope = new Envelope2D(crs, minX, minY, width * cellsize, height * cellsize);
        for (int y = 0; y < height; y++) {

            startX = (float) minX + (cellsize / 2.0f);
            for (int x = 0; x < width; x++) {
                localSite = new Site(factory.createPoint(new Coordinate(startX, startY)));
                double hazardValue = getGroundMotionAtSite(scenarioEarthquake, attenuations, localSite, period, demand, demandUnits, 0, amplifyHazard).getHazardValue();

                raster.setSample(x, y, 0, hazardValue);

                startX += (float) gridSpacing;

            }
            startY -= gridSpacing;
        }

        Color[] colors = new Color[]{Color.BLUE, Color.CYAN, Color.WHITE, Color.YELLOW, Color.RED};
        GridCoverage gc = gcFactory.create("Hazard Coverage", raster, envelope, null, null, null, new Color[][]{colors}, null);
        return gc;
    }

    public static void getEarthquakeHazardAsAsciiGrid(GridCoverage gridCoverage, File rasterFile) throws IOException {
        GridCoverageWriter writer = new ArcGridFormat().getWriter(rasterFile);

        final ArcGridWriteParams wp = new ArcGridWriteParams();
        wp.setSourceBands(new int[]{0});

        ParameterValueGroup params = writer.getFormat().getWriteParameters();
        params.parameter("GRASS").setValue(false);
        params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

        GeneralParameterValue[] gpv = {params.parameter("GRASS"), params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())};
        writer.write(gridCoverage, gpv);
        writer.dispose();
    }

    public static void getEarthquakeHazardAsGeoTiff(GridCoverage gridCoverage, File tiffFile) throws IOException {
        //getting a format
        final GeoTiffFormat format = new GeoTiffFormat();

        //getting the write parameters
        final GeoTiffWriteParams wp = new GeoTiffWriteParams();

        //setting compression to LZW
        wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
        wp.setCompressionType("LZW");
        wp.setCompressionQuality(0.75F);

        //setting the write parameters for this geotiff
        final ParameterValueGroup params = format.getWriteParameters();
        params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

        GridCoverageWriter writer = format.getWriter(tiffFile);
        writer.write(gridCoverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
        writer.dispose();
    }

}
