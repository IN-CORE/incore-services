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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TornadoControllerTest extends CustomJerseyTest {

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        forceSet(TestProperties.CONTAINER_PORT, "0");

        return new MockApplication(TornadoController.class);
    }

//    @Test
//    public void testListTornadoes() {
//        String output = target("tornadoes").request().accept(MediaType.APPLICATION_JSON).get(String.class);
//        JSONArray parsedObject = new JSONArray(output);
//
//        assertEquals(2, parsedObject.length());
//    }
}
