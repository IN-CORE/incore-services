/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert, Chen Wang
 */

package edu.illinois.ncsa.incore.service.fragility.controllers;

import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;

import com.fasterxml.jackson.databind.ObjectMapper;

import mocks.MockApplication;
import org.geojson.FeatureCollection;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        MockApplication application = new MockApplication(FragilityController.class, MappingController.class);

        return application;
    }

    // test GET /fragilities
    @Test
    public void testGetFragilities() throws IOException {
        Response response = target("/fragilities").request()
                                .get();
        assertEquals(200, response.getStatus());
        assertTrue( response.getLength() >= 0 );
    }


    // test POST /fragilities
    @Test
    public void testSaveFragility() throws IOException {
        
        // read payload data from json file
        URL jsonURL = this.getClass().getClassLoader().getResource("json/fragility_request.json");
        ObjectMapper mapper = new ObjectMapper();
        FragilitySet request = mapper.readValue(jsonURL, FragilitySet.class);

        // act
        Response response = target("/fragilities").request()
                                .accept(MediaType.APPLICATION_JSON)
                                .post(Entity.json(request), Response.class);


        // assert endpoint works
        assertEquals(200, response.getStatus());

        // succesfully ingest the data to the database will generate new id;
        FragilitySet returned = response.readEntity(FragilitySet.class);
        assertNotNull(returned.getId());
    }


}


