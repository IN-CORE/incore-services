package edu.illinois.ncsa.incore.service.data.controllers;

import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatatypeControllerTest extends CustomJerseyTest {
    public DatatypeControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(DatatypeController.class);

        return application;
    }

    @Test
    public void testGetDatatypes() {
        String output = target("/datatypes").request().
            header("x-auth-userinfo", "{\"preferred_username\": \"test\"}").accept(MediaType.APPLICATION_JSON)
            .header("x-auth-usergroup", "{\"groups\": [\"incore_user\", \"incore_coe\"]}")
            .get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(2, parsedObject.length());
        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

        assertEquals("incore:testTypeOne", firstObject.get("dataType").toString());
    }


}
