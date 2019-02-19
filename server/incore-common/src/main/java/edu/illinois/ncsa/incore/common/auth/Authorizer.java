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

import edu.illinois.ncsa.incore.common.config.Config;
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
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privileges);
        return (privilegesFor.contains(PrivilegeLevel.READ) ||
            privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canRead(String user, String privilegeSpecJson) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privilegeSpecJson);
        return (privilegesFor.contains(PrivilegeLevel.READ) ||
            privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canWrite(String user, Privileges privileges) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privileges);
        return (privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canWrite(String user, String privilegeSpecJson) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privilegeSpecJson);
        return (privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }


    private Set<PrivilegeLevel> getGroupSpecificPrivileges(String user, Privileges spec) {
        if (spec == null) {
            return allowThisUser(user);
        }
        try {
            LdapClient ldapClient = getLdapClient();
            Set<String> userGroups = ldapClient.getUserGroups(user);

            //if the user is an admin, they get full privileges
            if (userGroups.contains(Config.getConfigProperties().getProperty("auth.ldap.admins"))) {
                HashSet<PrivilegeLevel> admin  = new HashSet<>();
                admin.add(PrivilegeLevel.ADMIN);
                admin.add(PrivilegeLevel.WRITE);
                admin.add(PrivilegeLevel.READ);
                return admin;
            }


            Set <PrivilegeLevel> privs = spec.groupPrivileges.keySet().stream()
                .filter(userGroups::contains)
                .map(key -> spec.groupPrivileges.get(key))
                .collect(Collectors.toSet());

            //if the user is in the magic view-all group, give them read access
            if (userGroups.contains(Config.getConfigProperties().getProperty("auth.ldap.viewall"))) {
                privs.add(PrivilegeLevel.READ);
            }

            return privs;


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
