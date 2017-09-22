package edu.illinois.ncsa.incore.services.maestro.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.maestro.controllers.AnalysesController;
import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
import mocks.MockApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalysesControllerTest extends CustomJerseyTest{

    public AnalysesControllerTest() { super();}

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application  = new MockApplication(AnalysesController.class);

        return application;
    }

    @Test
    public void testListAnalysis() {

       String output = target("/analysis/all").request().accept(MediaType.APPLICATION_JSON).get(String.class);
       JSONArray parsedObject = new JSONArray(output);

       assertEquals(4, parsedObject.length());
       JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

       assertNotEquals(0, firstObject.get("inputs").toString().length());
       assertNotEquals(0, firstObject.get("outputs").toString().length());
    }

    @Test
    public void testListAnalysisMetadata() {

        String output = target("/analysis").request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(4, parsedObject.length());
    }

    @Test
    public void testGetAnalysisById() {

        String id = "5894ebee1a743941f0c4a4e8";
        Analysis output = target("/analysis/"+id).request().accept(MediaType.APPLICATION_JSON).get(Analysis.class);

        assertEquals(id,output.getId());
        assertEquals(3, output.getInputs().size());
        assertEquals(1, output.getOutputs().size());
        assertEquals("hazard", output.getCategory());
        assertNotNull(output.getDescription());
        assertNotNull(output.getUrl());
        assertNotNull(output.getName());
    }

    @Test
    public void testCreateNewAnalysis() throws IOException {

        URL jsonURL = this.getClass().getClassLoader().getResource("json/analysisPost.json");
        Analysis analysis = new ObjectMapper().readValue(jsonURL, Analysis.class);

        Analysis response = target("/analysis").request().accept(MediaType.APPLICATION_JSON).post(
            Entity.json(analysis), Analysis.class);

        assertNotNull(response.getId());
        assertEquals(3, response.getInputs().size());
        assertEquals(1, response.getOutputs().size());
        assertNotNull(response.getCategory());
        assertNotNull(response.getDescription());
        assertNotNull(response.getName());
    }
}
