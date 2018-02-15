/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.service.hazard.CustomJerseyTest;
import edu.illinois.ncsa.incore.service.hazard.MockApplication;
import edu.illinois.ncsa.incore.service.hazard.controllers.TornadoController;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeanWidthTornadoTest extends CustomJerseyTest {

    private GeometryFactory factory = new GeometryFactory();

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        forceSet(TestProperties.CONTAINER_PORT, "0");

        return new MockApplication(TornadoController.class);
    }

    @Test
    public void testMeanWidthTornadoValues() throws Exception {
        String id = "5a7385645a52284c6a7d273a";
        ScenarioTornado tornado = target("tornadoes/" + id).request().accept(MediaType.APPLICATION_JSON).get(ScenarioTornado.class);

        double siteLong = -97.4788;
        double siteLat = 35.2265;
        Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));
        String demandUnits = "mph";
        int simulation = 0;

        WindHazardResult result = TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation);
        assertTrue(result.getHazardValue() >= 65.0);
        assertTrue(result.getHazardValue() < 86.0);
    }

    @Test
    public void testMeanWidthTornadoValuesMetersPerSecond() throws Exception {
        String id = "5a7385645a52284c6a7d273a";
        ScenarioTornado tornado = target("tornadoes/" + id).request().accept(MediaType.APPLICATION_JSON).get(ScenarioTornado.class);

        double siteLong = -97.4788;
        double siteLat = 35.2265;
        Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));
        String demandUnits = "mps";
        int simulation = 0;

        WindHazardResult result = TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation);
        assertTrue(result.getHazardValue() >= 29.0576);
        assertTrue(result.getHazardValue() < 38.44544);
    }
}
