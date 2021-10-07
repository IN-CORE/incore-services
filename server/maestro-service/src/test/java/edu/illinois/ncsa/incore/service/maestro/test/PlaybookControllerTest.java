/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.test;

import edu.illinois.ncsa.incore.service.maestro.controllers.PlaybookController;
import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaybookControllerTest extends CustomJerseyTest {

    public PlaybookControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(PlaybookController.class);

        return application;
    }

//    TODO test doesn't work yet
//    @Test
//    public void testGetPlaybooks() {
//
//        String output = target("/playbooks").request().accept(MediaType.APPLICATION_JSON).get(String.class);
//        JSONArray parsedObject = new JSONArray(output);
//
//        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());
//
//        assertTrue(firstObject.has("id"));
//        assertTrue(firstObject.has("name"));
//        assertTrue(firstObject.has("description"));
//        assertNotEquals(3, firstObject.get("steps").toString().length());
//
//    }
//
//
//    @Test
//    public void testGetPlaybookById() {
//
//        String id = "615f0d772d387e00e68bef2d";
//        String output = target("/playbooks/" + id).request().accept(MediaType.APPLICATION_JSON).get(String.class);
//
//        JSONArray parsedObject = new JSONArray(output);
//
//        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());
//
//        assertTrue(firstObject.has("id"));
//        assertTrue(firstObject.has("name"));
//        assertTrue(firstObject.has("description"));
//        assertNotEquals(3, firstObject.get("steps").toString().length());
//
//    }
}
