/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro, Chen Wang (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import com.opengamma.analytics.math.statistics.distribution.GeneralizedExtremeValueDistribution;
import org.locationtech.jts.geom.Coordinate;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class RandomLengthWidthAngleTornado extends BaseTornado {

    private static double[] maxWidth = {2500.0, 3000.0, 3520.0, 3872.0, 4400.0, 3000.0}; // Maximum width for EF0 - EF5

    private static double[] maxLength = {103.50, 99.80, 168.50, 169.70, 148.97, 132.00}; // Maximum length for EF0 - EF5
    // GEV shape parameters, EF0 - EF5
    private static double[] widthShape = {0.4228, 0.595078, 0.657065, 0.515384, 0.264031, 0.185012};

    private static double[] widthScale = {16.5948, 37.1997, 81.1465, 198.266, 364.877, 421.403};
    private static double[] widthLocation = {20.4790, 40.5151, 77.711, 197.736, 383.485, 522.085};
    private static double[] lengthShape = {0.9741, 1.2606, 0.8398, 0.4132, 0.3927, 0.1812};
    private static double[] lengthScale = {0.2165, 0.8913, 2.7859, 7.2996, 11.7168, 17.6212};
    private static double[] lengthLocation = {0.1938, 0.6467, 2.2052, 7.5138, 12.4357, 20.4455};

    @Override
    public boolean requiresEndPoint() {
        return false;
    }

    @Override
    public void createTornado(TornadoParameters tornadoParameters) {
        String efRating = tornadoParameters.getEfRating();
        int efRatingValue = TornadoUtils.getEFRating(efRating);
        int randomSeed = tornadoParameters.getRandomSeed();

        // Each EF rating has a GEV associated with it
        GeneralizedExtremeValueDistribution gevWidth = new GeneralizedExtremeValueDistribution(widthLocation[efRatingValue],
            widthScale[efRatingValue], widthShape[efRatingValue], randomSeed);

        GeneralizedExtremeValueDistribution gevLength = new GeneralizedExtremeValueDistribution(lengthLocation[efRatingValue],
            lengthScale[efRatingValue], lengthShape[efRatingValue], randomSeed);

        tornadoWidths = new ArrayList<Double>();
        List<Double> tornadoLengths = new ArrayList<Double>();

        Random randomAngleGenerator = new Random(randomSeed);

        double meanAngle = TornadoUtils.computeMeanAngle(efRating);
        double stdDevAngle = TornadoUtils.computeAngleStandardDeviation(efRating);

        double rWidth = 0.0;
        double rLength = 0.0;
        double rAngle = 0.0;

        // put the calculated endpoint back to tornadoParameters
        List<Double> endLongitude = new ArrayList<Double>();
        List<Double> endLatitude = new ArrayList<Double>();

        // For each simulation, obtain the random width from the GEV
        for (int i = 0; i < tornadoParameters.getNumSimulations(); i++) {
            // CMN: There are cases when the random sample is less than 0. When the value is less than 0, we go to the next random
            // value. This also appears with another Java implementation, jdistlib, that is based on the R implementation of GEV.
            // Since I don't see this behavior with Matlab gevrnd(), they must be doing something to eliminates this case.
            rWidth = Math.min(maxWidth[efRatingValue], gevWidth.nextRandom());
            rLength = Math.min(maxLength[efRatingValue], gevLength.nextRandom());

            // Get a random angle following the normal distribution
            rAngle = (randomAngleGenerator.nextGaussian() * stdDevAngle) + meanAngle;

            double normalizedAngle = (rAngle + 360) % 360;

            while (rWidth < 0.0) {
                rWidth = gevWidth.nextRandom();
            }

            while (rLength < 0.0) {
                rLength = gevLength.nextRandom();
            }

            // Width in Meters
            tornadoWidths.add(rWidth * TornadoHazard.YARD_TO_METERS);
            // Length in Miles
            tornadoLengths.add(rLength);

            Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(), tornadoParameters.getStartLatitude());
            Coordinate endPtCoordinate = TornadoUtils.calculateDestination(startPtCoordinate, normalizedAngle, tornadoLengths.get(i));

            this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, tornadoWidths.get(i), efRating));

            // put the calculated endpoint back to tornadoParameters
            endLongitude.add(endPtCoordinate.x);
            endLatitude.add(endPtCoordinate.y);
        }

        // put the calculated endpoint back to tornadoParameters
        tornadoParameters.setEndLongitude(endLongitude);
        tornadoParameters.setEndLatitude(endLatitude);
    }


}
