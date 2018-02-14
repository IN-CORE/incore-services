/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
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
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TornadoControllerTest extends CustomJerseyTest {

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        MockApplication application = new MockApplication(TornadoController.class);

        return application;
    }

    @Test
    public void testListTornadoes() {
        String output = target("tornadoes").request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(2, parsedObject.length());
    }
}
