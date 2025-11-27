package edu.illinois.ncsa.incore.service.dfr3.utils;

import edu.illinois.ncsa.incore.common.SemanticsConstants;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicResponseHandler;


import java.io.IOException;


public class ServiceUtil {

    private static final String SEMANTICS_ENDPOINT = "http://localhost:8080/semantics/api/types";

    public static String getJsonFromSemanticsEndpoint(String name, String username, String userGroups) throws IOException {

        String dataEndpoint = createEndpoint();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + SemanticsConstants.SEMANTICS_ENDPOINT + "/" + name;

        HttpGet httpGet = new HttpGet(requestUrl);
        httpGet.setHeader(SemanticsConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + username + "\"}");
        httpGet.setHeader(SemanticsConstants.X_AUTH_USERGROUP, userGroups);
        httpGet.setHeader("contentType", "application/json");
        httpGet.setHeader("accept", "application/json");

        HttpResponse response = null;

        response = httpclient.execute(httpGet);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        return responseHandler.handleResponse(response);
    }

    public static String createEndpoint() {
        String endpoint = "http://localhost:8080/";
        String endpointProp = System.getenv("SERVICES_URL");
        if (endpointProp != null && !endpointProp.isEmpty()) {
            endpoint = endpointProp;
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
        }

        return endpoint;
    }

}
