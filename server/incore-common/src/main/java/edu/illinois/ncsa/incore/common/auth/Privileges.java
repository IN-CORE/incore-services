/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Nathan Tolbert
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A definition of privileges, which are applied to a single
 * entity in incore. The Privileges and Authorizer don't
 * know anything about the actual entity. They only care about
 * what the Privileges indicates about who is allowed to access it.
 */
public class Privileges {

    private static final Logger log = Logger.getLogger(Privileges.class);


    public Map<String,PrivilegeLevel> userPrivileges;
    public Map<String,PrivilegeLevel> groupPrivileges;

    public Privileges() {
        userPrivileges = new HashMap<String, PrivilegeLevel>();
        groupPrivileges = new HashMap<String, PrivilegeLevel>();
    }

    public static Privileges fromJson(String privilegeSpecJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(privilegeSpecJson, Privileges.class);
        } catch (Exception e) {
            log.error("Could not parse privilegeSpec JSON. Returning empty Privileges", e);
            return new Privileges();
        }
    }

    public static Privileges newWithSingleOwner(String owner) {
        Privileges privileges = new Privileges();
        privileges.userPrivileges.put(owner, PrivilegeLevel.ADMIN);
        return privileges;
    }

}
