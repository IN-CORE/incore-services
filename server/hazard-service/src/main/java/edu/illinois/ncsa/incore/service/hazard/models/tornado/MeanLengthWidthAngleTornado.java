/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro, Chen Wang(NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.vividsolutions.jts.geom.Coordinate;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;

import java.util.ArrayList;
import java.util.List;

public class MeanLengthWidthAngleTornado extends BaseTornado{

    public void createTornado(TornadoParameters tornadoParameters) {

        String efRating = tornadoParameters.getEfRating();

        double meanDistance = TornadoUtils.computeMeanLength(efRating);
        double meanWidth = TornadoUtils.computeMeanWidth(efRating);
        double meanAngle = TornadoUtils.computeMeanAngle(efRating);

        tornadoWidths = new ArrayList<Double>();
        this.tornadoWidths.add(meanWidth);

        Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(), tornadoParameters.getStartLatitude());
        Coordinate endPtCoordinate = TornadoUtils.calculateDestination(startPtCoordinate, meanAngle, meanDistance);

        this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, meanWidth, efRating));

        List<Double> endLongitude = new ArrayList<Double>();
        List<Double> endLatitude = new ArrayList<Double>();
        endLongitude.add(endPtCoordinate.x);
        endLatitude.add(endPtCoordinate.y);

        tornadoParameters.setEndLongitude(endLongitude);
        tornadoParameters.setEndLatitude(endLatitude);
    }

    @Override
    public boolean requiresEndPoint() {
        return true;
    }
}
