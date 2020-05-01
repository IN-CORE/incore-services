/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.maestro;

    import edu.illinois.ncsa.incore.common.config.Config;

    import javax.ws.rs.container.ContainerRequestContext;
    import javax.ws.rs.container.ContainerResponseContext;
    import javax.ws.rs.container.ContainerResponseFilter;
    import javax.ws.rs.core.MultivaluedMap;
    import java.io.IOException;

public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        // if header origin is in the allowed origin list; add it to access-control-allow-origin header
        String allowedOrigins = Config.getConfigProperties().getProperty("services.allow.origin");
        String requestHeadersOrigin = requestContext.getHeaderString("Origin");
        if (allowedOrigins.contains(requestHeadersOrigin)){
            headers.add("Access-Control-Allow-Origin", requestHeadersOrigin);
        }
        headers.add("Access-Control-Allow-Methods", "GET");
        headers.add("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, " +
            "Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, auth-user, " +
            "auth-token, x-auth-userinfo");
    }
}
