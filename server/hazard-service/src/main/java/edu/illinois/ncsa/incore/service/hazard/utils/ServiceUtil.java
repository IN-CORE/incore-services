/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.utils;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.HazardDataset;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ServiceUtil {
    /**
     * Utility for the hazard service to save files to the dataset repository
     *
     * @param datasetObject
     * @param creator
     * @param files
     * @return
     * @throws IOException
     */
    public static String createDataset(JSONObject datasetObject, String creator, File[] files) throws IOException {
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = Config.getConfigProperties().getProperty("dataservice.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardDataset.DATASETS_ENDPOINT;
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardDataset.X_CREDENTIAL_USERNAME, creator);

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.addTextBody(HazardDataset.DATASET_PARAMETER, datasetObject.toString());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        httpPost.setEntity(params.build());
        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JSONObject object = new JSONObject(responseStr);

            String datasetId = object.getString("id");
            requestUrl += "/" + datasetId + "/" + HazardDataset.DATASETS_FILES;

            params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for(File file : files) {
                params.addBinaryBody(HazardDataset.FILE_PARAMETER_, file);
            }

            // Attach file
            httpPost = new HttpPost(requestUrl);
            httpPost.setHeader(HazardDataset.X_CREDENTIAL_USERNAME, creator);
            httpPost.setEntity(params.build());

            response = httpclient.execute(httpPost);
            responseStr = responseHandler.handleResponse(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return datasetId;
            }
        }

        return null;

    }
}
