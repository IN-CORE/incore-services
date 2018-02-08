/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.controllers;

import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpaceControllerTest extends CustomJerseyTest{

    public SpaceControllerTest() { super();}

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application  = new MockApplication(SpaceController.class);

        return application;
    }

    @Test
    public void testGetSpaceList() {
        String output = target("/spaces").request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(2, parsedObject.length());

        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());
        assertNotNull(firstObject.get("id").toString());
        assertNotNull(firstObject.get("name").toString());
        assertNotEquals(223, firstObject.get("datasetIds").toString().length());
    }
}