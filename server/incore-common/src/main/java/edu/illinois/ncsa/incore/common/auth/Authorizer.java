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

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Authorizer is used by various services to determine if,
 * based on an entity's Privileges and a user, whether that user
 * has a specific privilege for the entity
 */
public class Authorizer implements IAuthorizer {

    private static final Logger logger = Logger.getLogger(Authorizer.class);

    public static final String ANONYMOUS_USER = "anonymous";
    private static IAuthorizer instance;
    private LdapClient ldapClient;

    //I don't really like the idea of doing this as a singleton,
    //this thing is going to need some configuration to know what
    //ldap it connects to, etc. Some sort of configuration or dependency injection
    //might be preferable, but that's not going to happen for the prototype
    public static IAuthorizer getInstance() {
        if (instance == null) {
            instance = new Authorizer();
        }
        return instance;
    }


    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, Privileges spec) {
        Set<PrivilegeLevel> privs = getUserSpecificPrivileges(user, spec);
        privs.addAll(getGroupSpecificPrivileges(user, spec));
        return privs;
    }


    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, String privilegeSpecJson) {
        Privileges spec = Privileges.fromJson(privilegeSpecJson);
        return getPrivilegesFor(user, spec);
    }

    ////////////////////////////////////////////////////////
    // convenience methods to make it easier to query
    /////////////////////////////////////////////////////////

    @Override
    public boolean canRead(String user, Privileges privileges) {
        return getPrivilegesFor(user, privileges).contains(PrivilegeLevel.READ);
    }

    @Override
    public boolean canRead(String user, String privilegeSpecJson) {
        return getPrivilegesFor(user, privilegeSpecJson).contains(PrivilegeLevel.READ);
    }

    @Override
    public boolean canWrite(String user, Privileges privileges) {
        return getPrivilegesFor(user, privileges).contains(PrivilegeLevel.WRITE);
    }

    @Override
    public boolean canWrite(String user, String privilegeSpecJson) {
        return getPrivilegesFor(user, privilegeSpecJson).contains(PrivilegeLevel.WRITE);
    }


    private Set<PrivilegeLevel> getGroupSpecificPrivileges(String user, Privileges spec) {
        if (spec == null) {
            return allowThisUser(user);
        }
        try {
            LdapClient ldapClient = getLdapClient();
            Set<String> userGroups = ldapClient.getUserGroups(user);
            return spec.groupPrivileges.keySet().stream()
                .filter(userGroups::contains)
                .map(key -> spec.groupPrivileges.get(key))
                .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error(e);
        }
        return new HashSet<PrivilegeLevel>();
    }

    private Set<PrivilegeLevel> allowThisUser(String user) {
        Set<PrivilegeLevel> allower = new HashSet<>();
        allower.add(PrivilegeLevel.ADMIN);
        return allower;
    }

    private Set<PrivilegeLevel> getUserSpecificPrivileges(String user, Privileges spec) {
        if (spec == null) {
            return allowThisUser(user);
        }
        try {
            return spec.userPrivileges.keySet().stream()
                .filter(user::equals)
                .map(key -> spec.userPrivileges.get(key))
                .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error(e);
        }
        return new HashSet<PrivilegeLevel>();
    }

    private LdapClient getLdapClient() {
        if (ldapClient == null) {
            ldapClient = new LdapClient();
        }
        return ldapClient;
    }


}
