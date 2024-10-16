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

import edu.illinois.ncsa.incore.service.hazard.CustomJerseyTest;
import edu.illinois.ncsa.incore.service.hazard.MockApplication;
import edu.illinois.ncsa.incore.service.hazard.controllers.TornadoController;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.GeometryFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeanWidthTornadoTest extends CustomJerseyTest {

    private final GeometryFactory factory = new GeometryFactory();

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new MockApplication(TornadoController.class);
    }
//
//    @Test
//    public void testMeanWidthTornadoValues() throws Exception {
//        String id = "5a7385645a52284c6a7d273a";
//        Tornado tornado = target("tornadoes/" + id).request().accept(MediaType.APPLICATION_JSON).get(Tornado.class);
//
//        double siteLong = -97.4788;
//        double siteLat = 35.2265;
//        Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));
//        String demandUnits = "mph";
//        int simulation = 0;
//
//        WindHazardResult result = TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation, "incrtest");
//        assertTrue(result.getHazardValue() >= 65.0);
//        assertTrue(result.getHazardValue() < 86.0);
//    }

//    @Test
//    public void testMeanWidthTornadoValuesMetersPerSecond() throws Exception {
//        String id = "5a7385645a52284c6a7d273a";
//        Tornado tornado = target("tornadoes/" + id).request().accept(MediaType.APPLICATION_JSON).get(Tornado.class);
//
//        double siteLong = -97.4788;
//        double siteLat = 35.2265;
//        Point localSite = factory.createPoint(new Coordinate(siteLong, siteLat));
//        String demandUnits = "mps";
//        int simulation = 0;
//
//        WindHazardResult result = TornadoCalc.getWindHazardAtSite(tornado, localSite, demandUnits, simulation, "incrtest");
//        assertTrue(result.getHazardValue() >= 29.0576);
//        assertTrue(result.getHazardValue() < 38.44544);
//    }
}
