/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro, Chen Wang(NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAngleTornado extends BaseTornado {

    @Override
    public boolean requiresEndPoint() {
        return true;
    }

    @Override
    public void createTornado(TornadoParameters tornadoParameters) {
        // generate random
        int randomSeed = tornadoParameters.getRandomSeed();
        Random randomAngleGenerator = new Random(randomSeed);

        String efRating = tornadoParameters.getEfRating();

        double meanWidth = TornadoUtils.computeMeanWidth(efRating);
        tornadoWidths = new ArrayList<Double>();
        tornadoWidths.add(meanWidth);

        double meanDistance = TornadoUtils.computeMeanLength(efRating);
        double meanAngle = TornadoUtils.computeMeanAngle(efRating);
        double stdDevAngle = TornadoUtils.computeAngleStandardDeviation(efRating);

        double randomAngle = 0;

        // put the caclulated endpoint back to tornadoParameters
        List<Double> endLongitude = new ArrayList<Double>();
        List<Double> endLatitude = new ArrayList<Double>();

        for (int i = 0; i < tornadoParameters.getNumSimulations(); i++) {
            // Get a random angle following the normal distribution
            randomAngle = (randomAngleGenerator.nextGaussian() * stdDevAngle) + meanAngle;
            double normalizedAngle = (randomAngle + 360) % 360;

            Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(),
                tornadoParameters.getStartLatitude());
            Coordinate endPtCoordinate = TornadoUtils.calculateDestination(startPtCoordinate, normalizedAngle, meanDistance);

            this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, meanWidth, efRating));

            // put the caclulated endpoint back to tornadoParameters
            endLongitude.add(endPtCoordinate.x);
            endLatitude.add(endPtCoordinate.y);
            tornadoParameters.setEndLongitude(endLongitude);
            tornadoParameters.setEndLatitude(endLatitude);
        }
    }

}
