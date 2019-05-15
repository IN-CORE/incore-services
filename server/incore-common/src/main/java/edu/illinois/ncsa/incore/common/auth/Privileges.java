/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Nathan Tolbert
 *******************************************************************************/

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
        if (owner == null) {
            log.warn("Owner not specified (did you authenticate, or forget to set X-Credential-Username?)");
        } else {
            privileges.userPrivileges.put(owner, PrivilegeLevel.ADMIN);
        }
        return privileges;
    }

    public Map<String, PrivilegeLevel> getUserPrivileges() {
        return this.userPrivileges;
    }

    public Map<String, PrivilegeLevel> getGroupPrivileges() {
        return this.groupPrivileges;
    }

    public void addUserPrivileges(String owner, PrivilegeLevel privilegeLevel){
        if(userPrivileges != null){
            this.userPrivileges.put(owner, privilegeLevel);
        }
        else{
            throw new NullPointerException("userPrivileges was not instantiated");
        }
    }

    public void addUserPrivilegesMap(Map<String, PrivilegeLevel> userPrivilegesMap) {
        this.userPrivileges.putAll(userPrivilegesMap);
    }

    public void addGroupPrivilegesMap(Map<String, PrivilegeLevel> groupPrivilegesMap) {
        this.groupPrivileges.putAll(groupPrivilegesMap);
    }

    public PrivilegeLevel getUserPrivilegeLevel(String username) {
        if (userPrivileges.containsKey(username)) {
            return userPrivileges.get(username);
        }
        return null;
    }

    public PrivilegeLevel getGroupPrivilegeLevel(String groupName) {
        if (groupPrivileges.containsKey(groupName)) {
            return groupPrivileges.get(groupName);
        }
        return null;
    }


}
