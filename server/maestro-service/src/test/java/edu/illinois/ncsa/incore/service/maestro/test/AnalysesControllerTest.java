/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.maestro.controllers.AnalysesController;
import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalysesControllerTest extends CustomJerseyTest{

    public AnalysesControllerTest() { super();}

    @Override
    public ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application  = new MockApplication(AnalysesController.class);

        return application;
    }

    @Test
    public void testListAnalysis() {

       String output = target("/analyses").queryParam("full", "true").request().accept(MediaType.APPLICATION_JSON).get(String.class);
       JSONArray parsedObject = new JSONArray(output);

       assertEquals(4, parsedObject.length());
       JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

       assertNotEquals(0, firstObject.get("datasets").toString().length());
       assertNotEquals(0, firstObject.get("parameters").toString().length());
       assertNotEquals(0, firstObject.get("outputs").toString().length());

       output = target("/analyses").queryParam("full", "false").request().accept(MediaType.APPLICATION_JSON).get(String.class);
       parsedObject = new JSONArray(output);

       assertEquals(4, parsedObject.length());
       firstObject = new JSONObject(parsedObject.get(0).toString());
        assertFalse(firstObject.has("datasets"));
        assertFalse(firstObject.has("parameters"));
        assertFalse(firstObject.has("outputs"));

    }

    @Test
    public void testListAnalysis2(){
        String output = target("/analyses").queryParam("full", "true")
            .queryParam("category", "hazard").request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(1, parsedObject.length());
        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

        assertNotEquals(0, firstObject.get("datasets").toString().length());
        assertNotEquals(0, firstObject.get("parameters").toString().length());
        assertNotEquals(0, firstObject.get("outputs").toString().length());

        output = target("/analyses").queryParam("full", "false")
            .queryParam("category", "Building").request().accept(MediaType.APPLICATION_JSON).get(String.class);
        parsedObject = new JSONArray(output);

        assertEquals(2, parsedObject.length());
        firstObject = new JSONObject(parsedObject.get(0).toString());

        assertFalse(firstObject.has("datasets"));
        assertFalse(firstObject.has("parameters"));
        assertFalse(firstObject.has("outputs"));

    }

    @Test
    public void testListAnalysis3(){
        String output = target("/analyses").queryParam("full", "true")
            .queryParam("name", "Electric Power Network Damage, Tornado")
            .request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(1, parsedObject.length());
        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

        assertNotEquals(0, firstObject.get("datasets").toString().length());
        assertNotEquals(0, firstObject.get("parameters").toString().length());
        assertNotEquals(0, firstObject.get("outputs").toString().length());

        output = target("/analyses").queryParam("full", "false")
            .queryParam("name", "Electric Power Network Damage, Tornado")
            .request().accept(MediaType.APPLICATION_JSON).get(String.class);
        parsedObject = new JSONArray(output);

        assertEquals(1, parsedObject.length());
        firstObject = new JSONObject(parsedObject.get(0).toString());

        assertFalse(firstObject.has("datasets"));
        assertFalse(firstObject.has("parameters"));
        assertFalse(firstObject.has("outputs"));
    }

    @Test
    public void testListAnalysis4(){
        String output = target("/analyses").queryParam("full", "true")
            .queryParam("category", "Building")
            .queryParam("name", "Electric Power Network Damage, Tornado")
            .request().accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray parsedObject = new JSONArray(output);

        assertEquals(0, parsedObject.length());


        output = target("/analyses").queryParam("full", "false")
            .queryParam("category", "Building")
            .queryParam("name", "Building Structural Damage")
            .request().accept(MediaType.APPLICATION_JSON).get(String.class);
        parsedObject = new JSONArray(output);

        assertEquals(1, parsedObject.length());
        JSONObject firstObject = new JSONObject(parsedObject.get(0).toString());

        assertFalse(firstObject.has("datasets"));
        assertFalse(firstObject.has("parameters"));
        assertFalse(firstObject.has("outputs"));
    }


    @Test
    public void testGetAnalysisById() {

        String id = "5894ebee1a743941f0c4a4e8";
        Analysis output = target("/analyses/"+id).request().accept(MediaType.APPLICATION_JSON).get(Analysis.class);

        assertEquals(id,output.getId());
        assertEquals(2, output.getDatasets().size());
        assertEquals(1, output.getOutputs().size());
        assertEquals(1, output.getParameters().size());
        assertEquals("hazard", output.getCategory());
        assertNotNull(output.getDescription());
        assertNotNull(output.getUrl());
        assertNotNull(output.getName());
    }

    @Test
    public void testCreateNewAnalysis() throws IOException {

        URL jsonURL = this.getClass().getClassLoader().getResource("json/analysesPost.json");
        Analysis analysis = new ObjectMapper().readValue(jsonURL, Analysis.class);

        Analysis response = target("/analyses").request().accept(MediaType.APPLICATION_JSON).post(
            Entity.json(analysis), Analysis.class);

        assertNotNull(response.getId());
        assertEquals(2, response.getDatasets().size());
        assertEquals(1, response.getOutputs().size());
        assertEquals(3, response.getParameters().size());
        assertNotNull(response.getCategory());
        assertNotNull(response.getDescription());
        assertNotNull(response.getName());
    }
}
