/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Yong Wook Kim (NCSA) - initial API and implementation
 ********************************************************************************/

package edu.illinois.ncsa.incore.service.data.controllers;

import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileControllerTest extends CustomJerseyTest {
    public FileControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(FileController.class);

        return application;
    }

    @Test
    public void testGetFileDescriptorList() {
//        String output = target("/files").request().accept(MediaType.APPLICATION_JSON).get(String.class);
//        JSONArray parsedObject = new JSONArray(output);
//
//        assertEquals(4, parsedObject.length());
//        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());
//
//        assertNotNull(firstObject.get("id").toString());
    }

    @Test
    public void testGetFileDescriptorById() {
//        String id = "5a207b29beefa40740e87c96";
//        FileDescriptor output = target("/files/" + id).request().accept(MediaType.APPLICATION_JSON).get(FileDescriptor.class);
//        assertNotNull(output.getId());
    }
}
