/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.space.controllers;

import mocks.MockApplication;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpaceControllerTest extends CustomJerseyTest {

    public SpaceControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(SpaceController.class);

        return application;
    }

//    @Test
//    public void testGetSpaceList() {
//        String output = target("/spaces").request().header("X-Credential-Username", "test").accept(MediaType.APPLICATION_JSON).get
//        (String.class);
//        JSONArray parsedObject = new JSONArray(output);
//
//        for(int i = 0; i < parsedObject.length(); i++){
//            JSONObject space = new JSONObject(parsedObject.get(i).toString());
//            assertNotNull(space.get("id").toString());
//            assertNotNull(space.get("metadata").toString());
//            assertNotEquals(223, space.get("members").toString().length());
//        }
//    }

    @Test
    public void testIngestSpace() throws IOException {
        URL jsonURL = this.getClass().getClassLoader().getResource("json/spacePost.json");
        InputStream inputStream = jsonURL.openStream();
        String jsontext = IOUtils.toString(inputStream);
        final FormDataMultiPart multiPartEntity = new FormDataMultiPart().field("space", jsontext);

        Response response = target("/spaces").register(MultiPartWriter.class).request()
            .header("x-auth-userinfo",
                "{\"sub\":\"\",\"email_verified\":true,\"name\":\"Incore Tester Tester\",\"preferred_username\":\"incrtest\"," +
                    "\"given_name\":\"Incore Tester\",\"family_name\":\"Tester\",\"email\":\"tolbert+incoretester@illinois.edu\"}")
            .header("x-auth-usergroup", "{\"groups\": [\"incore_user\", \"incore_coe\"]}")
            .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        String output = response.readEntity(String.class);
        JSONObject parsedObject = new JSONObject(output);

        assertNotNull(parsedObject.get("id").toString());
        assertNotNull(parsedObject.get("metadata").toString());
    }

    @Test
    public void testGrantPrivileges() throws IOException {
        String id = "5a25853ebeefaa1a583212b7";

        URL jsonURL = this.getClass().getClassLoader().getResource("json/privileges.json");
        InputStream inputStream = jsonURL.openStream();
        String jsontext = IOUtils.toString(inputStream);
        final FormDataMultiPart multiPartEntity = new FormDataMultiPart().field("grant", jsontext);

        Response response = target("/spaces/" + id + "/grant").register(MultiPartWriter.class).request()
            .header("x-auth-userinfo",
                "{\"sub\":\"\",\"email_verified\":true,\"name\":\"Incore Tester Tester\",\"preferred_username\":\"incrtest\"," +
                    "\"given_name\":\"Incore Tester\",\"family_name\":\"Tester\",\"email\":\"tolbert+incoretester@illinois.edu\"}")
            .header("x-auth-usergroup", "{\"groups\": [\"incore_user\", \"incore_coe\"]}")
            .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        String output = response.readEntity(String.class);
        JSONObject parsedObject = new JSONObject(output);

        assertNotNull(parsedObject.get("id").toString());
        assertNotNull(parsedObject.get("privileges").toString());
    }

}
