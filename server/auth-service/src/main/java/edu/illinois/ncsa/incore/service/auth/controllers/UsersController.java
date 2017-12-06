/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.auth.controllers;

import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.LdapClient;
import edu.illinois.ncsa.incore.common.users.UserInfo;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("users")
public class UsersController {

    private static final Logger logger = Logger.getLogger(UsersController.class);

    @GET
    @Path("{usernameQuery}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfo attemptLogin(@HeaderParam("X-Credential-Username") String currentUser, @PathParam("usernameQuery") String usernameQuery) {

        if (currentUser == null || "".equals(currentUser) || Authorizer.ANONYMOUS_USER.equals(currentUser)){
            throw new NotAuthorizedException("Authorization Required");
        }

        LdapClient ldap = new LdapClient();
        UserInfo userInfo = ldap.getUserInfoFor(usernameQuery);
        if (userInfo == null) {
            throw new NotFoundException();
        }
        return userInfo;
    }

}
