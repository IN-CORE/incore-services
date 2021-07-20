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

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class ServiceUtils {
    public static final String HAZARD_STATUS_ENDPOINT = "hazard/api/status/usage";
    public static final String X_AUTH_USERINFO = "x-auth-userinfo";

    /**
     * utility for creating data endpoint string
     *
     * @return
     */
    public static String createHazardEndpoint(){
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
}
