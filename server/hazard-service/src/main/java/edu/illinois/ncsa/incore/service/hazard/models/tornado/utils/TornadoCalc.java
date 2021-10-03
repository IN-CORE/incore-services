/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado.utils;

import com.google.common.math.LongMath;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.*;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
import edu.illinois.ncsa.incore.service.hazard.utils.GISUtil;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.util.List;

public class TornadoCalc {
    private static final Logger logger = Logger.getLogger(TornadoCalc.class);

    /**
     * Computes the wind hazard for the given location
     *
     * @param tornado     - Tornado event
     * @param localSite   - location to compute hazard
     * @param demandUnits - hazard units
     * @param simulation  - simulation number
     * @param initialSeed - initial seed
     * @return wind hazard at the specified location
     * @throws Exception
     */
    public static WindHazardResult getWindHazardAtSite(Tornado tornado, Point localSite, String demandUnits, int simulation,
                                                       int initialSeed, String username) throws TransformException, IOException {

        Double windHazard;
        String demandType = TornadoHazard.DEMAND_TYPE;
        if (tornado instanceof TornadoModel) {
            TornadoModel scenarioTornado = (TornadoModel) tornado;
            EFBox simulationEfBoxWidths = scenarioTornado.getEfBoxes().get(simulation);
            List<Double> efBoxWidths = simulationEfBoxWidths.getEfBoxWidths();

            LineString tornadoPath = TornadoUtils.createTornadoPath(scenarioTornado.getTornadoParameters(), simulation);
            List<Geometry> efBoxPolygons = TornadoUtils.createTornadoGeometry(scenarioTornado.getTornadoParameters(), efBoxWidths,
                tornadoPath);

            long seed = getRandomSeed(scenarioTornado.getTornadoParameters(), initialSeed, localSite);
            windHazard = BaseTornado.calculateWindSpeed(localSite, tornadoPath, efBoxPolygons, efBoxWidths,
                scenarioTornado.getTornadoParameters(), seed);
        } else {
            TornadoDataset tornadoDataset = (TornadoDataset) tornado;
            Object obj = GISUtil.getFeatureCollection(tornadoDataset.getDatasetId(), username);
            if (obj == null) {
                throw new IOException(" Could not calculate the grid coverage for the raster. Possibly because the dataset files are " +
                    "unreadable or not found.");
            } else {
                SimpleFeatureCollection efBoxPolygons = (SimpleFeatureCollection) obj;

                long seed = getRandomSeed(null, initialSeed, localSite);
                windHazard = TornadoCalc.calculateWindSpeedUniformRandomDist(localSite, efBoxPolygons, 250.0, seed);
            }
        }

        if (windHazard != null) {
            JSONObject tornadoThresholds;
            if (tornado.getThreshold() != null){
                tornadoThresholds = HazardUtil.toLowerKey(new JSONObject(tornado.getThresholdJsonString()));
            } else {
                tornadoThresholds = HazardUtil.toLowerKey(HazardUtil.TORNADO_THRESHOLDS);
            }
            if (tornadoThresholds.has(demandType.toLowerCase())) {
                JSONObject demandThreshold = (JSONObject) tornadoThresholds.get(demandType.toLowerCase());
                Double threshold = demandThreshold.get("value") == JSONObject.NULL ? null : demandThreshold.getDouble("value");
                // wind hazard value is always calculated in mph. Conversion not needed.
                if (threshold != null && windHazard <= threshold) {
                    windHazard = null;
                }
            }

            // Perform unit conversion if requested in mps
            if (windHazard != null && demandUnits.equalsIgnoreCase(TornadoHazard.WIND_MPS)) {
                windHazard *= getConversionFactor(demandUnits);
            }
        }

        return new WindHazardResult(demandUnits, windHazard);
    }

    /**
     * Determines which EF box the location is within and generates a uniform random wind speed for that boundary
     *
     * @param location      location to check
     * @param efBoxPolygons EF boxes
     * @param maxWindSpeed  Maximum wind speed for EF5
     * @return Random wind speed for location or 0.0 if outside tornado boundary
     */
    public static Double calculateWindSpeedUniformRandomDist(Point location, SimpleFeatureCollection efBoxPolygons, double maxWindSpeed,
                                                             long seed) {
        // check which EF box the point is in
        int efBox = -1;
        SimpleFeatureIterator iterator = null;

        try {
            iterator = efBoxPolygons.features();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry g = (Geometry) feature.getDefaultGeometry();

                if (location.within(g)) {
                    String efrating = feature.getAttribute(TornadoHazard.EF_RATING_FIELD).toString();
                    efBox = TornadoUtils.getEFRating(efrating);
                    break;
                }
            }
        } finally {
            if (iterator != null) {
                iterator.close();
            }
        }
        // If point is not in the path of the tornado return 0 mph
        if (efBox < 0) {
            return null;
        }

        double bottomSpeed = 0;
        double topSpeed = 0;

        // calc wind speed range in the container box
        if (efBox == 5) { // EF5
            bottomSpeed = TornadoHazard.efWindSpeed[efBox];
            topSpeed = maxWindSpeed;
        } else {
            bottomSpeed = TornadoHazard.efWindSpeed[efBox];
            topSpeed = TornadoHazard.efWindSpeed[efBox + 1];
        }

        UniformRealDistribution windDistribution = new UniformRealDistribution(new MersenneTwister(seed), bottomSpeed, topSpeed);

        return windDistribution.sample();
    }

    /**
     * Finds the conversion factor to convert miles per hour to target demand units
     *
     * @param targetUnits - target demand units
     * @return conversion factor for target demand units
     */
    public static double getConversionFactor(String targetUnits) throws UnsupportedOperationException {
        if (targetUnits.equalsIgnoreCase(TornadoHazard.WIND_MPS)) {
            return 0.44704;
        }

        throw new UnsupportedOperationException("Cannot convert from miles per hour to " + targetUnits);
    }

    /**
     * Get a unique random seed for a location using an initial seed (or system time) and the lat/long
     *
     * @param parameters  - Tornado parameters
     * @param initialSeed - initial seed value
     * @param location    - lat/long location
     * @return unique seed for a location
     */
    public static long getRandomSeed(TornadoParameters parameters, int initialSeed, Point location) {
        long seed = initialSeed;

        // Get seed from the model and override if no value specified
        if (seed == -1 && parameters != null) {
            seed = parameters.getRandomSeed();
        }

        // If no seed value provided OR model seed value was never set by the user, use current system time
        if (seed == -1) {
            seed = System.currentTimeMillis();
        }

        // Use 4 decimal places for getting unique seed values from lat/long
        try {
            seed = LongMath.checkedAdd(seed, (long) Math.abs((location.getX() + location.getY()) * 10000));
        } catch (ArithmeticException exception) {
            logger.warn("Seed + Math.abs((location.getX() + location.getY()) * 10000 exceeds max value, capping at Maximum value");
            seed = Long.MAX_VALUE;
        }

        return seed;
    }
}
