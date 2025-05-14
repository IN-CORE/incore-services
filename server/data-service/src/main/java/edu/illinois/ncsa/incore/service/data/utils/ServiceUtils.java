/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.utils;

import com.opencsv.exceptions.CsvValidationException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import jakarta.ws.rs.core.Response;
import java.io.IOException;

import com.opencsv.CSVReader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.*;

public class ServiceUtils {
    public static final String HAZARD_STATUS_ENDPOINT = "hazard/api/status/usage";
    public static final String X_AUTH_USERINFO = "x-auth-userinfo";

    /**
     * utility for creating data endpoint string
     *
     * @return
     */
    public static String createHazardEndpoint() {
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("HAZARD_SERVICE_URL");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
    }

    /**
     * utility for calling hazard usage end point
     *
     * @param userInfo
     * @return
     * @throws IOException
     */
    public static String getHazardUsage(String userInfo) throws IOException {
        // parse username
        String userName = DataJsonUtils.parseUserName(userInfo);

        if (userName == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the username");
        }

        // access earthquake usage enpoint
        String hazardEndpoint = ServiceUtils.createHazardEndpoint();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = "";
        requestUrl = hazardEndpoint + HAZARD_STATUS_ENDPOINT;

        HttpGet httpGet = new HttpGet(requestUrl);
        httpGet.setHeader(X_AUTH_USERINFO, "{\"preferred_username\": \"" + userName + "\"}");

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        response = httpclient.execute(httpGet);
        responseStr = responseHandler.handleResponse(response);

        return responseStr;
    }

    public static boolean validateJoinCompatibility(Dataset child, Dataset parent, IRepository repository) {
        File childCsv;
        File parentCsv;

        try {
            childCsv = FileUtils.loadFileFromService(child, repository, false, "");
            parentCsv = FileUtils.loadFileFromService(parent, repository, false, "");
        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to load dataset files.");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Set<String> childGuids = extractGuid(childCsv);
        Set<String> parentGuids = extractGuid(parentCsv);

        // Check for intersection
        Set<String> intersection = new HashSet<>(childGuids);
        intersection.retainAll(parentGuids);

        if (intersection.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                "No matching GUIDs found between source and parent datasets.");
        }

        return true;
    }

    private static Set<String> extractGuid(File csvFile) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "CSV file does not contain a header row.");
            }

            // Find GUID column index (case-insensitive)
            int guidIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase("guid")) {
                    guidIndex = i;
                    break;
                }
            }

            if (guidIndex == -1) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "CSV file does not contain a 'GUID' column.");
            }

            // Collect GUIDs from the column
            Set<String> guidSet = new HashSet<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (guidIndex < row.length && row[guidIndex] != null && !row[guidIndex].isEmpty()) {
                    guidSet.add(row[guidIndex].trim());
                }
            }

            if (guidSet.isEmpty()) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "GUID column is empty or missing values.");
            }

            return guidSet;

        } catch (IOException | CsvValidationException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error reading CSV file.");
        }
    }
}
