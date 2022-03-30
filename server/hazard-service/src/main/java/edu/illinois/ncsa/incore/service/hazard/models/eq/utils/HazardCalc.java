/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import edu.illinois.ncsa.incore.service.hazard.models.eq.liquefaction.HazusLiquefaction;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.NEHRPSiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.site.SiteAmplification;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.LiquefactionHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.SeismicHazardResult;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.log4j.Logger;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.coverage.PointOutsideCoverageException;
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

public class HazardCalc {
    private static final Logger logger = Logger.getLogger(HazardCalc.class);
    private static final GeometryFactory factory = new GeometryFactory();

    public static LiquefactionHazardResult getLiquefactionAtSite(Earthquake earthquake,
                                                                 Map<BaseAttenuation, Double> attenuations,
                                                                 Site site, SimpleFeatureCollection soilGeology,
                                                                 String demandUnits, SimpleFeatureCollection siteClassFC,
                                                                 String creator) {

        // TODO fix this for dataset
        EarthquakeModel eqModel = (EarthquakeModel) earthquake;
        HazusLiquefaction liquefaction = new HazusLiquefaction();
        String susceptibilitity = null;
        double pgaValue = 0.0;
        double groundDeformation = 0.0;
        double[] groundFailureProb = new double[3];
        double magnitude = eqModel.getEqParameters().getMagnitude();
        // Assumption from Hazus
        // TODO this should be optionally provided by the user with a ground water depth map
        double groundWaterDepth = 5.0;
        try {

            SimpleFeature feature = GISUtil.getPointInPolygon(site.getLocation(), soilGeology);
            if (feature != null) {
                susceptibilitity = feature.getAttribute(HazardUtil.LIQ_SUSCEPTIBILITY).toString();
                pgaValue = getGroundMotionAtSite(earthquake, attenuations, site, "0.0", HazardUtil.PGA,
                    HazardUtil.units_g, 0, true, siteClassFC, creator).getHazardValue();
                groundDeformation = liquefaction.getPermanentGroundDeformation(susceptibilitity, pgaValue, magnitude);
                double liqProbability = liquefaction.getProbabilityOfLiquefaction(eqModel.getEqParameters().getMagnitude(), pgaValue,
                    susceptibilitity, groundWaterDepth);
                groundFailureProb = liquefaction.getProbabilityOfGroundFailure(susceptibilitity, pgaValue, groundWaterDepth, magnitude);

                // Default units of permanent ground deformation
                String pgdUnits = HazardUtil.units_in;
                if (demandUnits.equalsIgnoreCase(HazardUtil.units_cm)) {
                    pgdUnits = HazardUtil.units_cm;
                    groundDeformation *= 2.54;
                }

                // TODO we could add other conversions or let the user convert from the default
                return new LiquefactionHazardResult(site.getLocation().getY(), site.getLocation().getX(), groundDeformation, pgdUnits,
                    liqProbability, groundFailureProb);
            } else {
                return new LiquefactionHazardResult(site.getLocation().getY(), site.getLocation().getX(), 0.0, "in", 0.0,
                    groundFailureProb);
            }
        } catch (Exception e) {
            logger.error("Could not compute PGA ground motion required to determine liquefaction.");
            return null;
        }

    }

