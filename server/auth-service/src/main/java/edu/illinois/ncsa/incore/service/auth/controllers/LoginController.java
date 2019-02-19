/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.auth.controllers;

import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("login")
public class LoginController {

    private static final Logger logger = Logger.getLogger(LoginController.class);

    /**
     * To authenticate, a header must be sent, of the form:
     * Authorization: LDAP xxxxxxxxxxxxxx
     * where xxxxxx is the base64 encoded "username:password"
     *
     * @param username username of the authenticated user (or anonymous if not authenticated)
     * @param didFail message from kong indicated whether the anonymous user was used instead
     * @return map of the results
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> attemptLogin(@HeaderParam("X-Credential-Username") String username, @HeaderParam("X-Anonymous-Consumer") String didFail, @HeaderParam("auth_token") String authToken) {

        Map<String, String> result = new HashMap<>();
        if (didFail != null || username == null) {
            result.put("result", "failed");
            result.put("user", "anonymous");
            return result;
        }
        result.put("result", "success");
        result.put("user", username);
        result.put("auth-token", authToken);
        return result;
    }

}
