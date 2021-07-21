/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 * Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTornado {
    private static final Logger logger = Logger.getLogger(BaseTornado.class);
    protected List<EFBox> efBoxes = new ArrayList<EFBox>();
    protected List<Double> tornadoWidths = new ArrayList<Double>();

    /**
     * Get the Wind speed for a given location using the specified method.
     *
     * @param location      X/Y location to get the wind speed.
     * @param tornadoPath   Path of the tornado
     * @param efBoxPolygons EF box polygons
     * @param efBoxWidths   ef Box widths
     * @param seed          Seed value
     * @return Wind speed in miles per hour
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public static Double calculateWindSpeed(Point location, LineString tornadoPath, List<Geometry> efBoxPolygons, List<Double> efBoxWidths, TornadoParameters parameters, long seed
    ) throws MismatchedDimensionException, TransformException {
        // TODO consider exposing the computational method so it can be overridden
        if (parameters.getWindSpeedMethod() == 0) {
            return calculateWindSpeedLinearInterpolation(location, tornadoPath, efBoxPolygons, efBoxWidths, parameters);
        } else {
            return calculateWindSpeedUniformRandomDist(location, tornadoPath, efBoxPolygons, efBoxWidths, parameters, seed);
        }
    }

    /**
     * Get the Wind speed for a given location using linear interpolation.
     *
     * @param location    X/Y location to get the wind speed.
     * @param tornadoPath Path of the tornado
     * @param efBoxes     EF box polygons
     * @param efBoxWidths ef Box widths
     * @return Wind speed in miles per hour
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public static Double calculateWindSpeedLinearInterpolation(Point location, LineString tornadoPath, List<Geometry> efBoxes,
                                                               List<Double> efBoxWidths, TornadoParameters parameters) throws MismatchedDimensionException, TransformException {
        // check which EF box the point is in
        int containerBox = -1;
        for (int i = 0; i < efBoxes.size(); i++) {
            if (location.within(efBoxes.get(i)) || efBoxes.get(i).touches(location)) {
                containerBox = i;
                break;
            }
        }

        // If point is not in the path of the tornado return 0 mph
        if (containerBox < 0) {
            return null;
        }

        // calc shortest distance between path and location
        double distPt = DistanceOp.distance(tornadoPath, location);
        double boxWidth = efBoxWidths.get(containerBox);
        double bottomSpeed = 0;
        double topSpeed = 0;
        int spdLocation = efBoxes.size() - containerBox - 1;

        // calc wind speed range in the container box
        if (containerBox == 0 && TornadoUtils.getEFRating(parameters.getEfRating()) == 5) { // EF5
            bottomSpeed = TornadoHazard.efWindSpeed[spdLocation];
            topSpeed = parameters.getMaxWindSpeed();
        } else {
            bottomSpeed = TornadoHazard.efWindSpeed[spdLocation];
            topSpeed = TornadoHazard.efWindSpeed[spdLocation + 1];
        }

        // apply max speed if the max speed is lower than the highest speed
        if (containerBox == 0) {
            if (topSpeed > parameters.getMaxWindSpeed()) {
                topSpeed = parameters.getMaxWindSpeed();
            }
        }

        double lenFraction = distPt / boxWidth;
        double windSpeed = ((topSpeed - bottomSpeed) * (1 - lenFraction)) + bottomSpeed;

        return windSpeed;
    }

    /**
     * Get the Wind speed for a given location using uniform random distribution.
     *
     * @param location    X/Y location to get the wind speed.
     * @param tornadoPath Path of the tornado
     * @param efBoxes     EF box polygons
     * @param efBoxWidths ef Box widths
     * @param seed        Seed value
     * @return Wind speed in miles per hour
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    public static Double calculateWindSpeedUniformRandomDist(Point location, LineString tornadoPath, List<Geometry> efBoxes,
                                                             List<Double> efBoxWidths, TornadoParameters parameters, long seed) throws MismatchedDimensionException, TransformException {
        // check which EF box the point is in
        int containerBox = -1;
        for (int i = 0; i < efBoxes.size(); i++) {
            Geometry g = efBoxes.get(i);
            // sometimes geometry has an error due to JTS bug
            // if geometry has an error, rebuild it using buffer with zero distance
            if (!g.isValid()) {
                g = g.buffer(0);
            }

            if (location.within(g)) {
                containerBox = i;
                break;
            }
        }

        // If point is not in the path of the tornado return 0 mph
        if (containerBox < 0) {
            return null;
        }

        double bottomSpeed = 0;
        double topSpeed = 0;
        int spdLocation = efBoxes.size() - containerBox - 1;

        // calc wind speed range in the container box
        if (containerBox == 0 && TornadoUtils.getEFRating(parameters.getEfRating()) == 5) { // EF5
            bottomSpeed = TornadoHazard.efWindSpeed[spdLocation];
            topSpeed = parameters.getMaxWindSpeed();
        } else {
            bottomSpeed = TornadoHazard.efWindSpeed[spdLocation];
            topSpeed = TornadoHazard.efWindSpeed[spdLocation + 1];
        }

        UniformRealDistribution windDistribution = null;
        if(seed == -1) {
            windDistribution = new UniformRealDistribution(new MersenneTwister(), bottomSpeed, topSpeed);
        } else {
            // Add 1 to avoid interference
            windDistribution = new UniformRealDistribution(new MersenneTwister(seed + 1), bottomSpeed, topSpeed);
        }

        return windDistribution.sample();
    }

    /**
     * Returns whether the model requires an endpoint to be defined
     *
     * @return true if endpoint required, false otherwise.
     */
    public abstract boolean requiresEndPoint();

    public abstract void createTornado(TornadoParameters tornadoParameters);

    protected EFBox computeTornadoEFBoxWidths(Coordinate startPtCoordinate, Coordinate endPtCoordinate, double width, String efRating) {

        // EF box width multipliers
        double[] efWidthRate = getWidthMultiplier(efRating);
        EFBox boxWidth = new EFBox();
        try {
            Coordinate[] coords = new Coordinate[]{startPtCoordinate, endPtCoordinate};

            // convert given width, which is meter, to degree
            double tornadoWidth = width * TornadoHazard.METERS_TO_DEGREES;

            double tmpWidth = 0.0;
            double efWidth = 0.0;
            // Just the widths for each EF rating
            // widthArray = new double[efWidthRate.length];
            List<Double> efWidths = new ArrayList<Double>();
            for (int i = 0; i < efWidthRate.length; i++) {
                tmpWidth = tmpWidth + tornadoWidth * (efWidthRate[i] / 100);
                efWidth = tmpWidth / 2;
                efWidths.add(efWidth);
            }

            boxWidth.setEfBoxWidths(efWidths);

            return boxWidth;

        } catch (MismatchedDimensionException e) {
            logger.error("Error initializing GIS functionality required for determining raster point wind speed.", e); //$NON-NLS-1$
        }

        return null;
    }

    public List<Double> getTornadoWidths() {
        return tornadoWidths;
    }

    public void setTornadoWidths(List<Double> tornadoWidths) {
        this.tornadoWidths = tornadoWidths;
    }

    public List<EFBox> getEFBoxes() {
        return this.efBoxes;
    }

    private double[] getLengthMultiplier(String efRating) {
        int efRatingValue = TornadoUtils.getEFRating(efRating);

        // Find Length Multiplier
        switch (efRatingValue) {
            case 0:
                return TornadoHazard.ef0LenRate;
            case 1:
                return TornadoHazard.ef1LenRate;
            case 2:
                return TornadoHazard.ef2LenRate;
            case 3:
                return TornadoHazard.ef3LenRate;
            case 4:
                return TornadoHazard.ef4LenRate;
            case 5:
                return TornadoHazard.ef5LenRate;
            default:
                return null;
        }
    }

    private double[] getWidthMultiplier(String efRating) {
        int efRatingValue = TornadoUtils.getEFRating(efRating);

        // Find Width Multiplier
        switch (efRatingValue) {
            case 0:
                return TornadoHazard.ef0WidthRate;
            case 1:
                return TornadoHazard.ef1WidthRate;
            case 2:
                return TornadoHazard.ef2WidthRate;
            case 3:
                return TornadoHazard.ef3WidthRate;
            case 4:
                return TornadoHazard.ef4WidthRate;
            case 5:
                return TornadoHazard.ef5WidthRate;
            default:
                return null;
        }
    }

}