    public static SeismicHazardResult getGroundMotionAtSite(Earthquake earthquake, Map<BaseAttenuation, Double> attenuations,
                                                            Site site, String period, String hazardType, String demandUnits,
                                                            int spectrumOverride, boolean amplifyHazard,
                                                            SimpleFeatureCollection siteClassFC,
                                                            String creator) throws Exception {
        // If demand type units is null, use default for the requested demand
        // This might not be the best way to handle it, but it is at least consistent in providing the units of what is
        // being returned
        if (demandUnits == null) {
            // This code should be probably not be inside the BaseAttenuation
            demandUnits = BaseAttenuation.getUnits(hazardType);
        }

        if (amplifyHazard && siteClassFC != null) {
            SimpleFeature feature = GISUtil.getPointInPolygon(site.getLocation(), siteClassFC);
            if (feature != null && feature.getAttribute(HazardUtil.SOILTYPE) != null) {
                String siteClass = feature.getAttribute(HazardUtil.SOILTYPE).toString();
                site.setSiteClass(siteClass);
            }
        }

        if (HazardUtil.SD.equalsIgnoreCase(hazardType)) {
            boolean supported = supportsHazard(earthquake, attenuations, period, hazardType, true);
            if (!supported) {
                SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, period, HazardUtil.SA,
                    spectrumOverride, amplifyHazard, creator, demandUnits);
                // We use the result period for conversion because in the case of closest match, the requested period may not have been used
                Double updatedHazardValue = null;
                if (result.getHazardValue() != null) {
                    updatedHazardValue = HazardUtil.convertHazard(result.getHazardValue(), result.getUnits(),
                        Double.parseDouble(result.getPeriod()), HazardUtil.SA, demandUnits, HazardUtil.SD);
                }
                return new SeismicHazardResult(updatedHazardValue, result.getPeriod(), HazardUtil.SD, demandUnits);
            } else {
                SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, period, hazardType,
                    spectrumOverride, amplifyHazard, creator, demandUnits);
                Double updatedHazardValue = null;
                if (result.getHazardValue() != null) {
                    updatedHazardValue = HazardUtil.convertHazard(result.getHazardValue(), result.getUnits(),
                        Double.parseDouble(result.getPeriod()), result.getDemand(), demandUnits, result.getDemand());
                }
                return new SeismicHazardResult(updatedHazardValue, result.getPeriod(), result.getDemand(), demandUnits);
            }
        } else if (HazardUtil.PGV.equalsIgnoreCase(hazardType)) {
            // First, check if the hazard is directly from the attenuation models or datasets
            boolean supported = supportsHazard(earthquake, attenuations, period, hazardType, true);
            // If not supported, check if it supports 1.0 Sec SA
            if (!supported) {
                supported = supportsHazard(earthquake, attenuations, "1.0", "Sa", true);

                if (!supported) {
                    throw new UnsupportedHazardException(hazardType + " is not supported and cannot be converted to given the defined " +
                        "earthquake");
                }
                logger.debug(hazardType + " is not directly supported by the earthquake, using 1.0 second SA to compute " + hazardType);

                SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, "1.0",
                    "Sa", spectrumOverride, amplifyHazard, creator, null);
                Double updatedHazardVal = null;
                if (result.getHazardValue() != null) {
                    updatedHazardVal = HazardUtil.convertHazard(result.getHazardValue(), "g", 1.0, HazardUtil.SA, demandUnits,
                        HazardUtil.PGV);
                }
                return new SeismicHazardResult(updatedHazardVal, "0.0", HazardUtil.PGV, demandUnits);
            } else {
                // Before returning the result, make sure the requested demand unit matches the demand unit produced by the EQ
                SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, period, hazardType,
                    spectrumOverride, amplifyHazard, creator, demandUnits);
                Double updatedHazardValue = null;
                if (result.getHazardValue() != null) {
                    updatedHazardValue = HazardUtil.convertHazard(result.getHazardValue(), result.getUnits(),
                        Double.parseDouble(result.getPeriod()), result.getDemand(), demandUnits, result.getDemand());
                }

                return new SeismicHazardResult(updatedHazardValue, result.getPeriod(), result.getDemand(), demandUnits);
            }

        } else {
            // TODO we need to modify this when we support spectrum methods
            boolean supported = supportsHazard(earthquake, attenuations, period, hazardType, false);
            if (!supported) {
                // TODO add spectrum method support so we can infer values
                logger.warn(hazardType + " is not supported by the defined earthquake.");
                return null;
            }
            SeismicHazardResult result = computeGroundMotionAtSite(earthquake, attenuations, site, period, hazardType, spectrumOverride,
                amplifyHazard,
                creator, demandUnits);
            Double updatedHazardValue = null;
            if (result.getHazardValue() != null) {
                updatedHazardValue = HazardUtil.convertHazard(result.getHazardValue(), result.getUnits(),
                    Double.parseDouble(result.getPeriod()), result.getDemand(), demandUnits, result.getDemand());
            }

            // Before returning the result, make sure the requested demand unit matches the demand unit produced by the EQ
            return new SeismicHazardResult(updatedHazardValue, result.getPeriod(), result.getDemand(), demandUnits);
        }
    }

    public static Double applyEqThresholds(JSONObject earthquakeThresholds, double hazardValue,
                                           String demand, String demandUnits, String closestHazardPeriod) {
        Double adjustedHazardValue = hazardValue;
        if (earthquakeThresholds.has(demand.toLowerCase()) && earthquakeThresholds.get(demand.toLowerCase()) != JSONObject.NULL) {
            JSONObject demandThresholds = ((JSONObject) earthquakeThresholds.get(demand.toLowerCase()));

            if (demandThresholds.get("value") != JSONObject.NULL) {
                double threshold = HazardUtil.convertHazard(demandThresholds.getDouble("value"),
                    demandThresholds.getString("unit"), Double.parseDouble(closestHazardPeriod), demand.toLowerCase(),
                    demandUnits, demand.toLowerCase());
                if (hazardValue <= threshold) {
                    adjustedHazardValue = null;
                }
            }
        }
        return adjustedHazardValue;
    }

    public static SeismicHazardResult computeGroundMotionAtSite(Earthquake earthquake, Map<BaseAttenuation, Double> attenuations,
                                                                Site site, String period, String demand, int spectrumOverride,
                                                                boolean amplifyHazard, String creator,
                                                                String demandUnits) throws Exception {

        Double hazardValue = 0.0;
        String closestHazardPeriod = period;
        if (earthquake instanceof EarthquakeModel) {
            // Handles the case where PGV/PGD/PGA are all at 0.0 so coefficients are stored by demand type
            // This could be fixed by storing all coefficients in a more verbose way, e.g. 0.0 PGA, 0.0 PGV, 0.2 Sa, etc
            String hazardType = demand;
            if (Double.parseDouble(period) != 0.0) {
                // Sa values are all stored by period 0.2, 0.3, etc
                hazardType = period;
            }

            EarthquakeModel eqModel = (EarthquakeModel) earthquake;
            Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();
            while (iterator.hasNext()) {
                BaseAttenuation model = iterator.next();
                double weight = attenuations.get(model);
                demandUnits = BaseAttenuation.getUnits(demand);
                SeismicHazardResult matchedResult = model.getValueClosestMatch(hazardType, site);

                hazardValue += (Math.log(matchedResult.getHazardValue()) * weight);

                closestHazardPeriod = matchedResult.getPeriod();
            }

            hazardValue = Math.exp(hazardValue);

            // Get site class information if available, otherwise use the scenario default
            int siteClass = site.getSiteClass() != null ? HazardUtil.getSiteClassAsInt(site.getSiteClass()) :
                HazardUtil.getSiteClassAsInt(eqModel.getDefaultSiteClass());

            SiteAmplification siteAmplification = null;
            if (amplifyHazard && siteClass != -1) {
                // TODO need to add check for if VS already accounted for soil type

                // TODO add check for Rix Fernandez, no need to amplify

                // TODO Add support for other amplification methods besides NEHRP

                if (eqModel.getSiteAmplification().equalsIgnoreCase("NEHRP")) {
                    siteAmplification = new NEHRPSiteAmplification();

                    if (closestHazardPeriod.equalsIgnoreCase(HazardUtil.PGV)) {
                        double pga = computeGroundMotionAtSite(earthquake, attenuations, site, "0.0", "PGA",
                            spectrumOverride, false, creator, null).getHazardValue();
                        hazardValue *= siteAmplification.getSiteAmplification(site, pga, siteClass, closestHazardPeriod);
                    } else {
                        // Note, hazard value input should be PGA if amplifying PGV hazard because NEHRP uses PGA coefficients for
                        // amplifying PGV and the range for interpretation is in units of g
                        hazardValue *= siteAmplification.getSiteAmplification(site, hazardValue, siteClass, closestHazardPeriod);
                    }
                }

            }

            // A bit of a hack to return PGA/PGD/PGV with period as 0.0 instead of PGA/PGV/PGD which was used to locate the coefficients
            if (Double.parseDouble(period) == 0.0) {
                closestHazardPeriod = "0.0";
            }
            JSONObject eqThresholds = HazardUtil.toLowerKey(HazardUtil.EARTHQUAKE_THRESHOLDS);
            Double adjHazardVal = HazardCalc.applyEqThresholds(eqThresholds, hazardValue, demand, demandUnits, closestHazardPeriod);
            return new SeismicHazardResult(adjHazardVal, closestHazardPeriod, demand);

        } else {
            EarthquakeDataset eqDataset = (EarthquakeDataset) earthquake;
            HazardDataset hazardDataset = HazardUtil.findHazard(eqDataset.getHazardDatasets(), demand, period, false);
            closestHazardPeriod = Double.toString(hazardDataset.getPeriod());

            GridCoverage gc = GISUtil.getGridCoverage(hazardDataset.getDatasetId(), creator);
            try {
                JSONObject eqThresholds;
                if (hazardDataset.getThreshold() != null) {
                    eqThresholds = HazardUtil.toLowerKey(new JSONObject(hazardDataset.getThresholdJsonString()));
                } else {
                    eqThresholds = HazardUtil.toLowerKey(HazardUtil.EARTHQUAKE_THRESHOLDS);
                }

                hazardValue = HazardUtil.findRasterPoint(site.getLocation(), (GridCoverage2D) gc);
                Double adjHazardVal = HazardCalc.applyEqThresholds(eqThresholds,
                    hazardValue, hazardDataset.getDemandType(), hazardDataset.getDemandUnits(), closestHazardPeriod);

                if (adjHazardVal != null) {
                    Double convertedHazardVal = HazardUtil.convertHazard(hazardValue, hazardDataset.getDemandUnits(),
                        Double.parseDouble(period), hazardDataset.getDemandType(), demandUnits, demand);
                    return new SeismicHazardResult(convertedHazardVal, closestHazardPeriod, demand, demandUnits);
                } else {
                    return new SeismicHazardResult(null, closestHazardPeriod, demand, demandUnits);
                }

            } catch (PointOutsideCoverageException e) {
                logger.debug("Point outside tiff image.");
                return new SeismicHazardResult(null, closestHazardPeriod, demand, demandUnits);
            }
        }
    }

    public static boolean supportsHazard(Earthquake earthquake, Map<BaseAttenuation, Double> attenuations, String period,
                                         String demandType, boolean exactOnly) {
        boolean canOutputHazard = true;
        if (earthquake instanceof EarthquakeModel) {
            String fullDemandType = HazardUtil.getFullDemandType(period, demandType);
            Iterator<BaseAttenuation> iterator = attenuations.keySet().iterator();
            while (iterator.hasNext()) {
                BaseAttenuation model = iterator.next();
                if (!model.canOutput(fullDemandType)) {
                    if (exactOnly) {
                        canOutputHazard = false;
                    } else {
                        // If we don't need an exact match, check for closest match
                        if (model.closestSupportedHazard(period.trim() + " " + demandType.trim()) == null) {
                            canOutputHazard = false;
                        }
                    }
                }
            }

        } else {
            HazardDataset hazardDataset = HazardUtil.findHazard(((EarthquakeDataset) earthquake).getHazardDatasets(), demandType, period,
                exactOnly);
            if (hazardDataset == null) {
                canOutputHazard = false;
            }
        }

        return canOutputHazard;
    }

    public static GridCoverage getEarthquakeHazardRaster(EarthquakeModel scenarioEarthquake,
                                                         Map<BaseAttenuation, Double> attenuations,
                                                         String creator) throws Exception {
        EqVisualization visualizationParameters = scenarioEarthquake.getVisualizationParameters();
        boolean amplifyHazard = visualizationParameters.isAmplifyHazard();
        double minX = visualizationParameters.getMinX();
        double maxX = visualizationParameters.getMaxX();
        double minY = visualizationParameters.getMinY();
        double maxY = visualizationParameters.getMaxY();
        int numPoints = visualizationParameters.getNumPoints();

        String demandType = visualizationParameters.getDemandType();
        String[] demandComponents = HazardUtil.getHazardDemandComponents(demandType);
//        String period = demandType;
//        String demand = demandType;
//
//        if (Pattern.compile(Pattern.quote(HazardUtil.SA), Pattern.CASE_INSENSITIVE).matcher(demandType).find()) {
//            String[] demandSplit = demandType.split(" ");
//            period = demandSplit[0];
//            demand = demandSplit[1];
//        }

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
        String demandUnits = BaseAttenuation.getUnits(demandComponents[1]);
        Envelope envelope = new Envelope2D(crs, minX, minY, width * cellsize, height * cellsize);
        for (int y = 0; y < height; y++) {

            startX = (float) minX + (cellsize / 2.0f);
            for (int x = 0; x < width; x++) {
                localSite = new Site(factory.createPoint(new Coordinate(startX, startY)));
                double hazardValue = getGroundMotionAtSite(scenarioEarthquake, attenuations,
                    localSite, demandComponents[0], demandComponents[1], demandUnits,
                    0, amplifyHazard, null, creator).getHazardValue();

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

        GeneralParameterValue[] gpv = {params.parameter("GRASS"),
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())};
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
        writer.write(gridCoverage, params.values().toArray(new GeneralParameterValue[1]));
        writer.dispose();
    }

}
