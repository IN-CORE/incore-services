/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.vividsolutions.jts.geom.Coordinate;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;

import java.util.ArrayList;
import java.util.List;

public class MeanWidthTornado extends BaseTornado {

    public void createTornado(TornadoParameters tornadoParameters) {
        String efRating = tornadoParameters.getEfRating();
        tornadoWidths = new ArrayList<Double>();

        double tornadoWidth = TornadoUtils.computeMeanWidth(efRating);
        this.tornadoWidths.add(tornadoWidth);

        Coordinate startPtCoordinate = new Coordinate(tornadoParameters.getStartLongitude(), tornadoParameters.getStartLatitude());
        Coordinate endPtCoordinate = new Coordinate(tornadoParameters.getEndLongitude().get(0), tornadoParameters.getEndLatitude().get(0));

        this.efBoxes.add(this.computeTornadoEFBoxWidths(startPtCoordinate, endPtCoordinate, tornadoWidth, efRating));
    }

    @Override
    public boolean requiresEndPoint() {
        return true;
    }

}
