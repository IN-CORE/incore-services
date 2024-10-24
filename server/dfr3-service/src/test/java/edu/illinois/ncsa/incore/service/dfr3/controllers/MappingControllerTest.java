/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.dfr3.controllers;

import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MappingControllerTest extends CustomJerseyTest {
    public MappingControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(MappingController.class);

        return application;
    }

//    @Test
//    public void testMappingFunctionFeature() throws IOException {
//        // arrange
//        URL jsonURL = this.getClass().getClassLoader().getResource("mapping_request.json");
//
//        MappingRequest request = new ObjectMapper()
//            .configure(MapperFeature.USE_ANNOTATIONS, true)
//            .readValue(jsonURL, MappingRequest.class);
//
//        // act
//        MappingResponse response = target("/mappings/fakemappingid/matched").request()
//                                                                          .accept(MediaType.APPLICATION_JSON)
//                                                                          .post(Entity.json(request), MappingResponse.class);
//
//        // assert
//        // NSDS_PFM_MTB_UL_475_W1_4
//        String fragilityKey = "5aa9858b949f232724db42e0";
//        assertTrue(response.getFragilitySets().containsKey(fragilityKey));
//    }
//
//    @Test
//    public void testMappingFunctionFeatureCollection() throws IOException {
//        // arrange
//        URL jsonURL = this.getClass().getClassLoader().getResource("inventory.json");
//
//        FeatureCollection featureCollection = new ObjectMapper().readValue(jsonURL, FeatureCollection.class);
//
//        MappingSubject subject = new MappingSubject(SchemaType.Building, featureCollection);
//
//        MappingRequest request = new MappingRequest(subject);
//
//        // act
//        MappingResponse response = target("/mappings/fakemappingid/matched").request()
//                                                                          .accept(MediaType.APPLICATION_JSON)
//                                                                          .post(Entity.json(request), MappingResponse.class);
//
//        // assert
//        // NSDS_PFM_MTB_UL_475_W1_4
//        String fragilityKey = "5aa9858b949f232724db42e0";
//        assertTrue(response.getFragilitySets().containsKey(fragilityKey));
//        assertEquals(response.getFragilityToInventoryMapping().get("all_bldgs_ver5_WGS1984.1"), fragilityKey);
//        assertEquals(response.getFragilityToInventoryMapping().get("all_bldgs_ver5_WGS1984.2"), fragilityKey);
//    }
}
