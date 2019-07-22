/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert, Chen Wang
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.resilience.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.resilience.models.FragilitySet;
import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FragilityControllerTest extends CustomJerseyTest {
    public FragilityControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(FragilityController.class);

        return application;
    }

//    @Test
//    public void testGetFragilities() throws IOException {
//        Response response = target("/fragilities").request().get();
//
//        assertEquals(200, response.getStatus());
//        assertTrue(response.getLength() >= 0);
//    }
//
//    @Test
//    public void testSaveFragility() throws IOException {
//        // read payload data from json file
//        URL jsonURL = this.getClass().getClassLoader().getResource("fragility_request.json");
//        ObjectMapper mapper = new ObjectMapper();
//        FragilitySet request = mapper.readValue(jsonURL, FragilitySet.class);
//
//        // act
//        FragilitySet returned = target("/fragilities").request()
//            .header("X-Credential-Username", "tester")
//            .accept(MediaType.APPLICATION_JSON)
//            .post(Entity.json(request), FragilitySet.class);
//
//        // assert
//        assertNotNull(returned.getId());
//    }
}


