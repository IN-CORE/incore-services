/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import edu.illinois.ncsa.incore.service.hazard.CustomJerseyTest;
import edu.illinois.ncsa.incore.service.hazard.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TsunamiControllerTest extends CustomJerseyTest {

    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(TsunamiController.class);

        return application;
    }

//    @Test
//    public void testGetTsunamis() throws Exception {
//        String output = target("tsunamis").request().accept(MediaType.APPLICATION_JSON).get(String.class);
//        JSONArray parsedObject = new JSONArray(output);
//        assertEquals(1, parsedObject.length());
//    }
}
