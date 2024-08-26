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

    private static final String DATA_SERVICE_URL = System.getenv("DATA_SERVICE_URL");
    private static final String HAZARD_SERVICE_URL = System.getenv("HAZARD_SERVICE_URL");
    private static final String DFR3_SERVICE_URL = System.getenv("DFR3_SERVICE_URL");
    private static final String EARTHQUAKE_URL = HAZARD_SERVICE_URL + "/hazard/api/earthquakes/";
    private static final String TORNADO_URL = HAZARD_SERVICE_URL + "/hazard/api/tornadoes/";
    private static final String HURRICANE_URL = HAZARD_SERVICE_URL + "/hazard/api/hurricanes/";
    private static final String FLOOD_URL = HAZARD_SERVICE_URL + "/hazard/api/floods/";
    private static final String TSUNAMI_URL = HAZARD_SERVICE_URL + "/hazard/api/tsunamis/";
    private static final String MAPPING_URL = DFR3_SERVICE_URL + "/dfr3/api/mappings/";
    private static final String DATA_URL = DATA_SERVICE_URL + "/data/api/datasets/";

    private static final Logger logger = Logger.getLogger(ServiceUtil.class);

    /**
     * Utility for request resource metadata based on input JSON.
     *
     * @param requestUrl the URL to send the request to
     * @param id the ID associated with the request
     * @param creator the creator's username
     * @param userGroups the user groups to be included in the request header
     * @return a JSONObject containing the resource information
     * @throws IOException if an I/O error occurs
     */
    public static JSONObject getResourceInfo(String requestUrl, String id, String creator, String userGroups)
        throws IOException {

        // Create a CloseableHttpClient to ensure resources are properly managed
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpGet httpGet = new HttpGet(requestUrl + id);
            httpGet.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
            httpGet.setHeader(HazardConstants.X_AUTH_USERGROUP, userGroups);

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseStr = responseHandler.handleResponse(response);

                return new JSONObject(responseStr);
            } catch (IOException e) {
                // Log the error and rethrow the exception
                logger.error("Error executing request to URL: " + requestUrl, e);
                throw e;
            }

        } catch (IOException e) {
            // Log the error and rethrow it
            logger.error("Failed to create HttpClient or process the request", e);
            throw e;
        }
    }

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

    private static ProjectResource updateResourceStatusAndSpaces(ProjectResource resource, String requestUrl, String creator,
                                                                 String userGroups) {
        try {
            JSONObject resourceInfo = ServiceUtil.getResourceInfo(requestUrl, resource.getId(), creator, userGroups);
            resource.setSpaces(resourceInfo.getJSONArray("spaces").toList().stream().map(Object::toString).collect(Collectors.toList()));

            int statusCode = resourceInfo.getInt("statusCode");
            switch (statusCode) {
                case 200:
                    resource.setStatus(ProjectResource.Status.EXISTING);
                    break;
                case 404:
                    resource.setStatus(ProjectResource.Status.DELETED);
                    break;
                case 403:
                    resource.setStatus(ProjectResource.Status.UNAUTHORIZED);
                    break;
                default:
                    resource.setStatus(ProjectResource.Status.UNKNOWN);
                    break;
            }

            // Set the type only if the resource is a DatasetResource and type hasn't been set
            if (resource instanceof DatasetResource &&
                ( ((DatasetResource) resource).getType() == null || ((DatasetResource) resource).getType().isEmpty())) {
                ((DatasetResource) resource).setType(resourceInfo.getString("dataType"));
            }

            return resource;
        } catch (IOException e) {
            throw new RuntimeException("Error fetching resource information for resource ID: " + resource.getId(), e);
        }
    }

    private static String constructHazardRequestUrl(String resourceType, String resourceId) {
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
