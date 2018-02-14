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
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.TornadoHazard;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;

import java.util.List;

public class TornadoCalc {
    /**
     * Computes the wind hazard for the given location
     *
     * @param scenarioTornado - Tornado event
     * @param localSite       - location to compute hazard
     * @param demandUnits     - hazard units
     * @param simulation      - simulation number
     * @return wind hazard at the specified location
     * @throws Exception
     */
    public static WindHazardResult getWindHazardAtSite(ScenarioTornado scenarioTornado, Point localSite, String demandUnits, int simulation) throws Exception {
        EFBox simulationEfBoxWidths = scenarioTornado.getEfBoxes().get(simulation);
        List<Double> efBoxWidths = simulationEfBoxWidths.getEfBoxWidths();

        LineString tornadoPath = TornadoUtils.createTornadoPath(scenarioTornado.getTornadoParameters(), simulation);
        List<Geometry> efBoxPolygons = TornadoUtils.createTornadoGeometry(scenarioTornado.getTornadoParameters(), efBoxWidths, tornadoPath);

        double windHazard = Tornado.calculateWindSpeed(localSite, tornadoPath, efBoxPolygons, efBoxWidths, scenarioTornado.getTornadoParameters());
        if (demandUnits.equalsIgnoreCase(TornadoHazard.WIND_MPS)) {
            // TODO this was previously handled by UnitUtil in version 1.0
            windHazard *= getConversionFactor(demandUnits);
        }

        return new WindHazardResult(demandUnits, windHazard);
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
}
