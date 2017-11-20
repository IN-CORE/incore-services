/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;

import java.util.List;

public class TornadoCalc {
    public static WindHazardResult getWindHazardAtSite(ScenarioTornado scenarioTornado, Tornado model, Point localSite, String demandUnits, int simulation) throws Exception {

        EFBox simulationEfBoxWidths = scenarioTornado.getEfBoxes().get(simulation);
        List<Double> efBoxWidths = simulationEfBoxWidths.getEfBoxWidths();

        LineString tornadoPath = TornadoUtils.createTornadoPath(scenarioTornado.getTornadoParameters(), simulation);
        List<Geometry> efBoxPolygons = TornadoUtils.createTornadoGeometry(scenarioTornado.getTornadoParameters(), efBoxWidths, tornadoPath);

        // We could make this a Utility method; however, by using the implemented model. users could override how the model generates wind speed
        // TODO use demand units to convert mph to requested demand type

        return new WindHazardResult(demandUnits, model.calculateWindSpeed(localSite, tornadoPath, efBoxPolygons, efBoxWidths, scenarioTornado.getTornadoParameters()));
    }
}
