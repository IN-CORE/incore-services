/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.controllers;

import edu.illinois.ncsa.incore.service.maestro.daos.IRepository;
import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import edu.illinois.ncsa.incore.service.maestro.models.AnalysisMetadata;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("login")
public class LoginController {

    private static final Logger logger = Logger.getLogger(LoginController.class);

    /**
     * To authenticate, a header must be sent, of the form:
     * Authorization: LDAP xxxxxxxxxxxxxx
     * where xxxxxx is the base64 encoded "username:password"
     * @param username
     * @param didFail
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> attemptLogin(@HeaderParam("X-Credential-Username") String username, @HeaderParam("X-Anonymous-Consumer") String didFail) {

        Map<String,String> result = new HashMap<>();
        if (didFail != null || username == null) {
            result.put("result", "failed");
            result.put("user", "anonymous");
            return result;
        }
        result.put("result", "success");
        result.put("user", username);
        return result;
    }

}
