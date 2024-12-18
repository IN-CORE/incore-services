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

import edu.illinois.ncsa.incore.common.models.Space;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
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

    public static IAuthorizer getInstance() {
        if (instance == null) {
            instance = new Authorizer();
        }
        return instance;
    }


    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, Privileges spec, List<String> userGroups) {
        Set<PrivilegeLevel> privs = getUserSpecificPrivileges(user, spec);
        privs.addAll(getGroupSpecificPrivileges(spec, userGroups));
        return privs;
    }


    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, String privilegeSpecJson, List<String> userGroups) {
        Privileges spec = Privileges.fromJson(privilegeSpecJson);
        return getPrivilegesFor(user, spec, userGroups);
    }

    ////////////////////////////////////////////////////////
    // convenience methods to make it easier to query
    /////////////////////////////////////////////////////////

    @Override
    public boolean canRead(String user, Privileges privileges, List<String> userGroups) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privileges, userGroups);
        return (privilegesFor.contains(PrivilegeLevel.READ) ||
            privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canRead(String user, String privilegeSpecJson, List<String> userGroups) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privilegeSpecJson, userGroups);
        return (privilegesFor.contains(PrivilegeLevel.READ) ||
            privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canWrite(String user, Privileges privileges, List<String> userGroups) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privileges, userGroups);
        return (privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canWrite(String user, String privilegeSpecJson, List<String> userGroups) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privilegeSpecJson, userGroups);
        return (privilegesFor.contains(PrivilegeLevel.WRITE) ||
            privilegesFor.contains(PrivilegeLevel.ADMIN)
        );
    }

    @Override
    public boolean canDelete(String user, Privileges privileges, List<String> userGroups) {
        Set<PrivilegeLevel> privilegesFor = getPrivilegesFor(user, privileges, userGroups);
        return (privilegesFor.contains(PrivilegeLevel.ADMIN));
    }

    ////////////////////////////////////////////////////////
    // methods to get all members a user has access to
    /////////////////////////////////////////////////////////

    /**
     * Return a list of spaces a user can read
     *
     * @param username string name of user
     * @param spaces   a list of all spaces
     * @param userGroups a list of groups the user belongs to
     * @return a list of accessible spaces
     */
    public List<Space> getAllSpacesUserCanRead(String username, List<Space> spaces, List<String> userGroups) {
        return getSpacesUserCanRead(spaces, username, userGroups);
    }

    /**
     * Retrieve all members a user has access to
     *
     * @param username string name of user
     * @param spaces   list of all spaces the user has at least READ permission
     * @param userGroups a list of groups the user belongs to
     * @return a set of all members a user can access with at least READ permission
     */
    public Set<String> getAllMembersUserHasReadAccessTo(String username, List<Space> spaces, List<String> userGroups) {
        Set<String> membersSet = new HashSet<>();
        List<Space> userAccessibleSpaces = getSpacesUserCanRead(spaces, username, userGroups);
        for (Space space : userAccessibleSpaces) {
            membersSet.addAll(space.getMembers());
        }
        return membersSet;
    }

    /**
     * Determines if the user has permission to read the memberId (dataset, hazard, etc)
     *
     * @param memberId the String id of the member
     * @param spaces   a list of all spaces
     * @param userGroups a list of groups the user belongs to
     * @return boolean - true if the user has reading permissions on a space tha contains the member
     */
    public boolean canUserReadMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        Set<String> userMembersSet = getAllMembersUserHasReadAccessTo(username, spaces, userGroups);
        return userMembersSet.contains(memberId);
    }

    /**
     * This method determines if a user can delete a member based on its privileges on a space the member is part of, or
     * based on a special group the user might be part of.
     *
     * @param username string name of the user
     * @param memberId the string id of the member
     * @param spaces   a list of all spaces
     * @param userGroups a list of groups the user belongs to
     * @return boolean - true if the user has admin permissions on a space that contains the member
     */
    public boolean canUserDeleteMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        List<Space> userSpaces = spaces.stream()
            .filter(space -> canUserAccessSpace(space, username, PrivilegeLevel.ADMIN, userGroups))
            .collect(Collectors.toList());

        Set<String> membersSet = new HashSet<>();
        for (Space space : userSpaces) {
            membersSet.addAll(space.getMembers());
        }
        return membersSet.contains(memberId);
    }

    /**
     * This method determines if a user can add a member to a space (or modify it) based on its privileges on a space the member is part
     * of, or
     * based on a special group the user might be part of.
     *
     * @param username string name of the user
     * @param memberId string id of the member
     * @param spaces   a list of all spaces
     * @param userGroups a list of groups the user belongs to
     * @return boolean - true if the user has write or admin permissions on a space that contains the member
     */
    public boolean canUserWriteMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        List<Space> userSpaces = spaces.stream()
            .filter(space -> canUserAccessSpace(space, username, PrivilegeLevel.WRITE, userGroups))
            .collect(Collectors.toList());

        Set<String> membersSet = new HashSet<>();
        for (Space space : userSpaces) {
            membersSet.addAll(space.getMembers());
        }
        return membersSet.contains(memberId);
    }

    /**
     * This method determines if a user is an admin or not.
     *
     * @param userGroups list of strings of groups user belongs to
     * @return boolean - true if the user is admin
     */
    public boolean isUserAdmin(List<String> userGroups) {
        return (userGroups.contains("incore_admin") || userGroups.contains("incore_ncsa"));
    }

    ////////////////////////////////////////////////////////
    // private methods
    /////////////////////////////////////////////////////////

    private Set<PrivilegeLevel> getGroupSpecificPrivileges(Privileges spec, List<String> userGroups) {
        if (spec == null) {
            return allowThisUser();
        }
        try {
            if (isUserAdmin(userGroups)) {
                HashSet<PrivilegeLevel> admin = new HashSet<>();
                admin.add(PrivilegeLevel.ADMIN);
                admin.add(PrivilegeLevel.WRITE);
                admin.add(PrivilegeLevel.READ);
                return admin;
            }

            Set<PrivilegeLevel> privs = spec.groupPrivileges.keySet().stream()
                .filter(userGroups::contains)
                .map(key -> spec.groupPrivileges.get(key))
                .collect(Collectors.toSet());

            //if the user is in the magic view-all group, give them read access
            if (userGroups.contains("incore_admin") || userGroups.contains("incore_ncsa")) {
                privs.add(PrivilegeLevel.READ);
            }

            return privs;


        } catch (Exception e) {
            logger.error(e);
        }
        return new HashSet<PrivilegeLevel>();
    }

    private Set<PrivilegeLevel> allowThisUser() {
        Set<PrivilegeLevel> allowed = new HashSet<>();
        allowed.add(PrivilegeLevel.ADMIN);
        return allowed;
    }

    private Set<PrivilegeLevel> getUserSpecificPrivileges(String user, Privileges spec) {
        if (spec == null) {
            return allowThisUser();
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

    /**
     * Retrieve all spaces that a user has at least READ permissions
     *
     * @param spaces   List of all spaces in spacedb
     * @param username Name of user's space
     * @param userGroups list of strings of groups user belongs to
     * @return List of spaces that a user can access with at least READ permission
     */
    private List<Space> getSpacesUserCanRead(List<Space> spaces, String username, List<String> userGroups) {
        return spaces.stream()
            .filter(space -> canUserAccessSpace(space, username, PrivilegeLevel.READ, userGroups))
            .collect(Collectors.toList());
    }

    //TODO: review this implementation

    /**
     * This method is to check if a user can access a space with a specific privilege level
     *
     * @param space          the space retrieved from the repository
     * @param username       the string name of the user
     * @param privilegeLevel the privilege level we want to check for
     * @param userGroups list of strings of groups user belongs to
     * @return boolean - true if the user has the specified privilege level
     */
    private boolean canUserAccessSpace(Space space, String username, PrivilegeLevel privilegeLevel, List<String> userGroups) {
        if (privilegeLevel == PrivilegeLevel.READ) {
            return space.getUserPrivilegeLevel(username) != null ||
                space.getGroupPrivilegeLevel(username) != null ||
                !getGroupSpecificPrivileges(space.getPrivileges(), userGroups).isEmpty();
        } else if (privilegeLevel == PrivilegeLevel.WRITE) {
            return space.getUserPrivilegeLevel(username) == PrivilegeLevel.WRITE || space.getUserPrivilegeLevel(username) == PrivilegeLevel.ADMIN ||
                space.getGroupPrivilegeLevel(username) == PrivilegeLevel.WRITE || space.getGroupPrivilegeLevel(username) == PrivilegeLevel.ADMIN ||
                (getGroupSpecificPrivileges(space.getPrivileges(), userGroups)).contains(PrivilegeLevel.WRITE) ||
                (getGroupSpecificPrivileges(space.getPrivileges(), userGroups)).contains(PrivilegeLevel.ADMIN);
        } else if (privilegeLevel == PrivilegeLevel.ADMIN) {
            return space.getUserPrivilegeLevel(username) == PrivilegeLevel.ADMIN ||
                space.getGroupPrivilegeLevel(username) == PrivilegeLevel.ADMIN ||
                (getGroupSpecificPrivileges(space.getPrivileges(), userGroups)).contains(PrivilegeLevel.ADMIN);
        }
        return false;
    }


}
