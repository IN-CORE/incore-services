/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.fragility.controllers.FragilityController;
import edu.illinois.ncsa.incore.service.fragility.models.MappingResponse;
import edu.illinois.ncsa.incore.service.fragility.models.MappingRequest;
import edu.illinois.ncsa.incore.service.fragility.models.MappingSubject;
import edu.illinois.ncsa.incore.service.fragility.models.SchemaType;
import mocks.MockApplication;
import org.geojson.FeatureCollection;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FragilityControllerTest extends CustomJerseyTest {
    public FragilityControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(FragilityController.class);

        return application;
    }
    @Test
    public void testMappingFunctionFeature() throws IOException {
        // arrange
        URL jsonURL = this.getClass().getClassLoader().getResource("json/mapping_request.json");

        MappingRequest request = new ObjectMapper()
                                    .configure(MapperFeature.USE_ANNOTATIONS, true)
                                    .readValue(jsonURL, MappingRequest.class);

        // act
        MappingResponse response = target("/fragilities/map").request()
                                                           .accept(MediaType.APPLICATION_JSON)
                                                           .post(Entity.json(request), MappingResponse.class);

        // assert
        assertTrue(response.getFragilitySets().containsKey("NSDS_PFM_MTB_UL_475_W1_4"));
    }

    @Test
    public void testMappingFunctionFeatureCollection() throws IOException {
        // arrange
        URL jsonURL = this.getClass().getClassLoader().getResource("json/inventory.json");

        FeatureCollection featureCollection = new ObjectMapper().readValue(jsonURL, FeatureCollection.class);

        MappingSubject subject = new MappingSubject(SchemaType.Building, featureCollection);

        MappingRequest request = new MappingRequest(subject);

        // act
        MappingResponse response = target("/fragilities/map").request()
                                                            .accept(MediaType.APPLICATION_JSON)
                                                            .post(Entity.json(request), MappingResponse.class);

        // assert
        assertTrue(response.getFragilitySets().containsKey("NSDS_PFM_MTB_UL_475_W1_4"));
        assertEquals(response.getFragilityToInventoryMapping().get("all_bldgs_ver5_WGS1984.1"), "NSDS_PFM_MTB_UL_475_W1_4");
        assertEquals(response.getFragilityToInventoryMapping().get("all_bldgs_ver5_WGS1984.2"), "NSDS_PFM_MTB_UL_475_W1_4");
    }
}
