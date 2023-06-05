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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockAuthorizer implements IAuthorizer {

    private boolean canRead = false;
    private boolean canWrite = false;
    private final boolean canDelete = false;

    public MockAuthorizer(boolean canRead, boolean canWrite) {
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, Privileges spec, List<String> userGroups) {
        Set<PrivilegeLevel> privs = new HashSet<>();
        if (canRead) {
            privs.add(PrivilegeLevel.READ);
        }
        if (canWrite) {
            privs.add(PrivilegeLevel.WRITE);
        }
        return privs;
    }

    @Override
    public Set<PrivilegeLevel> getPrivilegesFor(String user, String privilegeSpecJson, List<String> userGroups) {
        Set<PrivilegeLevel> privs = new HashSet<>();
        if (canRead) {
            privs.add(PrivilegeLevel.READ);
        }
        if (canWrite) {
            privs.add(PrivilegeLevel.WRITE);
        }
        return privs;
    }

    @Override
    public boolean canRead(String user, Privileges privileges, List<String> userGroups) {
        return canRead;
    }

    @Override
    public boolean canRead(String user, String privilegeSpecJson, List<String> userGroups) {
        return canRead;
    }

    @Override
    public boolean canWrite(String user, Privileges privileges, List<String> userGroups) {
        return canWrite;
    }

    @Override
    public boolean canWrite(String user, String privilegeSpecJson, List<String> userGroups) {
        return canWrite;
    }

    @Override
    public boolean canDelete(String user, Privileges privileges, List<String> userGroups) {
        return canDelete;
    }

    @Override
    public Set<String> getAllMembersUserHasReadAccessTo(String username, List<Space> spaces, List<String> userGroups) {
        // Simplified version
        // TODO: update this to reflect the actual method
        Set<String> members = new HashSet<>();
        for (Space space : spaces) {
            if (space.getUserPrivilegeLevel(username) == PrivilegeLevel.ADMIN) {
                members.addAll(space.getMembers());
            }
        }
        return members;
    }

    @Override
    public boolean canUserReadMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        // simplified version (checking if username is the owner of the space
        // TODO: update this to reflect the actual method
        for (Space space : spaces) {
            if (space.getUserPrivilegeLevel(username) == PrivilegeLevel.ADMIN) {
                if (space.hasMember(memberId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canUserDeleteMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        return false;
    }

    @Override
    public boolean isUserAdmin(List<String> userGroups) {
        return false;
    }

    @Override
    public boolean canUserWriteMember(String username, String memberId, List<Space> spaces, List<String> userGroups) {
        return false;
    }

    @Override
    public List<Space> getAllSpacesUserCanRead(String username, List<Space> spaces, List<String> userGroups) {
        return null;
    }


}
