/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.project.utils;


import edu.illinois.ncsa.incore.common.HazardConstants;
import edu.illinois.ncsa.incore.service.project.models.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ServiceUtil {

    public static final String DATA_SERVICE_URL = System.getenv("DATA_SERVICE_URL");
    public static final String HAZARD_SERVICE_URL = System.getenv("HAZARD_SERVICE_URL");
    public static final String DFR3_SERVICE_URL = System.getenv("DFR3_SERVICE_URL");
    public static final String DATAWOLF_URL = System.getenv("DATAWOLF_URL");
    public static final String EARTHQUAKE_URL = HAZARD_SERVICE_URL + "/hazard/api/earthquakes/";
    public static final String TORNADO_URL = HAZARD_SERVICE_URL + "/hazard/api/tornadoes/";
    public static final String HURRICANE_URL = HAZARD_SERVICE_URL + "/hazard/api/hurricanes/";
    public static final String FLOOD_URL = HAZARD_SERVICE_URL + "/hazard/api/floods/";
    public static final String TSUNAMI_URL = HAZARD_SERVICE_URL + "/hazard/api/tsunamis/";
    public static final String MAPPING_URL = DFR3_SERVICE_URL + "/dfr3/api/mappings/";
    public static final String DATA_URL = DATA_SERVICE_URL + "/data/api/datasets/";
    public static final String DATAWOLF_WORKFLOW_URL = System.getenv("DATAWOLF_URL") + "/workflows/";
    public static final String DATAWOLF_EXECUTION_URL = System.getenv("DATAWOLF_URL") + "/executions/";

    private static final Logger logger = Logger.getLogger(ServiceUtil.class);

    public static Project processProjectResources(Project project, String creator, String userGroups) {
        // Process hazards
        List<HazardResource> updatedHazards = new ArrayList<>();
        for (HazardResource hazard : project.getHazards()) {
            String requestUrl = constructHazardRequestUrl(hazard.getType().toString(), hazard.getId());
            ProjectResource updatedHazard = updateResourceStatusAndSpaces(hazard, requestUrl, creator, userGroups);
            updatedHazards.add((HazardResource) updatedHazard);
        }
        project.setHazards(updatedHazards);

        // Process dfr3Mappings
        List<DFR3MappingResource> updatedMappings = new ArrayList<>();
        for (DFR3MappingResource mapping : project.getDfr3Mappings()) {
            String requestUrl = MAPPING_URL + mapping.getId();
            ProjectResource updatedMaping = updateResourceStatusAndSpaces(mapping, requestUrl, creator, userGroups);
            updatedMappings.add((DFR3MappingResource) updatedMaping);
        }
        project.setDfr3Mappings(updatedMappings);

        // Process datasets
        List<DatasetResource> updatedDatasets = new ArrayList<>();
        for (DatasetResource dataset : project.getDatasets()) {
            String requestUrl = DATA_URL + dataset.getId();
            ProjectResource updatedDataset = updateResourceStatusAndSpaces(dataset, requestUrl, creator, userGroups);
            updatedDatasets.add((DatasetResource) updatedDataset);
        }
        project.setDatasets(updatedDatasets);

        // TODO missing workflow

        return project;
    }

    public static ProjectResource updateResourceStatusAndSpaces(ProjectResource resource, String requestUrl, String creator, String userGroups) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpGet httpGet = new HttpGet(requestUrl);
            httpGet.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
            httpGet.setHeader(HazardConstants.X_AUTH_USERGROUP, userGroups);

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                ProjectResource.Status status;
                switch (statusCode) {
                    case 200:
                        // set status
                        status = ProjectResource.Status.EXISTING;

                        // set space
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        String responseStr = responseHandler.handleResponse(response);
                        JSONObject resourceInfo = new JSONObject(responseStr);
                        resource.setSpaces(resourceInfo.getJSONArray("spaces").toList().stream().map(Object::toString).collect(Collectors.toList()));

                        // set dataset type
                        if (resource instanceof DatasetResource) {
                            DatasetResource datasetResource = (DatasetResource) resource;
                            if (datasetResource.getType() == null || datasetResource.getType().isEmpty()) {
                                datasetResource.setType(resourceInfo.getString("dataType"));
                            }
                        }

                        break;
                    case 404:
                        status = ProjectResource.Status.DELETED;
                        break;
                    case 403:
                        status = ProjectResource.Status.UNAUTHORIZED;
                        break;
                    default:
                        status = ProjectResource.Status.UNKNOWN;
                        break;
                }
                resource.setStatus(status);

                return resource;
            } catch (IOException e) {
                logger.error("Error executing request to URL: " + requestUrl + resource.getId(), e);
                return resource;
            }

        } catch (IOException e) {
            logger.error("Failed to create HttpClient or process the request", e);
            return resource;
        }
    }

    public static String constructHazardRequestUrl(String resourceType, String resourceId) {
        switch (resourceType) {
            case "earthquake":
                return EARTHQUAKE_URL + resourceId;
            case "tornado":
                return TORNADO_URL + resourceId;
            case "hurricane":
                return HURRICANE_URL + resourceId;
            case "flood":
                return FLOOD_URL + resourceId;
            case "tsunami":
                return TSUNAMI_URL + resourceId;
            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        }
    }

}
