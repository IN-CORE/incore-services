/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.opengamma.analytics.math.statistics.distribution.GeneralizedExtremeValueDistribution;
import com.vividsolutions.jts.geom.Coordinate;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;

import java.util.ArrayList;

public class TornadoRandomWidth extends Tornado {

    private static double[] maxWidth = { 2500.0, 3000.0, 3520.0, 3872.0, 4400.0, 3000.0 }; // Maximum width for EF0 - EF5

    private static double[] shape = { 0.4228, 0.595078, 0.657065, 0.515384, 0.264031, 0.185012 }; // GEV shape parameter, EF0 - EF5
    private static double[] scale = { 16.5948, 37.1997, 81.1465, 198.266, 364.877, 421.403 }; // GEV shape parameter, EF0 - EF5
    private static double[] location = { 20.4790, 40.5151, 77.711, 197.736, 383.485, 522.085 }; // GEV shape parameter, EF0 - EF5

    @Override
    public boolean requiresEndPoint() {
        return true;
    }

    @Override
    public void createTornado(TornadoParameters tornadoParameters) {
        String efRating = tornadoParameters.getEfRating();
        int ef = TornadoUtils.getEFRating(efRating);

        // Each EF rating has a GEV associated with it
        GeneralizedExtremeValueDistribution gev = getGEV(ef, tornadoParameters.getRandomSeed());

        tornadoWidths = new ArrayList<Double>();
        double randomWidth = 0.0;

        Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(), tornadoParameters.getStartLatitude());
        Coordinate endPtCoordinate = new Coordinate(tornadoParameters.getEndLongitude().get(0), tornadoParameters.getEndLatitude().get(0));

        for(int i = 0; i < tornadoParameters.getNumSimulations(); i++) {
           randomWidth = Math.min(maxWidth[ef], gev.nextRandom());

            // CMN: There are cases when the random sample is less than 0. When the value is less than 0, we go to the next random
            // value. This also appears with another Java implementation, jdistlib, that is based on the R implementation of GEV.
            // Since I don't see this behavior with Matlab gevrnd(), they must be handling this case internally.
           while(randomWidth < 0.0) {
               randomWidth = Math.min(maxWidth[ef], gev.nextRandom());
           }

           randomWidth *= TornadoHazard.YARD_TO_METERS;
           tornadoWidths.add(randomWidth);
           this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, randomWidth, efRating));
        }

    }

    private GeneralizedExtremeValueDistribution getGEV(int ef, int randomSeed)
    {
        if (ef > 5 || ef < 0) {
            return null;
        }

        GeneralizedExtremeValueDistribution gev = new GeneralizedExtremeValueDistribution(location[ef], scale[ef], shape[ef], randomSeed);

        return gev;
    }
}
