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

import java.util.List;
import java.util.Set;

public interface IAuthorizer {
    Set<PrivilegeLevel> getPrivilegesFor(String user, Privileges spec, List<String> userGroups);

    Set<PrivilegeLevel> getPrivilegesFor(String user, String privilegeSpecJson, List<String> userGroups);

    boolean canRead(String user, Privileges privileges, List<String> userGroups);

    boolean canRead(String user, String privilegeSpecJson, List<String> userGroups);

    boolean canWrite(String user, Privileges privileges, List<String> userGroups);

    boolean canWrite(String user, String privilegeSpecJson, List<String> userGroups);

    boolean canDelete(String user, Privileges privileges, List<String> userGroups);

    List<Space> getAllSpacesUserCanRead(String username, List<Space> spaces, List<String> userGroups);

    Set<String> getAllMembersUserHasReadAccessTo(String username, List<Space> spaces, List<String> userGroups);

    boolean canUserReadMember(String username, String memberId, List<Space> spaces, List<String> userGroups);

    boolean canUserWriteMember(String username, String memberId, List<Space> spaces, List<String> userGroups);

    boolean canUserDeleteMember(String username, String memberId, List<Space> spaces, List<String> userGroups);

    boolean isUserAdmin(List<String> userGroups);

}
